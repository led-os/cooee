package com.coco.shortcut.shortcutbox;


import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.coco.download.Assets;
import com.coco.download.CustomerHttpClient;
import com.coco.download.DownloadList;
import com.coco.download.Installation;
import com.coco.download.ResultEntity;
import com.cooee.shell.sdk.CooeeSdk;
import com.iLoong.base.themebox.R;


public class ShortCutProxy
{
	
	private boolean watchNetwork = true;
	private final String UPDATEINTERVAL_KEY = "update_interval";
	private final String NEXT_UPDATE_KEY = "next_update_time";
	private final String CONFIG_TIME_STAMP_KEY = "config_time_stamp";
	private final String CONTENT_TIME_STAMP_KEY = "content_time_stamp";
	private final String MIN_CALL_TIME_KEY = "min_call_time";
	private final String MIN_CALL_DATE_KEY = "min_call_date";
	private final String MIN_SMS_NUMBER_KEY = "min_sms_number";
	private final String GAMETAB_ISSHOW = "gametab_isshow";
	private final int REQUEST_ACTION_CONFIG = 1303;
	private final int REQUEST_ACTION_CONTENT = 1002;
	private static Context mContext;
	//	private final String SERVER_URL_TEST = "http://uifolder.coolauncher.com.cn/iloong/pui/ServicesEngine/DataService";
	private final String DEFAULT_KEY = "f24657aafcb842b185c98a9d3d7c6f4725f6cc4597c3a4d531c70631f7c7210fd7afd2f8287814f3dfa662ad82d1b02268104e8ab3b2baee13fab062b3d27bff";
	public final long DEFAULT_MIN_CALL_TIME = 18000L;
	public final long DEFAULT_MIN_CALL_DATE = 2592000000L;
	public final long DEFAULT_TIME_INTERVAL = 86400000L;
	private final long MIN_TIME_INTERVAL = 24; //最小间隔时间，小时单位
	private final long MAX_TIME_INTERVAL = 7 * 24;//最大间隔时间，小时单位
	public final long DEFAULT_SMS_NUMBER = 100;
	private PendingIntent pi;
	private static ShortCutProxy mInstance;
	private static SharedPreferences preferences;
	
	public static ShortCutProxy getInstance(
			Context context )
	{
		mContext = null;
		mContext = context;
		if( mInstance == null )
		{
			mInstance = new ShortCutProxy();
			preferences = PreferenceManager.getDefaultSharedPreferences( mContext );
			IntentFilter filter = new IntentFilter( Intent.ACTION_SCREEN_OFF );
			mContext.getApplicationContext().registerReceiver( new NetworkListenerReceiver() , filter );
		}
		return mInstance;
	}
	
	//public static final String SERVER_URL_TEST = "http://58.246.135.237:20180/iloong/pui/ServicesEngine/DataService";
	//public static final String SERVER_URL = "http://uifolder.coolauncher.com.cn/iloong/pui/ServicesEngine/DataService";
	public void doRequest()
	{
		if( !isNetworkAvailable( mContext ) )
		{
			if( UtilsBase.showOperateLog )
				Log.v( "OPCenter" , "OPCenter--------doRequest 没有网，进行监听" );
			setWatchNetwork( true );
			return;
		}
		if( mHandler != null )
		{
			if( UtilsBase.showOperateLog )
				Log.v( "OPCenter" , "OPCenter--------doRequest:已经在request" );
			return;
		}
		checkThread();
		mHandler.post( requestRunnable );
	}
	
