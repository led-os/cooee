package com.cooee.phenix.camera;


// CameraPage
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.camera.utils.AnimationUtils;
import com.cooee.phenix.camera.utils.CameraUtils;


public class CooeeAutoFocusObject
{
	
	protected static final String TAG = "CooeeAutoFocusObject";
	private ImageView focusView = null;
	
	public CooeeAutoFocusObject(
			ImageView focusView )
	{
		this.focusView = focusView;
	}
	
	public void setFocusPos(
			float touchX ,
			float touchY )
	{
		focusView.setX( touchX - focusView.getWidth() / 2 );
		focusView.setY( touchY - focusView.getHeight() / 2 );
	}
	
	public void clearFocusView()
	{
		focusView.post( new Runnable() {
			
			@Override
			public void run()
			{
				focusView.clearAnimation();
				focusView.setImageResource( R.drawable.camera_page_ic_focus_focusing );
				focusView.setAlpha( 0 );
				focusView.setVisibility( View.GONE );
			}
		} );
	}
	
	// gaominghui@2016/12/14 ADD START
	public AutoFocusMoveCallback getAutoFocusMoveCallbackCompat()
	{
		return getInstance();
	}
	
	private AutoFocusMoveCallbackCompat instance = null;
	
	private AutoFocusMoveCallbackCompat getInstance()
	{
		if( instance == null )
		{
			instance = new AutoFocusMoveCallbackCompat();
		}
		return instance;
	}
	
	public class AutoFocusMoveCallbackCompat implements AutoFocusMoveCallback
	{
		
		/**
		 *
		 * @see android.hardware.Camera.AutoFocusMoveCallback#onAutoFocusMoving(boolean, android.hardware.Camera)
		 * @auther gaominghui  2016年12月14日
		 */
		@Override
		public void onAutoFocusMoving(
				boolean start ,
				Camera camera )
		{
			if( start )
			{
				CameraView.logI( "onAutoFocusMoving , start" );
				//
				focusView.clearAnimation();
				focusView.setVisibility( View.VISIBLE );
				focusView.setImageResource( R.drawable.camera_page_ic_focus_focusing );
				focusView.setAlpha( 255 );
				// YANGTIANYU@2016/07/14 UPD START
				//Animation anim = AnimationUtils.loadAnimation( focusView.getContext() , R.anim.scale_indicator );
				Animation anim = AnimationUtils.getFocusAnimation( focusView );
				// YANGTIANYU@2016/07/14 UPD END
				anim.setAnimationListener( new AnimationListener() {
					
					@Override
					public void onAnimationStart(
							Animation animation )
					{
					}
					
					@Override
					public void onAnimationRepeat(
							Animation animation )
					{
					}
					
					@Override
					public void onAnimationEnd(
							Animation animation )
					{
						focusView.setImageResource( R.drawable.camera_page_ic_focus_focused );
						focusView.postDelayed( new Runnable() {
							
							@Override
							public void run()
							{
								clearFocusView();
							}
						} , 300 );
					}
				} );
				focusView.startAnimation( anim );
			}
			else
			{
				if( camera != null )
				{
					try
					{
						Camera.Parameters params = camera.getParameters();
						params.setFlashMode( CameraUtils.getCurFlashMode() );
						params.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO );
						camera.setParameters( params );
						camera.cancelAutoFocus();
					}
					catch( RuntimeException e )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.v( TAG , StringUtils.concat( "cancelAutoFocus Failed RuntimeException:" , e.toString() ) );
					}
				}
			}
		}
	}

	// gaominghui@2016/12/14 ADD END
}
