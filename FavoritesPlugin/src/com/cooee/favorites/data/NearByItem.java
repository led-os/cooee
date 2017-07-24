package com.cooee.favorites.data;


import java.util.ArrayList;

import android.graphics.Bitmap;


public class NearByItem
{
	
	private String mTitle;
	private String mTitlezhrCN = "";
	private String mTitlezhrTW = "";
	private String mUrl = "";
	private String mCmp = "";
	private Bitmap mBmp = null;
	private ArrayList<String> extra = null;
	
	public ArrayList<String> getExtra()
	{
		return extra;
	}
	
	public void setExtra(
			ArrayList<String> extra )
	{
		this.extra = extra;
	}
	
	public Bitmap getBmp()
	{
		return mBmp;
	}
	
	public void setBmp(
			Bitmap bmp )
	{
		this.mBmp = bmp;
	}
	
	public String getUrl()
	{
		return mUrl;
	}
	
	public void setUrl(
			String url )
	{
		this.mUrl = url;
	}
	
	public String getCmp()
	{
		return mCmp;
	}
	
	public void setCmp(
			String cmp )
	{
		this.mCmp = cmp;
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
	
	public String getTitlezhrCN()
	{
		return mTitlezhrCN;
	}
	
	public void setTitlezhrCN(
			String titlezhrCN )
	{
		this.mTitlezhrCN = titlezhrCN;
	}
	
	public String getTitlezhrTW()
	{
		return mTitlezhrTW;
	}
	
	public void setTitlezhrTW(
			String titlezhrTW )
	{
		this.mTitlezhrTW = titlezhrTW;
	}
}
