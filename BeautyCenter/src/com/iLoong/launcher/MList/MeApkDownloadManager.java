package com.iLoong.launcher.MList;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
import cool.sdk.MicroEntry.MicroEntry;
import cool.sdk.MicroEntry.MicroEntryHelper;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;
import cool.sdk.download.manager.dl_task;


public class MeApkDownloadManager
{
	
	private CoolDLMgr iconDlMgr = null;
	private CoolDLMgr apkDlMgr = null;
	private int entryId = 0;
	Map<String , MeApkDownloadTask> MeApkDownloadTaskMap = new HashMap<String , MeApkDownloadManager.MeApkDownloadTask>();
	static Map<MeApkDLShowType , MeActiveCallback> MeAddActiveCallBackMap = new HashMap<MeApkDLShowType , MeActiveCallback>();
	Context context = null;
	String moudleName = null;
	
	public MeApkDownloadManager(
			Context context ,
			String moudleName ,
			int entryId )
	{
		// TODO Auto-generated constructor stub
		this.context = context;
		this.entryId = entryId;
		this.moudleName = moudleName;
		apkDlMgr = MicroEntry.CoolDLMgr( context , moudleName , entryId );
		apkDlMgr.dl_mgr.setMaxConnectionCount( 2 );
		apkDlMgr.dl_mgr.setDownloadPath( apkDlMgr.getExternalPath() );
		apkDlMgr.setCheckPathEverytime( false );
		//apkDlMgr.dl_mgr.setDataBasePath( apkDlMgr.getExternalPath() );
		apkDlMgr.dl_mgr.setUseHandler( true );
		iconDlMgr = MicroEntry.CoolDLMgr( context , "icon" , entryId );
		iconDlMgr.dl_mgr.setMaxConnectionCount( 2 );
	}
	
	public CoolDLMgr GetSdkApkDlMgr()
	{
		return apkDlMgr;
	}
	
	public CoolDLMgr GetSdkIconMgr()
	{
		return iconDlMgr;
	}
	
