package com.cooee.phenix.iconhouse.provider;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.theme.ThemeManager;


public class CalendarProvider extends IconHouseProvider
{
	
	
	protected float WDAY_Y_RATE = 0.815f;
	protected float DAY_Y_RATE = 0.238f;
	protected int[] NUM_BMP = new int[]{
			R.drawable.icon_house_0 ,
			R.drawable.icon_house_1 ,
			R.drawable.icon_house_2 ,
			R.drawable.icon_house_3 ,
			R.drawable.icon_house_4 ,
			R.drawable.icon_house_5 ,
			R.drawable.icon_house_6 ,
			R.drawable.icon_house_7 ,
			R.drawable.icon_house_8 ,
			R.drawable.icon_house_9 };
	//protected int NUM_ELEVEN = R.drawable.icon_house_11;//zhujieping del//这个是德晨需求，放到CalendarProviderDeChen中
	protected int[] WDAY_BMP = new int[]{
			R.drawable.icon_house_sun ,
			R.drawable.icon_house_mon ,
			R.drawable.icon_house_tue ,
			R.drawable.icon_house_wed ,
			R.drawable.icon_house_the ,
			R.drawable.icon_house_fri ,
			R.drawable.icon_house_sat , };
	protected Context mContext;
	protected Paint mPaint;
	protected int mMonthDay = 0;
	protected int mWeekDay = 0;
	protected Time mTime;
	private Handler mHandler;//zhujieping add  //需求：桌面动态图标支持随主题变化

