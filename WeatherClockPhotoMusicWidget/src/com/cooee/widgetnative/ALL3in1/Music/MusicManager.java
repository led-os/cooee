package com.cooee.widgetnative.ALL3in1.Music;


import java.io.FileDescriptor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.RemoteViews;

import com.cooee.widgetnative.ALL3in1.R;
import com.cooee.widgetnative.ALL3in1.Photo.utils.BitmapUtils;
import com.cooee.widgetnative.ALL3in1.base.WidgetManager;
import com.cooee.widgetnative.ALL3in1.base.WidgetProvider;


public class MusicManager
{
	
	private static MusicManager mMusicManager;
	public static final String TAG = "MusicManager";
	private Context mContext;
	private Bitmap defaultBitmap;
	public Bitmap currentBitmap;
	private int WIDGET_WIDTH = 110;
	private int WIDGET_HEIGHT = 110;
	public boolean isPlaying = false;
	public String songString;
	
	public static MusicManager getInstance(
			Context context )
	{
		if( mMusicManager == null && context != null )
		{
			synchronized( TAG )
			{
				if( mMusicManager == null && context != null )
				{
					mMusicManager = new MusicManager( context );
				}
			}
		}
		return mMusicManager;
	}
	
	private MusicManager(
			Context context )
	{
		Log.d( "" , "cyk MusicManager :" + this );
		mContext = context;
		//初始化默认图片
		defaultBitmap = BitmapFactory.decodeResource( mContext.getResources() , R.drawable.widget_music_bg );
		WIDGET_WIDTH = mContext.getResources().getDimensionPixelSize( R.dimen.music_width_min );
		WIDGET_HEIGHT = mContext.getResources().getDimensionPixelSize( R.dimen.music_height_min );
		initView( context );
	}
	
	/**初次运行初始化界面*/
	public void initView(
			Context context )
	{
		RemoteViews mRemoteViews = WidgetManager.getInstance( context ).getRemoteViews();
		Intent intent;
		PendingIntent pendingIntent;
		intent = new Intent( "android.intent.action.PICK" );
		intent.setDataAndType( Uri.EMPTY , "vnd.android.cursor.dir/playlist" );
		pendingIntent = PendingIntent.getActivity( context , 0 , intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK ) , 0 );
		mRemoteViews.setOnClickPendingIntent( R.id.iv_albumart , pendingIntent );
		intent = new Intent( WidgetProvider.TOGGLEPAUSE_ACTION );
		pendingIntent = PendingIntent.getBroadcast( context , 0 , intent , 0 );
		mRemoteViews.setOnClickPendingIntent( R.id.iv_play , pendingIntent );
		intent = new Intent( WidgetProvider.NEXT_ACTION );
		pendingIntent = PendingIntent.getBroadcast( context , 0 , intent , 0 );
		mRemoteViews.setOnClickPendingIntent( R.id.iv_next , pendingIntent );
		intent = new Intent( WidgetProvider.PREVIOUS_ACTION );
		pendingIntent = PendingIntent.getBroadcast( context , 0 , intent , 0 );
		mRemoteViews.setOnClickPendingIntent( R.id.iv_previous , pendingIntent );
	}
	
	public void changeMusicWidgetView(
			Intent intent )
	{
		RemoteViews mRemoteViews = WidgetManager.getInstance( mContext ).getRemoteViews();
		if( mRemoteViews != null )
		{
			if( currentBitmap != null && !currentBitmap.isRecycled() && currentBitmap != defaultBitmap )
			{
				currentBitmap.recycle();
			}
			//获取当前音乐的bitmap
			currentBitmap = getArtwork( intent );
		}
		Log.d( TAG , "cyk changeMusicWidgetView currentBitmap: " + currentBitmap + " defaultBitmap: " + defaultBitmap );
	}
	
	/**获取歌曲名和演唱者*/
	public void setSongInfo(
			Intent intent )
	{
		songString = intent.getStringExtra( "track" );
		Log.d( TAG , "cyk setSongInfo songString: " + songString );
	}
	
	/**获取专辑封面，没有的话显示默认*/
	private Bitmap getArtwork(
			Intent intent )
	{
		String str = "content://media/external/audio/media/" + intent.getLongExtra( "id" , -1L ) + "/albumart";
		Log.d( TAG , "获取封面" + str );
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
			Log.i( TAG , "cyk 压缩后图片的大小" + ( bm.getByteCount()/* / 1024 / 1024*/) + "byte 宽度为" + bm.getWidth() + "高度为" + bm.getHeight() );
			return newBitmap;
		}
		return defaultBitmap;
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
			if( !newImage.isRecycled() && !newImage.equals( defaultBitmap ) )//新换的图不是默认图,才去释放
				newImage.recycle();
			if( !minbmp.isRecycled() && !minbmp.equals( defaultBitmap ) )
				minbmp.recycle();
			if( !dstBitmap.isRecycled() && !dstBitmap.equals( defaultBitmap ) )
				dstBitmap.recycle();
			return roundedCornerBitmap;
		}
		return null;
	}
	
	/**监听播放/暂停按钮状态切换背景图片*/
	public void setPlayingState(
			Context context ,
			Intent isPlayingIntent )
	{
		isPlaying = isPlayingIntent.getBooleanExtra( "playing" , false );
		Log.d( TAG , "cyk setPlayingState isPlaying : " + isPlaying );
	}
	
	/**监听播放/暂停按钮状态切换点击事件*/
	public void changeAlbumArtClickState(
			Context context )
	{
		RemoteViews mRemoteViews = WidgetManager.getInstance( context ).getRemoteViews();
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
	
	public void initLastMusic()
	{
		Log.d( "" , "cyk initLastMusic :" + this );
		RemoteViews mRemoteViews = WidgetManager.getInstance( mContext ).getRemoteViews();
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
}
