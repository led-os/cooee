package com.iLoong.launcher.MList;


import android.graphics.drawable.Drawable;


public class InstallApkItemEx
{
	
	String PackageName;// key
	Drawable IconImgSrc;
	String AppName;
	Long appSize;
	String AppVersion;
	int InstallState; /* 0:初始化状态 1:安装 2:启动 */
	
	public int getInstallState()
	{
		return InstallState;
	}
	
	public void setInstallState(
			int installState )
	{
		InstallState = installState;
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
	
	public String getAppVersion()
	{
		return AppVersion;
	}
	
	public void setAppVersion(
			String appVersion )
	{
		AppVersion = appVersion;
	}
	
	public Long getAppSize()
	{
		return appSize;
	}
	
	public void setAppSize(
			Long appSize )
	{
		this.appSize = appSize;
	}
}
