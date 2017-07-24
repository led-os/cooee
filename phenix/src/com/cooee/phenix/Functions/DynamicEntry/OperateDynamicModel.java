package com.cooee.phenix.Functions.DynamicEntry;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.cooee.framework.function.DynamicEntry.OperateDynamicClient;
import com.cooee.framework.function.DynamicEntry.OperateDynamicData;
import com.cooee.framework.function.DynamicEntry.OperateDynamicItem;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.function.DynamicEntry.DLManager.Constants;
import com.cooee.framework.function.DynamicEntry.DLManager.DlManager;
import com.cooee.framework.function.DynamicEntry.DLManager.DlNotifyManager;
import com.cooee.framework.function.DynamicEntry.DLManager.DownloadingItem;
import com.cooee.framework.function.DynamicEntry.Dialog.DynamicEntryDialogConstant;
import com.cooee.framework.function.DynamicEntry.Dialog.DynamicEntrySmartDownloadInfo;
import com.cooee.framework.utils.StringUtils;
import com.cooee.framework.utils.Utils;
import com.cooee.phenix.AllAppsList;
import com.cooee.phenix.BubbleTextView;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.Folder.Folder;
import com.cooee.phenix.Folder.FolderIcon;
import com.cooee.phenix.Functions.Category.OperateHelp;
import com.cooee.phenix.Functions.DynamicEntry.Dialog.DynamicEntryDialogView;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.ShortcutInfo;

import cool.sdk.DynamicEntry.DynamicEntryHelper;
import cool.sdk.DynamicEntry.DynamicEntryHelper.DynamicEntryListener;
import cool.sdk.SAManager.SACoolDLMgr.NotifyType;
import cool.sdk.download.manager.dl_info;


public class OperateDynamicModel implements DynamicEntryListener , OperateDynamicClient
{
	
	public interface IOperateCallbacks
	{
		
		public void bindItems(
				ArrayList<ItemInfo> shortcuts ,
				int start ,
				int end ,
				boolean forceAnimateIcons ,
				Runnable r ,//zhujieping modify
				boolean isLoadFinish );//cheyingkun add
		
		public void bindAddScreens(
				ArrayList<Long> orderedScreenIds );
		
		public void removeFolderIconByFolderInfo(
				ArrayList<View> allFolderIcons );
		
		public void removeEmptyScreen();
		
		public boolean isAllowToOperate();
		
		public void exitOverviewMode();
		
		public ArrayList<Long> getScreenOrder();
		
		public void bindOperateFoloderEnd();
		
		// zhujieping@2015/06/08 UPD START
		public void removewaitUntilResumeRunnable(
				List<Runnable> list );
		
		public void removeFolderInfo(
				ArrayList<ItemInfo> newInfos );
		
		// zhujieping@2015/06/08 UPD END
		public FolderInfo getSameOperateFolder(
				String dynamicID );
		
		public Folder getOpenFolderNotInPause();
		
		public ArrayList<View> getAllShortcutInworkspace();
	}
	
	public static final String DEFAULT_CONFIG_FILE = "operate_folder/default_config.ini";
	private static OperateDynamicModel mOperateDynamicModel = null;
	private OperateDynamicMain mOperateDynamicMain = null;
	private Context mContext = null;
	private IOperateCallbacks mCallbacks = null;
	/**
	 * 在切换到智能分类的时候，先释放了文件夹中图标的bitmap资源，但运营文件夹仍然会在绘制，因此会在文件夹绘制的时候重启，此时解决的方案是，暂时不释放资源，一直存放在HashMap中，只有等到从文件夹中删除应用图标的时候再释放资源
	 */
	private static HashMap<String , Bitmap> mAllIconBitmap = new HashMap<String , Bitmap>();//通过应用的包名来存储对应的bitmap
	private List<OperateDynamicData> mJsonData = null;
	private String mOperateVersion = null;
	private ArrayList<View> removeIcons = new ArrayList<View>();
	private static DynamicEntryDialogView dialogView = null;
	
	public OperateDynamicModel(
			IOperateCallbacks callbacks ,
			OperateDynamicMain operateDynamicMain ,
			Context context )
	{
		mOperateDynamicMain = operateDynamicMain;
		mContext = context;
		mCallbacks = callbacks;
		DynamicEntryHelper.getInstance( context ).setListener( this );
	}
	
