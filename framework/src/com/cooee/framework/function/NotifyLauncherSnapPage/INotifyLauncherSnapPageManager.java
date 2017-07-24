// xiatian add whole file //通知桌面切页（代码框架）。详见“INotifyLauncherSnapPageManager”中的备注。
package com.cooee.framework.function.NotifyLauncherSnapPage;


// 【备注】
// 1、现在有不少客户使用“光感切页”和“指纹切页”，桌面需要针对不同的【对接方式】，进行不同的处理。
// 2、现在汇总整合成为一个接口，以适配不同的对接方式。
public interface INotifyLauncherSnapPageManager
{
	
	//是否支持该功能，即开关是否打开
	public boolean isEnable();
	
	//注册“传感器”【由于不同的“光感传感器”和“指纹传感器”的对接方法各不相同，需要桌面在特定的情况下做相应的特殊处理】
	public void register(
			Object mObject );
	
	//注销“传感器”【由于不同的“光感传感器”和“指纹传感器”的对接方法各不相同，需要桌面在特定的情况下做相应的特殊处理】
	public void unRegister(
			Object mObject );
	
	//设置回调
	public void setCallBack(
			INotifyLauncherSnapPageCallBack mINotifyLauncherSnapPageCallBack );
	
	//是否向左切页
	public boolean isSnapToLeft(
			Object mObject );
	
	//是否向右切页
	public boolean isSnapToRight(
			Object mObject );
	
	//是否需要向左切页
	public boolean isNeedSnapToLeft(
			Object mObject );
	
	//是否需要向右切页
	public boolean isNeedSnapToRight(
			Object mObject );
	
	//通知桌面，向左切页
	public boolean notifyLauncherSnapToLeft();
	
	//通知桌面，向右切页
	public boolean notifyLauncherSnapToRight();
}
