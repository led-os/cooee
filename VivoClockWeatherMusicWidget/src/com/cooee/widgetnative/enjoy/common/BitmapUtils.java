package com.cooee.widgetnative.enjoy.common;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;


public class BitmapUtils
{
	
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
	 * 图片合成
	 *
	 * @param srcBitmap
	 * @param dstBitmap
	 * @return
	 */
	public static Bitmap onCompositeImages(
			Bitmap bgBitmap ,
			Bitmap iconBitmap ,
			Bitmap maskBitmap )
	{
		if( iconBitmap == null || maskBitmap == null )
		{
			return null;
		}
		Bitmap bmp = null;
		int width = bgBitmap.getWidth();
		int height = bgBitmap.getHeight();
		bmp = Bitmap.createBitmap( width , height , bgBitmap.getConfig() );
		final Paint paint = new Paint();
		final Canvas canvas = new Canvas( bmp );
		//先画背板
		canvas.drawBitmap( bgBitmap , 0 , 0 , paint );
		int saveLayer = canvas.saveLayer(
				0 ,
				0 ,
				width ,
				height ,
				null ,
				Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG );
		//画图片
		int mOffsetX = ( width - iconBitmap.getWidth() ) / 2;
		int mOffsetY = ( height - iconBitmap.getHeight() ) / 2;
		canvas.drawBitmap( iconBitmap , mOffsetX , mOffsetY , paint );
		//画蒙版
		paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.DST_IN ) );
		canvas.drawBitmap( maskBitmap , mOffsetX , mOffsetY , paint );
		paint.setXfermode( null );
		canvas.restoreToCount( saveLayer );
		return bmp;
	}
	
	public static void saveMyBitmap(
			String bitName ,
			Bitmap mBitmap )
	{
		File f = new File( "/sdcard/" + bitName + ".png" );
		try
		{
			f.createNewFile();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			//			DebugMessage.put( "在保存图片时出错：" + e.toString() );
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
}
