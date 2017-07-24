package com.cooee.framework.color;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;


/**
 * 颜色管理类  目前主要用于背景图亮度获取 
 * @author lvjiangbin  2015-7-23
 */
public class ColorManagement
{
	
	public static ColorManagement mColorManagement = null;
	public final static float GET_LIGHT_FAIL = -1f;
	private HashMap<String , ColorDate> mColorDateMap = new HashMap<String , ColorDate>();
	//亮度界限 取值区间为0-1f    举例为0.75f   小于0.75可视为偏黑   大于 可以为偏白
	public static final float BRIGHTNESS_BOUNDARIES = 0.95f;//cheyingkun add	//统一文字颜色跟随壁纸变化的判断值。【i_0012333】【i_0012334】
	
	class ColorDate
	{
		
		public float light = -1;
		public Palette palette = null;
		public ArrayList<ColorChangeListener> listeners = new ArrayList<ColorChangeListener>();
	}
	
	public static ColorManagement getInstance()
	{
		if( mColorManagement == null )
		{
			synchronized( ColorManagement.class )
			{
				if( mColorManagement == null )
				{
					mColorManagement = new ColorManagement();
				}
			}
		}
		return mColorManagement;
	}
	
	/**
	 * 
	 *	通过位图初始化亮度值
	 * @author lvjiangbin 2015-7-23
	 */
	public void obtainBitmapLight(
			final Bitmap bitmap ,
			final String key ,
			final Handler handler )
	{
		if( bitmap == null )
		{
			handler.post( new Runnable() {
				
				@Override
				public void run()
				{
					ColorDate d = mColorDateMap.get( key );
					for( int i = 0 ; i < d.listeners.size() ; i++ )
					{
						d.listeners.get( i ).onLightChange( GET_LIGHT_FAIL );
					}
				}
			} );
			return;
		}
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				ColorDate data = mColorDateMap.get( key );
				if( data == null )
				{
					data = new ColorDate();
					mColorDateMap.put( key , data );
				}
				Palette palette = Palette.generate( bitmap , 3 );
				boolean success = false;
				float l = -1f;
				List<Swatch> swatches = palette.getSwatches();
				if( swatches != null && swatches.size() > 0 )
				{
					Swatch swatch = swatches.get( 0 );
					float[] hsl = swatch.getHsl();
					if( hsl != null && hsl[2] != 0 )
					{
						success = true;
						l = hsl[2];
						//											Log.v( "lvjiangbin" , "亮度是 :" + hsl[2] );
					}
					if( !success )
					{
						Bitmap temp = scaleBitmapDown( bitmap , 20 );
						l = getLightForBitmap( temp );
						temp.recycle();
						temp = null;
					}
				}
				data.light = l;
				handler.post( new Runnable() {
					
					@Override
					public void run()
					{
						ColorDate d = mColorDateMap.get( key );
						for( int i = 0 ; i < d.listeners.size() ; i++ )
						{
							d.listeners.get( i ).onLightChange( d.light );
						}
					}
				} );
			}
		} );
	}
	
	public void addColorChangeListener(
			ColorChangeListener colorChangeListener ,
			String key )
	{
		ColorDate data = mColorDateMap.get( key );
		if( data == null )
		{
			data = new ColorDate();
			mColorDateMap.put( key , data );
		}
		if( !data.listeners.contains( colorChangeListener ) )
		{
			data.listeners.add( colorChangeListener );
		}
		colorChangeListener.onLightChange( data.light );
	}
	
	public float getLight(
			String key )
	{
		ColorDate data = mColorDateMap.get( key );
		if( data != null )
		{
			return data.light;
		}
		return -1;
	}
	
	public void removeColorChangeListener(
			ColorChangeListener colorChangeListener ,
			String key )
	{
		ColorDate data = mColorDateMap.get( key );
		if( data != null )
		{
			data.listeners.remove( colorChangeListener );
		}
	}
	
	private float getLightForBitmap(
			Bitmap bitmap )
	{
		int r;
		int g;
		int b;
		int number = 0;
		double bright = -1f;
		Integer localTemp;
		for( int i = 0 ; i < bitmap.getWidth() ; i++ )
		{
			for( int j = 0 ; j < bitmap.getHeight() ; j++ )
			{
				number++;
				localTemp = (Integer)bitmap.getPixel( i , j );
				r = ( localTemp | 0xff00ffff ) >> 16 & 0x00ff;
				g = ( localTemp | 0xffff00ff ) >> 8 & 0x0000ff;
				b = ( localTemp | 0xffffff00 ) & 0x0000ff;
				bright = bright + 0.299 * r + 0.587 * g + 0.114 * b;
				//				Log.v( "lvjiangbin" , "bright = " + bright );
			}
		}
		return (float)( bright = ( bright / number / 255f ) );
	}
	
	private Bitmap scaleBitmapDown(
			Bitmap bitmap ,
			int targetMaxDimension )
	{
		if( bitmap == null )
		{
			return Bitmap.createBitmap( targetMaxDimension , targetMaxDimension , Config.ARGB_8888 );
		}
		int maxDimension = Math.max( bitmap.getWidth() , bitmap.getHeight() );
		if( maxDimension <= targetMaxDimension )
		{
			// If the bitmap is small enough already, just return it
			return bitmap;
		}
		float scaleRatio = targetMaxDimension / (float)maxDimension;
		return Bitmap.createScaledBitmap( bitmap , Math.round( bitmap.getWidth() * scaleRatio ) , Math.round( bitmap.getHeight() * scaleRatio ) , false );
	}
	
	public interface ColorChangeListener
	{
		
		/**
		 * 亮度改变
		 * -1代表失败 请设置为默认
		 * @author lvjiangbin 2015-7-22
		 */
		void onLightChange(
				float light );
	}
}
