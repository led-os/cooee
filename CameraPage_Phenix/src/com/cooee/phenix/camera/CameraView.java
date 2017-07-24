package com.cooee.phenix.camera;


// CameraPage
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.R.color;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.Parameters;
import android.os.FileObserver;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooee.framework.config.ConfigUtils;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.UmengStatistics;
import com.cooee.phenix.camera.control.AdManager;
import com.cooee.phenix.camera.control.CameraControl;
import com.cooee.phenix.camera.control.CameraControlCallBcak;
import com.cooee.phenix.camera.entity.PictureInfo;
import com.cooee.phenix.camera.entity.PictureListViewAdapter;
import com.cooee.phenix.camera.utils.AnimationUtils;
import com.cooee.phenix.camera.utils.BitmapUtils;
import com.cooee.phenix.camera.utils.ColorUtils;
import com.cooee.phenix.camera.utils.EnvironmentUtils;
import com.cooee.phenix.camera.utils.StorageUtil;
import com.cooee.phenix.camera.utils.TimeUtils;
import com.cooee.phenix.camera.utils.ToastUtils;
import com.cooee.phenix.camera.utils.ViewUtils;
import com.cooee.phenix.camera.view.BounceListView;
import com.cooee.phenix.camera.view.BounceScroller;
import com.cooee.phenix.camera.view.BounceScroller.BounceListener;
import com.cooee.phenix.camera.view.BounceScroller.State;
import com.cooee.phenix.camera.view.CameraAdView;
import com.cooee.phenix.camera.view.CameraTextureView;
import com.cooee.phenix.mediapage.IMediaPlugin;
import com.umeng.analytics.MobclickAgent;


public class CameraView implements IMediaPlugin
{
	
	private static final String TAG = "CameraView_phenix";
	private Activity activity = null;
	private long mainThreadId = 0;
	private static CameraView cameraView = null;
	private View cameraPageView = null;
	//
	public static ConfigUtils configUtils = null;
	private CameraControl cameraControl = null;
	//
	//	private List<BottomButtonInfo> bottomBarButtonInfos = new ArrayList<BottomButtonInfo>();
	//child
	private LinearLayout bottomBarLayout = null;
	private TextureView cameraTextureView = null;
	private RelativeLayout cameraLayout = null;
	/**预览部分的布局,会包含默认图片,拍照时的预览,广告图片*/
	private RelativeLayout cameraPreviewLayout = null;
	/**图片显示区域的布局*/
	private RelativeLayout cameraContentLayout = null;
	/**封面及广告的布局*/
	private FrameLayout cameraCoverLayout = null;
	private ImageView cameraCoverImageView = null;
	private ImageView openCameraImageView = null;
	private ImageView focusImageView = null;
	private ImageView switchFlashlightImageView = null;
	private ImageView switchCameraImageView = null;
	private ImageView closeCameraImageView = null;
	private ImageView circleImageView = null;
	private TextView dateTextView = null;
	private BounceListView picturesListView = null;
	/**套在图片listview外层,用于实现回弹效果的一个布局*/
	private BounceScroller bounceLayout = null;
	private PictureListViewAdapter adapter = null;
	/**广告view*/
	private CameraAdView vCameraAd = null;
	/**照片动画中用来做动画的布局,包含一个背景和一张照片*/
	private ViewGroup photoAnimLayout = null;
	/**照片动画中显示新拍摄的照片的view*/
	private ImageView vPhotoAnim = null;
	//
	private String photoSavaPath = "/storage/sdcard0/DCIM/Photo space/";
	//
	private float picturesListViewItemHeight = 0;
	private int marginTop = 0;
	private boolean getPictureListThreadIsRunning = false;
	private boolean needUpdateDate = false;
	private boolean cameraViewPause = false;
	//
	private PopupWindow deletePop = null;
	private View deleteView = null;
	private ImageView deleteImageView = null;
	/**编辑框layout,包含编辑区和计数区*/
	private RelativeLayout deleteEditLayout = null;
	/**编辑区*/
	private EditText deleteEditView = null;
	/**编辑区文字字数的计数*/
	private TextView deleteCountView = null;
	/**编辑框状态,未显示过编辑框*/
	private static final int STATE_NORMAL = 0x01;
	/**编辑框状态,显示过编辑框*/
	private static final int STATE_EDIT = 0x02;
	/**编辑框状态记录*/
	private int mEditState = STATE_NORMAL;
	private Button deleteButton = null;
	private boolean mediaMounted = true;// sd卡在的
	// 保存图片目录
	public final static String FILE_DIR = "DCIM/Photo space/";
	/**最近一次成功操作的时间*/
	private long lastOperateTime = 0;
	private AdManager mAdManager = null;
	/**拍照时,照片向下移动到列表中的动画*/
	private AnimationSet mPhotoAnimationSet = null;
	
	private CameraView()
	{
	}
	
	// 对外接口 , start
	public synchronized static CameraView getInstance()
	{
		if( cameraView == null )
		{
			cameraView = new CameraView();
		}
		return cameraView;
	}
	
	public synchronized void deleteInstance()
	{
		unRegisterObserver();
		unRegisterReceiver();
		// YANGTIANYU@2016/06/16 DEL START
		// TODO hotseat不用管
		//bottomBarButtonInfos.clear();
		// YANGTIANYU@2016/06/16 DEL END
		if( adapter != null )
			adapter.releaseResource();
		stopCamera();
		//		configUtils = null;
		activity = null;
		cameraView = null;
	}
	
