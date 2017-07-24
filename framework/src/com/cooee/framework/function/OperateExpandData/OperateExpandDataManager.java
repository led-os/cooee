package com.cooee.framework.function.OperateExpandData;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


// cheyinkgun add whole file //文件夹推荐应用读取服务器配置(开关、wifi更新、更新间隔)
/**运营扩展配置数据管理类*/
public class OperateExpandDataManager
{
	
	private static final boolean DEBUG = false;
	private static final String TAG = "OperateExpandDataManager";
	private static final String OPERATE_EXPAND_DATA_LIST = "operateExpandDataList";
	
	/**解析服务器传来的字符串信息,并更新*/
	public static void notifyOperateExpandDataManager(
			String string )
	{
		if( DEBUG )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "OperateExpandDataManager notifyOperateExpandDataManager - string:" , string ) );
		}
		try
		{
			//拿到json数据
			JSONObject json = new JSONObject( string );
			//拿到成json数组
			JSONArray jsonArray = json.getJSONArray( OPERATE_EXPAND_DATA_LIST );
			//循环json数组,判断每种情况的处理方式(目前只有文件夹推荐应用的json)
			for( int i = 0 ; i < jsonArray.length() ; i++ )
			{
				JSONObject object = jsonArray.getJSONObject( i );
				updateOperateExpandData( object );
			}
		}
		catch( JSONException e )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( "OperateExpandDataManager notifyOperateExpandDataManager:" , e.toString() ) );
		}
	}
	
	/**更新运营扩展数据*/
	private static void updateOperateExpandData(
			JSONObject object )
	{
		if( object != null )
		{
			try
			{
				//是否是文件夹推荐应用的json
				String string = object.getString( OperateNativeData.TAG );
				if( !TextUtils.isEmpty( string ) )
				{
					OperateNativeData.notifyOperateNativeDataChanged( string );
				}
			}
			catch( JSONException e )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "OperateExpandDataManager updateOperateExpandData:" , e.toString() ) );
			}
		}
	}
}
