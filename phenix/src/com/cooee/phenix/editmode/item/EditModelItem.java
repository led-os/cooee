package com.cooee.phenix.editmode.item;


import android.content.Context;
import android.graphics.Bitmap;

import com.cooee.phenix.editmode.interfaces.IEditControlCallBack;


public abstract class EditModelItem
{
	
	private String mTitle = null;
	private String mKey = null;
	private String uMengKey;//cheyingkun add	//添加友盟统计自定义事件
	private String mPackageNameKey = null;//用来记住包名（主题、widget），壁纸则为壁纸的名字，这个属性用来保证唯一性
	private Bitmap mBitmap;
	private boolean isSelected = false;
	
	public String getKey()
	{
		return mKey;
	}
	
	public void setKey(
			String mKey )
	{
		this.mKey = mKey;
	}
	
	public String getTitle()
	{
		return mTitle;
	}
	
	public void setTitle(
			String title )
	{
		this.mTitle = title;
	}
	
	//cheyingkun add start	//添加友盟统计自定义事件
	public String getUMengKey()
	{
		return uMengKey;
	}
	
	public void setUMengKey(
			String uMengKey )
	{
		this.uMengKey = uMengKey;
	}
	
	//cheyingkun add end
	public String getPackageNameKey()
	{
		return mPackageNameKey;
	}
	
	public void setPackageNameKey(
			String mPackageNameKey )
	{
		this.mPackageNameKey = mPackageNameKey;
	}
	
	public void setBitmap(
			Bitmap bmp )
	{
		this.mBitmap = bmp;
	}
	
	public Bitmap getBitmap()
	{
		return mBitmap;
	}
	
	public boolean isSelected()
	{
		return isSelected;
	}
	
	public void setSelected(
			boolean isSelected )
	{
		this.isSelected = isSelected;
	}
	
	public abstract void onItemClick(
			IEditControlCallBack callback ,
			Context context );
}
