// xiatian add whole file //运营友盟（详见“OperateUmeng”中说明）
package com.cooee.framework.function.OperateUmeng;


import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class OperateUmeng
{
	
	//【说明】
	//	现在桌面使用了友盟统计的相关代码，为防止友盟代码的不可控性，需要加大对友盟的控制力度。故通过服务器开关来通知桌面是否使用友盟统计的相关代码。
	private static final String TAG = "OperateUmeng";
	private static IOperateUmengCallbacks mOperateUmengCallbacks = null;
	public final static String OPERATE_UMENG_NEED_ENABLE_UMENG_SWITCH_KEY = "OperateUmengNeedEnableUmengSwitch";
	
	public interface IOperateUmengCallbacks
	{
		
		public void notifyUmengSwitch(
				boolean isShow );
	}
	
	public static void setCallbacks(
			IOperateUmengCallbacks mCallbacks )
	{
		mOperateUmengCallbacks = mCallbacks;
	}
	
	public static void notifyUmengSwitch(
			boolean isShow )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "notifyUmengSwitch - isShow:" , isShow ) );
		if( mOperateUmengCallbacks == null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "notifyUmengSwitch - return[( mOperateUmengCallbacks == null )]" );
			return;
		}
		mOperateUmengCallbacks.notifyUmengSwitch( isShow );
	}
}
