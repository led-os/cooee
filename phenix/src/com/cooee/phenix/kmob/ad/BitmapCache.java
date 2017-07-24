package com.cooee.phenix.kmob.ad;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class BitmapCache implements ImageCache
{
	
	private static final String TAG = "BitmapCache";
	private LruCache<String , Bitmap> mCache;
	
	public BitmapCache(
			Context context )
	{
		int maxSize = 4 * 1024 * 1024;
		mCache = new LruCache<String , Bitmap>( maxSize ) {
			
			@Override
			protected int sizeOf(
					String key ,
					Bitmap bitmap )
			{
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
	}
	
	@Override
	public Bitmap getBitmap(
			String url )
	{
		//如果连续两次请求广告，广告返回的数据是同一个数据源，如果直接从mCache.get( url )取到的是同一张bitmap，此时会出现bitmap已经被回收的问题，
		//由于请求广告频率不高，缓存意义不大，故此处返回null，每次请求广告重新生成bitmap
		return null;
	}
	
	@Override
	public void putBitmap(
			String url ,
			Bitmap bitmap )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "get cache: " , url ) );
		if( bitmap != null )
		{
			if( mCache.get( url ) == null )
				mCache.put( url , bitmap );
		}
	}
	
	public void onDestroy()
	{
		mCache.evictAll();
	}
}
