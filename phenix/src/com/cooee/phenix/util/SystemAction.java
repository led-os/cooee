package com.cooee.phenix.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.cooee.phenix.Launcher;


public class SystemAction
{
	
	public static class ResestActivity extends Activity
	{
		
		public void onCreate(
				Bundle bundle )
		{
			super.onCreate( bundle );
		}
		
		public void onStart()
		{
			Launcher.finishSelf();//zhujieping,launcher一定要finish，否则启动后，进入桌面设置，返回，桌面重启
			final Intent intent = new Intent();
			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
			intent.addFlags( Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT );
			intent.setAction( Intent.ACTION_MAIN );
			intent.addCategory( Intent.CATEGORY_HOME );
			intent.setPackage( this.getPackageName() );
			startActivity( intent );
			overridePendingTransition( 0 , 0 );
			finish();
			System.exit( 0 );
		}
	}
	
	public static void RestartSystem(
			Context context )
	{
		Intent startMain = new Intent();
		startMain.setClass( context , ResestActivity.class );
		context.startActivity( startMain );
	}
}
