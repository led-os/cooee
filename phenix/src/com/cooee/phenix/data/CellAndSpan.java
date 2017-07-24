package com.cooee.phenix.data;


import com.cooee.framework.utils.StringUtils;


/**
 * Represents an item in the launcher.
 */
public class CellAndSpan
{
	
	int x , y;
	int spanX , spanY;
	
	public CellAndSpan()
	{
	}
	
	public void copy(
			CellAndSpan copy )
	{
		copy.x = x;
		copy.y = y;
		copy.spanX = spanX;
		copy.spanY = spanY;
	}
	
	public CellAndSpan(
			int x ,
			int y ,
			int spanX ,
			int spanY )
	{
		this.x = x;
		this.y = y;
		this.setSpanX( spanX );
		this.setSpanY( spanY );
	}
	
	public String toString()
	{
		return StringUtils.concat( "(x:" , x , "-y:" , y , "-spanX:" , spanX , "-spanY:" , spanY , ")" );
	}
	
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
	
	public int getX()
	{
		return x;
	}
	
	public void setX(
			int x )
	{
		this.x = x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public void setY(
			int y )
	{
		this.y = y;
	}
}
