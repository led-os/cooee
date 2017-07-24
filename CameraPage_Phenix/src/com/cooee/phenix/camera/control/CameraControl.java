package com.cooee.phenix.camera.control;


// CameraPage
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.UmengStatistics;
import com.cooee.phenix.camera.CameraView;
import com.cooee.phenix.camera.CooeeAutoFocusObject;
import com.cooee.phenix.camera.R;
import com.cooee.phenix.camera.control.CameraControlTask.RunningTask;
import com.cooee.phenix.camera.entity.PictureInfo;
import com.cooee.phenix.camera.utils.BitmapUtils;
import com.cooee.phenix.camera.utils.CameraUtils;
import com.cooee.phenix.camera.utils.EnvironmentUtils;
import com.cooee.phenix.camera.utils.ToastUtils;
import com.umeng.analytics.MobclickAgent;


public class CameraControl
{
	
	private Activity activity = null;
	private CameraControlCallBcak callBcak = null;
	private int frameHeight = 0;
	private boolean cameraBeingOpen = false;
	/**是否正在关闭相机*/
	private boolean cameraBeingClose = false;
	//
	private Camera camera = null;
	private MediaPlayer mp = null;
	private int cameraCount = 0;
	private Camera.CameraInfo[] cameraInfos = null;
	private int curCameraId = 0;
	private int frontCameraId = 0;
	private int backCameraId = 0;
	//
	private CooeeAutoFocusObject autoFocusObject = null;//负责对自动对焦进行处理
	//
	private boolean takePhotoing = false;
	/**是否可以关闭相机,在点击照相按钮到回调onPictureTaken之间不允许关闭相机*/
	private boolean canCloseCamera = true;
	/**是否需要在拍照完成后关闭照相机*/
	private boolean needCloseAfterTakePhoto = false;
	private String photoSavaPath = null;
	//
	public static final String picturePreFix = "IMG";
	public static final String pictureSufFix = ".jpg";
	/**图片最大展示数*/
	public static final int SHOW_PICTURE_COUNT = 10;
	//
	public static final String dateFormat = "%s.%s.%s";
	public static final String timeFormat = "%s:%s";
	private static final String TAG = "CameraControl";
	
	public CameraControl(
			ImageView focusView ,
			Activity activity ,
			CameraControlCallBcak callBcak ,
			String photoSavaPath )
	{
		this.activity = activity;
		this.callBcak = callBcak;
		//
		init( activity , focusView , photoSavaPath );
	}
	
	private void init(
			Activity activity ,
			ImageView focusView ,
			String photoSavaPath )
	{
		this.photoSavaPath = photoSavaPath;
		//
		if( Build.VERSION.SDK_INT >= 16 )
			autoFocusObject = new CooeeAutoFocusObject( focusView );
		//
		// YANGTIANYU@2016/06/24 UPD START
		float width = (int)activity.getResources().getDimension( R.dimen.camera_page_camera_preview_layout_width );
		// YANGTIANYU@2016/06/27 UPD START
		// TODO 测试用,需改回来后定义一个合理的算法
		//frameHeight = (int)( width * 3.0f / 4.0f );
		frameHeight = 776;
		// YANGTIANYU@2016/06/27 UPD END
		// YANGTIANYU@2016/06/24 UPD END
		CameraView.logI( StringUtils.concat( "CameraControl , init , frameHeight:" , frameHeight ) );
		//
		cameraCount = Camera.getNumberOfCameras();
		cameraInfos = new Camera.CameraInfo[cameraCount];
		// 获取camera info 信息
		for( int i = 0 ; i < cameraInfos.length ; ++i )
		{
			cameraInfos[i] = new Camera.CameraInfo();
			Camera.getCameraInfo( i , cameraInfos[i] );
			// 设置正反照相机
			if( cameraInfos[i].facing == Camera.CameraInfo.CAMERA_FACING_FRONT )
				frontCameraId = i;
			else if( cameraInfos[i].facing == Camera.CameraInfo.CAMERA_FACING_BACK )
				backCameraId = i;
		}
		// 如果没有正面相机，就使用默认的背面或者均无
		if( frontCameraId == -1 )
			frontCameraId = backCameraId;
	}
	
