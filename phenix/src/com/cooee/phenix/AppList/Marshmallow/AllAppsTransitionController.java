package com.cooee.phenix.AppList.Marshmallow;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.cooee.phenix.Hotseat;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAnimUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.Workspace;
import com.cooee.phenix.AppList.KitKat.AppsCustomizePagedView;
import com.cooee.phenix.AppList.KitKat.AppsView;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.pageIndicators.PageIndicatorCaret;
import com.cooee.util.TouchController;


/**
 * Handles AllApps view transition.
 * 1) Slides all apps view using direct manipulation
 * 2) When finger is released, animate to either top or bottom accordingly.
 * <p/>
 * Algorithm:
 * If release velocity > THRES1, snap according to the direction of movement.
 * If release velocity < THRES1, snap according to either top or bottom depending on whether it's
 * closer to top or closer to the page indicator.
 */
public class AllAppsTransitionController implements TouchController, VerticalPullDetector.Listener,
		//
		View.OnLayoutChangeListener
{

	
	private static final String TAG = "AllAppsTrans";
	private static final boolean DBG = false;
	private final Interpolator mAccelInterpolator = new AccelerateInterpolator( 2f );
	private final Interpolator mDecelInterpolator = new DecelerateInterpolator( 3f );
	private final Interpolator mFastOutSlowInInterpolator = new FastOutSlowInInterpolator();
	private final ScrollInterpolator mScrollInterpolator = new ScrollInterpolator();
	private static final float ANIMATION_DURATION = 1200;
	private static final float PARALLAX_COEFFICIENT = .125f;
	private static final float FAST_FLING_PX_MS = 10;
	private static final int SINGLE_FRAME_MS = 16;
	private AppsView mAppsView;//7.0进入主菜单动画改成也支持4.4主菜单样式
	private int mAllAppsBackgroundColor;
	private Workspace mWorkspace;
	private Hotseat mHotseat;
	private int mHotseatBackgroundColor;
    private AllAppsCaretController mCaretController;
	private float mStatusBarHeight;
	private final Launcher mLauncher;
    private final VerticalPullDetector mDetector;
	private final ArgbEvaluator mEvaluator;
	// Animation in this class is controlled by a single variable {@link mProgress}.
	// Visually, it represents top y coordinate of the all apps container if multiplied with
	// {@link mShiftRange}.
	// When {@link mProgress} is 0, all apps container is pulled up.
	// When {@link mProgress} is 1, all apps container is pulled down.
	private float mShiftStart; // [0, mShiftRange]
	private float mShiftRange; // changes depending on the orientation
	private float mProgress; // [0, 1], mShiftRange * mProgress = shiftCurrent
	// Velocity of the container. Unit is in px/ms.
	private float mContainerVelocity;
	private static final float DEFAULT_SHIFT_RANGE = 10;
	private static final float RECATCH_REJECTION_FRACTION = .0875f;
	private int mBezelSwipeUpHeight;
	private long mAnimationDuration;
	private AnimatorSet mCurrentAnimation;
	private boolean mNoIntercept;
	// Used in discovery bounce animation to provide the transition without workspace changing.
	private boolean mIsTranslateWithoutWorkspace = false;
	private AnimatorSet mDiscoBounceAnimation;
	private static float SLIDING_COEFFICIENT = .25f;//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
	public AllAppsTransitionController(
			Launcher l )
	{
		mLauncher = l;
        mDetector = new VerticalPullDetector(l);
        mDetector.setListener(this);
		mShiftRange = DEFAULT_SHIFT_RANGE;
		mProgress = 1f;
		mBezelSwipeUpHeight = l.getResources().getDimensionPixelSize( R.dimen.all_apps_bezel_swipe_height );
		mEvaluator = new ArgbEvaluator();
		mAllAppsBackgroundColor = ContextCompat.getColor( l , R.color.all_apps_container_color );
	}
	
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mNoIntercept = false;
			if( !mLauncher.isAllAppsContainerViewVisible() && mLauncher.getWorkspace().workspaceInModalState() )
			{
                mNoIntercept = true;
			}
			else if( mLauncher.isAllAppsContainerViewVisible() &&
                    !mAppsView.shouldContainerScroll(ev)) {
                mNoIntercept = true;
			}
			else if( !mLauncher.isAllAppsContainerViewVisible() && !shouldPossiblyIntercept( ev ) )
			{
                mNoIntercept = true;
            } else {
                // Now figure out which direction scroll events the controller will start
                // calling the callbacks.
                int directionsToDetectScroll = 0;
                boolean ignoreSlopWhenSettling = false;

                if (mDetector.isIdleState()) {
                    if (mLauncher.isAllAppsVisible()) {
                        directionsToDetectScroll |= VerticalPullDetector.DIRECTION_DOWN;
                    } else {
                        directionsToDetectScroll |= VerticalPullDetector.DIRECTION_UP;
                    }
                } else {
                    if (isInDisallowRecatchBottomZone()) {
                        directionsToDetectScroll |= VerticalPullDetector.DIRECTION_UP;
                    } else if (isInDisallowRecatchTopZone()) {
                        directionsToDetectScroll |= VerticalPullDetector.DIRECTION_DOWN;
                    } else {
                        directionsToDetectScroll |= VerticalPullDetector.DIRECTION_BOTH;
                        ignoreSlopWhenSettling = true;
                    }
                }
                mDetector.setDetectableScrollConditions(directionsToDetectScroll,
                        ignoreSlopWhenSettling);
            }
        }
        if (mNoIntercept) {
            return false;
        }
        mDetector.onTouchEvent(ev);
        if (mDetector.isSettlingState() && (isInDisallowRecatchBottomZone() || isInDisallowRecatchTopZone())) {
            return false;
        }
        return mDetector.isDraggingOrSettling();
    }
    private boolean shouldPossiblyIntercept(MotionEvent ev) {
		//zhujieping add start //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		if( mLauncher.isOverviewAnimOrStateAnim() )
		{
			return false;
		}
		if( LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE == LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_S8 )
		{
			if( mLauncher.getDragLayer().isEventOverHotseat( ev ) || mLauncher.getDragLayer().isEventOverPageIndicator( ev ) )
			{
				return true;
			}
			if( mWorkspace.isFunctionPageByPageIndex( mWorkspace.getCurrentPage() ) )
			{
				return false;
			}
			return true;
		}
		//zhujieping add end
		if( mDetector.isIdleState() )
		{
			if( mLauncher.getDragLayer().isEventOverHotseat( ev ) || mLauncher.getDragLayer().isEventOverPageIndicator( ev ) )
			{
				return true;
			}
			return false;
		}
		else
		{
			return true;
		}
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDetector.onTouchEvent(ev);
    }

    private boolean isInDisallowRecatchTopZone() {
        return mProgress < RECATCH_REJECTION_FRACTION;
    }

    private boolean isInDisallowRecatchBottomZone() {
        return mProgress > 1 - RECATCH_REJECTION_FRACTION;
    }

    @Override
    public void onDragStart(boolean start) {
		if( mCaretController != null )
			mCaretController.onDragStart();
        cancelAnimation();
        mCurrentAnimation = LauncherAnimUtils.createAnimatorSet();
		preparePull( start );
        mShiftStart = mAppsView.getTranslationY();
    }

    @Override
    public boolean onDrag(float displacement, float velocity) {
        if (mAppsView == null) {
            return false;   // early termination.
        }
        mContainerVelocity = velocity;
		//zhujieping add start //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		float shift;
		if( LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE == LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_S8 )
		{
			shift = Math.min( Math.max( 0 , mShiftStart + displacement * SLIDING_COEFFICIENT ) , mShiftRange );
		}
		else
		//zhujieping add end
		{
			shift = Math.min( Math.max( 0 , mShiftStart + displacement ) , mShiftRange );
		}
		setProgress( shift / mShiftRange );
		return true;
    }

    @Override
    public void onDragEnd(float velocity, boolean fling) {
        if (mAppsView == null) {
            return; // early termination.
        }

        if (fling) {
            if (velocity < 0) {
                calculateDuration(velocity, mAppsView.getTranslationY());

				//                if (!mLauncher.isAllAppsVisible()) {
				//                    mLauncher.getUserEventDispatcher().logActionOnContainer(
				//                            LauncherLogProto.Action.FLING,
				//                            LauncherLogProto.Action.UP,
				//                            LauncherLogProto.HOTSEAT);
				//                }
                mLauncher.showAppsView(true /* animated */,
						true , /* resetListToTop */
                        false /* updatePredictedApps */,
                        false /* focusSearchBar */,
						AppsCustomizePagedView.ContentType.Applications );
            } else {
                calculateDuration(velocity, Math.abs(mShiftRange - mAppsView.getTranslationY()));
                mLauncher.showWorkspace(true);
            }
            // snap to top or bottom using the release velocity
        } else {
            if (mAppsView.getTranslationY() > mShiftRange / 2) {
                calculateDuration(velocity, Math.abs(mShiftRange - mAppsView.getTranslationY()));
                mLauncher.showWorkspace(true);
            } else {
                calculateDuration(velocity, Math.abs(mAppsView.getTranslationY()));
				//                if (!mLauncher.isAllAppsVisible()) {
				//                    mLauncher.getUserEventDispatcher().logActionOnContainer(
				//                            LauncherLogProto.Action.SWIPE,
				//                            LauncherLogProto.Action.UP,
				//                            LauncherLogProto.HOTSEAT);
				//                }
                mLauncher.showAppsView(true, /* animated */
						true /* resetListToTop */ ,
                        false /* updatePredictedApps */,
						false /* focusSearchBar */ ,
						AppsCustomizePagedView.ContentType.Applications );
            }
        }
    }

    public boolean isTransitioning() {
        return mDetector.isDraggingOrSettling();
    }

    /**
     * @param start {@code true} if start of new drag.
     */
    public void preparePull(boolean start) {
		if( start )
		{
			// Initialize values that should not change until #onDragEnd
			mStatusBarHeight = mLauncher.getDragLayer().getInsets().top;
			mWorkspace.setVisibility( View.VISIBLE );
			mWorkspace.stopCurrentPageAnimation();
			mHotseat.setVisibility( View.VISIBLE );
			boolean isToWorkspace = mProgress == 0 ? true : false;//zhujieping add //7.0进入主菜单动画改成也支持4.4主菜单样式
			mAppsView.onLauncherTransitionPrepare( mLauncher , false , isToWorkspace );//跟手前，设置view可见
			mHotseatBackgroundColor = mHotseat.getBackgroundDrawableColor();
			//			mHotseat.setBackgroundTransparent( true  );
			if( !mLauncher.isAllAppsVisible() )
			{
				mAppsView.setVisibility( View.VISIBLE );
				mAppsView.getContentView().setVisibility( View.VISIBLE );
				//				mAppsView.setRevealDrawableColor( mHotseatBackgroundColor );
				//zhujieping add start//7.0进入主菜单动画改成也支持4.4主菜单样式
				mAppsView.preparePull();
				float shiftCurrent = mProgress * mShiftRange;
				float alpha = 1 - Utilities.boundToRange( mProgress , 0f , 1f );
				mAppsView.setAlpha( alpha );
				mAppsView.setTranslationY( shiftCurrent );
				//zhujieping add end
			}
        }
    }


	/**
	 * @param progress       value between 0 and 1, 0 shows all apps and 1 shows workspace
	 */
	public void setProgress(
			float progress )
	{
		float shiftPrevious = mProgress * mShiftRange;
		mProgress = progress;
		float shiftCurrent = progress * mShiftRange;
		float workspaceHotseatAlpha = Utilities.boundToRange( progress , 0f , 1f );
		float alpha = 1 - workspaceHotseatAlpha;
		float interpolation = mAccelInterpolator.getInterpolation( workspaceHotseatAlpha );
		int color = (Integer)mEvaluator.evaluate( mDecelInterpolator.getInterpolation( alpha ) , mHotseatBackgroundColor , mAllAppsBackgroundColor );
		int bgAlpha = Color.alpha( (Integer)mEvaluator.evaluate( alpha , mHotseatBackgroundColor , mAllAppsBackgroundColor ) );
		//		mAppsView.setRevealDrawableColor( ColorUtils.setAlphaComponent( color , bgAlpha ) );
		//zhujieping add start //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		if( LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE == LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_S8 )
		{
			mAppsView.setAlpha( alpha );
			mAppsView.setTranslationY( shiftCurrent * PARALLAX_COEFFICIENT );
			mWorkspace.setHotseatTranslationAndAlpha( Workspace.Direction.Y , PARALLAX_COEFFICIENT * ( -mShiftRange + shiftCurrent ) , interpolation );
			if( mIsTranslateWithoutWorkspace )
			{
				return;
			}
			mWorkspace.setWorkspaceYTranslationAndAlpha( PARALLAX_COEFFICIENT * ( -mShiftRange + shiftCurrent ) , interpolation );
		}
		else
		//zhujieping add end
		{
			mAppsView.setAlpha( alpha );
			mAppsView.setTranslationY( shiftCurrent );
			mWorkspace.setHotseatTranslationAndAlpha( Workspace.Direction.Y , -mShiftRange + shiftCurrent , interpolation );
			if( mIsTranslateWithoutWorkspace )
			{
				return;
			}
			mWorkspace.setWorkspaceYTranslationAndAlpha( PARALLAX_COEFFICIENT * ( -mShiftRange + shiftCurrent ) , interpolation );
			if( !mDetector.isDraggingState() )
			{
				mContainerVelocity = mDetector.computeVelocity( shiftCurrent - shiftPrevious , System.currentTimeMillis() );
			}
			if( mCaretController != null )
				mCaretController.updateCaret( progress , mContainerVelocity , mDetector.isDraggingState() );
		}
    }
	
	public float getProgress()
	{
		return mProgress;
	}
	
	private void calculateDuration(
			float velocity ,
			float disp )
	{
		// TODO: make these values constants after tuning.
		float velocityDivisor = Math.max( 2f , Math.abs( 0.5f * velocity ) );
		float travelDistance = Math.max( 0.2f , disp / mShiftRange );
		mAnimationDuration = (long)Math.max( 100 , ANIMATION_DURATION / velocityDivisor * travelDistance );
		if( DBG )
		{
			Log.d( TAG , String.format( "calculateDuration=%d, v=%f, d=%f" , mAnimationDuration , velocity , disp ) );
		}
	}
	
	public boolean animateToAllApps(
			AnimatorSet animationOut ,
			long duration )
	{
		boolean shouldPost = true;
		if( animationOut == null )
		{
			return shouldPost;
		}
		Interpolator interpolator;
        if (mDetector.isIdleState()) {
            preparePull(true);
            mAnimationDuration = duration;
            mShiftStart = mAppsView.getTranslationY();
            interpolator = mFastOutSlowInInterpolator;
        } else {
            mScrollInterpolator.setVelocityAtZero(Math.abs(mContainerVelocity));
            interpolator = mScrollInterpolator;
            float nextFrameProgress = mProgress + mContainerVelocity * SINGLE_FRAME_MS / mShiftRange;
            if (nextFrameProgress >= 0f) {
                mProgress = nextFrameProgress;
            }
            shouldPost = false;
        }

		ObjectAnimator driftAndAlpha = ObjectAnimator.ofFloat( this , "progress" , mProgress , 0f );
		driftAndAlpha.setDuration( mAnimationDuration );
		driftAndAlpha.setInterpolator( interpolator );
		animationOut.play( driftAndAlpha );
		animationOut.addListener( new AnimatorListenerAdapter() {
			
			
			boolean canceled = false;
			
			@Override
			public void onAnimationCancel(
					Animator animation )
			{
				canceled = true;
			}
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				if( canceled )
				{
					return;
				}
				else
				{
					finishPullUp();
					cleanUpAnimation();
                    mDetector.finishedScrolling();
				}
			}
		} );
		mCurrentAnimation = animationOut;
		return shouldPost;
	}
	
	public void showDiscoveryBounce()
	{
		// cancel existing animation in case user locked and unlocked at a super human speed.
		cancelDiscoveryAnimation();
		// assumption is that this variable is always null
		mDiscoBounceAnimation = (AnimatorSet)AnimatorInflater.loadAnimator( mLauncher , R.anim.discovery_bounce );
		mDiscoBounceAnimation.addListener( new AnimatorListenerAdapter() {
			
			
			@Override
			public void onAnimationStart(
					Animator animator )
			{
				mIsTranslateWithoutWorkspace = true;
				preparePull( true );
			}
			
			@Override
			public void onAnimationEnd(
					Animator animator )
			{
				finishPullDown();
				mDiscoBounceAnimation = null;
				mIsTranslateWithoutWorkspace = false;
			}
		} );
		mDiscoBounceAnimation.setTarget( this );
		mAppsView.post( new Runnable() {
			
			
			@Override
			public void run()
			{
				if( mDiscoBounceAnimation == null )
				{
					return;
				}
				mDiscoBounceAnimation.start();
			}
		} );
	}
	
	public boolean animateToWorkspace(
			AnimatorSet animationOut ,
			long duration )
	{
		boolean shouldPost = true;
		if( animationOut == null )
		{
			return shouldPost;
		}
		Interpolator interpolator;
        if (mDetector.isIdleState()) {
            preparePull(true);
            mAnimationDuration = duration;
            mShiftStart = mAppsView.getTranslationY();
            interpolator = mFastOutSlowInInterpolator;
        } else {
            mScrollInterpolator.setVelocityAtZero(Math.abs(mContainerVelocity));
            interpolator = mScrollInterpolator;
            float nextFrameProgress = mProgress + mContainerVelocity * SINGLE_FRAME_MS / mShiftRange;
            if (nextFrameProgress <= 1f) {
                mProgress = nextFrameProgress;
            }
            shouldPost = false;
        }

		ObjectAnimator driftAndAlpha = ObjectAnimator.ofFloat( this , "progress" , mProgress , 1f );
		driftAndAlpha.setDuration( mAnimationDuration );
		driftAndAlpha.setInterpolator( interpolator );
		animationOut.play( driftAndAlpha );
		animationOut.addListener( new AnimatorListenerAdapter() {
			
			
			boolean canceled = false;
			
			@Override
			public void onAnimationCancel(
					Animator animation )
			{
				canceled = true;
			}
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				if( canceled )
				{
					return;
				}
				else
				{
					finishPullDown();
					cleanUpAnimation();
					mDetector.finishedScrolling();
				}
			}
		} );
		mCurrentAnimation = animationOut;
		return shouldPost;
	}
	
	public void finishPullUp()
	{
		mHotseat.setVisibility( View.INVISIBLE );
		setProgress( 0f );
	}
	
	public void finishPullDown()
	{
		mAppsView.setVisibility( View.INVISIBLE );
		//		mHotseat.setBackgroundTransparent( false /* transparent */ );
		mHotseat.setVisibility( View.VISIBLE );
		if( !mLauncher.isAllAppsVisible() )
			mWorkspace.startCurrentPageAnimation();
		mAppsView.reset();
		setProgress( 1f );
	}
	
	private void cancelAnimation()
	{
		if( mCurrentAnimation != null )
		{
			mCurrentAnimation.cancel();
			mCurrentAnimation = null;
		}
		cancelDiscoveryAnimation();
	}
	
	public void cancelDiscoveryAnimation()
	{
		if( mDiscoBounceAnimation == null )
		{
			return;
		}
		mDiscoBounceAnimation.cancel();
		mDiscoBounceAnimation = null;
	}
	
	private void cleanUpAnimation()
	{
		mCurrentAnimation = null;
	}
	
	public void setupViews(
			AppsView appsView ,
			Hotseat hotseat ,
			Workspace workspace )
	{
		mAppsView = appsView;
		mHotseat = hotseat;
		mWorkspace = workspace;
		mHotseat.addOnLayoutChangeListener( this );
		mHotseat.bringToFront();
	}
	
	@Override
	public void onLayoutChange(
			View v ,
			int left ,
			int top ,
			int right ,
			int bottom ,
			int oldLeft ,
			int oldTop ,
			int oldRight ,
			int oldBottom )
	{
		if( LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE == LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_NOUGAT && mCaretController == null )//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		{
			CaretDrawable caretDrawable;
			if( mWorkspace.getPageIndicator() != null && mWorkspace.getPageIndicator() instanceof PageIndicatorCaret )
			{
				caretDrawable = ( (PageIndicatorCaret)mWorkspace.getPageIndicator() ).getCaretDrawable();
			}
			else
			{
				int caretSize = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_caret_size );
				caretDrawable = new CaretDrawable( v.getContext() );
				caretDrawable.setBounds( 0 , 0 , caretSize , caretSize );
			}
			mCaretController = new AllAppsCaretController( caretDrawable , mLauncher );
		}
		//zhujieping add start //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
		if( LauncherDefaultConfig.CONFIG_APPLIST_IN_AND_OUT_ANIM_STYLE == LauncherDefaultConfig.APPLIST_IN_AND_OUT_ANIM_STYLE_S8 )
		{
			if( mShiftRange != top * PARALLAX_COEFFICIENT )
			{
				mShiftRange = top * PARALLAX_COEFFICIENT;
				setProgress( mProgress );
			}
		}
		else
		//zhujieping add end
		{
			if( mShiftRange != top )
			{
				mShiftRange = top;
				setProgress( mProgress );
			}
		}
	}
	
	static class ScrollInterpolator implements Interpolator
	{
		
		
		boolean mSteeper;
		
		public void setVelocityAtZero(
				float velocity )
		{
			mSteeper = velocity > FAST_FLING_PX_MS;
		}
		
		public float getInterpolation(
				float t )
		{
			t -= 1.0f;
			float output = t * t * t;
			if( mSteeper )
			{
				output *= t * t; // Make interpolation initial slope steeper
			}
			return output + 1;
		}
	}
	
	@Override
	public boolean isCanDrag()
	{
		// TODO Auto-generated method stub
		if(
		//
		mWorkspace.isScrollPage()
		//
				|| ( mWorkspace.isFavoritesPageByPageIndex( mWorkspace.getCurrentPage() ) /* //cheyingkun add	//解决“双层模式、删除默认配置的时钟插件、打开循环切页、关掉酷生活、打开音乐页后，循环切页后主菜单点击不进去”的问题【c_0004425】 */ )
				//
				|| mWorkspace.getState() == Workspace.State.OVERVIEW
		//
		)
		{
			return false;
		}
		return true;
	}
	
}
