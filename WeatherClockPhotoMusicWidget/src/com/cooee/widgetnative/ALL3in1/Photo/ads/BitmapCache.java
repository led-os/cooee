package com.cooee.widgetnative.ALL3in1.Photo.ads;


public class BitmapCache
{
	//	private static final String TAG = "BitmapCache";
	//	private LruCache<String , Bitmap> mCache;
	//	
	//	public BitmapCache(
	//			Context context )
	//	{
	//		int maxSize = 4 * 1024 * 1024;
	//		mCache = new LruCache<String , Bitmap>( maxSize ) {
	//			
	//			@Override
	//			protected int sizeOf(
	//					String key ,
	//					Bitmap bitmap )
	//			{
	//				return bitmap.getRowBytes() * bitmap.getHeight();
	//			}
	//		};
	//	}
	//	
	//	@Override
	//	public Bitmap getBitmap(
	//			String url )
	//	{
	//		Log.i( TAG , "get cache " + url + " " + mCache.get( url ) );
	//		Bitmap bmp = mCache.get( url );
	//		return bmp;
	//	}
	//	
	//	@Override
	//	public void putBitmap(
	//			String url ,
	//			Bitmap bitmap )
	//	{
	//		Log.i( TAG , "get cache: " + url );
	//		if( bitmap != null )
	//		{
	//			if( mCache.get( url ) == null )
	//				mCache.put( url , bitmap );
	//		}
	//	}
	//	
	//	public void onDestroy()
	//	{
	//		mCache.evictAll();
	//	}
}