	public boolean getCameraOpened()
	{
		return ( camera == null ) ? false : true;
	}
	
	public boolean takePhotoing()
	{
		return takePhotoing;
	}
	
	public void setTakePhotoing(
			boolean takePhotoing )
	{
		this.takePhotoing = takePhotoing;
	}
	
	/**
	 * 切换前后摄像头
	 */
	public void toggleCamera(
			final SurfaceTexture surfaceTexture )
	{
		CameraView.logI( "CameraControl , cyk_bug:i_0014531: toggleCamera  cameraBeingOpen : " + ( cameraBeingOpen ) );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( !cameraBeingOpen )
		{
			CameraView.logI( "CameraControl , toggleCamera can run" );
			CameraControlTaskManager.startTask( new RunningTask() {
				
				@Override
				public void task()
				{
					CameraView.logI( "CameraControl , toggleCamera" );
					//
					if( frontCameraId == curCameraId )
						openBackCamera( surfaceTexture );
					else if( backCameraId == curCameraId )
						openFrontCamera( surfaceTexture );
				}
			} );
		}
	}
	
	public void openCamera(
			final SurfaceTexture surfaceTexture )
	{
		if( !cameraBeingOpen )
		{
			CameraControlTaskManager.startTask( new RunningTask() {
				
				@Override
				public void task()
				{
					if( !getCameraOpened() )
					{
						CameraView.logI( "CameraControl , openCamera" );
						openBackCamera( surfaceTexture );
					}
				}
			} );
		}
	}
	
	public void closeCamera()
	{
		//		if( checkOperateTime() )
		//		{
		CameraControlTaskManager.stopAllTask( new RunningTask() {
			
			@Override
			public void task()
			{
				if( getCameraOpened() && !cameraBeingClose )
				{
					CameraView.logI( "CameraControl , closeCamera" );
					closeCamera( true );
				}
			}
		} );
		//		CameraControlTaskManager.startTask(  );
		//		}
	}
	
	public void toggleFlashlight()
	{
		CameraControlTaskManager.startTask( new RunningTask() {
			
			@Override
			public void task()
			{
				if( getCameraOpened() )
				{
					if( camera == null || curCameraId == -1 )
						return;
					// 仅关闭背面闪光灯
					if( curCameraId != backCameraId )
						return;
					//
					String mode = camera.getParameters().getFlashMode();
					String nextStatus = null;
					if( Parameters.FLASH_MODE_OFF.equals( mode ) )
						nextStatus = Parameters.FLASH_MODE_AUTO;
					else if( Parameters.FLASH_MODE_AUTO.equals( mode ) )
						nextStatus = Parameters.FLASH_MODE_ON;
					else if( Parameters.FLASH_MODE_ON.equals( mode ) )
						nextStatus = Parameters.FLASH_MODE_OFF;
					changeFlashlight( nextStatus );
				}
			}
		} );
	}
	
	public void takePhotos()
	{
		CameraView.logI( "CmaeraControl , cyk_bug:i_0014531: takePhotos  takePhotoing : " + ( takePhotoing ) );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( !takePhotoing )
		{
			CameraControlTaskManager.startTask( new RunningTask() {
				
				@Override
				public void task()
				{
					CameraView.logI( "CmaeraControl , cyk_bug:i_0014531: takePhotos   getCameraOpened()  : " + ( getCameraOpened() ) );//cheyingkun add	//为bug i_0014531添加log（开启配置后“switch_enable_debug”生效），以便定位。
					if( getCameraOpened() )
					{
						CameraView.logI( "CmaeraControl , takePhotos" );
						if( camera == null || curCameraId == -1 || takePhotoing )
							return;
						if( !EnvironmentUtils.isExternalStorageAvailable() )
						{
							ToastUtils.showToast( activity , "请插入sd卡" );
							return;
						}
						takePhotoing = true;
						canCloseCamera = false;
						//
						camera.takePicture( null , null , jpeg );
						// YANGTIANYU@2016/07/20 DEL START
						// 【i_0014122】
						//playSound( activity );
						// YANGTIANYU@2016/07/20 DEL END
						// YANGTIANYU@2016/06/30 ADD START
						// 友盟统计
						MobclickAgent.onEvent( activity , UmengStatistics.CAMERA_TAKE_PICTURE );
						// YANGTIANYU@2016/06/30 ADD END
					}
				}
			} );
		}
	}
	
