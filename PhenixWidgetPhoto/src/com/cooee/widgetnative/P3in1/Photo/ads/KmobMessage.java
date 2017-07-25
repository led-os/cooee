package com.cooee.widgetnative.P3in1.Photo.ads;




public class KmobMessage
{
	//	private static final String TAG = "KmobMessage";
	//	public static final String UNI_ADPLACE_ID = "20160122080149502";
	//	private static final String UNI_APPID = "502";
	//	/**一次获取的广告数量*/
	//	public static final int SUMS = 5;
	//	private KmobAdItem mAdItem;
	//	private boolean isGetting = false;
	//	/**
	//	* 原生广告
	//	*/
	//	private AdBaseView mNativeView = null;
	//	
	//	public KmobMessage(
	//			KmobAdItem adItem )
	//	{
	//		mAdItem = adItem;
	//	}
	//	
	//	public void getKmobMessage(
	//			final Context context )
	//	{
	//		Log.i( TAG , "getKmobMessage start" );
	//		if( isGetting || mAdItem.isGetting() || !isWifi( context ) )
	//		{
	//			return;
	//		}
	//		isGetting = true;
	//		Log.i( TAG , "getKmobMessage isGetting" );
	//		if( mNativeView != null )
	//			mNativeView.onDestroy();
	//		mNativeView = createAdView( context );
	//		mNativeView.addAdViewListener( new AdViewListener() {
	//			
	//			@Override
	//			public void onAdShow(
	//					String arg0 )
	//			{
	//				Log.w( TAG , "NativeAdActivity onAdShow info: " + arg0 );
	//			}
	//			
	//			@Override
	//			public void onAdReady(
	//					String arg0 )
	//			{
	//				Log.w( TAG , "onAdReady" );
	//				if( arg0 != null )//NativeAdData
	//				{
	//					try
	//					{
	//						if( mAdItem != null )
	//						{
	//							mAdItem.updateAdData( new JSONArray( arg0 ) );
	//						}
	//					}
	//					catch( JSONException e )
	//					{
	//						// TODO Auto-generated catch block
	//						try
	//						{
	//							JSONObject obj = new JSONObject( arg0 );//后台返回的只有1个广告时，返回的是jasonobject，不是jasonarray
	//							JSONArray array = new JSONArray();
	//							array.put( obj );
	//							if( mAdItem != null )
	//							{
	//								mAdItem.updateAdData( array );
	//							}
	//						}
	//						catch( JSONException e1 )
	//						{
	//							// TODO Auto-generated catch block
	//							e1.printStackTrace();
	//						}
	//					}
	//				}
	//				isGetting = false;
	//			}
	//			
	//			@Override
	//			public void onAdFailed(
	//					String arg0 )
	//			{
	//				Log.w( TAG , "NativeAdActivity onAdFailed info: " + arg0 );
	//				isGetting = false;
	//			}
	//			
	//			@Override
	//			public void onAdClose(
	//					String arg0 )
	//			{
	//				Log.w( TAG , "NativeAdActivity onAdClose info: " + arg0 );
	//			}
	//			
	//			@Override
	//			public void onAdClick(
	//					String arg0 )
	//			{
	//				Log.w( TAG , "NativeAdActivity onAdClick info: " + arg0 );
	//			}
	//			
	//			@Override
	//			public void onAdCancel(
	//					String arg0 )
	//			{
	//				Log.w( TAG , "NativeAdActivity onAdCancel info: " + arg0 );
	//			}
	//		} );
	//	}
	//	
	//	/**
	//	 * 根据桌面版本创建对应的原生广告View
	//	 * 后期在其他桌面上使用不同的广告id时可以在此处添加
	//	 * @param context
	//	 * @return 原生广告view
	//	 * @author yangtianyu 2016-1-30
	//	 */
	//	private AdBaseView createAdView(
	//			Context context )
	//	{
	//		String launcherPkg = iLoongLauncher.getInstance().getPackageName();
	//		String appId = UNI_APPID;
	//		String adPlaceId = UNI_ADPLACE_ID;
	//		if( "com.cooee.unilauncher".equals( launcherPkg ) )
	//		{
	//			appId = UNI_APPID;
	//			adPlaceId = UNI_ADPLACE_ID;
	//		}
	//		KmobManager.setAppId( appId );
	//		// YANGTIANYU@2016/03/29 ADD START
	//		// 使用setChannelId设置厂商渠道号. 用来区分厂商收益.
	//		String channelid;
	//		try
	//		{
	//			JSONObject config = Assets.config.getJSONObject( "config" );
	//			channelid = config.getString( "serialno" );
	//			if( channelid != null )
	//				KmobManager.setChannel( channelid );
	//		}
	//		catch( Exception e )
	//		{
	//			e.printStackTrace();
	//		}
	//		// YANGTIANYU@2016/03/29 ADD END
	//		return KmobManager.createNative( adPlaceId , context , SUMS );
	//	}
	//	
	//	/**
	//	 * make true current connect service is wifi
	//	 * @param mContext
	//	 * @return
	//	 * @author yangtianyu 2016-1-30
	//	 */
	//	private static boolean isWifi(
	//			Context mContext )
	//	{
	//		ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getSystemService( Context.CONNECTIVITY_SERVICE );
	//		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
	//		if( activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI )
	//		{
	//			return true;
	//		}
	//		return false;
	//	}
	//	
	//	public void onDestroy()
	//	{
	//		if( mNativeView != null )
	//		{
	//			mNativeView.onDestroy();
	//			mNativeView.removeAllViews();
	//		}
	//		if( mAdItem != null )
	//		{
	//			mAdItem.onDestroy();
	//		}
	//	}
	//	
	//	public void onResume()
	//	{
	//		if( mNativeView != null )
	//			mNativeView.onResume();
	//	}
	//	
	//	public void onPause()
	//	{
	//		if( mNativeView != null )
	//			mNativeView.onPause();
	//	}
}
