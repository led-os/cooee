// xiatian add whole file //需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
package com.cooee.phenix.WorkspaceMenu;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAnimUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class WorkspaceMenuVerticalList extends FrameLayout implements View.OnClickListener
{
	
	private static final String TAG = "WorkspaceMenuVerticalList";
	private ImageView mBgImageView = null;//workspace_menu_vertical_list_bg_id
	private WorkspaceMenuVerticalListContent mContentListView = null;//workspace_menu_vertical_list_content_id
	private AnimatorSet mShowOrHideAnimation;
	private DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator( 1.5f );
	private AnimatorListenerAdapter mShowAnimatorListenerAdapter;
	private AnimatorListenerAdapter mHideAnimatorListenerAdapter;
	
	public WorkspaceMenuVerticalList(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
	}
	
	@Override
	public void onFinishInflate()
	{
		super.onFinishInflate();
		setupViews();
	}
	
	private void setupViews()
	{
		if( mBgImageView == null )
		{
			setupBgView();
		}
		if( mContentListView == null )
		{
			setupContentView();
		}
	}
	
	private void setupBgView()
	{
		mBgImageView = (ImageView)findViewById( R.id.workspace_menu_vertical_list_bg_id );
		mBgImageView.setClickable( true );
		mBgImageView.setOnClickListener( this );
	}
	
	private void setupContentView()
	{
		mContentListView = (WorkspaceMenuVerticalListContent)findViewById( R.id.workspace_menu_vertical_list_content_id );
	}
	
	@Override
	public void onClick(
			View v )
	{
		if( v == mBgImageView )
		{//点击bg view（即：ListView以外的区域），隐藏menu。
			hideWithAnim();
		}
	}
	
	@Override
	public void setVisibility(
			int visibility )
	{
		if( visibility == View.INVISIBLE )
		{//隐藏
			if( mContentListView != null )
			{//隐藏时，设置ListView回到顶部。
				mContentListView.setSelection( 0 );
			}
		}
		super.setVisibility( visibility );
	}
	
	public void showWithAnim()
	{
		if( getVisibility() == View.VISIBLE )
		{
			return;
		}
		if( mShowOrHideAnimation == null )
		{
			mShowOrHideAnimation = LauncherAnimUtils.createAnimatorSet();
		}
		if( mShowOrHideAnimation.isRunning() )
		{
			return;
		}
		if( mShowOrHideAnimation.isStarted() && mShowOrHideAnimation.isPaused() )
		{
			mShowOrHideAnimation.cancel();
		}
		//BgImageView
		ObjectAnimator mBgShowAlphaAnim = getBgShowAlphaAnim();
		mShowOrHideAnimation.play( mBgShowAlphaAnim );
		//ContentListView
		int mContentListViewHeight = mContentListView.getHeight();
		mContentListView.setTranslationY( mContentListViewHeight );
		ObjectAnimator mContentShowTranslationYAnim = getContentShowTranslationYAnim();
		mShowOrHideAnimation.play( mContentShowTranslationYAnim );
		//to start
		////prepareStartAnimation
		prepareStartAnimation( mBgImageView );
		prepareStartAnimation( mContentListView );
		////add and remove Listener
		mShowOrHideAnimation.removeAllListeners();
		if( mShowAnimatorListenerAdapter == null )
		{
			mShowAnimatorListenerAdapter = new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationStart(
						Animator animation )
				{
					setVisibility( View.VISIBLE );
				}
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
				}
			};
		}
		mShowOrHideAnimation.addListener( mShowAnimatorListenerAdapter );
		////start
		mShowOrHideAnimation.start();
	}
	
	public void hideWithAnim()
	{
		if( getVisibility() != View.VISIBLE )
		{
			return;
		}
		if( mShowOrHideAnimation == null )
		{
			mShowOrHideAnimation = LauncherAnimUtils.createAnimatorSet();
		}
		if( mShowOrHideAnimation.isRunning() )
		{
			return;
		}
		if( mShowOrHideAnimation.isStarted() && mShowOrHideAnimation.isPaused() )
		{
			mShowOrHideAnimation.cancel();
		}
		//BgImageView
		ObjectAnimator mBgHideAlphaAnim = getBgHideAlphaAnim();
		mShowOrHideAnimation.play( mBgHideAlphaAnim );
		//ContentListView
		mContentListView.setTranslationY( 0 );
		ObjectAnimator mContentHideTranslationYAnim = getContentHideTranslationYAnim();
		mShowOrHideAnimation.play( mContentHideTranslationYAnim );
		//to start
		////prepareStartAnimation
		prepareStartAnimation( mBgImageView );
		prepareStartAnimation( mContentListView );
		////add and remove Listener
		mShowOrHideAnimation.removeAllListeners();
		if( mHideAnimatorListenerAdapter == null )
		{
			mHideAnimatorListenerAdapter = new AnimatorListenerAdapter() {
				
				@Override
				public void onAnimationStart(
						Animator animation )
				{
				}
				
				@Override
				public void onAnimationEnd(
						Animator animation )
				{
					setVisibility( View.INVISIBLE );
				}
			};
		}
		mShowOrHideAnimation.addListener( mHideAnimatorListenerAdapter );
		////start
		mShowOrHideAnimation.start();
	}
	
	private ObjectAnimator getBgShowAlphaAnim()
	{
		ObjectAnimator mBgShowAlphaAnim = LauncherAnimUtils.ofFloat( mBgImageView , "alpha" , 0f , 1f );
		setupAnimation( mBgShowAlphaAnim , mBgImageView );
		return mBgShowAlphaAnim;
	}
	
	private ObjectAnimator getContentShowTranslationYAnim()
	{
		int mContentListViewHeight = mContentListView.getHeight();
		ObjectAnimator mContentShowTranslationYAnim = LauncherAnimUtils.ofFloat( mContentListView , "translationY" , mContentListViewHeight , 0 );
		setupAnimation( mContentShowTranslationYAnim , mContentListView );
		return mContentShowTranslationYAnim;
	}
	
	private ObjectAnimator getBgHideAlphaAnim()
	{
		ObjectAnimator mBgHideAlphaAnim = LauncherAnimUtils.ofFloat( mBgImageView , "alpha" , 1f , 0f );
		setupAnimation( mBgHideAlphaAnim , mBgImageView );
		return mBgHideAlphaAnim;
	}
	
	private ObjectAnimator getContentHideTranslationYAnim()
	{
		int mContentListViewHeight = mContentListView.getHeight();
		ObjectAnimator mContentHideTranslationYAnim = LauncherAnimUtils.ofFloat( mContentListView , "translationY" , 0 , mContentListViewHeight );
		setupAnimation( mContentHideTranslationYAnim , mContentListView );
		return mContentHideTranslationYAnim;
	}
	
	private void setupAnimation(
			ObjectAnimator anim ,
			final View v )
	{
		anim.setInterpolator( mDecelerateInterpolator );
		anim.setDuration( LauncherDefaultConfig.getInt( R.integer.config_workspace_menu_vertical_list_show_or_hide_animation_duration ) );
		anim.addListener( new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				if( v != null )
				{
					v.setLayerType( View.LAYER_TYPE_NONE , null );
				}
			}
		} );
	}
	
	private void prepareStartAnimation(
			View v )
	{
		v.setLayerType( View.LAYER_TYPE_HARDWARE , null );
	}
	
	public boolean isAnimationRuning()
	{
		boolean ret = false;
		if( mShowOrHideAnimation != null && mShowOrHideAnimation.isRunning() )
		{
			ret = true;
		}
		return ret;
	}
	
	public void setLauncher(
			Launcher mLauncher )
	{
		mContentListView.setLauncher( mLauncher );
	}
	
	public void hideNoAnim()
	{
		if( getVisibility() != View.VISIBLE )
		{
			return;
		}
		if( mShowOrHideAnimation == null )
		{
			mShowOrHideAnimation = LauncherAnimUtils.createAnimatorSet();
		}
		if( mShowOrHideAnimation.isRunning() )
		{
			return;
		}
		setVisibility( View.INVISIBLE );
	}
}
