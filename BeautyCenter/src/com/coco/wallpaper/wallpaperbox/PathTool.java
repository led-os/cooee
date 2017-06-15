package com.coco.wallpaper.wallpaperbox;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	
	public static String getWallpaperDir()
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Wallpaper/";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Wallpaper/";
	}
	
	public static String getRecommendDir()
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Wallpaper/recommend";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Wallpaper/recommend";
	}
	
	public static String getImageDir(
			String packageName )
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Wallpaper/Image/" + packageName;
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Wallpaper/Image/" + packageName;
	}
	
	public static String getDownloadingDir()
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Wallpaper/Downloading";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Wallpaper/Downloading";
	}
	
	public static String getAppDir()
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Wallpaper/App";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Wallpaper/App";
	}
	
	public static String getTempDir()
	{
		if( !com.coco.theme.themebox.StaticClass.set_directory_path.equals( "" ) )
		{
			return com.coco.theme.themebox.StaticClass.set_directory_path + custom_sdcard_root_path + "Wallpaper/Temp";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + custom_sdcard_root_path + "Wallpaper/Temp";
	}
	
	public static String getClipFilePath()
	{
		return getWallpaperDir() + "/" + "clip.jpg";
	}
	
	public static String getLauncFilePath()
	{
		return getWallpaperDir() + "/" + "launcherthumb.png";
	}
	
	public static String getAppFile(
			String packageName )
	{
		return getAppDir() + "/" + packageName + ".jpg";
	}
	
	public static String getAppLiveFile(
			String packageName )
	{
		return getAppDir() + "/" + packageName + ".apk";
	}
	
	public static String getAppSmallFile(
			String packageName )
	{
		return getAppDir() + "/" + packageName + "_small.jpg";
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
	
	public static String getDownloadingList()
	{
		return getDownloadingDir() + "/list.tmp";
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
			new File( getWallpaperDir() , ".nomedia" ).createNewFile();
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
	
	public static Bitmap compressBitmap(
			Bitmap image ,
			float with ,
			float height )
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress( Bitmap.CompressFormat.JPEG , 100 , baos );
		if( baos.toByteArray().length / 1024 > 1024 )
		{//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出     
			baos.reset();//重置baos即清空baos   
			image.compress( Bitmap.CompressFormat.JPEG , 50 , baos );//这里压缩50%，把压缩后的数据存放到baos中   
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream( baos.toByteArray() );
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了   
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream( isBm , null , newOpts );
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是800*480分辨率，所以高和宽我们设置为   
		float hh = height;//这里设置高度为800f   
		float ww = with;//这里设置宽度为480f   
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可   
		int be = 1;//be=1表示不缩放   
		//		if( w < h && h > hh )
		//		{//如果高度高的话根据宽度固定大小缩放   
		//			be = (int)( newOpts.outHeight / hh );
		//		}
		//		else if( w > h && w > ww )
		//		{//如果宽度大的话根据宽度固定大小缩放   
		//			be = (int)( newOpts.outWidth / ww );
		//		}
		if( h > hh )
		{
			be = (int)( newOpts.outHeight / hh );
		}
		if( be <= 0 )
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例   
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了   
		isBm = new ByteArrayInputStream( baos.toByteArray() );
		bitmap = BitmapFactory.decodeStream( isBm , null , newOpts );
		return compressImage( bitmap );//压缩好比例大小后再进行质量压缩   
	}
	
	private static Bitmap compressImage(
			Bitmap image )
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress( Bitmap.CompressFormat.JPEG , 100 , baos );//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中   
		int options = 100;
		while( baos.toByteArray().length / 1024 > 100 )
		{ //循环判断如果压缩后图片是否大于100kb,大于继续压缩          
			baos.reset();//重置baos即清空baos   
			image.compress( Bitmap.CompressFormat.JPEG , options , baos );//这里压缩options%，把压缩后的数据存放到baos中   
			options -= 10;//每次都减少10   
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream( baos.toByteArray() );//把压缩后的数据baos存放到ByteArrayInputStream中   
		Bitmap bitmap = BitmapFactory.decodeStream( isBm , null , null );//把ByteArrayInputStream数据生成图片   
		return bitmap;
	}
}
