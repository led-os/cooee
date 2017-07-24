/***/
package com.cooee.phenix.musicpage.control;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicpage.entity.MusicData;
import com.cooee.phenix.musicpage.vivo.GetVIVOonLineAlbumRegionThredManager;
import com.cooee.phenix.musicpage.vivo.GetVIVOonLineAlbumRegionThredManager.CallBack;


/**
 * @author gaominghui 2016年9月20日
 */
public class Vivo implements MusicControlInterface
{
	
	private static final String TAG = "MusicPage_Vivo";
	public static final String ACTION_SERVICE_START = "com.android.bbkmusic.action.service.start";
	public static final String ACTION_ENTRY_MUSIC_APP = "com.android.bbkmusic.restartBBKMusic";
	public static final String ACTION_LOCAL_MUSIC_NEXT = "com.android.music.musicservicecommand.next";
	public static final String ACTION_LOCAL_MUSIC_PLAY_POSITION = "com.android.music.musicservicecommand.playposition";
	public static final String ACTION_LOCAL_MUSIC_PREV = "com.android.music.musicservicecommand.previous";
	public static final String ACTION_LOCAL_MUSIC_TOGGLE_PAUSE = "com.android.music.musicservicecommand.togglepause";
	public static final String ACTION_ONLINE_MUSIC_NEXT = "com.bbk.next.online.music";
	public static final String ACTION_ONLINE_MUSIC_PAUSE = "com.bbk.pause.online.music";
	public static final String ACTION_ONLINE_MUSIC_PLAY = "com.bbk.play.online.music";
	public static final String ACTION_ONLINE_MUSIC_PREV = "com.bbk.pre.online.music";
	public static final String CMD_NAME = "command";
	public static final String CMD_NEXT = "next";
	public static final String CMD_NOOP = "nooperation";
	public static final String CMD_PLAY_ORDER = "orderplayposition";
	public static final String CMD_PLAY_PAUSE = "togglepause";
	public static final String CMD_PLAY_POS = "playposition";
	public static final String CMD_PREV = "previous";
	public static final String ACTION_ONLING_SEND_MUSIC_INFO = "com.android.music.send_music_info";
	public static final String ACTION_ONLING_SEND_MUSIC_ALBUM_URL = "com.android.music.send_music_album_url";
	public static final String ACTION_SEND_MUSIC_POSITION = "com.android.music.send_music_position";
	public static final String META_CHANGED = "com.android.music.metachanged";
	public static final String POSITION_CHANGED = "com.android.music.position_changed";
	public static final String PLAY_STATE_CHANGED = "com.android.music.playstatechanged";
	public static final String PLAY_BACK_COMPLETE = "com.android.music.playbackcomplete";
	public static final String ACTION_QUEUE_CHANGED = "com.android.music.queuechanged";
	private ComponentName componentName = new ComponentName( "com.android.bbkmusic" , "com.android.bbkmusic.MediaPlaybackService" );
	private MusicData musicData = null;
	private MusicData onLineMusicData = null;
	private boolean isOnlineMusic = false;
	private Activity activity = null;
	private MusicControlCallBack callBack = null;
	private long position = 0;
	private long duration = 0;
	private boolean mIsPlaying = false;
	
	/**
	 * @param mAppContext
	 */
	public boolean isHigherROM()
	{
		PackageManager localPackageManager = activity.getPackageManager();
		try
		{
			String str = localPackageManager.getPackageInfo( "com.android.bbkmusic" , 1 ).versionName;
			if( ( str != null ) && ( ( str.startsWith( "1." ) ) || ( str.startsWith( "2.0" ) ) ) )
				return false;
			else
			{
				return true;
			}
		}
		catch( Exception localException )
		{
			return true;
		}
	}
	
