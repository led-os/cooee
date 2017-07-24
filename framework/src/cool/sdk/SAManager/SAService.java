package cool.sdk.SAManager;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;

import cool.sdk.SAManager.SACoolDLMgr.finishCb;
import cool.sdk.download.manager.DlMethod;
import cool.sdk.log.CoolLog;


public class SAService extends Service
{
	
	CoolLog Log;
	static boolean isChecking = false;
	
	@Override
	public int onStartCommand(
			Intent intent ,
			int flags ,
			int startId )
	{
		// TODO Auto-generated method stub
		Log = new CoolLog( this );
		//		new Thread() {
		//			
		//			@Override
		//			public void run()
		//			{
		//				// TODO Auto-generated method stub
		//				long startTick = System.currentTimeMillis();
		//				while( true )
		//				{
		//					long abs = Math.abs( System.currentTimeMillis() - startTick );
		//					if( abs > 2 * 60 * 1000 )
		//					{
		//						break;
		//					}
		//					if( !DlMethod.IsWifiConnected( SAService.this ) )
		//					{
		//						isChecking = false;
		//						Log.v( "SA" , "SAService stopSelf wifi switch off" );
		//						stopSelf();
		//						return;
		//					}
		//					Log.v( "SA" , "tick:" + ( 2 * 60 - abs / 1000 ) );
		//					try
		//					{
		//						Thread.sleep( 1000 );
		//					}
		//					catch( Exception e )
		//					{
		//						// TODO Auto-generated catch block
		//						e.printStackTrace();
		//					}
		//				}
		//				try
		//				{
		//					if( !SAHelper.getInstance( SAService.this ).checkSilent( new finishCb() {
		//						
		//						@Override
		//						public void onFinish()
		//						{
		//							// TODO Auto-generated method stub
		//							isChecking = false;
		//							Log.v( "SA" , "SAService stopSelf onFinish" );
		//							stopSelf();
		//						}
		//					} ) )
		//					{
		//						isChecking = false;
		//						Log.v( "SA" , "SAService stopSelf no download" );
		//						stopSelf();
		//					}
		//				}
		//				catch( Exception e )
		//				{
		//					// TODO: handle exception
		//					e.printStackTrace();
		//					isChecking = false;
		//					Log.v( "SA" , "SAService stopSelf Exception" );
		//					stopSelf();
		//				}
		//			}
		//		}.start();
		try
		{
			if( !SAHelper.getInstance( SAService.this ).checkSilent( new finishCb() {
				
				@Override
				public void onFinish()
				{
					// TODO Auto-generated method stub
					isChecking = false;
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "SA" , "SAService stopSelf onFinish" );
					stopSelf();
				}
			} ) )
			{
				isChecking = false;
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( "SA" , "SAService stopSelf no download" );
				stopSelf();
			}
		}
		catch( Exception e )
		{
			// TODO: handle exception
			e.printStackTrace();
			isChecking = false;
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "SA" , "SAService stopSelf Exception" );
			stopSelf();
		}
		return super.onStartCommand( intent , flags , startId );
	}
	
	public synchronized static void doCheck(
			Context context )
	{
		if( DlMethod.IsWifiConnected( context ) && !isChecking )
		{
			isChecking = true;
			Intent intent = new Intent();
			intent.setClass( context , SAService.class );
			context.startService( intent );
		}
	}
	
	public synchronized static void stopCheck(
			Context context )
	{
		Intent intent = new Intent();
		intent.setClass( context , SAService.class );
		context.stopService( intent );
		isChecking = false;
	}
	
	@Override
	public IBinder onBind(
			Intent intent )
	{
		// TODO Auto-generated method stub
		return null;
	}
}
