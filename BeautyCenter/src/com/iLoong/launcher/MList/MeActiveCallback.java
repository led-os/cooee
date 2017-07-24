package com.iLoong.launcher.MList;


public interface MeActiveCallback
{
	
	void NoifySatrtAction(
			String pkgName );
	
	void NotifyDelAction(
			String pkgName );
	
	void NotifyInstallSucessAction(
			String pkgName );
	
	void NotifyUninstallApkAction(
			String pkgName );
}
