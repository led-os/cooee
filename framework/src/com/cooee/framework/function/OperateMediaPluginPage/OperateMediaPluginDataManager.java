/** gaominghui add whole file //需求：支持后台运营音乐页和相机页 */
package com.cooee.framework.function.OperateMediaPluginPage;


import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


/**
 * @author gaominghui 2017年6月26日
 * 用于获取服务器配置的专属页开关状态的管理类
 */
public class OperateMediaPluginDataManager
{
	
	private static final String TAG = "OperateMediaPluginDataManager";
	private static IOperateMediaPluginCallBack mOperateMedigPluginCallBack = null;
	public final static String OPERATE_MUSICPAGE_SWITCH_KEY = "OperateMusicPageSwitchEnable";
	public final static String OPERATE_CAMERAPAGE_SWITCH_KEY = "OperateCameraPageSwitchEnable";
	
	public interface IOperateMediaPluginCallBack
	{
		
		public void notifyCameraPageSwitch(
				boolean isShow );
		
		public void notifyMusicPageSwitch(
				boolean isShow );
	}
	
	public static void setCallbacks(
			IOperateMediaPluginCallBack mCallbacks )
	{
		mOperateMedigPluginCallBack = mCallbacks;
	}
	
	public static void notifyCameraPageSwitch(
			boolean isShow )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "OperateMusicPageSwitchKey - isShow:" , isShow ) );
		if( mOperateMedigPluginCallBack == null )
		{
			return;
		}
		mOperateMedigPluginCallBack.notifyCameraPageSwitch( isShow );
	}
	
	public static void notifyMusicPageSwitch(
			boolean isShow )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "OperateMusicPageSwitchKey - isShow:" , isShow ) );
		if( mOperateMedigPluginCallBack == null )
		{
			return;
		}
		mOperateMedigPluginCallBack.notifyMusicPageSwitch( isShow );
	}
}