	//call this method must be in thread
	public synchronized void initConfig(
			Activity activity /*,
								IconCache iconCache*/)
	{
		this.activity = activity;
		if( BaseDefaultConfig.mConfigUtils != null )
		{
			configUtils = BaseDefaultConfig.mConfigUtils;
		}
		else
		{
			configUtils = new ConfigUtils();
			configUtils.loadConfig( activity.getApplicationContext() , "assets/camera_page/config.xml" );
		}
		//
		// YANGTIANYU@2016/06/27 UPD START
		//photoSavaPath = configUtils.getString( "photo_sava_path" , photoSavaPath );
		photoSavaPath = StringUtils.concat( StorageUtil.getExternalPath() , File.separator , FILE_DIR );
		;
		// YANGTIANYU@2016/06/27 UPD END
		File dirFile = new File( photoSavaPath );
		// 如果文件不存在就创建
		if( !dirFile.exists() )
		{
			dirFile.mkdirs();
			//			MediaScannerConnection.scanFile( activity , new String[]{ photoSavaPath.substring( 0 , photoSavaPath.indexOf( "/Photo space/" ) ) } , null , null );
		}
		//
		// TODO hotseat不用管
		/*bottomBarButtonInfos.clear();
		for( String pckAndClass : configUtils.getStringArray( "camera_page_bottom_bar_pac" ) )
		{
			String[] PAC = pckAndClass.split( ";" );
			if( PAC != null && PAC.length == 2 )
			{
				Intent intent = new Intent();
				intent.setComponent( new ComponentName( PAC[0] , PAC[1] ) );
				Bitmap iconBitmap = iconCache.getIcon( intent );
				@SuppressWarnings( "deprecation" )
				BitmapDrawable drawable = new BitmapDrawable( iconBitmap );
				bottomBarButtonInfos.add( new BottomButtonInfo( drawable , PAC[0] , PAC[1] ) );
			}
		}*/
	}
	
	public synchronized View getCameraPageView(
			Activity activity /*,
								IconCache iconCache */)
	{
		this.mainThreadId = Thread.currentThread().getId();
		if( cameraPageView == null )
		{
			LayoutInflater inflater = activity.getLayoutInflater();
			cameraPageView = inflater.inflate( R.layout.camera_page_view_layout , null );
			getView();
			setViewAttribute( activity );
			setListener();
			cameraControl = new CameraControl( focusImageView , activity , callBcak , photoSavaPath );
			// YANGTIANYU@2016/07/02 ADD START
			( (CameraTextureView)this.cameraTextureView ).setCameraControl( cameraControl );
			mAdManager = new AdManager( activity , cameraCoverImageView , vCameraAd );
			// YANGTIANYU@2016/07/02 ADD END
			startGetPictureListThread();
			registerScreenReceiver();
			registerMediaReceiver();
			registerObserver();
		}
		return cameraPageView;
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
		//
		// YANGTIANYU@2016/06/16 DEL START
		// TODO hotseat不用管
		//addBottomBarButton( activity );
		// YANGTIANYU@2016/06/16 DEL END
		return bottomBarLayout;
	}
	
	public void onResume()
	{
		if( cameraViewPause )
		{
		}
		//
		cameraViewPause = false;
	}
	
	public void onPause()
	{
		if( !cameraViewPause )
		{
		}
		//
		cameraViewPause = true;
		stopCamera();
	}
	
	/**
	 * 关闭相机
	 * 如果此时为点击拍照到回调onPictureTaken方法之间,
	 * 则先隐藏预览界面,等回调onPictureTaken方法后完全关闭相机,否则相机会出现异常
	 * i_0014249
	 * @author yangtianyu 2016-8-3
	 */
	public void stopCamera()
	{
		// gaominghui@2017/01/04 ADD START 0014805: 从双层模式切换至单层模式，桌面加载过程中按HOME键，桌面会出现重启现象,此时cameraControl为null导致空指针
		if( null != cameraControl )
		// gaominghui@2017/01/04 ADD END 0014805: 从双层模式切换至单层模式，桌面加载过程中按HOME键，桌面会出现重启现象,此时cameraControl为null导致空指针
		{
			if( !cameraControl.ifCanCloseCamera() )
			{
				logI( "closeCamera When Taking Photo" );
				if( mainThreadId == Thread.currentThread().getId() )
				{
					resetViewsAfterCloseCamera();
				}
				else
					cameraPageView.post( new Runnable() {
						
						@Override
						public void run()
						{
							resetViewsAfterCloseCamera();
						}
					} );
				cameraControl.setNeedCloseAfterTakePhoto( true );
				return;
			}
			else
			{
				cameraControl.setNeedCloseAfterTakePhoto( false );
				cameraControl.closeCamera();
			}
		}
	}
	
