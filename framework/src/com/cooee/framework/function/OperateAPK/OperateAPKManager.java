// xiatian add whole file //桌面运营某些内置应用的某些界面（详见“BaseDefaultConfig”中说明）
package com.cooee.framework.function.OperateAPK;


import java.net.HttpURLConnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.common.CoolMethod;
import cool.sdk.common.UrlUtil;
import cool.sdk.download.manager.DlMethod;


public class OperateAPKManager extends BroadcastReceiver
{
	
	private static final String TAG = "OperateAPKManager";
	private static IOperateAPKCallbacks mOperateAPKCallbacks = null;
	
	public interface IOperateAPKCallbacks
	{
		
		public void showAPKS(
				String[] packages );
		
		public boolean canShowAPKS();
	}
	
	public static void setCallbacks(
			IOperateAPKCallbacks mCallbacks )
	{
		mOperateAPKCallbacks = mCallbacks;
	}
	
	@Override
	public void onReceive(
			Context c ,
			Intent intent )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "intent.getAction:" , intent.getAction() ) );
		//		if(BaseAppState.isWifiEnabled( c ))
		ConnectivityManager mConnectivityManager = (ConnectivityManager)c.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo activeInfo = mConnectivityManager.getActiveNetworkInfo();
		if( activeInfo != null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , " activeInfo != null " );
			showApksInDelayShowAppList( c );
		}
		else
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , " activeInfo == null " );
		}
	}
	
	private void showApksInDelayShowAppList(
			final Context context )
	{
		if( mOperateAPKCallbacks == null || mOperateAPKCallbacks.canShowAPKS() == false )
		{
			return;
		}
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{//耗时操作（联网、遍历查找等），放入线程
				long mUseTime = getUseTime( context );
				if( mUseTime <= 0 )
				{
					return;
				}
				String[] mPackages = BaseAppState.getToShowApksInDelayShowAppList( context , mUseTime );
				if( mPackages != null )
				{
					mOperateAPKCallbacks.showAPKS( mPackages );
				}
			}
		} ).start();
	}
	
	private long getServiceTime(
			Context context )
	{
		long time = -1;
		try
		{
			HttpURLConnection conn = DlMethod.HttpGet( context , UrlUtil.urlGetTime );
			time = Long.parseLong( new String( DlMethod.bytesFromStream( conn.getInputStream() ) ) );
			conn.disconnect();
		}
		catch( Exception e )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( TAG , StringUtils.concat( "getServiceTime --e:" , e.toString() ) );
		}
		return time;
	}
	
	private long getAppActiveTime(
			Context context )
	{
		return CoolMethod.getAppActiveTime( context );//应用激活时间
	}
	
	private long getUseTime(
			Context context )
	{
		long mAppActiveTime = getAppActiveTime( context );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "getAppActiveTime:" , mAppActiveTime ) );
		if( mAppActiveTime <= 0 )
		{
			return -1;
		}
		long mCurTime = getServiceTime( context );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "getServiceTime:" , mCurTime ) );
		if( mCurTime <= 0 )
		{
			return -1;
		}
		long mUseTime = mCurTime - mAppActiveTime;
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , StringUtils.concat( "mUseTime:" , mUseTime ) );
		if( mUseTime <= 0 )
		{
			return -1;
		}
		return mUseTime;
	}
}
