package cool.sdk.FavoriteControl;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.ad.nearby.KmobAdMessage;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.news.data.NewsData;

import cool.sdk.common.CoolHttpClient;
import cool.sdk.common.CoolHttpClient.ResultEntity;
import cool.sdk.common.JsonUtil;
import cool.sdk.common.UrlUtil;
import cool.sdk.download.manager.DlMethod;
import cool.sdk.update.manager.UpdateConfig;
import cool.sdk.update.manager.UpdateHelper;


public abstract class FavoriteControlUpdate extends UpdateHelper
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
	public static final String NEWS_SOURCE = "newssource";//新闻的来源
	public static final String AD_PLACE = "adplace";//广告的位置
	public static final String ENABLE_NEWS = "en";//是否显示新闻流
	public static final String ENABLE_NEARBY = "enb";//是否显示附近
	public static final String NEARBY_UPDATE_TIME = "nearby_update_time";//附近广告更新时间间隔  分钟为单位
	
	protected FavoriteControlUpdate(
			Context context )
	{
		super( context , FavoriteControl.h13 , config );
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
		System.out.println( "zjp OnUpdate = " );
		if( !DlMethod.IsNetworkAvailable( context ) )
		{
			return false;
		}
		JSONObject reqJson = JsonUtil.NewRequestJSON( context.getApplicationContext() , FavoriteControl.h12 , FavoriteControl.h13 );//读取桌面中的appid、sn
		reqJson.put( "Action" , ACTION_CONFIG_REQUEST );
		reqJson.put( "p1" , FavoritesManager.getInstance().getContainerContext().getPackageName() );
		Log.v( "COOL" , "zjp FavoriteControlUpdate req:" + reqJson.toString() );
		ResultEntity result = CoolHttpClient.postEntity( UrlUtil.getDataServerUrl() , reqJson.toString() );
		if( result.exception != null )
		{
			Log.v( "COOL" , "zjp FavoriteControlUpdate rsp:(error)" + result.httpCode + " " + result.exception );
			return false;
		}
		Log.v( "COOL" , "zjp FavoriteControlUpdate rsp:" + result.httpCode + " " + result.content );
		JSONObject resJson = new JSONObject( result.content );
		int retcode = resJson.optInt( "rc0" );
		if( retcode == 0 )
		{
			config.UPDATE_DEFAULT_MINUTES = resJson.optLong( "c1" );
			setGapMinute( config.UPDATE_DEFAULT_MINUTES );//将更新时间间隔保存起来
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
			if( json.has( NEWS_SOURCE ) )
			{
				int newssource = json.getInt( NEWS_SOURCE );
				//cheyingkun add start	//运营关闭新闻后，再运营打开，新闻刷新不出来【i_0014803】
				if( newssource == NewsData.NEWS_FROM_DIANKU )
				{
					newssource = NewsData.NEWS_FROM_TOUTIAO;
				}
				//cheyingkun add end
				if( getInt( NEWS_SOURCE , -1 ) != newssource )
				{
					setValue( NEWS_SOURCE , newssource );
					if( FavoritesManager.getInstance() != null )
						FavoritesManager.getInstance().newsSourceChanged( newssource );
				}
			}
			if( json.has( AD_PLACE ) )
			{
				String adplace = json.getString( AD_PLACE );
				if( adplace != null && adplace != "" && !adplace.equals( getString( AD_PLACE , "" ) ) )
				{
					setValue( AD_PLACE , adplace );
					if( FavoritesManager.getInstance() != null )
						FavoritesManager.getInstance().adPlaceChanged( adplace );
				}
			}
			if( json.has( NEARBY_UPDATE_TIME ) )
			{
				String nearbyUpdateTime = json.getString( NEARBY_UPDATE_TIME );
				if( nearbyUpdateTime != null && nearbyUpdateTime != "" )
				{
					SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( FavoritesManager.getInstance().getContainerContext() );
					Editor editor = mSharedPreferences.edit();
					if( Integer.parseInt( nearbyUpdateTime ) <= 0 )
					{
						Log.v( "lvjiangbin" , "Don't do anything ,return . nearbyUpdateTime = " + nearbyUpdateTime );
						editor.putBoolean( KmobAdMessage.AD_GET_FIRST , false );
					}
					else
					{
						Log.v( "lvjiangbin" , "nearbyUpdateTime = " + nearbyUpdateTime );
						if( mSharedPreferences.getBoolean( KmobAdMessage.AD_GET_FIRST , true ) )
						{
							editor.putLong( KmobAdMessage.AD_GET_LAST_TIME_KEY , System.currentTimeMillis() );
						}
						editor.putLong( KmobAdMessage.AD_GET_DELAY_TIME_KEY , Integer.parseInt( nearbyUpdateTime ) * 60 * 1000 );
						editor.putBoolean( KmobAdMessage.AD_GET_FIRST , false );
					}
					editor.commit();
				}
			}
			if( json.has( ENABLE_NEWS ) )
			{
				boolean enablenews = json.getBoolean( ENABLE_NEWS );
				//xiatian add start	//确保老人桌面时，1、“新闻”模块不可运营，一直显示；2、“新闻类型”模块不可运营，一直显示为“我们自己的新闻”；3、“附近”模块模块不可运营，一直不显示。
				if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() ) )
				{//老人桌面时，“新闻”模块不可运营，一直显示。
					enablenews = true;
				}
				//xiatian add end
				setValue( ENABLE_NEWS , enablenews + "" );
				FavoritesManager.getInstance().notifyNewsChange( enablenews );
			}
			if( json.has( ENABLE_NEARBY ) )
			{
				boolean enbaleNearby = json.getBoolean( ENABLE_NEARBY );
				//xiatian add start	//确保老人桌面时，1、“新闻”模块不可运营，一直显示；2、“新闻类型”模块不可运营，一直显示为“我们自己的新闻”；3、“附近”模块模块不可运营，一直不显示。
				if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() ) )
				{//老人桌面时，“附近”模块模块不可运营，一直不显示。
					enbaleNearby = false;
				}
				//xiatian add end
				setValue( ENABLE_NEARBY , enbaleNearby + "" );
				FavoritesManager.getInstance().notifyNearbyChange( enbaleNearby );
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
}
