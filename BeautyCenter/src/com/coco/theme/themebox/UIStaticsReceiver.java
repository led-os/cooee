package com.coco.theme.themebox;


import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import com.coco.download.Assets;
import com.coco.download.DownloadList;
import com.coco.shortcut.shortcutbox.HotOperateService;
import com.coco.theme.themebox.database.service.HotService;
import com.coco.theme.themebox.update.UpdateService;
import com.coco.theme.themebox.util.DownloadEngineApkService;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Log;
import com.coco.theme.themebox.util.Tools;


public class UIStaticsReceiver extends BroadcastReceiver
{
	
	@Override
	public void onReceive(
			final Context context ,
			final Intent intent )
	{
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.v( "UIStaticsReceiver" , "onReceive----action=" + action );
		if( Intent.ACTION_PACKAGE_ADDED.equals( action ) )
		{
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					precessInstallApkInfo( context , intent );
				}
			} ).start();
		}
		else if( Intent.ACTION_PACKAGE_REMOVED.equals( action ) )
		{
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					precessRemoveApkInfo( context , intent );
				}
			} ).start();
		}
		else if( "cn.moppo.fontstore.flipfont.font_set".equals( action ) )
		{
			ActivityManager.KillSomeActivity( "FontPreviewActivity" );
		}
		else if( "com.coco.personalCenter.stop.update".equals( action ) )
		{
			Intent it = new Intent( context , UpdateService.class );
			context.stopService( it );
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences( context ).edit();
			editor.putLong( "selfrefresh" , 0 );
			editor.commit();
		}
		else if( "com.coco.engine.stop.download".equals( action ) )
		{
			Intent it = new Intent( context , DownloadEngineApkService.class );
			it.putExtra( "stop" , "stopcurrent" );
			context.startService( it );
		}
		else if( "com.android.topwise.system_fonts".equals( action ) )
		{
			if( FunctionConfig.isEnable_topwise_style() )
			{
				String path = intent.getStringExtra( "path" );
				if( path != null )
				{
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( context );
					pref.edit().putString( "currentFont" , path ).commit();
				}
			}
		}
		else if( "com.hw.lockwallpaper.change".equals( action ) )
		{
			WallpaperManager wpm = (WallpaperManager)context.getSystemService( Context.WALLPAPER_SERVICE );
			Drawable drawable = wpm.getDrawable();
			Bitmap wallpaperBitmap = ( (BitmapDrawable)drawable ).getBitmap();
			if( FunctionConfig.getLockWallpaperPath() == null || FunctionConfig.getLockWallpaperPath().equals( "" ) )
			{
				Tools.saveMyBitmap( "/data/data/com.iLoong.base.themebox/lockwallpapers" , wallpaperBitmap );
			}
			else
			{
				Tools.saveMyBitmap( FunctionConfig.getLockWallpaperPath() , wallpaperBitmap );
			}
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( context );
			pref.edit().putString( "lockWallpaper" , Assets.getHWWallpaper( context , "currentWallpaper" ) ).commit();
			Log.v( "test" , "Assets.getWallpaper( context  ) = " + Assets.getHWWallpaper( context , "currentWallpaper" ) );
		}
	}
	
	private void precessRemoveApkInfo(
			final Context context ,
			final Intent intent )
	{
		String packageName = intent.getData().getSchemeSpecificPart();
		HotService sv = new HotService( context );
		String resid = sv.queryResid( packageName );
		if( resid != null )
		{
			DownloadList.getInstance( context ).startUICenterLog( DownloadList.ACTION_UNINSTALL_LOG , resid , packageName );
		}
		else
		{
			HotOperateService opsv = new HotOperateService( context );
			resid = opsv.queryResid( packageName );
			if( resid != null )
			{
				DownloadList.getInstance( context ).startUICenterLog( DownloadList.ACTION_UNINSTALL_LOG , resid , packageName );
			}
		}
		Log.v( "UIStaticsReceiver" , "ACTION_PACKAGE_REMOVED----packageName=" + packageName );
	}
	
	private void precessInstallApkInfo(
			Context context ,
			Intent intent )
	{
		String packageName = intent.getData().getSchemeSpecificPart();
		HotService sv = new HotService( context );
		String resid = sv.queryResid( packageName );
		if( resid != null )
		{
			DownloadList.getInstance( context ).startUICenterLog( DownloadList.ACTION_INSTALL_LOG , resid , packageName );
		}
		else
		{
			HotOperateService opsv = new HotOperateService( context );
			resid = opsv.queryResid( packageName );
			if( resid != null )
			{
				DownloadList.getInstance( context ).startUICenterLog( DownloadList.ACTION_INSTALL_LOG , resid , packageName );
			}
		}
		Log.v( "UIStaticsReceiver" , "ACTION_PACKAGE_ADDED----packageName=" + packageName );
	}
}
