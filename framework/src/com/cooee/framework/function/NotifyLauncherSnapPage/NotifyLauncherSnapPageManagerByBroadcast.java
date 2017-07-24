// xiatian add whole file //通知桌面切页：“晨想”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerByBroadcast.java”中的备注。
package com.cooee.framework.function.NotifyLauncherSnapPage;


// 【备注（对接方式）】
// 1、桌面在LauncherAppState.java实例化的时候，进行注册register。注册需要监听的广播（action为配置项“config_scroll_by_broadcast”配置的字符串）
// 2、桌面在LauncherAppState.java的onTerminate方法中，进行注销unRegister。注销需要监听的广播（action为配置项“config_scroll_by_broadcast”配置的字符串）
// 3、触发指纹传感器后，底层发送广播通知桌面。广播的action为配置项“config_scroll_by_broadcast”配置的字符串
// 4、桌面收到广播后，获取附加参数（对应key为"scrollBroadcastRight"）进行切页：“true”为向右切页；“false”为向左切页
// 5、配置项为“config_scroll_by_broadcast”
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class NotifyLauncherSnapPageManagerByBroadcast implements INotifyLauncherSnapPageManager
{
	
	private static NotifyLauncherSnapPageManagerByBroadcast mInstance = null;
	private final String TAG = "NotifyLauncherSnapPageManagerByBroadcast";
	private INotifyLauncherSnapPageCallBack mINotifyLauncherSnapPageCallBack = null;
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			final String mAction = intent.getAction();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.v( TAG , StringUtils.concat( "mAction=" , mAction , "-config_scroll_by_broadcast=" , BaseDefaultConfig.SCROLL_BY_BROADCAST ) );
			}
			if( mAction.equals( BaseDefaultConfig.SCROLL_BY_BROADCAST ) )
			{
				if( isNeedSnapToRight( intent ) )
				{
					notifyLauncherSnapToRight();
				}
				else
				{
					notifyLauncherSnapToLeft();
				}
			}
		}
	};
	
	private NotifyLauncherSnapPageManagerByBroadcast()
	{
	}
	
	public static NotifyLauncherSnapPageManagerByBroadcast getInstance()
	{
		if(
		//
		( !TextUtils.isEmpty( BaseDefaultConfig.SCROLL_BY_BROADCAST )/* isEnable() */)
		//
		&& ( mInstance == null )
		//
		)
		{
			synchronized( NotifyLauncherSnapPageManagerByBroadcast.class )
			{
				if( mInstance == null )
				{
					mInstance = new NotifyLauncherSnapPageManagerByBroadcast();
				}
			}
		}
		return mInstance;
	}
	
	@Override
	public boolean isEnable()
	{
		return( !TextUtils.isEmpty( BaseDefaultConfig.SCROLL_BY_BROADCAST ) );
	}
	
	@Override
	public void register(
			Object mObject )
	{//注册“广播”
		if( mObject instanceof Context )
		{
			Context mContext = (Context)mObject;
			IntentFilter mIntentFilter = new IntentFilter();
			mIntentFilter.addAction( BaseDefaultConfig.SCROLL_BY_BROADCAST );
			mContext.registerReceiver( mReceiver , mIntentFilter );
		}
	}
	
	@Override
	public void unRegister(
			Object mObject )
	{//注销“广播”
		if( mObject instanceof Context )
		{
			Context mContext = (Context)mObject;
			mContext.unregisterReceiver( mReceiver );
		}
	}
	
	@Override
	public void setCallBack(
			INotifyLauncherSnapPageCallBack mINotifyLauncherSnapPageCallBack )
	{
		this.mINotifyLauncherSnapPageCallBack = mINotifyLauncherSnapPageCallBack;
	}
	
	@Override
	public boolean isSnapToLeft(
			Object mObject )
	{
		boolean mIsSnapToLeft = false;
		if(
		//
		isNeedSnapToLeft( mObject )
		//
		&& notifyLauncherSnapToLeft()
		//
		)
		{
			mIsSnapToLeft = true;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "isSnapToLeft=" , mIsSnapToLeft ) );
		}
		return mIsSnapToLeft;
	}
	
	@Override
	public boolean isSnapToRight(
			Object mObject )
	{
		boolean mIsSnapToRight = false;
		if(
		//
		isNeedSnapToRight( mObject )
		//
		&& notifyLauncherSnapToRight()
		//
		)
		{
			mIsSnapToRight = true;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "isSnapToRight=" , mIsSnapToRight ) );
		}
		return mIsSnapToRight;
	}
	
	@Override
	public boolean isNeedSnapToLeft(
			Object mObject )
	{
		return( isNeedSnapToRight( mObject ) == false );
	}
	
	@Override
	public boolean isNeedSnapToRight(
			Object mObject )
	{
		if( ( mObject instanceof Intent ) == false )
		{
			throw new IllegalStateException( StringUtils.concat( TAG , "isNeedSnapToRight - [mObject not Intent]" ) );
		}
		boolean mIsNeedSnapToRight = false;
		final String mSnapPageToRightKey = "scrollBroadcastRight";
		Intent mIntent = (Intent)mObject;
		String mAction = mIntent.getAction();
		if( mAction.equals( BaseDefaultConfig.SCROLL_BY_BROADCAST ) == false )
		{
			throw new IllegalStateException( StringUtils.concat( TAG , "isNeedSnapToRight - [mAction != config_scroll_by_broadcast] - mAction:" , mAction ) );
		}
		if( mIntent.getBooleanExtra( mSnapPageToRightKey , true ) )
		{
			mIsNeedSnapToRight = true;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "isNeedSnapToRight=" , mIsNeedSnapToRight ) );
		}
		return mIsNeedSnapToRight;
	}
	
	@Override
	public boolean notifyLauncherSnapToLeft()
	{
		boolean mIsNotifyLauncherSnapToLeft = false;
		if( mINotifyLauncherSnapPageCallBack != null )
		{
			mINotifyLauncherSnapPageCallBack.notifyLauncherSnapToLeft();
			mIsNotifyLauncherSnapToLeft = true;
		}
		else
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.w( TAG , "isNotifyLauncherSnapToLeft - [please setCallBack frist!!]" );
			}
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "mIsNotifyLauncherSnapToLeft=" , mIsNotifyLauncherSnapToLeft ) );
		}
		return mIsNotifyLauncherSnapToLeft;
	}
	
	@Override
	public boolean notifyLauncherSnapToRight()
	{
		boolean mIsNotifyLauncherSnapToRight = false;
		if( mINotifyLauncherSnapPageCallBack != null )
		{
			mINotifyLauncherSnapPageCallBack.notifyLauncherSnapToRight();
			mIsNotifyLauncherSnapToRight = true;
		}
		else
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.w( TAG , "notifyLauncherSnapToRight - [please setCallBack frist!!]" );
			}
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "mIsNotifyLauncherSnapToRight=" , mIsNotifyLauncherSnapToRight ) );
		}
		return mIsNotifyLauncherSnapToRight;
	}
}
