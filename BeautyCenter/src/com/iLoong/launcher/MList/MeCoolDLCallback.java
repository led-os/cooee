package com.iLoong.launcher.MList;


import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import cool.sdk.MicroEntry.MicroEntryHelper;
import cool.sdk.common.MyMethod;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_info;


class MeCoolDLCallback implements CoolDLCallback
{
	
	Map<MeApkDLShowType , MeApkDownloadCallBack> MeApkDlCallbackMap = new HashMap<MeApkDLShowType , MeApkDownloadCallBack>();
	Context context;
	int entryID;
	String apkMoudleName;
	
	//static Map<MeApkDLShowType , MeActiveCallback> MeAddActiveCallBackMap = new HashMap<MeApkDLShowType , MeActiveCallback>();
	public MeCoolDLCallback(
			Context context ,
			String apkMoudleName ,
			int entryID )
	{
		// TODO Auto-generated constructor stub
		this.context = context;
		this.entryID = entryID;
		this.apkMoudleName = apkMoudleName;
	}
	
	public void AddMECallBack(
			MeApkDLShowType Type ,
			MeApkDownloadCallBack CurCallBack )
	{
		// TODO Auto-generated method stub
		if( null != CurCallBack )
		{
			MeApkDlCallbackMap.put( Type , CurCallBack );
		}
	}
	
	@Override
	public void onDoing(
			CoolDLResType type ,
			String name ,
			dl_info info )
	{
		// TODO Auto-generated method stub
		//MELOG.v( "ME_RTFSC" , "MeCoolDLCallback:onDoing" );
		for( MeApkDLShowType KeyShowType : MeApkDlCallbackMap.keySet() )
		{
			MeApkDlCallbackMap.get( KeyShowType ).onDoing( name , info );
		}
	}
	
	@Override
	public void onSuccess(
			CoolDLResType type ,
			String name ,
			dl_info info )
	{
		// TODO Auto-generated method stub
		MicroEntryHelper.getInstance( context ).setValue( name + MeServiceType.MEApkOnSucess + entryID , entryID );
		MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkDlSucess( entryID , apkMoudleName , name , info );
		for( MeApkDLShowType KeyShowType : MeApkDlCallbackMap.keySet() )
		{
			MeApkDlCallbackMap.get( KeyShowType ).onSuccess( name , info );
		}
		MyMethod.InstallApk( context , info.getFilePath() );
		if( !MeGeneralMethod.IsForegroundRunning( context ) && !MeGeneralMethod.IsDownloadTaskRunning( context ) )
		{
			android.os.Process.killProcess( android.os.Process.myPid() );
		}
	}
	
	@Override
	public void onFail(
			CoolDLResType type ,
			String pkgName ,
			dl_info info )
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "MeCoolDLCallback:onFail" );
		MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkDlFailed( entryID , apkMoudleName , pkgName , info );
		for( MeApkDLShowType KeyShowType : MeApkDlCallbackMap.keySet() )
		{
			MeApkDlCallbackMap.get( KeyShowType ).onFail( pkgName , info );
		}
		if( !MeGeneralMethod.IsForegroundRunning( context ) && !MeGeneralMethod.IsDownloadTaskRunning( context ) )
		{
			android.os.Process.killProcess( android.os.Process.myPid() );
		}
	}
	
	public void onstart(
			MeApkDLShowType KeyShowType ,
			String pkgName )
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "MeCoolDLCallback:onRestart  Type:" + KeyShowType );
		MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkDlStart( entryID , apkMoudleName , pkgName );
		for( MeApkDLShowType type : MeApkDownloadManager.MeAddActiveCallBackMap.keySet() )
		{
			if( !type.equals( KeyShowType ) )
			{
				MeApkDownloadManager.MeAddActiveCallBackMap.get( type ).NoifySatrtAction( pkgName );
			}
		}
	}
	
	public void onRestart(
			MeApkDLShowType curType ,
			String pkgName )
	{
		MELOG.v( "ME_RTFSC" , "MeCoolDLCallback:onRestart  Type:" + curType );
		MELOG.v( "ME_RTFSC" , "MeAddActiveCallBackMap:" + MeApkDownloadManager.MeAddActiveCallBackMap );
		MELOG.v( "ME_RTFSC" , "MeApkDlCallbackMap:" + MeApkDlCallbackMap );
		for( MeApkDLShowType KeyShowType : MeApkDownloadManager.MeAddActiveCallBackMap.keySet() )
		{
			for( MeApkDLShowType KeyShowTypesub : MeApkDlCallbackMap.keySet() )
			{
				if( KeyShowType.equals( curType ) || KeyShowType.equals( KeyShowTypesub ) )
				{
					continue;
				}
			}
			MeApkDownloadManager.MeAddActiveCallBackMap.get( KeyShowType ).NoifySatrtAction( pkgName );
		}
		for( MeApkDLShowType KeyShowType : MeApkDlCallbackMap.keySet() )
		{
			if( KeyShowType != curType )
			{
				MeApkDlCallbackMap.get( KeyShowType ).onRestart( pkgName );
			}
		}
		MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkDlStart( entryID , apkMoudleName , pkgName );
	}
	
	public void onStop(
			MeApkDLShowType Type ,
			String pkgName )
	{
		MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkDlStop( entryID , apkMoudleName , pkgName );
		for( MeApkDLShowType KeyShowType : MeApkDlCallbackMap.keySet() )
		{
			if( KeyShowType != Type )
			{
				MeApkDlCallbackMap.get( KeyShowType ).onStop( pkgName );
			}
		}
	}
	//	public void ondelete(
	//			MeApkDLShowType Type ,
	//			String pkgName )
	//	{
	//		MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkDlDel( entryID , apkMoudleName , pkgName );
	//		
	//		// TODO Auto-generated method stub
	////		for( MeApkDLShowType KeyShowType : MeApkDlCallbackMap.keySet() )
	////		{
	////			if( KeyShowType != Type )
	////			{
	////				MeApkDlCallbackMap.get( KeyShowType ).ondelete( pkgName );
	////			}
	////		}
	//	}
	//	
	//	public void onApkInstalled(
	//			String pkgName )
	//	{
	//		// TODO Auto-generated method stub
	//		MELOG.v( "ME_RTFSC" , "MeCoolDLCallback:onApkInstalled" );
	//		MeApkDlNotifyManager.getInstance( context.getApplicationContext() ).onMeApkInstalled( entryID , apkMoudleName , pkgName );
	////		for( MeApkDLShowType KeyShowType : MeApkDlCallbackMap.keySet() )
	////		{
	////			MeApkDlCallbackMap.get( KeyShowType ).onInstalled( pkgName );
	////		}
	//	}
	//	
	//	public void onApkRemoved(
	//			String pkgName )
	//	{
	//		// TODO Auto-generated method stub
	////		for( MeApkDLShowType KeyShowType : MeApkDlCallbackMap.keySet() )
	////		{
	////			MeApkDlCallbackMap.get( KeyShowType ).onRemoved( pkgName );
	////		}
	//	}
}
