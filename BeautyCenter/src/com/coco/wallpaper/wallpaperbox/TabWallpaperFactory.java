package com.coco.wallpaper.wallpaperbox;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coco.download.Assets;
import com.coco.download.DownloadList;
import com.coco.pub.provider.PubContentProvider;
import com.coco.pub.provider.PubProviderHelper;
import com.coco.theme.themebox.ContentFactory;
import com.coco.theme.themebox.DownloadApkContentService;
import com.coco.theme.themebox.PullToRefreshView;
import com.coco.theme.themebox.database.model.DownloadStatus;
import com.coco.theme.themebox.service.ThemesDB;
import com.coco.theme.themebox.util.DownModule;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Log;
import com.coco.theme.themebox.util.Tools;
import com.iLoong.base.themebox.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


public class TabWallpaperFactory implements ContentFactory
{
	
	/**
	 * 判断是否联网
	 */
	public boolean IsHaveInternet(
			final Context context )
	{
		try
		{
			ConnectivityManager manger = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
			NetworkInfo info = manger.getActiveNetworkInfo();
			return( info != null && info.isConnected() );
		}
		catch( Exception e )
		{
			return false;
		}
	}
	
	Context mContext;
	GridView gridviewLocal;
	private GridView gridViewHot;
	private GridView gridViewLive;
	private GridHotWallpaperAdapter hotAdapter;
	private GridLiveWallpaperAdapter liveAdapter;
	private ViewPager gridPager;
	private GridPagerAdapter themePagerAdapter;
	private DownModule downModule;
	private final int INDEX_LOCAL = 0;
	private final int INDEX_HOT = 1;
	private View hotView;
	private PullToRefreshView mPullToRefreshView;
	private boolean footerRefresh = false;
	private boolean headerRefresh = false;
	private Handler handler = new Handler();
	private boolean listRefresh = false;
	private static boolean interneterr = false;
	List<ResolveInfo> mResolveInfoList = new ArrayList<ResolveInfo>();
	private List<String> mThumbs = new ArrayList<String>( 24 );
	boolean useCustomWallpaper = false;
	private static String wallpaperPath = "launcher/wallpapers";
	private String[] galleryPkg;
	private String GALLERY = "";
	private final String livePkg[] = { "com.android.wallpaper.livepicker" , "com.android.wallpaper.multipicker" };
	private String LIVEPICKER = "com.android.wallpaper.livepicker";
	private final String UPDATA_CURRENT = "com.coco.wallpaper.update";
	String customWallpaperPath;
	GridLocalWallpaperAdapter mAdapter;
	ProgressDialog mDialog;
	private WallpaperInfo infos;
	private List<Bitmap> localBmp = new ArrayList<Bitmap>();
	private boolean isHWStyle = false;
	private ListView moreList = null;
	private boolean isChange = false;
	private boolean isDesktopWall = true;
	private RadioButton HotButton;
	private RadioButton LocalButton;
	private File sdcardTempFile;
	DisplayImageOptions options;
	private static boolean isEnableShowPreviewWallpaper = false;
	// @gaominghui 2014/12/11 ADD START 鼎智需求添加设置视频壁纸的包名
	private final String VIDEO_WALLPAPER = "com.broadcom.btk.videowallpaper";//视频壁纸的包名
	
	// @gaominghui 2014/12/11 ADD END
	//private String photoName = null;
	@Override
	public void changeTab(
			int tab )
	{
		// TODO Auto-generated method stub
		if( tab == INDEX_LOCAL )
		{
			if( LocalButton != null && LocalButton.getVisibility() == View.VISIBLE )
			{
				LocalButton.setChecked( true );
				gridPager.setCurrentItem( INDEX_LOCAL , false );
			}
		}
		else
		{
			if( HotButton != null && HotButton.getVisibility() == View.VISIBLE )
			{
				HotButton.setChecked( true );
				gridPager.setCurrentItem( INDEX_HOT , false );
			}
		}
	}
	
	@Override
	public void reloadView()
	{
		if( isHWStyle )
		{
			return;
		}
		else
		{
			LocalButton.setText( R.string.text_lcoal_Wallpapers );
		}
	}
	
	public TabWallpaperFactory(
			Context context ,
			DownModule module )
	{
		// TODO Auto-generated constructor stub
		mContext = context;
		downModule = module;
		isEnableShowPreviewWallpaper = FunctionConfig.isEnableShowPreviewWallpaper();
		//add by liuhailin begin
		options = new DisplayImageOptions.Builder().showStubImage( R.drawable.default_img ).showImageForEmptyUri( R.drawable.default_img ).showImageOnFail( R.drawable.default_img )
				.cacheInMemory( true ).cacheOnDisc( true ).bitmapConfig( Bitmap.Config.RGB_565 ).build();
		//add by liuhailin end
		PathTool.makeDirApp();
	}
	
	public void setHWStyle(
			boolean isHW ,
			boolean isDesktop )
	{
		isHWStyle = isHW;
		isDesktopWall = isDesktop;
	}
	