	private void closeCamera(
			boolean callBcak )
	{
		cameraBeingClose = true;
		if( autoFocusObject != null )
			autoFocusObject.clearFocusView();
		changeFlashlight( Parameters.FLASH_MODE_OFF );
		CameraUtils.release( camera );
		camera = null;
		if( this.callBcak != null && callBcak )
			this.callBcak.closeCamera();
		cameraBeingClose = false;
	}
	
	private void openFrontCamera(
			SurfaceTexture surfaceTexture )
	{
		cameraBeingOpen = true;
		closeCamera( false );
		camera = CameraUtils.openCamera( frontCameraId , frameHeight , autoFocusObject , surfaceTexture );
		if( camera != null )
		{
			curCameraId = frontCameraId;
			camera.startPreview();
			callBcak.openCamera( true , false );
			// 关闭闪光灯
			callBcak.changeFlashlight( Parameters.FLASH_MODE_OFF );
		}
		else
		{
			callBcak.openCamera( false , false );
		}
		//
		CameraView.logI( "CmaeraControl , openFrontCamera , finish" );
		cameraBeingOpen = false;
	}
	
	private void openBackCamera(
			SurfaceTexture surfaceTexture )
	{
		cameraBeingOpen = true;
		closeCamera( false );
		camera = CameraUtils.openCamera( backCameraId , frameHeight , autoFocusObject , surfaceTexture );
		if( camera != null )
		{
			curCameraId = backCameraId;
			camera.startPreview();
			callBcak.openCamera( true , true );
		}
		else
		{
			callBcak.openCamera( false , false );
		}
		//
		CameraView.logI( "CmaeraControl , openBackCamera , finish" );
		cameraBeingOpen = false;
	}
	
	/**
	 * 更改闪光灯状态,一共存在三种状态,关闭、自动和开启。
	 * @param nextStatus 需要更改为的闪光灯状态,为Parameters.FLASH_MODE_OFF,
	 * Parameters.FLASH_MODE_ON,Parameters.FLASH_MODE_AUTO中的一种
	 * @see #android.hardware.Camera.Parameters
	 * @author yangtianyu 2016-6-24
	 */
	private void changeFlashlight(
			String nextStatus )
	{
		if( camera == null || curCameraId == -1 || nextStatus == null )
			return;
		// 仅关闭背面闪光灯
		if( curCameraId != backCameraId )
			return;
		CameraUtils.changeFlashlight( camera , nextStatus );
		if( this.callBcak != null )
			this.callBcak.changeFlashlight( nextStatus );
	}
	
