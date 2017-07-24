package com.cooee.framework.function.DynamicEntry.DLManager;


import android.app.Notification;


public class DownloadingItem
{
	
	public String packageName;
	public String title;
	//public String downloadUrl;
	public int notifyID;
	public int state;//1:正在下载状�?2:暂停下载状�?3:下载完成;0:未在下载列表�?4:安装完成
	public int progress;
	public Notification notification;
	public String filePath;
	public DlCallback callback;
	public boolean popSale; //是否弹出过搭配销售
	public int productType;
	public int categoryID;
	public boolean isWifiReDownload;//是否是WIFI继续下载。
}
