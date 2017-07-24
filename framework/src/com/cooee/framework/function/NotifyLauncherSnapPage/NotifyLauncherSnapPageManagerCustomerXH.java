// xiatian add whole file //通知桌面切页：“讯虎”定制功能（指纹切页）。详见“NotifyLauncherSnapPageManagerCustomerXH.java”中的备注。
package com.cooee.framework.function.NotifyLauncherSnapPage;


// 【备注（对接方式）】
// 1、不需要在进行激活register；
// 2、不需要进行注销unRegister；
// 3、触发指纹传感器后，底层调用KeyEvent事件，并下发“KEYCODE_F11消息（向右切页）”
// 4、不需要设置回调。我们直接在主activity（Launcher.java）的dispatchKeyEvent中的ACTION_DOWN进行拦截
// 5、开关为“switch_enable_customer_xh_finger_print_scroll”
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class NotifyLauncherSnapPageManagerCustomerXH implements INotifyLauncherSnapPageManager
{
	
	private static NotifyLauncherSnapPageManagerCustomerXH mInstance = null;
	private final static String TAG = "NotifyLauncherSnapPageManagerCustomerXH";
	private INotifyLauncherSnapPageCallBack mINotifyLauncherSnapPageCallBack = null;
	
	private NotifyLauncherSnapPageManagerCustomerXH()
	{
	}
	
	public static NotifyLauncherSnapPageManagerCustomerXH getInstance()
	{
		if(
		//
		( BaseDefaultConfig.SWITCH_ENABLE_CUSTOMER_XH_FINGER_PRINT_SCROLL/* isEnable() */)
		//
		&& ( mInstance == null )
		//
		)
		{
			synchronized( NotifyLauncherSnapPageManagerCustomerXH.class )
			{
				if( mInstance == null )
				{
					mInstance = new NotifyLauncherSnapPageManagerCustomerXH();
				}
			}
		}
		return mInstance;
	}
	
	@Override
	public boolean isEnable()
	{
		return BaseDefaultConfig.SWITCH_ENABLE_CUSTOMER_XH_FINGER_PRINT_SCROLL;
	}
	
	@Override
	public void register(
			Object mObject )
	{
	}
	
	@Override
	public void unRegister(
			Object mObject )
	{
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
		return false;
	}
	
	@Override
	public boolean isNeedSnapToRight(
			Object mObject )
	{
		if( ( mObject instanceof Object[] ) == false )
		{
			throw new IllegalStateException( StringUtils.concat( TAG , "isNeedSnapToRight - [mObject not Object[]]" ) );
		}
		Object[] mObjects = (Object[])mObject;
		if( mObjects.length != 2 )
		{
			throw new IllegalStateException( StringUtils.concat( TAG , "isNeedSnapToRight - [mObjects.length != 2]" ) );
		}
		boolean mIsNeedSnapToRight = false;
		Object mObject1 = mObjects[0];
		if( ( mObject1 instanceof KeyEvent ) == false )
		{
			throw new IllegalStateException( StringUtils.concat( TAG , "isNeedSnapToRight - [mObject1 not KeyEvent]" ) );
		}
		Object mObject2 = mObjects[1];
		if( ( mObject2 instanceof Context ) == false )
		{
			throw new IllegalStateException( StringUtils.concat( TAG , "isNeedSnapToRight - [mObject2 not Context]" ) );
		}
		KeyEvent mKeyEvent = (KeyEvent)mObject1;
		Context mContext = (Context)mObject2;
		if(
		//
		( mKeyEvent.getAction() == KeyEvent.ACTION_DOWN )
		//
		&& ( mKeyEvent.getKeyCode() == KeyEvent.KEYCODE_F11 )
		//
		&& ( Settings.System.getInt( mContext.getContentResolver() , "com_cdfinger_fingerprint_usedto_launcher" , 1 ) == 1 )
		//
		)
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
		return false;
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
