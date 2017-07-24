package com.cooee.phenix.AppList.KitKat;


import android.content.Context;
import android.view.View;

import com.cooee.phenix.CellLayout;
import com.cooee.phenix.ShortcutAndWidgetContainer;
import com.cooee.phenix.PagedView.IPage;


public class AppsCustomizeCellLayout extends CellLayout implements IPage
{
	
	public AppsCustomizeCellLayout(
			Context context )
	{
		super( context );
	}
	
	@Override
	public void removeAllViewsOnPage()
	{
		removeAllViews();
		setLayerType( LAYER_TYPE_NONE , null );
	}
	
	@Override
	public void removeViewOnPageAt(
			int index )
	{
		removeViewAt( index );
	}
	
	//	@Override
	//	public int getPageChildCount()
	//	{
	//		return getChildCount();
	//	}
	@Override
	public View getChildOnPageAt(
			int i )
	{
		return getChildAt( i );
	}
	
	@Override
	public int indexOfChildOnPage(
			View v )
	{
		return indexOfChild( v );
	}
	
	/**
	 * Clears all the key listeners for the individual icons.
	 */
	public void resetChildrenOnKeyListeners()
	{
		ShortcutAndWidgetContainer children = getShortcutsAndWidgets();
		int childCount = children.getChildCount();
		for( int j = 0 ; j < childCount ; ++j )
		{
			children.getChildAt( j ).setOnKeyListener( null );
		}
	}
	
	@Override
	public int getChildrenCellX(
			int index )
	{
		return ( (AppsCustomizeCellLayout.LayoutParams)( getChildrenLayout().getChildAt( index ).getLayoutParams() ) ).cellX;
	}
	
	@Override
	public int getChildrenCellY(
			int index )
	{
		return ( (AppsCustomizeCellLayout.LayoutParams)( getChildrenLayout().getChildAt( index ).getLayoutParams() ) ).cellY;
	}
	
	//cheyingkun add start	//主菜单和小部件页面指示器、页面底边距分开配置(修正主菜单界面打开动态图标界面跳动问题)
	private int top = -1;
	private int bottom = -1;
	
	@Override
	public int getPaddingTop()
	{
		if( top != -1 )
		{
			return top;
		}
		return super.getPaddingTop();
	}
	
	@Override
	public int getPaddingBottom()
	{
		if( bottom != -1 )
		{
			return bottom;
		}
		return super.getPaddingBottom();
	}
	
	@Override
	public void setPadding(
			int left ,
			int top ,
			int right ,
			int bottom )
	{
		super.setPadding( left , top , right , bottom );
		if( this.top < top )
		{
			this.top = top;
		}
		if( this.bottom < top )
		{
			this.bottom = top;
		}
	}
	//cheyingkun add end
}