	@Override
	public View createTabContent(
			String tag )
	{
		// TODO Auto-generated method stub
		View result;
		if( isHWStyle )
		{
			result = View.inflate( mContext , R.layout.hw_wallpaper_main , null );
		}
		else
		{
			result = View.inflate( mContext , R.layout.theme_main , null );
			HotButton = (RadioButton)result.findViewById( R.id.btnHotTheme );
			LocalButton = (RadioButton)result.findViewById( R.id.btnLocalTheme );
			LocalButton.setText( R.string.text_lcoal_Wallpapers );
		}
		infos = new WallpaperInfo( mContext );
		View localView = View.inflate( mContext , R.layout.hw_wallpaper_grid_lcoal , null );
		gridviewLocal = (GridView)localView.findViewById( R.id.gridlocal );//( View.inflate( mContext , R.layout.lock_grid , null ) );
		RadioGroup radioGroup = (RadioGroup)localView.findViewById( R.id.wallpaperradioGroup );
		if( !isHWStyle )
		{
			localView.findViewById( R.id.spitView ).setVisibility( View.GONE );
			localView.findViewById( R.id.spitline ).setVisibility( View.GONE );
			radioGroup.setVisibility( View.GONE );
			gridviewLocal.setNumColumns( 2 );
			gridviewLocal.setPadding( 0 , 0 , 0 , 0 );
			gridviewLocal.setHorizontalSpacing( 0 );
			gridviewLocal.setVerticalSpacing( 0 );
			gridviewLocal.setColumnWidth( infos.getScreenWidth() / 2 );
		}
		else
		{
			if( isDesktopWall )
			{
				radioGroup.setVisibility( View.VISIBLE );
			}
			else
			{
				radioGroup.setVisibility( View.GONE );
			}
			final RadioButton local = (RadioButton)localView.findViewById( R.id.radiolocal );
			RadioButton live = (RadioButton)localView.findViewById( R.id.radiolocallive );
			final RadioButton more = (RadioButton)localView.findViewById( R.id.radiomore );
			moreList = (ListView)localView.findViewById( R.id.listMore );
			moreList.setAdapter( new MoreListAdapter() );
			moreList.setOnItemClickListener( new OnItemClickListener() {
				
				@Override
				public void onItemClick(
						AdapterView<?> parent ,
						View view ,
						int position ,
						long id )
				{
					// TODO Auto-generated method stub
					ResolveInfo resolveInfo = (ResolveInfo)parent.getItemAtPosition( position );
					ComponentName mComponentName = new ComponentName( resolveInfo.activityInfo.packageName , resolveInfo.activityInfo.name );
					Intent intent = new Intent( Intent.ACTION_SET_WALLPAPER );
					intent.setComponent( mComponentName );
					( (Activity)mContext ).startActivity( intent );
				}
			} );
			live.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					PackageManager pm = mContext.getPackageManager();
					Intent pickWallpaper = new Intent( Intent.ACTION_SET_WALLPAPER );
					List<ResolveInfo> infos = pm.queryIntentActivities( pickWallpaper , PackageManager.GET_ACTIVITIES );
					for( ResolveInfo info : infos )
					{
						if( info.activityInfo.packageName.equals( LIVEPICKER ) )
						{
							String cls = info.activityInfo.name;
							Intent it = new Intent( Intent.ACTION_SET_WALLPAPER );
							ComponentName comp = new ComponentName( LIVEPICKER , cls );
							it.setComponent( comp );
							( (Activity)mContext ).startActivity( it );
						}
					}
					if( gridviewLocal.getVisibility() == View.VISIBLE )
					{
						local.setChecked( true );
					}
					if( moreList.getVisibility() == View.VISIBLE )
					{
						more.setChecked( true );
					}
				}
			} );
			local.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					gridviewLocal.setVisibility( View.VISIBLE );
					moreList.setVisibility( View.GONE );
				}
			} );
			more.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					gridviewLocal.setVisibility( View.GONE );
					moreList.setVisibility( View.VISIBLE );
				}
			} );
		}
		mDialog = new ProgressDialog( mContext );
		mAdapter = new GridLocalWallpaperAdapter( mContext , downModule );
		gridviewLocal.setAdapter( mAdapter );
		gridviewLocal.setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				// TODO Auto-generated method stub
				if( position < mResolveInfoList.size() )
				{
					sdcardTempFile = new File( PathTool.getClipFilePath() );
					int requestCode = 0;
					ResolveInfo resolveInfo = mResolveInfoList.get( position );
					ComponentName mComponentName = new ComponentName( resolveInfo.activityInfo.packageName , resolveInfo.activityInfo.name );
					/*Log.i( "sss" , "resolveInfo.activityInfo.packageName = " + resolveInfo.activityInfo.packageName );
					Log.i( "sss" , "GALLERY = " + GALLERY );*/
					Intent intent = new Intent();
					if( GALLERY.equals( resolveInfo.activityInfo.packageName ) )
					{
						requestCode = 2000;
						//<c_0000796> liuhailin@2014-08-19 modify begin
						WallpaperManager wallpaperManager = WallpaperManager.getInstance( mContext );
						//Bitmap wallpaperBitmap = ( (BitmapDrawable)( wallpaperManager.getDrawable() ) ).getBitmap();
						int height = wallpaperManager.getDesiredMinimumHeight();
						int width = wallpaperManager.getDesiredMinimumWidth();
						Log.i( "TabWallpaperFactory" , "height = " + height );
						Log.i( "TabWallpaperFactory" , "width = " + width );
						//<c_0000796> liuhailin@2014-08-19 modify end
						Log.i( "TabWallpaperFactory" , "galleryClip = " + FunctionConfig.isEnableSetwallpaperByGalleryClip() );
						if( FunctionConfig.isEnableSetwallpaperByGalleryClip() )
						{
							intent.setAction( Intent.ACTION_PICK );
							intent.setPackage( GALLERY );
							Log.i( "TabWallpaperFactory" , "setPackage = " + GALLERY );
							intent.setDataAndType( MediaStore.Images.Media.INTERNAL_CONTENT_URI , "image/*" );
							intent.putExtra( "output" , Uri.fromFile( sdcardTempFile ) );
							intent.putExtra( "crop" , "true" );
							//<c_0000796> liuhailin@2014-08-19 modify begin
							/*if(photoName.equals( "com.google.android.apps.plus" )){
								requestCode = 2002;
							}else*/{
								Log.i( "TabWallpaperFactory" , "isEnableWallpaperClipByScale = " + FunctionConfig.isEnableWallpaperClipByScale() );
								if( FunctionConfig.isEnableWallpaperClipByScale() )
								{
									intent.putExtra( "aspectX" , width );// 裁剪框比例
									intent.putExtra( "aspectY" , height );
									intent.putExtra( "outputX" , width );
									intent.putExtra( "outputY" , height );
									intent.putExtra( "noFaceDetection" , true );
								}
								else
								{
									// gaominghui@2017/05/09 ADD START 智科需求0004684：美化中心里通过图库设置壁纸,在裁剪时的裁剪比例是否根据系统图库的比例来裁剪（该开关在enable_wallpaper_clip_by_systemwallpaper_scale关掉后设置才有效）；
									if( !FunctionConfig.isEnable_wallpaper_clip_by_systemgallery() )
									{
										intent.putExtra( "aspectX" , 1 );// 裁剪框比例
										intent.putExtra( "aspectY" , 1 );
									}
									// gaominghui@2017/05/09 ADD END 智科需求0004684：美化中心里通过图库设置壁纸,在裁剪时的裁剪比例是否根据系统图库的比例来裁剪（该开关在enable_wallpaper_clip_by_systemwallpaper_scale关掉后设置才有效）；
								}
							}
							//<c_0000796> liuhailin@2014-08-19 modify end
						}
						else
						{
							intent.setComponent( mComponentName );
							intent.setAction( Intent.ACTION_SET_WALLPAPER );
						}
					}
					else if( LIVEPICKER.equals( resolveInfo.activityInfo.packageName ) )
					{
						intent.setComponent( mComponentName );
						intent.setAction( Intent.ACTION_SET_WALLPAPER );
						requestCode = 2001;
					}
					else if( VIDEO_WALLPAPER.equals( resolveInfo.activityInfo.packageName ) )
					{
						if( FunctionConfig.isEnableShowVideoWallpaper() )
						{
							intent.setComponent( mComponentName );
							mContext.startActivity( intent );
							return;
						}
					}
					else if( "com.coco.wallpaper.wallpaperbox.WallpaperPreviewActivity".equals( resolveInfo.activityInfo.name ) )
					{
						intent.setComponent( mComponentName );
						intent.setAction( Intent.ACTION_SET_WALLPAPER );
						intent.setClass( mContext , WallpaperPreviewActivity.class );
						intent.putExtra( "type" , "local" );
						intent.putExtra( "position" , position );
						intent.putExtra( "showpreviewwallpaperbyadapter" , FunctionConfig.isEnablePreviewWallpaperByAdapter() );
						intent.putExtra( "showpreviewbtn" , isEnableShowPreviewWallpaper );
						intent.putExtra( "showapplylockbtn" , FunctionConfig.isEnableShowApplyLockWallpaper() );
						intent.putExtra( "lockwallpaperpath" , FunctionConfig.getLockWallpaperPath() );
						intent.putExtra( "currentLauncherPackageName" , ThemesDB.LAUNCHER_PACKAGENAME );
						intent.putExtra( "currentLauncherProvider" , PubContentProvider.LAUNCHER_AUTHORITY );
						intent.putExtra( "enable_delete_current_wallpaper" , FunctionConfig.isEnableDeleteCurrentDeskWallpaper() );
						intent.putExtra( "buttonsize" , mResolveInfoList.size() );
						intent.putExtra( "customWallpaperPath" , FunctionConfig.getCustomWallpaperPath() );
						intent.putExtra( "disableSetWallpaperDimensions" , FunctionConfig.getDisableSetWallpaperDimensions() );
						intent.putExtra( "fromotherapk" , FunctionConfig.getWallpapers_from_other_apk() );
						intent.putExtra( "launchername" , ThemesDB.LAUNCHER_PACKAGENAME );
						intent.putExtra( "langyitong_wallpaper_set" , FunctionConfig.isLangyitong_wallpaper_set() );
						intent.putExtra( "ismovetaskback" , FunctionConfig.isEnableMoveTaskBackAfterSetDeskWallpaper() );
						intent.putExtra( "isDesktopWall" , isDesktopWall );
						intent.putExtra( "isUmengStatistics_key" , FunctionConfig.isUmengStatistics_key() );
						// @gaominghui2015/09/01 ADD START
						intent.putExtra( "isEnable_apply_desktopwallpaper_lockwallpaper" , FunctionConfig.isEnable_apply_desktopwallpaper_lockwallpaper() );
						// @gaominghui2015/09/01 ADD END
					}
					else if( "com.coco.wallpaper.wallpaperbox.LockWallpaperPreview".equals( resolveInfo.activityInfo.name ) )
					{
						// gaominghui@2016/12/09 ADD START 晨想需求 0004580: lockwallpaper_icon_show，壁纸页面，是否显示锁屏的icon，这个开关打开之后，客户要求进入他们的模块
						if( FunctionConfig.isCx_lockwallpaper_show() )
						{
							ComponentName cxComponentName = new ComponentName( "com.magcomm.lockwallpapers" , "com.magcomm.lockwallpapers.LockWallpapersActivity" );
							intent.setComponent( cxComponentName );
						}
						else
						{
							intent.setComponent( mComponentName );
						}
						// gaominghui@2016/12/09 ADD END 晨想需求 0004580: lockwallpaper_icon_show，壁纸页面，是否显示锁屏的icon，这个开关打开之后，客户要求进入他们的模块
					}
					( (Activity)mContext ).startActivityForResult( intent , requestCode );
				}
				else
				{
					Intent it = new Intent();
					it.setClass( mContext , WallpaperPreviewActivity.class );
					it.putExtra( "type" , "local" );
					it.putExtra( "position" , position );
					it.putExtra( "showpreviewwallpaperbyadapter" , FunctionConfig.isEnablePreviewWallpaperByAdapter() );
					it.putExtra( "showpreviewbtn" , isEnableShowPreviewWallpaper );
					it.putExtra( "showapplylockbtn" , FunctionConfig.isEnableShowApplyLockWallpaper() );
					it.putExtra( "lockwallpaperpath" , FunctionConfig.getLockWallpaperPath() );
					it.putExtra( "currentLauncherPackageName" , ThemesDB.LAUNCHER_PACKAGENAME );
					it.putExtra( "currentLauncherProvider" , PubContentProvider.LAUNCHER_AUTHORITY );
					it.putExtra( "enable_delete_current_wallpaper" , FunctionConfig.isEnableDeleteCurrentDeskWallpaper() );
					it.putExtra( "buttonsize" , mResolveInfoList.size() );
					it.putExtra( "customWallpaperPath" , FunctionConfig.getCustomWallpaperPath() );
					it.putExtra( "disableSetWallpaperDimensions" , FunctionConfig.getDisableSetWallpaperDimensions() );
					it.putExtra( "fromotherapk" , FunctionConfig.getWallpapers_from_other_apk() );
					it.putExtra( "launchername" , ThemesDB.LAUNCHER_PACKAGENAME );
					it.putExtra( "langyitong_wallpaper_set" , FunctionConfig.isLangyitong_wallpaper_set() );
					it.putExtra( "ismovetaskback" , FunctionConfig.isEnableMoveTaskBackAfterSetDeskWallpaper() );
					it.putExtra( "isDesktopWall" , isDesktopWall );
					it.putExtra( "isUmengStatistics_key" , FunctionConfig.isUmengStatistics_key() );
					// @gaominghui2015/09/01 ADD START
					it.putExtra( "isEnable_apply_desktopwallpaper_lockwallpaper" , FunctionConfig.isEnable_apply_desktopwallpaper_lockwallpaper() );
					// @gaominghui2015/09/01 ADD END
					mContext.startActivity( it );
				}
			}
		} );
		packageReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(
					Context context ,
					Intent intent )
			{
				String actionName = intent.getAction();
				if( actionName.equals( UPDATA_CURRENT ) )
				{
					if( intent.getStringExtra( "wallpaper" ) != null )
					{
						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( mContext );
						if( isDesktopWall )
						{
							pref.edit().putString( "currentWallpaper" , intent.getStringExtra( "wallpaper" ) ).commit();
							pref.edit().putBoolean( "cooeechange" , true ).commit();
							PubProviderHelper.addOrUpdateValue( PubContentProvider.LAUNCHER_AUTHORITY , "wallpaper" , "currentWallpaper" , intent.getStringExtra( "wallpaper" ) );
							PubProviderHelper.addOrUpdateValue( PubContentProvider.LAUNCHER_AUTHORITY , "wallpaper" , "cooeechange" , "true" );
							//String temp = PubProviderHelper.queryValue( "wallpaper" ,  "cooeechange" );
							//Log.v("TabWallpaperFactory" ,"temp = "+temp);
							Log.v(
									"TabWallpaperFactory" ,
									"pref onclick " + pref.getBoolean( "cooeechange" , false ) + intent.getStringExtra( "wallpaper" ) + "/" + PubContentProvider.LAUNCHER_AUTHORITY );
							isChange = true;
						}
						else
						{
							pref.edit().putString( "lockWallpaper" , intent.getStringExtra( "wallpaper" ) ).commit();
						}
					}
					mAdapter.notifyDataSetChanged();
				}
				else if( actionName.equals( Intent.ACTION_WALLPAPER_CHANGED ) )
				{
					Log.v( "TabWallpaperFactory" , "ACTION_WALLPAPER_CHANGED" );
					if( !isChange )
					{
						new Thread( new Runnable() {
							
							public void run()
							{
								while( true )
								{
									String cooeechange = Assets.getWallpaper( mContext , "cooeechange" );
									if( ( cooeechange != null ) && ( !cooeechange.equals( "true" ) ) )
									{
										break;
									}
								}
								PubProviderHelper.addOrUpdateValue( PubContentProvider.LAUNCHER_AUTHORITY , "wallpaper" , "cooeechange" , "true" );
							}
						} ).start();
					}
					Log.v( "TabWallpaperFactory" , "onreceive ----isChange = " + isChange );
					isChange = false;
				}
				else if( actionName.equals( StaticClass.ACTION_THUMB_CHANGED ) )
				{
					if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() )
					{
						hotAdapter.updateThumb( intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ) );
					}
				}
				else if( actionName.equals( StaticClass.ACTION_HOTLIST_CHANGED ) )
				{
					Log.v( "TabWallpaperFactory" , "222222" + StaticClass.ACTION_HOTLIST_CHANGED );
					if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() )
					{
						hotAdapter.reloadPackage();
						hotAdapter.setShowProgress( false );
					}
					if( FunctionConfig.isLiveWallpaperShow() )
					{
						liveAdapter.reloadPackage();
						liveAdapter.setShowProgress( false );
					}
					themePagerAdapter.notifyDataSetChanged();
					if( listRefresh )
					{
						listRefresh = false;
					}
					if( footerRefresh )
					{
						mPullToRefreshView.onFooterRefreshComplete();
						footerRefresh = false;
					}
					if( headerRefresh )
					{
						mPullToRefreshView.onHeaderRefreshComplete();
						headerRefresh = false;
					}
				}
				else if( actionName.equals( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED ) )
				{
					mAdapter.reloadPackage();
					if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() )
					{
						hotAdapter.reloadPackage();
					}
				}
				else if( actionName.equals( StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED ) )
				{
					if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() )
					{
						hotAdapter.updateDownloadSize(
								intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ) ,
								intent.getIntExtra( StaticClass.EXTRA_DOWNLOAD_SIZE , 0 ) ,
								intent.getIntExtra( StaticClass.EXTRA_TOTAL_SIZE , 0 ) );
					}
				}
				else if( actionName.equals( StaticClass.ACTION_START_DOWNLOAD_APK ) )
				{
					String curdownApkname = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
					// downModule.downloadApk(curdownApkname);
					Intent it = new Intent();
					it.putExtra( "name" , intent.getStringExtra( "apkname" ) );
					// @gaominghui 2015/04/02 UPD START添加壁纸英文名字
					it.putExtra( "name_en" , intent.getStringExtra( "apkEnglishName" ) );
					// @gaominghui 2015/04/02 UPD END
					it.putExtra( "packageName" , curdownApkname );
					it.putExtra( "type" , DownloadList.Wallpaper_Type );
					it.putExtra( "status" , "download" );
					//<c_0000707> liuhailin@2014-08-11 modify begin
					it.putExtra( "position" , intent.getIntExtra( "position" , 0 ) );
					it.putExtra( "showpreviewbtn" , isEnableShowPreviewWallpaper );
					//<c_0000707> liuhailin@2014-08-11 modify end
					it.setClass( mContext , DownloadApkContentService.class );
					mContext.startService( it );
					if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() )
					{
						hotAdapter.notifyDataSetChanged();
					}
				}
				else if( actionName.equals( StaticClass.ACTION_PAUSE_DOWNLOAD_APK ) )
				{
					String packName = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
					// downModule.stopDownApk(packName);
					Intent it = new Intent();
					it.putExtra( "packageName" , packName );
					it.putExtra( "type" , DownloadList.Wallpaper_Type );
					it.putExtra( "name" , intent.getStringExtra( "apkname" ) );
					// @gaominghui 2015/04/02 UPD START添加壁纸英文名字
					it.putExtra( "name_en" , intent.getStringExtra( "apkEnglishName" ) );
					// @gaominghui 2015/04/02 UPD END
					it.putExtra( "status" , "pause" );
					it.setClass( mContext , DownloadApkContentService.class );
					mContext.startService( it );
					if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() )
					{
						hotAdapter.notifyDataSetChanged();
					}
					Log.v( "TabWallpaperFactory" , "receive packName = " + packName );
				}
				else if( actionName.equals( StaticClass.ACTION_LIVE_THUMB_CHANGED ) )
				{
					if( FunctionConfig.isLiveWallpaperShow() )
					{
						liveAdapter.updateThumb( intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ) );
					}
				}
				else if( actionName.equals( StaticClass.ACTION_LIVE_DOWNLOAD_STATUS_CHANGED ) )
				{
					if( FunctionConfig.isLiveWallpaperShow() )
					{
						liveAdapter.reloadPackage();
					}
				}
				else if( actionName.equals( StaticClass.ACTION_LIVE_DOWNLOAD_SIZE_CHANGED ) )
				{
					if( FunctionConfig.isLiveWallpaperShow() )
					{
						liveAdapter.updateDownloadSize(
								intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ) ,
								intent.getIntExtra( StaticClass.EXTRA_DOWNLOAD_SIZE , 0 ) ,
								intent.getIntExtra( StaticClass.EXTRA_TOTAL_SIZE , 0 ) );
					}
				}
				else if( actionName.equals( StaticClass.ACTION_LIVE_START_DOWNLOAD_APK ) )
				{
					if( FunctionConfig.isLiveWallpaperShow() )
					{
						String curdownApkname = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
						// downModule.downloadApk(curdownApkname);
						Intent it = new Intent();
						it.putExtra( "name" , intent.getStringExtra( "apkname" ) );
						// @gaominghui 2015/04/02 UPD START添加壁纸英文名字
						it.putExtra( "name_en" , intent.getStringExtra( "apkEnglishName" ) );
						// @gaominghui 2015/04/02 UPD END
						it.putExtra( "packageName" , curdownApkname );
						it.putExtra( "type" , DownloadList.LiveWallpaper_Type );
						it.putExtra( "status" , "download" );
						//<c_0000707> liuhailin@2014-08-11 modify begin
						it.putExtra( "position" , intent.getIntExtra( "position" , 0 ) );
						Log.v( "TabWallpaperFactory" , "live position = " + intent.getIntExtra( "position" , 0 ) );
						//<c_0000707> liuhailin@2014-08-11 modify end
						it.setClass( mContext , DownloadApkContentService.class );
						mContext.startService( it );
						liveAdapter.notifyDataSetChanged();
					}
				}
				else if( actionName.equals( StaticClass.ACTION_LIVE_PAUSE_DOWNLOAD_APK ) )
				{
					if( FunctionConfig.isLiveWallpaperShow() )
					{
						String packName = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
						// downModule.stopDownApk(packName);
						Intent it = new Intent();
						it.putExtra( "packageName" , packName );
						it.putExtra( "type" , DownloadList.LiveWallpaper_Type );
						it.putExtra( "name" , intent.getStringExtra( "apkname" ) );
						// @gaominghui 2015/04/02 UPD START添加壁纸英文名字
						it.putExtra( "name_en" , intent.getStringExtra( "apkEnglishName" ) );
						// @gaominghui 2015/04/02 UPD END
						it.putExtra( "status" , "pause" );
						it.setClass( mContext , DownloadApkContentService.class );
						mContext.startService( it );
						liveAdapter.notifyDataSetChanged();
					}
				}
				else if( Intent.ACTION_PACKAGE_REMOVED.equals( actionName ) )
				{
					if( FunctionConfig.isLiveWallpaperShow() )
					{
						String packageName = intent.getData().getSchemeSpecificPart();
						if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() )
						{
							if( liveAdapter.findPackageIndex( packageName ) >= 0 )
								liveAdapter.reloadPackage();
						}
					}
				}
				else if( Intent.ACTION_PACKAGE_ADDED.equals( actionName ) )
				{
					if( FunctionConfig.isLiveWallpaperShow() )
					{
						String packageName = intent.getData().getSchemeSpecificPart();
						if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() )
						{
							if( liveAdapter.findPackageIndex( packageName ) >= 0 )
								liveAdapter.reloadPackage();
						}
					}
				}
			}
		};
		if( FunctionConfig.isShowHotWallpaper() || FunctionConfig.isLiveWallpaperShow() )
		{
			if( isHWStyle )
			{
				hotView = View.inflate( mContext , R.layout.hw_wallpaper_grid_hot , null );
				if( !isDesktopWall )
				{
					hotView.findViewById( R.id.wallpaperradioGroup ).setVisibility( View.GONE );
				}
			}
			else
			{
				hotView = View.inflate( mContext , R.layout.wallpaper_grid_hot , null );
			}
			mPullToRefreshView = (PullToRefreshView)hotView.findViewById( R.id.wallpaper_refresh );
			gridViewHot = (GridView)hotView.findViewById( R.id.gridStatic );
			gridViewLive = (GridView)hotView.findViewById( R.id.gridLive );
			if( FunctionConfig.isBrzh_setWaitBackgroundView() )
			{
				gridViewHot.setBackgroundResource( R.drawable.brzh_viewpager_bg );
				gridViewLive.setBackgroundResource( R.drawable.brzh_viewpager_bg );
			}
			gridPager = (ViewPager)result.findViewById( R.id.themeGridPager );
			if( FunctionConfig.isBrzh_setWaitBackgroundView() )
			{
				gridPager.setBackgroundResource( R.drawable.brzh_viewpager_bg );
			}
			if( !( FunctionConfig.isShowHotWallpaper() && FunctionConfig.isLiveWallpaperShow() ) )
			{
				hotView.findViewById( R.id.wallpaperradioGroup ).setVisibility( View.GONE );
				if( FunctionConfig.isShowHotWallpaper() )
				{
					gridViewHot.setVisibility( View.VISIBLE );
					gridViewLive.setVisibility( View.GONE );
				}
				if( FunctionConfig.isLiveWallpaperShow() )
				{
					gridViewHot.setVisibility( View.GONE );
					gridViewLive.setVisibility( View.VISIBLE );
				}
			}
			else
			{
				RadioButton staticwallpaper = (RadioButton)hotView.findViewById( R.id.radioStatic );
				RadioButton livewallpaper = (RadioButton)hotView.findViewById( R.id.radioLive );
				staticwallpaper.setOnClickListener( new View.OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						// TODO Auto-generated method stub
						gridViewHot.setVisibility( View.VISIBLE );
						gridViewLive.setVisibility( View.GONE );
						mPullToRefreshView.initContentAdapterView();
					}
				} );
				livewallpaper.setOnClickListener( new View.OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						// TODO Auto-generated method stub
						gridViewHot.setVisibility( View.GONE );
						gridViewLive.setVisibility( View.VISIBLE );
						mPullToRefreshView.initContentAdapterView();
					}
				} );
			}
			if( FunctionConfig.isShowHotWallpaper() )
			{
				gridViewHot.setColumnWidth( infos.getScreenWidth() / 2 );
				hotAdapter = new GridHotWallpaperAdapter( mContext , downModule );
				gridViewHot.setAdapter( hotAdapter );
				if( hotAdapter.showProgress() || downModule.isRefreshList() )
				{
					downModule.downloadList();
					if( IsHaveInternet( mContext ) )
					{
						interneterr = false;
						listRefresh = true;
						handler.postDelayed( new Runnable() {
							
							@Override
							public void run()
							{
								downModule.stopDownlist();
								if( listRefresh )
								{
									if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
									{
										if( gridPager.getCurrentItem() == INDEX_HOT )
											Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
									}
								}
								if( themePagerAdapter != null && themePagerAdapter.viewDownloading != null )
								{
									themePagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
								}
							}
						} , 1000 * 30 );
					}
					else
					{
						if( gridPager.getCurrentItem() == INDEX_HOT )
							Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
						interneterr = true;
						if( themePagerAdapter != null && themePagerAdapter.viewDownloading != null )
						{
							themePagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
						}
					}
				}
				gridViewHot.setOnItemClickListener( new OnItemClickListener() {
					
					@Override
					public void onItemClick(
							AdapterView<?> parent ,
							View v ,
							int position ,
							long id )
					{
						WallpaperInformation infor = (WallpaperInformation)parent.getItemAtPosition( position );
						Intent i = new Intent();
						i.putExtra( "type" , "hot" );
						i.putExtra( StaticClass.EXTRA_PACKAGE_NAME , infor.getPackageName() );
						i.putExtra( "position" , position );
						i.putExtra( "showpreviewwallpaperbyadapter" , FunctionConfig.isEnablePreviewWallpaperByAdapter() );
						i.putExtra( "showpreviewbtn" , isEnableShowPreviewWallpaper );
						i.putExtra( "showapplylockbtn" , FunctionConfig.isEnableShowApplyLockWallpaper() );
						i.putExtra( "lockwallpaperpath" , FunctionConfig.getLockWallpaperPath() );
						i.putExtra( "currentLauncherPackageName" , ThemesDB.LAUNCHER_PACKAGENAME );
						i.putExtra( "currentLauncherProvider" , PubContentProvider.LAUNCHER_AUTHORITY );
						i.putExtra( "enable_delete_current_wallpaper" , FunctionConfig.isEnableDeleteCurrentDeskWallpaper() );
						i.putExtra( "enable_show_video_wallpaper" , FunctionConfig.isEnableShowVideoWallpaper() );
						i.putExtra( "isUmengStatistics_key" , FunctionConfig.isUmengStatistics_key() );
						i.setClass( mContext , WallpaperPreviewActivity.class );
						i.putExtra( "isPriceVisible" , FunctionConfig.isPriceVisible() );
						i.putExtra( "ismovetaskback" , FunctionConfig.isEnableMoveTaskBackAfterSetDeskWallpaper() );
						// @gaominghui2015/09/01 ADD START
						i.putExtra( "isEnable_apply_desktopwallpaper_lockwallpaper" , FunctionConfig.isEnable_apply_desktopwallpaper_lockwallpaper() );
						// @gaominghui2015/09/01 ADD END
						mContext.startActivity( i );
					}
				} );
			}
			if( FunctionConfig.isLiveWallpaperShow() )
			{
				gridViewLive.setColumnWidth( infos.getScreenWidth() / 2 );
				liveAdapter = new GridLiveWallpaperAdapter( mContext );
				gridViewLive.setAdapter( liveAdapter );
				if( liveAdapter.showProgress() || liveAdapter.getDownModule().isRefreshList() )
				{
					liveAdapter.getDownModule().downloadList();
					if( IsHaveInternet( mContext ) )
					{
						interneterr = false;
						listRefresh = true;
						handler.postDelayed( new Runnable() {
							
							@Override
							public void run()
							{
								liveAdapter.getDownModule().stopDownlist();
								if( listRefresh )
								{
									if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
									{
										if( gridPager.getCurrentItem() == INDEX_HOT )
											Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
									}
								}
								if( themePagerAdapter != null && themePagerAdapter.viewDownloading != null )
								{
									themePagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
								}
							}
						} , 1000 * 30 );
					}
					else
					{
						if( gridPager.getCurrentItem() == INDEX_HOT )
							Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
						interneterr = true;
						if( themePagerAdapter != null && themePagerAdapter.viewDownloading != null )
						{
							themePagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
						}
					}
				}
				gridViewLive.setOnItemClickListener( new OnItemClickListener() {
					
					@Override
					public void onItemClick(
							AdapterView<?> parent ,
							View view ,
							int position ,
							long id )
					{
						// TODO Auto-generated method stub
						WallpaperInformation infor = (WallpaperInformation)parent.getItemAtPosition( position );
						Intent i = new Intent();
						i.putExtra( StaticClass.EXTRA_PACKAGE_NAME , infor.getPackageName() );
						i.putExtra( "position" , position );
						i.setClass( mContext , LiveWallpaperPreviewActivity.class );
						i.putExtra( "isPriceVisible" , FunctionConfig.isPriceVisible() );
						mContext.startActivity( i );
					}
				} );
			}
			mPullToRefreshView.setOnHeaderRefreshListener( this );
			mPullToRefreshView.setOnFooterRefreshListener( this );
			// ViewPager
			themePagerAdapter = new GridPagerAdapter( localView , hotView );
			if( FunctionConfig.isShowHotWallpaper() )
				themePagerAdapter.setGridView( gridViewHot );
			else if( FunctionConfig.isLiveWallpaperShow() )
				themePagerAdapter.setGridView( gridViewLive );
			gridPager.setAdapter( themePagerAdapter );
			gridPager.setOverScrollMode( View.OVER_SCROLL_NEVER );
			// 热门按钮
			final RadioButton hotButton = (RadioButton)result.findViewById( R.id.btnHotTheme );
			final RadioButton localButton = (RadioButton)result.findViewById( R.id.btnLocalTheme );
			gridPager.setOnPageChangeListener( new OnPageChangeListener() {
				
				@Override
				public void onPageScrollStateChanged(
						int arg0 )
				{
				}
				
				@Override
				public void onPageScrolled(
						int arg0 ,
						float arg1 ,
						int arg2 )
				{
				}
				
				@Override
				public void onPageSelected(
						int index )
				{
					if( index == INDEX_LOCAL )
					{
						localButton.toggle();
					}
					else if( index == INDEX_HOT )
					{
						if( com.coco.theme.themebox.StaticClass.isAllowDownload( mContext ) )
						{
							// 无网�?
							if( IsHaveInternet( mContext ) == false )
							{
								Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
								if( themePagerAdapter != null && themePagerAdapter.viewDownloading != null )
								{
									themePagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
								}
							}
							else
							{
								if( themePagerAdapter != null && themePagerAdapter.viewDownloading != null )
								{
									themePagerAdapter.viewDownloading.setVisibility( View.VISIBLE );
								}
								if( FunctionConfig.isShowHotWallpaper() )
								{
									if( hotAdapter.getCount() == 0 )
									{
										themePagerAdapter.notifyDataSetChanged();
										downModule.downloadList();
										if( IsHaveInternet( mContext ) )
										{
											listRefresh = true;
											handler.postDelayed( new Runnable() {
												
												@Override
												public void run()
												{
													downModule.stopDownlist();
													if( listRefresh )
													{
														if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
														{
															Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
														}
													}
													if( themePagerAdapter.viewDownloading != null )
													{
														themePagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
													}
												}
											} , 1000 * 30 );
										}
									}
								}
								else
								{
									if( liveAdapter.getCount() == 0 )
									{
										themePagerAdapter.notifyDataSetChanged();
										liveAdapter.getDownModule().downloadList();
										if( IsHaveInternet( mContext ) )
										{
											listRefresh = true;
											handler.postDelayed( new Runnable() {
												
												@Override
												public void run()
												{
													liveAdapter.getDownModule().stopDownlist();
													if( listRefresh )
													{
														if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
														{
															Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
														}
													}
													if( themePagerAdapter.viewDownloading != null )
													{
														themePagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
													}
												}
											} , 1000 * 30 );
										}
									}
								}
							}
						}
						else
						{
							Toast.makeText( mContext , R.string.sdcard_not_available , Toast.LENGTH_SHORT ).show();
						}
						hotButton.toggle();
					}
				}
			} );
			// 热门
			hotButton.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View arg0 )
				{
					gridPager.setCurrentItem( INDEX_HOT , true );
				}
			} );
			// 本地按钮
			localButton.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View arg0 )
				{
					gridPager.setCurrentItem( INDEX_LOCAL , true );
				}
			} );
		}
		else
		{
			// 热门按钮
			final RadioButton themeHotButton = (RadioButton)result.findViewById( R.id.btnHotTheme );
			themeHotButton.setVisibility( View.GONE );
			// ViewPager
			gridPager = (ViewPager)result.findViewById( R.id.themeGridPager );
			themePagerAdapter = new GridPagerAdapter( localView );
			gridPager.setAdapter( themePagerAdapter );
			gridPager.setOverScrollMode( View.OVER_SCROLL_NEVER );
		}
		if( FunctionConfig.isLiveWallpaperShow() )
		{
			IntentFilter pkgFilter = new IntentFilter();
			pkgFilter.addAction( Intent.ACTION_PACKAGE_REMOVED );
			pkgFilter.addAction( Intent.ACTION_PACKAGE_ADDED );
			pkgFilter.addDataScheme( "package" );
			mContext.registerReceiver( packageReceiver , pkgFilter );
		}
		IntentFilter screenFilter1 = new IntentFilter();
		screenFilter1.addAction( UPDATA_CURRENT );
		screenFilter1.addAction( Intent.ACTION_WALLPAPER_CHANGED );
		screenFilter1.addAction( StaticClass.ACTION_START_DOWNLOAD_APK );
		screenFilter1.addAction( StaticClass.ACTION_THUMB_CHANGED );
		screenFilter1.addAction( StaticClass.ACTION_HOTLIST_CHANGED );
		screenFilter1.addAction( StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED );
		screenFilter1.addAction( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED );
		screenFilter1.addAction( StaticClass.ACTION_PAUSE_DOWNLOAD_APK );
		if( FunctionConfig.isLiveWallpaperShow() )
		{
			screenFilter1.addAction( StaticClass.ACTION_LIVE_START_DOWNLOAD_APK );
			screenFilter1.addAction( StaticClass.ACTION_LIVE_THUMB_CHANGED );
			screenFilter1.addAction( StaticClass.ACTION_LIVE_DOWNLOAD_SIZE_CHANGED );
			screenFilter1.addAction( StaticClass.ACTION_LIVE_DOWNLOAD_STATUS_CHANGED );
			screenFilter1.addAction( StaticClass.ACTION_LIVE_PAUSE_DOWNLOAD_APK );
		}
		mContext.registerReceiver( packageReceiver , screenFilter1 );
		// 下载成功
		return result;
	}
	
	private BroadcastReceiver packageReceiver;
	
	public void onDestroy()
	{
		unregisterReceiver();
		if( mAdapter != null )
		{
			mAdapter.onDestory();
		}
		if( hotAdapter != null )
		{
			hotAdapter.onDestory();
		}
		if( liveAdapter != null )
		{
			liveAdapter.onDestory();
		}
	}
	
	public void unregisterReceiver()
	{
		if( packageReceiver != null )
		{
			mContext.unregisterReceiver( packageReceiver );
		}
	}
	
	public void onStop()
	{
		//		mAdapter.saveCurrentWallpaper();
	}
	
	public BaseAdapter getLocalAdapter()
	{
		return mAdapter;
	}
	
	class GridHotWallpaperAdapter extends BaseAdapter
	{
		
		private List<WallpaperInformation> appList = new ArrayList<WallpaperInformation>();
		private Context context;
		private DownModule downThumb;
		private Bitmap imgDefaultThumb;
		private boolean mShowProgress = false;
		private PageTask pageTask = null;
		private Set<ImageView> recycle = new HashSet<ImageView>();
		
		public GridHotWallpaperAdapter(
				Context cxt ,
				DownModule down )
		{
			context = cxt;
			downThumb = down;
			imgDefaultThumb = ( (BitmapDrawable)cxt.getResources().getDrawable( R.drawable.default_img_large ) ).getBitmap();
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context );
			int size = preferences.getInt( "list-" + DownloadList.Wallpaper_Type , 0 );
			if( size == 0 )
			{
				if( !com.coco.theme.themebox.StaticClass.isAllowDownload( context ) )
				{
					mShowProgress = false;
				}
				else
				{
					mShowProgress = true;
				}
			}
			else
			{
				mShowProgress = false;
			}
			if( pageTask != null && pageTask.getStatus() != PageTask.Status.FINISHED )
			{
				pageTask.cancel( true );
			}
			pageTask = (PageTask)new PageTask().execute();
		}
		
		public void onDestory()
		{
			for( WallpaperInformation info : appList )
			{
				info.disposeThumb();
				info = null;
			}
			if( imgDefaultThumb != null && !imgDefaultThumb.isRecycled() )
			{
				imgDefaultThumb.recycle();
			}
		}
		
		public boolean showProgress()
		{
			System.out.println( "hotAdapter showprogress = " + mShowProgress );
			return mShowProgress;
		}
		
		public void setShowProgress(
				boolean isShow )
		{
			mShowProgress = isShow;
		}
		
		public void reloadPackage()
		{
			if( pageTask != null && pageTask.getStatus() != PageTask.Status.FINISHED )
			{
				pageTask.cancel( true );
			}
			pageTask = (PageTask)new PageTask().execute();
		}
		
		public List<WallpaperInformation> queryPackage(
				Set<String> pkgNameSet )
		{
			List<WallpaperInformation> appList = new ArrayList<WallpaperInformation>();
			WallpaperService service = new WallpaperService( context );
			List<WallpaperInformation> hotList = service.queryShowList();
			if( hotList.size() == 0 )
			{
				if( !com.coco.theme.themebox.StaticClass.isAllowDownload( context ) )
				{
					mShowProgress = false;
				}
				else
				{
					mShowProgress = true;
				}
			}
			else
			{
				mShowProgress = false;
			}
			for( WallpaperInformation item : hotList )
			{
				if( !pkgNameSet.contains( item.getPackageName() ) )
				{
					appList.add( item );
				}
			}
			return appList;
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
		
		public void updateThumb(
				final String pkgName )
		{
			new Thread() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					int findIndex = findPackageIndex( pkgName );
					if( findIndex < 0 )
					{
						return;
					}
					WallpaperInformation info = appList.get( findIndex );
					info.reloadThumb();
					( (Activity)context ).runOnUiThread( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							notifyDataSetChanged();
						}
					} );
				}
			}.start();
		}
		
		public void updateDownloadSize(
				String pkgName ,
				long downSize ,
				long totalSize )
		{
			int findIndex = findPackageIndex( pkgName );
			if( findIndex < 0 )
			{
				return;
			}
			WallpaperInformation info = appList.get( findIndex );
			info.setDownloadSize( downSize );
			info.setTotalSize( totalSize );
			notifyDataSetChanged();
		}
		
		@Override
		public void notifyDataSetChanged()
		{
			// TODO Auto-generated method stub
			recycle.clear();
			super.notifyDataSetChanged();
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
			if( FunctionConfig.isBrzh_setWaitBackgroundView() )
			{
				gridViewHot.setBackgroundColor( Color.TRANSPARENT );
			}
			View retView = convertView;
			ViewHolder viewHolder = null;
			if( retView != null )
			{
				viewHolder = (ViewHolder)convertView.getTag();
			}
			else
			{
				if( isHWStyle )
				{
					retView = LayoutInflater.from( mContext ).inflate( R.layout.hw_main_item , null );
				}
				else
				{
					retView = LayoutInflater.from( mContext ).inflate( R.layout.main_font_item , null );
				}
				viewHolder = new ViewHolder();
				viewHolder.viewName = (TextView)retView.findViewById( R.id.textAppName );
				viewHolder.viewThumb = (ImageView)retView.findViewById( R.id.imageThumb );
				viewHolder.imageCover = (ImageView)retView.findViewById( R.id.imageCover );
				viewHolder.imageUsed = retView.findViewById( R.id.imageUsed );
				viewHolder.barPause = (ProgressBar)retView.findViewById( R.id.barPause );
				viewHolder.barDownloading = (ProgressBar)retView.findViewById( R.id.barDownloading );
				viewHolder.pricetxt = (TextView)retView.findViewById( R.id.price );
				viewHolder.imageUsed.setVisibility( View.INVISIBLE );
				if( isHWStyle )
				{
					float density = mContext.getResources().getDisplayMetrics().density;
					int itemWidth = (int)( mContext.getResources().getDisplayMetrics().widthPixels / 3 - 6 * 2 * density - 10 * density );
					viewHolder.viewThumb.setLayoutParams( new RelativeLayout.LayoutParams( (int)( itemWidth + 6 * 2 * density ) , (int)( itemWidth / 0.6f + 6 * 2 * density ) ) );
					viewHolder.viewThumb.setBackgroundResource( R.drawable.hw_image_select );
					ImageView imageselect = (ImageView)retView.findViewById( R.id.imageselect );
					imageselect.setLayoutParams( new RelativeLayout.LayoutParams(
							(int)( itemWidth + 6 * 2 * mContext.getResources().getDisplayMetrics().density ) ,
							(int)( itemWidth / 0.6f + 6 * 2 * mContext.getResources().getDisplayMetrics().density ) ) );
					imageselect.setVisibility( View.VISIBLE );
				}
			}
			WallpaperInformation info = (WallpaperInformation)getItem( position );
			if( info.isNeedLoadDetail() )
			{
				Log.v( "GridHotWallpaperAdapter" , "loadDetail =" + info.getPackageName() );
				info.loadDetail( context , isHWStyle );
				if( info.getThumbImage() == null )
				{
					downThumb.downloadThumb( info.getPackageName() , DownloadList.Wallpaper_Type );
					Log.v( "GridHotWallpaperAdapter" , "downloadThumb = " + info.getPackageName() );
				}
			}
			//把position为0的单独拿出来显示,是为了解决,首次进入此页面时,第一张缩略图会闪烁多次。
			if( position == 0 )
			{
				Bitmap imgThumb = info.getThumbImage();
				if( imgThumb == null )
				{
					imgThumb = imgDefaultThumb;
				}
				viewHolder.viewThumb.setImageBitmap( imgThumb );
			}
			else
			{
				ImageLoader.getInstance().displayImage( "file:///" + PathTool.getThumbFile( info.getPackageName() ) , viewHolder.viewThumb , options );
			}
			if( FunctionConfig.isPriceVisible() )
			{
				int price = info.getPrice();
				if( info.getPrice() > 0 && !info.isDownloadedFinish() )
				{// 下载完成后，热门推荐中不显示价格
					viewHolder.pricetxt.setVisibility( View.VISIBLE );
					boolean ispay = Tools.isContentPurchased( context , DownloadList.Wallpaper_Type , info.getPackageName() );
					if( ispay )
					{
						viewHolder.pricetxt.setBackgroundResource( R.drawable.buyed_bg );
						viewHolder.pricetxt.setText( R.string.has_bought );
					}
					else
					{
						viewHolder.pricetxt.setBackgroundResource( R.drawable.price_bg );
						viewHolder.pricetxt.setText( "￥：" + price / 100 );
					}
				}
				else
				{
					viewHolder.pricetxt.setVisibility( View.GONE );
				}
			}
			if( info.getDownloadStatus() == DownloadStatus.StatusInit || info.getDownloadStatus() == DownloadStatus.StatusFinish )
			{
				viewHolder.imageCover.setVisibility( View.INVISIBLE );
				viewHolder.barPause.setVisibility( View.INVISIBLE );
				viewHolder.barDownloading.setVisibility( View.INVISIBLE );
			}
			else
			{
				viewHolder.imageCover.setVisibility( View.VISIBLE );
				if( info.getDownloadStatus() == DownloadStatus.StatusDownloading )
				{
					viewHolder.barDownloading.setVisibility( View.VISIBLE );
					viewHolder.barPause.setVisibility( View.INVISIBLE );
					viewHolder.barDownloading.setProgress( info.getDownloadPercent() );
				}
				else
				{
					viewHolder.barDownloading.setVisibility( View.INVISIBLE );
					viewHolder.barPause.setVisibility( View.VISIBLE );
					viewHolder.barPause.setProgress( info.getDownloadPercent() );
				}
			}
			viewHolder.viewName.setVisibility( View.GONE );
			retView.setTag( viewHolder );
			return retView;
		}
		
		private void Recyclebitmap(
				ImageView view )
		{
			boolean isrecycle = true;
			Bitmap bmp = Tools.recycleImageBitmap( view );
			if( bmp == null || bmp.isRecycled() || bmp == imgDefaultThumb )
			{
				return;
			}
			for( ImageView v : recycle )
			{
				if( v == view )
				{
					continue;
				}
				Bitmap temp = Tools.recycleImageBitmap( v );
				if( temp == bmp )
				{
					isrecycle = false;
					break;
				}
			}
			if( isrecycle )
			{
				bmp.recycle();
			}
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
				for( WallpaperInformation info : appList )
				{
					info.disposeThumb();
					info = null;
				}
				appList.clear();
				appList.addAll( result );
				notifyDataSetChanged();
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
				WallpaperService themeSv = new WallpaperService( mContext );
				List<WallpaperInformation> installList = themeSv.queryDownloadList();
				Set<String> packageNameSet = new HashSet<String>();
				// @2015/09/09 ADD START by gaominghui
				String[] pkg = null;
				String pkgName = null;
				for( WallpaperInformation info : installList )
				{
					pkg = info.getPackageName().split( "#" );
					if( pkg != null )
					{
						pkgName = pkg[0];
					}
					else
					{
						pkgName = info.getPackageName();
					}
					packageNameSet.add( pkgName );
				}
				// @2015/09/09 ADD END
				return queryPackage( packageNameSet );
			}
		}
	}
	
	class ViewHolder
	{
		
		ImageView viewThumb;
		TextView viewName;
		ImageView imageCover;
		View imageUsed;
		ProgressBar barPause;
		ProgressBar barDownloading;
		TextView pricetxt;
	}
	
	class GridLocalWallpaperAdapter extends BaseAdapter
	{
		
		private Context mContext;
		private String currentWallpaper;
		private List<WallpaperInformation> localList = new ArrayList<WallpaperInformation>();
		private Bitmap imgDefaultThumb;
		private DownModule downThumb;
		private Set<String> packageNameSet = new HashSet<String>();
		private Set<ImageView> recycle = new HashSet<ImageView>();
		
		public GridLocalWallpaperAdapter(
				final Context context ,
				DownModule module )
		{
			mContext = context;
			downThumb = module;
			imgDefaultThumb = ( (BitmapDrawable)mContext.getResources().getDrawable( R.drawable.default_img_large ) ).getBitmap();
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					if( isDesktopWall )
						currentWallpaper = Assets.getWallpaper( context , "currentWallpaper" );
					else
					{
						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( mContext );
						currentWallpaper = pref.getString( "lockWallpaper" , "default" );
					}
					// currentWallpaper = pref.getString("currentWallpaper", "default");
					if( currentWallpaper == null || currentWallpaper.trim().length() == 0 )
					{
						currentWallpaper = "default";
					}
					//getWallpapersInfo();
					//queryPackage();//将这两句放到runOnUiThread中是为了让图库和动态壁纸两个Item与其他本地图片同时显示出来
					( (Activity)mContext ).runOnUiThread( new Runnable() {
						
						@Override
						public void run()
						{
							getWallpapersInfo();
							queryPackage();
							recycle.clear();
							notifyDataSetChanged();
						}
					} );
				}
			} ).start();
		}
		
		public void onDestory()
		{
			for( WallpaperInformation info : localList )
			{
				info.disposeThumb();
				info = null;
			}
			for( Bitmap bmp : localBmp )
			{
				bmp.recycle();
				bmp = null;
			}
			if( imgDefaultThumb != null && !imgDefaultThumb.isRecycled() )
			{
				imgDefaultThumb.recycle();
			}
		}
		
		private void getWallpapersInfo()
		{
			if( !isHWStyle )
			{
				List<ResolveInfo> temp;
				galleryPkg = FunctionConfig.getGalleryPkg().split( ";" );
				Intent pickWallpaper = new Intent( Intent.ACTION_SET_WALLPAPER );
				temp = mContext.getPackageManager().queryIntentActivities( pickWallpaper , PackageManager.GET_ACTIVITIES );
				mResolveInfoList.clear();
				packageNameSet.clear();
				boolean isfindGallery = false;
				boolean isfindLiveWallpaper = false;
				for( ResolveInfo info : temp )
				{
					String packagename = info.activityInfo.packageName;
					/*Log.i( "sss" , "packagename = "+packagename );
					if(packagename.equals( "com.google.android.apps.plus" )){
						photoName = packagename;
					}*/
					// @gaominghui2015/07/08 ADD START 添加是否显示图库的开关控制
					if( FunctionConfig.isIs_show_local_gallery() )
					// @gaominghui2015/07/08 ADD END
					{
						if( !isfindGallery )
						{
							for( int i = 0 ; i < galleryPkg.length ; i++ )
							{
								if( galleryPkg[i].equals( packagename ) )
								{
									GALLERY = packagename;
									mResolveInfoList.add( info );
									isfindGallery = true;
									break;
								}
							}
						}
					}
					// @gaominghui2015/07/08 ADD START添加是否显示动态壁纸的开关控制
					if( FunctionConfig.isShow_local_livewallpaper() )
					{
						// @gaominghui2015/07/08 ADD END
						if( !isfindLiveWallpaper )
						{
							for( int i = 0 ; i < livePkg.length ; i++ )
							{
								if( livePkg[i].equals( packagename ) )
								{
									LIVEPICKER = packagename;
									mResolveInfoList.add( info );
									isfindLiveWallpaper = true;
									break;
								}
								//<Folder Refactoring> liuhailin@2014-11-26 del begin
								//if( LIVEPICKER.equals( packagename ) )
								//{
								//	mResolveInfoList.add( info );
								//}
								//<Folder Refactoring> liuhailin@2014-11-26 del end
							}
						}
					}
				}
				//将视频壁纸排放在图库和动态壁纸后面
				for( ResolveInfo info : temp )
				{
					String packagename = info.activityInfo.packageName;
					if( VIDEO_WALLPAPER.equals( packagename ) )
					{
						if( FunctionConfig.isEnableShowVideoWallpaper() )
						{
							mResolveInfoList.add( info );
						}
					}
				}
				temp = mContext.getPackageManager().queryIntentActivities( new Intent( "com.coco.action.wallpaper" ) , PackageManager.GET_ACTIVITIES );
				for( ResolveInfo info : temp )
				{
					String packagename = info.activityInfo.packageName;
					String cls = info.activityInfo.name;
					if( FunctionConfig.isStatictoIcon() )
					{
						if( mContext.getPackageName().equals( packagename ) && "com.coco.wallpaper.wallpaperbox.WallpaperPreviewActivity".equals( cls ) )
						{
							mResolveInfoList.add( info );
						}
					}
					if( FunctionConfig.isLockwallpaperShow() )
					{
						if( mContext.getPackageName().equals( packagename ) && "com.coco.wallpaper.wallpaperbox.LockWallpaperPreview".equals( cls ) )
						{
							mResolveInfoList.add( info );
						}
					}
				}
				if( FunctionConfig.isEnable_topwise_style() )
				{
					Intent it = new Intent();
					it.setPackage( "topwise.shark.wallpaperSet" );
					it.addCategory( "android.intent.category.LAUNCHER" );
					temp = mContext.getPackageManager().queryIntentActivities( it , PackageManager.GET_ACTIVITIES );
					for( ResolveInfo info : temp )
					{
						mResolveInfoList.add( info );
					}
				}
			}
			if( !FunctionConfig.isStatictoIcon() || isHWStyle )
			{
				if( FunctionConfig.getWallpapers_from_other_apk() != null )
				{
					try
					{
						Context remountContext = mContext.createPackageContext( FunctionConfig.getWallpapers_from_other_apk() , Context.CONTEXT_IGNORE_SECURITY );
						Resources res = remountContext.getResources();
						for( int i = 1 ; ; i++ )
						{
							try
							{
								int drawable = res.getIdentifier( "wallpaper_" + ( i < 10 ? "0" + i : i ) + "_small" , "drawable" , FunctionConfig.getWallpapers_from_other_apk() );
								if( drawable == 0 )
								{
									break;
								}
								Bitmap bitmap = Tools.drawableToBitmap( res.getDrawable( drawable ) );
								mThumbs.add( "wallpaper_" + ( i < 10 ? "0" + i : i ) );
								localBmp.add( bitmap );
							}
							catch( IllegalArgumentException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						return;
					}
					catch( NameNotFoundException e )
					{
						Log.e( "tabwallpaper" , "createPackageContext exception: " + e );
					}
					return;
				}
				infos.findWallpapers( mThumbs );
				useCustomWallpaper = infos.isUseCustomWallpaper();
				customWallpaperPath = infos.getCustomWallpaperPath();
				Log.i( "wallpaperPath" , "useCustomWallpaper = " + useCustomWallpaper );
				Log.i( "wallpaperPath" , "customWallpaperPath = " + customWallpaperPath );
				for( String str : mThumbs )
				{
					InputStream is = null;
					if( useCustomWallpaper )
					{
						try
						{
							is = new FileInputStream( customWallpaperPath + "/" + str );
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
							Context remoteContext = mContext.createPackageContext( ThemesDB.LAUNCHER_PACKAGENAME , Context.CONTEXT_IGNORE_SECURITY );
							AssetManager asset = remoteContext.getResources().getAssets();
							Log.i( "wallpaperPath" , "ThemesDB.LAUNCHER_PACKAGENAME = " + ThemesDB.LAUNCHER_PACKAGENAME );
							if( ThemesDB.LAUNCHER_PACKAGENAME.equals( "com.cooee.Mylauncher" ) || ThemesDB.LAUNCHER_PACKAGENAME.equals( "com.cooee.launcherS4" ) || ThemesDB.LAUNCHER_PACKAGENAME
									.equals( "com.cooee.launcherS4" ) || ThemesDB.LAUNCHER_PACKAGENAME.equals( "com.cooee.launcherS5" ) || ThemesDB.LAUNCHER_PACKAGENAME
									.equals( "com.cooee.launcherHS" ) )
							{
								wallpaperPath = "wallpapers";
							}
							try
							{
								is = asset.open( wallpaperPath + "/" + str );
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
						Bitmap bitmap = BitmapFactory.decodeStream( is );
						if( isHWStyle )
						{
							int height = bitmap.getHeight();
							int width = bitmap.getWidth();
							Bitmap bmp = null;
							if( height * 0.6f < width )
							{
								bmp = Bitmap.createBitmap( bitmap , (int)( ( width - height * 0.6f ) / 2 ) , 0 , (int)( height * 0.6f ) , height );
							}
							else
							{
								bmp = Bitmap.createBitmap( bitmap , 0 , (int)( ( height - width * 0.6f ) / 2 ) , width , (int)( width * 0.6f ) );
							}
							if( bitmap != null && !bitmap.isRecycled() )
							{
								bitmap.recycle();
								bitmap = null;
							}
							localBmp.add( bmp );
						}
						else
						{
							localBmp.add( bitmap );
						}
					}
				}
			}
		}
		
		private synchronized void queryPackage()
		{
			//Tools.printStackTrace( "tagWallpaper" );
			if( !FunctionConfig.isStatictoIcon() )
			{
				packageNameSet.clear();
				for( ResolveInfo info : mResolveInfoList )
				{
					packageNameSet.add( info.activityInfo.packageName + " local" );
				}
				for( String str : mThumbs )
				{
					packageNameSet.add( str + " local" );
				}
				WallpaperService themeSv = new WallpaperService( mContext );
				List<WallpaperInformation> installList = themeSv.queryDownloadList();
				//Log.v( "tagWallpaper" ,"installList.SIZE-- = " + installList.size() );
				localList.clear();
				Log.v( "tagWallpaper" , "installList.size() = " + installList.size() );
				for( WallpaperInformation info : installList )
				{
					info.setThumbImage( mContext , info.getPackageName() , info.getClassName() );
					localList.add( info );
					packageNameSet.add( info.getPackageName() );
				}
				Log.v( "tagWallpaper" , "locallIST.SIZE-- = " + localList.size() );
			}
		}
		
		public void reloadPackage()
		{
			if( !FunctionConfig.isStatictoIcon() )
			{
				new Thread( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						queryPackage();
						( (Activity)mContext ).runOnUiThread( new Runnable() {
							
							@Override
							public void run()
							{
								recycle.clear();
								notifyDataSetChanged();
							}
						} );
					}
				} ).start();
			}
		}
		
		public void updateDownloadSize(
				String pkgName ,
				long downSize ,
				long totalSize )
		{
			int findIndex = findPackageIndex( pkgName );
			if( findIndex < 0 )
			{
				return;
			}
			WallpaperInformation info = localList.get( findIndex );
			info.setDownloadSize( downSize );
			info.setTotalSize( totalSize );
			notifyDataSetChanged();
		}
		
		public Set<String> getPackageNameSet()
		{
			return packageNameSet;
		}
		
		public boolean containPackage(
				String packageName )
		{
			return findPackageIndex( packageName ) >= 0;
		}
		
		private int findPackageIndex(
				String packageName )
		{
			int i = 0;
			for( i = 0 ; i < localList.size() ; i++ )
			{
				if( packageName.equals( localList.get( i ).getPackageName() ) )
				{
					return i;
				}
			}
			return -1;
		}
		
		@Override
		public int getCount()
		{
			// TODO Auto-generated method stub
			if( FunctionConfig.isStatictoIcon() )
			{
				return mResolveInfoList.size();
			}
			return mResolveInfoList.size() + localBmp.size() + localList.size();
		}
		
		@Override
		public Object getItem(
				int position )
		{
			// TODO Auto-generated method stub
			if( position < mResolveInfoList.size() + localBmp.size() )
			{
				return position;
			}
			if( localList.size() == 0 )
			{
				return null;
			}
			int tagPosition = position - mResolveInfoList.size() - localBmp.size();
			if( tagPosition >= localList.size() )
			{
				return null;
			}
			return localList.get( tagPosition );
		}
		
		@Override
		public long getItemId(
				int position )
		{
			// TODO Auto-generated method stub
			return position;
		}
		
		@Override
		public void notifyDataSetChanged()
		{
			// TODO Auto-generated method stub
			if( isDesktopWall )
				currentWallpaper = Assets.getWallpaper( mContext , "currentWallpaper" );
			else
			{
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( mContext );
				currentWallpaper = pref.getString( "lockWallpaper" , "default" );
			}
			Log.v( "test" , "currentWallpaper = " + currentWallpaper );
			if( currentWallpaper == null || currentWallpaper.trim().length() == 0 )
			{
				currentWallpaper = "default";
			}
			super.notifyDataSetChanged();
		}
		
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			// TODO Auto-generated method stub
			if( FunctionConfig.isBrzh_setWaitBackgroundView() )
			{
				gridPager.setBackgroundColor( Color.TRANSPARENT );
			}
			if( convertView == null )
			{
				if( isHWStyle )
				{
					convertView = LayoutInflater.from( mContext ).inflate( R.layout.hw_main_item , null );
				}
				else
				{
					convertView = LayoutInflater.from( mContext ).inflate( R.layout.main_font_item , null );
				}
			}
			ImageView viewThumb = (ImageView)convertView.findViewById( R.id.imageThumb );
			ImageView viewIcon = (ImageView)convertView.findViewById( R.id.imageIcon );
			TextView viewName = (TextView)convertView.findViewById( R.id.textAppName );
			if( isHWStyle )
			{
				float density = mContext.getResources().getDisplayMetrics().density;
				int itemWidth = (int)( mContext.getResources().getDisplayMetrics().widthPixels / 3 - 6 * 2 * density - 10 * density );
				viewThumb.setLayoutParams( new RelativeLayout.LayoutParams( (int)( itemWidth + 6 * 2 * density ) , (int)( itemWidth / 0.6f + 6 * 2 * density ) ) );
				viewThumb.setBackgroundResource( R.drawable.hw_image_select );
				ImageView imageselect = (ImageView)convertView.findViewById( R.id.imageselect );
				imageselect.setLayoutParams( new RelativeLayout.LayoutParams(
						(int)( itemWidth + 6 * 2 * mContext.getResources().getDisplayMetrics().density ) ,
						(int)( itemWidth / 0.6f + 6 * 2 * mContext.getResources().getDisplayMetrics().density ) ) );
				imageselect.setVisibility( View.VISIBLE );
			}
			if( position < mResolveInfoList.size() )
			{
				ResolveInfo resolve = mResolveInfoList.get( position );
				if( GALLERY.equals( resolve.activityInfo.packageName ) )
				{
					if( FunctionConfig.isEnable_tophard_style() )
					{
						viewIcon.setImageResource( R.drawable.gallery_scene );
					}
					else
					{
						viewIcon.setImageResource( R.drawable.gallery_normal );
					}
				}
				else if( LIVEPICKER.equals( resolve.activityInfo.packageName ) )
				{
					if( FunctionConfig.isEnable_tophard_style() )
					{
						viewIcon.setImageResource( R.drawable.livewallpaper_scene );
					}
					else
					{
						viewIcon.setImageResource( R.drawable.livewallpaper_normal );
					}
				}
				else if( VIDEO_WALLPAPER.equals( resolve.activityInfo.packageName ) )
				{
					if( FunctionConfig.isEnableShowVideoWallpaper() )
					{
						viewIcon.setImageResource( R.drawable.video_normal );
					}
				}
				else if( "com.coco.wallpaper.wallpaperbox.WallpaperPreviewActivity".equals( resolve.activityInfo.name ) )
				{
					viewIcon.setImageResource( R.drawable.staticwallpaper_scene );
				}
				else if( "com.coco.wallpaper.wallpaperbox.LockWallpaperPreview".equals( resolve.activityInfo.name ) )
				{
					viewIcon.setImageResource( R.drawable.lockwallpaper_scene );
				}
				else if( "topwise.shark.wallpaperSet".equals( resolve.activityInfo.packageName ) )
				{
					viewIcon.setImageResource( R.drawable.wallpaper_more );
				}
				viewIcon.setVisibility( View.VISIBLE );
				viewThumb.setVisibility( View.INVISIBLE );
				viewName.setText( resolve.loadLabel( mContext.getPackageManager() ).toString() );
				convertView.findViewById( R.id.imageUsed ).setVisibility( View.INVISIBLE );
			}
			else if( position >= mResolveInfoList.size() && position < ( localBmp.size() + mResolveInfoList.size() ) )
			{
				Bitmap bmp = localBmp.get( position - mResolveInfoList.size() );
				if( bmp == null )
				{
					viewThumb.setImageBitmap( imgDefaultThumb );
				}
				else
				{
					viewThumb.setImageBitmap( bmp );
				}
				Drawable thumbDrawable = viewThumb.getDrawable();
				if( thumbDrawable != null )
				{
					thumbDrawable.setDither( true );
				}
				if( mThumbs.get( position - mResolveInfoList.size() ).replace( "_small" , "" ).equals( currentWallpaper ) /*|| ( position - mResolveInfoList.size() == 0 && currentWallpaper
																															.equals( "default" ) )*/)
				{
					//使用中是否显示
					if( FunctionConfig.isEnableShowCurrentWallpaperFlag() )
					{
						convertView.findViewById( R.id.imageUsed ).setVisibility( View.VISIBLE );
					}
					else
					{
						convertView.findViewById( R.id.imageUsed ).setVisibility( View.INVISIBLE );
					}
				}
				else
				{
					convertView.findViewById( R.id.imageUsed ).setVisibility( View.INVISIBLE );
				}
				viewIcon.setVisibility( View.INVISIBLE );
				viewThumb.setVisibility( View.VISIBLE );
			}
			else
			{
				if( (WallpaperInformation)getItem( position ) == null )
				{
					return convertView;
				}
				WallpaperInformation Info = (WallpaperInformation)getItem( position );
				if( Info.isNeedLoadDetail() )
				{
					Bitmap imgThumb = Info.getThumbImage();
					if( imgThumb == null )
					{
						Info.loadDetail( mContext , isHWStyle );
					}
					if( Info.getThumbImage() == null )
					{
						downThumb.downloadThumb( Info.getPackageName() , DownloadList.Wallpaper_Type );
					}
				}
				Bitmap imgThumb = Info.getThumbImage();
				//				recycle.add( viewThumb );
				//				Tools.Recyclebitmap( imgDefaultThumb , imgThumb , viewThumb , recycle );
				if( imgThumb == null )
				{
					imgThumb = imgDefaultThumb;
				}
				viewThumb.setImageBitmap( imgThumb );
				if( currentWallpaper.contains( Info.getPackageName() ) )
				{
					Log.i( "test" , "item name = " + Info.getPackageName() );
					//使用中是否显示
					if( FunctionConfig.isEnableShowCurrentWallpaperFlag() )
					{
						convertView.findViewById( R.id.imageUsed ).setVisibility( View.VISIBLE );
					}
					else
					{
						convertView.findViewById( R.id.imageUsed ).setVisibility( View.INVISIBLE );
					}
				}
				else
				{
					convertView.findViewById( R.id.imageUsed ).setVisibility( View.INVISIBLE );
				}
				viewIcon.setVisibility( View.INVISIBLE );
				viewThumb.setVisibility( View.VISIBLE );
			}
			convertView.findViewById( R.id.imageCover ).setVisibility( View.GONE );
			convertView.findViewById( R.id.barPause ).setVisibility( View.GONE );
			convertView.findViewById( R.id.barDownloading ).setVisibility( View.GONE );
			viewName.setVisibility( View.GONE );
			return convertView;
		}
	}
	
	class GridLiveWallpaperAdapter extends BaseAdapter
	{
		
		private List<WallpaperInformation> appList = new ArrayList<WallpaperInformation>();
		private Context context;
		private DownModule downThumb;
		private Bitmap imgDefaultThumb;
		private boolean mShowProgress = true;
		private PageTask pageTask = null;
		private Set<ImageView> recycle = new HashSet<ImageView>();;
		
		public GridLiveWallpaperAdapter(
				Context cxt )
		{
			context = cxt;
			downThumb = DownModule.getInstance( context );
			imgDefaultThumb = ( (BitmapDrawable)cxt.getResources().getDrawable( R.drawable.default_img_large ) ).getBitmap();
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context );
			int size = preferences.getInt( "list-" + DownloadList.LiveWallpaper_Type , 0 );
			if( size == 0 )
			{
				if( !com.coco.theme.themebox.StaticClass.isAllowDownload( context ) )
				{
					mShowProgress = false;
				}
				else
				{
					mShowProgress = true;
				}
			}
			else
			{
				mShowProgress = false;
			}
			if( pageTask != null && pageTask.getStatus() != PageTask.Status.FINISHED )
			{
				pageTask.cancel( true );
			}
			pageTask = (PageTask)new PageTask().execute();
		}
		
		public void onDestory()
		{
			for( WallpaperInformation info : appList )
			{
				info.disposeThumb();
				info = null;
			}
			if( imgDefaultThumb != null && !imgDefaultThumb.isRecycled() )
			{
				imgDefaultThumb.recycle();
			}
		}
		
		public boolean showProgress()
		{
			return mShowProgress;
		}
		
		public void setShowProgress(
				boolean isShow )
		{
			mShowProgress = isShow;
		}
		
		public void reloadPackage()
		{
			if( pageTask != null && pageTask.getStatus() != PageTask.Status.FINISHED )
			{
				pageTask.cancel( true );
			}
			pageTask = (PageTask)new PageTask().execute();
		}
		
		public List<WallpaperInformation> queryPackage(
				Set<String> pkgNameSet )
		{
			List<WallpaperInformation> appList = new ArrayList<WallpaperInformation>();
			LiveWallpaperService service = new LiveWallpaperService( context );
			List<WallpaperInformation> hotList = service.queryShowList();
			if( hotList.size() == 0 )
			{
				if( !com.coco.theme.themebox.StaticClass.isAllowDownload( context ) )
				{
					mShowProgress = false;
				}
				else
				{
					mShowProgress = true;
				}
			}
			else
			{
				mShowProgress = false;
			}
			for( WallpaperInformation item : hotList )
			{
				if( item.getPackageName().startsWith( "com.vlife.coco.wallpaper" ) || !pkgNameSet.contains( item.getPackageName() ) )
				{
					appList.add( item );
				}
			}
			return appList;
		}
		
		public int findPackageIndex(
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
		
		public void updateThumb(
				final String pkgName )
		{
			new Thread() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					int findIndex = findPackageIndex( pkgName );
					if( findIndex < 0 )
					{
						return;
					}
					WallpaperInformation info = appList.get( findIndex );
					info.reloadThumb();
					( (Activity)context ).runOnUiThread( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							notifyDataSetChanged();
						}
					} );
				}
			}.start();
		}
		
		public void updateDownloadSize(
				String pkgName ,
				long downSize ,
				long totalSize )
		{
			int findIndex = findPackageIndex( pkgName );
			if( findIndex < 0 )
			{
				return;
			}
			WallpaperInformation info = appList.get( findIndex );
			info.setDownloadSize( downSize );
			info.setTotalSize( totalSize );
			notifyDataSetChanged();
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
		
		public DownModule getDownModule()
		{
			return downThumb;
		}
		
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			if( FunctionConfig.isBrzh_setWaitBackgroundView() )
			{
				gridViewLive.setBackgroundColor( Color.TRANSPARENT );
			}
			View retView = convertView;
			ViewHolder viewHolder = null;
			if( retView != null )
			{
				// Log.e("test", "convertView!=null");
				viewHolder = (ViewHolder)convertView.getTag();
			}
			else
			{
				if( isHWStyle )
				{
					retView = LayoutInflater.from( mContext ).inflate( R.layout.hw_main_item , null );
				}
				else
				{
					retView = LayoutInflater.from( mContext ).inflate( R.layout.main_font_item , null );
				}
				viewHolder = new ViewHolder();
				viewHolder.viewName = (TextView)retView.findViewById( R.id.textAppName );
				viewHolder.viewThumb = (ImageView)retView.findViewById( R.id.imageThumb );
				viewHolder.imageCover = (ImageView)retView.findViewById( R.id.imageCover );
				viewHolder.imageUsed = retView.findViewById( R.id.imageUsed );
				viewHolder.barPause = (ProgressBar)retView.findViewById( R.id.barPause );
				viewHolder.barDownloading = (ProgressBar)retView.findViewById( R.id.barDownloading );
				viewHolder.pricetxt = (TextView)retView.findViewById( R.id.price );
				viewHolder.imageUsed.setVisibility( View.INVISIBLE );
				if( isHWStyle )
				{
					float density = mContext.getResources().getDisplayMetrics().density;
					int itemWidth = (int)( mContext.getResources().getDisplayMetrics().widthPixels / 3 - 6 * 2 * density - 10 * density );
					viewHolder.viewThumb.setLayoutParams( new RelativeLayout.LayoutParams( (int)( itemWidth + 6 * 2 * density ) , (int)( itemWidth / 0.6f + 6 * 2 * density ) ) );
					viewHolder.viewThumb.setBackgroundResource( R.drawable.hw_image_select );
					ImageView imageselect = (ImageView)retView.findViewById( R.id.imageselect );
					imageselect.setLayoutParams( new RelativeLayout.LayoutParams(
							(int)( itemWidth + 6 * 2 * mContext.getResources().getDisplayMetrics().density ) ,
							(int)( itemWidth / 0.6f + 6 * 2 * mContext.getResources().getDisplayMetrics().density ) ) );
					imageselect.setVisibility( View.VISIBLE );
				}
			}
			//recycle.add( viewHolder.viewThumb );
			//Recyclebitmap( viewHolder.viewThumb );
			WallpaperInformation info = (WallpaperInformation)getItem( position );
			if( info.isNeedLoadDetail() )
			{
				info.loadDetail( context , isHWStyle );
				if( info.getThumbImage() == null )
				{
					downThumb.downloadThumb( info.getPackageName() , DownloadList.LiveWallpaper_Type );
				}
			}
			//把position为0的单独拿出来显示,是为了解决,首次进入此页面时,第一张缩略图会闪烁多次。
			if( position == 0 )
			{
				Bitmap imgThumb = info.getThumbImage();
				if( imgThumb == null )
				{
					imgThumb = imgDefaultThumb;
				}
				viewHolder.viewThumb.setImageBitmap( imgThumb );
			}
			else
			{
				ImageLoader.getInstance().displayImage( "file:///" + PathTool.getThumbFile( info.getPackageName() ) , viewHolder.viewThumb , options );
			}
			if( FunctionConfig.isPriceVisible() )
			{
				int price = info.getPrice();
				if( info.getPrice() > 0 && !info.isDownloadedFinish() )
				{// 下载完成后，热门推荐中不显示价格
					viewHolder.pricetxt.setVisibility( View.VISIBLE );
					boolean ispay = Tools.isContentPurchased( context , DownloadList.LiveWallpaper_Type , info.getPackageName() );
					if( ispay )
					{
						viewHolder.pricetxt.setBackgroundResource( R.drawable.buyed_bg );
						viewHolder.pricetxt.setText( R.string.has_bought );
					}
					else
					{
						viewHolder.pricetxt.setBackgroundResource( R.drawable.price_bg );
						viewHolder.pricetxt.setText( "￥：" + price / 100 );
					}
				}
				else
				{
					viewHolder.pricetxt.setVisibility( View.GONE );
				}
			}
			if( !FunctionConfig.isShowHotWallpaper() && FunctionConfig.isLiveWallpaperShow() )
			{
				retView.findViewById( R.id.live_sign ).setVisibility( View.VISIBLE );
			}
			if( info.getDownloadStatus() == DownloadStatus.StatusInit || info.getDownloadStatus() == DownloadStatus.StatusFinish )
			{
				viewHolder.imageCover.setVisibility( View.INVISIBLE );
				viewHolder.barPause.setVisibility( View.INVISIBLE );
				viewHolder.barDownloading.setVisibility( View.INVISIBLE );
			}
			else
			{
				viewHolder.imageCover.setVisibility( View.VISIBLE );
				if( info.getDownloadStatus() == DownloadStatus.StatusDownloading )
				{
					viewHolder.barDownloading.setVisibility( View.VISIBLE );
					viewHolder.barPause.setVisibility( View.INVISIBLE );
					viewHolder.barDownloading.setProgress( info.getDownloadPercent() );
				}
				else
				{
					viewHolder.barDownloading.setVisibility( View.INVISIBLE );
					viewHolder.barPause.setVisibility( View.VISIBLE );
					viewHolder.barPause.setProgress( info.getDownloadPercent() );
				}
			}
			viewHolder.viewName.setVisibility( View.GONE );
			retView.setTag( viewHolder );
			return retView;
		}
		
		private void Recyclebitmap(
				ImageView view )
		{
			boolean isrecycle = true;
			Bitmap bmp = Tools.recycleImageBitmap( view );
			if( bmp == null || bmp.isRecycled() || bmp == imgDefaultThumb )
			{
				return;
			}
			for( ImageView v : recycle )
			{
				if( v == view )
				{
					continue;
				}
				Bitmap temp = Tools.recycleImageBitmap( v );
				if( temp == bmp )
				{
					isrecycle = false;
					break;
				}
			}
			if( isrecycle )
			{
				bmp.recycle();
			}
		}
		
		@Override
		public void notifyDataSetChanged()
		{
			// TODO Auto-generated method stub
			recycle.clear();
			super.notifyDataSetChanged();
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
				for( WallpaperInformation info : appList )
				{
					info.disposeThumb();
					info = null;
				}
				appList.clear();
				appList.addAll( result );
				notifyDataSetChanged();
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
				LiveWallpaperService lws = new LiveWallpaperService( mContext );
				List<WallpaperInformation> installList = lws.queryInstallList();
				Set<String> packageNameSet = new HashSet<String>();
				for( WallpaperInformation info : installList )
				{
					packageNameSet.add( info.getPackageName() );
				}
				return queryPackage( packageNameSet );
			}
		}
	}
	
	private void refreshList()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( mContext );
		long refresh = prefs.getLong( "refresh" , 0 );
		if( System.currentTimeMillis() - refresh < 1000 * 100 && System.currentTimeMillis() - refresh > 0 )
		{
			handler.postDelayed( new Runnable() {
				
				@Override
				public void run()
				{
					if( footerRefresh )
					{
						mPullToRefreshView.onFooterRefreshComplete();
						footerRefresh = false;
					}
					if( headerRefresh )
					{
						mPullToRefreshView.onHeaderRefreshComplete();
						headerRefresh = false;
					}
				}
			} , 1000 * 10 );
		}
		else
		{
			prefs.edit().putLong( "refresh" , System.currentTimeMillis() ).commit();
			downModule.downloadList();
			handler.postDelayed( new Runnable() {
				
				@Override
				public void run()
				{
					downModule.stopDownlist();
					if( footerRefresh )
					{
						mPullToRefreshView.onFooterRefreshComplete();
						footerRefresh = false;
						if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
						{
							Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
						}
					}
					if( headerRefresh )
					{
						mPullToRefreshView.onHeaderRefreshComplete();
						headerRefresh = false;
						if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
						{
							Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
						}
					}
				}
			} , 1000 * 30 );
		}
	}
	
	@Override
	public void onFooterRefresh(
			PullToRefreshView view )
	{
		Log.v( "PullToRefreshView" , "tablock_onFooterRefresh" );
		if( gridPager.getCurrentItem() == INDEX_HOT )
		{
			if( IsHaveInternet( mContext ) )
			{
				footerRefresh = true;
				refreshList();
			}
			else
			{
				Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
				mPullToRefreshView.onFooterRefreshComplete();
			}
		}
	}
	
	@Override
	public void onHeaderRefresh(
			PullToRefreshView view )
	{
		Log.v( "PullToRefreshView" , "tablock_onFooterRefresh" );
		if( gridPager.getCurrentItem() == INDEX_HOT )
		{
			Log.v( "onHeaderRefresh" , "**************" );
			if( IsHaveInternet( mContext ) )
			{
				headerRefresh = true;
				refreshList();
			}
			else
			{
				Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
				mPullToRefreshView.onHeaderRefreshComplete();
			}
		}
	}
	
	/**
	 * ViewPager适配�?
	 */
	static class GridPagerAdapter extends PagerAdapter
	{
		
		private final String LOG_TAG = "GridPagerAdapter";
		private View gridLocal;
		private GridView gridHot;
		private View hotView;
		private View viewDownloading = null;
		private BaseAdapter hotAdapter;
		
		public GridPagerAdapter(
				View local ,
				View view )
		{
			gridLocal = local;
			hotView = view;
		}
		
		public GridPagerAdapter(
				View local )
		{
			gridLocal = local;
		}
		
		public void setGridView(
				GridView hot )
		{
			gridHot = hot;
			hotAdapter = (BaseAdapter)gridHot.getAdapter();
		}
		
		public View getviewDownloading()
		{
			return viewDownloading;
		}
		
		@Override
		public void destroyItem(
				ViewGroup container ,
				int position ,
				Object object )
		{
			Log.d( LOG_TAG , "destroyItem,pos" + position );
			if( viewDownloading != null && isViewFromObject( viewDownloading , object ) )
			{
				container.removeView( viewDownloading );
				viewDownloading = null;
			}
		}
		
		@Override
		public int getItemPosition(
				Object object )
		{
			if( viewDownloading != null && isViewFromObject( viewDownloading , object ) && ( ( hotAdapter instanceof GridHotWallpaperAdapter && !( (GridHotWallpaperAdapter)hotAdapter ).showProgress() ) || ( hotAdapter instanceof GridLiveWallpaperAdapter && !( (GridLiveWallpaperAdapter)hotAdapter )
					.showProgress() ) ) )
			{
				return PagerAdapter.POSITION_NONE;
			}
			return PagerAdapter.POSITION_UNCHANGED;
		}
		
		@Override
		public int getCount()
		{
			if( !com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() && !FunctionConfig.isLiveWallpaperShow() )
			{
				return 1;
			}
			return 2;
		}
		
		@Override
		public Object instantiateItem(
				ViewGroup container ,
				int position )
		{
			Log.d( LOG_TAG , "instantiateItem,pos=" + position );
			if( position == 0 )
			{
				container.addView( gridLocal );
				return gridLocal;
			}
			if( com.coco.theme.themebox.util.FunctionConfig.isShowHotWallpaper() || FunctionConfig.isLiveWallpaperShow() )
			{
				if( ( hotAdapter instanceof GridHotWallpaperAdapter && ( (GridHotWallpaperAdapter)hotAdapter ).showProgress() ) || ( hotAdapter instanceof GridLiveWallpaperAdapter && ( (GridLiveWallpaperAdapter)hotAdapter )
						.showProgress() ) )
				{
					viewDownloading = View.inflate( container.getContext() , R.layout.grid_item_downloading , null );
					if( interneterr )
					{
						viewDownloading.setVisibility( View.GONE );
					}
					else
					{
						viewDownloading.setVisibility( View.VISIBLE );
					}
					container.addView( viewDownloading );
					return viewDownloading;
				}
				// ((ViewPager) container).addView(gridHot);
				( (ViewPager)container ).addView( hotView );
				return hotView;
			}
			return gridLocal;
		}
		
		@Override
		public boolean isViewFromObject(
				View view ,
				Object object )
		{
			return view == ( object );
		}
		
		@Override
		public void restoreState(
				Parcelable state ,
				ClassLoader loader )
		{
		}
		
		@Override
		public Parcelable saveState()
		{
			return null;
		}
	}
	
	private class MoreListAdapter extends BaseAdapter
	{
		
		List<ResolveInfo> mResolveInfoList = new ArrayList<ResolveInfo>();
		private PackageManager pm;
		
		public MoreListAdapter()
		{
			// TODO Auto-generated constructor stub
			getMoreWallpaper();
			pm = mContext.getPackageManager();
		}
		
		private void getMoreWallpaper()
		{
			Intent pickWallpaper = new Intent( Intent.ACTION_SET_WALLPAPER );
			List<ResolveInfo> temp = mContext.getPackageManager().queryIntentActivities( pickWallpaper , PackageManager.GET_ACTIVITIES );
			mResolveInfoList.clear();
			for( ResolveInfo info : temp )
			{
				String packagename = info.activityInfo.packageName;
				if( !LIVEPICKER.equals( packagename ) )
				{
					mResolveInfoList.add( info );
				}
			}
		}
		
		@Override
		public int getCount()
		{
			// TODO Auto-generated method stub
			return mResolveInfoList.size();
		}
		
		@Override
		public Object getItem(
				int position )
		{
			// TODO Auto-generated method stub
			return mResolveInfoList.get( position );
		}
		
		@Override
		public long getItemId(
				int position )
		{
			// TODO Auto-generated method stub
			return position;
		}
		
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			// TODO Auto-generated method stub
			if( convertView == null )
			{
				convertView = View.inflate( mContext , R.layout.push_app_item , null );
			}
			ImageView imageView = (ImageView)convertView.findViewById( R.id.app_icon );
			TextView textView1 = (TextView)convertView.findViewById( R.id.app_name );
			imageView.setImageDrawable( mResolveInfoList.get( position ).loadIcon( pm ) );
			textView1.setText( mResolveInfoList.get( position ).loadLabel( pm ).toString() );
			return convertView;
		}
	}
}
