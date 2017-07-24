package com.cooee.phenix.kmob.ad;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cooee.framework.utils.LauncherConfigUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;


public class KmobMessage
{
	
	private static final String TAG = "KmobMessage";
	private KmobAdItem mAdItem;
	private boolean isGetting = false;
	/**
	* 原生广告
	*/
	private AdBaseView mNativeView = null;
	
	public KmobMessage(
			KmobAdItem adItem )
	{
		mAdItem = adItem;
	}
	
	public void getKmobMessage(
			final Context context ,
			String adPlackId ,
			int sums )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "getKmobMessage start - isGetting = " , isGetting , ";mAdItem.isGetting() =  " , mAdItem.isGetting() , "; mAdItem = " , mAdItem.toString() ) );
		if( isGetting || mAdItem.isGetting() )
		{
			return;
		}
		isGetting = true;
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "getKmobMessage isGetting" );
		if( mNativeView != null )
			mNativeView.onDestroy();
		mNativeView = createAdView( context , adPlackId , sums );
		mNativeView.addAdViewListener( new AdViewListener() {
			
			@Override
			public void onAdShow(
					String arg0 )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , StringUtils.concat( "NativeAdActivity onAdShow info: " , arg0 ) );
			}
			
			@Override
			public void onAdReady(
					String arg0 )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , StringUtils.concat( "onAdReady  arg0 = " , arg0 ) );
				if( arg0 != null )//NativeAdData
				{
					try
					{
						if( mAdItem != null )
						{
							mAdItem.updateAdData( new JSONArray( arg0 ) );
						}
					}
					catch( JSONException e )
					{
						try
						{
							JSONObject obj = new JSONObject( arg0 );//后台返回的只有1个广告时，返回的是jasonobject，不是jasonarray
							JSONArray array = new JSONArray();
							array.put( obj );
							if( mAdItem != null )
							{
								mAdItem.updateAdData( array );
							}
						}
						catch( JSONException e1 )
						{
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
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , StringUtils.concat( "NativeAdActivity onAdFailed info: " , arg0 ) );
				isGetting = false;
			}
			
			@Override
			public void onAdClose(
					String arg0 )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , StringUtils.concat( "NativeAdActivity onAdClose info: " , arg0 ) );
			}
			
			@Override
			public void onAdClick(
					String arg0 )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , StringUtils.concat( "NativeAdActivity onAdClick info: " , arg0 ) );
			}
			
			@Override
			public void onAdCancel(
					String arg0 )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( TAG , StringUtils.concat( "NativeAdActivity onAdCancel info: " , arg0 ) );
			}
		} );
	}
	
	/**
	 * make true current connect service is wifi
	 * @param mContext
	 * @return
	 * @author yangtianyu 2016-1-30
	 */
	public static boolean isWifi(
			Context mContext )
	{
		ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if( activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI )
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 根据桌面版本创建对应的原生广告View
	 * 后期在其他桌面上使用不同的广告id时可以在此处添加
	 * @param context
	 * @return 原生广告view
	 * @author yangtianyu 2016-1-30
	 */
	private AdBaseView createAdView(
			Context context ,
			String adPlaceId ,
			int sums )
	{
		String channelid;
		try
		{
			channelid = LauncherConfigUtils.getSN( context );
			KmobManager.setContext( context.getApplicationContext() ); //gaominghui add kmob初始化context
			if( channelid != null )
				KmobManager.setChannel( channelid );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return KmobManager.createNative( adPlaceId , context , sums );
	}
	
	public void onDestroy()
	{
		if( mNativeView != null )
		{
			mNativeView.onDestroy();
			mNativeView.removeAllViews();
		}
		if( mAdItem != null )
		{
			mAdItem.onDestroy();
		}
	}
	
	public void onResume()
	{
		if( mNativeView != null )
			mNativeView.onResume();
	}
	
	public void onPause()
	{
		if( mNativeView != null )
			mNativeView.onPause();
	}
}
