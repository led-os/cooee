package com.iLoong.launcher.MList;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.iLoong.base.themebox.R;

import cool.sdk.MicroEntry.MicroEntryHelper;
import cool.sdk.common.MyMethod;
import cool.sdk.download.CoolDLCallback;
import cool.sdk.download.CoolDLResType;
import cool.sdk.download.manager.dl_cb;
import cool.sdk.download.manager.dl_info;
import cool.sdk.download.manager.dl_result;


public class JSClass
{
	
	//public static JSClass JSClass;
	WebView webView;
	Context context;
	MeApkDownloadManager MeApkDlMgr = null;
	//CoolDLMgr iconDlMgr;
	String failingUrl;
	static Map<String , PackageInfo> infoMap = new ConcurrentHashMap<String , PackageInfo>();
	//PhoneInfo phoneInfo = new PhoneInfo();
	private ProgressDialog builderDlg;
	String moudleName;
	int entryId;
	Handler MainActivityHandler = null;
	MeApkDLShowType curShowType;
	
	public JSClass(
			WebView webView ,
			String moudleName ,
			int entryId ,
			Handler MainActivityHandler ,
			MeApkDLShowType curShowType )
	{
		this.context = webView.getContext();
		this.webView = webView;
		this.moudleName = moudleName;
		this.entryId = entryId;
		this.MainActivityHandler = MainActivityHandler;
		this.curShowType = curShowType;
		MeApkDlMgr = MeApkDlMgrBuilder.Build( context.getApplicationContext() , moudleName , entryId );
		initPackageInfo( context );
	}
	
	public void initPackageInfo(
			Context context )
	{
		List<PackageInfo> list = context.getPackageManager().getInstalledPackages( 0 );
		for( PackageInfo info : list )
		{
			infoMap.put( info.packageName , info );
		}
	}
	
