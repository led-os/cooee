package com.cooee.phenix.iconhouse.provider;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.cooee.phenix.R;


public class CalendarProviderDeChen extends CalendarProvider
{
	protected float WDAY_Y_RATE = 0.143f;
	protected float DAY_Y_RATE = 0.37f;
	protected int NUM_ELEVEN = R.drawable.icon_house_11;
	public CalendarProviderDeChen(
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
		if( mBitmap != null && mBitmap.isRecycled() == false )
		{
			mBitmap.recycle();
		}
		Bitmap bmp = BitmapFactory.decodeResource( mContext.getResources() , R.drawable.icon_house_default );
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap outbmp = Bitmap.createBitmap( width , height , Config.ARGB_8888 );
		Canvas canvas = new Canvas( outbmp );
		canvas.drawBitmap( bmp , 0 , 0 , null );
		bmp.recycle();
		//画周几
		mWeekDay = mTime.weekDay;
		Bitmap wbmp = BitmapFactory.decodeResource( mContext.getResources() , WDAY_BMP[mWeekDay] );
		int wdaypy = (int)( height * WDAY_Y_RATE );
		int wdaypx = ( width - wbmp.getWidth() ) / 2;
		canvas.drawBitmap( wbmp , wdaypx , wdaypy , null );
		wbmp.recycle();
		//画日期
		int daypy = (int)( height * DAY_Y_RATE );
		int daypx = 0;
		mMonthDay = mTime.monthDay;
		if( mMonthDay < 10 )
		{
			Bitmap daybmp = BitmapFactory.decodeResource( mContext.getResources() , NUM_BMP[mMonthDay] );
			daypx = ( width - daybmp.getWidth() ) / 2;
			canvas.drawBitmap( daybmp , daypx , daypy , null );
			daybmp.recycle();
		}
		else if( mMonthDay == 11 ) // 11号特殊绘制
		{
			Bitmap daybmp = BitmapFactory.decodeResource( mContext.getResources() , NUM_ELEVEN );
			daypx = ( width - daybmp.getWidth() ) / 2;
			canvas.drawBitmap( daybmp , daypx , daypy , null );
			daybmp.recycle();
		}
		else
		{
			int firstNum = mMonthDay / 10;
			int secNum = mMonthDay % 10;
			Bitmap dayFirst = BitmapFactory.decodeResource( mContext.getResources() , NUM_BMP[firstNum] );
			Bitmap daySec = BitmapFactory.decodeResource( mContext.getResources() , NUM_BMP[secNum] );
			daypx = ( width - dayFirst.getWidth() - daySec.getWidth() ) / 2;
			canvas.drawBitmap( dayFirst , daypx , daypy , null );
			dayFirst.recycle();
			daypx += dayFirst.getWidth();
			canvas.drawBitmap( daySec , daypx , daypy , null );
			daySec.recycle();
		}
		mBitmap = outbmp;
	}
}
