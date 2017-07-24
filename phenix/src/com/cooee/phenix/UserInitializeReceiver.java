package com.cooee.phenix;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Takes care of setting initial wallpaper for a user, by selecting the
 * first wallpaper that is not in use by another user.
 */
public class UserInitializeReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		// TODO: initial wallpaper now that wallpapers are owned by another app
	}
}