	public synchronized dl_info GetInfoByPkgName(
			String PkgName )
	{
		// TODO Auto-generated method stub
		return apkDlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , PkgName );
	}
	
	public void StartDownload(
			MeApkDLShowType ShowType ,
			String PkgName ,
			String appName ,
			String size ,
			String versionName ,
			MeApkDownloadCallBack CallBack )
	{
		//下载任务的开始只能够从Web的主页面开始
		MeApkDownloadTask CurTask = new MeApkDownloadTask( ShowType , PkgName , CallBack , appName , size , versionName );
		MeApkDownloadTaskMap.put( PkgName , CurTask );
		MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkDlStart( entryId , moudleName , PkgName );
		for( MeApkDLShowType type : MeAddActiveCallBackMap.keySet() )
		{
			if( !type.equals( ShowType ) )
			{
				MeAddActiveCallBackMap.get( type ).NoifySatrtAction( PkgName );
			}
		}
		//CurTask.CurCallback.onstart( ShowType , PkgName );
	}
	
	public void ReStartDownload(
			MeApkDLShowType ShowType ,
			String PkgName ,
			MeApkDownloadCallBack CallBack )
	{
		MeApkDownloadTask MECurDLTask = MeApkDownloadTaskMap.get( PkgName );
		if( null != MECurDLTask && null != MECurDLTask.CurCallback && null != MECurDLTask.CurSDKDLTask )
		{
			MECurDLTask.MeApkReStartDownload( ShowType , PkgName , CallBack );
		}
		else
		{
			MECurDLTask = new MeApkDownloadTask( ShowType , PkgName , CallBack );
			MeApkDownloadTaskMap.put( PkgName , MECurDLTask );
			MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkDlStart( entryId , moudleName , PkgName );
			for( MeApkDLShowType type : MeAddActiveCallBackMap.keySet() )
			{
				if( !type.equals( ShowType ) )
				{
					MeAddActiveCallBackMap.get( type ).NoifySatrtAction( PkgName );
				}
			}
			//MECurDLTask.CurCallback.onstart( ShowType , PkgName );
		}
	}
	
	public void AddCallback(
			MeApkDLShowType ShowType ,
			String PkgName ,
			MeApkDownloadCallBack CallBack )
	{
		MeApkDownloadTask CurTask = MeApkDownloadTaskMap.get( PkgName );
		CurTask.SetTaskCallback( ShowType , CallBack );
	}
	
	public void StopDownload(
			MeApkDLShowType ShowType ,
			String PkgName )
	{
		MeApkDownloadTask CurTask = MeApkDownloadTaskMap.get( PkgName );
		CurTask.MeApkStopDownload( ShowType , PkgName );
	}
	
	public void DelDownload(
			MeApkDLShowType ShowType ,
			String PkgName )
	{
		//先处理notificatio的事物，消除与之有关的notify，由于用到下载管理数据，所以必须在ResDownloadStop之前调用
		//先暂停
		apkDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , PkgName , false );
		MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkDlDel( entryId , moudleName , PkgName );
		//再删除
		apkDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , PkgName , true );
		for( MeApkDLShowType type : MeAddActiveCallBackMap.keySet() )
		{
			if( !type.equals( ShowType ) )
			{
				MELOG.v( "ME_RTFSC" , "MeApkDownloadManager DelDownload type = " + type );
				MeAddActiveCallBackMap.get( type ).NotifyDelAction( PkgName );
			}
		}
	}
	
	public void ApkInstalled(
			String pkgName )
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "MeApkDownloadManager  ApkInstalled" );
		dl_info info = apkDlMgr.ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
		if( null != info && true == info.IsDownloadSuccess() )
		{
			for( MeApkDLShowType type : MeAddActiveCallBackMap.keySet() )
			{
				MELOG.v( "ME_RTFSC" , "MeApkDownloadManager ApkInstalled type = " + type );
				MeAddActiveCallBackMap.get( type ).NotifyInstallSucessAction( pkgName );
			}
			MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkInstalled( entryId , moudleName , pkgName );
		}
		else if( null == info )
		{
			for( MeApkDLShowType type : MeAddActiveCallBackMap.keySet() )
			{
				MELOG.v( "ME_RTFSC" , "MeApkDownloadManager ApkInstalled type = " + type );
				MeAddActiveCallBackMap.get( type ).NotifyInstallSucessAction( pkgName );
			}
		}
	}
	
	public void ApkUninstall(
			String pkgName )
	{
		// TODO Auto-generated method stub
		for( MeApkDLShowType type : MeAddActiveCallBackMap.keySet() )
		{
			MELOG.v( "ME_RTFSC" , "MeApkDownloadManager DelDownload type = " + type );
			MeAddActiveCallBackMap.get( type ).NotifyUninstallApkAction( pkgName );
		}
	}
	
	public int getVersionCode(
			String pageName ,
			Context context )
	{
		try
		{
			return context.getPackageManager().getPackageInfo( pageName , 0 ).versionCode;
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			MELOG.v( "ME_RTFSC" , "getVersionCode Error:" + e.toString() );
			return 0;
		}
	}
	
	public synchronized int GetUninstallApkCount()
	{
		int UninstallApkCount = 0;
		List<dl_info> ApkTaskList = null;
		try
		{
			ApkTaskList = apkDlMgr.ResGetTaskList( CoolDLResType.RES_TYPE_APK );
		}
		catch( Exception e )
		{
			// TODO: handle exception
		}
		if( null == ApkTaskList || 0 == ApkTaskList.size() )
		{
			return 0;
		}
		for( dl_info info : ApkTaskList )
		{
			//拿不到包名的不算
			if( null == (String)info.getValue( "r4" ) && null == (String)info.getValue( "p2" ) )
			{
				continue;
			}
			MELOG.v( "ME_RTFSC" , "---pkgname:" + (String)info.getValue( "r4" ) );
			//没有下载完成   或者 下载完成但是没有安装完成
			if( !info.IsDownloadSuccess() )
			{
				UninstallApkCount++;
			}
			else
			{
				int CurVersionCode = (Integer)info.getValue( "versionCode" );
				int versionCode = getVersionCode( (String)info.getValue( "r4" ) , context );
				//MELOG.v( "ME_RTFSC" , "----CurVersionCode:"  +CurVersionCode + " ----versionCode:" + versionCode);
				if( CurVersionCode != versionCode )
				{
					UninstallApkCount++;
				}
			}
		}
		return UninstallApkCount;
	}
	
	public synchronized int GetDownLoadingApkCount()
	{
		// TODO Auto-generated method stub
		int DownLoadingApkCount = 0;
		List<dl_info> ApkTaskList = null;
		try
		{
			ApkTaskList = apkDlMgr.ResGetTaskList( CoolDLResType.RES_TYPE_APK );
		}
		catch( Exception e )
		{
			// TODO: handle exception
		}
		if( null == ApkTaskList || 0 == ApkTaskList.size() )
		{
			return 0;
		}
		for( dl_info info : ApkTaskList )
		{
			// DownloadState 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
			if( 0 != info.getDownloadState() )
			{
				if( null == (String)info.getValue( "r4" ) && null == (String)info.getValue( "p2" ) )
				{
					continue;
				}
				DownLoadingApkCount++;
			}
		}
		return DownLoadingApkCount;
	}
	
	class MeApkDownloadTask
	{
		
		dl_task CurSDKDLTask = null;
		MeCoolDLCallback CurCallback = null;
		
		public MeApkDownloadTask(
				MeApkDLShowType ShowType ,
				String PackageName ,
				MeApkDownloadCallBack CallBack ,
				String appName ,
				String size ,
				String versionName )
		{
			// TODO Auto-generated constructor stub
			MELOG.v( "ME_RTFSC" , "MeApkDownloadTask:MeApkDownloadTask  WebStart" );
			CurCallback = new MeCoolDLCallback( context , moudleName , entryId );
			CurCallback.AddMECallBack( ShowType , CallBack );
			CurSDKDLTask = apkDlMgr.ResDownloadNewTask( CoolDLResType.RES_TYPE_APK , PackageName , CurCallback );
			CurSDKDLTask.setValue( "p101" , appName );
			CurSDKDLTask.setValue( "p102" , size );
			CurSDKDLTask.setValue( "p103" , versionName );
			//Environment.getExternalStorageState()
			if( !Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ) || 2 == apkDlMgr.ResDownloadStart( CurSDKDLTask ) )
			{
				CurCallback.onFail( CoolDLResType.RES_TYPE_APK , PackageName , null );
			}
		}
		
		public MeApkDownloadTask(
				MeApkDLShowType ShowType ,
				String PackageName ,
				MeApkDownloadCallBack CallBack )
		{
			// TODO Auto-generated constructor stub
			MELOG.v( "ME_RTFSC" , "MeApkDownloadTask:MeApkDownloadTask " );
			CurCallback = new MeCoolDLCallback( context , moudleName , entryId );
			CurCallback.AddMECallBack( ShowType , CallBack );
			CurSDKDLTask = apkDlMgr.ResDownloadNewTask( CoolDLResType.RES_TYPE_APK , PackageName , CurCallback );
			if( !Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ) || 2 == apkDlMgr.ResDownloadStart( CurSDKDLTask ) )
			{
				CurCallback.onFail( CoolDLResType.RES_TYPE_APK , PackageName , null );
			}
		}
		
		public void MeApkReStartDownload(
				MeApkDLShowType ShowType ,
				String PackageName ,
				MeApkDownloadCallBack CallBack )
		{
			MELOG.v( "ME_RTFSC" , "MeApkDownloadTask:MeApkReStartDownload" );
			// TODO Auto-generated method stub
			CurCallback.AddMECallBack( ShowType , CallBack );
			if( !Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED ) || 2 == apkDlMgr.ResDownloadStart( CoolDLResType.RES_TYPE_APK , PackageName , CurCallback ) )
			{
				CurCallback.onFail( CoolDLResType.RES_TYPE_APK , PackageName , null );
			}
			else
			{
				CurCallback.onRestart( ShowType , PackageName );
			}
		};
		
		public void SetTaskCallback(
				MeApkDLShowType ShowType ,
				MeApkDownloadCallBack CallBack )
		{
			MELOG.v( "ME_RTFSC" , "MeApkDownloadTask:SetTaskCallback" );
			CurCallback.AddMECallBack( ShowType , CallBack );
		}
		
		public void MeApkStopDownload(
				MeApkDLShowType ShowType ,
				String PackageName )
		{
			MELOG.v( "ME_RTFSC" , "MeApkDownloadTask:MeApkStopDownload" );
			apkDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , PackageName );
			CurCallback.onStop( ShowType , PackageName );
		}
		//		public void MeApkDelDownload(
		//				MeApkDLShowType ShowType ,
		//				String PackageName )
		//		{
		//			MELOG.v( "ME_RTFSC" , "MeApkDownloadTask:MeApkDelDownload" );
		//			apkDlMgr.ResDownloadStop( CoolDLResType.RES_TYPE_APK , PackageName , true );
		//			//CurCallback.ondelete( ShowType , PackageName );
		//		}
		//		
		//		public void MeApkInstalled(String PackageName)
		//		{
		//			// TODO Auto-generated method stub
		//			MELOG.v( "ME_RTFSC" , "MeApkDownloadTask:MeApkInstalled" );
		//			//CurCallback.onApkInstalled( PackageName );
		//		}
		//		
		//		public void MeApkRemoved(String PackageName)
		//		{
		//			// TODO Auto-generated method stub
		//			//CurCallback.onApkRemoved( PackageName );
		//		}
	}
}
