// xiatian add whole file //OperateFolder
package com.cooee.weather.download;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cooee.weather.download.DownloadHelper.DownloadListener;


public class DownloadProxy
{
	
	public static final String VERSION_CODE = "1";
	// preference key
	public static final String PREFERENCE_KEY = "icon_download";
	public static final String FOLDER_ITEMS_KEY = "icon_items";
	// server config
	public static final String SERVER_URL_TEST = "http://uifolder.coolauncher.com.cn/iloong/pui/ServicesEngine/DataService";
	public static final String DEFAULT_KEY = "f24657aafcb842b185c98a9d3d7c6f4725f6cc4597c3a4d531c70631f7c7210fd7afd2f8287814f3dfa662ad82d1b02268104e8ab3b2baee13fab062b3d27bff";
	public static final int REQUEST_ACTION_VIRTURE_ICON_URL = 1005;
	private static DownloadProxy proxy;
	private static Context context;
	private OperateFolderReceiver receiver;
	private SharedPreferences preferences;
	private String curDownloadPkgName = null;
	private String curResID = null;
	private List<DownloadListener> listeners = new ArrayList<DownloadListener>();
	
	public static DownloadProxy getInstance(
			Context _activity )
	{
		if( proxy == null )
		{
			synchronized( DownloadProxy.class )
			{
				if( proxy == null )
					proxy = new DownloadProxy( _activity );
			}
		}
		return proxy;
	}
	
	public void addListener(
			DownloadListener listener )
	{
		listeners.add( listener );
	}
	
	public static void onDestroy()
	{
		synchronized( DownloadProxy.class )
		{
			if( proxy != null )
				proxy.destroy();
		}
	}
	
	private void destroy()
	{
		if( receiver != null )
		{
			context.unregisterReceiver( receiver );
		}
		proxy = null;
	}
	
	public DownloadProxy(
			Context _context )
	{
		context = _context;
		preferences = context.getSharedPreferences( PREFERENCE_KEY , Activity.MODE_PRIVATE );
		start();
	}
	
	public void start()
	{
		Log.v( "OPFolder" , "OPFolder--------start" );
		LogHelper.initPreferences( preferences );
		receiver = new OperateFolderReceiver();
		IntentFilter filter2 = new IntentFilter( Intent.ACTION_PACKAGE_ADDED );
		filter2.addAction( Intent.ACTION_PACKAGE_REMOVED );
		filter2.addDataScheme( "package" );
		context.registerReceiver( receiver , filter2 );
		IntentFilter filter3 = new IntentFilter( Intent.ACTION_MEDIA_EJECT );
		filter3.addDataScheme( "file" );
		context.registerReceiver( receiver , filter3 );
	}
	
