package com.cooee.widget.samweatherclock;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.cooee.StatisticsBase.Assets;
import com.cooee.StatisticsBase.StatisticsMainBase;
import com.cooee.app.cooeeweather.dataentity.InlandCitysEntity;
import com.cooee.app.cooeeweather.dataentity.PostalCodeEntity;
import com.cooee.app.cooeeweather.dataentity.SettingEntity;
import com.cooee.app.cooeeweather.dataentity.WeatherCondition;
import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;
import com.cooee.app.cooeeweather.dataprovider.weatherdataprovider;
import com.cooee.app.cooeeweather.filehelp.Log;
import com.cooee.app.cooeeweather.util.ResolverUtil;
import com.cooee.app.cooeeweather.view.WeatherConditionImage;
import com.cooee.app.cooeeweather.view.WeatherEditPost;
import com.cooee.app.cooeeweather.view.WeatherObserver;
import com.cooee.app.cooeeweather.view.WeatherReceiver;
import com.cooee.app.cooeeweather.view.WeatherSetting;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.kpsh.sdk.KpshSdk;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements View.OnClickListener , View.OnTouchListener
{
	
	public final static String TAG = "MainActivity";
	public final static String DATA_SERVICE_ACTION = "com.cooee.app.cooeeweather.dataprovider.weatherDataService";
	public final static String WEATHER_URI = "content://com.cooee.app.cooeeweather.dataprovider/weather";
	public final static String POSTALCODE_URI = "content://com.cooee.app.cooeeweather.dataprovider/postalCode";
	public final static String SETTING_URI = "content://com.cooee.app.cooeeweather.dataprovider/setting";
	private final String CITY_CONTENT_URI = "content://com.cooee.app.cooeeweather.dataprovider/citys";
	public final static String UPDATE_SETTING = "com.cooee.weather.data.action.UPDATE_SETTING";
	public static boolean city_is_faulu = true;
	private DisplayMetrics dm = new DisplayMetrics();
	// ������߷��͸�ĳ��еĹ㲥Action
	private final String CHANGE_POSTALCODE = "com.cooee.weather.Weather.action.CHANGE_POSTALCODE";
	// weijie_20130422
	public final static String CLOSED_UPDATE_LAUNCHER = "com.cooee.weather.Weather.action.CLOSED_UPDATE_LAUNCHER";
	public final static String BROATCAST_URI = "com.cooee.weather.data.action.UPDATE_RESULT";
	private Context mContext;
	private PopupWindow mPop;
	private weatherdataentity mDataEntity; // �������
	private SettingEntity mSettingEntity; // ����
	public static boolean defaultcity = false;
	private AppConfig mAppconfig = null;
	private Handler mHandler;
	// ����Observer����broadcast
	// private WeatherObserver mObserver;
	private ArrayList<String> mPoscalCodList;
	private String mstrcity;
	public static int mUserId; // ���ô�APP�Ķ���ID��Ϊ0˵���޵����ߣ�����ΪWidgetId
	private String mCurrentPostalCode;
	private int mCurrentIndex = 0;
	private boolean requesting = false;
	private boolean requestingFailed = false;
	private final int POSTALCODE_LIST_COUNT = 10;
	private Float mInitialY;
	private Float mInitialX;
	private final Float SLIDE_SENSITIVITY = 100.0f;
	public final static int ERROR_BAD_UNKOWN = -1;
	public final static int ERROR_BAD_PARAMS = -2;
	public final static int ERROR_BAD_REQUEST = -3;
	public final static int ERROR_BAD_GETCITY_DATABASE = -4;
	public final static int ERROR_BAD_GETDATA_DATABASE = -5;
	public final static int ERROR_BAD_GETDATA_INTENT = -6;
	public final static int ERROR_BAD_GETMAINCITY = -7;
	private StatisticsMainBase statisticsMainBase = null;
	private ViewGroup dotsGroup = null;
	private final static int POSITION_FAILURE = -9;
	private final static int POSITION_SUCCESS = -10;
	private final static int REFRESH_FINISH = -12;
	private final static int REFRESH_FAILED = -13;
	public final static int USER_CONTINUE = -11;
	public boolean USER_EXIT = false;
	private static boolean ifShowDisclaimer = false;
	private UpdateRecieve recieve = null;
	private boolean isRegister = false;
	// @gaominghui2015/05/27 ADD START用于读取arrays数组里面doovNotShare这个数组
	private String[] doovShieldShare = null;
	private List<String> packages = new ArrayList<String>();
	private List<String> classNames = new ArrayList<String>();
	// @2015/05/27 ADD END
	private int launcherVersion = -1;
	private String appid = null;
	private String sn = null;
	public SharedPreferences prefs;
	
	public void mySendBroadcast()
	{
		// ���mUserId��Ϊ0����˵����widget����ģ����͹㲥
		if( mUserId != 0 )
		{
			Intent intent = new Intent();
			intent.setAction( CHANGE_POSTALCODE );
			intent.putExtra( "com.cooee.weather.Weather.postalCode" , mCurrentPostalCode );
			intent.putExtra( "com.cooee.weather.Weather.userId" , mUserId );
			intent.putExtra( "com.cooee.weather.Weather.skin" , WeatherSetting.mSkinToUpdate );
			sendBroadcast( intent );
			Log.v( TAG , "send broadcast: userId = " + mUserId + ", postalCode = " + mCurrentPostalCode );
		}
	}
	
	// weijie_20130422
	public void SendBroadcastToLauncherByClosed()
	{
		try
		{
			Intent intent = new Intent();
			intent.setAction( CLOSED_UPDATE_LAUNCHER );
			intent.putExtra( "postalCode" , mCurrentPostalCode );
			intent.putExtra( "postalListId" , mCurrentIndex );
			Log.d( TAG , "SendBroadcastToLauncherByClosed saveSetting" );
			saveSetting();
			Log.d( TAG , "SendBroadcastToLauncherByClosed readData" );
			readData();
			Log.d( TAG , "SendBroadcastToLauncherByClosed readData end" );
			if( mDataEntity != null )
			{
				intent.putExtra( "T0_tempc_now" , mDataEntity.getTempC().intValue() );
				intent.putExtra( "T0_tempc_high" , mDataEntity.getDetails().get( 0 ).getHight().intValue() );
				intent.putExtra( "T0_tempc_low" , mDataEntity.getDetails().get( 0 ).getLow().intValue() );
				intent.putExtra( "T0_condition" , mDataEntity.getCondition() );
				intent.putExtra( "T0_lastupdatetime" , mDataEntity.getLastUpdateTime().longValue() );
				intent.putExtra( "T0_windCondition" , mDataEntity.getWindCondition() );
				intent.putExtra( "T0_humidity" , mDataEntity.getHumidity() );
				intent.putExtra( "T0_lunarcalendar" , mDataEntity.getLunarcalendar() );
				intent.putExtra( "T0_weathertime" , mDataEntity.getWeathertime() );
				intent.putExtra( "T0_ultravioletray" , mDataEntity.getUltravioletray() );
				intent.putExtra( "T0_condition_index" , WeatherCondition.convertCondition( mDataEntity.getCondition() ).toString() );
				Log.v( TAG , "T0_ultravioletray is " + mDataEntity.getUltravioletray() + " T0_weathertime is " + mDataEntity.getWeathertime() );
				Log.v( TAG , "T0_windCondition is " + mDataEntity.getWindCondition() + " humidity is " + mDataEntity.getHumidity() + " T0_lunarcalendar is " + mDataEntity.getLunarcalendar() );
				intent.putExtra( "T1_tempc_high" , mDataEntity.getDetails().get( 1 ).getHight().intValue() );
				intent.putExtra( "T1_tempc_low" , mDataEntity.getDetails().get( 1 ).getLow().intValue() );
				intent.putExtra( "T1_condition" , mDataEntity.getDetails().get( 1 ).getCondition() );
				intent.putExtra( "T1_condition_index" , WeatherCondition.convertCondition( mDataEntity.getDetails().get( 1 ).getCondition() ).toString() );
				intent.putExtra( "T2_tempc_high" , mDataEntity.getDetails().get( 2 ).getHight().intValue() );
				intent.putExtra( "T2_tempc_low" , mDataEntity.getDetails().get( 2 ).getLow().intValue() );
				intent.putExtra( "T2_condition" , mDataEntity.getDetails().get( 2 ).getCondition() );
				intent.putExtra( "T2_condition_index" , WeatherCondition.convertCondition( mDataEntity.getDetails().get( 2 ).getCondition() ).toString() );
				intent.putExtra( "T3_tempc_high" , mDataEntity.getDetails().get( 3 ).getHight().intValue() );
				intent.putExtra( "T3_tempc_low" , mDataEntity.getDetails().get( 3 ).getLow().intValue() );
				intent.putExtra( "T3_condition" , mDataEntity.getDetails().get( 3 ).getCondition() );
				intent.putExtra( "T3_condition_index" , WeatherCondition.convertCondition( mDataEntity.getDetails().get( 3 ).getCondition() ).toString() );
				intent.putExtra( "result" , "OK" );
				Log.e( "T0_condition" , "getDetails condition = " + mDataEntity.getDetails().get( 0 ).getCondition() );
				Log.e( "T0_condition" , "condition = " + mDataEntity.getCondition() );
			}
			else
			{
				Log.d( TAG , "SendBroadcastToLauncherByClosed 0" );
				intent.putExtra( "errorcode" , ERROR_BAD_GETDATA_DATABASE );
				intent.putExtra( "result" , "ERROR" );
			}
			Log.d( TAG , "SendBroadcastToLauncherByClosed 1" );
			sendBroadcast( intent );
			Log.d( TAG , "SendBroadcastToLauncherByClosed 2" );
		}
		catch( Exception ex )
		{
			Log.d( TAG , "SendBroadcastToLauncherByClosed exception" );
		}
	}
	
	public static void setdeldefault(
			boolean boolvalue )
	{
		defaultcity = boolvalue;
	}
	
	public static boolean getdeldefault()
	{
		return defaultcity;
	}
	
	public static String convertDate(
			long milis )
	{
		String timeString;
		Date date = new Date( milis );
		SimpleDateFormat sDateFormat = new SimpleDateFormat( "yyMMdd:HH:mm" );
		timeString = sDateFormat.format( date );
		return timeString;
	}
	
	@Override
	public boolean onKeyDown(
			int keyCode ,
			KeyEvent event )
	{
		if( keyCode == KeyEvent.KEYCODE_HOME )
		{
			finish();
		}
		return super.onKeyDown( keyCode , event );
	}
	
	@Override
	public void onAttachedToWindow()
	{
		// 4.0���޷�ʹ�ô����
		// this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}
	
	@Override
	public void onNewIntent(
			Intent intent )
	{
		mUserId = intent.getIntExtra( "userId" , 0 );
		String strcity = intent.getStringExtra( "defaultcity" );
		if( strcity == null )
		{
			defaultcity = false;
			return;
		}
		if( !strcity.equals( "none" ) )
		{
			defaultcity = true;
		}
		else
		{
			defaultcity = false;
		}
		Log.v( TAG , "onNewIntent mUserId = " + mUserId );
		// ���mUserId��ȡpostalCode����ȷ����ʼ��mCurrentIndex��ֻ��onCreateʱ��һ��
		readPostalCodeByUserId();
	}
	
	@Override
	public void onDestroy()
	{
		// ����Observer����broadcast
		// ContentResolver resolver = getContentResolver();
		// resolver.unregisterContentObserver(mObserver);
		Log.v( TAG , "onDestroy" );
		unRegisterBroadCast();
		WeatherReceiver.setHandler( null );
		// �����widget����ģ����widget�����㲥����ĳ���
		// mySendBroadcast();
		// weijie_20130422
		// SendBroadcastToLauncherByClosed();
		super.onDestroy();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if( !isRegister )
		{
			regBroadCast();
			isRegister = true;
		}
		if( !ifShowDisclaimer )
		{
			if( statisticsMainBase != null )
			{
				statisticsMainBase.onResume();
			}
			Log.v( TAG , "onResume" );
			// ��ȡ���
			updateEntity();
			// ���½���
			if( mPoscalCodList.isEmpty() && AppConfig.getInstance( mContext ).isPosition() )
			{
				//updateViews();
				mProgressDialog = new ProgressDialog( this );
				mProgressDialog.setProgressStyle( ProgressDialog.STYLE_SPINNER );
				mProgressDialog.setMessage( getResources().getString( R.string.positioning ) );
				mProgressDialog.setIndeterminate( false );
				mProgressDialog.getWindow().setBackgroundDrawable( new ColorDrawable( 0x00000000 ) );
				mProgressDialog.getWindow().setLayout( 30 , 30 );
				mProgressDialog.setCancelable( false );
				mProgressDialog.show();
				new Thread() {
					
					@Override
					public void run()
					{
						if( mPoscalCodList != null && mPoscalCodList.isEmpty() )
						{
							Log.i( "MainActivity" , "run ---before addLocatedCity = " );
							ResolverUtil.addLocatedCity( MainActivity.this );
							mProgressDialog.cancel();
							readPostalCodeList();
							setCurrentPostalCode();
							if( mPoscalCodList.isEmpty() )
							{
								mHandler.obtainMessage( POSITION_FAILURE ).sendToTarget();
								Intent intent = new Intent( MainActivity.this , WeatherEditPost.class );
								startActivityForResult( intent , CONTEXT_RESTRICTED );
							}
							else
							{
								mHandler.obtainMessage( POSITION_SUCCESS ).sendToTarget();
							}
						}
						else
						{
							mProgressDialog.cancel();
							readPostalCodeList();
							setCurrentPostalCode();
						}
					};
				}.start();
			}
			else
			{
				updateViews();
			}
		}
		StatisticsExpandNew.use( this , sn , appid , CooeeSdk.cooeeGetCooeeId( this ) , 1 , this.getApplication().getPackageName() , "" + launcherVersion );
		if( !prefs.contains( "first_run" ) )
			prefs.edit().putBoolean( "first_run" , false ).apply();
	}
	
	@Override
	public void onRestart()
	{
		super.onRestart();
		Log.v( TAG , "onRestart" );
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		Log.v( TAG , "onStop" );
		//mySendBroadcast();
		// weijie_20130422
		if( !USER_EXIT )
		{
			SendBroadcastToLauncherByClosed();
			Intent intent = new Intent( this , WeatherRequestIntface.class );
			intent.setAction( WeatherRequestIntface.REQUEST_REFRESH_ACTION );
			sendBroadcast( intent );
			this.getContentResolver().notifyChange( Uri.parse( weatherdataprovider.DB_LISTENER_URI ) , null );
		}
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		unRegisterBroadCast();
		Log.v( TAG , "onPause" );
	}
	
	/**
	 * 注册广播
	 * @author gaominghui  2015年4月28日
	 */
	private void regBroadCast()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction( UPDATE_SETTING );
		recieve = new UpdateRecieve();
		registerReceiver( recieve , filter );
	}
	
	/**
	 * 解除广播
	 * @author gaominghui  2015年4月28日
	 */
	private void unRegisterBroadCast()
	{
		if( isRegister )
		{
			this.unregisterReceiver( recieve );
			isRegister = false;
		}
	}
	
	private class UpdateRecieve extends BroadcastReceiver
	{
		
		/**
		 *
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 * @auther tangliang  2015年4月28日
		 */
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			// TODO Auto-generated method stub
			Log.v( TAG , "onReceive。。。。。。。。" );
			if( intent.getAction().equals( UPDATE_SETTING ) )
			{
				readPostalCodeList();
				updateViews();
			}
		}
	}
	
	@Override
	public void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		mAppconfig = AppConfig.getInstance( this );
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				KpshSdk.setAppKpshTag( MainActivity.this , MainActivity.this.getPackageName() );
				CooeeSdk.initCooeeSdk( MainActivity.this );
			}
		} ).start();
		prefs = this.getSharedPreferences( TAG , Activity.MODE_PRIVATE );
		Assets.initAssets( this );
		JSONObject object = Assets.config;
		try
		{
			JSONObject config = object.getJSONObject( "config" );
			appid = config.getString( "app_id" );
			sn = config.getString( "serialno" );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			launcherVersion = getPackageManager().getPackageInfo( this.getPackageName() , 0 ).versionCode;
		}
		catch( NameNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( prefs.getBoolean( "first_run" , true ) )
		{
			StatisticsExpandNew.register( this , sn , appid , CooeeSdk.cooeeGetCooeeId( this ) , 1 , this.getApplication().getPackageName() , "" + launcherVersion );
		}
		else
		{
			StatisticsExpandNew.startUp( this , sn , appid , CooeeSdk.cooeeGetCooeeId( this ) , 1 , this.getApplication().getPackageName() , "" + launcherVersion );
		}
		/*AssetManager mgr = getAssets();//得到AssetManager
		tf = Typeface.createFromAsset( mgr , "MingHei_R.ttf" );//根据路径得到
		
		*/
		// gaominghui@2016/12/19 ADD START判断6.0是否有READ_PHONE_STATE的权限
		if( Build.VERSION.SDK_INT >= 23 )
		{
			if( !( checkSelfPermission( Manifest.permission.READ_PHONE_STATE ) == PackageManager.PERMISSION_GRANTED ) )
			{
				requestPhonePermission( Manifest.permission.READ_PHONE_STATE , REQUEST_PERMISSION_READ_PHONE_STATE );
			}
			if( !( checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED ) )
			{
				requestPhonePermission( Manifest.permission.WRITE_EXTERNAL_STORAGE , REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE );
			}
		}
		// gaominghui@2016/12/19 ADD END
		WindowManager mWm = (WindowManager)getSystemService( Context.WINDOW_SERVICE );
		mWm.getDefaultDisplay().getMetrics( dm );
		if( mAppconfig.isHuaweiStyle() )
		{
			setContentView( R.layout.app1_layout );
		}
		else
		{
			setContentView( R.layout.app_layout );
		}
		ImageView imageview0 = (ImageView)findViewById( R.id.imgeview );
		LayoutParams laParams0 = (LayoutParams)imageview0.getLayoutParams();
		laParams0.height = dm.heightPixels - dm.widthPixels;
		if( dm.heightPixels == 854 )
			laParams0.height += 54;
		laParams0.width = dm.widthPixels;
		// imageview0.setBottom(0);
		// imageview0.setBackgroundResource(R.drawable.feature_weather_background);
		imageview0.setLayoutParams( laParams0 );
		for( int i = 0 ; i < 4 ; i++ )
		{
			LinearLayout cell = (LinearLayout)findViewById( R.id.celllayout01 + i );
			LayoutParams cellparmas = (LayoutParams)cell.getLayoutParams();
			// cellparmas.height=dm.heightPixels - dm.widthPixels;
			cellparmas.width = dm.widthPixels / 4;
			cell.setLayoutParams( cellparmas );
			TextView textview_condition1 = (TextView)findViewById( R.id.cell_textview_week01 + i );
			TextView textview_condition = (TextView)findViewById( R.id.textview_condition1 + i );
			TextView textview_tmp1 = (TextView)findViewById( R.id.textview_tmp01 + i );
			ImageView imageview_week = (ImageView)findViewById( R.id.imageview_week01 + i );
			LayoutParams pimageview_week = (LayoutParams)imageview_week.getLayoutParams();
			Log.i( TAG , "dm.widthPixels = " + dm.widthPixels + ";  dm.heightPixels = " + dm.heightPixels );
			if( dm.widthPixels == 800 )
			{
				textview_condition1.setTextSize( 20 );
				textview_condition.setTextSize( 18 );
				textview_tmp1.setTextSize( 20 );
				pimageview_week.width = dm.widthPixels / 4 - 3;
				imageview0.setLayoutParams( laParams0 );
			}
			else
			{
				if( dm.widthPixels == 1080 )
				{
					pimageview_week.width = dm.widthPixels / 4 - 40;
				}
				else
				{
					pimageview_week.width = dm.widthPixels / 4 - 30;
				}
				pimageview_week.height = pimageview_week.width - 26;
				imageview0.setLayoutParams( laParams0 );
			}
			/**
			 * 横屏适配 
			 * fulijuan add 2017/5/9 start
			 */
			int mCurrentOrientation = getResources().getConfiguration().orientation;
			if( mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE )
			{
				pimageview_week.width = getResources().getDimensionPixelSize( R.dimen.pimageview_week_width );
				pimageview_week.height = getResources().getDimensionPixelSize( R.dimen.pimageview_week_height );
				imageview0.setLayoutParams( laParams0 );
			}
			/**
			 * 横屏适配
			 * fulijuan add 2017/5/9 end
			 */
		}
		//注册广播
		if( !isRegister )
		{
			regBroadCast();
			isRegister = true;
		}
		mContext = this;
		// ���ö����ť��onClickListener
		setViewsOnClickListener();
		// ����Handler�����Դ���receiver���յ�����Ϣ
		mHandler = new Handler() {
			
			public void handleMessage(
					Message msg )
			{
				switch( msg.what )
				{
					case WeatherObserver.MSG_REFRESH:
						Log.v( TAG , "WeatherObserver.MSG_REFRESH" );
						readData();
						break;
					case WeatherReceiver.MSG_REFRESH:
						Log.v( TAG , "WeatherReceiver.MSG_REFRESH" );
						requesting = false;
						readData();
						break;
					case WeatherReceiver.MSG_AVAILABLE:
						Log.v( TAG , "WeatherReceiver.MSG_AVAILABLE" );
						requesting = false;
						readData();
						break;
					// shanjie 20130130
					case WeatherReceiver.MSG_WEBSERVICE_ERROR:
						Log.v( "shanjie" , "Handler handleMessage WeatherReceiver.MSG_WEBSERVICE_ERROR" );
						requesting = false;
						requestingFailed = true;
						// 弹出提示
						Toast.makeText( mContext , R.string.update_failed , Toast.LENGTH_SHORT ).show();
						// 读取数据
						readData();
						// 如果没有数据，显示无数据
						if( mDataEntity == null )
						{
							requesting = false;
							requestingFailed = true;
							Log.v( "shanjie" , "updateViews" );
							updateViews();
						}
						requestingFailed = false;
						break;
					case WeatherReceiver.MSG_FAILED:
						// ����ʧ��
						Log.v( TAG , "WeatherReceiver.MSG_FAILED" );
						requesting = false;
						requestingFailed = true;
						// ������ʾ
						Toast.makeText( mContext , R.string.update_failed , Toast.LENGTH_SHORT ).show();
						// ��ȡ���
						readData();
						// ���û����ݣ���ʾ�����
						if( mDataEntity == null )
						{
							requesting = false;
							requestingFailed = true;
							updateViews();
						}
						requestingFailed = false;
						break;
					case WeatherReceiver.MSG_INVILIDE:
						requesting = false;
						requestingFailed = true;
						readData();
						// ���û����ݣ���ʾ�����
						if( mDataEntity == null )
						{
							requesting = false;
							requestingFailed = true;
							updateViews();
						}
						requestingFailed = false;
						MainActivity.city_is_faulu = false;
						break;
					case WeatherReceiver.MSG_SREACH_SUCCES:
						requesting = false;
						readData();
						break;
					case WeatherReceiver.MSG_SREACH_FAILED:
						requesting = false;
						requestingFailed = true;
						// ������ʾ
						//Toast.makeText( mContext , R.string.update_failed , Toast.LENGTH_SHORT ).show();
						// ��ȡ���
						readData();
						// ���û����ݣ���ʾ�����
						if( mDataEntity == null )
						{
							requesting = false;
							requestingFailed = true;
							updateViews();
						}
						requestingFailed = false;
						break;
					case POSITION_FAILURE:
						Toast.makeText( mContext , R.string.position_failure , Toast.LENGTH_SHORT ).show();
						break;
					case POSITION_SUCCESS:
						Toast.makeText( mContext , R.string.position_success , Toast.LENGTH_SHORT ).show();
						refreshData();
						break;
					case REFRESH_FINISH:
						Toast.makeText( mContext , R.string.refresh_finish , 1000 ).show();
						break;
					case REFRESH_FAILED:
						Toast.makeText( mContext , R.string.refresh_failed , Toast.LENGTH_SHORT ).show();
						break;
					case USER_CONTINUE:
						ifShowDisclaimer = false;
						//统计
						statisticsMainBase = new StatisticsMainBase( mContext );
						statisticsMainBase.oncreate();
						//统计结束
						onResume();
						break;
					default:
						break;
				}
				super.handleMessage( msg );
			}
		};
		if( mAppconfig.showDisclaimer() )
		{
			ifShowDisclaimer = this.getSharedPreferences( "weacherClient" , Activity.MODE_PRIVATE ).getBoolean( "notice" , true );
			if( ifShowDisclaimer )
			{
				new DisclaimerDialog( this , R.style.dialog , mHandler ).show();
			}
		}
		// ����Observer����broadcast
		// mObserver = new WeatherObserver(this, mHandler);
		// ContentResolver resolver = getContentResolver();
		// Uri uri = Uri.parse(WEATHER_URI);
		// Log.v(TAG, "mObserver uri = " + uri);
		// resolver.registerContentObserver(uri, true, mObserver);
		WeatherReceiver.setHandler( mHandler );
		mSettingEntity = new SettingEntity();
		mPoscalCodList = new ArrayList<String>();
		readSetting();
		Intent intent = getIntent();
		mUserId = intent.getIntExtra( "userId" , 0 );
		mstrcity = intent.getStringExtra( "defaultcity" );
		Log.v( TAG , "onCreate defaultcity = " + defaultcity );
		Log.v( TAG , "onCreate mstrcity = " + mstrcity );
		if( mstrcity == null )
		{
			String tmp = mSettingEntity.getMainCity();
			Log.v( TAG , "onCreate tmp = " + tmp );
			if( tmp != null && !tmp.equals( "" ) )
				mstrcity = tmp;
		}
		Log.v( TAG , "onCreate mstrcity 2 = " + mstrcity );
		defaultcity = false;
		/*
		 * if(strcity!= null && !(strcity.equals("none"))){ defaultcity = true;
		 * } else{ defaultcity = false; }
		 */
		Log.v( TAG , "onCreate mUserId = " + mUserId );
		// ���mUserId��ȡpostalCode����ȷ����ʼ��mCurrentIndex��ֻ��onCreateʱ��һ��
		readPostalCodeByUserId();
		// ������ͼƬ���ô������?��
		ImageView iv = (ImageView)findViewById( R.id.weather_image_shoot );
		iv.setOnTouchListener( (OnTouchListener)this );
		Log.v( TAG , "onCreate end mCurrentIndex=" + mCurrentIndex );
		/*
		 * wanghongjian add 统计
		 */
		if( !ifShowDisclaimer )
		{
			statisticsMainBase = new StatisticsMainBase( this );
			statisticsMainBase.oncreate();
		}
		// addlayout.setOnTouchListener((OnTouchListener) this);
		// menulayout.setOnTouchListener((OnTouchListener) this);
		// reflayout.setOnTouchListener((OnTouchListener) this);
		/*
		 * wangjing start 实现2次跳转，直接进入城市管理界面
		 */
		Intent intent2 = getIntent();
		boolean isDirectEidt = intent2.getBooleanExtra( "directEidt" , false );
		Log.v( "wangjing" , "截取intent ttt:" + isDirectEidt );
		if( isDirectEidt )
		{
			Intent intent3 = new Intent( this , WeatherEditPost.class );
			startActivityForResult( intent3 , CONTEXT_RESTRICTED );
		}
		/*
		 * wangjing end 实现2次跳转，直接进入城市管理界面
		 */
		// @2015/05/27 UPD START  0002892: 闪耀项目：天气分享里面需屏蔽手机管家和朵唯云。
		//用于读取arrays数组里面doovNotShare这个数组，数组里面是朵唯闪耀项目要求在天气客户端点击分享按钮出现的分享应用列表里需要屏蔽的apk的包名和类名
		if( mAppconfig.isDoovShare() )
		{
			doovShieldShare = getResources().getStringArray( R.array.doov_shield_share );
			if( doovShieldShare.length > 0 )
			{
				String[] temp = null;
				for( int i = 0 ; i < doovShieldShare.length ; i++ )
				{
					temp = doovShieldShare[i].split( "," );
					if( temp.length > 1 )
					{
						packages.add( temp[0] );
						classNames.add( temp[temp.length - 1] );
					}
				}
			}
		}
		// @2015/05/27 UPD END
	}
	
	@Override
	public boolean onCreateOptionsMenu(
			Menu menu )
	{
		if( mAppconfig.isHuaweiStyle() )
		{
			return super.onCreateOptionsMenu( menu );
		}
		else
		{
			MenuItem setting = menu.add( 0 , 1 , 0 , getResources().getString( R.string.setting ) );
			setting.setOnMenuItemClickListener( new OnMenuItemClickListener() {
				
				public boolean onMenuItemClick(
						MenuItem item )
				{
					// �������ý���
					Intent intent = new Intent( MainActivity.this , WeatherSetting.class );
					startActivity( intent );
					return true;
				}
			} );
			setting.setIcon( R.drawable.setting_icon );
			// setting.setHeaderIcon(R.drawable.setting_icon);
			MenuItem edit = menu.add( 0 , 2 , 0 , getResources().getString( R.string.exit ) );
			edit.setOnMenuItemClickListener( new OnMenuItemClickListener() {
				
				public boolean onMenuItemClick(
						MenuItem item )
				{
					finish();
					return true;
				}
			} );
			edit.setIcon( R.drawable.exit_icon );
			// edit.setHeaderIcon(R.drawable.setting_icon);
			return true;
		}
	}
	
	// ���mUserId��ȡpostalCode����ȷ����ʼ��mCurrentIndex��ֻ��onCreateʱ��һ��
	public void readPostalCodeByUserId()
	{
		Cursor mCursor = null;
		String select = null;
		if( true )
		{
			readPostalCodeList();
			Log.v( TAG , "readPostalCodeByUserId mstrcity=" + mstrcity );
			if( mstrcity != null )
			{
				Log.v( TAG , "readPostalCodeByUserId 2mstrcity=" + mstrcity );
				if( !mstrcity.equals( "none" ) && !mstrcity.equals( "" ) )
				{
					Log.v( TAG , "readPostalCodeByUserId 3mstrcity=" + mstrcity );
					int i = 0;
					for( i = 0 ; i < mPoscalCodList.size() ; i++ )
					{
						Log.v( TAG , "readPostalCodeByUserId 4get(i)=" + mPoscalCodList.get( i ) );
						if( mstrcity.equals( (String)mPoscalCodList.get( i ) ) )
						{
							mCurrentIndex = i;
							break;
						}
					}
					Log.v( TAG , "readPostalCodeByUserId (i)=" + i + ",mPoscalCodList.size()=" + mPoscalCodList.size() );
					if( i == mPoscalCodList.size() )
					{
						//如果不开启定位，则将默认城市加入到数据库中
						//原本可能是将intent中传过来的城市加入数据库，不知道为何改成了默认城市
						if( !AppConfig.getInstance( mContext ).isPosition() )
						{
							select = InlandCitysEntity.NAME + " LIKE " + "'%" + mstrcity + "%'";
							mCursor = getContentResolver().query( Uri.parse( CITY_CONTENT_URI ) , InlandCitysEntity.projection , select , null , null );
							if( mCursor == null )
							{
							}
							else
							{
								ContentResolver resolver = getContentResolver();
								ContentValues values = new ContentValues();
								String defaultCity = AppConfig.getInstance( this ).getDefaultCity();
								Log.i( "weatherDataService" , "readPostalCodeByUserId ---defaultCity = " + defaultCity );
								values.put( PostalCodeEntity.POSTAL_CODE , defaultCity );
								values.put( PostalCodeEntity.USER_ID , 0 );
								resolver.insert( Uri.parse( POSTALCODE_URI ) , values );
							}
						}
						mCurrentIndex = 0;
					}
				}
			}
			Log.v( TAG , "readPostalCodeByUserId mCurrentIndex=" + mCurrentIndex );
		}
		else
		{
			ContentResolver resolver = getContentResolver();
			Cursor cursor = null;
			Uri uri;
			String selection;
			if( mUserId == 0 )
			{
				mCurrentIndex = 0;
				return;
			}
			uri = Uri.parse( POSTALCODE_URI );
			selection = PostalCodeEntity.USER_ID + "=" + "'" + mUserId + "'";
			cursor = resolver.query( uri , PostalCodeEntity.projection , selection , null , null );
			// �����ȶ�ȡ�����б�
			readPostalCodeList();
			Log.v( TAG , "cursor = " + cursor );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
				{
					String post = cursor.getString( 0 );
					Log.v( TAG , "cursor post= " + post );
					for( int i = 0 ; i < mPoscalCodList.size() ; i++ )
						if( post.equals( (String)mPoscalCodList.get( i ) ) )
						{
							mCurrentIndex = i;
							break;
						}
				}
				else
				{
					if( mstrcity != null )
					{
						if( !mstrcity.equals( "none" ) )
						{
							for( int i = 0 ; i < mPoscalCodList.size() ; i++ )
								if( mstrcity.equals( (String)mPoscalCodList.get( i ) ) )
								{
									mCurrentIndex = i;
									break;
								}
						}
					}
				}
				Log.v( TAG , "cursor moveToFirst error" );
				cursor.close();
			}
		}
	}
	
	public void setViewsOnClickListener()
	{
		View v;
		v = findViewById( R.id.combo_layout );
		v.setOnClickListener( this );
		/*
		 * v = findViewById(R.id.add_button); v.setOnClickListener(this);
		 * 
		 * v = findViewById(R.id.refresh_button); v.setOnClickListener(this);
		 * 
		 * v = findViewById(R.id.menu_button); v.setOnClickListener(this);
		 */
		/*
		 * v = findViewById(R.id.refresh_button2); v.setOnClickListener(this);
		 */
	}
	
	public List<Map<String , Object>> getListData()
	{
		List<Map<String , Object>> list = new ArrayList<Map<String , Object>>();
		Map<String , Object> map;
		if( mPoscalCodList.size() == 0 )
		{
			map = new HashMap<String , Object>();
			map.put( "bg" , R.drawable.popup_item_bg_normal );
			map.put( "text" , getResources().getString( R.string.none ) );
			map.put( "divider" , null );
			list.add( map );
		}
		else
			if( mPoscalCodList.size() == 1 )
		{
			map = new HashMap<String , Object>();
			map.put( "bg" , R.drawable.popup_item_bg_single );
			map.put( "text" , mPoscalCodList.get( 0 ) );
			map.put( "divider" , null );
			list.add( map );
		}
		else
		{
			for( int i = 0 ; i < mPoscalCodList.size() ; i++ )
			{
				map = new HashMap<String , Object>();
				if( i == 0 )
				{
					map.put( "bg" , R.drawable.popup_item_bg_up );
					map.put( "divider" , null );
				}
				else
					if( i == mPoscalCodList.size() - 1 )
				{
					map.put( "bg" , R.drawable.popup_item_bg_down );
					map.put( "divider" , R.drawable.popup_item_divider );
				}
				else
				{
					map.put( "bg" , R.drawable.popup_item_bg_normal );
					map.put( "divider" , R.drawable.popup_item_divider );
				}
				map.put( "text" , mPoscalCodList.get( i ) );
				list.add( map );
			}
		}
		return list;
	}
	
	public void selectPostalCode()
	{
		LayoutInflater mLayoutInflater = (LayoutInflater)getSystemService( LAYOUT_INFLATER_SERVICE );
		View pop_view = mLayoutInflater.inflate( R.layout.popup_window_layout , null , false );
		ListView listView = (ListView)pop_view.findViewById( R.id.listview );
		listView.setAdapter(
				new SimpleAdapter(
						this ,
						getListData() ,
						R.layout.popup_window_item_layout ,
						new String[]{ "bg" , "text" , "divider" } ,
						new int[]{ R.id.listitem_bg , R.id.listitem_text , R.id.listitem_divider } ) );
		listView.setDivider( null );
		listView.setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				// �˳�popup
				if( mPop != null )
				{
					mPop.dismiss();
				}
				if( mPoscalCodList.size() > 0 )
				{
					mCurrentIndex = position;
					mCurrentPostalCode = mPoscalCodList.get( position );
					changePostalCode();
				}
			}
		} );
		// ����popup window
		int width = (int)getResources().getDimension( R.dimen.popup_window_width );
		int height = (int)getResources().getDimension( R.dimen.popup_item_height ) + 2;
		if( mPoscalCodList.size() > 0 )
		{
			if( mPoscalCodList.size() > POSTALCODE_LIST_COUNT )
				height = height * POSTALCODE_LIST_COUNT;
			else
				height = height * mPoscalCodList.size();
		}
		mPop = new PopupWindow( pop_view , width , height );
		// mPop.setAnimationStyle(R.style.popwindow);
		mPop.setFocusable( true ); // ����PopupWindow�ɻ�ý���
		mPop.setTouchable( true ); // ����PopupWindow�ɴ���
		mPop.setOutsideTouchable( true ); // ���÷�PopupWindow����ɴ���
		ShapeDrawable mShapeDrawable = new ShapeDrawable( new OvalShape() );
		mShapeDrawable.getPaint().setColor( 0x00000000 );
		mShapeDrawable.setBounds( 0 , 0 , width , height );
		mPop.setBackgroundDrawable( mShapeDrawable ); // �����ñ����޷������˳�
		mPop.showAsDropDown( findViewById( R.id.combo_layout ) , 0 , 8 );
	}
	
	private ProgressDialog mProgressDialog;
	
	public void updateViews()
	{
		Log.i( TAG , "updateViews!!!" );
		TextView tv;
		ImageView iv;
		try
		{
			try
			{
				if( mPoscalCodList == null )
					mPoscalCodList = new ArrayList<String>();
				if( mPoscalCodList.size() == 0 )
				{
					Log.i( TAG , " updateViews!!! mPoscalCodList.size() == 0" );
					if( mAppconfig.isHuaweiStyle() )
					{
						TextView textView = (TextView)findViewById( R.id.cityname );
						textView.setText( R.string.na );
						mCurrentPostalCode = "none";
					}
					else
					{
						TextView textView = (TextView)findViewById( R.id.cur_postalCode );
						textView.setText( R.string.na );
						mCurrentPostalCode = "none";
					}
				}
				else
				{
					Log.i( TAG , " updateViews!!! mPoscalCodList.size() > 0" );
					if( mAppconfig.isHuaweiStyle() )
					{
						TextView textView = (TextView)findViewById( R.id.cityname );
						mCurrentPostalCode = mPoscalCodList.get( mCurrentIndex );
						Log.i( "TAG" , " mCurrentPostalCode = " + mCurrentPostalCode );
						textView.setText( mCurrentPostalCode );
					}
					else
					{
						TextView textView = (TextView)findViewById( R.id.cur_postalCode );
						mCurrentPostalCode = mPoscalCodList.get( mCurrentIndex );
						textView.setText( mCurrentPostalCode );
					}
				}
				if( !mAppconfig.isHuaweiStyle() )
				{
					TextView textView = (TextView)findViewById( R.id.textview_page );
					if( mPoscalCodList.size() > 0 )
					{
						textView.setText( ( mCurrentIndex + 1 ) + "/" + mPoscalCodList.size() );
					}
					else
					{
						textView.setText( R.string.na );
					}
				}
			}
			catch( Exception e )
			{
				// TODO Auto-generated catch block
				Log.e( TAG , "updateViews!!! e = " + e );
				e.printStackTrace();
			}
			// ���û��ݣ����������
			if( !mCurrentPostalCode.equals( "none" ) )
			{
				if( mDataEntity == null )
				{
					if( requestingFailed == false )
					{
						Log.i( TAG , " updateViews!!! requestingFailed == false " );
						try
						{
							requestData();
						}
						catch( Exception e )
						{
							// TODO Auto-generated catch block
							Log.e( TAG , "updateViews!!! requestData Exception  = " + e );
							e.printStackTrace();
						}
					}
				}
			}
			else
			{ // weather 数据重新读取20130121
				Log.i( TAG , " updateViews!!! before readData " );
				try
				{
					readData();
				}
				catch( Exception e )
				{
					// TODO Auto-generated catch block
					Log.e( TAG , "updateViews!!! readData Exception  = " + e );
					e.printStackTrace();
				}
			}
			// ���û��ݣ�����ʾ�����
			Log.i( TAG , "updateViews!!! mDataEntity = " + mDataEntity );
			if( mDataEntity == null )
			{
				if( !mAppconfig.isHuaweiStyle() )
				{
					findViewById( R.id.nodata_layout ).setVisibility( View.VISIBLE );
				}
				if( requesting && !requestingFailed )
				{
					findViewById( R.id.updating_layout ).setVisibility( View.VISIBLE );
					if( mAppconfig.isHuaweiStyle() )
					{
						init_smallDots( mCurrentIndex , mPoscalCodList.size() );
					}
					findViewById( R.id.no_data_layout ).setVisibility( View.INVISIBLE );
				}
				else
				{
					findViewById( R.id.updating_layout ).setVisibility( View.INVISIBLE );
					findViewById( R.id.no_data_layout ).setVisibility( View.VISIBLE );
					if( mCurrentPostalCode.equals( "none" ) )
					{
						( (TextView)findViewById( R.id.nodate ) ).setText( R.string.please_add_city );
						( (TextView)findViewById( R.id.pleaseupdate ) ).setVisibility( View.INVISIBLE );
						if( true )
						{
							Intent intent = new Intent();
							intent.setClassName( this , "com.cooee.app.cooeeweather.view.WeatherAddPost" );
							intent.putExtra( "citys" , "0" );// ��������Ļ������Բ��Ӵ��д���
							this.startActivityForResult( intent , CONTEXT_RESTRICTED );// CONTEXT_RESTRICTED
							// startActivity(intent);
						}
					}
					else
					{
						if( !city_is_faulu )
						{
							TextView textView = (TextView)findViewById( R.id.nodate );
							textView.setText( R.string.update_fault );
							( (TextView)findViewById( R.id.pleaseupdate ) ).setVisibility( View.INVISIBLE );
							city_is_faulu = true;
						}
						else
						{
							( (TextView)findViewById( R.id.nodate ) ).setText( R.string.no_data );
							( (TextView)findViewById( R.id.pleaseupdate ) ).setVisibility( View.VISIBLE );
							if( mAppconfig.isHuaweiStyle() )
							{
								findViewById( R.id.nodata_layout ).setVisibility( View.VISIBLE );
							}
						}
					}
				}
				return;
			}
			findViewById( R.id.nodata_layout ).setVisibility( View.INVISIBLE );
			if( mAppconfig.isHuaweiStyle() )
			{
				findViewById( R.id.cityname ).setVisibility( View.VISIBLE );
				findViewById( R.id.dotsGroup ).setVisibility( View.VISIBLE );
				init_smallDots( mCurrentIndex , mPoscalCodList.size() );
			}
			// ��ǰ����
			String[] weekdayArry = getResources().getStringArray( R.array.weekday );
			String condition;
			ImageView imNeg = (ImageView)findViewById( R.id.imageview_negative );
			ImageView im1 = (ImageView)findViewById( R.id.imageview_tmp1 );
			ImageView im2 = (ImageView)findViewById( R.id.imageview_tmp2 );
			int temp = mDataEntity.getTempC();
			if( temp >= 0 )
			{
				imNeg.setVisibility( View.GONE );
			}
			else
			{
				imNeg.setVisibility( View.VISIBLE );
			}
			int temperature = Math.abs( temp );
			if( temperature < 10 )
			{
				im1.setVisibility( View.GONE );
				im2.setImageResource( R.drawable.tp_0 + temperature );
			}
			else
			{
				im1.setVisibility( View.VISIBLE );
				if( temperature >= 0 )
				{
					int decade = temperature / 10;
					int digit = temperature % 10;
					im1.setImageResource( R.drawable.tp_0 + decade );
					im2.setImageResource( R.drawable.tp_0 + digit );
				}
			}
			// ����������������
			if( !mAppconfig.isHuaweiStyle() )
			{
				tv = (TextView)findViewById( R.id.textview_totay_high_tmp );
				tv.setText( mDataEntity.getDetails().get( 0 ).getHight().toString() + "℃" );
				tv = (TextView)findViewById( R.id.textview_totay_low_tmp );
				tv.setText( mDataEntity.getDetails().get( 0 ).getLow().toString() + "℃" );
			}
			else
			{
				condition = mDataEntity.getDetails().get( 0 ).getCondition();
				String[] str = condition.split( "转" );
				tv = (TextView)findViewById( R.id.tv_condition );
				tv.setText( str[0] );
				tv = (TextView)findViewById( R.id.tv_time );
				String updateTime;
				Date date = new Date( mDataEntity.getLastUpdateTime() );
				SimpleDateFormat sDateFormat = new SimpleDateFormat( "MM/dd" );
				updateTime = sDateFormat.format( date );
				tv.setText( updateTime );
				tv = (TextView)findViewById( R.id.tv_week );
				tv.setText( weekdayArry[mDataEntity.getDetails().get( 0 ).getDayOfWeek()] );
				if( "".equals( mDataEntity.getLunarcalendar() ) || null == mDataEntity.getLunarcalendar() )
				{
					findViewById( R.id.tv_lunar_calendar ).setVisibility( View.GONE );
					findViewById( R.id.tv_calendar ).setVisibility( View.GONE );
				}
				else
				{
					findViewById( R.id.tv_lunar_calendar ).setVisibility( View.VISIBLE );
					findViewById( R.id.tv_calendar ).setVisibility( View.VISIBLE );
					tv = (TextView)findViewById( R.id.tv_calendar );
					tv.setText( mDataEntity.getLunarcalendar() );
				}
				tv = (TextView)findViewById( R.id.tv_humidity );
				String hum = mDataEntity.getHumidity();
				if( !"".equals( hum ) && null != hum )
				{
					hum = hum + "%";
					tv.setText( hum );
				}
				else
				{
					tv.setText( "无数据" );
				}
				String[] windCondition = mDataEntity.getWindCondition().split( ";" );
				if( !"".equals( windCondition[0] ) && null != windCondition[0] )
				{
					if( !"".equals( windCondition[0].split( "," )[0] ) && null != windCondition[0].split( "," )[0] )
					{
						tv = (TextView)findViewById( R.id.tv_wind_direction );
						tv.setText( windCondition[0].split( "," )[0] );
					}
					if( !"".equals( windCondition[0].split( "," )[1] ) && null != windCondition[0].split( "," )[1] )
					{
						tv = (TextView)findViewById( R.id.tv_wind_force );
						tv.setText( windCondition[0].split( "," )[1] );
					}
				}
			}
			{
				// if video can not play,display image
				iv = (ImageView)findViewById( R.id.weather_image_shoot );
				// ͼƬ̫��Ҫ����һ��
				int resid = WeatherConditionImage.getFullConditionImage( mDataEntity.getCondition() );
				if( mAppconfig.isHuaweiStyle() )
				{
					resid = WeatherConditionImage.getFullConditionImageHuawei( mDataEntity.getCondition() );
				}
				iv.setBackgroundResource( resid );
				iv.setVisibility( View.VISIBLE );
			}
			// ��һ��
			tv = (TextView)findViewById( R.id.cell_textview_week01 );
			tv.setText( weekdayArry[mDataEntity.getDetails().get( 0 ).getDayOfWeek() > 6 ? mDataEntity.getDetails().get( 0 ).getDayOfWeek() % 7 : mDataEntity.getDetails().get( 0 ).getDayOfWeek()] );
			tv = (TextView)findViewById( R.id.textview_tmp01 );
			tv.setText( mDataEntity.getDetails().get( 0 ).getHight() + "℃" + "/" + mDataEntity.getDetails().get( 0 ).getLow() + "℃" );
			iv = (ImageView)findViewById( R.id.imageview_week01 );
			// shanjie 20130130
			if( true )
			{
				if( mAppconfig.isHuaweiStyle() )
				{
					iv.setImageResource( WeatherConditionImage.getConditionImageHuawei( mDataEntity.getDetails().get( 0 ).getCondition() ) );
				}
				else
				{
					iv.setImageResource( WeatherConditionImage.getConditionImage( mDataEntity.getDetails().get( 0 ).getCondition() ) );
				}
				tv = (TextView)findViewById( R.id.textview_condition1 );
				condition = mDataEntity.getDetails().get( 0 ).getCondition();
				String[] str1 = condition.split( "转" );
				tv.setText( str1[0] );
			}
			// �ڶ���
			tv = (TextView)findViewById( R.id.cell_textview_week02 );
			tv.setText( weekdayArry[mDataEntity.getDetails().get( 1 ).getDayOfWeek() > 6 ? mDataEntity.getDetails().get( 1 ).getDayOfWeek() % 7 : mDataEntity.getDetails().get( 1 ).getDayOfWeek()] );
			tv = (TextView)findViewById( R.id.textview_tmp02 );
			tv.setText( mDataEntity.getDetails().get( 1 ).getHight() + "℃" + "/" + mDataEntity.getDetails().get( 1 ).getLow() + "℃" );
			iv = (ImageView)findViewById( R.id.imageview_week02 );
			// iv.setImageResource(WeatherConditionImage.getConditionImage(mDataEntity.getDetails().get(1).getCondition()));
			if( mAppconfig.isHuaweiStyle() )
			{
				iv.setImageResource( WeatherConditionImage.getConditionImageHuawei( mDataEntity.getDetails().get( 1 ).getCondition() ) );
			}
			else
			{
				iv.setImageResource( WeatherConditionImage.getConditionImage( mDataEntity.getDetails().get( 1 ).getCondition() ) );
			}
			tv = (TextView)findViewById( R.id.textview_condition2 );
			condition = mDataEntity.getDetails().get( 1 ).getCondition();
			String[] str2 = condition.split( "转" );
			tv.setText( str2[0] );
			// ������
			tv = (TextView)findViewById( R.id.cell_textview_week03 );
			tv.setText( weekdayArry[mDataEntity.getDetails().get( 2 ).getDayOfWeek() > 6 ? mDataEntity.getDetails().get( 2 ).getDayOfWeek() % 7 : mDataEntity.getDetails().get( 2 ).getDayOfWeek()] );
			tv = (TextView)findViewById( R.id.textview_tmp03 );
			tv.setText( mDataEntity.getDetails().get( 2 ).getHight() + "℃" + "/" + mDataEntity.getDetails().get( 2 ).getLow() + "℃" );
			iv = (ImageView)findViewById( R.id.imageview_week03 );
			// iv.setImageResource(WeatherConditionImage.getConditionImage(mDataEntity.getDetails().get(2).getCondition()));
			if( mAppconfig.isHuaweiStyle() )
			{
				iv.setImageResource( WeatherConditionImage.getConditionImageHuawei( mDataEntity.getDetails().get( 2 ).getCondition() ) );
			}
			else
			{
				iv.setImageResource( WeatherConditionImage.getConditionImage( mDataEntity.getDetails().get( 2 ).getCondition() ) );
			}
			tv = (TextView)findViewById( R.id.textview_condition3 );
			condition = mDataEntity.getDetails().get( 2 ).getCondition();
			String[] str3 = condition.split( "转" );
			tv.setText( str3[0] );
			// ������
			tv = (TextView)findViewById( R.id.cell_textview_week04 );
			tv.setText( weekdayArry[mDataEntity.getDetails().get( 3 ).getDayOfWeek() > 6 ? mDataEntity.getDetails().get( 3 ).getDayOfWeek() % 7 : mDataEntity.getDetails().get( 3 ).getDayOfWeek()] );
			tv = (TextView)findViewById( R.id.textview_tmp04 );
			tv.setText( mDataEntity.getDetails().get( 3 ).getHight() + "℃" + "/" + mDataEntity.getDetails().get( 3 ).getLow() + "℃" );
			iv = (ImageView)findViewById( R.id.imageview_week04 );
			//iv.setImageResource(WeatherConditionImage.getConditionImage(mDataEntity.getDetails().get(3).getCondition()));
			if( mAppconfig.isHuaweiStyle() )
			{
				iv.setImageResource( WeatherConditionImage.getConditionImageHuawei( mDataEntity.getDetails().get( 3 ).getCondition() ) );
			}
			else
			{
				iv.setImageResource( WeatherConditionImage.getConditionImage( mDataEntity.getDetails().get( 3 ).getCondition() ) );
			}
			tv = (TextView)findViewById( R.id.textview_condition4 );
			condition = mDataEntity.getDetails().get( 3 ).getCondition();
			String[] str4 = condition.split( "转" );
			tv.setText( str4[0] );
			if( !mAppconfig.isHuaweiStyle() )
			{
				tv = (TextView)findViewById( R.id.textview_time );
			}
			else
			{
				tv = (TextView)findViewById( R.id.tv_updatetime );
			}
			// ��ʾ����ʱ��
			long milis = mDataEntity.getLastUpdateTime();
			String updateTime;
			Date date = new Date( milis );
			SimpleDateFormat sDateFormat = new SimpleDateFormat( "MM-dd HH:mm" );
			updateTime = sDateFormat.format( date );
			// print now & milis
			if( mContext == null )
				return;
			String str = mContext.getResources().getString( R.string.lasttime );
			tv.setText( updateTime + str );
		}
		catch( Exception e )
		{
			Log.e( TAG , "com.cooee.widget.weither ERROR!! e = " + e );
			mHandler.obtainMessage( REFRESH_FAILED ).sendToTarget();
			e.printStackTrace();
		}
	}
	
	public void setTestData()
	{
		mDataEntity = new weatherdataentity();
		mDataEntity.setTestData();
	}
	
	private void init_smallDots(
			int index ,
			int size )
	{// 初始化滑动圆点
		dotsGroup = (ViewGroup)findViewById( R.id.dotsGroup );
		dotsGroup.removeAllViews();
		// add small dots
		for( int i = 0 ; i < size ; i++ )
		{
			ImageView dot_image = new ImageView( getBaseContext() );
			dot_image.setLayoutParams( new LayoutParams( LayoutParams.WRAP_CONTENT , LayoutParams.WRAP_CONTENT ) );
			dot_image.setPadding( 6 , 6 , 6 , 6 );
			if( i == index )
			{ // the picture of selected small dot is different
				dot_image.setImageResource( R.drawable.spot_select );
			}
			else
			{
				dot_image.setImageResource( R.drawable.spot_normal );
			}
			dotsGroup.addView( dot_image );
		}
	}
	
	public void requestData()
	{
		Log.i( TAG , "requestData!!!requesting = " + requesting );
		if( !requesting )
		{
			Log.i( TAG , "===== " + requesting );
			Intent intent = new Intent( mContext , com.cooee.app.cooeeweather.dataprovider.weatherDataService.class );
			intent.setAction( "com.cooee.app.cooeeweather.dataprovider.weatherDataService" );
			intent.putExtra( "postalCode" , mCurrentPostalCode );
			intent.putExtra( "forcedUpdate" , 1 ); // ǿ�Ƹ���
			boolean found = false;
			String wc = null;
			int i = 0;
			try
			{
				ContentResolver resolver = getContentResolver();
				Uri uri = Uri.parse( MainActivity.POSTALCODE_URI );
				Cursor cursor = resolver
						.query( uri , PostalCodeEntity.projection , PostalCodeEntity.POSTAL_CODE + " = '" + mCurrentPostalCode + "'" + " and " + PostalCodeEntity.USER_ID + " = '0'" , null , null );
				if( cursor != null )
				{
					if( cursor.moveToFirst() )
					{
						found = true;
						wc = cursor.getString( 6 );
						i = 1;
						if( wc == null || "".equals( wc ) || "null".equals( wc ) )
						{
							wc = mCurrentPostalCode;
							i = 0;
						}
					}
					cursor.close();
				}
				intent.putExtra( "forcedUpdate" , 1 );
				intent.putExtra( "postmark" , makeTimePostMark() );
				intent.putExtra( "wc" , i );
				intent.putExtra( "wcname" , wc );
				intent.putExtra( "foreignCity" , ResolverUtil.checkCityIsForeignCity( mContext , mCurrentPostalCode ) );
			}
			catch( Exception e )
			{
				// TODO Auto-generated catch block
				Log.i( TAG , "requestData!!! e = " + e );
				e.printStackTrace();
			}
			startService( intent );
			requesting = true;
		}
	}
	
	private String makeTimePostMark()
	{
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get( Calendar.YEAR );
		int month = calendar.get( Calendar.MONTH );
		month = month + 1;
		int day = calendar.get( Calendar.DAY_OF_MONTH );
		int hour = calendar.get( Calendar.HOUR_OF_DAY );
		int min = calendar.get( Calendar.MINUTE );
		int srd = calendar.get( Calendar.SECOND );
		int ms = calendar.get( Calendar.MILLISECOND );
		return String.format( "%d.%d.%d.%d.%d.%d.%d" , year , month , day , hour , min , srd , ms );
	}
	
	@Override
	protected void onActivityResult(
			int requestCode ,
			int resultCode ,
			Intent data )
	{
		// ���Ը�ݶ���������������Ӧ�Ĳ���
		if( ( 0 == resultCode ) && ( ( mPoscalCodList == null ) || ( mPoscalCodList.size() == 0 ) ) )
		{
			finish();
		}
		else
		{
			if( 0 != resultCode )
			{
				Bundle bunde = data.getExtras();
				String cityname = bunde.getString( "citys" );
				mDataEntity = null;
				updateEntity();
				for( int i = 0 ; i < mPoscalCodList.size() ; i++ )
				{
					if( mPoscalCodList.get( i ).equals( cityname ) )
					{
						mCurrentIndex = i;
						mCurrentPostalCode = cityname;
						break;
					}
				}
				updateViews();
			}
			// changePostalCode();
		}
	}
	
	public void readData()
	{
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		Uri uri;
		String selection;
		uri = Uri.parse( WEATHER_URI + "/" + mCurrentPostalCode );
		selection = weatherdataentity.POSTALCODE + "=" + "'" + mCurrentPostalCode + "'" + " or " + weatherdataentity.CITY + " = " + "'" + mCurrentPostalCode + "'";
		if( !"none".equals( mCurrentPostalCode ) )
		{
			cursor = resolver.query( uri , weatherdataentity.projection , selection , null , null );
			if( null != cursor )
			{
				mDataEntity = new weatherdataentity();
				if( cursor.moveToFirst() )
				{
					mDataEntity.setUpdateMilis( cursor.getInt( 0 ) );
					mDataEntity.setCity( cursor.getString( 1 ) );
					//Log.i( "minghui" , "cursor.getString( 2 ) = " + cursor.getString( 2 ) );
					mDataEntity.setPostalCode( cursor.getString( 2 ) );
					mDataEntity.setForecastDate( cursor.getLong( 3 ) );
					String language = this.getResources().getConfiguration().locale.getCountry();
					mDataEntity.setCondition( WeatherCondition.convertCondition( cursor.getString( 4 ) , language ) );
					language = null;
					mDataEntity.setTempF( cursor.getInt( 5 ) );
					mDataEntity.setTempC( cursor.getInt( 6 ) );
					mDataEntity.setHumidity( cursor.getString( 7 ) );
					mDataEntity.setIcon( cursor.getString( 8 ) );
					mDataEntity.setWindCondition( cursor.getString( 9 ) );
					mDataEntity.setLastUpdateTime( cursor.getLong( 10 ) );
					mDataEntity.setIsConfigured( cursor.getInt( 11 ) );
					mDataEntity.setLunarcalendar( cursor.getString( 12 ) );
					mDataEntity.setUltravioletray( cursor.getString( 13 ) );
					mDataEntity.setWeathertime( cursor.getString( 14 ) );
				}
				int count = 0;
				while( cursor.moveToNext() )
				{
					Log.v( TAG , "updateMilis[" + count + "] = " + cursor.getInt( 0 ) );
					Log.v( TAG , "city[" + count + "] = " + cursor.getString( 1 ) );
					Log.v( TAG , "postcalCode[" + count + "] = " + cursor.getString( 2 ) );
					count++;
				}
				cursor.close();
			}
		}
		int details_count = 0;
		if( mDataEntity != null )
		{
			uri = Uri.parse( WEATHER_URI + "/" + mCurrentPostalCode + "/detail" );
			selection = weatherforecastentity.CITY + "=" + "'" + mCurrentPostalCode + "'" + " or " + weatherdataentity.POSTALCODE + " = " + "'" + mCurrentPostalCode + "'";
			cursor = resolver.query( uri , weatherforecastentity.forecastProjection , selection , null , null );
			if( cursor != null )
			{
				weatherforecastentity forecast;
				while( cursor.moveToNext() )
				{
					forecast = new weatherforecastentity();
					forecast.setDayOfWeek( cursor.getInt( 2 ) );
					forecast.setLow( cursor.getInt( 3 ) );
					forecast.setHight( cursor.getInt( 4 ) );
					forecast.setIcon( cursor.getString( 5 ) );
					String language = this.getResources().getConfiguration().locale.getCountry();
					forecast.setCondition( WeatherCondition.convertCondition( cursor.getString( 6 ) , language ) );
					language = null;
					// forecast.setWidgetId(cursor.getInt(6));
					mDataEntity.getDetails().add( forecast );
					details_count = details_count + 1;
				}
				cursor.close();
			}
		}
		Log.v( TAG , "details_count = " + details_count );
		if( details_count < 4 )
		{
			mDataEntity = null;
		}
		// 读完数据重新显示
		if( mDataEntity != null )
		{
			updateViews();
		}
	}
	
	public void readPostalCodeList()
	{
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		Uri uri = Uri.parse( POSTALCODE_URI );
		if( !AppConfig.getInstance( mContext ).isPosition() )
		{
			CheckDefaultCity();
		}
		// �����mPoscalCodList
		mPoscalCodList.clear();
		String selection;
		selection = PostalCodeEntity.USER_ID + "=" + "'0'";
		cursor = resolver.query( uri , PostalCodeEntity.projection , selection , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				do
				{
					PostalCodeEntity mPostalCodeEntity;
					mPostalCodeEntity = new PostalCodeEntity();
					mPostalCodeEntity.setPostalCode( cursor.getString( 0 ) );
					mPostalCodeEntity.setUserId( cursor.getString( 1 ) );
					mPostalCodeEntity.setAuto_locate( "true".equalsIgnoreCase( cursor.getString( 8 ) ) );
					if( !"none".equals( mPostalCodeEntity.getPostalCode() ) )
					{
						mPoscalCodList.add( mPostalCodeEntity.getPostalCode() );
						/*Log.i( TAG , "readPostalCodeList!!!   mPostalCodeEntity.getPostalCode() = " + mPostalCodeEntity.getPostalCode() );
						Log.i( TAG , "readPostalCodeList!!!   mPostalCodeEntity.isAuto_locate() = " + mPostalCodeEntity.isAuto_locate() );
						entity.put( mPostalCodeEntity.getPostalCode() , mPostalCodeEntity.isAuto_locate() );*/
					}
					Log.v( TAG , "readPostalCodeList!!!mPostalCodeEntity.getPostalCode() = " + mPostalCodeEntity.getPostalCode() + ",userID=" + mPostalCodeEntity.getUserId() );
				}
				while( cursor.moveToNext() );
			}
			cursor.close();
		}
	}
	
	public void CheckDefaultCity()
	{
		// Add default city <add by liuhailin begin>
		boolean isContainsDefaultCity = false;
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		Uri uri = Uri.parse( POSTALCODE_URI );
		String defaultCity = AppConfig.getInstance( this ).getDefaultCity(); /*getResources().getString( R.string.default_city );*/
		Log.i( "weatherDataService" , "CheckDefaultCity ---defaultCity = " + defaultCity );
		cursor = resolver.query( uri , PostalCodeEntity.projection , PostalCodeEntity.POSTAL_CODE + " = '" + defaultCity + "'" + " and " + PostalCodeEntity.USER_ID + " = '0'" , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				isContainsDefaultCity = true;
			}
			cursor.close();
		}
		if( !isContainsDefaultCity )
		{
			ContentValues values = new ContentValues();
			values.put( PostalCodeEntity.POSTAL_CODE , AppConfig.getInstance( this ).getDefaultCity() );
			values.put( PostalCodeEntity.USER_ID , 0 );
			resolver.insert( uri , values );
		}
		// Add default city <add by liuhailin end>
	}
	
	public void setCurrentPostalCode()
	{
		// ���mCurrentIndex��mCurrentPostalCode
		if( mPoscalCodList.size() == 0 )
		{
			mCurrentIndex = 0;
			mCurrentPostalCode = "none";
		}
		else
			if( mCurrentIndex > mPoscalCodList.size() - 1 )
		{ // ���mCurrentIndex�ǲ��Ǳ�ɾ��
			// ���ó����һ��
			mCurrentIndex = mPoscalCodList.size() - 1;
			mCurrentPostalCode = mPoscalCodList.get( mCurrentIndex );
		}
		else
		{
			// ��һ�ν���ʱ��Ĭ��Ϊ��mCurrentIndex��
			mCurrentPostalCode = mPoscalCodList.get( mCurrentIndex );
		}
	}
	
	/**
	 * ������CurrentPostalCode��CurrentIndex���ٵ��ô˺������ػ���Ļ
	 */
	public void changePostalCode()
	{
		mDataEntity = null;
		// ���û�������������£���ֱ�Ӷ�ȡ���
		if( mSettingEntity.getUpdateWhenOpen() != 1 )
		{
			readData();
		}
		updateViews();
	}
	
	// �ֶ�����
	public void refreshData()
	{
		Log.i( TAG , "refreshData!!!" );
		mDataEntity = null;
		updateViews();
		mHandler.sendEmptyMessageDelayed( REFRESH_FINISH , 1000 );
	}
	
	public void updateEntity()
	{
		// ��ȡ����
		readSetting();
		// ��ȡ�����б�
		readPostalCodeList();
		// ����mCurrentIndex��mCurrentPostalCode
		setCurrentPostalCode();
		// setTestData();
		// ���û�������������£���ֱ�Ӷ�ȡ���
		if( mSettingEntity.getUpdateWhenOpen() != 1 )
		{
			if( !mCurrentPostalCode.equals( "none" ) )
			{
				readData();
			}
		}
	}
	
	public List<Map<String , Object>> getMenuListData()
	{
		List<Map<String , Object>> list = new ArrayList<Map<String , Object>>();
		Map<String , Object> map;
		final int string_id[] = { R.string.edit_list , R.string.setting };
		for( int i = 0 ; i < string_id.length ; i++ )
		{
			map = new HashMap<String , Object>();
			if( i == 0 )
			{
				map.put( "bg" , R.drawable.popup_item_bg_up );
				map.put( "divider" , null );
			}
			else
				if( i == string_id.length - 1 )
			{
				map.put( "bg" , R.drawable.popup_item_bg_down );
				map.put( "divider" , R.drawable.popup_item_divider );
			}
			else
			{
				map.put( "bg" , R.drawable.popup_item_bg_normal );
				map.put( "divider" , R.drawable.popup_item_divider );
			}
			map.put( "text" , getResources().getString( string_id[i] ) );
			list.add( map );
		}
		return list;
	}
	
	public void launcherSettingMenu()
	{
		LayoutInflater mLayoutInflater = (LayoutInflater)getSystemService( LAYOUT_INFLATER_SERVICE );
		View pop_view = mLayoutInflater.inflate( R.layout.popup_window_layout , null , false );
		ListView listView = (ListView)pop_view.findViewById( R.id.listview );
		listView.setAdapter(
				new SimpleAdapter(
						this ,
						getMenuListData() ,
						R.layout.popup_window_item_layout ,
						new String[]{ "bg" , "text" , "divider" } ,
						new int[]{ R.id.listitem_bg , R.id.listitem_text , R.id.listitem_divider } ) );
		listView.setDivider( null );
		// ��ʺ��ɫ�ĸ���������
		listView.setOnItemSelectedListener( new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(
					AdapterView<?> arg0 ,
					View arg1 ,
					int arg2 ,
					long arg3 )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onNothingSelected(
					AdapterView<?> arg0 )
			{
				// TODO Auto-generated method stub
			}
		} );
		listView.setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				// �˳�popup
				if( mPop != null )
				{
					mPop.dismiss();
				}
				switch( position )
				{
					case 0:
					{
						// ������б༭����
						Intent intent = new Intent( MainActivity.this , WeatherEditPost.class );
						startActivity( intent );
						break;
					}
					case 1:
					{
						// �������ý���
						Intent intent = new Intent( MainActivity.this , WeatherSetting.class );
						startActivity( intent );
						break;
					}
					default:
						break;
				}
			}
		} );
		// ����popup window
		int width = (int)getResources().getDimension( R.dimen.popup_window_width );
		int height = (int)getResources().getDimension( R.dimen.popup_item_height ) + 2;
		// ��ʱд������Ϊ2
		height = height * 2;
		mPop = new PopupWindow( pop_view , width , height );
		// mPop.setAnimationStyle(R.style.popwindow);
		mPop.setFocusable( true ); // ����PopupWindow�ɻ�ý���
		mPop.setTouchable( true ); // ����PopupWindow�ɴ���
		mPop.setOutsideTouchable( true ); // ���÷�PopupWindow����ɴ���
		ShapeDrawable mShapeDrawable = new ShapeDrawable( new OvalShape() );
		mShapeDrawable.getPaint().setColor( 0x00000000 );
		mShapeDrawable.setBounds( 0 , 0 , width , height );
		mPop.setBackgroundDrawable( mShapeDrawable ); // �����ñ����޷������˳�
		mPop.showAsDropDown( findViewById( R.id.combo_layout ) , 0 , 8 );
		// mPop.showAsDropDown(findViewById(R.id.menu_button), -184 + 30, 8);
	}
	
	@Override
	public void onClick(
			View v )
	{
		if( v == findViewById( R.id.combo_layout ) )
		{
			selectPostalCode();
		}
		else
			if( ( v == findViewById( R.id.button_citymange ) || ( v == findViewById( R.id.home_button1 ) ) ) )
		{
			Intent intent = new Intent( this , WeatherEditPost.class );
			startActivityForResult( intent , CONTEXT_RESTRICTED );// CONTEXT_RESTRICTED
																	// int�ͱ��������Զ���
																	// startActivity(intent);
		}
		else
				if( v == findViewById( R.id.button_refresh ) || ( v == findViewById( R.id.refresh_button2 ) ) )
		{
			refreshData();
		}
		else
					if( R.id.button1 == v.getId() )
		{
			if( mDataEntity != null )
			{
				StringBuffer text = new StringBuffer();
				text.append( mDataEntity.getCity() );
				text.append( "," + getResources().getText( R.string.current_temp ) + "：" );
				text.append( mDataEntity.getTempC() + "°C" );
				text.append( "," + getResources().getText( R.string.high_temp ) + "：" );
				text.append( mDataEntity.getDetails().get( 0 ).getHight() + "°C" );
				text.append( "," + getResources().getText( R.string.low_temp ) + "：" );
				text.append( mDataEntity.getDetails().get( 0 ).getLow() + "°C" );
				// text.append();
				// @gaominghui015/05/27 ADD START  0002892: 闪耀项目：天气分享里面需屏蔽手机管家和朵唯云。
				if( mAppconfig.isDoovShare() )
				{
					Intent it = new Intent( Intent.ACTION_SEND );
					it.setType( "text/plain" );
					List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities( it , 0 );
					if( !resInfo.isEmpty() )
					{
						List<Intent> targetedShareIntents = new ArrayList<Intent>();
						boolean doov = false;
						for( ResolveInfo info : resInfo )
						{
							doov = false;
							Intent targeted = new Intent( Intent.ACTION_SEND );
							targeted.setType( "text/plain" );
							ActivityInfo activityInfo = info.activityInfo;
							// judgments : activityInfo.packageName, activityInfo.name, etc.
							/*ApplicationInfo appInfo = null;
							try
							{
								appInfo = getPackageManager().getApplicationInfo( activityInfo.packageName , PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES );
								
								Log.i( "andy" , "appInfo = "+appInfo );
								Log.i( "andy" , "lable = "+getPackageManager().getApplicationLabel( appInfo ));
							}
							catch( NameNotFoundException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
							//Log.i( "andy" , "packages = " + activityInfo.packageName );
							if( packages != null && classNames != null )
							{
								for( int i = 0 ; i < packages.size() ; i++ )
								{
									if( activityInfo.packageName.contains( packages.get( i ) ) || activityInfo.name.contains( classNames.get( i ) ) )
									{
										doov = true;
										break;
									}
								}
							}
							if( doov )
							{
								continue;
							}
							// targeted.putExtra( Intent.EXTRA_SUBJECT , "分享" );
							targeted.putExtra( Intent.EXTRA_TEXT , text.toString() );
							//Log.i( "andy" , "text = " + text.toString() );
							targeted.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
							targeted.setPackage( activityInfo.packageName );
							targetedShareIntents.add( targeted );
						}
						//Log.i( "ssss" , "targetedShareIntents = " +targetedShareIntents);
						Intent chooserIntent = Intent.createChooser( targetedShareIntents.remove( 0 ) , getTitle() );
						if( chooserIntent == null )
						{
							return;
						}
						chooserIntent.putExtra( Intent.EXTRA_INITIAL_INTENTS , targetedShareIntents.toArray( new Parcelable[]{} ) );
						try
						{
							startActivity( chooserIntent );
						}
						catch( android.content.ActivityNotFoundException ex )
						{
							Toast.makeText( this , "Can't find share component to share" , Toast.LENGTH_SHORT ).show();
						}
					}
				}
				else
				{
					Intent intent = new Intent( Intent.ACTION_SEND );
					intent.setType( "text/plain" );
					intent.putExtra( Intent.EXTRA_SUBJECT , getResources().getText( R.string.share_temp ) );
					intent.putExtra( Intent.EXTRA_TEXT , text.toString() );
					intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
					Intent in = Intent.createChooser( intent , getTitle() );
					startActivity( in );
				}
			}
			// @gaominghui2015/05/27 ADD END
		}
		else
						if( R.id.button2 == v.getId() )
		{
			Intent intent = new Intent( MainActivity.this , WeatherSetting.class );
			startActivity( intent );
		}
	}
	
	public void readSetting()
	{
		// ��ȡ����
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		boolean found = false;
		Uri uri = Uri.parse( MainActivity.SETTING_URI );
		Log.v( TAG , "readSetting uri = " + uri );
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
	
	public void saveSetting()
	{
		// ��������
		ContentResolver resolver = getContentResolver();
		Cursor cursor = null;
		boolean found = false;
		Uri uri = Uri.parse( MainActivity.SETTING_URI );
		cursor = resolver.query( uri , SettingEntity.projection , null , null , null );
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				found = true;
			}
		}
		if( found )
		{
			ContentValues values = new ContentValues();
			values.put( SettingEntity.UPDATE_WHEN_OPEN , mSettingEntity.getUpdateWhenOpen() );
			values.put( SettingEntity.UPDATE_REGULARLY , mSettingEntity.getUpdateRegularly() );
			values.put( SettingEntity.UPDATE_INTERVAL , mSettingEntity.getUpdateInterval() );
			values.put( SettingEntity.SOUND_ENABLE , mSettingEntity.getSoundEnable() );
			values.put( SettingEntity.MAINCITY , mCurrentPostalCode );
			int updateRows;
			updateRows = resolver.update( uri , values , null , null );
			Log.v( TAG , "update setting rows = " + updateRows );
		}
		else
		{
			if( cursor != null )
			{
				cursor.close();
			}
			throw new UnsupportedOperationException();
		}
		if( cursor != null )
		{
			cursor.close();
		}
	}
	
	@Override
	public boolean onTouch(
			View v ,
			MotionEvent event )
	{
		if( v == findViewById( R.id.weather_image_shoot ) )
		{
			if( event.getAction() == MotionEvent.ACTION_DOWN )
			{
				if( mAppconfig.isHuaweiStyle() )
				{
					mInitialX = event.getX();
				}
				else
				{
					mInitialY = event.getY();
				}
			}
			else
				if( event.getAction() == MotionEvent.ACTION_UP )
			{
				Float deltaY = 0F;
				if( mAppconfig.isHuaweiStyle() )
				{
					deltaY = mInitialX - event.getX();
				}
				else
				{
					deltaY = mInitialY - event.getY();
				}
				if( deltaY > SLIDE_SENSITIVITY )
				{
					// ���ϻ������Ƶ���һҳ
					if( mPoscalCodList.size() > 1 )
					{
						mCurrentIndex = mCurrentIndex + 1;
						if( mCurrentIndex == mPoscalCodList.size() )
						{
							mCurrentIndex = 0;
						}
						mCurrentPostalCode = mPoscalCodList.get( mCurrentIndex );
						changePostalCode();
					}
				}
				else
					if( deltaY < -SLIDE_SENSITIVITY )
				{
					// ���»������Ƶ���һҳ
					if( mPoscalCodList.size() > 1 )
					{
						mCurrentIndex = mCurrentIndex - 1;
						if( mCurrentIndex == -1 )
						{
							mCurrentIndex = mPoscalCodList.size() - 1;
						}
						mCurrentPostalCode = mPoscalCodList.get( mCurrentIndex );
						changePostalCode();
					}
				}
			}
		}
		return true;
	}
	
	class DisclaimerDialog extends Dialog
	{
		
		private Context mContext;
		private CheckBox mCheckBox;
		private boolean isNeedWarnningNextTime = false;
		private Handler mHandler = null;
		
		public DisclaimerDialog(
				Context context )
		{
			super( context );
			// TODO Auto-generated constructor stub
		}
		
		public DisclaimerDialog(
				Context context ,
				int theme ,
				Handler handler )
		{
			super( context , theme );
			mContext = context;
			mHandler = handler;
		}
		
		@Override
		protected void onCreate(
				Bundle savedInstanceState )
		{
			// TODO Auto-generated method stub
			Log.v( "oncreate" , "onCreate" );
			super.onCreate( savedInstanceState );
			setCanceledOnTouchOutside( false );
			//inflater = LayoutInflater.from(mContext);
			//contentView = inflater.inflate(R.layout.delete_dialog_layout, null);
			this.setContentView( R.layout.disclaimer_dialog_layout );
			mCheckBox = (CheckBox)findViewById( R.id.check_box );
			mCheckBox.setOnCheckedChangeListener( new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(
						CompoundButton buttonView ,
						boolean isChecked )
				{
					// TODO Auto-generated method stub
					isNeedWarnningNextTime = !isChecked;
				}
			} );
			TextView exitButton = (TextView)findViewById( R.id.dialog_button_exit );
			TextView iKnowButton = (TextView)findViewById( R.id.dialog_button_ok );
			exitButton.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					DisclaimerDialog.this.dismiss();
					//mHandler.obtainMessage( MainActivity.USER_EXIT ).sendToTarget();
					USER_EXIT = true;
					finish();
				}
			} );
			iKnowButton.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					DisclaimerDialog.this.dismiss();
					SharedPreferences prefs = mContext.getSharedPreferences( "weacherClient" , Activity.MODE_PRIVATE );
					Editor connectEditor = prefs.edit();
					connectEditor.putBoolean( "notice" , isNeedWarnningNextTime );
					connectEditor.commit();
					mHandler.obtainMessage( MainActivity.USER_CONTINUE ).sendToTarget();
				}
			} );
		}
		
		@Override
		public boolean onKeyUp(
				int keyCode ,
				KeyEvent event )
		{
			// TODO Auto-generated method stub
			//
			switch( event.getKeyCode() )
			{
				case KeyEvent.KEYCODE_BACK:
					return true;
				default:
					break;
			}
			return super.onKeyUp( keyCode , event );
		}
	}
	
	// gaominghui@2016/12/19 ADD START
	//android 6.0需要检查权限
	private static final int REQUEST_PERMISSION_READ_PHONE_STATE = 1;
	private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2;
	public static String imsi = "";
	public static String imei = "";
	public static String countryIso = "";
	
	// gaominghui@2016/12/22 ADD START
	private void requestPhonePermission(
			String permission ,
			int requestCode )
	{
		if( Build.VERSION.SDK_INT >= 23 )
		{
			requestPermissions( new String[]{ permission } , requestCode );
		}
	}
	
	@Override
	public void onRequestPermissionsResult(
			int requestCode ,
			String[] permissions ,
			int[] grantResults )
	{
		super.onRequestPermissionsResult( requestCode , permissions , grantResults );
		if( requestCode == REQUEST_PERMISSION_READ_PHONE_STATE )
		{
			if( grantResults.length > 0 )
			{
				int grantResult = grantResults[0];
				boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
				if( granted )
				{
					TelephonyManager mTelephonyMgr = (TelephonyManager)mContext.getSystemService( Context.TELEPHONY_SERVICE );
					imsi = mTelephonyMgr.getSubscriberId();
					imei = mTelephonyMgr.getDeviceId();
					countryIso = mTelephonyMgr.getSimCountryIso();
				}
			}
		}
		else
			if( requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE )
		{
			if( grantResults.length > 0 )
			{
				int grantResult = grantResults[0];
				boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
				Log.i( "MainActivity" , "onRequestPermissionsResult granted=" + granted );
			}
		}
	}
	// gaominghui@2016/12/22 ADD END
	// gaominghui@2016/12/19 ADD END
}