	// 对外接口 , end
	// TODO hotseat不用管
	/*private void addBottomBarButton(
			Activity activity )
	{
		//		bottomBarLayout
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
	}*/
	private void getView()
	{
		this.cameraLayout = (RelativeLayout)this.cameraPageView.findViewById( R.id.camera_page_camera_layout );
		// YANGTIANYU@2016/06/23 ADD START
		this.cameraPreviewLayout = (RelativeLayout)this.cameraPageView.findViewById( R.id.camera_page_camera_preview_layout );
		this.cameraContentLayout = (RelativeLayout)this.cameraPageView.findViewById( R.id.camera_page_camera_content_layout );
		this.cameraCoverLayout = (FrameLayout)this.cameraPageView.findViewById( R.id.camera_page_camera_cover_layout );
		this.vCameraAd = (CameraAdView)this.cameraPageView.findViewById( R.id.camera_page_camera_preview_ad );
		this.photoAnimLayout = (ViewGroup)this.cameraPageView.findViewById( R.id.camera_page_camera_photo_anim_layout );
		this.vPhotoAnim = (ImageView)this.cameraPageView.findViewById( R.id.camera_page_camera_photo_anim_view );
		// YANGTIANYU@2016/06/23 ADD END
		this.cameraCoverImageView = (ImageView)this.cameraPageView.findViewById( R.id.camera_page_camera_cover );
		this.openCameraImageView = (ImageView)this.cameraPageView.findViewById( R.id.camera_page_open_camera );
		this.focusImageView = (ImageView)this.cameraPageView.findViewById( R.id.camera_page_focus_view );
		this.switchCameraImageView = (ImageView)this.cameraPageView.findViewById( R.id.camera_page_switch_camera_image_view );
		this.closeCameraImageView = (ImageView)this.cameraPageView.findViewById( R.id.camera_page_close_camera_image_view );
		this.switchFlashlightImageView = (ImageView)this.cameraPageView.findViewById( R.id.camera_page_switch_flashlight_image_view );
		this.circleImageView = (ImageView)this.cameraPageView.findViewById( R.id.camera_page_circle_image_view );
		this.dateTextView = (TextView)this.cameraPageView.findViewById( R.id.camera_page_date_text_view );
		this.picturesListView = (BounceListView)this.cameraPageView.findViewById( R.id.camera_page_pictures_list_view );
		// YANGTIANYU@2016/07/08 ADD START
		this.bounceLayout = (BounceScroller)this.cameraPageView.findViewById( R.id.camera_page_bounce_layout );
		// YANGTIANYU@2016/07/08 ADD END
		//
		this.cameraTextureView = new CameraTextureView( activity );
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.MATCH_PARENT );
		params.addRule( RelativeLayout.CENTER_IN_PARENT );
		this.cameraTextureView.setLayoutParams( params );
		params = null;
	}
	
	private void setViewAttribute(
			Activity activity )
	{
		marginTop = configUtils.getInteger( "camera_page_camera_view_magin_top" );
		ViewUtils.setViewTopMargin( cameraLayout , marginTop );
		ViewUtils.setViewTopMargin( photoAnimLayout , marginTop );
		ViewUtils.setViewTopMargin( cameraContentLayout , marginTop );
		//
		this.dateTextView.setTextSize( TypedValue.COMPLEX_UNIT_PX , configUtils.getInteger( "camera_page_date_font_size" , 15 ) );
		this.dateTextView.setTextColor( ColorUtils.getLyricsColor( configUtils.getString( "camera_page_date_font_color" ) , color.black ) );
		//
		this.adapter = new PictureListViewAdapter( activity );
		this.adapter.setItemFontColor( ColorUtils.getLyricsColor( configUtils.getString( "camera_page_item_font_color" ) , color.black ) );
		this.adapter.setItemFontSize( configUtils.getInteger( "camera_page_item_font_size" , 15 ) );
		this.adapter.setEntryGalleryFontColor( ColorUtils.getLyricsColor( configUtils.getString( "camera_page_item_text_font_color" ) , color.black ) );
		this.adapter.setEntryGalleryFontSize( configUtils.getInteger( "camera_page_item_text_font_size" , 15 ) );
		float listViewHeight = configUtils.getInteger( "camera_page_list_view_height" , 15 );
		if( listViewHeight > 0 )
		{
			ViewGroup.LayoutParams params = this.picturesListView.getLayoutParams();
			params.height = (int)listViewHeight;
			this.picturesListView.setLayoutParams( params );
		}
		this.picturesListView.setAdapter( this.adapter );
		//
		Resources resources = activity.getResources();
		picturesListViewItemHeight = resources.getDimension( R.dimen.camera_page_picture_item_height );
	}
	
	private void startGetPictureListThread()
	{
		getPictureListThreadIsRunning = true;
		new GetPictureListThread( activity , photoSavaPath , new GetPictureListThread.CallBack() {
			
			@Override
			public void loadPictureListCompleted(
					List<PictureInfo> list )
			{
				needUpdateDate = true;
				//
				adapter.releaseResource();
				adapter.setList( list );
				getPictureListThreadIsRunning = false;
			}
		} ).start();
	}
	
	private void setListener()
	{
		this.openCameraImageView.setOnClickListener( clickListener );
		this.closeCameraImageView.setOnClickListener( clickListener );
		this.switchCameraImageView.setOnClickListener( clickListener );
		this.switchFlashlightImageView.setOnClickListener( clickListener );
		// YANGTIANYU@2016/07/01 ADD START
		this.vCameraAd.setOnClickListener( clickListener );
		this.bounceLayout.setListener( bounceListener );
		this.picturesListView.setOverScrollListener( this.bounceLayout );
		// YANGTIANYU@2016/07/01 ADD END
		this.picturesListView.setOnItemClickListener( onItemClickListener );
		this.picturesListView.setOnScrollListener( onScrollListener );
		this.cameraTextureView.setSurfaceTextureListener( new SurfaceTextureListener() {
			
			@Override
			public void onSurfaceTextureUpdated(
					SurfaceTexture surface )
			{
			}
			
			@Override
			public void onSurfaceTextureSizeChanged(
					SurfaceTexture surface ,
					int width ,
					int height )
			{
			}
			
			@Override
			public boolean onSurfaceTextureDestroyed(
					SurfaceTexture surface )
			{
				surface.release();
				cameraTextureView.destroyDrawingCache();
				return false;
			}
			
			@Override
			public void onSurfaceTextureAvailable(
					SurfaceTexture surface ,
					int width ,
					int height )
			{
				cameraControl.openCamera( surface );
			}
		} );
	}
	
	private CameraControlCallBcak callBcak = new CameraControlCallBcak() {
		
		@Override
		public void openCamera(
				final boolean success ,
				final boolean isBack )
		{
			if( mainThreadId == Thread.currentThread().getId() )
				openCameraByMainThread( success , isBack );
			else
				cameraPageView.post( new Runnable() {
					
					@Override
					public void run()
					{
						openCameraByMainThread( success , isBack );
					}
				} );
		}
		
		@Override
		public void closeCamera()
		{
			if( mainThreadId == Thread.currentThread().getId() )
			{
				resetViewsAfterCloseCamera();
				removePreview();
			}
			else
				// YANGTIANYU@2016/09/21 UPD START
				// 【i_0014515】关闭相机的操作以桌面的context来执行,防止相机页被移除后无法执行到
				//cameraPageView.post( new Runnable() {
				activity.runOnUiThread( new Runnable() {
					
					// YANGTIANYU@2016/09/21 UPD END
					@Override
					public void run()
					{
						resetViewsAfterCloseCamera();
						removePreview();
					}
				} );
		}
		
		@Override
		public void tackPictureSuccess(
				final PictureInfo info )
		{
			needUpdateDate = true;
			final BitmapDrawable newPhoto = info.getDrawable();
			info.setDrawable( new BitmapDrawable( activity.getResources() , Bitmap.createBitmap( 1 , 1 , Bitmap.Config.ARGB_4444 ) ) );
			adapter.addItem( info , picturesListView );
			if( mainThreadId == Thread.currentThread().getId() )
				startPhotoAnim( info , newPhoto );
			else
				// YANGTIANYU@2016/09/21 UPD START
				// 【i_0014515】
				//cameraPageView.post( new Runnable() {
				activity.runOnUiThread( new Runnable() {
					
					// YANGTIANYU@2016/09/21 UPD END
					@Override
					public void run()
					{
						startPhotoAnim( info , newPhoto );
					}
				} );
		}
		
		@Override
		public void tackPictureFail()
		{
			ToastUtils.showToast( activity , R.string.camera_page_pictures_failed );
			cameraControl.setTakePhotoing( false );
		}
		
		@Override
		public void changeFlashlight(
				String Status )
		{
			int flashImgId = R.drawable.camera_page_light_close;
			if( Parameters.FLASH_MODE_AUTO.equals( Status ) )
				flashImgId = R.drawable.camera_page_light_auto;
			else if( Parameters.FLASH_MODE_ON.equals( Status ) )
				flashImgId = R.drawable.camera_page_light_open;
			changeFlashImg( flashImgId );
		}
		
		private void startPhotoAnim(
				final PictureInfo info ,
				final BitmapDrawable newPhoto )
		{
			photoAnimLayout.setVisibility( View.VISIBLE );
			// YANGTIANYU@2016/07/14 ADD START
			// 加个禁止滑动的
			bounceLayout.setEnabled( false );
			// YANGTIANYU@2016/07/14 ADD END
			// gaominghui@2016/12/14 ADD START
			//vPhotoAnim.setBackground( newPhoto );
			vPhotoAnim.setBackgroundDrawable( newPhoto );
			// gaominghui@2016/12/14 ADD END
			float targetY = activity.getResources().getDimension( R.dimen.camera_page_camera_content_layout_margin_top );
			float layoutWidth = activity.getResources().getDimension( R.dimen.camera_page_photo_anim_layout_width );
			float layoutHeight = activity.getResources().getDimension( R.dimen.camera_page_photo_anim_layout_height );
			photoAnimLayout.setPivotX( photoAnimLayout.getX() + layoutWidth * 0.75f );
			photoAnimLayout.setPivotY( photoAnimLayout.getY() + layoutHeight * 0.25f + targetY );
			mPhotoAnimationSet = AnimationUtils.getPhotoAnimation( photoAnimLayout , new AnimationListener() {
				
				@Override
				public void onAnimationStart(
						Animation animation )
				{
					logI( "cyk_bug:i_0014531: mPhotoAnimationSet onAnimationStart " );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
				}
				
				@Override
				public void onAnimationEnd(
						Animation animation )
				{
					if( animation instanceof TranslateAnimation )
					{
						logI( "cyk_bug:i_0014531: mPhotoAnimationSet onAnimationEnd 平移结束 " );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
						// 平移结束
						info.setDrawable( newPhoto );
						adapter.notifyDataSetChanged();
					}
					else if( animation instanceof AnimationSet )
					{
						logI( "cyk_bug:i_0014531: mPhotoAnimationSet onAnimationEnd 所有动画结束 " );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
						// 所有动画结束
						// gaominghui@2016/12/14 ADD START
						//vPhotoAnim.setBackground( null );
						vPhotoAnim.setBackgroundDrawable( null );
						// gaominghui@2016/12/14 ADD END
						photoAnimLayout.setVisibility( View.GONE );
						cameraControl.setTakePhotoing( false );
						// YANGTIANYU@2016/07/14 ADD START
						// 取消禁止滑动
						bounceLayout.setEnabled( true );
						// YANGTIANYU@2016/07/14 ADD END
						mPhotoAnimationSet = null;
					}
					else
					{
						logI( "cyk_bug:i_0014531: mPhotoAnimationSet onAnimationEnd error " );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
					}
				}
				
				@Override
				public void onAnimationRepeat(
						Animation animation )
				{
					logI( "cyk_bug:i_0014531: mPhotoAnimationSet onAnimationRepeat " );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
				}
			} , targetY );
		}
		
		/**
		 * 根据图片id改变闪光灯状态显示的内容。
		 * @param flashImgId 需要显示的图片id
		 * @author yangtianyu 2016-6-24
		 */
		private void changeFlashImg(
				final int flashImgId )
		{
			if( mainThreadId == Thread.currentThread().getId() )
				switchFlashlightImageView.setImageResource( flashImgId );
			else
				cameraPageView.post( new Runnable() {
					
					@Override
					public void run()
					{
						switchFlashlightImageView.setImageResource( flashImgId );
					}
				} );
		}
	};
	
	private void openCameraByMainThread(
			boolean success ,
			boolean isBack )
	{
		if( success )
		{
			ViewUtils.setVisibility( switchCameraImageView , View.VISIBLE );
			ViewUtils.setVisibility( closeCameraImageView , View.VISIBLE );
			//
			if( isBack )
				ViewUtils.setVisibility( switchFlashlightImageView , View.VISIBLE );
			else
				ViewUtils.setVisibility( switchFlashlightImageView , View.GONE );
			//
			ViewUtils.setVisibility( cameraCoverLayout , View.GONE );
		}
		else
		{
			ToastUtils.showToast( activity , R.string.camera_page_camera_used );
			stopCamera();
		}
	}
	
	/**
	 * 在关闭相机后重置界面内容,只能在主线程中调用
	 * @author yangtianyu 2016-6-29
	 */
	private void resetViewsAfterCloseCamera()
	{
		ViewUtils.setVisibility( cameraCoverLayout , View.VISIBLE );
		ViewUtils.setVisibility( openCameraImageView , View.VISIBLE );
		//
		ViewUtils.setVisibility( switchCameraImageView , View.GONE );
		ViewUtils.setVisibility( closeCameraImageView , View.GONE );
		//
		ViewUtils.setVisibility( switchFlashlightImageView , View.GONE );
	}
	
	/**
	 * 显示出照片删除界面后，点击照片框的事件响应，显示出编辑框
	 * @author yangtianyu 2016-7-29
	 */
	private void onClickDeleteImage()
	{
		mEditState = STATE_EDIT;
		deleteEditLayout.setVisibility( View.VISIBLE );
		deleteEditView.setText( null );
		deleteCountView.setText( "0 / 15" );
		deleteEditView.requestFocus();
		InputMethodManager imm = (InputMethodManager)activity.getSystemService( Context.INPUT_METHOD_SERVICE );
		imm.showSoftInput( deleteEditView , InputMethodManager.SHOW_IMPLICIT );
		deleteEditView.addTextChangedListener( new TextWatcher() {
			
			@Override
			public void onTextChanged(
					CharSequence s ,
					int start ,
					int before ,
					int count )
			{
			}
			
			@Override
			public void beforeTextChanged(
					CharSequence s ,
					int start ,
					int count ,
					int after )
			{
			}
			
			@Override
			public void afterTextChanged(
					Editable s )
			{
				int count = deleteEditView.getText().length();
				deleteCountView.setText( StringUtils.concat( count , " / 15" ) );
			}
		} );
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(
				View v )
		{
			logI( "cyk_bug:i_0014531: onClick v: " + v );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
			if( !checkOperateTime() )
				return;
			if( v == openCameraImageView && !cameraControl.isCameraBeingClose() )
			{
				logI( "cyk_bug:i_0014531: onClick  cameraControl.getCameraOpened() : " + ( cameraControl.getCameraOpened() ) );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
				// 打开相机
				if( cameraControl.getCameraOpened() )
				{
					// 拍照
					if( getPictureListThreadIsRunning )
						ToastUtils.showToast( activity , R.string.camera_page_preview_being_prepared );
					else if( !mediaMounted )
						ToastUtils.showToast( activity , R.string.camera_page_insert_sd_card );
					else
						cameraControl.takePhotos();
				}
				else
				{
					openCamera();
					mAdManager.startPreview();
				}
				//				cameraControl.openCamera( cameraTextureView.getSurfaceTexture() );
				//				ViewUtils.setVisibility( cameraTextureView , View.VISIBLE );
				//				ViewUtils.addView( cameraLayout , cameraTextureView , 0 );
			}
			else if( v == closeCameraImageView )
			{
				stopCamera();
				mAdManager.stopPreview();
				//				ViewUtils.setVisibility( cameraCoverImageView , View.VISIBLE );
				//				ViewUtils.setVisibility( cameraTextureView , View.GONE );
			}
			else if( v == switchCameraImageView )
			{
				cameraTextureView.destroyDrawingCache();
				cameraControl.toggleCamera( cameraTextureView.getSurfaceTexture() );
			}
			else if( v == switchFlashlightImageView )
			{
				cameraControl.toggleFlashlight();
			}
			else if( v == deleteButton )
			{
				PictureInfo info = (PictureInfo)deleteButton.getTag();
				// YANGTIANYU@2016/09/09 UPD START
				// 使用相机页，快速乱序删除已经拍摄的图片，可能会出现一次性删除2张照片，多次拍照后删除，桌面重启.【i_0014443】
				//if( EnvironmentUtils.deleteFile( info.getPicturePath() ) )
				//{
				//	needUpdateDate = true;
				//	BitmapUtils.recycleBitmapDrawable( info.getDrawable() );
				//	adapter.deleteItem( info.getPicturePath() );
				//	//
				//	cameraControl.notifySystemScanPic( activity , new File( info.getPicturePath() ) );
				//}
				//if( deletePop != null && deletePop.isShowing() )
				//	deletePop.dismiss();
				if( deletePop != null && deletePop.isShowing() )
					deletePop.dismiss();
				EnvironmentUtils.deleteFile( info.getPicturePath() );
				// YANGTIANYU@2016/09/09 UPD END
				// YANGTIANYU@2016/06/30 ADD START
				// 友盟统计
				MobclickAgent.onEvent( activity , UmengStatistics.DELETE_CLICK );
				// YANGTIANYU@2016/06/30 ADD END
			}
			else if( v == deleteView )
			{
				if( deletePop != null && deletePop.isShowing() )
					deletePop.dismiss();
			}
			// YANGTIANYU@2016/07/28 ADD START
			else if( v == deleteImageView )
			{
				onClickDeleteImage();
			}
			// YANGTIANYU@2016/07/28 ADD END
			else if( v == vCameraAd )
			{
				mAdManager.onClick();
			}
			// TODO hotseat不用管
			/*else
			{
				Object tag = v.getTag();
				if( tag instanceof BottomButtonInfo )
				{
					BottomButtonInfo info = (BottomButtonInfo)tag;
					Intent intent = new Intent();
					intent.setClassName( info.getPackName() , info.getClassName() );
					if( v.getContext().getPackageManager().queryIntentActivities( intent , 0 ).size() != 0 )
					{
						v.getContext().startActivity( intent );
						stopCamera();
					}
					else
						ToastUtils.showToast( activity , R.string.application_not_installed );
				}
			}*/
		}
	};
	private OnItemClickListener onItemClickListener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(
				AdapterView<?> parent ,
				View view ,
				int position ,
				long id )
		{
			if( mediaMounted )
			{
				if( deletePop == null || !deletePop.isShowing() )
				{
					if( position != CameraControl.SHOW_PICTURE_COUNT )
					{
						if( adapter.getList().size() != 0 )
						{
							showDeletePop( adapter.getList().get( position ) );
							stopCamera();
						}
					}
					else
					{
						String pac = configUtils.getString( "camera_page_item_text_pac" );
						if( !TextUtils.isEmpty( pac ) )
						{
							String[] pacs = pac.split( ";" );
							Intent intent = new Intent();
							intent.setClassName( pacs[0] , pacs[1] );
							if( view.getContext().getPackageManager().queryIntentActivities( intent , 0 ).size() != 0 )
								view.getContext().startActivity( intent );
							else
								ToastUtils.showToast( activity , R.string.camera_page_application_not_installed );
						}
					}
				}
			}
			else
			{
				ToastUtils.showToast( activity , R.string.camera_page_insert_sd_card );
			}
		}
	};
	private OnScrollListener onScrollListener = new OnScrollListener() {
		
		private float lastFirstVisibleItem = -1;
		
		@Override
		public void onScrollStateChanged(
				AbsListView view ,
				int scrollState )
		{
			logI( "onScrollListener , onScrollStateChanged" );
		}
		
		@Override
		public void onScroll(
				AbsListView view ,
				int firstVisibleItem ,
				int visibleItemCount ,
				int totalItemCount )
		{
			logI( "onScrollListener , onScroll" );
			//
			View firstChild = view.getChildAt( 0 );
			if( firstChild != null )
			{
				float scrollY = picturesListViewItemHeight * firstVisibleItem + Math.abs( firstChild.getTop() );
				ViewUtils.setRotation( circleImageView , scrollY );
			}
			//
			if( lastFirstVisibleItem != firstVisibleItem || needUpdateDate )
			{
				if( adapter.getList().size() > 0 )
				{
					String curDate = dateTextView.getText().toString();
					String thisDate = adapter.getList().get( firstVisibleItem ).getPictureDate();
					StringBuffer buffer = new StringBuffer( thisDate );
					buffer.append( " " );
					buffer.append( activity.getResources().getStringArray( R.array.camera_page_weekday )[adapter.getList().get( firstVisibleItem ).getPictureWeek()] );
					thisDate = buffer.toString();
					if( !curDate.equals( thisDate ) )
					{
						dateTextView.setText( thisDate );
						AnimationDrawable animator = (AnimationDrawable)circleImageView.getBackground();
						animator.stop();
						animator.start();
					}
				}
				else
				{
					dateTextView.setText( TimeUtils.getCurrentDate( activity ) );
				}
				//
				lastFirstVisibleItem = firstVisibleItem;
				needUpdateDate = false;
			}
		}
	};
	private BounceListener bounceListener = new BounceListener() {
		
		@Override
		public void onState(
				boolean header ,
				State state )
		{
		}
		
		@Override
		public void onOffset(
				boolean header ,
				int offset )
		{
		}
		
		@Override
		public void onScrollDelta(
				int delta )
		{
			ViewUtils.setRotation( circleImageView , circleImageView.getRotation() - delta );
		}
	};
	
	private void openCamera()
	{
		ViewUtils.addView( cameraPreviewLayout , cameraTextureView , 0 );
	}
	
	private void removePreview()
	{
		ViewUtils.removeView( cameraPreviewLayout , cameraTextureView );
		//		cameraLayout.removeView( cameraTextureView );
		//		if( cameraControl != null && cameraControl.getCameraOpened() && cameraLayout != null && cameraTextureView != null )
		//		{
		//			CameraControlTaskManager.stopAllTask( null );
		//			ViewUtils.setVisibility( cameraCoverImageView , View.VISIBLE );
		//			ViewUtils.setVisibility( cameraTextureView , View.GONE );
		//			cameraControl.closeCamera();
		//			
		//		}
	}
	
	// delete , start
	@SuppressWarnings( "deprecation" )
	private void initPopupWindow()
	{
		deleteView = activity.getLayoutInflater().inflate( R.layout.camera_page_delete_item_layout , null );
		deletePop = new PopupWindow( deleteView , ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT );
		deletePop.setBackgroundDrawable( new BitmapDrawable() );
		deletePop.setOutsideTouchable( true );
		deletePop.setFocusable( true );
		deleteImageView = (ImageView)deleteView.findViewById( R.id.camera_page_delete_image_view );
		deleteEditLayout = (RelativeLayout)deleteView.findViewById( R.id.camera_page_delete_edit_layout );
		deleteEditView = (EditText)deleteView.findViewById( R.id.camera_page_delete_edit_view );
		deleteCountView = (TextView)deleteView.findViewById( R.id.camera_page_delete_edit_text_count );
		deleteButton = (Button)deleteView.findViewById( R.id.camera_page_delete_button );
		deleteButton.setOnClickListener( clickListener );
		deleteView.setOnClickListener( clickListener );
		deleteImageView.setOnClickListener( clickListener );
		deletePop.setOnDismissListener( new OnDismissListener() {
			
			@Override
			public void onDismiss()
			{
				deleteEditView.clearFocus();
				deleteEditLayout.setVisibility( View.INVISIBLE );
				Bitmap photoBmp = null;
				Drawable drawable = deleteImageView.getDrawable();
				if( drawable != null && drawable instanceof BitmapDrawable )
				{
					photoBmp = ( (BitmapDrawable)drawable ).getBitmap();
					photoBmp = BitmapUtils.cropPhotoFromBmp( activity , photoBmp , 1 , true );
				}
				PictureInfo info = (PictureInfo)deleteImageView.getTag();
				if( mEditState == STATE_EDIT && photoBmp != null && info != null )
				{
					String photoPath = info.getPicturePath();
					String newText = deleteEditView.getEditableText().toString();
					BitmapUtils.savePhoto( activity , photoBmp , newText , photoPath , true );
				}
				mEditState = STATE_NORMAL;
			}
		} );
	}
	
	public void showDeletePop(
			PictureInfo info )
	{
		//
		if( deletePop == null )
			initPopupWindow();
		//
		Drawable drawable = BitmapUtils.getBitmapDrawableByPath( activity , info.getPicturePath() , 1 , true );
		//chenliang add start	//解决“相机页拍照后，删除一张照片后，再次点击照片偶先只显示“删除”字样，不显示照片”的问题。【i_0014761】
		if( drawable == null )
		{//该bug复现的几率很低，所以添加保护，防止点击图片后只显示删除按钮。
			return;
		}
		//chenliang add end
		deleteImageView.setImageDrawable( drawable );
		deleteImageView.setTag( info );
		deleteButton.setTag( info );
		//
		if( !deletePop.isShowing() )
			if( cameraLayout != null )
				deletePop.showAtLocation( cameraLayout , Gravity.TOP | Gravity.CENTER_HORIZONTAL , 0 , 0 );
	}
	
	// delete , end
	// 屏幕广播 , start
	private void registerScreenReceiver()
	{
		if( activity != null )
		{
			IntentFilter filter = new IntentFilter();
			filter.addAction( Intent.ACTION_SCREEN_OFF );
			filter.addAction( Intent.ACTION_SCREEN_ON );
			filter.addAction( Intent.ACTION_CLOSE_SYSTEM_DIALOGS );//home
			//
			activity.registerReceiver( receiver , filter );
		}
	}
	
	// 屏幕广播 , end
	// sdcard , start
	private void registerMediaReceiver()
	{
		if( activity != null )
		{
			IntentFilter mediaFilter = new IntentFilter();
			mediaFilter.addAction( Intent.ACTION_MEDIA_EJECT );
			mediaFilter.addAction( Intent.ACTION_MEDIA_MOUNTED );
			mediaFilter.addAction( Intent.ACTION_MEDIA_UNMOUNTED );
			mediaFilter.addDataScheme( "file" );
			activity.registerReceiver( receiver , mediaFilter );
		}
	}
	
	// sdcard , end
	private void unRegisterReceiver()
	{
		if( activity != null )
			activity.unregisterReceiver( receiver );
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			String action = intent.getAction();
			logI( StringUtils.concat( "screenReceiver , action:" , action ) );
			if( action.equals( Intent.ACTION_SCREEN_ON ) )//解锁
			{
			}
			else if( action.equals( Intent.ACTION_SCREEN_OFF ) )//加锁
			{
				stopCamera();
			}
			else if( action.equals( Intent.ACTION_CLOSE_SYSTEM_DIALOGS ) )//home
			{
				stopCamera();
			}
			//
			else if( Intent.ACTION_MEDIA_EJECT.equals( action ) )
			{
				mediaMounted = false;
				adapter.setList( new LinkedList<PictureInfo>() );
			}
			else if( Intent.ACTION_MEDIA_MOUNTED.equals( action ) )
			{
				mediaMounted = true;
				startGetPictureListThread();
			}
			else if( Intent.ACTION_MEDIA_UNMOUNTED.equals( action ) )
			{
			}
		}
	};
	// 文件夹监听 , start
	private PhotoDirObserver photoDirObserver = null;
	
	private void registerObserver()
	{
		if( photoDirObserver == null )
			photoDirObserver = new PhotoDirObserver( photoSavaPath );
		photoDirObserver.startWatching();
	}
	
	private void unRegisterObserver()
	{
		if( photoDirObserver != null )
			photoDirObserver.stopWatching();
		photoDirObserver = null;
	}
	
	private class PhotoDirObserver extends FileObserver
	{
		
		public PhotoDirObserver(
				String path )
		{
			super( path );
		}
		
		@Override
		public void onEvent(
				int event ,
				String path )
		{
			//			logI( "PhotoDirObserver , photo change , event : " + event );
			//			logI( "PhotoDirObserver , photo change , path : " + path );
			switch( event )
			{
				case FileObserver.DELETE://512
				case FileObserver.DELETE_SELF://1024
					if( adapter != null )
					{
						logI( "PhotoDirObserver , switch , deleteItem" );
						needUpdateDate = true;
						adapter.deleteItem( StringUtils.concat( photoSavaPath , path ) );
					}
					break;
				case FileObserver.MOVE_SELF://2048
				case FileObserver.MOVED_TO://128
				case FileObserver.CLOSE_WRITE://8
					if( cameraControl != null && !cameraControl.takePhotoing() )
					{
						logI( StringUtils.concat( "PhotoDirObserver , switch , startGetPictureListThread , event:" , event ) );
						startGetPictureListThread();
					}
					break;
				default:
					break;
			}
		}
	}
	
	// 文件夹监听 , end
	/**
	 * 检查本次操作与上一次有效操作的间隔时间,判断是否允许操作。
	 * @return 是否允许操作,true为允许,false为不允许
	 * @author yangtianyu 2016-8-4
	 */
	public synchronized boolean checkOperateTime()
	{
		boolean result = false;
		long curTime = System.currentTimeMillis();
		if( Math.abs( curTime - lastOperateTime ) > 500 )
		{
			lastOperateTime = curTime;
			result = true;
		}
		//
		logI( StringUtils.concat( "checkOperateTime():" , result ) );
		//
		return result;
	}
	
	public static void logI(
			String message )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , message );
	}
	
	/**
	 * 如果拍照动画未完成,则停止动画
	 * 【i_0014515】从桌面临时移除专属页时,如果动画未完成,需终止
	 * @author yangtianyu 2016-9-21
	 */
	private void stopPhotoAnim()
	{
		logI( "cyk_bug:i_0014531: stopPhotoAnim 0... " );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( mPhotoAnimationSet != null && !mPhotoAnimationSet.hasEnded() )
		{
			List<Animation> anims = mPhotoAnimationSet.getAnimations();
			Animation tmp = null;
			for( int i = 0 ; i < anims.size() ; i++ )
			{
				logI( "cyk_bug:i_0014531: stopPhotoAnim 1... i: " + i );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
				tmp = anims.get( i );
				if( tmp != null && !tmp.hasEnded() )
				{
					logI( "cyk_bug:i_0014531: stopPhotoAnim 2... i: " + i );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
					tmp.cancel();
				}
			}
			logI( "cyk_bug:i_0014531: stopPhotoAnim 3... " );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
			mPhotoAnimationSet.cancel();
		}
		logI( "cyk_bug:i_0014531: stopPhotoAnim 4... " );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
	}
	
	/**
	 * 相机专属页从屏幕上被移除（拖动图标时的临时移除）
	 * 【i_0014515】从桌面临时移除专属页时,需关闭相机,终止动画
	 * @author yangtianyu 2016-9-21
	 */
	public void onRemoveFromScreen()
	{
		stopCamera();
		stopPhotoAnim();
	}
	
	@Override
	public void onPageBeginMoving()
	{
		stopCamera();
	}
	
	@Override
	public void onPageMoveIn()
	{
		// YANGTIANYU@2016/06/30 ADD START
		// 友盟统计
		MobclickAgent.onEvent( activity , UmengStatistics.CAMERA_PAGE_IN );
		if( mAdManager != null )
			mAdManager.onPageMoveIn();
		// YANGTIANYU@2016/06/30 ADD END
	}
	
	@Override
	public void onPageMoveOut()
	{
		if( mAdManager != null )
			mAdManager.onPageMoveOut();
	}
	
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	public void hideDeletePop()
	{
		if( deletePop != null && deletePop.isShowing() )
			deletePop.dismiss();
	}
	//zhujieping add end
}
