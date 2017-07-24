package cool.sdk.FavoriteControl;


import android.content.Context;


public class FavoriteControlHelper extends FavoriteControlUpdate
{
	
	private static FavoriteControlHelper instance;
	
	protected FavoriteControlHelper(
			Context context )
	{
		super( context );
		// TODO Auto-generated constructor stub
	}
	
	public static FavoriteControlHelper getInstance(
			Context context )
	{
		synchronized( FavoriteControlHelper.class )
		{
			if( instance == null )
			{
				instance = new FavoriteControlHelper( context );
			}
		}
		return instance;
	}
}
