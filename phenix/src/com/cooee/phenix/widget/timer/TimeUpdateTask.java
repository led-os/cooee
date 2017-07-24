package com.cooee.phenix.widget.timer;


// luomingjun add whole file //桌面时钟
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;


/**
 * 时钟插件 luomingjun
 * 
 * @author Administrator
 * 
 */
public class TimeUpdateTask implements Runnable
{
	
	private Context context;
	private int i = 0;
	
	public TimeUpdateTask(
			Context context )
	{
		super();
		this.context = context;
		IntentFilter filter = new IntentFilter();
		filter.addAction( Intent.ACTION_DATE_CHANGED );
		filter.addAction( Intent.ACTION_TIMEZONE_CHANGED );
		filter.addAction( Intent.ACTION_TIME_CHANGED );
		filter.addAction( Intent.ACTION_TIME_TICK );
		filter.addAction( "android.intent.action.TIME_SET" );
		context.getApplicationContext().registerReceiver( mIntentReceiver , filter );
	}
	
	public void run()
	{
		i++;
		new TimeUtil( context , i ).updateTimeWidget();
	}
	
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			if( ( intent.getAction().equals( Intent.ACTION_TIMEZONE_CHANGED ) ) || ( intent.getAction().equals( Intent.ACTION_TIME_TICK ) ) || ( intent.getAction().equals( Intent.ACTION_DATE_CHANGED ) ) || ( intent
					.getAction().equals( Intent.ACTION_TIME_CHANGED ) ) || ( intent.getAction().equals( "android.intent.action.TIME_SET" ) ) )
			{
				if( TimeAppWidgetProvider.stpe == null )
				{
					TimeAppWidgetProvider.stpe = new ScheduledThreadPoolExecutor( 1 );
					TimeUpdateTask timer = new TimeUpdateTask( context );
					TimeAppWidgetProvider.stpe.scheduleWithFixedDelay( timer , 1 , 1 , TimeUnit.SECONDS );
				}
			}
		}
	};
}
