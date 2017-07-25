package com.cooee.widgetnative.M3in1.base;


import java.io.FileDescriptor;

import org.json.JSONException;

import com.cooee.widgetnative.M3in1.R;
import com.cooee.widgetnative.M3in1.Music.BitmapUtils;
import com.cooee.widgetnative.M3in1.utils.StatisticsUtils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.RemoteViews;


public class WidgetManager
{
	
	private static WidgetManager mWidgetManager;
	public static final String TAG = "WidgetManager";
	// private RemoteViews musicRemoteViews;
	private Context mContext;
	private Bitmap defaultBitmap;
	private Bitmap currentBitmap;
	private boolean isPlaying = false;
	private int WIDGET_WIDTH = 110;
	private int WIDGET_HEIGHT = 110;
	private String songString;
	
	public static WidgetManager getInstance(
			Context context )
	{
		if( mWidgetManager == null && context != null )
		{
			synchronized( TAG )
			{
				if( mWidgetManager == null && context != null )
				{
					mWidgetManager = new WidgetManager( context );
				}
			}
		}
		return mWidgetManager;
	}
	
	private WidgetManager(
			Context context )
	{
		mContext = context;
		// 初始化默认图片
		defaultBitmap = BitmapFactory.decodeResource( mContext.getResources() , R.drawable.widget_bg );
		// initView( context );
		WIDGET_WIDTH = mContext.getResources().getDimensionPixelSize( R.dimen.widget_width_min );
		WIDGET_HEIGHT = mContext.getResources().getDimensionPixelSize( R.dimen.widget_height_min );
	}
	
	/**
	 * 更新widget
	 * 
	 * @param mRemoteViews
	 */
	public void updateAppWidget(
			RemoteViews mRemoteViews )
	{
		if( mRemoteViews == null )
		{
			Log.d( TAG , "widgetProvider --> updateAppWidget  rv == null : " );
		}
		AppWidgetManager appWidgetManger = AppWidgetManager.getInstance( mContext );
		int[] appIds = appWidgetManger.getAppWidgetIds( new ComponentName( mContext , WidgetProvider.class ) );
		appWidgetManger.updateAppWidget( appIds , mRemoteViews );
	}
	
