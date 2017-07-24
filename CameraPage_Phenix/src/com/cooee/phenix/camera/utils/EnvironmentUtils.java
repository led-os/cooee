package com.cooee.phenix.camera.utils;


// MusicPage CameraPage
import java.io.File;

import android.os.Environment;
import android.text.TextUtils;


public class EnvironmentUtils
{
	
	public static boolean isExternalStorageAvailable()
	{
		boolean state = false;
		String extStorageState = Environment.getExternalStorageState();
		if( Environment.MEDIA_MOUNTED.equals( extStorageState ) )
		{
			state = true;
		}
		return state;
	}
	
	public static boolean deleteFile(
			String path )
	{
		boolean result = false;
		if( !TextUtils.isEmpty( path ) )
		{
			File file = new File( path );
			if( file.exists() )
			{
				result = file.delete();
			}
		}
		return result;
	}
}
