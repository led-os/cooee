package com.cooee.phenix.loading;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.cooee.phenix.LauncherAnimUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.Functions.DefaultLauncherGuide.DefaultLauncherGuideManager;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


// cheyingkun add whole file //桌面启动页样式（详见“BaseDefaultConfig”中说明）
public class LauncherLoading extends RelativeLayout
{
	
	private boolean isAnimation;
	private boolean isLoadFinish;
	private int minShowTime;
	private int HIDE_ANIM_DURATION;
	
	public LauncherLoading(
			Context context )
	{
		super( context );
	}
	
	public LauncherLoading(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
	}
	
	public LauncherLoading(
			Context context ,
			AttributeSet attrs ,
			int defStyleAttr )
	{
		super( context , attrs , defStyleAttr );
	}
	
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		minShowTime = LauncherDefaultConfig.getInt( R.integer.config_loading_page_style_0_min_keep_time );
		HIDE_ANIM_DURATION = LauncherDefaultConfig.getInt( R.integer.config_loading_page_style_0_hide_anim_duration );
	}
	
	@Override
	public boolean onTouchEvent(
			MotionEvent event )
	{
		return true;
	}
	
	public void setLoadFinish(
			boolean isLoadFinish )
	{
		this.isLoadFinish = isLoadFinish;
	}
	
	public void showLauncherLoadingAnim()
	{
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat( "alpha" , 1 );
		ObjectAnimator oas = LauncherAnimUtils.ofPropertyValuesHolder( this , alpha );
		oas.addListener( new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationStart(
					Animator animation )
			{
				setVisibility( View.VISIBLE );
				isAnimation = true;
			}
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				isAnimation = false;
				dismissLauncherLoadingAnim();
			}
		} );
		oas.setDuration( minShowTime );
		oas.start();
	}
	
	public void dismissLauncherLoadingAnim()
	{
		if( !isLoadFinish || isAnimation || getVisibility() == View.GONE )
		{
			return;
		}
		PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat( "alpha" , 0 );
		ObjectAnimator oas = LauncherAnimUtils.ofPropertyValuesHolder( this , alpha );
		oas.addListener( new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationStart(
					Animator animation )
			{
				isAnimation = true;
			}
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				isAnimation = false;
				setVisibility( View.GONE );
				if( getParent() != null && getParent() instanceof ViewGroup )
				{
					( (ViewGroup)getParent() ).removeView( LauncherLoading.this );
				}
				removeAllViews();
				//xiatian add start	//设置默认桌面引导
				if( !LauncherDefaultConfig.SWITCH_ENABLE_CLINGS )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_SET_TO_DEFAULT_LAUNCHER_GUIDE && !DefaultLauncherGuideManager.getInstance().isOnlyLauncher( LauncherAppState.getActivityInstance() ) )
					{
						DefaultLauncherGuideManager.getInstance().checkDefaultLauncherAndShowGuideDialog( true , LauncherAppState.getActivityInstance() );
					}
				}
				//xiatian add end
			}
		} );
		oas.setDuration( HIDE_ANIM_DURATION );
		oas.start();
	}
}
