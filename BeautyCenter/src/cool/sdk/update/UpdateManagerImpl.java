package cool.sdk.update;


import android.content.Context;
import cool.sdk.MicroEntry.MicroEntryHelper;


public class UpdateManagerImpl extends UpdateManager
{
	
	public static void Update(
			Context context )
	{
		try
		{
			MicroEntryHelper.getInstance( context ).Update();
		}
		catch( Exception e )
		{
		}
	}
	
	public static void UpdateSync(
			Context context )
	{
		try
		{
			MicroEntryHelper.getInstance( context ).UpdateSync( false );
		}
		catch( Exception e )
		{
		}
	}
	
	public static void UpdateOver(
			Context context )
	{
	}
	
	public static boolean allowUpdate(
			Context context )
	{
		return true;
	}
}
