package com.coco.wallpaper.wallpaperbox;


import java.io.File;

import android.content.Context;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.util.Log;

import com.coco.download.DownloadList;
import com.coco.theme.themebox.ThemeInformation;
import com.coco.theme.themebox.database.model.DownloadStatus;
import com.coco.theme.themebox.database.model.ThemeInfoItem;
import com.coco.theme.themebox.database.service.DownloadThemeService;
import com.coco.theme.themebox.service.ThemesDB;
import com.coco.theme.themebox.util.Tools;


public class WallpaperInformation extends ThemeInformation
{
	
	public void copy(
			WallpaperInformation item )
	{
		this.className = item.className;
		this.installed = item.installed;
		this.displayName = item.displayName;
		this.themeInfo.copyFrom( item.themeInfo );
		this.downloadSize = item.downloadSize;
		this.downloadStatus = item.downloadStatus;
		this.thumbImage = item.thumbImage;
		this.needLoadDetail = item.needLoadDetail;
		this.mSystem = item.mSystem;
		this.downloaded = item.downloaded;
	}
	
	public boolean isDownloadedFinish()
	{
		File file = new File( PathTool.getAppFile( getPackageName() ) );
		File small = new File( PathTool.getAppSmallFile( getPackageName() ) );
		if( ( file.exists() && small.exists() ) && ( getDownloadStatus() == DownloadStatus.StatusFinish ) )
		{
			return true;
		}
		return false;
	}
	
	public boolean isDownloaded(
			Context context )
	{
		File file = new File( PathTool.getAppFile( getPackageName() ) );
		File small = new File( PathTool.getAppSmallFile( getPackageName() ) );
		if( ( !file.exists() || !small.exists() ) && ( getDownloadStatus() == DownloadStatus.StatusInit || getDownloadStatus() == DownloadStatus.StatusFinish ) )
		{
			File f1 = new File( PathTool.getDownloadingDir() + getPackageName() + "_app.tmp" );
			f1.delete();
			DownloadThemeService dSv = new DownloadThemeService( context );
			dSv.updateDownloadSizeAndStatus( getPackageName() , 0 , DownloadStatus.StatusInit , DownloadList.Wallpaper_Type );
			return false;
		}
		return downloaded;
	}
	
	// @2015/09/09 ADD START
	public boolean isWallpaperDownloaded(
			WallpaperInformation info ,
			Context context ,
			String appName )
	{
		File file = new File( PathTool.getAppFile( info.getPackageName() + appName ) );
		File small = new File( PathTool.getAppSmallFile( info.getPackageName() + appName ) );
		if( ( !file.exists() || !small.exists() ) && ( getDownloadStatus() == DownloadStatus.StatusInit || getDownloadStatus() == DownloadStatus.StatusFinish ) )
		{
			//Log.i( "minghui" , "file.exists() = "+file.exists()+";small.exists() =  " +small.exists()+"; getDownloadStatus() = "+getDownloadStatus());
			File f1 = new File( PathTool.getDownloadingDir() + info.getPackageName() + "_app.tmp" );
			f1.delete();
			DownloadThemeService dSv = new DownloadThemeService( context );
			dSv.updateDownloadSizeAndStatus( getPackageName() , 0 , DownloadStatus.StatusInit , DownloadList.Wallpaper_Type );
			return false;
		}
		else if( file.exists() && small.exists() )
		{
			info.setDownloadStatus( DownloadStatus.StatusFinish );
		}
		//Log.i( "minghui" , "downloaded = "+downloaded );
		return downloaded;
	}
	
	// @2015/09/09 ADD END
	public boolean isLiveDownloaded(
			Context context )
	{
		File file = new File( PathTool.getAppLiveFile( getPackageName() ) );
		File small = new File( PathTool.getAppSmallFile( getPackageName() ) );
		if( ( !file.exists() || !small.exists() ) && ( getDownloadStatus() == DownloadStatus.StatusInit || getDownloadStatus() == DownloadStatus.StatusFinish ) )
		{
			File f1 = new File( PathTool.getDownloadingDir() + getPackageName() + "_app.tmp" );
			f1.delete();
			DownloadThemeService dSv = new DownloadThemeService( context );
			dSv.updateDownloadSizeAndStatus( getPackageName() , 0 , DownloadStatus.StatusInit , DownloadList.Wallpaper_Type );
			return false;
		}
		return downloaded;
	}
	
