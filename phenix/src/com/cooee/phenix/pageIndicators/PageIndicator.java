package com.cooee.phenix.pageIndicators;


import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;


public abstract class PageIndicator extends LinearLayout
{
	
	@SuppressWarnings( "unused" )
	private static final String TAG = "PageIndicator";
	
	public PageIndicator(
			Context context )
	{
		this( context , null );
	}
	
	public PageIndicator(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public PageIndicator(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );

	}

	public abstract void addMarker(
			int index ,
			PageMarkerResources marker ,
			boolean allowAnimations );
	
	public abstract void addMarkers(
			ArrayList<PageMarkerResources> markers ,
			boolean allowAnimations );
	
	public abstract void updateMarker(
			int index ,
			PageMarkerResources marker );
	
	public abstract void removeMarker(
			int index ,
			boolean allowAnimations );
	
	public abstract void removeAllMarkers(
			boolean allowAnimations );
	
	public abstract void setActiveMarker(
			int index );
	
	public void showOrHideFunctionPagesPageIndicator(
			ArrayList<Integer> mFunctionPagesIndex ,
			boolean show )
	{
		
	}
	
	public PageMarkerResources getMusicPageMarkerResources()
	{
		return null;
	}
	
	public PageMarkerResources getCameraPageMarkerResources()
	{
		return null;
	}
	
	public PageMarkerResources getFavoritesPageMarkerResources()
	{
		return null;
	}
	
	public PageMarkerResources getAddPageMarkerResources()
	{
		return null;
	}
	
	public PageMarkerResources getHomePageMarkerResources()
	{
		return null;
	}
	//fulijuan add start	//页面指示器支持本地化
	public PageMarkerResources getDefaultPageMarkerResources()
	{
		return null;
	}
	//fulijuan add end
	
}
