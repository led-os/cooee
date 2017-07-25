package com.cooee.widgetnative.M3in1.base;


import com.cooee.widgetnative.M3in1.R;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;


public class WidgetProvider extends AppWidgetProvider
{
	
	public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
	public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
	public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";
	//
	public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
	public static final String META_CHANGED = "com.android.music.metachanged";
	public static final String ACTION_SEND_MUSIC_POSITION = "com.android.music.send_music_position";
	
	@Override
	public void onReceive(
			Context context ,
			Intent intent )
	{
		super.onReceive( context , intent );
		Log.v( "" , "cyk1111 " + intent.getAction() );
		//		RemoteViews remoteViews = new RemoteViews( context.getPackageName() , R.layout.musicwidget_layout );
		//		MusicWidgetManager.getInstance( context ).initView( remoteViews );
		//		MusicWidgetManager.getInstance( context ).updateAppWidget( remoteViews );
	}
	
	@Override
	public void onEnabled(
			Context context )
	{
		super.onEnabled( context );
		Log.v( "" , "cyk1111 onEnabled " );
	}
	
	@Override
	public void onDisabled(
			Context context )
	{
		super.onDisabled( context );
		Log.v( "" , "cyk1111 onDisabled " );
		context.stopService( new Intent( context , WidgetService.class ) );
	}
	
	@Override
	public void onUpdate(
			Context context ,
			AppWidgetManager appWidgetManager ,
			int[] appWidgetIds )
	{
		// TODO Auto-generated method stub
		super.onUpdate( context , appWidgetManager , appWidgetIds );
		Log.v( "" , "cyk1111 onUpdate " );
		context.startService( new Intent( context , WidgetService.class ) );
		RemoteViews mRemoteViews = new RemoteViews( context.getPackageName() , R.layout.widget_layout );
		WidgetManager mWidgetManager = WidgetManager.getInstance( context );
		mWidgetManager.initView( mRemoteViews );
		mWidgetManager.changeAlbumArtClickState( context , null , mRemoteViews );
		mWidgetManager.initLastView( mRemoteViews );
		//		MusicWidgetManager.getInstance( context ).setSongInfo( null , remoteViews );
		//		MusicWidgetManager.getInstance( context ).changeMusicWidgetView( null , remoteViews );
		//		MusicWidgetManager.getInstance( context ).setPlayState( null , remoteViews );
		//		MusicWidgetManager.getInstance( context ).changeAlbumArtClickState( context , null , remoteViews );
		appWidgetManager.updateAppWidget( appWidgetIds , mRemoteViews );
		//添加插件统计
		WidgetManager.getInstance( context ).doStatistics();
		context.startService( new Intent( context , WidgetService.class ) );
	}
}
