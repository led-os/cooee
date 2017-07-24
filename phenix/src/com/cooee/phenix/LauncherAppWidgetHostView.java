package com.cooee.phenix;


import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.phenix.DragLayer.TouchCompleteListener;


/**
 * {@inheritDoc}
 */
public class LauncherAppWidgetHostView extends AppWidgetHostView implements TouchCompleteListener
//
, IOnThemeChanged
{
	
	private CheckLongPressHelper mLongPressHelper;
	private LayoutInflater mInflater;
	private Context mContext;
	private int mPreviousOrientation;
	private DragLayer mDragLayer;
	
	public LauncherAppWidgetHostView(
			Context context )
	{
		super( context );
		mContext = context;
		mLongPressHelper = new CheckLongPressHelper( this );
		mInflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		mDragLayer = ( (Launcher)context ).getDragLayer();
	}
	
	@Override
	protected View getErrorView()
	{
		return mInflater.inflate( R.layout.appwidget_error , this , false );
	}
	
	@Override
	public void updateAppWidget(
			RemoteViews remoteViews )
	{
		// Store the orientation in which the widget was inflated
		mPreviousOrientation = mContext.getResources().getConfiguration().orientation;
		super.updateAppWidget( remoteViews );
	}
	
	public boolean orientationChangedSincedInflation()
	{
		int orientation = mContext.getResources().getConfiguration().orientation;
		if( mPreviousOrientation != orientation )
		{
			return true;
		}
		return false;
	}
	
	public boolean onInterceptTouchEvent(
			MotionEvent ev )
	{
		// Consume any touch events for ourselves after longpress is triggered
		if( mLongPressHelper.hasPerformedLongPress() )
		{
			mLongPressHelper.cancelLongPress();
			return true;
		}
		// Watch for longpress events at this level to make sure
		// users can always pick up this widget
		switch( ev.getAction() )
		{
			case MotionEvent.ACTION_DOWN:
			{
				mLongPressHelper.postCheckForLongPress();
				mDragLayer.setTouchCompleteListener( this );
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mLongPressHelper.cancelLongPress();
				break;
		}
		// Otherwise continue letting touch events fall through to children
		return false;
	}
	
	public boolean onTouchEvent(
			MotionEvent ev )
	{
		// If the widget does not handle touch, then cancel
		// long press when we release the touch
		switch( ev.getAction() )
		{
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mLongPressHelper.cancelLongPress();
				break;
		}
		return false;
	}
	
	@Override
	public void cancelLongPress()
	{
		super.cancelLongPress();
		mLongPressHelper.cancelLongPress();
	}
	
	@Override
	public void onTouchComplete()
	{
		mLongPressHelper.cancelLongPress();
	}
	
	@Override
	public int getDescendantFocusability()
	{
		return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
	}
	
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
	}
}
