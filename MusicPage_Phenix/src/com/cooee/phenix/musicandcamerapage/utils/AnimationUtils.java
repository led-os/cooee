package com.cooee.phenix.musicandcamerapage.utils;


// MusicPage CameraPage
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicpage.R;


@SuppressLint( "InlinedApi" )
public class AnimationUtils
{
	
	public static final String TAG = "AnimationUtils";
	public static final long ALPHA_DURATION = 900;
	public static final long ROTATION_DURATION = 1000;
	//fulijuan start		//没有音乐时 滑杆滑动后立马弹回
	//public static final long STYLUS_ROTATION_DURATION = 100; //fulijuan del
	public static final long STYLUS_ROTATION_DURATION = 200;//fulijuan add 
	//fulijuan end
	private static final long CUR_ALBUM_ROTATION_DURATION = 40000;
	public static final Interpolator INTERPOLATOR = new LinearInterpolator();
	
	public static ObjectAnimator getcurAlbumLayoutRotateAnimation(
			Context context ,
			View view )
	{
		float widthAndHeight = context.getResources().getDimension( R.dimen.music_page_album_bg_image_view_width_and_height );
		view.setPivotX( widthAndHeight / 2 );
		view.setPivotY( widthAndHeight / 2 );
		ObjectAnimator animator = ObjectAnimator.ofFloat( view , "rotation" , 0f , 359F );
		animator.setRepeatCount( ValueAnimator.INFINITE );
		animator.setInterpolator( INTERPOLATOR );
		animator.setDuration( CUR_ALBUM_ROTATION_DURATION );
		return animator;
	}
	
	public static TranslateAnimation getAlbumAnimation(
			View animView ,
			AnimationListener animationListener ,
			float toYDelta )
	{
		TranslateAnimation animation = new TranslateAnimation( 0 , 0 , 0 , toYDelta );
		animation.setDuration( ROTATION_DURATION );
		animation.setInterpolator( new AccelerateDecelerateInterpolator() );
		animation.setAnimationListener( animationListener );
		animView.startAnimation( animation );
		return animation;
	}
	
	/**
	 *设置唱盘拖动的透明度动画
	 * @param absMoveY
	 * @param animView
	 * @author gaominghui 2016年6月23日
	 */
	public static ObjectAnimator setAlphaAnimation(
			View animView ,
			float startAlpha ,
			float endAlpha )
	{
		ObjectAnimator alphAnimaton = ObjectAnimator.ofFloat( animView , "alpha" , startAlpha , endAlpha );
		alphAnimaton.setInterpolator( INTERPOLATOR );
		alphAnimaton.setDuration( ALPHA_DURATION );
		alphAnimaton.addListener( new AnimatorListener() {
			
			@Override
			public void onAnimationStart(
					Animator animation )
			{
				// TODO Auto-generated method stub
				Log.i( TAG , "setAlphaAnimation onAnimationStart!" );
			}
			
			@Override
			public void onAnimationRepeat(
					Animator animation )
			{
				// TODO Auto-generated method stub
				Log.i( TAG , "setAlphaAnimation onAnimationRepeat!" );
			}
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				// TODO Auto-generated method stub
				Log.i( TAG , "setAlphaAnimation onAnimationEnd!" );
			}
			
			@Override
			public void onAnimationCancel(
					Animator animation )
			{
				// TODO Auto-generated method stub
				Log.i( TAG , "setAlphaAnimation onAnimationCancel!" );
			}
		} );
		alphAnimaton.start();
		return alphAnimaton;
	}
	
	public static void startStylusRotationAnimatior(
			View animView ,
			float startDelta ,
			float toDelta )
	{
		//ViewUtils.printStackTrace( "andy" );
		ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat( animView , "rotation" , startDelta , toDelta );
		rotateAnimator.setDuration( STYLUS_ROTATION_DURATION );
		rotateAnimator.setInterpolator( new AccelerateInterpolator() );
		rotateAnimator.start();
	}
	
	// gaominghui@2016/07/11 ADD END
	public static void albumAnimationFinish(
			View topAlbumImageView ,
			View bottomAlbumImageView ,
			ViewGroup bottomParent )
	{
		Log.i( "AnimationUtils" , StringUtils.concat(
				"albumAnimationFinish topAlbumImageView.getTranslationY:" ,
				topAlbumImageView.getTranslationY() ,
				"-topAlbumImageView.y:" ,
				topAlbumImageView.Y ,
				"-topAlbumImageView.getY():" ,
				topAlbumImageView.getY() ) );
		ViewUtils.clearAnimation( topAlbumImageView );
		ViewUtils.clearAnimation( bottomAlbumImageView );
		ViewUtils.setTranslationY( topAlbumImageView , 0 );
		ViewUtils.setAlpha( topAlbumImageView , 1 );
		ViewUtils.setAlpha( bottomAlbumImageView , 1 );
		ViewUtils.removeView( bottomParent , bottomAlbumImageView );
	}
}
