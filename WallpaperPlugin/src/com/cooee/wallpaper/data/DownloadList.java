package com.cooee.wallpaper.data;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.wallpaper.manager.ChangeWallpaperManager;
import com.cooee.wallpaper.util.Assets;
import com.cooee.wallpaper.util.ThreadUtil;
import com.cooee.wallpaper.util.Tools;

import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;


public class DownloadList
{
	
	private final String DEFAULT_KEY = "f24657aafcb842b185c98a9d3d7c6f4725f6cc4597c3a4d531c70631f7c7210fd7afd2f8287814f3dfa662ad82d1b02268104e8ab3b2baee13fab062b3d27bff";
	//测试用的后台url
	//public static final String SERVER_URL_TEST = "http://192.168.1.225/iloong/pui/ServicesEngine/DataService";
	//private final String LOG_URL_TEST = "http://192.168.1.225/iloong/pui/LogEngine/DataService";
	//	public static final String SERVER_URL_TEST = "http://58.246.135.237:20180/iloong/pui/ServicesEngine/DataService";
	//	private final String LOG_URL_TEST = "http://58.246.135.237:20180/iloong/pui/LogEngine/DataService";
	//正式版本用的url
	public static final String SERVER_URL_TEST = "http://uifolder.coolauncher.com.cn/iloong/pui/ServicesEngine/DataService";
	private final String ACTION_LIST = "1300";
	private final String LOG_TAG = "DownloadList";
	public static final String ACTION_HOTLIST_CHANGED = "com.coco.action.HOTLIST_CHANGED";
	private final String WALLPAPER_LIST_DATE = "wallpaperListDate";
	//	private static DownloadList proxy;
	private Context mContext;
	private DownloadListThread downListThread = null;
	private Object syncObject = new Object();
	private List<WallpaperItemInfo> mInfos = new ArrayList<WallpaperItemInfo>();
	public static final String Wallpaper_Type = "2";
	public static final String FIELD_PACKAGE_NAME = "packageName";
	public static final String FIELD_RESURL = "resurl";
	public static final String FIELD_RESID = "resid";
	public static final String TABLE_NAME = "hotWallpaper";
	private WallpaperDbHelper dbHelper;
	private RequestQueue mQueue;
	public DownloadWallpaperCallbacks mDownloadWallpaperCallbacks;
	//sd卡中保存的网络壁纸路径
	public static final String save_wallpaper_bitmap_path = "/pl_ad_wallpapers";
	
	public DownloadList(
			Context context ,
			DownloadWallpaperCallbacks callbacks )
	{
		mContext = context;
		dbHelper = new WallpaperDbHelper( mContext );
		mQueue = Volley.newRequestQueue( context );
		mDownloadWallpaperCallbacks = callbacks;
	}
	
	public void downList()
	{
		synchronized( this.syncObject )
		{
			if( downListThread == null )
			{
				downListThread = new DownloadListThread();
				downListThread.start();
			}
		}
	}
	
	public void getWallpaperOnLine()
	{
		ThreadUtil.execute( new Runnable() {
			
			@Override
			public void run()
			{
				if( isRefreshList() )
				{
					downList();
				}
				else
				{
					// TODO Auto-generated method stub
					if( mInfos.size() == 0 )
						queryWallpaperList();
					handleImageList();
					Log.v( LOG_TAG , "handlerImageList = " + Thread.currentThread() );
				}
			}
		} );
	}
	
