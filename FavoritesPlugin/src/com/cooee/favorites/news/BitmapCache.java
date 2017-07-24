package com.cooee.favorites.news;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;


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
		Log.i( TAG , "get cache " + url + " " + mCache.get( url ) );
		Bitmap bmp = mCache.get( url );
		return bmp;
	}
	
	@Override
	public void putBitmap(
			String url ,
			Bitmap bitmap )
	{
		Log.i( TAG , "get cache: " + url );
		if( bitmap != null )
		{
			if( mCache.get( url ) == null )
				mCache.put( url , bitmap );
		}
	}
}
