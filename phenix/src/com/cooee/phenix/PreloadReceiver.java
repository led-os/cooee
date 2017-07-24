package com.cooee.phenix;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class PreloadReceiver extends BroadcastReceiver
{
	
	private static final String TAG = "Launcher.PreloadReceiver";
	private static final boolean LOGD = false;
	public static final String EXTRA_WORKSPACE_NAME = "com.cooee.phenix.action.EXTRA_WORKSPACE_NAME";
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		final LauncherProvider provider = LauncherAppState.getLauncherProvider();
		if( provider != null )
		{
			String name = intent.getStringExtra( EXTRA_WORKSPACE_NAME );
			final int workspaceResId = !TextUtils.isEmpty( name ) ? context.getResources().getIdentifier( name , "xml" , "com.cooee.phenix" ) : 0;
			if( LOGD )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "workspace name: " , name , " id: " , workspaceResId ) );
			}
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					provider.loadDefaultFavoritesIfNecessary( workspaceResId );
				}
			} ).start();
		}
	}
}