	private boolean isNetworkAvailable(
			Context context )
	{
		try
		{
			ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
			NetworkInfo info = cm.getActiveNetworkInfo();
			return( info != null && info.isConnected() );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private HandlerThread handlerThread;
	private Handler mHandler;
	private Object threadSync = new Object();
	private boolean configSuccess = true;
	private Runnable requestRunnable = new Runnable() {
		
		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			mHandler.removeCallbacks( requestRunnable );
			configSuccess = true;
			android.os.Process.setThreadPriority( android.os.Process.THREAD_PRIORITY_BACKGROUND );
			configSuccess = requestConfig();
			preferences.edit().putLong( "requestTime" , System.currentTimeMillis() ).commit();
			//激活后上送访问文件夹次数数据
			if( configSuccess )
			{
				if( isActive() )
				{
					//				LogHelper.logFolderOpened( context , preferences , getFolderOpenedNum() , getLastTimeLogFolderOpenedNum() );
					int isshow = preferences.getInt( GAMETAB_ISSHOW , 0 );
					Log.v( "OPCenter" , "isshow == " + isshow );
					if( isshow == 1 )
					{
						PowerManager pm = (PowerManager)mContext.getSystemService( Context.POWER_SERVICE );
						boolean screen = pm.isScreenOn();
						if( !screen )
						{
							removeShortcut();
							addShortcut();
						}
						else
						{
							preferences.edit().putBoolean( "addShortcut" , true ).commit();
						}
					}
					else
					{
						PowerManager pm = (PowerManager)mContext.getSystemService( Context.POWER_SERVICE );
						boolean screen = pm.isScreenOn();
						if( !screen )
						{
							removeShortcut();
						}
						else
						{
							preferences.edit().putBoolean( "removeShortcut" , true ).commit();
						}
					}
				}
			}
			exitThread();
		}
	};
	
	private boolean isActive()
	{
		long callTimeDefaut = preferences.getLong( MIN_CALL_TIME_KEY , DEFAULT_MIN_CALL_TIME );
		long callDateIntervalDefaut = preferences.getLong( MIN_CALL_DATE_KEY , DEFAULT_MIN_CALL_DATE );
		long smsNumDefaut = preferences.getLong( MIN_SMS_NUMBER_KEY , DEFAULT_SMS_NUMBER );
		long callTime = UtilsBase.getInstance( mContext ).getTotalCallTime( callTimeDefaut );
		long callDate = UtilsBase.getInstance( mContext ).getEarliestCallDate( callDateIntervalDefaut );
		long currentTimeMillis = System.currentTimeMillis();
		long callDateInterval = currentTimeMillis - callDate;
		int smsNum = UtilsBase.getInstance( mContext ).getSmsNum( smsNumDefaut );
		//如果返回值为-1，则为获取失败，三个条件都失败，激活失败，否则，比较另外的条件
		if( UtilsBase.showOperateLog )
			Log.v(
					"OPCenter" ,
					"OPCenter--------isActive " + "\ncallTime--------      " + callTime + "\ncallTimeDefaut--------" + callTimeDefaut + "\ncallDate--------" + callDate + "\ncallDateInterval--------      " + callDateInterval + "\ncallDateIntervalDefaut--------" + callDateIntervalDefaut + "\nsmsNum--------      " + smsNum + "\nsmsNumDefaut--------" + smsNumDefaut );
		if( callTime == -1 )
		{
			if( callDate == -1 )
			{
				if( smsNum == -1 )
				{
					//三个条件都获取失败，激活失败
					return false;
				}
				else
				{
					if( (long)smsNum >= smsNumDefaut )
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
			else
			{
				if( callDateInterval < 0 )
				{
					callDateInterval = -callDateInterval;
				}
				if( smsNum == -1 )
				{
					if( callDateInterval >= callDateIntervalDefaut )
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				else
				{
					if( callDateInterval >= callDateIntervalDefaut && (long)smsNum >= smsNumDefaut )
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
		}
		else
		{
			if( callDate == -1 )
			{
				if( smsNum == -1 )
				{
					if( callTime >= callTimeDefaut )
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				else
				{
					if( callTime >= callTimeDefaut && (long)smsNum >= smsNumDefaut )
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
			else
			{
				if( callDateInterval < 0 )
				{
					callDateInterval = -callDateInterval;
				}
				if( smsNum == -1 )
				{
					if( callTime >= callTimeDefaut && callDateInterval >= callDateIntervalDefaut )
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				else
				{
					if( callTime >= callTimeDefaut && callDateInterval >= callDateIntervalDefaut && (long)smsNum >= smsNumDefaut )
					{
						return true;
					}
					else
					{
						return false;
					}
				}
			}
		}
		//	    return true;
	}
	
	private void checkThread()
	{
		synchronized( threadSync )
		{
			if( handlerThread == null )
			{
				handlerThread = new HandlerThread( "handlerThread" );
				handlerThread.start();
				mHandler = new Handler( handlerThread.getLooper() );
				if( UtilsBase.showOperateLog )
					Log.v( "OPCenter" , "OPCenter--------checkThread" );
			}
		}
	}
	
	private void exitThread()
	{
		synchronized( threadSync )
		{
			if( handlerThread != null )
			{
				handlerThread.quit();
				handlerThread = null;
				mHandler = null;
				if( UtilsBase.showOperateLog )
					Log.v( "OPCenter" , "OPCenter--------exitThread" );
			}
		}
	}
	
	private boolean requestConfig()
	{
		Log.i( "OPCenter" , "OPCenter--------request config!" );
		if( UtilsBase.showOperateLog )
			Log.v( "OPCenter" , "OPCenter--------request config" );
		boolean result = true;
		String url = DownloadList.SERVER_URL_TEST;
		String params = getParams( REQUEST_ACTION_CONFIG );
		//Log.i("statistics", "post count:"+logInfo);
		if( params != null )
		{
			CustomerHttpClient client = new CustomerHttpClient( mContext );
			ResultEntity res = client.postEntity( url , params );
			if( res.exception != null )
			{
				if( UtilsBase.showOperateLog )
					Log.v( "OPCenter" , "OPCenter--------request config exception:" + res.exception.getMessage() );
				return false;
			}
			else
			{
				String content = res.content;
				JSONObject json = null;
				try
				{
					json = new JSONObject( content );
					int retCode = json.getInt( "retcode" );
					if( retCode == 0 )
					{
						long timeStamp = Long.parseLong( json.getString( "timestamp" ) );
						//String configUrl = json.getString( "url" );
						// @gaominghui2015/02/28 ADD START 把getString改成用optString防止出现json解析异常
						String configUrl = json.optString( "url" );
						// @gaominghui2015/02/28 ADD END
						preferences.edit().putLong( CONFIG_TIME_STAMP_KEY , timeStamp ).commit();
						result = downloadConfig( configUrl );
					}
				}
				catch( JSONException e )
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			result = false;
		}
		return result;
	}
	
	private String getParams(
			int action )
	{
		String appid = null;
		String sn = null;
		PackageManager pm;
		JSONObject res;
		int networktype = -1;
		int networksubtype = -1;
		ConnectivityManager connMgr = (ConnectivityManager)mContext.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
		if( netInfo != null )
		{
			networktype = netInfo.getType();
			networksubtype = netInfo.getSubtype();
		}
		String productname = "uipersonalcenter";
		switch( action )
		{
			case REQUEST_ACTION_CONFIG:
				appid = Assets.getAppId( mContext );
				sn = Assets.getSerialNo( mContext );
				if( appid == null || sn == null )
					return null;
				if( UtilsBase.showOperateLog )
					Log.v( "OPCenter" , "OPCenter-------sn = " + sn );
				pm = mContext.getPackageManager();
				res = new JSONObject();
				try
				{
					res.put( "Action" , REQUEST_ACTION_CONFIG + "" );
					res.put( "packname" , mContext.getPackageName() );
					res.put( "versioncode" , pm.getPackageInfo( mContext.getPackageName() , 0 ).versionCode );
					res.put( "versionname" , pm.getPackageInfo( mContext.getPackageName() , 0 ).versionName );
					res.put( "sn" , sn );
					res.put( "appid" , appid );
					res.put( "shellid" , CooeeSdk.cooeeGetCooeeId( mContext ) );
					res.put( "timestamp" , 0/*preferences.getLong( CONFIG_TIME_STAMP_KEY , 0 ) */);
					res.put( "uuid" , Installation.id( mContext ) );
					TelephonyManager mTelephonyMgr = (TelephonyManager)mContext.getSystemService( Context.TELEPHONY_SERVICE );
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
					res.put( "androidid" , android.provider.Settings.Secure.getString( mContext.getContentResolver() , android.provider.Settings.Secure.ANDROID_ID ) );
					res.put( "buildtime" , Build.TIME );
					res.put( "heightpixels" , mContext.getResources().getDisplayMetrics().heightPixels );
					res.put( "widthpixels" , mContext.getResources().getDisplayMetrics().widthPixels );
					res.put( "networktype" , networktype );
					res.put( "networksubtype" , networksubtype );
					res.put( "producttype" , 4 );
					res.put( "productname" , productname );
					res.put( "count" , 0 );
					res.put( "opversion" , DownloadList.getVersion() );
					String content = res.toString();
					String md5_res = DownloadList.getMD5EncruptKey( content + DEFAULT_KEY );
					//res.put("md5", md5_res);
					String newContent = content.substring( 0 , content.lastIndexOf( '}' ) );
					String params = newContent + ",\"md5\":\"" + md5_res + "\"}";
					return params;
				}
				catch( Exception e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			case REQUEST_ACTION_CONTENT:
				break;
		}
		return null;
	}
	
	private boolean downloadConfig(
			String url )
	{
		if( UtilsBase.showOperateLog )
			Log.v( "OPCenter" , "OPCenter--------download config" );
		ResultEntity res = CustomerHttpClient.sendGetEntity( url , null );
		if( res.exception != null )
		{
			//			preferences.edit().putString(FOLDER_CONFIG_ERROR,res.exception.getMessage()).commit();
			Log.v( "OPCenter" , "OPCenter--------download config = " + res.exception );
			return false;
		}
		else
		{
			JSONObject json = null;
			try
			{
				json = new JSONObject( res.content );
				System.out.println( "res.content = " + res.exception );
				Log.v( "OPCenter" , "OPCenter-------json = " + json );
				long interval = json.getInt( "update_interval" );
				Log.v( "OPCenter" , "OPCenter-------interval = " + interval );
				long minCallTime = json.getInt( "min_call_time" ) * 60 * 60 * 1000;
				long minCallDate = ( (long)( json.getInt( "min_call_date" ) ) ) * 24 * 60 * 60 * 1000;
				long minSmsNum = json.getInt( "min_sms_number" );
				System.out.println( "minCallTime = " + minCallTime + " minCallDate = " + minCallDate + " minSmsNum = " + minSmsNum );
				int isshow = json.getInt( GAMETAB_ISSHOW );
				preferences.edit().putLong( UPDATEINTERVAL_KEY , interval ).commit();
				preferences.edit().putLong( MIN_CALL_TIME_KEY , minCallTime ).commit();
				preferences.edit().putLong( MIN_CALL_DATE_KEY , minCallDate ).commit();
				preferences.edit().putLong( MIN_SMS_NUMBER_KEY , minSmsNum ).commit();
				preferences.edit().putInt( GAMETAB_ISSHOW , isshow ).commit();
				Log.v( "OPCenter" , "OPCenter--------download config  isshow = " + isshow );
			}
			catch( Exception e )
			{
				//				preferences.edit().putString(FOLDER_CONFIG_ERROR,res.httpCode + ";" + e.getMessage()).commit();
				e.printStackTrace();
				return false;
			}
		}
		//			preferences.edit().putString(FOLDER_CONFIG_ERROR,null).commit();
		if( UtilsBase.showOperateLog )
			Log.v( "OPCenter" , "OPCenter--------download config success" );
		return true;
	}
	
	public void addShortcut()
	{
		Intent shortcut = new Intent( "com.android.launcher.action.INSTALL_SHORTCUT" );
		shortcut.putExtra( Intent.EXTRA_SHORTCUT_NAME , mContext.getString( R.string.shortcut ) );
		shortcut.putExtra( "duplicate" , false );
		Intent newintent = new Intent();
		String appClass = "com.coco.shortcut.shortcutbox.OperateCenterActivity";
		ComponentName comp = new ComponentName( mContext.getPackageName() , appClass );
		newintent.setComponent( comp );
		shortcut.putExtra( Intent.EXTRA_SHORTCUT_INTENT , newintent );
		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext( mContext , R.drawable.shortcut );
		shortcut.putExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE , iconRes );
		mContext.sendBroadcast( shortcut );
		preferences.edit().putLong( "addshortcut" , System.currentTimeMillis() ).commit();
		checkState( mContext , true );
		DownloadList.getInstance( mContext ).startUICenterLog( "0034" , null , null );
	}
	
	public void removeShortcut()
	{
		Intent shortcut = new Intent();
		shortcut.putExtra( Intent.EXTRA_SHORTCUT_NAME , mContext.getString( R.string.shortcut ) );
		shortcut.putExtra( "duplicate" , false );
		Intent newintent = new Intent();
		String appClass = "com.coco.shortcut.shortcutbox.OperateCenterActivity";
		ComponentName comp = new ComponentName( mContext.getPackageName() , appClass );
		newintent.setComponent( comp );
		shortcut.putExtra( Intent.EXTRA_SHORTCUT_INTENT , newintent );
		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext( mContext , R.drawable.shortcut );
		shortcut.putExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE , iconRes );
		shortcut.setAction( "com.android.launcher.action.UNINSTALL_SHORTCUT" );
		mContext.sendBroadcast( shortcut );
		checkState( mContext , false );
	}
	
	private Handler mHandler1 = new Handler() {
		//		public void handleMessage(
		//				android.os.Message msg )
		//		{
		//			checkState( mContext , false );
		//		};
	};
	
	private void checkState(
			Context context ,
			boolean isenable )
	{
		PackageManager pkgMgr = context.getPackageManager();
		if( !isenable )
		{
			setComponentEnabled( pkgMgr , new ComponentName( context , OperateCenterActivity.class ) , PackageManager.COMPONENT_ENABLED_STATE_DISABLED );
		}
		else
		{
			setComponentEnabled( pkgMgr , new ComponentName( context , OperateCenterActivity.class ) , PackageManager.COMPONENT_ENABLED_STATE_ENABLED );
		}
	}
	
	private void setComponentEnabled(
			PackageManager pkgMgr ,
			ComponentName compName ,
			int newStat )
	{
		if( pkgMgr.getComponentEnabledSetting( compName ) != newStat )
		{
			pkgMgr.setComponentEnabledSetting( compName , newStat , PackageManager.DONT_KILL_APP );
		}
	}
	
	public boolean isWatchNetwork()
	{
		return watchNetwork;
	}
	
	public void setWatchNetwork(
			boolean watchNetwork )
	{
		this.watchNetwork = watchNetwork;
	}
	
	public PendingIntent getPendingIntent()
	{
		return pi;
	}
	
	public void setPendingIntent(
			PendingIntent pi )
	{
		this.pi = pi;
	}
}
