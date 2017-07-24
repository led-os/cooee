package com.cooee.phenix.camera.utils;


// MusicPage CameraPage
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.camera.CameraView;
import com.cooee.phenix.camera.CooeeAutoFocusObject;


public class CameraUtils
{
	
	private static final String TAG = "CameraUtils";
	public static int frameHeight = 0;
	public static int frameWidth = 0;
	private static String curFlashMode = Parameters.FLASH_MODE_OFF;
	
	public static Camera.Size getSuitablePictureSize(
			Camera.Parameters parameters )
	{
		return getSuitableSize( getSupportedPictureSizes( parameters ) , true );
	}
	
	public static Camera.Size getSuitablePictureSizeFromPreview(
			Camera.Parameters params )
	{
		if( params == null )
		{
			return null;
		}
		Camera.Size pictureSize = getSuitablePictureSize( params );
		return pictureSize;
	}
	
	public static Camera.Size getSuitablePreviewSize(
			Camera.Parameters paramParameters )
	{
		return getSuitableSize( getSupportedPreviewSizes( paramParameters ) , false );
	}
	
	public static Camera.Size getSuitablePreviewSizeFromPicture(
			Camera.Parameters paramParameters )
	{
		Camera.Size localSize1 = getSuitablePictureSize( paramParameters );
		@SuppressWarnings( "rawtypes" )
		Iterator localIterator = getSupportedPreviewSizes( paramParameters ).iterator();
		Camera.Size localSize2 = getSuitablePreviewSize( paramParameters );
		do
		{
			if( !localIterator.hasNext() )
				break;
			localSize2 = (Camera.Size)localIterator.next();
		}
		while( !isSameSize( localSize2 , localSize1 ) );
		return localSize2;
	}
	
	public static Camera.Size getSuitableSize(
			List<Camera.Size> paramList ,
			boolean isPicSize )
	{
		return getSuitableSize( paramList , getCameraFrameHeight() , isPicSize );
	}
	
	/**
	 * 给定高度，获得最相近的camera 尺寸
	 * @param sizeList
	 *            尺寸了列表
	 * @param height
	 *            frame高度
	 * @param isPicSize
	 * 			    是否为拍摄的图片尺寸（不是预览尺寸）
	 * @return camera大小
	 * @author yangtianyu 2016-6-27
	 */
	public static Camera.Size getSuitableSize(
			List<Camera.Size> sizeList ,
			int height ,
			boolean isPicSize )
	{
		if( sizeList == null )
		{
			return null;
		}
		if( height == 0 )
		{
		}
		int min = -1;
		Camera.Size ret = null;
		for( int i = 0 ; i < sizeList.size() ; ++i )
		{
			Camera.Size size = sizeList.get( i );
			if( 3 * size.width != 4 * size.height )
				continue;
			// YANGTIANYU@2016/06/27 ADD START
			if( isPicSize && size.height < height )
				continue;
			// YANGTIANYU@2016/06/27 ADD END
			// 大小必须小于480，以避免预览速度下降
			//			if (size.height > 480) {
			//				continue;
			//			}
			if( min == -1 )
			{
				min = Math.abs( size.height - height );
				ret = size;
			}
			int diff = Math.abs( size.height - height );
			if( diff < min )
			{
				min = diff;
				ret = size;
			}
		}
		if( ret == null && sizeList.size() > 0 )
			ret = sizeList.get( 0 );
		return ret;
	}
	
	public static List<Camera.Size> getSupportedPictureSizes(
			Camera.Parameters params )
	{
		if( params == null )
		{
			return null;
		}
		return params.getSupportedPictureSizes();
	}
	
	public static List<Camera.Size> getSupportedPreviewSizes(
			Camera.Parameters params )
	{
		if( params == null )
		{
			return null;
		}
		return params.getSupportedPreviewSizes();
	}
	
	public static boolean isSameSize(
			Camera.Size size1 ,
			Camera.Size size2 )
	{
		if( ( size1 == null ) || ( size2 == null ) )
		{
			return false;
		}
		return size1.width == size2.width && size1.height == size2.height;
	}
	
	public static int getCameraFrameHeight()
	{
		return frameHeight;
	}
	
	public static void setCameraFrameHeight(
			int height )
	{
		if( height < 0 )
		{
			height = 0;
		}
		frameHeight = height;
	}
	
