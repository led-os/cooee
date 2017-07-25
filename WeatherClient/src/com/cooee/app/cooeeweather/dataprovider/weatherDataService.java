package com.cooee.app.cooeeweather.dataprovider;


import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.text.format.Time;

import com.cooee.app.cooeeweather.dataentity.PostalCodeEntity;
import com.cooee.app.cooeeweather.dataentity.SettingEntity;
import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataprovider.weatherwebservice.FLAG_UPDATE;
import com.cooee.app.cooeeweather.filehelp.Log;
import com.cooee.app.cooeeweather.util.ResolverUtil;
import com.cooee.widget.samweatherclock.AppConfig;


// public class weatherDataService extends Service implements Runnable {
public class weatherDataService extends IntentService
{
	
	private static String TAG = "weatherDataService";
	private static boolean isThreadRun = false;
	// private static Object sLock = new Object();
	private static Queue<Integer> requestWidgetIDs = new LinkedList<Integer>();
	public static final String ACTION_UPDATE_ALL = "com.cooee.weather.dataprovider.UPDATE_ALL";
	public static final String[] widgetProjection = new String[]{ weatherdataentity.IS_CONFIGURED , weatherdataentity.LAST_UPDATE_TIME , weatherdataentity.UPDATE_MILIS };
	public static final String NUM_COUNT_RECEIVER = "com.cooee.weather.datacom.action.NUM_COUNT";
	public static final String UPDATE_RESULT = "com.cooee.weather.data.action.UPDATE_RESULT";
	public static final String UPDATE_SETTING = "com.cooee.weather.data.action.UPDATE_SETTING";
	public static final String SETTING_URI = "content://com.cooee.app.cooeeweather.dataprovider/setting";
	private String mPostalCode = null;
	private String allPostalCode = null;
	private int mUserId = 0;
	private int allUserId = 0;
	private int mForcedUpdate = 0; // ǿ�и��У���ʹ��ʱ���������
	private String mPostMark = "";
	private int mUseWC = 0;
	private String mWCName = "";
	private SettingEntity mSettingEntity = new SettingEntity();
	private ProgressDialog mProgressDialog = null;
	private boolean foreignCity = false;
	
	public weatherDataService(
			String name )
	{
		super( "aaaaaaaaaa" );
	}
	
	public weatherDataService()
	{
		super( "aaaaaaaaaa" );
	}
	
