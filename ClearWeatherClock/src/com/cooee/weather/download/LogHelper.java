// xiatian add whole file //OperateFolder
package com.cooee.weather.download;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;


// import com.iLoong.launcher.macinfo.Installation;
public class LogHelper
{
	
	public static final String LOG_URL_TEST = "http://uilog.coolauncher.com.cn/iloong/pui/LogEngine/DataService";
	// public static final String LOG_URL_TEST =
	// "http://58.246.135.237:20180/iloong/pui/LogEngine/DataService";
	public static final String LOG_URL = "http://uilog.coolauncher.com.cn/iloong/pui/LogEngine/DataService";
	public static final String LOG_ACTION_DOWNLOAD = "0003";
	public static final String LOG_ACTION_INSTALL = "0004";
	public static final String LOG_ACTION_UNINSTALL = "0005";
	public static final String LOG_ACTION_DOWNLOAD_VIRTURE_ICON = "0024";
	public static final String LOG_ACTION_REQUEST_DOWNLOAD_VIRTURE_ICON = "0025";
	public static final String LOG_ACTION_INSTALL_VIRTURE_ICON = "0026";
	public static final String LOG_ACTION_UNINSTALL_VIRTURE_ICON = "0027";
	// private static String curPkgName = null;
	// private static String curResID = null;
	// private static int curCount = 0;
	private static SharedPreferences curPreferences;
	private static List<String> logtextList = new ArrayList<String>();
	
	public synchronized static void initPreferences(
			SharedPreferences preferences )
	{
		if( curPreferences == null )
		{
			curPreferences = preferences;
		}
		String logList = curPreferences.getString( "logList" , null );
		if( logList != null )
		{
			logtextList.clear();
			String[] itemList = logList.split( "#" );
			for( int i = 0 ; i < itemList.length ; i++ )
			{
				logtextList.add( itemList[i] );
			}
		}
	}
	
	public synchronized static boolean log(
			Context context ,
			String action ,
			String pkgName ,
			String resID )
	{
		// curPkgName = pkgName;
		// curResID = resID;
		String url = LOG_URL_TEST;
		String logtext = action + ";" + pkgName + ";" + resID;
		Log.i( "OPFolder" , "log action,pkgname,resID:" + action + "," + pkgName + "," + resID );
		if( !findLogtext( logtext ) )
		{
			logtextList.add( logtext );
			saveLogList();
		}
		String params = getLongParams( context );// getParams(context,action);
		//Log.i("OPFolder", "log param:" + params);
		if( params != null )
		{
			String[] res = DownloadUtils.post( context , url , params );
			if( res != null )
			{
				String content = res[0];
				JSONObject json = null;
				try
				{
					json = new JSONObject( content );
					int retCode = json.getInt( "retcode" );
					if( retCode == 0 )
					{
						Log.i( "OPFolder" , "log ok!" );
						clearLogtextList();
						return true;
					}
				}
				catch( JSONException e )
				{
					e.printStackTrace();
				}
			}
		}
		Log.i( "OPFolder" , "log fail!" );
		return false;
	}
	
	//
	private static boolean findLogtext(
			String logtext )
	{
		for( String log : logtextList )
		{
			if( log.equals( logtext ) )
			{
				return true;
			}
		}
		return false;
	}
	
	//
	private static void saveLogList()
	{
		String result = null;
		for( String log : logtextList )
		{
			if( result == null )
			{
				result = log + "#";
			}
			else
			{
				result += log + "#";
			}
		}
		if( curPreferences != null )
		{
			curPreferences.edit().putString( "logList" , result ).commit();
		}
	}
	
	//
	private static void clearLogtextList()
	{
		logtextList.clear();
		saveLogList();
	}
	
	//
	private synchronized static String getLongParams(
			Context context )
	{
		String text = "";
		boolean isSingleLog = false;
		String log0017 = null;
		String log0017List = null;
		for( int i = 0 ; i < logtextList.size() ; i++ )
		{
			String logtext = logtextList.get( i );
			if( logtextList.size() == 1 )
			{
				isSingleLog = true;
				text = getParams( context , logtext , true );
				break;
			}
			else
			{
				if( log0017 == null )
				{
					log0017 = getParams0017NoMd5( context , logtext );
				}
				text = getParams( context , logtext , false );
				log0017List = getParams0017List( log0017List , text );
			}
		}
		if( !isSingleLog )
		{
			if( ( log0017 != null ) && ( log0017List != null ) )
			{
				text = getParams0017WithMd5( log0017 , log0017List );
			}
			else
			{
				text = null;
			}
		}
		return text;
	}
	
