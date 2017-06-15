package com.iLoong.launcher.MList;


import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;


public class MEServiceActivity extends Activity
{
	
	boolean[] visible;
	int index = -1;
	int MEShowType = 0;
	AlertDialog ad = null;
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		MELOG.v( "ME_RTFSC" , "==== MEServiceActivity  onCreate" );
		Intent intent = getIntent();
		if( null != intent )
		{
			MeServiceType type = (MeServiceType)intent.getSerializableExtra( "MeServiceType" );
			MELOG.v( "ME_RTFSC" , "==== MeServiceType  type:" + type );
			switch( type )
			{
			//				case MEShowType:
			//					onMEShowType( intent );
			//					break;
				case MEApkOnNotifyReStart:
					onMEApkOnNotifyReStart( intent );
					break;
				case MEApkOnSucess:
					onMEApkOnSucess( intent );
					break;
				case MEApkOnDownloading:
					onMEApkOnDownloading( intent );
					break;
				case MePushShowType:
					onMePushShowType( intent );
					break;
				case MeApkOnPkgInstalled:
					onMeApkOnPkgInstalled( intent );
					break;
				case MeApkOnPkgUninstall:
					onMeApkOnPkgUninstall( intent );
					break;
				default:
					finish();
					break;
			}
		}
		else
		{
			finish();
		}
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		//当没有正在运行的前台和正在下载项，可以结束当前进程
		if( !MeGeneralMethod.IsDownloadTaskRunning( getApplicationContext() ) && !MeGeneralMethod.IsForegroundRunning( getApplicationContext() ) )
		{
			MELOG.v( "ME_RTFSC" , "Kill Curret process  MEServiceActivity onDestroy " );
			android.os.Process.killProcess( android.os.Process.myPid() );
		}
	}
	
	public void onMeApkOnPkgUninstall(
			Intent intent )
	{
		MELOG.v( "ME_RTFSC" , "==== onMeApkOnPkgUninstall  " );
		//MePkgRemoveIntent.putExtra( "PkgName" , pkgName );
		String pkgName = intent.getStringExtra( "PkgName" );
		if( null != pkgName && !pkgName.isEmpty() )
		{
			Map<String , MeApkDownloadManager> mgrMap = MeApkDlMgrBuilder.GetAllMeApkDownloadManager();
			MeApkDlNotifyManager.getInstance( getApplicationContext() ).onMeApkUninstallCanel( pkgName );
			if( null != mgrMap && !mgrMap.isEmpty() )
			{
				for( String curMgrID : mgrMap.keySet() )
				{
					MELOG.v( "ME_RTFSC" , "ACTION_PACKAGE_REMOVED:" + curMgrID );
					mgrMap.get( curMgrID ).ApkUninstall( pkgName );
				}
			}
		}
		finish();
	}
	
	public void onMeApkOnPkgInstalled(
			Intent intent )
	{
		MELOG.v( "ME_RTFSC" , "==== onMeApkOnPkgAdded  " );
		MELOG.v( "ME_RTFSC" , "MeGeneralMethod.IsForegroundRunning = " + MeGeneralMethod.IsForegroundRunning( getApplicationContext() ) );
		ArrayList<Integer> PkgAddedEntryIDList = intent.getIntegerArrayListExtra( "PkgAddedEntryIDList" );
		String pkgName = intent.getStringExtra( "PkgName" );
		Map<String , MeApkDownloadManager> mgrMap = MeApkDlMgrBuilder.GetAllMeApkDownloadManager();
		MELOG.v( "ME_RTFSC" , "pkgName = " + pkgName + ", PkgAddedEntryIDList" + PkgAddedEntryIDList );
		if( null != PkgAddedEntryIDList && !PkgAddedEntryIDList.isEmpty() && null != pkgName && !pkgName.isEmpty() )
		{
			MELOG.v( "ME_RTFSC" , "ACTION_PACKAGE_ADDED:mgrMap =" + mgrMap );
			if( null != mgrMap && !mgrMap.isEmpty() )
			{
				for( String curMgrID : mgrMap.keySet() )
				{
					MELOG.v( "ME_RTFSC" , "ACTION_PACKAGE_ADDED:" + curMgrID );
					mgrMap.get( curMgrID ).ApkInstalled( pkgName );
				}
			}
			else
			{
				for( int i = 0 ; i < PkgAddedEntryIDList.size() ; i++ )
				{
					MeApkDlMgrBuilder.Build( getApplicationContext() , "M" , PkgAddedEntryIDList.get( i ) ).ApkInstalled( pkgName );
				}
			}
		}
		else if( null != pkgName && !pkgName.isEmpty() && null != mgrMap && !mgrMap.isEmpty() )
		{
			for( String curMgrID : mgrMap.keySet() )
			{
				MELOG.v( "ME_RTFSC" , "ACTION_PACKAGE_ADDED:" + curMgrID );
				mgrMap.get( curMgrID ).ApkInstalled( pkgName );
			}
		}
		finish();
	}
	
	public void onMePushShowType(
			Intent intent )
	{
		MELOG.v( "ME_RTFSC" , "==== onMePushShowType  " );
		int PushID = intent.getIntExtra( "PUSH_ID" , 0 );
		int MicEnrtyID = intent.getIntExtra( "APP_ID" , 0 );
		String strAction = intent.getStringExtra( "Action" );
		String strActionDescription = intent.getStringExtra( "ActionDescription" );
		MELOG.v( "ME_RTFSC" , "onMePushShowType  strPushID:" + PushID + ", " + "strMicEnrtyID:" + MicEnrtyID + ", strAction:" + strAction + ", strActionDescription:" + strActionDescription );
		if( MicEnrtyID > 0 && null != strAction && null != strActionDescription )
		{
			MELOG.v( "ME_RTFSCX" , " ApkMangerActivity  onMePushShowType  ======    instance:" + ApkMangerActivity.instance + ",CurEntryID:" + ApkMangerActivity.CurEntryID );
			if( null != ApkMangerActivity.instance )
			{
				MELOG.v( "ME_RTFSC" , "finish ApkMangerActivity.instance.finish() " );
				ApkMangerActivity.instance.finish();
			}
			if( null != MeMainActivity.instance )
			{
				MELOG.v( "ME_RTFSC" , "finish MainActivity.instance.finish() " );
				MeMainActivity.instance.finish();
			}
			Intent ActivtyIntent = new Intent( getApplicationContext() , MeMainActivity.class );
			ActivtyIntent.putExtra( "APP_ID" , MicEnrtyID );
			ActivtyIntent.putExtra( "Action" , strAction );
			ActivtyIntent.putExtra( "ActionDescription" , strActionDescription );
			ActivtyIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			startActivity( ActivtyIntent );
		}
		//ActivtyIntent.putExtra( "PUSH_ID" , msg.getMsgId() );
		finish();
	}
	
	public void onMEApkOnDownloading(
			Intent intent )
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "==== onMEApkOnDownloading  " );
		int entryID = intent.getIntExtra( "entryId" , 0 );
		String moudleName = intent.getStringExtra( "moudleName" );
		MELOG.v( "ME_RTFSC" , "entryID:" + entryID + ", moudleName:" + moudleName );
		//所以微入口的下载管理器都是用ApkMangerActivity现实，当有两个和两个以上的正在下载的入口的时候，
		//这里需要先结束之前的下载/安装管理器，然后启动当前的下载/安装管理器
		if( ApkMangerActivity.CurEntryID != entryID && null != ApkMangerActivity.instance )
		{
			ApkMangerActivity.instance.finish();
		}
		Intent intenta = new Intent( this , ApkMangerActivity.class );
		intenta.addFlags( Intent.FLAG_ACTIVITY_NO_HISTORY );
		intenta.putExtra( "moudleName" , moudleName );
		intenta.putExtra( "entryId" , entryID );
		startActivity( intenta );
		finish();
	}
	
	public void onMEApkOnSucess(
			Intent intent )
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "==== onMEApkOnSucess  " );
		String moudleName = intent.getStringExtra( "moudleName" );
		String FilePath = intent.getStringExtra( "FilePath" );
		if( !moudleName.isEmpty() && !FilePath.isEmpty() )
		{
			Intent installIntent = new Intent( Intent.ACTION_VIEW );
			installIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			installIntent.addFlags( Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
			installIntent.setDataAndType( Uri.fromFile( new File( FilePath ) ) , "application/vnd.android.package-archive" );
			startActivity( installIntent );
		}
		finish();
	}
	
	public void onMEApkOnNotifyReStart(
			Intent intent )
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "MEServiceActivity:onMEApkOnNotifyReStart" );
		int entryID = intent.getIntExtra( "entryID" , -1 );
		String moudleName = intent.getStringExtra( "moudleName" );
		String PkgName = intent.getStringExtra( "PkgName" );
		MELOG.v( "ME_RTFSC" , "PkgName:" + PkgName + "entryID:" + entryID );
		if( -1 != entryID && !moudleName.isEmpty() && !PkgName.isEmpty() )
		{
			MeApkDownloadManager DlMgr = MeApkDlMgrBuilder.Build( getApplicationContext() , moudleName , entryID );
			DlMgr.ReStartDownload( MeApkDLShowType.Notification , PkgName , null );
		}
		finish();
	}
	
	//	public void onMEShowType(
	//			Intent intent )
	//	{
	//		visible = intent.getBooleanArrayExtra( "NOTIFY_ME_SHOW_ARRY" );
	//		index = intent.getIntExtra( "NOTIFY_ME_SHOW_ID" , -1 );
	//		MEShowType = intent.getIntExtra( "NOTIFY_ME_SHOW_TYPE" , -1 );
	//		MELOG.v( "ME_RTFSC" , "  index:" + index + "visible" + visible[0] + "," + visible[1] + "," + visible[2] + "," + visible[3] );
	//		if( -1 == index )
	//		{
	//			finish();
	//		}
	//		
	//		AlertDialog.Builder builder = new AlertDialog.Builder( getApplicationContext() );
	//		builder.setTitle( R.string.cool_ml_new_content );
	//		builder.setMessage( R.string.cool_ml_confirm_content );
	//		builder.setPositiveButton( R.string.cool_ml_confirm_ok , new android.content.DialogInterface.OnClickListener() {
	//			
	//			@Override
	//			public void onClick(
	//					DialogInterface arg0 ,
	//					int arg1 )
	//			{
	//				// TODO Auto-generated method stub
	//				MELOG.v( "ME_RTFSC" , " Confrim ME Show" );
	//				new Thread( new Runnable() {
	//					
	//					@Override
	//					public void run()
	//					{
	//						// TODO Auto-generated method stub
	//						MicroEntryHelper.getInstance( getApplicationContext() ).UpdateMeStateUserConfirm( visible , index );
	//					}
	//				} ).start();
	//				finish();
	//			}
	//		} );
	//		if( 0 == MEShowType )
	//		{
	//			builder.setNegativeButton( R.string.cool_ml_confirm_canel , new android.content.DialogInterface.OnClickListener() {
	//				
	//				@Override
	//				public void onClick(
	//						DialogInterface arg0 ,
	//						int arg1 )
	//				{
	//					// TODO Auto-generated method stub
	//					ad.cancel();
	//					finish();
	//				}
	//			} );
	//		}
	//		builder.setOnCancelListener( new OnCancelListener() {
	//			
	//			@Override
	//			public void onCancel(
	//					DialogInterface arg0 )
	//			{
	//				// TODO Auto-generated method stub
	//				MELOG.v( "ME_RTFSC" , " Canel ME Show" );
	//				finish();
	//			}
	//		} );
	//		ad = builder.create();
	//		ad.getWindow().setType( WindowManager.LayoutParams.TYPE_SYSTEM_ALERT );
	//		ad.setCanceledOnTouchOutside( false ); //点击外面区域不会让dialog消失  
	//		ad.show();
	//	}
	@Override
	public boolean onKeyDown(
			int keyCode ,
			KeyEvent event )
	{
		// TODO Auto-generated method stub
		return false;
	}
}
