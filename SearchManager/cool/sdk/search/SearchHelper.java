package cool.sdk.search;


import android.content.Context;


public class SearchHelper extends SearchOperateUpdate
{
	
	private static SearchHelper instance = null;
	
	protected SearchHelper(
			Context context )
	{
		super( context );
	}
	
	public static SearchHelper getInstance(
			Context context )
	{
		synchronized( SearchHelper.class )
		{
			if( instance == null )
			{
				instance = new SearchHelper( context );
			}
		}
		return instance;
	}
}
