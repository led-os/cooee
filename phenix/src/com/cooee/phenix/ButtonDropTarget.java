package com.cooee.phenix;


import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cooee.phenix.AppList.KitKat.AppsCustomizePagedView;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


/**
 * Implements a DropTarget.
 */
public class ButtonDropTarget extends TextView implements DropTarget , DragController.DragListener
{
	
	protected final int mTransitionDuration;
	protected Launcher mLauncher;
	private int mBottomDragPadding;
	protected TextView mText;
	protected SearchDropTargetBar mSearchDropTargetBar;
	/** Whether this drop target is active for the current drag */
	protected boolean mActive;
	/** The paint applied to the drag view on hover */
	protected int mHoverColor = 0;
	
	public ButtonDropTarget(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public ButtonDropTarget(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		mTransitionDuration = LauncherDefaultConfig.getInt( R.integer.config_dropTargetBgTransitionDuration );
		mBottomDragPadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.drop_target_drag_padding );
	}
	
	void setLauncher(
			Launcher launcher )
	{
		mLauncher = launcher;
	}
	
	public boolean acceptDrop(
			DragObject d )
	{
		return false;
	}
	
	public void setSearchDropTargetBar(
			SearchDropTargetBar searchDropTargetBar )
	{
		mSearchDropTargetBar = searchDropTargetBar;
	}
	
	protected Drawable getCurrentDrawable()
	{
		// gaominghui@2016/12/14 ADD START
		Drawable[] drawables;
		if( Build.VERSION.SDK_INT < 17 )
		{
			drawables = getCompoundDrawables();
		}
		else
		{
			drawables = getCompoundDrawablesRelative();
		}
		// gaominghui@2016/12/14 ADD END
		for( int i = 0 ; i < drawables.length ; ++i )
		{
			if( drawables[i] != null )
			{
				return drawables[i];
			}
		}
		return null;
	}
	
	public void onDrop(
			DragObject d )
	{
	}
	
	public void onFlingToDelete(
			DragObject d ,
			int x ,
			int y ,
			PointF vec )
	{
		// Do nothing
	}
	
	public void onDragEnter(
			DragObject d )
	{
		if( d != null && d.dragView != null )//zhujieping add //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
			d.dragView.setColor( mHoverColor );
	}
	
	public void onDragOver(
			DragObject d )
	{
		// Do nothing
	}
	
	public void onDragExit(
			DragObject d )
	{
		if( d != null && d.dragView != null )//zhujieping add //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
			d.dragView.setColor( 0 );
	}
	
	public void onDragStart(
			DragSource source ,
			Object info ,
			int dragAction )
	{
		if( source instanceof AppsCustomizePagedView )
		{
			if( mLauncher.getAppsMode() == AppsCustomizePagedView.EDIT_MODE )
			{
				if( getParent() != null && getParent() instanceof View )
					( (View)getParent() ).setBackgroundColor( getResources().getColor( R.color.app_menu_search_drop_target_bg_color ) );
			}
		}
	}
	
	public boolean isDropEnabled()
	{
		return mActive;
	}
	
	public void onDragEnd()
	{
		if( getParent() != null && getParent() instanceof View )
			( (View)getParent() ).setBackgroundColor( 0 );
	}
	
