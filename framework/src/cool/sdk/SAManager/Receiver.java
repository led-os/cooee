package cool.sdk.SAManager;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;

import cool.sdk.log.CoolLog;


public class Receiver extends BroadcastReceiver
{
	
	public static final String clickAction = "cool.sdk.SAManager.action.clickNotify";
	CoolLog Log;
	
	@Override
	public void onReceive(
			final Context context ,
			final Intent intent )
	{
		// TODO Auto-generated method stub
		Log = new CoolLog( context );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "SA" , intent.getAction() );
		if( clickAction.equals( intent.getAction() ) )
		{
			//SAHelper.getInstance( context ).clickNotify( intent );
			//			new Thread() {
			//				
			//				@Override
			//				public void run()
			//				{
			//					// TODO Auto-generated method stub
			//					try
			//					{
			//						SAHelper.getInstance( context ).clickNotify( intent );
			//					}
			//					catch( Exception e )
			//					{
			//						// TODO: handle exception
			//					}
			//				}
			//			}.start();
			return;
		}
		if( ConnectivityManager.CONNECTIVITY_ACTION.equals( intent.getAction() ) )
		{
			SAService.doCheck( context );
		}
		new Thread() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				try
				{
					SAHelper.getInstance( context ).checkNotify();
				}
				catch( Exception e )
				{
					// TODO: handle exception
				}
			}
		}.start();
	}
}
