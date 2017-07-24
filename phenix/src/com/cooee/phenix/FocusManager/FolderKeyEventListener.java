package com.cooee.phenix.FocusManager;


import android.view.KeyEvent;
import android.view.View;

import com.cooee.phenix.FocusHelper;


/**
 * A keyboard listener we set on the last tab button in AppsCustomize to jump to then market icon and vice versa.
 */
/**
 * A keyboard listener we set on all the workspace icons.
 */
public class FolderKeyEventListener implements View.OnKeyListener
{
	
	public boolean onKey(
			View v ,
			int keyCode ,
			KeyEvent event )
	{
		return FocusHelper.handleFolderKeyEvent( v , keyCode , event );
	}
}
