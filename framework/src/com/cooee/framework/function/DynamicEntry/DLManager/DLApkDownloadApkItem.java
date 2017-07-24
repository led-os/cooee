package com.cooee.framework.function.DynamicEntry.DLManager;


import android.graphics.drawable.Drawable;
import cool.sdk.download.CoolDLCallback;


public class DLApkDownloadApkItem
{
	
	String PackageName;// key
	Drawable IconImgSrc;
	String AppName;
	Long CurSize;
	Long TotalSize;
	int DownLoadState; // 0:未在下载(none) 1:排队等待(wait) 2:正在下载(doing)
	CoolDLCallback DownloadCallback;
	boolean bNotifyDoingToLauncher;
	
	public DLApkDownloadApkItem()
	{
		// TODO Auto-generated constructor stub
		PackageName = null;
		IconImgSrc = null;
		AppName = null;
		CurSize = 0L;
		TotalSize = 0L;
		DownloadCallback = null;
		bNotifyDoingToLauncher = true;
	}
	
	public boolean getNotifyDoingToLauncher()
	{
		return bNotifyDoingToLauncher;
	}
	
	public void setNotifyDoingToLauncher(
			boolean bPermit )
	{
		bNotifyDoingToLauncher = bPermit;
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
	
	public CoolDLCallback getDownloadCallback()
	{
		return DownloadCallback;
	}
	
	public void setDownloadCallback(
			CoolDLCallback downloadCallback )
	{
		DownloadCallback = downloadCallback;
	}
}
