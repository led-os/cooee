package com.cooee.phenix.camera.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;

import com.cooee.phenix.camera.R;
import com.cooee.phenix.camera.control.CameraControl;


public class CameraTextureView extends TextureView
{
	
	private CameraControl mCameraControl = null;
	
	public CameraTextureView(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
	}
	
	public CameraTextureView(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
	}
	
	public CameraTextureView(
			Context context )
	{
		super( context );
	}
	
	public void setCameraControl(
			CameraControl cameraControl )
	{
		this.mCameraControl = cameraControl;
	}
	
	@Override
	public boolean onTouchEvent(
			android.view.MotionEvent event )
	{
		if( event.getAction() == android.view.MotionEvent.ACTION_DOWN )
		{
			if( mCameraControl != null )
			{
				// YANGTIANYU@2016/07/18 UPD START
				// 布局为居中,所以绘制聚焦框的Y轴位置需要进行换算
				//mCameraControl.focusAtTouch( event.getX() , event.getY() );
				float focusX = event.getX();
				float focusY = event.getY();
				mCameraControl.focusAtTouch( focusX , focusY );
				float previewHeight = getContext().getResources().getDimension( R.dimen.camera_page_camera_preview_layout_height );
				mCameraControl.setFocusFramePosition( focusX , focusY - ( this.getHeight() - previewHeight ) / 2 );
				// YANGTIANYU@2016/07/18 UPD END
			}
		}
		return super.onTouchEvent( event );
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		// 根据w的大小设置 以4：3方式设置view大小
		int w = (int)( View.MeasureSpec.getSize( widthMeasureSpec ) );
		int h = (int)( 4.0f / 3.0f * w );
		setMeasuredDimension( w , h );
	}
}
