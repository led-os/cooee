package com.cooee.favorites.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cooee.favorites.R;


/**
 * cheyingkun add whole file	//酷生活界面优化(酷生活搜索框点击效果)
 * @author cheyingkun
 */
public class HolographicLinearLayout extends LinearLayout
{
	
	private ImageView mImageView;
	private boolean mIsPressed;
	private boolean mIsFocused;
	
	public HolographicLinearLayout(
			Context context )
	{
		this( context , null );
	}
	
	public HolographicLinearLayout(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public HolographicLinearLayout(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		setWillNotDraw( false );
		setOnTouchListener( new OnTouchListener() {
			
			@Override
			public boolean onTouch(
					View v ,
					MotionEvent event )
			{
				if( isPressed() != mIsPressed )
				{
					mIsPressed = isPressed();
					refreshDrawableState();
				}
				return false;
			}
		} );
		setOnFocusChangeListener( new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(
					View v ,
					boolean hasFocus )
			{
				if( isFocused() != mIsFocused )
				{
					mIsFocused = isFocused();
					refreshDrawableState();
				}
			}
		} );
	}
	
	@Override
	protected void drawableStateChanged()
	{
		super.drawableStateChanged();
		if( mImageView != null )
		{
			Drawable d = mImageView.getBackground();
			if( d instanceof StateListDrawable )
			{
				StateListDrawable sld = (StateListDrawable)d;
				sld.setState( getDrawableState() );
				sld.invalidateSelf();
			}
		}
	}
	
	void invalidatePressedFocusedStates()
	{
		invalidate();
	}
	
	@Override
	protected void onDraw(
			Canvas canvas )
	{
		super.onDraw( canvas );
		// One time call to generate the pressed/focused state -- must be called after
		// measure/layout
		if( mImageView == null )
		{
			mImageView = (ImageView)findViewById( R.id.imageButton1 );
		}
	}
	
	@Override
	public int[] onCreateDrawableState(
			int extraSpace )
	{
		final int[] drawableState = super.onCreateDrawableState( extraSpace + 1 );
		return drawableState;
	}
}
