package com.cooee.framework.function.DynamicEntry.DLManager;


import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class WifiStateReveiver extends BroadcastReceiver
{
	
	private Timer timer = null;
	private final static int waitRestartTime = 1000 * 60 * 2;//广播2分钟开始下载
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		if( BaseAppState.getActivityInstance() != null && BaseAppState.isWifiEnabled( context ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "Cool" , StringUtils.concat( "wifi broadcast time:" , DlManager.getInstance().getReDownloadHelperHandle().getTime() ) );
			startImproperStop downTask = new startImproperStop();
			timer = new Timer();
			timer.schedule( downTask , waitRestartTime );
		}
	}
	
	class startImproperStop extends TimerTask
	{
		
		@Override
		public void run()
		{
			ReDownloadHelperHandle mReDownloadHelperHandle = DlManager.getInstance().getReDownloadHelperHandle();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "Cool" , StringUtils.concat( "wifi broadcast start time:" , mReDownloadHelperHandle.getTime() ) );
			mReDownloadHelperHandle.startImproperStopTasks( BaseAppState.getActivityInstance() );
		}
	}
}