	public void setThumbImage(
			Context mContext ,
			String pkgName ,
			String actName )
	{
		String thumbPath = PathTool.getAppSmallFile( pkgName );
		try
		{
			thumbImage = Tools.getPurgeableBitmap( thumbPath , -1 , -1 );
		}
		catch( OutOfMemoryError e )
		{
			disposeThumb();
			thumbImage = null;
			e.printStackTrace();
		}
	}
	
	public void loadDetail(
			Context cxt )
	{
		needLoadDetail = false;
		disposeThumb();
		thumbImage = null;
		String thumbPath = PathTool.getThumbFile( themeInfo.getPackageName() );
		if( new File( thumbPath ).exists() )
		{
			try
			{
				thumbImage = Tools.getPurgeableBitmap( thumbPath , -1 , -1 );//BitmapFactory.decodeFile( thumbPath );
			}
			catch( OutOfMemoryError error )
			{
				Log.v( "thumbImage" , error.toString() );
			}
		}
	}
	
	public void loadDetail(
			Context cxt ,
			boolean isHW )
	{
		needLoadDetail = false;
		disposeThumb();
		thumbImage = null;
		String thumbPath = PathTool.getThumbFile( themeInfo.getPackageName() );
		if( new File( thumbPath ).exists() )
		{
			try
			{
				Bitmap bmp = Tools.getPurgeableBitmap( thumbPath , -1 , -1 );//BitmapFactory.decodeFile( thumbPath );
				if( isHW )
				{
					int height = bmp.getHeight();
					int width = bmp.getWidth();
					if( height * 0.6f < width )
					{
						thumbImage = Bitmap.createBitmap( bmp , (int)( ( width - height * 0.6f ) / 2 ) , 0 , (int)( height * 0.6f ) , height );
					}
					else
					{
						thumbImage = Bitmap.createBitmap( bmp , 0 , (int)( ( height - width * 0.6f ) / 2 ) , width , (int)( width * 0.6f ) );
					}
					if( bmp != null && !bmp.isRecycled() )
					{
						bmp.recycle();
						bmp = null;
					}
				}
				else
				{
					thumbImage = bmp;
				}
			}
			catch( OutOfMemoryError error )
			{
				Log.v( "thumbImage" , error.toString() );
			}
		}
	}
	
	public void reloadThumb()
	{
		String thumbPath = PathTool.getThumbFile( themeInfo.getPackageName() );
		if( new File( thumbPath ).exists() )
		{
			try
			{
				thumbImage = Tools.getPurgeableBitmap( thumbPath , -1 , -1 );//BitmapFactory.decodeFile( thumbPath );
			}
			catch( OutOfMemoryError error )
			{
				error.printStackTrace();
			}
		}
	}
	
	public void setWallpaperDownloadItem(
			String packageName )
	{
		className = "";
		installed = false;
		if( thumbImage != null && !thumbImage.isRecycled() )
		{
			needLoadDetail = false;
		}
		else
		{
			needLoadDetail = true;
		}
		themeInfo.setPackageName( packageName );
		downloadSize = 0;
		displayName = "";
		mSystem = false;
		downloadStatus = DownloadStatus.StatusFinish;
		downloaded = true;
	}
	
	public void setThemeItem(
			ThemeInfoItem item )
	{
		className = "";
		installed = false;
		if( thumbImage != null && !thumbImage.isRecycled() )
		{
			needLoadDetail = false;
		}
		else
		{
			needLoadDetail = true;
		}
		downloadStatus = DownloadStatus.StatusInit;
		themeInfo.copyFrom( item );
		downloadSize = 0;
		displayName = themeInfo.getApplicationName();
		downloaded = false;
		mSystem = false;
	}
	
	public void setService(
			Context cxt ,
			ServiceInfo service )
	{
		className = service.name;
		installed = true;
		disposeThumb();
		thumbImage = null;
		needLoadDetail = true;
		downloadStatus = DownloadStatus.StatusFinish;
		themeInfo = new ThemeInfoItem();
		themeInfo.setPackageName( service.packageName );
		downloadSize = 0;
		displayName = service.loadLabel( cxt.getPackageManager() ).toString();
		mSystem = service.packageName.equals( ThemesDB.LAUNCHER_PACKAGENAME );
	}
}
