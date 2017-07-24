package com.cooee.phenix.editmode.item;


import android.content.Context;

import com.cooee.phenix.editmode.interfaces.IEditControlCallBack;
import com.cooee.phenix.editmode.provider.WallpaperUtils;
import com.cooee.phenix.editmode.provider.WallpaperUtils.WallPaperFile;
import com.cooee.theme.ThemeManager;


public class EditModelWallpaperItem extends EditModelItem
{
	
	private WallPaperFile mFile = null;
	
	public WallPaperFile getWallPaperFile()
	{
		return mFile;
	}
	
	public void setWallPaperFile(
			WallPaperFile file )
	{
		this.mFile = file;
	}
	
	@Override
	public void onItemClick(
			IEditControlCallBack callback ,
			Context context )
	{
		// TODO Auto-generated method stub
		if( mFile == null )
		{
			callback.enterOrDownloadBeautyCenter( ThemeManager.BEAUTY_CENTER_TAB_WALLPAPER_CLASS_NAME );
		}
		else
		{
			WallpaperUtils.setWallpaper( context , mFile );
		}
	}
}
