package com.coco.theme.themebox.preview;


import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.coco.download.DownloadList;
import com.coco.theme.themebox.ActivityManager;
import com.coco.theme.themebox.MainActivity;
import com.coco.theme.themebox.PreViewGallery;
import com.coco.theme.themebox.StaticClass;
import com.coco.theme.themebox.ThemeInformation;
import com.coco.theme.themebox.database.model.DownloadStatus;
import com.coco.theme.themebox.database.model.DownloadThemeItem;
import com.coco.theme.themebox.database.service.DownloadThemeService;
import com.coco.theme.themebox.service.ThemeService;
import com.coco.theme.themebox.util.ContentConfig;
import com.coco.theme.themebox.util.DownModule;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Log;
import com.coco.theme.themebox.util.PathTool;
import com.coco.theme.themebox.util.Tools;
import com.iLoong.base.themebox.R;
import com.umeng.analytics.MobclickAgent;

import dalvik.system.VMRuntime;


public class ThemePreviewHotActivity extends Activity
{
	
	private final String LOG_TAG = "ThemePreviewHotActivity";
	private ScrollView previewScroll;
	private DownModule downModule;
	private RelativeLayout relativeNormal;
	private RelativeLayout relativeDownload;
	private ThemeInformation themeInformation;
	private SeekBar scrollGallery;
	private PreViewGallery galleryPreview;
	private String packageName;
	private String destClassName;
	private Context mContext; // xiatian add //Statistics
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		VMRuntime.getRuntime().setTargetHeapUtilization( 0.8f );
		this.requestWindowFeature( Window.FEATURE_NO_TITLE );
		ActivityManager.pushActivity( this );
		setContentView( R.layout.preview_hot );
		mContext = this; // xiatian add //Statistics
		scrollGallery = (SeekBar)findViewById( R.id.scrollGallery );
		galleryPreview = (PreViewGallery)findViewById( R.id.galleryPreview );
		downModule = DownModule.getInstance( this );
		Intent intent = this.getIntent();
		packageName = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
		destClassName = intent.getStringExtra( StaticClass.EXTRA_CLASS_NAME );
		if( destClassName == null || destClassName.equals( "" ) )
		{
			destClassName = "";
		}
		loadThemeInformation( true );
		updateShowInfo();
		{
			if( galleryPreview.getCount() > 1 )
			{
				scrollGallery.setVisibility( View.VISIBLE );
			}
			else
			{
				scrollGallery.setVisibility( View.GONE );
			}
			scrollGallery.setThumbOffset( -2 );
			scrollGallery.setEnabled( false );
			galleryPreview.setOnItemSelectedListener( new OnItemSelectedListener() {
				
				@Override
				public void onItemSelected(
						AdapterView<?> parent ,
						View view ,
						int position ,
						long id )
				{
					Log.d( LOG_TAG , "galleryPreview,position=" + position );
					scrollGallery.setProgress( position );
					scrollGallery.setMax( parent.getAdapter().getCount() - 1 );
				}
				
				@Override
				public void onNothingSelected(
						AdapterView<?> parent )
				{
					Log.d( LOG_TAG , "galleryPreview,onNothingSelected" );
					scrollGallery.setProgress( 0 );
					scrollGallery.setMax( parent.getAdapter().getCount() - 1 );
				}
			} );
		}
		previewScroll = (ScrollView)findViewById( R.id.previewScroll );
		final int apiLevel = Build.VERSION.SDK_INT;
		if( apiLevel >= 9 )
		{ // 2.3
			previewScroll.setOverScrollMode( View.OVER_SCROLL_NEVER );
		}
		relativeNormal = (RelativeLayout)findViewById( R.id.layoutNormal );
		relativeDownload = (RelativeLayout)findViewById( R.id.layoutDownload );
		reLayoutScroll();
		IntentFilter screenFilter = new IntentFilter();
		screenFilter.addAction( StaticClass.ACTION_PREVIEW_CHANGED );
		screenFilter.addAction( StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED );
		screenFilter.addAction( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED );
		registerReceiver( previewReceiver , screenFilter );
		// 注册删除事件
		IntentFilter pkgFilter = new IntentFilter();
		pkgFilter.addAction( Intent.ACTION_PACKAGE_REMOVED );
		pkgFilter.addAction( Intent.ACTION_PACKAGE_ADDED );
		pkgFilter.addDataScheme( "package" );
		registerReceiver( previewReceiver , pkgFilter );
		updateShowStatus();
		// 监听返回按钮
		LinearLayout btnReturn = (LinearLayout)findViewById( R.id.btnReturn );
		btnReturn.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View arg0 )
			{
				finish();
			}
		} );
		// 分享
		ImageButton btnShare = (ImageButton)findViewById( R.id.btnShare );
		if( !com.coco.theme.themebox.util.FunctionConfig.isShareVisible() )
		{
			btnShare.setVisibility( View.GONE );
		}
		btnShare.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				sendShare();
			}
		} );
		// 更多
		Button btnMore = (Button)findViewById( R.id.btnMore );
		btnMore.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				smoothScrollMore();
			}
		} );
		if( !FunctionConfig.isThemeMoreShow() )
		{
			btnMore.setVisibility( View.GONE );
		}
		else
		{
			btnMore.setVisibility( View.VISIBLE );
		}
		// 应用按钮
		Button btnApply = (Button)findViewById( R.id.btnApply );
		btnApply.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				//ThemePreviewHotActivity.this.getSharedPreferences( "InfoToLauncher" , Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE ).edit().putInt( "index" , -1 ).commit();
				if( FunctionConfig.getThemeApplyLauncherPackageName() == null || FunctionConfig.getThemeApplyLauncherClassName() == null )
				{
					ThemeService sv = new ThemeService( ThemePreviewHotActivity.this );
					//xiatian add start	//fix bug：解决“在主题预览界面，长按home键打开手机最近运行的应用界面干掉桌面后，再点击应用主题，桌面由于ThemeService没启动，从而收不到换主题广播（com.coco.launcher.apply_theme），没法换主题”的问题。
					//【问题原因】
					//	1、在主题预览界面，长按home键打开手机最近运行的应用界面干掉桌面后，桌面静态注册的广播ThemeService也会被干掉
					//	2、上述情况干掉桌面后，当其他应用发出action为ThemeReceiver的intent-filter中的action时，系统【不会】主动调用LauncherApplication的onCreate从而重启ThemeReceiver（目前公司的测试机和MI3等等真机，和按照官方资料说的不一样）
					//	3、所以由于ThemeReceiver没启动，桌面由于收不到美化中心发出的换主题广播（com.coco.launcher.apply_theme），因而没法换主题。
					//【解决方案】
					//	1、每次点击应用按钮时，都主动访问一下桌面和美化中心共用的那个数据库（content://" + LAUNCHER_PROVIDER_AUTHOR + "/" + "theme）
					//	2、上述步骤后，系统会启动桌面进程I/ActivityManager: Start proc com.cooee.uniex for content provider com.cooee.uniex/com.cooee.center.pub.provider.PubContentProvider
					//	3、所以上述步骤后，系统【会】主动调用LauncherApplication的onCreate从而重启ThemeReceiver，接下来美化中心再发出的换主题广播（com.coco.launcher.apply_theme），桌面便可以收到相应广播，并做换主题处理。
					//【备注】
					//	1、不考虑“使用eclipse干掉桌面进程”的情况
					//	2、上述情况下，当其他应用发出action为ThemeReceiver的intent-filter中的action时，系统会主动调用LauncherApplication的onCreate从而重启ThemeReceiver和【Launcher的onCreate从而重启launcher】。
					//	3、由于上述情况，收到action时会出现“系统会主动调用【Launcher的onCreate从而重启launcher】”这个不正常现象，故现在不考虑这个情况。
					sv.queryCurrentTheme();
					//xiatian add end
					if( FunctionConfig.isApply_theme_show_toast() )
					{
						Toast.makeText( ThemePreviewHotActivity.this , getString( R.string.toastPreviewApply , themeInformation.getDisplayName() ) , Toast.LENGTH_SHORT ).show();
					}
					sv.applyTheme( new ComponentName( packageName , destClassName ) );
					sendBroadcast( new Intent( StaticClass.ACTION_DEFAULT_THEME_CHANGED ) );
					Log.v( "ThemePreviewHotActivity" , "SendBroacast:/" + StaticClass.ACTION_DEFAULT_THEME_CHANGED );
					// @2015/09/11 ADD START 写文件到sdcard中变相通知桌面更换主题
					//writeSDFile( packageName , "changeTheme.txt" );
					// @2015/09/11 ADD END
				}
				else
				{
					ThemeService sv = new ThemeService( ThemePreviewHotActivity.this );
					sv.SaveThemes( packageName );
					Intent it = new Intent();
					ComponentName cmp = new ComponentName( FunctionConfig.getThemeApplyLauncherPackageName() , FunctionConfig.getThemeApplyLauncherClassName() );
					it.setComponent( cmp );
					it.setAction( "com.coco.launcher.action.APPLY_THEME" );
					it.putExtra( "theme_status" , 1 );
					it.putExtra( "theme" , packageName );
					mContext.startActivity( it );
					Log.v( "ThemePreviewHotActivity" , "startActivity" );
				}
				if( FunctionConfig.isExitSystemProgress() )
				{
					// @2015/03/11 ADD START 友盟统计美华中心退出
					Log.i( "startTime" , " 应用主题退出  !!!" );
					MainActivity.statisticsExitBeautyCenter( mContext );
					// @2015/03/11 ADD END
					ActivityManager.KillActivity();
				}
				else
				{
					( (Activity)mContext ).moveTaskToBack( true );
				}
			}
		} );
		// 安装
		Button btnInstall = (Button)findViewById( R.id.btnInstall );
		btnInstall.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				downModule.installApk( packageName , DownloadList.Theme_Type );
			}
		} );
		// 暂停
		relativeDownload.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				Log.v( LOG_TAG , " relativeDownload click" );
				if( relativeDownload.findViewById( R.id.linearDownload ).getVisibility() == View.VISIBLE )
				{
					// @2015/01/19 ADD START
					//Log.v( "andy test" , "[ThemePreviewHotActivity]relativeDownload Download Pause" );
					// @2015/01/19 ADD END
					Intent intent = new Intent( StaticClass.ACTION_PAUSE_DOWNLOAD_APK );
					intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , packageName );
					if( themeInformation != null )
						intent.putExtra( "apkname" , themeInformation.getDisplayName() );
					else
						intent.putExtra( "apkname" , packageName );
					sendBroadcast( intent );
					switchToPause();
					// @gaominghui2016/01/29 ADD START 友盟统计点击暂停
					if( FunctionConfig.isUmengStatistics_key() )
					{
						MobclickAgent.onEvent( mContext , "click_stop" );
					}
					// @gaominghui20162016/01/29 ADD END
				}
				else
				{
					// @2015/01/19 ADD START
					//Log.v( "andy test" , "[ThemePreviewHotActivity]relativeDownload Download start Download" );
					// @2015/01/19 ADD END
					Intent intent = new Intent( StaticClass.ACTION_START_DOWNLOAD_APK );
					intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , packageName );
					if( themeInformation != null )
						intent.putExtra( "apkname" , themeInformation.getDisplayName() );
					else
						intent.putExtra( "apkname" , packageName );
					sendBroadcast( intent );
					switchToDownloading();
					// @gaominghui2016/01/29 ADD START 友盟统计点击继续下载
					if( FunctionConfig.isUmengStatistics_key() )
					{
						MobclickAgent.onEvent( mContext , "click_contiune_download" );
					}
					// @gaominghui20162016/01/29 ADD END
				}
			}
		} );
		// 下载按钮
		Button btnDown = (Button)findViewById( R.id.btnDownload );
		btnDown.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				/// @gaominghui2016/01/29 ADD START 友盟统计点击下载，下载主题
				if( FunctionConfig.isUmengStatistics_key() )
				{
					MobclickAgent.onEvent( mContext , "click_download" );
					MobclickAgent.onEvent( mContext , "online_theme_download" );
				}
				// @gaominghui20162016/01/29 ADD END
				if( com.coco.theme.themebox.StaticClass.canDownToInternal )
				{
					File f = new File( PathTool.getAppDir() );
					int num = f.listFiles().length;
					if( num >= 5 )
					{
						recursionDeleteFile( new File( PathTool.getDownloadingDir() ) );
						Toast.makeText( mContext , mContext.getString( R.string.memory_prompt ) , Toast.LENGTH_SHORT ).show();
					}
				}
				else if( !StaticClass.isAllowDownloadWithToast( ThemePreviewHotActivity.this ) )
				{
					return;
				}
				String enginePKG = themeInformation.getEnginepackname();
				if( enginePKG != null && !enginePKG.equals( "" ) && !enginePKG.equals( "null" ) )
				{
					if( !Tools.isAppInstalled( mContext , enginePKG ) )
					{// 第三方引擎没有安装
						Tools.showNoticeDialog( mContext , enginePKG , themeInformation.getEnginedesc() , themeInformation.getEngineurl() , themeInformation.getEnginesize() );
						return;
					}
				}
				DownloadThemeService dSv = new DownloadThemeService( ThemePreviewHotActivity.this );
				DownloadThemeItem dItem = dSv.queryByPackageName( packageName , DownloadList.Theme_Type );
				if( dItem == null )
				{
					dItem = new DownloadThemeItem();
					dItem.copyFromThemeInfo( themeInformation.getInfoItem() );
					dItem.setDownloadStatus( DownloadStatus.StatusDownloading );
					dSv.insertItem( dItem );
				}
				loadThemeInformation( false );
				Intent intent = new Intent();
				intent.setAction( StaticClass.ACTION_START_DOWNLOAD_APK );
				intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , packageName );
				//<> liuhailin@2014-08-08 modify begin
				intent.putExtra( StaticClass.EXTRA_CLASS_NAME , destClassName );
				//<> liuhailin@2014-08-08 modify end
				if( themeInformation != null )
					intent.putExtra( "apkname" , themeInformation.getDisplayName() );
				else
					intent.putExtra( "apkname" , packageName );
				sendBroadcast( intent );
				Log.v( "********" , "pressDown" );
				updateShowStatus();
			}
		} );
		// 购买
		//		Button butBuy = (Button)findViewById( R.id.btnBuy );
		//		butBuy.setOnClickListener( new View.OnClickListener() {
		//			
		//			@Override
		//			public void onClick(
		//					View v )
		//			{
		//				// TODO Auto-generated method stub
		//				CooeePaymentInfo paymentInfo = new CooeePaymentInfo();
		//				paymentInfo.setPrice( themeInformation.getPrice() );
		//				paymentInfo.setPayDesc( themeInformation.getDisplayName() );
		//				String payid = themeInformation.getPricePoint();
		//				Log.v( "themebox" , "payid theme= " + payid );
		//				if( payid == null || payid.equals( "" ) || payid.equals( "null" ) )
		//				{
		//					paymentInfo.setPayId( FunctionConfig.getCooeePayID( themeInformation.getPrice() ) );
		//				}
		//				else
		//				{
		//					paymentInfo.setPayId( payid );
		//				}
		//				paymentInfo.setCpId( themeInformation.getThirdparty() );
		//				paymentInfo.setPayType( CooeePaymentInfo.PAY_TYPE_EVERY_TIME );
		//				paymentInfo.setPayName( themeInformation.getDisplayName() );
		//				paymentInfo.setNotify( new PaymentResultReceiver() );
		//				CooeePayment.getInstance().startPayService( ThemePreviewHotActivity.this , paymentInfo );
		//			}
		//		} );
		// 删除
		Button btnDelete = (Button)findViewById( R.id.btnDelete );
		btnDelete.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				Log.d( LOG_TAG , "btnDelete" );
				Intent intent = new Intent( StaticClass.ACTION_PAUSE_DOWNLOAD_APK );
				intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , packageName );
				if( themeInformation != null )
					intent.putExtra( "apkname" , themeInformation.getDisplayName() );
				else
					intent.putExtra( "apkname" , packageName );
				sendBroadcast( intent );
				DownloadThemeService dSv = new DownloadThemeService( ThemePreviewHotActivity.this );
				dSv.deleteItem( packageName , DownloadList.Theme_Type );
				intent = new Intent( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED );
				intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , packageName );
				sendBroadcast( intent );
				updateShowStatus();
			}
		} );
		// 卸载按钮
		Button btnUninstall = (Button)findViewById( R.id.btnUninstall );
		btnUninstall.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// @gaominghui2016/01/29 ADD START 友盟统计主题卸载
				if( FunctionConfig.isUmengStatistics_key() )
				{
					MobclickAgent.onEvent( mContext , "online_theme_uninstall" );
				}
				// @gaominghui20162016/01/29 ADD END
				removePackage();
			}
		} );
	}
	
	@Override
	protected void onNewIntent(
			Intent intent )
	{
		// TODO Auto-generated method stub
		super.onNewIntent( intent );
		packageName = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
		destClassName = intent.getStringExtra( StaticClass.EXTRA_CLASS_NAME );
		if( destClassName == null || destClassName.equals( "" ) )
		{
			destClassName = "";
		}
		loadThemeInformation( true );
		updateShowInfo();
		updateShowStatus();
		if( previewScroll.getScrollY() != 0 )
		{
			previewScroll.smoothScrollTo( 0 , 0 );
		}
	}
	
	private void updateProgressSize()
	{
		if( findViewById( R.id.linearDownload ).getVisibility() == View.VISIBLE )
		{
			ProgressBar progressBar = (ProgressBar)findViewById( R.id.progressBarDown );
			progressBar.setProgress( themeInformation.getDownloadPercent() );
			// @2015/01/19 ADD START
			//Log.i( "andy test" , "progressBar.getProgress() = " + progressBar.getProgress() );
			// @2015/01/19 ADD END
			TextView text = (TextView)findViewById( R.id.textDownPercent );
			text.setText( getString( R.string.textDownloading , themeInformation.getDownloadPercent() ) );
		}
		else
		{
			ProgressBar progressBar = (ProgressBar)findViewById( R.id.progressBarPause );
			progressBar.setProgress( themeInformation.getDownloadPercent() );
			// @2015/01/19 ADD START
			//Log.i( "andy test" , "progressBarPause  progressBar.getProgress() = " + progressBar.getProgress() );
			// @2015/01/19 ADD END
			TextView text = (TextView)findViewById( R.id.textPausePercent );
			text.setText( getString( R.string.textPause , themeInformation.getDownloadPercent() ) );
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
		// @2016/01/29 ADD START 友盟统计
		if( FunctionConfig.isUmengStatistics_key() )
		{
			//Log.i( LOG_TAG , "ThemePreviewHotActivity onResume !!!" );
			MobclickAgent.onPageStart( "ThemePreviewHotActivity" );
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
			//Log.i( LOG_TAG , "ThemePreviewHotActivity onPause !!!" );
			MobclickAgent.onPageEnd( "ThemePreviewHotActivity" );
			MobclickAgent.onPause( this );
		}
		// @2016/01/29 ADD END
	}
	
	@Override
	protected void onDestroy()
	{
		Log.i( "ThemePreviewHotActivity" , "onDestroy!!!" );
		ActivityManager.popupActivity( this );
		unregisterReceiver( previewReceiver );
		// @gaominghui2014/11/21 ADD START解决无响应问题
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				downModule.dispose();
			}
		} ).start();
		// @2014/11/21 ADD END by gaominghui
		if( galleryPreview.getAdapter() != null )
		{
			BaseAdapter adapter = (BaseAdapter)galleryPreview.getAdapter();
			if( adapter instanceof ThemePreviewLocalAdapter )
			{
				( (ThemePreviewLocalAdapter)adapter ).onDestory();
			}
			else if( adapter instanceof ThemePreviewHotAdapter )
			{
				( (ThemePreviewHotAdapter)adapter ).onDestory();
			}
		}
		super.onDestroy();
	}
	
	/**
	 *
	 * @see android.app.Activity#onStop()
	 * @auther tangliang  2016年1月20日
	 */
	@Override
	protected void onStop()
	{
		// TODO Auto-generated method stub
		Log.i( "ThemePreviewHotActivity" , "onStop!!!" );
		super.onStop();
	}
	
	private void updateShowInfo()
	{
		TextView text = (TextView)findViewById( R.id.textAppName );
		text.setText( themeInformation.getDisplayName() );
		TextView viewAuthor = (TextView)findViewById( R.id.author );
		TextView viewInfo = (TextView)findViewById( R.id.info );
		if( !FunctionConfig.isThemeMoreShow() )
		{
			viewAuthor.setVisibility( View.GONE );
			viewInfo.setVisibility( View.GONE );
		}
		else
		{
			viewAuthor.setVisibility( View.VISIBLE );
			viewInfo.setVisibility( View.VISIBLE );
			String author = getString( R.string.previewThemeSize ) + themeInformation.getApplicationSize() / 1024 + "KB     " + getString( R.string.previewThemeAuther ) + themeInformation
					.getAuthor( mContext );
			viewAuthor.setText( author );
			if( FunctionConfig.isIntroductionVisible() )
			{
				String info = getString( R.string.previewIntroduction ) + "\n" + themeInformation.getIntroduction();
				viewInfo.setText( info );
			}
			else
			{
				viewInfo.setVisibility( View.GONE );
			}
		}
	}
	
	private void updateInforButton()
	{
		findViewById( R.id.btnDelete ).setVisibility( View.GONE );
		findViewById( R.id.btnUninstall ).setVisibility( View.GONE );
		if( themeInformation.isInstalled( this ) )
		{
			if( themeInformation.isSystem() )
			{
				return;
			}
			ThemeService service = new ThemeService( this );
			ComponentName curTheme = service.queryCurrentTheme();
			if( themeInformation.isComponent( curTheme ) )
			{
				return;
			}
			findViewById( R.id.btnUninstall ).setVisibility( View.VISIBLE );
			return;
		}
		if( !themeInformation.isDownloaded( this ) )
		{
			return;
		}
		if( themeInformation.getDownloadStatus() != DownloadStatus.StatusDownloading )
		{
			findViewById( R.id.btnDelete ).setVisibility( View.VISIBLE );
		}
	}
	
	private void updateShowStatus()
	{
		updateInforButton();
		findViewById( R.id.btnSetting ).setVisibility( View.GONE );
		findViewById( R.id.btnShare ).setVisibility( View.GONE );
		relativeDownload.setClickable( false );
		Log.v( "test" , "themeInformation.isInstalled = " + themeInformation.isInstalled( this ) );
		if( themeInformation.isInstalled( this ) )
		{
			relativeDownload.setVisibility( View.GONE );
			relativeNormal.setVisibility( View.VISIBLE );
			relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.GONE );
			Button button = (Button)relativeNormal.findViewById( R.id.btnApply );
			//button.setTextColor( getResources().getDrawable( R.drawable.applay_text_select ) );
			button.setClickable( true );
			button.setVisibility( View.VISIBLE );
			relativeNormal.findViewById( R.id.btnInstall ).setVisibility( View.GONE );
			relativeNormal.findViewById( R.id.btnBuy ).setVisibility( View.GONE );
			if( com.coco.theme.themebox.util.FunctionConfig.isShareVisible() )
			{
				findViewById( R.id.btnShare ).setVisibility( View.VISIBLE );
			}
			return;
		}
		if( !themeInformation.isDownloaded( this ) )
		{
			boolean ispay = Tools.isContentPurchased( this , DownloadList.Theme_Type , packageName );
			if( FunctionConfig.isPriceVisible() && themeInformation.getPrice() > 0 && !ispay )
			{
				relativeNormal.setVisibility( View.VISIBLE );
				relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnApply ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnInstall ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnBuy ).setVisibility( View.VISIBLE );
				return;
			}
			else
			{
				relativeDownload.setVisibility( View.GONE );
				relativeNormal.setVisibility( View.VISIBLE );
				relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.VISIBLE );
				relativeNormal.findViewById( R.id.btnApply ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnInstall ).setVisibility( View.GONE );
				relativeNormal.findViewById( R.id.btnBuy ).setVisibility( View.GONE );
				return;
			}
		}
		if( themeInformation.getDownloadStatus() == DownloadStatus.StatusFinish )
		{
			relativeDownload.setVisibility( View.GONE );
			relativeNormal.setVisibility( View.VISIBLE );
			relativeNormal.findViewById( R.id.btnDownload ).setVisibility( View.GONE );
			relativeNormal.findViewById( R.id.btnApply ).setVisibility( View.GONE );
			if( FunctionConfig.isInatall_silently_ThemeApk() )
			{
				Button btnApplay = (Button)relativeNormal.findViewById( R.id.btnApply );
				//btnApplay.setTextColor( Color.GRAY );
				btnApplay.setClickable( false );
				relativeNormal.findViewById( R.id.btnApply ).setVisibility( View.VISIBLE );
			}
			else
			{
				relativeNormal.findViewById( R.id.btnInstall ).setVisibility( View.VISIBLE );
			}
			relativeNormal.findViewById( R.id.btnBuy ).setVisibility( View.GONE );
			return;
		}
		relativeDownload.setClickable( true );
		if( themeInformation.getDownloadStatus() == DownloadStatus.StatusDownloading )
		{
			switchToDownloading();
		}
		else
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
		updateProgressSize();
	}
	
	private void switchToPause()
	{
		relativeDownload.setVisibility( View.VISIBLE );
		relativeNormal.setVisibility( View.GONE );
		relativeDownload.findViewById( R.id.linearDownload ).setVisibility( View.GONE );
		relativeDownload.findViewById( R.id.linearPause ).setVisibility( View.VISIBLE );
		updateProgressSize();
	}
	
	private void loadThemeInformation(
			boolean reloadGallery )
	{
		ThemeService service = new ThemeService( this );
		themeInformation = service.queryTheme( packageName , destClassName );
		if( themeInformation.isInstalled( this ) )
		{
			Context dstContext = null;
			try
			{
				dstContext = createPackageContext( packageName , Context.CONTEXT_IGNORE_SECURITY );
			}
			catch( NameNotFoundException e )
			{
				e.printStackTrace();
				return;
			}
			Log.v( LOG_TAG , "2222222222222222destClassName = " + destClassName );
			ContentConfig destContent = new ContentConfig();
			if( destContent.loadConfig( dstContext , destClassName ) )
			{
				themeInformation.loadInstallDetail( dstContext , destContent );
				if( reloadGallery )
				{
					galleryPreview.setAdapter( new ThemePreviewLocalAdapter( this , destContent , dstContext ) );
				}
				return;
			}
		}
		if( reloadGallery )
		{
			galleryPreview.setAdapter( new ThemePreviewHotAdapter( this , packageName , downModule ) );
		}
	}
	
	private String queryClassName(
			String pkgName )
	{
		ThemeService service = new ThemeService( this );
		ComponentName comName = service.queryComponent( pkgName );
		if( comName == null )
		{
			return "";
		}
		return comName.getClassName();
	}
	
	// @gaominghui2015/06/10 ADD START 0002952: phenix美化中心，美化中心无响应
	private static final int UPDATA_SHOW_STATUS = 1;
	private static final int LOAD_IMAGE = 2;
	//private static final int SILENT_INSTALL_SUCESS = 3;
	//private static final int SILENT_INSTALL_FAILED = 4;
	private Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			// TODO Auto-generated method stub
			super.handleMessage( msg );
			switch( msg.what )
			{
				case UPDATA_SHOW_STATUS:
					updateShowStatus();
					break;
				case LOAD_IMAGE:
					( (ThemePreviewHotAdapter)galleryPreview.getAdapter() ).reload();
					break;
				/*case SILENT_INSTALL_SUCESS:
					relativeNormal.findViewById( R.id.btnApply ).setVisibility( View.VISIBLE );
					break;
				case SILENT_INSTALL_FAILED:
					relativeNormal.findViewById( R.id.btnInstall ).setVisibility( View.VISIBLE );
					break;*/
				default:
					break;
			}
		}
	};
	// @gaominghui2015/06/10 ADD END
	private BroadcastReceiver previewReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			String actionName = intent.getAction();
			Log.d( LOG_TAG , "action=" + actionName );
			if( actionName.equals( StaticClass.ACTION_PREVIEW_CHANGED ) )
			{
				SpinnerAdapter apt = galleryPreview.getAdapter();
				if( apt != null && apt instanceof ThemePreviewHotAdapter )
				{
					// @gaominghui2015/06/10 ADD START 0002952: phenix美化中心，美化中心无响应
					//( (ThemePreviewHotAdapter)apt ).reload();
					mHandler.sendEmptyMessage( LOAD_IMAGE );
					// @gaominghui2015/06/10 ADD END  
					if( galleryPreview.getCount() > 1 )
					{
						scrollGallery.setVisibility( View.VISIBLE );
					}
					else
					{
						scrollGallery.setVisibility( View.GONE );
					}
					scrollGallery.setMax( galleryPreview.getCount() );
					scrollGallery.setProgress( galleryPreview.getSelectedItemPosition() );
				}
			}
			else if( actionName.equals( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED ) )
			{
				String name = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
				if( name.equals( packageName ) )
				{
					loadThemeInformation( false );
					// @gaominghui 2015/06/11 ADD START 0002952: phenix美化中心，美化中心无响应
					/*updateShowStatus();*/
					mHandler.sendEmptyMessage( UPDATA_SHOW_STATUS );
					// @gaominghui 2015/06/11 ADD END
				}
			}
			else if( actionName.equals( StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED ) )
			{
				if( packageName.equals( intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ) ) )
				{
					themeInformation.setDownloadSize( intent.getIntExtra( StaticClass.EXTRA_DOWNLOAD_SIZE , 0 ) );
					themeInformation.setTotalSize( intent.getIntExtra( StaticClass.EXTRA_TOTAL_SIZE , 0 ) );
					updateProgressSize();
				}
			}
			else if( Intent.ACTION_PACKAGE_REMOVED.equals( actionName ) )
			{
				String actionPkgName = intent.getData().getSchemeSpecificPart();
				if( actionPkgName.equals( packageName ) )
				{
					finish();
				}
			}
			else if( Intent.ACTION_PACKAGE_ADDED.equals( actionName ) )
			{
				String actionPkgName = intent.getData().getSchemeSpecificPart();
				Log.v( "test" , "actionPkgName = " + actionPkgName );
				if( actionPkgName.equals( packageName ) )
				{
					destClassName = queryClassName( packageName );
					Log.v( "test" , "destClassName = " + destClassName );
					loadThemeInformation( true );
					updateShowInfo();
					// @gaominghui 2015/06/11 ADD START 0002952: phenix美化中心，美化中心无响应
					/*updateShowStatus();*/
					mHandler.sendEmptyMessage( UPDATA_SHOW_STATUS );
					// @gaominghui 2015/06/11 ADD END
					//updateShowStatus();
				}
			}
		}
	};
	// private void reLayoutScroll() {
	// previewScroll.getViewTreeObserver().addOnGlobalLayoutListener(
	// new OnGlobalLayoutListener() {
	//
	// @Override
	// public void onGlobalLayout() {
	// findViewById(R.id.preview_picture).getLayoutParams().height =
	// previewScroll
	// .getHeight();
	// }
	// });
	// }
	private boolean drawScroll = true;
	
	private void reLayoutScroll()
	{
		previewScroll.getViewTreeObserver().addOnGlobalLayoutListener( new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout()
			{
				final int pictureHeight = findViewById( R.id.preview_picture ).getLayoutParams().height;
				Log.d( "PreviewHotActivity" , "reLayoutScroll,pictureH=" + pictureHeight + ",scrollH=" + previewScroll.getHeight() );
				findViewById( R.id.preview_picture ).getLayoutParams().height = previewScroll.getHeight();
				if( pictureHeight == previewScroll.getHeight() )
				{
					drawScroll = true;
					previewScroll.getViewTreeObserver().removeGlobalOnLayoutListener( this );
				}
				else
				{
					drawScroll = false;
				}
			}
		} );
		previewScroll.getViewTreeObserver().addOnPreDrawListener( new OnPreDrawListener() {
			
			@Override
			public boolean onPreDraw()
			{
				if( drawScroll )
				{
					previewScroll.getViewTreeObserver().removeOnPreDrawListener( this );
				}
				return drawScroll;
			}
		} );
	}
	
	private void smoothScrollMore()
	{
		if( previewScroll.getScrollY() != 0 )
		{
			previewScroll.smoothScrollTo( 0 , 0 );
		}
		else
		{
			previewScroll.smoothScrollTo( 0 , previewScroll.getMaxScrollAmount() );
		}
	}
	
	private void sendShare()
	{
		Intent intent = new Intent( Intent.ACTION_SEND );
		intent.setType( "image/*" );// "text/plain"
		intent.putExtra( Intent.EXTRA_SUBJECT , getString( R.string.shareSubject ) );
		intent.putExtra( Intent.EXTRA_TEXT , getString( R.string.shareText , themeInformation.getDisplayName() ) );
		if( saveThumb() )
		{
			intent.putExtra( Intent.EXTRA_STREAM , Uri.fromFile( new File( PathTool.getThumbTempFile() ) ) );
		}
		intent.putExtra( "sms_body" , getString( R.string.shareText , themeInformation.getDisplayName() ) );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		startActivity( Intent.createChooser( intent , getString( R.string.shareSubject ) ) );
	}
	
	private boolean saveThumb()
	{
		String thumbPath = PathTool.getThumbTempFile();
		if( thumbPath.equals( "" ) )
		{
			return false;
		}
		Context dstContext = null;
		try
		{
			dstContext = createPackageContext( packageName , Context.CONTEXT_IGNORE_SECURITY );
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
			return false;
		}
		ContentConfig destContent = new ContentConfig();
		destContent.loadConfig( dstContext , destClassName );
		return destContent.saveThumb( dstContext , thumbPath );
	}
	
	private void removePackage()
	{
		String delApkPackname = "package:" + packageName;
		Uri packageURI = Uri.parse( delApkPackname );
		Intent uninstallIntent = new Intent( Intent.ACTION_DELETE , packageURI );
		startActivity( uninstallIntent );
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
	// @2015/09/11 ADD START
	/** 
	* 写入内容到SD卡中的txt文本中 
	* str为内容 
	*/
	/*public void writeSDFile(
			String str ,
			String fileName )
	{
		try
		{
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Coco/";
			File file = new File( path , "changeTheme.txt" );
			if( file.exists() )
			{
				FileOutputStream fos = new FileOutputStream( file );
				fos.write( str.getBytes() );
				fos.flush();
				fos.close();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}*/
	// @2015/09/11 ADD END
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
	//					Toast.makeText( ThemePreviewHotActivity.this , "计费成功" , Toast.LENGTH_SHORT ).show();
	//					Tools.writePurchasedData( ThemePreviewHotActivity.this , DownloadList.Theme_Type , packageName );
	//					// Message message = iapHandler.obtainMessage(BILLING_FINISH);
	//					// message.sendToTarget();
	//					Intent intent = new Intent( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED );
	//					intent.putExtra( StaticClass.EXTRA_PACKAGE_NAME , packageName );
	//					mContext.sendBroadcast( intent );
	//					break;
	//				case CooeePaymentResultNotify.COOEE_PAYMENT_RESULT_FAIL:
	//					Toast.makeText( ThemePreviewHotActivity.this , "计费失败" + paymentInfo.getVersionName() , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case CooeePaymentResultNotify.COOEE_PAYMENT_RESULT_CANCEL_BY_USER:
	//					Toast.makeText( ThemePreviewHotActivity.this , "用户取消付费" , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case 1:
	//					Toast.makeText( ThemePreviewHotActivity.this , "配置免费" , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case 2:
	//					Toast.makeText( ThemePreviewHotActivity.this , "不需要重复计费" , Toast.LENGTH_SHORT ).show();
	//					break;
	//				case 3:
	//					Toast.makeText( ThemePreviewHotActivity.this , "无可用指令" , Toast.LENGTH_SHORT ).show();
	//					break;
	//			}
	//		}
	//	}
}
