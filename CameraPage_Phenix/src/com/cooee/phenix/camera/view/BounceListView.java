package com.cooee.phenix.camera.view;


import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ListView;

import com.cooee.phenix.camera.inte.IOverScrollListener;


public class BounceListView extends ListView
{
	
	private static final int MAX_Y_OVERSCROLL_DISTANCE = 200;
	private Context mContext;
	private int mMaxYOverscrollDistance;
	private IOverScrollListener mOverScrollListener;
	
	public BounceListView(
			Context context )
	{
		super( context );
		mContext = context;
		initBounceListView();
	}
	
	public BounceListView(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		mContext = context;
		initBounceListView();
	}
	
	public BounceListView(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		mContext = context;
		initBounceListView();
	}
	
	/**
	 * 根据分辨率初始化回弹可拉动的最大距离
	 * @author yangtianyu 2016-7-4
	 */
	private void initBounceListView()
	{
		//get the density of the screen and do some maths with it on the max overscroll distance
		//variable so that you get similar behaviors no matter what the screen size
		final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		final float density = metrics.density;
		mMaxYOverscrollDistance = (int)( density * MAX_Y_OVERSCROLL_DISTANCE );
	}
	
	public void setOverScrollListener(
			IOverScrollListener listener )
	{
		this.mOverScrollListener = listener;
	}
	
	@Override
	protected boolean overScrollBy(
			int deltaX ,
			int deltaY ,
			int scrollX ,
			int scrollY ,
			int scrollRangeX ,
			int scrollRangeY ,
			int maxOverScrollX ,
			int maxOverScrollY ,
			boolean isTouchEvent )
	{
		if( mOverScrollListener != null )
		{
			mOverScrollListener.overScrollBy( deltaY );
		}
		return super.overScrollBy( deltaX , deltaY , scrollX , scrollY , scrollRangeX , scrollRangeY , maxOverScrollX , mMaxYOverscrollDistance , isTouchEvent );
	}
}
