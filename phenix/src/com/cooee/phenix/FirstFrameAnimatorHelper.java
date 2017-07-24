package com.cooee.phenix;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


/* This is a helper class that listens to updates from the corresponding animation. For the first two frames, it adjusts the current play time of the animation to prevent jank at the beginning of the
 * animation */
public class FirstFrameAnimatorHelper extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener
{
	
	private static final boolean DEBUG = false;
	private static final int MAX_DELAY = 1000;
	private static final int IDEAL_FRAME_DURATION = 16;
	private View mTarget;
	private long mStartFrame;
	private long mStartTime = -1;
	private boolean mHandlingOnAnimationUpdate;
	private boolean mAdjustedSecondFrameTime;
	private static ViewTreeObserver.OnDrawListener sGlobalDrawListener;
	private static long sGlobalFrameCounter;
	private static boolean sVisible;
	
	public FirstFrameAnimatorHelper(
			ValueAnimator animator ,
			View target )
	{
		mTarget = target;
		animator.addUpdateListener( this );
	}
	
	public FirstFrameAnimatorHelper(
			ViewPropertyAnimator vpa ,
			View target )
	{
		mTarget = target;
		vpa.setListener( this );
	}
	
	// only used for ViewPropertyAnimators
	public void onAnimationStart(
			Animator animation )
	{
		final ValueAnimator va = (ValueAnimator)animation;
		va.addUpdateListener( FirstFrameAnimatorHelper.this );
		onAnimationUpdate( va );
	}
	
	public static void setIsVisible(
			boolean visible )
	{
		sVisible = visible;
	}
	
	public static void initializeDrawListener(
			View view )
	{
		// gaominghui@2016/12/14 ADD START 兼容android 4.0
		if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
		{
			if( sGlobalDrawListener != null )
			{
				view.getViewTreeObserver().removeOnDrawListener( sGlobalDrawListener );
			}
			sGlobalDrawListener = new ViewTreeObserver.OnDrawListener() {
				
				private long mTime = System.currentTimeMillis();
				
				public void onDraw()
				{
					sGlobalFrameCounter++;
					if( DEBUG )
					{
						long newTime = System.currentTimeMillis();
						if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.d( "FirstFrameAnimatorHelper" , StringUtils.concat( "TICK " , ( newTime - mTime ) ) );
						mTime = newTime;
					}
				}
			};
			view.getViewTreeObserver().addOnDrawListener( sGlobalDrawListener );
			sVisible = true;
		}
		else
		{
			sVisible = true;
		}
		// gaominghui@2016/12/14 ADD END 兼容android 4.0
	}
	
	public void onAnimationUpdate(
			final ValueAnimator animation )
	{
		final long currentTime = System.currentTimeMillis();
		if( mStartTime == -1 )
		{
			mStartFrame = sGlobalFrameCounter;
			mStartTime = currentTime;
		}
		if( !mHandlingOnAnimationUpdate && sVisible &&
		// If the current play time exceeds the duration, the animation
		// will get finished, even if we call setCurrentPlayTime -- therefore
		// don't adjust the animation in that case
		animation.getCurrentPlayTime() < animation.getDuration() )
		{
			mHandlingOnAnimationUpdate = true;
			long frameNum = sGlobalFrameCounter - mStartFrame;
			// If we haven't drawn our first frame, reset the time to t = 0
			// (give up after MAX_DELAY ms of waiting though - might happen, for example, if we
			// are no longer in the foreground and no frames are being rendered ever)
			if( frameNum == 0 && currentTime < mStartTime + MAX_DELAY )
			{
				// The first frame on animations doesn't always trigger an invalidate...
				// force an invalidate here to make sure the animation continues to advance
				mTarget.getRootView().invalidate();
				animation.setCurrentPlayTime( 0 );
				// For the second frame, if the first frame took more than 16ms,
				// adjust the start time and pretend it took only 16ms anyway. This
				// prevents a large jump in the animation due to an expensive first frame
			}
			else if( frameNum == 1 && currentTime < mStartTime + MAX_DELAY && !mAdjustedSecondFrameTime && currentTime > mStartTime + IDEAL_FRAME_DURATION )
			{
				animation.setCurrentPlayTime( IDEAL_FRAME_DURATION );
				mAdjustedSecondFrameTime = true;
			}
			else
			{
				if( frameNum > 1 )
				{
					mTarget.post( new Runnable() {
						
						public void run()
						{
							animation.removeUpdateListener( FirstFrameAnimatorHelper.this );
						}
					} );
				}
				if( DEBUG )
					print( animation );
			}
			mHandlingOnAnimationUpdate = false;
		}
		else
		{
			if( DEBUG )
				print( animation );
		}
	}
	
	public void print(
			ValueAnimator animation )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			float flatFraction = animation.getCurrentPlayTime() / (float)animation.getDuration();
			Log.d( "FirstFrameAnimatorHelper" , StringUtils.concat(
					"sGlobalFrameCounter:" ,
					sGlobalFrameCounter ,
					"-[sGlobalFrameCounter - mStartFrame]:" ,
					( sGlobalFrameCounter - mStartFrame ) ,
					"mTarget" + mTarget ,
					"-mTarget.isDirty():" ,
					mTarget.isDirty() ,
					"-flatFraction:" ,
					flatFraction ,
					"this:" + this ,
					"animation" + animation ) );
		}
	}
}
