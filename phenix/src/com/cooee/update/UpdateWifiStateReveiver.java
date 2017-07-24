package com.cooee.update;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class UpdateWifiStateReveiver extends BroadcastReceiver
{
	
	private static final String TAG = "UpdateUi.UpdateWifiStateReveiver";
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		// zhangjin@2015/12/21 bug i_0013100 ADD START		
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "onReceive mAction:" , intent.getAction() ) );
		Bundle bundle = intent.getExtras();
		int previous_wifi_state = bundle.getInt( WifiManager.EXTRA_PREVIOUS_WIFI_STATE );
		int wifi_state = bundle.getInt( WifiManager.EXTRA_WIFI_STATE );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "previous_wifi_state:" , previous_wifi_state , "-wifi_state:" , wifi_state ) );
		if( previous_wifi_state == WifiManager.WIFI_STATE_ENABLED && wifi_state != WifiManager.WIFI_STATE_ENABLED && wifi_state != WifiManager.WIFI_STATE_ENABLING )
		{
			DownloadTask downtask = UpdateDownloadManager.getInstance( context ).getDownTask();
			if( downtask != null && downtask.getRunState() == true )
			{
				UpdateDownloadManager.getInstance( context ).pauseDownload();
				LauncherUpdateFragment curFrag = LauncherUpdateFragment.getCurFrag();
				if( curFrag != null && UpdateUtil.FragUpdatable( curFrag ) )
				{
					curFrag.updateDownFailed();
				}
			}
		}
		// zhangjin@2015/12/21 ADD END
	}
}
