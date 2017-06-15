package com.iLoong.launcher.MList;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
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

import com.iLoong.base.themebox.R;

import cool.sdk.common.MyMethod;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


public class ApkMangerActivity extends Activity
{
	
	MeApkDownloadManager MeapkDlMgr = null;
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
	FrameLayout flDownloadNUllDate = null;
	FrameLayout flDownlaodListDate = null;
	FrameLayout flInstallNUllDate = null;
	FrameLayout flInstallListDate = null;
	ListView lvInstallListView = null;
	ListView lvDownloadListView = null;
	List<DownloadApkItem> DownloadApkList = new ArrayList<DownloadApkItem>();
	List<InstallApkItemEx> InstallApkList = new ArrayList<InstallApkItemEx>();
	long longTime = 0L;
	static List<String> SucessCallbackList = new ArrayList<String>();
	static List<String> FailedCallbackList = new ArrayList<String>();
	//Class<?> mActivityClass[] = { Main_FirstActivity.class , Main_SecondActivity.class};
	boolean visible[];
	int index = -1;
	public static ApkMangerActivity instance = null;
	public static int CurEntryID = -1;
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
					case 1:// 下载进度更新
					{
						DownLoadCallBackMsgData MsgData = (DownLoadCallBackMsgData)msg.obj;
						dl_info dlInfo = MsgData.getDlInfo();
						DownloadApkItem CurItem = MsgData.getCurItem();
						CurItem.setCurSize( dlInfo.getCurBytes() );
						CurItem.setTotalSize( dlInfo.getTotalBytes() );
						if( null == CurItem.getIconImgSrc() && null != MeapkDlMgr.GetSdkIconMgr().IconGetInfo( CurItem.getPackageName() ) )
						{
							String ImgPath = MeapkDlMgr.GetSdkIconMgr().IconGetInfo( CurItem.getPackageName() ).getFilePath();
							if( null != ImgPath && ImgPath.length() > 1 )
							{
								CurItem.setIconImgSrc( Drawable.createFromPath( ImgPath ) );
							}
						}
						// int DownLoadState; //0:未在下载(none) 1:排队等待(wait)
						// 2:正在下载(doing)
						if( 2 != CurItem.getDownLoadState() )
						{
							CurItem.setDownLoadState( 2 );
						}
						if( System.currentTimeMillis() - longTime > 1500 )
						{
							downloadAdapter.Update( DownloadApkList );
							longTime = System.currentTimeMillis();
						}
						MsgData = null;
					}
						break;
					case 2:// 成功
					{
						DownloadApkItem CurItem = (DownloadApkItem)msg.obj;
						if( null != CurItem )
						{
							DownloadApkList.remove( CurItem );
							downloadAdapter.Update( DownloadApkList );
							UpdateDownloadFrame();
							InitInstallList();
						}
					}
						break;
					case 3:// 失败
					{
						DownloadApkItem CurItem = (DownloadApkItem)msg.obj;
						// int DownLoadState; //0:未在下载(none) 1:排队等待(wait)
						// 2:正在下载(doing)
						if( 0 != CurItem.getDownLoadState() )
						{
							CurItem.setDownLoadState( 0 );
						}
						downloadAdapter.Update( DownloadApkList );
					}
						break;
					case 4:// 开始或暂停下载
					{
						DownloadApkItem CurDownloadItem = (DownloadApkItem)msg.obj;
						// int DownLoadState; //0:未在下载(none) 1:排队等待(wait)
						// 2:正在下载(doing)
						if( 0 == CurDownloadItem.getDownLoadState() )
						{
							CurDownloadItem.setDownLoadState( 1 );
						}
						else
						{
							CurDownloadItem.setDownLoadState( 0 );
						}
						downloadAdapter.Update( DownloadApkList );
					}
						break;
					case 5:// 删除下载项
					{
						DownloadApkItem CurDownloadItem = (DownloadApkItem)msg.obj;
						//						try
						//						{
						//							JSClass.JSClass.invokeJSMethod( "reloadDownstate" , CurDownloadItem.getPackageName() );
						//						}
						//						catch( Exception e )
						//						{
						//							// TODO: handle exception
						//							MELOG.e( "ME_RTFSC" , "DownloadUpdateHander 5 Exception:" + e.toString() );
						//						}
						DownloadApkList.remove( CurDownloadItem );
						downloadAdapter.Update( DownloadApkList );
					}
						break;
					case 6:// 开始创建下载项时失败
					{
						MELOG.v( "ME_RTFSC" , " ======  cool_ml_download_failed  ApkManager===== " );
						int InfoID = R.string.cool_ml_download_failed;
						// TODO: handle exception
						if( false == JSClass.IsNetworkAvailableLocal( getApplicationContext() ) )
						{
							InfoID = R.string.cool_ml_network_not_available;
						}
						else if( false == JSClass.IsStorageCanUsed() )
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
				MELOG.e( "ME_RTFSC" , "handleMessage Exception:" + e.toString() );
			}
		}
	};
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSCX" , "ApkMangerActivity onCreate" );
		super.onCreate( savedInstanceState );
		Intent intent = getIntent();
		try
		{
			String moudleName = intent.getStringExtra( "moudleName" );
			entryId = intent.getIntExtra( "entryId" , -1 );
			MELOG.v( "ME_RTFSC" , "1111 entryId:" + entryId );
			if( entryId == -1 )
			{
				finish();
				return;
			}
			MeapkDlMgr = MeApkDlMgrBuilder.Build( getApplicationContext() , moudleName , entryId );
			instance = this;
			CurEntryID = entryId;
			MELOG.v( "ME_RTFSCX" , "ApkMangerActivity onCreate ===  instance:" + instance + ",CurEntryID:" + CurEntryID );
		}
		catch( Exception e )
		{
			finish();
			return;
		}
		setContentView( R.layout.cool_ml_apk_manager );
		InitDownloadList();
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
		flDownloadNUllDate = (FrameLayout)viewDownload.findViewById( R.id.cool_ml_flDownloadNULLData );
		flDownlaodListDate = (FrameLayout)viewDownload.findViewById( R.id.cool_ml_flDownloadList );
		lvDownloadListView = (ListView)viewDownload.findViewById( R.id.cool_ml_lvDownload );
		flInstallNUllDate = (FrameLayout)viewInstall.findViewById( R.id.cool_ml_flInsatllNULLData );
		flInstallListDate = (FrameLayout)viewInstall.findViewById( R.id.cool_ml_flInsatllList );
		lvInstallListView = (ListView)viewInstall.findViewById( R.id.cool_ml_lvInstall );
		//		MeCoolDLCallback.MeAddActiveCallBackMap.put( MeApkDLShowType.DownloadManagerActivity , new MeActiveCallback() {
		//			
		//			@Override
		//			public void NoifySatrtAction(
		//					String pkgname )
		//			{
		//				// TODO Auto-generated method stub
		//				for( int i = 0 ; i < DownloadApkList.size() ; i++ )
		//				{
		//					if( DownloadApkList.get( i ).getPackageName().equals( pkgname ) )
		//					{
		//						DownloadApkItem curItem = DownloadApkList.get( i );
		//						MeapkDlMgr.AddCallback( MeApkDLShowType.DownloadManagerActivity , pkgname , curItem.getDownloadCallback() );
		//						Message DownloadControlButClickMsg = new Message();
		//						// 4 -- 开始或者暂停下载
		//						DownloadControlButClickMsg.what = 4;
		//						DownloadControlButClickMsg.obj = curItem;
		//						DownloadUpdateHander.sendMessage( DownloadControlButClickMsg );
		//					}
		//				}
		//			}
		//		} );
		MeApkDownloadManager.MeAddActiveCallBackMap.put( MeApkDLShowType.DownloadManagerActivity , new MeActiveCallback() {
			
			@Override
			public void NotifyUninstallApkAction(
					String pkgname )
			{
				// TODO Auto-generated method stub
				for( int i = 0 ; i < InstallApkList.size() ; i++ )
				{
					InstallApkItemEx CurInstallApkItem = InstallApkList.get( i );
					if( pkgname.equals( CurInstallApkItem.getPackageName() ) )
					{
						CurInstallApkItem.setInstallState( 1 );
					}
					installAdapter.Update( InstallApkList );
				}
			}
			
			@Override
			public void NotifyInstallSucessAction(
					String pkgname )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void NotifyDelAction(
					String pkgname )
			{
				// TODO Auto-generated method stub
				//只有此模块才能删除下载、安装列表中的项，回调函数不做任何处理
			}
			
			@Override
			public void NoifySatrtAction(
					String pkgname )
			{
				// TODO Auto-generated method stub
				for( int i = 0 ; i < DownloadApkList.size() ; i++ )
				{
					if( DownloadApkList.get( i ).getPackageName().equals( pkgname ) )
					{
						DownloadApkItem curItem = DownloadApkList.get( i );
						MeapkDlMgr.AddCallback( MeApkDLShowType.DownloadManagerActivity , pkgname , curItem.getDownloadCallback() );
						Message DownloadControlButClickMsg = new Message();
						// 4 -- 开始或者暂停下载
						DownloadControlButClickMsg.what = 4;
						DownloadControlButClickMsg.obj = curItem;
						DownloadUpdateHander.sendMessage( DownloadControlButClickMsg );
					}
				}
			}
		} );
	}
	
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	public String getVersion(
			String pageName ,
			Context context )
	{
		try
		{
			return context.getPackageManager().getPackageInfo( pageName , 0 ).versionName;
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			MELOG.v( "ME_RTFSC" , "getVersion Error:" + e.toString() );
			return null;
		}
	}
	
	private void InitInstallList()
	{
		// TODO Auto-generated method stub
		try
		{
			List<dl_info> ApkTaskList = null;
			List<dl_info> IconTaskList = null;
			MELOG.v( "ME_RTFSC" , "InitInstallList" );
			ApkTaskList = MeapkDlMgr.GetSdkApkDlMgr().ResGetTaskList( CoolDLResType.RES_TYPE_APK );
			MELOG.v( "ME_RTFSC" , "ApkTaskList.size()" + ApkTaskList.size() );
			// IconTaskList = apkDlMgr.ResGetTaskList(CoolDLResType.RES_TYPE_PIC);
			InstallApkList.clear();
			for( dl_info info : ApkTaskList )
			{
				if( info.IsDownloadSuccess() )
				{
					if( null == (String)info.getValue( "r4" ) )
					{
						MELOG.v( "ME_RTFSC" , " null == (String)info.getValue( 'r4')" );
						continue;
					}
					InstallApkItemEx InstallApkItem = new InstallApkItemEx();
					InstallApkItem.setPackageName( (String)info.getValue( "r4" ) );
					InstallApkItem.setAppName( (String)info.getValue( "p101" ) );
					InstallApkItem.setAppSize( info.getCurBytes() );
					InstallApkItem.setAppVersion( (String)info.getValue( "versionName" ) );
					if( null != MeapkDlMgr.GetSdkIconMgr().IconGetInfo( InstallApkItem.getPackageName() ) )
					{
						String ImgPath = MeapkDlMgr.GetSdkIconMgr().IconGetInfo( InstallApkItem.getPackageName() ).getFilePath();
						if( null != ImgPath && ImgPath.length() > 1 )
						{
							InstallApkItem.setIconImgSrc( Drawable.createFromPath( ImgPath ) );
						}
					}
					// int InstallState; /* 0:初始化状态 1:安装 2:启动 */
					if( null != InstallApkItem.getAppVersion() && true == InstallApkItem.getAppVersion().equals( getVersion( InstallApkItem.getPackageName() , ApkMangerActivity.this ) ) )
					{
						InstallApkItem.setInstallState( 2 );
					}
					else
					{
						InstallApkItem.setInstallState( 1 );
					}
					InstallApkList.add( InstallApkItem );
				}
			}
			MELOG.v( "ME_RTFSC" , "InstallApkList.size()" + InstallApkList.size() );
		}
		catch( Exception e )
		{
			// TODO: handle exception
			MELOG.v( "ME_RTFSC" , "InitInstallList.Exception" + e.toString() );
		}
	}
	
	private void InitDownloadList()
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "InitDownloadList" );
		try
		{
			List<dl_info> ApkTaskList = null;
			List<dl_info> IconTaskList = null;
			ApkTaskList = MeapkDlMgr.GetSdkApkDlMgr().ResGetTaskList( CoolDLResType.RES_TYPE_APK );
			// IconTaskList = apkDlMgr.ResGetTaskList(CoolDLResType.RES_TYPE_PIC);
			MELOG.v( "ME_RTFSC" , "ApkTaskList.size()" + ApkTaskList.size() );
			for( dl_info info : ApkTaskList )
			{
				// DownloadState 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
				if( !info.IsDownloadSuccess() || 0 != info.getDownloadState() )
				{
					if( null == (String)info.getValue( "r4" ) && null == (String)info.getValue( "p2" ) )
					{
						MELOG.v( "ME_RTFSC" , " null == (String)info.getValue( 'r4' ) && null == (String)info.getValue( 'p2' )" );
						continue;
					}
					DownloadApkItem downloadApkItem = new DownloadApkItem();
					if( null != (String)info.getValue( "r4" ) )
					{
						downloadApkItem.setPackageName( (String)info.getValue( "r4" ) );
					}
					else
					{
						downloadApkItem.setPackageName( (String)info.getValue( "p2" ) );
					}
					// downloadApkItem.setPackageName((String)
					// info.getValue("p2"));
					downloadApkItem.setAppName( (String)info.getValue( "p101" ) );
					downloadApkItem.setCurSize( info.getCurBytes() );
					downloadApkItem.setTotalSize( info.getTotalBytes() );
					if( null != MeapkDlMgr.GetSdkIconMgr().IconGetInfo( downloadApkItem.getPackageName() ) )
					{
						String ImgPath = MeapkDlMgr.GetSdkIconMgr().IconGetInfo( downloadApkItem.getPackageName() ).getFilePath();
						if( null != ImgPath && ImgPath.length() > 1 )
						{
							downloadApkItem.setIconImgSrc( Drawable.createFromPath( ImgPath ) );
						}
					}
					downloadApkItem.setDownloadCallback( new DownLoadCallBack( MeApkDLShowType.DownloadManagerActivity , downloadApkItem ) );
					// int DownLoadState; //0:未在下载(none) 1:排队等待(wait)
					// 2:正在下载(doing)
					downloadApkItem.setDownLoadState( info.getDownloadState() );
					if( 0 != info.getDownloadState() )
					{
						//apkDlMgr.ResSetTaskCallback( info , downloadApkItem.getPackageName() , downloadApkItem.getDownloadCallback() );
						MeapkDlMgr.AddCallback( MeApkDLShowType.DownloadManagerActivity , downloadApkItem.getPackageName() , downloadApkItem.getDownloadCallback() );
					}
					DownloadApkList.add( downloadApkItem );
				}
			}
			MELOG.v( "ME_RTFSC" , "DownloadApkList.size()" + DownloadApkList.size() );
		}
		catch( Exception e )
		{
			// TODO: handle exception
			MELOG.e( "ME_RTFSC" , "InitDownloadList.Exception:" + e.toString() );
		}
	}
	
	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "ApkMangerActivity onStart" );
		super.onStart();
	}
	
	@Override
	protected void onRestart()
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "ApkMangerActivity onRestart" );
		super.onRestart();
	}
	
	DownloadApkItem LongClickListenerDownloadItem = null;
	InstallApkItemEx LongClickListenerInstallItem = null;
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
			MELOG.v( "ME_RTFSC" , "==== lvDownloadListView.setOnItemLongClickListener===" );
			LongClickListenerDownloadItem = null;
			LongClickListenerDialog = null;
			LongClickListenerDownloadItem = DownloadApkList.get( arg2 );
			LongClickListenerDialog = new AlertDialog.Builder( ApkMangerActivity.this ).create();
			if( null != LongClickListenerDownloadItem && null != LongClickListenerDialog )
			{
				LayoutInflater inflaterDl = LayoutInflater.from( ApkMangerActivity.this );
				LinearLayout layout = (LinearLayout)inflaterDl.inflate( R.layout.cool_ml_onlongclick_listview , null );
				TextView t1 = (TextView)layout.findViewById( R.id.cool_ml_appListTextView1 );
				TextView t2 = (TextView)layout.findViewById( R.id.cool_ml_appListTextView2 );
				// DownLoadState 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
				if( 0 != LongClickListenerDownloadItem.getDownLoadState() )
				{
					t1.setText( "暂停" );
					t1.setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View arg0 )
						{
							// TODO Auto-generated method stub
							//apkDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , LongClickListenerDownloadItem.getPackageName() );
							MeapkDlMgr.StopDownload( MeApkDLShowType.DownloadManagerActivity , LongClickListenerDownloadItem.getPackageName() );
							//							try
							//							{
							//								JSClass.JSClass.invokeJSMethod( "reloadDownstate" , LongClickListenerDownloadItem.getPackageName() );
							//							}
							//							catch( Exception e )
							//							{
							//								// TODO: handle exception
							//								MELOG.e( "ME_RTFSC" , "DownloadUpdateHander 4 Exception:" + e.toString() );
							//							}
							LongClickListenerDialog.cancel();
							Message DownloadControlButClickMsg = new Message();
							// 4 -- 开始或者暂停下载
							DownloadControlButClickMsg.what = 4;
							DownloadControlButClickMsg.obj = LongClickListenerDownloadItem;
							DownloadUpdateHander.sendMessage( DownloadControlButClickMsg );
						}
					} );
				}
				else
				{
					t1.setText( "继续下载" );
					t1.setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View arg0 )
						{
							//首先刷新状态，再调用下载（停止）函数进行下载（停止）
							Message DownloadControlButClickMsg = new Message();
							// 4 -- 开始或者暂停下载
							DownloadControlButClickMsg.what = 4;
							DownloadControlButClickMsg.obj = LongClickListenerDownloadItem;
							DownloadUpdateHander.sendMessage( DownloadControlButClickMsg );
							// TODO Auto-generated method stub
							MeapkDlMgr.ReStartDownload( MeApkDLShowType.DownloadManagerActivity , LongClickListenerDownloadItem.getPackageName() , LongClickListenerDownloadItem.getDownloadCallback() );
							LongClickListenerDialog.cancel();
						}
					} );
				}
				t2.setText( "删除" );
				t2.setOnClickListener( new OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						// TODO Auto-generated method stub
						LongClickListenerDialog.cancel();
						//apkDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , LongClickListenerDownloadItem.getPackageName() , true );
						MeapkDlMgr.DelDownload( MeApkDLShowType.DownloadManagerActivity , LongClickListenerDownloadItem.getPackageName() );
						Message DownloadDelMsg = new Message();
						DownloadDelMsg.what = 5;
						DownloadDelMsg.obj = LongClickListenerDownloadItem;
						DownloadUpdateHander.sendMessage( DownloadDelMsg );
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
			MELOG.v( "ME_RTFSC" , "==== lvInstallListView.setOnItemLongClickListener===" );
			LongClickListenerInstallItem = null;
			LongClickListenerDialog = null;
			LongClickListenerInstallItem = InstallApkList.get( arg2 );
			LongClickListenerDialog = new AlertDialog.Builder( ApkMangerActivity.this ).create();
			if( null != LongClickListenerInstallItem && null != LongClickListenerDialog )
			{
				LayoutInflater inflaterDl = LayoutInflater.from( ApkMangerActivity.this );
				LinearLayout layout = (LinearLayout)inflaterDl.inflate( R.layout.cool_ml_onlongclick_listview_install , null );
				TextView t1 = (TextView)layout.findViewById( R.id.cool_ml_InstallListTextView1 );
				TextView t2 = (TextView)layout.findViewById( R.id.cool_ml_InstallListTextView2 );
				if( 1 == LongClickListenerInstallItem.getInstallState() )
				{
					t1.setText( "安装" );
					t1.setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View arg0 )
						{
							// TODO Auto-generated method stub
							dl_info info = MeapkDlMgr.GetSdkApkDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , LongClickListenerInstallItem.getPackageName() , true );
							LongClickListenerDialog.cancel();
							if( info != null && info.IsDownloadSuccess() )
							{
								MyMethod.InstallApk( ApkMangerActivity.this , info.getFilePath() );
							}
						}
					} );
				}
				else
				{
					t1.setText( "启动" );
					t1.setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View arg0 )
						{
							// TODO Auto-generated method stub
							LongClickListenerDialog.cancel();
							StartActivityByPackageName( LongClickListenerInstallItem.getPackageName() , getApplicationContext() );
							//MyMethod.StartActivityByPackageName( getApplicationContext() , LongClickListenerInstallItem.getPackageName() );
							PackageInfo pi;
							try
							{
								pi = getPackageManager().getPackageInfo( LongClickListenerInstallItem.getPackageName() , 0 );
								Intent resolveIntent = new Intent( Intent.ACTION_MAIN , null );
								resolveIntent.setPackage( pi.packageName );
								PackageManager pManager = getPackageManager();
								List<ResolveInfo> apps = pManager.queryIntentActivities( resolveIntent , 0 );
								ResolveInfo ri = apps.iterator().next();
								if( ri != null )
								{
									Intent intent = new Intent( Intent.ACTION_VIEW );
									intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
									intent.addFlags( Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
									intent.setClassName( ri.activityInfo.packageName , ri.activityInfo.name );
									startActivity( intent );
								}
							}
							catch( Exception e )
							{
								// TODO Auto-generated catch block
								//e.printStackTrace();
							}
						}
					} );
				}
				t2.setText( "删除" );
				t2.setOnClickListener( new OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						// TODO Auto-generated method stub
						//apkDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , LongClickListenerInstallItem.getPackageName() , true );
						MeapkDlMgr.DelDownload( MeApkDLShowType.DownloadManagerActivity , LongClickListenerInstallItem.getPackageName() );
						LongClickListenerDialog.cancel();
						//						try
						//						{
						//							JSClass.JSClass.invokeJSMethod( "reloadDownstate" , LongClickListenerInstallItem.getPackageName() );
						//						}
						//						catch( Exception e )
						//						{
						//							// TODO: handle exception
						//							MELOG.e( "ME_RTFSC" , "InstallOnItemLongClickListener Exception:" + e.toString() );
						//						}
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
	
	private boolean StartActivityByPackageName(
			String pkgName ,
			Context mContect )
	{
		PackageManager packageManager = mContect.getPackageManager();
		Intent intent = null;
		try
		{
			intent = packageManager.getLaunchIntentForPackage( pkgName );
		}
		catch( Exception e )
		{
			intent = null;
		}
		if( null != intent )
		{
			mContect.startActivity( intent );
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void UpdateDownloadFrame()
	{
		MELOG.v( "ME_RTFSC" , "UpdateDownloadFrame" );
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
		MELOG.v( "ME_RTFSC" , "UpdateInstallFrame" );
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
		MELOG.v( "ME_RTFSC" , "InitDownLoadListView " );
		if( null != DownloadApkList && DownloadApkList.size() > 0 )
		{
			downloadAdapter = new DownloadListViewAdapter( DownloadApkList , ApkMangerActivity.this );
			lvDownloadListView.setAdapter( downloadAdapter );
			lvDownloadListView.setOnItemLongClickListener( new DownloadOnItemLongClickListener() );
		}
	}
	
	private void InitInstallListView()
	{
		MELOG.v( "ME_RTFSC" , "InitInstallListView " );
		if( null != InstallApkList && InstallApkList.size() > 0 )
		{
			installAdapter = new InstallListViewAdapter( InstallApkList , ApkMangerActivity.this );
			lvInstallListView.setAdapter( installAdapter );
			lvInstallListView.setOnItemLongClickListener( new InstallOnItemLongClickListener() );
		}
	}
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "ApkMangerActivity onResume" );
		InitInstallList();
		InitInstallListView();
		InitDownLoadListView();
		UpdateDownloadFrame();
		UpdateInstallFrame();
		super.onResume();
		MELOG.v( "ME_RTFSC" , "ApkMangerActivity END" );
	}
	
	@Override
	protected void onStop()
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "ApkMangerActivity onStop" );
		super.onStop();
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "ApkMangerActivity onDestroy" );
		SucessCallbackList.clear();
		FailedCallbackList.clear();
		//MeCoolDLCallback.MeAddActiveCallBackMap.remove( MeApkDLShowType.DownloadManagerActivity );
		MeApkDownloadManager.MeAddActiveCallBackMap.remove( MeApkDLShowType.DownloadManagerActivity );
		instance = null;
		CurEntryID = -1;
		if( !MeGeneralMethod.IsDownloadTaskRunning( getApplicationContext() ) && !MeGeneralMethod.IsForegroundRunning( getApplicationContext() ) )
		{
			android.os.Process.killProcess( android.os.Process.myPid() );
		}
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
		public TextView tvAppSize;
		public TextView TvAppInstallControl;
	}
	
	class InstallControlButClicklister implements OnClickListener
	{
		
		InstallApkItemEx CurItem = null;
		
		public InstallControlButClicklister(
				InstallApkItemEx CurItem )
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
			MELOG.v( "ME_RTFSC" , "====InstallControlButClicklister  onClick ===" );
			if( 1 == CurItem.getInstallState() )
			{
				dl_info info = MeapkDlMgr.GetSdkApkDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , CurItem.getPackageName() , true );
				if( info != null && info.IsDownloadSuccess() )
				{
					MyMethod.InstallApk( ApkMangerActivity.this , info.getFilePath() );
				}
			}
			else if( 2 == CurItem.getInstallState() )
			{
				StartActivityByPackageName( CurItem.getPackageName() , getApplicationContext() );
			}
		}
	}
	
	class InstallListViewAdapter extends BaseAdapter
	{
		
		List<InstallApkItemEx> InstallApkList = null;
		private LayoutInflater mInflater = null;
		
		public InstallListViewAdapter(
				List<InstallApkItemEx> InstallApkList ,
				Context context )
		{
			// TODO Auto-generated constructor stub
			this.InstallApkList = InstallApkList;
			this.mInflater = LayoutInflater.from( context );
		}
		
		public void Update(
				List<InstallApkItemEx> InstallApkList )
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
			InstallApkItemEx CurItem = InstallApkList.get( position );
			// 如果缓存convertView为空，则需要创建View
			if( convertView == null )
			{
				Holder = new InstallListViewHolder();
				// 根据自定义的Item布局加载布局
				convertView = mInflater.inflate( R.layout.cool_ml_manager_install_listview , null );
				Holder.ivAppIcon = (ImageView)convertView.findViewById( R.id.cool_ml_manager_appIco1 );
				Holder.tvAppName = (TextView)convertView.findViewById( R.id.cool_ml_manager_appName1 );
				Holder.tvAppVersion = (TextView)convertView.findViewById( R.id.cool_ml_manager_appVersion1 );
				Holder.tvAppSize = (TextView)convertView.findViewById( R.id.cool_ml_manager_appSize1 );
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
			Holder.tvAppSize.setText( CurItem.getAppSize() / 1024 + "K" );
			// int InstallState; /* 0:初始化状态 1:安装 2:启动 */
			if( 1 == CurItem.getInstallState() )
			{
				Holder.TvAppInstallControl.setBackgroundResource( R.drawable.cool_ml_icon_btn_list_install );
			}
			else
			{
				Holder.TvAppInstallControl.setBackgroundResource( R.drawable.cool_ml_icon_btn_list_run );
			}
			return convertView;
		}
	}
	
	class DownloadListViewAdapter extends BaseAdapter
	{
		
		List<DownloadApkItem> DownloadApkList = null;
		private LayoutInflater mInflater = null;
		
		public DownloadListViewAdapter(
				List<DownloadApkItem> DownloadApkList ,
				Context context )
		{
			// TODO Auto-generated constructor stub
			this.DownloadApkList = DownloadApkList;
			this.mInflater = LayoutInflater.from( context );
		}
		
		public void Update(
				List<DownloadApkItem> DownloadApkList )
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
			DownloadApkItem CurItem = DownloadApkList.get( position );
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
			Holder.tvAppName.setText( CurItem.getAppName() );
			int press = (int)( (float)CurItem.getCurSize() * 100 / (float)CurItem.getTotalSize() );
			Holder.progressBarApp.setProgress( press );
			Holder.tvAppSize.setText( (int)( CurItem.getCurSize() >> 10 ) + "K/" + (int)( CurItem.getTotalSize() >> 10 ) + "k" );
			// DownLoadState 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
			// icon_btn_list_pause icon_btn_list_install icon_btn_list_download
			if( 2 == CurItem.getDownLoadState() )
			{
				Holder.TvAppDownloadControl.setBackgroundResource( R.drawable.cool_ml_icon_btn_list_download );
			}
			else if( 1 == CurItem.getDownLoadState() )
			{
				Holder.TvAppDownloadControl.setBackgroundResource( R.drawable.cool_ml_icon_btn_list_waiting_download );
			}
			else
			{
				Holder.TvAppDownloadControl.setBackgroundResource( R.drawable.cool_ml_icon_btn_list_pause );
			}
			return convertView;
		}
	}
	
	class DownloadControlButClicklister implements OnClickListener
	{
		
		DownloadApkItem CurItem = null;
		
		public DownloadControlButClicklister(
				DownloadApkItem CurItem )
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
			MELOG.v( "ME_RTFSC" , "====DownloadControlButClicklister  onClick  ===" + CurItem.getDownLoadState() + CurItem.getPackageName() );
			//首先刷新状态，再调用下载（停止）函数进行下载（停止）
			Message DownloadControlButClickMsg = new Message();
			// 4 -- 开始或者暂停下载
			DownloadControlButClickMsg.what = 4;
			DownloadControlButClickMsg.obj = CurItem;
			DownloadUpdateHander.sendMessage( DownloadControlButClickMsg );
			// DownloadState 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
			if( 0 == CurItem.getDownLoadState() )
			{
				MeapkDlMgr.ReStartDownload( MeApkDLShowType.DownloadManagerActivity , CurItem.getPackageName() , CurItem.getDownloadCallback() );
			}
			else
			{
				//apkDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , CurItem.getPackageName() );
				MeapkDlMgr.StopDownload( MeApkDLShowType.DownloadManagerActivity , CurItem.getPackageName() );
			}
			//			try
			//			{
			//				JSClass.JSClass.invokeJSMethod( "reloadDownstate" , CurItem.getPackageName() );
			//			}
			//			catch( Exception e )
			//			{
			//				// TODO: handle exception
			//				MELOG.e( "ME_RTFSC" , "DownloadUpdateHander 4 Exception:" + e.toString() );
			//			}
		}
	}
	
	class DownloadListViewHolder
	{
		
		public ImageView ivAppIcon;
		public TextView tvAppName;
		public ProgressBar progressBarApp;
		public TextView tvAppSize;
		public TextView TvAppDownloadControl;
	}
	
	public enum DownloadType
	{
		ON_SUCESS , ON_FAILED , ON_DOING ,
	}
	
	class DownLoadCallBackMsgData
	{
		
		DownloadApkItem CurItem;
		dl_info DlInfo;
		
		public DownLoadCallBackMsgData(
				DownloadApkItem CurItem ,
				dl_info DlInfo )
		{
			// TODO Auto-generated constructor stub
			this.CurItem = CurItem;
			this.DlInfo = DlInfo;
		}
		
		public DownloadApkItem getCurItem()
		{
			return CurItem;
		}
		
		public dl_info getDlInfo()
		{
			return DlInfo;
		}
	}
	
	class DownLoadCallBack extends MeApkDownloadCallBack
	{
		
		private DownloadApkItem DownLoadCallBack;
		DownloadApkItem CurItem;
		
		public DownLoadCallBack(
				MeApkDLShowType showType ,
				DownloadApkItem CurItem )
		{
			// TODO Auto-generated constructor stub
			super( showType );
			this.CurItem = CurItem;
		}
		
		@Override
		void onDoing(
				String PackageName ,
				dl_info info )
		{
			// TODO Auto-generated method stub
			//MELOG.v( "ME_RTFSC" , "DownLoadCallBack:onDoing" );
			DownLoadCallBackMsgData msgData = new DownLoadCallBackMsgData( CurItem , info );
			Message DownloadOnDoingMsg = new Message();
			DownloadOnDoingMsg.what = 1;
			DownloadOnDoingMsg.obj = msgData;
			DownloadUpdateHander.sendMessage( DownloadOnDoingMsg );
		}
		
		@Override
		void onSuccess(
				String PackageName ,
				dl_info info )
		{
			// TODO Auto-generated method stub
			MELOG.v( "ME_RTFSC" , "APK  onSuccess  name:" + CurItem.getPackageName() );
			Message DownloadOnDoingMsg = new Message();
			DownloadOnDoingMsg.what = 2;
			DownloadOnDoingMsg.obj = CurItem;
			DownloadUpdateHander.sendMessage( DownloadOnDoingMsg );
		}
		
		@Override
		void onFail(
				String PackageName ,
				dl_info info )
		{
			// TODO Auto-generated method stub
			MELOG.v( "ME_RTFSC" , "APK  onFail  name:" + CurItem.getPackageName() );
			Message DownloadOnDoingMsg = new Message();
			DownloadOnDoingMsg.what = 3;
			DownloadOnDoingMsg.obj = CurItem;
			DownloadUpdateHander.sendMessage( DownloadOnDoingMsg );
		}
		
		@Override
		void onRestart(
				String PackageName )
		{
			// TODO Auto-generated method stub
			MELOG.v( "ME_RTFSC" , "ApkMangerActivity  callback :onRestart" );
			for( int i = 0 ; i < DownloadApkList.size() ; i++ )
			{
				if( DownloadApkList.get( i ).getAppName().equals( PackageName ) )
				{
					DownloadApkItem curItem = DownloadApkList.get( i );
					MeapkDlMgr.AddCallback( MeApkDLShowType.DownloadManagerActivity , PackageName , curItem.getDownloadCallback() );
					Message DownloadControlButClickMsg = new Message();
					DownloadControlButClickMsg.what = 4;
					DownloadControlButClickMsg.obj = curItem;
					DownloadUpdateHander.sendMessage( DownloadControlButClickMsg );
				}
			}
		}
		
		@Override
		void onStop(
				String PackageName )
		{
			// TODO Auto-generated method stub
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
}
