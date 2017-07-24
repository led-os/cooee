package cool.sdk.Uiupdate;


import android.content.Context;

import cool.sdk.update.UpdateManagerImpl.UpdateListener;


public class UiupdateHelper extends UiupdateUpdate implements UpdateListener
{
	
	static UiupdateHelper instance;
	
	protected UiupdateHelper(
			Context context )
	{
		super( context );
		// TODO Auto-generated constructor stub
	}
	
	public static UiupdateHelper getInstance(
			Context context )
	{
		if( instance == null )
		{
			synchronized( UiupdateHelper.class )
			{
				if( instance == null )
				{
					instance = new UiupdateHelper( context );
				}
			}
		}
		return instance;
	}
}
