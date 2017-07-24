package com.cooee.wallpaper.data;


import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.wallpaper.manager.ChangeWallpaperManager;
import com.cooee.wallpaper.util.Assets;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;
import com.umeng.analytics.MobclickAgent;


public class KmobAdverManager
{
	
	private static KmobAdverManager mKmobAdverManager;
	private static String TAG = "KmobAdverManager";
	private Context mContext;
	// zhangjin@2016/03/29 ADD START
	private String mSn = "";
	
	// zhangjin@2016/03/29 ADD END
	private KmobAdverManager(
			Context mContext )
	{
		this.mContext = mContext;
	}
	
	public static KmobAdverManager getKmobAdverManager(
			Context mContext )
	{
		if( mKmobAdverManager == null && mContext != null )
		{
			synchronized( TAG )
			{
				if( mKmobAdverManager == null && mContext != null )
				{
					mKmobAdverManager = new KmobAdverManager( mContext );
				}
			}
		}
		return mKmobAdverManager;
	}
	
	/*************************一键换壁纸 start*****************************/
	private AdBaseView mWallpaperAdverView = null;
	private KmobAdWallpaperCallbacks mKmobAdWallpaperCallbacks = null;
	
	/**
	 * 初始化壁纸广告
	 * @param wallpaper 
	 */
	public void setKmobAdWallpaperCallbacks(
			KmobAdWallpaperCallbacks callbacks )
	{
		mKmobAdWallpaperCallbacks = callbacks;
	}
	
	public static String getAppId(
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
		return "441";// manifest中没有注册则默认使用的是phenix的广告位
	}
	
	private String getAdPlaceId()
	{
		String id = getAppId( mContext );
		KmobManager.setAppId( id );
		String adplaceID = "";
		/*if( id.equals( "461" ) )
		{
			adplaceID = "20160504100502461";//UNI4桌面
		}
		else if( id.equals( "281" ) )
		{
			adplaceID = "20160504100545281";//UNI3桌面
		}
		else if( id.equals( "505" ) )
		{
			adplaceID = "20160504090541505";//老人桌面
		}
		else */if( id.equals( "441" ) )
		{
			adplaceID = "20160303030334441";//phenix1.1稳定版
		}
		else if( id.equals( "543" ) )
		{
			adplaceID = "20160613060616543";//s5
		}
		else if( id.equals( "506" ) )
		{
			adplaceID = "20160516050545506";//移植包
		}
		return adplaceID;
	}
	
	public void initWallpaperAdverAdver()
	{
		if( mWallpaperAdverView != null )
		{
			mWallpaperAdverView.onDestroy();
		}
		String adPlaceId = getAdPlaceId();
		Log.v( "operateWallpaperData" , "initWallpaperAdverAdver" );
		KmobManager.setContext( ChangeWallpaperManager.getContainerContext().getApplicationContext() ); //gaominghui add kmob初始化context
		// zhangjin@2016/03/29 ADD START
		KmobManager.setChannel( getSn() );
		// zhangjin@2016/03/29 ADD END
		if( ChangeWallpaperManager.SWITCH_ENABLE_UMENG )
		{
			MobclickAgent.onEvent( ChangeWallpaperManager.getContainerContext() , UmengStatistics.one_key_change_wallpapper_AD_request );
		}
		int width = mContext.getResources().getDisplayMetrics().widthPixels;
		int height = (int)( width / 6.4f );
		mWallpaperAdverView = KmobManager.createBanner( adPlaceId , mContext , width , height );
		mWallpaperAdverView.addAdViewListener( new AdViewListener() {
			
			@Override
			public void onAdShow(
					String info )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAdReady(
					final String info )
			{
				Log.v( "operateWallpaperData" , "onAdReady info = " + info + " mKmobAdWallpaperCallbacks = " + mKmobAdWallpaperCallbacks );
				if( mKmobAdWallpaperCallbacks != null )
				{
					mWallpaperAdverView.setTag( info );
					mKmobAdWallpaperCallbacks.notifyKmobAdDataChanged( mWallpaperAdverView );
				}
				if( ChangeWallpaperManager.SWITCH_ENABLE_UMENG )
				{
					MobclickAgent.onEvent( ChangeWallpaperManager.getContainerContext() , UmengStatistics.one_key_change_wallpapper_AD_downloadfinish );
				}
			}
			
			@Override
			public void onAdFailed(
					String reason )
			{
				Log.v( "operateWallpaperData" , "onAdFailed info = " + reason );
			}
			
			@Override
			public void onAdClose(
					String info )
			{
			}
			
			@Override
			public void onAdClick(
					String info )
			{
				if( ChangeWallpaperManager.SWITCH_ENABLE_UMENG )
				{
					MobclickAgent.onEvent( ChangeWallpaperManager.getContainerContext() , UmengStatistics.click_one_key_change_wallpapper_AD );
				}
			}
			
			@Override
			public void onAdCancel(
					String info )
			{
			}
		} );
	}
	
	public interface KmobAdWallpaperCallbacks
	{
		
		public void notifyKmobAdDataChanged(
				AdBaseView bannerView );
	}
	
	/*************************一键换壁纸 end*****************************/
	public void onResume()
	{
		if( mWallpaperAdverView != null )
		{
			mWallpaperAdverView.onResume();
		}
	}
	
	public void onPause()
	{
		if( mWallpaperAdverView != null )
		{
			mWallpaperAdverView.onPause();
		}
	}
	
	public void onDestroy()
	{
		if( mWallpaperAdverView != null )
		{
			mWallpaperAdverView.onDestroy();
		}
	}
	
	// zhangjin@2016/03/29 ADD START
	public String getSn()
	{
		if( TextUtils.isEmpty( mSn ) == false )
		{
			return mSn;
		}
		JSONObject tmp = Assets.config;
		if( tmp != null )
			try
			{
				JSONObject config = tmp.getJSONObject( "config" );
				String sn = config.getString( "serialno" );
				return sn;
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
		return null;
	}
	// zhangjin@2016/03/29 ADD END
}
