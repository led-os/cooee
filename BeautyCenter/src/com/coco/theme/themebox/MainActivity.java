package com.coco.theme.themebox;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.coco.download.Assets;
import com.coco.download.DownloadList;
import com.coco.font.fontbox.TabFontFactory;
import com.coco.lock2.lockbox.LockService;
import com.coco.lock2.lockbox.OnPanelStatusChangedListener;
import com.coco.lock2.lockbox.PlatformInfo;
import com.coco.lock2.lockbox.TabLockFactory;
import com.coco.lock2.lockbox.util.LockManager;
import com.coco.pub.provider.PubContentProvider;
import com.coco.pub.provider.PubProviderHelper;
import com.coco.scene.scenebox.TabSceneFactory;
import com.coco.theme.themebox.apprecommend.IconAsyncTask;
import com.coco.theme.themebox.apprecommend.LoadRecomandActivity;
import com.coco.theme.themebox.apprecommend.MyAsyncTask;
import com.coco.theme.themebox.apprecommend.MyDBHelper;
import com.coco.theme.themebox.apprecommend.Profile;
import com.coco.theme.themebox.database.service.ConfigurationTabService;
import com.coco.theme.themebox.service.ThemeService;
import com.coco.theme.themebox.service.ThemesDB;
import com.coco.theme.themebox.update.UpdateManager;
import com.coco.theme.themebox.util.DownModule;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Log;
import com.coco.theme.themebox.util.PathTool;
import com.coco.theme.themebox.util.Tools;
import com.coco.wallpaper.wallpaperbox.TabWallpaperFactory;
import com.coco.wf.wfbox.TabEffectFactory;
import com.coco.widget.widgetbox.TabWidgetFactory;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.iLoong.base.themebox.R;
import com.iLoong.launcher.MList.MELOG;
import com.iLoong.launcher.MList.Main_FirstActivity;
import com.iLoong.launcher.MList.Main_SecondActivity;
import com.iLoong.launcher.MList.MeGeneralMethod;
import com.kpsh.sdk.KpshSdk;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.BeautyCenterConfig.OperateUmeng;
import cool.sdk.BeautyCenterConfig.OperateUmeng.IOperateUmengCallbacks;
import cool.sdk.MicroEntry.MicroEntryHelper;


// CF_RTFSC [end]
public class MainActivity extends Activity implements OnPanelStatusChangedListener , OnTouchListener , GestureDetector.OnGestureListener , View.OnClickListener , IOperateUmengCallbacks
{
	
