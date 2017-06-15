package com.coco.shortcut.shortcutbox;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import android.os.Environment;

import com.coco.theme.themebox.util.Log;


public class PathTool
{
	
	private static final String LOG_TAG = "PathTool";
	private static String custom_sdcard_root_path = "/Coco/";
	
	public static String getCustomRootPath()
	{
		return custom_sdcard_root_path;
	}
	
	public static void setCustomRootPath(
			String customRootPath )
	{
		custom_sdcard_root_path = customRootPath;
	}
	
	public static String getSceneDir()
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Operate/";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Operate/";
	}
	
	public static String getRecommendDir()
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Operate/recommend";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Operate/recommend";
	}
	
	public static String getImageDir(
			String packageName )
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Operate/Image/" + packageName;
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Operate/Image/" + packageName;
	}
	
	public static String getDownloadingDir()
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Operate/Downloading";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Operate/Downloading";
	}
	
	public static String getAppDir()
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Operate/App";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Operate/App";
	}
	
	public static String getTempDir()
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Operate/Temp";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Operate/Temp";
	}
	
	public static String getAppFile(
			String packageName )
	{
		return getAppDir() + "/" + packageName + ".apk";
	}
	
	public static String getThumbFile(
			String packageName )
	{
		return getImageDir( packageName ) + "/thumb.tupian";
	}
	
	public static String getPreviewDir(
			String packageName )
	{
		return getImageDir( packageName ) + "/Preview";
	}
	
	public static String[] getPreviewLists(
			String packageName )
	{
		String rootPath = getPreviewDir( packageName );
		File rootFile = new File( rootPath );
		String[] fileNames = rootFile.list( new FilenameFilter() {
			
			@Override
			public boolean accept(
					File dir ,
					String filename )
			{
				if( filename.endsWith( ".tupian" ) )
				{
					return true;
				}
				return false;
			}
		} );
		if( fileNames == null || fileNames.length == 0 )
		{
			return new String[]{};
		}
		String[] filePaths = new String[fileNames.length];
		for( int i = 0 ; i < filePaths.length ; i++ )
		{
			filePaths[i] = rootPath + "/" + fileNames[i];
		}
		return filePaths;
	}
	
	public static String getDownloadingThumb(
			String packageName )
	{
		return getDownloadingDir() + "/" + packageName + "_thumb.tmp";
	}
	
	public static String getDownloadingPreview(
			String packageName )
	{
		return getDownloadingDir() + "/" + packageName + "_preview.tmp";
	}
	
	public static String getDownloadingApp(
			String packageName )
	{
		return getDownloadingDir() + "/" + packageName + "_app.tmp";
	}
	
	public static String getSceneDownloadingList()
	{
		return getDownloadingDir() + "/scenelist.tmp";
	}
	
	public static String getThumbTempFile()
	{
		String result = getTempDir() + "/share_thumb.png";
		Log.d( LOG_TAG , "thumbTempPath=" + result );
		return result;
	}
	
	public static void makeDirApp()
	{
		try
		{
			String appDir = getAppDir();
			new File( getAppDir() ).mkdirs();
			Log.d( LOG_TAG , "appDir=" + appDir );
			String recommendDir = getRecommendDir();
			new File( recommendDir ).mkdirs();
			String downloadingDir = getDownloadingDir();
			new File( downloadingDir ).mkdirs();
			Log.d( LOG_TAG , "downloadingDir=" + downloadingDir );
			String tempDir = getTempDir();
			new File( tempDir ).mkdirs();
			Log.d( LOG_TAG , "tempDir=" + tempDir );
			new File( getSceneDir() , ".nomedia" ).createNewFile();
		}
		catch( SecurityException e )
		{
			e.printStackTrace();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public static void makeDirImage(
			String packageName )
	{
		try
		{
			String imageDir = getImageDir( packageName );
			new File( imageDir ).mkdirs();
			Log.d( LOG_TAG , "imageDir=" + imageDir );
		}
		catch( SecurityException e )
		{
			e.printStackTrace();
		}
	}
	
	public static void makePreviewDir(
			String packageName )
	{
		try
		{
			String imageDir = getPreviewDir( packageName );
			new File( imageDir ).mkdirs();
			Log.d( LOG_TAG , "previewDir=" + imageDir );
		}
		catch( SecurityException e )
		{
			e.printStackTrace();
		}
	}
}
