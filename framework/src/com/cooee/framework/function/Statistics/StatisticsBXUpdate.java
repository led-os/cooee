package com.cooee.framework.function.Statistics;


import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;
import cool.sdk.common.JsonUtil;
import cool.sdk.common.UrlUtil;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public class StatisticsBXUpdate extends UpdateHelper
{
	
	protected Context context;
	static UpdateConfig config;
	static String ACTION_BILLING_REQUEST = "3706";
	private static final String LIST_FLAG = "p1";
	static
	{
		config = new UpdateConfig();
		config.UPDATE_DEFAULT_MINUTES = 2 * 24 * 60;// 默认更新间隔
		config.UPDATE_MIN_MINUTES = 8 * 60;// 最小更新间隔
		config.UPDATE_MAX_MINUTES = 15 * 24 * 60;// 最大更新间隔
		config.MAX_UPDATE_TIMES_PER_DAY = 3;// 每天最大更新次数
		config.RETRY_TIMES_WHEN_ONLINE = 3;// 有网络下的重试次数
	};
	
	public StatisticsBXUpdate(
			Context context )
	{
		super( context , context.getPackageName() , config );
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	static StatisticsBXUpdate instance = null;
	
	public static StatisticsBXUpdate getInstance(
			Context context )
	{
		synchronized( StatisticsBXUpdate.class )
		{
			if( instance == null )
			{
				instance = new StatisticsBXUpdate( context );
			}
		}
		return instance;
	}
	
	private String getApkList()
	{
		return getString( LIST_FLAG , null );
	}
	
	@Override
	protected boolean OnUpdate(
			final Context context ) throws Exception
	{
		// TODO Auto-generated method stub
		JSONObject reqJson = JsonUtil.NewRequestJSON( context , StatisticsLog.h12 , context.getPackageName() );
		reqJson.put( "Action" , ACTION_BILLING_REQUEST );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "StatisticsUpdate rsp ACTION_BILLING_REQUEST:" , ACTION_BILLING_REQUEST ) );
		ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
		if( result.exception != null )
		{
			return false;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "StatisticsUpdate rsp，httpCode:" , result.httpCode , "-content:" , result.content ) );
		JSONObject resJson = new JSONObject( result.content );
		int retcode = resJson.getInt( "retcode" );
		if( retcode == 0 ) //有记录
		{
			if( resJson.has( "u1" ) )
			{
				setGapMinute( resJson.getInt( "u1" ) );
			}
			if( resJson.has( LIST_FLAG ) )
			{
				JSONArray list = resJson.getJSONArray( LIST_FLAG );
				// 服务器传递的数组为[],表示要清除数据
				if( list == null )
				{
					setValue( LIST_FLAG , "[]" );
				}
				else
				{
					setValue( LIST_FLAG , list.toString() );
				}
				StatisticsLog.LogBilling( context , list.toString() );
			}
		}
		else if( retcode == 100 ) //无更新
		{
			StatisticsLog.LogBilling( context , getApkList() );
		}
		return true;
	}
}