	/*
	 * 根据pageName找出版本号
	 */
	public String getVersion(
			String pageName ,
			Context context )
	{
		try
		{
			PackageInfo info = context.getPackageManager().getPackageInfo( pageName , 0 );
			return info.versionName;
		}
		catch( NameNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * 1: install 0: remove
	 */
	public void appInstallInfoChange(
			Context context ,
			String packageName ,
			int installOrRemove )
	{
		try
		{
			if( installOrRemove == 0 )
			{
				infoMap.remove( packageName );
			}
			else
			{
				infoMap.put( packageName , context.getPackageManager().getPackageInfo( packageName , 0 ) );
			}
		}
		catch( Exception e )
		{
		}
	}
	
	public int checkAppIsInstall(
			String packageName )
	{
		if( infoMap.containsKey( packageName ) )
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	public PackageInfo getAppPackageInfo(
			String packageName )
	{
		return infoMap.get( packageName );
	}
	
	@JavascriptInterface
	public boolean invokeJSMethod(
			String methodName ,
			Object ... params )
	{
		try
		{
			//MELOG.v( "ME_RTFSC" , "invokeJSMethod:" + moudleName );
			StringBuilder sb = new StringBuilder();
			sb.append( "javascript:" );
			sb.append( methodName );
			sb.append( "(" );
			for( Object param : params )
			{
				if( param instanceof String )
				{
					sb.append( "'" );
					sb.append( param );
					sb.append( "'," );
				}
				else
				{
					sb.append( param );
					sb.append( "," );
				}
			}
			if( params.length > 0 )
			{
				sb.setLength( sb.length() - 1 );
			}
			sb.append( ")" );
			//	MELOG.v( "ME_RTFSC" , "invokeJSMethod:" + sb.toString() );
			webView.loadUrl( sb.toString() );
			return true;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			MELOG.e( "ME_RTFSC" , "invokeJSMethod Exp:" + e.toString() );
			return false;
		}
	}
	
	private int getVersionCode(
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
	
	// 获取APK是否安装
	@JavascriptInterface
	public int AppQueryState(
			String pkgName )
	{
		dl_info curDLInfo = MeApkDlMgr.GetSdkApkDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName );
		int res = MeApkDlMgr.GetSdkApkDlMgr().ResQueryState( CoolDLResType.RES_TYPE_APK , pkgName );
		//如果已安装完成
		if( res == 3 )
		{
			//如果有下载记录
			if( null != curDLInfo )
			{
				//有下载记录，但未下载完成
				if( !curDLInfo.IsDownloadSuccess() )
				{
					res = 1;
				}
				//有下载记录，下载完成，但未安装
				else if( curDLInfo.IsDownloadSuccess() )
				{
					int CurVersionCode = (Integer)curDLInfo.getValue( "versionCode" );
					if( CurVersionCode != getVersionCode( pkgName , context ) )
					{
						res = 2;
					}
				}
			}
		}
		return res;
	}
	
	@JavascriptInterface
	public void subWebviewBack()
	{
		MainActivityHandler.sendEmptyMessage( MeMainActivity.subWebViewBackSoftKey );
	}
	
	@JavascriptInterface
	public void OpenSubWebview(
			String subUrl )
	{
		// TODO Auto-generated method stub
		MELOG.v( "ME_RTFSC" , "subUrl:" + subUrl );
		//loadUrl(subUrl );
		Message msg = new Message();
		msg.what = MeMainActivity.openSubWebView;
		msg.obj = subUrl;
		MainActivityHandler.sendMessage( msg );
	}
	
	@JavascriptInterface
	public int DownLoadingApkCount()
	{
		//return MeApkDlMgr.GetDownLoadingApkCount();
		return MeApkDlMgr.GetUninstallApkCount();
	}
	
	@JavascriptInterface
	public void setDialog()
	{
		builderDlg = new ProgressDialog( context );
		//builder.setIcon( R.drawable.ic_launcher );
		builderDlg.setMessage( context.getString( R.string.cool_ml_loading_1 ) );
		builderDlg.setCanceledOnTouchOutside( false );
		//		builderDlg.setOnCancelListener( new OnCancelListener() {
		//			
		//			@Override
		//			public void onCancel(
		//					DialogInterface arg0 )
		//			{
		//				// TODO Auto-generated method stub
		//				MELOG.v( "ME_RTFSCX" , "OnCancelListener:" + curShowType + ",this:" + this + ",builder:" + builderDlg);
		//			}
		//		} );
		//		
		//	
		//		builderDlg.setOnDismissListener( new OnDismissListener() {
		//			
		//			@Override
		//			public void onDismiss(
		//					DialogInterface arg0 )
		//			{
		//				// TODO Auto-generated method stub
		//				MELOG.v( "ME_RTFSCX" , "setOnDismissListener:" + curShowType + ",this:" + this +" ,builder:" + builderDlg);
		//			}
		//		} );
		builderDlg.show();
		MELOG.v( "ME_RTFSC" , "setDialog:" + curShowType );
	}
	
	@JavascriptInterface
	public void canelDialog()
	{
		MELOG.v( "ME_RTFSC" , "canelDialog:" + curShowType );
		if( builderDlg != null )
		{
			builderDlg.cancel();
			builderDlg = null;
		}
	}
	
	public boolean IsForegroundRunning(
			Context context )
	{
		ActivityManager mActivityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
		List<ActivityManager.RunningAppProcessInfo> mRunningService = mActivityManager.getRunningAppProcesses();
		for( ActivityManager.RunningAppProcessInfo amService : mRunningService )
		{
			if( amService.pid == android.os.Process.myPid() )
			{
				return amService.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
			}
		}
		return false;
	}
	
	MeApkDownloadCallBack WebMainApkDownloadCallBack = new MeApkDownloadCallBack( curShowType ) {
		
		@Override
		void onSuccess(
				String PackageName ,
				dl_info info )
		{
			// TODO Auto-generated method stub
			MELOG.v( "ME_RTFSC" , "webPage  onSuccess" );
			invokeJSMethod( "DownloadSuccess" , PackageName );
			//			if( !IsForegroundRunning( context ) )
			//			{
			//				if( MicroEntryHelper.shouldExit( context ) )
			//				{
			//					//AppInstall( PackageName );
			//					android.os.Process.killProcess( android.os.Process.myPid() );
			//					return;
			//				}
			//			}
			//AppInstall( PackageName );
		}
		
		@Override
		void onFail(
				String PackageName ,
				dl_info info )
		{
			// TODO Auto-generated method stub
			MELOG.v( "ME_RTFSC" , "webPage  onFail" );
			invokeJSMethod( "DownloadFail" , PackageName );
			//			if( !IsForegroundRunning( context ) )
			//			{
			//				if( MicroEntryHelper.shouldExit( context ) )
			//				{
			//					android.os.Process.killProcess( android.os.Process.myPid() );
			//				}
			//			}
		}
		
		@Override
		void onDoing(
				String PackageName ,
				dl_info info )
		{
			// TODO Auto-generated method stub
			//MELOG.v( "ME_RTFSC" , "Webview:onDoing"  + curShowType);
			invokeJSMethod( "DownloadProgress" , PackageName , info.getCurBytes() , info.getTotalBytes() );
		}
		
		@Override
		void onStop(
				String PackageName )
		{
			// TODO Auto-generated method stub
			invokeJSMethod( "reloadDownstate" , PackageName );
		}
		
		@Override
		void onRestart(
				String PackageName )
		{
			// TODO Auto-generated method stub
			MELOG.v( "ME_RTFSC" , "Webview :onRestart" + curShowType );
			invokeJSMethod( "reloadDownstate" , PackageName );
		}
	};
	
	@JavascriptInterface
	public int DownloadApk(
			final String pkgName ,
			final String json )
	{
		MELOG.v( "ME_RTFSC" , "=============== DownloadApk =================== " );
		Thread th = new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				MeApkDlMgr.GetSdkIconMgr().IconDownload( pkgName , new CoolDLCallback() {
					
					@Override
					public void onSuccess(
							CoolDLResType arg0 ,
							String arg1 ,
							dl_info arg2 )
					{
						// TODO Auto-generated method stub
					}
					
					@Override
					public void onFail(
							CoolDLResType arg0 ,
							String arg1 ,
							dl_info arg2 )
					{
						// TODO Auto-generated method stub
					}
					
					@Override
					public void onDoing(
							CoolDLResType arg0 ,
							String arg1 ,
							dl_info arg2 )
					{
						// TODO Auto-generated method stub
					}
				} );
				if( pkgName.startsWith( "http://" ) || pkgName.startsWith( "https://" ) )
				{
					dl_cb dl = new dl_cb() {
						
						@Override
						public void onSuccess(
								dl_info info ) throws Exception
						{
							// TODO Auto-generated method stub
							invokeJSMethod( "DownloadSuccess" , pkgName );
							if( !IsForegroundRunning( context ) )
							{
								if( MicroEntryHelper.shouldExit( context ) )
								{
									android.os.Process.killProcess( android.os.Process.myPid() );
								}
							}
							//AppInstall( pkgName );
							MyMethod.InstallApk( context , info.getFilePath() );
						}
						
						@Override
						public void onStart(
								dl_info info ) throws Exception
						{
							// TODO Auto-generated method stub
						}
						
						@Override
						public void onFail(
								dl_result result ,
								dl_info info ) throws Exception
						{
							// TODO Auto-generated method stub
							invokeJSMethod( "DownloadFail" , pkgName );
							if( !IsForegroundRunning( context ) )
							{
								if( MicroEntryHelper.shouldExit( context ) )
								{
									android.os.Process.killProcess( android.os.Process.myPid() );
								}
							}
						}
						
						@Override
						public void onDoing(
								dl_info info ) throws Exception
						{
							// TODO Auto-generated method stub
							invokeJSMethod( "DownloadProgress" , pkgName , info.getCurBytes() , info.getTotalBytes() );
						}
					};
					MeApkDlMgr.GetSdkApkDlMgr().UrlDownload( pkgName , dl );
					return;
				}
				if( null == MeApkDlMgr.GetSdkApkDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName ) )
				{
					try
					{
						JSONObject jsonObj = new JSONObject( json );
						String appName = jsonObj.getString( "name" );
						String size = jsonObj.getString( "size" );
						String versionName = jsonObj.getString( "versionName" );
						MeApkDlMgr.StartDownload( curShowType , pkgName , appName , size , versionName , WebMainApkDownloadCallBack );
					}
					catch( Exception e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if( 0 == MeApkDlMgr.GetSdkApkDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName ).getDownloadState() )
				{
					MeApkDlMgr.ReStartDownload( curShowType , pkgName , WebMainApkDownloadCallBack );
				}
				else
				{
					MeApkDlMgr.AddCallback( curShowType , pkgName , WebMainApkDownloadCallBack );
				}
			}
		} );
		th.start();
		return 0;
	}
	
