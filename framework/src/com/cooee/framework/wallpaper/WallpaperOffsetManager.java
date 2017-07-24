package com.cooee.framework.wallpaper;


public class WallpaperOffsetManager
{
	
	private IWallpaperOffsetInterpolator mWallpaperOffsetInterpolator = null;
	private static WallpaperOffsetManager mInstance = null;
	
	public static WallpaperOffsetManager getInstance()
	{
		if( mInstance == null )
		{
			synchronized( WallpaperOffsetManager.class )
			{
				if( mInstance == null )
				{
					mInstance = new WallpaperOffsetManager();
				}
			}
		}
		return mInstance;
	}
	
	public void setWallpaperOffsetInterpolator(
			IWallpaperOffsetInterpolator wallpaperOffsetInterpolator )
	{
		mWallpaperOffsetInterpolator = wallpaperOffsetInterpolator;
	}
	
	public float getWallpaperXOffset()
	{
		if( mWallpaperOffsetInterpolator != null )
		{
			return mWallpaperOffsetInterpolator.getWallpaperXOffset();
		}
		return 0;
	}
	
	public float getWallpaperYOffset()
	{
		if( mWallpaperOffsetInterpolator != null )
		{
			return mWallpaperOffsetInterpolator.getWallpaperYOffset();
		}
		return 0;
	}
}
