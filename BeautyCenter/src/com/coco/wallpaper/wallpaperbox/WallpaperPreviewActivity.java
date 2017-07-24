package com.coco.wallpaper.wallpaperbox;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coco.download.DownloadList;
import com.coco.lock2.lockbox.LockInformation;
import com.coco.lock2.lockbox.util.ContentConfig;
import com.coco.lock2.lockbox.util.LockManager;
import com.coco.theme.themebox.ActivityManager;
import com.coco.theme.themebox.DownloadApkContentService;
import com.coco.theme.themebox.database.model.DownloadStatus;
import com.coco.theme.themebox.database.model.DownloadThemeItem;
import com.coco.theme.themebox.database.service.DownloadThemeService;
import com.coco.theme.themebox.service.ThemesDB;
import com.coco.theme.themebox.util.DownModule;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Log;
import com.coco.theme.themebox.util.Tools;
import com.cooee.shell.sdk.CooeeSdk;
import com.iLoong.base.themebox.R;
import com.iLoong.launcher.MList.MeGeneralMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;


public class WallpaperPreviewActivity extends Activity implements IWallpaperDialog , AdapterView.OnItemSelectedListener , OnClickListener
{
	
	private final static String LOG_TAG = "WallpaperPreviewActivity";
	private String wallpaperPath = "launcher/wallpapers";
	private String customWallpaperPath;
	private String wallpapers_from_other_apk = null;
	private boolean useCustomWallpaper = false;
	private Gallery mGallery;
	private ImageView mImageView;
	private ViewPager mViewPager;
	private ArrayList<String> mThumbs = new ArrayList<String>( 24 );
	private List<WallpaperInformation> localList = new ArrayList<WallpaperInformation>();
	private WallpaperLoader mLoader;
	private PreviewImageTask mPreviewLoader;
	private Context mThemeContext;
	private WallpaperInfo infos;
	ImageAdapter mAdapter;
	ImageHotAdapter mHotAdapter;
	LocalViewPagerAdapter mLocalViewPagerAdapter;
	HotViewPagerAdapter mHotViewPagerAdapter;
	LinearLayout setwallpaper;
	int buttonsize = 0;
	private String type = "local";
	private DownModule downModule;
	private ProgressBar progressbar;
	private RelativeLayout relativeNormal;
	private RelativeLayout relativeDownload;
	private BroadcastReceiver packageReceiver;
	private ImageButton delete;
	int clickPosition = 0;
	private boolean isPriceVisible = false;
	private String customPath = null;
	private List<Drawable> localBmp = new ArrayList<Drawable>();
	private boolean isDesktopWall = true;
	private List<WallpaperInformation> appList = new ArrayList<WallpaperInformation>();
	public static String currentWallpaperPath = null;
	private String currentOtherApkWallpaperResName = "";
	private ComponentName currentLock;
	private Boolean isLockSupportChangeWallpaper = false;
	private Boolean isEnableLauncherTakeScreenShot = false;
	private Boolean isShowPreviewBtnBoolean = false;
	private Boolean isShowApplyLockBtn = false;
	private String setLockWallpaperPath = "";
	private String currentLauncherPackageName = "";
	private String currentLauncherProvider = "";
	private Drawable defaultDrawable = null;
	private Drawable currentDrawable = null;
	private Map<String , Drawable> wallpaperMap = new HashMap<String , Drawable>();
	private Boolean isLoadComplete = false;
	private Boolean isShowPreviewWallpaperByAdapter = true;
	private boolean isPause = false;
	private String currentWallpaper = "";
	private boolean isEnableDeleteCurrentWallpaper = false;
	private Boolean movetaskback_after_setdeskwallpaper = false;
	// @2015/09/09 ADD START
	private String appName;
	// @2015/09/09 ADD END
	// @2014/12/18 ADD START
	/**
	 * 从主题盒子进程读取配置文件中的这个开关的状态（我们是在主题盒子中创建了一个sharedpreferences）remove_enable_support_lockwallpaper_judge:(兴软)去掉关于第三方锁屏或者系统锁屏能否支持锁屏壁纸的判断 true是去掉判断，false是代码中回去判断能否这是锁屏壁纸
	 *以及enable_preview_wallpaper_lowMemory这个开关状态，对于低内存低分辨率的手机在加载在线壁纸的大的预览图是请打开该开关，避免出现OOM
	 */
	private SharedPreferences preferences;
	private Boolean preResult = false;
	// @2014/12/18 ADD END
	// @gaominghui2015/09/01 ADD START 添加brzh本地壁纸预览界面显示的开关（0003415: 铂睿智恒 使用uni3.0 桌面，在美化中心，壁纸 预览界面，增加同时设置锁屏壁纸和桌面壁纸的功能）
	private static boolean brzh_set_desktop_lock_wallpaper = false;
	// @gaominghui2015/09/01 ADD END
	// @2015/11/20 ADD START
	private RelativeLayout wallpaper_preview;
	// @2015/11/20 ADD END
	// @2016/02/01 ADD START 友盟统计的开关
	private boolean isUmengStatistics_key = false;
	// @2016/02/01 ADD END
	// gaominghui@2017/01/03 ADD START
	private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsReceiver();
	
