package com.cooee.wallpaper.host;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;


public class CreatShortcutActivity extends Activity
{
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		final Intent intent = getIntent();
		final String action = intent.getAction();
		// If the intent is a request to create a shortcut, we'll do that and exit
		if( Intent.ACTION_CREATE_SHORTCUT.equals( action ) )
		{
			setupShortcut();
			finish();
			return;
		}
		else
		{
			finish();
		}
	}
	
	private void setupShortcut()
	{
		// First, set up the shortcut intent.  For this example, we simply create an intent that
		// will bring us directly back to this activity.  A more typical implementation would use a 
		// data Uri in order to display a more specific result, or a custom action in order to 
		// launch a specific operation.
		Intent shortcutIntent = new Intent( Intent.ACTION_MAIN );
		shortcutIntent.setClassName( this , "com.cooee.wallpaper.host.WallpaperMainActivity" );
		Intent intent = new Intent();
		intent.putExtra( Intent.EXTRA_SHORTCUT_INTENT , shortcutIntent );
		intent.putExtra( Intent.EXTRA_SHORTCUT_NAME , getString( R.string.default_change_wallpaper ) );
		Parcelable iconResource = Intent.ShortcutIconResource.fromContext( this , R.drawable.onekeychangewallpaper );
		intent.putExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE , iconResource );
		// Now, return the result to the launcher
		setResult( RESULT_OK , intent );
	}
}