	@SuppressLint( "InlinedApi" )
	public static Camera openCamera(
			int cameraId ,
			int frameHeight ,
			CooeeAutoFocusObject autoFocusObject ,
			SurfaceTexture surfaceTexture )
	{
		Camera camera = null;
		try
		{
			camera = Camera.open( cameraId );
			if( camera == null )
				throw new Exception( "camera == null" );
			Camera.Parameters params = camera.getParameters();
			// 设置预览大小
			CameraUtils.setCameraFrameHeight( frameHeight );
			Camera.Size previewSize = CameraUtils.getSuitablePreviewSize( params );
			// 设置图片大小
			Camera.Size pictureSize = CameraUtils.getSuitablePictureSizeFromPreview( params );
			CameraView.logI( StringUtils.concat( TAG , ",picture2 size w:" , previewSize.width , "-h:" , previewSize.height ) );
			CameraView.logI( StringUtils.concat( TAG , ",picture size w:" , pictureSize.width , "-h:" , pictureSize.height ) );
			// YANGTIANYU@2016/06/25 UPD START
			//params.setPreviewSize( previewSize.height , previewSize.width );//为什么要倒过来呢？
			//params.setPictureSize( pictureSize.height , pictureSize.width );//因为要旋转相机吗~
			params.setPreviewSize( previewSize.width , previewSize.height );//为什么要倒过来呢？
			params.setPictureSize( pictureSize.width , pictureSize.height );//因为要旋转相机吗~
			// YANGTIANYU@2016/06/25 UPD END
			params.setPictureFormat( ImageFormat.JPEG );
			//设置图像质量
			params.setJpegQuality( 100 );
			//对于支持连续对焦的camera，设置相关对焦功能
			java.util.List<String> focusType = params.getSupportedFocusModes();
			if( focusType != null && focusType.contains( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE ) )
			{
				//连续对焦
				//				params.setFocusMode( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE );
				//设置自动对焦相关参数
				//判断版本，只有api大于等于16才进行对焦
				if( Build.VERSION.SDK_INT >= 16 )
				{
					//					autoFocusObject.clearFocusView();
					//					camera.setAutoFocusMoveCallback( autoFocusObject.mAutoFocusMoveCallback );
					//mCamera.autoFocus( mAutoFocusCallback );
				}
			}
			camera.setParameters( params );
			setCameraDisplayOrientation( cameraId , camera );
			//
			//			holder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );//设置类型
			//			camera.setPreviewDisplay( holder );
			camera.setPreviewTexture( surfaceTexture );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			release( camera );
			camera = null;
		}
		return camera;
	}
	
	public static void setCameraDisplayOrientation(
			int cameraId ,
			android.hardware.Camera camera )
	{
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo( cameraId , info );
		//设备方向
		int rotation = 0;
		int degrees = 0;
		switch( rotation )
		{
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}
		int result;
		if( info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT )
		{
			result = ( info.orientation + degrees ) % 360;
			result = ( 360 - result ) % 360; // compensate the mirror
		}
		else
		{ // back-facing
			result = ( info.orientation - degrees + 360 ) % 360;
		}
		camera.setDisplayOrientation( result );
	}
	
	public static void release(
			Camera camera )
	{
		if( camera != null )
			try
			{
				//chenliang start	//解决“相机页打开拍照预览界面时，快捷切换前后摄像头，相机会卡死”的问题。【c_0004652】
				//				camera.setPreviewDisplay( null );	//chenliang del	
				camera.setPreviewCallback( null ); //chenliang add
				//chenliang end
				camera.stopPreview();
				camera.release();
				camera = null;
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
	}
	
	/**
	 * 更改闪光灯状态,一共存在三种状态,关闭、自动和开启。
	 * @param camera 相机对象
	 * @param nextStatus 需要更改为的闪光灯状态,为Parameters.FLASH_MODE_OFF,
	 * Parameters.FLASH_MODE_ON,Parameters.FLASH_MODE_AUTO中的一种
	 * @see #android.hardware.Camera.Parameters
	 * @author yangtianyu 2016-6-24
	 */
	public static void changeFlashlight(
			Camera camera ,
			String nextStatus )
	{
		try
		{
			Parameters param = camera.getParameters();
			if( !param.getFlashMode().equals( nextStatus ) )
			{
				param.setFlashMode( nextStatus );
				camera.setParameters( param );
				curFlashMode = nextStatus;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public static void resetPreview(
			Camera camera ,
			CooeeAutoFocusObject autoFocusObject )
	{
		try
		{
			camera.stopPreview();
			camera.startPreview();
			//重新开启自动对焦
			if( Build.VERSION.SDK_INT >= 16 )
			{
				autoFocusObject.clearFocusView();
				camera.setAutoFocusMoveCallback( autoFocusObject.getAutoFocusMoveCallbackCompat() );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 在触摸点进行对焦
	 * @author yangtianyu 2016-7-14
	 */
	public static void focusAtTouch(
			Camera camera ,
			Rect focusRect )
	{
		if( camera == null )
			return;
		try
		{
			Camera.Parameters parameters = camera.getParameters();
			//如果没有关闭闪光灯 则进行预闪光
			final String flashMode = parameters.getFlashMode();
			if( !flashMode.equals( Camera.Parameters.FLASH_MODE_OFF ) )
			{
				parameters.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );
			}
			if( parameters.getMaxNumFocusAreas() > 0 )
			{
				List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
				focusAreas.add( new Camera.Area( focusRect , 800 ) );
				parameters.setFocusAreas( focusAreas );
			}
			camera.cancelAutoFocus();
			parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE );
			camera.setParameters( parameters );
			camera.autoFocus( new AutoFocusCallback() {
				
				@Override
				public void onAutoFocus(
						boolean success ,
						Camera camera )
				{
					//对焦完成后 恢复闪光模式
					if( camera != null )
					{
						try
						{
							Camera.Parameters params = camera.getParameters();
							params.setFlashMode( flashMode );
							camera.setParameters( params );
						}
						catch( RuntimeException e )
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( TAG , StringUtils.concat( "onAutoFocus RuntimeException:" , e.toString() ) );
						}
					}
				}
			} );
			//设置对焦框位置
		}
		catch( RuntimeException e )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "onReceive ACTION_FOCUS_IN_ONE_POINT RuntimeException:" , e.toString() ) );
		}
	}
	
	public static String getCurFlashMode()
	{
		return curFlashMode;
	}
}
