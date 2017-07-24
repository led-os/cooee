package com.cooee.framework.function.Grab;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;


public class Receiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(
			final Context context ,
			Intent arg1 )
	{
		// TODO Auto-generated method stub
		try
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "GrabLauncher" , arg1.getAction() );
			Intent intent = new Intent();
			//intent.putExtra( "flag" , 0 );
			intent.setClass( context , GrabService.class );
			context.startService( intent );
		}
		catch( Exception e )
		{
			// TODO: handle exception
		}
	}
}
