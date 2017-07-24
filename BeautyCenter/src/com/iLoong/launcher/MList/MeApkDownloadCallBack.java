package com.iLoong.launcher.MList;


import cool.sdk.download.manager.dl_info;


public abstract class MeApkDownloadCallBack
{
	
	MeApkDLShowType ShowType = MeApkDLShowType.None;
	
	public MeApkDownloadCallBack(
			MeApkDLShowType ShowType )
	{
		// TODO Auto-generated constructor stub
		this.ShowType = ShowType;
	}
	
	abstract void onDoing(
			String PackageName ,
			dl_info info );
	
	abstract void onSuccess(
			String PackageName ,
			dl_info info );
	
	abstract void onFail(
			String PackageName ,
			dl_info info );
	
	abstract void onRestart(
			String PackageName );
	
	abstract void onStop(
			String PackageName );
}
