/* 文件名: Tools.java 2014年8月26日
 * 
 * 描述:桌面对图片处理相关的工具类
 * 
 * 作者: cooee */
package com.cooee.util;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class Tools
{
	
	public static Bitmap getImageFromSDCardFile(
			final String foldname ,
			final String filename )
	{
		Bitmap image = null;
		String file = StringUtils.concat( Environment.getExternalStorageDirectory() , File.separator , foldname , File.separator , filename );
		try
		{
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 1;
			image = BitmapFactory.decodeFile( file , opts );
		}
		catch( Exception e )
		{
		}
		return image;
	}
	
	public static Bitmap getImageFromInStream(
			InputStream is )
	{
		Bitmap image = null;
		try
		{
			image = BitmapFactory.decodeStream( is );
		}
		catch( Exception e )
		{
		}
		return image;
	}
	
	public static Bitmap getImageFromInStream(
			InputStream is ,
			Bitmap.Config config )
	{
		Bitmap image = null;
		try
		{
			Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = config;
			image = BitmapFactory.decodeStream( is , null , opts );
		}
		catch( Exception e )
		{
		}
		finally
		{
			if( is != null )
			{
				try
				{
					is.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
				is = null;
			}
		}
		return image;
	}
	
	public static Drawable zoomDrawable(
			Drawable drawable ,
			float scale )
	{
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap( drawable ); // drawable转换成bitmap
		Matrix matrix = new Matrix(); // 创建操作图片用的Matrix对象
		matrix.postScale( scale , scale ); // 设置缩放比例
		Bitmap newbmp = Bitmap.createBitmap( oldbmp , 0 , 0 , width , height , matrix , true ); // 建立新的bitmap，其内容是对原bitmap的缩放后的图
		return new BitmapDrawable( newbmp ); // 把bitmap转换成drawable并返回
	}
	
	public static Drawable zoomDrawable(
			Drawable drawable ,
			int w ,
			int h ,
			boolean holdSize )
	{
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap( drawable ); // drawable转换成bitmap
		Matrix matrix = new Matrix(); // 创建操作图片用的Matrix对象
		float scaleWidth = ( (float)w / width ); // 计算缩放比例
		float scaleHeight = ( (float)h / height );
		matrix.postScale( scaleWidth , scaleHeight ); // 设置缩放比例
		Bitmap newbmp = Bitmap.createBitmap( oldbmp , 0 , 0 , width , height , matrix , true ); // 建立新的bitmap，其内容是对原bitmap的缩放后的图
		//cheyingkun add start	//是否优先获取高分辨率图标（图标显示清晰）。true为优先获取高分辨率图标，没有高分辨图标则获取低分辨率图标；false为直接获取当前分辨率图标。默认为false。
		if( holdSize )
		{
			Drawable iconDrawable = Utilities.createIconDrawable( newbmp );
			return iconDrawable;
		}
		//cheyingkun add end
		return new BitmapDrawable( newbmp ); // 把bitmap转换成drawable并返回
	}
	
	// drawable 转换成bitmap
	public static Bitmap drawableToBitmap(
			Drawable drawable )
	{
		int width = drawable.getIntrinsicWidth(); // 取drawable的长宽
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565; // 取drawable的颜色格式
		Bitmap bitmap = Bitmap.createBitmap( width , height , config ); // 建立对应bitmap
		Canvas canvas = new Canvas( bitmap ); // 建立对应bitmap的画布
		drawable.setBounds( 0 , 0 , width , height );
		drawable.draw( canvas ); // 把drawable内容画到画布中
		return bitmap;
	}
	
	/**
	 * 图片旋转
	 * 
	 * @param bmp
	 *            要旋转的图片
	 * @param degree
	 *            图片旋转的角度，负值为逆时针旋转，正值为顺时针旋转
	 * @return
	 */
	public static Bitmap rotateBitmap(
			Bitmap bmp ,
			float degree )
	{
		Matrix matrix = new Matrix();
		matrix.postRotate( degree );
		return Bitmap.createBitmap( bmp , 0 , 0 , bmp.getWidth() , bmp.getHeight() , matrix , true );
	}
	
	/**
	 * 图片缩放
	 * 
	 * @param bm
	 * @param scale
	 *            值小于则为缩小，否则为放大
	 * @return
	 */
	public static Bitmap resizeBitmap(
			Bitmap bm ,
			float scale )
	{
		//		Matrix matrix = new Matrix();
		//		matrix.postScale(scale, scale);
		if( scale == 1 )
			return bm;
		if( (int)bm.getWidth() * scale < 1 || (int)bm.getHeight() * scale < 1 )
		{
			return bm;
		}
		Bitmap tmp = Bitmap.createScaledBitmap( bm , (int)( bm.getWidth() * scale ) , (int)( bm.getHeight() * scale ) , true );
		bm.recycle();
		return tmp;
		//		return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
	}
	
	/**
	 * 图片缩放
	 * 
	 * @param bm
	 * @param w
	 *            缩小或放大成的宽
	 * @param h
	 *            缩小或放大成的高
	 * @return
	 */
	public static Bitmap resizeBitmap(
			Bitmap bm ,
			int w ,
			int h )
	{
		Bitmap BitmapOrg = bm;
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		if( width == w && height == h )
			return bm;
		float scaleWidth = ( (float)w ) / width;
		float scaleHeight = ( (float)h ) / height;
		Matrix matrix = new Matrix();
		matrix.postScale( scaleWidth , scaleHeight );
		Bitmap tmp = Bitmap.createBitmap( BitmapOrg , 0 , 0 , width , height , matrix , true );
		bm.recycle();
		return tmp;
	}
	
	/**
	 * 图片反转
	 * 
	 * @param bm
	 * @param flag
	 *            0为水平反转，1为垂直反转
	 * @return
	 */
	public static Bitmap reverseBitmap(
			Bitmap bmp ,
			int flag )
	{
		float[] floats = null;
		switch( flag )
		{
			case 0: // 水平反转
				floats = new float[]{ -1f , 0f , 0f , 0f , 1f , 0f , 0f , 0f , 1f };
				break;
			case 1: // 垂直反转
				floats = new float[]{ 1f , 0f , 0f , 0f , -1f , 0f , 0f , 0f , 1f };
				break;
		}
		if( floats != null )
		{
			Matrix matrix = new Matrix();
			matrix.setValues( floats );
			return Bitmap.createBitmap( bmp , 0 , 0 , bmp.getWidth() , bmp.getHeight() , matrix , true );
		}
		return null;
	}
	
	public static int dip2px(
			Context context ,
			float dipValue )
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)( dipValue * scale + 0.5f );
	}
	
	public static int px2dip(
			Context context ,
			float pxValue )
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)( pxValue / scale + 0.5f );
	}
	
	public static boolean isServiceRunning(
			Context context ,
			String className )
	{
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager)context.getSystemService( Context.ACTIVITY_SERVICE );
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices( 30 );
		if( serviceList.size() <= 0 )
		{
			return false;
		}
		for( int i = 0 ; i < serviceList.size() ; i++ )
		{
			if( serviceList.get( i ).service.getClassName().equals( className ) == true )
			{
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}
	
	//cheyingkun add start	//TCardMount
	/**
	 * Bitmap灰化处理  
	 * @param mBitmap
	 * @return
	 */
	public static Bitmap getGrayBitmap(
			Bitmap mBitmap )
	{
		if( mBitmap == null )
		{
			return null;
		}
		Bitmap mGrayBitmap = Bitmap.createBitmap( mBitmap.getWidth() , mBitmap.getHeight() , Config.ARGB_8888 );
		Canvas mCanvas = new Canvas( mGrayBitmap );
		Paint mPaint = new Paint();
		//创建颜色变换矩阵  
		ColorMatrix mColorMatrix = new ColorMatrix();
		//设置灰度影响范围  
		mColorMatrix.setSaturation( 0 );
		//创建颜色过滤矩阵  
		ColorMatrixColorFilter mColorFilter = new ColorMatrixColorFilter( mColorMatrix );
		//设置画笔的颜色过滤矩阵  
		mPaint.setColorFilter( mColorFilter );
		//使用处理后的画笔绘制图像  
		mCanvas.drawBitmap( mBitmap , 0 , 0 , mPaint );
		return mGrayBitmap;
	}
	//cheyingkun add end
	;
	
	/**
	 * 展讯双卡平台SIM卡是否准备好  请判断返回数组长度 以确定是不是双卡
	 * @param context
	 * @return
	 */
	public static boolean[] spreadSimReady(
			Context context )
	{
		int count = spreadSimCount( context );
		boolean[] simReady = new boolean[count];
		for( int i = 0 ; i < simReady.length ; i++ )
		{
			simReady[i] = true;
		}
		try
		{
			Class<?> c = Class.forName( "android.telephony.TelephonyManagerSprd" );
			Constructor constructor = c.getConstructor( Context.class );
			Object objectCopy = constructor.newInstance( context );
			Method m = c.getMethod( "getSimState" , int.class );
			for( int i = 0 ; i < simReady.length ; i++ )
			{
				simReady[i] = ( (Integer)m.invoke( objectCopy , i ) == TelephonyManager.SIM_STATE_READY );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "lvjiangbin" , StringUtils.concat( "spreadSimReady " , i , "=" , simReady[i] ) );
			}
		}
		catch( Exception e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "lvjiangbin" , "spreadSimReady err" );
			e.printStackTrace();
		}
		return simReady;
	}
	
	/**
	 * 展讯双卡平台SIM卡数量
	 * @param context
	 * @return
	 */
	public static int spreadSimCount(
			Context context )
	{
		int count = 1;
		try
		{
			Class<?> c = Class.forName( "android.telephony.TelephonyManagerSprd" );
			Constructor constructor = c.getConstructor( Context.class );
			Object objectCopy = constructor.newInstance( context );
			Method m = c.getMethod( "getPhoneCount" );
			count = (Integer)m.invoke( objectCopy );
		}
		catch( Exception e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "lvjiangbin" , "spreadSimCount err" );
			e.printStackTrace();
		}
		return count;
	}
	
	//cheyingkun add start	//应用名称逻辑完善(在所有Cache.put的时候,都先经过名称处理这段逻辑)【c_0004365】
	public static String appTitleFineTune(
			String appTitle )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_REMOVE_SPACE_IN_APP_TITLE )//cheyingkun add	//是否除去应用名称中的空格。true为去除空格；false为不去除空格。默认true。【c_0004348】
		{
			if( appTitle != null )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "" , StringUtils.concat( "cyk appTitleFineTune before: " , appTitle ) );
				// zhangjin@2016/04/19 ADD START
				appTitle = appTitle.toString().replaceAll( " " , "" ).trim();// 注意！！两个空格不一样
				// zhangjin@2016/04/19 ADD END
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.w( "" , StringUtils.concat( "cyk appTitleFineTune after: " , appTitle ) );
			}
		}
		return appTitle;
	}
	//cheyingkun add end
	;
	
	// gaominghui@2016/12/14 ADD START
	//xiatian del start	//整理判断“是否从左往右布局”的方法：由“mView.getLayoutDirection()”改为“getResources().getConfiguration().getLayoutDirection()”
	//	public static boolean isLayoutRTL(
	//			View view )
	//	{
	//		if( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 )
	//		{
	//			return false;
	//		}
	//		return( view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL );
	//	}
	//xiatian del end
	public static boolean bindAppWidgetId(
			AppWidgetManager widget ,
			int appWidgetId ,
			ComponentName provider )
	{
		if( widget == null || provider == null )
		{
			return false;
		}
		//api 15以下方法
		//内置发生错误，普通app可以通过
		if( Build.VERSION.SDK_INT >= 16 )
		{
			return widget.bindAppWidgetIdIfAllowed( appWidgetId , provider );
		}
		else
		{
			try
			{
				Class<?> clz = widget.getClass();
				Method method = clz.getMethod( "bindAppWidgetId" , int.class , ComponentName.class );
				method.invoke( widget , appWidgetId , provider );
				return true;
			}
			catch( NoSuchMethodException e )
			{
				e.printStackTrace();
				//Log.e( "andy" , "TOOLS bindAppWidgetId e1 = " + e );
				return false;
			}
			catch( IllegalArgumentException e )
			{
				e.printStackTrace();
				//Log.e( "andy" , "TOOLS bindAppWidgetId e2 = " + e );
				return false;
			}
			catch( IllegalAccessException e )
			{
				e.printStackTrace();
				//Log.e( "andy" , "TOOLS bindAppWidgetId e3 = " + e );
				return false;
			}
			catch( InvocationTargetException e )
			{
				e.printStackTrace();
				//Log.e( "andy" , "TOOLS bindAppWidgetId e4 = " + e );
				return false;
			}
		}
	}
	
	// gaominghui@2016/12/14 ADD END
	//zhujieping add start
	public static Bitmap readBitmapFromResourceId(
			Context context ,
			int resId )
	{
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		//获取资源图片  
		InputStream is = context.getResources().openRawResource( resId );
		return BitmapFactory.decodeStream( is , null , opt );
	}
	
	public static Bitmap BitmapToSmallBitamp(
			Bitmap bmp ,
			int maxWidth ,
			int maxHeight )
	{
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		float scale = 1f;
		float scaleW = (float)( w * 1.0 / maxWidth );
		float scaleH = (float)( h * 1.0 / maxHeight );
		if( scaleW > 1 || scaleH > 1 )
		{
			scale = Math.max( scaleW , scaleH );
		}
		w = (int)( w / scale );
		h = (int)( h / scale );
		return resizeBitmap( bmp , w , h );
	}
	
	public static Bitmap drawableToBitmap(
			Drawable drawable ,
			int width ,
			int height )
	{
		return createBitmap( width , height , drawable );
	}
	
	public static Bitmap drawableToSmallBitamp(
			Drawable drawable ,
			int maxWidth ,
			int maxHeight )
	{
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();
		float scale = 1f;
		float scaleW = (float)( w * 1.0 / maxWidth );
		float scaleH = (float)( h * 1.0 / maxHeight );
		if( scaleW > 1 || scaleH > 1 )
		{
			scale = Math.max( scaleW , scaleH );
		}
		w = (int)( w / scale );
		h = (int)( h / scale );
		return createBitmap( w , h , drawable );
	}
	
	private static Bitmap createBitmap(
			int w ,
			int h ,
			Drawable drawable )
	{
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap( w , h , config );
		//注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
		Canvas canvas = new Canvas( bitmap );
		drawable.setBounds( 0 , 0 , w , h );
		drawable.draw( canvas );
		return bitmap;
	}
	
	public static Bitmap inputStream2Bitmap(
			InputStream is ,
			int width ,
			int height )
	{
		Bitmap bmp = BitmapFactory.decodeStream( is );
		return Tools.resizeBitmap( bmp , width , height );
	}
	//zhujieping add end
	;
	
	//xiatian add start	//添加配置项“switch_enable_customer_lxt_change_custom_config_path”，是否支持客户“凌星通”定制的“切换本地化配置文件的文件夹”功能。true为支持，false为不支持。默认为false。
	public static boolean writeStringToFile(
			String mFileDir ,
			String mFileName ,
			String mFileContent )
	{
		if( TextUtils.isEmpty( mFileContent ) )
		{
			return false;
		}
		File destDir = new File( StringUtils.concat( Environment.getExternalStorageDirectory() , mFileDir ) );
		if( !destDir.exists() )
		{
			destDir.mkdirs();
		}
		File file = new File( StringUtils.concat( Environment.getExternalStorageDirectory() , mFileDir , File.separator , mFileName ) );
		FileWriter fw = null;
		BufferedWriter writer = null;
		try
		{
			fw = new FileWriter( file );
			writer = new BufferedWriter( fw );
			writer.write( mFileContent );
			writer.flush();
			return true;
		}
		catch( IOException e )
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			try
			{
				writer.close();
				fw.close();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}
	
	public static String readStringFromFile(
			String mFileFullPath )
	{
		File file = new File( StringUtils.concat( Environment.getExternalStorageDirectory() , mFileFullPath ) );
		if( file != null && file.exists() )
		{
			InputStreamReader input = null;
			try
			{
				String result = "";
				char[] temBuff = new char[512];
				input = new InputStreamReader( new FileInputStream( file ) );
				int len = 0;
				while( ( len = input.read( temBuff ) ) != -1 )
				{
					String tem = String.valueOf( temBuff );
					if( len < tem.length() )
					{
						tem = tem.substring( 0 , len );
					}
					result += tem;
				}
				return result;
			}
			catch( Exception e )
			{
			}
			finally
			{
				try
				{
					input.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	//xiatian add end
	;
}
