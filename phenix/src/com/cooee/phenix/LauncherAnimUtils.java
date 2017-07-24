package com.cooee.phenix;


import java.util.HashSet;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class LauncherAnimUtils
{
	
	static HashSet<Animator> sAnimators = new HashSet<Animator>();
	static Animator.AnimatorListener sEndAnimListener = new Animator.AnimatorListener() {
		
		public void onAnimationStart(
				Animator animation )
		{
			sAnimators.add( animation );
		}
		
		public void onAnimationRepeat(
				Animator animation )
		{
		}
		
		public void onAnimationEnd(
				Animator animation )
		{
			sAnimators.remove( animation );
		}
		
		public void onAnimationCancel(
				Animator animation )
		{
			sAnimators.remove( animation );
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "" , "zjp onAnimationCancel" + animation );
		}
	};
	
	public static void cancelOnDestroyActivity(
			Animator a )
	{
		a.addListener( sEndAnimListener );
	}
	
	// Helper method. Assumes a draw is pending, and that if the animation's duration is 0
	// it should be cancelled
	public static void startAnimationAfterNextDraw(
			final Animator animator ,
			final View view )
	{
		// gaominghui@2016/12/14 ADD START 兼容android4.0
		if( Build.VERSION.SDK_INT >= 16 )
		{
			view.getViewTreeObserver().addOnDrawListener( new ViewTreeObserver.OnDrawListener() {
				
				private boolean mStarted = false;
				
				public void onDraw()
				{
					if( mStarted )
						return;
					mStarted = true;
					// Use this as a signal that the animation was cancelled
					if( animator.getDuration() == 0 )
					{
						return;
					}
					animator.start();
					final ViewTreeObserver.OnDrawListener listener = this;
					view.post( new Runnable() {
						
						public void run()
						{
							view.getViewTreeObserver().removeOnDrawListener( listener );
						}
					} );
				}
			} );
		}
		// gaominghui@2016/12/14 ADD END 兼容android4.0
	}
	
	public static void onDestroyActivity()
	{
		HashSet<Animator> animators = new HashSet<Animator>( sAnimators );
		for( Animator a : animators )
		{
			if( a.isRunning() )
			{
				a.cancel();
			}
			else
			{
				sAnimators.remove( a );
			}
		}
	}
	
	public static AnimatorSet createAnimatorSet()
	{
		AnimatorSet anim = new AnimatorSet();
		cancelOnDestroyActivity( anim );
		return anim;
	}
	
	public static ValueAnimator ofFloat(
			View target ,
			float ... values )
	{
		ValueAnimator anim = new ValueAnimator();
		anim.setFloatValues( values );
		cancelOnDestroyActivity( anim );
		return anim;
	}
	
	public static ObjectAnimator ofFloat(
			View target ,
			String propertyName ,
			float ... values )
	{
		ObjectAnimator anim = new ObjectAnimator();
		anim.setTarget( target );
		anim.setPropertyName( propertyName );
		anim.setFloatValues( values );
		cancelOnDestroyActivity( anim );
		// zhangjin@2015/07/20 UPD START
		//new FirstFrameAnimatorHelper( anim , target );
		// zhangjin@2015/07/20 UPD END
		return anim;
	}
	
	public static ObjectAnimator ofPropertyValuesHolder(
			View target ,
			PropertyValuesHolder ... values )
	{
		ObjectAnimator anim = new ObjectAnimator();
		anim.setTarget( target );
		anim.setValues( values );
		cancelOnDestroyActivity( anim );
		// zhangjin@2015/07/20 UPD START
		//new FirstFrameAnimatorHelper( anim , target );		
		// zhangjin@2015/07/20 UPD END
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG && target instanceof BubbleTextView )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "i_0011202" , StringUtils.concat( "zjp ofPropertyValuesHolder target = " , ( (BubbleTextView)target ).getText() ) );
		}
		return anim;
	}
	
	public static ObjectAnimator ofPropertyValuesHolder(
			Object target ,
			View view ,
			PropertyValuesHolder ... values )
	{
		ObjectAnimator anim = new ObjectAnimator();
		anim.setTarget( target );
		anim.setValues( values );
		cancelOnDestroyActivity( anim );
		// zhangjin@2015/07/20 UPD START
		//new FirstFrameAnimatorHelper( anim , view );		
		// zhangjin@2015/07/20 UPD END
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG && view instanceof BubbleTextView )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "i_0011202" , StringUtils.concat( "zjp ofPropertyValuesHolder view = " , ( (BubbleTextView)view ).getText() ) );
		}
		return anim;
	}
}
