package com.cooee.widgetnative.ALL3in1.Photo.utils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;


public class BitmapUtils
{
	
	/**
	 * bitmap通过宽高缩放系数来进行等比例缩放
	 *
	 * @param bitmap
	 * @param scale_width
	 * @param scale_height
	 * @return
	 */
	public static Bitmap adaptiveByScaleFactor(
			Bitmap bitmap ,
			float scale_width ,
			float scale_height )
	{
		Matrix matrix = new Matrix();
		int width = bitmap.getWidth();// 获取资源位图的宽
		int height = bitmap.getHeight();// 获取资源位图的高
		matrix.postScale( scale_width , scale_height );// 获取缩放比例
		// 根据缩放比例获取新的位图
		Bitmap newbmp = Bitmap.createBitmap( bitmap , 0 , 0 , width , height , matrix , true );
		//		if( bitmap != newbmp )
		//			bitmap.recycle();
		return newbmp;
	}
	
	/**
	 * bitmap通过宽高缩放系数来进行等比例缩放
	 *
	 * @param bitmap
	 * @param scale_width
	 * @param scale_height
	 * @return
	 */
	public static Bitmap adaptiveByScaleFactor(
			Bitmap bitmap ,
			float scale_width ,
			float scale_height ,
			boolean isUpperHalf )
	{
		Matrix matrix = new Matrix();
		int width = bitmap.getWidth();// 获取资源位图的宽
		int height = bitmap.getHeight() / 2;// 获取资源位图的高
		matrix.postScale( scale_width , scale_height );// 获取缩放比例
		int startY = 0;
		if( !isUpperHalf )
			startY = height;
		// 根据缩放比例获取新的位图
		Bitmap newbmp = Bitmap.createBitmap( bitmap , 0 , startY , width , height , matrix , true );
		return newbmp;
	}
	
	public static Bitmap adaptive(
			Bitmap bitmap ,
			float newWidth ,
			float newHeight )
	{
		Matrix matrix = new Matrix();
		int width = bitmap.getWidth();// 获取资源位图的宽
		int height = bitmap.getHeight();// 获取资源位图的高
		float w = newWidth / width;
		float h = newHeight / height;
		matrix.postScale( w , h );// 获取缩放比例
		// 根据缩放比例获取新的位图
		Bitmap newbmp = Bitmap.createBitmap( bitmap , 0 , 0 , width , height , matrix , true );
		return newbmp;
	}
	
	/**
	 * 裁剪bitmap
	 *
	 * @param bitmap
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap cutBitmap(
			Bitmap bitmap ,
			int x ,
			int y ,
			int width ,
			int height )
	{
		Bitmap newbmp = Bitmap.createBitmap( bitmap , x , y , width , height );
		return newbmp;
	}
	
	/**
	 * @brief 保存Bitmap至文件
	 * @param bm
	 *            Bitmap
	 * @param path
	 *            图片路径
	 * @return 成功与否
	 */
	public static void compressBitmap(
			Bitmap mBitmap ,
			String bitName )
	{
		File f = new File( "/sdcard/" + bitName + ".png" );
		try
		{
			f.createNewFile();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
		}
		FileOutputStream fOut = null;
		try
		{
			fOut = new FileOutputStream( f );
		}
		catch( FileNotFoundException e )
		{
			e.printStackTrace();
		}
		mBitmap.compress( Bitmap.CompressFormat.PNG , 100 , fOut );
		try
		{
			fOut.flush();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		try
		{
			fOut.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 图片合成
	 *
	 * @param srcBitmap
	 * @param dstBitmap
	 * @return
	 */
	public static Bitmap onCompositeImages(
			Bitmap srcBitmap ,
			Bitmap dstBitmap ,
			PorterDuff.Mode mode )
	{
		if( srcBitmap == null || dstBitmap == null )
		{
			return null;
		}
		Bitmap bmp = null;
		bmp = Bitmap.createBitmap( srcBitmap.getWidth() , srcBitmap.getHeight() , srcBitmap.getConfig() );
		final Paint paint = new Paint();
		final Canvas canvas = new Canvas( bmp );
		canvas.drawBitmap( srcBitmap , 0 , 0 , paint );
		paint.setXfermode( new PorterDuffXfermode( mode ) );
		canvas.drawBitmap( dstBitmap , 0 , 0 , paint );
		return bmp;
	}
	
	/**
	 * 图片合成，取上半张或下半张图片
	 * @param srcBitmap 源图片
	 * @param dstBitmap 蒙板图片
	 * @param mode 与蒙板的合成方式
	 * @param isUpperHalf 是否为上半张图片
	 * @return
	 * @author yangtianyu 2016-1-27
	 */
	public static Bitmap onCompositeImages(
			Bitmap srcBitmap ,
			Bitmap dstBitmap ,
			PorterDuff.Mode mode ,
			boolean isUpperHalf )
	{
		if( srcBitmap == null || dstBitmap == null )
		{
			return null;
		}
		Bitmap bmp = null;
		bmp = Bitmap.createBitmap( srcBitmap.getWidth() , srcBitmap.getHeight() / 2 , Config.ARGB_8888 );
		final Paint paint = new Paint();
		final Canvas canvas = new Canvas( bmp );
		int top = isUpperHalf ? 0 : srcBitmap.getHeight() / 2;
		Rect src = new Rect( 0 , top , srcBitmap.getWidth() , top + srcBitmap.getHeight() / 2 );
		Rect dst = new Rect( 0 , 0 , bmp.getWidth() , bmp.getHeight() );
		canvas.drawBitmap( srcBitmap , src , dst , paint );
		paint.setXfermode( new PorterDuffXfermode( mode ) );
		canvas.drawBitmap( dstBitmap , src , dst , paint );
		return bmp;
	}
	
	/**
	
	 * add shadow to bitmap
	 *
	 * @param originalBitmap
	 * @return
	 */
	public static Bitmap drawImageDropShadow(
			Bitmap originalBitmap ,
			String Color )
	{
		BlurMaskFilter blurFilter = new BlurMaskFilter( 1 , BlurMaskFilter.Blur.NORMAL );
		Paint shadowPaint = new Paint();
		shadowPaint.setAlpha( 50 );
		shadowPaint.setColor( android.graphics.Color.parseColor( Color ) );
		shadowPaint.setMaskFilter( blurFilter );
		int[] offsetXY = new int[2];
		Bitmap shadowBitmap = originalBitmap.extractAlpha( shadowPaint , offsetXY );
		Bitmap shadowImage32 = shadowBitmap.copy( Bitmap.Config.ARGB_8888 , true );
		Canvas c = new Canvas( shadowImage32 );
		c.drawBitmap( originalBitmap , offsetXY[0] , offsetXY[1] , null );
		return shadowImage32;
	}
}
