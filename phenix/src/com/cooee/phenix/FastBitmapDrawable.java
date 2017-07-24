package com.cooee.phenix;


import android.animation.TimeInterpolator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class FastBitmapDrawable extends Drawable
{
	
	// zhangjin@2016/05/05 ADD START
	static final TimeInterpolator CLICK_FEEDBACK_INTERPOLATOR = new TimeInterpolator() {
		
		@Override
		public float getInterpolation(
				float input )
		{
			if( input < 0.05f )
			{
				return input / 0.05f;
			}
			else if( input < 0.3f )
			{
				return 1;
			}
			else
			{
				return ( 1 - input ) / 0.7f;
			}
		}
	};
	static final long CLICK_FEEDBACK_DURATION = 2000;
	// zhangjin@2016/05/05 ADD END
	private Bitmap mBitmap;
	private int mAlpha;
	private int mWidth;
	private int mHeight;
	private final Paint mPaint = new Paint( Paint.FILTER_BITMAP_FLAG );
	
	public FastBitmapDrawable(
			Bitmap b )
	{
		mAlpha = 255;
		mBitmap = b;
		if( b != null )
		{
			mWidth = mBitmap.getWidth();
			mHeight = mBitmap.getHeight();
		}
		else
		{
			mWidth = mHeight = 0;
		}
	}
	
	@Override
	public void draw(
			Canvas canvas )
	{
		//zhujieping add start	//添加保护（应用主题的过程中在PC机上安装应用，概率性出现重启【i_0013156】）
		if( mBitmap == null || mBitmap.isRecycled() )
		{
			return;
		}
		//zhujieping add end
		final Rect r = getBounds();
		// Draw the bitmap into the bounding rect
		//cheyingkun add start	//为bug c_0003076与c_0003080添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			if( mBitmap == null || mPaint == null || canvas == null || r == null )
			{
				Log.d( "cheyingkun - bug-c_0003076  FastBitmapDrawable : " , StringUtils.concat(
						"draw - [mBitmap == null]:" ,
						( mBitmap == null ) ,
						"-[mPaint == null]:" ,
						( mPaint == null ) ,
						"-[canvas == null]:" ,
						( canvas == null ) ,
						"-[r == null]:" ,
						( r == null ) ) );
				new Throwable().printStackTrace();
			}
			if( mBitmap != null && mBitmap.isRecycled() )
			{
				Log.v( "cheyingkun - bug-c_0003076  FastBitmapDrawable : " , "\n draw:mBitmap != null && mBitmap.isRecycled() " );
				new Throwable().printStackTrace();
			}
		}
		//cheyingkun add end
		// zhangjin@2015/09/02 ADD UPD
		//canvas.drawBitmap( mBitmap , null , r , mPaint );
		if( mBitmap.isRecycled() == false )
		{
			canvas.drawBitmap( mBitmap , null , r , mPaint );
		}
		// zhangjin@2015/09/02 ADD UPD
	}
	
	@Override
	public void setColorFilter(
			ColorFilter cf )
	{
		mPaint.setColorFilter( cf );
	}
	
	@Override
	public int getOpacity()
	{
		return PixelFormat.TRANSLUCENT;
	}
	
	@Override
	public void setAlpha(
			int alpha )
	{
		mAlpha = alpha;
		mPaint.setAlpha( alpha );
	}
	
	public void setFilterBitmap(
			boolean filterBitmap )
	{
		mPaint.setFilterBitmap( filterBitmap );
	}
	
	public int getAlpha()
	{
		return mAlpha;
	}
	
	@Override
	public int getIntrinsicWidth()
	{
		return mWidth;
	}
	
	@Override
	public int getIntrinsicHeight()
	{
		return mHeight;
	}
	
	@Override
	public int getMinimumWidth()
	{
		return mWidth;
	}
	
	@Override
	public int getMinimumHeight()
	{
		return mHeight;
	}
	
	public void setBitmap(
			Bitmap b )
	{
		mBitmap = b;
		if( b != null )
		{
			mWidth = mBitmap.getWidth();
			mHeight = mBitmap.getHeight();
		}
		else
		{
			mWidth = mHeight = 0;
		}
	}
	
	public Bitmap getBitmap()
	{
		return mBitmap;
	}
}
