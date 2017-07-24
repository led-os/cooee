package com.cooee.phenix.camera.utils;


// MusicPage CameraPage
import android.annotation.SuppressLint;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;


@SuppressLint( "InlinedApi" )
public class AnimationUtils
{
	
	private static final long TRANSLATE_DURATION = 800;
	private static final long SCALE_ALPHA_DURATION = 500;
	private static final long FOCUS_SCALE_DURATION = 300;
	private static final Interpolator INTERPOLATOR = new LinearInterpolator();
	
	/**
	 * 获取对焦动画
	 * @param animView 焦点框的view
	 * @return 对焦动画
	 * @author yangtianyu 2016-7-14
	 */
	public static Animation getFocusAnimation(
			View animView )
	{
		float pivotX = animView.getX() + animView.getWidth() / 2;
		float pivotY = animView.getY() + animView.getHeight() / 2;
		Animation animation = new ScaleAnimation( 1f , 0.7f , 1f , 0.7f , pivotX , pivotY );
		animation.setFillAfter( true );
		animation.setDuration( FOCUS_SCALE_DURATION );
		animation.setInterpolator( INTERPOLATOR );
		return animation;
	}
	
	public static AnimationSet getPhotoAnimation(
			View animView ,
			AnimationListener listener ,
			float toYDelta )
	{
		AnimationSet animationSet = new AnimationSet( false );
		animationSet.setAnimationListener( listener );
		Animation animation = null;
		//
		animation = new TranslateAnimation( 0 , 0 , 0 , toYDelta );
		animation.setDuration( TRANSLATE_DURATION );
		animation.setInterpolator( INTERPOLATOR );
		animation.setAnimationListener( listener );
		animationSet.addAnimation( animation );
		//
		// YANGTIANYU@2016/07/13 UPD START
		//animation = new ScaleAnimation( 1f , 0.6f , 1f , 0.6f , Animation.RELATIVE_TO_SELF , 1f , Animation.RELATIVE_TO_SELF , 0f );
		animation = new ScaleAnimation( 1f , 0.65f , 1f , 0.65f , animView.getPivotX() , animView.getPivotY() );
		// YANGTIANYU@2016/07/13 UPD END
		animation.setStartOffset( TRANSLATE_DURATION );
		animation.setDuration( SCALE_ALPHA_DURATION );
		animation.setInterpolator( INTERPOLATOR );
		animationSet.addAnimation( animation );
		//
		animation = new AlphaAnimation( 1f , 0f );
		animation.setStartOffset( TRANSLATE_DURATION );
		animation.setDuration( SCALE_ALPHA_DURATION );
		animation.setInterpolator( INTERPOLATOR );
		animationSet.addAnimation( animation );
		//
		animView.startAnimation( animationSet );
		return animationSet;
	}
	/*public static void albumAnimationFinish(
			View progress ,
			View topAlbumImageView ,
			View bottomAlbumImageView ,
			ViewGroup bottomParent )
	{
		if( MusicView.configUtils.getBoolean( "show_progress" , false ) )
			ViewUtils.setVisibility( progress , View.VISIBLE );
		ViewUtils.setTranslationY( topAlbumImageView , 0 );
		ViewUtils.clearAnimation( topAlbumImageView );
		ViewUtils.clearAnimation( bottomAlbumImageView );
		ViewUtils.removeView( bottomParent , bottomAlbumImageView );
	}*/
}
