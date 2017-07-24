package com.cooee.framework.function.DynamicEntry;


import java.util.List;

import com.cooee.framework.function.DynamicEntry.DLManager.DownloadingItem;

import cool.sdk.SAManager.SACoolDLMgr.NotifyType;
import cool.sdk.download.manager.dl_info;


public interface OperateDynamicClient
{
	
	public boolean onDynamicDataChange();
	
	public void cancelDynamicUpdateWaiteDialog(
			boolean success );
	
	//wifi1118 start
	public void showWifiDownloadNotify(
			NotifyType type ,
			List<dl_info> dl_info_list );
	
	//wifi1118 end
	public boolean onCreateDynamicInfo(
			List<OperateDynamicData> list ,
			String operateVersion );
	
	public void upateDownloadItemState(
			String pkgName ,
			int state );
	
	public boolean showSaleSmartDownloadDialog(
			DownloadingItem dlItem );
}
