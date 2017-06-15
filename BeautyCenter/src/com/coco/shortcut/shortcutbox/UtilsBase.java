package com.coco.shortcut.shortcutbox;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.util.Log;


public class UtilsBase
{
	
	public static Context mContext;
	public long resumeTime;
	public long pauseTime;
	private long totalCallTime = -1;
	private long earliestCallDate = -1;
	private int totalSmsNum = -1;//xiatian add	//OperateFolder	
	Object exe_lock = new Object();
	public static final String ACTION_HOT_CHANGED = "com.coco.action.HOTOPERATE_CHANGED";
	public static final String ACTION_START_DOWNLOAD_APK = "com.coco.operate.action.START_DOWNLOAD_APK";
	public static final String ACTION_PAUSE_DOWNLOAD_APK = "com.coco.operate.action.PAUSE_DOWNLOAD_APK";
	public static final String ACTION_THUMB_CHANGED = "com.coco.operate.action.THUMB_CHANGED";
	public static final String ACTION_PREVIEW_CHANGED = "com.coco.operate.action.PREVIEW_CHANGED";
	public static final String ACTION_DOWNLOAD_STATUS_CHANGED = "com.coco.operate.action.DOWNLOAD_STATUS_CHANGED";
	public static final String ACTION_DOWNLOAD_SIZE_CHANGED = "com.coco.operate.action.DOWNLOAD_SIZE_CHANGED";
	public static final String EXTRA_PACKAGE_NAME = "PACKAGE_NAME";
	public static final String EXTRA_CLASS_NAME = "CLASS_NAME";
	public static final String EXTRA_DOWNLOAD_SIZE = "EXTRA_DOWNLOAD_SIZE";
	public static final String EXTRA_TOTAL_SIZE = "EXTRA_TOTAL_SIZE";
	public static final boolean showOperateLog = true;
	private static UtilsBase mInstance;
	
	public static UtilsBase getInstance(
			Context context )
	{
		if( mInstance == null )
		{
			mInstance = new UtilsBase();
		}
		mContext = null;
		mContext = context;
		return mInstance;
	}
	
	public void init(
			Context _activity )
	{
		mContext = _activity;
	}
	
	public long getTotalCallTime()
	{
		if( totalCallTime != -1 )
			return totalCallTime;
		initCallData();
		return totalCallTime;
	}
	
	public long getTotalCallTime(
			long max_totalCallTime )
	{
		if( totalCallTime != -1 && totalCallTime >= max_totalCallTime )
			return totalCallTime;
		initCallData();
		return totalCallTime;
	}
	
	public long getEarliestCallDate()
	{
		if( earliestCallDate != -1 )
			return earliestCallDate;
		initCallData();
		return earliestCallDate;
	}
	
	public long getEarliestCallDate(
			long max_callDateInterval )
	{
		long currentTimeMillis = System.currentTimeMillis();
		if( earliestCallDate != -1 && ( currentTimeMillis - earliestCallDate ) >= max_callDateInterval )
			return earliestCallDate;
		initCallData();
		return earliestCallDate;
	}
	
	public void initCallData()
	{
		Cursor cursor = null;
		try
		{
			cursor = mContext.getContentResolver().query( Calls.CONTENT_URI , new String[]{ Calls.DURATION , Calls.TYPE , Calls.DATE } , null , null , Calls.DEFAULT_SORT_ORDER );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return;
		}
		//		long incoming = 0L;
		//		long outgoing = 0L;
		if( cursor != null )
		{
			try
			{
				boolean hasRecord = cursor.moveToFirst();
				while( hasRecord )
				{
					int type = cursor.getInt( cursor.getColumnIndex( Calls.TYPE ) );
					long duration = cursor.getLong( cursor.getColumnIndex( Calls.DURATION ) );
					long date = cursor.getLong( cursor.getColumnIndex( Calls.DATE ) );
					switch( type )
					{
						case Calls.INCOMING_TYPE:
							//						incoming += duration;
							//						break;
						case Calls.OUTGOING_TYPE:
							//						outgoing += duration;
							if( totalCallTime == -1 )
							{
								totalCallTime = 0;
							}
							totalCallTime += duration;
						default:
							break;
					}
					if( date < earliestCallDate )
						earliestCallDate = date;
					if( earliestCallDate == -1 )
						earliestCallDate = date;
					hasRecord = cursor.moveToNext();
				}
			}
			finally
			{
				cursor.close();
			}
		}
		//		totalCallTime = (incoming + outgoing);
		Log.v( "call" , "callog time=" + totalCallTime + " date=" + earliestCallDate );
	}
	
	public String sync_do_exec(
			String cmd )
	{
		String s = "\n";
		try
		{
			java.lang.Process p = Runtime.getRuntime().exec( cmd );
			BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
			String line = null;
			while( ( line = in.readLine() ) != null )
			{
				s += line + "\n";
			}
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
	
	public boolean do_exec(
			final String cmd ,
			String packageName )
	{
		boolean success = false;
		new Thread() {
			
			@Override
			public void run()
			{
				String s = "\n";
				try
				{
					java.lang.Process p = Runtime.getRuntime().exec( cmd );
					BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
					String line = null;
					while( ( line = in.readLine() ) != null )
					{
						s += line + "\n";
					}
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				super.run();
				synchronized( exe_lock )
				{
					Log.d( "apk" , "exe_lock notify" );
					exe_lock.notify();
				}
			}
		}.start();
		int i = 0;
		PackageManager pm = mContext.getPackageManager();
		synchronized( exe_lock )
		{
			while( !success && i < 12 )
			{
				Log.d( "apk" , "exe_lock wait" );
				try
				{
					exe_lock.wait( 10000 );
					Log.d( "apk" , "exe_lock wait finish" );
				}
				catch( InterruptedException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if( packageName != null )
				{
					try
					{
						pm.getPackageInfo( packageName , PackageManager.GET_ACTIVITIES );
						Log.e( "apk" , "has install,do not wait:" + packageName );
						success = true;
						break;
					}
					catch( Exception e )
					{
						Log.e( "apk" , "wait again:" + packageName );
					}
				}
				i++;
			}
		}
		return success;
	}
	
	//xiatian add start	//OperateFolder
	public int getSmsNum(
			long max_SmsNum )
	{
		if( totalSmsNum != -1 && totalSmsNum >= max_SmsNum )
			return totalSmsNum;
		Cursor csr = null;
		try
		{
			csr = mContext.getContentResolver().query( Uri.parse( "content://sms" ) , null , "type = 1" , null , null );
			totalSmsNum = csr.getCount();
			Log.i( "OPFolder" , "sms=" + totalSmsNum );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( csr != null )
				csr.close();
		}
		return totalSmsNum;
	}
}