	//	public void  AddCallback(String pkgName)
	//	{
	//	
	//		//MeApkDlMgr.AddCallback(curShowType , pkgName , WebMainApkDownloadCallBack );
	//	}
	@JavascriptInterface
	public void DownloadStop(
			final String pkgName )
	{
		Thread th = new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				MeApkDlMgr.StopDownload( curShowType , pkgName );
			}
		} );
		th.start();
	}
	
	@JavascriptInterface
	public String DownloadQueryInfo(
			String pkgName )
	{
		MELOG.v( "ME_RTFSC" , "DownloadQueryInfo:" + pkgName );
		dl_info info = MeApkDlMgr.GetSdkApkDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , pkgName , false );
		JSONObject json = new JSONObject();
		try
		{
			if( info != null )
			{
				// int DownLoadState; /* 0:初始化状态 1:下载中 2:暂停下载 3:下载完成*/
				json.put( "state" , info.getDownloadState() );
				json.put( "curBytes" , info.getCurBytes() );
				json.put( "totalBytes" , info.getTotalBytes() );
			}
			else
			{
				json.put( "state" , 0 );
				json.put( "curBytes" , 0 );
				json.put( "totalBytes" , 0 );
			}
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json.toString();
	}
	
	// 获取已经安装的APK信息
	@JavascriptInterface
	public String AppQueryInfo(
			String pkgName )
	{
		MELOG.v( "ME_RTFSC" , "AppQueryInfo:" + pkgName );
		JSONObject json = new JSONObject();
		PackageInfo info = getAppPackageInfo( pkgName );
		try
		{
			if( null != info )
			{
				json.put( "appName" , info.applicationInfo.toString() );
				json.put( "packageName" , info.packageName.toString() );
				json.put( "versionCode" , info.versionCode + "" );
				json.put( "versionName" , info.versionName.toString() );
				json.put( "installLocation" , info.applicationInfo.sourceDir.toString() );
				json.put( "cardBytes" , info.applicationInfo.publicSourceDir.toString() );
				json.put( "icon" , info.applicationInfo.icon );
				if( ( info.applicationInfo.flags & info.applicationInfo.FLAG_SYSTEM ) <= 0 )
				{
					json.put( "isInternal" , 0 + "" );
				}
				else
				{
					json.put( "isInternal" , 1 + "" );
				}
			}
		}
		catch( Exception e )
		{
			return new JSONObject().toString();
		}
		//MELOG.v( "ME_RTFSC" , "AppQueryInfo:" + info.versionCode + "" );
		return json.toString();
	}
	
	// 所有安装APK信息
	@JavascriptInterface
	public String AppQueryAll()
	{
		List<PackageInfo> apps = new ArrayList<PackageInfo>();
		PackageManager pManager = context.getPackageManager();
		// 获取手机内所有应�?
		List<PackageInfo> paklist = pManager.getInstalledPackages( 0 );
		for( int i = 0 ; i < paklist.size() ; i++ )
		{
			PackageInfo pak = (PackageInfo)paklist.get( i );
			apps.add( pak );
		}
		JSONArray array = new JSONArray();
		try
		{
			for( int i = 0 ; i <= apps.size() - 1 ; i++ )
			{
				JSONObject json = new JSONObject();
				if( ( apps.get( i ).applicationInfo.flags & apps.get( i ).applicationInfo.FLAG_SYSTEM ) <= 0 )
				{
					json.put( "appName" , apps.get( i ).applicationInfo.toString() );
					json.put( "packageName" , apps.get( i ).packageName.toString() );
					json.put( "versionCode" , apps.get( i ).versionCode + "" );
					json.put( "versionName" , apps.get( i ).versionName.toString() );
					json.put( "installLocation" , apps.get( i ).applicationInfo.sourceDir.toString() );
					json.put( "cardBytes" , apps.get( i ).applicationInfo.publicSourceDir.toString() );
					json.put( "icon" , apps.get( i ).applicationInfo.icon );
					json.put( "isInternal" , 0 + "" );
					array.put( json );
				}
				else
				{
					json.put( "appName" , apps.get( i ).applicationInfo.toString() );
					json.put( "packageName" , apps.get( i ).packageName.toString() );
					json.put( "versionCode" , apps.get( i ).versionCode + "" );
					json.put( "versionName" , apps.get( i ).versionName.toString() );
					json.put( "installLocation" , apps.get( i ).applicationInfo.sourceDir.toString() );
					json.put( "cardBytes" , apps.get( i ).applicationInfo.publicSourceDir.toString() );
					json.put( "icon" , apps.get( i ).applicationInfo.icon );
					json.put( "isInternal" , 1 + "" );
					array.put( json );
				}
			}
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array.toString();
	}
	
	public boolean loadUrl(
			String url )
	{
		MELOG.v( "ME_RTFSC" , "1111   loadUrl" );
		try
		{
			webView.loadUrl( url );
			return true;
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	@JavascriptInterface
	public void alert(
			String text )
	{
		Toast.makeText( context , text , Toast.LENGTH_SHORT ).show();
	}
	
	@JavascriptInterface
	public String getScreenSize() //h11
	{
		JSONObject obj = new JSONObject();
		try
		{
			obj.put( "width" , PhoneInfo.instance( context ).getWidth() );
			obj.put( "height" , PhoneInfo.instance( context ).getHight() );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj.toString();
	}
	
	public static Boolean IsNetworkAvailableLocal(
			Context context )
	{
		final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		if( info != null && info.isAvailable() )
		{
			MELOG.v( "ME_RTFSC" , "=== IsNetworkAvailable  true ===" );
			return true;
		}
		MELOG.v( "ME_RTFSC" , "=== IsNetworkAvailable  false ===" );
		return false;
	}
	
	@JavascriptInterface
	public int IsNetworkAvailable()
	{
		final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		if( info != null && info.isAvailable() )
		{
			MELOG.v( "ME_RTFSC" , "=== IsNetworkAvailable  true ===" );
			return 1;
		}
		MELOG.v( "ME_RTFSC" , "=== IsNetworkAvailable  false ===" );
		return 0;
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
			MELOG.v( "ME_RTFSC" , " ******" + bignumber + "MB" );
			if( bignumber > 10 )
			{
				MELOG.v( "ME_RTFSC" , "=== IsStorageCanUsed  true ===" );
				return true;
			}
		}
		MELOG.v( "ME_RTFSC" , "=== IsStorageCanUsed  false ===" );
		return false;
	}
	
	//	@JavascriptInterface
	//	public int getVersion()
	//	{
	//		return android.os.Build.VERSION.SDK_INT;
	//	}
	@JavascriptInterface
	public void AppStart(
			String packageName )
	{
		//	MyMethod.StartActivityByPackageName( context , packageName );
		PackageManager packageManager = context.getPackageManager();
		Intent intent = null;
		try
		{
			intent = packageManager.getLaunchIntentForPackage( packageName );
		}
		catch( Exception e )
		{
			intent = null;
		}
		if( null != intent )
		{
			context.startActivity( intent );
		}
	}
	
	@JavascriptInterface
	public int AppInstall(
			String PackageName )
	{
		MELOG.v( "ME_RTFSC" , " ====== AppInstall   webpage===== " );
		dl_info info = MeApkDlMgr.GetSdkApkDlMgr().ResGetInfo( CoolDLResType.RES_TYPE_APK , PackageName , true );
		if( info != null )
		{
			if( info.IsDownloadSuccess() )
			{
				MyMethod.InstallApk( context , info.getFilePath() );
				return 0;
			}
		}
		return 1;
	}
	
	@JavascriptInterface
	public void SetBackgroundWithWallpaper()
	{
		Message msg = new Message();
		msg.what = MeMainActivity.setBackgroundWithWallpaper;
		msg.obj = webView;
		MainActivityHandler.sendMessage( msg );
	}
	
	// 返回值为字符串类型， 'zh'或者'ZH'为中文,其余的显示英文
	@JavascriptInterface
	public String GetSysLanguage()
	{
		Locale locale = context.getResources().getConfiguration().locale;
		String language = locale.getLanguage();
		return language;
	}
	
	@JavascriptInterface
	public void DlMgrOpen()
	{
		Intent intent = new Intent( Intent.ACTION_VIEW );
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		intent.setClass( context , ApkMangerActivity.class );
		intent.putExtra( "moudleName" , moudleName );
		intent.putExtra( "entryId" , 0 );
		context.startActivity( intent );
	}
	
	@JavascriptInterface
	public void Refresh_Retry()
	{
		MELOG.v( "ME_RTFSC" , "2222   Refresh_Retry" );
		webView.loadUrl( failingUrl );
	}
	
	@JavascriptInterface
	public void Set_Network()
	{
		( (Activity)context ).startActivity( new Intent( Settings.ACTION_SETTINGS ) );
	}
	
	@JavascriptInterface
	public void LoadUrlByBrowser(
			String url )
	{
		PackageManager packageMgr = context.getPackageManager();
		ResolveInfo info = getUserDefinitionBrowers( packageMgr );
		if( info != null )
		{
			gotoUrl( context , info.activityInfo.packageName , url , packageMgr );
		}
		else
		{
			choiceBrowserToVisitUrl( context , url , packageMgr );
		}
	}
	
	private List<ResolveInfo> getAllInstallBrowser(
			PackageManager packageMgr )
	{
		Intent intent = ( new Intent( Intent.ACTION_VIEW , Uri.parse( "http://" ) ) );
		intent.addCategory( Intent.CATEGORY_BROWSABLE );
		List<ResolveInfo> allMatches = packageMgr.queryIntentActivities( intent , PackageManager.MATCH_DEFAULT_ONLY );
		return allMatches;
	}
	
	private void choiceBrowserToVisitUrl(
			Context context ,
			String url ,
			PackageManager packageMgr )
	{
		boolean existUC = false , existOpera = false , existQQ = false , existBaidu = false , existFirebox = false , existGoogle = false;
		String ucPath = "" , operaPath = "" , qqPath = "" , fireboxPath = "" , baiduPath = "" , googlePath = "";
		//List<PackageInfo> list = packageMgr.getInstalledPackages( 0 );
		List<ResolveInfo> list = getAllInstallBrowser( packageMgr );
		for( int i = 0 ; i < list.size() ; i++ )
		{
			ResolveInfo info = list.get( i );
			String temp = info.activityInfo.applicationInfo.packageName;
			if( temp.equals( "com.tencent.mtt" ) )
			{
				// 存在QQ
				qqPath = temp;
				existQQ = true;
			}
			else if( temp.equals( "com.UCMobile" ) )//com.uc.browser
			{
				// 存在UC
				ucPath = temp;
				existUC = true;
			}
			else if( temp.equals( "com.baidu.browser.apps" ) )//com.baidu.browser.cloud.launcher.RECEIVE
			{
				baiduPath = temp;
				existBaidu = true;
			}
			else if( temp.equals( "com.opera.browser.classic" ) || temp.equals( "com.oupeng.mini.android" ) )//com.opera.mini.android
			{
				// 存在Opera
				operaPath = temp;
				existOpera = true;
			}
			else if( temp.equals( "org.mozilla.firefox_beta" ) || temp.equals( "org.mozilla.firefox" ) )
			{
				fireboxPath = temp;
				existFirebox = true;
			}
			else if( temp.equals( "com.android.chrome" ) )
			{
				// 存在GoogleBroser
				googlePath = temp;
				existGoogle = true;
			}
		}
		if( existQQ )
		{
			gotoUrl( context , qqPath , url , packageMgr );
		}
		else if( existUC )
		{
			gotoUrl( context , ucPath , url , packageMgr );
		}
		else if( existBaidu )
		{
			gotoUrl( context , baiduPath , url , packageMgr );
		}
		else if( existOpera )
		{
			gotoUrl( context , operaPath , url , packageMgr );
		}
		else if( existFirebox )
		{
			gotoUrl( context , fireboxPath , url , packageMgr );
		}
		else if( existGoogle )
		{
			gotoUrl( context , googlePath , url , packageMgr );
		}
		else
		{
			doDefaultBrowser( context , url );
		}
	}
	
	private ResolveInfo getUserDefinitionBrowers(
			PackageManager packageMgr )
	{
		Intent intent = ( new Intent( Intent.ACTION_VIEW , Uri.parse( "http://" ) ) );
		intent.addCategory( Intent.CATEGORY_BROWSABLE );
		ResolveInfo bestMatch = packageMgr.resolveActivity( intent , PackageManager.MATCH_DEFAULT_ONLY );
		List<ResolveInfo> allMatches = getAllInstallBrowser( packageMgr );
		boolean found = false;
		for( ResolveInfo ri : allMatches )
		{
			if( bestMatch.activityInfo.name.equals( ri.activityInfo.name ) && bestMatch.activityInfo.applicationInfo.packageName.equals( ri.activityInfo.applicationInfo.packageName ) )
			{
				found = true;
				break;
			}
		}
		if( !found )
		{
			return null;
		}
		return bestMatch;
	}
	
	private void doDefaultBrowser(
			Context context ,
			String visitUrl )
	{
		try
		{
			Intent intent = new Intent( Intent.ACTION_VIEW , Uri.parse( visitUrl ) );
			context.startActivity( intent );
		}
		catch( Exception e )
		{
			// 在1.5及以前版本会要求catch(android.content.pm.PackageManager.NameNotFoundException)异常，该异常在1.5以后版本已取消。
			e.printStackTrace();
		}
	}
	
	private void gotoUrl(
			Context context ,
			String packageName ,
			String url ,
			PackageManager packageMgr )
	{
		try
		{
			Intent intent = packageMgr.getLaunchIntentForPackage( packageName );
			intent.setAction( Intent.ACTION_VIEW );
			intent.addCategory( Intent.CATEGORY_DEFAULT );
			intent.setData( Uri.parse( url ) );
			context.startActivity( intent );
		}
		catch( Exception e )
		{
			// 在1.5及以前版本会要求catch(android.content.pm.PackageManager.NameNotFoundException)异常，该异常在1.5以后版本已取消。
			e.printStackTrace();
		}
	}
	
	@JavascriptInterface
	public String h01()
	{
		return MceInfo.instance( context ).getH01();
	}
	
	@JavascriptInterface
	public int h02()
	{
		return MceInfo.instance( context ).getH02();
	}
	
	@JavascriptInterface
	public String h03()
	{
		return MceInfo.instance( context ).getH03();
	}
	
	@JavascriptInterface
	public String h04()
	{
		return MceInfo.instance( context ).getH04();
	}
	
	@JavascriptInterface
	public String h05()
	{
		return MceInfo.instance( context ).getH05();
	}
	
	@JavascriptInterface
	public String h06()
	{
		return MceInfo.instance( context ).getH06();
	}
	
	@JavascriptInterface
	public String h07()
	{
		return MceInfo.instance( context ).getH07();
	}
	
	@JavascriptInterface
	public String h08()
	{
		return MceInfo.instance( context ).getH08();
	}
	
	@JavascriptInterface
	public String h09()
	{
		return MceInfo.instance( context ).getH09();
	}
	
	@JavascriptInterface
	public String h10()
	{
		// int simIdx = 1;
		return MceInfo.instance( context ).getH10();
	}
	
	@JavascriptInterface
	public String h11()
	{
		return MceInfo.instance( context ).getH11();
	}
	
	@JavascriptInterface
	public int h12()
	{
		return MceInfo.instance( context ).getH12();
	}
	
	@JavascriptInterface
	public int h13()
	{
		return MceInfo.instance( context ).getH13();
	}
	
	@JavascriptInterface
	public String h16()
	{
		return MceInfo.instance( context ).getH16();
	}
	
	@JavascriptInterface
	public String h18()
	{
		return MceInfo.instance( context ).getH18();
	}
	
	@JavascriptInterface
	public String h19()
	{
		return MceInfo.instance( context ).getH19();
	}
	
	@JavascriptInterface
	public String a00()
	{
		return PhoneInfo.instance( context ).getA00();
	}
	
	@JavascriptInterface
	public String a01()
	{
		return PhoneInfo.instance( context ).getA01();
	}
	
	@JavascriptInterface
	public String a02()
	{
		return PhoneInfo.instance( context ).getA02();
	}
	
	@JavascriptInterface
	public String a03()
	{
		return PhoneInfo.instance( context ).getA03();
	}
	
	@JavascriptInterface
	public String a04()
	{
		return PhoneInfo.instance( context ).getA04();
	}
	
	@JavascriptInterface
	public String a05()
	{
		return PhoneInfo.instance( context ).getA05();
	}
	
	@JavascriptInterface
	public String a06()
	{
		return PhoneInfo.instance( context ).getA06();
	}
	
	@JavascriptInterface
	public String a07()
	{
		return PhoneInfo.instance( context ).getA07();
	}
	
	@JavascriptInterface
	public String a08()
	{
		return PhoneInfo.instance( context ).getA08();
	}
	
	@JavascriptInterface
	public String a09()
	{
		return PhoneInfo.instance( context ).getA09();
	}
	
	@JavascriptInterface
	public String a10()
	{
		return PhoneInfo.instance( context ).getA10();
	}
	
	@JavascriptInterface
	public String a11()
	{
		return PhoneInfo.instance( context ).getA11();
	}
	
	@JavascriptInterface
	public String a12()
	{
		return PhoneInfo.instance( context ).getA12();
	}
	
	@JavascriptInterface
	public String a13()
	{
		return PhoneInfo.instance( context ).getA13();
	}
	
	@JavascriptInterface
	public String a14()
	{
		return PhoneInfo.instance( context ).getA14();
	}
	
	@JavascriptInterface
	public String a15()
	{
		return PhoneInfo.instance( context ).getA15();
	}
	
	@JavascriptInterface
	public String a16()
	{
		return PhoneInfo.instance( context ).getA16();
	}
	
	@JavascriptInterface
	public String a17()
	{
		return PhoneInfo.instance( context ).getA17();
	}
	
	@JavascriptInterface
	public String a18()
	{
		return PhoneInfo.instance( context ).getA18();
	}
	
	@JavascriptInterface
	public String a19()
	{
		return PhoneInfo.instance( context ).getA19();
	}
	
	@JavascriptInterface
	public String a20()
	{
		return PhoneInfo.instance( context ).getA20();
	}
	
	@JavascriptInterface
	public String a21()
	{
		return PhoneInfo.instance( context ).getA21();
	}
}