	@SuppressWarnings( "deprecation" )
	public void handleImageList()
	{
		for( WallpaperItemInfo info : mInfos )
		{
			//一次只下一张图片
			if( getExistFile( mContext , info.getPackname() ) == null )
			{
				final String packageName = info.getPackname();
				ImageRequest request = new ImageRequest( info.getResUrl() , new Response.Listener<Bitmap>() {
					
					@Override
					public void onResponse(
							final Bitmap response )
					{
						ThreadUtil.execute( new Runnable() {
							
							@Override
							public void run()
							{
								// TODO Auto-generated method stub
								Log.v( LOG_TAG , "getImage success  " + response.getWidth() + " id = " + Thread.currentThread() );
								saveBitmap( mContext , response , packageName + ".tupian" );
								if( mDownloadWallpaperCallbacks != null )
								{
									mDownloadWallpaperCallbacks.changeDownloadImage( response , packageName );
								}
							}
						} );
					}
				} , mContext.getResources().getDisplayMetrics().widthPixels * 2 , 0 , Bitmap.Config.RGB_565 , new Response.ErrorListener() {
					
					public void onErrorResponse(
							VolleyError arg0 )
					{
						Log.v( LOG_TAG , "error = " + arg0.getMessage() );
						ThreadUtil.execute( new Runnable() {
							
							@Override
							public void run()
							{
								// TODO Auto-generated method stub
								if( mDownloadWallpaperCallbacks != null )
								{
									mDownloadWallpaperCallbacks.changeLocalImage();
								}
							}
						} );
					};
				} );
				mQueue.add( request );
				return;
			}
		}
		//运行到这里说明数据库中的所有壁纸都已经缓存到sd卡中，直接本地换壁纸
		if( mDownloadWallpaperCallbacks != null )
		{
			mDownloadWallpaperCallbacks.changeLocalImage();
		}
	}
	
	public void dispose()
	{
		stopDownloadList();
		mInfos.clear();
	}
	
	public void stopDownloadList()
	{
		if( downListThread != null )
		{
			downListThread.stopRun();
			downListThread = null;
		}
	}
	
	private void downloadListFinish()
	{
		Log.v( LOG_TAG , "downloadListFinish:" + ACTION_HOTLIST_CHANGED );
		if( mInfos.size() > 0 )
		{
			handleImageList();
			batchInsert( mInfos );
			saveListTime();
		}
	}
	
	private void saveListTime()
	{
		//		Time curTime = new Time();
		//		String curDateString = curTime.format( "yyyyMMdd" );
		SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMdd" );
		Date curDate = new Date( System.currentTimeMillis() );//获取当前时间
		String curDateString = formatter.format( curDate );
		SharedPreferences sharedPrefer = PreferenceManager.getDefaultSharedPreferences( mContext );
		Editor edit = sharedPrefer.edit();
		edit.putString( WALLPAPER_LIST_DATE , curDateString );
		edit.commit();
	}
	
	public boolean isRefreshList()
	{
		SharedPreferences sharedPrefer = PreferenceManager.getDefaultSharedPreferences( mContext );
		String downListDate = sharedPrefer.getString( WALLPAPER_LIST_DATE , "" );
		//		Time curTime = new Time();
		//		String curDateString = curTime.format( "yyyyMMdd" );
		SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMdd" );
		Date curDate = new Date( System.currentTimeMillis() );//获取当前时间
		String curDateString = formatter.format( curDate );
		if( curDateString.equals( downListDate ) )
		{
			return false;
		}
		return true;
	}
	
	/*
	 * 下载列表的线程
	 */
	private class DownloadListThread extends Thread
	{
		
		private volatile boolean isExit = false;
		
		public DownloadListThread()
		{
		}
		
		public void stopRun()
		{
			isExit = true;
		}
		
