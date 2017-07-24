package com.cooee.favorites.news;


import java.io.InputStream;
import java.util.Random;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cooee.dynamicload.DLBasePluginActivity;
import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.FavoritesPlugin;
import com.cooee.favorites.R;
import com.cooee.favorites.manager.AdPlaceIdManager;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.utils.SystemBarTintManager;
import com.cooee.favorites.view.NewsWebView;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;
import com.umeng.analytics.MobclickAgent;


public class NewsActivity extends DLBasePluginActivity
{
	
	int[] color = {
			0xffd8671c ,
			0xffa0a31d ,
			0xffe0b62f ,
			0xff5bb03d ,
			0xffce2832 ,
			0xffa054c0 ,
			0xff274ac6 ,
			0xff3d89ac ,
			0xffd3a106 ,
			0xff4d9434 ,
			0xffd97d14 ,
			0xffb4b82a ,
			0xff972fc4 ,
			0xff207298 ,
			0xffe44738 ,
			0xff3c5dd1 };
	int mCurrColor = color[0];
	NewsWebView mWebView;
	String mShare;
	private boolean isFirstLoad = true;
	private RelativeLayout mView;
	private final int Statusbar_View_id = 1000;
	private AdBaseView mbannerView = null;
	private FrameLayout bannerParent = null;
	private boolean isAdReady = false;
	private int mProgress = 0;
	private SharedPreferences mPreferences;
	private final int DAY_MODE = 0;
	private final int NIGHT_MODE = 1;
	private View changeSizeDialogView;
	private WebSettings settings;
	private View mCover;
	private int currentselectMarkItem = 1;
	private int currentMode;
	private Handler mHandler = new Handler();
	private boolean isPageFinish = false;
	private String nightCode = null;
	private String dayCode = null;
	private ChasingDots mChasingDotsDrawable;
	
