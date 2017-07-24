package com.cooee.phenix.musicpage.entity;


// MusicPage
import android.graphics.drawable.BitmapDrawable;


public class BottomButtonInfo
{
	
	public BottomButtonInfo(
			BitmapDrawable icon ,
			String packName ,
			String className )
	{
		this.icon = icon;
		this.packName = packName;
		this.className = className;
	}
	
	private BitmapDrawable icon = null;
	private String packName = null;
	private String className = null;
	
	public BitmapDrawable getIcon()
	{
		return icon;
	}
	
	public String getPackName()
	{
		return packName;
	}
	
	public String getClassName()
	{
		return className;
	}
}
