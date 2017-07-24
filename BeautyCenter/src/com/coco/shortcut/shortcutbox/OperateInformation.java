package com.coco.shortcut.shortcutbox;


import java.io.File;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import com.coco.download.DownloadList;
import com.coco.theme.themebox.ThemeInformation;
import com.coco.theme.themebox.database.model.DownloadStatus;
import com.coco.theme.themebox.database.model.ThemeInfoItem;
import com.coco.theme.themebox.database.service.DownloadThemeService;
import com.coco.theme.themebox.util.ContentConfig;
import com.coco.theme.themebox.util.Tools;


public class OperateInformation extends ThemeInformation
{
	
	public boolean isDownloaded(
			Context context )
	{
		File file = new File( PathTool.getAppDir() + "/" + getPackageName() + ".apk" );
		if( !file.exists() && ( getDownloadStatus() == DownloadStatus.StatusInit || getDownloadStatus() == DownloadStatus.StatusFinish ) )
		{
			File f1 = new File( PathTool.getDownloadingDir() + getPackageName() + "_app.tmp" );
			f1.delete();
			DownloadThemeService dSv = new DownloadThemeService( context );
			dSv.updateDownloadSizeAndStatus( getPackageName() , 0 , DownloadStatus.StatusInit , DownloadList.Operate_Type );
			return false;
		}
		return downloaded;
	}
	
	public void loadDetail(
			Context cxt )
	{
		needLoadDetail = false;
		disposeThumb();
		thumbImage = null;
		if( installed )
		{
			ContentConfig cfg = new ContentConfig();
			try
			{
				Context remoteContext = cxt.createPackageContext( themeInfo.getPackageName() , Context.CONTEXT_IGNORE_SECURITY );
				cfg.loadOperateConfig( remoteContext );
				loadInstallDetail( remoteContext , cxt , cfg );
			}
			catch( NameNotFoundException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			reloadThumb( cxt );
		}
	}
	
	public void loadInstallDetail(
			Context remoteContext ,
			Context ctx ,
			ContentConfig cfg )
	{
		needLoadDetail = false;
		disposeThumb();
		thumbImage = null;
		installed = true;
		reloadThumb( ctx );
		HotOperateService service = new HotOperateService( ctx );
		ThemeInfoItem info = service.queryByPackageName( remoteContext.getPackageName() );
		themeInfo.copyFrom( info );
		if( !mSystem )
		{
			mSystem = cfg.getReflection();
		}
	}
	
	public void reloadThumb(
			Context cxt )
	{
		// Log.d(LOG_TAG, "reloadThumb,pkg="+lockInfo.getPackageName());
		String thumbPath = PathTool.getThumbFile( themeInfo.getPackageName() );
		if( new File( thumbPath ).exists() )
		{
			try
			{
				thumbImage = Tools.getPurgeableBitmap( thumbPath , Tools.dip2px( cxt , 120 ) , Tools.dip2px( cxt , 200 ) );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
	protected void checkThemePrefix()
	{
	}
}
