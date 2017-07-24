package com.cooee.wallpaper;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import com.cooee.wallpaper.manager.ChangeWallpaperManager;
import com.cooee.wallpaper.manager.ChangeWallpaperManager.ChangeWallpaperInterface;
import com.cooee.wallpaper.wrap.DynamicImageView;


public class ChangeWallpaper implements ChangeWallpaperInterface , View.OnClickListener
{
	
	private Activity mActivity;
	private DynamicImageView mImageView;
	private ViewGroup mParent;
	private ChangeWallpaperManager mManager;
	private boolean isFinishing = false;
	private final int SUCEESS_VIEW_SHOW_TIME = 8000;//换壁纸成功后，view显示的时间
	private final int MSG_HIDE_SUCCESS_VIEW = 0;
	private Handler mHandler = new Handler() {
		
		public void handleMessage(
				android.os.Message msg )
		{
			if( msg.what == MSG_HIDE_SUCCESS_VIEW )
			{
				if( isSuccessViewShow() )
				{
					hideSuccessView( true , true );
				}
			}
		};
	};
	
	public ChangeWallpaper(
			Activity activity ,
			ViewGroup parent ,
			DynamicImageView dynamicview )
	{
		mActivity = activity;
		mParent = parent;
		mImageView = dynamicview;
	}
	
	public void startChangeWallpaper(
			Context plugincontext )
	{
		// TODO Auto-generated method stub
		mManager = ChangeWallpaperManager.getInstance( plugincontext );
		mManager.setChangeWallpaperInterface( this );
		mManager.startOneKeyChangeWallpaper( mImageView );
		mParent.setOnClickListener( this );
	}
	
	@SuppressLint( "NewApi" )
	@Override
	public void showSuccessView()
	{
		// TODO Auto-generated method stub
		if( !mManager.getChangeWallpaperResult() )
		{
			return;
		}
		if( mParent != null )
		{
			final View mSuccessView = mManager.getInitedSucessView();
			if( mSuccessView != null )
			{
				if( mSuccessView.getParent() == null )
				{
					mManager.closeFolder();
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT , FrameLayout.LayoutParams.WRAP_CONTENT , Gravity.BOTTOM );
					mParent.addView( mSuccessView , lp );
					mParent.sendAccessibilityEvent( AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED );
					//设置不可见
					mSuccessView.setAlpha( 0 );
					//显示动画
					PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat( "alpha" , 1 );
					ObjectAnimator anim = new ObjectAnimator();
					anim.setTarget( mSuccessView );
					anim.setValues( alpha );
					anim.addListener( new AnimatorListenerAdapter() {
						
						@Override
						public void onAnimationStart(
								Animator animation )
						{
						}
						
						@Override
						public void onAnimationEnd(
								Animator animation )
						{
							mSuccessView.setLayerType( View.LAYER_TYPE_NONE , null );
							mHandler.sendEmptyMessageDelayed( MSG_HIDE_SUCCESS_VIEW , SUCEESS_VIEW_SHOW_TIME );
						}
					} );
					anim.setDuration( 500 );
					mSuccessView.setLayerType( View.LAYER_TYPE_HARDWARE , null );
					anim.start();
				}
			}
		}
	}
	
	@SuppressLint( "NewApi" )
	@Override
	public void hideSuccessView(
			boolean isAnim ,
			final boolean isFinish )
	{
		// TODO Auto-generated method stub
		if( mParent != null )
		{
			final View mSuccessView = mManager.getChangeWallpaperSuccessAfterView();
			if( isFinish )
			{
				this.isFinishing = true;
			}
			if( mSuccessView != null && mSuccessView.getParent() != null )
			{
				if( !isAnim )
				{
					mSuccessView.setLayerType( View.LAYER_TYPE_NONE , null );
					mManager.removeAdverView();
					mParent.removeView( mSuccessView );
					if( isFinish )
						finish();
					return;
				}
				PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat( "alpha" , 0 );
				ObjectAnimator anim = new ObjectAnimator();
				anim.setTarget( mSuccessView );
				anim.setValues( alpha );
				anim.addListener( new AnimatorListenerAdapter() {
					
					@Override
					public void onAnimationStart(
							Animator animation )
					{
					}
					
					@Override
					public void onAnimationEnd(
							Animator animation )
					{
						mSuccessView.setLayerType( View.LAYER_TYPE_NONE , null );
						mManager.removeAdverView();
						mParent.removeView( mSuccessView );
						if( isFinish )
							finish();
					}
				} );
				anim.setDuration( 500 );
				mSuccessView.setLayerType( View.LAYER_TYPE_HARDWARE , null );
				anim.start();
			}
			else
			{
				if( isFinish )
					finish();
			}
		}
	}
	
	public boolean isSuccessViewShow()
	{
		View successView = mManager.getChangeWallpaperSuccessAfterView();
		if( mParent != null && mParent.indexOfChild( successView ) != -1 )
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void onClick(
			View v )
	{
		// TODO Auto-generated method stub
		if( mImageView != null && mImageView.getVisibility() == View.VISIBLE )
		{
			return;
		}
		if( v == mParent )
		{
			hideSuccessView( true , true );
		}
	}
	
	@Override
	public void finish()
	{
		// TODO Auto-generated method stub
		if( mActivity != null )
		{
			mActivity.finish();
		}
	}
	
	@Override
	public boolean isFinishing()
	{
		// TODO Auto-generated method stub
		return isFinishing;
	}
	
	public void onBackPressed()
	{
		if( mImageView != null && mImageView.getVisibility() == View.VISIBLE )
		{
			return;
		}
		if( isSuccessViewShow() )
		{
			hideSuccessView( true , true );
		}
	}
}
