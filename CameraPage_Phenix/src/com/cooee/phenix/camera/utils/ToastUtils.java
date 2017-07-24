package com.cooee.phenix.camera.utils;


// MusicPage CameraPage
import android.app.Activity;
import android.widget.Toast;


public class ToastUtils
{
	
	public static void showToast(
			final Activity activity ,
			final String messgae )
	{
		if( activity != null )
			activity.runOnUiThread( new Runnable() {
				
				@Override
				public void run()
				{
					Toast.makeText( activity , messgae , Toast.LENGTH_SHORT ).show();
				}
			} );
	}
	
	public static void showToast(
			final Activity activity ,
			final int messgaeId )
	{
		if( activity != null )
			activity.runOnUiThread( new Runnable() {
				
				@Override
				public void run()
				{
					Toast.makeText( activity , messgaeId , Toast.LENGTH_SHORT ).show();
				}
			} );
	}
}
