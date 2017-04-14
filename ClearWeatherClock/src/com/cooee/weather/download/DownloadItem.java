// xiatian add whole file //OperateFolder
package com.cooee.weather.download;


import android.graphics.Bitmap;


public class DownloadItem
{
	
	public String folderid;
	public String mPackageName;
	public String mTitle;
	public String mCNTitle;
	public String mTWTitle;
	public String mDefaultTitle;
	public Bitmap mIconBitmap;
	public String mIconUrl;
	//public String mDownloadPackageUrl;
	public String mBitmapPath;
	public String mResID;
	
	public DownloadItem(
			String folderid ,
			String mPackageName ,
			String mTitle ,
			String mCNTitle ,
			String mTWTitle ,
			String mDefaultTitle ,
			Bitmap mIconBitmap ,
			String mIconUrl ,
			String mBitmapPath ,
			String mResID )
	{
		this.folderid = folderid;
		this.mPackageName = mPackageName;
		this.mTitle = mTitle;
		this.mCNTitle = mCNTitle;
		this.mTWTitle = mTWTitle;
		this.mDefaultTitle = mDefaultTitle;
		this.mIconBitmap = mIconBitmap;
		this.mIconUrl = mIconUrl;
		//this.mDownloadPackageUrl = downloadPackageUrl;
		this.mBitmapPath = mBitmapPath;
		this.mResID = mResID;
	}
	
	public DownloadItem(
			DownloadItem item )
	{
		this.folderid = item.folderid;
		this.mPackageName = item.mPackageName;
		this.mTitle = item.mTitle;
		this.mCNTitle = item.mCNTitle;
		this.mTWTitle = item.mTWTitle;
		this.mDefaultTitle = item.mDefaultTitle;
		this.mIconBitmap = item.mIconBitmap;
		this.mIconUrl = item.mIconUrl;
		this.mBitmapPath = item.mBitmapPath;
		this.mResID = item.mResID;
	}
	
	public DownloadItem()
	{
		// TODO Auto-generated constructor stub
	}
}
