package com.cooee.phenix;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.TextView;

import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


/**
 * We use a custom tab view to process our own focus traversals.
 */
public class AccessibleTabView extends TextView
{
	
	public AccessibleTabView(
			Context context )
	{
		super( context );
	}
	
	public AccessibleTabView(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
	}
	
	public AccessibleTabView(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
	}
	
	@Override
	public boolean onKeyDown(
			int keyCode ,
			KeyEvent event )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
		{
			return FocusHelper.handleTabKeyEvent( this , keyCode , event ) || super.onKeyDown( keyCode , event );
		}
		//cheyingkun add start	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
		else
		{
			return super.onKeyDown( keyCode , event );
		}
		//cheyingkun add end
	}
	
	@Override
	public boolean onKeyUp(
			int keyCode ,
			KeyEvent event )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER )//cheyingkun add	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
		{
			return FocusHelper.handleTabKeyEvent( this , keyCode , event ) || super.onKeyUp( keyCode , event );
		}
		//cheyingkun add start	//桌面是否支持按键机，true支持、false不支持，默认true【c_0004522】
		else
		{
			return super.onKeyUp( keyCode , event );
		}
		//cheyingkun add end
	}
}
