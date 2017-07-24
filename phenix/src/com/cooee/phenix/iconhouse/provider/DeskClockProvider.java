package com.cooee.phenix.iconhouse.provider;


import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class DeskClockProvider extends IconHouseProvider
{
	
	protected static final int BMP_BG = R.drawable.icon_house_deskclock;
	protected static final int BMP_HOUR = R.drawable.icon_house_deskclock_hour;
	//	protected static final int BMP_HOUR_BG = R.drawable.icon_house_deskclock_hour_bg;
	protected static final int BMP_MIN = R.drawable.icon_house_deskclock_min;
	//	protected static final int BMP_MIN_BG = R.drawable.icon_house_deskclock_min_bg;
	protected static final int BMP_SEC = R.drawable.icon_house_deskclock_sec;
	//	protected static final int BMP_SEC_BG = R.drawable.icon_house_deskclock_sec_bg;
	protected static final int BMP_CENTER = R.drawable.icon_house_deskclock_center;
	protected Bitmap mBitmapBg = null;
	protected Bitmap mBitmapHour = null;
	//	protected Bitmap mBitmapHourBg = null;
	protected Bitmap mBitmapMin = null;
	//	protected Bitmap mBitmapMinBg = null;
	protected Bitmap mBitmapSec = null;
	//	protected Bitmap mBitmapSecBg = null;
	protected Bitmap mBitmapCenter = null;
	protected Context mContext;
	protected Paint mPaint;
	protected PaintFlagsDrawFilter mDrawFilter;
	//	protected Time mTime;
	protected Calendar mCalendar;
	//
	private Handler mHandler;
	private TimerTask mTask;
	private Runnable mTaskRunnable;
	private Timer mTimer;
	//
	private final BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			mTask.cancel();
			mTimer.purge();
			mTask = new TimerTask() {
				
				public void run()
				{
					mTaskRunnable.run();
				}
			};
			mTimer.scheduleAtFixedRate( mTask , 0 , 1000 );
		}
	};
	
	public DeskClockProvider(
			Context context )
	{
		mContext = context;
		mPaint = new Paint();
		mPaint.setColor( 0x55ff0000 );
		//初始化图片
		themePath = "theme/icon/80/dynamicicon/deskclock/";//zhujieping add  //需求：桌面动态图标支持随主题变化
		Resources res = mContext.getResources();
		mBitmapBg = getCurrentThemeBitmap( res , BMP_BG );
		mBitmapHour = getCurrentThemeBitmap( res , BMP_HOUR );
		mBitmapMin = getCurrentThemeBitmap( res , BMP_MIN );
		mBitmapSec = getCurrentThemeBitmap( res , BMP_SEC );
		mBitmapCenter = getCurrentThemeBitmap( res , BMP_CENTER );
		mDrawFilter = new PaintFlagsDrawFilter( 0 , Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG );
		//
		// zhangjin@2016/07/19 UPD START
		//mTime = new Time();
		//mTime.setToNow();
		mCalendar = Calendar.getInstance();
		// zhangjin@2016/07/19 UPD END
		//begin desk clock
		mHandler = new Handler( Looper.getMainLooper() ) {
			
			@Override
			public void handleMessage(
					Message msg )
			{
				// TODO Auto-generated method stub
				performUpdate();
			}
		};
		mTaskRunnable = new Runnable() {
			
			public void run()
			{
				boolean canUpdate = mUpdateListener.isCmpVisible( mTarget );
				if( mUpdateListener == null || canUpdate == false )
				{
					return;
				}
				updateBitmap();
				Message message = Message.obtain();
				message.what = 1;
				mHandler.sendMessage( message );
			}
		};
		mTask = new TimerTask() {
			
			public void run()
			{
				mTaskRunnable.run();
			}
		};
		mTimer = new Timer();
		//		mTimer.schedule( mTask , 1000 , 1000 );
		mTimer.scheduleAtFixedRate( mTask , 1000 , 1000 );
		//
		IntentFilter filter = new IntentFilter();
		//		filter.addAction( Intent.ACTION_TIME_TICK );
		filter.addAction( Intent.ACTION_TIME_CHANGED );
		filter.addAction( Intent.ACTION_DATE_CHANGED );
		context.registerReceiver( mTimeReceiver , filter );
	}
	
	@Override
	public Bitmap getBitmap()
	{
		// TODO Auto-generated method stub
		if( mBitmap == null || mBitmap.isRecycled() )
		{
			updateBitmap();
		}
		return mBitmap;
	}
	
	protected void updateBitmap()
	{
		//		Log.d( "MM" , "DeskClockProvider  updateBitmap" );
		int width = mBitmapBg.getWidth();
		int height = mBitmapBg.getHeight();
		Bitmap outbmp = Bitmap.createBitmap( width , height , Config.ARGB_8888 );
		Canvas canvas = new Canvas( outbmp );
		canvas.setDrawFilter( mDrawFilter );
		canvas.drawBitmap( mBitmapBg , 0 , 0 , null );
		int centerX = width / 2;
		int centerY = height / 2;
		// zhangjin@2016/07/19 DEL START
		//mTime.setToNow();
		// zhangjin@2016/07/19 DEL END
		//画时针		
		// zhangjin@2016/07/19 UPD START
		//float hourdegree = 30f * ( mTime.hour % 12 ) + 180;
		mCalendar.setTimeInMillis( System.currentTimeMillis() );
		mCalendar.setTimeZone( TimeZone.getDefault() );
		if( mBitmapHour != null )
		{
			float hourdegree = 30f * ( mCalendar.get( Calendar.HOUR_OF_DAY ) % 12 ) + 180;
			// zhangjin@2016/07/20 ADD START bug c_0004375
			hourdegree += mCalendar.get( Calendar.MINUTE ) * 0.5;
			// zhangjin@2016/07/20 ADD END
			// zhangjin@2016/07/19 UPD END
			canvas.save();
			canvas.rotate( hourdegree , centerX , centerY );
			canvas.drawBitmap( mBitmapHour , centerX - mBitmapHour.getWidth() / 2 , centerY , null );
			canvas.restore();
		}
		//分针
		// zhangjin@2016/07/19 UPD START
		//float mindegree = 6f * mTime.minute + 180;
		if( mBitmapMin != null )
		{
			float mindegree = 6f * mCalendar.get( Calendar.MINUTE ) + 180;
			// zhangjin@2016/07/19 UPD END
			canvas.save();
			canvas.rotate( mindegree , centerX , centerY );
			canvas.drawBitmap( mBitmapMin , centerX - mBitmapMin.getWidth() / 2 , centerY , null );
			canvas.restore();
		}
		//秒针
		// zhangjin@2016/07/19 UPD START
		//float secdegree = 6f * mTime.second + 180;
		if( mBitmapSec != null )
		{
			float secdegree = 6f * mCalendar.get( Calendar.SECOND ) + 180;
			// zhangjin@2016/07/19 UPD END
			canvas.save();
			canvas.rotate( secdegree , centerX , centerY );
			canvas.drawBitmap( mBitmapSec , centerX - mBitmapSec.getWidth() / 2 , centerY , null );
			canvas.restore();
		}
		//画圆心
		if( mBitmapCenter != null )
			canvas.drawBitmap( mBitmapCenter , centerX - mBitmapCenter.getWidth() / 2 , centerY - mBitmapCenter.getHeight() / 2 , null );
		//cheyingkun add start	//优化动态时钟bitmap释放
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.i( "" , "cyk mBitmap 0 : " + mBitmap );
		}
		//		Bitmap bmp = mBitmap;
		mRecycleBitmap.add( mBitmap );//zhujieping //mBitmap的释放在线程中，mBitmap更新使用是在ui线程，将需要释放的图片保存，等mBitmap在ui线程更新使用后，在ui线程释放，保持同步【c_0004692】
		mBitmap = outbmp;
		//zhujieping del start //mBitmap的释放在线程中，mBitmap更新使用是在ui线程，将需要释放的图片保存，等mBitmap在ui线程更新使用后，在ui线程释放，保持同步【c_0004692】
		//		if( bmp != mBitmap && bmp != null && bmp.isRecycled() == false )
		//		{
		//			bmp.recycle();
		//			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		//			{
		//				Log.d( "" , StringUtils.concat( "cyk mBitmap: " + mBitmap , "-mBitmap.isRecycled()=false" ) );
		//			}
		//			bmp = null;
		//		}
		//zhujieping del end
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.w( "" , "cyk mBitmap 1 : " + mBitmap );
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.e( "" , "cyk mBitmap 2 : " + mBitmap );
		}
		//cheyingkun add end
	}
	
	//zhujieping add  start//需求：桌面动态图标支持随主题变化
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		Resources res = mContext.getResources();
		Bitmap tempBitmapBg = mBitmapBg;
		Bitmap tempBitmapHour = mBitmapHour;
		Bitmap tempBitmapMin = mBitmapMin;
		Bitmap tempBitmapSec = mBitmapSec;
		Bitmap tempBitmapCenter = mBitmapCenter;
		mBitmapBg = getCurrentThemeBitmap( res , BMP_BG );
		mBitmapHour = getCurrentThemeBitmap( res , BMP_HOUR );
		mBitmapMin = getCurrentThemeBitmap( res , BMP_MIN );
		mBitmapSec = getCurrentThemeBitmap( res , BMP_SEC );
		mBitmapCenter = getCurrentThemeBitmap( res , BMP_CENTER );
		if( tempBitmapBg != null && tempBitmapBg != mBitmapBg && !tempBitmapBg.isRecycled() )
		{
			tempBitmapBg.recycle();
			tempBitmapBg = null;
		}
		if( tempBitmapHour != null && tempBitmapHour != mBitmapHour && !tempBitmapHour.isRecycled() )
		{
			tempBitmapHour.recycle();
			tempBitmapHour = null;
		}
		if( tempBitmapMin != null && tempBitmapMin != mBitmapMin && !tempBitmapMin.isRecycled() )
		{
			tempBitmapMin.recycle();
			tempBitmapMin = null;
		}
		if( tempBitmapSec != null && tempBitmapSec != mBitmapSec && !tempBitmapSec.isRecycled() )
		{
			tempBitmapSec.recycle();
			tempBitmapSec = null;
		}
		if( tempBitmapCenter != null && tempBitmapCenter != mBitmapCenter && !tempBitmapCenter.isRecycled() )
		{
			tempBitmapCenter.recycle();
			tempBitmapCenter = null;
		}
		updateBitmap();
		Message message = Message.obtain();
		message.what = 1;
		mHandler.sendMessage( message );
	}
	//zhujieping add  end
}
