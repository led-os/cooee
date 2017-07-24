// xiatian add whole file //通知桌面切页（代码框架）。详见“INotifyLauncherSnapPageManager”中的备注。
package com.cooee.framework.function.NotifyLauncherSnapPage;


public interface INotifyLauncherSnapPageCallBack
{
	
	//通知桌面，向左切页
	public void notifyLauncherSnapToLeft();
	
	//通知桌面，向右切页
	public void notifyLauncherSnapToRight();
}
