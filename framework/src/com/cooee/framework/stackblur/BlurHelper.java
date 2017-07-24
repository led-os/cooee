package com.cooee.framework.stackblur;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.framework.wallpaper.WallpaperOffsetManager;
import com.cooee.launcher.framework.R;


/**
 * 模糊效果的帮助接口
 * 
 */
public final class BlurHelper
{
	
	private static final String TAG = "BlurHelper";
	private static Lock lock = new ReentrantLock();
	
	/**
	 *回调接口
	 */
	public interface BlurCallbacks
	{
		
		public void blurCompleted(
				Bitmap bluredBitmap );
	}
	
	/**
	 * 在非 UI线程里对view进行模糊操作，操作完成后会将模糊后的Bitmap通过BlurCallbacks接口回传，
	 * 所以请实现BlurCallbacks回调接口获取最终模糊的bitmap
	 * 
	 * @param context 
	 * @param oriView 需要模糊的view
	 * @param option 选项
	 */
	public static void blurViewNonUiTread(
			final Context context ,
			final View oriView ,
			final BlurOptions option )
	{
		if( context == null || oriView == null || option == null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "blurViewNonUiTread param is missing" );
			return;
		}
		blurView( context , oriView , option );
	}
	
	/**
	 * 在非 UI线程里对壁纸进行模糊操作，操作完成后会将模糊后的Bitmap通过BlurCallbacks接口回传，
	 * 所以请实现BlurCallbacks回调接口获取最终模糊的bitmap
	 * 
	 * @param context 
	 * @param option 选项
	 */
	public static void blurWallpaperNonUiTread(
			final Context context ,
			final BlurOptions option )
	{
		if( context == null || option == null || option.src == null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "blurViewNonUiTread param is missing" );
		}
		blurView( context , null , option );
	}
	
	/**
	 * 模糊接口，对原始oriBitmap进行模糊，返回模糊后的bitmap
	 * 
	 * @param oriBitmap 需要模糊的bitmap
	 */
	public static Bitmap fastBlur(
			Bitmap oriBitmap ,
			BlurOptions option )
	{
		return fastBlur( oriBitmap , option.scaleFactor , option.radius , new Paint( Paint.FILTER_BITMAP_FLAG ) );
	}
	
	/**
	 * 模糊接口，对原始oriBitmap进行模糊
	 * 在非 UI线程里对原始oriBitmap进行模糊，操作完成后会将模糊后的Bitmap通过BlurCallbacks接口回传，
	 * 所以请实现BlurCallbacks回调接口获取最终模糊的bitmap，

	 * @param oriBitmap
	 */
	public static void fastBlurNonUiThread(
			final Bitmap oriBitmap ,
			final BlurOptions option )
	{
		new AsyncTask<Bitmap , Void , Bitmap>() {
			
			@Override
			protected Bitmap doInBackground(
					Bitmap ... params )
			{
				return fastBlur( params[0] , option.scaleFactor , option.radius , new Paint( Paint.FILTER_BITMAP_FLAG ) );
			}
			
			@Override
			protected void onPostExecute(
					Bitmap result )
			{
				super.onPostExecute( result );
				if( option.callbacks != null )
				{
					option.callbacks.blurCompleted( result );
				}
			}
		}.execute( oriBitmap );
	}
	
	/**
	 * 模糊接口，对原始oriBitmap进行模糊，返回模糊后的bitmap
	 * 
	 * @param oriBitmap 需要模糊的bitmap
	 * @param scaleFactor 对oriView的缩放因子，必须
	 *            >=1f。对于模糊算法的一种优化，现将需要模糊的oriView进行缩放，然后进行模糊，减少计算量，增大效率，但是会削弱效果
	 * @param radius 模糊半径，必须 >=1
	 * @param paint 可以为null,或者Paint.FILTER_BITMAP_FLAG 进行抗锯齿
	 */
	public static Bitmap fastBlur(
			Bitmap oriBitmap ,
			float scaleFactor ,
			int radius ,
			Paint paint )
	{
		Bitmap bluredBitmap = null;
		//		if( scaleFactor > 1f )
		//		{
		//			Bitmap overlayBitmap = Bitmap.createBitmap( (int)( oriBitmap.getWidth() / scaleFactor ) , (int)( oriBitmap.getHeight() / scaleFactor ) , Bitmap.Config.ARGB_8888 );
		//			Canvas blurCanvas = new Canvas( overlayBitmap );
		//			blurCanvas.scale( 1 / scaleFactor , 1 / scaleFactor );
		//			blurCanvas.drawBitmap( oriBitmap , 0 , 0 , paint );
		//			bluredBitmap = FastBlur.doBlur( overlayBitmap , radius , true );
		//		}
		//		else
		//		{
		bluredBitmap = FastBlur.doBlur( oriBitmap , radius , true );
		//		}
		return bluredBitmap;
	}
	
	private static void blurView(
			final Context context ,
			View oriView ,
			final BlurOptions option )
	{
		lock.lock();
		if( option.src == null )
		{
			option.src = getRectByView( oriView );
		}
		Rect src = option.src;
		boolean isCaptureWallPaper = option.captureWallPaper;
		//如果是截壁纸并且是动态壁纸，则不模糊。
		if( isCaptureWallPaper && isLiveWallpaper( context ) )
		{
			new AsyncTask<Void , Void , Void>() {
				
				@Override
				protected Void doInBackground(
						Void ... params )
				{
					return null;
				}
				
				@Override
				protected void onPostExecute(
						Void result )
				{
					super.onPostExecute( result );
					if( option.callbacks != null )
					{
						Bitmap defaultBlurWallpaperBitmap = getDefaultBlurWallpaperBitmap( context );//cheyingkun add	//动态壁纸返回默认模糊背景
						option.callbacks.blurCompleted( defaultBlurWallpaperBitmap );
					}
				}
			}.execute();
			lock.unlock();
			return;
		}
		int width = src.width();
		int height = src.height();
		Bitmap oriBitmap = null;
		try
		{
			oriBitmap = Bitmap.createBitmap( (int)( width / option.scaleFactor ) , (int)( height / option.scaleFactor ) , Bitmap.Config.ARGB_8888 );
		}
		catch( Exception e )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "BlurHelper" , "blurView err" );
			return;
		}
		Canvas captureCanvas = new Canvas( oriBitmap );
		final Paint paint = new Paint( Paint.FILTER_BITMAP_FLAG );
		//width和height可能等于0，调用Bitmap.createBitmap的方法会抛出异常。
		boolean captureSuccess = false;
		Matrix matrix = new Matrix();
		matrix.setScale( (float)( 1 / option.scaleFactor ) , (float)( 1 / option.scaleFactor ) );
		captureCanvas.setMatrix( matrix );
		if( oriView != null && oriView.getMeasuredWidth() != 0 && oriView.getMeasuredWidth() != 0 )
		{
			// The first time to capture screen
			captureSuccess = captureViewBitmapToCanvas( context , oriView , captureCanvas , paint , src , isCaptureWallPaper );
			// we make sure that capture screen success , so try again !
			if( !captureSuccess )
			{
				captureSuccess = captureViewBitmapToCanvas2( context , oriView , captureCanvas , paint , src , isCaptureWallPaper );
			}
		}
		else
		{
			// 首次启动可能view还未初始化完成，确保背景模糊效果
			if( isCaptureWallPaper )
			{
				captureSuccess = captureWallpaperBitmapToCanvas( context , captureCanvas , src , new Rect( 0 , 0 , width , height ) , paint );
			}
		}
		if( captureSuccess )
		{
			new AsyncTask<Bitmap , Void , Bitmap>() {
				
				@Override
				protected Bitmap doInBackground(
						Bitmap ... params )
				{
					return fastBlur( params[0] , option.scaleFactor , option.radius , paint );
				}
				
				@Override
				protected void onPostExecute(
						Bitmap result )
				{
					super.onPostExecute( result );
					if( option.callbacks != null )
					{
						option.callbacks.blurCompleted( result );
					}
				}
			}.execute( oriBitmap );
		}
		lock.unlock();
	}
	
	/**
	 * 截屏
	 * @param oriView 需要模糊的view
	 * @param canvas 画布
	 * @param paint 画笔
	 * @param isCaptureWallPaper 是否包含壁纸
	 * @return
	 */
	private static boolean captureViewBitmapToCanvas(
			Context context ,
			View oriView ,
			Canvas canvas ,
			Paint paint ,
			Rect src ,
			boolean isCaptureWallPaper )
	{
		boolean captureSuccess = false;
		try
		{
			if( isCaptureWallPaper )
			{
				captureWallpaperBitmapToCanvas( context , canvas , src , new Rect( 0 , 0 , src.width() , src.height() ) , paint );
			}
			//获取oriView上的bitmap.
			oriView.draw( canvas );
			captureSuccess = true;
		}
		catch( Exception e )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "captureViewBitmapToCanvas failed ! " , e );
		}
		return captureSuccess;
	}
	
	/**
	 * 截屏
	 * @param oriView 需要模糊的view,
	 * @param canvas 画布
	 * @param paint 画笔
	 * @param isCaptureWallPaper 是否包含壁纸
	 * @return
	 */
	private static boolean captureViewBitmapToCanvas2(
			Context context ,
			View oriView ,
			Canvas canvas ,
			Paint paint ,
			Rect src ,
			boolean isCaptureWallPaper )
	{
		boolean captureSuccess = false;
		try
		{
			if( isCaptureWallPaper )
			{
				captureWallpaperBitmapToCanvas( context , canvas , src , new Rect( 0 , 0 , src.width() , src.height() ) , paint );
			}
			Bitmap captureBitmap = getViewBitmap( oriView );
			canvas.drawBitmap( captureBitmap , 0 , 0 , paint );
			captureSuccess = true;
			//释放
			if( captureBitmap != null && !captureBitmap.isRecycled() )
			{
				captureBitmap.recycle();
				captureBitmap = null;
			}
		}
		catch( Exception e )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "captureViewBitmapToCanvas2 failed !" , e );
		}
		return captureSuccess;
	}
	
	/**
	 * 截壁纸
	 * @param canvas
	 * @param src
	 * @param dst
	 * @param paint
	 * @return
	 */
	private static boolean captureWallpaperBitmapToCanvas(
			Context context ,
			Canvas canvas ,
			Rect src ,
			Rect dst ,
			Paint paint )
	{
		boolean rst = false;
		try
		{
			Bitmap bitmap = null;
			Bitmap wpBitmap = getWallpaperBitmap( context );
			if( wpBitmap.getWidth() < dst.width() || wpBitmap.getHeight() < dst.height() )
			{
				//当壁纸的宽或者高小于目标的宽或者高,避免模糊效果不佳
				bitmap = Bitmap.createScaledBitmap( wpBitmap , Math.max( wpBitmap.getWidth() , dst.width() ) , Math.max( wpBitmap.getHeight() , dst.height() ) , true );
				//释放
			}
			else
			{
				bitmap = wpBitmap;
			}
			//壁纸的偏移量处理
			Rect srcRect = new Rect( src );
			float xOffset = WallpaperOffsetManager.getInstance().getWallpaperXOffset();
			float yOffset = WallpaperOffsetManager.getInstance().getWallpaperYOffset();
			srcRect.left += xOffset;
			srcRect.right += xOffset;
			srcRect.top += yOffset;
			srcRect.bottom += yOffset;
			//当超过了壁纸的边界
			int distanceX = srcRect.right - bitmap.getWidth();
			if( distanceX > 0 )
			{
				srcRect.left -= distanceX;
				srcRect.right -= distanceX;
			}
			int distanceY = srcRect.bottom - bitmap.getHeight();
			if( distanceY > 0 )
			{
				srcRect.top -= distanceY;
				srcRect.bottom -= distanceY;
			}
			canvas.drawBitmap( bitmap , srcRect , dst , paint );
			rst = true;
			//释放
			if( bitmap != null && bitmap != wpBitmap && !bitmap.isRecycled() )
			{
				bitmap.recycle();
				bitmap = null;
			}
		}
		catch( Exception e )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "captureWallpaperBitmapToCanvas failed !" , e );
		}
		return rst;
	}
	
	/**
	 * get current wallpaper's bitmap
	 */
	private static Bitmap getWallpaperBitmap(
			Context context )
	{
		Bitmap wpBitmap = null;
		try
		{
			WallpaperManager wpm = WallpaperManager.getInstance( context );
			wpBitmap = ( (BitmapDrawable)wpm.getDrawable() ).getBitmap();
		}
		catch( Exception e )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "get wallpaper bitmap failed !" , e );
		}
		return wpBitmap;
	}
	
	/**
	 * Draw the view into a bitmap.
	 * 把view对象转换成bitmap.
	 * @param view 需要绘制的View
	 * @return 返回Bitmap对象
	 */
	private static Bitmap getViewBitmap(
			View v )
	{
		v.clearFocus();// 清除视图焦点  
		v.setPressed( false );// 将视图设为不可点击  
		// 返回视图是否可以保存他的画图缓存.能画缓存就返回false
		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing( false );
		// Reset the drawing cache background color to fully transparent
		// for the duration of this operation 
		//将视图在此操作时置为透明
		int color = v.getDrawingCacheBackgroundColor();// 获得绘制缓存位图的背景颜色 
		v.setDrawingCacheBackgroundColor( 0 );// 设置绘图背景颜色  
		float alpha = v.getAlpha();
		v.setAlpha( 1.0f );
		if( color != 0 )// 如果获得的背景不是黑色的则释放以前的绘图缓存 
		{
			v.destroyDrawingCache();
		}
		boolean successed = true;
		// The first try
		v.buildDrawingCache();// 重新创建绘图缓存，此时的背景色是黑色
		Bitmap cacheBitmap = v.getDrawingCache();// 将绘图缓存得到的,注意这里得到的只是一个图像的引用
		if( cacheBitmap == null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( "The first try failed getViewBitmap(" + v , ")" ) , new RuntimeException() );
			successed = false;
		}
		// The scecond try
		if( !successed )
		{
			int widthSpec = View.MeasureSpec.makeMeasureSpec( v.getMeasuredWidth() , View.MeasureSpec.EXACTLY );
			int heightSpec = View.MeasureSpec.makeMeasureSpec( v.getMeasuredHeight() , View.MeasureSpec.EXACTLY );
			v.measure( widthSpec , heightSpec );
			v.layout( 0 , 0 , v.getMeasuredWidth() , v.getMeasuredHeight() );
			v.buildDrawingCache();
			cacheBitmap = v.getDrawingCache();
			if( cacheBitmap == null )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "The scecond try failed getViewBitmap(" + v , ")" ) , new RuntimeException() );
				return null;
			}
		}
		Bitmap bitmap = Bitmap.createBitmap( cacheBitmap );
		// Restore the view
		v.destroyDrawingCache();
		v.setAlpha( alpha );
		v.setWillNotCacheDrawing( willNotCache );
		v.setDrawingCacheBackgroundColor( color );
		return bitmap;
	}
	
	private static Rect getRectByView(
			View oriView )
	{
		int left = oriView.getLeft();
		int top = oriView.getTop();
		int right = left + oriView.getMeasuredWidth();
		int bottom = top + oriView.getMeasuredHeight();
		return new Rect( left , top , right , bottom );
	}
	
	private static boolean isLiveWallpaper(
			Context context )
	{
		try
		{
			WallpaperManager wpm = WallpaperManager.getInstance( context );
			if( wpm.getWallpaperInfo() != null )
			{
				return true;
			}
		}
		catch( Exception e )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "judge it is live wallpaper failed !" , e );
		}
		return false;
	}
	
	//cheyingkun add start	//动态壁纸返回默认模糊背景
	/**获取默认模糊壁纸*/
	private static Bitmap getDefaultBlurWallpaperBitmap(
			Context context )
	{
		if( context == null )
		{
			return null;
		}
		Drawable blurDefaultBg = context.getResources().getDrawable( R.drawable.bulr_default_bg );
		int width = blurDefaultBg.getMinimumWidth();
		int height = blurDefaultBg.getMinimumHeight();
		Bitmap bitmap = Bitmap.createBitmap( width , height , Bitmap.Config.ARGB_8888 );
		//注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
		Canvas canvas = new Canvas( bitmap );
		blurDefaultBg.setBounds( 0 , 0 , width , height );
		blurDefaultBg.draw( canvas );
		return bitmap;
	}
	//cheyingkun add end
}