	public synchronized String[] getVirtureIconDownloadUrl(
			Context context ,
			String packageName )
	{
		curDownloadPkgName = packageName;
		String url = SERVER_URL_TEST;
		String params;
		params = getParams( REQUEST_ACTION_VIRTURE_ICON_URL );
		String[] result = null;
		if( params != null )
		{
			String[] res = DownloadUtils.post( context , url , params );
			if( res != null )
			{
				String content = res[0];
				Log.i( "OPFolder" , "res:" + content );
				JSONObject json = null;
				try
				{
					json = new JSONObject( content );
					int retCode = json.getInt( "retcode" );
					if( retCode == 0 )
					{
						String downloadUrl = json.getString( "url" );
						curResID = json.getString( "resid" );
						result = new String[2];
						result[0] = downloadUrl;
						result[1] = curResID;
					}
				}
				catch( JSONException e )
				{
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	// 鐢熸垚璇锋眰鍙傛暟
	private String getParams(
			int action )
	{
		String appid = null;
		String sn = null;
		JSONObject tmp = Assets.getConfig( context );
		PackageManager pm;
		JSONObject res;
		int networktype = -1;
		int networksubtype = -1;
		ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
		if( netInfo != null )
		{
			networktype = netInfo.getType();
			networksubtype = netInfo.getSubtype();
		}
		switch( action )
		{
			case REQUEST_ACTION_VIRTURE_ICON_URL:
				if( tmp != null )
				{
					try
					{
						JSONObject config = tmp.getJSONObject( "config" );
						appid = config.getString( "app_id" );
						sn = config.getString( "serialno" );
					}
					catch( JSONException e )
					{
						e.printStackTrace();
					}
				}
				if( appid == null || sn == null )
					return null;
				pm = context.getPackageManager();
				res = new JSONObject();
				try
				{
					res.put( "Action" , REQUEST_ACTION_VIRTURE_ICON_URL + "" );
					res.put( "packname" , context.getPackageName() );
					res.put( "versioncode" , pm.getPackageInfo( context.getPackageName() , 0 ).versionCode );
					res.put( "versionname" , pm.getPackageInfo( context.getPackageName() , 0 ).versionName );
					res.put( "sn" , sn );
					res.put( "appid" , appid );
					res.put( "shellid" , DownloadUtils.getShellID( context ) );
					res.put( "respackname" , this.curDownloadPkgName );
					res.put( "uuid" , Installation.id( context ) );
					TelephonyManager mTelephonyMgr = (TelephonyManager)context.getSystemService( Context.TELEPHONY_SERVICE );
					res.put( "imsi" , mTelephonyMgr.getSubscriberId() == null ? "" : mTelephonyMgr.getSubscriberId() );
					res.put( "iccid" , mTelephonyMgr.getSimSerialNumber() == null ? "" : mTelephonyMgr.getSimSerialNumber() );
					res.put( "imei" , mTelephonyMgr.getDeviceId() == null ? "" : mTelephonyMgr.getDeviceId() );
					res.put( "phone" , mTelephonyMgr.getLine1Number() == null ? "" : mTelephonyMgr.getLine1Number() );
					java.text.DateFormat format = new java.text.SimpleDateFormat( "yyyyMMddhhmmss" );
					res.put( "localtime" , format.format( new Date() ) );
					res.put( "model" , Build.MODEL );
					res.put( "display" , Build.DISPLAY );
					res.put( "product" , Build.PRODUCT );
					res.put( "device" , Build.DEVICE );
					res.put( "board" , Build.BOARD );
					res.put( "manufacturer" , Build.MANUFACTURER );
					res.put( "brand" , Build.BRAND );
					res.put( "hardware" , Build.HARDWARE );
					res.put( "buildversion" , Build.VERSION.RELEASE );
					res.put( "sdkint" , Build.VERSION.SDK_INT );
					res.put( "androidid" , android.provider.Settings.Secure.getString( context.getContentResolver() , android.provider.Settings.Secure.ANDROID_ID ) );
					res.put( "buildtime" , Build.TIME );
					res.put( "heightpixels" , context.getResources().getDisplayMetrics().heightPixels );
					res.put( "widthpixels" , context.getResources().getDisplayMetrics().widthPixels );
					res.put( "networktype" , networktype );
					res.put( "networksubtype" , networksubtype );
					res.put( "producttype" , 4 );
					res.put( "productname" , "uiicondown" );
					res.put( "count" , 0 );
					res.put( "opversion" , DownloadUtils.getVersion() );
					String content = res.toString();
					String md5_res = DownloadUtils.getMD5EncruptKey( content + DEFAULT_KEY );
					// res.put("md5", md5_res);
					String newContent = content.substring( 0 , content.lastIndexOf( '}' ) );
					String params = newContent + ",\"md5\":\"" + md5_res + "\"}";
					return params;
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
				return null;
		}
		return null;
	}
	
	public void markDownload(
			String packageName ,
			String resID )
	{
		String s = preferences.getString( FOLDER_ITEMS_KEY , "" );
		FolderItem item = null;
		item = new FolderItem( packageName , "" , true , false );
		if( s.equals( "" ) )
			s += item.toString();
		else
			s += "#" + item.toString();
		preferences.edit().putString( FOLDER_ITEMS_KEY , s ).commit();
	}
	
	public void markInstall(
			String packageName ,
			String resID )
	{
		String s = preferences.getString( FOLDER_ITEMS_KEY , "" );
		FolderItem item1 = null;
		item1 = new FolderItem( packageName , resID , true , false );
		FolderItem item2 = null;
		item2 = new FolderItem( packageName , resID , true , true );
		s = s.replace( item1.toString() , item2.toString() );
		preferences.edit().putString( FOLDER_ITEMS_KEY , s ).commit();
	}
	
	public boolean hasDownload(
			String packageName ,
			String resID )
	{
		String s = preferences.getString( FOLDER_ITEMS_KEY , null );
		if( s == null )
			return false;
		FolderItem item = null;
		item = new FolderItem( packageName , resID , true , false );
		return s.contains( item.toString() );
	}
	
	public boolean hasInstall(
			String packageName ,
			String resID )
	{
		String s = preferences.getString( FOLDER_ITEMS_KEY , null );
		if( s == null )
			return false;
		FolderItem item = null;
		item = new FolderItem( packageName , resID , true , true );
		return s.contains( item.toString() );
	}
	
	public void markUninstall(
			String packageName ,
			String resID )
	{
		String s = preferences.getString( FOLDER_ITEMS_KEY , "" );
		FolderItem item1 = null;
		item1 = new FolderItem( packageName , resID , true , true );
		FolderItem item2 = null;
		item2 = new FolderItem( packageName , resID , false , false );
		s = s.replace( item1.toString() , item2.toString() );
		preferences.edit().putString( FOLDER_ITEMS_KEY , s ).commit();
	}
	
	class FolderItem
	{
		
		String pkgName;
		String resID;
		boolean download = false;
		boolean install = false;
		
		public FolderItem(
				String pkgName ,
				String resID ,
				boolean download ,
				boolean install )
		{
			this.pkgName = pkgName;
			this.resID = resID;
			this.download = download;
			this.install = install;
		}
		
		public FolderItem(
				String s )
		{
			String[] res = s.split( ";" );
			pkgName = res[0];
			resID = res[1];
			download = res[2].equals( "true" ) ? true : false;
			install = res[3].equals( "true" ) ? true : false;
		}
		
		public String toString()
		{
			return pkgName + ";" + resID + ";" + ( download ? "true" : "false" ) + ";" + ( install ? "true" : "false" );
		}
	}
	
	public class OperateFolderReceiver extends BroadcastReceiver
	{
		
		@Override
		public void onReceive(
				final Context context ,
				Intent intent )
		{
			String action = intent.getAction();
			Log.v( "OPFolder" , "Icon Download--------onReceive:" + action );
			if( action.equals( Intent.ACTION_PACKAGE_ADDED ) )
			{
				final String packageName = intent.getData().getSchemeSpecificPart();
				if( hasDownload( packageName , "" ) )
				{
					DownloadHelper.removeFinishNotification( context , packageName );
					if( listeners != null )
					{
						Iterator<DownloadListener> ite = listeners.iterator();
						while( ite.hasNext() )
						{
							DownloadListener listener = ite.next();
							listener.onInstallSuccess( packageName );
							ite.remove();
						}
					}
					new Thread() {
						
						@Override
						public void run()
						{
							if( LogHelper.log( context , LogHelper.LOG_ACTION_INSTALL_VIRTURE_ICON , packageName , "" ) )
							{
								markInstall( packageName , "" );
							}
							super.run();
						}
					}.start();
				}
			}
			else if( action.equals( Intent.ACTION_PACKAGE_REMOVED ) )
			{
				Log.v( "OPFolder" , "OPFolder--------package removed" );
				final String packageName = intent.getData().getSchemeSpecificPart();
				if( hasInstall( packageName , "" ) )
				{
					new Thread() {
						
						@Override
						public void run()
						{
							if( LogHelper.log( context , LogHelper.LOG_ACTION_UNINSTALL_VIRTURE_ICON , packageName , "" ) )
							{
								markUninstall( packageName , "" );
							}
							super.run();
						}
					}.start();
				}
			}
			else if( action.equals( Intent.ACTION_MEDIA_EJECT ) )
			{
				DownloadHelper.failAllDowningNotification( context );
			}
		}
	}
}
