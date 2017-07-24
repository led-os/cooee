package com.cooee.phenix.Functions.Category;


import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.cooee.framework.function.Category.CategoryInstallActivity;
import com.cooee.framework.function.Category.CategoryParse;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.framework.utils.ResourceUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.AllAppsList;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherModel;
import com.cooee.phenix.LauncherProvider;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicMain;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.EnhanceItemInfo;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.phenix.data.VirtualInfo;
import com.cooee.util.MapUtils;
import com.cooee.util.MapUtils.MapTraversalCallBack;
import com.cooee.util.NetWorkUtils;
import com.cooee.util.NotificationUtils;
import com.cooee.util.NotificationUtils.NotificationInfo;
import com.cooee.util.ToastUtils;
import com.iLoong.launcher.MList.MainActivity;
import com.iLoong.launcher.MList.MeLauncherInterface;

import cool.sdk.Category.CaregoryReqCallBack;
import cool.sdk.Category.CaregoyReqType;
import cool.sdk.Category.CategoryConstant;
import cool.sdk.Category.CategoryHelper;
import cool.sdk.Category.CategoryUpdate;
import cool.sdk.Category.DictData;
import cool.sdk.Category.RecommendApkInfo;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


// 智能分类添加运营 , change by shlt@2014/12/19 ADD START
public class OperateHelp implements CategoryHelper.CategoryListener
{
	
	private static OperateHelp mInstance = null;
	private boolean gettingOperateData = false;
	//
	private Map<String , ShortcutInfo> downloadAppItemInfos = new HashMap<String , ShortcutInfo>();
	//cheyingkun add start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
	private static Context mContext = null;
	private CustomProgressDialog progressDialog = null;
	private CategoryListener mListener = null;
	//cheyingkun add end
	public final static String ClassificationTime = "classificationTime";
	private boolean mHasStartOperateFolderRun = false;//判断在开始智能分类的时候，运营文件夹是否在更新 wanghongjian add
	private boolean mHasStopOperateFolderRun = false;//判断在结束智能分类的时候，运营文件夹是否在更新 wanghongjian add
	private final String CATEGORY_ICON_RESOURCE_NAME = "default_config_category";
	private final String CATEGORY_TITEL_RESOURCE_NAME = "intelligent_classification";
	private static final String CATEGORY_INTENT_URI = "#Intent;action=android.intent.action.MAIN;component=com.cooee.phenix/com.cooee.phenix.categoryShortcut;B.mIsCanUninstall=false;i.mVirtualType=1;B.mIsFollowAppUninstall=true;end";
	private boolean isDealInstallActivity = false;
	
	public boolean hasStopOperateFolderRun()
	{
		return mHasStopOperateFolderRun;
	}
	
	public void setmHasStopOperateFolderRun(
			boolean mHasStopOperateFolderRun )
	{
		this.mHasStopOperateFolderRun = mHasStopOperateFolderRun;
	}
	
	public boolean hasStartOperateFolderRun()
	{
		return mHasStartOperateFolderRun;
	}
	
	public void setmHasStartOperateFolderRun(
			boolean mHasStartOperateFolderRun )
	{
		this.mHasStartOperateFolderRun = mHasStartOperateFolderRun;
	}
	
	public static OperateHelp getInstance(
			Context context )
	{
		if( mInstance == null && context != null )
		{
			synchronized( OperateHelp.class )
			{
				if( mInstance == null && context != null )
				{
					mInstance = new OperateHelp();
					mContext = context;
					CategoryHelper.getInstance( context ).setListener( mInstance );
					CategoryParse.getInstance().init();
					//					CategoryParse.getInstance().showCategoryNotifyResume();
				}
			}
		}
		return mInstance;
	}
	
	public synchronized static void deleteInstance()
	{
		mContext = null;
		mInstance = null;
	}
	
	private OperateHelp()
	{
		super();
	}
	
