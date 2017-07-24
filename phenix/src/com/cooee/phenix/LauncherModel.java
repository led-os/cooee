package com.cooee.phenix;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.Category.CategoryParse;
import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherSettings.Favorites;
import com.cooee.phenix.AppList.KitKat.WidgetPreviewLoader;
import com.cooee.phenix.Folder.Folder;
import com.cooee.phenix.Functions.Category.OperateHelp;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicMain;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.config.defaultConfig.LauncherIconBaseConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.EnhanceItemInfo;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.LauncherAppWidgetInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.phenix.data.VirtualInfo;
import com.cooee.phenix.iconhouse.IconHouseManager;
import com.cooee.phenix.util.ZhiKeShortcutManager;
import com.cooee.phenix.widget.timer.TimeAppWidgetProvider;
import com.cooee.phenix.widget.timer.TimeUpdateTask;
import com.cooee.theme.ThemeManager;
import com.cooee.update.UpdateIconManager;
import com.cooee.util.Tools;
import com.iLoong.launcher.MList.MeLauncherInterface;

import cool.sdk.Category.CategoryConstant;
import cool.sdk.Category.CategoryHelper;


/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state
 * for the Launcher.
 */
public class LauncherModel extends BroadcastReceiver
{
	
	// zhangjin@2015/07/28 UPD START
	//static final boolean DEBUG_LOADERS = false;
	static boolean DEBUG_LOADERS = true;
	// zhangjin@2015/07/28 UPD END
	static final String TAG = "Launcher.Model";
	// true = use a "More Apps" folder for non-workspace apps on upgrade
	// false = strew non-workspace apps across the workspace on upgrade
	public static final boolean UPGRADE_USE_MORE_APPS_FOLDER = false;
	private static final int ITEMS_CHUNK = 6; // batch size for the workspace icons
	private final boolean mAppsCanBeOnRemoveableStorage;
	private final LauncherAppState mApp;
	private final Object mLock = new Object();
	private DeferredHandler mHandler = new DeferredHandler();
	private LoaderTask mLoaderTask;
	private boolean mIsLoaderTaskRunning;
	private volatile boolean mFlushingWorkerThread;
	// Specific runnable types that are run on the main thread deferred handler, this allows us to
	// clear all queued binding runnables when the Launcher activity is destroyed.
	private static final int MAIN_THREAD_NORMAL_RUNNABLE = 0;
	private static final int MAIN_THREAD_BINDING_RUNNABLE = 1;
	private static final HandlerThread sWorkerThread = new HandlerThread( "launcher-loader" );
	static
	{
		sWorkerThread.start();
	}
	private static final Handler sWorker = new Handler( sWorkerThread.getLooper() );
	// We start off with everything not loaded.  After that, we assume that
	// our monitoring of the package manager provides all updates and we never
	// need to do a requery.  These are only ever touched from the loader thread.
	private boolean mWorkspaceLoaded;
	private boolean mAllAppsLoaded;
	// When we are loading pages synchronously, we can't just post the binding of items on the side
	// pages as this delays the rotation process.  Instead, we wait for a callback from the first
	// draw (in Workspace) to initiate the binding of the remaining side pages.  Any time we start
	// a normal load, we also clear this set of Runnables.
	static final ArrayList<Runnable> mDeferredBindRunnables = new ArrayList<Runnable>();
	private WeakReference<Callbacks> mCallbacks;
	// < only access in worker thread >
	public AllAppsList mBgAllAppsList;
	// The lock that must be acquired before referencing any static bg data structures.  Unlike
	// other locks, this one can generally be held long-term because we never expect any of these
	// static data structures to be referenced outside of the worker thread except on the first
	// load after configuration change.
	static final Object sBgLock = new Object();
	// sBgItemsIdMap maps *all* the ItemInfos (shortcuts, folders, and widgets) created by
	// LauncherModel to their ids
	static final HashMap<Long , ItemInfo> sBgItemsIdMap = new HashMap<Long , ItemInfo>();
	// sBgWorkspaceItems is passed to bindItems, which expects a list of all folders and shortcuts
	//       created by LauncherModel that are directly on the home screen (however, no widgets or
	//       shortcuts within folders).
	static final ArrayList<ItemInfo> sBgWorkspaceItems = new ArrayList<ItemInfo>();
	// sBgAppWidgets is all LauncherAppWidgetInfo created by LauncherModel. Passed to bindAppWidget()
	static final ArrayList<LauncherAppWidgetInfo> sBgAppWidgets = new ArrayList<LauncherAppWidgetInfo>();
	// sBgFolders is all FolderInfos created by LauncherModel. Passed to bindFolders()
	static final HashMap<Long , FolderInfo> sBgFolders = new HashMap<Long , FolderInfo>();
	// sBgDbIconCache is the set of ItemInfos that need to have their icons updated in the database
	static final HashMap<Object , byte[]> sBgDbIconCache = new HashMap<Object , byte[]>();
	// sBgWorkspaceScreens is the ordered set of workspace screens.
	static final ArrayList<Long> sBgWorkspaceScreens = new ArrayList<Long>();
	// </ only access in worker thread >
	private IconCache mIconCache;
	private Bitmap mDefaultIcon;
	protected int mPreviousConfigMcc;
	//cheyingkun start	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
	//cheyingkun del start
	//	// 用于保存当智能分类时候，此时发送安装和卸载图标广播时候，存储的数据wanghongjian@2015/04/27 UPD START
	//	private ArrayList<Integer> mOps = new ArrayList<Integer>();
	//	private ArrayList<String> mPkgNames = new ArrayList<String>();//由于此时接受的广播可能有很多个数据一起发送，因此用list存储
	//	// wanghongjian@2015/04/27 UPD END
	//cheyingkun del end
	/**加载时收到T卡插入或拔出广播时,把操作保存,等桌面加载完毕再遍历列表执行该操作*/
	public ArrayList<PackageUpdatedTask> mTCardMountOpInLoad = new ArrayList<LauncherModel.PackageUpdatedTask>();//cheyingkun add	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
	/**所有配置空位的数据列表*/
	static final ArrayList<EmtySeatEntity> mEmtySeatEntityList = new ArrayList<EmtySeatEntity>();//cheyingkun add	//桌面支持配置空位【c_0003636】
	// chenchen add start //从Launcher类中获取tm，sb对象
	private TelephonyManager mTelephonyManager;
	private List<ResolveInfo> apps;
	
	// chenchen add end
	public interface Callbacks
	{
		
		public boolean setLoadOnResume();
		
		public int getCurrentWorkspaceScreen();
		
		public void startBinding();
		
		public void bindItems(
				ArrayList<ItemInfo> shortcuts ,
				int start ,
				int end ,
				boolean forceAnimateIcons ,
				Runnable runnable ,//zhujieping add
				boolean isLoadFinish );//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页(逻辑优化)
		
		public void bindScreens(
				ArrayList<Long> orderedScreenIds );
		
		public void bindAddScreens(
				ArrayList<Long> orderedScreenIds );
		
		public void bindFolders(
				HashMap<Long , FolderInfo> folders );
		
		public void finishBindingItems(
				boolean upgradePath );
		
		public void bindAppWidget(
				LauncherAppWidgetInfo info );
		
		public void bindAllApplications(
				ArrayList<AppInfo> apps );
		
		public void bindItemsAdded(
				ArrayList<Long> newScreens ,
				ArrayList<ItemInfo> addNotAnimated ,
				ArrayList<ItemInfo> addAnimated ,
				ArrayList<AppInfo> addedItems ,
				boolean installApp ,//cheyingkun add	//解决“桌面启动后立即点击打开文件夹，文件夹打开后，过会会自动关闭”的问题。【i_0014084】
				boolean isLoadFinish );//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页(逻辑优化)
		
		public void bindAppsUpdated(
				ArrayList<AppInfo> apps );
		
		public void bindComponentsRemoved(
				ArrayList<String> packageNames ,
				ArrayList<AppInfo> appInfos ,
				boolean matchPackageNamesOnly );
		
		public void bindPackagesUpdated(
				ArrayList<Object> widgetsAndShortcuts );
		
		public void bindSearchablesChanged();
		
		public boolean isAllAppsButtonRank(
				int rank );
		
		public void onPageBoundSynchronously(
				int page );
		
		public void dumpLogsToLocalData();
		
		public void closeFolder();
		
		public void onAppsChanged(
				String pkgName ,
				String action ); //WangLei add //bug:0010453 //安装、更新或卸载应用时，回调此方法，//zhujieping 增加变量，增强扩展性
		
		public void loadFinish();//桌面加载完毕	//cheyingkun add	//优化加载速度(统计和sdk放到桌面加载完再初始化)
		
		public void clearOperateIcon(
				String[] pkgNames );
		
		//xiatian add start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
		public void bindDefaultPage(
				final Callbacks oldCallbacks );
		//xiatian add end
		;
		
		public void dismissLoadingPage();//cheyingkun add	//桌面启动页样式（详见“BaseDefaultConfig”中说明）
		
		//cheyingkun add start	//应用自动创建快捷方式时，优化判断是否存在的方法。【c_0003813】
		public boolean shortcutExistsByWorkspaceItems(
				String name ,
				Intent intent );
		//cheyingkun add end
		;
		
		public int getDefaultPageFromSharedPreferences();//xiatian add	//解决“加载桌面时，先显示第一页，再跳到默认主页”的问题（由“加载item完毕之后，再跳到默认主页”改为“在加载item之前，直接跳到默认主页”）。【c_0004499】
	}
	
	public interface ItemInfoFilter
	{
		
		public boolean filterItem(
				ItemInfo parent ,
				ItemInfo info ,
				ComponentName cn );
	}
	