		@Override
		public void run()
		{
			mInfos.clear();//清除缓存数据
			String url = SERVER_URL_TEST;
			Log.v( LOG_TAG , "SERVER_URL_TEST = " + SERVER_URL_TEST );
			String params = getParams( ACTION_LIST , true );
			Log.v( LOG_TAG , "SERVER_URL_TEST params = " + params );
			boolean isSucceed = false;
			if( params != null )
			{
				ResultEntity res = CoolHttpClient.postEntity( url , params );
				if( isExit )
				{
					synchronized( syncObject )
					{
						downListThread = null;
						return;
					}
				}
				Log.v( LOG_TAG , "SERVER_URL_TEST res = " + res.exception + " res.content " + res.content );
				if( res.exception != null )
				{
					handleImageList();
				}
				else
				{
					String content = res.content;
					if( content.contains( "fatal error:" ) )
					{
						content = content.replace( "fatal error:" , "" );
					}
					Log.v( LOG_TAG , "content = " + content );
					try
					{
						JSONObject orgjson = new JSONObject( content );
						Log.v( "LOG_TAG" , "json = " + orgjson.toString() );
						int retCode = orgjson.getInt( "retcode" );
						if( retCode == 0 )
						{
							String configUrl = orgjson.getString( "reslist" );
							JSONObject json = new JSONObject( configUrl );
							for( Iterator iter = json.keys() ; iter.hasNext() ; )
							{
								String key = (String)iter.next();
								//Tools.writelogTosd( key + "\n" );
								JSONObject tmJson = (JSONObject)json.get( key );
								if( !tmJson.getString( "tabid" ).equals( Wallpaper_Type ) )
								{
									continue;
								}
								Log.v( LOG_TAG , "tmJson = " + tmJson );
								for( Iterator mIterator = tmJson.keys() ; mIterator.hasNext() ; )
								{
									String mKey = (String)mIterator.next();
									if( mKey.equals( "tabid" ) )
									{
									}
									else if( mKey.equals( "enname" ) )
									{
									}
									else if( mKey.equals( "cnname" ) )
									{
									}
									else if( mKey.equals( "twname" ) )
									{
									}
									else if( mKey.equals( "typeid" ) )
									{
									}
									else
									{
										JSONObject jsObj = (JSONObject)tmJson.get( mKey );
										WallpaperItemInfo itemInfo = new WallpaperItemInfo();
										itemInfo.setResId( jsObj.getString( "resid" ) );
										itemInfo.setResUrl( jsObj.getString( "resurl" ) );
										itemInfo.setPackname( jsObj.getString( "packname" ) );
										itemInfo.setSize( jsObj.getString( "size" ) );
										mInfos.add( itemInfo );
									}
								}
							}
							isSucceed = true;
						}
					}
					catch( JSONException e )
					{
						//Log.i( "downloadlist" , "JSONException 4= " + e.getStackTrace() );
						e.printStackTrace();
					}
				}
			}
			if( isSucceed )
			{
				downloadListFinish();
			}
			synchronized( syncObject )
			{
				downListThread = null;
				return;
			}
		}
	}
	
	private String getShellID()
	{
		//测试用的
		//return "R001_TESTAFAE";
		//正式版本用的
		return CooeeSdk.cooeeGetCooeeId( mContext );
	}
	
