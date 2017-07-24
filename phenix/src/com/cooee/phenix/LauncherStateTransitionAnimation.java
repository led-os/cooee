/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cooee.phenix;


import java.util.HashMap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.cooee.phenix.AppList.KitKat.AppsCustomizePagedView;
import com.cooee.phenix.AppList.KitKat.AppsView;
import com.cooee.phenix.AppList.Marshmallow.AllAppsTransitionController;
import com.cooee.phenix.util.Thunk;

/**
 * TODO: figure out what kind of tests we can write for this
 *
 * Things to test when changing the following class.
 *   - Home from workspace
 *          - from center screen
 *          - from other screens
 *   - Home from all apps
 *          - from center screen
 *          - from other screens
 *   - Back from all apps
 *          - from center screen
 *          - from other screens
 *   - Launch app from workspace and quit
 *          - with back
 *          - with home
 *   - Launch app from all apps and quit
 *          - with back
 *          - with home
 *   - Go to a screen that's not the default, then all
 *     apps, and launch and app, and go back
 *          - with back
 *          -with home
 *   - On workspace, long press power and go back
 *          - with back
 *          - with home
 *   - On all apps, long press power and go back
 *          - with back
 *          - with home
 *   - On workspace, power off
 *   - On all apps, power off
 *   - Launch an app and turn off the screen while in that app
 *          - Go back with home key
 *          - Go back with back key  TODO: make this not go to workspace
 *          - From all apps
 *          - From workspace
 *   - Enter and exit car mode (becuase it causes an extra configuration changed)
 *          - From all apps
 *          - From the center workspace
 *          - From another workspace
 */
public class LauncherStateTransitionAnimation {

	

    public static final int PULLUP = 1;

    /**
     * Private callbacks made during transition setup.
     */
    private static class PrivateTransitionCallbacks {
        private final float materialRevealViewFinalAlpha;

        PrivateTransitionCallbacks(float revealAlpha) {
            materialRevealViewFinalAlpha = revealAlpha;
        }

        float getMaterialRevealViewStartFinalRadius() {
            return 0;
        }
        AnimatorListenerAdapter getMaterialRevealViewAnimatorListener(View revealView,
                View buttonView) {
            return null;
        }
        void onTransitionComplete() {}
    }

    public static final String TAG = "LSTAnimation";

    // Flags to determine how to set the layers on views before the transition animation
    public static final int BUILD_LAYER = 0;
    public static final int BUILD_AND_SET_LAYER = 1;
    public static final int SINGLE_FRAME_DELAY = 16;

    @Thunk Launcher mLauncher;
    @Thunk AnimatorSet mCurrentAnimation;
    AllAppsTransitionController mAllAppsController;

    public LauncherStateTransitionAnimation(Launcher l, AllAppsTransitionController allAppsController) {
        mLauncher = l;
        mAllAppsController = allAppsController;
    }

    /**
     * Starts an animation to the apps view.
     *
     * @param startSearchAfterTransition Immediately starts app search after the transition to
     *                                   All Apps is completed.
     */
    public void startAnimationToAllApps(final Workspace.State fromWorkspaceState,
            final boolean animated, final boolean startSearchAfterTransition) {
		final AppsView toView = mLauncher.getAppsView( AppsCustomizePagedView.ContentType.Applications );
        final View buttonView = mLauncher.getStartViewForAllAppsRevealAnimation();
		if( toView == null )
		{
			return;
		}
		PrivateTransitionCallbacks cb = new PrivateTransitionCallbacks( 1f ) {

			@Override
			public float getMaterialRevealViewStartFinalRadius()
			{
				int allAppsButtonSize = mLauncher.getDeviceProfile().getIconWidthSizePx();
				return allAppsButtonSize / 2;
			}
			
			@Override
			public AnimatorListenerAdapter getMaterialRevealViewAnimatorListener(
					final View revealView ,
					final View allAppsButtonView )
			{
				return new AnimatorListenerAdapter() {
					
					
					public void onAnimationStart(
							Animator animation )
					{
						allAppsButtonView.setVisibility( View.INVISIBLE );
					}
					
					public void onAnimationEnd(
							Animator animation )
					{
						allAppsButtonView.setVisibility( View.VISIBLE );
					}
				};
			}
			
			@Override
			void onTransitionComplete()
			{
				//                mLauncher.getUserEventDispatcher().resetElapsedContainerMillis();
				if( startSearchAfterTransition )
				{
					toView.startAppsSearch();
				}
			}
		};
		int animType = PULLUP;
        // Only animate the search bar if animating from spring loaded mode back to all apps
        startAnimationToOverlay(fromWorkspaceState,
				Workspace.State.SMALL ,
				buttonView ,
				toView ,
				animated ,
				animType ,
				cb );
    }


