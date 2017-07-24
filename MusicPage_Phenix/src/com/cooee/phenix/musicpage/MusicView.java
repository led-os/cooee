package com.cooee.phenix.musicpage;


// MusicPage
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.R.color;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cooee.framework.config.ConfigUtils;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.UmengStatistics;
import com.cooee.phenix.kmob.ad.KmobUtil;
import com.cooee.phenix.mediapage.IMediaPlugin;
import com.cooee.phenix.musicandcamerapage.utils.AnimationUtils;
import com.cooee.phenix.musicandcamerapage.utils.ColorUtils;
import com.cooee.phenix.musicandcamerapage.utils.ToastUtils;
import com.cooee.phenix.musicandcamerapage.utils.ViewUtils;
import com.cooee.phenix.musicpage.control.MusicControl;
import com.cooee.phenix.musicpage.control.MusicControlCallBack;
import com.cooee.phenix.musicpage.entity.BottomButtonInfo;
import com.cooee.phenix.musicpage.entity.LyricSentence;
import com.cooee.phenix.musicpage.entity.MusicData;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.KmobConfig.KmobConfigData;


@SuppressLint( "NewApi" )
public class MusicView implements IMediaPlugin
{
	
	private final static String TAG = "MusicView";
	private Activity activity = null;
	private static MusicView musicView = null;
	private View musicPageView = null;
	// child view
	/**唱盘布局*/
	private RelativeLayout albumAnimLayout = null;
	/**点击响应唱针区域的布局*/
	private LinearLayout stylusAnimLayout = null;
	/**唱针做动画的布局*/
	private RelativeLayout stylusViewLayout = null;
	private CooeeImageView topAlbumImageView = null;// 含义有变，现在代表curAlbumLayout的替身
	private CooeeImageView bottomAlbumImageView = null;//含义有变，现在代表显示区域外面那个唱片
	private CooeeTextView musicNameTextView = null;
	private CooeeTextView singerNameTextView = null;
	private ImageView im_stylus_head = null;
	private CooeeLyricsTextView lyricTextView = null;
	private LinearLayout bottomBarLayout = null;
	private boolean haveAnimationRunning = false;
	private boolean hasTouchDown = false;
	private boolean firstInTouchMove = false;
	private boolean touchMoveHorizontal = false;
	private boolean albumButtonClick = false;
	private boolean lyricsTouching = false;
	private boolean musicViewPause = false;
	private Animation albumAnimation = null;
	private ObjectAnimator topAlbumRotateAnimator = null;
	private long viewStartDownTime = -1;
	private int viewQuickTouchCutSongTime = 0;
	private final int touchAngle = 30;
	private float viewStartDownX = 0;
	private float viewStartDownY = 0;
	private float viewLastDownX = 0;
	private float viewLastDownY = 0;
	private float albumTouchMinX = 0F;
	private float albumTouchMaxX = 0F;
	private float albumTouchMinY = 0F;
	private float albumTouchMaxY = 0F;
	private float albumButtonTouchMinX = 0F;
	private float albumButtonTouchMaxX = 0F;
	private float albumButtonTouchMinY = 0F;
	private float albumButtonTouchMaxY = 0F;
	private float albumMoveUpScale = 1F;
	private float albumBetweenLineTop = 0F;
	private float albumImageHeight = 0F;
	private float cutSongMaxMoveTag = 0F;
	private List<BottomButtonInfo> bottomBarButtonInfos = new ArrayList<BottomButtonInfo>();
	private MusicControl musicControl = null;
	public static ConfigUtils configUtils = null;
	public static final float ROTATION_PAUSE = 0;
	public static final float ROTATION_DIVIDING = 5;
	public static final float ROTATION_PLAY = 15;
	public static final float ROTATION_MAX = 25;
	private float density = 0;
	/**文字广告布局view*/
	private AdTextView adTextView = null;
	private boolean hasLyrics = false;
	// gaominghui@2016/07/19 ADD START
	private int vCenterX = 0; //唱盘圆心x坐标
	private int vCenterY = 0; //唱盘圆心y坐标
	private int r = 0;//唱盘半径
	Point mPoint = null;//用于存放唱盘坐标的
	private MusicData mCurMusicData = null;
	// gaominghui@2016/07/19 ADD END
	// gaominghui@2016/08/11 ADD START
	/**保存的日期,用于和当前日期比较是否为同一天*/
	private String mLastDate = null;
	/**当前日期格式化的format*/
	private SimpleDateFormat mCurDateFormat = null;
	/**SharedPreferences文件名*/
	private final static String SHARED_PREF_NAME = "music_page_phenix";
	/**日期字段的key*/
	private final static String DATE_KEY = "last_date";
	private static long lastClickTime;
	// gaominghui@2016/08/11 ADD END
	// gaominghui@2016/11/15 ADD START 计算文字广告位置的相关参数
	int adTextViewMarginTopMax = 0;
	int album_layoutTopMargin = 0;
	int nameTextViewMargin = 0;
	int singerTextViewMargin = 0;
	// gaominghui@2016/11/15 ADD END 计算文字广告位置的相关参数
	private int clickAlumbRange = 0;
	private int clickStylusRange = 0;
	//gaominghui add start //整理获取开关配置的代码
	private static boolean isShowLyrics = false;
	private static boolean enableLyricsFastLocate = false;
	
	//gaominghui add end
	private MusicView()
	{
	}
	
	// 对外接口 , start
	public synchronized static MusicView getInstance()
	{
		if( musicView == null )
		{
			musicView = new MusicView();
		}
		return musicView;
	}
	
	public synchronized void deleteInstance()
	{
		unRegisterScreenReceiver();
		GetAlbumRegionAndLyricsThredManager.stop();
		if( musicControl != null )
			musicControl.finish();
		//		musicControl = null;
		musicPageView = null;
		musicView = null;
		//		configUtils = null;
		bottomBarButtonInfos.clear();
	}
	
	//call this method must be in thread
	public synchronized void initConfig(
			Activity activity )
	{
		this.activity = activity;
		if( configUtils == null ) //gaominghui add  整理获取开关配置的代码
		{
			if( BaseDefaultConfig.mConfigUtils != null )
			{
				configUtils = BaseDefaultConfig.mConfigUtils;
			}
			else
			{
				configUtils = new ConfigUtils();
				configUtils.loadConfig( activity.getApplicationContext() , "assets/music_page/config.xml" );
			}
		}
		bottomBarButtonInfos.clear();
	}
	
	public synchronized View getMusicPageView(
			Activity activity )
	{
		if( musicPageView == null )
		{
			logI( "getMusicPageView , new" );
			LayoutInflater inflater = activity.getLayoutInflater();
			musicPageView = inflater.inflate( R.layout.music_page_view_layout , null );
			inflater = null;
			initDefaultConfig(); //gaomignhui add  //整理获取开关配置的代码
			getView();
			setViewAttribute( activity );
			setListener();
			musicControl = new MusicControl( activity , musicControlCallBack , lyricTextView.getVisibility() == View.VISIBLE );
			registerScreenReceiver();
			initDateUtil();
			// gaominghui@2016/11/11 ADD START
			clickAlumbRange = ViewUtils.dp2px( activity , 10 );
			clickStylusRange = ViewUtils.dp2px( activity , 4 );
			//Log.i( TAG , "getMusicPageView clickRange = " + clickAlumbRange + "; clickStylusRange = " + clickStylusRange );
			// gaominghui@2016/11/11 ADD END
		}
		return musicPageView;
	}
	
