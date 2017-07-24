package com.cooee.widgetnative.enjoy.manager;


import java.io.FileDescriptor;
import java.util.Set;

import com.cooee.widgetnative.enjoy.R;
import com.cooee.widgetnative.enjoy.common.BitmapUtils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


public class MusicManager
{
	
	private static MusicManager mMusicManager;
	public static final String TAG = "MusicManager";
	/**原生音乐的音乐字符串*/
	public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
	public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
	public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";
	public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
	public static final String META_CHANGED = "com.android.music.metachanged";
	private Context mContext;
	/**音乐配置*/
	private boolean showMusicVeiw;
	//音乐信息
	//白色圆形透明图
	private Bitmap roundWhiteBitmap;
	//黑色圆形透明图
	private Bitmap roundBlackBitmap;
	//默认方图
	private Bitmap defaultBitmap;
	//处理过的圆形默认图
	private Bitmap compositeDefaultBitmap;
	//当前音乐的图
	public Bitmap currentBitmap;
	private int WIDGET_WIDTH = 110;
	private int WIDGET_HEIGHT = 110;
	private boolean isPlaying = false;
	private String musicName;
	private String musicSinger;
	
	private MusicManager(
			Context context )
	{
		mContext = context;
		initConfig();
	}
	
	public static MusicManager getInstance(
			Context mContext )
	{
		if( mMusicManager == null )
		{
			synchronized( TAG )
			{
				if( mMusicManager == null )
				{
					mMusicManager = new MusicManager( mContext );
				}
			}
		}
		return mMusicManager;
	}
	
	private void initConfig()
	{
		//音乐配置
		showMusicVeiw = mContext.getResources().getBoolean( R.bool.show_musicView );
		if( showMusicVeiw )
		{
			musicName = mContext.getResources().getString( R.string.music_def_name );
			musicSinger = mContext.getResources().getString( R.string.singer_def_name );
			//初始化默认图片
			defaultBitmap = BitmapFactory.decodeResource( mContext.getResources() , R.drawable.music_default );
			roundWhiteBitmap = BitmapFactory.decodeResource( mContext.getResources() , R.drawable.music_cd_stage );
			roundBlackBitmap = BitmapFactory.decodeResource( mContext.getResources() , R.drawable.music_cover_mask );
			WIDGET_WIDTH = roundWhiteBitmap.getWidth();
			WIDGET_HEIGHT = roundWhiteBitmap.getHeight();
			//合成默认图
			compositeDefaultBitmap = getCompositeBitmap( defaultBitmap );
			currentBitmap = compositeDefaultBitmap;
			defaultBitmap.recycle();
			defaultBitmap = null;
		}
	}
	
