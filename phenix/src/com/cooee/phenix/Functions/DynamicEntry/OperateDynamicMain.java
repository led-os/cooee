package com.cooee.phenix.Functions.DynamicEntry;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.function.DynamicEntry.DLManager.Constants;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.AllAppsList;
import com.cooee.phenix.BubbleTextView;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherModel;
import com.cooee.phenix.LauncherProvider;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.Folder.FolderIcon;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicModel.IOperateCallbacks;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.ShortcutInfo;


public class OperateDynamicMain
{
	
	private Context mContext = null;
	/**
	 * 通过应用的包名将shortcutInfo存储起来
	 */
	private static HashMap<String , ShortcutInfo> mAllOperateIcon = new HashMap<String , ShortcutInfo>();
	private OperateFolderDatabase mOperateFolderDatabase = null;
	public static final String OperateActivityCls = "com.cooee.phenix.Functions.DynamicEntry.OperateActivity";
	/**
	 * 存储于运营文件夹中的shortcutInfo的intent中，表示为运营文件夹中的应用
	 */
	public static final String OPERATE_DYNAMIC_FOLDER = "OperateDynamicFolder";
	/**
	 * 表示运营文件夹的最低id，数据库使用
	 */
	public static final int OPERATE_MINID = 10000;
	public static final String PKGNAME_ID = "pkgName";
	public static final String FOLDER_VERSION = "folderVersion";
	public static final String OPEARTE_DYNAMIC_ID = "dynamicID";//用来存储运营对用的id
	public static final String OPERATE_WEBLINKPKG = "weblinkpkg";
	public static final String DEFAULT_OPERATE_FOLDER = "defaultfolder";
	public static final String OPERATE_DATA_DEFAULT = "defaultData";
	private OperateDynamicModel mOperateDynamicModel = null;
	private LauncherModel mModel = null;
	private static ArrayList<View> mAllOperateFolderIcons = new ArrayList<View>();
	private static String mOldOperateFolderVersion = null;
	/*用于开启一个延迟的线程  start*/
	private Handler mHandler = null;
	private int mStartNum = 0;
	private int mAllNum = 0;
	private ArrayList<ItemInfo> mStartFolders = new ArrayList<ItemInfo>();
	private final long DELAY_TIME = 1000;
	/*用于开启一个延迟的线程 end*/
	/**
	 * 智能分类与运营文件夹加载顺序逻辑
	 * a:若start或者stop智能分类先开始，此时进行运营文件夹更新或者加载，则此时等待start或者stop智能分类结束了，再加载文件夹
	 * b:若运营文件夹先开始，此时start或者stop智能智能分类执行的话，则等待运营文件夹结束以后再执行start或者stop智能分类
	 */
	private boolean mIsOperateFolderRunning = false;//判断是否正在加载运营文件夹
	public final static String SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY = "showOperateIconDownOrInstall";//判断运营图标，是显示下载图标，还是显示安装图标的一个key，存在于intent中传递 wanghongjian add 
	
	public OperateDynamicMain(
			Context context ,
			IOperateCallbacks callbacks ,
			LauncherModel model )
	{
		mContext = context;
		mModel = model;
		mOperateFolderDatabase = new OperateFolderDatabase( LauncherAppState.getLauncherProvider().getProviderDB() , this );
		mOperateDynamicModel = new OperateDynamicModel( callbacks , this , context );
		mHandler = new Handler( mContext.getMainLooper() );
	}
	
	private void removeFolderInWorkspace(
			ArrayList<ItemInfo> newInfos )
	{
		if( mAllOperateFolderIcons.size() > 0 )
			mOperateDynamicModel.removeFolderInWorkspace( mAllOperateFolderIcons , newInfos , true );
	}
	
	public OperateDynamicModel getOperateDynamicModel()
	{
		return mOperateDynamicModel;
	}
	
	/**
	 * 清楚所有已经存在的文件夹信息，包括数据库
	 */
	private void clearAllFolderData(
			ArrayList<ItemInfo> allFolderInfos )
	{
		mOperateDynamicModel.removeFolderInfo( allFolderInfos );
		removeFolderInWorkspace( allFolderInfos );
		deleteFolderAllDataBase();//之前注释掉是因为allFolderInfos数据里不包含显性文件夹的数据，防止显性文件夹数据被清除掉，allFolderInfos包含了显性数据
		clearAllIconData();
		mOldOperateFolderVersion = null;
		// zhujieping@2015/06/08 ADD START,清空未执行的生成运营文件夹的runnable和list,并清除数据库
		removeRunnableFromList();
		// zhujieping@2015/06/08 ADD END
	}
	
