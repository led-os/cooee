package com.cooee.phenix.iconhouse.provider;


import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class DeskClockProviderDeChen extends DeskClockProvider
{
	
	public DeskClockProviderDeChen(
			Context context )
	{
		super( context );
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
		float hourdegree = 30f * ( mCalendar.get( Calendar.HOUR_OF_DAY ) % 12 ) + 180;
		// zhangjin@2016/07/20 ADD START bug c_0004375
		hourdegree += mCalendar.get( Calendar.MINUTE ) * 0.5;
		// zhangjin@2016/07/20 ADD END
		// zhangjin@2016/07/19 UPD END
		canvas.save();
		canvas.rotate( hourdegree , centerX , centerY );
		canvas.drawBitmap( mBitmapHour , centerX - mBitmapHour.getWidth() / 2 , centerY , null );
		canvas.restore();
		//分针
		// zhangjin@2016/07/19 UPD START
		//float mindegree = 6f * mTime.minute + 180;
		float mindegree = 6f * mCalendar.get( Calendar.MINUTE ) + 180;
		// zhangjin@2016/07/19 UPD END
		canvas.save();
		canvas.rotate( mindegree , centerX , centerY );
		canvas.drawBitmap( mBitmapMin , centerX - mBitmapMin.getWidth() / 2 , centerY , null );
		canvas.restore();
		//秒针
		float secdegree = 6f * mCalendar.get( Calendar.SECOND ) + 180;
		canvas.save();
		canvas.rotate( secdegree , centerX , centerY );
		canvas.drawBitmap( mBitmapSec , centerX - mBitmapSec.getWidth() / 2 , centerY - ( mBitmapSec.getHeight() / 9 ) , null );
		canvas.restore();
		//cheyingkun add start	//优化动态时钟bitmap释放
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.i( "" , "cyk mBitmap 0 : " + mBitmap );
		}
		if( mBitmap != null && mBitmap.isRecycled() == false )
		{
			mBitmap.recycle();
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.d( "" , StringUtils.concat( "cyk mBitmap: " + mBitmap , "-mBitmap.isRecycled()=false" ) );
			}
			mBitmap = null;
		}
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.w( "" , "cyk mBitmap 1 : " + mBitmap );
		}
		mBitmap = outbmp;
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.e( "" , "cyk mBitmap 2 : " + mBitmap );
		}
		//cheyingkun add end
	}
}
