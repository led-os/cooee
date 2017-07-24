package cool.sdk.Pspread;


import android.content.Context;


public class PspreadHelper extends PspreadUpdate
{
	
	private static PspreadHelper instance;
	
	protected PspreadHelper(
			Context context )
	{
		super( context );
		// TODO Auto-generated constructor stub
	}
	
	public static PspreadHelper getInstance(
			Context context )
	{
		synchronized( PspreadHelper.class )
		{
			if( instance == null )
			{
				instance = new PspreadHelper( context );
			}
		}
		return instance;
	}
}