	public synchronized View getHeatSetBarView(
			Activity activity ,
			int height )
	{
		this.bottomBarLayout = new LinearLayout( activity );
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , height );
		this.bottomBarLayout.setLayoutParams( params );
		this.bottomBarLayout.setGravity( Gravity.CENTER );
		this.bottomBarLayout.setOrientation( LinearLayout.HORIZONTAL );
		addBottomBarButton( activity );
		return bottomBarLayout;
	}
	
	public void onResume()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "onResume  musicViewPause:" , musicViewPause ) );
		if( musicViewPause )
			if( musicControl != null && musicControl.getPlayStatus() && topAlbumRotateAnimator != null )
				if( android.os.Build.VERSION.SDK_INT >= 19 )
				{
					if( topAlbumRotateAnimator.isPaused() )
						topAlbumRotateAnimator.resume();
					else if( !topAlbumRotateAnimator.isStarted() )
					{
						topAlbumRotateAnimator.start();
					}
				}
				else
				{
					if( !topAlbumRotateAnimator.isRunning() )
						topAlbumRotateAnimator.start();
				}
		musicViewPause = false;
	}
	
	public void onPause()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "onPause=========" );
		if( !musicViewPause )
			if( android.os.Build.VERSION.SDK_INT >= 19 )
			{
				if( topAlbumRotateAnimator != null && !topAlbumRotateAnimator.isPaused() )
					topAlbumRotateAnimator.pause();
			}
			else
			{
				if( topAlbumRotateAnimator != null && topAlbumRotateAnimator.isStarted() )
					topAlbumRotateAnimator.cancel();
			}
		musicViewPause = true;
	}
	// 对外接口 , end
	;
	
	private void getView()
	{
		albumAnimLayout = (RelativeLayout)musicPageView.findViewById( R.id.music_page_album_anim_layout );
		stylusAnimLayout = (LinearLayout)musicPageView.findViewById( R.id.music_page_stylus_anim_touch_layout );
		stylusViewLayout = (RelativeLayout)musicPageView.findViewById( R.id.music_page_stylus_layout );
		density = activity.getApplicationContext().getResources().getDisplayMetrics().density;
		/**设置唱针中心点*/
		stylusViewLayout.setPivotX( density * 39.5f );
		stylusViewLayout.setPivotY( density * 26.0f );
		topAlbumImageView = (CooeeImageView)musicPageView.findViewById( R.id.music_page_top_album_image_view );
		bottomAlbumImageView = (CooeeImageView)musicPageView.findViewById( R.id.music_page_buttom_alumb_image_view );
		musicNameTextView = (CooeeTextView)musicPageView.findViewById( R.id.music_page_music_name_text_view );
		singerNameTextView = (CooeeTextView)musicPageView.findViewById( R.id.music_page_singer_name_text_view );
		lyricTextView = (CooeeLyricsTextView)musicPageView.findViewById( R.id.music_page_lyric_text_view );
		im_stylus_head = (ImageView)musicPageView.findViewById( R.id.music_page_im_stylus_head );
		im_stylus_head.setVisibility( View.INVISIBLE );
		adTextView = (AdTextView)musicPageView.findViewById( R.id.music_page_ad_text_view );
	}
	
	private void setViewAttribute(
			Activity activity )
	{
		topAlbumRotateAnimator = AnimationUtils.getcurAlbumLayoutRotateAnimation( activity , topAlbumImageView );
		//
		Resources resources = activity.getResources();
		float albumBgWH = resources.getDimension( R.dimen.music_page_album_bg_image_view_width_and_height );
		float albumButtonWH = resources.getDimension( R.dimen.music_page_album_image_button_width_and_height );
		//
		int viewTopMargin = configUtils.getInteger( "music_page_album_layout_magin_top" );
		//
		ViewUtils.setViewTopMargin( albumAnimLayout , viewTopMargin );
		ViewUtils.setViewTopMargin( musicNameTextView , configUtils.getInteger( "music_page_album_layout_to_music_name_text_magin" ) );
		ViewUtils.setViewTopMargin( singerNameTextView , configUtils.getInteger( "music_page_music_name_to_singer_name_text_magin" ) );
		// 
		musicNameTextView.setTextSize( configUtils.getInteger( "music_page_music_name_font_size" ) );
		musicNameTextView.setTextColor( ColorUtils.getLyricsColor( configUtils.getString( "music_page_music_name_font_color" ) , color.black ) );
		singerNameTextView.setTextSize( configUtils.getInteger( "music_page_singer_name_font_size" ) );
		singerNameTextView.setTextColor( ColorUtils.getLyricsColor( configUtils.getString( "music_page_singer_name_font_color" ) , color.black ) );
		//
		float musicNameHeight = musicNameTextView.getTextHeight() + musicNameTextView.getMarginTop();
		float singerNameHeight = singerNameTextView.getTextHeight() + singerNameTextView.getMarginTop();
		float lineMarginTop = resources.getDimension( R.dimen.music_page_music_view_line_margin );
		albumBetweenLineTop = musicNameHeight + singerNameHeight + lineMarginTop;
		albumImageHeight = resources.getDimension( R.dimen.music_page_album_bg_image_view_width_and_height );
		//
		this.albumTouchMinX = this.albumTouchMinY = 0F;
		this.albumTouchMaxX = albumBgWH;
		this.albumTouchMaxY = albumBgWH /*+ albumBetweenLineTop*/;
		this.albumButtonTouchMinX = this.albumButtonTouchMinY = ( albumBgWH - albumButtonWH ) / 2;
		this.albumButtonTouchMaxX = this.albumButtonTouchMaxY = this.albumButtonTouchMinX + albumButtonWH;
		this.cutSongMaxMoveTag = albumBgWH / 4;
		//		this.cutSongMinMoveTag = cutSongMaxMoveTag / 2;
		this.viewQuickTouchCutSongTime = configUtils.getInteger( "music_page_view_quick_touch_cut_song_time" );
		this.albumMoveUpScale = configUtils.getInteger( "music_page_album_move_up_scale" );
		if( isShowLyrics )//gaominghui add //整理获取开关配置的代码
		{
			ViewUtils.setViewTopMargin( lyricTextView , configUtils.getInteger( "music_page_music_and_singer_name_text_to_lyric_magin" ) );
			lyricTextView.setFontColor(
					ColorUtils.getLyricsColor( configUtils.getString( "music_page_lyrics_last_color" ) , color.black ) ,
					ColorUtils.getLyricsColor( configUtils.getString( "music_page_lyrics_current_color" ) , color.black ) ,
					ColorUtils.getLyricsColor( configUtils.getString( "music_page_lyrics_next_color" ) , color.black ) );
			lyricTextView.setMoveSet( configUtils.getInteger( "music_page_lyric_move_set" ) );
			lyricTextView.setCurFontSize( configUtils.getInteger( "music_page_cur_lyric_font_size" ) );
			lyricTextView.setOtherFontSize( configUtils.getInteger( "music_page_other_lyric_font_size" ) );
			lyricTextView.setLineSpacing( configUtils.getInteger( "music_page_lyric_line_height" ) );
			lyricTextView.setScrollWaitTime( configUtils.getInteger( "music_page_scroll_wait_time" ) );
			lyricTextView.initViewHeight();
		}
		else
		{
			lyricTextView.setVisibility( View.GONE );
		}
		// gaominghui@2016/11/14 ADD START  0014676: 【桌面】切换主题后，音乐页显示广告名称被截
		album_layoutTopMargin = configUtils.getInteger( "music_page_album_layout_magin_top" );
		nameTextViewMargin = configUtils.getInteger( "music_page_album_layout_to_music_name_text_magin" );
		singerTextViewMargin = configUtils.getInteger( "music_page_music_name_to_singer_name_text_magin" );
		// gaominghui@2016/12/30 ADD START 	0014757 适配480*800,480*854手机，音乐页显示广告名称被截
		adTextViewMarginTopMax = (int)resources.getDimension( R.dimen.music_page_ad_text_view_to_singer_name_margin );
		// gaominghui@2016/12/30 ADD END 0014757 适配480*800,480*854手机，音乐页显示广告名称被截
		// gaominghui@2016/11/14 ADD END  0014676: 【桌面】切换主题后，音乐页显示广告名称被截
		adTextView.setTextSize( configUtils.getInteger( "music_page_music_name_font_size" ) );
		adTextView.setTextColor( ColorUtils.getLyricsColor( configUtils.getString( "music_page_lyrics_current_color" ) , color.black ) );
		//adTextView.init( activity );
	}
	
	private void setListener()
	{
		albumAnimLayout.setOnTouchListener( touchListener );
		stylusAnimLayout.setOnTouchListener( touchListener );
		stylusViewLayout.setOnTouchListener( touchListener );
		//gaominghui add start //整理获取开关配置的代码
		if( isShowLyrics && enableLyricsFastLocate )
		//gaominghui add start //整理获取开关配置的代码
		{
			//			lyricTextView.setOnLongClickListener( longClickListener );
			lyricTextView.setOnTouchListener( touchListener );
		}
	}
	
	private void addBottomBarButton(
			Activity activity )
	{
		for( BottomButtonInfo info : bottomBarButtonInfos )
		{
			ImageView imageView = new ImageView( activity );
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( info.getIcon().getBitmap().getWidth() , info.getIcon().getBitmap().getHeight() );
			params.weight = 1f;
			imageView.setLayoutParams( params );
			imageView.setImageDrawable( info.getIcon() );
			imageView.setOnClickListener( clickListener );
			imageView.setTag( info );
			bottomBarLayout.addView( imageView );
		}
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(
				View v )
		{
			Object tag = v.getTag();
			if( tag instanceof BottomButtonInfo )
			{
				BottomButtonInfo info = (BottomButtonInfo)tag;
				Intent intent = new Intent();
				intent.setClassName( info.getPackName() , info.getClassName() );
				if( v.getContext().getPackageManager().queryIntentActivities( intent , 0 ).size() != 0 )
					v.getContext().startActivity( intent );
				else
					ToastUtils.showToast( activity , R.string.music_page_application_not_installed );
			}
		}
	};
	private float viewLastAngle = 0f;
	private float viewStartAngle = 0f;
	private boolean stylusMoveLeft = false;
	private float stylusAngle = 0;//指针旋转角度
	private float x = 0f;
	private float y = 0f;
	private float startX = 0f;
	private float startY = 0f;
	
	private boolean onTouchAlumb(
			float lastx ,
			float lasty )
	{
		int distanceX = (int)Math.abs( vCenterX - lastx );
		//点击位置y坐标与圆心的y坐标的距离
		int distanceY = (int)Math.abs( vCenterY - lasty );
		//点击位置与圆心的直线距离
		int distanceZ = (int)Math.sqrt( Math.pow( distanceX , 2 ) + Math.pow( distanceY , 2 ) );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "onTouchAlumb distanceZ:" , distanceZ , "-r:" , r ) );
		if( distanceZ > r )
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	/**
	 *
	 * @author gaominghui 2016年7月19日
	 */
	private void initAlumbCenterCoordinates()
	{
		int[] location = new int[2];
		bottomAlbumImageView.getLocationOnScreen( location );
		//控件相对于屏幕的x与y坐标
		int x = location[0];
		int y = location[1];
		mPoint = new Point( x , y );
		//圆半径 通过左右坐标计算获得getLeft
		r = ( bottomAlbumImageView.getRight() - bottomAlbumImageView.getLeft() ) / 2;
		//圆心坐标
		vCenterX = x + r;
		vCenterY = y + r;
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "vCenterX:" , vCenterX , "-vCenterY:" , vCenterY , "-r:" , r ) );
	}
	
	// gaominghui@2016/10/31 ADD START
	//用来区分是点击唱针还是波动唱针的标志位
	private boolean isClickstylusAnimLayout = false;
	// gaominghui@2016/10/31 ADD END
	private OnTouchListener touchListener = new OnTouchListener() {
		
		/**
		 *
		 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
		 * @auther gaominghui  2016年7月1日
		 */
		@Override
		public boolean onTouch(
				View v ,
				MotionEvent event )
		{
			if( haveAnimationRunning )
				return true;
			boolean returnValue = false;
			switch( event.getAction() )
			{
				case MotionEvent.ACTION_DOWN:
					hasTouchDown = true;
					firstInTouchMove = true;
					touchMoveHorizontal = false;
					logI( "onTouch : MotionEvent.ACTION_DOWN" );
					viewLastDownX = viewStartDownX = event.getRawX();
					viewLastDownY = viewStartDownY = event.getRawY();
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , StringUtils.concat( "MotionEvent.ACTION_DOWN viewLastDownX:" , viewLastDownX , "-viewLastDownY:" , viewLastDownY ) );
					viewStartDownTime = System.currentTimeMillis();
					startX = event.getX();
					startY = event.getY();
					// gaominghui@2016/07/19 ADD START 布局初始化之后先确定唱盘的原因坐标，方便后边判断点击区域
					if( mPoint == null )
					{
						initAlumbCenterCoordinates();
					}
					// gaominghui@2016/07/19 ADD END 布局初始化之后先确定唱盘的点击范围 方便后边判断点击区域
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , StringUtils.concat( "MotionEvent.ACTION_DOWN  topAlbumImageView.isShowingAd():" , topAlbumImageView.isShowingAd() ) );
					if( v == albumAnimLayout )
					{
						if( albumButtonTouchMinX < startX && startX < albumButtonTouchMaxX && albumButtonTouchMinY < startY && startY < albumButtonTouchMaxY )
						{
							//resetViewByTouchDown();
							// 封面
							curAlbumImageButtonTouchDown();
							returnValue = true;
						}
						else if( onTouchAlumb( viewLastDownX , viewLastDownY ) )
						{
							resetViewByTouchDown();
							//	Log.i( TAG , "resetViewByTouchDown 2222222222 topAlbumImageView.x = " + topAlbumImageView.getX() + "; topAlbumImageView.y = " + topAlbumImageView.getY() );
							setCurAlbumAnimImageView();
							//	Log.i( TAG , "resetViewByTouchDown 333333333333333333 topAlbumImageView.x = " + topAlbumImageView.getX() + "; topAlbumImageView.y = " + topAlbumImageView.getY() );
							returnValue = true;
						}
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "On Touch Down v == albumAnimLayout !!! " );
					}
					else if( v == stylusAnimLayout )
					{
						isClickstylusAnimLayout = false;
						//Log.i( TAG , " v == stylusAnimLayout!!!   viewLastAngle = " + viewLastAngle + "; viewStartAngle = " + viewStartAngle );
						//如果判断点击的是该区域，需要以唱针旋转移动
						v.getParent().requestDisallowInterceptTouchEvent( true );
						viewStartAngle = viewLastAngle;
						if( viewStartAngle == ROTATION_PAUSE )
						{
							stylusMoveLeft = false;
						}
						else if( viewStartAngle == ROTATION_PLAY )
						{
							stylusMoveLeft = true;
							im_stylus_head.setVisibility( View.INVISIBLE );
						}
						returnValue = true;
						resetViewByTouchDown();
						albumButtonClick = false;
					}
					/*else if( v == lyricTextView )
					{
						List<LyricSentence> list = lyricTextView.getLyricList();
						if( list != null && list.size() > 0 && musicControl.getPlayStatus() )
						{
							lyricTextViewTouchDown();
							returnValue = true;
						}
					}*/
					break;
				case MotionEvent.ACTION_MOVE:
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , "MotionEvent.ACTION_MOVE!!!" );
					if( ( viewLastDownX == event.getRawX() && viewLastDownY == event.getRawY() ) || !hasTouchDown )
						return returnValue;
					//logI( "onTouch : MotionEvent.ACTION_MOVE" );
					viewLastDownX = event.getRawX();
					viewLastDownY = event.getRawY();
					x = event.getX();
					y = event.getY();
					if( firstInTouchMove )
					{
						firstInTouchMove = false;
						float moveHorizontal = Math.abs( x - startX );
						float moveVertical = Math.abs( y - startY );
						int angle = (int)( Math.atan( moveHorizontal / moveVertical ) / Math.PI * 180 );
						if( angle > touchAngle && v != stylusAnimLayout )
							touchMoveHorizontal = true;
						else
							touchMoveHorizontal = false;
					}
					if( touchMoveHorizontal )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "onTouch : MotionEvent.ACTION_MOVE , moveHorizontal" );
						hasTouchDown = false;
						if( topAlbumImageView.isShowingAd() )
						{
							topAlbumImageView.setImageBitmap( true );
						}
						else
						{
							ViewUtils.setImageResourceByTag( topAlbumImageView , true );
						}
						ViewUtils.clearAnimation( topAlbumImageView );
						ViewUtils.setTranslationY( topAlbumImageView , 0 );
						ViewUtils.setAlpha( topAlbumImageView , 1 );
						ViewUtils.setAlpha( bottomAlbumImageView , 1 );
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "ON Touch Move before remove bottomAlbumImageView" );
						ViewUtils.removeView( albumAnimLayout , bottomAlbumImageView );
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "ON Touch Move after remove bottomAlbumImageView" );
						/*if( musicControl.getPlayStatus() )
						{
							if( android.os.Build.VERSION.SDK_INT >= 19 )
							{
								if( !topAlbumRotateAnimator.isPaused() )
									topAlbumRotateAnimator.pause();
							}
							else
							{
								if( topAlbumRotateAnimator.isStarted() )
									topAlbumRotateAnimator.cancel();
							}
						}*/
						return returnValue;
					}
					logI( "onTouch : MotionEvent.ACTION_MOVE ,  " );
					if( v == stylusAnimLayout )
					{
						//如果判断点击的是该区域，需要以唱针旋转移动
						float moveHorizontal = event.getRawX() - viewStartDownX;
						float moveAbsoluteHorizontal = Math.abs( moveHorizontal );
						float moveVertical = density * 254;
						stylusAngle = (float)( Math.asin( moveAbsoluteHorizontal / moveVertical ) * 180 / Math.PI );
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , StringUtils.concat( "stylusAngle:" , stylusAngle , "-stylusMoveLeft:" , stylusMoveLeft ) );
						if( stylusMoveLeft )
						{
							if( moveHorizontal > 0 )//上一次是播放（左滑），这次暂停（右滑）
							{
								stylusAngle = viewStartAngle - stylusAngle;
							}
							else
							{
								stylusAngle = viewStartAngle + stylusAngle;
							}
							if( stylusAngle >= ROTATION_MAX )
							{
								stylusAngle = ROTATION_MAX;
							}
							else if( stylusAngle <= ROTATION_PAUSE )
							{
								stylusAngle = ROTATION_PAUSE;
							}
						}
						else if( moveHorizontal >= 0 )
						{
							stylusAngle = ROTATION_PAUSE;
						}
						else if( moveHorizontal < 0 && stylusAngle >= ROTATION_MAX )
						{
							stylusAngle = ROTATION_MAX;
						}
						ViewUtils.setRotation( stylusViewLayout , stylusAngle );
						viewLastAngle = stylusAngle;
						returnValue = true;
					}
					if( v == albumAnimLayout )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "On Touch Move v == albumAnimLayout !!! " );
						if( albumButtonTouchMinX < x && x < albumButtonTouchMaxX && //
						albumButtonTouchMinY < y && y < albumButtonTouchMaxY )
						{
							// 封面
							// gaominghui@2016/07/15 ADD START
							//如果当前正在显示广告,点击效果要走有广告的点击效果图片
							if( topAlbumImageView.isShowingAd() )
							{
								topAlbumImageView.setImageBitmap( true );
							}
							// gaominghui@2016/07/15 ADD END
							else
							{
								ViewUtils.setImageResourceByTag( topAlbumImageView , true );
							}
							curAlbumAnimImageViewTouchMove();
							returnValue = true;
						}
						else
						/*if( onTouchAlumb( viewLastDownX , viewLastDownY ) )*/
						{
							// 唱盘
							curAlbumAnimImageViewTouchMove();
							returnValue = true;
						}
					}
					/*if( v == lyricTextView )
					{
						lyricTextViewTouchMove();
						returnValue = true;
					}*/
					break;
				case MotionEvent.ACTION_UP:
					if( !hasTouchDown )
						return returnValue;
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , "onTouch : MotionEvent.ACTION_UP" );
					viewLastDownX = event.getRawX();
					viewLastDownY = event.getRawY();
					x = event.getX();
					y = event.getY();
					if( v == albumAnimLayout )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , StringUtils.concat(
									"On Touch Up [v == albumAnimLayout] viewLastDownX:" ,
									viewLastDownX ,
									"-viewLastDownY:" ,
									viewLastDownY ,
									"-viewStartDownX:" ,
									viewStartDownX ,
									"-viewStartDownY:" ,
									viewStartDownY ) );
						float absviewLastDownX = Math.abs( viewLastDownX );
						float absviewStartDownX = Math.abs( viewStartDownX );
						float rangeX = Math.abs( absviewLastDownX - absviewStartDownX );
						float absviewLastDownY = Math.abs( viewLastDownY );
						float absviewStartDownY = Math.abs( viewStartDownY );
						float rangeY = Math.abs( absviewLastDownY - absviewStartDownY );
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , StringUtils.concat( "rangeX:" , rangeX , "-rangeY:" , rangeY , "-clickRange:" , clickAlumbRange ) );
						if( rangeX <= clickAlumbRange && rangeY <= clickAlumbRange )
						/*{
						}
						if( viewLastDownX == viewStartDownX && viewLastDownY == viewStartDownY )
						*/
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.i( TAG , "onTouch : MotionEvent.ACTION_UP , click" );
							// 单击
							if( albumButtonTouchMinX < x && x < albumButtonTouchMaxX && albumButtonTouchMinY < y && y < albumButtonTouchMaxY )
							{
								// 封面
								// gaominghui@2016/07/15 ADD START
								//如果当前正在显示广告,点击效果要走有广告的点击效果图片
								if( topAlbumImageView.isShowingAd() )
								{
									topAlbumImageView.setImageBitmap( true );
								}
								// gaominghui@2016/07/15 ADD END
								else
								{
									ViewUtils.setImageResourceByTag( topAlbumImageView , true );
								}
								curAlbumImageButtonTouchUp();
								returnValue = true;
							}
							else if( onTouchAlumb( viewLastDownX , viewLastDownY ) && !isFastClick() )
							{
								if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
									Log.i( TAG , StringUtils.concat( "ONTOUCHUP isShowingAd:" , topAlbumImageView.isShowingAd() ) );
								if( topAlbumImageView.isShowingAd() )
								{
									topAlbumImageView.onAdClick();
								}
								else
								{
									resetTopAlumbRotationAnimatorState();
								}
								returnValue = true;
							}
							AnimationUtils.albumAnimationFinish( topAlbumImageView , bottomAlbumImageView , albumAnimLayout );
						}
						else
						{
							logI( "onTouch : MotionEvent.ACTION_UP , move" );
							// 唱盘
							curAlbumAnimImageViewTouchUp();
							if( albumButtonTouchMinX < viewStartDownX && viewStartDownX < albumButtonTouchMaxX && albumButtonTouchMinY < viewStartDownY && viewStartDownY < albumButtonTouchMaxY )
							{
								// 封面
								// gaominghui@2016/07/15 ADD START
								if( topAlbumImageView.isShowingAd() )
								{
									topAlbumImageView.setImageBitmap( true );
								}
								// gaominghui@2016/07/15 ADD END
								else
								{
									ViewUtils.setImageResourceByTag( topAlbumImageView , true );
								}
							}
							returnValue = true;
						}
					}
					/*else if( v == lyricTextView )
					{
						if( viewStartDownY != viewLastDownY )// 屏蔽点击
							lyricTextViewTouchUp();
						returnValue = true;
					}*/
					else if( v == stylusAnimLayout )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "On Touch Up [v == stylusAnimLayout] " );
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , StringUtils.concat(
									"On Touch Up [v == stylusAnimLayout] viewLastDownX:" ,
									viewLastDownX ,
									"-viewLastDownY" ,
									viewLastDownY ,
									"-viewStartDownX:" ,
									viewStartDownX ,
									"-viewStartDownY:" ,
									viewStartDownY ) );
						float absviewLastDownX = Math.abs( viewLastDownX );
						float absviewStartDownX = Math.abs( viewStartDownX );
						float rangeX = Math.abs( absviewLastDownX - absviewStartDownX );
						float absviewLastDownY = Math.abs( viewLastDownY );
						float absviewStartDownY = Math.abs( viewStartDownY );
						float rangeY = Math.abs( absviewLastDownY - absviewStartDownY );
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , StringUtils.concat( "rangeX:" , rangeX , "-rangeY:" , rangeY , "-clickStylusRange:" , clickStylusRange ) );
						//if( viewStartDownX == viewLastDownX && viewStartDownY == viewLastDownY )
						if( rangeX <= clickStylusRange && rangeY <= clickStylusRange )
						{
							isClickstylusAnimLayout = true;
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.i( TAG , "On Touch Up v == stylusAnimLayout !!!=============click " );
							int rotation = Math.round( stylusViewLayout.getRotation() );
							if( rotation == 0 )
							{
								AnimationUtils.startStylusRotationAnimatior( stylusViewLayout , 0 , MusicView.ROTATION_PLAY );
								im_stylus_head.setVisibility( View.VISIBLE );
								viewLastAngle = ROTATION_PLAY;
								//fulijuan add start		//没有音乐时 滑杆滑动后立马弹回
								if( null == mCurMusicData )
								{
									AnimationUtils.startStylusRotationAnimatior( stylusViewLayout , MusicView.ROTATION_PLAY , 0 );
									im_stylus_head.setVisibility( View.GONE );
									viewLastAngle = ROTATION_PAUSE;
								}
								//fulijuan add end
							}
							else if( rotation == Math.round( ROTATION_PLAY ) )
							{
								AnimationUtils.startStylusRotationAnimatior( stylusViewLayout , MusicView.ROTATION_PLAY , 0 );
								im_stylus_head.setVisibility( View.VISIBLE );
								viewLastAngle = ROTATION_PAUSE;
							}
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.i( TAG , StringUtils.concat( "On Touch Up v == albumAnimLayout  stylusViewLayout.getRotation():" , stylusViewLayout.getRotation() ) );
						}
						if( viewLastAngle <= ROTATION_DIVIDING )
						{
							viewLastAngle = ROTATION_PAUSE;
							// gaominghui@2016/10/31 ADD START
							//如果是点击唱针时则不需要在从新设角度了，因为在点击后的动画里面已经设置过角度了
							if( !isClickstylusAnimLayout )
							// gaominghui@2016/10/31 ADD END
							{
								ViewUtils.setRotation( stylusViewLayout , viewLastAngle );
								//fulijuan add start		//没有音乐时 滑杆滑动后立马弹回
								if( null == mCurMusicData )
								{
									ViewUtils.setRotation( stylusViewLayout , 0 );
								}
								//fulijuan add end
							}
							stylusMoveLeft = false;
							musicControl.pause();
							im_stylus_head.setVisibility( View.INVISIBLE );
							resetTopAlumbRotationAnimatorState();
						}
						else if( viewLastAngle >= ROTATION_MAX || ( viewLastAngle > ROTATION_DIVIDING && ROTATION_MAX > viewLastAngle ) )
						{
							viewLastAngle = ROTATION_PLAY;
							im_stylus_head.setVisibility( View.VISIBLE ); //fulijuan add //没有音乐时 滑杆滑动后立马弹回
							// gaominghui@2016/10/31 ADD START
							//如果是点击唱针时则不需要在从新设角度了，因为在点击后的动画里面已经设置过角度了
							if( !isClickstylusAnimLayout )
							// gaominghui@2016/10/31 ADD END
							{
								ViewUtils.setRotation( stylusViewLayout , viewLastAngle );
								//fulijuan add start		//没有音乐时 滑杆滑动后立马弹回
								if( null == mCurMusicData )
								{
									ViewUtils.setRotation( stylusViewLayout , 0 );
									im_stylus_head.setVisibility( View.GONE );
								}
								//fulijuan add end
							}
							stylusMoveLeft = true;
							musicControl.play();
							//im_stylus_head.setVisibility( View.VISIBLE ); //fulijuan del //没有音乐时 滑杆滑动后立马弹回
							resetTopAlumbRotationAnimatorState();
						}
						returnValue = true;
						albumButtonClick = true;
					}
					hasTouchDown = false;
					break;
				//
				case MotionEvent.ACTION_CANCEL:
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , StringUtils.concat( "onTouch : MotionEvent.ACTION_CANCEL touchMoveHorizontal:" , touchMoveHorizontal , "-hasTouchDown:" , hasTouchDown ) );
					if( !touchMoveHorizontal && hasTouchDown )
					{
						if( lyricsTouching )
						{
							lyricTextViewTouchUp();
						}
						else if( v == stylusAnimLayout )
						{
							ViewUtils.setRotation( stylusViewLayout , 0 );
							viewLastAngle = 0;
						}
						else if( v == albumAnimLayout )
						{
							curAlbumAnimImageViewTouchUp();
							if( topAlbumImageView.isShowingAd() )
							{
								topAlbumImageView.setImageBitmap( true );
							}
							else
							{
								ViewUtils.setImageResourceByTag( topAlbumImageView , true );
							}
						}
						returnValue = true;
					}
					lyricsTouching = false;
					lyricTextView.setFastLocate( lyricsTouching );
					break;
			}
			return returnValue;
		}
	};
	
	/**
	 *
	 * @author gaominghui 2016年7月9日
	 */
	private void resetTopAlumbRotationAnimatorState()
	{
		if( musicControl.getPlayStatus() )
		{
			if( android.os.Build.VERSION.SDK_INT >= 19 )
			{
				if( topAlbumRotateAnimator.isPaused() )
					topAlbumRotateAnimator.resume();
				else if( !topAlbumRotateAnimator.isStarted() )
				{
					topAlbumRotateAnimator.start();
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , "1111111111 android.os.Build.VERSION.SDK_INT >= 19  resetTopAlumbRotationAnimatorState start" );
				}
			}
			else
			{
				if( !topAlbumRotateAnimator.isRunning() )
				{
					topAlbumRotateAnimator.start();
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , "1111111111 resetTopAlumbRotationAnimatorState start" );
				}
			}
		}
	}
	
	private AnimationListener animationListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(
				Animation animation )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "AnimationUtils" , " animationListener onAnimationStart!!" );
		}
		
		@Override
		public void onAnimationRepeat(
				Animation animation )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "AnimationUtils" , " animationListener onAnimationRepeat!!" );
		}
		
		@Override
		public void onAnimationEnd(
				Animation animation )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "AnimationUtils" , " animationListener onAnimationEnd!!" );
			if( animation == albumAnimation )
			{
				Boolean musicControlBoolean = (Boolean)bottomAlbumImageView.getTag();//null是不操作，true是上一首，false是下一首
				AnimationUtils.albumAnimationFinish( topAlbumImageView , bottomAlbumImageView , albumAnimLayout );
				if( musicControlBoolean == null )
				{// 防止空指针死机
					if( musicControl.getPlayStatus() )
						if( android.os.Build.VERSION.SDK_INT >= 19 )
						{
							if( topAlbumRotateAnimator.isPaused() )
								topAlbumRotateAnimator.resume();
							else if( !topAlbumRotateAnimator.isStarted() )
							{
								topAlbumRotateAnimator.start();
							}
						}
						else
						{
							if( !topAlbumRotateAnimator.isRunning() )
							{
								topAlbumRotateAnimator.start();
							}
						}
				}
				else if( musicControlBoolean == true )
				{
					musicControlCallBack.onMusicAlbumRegionChange( null );
					if( android.os.Build.VERSION.SDK_INT >= 19 )
					{
						if( topAlbumRotateAnimator != null && !topAlbumRotateAnimator.isPaused() )
							topAlbumRotateAnimator.pause();
						else if( topAlbumRotateAnimator != null && topAlbumRotateAnimator.isStarted() )
							topAlbumRotateAnimator.cancel();
					}
					else
					{
						if( topAlbumRotateAnimator != null && topAlbumRotateAnimator.isStarted() )
							topAlbumRotateAnimator.cancel();
					}
					if( topAlbumImageView.getRotation() != 0 )
						topAlbumImageView.setRotation( 0 );
					if( MusicView.configUtils.getInteger( "music_page_switch_music" ) != MusicControl.VIVO_MUSIC_TYPE )
					{
						if( stylusViewLayout.getRotation() == 0 && mCurMusicData != null )
						{
							AnimationUtils.startStylusRotationAnimatior( stylusViewLayout , 0 , MusicView.ROTATION_PLAY );
							im_stylus_head.setVisibility( View.VISIBLE );
							viewLastAngle = ROTATION_PLAY;
						}
					}
					musicControl.previous();
					if( mCurMusicData != null )
					{
						ViewUtils.setImageResourceByTag( topAlbumImageView , true );
					}
				}
				else if( musicControlBoolean == false )
				{
					musicControlCallBack.onMusicAlbumRegionChange( null );
					//
					if( android.os.Build.VERSION.SDK_INT >= 19 )
					{
						if( topAlbumRotateAnimator != null && !topAlbumRotateAnimator.isPaused() )
							topAlbumRotateAnimator.pause();
						else if( topAlbumRotateAnimator != null && topAlbumRotateAnimator.isStarted() )
							topAlbumRotateAnimator.cancel();
					}
					else
					{
						if( topAlbumRotateAnimator != null && topAlbumRotateAnimator.isStarted() )
							topAlbumRotateAnimator.cancel();
					}
					if( topAlbumImageView.getRotation() != 0 )
						topAlbumImageView.setRotation( 0 );
					if( MusicView.configUtils.getInteger( "music_page_switch_music" ) != MusicControl.VIVO_MUSIC_TYPE )
					{
						if( stylusViewLayout.getRotation() == 0 && mCurMusicData != null )
						{
							AnimationUtils.startStylusRotationAnimatior( stylusViewLayout , 0 , MusicView.ROTATION_PLAY );
							im_stylus_head.setVisibility( View.VISIBLE );
							viewLastAngle = ROTATION_PLAY;
						}
					}
					musicControl.next();
					if( mCurMusicData != null )
						ViewUtils.setImageResourceByTag( topAlbumImageView , true );
				}
				bottomAlbumImageView.setTag( null );
				albumAnimation = null;
			}
			haveAnimationRunning = false;
		}
	};
	
	// 唱盘拖动 , start
	private void resetViewByTouchDown()
	{
		/*if( android.os.Build.VERSION.SDK_INT >= 19 )
		{
			if( topAlbumRotateAnimator != null && !topAlbumRotateAnimator.isPaused() )
				topAlbumRotateAnimator.pause();
		}
		else
		{
			if( topAlbumRotateAnimator != null && topAlbumRotateAnimator.isStarted() )
				topAlbumRotateAnimator.cancel();
		}*/
		ViewUtils.clearAnimation( topAlbumImageView );
		ViewUtils.clearAnimation( bottomAlbumImageView );
		ViewUtils.setTranslationY( topAlbumImageView , 0 );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , " resetViewByTouchDown=======before remove  bottomAlbumImageView" );
		ViewUtils.removeView( albumAnimLayout , bottomAlbumImageView );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , " resetViewByTouchDown=======after remove  bottomAlbumImageView" );
	}
	
	private void setCurAlbumAnimImageView()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "curAlbumAnimImageViewTouchDown topAlbumImageView.isShowingAd():" , topAlbumImageView.isShowingAd() ) );
		if( topAlbumImageView.isShowingAd() )
		{
			topAlbumImageView.setImageBitmap( true );
		}
		else
		{
			ViewUtils.setImageResourceByTag( topAlbumImageView , true );
		}
	}
	
	private void curAlbumAnimImageViewTouchMove()
	{
		if( viewLastDownY != viewStartDownY )
		{
			float moveY = ( viewLastDownY - viewStartDownY );
			float moveOrigin = albumImageHeight / 2;
			if( moveY < 0 )
			{
				// 上滑
				moveY = moveOrigin + moveY * albumMoveUpScale;
				if( moveY < 0 )
					moveY = 0;
				ViewUtils.setTranslationY( topAlbumImageView , 0 );
				ViewUtils.setTranslationY( bottomAlbumImageView , moveY );
				float alpha = 1 - ( Math.abs( moveY ) / moveOrigin );
				if( alpha < 0 )
				{
					alpha = 0;
				}
				ViewUtils.setAlpha( bottomAlbumImageView , alpha );
				if( topAlbumImageView.getAlpha() != 1 )
				{
					ViewUtils.setAlpha( topAlbumImageView , 1 );
				}
				ViewUtils.addView( albumAnimLayout , bottomAlbumImageView , 1 );
			}
			else
			{
				// 下滑
				if( moveY > moveOrigin )
					moveY = moveOrigin;
				ViewUtils.setTranslationY( topAlbumImageView , moveY );
				ViewUtils.setTranslationY( bottomAlbumImageView , 0 );
				float alpha = 1 - ( moveY / moveOrigin );
				if( alpha < 0 )
				{
					alpha = 0;
				}
				ViewUtils.setAlpha( topAlbumImageView , alpha );
				if( bottomAlbumImageView.getAlpha() != 1 )
				{
					ViewUtils.setAlpha( bottomAlbumImageView , 1 );
				}
				ViewUtils.addView( albumAnimLayout , bottomAlbumImageView , 0 );
			}
		}
	}
	
	private void curAlbumAnimImageViewTouchUp()
	{
		int moveY = (int)( viewLastDownY - viewStartDownY );
		logI( StringUtils.concat( "curAlbumAnimImageViewTouchUp , moveY:" , moveY ) );
		if( moveY != 0 )
		{
			haveAnimationRunning = true;
			//
			float toYDelta = 0F;
			float absMoveY = Math.abs( moveY );
			View animView = null;
			Boolean musicControl = null;//null是不操作，true是上一首，false是下一首
			float max = albumImageHeight / 2;
			long touchTime = System.currentTimeMillis() - viewStartDownTime;
			//
			if( moveY < 0 )
			{
				// 上滑
				animView = bottomAlbumImageView;
				moveY *= albumMoveUpScale;
				absMoveY *= albumMoveUpScale;
				if( absMoveY > cutSongMaxMoveTag || touchTime < viewQuickTouchCutSongTime )
				{
					if( absMoveY > ( max ) )
						absMoveY = max;
					toYDelta = -( max - absMoveY );
					musicControl = true;
					AnimationUtils.setAlphaAnimation( animView , animView.getAlpha() , 1 );
				}
				else
				{
					toYDelta = absMoveY;
					AnimationUtils.setAlphaAnimation( animView , animView.getAlpha() , 0 );
				}
			}
			else
			{
				// 下滑
				animView = topAlbumImageView;
				if( absMoveY > cutSongMaxMoveTag || touchTime < viewQuickTouchCutSongTime )
				{
					if( absMoveY > ( max ) )
						absMoveY = max;
					toYDelta = max - absMoveY;
					musicControl = false;
					AnimationUtils.setAlphaAnimation( animView , animView.getAlpha() , 0 );
				}
				else
				{
					toYDelta = -absMoveY;
					AnimationUtils.setAlphaAnimation( animView , animView.getAlpha() , 1 );
				}
			}
			bottomAlbumImageView.setTag( musicControl );
			albumAnimation = AnimationUtils.getAlbumAnimation( animView , animationListener , toYDelta );
		}
		else
		{
			AnimationUtils.albumAnimationFinish( topAlbumImageView , bottomAlbumImageView , albumAnimLayout );
		}
	}
	
	// 唱盘拖动 , end
	// 封面点击 , start
	private void curAlbumImageButtonTouchDown()
	{
		albumButtonClick = false;
		// gaominghui@2016/07/15 ADD START 
		//如果点击时当前唱盘是有广告的，这个时候点击下去应该显示有广告的那张按下去的唱盘
		if( topAlbumImageView.isShowingAd() )
		{
			topAlbumImageView.setImageBitmap( false );
		}
		// gaominghui@2016/07/15 ADD END
		else
		{
			ViewUtils.setImageResourceByTag( topAlbumImageView , false );
		}
	}
	
	private void curAlbumImageButtonTouchUp()
	{
		musicControl.enterClient();
		albumButtonClick = true;
	}
	
	// 封面点击 , end
	// 音乐  , start
	private MusicControlCallBack musicControlCallBack = new MusicControlCallBack() {
		
		@Override
		public void onMusicPlay()
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "musicControlCallBack onMusicPlay musicViewPause:" , musicViewPause , "-albumButtonClick:" , albumButtonClick ) );
			if( albumButtonClick )
			{
				if( !musicViewPause )
					if( android.os.Build.VERSION.SDK_INT >= 19 )
					{
						if( topAlbumRotateAnimator.isPaused() )
							topAlbumRotateAnimator.resume();
						else
						{
							topAlbumRotateAnimator.start();
						}
					}
					else
					{
						if( !topAlbumRotateAnimator.isRunning() )
						{
							topAlbumRotateAnimator.start();
						}
					}
				albumButtonClick = false;
			}
			else
			{
				if( topAlbumRotateAnimator != null )
				{
					topAlbumRotateAnimator.removeAllListeners();
					topAlbumRotateAnimator.end();
					topAlbumRotateAnimator.cancel();
				}
				topAlbumRotateAnimator.start();
			}
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "onMusicPlay !stylusViewLayout.getRotation():" , stylusViewLayout.getRotation() ) );
			if( stylusViewLayout.getRotation() == ROTATION_PAUSE )
			{
				AnimationUtils.startStylusRotationAnimatior( stylusViewLayout , ROTATION_PAUSE , ROTATION_PLAY );
				im_stylus_head.setVisibility( View.VISIBLE );
				viewLastAngle = ROTATION_PLAY;
			}
		}
		
		@Override
		public void onMusicPause()
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "musicControlCallBack onMusicPause" );
			if( android.os.Build.VERSION.SDK_INT >= 19 )
			{
				if( topAlbumRotateAnimator != null && !topAlbumRotateAnimator.isPaused() )
					topAlbumRotateAnimator.pause();
			}
			else
			{
				if( topAlbumRotateAnimator != null && topAlbumRotateAnimator.isStarted() )
					topAlbumRotateAnimator.cancel();
			}
			stylusViewLayout.clearAnimation();
			if( stylusViewLayout.getRotation() == ROTATION_PLAY )
			{
				AnimationUtils.startStylusRotationAnimatior( stylusViewLayout , ROTATION_PLAY , ROTATION_PAUSE );
				im_stylus_head.setVisibility( View.INVISIBLE );
				viewLastAngle = ROTATION_PAUSE;
			}
		}
		
		@Override
		public void onMusicInfoChange(
				Activity activity ,
				MusicData musicData )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "onMusicInfoChange , musicData : " + musicData );
			mCurMusicData = musicData;
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "onMusicInfoChange , mCurMusicData : " + mCurMusicData );
			if( musicData == null )
			{
				return;
			}
			String newMusicName = null;
			String newSingerName = null;
			if( musicData != null )
			{
				newMusicName = musicData.getTitle();
				newSingerName = musicData.getArtist();
			}
			if( !musicNameTextView.getText().toString().equals( newMusicName ) )
				musicNameTextView.setText( newMusicName );
			if( !singerNameTextView.getText().toString().equals( newSingerName ) )
				singerNameTextView.setText( newSingerName );
			//
			if( topAlbumImageView.getRotation() != 0 )
				topAlbumImageView.setRotation( 0 );
			//
			lyricsTouching = false;
			lyricTextView.setFastLocate( lyricsTouching );
		}
		
		@Override
		public void onMusicAlbumRegionChange(
				final BitmapDrawable[] topDrawables )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "onMusicAlbumRegionChange , ( topDrawables == null ):" , ( topDrawables == null ) ) );
			// go to here , may be in thread
			final Object oldTag = topAlbumImageView.getTag();
			topAlbumImageView.setTag( topDrawables );
			if( oldTag == null && topDrawables == null )
			{
				boolean enableShowAlbumAd = KmobUtil.getInstance().enableShowAd( KmobConfigData.ALBUM_ADPLACE_ID , topAlbumImageView.getHasShows() );
				if( enableShowAlbumAd && topAlbumImageView.mAdBitmaps != null && topAlbumImageView.mAdBitmaps.length > 0 )
				{
					activity.runOnUiThread( new Runnable() {
						
						@Override
						public void run()
						{
							topAlbumImageView.setImageBitmap( true );
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.e( TAG , "oldTag == null && topDrawables == null!!!!! topAlbumImageView.setImageBitmap( true ) " );
							topAlbumImageView.setHasShows( topAlbumImageView.getHasShows() + 1 );
						}
					} );
				}
				else if( !enableShowAlbumAd && topAlbumImageView.isShowingAd() )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , "oldTag == null && topDrawables == null!!!!! topAlbumImageView.setImageBitmap( false ) " );
					activity.runOnUiThread( new Runnable() {
						
						@Override
						public void run()
						{
							ViewUtils.setImageResourceByTag( topAlbumImageView , true );
							topAlbumImageView.setShowingAd( false );
						}
					} );
				}
			}
			else
			{
				activity.runOnUiThread( new Runnable() {
					
					@Override
					public void run()
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "onMusicAlbumRegionChange  setBackgroundByTag topAlbumImageView = " + topAlbumImageView );
						if( topDrawables != null )
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.e( TAG , "topDrawables!=null   topAlbumImageView.setShowingAd( false )" );
							topAlbumImageView.setShowingAd( false );
						}
						ViewUtils.setImageResourceByTag( topAlbumImageView , true );
						// gaominghui@2016/11/04 ADD START 不用的tag要及时回收掉，不然会引起内存泄漏
						if( oldTag != null && ( oldTag instanceof BitmapDrawable[] ) )
						{
							BitmapDrawable[] oldDrawables = (BitmapDrawable[])oldTag;
							for( BitmapDrawable bitmapDrawable : oldDrawables )
							{
								bitmapDrawable.getBitmap().recycle();
							}
						}
						// gaominghui@2016/11/04 ADD END 不用的tag要及时回收掉，不然会引起内存泄漏
					}
				} );
			}
		}
		
		@Override
		public void onMusicLyricChange(
				List<LyricSentence> list ,
				boolean isLoadCompleted )
		{
			// go to here , may be in thread
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( "onMusicLyricChange!! isLoadCompleted:" , isLoadCompleted ) );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "list = " + list );
			lyricTextView.setLyricSentence( list );
			if( isLoadCompleted )
			{
				if( list != null )
				{
					hasLyrics = true;
					activity.runOnUiThread( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							adTextView.setVisibility( View.INVISIBLE );
						}
					} );
				}
				else
				{
					hasLyrics = false;
					activity.runOnUiThread( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							if( KmobUtil.getInstance().enableShowAd( KmobConfigData.LYRIC_ADPLACE_ID , adTextView.getHasShows() ) )
							{
								setAdTextViewAttribute();
								adTextView.setVisibility( View.VISIBLE );
							}
						}
					} );
				}
			}
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( "onMusicLyricChange!! hasLyrics:" , hasLyrics ) );
		}
		
		@Override
		public void onMusicPositionChange(
				long position ,
				long duration )
		{
			if( lyricsTouching || musicViewPause )
				return;
			// go to here , may be in thread
			if( isShowLyrics ) //gaominghui add //整理获取开关配置的代码
				lyricTextView.setPosition( position , duration );
		}
	};
	
	// 歌词拖动 , start
	private void lyricTextViewTouchDown()
	{
		musicControl.saveLyricMoveStartPosition();
		if( !lyricsTouching )
		{
			lyricsTouching = true;
			lyricTextView.setFastLocate( lyricsTouching );
		}
	}
	
	private void lyricTextViewTouchMove()
	{
		List<LyricSentence> list = lyricTextView.getLyricList();
		if( list != null && list.size() > 0 )
		{
			long position = (long)( viewStartDownY - viewLastDownY ) * configUtils.getInteger( "music_page_drag_scale" ) + musicControl.getLyricMoveStartPosition();
			long duration = musicControl.getCurDuration();
			lyricTextView.setPosition( position , duration );
		}
	}
	
	private void lyricTextViewTouchUp()
	{
		List<LyricSentence> list = lyricTextView.getLyricList();
		if( list != null && list.size() > 0 )
		{
			long position = (long)( viewStartDownY - viewLastDownY ) * configUtils.getInteger( "music_page_drag_scale" ) + musicControl.getLyricMoveStartPosition();
			long duration = musicControl.getCurDuration();
			musicControl.seek( position , duration );
			lyricsTouching = false;
			lyricTextView.setFastLocate( lyricsTouching );
		}
	}
	
	// 歌词拖动 , end
	// 屏幕广播 , start
	private void registerScreenReceiver()
	{
		if( activity != null )
		{
			IntentFilter filter = new IntentFilter();
			filter.addAction( Intent.ACTION_SCREEN_OFF );
			filter.addAction( Intent.ACTION_SCREEN_ON );
			//
			activity.registerReceiver( screenReceiver , filter );
			IntentFilter dateChangedFilter = new IntentFilter();
			dateChangedFilter.addAction( Intent.ACTION_DATE_CHANGED );
			activity.registerReceiver( dateChangedReceiver , dateChangedFilter );
		}
	}
	
	private void unRegisterScreenReceiver()
	{
		if( activity != null )
		{
			activity.unregisterReceiver( screenReceiver );
			activity.unregisterReceiver( dateChangedReceiver );
		}
	}
	
	private BroadcastReceiver dateChangedReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "onReceive dateChangedReceiver!! ACTION:" , intent.getAction() ) );
			adTextView.setHasShows( 0 );
			// TODO Auto-generated method stub
			topAlbumImageView.setHasShows( 0 );
		}
	};
	
	/**
	 * 检查日期是否出现变化,日期变化后保存该日期并将展示次数清空
	 *i_0014299
	 * @author gaominghui 2016年8月11日
	 */
	private void checkDateForTimes()
	{
		if( mCurDateFormat != null ) //gaomignhui add //删除音乐页view时，偶先空指针
		{
			String curDate = mCurDateFormat.format( Calendar.getInstance().getTime() );
			if( !mLastDate.equals( curDate ) )
			{
				adTextView.setHasShows( 0 );
				// TODO Auto-generated method stub
				topAlbumImageView.setHasShows( 0 );
				SharedPreferences sharedPref = activity.getSharedPreferences( SHARED_PREF_NAME , Context.MODE_PRIVATE );
				Editor editor = sharedPref.edit();
				editor.putString( DATE_KEY , curDate );
				editor.commit();
				mLastDate = curDate;
			}
		}
	}
	
	/**
	 * 检查日期是否出现变化,日期变化后保存该日期并将展示次数清空
	 *i_0014299
	 * @author gaominghui 2016年8月11日
	 */
	private void initDateUtil()
	{
		mCurDateFormat = new SimpleDateFormat( "yyyyMMdd" );
		SharedPreferences sharedPref = activity.getSharedPreferences( SHARED_PREF_NAME , Context.MODE_PRIVATE );
		mLastDate = sharedPref.getString( DATE_KEY , mCurDateFormat.format( Calendar.getInstance().getTime() ) );
		Editor editor = sharedPref.edit();
		editor.putString( DATE_KEY , mLastDate );
		editor.commit();
	}
	
	private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			String action = intent.getAction();
			logI( StringUtils.concat( "screenReceiver , action:" , action ) );
			if( action.equals( Intent.ACTION_SCREEN_ON ) )//解锁
			{
				if( musicControl.getPlayStatus() )
					if( android.os.Build.VERSION.SDK_INT >= 19 )
					{
						if( topAlbumRotateAnimator.isPaused() )
							topAlbumRotateAnimator.resume();
						else
							topAlbumRotateAnimator.start();
					}
					else
					{
						if( !topAlbumRotateAnimator.isRunning() )
							topAlbumRotateAnimator.start();
					}
			}
			else if( action.equals( Intent.ACTION_SCREEN_OFF ) )//加锁
			{
				resetViewByTouchDown();
			}
		}
	};
	
	// 屏幕广播 , end
	// 音乐  , end
	public static void logI(
			String message )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , message );
	}
	
	/**
	 *
	 * @see com.cooee.phenix.mediapage.IMediaPlugin#onPageBeginMoving()
	 * @auther gaominghui  2016年7月14日
	 */
	@Override
	public void onPageBeginMoving()
	{
		// TODO Auto-generated method stub
		//curAlbumAnimImageViewTouchDown();
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "onPageBeginMoving!!!!" );
	}
	
	/**
	 *
	 * @see com.cooee.phenix.mediapage.IMediaPlugin#onPageMoveIn()
	 * @auther gaominghui  2016年7月14日
	 */
	@Override
	public void onPageMoveIn()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "onPageMoveIn - hasLyrics:" , hasLyrics ) );
		checkDateForTimes();
		// gaominghui@2016/07/14 ADD START 友盟统计进入音乐页
		MobclickAgent.onEvent( activity , UmengStatistics.MUSIC_PAGE_IN );
		// gaominghui@2016/07/14 ADD END 友盟统计进入音乐页
		// gaominghui@2016/07/15 ADD START 文字广告部分
		if( isShowLyrics && hasLyrics )//gaominghui add //整理获取开关配置的代码
		{
			adTextView.setVisibility( View.INVISIBLE );
		}
		else
		{
			long hasShows = adTextView.getHasShows();
			boolean enableRequestAd = KmobUtil.getInstance().enableRequestAd( KmobConfigData.LYRIC_ADPLACE_ID , hasShows , AdTextView.lastRequestTime );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "onPageMoveIn - enableRequestAd:" , enableRequestAd , "-hasShows:" , hasShows ) );
			if( enableRequestAd )
			{
				adTextView.requestAdItem();
			}
			boolean enableShowAd = KmobUtil.getInstance().enableShowAd( KmobConfigData.LYRIC_ADPLACE_ID , hasShows );
			if( enableShowAd )
			{
				setAdTextViewAttribute();
				adTextView.setVisibility( View.VISIBLE );
			}
		}
		// gaominghui@2016/07/15 ADD END 文字广告部分
		//
		// gaominghui@2016/07/15 ADD START 唱盘广告部分
		long albumHasAdShows = topAlbumImageView.getHasShows();
		boolean albumEnableRequestAd = KmobUtil.getInstance().enableRequestAd( KmobConfigData.ALBUM_ADPLACE_ID , albumHasAdShows , CooeeImageView.lastRequestTime );
		if( albumEnableRequestAd )
		{
			topAlbumImageView.requestAdItem();
		}
		boolean enableAlbumShowAd = KmobUtil.getInstance().enableShowAd( KmobConfigData.ALBUM_ADPLACE_ID , albumHasAdShows );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "onPageMoveIn - albumEnableRequestAd:" , albumEnableRequestAd , "-enableAlbumShowAd:" , enableAlbumShowAd ) );
		if( enableAlbumShowAd && topAlbumImageView.getTag() == null && topAlbumImageView.mAdBitmaps != null && topAlbumImageView.mAdBitmaps.length > 0 )//广告允许展示并且没有封面
		{
			topAlbumImageView.setImageBitmap( true );
			topAlbumImageView.setHasShows( topAlbumImageView.getHasShows() + 1 );
		}
		else if( !enableAlbumShowAd/* && topAlbumImageView.isShowingAd()*/)
		{
			ViewUtils.setImageResourceByTag( topAlbumImageView , true );
			topAlbumImageView.setShowingAd( false );
		}
		// gaominghui@2016/07/15 ADD END 唱盘广告部分
		// gaominghui@2016/07/19 ADD START如果音乐正在播放，唱盘动画暂停了，切页回来要继续动画
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		if( android.os.Build.VERSION.SDK_INT >= 19 )
		{
			if( musicControl.getPlayStatus() && topAlbumRotateAnimator.isPaused() )
			{
				topAlbumRotateAnimator.resume();
			}
		}
		else
		{
			if( musicControl.getPlayStatus() && !topAlbumRotateAnimator.isRunning() )
			{
				topAlbumRotateAnimator.start();
			}
		}
		// gaominghui@2016/12/14 ADD END  兼容android4.0
		// gaominghui@2016/07/19 ADD END如果音乐正在播放，唱盘动画暂停了，切页回来要继续动画
	}
	
	/**
	 *
	 * @see com.cooee.phenix.mediapage.IMediaPlugin#onPageMoveOut()
	 * @auther gaominghui  2016年7月14日
	 */
	@Override
	public void onPageMoveOut()
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "onPageMoveOut!!!" );
		boolean enableShowAd = false;
		// gaominghui@2016/07/15 ADD START 文字广告部分
		enableShowAd = KmobUtil.getInstance().enableShowAd( KmobConfigData.LYRIC_ADPLACE_ID , adTextView.getHasShows() );
		if( !enableShowAd && adTextView.getVisibility() == View.VISIBLE )
		{
			adTextView.setVisibility( View.INVISIBLE );
		}
		// gaominghui@2016/07/15 ADD END 文字广告部分
		//
		// gaominghui@2016/07/15 ADD START 唱盘广告部分
		enableShowAd = KmobUtil.getInstance().enableShowAd( KmobConfigData.ALBUM_ADPLACE_ID , topAlbumImageView.getHasShows() );
		if( !enableShowAd && topAlbumImageView.isShowingAd() )
		{
			ViewUtils.setImageResourceByTag( topAlbumImageView , true );
			topAlbumImageView.setShowingAd( false );
		}
		// gaominghui@2016/07/15 ADD END 唱盘广告部
		// gaominghui@2016/07/19 ADD START如果音乐正在播放，唱盘动画在旋转，切页出去要停掉动画
		if( musicControl.getPlayStatus() && topAlbumRotateAnimator.isStarted() )
		{
			// gaominghui@2016/12/14 ADD START 兼容android4.0
			if( android.os.Build.VERSION.SDK_INT >= 19 )
			{
				topAlbumRotateAnimator.pause();
			}
			else
			{
				topAlbumRotateAnimator.cancel();
			}
			// gaominghui@2016/12/14 ADD END  兼容android4.0
		}
		// gaominghui@2016/07/19 ADD END 如果音乐正在播放，唱盘动画在旋转，切页出去要停掉动画
	}
	
	public synchronized static boolean isFastClick()
	{
		long time = System.currentTimeMillis();
		if( time - lastClickTime < 700 )
		{
			return true;
		}
		lastClickTime = time;
		return false;
	}
	
	// gaominghui@2016/11/14 ADD START 动态计算adTextView的相对位置 0014676: 【桌面】切换主题后，音乐页显示广告名称被截
	/**
	 *设置adTextView的相对位置
	 * @author gaominghui 2016年11月14日
	 */
	private void setAdTextViewAttribute()
	{
		int marginTop = ( musicPageView.getHeight() - album_layoutTopMargin - nameTextViewMargin - singerTextViewMargin - albumAnimLayout.getHeight() - singerNameTextView.getHeight() - musicNameTextView
				.getHeight() );//音乐页除去唱盘和歌曲名，歌手名之后的剩余区域
		if( marginTop <= adTextView.getTextSize() * 2 )//如果剩余区域小于文字广告高度的两倍，则默认的margintop值就是文字广告文字的高度
		{
			ViewUtils.setViewTopMargin( adTextView , singerTextViewMargin );
		}
		else
		{
			int adTextViewMarginTop = marginTop / 2 - singerTextViewMargin;//如果剩余区域高度大于文字广告文字高度的两倍，则需要参考的margintop的值应是剩余区域的一半在减去行间距
			if( adTextViewMarginTopMax >= adTextViewMarginTop )//如果margintop的参考值依旧大于默认配置文件的margintop值，则选择配置文件的margintop值
			{
				ViewUtils.setViewTopMargin( adTextView , adTextViewMarginTop );
			}
			else
			//如果margintop的参考值小于默认配置文件的margintop值，则选择参考值
			{
				ViewUtils.setViewTopMargin( adTextView , adTextViewMarginTopMax );
			}
		}
	}
	
	// gaominghui@2016/11/14 ADD END 动态计算adTextView的相对位置 0014676: 【桌面】切换主题后，音乐页显示广告名称被截
	//gaomignhui add start //整理获取开关配置的代码
	private void initDefaultConfig()
	{
		//是初始化一次，防止后面桌面重新加载view出现空指针等问题
		if( configUtils != null )
		{
			isShowLyrics = configUtils.getBoolean( "music_page_show_lyrics" , false );
			enableLyricsFastLocate = configUtils.getBoolean( "music_page_enable_lyrics_fast_locate" , false );
		}
	}
	//gaominghui add end
}