	//
	private static String getParams0017WithMd5(
			String logtext ,
			String listItems )
	{
		JSONArray array = null;
		try
		{
			array = new JSONArray( listItems );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject res = null;
		try
		{
			res = new JSONObject( logtext );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			res.put( "list" , array );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String content = res.toString();
		String md5_res = DownloadUtils.getMD5EncruptKey( content + DownloadHelper.DEFAULT_KEY );
		String newContent = content.substring( 0 , content.lastIndexOf( '}' ) );
		String params = newContent + ",\"md5\":\"" + md5_res + "\"}";
		return params;
	}
	
	//
	private static String getParams0017List(
			String logtext ,
			String listItem )
	{
		JSONArray array = null;
		if( logtext == null )
		{
			array = new JSONArray();
		}
		else
		{
			try
			{
				array = new JSONArray( logtext );
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		JSONObject res = null;
		try
		{
			res = new JSONObject( listItem );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		array.put( res );
		return array.toString();
	}
	
	//
	private static String getParams0017NoMd5(
			Context context ,
			String logtext )
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
		String productname = "uifolder";
		// if (OperateFolderProxy.folder_default_show) {
		// productname = "uishowfolder";
		// }
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if( appid == null || sn == null )
			return null;
		pm = context.getPackageManager();
		res = new JSONObject();
		try
		{
			res.put( "Action" , "0017" );
			res.put( "packname" , context.getPackageName() );
			res.put( "versioncode" , pm.getPackageInfo( context.getPackageName() , 0 ).versionCode );
			res.put( "versionname" , pm.getPackageInfo( context.getPackageName() , 0 ).versionName );
			res.put( "sn" , sn );
			res.put( "appid" , appid );
			res.put( "shellid" , DownloadUtils.getShellID( context ) );
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
			res.put( "productname" , productname );
			res.put( "count" , 0 );
			res.put( "opversion" , DownloadUtils.getVersion() );
			String content = res.toString();
			String params = content;
			return params;
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private synchronized static String getParams(
			Context context ,
			String logtext ,
			boolean isAddMd5 )
	{
		String[] itemsTemp = logtext.split( ";" );
		String action = null;
		String pkgName = null;
		String resID = null;
		if( itemsTemp.length > 2 )
		{
			resID = itemsTemp[2];
			pkgName = itemsTemp[1];
			action = itemsTemp[0];
		}
		else if( itemsTemp.length > 1 )
		{
			pkgName = itemsTemp[1];
			action = itemsTemp[0];
		}
		else if( itemsTemp.length > 0 )
		{
			action = itemsTemp[0];
		}
		else
		{
			return null;
		}
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
		String productname = "uifolder";
		// if (OperateFolderProxy.folder_default_show) {
		// productname = "uishowfolder";
		// }
		if( action.equals( LOG_ACTION_REQUEST_DOWNLOAD_VIRTURE_ICON ) || action.equals( LOG_ACTION_DOWNLOAD_VIRTURE_ICON ) || action.equals( LOG_ACTION_INSTALL_VIRTURE_ICON ) || action
				.equals( LOG_ACTION_UNINSTALL_VIRTURE_ICON ) )
		{
			productname = "uiicondown";
		}
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if( appid == null || sn == null )
			return null;
		pm = context.getPackageManager();
		res = new JSONObject();
		try
		{
			res.put( "Action" , action );
			if( isAddMd5 )
			{
				res.put( "packname" , context.getPackageName() );
				res.put( "versioncode" , pm.getPackageInfo( context.getPackageName() , 0 ).versionCode );
				res.put( "versionname" , pm.getPackageInfo( context.getPackageName() , 0 ).versionName );
				res.put( "sn" , sn );
				res.put( "appid" , appid );
				res.put( "shellid" , DownloadUtils.getShellID( context ) );
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
				res.put( "productname" , productname );
				res.put( "count" , 0 );
				res.put( "opversion" , DownloadUtils.getVersion() );
			}
			if( action.equals( LOG_ACTION_REQUEST_DOWNLOAD_VIRTURE_ICON ) || action.equals( LOG_ACTION_DOWNLOAD ) || action.equals( LOG_ACTION_INSTALL ) || action.equals( LOG_ACTION_UNINSTALL ) || action
					.equals( LOG_ACTION_DOWNLOAD_VIRTURE_ICON ) || action.equals( LOG_ACTION_INSTALL_VIRTURE_ICON ) || action.equals( LOG_ACTION_UNINSTALL_VIRTURE_ICON ) )
			{
				res.put( "param1" , resID );
				res.put( "param2" , pkgName );
			}
			String content = res.toString();
			String params = null;
			if( isAddMd5 )
			{
				String md5_res = DownloadUtils
						.getMD5EncruptKey( content + "f24657aafcb842b185c98a9d3d7c6f4725f6cc4597c3a4d531c70631f7c7210fd7afd2f8287814f3dfa662ad82d1b02268104e8ab3b2baee13fab062b3d27bff" );
				// res.put("md5", md5_res);
				String newContent = content.substring( 0 , content.lastIndexOf( '}' ) );
				params = newContent + ",\"md5\":\"" + md5_res + "\"}";
			}
			else
			{
				params = content;
			}
			return params;
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