    /**
     * Starts an animation to the workspace from the current overlay view.
     */
    public void startAnimationToWorkspace(final Launcher.State fromState,
            final Workspace.State fromWorkspaceState, final Workspace.State toWorkspaceState,
            final boolean animated, final Runnable onCompleteRunnable) {
        if (toWorkspaceState != Workspace.State.NORMAL &&
                toWorkspaceState != Workspace.State.SPRING_LOADED &&
                toWorkspaceState != Workspace.State.OVERVIEW) {
            Log.e(TAG, "Unexpected call to startAnimationToWorkspace");
        }

		if( fromState == Launcher.State.APPS_CUSTOMIZE || fromState == Launcher.State.APPS_CUSTOMIZE_SPRING_LOADED
                || mAllAppsController.isTransitioning()) {

			int animType = PULLUP;
            startAnimationToWorkspaceFromAllApps(fromWorkspaceState, toWorkspaceState,
                    animated, animType, onCompleteRunnable);
		}
		else
		{
			startAnimationToNewWorkspaceState( fromWorkspaceState , toWorkspaceState ,
                    animated, onCompleteRunnable);
        }
    }

    /**
     * Creates and starts a new animation to a particular overlay view.
     */
    @SuppressLint("NewApi")
    private void startAnimationToOverlay(
            final Workspace.State fromWorkspaceState, final Workspace.State toWorkspaceState,
			final View buttonView ,
			final AppsView toView ,
            final boolean animated, int animType, final PrivateTransitionCallbacks pCb) {
        final AnimatorSet animation = LauncherAnimUtils.createAnimatorSet();
        final Resources res = mLauncher.getResources();
		final int revealDurationSlide = res.getInteger( R.integer.config_overlaySlideRevealTime );

        final View fromView = mLauncher.getWorkspace();

		final HashMap<View , Integer> layerViews = new HashMap<View , Integer>();

        // If for some reason our views aren't initialized, don't animate
        boolean initialized = buttonView != null;

        // Cancel the current animation
        cancelAnimation();

		View contentView = toView.getContentView();
		playCommonTransitionAnimations( toWorkspaceState , fromView , toView , animated , initialized , animation , layerViews );
        if (!animated || !initialized) {
			if( toWorkspaceState == Workspace.State.SMALL )
			{
                mAllAppsController.finishPullUp();
            }
            toView.setTranslationX(0.0f);
            toView.setTranslationY(0.0f);
            toView.setScaleX(1.0f);
            toView.setScaleY(1.0f);
            toView.setAlpha(1.0f);
            toView.setVisibility(View.VISIBLE);

            // Show the content view
			if( contentView != null )
				contentView.setVisibility( View.VISIBLE );
			if( mLauncher.getSearchDropTargetBar() != null )
			{
				mLauncher.getSearchDropTargetBar().hideSearchBar( false );
			}
            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionStart(fromView, animated, false);
            dispatchOnLauncherTransitionEnd(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);
            dispatchOnLauncherTransitionStart(toView, animated, false);
            dispatchOnLauncherTransitionEnd(toView, animated, false);
            pCb.onTransitionComplete();
            return;
        }
		if( animType == PULLUP )
		{
            // We are animating the content view alpha, so ensure we have a layer for it
			contentView.setVisibility( View.VISIBLE );
			layerViews.put( contentView , BUILD_AND_SET_LAYER );
			animation.addListener( new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
					dispatchOnLauncherTransitionEnd( fromView , animated , false );
					dispatchOnLauncherTransitionEnd( toView , animated , false );
					// Disable all necessary layers
					for( View v : layerViews.keySet() )
					{
						if( v != null && layerViews.get( v ) == BUILD_AND_SET_LAYER )
						{
							v.setLayerType( View.LAYER_TYPE_NONE , null );
						}
					}
					if( mLauncher.getSearchDropTargetBar() != null )
					{
						mLauncher.getSearchDropTargetBar().hideSearchBar( false );
					}
					cleanupAnimation();
					pCb.onTransitionComplete();
				}
			} );
			boolean shouldPost = mAllAppsController.animateToAllApps( animation , revealDurationSlide );

