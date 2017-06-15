package com.iLoong.launcher.MList;


import com.iLoong.launcher.MList.ApkMangerActivity.DownLoadCallBack;

import cool.sdk.download.CoolDLCallback;
import android.graphics.drawable.Drawable;


public class DownloadApkItem
{
	
	String PackageName;// key
	Drawable IconImgSrc;
	String AppName;
	Long CurSize;
	Long TotalSize;
	int DownLoadState; // 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
	DownLoadCallBack downloadCallback;
	
	public DownloadApkItem()
	{
		// TODO Auto-generated constructor stub
		PackageName = null;
		Drawable IconImgSrc = null;
		String AppName = null;
		Long CurSize = 0L;
		Long TotalSize = 0L;
		downloadCallback = null;
	}
	
	public String getPackageName()
	{
		return PackageName;
	}
	
	public void setPackageName(
			String packageName )
	{
		PackageName = packageName;
	}
	
	public Drawable getIconImgSrc()
	{
		return IconImgSrc;
	}
	
	public void setIconImgSrc(
			Drawable iconImgSrc )
	{
		IconImgSrc = iconImgSrc;
	}
	
	public String getAppName()
	{
		return AppName;
	}
	
	public void setAppName(
			String appName )
	{
		AppName = appName;
	}
	
	public Long getCurSize()
	{
		return CurSize;
	}
	
	public void setCurSize(
			Long curSize )
	{
		CurSize = curSize;
	}
	
	public Long getTotalSize()
	{
		return TotalSize;
	}
	
	public void setTotalSize(
			Long totalSize )
	{
		TotalSize = totalSize;
	}
	
	public int getDownLoadState()
	{
		return DownLoadState;
	}
	
	public void setDownLoadState(
			int downLoadState )
	{
		DownLoadState = downLoadState;
	}
	
	public DownLoadCallBack getDownloadCallback()
	{
		return downloadCallback;
	}
	
	public void setDownloadCallback(
			DownLoadCallBack downloadCallback )
	{
		this.downloadCallback = downloadCallback;
	}
}