	// gaominghui@2017/01/03 ADD END
	@Override
	public void onCreate(
			Bundle icicle )
	{
		super.onCreate( icicle );
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		ActivityManager.pushActivity( this );
		// gaominghui@2017/01/03 ADD START注册监听home键广播
		IntentFilter filter = new IntentFilter( Intent.ACTION_CLOSE_SYSTEM_DIALOGS );
		registerReceiver( mCloseSystemDialogsReceiver , filter );
		// gaominghui@2017/01/03 ADD END 注册监听home键广播
		infos = new WallpaperInfo( this );
		mThemeContext = this;
		defaultDrawable = mThemeContext.getResources().getDrawable( R.drawable.default_img_large );
		//imgDefaultThumb = ( (BitmapDrawable)mThemeContext.getResources().getDrawable( R.drawable.default_img_large ) ).getBitmap();
		Intent it = getIntent();
		if( it != null )
		{
			infos.setDisableSetWallpaperDimensions( it.getBooleanExtra( "disableSetWallpaperDimensions" , false ) );
			customPath = it.getStringExtra( "customWallpaperPath" );
			isPriceVisible = it.getBooleanExtra( "isPriceVisible" , false );
			FunctionConfig.setLangyitong_wallpaper_set( it.getBooleanExtra( "langyitong_wallpaper_set" , false ) );
			String launcher = it.getStringExtra( "launchername" );
			if( launcher != null )
			{
				ThemesDB.LAUNCHER_PACKAGENAME = launcher;
			}
			isDesktopWall = it.getBooleanExtra( "isDesktopWall" , true );
			// @gaominghui2015/09/01 ADD START
			brzh_set_desktop_lock_wallpaper = it.getBooleanExtra( "isEnable_apply_desktopwallpaper_lockwallpaper" , false );
			// @gaominghui2015/09/01 ADD END
			// @2016/02/01 ADD START 获取友盟统计开关
			isUmengStatistics_key = it.getBooleanExtra( "isUmengStatistics_key" , false );
			// @2016/02/01 ADD END
		}
		if( isPriceVisible )
		{
			CooeeSdk.initCooeeSdk( this );
		}
		setContentView( R.layout.preview_wallpaper );
		progressbar = (ProgressBar)findViewById( R.id.progressBar );
		mGallery = (Gallery)findViewById( R.id.thumbs );
		mViewPager = (ViewPager)findViewById( R.id.previewPager );
		delete = (ImageButton)findViewById( R.id.btnDel );
		delete.setVisibility( View.GONE );
		delete.setOnClickListener( this );
		relativeNormal = (RelativeLayout)findViewById( R.id.layoutNormal );
		relativeDownload = (RelativeLayout)findViewById( R.id.layoutDownload );
		relativeDownload.setOnClickListener( this );
		mGallery.setOnItemSelectedListener( this );
		// @2014/12/18 ADD START
		preferences = mThemeContext.getSharedPreferences( "isRemove_enable_support_lockwallpaper_judge" , MODE_PRIVATE );
		preResult = preferences.getBoolean( "key" , false );
		// @2014/12/18 ADD END
		mViewPager.setOnPageChangeListener( new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(
					int arg0 )
			{
				// TODO Auto-generated method stub
				mGallery.setSelection( arg0 );
			}
			
			@Override
			public void onPageScrolled(
					int arg0 ,
					float arg1 ,
					int arg2 )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPageScrollStateChanged(
					int arg0 )
			{
				// TODO Auto-generated method stub
			}
		} );
		if( it != null && it.getStringExtra( "type" ).equals( "hot" ) )
		{
			downModule = DownModule.getInstance( this );
			type = "hot";
			isShowPreviewWallpaperByAdapter = getIntent().getBooleanExtra( "showpreviewwallpaperbyadapter" , true );
			isShowPreviewBtnBoolean = getIntent().getBooleanExtra( "showpreviewbtn" , false );
			isShowApplyLockBtn = getIntent().getBooleanExtra( "showapplylockbtn" , false );
			setLockWallpaperPath = getIntent().getStringExtra( "lockwallpaperpath" );
			currentLauncherPackageName = getIntent().getStringExtra( "currentLauncherPackageName" );
			currentLauncherProvider = getIntent().getStringExtra( "currentLauncherProvider" );
			isEnableDeleteCurrentWallpaper = getIntent().getBooleanExtra( "enable_delete_current_wallpaper" , false );
			movetaskback_after_setdeskwallpaper = getIntent().getBooleanExtra( "ismovetaskback" , false );
			clickPosition = getIntent().getIntExtra( "position" , 0 );
			mHotAdapter = new ImageHotAdapter( this , downModule );
			// mHotAdapter.queryPackage();
			mGallery.setAdapter( mHotAdapter );
			mHotViewPagerAdapter = new HotViewPagerAdapter( this );
			if( isShowPreviewWallpaperByAdapter )
			{
				mViewPager.setAdapter( mHotViewPagerAdapter );
			}
			else
			{
				mViewPager.setVisibility( View.GONE );
			}
			progressbar.setVisibility( View.VISIBLE );
		}
		else
		{
			type = "local";
			buttonsize = getIntent().getIntExtra( "buttonsize" , 0 );
			isShowPreviewWallpaperByAdapter = getIntent().getBooleanExtra( "showpreviewwallpaperbyadapter" , true );
			isShowPreviewBtnBoolean = getIntent().getBooleanExtra( "showpreviewbtn" , false );
			isShowApplyLockBtn = getIntent().getBooleanExtra( "showapplylockbtn" , false );
			setLockWallpaperPath = getIntent().getStringExtra( "lockwallpaperpath" );
			currentLauncherPackageName = getIntent().getStringExtra( "currentLauncherPackageName" );
			currentLauncherProvider = getIntent().getStringExtra( "currentLauncherProvider" );
			isEnableDeleteCurrentWallpaper = getIntent().getBooleanExtra( "enable_delete_current_wallpaper" , false );
			clickPosition = getIntent().getIntExtra( "position" , buttonsize ) - buttonsize;
			wallpapers_from_other_apk = getIntent().getStringExtra( "fromotherapk" );
			movetaskback_after_setdeskwallpaper = getIntent().getBooleanExtra( "ismovetaskback" , false );
			mAdapter = new ImageAdapter( this );
			mGallery.setAdapter( mAdapter );
			if( isShowPreviewWallpaperByAdapter )
			{
				mLocalViewPagerAdapter = new LocalViewPagerAdapter( this );
				mViewPager.setAdapter( mLocalViewPagerAdapter );
			}
			else
			{
				mViewPager.setVisibility( View.GONE );
			}
			progressbar.setVisibility( View.INVISIBLE );
		}
		// @gaominghui2016/02/01 ADD START 友盟统计
		if( isUmengStatistics_key )
		{
			// @gaominghui2015/03/11 ADD START 统计用户使用状况
			//MobclickAgent.setDebugMode( true );//友盟统计实时日志的开关，可以在友盟上实时测试统计结果
			MobclickAgent.openActivityDurationTrack( false );
			//MobclickAgent.setSessionContinueMillis( 10 );
			// @2015/03/11 ADD END
		}
		// @gaominghui2016/02/01 ADD END 友盟统计
		mGallery.setCallbackDuringFling( false );
		findViewById( R.id.btnReturn ).setOnClickListener( this );
		setwallpaper = (LinearLayout)findViewById( R.id.setwallpaper );
		findViewById( R.id.setdesktopwallpaper ).setOnClickListener( this );
		findViewById( R.id.btnpreview ).setOnClickListener( this );
		findViewById( R.id.setlockwallpaper ).setOnClickListener( this );
		findViewById( R.id.btnBuy ).setOnClickListener( this );
		findViewById( R.id.btnDownload ).setOnClickListener( this );
		if( brzh_set_desktop_lock_wallpaper )
		{
			Button btn_brzh_setWallpaper = (Button)findViewById( R.id.btnApplyWallpaper );
			btn_brzh_setWallpaper.setOnClickListener( this );
			btn_brzh_setWallpaper.setVisibility( View.VISIBLE );
		}
		mImageView = (ImageView)findViewById( R.id.preview );
		if( isShowPreviewWallpaperByAdapter )
		{
			mImageView.setVisibility( View.GONE );
		}
		else
		{
			mImageView.setVisibility( View.VISIBLE );
		}
		packageReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(
					Context context ,
					Intent intent )
			{
				String actionName = intent.getAction();
				if( actionName.equals( StaticClass.ACTION_THUMB_CHANGED ) )
				{
					if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() && type.equals( "hot" ) )
					{
						mHotAdapter.updateThumb( intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ) );
					}
				}
				else if( actionName.equals( StaticClass.ACTION_HOTLIST_CHANGED ) )
				{
					if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() && type.equals( "hot" ) )
					{
						mHotAdapter.reloadPackage();
					}
				}
				else if( actionName.equals( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED ) )
				{
					Log.v( "********" , "hwh activity:ACTION_DOWNLOAD_STATUS_CHANGED" );
					isPause = false;
					if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() && type.equals( "hot" ) )
					{
						// @2015/09/09 UPD START
						String[] pkgName = null;
						String wallpaperPackageName = null;
						// @2015/09/09 UPD END
						for( int i = 0 ; i < mGallery.getChildCount() ; i++ )
						{
							WallpaperInformation information = (WallpaperInformation)mGallery.getSelectedItem();
							// @2015/09/09 UPD START
							pkgName = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ).split( "#" );
							if( pkgName != null )
							{
								wallpaperPackageName = pkgName[0];
							}
							else
							{
								wallpaperPackageName = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
							}
							// @2015/09/09 UPD END
							if( information != null && information.getPackageName().equals( wallpaperPackageName ) )
							{
								WallpaperService service = new WallpaperService( mThemeContext );
								service.queryWallpaper( wallpaperPackageName , information );
								break;
							}
						}
						appName = intent.getStringExtra( StaticClass.EXTRA_APP_NAME );
						updateShowStatus( appName );
						mHotAdapter.reloadPackage();
					}
					else
					{
						new Thread( new Runnable() {
							
							@Override
							public void run()
							{
								// TODO Auto-generated method stub
								try
								{
									Thread.sleep( 500 );
								}
								catch( InterruptedException e )
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								localList = queryDownloadList();
								runOnUiThread( new Runnable() {
									
									@Override
									public void run()
									{
										// TODO Auto-generated method stub
										mAdapter.notifyDataSetChanged();
										//mLocalViewPagerAdapter.notifyDataSetChanged();
									}
								} );
							}
						} ).start();
					}
				}
				else if( actionName.equals( StaticClass.ACTION_PREVIEW_CHANGED ) )
				{
					String pkg = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
					if( pkg != null && pkg.equals( ( (WallpaperInformation)mGallery.getSelectedItem() ).getPackageName() ) )
					{
						if( mPreviewLoader != null && mPreviewLoader.getStatus() != PreviewImageTask.Status.FINISHED )
						{
							mPreviewLoader.cancel();
						}
						mPreviewLoader = (PreviewImageTask)new PreviewImageTask().execute( pkg );
						if( isShowPreviewWallpaperByAdapter )
						{
							if( mHotViewPagerAdapter != null )
							{
								mHotViewPagerAdapter.notifyDataSetChanged();
							}
						}
					}
				}
				else if( actionName.equals( StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED ) )
				{
					if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() && type.equals( "hot" ) )
					{
						// @2015/09/09 ADD START by gaominghui
						String[] pkgName = null;
						String tempPkgName = null;
						// @2015/09/09 ADD END by gaominghui
						for( int i = 0 ; i < mGallery.getCount() ; i++ )
						{
							WallpaperInformation information = (WallpaperInformation)mGallery.getItemAtPosition( i );
							pkgName = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ).split( "#" );
							if( pkgName != null )
							{
								tempPkgName = pkgName[0];
							}
							else
							{
								tempPkgName = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
							}
							if( information.getPackageName().equals( tempPkgName ) )
							{
								information.setDownloadSize( intent.getIntExtra( StaticClass.EXTRA_DOWNLOAD_SIZE , 0 ) );
								information.setTotalSize( intent.getIntExtra( StaticClass.EXTRA_TOTAL_SIZE , 0 ) );
								if( i == mGallery.getSelectedItemPosition() )
									updateProgressSize();
								break;
							}
						}
						//mHotAdapter.reloadPackage(); //下载的时候推出后再进入preview界面会一直调用此方法。需要注释
					}
				}
			}
		};
		if( "local".equals( type ) )
		{
			isLoadComplete = false;
			if( clickPosition == -1 )
			{
				clickPosition = 0;
			}
			initInfo( clickPosition );
		}
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				isLockSupportChangeWallpaper = getcurrentLockInfo();
			}
		} ).start();
		initPreviewButton();//预览按钮显示
		IntentFilter screenFilter1 = new IntentFilter();
		screenFilter1.addAction( StaticClass.ACTION_THUMB_CHANGED );
		screenFilter1.addAction( StaticClass.ACTION_HOTLIST_CHANGED );
		screenFilter1.addAction( StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED );
		screenFilter1.addAction( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED );
		screenFilter1.addAction( StaticClass.ACTION_PREVIEW_CHANGED );
		registerReceiver( packageReceiver , screenFilter1 );
	}
	
	/*读取桌面预览配置开关*/
	private final String LAUNCHER_CONFIG_FILENAME = "default/default_layout.xml";
	private final String CUSTOM_LAUNCHER_CONFIG_FILENAME = "/system/launcher/default_layout.xml";
	private final String CUSTOM_FIRST_LAUNCHER_CONFIG_FILENAME = "/system/oem/launcher/default_layout.xml";
	
	private void readDefaultData()
	{
		try
		{
			Context remoteContext = mThemeContext.createPackageContext( currentLauncherPackageName , Context.CONTEXT_IGNORE_SECURITY );
			InputSource xmlin = null;
			File f1 = new File( CUSTOM_FIRST_LAUNCHER_CONFIG_FILENAME );
			if( !f1.exists() )
			{
				f1 = new File( CUSTOM_LAUNCHER_CONFIG_FILENAME );
			}
			boolean builtIn = ( getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM ) != 0;
			try
			{
				if( builtIn && f1.exists() )
					xmlin = new InputSource( new FileInputStream( f1.getAbsolutePath() ) );
				else
					xmlin = new InputSource( remoteContext.getAssets().open( LAUNCHER_CONFIG_FILENAME ) );
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
		catch( NameNotFoundException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 *
	 * @see android.app.Activity#onResume()
	 * @auther gaominghui  2016年1月29日
	 */
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		// @2016/01/29 ADD START友盟 
		if( isUmengStatistics_key )
		{
			Log.i( LOG_TAG , "WallpaperPreviewHotActivity onResume !!!" );
			MobclickAgent.onPageStart( "WallpaperPreviewActivity" );
			MobclickAgent.onResume( this );
		}
		// @2016/01/29 ADD END
	}
	
	/**
	 *
	 * @see android.app.Activity#onPause()
	 * @auther gaominghui  2016年1月29日
	 */
	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		// @2016/01/29 ADD START 友盟 
		if( FunctionConfig.isUmengStatistics_key() )
		{
			Log.i( LOG_TAG , "WallpaperPreviewHotActivity onPause !!!" );
			MobclickAgent.onPageEnd( "WallpaperPreviewActivity" );
			MobclickAgent.onPause( this );
		}
		// @2016/01/29 ADD END
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
			if( localName.equals( GENERAl_CONFIG ) )
			{
				String temp;
				temp = atts.getValue( "enable_launcher_take_screen_shot" );
				if( temp != null && temp.equals( "true" ) )
				{
					isEnableLauncherTakeScreenShot = true;
				}
				else
				{
					isEnableLauncherTakeScreenShot = false;
				}
			}
		}
		
		@Override
		public void endDocument() throws SAXException
		{
			// TODO Auto-generated method stub
			super.endDocument();
		}
	}
	
	private void initPreviewButton()
	{
		readDefaultData();
		if( isShowPreviewBtnBoolean )
		{
			if( isEnableLauncherTakeScreenShot || ( isLockSupportChangeWallpaper && isShowApplyLockBtn ) )
			{
				findViewById( R.id.btnpreview ).setVisibility( View.VISIBLE );
			}
			else
			{
				findViewById( R.id.btnpreview ).setVisibility( View.GONE );
			}
		}
		else
		{
			findViewById( R.id.btnpreview ).setVisibility( View.GONE );
		}
		if( brzh_set_desktop_lock_wallpaper )
		{
			findViewById( R.id.setlockwallpaper ).setVisibility( View.GONE );
			findViewById( R.id.setdesktopwallpaper ).setVisibility( View.GONE );
		}
		else
		{
			//是否显示应用锁屏壁纸按钮
			if( isShowApplyLockBtn )
			{
				findViewById( R.id.setlockwallpaper ).setVisibility( View.VISIBLE );
			}
			else
			{
				if( findViewById( R.id.btnpreview ).getVisibility() != View.VISIBLE )
				{
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT );
					lp.setMargins( 50 , 0 , 50 , 0 );
					findViewById( R.id.setdesktopwallpaper ).setLayoutParams( lp );
				}
				findViewById( R.id.setlockwallpaper ).setVisibility( View.GONE );
			}
		}
	}
	
	private Boolean getCurrentLockInfoDZ()
	{
		LockManager mgr = new LockManager( this );
		List<LockInformation> installList = mgr.queryInstallList();
		for( LockInformation infor : installList )
		{
			Context dstContext = null;
			try
			{
				dstContext = createPackageContext( infor.getPackageName() , Context.CONTEXT_IGNORE_SECURITY );
			}
			catch( NameNotFoundException e )
			{
				e.printStackTrace();
				return false;
			}
			ContentConfig destContent = new ContentConfig();
			destContent.loadConfig( dstContext , infor.getClassName() );
			if( destContent.getLockStyleValue().equals( String.valueOf( Settings.System.getInt( mThemeContext.getContentResolver() , "system.settings.lockstyle" , -1 ) ) ) )
			{
				return true;
			}
		}
		if( getcurrentLockInfo() )
		{
			return true;
		}
		return false;
	}
	
	private Boolean getcurrentLockInfo()
	{
		LockManager mgr = new LockManager( this );
		currentLock = mgr.queryCurrentLock();
		//第三方锁屏也要支持换壁纸,第三方换壁纸也要从我们的路径读取图片
		/*if( "com.third.test".equals( currentLock.getPackageName() ) )
		{
			return true;
		}*/
		Context dstContext = null;
		try
		{
			if( currentLock != null && currentLock.getPackageName() != null )
			{
				dstContext = createPackageContext( currentLock.getPackageName() , Context.CONTEXT_IGNORE_SECURITY );
			}
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
			return false;
		}
		ContentConfig destContent = new ContentConfig();
		destContent.loadConfig( dstContext , currentLock.getClassName() );
		if( destContent.getBackgroundPathString().equals( "" ) )
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	private void initInfo(
			final int mPosition )
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( wallpapers_from_other_apk != null )
				{
					try
					{
						Context remountContext = mThemeContext.createPackageContext( wallpapers_from_other_apk , Context.CONTEXT_IGNORE_SECURITY );
						Resources res = remountContext.getResources();
						for( int i = 1 ; ; i++ )
						{
							try
							{
								int drawable = res.getIdentifier( "wallpaper_" + ( i < 10 ? "0" + i : i ) + "_small" , "drawable" , wallpapers_from_other_apk );
								//int drawablewallpaper = res.getIdentifier( "wallpaper_" + ( i < 10 ? "0" + i : i ) , "drawable" , wallpapers_from_other_apk );
								if( drawable == 0 )
								{
									break;
								}
								//Bitmap bitmap = Tools.drawableToBitmap( res.getDrawable( drawable ) );
								mThumbs.add( "wallpaper_" + ( i < 10 ? "0" + i : i ) );
								localBmp.add( res.getDrawable( drawable ) );
								if( isShowPreviewWallpaperByAdapter )
								{
									wallpaperMap.put( String.valueOf( i - 1 ) , defaultDrawable );
								}
							}
							catch( IllegalArgumentException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if( isShowPreviewWallpaperByAdapter )
						{
							int drawable = res.getIdentifier( "wallpaper_" + ( clickPosition + 1 < 10 ? "0" + clickPosition + 1 : clickPosition + 1 ) , "drawable" , wallpapers_from_other_apk );
							if( drawable == 0 )
							{
								wallpaperMap.put( String.valueOf( clickPosition ) , defaultDrawable );
							}
							else
							{
								wallpaperMap.put( String.valueOf( clickPosition ) , res.getDrawable( drawable ) );
							}
						}
					}
					catch( NameNotFoundException e )
					{
						Log.e( "tabwallpaper" , "createPackageContext exception: " + e );
					}
				}
				else
				{
					infos.findWallpapers( mThumbs , customPath );
					useCustomWallpaper = infos.isUseCustomWallpaper();
					customWallpaperPath = infos.getCustomWallpaperPath();
					for( int i = 0 ; i < mThumbs.size() ; i++ )
					{
						//第二个参数0表示加载小的缩略图
						getCustomWallpaperDrawable( mThumbs.get( i ) , 0 , i );
						if( isShowPreviewWallpaperByAdapter )
						{
							wallpaperMap.put( String.valueOf( i ) , defaultDrawable );
						}
					}
					if( isShowPreviewWallpaperByAdapter )
					{
						if( clickPosition < mThumbs.size() )
						{
							getCustomWallpaperDrawable( mThumbs.get( clickPosition ).replace( "_small" , "" ) , 1 , clickPosition );
						}
					}
				}
				localList = queryDownloadList();
				runOnUiThread( new Runnable() {
					
					public void run()
					{
						if( mAdapter != null )
						{
							mAdapter.notifyDataSetChanged();
						}
						//此项不需要通知更新，会导致先刷新第一张后再跳转到相应的项
						if( isShowPreviewWallpaperByAdapter )
						{
							if( mLocalViewPagerAdapter != null )
							{
								mLocalViewPagerAdapter.notifyDataSetChanged();
							}
							mViewPager.setCurrentItem( clickPosition , false );
						}
						mGallery.setSelection( clickPosition );
					}
				} );
			}
		} ).start();
	}
	
	//在显示第一张预览图的同时继续加载其他的预览图,解决进入本地内置壁纸Loading时间长的问题
	private void LoadLocalWallpaperThread()
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( wallpapers_from_other_apk != null )
				{
					try
					{
						Context remountContext = mThemeContext.createPackageContext( wallpapers_from_other_apk , Context.CONTEXT_IGNORE_SECURITY );
						Resources res = remountContext.getResources();
						for( int i = 1 ; ; i++ )
						{
							try
							{
								int drawable = res.getIdentifier( "wallpaper_" + ( i < 10 ? "0" + i : i ) , "drawable" , wallpapers_from_other_apk );
								if( drawable == 0 )
								{
									break;
								}
								Drawable mDrawable = res.getDrawable( drawable );
								wallpaperMap.put( String.valueOf( i - 1 ) , mDrawable );
							}
							catch( IllegalArgumentException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					catch( NameNotFoundException e )
					{
						Log.e( "tabwallpaper" , "createPackageContext exception: " + e );
					}
				}
				else
				{
					String thumb = null;
					for( int i = 0 ; i < mThumbs.size() ; i++ )
					{
						//第二个参数1表示加载大的预览图
						try
						{
							thumb = mThumbs.get( i ).replace( "_small" , "" );
							getCustomWallpaperDrawable( thumb , 1 , i );
						}
						catch( Exception e )
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				runOnUiThread( new Runnable() {
					
					public void run()
					{
						isLoadComplete = true;
						if( mLocalViewPagerAdapter != null )
						{
							mLocalViewPagerAdapter.notifyDataSetChanged();
						}
					}
				} );
			}
		} ).start();
	}
	
	private synchronized List<WallpaperInformation> queryDownloadList()
	{
		List<WallpaperInformation> locallist = new ArrayList<WallpaperInformation>();
		WallpaperService themeSv = new WallpaperService( WallpaperPreviewActivity.this );
		List<WallpaperInformation> installList = themeSv.queryDownloadList();
		for( WallpaperInformation info : installList )
		{
			info.setThumbImage( WallpaperPreviewActivity.this , info.getPackageName() , info.getClassName() );
			locallist.add( info );
		}
		return locallist;
	}
	
	private void querryCurrentWallpeper()
	{
		currentWallpaper = getWallpaper( mThemeContext , "currentWallpaper" );
		if( currentWallpaper == null || currentWallpaper.trim().length() == 0 )
		{
			currentWallpaper = "default";
		}
	}
	
	private String getWallpaper(
			Context context ,
			String name )
	{
		ContentResolver resolver = context.getContentResolver();
		Uri uri;
		uri = Uri.parse( "content://" + currentLauncherProvider + "/" + "wallpaper" );
		String[] projection = null;
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		String theme = "default";
		try
		{
			selection = " propertyName=? ";
			selectionArgs = new String[]{ name };
			Cursor cursor = resolver.query( uri , projection , selection , selectionArgs , sortOrder );
			if( cursor != null )
			{
				if( cursor.moveToFirst() )
				{
					theme = cursor.getString( cursor.getColumnIndex( "propertyValue" ) );
				}
				cursor.close();
			}
			else
			{
				theme = null;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		if( theme == null || theme.trim().length() == 0 )
		{
			theme = null;
		}
		return theme;
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//		if( imgDefaultThumb != null && !imgDefaultThumb.isRecycled() )
		//		{
		//			imgDefaultThumb.recycle();
		//		}
		wallpaperMap.clear();
		if( mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED )
		{
			mLoader.cancel( true );
			mLoader = null;
		}
		if( mPreviewLoader != null && mPreviewLoader.getStatus() != PreviewImageTask.Status.FINISHED )
		{
			mPreviewLoader.cancel( true );
			mPreviewLoader = null;
		}
		if( packageReceiver != null )
		{
			unregisterReceiver( packageReceiver );
		}
		if( mAdapter != null )
		{
			mAdapter.onDestory();
		}
		if( mHotAdapter != null )
		{
			mHotAdapter.onDestory();
		}
		ActivityManager.popupActivity( this );
		// gaominghui@2017/01/03 ADD START 解注册home键监听广播
		unregisterReceiver( mCloseSystemDialogsReceiver );
		// gaominghui@2017/01/03 ADD END 解注册home键监听广播
		System.exit( 0 );
	}
	
	private int selectPositon;
	
	public void onItemSelected(
			AdapterView parent ,
			View v ,
			int position ,
			long id )
	{
		selectPositon = position;
		System.out.println( "selected position = " + position );
		Log.v( "WallpaperPreviewActivity" , "selected position = " + position );
		if( type.equals( "local" ) )
		{
			if( mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED )
			{
				mLoader.cancel();
			}
			relativeNormal.findViewById( R.id.setwallpaper ).setVisibility( View.VISIBLE );
			mLoader = (WallpaperLoader)new WallpaperLoader().execute( position );
		}
		else
		{
			if( mPreviewLoader != null && mPreviewLoader.getStatus() != PreviewImageTask.Status.FINISHED )
			{
				mPreviewLoader.cancel();
			}
			mPreviewLoader = (PreviewImageTask)new PreviewImageTask().execute( ( (WallpaperInformation)mHotAdapter.getItem( position ) ).getPackageName() );
			updateShowStatus( ( (WallpaperInformation)mHotAdapter.getItem( position ) ).getDisplayName() );
		}
		if( isShowPreviewWallpaperByAdapter )
		{
			mViewPager.setCurrentItem( position , false );
		}
		if( mGallery.getSelectedItem() instanceof WallpaperInformation )
		{
			if( ( (WallpaperInformation)mGallery.getSelectedItem() ).isDownloaded( this ) && ( (WallpaperInformation)mGallery.getSelectedItem() ).getDownloadStatus() != DownloadStatus.StatusDownloading )
			{
				delete.setVisibility( View.VISIBLE );
				return;
			}
		}
		else
		{
			delete.setVisibility( View.GONE );
		}
	}
	
	// @gaominghui2015/09/09 ADD START
	/**
	 *设置壁纸名字
	 * @param item
	 * @param pkg
	 * @author gaominghui 2015年9月9日
	 */
	// @gaominghui2015/09/09 ADD END
	private void updateShowStatus(
			String appName )
	{
		Log.i( "WallpaperPreviewActivity" , " updateShowStatus!!!appName = " + appName );
		WallpaperInformation Information = (WallpaperInformation)mGallery.getSelectedItem();
		if( !( Information.getDisplayName().equals( appName ) ) )
		{
			appName = Information.getDisplayName();
		}
		if( !( mGallery.getSelectedItem() instanceof WallpaperInformation ) )
		{
			return;
		}
		// @2015/09/09 ADD START by gaominghui
		boolean isDownload = false;
		//Log.i( "WallpaperPreviewActivity" , " updateShowStatus!!!Information.getpackageName = " + Information );
		//Log.i( "WallpaperPreviewActivity" , " updateShowStatus!!!appName = " + appName );
		/*appName = Information.getDisplayName();*/
		if( appName != null )
		{
			isDownload = Information.isWallpaperDownloaded( Information , this , "#" + appName );
		}
		if( !isDownload )
		{
			isDownload = Information.isDownloaded( this );
		}
		/*if( isDownload )
		{
			Information.setDownloadStatus( DownloadStatus.StatusFinish );
		}*/
		// @2015/09/09 ADD ENDby gaominghui
		if( isDownload && Information.getDownloadStatus() != DownloadStatus.StatusDownloading )
		{
			delete.setVisibility( View.VISIBLE );
		}
		else
		{
			delete.setVisibility( View.GONE );
		}
		relativeDownload.setClickable( false );
		if( Information == null )
		{
			return;
		}
		if( !isDownload )
		{
			boolean ispay = Tools.isContentPurchased( this , DownloadList.Wallpaper_Type , Information.getPackageName() );
			if( isPriceVisible && Information.getPrice() > 0 && !ispay )
			{
				relativeDownload.setVisibility( View.GONE );
				relativeNormal.setVisibility( View.VISIBLE );
				relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.INVISIBLE );
				relativeNormal.findViewById( R.id.setwallpaper ).setVisibility( View.INVISIBLE );
				relativeNormal.findViewById( R.id.btnBuy ).setVisibility( View.VISIBLE );
				return;
			}
			else
			{
				relativeDownload.setVisibility( View.GONE );
				relativeNormal.setVisibility( View.VISIBLE );
				relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.VISIBLE );
				relativeNormal.findViewById( R.id.setwallpaper ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnBuy ).setVisibility( View.GONE );
				return;
			}
		}
		if( Information.getDownloadStatus() == DownloadStatus.StatusFinish )
		{
			relativeDownload.setVisibility( View.GONE );
			relativeNormal.setVisibility( View.VISIBLE );
			relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.GONE );
			relativeNormal.findViewById( R.id.setwallpaper ).setVisibility( View.VISIBLE );
			if( brzh_set_desktop_lock_wallpaper )
			{
				relativeNormal.findViewById( R.id.setwallpaper ).findViewById( R.id.btnApplyWallpaper ).setVisibility( View.VISIBLE );
				findViewById( R.id.setlockwallpaper ).setVisibility( View.GONE );
				findViewById( R.id.setdesktopwallpaper ).setVisibility( View.GONE );
			}
			else
			{
				if( isShowPreviewBtnBoolean )
				{
					if( isEnableLauncherTakeScreenShot || ( isLockSupportChangeWallpaper && isShowApplyLockBtn ) )
					{
						findViewById( R.id.btnpreview ).setVisibility( View.VISIBLE );
					}
					else
					{
						findViewById( R.id.btnpreview ).setVisibility( View.GONE );
					}
					//findViewById( R.id.btnpreview ).setVisibility( View.VISIBLE );
				}
				//是否显示应用锁屏壁纸按钮
				if( isShowApplyLockBtn )
				{
					findViewById( R.id.setlockwallpaper ).setVisibility( View.VISIBLE );
				}
				else
				{
					if( findViewById( R.id.btnpreview ).getVisibility() != View.VISIBLE )
					{
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT );
						lp.setMargins( 50 , 0 , 50 , 0 );
						findViewById( R.id.setdesktopwallpaper ).setLayoutParams( lp );
					}
					findViewById( R.id.setlockwallpaper ).setVisibility( View.GONE );
				}
				if( progressbar != null && progressbar.getVisibility() == View.VISIBLE )
				{
					if( mPreviewLoader != null && mPreviewLoader.getStatus() != PreviewImageTask.Status.FINISHED )
					{
						mPreviewLoader.cancel();
					}
					mPreviewLoader = (PreviewImageTask)new PreviewImageTask().execute( Information.getPackageName() );
				}
			}
			return;
		}
		relativeDownload.setClickable( true );
		if( Information.getDownloadStatus() == DownloadStatus.StatusDownloading )
		{
			switchToDownloading();
		}
		else if( Information.getDownloadStatus() == DownloadStatus.StatusPause )
		{
			switchToPause();
		}
	}
	
	private void switchToDownloading()
	{
		relativeDownload.setVisibility( View.VISIBLE );
		relativeNormal.setVisibility( View.GONE );
		relativeDownload.findViewById( R.id.linearDownload ).setVisibility( View.VISIBLE );
		relativeDownload.findViewById( R.id.linearPause ).setVisibility( View.GONE );
		delete.setVisibility( View.INVISIBLE );
		updateProgressSize();
	}
	
	private void switchToPause()
	{
		relativeDownload.setVisibility( View.VISIBLE );
		relativeNormal.setVisibility( View.GONE );
		relativeDownload.findViewById( R.id.linearDownload ).setVisibility( View.GONE );
		relativeDownload.findViewById( R.id.linearPause ).setVisibility( View.VISIBLE );
		delete.setVisibility( View.VISIBLE );
		updateProgressSize();
	}
	
	private void updateProgressSize()
	{
		WallpaperInformation Information = (WallpaperInformation)mGallery.getSelectedItem();
		if( Information == null )
		{
			return;
		}
		if( findViewById( R.id.linearDownload ).getVisibility() == View.VISIBLE )
		{
			ProgressBar progressBar = (ProgressBar)findViewById( R.id.progressBarDown );
			progressBar.setProgress( Information.getDownloadPercent() );
			TextView text = (TextView)findViewById( R.id.textDownPercent );
			text.setText( getString( R.string.textDownloading , Information.getDownloadPercent() ) );
		}
		else
		{
			ProgressBar progressBar = (ProgressBar)findViewById( R.id.progressBarPause );
			progressBar.setProgress( Information.getDownloadPercent() );
			TextView text = (TextView)findViewById( R.id.textPausePercent );
			text.setText( getString( R.string.textPause , Information.getDownloadPercent() ) );
		}
	}
	
	public class PreviewImageTask extends AsyncTask<String , Integer , Boolean>
	{
		
		public PreviewImageTask()
		{
		}
		
		@Override
		protected void onPostExecute(
				Boolean result )
		{
			// TODO Auto-generated method stub
			if( progressbar != null && !result )
			{
				// TODO Auto-generated method stub
				progressbar.setVisibility( View.INVISIBLE );
			}
			if( !isShowPreviewWallpaperByAdapter )
			{
				mImageView.setImageDrawable( currentDrawable );
			}
		}
		
		@Override
		protected void onPreExecute()
		{
			// TODO Auto-generated method stub
			super.onPreExecute();
			if( progressbar != null && progressbar.getVisibility() != View.VISIBLE )
			{
				progressbar.setVisibility( View.VISIBLE );
			}
		}
		
		@Override
		protected Boolean doInBackground(
				String ... params )
		{
			// TODO Auto-generated method stub
			// List<LockInformation> result = queryPackage();
			// return result;
			String packageName = params[0];
			String[] strArray = PathTool.getPreviewLists( packageName );
			boolean needDownImage = true;
			String imagePath = null;
			if( strArray != null && strArray.length != 0 )
			{
				imagePath = strArray[0];
			}
			else if( PathTool.getAppFile( packageName ) != null )
			{
				imagePath = PathTool.getAppFile( packageName );
			}
			if( imagePath != null )
			{
				FileInputStream is = null;
				try
				{
					currentWallpaperPath = imagePath;
					Drawable mDrawable = null;
					// gaominghui@2016/04/25 ADD START 0004203: 美化中心->在线壁纸，选择任意壁纸下载，在这个界面左右滑动，提示美化中心停止运行,出现OOM
					preferences = mThemeContext.getSharedPreferences( "isRemove_enable_support_lockwallpaper_judge" , MODE_PRIVATE );
					preResult = preferences.getBoolean( "enable_preview_wallpaper_lowMemory" , false );
					if( preResult )
					{
						Log.i( "andy" , "doInBackground imagePath = " + imagePath );
						File imageFile = new File( imagePath );
						mDrawable = decodeFile( imageFile );
					}
					else
					{
						is = new FileInputStream( imagePath );
						mDrawable = Drawable.createFromStream( is , "" );
					}
					// gaominghui@2016/04/25 ADD END 0004203: 美化中心->在线壁纸，选择任意壁纸下载，在这个界面左右滑动，提示美化中心停止运行,出现OOM
					currentDrawable = mDrawable;
					if( mDrawable == null )
					{
						needDownImage = true;
					}
					else
					{
						needDownImage = false;
					}
				}
				catch( Exception e1 )
				{
					// TODO Auto-generated catch block
					needDownImage = true;
				}
				if( is != null )
				{
					try
					{
						if( is != null )
						{
							is.close();
						}
					}
					catch( IOException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if( needDownImage )
			{
				if( downModule != null )
					downModule.downloadPreview( packageName , DownloadList.Wallpaper_Type );
			}
			return needDownImage;
		}
		
		void cancel()
		{
			super.cancel( true );
		}
	}
	
	// gaominghui@2016/04/25 ADD START 0004203: 美化中心->在线壁纸，选择任意壁纸下载，在这个界面左右滑动，提示美化中心停止运行,出现OOM
	private Drawable decodeFile(
			File f )
	{
		try
		{
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream( new FileInputStream( f ) , null , o );
			// The new size we want to scale to
			final int REQUIRED_SIZE = 400;
			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			while( o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE )
				scale *= 2;
			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			Bitmap bitmap = BitmapFactory.decodeStream( new FileInputStream( f ) , null , o2 );
			ImageView iv = new ImageView( WallpaperPreviewActivity.this );
			iv.setImageBitmap( bitmap );
			return iv.getDrawable();
		}
		catch( FileNotFoundException e )
		{
			Log.e( "andy" , "decodeFile  FileNotFoundException e = " + e );
		}
		return null;
	}
	
	private Drawable decodeInPutStream(
			InputStream is )
	{
		// Decode image size
		InputStream isCloneInputStream = is;
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream( is , null , o );
		// The new size we want to scale to
		final int REQUIRED_SIZE = 400;
		// Find the correct scale value. It should be the power of 2.
		int scale = 1;
		while( o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE )
			scale *= 2;
		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeStream( isCloneInputStream , null , o2 );
		ImageView iv = new ImageView( WallpaperPreviewActivity.this );
		iv.setImageBitmap( bitmap );
		return iv.getDrawable();
	}
	
	// gaominghui@2016/04/25 ADD END 0004203: 美化中心->在线壁纸，选择任意壁纸下载，在这个界面左右滑动，提示美化中心停止运行,出现OOM
	public void onNothingSelected(
			AdapterView parent )
	{
	}
	
	private class HotViewPagerAdapter extends PagerAdapter
	{
		
		private List<View> mListViews;
		private LayoutInflater mLayoutInflater;
		private Context mContext;
		DisplayImageOptions options;
		
		HotViewPagerAdapter(
				WallpaperPreviewActivity context )
		{
			mContext = context;
			mLayoutInflater = context.getLayoutInflater();
			//add by liuhailin begin
			options = new DisplayImageOptions.Builder().showStubImage( R.drawable.default_img_large ).showImageForEmptyUri( R.drawable.default_img_large )
					.showImageOnFail( R.drawable.default_img_large ).cacheInMemory().cacheOnDisc().bitmapConfig( Bitmap.Config.RGB_565 ).build();
			//add by liuhailin end
		}
		
		@Override
		public void destroyItem(
				ViewGroup container ,
				int position ,
				Object object )
		{
			//container.removeView( mListViews.get( position ) );//删除页卡  
			( (ViewPager)container ).removeView( (View)object );
		}
		
		@Override
		public Object instantiateItem(
				ViewGroup container ,
				int position )
		{ //这个方法用来实例化页卡         
			String imagePath = null;
			View imageLayout = mLayoutInflater.inflate( R.layout.wallpaper_preview_pager_item , container , false );
			ImageView imageView = (ImageView)imageLayout.findViewById( R.id.image );
			//container.addView( mListViews.get( position ) , 0 );//添加页卡  
			WallpaperInformation info = appList.get( position );
			String[] strArray = PathTool.getPreviewLists( info.getPackageName() );
			if( strArray != null && strArray.length != 0 )
			{
				imagePath = strArray[0];
			}
			else if( PathTool.getAppFile( info.getPackageName() ) != null )
			{
				imagePath = PathTool.getAppFile( info.getPackageName() );
			}
			String tempPath = "file:///" + imagePath;
			ImageLoader.getInstance().displayImage( tempPath , imageView , options );
			( (ViewPager)container ).addView( imageLayout , 0 );
			return imageLayout;
		}
		
		@Override
		public int getCount()
		{
			return appList.size();//返回页卡的数量  
		}
		
		@Override
		public int getItemPosition(
				Object object )
		{
			// TODO Auto-generated method stub
			return POSITION_NONE;
		}
		
		@Override
		public boolean isViewFromObject(
				View arg0 ,
				Object arg1 )
		{
			return arg0 == arg1;//官方提示这样写  
		}
	}
	
	private class ImageHotAdapter extends BaseAdapter
	{
		
		private Context context;
		private DownModule downThumb;
		private Bitmap imgDefaultThumb;
		private Set<String> firstAdd = new HashSet<String>();
		private PageTask pageTask = null;
		
		public ImageHotAdapter(
				Context cxt ,
				DownModule downModule )
		{
			context = cxt;
			downThumb = downModule;
			imgDefaultThumb = ( (BitmapDrawable)cxt.getResources().getDrawable( R.drawable.default_img_large ) ).getBitmap();
			if( pageTask != null && pageTask.getStatus() != PageTask.Status.FINISHED )
			{
				pageTask.cancel( true );
			}
			pageTask = (PageTask)new PageTask().execute();
		}
		
		public void reloadPackage()
		{
			if( pageTask != null && pageTask.getStatus() != PageTask.Status.FINISHED )
			{
				pageTask.cancel( true );
			}
			pageTask = (PageTask)new PageTask().execute();
		}
		
		public ArrayList<WallpaperInformation> queryPackage(
				Set<String> set )
		{
			ArrayList<WallpaperInformation> list = new ArrayList<WallpaperInformation>();
			WallpaperService service = new WallpaperService( context );
			List<WallpaperInformation> hotList = service.queryShowList();
			if( firstAdd.size() > 0 )
			{
				for( WallpaperInformation item : hotList )
				{
					if( firstAdd.contains( item.getPackageName() ) )
					{
						service.queryWallpaper( item.getPackageName() , item );
						list.add( item );
					}
				}
			}
			else
			{
				for( WallpaperInformation item : hotList )
				{
					if( !set.contains( item.getPackageName() ) )
					{
						//service.queryWallpaper( item.getPackageName() , item ); //在下载时候推出preview在进入,此处耗时太大
						list.add( item );
						firstAdd.add( item.getPackageName() );
					}
				}
			}
			return list;
		}
		
		private int findPackageIndex(
				String packageName )
		{
			int i = 0;
			for( i = 0 ; i < appList.size() ; i++ )
			{
				if( packageName.equals( appList.get( i ).getPackageName() ) )
				{
					return i;
				}
			}
			return -1;
		}
		
		public class PageTask extends AsyncTask<String , Integer , List<WallpaperInformation>>
		{
			
			public PageTask()
			{
			}
			
			@Override
			protected void onPostExecute(
					List<WallpaperInformation> result )
			{
				// TODO Auto-generated method stub
				synchronized( appList )
				{
					for( WallpaperInformation info : appList )
					{
						info.disposeThumb();
						info = null;
					}
					appList.clear();
					appList.addAll( result );
				}
				//此项不需要通知更新,会导致闪屏
				//				if( mHotViewPagerAdapter != null )
				//				{
				//					mHotViewPagerAdapter.notifyDataSetChanged();
				//				}
				notifyDataSetChanged();
				if( clickPosition != -1 )
				{
					mGallery.setSelection( clickPosition );
					//mViewPager.setCurrentItem( position , false );
					clickPosition = -1;
				}
				pageTask = null;
			}
			
			@Override
			protected void onPreExecute()
			{
				// TODO Auto-generated method stub
				super.onPreExecute();
			}
			
			@Override
			protected List<WallpaperInformation> doInBackground(
					String ... params )
			{
				// TODO Auto-generated method stub
				Log.v( "WallpaperPreviewActivity" , "PageTask doInBackground" );
				if( firstAdd.size() > 0 )
				{
					return queryPackage( null );
				}
				Set<String> packageNameSet = new HashSet<String>();
				WallpaperService sv = new WallpaperService( WallpaperPreviewActivity.this );
				List<WallpaperInformation> installList = sv.queryDownloadList();
				for( WallpaperInformation info : installList )
				{
					// @2015/09/09 ADD START
					//packageNameSet.add( info.getPackageName() );
					packageNameSet.add( info.getPackageName().split( "#" )[0] );
					// @2015/09/09 ADD END
				}
				return queryPackage( packageNameSet );
			}
		}
		
		public class ImageTask extends AsyncTask<String , Integer , Bitmap>
		{
			
			public ImageTask()
			{
			}
			
			@Override
			protected void onPostExecute(
					Bitmap result )
			{
				// TODO Auto-generated method stub
				if( isShowPreviewWallpaperByAdapter )
				{
					if( mHotViewPagerAdapter != null )
					{
						mHotViewPagerAdapter.notifyDataSetChanged();
					}
				}
				notifyDataSetChanged();
			}
			
			@Override
			protected void onPreExecute()
			{
				// TODO Auto-generated method stub
				super.onPreExecute();
			}
			
			@Override
			protected Bitmap doInBackground(
					String ... params )
			{
				// TODO Auto-generated method stub
				// List<LockInformation> result = queryPackage();
				// return result;
				int findIndex = findPackageIndex( params[0] );
				if( findIndex < 0 )
				{
					return null;
				}
				WallpaperInformation info = appList.get( findIndex );
				info.reloadThumb();
				return null;
			}
		}
		
		public void updateThumb(
				String pkgName )
		{
			new ImageTask().execute( pkgName );
			// int findIndex = findPackageIndex(pkgName);
			// if (findIndex < 0) {
			// return;
			// }
			// WallpaperInformation info = appList.get(findIndex);
			// info.reloadThumb();
			// notifyDataSetChanged();
		}
		
		public void onDestory()
		{
			if( appList == null )
			{
				return;
			}
			for( WallpaperInformation info : appList )
			{
				info.disposeThumb();
			}
			appList.clear();
			appList = null;
		}
		
		@Override
		public int getCount()
		{
			return appList.size();
		}
		
		@Override
		public Object getItem(
				int position )
		{
			return appList.get( position );
		}
		
		@Override
		public long getItemId(
				int position )
		{
			return position;
		}
		
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			ImageView image = null;
			if( convertView == null )
			{
				image = (ImageView)LayoutInflater.from( context ).inflate( R.layout.wallpaper_preview_item , parent , false );
			}
			else
			{
				image = (ImageView)convertView;
			}
			WallpaperInformation info = (WallpaperInformation)getItem( position );
			if( info.isNeedLoadDetail() )
			{
				info.loadDetail( context );
				if( info.getThumbImage() == null )
				{
					downThumb.downloadThumb( info.getPackageName() , DownloadList.Wallpaper_Type );
				}
			}
			Bitmap imgThumb = info.getThumbImage();
			if( imgThumb == null )
			{
				imgThumb = imgDefaultThumb;
			}
			image.setImageBitmap( imgThumb );
			return image;
		}
	}
	
	public class LocalViewPagerAdapter extends PagerAdapter
	{
		
		private List<View> mListViews;
		private LayoutInflater mLayoutInflater;
		DisplayImageOptions options;
		
		LocalViewPagerAdapter(
				WallpaperPreviewActivity context )
		{
			mLayoutInflater = context.getLayoutInflater();
			//add by liuhailin begin
			options = new DisplayImageOptions.Builder().showStubImage( R.drawable.default_img_large ).showImageForEmptyUri( R.drawable.default_img_large )
					.showImageOnFail( R.drawable.default_img_large ).cacheInMemory().cacheOnDisc().bitmapConfig( Bitmap.Config.RGB_565 ).build();
			//add by liuhailin end
		}
		
		@Override
		public void destroyItem(
				ViewGroup container ,
				int position ,
				Object object )
		{
			//container.removeView( mListViews.get( position ) );//删除页卡  
			( (ViewPager)container ).removeView( (View)object );
		}
		
		@Override
		public Object instantiateItem(
				ViewGroup container ,
				int position )
		{ //这个方法用来实例化页卡         
			//Log.i( "WallpaperPreviewActivity" , "instantiateItem position = " + position );
			//Log.i( "WallpaperPreviewActivity" , "instantiateItem  localList.size() = " + localList.size() );
			String imagePath = null;
			View imageLayout = mLayoutInflater.inflate( R.layout.wallpaper_preview_pager_item , container , false );
			ImageView imageView = (ImageView)imageLayout.findViewById( R.id.image );
			//container.addView( mListViews.get( position ) , 0 );//添加页卡
			if( position < mThumbs.size() )
			{
				imageView.setImageDrawable( wallpaperMap.get( String.valueOf( position ) ) );//( localWallpaperBmp.get( position ) );//localBmp.get( position ) );
			}
			else
			{
				try
				{
					String pkg = ( (WallpaperInformation)localList.get( position - mThumbs.size() ) ).getPackageName();
					imagePath = PathTool.getAppFile( pkg );
					String tempPath = "file:///" + imagePath;
					ImageLoader.getInstance().displayImage( tempPath , imageView , options );
					//imageView.setImageBitmap( Tools.getPurgeableBitmap( imagePath , -1 , -1 ) );
				}
				catch( Exception ex )
				{
					imageView.setImageDrawable( defaultDrawable );
				}
			}
			( (ViewPager)container ).addView( imageLayout , 0 );
			return imageLayout;
		}
		
		@Override
		public int getCount()
		{
			return localBmp.size() + localList.size();//返回页卡的数量  
		}
		
		@Override
		public int getItemPosition(
				Object object )
		{
			// TODO Auto-generated method stub
			return POSITION_NONE;
		}
		
		@Override
		public boolean isViewFromObject(
				View arg0 ,
				Object arg1 )
		{
			return arg0 == arg1;
		}
	}
	
	private class ImageAdapter extends BaseAdapter
	{
		
		private LayoutInflater mLayoutInflater;
		
		ImageAdapter(
				WallpaperPreviewActivity context )
		{
			mLayoutInflater = context.getLayoutInflater();
		}
		
		public int getCount()
		{
			return localBmp.size() + localList.size();
		}
		
		public Object getItem(
				int position )
		{
			if( position < mThumbs.size() )
			{
				return position;
			}
			return localList.get( position - mThumbs.size() );
		}
		
		public void onDestory()
		{
			if( localList != null )
			{
				for( WallpaperInformation info : localList )
				{
					info.disposeThumb();
				}
				localList.clear();
				localList = null;
			}
			//			for( Bitmap bmp : localBmp )
			//			{
			//				if( bmp != null && !bmp.isRecycled() )
			//				{
			//					bmp.recycle();
			//					bmp = null;
			//				}
			//			}
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
			ImageView image = null;
			if( convertView == null )
			{
				image = (ImageView)mLayoutInflater.inflate( R.layout.wallpaper_preview_item , parent , false );
			}
			else
			{
				image = (ImageView)convertView;
			}
			// int thumbRes = mThumbs.get(position);
			// image.setImageResource(thumbRes);
			if( position < mThumbs.size() )
			{
				image.setImageDrawable( localBmp.get( position ) );
			}
			else
			{
				Bitmap bmp = ( (WallpaperInformation)getItem( position ) ).getThumbImage();
				if( bmp != null )
				{
					image.setImageBitmap( bmp );
				}
			}
			Drawable thumbDrawable = image.getDrawable();
			if( thumbDrawable != null )
			{
				thumbDrawable.setDither( true );
			}
			return image;
		}
	}
	
	public void onClick(
			View v )
	{
		if( v.getId() == R.id.btnReturn )
		{
			finish();
		}
		if( v.getId() == R.id.btnpreview )
		{
			if( Tools.isFastDoubleClick() )
			{
				return;
			}
			String select = null;
			if( mGallery.getSelectedItem() instanceof WallpaperInformation )
			{
				select = ( (WallpaperInformation)mGallery.getSelectedItem() ).getPackageName();
			}
			else
			{
				select = mThumbs.get( mGallery.getSelectedItemPosition() ).replace( "_small" , "" );
			}
			Intent mIntent = new Intent();
			mIntent.setClass( WallpaperPreviewActivity.this , WallpaperPreViewWindowActivity.class );
			mIntent.putExtra( "isenablelaunchertakescreenshot" , isEnableLauncherTakeScreenShot );
			mIntent.putExtra( "imagePath" , currentWallpaperPath );
			mIntent.putExtra( "wallpaper" , select );
			mIntent.putExtra( "wallpapers_from_other_apk" , wallpapers_from_other_apk );
			mIntent.putExtra( "current_other_apk_res_name" , currentOtherApkWallpaperResName );
			mIntent.putExtra( "position" , mGallery.getSelectedItemPosition() );
			mIntent.putExtra( "isWallpaperInformation" , mGallery.getSelectedItem() instanceof WallpaperInformation );
			startActivity( mIntent );
		}
		else if( v.getId() == R.id.setdesktopwallpaper )
		{
			setDesktopWallpaper();
		}
		else if( v.getId() == R.id.setlockwallpaper )
		{
			//Log.v( "lvjiangbin" , "c  " + FunctionConfig.class );
			if( preResult )
			{
				isLockSupportChangeWallpaper = true;
			}
			if( !isLockSupportChangeWallpaper )
			{
				Toast.makeText( WallpaperPreviewActivity.this , R.string.lockwallpaper_apply_tost , Toast.LENGTH_SHORT ).show();
				return;
			}
			setLockWallpaper();
		}
		else if( v.getId() == R.id.btnApplyWallpaper )
		{
			WallpaperDialog wallpaperDialog = new WallpaperDialog( this );
			wallpaperDialog.show( getFragmentManager() , "wallpaperDialog" );
		}
		else if( v.getId() == R.id.btnDownload )
		{
			if( com.coco.theme.themebox.StaticClass.canDownToInternal )
			{
				File f = new File( PathTool.getAppDir() );
				int num = f.listFiles().length;
				if( num >= 5 )
				{
					recursionDeleteFile( new File( PathTool.getDownloadingDir() ) );
				}
			}
			else if( !com.coco.theme.themebox.StaticClass.isAllowDownloadWithToast( this ) )
			{
				return;
			}
			final WallpaperInformation Information = (WallpaperInformation)mGallery.getSelectedItem();
			if( Information == null )
			{
				return;
			}
			String enginePKG = Information.getEnginepackname();
			if( enginePKG != null && !enginePKG.equals( "" ) && !enginePKG.equals( "null" ) )
			{
				if( !Tools.isAppInstalled( this , enginePKG ) )
				{// 第三方引擎没有安装
					Tools.showNoticeDialog( this , enginePKG , Information.getEnginedesc() , Information.getEngineurl() , Information.getEnginesize() );
					return;
				}
			}
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					// @2015/01/19 ADD START
					//Log.v( "andy test" , "[WallpaperPreviewActivity]下载线程!" );
					// @2015/01/19 ADD END
					DownloadThemeService dSv = new DownloadThemeService( WallpaperPreviewActivity.this );
					DownloadThemeItem dItem = dSv.queryByPackageName( Information.getPackageName() , DownloadList.Wallpaper_Type );
					if( dItem == null )
					{
						dItem = new DownloadThemeItem();
						dItem.copyFromThemeInfo( Information.getInfoItem() );
						dItem.setDownloadStatus( DownloadStatus.StatusDownloading );
						dSv.insertItem( dItem );
					}
					WallpaperService service = new WallpaperService( WallpaperPreviewActivity.this );
					service.queryWallpaper( Information.getPackageName() , Information );
					runOnUiThread( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							Intent intent = new Intent();
							intent.setAction( StaticClass.ACTION_START_DOWNLOAD_APK );
							intent.putExtra( "apkname" , Information.getDisplayName() );
							intent.putExtra( "apkEnglishName" , Information.getApplicationName_en() );
							Log.v( "andy" , "英文名字 = " + Information.getApplicationName_en() );
							intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , Information.getPackageName() );
							//<c_0000707> liuhailin@2014-08-11 modify begin
							intent.putExtra( "position" , mGallery.getSelectedItemPosition() );
							//<c_0000707> liuhailin@2014-08-11 modify end
							sendBroadcast( intent );
							updateShowStatus( Information.getDisplayName() );
							/// @gaominghui2016/01/29 ADD START 友盟统计点击下载，下载壁纸
							if( isUmengStatistics_key )
							{
								MobclickAgent.onEvent( mThemeContext , "click_download" );
								MobclickAgent.onEvent( mThemeContext , "online_wallpaper_download" );
							}
							// @gaominghui20162016/01/29 ADD END
						}
					} );
				}
			} ).start();
		}
		else if( v.getId() == R.id.layoutDownload )
		{
			// @2015/01/19 ADD START
			//Log.v( "andy test" , "[WallpaperPreviewActivity]点击下载进度条 ! isPause= " + isPause );
			// @2015/01/19 ADD END
			if( isPause )
				return;
			String pkg = ( (WallpaperInformation)mGallery.getSelectedItem() ).getPackageName();
			if( relativeDownload.findViewById( R.id.linearDownload ).getVisibility() == View.VISIBLE )
			{
				isPause = true;
				// @2015/01/19 ADD START
				//Log.v( "andy" , "[WallpaperPreviewActivity]点击下载进度条 ! ACTION_PAUSE_DOWNLOAD_APK" );
				// @2015/01/19 ADD END
				Intent intent = new Intent( StaticClass.ACTION_PAUSE_DOWNLOAD_APK );
				intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , pkg );
				intent.putExtra( "apkname" , ( (WallpaperInformation)mGallery.getSelectedItem() ).getDisplayName() );
				// @gaominghui2015/04/02 ADD START添加壁纸的英文名字
				intent.putExtra( "apkEnglishName" , ( (WallpaperInformation)mGallery.getSelectedItem() ).getApplicationName_en() );
				Log.v( "andy" , "英文名字 = " + ( (WallpaperInformation)mGallery.getSelectedItem() ).getApplicationName_en() );
				// @gaominghui2015/04/02 ADD END
				sendBroadcast( intent );
				switchToPause();
				// @gaominghui2016/01/29 ADD START 友盟统计点击暂停
				if( isUmengStatistics_key )
				{
					MobclickAgent.onEvent( mThemeContext , "click_stop" );
				}
				// @gaominghui20162016/01/29 ADD END
			}
			else
			{
				// @2015/01/19 ADD START
				//Log.v( "andy test" , "[WallpaperPreviewActivity]点击下载进度条 ! ACTION_START_DOWNLOAD_APK" );
				// @2015/01/19 ADD END
				Intent intent = new Intent( StaticClass.ACTION_START_DOWNLOAD_APK );
				intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , pkg );
				// @gaominghui2015/04/02 ADD START添加壁纸的英文名字
				intent.putExtra( "apkEnglishName" , ( (WallpaperInformation)mGallery.getSelectedItem() ).getApplicationName_en() );
				Log.v( "andy" , "英文名字 = " + ( (WallpaperInformation)mGallery.getSelectedItem() ).getApplicationName_en() );
				// @gaominghui2015/04/02 ADD END
				intent.putExtra( "apkname" , ( (WallpaperInformation)mGallery.getSelectedItem() ).getDisplayName() );
				//<c_0000707> liuhailin@2014-08-11 modify begin
				intent.putExtra( "position" , mGallery.getSelectedItemPosition() );
				//<c_0000707> liuhailin@2014-08-11 modify end
				sendBroadcast( intent );
				switchToDownloading();
				// @gaominghui2016/01/29 ADD START 友盟统计点击继续下载
				if( isUmengStatistics_key )
				{
					MobclickAgent.onEvent( mThemeContext , "click_contiune_download" );
				}
				// @gaominghui20162016/01/29 ADD END
			}
		}
		else if( v.getId() == R.id.btnDel )
		{
			if( isEnableDeleteCurrentWallpaper )
			{
				querryCurrentWallpeper();
				if( currentWallpaperPath.contains( currentWallpaper ) )
				{
					Toast.makeText( mThemeContext , R.string.can_not_delete_wallpaper , Toast.LENGTH_SHORT ).show();
					return;
				}
			}
			if( mGallery.getSelectedItem() instanceof WallpaperInformation )
			{
				AlertDialog.Builder builder = null;
				//Log.i( "WallpaperPreviewActivity" , "SDK_INT = "+android.os.Build.VERSION.SDK_INT );
				if( android.os.Build.VERSION.SDK_INT >= 21 )
				{
					builder = new AlertDialog.Builder( mThemeContext , R.style.wallpaperDeleteDialog );
				}
				else
				{
					builder = new AlertDialog.Builder( mThemeContext );
				}
				builder.setMessage( R.string.makesure_to_delete );
				builder.setCancelable( true );
				builder.setPositiveButton( R.string.delete_ok , new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(
							DialogInterface dialog ,
							int which )
					{
						// TODO Auto-generated method stub
						// @gaominghui2016/01/29 ADD START 友盟统计插件卸载
						if( isUmengStatistics_key )
						{
							MobclickAgent.onEvent( mThemeContext , "online_wallpaper_uninstall" );
						}
						// @gaominghui20162016/01/29 ADD END
						String packageName = null;
						int position = mGallery.getSelectedItemPosition();
						WallpaperInformation wallpaperInfo = (WallpaperInformation)mGallery.getSelectedItem();
						if( mGallery != null && wallpaperInfo != null )
						{
							packageName = wallpaperInfo.getPackageName();
						}
						DownloadThemeService dSv = new DownloadThemeService( WallpaperPreviewActivity.this );
						dSv.deleteItem( packageName , DownloadList.Wallpaper_Type );
						File file = new File( PathTool.getAppFile( packageName ) );
						if( file != null && file.exists() )
						{
							file.delete();
							file = null;
						}
						file = new File( PathTool.getAppSmallFile( packageName ) );
						if( file != null && file.exists() )
						{
							file.delete();
							file = null;
						}
						Intent intent = new Intent( StaticClass.ACTION_PAUSE_DOWNLOAD_APK );
						intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , packageName );
						if( wallpaperInfo != null )
						{
							intent.putExtra( "apkname" , wallpaperInfo.getDisplayName() );
							// @gaominghui 2015/04/02 UPD START添加壁纸英文名字
							intent.putExtra( "apkEnglishName" , wallpaperInfo.getApplicationName_en() );
							//Log.v( "andy" , "英文名字 = " + ( (WallpaperInformation)mGallery.getSelectedItem() ).getApplicationName_en() );
						}
						// @gaominghui 2015/04/02 UPD END
						sendBroadcast( intent );
						if( "local".equals( type ) )//如果是下载到本地的壁纸
						{
							// @gaominghui2015/09/25 ADD START 修改美华中心下载到本地的壁纸点击删除报停（数组越界）
							if( !isShowPreviewWallpaperByAdapter )//如果大图不可滑动
							{
								mGallery.setSelection( position );
								mAdapter.notifyDataSetChanged();
								//Log.i( "WallpaperPreviewActivity" , "mGallery.getAdapter().getCount() = " + mGallery.getAdapter().getCount() + "; localList.size() = " + localList.size() );
								if( mGallery.getAdapter().getCount() > 1 )
								{
									mLoader = (WallpaperLoader)new WallpaperLoader().execute( position + 1 );
								}
								else
								{
									finish();
								}
							}
							else
							//如果大的预览图可以滑动
							{
								if( mGallery.getAdapter().getCount() <= localList.size() )//如果没有桌面内置壁纸情况
								{
									if( mGallery.getAdapter().getCount() <= 1 )//如果没有壁纸了
									{
										finish();
									}
									else
									{
										if( position == 0 )//如果没有桌面内置壁纸同时下载到本地的壁纸从第一张（position==0）开始删除
										{
											localList.remove( position );
											mLocalViewPagerAdapter.notifyDataSetChanged();
											mGallery.setSelection( position );
										}
										else if( position > 0 )//如果不是从第一张开始删除
										{
											localList.remove( position );
											mGallery.setSelection( 0 );
										}
									}
								}
								else
								{
									mGallery.setSelection( 0 );
								}
							}
							//<c_0000733> liuhailin@2014-08-12 modify begin
							isLoadComplete = false;
							//<c_0000733> liuhailin@2014-08-12 modify end
							//							if( mGallery.getAdapter().getCount() == 1 )
							//							{
							//								mGallery.setSelection( 0 );
							//							}
							//							else
							//							{
							//								if( mGallery.getAdapter().getCount() > mGallery.getSelectedItemPosition() )
							//								{
							//									mGallery.setSelection( mGallery.getSelectedItemPosition() );
							//								}
							//								else
							//								{
							//									mGallery.setSelection( mGallery.getSelectedItemPosition() - 1 );
							//								}
							//							}
						}
					}
				} );
				builder.setNegativeButton( R.string.delete_cancel , new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(
							DialogInterface dialog ,
							int which )
					{
						// TODO Auto-generated method stub
					}
				} );
				builder.create().show();
			}
		}
		else if( v.getId() == R.id.btnBuy )
		{
			//			WallpaperInformation information = (WallpaperInformation)mGallery.getSelectedItem();
			//			CooeePaymentInfo paymentInfo = new CooeePaymentInfo();
			//			paymentInfo.setPrice( information.getPrice() );
			//			paymentInfo.setPayDesc( information.getDisplayName() );
			//			String payid = information.getPricePoint();
			//			Log.v( "themebox" , "payid wallpaper = " + payid );
			//			if( payid == null || payid.equals( "" ) || payid.equals( "null" ) )
			//			{
			//				paymentInfo.setPayId( FunctionConfig.getCooeePayID( information.getPrice() ) );
			//			}
			//			else
			//			{
			//				paymentInfo.setPayId( payid );
			//			}
			//			paymentInfo.setCpId( information.getThirdparty() );
			//			paymentInfo.setPayName( information.getDisplayName() );
			//			paymentInfo.setPayType( CooeePaymentInfo.PAY_TYPE_EVERY_TIME );
			//			paymentInfo.setNotify( new PaymentResultReceiver() );
			//			CooeePayment.getInstance().startPayService( WallpaperPreviewActivity.this , paymentInfo );
		}
	}
	
	/**
	 *
	 * @author gaominghui 2015年9月1日
	 */
	@Override
	public void setLockWallpaper()
	{
		final ProgressDialog setLockWallpaperDialog = new ProgressDialog( mThemeContext );
		setLockWallpaperDialog.setSecondaryProgress( ProgressDialog.STYLE_SPINNER );
		setLockWallpaperDialog.setMessage( this.getResources().getString( R.string.changingWallpaper ) );
		setLockWallpaperDialog.setCancelable( false );
		setLockWallpaperDialog.show();
		String select = null;
		if( mGallery.getSelectedItem() instanceof WallpaperInformation )
		{
			select = ( (WallpaperInformation)mGallery.getSelectedItem() ).getPackageName();
		}
		else
		{
			select = mThumbs.get( mGallery.getSelectedItemPosition() ).replace( "_small" , "" );
		}
		final String path = select;
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				//					Bitmap newBitmap = PathTool.compressBitmap(
				//							( (BitmapDrawable)currentDrawable ).getBitmap() ,
				//							getResources().getDisplayMetrics().widthPixels ,
				//							getResources().getDisplayMetrics().heightPixels );
				boolean temp = false;
				final String time = String.valueOf( System.currentTimeMillis() );
				if( setLockWallpaperPath == null || setLockWallpaperPath.equals( "" ) )
				{
					temp = Tools.saveMyBitmap( "/data/data/com.iLoong.base.themebox/lockwallpapers" , ( (BitmapDrawable)currentDrawable ).getBitmap() );
				}
				else
				{
					if( brzh_set_desktop_lock_wallpaper )
					{
						temp = Tools.saveWallpaperBitmap( setLockWallpaperPath , "/lock" + time + ".png" , ( (BitmapDrawable)currentDrawable ).getBitmap() );
					}
					else
					{
						temp = Tools.saveMyBitmap( setLockWallpaperPath , ( (BitmapDrawable)currentDrawable ).getBitmap() );
					}
				}
				final boolean result = temp;
				setwallpaper.post( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						setLockWallpaperDialog.dismiss();
						if( result )
						{
							if( brzh_set_desktop_lock_wallpaper )
							{
								Settings.System.putString( mThemeContext.getContentResolver() , "keyguard_wallpaper" , setLockWallpaperPath + "/lock" + time + ".png" );
							}
							else
							{
								Toast.makeText( WallpaperPreviewActivity.this , R.string.toast_setwallpaper_success , Toast.LENGTH_SHORT ).show();
							}
						}
						else
						{
							Toast.makeText( WallpaperPreviewActivity.this , R.string.apply_fail , Toast.LENGTH_SHORT ).show();
						}
					}
				} );
			}
		} ).start();
	}
	
	/**
	 *
	 * @author gaominghui 2015年9月1日
	 */
	@Override
	public void setDesktopWallpaper()
	{
		WallpaperPreviewActivity.this.getSharedPreferences( "InfoToLauncher" , Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE ).edit().putInt( "index" , selectPositon ).commit();
		final ProgressDialog dialog = new ProgressDialog( this );
		dialog.setMessage( getString( R.string.changingWallpaper ) );
		dialog.setCancelable( false );
		dialog.show();
		String select = null;
		if( mGallery.getSelectedItem() instanceof WallpaperInformation )
		{
			select = ( (WallpaperInformation)mGallery.getSelectedItem() ).getPackageName();
		}
		else
		{
			select = mThumbs.get( mGallery.getSelectedItemPosition() ).replace( "_small" , "" );
		}
		if( isDesktopWall )
		{
			Intent it = new Intent( "com.coco.wallpaper.update" );
			it.putExtra( "isDesktopWall" , isDesktopWall );
			it.putExtra( "wallpaper" , select );
			sendBroadcast( it );
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					try
					{
						Thread.sleep( 200 );
					}
					catch( InterruptedException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if( mGallery.getSelectedItem() instanceof WallpaperInformation )
					{
						String path = null;
						if( appName != null )
						{
							path = PathTool.getAppFile( ( (WallpaperInformation)mGallery.getSelectedItem() ).getPackageName() + "#" + appName );
						}
						else
						{
							path = PathTool.getAppFile( ( (WallpaperInformation)mGallery.getSelectedItem() ).getPackageName() );
						}
						//Log.i( "jbc" , "infos.setWallpaperByPath( path ) path = "+path );
						infos.setWallpaperByPath( path );
					}
					else
					{
						if( wallpapers_from_other_apk != null )
						{
							try
							{
								infos.selsectWallpaper( ( (BitmapDrawable)currentDrawable ).getBitmap() );
							}
							catch( Exception e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
								Log.v( "WallpaperPreviewActivity" , "setdesktopwallpaper######" + e.getMessage() );
							}
						}
						else
						{
							infos.selectWallpaper( mGallery.getSelectedItemPosition() );
						}
					}
					setwallpaper.post( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							dialog.dismiss();
							// finish();
							if( !( mGallery.getSelectedItem() instanceof WallpaperInformation ) )
							{
								sendBroadcast( new Intent( "com.cooee.scene.wallpaper.change" ) );
							}
							Toast.makeText( WallpaperPreviewActivity.this , R.string.toast_setwallpaper_success , Toast.LENGTH_SHORT ).show();
							//<c_0001415> liuhailin@2014-10-24 modify begin
							if( movetaskback_after_setdeskwallpaper )
							{
								( (Activity)mThemeContext ).moveTaskToBack( true );
							}
							//<c_0001415> liuhailin@2014-10-24 modify end
						}
					} );
				}
			} ).start();
		}
		else
		{
			final String path = select;
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					boolean temp = false;
					// TODO Auto-generated method stub
					if( setLockWallpaperPath == null || setLockWallpaperPath.equals( "" ) )
					{
						temp = Tools.saveMyBitmap( "/data/data/com.iLoong.base.themebox/lockwallpapers" , ( (BitmapDrawable)currentDrawable ).getBitmap() );
					}
					else
					{
						temp = Tools.saveMyBitmap( setLockWallpaperPath , ( (BitmapDrawable)currentDrawable ).getBitmap() );
					}
					final boolean result = temp;
					setwallpaper.post( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							dialog.dismiss();
							if( result )
							{
								Intent it = new Intent( "com.coco.wallpaper.update" );
								it.putExtra( "isDesktopWall" , isDesktopWall );
								it.putExtra( "wallpaper" , path );
								sendBroadcast( it );
								Toast.makeText( WallpaperPreviewActivity.this , R.string.toast_setwallpaper_success , Toast.LENGTH_SHORT ).show();
							}
							else
							{
								Toast.makeText( WallpaperPreviewActivity.this , R.string.apply_fail , Toast.LENGTH_SHORT ).show();
							}
						}
					} );
				}
			} ).start();
		}
	}
	
	private void recursionDeleteFile(
			File file )
	{
		if( file.isFile() )
		{
			file.delete();
			return;
		}
		if( file.isDirectory() )
		{
			File[] childFile = file.listFiles();
			if( childFile == null )
			{
				return;
			}
			for( File f : childFile )
			{
				recursionDeleteFile( f );
			}
			file.delete();
		}
	}
	
	private void resetPreviewParams()
	{
		currentOtherApkWallpaperResName = null;
		currentWallpaperPath = null;
	}
	
	private void getCustomWallpaperDrawable(
			String str ,
			int type ,
			int index )
	{
		InputStream is = null;
		String path = null;
		if( useCustomWallpaper )
		{
			try
			{
				path = customWallpaperPath + "/" + str;
				is = new FileInputStream( path );
			}
			catch( FileNotFoundException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				Context remoteContext = mThemeContext.createPackageContext( ThemesDB.LAUNCHER_PACKAGENAME , Context.CONTEXT_IGNORE_SECURITY );
				AssetManager asset = remoteContext.getResources().getAssets();
				try
				{
					if( ThemesDB.LAUNCHER_PACKAGENAME.equals( "com.cooee.Mylauncher" ) || ThemesDB.LAUNCHER_PACKAGENAME.equals( "com.cooee.launcherS4" ) || ThemesDB.LAUNCHER_PACKAGENAME
							.equals( "com.cooee.launcherS4" ) || ThemesDB.LAUNCHER_PACKAGENAME.equals( "com.cooee.launcherS5" ) || ThemesDB.LAUNCHER_PACKAGENAME.equals( "com.cooee.launcherHS" ) )
					{
						wallpaperPath = "wallpapers";
					}
					path = wallpaperPath + "/" + str;
					is = asset.open( path );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
			catch( NameNotFoundException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if( is != null )
		{
			//Bitmap bitmap = BitmapFactory.decodeStream( is );
			// gaominghui@2016/10/09 ADD START
			preferences = mThemeContext.getSharedPreferences( "isRemove_enable_support_lockwallpaper_judge" , MODE_PRIVATE );
			boolean preResult = preferences.getBoolean( "custom_preview_wallpaper_for_OOM" , false );
			Drawable mDrawable = null;
			Log.i( "WallpaperPreviewActivity" , "getCustomWallpaperDrawable path = " + path );
			Log.i( "WallpaperPreviewActivity" , "getCustomWallpaperDrawable preResult = " + preResult );
			if( preResult )
			{
				mDrawable = decodeInPutStream( is );
				Log.i( "andy" , "mDrawable = " + mDrawable );
			}
			else
			{
				mDrawable = Drawable.createFromStream( is , "" );
			}
			// gaominghui@2016/10/09 ADD END
			if( type == 0 )
			{
				localBmp.add( mDrawable );//加载小缩略图
			}
			else
			{
				wallpaperMap.put( String.valueOf( index ) , mDrawable );
			}
			try
			{
				is.close();
			}
			catch( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	class WallpaperLoader extends AsyncTask<Integer , Void , Drawable>
	{
		
		WallpaperLoader()
		{
		}
		
		protected Drawable doInBackground(
				Integer ... params )
		{
			if( isCancelled() )
				return null;
			resetPreviewParams();
			try
			{
				InputStream is = null;
				if( params[0] < mThumbs.size() )
				{
					if( wallpapers_from_other_apk != null )
					{
						try
						{
							Context remountContext = mThemeContext.createPackageContext( wallpapers_from_other_apk , Context.CONTEXT_IGNORE_SECURITY );
							Resources res = remountContext.getResources();
							try
							{
								currentOtherApkWallpaperResName = mThumbs.get( params[0] );
								int drawable = res.getIdentifier( mThumbs.get( params[0] ) , "drawable" , wallpapers_from_other_apk );
								Drawable mDrawable = res.getDrawable( drawable );
								currentDrawable = mDrawable;
								if( isShowPreviewWallpaperByAdapter )
								{
									if( wallpaperMap.get( String.valueOf( params[0] ) ).equals( defaultDrawable ) )
									{
										wallpaperMap.put( String.valueOf( params[0] ) , mDrawable );
									}
								}
								return currentDrawable;
							}
							catch( Exception e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						catch( Exception e1 )
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					else
					{
						if( useCustomWallpaper )
						{
							try
							{
								currentWallpaperPath = customWallpaperPath + "/" + mThumbs.get( params[0] ).replace( "_small" , "" );
								is = new FileInputStream( customWallpaperPath + "/" + mThumbs.get( params[0] ).replace( "_small" , "" ) );
								Drawable mDrawable = Drawable.createFromStream( is , "" );
								currentDrawable = mDrawable;
								if( isShowPreviewWallpaperByAdapter )
								{
									if( wallpaperMap.get( String.valueOf( params[0] ) ).equals( defaultDrawable ) )
									{
										wallpaperMap.put( String.valueOf( params[0] ) , mDrawable );
									}
								}
							}
							catch( Exception e )
							{
								e.printStackTrace();
								return null;
							}
						}
						else
						{
							try
							{
								Context remoteContext = mThemeContext.createPackageContext( ThemesDB.LAUNCHER_PACKAGENAME , Context.CONTEXT_IGNORE_SECURITY );
								AssetManager asset = remoteContext.getResources().getAssets();
								try
								{
									if( ThemesDB.LAUNCHER_PACKAGENAME.equals( "com.cooee.Mylauncher" ) || ThemesDB.LAUNCHER_PACKAGENAME.equals( "com.cooee.launcherS4" ) || ThemesDB.LAUNCHER_PACKAGENAME
											.equals( "com.cooee.launcherS4" ) || ThemesDB.LAUNCHER_PACKAGENAME.equals( "com.cooee.launcherS5" ) || ThemesDB.LAUNCHER_PACKAGENAME
											.equals( "com.cooee.launcherHS" ) )
									{
										wallpaperPath = "wallpapers";
									}
									currentWallpaperPath = ( wallpaperPath + "/" + mThumbs.get( params[0] ).replace( "_small" , "" ) );
									is = asset.open( wallpaperPath + "/" + mThumbs.get( params[0] ).replace( "_small" , "" ) );
									Drawable mDrawable = Drawable.createFromStream( is , "" );
									currentDrawable = mDrawable;
									if( isShowPreviewWallpaperByAdapter )
									{
										if( wallpaperMap.get( String.valueOf( params[0] ) ).equals( defaultDrawable ) )
										{
											wallpaperMap.put( String.valueOf( params[0] ) , mDrawable );
										}
									}
								}
								catch( Exception e )
								{
									e.printStackTrace();
								}
							}
							catch( NameNotFoundException e1 )
							{
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
				}
				else
				{
					String pkg = ( (WallpaperInformation)localList.get( params[0] - mThumbs.size() ) ).getPackageName();
					try
					{
						currentWallpaperPath = PathTool.getAppFile( pkg );
						is = new FileInputStream( PathTool.getAppFile( pkg ) );
						Drawable mDrawable = Drawable.createFromStream( is , "" );
						currentDrawable = mDrawable;
					}
					catch( Exception e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if( is == null )
				{
					return null;
				}
				try
				{
					if( is != null )
					{
						is.close();
					}
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			return currentDrawable;
		}
		
		@Override
		protected void onPostExecute(
				Drawable isSuccess )
		{
			if( isShowPreviewWallpaperByAdapter && !isLoadComplete )
			{
				LoadLocalWallpaperThread();
				if( mLocalViewPagerAdapter != null )
				{
					mLocalViewPagerAdapter.notifyDataSetChanged();
				}
			}
			else if( !isShowPreviewWallpaperByAdapter )
			{
				mImageView.setImageDrawable( isSuccess );
			}
		}
		
		void cancel()
		{
			super.cancel( true );
		}
	}
	
	//	class PaymentResultReceiver implements CooeePaymentResultNotify
	//	{
	//		
	//		public void paymentResult(
	//				int resultCode ,
	//				CooeePaymentInfo paymentInfo )
	//		{
	//			switch( resultCode )
	//			{
	//				case CooeePaymentResultNotify.COOEE_PAYMENT_RESULT_SUCCESS:
	//					Toast.makeText( WallpaperPreviewActivity.this , "计费成功" , Toast.LENGTH_SHORT ).show();
	//					WallpaperInformation information = (WallpaperInformation)mGallery.getSelectedItem();
	//					Tools.writePurchasedData( WallpaperPreviewActivity.this , DownloadList.Wallpaper_Type , information.getPackageName() );
	//					// Message message = iapHandler.obtainMessage(BILLING_FINISH);
	//					// message.sendToTarget();
	//					Intent intent = new Intent( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED );
	//					intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , information.getPackageName() );
	//					sendBroadcast( intent );
	//					break;
	//				case CooeePaymentResultNotify.COOEE_PAYMENT_RESULT_FAIL:
	//					Toast.makeText( WallpaperPreviewActivity.this , "计费失败" + paymentInfo.getVersionName() , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case CooeePaymentResultNotify.COOEE_PAYMENT_RESULT_CANCEL_BY_USER:
	//					Toast.makeText( WallpaperPreviewActivity.this , "用户取消付费" , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case 1:
	//					Toast.makeText( WallpaperPreviewActivity.this , "配置免费" , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case 2:
	//					Toast.makeText( WallpaperPreviewActivity.this , "不需要重复计费" , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case 3:
	//					Toast.makeText( WallpaperPreviewActivity.this , "无可用指令" , Toast.LENGTH_SHORT ).show();
	//					break;
	//			}
	//		}
	//	}
	// gaominghui@2017/01/03 ADD STARThome键监听广播 点击home键退出进程，解决i_0014673
	private class CloseSystemDialogsReceiver extends BroadcastReceiver
	{
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			if( FunctionConfig.isUmengStatistics_key() )
			{
				MobclickAgent.onEvent( mThemeContext , "click_return" );
			}
			if( FunctionConfig.isExitSystemProgress() && !DownloadApkContentService.isDownloadingAPK && !MeGeneralMethod.IsDownloadTaskRunning( getApplicationContext() ) )
			{
				finish();
			}
			else
			{
				Log.v( "test" , "WallpaperPreview onKeyUp BackKeyPressed!!" );
				moveTaskToBack( true );
			}
		}
	}
	// gaominghui@2017/01/03 ADD END home键监听广播  点击home键退出进程，解决i_0014673
}
