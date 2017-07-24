/***/
package com.cooee.phenix.kmob.ad;


import java.sql.Date;
import java.util.Map;

import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;

import cool.sdk.KmobConfig.KmobAdPlaceIDConfig;
import cool.sdk.KmobConfig.KmobConfigData;


/**
 * @author gaominghui 2016年5月20日
 */
public class KmobUtil
{
	
	public static final String TAG = "KmobUtil";
	public static KmobUtil instance = null;
	
	public static KmobUtil getInstance()
	{
		if( instance == null )
		{
			instance = new KmobUtil();
		}
		return instance;
	}
	
	/**
	 * 
	 */
	protected KmobUtil()
	{
	}
	
	/**
	 * 
	 *
	 * @param adPlaceID 广告位id
	 * @param hasShows 该广告位已经展示的次数
	 * @return 是否可以请求广告
	 * @author gaominghui 2016年5月20日
	 */
	private KmobAdPlaceIDConfig mAdPlaceIdConfig = null;
	
	public boolean enableRequestAd(
			String adPlaceID ,
			long hasShows ,
			long lastRequestTime )
	{
		boolean enableRequestAd = false;
		requestAdConfig( adPlaceID );
		if( KmobConfigData.getInstance().isC0() == 0 )
		{
			return enableRequestAd;
		}
		long requestGap = ( System.currentTimeMillis() - lastRequestTime ) / ( 1000 * 60 );//这次请求与上次请求的时间间隔（分钟）
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.w( TAG , StringUtils.concat( "requestGap = " , requestGap , "-mAdPlaceIdConfig.isOn()=" , mAdPlaceIdConfig.isOn() , "-mAdPlaceIdConfig.getReqGap()=" , mAdPlaceIdConfig.getReqGap() ) );
		if( mAdPlaceIdConfig != null && mAdPlaceIdConfig.isOn() && requestGap > mAdPlaceIdConfig.getReqGap() && enableShowAd( adPlaceID , hasShows ) )
		{
			enableRequestAd = true;
		}
		return enableRequestAd;
	}
	
	/**
	 *解析广告配置信息
	 * @param adPlaceID
	 * @author gaominghui 2016年5月20日
	 */
	private void requestAdConfig(
			String adPlaceID )
	{
		switch( KmobConfigData.getInstance().isC0() )
		{
			case 1:
				mAdPlaceIdConfig = KmobConfigData.getInstance().isC3();
				break;
			case 2:
				Map<String , KmobAdPlaceIDConfig> map = KmobConfigData.getInstance().isC4();
				if( map.containsKey( adPlaceID ) )
				{
					mAdPlaceIdConfig = map.get( adPlaceID );
				}
				else
				{
					mAdPlaceIdConfig = KmobConfigData.getInstance().isC3();
				}
				break;
			default:
				mAdPlaceIdConfig = KmobConfigData.getInstance().isC3();
				break;
		}
	}
	
	public boolean enableShowAd(
			String adPlaceId ,
			long hasShows )
	{
		boolean enableShowAd = false;
		requestAdConfig( adPlaceId );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "hasShows = " , hasShows , "; shows = " , mAdPlaceIdConfig.getShows() , ";KmobConfigData.getInstance().isC0() = " , KmobConfigData.getInstance().isC0() ) );
		if( KmobConfigData.getInstance().isC0() == 0 || ( mAdPlaceIdConfig != null && !mAdPlaceIdConfig.isOn() ) || hasShows >= mAdPlaceIdConfig.getShows() )
		{
			return enableShowAd;
		}
		enableShowAd = parseAdShowTime();
		return enableShowAd;
	}
	
	public long enableShowTimes(
			String adPlaceId )
	{
		long enableShowTimes = 0;
		if( adPlaceId != null )
		{
			requestAdConfig( adPlaceId );
		}
		if( mAdPlaceIdConfig != null )
		{
			enableShowTimes = mAdPlaceIdConfig.getShows();
		}
		return enableShowTimes;
	}
	
	/**
	 *
	 * @param enableShowAd
	 * @return
	 * @author gaominghui 2016年5月25日
	 */
	private boolean parseAdShowTime()
	{
		boolean res = false;
		String showTime = mAdPlaceIdConfig.getShowtime();
		String[] s1 = showTime.split( "," );
		String[] s2 = null;
		for( int i = 0 ; i < s1.length ; i++ )
		{
			s2 = s1[i].split( "-" );
			s2[0] = getHour( s2[0] );
			long currentTime = System.currentTimeMillis();
			Date date = new Date( currentTime );
			String hour = String.format( "%tH" , date );
			long currentHour = Long.valueOf( hour );
			long startTime = Long.valueOf( s2[0] );
			long endTime = 0;
			if( s2.length > 1 )
			{
				s2[1] = getHour( s2[1] );
				endTime = Long.valueOf( s2[1] );
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "startTime = " , startTime , ";currentHour =  " , currentHour , "; endTime = " , endTime ) );
			if( startTime <= currentHour && currentHour < endTime )
			{
				res = true;
				return res;
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "enableShowAd =  " , res ) );
		}
		return res;
	}
	
	/**
	 *
	 * @param s2
	 * @author gaominghui 2016年5月23日
	 */
	private String getHour(
			String s )
	{
		if( s.contains( ":" ) )
		{
			s = s.split( ":" )[0];
		}
		return s;
	}
}
