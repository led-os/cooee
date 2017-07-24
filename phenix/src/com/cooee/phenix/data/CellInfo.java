package com.cooee.phenix.data;


import android.view.View;

import com.cooee.framework.utils.StringUtils;


/**
 * Represents an item in the launcher.
 */
public class CellInfo
{
	
	View cell;
	int cellX = -1;
	int cellY = -1;
	int spanX;
	int spanY;
	long screenId;
	long container;
	
	@Override
	public String toString()
	{
		return StringUtils.concat( "Cell[view:" + ( cell == null ? "null" : cell.getClass() ) , "-x:" , cellX , "-y:" , cellY , "]" );
	}
	
	//cheyingkun add start	//解决“拖动图标到垃圾框，松手快速点击桌面空白处，卸载提示出来后点击取消，之前拖动的图标消失”的问题。（bug：0010055）
	/**
	 * 把CellInfo的值克隆一份并返回出去
	 * @return
	 */
	public void cloneCellInfo(
			CellInfo mDragInfo )
	{
		if( mDragInfo == null )
		{
			return;
		}
		mDragInfo.cell = this.cell;
		mDragInfo.setCellX( this.getCellX() );
		mDragInfo.setCellY( this.getCellY() );
		mDragInfo.setSpanX( this.getSpanX() );
		mDragInfo.setSpanY( this.getSpanY() );
		mDragInfo.setScreenId( this.getScreenId() );
		mDragInfo.setContainer( this.getContainer() );
	}
	//cheyingkun add end
	;
	
	public int getSpanX()
	{
		return spanX;
	}
	
	public void setSpanX(
			int spanX )
	{
		this.spanX = spanX;
	}
	
	public int getSpanY()
	{
		return spanY;
	}
	
	public void setSpanY(
			int spanY )
	{
		this.spanY = spanY;
	}
	
	public long getScreenId()
	{
		return screenId;
	}
	
	public void setScreenId(
			long screenId )
	{
		this.screenId = screenId;
	}
	
	public View getCell()
	{
		return cell;
	}
	
	public void setCell(
			View cell )
	{
		this.cell = cell;
	}
	
	public int getCellX()
	{
		return cellX;
	}
	
	public void setCellX(
			int cellX )
	{
		this.cellX = cellX;
	}
	
	public int getCellY()
	{
		return cellY;
	}
	
	public void setCellY(
			int cellY )
	{
		this.cellY = cellY;
	}
	
	public long getContainer()
	{
		return container;
	}
	
	public void setContainer(
			long container )
	{
		this.container = container;
	}
}