	// @gaominghui2015/03/11 ADD START 统计美华中心进入的时间
	public static long enterTime = 0;
	// @gaominghui2015/03/11 ADD END
	private final String TAG_THEME = "tagTheme";
	private final String TAG_LOCK = "tagLock";
	private DownModule downModule;
	private TabLockFactory tabLock;
	private TabThemeFactory tabTheme;
	private TabWallpaperFactory tabWallpaper;
	private TabFontFactory tabFont;
	private TabWidgetFactory tabWidget;
	private TabSceneFactory tabScene;
	private TabEffectFactory effect;
	private TabHost tabHost;
	// teapotXu_20130304: add start
	// set a flag that indicates whether the ThemeSelectIcon launched the
	// ThemeBox or Launcher app.
	private boolean b_theme_icon_start_launcher = false;
	// teapotXu_20130304: add end
	private LinearLayout layout_recommend;
	private ImageView iv;
	private GestureDetector mGestureDetector;
	private boolean hasMeasured = false;
	private boolean isScrolling = false;
	private float mScrollY;
	private int MAX_HEIGHT = 0;
	private Animation starScaleAnim;
	private ListView listView;
	private ArrayList<AppInfos> appInfos = null;
	private AppAdapter appAdapter;
	private String appIconUrl[];
	private ImageView imageView;
	private boolean isUnfold = false;
	private ImageView starIv;
	private int pressY;
	private ProgressBar mProgressBar;
	private TextView netPrompt = null;
	private Context mContext;
	private Handler mHandler = new Handler();
	private MyDBHelper mDbHelper = null;
	private final String TAG_WALLPAPER = "tagWallpaper";
	private final String TAG_FONT = "tagFont";
	private final String TAG_EFFECT = "tagEffect";
	private boolean isChange = false;
	private View load;
	private final String TAG_SCENE = "tagScene";
	private final String TAG_WIDGET = "tagWidget";
	private int KILL_DELAY = 250;
	private MessageReceiver receiver = null;
	public static boolean isExit = false;
	private View progress = null;
	private Handler handler = new Handler();
	private Handler postHandler = new Handler();
	private Bundle bundle;
	private List<String> listTab;
	private UpdateManager updateManager;
	private DisclaimerDialog mDisclaimerDialog;
	//<c_0000707> liuhailin@2014-08-11 modify begin
	private static boolean isDownloading = false;
	//<c_0000707> liuhailin@2014-08-11 modify end
	// @gaominghui 2015/06/11 ADD START 为了用户在线下载的美化中心，然后有些桌面不需要显示小组件，所以从桌面获取参数判断是否显示某个tab页面
	//默认true显示，false不显示
	private boolean desktop_support_widget = true;
	private boolean desktop_support_lock = true;
	private boolean desktop_support_wallpaper = true;
	private boolean desktop_support_theme = true;
	// @2015/06/11 ADD END
	// @2015/11/12 ADD STARTbrzh桌面传给美化中心的内置主题列表
	private ArrayList<String> themeSortList = new ArrayList<String>();
	// @2015/11/12 ADD END
	private int launcherVersion = -1;
	private String appid = null;
	private String sn = null;
	public SharedPreferences prefs;
	// @gaomignhui2016/01/29 ADD START 友盟统计需要的参数
	/*private int clickThemeTabTimes = 0;//统计点击主题tab的次数
	private int clickWallpaperTabTimes = 0;//统计点击壁纸tab的次数
	private int clickWidgetTabTimes = 0;//统计点击插件tab的次数
	private int clickLockTabTimes = 0;//统计点击锁屏tab的次数
	public static int clickDownloadTimes = 0;//统计美化中心主题，壁纸，插件，锁屏页面点击下载的总次数
	*/// @gaominghui2016/01/29 ADD END
		// gaominghui@2016/11/14 ADD START 监听home键的广播
	private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsReceiver();
	// gaominghui@2016/11/14 ADD END 监听home键的广播
	private Handler mDelayedStopHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			Log.v( "themebox" , "themebox  kill ! msg = " + msg );
			if( Tools.isServiceRunning( mContext , "com.coco.theme.themebox.DownloadApkContentService" ) || Tools.isServiceRunning( mContext , "com.coco.theme.themebox.update.UpdateService" ) )
			{
				Message m = mDelayedStopHandler.obtainMessage();
				mDelayedStopHandler.sendMessageDelayed( m , 10000 );
				return;
			}
			try
			{
				if( isExit )
				{
					if( receiver != null )
					{
						unregisterReceiver( receiver );
						receiver = null;
					}
					if( FunctionConfig.isExitSystemProgress() )
					{
						Log.v( "themebox" , "themebox System.exit( 0 )" );
						// @2015/03/11 ADD START 友盟统计美华中心退出
						//Log.i( "startTime" , " MainActivity mDelayedStopHandler themebox System.exit( 0 ) !!!" );
						//MainActivity.statisticsExitBeautyCenter( mContext );
						// @2015/03/11 ADD END
						System.exit( 0 );
					}
				}
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
			}
		}
	};
	
	//CF_RTFSC [start]
	public Boolean IsWifiAvailable(
			Context context )
	{
		try
		{
			final WifiManager wifiMgr = (WifiManager)context.getSystemService( Context.WIFI_SERVICE );
			return( wifiMgr != null && WifiManager.WIFI_STATE_ENABLED == wifiMgr.getWifiState() );
		}
		catch( Exception e )
		{
			// TODO: handle exception
			return false;
		}
	}
	
	public static Boolean IsNetworkAvailableLocal(
			Context context )
	{
		final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo info = connMgr.getActiveNetworkInfo();
		if( info != null && info.isAvailable() )
		{
			//MELOG.v( "ME_RTFSC" , "=== IsNetworkAvailable  true ===" );
			return true;
		}
		//MELOG.v( "ME_RTFSC" , "=== IsNetworkAvailable  false ===" );
		return false;
	}
	
	//CF_RTFSC [end]
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		//Jone modify. make sure this function will be called before all initialization 
		// gaominghui@2016/11/14 ADD START 注册监听home键广播
		IntentFilter filter = new IntentFilter( Intent.ACTION_CLOSE_SYSTEM_DIALOGS );
		registerReceiver( mCloseSystemDialogsReceiver , filter );
		// gaominghui@2016/11/14 ADD END 注册监听home键广播
		mContext = this;
		//xiatian add start	//适配phenix虚图标逻辑（通过不同activity来实现虚图标的显示图标跟随主题变换）
		String mClassName = getComponentName().getClassName();
		String mTabTag = "";
		if( mClassName.equals( "com.cooee.BeautyCenter.tabTheme" ) )
		{
			mTabTag = "tagTheme";
		}
		else if( mClassName.equals( "com.cooee.BeautyCenter.tabWallpaper" ) )
		{
			mTabTag = "tagWallpaper";
		}
		else if( mClassName.equals( "com.cooee.BeautyCenter.tabLocker" ) )
		{
			mTabTag = "tagLock";
		}
		if( TextUtils.isEmpty( mTabTag ) == false )
		{
			getIntent().putExtra( "currentTab" , mTabTag );
		}
		//xiatian add end
		bindData( this.getIntent() );
		//Jone end  
		ActivityManager.pushActivity( this );
		super.onCreate( savedInstanceState );
		setTheme( R.style.normalTheme ); //防止状态栏显示beauty_logo.png背景
		//CF_RTFSC [start]
		initPushAndStatistics();
		MicroEntryHelper Meh = MicroEntryHelper.getInstance( this );
		if( null != Meh && IsWifiAvailable( mContext ) )
		{
			MELOG.v( "ME_RTFSC" , "WIFI IS Available!!!!" );
			Meh.Update( true );
			Meh.setValue( "ForeThemeBoxUpdateTime" , System.currentTimeMillis() );
		}
		else if( null != Meh && IsNetworkAvailableLocal( mContext ) )
		{
			Long preTime = Meh.getLong( "ForeThemeBoxUpdateTime" , 0L );
			Long curTime = System.currentTimeMillis();
			Log.v( "ME_RTFSC" , "preTime:" + preTime + "   curTime:" + curTime );
			if( Math.abs( curTime - preTime ) >= 1000 * 60 * 60 * 24 * 3L )
			{
				MELOG.v( "ME_RTFSC" , "curTime - preTime:" + Math.abs( curTime - preTime ) + "  ---- " + 1000 * 60 * 60 * 24 * 1L );
				Meh.Update( true );
				Meh.setValue( "ForeThemeBoxUpdateTime" , System.currentTimeMillis() );
			}
		}
		//CF_RTFSC [end]
		mDelayedStopHandler.removeCallbacksAndMessages( null );
		isExit = false;
		Log.v( "themebox" , "onCreate isExit = " + isExit );
		/*if( "true".equals( getString( R.string.is_check_sms_permission ) ) )
		{ //检查是否有发送短信的权限,因为此权限可能被客户删除
			enforceCallingOrSelfPermission( permission.SEND_SMS , "com.iLoong.base.themebox no permission to send sms" );
		}*/
		WallpaperManager wallpaperManager = WallpaperManager.getInstance( mContext );
		new PubProviderHelper( this );
		readDefaultData();
		// @gaominghui2016/01/08 ADD START 
		//如果从第三方桌面或者 系统设置界面进入美化中心时，需要美化中心自己保存桌面给我们传递参数，并且美化中心也需要默认参数，所以需要相应的我们内置桌面的包名以及桌面的pub.provider的数据库的名字
		//但是 bindData的时候还没有初始化配置文件，所以导致拿不到包名，和桌面数据库名字，所以在配置文件获取配置后将这两个参数重新初始化
		SharedPreferences initPref = mContext.getSharedPreferences( "initData" , Context.MODE_PRIVATE );
		if( getIntent().getExtras() == null )
		{
			com.coco.theme.themebox.service.ThemesDB.LAUNCHER_PACKAGENAME = FunctionConfig.getThemeApplyLauncherPackageName();
			initPref.edit().putString( "launcherPackageName" , com.coco.theme.themebox.service.ThemesDB.LAUNCHER_PACKAGENAME ).commit();
			PubContentProvider.LAUNCHER_AUTHORITY = FunctionConfig.getLauncher_pub_provider_authority();
			Assets.setLauncherAuthor( PubContentProvider.LAUNCHER_AUTHORITY );
			initPref.edit().putString( "launcher_authority" , PubContentProvider.LAUNCHER_AUTHORITY ).commit();
		}
		// @gaominghui2016/01/08 ADD END
		if( FunctionConfig.isUmengStatistics_key() )
		{
			// @gaominghui2015/03/11 ADD START 统计用户使用状况
			//MobclickAgent.setDebugMode( true );//友盟统计实时日志的开关，可以在友盟上实时测试统计结果
			MobclickAgent.openActivityDurationTrack( false );
			// @gaominghui 2016/03/09 ADD START 友盟统计添加channa_id
			String channel_id = Assets.getSerialNo( mContext );
			AnalyticsConfig.setChannel( channel_id );
			// @gaominghui 2016/03/09 ADD END 友盟统计添加channa_id
			enterTime = System.currentTimeMillis();
			MobclickAgent.onEvent( this , "EnterBeautyCenter" );
			//Log.v( "startTime" , "start===" + enterTime );
			// @2015/03/11 ADD END
		}
		initCreate( savedInstanceState );
		postHandler.postDelayed( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				initShowDisclaimerDialog();
			}
		} , 1000 );
	}
	
	/**
	 *
	 * @author gaominghui 2016年1月22日
	 */
	private void initPushAndStatistics()
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				KpshSdk.setAppKpshTag( mContext , mContext.getPackageName() );
				CooeeSdk.initCooeeSdk( mContext );
				prefs = mContext.getSharedPreferences( "themebox" , Activity.MODE_PRIVATE );
				try
				{
					JSONObject tmp = Assets.getConfig( mContext );
					//JSONObject config = tmp.getJSONObject( "config" );
					appid = tmp.getString( "app_id" );
					sn = tmp.getString( "serialno" );
				}
				catch( JSONException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try
				{
					launcherVersion = getPackageManager().getPackageInfo( mContext.getPackageName() , 0 ).versionCode;
				}
				catch( NameNotFoundException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if( prefs.getBoolean( "first_run" , true ) )
				{
					StatisticsExpandNew.register( mContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContext ) , 1 , MainActivity.this.getApplication().getPackageName() , "" + launcherVersion );
				}
				else
				{
					StatisticsExpandNew.startUp( mContext , sn , appid , CooeeSdk.cooeeGetCooeeId( mContext ) , 1 , MainActivity.this.getApplication().getPackageName() , "" + launcherVersion );
				}
			}
		} ).start();
	}
	
	private void initCreate(
			Bundle savedInstanceState )
	{
		if( ( !FunctionConfig.personal_center_internal && receiver == null ) || FunctionConfig.isEnable_background_configuration_tab() )
		{
			receiver = new MessageReceiver();
			IntentFilter filter = new IntentFilter();
			if( !com.coco.theme.themebox.util.FunctionConfig.personal_center_internal )
			{
				filter.addAction( "com.cooee.launcher.action.start" );
				filter.addAction( "com.cooee.scene.action.SHOW_IDLE" );
			}
			if( FunctionConfig.isEnable_background_configuration_tab() )
			{
				filter.addAction( "com.coco.action.TAB_CHANGED" );
			}
			registerReceiver( receiver , filter );
		}
		if( !com.coco.theme.themebox.StaticClass.isAllowDownload( mContext ) && com.coco.theme.themebox.util.FunctionConfig.isDownToInternal() )
		{
			com.coco.theme.themebox.StaticClass.canDownToInternal = true;
		}
		PathTool.makeDirApp();
		Boolean lockScreen = PreferenceManager.getDefaultSharedPreferences( this ).getBoolean( com.coco.lock2.lockbox.StaticClass.ENABLE_LOCK , false );
		if( !PlatformInfo.getInstance( this ).isSupportViewLock() || !lockScreen )
		{
			startService( new Intent( MainActivity.this , LockService.class ) );
		}
		// teapotXu_20130304: add start
		// set a flag that indicates whether the ThemeSelectIcon launched the
		// ThemeBox or Launcher app.
		// @2016/01/11 ADD START
		SharedPreferences initPref = mContext.getSharedPreferences( "initData" , Context.MODE_PRIVATE );
		String pkgNameFromThemeBox = null;
		if( getIntent().getExtras() != null )
		{
			pkgNameFromThemeBox = getIntent().getStringExtra( "FROM_PACKAGE" );
			initPref.edit().putString( "FROM_PACKAGE" , getIntent().getStringExtra( "FROM_PACKAGE" ) ).commit();
		}
		else
		{
			pkgNameFromThemeBox = initPref.getString( "FROM_PACKAGE" , FunctionConfig.getThemeApplyLauncherPackageName() );
		}
		// @2016/01/11 ADD END
		if( pkgNameFromThemeBox != null && pkgNameFromThemeBox.length() > 16 && pkgNameFromThemeBox.substring( 0 , 16 ).equals( "com.coco.themes." ) )
		{
			b_theme_icon_start_launcher = true;
			return;
		}
		// teapotXu_20130304: add end
		if( FunctionConfig.isEnable_eastaeon_style() )
		{
			setContentView( R.layout.main_entrance );
			ImageView lock = (ImageView)findViewById( R.id.lock_grid );
			ImageView theme = (ImageView)findViewById( R.id.theme_grid );
			ImageView wallpaper = (ImageView)findViewById( R.id.wallpaper_grid );
			ImageView effect = (ImageView)findViewById( R.id.effect_grid );
			lock.setImageResource( R.drawable.lock_grid );
			theme.setImageResource( R.drawable.theme_grid );
			wallpaper.setImageResource( R.drawable.wallpaper_grid );
			effect.setImageResource( R.drawable.effect_grid );
			lock.setOnClickListener( this );
			theme.setOnClickListener( this );
			wallpaper.setOnClickListener( this );
			effect.setOnClickListener( this );
		}
		else
		{
			setContentView( R.layout.main_tab_lock );// 因锁屏代码合入，判断锁屏盒子是否安装无效
			progress = findViewById( R.id.progress );
			tabHost = (TabHost)findViewById( R.id.tabhost );
			if( FunctionConfig.isEnable_background_configuration_tab() )
			{
				progress.setVisibility( View.VISIBLE );
				bundle = savedInstanceState;
				new Thread( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						ConfigurationTabService service = new ConfigurationTabService( mContext );
						listTab = service.queryTabList();
						runOnUiThread( new Runnable() {
							
							public void run()
							{
								if( listTab.size() == 0 || DownloadList.getInstance( mContext ).isRefreshTab() )
								{// 没有读取到后台数据或者需要更新后台tab
									if( StaticClass.isHaveInternet( mContext ) )
									{// 有网的情况，下载后台tab数据
										DownloadList.getInstance( mContext ).downTab();
										handler.postDelayed( new Runnable() {
											
											@Override
											public void run()
											{// 30s后没有读到后台数据，显示默认配置的tab
												// TODO Auto-generated method stub
												DownloadList.getInstance( mContext ).stopDownloadTab();
												if( progress != null && progress.getVisibility() == View.VISIBLE )
												{
													progress.setVisibility( View.GONE );
												}
												if( tabHost.getTabWidget() == null )
												{
													for( String item : DownloadList.types )
													{
														listTab.add( item );
													}
													if( tabHost.getTabWidget() == null )
													{
														initContentView( bundle );
													}
												}
											}
										} , 1000 * 30 );
									}
									else
									{
										Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
										if( progress != null && progress.getVisibility() == View.VISIBLE )
										{
											progress.setVisibility( View.GONE );
										}
										for( String item : DownloadList.types )
										{// 没有网的情况，且没有读到保存下来的数据，显示默认配置的tab
											listTab.add( item );
										}
										if( tabHost.getTabWidget() == null )
										{
											initContentView( bundle );
										}
									}
								}
								else
								{
									resetTabs();// 读到后台配置数据，按照后台配置，重新设置开关值
									if( progress != null && progress.getVisibility() == View.VISIBLE )
									{
										progress.setVisibility( View.GONE );
									}
									if( tabHost.getTabWidget() == null )
									{
										initContentView( bundle );
									}
								}
							}
						} );
					}
				} ).start();
			}
			else
			{// 后台配置开关没有打开，直接显示默认配置的tab
				listTab = new ArrayList<String>();
				if( FunctionConfig.getTab_sequence() != null )
				{
					String types[] = FunctionConfig.getTab_sequence().split( "," );
					for( String item : types )
					{
						listTab.add( item );
					}
				}
				else
				{
					for( String item : DownloadList.types )
					{
						listTab.add( item );
					}
					listTab.add( "0" );
				}
				if( tabHost.getTabWidget() == null )
				{
					initContentView( bundle );
				}
			}
		}
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				CooeeSdk.initCooeeSdk( mContext );
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( mContext );
				int count = prefs.getInt( "useCount" , 0 );
				prefs.edit().putInt( "useCount" , ++count ).commit();
				DownloadList.getInstance( mContext ).startUICenterLog( DownloadList.ACTION_USE_LOG , "" , "" );
			}
		} ).start();
		if( FunctionConfig.isEnableUpdateself() )
			mHandler.post( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					if( updateManager == null )
					{
						updateManager = new UpdateManager( mContext );
					}
					updateManager.updateApkInfo( false );
				}
			} );
		//Log.v( "startTime" , "end===" + String.valueOf( System.currentTimeMillis() ) );
	}
	
	private void initShowDisclaimerDialog()
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
		boolean isNeedShowDisclaimerDialog = prefs.getBoolean( "showDisclaimerDialog" , true );
		if( isNeedShowDisclaimerDialog && FunctionConfig.isEnableDisclaimerDialog() )
		{
			mDisclaimerDialog = new DisclaimerDialog( mContext , R.style.dialog );
			mDisclaimerDialog.setOnClickListener( new DisclaimerDialog.OnClickListener() {
				
				@Override
				public void onClick(
						View v ,
						boolean isConfirm ,
						boolean isChecked )
				{
					// TODO Auto-generated method stub
					if( isConfirm )
					{
						Log.v( "MainActivity" , "initShowDisclaimerDialog  isChecked=" + isChecked );
						prefs.edit().putBoolean( "showDisclaimerDialog" , !isChecked ).commit();
						mDisclaimerDialog.dismiss();
					}
					else
					{
						mDisclaimerDialog.dismiss();
						finish();
					}
				}
			} );
			mDisclaimerDialog.show();
		}
	}
	
	//CF_RTFSC [start]	
	private static final String TAG_LACUNHER = "10005";
	private static final String TAG_APPS = "10006";
	
	//CF_RTFSC [end]	
	private void initContentView(
			Bundle savedInstanceState )
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
		boolean isfirst = prefs.getBoolean( "firstThemebox" , true );
		Log.v( "loading" , "----first----" );
		if( FunctionConfig.isLoadingShow() && isfirst )
		{
			load = findViewById( R.id.load );
			ImageView bg = (ImageView)load.findViewById( R.id.loadbg );
			String systemLauncher = Locale.getDefault().getLanguage().toString();
			if( systemLauncher.equals( "zh" ) )
			{
				bg.setImageResource( R.drawable.load );
			}
			else
			{
				bg.setImageResource( R.drawable.loades );
			}
			bg.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					load.setVisibility( View.GONE );
				}
			} );
			load.setVisibility( View.VISIBLE );
			mHandler.postDelayed( loading , 6000 );
			prefs.edit().putBoolean( "firstThemebox" , false ).commit();
		}
		else
		{
			//load.setVisibility( View.GONE );
		}
		downModule = DownModule.getInstance( this );// new DownModule(this);
		LocalActivityManager groupActivity = new LocalActivityManager( this , false );
		groupActivity.dispatchCreate( savedInstanceState );
		tabHost.setup( groupActivity );
		// 添加主题页面
		if( getIntent().getBooleanExtra( "isHWstyle" , false ) )
		{
			tabWallpaper = new TabWallpaperFactory( mContext , downModule );
			tabWallpaper.setHWStyle( true , getIntent().getBooleanExtra( "isDesktopWall" , true ) );
			View indicatorWallpaper = View.inflate( mContext , R.layout.indicator_wallpaper , null );
			tabHost.addTab( tabHost.newTabSpec( TAG_WALLPAPER ).setIndicator( indicatorWallpaper ).setContent( tabWallpaper ) );
			tabHost.getTabWidget().setVisibility( View.GONE );
			return;
		}
		for( String tabid : listTab )
		{
			if( tabid.equals( DownloadList.Theme_Type ) && FunctionConfig.isThemeVisible() )
			{
				tabTheme = new TabThemeFactory( MainActivity.this , downModule );
				final View indicatorTheme = View.inflate( MainActivity.this , R.layout.indicator_theme , null );
				tabHost.addTab( tabHost.newTabSpec( TAG_THEME ).setIndicator( indicatorTheme ).setContent( tabTheme ) );
				continue;
			}
			if( tabid.equals( DownloadList.Lock_Type ) && com.coco.theme.themebox.util.FunctionConfig.isDisplayLock() )
			{
				if( com.coco.theme.themebox.util.FunctionConfig.isLockVisible() )
				{
					// 添加锁屏页面
					tabLock = new TabLockFactory( MainActivity.this , downModule );
					View indicatorLock = View.inflate( MainActivity.this , R.layout.indicator_lock , null );
					tabHost.addTab( tabHost.newTabSpec( TAG_LOCK ).setIndicator( indicatorLock ).setContent( tabLock ) );
				}
				continue;
			}
			//CF_RTFSC [start]
			//场景不再需要，使用if(false)关闭
			//if( tabid.equals( DownloadList.Scene_Type ) && FunctionConfig.isShowSceneTab() )
			if( false )
			//CF_RTFSC [end]
			{
				tabScene = new TabSceneFactory( mContext , downModule );
				View indicatorFont = View.inflate( mContext , R.layout.indicator_scene , null );
				tabHost.addTab( tabHost.newTabSpec( TAG_SCENE ).setIndicator( indicatorFont ).setContent( tabScene ) );
				continue;
			}
			// 添加壁纸页面
			if( tabid.equals( DownloadList.Wallpaper_Type ) && FunctionConfig.isWallpaperVisible() )
			{
				IntentFilter recommendFilter = new IntentFilter();
				recommendFilter.addAction( Intent.ACTION_WALLPAPER_CHANGED );
				registerReceiver( recommendReceiver , recommendFilter );
				tabWallpaper = new TabWallpaperFactory( mContext , downModule );
				View indicatorWallpaper = View.inflate( mContext , R.layout.indicator_wallpaper , null );
				tabHost.addTab( tabHost.newTabSpec( TAG_WALLPAPER ).setIndicator( indicatorWallpaper ).setContent( tabWallpaper ) );
				continue;
			}
			//CF_RTFSC [start]
			// 添加字体页面
			//if( tabid.equals( DownloadList.Font_Type ) && FunctionConfig.isFontVisible() )
			//字体页面不再需要，使用if(false)关闭
			if( false )
			//CF_RTFSC [end]
			{
				tabFont = new TabFontFactory( mContext , downModule );
				View indicatorFont = View.inflate( mContext , R.layout.indicator_font , null );
				tabHost.addTab( tabHost.newTabSpec( TAG_FONT ).setIndicator( indicatorFont ).setContent( tabFont ) );
				continue;
			}
			if( tabid.equals( DownloadList.Widget_Type ) && FunctionConfig.isShowWidgetTab() )
			{
				tabWidget = new TabWidgetFactory( mContext , downModule );
				View indicatorFont = View.inflate( mContext , R.layout.indicator_widget , null );
				tabHost.addTab( tabHost.newTabSpec( TAG_WIDGET ).setIndicator( indicatorFont ).setContent( tabWidget ) );
				continue;
			}
			//CF_RTFSC [start]
			//特效页面不再需要，使用if(false)关闭
			if( false )
			//if( !FunctionConfig.isEnable_background_configuration_tab() )
			//CF_RTFSC [end]
			{
				if( tabid.equals( "0" ) && FunctionConfig.isEffectVisiable() )
				{
					int type = getIntent().getIntExtra( "type" , -1 );
					effect = new TabEffectFactory( mContext , type );
					View indicatorEffect = View.inflate( mContext , R.layout.indicator_effect , null );
					tabHost.addTab( tabHost.newTabSpec( TAG_EFFECT ).setIndicator( indicatorEffect ).setContent( effect ) );
					continue;
				}
			}
		}
		//CF_RTFSC [start]
		//特效页面不再需要，使用if(false)关闭
		if( false )
		//if( FunctionConfig.isEnable_background_configuration_tab() )
		//CF_RTFSC [end]
		{
			if( FunctionConfig.isEffectVisiable() )
			{
				int type = getIntent().getIntExtra( "type" , -1 );
				TabEffectFactory effect = new TabEffectFactory( mContext , type );
				View indicatorEffect = View.inflate( mContext , R.layout.indicator_effect , null );
				tabHost.addTab( tabHost.newTabSpec( TAG_EFFECT ).setIndicator( indicatorEffect ).setContent( effect ) );
			}
		}
		//CF_RTFSC [start]
		MicroEntryHelper microEntryHelper = MicroEntryHelper.getInstance( this );
		MELOG.v( "NE_RTFSC" , "init" );
		MELOG.v( "ME_RTFSC" , "TAG_LACUNHER	 :" + microEntryHelper.getString( TAG_LACUNHER ) );
		MELOG.v( "ME_RTFSC" , "TAG_APPS  :" + microEntryHelper.getString( TAG_APPS ) );
		if( null != microEntryHelper && null != microEntryHelper.getString( TAG_LACUNHER ) && microEntryHelper.getString( TAG_LACUNHER ).equals( "TRUE" ) )
		{
			String LanguageType = getResources().getConfiguration().locale.getLanguage();
			String TabName = microEntryHelper.getString( TAG_LACUNHER + LanguageType );
			View indicatorLauncher = View.inflate( mContext , R.layout.indicator_launcher , null );
			TextView textView = (TextView)indicatorLauncher.findViewById( R.id.tabname );
			textView.setText( TabName );
			Intent tabIntent = new Intent( this , Main_FirstActivity.class );
			if( TAG_LACUNHER.equals( com.coco.theme.themebox.util.FunctionConfig.getApp_id() + "" ) )
			{
				tabIntent.putExtra( "Action" , com.coco.theme.themebox.util.FunctionConfig.getStrAction() );
				tabIntent.putExtra( "ActionDescription" , com.coco.theme.themebox.util.FunctionConfig.getStrActionDescription() );
				tabHost.setCurrentTabByTag( TAG_LACUNHER );
			}
			tabHost.addTab( tabHost.newTabSpec( TAG_LACUNHER ).setIndicator( indicatorLauncher ).setContent( tabIntent ) );
		}
		if( null != microEntryHelper && null != microEntryHelper.getString( TAG_APPS ) && microEntryHelper.getString( TAG_APPS ).equals( "TRUE" ) )
		{
			String LanguageType = getResources().getConfiguration().locale.getLanguage();
			String TabName = microEntryHelper.getString( TAG_APPS + LanguageType );
			View indicatorApps = View.inflate( mContext , R.layout.indicator_apps , null );
			TextView textView = (TextView)indicatorApps.findViewById( R.id.tabname );
			textView.setText( TabName );
			Intent tabIntent = new Intent( this , Main_SecondActivity.class );
			if( TAG_APPS.equals( com.coco.theme.themebox.util.FunctionConfig.getApp_id() + "" ) )
			{
				tabIntent.putExtra( "Action" , com.coco.theme.themebox.util.FunctionConfig.getStrAction() );
				tabIntent.putExtra( "ActionDescription" , com.coco.theme.themebox.util.FunctionConfig.getStrActionDescription() );
				tabHost.setCurrentTabByTag( TAG_APPS );
			}
			tabHost.addTab( tabHost.newTabSpec( TAG_APPS ).setIndicator( indicatorApps ).setContent( tabIntent ) );
		}
		//CF_RTFSC [end]
		if( tabHost.getChildCount() != 2 )
		{
			tabHost.getTabWidget().setBackgroundResource( R.drawable.manager_bar );
		}
		else
		{
			tabHost.getTabWidget().setBackgroundResource( R.drawable.manager_bar_1 );
		}
		if( com.coco.theme.themebox.util.FunctionConfig.isDisplayLock() )
		{
			if( com.coco.theme.themebox.util.FunctionConfig.isRecommendVisible() )
			{
				mDbHelper = new MyDBHelper( mContext );
				IntentFilter recommendFilter = new IntentFilter();
				recommendFilter.addAction( StaticClass.ACTION_THEME_UPDATE_RECOMMEND );
				recommendFilter.addAction( "android.net.conn.CONNECTIVITY_CHANGE" );
				recommendFilter.addAction( Intent.ACTION_WALLPAPER_CHANGED );
				registerReceiver( recommendReceiver , recommendFilter );
				if( com.coco.theme.themebox.util.FunctionConfig.isLockVisible() )
				{
					layout_recommend = (LinearLayout)findViewById( R.id.mainRelLayout );
					mProgressBar = (ProgressBar)findViewById( R.id.circleProgressBar );
					netPrompt = (TextView)findViewById( R.id.internetPrompt );
					imageView = (ImageView)findViewById( R.id.imageViewRecom );
					imageView.setClickable( false );
					iv = (ImageView)findViewById( R.id.labelImageView );
					iv.setOnTouchListener( this );
					iv.setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View v )
						{
						}
					} );
					mGestureDetector = new GestureDetector( this , this );
					mGestureDetector.setIsLongpressEnabled( false );
					calculatorWidth();
					starScaleAnim = AnimationUtils.loadAnimation( this , R.anim.star_scale_anim );
					starIv = (ImageView)findViewById( R.id.starImageView );
					starIv.startAnimation( starScaleAnim );
					listView = (ListView)findViewById( R.id.pushListView );
					appInfos = new ArrayList<AppInfos>();
					appAdapter = new AppAdapter( MainActivity.this , appInfos );
					if( tabHost.getCurrentTab() == 1 )
					{
						layout_recommend.setVisibility( View.VISIBLE );
						mHandler.postDelayed( recommendRun , 0 );
					}
					tabHost.getTabWidget().getChildAt( 0 ).setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View v )
						{
							if( tabHost.getCurrentTab() != 0 )
							{// 一定要判断这个是为了防止阻碍切换事�?
								tabHost.setCurrentTab( 0 );
							}
							else
							{
								// 做你要做的事
							}
							layout_recommend.setVisibility( View.INVISIBLE );
						}
					} );
					tabHost.getTabWidget().getChildAt( 1 ).setOnClickListener( new OnClickListener() {
						
						@Override
						public void onClick(
								View v )
						{
							if( tabHost.getCurrentTab() != 1 )
							{// 一定要判断这个是为了防止阻碍切换事�?
								tabHost.setCurrentTab( 1 );
							}
							else
							{
								// 做你要做的事
							}
							layout_recommend.setVisibility( View.VISIBLE );
							mHandler.postDelayed( recommendRun , 0 );
						}
					} );
				}
			}
		}
		final SharedPreferences perferences = PreferenceManager.getDefaultSharedPreferences( this );
		String def = null;
		if( FunctionConfig.getTabdefaultHighlight() != null )
		{
			def = getTabTag( FunctionConfig.getTabdefaultHighlight() );
		}
		else
		{
			def = TAG_THEME;
		}
		String currentTab = perferences.getString( "currentTab" , def );
		Log.v( "ThemeBox" , "currentTab == " + currentTab );
		int tabIndex = 0;
		SharedPreferences initPref = mContext.getSharedPreferences( "initData" , Context.MODE_PRIVATE );
		if( getIntent().getExtras() != null )
		{
			tabIndex = getIntent().getIntExtra( StaticClass.EXTRA_MAIN_TAB_INDEX , 0 );
			initPref.edit().putInt( "MAIN_TAB_INDEX" , tabIndex ).commit();
		}
		else
		{
			tabIndex = initPref.getInt( "MAIN_TAB_INDEX" , 0 );
		}
		Log.v( "test" , "MAIN_TAB_INDEX:" + initPref.getInt( "MAIN_TAB_INDEX" , 0 ) );
		Log.v( "ThemeBox" , "tabIndex == " + tabIndex );
		if( tabIndex != 1 )
		{
			String tab = null;
			if( getIntent().getExtras() != null )
			{
				tab = getIntent().getStringExtra( "currentTab" );
				initPref.edit().putString( "currentTab" , getIntent().getStringExtra( "currentTab" ) ).commit();
			}
			else
			{
				tab = initPref.getString( "currentTab" , currentTab );
			}
			Log.v( "test" , "currentTab:" + initPref.getString( "currentTab" , currentTab ) );
			boolean isNeedChangeToOnlineTab = false;
			if( getIntent().getExtras() != null )
			{
				isNeedChangeToOnlineTab = getIntent().getBooleanExtra( "isCurrentTabToOnline" , false );
				initPref.edit().putBoolean( "isCurrentTabToOnline" , getIntent().getBooleanExtra( "isCurrentTabToOnline" , false ) ).commit();
			}
			else
			{
				isNeedChangeToOnlineTab = initPref.getBoolean( "isCurrentTabToOnline" , false );
			}
			Log.v( "test" , "isCurrentTabToOnline:" + initPref.getBoolean( "isCurrentTabToOnline" , false ) );
			if( tab != null )
			{
				tabHost.setCurrentTabByTag( tab );
			}
			else
			{
				tabHost.setCurrentTabByTag( currentTab );
			}
			//是否需要进入在线内容页面
			String tabId = tabHost.getCurrentTabTag();
			if( isNeedChangeToOnlineTab )
			{
				if( tabId.equals( TAG_THEME ) )
				{
					tabTheme.changeTab( 1 );
				}
				else if( tabId.equals( TAG_LOCK ) )
				{
					tabLock.changeTab( 1 );
				}
				else if( tabId.equals( TAG_WALLPAPER ) )
				{
					tabWallpaper.changeTab( 1 );
				}
				else if( tabId.equals( TAG_FONT ) )
				{
					tabFont.changeTab( 1 );
				}
				else if( tabId.equals( TAG_WIDGET ) )
				{
					tabWidget.changeTab( 1 );
				}
			}
		}
		String exitFont = perferences.getString( "applyFontExit" , "no" );
		Log.v( "ThemeBox" , "exitFont == " + exitFont );
		if( FunctionConfig.isEnable_topwise_style() && ( exitFont != null && ( exitFont.equals( "hot" ) || exitFont.equals( "local" ) ) ) )
		{
			tabHost.setCurrentTabByTag( TAG_FONT );
			if( exitFont.equals( "hot" ) )
			{
				tabFont.changeTab( 1 );
				tabFont.reloadView();
			}
			prefs.edit().putString( "applyFontExit" , "no" ).commit();
		}
		perferences.edit().putString( "currentTab" , tabHost.getCurrentTabTag() ).commit();
		tabHost.setOnTabChangedListener( new TabHost.OnTabChangeListener() {
			
			@Override
			public void onTabChanged(
					String tabId )
			{
				// TODO Auto-generated method stub
				// @gaominghui2016/01/29 ADD START 友盟统计点击进入主题，壁纸，锁屏，插件页次数
				if( FunctionConfig.isUmengStatistics_key() )
				{
					statisticClickTabsTimes( tabId );
				}
				// @gaominghui2016/01/29 ADD END
				Editor edit = perferences.edit();
				edit.putString( "currentTab" , tabId );
				edit.commit();
				if( tabId.equals( TAG_WALLPAPER ) || tabId.equals( TAG_FONT ) || tabId.equals( TAG_EFFECT ) )
				{
					if( layout_recommend != null )
						layout_recommend.setVisibility( View.INVISIBLE );
				}
				if( downModule != null )
				{
					String type = null;
					if( tabId.equals( TAG_THEME ) )
					{
						type = DownloadList.Theme_Type;
						tabTheme.reloadView();
					}
					else if( tabId.equals( TAG_LOCK ) )
					{
						type = DownloadList.Lock_Type;
					}
					else if( tabId.equals( TAG_WALLPAPER ) )
					{
						type = DownloadList.Wallpaper_Type;
						tabWallpaper.reloadView();
					}
					else if( tabId.equals( TAG_FONT ) )
					{
						type = DownloadList.Font_Type;
						tabFont.reloadView();
					}
					else if( tabId.equals( TAG_SCENE ) )
					{
						type = DownloadList.Scene_Type;
					}
					else if( tabId.equals( TAG_WIDGET ) )
					{
						type = DownloadList.Widget_Type;
					}
					downModule.resetdownThumbList( type );
				}
			}
		} );
	}
	
	private String getTabTag(
			String tabId )
	{
		String tag = null;
		if( tabId.equals( DownloadList.Theme_Type ) )
		{
			tag = TAG_THEME;
		}
		else if( tabId.equals( TAG_LOCK ) )
		{
			tag = TAG_LOCK;
		}
		else if( tabId.equals( DownloadList.Wallpaper_Type ) )
		{
			tag = TAG_WALLPAPER;
		}
		else if( tabId.equals( DownloadList.Font_Type ) )
		{
			tag = TAG_FONT;
		}
		else if( tabId.equals( DownloadList.Scene_Type ) )
		{
			tag = TAG_SCENE;
		}
		else if( tabId.equals( DownloadList.Widget_Type ) )
		{
			tag = TAG_WIDGET;
		}
		else if( tabId.equals( "0" ) )
		{
			tag = TAG_EFFECT;
		}
		else
		{
			tag = TAG_THEME;
		}
		return tag;
	}
	
	/**
	 *友盟统计点击进入主题，壁纸，锁屏，插件页次数
	 * @param tabId
	 * @author gaomignhui 2016年1月29日
	 */
	private void statisticClickTabsTimes(
			String tabId )
	{
		Map<String , String> map_value = new HashMap<String , String>();
		if( tabId.equals( TAG_THEME ) )
		{
			//clickThemeTabTimes = clickThemeTabTimes + 1;
			//map_value.put( "clickThemeTabTimes" , String.valueOf( clickThemeTabTimes ) );
			MobclickAgent.onEvent( mContext , "click_theme" /*, map_value */);
		}
		else if( tabId.equals( TAG_WALLPAPER ) )
		{
			//	clickWallpaperTabTimes += 1;
			//map_value.put( "clickWallpaperTabTimes" , String.valueOf( clickWallpaperTabTimes ) );
			MobclickAgent.onEvent( mContext , "click_wallpaper" /*, map_value */);
		}
		else if( tabId.equals( TAG_LOCK ) )
		{
			//clickLockTabTimes += 1;
			//map_value.put( "clickLockTabTimes" , String.valueOf( clickLockTabTimes ) );
			MobclickAgent.onEvent( mContext , "click_lockscreen" /*, map_value*/);
		}
		else if( tabId.equals( TAG_WIDGET ) )
		{
			/*clickWidgetTabTimes += 1;
			map_value.put( "clickWidgetTabTimes" , String.valueOf( clickWidgetTabTimes ) );*/
			MobclickAgent.onEvent( mContext , "click_widget"/* , map_value */);
		}
	}
	
	private void bindData(
			Intent intent )
	{
		Bundle data = intent.getExtras();
		SharedPreferences initPref = mContext.getSharedPreferences( "initData" , Context.MODE_PRIVATE );
		if( data != null )
		{
			Log.v( "ThemeBox" , "MainActivity bindData size:" + data.size() );
			com.coco.theme.themebox.util.FunctionConfig.setNetVersion( data.getBoolean( "net_version" , false ) );
			// @2016/01/07 ADD START
			initPref.edit().putBoolean( "net_version" , data.getBoolean( "net_version" , false ) ).commit();
			if( com.coco.theme.themebox.util.FunctionConfig.isNetVersion() )
			{
				Log.v( "ThemeBox" , "init turbo theme box" );
				ThemesDB.LAUNCHER_PACKAGENAME = "com.cooeeui.turbolauncher";
				PubContentProvider.LAUNCHER_AUTHORITY = "com.cooeeui.turbolauncher.pub.provider";
				initPref.edit().putString( "net_version" , data.getString( "com.cooeeui.turbolauncher" ) ).commit();
			}
			else
			{
				com.coco.theme.themebox.service.ThemesDB.LAUNCHER_PACKAGENAME = data.getString( "launcherPackageName" );
				initPref.edit().putString( "launcherPackageName" , data.getString( "launcherPackageName" ) ).commit();
			}
			com.coco.theme.themebox.service.ThemesDB.default_theme_package_name = data.getString( "defaultThemePackageName" );
			initPref.edit().putString( "defaultThemePackageName" , data.getString( "defaultThemePackageName" ) ).commit();
			com.coco.theme.themebox.service.ThemesDB.ACTION_LAUNCHER_APPLY_THEME = data.getString( "launcherApplyThemeAction" );
			initPref.edit().putString( "launcherApplyThemeAction" , data.getString( "launcherApplyThemeAction" ) ).commit();
			com.coco.theme.themebox.service.ThemesDB.ACTION_LAUNCHER_RESTART = data.getString( "launcherRestartAction" );
			initPref.edit().putString( "launcherRestartAction" , data.getString( "launcherRestartAction" ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.personal_center_internal = data.getBoolean( "personal_center_internal" );
			initPref.edit().putBoolean( "personal_center_internal" , data.getBoolean( "personal_center_internal" ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setPage_effect_no_radom_style( data.getBoolean( "page_effect_no_radom_style" ) );
			initPref.edit().putBoolean( "page_effect_no_radom_style" , data.getBoolean( "page_effect_no_radom_style" ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setCustomWallpaperPath( data.getString( "customWallpaperPath" ) );
			initPref.edit().putString( "customWallpaperPath" , data.getString( "customWallpaperPath" ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setdoovStyle( data.getBoolean( "isdoovStyle" ) );
			initPref.edit().putBoolean( "isdoovStyle" , data.getBoolean( "isdoovStyle" ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setGalleryPkg( data.getString( "galleryPkg" ) );
			initPref.edit().putString( "galleryPkg" , data.getString( "galleryPkg" ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setEffectVisiable( data.getBoolean( "isEffectVisiable" ) );
			initPref.edit().putBoolean( "isEffectVisiable" , data.getBoolean( "isEffectVisiable" ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setAppliststring( data.getStringArray( "app_list_string" ) );
			if( data.getStringArray( "app_list_string" ) != null && data.getStringArray( "app_list_string" ).length > 0 )
			{
				String[] applist = data.getStringArray( "app_list_string" );
				String app_list_string = null;
				for( int i = 0 ; i < applist.length ; i++ )
				{
					app_list_string += applist[i] + ";";
				}
				initPref.edit().putString( "app_list_string" , app_list_string ).commit();
			}
			com.coco.theme.themebox.util.FunctionConfig.setWorkSpaceliststring( data.getStringArray( "workSpace_list_string" ) );
			if( data.getStringArray( "workSpace_list_string" ) != null )
			{
				String[] applist = data.getStringArray( "workSpace_list_string" );
				String workSpace_list_string = null;
				for( int i = 0 ; i < applist.length ; i++ )
				{
					workSpace_list_string += applist[i] + ";";
				}
				initPref.edit().putString( "workSpace_list_string" , workSpace_list_string ).commit();
			}
			com.coco.theme.themebox.util.FunctionConfig.setDisableSetWallpaperDimensions( data.getBoolean( "disableSetWallpaperDimensions" ) );
			initPref.edit().putBoolean( "disableSetWallpaperDimensions" , data.getBoolean( "disableSetWallpaperDimensions" ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setEnable_add_widget( data.getBoolean( "enable_personalcenetr_click_widget_to_add" ) );
			initPref.edit().putBoolean( "enable_personalcenetr_click_widget_to_add" , data.getBoolean( "enable_personalcenetr_click_widget_to_add" ) ).commit();
			// @gaominghui2015/07/09 ADD START 铂睿智恒 使用uni3.0 桌面，客户要求，launcher 中的预览图，可以由客户配置到手机系统文件中去，美华中心也要和桌面保持一致，所以桌面会传值过来
			String parm = data.getString( "custom_default_theme_path" , "nothing" );
			initPref.edit().putString( "custom_default_theme_path" , data.getString( "custom_default_theme_path" , "nothing" ) ).commit();
			Log.i( "ContentConfig" , "MainActivity custom_default_theme_path = " + parm );
			com.coco.theme.themebox.util.FunctionConfig.setCustom_theme_path_brzh( parm );
			//Log.i( "andy" , "path = "+data.getString( "custom_default_theme_path" ));
			// @2015/07/09 ADD END
			// @gaominghui 2015/06/11 ADD START 为了用户在线下载的美化中心，然后有些桌面不需要显示小组件，所以从桌面获取参数判断是否显示某个tab页面
			//默认true显示，false不显示
			desktop_support_widget = data.getBoolean( "enableShowWidget" , true );
			initPref.edit().putBoolean( "enableShowWidget" , data.getBoolean( "enableShowWidget" , true ) ).commit();
			desktop_support_lock = data.getBoolean( "enableShowLock" , true );
			initPref.edit().putBoolean( "enableShowLock" , data.getBoolean( "enableShowLock" , true ) ).commit();
			desktop_support_wallpaper = data.getBoolean( "enableShowWallpaper" , true );
			initPref.edit().putBoolean( "enableShowWallpaper" , data.getBoolean( "enableShowWallpaper" , true ) ).commit();
			desktop_support_theme = data.getBoolean( "enableShowTheme" , true );
			initPref.edit().putBoolean( "enableShowTheme" , data.getBoolean( "enableShowTheme" , true ) ).commit();
			// @2016/01/07 ADD END
			// @2015/06/11 ADD END
			// @2015/11/12 ADD START brzh uni桌面会传内置主题列表给美化中心，要求美化中心按照桌面传过来的列表顺序显示主题，如果列表为空，则按照美化中心原本显示主题的逻辑显示
			themeSortList = data.getStringArrayList( "sortThemeList" );
			// @2016/01/07 ADD START
			if( data.getStringArrayList( "sortThemeList" ) != null && data.getStringArrayList( "sortThemeList" ).size() > 0 )
			{
				ArrayList<String> list = data.getStringArrayList( "sortThemeList" );
				String sortThemeList = null;
				for( int i = 0 ; i < list.size() ; i++ )
				{
					sortThemeList += list.get( i ) + ";";
				}
				initPref.edit().putString( "sortThemeList" , sortThemeList ).commit();
			}
			// @2016/01/07 ADD END
			com.coco.theme.themebox.util.FunctionConfig.setBrzhSortThemeList( themeSortList );
			// @2015/11/12 ADD END
			// gaominghui@2016/06/20 ADD START 通知美化中心默认主题位置
			com.coco.theme.themebox.util.FunctionConfig.setDefault_theme_show_front( data.getBoolean( "defaultThemeShowFront" ) );
			// gaominghui@2016/06/20 ADD END 通知美化中心默认主题位置
			if( data.getString( "launcher_authority" ) != null )
			{
				Assets.setLauncherAuthor( data.getString( "launcher_authority" ) );
				PubContentProvider.LAUNCHER_AUTHORITY = data.getString( "launcher_authority" );
				// @2016/01/07 ADD START
				initPref.edit().putString( "launcher_authority" , data.getString( "launcher_authority" ) ).commit();
				// @2016/01/07 ADD END
			}
			else
			{
				Assets.setLauncherAuthor( "com.iLoong.launcher.pub.provider" );
				PubContentProvider.LAUNCHER_AUTHORITY = "com.iLoong.launcher.pub.provider";
			}
			if( data.getString( "launcherApplySceneAction" ) != null )
			{
				com.coco.scene.scenebox.StaticClass.APPLY_SCENE = data.getString( "launcherApplySceneAction" );
			}
			else
			{
				com.coco.scene.scenebox.StaticClass.APPLY_SCENE = "com.coco.launcher.apply_scene";
			}
			if( data.getString( "launcherAddWidgetAction" ) != null )
			{
				com.coco.widget.widgetbox.StaticClass.ADD_WIDGET_TO_LAUNCHER = data.getString( "launcherAddWidgetAction" );
				// @2016/01/07 ADD START
				initPref.edit().putString( "launcherAddWidgetAction" , data.getString( "launcherAddWidgetAction" ) ).commit();
				// @2016/01/07 ADD END
			}
			else
			{
				com.coco.widget.widgetbox.StaticClass.ADD_WIDGET_TO_LAUNCHER = "com.coco.launcher.add.widget";
				// @2016/01/07 ADD START
				initPref.edit().putString( "launcherAddWidgetAction" , "com.coco.launcher.add.widget" ).commit();
				// @2016/01/07 ADD END
			}
			//CF_RTFSC [start]
			com.coco.theme.themebox.util.FunctionConfig.setApp_id( data.getInt( "APP_ID" , -1 ) );
			// @2016/01/07 ADD START
			initPref.edit().putInt( "APP_ID" , data.getInt( "APP_ID" , -1 ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setStrAction( data.getString( "Action" ) );
			initPref.edit().putString( "Action" , data.getString( "Action" ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setStrActionDescription( data.getString( "ActionDescription" ) );
			initPref.edit().putString( "ActionDescription" , data.getString( "ActionDescription" ) ).commit();
			// @2016/01/07 ADD END
			MELOG.v(
					"ME_RTFSC" ,
					"[bindData]  APP_ID:" + com.coco.theme.themebox.util.FunctionConfig.getApp_id() + ", Action" + com.coco.theme.themebox.util.FunctionConfig.getStrAction() + ", ActionDescription" + com.coco.theme.themebox.util.FunctionConfig
							.getStrActionDescription() );
			//CF_RTFSC [end]
		}
		else
		{
			/*com.coco.theme.themebox.util.FunctionConfig.personal_center_internal = false;
			com.coco.theme.themebox.util.FunctionConfig.setdoovStyle( false );
			com.coco.theme.themebox.util.FunctionConfig.setEffectVisiable( false );
			com.coco.theme.themebox.util.FunctionConfig.setDisableSetWallpaperDimensions( false );
			com.coco.theme.themebox.util.FunctionConfig.setEnable_add_widget( false );*/
			Log.v( "ThemeBox" , "MainActivity bindData null then read sharedPrefers init data!!!" );
			/*******************************************************************************************/
			// @2016/01/07 ADD START
			com.coco.theme.themebox.util.FunctionConfig.setNetVersion( initPref.getBoolean( "net_version" , false ) );
			initPref.edit().putBoolean( "net_version" , com.coco.theme.themebox.util.FunctionConfig.isNetVersion() ).commit();
			if( com.coco.theme.themebox.util.FunctionConfig.isNetVersion() )
			{
				Log.v( "ThemeBox" , "init turbo theme box" );
				ThemesDB.LAUNCHER_PACKAGENAME = "com.cooeeui.turbolauncher";
				PubContentProvider.LAUNCHER_AUTHORITY = "com.cooeeui.turbolauncher.pub.provider";
			}
			else
			{
				com.coco.theme.themebox.service.ThemesDB.LAUNCHER_PACKAGENAME = initPref.getString( "launcherPackageName" , FunctionConfig.getThemeApplyLauncherPackageName() );
			}
			//initPref.edit().putString( "launcherPackageName" , com.coco.theme.themebox.service.ThemesDB.LAUNCHER_PACKAGENAME );
			com.coco.theme.themebox.service.ThemesDB.default_theme_package_name = initPref.getString( "defaultThemePackageName" , null );
			initPref.edit().putString( "defaultThemePackageName" , com.coco.theme.themebox.service.ThemesDB.default_theme_package_name ).commit();
			com.coco.theme.themebox.service.ThemesDB.ACTION_LAUNCHER_APPLY_THEME = initPref.getString( "launcherApplyThemeAction" , null );
			initPref.edit().putString( "launcherApplyThemeAction" , com.coco.theme.themebox.service.ThemesDB.ACTION_LAUNCHER_APPLY_THEME ).commit();
			com.coco.theme.themebox.service.ThemesDB.ACTION_LAUNCHER_RESTART = initPref.getString( "launcherRestartAction" , null );
			initPref.edit().putString( "launcherRestartAction" , com.coco.theme.themebox.service.ThemesDB.ACTION_LAUNCHER_RESTART ).commit();
			com.coco.theme.themebox.util.FunctionConfig.personal_center_internal = initPref.getBoolean( "personal_center_internal" , true );
			initPref.edit().putBoolean( "personal_center_internal" , com.coco.theme.themebox.util.FunctionConfig.personal_center_internal ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setPage_effect_no_radom_style( initPref.getBoolean( "page_effect_no_radom_style" , false ) );
			initPref.edit().putBoolean( "page_effect_no_radom_style" , initPref.getBoolean( "page_effect_no_radom_style" , false ) ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setCustomWallpaperPath( initPref.getString( "customWallpaperPath" , null ) );
			initPref.edit().putString( "customWallpaperPath" , com.coco.theme.themebox.util.FunctionConfig.getCustomWallpaperPath() ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setdoovStyle( initPref.getBoolean( "isdoovStyle" , false ) );
			initPref.edit().putBoolean( "isdoovStyle" , com.coco.theme.themebox.util.FunctionConfig.isdoovStyle() ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setGalleryPkg( initPref.getString( "galleryPkg" , null ) );
			initPref.edit().putString( "galleryPkg" , com.coco.theme.themebox.util.FunctionConfig.getGalleryPkg() ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setEffectVisiable( initPref.getBoolean( "isEffectVisiable" , false ) );
			initPref.edit().putBoolean( "isEffectVisiable" , com.coco.theme.themebox.util.FunctionConfig.isEffectVisiable() ).commit();
			if( initPref.getString( "app_list_string" , null ) != null )
			{
				String applist = initPref.getString( "app_list_string" , null );
				String[] applistAray = applist.split( ";" );
				com.coco.theme.themebox.util.FunctionConfig.setAppliststring( applistAray );
			}
			else
			{
				com.coco.theme.themebox.util.FunctionConfig.setAppliststring( null );
				initPref.edit().putString( "app_list_string" , null ).commit();
			}
			if( initPref.getString( "workSpace_list_string" , null ) != null )
			{
				String applist = initPref.getString( "workSpace_list_string" , null );
				String[] workSpace_list_string = applist.split( ";" );
				com.coco.theme.themebox.util.FunctionConfig.setWorkSpaceliststring( workSpace_list_string );
			}
			else
			{
				com.coco.theme.themebox.util.FunctionConfig.setWorkSpaceliststring( null );
				initPref.edit().putString( "workSpace_list_string" , null ).commit();
			}
			com.coco.theme.themebox.util.FunctionConfig.setDisableSetWallpaperDimensions( initPref.getBoolean( "disableSetWallpaperDimensions" , false ) );
			initPref.edit().putBoolean( "disableSetWallpaperDimensions" , com.coco.theme.themebox.util.FunctionConfig.getDisableSetWallpaperDimensions() ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setEnable_add_widget( initPref.getBoolean( "enable_personalcenetr_click_widget_to_add" , false ) );
			initPref.edit().putBoolean( "enable_personalcenetr_click_widget_to_add" , com.coco.theme.themebox.util.FunctionConfig.isEnable_add_widget() ).commit();
			// @gaominghui2015/07/09 ADD START 铂睿智恒 使用uni3.0 桌面，客户要求，launcher 中的预览图，可以由客户配置到手机系统文件中去，美华中心也要和桌面保持一致，所以桌面会传值过来
			String parm = initPref.getString( "custom_default_theme_path" , "nothing" );
			initPref.edit().putString( "custom_default_theme_path" , parm ).commit();
			Log.v( "test" , "custom_default_theme_path:" + initPref.getString( "custom_default_theme_path" , "nothing" ) );
			com.coco.theme.themebox.util.FunctionConfig.setCustom_theme_path_brzh( parm );
			// @gaominghui 2015/06/11 ADD START 为了用户在线下载的美化中心，然后有些桌面不需要显示小组件，所以从桌面获取参数判断是否显示某个tab页面
			//默认true显示，false不显示
			desktop_support_widget = initPref.getBoolean( "enableShowWidget" , true );
			initPref.edit().putBoolean( "enableShowWidget" , desktop_support_widget ).commit();
			desktop_support_lock = initPref.getBoolean( "enableShowLock" , true );
			initPref.edit().putBoolean( "enableShowLock" , desktop_support_lock ).commit();
			desktop_support_wallpaper = initPref.getBoolean( "enableShowWallpaper" , true );
			initPref.edit().putBoolean( "enableShowWallpaper" , desktop_support_wallpaper ).commit();
			desktop_support_theme = initPref.getBoolean( "enableShowTheme" , true );
			initPref.edit().putBoolean( "enableShowTheme" , desktop_support_theme ).commit();
			// @2015/11/12 ADD START brzh uni桌面会传内置主题列表给美化中心，要求美化中心按照桌面传过来的列表顺序显示主题，如果列表为空，则按照美化中心原本显示主题的逻辑显示
			String sortThemeList = initPref.getString( "sortThemeList" , null );
			initPref.edit().putString( "sortThemeList" , sortThemeList ).commit();
			if( sortThemeList != null )
			{
				ArrayList<String> list = new ArrayList<String>();
				String[] temp = sortThemeList.split( ";" );
				for( int i = 0 ; i < temp.length ; i++ )
				{
					list.add( i , temp[i] );
				}
				themeSortList = list;
			}
			com.coco.theme.themebox.util.FunctionConfig.setBrzhSortThemeList( themeSortList );
			String launcher_authority = initPref.getString( "launcher_authority" , FunctionConfig.getLauncher_pub_provider_authority() );
			initPref.edit().putString( "launcher_authority" , launcher_authority ).commit();
			if( launcher_authority != null && !launcher_authority.equals( "nothing" ) )
			{
				Assets.setLauncherAuthor( launcher_authority );
				PubContentProvider.LAUNCHER_AUTHORITY = launcher_authority;
				Log.v( "test" , "launcher_authority:" + launcher_authority );
			}
			else
			{
				Assets.setLauncherAuthor( "com.iLoong.launcher.pub.provider" );
				PubContentProvider.LAUNCHER_AUTHORITY = "com.iLoong.launcher.pub.provider";
			}
			String launcherApplySceneAction = initPref.getString( "launcherApplySceneAction" , null );
			initPref.edit().putString( "launcherApplySceneAction" , launcherApplySceneAction ).commit();
			if( launcherApplySceneAction != null )
			{
				com.coco.scene.scenebox.StaticClass.APPLY_SCENE = launcherApplySceneAction;
			}
			else
			{
				com.coco.scene.scenebox.StaticClass.APPLY_SCENE = "com.coco.launcher.apply_scene";
			}
			String launcherAddWidgetAction = initPref.getString( "launcherAddWidgetAction" , "com.cooee.unilauncher.add.widget" );
			initPref.edit().putString( "launcherAddWidgetAction" , launcherAddWidgetAction ).commit();
			if( launcherAddWidgetAction != null )
			{
				com.coco.widget.widgetbox.StaticClass.ADD_WIDGET_TO_LAUNCHER = launcherAddWidgetAction;
			}
			else
			{
				com.coco.widget.widgetbox.StaticClass.ADD_WIDGET_TO_LAUNCHER = "com.coco.launcher.add.widget";
			}
			com.coco.theme.themebox.util.FunctionConfig.setApp_id( initPref.getInt( "APP_ID" , -1 ) );
			initPref.edit().putInt( "APP_ID" , com.coco.theme.themebox.util.FunctionConfig.getApp_id() ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setStrAction( initPref.getString( "Action" , null ) );
			initPref.edit().putString( "Action" , com.coco.theme.themebox.util.FunctionConfig.getStrAction() ).commit();
			com.coco.theme.themebox.util.FunctionConfig.setStrActionDescription( initPref.getString( "ActionDescription" , null ) );
			initPref.edit().putString( "ActionDescription" , com.coco.theme.themebox.util.FunctionConfig.getStrActionDescription() ).commit();
		}
	}
	
	private final String PERSONALBOX_CONFIG_FILENAME = "personalbox_config.xml";
	private final String CUSTOM_PERSONALBOX_CONFIG_FILENAME = "/system/launcher/personalbox_config.xml";
	private final String CUSTOM_FIRST_PERSONALBOX_CONFIG_FILENAME = "/system/oem/launcher/personalbox_config.xml";
	
	private void readDefaultData()
	{
		InputSource xmlin = null;
		File f1 = new File( CUSTOM_FIRST_PERSONALBOX_CONFIG_FILENAME );
		if( !f1.exists() )
		{
			f1 = new File( CUSTOM_PERSONALBOX_CONFIG_FILENAME );
		}
		boolean builtIn = ( getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) != 0;
		try
		{
			if( builtIn && f1.exists() )
				xmlin = new InputSource( new FileInputStream( f1.getAbsolutePath() ) );
			else
				xmlin = new InputSource( mContext.getAssets().open( PERSONALBOX_CONFIG_FILENAME ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		if( xmlin != null )
		{
			try
			{
				SAXParserFactory factoey = SAXParserFactory.newInstance();
				SAXParser parser = factoey.newSAXParser();
				XMLReader xmlreader = parser.getXMLReader();
				DefaultLayoutHandler handler = new DefaultLayoutHandler();
				parser = factoey.newSAXParser();
				xmlreader = parser.getXMLReader();
				xmlreader.setContentHandler( handler );
				xmlreader.parse( xmlin );
				handler = null;
				xmlin = null;
			}
			catch( ParserConfigurationException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch( SAXException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	class DefaultLayoutHandler extends DefaultHandler
	{
		
		public static final String GENERAl_CONFIG = "general_config";
		
		public DefaultLayoutHandler()
		{
		}
		
		public void startElement(
				String namespaceURI ,
				String localName ,
				String qName ,
				Attributes atts ) throws SAXException
		{
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( mContext );
			Boolean isShowHotTab = preferences.getBoolean( "showOnline" , false );
			// @2014/12/18 ADD START
			/**
			 * 为了remove_enable_support_lockwallpaper_judge这个开关在壁纸预览的进程能够读取
			 * 为了让壁纸进程可以读到enable_preview_wallpaper_lowMemory,custom_preview_wallpaper_for_OOM这两个开关状态，将enable_preview_wallpaper_lowMemory这个参数添加到了这个sharePreference里面
			 */
			SharedPreferences wallpaperPreferences = mContext.getSharedPreferences( "isRemove_enable_support_lockwallpaper_judge" , MODE_WORLD_READABLE );
			// @2014/12/18 ADD END
			FunctionConfig.setIsShowHotTab( isShowHotTab );
			if( localName.equals( GENERAl_CONFIG ) )
			{
				String temp;
				// @gaominghui 2015/06/11 ADD START 为了用户在线下载的美化中心，然后有些桌面不需要显示小组件，所以从桌面获取参数判断是否显示某个tab页面
				//默认true显示，false不显示
				// @gaominghui 2015/06/11 ADD END )
				if( desktop_support_theme )
				{
					temp = atts.getValue( "themebox_theme_visible" );
					if( temp != null )
					{
						FunctionConfig.setThemeVisible( temp.equals( "true" ) );
					}
				}
				else
				{
					FunctionConfig.setThemeVisible( false );
				}
				if( desktop_support_wallpaper )
				{
					temp = atts.getValue( "enable_wallpapervisible" );
					if( temp != null )
					{
						FunctionConfig.setWallpaperVisible( temp.equals( "true" ) );
					}
				}
				else
				{
					FunctionConfig.setWallpaperVisible( false );
				}
				temp = atts.getValue( "enable_fontvisible" );
				if( temp != null )
				{
					FunctionConfig.setFontVisible( temp.equals( "true" ) );
				}
				if( desktop_support_lock )
				{
					temp = atts.getValue( "show_theme_lock" );
					if( temp != null )
					{
						FunctionConfig.setDisplayLock( temp.equals( "true" ) );
					}
				}
				else
				{
					FunctionConfig.setDisplayLock( false );
				}
				if( desktop_support_widget )
				{
					temp = atts.getValue( "themebox_widget_visible" );
					//Log.i("andy","desktop_support_widget = "+desktop_support_widget+"; temp = "+temp);
					if( temp != null )
					{
						FunctionConfig.setShowWidgetTab( temp.equals( "true" ) );
						//Log.i("andy","FunctionConfig.getShowWidgetTab = "+FunctionConfig.isShowWidgetTab());
					}
				}
				else
				{
					FunctionConfig.setShowWidgetTab( false );
				}
				if( FunctionConfig.isNetVersion() )
				{
					FunctionConfig.setShowSceneTab( false );
				}
				else
				{
					temp = atts.getValue( "themebox_scene_visible" );
					if( temp != null )
					{
						FunctionConfig.setShowSceneTab( temp.equals( "true" ) );
					}
				}
				temp = atts.getValue( "themebox_hot_scene_visible" );
				if( temp != null )
				{
					FunctionConfig.setShowHotScene( temp.equals( "true" ) || isShowHotTab );//加上服务器开关
				}
				temp = atts.getValue( "enable_hotlockVisible" );
				if( temp != null )
				{
					FunctionConfig.setHotLockVisible( temp.equals( "true" ) || isShowHotTab );
				}
				temp = atts.getValue( "themebox_hot_widget_visible" );
				if( temp != null )
				{
					FunctionConfig.setShowHotWidget( temp.equals( "true" ) || isShowHotTab );
				}
				temp = atts.getValue( "themebox_hot_wallpaper_visible" );
				if( temp != null )
				{
					FunctionConfig.setShowHotWallpaper( temp.equals( "true" ) || isShowHotTab );
				}
				temp = atts.getValue( "themebox_hot_font_visible" );
				if( temp != null )
				{
					FunctionConfig.setShowHotFont( temp.equals( "true" ) || isShowHotTab );
				}
				temp = atts.getValue( "enable_hotthemeVisible" );
				if( temp != null )
				{
					FunctionConfig.setThemeHotVisible( temp.equals( "true" ) || isShowHotTab );
				}
				temp = atts.getValue( "themebox_livewallpaper_show" );
				if( temp != null )
				{
					FunctionConfig.setLiveWallpaperShow( temp.equals( "true" ) || isShowHotTab );
				}
				temp = atts.getValue( "enable_lockset_visible" );
				if( temp != null )
				{
					FunctionConfig.setLockSetVisible( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_theme_introducation" );
				if( temp != null )
				{
					FunctionConfig.setIntroductionVisible( temp.equals( "true" ) );
				}
				temp = atts.getValue( "show_theme_share" );
				if( temp != null )
				{
					FunctionConfig.setShareVisible( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_themebox_loading" );
				if( temp != null )
				{
					FunctionConfig.setLoadingShow( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_themebox_showmore" );
				if( temp != null )
				{
					FunctionConfig.setThemeMoreShow( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_themebox_showstar" );
				if( temp != null )
				{
					FunctionConfig.setRecommendVisible( temp.equals( "true" ) );
				}
				temp = atts.getValue( "lockvisible_regradless_whether_install_lockbox" );
				if( temp != null )
				{
					FunctionConfig.setLockVisible( temp.equals( "true" ) );
				}
				//				temp = atts.getValue( "themebox_price_enable" );
				//				if( temp != null )
				//				{
				//					FunctionConfig.setPriceVisible( temp.equals( "true" ) );
				//				}
				temp = atts.getValue( "enable_background_configuration_tab" );
				if( temp != null )
				{
					FunctionConfig.setEnable_background_configuration_tab( temp.equals( "true" ) );
				}
				//				temp = atts.getValue( "enable_update_self" );
				//				if( temp != null )
				//				{
				//					FunctionConfig.setEnableUpdateself( temp.equals( "true" ) );
				//				}
				temp = atts.getValue( "tab_sequence" );
				if( temp != null )
				{
					if( temp.equals( "nothing" ) )
					{
						FunctionConfig.setTab_sequence( null );
					}
					else
					{
						FunctionConfig.setTab_sequence( temp );
					}
				}
				temp = atts.getValue( "tab_first_default_highlight" );
				if( temp != null )
				{
					if( temp.equals( "nothing" ) )
					{
						FunctionConfig.setTabdefaultHighlight( null );
					}
					else
					{
						FunctionConfig.setTabdefaultHighlight( temp );
					}
				}
				temp = atts.getValue( "static_wallpapers_to_icon" );
				if( temp != null )
				{
					FunctionConfig.setStatictoIcon( temp.equals( "true" ) );
				}
				temp = atts.getValue( "lockwallpaper_icon_show" );
				if( temp != null )
				{
					FunctionConfig.setLockwallpaperShow( temp.equals( "true" ) );
				}
				temp = atts.getValue( "lockwallpaper_custom_path" );
				if( temp != null )
				{
					if( temp.equals( "nothing" ) )
					{
						FunctionConfig.setCustomLockWallpaperPath( null );
					}
					else
					{
						FunctionConfig.setCustomLockWallpaperPath( temp );
					}
				}
				temp = atts.getValue( "enable_topwise_style" );
				if( temp != null )
				{
					FunctionConfig.setEnable_topwise_style( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_tophard_style" );
				if( temp != null )
				{
					FunctionConfig.setEnable_tophard_style( temp.equals( "true" ) );
					if( FunctionConfig.isEnable_tophard_style() )
					{
						FunctionConfig.setStatictoIcon( true );
						FunctionConfig.setLockwallpaperShow( true );
					}
				}
				temp = atts.getValue( "enable_manual_update" );
				if( temp != null )
				{
					FunctionConfig.setEnable_manual_update( temp.equals( "true" ) );
				}
				temp = atts.getValue( "wallpapers_from_other_apk" );
				if( temp != null )
				{
					if( temp.equals( "nothing" ) )
					{
						FunctionConfig.setWallpapers_from_other_apk( null );
					}
					else
					{
						FunctionConfig.setWallpapers_from_other_apk( temp );
					}
				}
				temp = atts.getValue( "langyitong_wallpaper_set" );
				if( temp != null )
				{
					FunctionConfig.setLangyitong_wallpaper_set( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_langyitong_theme_style" );
				if( temp != null )
				{
					FunctionConfig.setEnable_langyitong_theme_style( temp.equals( "true" ) );
				}
				temp = atts.getValue( "langyitong_theme_order_set" );
				if( temp != null )
				{
					FunctionConfig.setLangyitong_theme_order_set( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_eastaeon_style" );
				if( temp != null )
				{
					FunctionConfig.setEnable_eastaeon_style( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_check_lock_mode" );
				if( temp != null )
				{
					FunctionConfig.setEnable_CheckLockMode( temp.equals( "true" ) );
				}
				temp = atts.getValue( "is_show_systemlock_in_local" );
				if( temp != null )
				{
					FunctionConfig.setIsShowSystemLockInLocal( temp.equals( "true" ) );
				}
				temp = atts.getValue( "theme_apply_launcher_package_name" );
				if( temp != null )
				{
					if( temp.equals( "nothing" ) )
					{
						FunctionConfig.setThemeApplyLauncherPackageName( null );
					}
					else
					{
						FunctionConfig.setThemeApplyLauncherPackageName( temp );
					}
				}
				temp = atts.getValue( "theme_apply_launcher_class_name" );
				if( temp != null )
				{
					if( temp.equals( "nothing" ) )
					{
						FunctionConfig.setThemeApplyLauncherClassName( null );
					}
					else
					{
						FunctionConfig.setThemeApplyLauncherClassName( temp );
					}
				}
				// @gaominghui2016/01/08 ADD START brzh需求：从系统设置界面进入美化中心需要配置的内置桌面的数据库名字参数
				temp = atts.getValue( "launcher_pub_provider_authority" );
				if( temp != null )
				{
					if( temp.equals( "nothing" ) )
					{
						FunctionConfig.setLauncher_pub_provider_authority( null );
					}
					else
					{
						FunctionConfig.setLauncher_pub_provider_authority( temp );
					}
				}
				temp = atts.getValue( "enable_local_wallpaper_path" );
				if( temp != null )
				{
					if( !temp.equals( "nothing" ) && ( FunctionConfig.getCustomWallpaperPath() == null || FunctionConfig.getCustomWallpaperPath().equals( "" ) ) )
					{
						FunctionConfig.setCustomWallpaperPath( temp );
					}
				}
				// @gaominghui2016/01/08 ADD END
				temp = atts.getValue( "themebox_system_exit" );
				if( temp != null )
				{
					FunctionConfig.setIsExitSystemProgress( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_show_preview_wallpaper" );
				if( temp != null )
				{
					FunctionConfig.setIsEnableShowPreviewWallpaper( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_show_apply_lock_wallpaper" );
				if( temp != null )
				{
					FunctionConfig.setEnableShowApplyLockWallpaper( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_topwise_apply_lock_wallpaper" );
				if( temp != null )
				{
					FunctionConfig.setEnableShowVideoWallpaper( temp.equals( "true" ) );
				}
				temp = atts.getValue( "remove_enable_support_lockwallpaper_judge" );
				if( temp != null )
				{
					FunctionConfig.setRemove_enable_support_lockwallpaper_judge( temp.equals( "true" ) );
					wallpaperPreferences.edit().putBoolean( "key" , temp.equals( "true" ) ).commit();
				}
				temp = atts.getValue( "set_lockwallpaper_path" );
				if( temp != null )
				{
					if( temp.equals( "nothing" ) )
					{
						FunctionConfig.setLockWallpaperPath( null );
					}
					else
					{
						FunctionConfig.setLockWallpaperPath( temp );
					}
				}
				temp = atts.getValue( "enable_hedafeng_style" );
				if( temp != null )
				{
					FunctionConfig.setEnable_hedafeng_style( temp.equals( "true" ) );
				}
				temp = atts.getValue( "local_default_font_path" );
				if( temp != null )
				{
					if( temp.equals( "nothing" ) )
					{
						FunctionConfig.setLocalDefaultFontPath( null );
					}
					else
					{
						FunctionConfig.setLocalDefaultFontPath( temp );
					}
				}
				temp = atts.getValue( "enable_disclaimer_dialog" );
				if( temp != null )
				{
					FunctionConfig.setEnableDisclaimerDialog( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_start_livewallpaper_picker" );
				if( temp != null )
				{
					FunctionConfig.setEnableStartLiveWallpaperPicker( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_preview_wallpaper_by_adapter" );
				if( temp != null )
				{
					FunctionConfig.setEnablePreviewWallpaperByAdapter( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_setwallpaper_by_gallery_clip" );
				if( temp != null )
				{
					FunctionConfig.setEnableSetwallpaperByGalleryClip( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_delete_current_desk_wallpaper" );
				if( temp != null )
				{
					FunctionConfig.setEnableDeleteCurrentDeskWallpaper( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_startactivity_by_async" );
				if( temp != null )
				{
					FunctionConfig.setEnableStartActivityByAsync( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_show_current_wallpaper_flag" );
				if( temp != null )
				{
					FunctionConfig.setEnableShowCurrentWallpaperFlag( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_wallpaper_clip_by_systemwallpaper_scale" );
				if( temp != null )
				{
					FunctionConfig.setEnableWallpaperClipByScale( temp.equals( "true" ) );
				}
				// gaominghui@2017/05/09 ADD START 智科需求0004684：美化中心里通过图库设置壁纸,在裁剪时的裁剪比例是否根据系统图库的比例来裁剪（该开关在enable_wallpaper_clip_by_systemwallpaper_scale关掉后设置才有效）；
				temp = atts.getValue( "enable_wallpaper_clip_by_systemgallery" );
				if( temp != null )
				{
					FunctionConfig.setEnable_wallpaper_clip_by_systemgallery( temp.equals( "true" ) );
				}
				// gaominghui@2017/05/09 ADD END 智科需求0004684：美化中心里通过图库设置壁纸,在裁剪时的裁剪比例是否根据系统图库的比例来裁剪（该开关在enable_wallpaper_clip_by_systemwallpaper_scale关掉后设置才有效）；
				temp = atts.getValue( "enable_move_task_back_after_setdeskwallpaper" );
				if( temp != null )
				{
					FunctionConfig.setEnableMoveTaskBackAfterSetDeskWallpaper( temp.equals( "true" ) );
				}
				temp = atts.getValue( "umeng_statistics_key" );
				if( temp != null )
				{
					FunctionConfig.setUmengStatistics_key( temp.equals( "true" ) );
				}
				temp = atts.getValue( "is_show_local_gallery" );
				if( temp != null )
				{
					FunctionConfig.setIs_show_local_gallery( temp.equals( "true" ) );
				}
				temp = atts.getValue( "show_local_livewallpaper" );
				if( temp != null )
				{
					FunctionConfig.setShow_local_livewallpaper( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_apply_desktopwallpaper_lockwallpaper" );
				if( temp != null )
				{
					FunctionConfig.setEnable_apply_desktopwallpaper_lockwallpaper( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_local_thumb_preview_path" );
				if( temp != null )
				{
					FunctionConfig.setEnable_local_thumb_preview_path( temp.equals( "true" ) );
				}
				temp = atts.getValue( "inatall_silently_ThemeApk" );
				if( temp != null )
				{
					FunctionConfig.setInatall_silently_ThemeApk( temp.equals( "true" ) );
				}
				temp = atts.getValue( "brzh_setWaitBackgroundView" );
				if( temp != null )
				{
					FunctionConfig.setBrzh_setWaitBackgroundView( temp.equals( "true" ) );
				}
				temp = atts.getValue( "apply_theme_show_toast" );
				if( temp != null )
				{
					FunctionConfig.setApply_theme_show_toast( temp.equals( "true" ) );
				}
				temp = atts.getValue( "enable_preview_wallpaper_lowMemory" );
				if( temp != null )
				{
					//FunctionConfig.setEnable_preview_wallpaper_lowMemory( temp.equals( "true" ) );
					wallpaperPreferences.edit().putBoolean( "enable_preview_wallpaper_lowMemory" , temp.equals( "true" ) ).commit();
				}
				temp = atts.getValue( "custom_preview_wallpaper_for_OOM" );
				if( temp != null )
				{
					wallpaperPreferences.edit().putBoolean( "custom_preview_wallpaper_for_OOM" , temp.equals( "true" ) ).commit();
				}
				// gaominghui@2016/12/09 ADD START 晨想需求 0004580: lockwallpaper_icon_show，壁纸页面，是否显示锁屏的icon，这个开关打开之后，客户要求进入他们的模块
				temp = atts.getValue( "cx_lockwallpaper_show" );
				if( temp != null )
				{
					FunctionConfig.setCx_lockwallpaper_show( temp.equals( "true" ) );
				}
				// gaominghui@2016/12/09 ADD END 晨想需求 0004580: lockwallpaper_icon_show，壁纸页面，是否显示锁屏的icon，这个开关打开之后，客户要求进入他们的模块
			}
		}
		
		@Override
		public void endDocument() throws SAXException
		{
			// TODO Auto-generated method stub
			super.endDocument();
			if( FunctionConfig.isEnable_eastaeon_style() )
			{
				FunctionConfig.setThemeVisible( true );
				FunctionConfig.setWallpaperVisible( true );
				FunctionConfig.setFontVisible( false );
				FunctionConfig.setDisplayLock( true );
				FunctionConfig.setShowWidgetTab( false );
				FunctionConfig.setShowSceneTab( false );
				FunctionConfig.setEffectVisiable( true );
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(
			Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		if( FunctionConfig.isEnable_manual_update() )
		{
			getMenuInflater().inflate( R.menu.activity_main , menu );
		}
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(
			Menu menu )
	{
		return super.onPrepareOptionsMenu( menu );
	}
	
	@Override
	public boolean onOptionsItemSelected(
			MenuItem item )
	{
		// TODO Auto-generated method stub
		if( item.getItemId() == R.id.menu_settings )
		{
			if( Tools.isServiceRunning( mContext , "com.coco.theme.themebox.update.UpdateService" ) )
			{
				Toast.makeText( mContext , R.string.soft_updating , Toast.LENGTH_SHORT );
			}
			else
			{
				if( updateManager == null )
				{
					updateManager = new UpdateManager( mContext );
				}
				updateManager.updateApkInfo( true );
			}
		}
		return super.onOptionsItemSelected( item );
	}
	
	private Runnable loading = new Runnable() {
		
		@Override
		public void run()
		{
			Log.v( "loading" , "----end----" );
			load.setVisibility( View.GONE );
		}
	};
	
	@Override
	public void onStart()
	{
		super.onStart();
		//Log.v( "startTime" , "onStart start===" + String.valueOf( System.currentTimeMillis() ) );
		mDelayedStopHandler.removeCallbacksAndMessages( null );
		Log.v( "themebox" , "onStart isExit = " + isExit );
		isExit = false;
		if( com.coco.theme.themebox.util.FunctionConfig.isStatusBarTranslucent() )
		{
			Log.v( "status" , "onCreate STATUSBAR_OPAQUE" );
			String lostFocusAction = com.coco.theme.themebox.util.FunctionConfig.getLostFocusAction();
			if( lostFocusAction == null )
			{
				/* 通过反射机制来实现状态栏透明 */
				StatusBarUtils.setStatusBarBackgroundTransparent( mContext , false );
			}
			else
			{
				Intent intent = new Intent();
				intent.setAction( lostFocusAction );
				sendBroadcast( intent );
			}
		}
		//Log.v( "startTime" , "onStart end===" + String.valueOf( System.currentTimeMillis() ) );
	}
	
	@Override
	protected void onActivityResult(
			int requestCode ,
			int resultCode ,
			Intent data )
	{
		// TODO Auto-generated method stub
		if( requestCode == 2000 )
		{//
			if( resultCode == Activity.RESULT_OK )
			{
				if( FunctionConfig.isEnableSetwallpaperByGalleryClip() )
				{
					Log.i( "sss" , "onActivityResult!!! getPackageName = " + getPackageName() );
					Intent mIntent = new Intent();
					mIntent.setClassName( getPackageName() , "com.coco.wallpaper.wallpaperbox.GalleryClipActivity" );
					startActivity( mIntent );
				}
				isChange = true;
			}
		}
		else if( requestCode == 2001 )
		{
			WallpaperManager wallpaperManager = WallpaperManager.getInstance( this );
			WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
			if( wallpaperInfo != null )
			{
				isChange = true;
			}
		}
		if( isChange )
		{
			isChange = false;
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( this );
			pref.edit().putString( "currentWallpaper" , "other" ).commit();
			PubProviderHelper.addOrUpdateValue( PubContentProvider.LAUNCHER_AUTHORITY , "wallpaper" , "currentWallpaper" , "other" );
			Intent intent = new Intent();
			intent.setAction( "com.coco.wallpaper.update" );
			sendBroadcast( intent );
		}
	}
	
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		// @2015/03/11 ADD START 友盟 
		if( FunctionConfig.isUmengStatistics_key() )
		{
			MobclickAgent.onPageEnd( "MainActivity" );
			MobclickAgent.onPause( this );
		}
		// @2015/03/11 ADD END
		Log.v( "themebox" , "onPause" );
	}
	
	@Override
	public void onStop()
	{
		isChange = false;
		super.onStop();
		String lostFocusAction = com.coco.theme.themebox.util.FunctionConfig.getLostFocusAction();
		if( lostFocusAction == null )
		{
			/* 通过反射机制来实现状态栏透明 */
			StatusBarUtils.setStatusBarBackgroundTransparent( mContext , true );
		}
		if( tabWallpaper != null )
		{
			if( tabWallpaper.getLocalAdapter() != null )
			{
				tabWallpaper.onStop();
			}
		}
		Log.v( "themebox" , "onstop" );
	}
	
	// teapotXu_20130304: add start
	// set a flag that indicates whether the ThemeSelectIcon launched the
	// ThemeBox or Launcher app.
	@Override
	protected void onResume()
	{
		super.onResume();
		// @2015/03/11 ADD START 友盟统计
		if( FunctionConfig.isUmengStatistics_key() )
		{
			MobclickAgent.onPageStart( "MainActivity" );
			MobclickAgent.onResume( this );
		}
		if( updateManager != null )
		{
			updateManager.showDialog();
		}
		mDelayedStopHandler.removeCallbacksAndMessages( null );
		isExit = false;
		Log.v( "themebox" , "onResume isExit = " + isExit );
		if( b_theme_icon_start_launcher == true )
		{
			String theme_icon_pkgName = getIntent().getStringExtra( "FROM_PACKAGE" );
			// start the launcher directly
			ThemeService sv = new ThemeService( this );
			sv.applyTheme( sv.queryComponent( theme_icon_pkgName ) );
			sendBroadcast( new Intent( StaticClass.ACTION_DEFAULT_THEME_CHANGED ) );
			// @2015/03/11 ADD START 友盟统计美华中心退出
			if( FunctionConfig.isUmengStatistics_key() )
			{
				MainActivity.statisticsExitBeautyCenter( mContext );
			}
			// @2015/03/11 ADD END
			ActivityManager.KillActivity();
		}
		if( com.coco.theme.themebox.util.FunctionConfig.isDisplayLock() )
		{
			if( com.coco.theme.themebox.util.FunctionConfig.isRecommendVisible() )
			{
				if( layout_recommend != null )
				{
					RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)layout_recommend.getLayoutParams();
					if( isScrolling )
					{
						lp.topMargin = -MAX_HEIGHT;
					}
					isScrolling = false;
					starIv.setBackgroundResource( R.drawable.star );
					starIv.startAnimation( starScaleAnim );
					layout_recommend.setLayoutParams( lp );
					imageView.setClickable( false );
					netPrompt.setVisibility( View.GONE );
					mProgressBar.setVisibility( View.INVISIBLE );
				}
			}
		}
		StatisticsExpandNew.use( this , sn , appid , CooeeSdk.cooeeGetCooeeId( this ) , 1 , this.getApplication().getPackageName() , "" + launcherVersion );
		if( prefs != null && !prefs.contains( "first_run" ) )
			prefs.edit().putBoolean( "first_run" , false ).apply();
		//Log.v( "startTime" , "onResume end===" + String.valueOf( System.currentTimeMillis() ) );
	}
	
	@Override
	protected void onSaveInstanceState(
			Bundle outState )
	{
		// TODO Auto-generated method stub
		super.onSaveInstanceState( outState );
		Log.v( "test" , "onSaveInstanceState" );
	}
	
	@Override
	protected void onNewIntent(
			Intent intent )
	{
		// TODO Auto-generated method stub
		super.onNewIntent( intent );
		if( intent != null )
		{
			String tab = intent.getStringExtra( "currentTab" );
			if( tabHost != null && tab != null )
				tabHost.setCurrentTabByTag( tab );
			Log.v( "test" , "onNewIntent tab :" + tab );
		}
		if( !FunctionConfig.isExitSystemProgress() )
		{
			if( tabHost != null )
			{
				ContentFactory factorys[] = new ContentFactory[]{ tabTheme , tabLock , tabScene , tabWallpaper , tabWidget , tabFont , effect };
				for( ContentFactory factory : factorys )
				{
					if( factory != null )
					{
						factory.changeTab( 0 );
					}
				}
			}
		}
		if( tabWallpaper != null )
		{
			if( tabWallpaper.getLocalAdapter() != null )
			{
				tabWallpaper.getLocalAdapter().notifyDataSetChanged();
			}
		}
		if( tabTheme != null )
		{
			tabTheme.reloadView();
			if( tabTheme.getLocalAdapter() != null )
			{
				tabTheme.getLocalAdapter().reloadCurrent();
			}
		}
	}
	
	// teapotXu_20130304: add end
	@Override
	public boolean onKeyDown(
			int keyCode ,
			KeyEvent event )
	{
		switch( keyCode )
		{
			case KeyEvent.KEYCODE_MENU:
				if( com.coco.theme.themebox.util.FunctionConfig.isLockVisible() && FunctionConfig.isLockSetVisible() )
				{
					// 在lock界面按设置才有用
					if( tabHost != null )
					{
						String tab = tabHost.getCurrentTabTag();
						if( tab.equals( TAG_LOCK ) && !PlatformInfo.getInstance( mContext ).isSupportViewLock() )
						{
							Intent intentSetting = new Intent();
							intentSetting.setClassName( getPackageName() , StaticClass.LOCKBOX_SETTING_ACTIVITY );
							startActivity( intentSetting );
						}
					}
				}
				break;
		}
		return super.onKeyDown( keyCode , event );
	}
	
	public void BackKeyPressed()
	{
		//		Intent i = new Intent( Intent.ACTION_MAIN );
		//		i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		//		i.addCategory( Intent.CATEGORY_HOME );
		//		startActivity( i );
		this.moveTaskToBack( true );
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Log.d( "" , "bc onDestroy1" );
		boolean kill = false;
		if( !com.coco.theme.themebox.util.FunctionConfig.isNetVersion() )
		{
			if( !FunctionConfig.isDisplayLock() )
			{
				Message msg = mDelayedStopHandler.obtainMessage();
				mDelayedStopHandler.sendMessageDelayed( msg , KILL_DELAY );
				kill = true;
			}
			else
			{
				if( PlatformInfo.getInstance( this ).isSupportViewLock() )
				{
					Message msg = mDelayedStopHandler.obtainMessage();
					mDelayedStopHandler.sendMessageDelayed( msg , KILL_DELAY );
					kill = true;
				}
				else
				{
					Boolean lockScreen = PreferenceManager.getDefaultSharedPreferences( this ).getBoolean( com.coco.lock2.lockbox.StaticClass.ENABLE_LOCK , false );
					LockManager mgr = new LockManager( this );
					if( !lockScreen || !mgr.isenableCooeeLock() )//没有移植包的情况，锁屏开关关闭或者当前锁屏不存在，退出线程
					{
						Message msg = mDelayedStopHandler.obtainMessage();
						mDelayedStopHandler.sendMessageDelayed( msg , KILL_DELAY );
						kill = true;
					}
				}
			}
		}
		if( !kill )
		{
			ActivityManager.popupActivity( this );
		}
		// teapotXu_20130304: add start
		// set a flag that indicates whether the ThemeSelectIcon launched the
		// ThemeBox or Launcher app.
		if( b_theme_icon_start_launcher == false )
		{
			if( tabLock != null )
			{
				if( !kill )
					tabLock.onDestroy();
				else
					tabLock.unregisterReceiver();
			}
			if( FunctionConfig.isThemeVisible() && tabTheme != null )
			{
				if( !kill )
					tabTheme.onDestroy();
				else
					tabTheme.unregisterReceiver();
			}
		}
		// @gaominghui 2014/12/25 ADD START防止出现anr
		if( !kill )
		{
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					if( downModule != null )
						downModule.dispose();
				}
			} ).start();
		}
		// @2014/12/25 ADD END
		// teapotXu_20130304: add end
		Log.d( "" , "bc onDestroy2" );
		if( FunctionConfig.isWallpaperVisible() )
		{
			if( tabWallpaper != null )
			{
				if( !kill )
					tabWallpaper.onDestroy();
				else
					tabWallpaper.unregisterReceiver();
			}
			try
			{
				unregisterReceiver( recommendReceiver );
			}
			catch( IllegalArgumentException e )
			{
				e.printStackTrace();
			}
		}
		if( FunctionConfig.isFontVisible() )
		{
			if( tabFont != null )
			{
				if( !kill )
					tabFont.onDestroy();
				else
					tabFont.unregisterReceiver();
			}
		}
		if( FunctionConfig.isShowSceneTab() )
		{
			if( tabScene != null )
			{
				if( !kill )
					tabScene.onDestroy();
				else
					tabScene.unregisterReceiver();
			}
		}
		if( FunctionConfig.isShowWidgetTab() )
		{
			if( tabWidget != null )
			{
				if( !kill )
					tabWidget.onDestroy();
				else
					tabWidget.unregisterReceiver();
			}
		}
		if( FunctionConfig.isEffectVisiable() )
		{
			if( effect != null )
			{
				if( !kill )
					effect.onDestroy();
				else
					effect.unregisterReceiver();
			}
		}
		Log.d( "" , "bc onDestroy3" );
		if( com.coco.theme.themebox.util.FunctionConfig.isDisplayLock() )
		{
			if( com.coco.theme.themebox.util.FunctionConfig.isRecommendVisible() )
			{
				if( b_theme_icon_start_launcher == false )
				{
					//this exception module is modified by Jone 
					//for fix an IllegalArgumentException exception thrown by system 
					//while network is not available.
					try
					{
						unregisterReceiver( recommendReceiver );
					}
					catch( IllegalArgumentException e )
					{
						e.printStackTrace();
					}
				}
			}
		}
		if( receiver != null )
		{
			unregisterReceiver( receiver );
			receiver = null;
		}
		isExit = true;
		// gaominghui@2016/11/14 ADD START 解注册home键监听广播
		unregisterReceiver( mCloseSystemDialogsReceiver );
		// gaominghui@2016/11/14 ADD END 解注册home键监听广播
		//Jone add start, There is no necessary to restart Launcher if  Personal Center is embeded into Launcher,
		//or it will occurs an exception.
		Assets.onDestory();//防止没有退出进程时，静态变量不被释放，从不同launcher进入时，不同的sn、shellid等
		Log.d( "" , "bc onDestroy4" );
		//Jone end
	}
	
	public static int getProxyPort(
			Context context )
	{
		int res = Proxy.getPort( context );
		if( res == -1 )
			res = Proxy.getDefaultPort();
		return res;
	}
	
	public static boolean isCWWAPConnect(
			Context context )
	{
		boolean result = false;
		ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if( info != null && info.getType() == ConnectivityManager.TYPE_MOBILE )
		{
			if( ( Proxy.getDefaultHost() != null || Proxy.getHost( context ) != null ) && ( Proxy.getPort( context ) != -1 || Proxy.getDefaultPort() != -1 ) )
			{
				result = true;
			}
		}
		return result;
	}
	
	public static String getProxyHost(
			Context context )
	{
		String res = Proxy.getHost( context );
		if( res == null )
			res = Proxy.getDefaultHost();
		return res;
	}
	
	@Override
	public boolean onDown(
			MotionEvent e )
	{
		return false;
	}
	
	@Override
	public boolean onFling(
			MotionEvent e1 ,
			MotionEvent e2 ,
			float velocityX ,
			float velocityY )
	{
		return false;
	}
	
	@Override
	public void onLongPress(
			MotionEvent e )
	{
	}
	
	@Override
	public boolean onScroll(
			MotionEvent e1 ,
			MotionEvent e2 ,
			float distanceX ,
			float distanceY )
	{
		isScrolling = true;
		mScrollY += distanceY;// distanceX:向左为正，右为负
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)layout_recommend.getLayoutParams();
		lp.topMargin = lp.topMargin - (int)mScrollY;
		if( lp.topMargin <= -MAX_HEIGHT )
		{// 展开之后
			isScrolling = false;// 拖过头了不需要再执行AsynMove
			isUnfold = false;
			lp.topMargin = -MAX_HEIGHT;
			starIv.setBackgroundResource( R.drawable.star );
			starIv.startAnimation( starScaleAnim );
			onPanelOpened();// 调用OPEN回调函数
		}
		if( lp.topMargin >= 0 )
		{// 收缩之后
			isScrolling = false;
			isUnfold = true;
			lp.topMargin = 0;
			starIv.clearAnimation();
			starIv.setBackgroundResource( R.drawable.close );
			onPanelClosed();// 调用CLOSE回调函数
		}
		layout_recommend.setLayoutParams( lp );
		return false;
	}
	
	@Override
	public void onShowPress(
			MotionEvent e )
	{
	}
	
	@Override
	public boolean onSingleTapUp(
			MotionEvent e )
	{
		return false;
	}
	
	@Override
	public boolean onTouch(
			View v ,
			MotionEvent event )
	{
		int y = (int)event.getRawY();
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)layout_recommend.getLayoutParams();
		if( event.getAction() == MotionEvent.ACTION_DOWN )
		{
			isScrolling = true;
			pressY = y;
		}
		else if( event.getAction() == MotionEvent.ACTION_UP && ( isScrolling == true ) )
		{
			imageView.setClickable( true );
			if( lp.topMargin <= -MAX_HEIGHT )
			{
				imageView.setClickable( true );
				new AsynMove().execute( new Integer[]{ 30 } );
			}
			else if( lp.topMargin >= 0 )
			{
				imageView.setClickable( true );
				new AsynMove().execute( new Integer[]{ -30 } );
			}
			else if( y - pressY > 0 )
			{
				if( y - pressY >= MAX_HEIGHT / 5 )
				{
					new AsynMove().execute( new Integer[]{ 30 } );
				}
				else
				{
					new AsynMove().execute( new Integer[]{ -30 } );
				}
			}
			else
			{
				if( y - pressY <= -MAX_HEIGHT / 5 )
				{
					new AsynMove().execute( new Integer[]{ -30 } );
				}
				else
				{
					new AsynMove().execute( new Integer[]{ 30 } );
				}
			}
		}
		return mGestureDetector.onTouchEvent( event );
	}
	
	@Override
	public void onPanelOpened()
	{
	}
	
	@Override
	public void onPanelClosed()
	{
	}
	
	private void calculatorWidth()
	{
		ViewTreeObserver observer = layout_recommend.getViewTreeObserver();
		// 为了取得控件的宽
		observer.addOnPreDrawListener( new ViewTreeObserver.OnPreDrawListener() {
			
			public boolean onPreDraw()
			{
				if( hasMeasured == false )
				{
					MAX_HEIGHT = 0 - layout_recommend.getTop();
					if( MAX_HEIGHT > 0 )
					{
						hasMeasured = true;
					}
				}
				return true;
			}
		} );
	}
	
	private Runnable recommendRun = new Runnable() {
		
		public void run()
		{
			loadDatas();
			listView.setAdapter( appAdapter );
			listView.setOnItemClickListener( new OnItemClickListener() {
				
				@Override
				public void onItemClick(
						AdapterView<?> arg0 ,
						View arg1 ,
						int arg2 ,
						long arg3 )
				{
					if( appInfos.get( arg2 ).getAppItemType().equals( "app" ) )
					{
						if( isInstalled( appInfos.get( arg2 ).getAppPackage() ) )
						{
							PackageManager pm = getPackageManager();
							Intent intentActivity = pm.getLaunchIntentForPackage( appInfos.get( arg2 ).getAppPackage() );
							startActivity( intentActivity );
						}
						else
						{
							Intent intentActivity = new Intent();
							intentActivity.setClass( MainActivity.this , LoadRecomandActivity.class );
							intentActivity.putExtra( "name" , appInfos.get( arg2 ).getAppName() );
							String[] str = appInfos.get( arg2 ).getAppApkUrl().split( "," );
							String url = str[(int)( Math.random() * 10 ) % ( str.length )] + "?p01=" + appInfos.get( arg2 ).getAppPackage() + "&p06=1&";
							intentActivity.putExtra( "apkurl" , url );
							startActivity( intentActivity );
						}
					}
					else if( appInfos.get( arg2 ).getAppItemType().equals( "url" ) )
					{
						Uri uri = Uri.parse( appInfos.get( arg2 ).getAppPackage() );
						startActivity( new Intent( Intent.ACTION_VIEW , uri ) );
					}
				}
			} );
		}
	};
	
	public boolean isInstalled(
			String packname )
	{
		try
		{
			PackageInfo packageInfo = getPackageManager().getPackageInfo( packname , 0 );
			Log.d( "isInstall" , "packageInfo=" + packageInfo );
			return true;
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public void loadDatas()
	{
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor mCursor = db.query( Profile.TABLE_NAME , null , null , null , null , null , null );
		String language = Locale.getDefault().toString();
		if( appInfos != null )
		{
			appInfos.clear();
		}
		if( mCursor != null )
		{
			if( mCursor.getCount() > 0 )
			{
				int index = 0;
				while( mCursor.moveToNext() )
				{
					if( index > 0 )
					{
						AppInfos ai = new AppInfos();
						if( language.equals( "zh_CN" ) || language.equals( "zh_TW" ) )
						{
							ai.setAppName( mCursor.getString( mCursor.getColumnIndex( Profile.COLUMN_NAME_CH ) ) );
						}
						else
						{
							ai.setAppName( mCursor.getString( mCursor.getColumnIndex( Profile.COLUMN_NAME_EN ) ) );
						}
						ai.setAppItemType( mCursor.getString( mCursor.getColumnIndex( Profile.COLUMN_ITEMTYPE ) ) );
						ai.setAppPackage( mCursor.getString( mCursor.getColumnIndex( Profile.COLUMN_PACKAGE ) ) );
						ai.setAppApkUrl( mCursor.getString( mCursor.getColumnIndex( Profile.COLUMN_URL_APK ) ) );
						ai.setAppIconName( mCursor.getString( mCursor.getColumnIndex( Profile.COLUMN_ICON ) ) );
						Bitmap bitimap = null;
						bitimap = BitmapFactory.decodeFile( PathTool.getRecommendDir() + "/" + ai.getAppIconName() );
						if( bitimap == null )
						{
							bitimap = ( (BitmapDrawable)getResources().getDrawable( R.drawable.ic_launcher ) ).getBitmap();
							String iconUrl = appIconUrl[(int)( Math.random() * 10 ) % ( appIconUrl.length )] + ai.getAppIconName();
							try
							{
								new IconAsyncTask( this ).execute( iconUrl , PathTool.getRecommendDir() , ai.getAppIconName() );
							}
							catch( RejectedExecutionException e )
							{
								e.printStackTrace();
							}
						}
						ai.setAppIcon( bitimap );
						appInfos.add( ai );
					}
					else if( index == 0 )
					{
						appIconUrl = mCursor.getString( mCursor.getColumnIndex( Profile.COLUMN_URL_ICON ) ).split( "," );
					}
					index++;
				}
			}
		}
		if( mCursor != null )
		{
			if( mCursor.getCount() > 0 )
			{
				mHandler.removeCallbacks( promptRun );
				mProgressBar.setVisibility( View.GONE );
				netPrompt.setVisibility( View.GONE );
			}
			else
			{
				netPrompt.setVisibility( View.GONE );
				mProgressBar.setVisibility( View.VISIBLE );
				mHandler.postDelayed( promptRun , 1000 * 30 );
			}
		}
		else
		{
			netPrompt.setVisibility( View.GONE );
			mProgressBar.setVisibility( View.VISIBLE );
			mHandler.postDelayed( promptRun , 1000 * 30 );
		}
		if( mCursor != null )
		{
			mCursor.close();
		}
		db.close();
	}
	
	private Runnable promptRun = new Runnable() {
		
		@Override
		public void run()
		{
			netPrompt.setVisibility( View.VISIBLE );
			mProgressBar.setVisibility( View.GONE );
		}
	};
	
	public class AppAdapter extends BaseAdapter
	{
		
		private Context mAdapterContext;
		private ArrayList<AppInfos> appInfo;
		
		public AppAdapter(
				Context c ,
				ArrayList<AppInfos> appInfo )
		{
			this.mAdapterContext = c;
			this.appInfo = appInfo;
		}
		
		public int getCount()
		{
			return appInfos.size();
		}
		
		public Object getItem(
				int position )
		{
			return appInfos.get( position );
		}
		
		public long getItemId(
				int position )
		{
			return position;
		}
		
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			if( appInfo == null )
			{
				return null;
			}
			ImageView imageView = null;
			TextView textView1 = null;
			convertView = View.inflate( mAdapterContext , R.layout.push_app_item , null );
			imageView = (ImageView)convertView.findViewById( R.id.app_icon );
			textView1 = (TextView)convertView.findViewById( R.id.app_name );
			AppInfos info = appInfo.get( position );
			imageView.setImageBitmap( info.app_icon );
			textView1.setText( info.app_name );
			return convertView;
		}
	}
	
	public class AppInfos
	{
		
		private Bitmap app_icon;
		private String app_icon_name;
		private String app_name;
		private String app_describe;
		private String app_item_type;
		private String app_package;
		private String app_apk_url;
		private String app_icon_url;
		
		public Bitmap getImages()
		{
			return app_icon;
		}
		
		public void setImages(
				Bitmap bitmap )
		{
			this.app_icon = bitmap;
		}
		
		public String getAppName()
		{
			return app_name;
		}
		
		public void setAppName(
				String appName )
		{
			app_name = appName;
		}
		
		public String getAppDescribe()
		{
			return app_describe;
		}
		
		public void setAppDescribe(
				String appDescribe )
		{
			app_describe = appDescribe;
		}
		
		public String getAppItemType()
		{
			return app_item_type;
		}
		
		public void setAppItemType(
				String appItemType )
		{
			app_item_type = appItemType;
		}
		
		public String getAppPackage()
		{
			return app_package;
		}
		
		public void setAppPackage(
				String appPackage )
		{
			app_package = appPackage;
		}
		
		public String getAppApkUrl()
		{
			return app_apk_url;
		}
		
		public void setAppApkUrl(
				String appApkUrl )
		{
			app_apk_url = appApkUrl;
		}
		
		public String getAppIconUrl()
		{
			return app_icon_url;
		}
		
		public void setAppIconUrl(
				String appIconUrl )
		{
			app_icon_url = appIconUrl;
		}
		
		public String getAppIconName()
		{
			return app_icon_name;
		}
		
		public void setAppIconName(
				String appIconName )
		{
			app_icon_name = appIconName;
		}
		
		public Bitmap getAppIcon()
		{
			return app_icon;
		}
		
		public void setAppIcon(
				Bitmap appIcon )
		{
			app_icon = appIcon;
		}
	}
	
	private BroadcastReceiver recommendReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			if( intent.getAction().equals( StaticClass.ACTION_THEME_UPDATE_RECOMMEND ) )
			{
				if( com.coco.theme.themebox.util.FunctionConfig.isLockVisible() )
				{
					loadDatas();
					appAdapter.notifyDataSetChanged();
				}
			}
			else if( intent.getAction().equals( ConnectivityManager.CONNECTIVITY_ACTION ) )
			{
				if( com.coco.theme.themebox.util.FunctionConfig.isLockVisible() )
				{
					if( StaticClass.isHaveInternet( context ) )
					{
						SQLiteDatabase db = mDbHelper.getReadableDatabase();
						Cursor mCursor = db.query( Profile.TABLE_NAME , null , null , null , null , null , null );
						if( mCursor == null || mCursor.getCount() <= 0 )
						{
							String[] str = { "http://yu01.coomoe.com/uimenu/getlist.ashx" , "http://yu02.coomoe.com/uimenu/getlist.ashx" };
							String oldVersion = "";
							String url = "";
							SharedPreferences sharedPrefer1 = PreferenceManager.getDefaultSharedPreferences( context );
							oldVersion = sharedPrefer1.getString( "recommendVersion" , "" );
							if( oldVersion != null )
							{
								if( oldVersion.equals( "" ) )
								{
									url = str[(int)( Math.random() * 10 ) % ( str.length )] + "?p07=com.coco.lock2.lockbox" + "&p02=" + getVersionCode( context ) + "&" + Assets
											.getPhoneParams( context );
								}
								else
								{
									url = str[(int)( Math.random() * 10 ) % ( str.length )] + "?p07=com.coco.lock2.lockbox" + "&p02=" + getVersionCode( context ) + "&p08=" + oldVersion + "&" + Assets
											.getPhoneParams( context );
								}
							}
							else
							{
								url = str[(int)( Math.random() * 10 ) % ( str.length )] + "?p07=com.coco.lock2.lockbox" + "&p02=" + getVersionCode( context ) + "&" + Assets.getPhoneParams( context );
							}
							new MyAsyncTask( context ).execute( url );
							if( netPrompt != null )
							{
								netPrompt.setVisibility( View.GONE );
								mProgressBar.setVisibility( View.VISIBLE );
							}
						}
						db.close();
					}
				}
			}
			else if( intent.getAction().equals( Intent.ACTION_WALLPAPER_CHANGED ) )
			{
				isChange = true;
			}
		}
	};
	
	/**
	 * 获取软件版本�?
	 */
	private int getVersionCode(
			Context context )
	{
		int versionCode = 0;
		try
		{
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = context.getPackageManager().getPackageInfo( context.getPackageName() , 0 ).versionCode;
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		return versionCode;
	}
	
	@Override
	public boolean onKeyUp(
			int keyCode ,
			KeyEvent event )
	{
		Log.v( "test" , "onKeyUp " );
		switch( keyCode )
		{
			case KeyEvent.KEYCODE_BACK:
				return exitBeautyCenter();
			default:
				break;
		}
		return super.onKeyUp( keyCode , event );
	}
	
	/**
	 *
	 * @return
	 * @author gaominghui 2016年11月11日
	 */
	private boolean exitBeautyCenter()
	{
		if( FunctionConfig.isUmengStatistics_key() )
		{
			MobclickAgent.onEvent( this , "click_return"/*,map_value */);
		}
		Log.v( "test" , "onKeyUp isUnfold = " + isUnfold );
		if( isUnfold )
		{
			new AsynMove().execute( new Integer[]{ -30 } );
			isUnfold = false;
			return true;
		}
		else
		{
			//<c_0000707> liuhailin@2014-08-11 modify begin
			//if( FunctionConfig.isExitSystemProgress() )
			if( FunctionConfig.isExitSystemProgress() && !DownloadApkContentService.isDownloadingAPK && !MeGeneralMethod.IsDownloadTaskRunning( getApplicationContext() ) )
			//<c_0000707> liuhailin@2014-08-11 modify end
			{
				Log.v( "test" , "onKeyUp finish!!" );
				finish();
			}
			else
			{
				Log.v( "test" , "onKeyUp BackKeyPressed!!" );
				BackKeyPressed();
			}
			return true;
		}
	}
	
	class AsynMove extends AsyncTask<Integer , Integer , Void>
	{
		
		@Override
		protected Void doInBackground(
				Integer ... params )
		{
			int times;
			if( MAX_HEIGHT % Math.abs( params[0] ) == 0 )// 整除
				times = MAX_HEIGHT / Math.abs( params[0] );
			else
				times = MAX_HEIGHT / Math.abs( params[0] ) + 1;// 有余
			for( int i = 0 ; i < times ; i++ )
			{
				publishProgress( params );
				try
				{
					Thread.sleep( Math.abs( params[0] ) );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(
				Integer ... params )
		{
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)layout_recommend.getLayoutParams();
			if( params[0] < 0 )
				lp.topMargin = Math.max( lp.topMargin + params[0] , -MAX_HEIGHT );
			else
				lp.topMargin = Math.min( lp.topMargin + params[0] , 0 );
			if( lp.topMargin >= 0 )
			{// 展开之后
				imageView.setClickable( true );
				starIv.clearAnimation();
				starIv.setBackgroundResource( R.drawable.close );
				isScrolling = false;
				isUnfold = true;
				onPanelOpened();// 调用OPEN回调函数
			}
			else if( lp.topMargin <= ( -MAX_HEIGHT ) )
			{// 收缩之后
				imageView.setClickable( false );
				starIv.setBackgroundResource( R.drawable.star );
				starIv.startAnimation( starScaleAnim );
				isScrolling = false;
				isUnfold = false;
				onPanelClosed();// 调用CLOSE回调函数
			}
			layout_recommend.setLayoutParams( lp );
		}
	}
	
	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		Log.i( "startTime" , "MainActivity finish !!!" );
		if( FunctionConfig.isUmengStatistics_key() )
		{
			statisticsExitBeautyCenter( mContext );
		}
		super.finish();
	}
	
	public class MessageReceiver extends BroadcastReceiver
	{
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if( action != null )
			{
				if( action.equals( "com.cooee.launcher.action.start" ) || action.equals( "com.cooee.scene.action.SHOW_IDLE" ) )
				{
					if( mDelayedStopHandler != null )
					{
						if( PlatformInfo.getInstance( MainActivity.this ).isSupportViewLock() )
						{
							Log.v( "themebox" , "onReceive KILL_DELAY " );
							Message msg = mDelayedStopHandler.obtainMessage();
							mDelayedStopHandler.sendMessageDelayed( msg , KILL_DELAY );
						}
					}
				}
				else if( action.equals( "com.coco.action.TAB_CHANGED" ) )
				{
					new Thread( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							ConfigurationTabService service = new ConfigurationTabService( MainActivity.this );
							listTab = service.queryTabList();
							resetTabs();
							runOnUiThread( new Runnable() {
								
								public void run()
								{
									if( progress != null && progress.getVisibility() == View.VISIBLE )
									{
										progress.setVisibility( View.GONE );
									}
									if( tabHost.getTabWidget() == null )
									{
										initContentView( bundle );
									}
								}
							} );
						}
					} ).start();
				}
			}
		}
	}
	
	private void resetTabs()
	{
		if( FunctionConfig.isEnable_background_configuration_tab() )
		{
			FunctionConfig.setThemeVisible( false );
			FunctionConfig.setDisplayLock( false );
			FunctionConfig.setWallpaperVisible( false );
			FunctionConfig.setFontVisible( false );
			FunctionConfig.setShowWidgetTab( false );
			FunctionConfig.setShowSceneTab( false );
			FunctionConfig.setLiveWallpaperShow( false );
			for( String item : listTab )
			{
				if( item.equals( DownloadList.Theme_Type ) )
				{
					FunctionConfig.setThemeVisible( true );
					continue;
				}
				if( item.equals( DownloadList.Lock_Type ) )
				{
					FunctionConfig.setDisplayLock( true );
					continue;
				}
				if( item.equals( DownloadList.Wallpaper_Type ) )
				{
					FunctionConfig.setWallpaperVisible( true );
					continue;
				}
				if( item.equals( DownloadList.Font_Type ) )
				{
					FunctionConfig.setFontVisible( true );
					continue;
				}
				if( item.equals( DownloadList.Widget_Type ) )
				{
					FunctionConfig.setShowWidgetTab( true );
					continue;
				}
				if( item.equals( DownloadList.Scene_Type ) )
				{
					FunctionConfig.setShowSceneTab( true );
					continue;
				}
				if( item.equals( DownloadList.LiveWallpaper_Type ) )
				{
					FunctionConfig.setLiveWallpaperShow( true );
					continue;
				}
			}
		}
	}
	
	@Override
	public void onClick(
			View v )
	{
		// TODO Auto-generated method stub
		Intent it = new Intent();
		it.setClass( this , MainTabActivity.class );
		switch( v.getId() )
		{
			case R.id.lock_grid:
				it.putExtra( "currentTab" , "tagLock" );
				break;
			case R.id.theme_grid:
				it.putExtra( "currentTab" , "tagTheme" );
				break;
			case R.id.wallpaper_grid:
				it.putExtra( "currentTab" , "tagWallpaper" );
				break;
			case R.id.effect_grid:
				it.putExtra( "currentTab" , "tagEffect" );
				break;
		}
		startActivity( it );
	}
	
	/**
	 *友盟统计退出美化中心
	 *
	 * @author gaominghui 2015年3月11日
	 */
	public static void statisticsExitBeautyCenter(
			Context context )
	{
		Map<String , String> map_value = new HashMap<String , String>();
		String UserTime = statisticsUseDuration();
		Log.i( "startTime" , "exit beautyCenter!!! UserTime = " + UserTime );
		map_value.put( "duration" , UserTime );
		//android.util.Log.i( "statistics" , "duration = " + duration );
		MobclickAgent.onEvent( context , "ExitBeautyCenter" , map_value );
		Log.i( "startTime" , "exit beautyCenter!!!" );
	}
	
	/**
	 *友盟统计用于记录美化中心使用时长
	 * @return 使用美化中心时长
	 * @author gaominghui 2016年1月28日
	 */
	private static String statisticsUseDuration()
	{
		int duration = (int)( System.currentTimeMillis() - enterTime );
		long seconds = duration / 1000;
		long minutes = 0;
		long hours = 0;
		if( seconds >= 60 )
		{
			minutes = seconds / 60l;
			seconds = seconds % 60;
			if( minutes >= 60 )
			{
				hours = minutes / 60l;
				minutes = minutes % 60;
			}
		}
		String UserTime = String.valueOf( hours ) + ":" + String.valueOf( minutes ) + ":" + String.valueOf( seconds );
		return UserTime;
	}
	
	/**
	 *
	 * @see cool.sdk.BeautyCenterConfig.OperateUmeng.IOperateUmengCallbacks#notifyUmengSwitch(boolean)
	 * @auther gaominghui  2016年1月27日
	 */
	@Override
	public void notifyUmengSwitch(
			final boolean isShow )
	{
		// TODO Auto-generated method stub
		Runnable mRunnable = new Runnable() {
			
			@Override
			public void run()
			{
				Log.v( "OperateUmeng" , "launcher - notifyUmengSwitch：" + isShow );
				if( isShow )
				{//use Umeng
					if( !FunctionConfig.isUmengStatistics_key() )
					{
						FunctionConfig.setUmengStatistics_key( true );
						SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
						Editor mEditor = mSharedPreferences.edit();
						mEditor.putBoolean( OperateUmeng.OPERATE_UMENG_NEED_ENABLE_UMENG_SWITCH_KEY , true );//1代表打开友盟统计，0关闭友盟统计
						mEditor.commit();
					}
				}
				else
				{//unuse Umeng
					if( FunctionConfig.isUmengStatistics_key() )
					{
						FunctionConfig.setUmengStatistics_key( false );
						SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
						Editor mEditor = mSharedPreferences.edit();
						mEditor.putBoolean( OperateUmeng.OPERATE_UMENG_NEED_ENABLE_UMENG_SWITCH_KEY , false );
						mEditor.commit();
					}
				}
			}
		};
		runOnUiThread( mRunnable );
	}
	
	// gaominghui@2016/11/11 ADD START home键监听广播 点击home键退出进程，解决i_0014673
	private class CloseSystemDialogsReceiver extends BroadcastReceiver
	{
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			exitBeautyCenter();
		}
	}
	// gaominghui@2016/11/11 ADD END home键监听广播  点击home键退出进程，解决i_0014673
}
