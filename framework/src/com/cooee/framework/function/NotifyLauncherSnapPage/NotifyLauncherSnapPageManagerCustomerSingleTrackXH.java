/***/
package com.cooee.framework.function.NotifyLauncherSnapPage;


// 【备注（对接方式）】
// 1、桌面在启动（onCreate）的时候，需要在进行激活（register）一次（注册mProximitySensorListener）；
// 2、桌面在销毁（onDestroy）的时候，需要在进行注销（unRegister）一次（注销mProximitySensorListener）；
// 3、触发光感传感器后，底层通知mProximitySensorListener；
// 4、需要设置回调。在mProximitySensorListener的onSensorChanged中进行判断，当满足一定条件后，通过回调去通知桌面切页
// 5、开关为“xunhu_proximity_sensor_scroll”
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


/**
 * 讯虎单向切页
 * @author gaominghui 2017年4月26日
 */
public class NotifyLauncherSnapPageManagerCustomerSingleTrackXH implements INotifyLauncherSnapPageManager
{
	
	private final static String TAG = "NotifyLauncherSnapPageManagerCustomerSingleTrackXH";
	private SensorManager mSensorManager;
	private Sensor mProximitySensor;
	private Runnable mProximityTask;
	private Context mContext;
	private long mLastProximityEventTime;
	private static final float PROXIMITY_THRESHOLD = 5.0f;
	private static final int PROXIMITY_SENSOR_DELAY = 200;
	private int mProximityPendingValue = -1;
	private Handler mPsensorHandler = new Handler();
	private static final String FLIPNEXTPAGE_PREFERENCE_PERSIST = "persist.sys.nextpageenable";
	private INotifyLauncherSnapPageCallBack mINotifyLauncherSnapPageCallBack = null;
	private static NotifyLauncherSnapPageManagerCustomerSingleTrackXH mInstance = null;
	
	private NotifyLauncherSnapPageManagerCustomerSingleTrackXH()
	{
	}
	
	public static NotifyLauncherSnapPageManagerCustomerSingleTrackXH getInstance()
	{
		if( ( BaseDefaultConfig.XUNHU_PROXIMITY_SENSOR_SCROLL ) && ( mInstance == null ) )
		{
			synchronized( NotifyLauncherSnapPageManagerCustomerSingleTrackXH.class )
			{
				if( mInstance == null )
				{
					mInstance = new NotifyLauncherSnapPageManagerCustomerSingleTrackXH();
				}
			}
		}
		return mInstance;
	}
	
	private final SensorEventListener mProximityListener = new SensorEventListener() {
		
		public void onSensorChanged(
				SensorEvent event )
		{
			isSnapToRight( event );
		}
		
		public void onAccuracyChanged(
				Sensor sensor ,
				int accuracy )
		{
		}
	};
	
