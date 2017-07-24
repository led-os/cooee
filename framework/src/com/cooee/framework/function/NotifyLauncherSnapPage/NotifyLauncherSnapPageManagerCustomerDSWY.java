// xiatian add whole file //通知桌面切页：“德盛伟业”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerDSWY.java”中的备注。
package com.cooee.framework.function.NotifyLauncherSnapPage;


// 【备注（对接方式）】
// 1、桌面在每次回到桌面（onResume）的时候，都需要在进行激活（这个“光感传感器”比较特殊,激活（register）的时候，激活方式为：需要先激活光感传感器再注销光感传感器）
// 2、桌面在每次离开桌面（onPause）的时候，都不需要进行注销unRegister；
// 3、触发光感传感器后，底层调用KeyEvent事件，并下发“KEYCODE_F9消息（向左切页）”和“KEYCODE_F10消息（向右切页）”
// 4、不需要设置回调。我们直接在主activity（Launcher.java）的dispatchKeyEvent中的ACTION_UP进行拦截
// 5、开关为“switch_enable_customer_dswy_proximity_sensor_scroll”
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class NotifyLauncherSnapPageManagerCustomerDSWY implements INotifyLauncherSnapPageManager
{
	
	private final String TAG = "NotifyLauncherSnapPageManagerCustomerDSWY";
	private static NotifyLauncherSnapPageManagerCustomerDSWY mInstance = null;
	private INotifyLauncherSnapPageCallBack mINotifyLauncherSnapPageCallBack = null;
	
	private NotifyLauncherSnapPageManagerCustomerDSWY()
	{
	}
	
	public static NotifyLauncherSnapPageManagerCustomerDSWY getInstance()
	{
		if(
		//
		( BaseDefaultConfig.SWITCH_ENABLE_CUSTOMER_DSWY_PROXIMITY_SENSOR_SNAP_PAGE/* isEnable() */)
		//
		&& ( mInstance == null )
		//
		)
		{
			synchronized( NotifyLauncherSnapPageManagerCustomerDSWY.class )
			{
				if( mInstance == null )
				{
					mInstance = new NotifyLauncherSnapPageManagerCustomerDSWY();
				}
			}
		}
		return mInstance;
	}
	
	@Override
	public boolean isEnable()
	{
		return BaseDefaultConfig.SWITCH_ENABLE_CUSTOMER_DSWY_PROXIMITY_SENSOR_SNAP_PAGE;
	}
	
	@Override
	public void register(
			Object mObject )
	{//激活“光感传感器”
		//德盛伟业的这个“光感传感器”比较特殊（激活（register）的时候，激活方式为：需要先激活光感传感器再注销光感传感器）
		if( mObject instanceof Activity )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.v( TAG , "register" );
			}
			Activity mActivity = (Activity)mObject;
			if( mActivity != null )
			{
				PowerManager pm = (PowerManager)mActivity.getSystemService( Context.POWER_SERVICE );
				PowerManager.WakeLock mZZZWakeLock;
				Class<? extends PowerManager> class1 = pm.getClass();//拿不到 PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK 通过反射获取值
				int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 0x00000020;
				try
				{
					Field id = class1.getDeclaredField( "PROXIMITY_SCREEN_OFF_WAKE_LOCK" );
					PROXIMITY_SCREEN_OFF_WAKE_LOCK = id.getInt( id );
				}
				catch( Exception e )
				{
				}
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				{
					Log.d( TAG , StringUtils.concat( "PROXIMITY_SCREEN_OFF_WAKE_LOCK is" , PROXIMITY_SCREEN_OFF_WAKE_LOCK ) );
				}
				mZZZWakeLock = pm.newWakeLock( PROXIMITY_SCREEN_OFF_WAKE_LOCK , TAG );
				if( mZZZWakeLock != null )
				{
					if( !mZZZWakeLock.isHeld() )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Log.d( TAG , "PROXIMITY_SCREEN_OFF_WAKE_LOCK open" );
						}
						mZZZWakeLock.acquire();//打开
					}
					if( mZZZWakeLock.isHeld() )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						{
							Log.d( TAG , "PROXIMITY_SCREEN_OFF_WAKE_LOCK close" );
						}
						mZZZWakeLock.release();//关闭
					}
				}
			}
		}
	}
	
	@Override
	public void unRegister(
			Object mObject )
	{//注销“光感传感器”
		//德盛伟业的这个“光感传感器”比较特殊（不需要注销（unRegister））
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
		boolean mIsNeedSnapToLeft = false;
		if( mObject instanceof KeyEvent )
		{
			KeyEvent mKeyEvent = (KeyEvent)mObject;
			if(
			//
			( mKeyEvent.getAction() == KeyEvent.ACTION_UP )
			//
			&& ( mKeyEvent.getKeyCode() == KeyEvent.KEYCODE_F9 )
			//
			)
			{
				mIsNeedSnapToLeft = true;
			}
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "isNeedSnapToLeft=" , mIsNeedSnapToLeft ) );
		}
		return mIsNeedSnapToLeft;
	}
	
	@Override
	public boolean isNeedSnapToRight(
			Object mObject )
	{
		if( ( mObject instanceof KeyEvent ) == false )
		{
			throw new IllegalStateException( StringUtils.concat( TAG , "isNeedSnapToRight - [mObject not KeyEvent]" ) );
		}
		boolean mIsNeedSnapToRight = false;
		KeyEvent mKeyEvent = (KeyEvent)mObject;
		if(
		//
		( mKeyEvent.getAction() == KeyEvent.ACTION_UP )
		//
		&& ( mKeyEvent.getKeyCode() == KeyEvent.KEYCODE_F10 )
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
