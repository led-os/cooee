package com.cooee.favorites.manager;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.utils.Assets;
import com.kmob.kmobsdk.KmobManager;


public class AdPlaceIdManager
{
	
	private static AdPlaceIdManager instance = null;
	private static String newsId[] = new String[2];
	private static String nearbyId[] = new String[4];
	private static String[][] idAll = {
			//    {"新闻流广告一级界面","新闻流广告二级界面","服务模块1","服务模块2","服务模块3","服务模块4","酷生活一级界面开屏广告" /*//fulijuan add    //需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）*/} 
			//Uni4桌面
			{ "20160106050117461" , "20160504100502461" , "20160504100517461" , "20160504100528461" , "20160504100537461" , "20160504100545461" } ,
			//Uni3桌面
			{ "20160308010331281" , "20160504100545281" , "20160504100511281" , "20160504100521281" , "20160504100532281" , "20160504100541281" , "20170421141434281"/*//fulijuan add    //需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）*/},	
			//老人桌面
			{ "20160309020336505" , "20160504090541505" , "20160504090534505" , "20160504090545505" , "20160504090556505" , "20160504090507505" } ,
			//Phinex桌面
			{ "20160322110357441" , "20160504090541441" , "20160504090505441" , "20160504090515441" , "20160504090523441" , "20160504090532441" , "20161124154808441"/*//fulijuan add    //需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）*/},
			//launcher3
			{ "20160322110320506" , "20160504090550506" , "20160427010408506" , "20160428020430506" , "20160428020452506" , "20160428020417506" } ,
			//s5桌面
			{ "20160504100506543" , "20160504100525543" , "20160504100558543" , "20160504100515543" , "20160504100532543" , "20160504100545543" } };
	private static String demoAdWhenOnShowId = null;//fulijuan add 		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
	public static AdPlaceIdManager getInstance()
	{
		if( instance == null )
		{
			instance = new AdPlaceIdManager();
			initAdId();
		}
		return instance;
	}
	
	private static void initAdId()
	{
		String id = getAppId( FavoritesManager.getInstance().getContainerContext() );
		KmobManager.setContext( FavoritesManager.getInstance().getContainerContext() ); //gaominghui add kmob初始化context
		KmobManager.setAppId( id );
		//		onPageFinishedLoading();
		//-1屏没有activity了，此处需要sdk进行修改
		//		KmobMessage.getKmobMessage( mHandler , service , KmobMessage.adPlaceId );
		int index = 0;
		if( id.equals( "461" ) )
		{
			index = 0;//UNI4桌面
		}
		else if( id.equals( "281" ) )
		{
			index = 1;//UNI3桌面
		}
		else if( id.equals( "505" ) )
		{
			index = 2;//老人桌面
		}
		//cheyingkun add start	//添加phenix1.1稳定版app_id
		else if( id.equals( "441" ) )
		{
			index = 3;//phenix1.1稳定版
		}
		//cheyingkun add end
		else if( id.equals( "506" ) )
		{
			index = 4;//酷生活移植包
		}
		else if( id.equals( "543" ) )
		{
			index = 5;//s5桌面
		}
		try
		{
			JSONObject tmp = Assets.config;
			JSONObject config = tmp.getJSONObject( "config" );
			KmobManager.setChannel( config.getString( "serialno" ) );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String nearbyIdTemp = FavoritesManager.getInstance().getConfig().getString( FavoriteConfigString.getNearbyAdPlaceIdKey() , FavoriteConfigString.getNearbyAdPlaceIdValue() );
		String[] nearbyIdArray = null;
		if( nearbyIdTemp != null && !"".equals( nearbyIdTemp ) )
		{
			nearbyIdArray = nearbyIdTemp.split( "/" );
		}
		for( int i = 0 ; i < idAll[index].length ; i++ )
		{
			if( i < 2 )
			{
				newsId[i] = idAll[index][i];
			}
			else
			{	
				if( i < 6 )//fulijuan add	//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
				{
					
					if( nearbyIdArray != null )
					{
						if( nearbyIdArray[i - 2] != null )
						{
						idAll[index][i] = nearbyIdArray[i - 2];
						Log.v( "lvjiangbin" , "nearbyIdArray[" + ( i - 2 ) + "]=" + nearbyIdArray[i - 2] );
						}
					}
				nearbyId[i - 2] = idAll[index][i];
				}
				//fulijuan add start		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
				else if(i == 6) 
				{ 
					demoAdWhenOnShowId = idAll[index][i];
				}
				//fulijuan add end
			}
		}
	}

	public String[] getNewsAdId()
	{
		return newsId;
	}
	
	public String[] getNearbyAdId()
	{
		return nearbyId;
	}
	
	private static String getAppId(
			Context context )
	{
		try
		{
			String key = "KMobAd_APP_ID";
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo( context.getPackageName() , PackageManager.GET_META_DATA );
			if( appInfo.metaData.containsKey( key ) )
			{
				Object msgKey = appInfo.metaData.get( key );
				String msg = "";
				if( msgKey instanceof Integer )
				{
					msg = appInfo.metaData.getInt( key ) + "";
				}
				else if( msgKey instanceof String )
				{
					msg = appInfo.metaData.getString( key );
				}
				return msg;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		Log.i( KmobManager.LOGTAG , "APPId getAdAppId " );
		return "461";// manifest中没有注册则默认使用的是uni4的广告位
	}
	
	//fulijuan add start		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
	public String getDemoAdWhenOnShowId()
	{
		return demoAdWhenOnShowId;
	}
	//fulijuan add end

}
