package com.coco.theme.themebox;


import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.coco.font.fontbox.TabFontFactory;
import com.coco.lock2.lockbox.TabLockFactory;
import com.coco.pub.provider.PubContentProvider;
import com.coco.pub.provider.PubProviderHelper;
import com.coco.scene.scenebox.TabSceneFactory;
import com.coco.theme.themebox.util.DownModule;
import com.coco.wallpaper.wallpaperbox.TabWallpaperFactory;
import com.coco.wf.wfbox.TabEffectFactory;
import com.coco.widget.widgetbox.TabWidgetFactory;


public class MainTabActivity extends Activity
{
	
	private ContentFactory content;
	private DownModule downModule;
	private boolean isChange = false;
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		ActivityManager.pushActivity( this );
		super.onCreate( savedInstanceState );
		downModule = DownModule.getInstance( this );
		String tab = getIntent().getStringExtra( "currentTab" );
		if( tab == null || tab.equals( "tagTheme" ) )
		{
			content = new TabThemeFactory( this , downModule );
		}
		else if( tab.equals( "tagLock" ) )
		{
			content = new TabLockFactory( this , downModule );
		}
		else if( tab.equals( "tagWallpaper" ) )
		{
			content = new TabWallpaperFactory( this , downModule );
		}
		else if( tab.equals( "tagScene" ) )
		{
			content = new TabSceneFactory( this , downModule );
		}
		else if( tab.equals( "tagWidget" ) )
		{
			content = new TabWidgetFactory( this , downModule );
		}
		else if( tab.equals( "tagFont" ) )
		{
			content = new TabFontFactory( this , downModule );
		}
		else if( tab.equals( "tagEffect" ) )
		{
			content = new TabEffectFactory( this , 0 );
		}
		else
		{
			content = new TabThemeFactory( this , downModule );
		}
		setContentView( content.createTabContent( tab ) );
		IntentFilter recommendFilter = new IntentFilter();
		recommendFilter.addAction( Intent.ACTION_WALLPAPER_CHANGED );
		registerReceiver( recommendReceiver , recommendFilter );
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		content.onDestroy();
		unregisterReceiver( recommendReceiver );
		ActivityManager.popupActivity( this );
	}
	
	@Override
	protected void onActivityResult(
			int requestCode ,
			int resultCode ,
			Intent data )
	{
		// TODO Auto-generated method stub
		if( requestCode == 2001 )
		{
			WallpaperManager wallpaperManager = WallpaperManager.getInstance( this );
			WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
			if( wallpaperInfo != null )
			{
				isChange = false;
				PubProviderHelper.addOrUpdateValue( PubContentProvider.LAUNCHER_AUTHORITY , "wallpaper" , "currentWallpaper" , "other" );
				Intent intent = new Intent();
				intent.setAction( "com.coco.wallpaper.update" );
				sendBroadcast( intent );
			}
		}
	}
	
	@Override
	protected void onRestart()
	{
		// TODO Auto-generated method stub
		super.onRestart();
		if( isChange )
		{
			isChange = false;
			PubProviderHelper.addOrUpdateValue( PubContentProvider.LAUNCHER_AUTHORITY , "wallpaper" , "currentWallpaper" , "other" );
			Intent intent = new Intent();
			intent.setAction( "com.coco.wallpaper.update" );
			sendBroadcast( intent );
		}
	}
	
	@Override
	protected void onStop()
	{
		// TODO Auto-generated method stub
		isChange = false;
		super.onStop();
		if( content != null && content instanceof TabWallpaperFactory )
		{
			( (TabWallpaperFactory)content ).onStop();
		}
		Log.v( "test" , "tab onstop" );
	}
	
	private BroadcastReceiver recommendReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			if( Intent.ACTION_WALLPAPER_CHANGED.equals( intent.getAction() ) )
			{
				isChange = true;
			}
		}
	};
}