	/**初始化view点击事件和是否可见*/
	public void initClickView(
			RemoteViews remoteview )
	{
		if( remoteview != null )
		{
			int visibility = View.GONE;
			if( showMusicVeiw )
			{
				visibility = View.VISIBLE;
				Intent intent;
				PendingIntent pendingIntent;
				intent = new Intent( "android.intent.action.PICK" );
				intent.setDataAndType( Uri.EMPTY , "vnd.android.cursor.dir/playlist" );
				pendingIntent = PendingIntent.getActivity( mContext , 0 , intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK ) , 0 );
				remoteview.setOnClickPendingIntent( R.id.music_layout , pendingIntent );
				intent = new Intent( TOGGLEPAUSE_ACTION );
				pendingIntent = PendingIntent.getBroadcast( mContext , 0 , intent , 0 );
				remoteview.setOnClickPendingIntent( R.id.music_play , pendingIntent );
				intent = new Intent( NEXT_ACTION );
				pendingIntent = PendingIntent.getBroadcast( mContext , 0 , intent , 0 );
				remoteview.setOnClickPendingIntent( R.id.music_next , pendingIntent );
				intent = new Intent( PREVIOUS_ACTION );
				pendingIntent = PendingIntent.getBroadcast( mContext , 0 , intent , 0 );
				remoteview.setOnClickPendingIntent( R.id.music_prev , pendingIntent );
			}
			remoteview.setViewVisibility( R.id.music_layout , visibility );
		}
	}
	
	public void changeMusicWidgetView(
			Intent intent )
	{
		if( intent != null )
		{
			if( currentBitmap != null && !currentBitmap.isRecycled() && currentBitmap != compositeDefaultBitmap )
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
		if( intent != null )
		{
			Bundle extras = intent.getExtras();
			Set<String> keySet = extras.keySet();
			Log.i( TAG , "cyk setSongInfo extras " );
			for( String string : keySet )
			{
				Log.d( TAG , "cyk key:  " + string + " value: " + extras.get( string ) );
			}
			Log.e( TAG , "cyk setSongInfo extras: " );
			String musicName = intent.getStringExtra( MediaStore.Audio.Media.TRACK );
			String musicSinger = intent.getStringExtra( MediaStore.Audio.Media.ARTIST );
			if( !TextUtils.isEmpty( musicName ) )
			{
				this.musicName = musicName;
			}
			if( !TextUtils.isEmpty( musicSinger ) )
			{
				this.musicSinger = musicSinger;
			}
		}
		Log.d( TAG , "cyk setSongInfo musicName: " + musicName + " musicSinger: " + musicSinger );
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
			Bitmap newBitmap = getCompositeBitmap( bm );
			Log.i( TAG , "cyk 压缩后图片的大小" + ( bm.getByteCount()/* / 1024 / 1024*/) + "byte 宽度为" + bm.getWidth() + "高度为" + bm.getHeight() );
			return newBitmap;
		}
		return compositeDefaultBitmap;
	}
	
	private Bitmap getCompositeBitmap(
			Bitmap newImage )
	{
		Bitmap iconBitmap = newImage;
		//计算icon的图片大小
		//图片缩放
		if( newImage.getWidth() != WIDGET_WIDTH || newImage.getHeight() != WIDGET_HEIGHT )
		{
			iconBitmap = ThumbnailUtils.extractThumbnail( newImage , (int)WIDGET_WIDTH , (int)WIDGET_HEIGHT );
		}
		if( iconBitmap != null )
		{
			//缩放蒙版
			Bitmap maskBitmap = BitmapUtils.adaptive( roundBlackBitmap , WIDGET_WIDTH , WIDGET_HEIGHT );
			//图片合成
			Bitmap roundedCornerBitmap = BitmapUtils.onCompositeImages( roundWhiteBitmap , iconBitmap , maskBitmap );
			if( !newImage.isRecycled() )//新换的图不是默认图,才去释放
				newImage.recycle();
			if( !iconBitmap.isRecycled() )
				iconBitmap.recycle();
			if( !maskBitmap.isRecycled() && !maskBitmap.equals( roundBlackBitmap ) )
				maskBitmap.recycle();
			return roundedCornerBitmap;
		}
		return null;
	}
	
	/**设置播放状态*/
	public void setPlayingState(
			Context context ,
			Intent isPlayingIntent )
	{
		if( isPlayingIntent != null )
		{
			isPlaying = isPlayingIntent.getBooleanExtra( "playing" , false );
		}
		Log.d( TAG , "cyk setPlayingState isPlaying : " + isPlaying );
	}
	
	/**监听播放/暂停按钮状态切换点击事件*/
	private void changeAlbumArtClickState(
			Context context ,
			RemoteViews rv )
	{
		if( rv != null )
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
			rv.setOnClickPendingIntent( R.id.music_layout , pendingIntent );
		}
	}
	
	public void updateAllWidget(
			RemoteViews rv )
	{
		if( showMusicVeiw )
		{
			Log.d( TAG , "cyk initLastMusic :" + this );
			if( currentBitmap == null )
			{
				Log.d( TAG , "cyk initLastMusic currentBitmap = compositeDefaultBitmap " );
				new Throwable().printStackTrace();
			}
			Log.d( TAG , "cyk initLastMusic currentBitmap: " + currentBitmap + " compositeDefaultBitmap: " + compositeDefaultBitmap );
			Log.d( TAG , "cyk initLastMusic currentBitmap: " + currentBitmap + " musicName: " + musicName + " isPlaying: " + isPlaying );
			//设置背景
			rv.setImageViewBitmap( R.id.music_round , currentBitmap );
			rv.setTextViewText( R.id.music_name , musicName );
			rv.setTextViewText( R.id.music_singer , musicSinger );
			if( isPlaying )
			{
				rv.setImageViewResource( R.id.music_play , R.drawable.btn_pause );
			}
			else
			{
				rv.setImageViewResource( R.id.music_play , R.drawable.btn_play );
			}
			changeAlbumArtClickState( mContext , rv );
		}
	}
	
	public boolean isShowMusicVeiw()
	{
		return showMusicVeiw;
	}
}