	@Override
	public void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		mPreferences = PreferenceManager.getDefaultSharedPreferences( that );
		mView = (RelativeLayout)LayoutInflater.from( that ).cloneInContext( that ).inflate( R.layout.news_content , null );
		Animation animation = AnimationUtils.loadAnimation( that , R.anim.slide_in_from_right );
		mView.startAnimation( animation );
		that.setContentView( mView );
		Random mRandom = new Random();
		mCurrColor = color[mRandom.nextInt( color.length )];
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
		{
			setTranslucentStatus( true );
			SystemBarTintManager tintManager = new SystemBarTintManager( that );
			tintManager.setStatusBarTintEnabled( true );
			tintManager.setStatusBarTintColor( 0x00000000 );
			//			tintManager.setStatusBarTintResource( mCurrColor );//通知栏所需颜色
		}
		Intent intent = that.getIntent();
		String url = intent.getStringExtra( "com.cooee.news.url" );
		mShare = intent.getStringExtra( "com.cooee.news.share" );
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT )
		{
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , getStatusBarHeight() );
			layoutParams.addRule( RelativeLayout.ALIGN_PARENT_TOP , R.id.title_action_bar );
			View view = new View( that );
			view.setId( Statusbar_View_id );
			view.setBackgroundColor( mCurrColor );
			mView.addView( view , layoutParams );
			View actionBar = mView.findViewById( R.id.title_action_bar );
			if( actionBar.getLayoutParams() != null )
			{
				( (RelativeLayout.LayoutParams)actionBar.getLayoutParams() ).addRule( RelativeLayout.BELOW , Statusbar_View_id );
			}
		}
		setActionBar();
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.MATCH_PARENT );
		//		mLinearLayout.LayoutParams layoutParams = new LinearLayout.lay( source )
		layoutParams.addRule( RelativeLayout.BELOW , R.id.title_action_bar );
		View loading = mView.findViewById( R.id.loading );
		if( currentMode == DAY_MODE )
		{
			loading.setBackgroundColor( 0xffffffff );
		}
		else
		{
			loading.setBackgroundColor( 0xff111111 );
		}
		ImageView imageView = (ImageView)mView.findViewById( R.id.progressView );
		mChasingDotsDrawable = new ChasingDots();
		mChasingDotsDrawable.setColor( 0xfff88632 );
		imageView.setImageDrawable( mChasingDotsDrawable );
		loading.setVisibility( View.VISIBLE );
		mWebView = new NewsWebView( that );
		mView.addView( mWebView , 0 , layoutParams );
		initAd();
		settings = mWebView.getSettings();
		settings.setJavaScriptEnabled( true );
		settings.setSupportZoom( true );
		settings.setDomStorageEnabled(true);//yangmengchao add	//解决“点击酷生活新闻二级界面底部精彩推荐中的新闻，无法打开相应新闻”的问题 【c_0004715】
		currentselectMarkItem = mPreferences.getInt( "fontsize" , 1 );
		settings.setTextZoom( getFontZoom( currentselectMarkItem ) );
		mWebView.setWebViewClient( new WebViewClient() {
			
			public boolean shouldOverrideUrlLoading(
					WebView view ,
					String url )
			{ //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
				Log.v( "lvjiangbin" , "shouldOverrideUrlLoading=" + url );
				if( isFirstLoad )
				{
					view.loadUrl( url );
					isFirstLoad = false;
					return true;
				}
				else
				{
					return false;
				}
			}
			
			public void onPageStarted(
					WebView view ,
					String url ,
					android.graphics.Bitmap favicon )
			{
				super.onPageStarted( view , url , favicon );
				View loading = mView.findViewById( R.id.loading );
				if( currentMode == DAY_MODE )
				{
					loading.setBackgroundColor( 0xffffffff );
				}
				else
				{
					loading.setBackgroundColor( 0xff111111 );
				}
				mChasingDotsDrawable.start();
				loading.setVisibility( View.VISIBLE );
				isPageFinish = false;
			};
			
			@Override
			@TargetApi( 21 )
			public WebResourceResponse shouldInterceptRequest(
					WebView view ,
					android.webkit.WebResourceRequest request )
			{
				return super.shouldInterceptRequest( view , request );
			};
			
			@Override
			@Deprecated
			public WebResourceResponse shouldInterceptRequest(
					WebView view ,
					String url )
			{
				// TODO Auto-generated method stub
				return super.shouldInterceptRequest( view , url );
			}
			
			public void onPageFinished(
					WebView view ,
					String url )
			{
				super.onPageFinished( view , url );
				Log.v( "lvjiangbin" , "onPageFinished mProgress = " + mProgress );
				if( mProgress == 100 && !isPageFinish )
				{
					isPageFinish = true;
					Log.v( "lvjiangbin" , " onPageFinished setInitialScale = " + mWebView.getScale() );
					changeWebViewMode();
					Log.d( "lvjiangbin" , " onPageFinished setInitialScale = " + mWebView.getScale() );
					mHandler.postDelayed( new Runnable() {
						
						public void run()
						{
							View loading = mView.findViewById( R.id.loading );
							mChasingDotsDrawable.stop();
							loading.setVisibility( View.GONE );
						}
					} , 500 );//这里延时，changewebviewmode调用的是js代码，等待完成后，在消失动画，否则夜间模式会闪现一下白色
				}
			};
		} );
		mWebView.setWebChromeClient( new WebChromeClient() {
			
			public void onProgressChanged(
					WebView view ,
					int progress )
			{
				// Activity和Webview根据加载程度决定进度条的进度大小  
				//					// 当加载到100%的时候 进度条自动消失  
				Log.v( "lvjiangbin" , "progress progress=" + progress );
				mProgress = progress;
				//cheyingkun add start	//解决“4.0手机新闻二级界面经常刷不出来”的问题【i_0014780】
				if( mProgress == 100 && !isPageFinish )
				{
					isPageFinish = true;
					Log.v( "lvjiangbin" , " onProgressChanged setInitialScale = " + mWebView.getScale() );
					changeWebViewMode();
					Log.d( "lvjiangbin" , " onProgressChanged setInitialScale = " + mWebView.getScale() );
					mHandler.postDelayed( new Runnable() {
						
						public void run()
						{
							View loading = mView.findViewById( R.id.loading );
							mChasingDotsDrawable.stop();
							loading.setVisibility( View.GONE );
						}
					} , 500 );//这里延时，changewebviewmode调用的是js代码，等待完成后，在消失动画，否则夜间模式会闪现一下白色
				}
				//cheyingkun add end
			}
		} );
		mWebView.setDownloadListener( new DownloadListener() {
			
			@Override
			public void onDownloadStart(
					String url ,
					String userAgent ,
					String contentDisposition ,
					String mimetype ,
					long contentLength )
			{
				Uri uri = Uri.parse( url );
				Intent intent = new Intent( Intent.ACTION_VIEW , uri );
				that.startActivity( intent );
			}
		} );
		mWebView.setListener( new NewsWebView.onScrollListener() {
			
			@Override
			public void outsideScroll()
			{
				// TODO Auto-generated method stub
				showAdBanner();//显示广告
			}
			
			@Override
			public void insideScroll()
			{
				// TODO Auto-generated method stub
				hideAdBanner();//不显示广告
			}
			
			@Override
			public void flingListener()
			{
				// TODO Auto-generated method stub
				finishWithAnimation();//右滑退出
			}
		} );
		mWebView.loadUrl( url );
		mWebView.setBackgroundColor( 0 );
	}
	
	public void changeWebViewMode()
	{
		View loading = mView.findViewById( R.id.loading );
		if( loading.getVisibility() == View.VISIBLE )
		{
			if( currentMode == DAY_MODE )
			{
				loading.setBackgroundColor( 0xffffffff );
			}
			else
			{
				loading.setBackgroundColor( 0xff111111 );
			}
		}
		if( currentMode == DAY_MODE )
		{
			try
			{
				if( dayCode == null )
				{
					InputStream is = getResources().openRawResource( R.raw.day );
					byte[] buffer = new byte[is.available()];
					is.read( buffer );
					is.close();
					dayCode = Base64.encodeToString( buffer , Base64.NO_WRAP );
				}
				mWebView.loadUrl( "javascript:(function() {" + "var parent = document.getElementsByTagName('head').item(0);" + "var style = document.createElement('style');" + "style.type = 'text/css';" + "style.innerHTML = window.atob('" + dayCode + "');" + "parent.appendChild(style)" + "})();" );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			try
			{
				if( nightCode == null )
				{
					InputStream is = getResources().openRawResource( R.raw.night );
					byte[] buffer = new byte[is.available()];
					is.read( buffer );
					is.close();
					nightCode = Base64.encodeToString( buffer , Base64.NO_WRAP );
				}
				mWebView.loadUrl( "javascript:(function() {" + "var parent = document.getElementsByTagName('head').item(0);" + "var style = document.createElement('style');" + "style.type = 'text/css';" + "style.innerHTML = window.atob('" + nightCode + "');" + "parent.appendChild(style)" + "})();" );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
	public void initAd()
	{
		final FavoritesManager mFavoritesManager = FavoritesManager.getInstance();
		bannerParent = (FrameLayout)mView.findViewById( R.id.banner );
		mView.findViewById( R.id.banner_cancle ).setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				if( bannerParent.getVisibility() == View.VISIBLE )
				{
					bannerParent.setVisibility( View.GONE );
					isAdReady = false;
				}
				FavoritesConfig config = mFavoritesManager.getConfig();
				if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
				{
					MobclickAgent.onEvent( mFavoritesManager.getContainerContext() , "Ad_closeclick" );
				}
				try
				{
					StatisticsExpandNew.onCustomEvent(
							mFavoritesManager.getContainerContext() ,
							"Ad_closeclick" ,
							FavoritesPlugin.SN ,
							FavoritesPlugin.APPID ,
							CooeeSdk.cooeeGetCooeeId( mFavoritesManager.getContainerContext() ) ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName ,
							FavoritesPlugin.UPLOAD_VERSION + "" ,
							null );
				}
				catch( NoSuchMethodError e )
				{
					try
					{
						StatisticsExpandNew.onCustomEvent(
								mFavoritesManager.getContainerContext() ,
								"Ad_closeclick" ,
								FavoritesPlugin.SN ,
								FavoritesPlugin.APPID ,
								CooeeSdk.cooeeGetCooeeId( mFavoritesManager.getContainerContext() ) ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName );
					}
					catch( NoSuchMethodError e1 )
					{
						StatisticsExpandNew.onCustomEvent( mFavoritesManager.getContainerContext() , "Ad_closeclick" , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName );
					}
				}
			}
		} );
		String adplaceID = AdPlaceIdManager.getInstance().getNewsAdId()[1];
		KmobManager.setContext( mFavoritesManager.getContainerContext().getApplicationContext() ); //gaominghui add kmob初始化context
		KmobManager.setChannel( mFavoritesManager.getSn() );
		mbannerView = KmobManager.createBanner( adplaceID , that );
		mbannerView.addAdViewListener( new AdViewListener() {
			
			@Override
			public void onAdShow(
					String info )
			{
				Log.v( "" , "AdViewListener onAdShow info " + info );
			}
			
			@Override
			public void onAdReady(
					String space_id )
			{
				Log.v( "" , "AdViewListener onAdReady space_id " + space_id );
				if( mbannerView.getParent() == null )
				{
					FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.WRAP_CONTENT , FrameLayout.LayoutParams.WRAP_CONTENT );
					params.gravity = Gravity.CENTER;
					bannerParent.addView( mbannerView , 0 , params );
				}
				isAdReady = true;
				mbannerView.setOnClickListener( new View.OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						// TODO Auto-generated method stub
						Log.v( "" , "AdViewListener onAdReady mbannerView.getSpace_id() " + mbannerView.getSpace_id() );
						KmobManager.onClickDone( mbannerView.getSpace_id() , true );
					}
				} );
				if( mWebView != null && mWebView.isOutsideArae() )
				{
					showAdBanner();
				}
			}
			
			@Override
			public void onAdFailed(
					String reason )
			{
			}
			
			@Override
			public void onAdClick(
					String arg0 )
			{
				Log.v( "" , "AdViewListener onAdClick arg0 " + arg0 );
				FavoritesConfig config = mFavoritesManager.getConfig();
				if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
				{
					MobclickAgent.onEvent( mFavoritesManager.getContainerContext() , "Ad_click_secondpage" );
				}
				try
				{
					StatisticsExpandNew.onCustomEvent(
							mFavoritesManager.getContainerContext() ,
							"Ad_click_secondpage" ,
							FavoritesPlugin.SN ,
							FavoritesPlugin.APPID ,
							CooeeSdk.cooeeGetCooeeId( mFavoritesManager.getContainerContext() ) ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName ,
							FavoritesPlugin.UPLOAD_VERSION + "" ,
							null );
				}
				catch( NoSuchMethodError e )
				{
					try
					{
						StatisticsExpandNew.onCustomEvent(
								mFavoritesManager.getContainerContext() ,
								"Ad_click_secondpage" ,
								FavoritesPlugin.SN ,
								FavoritesPlugin.APPID ,
								CooeeSdk.cooeeGetCooeeId( mFavoritesManager.getContainerContext() ) ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName );
					}
					catch( NoSuchMethodError e1 )
					{
						StatisticsExpandNew.onCustomEvent( mFavoritesManager.getContainerContext() , "Ad_click_secondpage" , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName );
					}
				}
			}
			
			@Override
			public void onAdClose(
					String info )
			{
			}
			
			@Override
			public void onAdCancel(
					String info )
			{
			}
		} );
	}
	
	private void showAdBanner()
	{
		if( mView.findViewById( R.id.loading ).getVisibility() != View.VISIBLE && isAdReady && bannerParent != null && bannerParent.getVisibility() != View.VISIBLE )
		{
			bannerParent.setVisibility( View.VISIBLE );
			bannerParent.setTranslationY( mView.getHeight() );
			ObjectAnimator animator = ObjectAnimator.ofFloat( bannerParent , "translationY" , mView.getHeight() - getResources().getDimension( R.dimen.news_adview_height ) );
			animator.setDuration( getResources().getInteger( R.integer.ad_show_or_hide_anim_duration ) );
			animator.addListener( new AnimatorListener() {
				
				@Override
				public void onAnimationStart(
						Animator animation )
				{
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onAnimationRepeat(
						Animator animation )
				{
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
					// TODO Auto-generated method stub
					bannerParent.setTag( null );
				}
				
				@Override
				public void onAnimationCancel(
						Animator animation )
				{
					// TODO Auto-generated method stub
				}
			} );
			bannerParent.setTag( animator );
			animator.start();
		}
	}
	
	private void hideAdBanner()
	{
		if( bannerParent != null && ( bannerParent.getVisibility() == View.VISIBLE && bannerParent.getTag() == null ) )
		{
			ObjectAnimator animator = ObjectAnimator.ofFloat( bannerParent , "translationY" , mView.getHeight() );
			animator.setDuration( getResources().getInteger( R.integer.ad_show_or_hide_anim_duration ) );
			animator.addListener( new AnimatorListener() {
				
				@Override
				public void onAnimationStart(
						Animator animation )
				{
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onAnimationRepeat(
						Animator animation )
				{
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
					// TODO Auto-generated method stub
					bannerParent.setTag( null );
					bannerParent.setVisibility( View.GONE );
				}
				
				@Override
				public void onAnimationCancel(
						Animator animation )
				{
					// TODO Auto-generated method stub
				}
			} );
			bannerParent.setTag( animator );
			animator.start();
		}
	}
	
	public void finishWithAnimation()
	{
		if( mView == null )
		{
			finish();
		}
		if( "finishing".equals( mView.getTag() ) )//防止连续按返回键，动画一直执行
		{
			return;
		}
		mView.setTag( "finishing" );
		Animation animation = AnimationUtils.loadAnimation( that , R.anim.slide_out_to_right );
		animation.setAnimationListener( new AnimationListener() {
			
			@Override
			public void onAnimationStart(
					Animation animation )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationRepeat(
					Animation animation )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationEnd(
					Animation animation )
			{
				// TODO Auto-generated method stub
				finish();
				that.overridePendingTransition( 0 , 0 );
			}
		} );
		mView.startAnimation( animation );
	}
	
	@Override
	public void onNewIntent(
			Intent intent )
	{
		// TODO Auto-generated method stub
		String url = intent.getStringExtra( "com.cooee.news.url" );
		mShare = intent.getStringExtra( "com.cooee.news.share" );
		if( url != null )
			mWebView.loadUrl( url );
	}
	
	public boolean onKeyDown(
			int keyCode ,
			KeyEvent event )
	{
		if( keyCode == KeyEvent.KEYCODE_BACK )
		{
			if( mWebView != null && mWebView.canGoBack() )
			{
				mWebView.goBack();
			}
			else
			{
				finishWithAnimation();
			}
			return true;
		}
		return false;
	}
	
	@TargetApi( 19 )
	private void setTranslucentStatus(
			boolean on )
	{
		Window win = that.getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if( on )
		{
			winParams.flags |= bits;
		}
		else
		{
			winParams.flags &= ~bits;
		}
		win.setAttributes( winParams );
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if( mView != null )
		{
			mView.removeView( mWebView );
		}
		if( mWebView != null )
		{
			mWebView.stopLoading();
			mWebView.removeAllViews();
			mWebView.destroy();
		}
		if( mbannerView != null )
		{
			mbannerView.onDestroy();
			mbannerView = null;
		}
	}
	
	private void setActionBar()
	{
		//		ActionBar actionBar = that.getActionBar();
		//		if( actionBar != null )
		//		{
		//			actionBar.setTitle( "" );
		//		}
		//		actionBar.setDisplayShowCustomEnabled( true );
		//		actionBar.setCustomView( R.layout.news_action_bar );
		Drawable back = new ColorDrawable( mCurrColor );
		View actionBar = mView.findViewById( R.id.title_action_bar );
		actionBar.setBackgroundDrawable( back );
		View btnBack = mView.findViewById( R.id.back );
		View btnShare = mView.findViewById( R.id.share );
		View changeSize = mView.findViewById( R.id.chang_fontsize );
		final ImageView changeMode = (ImageView)mView.findViewById( R.id.changeMode );
		if( btnBack != null )
		{
			btnBack.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					FavoritesConfig config = FavoritesManager.getInstance().getConfig();
					if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
					{
						MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "return_click" );
					}
					try
					{
						StatisticsExpandNew.onCustomEvent(
								FavoritesManager.getInstance().getContainerContext() ,
								"return_click" ,
								FavoritesPlugin.SN ,
								FavoritesPlugin.APPID ,
								CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName ,
								FavoritesPlugin.UPLOAD_VERSION + "" ,
								null );
					}
					catch( NoSuchMethodError e )
					{
						try
						{
							StatisticsExpandNew.onCustomEvent(
									FavoritesManager.getInstance().getContainerContext() ,
									"return_click" ,
									FavoritesPlugin.SN ,
									FavoritesPlugin.APPID ,
									CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
									FavoritesPlugin.PRODUCTTYPE ,
									FavoritesPlugin.PluginPackageName );
						}
						catch( NoSuchMethodError e1 )
						{
							StatisticsExpandNew.onCustomEvent( FavoritesManager.getInstance().getContainerContext() , "return_click" , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName );
						}
					}
					finishWithAnimation();
				}
			} );
		}
		if( btnShare != null )
		{
			btnShare.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					Intent intent = new Intent( Intent.ACTION_SEND );
					intent.setType( "text/plain" );
					intent.putExtra( Intent.EXTRA_SUBJECT , "好友分享" );
					// 自动添加的发送的具体信息
					intent.putExtra( Intent.EXTRA_TEXT , mShare );
					intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
					that.startActivity( Intent.createChooser( intent , getTitle() ) );
					FavoritesManager favoritesManager = FavoritesManager.getInstance();
					FavoritesConfig config = favoritesManager.getConfig();
					if( config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
					{
						MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "share_click" );
					}
					try
					{
						StatisticsExpandNew.onCustomEvent(
								FavoritesManager.getInstance().getContainerContext() ,
								"share_click" ,
								FavoritesPlugin.SN ,
								FavoritesPlugin.APPID ,
								CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName ,
								FavoritesPlugin.UPLOAD_VERSION + "" ,
								null );
					}
					catch( NoSuchMethodError e )
					{
						try
						{
							StatisticsExpandNew.onCustomEvent(
									FavoritesManager.getInstance().getContainerContext() ,
									"share_click" ,
									FavoritesPlugin.SN ,
									FavoritesPlugin.APPID ,
									CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
									FavoritesPlugin.PRODUCTTYPE ,
									FavoritesPlugin.PluginPackageName );
						}
						catch( NoSuchMethodError e1 )
						{
							StatisticsExpandNew.onCustomEvent( FavoritesManager.getInstance().getContainerContext() , "share_click" , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName );
						}
					}
				}
			} );
		}
		if( changeSize != null )
		{
			changeSize.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					showChangeSizeLayout();
				}
			} );
		}
		if( changeMode != null )
		{
			currentMode = mPreferences.getInt( "current_mode" , DAY_MODE );
			if( currentMode == DAY_MODE )
			{
				changeMode.setImageResource( R.drawable.night_mode_selector );
			}
			else
			{
				changeMode.setImageResource( R.drawable.day_mode_selector );
			}
			changeMode.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					if( currentMode == DAY_MODE )
					{
						currentMode = NIGHT_MODE;
						changeMode.setImageResource( R.drawable.day_mode_selector );
					}
					else
					{
						currentMode = DAY_MODE;
						changeMode.setImageResource( R.drawable.night_mode_selector );
					}
					changeWebViewMode();
					mPreferences.edit().putInt( "current_mode" , currentMode ).commit();
				}
			} );
		}
	}
	
	public int getStatusBarHeight()
	{
		int result = 0;
		int resourceId = that.getResources().getIdentifier( "status_bar_height" , "dimen" , "android" );
		if( resourceId > 0 )
		{
			result = that.getResources().getDimensionPixelSize( resourceId );
		}
		return result;
	}
	
	private int getFontZoom(
			int size )
	{
		switch( size )
		{
			case 0:
				return 75;
			case 1:
				return 100;
			case 2:
				return 150;
			case 3:
				return 200;
			default:
				return 100;
		}
	}
	
	private void showChangeSizeLayout()
	{
		// TODO Auto-generated method stub
		final SeekBarWithMark xmlSeekBar = (SeekBarWithMark)that.findViewById( R.id.seekBarWithMark );
		if( changeSizeDialogView == null )
		{
			changeSizeDialogView = that.findViewById( R.id.changeSizeLayout );
		}
		if( mCover == null )
		{
			mCover = that.findViewById( R.id.cover );
			mCover.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					mCover.setVisibility( View.GONE );
					changeSizeDialogView.setVisibility( View.GONE );
				}
			} );
		}
		mCover.setVisibility( View.VISIBLE );
		changeSizeDialogView.setVisibility( View.VISIBLE );
		xmlSeekBar.selectMarkItem( currentselectMarkItem );
		xmlSeekBar.setOnSelectItemListener( new SeekBarWithMark.OnSelectItemListener() {
			
			@Override
			public void selectItem(
					int nowSelectItemNum ,
					String val )
			{
				currentselectMarkItem = nowSelectItemNum;
				settings.setTextZoom( getFontZoom( currentselectMarkItem ) );
				mPreferences.edit().putInt( "fontsize" , nowSelectItemNum ).commit();
			}
		} );
	}
	
	/*
	 * 0-255
	*/public int getScreenBrightness()
	{
		int nowBrightnessValue = 0;
		ContentResolver resolver = that.getContentResolver();
		try
		{
			nowBrightnessValue = android.provider.Settings.System.getInt( resolver , Settings.System.SCREEN_BRIGHTNESS );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return nowBrightnessValue;
	}
}