	public static String getMD5EncruptKey(
			String logInfo )
	{
		String res = null;
		MessageDigest messagedigest;
		try
		{
			messagedigest = MessageDigest.getInstance( "MD5" );
		}
		catch( NoSuchAlgorithmException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		messagedigest.update( logInfo.getBytes() );
		res = bufferToHex( messagedigest.digest() );
		// Log.v("http", "getMD5EncruptKey res =  " + res);
		return res;
	}
	
	protected static char hexDigits[] = { '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' , '9' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f' };
	
	private static String bufferToHex(
			byte bytes[] )
	{
		return bufferToHex( bytes , 0 , bytes.length );
	}
	
	private static String bufferToHex(
			byte bytes[] ,
			int m ,
			int n )
	{
		StringBuffer stringbuffer = new StringBuffer( 2 * n );
		int k = m + n;
		for( int l = m ; l < k ; l++ )
		{
			appendHexPair( bytes[l] , stringbuffer );
		}
		return stringbuffer.toString();
	}
	
	private static void appendHexPair(
			byte bt ,
			StringBuffer stringbuffer )
	{
		char c0 = hexDigits[( bt & 0xf0 ) >> 4]; // 取字节中�?4 位的数字转换, >>>
													// 为逻辑右移，将符号位一起右�?此处未发现两种符号有何不�?
		char c1 = hexDigits[bt & 0xf]; // 取字节中�?4 位的数字转换
		stringbuffer.append( c0 );
		stringbuffer.append( c1 );
	}
	
	private String getParams(
			String logText ,
			boolean isAddMd5 )
	{
		String action = null;
		String[] itemsTemp = logText.split( "#" );
		int len = itemsTemp.length;
		if( len > 0 )
		{
			action = itemsTemp[0];
		}
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
		JSONObject tmp = Assets.config;
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
		if( ChangeWallpaperManager.Online_wallpaper_from == ChangeWallpaperManager.ONLINE_WALLPAPER_FROM_NANO )
		{
			appid = "f5619";
		}
		Log.v( LOG_TAG , "sn = " + sn );
		if( appid == null || sn == null )
			return null;
		pm = mContext.getPackageManager();
		res = new JSONObject();
		try
		{
			res.put( "Action" , action );
			if( isAddMd5 )
			{
				res.put( "packname" , mContext.getPackageName() );
				res.put( "versioncode" , pm.getPackageInfo( mContext.getPackageName() , 0 ).versionCode );
				res.put( "versionname" , pm.getPackageInfo( mContext.getPackageName() , 0 ).versionName );
				res.put( "sn" , sn );
				res.put( "appid" , appid );
				res.put( "shellid" , getShellID() );
				res.put( "timestamp" , 0 );
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
				res.put( "productname" , "uipersonalcenter" );
				res.put( "count" , 0 );
				res.put( "opversion" , "1.0.0.1" );
				Log.v( "downloadlist" , logText + "下载线程启动" );
			}
			String content = res.toString();
			String params = content;
			if( isAddMd5 )
			{
				String md5_res = getMD5EncruptKey( content + DEFAULT_KEY );
				// res.put("md5", md5_res);
				String newContent = content.substring( 0 , content.lastIndexOf( '}' ) );
				params = newContent + ",\"md5\":\"" + md5_res + "\"}";
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
	
	public static void makeDir(
			String path )
	{
		try
		{
			new File( path ).mkdirs();
		}
		catch( SecurityException e )
		{
			e.printStackTrace();
		}
	}
	
	public static void saveBitmap(
			Context context ,
			Bitmap mBitmap ,
			String mSaveName )
	{
		String mSavePath;
		if( Tools.isSDCardExist() )
		{
			mSavePath = Environment.getExternalStorageDirectory().toString();
		}
		else
		{
			mSavePath = context.getFilesDir().toString();
		}
		//保存Bitmap   
		FileOutputStream fos = null;
		try
		{
			File path = new File( mSavePath + save_wallpaper_bitmap_path );
			//文件  
			String filepath = mSavePath + save_wallpaper_bitmap_path + "/" + mSaveName;
			File file = new File( filepath );
			if( !path.exists() )
			{
				path.mkdirs();
			}
			if( !file.exists() )
			{
				file.createNewFile();
			}
			fos = new FileOutputStream( file );
			if( fos != null )
			{
				mBitmap.compress( Bitmap.CompressFormat.PNG , 90 , fos );
				fos.flush();
				fos.close();
			}
			Log.v( "DownloadList" , "saveBitmap  success = " + filepath );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( fos != null )
			{
				try
				{
					fos.flush();
					fos.close();
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String getExistFile(
			Context context ,
			String pkgname )
	{
		if( Tools.isSDCardExist() )
		{
			File file = new File( Environment.getExternalStorageDirectory().toString() + save_wallpaper_bitmap_path );//检查sd卡下面
			if( file != null && file.exists() && file.isDirectory() )
			{
				for( String item : file.list() )
				{
					if( item.contains( pkgname ) )
					{
						String path = Environment.getExternalStorageDirectory().toString() + save_wallpaper_bitmap_path + "/" + item;
						File temp = new File( path );
						if( temp.length() > 0 )
							return path;
					}
				}
			}
		}
		File file = new File( context.getFilesDir().toString() + save_wallpaper_bitmap_path );//当sd卡不存在时，图片会存放到data下面，也去检查
		if( file != null && file.exists() && file.isDirectory() )
		{
			for( String item : file.list() )
			{
				if( item.contains( pkgname ) )
				{
					String path = context.getFilesDir().toString() + save_wallpaper_bitmap_path + "/" + item;
					File temp = new File( path );
					if( temp.length() > 0 )
						return path;
				}
			}
		}
		return null;
	}
	
	public static String getSDCardPath(
			Context context )
	{
		if( context == null )
		{
			return null;
		}
		File sdcardDir = null;
		//获取T卡是否准备就绪  
		if( Tools.isSDCardExist() )
		{
			sdcardDir = Environment.getExternalStorageDirectory();
			return sdcardDir.toString();
		}
		return null;
	}
	
	public static String getCreateSql()
	{
		String result = String//返回一个本地化的格式化字符串,使用提供的格式和参数,使用用户的缺省语言环境。
				.format( "CREATE TABLE %s (%s TEXT , %s TEXT, %s TEXT );" , TABLE_NAME , FIELD_PACKAGE_NAME , FIELD_RESURL , FIELD_RESID );
		return result;
	}
	
	public static String getDropSql()
	{
		String result = "DROP TABLE IF EXISTS " + TABLE_NAME;
		return result;
	}
	
	public boolean batchInsert(
			List<WallpaperItemInfo> list )
	{
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try
		{
			db.beginTransaction();//开启数据库事物
			ContentValues cv = new ContentValues();
			for( WallpaperItemInfo info : list )
			{
				cv.put( FIELD_PACKAGE_NAME , info.getPackname() );
				cv.put( FIELD_RESURL , info.getResUrl() );
				cv.put( FIELD_RESID , info.getResId() );
				long insertResult = db.insert( TABLE_NAME , null , cv );//直接向数据库总插入一行
				// @gaominghui2015/03/27 ADD START 由于从服务器拿到了重复包名的主题apk导致插入数据库失败从而整个热门主题不显示
				if( insertResult == -1 )
				{
					Log.e( "HotService" , "insert database error!" );
				}
				// @gaominghui2015/03/27 ADD END
			}
			db.setTransactionSuccessful();//将当前事务标记为成功。不做任何更多的数据库调用,调用endTransaction之间的工作。做尽可能少的非数据库工作在这种情况下。如果遇到任何错误,endTransaction事务仍将提交。
			return true;
		}
		catch( Exception e )
		{
			db.setTransactionSuccessful();
			e.printStackTrace();
			return false;
		}
		finally
		{
			db.endTransaction();//结束一个事物
			db.close();
			WallpaperDbHelper.lock_db = false;
		}
	}
	
	public synchronized void queryWallpaperList()
	{
		mInfos.clear();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		try
		{
			cursor = db.query( TABLE_NAME , null , null , null , null , null , null , null );
			while( cursor.moveToNext() )
			{
				WallpaperItemInfo item = new WallpaperItemInfo();
				item.setPackname( cursor.getString( cursor.getColumnIndexOrThrow( FIELD_PACKAGE_NAME ) ) );
				item.setResId( cursor.getString( cursor.getColumnIndexOrThrow( FIELD_RESID ) ) );
				item.setResUrl( cursor.getString( cursor.getColumnIndexOrThrow( FIELD_RESURL ) ) );
				mInfos.add( item );
			}
		}
		finally
		{
			if( cursor != null )
			{
				cursor.close();
			}
			db.close();
			WallpaperDbHelper.lock_db = false;
		}
	}
	
	public interface DownloadWallpaperCallbacks
	{
		
		public void changeDownloadImage(
				Bitmap bmp ,
				String pkgname );
		
		public void changeLocalImage();
	}
}