	private void initConfig(
			Context context )
	{
		mContext = context;
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.i( TAG , "initConfig  nextpageenable = " + SystemProperties.get( FLIPNEXTPAGE_PREFERENCE_PERSIST , "off" ) );
		}
		if( SystemProperties.get( FLIPNEXTPAGE_PREFERENCE_PERSIST , "off" ).equals( "on" ) )
		{
			if( mSensorManager != null )
				return;
			mSensorManager = (SensorManager)mContext.getSystemService( Context.SENSOR_SERVICE );
			mProximitySensor = mSensorManager.getDefaultSensor( Sensor.TYPE_PROXIMITY );
			mProximityTask = new Runnable() {
				
				public void run()
				{
					synchronized( this )
					{
						if( mProximityPendingValue != -1 )
						{
							if( mProximityPendingValue == 1 )
							{
								notifyLauncherSnapToRight();
							}
							mProximityPendingValue = -1;
						}
					}
				}
			};
			registerSensorEventListener();
		}
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageManager#isEnable()
	 * @auther gaominghui  2017年4月26日
	 */
	@Override
	public boolean isEnable()
	{
		// TODO Auto-generated method stub
		return BaseDefaultConfig.XUNHU_PROXIMITY_SENSOR_SCROLL;
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageManager#register(java.lang.Object)
	 * @auther gaominghui  2017年4月26日
	 */
	@Override
	public void register(
			Object mObject )
	{
		if( mObject instanceof Context )
		{
			initConfig( (Context)mObject );
		}
	}
	
	private void registerSensorEventListener()
	{
		if( mSensorManager != null && mProximitySensor != null )
		{
			mSensorManager.registerListener( mProximityListener , mProximitySensor , SensorManager.SENSOR_DELAY_NORMAL );
		}
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageManager#unRegister(java.lang.Object)
	 * @auther gaominghui  2017年4月26日
	 */
	@Override
	public void unRegister(
			Object mObject )
	{
		if( mSensorManager != null )
		{
			mSensorManager.unregisterListener( mProximityListener );
		}
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageManager#setCallBack(com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageCallBack)
	 * @auther gaominghui  2017年4月26日
	 */
	@Override
	public void setCallBack(
			INotifyLauncherSnapPageCallBack mINotifyLauncherSnapPageCallBack )
	{
		this.mINotifyLauncherSnapPageCallBack = mINotifyLauncherSnapPageCallBack;
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageManager#isSnapToLeft(java.lang.Object)
	 * @auther gaominghui  2017年4月26日
	 */
	@Override
	public boolean isSnapToLeft(
			Object mObject )
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageManager#isSnapToRight(java.lang.Object)
	 * @auther gaominghui  2017年4月26日
	 */
	@Override
	public boolean isSnapToRight(
			Object mObject )
	{
		boolean mIsSnapToRight = false;
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.i( TAG , "isSnapToRight  nextpageenable = " + SystemProperties.get( FLIPNEXTPAGE_PREFERENCE_PERSIST , "off" ) );
		}
		if( SystemProperties.get( FLIPNEXTPAGE_PREFERENCE_PERSIST , "off" ).equals( "on" ) )
		{
			if( isNeedSnapToRight( mObject ) && notifyLauncherSnapToRight() )
			{
				mIsSnapToRight = true;
			}
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.v( TAG , StringUtils.concat( "isSnapToRight=" , mIsSnapToRight ) );
			}
		}
		return mIsSnapToRight;
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageManager#isNeedSnapToLeft(java.lang.Object)
	 * @auther gaominghui  2017年4月26日
	 */
	@Override
	public boolean isNeedSnapToLeft(
			Object mObject )
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageManager#isNeedSnapToRight(java.lang.Object)
	 * @auther gaominghui  2017年4月26日
	 */
	@Override
	public boolean isNeedSnapToRight(
			Object mObject )
	{
		// TODO Auto-generated method stub
		boolean mIsNeedSnapToRight = false;
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.i( TAG , "isSnapToRight  nextpageenable = " + SystemProperties.get( FLIPNEXTPAGE_PREFERENCE_PERSIST , "off" ) );
		}
		if( SystemProperties.get( FLIPNEXTPAGE_PREFERENCE_PERSIST , "off" ).equals( "on" ) )
		{
			if( null == mObject )
			{
				return false;
			}
			if( mObject instanceof SensorEvent )
			{
				SensorEvent mSensorEvent = (SensorEvent)mObject;
				long milliseconds = SystemClock.elapsedRealtime();
				synchronized( this )
				{
					float distance = mSensorEvent.values[0];
					long timeSinceLastEvent = milliseconds - mLastProximityEventTime;
					mLastProximityEventTime = milliseconds;
					mPsensorHandler.removeCallbacks( mProximityTask );
					// liuning@2015/06/25 UPD START
					//如果距离小于某一个距离阈值（默认是5.0f，具体依据手机设定的maximumRange），说明手机和手距离贴近，应该要执行切页操作。
					boolean active = ( distance >= 0.0 && distance < PROXIMITY_THRESHOLD && distance < mProximitySensor.getMaximumRange() );
					//liuning @2015/06/25 UPD END
					if( timeSinceLastEvent < PROXIMITY_SENSOR_DELAY )
					{
						mProximityPendingValue = ( active ? 1 : 0 );
						mPsensorHandler.postDelayed( mProximityTask , PROXIMITY_SENSOR_DELAY - timeSinceLastEvent );
					}
					else
					{
						mProximityPendingValue = -1;
						if( active )
						{
							mIsNeedSnapToRight = true;
							//mXHSensor.xhSensorScrollToRight();
						}
					}
				}
			}
		}
		return mIsNeedSnapToRight;
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageManager#notifyLauncherSnapToLeft()
	 * @auther gaominghui  2017年4月26日
	 */
	@Override
	public boolean notifyLauncherSnapToLeft()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 *
	 * @see com.cooee.framework.function.NotifyLauncherSnapPage.INotifyLauncherSnapPageManager#notifyLauncherSnapToRight()
	 * @auther gaominghui  2017年4月26日
	 */
	@Override
	public boolean notifyLauncherSnapToRight()
	{
		boolean mIsNotifyLauncherSnapToRight = false;
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.i( TAG , "notifyLauncherSnapToRight nextpageenable  = " + SystemProperties.get( FLIPNEXTPAGE_PREFERENCE_PERSIST , "off" ) );
		}
		if( SystemProperties.get( FLIPNEXTPAGE_PREFERENCE_PERSIST , "off" ).equals( "on" ) )
		{
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
		}
		return mIsNotifyLauncherSnapToRight;
	}
}