            dispatchOnLauncherTransitionPrepare(fromView, animated, false);
            dispatchOnLauncherTransitionPrepare(toView, animated, false);

            final AnimatorSet stateAnimation = animation;
            final Runnable startAnimRunnable = new Runnable() {
                public void run() {
                    // Check that mCurrentAnimation hasn't changed while
                    // we waited for a layout/draw pass
                    if (mCurrentAnimation != stateAnimation)
                        return;

                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);

                    // Enable all necessary layers
					for( View v : layerViews.keySet() )
					{
						if( v != null && layerViews.get( v ) == BUILD_AND_SET_LAYER )
						{
							v.setLayerType( View.LAYER_TYPE_HARDWARE , null );
						}
						if( Utilities.ATLEAST_LOLLIPOP && v != null && v.isAttachedToWindow() )
						{
							v.buildLayer();
						}
					}
					toView.requestFocus();
                    stateAnimation.start();
                }
            };
            mCurrentAnimation = animation;
			if( shouldPost )
			{
				toView.post( startAnimRunnable );
			}
			else
			{
				startAnimRunnable.run();
			}
        }
    }

    /**
     * Plays animations used by various transitions.
     */
    private void playCommonTransitionAnimations(
			Workspace.State toWorkspaceState ,
			View fromView ,
			AppsView toView ,
            boolean animated, boolean initialized, AnimatorSet animation,
            HashMap<View, Integer> layerViews) {
        // Create the workspace animation.
        // NOTE: this call apparently also sets the state for the workspace if !animated
        Animator workspaceAnim = mLauncher.startWorkspaceStateChangeAnimation(toWorkspaceState,
                animated, layerViews);

		if( animated && initialized && workspaceAnim != null )
		{
            // Play the workspace animation
            if (workspaceAnim != null) {
                animation.play(workspaceAnim);
            }
            // Dispatch onLauncherTransitionStep() as the animation interpolates.
            animation.play(dispatchOnLauncherTransitionStepAnim(fromView, toView));
        }
    }

	/**
	 * Returns an Animator that calls {@link #dispatchOnLauncherTransitionStep(View, float)} on
	 * {@param fromView} and {@param toView} as the animation interpolates.
	 *
	 * This is a bit hacky: we create a dummy ValueAnimator just for the AnimatorUpdateListener.
	 */
	private Animator dispatchOnLauncherTransitionStepAnim(
			final View fromView ,
			final AppsView toView )
	{
        ValueAnimator updateAnimator = ValueAnimator.ofFloat(0, 1);
        updateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                dispatchOnLauncherTransitionStep(fromView, animation.getAnimatedFraction());
                dispatchOnLauncherTransitionStep(toView, animation.getAnimatedFraction());
            }
        });
        return updateAnimator;
    }

    /**
     * Starts an animation to the workspace from the apps view.
     */
    private void startAnimationToWorkspaceFromAllApps(final Workspace.State fromWorkspaceState,
            final Workspace.State toWorkspaceState, final boolean animated, int type,
            final Runnable onCompleteRunnable) {
		AppsView appsView = mLauncher.getAppsView( AppsCustomizePagedView.ContentType.Applications );
        // No alpha anim from all apps
		if( appsView == null )
		{
			return;
		}
		PrivateTransitionCallbacks cb = new PrivateTransitionCallbacks( 1f ) {
            @Override
            float getMaterialRevealViewStartFinalRadius() {
				int allAppsButtonSize = mLauncher.getDeviceProfile().getIconWidthSizePx();
                return allAppsButtonSize / 2;
            }
            @Override
            public AnimatorListenerAdapter getMaterialRevealViewAnimatorListener(
                    final View revealView, final View allAppsButtonView) {
                return new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animation) {
                        // We set the alpha instead of visibility to ensure that the focus does not
                        // get taken from the all apps view
                        allAppsButtonView.setVisibility(View.VISIBLE);
                        allAppsButtonView.setAlpha(0f);
                    }
                    public void onAnimationEnd(Animator animation) {
                        // Hide the reveal view
                        revealView.setVisibility(View.INVISIBLE);

                        // Show the all apps button, and focus it
                        allAppsButtonView.setAlpha(1f);
                    }
                };
            }
            @Override
            void onTransitionComplete() {
				//                mLauncher.getUserEventDispatcher().resetElapsedContainerMillis();
            }
        };
        // Only animate the search bar if animating to spring loaded mode from all apps
        startAnimationToWorkspaceFromOverlay(fromWorkspaceState, toWorkspaceState,
                mLauncher.getStartViewForAllAppsRevealAnimation(), appsView,
                animated, type, onCompleteRunnable, cb);
    }


    /**
     * Starts an animation to the workspace from another workspace state, e.g. normal to overview.
     */
    private void startAnimationToNewWorkspaceState(final Workspace.State fromWorkspaceState,
            final Workspace.State toWorkspaceState, final boolean animated,
            final Runnable onCompleteRunnable) {
        final View fromWorkspace = mLauncher.getWorkspace();
		final HashMap<View , Integer> layerViews = new HashMap<View , Integer>();
        final AnimatorSet animation = LauncherAnimUtils.createAnimatorSet();

        // Cancel the current animation
        cancelAnimation();

        playCommonTransitionAnimations(toWorkspaceState, fromWorkspace, null,
                animated, animated, animation, layerViews);

        if (animated) {
			dispatchOnLauncherTransitionPrepare( fromWorkspace , animated , true );

            final AnimatorSet stateAnimation = animation;
            final Runnable startAnimRunnable = new Runnable() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public void run() {
                    // Check that mCurrentAnimation hasn't changed while
                    // we waited for a layout/draw pass
                    if (mCurrentAnimation != stateAnimation)
                        return;

                    dispatchOnLauncherTransitionStart(fromWorkspace, animated, true);

                    // Enable all necessary layers
                    for (View v : layerViews.keySet()) {
						if( v != null && layerViews.get( v ) == BUILD_AND_SET_LAYER )
						{
                            v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                        }
						if( Utilities.ATLEAST_LOLLIPOP && v != null && v.isAttachedToWindow() )
						{
                            v.buildLayer();
                        }
                    }
                    stateAnimation.start();
                }
            };
            animation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    dispatchOnLauncherTransitionEnd(fromWorkspace, animated, true);

                    // Run any queued runnables
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }

                    // Disable all necessary layers
                    for (View v : layerViews.keySet()) {
						if( v != null && layerViews.get( v ) == BUILD_AND_SET_LAYER )
						{
                            v.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                    }

                    // This can hold unnecessary references to views.
                    cleanupAnimation();
                }
            });
            fromWorkspace.post(startAnimRunnable);
            mCurrentAnimation = animation;
        } else /* if (!animated) */ {
			dispatchOnLauncherTransitionPrepare( fromWorkspace , animated , true );
            dispatchOnLauncherTransitionStart(fromWorkspace, animated, true);
            dispatchOnLauncherTransitionEnd(fromWorkspace, animated, true);

            // Run any queued runnables
            if (onCompleteRunnable != null) {
                onCompleteRunnable.run();
            }

            mCurrentAnimation = null;
        }
    }

    /**
     * Creates and starts a new animation to the workspace.
     */
    private void startAnimationToWorkspaceFromOverlay(
            final Workspace.State fromWorkspaceState, final Workspace.State toWorkspaceState,
			final View buttonView ,
			final AppsView fromView ,
            final boolean animated, int animType, final Runnable onCompleteRunnable,
            final PrivateTransitionCallbacks pCb) {
        final AnimatorSet animation = LauncherAnimUtils.createAnimatorSet();
        final Resources res = mLauncher.getResources();
        final int revealDurationSlide = res.getInteger(R.integer.config_overlaySlideRevealTime);
		final View toView = mLauncher.getWorkspace();
		final View contentView = fromView.getContentView();
		final HashMap<View , Integer> layerViews = new HashMap<View , Integer>();

        // If for some reason our views aren't initialized, don't animate
        boolean initialized = buttonView != null;

        // Cancel the current animation
        cancelAnimation();

		boolean multiplePagesVisible = false;//toWorkspaceState.hasMultipleVisiblePages;

        playCommonTransitionAnimations(toWorkspaceState, fromView, toView,
                animated, initialized, animation, layerViews);
        if (!animated || !initialized) {
			if( fromWorkspaceState == Workspace.State.SMALL )
			{
                mAllAppsController.finishPullDown();
            }
            fromView.setVisibility(View.GONE);
			dispatchOnLauncherTransitionPrepare( fromView , animated , true );
            dispatchOnLauncherTransitionStart(fromView, animated, true);
            dispatchOnLauncherTransitionEnd(fromView, animated, true);
			dispatchOnLauncherTransitionPrepare( toView , animated , true );
            dispatchOnLauncherTransitionStart(toView, animated, true);
            dispatchOnLauncherTransitionEnd(toView, animated, true);
            pCb.onTransitionComplete();

            // Run any queued runnables
            if (onCompleteRunnable != null) {
                onCompleteRunnable.run();
            }
            return;
        }
		if( animType == PULLUP )
		{
            // We are animating the content view alpha, so ensure we have a layer for it
            layerViews.put(contentView, BUILD_AND_SET_LAYER);

            animation.addListener(new AnimatorListenerAdapter() {
                boolean canceled = false;
                @Override
                public void onAnimationCancel(Animator animation) {
                    canceled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (canceled) return;
                    dispatchOnLauncherTransitionEnd(fromView, animated, true);
                    dispatchOnLauncherTransitionEnd(toView, animated, true);
                    // Run any queued runnables
                    if (onCompleteRunnable != null) {
                        onCompleteRunnable.run();
                    }

                    // Disable all necessary layers
                    for (View v : layerViews.keySet()) {
						if( v != null && layerViews.get( v ) == BUILD_AND_SET_LAYER )
						{
                            v.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                    }

                    cleanupAnimation();
                    pCb.onTransitionComplete();
                }

            });
            boolean shouldPost = mAllAppsController.animateToWorkspace(animation, revealDurationSlide);

            // Dispatch the prepare transition signal
			dispatchOnLauncherTransitionPrepare( fromView , animated , true );
			dispatchOnLauncherTransitionPrepare( toView , animated , true );

            final AnimatorSet stateAnimation = animation;
            final Runnable startAnimRunnable = new Runnable() {
                public void run() {
                    // Check that mCurrentAnimation hasn't changed while
                    // we waited for a layout/draw pass
                    if (mCurrentAnimation != stateAnimation)
                        return;

                    dispatchOnLauncherTransitionStart(fromView, animated, false);
                    dispatchOnLauncherTransitionStart(toView, animated, false);

                    // Enable all necessary layers
                    for (View v : layerViews.keySet()) {
						if( v != null && layerViews.get( v ) == BUILD_AND_SET_LAYER )
						{
                            v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                        }
						if( Utilities.ATLEAST_LOLLIPOP && v != null && v.isAttachedToWindow() )
						{
                            v.buildLayer();
                        }
                    }

                    // Focus the new view
                    toView.requestFocus();
                    stateAnimation.start();
                }
            };
            mCurrentAnimation = animation;
            if (shouldPost) {
                fromView.post(startAnimRunnable);
            } else {
                startAnimRunnable.run();
            }
        }
        return;
    }
	
	//zhujieping add start //7.0进入主菜单动画改成也支持4.4主菜单样式
	/**
	 * Plays animations used by various transitions.
	 */
	private void playCommonTransitionAnimations(
			Workspace.State toWorkspaceState ,
			AppsView fromView ,
			View toView ,
			boolean animated ,
			boolean initialized ,
			AnimatorSet animation ,
			HashMap<View , Integer> layerViews )
	{
		// Create the workspace animation.
		// NOTE: this call apparently also sets the state for the workspace if !animated
		Animator workspaceAnim = mLauncher.startWorkspaceStateChangeAnimation( toWorkspaceState , animated , layerViews );
		if( animated && initialized && workspaceAnim != null )
		{
			// Play the workspace animation
			if( workspaceAnim != null )
			{
				animation.play( workspaceAnim );
			}
			// Dispatch onLauncherTransitionStep() as the animation interpolates.
			animation.play( dispatchOnLauncherTransitionStepAnim( fromView , toView ) );
		}
	}
	
	private Animator dispatchOnLauncherTransitionStepAnim(
			final AppsView fromView ,
			final View toView )
	{
		ValueAnimator updateAnimator = ValueAnimator.ofFloat( 0 , 1 );
		updateAnimator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(
					ValueAnimator animation )
			{
				dispatchOnLauncherTransitionStep( fromView , animation.getAnimatedFraction() );
				dispatchOnLauncherTransitionStep( toView , animation.getAnimatedFraction() );
			}
		} );
		return updateAnimator;
	}

    /**
     * Dispatches the prepare-transition event to suitable views.
     */
	void dispatchOnLauncherTransitionPrepare(
			View v ,
			boolean animated ,
			boolean multiplePagesVisible )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionPrepare( mLauncher , animated , multiplePagesVisible );
		}
	}
	
	/**
	 * Dispatches the start-transition event to suitable views.
	 */
	void dispatchOnLauncherTransitionStart(
			View v ,
			boolean animated ,
			boolean toWorkspace )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionStart( mLauncher , animated , toWorkspace );
		}
		// Update the workspace transition step as well
		dispatchOnLauncherTransitionStep( v , 0f );
	}
	
	/**
	 * Dispatches the step-transition event to suitable views.
	 */
	void dispatchOnLauncherTransitionStep(
			View v ,
			float t )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionStep( mLauncher , t );
		}
	}
	
	/**
	 * Dispatches the end-transition event to suitable views.
	 */
	void dispatchOnLauncherTransitionEnd(
			View v ,
			boolean animated ,
			boolean toWorkspace )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionEnd( mLauncher , animated , toWorkspace );
		}
		// Update the workspace transition step as well
		dispatchOnLauncherTransitionStep( v , 1f );
	}
	//zhujieping add end
	
	/**
	 * Dispatches the prepare-transition event to suitable views.
	 */
	void dispatchOnLauncherTransitionPrepare(
			AppsView v ,
			boolean animated ,
            boolean multiplePagesVisible) {
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionPrepare( mLauncher , animated ,
                    multiplePagesVisible);
        }
    }

    /**
     * Dispatches the start-transition event to suitable views.
     */
	void dispatchOnLauncherTransitionStart(
			AppsView v ,
			boolean animated ,
			boolean toWorkspace )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionStart( mLauncher , animated ,
                    toWorkspace);
        }

        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 0f);
    }

    /**
     * Dispatches the step-transition event to suitable views.
     */
	void dispatchOnLauncherTransitionStep(
			AppsView v ,
			float t )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionStep( mLauncher , t );
        }
    }

    /**
     * Dispatches the end-transition event to suitable views.
     */
	void dispatchOnLauncherTransitionEnd(
			AppsView v ,
			boolean animated ,
			boolean toWorkspace )
	{
		if( v instanceof ILauncherTransitionable )
		{
			( (ILauncherTransitionable)v ).onLauncherTransitionEnd( mLauncher , animated ,
                    toWorkspace);
        }
        // Update the workspace transition step as well
        dispatchOnLauncherTransitionStep(v, 1f);
    }

    /**
     * Cancels the current animation.
     */
    private void cancelAnimation() {
        if (mCurrentAnimation != null) {
            mCurrentAnimation.setDuration(0);
            mCurrentAnimation.cancel();
            mCurrentAnimation = null;
        }
    }

    @Thunk void cleanupAnimation() {
        mCurrentAnimation = null;
    }
}
