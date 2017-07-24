package cool.sdk.WallpaperControl;


import android.content.Context;


public class WallpaperControlHelper extends WallpaperControlUpdate
{
	
	private static WallpaperControlHelper instance;
	
	protected WallpaperControlHelper(
			Context context )
	{
		super( context );
		// TODO Auto-generated constructor stub
	}
	
	public static WallpaperControlHelper getInstance(
			Context context )
	{
		synchronized( WallpaperControlHelper.class )
		{
			if( instance == null )
			{
				instance = new WallpaperControlHelper( context );
			}
		}
		return instance;
	}
}