	//加载默认配置的运营文件夹
	public static void loadDefaultConfig(
			SQLiteDatabase db )
	{
		try
		{
			String json = null;
			if( !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_OPERATE_PATH ) )
			{
				try
				{
					FileInputStream in = new FileInputStream( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_OPERATE_PATH , "/default_config.ini" ) );
					json = Utils.readTextFile( in );
				}
				catch( FileNotFoundException e )
				{
					e.printStackTrace();
				}
			}
			if( json == null )
				json = Utils.readTextFile( LauncherAppState.getInstance().getContext().getAssets().open( DEFAULT_CONFIG_FILE ) );
			ArrayList<ItemInfo> allFolderInfos = genItemInfo(
					LauncherAppState.getInstance().getContext() ,
					OperateDynamicProxy.parseDynamicData( json , true ) ,
					String.valueOf( System.currentTimeMillis() ) ,
					true );
			if( allFolderInfos == null )
				return;
			OperateFolderDatabase.addFolderInfosToDatabase( LauncherAppState.getInstance().getContext() , db , allFolderInfos );
			String IDStr = "";
			for( ItemInfo info : allFolderInfos )
			{
				OperateFolderDatabase.addItemToFavoriteDatabase( LauncherAppState.getInstance().getContext() , info );
				String dynamicId = "";
				if( info instanceof FolderInfo )
				{
					dynamicId = ( (FolderInfo)info ).getOperateIntent().getStringExtra( OperateDynamicMain.OPEARTE_DYNAMIC_ID );
				}
				else if( info instanceof ShortcutInfo )
				{
					dynamicId = info.getIntent().getStringExtra( OperateDynamicMain.OPEARTE_DYNAMIC_ID );
				}
				IDStr = StringUtils.concat(
						dynamicId ,
						OperateDynamicUtils.DYNAMIC_COMMA ,
						info.getTitle() ,
						OperateDynamicUtils.DYNAMIC_COMMA ,
						OperateDynamicUtils.DYNAMIC_DATA_INALL ,
						OperateDynamicUtils.DYNAMIC_SEMICOLON );
			}
			SharedPreferences pref = LauncherAppState.getInstance().getContext().getSharedPreferences( "DynamicEntry" , Context.MODE_PRIVATE );
			pref.edit().putString( "defaultIDAndName" , IDStr ).commit();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static ArrayList<ItemInfo> genItemInfo(
			Context mContext ,
			List<OperateDynamicData> mDynamicDatas ,
			String operateVersion ,
			boolean isDefault )//标记显性或隐性
	{
		if( mDynamicDatas == null )
		{
			return null;
		}
		ArrayList<String> listPkg = new ArrayList<String>();//防止加入重复的icon
		final ArrayList<ItemInfo> allInfos = new ArrayList<ItemInfo>();
		for( int i = 0 ; i < mDynamicDatas.size() ; i++ )
		{
			OperateDynamicData data = mDynamicDatas.get( i );
			if( data.dynamicType == OperateDynamicUtils.FOLDER )
			{
				//CharSequence folderTitle = data.getDynamicEntryTitle( true );
				String folderTitle = String.format( "%s+cooee+%s+cooee+%s" , data.mDeskNameCN , data.mDeskName , data.mDeskNameTW );//将中文、英文、繁体存入，在iteminfo中重新根据语言选择
				FolderInfo folderInfo = OperateDynamicMain.createFolderInfo(
						LauncherSettings.Favorites.CONTAINER_DESKTOP ,
						LauncherSettings.Favorites.ITEM_TYPE_FOLDER ,
						operateVersion ,
						folderTitle ,
						data.screen ,
						data.cellX ,
						data.cellY ,
						data.dynamicID );
				folderInfo.getOperateIntent().putExtra( OperateDynamicMain.DEFAULT_OPERATE_FOLDER , isDefault );
				folderInfo.getOperateIntent().putExtra( OperateDynamicMain.OPERATE_DATA_DEFAULT , data.isDefault );
				int count = 0;
				if( data.mDynamicItems != null && data.mDynamicItems.size() > 0 )
				{
					for( int n = 0 ; n < data.mDynamicItems.size() ; n++ )
					{
						OperateDynamicItem item = data.mDynamicItems.get( n );
						String pkgName = item.mPackageName;
						if( item.dynamicType == OperateDynamicUtils.VIRTUAL_APP && isDefault && LauncherAppState.isApkInstalled( pkgName ) )
						{
							List<ResolveInfo> info = AllAppsList.findActivitiesForPackage( mContext , pkgName , true );
							if( info != null && info.size() > 0 )
							{
								AppInfo appInfo = new AppInfo( mContext.getPackageManager() , info.get( 0 ) , LauncherAppState.getInstance().getIconCache() , null );
								folderInfo.add( appInfo.makeShortcut() );
							}
						}
						else
						{
							if( OperateHelp.getInstance( mContext ).containsOperateAppOrLink( pkgName ) )
							{
								continue;
							}
							int iconSize = Utilities.sIconWidth > 0 ? Utilities.sIconWidth : LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_icon_size );
							Bitmap bitmap = null;
							bitmap = mAllIconBitmap.get( pkgName );
							if( bitmap == null || bitmap.isRecycled() )
							{
								bitmap = Bitmap.createScaledBitmap( item.mIconBitmap , iconSize , iconSize , true );
								mAllIconBitmap.put( pkgName , bitmap );
							}
							item.mIconBitmap = bitmap;
							String title = String.format( "%s+cooee+%s+cooee+%s" , item.mCNTitle , item.mTitle , item.mTWTitle );//item.getDynamicItemTitle();
							Intent intent = null;
							if( item.dynamicType == OperateDynamicUtils.VIRTUAL_APP )
							{
								if( LauncherAppState.isApkInstalled( pkgName ) )
								{
									continue;
								}
								intent = new Intent( OperateDynamicMain.OPERATE_DYNAMIC_FOLDER );
								ComponentName cp = new ComponentName( mContext.getPackageName() , OperateDynamicMain.OperateActivityCls );
								intent.setComponent( cp );
								intent.putExtra( OperateDynamicUtils.DYNAMIC_APP_DOWNLOAD_TYPE , item.mAppDownloadType );
								intent.putExtra( OperateDynamicUtils.DYNAMIC_APP_SIZE , item.mAppSize );
								int state = DlManager.getInstance().getPkgNameCurrentState( pkgName );
								intent.putExtra( OperateDynamicMain.SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY , state );
							}
							else if( item.dynamicType == OperateDynamicUtils.VIRTUAL_LINK )//虚链接
							{
								Uri uri = Uri.parse( pkgName );
								intent = new Intent( Intent.ACTION_VIEW , uri );
								intent.putExtra( OperateDynamicMain.OPERATE_WEBLINKPKG , item.mWeblinkPkg );
							}
							if( intent != null )
							{
								intent.putExtra( OperateDynamicMain.PKGNAME_ID , pkgName );
								intent.putExtra( OperateDynamicMain.FOLDER_VERSION , operateVersion );
								intent.putExtra( OperateDynamicUtils.BITMAP_PATH_KEY , item.mBitmapPath );
								intent.putExtra( OperateDynamicMain.OPEARTE_DYNAMIC_ID , data.dynamicID );
								intent.putExtra( OperateDynamicUtils.DYNAMIC_TYPE_KEY , item.dynamicType );
								intent.putExtra( OperateDynamicMain.DEFAULT_OPERATE_FOLDER , isDefault );
								if( !listPkg.contains( pkgName ) )
								{
									listPkg.add( pkgName );
									ShortcutInfo info = OperateDynamicMain.createShortcutInfo( bitmap , title , intent , folderInfo.getContainer() );
									folderInfo.add( info );
									count++;
								}
							}
						}
					}
				}
				if( folderInfo.getContents().size() > 0 )
				{
					if( folderInfo.getContents().size() == 1 )
					{
						ShortcutInfo info = folderInfo.getContents().get( 0 );
						info.setContainer( LauncherSettings.Favorites.CONTAINER_DESKTOP );
						allInfos.add( info );
					}
					else
					{
						allInfos.add( folderInfo );
						Intent intent = folderInfo.getOperateIntent();
						intent.putExtra( OperateDynamicUtils.DYNAMIC_HOT , data.mDeskHot );
						intent.putExtra( Constants.NEW_ICON_COUNT , count );
					}
				}
			}
			else if( data.dynamicType == OperateDynamicUtils.VIRTUAL_APP || data.dynamicType == OperateDynamicUtils.VIRTUAL_LINK )
			{
				if( data.iconPath == null )
				{
					continue;
				}
				if( OperateHelp.getInstance( mContext ).containsOperateAppOrLink( data.mPkgnameOrAddr ) )
				{
					continue;
				}
				if( data.dynamicType == OperateDynamicUtils.VIRTUAL_APP )
				{
					if( LauncherAppState.isApkInstalled( data.mPkgnameOrAddr ) )//安装的虚应用不再显示
					{
						continue;
					}
				}
				Bitmap bmp = BitmapFactory.decodeFile( data.iconPath );
				if( bmp == null )
				{
					continue;
				}
				if( !listPkg.contains( data.mPkgnameOrAddr ) )
				{
					listPkg.add( data.mPkgnameOrAddr );
					ShortcutInfo sInfo = CreateEntrceItem( mContext , data , bmp , operateVersion );
					if( sInfo != null )
					{
						allInfos.add( sInfo );
					}
				}
			}
		}
		return allInfos;
	}
	
	/**
	 * 通过josn数据生成对应的folderInfo和shortcutInfo，并把对应的shortcutInfo加入到folderInfo中
	 * @param jsonData
	 */
	public boolean onCreateDynamicInfo(
			List<OperateDynamicData> list ,
			String operateVersion )
	{
		if( list == null )//mIsOperateFolderRunning已设为true后，jsonData为null抛出异常，未添加成功运营文件夹，mIsOperateFolderRunning没有设置回去，导致智能分类无法分类，这里为空时不处理直接return
		{
			return false;
		}
		if( OperateHelp.getInstance( mContext ).isGettingOperateDate() || mOperateDynamicMain.isOperateFolderRunning() )//若此时智能分类已经开始，则等待智能分类结束以后再加载文件夹 wanghongjian add
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "" , "whj OperateStart 智能分类正在运行，运营文件先停止  " );
			mJsonData = list;
			this.mOperateVersion = operateVersion;
			return false;
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "" , "whj OperateStart 开始运营文件更新或者加载  " );
		mOperateDynamicMain.setOperateFolderRunning( true );
		if( operateVersion == null )
		{
			operateVersion = String.valueOf( System.currentTimeMillis() );
		}
		ArrayList<String> dynamicIDs = new ArrayList<String>();
		for( OperateDynamicData item : list )
		{
			dynamicIDs.add( item.dynamicID );
		}
		try
		{
			String json = null;
			if( !TextUtils.isEmpty( LauncherDefaultConfig.CONFIG_CUSTOM_OPERATE_PATH ) )
			{
				try
				{
					FileInputStream in = new FileInputStream( StringUtils.concat( LauncherDefaultConfig.CONFIG_CUSTOM_OPERATE_PATH , "/default_config.ini" ) );
					json = Utils.readTextFile( in );
				}
				catch( FileNotFoundException e )
				{
					e.printStackTrace();
				}
			}
			if( json == null )
				json = Utils.readTextFile( LauncherAppState.getInstance().getContext().getAssets().open( DEFAULT_CONFIG_FILE ) );
			List<OperateDynamicData> local = OperateDynamicProxy.parseDynamicData( json , true );
			if( local != null && local.size() != 0 )
			{
				for( OperateDynamicData item : local )//若隐形中没有显性文件夹id，则将显性加入，恢复到显性默认的样子
				{
					if( dynamicIDs.indexOf( item.dynamicID ) == -1 )
					{
						item.isDefault = true;
						list.add( 0 , item );
					}
				}
			}
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<ItemInfo> allFolderInfos = genItemInfo( mContext , list , operateVersion , false );//隐性文件夹
		mJsonData = null;
		//里面操作数据库的不需要放在主线程
		// zhujieping@2015/06/03 UPD START,更新解决方法
		//【原因】：launcher一直处在onpause的状态，运营文件夹第一个八个小时后数据更新文件夹，生成文件夹时
		//				会将icon存在list中，但文件夹在onPause的时候是不会生成的，list也为空，第二次八小时后请求数据，
		//				会根据list来删除先前的文件夹，但list为空，未能删除之前的运营图标，导致重复显示。
		//【复现步骤】：连续调两次时间，不回到launcher
		//【解决方法】：因onpaunse状态时，添加运营文件夹icon是放在launcher的List<Runnable>中等待onresume之后执行的，
		//增加list用来存储生成文件夹的runnable，每次来新数据后，先去清除上次未完成的runnable的list。
		if( allFolderInfos != null )
			mOperateDynamicMain.clearAndAddFolderByFolderList( mContext , allFolderInfos );
		// zhujieping@2015/06/03 UPD END
		return true;
	}
	
	/**
	 * 若mJsonData保存了数据，则表示要重新加载一下文件夹数据
	 */
	public void reStartLoadData()
	{
		if( mJsonData != null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "" , "whj OperateStart 智能分类结束 重新加载文件夹数据" );
			onCreateDynamicInfo( mJsonData , mOperateVersion );
			//			mJsonData = null;//不置空，因为onCreateDynamicInfo方法依然没有执行完成，在onCreateDynamicInfo执行后有置空
		}
	}
	
	public void bindItems(
			final ArrayList<ItemInfo> allFolderInfos )
	{
		if( mCallbacks != null )
		{
			final boolean isLoaderTaskRunning = LauncherAppState.getInstance().getModel().isLoaderTaskRunning();//cheyingkun add	//加载桌面过程中,加载手机应用是否切页到添加应用的那一页(逻辑优化)
			// zhujieping@2015/06/03 ADD START，将生成运营文件夹的icon存在list中
			Runnable r = new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					mCallbacks.bindItems( allFolderInfos , 0 , allFolderInfos.size() , false , null , !isLoaderTaskRunning );
				}
			};
			OperateDynamicMain.addRunnableToList( r );
			// zhujieping@2015/06/03 ADD END
			mCallbacks.bindItems( allFolderInfos , 0 , allFolderInfos.size() , false , r , !isLoaderTaskRunning );
		}
	}
	
	public void bindAddScreens(
			final ArrayList<Long> addedWorkspaceScreensFinal )
	{
		if( mCallbacks != null )
		{
			mCallbacks.bindAddScreens( addedWorkspaceScreensFinal );
		}
	}
	
	public void removeFolderInWorkspace(
			ArrayList<View> allIcons ,
			ArrayList<ItemInfo> newInfos ,
			boolean laterDelete )
	{
		removeIcons.clear();//removeIcons用来存放文件夹中要删除的icon，防止先删除导致文件夹解散
		ArrayList<View> remove = new ArrayList<View>();//在桌面的图标立即删除，否则在加icon到桌面时，提示位置被占
		Iterator<View> iter = allIcons.iterator();
		while( iter.hasNext() )
		{
			View view = iter.next();
			if( view instanceof FolderIcon )
			{
				FolderIcon icon = (FolderIcon)view;
				FolderInfo folderInfo = icon.getFolderInfo();
				if( OperateDynamicMain.shouldOperateFolderDelete( folderInfo ) )
				{
					//					LauncherModel.deleteItemFromDatabase( mContext , folderInfo );//前一个方法中已经删除
					remove.add( view );
					iter.remove();
				}
			}
			else if( view instanceof BubbleTextView )
			{
				Object obj = view.getTag();
				if( obj != null && obj instanceof ShortcutInfo )
				{
					ShortcutInfo si = (ShortcutInfo)obj;
					if( si.isOperateIconItem() && OperateDynamicMain.shouldOperateShortcutDelete( si , newInfos ) )
					{
						//						LauncherModel.deleteItemFromDatabase( mContext , si );
						if( laterDelete && si.getContainer() != LauncherSettings.Favorites.CONTAINER_DESKTOP )//laterDelete为true时，是稍后删除，为false是立即删除。稍后删除的原因是：先删除在添加icon时，会导致文件夹被解散，所以先添加后删除。但桌面上的图标要立即删除，防止出现位置被占
							removeIcons.add( view );
						else
							remove.add( view );
						iter.remove();
						OperateDynamicMain.getmAllOperateIcon().remove( si.getIntent().getStringExtra( OperateDynamicMain.PKGNAME_ID ) );
					}
				}
			}
		}
		if( mCallbacks != null )
		{
			mCallbacks.removeFolderIconByFolderInfo( remove );
		}
	}
	
	/**
	 * 删除桌面空白页
	 */
	public void removeEmptyScreen()
	{
		if( mCallbacks != null )
		{
			mCallbacks.removeEmptyScreen();
		}
	}
	
	public boolean isAllowToOperate()
	{
		if( mCallbacks != null )
		{
			return mCallbacks.isAllowToOperate();
		}
		return false;
	}
	
	public HashMap<String , Bitmap> getmAllIconBitmap()
	{
		return mAllIconBitmap;
	}
	
	public void exitOverviewMode()
	{
		if( mCallbacks != null )
		{
			mCallbacks.exitOverviewMode();
		}
	}
	
	public ArrayList<Long> getScreenOrder()
	{
		if( mCallbacks != null )
		{
			return mCallbacks.getScreenOrder();
		}
		return null;
	}
	
	/**
	 * 表示运营文件夹加载结束
	 */
	public void bindOperateFoloderEnd()
	{
		if( mCallbacks != null )
		{
			mCallbacks.removeFolderIconByFolderInfo( removeIcons );//加载结束后删除之前未删除的
			mCallbacks.bindOperateFoloderEnd();
			reStartLoadData();//智能分类中，更新数据的情况
		}
	}
	
	/**
	 * zhujieping add，移除launcher中等待onresume后要执行的生成运营文件夹icon的runnalbe
	 */
	public void removewaitUntilResumeRunnable(
			List<Runnable> r )
	{
		if( mCallbacks != null )
		{
			mCallbacks.removewaitUntilResumeRunnable( r );
		}
	}
	
	public void removeFolderInfo(
			ArrayList<ItemInfo> newInfos )
	{
		if( mCallbacks != null )
		{
			mCallbacks.removeFolderInfo( newInfos );
		}
	}
	
	public FolderInfo getSameOperateFolder(
			//获取桌面同样dynamicid的文件夹
			String dynamicid )
	{
		if( mCallbacks != null )
		{
			return mCallbacks.getSameOperateFolder( dynamicid );
		}
		return null;
	}
	
	@Override
	public void onDataChange(
			String json )
	{
		if( OperateDynamicProxy.context != null )
		{
			List<OperateDynamicData> datas = OperateDynamicProxy.getInstance().parseDynamicData( json , false );
			onCreateDynamicInfo( datas , OperateDynamicProxy.getInstance().getListVersion() );
		}
	}
	
	@Override
	public boolean onDynamicDataChange()
	{
		// TODO Auto-generated method stub
		return !mOperateDynamicMain.isOperateFolderRunning();
	}
	
	@Override
	public void cancelDynamicUpdateWaiteDialog(
			boolean success )
	{
		// TODO Auto-generated method stub
	}
	
	private static ShortcutInfo CreateEntrceItem(
			Context mContext ,
			OperateDynamicData item ,
			Bitmap bmp ,
			String operateVersion )
	{
		int iconSize = Utilities.sIconWidth > 0 ? Utilities.sIconWidth : LauncherDefaultConfig.getDimensionPixelSize( R.dimen.app_icon_size );
		String pkgName = item.mPkgnameOrAddr;
		Bitmap bitmap = null;
		bitmap = mAllIconBitmap.get( pkgName );
		if( bitmap == null || bitmap.isRecycled() )
		{
			bitmap = Bitmap.createScaledBitmap( bmp , iconSize , iconSize , true );
			mAllIconBitmap.put( pkgName , bitmap );
			if( bmp != null && !bmp.isRecycled() )
			{
				bmp.recycle();
				bmp = null;
			}
		}
		String title = String.format( "%s+cooee+%s+cooee+%s" , item.mDeskNameCN , item.mDeskName , item.mDeskNameTW );//item.getDynamicEntryTitle( true );
		Intent intent = null;
		if( item.dynamicType == OperateDynamicUtils.VIRTUAL_APP )
		{
			intent = new Intent( OperateDynamicMain.OPERATE_DYNAMIC_FOLDER );
			ComponentName cp = new ComponentName( mContext.getPackageName() , OperateDynamicMain.OperateActivityCls );
			intent.setComponent( cp );
			intent.putExtra( OperateDynamicUtils.DYNAMIC_APP_DOWNLOAD_TYPE , item.mAppDownloadType );
			intent.putExtra( OperateDynamicUtils.DYNAMIC_APP_SIZE , item.mAppSize );
			int state = DlManager.getInstance().getPkgNameCurrentState( pkgName );
			intent.putExtra( OperateDynamicMain.SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY , state );//判读是否是静默下载成功的
		}
		else if( item.dynamicType == OperateDynamicUtils.VIRTUAL_LINK )
		{
			Uri uri = Uri.parse( pkgName );
			intent = new Intent( Intent.ACTION_VIEW , uri );
			intent.putExtra( OperateDynamicMain.OPERATE_WEBLINKPKG , item.mWeblinkPkg );
		}
		if( intent != null )
		{
			intent.putExtra( OperateDynamicMain.PKGNAME_ID , pkgName );
			intent.putExtra( OperateDynamicMain.FOLDER_VERSION , operateVersion );
			intent.putExtra( OperateDynamicUtils.BITMAP_PATH_KEY , item.iconPath );
			intent.putExtra( OperateDynamicMain.OPEARTE_DYNAMIC_ID , item.dynamicID );//将运营id保存起来
			intent.putExtra( OperateDynamicUtils.DYNAMIC_TYPE_KEY , item.dynamicType );
			intent.putExtra( OperateDynamicUtils.DYNAMIC_HOT , item.mDeskHot );
			ShortcutInfo info = OperateDynamicMain.createShortcutInfo( bitmap , title , intent , LauncherSettings.Favorites.CONTAINER_DESKTOP );
			return info;
		}
		return null;
	}
	
	@Override
	public void showWifiDownloadNotify(
			//后续wifi下载要用
			NotifyType type ,
			List<dl_info> dl_info_list )
	{
		if( dl_info_list == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "SA" , "list is null" );
			return;
		}
		DlManager.getInstance().getWifiSAHandle().showWifiSANotify( type , dl_info_list );
		for( dl_info info : dl_info_list )
		{
			String packageName = (String)info.getValue( "p2" );
			if( mCallbacks != null )
				mOperateDynamicMain.refreshOperateFolderIconState( mCallbacks.getAllShortcutInworkspace() , packageName , Constants.DL_STATUS_SUCCESS );
		}
	}
	
	@Override
	public void upateDownloadItemState(
			String pkgName ,
			int state )//删除下载过的内容时，更新图标
	{
		// TODO Auto-generated method stub
		if( mCallbacks != null )
		{
			mOperateDynamicMain.refreshOperateFolderIconState( mCallbacks.getAllShortcutInworkspace() , pkgName , state );
		}
	}
	
	private static void getDLSuccessItem(
			int mCountX ,
			JSONArray jsonArray ,
			DlNotifyManager mDlNotifyManager ,
			FolderInfo userFolderInfo ,
			boolean isSA )
	{
		for( int i = 0 ; i < userFolderInfo.getContents().size() ; i++ )
		{
			ShortcutInfo sInfo = userFolderInfo.getContents().get( i );
			if( sInfo.isOperateIconItem() || sInfo.isOperateVirtualItem() )
			{
				Intent intent = sInfo.getIntent();
				String pkgname = intent.getStringExtra( OperateDynamicMain.PKGNAME_ID );
				if( pkgname == null )
				{
					continue;
				}
				DownloadingItem downloadItem = mDlNotifyManager.getDownloadingItem( pkgname );
				if( downloadItem != null && downloadItem.state == Constants.DL_STATUS_SUCCESS )
				{
					dl_info d_info = DlManager.getInstance().getWifiSAHandle().getDlInfo( pkgname );
					if( ( !isSA && d_info == null || isSA && d_info != null ) )
					{
						DynamicEntrySmartDownloadInfo smartDownInfo = new DynamicEntrySmartDownloadInfo( pkgname , sInfo.getTitle() , intent.getStringExtra( OperateDynamicUtils.BITMAP_PATH_KEY ) );
						jsonArray.put( smartDownInfo.toJSON() );
						if( jsonArray.length() >= mCountX )
						{
							break;
						}
					}
				}
			}
		}
	}
	
	public static boolean twoTimeIsSameWeek(
			long lastTime ,
			long curTime )
	{
		if( Math.abs( lastTime - curTime ) >= 1000 * 60 * 60 * 24 * 7 )
		{
			return false;
		}
		else if( Math.abs( lastTime - curTime ) >= 1000 * 60 * 60 * 24 )
		{
			int lastWeek = new Date( lastTime ).getDay();
			int curWeek = new Date( curTime ).getDay();
			if( curTime > lastTime )
			{
				if( curWeek <= lastWeek )
				{
					return false;
				}
			}
			else
			{
				if( curWeek >= lastWeek )
				{
					return false;
				}
			}
		}
		return true;
	}
	
	//进入文件夹时弹出
	public static void popSmartDownloadDialog(
			final int mCountX ,
			final FolderInfo userFolderInfo ,
			final Folder folder )
	{
		AsyncTask.execute( new Runnable() {
			
			public void run()
			{
				SharedPreferences preferences = LauncherAppState.getInstance().getContext().getSharedPreferences( "DynamicEntry" , Context.MODE_PRIVATE );
				boolean sdOpen = false;
				boolean normalOpen = false;
				String dynamicVersion = null;
				long lastTime = preferences.getLong( DynamicEntryDialogConstant.LAST_CLICK_TIME , -1 );
				long curTime = System.currentTimeMillis();
				boolean sameWeek = twoTimeIsSameWeek( lastTime , curTime );
				JSONArray jsonArray = new JSONArray();
				DlNotifyManager mDlNotifyManager = DlManager.getInstance().getDlNotifyManager();
				//先处理用户点击下载的
				if( !sameWeek )
				{
					getDLSuccessItem( mCountX , jsonArray , mDlNotifyManager , userFolderInfo , false );
					if( jsonArray.length() > 0 )
					{
						normalOpen = true;
					}
				}
				if( !normalOpen/* && !userFolderInfo.isOperateVirtualItem()*/)
				{
					dynamicVersion = DynamicEntryHelper.getInstance( LauncherAppState.getInstance().getContext() ).getListVersion();
					String curVersion = DlManager.getInstance().getSharedPreferenceHandle().getValue( DynamicEntryDialogConstant.DYNAMIC_VERSION );
					if( dynamicVersion != null && !dynamicVersion.equals( curVersion ) )
					{
						getDLSuccessItem( mCountX , jsonArray , mDlNotifyManager , userFolderInfo , true );
						if( jsonArray.length() > 0 )
						{
							sdOpen = true;
						}
					}
				}
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( "smart" , StringUtils.concat( "title:" , userFolderInfo.getTitle() , "-normalOpen:" , normalOpen , "-sdOpen:" , sdOpen ) );
				if( normalOpen == false && sdOpen == false )
				{
					return;
				}
				final JSONObject res = new JSONObject();
				if( userFolderInfo.getOpened() )
				{
					try
					{
						res.put( "SmartDownloadInfo" , jsonArray );
						if( normalOpen )
						{
							res.put( "normal" , true );
						}
						//						DlManager.getInstance().getDialogHandle().startSmartDownloadDialog( DynamicEntryDialogConstant.DIALOG_SMARTDOWNLOAD , res.toString() );
						LauncherAppState.getActivityInstance().runOnUiThread( new Runnable() {
							
							@Override
							public void run()
							{
								// TODO Auto-generated method stub
								if( dialogView == null )
									dialogView = new DynamicEntryDialogView();
								folder.showDynamicDialog( dialogView.getDynamicEntryView(
										LauncherAppState.getInstance().getContext() ,
										res.toString() ,
										DynamicEntryDialogConstant.DIALOG_SMARTDOWNLOAD ) );
							}
						} );
						if( normalOpen )
						{
							preferences.edit().putLong( DynamicEntryDialogConstant.LAST_CLICK_TIME , curTime ).commit();
						}
						else
						{
							DlManager.getInstance().getSharedPreferenceHandle().saveValue( DynamicEntryDialogConstant.DYNAMIC_VERSION , dynamicVersion );
						}
					}
					catch( JSONException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} );
	}
	
	public DynamicEntrySmartDownloadInfo getSaleSmartDownloadInfo(
			DownloadingItem dlItem ,
			Folder folder )
	{
		// TODO Auto-generated method stub
		if( folder != null )
		{
			ShortcutInfo minSizeInfo = null;
			boolean find = false;
			for( ShortcutInfo info : folder.getInfo().getContents() )
			{
				if( info.isOperateIconItem() || info.isOperateVirtualItem() )
				{
					String pkgName = info.getIntent().getStringExtra( OperateDynamicMain.PKGNAME_ID );
					if( pkgName == null )
					{
						continue;
					}
					int state = DlManager.getInstance().getDownloadHandle().getPkgNameCurrentState( pkgName );
					switch( state )
					{
						case Constants.DL_STATUS_ING:
							if( pkgName.equals( dlItem.packageName ) )
							{
								find = true;
							}
							break;
						case Constants.DL_STATUS_NOTDOWN:
							int size = info.getIntent().getIntExtra( OperateDynamicUtils.DYNAMIC_APP_SIZE , 0 );
							if( minSizeInfo == null )
							{
								if( size > 0 )
								{
									minSizeInfo = info;
								}
							}
							else
							{
								int minSize = minSizeInfo.getIntent().getIntExtra( OperateDynamicUtils.DYNAMIC_APP_SIZE , 0 );
								if( size > 0 && size < minSize )
								{
									minSizeInfo = info;
								}
							}
							break;
					}
				}
			}
			if( folder.getInfo().getOpened() && find && minSizeInfo != null )
			{
				DynamicEntrySmartDownloadInfo smartDownInfo = new DynamicEntrySmartDownloadInfo(
						minSizeInfo.getIntent().getStringExtra( OperateDynamicMain.PKGNAME_ID ) ,
						minSizeInfo.getTitle() ,
						minSizeInfo.getIntent().getStringExtra( OperateDynamicUtils.BITMAP_PATH_KEY ) ,
						minSizeInfo.getIntent().getIntExtra( OperateDynamicUtils.DYNAMIC_APP_SIZE , 0 ) ,
						minSizeInfo.getIntent().getIntExtra( OperateDynamicUtils.DYNAMIC_APP_DOWNLOAD_TYPE , OperateDynamicUtils.NORMAL_DOWNLOAD ) );
				return smartDownInfo;
			}
		}
		return null;
	}
	
	@Override
	public boolean showSaleSmartDownloadDialog(
			DownloadingItem dlItem )
	{
		// TODO Auto-generated method stub
		if( mCallbacks != null )
		{
			Folder folder = mCallbacks.getOpenFolderNotInPause();
			DynamicEntrySmartDownloadInfo smartDownInfo = getSaleSmartDownloadInfo( dlItem , folder );
			if( smartDownInfo != null )
			{
				try
				{
					JSONObject json = smartDownInfo.toJSON();
					json.put( DynamicEntryDialogConstant.LAST_NAME , dlItem.title );
					if( dialogView == null )
						dialogView = new DynamicEntryDialogView();
					folder.showDynamicDialog( dialogView.getDynamicEntryView( LauncherAppState.getInstance().getContext() , json.toString() , DynamicEntryDialogConstant.DIALOG_DOWNLOADONE ) );
					return true;
				}
				catch( JSONException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
