package com.cooee.phenix.Folder;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.cooee.framework.stackblur.FuzzyBackGroundCallBack;
import com.cooee.launcher.framework.R;
import com.cooee.phenix.Launcher;


public class FoloderFuzzyBackGround extends ImageView implements FuzzyBackGroundCallBack
{
	
	private Bitmap mBitmapOld = null;
	private ObjectAnimator mOpenAnimator = null;
	private ObjectAnimator mCloseAnimator = null;
	private int mExpandDuration = 200;
	
	public FoloderFuzzyBackGround(
			Context context ,
			AttributeSet attrs ,
			int blurRadius ,
			int duration )
	{
		super( context , attrs );
		//		mBlurOptions = new BlurOptions();
		//		mBlurOptions.captureWallPaper = true;
		//		mBlurOptions.radius = blurRadius;
		//		mBlurOptions.callbacks = this;
		mExpandDuration = duration;
	}
	
	public FoloderFuzzyBackGround(
			Context context ,
			int blurRadius ,
			int duration )
	{
		super( context );
		//		mBlurOptions = new BlurOptions();
		//		mBlurOptions.captureWallPaper = true;
		//		mBlurOptions.radius = blurRadius;
		//		mBlurOptions.callbacks = this;
		mExpandDuration = duration;
	}
	
	public void setBackground()
	{
		setBackgroundDrawable( getResources().getDrawable( R.drawable.bulr_default_bg ) );//获取模糊壁纸前,设置默认模糊背景
		( (Launcher)getContext() ).getDragLayer().getFuzzyBackGround( this );
		//		BlurHelper.blurViewNonUiTread( getContext() , fuzzy , mBlurOptions );
	}
	
	public void showFuzzyBackgroundAnimation()
	{
		setScaleType( ScaleType.FIT_XY );
		setVisibility( View.VISIBLE );
		setAlpha( 0f );
		PropertyValuesHolder alphaPropertyValuesHolder = PropertyValuesHolder.ofFloat( "alpha" , 1f );
		//		mOpenAnimator = LauncherAnimUtils.ofPropertyValuesHolder( this , alphaPropertyValuesHolder );
		mOpenAnimator = new ObjectAnimator();
		mOpenAnimator.setTarget( this );
		mOpenAnimator.setValues( alphaPropertyValuesHolder );
		mOpenAnimator.setDuration( mExpandDuration );
		//lvjiangbin add begin  解决�?0011710: 【文件夹】智能分类后，点击文件夹展开后点击返回键�?回桌面，文件夹会闪动两次
		mOpenAnimator.addListener( new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationStart(
					Animator animation )
			{
			}
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				setLayerType( LAYER_TYPE_NONE , null );
			}
		} );
		setLayerType( LAYER_TYPE_HARDWARE , null );
		//lvjiangbin add end  解决�?0011710: 【文件夹】智能分类后，点击文件夹展开后点击返回键�?回桌面，文件夹会闪动两次
		mOpenAnimator.start();
	}
	
	public void disappearFuzzyBackgroundAnimation()
	{
		if( mCloseAnimator != null )
			mCloseAnimator.cancel();
		if( mOpenAnimator != null )
			mOpenAnimator.cancel();
		PropertyValuesHolder alphaPropertyValuesHolder = PropertyValuesHolder.ofFloat( "alpha" , 0f );
		//		mCloseAnimator = LauncherAnimUtils.ofPropertyValuesHolder( this , alphaPropertyValuesHolder );
		mCloseAnimator = new ObjectAnimator();
		mCloseAnimator.setTarget( this );
		mCloseAnimator.setValues( alphaPropertyValuesHolder );
		mCloseAnimator.addListener( new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationStart(
					Animator animation )
			{
			}
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				setVisibility( View.GONE );
				setBackgroundDrawable( null );
				//lvjiangbin add begin  解决�?0011710: 【文件夹】智能分类后，点击文件夹展开后点击返回键�?回桌面，文件夹会闪动两次
				//不知道为何引�?   推测  �?个加了硬件加速的动画  和一个没有加硬件加�?�的动画 �?起执行的时�?? 就会�?
				setLayerType( LAYER_TYPE_NONE , null );
				//lvjiangbin add end  解决�?0011710: 【文件夹】智能分类后，点击文件夹展开后点击返回键�?回桌面，文件夹会闪动两次
			}
		} );
		//lvjiangbin add begin  解决�?0011710: 【文件夹】智能分类后，点击文件夹展开后点击返回键�?回桌面，文件夹会闪动两次
		setLayerType( LAYER_TYPE_HARDWARE , null );
		//lvjiangbin add end  解决�?0011710: 【文件夹】智能分类后，点击文件夹展开后点击返回键�?回桌面，文件夹会闪动两次
		mCloseAnimator.setDuration( mExpandDuration );
		mCloseAnimator.start();
	}
	
	public void disappearFuzzyBackgroundWithoutAnimation()
	{
		if( mCloseAnimator != null )
			mCloseAnimator.cancel();
		if( mOpenAnimator != null )
			mOpenAnimator.cancel();
		setAlpha( 0f );
		setVisibility( View.GONE );
		setBackgroundDrawable( null );//cheyingkun add	//解决“模糊view不可见时，背景未置空，再次显示改view时导致桌面重启”的问题【i_0012782】
	}
	
	@Override
	public void setFuzzBackGround(
			Bitmap bluredBitmap )
	{
		if( bluredBitmap == null )
			return;
		if( mBitmapOld != null )
		{
			mBitmapOld.recycle();
			mBitmapOld = null;
		}
		mBitmapOld = bluredBitmap;
		Drawable old = getBackground();
		if( old != null )
		{
			old.setCallback( null );
			old = null;
		}
		setBackgroundDrawable( new BitmapDrawable( bluredBitmap ) );
	}
}
