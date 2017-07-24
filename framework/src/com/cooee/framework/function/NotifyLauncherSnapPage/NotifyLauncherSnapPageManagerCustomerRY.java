// xiatian add whole file //通知桌面切页：“锐益”定制功能（光感切页）。详见“NotifyLauncherSnapPageManagerCustomerRY.java”中的备注。
package com.cooee.framework.function.NotifyLauncherSnapPage;


// 【备注（对接方式）】
// 1、桌面在启动（onCreate）的时候，需要在进行激活（register）一次（注册mProximitySensorListener）；
// 2、桌面在销毁（onDestroy）的时候，需要在进行注销（unRegister）一次（注销mProximitySensorListener）；
// 3、触发光感传感器后，底层通知mProximitySensorListener；
// 4、需要设置回调。在mProximitySensorListener的onSensorChanged中进行判断，当满足一定条件后，通过回调去通知桌面切页
// 5、开关为“switch_enable_customer_ry_proximity_sensor_scroll”
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class NotifyLauncherSnapPageManagerCustomerRY implements INotifyLauncherSnapPageManager
{
	
	private static NotifyLauncherSnapPageManagerCustomerRY mInstance = null;
	private final static String TAG = "NotifyLauncherSnapPageManagerCustomerRY";
	public Context mContext = null;
	private SensorManager mSensorManager = null;
	private Sensor mProximitySensor = null;
	private final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
	private float mProximityThreshold = -1;
	private final boolean isAirOperation = SystemProperties.getBoolean( "persist.sys.AirOperation" , false );
	private INotifyLauncherSnapPageCallBack mINotifyLauncherSnapPageCallBack = null;
	private final SensorEventListener mProximitySensorEventListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(
				SensorEvent event )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.d( TAG , "onSensorChanged" );
			}
			isSnapToRight( event );
		}
		
		@Override
		public void onAccuracyChanged(
				Sensor sensor ,
				int accuracy )
		{
			// Not used.
		}
	};
	
	private NotifyLauncherSnapPageManagerCustomerRY()
	{
	}
	
	public static NotifyLauncherSnapPageManagerCustomerRY getInstance()
	{
		if(
		//
		( BaseDefaultConfig.SWITCH_ENABLE_CUSTOMER_RY_PROXIMITY_SENSOR_SNAP_PAGE/* isEnable() */)
		//
		&& ( mInstance == null )
		//
		)
		{
			synchronized( NotifyLauncherSnapPageManagerCustomerRY.class )
			{
				if( mInstance == null )
				{
					mInstance = new NotifyLauncherSnapPageManagerCustomerRY();
				}
			}
		}
		return mInstance;
	}
	
	private void initConfig(
			Context mContext )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , "initConfig" );
		}
		this.mContext = mContext;
		mSensorManager = (SensorManager)this.mContext.getSystemService( Context.SENSOR_SERVICE );
		mProximitySensor = mSensorManager.getDefaultSensor( Sensor.TYPE_PROXIMITY );
		if( mProximitySensor != null )
		{
			mProximityThreshold = Math.min( mProximitySensor.getMaximumRange() , TYPICAL_PROXIMITY_THRESHOLD );
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "initConfig - mProximityThreshold=" , mProximityThreshold ) );
		}
	}
	
	private void registerSensorEventListener()
	{
		if(
		//
		( mSensorManager != null )
		//
		&& ( mProximitySensor != null )
		//
		)
		{
			mSensorManager.registerListener( mProximitySensorEventListener , mProximitySensor , SensorManager.SENSOR_DELAY_NORMAL );
		}
	}
	
	private void unRegisterSensorEventListener()
	{
		if( mSensorManager != null )
		{
			mSensorManager.unregisterListener( mProximitySensorEventListener );
		}
	}
	
	@Override
	public boolean isEnable()
	{
		return BaseDefaultConfig.SWITCH_ENABLE_CUSTOMER_RY_PROXIMITY_SENSOR_SNAP_PAGE;
	}
	
	@Override
	public void register(
			Object mObject )
	{//激活“光感传感器”
		if( mObject instanceof Context )
		{
			initConfig( (Context)mObject );
		}
		registerSensorEventListener();
	}
	
	@Override
	public void unRegister(
			Object mObject )
	{//注销“光感传感器”
		unRegisterSensorEventListener();
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
		//不向外提供该接口
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
		//不向外提供该接口
		return false;
	}
	
	@Override
	public boolean isNeedSnapToRight(
			Object mObject )
	{
		boolean mIsNeedSnapToRight = false;
		if( mObject instanceof SensorEvent )
		{
			SensorEvent mSensorEvent = (SensorEvent)mObject;
			if( isAirOperation )
			{
				int operation = 0;
				if( mContext != null )
				{
					operation = Settings.System.getInt( mContext.getContentResolver() , "air_operation_launcher" , 0 );
				}
				if( operation == 1 )
				{
					final float distance = mSensorEvent.values[0];
					if(
					//
					( distance >= 0.0f )
					//
					&& ( distance < mProximityThreshold )
					//
					)
					{
						mIsNeedSnapToRight = true;
					}
				}
			}
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
		//不向外提供该接口
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
				Log.w( TAG , "please setCallBack frist!! [isNotifyLauncherSnapToRight]" );
			}
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.v( TAG , StringUtils.concat( "mIsNotifyLauncherSnapToRight=" , mIsNotifyLauncherSnapToRight ) );
		}
		return mIsNotifyLauncherSnapToRight;
	}
}