	protected final BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			//			String action = intent.getAction();
			//			Log.d( "MM" , " action " + action );
			//			if( action.equals( Intent.ACTION_TIME_TICK ) )
			//			{
			//			Log.d( "MM" , " ACTION_TIME_TICK " );
			mTime.set( System.currentTimeMillis() );
			if( mMonthDay == mTime.monthDay && mWeekDay == mTime.weekDay )
			{
				return;
			}
			//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.w( "cyk_bug : c_0004400" , " CalendarProvider onReceive " );
			}
			//cheyingkun add end
			updateBitmap();
			performUpdate();
			//			}
		}
	};
	
	public CalendarProvider(
			Context context )
	{
		mContext = context;
		mPaint = new Paint();
		mPaint.setColor( 0x55ff0000 );
		mPaint.setStyle( Paint.Style.STROKE );
		mTime = new Time();
		mTime.setToNow();
		IntentFilter filter = new IntentFilter();
		filter.addAction( Intent.ACTION_TIME_TICK );
		filter.addAction( Intent.ACTION_TIME_CHANGED );
		filter.addAction( Intent.ACTION_DATE_CHANGED );
		context.registerReceiver( mTimeReceiver , filter );
		//zhujieping add start //需求：桌面动态图标支持随主题变化
		//zhujieping add start //增加配置控制动态日历的星期、日期的显示位置
		WDAY_Y_RATE = Float.parseFloat( ThemeManager.getInstance().getString( "calendar_week_y_rate" , LauncherDefaultConfig.getString( R.string.calendar_week_y_rate ) ) );
		DAY_Y_RATE = Float.parseFloat( ThemeManager.getInstance().getString( "calendar_day_y_rate" , LauncherDefaultConfig.getString( R.string.calendar_day_y_rate ) ) );
		//zhujieping add end
		mHandler = new Handler( Looper.getMainLooper() ) {
			
			
			@Override
			public void handleMessage(
					Message msg )
			{
				// TODO Auto-generated method stub
				performUpdate();
			}
		};
		themePath = "theme/icon/80/dynamicicon/calendar/";
		//zhujieping add end
	}
	
	@Override
	public Bitmap getBitmap()
	{
		//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.i( "cyk_bug : c_0004400" , " CalendarProvider getBitmap  updateBitmap 0 " );
		}
		//cheyingkun add end
		// TODO Auto-generated method stub
		if( mBitmap == null || mBitmap.isRecycled() )
		{
			//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.d( "cyk_bug : c_0004400" , " CalendarProvider getBitmap  updateBitmap 1 " );
			}
			//cheyingkun add end
			updateBitmap();
		}
		//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.w( "cyk_bug : c_0004400" , " CalendarProvider getBitmap  updateBitmap 2 " );
		}
		//cheyingkun add end
		return mBitmap;
	}
	
	protected void updateBitmap()
	{
		if( mBitmap != null && mBitmap.isRecycled() == false )
		{
			mRecycleBitmap.add( mBitmap );
		}
		Bitmap bmp = getCurrentThemeBitmap( mContext.getResources() , R.drawable.icon_house_default );
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap outbmp = Bitmap.createBitmap( width , height , Config.ARGB_8888 );
		Canvas canvas = new Canvas( outbmp );
		canvas.drawBitmap( bmp , 0 , 0 , null );
		bmp.recycle();
		//画周几
		mWeekDay = mTime.weekDay;
		//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.w( "cyk_bug : c_0004400" , StringUtils.concat( " CalendarProvider getBitmap  updateBitmap mWeekDay: " , mWeekDay ) );
		}
		//cheyingkun add end
		Bitmap wbmp = getCurrentThemeBitmap( mContext.getResources() , WDAY_BMP[mWeekDay] );
		if( wbmp != null )
		{
			int wdaypy = (int)( height * WDAY_Y_RATE );
			int wdaypx = ( width - wbmp.getWidth() ) / 2;
			canvas.drawBitmap( wbmp , wdaypx , wdaypy , null );
			wbmp.recycle();
			wbmp = null;
		}
		//画日期
		int daypy = (int)( height * DAY_Y_RATE );
		int daypx = 0;
		mMonthDay = mTime.monthDay;
		//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.w( "cyk_bug : c_0004400" , StringUtils.concat( " CalendarProvider getBitmap  updateBitmap mMonthDay: " , mMonthDay ) );
		}
		//cheyingkun add end
		if( mMonthDay < 10 )
		{
			Bitmap daybmp = getCurrentThemeBitmap( mContext.getResources() , NUM_BMP[mMonthDay] );
			daypx = ( width - daybmp.getWidth() ) / 2;
			canvas.drawBitmap( daybmp , daypx , daypy , null );
			daybmp.recycle();
		}
		else
		{
			int firstNum = mMonthDay / 10;
			int secNum = mMonthDay % 10;
			Bitmap dayFirst = getCurrentThemeBitmap( mContext.getResources() , NUM_BMP[firstNum] );
			Bitmap daySec = getCurrentThemeBitmap( mContext.getResources() , NUM_BMP[secNum] );
			daypx = ( width - dayFirst.getWidth() - daySec.getWidth() ) / 2;
			canvas.drawBitmap( dayFirst , daypx , daypy , null );
			dayFirst.recycle();
			daypx += dayFirst.getWidth();
			canvas.drawBitmap( daySec , daypx , daypy , null );
			daySec.recycle();
		}
		mBitmap = outbmp;
		//cheyingkun add start	//为bug c_0004400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.w( "cyk_bug : c_0004400" , StringUtils.concat( " CalendarProvider getBitmap  updateBitmap mBitmap: " + mBitmap + " mBitmap.isRecycled(): " , mBitmap.isRecycled() ) );
		}
		//cheyingkun add end
	}
	
	//zhujieping add start //需求：桌面动态图标支持随主题变化
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		WDAY_Y_RATE = Float.parseFloat( ThemeManager.getInstance().getString( "calendar_week_y_rate" , LauncherDefaultConfig.getString( R.string.calendar_week_y_rate ) ) );
		DAY_Y_RATE = Float.parseFloat( ThemeManager.getInstance().getString( "calendar_day_y_rate" , LauncherDefaultConfig.getString( R.string.calendar_day_y_rate ) ) );
		updateBitmap();
		Message message = Message.obtain();
		message.what = 1;
		mHandler.sendMessage( message );
	}
	//zhujieping add end
}
