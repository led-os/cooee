package com.cooee.favorites.ad.news;


import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.FavoritesPlugin;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.news.data.NewsData;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.log.CoolLog;


public class KmobNewsMessage
{
	
	private static final String TAG = "KmobMessage";
	private static String adPlaceId = "20160106050117461";
	public static final int sums = 10;
	private NewsData mNewsData;
	private boolean isGetting = false;
	private Handler mHandler;
	private CoolLog Log;
	/**
	* 原生广告
	*/
	private AdBaseView mNativeView = null;
	private AdBaseView mCustomNativeView = null;
	
	public KmobNewsMessage(
			NewsData data )
	{
		mNewsData = data;
		mHandler = new Handler( FavoritesManager.getInstance().getContainerContext().getMainLooper() );
	}
	
	public void getKmobMessage(
			final Context context )
	{
		// TODO Auto-generated method stub
		if( isGetting )
		{
			return;
		}
		if( Log == null )
			Log = new CoolLog( context );
		isGetting = true;
		mHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( mNativeView != null )
				{
					mNativeView.onDestroy();
				}
				Log.v( "zjp" , "adPlaceId = " + adPlaceId );
				KmobManager.setContext( context.getApplicationContext() ); //gaominghui add kmob初始化context
				KmobManager.setChannel( FavoritesManager.getInstance().getSn() );//cheyingkun add	//设置广告sn
				if( mCustomNativeView != null )
				{
					mCustomNativeView.onDestroy();
				}
				String newsId = FavoritesManager.getInstance().getConfig().getString( FavoriteConfigString.getNewsAdPlaceIdKey() , FavoriteConfigString.getNewsAdPlaceIdValue() );
				if( !TextUtils.isEmpty( newsId ) )
				{
					Log.v( TAG , "newsId = " + newsId );
					mCustomNativeView = KmobManager.createNative( newsId , FavoritesManager.getInstance().getContainerContext() , 1 );
					mCustomNativeView.addAdViewListener( new AdViewListener() {
						
						@Override
						public void onAdShow(
								String arg0 )
						{
							Log.v( TAG , "NativeAdActivity onAdShow info: " + arg0 );
						}
						
						@Override
						public void onAdReady(
								String arg0 )
						{
							Log.v( TAG , "onAdReady arg0:" + arg0 );
							if( arg0 != null )//NativeAdData
							{
								try
								{
									if( mNewsData != null )
									{
										JSONArray array = new JSONArray( arg0 );
										mNewsData.updateCustomAdData( array );
									}
								}
								catch( JSONException e )
								{
									// TODO Auto-generated catch block
									try
									{
										JSONObject obj = new JSONObject( arg0 );//后台返回的只有1个广告时，返回的是jasonobject，不是jasonarray
										JSONArray array = new JSONArray();
										array.put( obj );
										if( mNewsData != null )
										{
											mNewsData.updateCustomAdData( array );
										}
									}
									catch( JSONException e1 )
									{
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}
						}
						
						@Override
						public void onAdFailed(
								String arg0 )
						{
							Log.v( TAG , "NativeAdActivity onAdFailed info: " + arg0 );
						}
						
						@Override
						public void onAdClose(
								String arg0 )
						{
							Log.v( TAG , "NativeAdActivity onAdClose info: " + arg0 );
						}
						
						@Override
						public void onAdClick(
								String arg0 )
						{
							Log.v( TAG , "NativeAdActivity onAdClick info: " + arg0 );
						}
						
						@Override
						public void onAdCancel(
								String arg0 )
						{
							Log.v( TAG , "NativeAdActivity onAdCancel info: " + arg0 );
						}
					} );
				}
				mNativeView = KmobManager.createNative( adPlaceId , FavoritesManager.getInstance().getContainerContext() , sums );
				FavoritesConfig config = FavoritesManager.getInstance().getConfig();
				if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
				{
					MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "Ads_asking" );
				}
				try
				{
					StatisticsExpandNew.onCustomEvent(
							FavoritesManager.getInstance().getContainerContext() ,
							"Ads_asking" ,
							FavoritesPlugin.SN ,
							FavoritesPlugin.APPID ,
							CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName ,
							FavoritesPlugin.UPLOAD_VERSION + "" ,
							null );
				}
				catch( NoSuchMethodError e )
				{
					try
					{
						StatisticsExpandNew.onCustomEvent(
								FavoritesManager.getInstance().getContainerContext() ,
								"Ads_asking" ,
								FavoritesPlugin.SN ,
								FavoritesPlugin.APPID ,
								CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName );
					}
					catch( NoSuchMethodError e1 )
					{
						StatisticsExpandNew.onCustomEvent( FavoritesManager.getInstance().getContainerContext() , "Ads_asking" , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName );
					}
				}
				mNativeView.addAdViewListener( new AdViewListener() {
					
					@Override
					public void onAdShow(
							String arg0 )
					{
						Log.v( TAG , "NativeAdActivity onAdShow info: " + arg0 );
					}
					
					@Override
					public void onAdReady(
							String arg0 )
					{
						Log.v( TAG , "onAdReady arg0:" + arg0 );
						if( arg0 != null )//NativeAdData
						{
							try
							{
								if( mNewsData != null )
								{
									JSONArray array = new JSONArray( arg0 );
									mNewsData.updateAdData( array );
									Map<String , String> map = new HashMap<String , String>();
									map.put( "count" , array.length() + "" );
									FavoritesConfig config = FavoritesManager.getInstance().getConfig();
									if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
									{
										MobclickAgent.onEvent( context , "Ads_asking_success" , map );
									}
									try
									{
										StatisticsExpandNew.onCustomEvent(
												FavoritesManager.getInstance().getContainerContext() ,
												"Ads_asking_success" ,
												FavoritesPlugin.SN ,
												FavoritesPlugin.APPID ,
												CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
												FavoritesPlugin.PRODUCTTYPE ,
												FavoritesPlugin.PluginPackageName ,
												FavoritesPlugin.UPLOAD_VERSION + "" ,
												new JSONObject( map ) );
									}
									catch( NoSuchMethodError e )
									{
										try
										{
											StatisticsExpandNew.onCustomEvent(
													FavoritesManager.getInstance().getContainerContext() ,
													"Ads_asking_success" ,
													FavoritesPlugin.SN ,
													FavoritesPlugin.APPID ,
													CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
													FavoritesPlugin.PRODUCTTYPE ,
													FavoritesPlugin.PluginPackageName ,
													new JSONObject( map ) );
										}
										catch( NoSuchMethodError e1 )
										{
											StatisticsExpandNew.onCustomEvent(
													FavoritesManager.getInstance().getContainerContext() ,
													"Ads_asking_success" ,
													FavoritesPlugin.PRODUCTTYPE ,
													FavoritesPlugin.PluginPackageName ,
													new JSONObject( map ) );
										}
									}
								}
							}
							catch( JSONException e )
							{
								// TODO Auto-generated catch block
								try
								{
									JSONObject obj = new JSONObject( arg0 );//后台返回的只有1个广告时，返回的是jasonobject，不是jasonarray
									JSONArray array = new JSONArray();
									array.put( obj );
									if( mNewsData != null )
									{
										mNewsData.updateAdData( array );
									}
									Map<String , String> map = new HashMap<String , String>();
									map.put( "count" , 1 + "" );
									FavoritesConfig config = FavoritesManager.getInstance().getConfig();
									if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
									{
										MobclickAgent.onEvent( context , "Ads_asking_success" , map );
									}
									try
									{
										StatisticsExpandNew.onCustomEvent(
												FavoritesManager.getInstance().getContainerContext() ,
												"Ads_asking_success" ,
												FavoritesPlugin.SN ,
												FavoritesPlugin.APPID ,
												CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
												FavoritesPlugin.PRODUCTTYPE ,
												FavoritesPlugin.PluginPackageName ,
												FavoritesPlugin.UPLOAD_VERSION + "" ,
												new JSONObject( map ) );
									}
									catch( NoSuchMethodError e1 )
									{
										try
										{
											StatisticsExpandNew.onCustomEvent(
													FavoritesManager.getInstance().getContainerContext() ,
													"Ads_asking_success" ,
													FavoritesPlugin.SN ,
													FavoritesPlugin.APPID ,
													CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
													FavoritesPlugin.PRODUCTTYPE ,
													FavoritesPlugin.PluginPackageName ,
													new JSONObject( map ) );
										}
										catch( NoSuchMethodError e2 )
										{
											StatisticsExpandNew.onCustomEvent(
													FavoritesManager.getInstance().getContainerContext() ,
													"Ads_asking_success" ,
													FavoritesPlugin.PRODUCTTYPE ,
													FavoritesPlugin.PluginPackageName ,
													new JSONObject( map ) );
										}
									}
								}
								catch( JSONException e1 )
								{
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
						isGetting = false;
					}
					
					@Override
					public void onAdFailed(
							String arg0 )
					{
						Log.v( TAG , "NativeAdActivity onAdFailed info: " + arg0 );
						isGetting = false;
					}
					
					@Override
					public void onAdClose(
							String arg0 )
					{
						Log.v( TAG , "NativeAdActivity onAdClose info: " + arg0 );
					}
					
					@Override
					public void onAdClick(
							String arg0 )
					{
						Log.v( TAG , "NativeAdActivity onAdClick info: " + arg0 );
					}
					
					@Override
					public void onAdCancel(
							String arg0 )
					{
						Log.v( TAG , "NativeAdActivity onAdCancel info: " + arg0 );
					}
				} );
			}
		} );
	}
	
	public static void setAdPlaceId(
			String adpid )
	{
		adPlaceId = adpid;
	}
}