	public synchronized void startCategory()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , "shlt , test , OperateHelp , getOperateDate() : " );
		if( hasStopOperateFolderRun() )//若此时执行智能分类，stop智能分类还没完成，则此时不要停止智能分类 wanghongjian add
		{
			return;
		}
		if( mListener != null && mListener.getOperateDynamicMain() != null )
		{
			if( mListener.getOperateDynamicMain().isOperateFolderRunning() )//若此时执行智能分类的时候，运营文件夹正在更新，则此时不要智能分类，等运营文件夹结束的时候再执行智能分类
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "" , "whj OperateStart 运营文件夹正在执行，开始智能分类先停止" );
				setmHasStartOperateFolderRun( true );
				return;
			}
			else
			{
				setmHasStartOperateFolderRun( false );
			}
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , "whj OperateStart 开始智能分类执行" );
		if( !gettingOperateData )
		{
			//cheyingkun add start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
			if( mListener != null )
			{
				mListener.onBeforeStartCategory();
			}
			CategoryParse.getInstance().cancelNotify();
			//cheyingkun add end
			//WangLei start  //bug:0010742 //桌面成功进行智能分类后选择恢复布局，桌面提示语仍然是"智能分类中..."
			//showLoadingProgressView(); //WangLei del
			showLoadingProgressView( R.string.category_running ); //WangLei add
			//WangLei end
			if( NetWorkUtils.isNetworkAvailable( mContext ) )//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
			{
				gettingOperateData = true;
				//
				new Thread() {
					
					public void run()
					{
						//
						ArrayList<ItemInfo> workspaceApps = getFilteredAppList();
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "" , StringUtils.concat( "shlt , test , OperateHelp , workspaceApps : " , workspaceApps.size() ) );
						//
						if( workspaceApps != null && workspaceApps.size() > 0 )
						{
							getCategoryAndRecommendDataByInternet( workspaceApps );
						}
						else
						{
							gettingOperateData = false;
							dismissLoadingProgressView();
						}
					}
				}.start();
			}
			else
			{
				ToastUtils.showToast( LauncherAppState.getActivityInstance() , R.string.category_no_network );
				dismissLoadingProgressView();
			}
		}
		else
		{
			ToastUtils.showToast( LauncherAppState.getActivityInstance() , R.string.category_is_running );
		}
	}
	
	public synchronized void stopCategory()
	{
		if( hasStartOperateFolderRun() )//若此时执行开始智能分类，智能分类还没完成，则此时不要停止智能分类 wanghongjian add
		{
			return;
		}
		if( mListener != null && mListener.getOperateDynamicMain() != null )
		{
			if( mListener.getOperateDynamicMain().isOperateFolderRunning() )//若此时执行stop智能分类的时候，运营文件夹正在更新，则此时不要stop智能分类，等运营文件夹结束的时候再执行stop智能分类
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "" , "whj OperateStart 运营文件夹正在执行，stop智能分类先停止" );
				setmHasStopOperateFolderRun( true );
				return;
			}
			else
			{
				setmHasStopOperateFolderRun( false );
			}
		}
		//0010405: 【桌面】桌面编辑模式将某页面移动位置后再选择智能分类或回到之前，桌面重新刷新后该页面图标显示异常 , change by shlt@2015/03/09 ADD START
		gettingOperateData = true;
		//0010405: 【桌面】桌面编辑模式将某页面移动位置后再选择智能分类或回到之前，桌面重新刷新后该页面图标显示异常 , change by shlt@2015/03/09 ADD END
		//cheyingkun add start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		if( mListener != null )
		{
			mListener.onBeforeStartCategory();
		}
		//cheyingkun add end
		//WangLei start  //bug:0010742 //桌面成功进行智能分类后选择恢复布局，桌面提示语仍然是"智能分类中..."
		//showLoadingProgressView(); //WangLei del
		showLoadingProgressView( R.string.category_stop_running ); //WangLei add
		//WangLei end
		LauncherModel.runOnWorkerThread( new Runnable() {
			
			@Override
			public void run()
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
					//<智能分类恢复时,未更新maxScreenId> liuhailin@2015-03-27 modify begin
					long maxScreenRank = getMaxScreenRank( LauncherAppState.getLauncherProvider().getProviderDB() );
					//加1是因为screenRank计数是从0开始
					LauncherAppState.getLauncherProvider().updateMaxScreenId( maxScreenRank + 1 );
					//<智能分类恢复时,未更新maxScreenId> liuhailin@2015-03-27 modify end
					//WangLei add start //0011044  单层模式时将桌面除时间插件之外所有的图标拖动到一个文件夹里，让桌面只有一页。之后进行智能分类，成功后切换页面到第二页。恢复布局，桌面停止运行
					//【原因】恢复布局时会调用Folder.java的centerAboutIcon方法，其中获取当前页出错(大于等于childCount)，导致使用当前页对应的CellLayout时报空指针错误
					//【解决方案】恢复布局时，在获取页面的最大范围后更新桌面当前页和下一页的值
					if( mListener != null )
					{
						mListener.onStopCategorySucess();
					}
					//WangLei add end
					LauncherAppState.getInstance().getModel().resetLoadedState( true , true );
					LauncherAppState.getInstance().getModel().startLoaderFromBackground();
					//cheyingkin del start	//解决“智能分类或恢复布局时，进度条消失后桌面未刷新前快速打开文件夹，桌面刷新后文件夹依然是打开状态”的问题【i_0010669】
					//					//取消等待
					//					dismissLoadingProgressView();
					//					//0010405: 【桌面】桌面编辑模式将某页面移动位置后再选择智能分类或回到之前，桌面重新刷新后该页面图标显示异常 , change by shlt@2015/03/09 ADD START
					//					gettingOperateData = false;
					//					//0010405: 【桌面】桌面编辑模式将某页面移动位置后再选择智能分类或回到之前，桌面重新刷新后该页面图标显示异常 , change by shlt@2015/03/09 ADD END
					//cheyingkin del end
					// zhujieping@2015/04/10 UPD START,删掉上次分类成功的时间
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( mContext );
					sp.edit().remove( ClassificationTime ).commit();
					// zhujieping@2015/04/10 UPD END
				}
			}
		} );
	}
	
	//<智能分类恢复时,未更新maxScreenId> liuhailin@2015-03-27 add begin
	private long getMaxScreenRank(
			SQLiteDatabase db )
	{
		Cursor c = db.rawQuery( StringUtils.concat( "SELECT MAX(" , "screenRank" , ") FROM " , LauncherProvider.DatabaseHelper.getWorkspacesTabName() ) , null );
		// get the result
		final int maxIdIndex = 0;
		long value = -1;
		if( c != null && c.moveToNext() )
		{
			value = c.getLong( maxIdIndex );
		}
		if( c != null )
		{
			c.close();
		}
		if( value == -1 )
		{
			throw new RuntimeException( "Error: could not query max screen id" );
		}
		return value;
	}
	
	//<智能分类恢复时,未更新maxScreenId> liuhailin@2015-03-27 add end
	private ArrayList<ItemInfo> getFilteredAppList()
	{
		final ArrayList<ItemInfo> workspaceApps = new ArrayList<ItemInfo>();
		HashMap<Long , ItemInfo> bgItemsMap = LauncherAppState.getInstance().getModel().getBgItemsMap();
		MapTraversalCallBack callBack = new MapTraversalCallBack() {
			
			@Override
			public void findObject(
					Object object )
			{
				if( object instanceof EnhanceItemInfo )
				{
					EnhanceItemInfo info = (EnhanceItemInfo)object;
					//<i_0010504> liuhailin@2015-03-13 del begin
					//if( ( info instanceof ShortcutInfo ) //
					//		&& info.getContainer() != LauncherSettings.Favorites.CONTAINER_HOTSEAT //
					//		&& ( info.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP ) )
					//&& !( info.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP && info.getScreenId() == 1 ) )
					if( info instanceof ShortcutInfo )
					{
						if( isItemCanCategory( (ShortcutInfo)info ) )
							workspaceApps.add( info );
					}
					//<i_0010504> liuhailin@2015-03-13 del end
				}
			}
		};
		MapUtils.traversalMap( bgItemsMap , callBack );
		return workspaceApps;
	}
	
	private boolean isItemCanCategory(
			ItemInfo info )
	{
		if( info.getDefaultWorkspaceItemType() != LauncherSettings.Favorites.DEFAULT_WORKSPACE_ITEM_TYPE_NONE )//xiatian add
		//xiatian end
		//<数据库字段更新> liuhailin@2015-03-23 del end
		{
			//Log.v( "" , "shlt , test , getFilteredAppList , info belong to default_workspace.xml - return" );
			return false;
		}
		//xiatian add start	//fix bug：解决“在智能分类文件夹已经有推荐应用后，再次进行智能分类，分类完成后推荐应用的图标会多出来一份”的问题。
		//【问题原因】智能分类文件夹中的推荐应用，参与了再次分类，导致多写了一条数据库记录。
		//【解决方案】智能分类文件夹中的推荐应用，不参与智能分类。
		if( info instanceof ShortcutInfo && ( (ShortcutInfo)info ).isOperateVirtualItem() )
		{
			return false;
		}
		//xiatian add end 
		if( info instanceof ShortcutInfo && ( (ShortcutInfo)info ).getShortcutType() == LauncherSettings.Favorites.SHORTCUT_TYPE_OPERATE_DYNAMIC )
		{
			return false;
		}
		ComponentName comp = ( (ShortcutInfo)info ).getIntent().getComponent();
		if( comp != null && MeLauncherInterface.getInstance().MeIsMicroEntry( comp.getClassName() ) )//微入口图标不参与分类
		{
			return false;
		}
		return true;
	}
	
	private void getCategoryAndRecommendDataByInternet(
			ArrayList<ItemInfo> workspaceApps )
	{
		CategoryHelper categoryHelper = CategoryHelper.getInstance( mContext.getApplicationContext() );//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		JSONArray categoryAppList = getAppListByItemInfo( workspaceApps );
		categoryHelper.doCategoryRequestForegroundWithoutInsidedata( categoryAppList , new CaregoryReqCallBack() {
			
			@Override
			public void ReqFailed(
					CaregoyReqType type ,
					String Msg )
			{
				ToastUtils.showToast( LauncherAppState.getActivityInstance() , R.string.category_fail );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "" , "shlt , test , OperateHelp , getCategoryAndRecommendDataByInternet() , ReqFailed" );
				gettingOperateData = false;
				dismissLoadingProgressView();
				//cheyingkun add start	//完善T卡挂载逻辑(智能分类开始到加载结束期间,收到广播的处理)
				LauncherAppState.getInstance().getModel().startTaskOnLoaderTaskFinish();
				//cheyingkun add end
			}
			
			@Override
			public void ReqSucess(
					CaregoyReqType type ,
					List<String> appList )
			{
				ReloadDateToDB();
			}
		} );
	}
	
	private JSONArray getAppListByItemInfo(
			ArrayList<ItemInfo> workspaceApps )
	{
		try
		{
			JSONArray appList = new JSONArray();
			for( ItemInfo itemInfo : workspaceApps )
			{
				String packageName = null;
				String className = null;
				int flags = 0;
				String title = null;
				String versionName = null;
				String versionCode = null;
				//
				if( itemInfo == null )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( "" , "shlt , getAppListByItemInfo : itemInfo == null " );
					continue;
				}
				else if( itemInfo instanceof AppInfo )
				{
					AppInfo info = (AppInfo)itemInfo;
					try
					{
						packageName = info.getComponentName().getPackageName();
						className = info.getComponentName().getClassName();
						flags = info.getFlags();
						if( info.getTitle() == null )
						{
							info.setTitle( "name = null" );
						}
						title = info.getTitle().toString();
						versionName = info.getVersionName();
						versionCode = info.getVersionCode();
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
				else if( itemInfo instanceof ShortcutInfo )
				{
					ShortcutInfo info = (ShortcutInfo)itemInfo;
					try
					{
						packageName = info.getIntent().getComponent().getPackageName();
						className = info.getIntent().getComponent().getClassName();
						flags = info.getFlags();
						if( info.getTitle() == null )
						{
							info.setTitle( "name = null" );
						}
						title = info.getTitle().toString();
						PackageManager pm = LauncherAppState.getInstance().getContext().getPackageManager();
						PackageInfo packageInfo = pm.getPackageInfo( packageName , 0 );
						versionName = packageInfo.versionName;
						versionCode = String.valueOf( packageInfo.versionCode );
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
				else
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( "" , StringUtils.concat( "shlt , getAppListByItemInfo : " , itemInfo.getTitle() ) );
					continue;
				}
				//智能分类添加运营 , change by shlt@2014/12/24 UPD END
				if( packageName == null || className == null )
				{
					continue;
				}
				JSONObject curPkgJson = new JSONObject();
				curPkgJson.put( "pn" , packageName );
				if( ( flags & ApplicationInfo.FLAG_SYSTEM ) != 0 || ( flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
				{
					// 非系统应用
					curPkgJson.put( "sy" , "1" );
				}
				else
				{
					// 系统应用
					curPkgJson.put( "sy" , "0" );
				}
				//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD START
				//curPkgJson.put( "cn" , info.title );
				//curPkgJson.put( "en" , info.title );
				curPkgJson.put( "cn" , title );
				curPkgJson.put( "en" , title );
				//将public变量改为private ， 并添加get、set方法 , change by shlt@2014/12/03 UPD END
				// curPkgJson.put( "cn" , info.applicationInfo.)
				curPkgJson.put( "vr" , versionName );
				curPkgJson.put( "vn" , versionCode );
				// info.packageName;
				appList.put( curPkgJson );
				curPkgJson = null;
			}
			return appList;
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void loadAndInsertRecommendDate()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_CATEGORY_FOLDER_OPERATE )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "" , "shlt , test , OperateHelp , loadAndInsertRecommendDate" );
			new Thread() {
				
				public void run()
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "" , "shlt , test , OperateHelp , loadAndInsertRecommendDate , Thread , start" );
					HashMap<Long , FolderInfo> sBgFolders = new HashMap<Long , FolderInfo>( LauncherAppState.getInstance().getModel().getBgFolders() );
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "" , StringUtils.concat( "shlt , test , loadRecommendDateToDb , sBgFolders.size():" , sBgFolders.size() ) );
					MapUtils.traversalMap( sBgFolders , new MapTraversalCallBack() {
						
						@Override
						public void findObject(
								Object object )
						{
							try
							{
								Thread.sleep( 1000 );
							}
							catch( InterruptedException e1 )
							{
								e1.printStackTrace();
							}
							loadAndInsertSingleFolderRecommendDate( object );
						}
					} );
				};
			}.start();
		}
	}
	
	private void showLoadingProgressView(
			int value )
	{
		//cheyingkun add start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】	
		if( progressDialog == null )
		{
			//WangLei start //bug:0010742 //桌面成功进行智能分类后选择恢复布局，桌面提示语仍然是"智能分类中..."
			//progressDialog = new CustomProgressDialog( mContext , mContext.getResources().getString( R.string.category_running ) );//WangLei del		
			progressDialog = new CustomProgressDialog( LauncherAppState.getActivityInstance() , LauncherDefaultConfig.getString( value ) ); //WangLei add
			//WangLei end
		}
		progressDialog.setCancelable( false );
		progressDialog.show();
		//cheyingkun add end
	}
	
	private void dismissLoadingProgressView()
	{
		//cheyingkun add start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		if( progressDialog != null )
		{
			progressDialog.dismiss();
			progressDialog = null;//cheyingkun add	//解决“进行一次智能分类后,切换语言后再进行智能分类,提示语言未跟随系统语言改变”的问题【i_0010528】
		}
		//cheyingkun add end
	}
	
	private int startSearchPageIndex = 0;
	
	private void ReloadDateToDB()
	{
		Runnable r = new Runnable() {
			
			@Override
			public void run()
			{
				//Log.v( "" , "shlt , test , ReloadDateToDB , in" );
				synchronized( LauncherAppState.getLauncherProvider() )
				{
					//Log.v( "" , "shlt , test , ReloadDateToDB , start" );
					CategoryHelper.getInstance( mContext ).lockMap.readLock().lock();//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
					ArrayList<EnhanceItemInfo> workspaceApps = (ArrayList<EnhanceItemInfo>)getFilteredAppList().clone();
					final Map<Integer , EnhanceItemInfo> folderMap = new HashMap<Integer , EnhanceItemInfo>();
					//
					MapUtils.traversalMap( CategoryUpdate.cateinfoMap , new MapTraversalCallBack() {
						
						@Override
						public void findObject(
								Object object )
						{
							int categoryType = Integer.parseInt( object.toString() );
							if( folderMap.get( categoryType ) == null )
							{
								DictData dictData = (DictData)CategoryUpdate.dictMap.get( categoryType );
								FolderInfo folderInfo = new FolderInfo();
								//0010396: 【文件夹】英文状态下，智能分类后的文件夹名称仍然是中文 , change by shlt@2015/03/10 UPD START
								//folderInfo.setTitle( dictData.getCn() );
								//智能分类文件夹的名称有简体和英文两中，通过“中文+cooee+英文”方式连接起来存进数据库
								folderInfo.setTitle( String.format( "%s+cooee+%s" , dictData.getCn() , dictData.getEn() ) );
								//0010396: 【文件夹】英文状态下，智能分类后的文件夹名称仍然是中文 , change by shlt@2015/03/10 UPD END
								folderInfo.setCategoryFolderId( dictData.getId() );
								folderMap.put( dictData.getId() , folderInfo );
							}
						}
					} );
					//
					startSearchPageIndex = LauncherDefaultConfig.CONFIG_CATEGORY_FOLDER_START_ADD_SCREENS_ID;//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
					final ArrayList<Long> workspaceScreens = new ArrayList<Long>();
					TreeMap<Integer , Long> orderedScreens = LauncherModel.loadWorkspaceScreensDb( mContext );//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
					for( Integer i : orderedScreens.keySet() )
					{
						long screenId = orderedScreens.get( i );
						if( screenId > startSearchPageIndex )
						{
							break;
						}
						workspaceScreens.add( screenId );
					}
					//
					SQLiteDatabase db = LauncherAppState.getLauncherProvider().getProviderDB();
					try
					{
						//Log.v( "" , "shlt , test , ReloadDateToDB , in try" );
						db.beginTransaction();
						//先把表复制前来~
						db.execSQL( StringUtils.concat( "create table IF not EXISTS temp_favorites as select * from " , LauncherProvider.DatabaseHelper.getFavoritesTabName() ) );
						db.execSQL( StringUtils.concat( "create table IF not EXISTS temp_workspaceScreens as select * from " , LauncherProvider.DatabaseHelper.getWorkspacesTabName() ) );
						//删除数据库中，所有信息
						db.execSQL( StringUtils.concat( "delete from " , LauncherProvider.DatabaseHelper.getFavoritesTabName() ) );
						//加载默认布局（底边栏、第一页默认布局）
						//<i_0010504> liuhailin@2015-03-13 modify begin
						//LauncherAppState.getLauncherProvider().getOpenHelper().loadFavorites( db , R.xml.default_workspace );
						//<i_0010504> liuhailin@2015-03-13 modify end
						//Log.v( "" , "shlt , test , ReloadDateToDB , delete old date" );
						//<数据库字段更新> liuhailin@2015-03-26 del begin
						//LauncherAppState.getLauncherProvider().updateMaxScreenId( startSearchPageIndex );
						//<数据库字段更新> liuhailin@2015-03-26 del end
						//
						//xiatian add start	//fix bug：解决“智能分类时，由于未同步updateMaxScreenId，导致文件夹Iteminfo的screenId和数据库中的screenId不正确”的问题。
						//【问题原因】智能分类前没有同步updateMaxScreenId，导致在方法insertItemToDB中调用LauncherProvider的generateNewScreenId时得到的值不正确。
						LauncherAppState.getLauncherProvider().updateMaxScreenId( startSearchPageIndex );
						//xiatian add end
						//xiatian add start	//fix bug：解决“智能分类时，由于先加载智能分类数据，再到startLoaderFromBackground中加载默认配置，导致默认配置由于配置的位置被占从而不显示”的问题。
						//【问题原因】
						//		1、智能分类数据是根据配置startSearchPageIndex开始从上到下从左到右，依次找空位加载的
						//		2、加载默认配置时，若配置的格子被占，则不加载
						//		3、由于智能分类先加载，导致默认配置的格子被占。所以默认配置不加载。
						//【解决方案】先加载默认配置，再加载智能分类数据。
						LauncherAppState.getLauncherProvider().getOpenHelper().loadDefaultConfig( db , R.xml.default_workspace , false );
						//xiatian add end
						//xiatian add start	//fix bug：解决“1、未进行智能分类前，先将第一页所有图标移到其他页面后，再进行智能分类，分类后第一页页面和第一页所有图标都丢失；2、进行智能分类后，先将第一页所有图标移到其他页面后，再进行智能分类，分类后只显示一个空白页面，所有图标都丢失”的问题。【i_0011277】
						//【问题原因】
						//		1、当分类文成后重新加载launcher数据时，由于在launcherMode中的loadWorkspace方法里面获取loadedOldDb为false，从而导致进入if(loadedOldDb==false)的分支
						//		2、if(loadedOldDb==false)的分支中从数据库中读取的orderedScreens = loadWorkspaceScreensDb( mContext )不包含已经删除页面，故导致该bug
						//【解决方案】设置setFlagJustLoadedOldDb的数据，使得loadWorkspace方法里面获取的loadedOldDb为为true，从而不进入进入if(loadedOldDb==false)的分支，不读取老的sBgWorkspaceScreens。
						LauncherAppState.getLauncherProvider().getOpenHelper().setFlagJustLoadedOldDb();
						//xiatian add end
						MapUtils.traversalMap( folderMap , new MapTraversalCallBack() {
							
							@Override
							public void findObject(
									Object object )
							{
								EnhanceItemInfo folderInfo = (EnhanceItemInfo)object;
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( "" , StringUtils.concat( "shlt , test , ReloadDateToDB , insert folder : " , folderInfo.getTitle() , " categoryType : " , folderInfo.getCategoryFolderId() ) );
								insertItemToDB( workspaceScreens , folderInfo );
							}
						} );
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "" , "shlt , test , ReloadDateToDB , insert folder finish" );
						//
						for( EnhanceItemInfo itemInfo : workspaceApps )
						{
							//xiatian start	//fix bug：解决“在桌面添加1X1的'快捷拨号'插件后，智能分类不成功”的问题。
							//							String packageName = itemInfo.getIntent().getComponent().getPackageName();//xiatian del
							//xiatian add start
							Intent mIntent = itemInfo.getIntent();
							if( mIntent == null )
							{
								continue;
							}
							ComponentName mComponentName = mIntent.getComponent();
							if( mComponentName == null )
							{
								continue;
							}
							String packageName = mComponentName.getPackageName();
							//xiatian add end
							//xiatian end
							int categoryType = CategoryHelper.cateinfoMap.get( packageName );
							itemInfo.setCategoryFolderId( categoryType );
							EnhanceItemInfo foler = folderMap.get( categoryType );
							if( foler != null )//cheyingkun add	//添加非空保护
							{
								long curContainer = foler.getId();
								itemInfo.setContainer( curContainer );
								itemInfo.setScreenId( 0 );
								insertItemToDB( workspaceScreens , itemInfo );
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( "" , StringUtils.concat( "shlt , test , ReloadDateToDB , insert apps : " , itemInfo.getTitle() , " foler : " , foler.getId() ) );
							}
						}
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "" , "shlt , test , ReloadDateToDB , insert app finish" );
						//
						if( LauncherDefaultConfig.SWITCH_ENABLE_CATEGORY_FOLDER_OPERATE )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "" , "shlt , test , ReloadDateToDB , start , insert recommend" );
							MapUtils.traversalMap( folderMap , new MapTraversalCallBack() {
								
								@Override
								public void findObject(
										Object object )
								{
									EnhanceItemInfo folderInfo = (EnhanceItemInfo)object;
									if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
										Log.v( "" , StringUtils.concat( "shlt , test , ReloadDateToDB , folderMap , folderInfo : " , folderInfo.getTitle() ) );
									//								DictData dictData = (DictData)object;
									int categoryType = folderInfo.getCategoryFolderId();
									List<RecommendApkInfo> recommendApkInfos = CategoryHelper.getInstance( mContext ).getRecommendApkInfoList( categoryType );//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
									if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
										Log.v( "" , StringUtils.concat( "shlt , test , ReloadDateToDB , folderMap , recommendApkInfos : " , recommendApkInfos.size() ) );
									for( RecommendApkInfo recommendApkInfo : recommendApkInfos )
									{
										if( LauncherAppState.isApkInstalled( recommendApkInfo.getPkgName() ) || OperateDynamicProxy.getInstance().containsApp( recommendApkInfo.getPkgName() ) )//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
										{
											continue;
										}
										EnhanceItemInfo foler = folderMap.get( categoryType );
										if( foler != null )
										{
											//
											final ShortcutInfo shortcutInfo = new ShortcutInfo();
											shortcutInfo.setTitle( recommendApkInfo.getApkCN() );
											shortcutInfo.setContainer( foler.getId() );
											//
											//<数据库字段更新> liuhailin@2015-03-23 modify begin
											Intent intent = null;
											if( recommendApkInfo.getApkType() == OperateDynamicUtils.VIRTUAL_APP )
											{
												intent = new Intent( OperateDynamicMain.OPERATE_DYNAMIC_FOLDER );
												ComponentName cp = new ComponentName( mContext.getPackageName() , OperateDynamicMain.OperateActivityCls );
												intent.setComponent( cp );
												intent.putExtra( OperateDynamicUtils.DYNAMIC_APP_DOWNLOAD_TYPE , recommendApkInfo.getFlag() );
												intent.putExtra( OperateDynamicUtils.DYNAMIC_APP_SIZE , recommendApkInfo.getApkSize() );
												int state = DlManager.getInstance().getPkgNameCurrentState( recommendApkInfo.getPkgName() );
												intent.putExtra( OperateDynamicMain.SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY , state );//判读是否是静默下载成功的
											}
											else if( recommendApkInfo.getApkType() == OperateDynamicUtils.VIRTUAL_LINK )
											{
												Uri uri = Uri.parse( recommendApkInfo.getPkgName() );
												intent = new Intent( Intent.ACTION_VIEW , uri );
												intent.putExtra( OperateDynamicMain.OPERATE_WEBLINKPKG , recommendApkInfo.getWebLinkPkg() );
												intent.putExtra( "linkAddr" , recommendApkInfo.getLinkAddr() );
											}
											if( intent != null )
											{
												intent.setPackage( recommendApkInfo.getPkgName() );
												intent.putExtra( OperateDynamicUtils.BITMAP_PATH_KEY , recommendApkInfo.getApkIconpath() );
												intent.putExtra( OperateDynamicMain.PKGNAME_ID , recommendApkInfo.getPkgName() );
												intent.putExtra( OperateDynamicUtils.DYNAMIC_TYPE_KEY , recommendApkInfo.getApkType() );
											}
											//										intent.setPackage( recommendApkInfo.getPkgName() );
											//											intent.setClassName( recommendApkInfo.getPkgName() , recommendApkInfo.getPkgName() );
											//
											Intent operateIntent = new Intent();
											//<数据库字段更新> liuhailin@2015-03-24 modify begin
											operateIntent.putExtra( "categoryFolderId" , categoryType );
											operateIntent.putExtra( "canUninstall" , false );
											operateIntent.putExtra( "canDrag" , false );
											operateIntent.putExtra( "isOperateVirtualItem" , true );
											operateIntent.putExtra( "iconPath" , recommendApkInfo.getApkIconpath() );
											operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_CATEGORY_FOLDER_ID , categoryType );
											operateIntent.putExtra( "canUninstall" , false );
											operateIntent.putExtra( "canDrag" , false );
											operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM , true );
											operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_ICON_PATH , recommendApkInfo.getApkIconpath() );
											//<数据库字段更新> liuhailin@2015-03-24 modify end
											shortcutInfo.setIntent( intent );
											shortcutInfo.setCategoryFolderId( categoryType );
											shortcutInfo.setOperateVirtualItem( true );
											shortcutInfo.setIconPath( recommendApkInfo.getApkIconpath() );
											shortcutInfo.setOperateIntent( operateIntent );
											//<数据库字段更新> liuhailin@2015-03-23 modify end
											//
											insertItemToDB( workspaceScreens , shortcutInfo );
											if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
												Log.v( "" , StringUtils.concat( "shlt , test , ReloadDateToDB , insert recommend : " , shortcutInfo.getTitle() , " foler : " , foler.getId() ) );
										}
									}
								}
							} );
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "" , "shlt , test , ReloadDateToDB , insert recommend finish" );
						}
						//
						//LauncherAppState.getInstance().getModel().updateWorkspaceScreenOrder( mLauncher , workspaceScreens );
						db.setTransactionSuccessful();
						//
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "" , "shlt , test , finish" );
					}
					catch( Exception e )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "" , "shlt , test , catch{}" );
						e.printStackTrace();
					}
					finally
					{
						CategoryHelper.getInstance( mContext ).lockMap.readLock().unlock();//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
						db.endTransaction();
						//xiatian del start	//fix bug：解决“智能分类时，由于先加载智能分类数据，再到startLoaderFromBackground中加载默认配置，导致默认配置由于配置的位置被占从而不显示”的问题。
						//【备注】
						//		1、将“LauncherProvider.EMPTY_DATABASE_CREATED + DatabaseHelper.getFavoritesTabName()”设为false,会在loadDefaultFavoritesIfNecessary中再次加载默认配置。
						//		2、但是先加载智能分类数据的话，会占掉默认配置的位置，导致默认配置不显示
						//						//<i_0010504> liuhailin@2015-03-13 modify begin
						//						String spKey = LauncherAppState.getSharedPreferencesKey();
						//						SharedPreferences sp = mContext.getSharedPreferences( spKey , Context.MODE_PRIVATE );//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
						//						sp.edit().putBoolean( LauncherProvider.EMPTY_DATABASE_CREATED + DatabaseHelper.getFavoritesTabName() , true ).commit();
						//						//<i_0010504> liuhailin@2015-03-13 modify end
						//xiatian del end
						// zhujieping@2015/04/10 UPD START，记录下分类成功的时间
						SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences( mContext );
						sp1.edit().putString( ClassificationTime , String.valueOf( System.currentTimeMillis() ) ).commit();//zhujieping，记录时间，不记录字串，否则语言无法跟着变化
						// zhujieping@2015/04/10 UPD END
						//WangLei add //0011044   单层模式时将桌面除时间插件之外所有的图标拖动到一个文件夹里，让桌面只有一页。之后进行智能分类，成功后切换页面到第二页。恢复布局，桌面停止运行
						if( mListener != null )
						{
							mListener.onCategorySucess();
						}
						//WangLei add end
						LauncherAppState.getInstance().getModel().resetLoadedState( true , true );
						LauncherAppState.getInstance().getModel().startLoaderFromBackground();
						//cheyingkin del start	//解决“智能分类或恢复布局时，进度条消失后桌面未刷新前快速打开文件夹，桌面刷新后文件夹依然是打开状态”的问题【i_0010669】
						//						//取消等待
						//						gettingOperateData = false;
						//						dismissLoadingProgressView();
						//cheyingkun del end
					}
				}
			}
		};
		LauncherModel.runOnWorkerThread( r );
	}
	
	// zhujieping@2015/06/29 DEL START,用不到删除
	//private String getSystemDataATime()
	//{
	//	SimpleDateFormat dateFormat24 = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	//	return dateFormat24.format( Calendar.getInstance().getTime() );
	//}
	// zhujieping@2015/06/29 DEL END
	private void insertItemToDB(
			ArrayList<Long> workspaceScreens ,
			EnhanceItemInfo a )
	{
		if( a.getTitle() == null )
		{
			a.setTitle( "name = null" );
		}
		//<数据库字段更新> liuhailin@2015-03-24 modify begin
		//Pair<Long , int[]> coords = LauncherModel.findNextAvailableIconSpace( mContext , a.getTitle().toString() , a.getIntent() , startSearchPageIndex , workspaceScreens );//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		Pair<Long , int[]> coords = LauncherModel.findNextAvailableIconSpace( mContext , startSearchPageIndex , workspaceScreens );//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		//<数据库字段更新> liuhailin@2015-03-24 modify end
		if( coords == null )
		{
			LauncherProvider lp = LauncherAppState.getLauncherProvider();
			// If we can't find a valid position, then just add a new screen.
			// This takes time so we need to re-queue the add until the new
			// page is added.  Create as many screens as necessary to satisfy
			// the startSearchPageIndex.
			if( workspaceScreens.size() > startSearchPageIndex )
			{
				startSearchPageIndex++;
			}
			int numPagesToAdd = Math.max( 1 , startSearchPageIndex + 1 - workspaceScreens.size() );
			while( numPagesToAdd > 0 )
			{
				long screenId = lp.generateNewScreenId();
				// Save the screen id for binding in the workspace
				workspaceScreens.add( screenId );
				numPagesToAdd--;
			}
			// Find the coordinate again
			//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
			//<数据库字段更新> liuhailin@2015-03-24 modify begin
			//coords = LauncherModel.findNextAvailableIconSpace( mContext , a.getTitle().toString() , a.getIntent() , startSearchPageIndex , workspaceScreens );//cheyingkun add
			coords = LauncherModel.findNextAvailableIconSpace( mContext , startSearchPageIndex , workspaceScreens );//cheyingkun add
			//<数据库字段更新> liuhailin@2015-03-24 modify end
			//cheyingkun end
		}
		if( coords == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "" , "shlt , test , insertItemToDB , return " );
			return;
		}
		//
		if( a instanceof ShortcutInfo )
		{
		}
		else if( a instanceof AppInfo )
		{
			a = ( (AppInfo)a ).makeShortcut();
		}
		else if( a instanceof FolderInfo )
		{
			a.setScreenId( coords.first );
			a.setContainer( LauncherSettings.Favorites.CONTAINER_DESKTOP );
		}
		else
		{
			throw new RuntimeException( "Unexpected info type" );
		}
		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		LauncherModel.addItemToDatabase( mContext , a , a.getContainer() , a.getScreenId() , coords.second[0] , coords.second[1] , false );//cheyingkun add
		//cheyingkun end
	}
	
	public void downloadApp(
			final ShortcutInfo apkInfo )
	{
		if( LauncherAppState.getInstance().isSDCardExist() == false )
		{
			//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
			Toast.makeText( mContext.getApplicationContext() , LauncherDefaultConfig.getString( R.string.category_download_fail ) , Toast.LENGTH_LONG ).show();//cheyingkun add
			//cheyingkun end
			return;
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , "shlt , test , downloadApp , start" );
		final String packName = apkInfo.getIntent().getComponent().getPackageName();
		//
		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		CoolDLMgr dlMgr = CategoryHelper.getInstance( mContext.getApplicationContext() ).getCoolDLMgrApk();//cheyingkun add
		//cheyingkun end
		dl_info dlInfo = dlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , packName );
		if( apkInfo.getTitle() == null )
		{
			apkInfo.setTitle( "name = null" );
		}
		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		final NotificationInfo notificationInfo = NotificationUtils.getNotification( mContext , apkInfo.getTitle().toString() );//cheyingkun add
		//cheyingkun end
		if( dlInfo != null )
		{
			if( dlInfo.IsDownloadSuccess() )
			{
				installApk( dlInfo.getFilePath() );
				return;
			}
			else if( dlInfo.getDownloadState() != 0 )
			{
				//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
				Toast.makeText( mContext.getApplicationContext() , R.string.downloading_toast , Toast.LENGTH_SHORT ).show();//cheyingkun add
				//cheyingkun end
				return;
			}
		}
		final NotificationManager mNotificationManager = (NotificationManager)mContext.getSystemService( Context.NOTIFICATION_SERVICE );
		mNotificationManager.notify( notificationInfo.getNotifyID() , notificationInfo.getNotification() );
		dlMgr.ResDownloadStart( CoolDLResType.RES_TYPE_APK , packName , new CoolDLCallback() {
			
			@Override
			public void onSuccess(
					CoolDLResType arg0 ,
					String arg1 ,
					dl_info arg2 )
			{
				synchronized( downloadAppItemInfos )
				{
					ItemInfo tempInfo = downloadAppItemInfos.get( packName );
					if( tempInfo == null )
					{
						downloadAppItemInfos.put( packName , apkInfo );
					}
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "" , StringUtils.concat( "shlt , test , downloadApp , onSuccess , arg0 : " , arg0 , "arg1 : " , arg1 , "arg2 : " , arg2 ) );
					installApk( arg2.getFilePath() );
					//
					mNotificationManager.cancel( notificationInfo.getNotifyID() );
				}
			}
			
			@Override
			public void onFail(
					CoolDLResType arg0 ,
					String arg1 ,
					dl_info arg2 )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "" , StringUtils.concat( "shlt , test , downloadApp , onFail , arg0 : " , arg0 , "arg1 : " , arg1 , "arg2 : " , arg2 ) );
				if( apkInfo.getTitle() == null )
				{
					apkInfo.setTitle( "name = null" );
				}
				//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
				NotificationUtils.updateNotificationByDownloadFail( mContext , apkInfo.getTitle().toString() , notificationInfo );//cheyingkun add
				//cheyingkun end
				mNotificationManager.notify( notificationInfo.getNotifyID() , notificationInfo.getNotification() );
			}
			
			@Override
			public void onDoing(
					CoolDLResType arg0 ,
					String arg1 ,
					dl_info arg2 )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "" , StringUtils.concat( "shlt , test , downloadApp , onDoing , arg0 : " , arg0 , "arg1 : " , arg1 , "arg2 : " , arg2 ) );
				int progress = (int)( 100 * arg2.getCurBytes() / arg2.getTotalBytes() );
				//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
				NotificationUtils.updateNotificationByDownloading( mContext , progress , notificationInfo );//cheyingkun add
				//cheyingkun end
				mNotificationManager.notify( notificationInfo.getNotifyID() , notificationInfo.getNotification() );
			}
		} );
	}
	
	public boolean checkOperateDownLoad(
			String packageName )
	{
		synchronized( downloadAppItemInfos )
		{
			if( LauncherAppState.getInstance().getModel() == null || LauncherAppState.getInstance().getModel().getBgFolders() == null )
			{
				return false;
			}
			IconCache mIconCache = LauncherAppState.getInstance().getIconCache();
			if( mIconCache == null )
			{
				return false;
			}
			ShortcutInfo tempInfo = downloadAppItemInfos.get( packageName );
			if( tempInfo != null )
			{
				FolderInfo folderInfo = LauncherAppState.getInstance().getModel().getBgFolders().get( tempInfo.getContainer() );
				if( folderInfo != null )
				{
					updateRecommendItemWhenInstallApk( folderInfo , tempInfo , mIconCache );
					downloadAppItemInfos.remove( packageName );
					return true;
				}
			}
			else
			{
				HashMap<Long , FolderInfo> sBgFolders = LauncherAppState.getInstance().getModel().getBgFolders();
				if( sBgFolders != null && sBgFolders.size() > 0 )
				{
					Iterator<Entry> it = ( (Map)sBgFolders ).entrySet().iterator();
					while( it.hasNext() )
					{
						Map.Entry entry = (Map.Entry)it.next();
						FolderInfo mFolderInfo = (FolderInfo)entry.getValue();
						if( mFolderInfo != null )
						{
							ArrayList<ShortcutInfo> mContents = mFolderInfo.getContents();
							for( ShortcutInfo mShortcutInfo : mContents )
							{
								if( mShortcutInfo.getIntent() != null && packageName.equals( mShortcutInfo.getIntent().getPackage() ) )
								{
									updateRecommendItemWhenInstallApk( mFolderInfo , mShortcutInfo , mIconCache );
									return true;
								}
							}
						}
					}
				}
			}
			return false;
		}
	}
	
	private void updateRecommendItemWhenInstallApk(
			FolderInfo mFolderInfo ,
			ShortcutInfo mShortcutInfo ,
			IconCache mIconCache )
	{
		mFolderInfo.remove( mShortcutInfo );
		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		LauncherModel.moveItemInDatabase( mContext , mShortcutInfo , mShortcutInfo.getId() , 0 , mShortcutInfo.getCellX() , mShortcutInfo.getCellY() );//cheyingkun add 
		//cheyingkun end
		mIconCache.remove( mShortcutInfo.getIntent().getComponent() );
		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		final List<ResolveInfo> matches = AllAppsList.findActivitiesForPackage( mContext , mShortcutInfo.getIntent().getPackage() );//cheyingkun add
		//cheyingkun end
		if( matches.size() > 0 )
		{
			for( ResolveInfo info : matches )
			{
				//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
				AppInfo mAppInfo = new AppInfo( mContext.getPackageManager() , info , mIconCache , null );//cheyingkun add
				//cheyingkun end
				mFolderInfo.add( mAppInfo.makeShortcut() );
			}
		}
	}
	
	public ShortcutInfo getOperateMoreAppShortcutInfo(
			FolderInfo folderInfo )
	{
		//xiatian start	//整理canShowMoreEntry方法。
		//xiatian del start
		//		//xiatian add start	//需求：在双层模式下，不显示所有‘更多应用’图标（智能分类文件夹和非智能分类文件夹中）。
		//		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		//		{
		//			return null;
		//		}
		//		//xiatian add end
		//		//cheyingkun add start	//如果没有进行智能分类，打开文件夹不显示更多应用图标。
		//		//分类成功时，会记录下时间，根据是否有记录来判断是否分类
		//		SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences( mContext );
		//		String value = mSharedPrefs.getString( OperateHelp.ClassificationTime , null );
		//		if( value == null )
		//		{
		//			return null;
		//		}
		//		//cheyingkun add end
		//		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		//		boolean showMore = CategoryHelper.getInstance( mContext.getApplicationContext() ).canShowMoreEntry();//cheyingkun add
		//		//cheyingkun end
		//xiatian del end
		int mCategoryFolderId = folderInfo.getCategoryFolderId();
		boolean showMore = canShowMoreEntry( mCategoryFolderId );//xiatian add
		//xiatian end
		ShortcutInfo shortcutInfo = null;
		if( ( folderInfo != null ) && ( showMore == true ) )
		{
			//			if( mCategoryFolderId == Integer.MAX_VALUE/*MAX_VALUE为非智能分类产生的文件夹*/|| mCategoryFolderId == 0/*0为“系统应用”文件夹*/|| mCategoryFolderId == -1/*-1为“更多应用”文件夹*/)
			//			{
			//				//				return shortcutInfo;//以上三种情况，不显示"更多应用"图标
			//				mCategoryFolderId = 2;//以上三种情况，显示"更多应用"图标，点击后进入“装机必备”界面
			//			}
			shortcutInfo = new ShortcutInfo();
			//<数据库字段更新> liuhailin@2015-03-23 modify begin
			//shortcutInfo.setCanUninstall( false );
			//<数据库字段更新> liuhailin@2015-03-23 modify end
			shortcutInfo.setTitle( LauncherDefaultConfig.getString( R.string.category_more_apps ) );
			//
			//<数据库字段更新> liuhailin@2015-03-23 modify begin
			//Intent operateIntent = new Intent();
			//operateIntent.putExtra( "isOperateVirtualItem" , true );
			//operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM , true );
			//operateIntent.putExtra( "isOperateVirtualMoreAppItem" , true );
			//operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_IS_OPERATE_VIRTURAL_MORE_APP_ITEM , true );
			//operateIntent.putExtra( "canDrag" , false );
			//operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_CATEGORY_FOLDER_ID , mCategoryFolderId );
			//
			//shortcutInfo.setIntent( intent );
			//shortcutInfo.setOperateIntent( operateIntent );
			//xiatian add start	//fix bug：解决“点击‘更多应用’图标（智能分类文件夹和非智能分类文件夹中）后，桌面重启”的问题。
			Intent mIntent = new Intent();
			shortcutInfo.setIntent( mIntent );
			//xiatian add end
			shortcutInfo.setOperateVirtualItem( true );
			shortcutInfo.setOperateVirtualMoreAppItem( true );
			shortcutInfo.setCategoryFolderId( mCategoryFolderId );
			//<数据库字段更新> liuhailin@2015-03-23 modify end
		}
		return shortcutInfo;
	}
	
	public void enterMoreAppUI(
			ShortcutInfo shortcutInfo )
	{
		Intent intent = new Intent();
		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		intent.setClass( mContext , MainActivity.class );//cheyingkun add
		//cheyingkun end
		intent.putExtra( "APP_ID" , shortcutInfo.getCategoryFolderId() );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		mContext.startActivity( intent );//cheyingkun add
		//cheyingkun end
	}
	
	private void installApk(
			String mFilePath )
	{
		Intent intent = new Intent( Intent.ACTION_VIEW );
		intent.setDataAndType( Uri.fromFile( new File( mFilePath ) ) , "application/vnd.android.package-archive" );
		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		mContext.startActivity( intent );//cheyingkun add
		//cheyingkun end
	}
	
	private void loadAndInsertSingleFolderRecommendDate(
			Object mObject )
	{
		final FolderInfo folderInfo = (FolderInfo)mObject;
		if( folderInfo.getId() <= 0 )
		{
			return;
		}
		ArrayList<ShortcutInfo> mContents = folderInfo.getContents();
		ArrayList<ShortcutInfo> mContentsToRemoveList = new ArrayList<ShortcutInfo>();
		for( ShortcutInfo mShortcutInfo : mContents )
		{
			//<数据库字段更新> liuhailin@2015-03-23 modify begin
			//Intent mIntent = mShortcutInfo.getIntent();
			if( mShortcutInfo.isOperateVirtualItem() && ( mShortcutInfo.isOperateVirtualMoreAppItem() == false ) )
			{
				mContentsToRemoveList.add( mShortcutInfo );
			}
			//<数据库字段更新> liuhailin@2015-03-23 modify end
		}
		if( mContentsToRemoveList.size() > 0 )
		{
			for( final ShortcutInfo mShortcutInfo : mContentsToRemoveList )
			{
				//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
				LauncherAppState.getActivityInstance().runOnUiThread( new Runnable() {
					
					//cheyingkun end
					@Override
					public void run()
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "xiatian - updateRecommendDate - del" , ":" + mShortcutInfo );
						folderInfo.remove( mShortcutInfo );
						LauncherModel.deleteItemFromDatabase( mContext , mShortcutInfo );
					}
				} );
			}
		}
		final int categoryFolderId = folderInfo.getCategoryFolderId();
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , StringUtils.concat( "shlt , test , findObject , folderInfo : " , folderInfo.getTitle() , " categoryFolderId : " , categoryFolderId ) );
		//
		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		List<RecommendApkInfo> recommendApkInfos = CategoryHelper.getInstance( mContext ).getRecommendApkInfoList( categoryFolderId );//cheyingkun add
		//cheyingkun end
		//		List<RecommendApkInfo> mToRemoveList = new ArrayList<RecommendApkInfo>();
		//		for( RecommendApkInfo recommendApkInfo : recommendApkInfos )
		//		{
		//			if( LauncherAppState.isApkInstalled( recommendApkInfo.getPkgName() ) )
		//			{
		//				mToRemoveList.add( recommendApkInfo );
		//			}
		//		}
		//		if( mToRemoveList.size() > 0 )
		//		{
		//			recommendApkInfos.removeAll( mToRemoveList );
		//		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , StringUtils.concat( "shlt , test , loadRecommendDateToDb , recommendApkInfos : " , recommendApkInfos.size() ) );
		for( RecommendApkInfo recommendApkInfo : recommendApkInfos )
		{
			if( LauncherAppState.isApkInstalled( recommendApkInfo.getPkgName() ) || OperateDynamicProxy.getInstance().containsApp( recommendApkInfo.getPkgName() ) )
			{
				continue;
			}
			//<数据库字段更新> liuhailin@2015-03-23 modify begin
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "" , StringUtils.concat( "shlt , test , loadRecommendDateToDb , recommendApkInfo : " , recommendApkInfo.getTitle() ) );
			final ShortcutInfo shortcutInfo = new ShortcutInfo();
			shortcutInfo.setTitle( recommendApkInfo.getApkCN() );
			shortcutInfo.setContainer( ItemInfo.NO_ID );//folderInfo.add中对container值进行判断，id为NO_ID才添加，否则更新。这里应该是添加
			//
			//<数据库字段更新> liuhailin@2015-03-23 modify begin
			Intent intent = null;
			if( recommendApkInfo.getApkType() == OperateDynamicUtils.VIRTUAL_APP )
			{
				intent = new Intent( OperateDynamicMain.OPERATE_DYNAMIC_FOLDER );
				ComponentName cp = new ComponentName( mContext.getPackageName() , OperateDynamicMain.OperateActivityCls );
				intent.setComponent( cp );
				intent.putExtra( OperateDynamicUtils.DYNAMIC_APP_DOWNLOAD_TYPE , recommendApkInfo.getFlag() );
				intent.putExtra( OperateDynamicUtils.DYNAMIC_APP_SIZE , recommendApkInfo.getApkSize() );
				int state = DlManager.getInstance().getPkgNameCurrentState( recommendApkInfo.getPkgName() );
				intent.putExtra( OperateDynamicMain.SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY , state );//判读是否是静默下载成功的
			}
			else if( recommendApkInfo.getApkType() == OperateDynamicUtils.VIRTUAL_LINK )
			{
				Uri uri = Uri.parse( recommendApkInfo.getPkgName() );
				intent = new Intent( Intent.ACTION_VIEW , uri );
				intent.putExtra( OperateDynamicMain.OPERATE_WEBLINKPKG , recommendApkInfo.getWebLinkPkg() );
				intent.putExtra( "linkAddr" , recommendApkInfo.getLinkAddr() );
			}
			if( intent != null )
			{
				intent.setPackage( recommendApkInfo.getPkgName() );
				intent.putExtra( OperateDynamicUtils.BITMAP_PATH_KEY , recommendApkInfo.getApkIconpath() );
				intent.putExtra( OperateDynamicMain.PKGNAME_ID , recommendApkInfo.getPkgName() );
				intent.putExtra( OperateDynamicUtils.DYNAMIC_TYPE_KEY , recommendApkInfo.getApkType() );
			}
			Intent operateIntent = new Intent();
			operateIntent.putExtra( "categoryFolderId" , categoryFolderId );
			operateIntent.putExtra( "canUninstall" , false );
			operateIntent.putExtra( "canDrag" , false );
			operateIntent.putExtra( "isOperateVirtualItem" , true );
			operateIntent.putExtra( "iconPath" , recommendApkInfo.getApkIconpath() );
			operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_CATEGORY_FOLDER_ID , categoryFolderId );
			operateIntent.putExtra( "canUninstall" , false );
			operateIntent.putExtra( "canDrag" , false );
			operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM , true );
			operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_ICON_PATH , recommendApkInfo.getApkIconpath() );
			shortcutInfo.setIntent( operateIntent );
			shortcutInfo.setIntent( intent );
			shortcutInfo.setOperateIntent( operateIntent );
			shortcutInfo.setCategoryFolderId( categoryFolderId );
			shortcutInfo.setOperateVirtualItem( true );
			Bitmap bmp = BitmapFactory.decodeFile( recommendApkInfo.getApkIconpath() );
			shortcutInfo.setIcon( Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( bmp , mContext , true , false ) );
			shortcutInfo.setIconPath( recommendApkInfo.getApkIconpath() );
			//shortcutInfo.setOperateIntent( operateIntent );
			//<数据库字段更新> liuhailin@2015-03-23 modify end
			//
			//<数据库字段更新> liuhailin@2015-03-23 modify end
			//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
			LauncherAppState.getActivityInstance().runOnUiThread( new Runnable() {
				
				//cheyingkun end
				@Override
				public void run()
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "xiatian - updateRecommendDate - add" , ":" + shortcutInfo );
					folderInfo.add( shortcutInfo );
				}
			} );
		}
	}
	
	public void updateRecommendApkIcon(
			final int categoryFoldId )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_CATEGORY_FOLDER_OPERATE )
		{
			final List<String> updateSQL = new LinkedList<String>();
			HashMap<Long , FolderInfo> sBgFolders = LauncherAppState.getInstance().getModel().getBgFolders();
			// 刷新ShortcutInfo中的intent里面的iconPath，同时创建SQL语句,最后刷新一下界面~
			MapUtils.traversalMap( sBgFolders , new MapTraversalCallBack() {
				
				@Override
				public void findObject(
						Object object )
				{
					final FolderInfo folderInfo = (FolderInfo)object;
					if( folderInfo.getCategoryFolderId() == categoryFoldId )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "" , StringUtils.concat( "shlt , test , updateRecommendApkIcon , folderInfo : " , folderInfo.getTitle() ) );
						ArrayList<ShortcutInfo> contents = folderInfo.getContents();
						for( final ShortcutInfo shortcutInfo : contents )
						{
							//<数据库字段更新> liuhailin@2015-03-24 modify begin
							Intent intent = shortcutInfo.getIntent();
							//boolean isOperateVirtualItem = intent.getBooleanExtra( "isOperateVirtualItem" , false );
							boolean isOperateVirtualItem = shortcutInfo.isOperateVirtualItem();
							//boolean isOperateVirtualMoreAppItem = intent.getBooleanExtra( "isOperateVirtualMoreAppItem" , false );
							boolean isOperateVirtualMoreAppItem = shortcutInfo.isOperateVirtualMoreAppItem();
							//String iconPath = intent.getStringExtra( "iconPath" );
							String iconPath = shortcutInfo.getIconPath();
							//<数据库字段更新> liuhailin@2015-03-24 modify end
							//							ComponentName componentName = intent.getComponent();
							if( isOperateVirtualItem && !isOperateVirtualMoreAppItem && TextUtils.isEmpty( iconPath ) )
							{
								//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
								dl_info info = null;
								int type = intent.getIntExtra( OperateDynamicUtils.DYNAMIC_TYPE_KEY , OperateDynamicUtils.VIRTUAL_APP );
								if( type == OperateDynamicUtils.VIRTUAL_APP )
								{
									info = CategoryHelper.getInstance( mContext ).getCoolDLMgrIcon().IconGetInfo( intent.getStringExtra( OperateDynamicMain.PKGNAME_ID ) );//cheyingkun add
								}
								else if( type == OperateDynamicUtils.VIRTUAL_LINK )
								{
									info = CategoryHelper.getInstance( mContext ).getCoolDLMgrIcon().UrlGetInfo( intent.getStringExtra( "linkAddr" ) );
								}
								//cheyingkun end
								if( info != null && info.IsDownloadSuccess() )
								{
									String newPath = info.getFilePath();
									//<数据库字段更新> liuhailin@2015-03-23 modify begin
									//intent.putExtra( "iconPath" , newPath );
									shortcutInfo.setIconPath( newPath );
									//<数据库字段更新> liuhailin@2015-03-23 modify end
									shortcutInfo.setIntent( intent );
									intent.putExtra( OperateDynamicUtils.BITMAP_PATH_KEY , newPath );
									Bitmap bmp = BitmapFactory.decodeFile( newPath );
									shortcutInfo.setIcon( Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( bmp , mContext , true , false ) );
									//
									LauncherAppState.getActivityInstance().runOnUiThread( new Runnable() {
										
										//cheyingkun end
										@Override
										public void run()
										{
											folderInfo.itemIconChange( shortcutInfo );
										}
									} );
									//
									updateSQL.add( String.format(
											StringUtils.concat( "update " , LauncherProvider.DatabaseHelper.getFavoritesTabName() , " set intent = '%s' where _id = %s" ) ,
											intent.toUri( 0 ) ,
											shortcutInfo.getId() ) );
								}
							}
						}
					}
				}
			} );
			//更新数据库信息
			Runnable runnable = new Runnable() {
				
				@Override
				public void run()
				{
					SQLiteDatabase db = LauncherAppState.getLauncherProvider().getProviderDB();
					db.beginTransaction();
					try
					{
						for( String sql : updateSQL )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "" , StringUtils.concat( "shlt , test , updateRecommendApkIcon , sql : " , sql ) );
							db.execSQL( sql );
						}
						db.setTransactionSuccessful();
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "" , "shlt , test , updateRecommendApkIcon , finish" );
					}
					catch( Exception e )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( "" , "shlt , test , updateRecommendApkIcon , catch" );
						e.printStackTrace();
					}
					finally
					{
						db.endTransaction();
					}
				}
			};
			LauncherModel.runOnWorkerThread( runnable );
		}
	}
	
	//0010328: 【智能分类】桌面智能分类时menu键进入编辑模式，分类成功后桌面报停止运行 , change by shlt@2015/03/03 ADD START
	public boolean isGettingOperateDate()
	{
		return gettingOperateData;
	}
	//0010328: 【智能分类】桌面智能分类时menu键进入编辑模式，分类成功后桌面报停止运行 , change by shlt@2015/03/03 ADD END
	;
	
	//cheyingkun add start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
	public interface CategoryListener
	{
		
		public void onBeforeStartCategory();
		
		//WangLei add //0011044   单层模式时将桌面除时间插件之外所有的图标拖动到一个文件夹里，让桌面只有一页。之后进行智能分类，成功后切换页面到第二页。恢复布局，桌面停止运行
		public void onCategorySucess();//这是在智能分类成功成功后的操作，和这个bug关系不大，属于一个需求
		
		public void onStopCategorySucess();
		
		//WangLei add end
		/**
		 * 智能分类的圈圈结束消失了,表示分类结束或者恢复结束
		 */
		public void onDismissCategoryProgressView();//wanghongjian add
		
		/**
		 * 获得运营文件夹实例使用
		 * @return
		 */
		public OperateDynamicMain getOperateDynamicMain();//wanghongjian add
		
		// zhujieping@2015/09/23 ADD START,桌面移除view和添加view
		public void removeViewFromWorkspace(
				HashSet<ComponentName> cns ,
				boolean isDeleteInfolder );
		
		public void bindItems(
				ArrayList<ItemInfo> shortcuts ,
				int start ,
				int end ,
				boolean forceAnimateIcons ,
				Runnable runnable ,
				boolean isLoadFinish );//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页(逻辑优化)
		// zhujieping@2015/09/23 ADD END
	}
	
	public void setCategoryListener(
			CategoryListener listener )
	{
		mListener = listener;
	}
	
	//cheyingkun add end
	//cheyingkin add start	//解决“智能分类或恢复布局时，进度条消失后桌面未刷新前快速打开文件夹，桌面刷新后文件夹依然是打开状态”的问题【i_0010669】
	/***
	 * 取消等待智能分类的进度条
	 */
	public void dismissCategoryProgressView()
	{
		if( gettingOperateData// 
				&& progressDialog != null && progressDialog.isShowing()//cheyingkun add	//优化加载速度(智能分类初始化放到加载完成之后)
		)
		{
			gettingOperateData = false;
			dismissLoadingProgressView();
			if( mListener != null )
			{
				mListener.onDismissCategoryProgressView();//表示圈圈不显示,wanghongjian add
			}
		}
	}
	//cheyingkun add end
	;
	
	//xiatian add start	//整理canShowMoreEntry方法。
	public boolean canShowMoreEntry(
			int mCategoryFolderId )
	{
		boolean ret = false;
		if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		{
			return ret;
		}
		boolean mIsAlreadyCategory = LauncherAppState.isAlreadyCategory( mContext );
		if( mIsAlreadyCategory == false )
		{
			return ret;
		}
		boolean mSdkCanShowMore = CategoryHelper.getInstance( mContext ).canShowMoreEntry();
		if( mSdkCanShowMore )
		{
			if(
			//
			( mCategoryFolderId != Integer.MAX_VALUE/*MAX_VALUE为非智能分类产生的文件夹*/)
			//
			&& ( mCategoryFolderId != 0/*0为“系统应用”文件夹*/)
			//
			&& ( mCategoryFolderId != -1/*-1为“更多应用”文件夹*/)
			//
			)
			{//当服务器通知智能分类文件夹显示"更多应用"图标时，除了以上三种情况之外，都显示"更多应用"图标。
				ret = true;
			}
		}
		return ret;
	}
	
	//xiatian add end
	@Override
	public void onCategoryRecommendListChange(
			String version )
	{
		if( LauncherAppState.isAlreadyCategory( mContext ) )
			loadAndInsertRecommendDate();//cheyingkun add
	}
	
	@Override
	public void onRecommendIconDownload(
			int fid )
	{
		if( LauncherAppState.isAlreadyCategory( mContext ) )
			updateRecommendApkIcon( fid );//cheyingkun add
	};
	
	public boolean containsOperateAppOrLink(
			String pkg )
	{
		if( pkg != null && LauncherAppState.isAlreadyCategory( mContext ) )
		{
			HashMap<Long , FolderInfo> maps = LauncherAppState.getInstance().getModel().getBgFolders();
			Collection<FolderInfo> colls = maps.values();
			for( FolderInfo info : colls )
			{
				for( ShortcutInfo si : info.getContents() )
				{
					if( si.isOperateVirtualItem() )
					{
						String pkgName = si.getIntent().getStringExtra( OperateDynamicMain.PKGNAME_ID );
						if( pkg.equals( pkgName ) )
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public void dealInstallActivity(
			final String pkgName )
	{
		if( mListener != null && ( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE ) && LauncherAppState.isAlreadyCategory( mContext ) && NetWorkUtils
				.isNetworkAvailable( mContext ) && !isDealInstallActivity )
		{
			AsyncTask.execute( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					isDealInstallActivity = true;
					final ArrayList<ShortcutInfo> appInfoList = new ArrayList<ShortcutInfo>();
					final ArrayList<FolderInfo> folderInfoList = new ArrayList<FolderInfo>();
					ArrayList<ItemInfo> bgItems = new ArrayList<ItemInfo>( LauncherAppState.getInstance().getModel().getBgWorkspaceItems() );
					for( ItemInfo info : bgItems )
					{
						if( info instanceof ShortcutInfo )
						{
							if( !( (ShortcutInfo)info ).isOperateIconItem() && !( (ShortcutInfo)info ).isOperateVirtualItem() )
							{
								if( info.getIntent().getComponent() != null )
								{
									if( isItemCanCategory( info ) )
									{
										appInfoList.add( (ShortcutInfo)info );
									}
								}
							}
						}
						else if( info instanceof FolderInfo )
						{
							if( ( (FolderInfo)info ).getCategoryFolderId() != Integer.MAX_VALUE )
							{
								folderInfoList.add( (FolderInfo)info );
							}
						}
					}
					try
					{
						final JSONArray appList = new JSONArray();
						PackageManager Pkgmanger = mContext.getPackageManager();
						for( ShortcutInfo info : appInfoList )
						{
							PackageInfo pkgInfo = Pkgmanger.getPackageInfo( info.getIntent().getComponent().getPackageName() , 0 );
							JSONObject curPkgJson = new JSONObject();
							curPkgJson.put( "pn" , pkgInfo.packageName );
							if( ( pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) != 0 || ( pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) != 0 )
							{
								//系统应用
								curPkgJson.put( "sy" , "0" );
							}
							else
							{
								//非系统应用
								curPkgJson.put( "sy" , "1" );
							}
							curPkgJson.put( "cn" , Pkgmanger.getApplicationLabel( pkgInfo.applicationInfo ) );
							curPkgJson.put( "en" , Pkgmanger.getApplicationLabel( pkgInfo.applicationInfo ) );
							curPkgJson.put( "vr" , pkgInfo.versionName );
							curPkgJson.put( "vn" , pkgInfo.versionCode );
							appList.put( curPkgJson );
							curPkgJson = null;
						}
						CategoryHelper.getInstance( mContext ).doCategoryQuery( appList , new CaregoryReqCallBack() {
							
							@Override
							public void ReqFailed(
									CaregoyReqType type ,
									String Msg )
							{
								// TODO Auto-generated method stub
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( CategoryConstant.TAG , "-installIconCategory-Failed-type:" + type );
								isDealInstallActivity = false;
							}
							
							@Override
							public void ReqSucess(
									CaregoyReqType type ,
									List<String> appList )
							{
								// TODO Auto-generated method stub
								if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.v( CategoryConstant.TAG , "-installIconCategory-SUCESS-type:" + type );
								if( appList.size() > 0 )
								{
									// TODO Auto-generated method stub
									if( !LauncherAppState.isAlreadyCategory( mContext ) )
									{
										return;
									}
									//这里先将桌面上的图标移除，下面会将该info重新生成view添加的文件夹中。如果先添加到文件中，info的cellx、celly会变成文件夹中的位置，当开关switch_enable_sort_after_uninstall打开时，与桌面其他item的cellx、celly比较，如果有相等的，遍历桌面item的列表就不会添加该info，导致图标没有被删除
									final HashSet<ComponentName> cns = new HashSet<ComponentName>();
									ArrayList<ShortcutInfo> copyAppInfoList = new ArrayList<ShortcutInfo>();
									for( ShortcutInfo info : appInfoList )
									{
										if( info != null )
										{
											if( info.getIntent() != null && info.getIntent().getComponent() != null )
											{
												cns.add( info.getIntent().getComponent() );
											}
											LauncherModel.deleteItemFromDatabase( mContext , info );//这边从数据库中删除，下面新建文件夹时会从数据库中找空位，如果等找完空位在更新数据库，这个位置会被空出来，
											copyAppInfoList.add( new ShortcutInfo( mContext , info ) );
										}
									}
									LauncherAppState.getActivityInstance().runOnUiThread( new Runnable() {
										
										@Override
										public void run()
										{
											// TODO Auto-generated method stub
											if( mListener == null )
											{
												return;
											}
											mListener.removeViewFromWorkspace( cns , false );//这个是ui进程，里面执行有用到info，而下面的是在线程中，会改动到info，所以上面拷贝一份shortcutinfo出来，给下面使用
										}
									} );
									String folderTitle = null;
									final ArrayList<ItemInfo> toAdd = new ArrayList<ItemInfo>();
									for( final ShortcutInfo info : copyAppInfoList )
									{
										String mPkgName = info.getIntent().getComponent().getPackageName();
										int categoryID = CategoryHelper.cateinfoMap.get( mPkgName );
										boolean find = false;
										info.setContainer( ItemInfo.NO_ID );
										for( final FolderInfo fi : folderInfoList )
										{
											if( fi != null && categoryID == fi.getCategoryFolderId() )
											{
												find = true;
												LauncherAppState.getActivityInstance().runOnUiThread( new Runnable() {
													
													@Override
													public void run()
													{
														// TODO Auto-generated method stub
														fi.add( info );
														if( info.getContainer() == ItemInfo.NO_ID )
														{
															LauncherModel.addItemToDatabase( mContext , info , fi.getId() , 0 , info.getCellX() , info.getCellY() , false );
														}
													}
												} );
												if( mPkgName.equals( pkgName ) )
												{
													folderTitle = fi.getTitle();
												}
												break;
											}
										}
										if( !find )
										{
											ArrayList<Long> workspaceScreens = new ArrayList<Long>();
											TreeMap<Integer , Long> orderedScreens = LauncherModel.loadWorkspaceScreensDb( mContext );//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
											for( Integer i : orderedScreens.keySet() )
											{
												long screenId = orderedScreens.get( i );
												if( screenId >= 0 )
													workspaceScreens.add( screenId );
											}
											DictData dictData = (DictData)CategoryUpdate.dictMap.get( categoryID );
											final FolderInfo folderInfo = new FolderInfo();
											folderInfo.setTitle( String.format( "%s+cooee+%s" , dictData.getCn() , dictData.getEn() ) );
											folderInfo.setCategoryFolderId( categoryID );
											insertItemToDB( workspaceScreens , folderInfo );
											folderInfo.add( info );
											LauncherModel.addOrMoveItemInDatabase( mContext , info , folderInfo.getId() , 0 , info.getCellX() , info.getCellY() );
											toAdd.add( folderInfo );
											folderInfoList.add( folderInfo );
											if( LauncherDefaultConfig.SWITCH_ENABLE_CATEGORY_FOLDER_OPERATE && categoryID > 0 && categoryID != Integer.MAX_VALUE )
											{
												List<RecommendApkInfo> recommendApkInfos = CategoryHelper.getInstance( mContext ).getRecommendApkInfoList( categoryID );//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
												for( RecommendApkInfo recommendApkInfo : recommendApkInfos )
												{
													if( LauncherAppState.isApkInstalled( recommendApkInfo.getPkgName() ) || OperateDynamicProxy.getInstance().containsApp(
															recommendApkInfo.getPkgName() ) )//cheyingkun add	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
													{
														continue;
													}
													final ShortcutInfo shortcutInfo = new ShortcutInfo();
													shortcutInfo.setTitle( recommendApkInfo.getApkCN() );
													shortcutInfo.setContainer( folderInfo.getId() );
													Intent intent = null;
													if( recommendApkInfo.getApkType() == OperateDynamicUtils.VIRTUAL_APP )
													{
														intent = new Intent( OperateDynamicMain.OPERATE_DYNAMIC_FOLDER );
														ComponentName cp = new ComponentName( mContext.getPackageName() , OperateDynamicMain.OperateActivityCls );
														intent.setComponent( cp );
														intent.putExtra( OperateDynamicUtils.DYNAMIC_APP_DOWNLOAD_TYPE , recommendApkInfo.getFlag() );
														intent.putExtra( OperateDynamicUtils.DYNAMIC_APP_SIZE , recommendApkInfo.getApkSize() );
														int state = DlManager.getInstance().getPkgNameCurrentState( recommendApkInfo.getPkgName() );
														intent.putExtra( OperateDynamicMain.SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY , state );//判读是否是静默下载成功的
													}
													else if( recommendApkInfo.getApkType() == OperateDynamicUtils.VIRTUAL_LINK )
													{
														Uri uri = Uri.parse( recommendApkInfo.getPkgName() );
														intent = new Intent( Intent.ACTION_VIEW , uri );
														intent.putExtra( OperateDynamicMain.OPERATE_WEBLINKPKG , recommendApkInfo.getWebLinkPkg() );
														intent.putExtra( "linkAddr" , recommendApkInfo.getLinkAddr() );
													}
													if( intent != null )
													{
														intent.setPackage( recommendApkInfo.getPkgName() );
														intent.putExtra( OperateDynamicUtils.BITMAP_PATH_KEY , recommendApkInfo.getApkIconpath() );
														intent.putExtra( OperateDynamicMain.PKGNAME_ID , recommendApkInfo.getPkgName() );
														intent.putExtra( OperateDynamicUtils.DYNAMIC_TYPE_KEY , recommendApkInfo.getApkType() );
													}
													Intent operateIntent = new Intent();
													operateIntent.putExtra( "canUninstall" , false );
													operateIntent.putExtra( "canDrag" , false );
													operateIntent.putExtra( "isOperateVirtualItem" , true );
													operateIntent.putExtra( "iconPath" , recommendApkInfo.getApkIconpath() );
													operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_CATEGORY_FOLDER_ID , categoryID );
													operateIntent.putExtra( "canUninstall" , false );
													operateIntent.putExtra( "canDrag" , false );
													operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_IS_OPERATE_VIRTURAL_ITEM , true );
													operateIntent.putExtra( EnhanceItemInfo.INTENT_KEY_ICON_PATH , recommendApkInfo.getApkIconpath() );
													shortcutInfo.setIntent( intent );
													shortcutInfo.setCategoryFolderId( categoryID );
													shortcutInfo.setOperateVirtualItem( true );
													Bitmap bmp = BitmapFactory.decodeFile( recommendApkInfo.getApkIconpath() );
													shortcutInfo.setIcon( Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( bmp , mContext , true , false ) );
													shortcutInfo.setIconPath( recommendApkInfo.getApkIconpath() );
													shortcutInfo.setOperateIntent( operateIntent );
													folderInfo.add( shortcutInfo );
													LauncherModel.addItemToDatabase( mContext , shortcutInfo , folderInfo.getId() , 0 , shortcutInfo.getCellX() , shortcutInfo.getCellY() , false );
												}
											}
											if( mPkgName.equals( pkgName ) )
											{
												folderTitle = folderInfo.getTitle();
											}
										}
									}
									LauncherAppState.getActivityInstance().runOnUiThread( new Runnable() {
										
										@Override
										public void run()
										{
											// TODO Auto-generated method stub
											if( mListener == null )
											{
												return;
											}
											//											HashSet<ComponentName> cns = new HashSet<ComponentName>();
											//											for( ShortcutInfo info : appInfoList )
											//											{
											//												if( info != null )
											//												{
											//													if( info.getIntent() != null && info.getIntent().getComponent() != null )
											//													{
											//														cns.add( info.getIntent().getComponent() );
											//													}
											//												}
											//											}
											//											mListener.removeViewFromWorkspace( cns , false );
											//											Log.v( "Category" , "zjp cns = " + cns.toString() );
											if( toAdd.size() > 0 )
											{
												final boolean isLoaderTaskRunning = LauncherAppState.getInstance().getModel().isLoaderTaskRunning();//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页(逻辑优化)
												mListener.bindItems( toAdd , 0 , toAdd.size() , false , null , !isLoaderTaskRunning );
											}
										}
									} );
									if( folderTitle != null && !pkgName.equals( "com.android.stk" ) )
									{
										Intent intent = new Intent();
										intent.setClass( mContext , CategoryInstallActivity.class );
										Bundle bundle = new Bundle();
										bundle.putString( "packageName" , pkgName );
										bundle.putString( "folderName" , folderTitle );
										intent.putExtras( bundle );
										LauncherAppState.getActivityInstance().startActivity( intent );
									}
								}
								isDealInstallActivity = false;
							}
						} );
					}
					catch( Exception e )
					{
						isDealInstallActivity = false;
					}
				}
			} );
		}
	}
	
	public ShortcutInfo createCategoryIcon()
	{
		Resources mResources = mContext.getResources();
		String mPackageName = mContext.getPackageName();
		Intent mIntent = null;
		int mDrawableID = ResourceUtils.getDrawableResourceIdByReflectIfNecessary( -1 , mResources , mPackageName , CATEGORY_ICON_RESOURCE_NAME );
		if( mDrawableID <= 0 )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "Category" , StringUtils.concat( " error[mDrawableID] - mItemData == " , CATEGORY_ICON_RESOURCE_NAME ) );
			return null;
		}
		//mStringID
		int mStringID = ResourceUtils.getStringResourceIdByReflectIfNecessary( -1 , mResources , mPackageName , CATEGORY_TITEL_RESOURCE_NAME );
		if( mStringID <= 0 )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "Category" , StringUtils.concat( " error[mStringID] - mItemData == " , CATEGORY_TITEL_RESOURCE_NAME ) );
			return null;
		}
		//mIntent
		try
		{
			mIntent = Intent.parseUri( CATEGORY_INTENT_URI , 0 );
		}
		catch( URISyntaxException e )
		{
			e.printStackTrace();
			return null;
		}
		//xiatian start	//fix bug：解决“桌面上默认配置快捷方式和虚图标，切换语言后图标的名称没有切换为相应语言”的问题。
		//				String mTitle = mResources.getString( mStringID );//xiatian del
		String mTitle = StringUtils.concat( LauncherProvider.ITEM_TITLE_RESOURCE_NAME_KEY , mResources.getResourceName( mStringID ) );//xiatian add
		//xiatian end
		//ShortcutInfo
		ShortcutInfo mShortcutInfo = new ShortcutInfo();
		Drawable mDrawable = mResources.getDrawable( mDrawableID );
		Bitmap mBitmap = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( mDrawable , mContext , false , true );
		mShortcutInfo.setIcon( mBitmap );
		mShortcutInfo.setTitle( mTitle );
		mShortcutInfo.setIntent( mIntent );
		mShortcutInfo.setIsCustomIcon( true );
		mShortcutInfo.setItemType( LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL );
		ShortcutIconResource iconResource = new ShortcutIconResource();
		iconResource.packageName = mPackageName;
		iconResource.resourceName = mResources.getResourceName( mDrawableID );
		mShortcutInfo.setIconResource( iconResource );
		mShortcutInfo.setIsUsingFallbackIcon( false );
		return mShortcutInfo;
	}
	
	public void addOrRemoveCategoryEntry(
			boolean isAdd )
	{
		if( isAdd )
		{
			if( LauncherAppState.isAlreadyCategory( mContext ) || LauncherDefaultConfig.CONFIG_CATEGORY_TYPE != CategoryConstant.OPERATE_CATEGORY )
			{
				return;
			}
			final ShortcutInfo cateInfo = createCategoryIcon();
			if( cateInfo != null )
			{
				FolderInfo folder = null;
				ArrayList<ItemInfo> bgItems = new ArrayList<ItemInfo>( LauncherAppState.getInstance().getModel().getBgWorkspaceItems() );
				for( ItemInfo info : bgItems )
				{
					if( info instanceof ShortcutInfo )
					{
						if( info.getIntent() != null )
						{
							int virtaulaType = info.getIntent().getIntExtra( VirtualInfo.VIRTUAL_TYPE , VirtualInfo.VIRTUAL_TYPE_ERROR );
							if( virtaulaType == VirtualInfo.VIRTUAL_TYPE_CATEGORY_ENTRY )
							{
								return;
							}
						}
					}
					else if( info instanceof FolderInfo )
					{
						if( info != null )
						{
							if( info.getTitle().equals( ResourceUtils.getStringByReflectIfNecessary( "default_folder" ) ) )
							{
								folder = (FolderInfo)info;
							}
							for( ShortcutInfo item : ( (FolderInfo)info ).getContents() )
							{
								if( item.getIntent() != null )
								{
									int virtaulaType = item.getIntent().getIntExtra( VirtualInfo.VIRTUAL_TYPE , VirtualInfo.VIRTUAL_TYPE_ERROR );
									if( virtaulaType == VirtualInfo.VIRTUAL_TYPE_CATEGORY_ENTRY )
									{
										return;
									}
								}
							}
						}
					}
				}
				final FolderInfo default_folder = folder;
				LauncherAppState.getActivityInstance().runOnUiThread( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						if( default_folder != null )
						{
							default_folder.add( cateInfo );
						}
						else
						{
							ArrayList<ItemInfo> infoList = new ArrayList<ItemInfo>();
							infoList.add( cateInfo );
							final boolean isLoaderTaskRunning = LauncherAppState.getInstance().getModel().isLoaderTaskRunning();//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页(逻辑优化)
							LauncherAppState.getInstance().getModel().addAndBindAddedItems( mContext , infoList , null , null , true , !isLoaderTaskRunning );
						}
					}
				} );
			}
		}
		else
		{
			if( !LauncherAppState.isAlreadyCategory( mContext ) )
			{
				try
				{
					final Intent mIntent = Intent.parseUri( CATEGORY_INTENT_URI , 0 );
					if( mIntent != null && mIntent.getComponent() != null )
					{
						LauncherAppState.getActivityInstance().runOnUiThread( new Runnable() {
							
							@Override
							public void run()
							{
								// TODO Auto-generated method stub
								if( mListener != null )
								{
									HashSet<ComponentName> comps = new HashSet<ComponentName>();
									comps.add( mIntent.getComponent() );
									mListener.removeViewFromWorkspace( comps , true );
								}
							}
						} );
					}
				}
				catch( URISyntaxException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
}