	LauncherModel(
			LauncherAppState app ,
			IconCache iconCache )
	{
		// zhangjin@2015/07/28 ADD START
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			DEBUG_LOADERS = true;
		}
		// zhangjin@2015/07/28 ADD END
		final Context context = app.getContext();
		mAppsCanBeOnRemoveableStorage = Environment.isExternalStorageRemovable();
		mApp = app;
		mBgAllAppsList = new AllAppsList( iconCache );
		mIconCache = iconCache;
		//		mDefaultIcon = Utilities.createIconBitmap( mIconCache.getFullResDefaultActivityIcon() , context );//xiatian del	//优化桌面启动速度，mDefaultIcon不应该在LauncherModel的初始化中生成，应该在需要使用的时候生成。
		final Resources res = context.getResources();
		Configuration config = res.getConfiguration();
		mPreviousConfigMcc = config.mcc;
	}
	
	/** Runs the specified runnable immediately if called from the main thread, otherwise it is
	 * posted on the main thread handler. */
	private void runOnMainThread(
			Runnable r )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "zjp" , "runOnMainThread" );
		runOnMainThread( r , 0 );
	}
	
	private void runOnMainThread(
			Runnable r ,
			int type )
	{
		if( sWorkerThread.getThreadId() == Process.myTid() )
		{
			// If we are on the worker thread, post onto the main handler
			mHandler.post( r );
		}
		else
		{
			r.run();
		}
	}
	
	/** Runs the specified runnable immediately if called from the worker thread, otherwise it is
	 * posted on the worker thread handler. */
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//private static void runOnWorkerThread(
	public static void runOnWorkerThread(
			//添加智能分类功能 , change by shlt@2015/02/09 UPD END
			Runnable r )
	{
		if( sWorkerThread.getThreadId() == Process.myTid() )
		{
			r.run();
		}
		else
		{
			// If we are not on the worker thread, then post to the worker handler
			sWorker.post( r );
		}
	}
	
	static boolean findNextAvailableIconSpaceInScreen(
			ArrayList<ItemInfo> items ,
			int[] xy ,
			long screen )
	{
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		final int xCount = (int)grid.getNumColumns();
		final int yCount = (int)grid.getNumRows();
		boolean[][] occupied = new boolean[xCount][yCount];
		int cellX , cellY , spanX , spanY;
		for( int i = 0 ; i < items.size() ; ++i )
		{
			final ItemInfo item = items.get( i );
			if( item.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP )
			{
				if( item.getScreenId() == screen )
				{
					cellX = item.getCellX();
					cellY = item.getCellY();
					spanX = item.getSpanX();
					spanY = item.getSpanY();
					for( int x = cellX ; 0 <= x && x < cellX + spanX && x < xCount ; x++ )
					{
						for( int y = cellY ; 0 <= y && y < cellY + spanY && y < yCount ; y++ )
						{
							occupied[x][y] = true;
						}
					}
				}
			}
		}
		//cheyingkun add start	//桌面支持配置空位【c_0003636】
		for( EmtySeatEntity mEmtySeatEntity : mEmtySeatEntityList )
		{
			if( mEmtySeatEntity.getScreen() == screen )
			{
				int x = mEmtySeatEntity.getCellX();
				int y = mEmtySeatEntity.getCellY();
				if( x >= 0 && x < xCount && y >= 0 && y < yCount )
				{
					occupied[x][y] = true;
				}
			}
		}
		//cheyingkun add end
		return CellLayout.findVacantCell( xy , 1 , 1 , xCount , yCount , occupied );
	}
	
	public static Pair<Long , int[]> findNextAvailableIconSpace(
			Context context ,
			int firstScreenIndex ,
			ArrayList<Long> workspaceScreens )
	{
		return findNextAvailableIconSpace( context , firstScreenIndex , workspaceScreens , null );
	}
	
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//static Pair<Long , int[]> findNextAvailableIconSpace(
	public static Pair<Long , int[]> findNextAvailableIconSpace(
			//添加智能分类功能 , change by shlt@2015/02/09 UPD END
			Context context ,
			int firstScreenIndex ,
			ArrayList<Long> workspaceScreens ,
			ArrayList<ItemInfo> items )
	{
		LauncherAppState app = LauncherAppState.getInstance();
		LauncherModel model = app.getModel();
		if( sWorkerThread.getThreadId() != Process.myTid() )
		{
			// Flush the LauncherModel worker thread, so that if we just did another
			// processInstallShortcut, we give it time for its shortcut to get added to the
			// database (getItemsInLocalCoordinates reads the database)
			model.flushWorkerThread();
		}
		// Lock on the app so that we don't try and get the items while apps are being added
		boolean found = false;
		synchronized( app )
		{
			//zhujieping add如果方法中传入list，则不要再查找数据库
			if( items == null )
				items = LauncherModel.getItemsInLocalCoordinates( context );
			// Try adding to the workspace screens incrementally, starting at the default or center
			// screen and alternating between +1, -1, +2, -2, etc. (using ~ ceil(i/2f)*(-1)^(i-1))
			firstScreenIndex = Math.min( firstScreenIndex , workspaceScreens.size() );
			int count = workspaceScreens.size();
			for( int screenIndex = firstScreenIndex ; screenIndex < count && !found ; screenIndex++ )
			{
				int[] tmpCoordinates = new int[2];
				if( findNextAvailableIconSpaceInScreen( items , tmpCoordinates , workspaceScreens.get( screenIndex ) ) )
				{
					// Update the Launcher db
					return new Pair<Long , int[]>( workspaceScreens.get( screenIndex ) , tmpCoordinates );
				}
			}
		}
		return null;
	}
	
	public void addAndBindAddedItems(
			final Context context ,
			final ArrayList<ItemInfo> workspaceApps ,
			final ArrayList<ItemInfo> workspaceAppsNeed2Combine ,//xiatian add	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。//接受广播“ACTION_INSTALL_SHORTCUT”，自动生成快捷方式时，若是EXTRA_SHORTCUT_ICON，则在将原始图片存入数据库后，再对info的icon加背板、蒙版和盖板
			final ArrayList<AppInfo> allAppsApps ,
			final boolean installApp ,//cheyingkun add	//解决“桌面启动后立即点击打开文件夹，文件夹打开后，过会会自动关闭”的问题。【i_0014084】
			final boolean isLoadFinish )//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页(逻辑优化)
	{
		Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
		addAndBindAddedItems( context , workspaceApps , workspaceAppsNeed2Combine , cb , allAppsApps , installApp , isLoadFinish );
	}
	
	public void addAndBindAddedItems(
			final Context context ,
			final ArrayList<ItemInfo> workspaceApps ,
			final ArrayList<ItemInfo> workspaceAppsNeed2Combine ,//xiatian add	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。//接受广播“ACTION_INSTALL_SHORTCUT”，自动生成快捷方式时，若是EXTRA_SHORTCUT_ICON，则在将原始图片存入数据库后，再对info的icon加背板、蒙版和盖板
			final Callbacks callbacks ,
			final ArrayList<AppInfo> allAppsApps ,
			final boolean installApp ,//cheyingkun add	//解决“桌面启动后立即点击打开文件夹，文件夹打开后，过会会自动关闭”的问题。【i_0014084】
			final boolean isLoadFinish )//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页(逻辑优化)
	{
		//<i_0010422> liuhailin@2015-03-10 modify begin
		//if( workspaceApps.isEmpty() && allAppsApps.isEmpty() )
		if( ( workspaceApps != null && workspaceApps.isEmpty() ) && ( allAppsApps != null && allAppsApps.isEmpty() ) )
		//<i_0010422> liuhailin@2015-03-10 modify end
		{
			return;
		}
		// Process the newly added applications and add them to the database first
		Runnable r = new Runnable() {
			
			public void run()
			{
				final ArrayList<ItemInfo> addedItemsFinal = new ArrayList<ItemInfo>();
				final ArrayList<Long> addedWorkspaceScreensFinal = new ArrayList<Long>();
				// Get the list of workspace screens.  We need to append to this list and
				// can not use sBgWorkspaceScreens because loadWorkspace() may not have been
				// called.
				ArrayList<Long> workspaceScreens = new ArrayList<Long>();
				TreeMap<Integer , Long> orderedScreens = loadWorkspaceScreensDb( context );
				//cheyingkun add start	//为bug:c_0003819添加log，（打开配置项switch_enable_debug启动）。【c_0003819】
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.i(
							"cyk_bug : c_0003819" ,
							StringUtils.concat(
									"cyk launcherModel addAndBindAddedItems: orderedScreens.size(): " ,
									orderedScreens.size() ,
									"-sBgWorkspaceScreens.size(): " ,
									sBgWorkspaceScreens.size() ,
									"-workspaceApps.size(): " ,
									workspaceApps.size() ) );
				}
				//cheyingkun add end
				// hp@2015/08/11 DEL START
				//对桌面页数做最优处理,取最优值
				if( sBgWorkspaceScreens != null && sBgWorkspaceScreens.size() > orderedScreens.size() )
				{
					workspaceScreens.addAll( sBgWorkspaceScreens );
				}
				else
				{
					for( Integer i : orderedScreens.keySet() )
					{
						long screenId = orderedScreens.get( i );
						workspaceScreens.add( screenId );
					}
				}
				// hp@2015/08/11 DEL END
				synchronized( sBgLock )
				{
					Iterator<ItemInfo> iter = workspaceApps.iterator();
					//<phenix modify> liuhailin@2015-03-12 modify begin
					//当default_workspace.xml什么都不配置的时候，导致布局错误的问题，并且此时可以快速查找，从未排满的页开始查找空位
					int startSearchPageIndex = 0;
					//zhujieping add start //添加配置项“start_search_page_index_when_install_apk_find_availableI_space”，应用安装时，查找空位从该配置项页数后开始查找（普通页第一页为0）
					if( installApp )
					{
						startSearchPageIndex = LauncherDefaultConfig.getInt( R.integer.start_search_page_index_when_install_apk_find_availableI_space );
					}
					//zhujieping add end
					//zhujieping add start  //添加配置项“config_empty_screen_id_in_core”，单层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
					ArrayList<Long> tofindScreens = new ArrayList<Long>( workspaceScreens );
					if( !installApp )
					{
						if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
						{
							if( LauncherDefaultConfig.mConfigEmptyScreenIdArrayInCore != null && LauncherDefaultConfig.mConfigEmptyScreenIdArrayInCore.length > 0 )
							{
								for( long id : LauncherDefaultConfig.mConfigEmptyScreenIdArrayInCore )
								{
									tofindScreens.remove( id );
								}
							}
						}
					}
					//zhujieping add end
					//<phenix modify> liuhailin@2015-03-12 modify end
					ArrayList<ItemInfo> exsitItem = LauncherModel.getItemsInLocalCoordinates( context );//zhujieping,下面的方法是循环一个info，查一次数据库找空位，再写数据库，这里改成只有第一次查找数据库，后面根据这个list来判断空位，数据库也是最后统一写一次
					while( iter.hasNext() )
					{
						ItemInfo a = iter.next();
						//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
						//final String name = a.title.toString();
						if( false == ( a instanceof FolderInfo ) )
						{
							if( a.getTitle() == null )
							{
								a.setTitle( "name = null" );
							}
							final String name = a.getTitle().toString();
							//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
							final Intent launchIntent = a.getIntent();
							// Short-circuit this logic if the icon exists somewhere on the workspace
							if( LauncherModel.shortcutExists( context , name , launchIntent ) )
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								{
									Log.d( TAG , "bind added apps failed for it is exist ! ItemInfo=" + a );
									Log.d( "cyk_bug : c_0003400" , "cyk launcherModel addAndBindAddedItems: bind added apps failed for it is exist ! ItemInfo= " + a );//cheyingkun add	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
								}
								continue;
							}
						}
						// Add this icon to the db, creating a new page if necessary.  If there
						// is only the empty page then we just add items to the first page.
						// Otherwise, we add them to the next pages.
						//<phenix modify> liuhailin@2015-03-12 del begin
						//当default_workspace.xml什么都不配置的时候，导致布局错误的问题，并且此时可以快速查找，从未排满的页开始查找空位
						//int startSearchPageIndex = workspaceScreens.isEmpty() ? 0 : 1;
						//<phenix modify> liuhailin@2015-03-12 del end
						Pair<Long , int[]> coords = LauncherModel.findNextAvailableIconSpace( context , startSearchPageIndex , tofindScreens , exsitItem );
						//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcherModel addAndBindAddedItems: coords 1 at = " , coords ) );
						}
						//cheyingkun add end
						if( coords == null )
						{
							//<phenix modify> liuhailin@2015-03-12 modify begin
							//当default_workspace.xml什么都不配置的时候，导致布局错误的问题，并且此时可以快速查找，从未排满的页开始查找空位
							if( workspaceScreens.size() > 0 )
							{
								startSearchPageIndex++;
							}
							//<phenix modify> liuhailin@2015-03-12 modify end
							LauncherProvider lp = LauncherAppState.getLauncherProvider();
							// If we can't find a valid position, then just add a new screen.
							// This takes time so we need to re-queue the add until the new
							// page is added.  Create as many screens as necessary to satisfy
							// the startSearchPageIndex.
							int numPagesToAdd = Math.max( 1 , startSearchPageIndex + 1 - workspaceScreens.size() );
							//cheyingkun add start	//为bug:c_0003819添加log，（打开配置项switch_enable_debug启动）。【c_0003819】
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							{
								Log.e( "cyk_bug : c_0003819" , StringUtils.concat( "cyk launcherModel addAndBindAddedItems: numPagesToAdd: " , numPagesToAdd ) );
							}
							//cheyingkun add end
							while( numPagesToAdd > 0 )
							{
								//0010478: 【桌面】桌面图标已经满页无空间时进行智能分类，分类成功后安装应用至手机，成功安装后恢复布局，桌面报停止运行后桌面重启 , change by shlt@2015/03/12 UPD START
								if( workspaceScreens.size() > 0 )
								{
									// zhujieping@2015/04/02 UPDATE START
									//数组workspaceScreenst的最后一个不一定就是最大的，例如交换过位置，需要进行遍历查找最大值【i_0010833】
									//									lp.updateMaxScreenId( workspaceScreens.get( workspaceScreens.size() - 1 ) );
									lp.updateMaxScreenId( getWorkspaceScreensMaxID( workspaceScreens ) );
									// zhujieping@2015/04/02 UPDATE END
								}
								//0010478: 【桌面】桌面图标已经满页无空间时进行智能分类，分类成功后安装应用至手机，成功安装后恢复布局，桌面报停止运行后桌面重启 , change by shlt@2015/03/12 UPD END
								long screenId = lp.generateNewScreenId();
								// Save the screen id for binding in the workspace
								workspaceScreens.add( screenId );
								addedWorkspaceScreensFinal.add( screenId );
								numPagesToAdd--;
								// hp@2015/08/11 DEL START
								//当workspaceScreens与数据库中item信息的screenId不匹配的时候，导致异常重启。 
								// Find the coordinate again
								tofindScreens.add( screenId );//zhujieping add  //添加配置项“config_empty_screen_id_in_core”，单层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
								coords = LauncherModel.findNextAvailableIconSpace( context , startSearchPageIndex , tofindScreens , exsitItem );
								if( coords != null )
								{
									//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
									if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									{
										Log.d(
												"cyk_bug : c_0003400" ,
												"cyk launcherModel addAndBindAddedItems: 2 [coords != null] coords at = " + coords.first + "," + coords.second[0] + "," + coords.second[1] );
									}
									//cheyingkun add end
									break;
								}
								else
								{
									numPagesToAdd++;
									//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
									if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									{
										Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcherModel addAndBindAddedItems: 2 numPagesToAdd++ = " , numPagesToAdd ) );
									}
									//cheyingkun add end
								}
								// hp@2015/08/11 DEL END
							}
						}
						if( coords == null )
						{
							//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							{
								Log.d( "cyk_bug : c_0003400" , "cyk launcherModel addAndBindAddedItems: Coordinates should not be null " );
							}
							//cheyingkun add end
							throw new RuntimeException( "Coordinates should not be null" );
						}
						//cheyingkun add start	//为bug:c_0003819添加log，（打开配置项switch_enable_debug启动）。【c_0003819】
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Log.d( "cyk_bug : c_0003819" , StringUtils.concat( "cyk launcherModel addAndBindAddedItems: coords at = " , coords.first , "," , coords.second[0] , "," , coords.second[1] ) );
						}
						//cheyingkun add end
						ItemInfo mItemInfo;
						if( a instanceof ShortcutInfo )
						{
							mItemInfo = (ShortcutInfo)a;
							//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							{
								Log.d( "cyk_bug : c_0003400" , " cyk launcherModel addAndBindAddedItems mItemInfo  ShortcutInfo : " + (ShortcutInfo)a );
							}
							//cheyingkun add end
						}
						else if( a instanceof AppInfo )
						{
							mItemInfo = ( (AppInfo)a ).makeShortcut();
							//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							{
								Log.d( "cyk_bug : c_0003400" , " cyk launcherModel addAndBindAddedItems mItemInfo  AppInfo : " + ( (AppInfo)a ).makeShortcut() );
							}
							//cheyingkun add end
						}
						else if( a instanceof FolderInfo )
						{
							mItemInfo = (FolderInfo)a;
							//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							{
								Log.d( "cyk_bug : c_0003400" , " cyk launcherModel addAndBindAddedItems mItemInfo  FolderInfo : " + (FolderInfo)a );
							}
							//cheyingkun add end
						}
						else
						{
							//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							{
								Log.d( "cyk_bug : c_0003400" , " cyk launcherModel addAndBindAddedItems Unexpected info type : " );
							}
							//cheyingkun add end
							throw new RuntimeException( "Unexpected info type" );
						}
						// Add the ItemInfo to the db
						// Save the ShortcutInfo for binding in the workspace
						mItemInfo.setContainer( LauncherSettings.Favorites.CONTAINER_DESKTOP );
						mItemInfo.setScreenId( coords.first );
						mItemInfo.setCellX( coords.second[0] );
						mItemInfo.setCellY( coords.second[1] );
						addedItemsFinal.add( mItemInfo );
						if( mItemInfo.getItemType() != LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET )
						{
							exsitItem.add( mItemInfo );
						}
					}
					addItemsToDatabase( context , addedItemsFinal );
					for( ItemInfo mTempInfo : addedItemsFinal )
					{
						if( mTempInfo instanceof ShortcutInfo )
						{
							ShortcutInfo shortcutInfo = (ShortcutInfo)mTempInfo;
							//xiatian add start	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
							if( workspaceAppsNeed2Combine != null && workspaceAppsNeed2Combine.contains( shortcutInfo ) )
							{//接受广播“ACTION_INSTALL_SHORTCUT”，自动生成快捷方式时，若是EXTRA_SHORTCUT_ICON，则在将原始图片存入数据库后，再对info的icon加背板、蒙版和盖板
								if( !( shortcutInfo.getIntent().getComponent() != null && "com.cooee.wallpaper.host.WallpaperMainActivity".equals( shortcutInfo.getIntent().getComponent()
										.getClassName() ) ) )//一键换壁纸不跟随主题变化
									shortcutInfo.setIcon( Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( shortcutInfo.getIcon() , context , true ) );
							}
							//xiatian add end
						}
						else if( mTempInfo instanceof FolderInfo )
						{
							FolderInfo mFolderInfo = (FolderInfo)mTempInfo;
							ArrayList<ShortcutInfo> contents = mFolderInfo.getContents();
							for( ShortcutInfo mShortcutInfo : contents )
							{
								addOrMoveItemInDatabase( context , mShortcutInfo , mFolderInfo.getId() , 0 , mShortcutInfo.getCellX() , mShortcutInfo.getCellY() );
							}
						}
					}
				}
				//cheyingkun add start	//为bug:c_0003819添加log，（打开配置项switch_enable_debug启动）。【c_0003819】
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( "cyk_bug : c_0003819" , StringUtils.concat( " cyk launcherModel addAndBindAddedItems workspaceScreens: " , workspaceScreens.toArray() ) );
				}
				//cheyingkun add end
				// Update the workspace screens
				updateWorkspaceScreenOrder( context , workspaceScreens );
				//<i_0010422> liuhailin@2015-03-10 modify begin
				//if( !addedShortcutsFinal.isEmpty() || !allAppsApps.isEmpty()  )
				if( !addedItemsFinal.isEmpty() || ( allAppsApps != null && !allAppsApps.isEmpty() ) )
				//<i_0010422> liuhailin@2015-03-10 modify end
				{
					runOnMainThread( new Runnable() {
						
						public void run()
						{
							Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
							if( callbacks == cb && cb != null )
							{
								final ArrayList<ItemInfo> addAnimated = new ArrayList<ItemInfo>();
								final ArrayList<ItemInfo> addNotAnimated = new ArrayList<ItemInfo>();
								if( !addedItemsFinal.isEmpty() )
								{
									ItemInfo info = addedItemsFinal.get( addedItemsFinal.size() - 1 );
									long lastScreenId = info.getScreenId();
									for( ItemInfo i : addedItemsFinal )
									{
										if( i.getScreenId() == lastScreenId )
										{
											addAnimated.add( i );
										}
										else
										{
											addNotAnimated.add( i );
										}
									}
								}
								callbacks.bindItemsAdded( addedWorkspaceScreensFinal , addNotAnimated , addAnimated , allAppsApps , installApp , isLoadFinish );
							}
						}
					} );
				}
			}
		};
		runOnWorkerThread( r );
	}
	
	// zhujieping@2015/04/02 ADD START
	public long getWorkspaceScreensMaxID(
			ArrayList<Long> workscreens )
	{
		if( workscreens == null || workscreens.size() <= 0 )
		{
			return 0;
		}
		long maxId = workscreens.get( 0 );
		for( int i = 1 ; i < workscreens.size() ; i++ )
		{
			if( workscreens.get( i ) > maxId )
			{
				maxId = workscreens.get( i );
			}
		}
		return maxId;
	}
	
	// zhujieping@2015/04/02 ADD END
	public Bitmap getFallbackIcon(
			Context context //xiatian add	//优化桌面启动速度，mDefaultIcon不应该在LauncherModel的初始化中生成，应该在需要使用的时候生成。
	)
	{
		//xiatian add start	//优化桌面启动速度，mDefaultIcon不应该在LauncherModel的初始化中生成，应该在需要使用的时候生成。
		if( mDefaultIcon == null )
		{
			mDefaultIcon = Utilities.createIconBitmap(
					mIconCache.getFullResDefaultActivityIcon() ,
					context ,
					Utilities.sIconWidth ,
					Utilities.sIconHeight ,
					Utilities.sIconTextureWidth ,
					Utilities.sIconTextureHeight ,
					true );
		}
		//xiatian add end
		return Bitmap.createBitmap( mDefaultIcon );
	}
	
	public void unbindItemInfosAndClearQueuedBindRunnables()
	{
		if( sWorkerThread.getThreadId() == Process.myTid() )
		{
			throw new RuntimeException( "Expected unbindLauncherItemInfos() to be called from the main thread" );
		}
		// Clear any deferred bind runnables
		mDeferredBindRunnables.clear();
		// Remove any queued bind runnables
		mHandler.cancelAllRunnablesOfType( MAIN_THREAD_BINDING_RUNNABLE );
		// Unbind all the workspace items
		unbindWorkspaceItemsOnMainThread();
	}
	
	/** Unbinds all the sBgWorkspaceItems and sBgAppWidgets on the main thread */
	void unbindWorkspaceItemsOnMainThread()
	{
		// Ensure that we don't use the same workspace items data structure on the main thread
		// by making a copy of workspace items first.
		final ArrayList<ItemInfo> tmpWorkspaceItems = new ArrayList<ItemInfo>();
		final ArrayList<ItemInfo> tmpAppWidgets = new ArrayList<ItemInfo>();
		synchronized( sBgLock )
		{
			tmpWorkspaceItems.addAll( sBgWorkspaceItems );
			tmpAppWidgets.addAll( sBgAppWidgets );
		}
		Runnable r = new Runnable() {
			
			@Override
			public void run()
			{
				for( ItemInfo item : tmpWorkspaceItems )
				{
					item.unbind();
				}
				for( ItemInfo item : tmpAppWidgets )
				{
					item.unbind();
				}
			}
		};
		runOnMainThread( r );
	}
	
	/**
	 * Adds an item to the DB if it was not created previously, or move it to a new
	 * <container, screen, cellX, cellY>
	 */
	public static void addOrMoveItemInDatabase(
			Context context ,
			ItemInfo item ,
			long container ,
			long screenId ,
			int cellX ,
			int cellY )
	{
		if( item.getContainer() == ItemInfo.NO_ID )
		{
			// From all apps
			addItemToDatabase( context , item , container , screenId , cellX , cellY , false );
		}
		else
		{
			// From somewhere else
			moveItemInDatabase( context , item , container , screenId , cellX , cellY );
		}
	}
	
	static void checkItemInfoLocked(
			final long itemId ,
			final ItemInfo item ,
			StackTraceElement[] stackTrace )
	{
		ItemInfo modelItem = sBgItemsIdMap.get( itemId );
		if( modelItem != null && item != modelItem )
		{
			// check all the data is consistent
			if( modelItem instanceof ShortcutInfo && item instanceof ShortcutInfo )
			{
				ShortcutInfo modelShortcut = (ShortcutInfo)modelItem;
				ShortcutInfo shortcut = (ShortcutInfo)item;
				if( modelShortcut.getTitle().toString().equals( shortcut.getTitle().toString() ) && modelShortcut.getIntent().filterEquals( shortcut.getIntent() ) && modelShortcut.getId() == shortcut
						.getId() && modelShortcut.getItemType() == shortcut.getItemType() && modelShortcut.getContainer() == shortcut.getContainer() && modelShortcut.getScreenId() == shortcut
						.getScreenId() && modelShortcut.getCellX() == shortcut.getCellX() && modelShortcut.getCellY() == shortcut.getCellY() && modelShortcut.getSpanX() == shortcut.getSpanX() && modelShortcut
						.getSpanY() == shortcut.getSpanY() && ( ( modelShortcut.getDropPos() == null && shortcut.getDropPos() == null ) || ( modelShortcut.getDropPos() != null && shortcut
						.getDropPos() != null && modelShortcut.getDropPos()[0] == shortcut.getDropPos()[0] && modelShortcut.getDropPos()[1] == shortcut.getDropPos()[1] ) ) )
				{
					// For all intents and purposes, this is the same object
					return;
				}
			}
			// the modelItem needs to match up perfectly with item if our model is
			// to be consistent with the database-- for now, just require
			// modelItem == item or the equality check above
			String msg = StringUtils.concat(
					"item: " + ( ( item != null ) ? item : "null" ) ,
					"modelItem: " + ( ( modelItem != null ) ? modelItem : "null" ) ,
					"Error: ItemInfo passed to checkItemInfo doesn't match original" );
			RuntimeException e = new RuntimeException( msg );
			if( stackTrace != null )
			{
				e.setStackTrace( stackTrace );
			}
			// TODO: something breaks this in the upgrade path
			//throw e;
		}
	}
	
	public static void checkItemInfo(
			final ItemInfo item )
	{
		final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		final long itemId = item.getId();
		Runnable r = new Runnable() {
			
			public void run()
			{
				synchronized( sBgLock )
				{
					checkItemInfoLocked( itemId , item , stackTrace );
				}
			}
		};
		runOnWorkerThread( r );
	}
	
	static void updateItemInDatabaseHelper(
			Context context ,
			final ContentValues values ,
			final ItemInfo item ,
			final String callingFunction )
	{
		final long itemId = item.getId();
		final Uri uri = LauncherSettings.Favorites.getContentUri( itemId , false );
		final ContentResolver cr = context.getContentResolver();
		final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		Runnable r = new Runnable() {
			
			public void run()
			{
				cr.update( uri , values , null , null );
				updateItemArrays( item , itemId , stackTrace );
			}
		};
		runOnWorkerThread( r );
	}
	
	static void updateItemsInDatabaseHelper(
			Context context ,
			final ArrayList<ContentValues> valuesList ,
			final ArrayList<ItemInfo> items ,
			final String callingFunction )
	{
		final ContentResolver cr = context.getContentResolver();
		final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		Runnable r = new Runnable() {
			
			public void run()
			{
				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
				int count = items.size();
				for( int i = 0 ; i < count ; i++ )
				{
					ItemInfo item = items.get( i );
					final long itemId = item.getId();
					final Uri uri = LauncherSettings.Favorites.getContentUri( itemId , false );
					ContentValues values = valuesList.get( i );
					ops.add( ContentProviderOperation.newUpdate( uri ).withValues( values ).build() );
					updateItemArrays( item , itemId , stackTrace );
				}
				try
				{
					cr.applyBatch( LauncherProvider.AUTHORITY , ops );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		};
		runOnWorkerThread( r );
	}
	
	static void updateItemArrays(
			ItemInfo item ,
			long itemId ,
			StackTraceElement[] stackTrace )
	{
		// Lock on mBgLock *after* the db operation
		synchronized( sBgLock )
		{
			checkItemInfoLocked( itemId , item , stackTrace );
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				if( item.getContainer() != LauncherSettings.Favorites.CONTAINER_DESKTOP && item.getContainer() != LauncherSettings.Favorites.CONTAINER_HOTSEAT )
				{
					// Item is in a folder, make sure this folder exists
					if( !sBgFolders.containsKey( item.getContainer() ) )
					{
						// An items container is being set to a that of an item which is not in
						// the list of Folders.
						String msg = StringUtils.concat( "item:" + item , " container being set to: " , item.getContainer() , ", not in the list of folders" );
						Log.e( TAG , msg );
					}
				}
			}
			// Items are added/removed from the corresponding FolderInfo elsewhere, such
			// as in Workspace.onDrop. Here, we just add/remove them from the list of items
			// that are on the desktop, as appropriate
			ItemInfo modelItem = sBgItemsIdMap.get( itemId );
			//添加智能分类功能 , change by shlt@2015/02/26 ADD START
			if( modelItem == null )
				return;
			//添加智能分类功能 , change by shlt@2015/02/26 ADD END
			if( modelItem.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP || modelItem.getContainer() == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
			{
				switch( modelItem.getItemType() )
				{
					case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
					case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
					case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
						if( !sBgWorkspaceItems.contains( modelItem ) )
						{
							sBgWorkspaceItems.add( modelItem );
						}
						break;
					default:
						break;
				}
			}
			else
			{
				sBgWorkspaceItems.remove( modelItem );
			}
		}
	}
	
	//会导致死锁
	public void flushWorkerThread()
	{
		mFlushingWorkerThread = true;
		Runnable waiter = new Runnable() {
			
			public void run()
			{
				synchronized( this )
				{
					notifyAll();
					mFlushingWorkerThread = false;
				}
			}
		};
		synchronized( waiter )
		{
			runOnWorkerThread( waiter );
			if( mLoaderTask != null )
			{
				synchronized( mLoaderTask )
				{
					mLoaderTask.notify();
				}
			}
			boolean success = false;
			while( !success )
			{
				try
				{
					waiter.wait();
					success = true;
				}
				catch( InterruptedException e )
				{
				}
			}
		}
	}
	
	/**
	 * Move an item in the DB to a new <container, screen, cellX, cellY>
	 */
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//static void moveItemInDatabase(
	public static void moveItemInDatabase(
			//添加智能分类功能 , change by shlt@2015/02/09 UPD END
			Context context ,
			final ItemInfo item ,
			final long container ,
			final long screenId ,
			final int cellX ,
			final int cellY )
	{
		item.setContainer( container );
		item.setCellX( cellX );
		item.setCellY( cellY );
		// We store hotseat items in canonical form which is this orientation invariant position
		// in the hotseat
		if( context instanceof Launcher && screenId < 0 && container == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
		{
			item.setScreenId( ( (Launcher)context ).getHotseat().getOrderInHotseat( cellX , cellY ) );
		}
		else
		{
			item.setScreenId( screenId );
		}
		final ContentValues values = new ContentValues();
		values.put( LauncherSettings.Favorites.CONTAINER , item.getContainer() );
		//<数据库字段更新> liuhailin@2015-03-23 modify begin
		values.put( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE , item.getDefaultWorkspaceItemType() );
		//<数据库字段更新> liuhailin@2015-03-23 modify end
		values.put( LauncherSettings.Favorites.CELLX , item.getCellX() );
		values.put( LauncherSettings.Favorites.CELLY , item.getCellY() );
		values.put( LauncherSettings.Favorites.SCREEN , item.getScreenId() );
		updateItemInDatabaseHelper( context , values , item , "moveItemInDatabase" );
	}
	
	/**
	 * Move items in the DB to a new <container, screen, cellX, cellY>. We assume that the
	 * cellX, cellY have already been updated on the ItemInfos.
	 */
	public static void moveItemsInDatabase(
			Context context ,
			final ArrayList<ItemInfo> items ,
			final long container ,
			final int screen )
	{
		ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
		int count = items.size();
		for( int i = 0 ; i < count ; i++ )
		{
			ItemInfo item = items.get( i );
			item.setContainer( container );
			// We store hotseat items in canonical form which is this orientation invariant position
			// in the hotseat
			if( context instanceof Launcher && screen < 0 && container == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
			{
				item.setScreenId( ( (Launcher)context ).getHotseat().getOrderInHotseat( item.getCellX() , item.getCellY() ) );
			}
			else
			{
				item.setScreenId( screen );
			}
			final ContentValues values = new ContentValues();
			values.put( LauncherSettings.Favorites.CONTAINER , item.getContainer() );
			//<数据库字段更新> liuhailin@2015-03-23 modify begin
			values.put( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE , item.getDefaultWorkspaceItemType() );
			//<数据库字段更新> liuhailin@2015-03-23 modify end
			values.put( LauncherSettings.Favorites.CELLX , item.getCellX() );
			values.put( LauncherSettings.Favorites.CELLY , item.getCellY() );
			values.put( LauncherSettings.Favorites.SCREEN , item.getScreenId() );
			contentValues.add( values );
		}
		updateItemsInDatabaseHelper( context , contentValues , items , "moveItemInDatabase" );
	}
	
	/**
	 * Move and/or resize item in the DB to a new <container, screen, cellX, cellY, spanX, spanY>
	 */
	static void modifyItemInDatabase(
			Context context ,
			final ItemInfo item ,
			final long container ,
			final long screenId ,
			final int cellX ,
			final int cellY ,
			final int spanX ,
			final int spanY )
	{
		item.setContainer( container );
		item.setCellX( cellX );
		item.setCellY( cellY );
		item.setSpanX( spanX );
		item.setSpanY( spanY );
		// We store hotseat items in canonical form which is this orientation invariant position
		// in the hotseat
		if( context instanceof Launcher && screenId < 0 && container == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
		{
			item.setScreenId( ( (Launcher)context ).getHotseat().getOrderInHotseat( cellX , cellY ) );
		}
		else
		{
			item.setScreenId( screenId );
		}
		final ContentValues values = new ContentValues();
		values.put( LauncherSettings.Favorites.CONTAINER , item.getContainer() );
		//<数据库字段更新> liuhailin@2015-03-23 modify begin
		values.put( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE , item.getDefaultWorkspaceItemType() );
		//<数据库字段更新> liuhailin@2015-03-23 modify end
		values.put( LauncherSettings.Favorites.CELLX , item.getCellX() );
		values.put( LauncherSettings.Favorites.CELLY , item.getCellY() );
		values.put( LauncherSettings.Favorites.SPANX , item.getSpanX() );
		values.put( LauncherSettings.Favorites.SPANY , item.getSpanY() );
		values.put( LauncherSettings.Favorites.SCREEN , item.getScreenId() );
		updateItemInDatabaseHelper( context , values , item , "modifyItemInDatabase" );
	}
	
	/**
	 * Update an item to the database in a specified container.
	 */
	public static void updateItemInDatabase(
			Context context ,
			final ItemInfo item )
	{
		updateItemInDatabase( context , item , false );
	}
	
	//zhujieping add,这个方法表示是否只更新intent
	public static void updateItemInDatabase(
			Context context ,
			ItemInfo item ,
			boolean isOnlyUpdateIntent )
	{
		ContentValues values = new ContentValues();
		if( item instanceof ShortcutInfo && isOnlyUpdateIntent )
		{
			String uri = ( item.getIntent() != null ? item.getIntent().toUri( 0 ) : null );
			values.put( LauncherSettings.Favorites.INTENT , uri );
		}
		else
		{
			item.onAddToDatabase( values );
		}
		item.updateValuesWithCoordinates( values , item.getCellX() , item.getCellY() );
		updateItemInDatabaseHelper( context , values , item , "updateItemInDatabase" );
	}
	
	/**
	 * Returns true if the shortcuts already exists in the database.
	 * we identify a shortcut by its title and intent.
	 * 该方法通过title和intent到数据库中判断目标快捷方式是否存在数据库中
	 */
	static boolean shortcutExists(
			Context context ,
			String title ,
			Intent intent )
	{
		final ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query( LauncherSettings.Favorites.CONTENT_URI , new String[]{ LauncherSettings.Favorites.TITLE , LauncherSettings.Favorites.INTENT } , "title=? and intent=?" ,
		//				"title = 0 AND (m_type = 132 OR m_type = 130)" ,
				new String[]{ title , intent.toUri( 0 ) } ,
				//				null ,
				null );
		boolean result = false;
		try
		{
			result = c.moveToFirst();
		}
		finally
		{
			c.close();
		}
		return result;
	}
	
	//xiatian add start	//fix bug：解决“单层模式下，多米音乐会自动生成一个和原图标入口相同的快捷方式”的问题。【i_0010976】
	/**
	 * 方法“static boolean shortcutExists(Context context ,String title ,Intent intent )”的强化版。
	 * 先从数据库中取出所有title相同的item(application和shortcut)，再判断intent中的ComponentName是否相同（不考虑Action、Category、Flags、Extras和Data）。
	 */
	static boolean shortcutExistsIntensify(
			Context context ,
			String title ,
			Intent intent )
	{
		ComponentName mComponentNameSource = intent.getComponent();
		// yangxiaoming start 2015-05-18 由于i_0011035难复现，特在此打上Log
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "i_0011035" , StringUtils.concat( "LauncherModel shortcutExistsIntensify mComponentNameSource=====" , mComponentNameSource.toString() ) );
		}
		// yangxiaoming end
		if( mComponentNameSource == null )
		{
			return false;
		}
		final ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(
				LauncherSettings.Favorites.CONTENT_URI ,
				new String[]{ LauncherSettings.Favorites.TITLE , LauncherSettings.Favorites.ITEM_TYPE , LauncherSettings.Favorites.INTENT } ,
				"title = ? and (itemType = 0 OR itemType = 1)" ,
				new String[]{ title } ,
				null );
		try
		{
			final int intentIndex = c.getColumnIndex( Favorites.INTENT );
			while( c.moveToNext() )
			{
				final String intentUriTemp = c.getString( intentIndex );
				if( intentUriTemp != null )
				{
					try
					{
						final Intent mIntentTemp = Intent.parseUri( intentUriTemp , 0 );
						ComponentName mComponentNameSourceTemp = mIntentTemp.getComponent();
						// yangxiaoming start 2015-05-18 由于i_0011035难复现，特在此打上Log
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Log.d( "i_0011035" , StringUtils.concat( "LauncherModel shortcutExistsIntensify mComponentNameSourceTemp=====" , mComponentNameSourceTemp.toString() ) );
						}
						// yangxiaoming end
						if( mComponentNameSource.equals( mComponentNameSourceTemp ) )
						{
							if( c != null )
							{
								c.close();
							}
							return true;
						}
					}
					catch( Exception e )
					{
					}
				}
			}
		}
		catch( SQLException ex )
		{
		}
		finally
		{
			if( c != null )
			{
				c.close();
			}
		}
		if( c != null )
		{
			c.close();
		}
		return false;
	}
	//xiatian add end
	;
	
	/**
	 * Returns an ItemInfo array containing all the items in the LauncherModel.
	 * The ItemInfo.id is not set through this function.
	 */
	static ArrayList<ItemInfo> getItemsInLocalCoordinates(
			Context context )
	{
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		final ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query( LauncherSettings.Favorites.CONTENT_URI , new String[]{ LauncherSettings.Favorites.ITEM_TYPE , LauncherSettings.Favorites.CONTAINER ,
				//<数据库字段更新> liuhailin@2015-03-23 modify begin
				LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE ,
				//<数据库字段更新> liuhailin@2015-03-23 modify end
				LauncherSettings.Favorites.SCREEN ,
				LauncherSettings.Favorites.CELLX ,
				LauncherSettings.Favorites.CELLY ,
				LauncherSettings.Favorites.SPANX ,
				LauncherSettings.Favorites.SPANY } , null , null , null );
		final int itemTypeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ITEM_TYPE );
		final int containerIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CONTAINER );
		//<数据库字段更新> liuhailin@2015-03-23 modify begin
		final int defWorkspaceItemTypeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE );
		//<数据库字段更新> liuhailin@2015-03-23 modify end
		final int screenIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.SCREEN );
		final int cellXIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CELLX );
		final int cellYIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CELLY );
		final int spanXIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.SPANX );
		final int spanYIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.SPANY );
		try
		{
			while( c.moveToNext() )
			{
				ItemInfo item = new ItemInfo();
				item.setCellX( c.getInt( cellXIndex ) );
				item.setCellY( c.getInt( cellYIndex ) );
				item.setSpanX( Math.max( 1 , c.getInt( spanXIndex ) ) );
				item.setSpanY( Math.max( 1 , c.getInt( spanYIndex ) ) );
				item.setContainer( c.getInt( containerIndex ) );
				//<数据库字段更新> liuhailin@2015-03-23 modify begin
				item.setDefaultWorkspaceItemType( c.getInt( defWorkspaceItemTypeIndex ) );
				//<数据库字段更新> liuhailin@2015-03-23 modify end
				item.setItemType( c.getInt( itemTypeIndex ) );
				item.setScreenId( c.getInt( screenIndex ) );
				items.add( item );
			}
		}
		catch( Exception e )
		{
			items.clear();
		}
		finally
		{
			c.close();
		}
		return items;
	}
	
	/**
	 * Find a folder in the db, creating the FolderInfo if necessary, and adding it to folderList.
	 */
	FolderInfo getFolderById(
			Context context ,
			HashMap<Long , FolderInfo> folderList ,
			long id )
	{
		final ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(
				LauncherSettings.Favorites.CONTENT_URI ,
				null ,
				"_id=? and (itemType=? or itemType=?)" ,
				new String[]{ String.valueOf( id ) , String.valueOf( LauncherSettings.Favorites.ITEM_TYPE_FOLDER ) } ,
				null );
		try
		{
			if( c.moveToFirst() )
			{
				final int itemTypeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ITEM_TYPE );
				final int titleIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.TITLE );
				final int containerIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CONTAINER );
				//<数据库字段更新> liuhailin@2015-03-23 modify begin
				final int defWorkspaceItemTypeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE );
				//<数据库字段更新> liuhailin@2015-03-23 modify end
				final int screenIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.SCREEN );
				final int cellXIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CELLX );
				final int cellYIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CELLY );
				FolderInfo folderInfo = null;
				switch( c.getInt( itemTypeIndex ) )
				{
					case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
						folderInfo = findOrMakeFolder( folderList , id );
						break;
				}
				folderInfo.setTitle( c.getString( titleIndex ) );
				folderInfo.setId( id );
				folderInfo.setContainer( c.getInt( containerIndex ) );
				//<数据库字段更新> liuhailin@2015-03-23 modify begin
				folderInfo.setDefaultWorkspaceItemType( c.getInt( defWorkspaceItemTypeIndex ) );
				//<数据库字段更新> liuhailin@2015-03-23 modify end
				folderInfo.setScreenId( c.getInt( screenIndex ) );
				folderInfo.setCellX( c.getInt( cellXIndex ) );
				folderInfo.setCellY( c.getInt( cellYIndex ) );
				return folderInfo;
			}
		}
		finally
		{
			c.close();
		}
		return null;
	}
	
	/**
	 * Add an item to the database in a specified container. Sets the container, screen, cellX and
	 * cellY fields of the item. Also assigns an ID to the item.
	 */
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//static void addItemToDatabase(
	public static void addItemToDatabase(
			//添加智能分类功能 , change by shlt@2015/02/09 UPD END
			Context context ,
			final ItemInfo item ,
			final long container ,
			final long screenId ,
			final int cellX ,
			final int cellY ,
			final boolean notify )
	{
		item.setContainer( container );
		item.setCellX( cellX );
		item.setCellY( cellY );
		// We store hotseat items in canonical form which is this orientation invariant position
		// in the hotseat
		if( context instanceof Launcher && screenId < 0 && container == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
		{
			item.setScreenId( ( (Launcher)context ).getHotseat().getOrderInHotseat( cellX , cellY ) );
		}
		else
		{
			item.setScreenId( screenId );
		}
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		item.onAddToDatabase( values );
		item.setId( LauncherAppState.getLauncherProvider().generateNewItemId() );
		values.put( LauncherSettings.Favorites._ID , item.getId() );
		item.updateValuesWithCoordinates( values , item.getCellX() , item.getCellY() );
		Runnable r = new Runnable() {
			
			public void run()
			{
				cr.insert( notify ? LauncherSettings.Favorites.CONTENT_URI : LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION , values );
				// Lock on mBgLock *after* the db operation
				synchronized( sBgLock )
				{
					checkItemInfoLocked( item.getId() , item , null );
					sBgItemsIdMap.put( item.getId() , item );
					switch( item.getItemType() )
					{
						case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
							sBgFolders.put( item.getId() , (FolderInfo)item );//生成文件夹
							// Fall through
						case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
						case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
							if( item.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP || item.getContainer() == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
							{
								sBgWorkspaceItems.add( item );
							}
							else
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								{
									if( !sBgFolders.containsKey( item.getContainer() ) )
									{
										// Adding an item to a folder that doesn't exist.
										String msg = "adding item to a folder but folder doesn't exist!!item is=" + item;
										Log.e( TAG , msg );
									}
								}
							}
							break;
						case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
							sBgAppWidgets.add( (LauncherAppWidgetInfo)item );
							break;
					}
				}
			}
		};
		runOnWorkerThread( r );
	}
	
	/**
	 * Creates a new unique child id, for a given cell span across all layouts.
	 */
	static int getCellLayoutChildId(
			long container ,
			long screen ,
			int localCellX ,
			int localCellY ,
			int spanX ,
			int spanY )
	{
		return ( ( (int)container & 0xFF ) << 24 ) | ( (int)screen & 0xFF ) << 16 | ( localCellX & 0xFF ) << 8 | ( localCellY & 0xFF );
	}
	
	/**
	 * Removes the specified item from the database
	 * @param context
	 * @param item
	 */
	public static void deleteItemFromDatabase(
			Context context ,
			final ItemInfo item )
	{
		final ContentResolver cr = context.getContentResolver();
		final Uri uriToDelete = LauncherSettings.Favorites.getContentUri( item.getId() , false );
		Runnable r = new Runnable() {
			
			public void run()
			{
				cr.delete( uriToDelete , null , null );
				// Lock on mBgLock *after* the db operation
				synchronized( sBgLock )
				{
					switch( item.getItemType() )
					{
						case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
							sBgFolders.remove( item.getId() );//文件夹删除
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							{
								for( ItemInfo info : sBgItemsIdMap.values() )
								{
									if( info.getContainer() == item.getId() )
									{
										// We are deleting a folder which still contains items that
										// think they are contained by that folder.
										String msg = StringUtils.concat( "deleting a folder which still contains items." , "--folder is:" + item , "--items is:" + info );
										Log.e( TAG , msg );
									}
								}
							}
							sBgWorkspaceItems.remove( item );
							break;
						case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
						case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
							sBgWorkspaceItems.remove( item );
							break;
						case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
							sBgAppWidgets.remove( (LauncherAppWidgetInfo)item );
							break;
					}
					sBgItemsIdMap.remove( item.getId() );
					sBgDbIconCache.remove( item );
				}
			}
		};
		runOnWorkerThread( r );
	}
	
	/**
	 * Update the order of the workspace screens in the database. The array list contains
	 * a list of screen ids in the order that they should appear.
	 */
	//添加智能分类功能 , change by shlt@2015/02/09 UPD START
	//void updateWorkspaceScreenOrder(
	public void updateWorkspaceScreenOrder(
			//添加智能分类功能 , change by shlt@2015/02/09 UPD END
			Context context ,
			final ArrayList<Long> screens )
	{
		final ArrayList<Long> screensCopy = new ArrayList<Long>( screens );
		final ContentResolver cr = context.getContentResolver();
		final Uri uri = LauncherSettings.WorkspaceScreens.CONTENT_URI;
		// Remove any negative screen ids -- these aren't persisted
		Iterator<Long> iter = screensCopy.iterator();
		while( iter.hasNext() )
		{
			long id = iter.next();
			if( id < 0 )
			{
				iter.remove();
			}
		}
		Runnable r = new Runnable() {
			
			@Override
			public void run()
			{
				// Clear the table
				int delete = cr.delete( uri , null , null );
				//cheyingkun add start	//为bug:c_0003819添加log，（打开配置项switch_enable_debug启动）。【c_0003819】
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d(
							"cyk_bug : c_0003819" ,
							StringUtils.concat( " cyk launcherModel updateWorkspaceScreenOrder uri:" , uri.toString() , "-delete:" , delete , "-newScreens : " , screens.toArray() ) );
				}
				//cheyingkun add end
				int count = screensCopy.size();
				ContentValues[] values = new ContentValues[count];
				for( int i = 0 ; i < count ; i++ )
				{
					ContentValues v = new ContentValues();
					long screenId = screensCopy.get( i );
					v.put( LauncherSettings.WorkspaceScreens._ID , screenId );
					v.put( LauncherSettings.WorkspaceScreens.SCREEN_RANK , i );
					values[i] = v;
				}
				cr.bulkInsert( uri , values );
				synchronized( sBgLock )
				{
					sBgWorkspaceScreens.clear();
					sBgWorkspaceScreens.addAll( screensCopy );
				}
			}
		};
		runOnWorkerThread( r );
	}
	
	/**
	 * Remove the contents of the specified folder from the database
	 */
	static void deleteFolderContentsFromDatabase(
			Context context ,
			final FolderInfo info )
	{
		final ContentResolver cr = context.getContentResolver();
		Runnable r = new Runnable() {
			
			public void run()
			{
				cr.delete( LauncherSettings.Favorites.getContentUri( info.getId() , false ) , null , null );
				// Lock on mBgLock *after* the db operation
				synchronized( sBgLock )
				{
					sBgItemsIdMap.remove( info.getId() );
					sBgFolders.remove( info.getId() );
					sBgDbIconCache.remove( info );
					sBgWorkspaceItems.remove( info );
				}
				cr.delete( LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION , StringUtils.concat( LauncherSettings.Favorites.CONTAINER , "=" , info.getId() ) , null );
				// Lock on mBgLock *after* the db operation
				synchronized( sBgLock )
				{
					for( ItemInfo childInfo : info.getContents() )
					{
						sBgItemsIdMap.remove( childInfo.getId() );
						sBgDbIconCache.remove( childInfo );
					}
				}
			}
		};
		runOnWorkerThread( r );
	}
	
	/**
	 * Set this as the current Launcher activity object for the loader.
	 */
	public void initialize(
			Callbacks callbacks )
	{
		synchronized( mLock )
		{
			mCallbacks = new WeakReference<Callbacks>( callbacks );
		}
	}
	
	/**
	 * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED and
	 * ACTION_PACKAGE_CHANGED.
	 */
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		if( DEBUG_LOADERS )
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "onReceive intent=" , intent.toUri( 0 ) ) );
		final String action = intent.getAction();
		if( Intent.ACTION_PACKAGE_CHANGED.equals( action ) || Intent.ACTION_PACKAGE_REMOVED.equals( action ) || Intent.ACTION_PACKAGE_ADDED.equals( action ) )
		{
			final String packageName = intent.getData().getSchemeSpecificPart();
			final boolean replacing = intent.getBooleanExtra( Intent.EXTRA_REPLACING , false );
			int op = PackageUpdatedTask.OP_NONE;
			if( packageName == null || packageName.length() == 0 )
			{
				// they sent us a bad intent
				return;
			}
			if( LauncherDefaultConfig.NO_SIMCARD_UNDISPLAY_SIMCARD_APPLICATION )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "lvjiangbin" , StringUtils.concat( "action = " , action , "-packageName = " , packageName ) );
				if( isHideSimCard( packageName ) )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "lvjiangbin" , StringUtils.concat( "hide packageName = " , packageName ) );
					return;
				}
			}
			//智能分类添加运营 , change by shlt@2014/12/25 ADD START
			//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
			//			if( Intent.ACTION_PACKAGE_ADDED.equals( action ) && OperateHelp.getInstance( Launcher.instance ).checkOperateDownLoad( packageName ) )//cheyingkun del
			// zhujieping@2015/07/10 ADD START,智能分类中下载也是通过下载管理器，这个是安装后更新状态栏
			if( LauncherAppState.getActivityInstance() != null && DlManager.getInstance() != null )
			{
				DlManager.getInstance().dealReceiverAction( action , packageName );
			}
			// zhujieping@2015/07/10 ADD END
			if( LauncherAppState.getActivityInstance() != null && Intent.ACTION_PACKAGE_ADDED.equals( action ) && OperateHelp.getInstance( context ).checkOperateDownLoad( packageName ) )//cheyingkun add
			//cheyingkun end
			{
				return;
			}
			//ME_RTFSC START
			if( mApp.getContext().getPackageName().equals( packageName ) && true == intent.getBooleanExtra( Intent.EXTRA_DONT_KILL_APP , false ) )
			{
				//FeatureConfig.DONT_KILL_APP = true;
				MeLauncherInterface.getInstance().canelDialog();
				MeLauncherInterface.getInstance().ShowNotificationifExist( mApp.getContext() );
			}
			//ME_RTFSC END
			//WangLei add start //bug:0010453 //安装、更新或卸载应用时，回调此方法
			if( mCallbacks != null )
			{
				Callbacks cb = mCallbacks.get();
				if( cb != null )
				{
					cb.onAppsChanged( packageName , action );
				}
			}
			//WangLei add end
			if( Intent.ACTION_PACKAGE_CHANGED.equals( action ) )
			{
				op = PackageUpdatedTask.OP_UPDATE;
			}
			else if( Intent.ACTION_PACKAGE_REMOVED.equals( action ) )
			{
				if( !replacing )
				{
					op = PackageUpdatedTask.OP_REMOVE;
				}
				// else, we are replacing the package, so a PACKAGE_ADDED will be sent
				// later, we will update the package at this time
			}
			else if( Intent.ACTION_PACKAGE_ADDED.equals( action ) )
			{
				if( !replacing )
				{
					op = PackageUpdatedTask.OP_ADD;
				}
				else
				{
					op = PackageUpdatedTask.OP_UPDATE;
				}
			}
			//cheyingkun start	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
			//cheyingkun del start
			//			// 如果此时正在智能分类,则此时暂停接受广播,等分类结束以后再进行数据处理 wanghongjian@2015/04/27 UPD START
			//			if( OperateHelp.getInstance( context ).isGettingOperateDate() )
			//			{
			//				mOps.add( op );
			//				mPkgNames.add( packageName );
			//			}
			//			else
			//			{
			//				enqueuePackageUpdatedApp( op , packageName );
			//			}
			//			// wanghongjian@2015/04/27 UPD END
			//cheyingkun del end
			//cheyingkun add start
			enqueuePackageUpdatedApp( op , new String[]{ packageName } );
			//cheyingkun add end
			//cheyingkun end
		}
		else if( Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals( action ) )
		{
			//cheyingkun add start	//针对开关机会发送T卡挂载信息的手机,添加关机和保存信息的判断
			//关机不处理T卡插入操作
			TCardMountManager mTCardMountManager = TCardMountManager.getInstance( context );
			if( mTCardMountManager != null//
					&& !mTCardMountManager.canTCardMount() )//如果不能进行T卡挂载相关操作
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , " hasTCardMountInfo : not available" );
				return;
			}
			//cheyingkun add end
			//			cancelDrag();//cheyingkun add //TCardMountCancelDrag(收到T卡挂载和安装广播停止拖拽)//cheyingkun del	//打开usb存储设备,图标灰色,长按灰色图标不松手,拔掉usb线时不再停止拖拽,改为更新dragview
			// First, schedule to add these apps back in.
			String[] packages = intent.getStringArrayExtra( Intent.EXTRA_CHANGED_PACKAGE_LIST );
			//			enqueuePackageUpdated( new PackageUpdatedTask( PackageUpdatedTask.OP_ADD , packages ) );//cheyingkun del	//TCardMount
			enqueuePackageUpdated( new PackageUpdatedTask( PackageUpdatedTask.OP_AVAILABLE , packages ) );//cheyingkun add	//TCardMount
			// Then, rebind everything.
			//			startLoaderFromBackground();//cheyingkun del	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
		}
		else if( Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals( action ) )
		{
			//cheyingkun add start	//针对开关机会发送T卡挂载信息的手机,添加关机和保存信息的判断
			TCardMountManager mTCardMountManager = TCardMountManager.getInstance( context );
			if( mTCardMountManager != null//
					&& !mTCardMountManager.canTCardMount() )//如果不能进行T卡挂载相关操作
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , " hasTCardMountInfo :not save again" );
				return;//如果关机已经保存过挂载信息,直接返回,不重复保存
			}
			//cheyingkun add end
			//			cancelDrag();//cheyingkun add //TCardMountCancelDrag(收到T卡挂载和安装广播停止拖拽)//cheyingkun del	//打开usb存储设备,图标灰色,长按灰色图标不松手,拔掉usb线时不再停止拖拽,改为更新dragview
			String[] packages = intent.getStringArrayExtra( Intent.EXTRA_CHANGED_PACKAGE_LIST );
			enqueuePackageUpdated( new PackageUpdatedTask( PackageUpdatedTask.OP_UNAVAILABLE , packages ) );
			//			startLoaderFromBackground();//cheyingkun add	//TCardMount(参考取消挂载:挂载T卡,灰化处理后,重新绑定所有app,桌面会闪一下)//cheyingkun del	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
		}
		else if( Intent.ACTION_LOCALE_CHANGED.equals( action ) )
		{
			// If we have changed locale we need to clear out the labels in all apps/workspace.
			forceReload();
		}
		else if( Intent.ACTION_CONFIGURATION_CHANGED.equals( action ) )
		{
			// Check if configuration change was an mcc/mnc change which would affect app resources
			// and we would need to clear out the labels in all apps/workspace. Same handling as
			// above for ACTION_LOCALE_CHANGED
			Configuration currentConfig = context.getResources().getConfiguration();
			// mobile country code(mcc)的值为零是没有意义的 yangxiaoming add 2015/04/03 fix bug 0010741
			if( mPreviousConfigMcc != currentConfig.mcc && mPreviousConfigMcc != 0 )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "Reload apps on config change. curr_mcc:" , currentConfig.mcc , " prevmcc:" , mPreviousConfigMcc ) );
				forceReload();
			}
			// Update previousConfig
			mPreviousConfigMcc = currentConfig.mcc;
		}
		else if(
		// 
		( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE//桌面显示搜索
		|| ( LauncherDefaultConfig.SWITCH_ENABLE_FAVORITES && LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_FAVORITES_PAGE ) )//-1屏显示搜索
				//
				&& ( LauncherDefaultConfig.CONFIG_SEARCH_BAR_STYLE == LauncherDefaultConfig.SEARCH_BAR_STYLE_GLOBAL_SEARCH /* //xiatian add	//添加配置项“config_search_bar_type”，搜索栏中搜索的配置参数。0为酷搜，1为安卓的全局搜索。默认为0。（详见BaseDefaultConfig.java中的“SEARCH_BAR_STYLE_XXX”） */)
				//
				&& ( ( Build.VERSION.SDK_INT >= 16 && SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED.equals( action ) ) || SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED.equals( action ) )
		//
		)
		{
			if( mCallbacks != null )
			{
				Callbacks callbacks = mCallbacks.get();
				if( callbacks != null )
				{
					callbacks.bindSearchablesChanged();
				}
			}
		}
		//cheyingkun add start	//deleteGreyApp(灰化图标可删除)bug:i_0009469
		else if( LauncherAppState.REMOVE_TCRADMOUNT_GRAY_APP.equals( action ) )
		{
			String packageName = intent.getStringExtra( LauncherAppState.REMOVE_TCRADMOUNT_GRAY_APP );
			enqueuePackageUpdated( new PackageUpdatedTask( PackageUpdatedTask.OP_REMOVE , new String[]{ packageName } ) );
		}
		//cheyingkun add end
		//cheyingkun add start	//针对开关机会发送T卡挂载信息的手机,添加关机和保存信息的判断
		else if( Intent.ACTION_SHUTDOWN.equals( action ) )
		{
			TCardMountManager mTCardMountManager = TCardMountManager.getInstance( context );
			if( mTCardMountManager != null )
			{
				mTCardMountManager.setShuttingDown( true );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , " isShutDown " );
			}
		}
		//cheyingkun add end
		//cheyingkun add start	//是否监听飞利浦壁纸改变广播【c_0003456】
		else if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOMER_PHILIPS_WALLPAPER_CHANGED_NOTIFY//
				&& LauncherAppState.WALLPAPER_CHANGED_PHILIPS.equals( intent.getAction() ) )
		{
			WallpaperManager mWallpaperManager = WallpaperManager.getInstance( context );
			int desiredWidth = mWallpaperManager.getDesiredMinimumWidth();
			int desiredHeight = mWallpaperManager.getDesiredMinimumHeight();
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "WallpaperManager" , StringUtils.concat( " philips.wallpaperChanged LauncherModel onReceive w:" , desiredWidth , " h:" , desiredHeight ) );
			SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
			Editor edit = mSharedPreferences.edit();
			edit.putInt( LauncherAppState.WALLPAPER_DESIRED_WIDTH , desiredWidth );
			edit.putInt( LauncherAppState.WALLPAPER_DESIRED_HEIGHT , desiredHeight );
			edit.commit();
		}
		//cheyingkun add end
		else if( intent.getAction().equals( LauncherAppState.ACTION_HIDEAPP ) )
		{
			int myOp = PackageUpdatedTask.OP_REMOVE;
			String myPackageName = intent.getStringExtra( "HIDE_APPS_INFO" );
			//			UserHandleCompat user;
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "myPackageName = " , myPackageName ) );
			if( myPackageName == null || myPackageName.length() == 0 )
			{
				return;
			}
			enqueuePackageUpdated( new PackageUpdatedTask( myOp , new String[]{ myPackageName } ) );
		}
		else if( "android.intent.action.SIM_STATE_CHANGED".equals( action ) && LauncherDefaultConfig.NO_SIMCARD_UNDISPLAY_SIMCARD_APPLICATION )
		{
			if( intent.getStringExtra( "ss" ).equals( "LOADED" ) )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "lvjiangbin" , "action = LOADED" );
				updateSimCard();
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.v( "lvjiangbin" , StringUtils.concat( "action = " , action , "-getStringExtra = " , intent.getStringExtra( "ss" ) ) );
			}
		}
		//xiatian add start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
		else if(
		//
		LauncherDefaultConfig.SWITCH_ENABLE_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH
		//
		&& ( LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_ACTION.equals( action ) )
		//
		)
		{
			int mConfigCustomerLXTChangeCustomPathStyle = SystemProperties.getInt(
					LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_KEY ,
					LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST );
			if(
			//
			( mConfigCustomerLXTChangeCustomPathStyle != LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST )
			//
			&& ( mConfigCustomerLXTChangeCustomPathStyle != LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_NV_MAP )
			//
			)
			{
				throw new IllegalStateException( "[error] LauncherModel - unknow mConfigCustomerLXTChangeCustomPathStyle:" + mConfigCustomerLXTChangeCustomPathStyle );
			}
			if( mConfigCustomerLXTChangeCustomPathStyle == LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST )
			{
				//parse CustomDefaultConfigPath
				String mCustomDefaultConfigPath = intent.getStringExtra( LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_PATH );
				if( TextUtils.isEmpty( mCustomDefaultConfigPath ) )
				{
					mCustomDefaultConfigPath = LauncherDefaultConfig.CUSTOM_DEFAULT_CONFIG_PATH_DEFAULT;
				}
				//save CustomDefaultConfigPath to sd card "/cooee/launcher/phenix/CustomerLXTCchangeCustomConfigPathTemp"
				Tools.writeStringToFile(
						LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_FILE_DIR ,
						LauncherDefaultConfig.CONFIG_CUSTOMER_LXT_CHANGE_CUSTOM_CONFIG_PATH_STYLE_BROADCAST_KEY_FILE_NAME ,
						mCustomDefaultConfigPath );
			}
			//清空桌面数据
			String mCommondToClearSelf = "pm clear com.cooee.phenix";
			java.lang.Process p = null;
			try
			{
				p = Runtime.getRuntime().exec( mCommondToClearSelf );
			}
			catch( IOException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.v( "mCommondToClearSelf" , "exec Runtime commond:" + mCommondToClearSelf + ", IOException" + e );
				}
				e.printStackTrace();
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.v( "mCommondToClearSelf" , "exec Runtime commond:" + mCommondToClearSelf + ", Process:" + p );
			}
		}
		//xiatian add end
	}
	
	//cheyingkun del start	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)	
	//	/**
	//	 * 此时看看数据是否存储，若有数据存储则手动调用一下enqueuePackageUpdatedApp加载，卸载应用的流程
	//	 */
	//	public void restartOnReceive()
	//	{
	//		if( mOps.size() > 0 && mPkgNames.size() > 0 && mOps.size() == mPkgNames.size() )
	//		{
	//			for( int i = 0 ; i < mPkgNames.size() ; i++ )
	//			{
	//				int op = mOps.get( i );
	//				if( op != PackageUpdatedTask.OP_NONE )
	//				{
	//					String pkgName = mPkgNames.get( i );
	//					enqueuePackageUpdatedApp( op , pkgName );
	//				}
	//			}
	//		}
	//	}
	//cheyingkun add end
	/**
	 * 通过对应的op值和packageName去添加，卸载，更新应用
	 * @param op
	 * @param packageName
	 */
	public void enqueuePackageUpdatedApp(
			int op ,
			String[] packages )
	{
		if( op != PackageUpdatedTask.OP_NONE )
		{
			enqueuePackageUpdated( new PackageUpdatedTask( op , packages ) );
		}
		//cheyingkun del start	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
		//		mOps.clear();
		//		mPkgNames.clear();
		//cheyingkun del end
	}
	
	private void forceReload()
	{
		resetLoadedState( true , true );
		// Do this here because if the launcher activity is running it will be restarted.
		// If it's not running startLoaderFromBackground will merely tell it that it needs
		// to reload.
		startLoaderFromBackground();
	}
	
	public void resetLoadedState(
			boolean resetAllAppsLoaded ,
			boolean resetWorkspaceLoaded )
	{
		synchronized( mLock )
		{
			// Stop any existing loaders first, so they don't set mAllAppsLoaded or
			// mWorkspaceLoaded to true later
			stopLoaderLocked();
			if( resetAllAppsLoaded )
				mAllAppsLoaded = false;
			if( resetWorkspaceLoaded )
				mWorkspaceLoaded = false;
		}
	}
	
	/**
	 * When the launcher is in the background, it's possible for it to miss paired
	 * configuration changes.  So whenever we trigger the loader from the background
	 * tell the launcher that it needs to re-run the loader when it comes back instead
	 * of doing it now.
	 */
	public void startLoaderFromBackground()
	{
		boolean runLoader = false;
		if( mCallbacks != null )
		{
			Callbacks callbacks = mCallbacks.get();
			if( callbacks != null )
			{
				// Only actually run the loader if they're not paused.
				if( !callbacks.setLoadOnResume() )
				{
					runLoader = true;
				}
			}
		}
		if( runLoader )
		{
			startLoader( false , -1 );
		}
	}
	
	// If there is already a loader task running, tell it to stop.
	// returns true if isLaunching() was true on the old task
	private boolean stopLoaderLocked()
	{
		boolean isLaunching = false;
		LoaderTask oldTask = mLoaderTask;
		if( oldTask != null )
		{
			if( oldTask.isLaunching() )
			{
				isLaunching = true;
			}
			oldTask.stopLocked();
		}
		return isLaunching;
	}
	
	public void startLoader(
			boolean isLaunching ,
			int synchronousBindPage )
	{
		synchronized( mLock )
		{
			if( DEBUG_LOADERS )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "startLoader isLaunching=" , isLaunching ) );
			}
			// Clear any deferred bind-runnables from the synchronized load process
			// We must do this before any loading/binding is scheduled below.
			mDeferredBindRunnables.clear();
			// Don't bother to start the thread if we know it's not going to do anything
			if( mCallbacks != null && mCallbacks.get() != null )
			{
				// If there is already one running, tell it to stop.
				// also, don't downgrade isLaunching if we're already running
				isLaunching = isLaunching || stopLoaderLocked();
				mLoaderTask = new LoaderTask( mApp.getContext() , isLaunching );
				if( synchronousBindPage > -1 && mAllAppsLoaded && mWorkspaceLoaded )
				{
					mLoaderTask.runBindSynchronousPage( synchronousBindPage );
				}
				else
				{
					sWorkerThread.setPriority( Thread.NORM_PRIORITY );
					sWorker.postDelayed( mLoaderTask , LauncherDefaultConfig.getInt( R.integer.config_launcher_startloader_delay ) );
				}
			}
		}
	}
	
	void bindRemainingSynchronousPages()
	{
		// Post the remaining side pages to be loaded
		if( !mDeferredBindRunnables.isEmpty() )
		{
			for( final Runnable r : mDeferredBindRunnables )
			{
				mHandler.post( r , MAIN_THREAD_BINDING_RUNNABLE );
			}
			mDeferredBindRunnables.clear();
		}
	}
	
	public void stopLoader()
	{
		synchronized( mLock )
		{
			if( mLoaderTask != null )
			{
				mLoaderTask.stopLocked();
			}
		}
	}
	
	/** Loads the workspace screens db into a map of Rank -> ScreenId */
	public static TreeMap<Integer , Long> loadWorkspaceScreensDb(
			Context context )
	{
		final ContentResolver contentResolver = context.getContentResolver();
		final Uri screensUri = LauncherSettings.WorkspaceScreens.CONTENT_URI;
		final Cursor sc = contentResolver.query( screensUri , null , null , null , null );
		TreeMap<Integer , Long> orderedScreens = new TreeMap<Integer , Long>();
		try
		{
			final int idIndex = sc.getColumnIndexOrThrow( LauncherSettings.WorkspaceScreens._ID );
			final int rankIndex = sc.getColumnIndexOrThrow( LauncherSettings.WorkspaceScreens.SCREEN_RANK );
			while( sc.moveToNext() )
			{
				try
				{
					long screenId = sc.getLong( idIndex );
					int rank = sc.getInt( rankIndex );
					orderedScreens.put( rank , screenId );
				}
				catch( Exception e )
				{
					Launcher.addDumpLog( TAG , StringUtils.concat( "Desktop items loading interrupted - invalid screens: " + e.toString() ) , true );
				}
			}
		}
		finally
		{
			sc.close();
		}
		return orderedScreens;
	}
	
	public boolean isAllAppsLoaded()
	{
		return mAllAppsLoaded;
	}
	
	//cheyingkun add start	//解决“恢复出厂设置后会出现缺少相机应用图标”的问题。【c_0004169】
	public void setAllAppsLoaded(
			boolean mAllAppsLoaded )
	{
		this.mAllAppsLoaded = mAllAppsLoaded;
	}
	
	//cheyingkun add end
	boolean isLoadingWorkspace()
	{
		synchronized( mLock )
		{
			if( mLoaderTask != null )
			{
				return mLoaderTask.isLoadingWorkspace();
			}
		}
		return false;
	}
	
	//cheyingkun add start	//解决“由于启动页消失从桌面加载结束提前到当前页bind结束，在开启免责声明并且当前页bind结束桌面没加载结束时,点击免责声明继续使用按钮后,启动页不消失”的问题。
	boolean isFinishBindWrokspaceCurrentScreen()
	{
		synchronized( mLock )
		{
			if( mLoaderTask != null )
			{
				return mLoaderTask.isFinishBindWrokspaceCurrentScreen();
			}
		}
		//cheyingkun start//解决“打开免责声明情况下，等桌面加载完成之后点击继续使用，启动页不消失”的问题。【i_0012388】
		//		return false;//cheyingkun del
		return true;//cheyingkun add
		//cheyingkun end
	}
	
	//cheyingkun add end
	// chenchen add start //从Launcher类中获取tm，sb对象
	public void setSystemService(
			TelephonyManager telephonyManager )
	{
		this.mTelephonyManager = telephonyManager;
	}
	
	// chenchen add and
	/**
	 * Runnable for the thread that loads the contents of the launcher:
	 *   - workspace icons
	 *   - widgets
	 *   - all apps icons
	 */
	private class LoaderTask implements Runnable
	{
		
		private Context mContext;
		private boolean mIsLaunching;
		private boolean mIsLoadingAndBindingWorkspace;
		private boolean mStopped;
		private boolean mLoadAndBindStepFinished;
		private HashMap<Object , CharSequence> mLabelCache;
		private boolean mIsFinishBindWrokspaceCurrentScreen = false;//cheyingkun add	//解决“由于启动页消失从桌面加载结束提前到当前页bind结束，在开启免责声明并且当前页bind结束桌面没加载结束时,点击免责声明继续使用按钮后,启动页不消失”的问题。
		
		LoaderTask(
				Context context ,
				boolean isLaunching )
		{
			mContext = context;
			mIsLaunching = isLaunching;
			mLabelCache = new HashMap<Object , CharSequence>();
		}
		
		boolean isLaunching()
		{
			return mIsLaunching;
		}
		
		boolean isLoadingWorkspace()
		{
			return mIsLoadingAndBindingWorkspace;
		}
		
		//cheyingkun add start	//解决“由于启动页消失从桌面加载结束提前到当前页bind结束，在开启免责声明并且当前页bind结束桌面没加载结束时,点击免责声明继续使用按钮后,启动页不消失”的问题。
		public boolean isFinishBindWrokspaceCurrentScreen()
		{
			return mIsFinishBindWrokspaceCurrentScreen;
		}
		
		//cheyingkun add end
		/** Returns whether this is an upgrade path */
		private boolean loadAndBindWorkspace()
		{
			mIsLoadingAndBindingWorkspace = true;
			mIsFinishBindWrokspaceCurrentScreen = false;//cheyingkun add	//解决“由于启动页消失从桌面加载结束提前到当前页bind结束，在开启免责声明并且当前页bind结束桌面没加载结束时,点击免责声明继续使用按钮后,启动页不消失”的问题。
			// Load the workspace
			if( DEBUG_LOADERS )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "loadAndBindWorkspace mWorkspaceLoaded=" , mWorkspaceLoaded ) );
			}
			boolean isUpgradePath = false;
			if( !mWorkspaceLoaded )
			{
				if( LauncherAppState.isAlreadyCategory( mContext ) )
				{
					if( LauncherDefaultConfig.CONFIG_CATEGORY_TYPE == CategoryConstant.OPERATE_CATEGORY && !CategoryHelper.getInstance( BaseAppState.getActivityInstance() ).canDoCategory() )//后台关闭智能分类，重启时恢复布局
					{
						SQLiteDatabase db = LauncherAppState.getLauncherProvider().getProviderDB();
						try
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "" , "shlt , test , stopCategory , in try" );
							db.beginTransaction();
							//先把表复制前来~
							db.execSQL( StringUtils.concat( "DROP TABLE IF EXISTS " , LauncherProvider.DatabaseHelper.getFavoritesTabName() ) );
							db.execSQL( StringUtils.concat( "DROP TABLE IF EXISTS " , LauncherProvider.DatabaseHelper.getWorkspacesTabName() ) );
							db.execSQL( StringUtils.concat( "create table " , LauncherProvider.DatabaseHelper.getFavoritesTabName() , " as select * from temp_favorites" ) );
							db.execSQL( StringUtils.concat( "create table " , LauncherProvider.DatabaseHelper.getWorkspacesTabName() , " as select * from temp_workspaceScreens" ) );
							db.execSQL( "DROP TABLE IF EXISTS temp_favorites" );
							db.execSQL( "DROP TABLE IF EXISTS temp_workspaceScreens" );
							db.setTransactionSuccessful();
						}
						catch( Exception e )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "" , "shlt , test , stopCategory , catch{}" );
							e.printStackTrace();
						}
						finally
						{
							db.endTransaction();
							SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( mContext );
							sp.edit().remove( OperateHelp.ClassificationTime ).commit();
						}
						CategoryParse.setCategoryBackgroundSwitch( false );
					}
				}
				isUpgradePath = loadWorkspace();
				synchronized( LoaderTask.this )
				{
					if( mStopped )
					{
						return isUpgradePath;
					}
					mWorkspaceLoaded = true;
				}
			}
			// Bind the workspace
			bindWorkspace( -1 , isUpgradePath );
			return isUpgradePath;
		}
		
		private void waitForIdle()
		{
			// Wait until the either we're stopped or the other threads are done.
			// This way we don't start loading all apps until the workspace has settled
			// down.
			synchronized( LoaderTask.this )
			{
				final long workspaceWaitTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
				mHandler.postIdle( new Runnable() {
					
					public void run()
					{
						synchronized( LoaderTask.this )
						{
							mLoadAndBindStepFinished = true;
							if( DEBUG_LOADERS )
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.d( TAG , "done with previous binding step" );
							}
							LoaderTask.this.notify();
						}
					}
				} );
				while( !mStopped && !mLoadAndBindStepFinished && !mFlushingWorkerThread )
				{
					try
					{
						// Just in case mFlushingWorkerThread changes but we aren't woken up,
						// wait no longer than 1sec at a time
						this.wait( 1000 );
					}
					catch( InterruptedException ex )
					{
						// Ignore
					}
				}
				if( DEBUG_LOADERS )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "waited " , ( SystemClock.uptimeMillis() - workspaceWaitTime ) , "ms for previous step to finish binding" ) );
				}
			}
		}
		
		void runBindSynchronousPage(
				int synchronousBindPage )
		{
			if( synchronousBindPage < 0 )
			{
				// Ensure that we have a valid page index to load synchronously
				throw new RuntimeException( "Should not call runBindSynchronousPage() without valid page index" );
			}
			if( !mAllAppsLoaded || !mWorkspaceLoaded )
			{
				// Ensure that we don't try and bind a specified page when the pages have not been
				// loaded already (we should load everything asynchronously in that case)
				throw new RuntimeException( "Expecting AllApps and Workspace to be loaded" );
			}
			synchronized( mLock )
			{
				if( mIsLoaderTaskRunning )
				{
					// Ensure that we are never running the background loading at this point since
					// we also touch the background collections
					throw new RuntimeException( "Error! Background loading is already running" );
				}
			}
			// XXX: Throw an exception if we are already loading (since we touch the worker thread
			//      data structures, we can't allow any other thread to touch that data, but because
			//      this call is synchronous, we can get away with not locking).
			// The LauncherModel is static in the LauncherAppState and mHandler may have queued
			// operations from the previous activity.  We need to ensure that all queued operations
			// are executed before any synchronous binding work is done.
			mHandler.flush();
			// Divide the set of loaded items into those that we are binding synchronously, and
			// everything else that is to be bound normally (asynchronously).
			bindWorkspace( synchronousBindPage , false );
			// XXX: For now, continue posting the binding of AllApps as there are other issues that
			//      arise from that.
			onlyBindAllApps();
		}
		
		public void run()
		{
			boolean isUpgrade = false;
			synchronized( mLock )
			{
				mIsLoaderTaskRunning = true;
			}
			// Optimize for end-user experience: if the Launcher is up and // running with the
			// All Apps interface in the foreground, load All Apps first. Otherwise, load the
			// workspace first (default).
			keep_running:
			{
				// Elevate priority when Home launches for the first time to avoid
				// starving at boot time. Staring at a blank home is not cool.
				//<phenix modify> liuhailin@2015-01-26 modify begin
				//LauncherBaseConfig.setDefaultIcon();
				//<phenix modify> liuhailin@2015-01-26 modify end
				synchronized( mLock )
				{
					if( DEBUG_LOADERS )
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG , StringUtils.concat( "Setting thread priority to " , ( mIsLaunching ? "DEFAULT" : "BACKGROUND" ) ) );
					android.os.Process.setThreadPriority( mIsLaunching ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND );
				}
				if( DEBUG_LOADERS )
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , "step 1: loading workspace" );
				isUpgrade = loadAndBindWorkspace();
				if( mStopped )
				{
					break keep_running;
				}
				// Whew! Hard work done.  Slow us down, and wait until the UI thread has
				// settled down.
				synchronized( mLock )
				{
					if( mIsLaunching )
					{
						if( DEBUG_LOADERS )
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG , "Setting thread priority to BACKGROUND" );
						android.os.Process.setThreadPriority( Process.THREAD_PRIORITY_BACKGROUND );
					}
				}
				//				waitForIdle();//zhujieping del，这个是在等主线程绑定view结束，下面的内容跟绑定view没有关系，去掉
				// second step
				if( DEBUG_LOADERS )
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , "step 2: loading all apps" );
				loadAndBindAllApps();
				// Restore the default thread priority after we are done loading items
				synchronized( mLock )
				{
					android.os.Process.setThreadPriority( Process.THREAD_PRIORITY_DEFAULT );
				}
			}
			// Update the saved icons if necessary
			if( DEBUG_LOADERS )
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , "Comparing loaded icons to database icons" );
			synchronized( sBgLock )
			{
				for( Object key : sBgDbIconCache.keySet() )
				{
					updateSavedIcon( mContext , (ShortcutInfo)key , sBgDbIconCache.get( key ) );
				}
				sBgDbIconCache.clear();
			}
			if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
			{
				// Ensure that all the applications that are in the system are
				// represented on the home screen.
				if( !UPGRADE_USE_MORE_APPS_FOLDER || !isUpgrade )
				{
					verifyApplications();
				}
			}
			//xiatian del start	//解决“加载桌面时，先显示第一页，再跳到默认主页”的问题（由“加载item完毕之后，再跳到默认主页”改为“在加载item之前，直接跳到默认主页”）。【c_0004499】
			//			//xiatian add start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
			//			final Callbacks oldCallbacks = mCallbacks.get();
			//			bindDefaultPage( oldCallbacks );
			//			//xiatian add end
			//xiatian del end
			// Clear out this reference, otherwise we end up holding it until all of the
			// callback runnables are done.
			mContext = null;
			synchronized( mLock )
			{
				// If we are still the last one to be scheduled, remove ourselves.
				if( mLoaderTask == this )
				{
					mLoaderTask = null;
				}
				mIsLoaderTaskRunning = false;
				//cheyingkun add start	//完善T卡挂载逻辑(加载完后,调用下面这个方法,执行开始智能分类到加载结束期间的task逻辑处理)
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( "mIsLoaderTaskRunning: " , mIsLoaderTaskRunning ) );
				startTaskOnLoaderTaskFinish();
				//cheyingkun add end
				//cheyingkin add start	//解决“智能分类或恢复布局时，进度条消失后桌面未刷新前快速打开文件夹，桌面刷新后文件夹依然是打开状态”的问题【i_0010669】
				OperateHelp operateHelp = OperateHelp.getInstance( null );
				if( operateHelp != null )
				{
					operateHelp.dismissCategoryProgressView();
				}
				//cheyingkin add end
				//cheyingkun add start	//优化加载速度(智能分类初始化放到加载完成之后)
				mHandler.post( new Runnable() {
					
					public void run()
					{
						final Callbacks oldCallbacks = mCallbacks.get();
						Callbacks callbacks = tryGetCallbacks( oldCallbacks );
						if( callbacks != null )
						{
							callbacks.loadFinish();
						}
					}
				} );
				//cheyingkun add end
			}
		}
		
		public void stopLocked()
		{
			synchronized( LoaderTask.this )
			{
				mStopped = true;
				this.notify();
			}
		}
		
		/**
		 * Gets the callbacks object.  If we've been stopped, or if the launcher object
		 * has somehow been garbage collected, return null instead.  Pass in the Callbacks
		 * object that was around when the deferred message was scheduled, and if there's
		 * a new Callbacks object around then also return null.  This will save us from
		 * calling onto it with data that will be ignored.
		 */
		Callbacks tryGetCallbacks(
				Callbacks oldCallbacks )
		{
			synchronized( mLock )
			{
				if( mStopped )
				{
					return null;
				}
				if( mCallbacks == null )
				{
					return null;
				}
				final Callbacks callbacks = mCallbacks.get();
				if( callbacks != oldCallbacks )
				{
					return null;
				}
				if( callbacks == null )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.w( TAG , "no mCallbacks" );
					return null;
				}
				return callbacks;
			}
		}
		
		private void verifyApplications()
		{
			final Context context = mApp.getContext();
			// Cross reference all the applications in our apps list with items in the workspace
			ArrayList<ItemInfo> tmpInfos;
			ArrayList<ItemInfo> added = new ArrayList<ItemInfo>();
			// yangxiaoming add start 构造一个新的List 2015/05/25
			ArrayList<ItemInfo> app_list = new ArrayList<ItemInfo>();
			// yangxiaoming add end
			synchronized( sBgLock )
			{
				for( AppInfo app : mBgAllAppsList.data )
				{
					tmpInfos = getItemInfoForComponentName( app.getComponentName() );
					if( tmpInfos.isEmpty() )
					{
						// We are missing an application icon, so add this to the workspace
						added.add( app );
						// This is a rare event, so lets log it
						//Log.e( TAG , "Missing Application on load: " + app );
					}
				}
			}
			if( !added.isEmpty() )
			{
				// yangxiaoming delete start 使用这种方法排序非常影响效率，这个排序其实就是找出有默认icon的app放置在桌面最前面的位置 2015/05/25
				// Collections.sort( added , new ShortcutReplaceIconComparator() );
				// yangxiaoming delete end
				// yangxiaoming add start 改用传统的List方法排序，效率提高明显 2015/05/25
				app_list = sortAppByDefaultIcon( added );
				// yangxiaoming add end
				Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
				// yangxiaoming delete start 删除使用原来的排序法生成的集合代码 2015/05/25
				// addAndBindAddedApps( context , added , cb , null );
				// yangxiaoming delete end
				// yangxiaoming add start 使用新的排序方法产生的集合 2015/05/25
				//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcherModel verifyApplications: app_list.size():" , app_list.size() ) );
				}
				//cheyingkun add end
				addAndBindAddedItems( context , app_list , null , cb , null , false , !mIsLoaderTaskRunning );
				// yangxiaoming add end
			}
		}
		
		private boolean checkItemDimensions(
				ItemInfo info )
		{
			LauncherAppState app = LauncherAppState.getInstance();
			DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
			return ( info.getCellX() + info.getSpanX() ) > (int)grid.getNumColumns() || ( info.getCellY() + info.getSpanY() ) > (int)grid.getNumRows();
		}
		
		// check & update map of what's occupied; used to discard overlapping/invalid items
		private boolean checkItemPlacement(
				HashMap<Long , ItemInfo[][]> occupied ,
				ItemInfo item ,
				AtomicBoolean deleteOnItemOverlap )
		{
			LauncherAppState app = LauncherAppState.getInstance();
			DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
			int countX = (int)grid.getNumColumns();
			int countY = (int)grid.getNumRows();
			long containerIndex = item.getScreenId();
			if( item.getContainer() == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
			{
				// Return early if we detect that an item is under the hotseat button
				if( mCallbacks == null || mCallbacks.get().isAllAppsButtonRank( (int)item.getScreenId() ) )
				{
					deleteOnItemOverlap.set( true );
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , "Error loading shortcut into hotseat isAllAppsButtonRank" + item );
					return false;
				}
				if( occupied.containsKey( LauncherSettings.Favorites.CONTAINER_HOTSEAT ) )
				{
					if( occupied.get( LauncherSettings.Favorites.CONTAINER_HOTSEAT )[(int)item.getScreenId()][0] != null )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.e( TAG , StringUtils.concat(
									"Error loading shortcut into hotseat " + item ,
									" into position (" ,
									item.getScreenId() ,
									":" ,
									item.getCellX() ,
									"," ,
									item.getCellY() ,
									") occupied by " + occupied.get( LauncherSettings.Favorites.CONTAINER_HOTSEAT )[(int)item.getScreenId()][0] ) );
						return false;
					}
				}
				else
				{
					ItemInfo[][] items = new ItemInfo[countX + 1][countY + 1];
					items[(int)item.getScreenId()][0] = item;
					occupied.put( (long)LauncherSettings.Favorites.CONTAINER_HOTSEAT , items );
					return true;
				}
			}
			else if( item.getContainer() != LauncherSettings.Favorites.CONTAINER_DESKTOP )
			{
				// Skip further checking if it is not the hotseat or workspace container
				return true;
			}
			if( !occupied.containsKey( item.getScreenId() ) )
			{
				ItemInfo[][] items = new ItemInfo[countX + 1][countY + 1];
				occupied.put( item.getScreenId() , items );
			}
			ItemInfo[][] screens = occupied.get( item.getScreenId() );
			// Check if any workspace icons overlap with each other
			for( int x = item.getCellX() ; x < ( item.getCellX() + item.getSpanX() ) ; x++ )
			{
				for( int y = item.getCellY() ; y < ( item.getCellY() + item.getSpanY() ) ; y++ )
				{
					if( screens[x][y] != null )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.e( TAG , StringUtils.concat(
									"Error loading shortcut " + item ,
									" into cell (" ,
									containerIndex ,
									"-" ,
									item.getScreenId() ,
									":" ,
									x ,
									"," ,
									y ,
									") occupied by " + screens[x][y] ) );
						return false;
					}
				}
			}
			for( int x = item.getCellX() ; x < ( item.getCellX() + item.getSpanX() ) ; x++ )
			{
				for( int y = item.getCellY() ; y < ( item.getCellY() + item.getSpanY() ) ; y++ )
				{
					screens[x][y] = item;
				}
			}
			return true;
		}
		
		/** Clears all the sBg data structures */
		private void clearSBgDataStructures()
		{
			synchronized( sBgLock )
			{
				sBgWorkspaceItems.clear();
				sBgAppWidgets.clear();
				sBgFolders.clear();
				sBgItemsIdMap.clear();
				sBgDbIconCache.clear();
				sBgWorkspaceScreens.clear();
			}
		}
		
		/**	//如果有卡1或卡2任何一张，则移除自己之外的另一个Sim卡应用图标
		 * 从数据库中移除数组中所含需要移除的包名
		 * chenchen add 2016/4/15
		 * @param cn
		 * @param isHideSimCardApp
		 */
		private boolean isHideSimCard(
				ComponentName cn )
		{
			// chenchen add start //从数组中获取到需要移除的包
			if( BaseDefaultConfig.mHideSimCardList.size() > 0 )
			{
				if( !BaseDefaultConfig.mHideSimCardList.contains( cn ) )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "lvjiangbin" , StringUtils.concat( "isHideSimCard cn false" + cn.toString() ) );
					return false;
				}
			}
			int hideSimCardListSize = BaseDefaultConfig.mHideSimCardList.size();
			boolean[] simType = Tools.spreadSimReady( mApp.getContext() );
			if( simType.length > 0 )
			{
				for( int j = 0 ; j < simType.length && j < hideSimCardListSize ; j++ )
				{
					if( !simType[j] && BaseDefaultConfig.mHideSimCardList.get( j ).equals( cn ) )
					{
						return true;
					}
				}
			}
			return false;
		}
		
		/**无卡状态时
		 * 在应用列表中删除Sim卡的应用图标
		 * chenchen add 2016/4/15
		 * @param apps  所有APP的集合
		 */
		private void hideSimCard(
				List<ResolveInfo> apps )
		{
			List<ResolveInfo> removeApps = new ArrayList<ResolveInfo>();
			for( int i = 0 ; i < apps.size() ; i++ )
			{
				int temp = -1;
				ResolveInfo packageInfo = apps.get( i );
				String pagName = packageInfo.activityInfo.packageName;
				String className = packageInfo.activityInfo.name;
				ComponentName componentName = new ComponentName( pagName , className );
				if( isHideSimCard( componentName ) )
				{
					temp = i;
				}
				if( temp != -1 )
				{
					removeApps.add( packageInfo );
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "lvjiangbin" , StringUtils.concat( "hide cn = " , componentName.toString() ) );
				}
			}
			if( removeApps.size() > 0 )
			{
				apps.removeAll( removeApps );
			}
		}
		
		/** Returns whether this is an upgradge path */
		private boolean loadWorkspace()
		{
			final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
			final Context context = mContext;
			final ContentResolver contentResolver = context.getContentResolver();
			final PackageManager manager = context.getPackageManager();
			final AppWidgetManager widgets = AppWidgetManager.getInstance( context );
			final boolean isSafeMode = manager.isSafeMode();
			LauncherAppState app = LauncherAppState.getInstance();
			DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
			int countX = (int)grid.getNumColumns();
			int countY = (int)grid.getNumRows();
			// Make sure the default workspace is loaded, if needed
			LauncherAppState.getLauncherProvider().loadDefaultFavoritesIfNecessary( 0 );
			// Check if we need to do any upgrade-path logic
			boolean loadedOldDb = LauncherAppState.getLauncherProvider().justLoadedOldDb();
			synchronized( sBgLock )
			{
				clearSBgDataStructures();
				//xiatian add start	//解决“未智能分类模式下关闭双开开关的前提下，智能分类，并在智能分类模式打开双开开关，等双开图标创建后，再退出智能分类模式，这时桌面没双开图标但是系统设置中的双开开关是打开状态”的问题【c_0004640】
				//【问题原因】“智能分类模式”和“未智能分类模式”这两个模式独立的，快捷方式只会添加当前的模式的数据库。
				//【解决方案】每次加载的时候，都关闭所有双开开关。在加载双开图标时，打开对应双开开关。
				ZhiKeShortcutManager.getInstance( context ).closeAllZhikeShortcutSwitchs();
				//xiatian add end
				final ArrayList<Long> itemsToRemove = new ArrayList<Long>();
				final ArrayList<Integer> itemsRemoveInFolder = new ArrayList<Integer>();//cheyingkun add//解决“分类前，拖动两个应用生成文件夹。分类后卸载之前的两个应用。恢复之前布局，空文件夹未删除。”的问题【i_0011018】//删除过应用的文件夹id列表,如果数据库中id的应用不存在,并且该id的应用在文件夹中,则加入列表
				final Uri contentUri = LauncherSettings.Favorites.CONTENT_URI;
				if( DEBUG_LOADERS )
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "loading model from " , contentUri.toString() ) );
				final Cursor c = contentResolver.query( contentUri , null , null , null , null );
				// +1 for the hotseat (it can be larger than the workspace)
				// Load workspace in reverse order to ensure that latest items are loaded first (and
				// before any earlier duplicates)
				final HashMap<Long , ItemInfo[][]> occupied = new HashMap<Long , ItemInfo[][]>();
				try
				{
					final int idIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites._ID );
					final int intentIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.INTENT );
					final int titleIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.TITLE );
					final int iconTypeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ICON_TYPE );
					final int iconIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ICON );
					final int iconPackageIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ICON_PACKAGE );
					final int iconResourceIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ICON_RESOURCE );
					final int containerIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CONTAINER );
					//<数据库字段更新> liuhailin@2015-03-23 modify begin
					final int defWorkspaceItemTypeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE );
					final int operateIntentIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.OPERATE_INTENT );
					//<数据库字段更新> liuhailin@2015-03-23 modify end
					final int itemTypeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ITEM_TYPE );
					final int appWidgetIdIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.APPWIDGET_ID );
					final int appWidgetProviderIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.APPWIDGET_PROVIDER );
					final int screenIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.SCREEN );
					final int cellXIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CELLX );
					final int cellYIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CELLY );
					final int spanXIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.SPANX );
					final int spanYIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.SPANY );
					//final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);
					//final int displayModeIndex = c.getColumnIndexOrThrow(
					//        LauncherSettings.Favorites.DISPLAY_MODE);
					ShortcutInfo info;
					String intentDescription;
					//<数据库字段更新> liuhailin@2015-03-24 modify begin
					String operateIntentDescription;
					Intent operateIntent = null;
					//<数据库字段更新> liuhailin@2015-03-24 modify end
					LauncherAppWidgetInfo appWidgetInfo;
					int container;
					long id;
					Intent intent;
					//cheyingkun add start	//TCardMount(防止T卡挂载,图标灰化状态停止launcher,然后再次启动launcher后灰化图标消失)
					//【问题原因】加载workspace时,以前流程是先查询数据库,遍历每一项,判断应用是否有效,无效的加入itemsToRemove,然后删除数据库中itemsToRemove中的所有信息，这样，灰化的图标信息会在数据库删除，导致第二次启动灰化图标消失
					//【解决方案】当挂载T卡时，保存灰化图标信息到文件中，如果不停止launcher直接取消挂载，则清空文件，并对图标进行取消灰化处理。如果挂载状态下停止launcher，重新启动launcher，则从文件中读取挂载信息，当要添加到删除列表时，把读取到的挂载信息和数据库中查询的应用信息进行比较，如果intent相同，表示是挂载灰化的应用，不删除
					//cheyingkun start	//T卡挂载图标根据包类名或者componentName匹配
					//					ArrayList<String> intentStr = new ArrayList<String>();//挂载信息的intent序列化字符串列表 //cheyingkun del
					ArrayList<ComponentName> componentNameListInMountInfo = new ArrayList<ComponentName>();//cheyingkun add
					//cheyingkun end
					Map<Intent , Bitmap> mountInfoMap = null;//从文件中读取的挂在信息的map
					//cheyingkun add start	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
					boolean sdCardExist = LauncherAppState.isSDCardExist();
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "sdCardExist: " , sdCardExist ) );
					//cheyingkun add end
					TCardMountManager mTCardMountManager = TCardMountManager.getInstance( context );
					if( mTCardMountManager != null )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG , StringUtils.concat( "app.count" , c.getCount() ) );
						mTCardMountManager.readFromFile();//读取挂载信息
						mountInfoMap = mTCardMountManager.getMountInfo();
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG , StringUtils.concat( "mountInfoMap.count : " , mountInfoMap.size() ) );
						Set<Intent> keySet = mountInfoMap.keySet();
						for( Intent intent2 : keySet )//初始化列表
						{
							//cheyingkun start	//T卡挂载图标根据包类名或者componentName匹配
							//cheyingkun del start
							//							//cheyingkun start	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
							//							//由比较intent的序列化字符串改为比较包名
							//							//							intentStr.add( intent2.toUri( 0 ) );//cheyingkun del
							//							intentStr.add( intent2.getComponent() );//cheyingkun add
							//cheyingkun end
							//cheyingkun del end
							componentNameListInMountInfo.add( intent2.getComponent() );//cheyingkun add
							//cheyingkun end
						}
					}
					boolean mountInfoContainsIntent = false;//挂载信息是否包含当前的intent
					//cheyingkun add end
					while( !mStopped && c.moveToNext() )
					{
						AtomicBoolean deleteOnItemOverlap = new AtomicBoolean( false );
						try
						{
							int itemType = c.getInt( itemTypeIndex );
							//<数据库字段更新> liuhailin@2015-03-24 modify begin
							operateIntentDescription = c.getString( operateIntentIndex );
							if( operateIntentDescription != null )
							{
								operateIntent = Intent.parseUri( operateIntentDescription , 0 );
							}
							//<数据库字段更新> liuhailin@2015-03-24 modify end
							switch( itemType )
							{
								case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
								case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
								case LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL://xiatian add	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
									id = c.getLong( idIndex );
									intentDescription = c.getString( intentIndex );
									try
									{
										intent = Intent.parseUri( intentDescription , 0 );
										//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
										if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
										{
											Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcherModel loadWorkspace: id " , id , ", intent" , intent.toUri( 0 ) ) );
										}
										//cheyingkun add end
										ComponentName cn = intent.getComponent();
										// chenchen add star
										if( LauncherDefaultConfig.NO_SIMCARD_UNDISPLAY_SIMCARD_APPLICATION )
										{
											if( isHideSimCard( cn ) )
											{
												if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
													Log.v( "lvjiangbin" , StringUtils.concat( "itemsToRemove hide cn = " , cn.toString() ) );
												itemsToRemove.add( id );
												continue;
											}
										}
										// chenchen add end 
										if( itemType == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL )//zjp
										{
											if( !CategoryParse.canShowCategory() )// 不能智能分类，不显示智能分类虚图标
											{
												int virtaulaType = intent.getIntExtra( VirtualInfo.VIRTUAL_TYPE , VirtualInfo.VIRTUAL_TYPE_ERROR );
												if( virtaulaType == VirtualInfo.VIRTUAL_TYPE_CATEGORY_ENTRY )
												{
													itemsToRemove.add( id );//zhujieping，下面有统一的删除动作;
													continue;
												}
											}
										}
										//添加智能分类功能 , change by shlt@2015/02/10 UPD START
										//if( cn != null && !isValidPackageComponent( manager , cn ) )
										//<数据库字段更新> liuhailin@2015-03-24 modify begin
										//if( cn != null && !isValidPackageComponent( manager , intent ) )
										//cheyingkun start	//TCardMount(添加删除时的条件)
										//cheyingkun add start
										if( intent != null && intent.getComponent() != null )
										{
											mountInfoContainsIntent = componentNameListInMountInfo.contains( intent.getComponent() );//cheyingkun add
										}
										//cheyingkun add end
										//cheyingkun end										
										if(
										//
										cn != null
										//
										&& ( itemType != LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL /* //xiatian add	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。 */)
										//
										&& ( !mountInfoContainsIntent/* cheyingkun add	//解决“单层模式下，挂载T卡切换至双层模式再切换回单层，灰色图标消失”的问题。【i_0011907】//判断是否是T卡挂载的应用,如果是,不删除 */)
										//
										&& !isValidPackageComponent( manager , intent , operateIntent )
										//
										)
										//<数据库字段更新> liuhailin@2015-03-24 modify end
										//添加智能分类功能 , change by shlt@2015/02/10 UPD END
										{
											//cheyingkun add start	//解决“单层桌面插sim卡开机，概率性sim卡工具图标丢失”的问题【c_0004504】
											//【问题原因】客户手机识别sim卡较慢，开机后判断sim卡应用不可用、sd卡可插拔，没有删除掉数据库也没有加载图标。
											//sim卡可用之后，桌面添加图标判断到数据库已经有了sim卡的intent，就没有添加，导致图标丢失
											//【解决方案】添加判断应用是否安装在sd卡。如果应用不可用、sd卡可插拔、但是应用没有安装在sd卡，也走删除逻辑。
											boolean isAppInstalledSdcard = false;
											if( intent != null && intent.getComponent() != null //
													&& LauncherAppState.isAppInstalledSdcard( intent.getComponent().getPackageName() , manager ) )
											{
												isAppInstalledSdcard = true;
											}
											if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
												Log.d( "" , StringUtils.concat( "cyk launcherModel loadworkspace isAppInstalledSdcard: " , isAppInstalledSdcard ) );
											//cheyingkun add end
											if( !mAppsCanBeOnRemoveableStorage//
													//应用没安装在t卡
													|| !isAppInstalledSdcard//cheyingkun add	//解决“单层桌面插sim卡开机，概率性sim卡工具图标丢失”的问题【c_0004504】
											)
											{
												// Log the invalid package, and remove it from the db
												Launcher.addDumpLog( TAG , StringUtils.concat( "Invalid package removed: " , cn.toString() ) , true );
												itemsToRemove.add( id );
											}
											else
											{
												// If apps can be on external storage, then we just
												// leave them for the user to remove (maybe add
												// visual treatment to it)
												Launcher.addDumpLog( TAG , StringUtils.concat( "Invalid package found: " , cn.toString() ) , true );
											}
											//cheyingkun add start//解决“分类前，拖动两个应用生成文件夹。分类后卸载之前的两个应用。恢复之前布局，空文件夹未删除。”的问题【i_0011018】
											container = c.getInt( containerIndex );
											if( container != LauncherSettings.Favorites.CONTAINER_DESKTOP && container != LauncherSettings.Favorites.CONTAINER_HOTSEAT )
											{
												if( !itemsRemoveInFolder.contains( container ) )
												{
													itemsRemoveInFolder.add( container );
												}
											}
											//cheyingkun add end
											continue;
										}
									}
									catch( URISyntaxException e )
									{
										//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
										if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
										{
											Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcherModel loadWorkspace: URISyntaxException Invalid uri: " , intentDescription ) );
										}
										//cheyingkun add end
										Launcher.addDumpLog( TAG , StringUtils.concat( "Invalid uri: " , intentDescription ) , true );
										continue;
									}
									if( itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION )
									{
										//cheyingkun add start	//RestartMobileTCardAppDismiss(如果数据库中查询的信息包含在文件中,不能调用方法获取ShortcutInfo)bug:0009466
										if( mountInfoContainsIntent )
										{
											if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
												Log.d( TAG , StringUtils.concat( "intent : " , intent.toUri( 0 ) ) );
											info = new ShortcutInfo();
											info.setTitle( c.getString( titleIndex ) );
											info.setItemType( LauncherSettings.Favorites.ITEM_TYPE_APPLICATION );
											//cheyingkun add start	//解决“重复安装时，T卡应用变得无法卸载”的问题。【i_0011422】
											ComponentName componentName = intent.getComponent();
											PackageInfo pi;
											try
											{
												pi = manager.getPackageInfo( componentName.getPackageName() , 0 );
											}
											catch( Exception e )
											{
												pi = null;
												Launcher.addDumpLog( TAG , StringUtils.concat( "manager.getPackageInfo: " , e.toString() ) , true );
											}
											info.initFlagsAndFirstInstallTime( pi );
											//cheyingkun add end
										}
										else
										{
											//cheyingkun add end
											//<数据库字段更新> liuhailin@2015-03-24 modify begin
											//info = getShortcutInfo( manager , intent , context , c , iconIndex , titleIndex , mLabelCache );
											info = getShortcutInfo( manager , intent , context , c , iconIndex , titleIndex , mLabelCache , operateIntent );
											//<数据库字段更新> liuhailin@2015-03-24 modify end
										}
									}
									else
									{
										//添加智能分类功能 , change by shlt@2015/02/10 UPD START
										//info = getShortcutInfo( c , context , iconTypeIndex , iconPackageIndex , iconResourceIndex , iconIndex , titleIndex );
										//<数据库字段更新> liuhailin@2015-03-24 modify begin
										//info = getShortcutInfo( c , context , iconTypeIndex , iconPackageIndex , iconResourceIndex , iconIndex , titleIndex , intent );
										//xiatian start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
										//										info = getShortcutInfo( c , context , iconTypeIndex , iconPackageIndex , iconResourceIndex , iconIndex , titleIndex , intent , operateIntent );//xiatian del
										info = getShortcutInfo( c , context , itemType , iconTypeIndex , iconPackageIndex , iconResourceIndex , iconIndex , titleIndex , intent , operateIntent );//xiatian add
										//xiatian end
										//<数据库字段更新> liuhailin@2015-03-24 modify end
										//添加智能分类功能 , change by shlt@2015/02/10 UPD END
										// App shortcuts that used to be automatically added to Launcher
										// didn't always have the correct intent flags set, so do that
										// here
										if( intent.getAction() != null && intent.getCategories() != null && intent.getAction().equals( Intent.ACTION_MAIN ) && intent.getCategories().contains(
												Intent.CATEGORY_LAUNCHER ) )
										{
											intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
										}
									}
									if( info != null )
									{
										info.setId( id );
										//添加智能分类功能 , change by shlt@2015/02/10 UPD START
										//info.getIntent() = intent;
										info.setIntent( intent );
										//<数据库字段更新> liuhailin@2015-03-24 modify begin
										//										if( operateIntent != null )
										//										{
										//											info.setOperateIntent( operateIntent );
										//										}
										//<数据库字段更新> liuhailin@2015-03-24 modify end
										//添加智能分类功能 , change by shlt@2015/02/10 UPD END
										container = c.getInt( containerIndex );
										info.setContainer( container );
										//<数据库字段更新> liuhailin@2015-03-23 modify begin
										info.setDefaultWorkspaceItemType( c.getInt( defWorkspaceItemTypeIndex ) );
										//<数据库字段更新> liuhailin@2015-03-23 modify end
										info.setScreenId( c.getInt( screenIndex ) );
										info.setCellX( c.getInt( cellXIndex ) );
										info.setCellY( c.getInt( cellYIndex ) );
										info.setSpanX( 1 );
										info.setSpanY( 1 );
										//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
										if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
										{
											Log.d( "cyk_bug : c_0003400" , "cyk launcherModel loadWorkspace: info " + info );
										}
										//cheyingkun add end
										// Skip loading items that are out of bounds
										if( container == LauncherSettings.Favorites.CONTAINER_DESKTOP )
										{
											if( checkItemDimensions( info ) )
											{
												Launcher.addDumpLog( TAG , StringUtils.concat(
														"Skipped loading out of bounds shortcut: " + info ,
														"-NumColumns:" ,
														grid.getNumColumns() ,
														"-NumRows:" ,
														grid.getNumRows() ) , true );
												itemsToRemove.add( id );//如果不删除，桌面将不显示。
												continue;
											}
										}
										// check & update map of what's occupied
										deleteOnItemOverlap.set( false );
										if( !checkItemPlacement( occupied , info , deleteOnItemOverlap ) )
										{
											if( deleteOnItemOverlap.get() )
											{
												itemsToRemove.add( id );
												//cheyingkun add start//解决“分类前，拖动两个应用生成文件夹。分类后卸载之前的两个应用。恢复之前布局，空文件夹未删除。”的问题【i_0011018】
												if( container != LauncherSettings.Favorites.CONTAINER_DESKTOP && container != LauncherSettings.Favorites.CONTAINER_HOTSEAT )
												{
													if( !itemsRemoveInFolder.contains( container ) )
													{
														itemsRemoveInFolder.add( container );
													}
												}
												//cheyingkun add end
											}
											break;
										}
										//cheyingkun add start	//TCardMount(如果应用在挂载信息中,设置不可用并设置icon)
										if( mountInfoContainsIntent )
										{
											//cheyingkun start	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
											//											info.setAvailable( false );//设置不可用//cheyingkun del
											info.setAvailable( sdCardExist );//cheyingkun add(根据sd卡状态设置可用不可用)
											//cheyingkun end
											if( mountInfoMap != null )
											{
												//cheyingkun start	//修改T卡挂载逻辑,保存图标从以前的灰色图标变为保存正常的图标
												//												info.setIconUnavailable( mountInfoMap.get( intent ) );//cheyingkun del
												info.setIconUnavailable( Tools.getGrayBitmap( mountInfoMap.get( intent ) ) );//cheyingkun add
												//cheyingkun end
											}
										}
										//cheyingkun add end
										switch( container )
										{
											case LauncherSettings.Favorites.CONTAINER_DESKTOP:
											case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
												sBgWorkspaceItems.add( info );
												break;
											default:
												// Item is in a user folder
												FolderInfo folderInfo = findOrMakeFolder( sBgFolders , container );
												folderInfo.add( info );
												break;
										}
										sBgItemsIdMap.put( info.getId() , info );
										// now that we've loaded everthing re-save it with the
										// icon in case it disappears somehow.
										queueIconToBeChecked( sBgDbIconCache , info , c , iconIndex );
									}
									else
									{
										//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
										if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
										{
											Log.d( "cyk_bug : c_0003400" , "cyk launcherModel loadWorkspace: RuntimeException:　Unexpected null ShortcutInfo" );
										}
										//cheyingkun add end
										throw new RuntimeException( "Unexpected null ShortcutInfo" );
									}
									break;
								case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
									id = c.getLong( idIndex );
									FolderInfo folderInfo = findOrMakeFolder( sBgFolders , id );
									String title = c.getString( titleIndex );
									//xiatian add start	//桌面加载时，当从数据库读到的文件夹的名字为文件夹默认名称并且当前语言和默认名称不匹配时，重新读取文件夹默认名称。
									Locale locale = context.getResources().getConfiguration().locale;
									String language = locale.getLanguage();
									if( !TextUtils.isEmpty( title ) )
									{
										if( //
										( !language.equals( "zh-rCN" ) && ( title.equals( "文件夹" ) ) ) //
												|| ( !( language.equals( "zh-rHK" ) || language.equals( "zh-rTW" ) ) && title.equals( "文件夾" ) )//
												|| ( !language.contains( "zh" ) && ( title.equals( "文件夹" ) || title.equals( "文件夾" ) ) )//
												|| ( language.contains( "zh" ) && title.equals( "Folder" ) )//
										//
										)
											title = LauncherDefaultConfig.getString( R.string.folder_name );
									}
									//xiatian add end
									//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
									if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									{
										Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcherModel loadWorkspace: FolderInfo:　" + folderInfo , " title " , title ) );
									}
									//cheyingkun add end
									folderInfo.setTitle( title );
									folderInfo.setId( id );
									container = c.getInt( containerIndex );
									//<数据库字段更新> liuhailin@2015-03-23 modify begin
									folderInfo.setDefaultWorkspaceItemType( c.getInt( defWorkspaceItemTypeIndex ) );
									//<数据库字段更新> liuhailin@2015-03-23 modify end
									folderInfo.setContainer( container );
									folderInfo.setScreenId( c.getInt( screenIndex ) );
									folderInfo.setCellX( c.getInt( cellXIndex ) );
									folderInfo.setCellY( c.getInt( cellYIndex ) );
									folderInfo.setSpanX( 1 );
									folderInfo.setSpanY( 1 );
									//添加智能分类功能 , change by shlt@2015/02/10 ADD START
									//<数据库字段更新> liuhailin@2015-03-24 modify begin
									//intentDescription = c.getString( intentIndex );
									//folderInfo.setIntent( Intent.parseUri( intentDescription , 0 ) );									
									//xiatian add start	//fix bug：解决“点击所有‘更多应用’图标（智能分类文件夹和非智能分类文件夹中）后，全部都进入‘装机必备’界面”的问题。
									if( operateIntent != null )
									{
										folderInfo.setOperateIntent( operateIntent );
									}
									//xiatian add end
									//<数据库字段更新> liuhailin@2015-03-24 modify end
									//添加智能分类功能 , change by shlt@2015/02/10 ADD END
									// Skip loading items that are out of bounds
									if( container == LauncherSettings.Favorites.CONTAINER_DESKTOP )
									{
										if( checkItemDimensions( folderInfo ) )
										{
											if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
												Log.d( TAG , "Skipped loading out of bounds folder" );
											continue;
										}
									}
									// check & update map of what's occupied
									deleteOnItemOverlap.set( false );
									if( !checkItemPlacement( occupied , folderInfo , deleteOnItemOverlap ) )
									{
										if( deleteOnItemOverlap.get() )
										{
											itemsToRemove.add( id );
										}
										//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
										if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
										{
											Log.d( "cyk_bug : c_0003400" , "cyk launcherModel loadWorkspace:  !checkItemPlacement( occupied , folderInfo , deleteOnItemOverlap ) " );
										}
										//cheyingkun add end
										break;
									}
									switch( container )
									{
										case LauncherSettings.Favorites.CONTAINER_DESKTOP:
										case LauncherSettings.Favorites.CONTAINER_HOTSEAT:
											sBgWorkspaceItems.add( folderInfo );
											break;
									}
									sBgItemsIdMap.put( folderInfo.getId() , folderInfo );
									sBgFolders.put( folderInfo.getId() , folderInfo );
									break;
								case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
									// Read all Launcher-specific widget details
									int appWidgetId = c.getInt( appWidgetIdIndex );
									String savedProvider = c.getString( appWidgetProviderIndex );
									id = c.getLong( idIndex );
									final AppWidgetProviderInfo provider = widgets.getAppWidgetInfo( appWidgetId );
									if( !isSafeMode && ( provider == null || provider.provider == null || provider.provider.getPackageName() == null ) )
									{
										String log = StringUtils.concat( "Deleting widget that isn't installed anymore: id=" , id , " appWidgetId=" , appWidgetId );
										if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
											Log.e( TAG , log );
										Launcher.addDumpLog( TAG , log , false );
										itemsToRemove.add( id );
									}
									else
									{
										//luomingjun add start	//添加时钟插件	
										//【备注：】
										//1、这部分代码放在桌面的加载流程中，会导致时钟插件放在在其他桌面后，其他桌面重新启动后，时钟插件秒针不跳动！
										//2、为啥要放在这里，不能放在插件的AppWidgetProvider的onEnabled方法中吗？TimeAppWidgetProvider的onEnabled已经有同样代码，这段代码是否还要保留？
										if(
										//
										( provider.provider.getClassName() != null )//
												&& provider.provider.getClassName().equals( "com.cooee.phenix.widget.timer.TimeAppWidgetProvider" ) //
												&& provider.provider.getPackageName().equals( mContext.getPackageName() )
										//
										)
										{
											if( TimeAppWidgetProvider.stpe == null )
											{
												TimeAppWidgetProvider.stpe = new ScheduledThreadPoolExecutor( 1 );
												TimeUpdateTask timer = new TimeUpdateTask( context );
												TimeAppWidgetProvider.stpe.scheduleWithFixedDelay( timer , 1 , 1 , TimeUnit.SECONDS );
											}
										}
										//luomingjun add end
										appWidgetInfo = new LauncherAppWidgetInfo( appWidgetId , provider.provider );
										appWidgetInfo.setId( id );
										appWidgetInfo.setScreenId( c.getInt( screenIndex ) );
										appWidgetInfo.setCellX( c.getInt( cellXIndex ) );
										appWidgetInfo.setCellY( c.getInt( cellYIndex ) );
										appWidgetInfo.setSpanX( c.getInt( spanXIndex ) );
										appWidgetInfo.setSpanY( c.getInt( spanYIndex ) );
										int minSpanX = -1;
										int minSpanY = -1;
										//chenliang del start	//解决“桌面重启后，拖拽特定尺寸大小的插件到下一页或者拖拽到屏幕最右侧有白线出现时，松手后导致桌面重启”的问题。【i_0015035】(将setMinSpanX,Y的流程放在ShortcutAndWidgetContainer的measureChild方法中)
										//										int[] minSpan = Launcher.getMinSpanForWidget( context , provider );
										//										minSpanX = minSpan[0];
										//										minSpanY = minSpan[1];
										//chenliang del end
										appWidgetInfo.setMinSpanX( minSpanX );
										appWidgetInfo.setMinSpanY( minSpanY );
										container = c.getInt( containerIndex );
										if( container != LauncherSettings.Favorites.CONTAINER_DESKTOP && container != LauncherSettings.Favorites.CONTAINER_HOTSEAT )
										{
											if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
												Log.e( TAG , "Widget found where [container != CONTAINER_DESKTOP nor CONTAINER_HOTSEAT] - ignoring!" );
											continue;
										}
										appWidgetInfo.setContainer( c.getInt( containerIndex ) );
										// Skip loading items that are out of bounds
										if( container == LauncherSettings.Favorites.CONTAINER_DESKTOP )
										{
											if( checkItemDimensions( appWidgetInfo ) )
											{
												if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
													Log.d( TAG , "Skipped loading out of bounds app widget" );
												continue;
											}
										}
										//<数据库字段更新> liuhailin@2015-03-23 modify begin
										appWidgetInfo.setDefaultWorkspaceItemType( c.getInt( defWorkspaceItemTypeIndex ) );
										//<数据库字段更新> liuhailin@2015-03-23 modify end
										// check & update map of what's occupied
										deleteOnItemOverlap.set( false );
										if( !checkItemPlacement( occupied , appWidgetInfo , deleteOnItemOverlap ) )
										{
											if( deleteOnItemOverlap.get() )
											{
												itemsToRemove.add( id );
											}
											break;
										}
										String providerName = provider.provider.flattenToString();
										if( !providerName.equals( savedProvider ) )
										{
											ContentValues values = new ContentValues();
											values.put( LauncherSettings.Favorites.APPWIDGET_PROVIDER , providerName );
											String where = StringUtils.concat( BaseColumns._ID , "= ?" );
											String[] args = { Integer.toString( c.getInt( idIndex ) ) };
											contentResolver.update( contentUri , values , where , args );
										}
										sBgItemsIdMap.put( appWidgetInfo.getId() , appWidgetInfo );
										sBgAppWidgets.add( appWidgetInfo );
									}
									break;
							}
						}
						catch( Exception e )
						{
							//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							{
								Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcherModel loadWorkspace:  Desktop items loading interrupted:  " , e.toString() ) );
							}
							//cheyingkun add end
							Launcher.addDumpLog( TAG , StringUtils.concat( "Desktop items loading interrupted 3290 : " , e.toString() ) , true );
							e.printStackTrace();
						}
					}
				}
				finally
				{
					if( c != null )
					{
						c.close();
					}
				}
				// Break early if we've stopped loading
				if( mStopped )
				{
					clearSBgDataStructures();
					return false;
				}
				if( itemsToRemove.size() > 0 )
				{
					ContentProviderClient client = contentResolver.acquireContentProviderClient( LauncherSettings.Favorites.CONTENT_URI );
					// Remove dead items
					for( long id : itemsToRemove )
					{
						if( DEBUG_LOADERS )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG , StringUtils.concat( "Removed id = " , id ) );
						}
						// Don't notify content observers
						try
						{
							client.delete( LauncherSettings.Favorites.getContentUri( id , false ) , null , null );
						}
						catch( RemoteException e )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.w( TAG , StringUtils.concat( "Could not remove id = " , id ) );
						}
					}
				}
				removeEmptyFolderOnLoadWorkspace( itemsRemoveInFolder );//cheyingkun add	//解决“分类前，拖动两个应用生成文件夹。分类后卸载之前的两个应用。恢复之前布局，空文件夹未删除。”的问题【i_0011018】
				//cheyingkun add start	//配置可以通过广播删除的快捷方式【智科】【c_0004445】(删除的图标在文件夹里)
				removeEmptyFolderOnLoadWorkspace( UninstallShortcutReceiver.getRemoveItemFolderIdList() );
				UninstallShortcutReceiver.getRemoveItemFolderIdList().clear();
				//cheyingkun add end
				//cheyingkun add start	//为bug:c_0003819添加log，（打开配置项switch_enable_debug启动）。【c_0003819】
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( "cyk_bug : c_0003819" , StringUtils.concat( "cyk launcherModel loadWorkspace: loadedOldDb: " , loadedOldDb ) );
				}
				//cheyingkun add end
				if( loadedOldDb )
				{
					initWorkspaceScreensByItems( context );//cheyingkun add	//解决“loadedOldDb判断异常时，默认配置的图标不显示”的问题。【c_0003093】
				}
				else
				{
					initWorkspaceScreensByDB( context );//cheyingkun add	//解决“loadedOldDb判断异常时，默认配置的图标不显示”的问题。【c_0003093】
				}
				//cheyingkun add start	//为bug:c_0003819添加log，（打开配置项switch_enable_debug启动）。【c_0003819】
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( "cyk_bug : c_0003819" , StringUtils.concat( "cyk launcherModel loadWorkspace: sBgWorkspaceScreens: " , sBgWorkspaceScreens.toArray() ) );
				}
				//cheyingkun add end
				if( DEBUG_LOADERS )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.d( TAG , StringUtils.concat( "loaded workspace in " , ( SystemClock.uptimeMillis() - t ) , "ms" ) );
						Log.d( TAG , "workspace layout: " );
						int nScreens = occupied.size();
						for( int y = 0 ; y < countY ; y++ )
						{
							String line = "";
							Iterator<Long> iter = occupied.keySet().iterator();
							while( iter.hasNext() )
							{
								long screenId = iter.next();
								if( screenId > 0 )
								{
									line = StringUtils.concat( line , " | " );
								}
								for( int x = 0 ; x < countX ; x++ )
								{
									line = StringUtils.concat( line , ( ( occupied.get( screenId )[x][y] != null ) ? "#" : "." ) );
								}
							}
							Log.d( TAG , StringUtils.concat( "[ " , line , " ]" ) );
						}
					}
				}
			}
			return loadedOldDb;
		}
		
		//cheyingkun add start	//解决“loadedOldDb判断异常时，默认配置的图标不显示”的问题。【c_0003093】
		/**
		 * 根据配置的item初始化sBgWorkspaceScreens
		 * @param context
		 */
		private void initWorkspaceScreensByItems(
				Context context )
		{
			if( DEBUG_LOADERS )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "initWorkspaceScreensByItems. sBgItemsIdMap.size : " , sBgItemsIdMap.size() ) );
			}
			long maxScreenId = 0;
			// If we're importing we use the old screen order.
			for( ItemInfo item : sBgItemsIdMap.values() )
			{
				long screenId = item.getScreenId();
				if( item.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP && !sBgWorkspaceScreens.contains( screenId ) )
				{
					sBgWorkspaceScreens.add( screenId );
					if( screenId > maxScreenId )
					{
						maxScreenId = screenId;
					}
				}
			}
			//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
			if( LauncherDefaultConfig.isAllowEmptyScreen() )
			{
				int array[] = null;
				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
				{
					array = LauncherDefaultConfig.mConfigEmptyScreenIdArrayInDrawer;
				}
				//zhujieping add start //添加配置项“config_empty_screen_id_in_core”，单层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
				else if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
				{
					array = LauncherDefaultConfig.mConfigEmptyScreenIdArrayInCore;
				}
				//zhujieping add end
				for( long screenId : array )
				{
					if( !sBgWorkspaceScreens.contains( screenId ) )
					{
						sBgWorkspaceScreens.add( screenId );
						if( screenId > maxScreenId )
						{
							maxScreenId = screenId;
						}
					}
				}
				//zhujieping add start //添加配置项“config_empty_screen_id_in_core”，单层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
				{
					for( long i = 1 ; i <= maxScreenId ; i++ )
					{
						if( sBgWorkspaceScreens.contains( i ) == false )
							sBgWorkspaceScreens.add( i );
					}
				}
				//zhujieping add end
			}
			//zhujieping add end
			if( sBgWorkspaceScreens.size() == 0 )
			{
				long screenId = LauncherAppState.getLauncherProvider().generateNewScreenId();
				sBgWorkspaceScreens.add( screenId );
				maxScreenId = screenId;
			}
			Collections.sort( sBgWorkspaceScreens );
			LauncherAppState.getLauncherProvider().updateMaxScreenId( maxScreenId );
			updateWorkspaceScreenOrder( context , sBgWorkspaceScreens );
			// Update the max item id after we load an old db
			long maxItemId = 0;
			// If we're importing we use the old screen order.
			for( ItemInfo item : sBgItemsIdMap.values() )
			{
				maxItemId = Math.max( maxItemId , item.getId() );
			}
			LauncherAppState.getLauncherProvider().updateMaxItemId( maxItemId );
			//cheyingkun add start	//为bug:c_0003819添加log，（打开配置项switch_enable_debug启动）。【c_0003819】
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.d( "cyk_bug : c_0003819" , StringUtils.concat( "cyk launcherModel initWorkspaceScreensByItems: sBgWorkspaceScreens: " , sBgWorkspaceScreens.toArray() ) );
			}
			//cheyingkun add end
		}
		
		/**
		 * 根据数据库初始化sBgWorkspaceScreens
		 * @param context
		 */
		private void initWorkspaceScreensByDB(
				Context context )
		{
			TreeMap<Integer , Long> orderedScreens = loadWorkspaceScreensDb( mContext );
			if( DEBUG_LOADERS )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( TAG , StringUtils.concat( "initWorkspaceScreensByDB - orderedScreens.size : " , orderedScreens.size() ) );
				}
			}
			if( orderedScreens.size() > 0 )
			{
				for( Integer i : orderedScreens.keySet() )
				{
					sBgWorkspaceScreens.add( orderedScreens.get( i ) );
				}
				// Remove any empty screens
				ArrayList<Long> unusedScreens = new ArrayList<Long>( sBgWorkspaceScreens );
				for( ItemInfo item : sBgItemsIdMap.values() )
				{
					long screenId = item.getScreenId();
					if( item.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP )
					{
						if( unusedScreens.contains( screenId ) )
						{
							unusedScreens.remove( screenId );
						}
						//cheyingkun add start	//为bug:c_0003819添加log，（打开配置项switch_enable_debug启动）。【c_0003819】
						//【问题原因】桌面第一次启动时，addAndBindAddedItems方法的updateWorkspaceScreenOrder前被系统杀死。导致favorite_core正常但是workspaceScreen_core异常。再次加载桌面显示异常
						//【解决方案】如果sBgItemsIdMap存在sBgWorkspaceScreens中没有的screenId,那么清空sBgWorkspaceScreens,重新根据item初始化桌面数
						if( !sBgWorkspaceScreens.contains( screenId ) )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							{
								Log.e( "cyk_bug : c_0003819" , StringUtils.concat(
										"cyk launcherModel loadWorkspace initWorkspaceScreensByDB - sBgItemsIdMap.size:" ,
										sBgItemsIdMap.size() ,
										"-sBgWorkspaceScreens:" ,
										sBgWorkspaceScreens.toArray() ) );
							}
							sBgWorkspaceScreens.clear();
							initWorkspaceScreensByItems( context );
							return;
						}
						//cheyingkun add end
					}
				}
				// If there are any empty screens remove them, and update.
				if( !LauncherDefaultConfig.isAllowEmptyScreen() )//zhujieping add //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
					if( unusedScreens.size() != 0 )
					{
						sBgWorkspaceScreens.removeAll( unusedScreens );
						updateWorkspaceScreenOrder( context , sBgWorkspaceScreens );
					}
			}
			else
			{
				//【问题原因】第一次加载被中断，再次加载时loadedOldDb判断为false，并且从数据库读不到页面数。导致默认配置的时钟插件不显示
				//【解决方案】在非首次加载时，如果从数据库中读不到页面数，就根据item重新初始化页面数
				initWorkspaceScreensByItems( context );
			}
			//cheyingkun add start	//为bug:c_0003819添加log，（打开配置项switch_enable_debug启动）。【c_0003819】
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.d( "cyk_bug : c_0003819" , StringUtils.concat( "cyk launcherModel initWorkspaceScreensByDB: sBgWorkspaceScreens: " , sBgWorkspaceScreens.toArray() ) );
			}
			//cheyingkun add end
		}
		
		//cheyingkun add end
		/** Filters the set of items who are directly or indirectly (via another container) on the
		 * specified screen. */
		private void filterCurrentWorkspaceItems(
				int currentScreen ,
				ArrayList<ItemInfo> allWorkspaceItems ,
				ArrayList<ItemInfo> currentScreenItems ,
				ArrayList<ItemInfo> otherScreenItems )
		{
			// Purge any null ItemInfos
			Iterator<ItemInfo> iter = allWorkspaceItems.iterator();
			while( iter.hasNext() )
			{
				ItemInfo i = iter.next();
				if( i == null )
				{
					iter.remove();
				}
			}
			// If we aren't filtering on a screen, then the set of items to load is the full set of
			// items given.
			if( currentScreen < 0 )
			{
				currentScreenItems.addAll( allWorkspaceItems );
			}
			// Order the set of items by their containers first, this allows use to walk through the
			// list sequentially, build up a list of containers that are in the specified screen,
			// as well as all items in those containers.
			Set<Long> itemsOnScreen = new HashSet<Long>();
			Collections.sort( allWorkspaceItems , new Comparator<ItemInfo>() {
				
				@Override
				public int compare(
						ItemInfo lhs ,
						ItemInfo rhs )
				{
					if( !( lhs instanceof ItemInfo && rhs instanceof ItemInfo ) )
					{
						return 0;
					}
					return (int)( lhs.getContainer() - rhs.getContainer() );
				}
			} );
			for( ItemInfo info : allWorkspaceItems )
			{
				if( info.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP )
				{
					if( info.getScreenId() == currentScreen )
					{
						currentScreenItems.add( info );
						itemsOnScreen.add( info.getId() );
					}
					else
					{
						otherScreenItems.add( info );
					}
				}
				else if( info.getContainer() == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
				{
					currentScreenItems.add( info );
					itemsOnScreen.add( info.getId() );
				}
				else
				{
					if( itemsOnScreen.contains( info.getContainer() ) )
					{
						currentScreenItems.add( info );
						itemsOnScreen.add( info.getId() );
					}
					else
					{
						otherScreenItems.add( info );
					}
				}
			}
		}
		
		/** Filters the set of widgets which are on the specified screen. */
		private void filterCurrentAppWidgets(
				int currentScreen ,
				ArrayList<LauncherAppWidgetInfo> appWidgets ,
				ArrayList<LauncherAppWidgetInfo> currentScreenWidgets ,
				ArrayList<LauncherAppWidgetInfo> otherScreenWidgets )
		{
			// If we aren't filtering on a screen, then the set of items to load is the full set of
			// widgets given.
			if( currentScreen < 0 )
			{
				currentScreenWidgets.addAll( appWidgets );
			}
			for( LauncherAppWidgetInfo widget : appWidgets )
			{
				if( widget == null )
					continue;
				if( widget.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP && widget.getScreenId() == currentScreen )
				{
					currentScreenWidgets.add( widget );
				}
				else
				{
					otherScreenWidgets.add( widget );
				}
			}
		}
		
		/** Filters the set of folders which are on the specified screen. */
		private void filterCurrentFolders(
				int currentScreen ,
				HashMap<Long , ItemInfo> itemsIdMap ,
				HashMap<Long , FolderInfo> folders ,
				HashMap<Long , FolderInfo> currentScreenFolders ,
				HashMap<Long , FolderInfo> otherScreenFolders )
		{
			// If we aren't filtering on a screen, then the set of items to load is the full set of
			// widgets given.
			if( currentScreen < 0 )
			{
				currentScreenFolders.putAll( folders );
			}
			for( long id : folders.keySet() )
			{
				ItemInfo info = itemsIdMap.get( id );
				FolderInfo folder = folders.get( id );
				if( info == null || folder == null )
					continue;
				if( info.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP && info.getScreenId() == currentScreen )
				{
					currentScreenFolders.put( id , folder );
				}
				else
				{
					otherScreenFolders.put( id , folder );
				}
			}
		}
		
		/** Sorts the set of items by hotseat, workspace (spatially from top to bottom, left to
		 * right) */
		private void sortWorkspaceItemsSpatially(
				ArrayList<ItemInfo> workspaceItems )
		{
			final LauncherAppState app = LauncherAppState.getInstance();
			final DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
			// XXX: review this
			Collections.sort( workspaceItems , new Comparator<ItemInfo>() {
				
				@Override
				public int compare(
						ItemInfo lhs ,
						ItemInfo rhs )
				{
					if( !( lhs instanceof ItemInfo && rhs instanceof ItemInfo ) )
					{
						return 0;
					}
					int cellCountX = (int)grid.getNumColumns();
					int cellCountY = (int)grid.getNumRows();
					int screenOffset = cellCountX * cellCountY;
					int containerOffset = screenOffset * ( Launcher.SCREEN_COUNT + 1 ); // +1 hotseat
					long lr = ( lhs.getContainer() * containerOffset + lhs.getScreenId() * screenOffset + lhs.getCellY() * cellCountX + lhs.getCellX() );
					long rr = ( rhs.getContainer() * containerOffset + rhs.getScreenId() * screenOffset + rhs.getCellY() * cellCountX + rhs.getCellX() );
					return (int)( lr - rr );
				}
			} );
		}
		
		private void bindWorkspaceScreens(
				final Callbacks oldCallbacks ,
				final ArrayList<Long> orderedScreens )
		{
			final Runnable r = new Runnable() {
				
				@Override
				public void run()
				{
					Callbacks callbacks = tryGetCallbacks( oldCallbacks );
					if( callbacks != null )
					{
						callbacks.bindScreens( orderedScreens );
					}
				}
			};
			runOnMainThread( r , MAIN_THREAD_BINDING_RUNNABLE );
		}
		
		private void bindWorkspaceItems(
				final Callbacks oldCallbacks ,
				final ArrayList<ItemInfo> workspaceItems ,
				final ArrayList<LauncherAppWidgetInfo> appWidgets ,
				final HashMap<Long , FolderInfo> folders ,
				ArrayList<Runnable> deferredBindRunnables ,
				final boolean isFinishBinding ,
				final boolean isUpgradePath )
		{
			final boolean postOnMainThread = ( deferredBindRunnables != null );
			// Bind the workspace items
			//zhujieping，将这个方法中的所有需要在主线程中执行的按照原先顺序放到一个runnable中，new多个runnable，执行完一个再执行下一个时，这中间有几十ms的浪费
			final Runnable r = new Runnable() {
				
				@Override
				public void run()
				{
					Callbacks callbacks = tryGetCallbacks( oldCallbacks );
					int N = workspaceItems.size();
					for( int i = 0 ; i < N ; i += ITEMS_CHUNK )
					{
						final int start = i;
						final int chunkSize = ( i + ITEMS_CHUNK <= N ) ? ITEMS_CHUNK : ( N - i );
						if( callbacks != null )
						{
							callbacks.bindItems( workspaceItems , start , start + chunkSize , false , null , !mIsLoaderTaskRunning );
						}
					}
					if( !folders.isEmpty() )
					{
						if( callbacks != null )
						{
							callbacks.bindFolders( folders );
						}
					}
					N = appWidgets.size();
					for( int i = 0 ; i < N ; i++ )
					{
						final LauncherAppWidgetInfo widget = appWidgets.get( i );
						if( callbacks != null )
						{
							callbacks.bindAppWidget( widget );
						}
					}
					if( isFinishBinding )
					{
						if( callbacks != null )
						{
							callbacks.finishBindingItems( isUpgradePath );
						}
						// If we're profiling, ensure this is the last thing in the queue.
						mIsLoadingAndBindingWorkspace = false;
					}
				}
			};
			if( postOnMainThread )
			{
				deferredBindRunnables.add( r );
			}
			else
			{
				runOnMainThread( r , MAIN_THREAD_BINDING_RUNNABLE );
			}
		}
		
		//xiatian add start	//桌面默认主页的样式（详见BaseDefaultConfig.java中的“DEFAULT_PAGE_STYLE_XXX”）。
		private void bindDefaultPage(
				final Callbacks oldCallbacks )
		{
			if( oldCallbacks == null )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "bindDefaultPage - LoaderTask running with no launcher" );
				return;
			}
			final Runnable r = new Runnable() {
				
				@Override
				public void run()
				{
					Callbacks callbacks = tryGetCallbacks( oldCallbacks );
					if( callbacks != null )
					{
						callbacks.bindDefaultPage( callbacks );
					}
				}
			};
			runOnMainThread( r , MAIN_THREAD_BINDING_RUNNABLE );
		}
		//xiatian add end
		;
		
		/**
		 * Binds all loaded data to actual views on the main thread.
		 */
		private void bindWorkspace(
				int synchronizeBindPage ,
				final boolean isUpgradePath )
		{
			final long t = SystemClock.uptimeMillis();
			Runnable r;
			// Don't use these two variables in any of the callback runnables.
			// Otherwise we hold a reference to them.
			final Callbacks oldCallbacks = mCallbacks.get();
			if( oldCallbacks == null )
			{
				// This launcher has exited and nobody bothered to tell us.  Just bail.
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "LoaderTask running with no launcher" );
				return;
			}
			final boolean isLoadingSynchronously = ( synchronizeBindPage > -1 );
			final int currentScreen = isLoadingSynchronously ? synchronizeBindPage : oldCallbacks.getCurrentWorkspaceScreen();
			// Load all the items that are on the current page first (and in the process, unbind
			// all the existing workspace items before we call startBinding() below.
			unbindWorkspaceItemsOnMainThread();
			ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
			ArrayList<LauncherAppWidgetInfo> appWidgets = new ArrayList<LauncherAppWidgetInfo>();
			HashMap<Long , FolderInfo> folders = new HashMap<Long , FolderInfo>();
			HashMap<Long , ItemInfo> itemsIdMap = new HashMap<Long , ItemInfo>();
			ArrayList<Long> orderedScreenIds = new ArrayList<Long>();
			//gaominghui add start //解决"当主页面上只有小组件，智能分类后，该页面空白"的问题【i_0014888】。
			boolean isFirst = LauncherAppState.getLauncherProvider().isFirst();//判断是否为第一次加载
			if( isFirst )
			{
				//gaominghui add end
				//xiatian add start	//解决“加载桌面时，先显示第一页，再跳到默认主页”的问题（由“加载item完毕之后，再跳到默认主页”改为“在加载item之前，直接跳到默认主页”）。【c_0004499】
				//获取默认主页index
				int mDefaultScreenId = oldCallbacks.getDefaultPageFromSharedPreferences() + 1;//screenid是从1开始的，index是从0开始的
				//添加默认主页那个页面
				for( long i = 1 ; i <= mDefaultScreenId ; i++ )
				{//i从1开始，桌面页面数是从1开始的。
					if( sBgWorkspaceScreens.contains( i ) == false )
						sBgWorkspaceScreens.add( i );
				}
				//xiatian add end
				//gaominghui add start //解决"当主页面上只有小组件，智能分类后，该页面空白"的问题【i_0014888】。
			}
			else
			{
				int mDefaultPageIndex = oldCallbacks.getDefaultPageFromSharedPreferences();
				ArrayList<Long> screenIdList = queryScreenIdList( mDefaultPageIndex );
				for( int i = 0 ; i < screenIdList.size() ; i++ )
				{
					if( sBgWorkspaceScreens.contains( screenIdList.get( i ) ) == false )
						sBgWorkspaceScreens.add( screenIdList.get( i ) );
				}
			}
			//gaominghui add end
			synchronized( sBgLock )
			{
				workspaceItems.addAll( sBgWorkspaceItems );
				appWidgets.addAll( sBgAppWidgets );
				folders.putAll( sBgFolders );
				itemsIdMap.putAll( sBgItemsIdMap );
				orderedScreenIds.addAll( sBgWorkspaceScreens );
			}
			// Tell the workspace that we're about to start binding items
			r = new Runnable() {
				
				public void run()
				{
					Callbacks callbacks = tryGetCallbacks( oldCallbacks );
					if( callbacks != null )
					{
						callbacks.startBinding();
					}
				}
			};
			runOnMainThread( r , MAIN_THREAD_BINDING_RUNNABLE );
			bindWorkspaceScreens( oldCallbacks , orderedScreenIds );
			bindDefaultPage( oldCallbacks );//xiatian add	//解决“加载桌面时，先显示第一页，再跳到默认主页”的问题（由“加载item完毕之后，再跳到默认主页”改为“在加载item之前，直接跳到默认主页”）。【c_0004499】
			//把item分为默认页和其他页面
			ArrayList<ItemInfo> currentWorkspaceItems = new ArrayList<ItemInfo>();
			ArrayList<ItemInfo> otherWorkspaceItems = new ArrayList<ItemInfo>();
			ArrayList<LauncherAppWidgetInfo> currentAppWidgets = new ArrayList<LauncherAppWidgetInfo>();
			ArrayList<LauncherAppWidgetInfo> otherAppWidgets = new ArrayList<LauncherAppWidgetInfo>();
			HashMap<Long , FolderInfo> currentFolders = new HashMap<Long , FolderInfo>();
			HashMap<Long , FolderInfo> otherFolders = new HashMap<Long , FolderInfo>();
			// Separate the items that are on the current screen, and all the other remaining items
			filterCurrentWorkspaceItems( currentScreen , workspaceItems , currentWorkspaceItems , otherWorkspaceItems );
			filterCurrentAppWidgets( currentScreen , appWidgets , currentAppWidgets , otherAppWidgets );
			filterCurrentFolders( currentScreen , itemsIdMap , folders , currentFolders , otherFolders );
			sortWorkspaceItemsSpatially( currentWorkspaceItems );
			sortWorkspaceItemsSpatially( otherWorkspaceItems );
			// Load items on the current page
			bindWorkspaceItems( oldCallbacks , currentWorkspaceItems , currentAppWidgets , currentFolders , null , false , isUpgradePath );
			if( isLoadingSynchronously )
			{
				r = new Runnable() {
					
					public void run()
					{
						Callbacks callbacks = tryGetCallbacks( oldCallbacks );
						if( callbacks != null )
						{
							callbacks.onPageBoundSynchronously( currentScreen );
						}
					}
				};
				runOnMainThread( r , MAIN_THREAD_BINDING_RUNNABLE );
			}
			mIsFinishBindWrokspaceCurrentScreen = true;//cheyingkun add	//解决“由于启动页消失从桌面加载结束提前到当前页bind结束，在开启免责声明并且当前页bind结束桌面没加载结束时,点击免责声明继续使用按钮后,启动页不消失”的问题。
			//cheyingkun add start	//桌面启动页样式（详见“BaseDefaultConfig”中说明）
			//bind完当前页,启动页消失
			mHandler.post( new Runnable() {
				
				public void run()
				{
					final Callbacks oldCallbacks = mCallbacks.get();
					Callbacks callbacks = tryGetCallbacks( oldCallbacks );
					if( callbacks != null )
					{
						callbacks.dismissLoadingPage();
					}
				}
			} );
			//cheyingkun add end
			// Load all the remaining pages (if we are loading synchronously, we want to defer this
			// work until after the first render)
			mDeferredBindRunnables.clear();
			bindWorkspaceItems( oldCallbacks , otherWorkspaceItems , otherAppWidgets , otherFolders , ( isLoadingSynchronously ? mDeferredBindRunnables : null ) , true , isUpgradePath );
			// Tell the workspace that we're done binding items
			//zhujieping del，这段放到上面的bindWorkspaceItems这个方法中合并执行，若新new出一个runnable，会多出几十ms
			//			r = new Runnable() {
			//				
			//				public void run()
			//				{
			//					Callbacks callbacks = tryGetCallbacks( oldCallbacks );
			//					if( callbacks != null )
			//					{
			//						callbacks.finishBindingItems( isUpgradePath );
			//					}
			//					// If we're profiling, ensure this is the last thing in the queue.
			//					if( DEBUG_LOADERS )
			//					{
			//						Log.d( TAG , "bound workspace in " + ( SystemClock.uptimeMillis() - t ) + "ms" );
			//					}
			//					mIsLoadingAndBindingWorkspace = false;
			//				}
			//			};
			//			if( isLoadingSynchronously )
			//			{
			//				mDeferredBindRunnables.add( r );
			//			}
			//			else
			//			{
			//				runOnMainThread( r , MAIN_THREAD_BINDING_RUNNABLE );
			//			}
		}
		
		private void loadAndBindAllApps()
		{
			if( DEBUG_LOADERS )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "loadAndBindAllApps mAllAppsLoaded=" , mAllAppsLoaded ) );
			}
			if( !mAllAppsLoaded )
			{
				loadAllApps();
				synchronized( LoaderTask.this )
				{
					if( mStopped )
					{
						return;
					}
					mAllAppsLoaded = true;
				}
			}
			else
			{
				onlyBindAllApps();
			}
		}
		
		private void onlyBindAllApps()
		{
			final Callbacks oldCallbacks = mCallbacks.get();
			if( oldCallbacks == null )
			{
				// This launcher has exited and nobody bothered to tell us.  Just bail.
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "LoaderTask running with no launcher (onlyBindAllApps)" );
				return;
			}
			// shallow copy
			@SuppressWarnings( "unchecked" )
			final ArrayList<AppInfo> list = (ArrayList<AppInfo>)mBgAllAppsList.data.clone();
			Runnable r = new Runnable() {
				
				public void run()
				{
					final long t = SystemClock.uptimeMillis();
					final Callbacks callbacks = tryGetCallbacks( oldCallbacks );
					if( callbacks != null )
					{
						callbacks.bindAllApplications( list );
					}
					if( DEBUG_LOADERS )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( TAG , StringUtils.concat( "bound all " , list.size() , " apps from cache in " , ( SystemClock.uptimeMillis() - t ) , "ms" ) );
					}
				}
			};
			boolean isRunningOnMainThread = !( sWorkerThread.getThreadId() == Process.myTid() );
			if( isRunningOnMainThread )
			{
				r.run();
			}
			else
			{
				mHandler.post( r );
			}
		}
		
		private void loadAllApps()
		{
			final long loadTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
			final Callbacks oldCallbacks = mCallbacks.get();
			if( oldCallbacks == null )
			{
				// This launcher has exited and nobody bothered to tell us.  Just bail.
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "LoaderTask running with no launcher (loadAllApps)" );
				return;
			}
			final PackageManager packageManager = mContext.getPackageManager();
			final Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
			mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
			// Clear the list of apps
			mBgAllAppsList.clear();
			// Query for the set of apps
			final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
			apps = packageManager.queryIntentActivities( mainIntent , 0 );
			//xiatian add start	//主菜单支持配置显示特定activity。
			if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
			{
				ArrayList<ComponentName> mAppsShowInApplist = LauncherDefaultConfig.getAppsShowInApplist();
				if( mAppsShowInApplist.size() > 0 )
				{
					for( ComponentName mAppShowInApplistComponentName : mAppsShowInApplist )
					{
						Intent mAppShowInApplistIntent = new Intent( Intent.ACTION_MAIN );
						mAppShowInApplistIntent.setComponent( mAppShowInApplistComponentName );
						List<ResolveInfo> mResolveInfoList = packageManager.queryIntentActivities( mAppShowInApplistIntent , 0 );
						if( mResolveInfoList != null || mResolveInfoList.isEmpty() == false )
						{
							if( apps == null )
							{
								apps = mResolveInfoList;
							}
							else
							{
								apps.addAll( mResolveInfoList );
							}
						}
					}
				}
			}
			//xiatian add end
			if( DEBUG_LOADERS )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "queryIntentActivities took " , ( SystemClock.uptimeMillis() - qiaTime ) , "ms , got " , apps.size() , " apps" ) );
			}
			// Fail if we don't have any apps
			if( apps == null || apps.isEmpty() )
			{
				return;
			}
			//			// chenchen add start 
			//取得sim的无卡和有卡的状态 ，无卡时移除Sim卡应用图标
			if( LauncherDefaultConfig.NO_SIMCARD_UNDISPLAY_SIMCARD_APPLICATION )
			{
				hideSimCard( apps );
			}
			// chenchen add end
			//check items of mBgItems that apps not contains of
			//cheyingkun start	//重启手机,在灰色图标状态进入T9搜索,输入内容,桌面异常终止(bug:0009975)
			//			for( ItemInfo info : sBgWorkspaceItems )//cheyingkun del
			//cheyingkun add start
			//遍历sBgFolders列表,把所有灰色图标加到sBgFolderGreyItems中
			ArrayList<ItemInfo> sBgFolderGreyItems = new ArrayList<ItemInfo>();
			Set<Long> keySet = sBgFolders.keySet();
			for( Long id : keySet )
			{
				FolderInfo folderInfo = sBgFolders.get( id );
				if( folderInfo != null )
				{
					ArrayList<ShortcutInfo> contents = folderInfo.getContents();
					for( ShortcutInfo shortcutInfo : contents )
					{
						if( !shortcutInfo.getAvailable() )
						{
							sBgFolderGreyItems.add( shortcutInfo );
						}
					}
				}
			}
			ArrayList<ItemInfo> sBgLauncherItems = new ArrayList<ItemInfo>();
			TCardMountManager mTCardMountManager = TCardMountManager.getInstance( mApp.getContext() );
			sBgLauncherItems.addAll( sBgFolderGreyItems );
			sBgLauncherItems.addAll( sBgWorkspaceItems );
			//循环列表由sBgWorkspaceItems变为sBgWorkspaceItems+sBgFolderGreyItems,因为sBgWorkspaceItems中只包含桌面图标(灰色和正常)
			//sBgFolderGreyItems包含文件夹中的灰色图标
			//目的是把文件夹中的灰色图标加入到mBgAllAppsList.data中.否则重启手机,T9搜索搜不到文件夹中的灰色图标应用
			for( ItemInfo info : sBgLauncherItems )
			//cheyingkun add end
			//cheyingkun end
			{
				if( info.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION )
				{
					ShortcutInfo shortcut = (ShortcutInfo)info;
					// gaominghui@2016/12/06 ADD START 偶现空指针报停，自测
					if( mTCardMountManager != null && !mTCardMountManager.appsContainsOf( shortcut , apps ) )
					// gaominghui@2016/12/06 ADD END 偶现空指针报停，自测
					{
						ResolveInfo rInfo = null;
						//cheyingkun del start	//智能分类后，某些应用图标显示为机器人的问题
						//						rInfo.activityInfo = new ActivityInfo();
						//						rInfo.activityInfo.applicationInfo = new ApplicationInfo();
						//cheyingkun del end
						if( shortcut.getIntent() != null && shortcut.getIntent().getComponent() != null )
						{
							//cheyingkun start	//智能分类后，某些应用图标显示为机器人的问题
							//【问题原因】默认配置的图标,没有launcher、Main属性但是有包类名，会再次添加到apps里，使用自己new的rInfo，根据rInfo初始化appinfo，由于rInfo里没有icon信息，导致cache里缓存的图标变成机器人
							//		再次加载桌面时，获取cache里的图标，显示为机器人。比如智能分类;比如广播删除快捷方式后，onResume里重新加载、等等。
							//【解决方案】根据intent获取rinfo
							//cheyingkun del start
							//							rInfo.activityInfo.applicationInfo.packageName = shortcut.getIntent().getComponent().getPackageName();
							//							rInfo.activityInfo.packageName = shortcut.getIntent().getComponent().getPackageName();
							//							rInfo.activityInfo.name = shortcut.getIntent().getComponent().getClassName();
							//cheyingkun del end
							rInfo = packageManager.resolveActivity( shortcut.getIntent() , 0 );//cheyingkun add
							//cheyingkun end
							//cheyingkun add start	//重启手机,在灰色图标状态进入T9搜索,输入内容,桌面异常终止(bug:0009975)
							if( rInfo != null && info.getTitle() != null && info.getIconUnavailable() != null )
							{
								rInfo.activityInfo.metaData = new Bundle();
								rInfo.activityInfo.metaData.putString( "greyIconTitle" , info.getTitle().toString() );
								rInfo.activityInfo.metaData.putByteArray( "greyIconBitmap" , ItemInfo.flattenBitmap( info.getIconUnavailable() ) );
							}
							//cheyingkun add end
						}
						// gaominghui@2016/12/12 ADD START 有T卡应用的手机，启动桌面后设为默认桌面，重启手机，空指针保护
						if( rInfo != null )
						{
							apps.add( rInfo );
							//Log.i( "TCardMount" , "loadAllApps 11111 apps = " + apps.size() );
						}
						// gaominghui@2016/12/12 ADD END 有T卡应用的手机，启动桌面后设为默认桌面，重启手机，空指针保护
					}
				}
			}
			//cheyingkun add start	//重启手机,在灰色图标状态进入T9搜索,输入内容,桌面异常终止(bug:0009975)
			sBgLauncherItems.clear();
			sBgFolderGreyItems.clear();
			//cheyingkun add end
			// Sort the applications by name
			final long sortTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
			Collections.sort( apps , new LauncherModel.ShortcutNameComparator( packageManager , mLabelCache ) );
			if( DEBUG_LOADERS )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "sort took " , ( SystemClock.uptimeMillis() - sortTime ) , "ms" ) );
			}
			//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.e( "cyk_bug : c_0003400" , "cyk launcherModel loadAllApps: apps********start " );
			}
			//cheyingkun add end
			// Create the ApplicationInfos
			for( int i = 0 ; i < apps.size() ; i++ )
			{
				ResolveInfo app = apps.get( i );
				//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					final String packageName = app.activityInfo.applicationInfo.packageName;
					ComponentName componentName = new ComponentName( packageName , app.activityInfo.name );
					Log.d( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcherModel loadAllApps: componentName " , componentName.toString() ) );
				}
				//cheyingkun add end
				//<phenix modify> liuhailin@2015-01-27 modify begin
				if( LauncherAppState.hideAppList( mContext , app.activityInfo.applicationInfo.packageName , app.activityInfo.name ) )
				{
					continue;
				}
				//<phenix modify> liuhailin@2015-01-27 modify end
				// This builds the icon bitmaps.
				//cheyingkun start	//重启手机,在灰色图标状态进入T9搜索,输入内容,桌面异常终止(bug:0009975)
				//				mBgAllAppsList.add( new AppInfo( packageManager , app , mIconCache , mLabelCache ) );//cheyingkun del
				//cheyingkun add start
				AppInfo appInfo = new AppInfo( packageManager , app , mIconCache , mLabelCache );
				if( app.activityInfo.metaData != null )
				{
					String title = app.activityInfo.metaData.getString( "greyIconTitle" );
					if( title != null )//如果设置过了title,说明是自己new的ResolveInfo,设置title和bitmap
					{
						//cheyingkun add start	//解决“重启手机后,进入T9搜索搜浏览器,系统应用浏览器异常显示为灰色”的问题。(bug:0010321)
						//【问题原因】以前判断拿到title不为空，说明是自己设置的title和bitmap，即 !appsContainsOf( shortcut , apps ) 成立，表示安装在T卡。
						//		但是某些手机重启时，通过packageManager.queryIntentActivities( mainIntent , 0 )查到手机安装所有应用列表不全。
						//		例如:(LA6-L手机,重启手机时,浏览器  com.baidu.browser.framework.BdBrowserActivity不在查询到的所有app列表中。)
						//		导致  !appsContainsOf( shortcut , apps )判断成立,应用被认为是T卡应用。T9搜索时，系统应用显示为灰色。
						//【解决方案】添加是否安装在T卡判断，进一步确认app是否安装在T卡。
						if( appInfo.getComponentName() != null && LauncherAppState.isAppInstalledSdcard( appInfo.getComponentName().getPackageName() , packageManager ) )
						//cheyingkun add end
						{
							byte[] byteArray = app.activityInfo.metaData.getByteArray( "greyIconBitmap" );
							// gaominghui@2016/12/06 ADD START 偶现mTCardMountManager空指针报停
							Bitmap bitmap = null;
							if( mTCardMountManager != null )
							{
								bitmap = mTCardMountManager.BytesToBimap( byteArray );
							}
							if( bitmap != null )
							{
								appInfo.setIconUnavailable( bitmap );
							}
							// gaominghui@2016/12/06 ADD END 偶现mTCardMountManager空指针报停
							appInfo.setAvailable( false );
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( "TCardMount" , StringUtils.concat( "get :  " , title ) );
							appInfo.setTitle( title );
						}
					}
				}
				mBgAllAppsList.add( appInfo );
				//cheyingkun add end
				//cheyingkun end
			}
			//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( "cyk_bug : c_0003400" , StringUtils.concat( "cyk launcherModel loadAllApps: apps********end " , apps.size() ) );
			}
			//cheyingkun add end
			//cheyingkun add start	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
			//如果第一次安装,不进行T卡卸载安装相关操作,T卡信息文件不存在,直接重启手机,T卡应用会跑到桌面,所以需要初始化一次T卡挂载信息的文件
			SharedPreferences prefs = mApp.getContext().getSharedPreferences( "launcher" , Activity.MODE_PRIVATE );
			boolean initTCardMount = prefs.getBoolean( "initTCardMount" , false );//是否初始化过T卡挂载信息
			boolean sdCardExist = LauncherAppState.isSDCardExist();
			//如果T卡可用,并且没有初始化过T卡挂载信息文件,则进行初始化操作
			if( !initTCardMount && mTCardMountManager != null && sdCardExist )
			{//第一次启动时记录一次数据
				mBgAllAppsList.initSDCardAllApps( null , packageManager );//初始化sd卡的应用列表
				mTCardMountManager.initSDCardAppsToFile( mBgAllAppsList.initSDCardAllApps );//初始化sd卡文件
				prefs.edit().putBoolean( "initTCardMount" , true ).commit();//改变标志
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( "初始化sd卡app信息: " , mBgAllAppsList.initSDCardAllApps.size() ) );
			}
			//cheyingkun add end
			// Huh? Shouldn't this be inside the Runnable below?
			final ArrayList<AppInfo> added = mBgAllAppsList.added;
			mBgAllAppsList.added = new ArrayList<AppInfo>();
			// Post callback on main thread
			mHandler.post( new Runnable() {
				
				public void run()
				{
					final long bindTime = SystemClock.uptimeMillis();
					final Callbacks callbacks = tryGetCallbacks( oldCallbacks );
					if( callbacks != null )
					{
						callbacks.bindAllApplications( added );
						if( DEBUG_LOADERS )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG , StringUtils.concat( "bound " , added.size() , " apps in " , ( SystemClock.uptimeMillis() - bindTime ) , "ms" ) );
						}
					}
					else
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "not binding apps: no Launcher activity" );
					}
				}
			} );
			if( DEBUG_LOADERS )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "Icons processed in " , ( SystemClock.uptimeMillis() - loadTime ) , "ms" ) );
			}
		}
		
		public void dumpState()
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				synchronized( sBgLock )
				{
					Log.d( TAG , StringUtils.concat(
							"mLoaderTask.mContext=" + mContext ,
							"-mLoaderTask.mIsLaunching=" ,
							mIsLaunching ,
							"-mLoaderTask.mStopped=" ,
							mStopped ,
							"-mLoaderTask.mLoadAndBindStepFinished=" ,
							mLoadAndBindStepFinished ,
							"-mItems size=" ,
							sBgWorkspaceItems.size() ) );
				}
			}
		}
		
		//cheyingkun add start	//解决“分类前，拖动两个应用生成文件夹。分类后卸载之前的两个应用。恢复之前布局，空文件夹未删除。”的问题【i_0011018】
		private void removeEmptyFolderOnLoadWorkspace(
				ArrayList<Integer> itemsRemoveInFolder )
		{
			if( itemsRemoveInFolder == null )
			{
				return;
			}
			final ArrayList<Long> removeFolder = new ArrayList<Long>();//item=0,1   要删除的文件夹列表
			final ArrayList<ItemInfo> lastItemInFolder = new ArrayList<ItemInfo>();//要更新的应有列表
			for( int folderId : itemsRemoveInFolder )
			{
				FolderInfo folderInfo = sBgFolders.get( (long)folderId );
				if( folderInfo != null )
				{
					if( Folder.isNeedDeleteFolder( folderInfo , mContext ) )//cheyingkun add	//解决“单层模式下智能分类，切换到双层模式后，卸载某个分类好文件夹中的所有应用。再切换回单层模式时，智能分类空文件夹被删除”的问题【i_0011203】
					{
						if( folderInfo.getContents().size() == 0 )//文件夹没有item,删除文件夹
						{
							removeFolder.add( folderInfo.getId() );
						}
						//cheyingkun start	//解决“单层模式下智能分类，切换到双层模式后，卸载某个分类好文件夹中的所有应用。再切换回单层模式时，智能分类空文件夹被删除”的问题【i_0011203】
						//						else if( Folder.isNeedDeleteFolder( folderInfo , mContext ) )//cheyingkun del	
						else if( folderInfo.getContents().size() == 1 )//cheyingkun add
						//cheyingkun end
						{
							long id = folderInfo.getContents().get( 0 ).getId();
							ItemInfo itemInfo = sBgItemsIdMap.get( id );//拿到应用的信息
							//把文件夹页面数和位置信息复制给应用
							itemInfo.setContainer( folderInfo.getContainer() );
							itemInfo.setScreenId( folderInfo.getScreenId() );
							itemInfo.setCellX( folderInfo.getCellX() );
							itemInfo.setCellY( folderInfo.getCellY() );
							//把更新后的应用信息添加到要更新的列表
							lastItemInFolder.add( itemInfo );
							//把文件夹id添加到要删除的列表
							removeFolder.add( folderInfo.getId() );
						}
					}
				}
			}
			//删除列表中的文件夹
			for( Long removeEmptyFolderId : removeFolder )
			{
				sBgWorkspaceItems.remove( sBgFolders.get( removeEmptyFolderId ) );
				sBgFolders.remove( removeEmptyFolderId );
				sBgItemsIdMap.remove( removeEmptyFolderId );
			}
			//相应的数据库操作，如果列表size大于0，开启事务执行数据库操作
			if( removeFolder.size() > 0 || lastItemInFolder.size() > 0 )
			{
				SQLiteDatabase db = LauncherAppState.getLauncherProvider().getProviderDB();
				db.beginTransaction();
				try
				{
					if( removeFolder.size() > 0 )
					{
						final ContentResolver contentResolver = mContext.getContentResolver();
						ContentProviderClient client = contentResolver.acquireContentProviderClient( LauncherSettings.Favorites.CONTENT_URI );
						// remove emptyFolder in Database
						for( long id : removeFolder )
						{
							try
							{
								client.delete( LauncherSettings.Favorites.getContentUri( id , false ) , null , null );
							}
							catch( RemoteException e )
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.w( TAG , StringUtils.concat( "Could not remove id = " , id ) );
							}
						}
					}
					//update ItemInfo in Database
					for( ItemInfo info : lastItemInFolder )
					{
						moveItemInDatabase( mContext , info , info.getContainer() , info.getScreenId() , info.getCellX() , info.getCellY() );
					}
					db.setTransactionSuccessful();
				}
				catch( Exception e )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , e.toString() );
				}
				finally
				{
					db.endTransaction();
				}
			}
		}
		//cheyingkun add end
	}
	
	void enqueuePackageUpdated(
			PackageUpdatedTask task )
	{
		synchronized( sWorker )
		{
			//cheyignkun add start	//解决“偶先桌面图标尺寸初始化之前收到应用更新广播，获取图标时无限循环导致堆栈溢出”的问题。
			//【问题原因】桌面广播注册比图标尺寸初始化早，在图标尺寸初始化之前收到更新广播，获取图标时无限循环导致堆栈溢出。
			//【解决方案】图标尺寸没初始化完之前，收到的广播先存起来。
			DynamicGrid mDynamicGrid = LauncherAppState.getInstance().getDynamicGrid();
			//cheyingkun add end
			//cheyingkun add start	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
			if( isLoaderTaskRunning() || ( OperateHelp.getInstance( null ) != null && OperateHelp.getInstance( null ).isGettingOperateDate() ) || mTCardMountOpInLoad.size() > 0 //
					|| mDynamicGrid == null//cheyignkun add	//解决“偶先桌面图标尺寸初始化之前收到应用更新广播，获取图标时无限循环导致堆栈溢出”的问题。
			)
			{
				mTCardMountOpInLoad.add( task );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( "mTCardMountOpInLoad.add " , task.mOp ) );
			}
			else
			//cheyingkun add end
			{
				sWorker.post( task );
			}
		}
	}
	
	private class PackageUpdatedTask implements Runnable
	{
		
		int mOp;
		String[] mPackages;
		public static final int OP_NONE = 0;
		public static final int OP_ADD = 1;
		public static final int OP_UPDATE = 2;
		public static final int OP_REMOVE = 3; // uninstlled
		public static final int OP_UNAVAILABLE = 4; // external TCard unmounted
		public static final int OP_AVAILABLE = 5;//cheyingkun add	//TCardMount(external TCard mounted)
		
		public PackageUpdatedTask(
				int op ,
				String[] packages )
		{
			mOp = op;
			mPackages = packages;
		}
		
		public void run()
		{
			final Context context = mApp.getContext();
			final String[] packages = mPackages;
			final int N = packages.length;
			switch( mOp )
			{
				case OP_ADD:
					for( int i = 0 ; i < N ; i++ )
					{
						if( DEBUG_LOADERS )
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG , "mAllAppsList.addPackage " + packages[i] );
						//xiatian add start	//fix bug：解决“在phenix桌面'正常运行'（没有强制停止）的状态下安装主题，主题安装成功后，安装的主题会在桌面显示”的问题。
						if( LauncherAppState.hideAppList( context , packages[i] ) )
						{
							continue;
						}
						//xiatian add end
						mBgAllAppsList.addPackage( context , packages[i] );
					}
					break;
				case OP_UPDATE:
					for( int i = 0 ; i < N ; i++ )
					{
						if( DEBUG_LOADERS )
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG , "mAllAppsList.updatePackage " + packages[i] );
						//xiatian add start	//fix bug：解决“在隐藏列表的应用（目前是主题和桌面本身）apk升级后，应用图标在桌面显示”的问题。
						if( !packages[i].equals( context.getPackageName() ) && LauncherAppState.hideAppList( context , packages[i] ) )
						{
							continue;
						}
						//xiatian add end
						mBgAllAppsList.updatePackage( context , packages[i] );
						WidgetPreviewLoader.removePackageFromDb( mApp.getWidgetPreviewCacheDb() , packages[i] );
					}
					break;
				case OP_REMOVE:
					//				case OP_UNAVAILABLE://cheyingkun del	//TCardMount(把删除和挂载逻辑分开处理)
					for( int i = 0 ; i < N ; i++ )
					{
						if( DEBUG_LOADERS )
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG , StringUtils.concat( "mAllAppsList.removePackage " , packages[i] ) );
						mBgAllAppsList.removePackage( context , packages[i] );
						if( ThemeManager.getInstance() != null )
						{
							ThemeManager.getInstance().RemovePackage( packages[i] );
						}
						WidgetPreviewLoader.removePackageFromDb( mApp.getWidgetPreviewCacheDb() , packages[i] );
					}
					break;
				//cheyingkun add start	//TCardMount
				case OP_UNAVAILABLE://T卡拔出
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.d( "TCardMount" , StringUtils.concat( "OP_UNAVAILABLE" , N ) );
						Log.d( "TCardMount" , StringUtils.concat( "mBgAllAppsList.unavailable.size before: " , mBgAllAppsList.unavailable.size() ) );
					}
					for( int i = 0 ; i < N ; i++ )
					{
						if( DEBUG_LOADERS )
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( TAG , StringUtils.concat( "mAllAppsList.unavailable " , packages[i] ) );
						mBgAllAppsList.unavailablePackage( packages[i] );
						ThemeManager themeManager = ThemeManager.getInstance();
						if( themeManager != null )
						{
							themeManager.RemovePackage( packages[i] );
						}
					}
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , StringUtils.concat( "mBgAllAppsList.unavailable.size after: " , mBgAllAppsList.unavailable.size() ) );
					break;
				case OP_AVAILABLE://T卡插入
					break;
			//cheyingkun add end
			}
			ArrayList<AppInfo> added = null;
			ArrayList<AppInfo> modified = null;
			final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
			final ArrayList<AppInfo> removedApps = new ArrayList<AppInfo>();
			if( mOp == OP_ADD )
			{
				if( mBgAllAppsList.added.size() == 0 && packages != null )//说明新安装的应用没有launcher属性，若是运营文件夹中的图标，要删除
				{
					if( callbacks != null )
					{
						callbacks.clearOperateIcon( packages );
					}
				}
			}
			if( mBgAllAppsList.added.size() > 0 )
			{
				added = new ArrayList<AppInfo>( mBgAllAppsList.added );
				mBgAllAppsList.added.clear();
			}
			if( mBgAllAppsList.modified.size() > 0 )
			{
				modified = new ArrayList<AppInfo>( mBgAllAppsList.modified );
				mBgAllAppsList.modified.clear();
			}
			if( mBgAllAppsList.removed.size() > 0 )
			{
				removedApps.addAll( mBgAllAppsList.removed );
				mBgAllAppsList.removed.clear();
			}
			//cheyingkun add start	//TCardMount(挂载T卡的列表)
			final ArrayList<AppInfo> unavailable = new ArrayList<AppInfo>();
			if( mBgAllAppsList.unavailable.size() > 0 )
			{
				unavailable.addAll( mBgAllAppsList.unavailable );
				mBgAllAppsList.unavailable.clear();//cheyingkun add	//RestartMobileTCardAppDismiss(清空挂载列表)bug:0009466
			}
			//cheyingkun add end
			if( callbacks == null )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , "Nobody to tell about the new app.  Launcher is probably loading." );
				return;
			}
			if( mOp == OP_ADD )// mOp == OP_UPDATE时,也会改变added列表,所以判断 mOp == OP_ADD 不应该包括之前的add逻辑
			{
				TCardMountOpAdd( packages , added );//整理T卡代码(OP_ADD时的T卡挂载逻辑)
			}
			//cheyingkun add start	//解决“挂载T卡状态下，2.1版豆瓣FM变灰后，此时在安装4.2版豆瓣FM，返回桌面2.1版FM显示为机器人”的问题。【i_0011411】
			//update时,可能会把added列表改变,但操作不是mOp_ADD,所以放出来
			if( added != null )
			{
				// Ensure that we add all the workspace applications to the db
				Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
				{
					addAndBindAddedItems( context , new ArrayList<ItemInfo>() , null , cb , added , true , !mIsLoaderTaskRunning );
				}
				else
				{
					final ArrayList<ItemInfo> addedInfos = new ArrayList<ItemInfo>( added );
					addAndBindAddedItems( context , addedInfos , null , cb , added , true , !mIsLoaderTaskRunning );
				}
				//cheyingkun add end
				Runnable r = new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						if( LauncherAppState.getActivityInstance() != null )
							OperateHelp.getInstance( context ).dealInstallActivity( packages[0] );//zjp,新安装的应用在联网的情况自动分类
					}
				};
				runOnWorkerThread( r );
			}
			if( modified != null )
			{
				final ArrayList<AppInfo> modifiedFinal = modified;
				// Update the launcher db to reflect the changes
				for( AppInfo a : modifiedFinal )
				{
					ArrayList<ItemInfo> infos = getItemInfoForComponentName( a.getComponentName() );
					for( ItemInfo i : infos )
					{
						if( isShortcutInfoUpdateable( i ) )
						{
							ShortcutInfo info = (ShortcutInfo)i;
							//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
							//info.title = a.title.toString();
							if( a.getTitle() == null )
							{
								a.setTitle( "name = null" );
							}
							info.setTitle( a.getTitle().toString() );
							//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
							updateItemInDatabase( context , info );
						}
					}
				}
				mHandler.post( new Runnable() {
					
					public void run()
					{
						Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
						if( callbacks == cb && cb != null )
						{
							callbacks.bindAppsUpdated( modifiedFinal );
						}
					}
				} );
			}
			// If a package has been removed, or an app has been removed as a result of
			// an update (for example), make the removed callback.
			if( mOp == OP_REMOVE || !removedApps.isEmpty() )
			{
				final boolean packageRemoved = ( mOp == OP_REMOVE );
				final ArrayList<String> removedPackageNames = new ArrayList<String>( Arrays.asList( packages ) );
				// Update the launcher db to reflect the removal of apps
				if( packageRemoved )
				{
					for( String pn : removedPackageNames )
					{
						ArrayList<ItemInfo> infos = getItemInfoForPackageName( pn );
						for( ItemInfo i : infos )
						{
							//cheyingkun add start	//卸载应用时判断虚图标是否跟随apk删除
							if( !( i instanceof ShortcutInfo// 
									&& ( (ShortcutInfo)i ).getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL// 
							&& !( (ShortcutInfo)i ).makeVirtual().getIsFollowAppUninstall() )//
							)
							//cheyingkun add end
							{
								deleteItemFromDatabase( context , i );
							}
						}
					}
					// Remove any queued items from the install queue
					String spKey = LauncherAppState.getSharedPreferencesKey();
					SharedPreferences sp = context.getSharedPreferences( spKey , Context.MODE_PRIVATE );
					InstallShortcutReceiver.removeFromInstallQueue( sp , removedPackageNames );
				}
				else
				{
					for( AppInfo a : removedApps )
					{
						ArrayList<ItemInfo> infos = getItemInfoForComponentName( a.getComponentName() );
						for( ItemInfo i : infos )
						{
							deleteItemFromDatabase( context , i );
						}
					}
				}
				if( mOp == OP_REMOVE )
				{
					TCardMountOpRemove( removedApps );//整理T卡代码(卸载应用时,T卡相关处理)
				}
				mHandler.post( new Runnable() {
					
					public void run()
					{
						Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
						if( callbacks == cb && cb != null )
						{
							callbacks.bindComponentsRemoved( removedPackageNames , removedApps , packageRemoved );//哈哈
						}
					}
				} );
			}
			//cheyingkun add start	//TCardMount(挂载和取消挂载操作处理)
			if( mOp == OP_UNAVAILABLE && unavailable != null && unavailable.size() > 0 )//如果操作是挂载,并且挂载列表不为空
			{
				TCardMountOpUnavailable( unavailable );//整理T卡代码(挂载T卡时,T卡相关处理)
			}
			if( mOp == OP_AVAILABLE )//如果是OP_AVAILABLE操作
			{
				TCardAvailable( packages , context );//cheyingkun add	//RestartMobileTCardAppDismiss
			}
			//cheyingkun del start	//解决“多次安装卸载同一个能产生小部件的应用，安装成功后，该应用的小部件几率性显示为机器人”的问题。【i_0011098】
			//【问题原因】安装应用时，mLoadedPreviews.containsKey( name )包含新安装的应用，获取图标为机器人。
			//		1.卸载时，正常逻辑会删除mLoadedPreviews中该应用的name，然后刷新小部件列表。
			//		2.但是由于刷新的方法在多处被调用（LauncherModel的PackageUpdatedTask调用两次，LauncherAppWidgetHost的onProvidersChanged中调用一次）,一定概率出现刷新的列表依然包含被删除的应用。
			//		3.被删除的应用又被put到mLoadedPreviews中，其图标为机器人。再次安装时就直接从mLoadedPreviews中拿图片。
			//【解决方案】删除掉其他所有地方刷新widget的方法,只在LauncherAppWidgetHost的onProvidersChanged中调用
			//			//cheyingkun del start	//解决“小组件界面，安装卸载会生成快捷方式的应用，小插件几率性显示为机器人。”的问题。【i_0011025】
			//			//【问题原因】在小部件界面卸载了可以生成快捷方式的app，桌面收到remove广播进行一系列处理。先走到getSortedWidgetsAndShortcuts( context )
			//			//获取了老的快捷方式列表，然后Launcher的onCreate中注册的LauncherAppWidgetHost  widget改变的监听。系统收到widget变化，在回调方法onProvidersChanged中处理。
			//			//最后走到我们post的run方法。 由于获取widget数据过早，导致最后更新时把删除掉的应用也算进去，显示成了机器人。
			//			//【解决方案】把PackageUpdatedTask的run方法中获取widget数据的方法放到Runnable中，当真正要更新数据时再去获取最新的widget列表。
			//			//			final ArrayList<Object> widgetsAndShortcuts = getSortedWidgetsAndShortcuts( context );
			//			//cheyingkun del end 
			//			mHandler.post( new Runnable() {
			//				
			//				@Override
			//				public void run()
			//				{
			//					final ArrayList<Object> widgetsAndShortcuts = getSortedWidgetsAndShortcuts( context );//cheyingkun add	//解决“小组件界面，安装卸载会生成快捷方式的应用，小插件几率性显示为机器人。”的问题。【i_0011025】
			//					Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
			//					if( callbacks == cb && cb != null )
			//					{
			//						callbacks.bindPackagesUpdated( widgetsAndShortcuts );
			//					}
			//				}
			//			} );
			//cheyingkun del end
			// Write all the logs to disk
			mHandler.post( new Runnable() {
				
				public void run()
				{
					Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
					if( callbacks == cb && cb != null )
					{
						callbacks.dumpLogsToLocalData();
					}
				}
			} );
		}
		
		//cheyingkun add start	//RestartMobileTCardAppDismiss bug:0009466
		/**
		 * T卡插入的相关操作--安装sd卡的处理逻辑
		 * @param packages
		 * @param context
		 */
		private void TCardAvailable(
				String[] packages ,
				Context context )
		{
			if( packages == null || packages.length == 0 || context == null )
			{
				return;
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "TCardMount" , " TCardAvailable " );
			TCardMountManager mTCardMountManager = TCardMountManager.getInstance( context );
			//cheyingkun start	//RestartMobileTCardAppDismiss(修改T卡插入的逻辑处理)bug:0009466
			//【问题原因】重启手机,关机时OCCO V8 会发送T卡挂载广播,开机后会发送T卡安装广播.而launcher在收到广播后已经loadWorkspace完毕,loadWorkspace期间,会根据数据库信息,和已安装应用信息添加快捷方式到桌面,数据库中有T卡的挂载信息,但是当前安装应用信息没有T卡应用,所以未显示.
			//【解决方案】在加载时,如果数据库中有,挂载文件中也有,就不根据已安装应用信息进行处理.等收到T卡安装信息,在更新图标并取消灰色
			//cheyingkun add start
			ArrayList<String> availablePackageName = new ArrayList<String>();//保存T卡插入系统发来的包名列表
			for( String packageName : packages )//把系统发送的包名数组转换成列表
			{
				availablePackageName.add( packageName );
			}
			if( availablePackageName.size() > 0 )//如果T卡插入,从系统获取的包名不为空
			{
				Map<Intent , Bitmap> mountInfo = null;//从文件中读取的挂载应用信息
				if( mTCardMountManager != null )//挂载时,清空挂载文件
				{
					mTCardMountManager.readFromFile();//把文件中保存的挂在信息读取到map中
					//cheyingkun start	//解决“挂载T卡状态下，2.1版豆瓣FM变灰后，此时在安装4.2版豆瓣FM，返回桌面2.1版FM显示为机器人”的问题。【i_0011411】
					//						mountInfo.putAll( mTCardMountManager.getMountInfo() );//把读到的所有信息put到新的map中	//cheyingkun del
					mountInfo = mTCardMountManager.getMountInfo();//cheyingkun add
					//cheyingkun end
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , StringUtils.concat( "插入T卡,从文件中读取的信息mountInfo.size : " , mountInfo.size() ) );
					//cheyingkun del start	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
					//挂载信息文件不清空
					//						mTCardMountManager.clearMountInfoAddFile();//清空文件和map//插入T卡不清空文件
					//						//cheyingkun add start	//针对开关机会发送T卡挂载信息的手机,添加关机和保存信息的判断
					//						Log.d( "TCardMount" , " hasTCardMountInfo: clear " );
					//						mTCardMountManager.setHasTCardMountInfo( false );
					//						//cheyingkun add end
					//cheyingkun del end
				}
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( "插入T卡,从文件中读取的信息mountInfo.size : " , mountInfo.size() ) );
				//1.从文件中读取上次的挂在信息
				//					Map<String , Bitmap> appBitmap = new HashMap<String , Bitmap>();//安装应用列表中获取的应用图标(string 包名,bitmap 图标)//cheyingkun del	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
				/**不可用变为可用的列表(最终要取消灰化效果的appInfo列表)*/
				ArrayList<AppInfo> unavailableToAvailable = new ArrayList<AppInfo>();
				/**不可用直接删除列表(最终要直接删除灰化图标的包名列表)*/
				final ArrayList<String> unavailableToRemove = new ArrayList<String>();
				PackageManager packageManager = context.getPackageManager();
				/**不可用变为可用的PackageName列表*/
				//					ArrayList<String> unavailableToAvailableIntent = new ArrayList<String>();//cheyingkun del	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
				ArrayList<String> unavailableToAvailablePackageName = new ArrayList<String>();//cheyingkun add	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
				/**插入T卡,新安装的应用包名(插入T卡,从系统广播传过来的数据中分离出来的新安装的应用包名列表)*/
				final ArrayList<String> unavailableToAdd = new ArrayList<String>();
				/**文件中读取的挂载信息的的Intent集合(包括需要取消灰化的和直接删除灰化图标的)*/
				Set<Intent> mountIntentSet = mountInfo.keySet();
				//2.遍历整个从文件中读取的集合,判断哪些是要取消灰化的,哪些是要删除灰化图标的
				for( Intent intent : mountIntentSet )
				{
					List<ResolveInfo> apps = packageManager.queryIntentActivities( intent , 0 );//根据intent查询app是否安装
					if( apps != null && apps.size() > 0 )//如果查找到,说明该Intent代表的appInfo需要取消灰化,否则直接取消灰化图标
					{
						//							unavailableToAvailableIntent.add( intent.toUri( 0 ) );//cheyingkun del	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
						unavailableToAvailablePackageName.add( intent.getComponent().getPackageName() );//cheyingkun add	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
					}
					else
					{
						unavailableToRemove.add( intent.getComponent().getPackageName() );//否则 ,添加到删除列表
					}
				}
				//cheyingkun add start	//TCardMountUpdateAppBitmapOptimization
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( "TCardMount" , StringUtils.concat( "根据文件,要显示的个数 : " , unavailableToAvailablePackageName.size() ) );
					Log.d( "TCardMount" , StringUtils.concat( "系统发来的包名个数 : " , availablePackageName.size() ) );
				}
				//cheyingkun add end
				//2.根据系统发送来的包名和所有已安装app匹配到的应用信息,并获取应用信息图标
				final ArrayList<AppInfo> availableAppInfo = new ArrayList<AppInfo>();//根据报名信息得到的应用信息列表(包含取消灰化和新安装的)
				//新安装应用的AppInfo列表(T卡挂载时新安装,说明都是T卡应用)
				final ArrayList<AppInfo> addAppInfo = new ArrayList<AppInfo>();//cheyingkun add	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
				final Intent mainIntent = new Intent( Intent.ACTION_MAIN , null );
				mainIntent.addCategory( Intent.CATEGORY_LAUNCHER );
				List<ResolveInfo> apps = packageManager.queryIntentActivities( mainIntent , 0 );//查询所有已安装app
				HashMap<Object , CharSequence> labelCache = new HashMap<Object , CharSequence>();
				IconCache mIconCache = mApp.getIconCache();//使用同一个iconcache
				for( ResolveInfo app : apps )
				{
					if( availablePackageName.contains( app.activityInfo.applicationInfo.packageName ) )
					{
						AppInfo appInfo = new AppInfo( packageManager , app , mIconCache , labelCache );
						//							appBitmap.put( app.activityInfo.applicationInfo.packageName , Tools.drawableToBitmap( app.loadIcon( packageManager ) ) );//cheyingkun del	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
						availableAppInfo.add( appInfo );
					}
				}
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( "根据系统发来的包名得到的appInfo个数 : " , availableAppInfo.size() ) );//cheyingkun add	//TCardMountUpdateAppBitmapOptimization
				//3.插入T卡,根据系统发来的报名列表分离出取消灰化的和新安装的
				int availableAppInfoCount = availableAppInfo.size();
				for( int i = 0 ; i < availableAppInfoCount ; i++ )
				{
					Intent intent = availableAppInfo.get( i ).getIntent();
					//						if( !unavailableToAvailableIntent.contains( intent.toUri( 0 ) ) )//如果取消灰化的列表中不包含该Intent,说明是新安装的应用//cheyingkun del	//TCardMountUpdateAppBitmapOptimization(改为根据包名判断)
					if( intent.getComponent() != null && !unavailableToAvailablePackageName.contains( intent.getComponent().getPackageName() ) )//cheyingkun add	//TCardMountUpdateAppBitmapOptimization(改为根据包名判断)
					{
						unavailableToAdd.add( intent.getComponent().getPackageName() );
						addAppInfo.add( availableAppInfo.get( i ) );//cheyingkun add	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
					}
					else
					{//如果包含,说明是取消灰化的appInfo
						unavailableToAvailable.add( availableAppInfo.get( i ) );
					}
				}
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( "unavailableToAvailable.size : " , unavailableToAvailable.size() ) );
				//4.取消灰化效果(unavailableToAvailable)
				for( AppInfo a : unavailableToAvailable )//遍历所有取消灰化效果的appinfo,恢复原来图标-------------------这个循环的log没有打,所以图标没有取消灰化
				{
					ArrayList<ItemInfo> infos = getItemInfoForComponentName( a.getComponentName() );
					for( ItemInfo i : infos )
					{
						if( isShortcutInfoUpdateable( i ) )
						{
							//cheyingkun add start	//重启手机,在灰色图标状态进入T9搜索,输入内容,桌面异常终止(bug:0009975)
							for( AppInfo appInfo : mBgAllAppsList.data )//T卡插入,取消图标灰化,更新data中的数据
							{
								if( a.getIntent() != null//
										&& a.getIntent().getComponent() != null//
										&& appInfo.getIntent() != null//
										&& appInfo.getIntent().getComponent() != null//
										&& a.getIntent().getComponent().getPackageName() != null//
										&& a.getIntent().getComponent().getPackageName().equals( appInfo.getIntent().getComponent().getPackageName() ) )
								{
									appInfo.setAvailable( true );
									if( a.getTitle() != null && a.getIconBitmap() != null )
									{
										appInfo.setTitle( a.getTitle() );
										appInfo.setIconBitmap( a.getIconBitmap() );
										if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
											Log.d( "TCardMount" , StringUtils.concat( "插入T卡,更新data: " , appInfo.getTitle() ) );
									}
								}
							}
							//cheyingkun add end
							ShortcutInfo info = (ShortcutInfo)i;
							info.setAvailable( true );
							info.setIcon( a.getIconBitmap() );
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( "TCardMount" , StringUtils.concat( "插入T卡,取消灰化效果的info: " , info.getTitle() ) );
						}
					}
				}
				//					appBitmap.clear();//cheyingkun del	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
				availableAppInfo.clear();//清空不可用列表
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( "unavailableToRemove.size:" , unavailableToRemove.size() , "-unavailableToAdd.size:" , unavailableToAdd.size() ) );
				if( unavailableToRemove.size() > 0 )
				{
					mTCardMountManager.removeMountInfoByPackageName( unavailableToRemove );//cheyingkun add	//解决“挂载T卡状态下，2.1版豆瓣FM变灰后，此时在安装4.2版豆瓣FM，返回桌面2.1版FM显示为机器人”的问题。【i_0011411】
					String[] arrayToRemove = (String[])unavailableToRemove.toArray( new String[unavailableToRemove.size()] );
					enqueuePackageUpdated( new PackageUpdatedTask( PackageUpdatedTask.OP_REMOVE , arrayToRemove ) );//删除
				}
				if( unavailableToAdd.size() > 0 )
				{
					//cheyingkun add start	//重启时会发送T卡卸载安装的手机,多次重启后,文件夹中的图标跑到桌面.(bug:0010116)
					//添加操作,添加的都是T卡的应用,因为关机不保存T卡信息,如果最后一次安装T卡时有新安装的T卡应用,应该保存T卡信息
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , StringUtils.concat( "unavailableToAdd.size : " , unavailableToAdd.size() ) );
					boolean sdCardExist = LauncherAppState.isSDCardExist();
					if( mTCardMountManager != null && sdCardExist )//如果没有初始化过,并且T卡存在
					{
						mBgAllAppsList.initSDCardAllApps( addAppInfo , packageManager );//初始化sd卡的应用列表
						mTCardMountManager.initSDCardAppsToFile( mBgAllAppsList.initSDCardAllApps );//初始化sd卡文件
						if( mountInfo.size() == 0 )
						{
							SharedPreferences prefs = mApp.getContext().getSharedPreferences( "launcher" , Activity.MODE_PRIVATE );
							boolean initTCardMount = prefs.getBoolean( "initTCardMount" , false );
							if( !initTCardMount )
							{
								prefs.edit().putBoolean( "initTCardMount" , true ).commit();
							}
						}
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( "TCardMount" , StringUtils.concat( " mBgAllAppsList.initSDCardAllApps.size : " , mBgAllAppsList.initSDCardAllApps.size() ) );
					}
					//cheyingkun add end
					String[] arrayToAdd = (String[])unavailableToAdd.toArray( new String[unavailableToAdd.size()] );
					//cheyingkun start	//TCardMountUpdateAppBitmapOptimization(T卡挂载安装时,桌面T卡应用图标更新优化)
					//cheyingkun add start
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , StringUtils.concat( "mBgAllAppsList.data.size : " , mBgAllAppsList.data.size() ) );
					for( String string : arrayToAdd )
					{
						int allAppCount = mBgAllAppsList.data.size();
						for( int i = 0 ; i < allAppCount ; i++ )
						{
							if( mBgAllAppsList.data.get( i ).getIntent().getComponent().getPackageName().equals( string ) )
							{
								mBgAllAppsList.data.remove( i );
								i--;
								allAppCount--;
							}
						}
					}
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , StringUtils.concat( "mBgAllAppsList.data.size:" , mBgAllAppsList.data.size() , "-arrayToAdd.size:" , arrayToAdd.length ) );
					enqueuePackageUpdated( new PackageUpdatedTask( PackageUpdatedTask.OP_ADD , arrayToAdd ) );//新安装
				}
				//cheyingkun start	//解决“卸载T卡灰色图标状态下，再次安装灰色图标应用，应用被安装在系统内存，能打开但是图标没有变亮。”的问题（bug：0010346）
				mTCardMountManager.sendRefreshAppBitmapMessage( unavailableToAvailable );//cheyingkun add
				//cheyingkun end
				//cheyingkun add end
				//cheyingkun end
			}
			//cheyingkun add end
			//cheyingkun end
			//cheyingkun add end
		}
		
		/**
		 * 收到op_add时的T卡处理逻辑
		 * @param packages
		 * @param added
		 */
		private void TCardMountOpAdd(
				String[] packages ,
				ArrayList<AppInfo> added )
		{
			if( packages == null || packages.length == 0 )
			{
				return;
			}
			TCardMountManager mTCardMountManager = TCardMountManager.getInstance( mApp.getContext() );
			//cheyingkun add start	//解决“卸载T卡灰色图标状态下，再次安装灰色图标应用，应用被安装在系统内存，能打开但是图标没有变亮的问题。”（bug：0010346）
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "TCardMount" , " OP_ADD " );
			//cheyingkun add end
			//cheyingkun add start	//解决“卸载T卡灰色图标状态下，再次安装灰色图标应用，应用被安装在系统内存，能打开但是图标没有变亮。”的问题（bug：0010346）
			if( mTCardMountManager != null )//如果是添加操作,根据系统发来的包名更新T卡应用信息
			{
				PackageManager packageManager = LauncherAppState.getInstance().getContext().getPackageManager();
				//1.拿到挂载信息
				mTCardMountManager.readFromFile();//把文件中保存的挂在信息读取到map中
				Map<Intent , Bitmap> mountInfo = mTCardMountManager.getMountInfo();
				Set<Intent> keySet = mountInfo.keySet();
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , StringUtils.concat( " keySet.size: " , keySet.size() , " packages.size:  " , packages.length ) );
				//2.循环系统发来的包名,从data中拿到应用信息
				ArrayList<AppInfo> appInfoList = new ArrayList<AppInfo>();
				int size = mBgAllAppsList.data.size();
				for( int i = 0 ; i < packages.length ; i++ )
				{
					for( int j = 0 ; j < size ; j++ )
					{
						AppInfo appInfo = mBgAllAppsList.data.get( j );
						if( packages[i] != null//
								&& appInfo != null//
								&& appInfo.getComponentName() != null//
								&& packages[i].equals( appInfo.getComponentName().getPackageName() ) )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( "TCardMount" , StringUtils.concat( " appInfoList.add( appInfo ): " , appInfo.getTitle() ) );
							appInfoList.add( appInfo );
						}
					}
				}
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( "TCardMount" , StringUtils.concat( " appInfoList.size: " , appInfoList.size() ) );
					Log.d( "TCardMount" , StringUtils.concat( " isSDCardExist: " , LauncherAppState.isSDCardExist() ) );
				}
				//3.如果sd卡不存在,则应用被安装到内存
				if( !LauncherAppState.isSDCardExist() )
				{
					//3.1判断appInfoList中所有应用是否存在挂载信息
					//如果存在,表示重新安装灰色图标应用到手机内存,需要更新标志位并刷新图标
					ArrayList<AppInfo> refreshAppList = new ArrayList<AppInfo>();//需要刷新的AppInfo列表
					ArrayList<Intent> removeIntent = new ArrayList<Intent>();//需要从挂载信息map中移除的Intnet列表
					//需要卸载的应用
					ArrayList<String> removePackageName = new ArrayList<String>();//cheyingkun add start	//解决“挂载T卡状态下，2.1版豆瓣FM变灰后，此时在安装4.2版豆瓣FM，返回桌面2.1版FM显示为机器人”的问题。【i_0011411】
					for( AppInfo appInfo : appInfoList )
					{
						for( Intent intent : keySet )
						{
							if( appInfo != null//
									&& intent != null//
									&& appInfo.getComponentName() != null//
									&& appInfo.getComponentName().equals( intent.getComponent() )//
									&& !LauncherAppState.isAppInstalledSdcard( appInfo.getComponentName().getPackageName() , packageManager ) )
							{
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.d( "TCardMount" , StringUtils.concat( " appInfo.setAvailable( true ): " , appInfo.getTitle() ) );
								//cheyingkun add start	//解决“挂载T卡状态下，2.1版豆瓣FM变灰后，此时在安装4.2版豆瓣FM，返回桌面2.1版FM显示为机器人”的问题。【i_0011411】
								//【问题原因】豆瓣2.1和4.2版本,包名一样类名不同。挂载T卡状态下，会直接add应用，而系统发来的只有包名，会从data中拿到两个appInfo信息。所以把两个都变亮了，但是其中一个拿不到图标，所以是机器人。
								//【解决方案】在添加变量的info前,根据ComponentName判断应用是否安装,如果没安装,则发送remove删除桌面图标和数据库。但是因为remove也是根据包名，所以卸载完以后还要再次add
								if( !LauncherAppState.isApkInstalled( appInfo.getComponentName() ) )//如果应用未安装
								{
									String packageName = appInfo.getComponentName().getPackageName();
									if( !removePackageName.contains( packageName ) )//添加到remove卸载列表
									{
										removePackageName.add( packageName );
									}
								}
								else
								{//如果应用安装,则变亮
									//cheyingkun add end
									appInfo.setAvailable( true );
									removeIntent.add( intent );
									refreshAppList.add( appInfo );
								}
							}
						}
					}
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , StringUtils.concat( "removeIntent.size:" , removeIntent.size() , "-refreshAppList.size:" , refreshAppList.size() ) );
					//3.2根据refreshAppList更新挂载信息保存的文件
					for( Intent intent : removeIntent )
					{
						mountInfo.remove( intent );
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( "TCardMount" , StringUtils.concat( " mountInfo.remove( intent.getComponent().getPackageName ): " , intent.getComponent().getPackageName() ) );
					}
					if( removeIntent.size() > 0 && refreshAppList.size() > 0 )
					{
						setShortcutInfoAvailable( refreshAppList , true );
						mTCardMountManager.writeToFile();
						//3.3刷新refreshAppList列表中的应用图标
						mTCardMountManager.sendRefreshAppBitmapMessage( refreshAppList );
					}
					//cheyingkun add start	//解决“挂载T卡状态下，2.1版豆瓣FM变灰后，此时在安装4.2版豆瓣FM，返回桌面2.1版FM显示为机器人”的问题。【i_0011411】
					if( removePackageName != null && removePackageName.size() > 0 )
					{
						String[] arrayToRemove = (String[])removePackageName.toArray( new String[removePackageName.size()] );
						enqueuePackageUpdated( new PackageUpdatedTask( PackageUpdatedTask.OP_REMOVE , arrayToRemove ) );//删除
						enqueuePackageUpdated( new PackageUpdatedTask( PackageUpdatedTask.OP_ADD , arrayToRemove ) );//添加
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( "TCardMount" , " remove  add added = null " );
						added = null;//清空第一次added操作,自己再发一次add逻辑重新走
					}
					//cheyingkun add end
				}
				//4.如果安装时T卡存在,判断是否直接安装在T卡,如果是,更新挂载信息文件
				else
				{
					boolean needSaveFile = false;
					//4.1循环appInfoList,如果安装在T卡,则加入到挂载信息中
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , " mountInfo: " + mountInfo.size() );
					ArrayList<ComponentName> mountInfoCN = new ArrayList<ComponentName>();
					ArrayList<AppInfo> appAddToSDCard = new ArrayList<AppInfo>();//cheyingkun add	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
					for( Intent intent : keySet )
					{
						mountInfoCN.add( intent.getComponent() );
					}
					for( AppInfo appInfo : appInfoList )
					{
						if( appInfo.getComponentName() != null//
								&& LauncherAppState.isAppInstalledSdcard( appInfo.getComponentName().getPackageName() , packageManager ) )
						{
							//4.2应用信息put到挂载信息的map中
							if( appInfo.getIntent() != null && appInfo.getIconBitmap() != null )
							{
								needSaveFile = true;
								//cheyingkun add start	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
								appInfo.setAvailable( true );
								appAddToSDCard.add( appInfo );
								//cheyingkun add end
								if( !mountInfoCN.contains( appInfo.getComponentName() ) )
								{
									mountInfo.put( appInfo.getIntent() , appInfo.getIconBitmap() );
									if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
										Log.d( "TCardMount" , StringUtils.concat( " mountInfo.put: " , appInfo.getTitle() ) );
								}
							}
						}
					}
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , StringUtils.concat( " mountInfo.size: " , mountInfo.size() ) );
					//4.3保存挂载信息
					if( needSaveFile )
					{
						mTCardMountManager.writeToFile();
					}
					//cheyingkun add start	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
					setShortcutInfoAvailable( appAddToSDCard , true );
					mTCardMountManager.sendRefreshAppBitmapMessage( appAddToSDCard );
					//cheyingkun add end
				}
				//cheyingkun add end
			}
		}
		
		/**
		 * 卸载应用时T卡相关逻辑
		 * @param removedApps
		 */
		private void TCardMountOpRemove(
				ArrayList<AppInfo> removedApps )
		{
			TCardMountManager mTCardMountManager = TCardMountManager.getInstance( mApp.getContext() );
			//cheyingkun add start	//解决“挂载T卡状态下，2.1版豆瓣FM变灰后，此时在安装4.2版豆瓣FM，返回桌面2.1版FM显示为机器人”的问题。【i_0011411】
			if( mTCardMountManager != null && removedApps.size() > 0 )//删除应用时,如果被删除应用在挂载信息中,则更新挂载文件
			{
				mTCardMountManager.readFromFile();//读取挂载信息文件
				ArrayList<String> removePackageName = new ArrayList<String>();
				for( AppInfo appInfo : removedApps )
				{
					String packageName = appInfo.getComponentName().getPackageName();
					removePackageName.add( packageName );
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( "TCardMount" , StringUtils.concat( "packageName : " , packageName ) );
				}
				mTCardMountManager.removeMountInfoByPackageName( removePackageName );//删除对应的挂载信息
				mTCardMountManager.writeToFile();//保存挂载信息文件
			}
			//cheyingkun add end
		}
		
		/**
		 * T卡挂载时的相关逻辑
		 * @param unavailable
		 */
		private void TCardMountOpUnavailable(
				ArrayList<AppInfo> unavailable )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "TcardMount" , " TCardMountOpUnavailable " );
			TCardMountManager mTCardMountManager = TCardMountManager.getInstance( mApp.getContext() );
			//cheyingkun start	//解决“卸载T卡灰色图标状态下，再次安装灰色图标应用，应用被安装在系统内存，能打开但是图标没有变亮。”的问题（bug：0010346）
			//cheyingkun add start
			ArrayList<ShortcutInfo> shortcuts = setShortcutInfoAvailable( unavailable , false );
			mTCardMountManager.setMountInfo( shortcuts );
			mTCardMountManager.writeToFile();
			mTCardMountManager.sendRefreshAppBitmapMessage( unavailable );
			//cheyingkun add end
			//cheyingkun end
		}
		
		//cheyingkun add end
		/**
		 * 根据传入的appInfo列表设置图标的available标志
		 * @param appInfoList	安装在T卡的应用列表
		 * @return shortcuts 返回ShortcutInfo 列表
		 */
		private ArrayList<ShortcutInfo> setShortcutInfoAvailable(
				ArrayList<AppInfo> appInfoList ,
				boolean available )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "TCardMount" , StringUtils.concat( "unavailable.size : " , appInfoList.size() ) );
			PackageManager packageManager = LauncherAppState.getInstance().getContext().getPackageManager();
			final ArrayList<ShortcutInfo> shortcuts = new ArrayList<ShortcutInfo>();
			for( AppInfo a : appInfoList )//遍历传入的appInfo列表,设置available
			{
				ArrayList<ItemInfo> infos = getItemInfoForComponentName( a.getComponentName() );
				for( ItemInfo i : infos )
				{
					if( LauncherModel.isShortcutInfoUpdateable( i ) )
					{
						if( available || LauncherAppState.isAppInstalledSdcard( a.getComponentName().getPackageName() , packageManager ) )//如果应用安装在sd卡,才去设置不可见
						{
							ShortcutInfo info = (ShortcutInfo)i;
							shortcuts.add( info );
							info.setAvailable( available );
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.d( "TCardMount" , StringUtils.concat( "info : " , info.getTitle() ) );
						}
					}
				}
			}
			return shortcuts;
		}
	}
	
	// Returns a list of ResolveInfos/AppWindowInfos in sorted order
	public static ArrayList<Object> getSortedWidgetsAndShortcuts(
			Context context )
	{
		PackageManager packageManager = context.getPackageManager();
		final ArrayList<Object> widgetsAndShortcuts = new ArrayList<Object>();
		if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
		{
			//xiatian start	//桌面支持配置隐藏特定的widget插件。
			//		List<AppWidgetProviderInfo> mWidgetListFinal = AppWidgetManager.getInstance( context ).getInstalledProviders();//xiatian del
			//xiatian add start
			List<AppWidgetProviderInfo> mWidgetList = AppWidgetManager.getInstance( context ).getInstalledProviders();
			List<AppWidgetProviderInfo> mWidgetListFinal = new ArrayList<AppWidgetProviderInfo>();
			for( AppWidgetProviderInfo mWidget : mWidgetList )
			{
				String mPackageName = mWidget.provider.getPackageName();
				String mClassName = mWidget.provider.getClassName();
				if( LauncherAppState.hideWidgetList( mPackageName , mClassName ) )
				{
					continue;
				}
				mWidgetListFinal.add( mWidget );
			}
			//xiatian add end
			//xiatian end
			widgetsAndShortcuts.addAll( mWidgetListFinal );
		}
		else
		//4.0安装的launcher不显示系统的widget，内置的显示。安装的无权限
		{
			try
			{
				PackageInfo packageInfo = packageManager.getPackageInfo( context.getPackageName() , 0 );
				if( packageInfo != null )
				{
					int flags = packageInfo.applicationInfo.flags;
					if( ( flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) != 0 || ( flags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
					{
						List<AppWidgetProviderInfo> mWidgetList = AppWidgetManager.getInstance( context ).getInstalledProviders();
						List<AppWidgetProviderInfo> mWidgetListFinal = new ArrayList<AppWidgetProviderInfo>();
						for( AppWidgetProviderInfo mWidget : mWidgetList )
						{
							String mPackageName = mWidget.provider.getPackageName();
							String mClassName = mWidget.provider.getClassName();
							if( LauncherAppState.hideWidgetList( mPackageName , mClassName ) )
							{
								continue;
							}
							mWidgetListFinal.add( mWidget );
						}
						widgetsAndShortcuts.addAll( mWidgetListFinal );
					}
				}
			}
			catch( NameNotFoundException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_SHOW_SHORTCUT_IN_WIDGET_LIST )//cheyingkun add	//是否在小部件界面中显示快捷方式。true显示；false不显示。默认true。
		{
			Intent shortcutsIntent = new Intent( Intent.ACTION_CREATE_SHORTCUT );
			//xiatian start	//桌面支持配置隐藏特定的快捷方式插件。
			//		List<ResolveInfo> mShortcutsListFinal = packageManager.queryIntentActivities( shortcutsIntent , 0 );//xiatian del
			//xiatian add start
			List<ResolveInfo> mShortcutsList = packageManager.queryIntentActivities( shortcutsIntent , 0 );
			List<ResolveInfo> mShortcutsListFinal = new ArrayList<ResolveInfo>();
			for( ResolveInfo mShortcut : mShortcutsList )
			{
				String mPackageName = mShortcut.activityInfo.applicationInfo.packageName;
				String mClassName = mShortcut.activityInfo.name;
				if( LauncherAppState.hideShortcutList( mPackageName , mClassName ) )
				{
					continue;
				}
				mShortcutsListFinal.add( mShortcut );
			}
			//xiatian add end
			//xiatian end
			widgetsAndShortcuts.addAll( mShortcutsListFinal );
		}
		Collections.sort( widgetsAndShortcuts , new LauncherModel.WidgetAndShortcutNameComparator( packageManager ) );
		return widgetsAndShortcuts;
	}
	
	//添加智能分类功能 , change by shlt@2015/02/10 UPD START
	//private boolean isValidPackageComponent(
	//		PackageManager pm ,
	//		ComponentName cn )
	//{
	//	if( cn == null )
	//	{
	//		return false;
	//	}
	//	try
	//	{
	//		// Skip if the application is disabled
	//		PackageInfo pi = pm.getPackageInfo( cn.getPackageName() , 0 );
	//		if( !pi.applicationInfo.enabled )
	//		{
	//			return false;
	//		}
	//		// Check the activity
	//		return( pm.getActivityInfo( cn , 0 ) != null );
	//	}
	//	catch( NameNotFoundException e )
	//	{
	//		return false;
	//	}
	//}
	//<数据库字段更新> liuhailin@2015-03-24 modify begin
	private boolean isValidPackageComponent(
			PackageManager pm ,
			Intent intent ,
			Intent operateIntent )
	{
		//boolean mIsOperateVirtualItem = intent.getBooleanExtra( "isOperateVirtualItem" , false );
		boolean mIsOperateVirtualItem = false;
		if( operateIntent != null )
		{
			mIsOperateVirtualItem = operateIntent.getBooleanExtra( EnhanceItemInfo.INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM , false );
		}
		if( mIsOperateVirtualItem )
		{
			return true;
		}
		//
		ComponentName cn = intent.getComponent();
		if( cn == null )
		{
			return false;
		}
		try
		{
			// Skip if the application is disabled
			PackageInfo pi = pm.getPackageInfo( cn.getPackageName() , 0 );
			if( !pi.applicationInfo.enabled )
			{
				return false;
			}
			// Check the activity
			return( pm.getActivityInfo( cn , 0 ) != null );
		}
		catch( NameNotFoundException e )
		{
			return false;
		}
	}
	
	//<数据库字段更新> liuhailin@2015-03-24 modify end
	//添加智能分类功能 , change by shlt@2015/02/10 UPD END
	/**
	 * This is called from the code that adds shortcuts from the intent receiver.  This
	 * doesn't have a Cursor, but
	 */
	public ShortcutInfo getShortcutInfo(
			PackageManager manager ,
			Intent intent ,
			Context context )
	{
		//<数据库字段更新> liuhailin@2015-03-24 modify begin
		//return getShortcutInfo( manager , intent , context , null , -1 , -1 , null );
		return getShortcutInfo( manager , intent , context , null , -1 , -1 , null , null );
		//<数据库字段更新> liuhailin@2015-03-24 modify end
	}
	
	/**
	 * Make an ShortcutInfo object for a shortcut that is an application.
	 *
	 * If c is not null, then it will be used to fill in missing data like the title and icon.
	 */
	//<数据库字段更新> liuhailin@2015-03-24 modify begin
	public ShortcutInfo getShortcutInfo(
			PackageManager manager ,
			Intent intent ,
			Context context ,
			Cursor c ,
			int iconIndex ,
			int titleIndex ,
			HashMap<Object , CharSequence> labelCache ,
			Intent operateIntent )
	//<数据库字段更新> liuhailin@2015-03-24 modify end
	{
		ComponentName componentName = intent.getComponent();
		final ShortcutInfo info = new ShortcutInfo();
		//添加智能分类功能 , change by shlt@2015/02/10 UPD START
		//if( componentName != null && !isValidPackageComponent( manager , componentName ) )
		if( componentName != null && !isValidPackageComponent( manager , intent , operateIntent ) )
		//添加智能分类功能 , change by shlt@2015/02/10 UPD END
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "Invalid package found in getShortcutInfo 5289 -- componentName: " + componentName.toString() ) );
			return null;
		}
		else
		{
			try
			{
				PackageInfo pi = manager.getPackageInfo( componentName.getPackageName() , 0 );
				info.initFlagsAndFirstInstallTime( pi );
			}
			catch( NameNotFoundException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "getPackInfo failed for package " , componentName.getPackageName() ) );
			}
		}
		// TODO: See if the PackageManager knows about this case.  If it doesn't
		// then return null & delete this.
		// the resource -- This may implicitly give us back the fallback icon,
		// but don't worry about that.  All we're doing with usingFallbackIcon is
		// to avoid saving lots of copies of that in the database, and most apps
		// have icons anyway.
		// Attempt to use queryIntentActivities to get the ResolveInfo (with IntentFilter info) and
		// if that fails, or is ambiguious, fallback to the standard way of getting the resolve info
		// via resolveActivity().
		Bitmap icon = null;
		ResolveInfo resolveInfo = null;
		ComponentName oldComponent = intent.getComponent();
		Intent newIntent = new Intent( intent.getAction() , null );
		newIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		newIntent.setPackage( oldComponent.getPackageName() );
		List<ResolveInfo> infos = manager.queryIntentActivities( newIntent , 0 );
		for( ResolveInfo i : infos )
		{
			ComponentName cn = new ComponentName( i.activityInfo.packageName , i.activityInfo.name );
			if( cn.equals( oldComponent ) )
			{
				resolveInfo = i;
			}
		}
		if( resolveInfo == null )
		{
			resolveInfo = manager.resolveActivity( intent , 0 );
		}
		if( resolveInfo != null )
		{
			icon = mIconCache.getIcon( componentName , resolveInfo , labelCache );
		}
		// the db
		if( icon == null )
		{
			if( c != null )
			{
				icon = getIconFromCursor( c , iconIndex , context );
			}
		}
		// the fallback icon
		if( icon == null )
		{
			//xiatian start	//优化桌面启动速度，mDefaultIcon不应该在LauncherModel的初始化中生成，应该在需要使用的时候生成。
			//			icon = getFallbackIcon();//xiatian del
			icon = getFallbackIcon( context );//xiatian add
			//xiatian end
			info.setIsUsingFallbackIcon( true );
		}
		info.setIcon( icon );
		// from the resource
		if( resolveInfo != null )
		{
			ComponentName key = LauncherModel.getComponentNameFromResolveInfo( resolveInfo );
			if( labelCache != null && labelCache.containsKey( key ) )
			{
				info.setTitle( labelCache.get( key ) );
			}
			else
			{
				info.setTitle( resolveInfo.activityInfo.loadLabel( manager ) );
				if( labelCache != null )
				{
					labelCache.put( key , info.getTitle() );
				}
			}
		}
		// from the db
		if( info.getTitle() == null )
		{
			if( c != null )
			{
				info.setTitle( c.getString( titleIndex ) );
			}
		}
		// fall back to the class name of the activity
		if( info.getTitle() == null )
		{
			info.setTitle( componentName.getClassName() );
		}
		info.setTitle( LauncherAppState.getAppReplaceTitle( info.getTitle() , context , componentName ) );//xiatian add	//桌面支持配置特定的activity的显示名称。
		info.setItemType( LauncherSettings.Favorites.ITEM_TYPE_APPLICATION );
		return info;
	}
	
	static ArrayList<ItemInfo> filterItemInfos(
			Collection<ItemInfo> infos ,
			ItemInfoFilter f )
	{
		HashSet<ItemInfo> filtered = new HashSet<ItemInfo>();
		for( ItemInfo i : infos )
		{
			if( i instanceof ShortcutInfo )
			{
				ShortcutInfo info = (ShortcutInfo)i;
				ComponentName cn = info.getIntent().getComponent();
				if( cn != null && f.filterItem( null , info , cn ) )
				{
					filtered.add( info );
				}
			}
			else if( i instanceof FolderInfo )
			{
				FolderInfo info = (FolderInfo)i;
				for( ShortcutInfo s : info.getContents() )
				{
					ComponentName cn = s.getIntent().getComponent();
					if( cn != null && f.filterItem( info , s , cn ) )
					{
						filtered.add( s );
					}
				}
			}
			else if( i instanceof LauncherAppWidgetInfo )
			{
				LauncherAppWidgetInfo info = (LauncherAppWidgetInfo)i;
				ComponentName cn = info.getProviderComponentName();
				if( cn != null && f.filterItem( null , info , cn ) )
				{
					filtered.add( info );
				}
			}
		}
		return new ArrayList<ItemInfo>( filtered );
	}
	
	private ArrayList<ItemInfo> getItemInfoForPackageName(
			final String pn )
	{
		ItemInfoFilter filter = new ItemInfoFilter() {
			
			@Override
			public boolean filterItem(
					ItemInfo parent ,
					ItemInfo info ,
					ComponentName cn )
			{
				return cn.getPackageName().equals( pn );
			}
		};
		return filterItemInfos( sBgItemsIdMap.values() , filter );
	}
	
	public ArrayList<ItemInfo> getItemInfoForComponentName(
			final ComponentName cname )
	{
		ItemInfoFilter filter = new ItemInfoFilter() {
			
			@Override
			public boolean filterItem(
					ItemInfo parent ,
					ItemInfo info ,
					ComponentName cn )
			{
				return cn.equals( cname );
			}
		};
		return filterItemInfos( sBgItemsIdMap.values() , filter );
	}
	
	public static boolean isShortcutInfoUpdateable(
			ItemInfo i )
	{
		if( i instanceof ShortcutInfo )
		{
			ShortcutInfo info = (ShortcutInfo)i;
			// We need to check for ACTION_MAIN otherwise getComponent() might
			// return null for some shortcuts (for instance, for shortcuts to
			// web pages.)
			Intent intent = info.getIntent();
			ComponentName name = intent.getComponent();
			if( info.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && Intent.ACTION_MAIN.equals( intent.getAction() ) && name != null )
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Make an ShortcutInfo object for a shortcut that isn't an application.
	 */
	//添加智能分类功能 , change by shlt@2015/02/10 UPD START
	//private ShortcutInfo getShortcutInfo(
	//		Cursor c ,
	//		Context context ,
	//		int iconTypeIndex ,
	//		int iconPackageIndex ,
	//		int iconResourceIndex ,
	//		int iconIndex ,
	//		int titleIndex )
	//<数据库字段更新> liuhailin@2015-03-24 modify end
	private ShortcutInfo getShortcutInfo(
			Cursor c ,
			Context context ,
			int itemType ,//xiatian add	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
			int iconTypeIndex ,
			int iconPackageIndex ,
			int iconResourceIndex ,
			int iconIndex ,
			int titleIndex ,
			Intent intent ,
			Intent operateIntent )
	//<数据库字段更新> liuhailin@2015-03-24 modify end
	//添加智能分类功能 , change by shlt@2015/02/10 UPD END
	{
		Intent.ShortcutIconResource iconResource = new Intent.ShortcutIconResource();//zhujieping add	//对于配置了图片的ShortcutInfo和VirtualInfo，需要保存图片相关信息
		Bitmap icon = null;
		final ShortcutInfo info = new ShortcutInfo();
		//xiatian start	//需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
		//		info.setItemType( LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT );//xiatian del
		info.setItemType( itemType );//xiatian add
		//xiatian end
		// TODO: If there's an explicit component and we can't install that, delete it.
		String mTitle = c.getString( titleIndex );
		boolean mIsHaveResourceName = mTitle.contains( LauncherProvider.ITEM_TITLE_RESOURCE_NAME_KEY );//xiatian add	//fix bug：解决“桌面上默认配置快捷方式和虚图标，切换语言后图标的名称没有切换为相应语言”的问题。
		//xiatian add start	//fix bug：解决“动态入口的图标和智能分类运营出来的图标的名称显示为桌面名称”的问题。
		boolean mIsDynamicEntryItem = intent == null ? false : intent.getStringExtra( OperateDynamicMain.FOLDER_VERSION ) != null;
		boolean mIsCategoryOperateItem = operateIntent == null ? false : operateIntent.getBooleanExtra( EnhanceItemInfo.INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM , false );
		//xiatian add end
		ZhiKeShortcutManager mZhiKeShortcutManager = ZhiKeShortcutManager.getInstance( context );//cheyingkun add	//配置可以通过广播删除的快捷方式【智科】【c_0004455】(应用名称不变)
		if(
		//
		( LauncherDefaultConfig.SWITCH_ENABLE_SHORTCUT_WIDGET_NAME_FOLLOW_SYSTEM_LANGUAGE /*cheyingkun add	//快捷方式名称是否跟随系统语言变化。true为跟随系统语言变化；false为不跟随。默认true。【c_0003657】*/)
		//
		&& ( mIsHaveResourceName == false /* //xiatian add	//fix bug：解决“桌面上默认配置快捷方式和虚图标，切换语言后图标的名称没有切换为相应语言”的问题。 */)
		//xiatian add //fix bug：解决“动态入口的图标和智能分类运营出来的图标的名称显示为桌面名称”的问题。
		&& ( mIsDynamicEntryItem == false )
		//
		&& ( mIsCategoryOperateItem == false )
		//xiatian add end
		//
		&& ( !mZhiKeShortcutManager.isZhiKeShortcut( intent.getComponent() ) )//cheyingkun add	//配置可以通过广播删除的快捷方式【智科】【c_0004445】(应用名称不变)
		)
		{
			//cheyingkun add start	//解决“小部件设置快捷方式，切换语言待机界面小部件名称不变”的问题【c_0003414】
			CharSequence newtitle = getShortcutInfoName( context.getPackageManager() , intent , operateIntent );
			if( newtitle != null )
			{
				mTitle = newtitle.toString();
			}
			//cheyingkun add end
		}
		info.setTitle( mTitle );
		//xiatian add start	//需求：添加配置项“mIsIconFollowTheme”，虚图标的显示图标是否跟随主题（从主题中读取相应图标）。true为跟随主题；false为不跟随主题。默认为true。
		if( itemType == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL )
		{
			boolean mIsIconFollowTheme = intent.getBooleanExtra( VirtualInfo.IS_ICON_FOLLOW_THEME , true );
			if( mIsIconFollowTheme )
			{//优先到主题中找图标。找不到的话，再读取配置的图标。（【备注】这里会出现以下问题：如果这个activity没配置图标，则会显示apk的图标。）
				icon = mIconCache.getIcon( intent.getComponent() , mTitle );
			}
		}
		//zhujieping add start	//对于配置了图片的ShortcutInfo和VirtualInfo，需要保存图片相关信息
		int iconType = c.getInt( iconTypeIndex );
		if( iconType == LauncherSettings.Favorites.ICON_TYPE_RESOURCE )
		{
			String packageName = c.getString( iconPackageIndex );
			String resourceName = c.getString( iconResourceIndex );
			iconResource.packageName = packageName;
			iconResource.resourceName = resourceName;
			info.setIconResource( iconResource );
		}
		//zhujieping add end
		if( icon == null )
		//xiatian add end
		{
			switch( iconType )
			{
				case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
					String packageName = c.getString( iconPackageIndex );
					String resourceName = c.getString( iconResourceIndex );
					PackageManager packageManager = context.getPackageManager();
					info.setIsCustomIcon( false );
					//
					//添加智能分类功能 , change by shlt@2015/02/10 ADD START
					//<数据库字段更新> liuhailin@2015-03-24 modify begin
					if( operateIntent != null )
					{
						//boolean isOperateVirtualItem = intent.getBooleanExtra( "isOperateVirtualItem" , false );
						boolean isOperateVirtualItem = operateIntent.getBooleanExtra( EnhanceItemInfo.INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM , false );
						//<数据库字段更新> liuhailin@2015-03-24 modify end
						String iconPath = operateIntent.getStringExtra( "iconPath" );
						if( isOperateVirtualItem )
						{
							ComponentName comp = new ComponentName( intent.getPackage() , iconPath );
							icon = mIconCache.getVirtualIcon( comp , iconPath );
						}
					}
					//添加智能分类功能 , change by shlt@2015/02/10 ADD END
					// the resource
					if( icon == null )
					{
						try
						{
							Resources resources = packageManager.getResourcesForApplication( packageName );
							if( resources != null )
							{
								final int id = resources.getIdentifier( resourceName , null , null );
								float mIconDestMinWidth = 0f;
								float mIconDestMinHeight = 0f;
								//xiatian add start	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
								//对根据id反射获取到的资源，加背板、盖板和蒙版
								//							icon = Utilities.createIconBitmap( mIconCache.getFullResIcon( resources , id , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight ) , context );//xiatian del
								if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
								{
									mIconDestMinWidth = Utilities.sIconWidth;
									mIconDestMinHeight = Utilities.sIconHeight;
								}
								else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
								{
									mIconDestMinWidth = Utilities.sIconWidth * LauncherDefaultConfig.ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE;
									mIconDestMinHeight = Utilities.sIconWidth * LauncherDefaultConfig.ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE;
								}
								if( //
								intent.getComponent() != null //cheyingkun add	//解决“音乐播放器1*1插件添加后重启桌面，插件消失”的问题。
										&& intent.getComponent().getClassName().equals( "com.cooee.wallpaper.host.WallpaperMainActivity" ) )//一键换壁纸不跟随主题变化
								{
									icon = Utilities.createIconBitmap(
											mIconCache.getFullResIcon( resources , id , mIconDestMinWidth , mIconDestMinHeight ) ,
											context ,
											Utilities.sIconWidth ,
											Utilities.sIconHeight ,
											Utilities.sIconTextureWidth ,
											Utilities.sIconTextureHeight ,
											true );
								}
								else
								{
									icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem(
											mIconCache.getFullResIcon( resources , id , mIconDestMinWidth , mIconDestMinHeight ) ,
											context ,
											false );
								}
								//xiatian add end
							}
						}
						catch( Exception e )
						{
							// drop this.  we have other places to look for icons
						}
					}
					// the db
					if( icon == null )
					{
						icon = getIconFromCursor( c , iconIndex , context );
						if( //
						intent.getComponent() == null //cheyingkun add	//解决“音乐播放器1*1插件添加后重启桌面，插件消失”的问题。
								|| !intent.getComponent().getClassName().equals( "com.cooee.wallpaper.host.WallpaperMainActivity" ) )
						{
							icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( icon , context , true );//xiatian add	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
						}
					}
					// the fallback icon
					if( icon == null )
					{
						//xiatian start	//优化桌面启动速度，mDefaultIcon不应该在LauncherModel的初始化中生成，应该在需要使用的时候生成。
						//			icon = getFallbackIcon();//xiatian del
						icon = getFallbackIcon( context );//xiatian add
						//xiatian end
						icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( icon , context , false );//xiatian add	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
						info.setIsUsingFallbackIcon( true );
					}
					break;
				case LauncherSettings.Favorites.ICON_TYPE_BITMAP:
					icon = getIconFromCursor( c , iconIndex , context );
					if( icon == null )
					{
						//xiatian start	//优化桌面启动速度，mDefaultIcon不应该在LauncherModel的初始化中生成，应该在需要使用的时候生成。
						//			icon = getFallbackIcon();//xiatian del
						icon = getFallbackIcon( context );//xiatian add
						//xiatian end
						icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( icon , context , false );//xiatian add	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
						info.setIsCustomIcon( false );
						info.setIsUsingFallbackIcon( true );
					}
					else
					{
						info.setIsCustomIcon( true );
						icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( icon , context , true );//xiatian add	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
					}
					break;
				default:
					//xiatian start	//优化桌面启动速度，mDefaultIcon不应该在LauncherModel的初始化中生成，应该在需要使用的时候生成。
					//			icon = getFallbackIcon();//xiatian del
					icon = getFallbackIcon( context );//xiatian add
					//xiatian end
					icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( icon , context , true );//xiatian add	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
					info.setIsUsingFallbackIcon( true );
					info.setIsCustomIcon( false );
					break;
			}
		}
		info.setIcon( icon );
		//xiatian add start	//fix bug：解决“智能分类文件夹中的推荐应用，点击后无法下载（提示应用未安装）”的问题。
		//【问题原因】没有将数据库中读取的相关参数赋值给ShortcutInfo的operateIntent，从而导致无法判断该ShortcutInfo的相关属性。
		//【解决方案】将数据库中读取的相关参数赋值给ShortcutInfo的operateIntent
		if( operateIntent != null )
		{
			info.setOperateIntent( operateIntent );
		}
		//xiatian add end
		return info;
	}
	
	Bitmap getIconFromCursor(
			Cursor c ,
			int iconIndex ,
			Context context )
	{
		@SuppressWarnings( "all" )
		// suppress dead code warning
		final boolean debug = false;
		if( debug )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "getIconFromCursor app=" , c.getString( c.getColumnIndexOrThrow( LauncherSettings.Favorites.TITLE ) ) ) );
		}
		byte[] data = c.getBlob( iconIndex );
		try
		{
			//xiatian start	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
			//从数据库读取的图片，要按照原有尺寸生成图片，否则会模糊（原图尺寸小于目标尺寸）
			//			return Utilities.createIconBitmap( BitmapFactory.decodeByteArray( data , 0 , data.length ) , context );//xiatian del
			//xiatian add start
			Bitmap mBitmap = BitmapFactory.decodeByteArray( data , 0 , data.length );
			return Utilities.createIconBitmap( mBitmap , context , mBitmap.getWidth() , mBitmap.getHeight() , mBitmap.getWidth() , mBitmap.getHeight() , true , true );
			//xiatian add end
			//xiatian end
		}
		catch( Exception e )
		{
			return null;
		}
	}
	
	/**
	 * Attempts to find an AppWidgetProviderInfo that matches the given component.
	 */
	AppWidgetProviderInfo findAppWidgetProviderInfoWithComponent(
			Context context ,
			ComponentName component )
	{
		List<AppWidgetProviderInfo> widgets = AppWidgetManager.getInstance( context ).getInstalledProviders();
		for( AppWidgetProviderInfo info : widgets )
		{
			if( info.provider.equals( component ) )
			{
				return info;
			}
		}
		return null;
	}
	
	ShortcutInfo infoFromShortcutIntent(
			Context context ,
			Intent data ,
			Bitmap fallbackIcon )
	{
		Intent intent = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_INTENT );
		String name = data.getStringExtra( Intent.EXTRA_SHORTCUT_NAME );
		Parcelable bitmap = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_ICON );
		if( intent == null )
		{
			// If the intent is null, we can't construct a valid ShortcutInfo, so we return null
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "Can't construct ShorcutInfo with null intent" );
			return null;
		}
		Bitmap icon = null;
		boolean customIcon = false;
		ShortcutIconResource iconResource = null;
		if( bitmap != null && bitmap instanceof Bitmap )
		{
			Drawable mDrawable = new FastBitmapDrawable( (Bitmap)bitmap );
			//xiatian start	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
			//			icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( new FastBitmapDrawable( (Bitmap)bitmap ) , context );//xiatian del
			//按照原图大小生成图片，加背板、盖板和蒙版放在存入数据库之后
			icon = Utilities.createIconBitmap(
					mDrawable ,
					context ,
					mDrawable.getIntrinsicWidth() ,
					mDrawable.getIntrinsicHeight() ,
					mDrawable.getIntrinsicWidth() ,
					mDrawable.getIntrinsicHeight() ,
					true );//xiatian add
			//xiatian end
			customIcon = true;
		}
		else
		{
			Parcelable extra = data.getParcelableExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE );
			if( extra != null && extra instanceof ShortcutIconResource )
			{
				try
				{
					iconResource = (ShortcutIconResource)extra;
					final PackageManager packageManager = context.getPackageManager();
					Resources resources = packageManager.getResourcesForApplication( iconResource.packageName );
					final int id = resources.getIdentifier( iconResource.resourceName , null , null );
					float mIconDestMinWidth = 0f;
					float mIconDestMinHeight = 0f;
					//xiatian start	//系统自动生成的1X1快捷方式图标和用户手动添加的1X1快捷方式图标，添加背板、盖板和蒙版。
					//					icon = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( mIconCache.getFullResIcon( resources , id , Utilities.sIconTextureWidth , Utilities.sIconTextureHeight ) , context );//xiatian del
					//xiatian add start
					if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
					{
						mIconDestMinWidth = Utilities.sIconWidth;
						mIconDestMinHeight = Utilities.sIconHeight;
					}
					else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
					{
						mIconDestMinWidth = Utilities.sIconWidth * LauncherDefaultConfig.ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE;
						mIconDestMinHeight = Utilities.sIconWidth * LauncherDefaultConfig.ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE;
					}
					Drawable mDrawable = mIconCache.getFullResIcon( resources , id , mIconDestMinWidth , mIconDestMinHeight );
					//按照原图大小生成图片，加背板、盖板和蒙版放在存入数据库之后
					icon = Utilities.createIconBitmap(
							mDrawable ,
							context ,
							mDrawable.getIntrinsicWidth() ,
							mDrawable.getIntrinsicHeight() ,
							mDrawable.getIntrinsicWidth() ,
							mDrawable.getIntrinsicHeight() ,
							true );
					//xiatian add end
					//xiatian end
				}
				catch( Exception e )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.w( TAG , StringUtils.concat( "Could not load shortcut icon: " , extra.toString() ) );
				}
			}
		}
		final ShortcutInfo info = new ShortcutInfo();
		if( icon == null )
		{
			if( fallbackIcon != null )
			{
				icon = fallbackIcon;
			}
			else
			{
				//xiatian start	//优化桌面启动速度，mDefaultIcon不应该在LauncherModel的初始化中生成，应该在需要使用的时候生成。
				//			icon = getFallbackIcon();//xiatian del
				icon = getFallbackIcon( context );//xiatian add
				//xiatian end
				info.setIsUsingFallbackIcon( true );
			}
		}
		info.setIcon( icon );
		info.setTitle( name );
		info.setIntent( intent );
		info.setIsCustomIcon( customIcon );
		info.setIconResource( iconResource );
		return info;
	}
	
	boolean queueIconToBeChecked(
			HashMap<Object , byte[]> cache ,
			ShortcutInfo info ,
			Cursor c ,
			int iconIndex )
	{
		// If apps can't be on SD, don't even bother.
		if( !mAppsCanBeOnRemoveableStorage )
		{
			return false;
		}
		// If this icon doesn't have a custom icon, check to see
		// what's stored in the DB, and if it doesn't match what
		// we're going to show, store what we are going to show back
		// into the DB.  We do this so when we're loading, if the
		// package manager can't find an icon (for example because
		// the app is on SD) then we can use that instead.
		if( !info.getIsCustomIcon() && !info.getIsUsingFallbackIcon() )
		{
			cache.put( info , c.getBlob( iconIndex ) );
			return true;
		}
		return false;
	}
	
	void updateSavedIcon(
			Context context ,
			ShortcutInfo info ,
			byte[] data )
	{
		boolean needSave = false;
		try
		{
			if( data != null )
			{
				Bitmap saved = BitmapFactory.decodeByteArray( data , 0 , data.length );
				Bitmap loaded = info.getIcon( mIconCache );
				needSave = !saved.sameAs( loaded );
			}
			else
			{
				needSave = true;
			}
		}
		catch( Exception e )
		{
			needSave = true;
		}
		if( needSave )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "going to save icon bitmap for info=" + info );
			// This is slower than is ideal, but this only happens once
			// or when the app is updated with a new icon.
			updateItemInDatabase( context , info );
		}
	}
	
	/**
	 * Return an existing FolderInfo object if we have encountered this ID previously,
	 * or make a new one.
	 */
	private static FolderInfo findOrMakeFolder(
			HashMap<Long , FolderInfo> folders ,
			long id )
	{
		// See if a placeholder was created for us already
		FolderInfo folderInfo = folders.get( id );
		if( folderInfo == null )
		{
			// No placeholder -- create a new instance
			folderInfo = new FolderInfo();
			folders.put( id , folderInfo );
		}
		return folderInfo;
	}
	
	public static final Comparator<AppInfo> getAppNameComparator()
	{
		final Collator collator = Collator.getInstance();
		return new Comparator<AppInfo>() {
			
			public final int compare(
					AppInfo a ,
					AppInfo b )
			{
				if( !( a instanceof AppInfo && b instanceof AppInfo ) )
				{
					return 0;
				}
				//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
				//int result = collator.compare( a.title.toString().trim() , b.title.toString().trim() );
				if( a.getTitle() == null )
				{
					a.setTitle( "name = null" );
				}
				int result = collator.compare( a.getTitle().toString().trim() , b.getTitle().toString().trim() );
				//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
				if( result == 0 )
				{
					result = a.getComponentName().compareTo( b.getComponentName() );
				}
				return result;
			}
		};
	}
	
	//zhujieping@2015/03/13 ADD start
	private class ShortcutReplaceIconComparator implements Comparator<ItemInfo>
	{
		
		@Override
		public int compare(
				ItemInfo lhs ,
				ItemInfo rhs )
		{
			// TODO Auto-generated method stub
			if( !( lhs instanceof AppInfo && rhs instanceof AppInfo ) )
			{
				return 0;
			}
			boolean lhasReplace = LauncherIconBaseConfig.hasReplaceIcon( ( (AppInfo)lhs ).getComponentName().getPackageName() , ( (AppInfo)lhs ).getComponentName().getClassName() );
			boolean rhasReplace = LauncherIconBaseConfig.hasReplaceIcon( ( (AppInfo)rhs ).getComponentName().getPackageName() , ( (AppInfo)rhs ).getComponentName().getClassName() );
			if( lhasReplace && !rhasReplace )
			{
				return -1;
			}
			else if( !lhasReplace && rhasReplace )
			{
				return 1;
			}
			return 0;
		}
	}
	
	// zhujieping@2015/03/13 ADD END
	public static final Comparator<AppInfo> APP_INSTALL_TIME_COMPARATOR = new Comparator<AppInfo>() {
		
		public final int compare(
				AppInfo a ,
				AppInfo b )
		{
			if( !( a instanceof AppInfo && b instanceof AppInfo ) )
			{
				return 0;
			}
			if( a.getFirstInstallTime() < b.getFirstInstallTime() )
				return 1;
			if( a.getFirstInstallTime() > b.getFirstInstallTime() )
				return -1;
			return 0;
		}
	};
	
	public static final Comparator<AppWidgetProviderInfo> getWidgetNameComparator()
	{
		final Collator collator = Collator.getInstance();
		return new Comparator<AppWidgetProviderInfo>() {
			
			public final int compare(
					AppWidgetProviderInfo a ,
					AppWidgetProviderInfo b )
			{
				if( !( a instanceof AppWidgetProviderInfo && b instanceof AppWidgetProviderInfo ) )
				{
					return 0;
				}
				return collator.compare( a.label.toString().trim() , b.label.toString().trim() );
			}
		};
	}
	
	static ComponentName getComponentNameFromResolveInfo(
			ResolveInfo info )
	{
		if( info.activityInfo != null )
		{
			return new ComponentName( info.activityInfo.packageName , info.activityInfo.name );
		}
		else
		{
			return new ComponentName( info.serviceInfo.packageName , info.serviceInfo.name );
		}
	}
	
	public static class ShortcutNameComparator implements Comparator<ResolveInfo>
	{
		
		private Collator mCollator;
		private PackageManager mPackageManager;
		private HashMap<Object , CharSequence> mLabelCache;
		
		ShortcutNameComparator(
				PackageManager pm )
		{
			mPackageManager = pm;
			mLabelCache = new HashMap<Object , CharSequence>();
			mCollator = Collator.getInstance();
		}
		
		ShortcutNameComparator(
				PackageManager pm ,
				HashMap<Object , CharSequence> labelCache )
		{
			mPackageManager = pm;
			mLabelCache = labelCache;
			mCollator = Collator.getInstance();
		}
		
		public final int compare(
				ResolveInfo a ,
				ResolveInfo b )
		{
			if( !( a instanceof ResolveInfo && b instanceof ResolveInfo ) )
			{
				return 0;
			}
			CharSequence labelA , labelB;
			ComponentName keyA = LauncherModel.getComponentNameFromResolveInfo( a );
			ComponentName keyB = LauncherModel.getComponentNameFromResolveInfo( b );
			if( mLabelCache.containsKey( keyA ) )
			{
				labelA = mLabelCache.get( keyA );
			}
			else
			{
				labelA = a.loadLabel( mPackageManager ).toString().trim();
				labelA = Tools.appTitleFineTune( labelA.toString() );//cheyingkun add	//应用名称逻辑完善(在所有Cache.put的时候,都先经过名称处理这段逻辑)【c_0004365】
				mLabelCache.put( keyA , labelA );
			}
			if( mLabelCache.containsKey( keyB ) )
			{
				labelB = mLabelCache.get( keyB );
			}
			else
			{
				labelB = b.loadLabel( mPackageManager ).toString().trim();
				labelB = Tools.appTitleFineTune( labelB.toString() );//cheyingkun add	//应用名称逻辑完善(在所有Cache.put的时候,都先经过名称处理这段逻辑)【c_0004365】
				mLabelCache.put( keyB , labelB );
			}
			return mCollator.compare( labelA , labelB );
		}
	};
	
	public static class WidgetAndShortcutNameComparator implements Comparator<Object>
	{
		
		private Collator mCollator;
		private PackageManager mPackageManager;
		private HashMap<Object , String> mLabelCache;
		
		WidgetAndShortcutNameComparator(
				PackageManager pm )
		{
			mPackageManager = pm;
			mLabelCache = new HashMap<Object , String>();
			mCollator = Collator.getInstance();
		}
		
		public final int compare(
				Object a ,
				Object b )
		{
			if( !( a instanceof Object && b instanceof Object ) )
			{
				return 0;
			}
			String labelA , labelB;
			if( mLabelCache.containsKey( a ) )
			{
				labelA = mLabelCache.get( a );
			}
			else
			{
				labelA = ( a instanceof AppWidgetProviderInfo ) ? ( (AppWidgetProviderInfo)a ).label : ( (ResolveInfo)a ).loadLabel( mPackageManager ).toString().trim();
				labelA = Tools.appTitleFineTune( labelA );//cheyingkun add	//应用名称逻辑完善(在所有Cache.put的时候,都先经过名称处理这段逻辑)【c_0004365】
				mLabelCache.put( a , labelA );
			}
			if( mLabelCache.containsKey( b ) )
			{
				labelB = mLabelCache.get( b );
			}
			else
			{
				labelB = ( b instanceof AppWidgetProviderInfo ) ? ( (AppWidgetProviderInfo)b ).label : ( (ResolveInfo)b ).loadLabel( mPackageManager ).toString().trim();
				labelB = Tools.appTitleFineTune( labelB );//cheyingkun add	//应用名称逻辑完善(在所有Cache.put的时候,都先经过名称处理这段逻辑)【c_0004365】
				mLabelCache.put( b , labelB );
			}
			return mCollator.compare( labelA , labelB );
		}
	};
	
	public void dumpState()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( TAG , "mCallbacks=" + mCallbacks );
			AppInfo.dumpApplicationInfoList( TAG , "mAllAppsList.data" , mBgAllAppsList.data );
			AppInfo.dumpApplicationInfoList( TAG , "mAllAppsList.added" , mBgAllAppsList.added );
			AppInfo.dumpApplicationInfoList( TAG , "mAllAppsList.removed" , mBgAllAppsList.removed );
			AppInfo.dumpApplicationInfoList( TAG , "mAllAppsList.modified" , mBgAllAppsList.modified );
			if( mLoaderTask != null )
			{
				mLoaderTask.dumpState();
			}
			else
			{
				Log.d( TAG , "mLoaderTask=null" );
			}
		}
	}
	
	//智能分类添加运营 , change by shlt@2014/12/23 ADD START
	public HashMap<Long , ItemInfo> getBgItemsMap()
	{
		return sBgItemsIdMap;
	}
	
	public HashMap<Long , FolderInfo> getBgFolders()
	{
		return sBgFolders;
	}
	
	public ArrayList<ItemInfo> getBgWorkspaceItems()
	{
		return sBgWorkspaceItems;
	}
	
	public ArrayList<LauncherAppWidgetInfo> getBgAppWidgets()
	{
		return sBgAppWidgets;
	}
	
	public ArrayList<Long> getBgWorkspaceScreens()
	{
		return sBgWorkspaceScreens;
	}
	//智能分类添加运营 , change by shlt@2014/12/23 ADD END
	;
	
	public boolean isLoaderTaskRunning()
	{
		return mIsLoaderTaskRunning;
	}
	
	/**
	 * 这个方法实现了将拥有默认内置Icon的应用（例如电子邮件、联系人、信息等）放在所有应用集合最前面位置的功能
	 * 
	 * 这个功能原来是通过 ShortcutReplaceIconComparator这个类实现的，但是效率实在太差，平均耗时1s多
	 * 使用这个方法后平均耗时200ms，效率提升非常明显
	 * 
	 * 目前主要在两个地方被使用到：
	 * 1.初始化的时候
	 * 2.从单层模式切换到双层模式的时候
	 * 
	 * @param apps 所有应用的集合
	 * @return ArrayList<T> 实现功能后的集合
	 * @author yangxiaoming
	 * @date 2015/05/28
	 */
	public static <T extends ItemInfo>ArrayList<T> sortAppByDefaultIcon(
			ArrayList<T> apps )
	{
		// 这是我们将要返回的集合
		ArrayList<T> app_list = new ArrayList<T>();
		// 这个集合用来储存那些没有默认图标的app的ItemInfo
		ArrayList<T> app_list_without_default_icon = new ArrayList<T>();
		// yangxiaoming delete start 使用这种方法排序非常影响效率，这个排序其实就是找出有默认icon的app放置在桌面最前面的位置 2015/05/25
		// Collections.sort( added , new ShortcutReplaceIconComparator() );
		// yangxiaoming delete end
		// yangxiaoming add start 改用传统的List方法排序，效率提高明显 2015/05/25
		for( int i = 0 ; i < apps.size() ; i++ )
		{
			T info = apps.get( i );
			// 强制转化前先判断
			if( info instanceof AppInfo )
			{
				boolean b = LauncherIconBaseConfig.hasReplaceIcon( ( (AppInfo)info ).getComponentName().getPackageName() , ( (AppInfo)info ).getComponentName().getClassName() );
				if( b )
					app_list.add( info );
				else
					app_list_without_default_icon.add( info );
			}
		}
		app_list.addAll( app_list_without_default_icon );
		// yangxiaoming add end
		return app_list;
	}
	
	//cheyingkun add start	//TCardMountCancelDrag(收到T卡挂载和安装广播停止拖拽)
	//	public void cancelDrag()
	//	{
	//		DragController mDragController = null;
	//		if( mLauncher != null )
	//		{
	//			mDragController = mLauncher.getDragController();
	//			if( mDragController != null )
	//			{
	//				mDragController.cancelDrag();
	//				mDragController.resetLastGestureUpTime();
	//			}
	//		}
	//	}
	//cheyingkun add end
	//cheyingkun add start	//解决“卸载T卡灰色图标状态下，再次安装灰色图标应用，应用被安装在系统内存，能打开但是图标没有变亮。”的问题（bug：0010346）
	//cheyingkun add start	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
	/**
	 * 桌面加载结束后,开始执行加载期间受到的task
	 */
	public void startTaskOnLoaderTaskFinish()
	{
		synchronized( sWorker )
		{
			if( mTCardMountOpInLoad != null && mTCardMountOpInLoad.size() > 0 )
			{
				for( PackageUpdatedTask task : mTCardMountOpInLoad )
				{
					sWorker.post( task );
				}
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "TCardMount" , "mTCardMountOpInLoad " );
				mTCardMountOpInLoad.clear();
			}
		}
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//解决“小部件设置快捷方式，切换语言待机界面小部件名称不变”的问题【c_0003414】
	private CharSequence getShortcutInfoName(
			PackageManager manager ,
			Intent intent ,
			Intent operateIntent )
	{
		ComponentName componentName = intent.getComponent();
		if( componentName == null || //cheyingkun add	//解决“桌面上没有包类名的1*1小插件(如"直接拨打电话"和"直接发送短信"),重启桌面后消失”问题【i_0012352】
		!isValidPackageComponent( manager , intent , operateIntent ) )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "Invalid package found in getShortcutInfo 6236 --intent: " , intent.toUri( 0 ) ) );
			return null;
		}
		CharSequence title = null;
		ResolveInfo resolveInfo = null;
		ComponentName oldComponent = intent.getComponent();
		Intent newIntent = new Intent( intent.getAction() , null );
		newIntent.addCategory( Intent.CATEGORY_LAUNCHER );
		newIntent.setPackage( oldComponent.getPackageName() );
		List<ResolveInfo> infos = manager.queryIntentActivities( newIntent , 0 );
		for( ResolveInfo i : infos )
		{
			ComponentName cn = new ComponentName( i.activityInfo.packageName , i.activityInfo.name );
			if( cn.equals( oldComponent ) )
			{
				resolveInfo = i;
			}
		}
		if( resolveInfo == null )
		{
			resolveInfo = manager.resolveActivity( intent , 0 );
		}
		if( resolveInfo != null )
		{
			title = resolveInfo.activityInfo.loadLabel( manager );
		}
		return title;
	}
	
	//cheyingkun add end
	//cheyingkun add start	//应用自动创建快捷方式时，优化判断是否存在的方法。【c_0003813】
	/**
	 * 方法“static boolean shortcutExistsIntensify(Context context ,String title ,Intent intent )”的强化版。
	 * 比较创建快捷方式的title和包类名(跟workspace上的图标比)
	 */
	boolean shortcutExistsByWorkspaceItems(
			String name ,
			Intent intent )
	{
		final Callbacks callbacks = mCallbacks.get();
		if( callbacks != null )
		{
			return callbacks.shortcutExistsByWorkspaceItems( name , intent );
		}
		return false;
	}
	
	//cheyinkgun add end
	//cheyingkun add start	//解决“卸载应用后再安装应用，酷生活推荐应用显示应用原始图标”的问题。【i_0013740】
	public HashMap<ComponentName , Bitmap> getAllIcons()
	{
		if( mBgAllAppsList != null && mBgAllAppsList.data != null )
		{
			synchronized( mBgAllAppsList )
			{
				if( mBgAllAppsList != null && mBgAllAppsList.data != null )
				{
					HashMap<ComponentName , Bitmap> set = new HashMap<ComponentName , Bitmap>();
					for( AppInfo info : mBgAllAppsList.data )
					{
						ComponentName componentName = info.getComponentName();
						Bitmap iconBitmap = info.getIconBitmap();
						if( componentName != null && iconBitmap != null//
								&& !iconBitmap.isRecycled() )//cheyingkun add	//解决“常用应用显示动态时，改变日期后返回桌面，桌面重启”的问题【c_0004419】
						{
							//cheyingkun add start	//解决“常用应用显示动态时，改变日期后返回桌面，桌面重启”的问题【c_0004419】
							//如果是动态图标,复制一份图标传过去,防止动态图标被释放引起桌面异常
							if( IconHouseManager.getInstance().isIconHouse( componentName ) )
							{
								info.setIconBitmapBackup( Bitmap.createBitmap( iconBitmap ) );
								iconBitmap = info.getIconBitmapBackup();
							}
							//cheyingkun add end
							set.put( componentName , iconBitmap );
						}
					}
					return set;
				}
			}
		}
		return null;
	}
	
	//cheyingkun add end
	// zhangjin@2015/12/10 ADD START
	public void onAddUpdateIcon()
	{
		List<ResolveInfo> matches = UpdateIconManager.getInstance().getUpdateResolveInfo();
		if( matches == null || matches.size() == 0 )
		{
			return;
		}
		final Context context = mApp.getContext();
		for( ResolveInfo info : matches )
		{
			mBgAllAppsList.add( new AppInfo( context.getPackageManager() , info , mIconCache , null ) );
		}
		if( mBgAllAppsList.added.size() > 0 )
		{
			ArrayList<AppInfo> added = mBgAllAppsList.added;
			mBgAllAppsList.added = new ArrayList<AppInfo>();
			Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
			if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
			{
				addAndBindAddedItems( context , new ArrayList<ItemInfo>() , null , cb , added , true , !mIsLoaderTaskRunning );
			}
			else
			{
				final ArrayList<ItemInfo> addedInfos = new ArrayList<ItemInfo>( added );
				addAndBindAddedItems( context , addedInfos , null , cb , added , true , !mIsLoaderTaskRunning );
			}
		}
	}
	
	public void onRemoveUpdateIcon()
	{
		List<ResolveInfo> matches = UpdateIconManager.getInstance().getUpdateResolveInfo();
		if( matches == null || matches.size() == 0 )
		{
			return;
		}
		final Context context = mApp.getContext();
		for( ResolveInfo info : matches )
		{
			mBgAllAppsList.remove( new AppInfo( context.getPackageManager() , info , mIconCache , null ) );
		}
		if( mBgAllAppsList.removed.size() > 0 )
		{
			final ArrayList<AppInfo> removed = mBgAllAppsList.removed;
			mBgAllAppsList.removed = new ArrayList<AppInfo>();
			for( AppInfo a : removed )
			{
				ArrayList<ItemInfo> infos = getItemInfoForComponentName( a.getComponentName() );
				for( ItemInfo i : infos )
				{
					deleteItemFromDatabase( context , i );
				}
			}
			final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
			final ArrayList<String> removedPackageNames = new ArrayList<String>();
			removedPackageNames.add( ( context.getPackageName() ) );
			mHandler.post( new Runnable() {
				
				public void run()
				{
					Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
					if( callbacks == cb && cb != null )
					{
						callbacks.bindComponentsRemoved( removedPackageNames , removed , false );//哈哈
					}
				}
			} );
		}
	}
	
	// zhangjin@2015/12/10 ADD END
	private boolean isHideSimCard(
			String packageName )
	{
		// chenchen add start //从数组中获取到需要移除的包
		if( BaseDefaultConfig.mHideSimCardList.size() > 0 )
		{
			boolean mate = false;
			for( int i = 0 ; i < BaseDefaultConfig.mHideSimCardList.size() ; i++ )
			{
				if( BaseDefaultConfig.mHideSimCardList.get( i ).getPackageName().equals( packageName ) )
				{
					mate = true;
				}
			}
			if( !mate )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "lvjiangbin" , StringUtils.concat( "isHideSimCard  pk = " , packageName ) );
				return false;
			}
		}
		int hideSimCardListSize = BaseDefaultConfig.mHideSimCardList.size();
		boolean[] simType = Tools.spreadSimReady( mApp.getContext() );
		if( simType.length > 0 )
		{
			for( int j = 0 ; j < simType.length && j < hideSimCardListSize ; j++ )
			{
				if( !simType[j] && BaseDefaultConfig.mHideSimCardList.get( j ).getPackageName().equals( packageName ) )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private void updateSimCard()
	{
		int hideSimCardListSize = BaseDefaultConfig.mHideSimCardList.size();
		boolean[] simType = Tools.spreadSimReady( mApp.getContext() );
		int myOp = PackageUpdatedTask.OP_ADD;
		for( int j = 0 ; j < simType.length && j < hideSimCardListSize ; j++ )
		{
			if( simType[j] && BaseDefaultConfig.mHideSimCardList.get( j ).getPackageName() != null && !BaseDefaultConfig.mHideSimCardList.get( j ).getPackageName().equals( "" ) )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "lvjiangbin" , StringUtils.concat( "updateSimCard = " , j ) );
				PackageUpdatedTask task = new PackageUpdatedTask( myOp , new String[]{ BaseDefaultConfig.mHideSimCardList.get( j ).getPackageName() } );
				enqueuePackageUpdated( task );
			}
		}
	}
	
	//zhujieping add start，这个方法是批量往数据库中加入info，这样比一个一个添加要节省时间
	public static void addItemsToDatabase(
			Context context ,
			final ArrayList<ItemInfo> lists )
	{
		final ContentResolver cr = context.getContentResolver();
		final ContentValues[] valuesList = new ContentValues[lists.size()];
		for( int index = 0 ; index < lists.size() ; index++ )
		{
			ItemInfo item = lists.get( index );
			ContentValues values = new ContentValues();
			item.onAddToDatabase( values );
			item.setId( LauncherAppState.getLauncherProvider().generateNewItemId() );
			values.put( LauncherSettings.Favorites._ID , item.getId() );
			item.updateValuesWithCoordinates( values , item.getCellX() , item.getCellY() );
			valuesList[index] = values;
		}
		Runnable r = new Runnable() {
			
			public void run()
			{
				cr.bulkInsert( LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION , valuesList );
				// Lock on mBgLock *after* the db operation
				for( ItemInfo item : lists )
					synchronized( sBgLock )
					{
						checkItemInfoLocked( item.getId() , item , null );
						sBgItemsIdMap.put( item.getId() , item );
						switch( item.getItemType() )
						{
							case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
								sBgFolders.put( item.getId() , (FolderInfo)item );//生成文件夹
								// Fall through
							case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
							case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
								if( item.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP || item.getContainer() == LauncherSettings.Favorites.CONTAINER_HOTSEAT )
								{
									sBgWorkspaceItems.add( item );
								}
								else
								{
									if( !sBgFolders.containsKey( item.getContainer() ) )
									{
										if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
										{
											// Adding an item to a folder that doesn't exist.
											String msg = "adding item to a folder but folder doesn't exist!!item is=" + item;
											Log.e( TAG , msg );
										}
									}
								}
								break;
							case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
								sBgAppWidgets.add( (LauncherAppWidgetInfo)item );
								break;
						}
					}
			}
		};
		runOnWorkerThread( r );
	}
	
	//zhujieping add end
	//gaominghui add start //解决"当主页面上只有小组件，智能分类后，该页面空白"的问题【i_0014888】。
	private ArrayList<Long> queryScreenIdList(
			int defaultScreenIndex )
	{
		ArrayList<Long> screenIdList = new ArrayList<Long>();
		SQLiteDatabase db = LauncherAppState.getLauncherProvider().getProviderDB();
		String sql = StringUtils.concat( "SELECT _id FROM " , LauncherProvider.DatabaseHelper.getWorkspacesTabName() , " WHERE screenRank <= " , String.valueOf( defaultScreenIndex ) );
		Cursor c = db.rawQuery( sql , null );
		long screenId = -1;
		while( c != null && c.moveToNext() )
		{
			screenId = c.getLong( c.getColumnIndexOrThrow( "_id" ) );
			screenIdList.add( screenId );
		}
		if( c != null )
		{
			c.close();
		}
		return screenIdList;
	}
	//gaominghui add end
}