	private PictureCallback jpeg = new PictureCallback() {
		
		@Override
		public void onPictureTaken(
				final byte[] data ,
				final Camera camera )
		{
			canCloseCamera = true;
			if( needCloseAfterTakePhoto )
			{
				closeCamera();
				needCloseAfterTakePhoto = false;
			}
			else
				CameraUtils.resetPreview( camera , autoFocusObject );
			// YANGTIANYU@2016/07/21 ADD START
			// 在确定拍摄到照片之后再播放拍照音效【i_0014122】
			playSound( activity );
			// YANGTIANYU@2016/07/21 ADD END
			//
			new Thread() {
				
				@SuppressWarnings( "deprecation" )
				@SuppressLint( "SimpleDateFormat" )
				public void run()
				{
					boolean tackPictureSuccess = false;
					PictureInfo info = null;
					if( data != null && data.length > 0 )
					{
						// no
						File dirFile = new File( photoSavaPath );
						// 如果文件不存在就创建
						if( !dirFile.exists() )
						{
							dirFile.mkdirs();
							//								MediaScannerConnection.scanFile( activity , new String[]{ photoSavaPath.substring( 0 , photoSavaPath.indexOf( "/Photo space/" ) ) } , null , null );
						}
						Date date = new Date();
						String format = new SimpleDateFormat( "yyyyMMddHHmmss" ).format( date );
						StringBuffer buffer = new StringBuffer( photoSavaPath );
						buffer.append( picturePreFix );
						buffer.append( format );
						buffer.append( pictureSufFix );
						// 保存照片
						File file = savePicture( data , buffer.toString() , curCameraId );
						//
						if( file != null )
						{
							int month = date.getMonth() + 1;
							int day = date.getDate();
							String dat = String.format( dateFormat , date.getYear() + 1900 , month < 10 ? StringUtils.concat( "0" , month ) : month , day < 10 ? StringUtils.concat( "0" , day ) : day );
							int hours = date.getHours();
							int minutes = date.getMinutes();
							String time = String.format( timeFormat , hours < 10 ? StringUtils.concat( "0" , hours ) : hours , minutes < 10 ? StringUtils.concat( "0" , minutes ) : minutes );
							info = new PictureInfo( dat , time , date.getDay() , file.getAbsolutePath() , date.getTime() );
							//
							notifySystemScanPic( activity , file );
							//
							info.setDrawable( BitmapUtils.getBitmapDrawableByPath( activity , info.getPicturePath() , 2 , false ) );
							tackPictureSuccess = true;
						}
					}
					if( tackPictureSuccess )
						callBcak.tackPictureSuccess( info );
					else
						callBcak.tackPictureFail();
					// YANGTIANYU@2016/07/12 DEL START
					// 更在拍照的状态由callBack中tackPictureSuccess和tackPictureFail设置
					// 因为拍照成功后需要执行一个动画,动画完成后才能允许再次拍照
					//takePhotoing = false;
					// YANGTIANYU@2016/07/12 DEL END
				};
			}.start();
		}
	};
	
	/**
	 * 保存照片,会在源照片基础上截取中间一块合适的区域作为最终照片内容,并会加入背景和拍摄时间
	 * @param data 照片像素数组
	 * @param path 照片保存路径
	 * @param curCameraId 当前相机Id,用于区分前置摄像头和后置摄像头
	 * @return 保存的照片文件,失败时返回null
	 * @author yangtianyu 2016-6-27
	 */
	private File savePicture(
			byte[] data ,
			final String path ,
			int curCameraId )
	{
		Bitmap bitmap = BitmapFactory.decodeByteArray( data , 0 , data.length );
		// 先根据预览的宽高进行缩放,不改变图片的比例,当前图片仍然是90度旋转后的情况,所以宽高数值相反设置
		int width = (int)activity.getResources().getDimension( R.dimen.camera_page_camera_preview_layout_width );
		int height = (int)activity.getResources().getDimension( R.dimen.camera_page_camera_preview_layout_height );
		bitmap = BitmapUtils.resizeBitmap( bitmap , new int[]{ height , width } , true );
		// 做旋转操作后截取所需部分图片,之前以为前后摄像头要分别进行不同的处理,
		// 但是运行下来发现一样处理就可以了,但是为了避免某些手机需要分别处理,这里还是先分开了
		if( curCameraId == backCameraId )
			bitmap = BitmapUtils.rotateBitmap( bitmap , 90f , false , true );
		else if( curCameraId == frontCameraId )
			bitmap = BitmapUtils.rotateBitmap( bitmap , -90f , false , true );
		bitmap = BitmapUtils.cropCenter( bitmap , new int[]{ width , height } , true );
		// 此时已经是正常的图片的,后续的保存逻辑统一写在BitmapUtils中
		return BitmapUtils.savePhoto( activity , bitmap , null , path , true );
	}
	