	public void nextSongAtOnline()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "nextSongAtOnline!!" );
		Intent localIntent = new Intent();
		localIntent.setAction( ACTION_ONLINE_MUSIC_NEXT );
		activity.sendBroadcast( localIntent );
	}
	
	public void pauseSongAtOnline()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "pauseSongAtOnline!!" );
		Intent localIntent = new Intent();
		localIntent.setAction( ACTION_ONLINE_MUSIC_PAUSE );
		activity.sendBroadcast( localIntent );
	}
	
	public void playSongAtOnline()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "playSongAtOnline!!" );
		Intent localIntent = new Intent();
		localIntent.setAction( ACTION_ONLINE_MUSIC_PLAY );
		activity.sendBroadcast( localIntent );
	}
	
	public void prevSongAtOnline()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "prevSongAtOnline!!" );
		Intent localIntent = new Intent();
		localIntent.setAction( ACTION_ONLINE_MUSIC_PREV );
		activity.sendBroadcast( localIntent );
	}
	
	public boolean isVivoInstalled()
	{
		try
		{
			activity.createPackageContext( "com.android.bbkmusic" , Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY );
		}
		catch( Exception e )
		{
			return false;
		}
		return true;
	}
	
	/**
	 * @return the isOnlineMusic
	 */
	public boolean isOnlineMusic()
	{
		return isOnlineMusic;
	}
	
	/**
	 * @param isOnlineMusic the isOnlineMusic to set
	 */
	public void setOnlineMusic(
			boolean isOnlineMusic )
	{
		this.isOnlineMusic = isOnlineMusic;
	}
	
	public void startMusicService(
			Context context )
	{
		if( this.isHigherROM() )
		{
			final Intent intent = new Intent( "com.android.bbkmusic.action.service.start" );
			intent.putExtra( "command" , "nooperation" );
			context.startService( intent );
		}
		else
		{
			final Intent intent2 = new Intent( "com.android.music.musicservicecommand.nooperation" );
			intent2.setComponent( this.componentName );
			context.startService( intent2 );
		}
	}
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#init(android.app.Activity, com.cooee.phenix.musicpage.control.MusicControlCallBack)
	 * @auther gaominghui  2016年11月1日
	 */
	@Override
	public void init(
			Activity activity ,
			MusicControlCallBack callBack )
	{
		this.activity = activity;
		this.callBack = callBack;
		//
		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction( META_CHANGED );
		commandFilter.addAction( PLAY_STATE_CHANGED );
		commandFilter.addAction( POSITION_CHANGED );
		commandFilter.addAction( ACTION_ONLINE_MUSIC_NEXT );
		commandFilter.addAction( ACTION_ONLINE_MUSIC_PAUSE );
		commandFilter.addAction( ACTION_ONLINE_MUSIC_PLAY );
		commandFilter.addAction( ACTION_ONLINE_MUSIC_PREV );
		commandFilter.addAction( ACTION_ONLING_SEND_MUSIC_INFO );
		commandFilter.addAction( ACTION_ONLING_SEND_MUSIC_ALBUM_URL );
		commandFilter.addAction( ACTION_SEND_MUSIC_POSITION );
		commandFilter.addAction( PLAY_BACK_COMPLETE );
		commandFilter.addAction( ACTION_QUEUE_CHANGED );
		activity.registerReceiver( receiver , commandFilter );
		IntentFilter mediaFilter = new IntentFilter();
		mediaFilter.addAction( Intent.ACTION_MEDIA_EJECT );
		mediaFilter.addAction( Intent.ACTION_MEDIA_MOUNTED );
		mediaFilter.addAction( Intent.ACTION_MEDIA_UNMOUNTED );
		mediaFilter.addDataScheme( "file" );
		activity.registerReceiver( receiver , mediaFilter );
		startMusicService( activity );
	}
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#play()
	 * @auther gaominghui  2016年11月1日
	 */
	@Override
	public void play()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "play.." );
		if( isOnlineMusic )
		{
			playSongAtOnline();
			return;
		}
		if( isHigherROM() )
		{
			Intent localIntent1 = new Intent( ACTION_SERVICE_START );
			localIntent1.putExtra( CMD_NAME , CMD_PLAY_PAUSE );
			activity.startService( localIntent1 );
		}
		else
		{
			Intent localIntent2 = new Intent( ACTION_LOCAL_MUSIC_TOGGLE_PAUSE );
			localIntent2.setComponent( this.componentName );
			activity.startService( localIntent2 );
		}
	}
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#pause()
	 * @auther gaominghui  2016年11月1日
	 */
	@Override
	public void pause()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "Pause.." );
		if( isOnlineMusic )
		{
			pauseSongAtOnline();
			return;
		}
		if( isHigherROM() )
		{
			Intent localIntent1 = new Intent( ACTION_SERVICE_START );
			localIntent1.putExtra( CMD_NAME , CMD_PLAY_PAUSE );
			activity.startService( localIntent1 );
		}
		else
		{
			Intent localIntent2 = new Intent( ACTION_LOCAL_MUSIC_TOGGLE_PAUSE );
			localIntent2.setComponent( this.componentName );
			activity.startService( localIntent2 );
		}
	}
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#next()
	 * @auther gaominghui  2016年11月1日
	 */
	@Override
	public void next()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "nextSong.." );
		if( isOnlineMusic )
		{
			nextSongAtOnline();
			return;
		}
		if( isHigherROM() )
		{
			Intent localIntent1 = new Intent( ACTION_SERVICE_START );
			localIntent1.putExtra( CMD_NAME , CMD_NEXT );
			activity.startService( localIntent1 );
		}
		else
		{
			Intent localIntent2 = new Intent( ACTION_LOCAL_MUSIC_NEXT );
			localIntent2.setComponent( this.componentName );
			activity.startService( localIntent2 );
		}
	}
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#previous()
	 * @auther gaominghui  2016年11月1日
	 */
	@Override
	public void previous()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "prevSong.." );
		if( isOnlineMusic )
		{
			prevSongAtOnline();
			return;
		}
		if( isHigherROM() )
		{
			Intent localIntent1 = new Intent( ACTION_SERVICE_START );
			localIntent1.putExtra( CMD_NAME , CMD_PREV );
			activity.startService( localIntent1 );
		}
		else
		{
			Intent localIntent2 = new Intent( ACTION_LOCAL_MUSIC_PREV );
			localIntent2.setComponent( this.componentName );
			activity.startService( localIntent2 );
		}
	}
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#seek(long, long)
	 * @auther gaominghui  2016年11月1日
	 */
	@Override
	public void seek(
			long position ,
			long duration )
	{
		// TODO Auto-generated method stub
	}
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#getPosition()
	 * @auther gaominghui  2016年11月1日
	 */
	@Override
	public long getPosition()
	{
		// TODO Auto-generated method stub
		if( musicData != null )
		{
			return musicData.getPosition();
		}
		else
		{
			return -1;
		}
	}
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#getDuration()
	 * @auther gaominghui  2016年11月1日
	 */
	@Override
	public long getDuration()
	{
		// TODO Auto-generated method stub
		if( musicData != null )
		{
			return musicData.getDuration();
		}
		else
		{
			return 0;
		}
	}
	
	long delaySecend = 0;
	
	private void updateMusicProgress()
	{
		if( musicData != null )
		{
			long position = musicData.getPosition() + delaySecend;
			musicData.setPosition( position );
		}
		if( mIsPlaying )
		{
			this.musicHandler.removeMessages( MESSAGE_PROGRESS );
			this.musicHandler.sendEmptyMessageDelayed( MESSAGE_PROGRESS , delaySecend );
		}
	}
	
	private static final int MESSAGE_PROGRESS = 7;
	private Handler musicHandler = new Handler() {
		
		public void handleMessage(
				Message msg )
		{
			if( msg.what == MESSAGE_PROGRESS )
			{
				removeMessages( 7 );
				updateMusicProgress();
			}
		};
	};
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#enterClient(com.cooee.phenix.musicpage.entity.MusicData, boolean)
	 * @auther gaominghui  2016年11月1日
	 */
	@Override
	public void enterClient(
			MusicData curMusicData ,
			boolean isPlaying )
	{
		Intent localIntent = new Intent( ACTION_ENTRY_MUSIC_APP );
		activity.sendBroadcast( localIntent );
	}
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#finish()
	 * @auther gaominghui  2016年11月1日
	 */
	@Override
	public void finish()
	{
		if( activity != null && receiver != null )
			activity.unregisterReceiver( receiver );
		activity = null;
		receiver = null;
		callBack = null;
	}
	
	private void onPlayStateChanged(
			Intent intent )
	{
		boolean playing = intent.getBooleanExtra( "playing" , false );
		mIsPlaying = playing;
		long paramId = intent.getLongExtra( "id" , -1 );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "PLAY_STATE_CHANGED playing:" , playing , "-intent:" , intent.getExtras().toString() , "-paramId:" , paramId ) );
		/*if( paramId != -1 )
		{
			//musicData = MusicDataUtils.getMusicDataByParamId( activity , paramId );
			if( playing && isOnlineMusic() )
			{
				return;
			}
			if( playing )
				callBack.onMusicPlay();
			else
				callBack.onMusicPause();
		}
		else*/
		{
			Bundle bundle = intent.getBundleExtra( "updatePlaylist" );
			if( bundle != null )
			{
				boolean isOnline = bundle.getBoolean( "ISONLINE" , false );
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( "Widget_MusicController" , StringUtils.concat( "PLAY_STATE_CHANGED ISONLINE:" , isOnline ) );
				setOnlineMusic( isOnline );
				if( !isOnline )
				{
					return;
				}
			}
		}
	}
	
	// gaominghui@2016/12/01 ADD START
	private int mPosition = -1;
	private boolean hasPlaying = false;
	
	// gaominghui@2016/12/01 ADD END
	private void onMetaChange(
			Activity activity ,
			Intent intent )
	{
		long paramId = intent.getLongExtra( "id" , -1 );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "onMetaChange paramId:" , paramId ) );
		/*if( paramId != -1 )
		{
			Log.i( TAG , "intent.getExtras = " + intent.getExtras() );
			//musicData = MusicDataUtils.getMusicDataByParamId( activity , paramId );
			mIsPlaying = intent.getBooleanExtra( "playing" , false );
			Log.i( TAG , " onMetaChange isOnlineMusic() = " + isOnlineMusic() + "; isPlaying = " + mIsPlaying );
			if( musicData != null )
			{
				Log.i( TAG , "onMetaChange musicData.getDuration() = " + musicData.getDuration() );
			}
			if( !mIsPlaying &&isOnlineMusic() )
			{
				return;
			}
			setOnlineMusic( false );
			updateMusicProgress();
			callBack.onMusicInfoChange( activity , musicData );
			if( mIsPlaying )
			{
				callBack.onMusicPlay();
				Log.e( TAG , "onMetaChange paramId != -1 callBack.onMusicPlay()" );
			}
			else
				callBack.onMusicPause();
		}
		else*/
		{
			Bundle bundle = intent.getBundleExtra( "updatePlaylist" );
			if( bundle != null )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , StringUtils.concat( "onMetaChange bundle:" , bundle.toString() ) );
				isOnlineMusic = bundle.getBoolean( "ISONLINE" , false );
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "onMetaChange isOnline = " + isOnlineMusic );
				if( !isOnlineMusic )
				{
					musicData = new MusicData();
					// gaominghui@2016/11/28 ADD START 
					onLineMusicData = null;
					// gaominghui@2016/11/28 ADD END
					//Log.i( TAG , "onMetaChange isOnline = " + bundle.getBoolean( "ISONLINE" , false ) );
					mIsPlaying = bundle.getBoolean( "ISPLAYING" , false );
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , StringUtils.concat( "onMetaChange mIsPlaying:" , mIsPlaying ) );
					int position = bundle.getInt( "POSITION" );
					if( position != -1 )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , StringUtils.concat( "onMetaChange position:" , position , "-paramId:" , paramId ) );
						String[] songList = bundle.getStringArray( "TRACKLIST" );
						if( songList != null && songList.length > position )
						{
							musicData.setTitle( songList[position] );
						}
						String[] artList = bundle.getStringArray( "ARTISTLIST" );
						if( artList != null && artList.length > position )
						{
							musicData.setArtist( artList[position] );
						}
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "musicData.getPosition():" + musicData.getPosition() + "-musicData.getDuration():" + musicData.getDuration() );
						musicData.setPosition( bundle.getLong( "CURRENT_POS" ) );
						musicData.setDuration( bundle.getLong( "DURATION" )/* bundle.getLong( "CURRENT_POS" )*/);
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , StringUtils.concat( "DURATION:" , bundle.getLong( "DURATION" ) , "-CURRENT_POS:" , bundle.getLong( "CURRENT_POS" ) ) );
						delaySecend = 800l;
						updateMusicProgress();
						long[] songIDArray = bundle.getLongArray( "PLAYLIST" );
						if( songIDArray != null && songIDArray.length > position )
						{
							musicData.setId( songIDArray[position] );
						}
						long[] alumbIdArray = bundle.getLongArray( "ALBUMLIST" );
						if( alumbIdArray != null && alumbIdArray.length > position )
						{
							musicData.setAlbum_id( alumbIdArray[position] );
						}
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , "onMetaChange musicData = " + musicData );
						callBack.onMusicInfoChange( activity , musicData );
						// gaominghui@2016/12/01 ADD START
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , StringUtils.concat( "onMetaChange hasPlaying:" , hasPlaying , "-mIsPlaying:" , mIsPlaying , "-mPosition:" , mPosition , "-position:" , position ) );
						if( !( hasPlaying ^ mIsPlaying ) && mPosition == position )
						{
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.i( TAG , "onMetaChange return!!!" );
							return;
						}
						hasPlaying = mIsPlaying;
						mPosition = position;
						// gaominghui@2016/12/01 ADD END
						if( mIsPlaying )
						{
							callBack.onMusicPlay();
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.e( TAG , "onMetaChange paramId == -1 callBack.onMusicPlay()" );
						}
						else
						{
							callBack.onMusicPause();
							if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.e( TAG , "onMetaChange paramId == -1 callBack.onMusicPause()" );
						}
					}
				}
			}
		}
	}
	
	/*private void onPositionChange(
			Intent intent )
	{
		Bundle extras = intent.getExtras();
		boolean playing = intent.getBooleanExtra( "playing" , false );
		mIsPlaying = playing;
		long paramId = intent.getLongExtra( "id" , -1 );
		Log.i( TAG , "onPositionChange paramId = " + paramId + "; extras = " + extras );
		if( paramId != -1 )
		{
			setOnlineMusic( false );
			if( extras != null )
			{
				long position = extras.getLong( "position" , 0 );
				long duration = extras.getLong( "duration" , 0 );
				callBack.onMusicPositionChange( position , duration );
				if( !playing )
				{
					callBack.onMusicPlay();
				}
			}
		}
	}*/
	private void onLineMusicInfo(
			Intent intent )
	{
		Bundle bundle = intent.getBundleExtra( "updatePlaylist" );
		//Log.i( TAG , "MUSIC_INFO intent = " + intent.getExtras() );
		musicData = null;
		musicData = new MusicData();
		onLineMusicData = null;
		if( bundle != null )
		{
			boolean isOnline = bundle.getBoolean( "ISONLINE" , false );
			setOnlineMusic( isOnline );
			boolean isPlaying = bundle.getBoolean( "ISPLAYING" , false );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "onLineMusicInfo isPlaying:" , isPlaying ) );
			int position = bundle.getInt( "POSITION" );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "onLineMusicInfo position:" , position ) );
			/*musicData.setPosition( position );*/
			String[] songList = bundle.getStringArray( "TRACKLIST" );
			if( songList != null && songList.length > position )
			{
				musicData.setTitle( songList[position] );
				//Log.i( TAG , "onLineMusicInfo Title = " + songList[position] );
			}
			String[] artList = bundle.getStringArray( "ARTISTLIST" );
			if( artList != null && artList.length > position )
			{
				musicData.setArtist( artList[position] );
				//Log.i( TAG , "onLineMusicInfo artList = " + artList[position] );
			}
			long[] songIDArray = bundle.getLongArray( "PLAYLIST" );
			if( songIDArray != null && songIDArray.length > position )
			{
				musicData.setId( songIDArray[position] );
				//Log.i( TAG , "onLineMusicInfo songID = " + songIDArray[position] );
			}
			long[] alumbIdArray = bundle.getLongArray( "ALBUMLIST" );
			if( alumbIdArray != null && alumbIdArray.length > position )
			{
				musicData.setAlbum_id( alumbIdArray[position] );
				//Log.i( TAG , "onLineMusicInfo setAlbum_id = " + alumbIdArray[position] );
			}
			musicData.setPosition( bundle.getLong( "CURRENT_POS" , 0 ) );
			musicData.setDuration( bundle.getLong( "DURATION" ) );
			delaySecend = 600l;
			updateMusicProgress();
			//Log.i( TAG , "onLineMusicInfo setDuration = " + bundle.getLong( "DURATION" ) );
			//Log.i( TAG , "onLineMusicInfo musicData = " + musicData );
			callBack.onMusicInfoChange( activity , musicData );
			// gaominghui@2016/11/28 ADD START
			onLineMusicData = musicData;
			// gaominghui@2016/11/28 ADD END
			mIsPlaying = isPlaying;
			if( mIsPlaying )
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "onLineMusicInfo callBack.onMusicPlay()" );
				callBack.onMusicPlay();
			}
			else
			{
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "onLineMusicInfo callBack.onMusicPause()" );
				callBack.onMusicPause();
			}
		}
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			String action = intent.getAction();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "onReceive action:" , action , "-intent.getExtras():" , intent.getExtras().toString() ) );
			if( META_CHANGED.equals( action ) )
			{
				onMetaChange( activity , intent );
			}
			else if( ACTION_QUEUE_CHANGED.equals( action ) )
			{
			}
			else if( PLAY_STATE_CHANGED.equals( action ) )
			{
				onPlayStateChanged( intent );
			}
			else if( ACTION_ONLING_SEND_MUSIC_INFO.equals( action ) )
			{
				onLineMusicInfo( intent );
			}
			else if( ACTION_ONLING_SEND_MUSIC_ALBUM_URL.equals( action ) )
			{
				getOnlineAlbumUrl( intent );
			}
			else if( ACTION_SEND_MUSIC_POSITION.equals( action ) )
			{
				Bundle extras = intent.getExtras();
				if( extras != null )
				{
					if( musicData != null )
					{
						if( onLineMusicData != null )
						{
							musicData.setDuration( intent.getLongExtra( "DURATION" , 0 ) );
							musicData.setPosition( intent.getLongExtra( "CURRENT_POS" , 0 ) );
							delaySecend = 600l;
							setOnlineMusic( true );
							mIsPlaying = intent.getBooleanExtra( "ISPLAYING" , false );
						}
						else
						{
							musicData.setDuration( intent.getLongExtra( "DURATION" , musicData.getDuration() ) );
							musicData.setPosition( intent.getLongExtra( "CURRENT_POS" , musicData.getPosition() ) );
							delaySecend = 800l;
							setOnlineMusic( false );
						}
						updateMusicProgress();
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( TAG , StringUtils.concat( "SEND_MUSIC_POSITION mIsPlaying:" , mIsPlaying ) );
						if( mIsPlaying )
						{
							callBack.onMusicPlay();
						}
						else
						{
							callBack.onMusicPause();
						}
						callBack.onMusicPositionChange( musicData.getPosition() , musicData.getDuration() );
					}
				}
			}
		}
	};
	
	/**
	 * 获取在线音乐封面
	 *
	 * @param intent
	 * @author gaominghui 2016年11月1日
	 */
	private void getOnlineAlbumUrl(
			Intent intent )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , "getOnlineAlbumUrl!!!" );
		byte[] arrayOfByte = intent.getByteArrayExtra( "SMALLALBUM" );
		String url = intent.getStringExtra( "ALBUM_URL" );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "getOnlineAlbumUrl ALBUM_URL url:" , url , "-arrayOfByte:" , arrayOfByte.toString() ) );
		if( arrayOfByte != null )
			Bytes2Bimap( arrayOfByte );
	}
	
	private void Bytes2Bimap(
			byte[] paramArrayOfByte )
	{
		Bitmap localBitmap = null;
		if( paramArrayOfByte.length != 0 )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "Bytes2Bimap paramArrayOfByte.length:" , paramArrayOfByte.length ) );
			localBitmap = BitmapFactory.decodeByteArray( paramArrayOfByte , 0 , paramArrayOfByte.length );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "Bytes2Bimap localBitmap = " + localBitmap );
		}
		GetVIVOonLineAlbumRegionThredManager.start( activity , musicData , getVivoOnlineAlbumRegionThredCallBack , localBitmap );
	}
	
	private GetVIVOonLineAlbumRegionThredManager.CallBack getVivoOnlineAlbumRegionThredCallBack = new CallBack() {
		
		@Override
		public void loadVivoOnlineAlbumRegionCompleted(
				MusicData musicData ,
				Bitmap[] topBitmaps )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , "loadVivoOnlineAlbumRegionCompleted musicData = " + musicData );
			if( musicData != null )
			{
				BitmapDrawable[] drawables = null;
				if( topBitmaps != null )
				{
					drawables = new BitmapDrawable[2];
					drawables[0] = new BitmapDrawable( topBitmaps[0] );
					drawables[1] = new BitmapDrawable( topBitmaps[1] );
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( TAG , "loadVivoOnlineAlbumRegionCompleted  topBitmaps[0] = " + topBitmaps[0] + "; topBitmaps[1] = " + topBitmaps[1] );
				}
				callBack.onMusicAlbumRegionChange( drawables );
			}
		}
	};
}
