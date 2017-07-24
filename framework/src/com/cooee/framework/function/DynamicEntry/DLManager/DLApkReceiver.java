package com.cooee.framework.function.DynamicEntry.DLManager;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class DLApkReceiver extends BroadcastReceiver
{
	
	public static final String DLAPK_PACKAGENAME = "packageName";
	public static final String DLAPK_PROGRESS = "progress";
	public static final String DLAPK_TITLE = "dlTitle";
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO Auto-generated method stub
		if( BaseAppState.getActivityInstance() == null )
		{
			return;
		}
		final String action = intent.getAction();
		if( "com.cooee.microEntrce.download".equals( action ) )
		{
			String packageName = intent.getStringExtra( "pkgName" );
			int state = intent.getIntExtra( "state" , Constants.DL_STATUS_SUCCESS );
			if( packageName != null )
			{
				DlManager.getInstance().dealMicroEntrceItemUpdate( packageName , state );
			}
			return;
		}
		final String packageName = intent.getStringExtra( DLAPK_PACKAGENAME );
		final int progress = intent.getIntExtra( DLAPK_PROGRESS , 0 );
		final String title = intent.getStringExtra( DLAPK_TITLE );
		if( !action.equals( Constants.DL_MGR_ACTION_DOWNING ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "APK_HL" , StringUtils.concat( "action:" , action , "-packageName:" , packageName , "-progress:" , progress ) );
		}
		DlManager.getInstance().dealDownMgrActivityMsg( action , packageName , title , progress );
	}
}