	/**
	 * 初次运行初始化界面
	 * 
	 * @param mRemoteViews
	 */
	public void initView(
			RemoteViews mRemoteViews )
	{
		// musicRemoteViews = new RemoteViews( context.getPackageName() ,
		// R.layout.musicwidget_layout );
		Intent intent;
		PendingIntent pendingIntent;
		intent = new Intent( "android.intent.action.PICK" );
		intent.setDataAndType( Uri.EMPTY , "vnd.android.cursor.dir/playlist" );
		pendingIntent = PendingIntent.getActivity( mContext , 0 , intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK ) , 0 );
		mRemoteViews.setOnClickPendingIntent( R.id.iv_albumart , pendingIntent );
		intent = new Intent( WidgetProvider.TOGGLEPAUSE_ACTION );
		pendingIntent = PendingIntent.getBroadcast( mContext , 0 , intent , 0 );
		mRemoteViews.setOnClickPendingIntent( R.id.iv_play , pendingIntent );
		intent = new Intent( WidgetProvider.NEXT_ACTION );
		pendingIntent = PendingIntent.getBroadcast( mContext , 0 , intent , 0 );
		mRemoteViews.setOnClickPendingIntent( R.id.iv_next , pendingIntent );
		intent = new Intent( WidgetProvider.PREVIOUS_ACTION );
		pendingIntent = PendingIntent.getBroadcast( mContext , 0 , intent , 0 );
		mRemoteViews.setOnClickPendingIntent( R.id.iv_previous , pendingIntent );
	}
	
	public void changeMusicWidgetView(
			Intent intent ,
			RemoteViews mRemoteViews )
	{
		if( mRemoteViews != null )
		{
			if( intent != null )
			{
				// 获取当前音乐的bitmap
				currentBitmap = getArtwork( intent );
				if( currentBitmap == null )
				{
					currentBitmap = defaultBitmap;
				}
			}
			Log.d( "receive" , "cyk setImageViewBitmap " + currentBitmap );
			// 设置背景
			mRemoteViews.setImageViewBitmap( R.id.iv_albumart , currentBitmap );
		}
	}
	
	/**
	 * 获取歌曲名和演唱者
	 * 
	 * @param mRemoteViews
	 */
	public void setSongInfo(
			Intent intent ,
			RemoteViews mRemoteViews )
	{
		if( intent != null )
		{
			songString = intent.getStringExtra( "track" );
			Log.v( "receive" , "cyk " + songString + " intent: " + intent.getAction() );
		}
		mRemoteViews.setTextViewText( R.id.tv_song_name , songString );
	}
	
	/**
	 * 监听播放/暂停按钮状态切换背景图片
	 * 
	 * @param mRemoteViews
	 */
	public void setPlayState(
			Intent intent ,
			RemoteViews mRemoteViews )
	{
		if( intent != null )
		{
			isPlaying = intent.getBooleanExtra( "playing" , false );
			Log.v( "receive" , "cyk isPlaying " + isPlaying + " intent: " + intent.getAction() );
		}
		if( isPlaying )
		{
			mRemoteViews.setImageViewResource( R.id.iv_play , R.drawable.music_pause );
		}
		else
		{
			mRemoteViews.setImageViewResource( R.id.iv_play , R.drawable.music_play );
		}
	}
	
	/** 获取专辑封面，没有的话显示默认 */
	private Bitmap getArtwork(
			Intent intent )
	{
		String str = "content://media/external/audio/media/" + intent.getLongExtra( "id" , -1L ) + "/albumart";
		Uri uri = Uri.parse( str );
		ParcelFileDescriptor pfd = null;
		try
		{
			pfd = mContext.getContentResolver().openFileDescriptor( uri , "r" );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		if( pfd != null )
		{
			Bitmap bm = null;
			FileDescriptor fd = pfd.getFileDescriptor();
			bm = BitmapFactory.decodeFileDescriptor( fd );
			Bitmap newBitmap = getCompositeBitmap( bm , WIDGET_WIDTH , WIDGET_HEIGHT );
			Log.i( "wechat" , "压缩后图片的大小" + ( bm.getByteCount()/* / 1024 / 1024 */ ) + "byte 宽度为" + bm.getWidth() + "高度为" + bm.getHeight() );
			return newBitmap;
		}
		return null;
	}
	
	private Bitmap getCompositeBitmap(
			Bitmap newImage ,
			int width ,
			int height )
	{
		Bitmap minbmp = newImage;
		if( newImage.getWidth() != width || newImage.getHeight() != height )
		{
			minbmp = ThumbnailUtils.extractThumbnail( newImage , (int)width , (int)height );
		}
		if( minbmp != null )
		{
			Bitmap dstBitmap = BitmapUtils.adaptive( defaultBitmap , width , height );
			Bitmap roundedCornerBitmap = BitmapUtils.onCompositeImages( minbmp , dstBitmap , PorterDuff.Mode.DST_IN );
			if( !minbmp.isRecycled() && !minbmp.equals( defaultBitmap ) )
				minbmp.recycle();
			if( !newImage.isRecycled() && !newImage.equals( defaultBitmap ) ) // 新换的图不是默认图,才去释放
				newImage.recycle();
			if( !dstBitmap.isRecycled() && !dstBitmap.equals( defaultBitmap ) )
				dstBitmap.recycle();
			return roundedCornerBitmap;
		}
		return null;
	}
	
	public boolean isPlaying()
	{
		return isPlaying;
	}
	
	/* 更改 */
	public void changeAlbumArtClickState(
			Context context ,
			Intent isPlayingIntent ,
			RemoteViews mRemoteViews )
	{
		if( mRemoteViews != null )
		{
			Intent intent;
			PendingIntent pendingIntent;
			if( isPlaying )
			{
				intent = new Intent( "com.android.music.PLAYBACK_VIEWER" );
			}
			else
			{
				intent = new Intent( "android.intent.action.PICK" );
				intent.setDataAndType( Uri.EMPTY , "vnd.android.cursor.dir/playlist" );
			}
			pendingIntent = PendingIntent.getActivity( context , 0 , intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK ) , 0 );
			mRemoteViews.setOnClickPendingIntent( R.id.iv_albumart , pendingIntent );
		}
	}
	
	public void initLastView(
			RemoteViews mRemoteViews )
	{
		// TODO Auto-generated method stub
		Log.d( "" , "cyk initLastMusic :" + this );
		if( currentBitmap == null )
		{
			Log.d( "" , "cyk initLastMusic currentBitmap = defaultBitmap " );
			new Throwable().printStackTrace();
		}
		Log.d( TAG , "cyk initLastMusic currentBitmap: " + currentBitmap + " defaultBitmap: " + defaultBitmap );
		Log.d( "" , "cyk initLastMusic currentBitmap: " + currentBitmap + " songString: " + songString + " isPlaying: " + isPlaying );
		//设置背景
		mRemoteViews.setImageViewBitmap( R.id.iv_albumart , currentBitmap );
		mRemoteViews.setTextViewText( R.id.tv_song_name , songString );
		if( isPlaying )
		{
			mRemoteViews.setImageViewResource( R.id.iv_play , R.drawable.music_pause );
		}
		else
		{
			mRemoteViews.setImageViewResource( R.id.iv_play , R.drawable.music_play );
		}
	}
	
	public void doStatistics()
	{
		try
		{
			StatisticsUtils.getInstance( mContext ).olapStatistics();
		}
		catch( NameNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