	public static boolean shouldOperateFolderDelete(
			FolderInfo folderInfo )
	{
		if( folderInfo.getOperateIntent().getBooleanExtra( OperateDynamicMain.DEFAULT_OPERATE_FOLDER , false ) )
		{
			return false;
		}
		boolean isDelete = true;
		for( ShortcutInfo info : folderInfo.getContents() )
		{
			if( !info.isOperateIconItem() )
			{
				isDelete = false;
				break;
			}
		}
		return isDelete;
	}
	
	public static boolean shouldOperateShortcutDelete(
			ShortcutInfo info ,
			ArrayList<ItemInfo> newInfos )
	{
		if( info == null || info.getIntent() == null || newInfos == null )
		{
			return true;
		}
		Intent intent = info.getIntent();
		if( intent.getBooleanExtra( DEFAULT_OPERATE_FOLDER , false ) )//显性文件夹
		{
			String dynamicId = intent.getStringExtra( OPEARTE_DYNAMIC_ID );
			if( dynamicId != null )
			{
				for( ItemInfo iteminfo : newInfos )//遍历新数据，如果没有，不删除数据
				{
					String id = null;
					if( iteminfo instanceof FolderInfo )
					{
						id = ( (FolderInfo)iteminfo ).getOperateIntent().getStringExtra( OPEARTE_DYNAMIC_ID );
					}
					else if( iteminfo instanceof ShortcutInfo )
					{
						id = ( (ShortcutInfo)iteminfo ).getIntent().getStringExtra( OPEARTE_DYNAMIC_ID );
					}
					if( dynamicId.equals( id ) )
					{
						return true;
					}
				}
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 清除掉mAllOperateFolderIcons中的数据
	 */
	public void clearAllIconData()
	{
		if( mAllOperateFolderIcons.size() > 0 )
		{
			mOldOperateFolderVersion = null;
			mAllOperateFolderIcons.clear();
			mAllOperateIcon.clear();
		}
	}
	
	/**
	 * 通过数据库中的数据生成新的运营文件夹,在此之前先删除掉以前所有的文件夹信息
	 */
	public void addOperateFolderFromDb()
	{
		mOperateFolderDatabase.getFolderInfoAndShourtInfoByDb();
		ArrayList<ItemInfo> folderInfos = new ArrayList<ItemInfo>();
		for( ItemInfo info : mOperateFolderDatabase.getmFolderInfos() )//判断文件夹中icon的个数，为0或1的话就解散
		{
			if( info instanceof FolderInfo )
			{
				if( ( (FolderInfo)info ).getContents().size() == 0 )
				{
					continue;
				}
				if( ( (FolderInfo)info ).getContents().size() == 1 )
				{
					( (FolderInfo)info ).getContents().get( 0 ).setContainer( LauncherSettings.Favorites.CONTAINER_DESKTOP );
					folderInfos.add( ( (FolderInfo)info ).getContents().get( 0 ) );
					continue;
				}
			}
			folderInfos.add( info );
		}
		addOperateFolder( folderInfos );
	}
	
	/**
	 * 判断是否允许创建或者更新运营文件夹数据
	 * @return
	 */
	private boolean isAllowToOperate()
	{
		return mOperateDynamicModel.isAllowToOperate();
	}
	
	/**
	 * 此时是通过allFolderInfos生成最新的文件夹,该接口试用于以前有文件夹显示了，此时先删除以前老的数据，再加载最新的数据
	 * @param context
	 * @param allFolderInfos
	 */
	public boolean clearAndAddFolderByFolderList(
			Context context ,
			final ArrayList<ItemInfo> allFolderInfos )
	{
		if( !isAllowToOperate() )
		{
			// zhujieping@2015/05/20 ADD START，桌面有拖动图标时，不添加运营文件夹，置回标志位，否则无法进行分类
			setOperateFolderRunning( false );
			// zhujieping@2015/05/20 ADD END
			return false;
		}
		clearAllFolderData( allFolderInfos );
		for( ItemInfo info : allFolderInfos )
		{
			if( info instanceof FolderInfo )
			{
				String dynamicid = ( (FolderInfo)info ).getOperateIntent().getStringExtra( OPEARTE_DYNAMIC_ID );
				FolderInfo fi = mOperateDynamicModel.getSameOperateFolder( dynamicid );
				if( fi != null && fi.getOperateIntent().getBooleanExtra( DEFAULT_OPERATE_FOLDER , false ) )
				{
					info.setTitle( fi.getTitleString() );//显性文件夹被更新时，文件夹名字不修改
					( (FolderInfo)info ).getOperateIntent().putExtra( DEFAULT_OPERATE_FOLDER , true );
					if( ( (FolderInfo)info ).getOperateIntent().getBooleanExtra( OPERATE_DATA_DEFAULT , false ) )
					{
						( (FolderInfo)info ).getOperateIntent().putExtra( OperateDynamicUtils.DYNAMIC_HOT , fi.getOperateIntent().getBooleanExtra( OperateDynamicUtils.DYNAMIC_HOT , false ) );
					}
				}
			}
		}
		OperateFolderDatabase.addFolderInfosToDatabase( context , LauncherAppState.getLauncherProvider().getProviderDB() , allFolderInfos );
		Handler mMainHandler = new Handler( mContext.getMainLooper() );
		mMainHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				exitOverviewMode();
				addOperateFolder( allFolderInfos );
				// zhujieping@2015/06/24 DEL START,挪到删除icon的地方删除空白页，未删除icon就不删除空白页
				//				mOperateDynamicModel.removeEmptyScreen();
				// zhujieping@2015/06/24 DEL END
			}
		} );
		return true;
	}
	
