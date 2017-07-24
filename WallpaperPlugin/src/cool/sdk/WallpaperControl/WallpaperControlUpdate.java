package cool.sdk.WallpaperControl;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cooee.wallpaper.manager.ChangeWallpaperManager;

import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;
import cool.sdk.common.JsonUtil;
import cool.sdk.common.UrlUtil;
import cool.sdk.download.manager.DlMethod;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public abstract class WallpaperControlUpdate extends UpdateHelper
{
	
	protected static final String ACTION_CONFIG_REQUEST = "3705";
	private static UpdateConfig config;
	static
	{
		config = new UpdateConfig();
		config.UPDATE_DEFAULT_MINUTES = 3 * 24 * 60;// 默认更新间隔
		config.UPDATE_MIN_MINUTES = 8 * 60;// 最小更新间隔
		config.UPDATE_MAX_MINUTES = 15 * 24 * 60;// 最大更新间隔
		config.MAX_UPDATE_TIMES_PER_DAY = 3;// 每天最大更新次数
		config.RETRY_TIMES_WHEN_ONLINE = 3;//有网络下的重试次数
	};
	protected Context context;
	private String c6 = ""; //自定义拓展配置
	public static final String WALLPAPER_ADS = "wp_ad";//新闻的来源
	public static final String WALLPAPER_FROM = "wp_from";
	
	protected WallpaperControlUpdate(
			Context context )
	{
		super( context , WallpaperControl.h13 , config );
		// TODO Auto-generated constructor stub
		this.context = context;
		c6 = this.getString( "c6" , "" );
	}
	
	static int adhajd = 0;
	
	@Override
	protected boolean OnUpdate(
			Context context ) throws Exception
	{
		// TODO Auto-generated method stub
		if( !DlMethod.IsNetworkAvailable( context ) )
		{
			return false;
		}
		JSONObject reqJson = JsonUtil.NewRequestJSON( context.getApplicationContext() , WallpaperControl.h12 , WallpaperControl.h13 );//读取桌面中的appid、sn
		reqJson.put( "Action" , ACTION_CONFIG_REQUEST );
		reqJson.put( "p1" , context.getPackageName() );
		Log.v( "COOL" , " WallpaperControlUpdate req:" + reqJson.toString() );
		ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
		if( result.exception != null )
		{
			Log.v( "COOL" , "zjp WallpaperControlUpdate rsp:(error)" + result.httpCode + " " + result.exception );
			return false;
		}
		Log.v( "COOL" , "zjp WallpaperControlUpdate rsp:" + result.httpCode + " " + result.content );
		JSONObject resJson = new JSONObject( result.content );
		int retcode = resJson.optInt( "rc0" );
		if( retcode == 0 )
		{
			config.UPDATE_DEFAULT_MINUTES = resJson.optLong( "c1" );
			setGapMinute( config.UPDATE_DEFAULT_MINUTES );
			c6 = resJson.optString( "c6" , "" );
			notifySpreadSwitch();
			setValue( "c6" , c6 );
			setValue( "result.content" , result.content );
			return true;
		}
		else if( retcode == 200 )
		{
			c6 = "";
			notifySpreadSwitch();
			setValue( "c6" , c6 );
			setValue( "result.content" , "" );
			return true;
		}
		else if( retcode == 100 )
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void notifySpreadSwitch()
	{
		try
		{
			JSONObject json = new JSONObject( c6 );
			if( json.has( WALLPAPER_ADS ) )
			{
				ChangeWallpaperManager.SWITCH_ENABEL_ADS_ONLINE = json.getBoolean( WALLPAPER_ADS );
				setValue( WALLPAPER_ADS , ChangeWallpaperManager.SWITCH_ENABEL_ADS_ONLINE + "" );
			}
			if( json.has( WALLPAPER_FROM ) )
			{
				int from = json.getInt( WALLPAPER_FROM );
				if( ChangeWallpaperManager.Online_wallpaper_from != from )
				{
					ChangeWallpaperManager.Online_wallpaper_from = json.getInt( WALLPAPER_FROM );
					setValue( WALLPAPER_FROM , ChangeWallpaperManager.Online_wallpaper_from );
					SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences( context );
					prefer.edit().putString( "wallpaperListDate" , "" ).commit();//清除掉时间，下次重新获取列表
				}
			}
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
	}
	
	public String getC6()
	{
		return getString( "c6" , null );
	}
	
	public boolean enabelAdS()
	{
		String temp = getString( WALLPAPER_ADS , null );
		boolean enable_ads = false;
		if( temp != null )
		{
			enable_ads = temp.equals( "true" );
		}
		return enable_ads;
	}
	
	public int getWallpaperFrom(
			int defaultValue )
	{
		return getInt( WALLPAPER_FROM , defaultValue );
	}
}
