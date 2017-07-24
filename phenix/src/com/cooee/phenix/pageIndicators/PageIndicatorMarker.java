package com.cooee.phenix.pageIndicators;


import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class PageIndicatorMarker extends FrameLayout
{
	
	@SuppressWarnings( "unused" )
	private static final String TAG = "PageIndicator";
	private static final int MARKER_FADE_DURATION = 175;
	private ImageView mActiveMarker;
	private ImageView mInactiveMarker;
	private boolean mIsActive = false;
	
	public PageIndicatorMarker(
			Context context )
	{
		this( context , null );
	}
	
	public PageIndicatorMarker(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public PageIndicatorMarker(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
	}
	
	protected void onFinishInflate()
	{
		mActiveMarker = (ImageView)findViewById( R.id.active );
		mInactiveMarker = (ImageView)findViewById( R.id.inactive );
	}
	
	//fulijuan start	//页面指示器支持本地化
	//fulijuan del start
	//	public void setMarkerDrawables(
	//			int activeResId ,
	//			int inactiveResId )
	//	{
	//		Resources r = getResources();
	//		mActiveMarker.setImageDrawable( r.getDrawable( activeResId ) );
	//		mInactiveMarker.setImageDrawable( r.getDrawable( inactiveResId ) );
	//	}
	//fulijuan del end
	//fulijuan add start
	public void setMarkerDrawables(
			Drawable activeDrawable ,
			Drawable inactiveDrawable )
	{
		mActiveMarker.setImageDrawable( activeDrawable );
		mInactiveMarker.setImageDrawable( inactiveDrawable );
	}
	//fulijuan add end
	//fulijuan end
	
	public void activate(
			boolean immediate )
	{
		if( immediate )
		{
			mActiveMarker.animate().cancel();
			mActiveMarker.setAlpha( 1f );
			mActiveMarker.setScaleX( 1f );
			mActiveMarker.setScaleY( 1f );
			mInactiveMarker.animate().cancel();
			mInactiveMarker.setAlpha( 0f );
		}
		else
		{
			mActiveMarker.animate().alpha( 1f ).scaleX( 1f ).scaleY( 1f ).setDuration( MARKER_FADE_DURATION ).start();
			mInactiveMarker.animate().alpha( 0f ).setDuration( MARKER_FADE_DURATION ).start();
		}
		mIsActive = true;
	}
	
	public void inactivate(
			boolean immediate )
	{
		if( immediate )
		{
			mInactiveMarker.animate().cancel();
			mInactiveMarker.setAlpha( 1f );
			mActiveMarker.animate().cancel();
			mActiveMarker.setAlpha( 0f );
			mActiveMarker.setScaleX( LauncherDefaultConfig.CONFIG_PAGEINDICATOR_SCALE );
			mActiveMarker.setScaleY( LauncherDefaultConfig.CONFIG_PAGEINDICATOR_SCALE );
		}
		else
		{
			mInactiveMarker.animate().alpha( 1f ).setDuration( MARKER_FADE_DURATION ).start();
			mActiveMarker.animate().alpha( 0f ).scaleX( LauncherDefaultConfig.CONFIG_PAGEINDICATOR_SCALE ).scaleY( LauncherDefaultConfig.CONFIG_PAGEINDICATOR_SCALE )
					.setDuration( MARKER_FADE_DURATION ).start();
		}
		mIsActive = false;
	}
	
	boolean isActive()
	{
		return mIsActive;
	}
}
