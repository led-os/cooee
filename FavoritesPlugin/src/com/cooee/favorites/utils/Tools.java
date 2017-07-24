/* 文件名: Tools.java 2014年8月26日
 * 
 * 描述:桌面对图片处理相关的工具类
 * 
 * 作者: cooee */
package com.cooee.favorites.utils;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.AndroidProcess;


public class Tools
{
	
	private static String TAG = "Favorites.Tools";
	private static AndroidProcess foreProc = null;
	
	public static Bitmap getImageFromSDCardFile(
			final String foldname ,
			final String filename )
	{
		Bitmap image = null;
		String file = Environment.getExternalStorageDirectory() + File.separator + foldname + File.separator + filename;
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
			int h )
	{
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap( drawable ); // drawable转换成bitmap
		Matrix matrix = new Matrix(); // 创建操作图片用的Matrix对象
		float scaleWidth = ( (float)w / width ); // 计算缩放比例
		float scaleHeight = ( (float)h / height );
		matrix.postScale( scaleWidth , scaleHeight ); // 设置缩放比例
		Bitmap newbmp = Bitmap.createBitmap( oldbmp , 0 , 0 , width , height , matrix , true ); // 建立新的bitmap，其内容是对原bitmap的缩放后的图
		return new BitmapDrawable( newbmp ); // 把bitmap转换成drawable并返回
	}
	
	// drawable 转换成bitmap
	public static Bitmap drawableToBitmap(
			Drawable drawable )
	{
		int width = drawable.getIntrinsicWidth(); // 取drawable的长宽
		int height = drawable.getIntrinsicHeight();
		return createBitmap( width , height , drawable );
	}
	
	/**
	 * 将drawable转化为小图
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToSmallBitamp(
			Drawable drawable )
	{
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();
		float scale = 1f;
		if( w > 200 && h > 200 )
		{
			scale = 0.5f;
		}
		w *= scale;
		h *= scale;
		return createBitmap( w , h , drawable );
	}
	
	/**
	 * 将drawable转化为小图,请注意资源回收 此方法不会释放原图内存
	 * @param drawable
	 * @return
	 */
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
	
