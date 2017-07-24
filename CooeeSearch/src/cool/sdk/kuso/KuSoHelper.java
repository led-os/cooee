package cool.sdk.kuso;


import android.content.Context;


public class KuSoHelper extends KuSoUpdate
{
	
	private static KuSoHelper instance = null;
	
	protected KuSoHelper(
			Context context )
	{
		super( context );
	}
	
	public static KuSoHelper getInstance(
			Context context )
	{
		synchronized( KuSoHelper.class )
		{
			if( instance == null )
			{
				instance = new KuSoHelper( context );
			}
		}
		return instance;
	}
}
