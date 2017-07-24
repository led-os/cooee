package com.cooee.phenix.pageIndicators;


import java.util.ArrayList;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

import com.cooee.phenix.Launcher;
import com.cooee.phenix.R;
import com.cooee.phenix.AppList.Marshmallow.CaretDrawable;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class PageIndicatorCaret extends PageIndicator implements OnClickListener , OnLongClickListener
{

	
	private ImageView mAllAppsHandle;
	private CaretDrawable caretDrawable;
	private Launcher mLauncher;
	public PageIndicatorCaret(
			Context context )
	{
		this( context , null );
		// TODO Auto-generated constructor stub
	}
	
	public PageIndicatorCaret(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
		// TODO Auto-generated constructor stub
	}
	
	public PageIndicatorCaret(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		// TODO Auto-generated constructor stub
		int caretSize = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_caret_size );
		caretDrawable = new CaretDrawable( getContext() );
		caretDrawable.setBounds( 0 , 0 , caretSize , caretSize );
		mLauncher = getLauncher( context );
	}
	
	
	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mAllAppsHandle = (ImageView)findViewById( R.id.all_apps_handle );
		mAllAppsHandle.setImageDrawable( caretDrawable );
		mAllAppsHandle.setOnClickListener( this );
		mAllAppsHandle.setOnLongClickListener( this );
	}
	
	public CaretDrawable getCaretDrawable()
	{
		return caretDrawable;
	}
	@Override
	public void addMarker(
			int index ,
			PageMarkerResources marker ,
			boolean allowAnimations )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void addMarkers(
			ArrayList<PageMarkerResources> markers ,
			boolean allowAnimations )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void updateMarker(
			int index ,
			PageMarkerResources marker )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void removeMarker(
			int index ,
			boolean allowAnimations )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void removeAllMarkers(
			boolean allowAnimations )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void setActiveMarker(
			int index )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void showOrHideFunctionPagesPageIndicator(
			ArrayList<Integer> mFunctionPagesIndex ,
			boolean show )
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean onLongClick(
			View v )
	{
		// TODO Auto-generated method stub
		if( mLauncher != null && !mLauncher.getWorkspace().workspaceInModalState() )
		{
			mLauncher.onLongClickAllAppsButton( v );
			return true;
		}
		return false;
	}
	
	@Override
	public void onClick(
			View v )
	{
		// TODO Auto-generated method stub
		if( mLauncher != null && !mLauncher.getWorkspace().workspaceInModalState() )
		{
			mLauncher.onClickAllAppsButton( v );
		}
	}
	
	private Launcher getLauncher(
			Context context )
	{
		if( context instanceof Launcher )
		{
			return (Launcher)context;
		}
		return( (Launcher)( (ContextWrapper)context ).getBaseContext() );
	}
	
}