	/**
	 * 将原图压缩成指定的dstWidth和dstHeight，为了确保不失真，对应的进行截图
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createBitmapByScale(
			Bitmap bitmap ,
			int dstWidth ,
			int dstHeight ,
			boolean recycle )
	{
		Bitmap scaleBitmap = null;
		Bitmap newBitmap = null;
		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		float bitmapScale = bitmapWidth / ( bitmapHeight * ( 1f ) );
		float dstScale = dstWidth / ( dstHeight * 1f );
		float scale = 1f;
		int width = 0;
		int height = 0;
		int x = 0;
		int y = 0;
		if( bitmapScale < dstScale )
		{
			//表示此时原图的高度大于缩放比的高度，截取高度,保持宽度一致
			scale = bitmapWidth * ( 1f ) / dstWidth;
		}
		else
		{//此时表示原图的宽度大于缩放比的宽度，截取宽度，保持高度一致
			scale = bitmapHeight * ( 1f ) / dstHeight;
		}
		//此时对于fWidth采取四舍五入的算法获得width的int值，Math.round 避免104.9999这种值被算成104的错误算法  wanghongjian add
		float fWidth = bitmapWidth / scale;
		float fHeight = bitmapHeight / scale;
		width = Math.round( fWidth );
		height = Math.round( fHeight );
		float fx = ( width - dstWidth ) / 2f;
		float fy = ( height - dstHeight ) / 2f;
		x = Math.round( fx );
		y = Math.round( fy );
		scaleBitmap = Bitmap.createScaledBitmap( bitmap , width , height , true );
		newBitmap = Bitmap.createBitmap( scaleBitmap , x , y , dstWidth , dstHeight );
		if( scaleBitmap != newBitmap )
		{
			scaleBitmap.recycle();
			scaleBitmap = null;
		}
		if( recycle )
		{
			bitmap.recycle();
			bitmap = null;
		}
		return newBitmap;
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
	public static String bitmaptoString(
			Bitmap bitmap )
	{
		// 将Bitmap转换成字符串  
		String string = null;
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		bitmap.compress( CompressFormat.PNG , 100 , bStream );
		byte[] bytes = bStream.toByteArray();
		string = Base64.encodeToString( bytes , Base64.DEFAULT );
		return string;
	}
	
	public static byte[] bitmaptoByte(
			Bitmap bitmap )
	{
		// 将Bitmap转换成字符串  
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		bitmap.compress( CompressFormat.PNG , 100 , bStream );
		byte[] bytes = bStream.toByteArray();
		return bytes;
	}
	
	public boolean isRoot()
	{
		Process process = null;
		DataOutputStream os = null;
		try
		{
			process = Runtime.getRuntime().exec( "su" );
			os = new DataOutputStream( process.getOutputStream() );
			os.writeBytes( "exit\n" );
			os.flush();
			process.waitFor();
		}
		catch( Exception e )
		{
			Log.d( "*** DEBUG ***" , "Unexpected error - Here is what I know: " + e.getMessage() );
			return false;
		}
		finally
		{
			try
			{
				if( os != null )
				{
					os.close();
				}
				process.destroy();
			}
			catch( Exception e )
			{
				// nothing
			}
		}
		return true;
	}
	
	public static int getNavigationBarHeight(
			Context context )
	{
		int result = 0;
		int resourceId = context.getResources().getIdentifier( "navigation_bar_height" , "dimen" , "android" );
		if( resourceId > 0 )
		{
			result = context.getResources().getDimensionPixelSize( resourceId );
		}
		return result;
	}
	
	public static int getStatusBarHeight(
			Context context )
	{
		int result = 0;
		int resourceId = context.getResources().getIdentifier( "status_bar_height" , "dimen" , "android" );
		if( resourceId > 0 )
		{
			result = context.getResources().getDimensionPixelSize( resourceId );
		}
		return result;
	}
	
	public static int getWidthPx(
			Context context )
	{
		WindowManager wm = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE );
		Display display = wm.getDefaultDisplay();
		Point p = new Point();
		display.getSize( p );
		return p.x;
	}
	
	public static int getHeightPx(
			Context context )
	{
		WindowManager wm = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE );
		Display display = wm.getDefaultDisplay();
		Point p = new Point();
		display.getSize( p );
		return p.y;
	}
	
	private static boolean isForegroundChange()
	{
		if( foreProc == null )
		{
			return true;
		}
		try
		{
			AndroidProcess lastprocess = new AndroidAppProcess( foreProc.pid );
			if( lastprocess.oom_score() == foreProc.oom_score() && lastprocess.name.equals( foreProc.name ) && lastprocess.oom_adj() == 0 )
			{
				//Log.d( TAG , " foreProc.name " + foreProc.name + " foregroundPid " + foreProc.pid );
				return false;
			}
		}
		catch( Exception e )
		{
			Log.d( TAG , "isForegroundChange" );
			return true;
		}
		return true;
	}
	
	public static String getForegroundApp()
	{
		if( isForegroundChange() == false )
		{
			Log.d( TAG , " getForegroundApp isForegroundChange  false" );
			return foreProc.name;
		}
		File[] files = new File( "/proc" ).listFiles();
		int lowestOomScore = Integer.MAX_VALUE;
		String foregroundProcess = null;
		int foregroundPid = 0;
		for( File file : files )
		{
			if( !file.isDirectory() )
			{
				continue;
			}
			int pid;
			try
			{
				pid = Integer.parseInt( file.getName() );
			}
			catch( NumberFormatException e )
			{
				continue;
			}
			try
			{
				String cgroup = read( String.format( "/proc/%d/cgroup" , pid ) );
				if( cgroup.contains( "bg_non_interactive" ) )
				{
					continue;
				}
				String cmdline = read( String.format( "/proc/%d/cmdline" , pid ) );
				cmdline = cmdline.trim();
				if( cmdline.contains( "com.android.systemui" ) || cmdline.contains( "qihoo" ) || cmdline.contains( "android.process" ) )
				{
					continue;
				}
				if( cmdline.contains( "/" ) )
				{
					continue;
				}
				int begin = cgroup.indexOf( "uid_" );
				if( begin == -1 )
				{
					continue;
				}
				int end = cgroup.indexOf( "/" , begin );
				int uid = Integer.parseInt( cgroup.substring( begin , end ).replace( "uid_" , "" ) );
				// u{user_id}_a{app_id} is used on API 17+ for multiple user account support.
				// String uidName = String.format("u%d_a%d", userId, appId);
				File oomScoreAdj = new File( String.format( "/proc/%d/oom_score_adj" , pid ) );
				if( oomScoreAdj.canRead() )
				{
					int oomAdj = Integer.parseInt( read( oomScoreAdj.getAbsolutePath() ) );
					if( oomAdj != 0 )
					{
						continue;
					}
				}
				int oomscore = Integer.parseInt( read( String.format( "/proc/%d/oom_score" , pid ) ) );
				if( oomscore < lowestOomScore )
				{
					//					Log.d( TAG , " oomscore " + oomscore );
					//					Log.d( TAG , "cmdline " + cmdline );
					//					Log.d( TAG , " cgroup " + cgroup );
					//					Log.d( TAG , "uid " + uid );
					foregroundPid = pid;
					lowestOomScore = oomscore;
					foregroundProcess = cmdline;
				}
			}
			catch( Exception e )
			{
				Log.d( TAG , "pid " + pid );
				Log.d( TAG , " exception  " + e.toString() );
			}
		}
		try
		{
			if( foregroundPid != 0 )
			{
				foreProc = new AndroidProcess( foregroundPid );
			}
		}
		catch( Exception e )
		{
			Log.d( TAG , "pid " + foreProc );
		}
		Log.d( TAG , " foreProc.name " + foreProc.name + " foregroundPid " + foreProc.pid );
		if( foreProc != null )
		{
			return foreProc.name;
		}
		else
		{
			return null;
		}
	}
	
	private static String read(
			String path ) throws IOException
	{
		StringBuilder output = new StringBuilder();
		BufferedReader reader = new BufferedReader( new FileReader( path ) );
		output.append( reader.readLine() );
		for( String line = reader.readLine() ; line != null ; line = reader.readLine() )
		{
			output.append( '\n' ).append( line );
		}
		reader.close();
		return output.toString();
	}
	
	static public int getFontHeight(
			float fontSize )
	{
		Paint paint = new Paint();
		paint.setTextSize( fontSize );
		FontMetrics fm = paint.getFontMetrics();
		return (int)Math.ceil( fm.descent - fm.ascent );
	}
}
