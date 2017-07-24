package com.cooee.framework.function.DynamicEntry.DLManager;


import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.StatFs;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicClient;
import com.cooee.framework.function.DynamicEntry.OperateDynamicProxy;
import com.cooee.framework.utils.StringUtils;
import com.cooee.launcher.framework.R;

import cool.sdk.DynamicEntry.DynamicEntry;
import cool.sdk.DynamicEntry.DynamicEntryHelper;
import cool.sdk.SAManager.SAHelper;
import cool.sdk.common.MyMethod;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


public class DlApkMangerActivity extends Activity
{
	
	CoolDLMgr dlMgr = null;
	CoolDLMgr iconDlMgr = null;
	int entryId;
	private ViewPager gridPager;
	View viewDownload;
	View viewInstall;
	Boolean isShowDownloadView = true;
	Boolean isShowInstallView = false;
	DownloadListViewAdapter downloadAdapter = null;
	InstallListViewAdapter installAdapter = null;
	Button butCannelback = null;
	TextView tvTitleInfo = null;
	View flDownloadNUllDate = null;
	FrameLayout flDownlaodListDate = null;
	View flInstallNUllDate = null;
	FrameLayout flInstallListDate = null;
	ListView lvInstallListView = null;
	ListView lvDownloadListView = null;
	static List<DLApkDownloadApkItem> DownloadApkList = new ArrayList<DLApkDownloadApkItem>();
	List<DlApkInstallApkItemEx> InstallApkList = new ArrayList<DlApkInstallApkItemEx>();
	private static final int PAUSEING = 0;//暂停下载
	private static final int WAITTING = 1;//等待下载
	private static final int DOWNING = 2;//下载中
	private static final int STATRTING = 2;//启动
	private static final int INSTALLING = 1;//安装
	long longTime = 0L;
	int progress = 0;
	private static final String TAG = "APK_HL";
	private static boolean DOWNSTATE_STATE_TAG = true;
	public static int DOWNLOAD_MAX_NUM = 3;
	public static String ICON_DEFAULT_FOLDER = "operate_folder/icon/";
	Handler DownloadUpdateHander = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			// TODO Auto-generated method stub
			try
			{
				switch( msg.what )
				{
					case Constants.DL_STATUS_ING:// 下载进度更新
					{
						DownLoadCallBackMsgData MsgData = (DownLoadCallBackMsgData)msg.obj;
						dl_info dlInfo = MsgData.getDlInfo();
						DLApkDownloadApkItem CurItem = MsgData.getCurItem();
						if( dlInfo != null )
						{
							CurItem.setCurSize( dlInfo.getCurBytes() );
							CurItem.setTotalSize( dlInfo.getTotalBytes() );
							progress = (int)( (float)dlInfo.getCurBytes() / dlInfo.getTotalBytes() * 100 );
							if( CurItem.getNotifyDoingToLauncher() )
							{
								sendDlMgrBroad( CurItem.getPackageName() , CurItem.getAppName() , Constants.DL_MGR_ACTION_DOWNING , progress );
							}
						}
						if( null == CurItem.getIconImgSrc() && null != iconDlMgr.IconGetInfo( CurItem.getPackageName() ) )
						{
							String ImgPath = iconDlMgr.IconGetInfo( CurItem.getPackageName() ).getFilePath();
							if( null != ImgPath && ImgPath.length() > 1 )
							{
								CurItem.setIconImgSrc( Drawable.createFromPath( ImgPath ) );
							}
						}
						// int DownLoadState; //0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
						if( DOWNING != CurItem.getDownLoadState() )
						{
							CurItem.setDownLoadState( DOWNING );
						}
						if( System.currentTimeMillis() - longTime >= 1000 )
						{
							downloadAdapter.Update( DownloadApkList );
							longTime = System.currentTimeMillis();
						}
						MsgData = null;
					}
						break;
					case Constants.DL_STATUS_SUCCESS:// 成功
					{
						DLApkDownloadApkItem CurItem = (DLApkDownloadApkItem)msg.obj;
						if( null != CurItem )
						{
							DownloadApkList.remove( CurItem );
							downloadAdapter.Update( DownloadApkList );
							UpdateDownloadFrame();
							InitInstallList();
							sendDlMgrBroad( CurItem.getPackageName() , CurItem.getAppName() , Constants.DL_MGR_ACTION_SUCCESS );
						}
					}
						break;
					case Constants.DL_STATUS_FAIL:// 失败
					{
						DLApkDownloadApkItem CurItem = (DLApkDownloadApkItem)msg.obj;
						// int DownLoadState; //0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
						sendDlMgrBroad( CurItem.getPackageName() , CurItem.getAppName() , Constants.DL_MGR_ACTION_FAILURE );
						if( PAUSEING != CurItem.getDownLoadState() )
						{
							CurItem.setDownLoadState( PAUSEING );
						}
						//DownloadApkList.clear();
						//InitDownloadList();
						downloadAdapter.Update( DownloadApkList );
					}
						break;
					case Constants.DL_STATUS_PAUSE:// 开始或暂停下载
					{
						DLApkDownloadApkItem CurDownloadItem = (DLApkDownloadApkItem)msg.obj;
						CurDownloadItem.setNotifyDoingToLauncher( true );
						// int DownLoadState; //0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
						OperateDynamicClient client = OperateDynamicProxy.getInstance().getOperateDynamicClient();
						if( PAUSEING == CurDownloadItem.getDownLoadState() )
						{
							CurDownloadItem.setDownLoadState( DlApkMangerActivity.WAITTING );
							progress = (int)( (float)CurDownloadItem.getCurSize() / CurDownloadItem.getTotalSize() * 100 );
							sendDlMgrBroad( CurDownloadItem.getPackageName() , CurDownloadItem.getAppName() , Constants.DL_MGR_ACTION_DOWNING , progress );
							client.upateDownloadItemState( CurDownloadItem.getPackageName() , Constants.DL_STATUS_ING );//发出通知，更新角标
						}
						else
						{
							CurDownloadItem.setDownLoadState( PAUSEING );
							sendDlMgrBroad( CurDownloadItem.getPackageName() , CurDownloadItem.getAppName() , Constants.DL_MGR_ACTION_PAUSE );
							client.upateDownloadItemState( CurDownloadItem.getPackageName() , Constants.DL_STATUS_PAUSE );
						}
						downloadAdapter.Update( DownloadApkList );
					}
						break;
					case Constants.DL_STATUS_REMOVED:// 删除下载项
					{
						DLApkDownloadApkItem CurDownloadItem = (DLApkDownloadApkItem)msg.obj;
						DownloadApkList.remove( CurDownloadItem );
						downloadAdapter.Update( DownloadApkList );
						UpdateDownloadFrame();
						sendDlMgrBroad( CurDownloadItem.getPackageName() , CurDownloadItem.getAppName() , Constants.DL_MGR_ACTION_REMOVED );
					}
						break;
					case Constants.DL_STATUS_PATCH:
					{
						int InfoID = R.string.cool_ml_download_failed;
						// TODO: handle exception
						if( false == IsNetworkAvailable( getApplicationContext() ) )
						{
							InfoID = R.string.cool_ml_network_not_available;
						}
						else if( false == IsStorageCanUsed() )
						{
							InfoID = R.string.cool_ml_storage_not_available;
						}
						else
						{
							InfoID = R.string.cool_ml_download_failed;
						}
						Toast.makeText( getApplicationContext() , InfoID , Toast.LENGTH_SHORT ).show();
					}
						break;
					default:
						break;
				}
			}
			catch( Exception e )
			{
				// TODO: handle exception
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "handleMessage Exception:" , e.toString() ) );
			}
		}
	};
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "ApkMangerActivity onCreate" );
		super.onCreate( savedInstanceState );
		setContentView( R.layout.cool_ml_apk_manager );
		Intent intent = getIntent();
		try
		{
			String moudleName = intent.getStringExtra( "moudleName" );
			//			ArrayList<ShortcutInfo> temp;
			//			temp = (ArrayList<ShortcutInfo>) intent.getSerializableExtra("shourtcutInfo");
			//			Log.e(TAG, temp+"");
			//			entryId = intent.getIntExtra( "entryId" , -1 );
			//			if( entryId == -1 )
			//			{
			//				finish();
			//				return;
			//			}
			if( dlMgr == null || iconDlMgr == null )
			{
				dlMgr = DynamicEntry.CoolDLMgr( this , moudleName );
				dlMgr.dl_mgr.setMaxConnectionCount( DOWNLOAD_MAX_NUM );
				iconDlMgr = DynamicEntryHelper.getInstance( this ).getCoolDLMgrIcon();
			}
			//			
		}
		catch( Exception e )
		{
			finish();
			return;
		}
		//		InitDownloadList();
		initView();
		butCannelback = (Button)findViewById( R.id.cool_ml_back_text );
		butCannelback.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View arg0 )
			{
				// TODO Auto-generated method stub
				finish();
			}
		} );
		tvTitleInfo = (TextView)findViewById( R.id.cool_ml_title_text );
		tvTitleInfo.setText( R.string.cool_ml_donwloadorinstall_manager );
		flDownloadNUllDate = viewDownload.findViewById( R.id.cool_ml_flDownloadNULLData );
		flDownlaodListDate = (FrameLayout)viewDownload.findViewById( R.id.cool_ml_flDownloadList );
		lvDownloadListView = (ListView)viewDownload.findViewById( R.id.cool_ml_lvDownload );
		flInstallNUllDate = viewInstall.findViewById( R.id.cool_ml_flInsatllNULLData );
		flInstallListDate = (FrameLayout)viewInstall.findViewById( R.id.cool_ml_flInsatllList );
		lvInstallListView = (ListView)viewInstall.findViewById( R.id.cool_ml_lvInstall );
		String msg = intent.getStringExtra( "msg" );
		if( msg != null && msg.equals( "msgWifiSA" ) )
		{
			DlManager.getInstance().getDialogHandle().popDLManagerDialog();
		}
	}
	
	private void InitInstallList()
	{
		// TODO Auto-generated method stub
		try
		{
			List<dl_info> ApkTaskList = null;
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "InitInstallList" );
			try
			{
				ApkTaskList = dlMgr.ResGetTaskList( CoolDLResType.RES_TYPE_APK );
			}
			catch( Exception e )
			{
				ApkTaskList = new ArrayList<dl_info>();
			}
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "ApkTaskList.size():" , ApkTaskList.size() ) );
			//wifi1118 start 只加NOTIFY提示过的应用.
			List<dl_info> dl_info_list = SAHelper.getInstance( BaseAppState.getActivityInstance() ).getSuccessButNotInstallList();
			if( dl_info_list != null )
			{
				for( int i = 0 ; i < dl_info_list.size() ; i++ )
				{
					dl_info info = dl_info_list.get( i );
					String pkgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
					String value = DlManager.getInstance().getSharedPreferenceHandle().getValue( StringUtils.concat( SharedPreferenceHandle.SILENTDOWNLOAD_PREFIX , pkgName ) );
					if( value != null && value.equals( String.valueOf( SharedPreferenceHandle.SIENT_SHOW ) ) )
					{
						ApkTaskList.add( info );
					}
				}
			}
			//wifi1118 end
			InstallApkList.clear();
			for( dl_info info : ApkTaskList )
			{
				if( info.IsDownloadSuccess() )
				{
					String pkgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
					if( null == pkgName )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( TAG , " null == (String)info.getValue( 'r4')" );
						continue;
					}
					Drawable installDrawable = DlManager.getInstance().getDownloadHandle().getDownSuccessIcon( pkgName );
					if( installDrawable == null )
					{
						continue;
					}
					DlApkInstallApkItemEx InstallApkItem = new DlApkInstallApkItemEx();
					InstallApkItem.setPackageName( pkgName );
					//wifi1118 
					String appName = (String)info.getValue( "p101" );
					if( appName == null || appName.equals( "" ) )
					{
						appName = DlManager.getInstance().getWifiSAHandle().getTitleName( info );
						//安装后卸载了。就不再显示出来了。
						if( appName == null )
						{
							continue;
						}
					}
					//wifi1118 end
					InstallApkItem.setAppName( (String)appName );
					InstallApkItem.setAppSize( info.getCurBytes() );
					InstallApkItem.setAppVersion( (String)info.getValue( "versionName" ) );
					InstallApkItem.setIconImgSrc( installDrawable );
					if( CampareToPageNameInstall( InstallApkItem.getPackageName() ) )
					{
						InstallApkItem = null;
						continue;
					}
					// int InstallState; /* 0:初始化状态 1:安装 2:启动 */
					String versionName = getVersion( pkgName , DlApkMangerActivity.this );
					if( InstallApkItem.getAppVersion() != null && versionName != null && InstallApkItem.getAppVersion().equals( versionName ) )
					{
						InstallApkItem.setInstallState( STATRTING );
					}
					else
					{
						InstallApkItem.setInstallState( INSTALLING );
					}
					InstallApkList.add( InstallApkItem );
				}
			}
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "InstallApkList.size():" , InstallApkList.size() ) );
		}
		catch( Exception e )
		{
			// TODO: handle exception
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "InitInstallList.Exception:" , e.toString() ) );
		}
	}
	
	private boolean CampareToPageNameInstall(
			String PackageName )
	{
		for( int i = 0 ; i <= InstallApkList.size() - 1 ; i++ )
		{
			if( PackageName.equals( InstallApkList.get( i ).PackageName.toString() ) )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		return false;
	}
	
	private Drawable getDownloadIcon(
			String pkgName )
	{
		Drawable retDrawable = null;
		dl_info info = iconDlMgr.IconGetInfo( pkgName );
		if( null != info )
		{
			String imagePath = info.getFilePath();
			if( imagePath != null && imagePath.length() > 1 )
			{
				retDrawable = Drawable.createFromPath( imagePath );
			}
			else
			{
				retDrawable = getDefaultConfigIcon( pkgName );
			}
		}
		else
		{
			retDrawable = getDefaultConfigIcon( pkgName );
		}
		return retDrawable;
	}
	
	private Drawable getDefaultConfigIcon(
			String pkgName )
	{
		String defaultFileName = StringUtils.concat( ICON_DEFAULT_FOLDER , pkgName , ".png" );
		try
		{
			return new BitmapDrawable( this.getResources() , BitmapFactory.decodeStream( getAssets().open( defaultFileName ) ) );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			return BaseAppState.getActivityInstance().getResources().getDrawable( R.drawable.cool_ml_download_install );
		}
	}
	
	private void InitDownloadList()
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "InitDownloadList" );
		try
		{
			List<dl_info> ApkTaskList = null;
			try
			{
				ApkTaskList = dlMgr.ResGetTaskList( CoolDLResType.RES_TYPE_APK );
			}
			catch( Exception e )
			{
				ApkTaskList = new ArrayList<dl_info>();
			}
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "ApkTaskList.size():" , ApkTaskList.size() ) );
			DownloadApkList.clear();
			for( dl_info info : ApkTaskList )
			{
				// DownloadState 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
				if( !info.IsDownloadSuccess() || PAUSEING != info.getDownloadState() )
				{
					if( null == (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY ) )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( TAG , " null == (String)info.getValue( 'r4' ) && null == (String)info.getValue( 'p2' )" );
						continue;
					}
					String pkgName = (String)info.getValue( Constants.DL_INFO_GET_PKGNAME_KEY );
					Drawable apkDrawable = getDownloadIcon( pkgName );
					if( apkDrawable == null )
					{
						continue;
					}
					DLApkDownloadApkItem downloadApkItem = new DLApkDownloadApkItem();
					downloadApkItem.setAppName( (String)info.getValue( "p101" ) );
					downloadApkItem.setPackageName( pkgName );
					downloadApkItem.setCurSize( info.getCurBytes() );
					downloadApkItem.setTotalSize( info.getTotalBytes() );
					downloadApkItem.setIconImgSrc( apkDrawable );
					downloadApkItem.setDownloadCallback( new DownLoadCallBack( downloadApkItem ) );
					// int DownLoadState; //0:未在下载(none) 1:排队等待(wait)
					// 2:正在下载(doing)
					int state = info.getDownloadState();
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( "state=" , String.valueOf( state ) );
					downloadApkItem.setDownLoadState( state );
					if( PAUSEING != state )
					{
						downloadApkItem.setNotifyDoingToLauncher( false );
						dlMgr.ResSetTaskCallback( info , downloadApkItem.getPackageName() , downloadApkItem.getDownloadCallback() );
					}
					if( replacePkgNameDownload( downloadApkItem ) )
					{
						//downloadApkItem = null;
						continue;
					}
					else
					{
						DownloadApkList.add( downloadApkItem );
					}
				}
			}
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( "DownloadApkList.size():" , DownloadApkList.size() ) );
		}
		catch( Exception e )
		{
			// TODO: handle exception
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "InitDownloadList.Exception:" , e.toString() ) );
		}
	}
	
	private boolean replacePkgNameDownload(
			DLApkDownloadApkItem replaceApkItem )
	{
		String PackageName = replaceApkItem.getPackageName();
		DLApkDownloadApkItem dlLoadApkItem;
		for( int i = 0 ; i <= DownloadApkList.size() - 1 ; i++ )
		{
			dlLoadApkItem = DownloadApkList.get( i );
			if( PackageName.equals( dlLoadApkItem.PackageName.toString() ) )
			{
				//DownloadApkList.remove( i );
				//DownloadApkList.add( i , replaceApkItem );
				DownloadApkList.set( i , replaceApkItem );
				dlLoadApkItem = null;
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "ApkMangerActivity onStart" );
		//Init("com.iLoong.launcher.Functions.DynamicEntry.DLManager.MyReceiver.stop", my, ApkMangerActivity.this);
		super.onStart();
	}
	
	@Override
	protected void onRestart()
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "ApkMangerActivity onRestart" );
		super.onRestart();
		//		if(DownloadApkList!=null){
		//			DownloadApkList.clear();
		//			InitDownloadList();
		//		}
	}
	
	private void sendDlMgrBroad(
			String pkgName ,
			String title ,
			String action )
	{
		Intent intent = new Intent();
		intent.setAction( action );
		intent.putExtra( DLApkReceiver.DLAPK_TITLE , title );
		intent.putExtra( DLApkReceiver.DLAPK_PACKAGENAME , pkgName );
		sendBroadcast( intent );
	}
	
	private void sendDlMgrBroad(
			String pkgName ,
			String title ,
			String action ,
			int progress )
	{
		Intent intent = new Intent();
		intent.setAction( action );
		intent.putExtra( DLApkReceiver.DLAPK_TITLE , title );
		intent.putExtra( DLApkReceiver.DLAPK_PACKAGENAME , pkgName );
		intent.putExtra( DLApkReceiver.DLAPK_PROGRESS , progress );
		sendBroadcast( intent );
	}
	
	DLApkDownloadApkItem LongClickListenerDownloadItem = null;
	DlApkInstallApkItemEx LongClickListenerInstallItem = null;
	Dialog LongClickListenerDialog = null;
	
	class DownloadOnItemLongClickListener implements OnItemLongClickListener
	{
		
		@Override
		public boolean onItemLongClick(
				AdapterView<?> arg0 ,
				View arg1 ,
				int arg2 ,
				long arg3 )
		{
			// TODO Auto-generated method stub
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "==== lvDownloadListView.setOnItemLongClickListener===" );
			LongClickListenerDownloadItem = null;
			LongClickListenerDialog = null;
			LongClickListenerDownloadItem = DownloadApkList.get( arg2 );
			LongClickListenerDialog = new AlertDialog.Builder( DlApkMangerActivity.this ).create();
			if( null != LongClickListenerDownloadItem && null != LongClickListenerDialog )
			{
				LayoutInflater inflaterDl = LayoutInflater.from( DlApkMangerActivity.this );
				LinearLayout layout = (LinearLayout)inflaterDl.inflate( R.layout.cool_ml_onlongclick_listview_download , null );
				TextView t1 = (TextView)layout.findViewById( R.id.cool_ml_appListTextView1 );
				TextView t2 = (TextView)layout.findViewById( R.id.cool_ml_appListTextView2 );
				// DownLoadState 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
				if( PAUSEING != LongClickListenerDownloadItem.getDownLoadState() )
				{
					//					final String pageNmeApp = LongClickListenerInstallItem.getPackageName();
					t1.setText( getString( R.string.dynamic_download_app_paused ) );
					t1.setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View arg0 )
						{
							// TODO Auto-generated method stub
							dlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , LongClickListenerDownloadItem.getPackageName() );
							LongClickListenerDialog.cancel();
							Message DownloadControlButClickMsg = new Message();
							// 4 -- 开始或者暂停下载
							DownloadControlButClickMsg.what = Constants.DL_STATUS_PAUSE;
							DownloadControlButClickMsg.obj = LongClickListenerDownloadItem;
							DownloadUpdateHander.sendMessage( DownloadControlButClickMsg );
							//sendDlMgrBroad( LongClickListenerDownloadItem.getPackageName() , Constants.DL_MGR_ACTION_PAUSE );
						}
					} );
				}
				else
				{
					t1.setText( getString( R.string.download_app_continue ) );
					t1.setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View arg0 )
						{
							// TODO Auto-generated method stub
							if( DOWNING == dlMgr.ResDownloadStart( CoolDLResType.RES_TYPE_APK , LongClickListenerDownloadItem.getPackageName() , LongClickListenerDownloadItem.getDownloadCallback() ) )
							{
								DownloadUpdateHander.sendEmptyMessage( Constants.DL_STATUS_PATCH );
								return;
							}
							LongClickListenerDialog.cancel();
							Message DownloadControlButClickMsg = new Message();
							// 4 -- 开始或者暂停下载
							DownloadControlButClickMsg.what = Constants.DL_STATUS_PAUSE;
							DownloadControlButClickMsg.obj = LongClickListenerDownloadItem;
							DownloadUpdateHander.sendMessage( DownloadControlButClickMsg );
							//sendDlMgrBroad( LongClickListenerDownloadItem.getPackageName() , Constants.DL_MGR_ACTION_DOWNING );
						}
					} );
				}
				t2.setText( getString( R.string.dlman_delete ) );
				t2.setOnClickListener( new OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						// TODO Auto-generated method stub
						LongClickListenerDialog.cancel();
						dlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , LongClickListenerDownloadItem.getPackageName() , true );
						Message DownloadDelMsg = new Message();
						DownloadDelMsg.what = Constants.DL_STATUS_REMOVED;
						DownloadDelMsg.obj = LongClickListenerDownloadItem;
						DownloadUpdateHander.sendMessage( DownloadDelMsg );
						//sendDlMgrBroad( LongClickListenerDownloadItem.getPackageName() , Constants.DL_MGR_ACTION_REMOVED );
					}
				} );
				LongClickListenerDialog.show();
				LongClickListenerDialog.getWindow().setContentView( layout );
			}
			return false;
		}
	}
	
	class InstallOnItemLongClickListener implements OnItemLongClickListener
	{
		
		@Override
		public boolean onItemLongClick(
				AdapterView<?> arg0 ,
				View arg1 ,
				int arg2 ,
				long arg3 )
		{
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "==== lvInstallListView.setOnItemLongClickListener===" );
			LongClickListenerInstallItem = null;
			LongClickListenerDialog = null;
			LongClickListenerInstallItem = InstallApkList.get( arg2 );
			LongClickListenerDialog = new AlertDialog.Builder( DlApkMangerActivity.this ).create();
			if( null != LongClickListenerInstallItem && null != LongClickListenerDialog )
			{
				LayoutInflater inflaterDl = LayoutInflater.from( DlApkMangerActivity.this );
				LinearLayout layout = (LinearLayout)inflaterDl.inflate( R.layout.cool_ml_onlongclick_listview_install , null );
				TextView t1 = (TextView)layout.findViewById( R.id.cool_ml_InstallListTextView1 );
				TextView t2 = (TextView)layout.findViewById( R.id.cool_ml_InstallListTextView2 );
				if( INSTALLING == LongClickListenerInstallItem.getInstallState() )
				{
					t1.setText( getString( R.string.dlman_install ) );
					t1.setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View arg0 )
						{
							// TODO Auto-generated method stub
							dl_info info = dlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , LongClickListenerInstallItem.getPackageName() , true );
							LongClickListenerDialog.cancel();
							if( info != null && info.IsDownloadSuccess() )
							{
								MyMethod.InstallApk( DlApkMangerActivity.this , info.getFilePath() );
							}
						}
					} );
				}
				else
				{
					t1.setText( getString( R.string.dlman_start ) );
					t1.setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View arg0 )
						{
							// TODO Auto-generated method stub
							LongClickListenerDialog.cancel();
							StartActivityByPackageName( DlApkMangerActivity.this , LongClickListenerInstallItem.getPackageName() );
						}
					} );
				}
				t2.setText( getString( R.string.dlman_delete ) );
				t2.setOnClickListener( new OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						// TODO Auto-generated method stub
						dlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , LongClickListenerInstallItem.getPackageName() , true );
						SAHelper.getInstance( BaseAppState.getActivityInstance() ).removeSlientItem( LongClickListenerInstallItem.getPackageName() );
						LongClickListenerDialog.cancel();
						sendDlMgrBroad( LongClickListenerInstallItem.getPackageName() , LongClickListenerInstallItem.getAppName() , Constants.DL_MGR_ACTION_REMOVED );
						InstallApkList.remove( LongClickListenerInstallItem );
						installAdapter.Update( InstallApkList );
						UpdateInstallFrame();
					}
				} );
				LongClickListenerDialog.show();
				LongClickListenerDialog.getWindow().setContentView( layout );
			}
			return false;
		}
	}
	
	private void UpdateDownloadFrame()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "UpdateDownloadFrame" );
		if( null == DownloadApkList || 0 == DownloadApkList.size() )
		{
			flDownloadNUllDate.setVisibility( View.VISIBLE );
			flDownlaodListDate.setVisibility( View.GONE );
		}
		else
		{
			flDownloadNUllDate.setVisibility( View.GONE );
			flDownlaodListDate.setVisibility( View.VISIBLE );
		}
	}
	
	private void UpdateInstallFrame()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "UpdateInstallFrame" );
		// TODO Auto-generated method stub
		if( null == InstallApkList || 0 == InstallApkList.size() )
		{
			flInstallNUllDate.setVisibility( View.VISIBLE );
			flInstallListDate.setVisibility( View.GONE );
		}
		else
		{
			flInstallNUllDate.setVisibility( View.GONE );
			flInstallListDate.setVisibility( View.VISIBLE );
		}
	}
	
	private void InitDownLoadListView()
	{
		//		Log.v( TAG , "InitDownLoadListView "+intent.getStringExtra( "packageName" ).toString() );
		if( null != DownloadApkList && DownloadApkList.size() > 0 )
		{
			//			updateDown();
			downloadAdapter = new DownloadListViewAdapter( DownloadApkList , DlApkMangerActivity.this );
			lvDownloadListView.setAdapter( downloadAdapter );
			lvDownloadListView.setOnItemLongClickListener( new DownloadOnItemLongClickListener() );
		}
	}
	
	private void InitInstallListView()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "InitInstallListView " );
		if( null != InstallApkList && InstallApkList.size() > 0 )
		{
			installAdapter = new InstallListViewAdapter( InstallApkList , DlApkMangerActivity.this );
			lvInstallListView.setAdapter( installAdapter );
			lvInstallListView.setOnItemLongClickListener( new InstallOnItemLongClickListener() );
		}
	}
	
	public void updateDown()
	{
		//DownloadApkList.clear();
		InitDownloadList();
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.e( TAG , "ApkMangerActivity onResume" );
		DlManager.getInstance().getReDownloadHelperHandle().startImproperStopTasks( BaseAppState.getActivityInstance() );
		updateDown();
		UpdateDownloadFrame();
		InitDownLoadListView();
		InitInstallList();
		UpdateInstallFrame();
		InitInstallListView();
		//wifi1118 start
		Intent intent = getIntent();
		if( intent != null )
		{
			//	DynamicEntryHandle.getWifiSilentHandle().addInfoToDownloadList();
			int showWhichView = intent.getIntExtra( Constants.SHOW_WHICH_VIEW , Constants.SHOW_DOWNLOAD_VIEW );
			if( showWhichView == Constants.SHOW_INSTALL_VIEW )
			{
				gridPager.setCurrentItem( 1 , true );
			}
		}
		//wifi1118 END
	}
	
	@Override
	protected void onStop()
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "ApkMangerActivity onStop" );
		super.onStop();
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "ApkMangerActivity onDestroy" );
		super.onDestroy();
	}
	
	private void initView()
	{
		// TODO Auto-generated method stub
		gridPager = (ViewPager)findViewById( R.id.cool_ml_themeGridPager );
		viewDownload = LayoutInflater.from( this ).inflate( R.layout.cool_ml_apk_download_view , null );
		viewInstall = LayoutInflater.from( this ).inflate( R.layout.cool_ml_apk_install_view , null );
		GridPagerAdapter adapter = new GridPagerAdapter( viewDownload , viewInstall );
		gridPager.setAdapter( adapter );
		final RadioButton rbutDownload = (RadioButton)findViewById( R.id.cool_ml_rbutDownloadPage );
		final RadioButton rbutInstall = (RadioButton)findViewById( R.id.cool_ml_rbutInstallPage );
		gridPager.setOnPageChangeListener( new OnPageChangeListener() {
			
			@Override
			public void onPageScrollStateChanged(
					int arg0 )
			{
			}
			
			@Override
			public void onPageScrolled(
					int arg0 ,
					float arg1 ,
					int arg2 )
			{
			}
			
			@Override
			public void onPageSelected(
					int index )
			{
				if( index == 0 )
				{
					rbutDownload.toggle();
					isShowDownloadView = true;
					isShowInstallView = false;
					// UpdateListView();
				}
				else if( index == 1 )
				{
					rbutInstall.toggle();
					isShowDownloadView = false;
					isShowInstallView = true;
					// UpdateListView();
				}
			}
		} );
		rbutDownload.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View arg0 )
			{
				gridPager.setCurrentItem( 0 , true );
			}
		} );
		rbutInstall.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View arg0 )
			{
				gridPager.setCurrentItem( 1 , true );
			}
		} );
	}
	
	class InstallListViewHolder
	{
		
		public ImageView ivAppIcon;
		public TextView tvAppName;
		public TextView tvAppVersion;
		public TextView TvAppInstallControl;
	}
	
	class InstallControlButClicklister implements OnClickListener
	{
		
		DlApkInstallApkItemEx CurItem = null;
		
		public InstallControlButClicklister(
				DlApkInstallApkItemEx CurItem )
		{
			// TODO Auto-generated constructor stub
			// int DownLoadState; /* 0:初始化状态 1:下载中 2:正则下载 3:下载完成*/
			this.CurItem = CurItem;
		}
		
		@Override
		public void onClick(
				View v )
		{
			// TODO Auto-generated method stub
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "====InstallControlButClicklister  onClick ===" );
			if( INSTALLING == CurItem.getInstallState() )
			{
				dl_info info = dlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , CurItem.getPackageName() , true );
				//wifi1118 start
				if( info == null )
				{
					info = DlManager.getInstance().getWifiSAHandle().getDlInfo( CurItem.getPackageName() );
				}
				//wifi1118 end
				if( info != null && info.IsDownloadSuccess() )
				{
					MyMethod.InstallApk( DlApkMangerActivity.this , info.getFilePath() );
				}
			}
			else if( STATRTING == CurItem.getInstallState() )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( "DlApkMangerActivity" , StringUtils.concat( "CurItem.getPackageName():" , CurItem.getPackageName() , "-CurItem.getAppName():" , CurItem.getAppName() ) );
				StartActivityByPackageName( DlApkMangerActivity.this , CurItem.getPackageName() );
				//				MyMethod.StartActivityByPackageName( DlApkMangerActivity.this , CurItem.getPackageName() );
			}
		}
	}
	
	class InstallListViewAdapter extends BaseAdapter
	{
		
		List<DlApkInstallApkItemEx> InstallApkList = null;
		private LayoutInflater mInflater = null;
		
		public InstallListViewAdapter(
				List<DlApkInstallApkItemEx> InstallApkList ,
				Context context )
		{
			// TODO Auto-generated constructor stub
			this.InstallApkList = InstallApkList;
			this.mInflater = LayoutInflater.from( context );
		}
		
		public void Update(
				List<DlApkInstallApkItemEx> InstallApkList )
		{
			this.InstallApkList = InstallApkList;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount()
		{
			// TODO Auto-generated method stub
			// return 0;
			return InstallApkList.size();
		}
		
		@Override
		public Object getItem(
				int position )
		{
			// TODO Auto-generated method stub
			// return null;
			return InstallApkList.get( position );
		}
		
		@Override
		public long getItemId(
				int position )
		{
			// TODO Auto-generated method stub
			// return 0;
			return position;
		}
		
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			// TODO Auto-generated method stub
			InstallListViewHolder Holder = null;
			DlApkInstallApkItemEx CurItem = InstallApkList.get( position );
			// 如果缓存convertView为空，则需要创建View
			if( convertView == null )
			{
				Holder = new InstallListViewHolder();
				// 根据自定义的Item布局加载布局
				convertView = mInflater.inflate( R.layout.cool_ml_manager_install_listview , null );
				Holder.ivAppIcon = (ImageView)convertView.findViewById( R.id.cool_ml_manager_appIco1 );
				Holder.tvAppName = (TextView)convertView.findViewById( R.id.cool_ml_manager_appName1 );
				Holder.tvAppVersion = (TextView)convertView.findViewById( R.id.cool_ml_manager_appVersion1 );
				Holder.TvAppInstallControl = (TextView)convertView.findViewById( R.id.cool_ml_manager_button1 );
				Holder.TvAppInstallControl.setOnClickListener( new InstallControlButClicklister( CurItem ) );
				// 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
				convertView.setTag( Holder );
			}
			else
			{
				Holder = (InstallListViewHolder)convertView.getTag();
				Holder.TvAppInstallControl.setOnClickListener( new InstallControlButClicklister( CurItem ) );
			}
			if( null != CurItem.getIconImgSrc() )
			{
				Holder.ivAppIcon.setImageDrawable( CurItem.getIconImgSrc() );
			}
			Holder.tvAppName.setText( CurItem.getAppName() );
			Holder.tvAppVersion.setText( CurItem.getAppVersion() );
			// int InstallState; /* 0:初始化状态 1:安装 2:启动 */
			Resources mResources = getResources();
			if( INSTALLING == CurItem.getInstallState() )
			{
				Holder.TvAppInstallControl.setBackgroundResource( R.drawable.cool_ml_apk_manager_install_item_install_button_selector );
				Holder.TvAppInstallControl.setText( R.string.cool_ml_apk_manager_install_item_install_button_text );
				Holder.TvAppInstallControl.setTextColor( mResources.getColor( R.color.cool_ml_apk_manager_install_item_install_button_text_color ) );
			}
			else
			{
				Holder.TvAppInstallControl.setBackgroundResource( R.drawable.cool_ml_apk_manager_install_item_run_button_selector );
				Holder.TvAppInstallControl.setText( R.string.cool_ml_apk_manager_install_item_run_button_text );
				Holder.TvAppInstallControl.setTextColor( mResources.getColor( R.color.cool_ml_apk_manager_install_item_run_button_text_color ) );
			}
			return convertView;
		}
	}
	
	class DownloadListViewAdapter extends BaseAdapter
	{
		
		List<DLApkDownloadApkItem> DownloadApkList = null;
		
		public DownloadListViewAdapter(
				List<DLApkDownloadApkItem> DownloadApkList ,
				Context context )
		{
			// TODO Auto-generated constructor stub
			this.DownloadApkList = DownloadApkList;
			this.mInflater = LayoutInflater.from( context );
		}
		
		public void Update(
				List<DLApkDownloadApkItem> DownloadApkList )
		{
			this.DownloadApkList = DownloadApkList;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount()
		{
			// TODO Auto-generated method stub
			// return 0;
			return DownloadApkList.size();
		}
		
		@Override
		public Object getItem(
				int position )
		{
			// TODO Auto-generated method stub
			// return null;
			return DownloadApkList.get( position );
		}
		
		private LayoutInflater mInflater = null;
		
		@Override
		public long getItemId(
				int position )
		{
			// TODO Auto-generated method stub
			// return 0;
			return position;
		}
		
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			// TODO Auto-generated method stub
			DownloadListViewHolder Holder = null;
			DLApkDownloadApkItem CurItem = DownloadApkList.get( position );
			// 如果缓存convertView为空，则需要创建View
			if( convertView == null )
			{
				Holder = new DownloadListViewHolder();
				// 根据自定义的Item布局加载布局
				convertView = mInflater.inflate( R.layout.cool_ml_manager_download_listview , null );
				Holder.ivAppIcon = (ImageView)convertView.findViewById( R.id.cool_ml_manager_appIco );
				Holder.tvAppName = (TextView)convertView.findViewById( R.id.cool_ml_manager_appName );
				Holder.progressBarApp = (ProgressBar)convertView.findViewById( R.id.cool_ml_manager_progressBar );
				Holder.tvAppSize = (TextView)convertView.findViewById( R.id.cool_ml_manager_appSize );
				Holder.mDownloadState = (TextView)convertView.findViewById( R.id.cool_ml_manager_download_state );
				Holder.TvAppDownloadControl = (TextView)convertView.findViewById( R.id.cool_ml_manager_button );
				Holder.TvAppDownloadControl.setOnClickListener( new DownloadControlButClicklister( CurItem ) );
				// 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
				convertView.setTag( Holder );
			}
			else
			{
				Holder = (DownloadListViewHolder)convertView.getTag();
				Holder.TvAppDownloadControl.setOnClickListener( new DownloadControlButClicklister( CurItem ) );
			}
			if( null != CurItem.getIconImgSrc() )
			{
				Holder.ivAppIcon.setImageDrawable( CurItem.getIconImgSrc() );
			}
			else
			{
				//Holder.ivAppIcon.setImageDrawable( getIcon( CurItem.PackageName ) );
			}
			Holder.tvAppName.setText( CurItem.getAppName() );
			Long mApkCurSizeByByte = CurItem.getCurSize();
			Long mApkTotalSizeByByte = CurItem.getTotalSize();
			int mCurProgress = (int)( (float)mApkCurSizeByByte * 100 / (float)mApkTotalSizeByByte );
			int mLastProgress = Holder.progressBarApp.getProgress();
			Holder.progressBarApp.setProgress( mCurProgress );
			DecimalFormat df = new DecimalFormat( "0.00" );
			String mApkCurSizeByMB = df.format( mApkCurSizeByByte / 1024f / 1024f );
			String mApkTotalSizeByMB = df.format( mApkTotalSizeByByte / 1024f / 1024f );
			Holder.tvAppSize.setText( StringUtils.concat( mApkCurSizeByMB , "MB/" , mApkTotalSizeByMB , "MB" ) );
			// DownLoadState 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
			// icon_btn_list_pause icon_btn_list_install icon_btn_list_download
			String mDownloadStateText;
			if( DOWNING == CurItem.getDownLoadState() )
			{
				Holder.TvAppDownloadControl.setBackgroundResource( R.drawable.cool_ml_icon_btn_list_download );
				int mDownloadSpeed = (int)( ( mCurProgress - mLastProgress ) * mApkTotalSizeByByte / 1024 / 100 );
				mDownloadStateText = StringUtils.concat( mDownloadSpeed , "k/s" );
			}
			else if( WAITTING == CurItem.getDownLoadState() )
			{
				Holder.TvAppDownloadControl.setBackgroundResource( R.drawable.cool_ml_icon_btn_list_waiting_download );
				mDownloadStateText = getResources().getString( R.string.cool_ml_apk_manager_download_item_state_wait );
			}
			else
			{
				Holder.TvAppDownloadControl.setBackgroundResource( R.drawable.cool_ml_icon_btn_list_pause );
				mDownloadStateText = getResources().getString( R.string.cool_ml_apk_manager_download_item_state_pause );
			}
			Holder.mDownloadState.setText( mDownloadStateText );
			return convertView;
		}
		
		public Drawable getIcon(
				String packageName )
		{
			String dstFileName = StringUtils.concat( ICON_DEFAULT_FOLDER , packageName , ".png" );
			//			DefaultLayout.ICON_DEFAULT_FOLDER;
			try
			{
				//				item.mIconBitmap = BitmapFactory.decodeStream( iLoongLauncher.getInstance().getAssets().open( dstFileName ) );
				BitmapDrawable bd = new BitmapDrawable( DlApkMangerActivity.this.getResources() , BitmapFactory.decodeStream( getAssets().open( dstFileName ) ) );
				return bd;
			}
			catch( IOException ex )
			{
				ex.printStackTrace();
			}
			return DlApkMangerActivity.this.getResources().getDrawable( R.drawable.cool_ml_download_install );
		}
	}
	
	class DownloadControlButClicklister implements OnClickListener
	{
		
		DLApkDownloadApkItem CurItem = null;
		
		public DownloadControlButClicklister(
				DLApkDownloadApkItem CurItem )
		{
			// TODO Auto-generated constructor stub
			// int DownLoadState; /* 0:初始化状态 1:下载中 2:正则下载 3:下载完成*/
			this.CurItem = CurItem;
		}
		
		@Override
		public void onClick(
				View v )
		{
			// TODO Auto-generated method stub
			// int DownLoadState; /* 0:初始化状态 1:下载中 2:正则下载 3:下载完成*/
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "DownloadControlButClicklister - onClick  DownLoadState:" , CurItem.getDownLoadState() , "-PackageName:" , CurItem.getPackageName() ) );
			// DownloadState 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
			if( PAUSEING == CurItem.getDownLoadState() )
			{
				//				if( Icon3DHandle.SHOW_ICON_STYLE_V2 == DefaultLayout.show_icon_style )
				//				{
				//					DynamicEntryHandle.getSharedPreferenceHandle().saveValue(
				//							SharedPreferenceHandle.DOWNLOAD_APK_PREFIX + CurItem.getPackageName() ,
				//							String.valueOf( SharedPreferenceHandle.APK_DOWNLOAD ) );
				//				}
				//sendDlMgrBroad( CurItem.getPackageName() , Constants.DL_MGR_ACTION_DOWNING );
				if( DOWNING == dlMgr.ResDownloadStart( CoolDLResType.RES_TYPE_APK , CurItem.getPackageName() , CurItem.getDownloadCallback() ) )
				{
					DownloadUpdateHander.sendEmptyMessage( Constants.DL_STATUS_PATCH );
					return;
				}
			}
			else
			{
				//				if( Icon3DHandle.SHOW_ICON_STYLE_V2 == DefaultLayout.show_icon_style )
				//				{
				//					DynamicEntryHandle.getSharedPreferenceHandle()
				//							.saveValue( SharedPreferenceHandle.DOWNLOAD_APK_PREFIX + CurItem.getPackageName() , String.valueOf( SharedPreferenceHandle.APK_STOP ) );
				//				}
				//sendDlMgrBroad( CurItem.getPackageName() , Constants.DL_MGR_ACTION_PAUSE );
				dlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , CurItem.getPackageName() );
			}
			Message DownloadControlButClickMsg = new Message();
			// 4 -- 开始或者暂停下载
			DownloadControlButClickMsg.what = Constants.DL_STATUS_PAUSE;
			DownloadControlButClickMsg.obj = CurItem;
			DownloadUpdateHander.sendMessage( DownloadControlButClickMsg );
		}
	}
	
	class DownloadListViewHolder
	{
		
		public ImageView ivAppIcon;
		public TextView tvAppName;
		public ProgressBar progressBarApp;
		public TextView tvAppSize;
		public TextView mDownloadState;
		public TextView TvAppDownloadControl;
	}
	
	public enum DownloadType
	{
		ON_SUCESS , ON_FAILED , ON_DOING ,
	}
	
	class DownLoadCallBackMsgData
	{
		
		DLApkDownloadApkItem CurItem;
		dl_info DlInfo;
		
		public DownLoadCallBackMsgData(
				DLApkDownloadApkItem CurItem ,
				dl_info DlInfo )
		{
			// TODO Auto-generated constructor stub
			this.CurItem = CurItem;
			this.DlInfo = DlInfo;
		}
		
		public DLApkDownloadApkItem getCurItem()
		{
			return CurItem;
		}
		
		public dl_info getDlInfo()
		{
			return DlInfo;
		}
	}
	
	class DownLoadCallBack implements CoolDLCallback
	{
		
		private DLApkDownloadApkItem DownLoadCallBack;
		DLApkDownloadApkItem CurItem;
		
		public DownLoadCallBack(
				DLApkDownloadApkItem CurItem )
		{
			// TODO Auto-generated constructor stub
			this.CurItem = CurItem;
		}
		
		@Override
		public void onDoing(
				CoolDLResType arg0 ,
				String arg1 ,
				dl_info arg2 )
		{
			// TODO Auto-generated method stub
			//Log.v( "APK_HL" , "APK  onDoing  name:" + "getCurBytes:" + arg2.getCurBytes() );
			DownLoadCallBackMsgData msgData = new DownLoadCallBackMsgData( CurItem , arg2 );
			Message DownloadOnDoingMsg = new Message();
			DownloadOnDoingMsg.what = Constants.DL_STATUS_ING;
			DownloadOnDoingMsg.obj = msgData;
			DownloadUpdateHander.sendMessage( DownloadOnDoingMsg );
		}
		
		@Override
		public void onSuccess(
				CoolDLResType arg0 ,
				String arg1 ,
				dl_info arg2 )
		{
			// TODO Auto-generated method stub
			// UpdateDownloadList(CurItem, DownloadType.ON_SUCESS, arg2);
			Message DownloadOnDoingMsg = new Message();
			DownloadOnDoingMsg.what = Constants.DL_STATUS_SUCCESS;
			DownloadOnDoingMsg.obj = CurItem;
			DownloadUpdateHander.sendMessage( DownloadOnDoingMsg );
		}
		
		@Override
		public void onFail(
				CoolDLResType arg0 ,
				String arg1 ,
				dl_info arg2 )
		{
			// TODO Auto-generated method stub
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "====  onFail ===" );
			Message DownloadOnDoingMsg = new Message();
			DownloadOnDoingMsg.what = Constants.DL_STATUS_FAIL;
			DownloadOnDoingMsg.obj = CurItem;
			DownloadUpdateHander.sendMessage( DownloadOnDoingMsg );
		}
	}
	
	private class GridPagerAdapter extends PagerAdapter
	{
		
		private final String LOG_TAG = "GridPagerAdapter";
		private View viewFrist;
		private View viewSecond;
		
		public GridPagerAdapter(
				View frist ,
				View second )
		{
			this.viewFrist = frist;
			this.viewSecond = second;
		}
		
		@Override
		public void destroyItem(
				ViewGroup container ,
				int position ,
				Object object )
		{
		}
		
		@Override
		public int getItemPosition(
				Object object )
		{
			return PagerAdapter.POSITION_UNCHANGED;
		}
		
		@Override
		public int getCount()
		{
			return 2;
		}
		
		@Override
		public Object instantiateItem(
				ViewGroup container ,
				int position )
		{
			if( position == 0 )
			{
				container.addView( viewFrist );
				return viewFrist;
			}
			else
			{
				container.addView( viewSecond );
				return viewSecond;
			}
		}
		
		@Override
		public boolean isViewFromObject(
				View view ,
				Object object )
		{
			return view == ( object );
		}
		
		@Override
		public void restoreState(
				Parcelable state ,
				ClassLoader loader )
		{
		}
		
		@Override
		public Parcelable saveState()
		{
			return null;
		}
	}
	
	public static Boolean IsNetworkAvailable(
			Context context )
	{
		final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		if( info != null && info.isAvailable() )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "=== IsNetworkAvailable  true ===" );
			return true;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "=== IsNetworkAvailable  false ===" );
		return false;
	}
	
	public static Boolean IsStorageCanUsed()
	{
		if( Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED ) )
		{//获取外部存储空间
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs( path.getPath() );
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getAvailableBlocks();
			int bignumber = (int)( ( totalBlocks * blockSize ) >> 20 );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( " bignumber:" , bignumber , "MB" ) );
			if( bignumber > 10 )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , "=== IsStorageCanUsed  true ===" );
				return true;
			}
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "=== IsStorageCanUsed  false ===" );
		return false;
	}
	
	//	public static  void Init(String tent,MyReceiver rec,Context context)
	//	{
	//		rec = new MyReceiver();
	//		IntentFilter filter = new IntentFilter();
	//		filter.addAction( "com.iLoong.launcher.Functions.DynamicEntry.DLManager.MyReceiver.start" );
	//		filter.addAction( "com.iLoong.launcher.Functions.DynamicEntry.DLManager.MyReceiver.stop" );
	//		filter.addDataScheme( "package" );
	//		context.registerReceiver( rec , filter );
	//	}
	public static String getVersion(
			String pageName ,
			Context context )
	{
		try
		{
			PackageInfo info = context.getPackageManager().getPackageInfo( pageName , 0 );
			// String versionName = info.versionName;
			return info.versionName;
		}
		catch( NameNotFoundException e )
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	}
	
	public static void StartActivityByPackageName(
			Context context ,
			String packageName )
	{
		PackageInfo pi;
		try
		{
			pi = context.getPackageManager().getPackageInfo( packageName , 0 );
			Intent resolveIntent = new Intent( Intent.ACTION_MAIN , null );
			resolveIntent.addCategory( Intent.CATEGORY_LAUNCHER );
			resolveIntent.setPackage( pi.packageName );
			PackageManager pManager = context.getPackageManager();
			List<ResolveInfo> apps = pManager.queryIntentActivities( resolveIntent , 0 );
			ResolveInfo ri = apps.iterator().next();
			if( ri != null )
			{
				Intent intent = new Intent( Intent.ACTION_MAIN );
				intent.addCategory( Intent.CATEGORY_LAUNCHER );
				intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				intent.addFlags( Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
				intent.setClassName( ri.activityInfo.packageName , ri.activityInfo.name );
				context.startActivity( intent );
			}
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
}