	public void readSetting()
	{
		// ��ȡ����
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		boolean found = false;
		Uri uri = Uri.parse( SETTING_URI );
		cursor = resolver.query( uri , SettingEntity.projection , null , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				found = true;
			}
		}
		// ���û�ҵ����ã��趨Ĭ��ֵ
		if( !found )
		{
			// ��������Ĭ��ֵ
			ContentValues values = new ContentValues();
			// YANGTIANYU@2016/11/25 UPD START
			//mSettingEntity.setUpdateWhenOpen( 0 );
			mSettingEntity.setUpdateWhenOpen( AppConfig.getInstance( this ).isUpdateWhenOpen() );
			// YANGTIANYU@2016/11/25 UPD END
			mSettingEntity.setUpdateRegularly( 1 );
			mSettingEntity.setUpdateInterval( 1 );
			mSettingEntity.setSoundEnable( 0 );
			mSettingEntity.setMainCity( "" );
			values.put( SettingEntity.UPDATE_WHEN_OPEN , mSettingEntity.getUpdateWhenOpen() );
			values.put( SettingEntity.UPDATE_REGULARLY , mSettingEntity.getUpdateRegularly() );
			values.put( SettingEntity.UPDATE_INTERVAL , mSettingEntity.getUpdateInterval() );
			values.put( SettingEntity.SOUND_ENABLE , mSettingEntity.getSoundEnable() );
			values.put( SettingEntity.MAINCITY , mSettingEntity.getMainCity() );
			resolver.insert( uri , values );
		}
		else
		{
			mSettingEntity.setUpdateWhenOpen( cursor.getInt( 0 ) );
			mSettingEntity.setUpdateRegularly( cursor.getInt( 1 ) );
			mSettingEntity.setUpdateInterval( cursor.getInt( 2 ) );
			mSettingEntity.setMainCity( cursor.getString( 3 ) );
			mSettingEntity.setSoundEnable( cursor.getInt( 4 ) );
		}
		if( cursor != null )
		{
			cursor.close();
		}
	}
	
	/**
	 * 
	 *
	 * @see android.app.IntentService#onBind(android.content.Intent)
	 * @auther gaominghui  2015年4月28日
	 */
	public void updateSetting(
			String city )
	{
		// 先在数据库中搜索
		Cursor cursor = null;
		String mainCity = null;
		Log.v( TAG , "update setting city = " + city );
		try
		{
			ContentResolver resolver = getContentResolver();
			Uri uri = Uri.parse( SETTING_URI );
			if( city != null && !city.equals( mainCity ) )
			{
				Log.v( TAG , "update setting city = " + city + "; mainCity = " + mainCity );
				ContentValues values = new ContentValues();
				values.put( SettingEntity.MAINCITY , city );
				int updateRows;
				updateRows = resolver.update( uri , values , null , null );
				Log.v( TAG , "update setting rows = " + updateRows );
			}
			cursor = resolver.query( uri , SettingEntity.projection , null , null , null );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
				{
					mainCity = cursor.getString( 3 );
					Log.v( TAG , "update setting cursor.getString( 3 ) = " + cursor.getString( 3 ) );
					Intent intent = new Intent();
					intent.setAction( UPDATE_SETTING );
					intent.putExtra( "maincity" , mainCity );
					Log.v( TAG , "update setting maincity = " + mainCity );
					sendBroadcast( intent );
					Log.v( TAG , "update setting sendBroadcast。。。。。。。。" );
				}
			}
			if( cursor != null )
			{
				cursor.close();
			}
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public IBinder onBind(
			Intent intent )
	{
		return null;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.i( "sss" , "weather data service onCreate" );
	}
	
	@Override
	public int onStartCommand(
			Intent intent ,
			int flags ,
			int startId )
	{
		super.onStartCommand( intent , flags , startId );
		Log.i( "sss" , "weather data service onStartCommand" );
		Log.v( "sss" , "Intent action = " + intent.getAction() );
		return START_REDELIVER_INTENT;
	}
	
	public void mySendBroadcast()
	{
		Intent intent = new Intent();
		intent.setAction( UPDATE_RESULT );
		intent.putExtra( "cooee.weather.updateResult.postalcode" , mPostalCode );
		intent.putExtra( "cooee.weather.updateResult.userId" , mUserId );
		if( weatherwebservice.Update_Result_Flag == FLAG_UPDATE.UPDATE_SUCCES )
		{
			intent.putExtra( "cooee.weather.updateResult" , "UPDATE_SUCCESED" );
		}
		else if( weatherwebservice.Update_Result_Flag == FLAG_UPDATE.AVAILABLE_DATA )
		{
			intent.putExtra( "cooee.weather.updateResult" , "AVAILABLE_DATA" );
		}
		else if( weatherwebservice.Update_Result_Flag == FLAG_UPDATE.INVILIDE_VALUE )
		{
			intent.putExtra( "cooee.weather.updateResult" , "INVILIDE_DATA" );
		}
		// add weijie 0530
		else if( weatherwebservice.Update_Result_Flag == FLAG_UPDATE.UPDATE_SREACH_SUCCES )
		{
			intent.putExtra( "cooee.weather.updateResult" , "UPDATE_SREACH_SUCCES" );
		}
		else if( weatherwebservice.Update_Result_Flag == FLAG_UPDATE.UPDATE_SREACH_FAILED )
		{
			intent.putExtra( "cooee.weather.updateResult" , "UPDATE_SREACH_FAILED" );
		}
		else if( weatherwebservice.Update_Result_Flag == FLAG_UPDATE.UPDATE_REF_SUCCES )
		{
			intent.putExtra( "cooee.weather.updateResult" , "UPDATE_REF_SUCCES" );
		}
		else if( weatherwebservice.Update_Result_Flag == FLAG_UPDATE.UPDATE_SREACH_FAILED )
		{
			intent.putExtra( "cooee.weather.updateResult" , "UPDATE_SREACH_FAILED" );
		}
		// add end
		else
		{
			intent.putExtra( "cooee.weather.updateResult" , "UPDATE_FAILED" );
		}
		sendBroadcast( intent );
	}
	
	public void allSendBroadcast()
	{
		if( weatherwebservice.Update_Result_Flag == FLAG_UPDATE.UPDATE_SUCCES )
		{
			Intent intent = new Intent();
			intent.setAction( UPDATE_RESULT );
			intent.putExtra( "cooee.weather.updateResult" , "UPDATE_SUCCESED" );
			intent.putExtra( "cooee.weather.updateResult.postalcode" , allPostalCode );
			intent.putExtra( "cooee.weather.updateResult.userId" , allUserId );
			sendBroadcast( intent );
		}
		else
		{
			Intent intent = new Intent();
			intent.setAction( UPDATE_RESULT );
			intent.putExtra( "cooee.weather.updateResult" , "UPDATE_FAILED" );
			intent.putExtra( "cooee.weather.updateResult.postalcode" , allPostalCode );
			intent.putExtra( "cooee.weather.updateResult.userId" , allUserId );
			sendBroadcast( intent );
		}
	}
	
	// @Override
	// public void run() {}
	/**
	 * function addWedgetIDs ����������UserId
	 */
	public static void addWidgetIDs(
			int[] widgetIDs )
	{
		// synchronized (sLock) {
		for( int id : widgetIDs )
		{
			Log.d( TAG , "add widget ID:" + id );
			requestWidgetIDs.add( id );
		}
		// }
	}
	
	/**
	 * function hasMoreWidgetIds �ж϶������Ƿ���UserId
	 */
	public static boolean hasMoreWidgetIDs()
	{
		// synchronized (sLock) {
		boolean hasMore = !requestWidgetIDs.isEmpty();
		if( !hasMore )
		{
			isThreadRun = hasMore;
		}
		return hasMore;
		// }
	}
	
	/**
	 * ��ȡ����ͷ������
	 */
	public static Integer nextWidgetIDs()
	{
		// synchronized (sLock) {
		if( requestWidgetIDs.peek() != null )
		{
			return requestWidgetIDs.poll();
		}
		else
		{
			return 0;
		}
		// }
	}
	
	@Override
	protected void onHandleIntent(
			Intent intent )
	{
		Log.i( "sss" , "onHandleIntent" );
		long now = System.currentTimeMillis();
		Log.i( TAG , "now = " + now );
		// from startCommand to here
		/*if( ACTION_UPDATE_ALL.equals( intent.getAction() ) )
		{
		}
		else
		{
		}*/
		// Only start processing thread if not already running
		// synchronized (sLock) {
		// if (!isThreadRun) {
		if( true )
		{
			// ��ò���
			mPostalCode = intent.getStringExtra( "postalCode" );
			mUserId = intent.getIntExtra( "userId" , 0 );
			mForcedUpdate = intent.getIntExtra( "forcedUpdate" , 0 );
			foreignCity = intent.getBooleanExtra( "foreignCity" , false );
			mPostMark = intent.getStringExtra( "postmark" );
			mUseWC = intent.getIntExtra( "wc" , 0 );
			mWCName = intent.getStringExtra( "wcname" );
			if( mPostalCode == null )
			{
				mPostalCode = "all";
			}
			if( mPostalCode.equals( "" ) || mPostalCode.equals( "none" ) )
			{
				mPostalCode = "all";
			}
			isThreadRun = true;
			// new Thread(this).start();
		}
		// ��ȡ����c 
		readSetting();
		if( true )
		{
			//			// 获得参数
			if( mPostalCode == null && mSettingEntity != null && mSettingEntity.getMainCity() != null )
				mPostalCode = mSettingEntity.getMainCity();
			isThreadRun = true;
			// new Thread(this).start();
		}
		if( mPostalCode.equals( "bootup" ) )
		{
		}
		else if( mPostalCode.equals( "all" ) )
		{
			// �Ը���ʱ�������еĳ������������ر�Ĺ㲥
			ContentResolver resolver = this.getContentResolver();
			Cursor cursor = null;
			int i = 0;
			String[] postalcode = null;
			int[] id = null;
			String[] postalwc = null;
			Uri uri = Uri.parse( "content://com.cooee.app.cooeeweather.dataprovider/postalCode" );
			cursor = resolver.query( uri , PostalCodeEntity.projection , null , null , null );
			if( cursor != null )
			{
				int count = cursor.getCount();
				postalcode = new String[count];
				postalwc = new String[count];
				id = new int[count];
				if( count == 0 )
				{
					// gaominghui@2016/03/22 ADD START
					//Log.i( TAG , " AppConfig.getInstance( this ).isPosition() = "+AppConfig.getInstance( this ).isPosition() );
					String mainCity = null;
					if( AppConfig.getInstance( this ).isPosition() )
					{
						ResolverUtil.addLocatedCity( this );//获取定位城市，并且写入postalcode表
						mainCity = ResolverUtil.getLocatedCity( this );//获取定位城市
						Log.i( TAG , " mainCity = " + mainCity );
					}
					else
					{
						mainCity = ResolverUtil.addDefaultCity( this );//获取默认城市，并且写入postalcode表
					}
					if( !"".equals( mainCity ) && mainCity != null && !"none".equals( mainCity ) )
					{
						updateSetting( mainCity );//将默认城市或者主城市添加到settting表中
						Uri data_uri = Uri.parse( "content://com.cooee.app.cooeeweather.dataprovider/weather/" + mainCity );
						weatherwebservice.updateWeatherData( this , data_uri , mPostMark , 1 , mainCity , foreignCity );
					}
					// gaominghui@2016/03/22 ADD END
				}
				else
				{
					if( cursor != null && cursor.moveToFirst() )
					{
						do
						{
							postalcode[i] = cursor.getString( 0 );
							id[i] = cursor.getInt( 1 );
							postalwc[i] = cursor.getString( 6 );
							i++;
						}
						while( cursor.moveToNext() );
					}
					/*while( cursor != null && cursor.moveToNext() )
					{
						postalcode[i] = cursor.getString( 0 );
						id[i] = cursor.getInt( 1 );
						postalwc[i] = cursor.getString( 6 );
						i++;
					}*/
					addWidgetIDs( id );
					i = 0;
					while( hasMoreWidgetIDs() )
					{
						int widgetid = nextWidgetIDs();
						boolean ignored = false;
						allUserId = id[i];
						allPostalCode = postalcode[i];
						Log.v( TAG , "allUserId = " + id[i] + " allPostalCode = " + postalcode[i] + " WidgetIDs = " + widgetid );
						// �ж��Ƿ��Ѿ����¹�
						for( int j = 0 ; j < i ; j++ )
						{
							if( postalcode[i].equals( postalcode[j] ) )
							{
								ignored = true;
								break;
							}
						}
						if( !ignored )
						{ // �ظ��ĳ����������
							Uri data_uri = Uri.parse( "content://com.cooee.app.cooeeweather.dataprovider/weather/" + postalcode[i] );
							// weatherwebservice.updateWeatherData(this, data_uri);
							weatherwebservice.updateWeatherData( this , data_uri , mPostMark , 1 , postalwc[i] , foreignCity );
							// ������ɣ����͹㲥
							allSendBroadcast();
						}
						i++;
					}
					cursor.close();
				}
			}
		}
		else
		{
			boolean needUpdate = true;
			if( mForcedUpdate == 0 )
			{
				// ��ȡ��ݿ�
				ContentResolver resolver = this.getContentResolver();
				Cursor cursor = null;
				String selection = null;
				long lastUpdateTime = 0;
				Uri uri = Uri.parse( "content://" + weatherdataprovider.AUTHORITY + "/weather/" + mPostalCode );
				selection = weatherdataentity.POSTALCODE + "=" + "'" + mPostalCode + "'";
				cursor = resolver.query( uri , weatherdataentity.projection , selection , null , null );
				if( cursor != null )
				{
					if( cursor.moveToFirst() )
					{
						lastUpdateTime = cursor.getLong( 10 );
						// ����ڶ�ʱ���ڶ�����ݣ����������
						if( now - lastUpdateTime < mSettingEntity.getUpdateInterval() )
						{
							needUpdate = false;
						}
					}
					cursor.close();
				}
			}
			Log.v( TAG , "needUpdate = " + needUpdate );
			if( needUpdate )
			{
				// ʹ��google api��������Ԥ����Ϣ
				String city = null;
				// @gaominghui 2015/04/29 ADD START保存获取数据时的系统时间
				SharedPreferences pref = this.getSharedPreferences( "weacherClient" , Activity.MODE_PRIVATE );
				long lastUpdateTime = pref.getLong( "updateTime" , 0 );
				// @gaominghui 2015/04/29 ADD END
				boolean isLocate = false;
				if( lastUpdateTime != 0 && now != 0 && ( now - lastUpdateTime ) > 1000 * 60 * 30 )
				{
					city = ResolverUtil.updataLocatedCity( mPostalCode , this );
					//city = null;
					//Log.v( TAG , "mPostalCode = " + mPostalCode + "; city = " + city );
					if( !mPostalCode.equals( city ) && city != null && !"none".equals( city ) )
					{
						updateSetting( city );
						isLocate = true;
					}
					if( city != null )
					{
						mPostalCode = city;
					}
				}
				// @gaominghui 2015/04/07 ADD END
				Uri uri = Uri.parse( "content://" + weatherdataprovider.AUTHORITY + "/weather/" + mPostalCode );
				//Log.v( TAG , "uri = " + uri + "; mPostalCode = " + mPostalCode+"; isLocate  = "+isLocate  );
				weatherwebservice.updateWeatherData( this , uri , mPostMark , mUseWC , mWCName , foreignCity );
			}
			else
			{
				weatherwebservice.Update_Result_Flag = FLAG_UPDATE.AVAILABLE_DATA;
			}
			// ������ɣ����͹㲥
			mySendBroadcast();
			// @gaominghui 2015/04/29 ADD START保存获取数据时的系统时间
			{
				SharedPreferences pref = this.getSharedPreferences( "weacherClient" , Activity.MODE_PRIVATE );
				pref.edit().putLong( "updateTime" , System.currentTimeMillis() ).commit();
				Log.v( TAG , "updateTime = " + ( System.currentTimeMillis() - now ) );
			}
			// @gaominghui 2015/04/29 ADD END
		}
		//从sharedPreferences中读取是否需要显示联网提醒，需要联网提醒时为true，即表示用户未对联网进行授权,将其取反则为是否可以自动更新
		boolean canUpdateRegularly = !this.getSharedPreferences( "weacherClient" , Activity.MODE_PRIVATE ).getBoolean( "notice" , true );
		// 定时启动service
		if( mSettingEntity.getUpdateRegularly() != 0 && canUpdateRegularly )
		{
			Time time = new Time();
			// ������ʱ������
			long interval = mSettingEntity.getUpdateInterval();
			if( interval < 60 * 1000 * 60 * 6 )
				interval = 60 * 1000 * 60 * 6;
			if( weatherwebservice.Update_Result_Flag != FLAG_UPDATE.UPDATE_SUCCES )
			{
				interval = 60 * 1000 * 10;
			}
			time.set( now + interval );
			//			time.set( now + 2 * 60 * 1000 );
			long nextUpdate = time.toMillis( true );
			Intent updateIntent = new Intent( ACTION_UPDATE_ALL );
			updateIntent.setClass( this , weatherDataService.class );
			PendingIntent pendingIntent = PendingIntent.getService( this , 0 , updateIntent , 0 );
			// Schedule alarm, and force the device awake for this update
			AlarmManager alarmManager = (AlarmManager)getSystemService( Context.ALARM_SERVICE );
			alarmManager.set( AlarmManager.RTC_WAKEUP , nextUpdate , pendingIntent );
		}
		isThreadRun = false;
		// No updates remaining, so stop service
		stopSelf();
		// }
	}
}