	@Override
	public void getHitRectRelativeToDragLayer(
			android.graphics.Rect outRect )
	{
		super.getHitRect( outRect );
		outRect.bottom += mBottomDragPadding;
		int[] coords = new int[2];
		mLauncher.getDragLayer().getDescendantCoordRelativeToSelf( this , coords );
		outRect.offsetTo( coords[0] , coords[1] );
		//xiatian add start	//添加配置项“switch_enable_extend_drop_bar_area”，是否支持扩大“垃圾筐”和“应用信息框”的响应区域。true时为“1、单独显示一个时：上为屏幕顶端、左为搜索框左边框、右为搜索框右边框；2、显示两个时：上为屏幕顶端、宽度均分搜索框”；false时为“图标和文字区域的边界”。默认false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_EXTEND_DROP_BAR_AREA )
		{
			int mLeft = -1;
			int mRight = -1;
			ViewGroup mDropTargetBar = null;
			ViewGroup mDeleteDropTargetContainer = null;
			ViewGroup mInfoDropTargetContainer = null;
			if( this instanceof DeleteDropTarget && getVisibility() == View.VISIBLE )
			{
				mDeleteDropTargetContainer = (ViewGroup)getParent();
				mDropTargetBar = (ViewGroup)mDeleteDropTargetContainer.getParent();
				mInfoDropTargetContainer = (ViewGroup)mDropTargetBar.getChildAt( 1 );
				if( mInfoDropTargetContainer.getVisibility() != View.VISIBLE )
				{//抬起桌面可卸载（删除）图标
					mLeft = mDropTargetBar.getLeft();
					mRight = mDropTargetBar.getRight();
				}
				else
				{//抬起主菜单可卸载（删除）图标
					mLeft = mDropTargetBar.getLeft();
					mRight = mLeft + mDeleteDropTargetContainer.getWidth();
				}
			}
			if( this instanceof InfoDropTarget && getVisibility() == View.VISIBLE )
			{
				mInfoDropTargetContainer = (ViewGroup)getParent();
				mDropTargetBar = (ViewGroup)mInfoDropTargetContainer.getParent();
				mDeleteDropTargetContainer = (ViewGroup)mDropTargetBar.getChildAt( 0 );
				if( mDeleteDropTargetContainer.getVisibility() != View.VISIBLE )
				{//抬起桌面可卸载（删除）图标
					mLeft = mDropTargetBar.getLeft();
					mRight = mDropTargetBar.getRight();
				}
				else
				{//抬起主菜单可卸载（删除）图标
					mLeft = mDropTargetBar.getLeft() + mInfoDropTargetContainer.getLeft();
					mRight = mLeft + mInfoDropTargetContainer.getWidth();
				}
			}
			outRect.left = mLeft;
			outRect.right = mRight;
			outRect.top = 0;
		}
		//xiatian add end
	}
	
	private boolean isRtl()
	{
		//xiatian start	//整理判断“是否从左往右布局”的方法：由“mView.getLayoutDirection()”改为“getResources().getConfiguration().getLayoutDirection()”
		//		return Tools.isLayoutRTL( this );//xiatian del
		return LauncherAppState.isLayoutRTL();//xiatian add 
		//xiatian end
	}
	
	Rect getIconRect(
			int viewWidth ,
			int viewHeight ,
			int drawableWidth ,
			int drawableHeight )
	{
		DragLayer dragLayer = mLauncher.getDragLayer();
		// Find the rect to animate to (the view is center aligned)
		Rect to = new Rect();
		dragLayer.getViewRectRelativeToSelf( this , to );
		final int width = drawableWidth;
		final int height = drawableHeight;
		final int left;
		final int right;
		if( isRtl() )
		{
			right = to.right - getPaddingRight();
			left = right - width;
		}
		else
		{
			left = to.left + getPaddingLeft();
			right = left + width;
		}
		final int top = to.top + ( getMeasuredHeight() - height ) / 2;
		final int bottom = top + height;
		to.set( left , top , right , bottom );
		// Center the destination rect about the trash icon
		final int xOffset = (int)-( viewWidth - width ) / 2;
		final int yOffset = (int)-( viewHeight - height ) / 2;
		to.offset( xOffset , yOffset );
		return to;
	}
	
	public void getLocationInDragLayer(
			int[] loc )
	{
		mLauncher.getDragLayer().getLocationInDragLayer( this , loc );
	}
	
	//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
	public void setDropTargetVisible(
			boolean ... args )
	{
		
	}
	//zhujieping add end
}
