// gaominghui add whole file //通知桌面切页：“讯虎”定制功能（双向光感切页）。详见“NotifyLauncherSnapPageManagerByBroadcastXH.java”中的备注。
package com.cooee.framework.function.NotifyLauncherSnapPage;


// 【备注（对接方式）】
// 1、桌面在LauncherAppState.java实例化的时候，进行注册register。注册需要监听的广播（acting为 "com.qty.intent.action.PAGE_DOWN"和"com.qty.intent.action.PAGE_UP"）
// 2、桌面在LauncherAppState.java的onTerminate方法中，进行注销unRegister。注销已经监听的广播
// 3、触发传感器后，底层发送广播通知桌面切页
// 4、桌面收到广播后通过广播action进行切页："com.qty.intent.action.PAGE_DOWN"为向左切页；"com.qty.intent.action.PAGE_UP"向右切页
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class NotifyLauncherSnapPageManagerByBroadcastXH implements INotifyLauncherSnapPageManager
{
	
	/** 讯虎定制特殊传感器切页接收广播*/
	private final String XH_SCROLL_LEFT = "com.qty.intent.action.PAGE_DOWN";
	private final String XH_SCROLL_RIGHT = "com.qty.intent.action.PAGE_UP";
	private static final String FLIPNEXTPAGE_PREFERENCE_PERSIST = "persist.sys.nextpageenable";
	private static NotifyLauncherSnapPageManagerByBroadcastXH mInstance = null;
	private final String TAG = "NotifyLauncherSnapPageManagerByBroadcastXH";
	private INotifyLauncherSnapPageCallBack mINotifyLauncherSnapPageCallBack = null;
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			if( XH_SCROLL_LEFT.equals( intent.getAction() ) )
			{
				notifyLauncherSnapToLeft();
			}
			else if( XH_SCROLL_RIGHT.equals( intent.getAction() ) )
			{
				notifyLauncherSnapToRight();
			}
		}
	};
	
	private NotifyLauncherSnapPageManagerByBroadcastXH()
	{
	}
	
	public static NotifyLauncherSnapPageManagerByBroadcastXH getInstance()
	{
		if( ( BaseDefaultConfig.XUNHU_SENSOR ) && ( mInstance == null ) )
		{
			synchronized( NotifyLauncherSnapPageManagerByBroadcastXH.class )
			{
				if( mInstance == null )
				{
					mInstance = new NotifyLauncherSnapPageManagerByBroadcastXH();
				}
			}
		}
		return mInstance;
	}
	
	@Override
	public boolean isEnable()
	{
		return BaseDefaultConfig.XUNHU_SENSOR;
	}
	
	@Override
	public void register(
			Object mObject )
	{//注册“广播”
		if( mObject instanceof Context )
		{
			Context mContext = (Context)mObject;
			IntentFilter mIntentFilter = new IntentFilter();
			mIntentFilter.addAction( XH_SCROLL_LEFT );
			mIntentFilter.addAction( XH_SCROLL_RIGHT );
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
		return false;
	}
	
	@Override
	public boolean isSnapToRight(
			Object mObject )
	{
		return false;
	}
	
	@Override
	public boolean isNeedSnapToLeft(
			Object mObject )
	{
		return false;
	}
	
	@Override
	public boolean isNeedSnapToRight(
			Object mObject )
	{
		return false;
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
	
	public void enableGesture(
			Context context )
	{
		if( isSensorSettingOpened() )
		{
			Intent intent = new Intent();
			intent.setAction( "com.sensortek.broadcast.enable" );
			context.sendBroadcast( intent );
		}
	}
	
	private boolean isSensorSettingOpened()
	{
		String sensorValue = SystemProperties.get( FLIPNEXTPAGE_PREFERENCE_PERSIST , "off" );
		if( "on".equals( sensorValue ) )
		{
			return true;
		}
		return false;
	}
	
	public void disableGesture(
			Context context )
	{
		if( !isSensorSettingOpened() )
		{
			Intent intent = new Intent();
			intent.setAction( "com.sensortek.broadcast.disable" );
			context.sendBroadcast( intent );
		}
	}
}