	public void notifySystemScanPic(
			Activity activity ,
			File file )
	{
		try
		{
			Uri localUri = Uri.fromFile( file );
			Intent localIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE , localUri );
			activity.sendBroadcast( localIntent );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		MediaScannerConnection.scanFile( activity , new String[]{ file.getParent() } , null , null );
	}
	
	private void playSound(
			Context context )
	{
		if( mp == null )
		{
			mp = MediaPlayer.create( context , R.raw.camera_page_takepicture );
		}
		if( !mp.isPlaying() )
		{
			try
			{
				// mp.prepare();
				mp.setLooping( false );
				mp.setOnCompletionListener( new OnCompletionListener() {
					
					@Override
					public void onCompletion(
							MediaPlayer mp )
					{
						mp.stop();
						mp.release();
						CameraControl.this.mp = null;
					}
				} );
				AudioManager mgr = (AudioManager)context.getSystemService( Context.AUDIO_SERVICE );
				float streamVolumeCurrent = mgr.getStreamVolume( AudioManager.STREAM_MUSIC );
				float streamVolumeMax = mgr.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
				float volume = streamVolumeCurrent / streamVolumeMax;// 得到音量的大小
				mp.setVolume( volume , volume );
				int RingerMode = mgr.getRingerMode();
				if( RingerMode == AudioManager.RINGER_MODE_NORMAL )
				{
					mp.start();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
	public void focusAtTouch(
			float x ,
			float y )
	{
		int areaSize = 100;
		int left = clamp( (int)( x - ( areaSize / 2 ) ) , -1000 , 1000 );
		int top = clamp( (int)( y - ( areaSize / 2 ) ) , -1000 , 1000 );
		int right = clamp( left + areaSize , -1000 , 1000 );
		int bottom = clamp( top + areaSize , -1000 , 1000 );
		Rect focusArea = new Rect( left , top , right , bottom );
		CameraUtils.focusAtTouch( camera , focusArea );
		// YANGTIANYU@2016/07/18 DEL START
		// 不在这儿调用了
		//setFocusFramePosition( x , y );
		// YANGTIANYU@2016/07/18 DEL END
	}
	
	/**
	 * 在指定位置显示对焦框
	 * @param x
	 * @param y
	 */
	public void setFocusFramePosition(
			float x ,
			float y )
	{
		if( camera != null )
		{
			if( autoFocusObject != null )
			{
				autoFocusObject.setFocusPos( x , y );
				autoFocusObject.clearFocusView();
				try
				{
					// gaominghui@2016/12/14 ADD START
					if( Build.VERSION.SDK_INT >= 16 )
					{
						camera.setAutoFocusMoveCallback( autoFocusObject.getAutoFocusMoveCallbackCompat() );
					}
					// gaominghui@2016/12/14 ADD END
				}
				catch( RuntimeException e )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( TAG , StringUtils.concat( "mCamera.setAutoFocusMoveCallback failed RuntimeException:" + e.toString() ) );
				}
			}
		}
	}
	
	/**
	 * 将输入坐标控制在安卓相机坐标系范围内(1000>x>-1000 , 1000>y>-1000)
	 * @param x
	 * @param min 安卓相机坐标系最小值
	 * @param max 安卓相机坐标系最大值
	 * @return
	 */
	private int clamp(
			int x ,
			int min ,
			int max )
	{
		if( x > max )
		{
			return max;
		}
		if( x < min )
		{
			return min;
		}
		return x;
	}
	
	public void setNeedCloseAfterTakePhoto(
			boolean needCloseAfterTakePhoto )
	{
		this.needCloseAfterTakePhoto = needCloseAfterTakePhoto;
	}
	
	public boolean ifCanCloseCamera()
	{
		return canCloseCamera;
	}
	
	public boolean isCameraBeingClose()
	{
		return cameraBeingClose;
	}
}
