package com.cooee.theme;


import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


// gaominghui@2017/01/09 ADD START
/**
 * 壁纸工具类
 * @author gaominghui 2017年1月9日
 */
public class WallpaperManagerUtil
{
	
	private static final String TAG = "WallpaperManager";
	
	//cheyingkun add start	//是否监听飞利浦壁纸改变广播【c_0003456】
	/**
	 * 桌面onStart时,根据存的壁纸尺寸设置壁纸
	 */
	public static void setWallpaperDimensionBySharedPreferencesOnStart(
			Context context )
	{
		final WallpaperManager mWallpaperManager = WallpaperManager.getInstance( context );
		int desiredWidth = mWallpaperManager.getDesiredMinimumWidth();
		int desiredHeight = mWallpaperManager.getDesiredMinimumHeight();
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
		final int mWidth = mSharedPreferences.getInt( LauncherAppState.WALLPAPER_DESIRED_WIDTH , desiredWidth );
		final int mHeight = mSharedPreferences.getInt( LauncherAppState.WALLPAPER_DESIRED_HEIGHT , desiredHeight );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d(
					"WallpaperManager" ,
					StringUtils.concat( " philips.wallpaperChanged  set  mWidth:" , mWidth , " mHeight:" , mHeight , " desiredWidth: " , desiredWidth , " desiredHeight: " , desiredHeight ) );
		if( desiredWidth == mWidth && desiredHeight == mHeight )
		{
			return;
		}
		new Thread( "setWallpaperDimension" ) {
			
			@Override
			public void run()
			{
				mWallpaperManager.suggestDesiredDimensions( mWidth , mHeight );
			}
		}.start();
	}
}
//gaominghui@2017/01/09 ADD END
