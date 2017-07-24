package com.cooee.phenix;


import android.text.TextUtils;

import com.cooee.framework.utils.StringUtils;


/**
 * cheyingkun add whole file	//桌面支持配置空位【c_0003636】
 * @author cheyingkun
 */
public class EmtySeatEntity
{
	
	private long screen;
	private int cellX;
	private int cellY;
	
	public EmtySeatEntity(
			int screen ,
			int cellX ,
			int cellY )
	{
		this.screen = screen;
		this.cellX = cellX;
		this.cellY = cellY;
	}
	
	public long getScreen()
	{
		return screen;
	}
	
	public int getCellX()
	{
		return cellX;
	}
	
	public int getCellY()
	{
		return cellY;
	}
	
	@Override
	public String toString()
	{
		return StringUtils.concat( screen , "," , cellX , "," , cellY );
	}
	
	public static EmtySeatEntity stringToEmtySeatEntity(
			String str )
	{
		if( TextUtils.isEmpty( str ) )
		{
			return null;
		}
		String[] split = str.split( "," );
		if( split != null && split.length == 3 )
		{
			EmtySeatEntity mEmtySeatEntity;
			try
			{
				mEmtySeatEntity = new EmtySeatEntity( Integer.valueOf( split[0] ) , Integer.valueOf( split[1] ) , Integer.valueOf( split[2] ) );
				return mEmtySeatEntity;
			}
			catch( NumberFormatException e )
			{
				return null;
			}
		}
		return null;
	}
	//cheyingkun add end
}
