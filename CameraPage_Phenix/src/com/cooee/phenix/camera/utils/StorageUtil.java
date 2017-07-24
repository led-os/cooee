package com.cooee.phenix.camera.utils;


import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.os.StatFs;


/**
 * 存储相关工具类，主要用于检测当前系统是否有足够的存储空间
 * @author Xu Jin
 *
 */
public class StorageUtil
{
	
	/**
	 * 判断是否有外存储空间
	 * @return
	 */
	public static boolean hasExternalStorage()
	{
		String state = Environment.getExternalStorageState();
		if( !Environment.MEDIA_MOUNTED.equals( state ) )
		{
			return false;
		}
		return true;
	}
	
	/**
	 * 判断sd卡是否还有足够的空间
	 * @return
	 */
	public static boolean hasFreeExternalSpace()
	{
		String path = "/storage/emulated/0";
		File file = new File( path );
		if( !file.exists() )
		{
			if( !hasExternalStorage() )
			{
				return false;
			}
			File file2 = Environment.getExternalStorageDirectory();
			int count = new StatFs( file2.toString() ).getAvailableBlocks();
			return count > 0;
		}
		else
		{
			int count = new StatFs( file.toString() ).getAvailableBlocks();
			return count > 0;
		}
	}
	
	public static boolean externalSpaceIsWritable()
	{
		if( !hasExternalStorage() )
		{
			return false;
		}
		return false;
	}
	
	public static String getExternalPath()
	{
		String path = "/storage/emulated/0";
		if( new File( path ).exists() )
			return path;
		try
		{
			path = Environment.getExternalStorageDirectory().getCanonicalPath();
		}
		catch( IOException e )
		{
			path = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		return path;
	}
}
