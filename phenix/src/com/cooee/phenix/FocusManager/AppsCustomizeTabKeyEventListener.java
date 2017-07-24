package com.cooee.phenix.FocusManager;


import android.view.KeyEvent;
import android.view.View;

import com.cooee.phenix.FocusHelper;


/**
 * A keyboard listener we set on the last tab button in AppsCustomize to jump to then
 * market icon and vice versa.
 */
public class AppsCustomizeTabKeyEventListener implements View.OnKeyListener
{
	
	public boolean onKey(
			View v ,
			int keyCode ,
			KeyEvent event )
	{
		return FocusHelper.handleAppsCustomizeTabKeyEvent( v , keyCode , event );
	}
}
