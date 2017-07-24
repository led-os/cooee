package com.cooee.framework.function.OperateExpandData;


import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


// cheyingkun add whole file //文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
/**服务器运营原生广告配置数据管理类*/
public class OperateNativeData
{
	
	public static final String TAG = "operateNativeData";
	private static IOperateNativeDataCallbacks mOperateNativeDataCallbacks = null;
	
	public interface IOperateNativeDataCallbacks
	{
		
		public void notifyOperateNativeDataChanged(
				String string );
	}
	
	public static void setCallbacks(
			IOperateNativeDataCallbacks mCallbacks )
	{
		mOperateNativeDataCallbacks = mCallbacks;
	}
	
	/**文件夹推荐应用,运营数据发送改变*/
	public static void notifyOperateNativeDataChanged(
			String string )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "notifyOperateNativeDataChanged - string:" , string ) );
		if( mOperateNativeDataCallbacks == null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , "notifyOperateNativeDataChanged - return[( mOperateNativeDataCallbacks == null )]" );
			return;
		}
		mOperateNativeDataCallbacks.notifyOperateNativeDataChanged( string );
	}
}