	/**
	 * 如果此时在编辑模式,则先退出编辑模式
	 */
	private void exitOverviewMode()
	{
		mOperateDynamicModel.exitOverviewMode();
	}
	
	/**
	 *根据所给的allFolderInfos查找空位，并显示到workspace上
	 * @param allFolderInfos
	 */
	private void addOperateFolder(
			ArrayList<ItemInfo> allFolderInfos )
	{
		//		for( int i = 0 ; i < allFolderInfos.size() ; i++ )
		//		{
		//			final FolderInfo folderInfo = (FolderInfo)allFolderInfos.get( i );
		//			setFolderPositon( folderInfo );
		//			ArrayList<ItemInfo> oneFolders = new ArrayList<ItemInfo>();
		//			oneFolders.add( folderInfo );
		//			mOperateDynamicModel.bindItems( oneFolders );
		//			addItemToFavoriteDatabase( mContext , folderInfo );
		//		}
		mStartNum = 0;
		mAllNum = allFolderInfos.size();
		mStartFolders.clear();
		for( int i = 0 ; i < allFolderInfos.size() ; i++ )
		{
			mStartFolders.add( allFolderInfos.get( i ) );
		}
		mHandler.postDelayed( runnable , DELAY_TIME );
	}
	
	/**
	 * 添加文件夹的时候，每隔一秒后添加一次，防止添加的时候滑动桌面会卡顿的问题
	 */
	private Runnable runnable = new Runnable() {
		
		@Override
		public void run()
		{
			if( mStartNum < mAllNum )
			{
				ItemInfo info = mStartFolders.get( mStartNum );
				addOperateFolder( info );
				mHandler.postDelayed( runnable , DELAY_TIME );
			}
			if( mStartNum == mAllNum )//此时表示运营文件夹已经加载或者更新结束 wanghongjian add
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "" , "whj OperateStart 运营文件夹结束" );
				setOperateFolderRunning( false );
				mOperateDynamicModel.bindOperateFoloderEnd();
				OperateDynamicProxy.getInstance().cancelDynamicUpdateWaiteDialog( true );
			}
			mStartNum++;
		}
	};
	
	private void addOperateFolder(
			ItemInfo folderInfo )
	{
		if( folderInfo instanceof FolderInfo )
		{
			String dynamicid = ( (FolderInfo)folderInfo ).getOperateIntent().getStringExtra( OPEARTE_DYNAMIC_ID );
			if( dynamicid != null )
			{
				FolderInfo info = mOperateDynamicModel.getSameOperateFolder( dynamicid );//id与文件夹的id一致，则将icon直接加到显性文件夹中
				if( info != null )
				{
					Intent opIntent = info.getOperateIntent();
					if( !( (FolderInfo)folderInfo ).getOperateIntent().getBooleanExtra( OPERATE_DATA_DEFAULT , false ) )
					{
						opIntent.putExtra( OperateDynamicUtils.DYNAMIC_HOT , ( (FolderInfo)folderInfo ).getOperateIntent().getBooleanExtra( OperateDynamicUtils.DYNAMIC_HOT , false ) );
					}
					opIntent.putExtra( Constants.NEW_ICON_COUNT , ( (FolderInfo)folderInfo ).getContents().size() );
					opIntent.putExtra( OperateDynamicMain.FOLDER_VERSION , ( (FolderInfo)folderInfo ).getOperateIntent().getStringExtra( FOLDER_VERSION ) );
					LauncherModel.updateItemInDatabase( mContext , info );
					for( ShortcutInfo si : ( (FolderInfo)folderInfo ).getContents() )
					{
						OperateFolderDatabase.addItemToFavoriteDatabase( mContext , si );
						info.add( si );
					}
					return;
				}
			}
		}
		setFolderPositon( folderInfo );
		ArrayList<ItemInfo> oneFolders = new ArrayList<ItemInfo>();
		oneFolders.add( folderInfo );
		OperateFolderDatabase.addItemToFavoriteDatabase( mContext , folderInfo );
		mOperateDynamicModel.bindItems( oneFolders );
	}
	
	/**
	 * 将shortcutInfo保存到一个mAllOperateIcon中,通过pkgName匹配
	 */
	public static void addShortcutToAllOperateIcon(
			ItemInfo info )
	{
		if( info instanceof FolderInfo )
		{
			for( int j = 0 ; j < ( (FolderInfo)info ).getContents().size() ; j++ )
			{
				addOperateIcon( ( (FolderInfo)info ).getContents().get( j ) );
			}
		}
		else if( info instanceof ShortcutInfo )
		{
			addOperateIcon( (ShortcutInfo)info );
		}
	}
	
	private static void addOperateIcon(
			ShortcutInfo shortcutInfo )
	{
		shortcutInfo.setShortcutType( LauncherSettings.Favorites.SHORTCUT_TYPE_OPERATE_DYNAMIC );
		String pkgName = shortcutInfo.getIntent().getStringExtra( PKGNAME_ID );
		mAllOperateIcon.put( pkgName , shortcutInfo );
	}
	
	/**
	 * 查找出最新的空位,,查找规则与新安装应用规则一致,将查找出的空位都存在folderInfo中
	 * 
	 */
	private void setFolderPositon(
			ItemInfo info )
	{
		final ArrayList<Long> addedWorkspaceScreensFinal = new ArrayList<Long>();
		LauncherModel model = mModel;
		ArrayList<Long> workspaceScreens = new ArrayList<Long>();
		for( int i = 0 ; i < mOperateDynamicModel.getScreenOrder().size() ; i++ )
		{
			workspaceScreens.add( mOperateDynamicModel.getScreenOrder().get( i ) );
		}
		//		TreeMap<Integer , Long> orderedScreens = LauncherModel.loadWorkspaceScreensDb( mContext );
		//		for( Integer i : orderedScreens.keySet() )
		//		{
		//			long screenId = orderedScreens.get( i );
		//			Log.v( "" , "whj setFolderPositon screenId " + screenId );
		//			workspaceScreens.add( screenId );
		//		}
		int startSearchPageIndex = 0;
		Pair<Long , int[]> coords = LauncherModel.findNextAvailableIconSpace( mContext , startSearchPageIndex , workspaceScreens );
		if( coords == null )
		{
			if( workspaceScreens.size() > 0 )
			{
				startSearchPageIndex++;
			}
			LauncherProvider lp = LauncherAppState.getLauncherProvider();
			int numPagesToAdd = Math.max( 1 , startSearchPageIndex + 1 - workspaceScreens.size() );
			while( numPagesToAdd > 0 )
			{
				if( workspaceScreens.size() > 0 )
				{
					//					lp.updateMaxScreenId( workspaceScreens.get( workspaceScreens.size() - 1 ) );
					lp.updateMaxScreenId( model.getWorkspaceScreensMaxID( workspaceScreens ) );
				}
				long screenId = lp.generateNewScreenId();
				workspaceScreens.add( screenId );
				addedWorkspaceScreensFinal.add( screenId );
				numPagesToAdd--;
			}
			coords = LauncherModel.findNextAvailableIconSpace( mContext , startSearchPageIndex , workspaceScreens );
			if( coords != null )
			{
				mOperateDynamicModel.bindAddScreens( addedWorkspaceScreensFinal );
				model.updateWorkspaceScreenOrder( mContext , workspaceScreens );
			}
		}
		if( coords == null )
		{
			throw new RuntimeException( "Coordinates should not be null" );
		}
		if( coords != null )
		{
			info.setCellX( coords.second[0] );
			info.setCellY( coords.second[1] );
			info.setScreenId( coords.first );
		}
	}
	
	public boolean updateFolderIcon(
			String pkgName )
	{
		if( pkgName == null )
		{
			return false;
		}
		for( View icon : mAllOperateFolderIcons )
		{
			if( icon instanceof BubbleTextView )
			{
				ShortcutInfo shortcutInfo = null;
				if( icon.getTag() != null && icon.getTag() instanceof ShortcutInfo )
				{
					shortcutInfo = (ShortcutInfo)icon.getTag();
				}
				if( shortcutInfo != null && shortcutInfo.getIntent() != null )
				{
					if( pkgName.equals( shortcutInfo.getIntent().getStringExtra( PKGNAME_ID ) ) )
					{
						List<ResolveInfo> matches = AllAppsList.findActivitiesForPackage( mContext , pkgName );
						if( matches.size() > 0 )
						{
							OperateFolderDatabase.delete( shortcutInfo );
							mAllOperateIcon.remove( pkgName );
							ResolveInfo info = matches.get( 0 );
							{
								ComponentName componentName = new ComponentName( pkgName , info.activityInfo.name );
								shortcutInfo.setShortcutType( LauncherSettings.Favorites.SHORTCUT_TYPE_NORMAL );
								shortcutInfo.setActivity( mContext , componentName , Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
							}
							Bitmap bmp = getAllIconBitmap().get( pkgName );
							if( bmp != null && !bmp.isRecycled() )
							{
								bmp.recycle();
								getAllIconBitmap().remove( bmp );
							}
							shortcutInfo.setShortcutType( LauncherSettings.Favorites.SHORTCUT_TYPE_NORMAL );
							shortcutInfo.updateIcon( LauncherAppState.getInstance().getIconCache() );
							( (BubbleTextView)icon ).applyFromShortcutInfo( shortcutInfo , LauncherAppState.getInstance().getIconCache() );
							icon.setLongClickable( true );
							shortcutInfo.setIsCustomIcon( false );
							updateIconIntentDataBase( shortcutInfo , false );
							mAllOperateFolderIcons.remove( icon );
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public int getOperateDynamicIconCount(
			ArrayList<ShortcutInfo> list )
	{
		int count = 0;
		for( ShortcutInfo info : list )
		{
			if( info.getShortcutType() == LauncherSettings.Favorites.SHORTCUT_TYPE_OPERATE_DYNAMIC )
			{
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 如果下载安装的是运营文件夹中的应用，则此时就将文件夹中的icon删除
	 * @param addedApps
	 */
	public void removeFolderIcon(
			ArrayList<AppInfo> addedApps )
	{
		for( int i = 0 ; i < addedApps.size() ; i++ )
		{
			AppInfo info = addedApps.get( i );
			if( info.getIntent() != null && info.getIntent().getComponent() != null )
			{
				String pkgName = info.getIntent().getComponent().getPackageName();
				removeFolderIcon( pkgName );
			}
		}
	}
	
	public void removeFolderIcon(
			String pkgName )
	{
		if( pkgName == null )
		{
			return;
		}
		out:
		for( int j = 0 ; j < mAllOperateFolderIcons.size() ; j++ )
		{
			if( mAllOperateFolderIcons.get( j ) instanceof FolderIcon )
			{
				FolderInfo folderInfo = ( (FolderIcon)mAllOperateFolderIcons.get( j ) ).getFolderInfo();
				for( int n = 0 ; n < folderInfo.getContents().size() ; n++ )
				{
					ShortcutInfo shortcutInfo = folderInfo.getContents().get( n );
					if( pkgName.equals( shortcutInfo.getIntent().getStringExtra( PKGNAME_ID ) ) )
					{
						LauncherModel.deleteItemFromDatabase( mContext , shortcutInfo );
						folderInfo.remove( shortcutInfo );
						OperateFolderDatabase.delete( shortcutInfo );
						mAllOperateIcon.remove( pkgName );
						if( shortcutInfo.getIcon() != null && !shortcutInfo.getIcon().isRecycled() )
						{
							shortcutInfo.getIcon().recycle();
						}
						getAllIconBitmap().remove( pkgName );
						if( folderInfo.getContents().size() == 0 )
						{
							LauncherModel.deleteItemFromDatabase( mContext , folderInfo );
							delateFolderIcon( mAllOperateFolderIcons.get( j ) );
						}
						break out;
					}
				}
			}
			else if( mAllOperateFolderIcons.get( j ) instanceof BubbleTextView )
			{
				Object obj = mAllOperateFolderIcons.get( j ).getTag();
				if( obj != null && obj instanceof ShortcutInfo )
				{
					ShortcutInfo shortcutInfo = (ShortcutInfo)obj;
					if( shortcutInfo.getIntent().getStringExtra( PKGNAME_ID ).equals( pkgName ) )
					{
						LauncherModel.deleteItemFromDatabase( mContext , shortcutInfo );
						mAllOperateIcon.remove( pkgName );
						delateFolderIcon( mAllOperateFolderIcons.get( j ) );
						break out;
					}
				}
			}
		}
	}
	
	/**
	 * 通过传入的包名查找出要替换图标的icon
	 * @param pkgName
	 */
	public void refreshOperateFolderIconState(
			ArrayList<View> allicons ,
			String pkgName ,
			int state )//true表示下载完成，更新图标，false表示删掉下载内容，同样更新图标
	{
		if( allicons == null || pkgName == null )
		{
			return;
		}
		mOperateFolderDatabase.updateStateDataBase( pkgName , state );
		for( int i = 0 ; i < allicons.size() ; i++ )
		{
			View icon = allicons.get( i );
			if( icon instanceof FolderIcon )
			{
				for( int j = 0 ; j < ( (FolderIcon)icon ).getFolderInfo().getContents().size() ; j++ )
				{
					ShortcutInfo shortcutInfo = ( (FolderIcon)icon ).getFolderInfo().getContents().get( j );
					if( shortcutInfo.getIntent() != null )
					{
						String iconPkgName = shortcutInfo.getIntent().getStringExtra( PKGNAME_ID );
						if( pkgName.equals( iconPkgName ) )
						{
							shortcutInfo.getIntent().putExtra( SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY , state );//表示此时已经下载完毕，更新数据库使用
							( (FolderIcon)icon ).getFolder().refreshOperateFolderIconState( pkgName , PKGNAME_ID , state );
							//							updateIconIntentDataBase( shortcutInfo , true );
							return;
						}
					}
				}
			}
			else if( icon instanceof BubbleTextView )
			{
				Object obj = icon.getTag();
				if( obj instanceof ShortcutInfo )
				{
					ShortcutInfo shortcutInfo = (ShortcutInfo)obj;
					if( shortcutInfo.getIntent() != null )
					{
						String iconPkgName = shortcutInfo.getIntent().getStringExtra( PKGNAME_ID );
						if( pkgName.equals( iconPkgName ) )
						{
							shortcutInfo.getIntent().putExtra( SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY , state );//表示此时已经下载完毕，更新数据库使用
							( (BubbleTextView)icon ).setOperateIconLoadDone( state );
							( (BubbleTextView)icon ).postInvalidate();
							//							updateIconIntentDataBase( shortcutInfo , true );
							return;
						}
					}
				}
			}
		}
	}
	
	/**
	 * 更新运营Icon数据库，包括launcher中的数据库和运营文件夹中的数据库
	 * @param shortcutInfo
	 */
	private void updateIconIntentDataBase(
			ItemInfo info ,
			boolean isOnlyupdateIntent )
	{
		LauncherModel.updateItemInDatabase( mContext , info , isOnlyupdateIntent );
		mOperateFolderDatabase.updateIconIntentDataBase( info );
	}
	
	/**
	 * 当文件夹内没有图标了以后，将文件夹在桌面上删除
	 * @param folderIcon
	 */
	private void delateFolderIcon(
			View icon )
	{
		ItemInfo info = null;
		if( icon instanceof FolderIcon )
		{
			info = ( (FolderIcon)icon ).getFolderInfo();
		}
		else if( icon instanceof BubbleTextView )
		{
			Object obj = icon.getTag();
			if( obj != null && obj instanceof ItemInfo )
			{
				info = (ItemInfo)obj;
			}
		}
		if( info == null )
		{
			return;
		}
		mOperateFolderDatabase.delete( info );
		ArrayList<View> fs = new ArrayList<View>();
		fs.add( icon );
		mOperateDynamicModel.removeFolderInWorkspace( fs , null , false );
		mAllOperateFolderIcons.remove( icon );
	}
	
	/**
	 * 判断是否存在运营文件夹
	 * @return
	 */
	public boolean isExitOperateFolderDB()
	{
		return mOperateFolderDatabase.isExitOperateFolderDB();
	}
	
	/**
	 * 删除该表中所有的数据
	 */
	public void deleteFolderAllDataBase()
	{
		mOperateFolderDatabase.clearFolderAllData();
	}
	
	/**
	 * 查询运营文件夹中数据库中的version值
	 * @return
	 */
	public String findOperateFolderVersion()
	{
		String version = mOperateFolderDatabase.findOperateFolderVersion();
		return version;
	}
	
	public static FolderInfo createFolderInfo(
			long container ,
			int itemType ,
			String title ,
			long screen ,
			int cellX ,
			int cellY ,
			Intent opIntent )
	{
		FolderInfo folderInfo = new FolderInfo();
		folderInfo.setContainer( container );
		folderInfo.setTitle( title );
		folderInfo.setItemType( itemType );
		folderInfo.setFolderType( LauncherSettings.Favorites.FOLDER_TYPE_OPERATE_DYNAMIC );
		folderInfo.setScreenId( screen );
		folderInfo.setCellX( cellX );
		folderInfo.setCellY( cellY );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , StringUtils.concat( "of:" , title , "," , screen , "," , cellX , "," , cellY ) );
		folderInfo.setOperateIntent( opIntent );
		return folderInfo;
	}
	
	/**
	 * 创建新的运营文件夹info
	 */
	public static FolderInfo createFolderInfo(
			long container ,
			int itemType ,
			String version ,
			String title ,
			long screen ,
			int cellX ,
			int cellY ,
			String dynamicID )
	{
		FolderInfo folderInfo = new FolderInfo();
		folderInfo.setContainer( container );
		folderInfo.setTitle( title );
		folderInfo.setItemType( itemType );
		folderInfo.setFolderType( LauncherSettings.Favorites.FOLDER_TYPE_OPERATE_DYNAMIC );
		folderInfo.setScreenId( screen );
		folderInfo.setCellX( cellX );
		folderInfo.setCellY( cellY );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , StringUtils.concat( "of:" , title , "," , screen , "," , cellX , "," , cellY ) );
		Intent operateIntent = new Intent( OperateDynamicMain.OPERATE_DYNAMIC_FOLDER );
		operateIntent.putExtra( OperateDynamicMain.FOLDER_VERSION , version );
		operateIntent.putExtra( OPEARTE_DYNAMIC_ID , dynamicID );
		folderInfo.setOperateIntent( operateIntent );
		return folderInfo;
	}
	
	/**
	 * 创建ShortcutInfo
	 * @param bitmap
	 * @param title
	 * @param intent
	 * @param container
	 * @return
	 */
	public static ShortcutInfo createShortcutInfo(
			Bitmap bitmap ,
			String title ,
			Intent intent ,
			long container )
	{
		return createShortcutInfo( bitmap , title , intent , container , LauncherSettings.Favorites.SHORTCUT_TYPE_OPERATE_DYNAMIC );
	}
	
	public static ShortcutInfo createShortcutInfo(
			Bitmap bitmap ,
			String title ,
			Intent intent ,
			long container ,
			int shortcutType )
	{
		ShortcutInfo shortcutInfo = new ShortcutInfo();
		shortcutInfo.setShortcutType( shortcutType );
		shortcutInfo.setIcon( bitmap );
		//xiatian add start	//fix bug：解决“运营文件夹的item的iconType被设置为ICON_TYPE_RESOURCE（应该是ICON_TYPE_BITMAP）”的问题。
		//【备注】
		//		1、运营文件夹的item应改为ICON_TYPE_BITMAP
		//		2、如果被设置为ICON_TYPE_RESOURCE的话，在LauncherModel的方法
		//			“private ShortcutInfo getShortcutInfo(Cursor c ,Context context ,int itemType ,int iconTypeIndex ,int iconPackageIndex ,int iconResourceIndex ,int iconIndex ,int titleIndex ,Intent intent ,Intent operateIntent )”
		//			中会走错switch-case的分支。
		//		3、由于上述方法有保护，才确保iconType设置错误仍能取到正确图片。
		shortcutInfo.setIsCustomIcon( true );
		//xiatian add end
		shortcutInfo.setTitle( title );
		shortcutInfo.setIntent( intent );
		shortcutInfo.setContainer( container );
		return shortcutInfo;
	}
	
	/**
	 * 比较运营文件夹数据库中的shortcut个数与favorite数据库中运营文件夹的
	 * @return
	 */
	public boolean isShortcutSizeChange()
	{
		int favoriteSize = mAllOperateIcon.size();
		int operteDBSize = mOperateFolderDatabase.findOperateFolderAllShortCutSize();
		if( favoriteSize != operteDBSize )
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 查找出运营文件夹数据库中所有ArrayList<FolderInfo>
	 * @return
	 */
	public ArrayList<ItemInfo> getFolderInfoListByOperateDB()
	{
		mOperateFolderDatabase.getFolderInfoAndShourtInfoByDb();
		ArrayList<ItemInfo> folderInfos = new ArrayList<ItemInfo>();
		for( ItemInfo info : mOperateFolderDatabase.getmFolderInfos() )
		{
			if( info instanceof FolderInfo )
			{
				if( ( (FolderInfo)info ).getContents().size() == 0 )
				{
					continue;
				}
				if( ( (FolderInfo)info ).getContents().size() == 1 )
				{
					( (FolderInfo)info ).getContents().get( 0 ).setContainer( LauncherSettings.Favorites.CONTAINER_DESKTOP );
					folderInfos.add( ( (FolderInfo)info ).getContents().get( 0 ) );
					continue;
				}
			}
			folderInfos.add( info );
		}
		return folderInfos;
	}
	
	public static HashMap<String , ShortcutInfo> getmAllOperateIcon()
	{
		return mAllOperateIcon;
	}
	
	public static ArrayList<View> getmAllOperateFolderIcons()
	{
		return mAllOperateFolderIcons;
	}
	
	public static void updateOperateIcons(
			View view )
	{
		boolean isUpdate = false;
		for( View icon : mAllOperateFolderIcons )
		{
			if( view instanceof BubbleTextView && icon instanceof BubbleTextView )
			{
				if( icon.getTag() == view.getTag() )
				{
					mAllOperateFolderIcons.remove( icon );
					mAllOperateFolderIcons.add( view );
					isUpdate = true;
					break;
				}
			}
			else if( view instanceof FolderIcon && icon instanceof FolderIcon )
			{
				if( ( (FolderIcon)icon ).getFolderInfo() == ( (FolderIcon)view ).getFolderInfo() )
				{
					mAllOperateFolderIcons.remove( icon );
					mAllOperateFolderIcons.add( view );
					isUpdate = true;
					break;
				}
			}
		}
		if( !isUpdate )
		{
			mAllOperateFolderIcons.add( view );
		}
	}
	
	public static String getmOldOperateFolderVersion()
	{
		return mOldOperateFolderVersion;
	}
	
	public static void setmOldOperateFolderVersion(
			String mOldOperateFolderVersion )
	{
		OperateDynamicMain.mOldOperateFolderVersion = mOldOperateFolderVersion;
	}
	
	public HashMap<String , Bitmap> getAllIconBitmap()
	{
		return mOperateDynamicModel.getmAllIconBitmap();
	}
	
	public void reStartLoadData()
	{
		mOperateDynamicModel.reStartLoadData();
	}
	
	// zhujieping@2015/06/03 ADD START,list用来存储在桌面生成图表的runnable
	private static ArrayList<Runnable> mAddRunnable = new ArrayList<Runnable>();
	
	public static void addRunnableToList(
			Runnable r )
	{
		mAddRunnable.add( r );
	}
	
	public void removeRunnableFromList()
	{
		if( mAddRunnable.size() > 0 )
		{
			mOperateDynamicModel.removewaitUntilResumeRunnable( mAddRunnable );
			mAddRunnable.clear();
		}
	}
	
	// zhujieping@2015/06/03 ADD END
	public boolean isOperateFolderRunning()
	{
		return mIsOperateFolderRunning;
	}
	
	public void setOperateFolderRunning(
			boolean mIsOperateFolderRunning )
	{
		this.mIsOperateFolderRunning = mIsOperateFolderRunning;
	}
	
	public void updateAddedIconInOperateFolder(
			final ArrayList<ItemInfo> addNotAnimated ,
			final ArrayList<ItemInfo> addAnimated ,
			final ArrayList<AppInfo> addedApps )
	{
		List<String> list = new ArrayList<String>();
		for( AppInfo info : addedApps )
		{
			if( info.getIntent() != null && info.getIntent().getComponent() != null )
			{
				String pkgName = info.getIntent().getComponent().getPackageName();
				if( updateFolderIcon( pkgName ) )
				{
					list.add( pkgName );
				}
			}
		}
		Iterator<ItemInfo> iter = addNotAnimated.iterator();
		while( iter.hasNext() )
		{
			ItemInfo info = iter.next();
			if( info.getIntent() != null && info.getIntent().getComponent() != null )
			{
				String pkgName = info.getIntent().getComponent().getPackageName();
				if( list.indexOf( pkgName ) >= 0 )
				{
					iter.remove();
					LauncherModel.deleteItemFromDatabase( mContext , info );
				}
			}
		}
		Iterator<ItemInfo> it = addAnimated.iterator();
		while( it.hasNext() )
		{
			ItemInfo info = it.next();
			if( info.getIntent() != null && info.getIntent().getComponent() != null )
			{
				String pkgName = info.getIntent().getComponent().getPackageName();
				if( list.indexOf( pkgName ) >= 0 )
				{
					it.remove();
					LauncherModel.deleteItemFromDatabase( mContext , info );
				}
			}
		}
	}
	
	public boolean hideOperateFolderHot(
			ItemInfo info )
	{
		if( info instanceof FolderInfo )
		{
			FolderInfo folderInfo = (FolderInfo)info;
			if( folderInfo.getFolderType() == LauncherSettings.Favorites.FOLDER_TYPE_OPERATE_DYNAMIC )
			{
				if( folderInfo.getOperateIntent().getBooleanExtra( OperateDynamicUtils.DYNAMIC_HOT , false ) )
				{
					folderInfo.getOperateIntent().putExtra( OperateDynamicUtils.DYNAMIC_HOT , false );
					updateIconIntentDataBase( folderInfo , true );
					String id = folderInfo.getOperateIntent().getStringExtra( OperateDynamicMain.OPEARTE_DYNAMIC_ID );
					OperateDynamicProxy.getInstance().hideOperateFolderHot( id , true );
					return true;
				}
			}
		}
		else if( info instanceof ShortcutInfo )
		{
			ShortcutInfo shortcutInfo = (ShortcutInfo)info;
			if( shortcutInfo.getShortcutType() == LauncherSettings.Favorites.SHORTCUT_TYPE_OPERATE_DYNAMIC )
			{
				if( shortcutInfo.getIntent().getBooleanExtra( OperateDynamicUtils.DYNAMIC_HOT , false ) )
				{
					shortcutInfo.getIntent().putExtra( OperateDynamicUtils.DYNAMIC_HOT , false );
					updateIconIntentDataBase( shortcutInfo , true );
					String id = shortcutInfo.getIntent().getStringExtra( OperateDynamicMain.OPEARTE_DYNAMIC_ID );
					OperateDynamicProxy.getInstance().hideOperateFolderHot( id , true );
					return true;
				}
			}
		}
		return false;
	}
}
