// xiatian add whole file //需求：添加“运营浏览器主页”的功能（从uni3移植过来）。
package com.cooee.framework.function.OperateExplorer;


import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


/**
 * 浏览器主页支持运营
 * @author jubingcheng
 */
public class OperateExplorer
{
	
	private static final String TAG = "OperateExplorer";
	private static IOperateExplorerCallbacks mOperateExplorerCallbacks = null;
	public final static String OPERATE_EXPLORER_ENABLE_KEY = "OperateExplorerEnable";
	public final static String OPERATE_EXPLORER_ENABLE_SIMPLE_KEY = "OEE";
	public final static String OPERATE_EXPLORER_HOME_WEBSITE_KEY = "OperateExplorerHomeWebsite";
	public final static String OPERATE_EXPLORER_HOME_WEBSITE_SIMPLE_KEY = "OEHW";
	
	public interface IOperateExplorerCallbacks
	{
		
		public void notifyExplorerSwitch(
				boolean enable ,
				String url );
	}
	
	public static void setCallbacks(
			IOperateExplorerCallbacks mCallbacks )
	{
		mOperateExplorerCallbacks = mCallbacks;
	}
	
	public static void notifyExplorerSwitch(
			boolean enable ,
			String url )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "notifyExplorerSwitch enable:" , enable , "-url:" , url ) );
		if( mOperateExplorerCallbacks == null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "notifyExplorerSwitch return:mOperateExplorerCallbacks == null" );
			return;
		}
		mOperateExplorerCallbacks.notifyExplorerSwitch( enable , url );
	}
}
