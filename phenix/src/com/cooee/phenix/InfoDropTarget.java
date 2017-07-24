package com.cooee.phenix;


import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.cooee.phenix.AppList.KitKat.AppsCustomizePagedView;
import com.cooee.phenix.AppList.Marshmallow.AllAppsContainerView;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.PendingAddItemInfo;
import com.cooee.phenix.data.ShortcutInfo;


public class InfoDropTarget extends ButtonDropTarget
{
	
	private ColorStateList mOriginalTextColor;
	private TransitionDrawable mDrawable;
	
	public InfoDropTarget(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public InfoDropTarget(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
	}
	
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mOriginalTextColor = getTextColors();
		// Get the hover color
		Resources r = getResources();
		mHoverColor = r.getColor( R.color.info_target_hover_tint );
		mDrawable = (TransitionDrawable)getCurrentDrawable();
		if( null != mDrawable )
		{
			mDrawable.setCrossFadeEnabled( true );
		}
	}
	
	private boolean isFromAllApps(
			DragSource source )
	{
		return( source instanceof AppsCustomizePagedView
				//
				|| source instanceof AllAppsContainerView );/* //zhujieping add //解决“config_applist_style配置为1或2时，从主菜单拖动图标到桌面，不显示应用信息和卸载（系统应用不显示卸载，只显示应用信息）”的问题。 */
	}
	
	@Override
	public boolean acceptDrop(
			DragObject d )
	{
		// acceptDrop is called just before onDrop. We do the work here, rather than
		// in onDrop, because it allows us to reject the drop (by returning false)
		// so that the object being dragged isn't removed from the drag source.
		ComponentName componentName = null;
		if( d.dragInfo instanceof AppInfo )
		{
			componentName = ( (AppInfo)d.dragInfo ).getComponentName();
		}
		else if( d.dragInfo instanceof ShortcutInfo )
		{
			componentName = ( (ShortcutInfo)d.dragInfo ).getIntent().getComponent();
		}
		else if( d.dragInfo instanceof PendingAddItemInfo )
		{
			componentName = ( (PendingAddItemInfo)d.dragInfo ).getComponentName();
		}
		if( componentName != null )
		{
			mLauncher.startApplicationDetailsActivity( componentName );
		}
		// There is no post-drop animation, so clean up the DragView now
		d.deferDragViewCleanupPostAnimation = false;
		return false;
	}
	
	@Override
	public void onDragStart(
			DragSource source ,
			Object info ,
			int dragAction )
	{
		super.onDragStart( source , info , dragAction );
		boolean isVisible = true;
		// Hide this button unless we are dragging something from AllApps
		if( !isFromAllApps( source ) )
		{
			isVisible = false;
		}
		setDropTargetVisible( isVisible );
	}
	
	@Override
	public void setDropTargetVisible(
			boolean ... args )
	{
		// TODO Auto-generated method stub
		boolean isVisible = args[0];
		mActive = isVisible;
		if( mDrawable != null )
		{
			mDrawable.resetTransition();
		}
		setTextColor( mOriginalTextColor );
		( (ViewGroup)getParent() ).setVisibility( isVisible ? View.VISIBLE : View.GONE );
		if( isVisible && !LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
		{
			if( Build.VERSION.SDK_INT >= 16 )
			{
				mLauncher.getWorkspace().setSystemUiVisibility( View.SYSTEM_UI_FLAG_FULLSCREEN );
			}
			else
			{
				WindowManager.LayoutParams attrs = mLauncher.getWindow().getAttributes();
				attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				mLauncher.getWindow().setAttributes( attrs );
			}
		}
	}
	
	@Override
	public void onDragEnd()
	{
		super.onDragEnd();
		mActive = false;
		if( Build.VERSION.SDK_INT >= 16 )
		{
			mLauncher.getWorkspace().setSystemUiVisibility( View.SYSTEM_UI_FLAG_VISIBLE );
		}
		else
		{
			WindowManager.LayoutParams attrs = mLauncher.getWindow().getAttributes();
			attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
			mLauncher.getWindow().setAttributes( attrs );
		}
	}
	
	public void onDragEnter(
			DragObject d )
	{
		super.onDragEnter( d );
		if( mDrawable != null )
			mDrawable.startTransition( mTransitionDuration );
		setTextColor( mHoverColor );
	}
	
	public void onDragExit(
			DragObject d )
	{
		super.onDragExit( d );
		if( !d.dragComplete )
		{
			if( mDrawable != null )
				mDrawable.resetTransition();
			setTextColor( mOriginalTextColor );
		}
	}
}
