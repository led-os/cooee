package com.cooee.phenix.musicpage.control;


// MusicPage
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.cooee.dynamicload.utils.LOG;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicpage.GetAlbumRegionAndLyricsThredManager;
import com.cooee.phenix.musicpage.GetAlbumRegionAndLyricsThredManager.CallBack;
import com.cooee.phenix.musicpage.MusicView;
import com.cooee.phenix.musicpage.entity.LyricSentence;
import com.cooee.phenix.musicpage.entity.MusicData;


public class MusicControl
{
	
	private final static String TAG = "MusicControl";
	private final int SYSTEM_MUSIC_TYPE = 0;
	private final int YIYUN_MUSIC_TYPE = 1;
	private final int DUOMI_MUSIC_TYPE = 2;
	public static final int VIVO_MUSIC_TYPE = 3;
	private MusicControlInterface controlInterface = null;
	private MusicControlCallBack viewCallBack = null;
	//
	private boolean isPlaying = false;
	private boolean autoCutSong = true;
	private MusicData curMusicData = null;
	private long curPosition = -1;
	private long curDuration = -1;
	private long lyricMoveStartPosition = -1;
	//
	private Xiami xiami = null;
	private boolean getLyrics = false;
	
	public MusicControl(
			Activity activity ,
			MusicControlCallBack viewCallBack ,
			boolean getLyrics )
	{
		this.viewCallBack = viewCallBack;
		this.getLyrics = getLyrics;
		switch( MusicView.configUtils.getInteger( "music_page_switch_music" ) )
		{
			case SYSTEM_MUSIC_TYPE:
				controlInterface = new SystemMusic();
				break;
			case YIYUN_MUSIC_TYPE:
				break;
			case DUOMI_MUSIC_TYPE:
				break;
			case VIVO_MUSIC_TYPE:
				controlInterface = new Vivo();
				break;
			default:
				controlInterface = new SystemMusic();
				break;
		}
		if( MusicView.configUtils.getBoolean( "music_page_enable_xiami_find_lyrics" , false ) )
		{
			xiami = new Xiami( activity );
		}
		controlInterface.init( activity , controlCallBack );
	}
	
	public void finish()
	{
		controlInterface.finish();
	}
	
	public void play()
	{
		if( !isPlaying )
			controlInterface.play();
	}
	
	public void pause()
	{
		if( isPlaying )
			controlInterface.pause();
	}
	
	public void togglePause()
	{
		if( isPlaying )
			controlInterface.pause();
		else
			controlInterface.play();
	}
	
	public void next()
	{
		autoCutSong = false;
		controlInterface.next();
	}
	
	public void previous()
	{
		autoCutSong = false;
		controlInterface.previous();
	}
	
	public void seek(
			long position ,
			long duration )
	{
		this.curPosition = position;
		this.curDuration = duration;
		controlInterface.seek( position , duration );
	}
	
	public long getCurPosition()
	{
		return curPosition;
	}
	
	public long getCurDuration()
	{
		return curDuration;
	}
	
	public void saveLyricMoveStartPosition()
	{
		this.lyricMoveStartPosition = this.curPosition;
	}
	
	public long getLyricMoveStartPosition()
	{
		return lyricMoveStartPosition;
	}
	
	public void enterClient()
	{
		controlInterface.enterClient( curMusicData , isPlaying );
	}
	
	public boolean getPlayStatus()
	{
		return isPlaying;
	}
	
	private MusicControlCallBack controlCallBack = new MusicControlCallBack() {
		
		@Override
		public void onMusicPositionChange(
				long position ,
				long duration )
		{
			if( isPlaying )
				if( curPosition != position || curDuration != duration )
				{
					curPosition = position;
					curDuration = duration;
					viewCallBack.onMusicPositionChange( position , duration );
				}
		}
		
		@Override
		public void onMusicPlay()
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "onMusicPlay - isPlaying:" , isPlaying ) );
			if( MusicView.configUtils.getInteger( "music_page_switch_music" ) != VIVO_MUSIC_TYPE )
			{
				if( !isPlaying )
				{
					MusicView.logI( "onMusicPlay()" );
					isPlaying = true;
					viewCallBack.onMusicPlay();
					// gaominghui@2016/10/28 ADD START 当歌曲开始播放再去启动获取歌词状态的线程
					startUpdatePositionThread();
					// gaominghui@2016/10/28 ADD END 当歌曲开始播放再去启动获取歌词状态的线程
				}
			}
			else
			{
				isPlaying = true;
				viewCallBack.onMusicPlay();
				// gaominghui@2016/10/28 ADD START 当歌曲开始播放再去启动获取歌词状态的线程
				startUpdatePositionThread();
				// gaominghui@2016/10/28 ADD END 当歌曲开始播放再去启动获取歌词状态的线程
			}
		}
		
		@Override
		public void onMusicPause()
		{
			MusicView.logI( "onMusicPause()" );
			isPlaying = false;
			viewCallBack.onMusicPause();
			// gaominghui@2016/10/28 ADD START 暂停播放状态，要停掉获取歌词的线程
			stopUpdatePositionThread();
			// gaominghui@2016/10/28 ADD END 暂停播放状态，要停掉获取歌词的线程
		}
		
		@Override
		public void onMusicInfoChange(
				Activity activity ,
				MusicData musicData )
		{
			if( musicData == null )
			{
				viewCallBack.onMusicInfoChange( activity , musicData );
			}
			else
			{
				if( controlInterface instanceof SystemMusic && isPlaying && curMusicData != null && !curMusicData.equal( musicData ) )
				{// 系统播放器，播放的时候切歌，不会发送播放状态改变的广播
					LOG.i( "MusicControl" , "onMusicInfoChange viewCallBack.onMusicPlay() !!! " );
					viewCallBack.onMusicPlay();
				}
				if( curMusicData == null || !curMusicData.equal( musicData ) )
				{
					curMusicData = musicData;
					if( autoCutSong )
						viewCallBack.onMusicAlbumRegionChange( null );
					else
						autoCutSong = true;
					viewCallBack.onMusicInfoChange( activity , musicData );
					if( MusicView.configUtils.getBoolean( "music_page_show_lyrics" , false ) )
						viewCallBack.onMusicLyricChange( null , false );
					if( musicData != null )
					{
						GetAlbumRegionAndLyricsThredManager.start( activity , xiami , musicData , getAlbumRegionThredCallBack , getLyrics );
					}
				}
			}
		}
		
		// 以下两个接口忽略 , 由onMusicInfoChange来分发
		@Override
		public void onMusicLyricChange(
				List<LyricSentence> list ,
				boolean isLoadComplete )
		{
		}
		
		@Override
		public void onMusicAlbumRegionChange(
				BitmapDrawable[] topDrawables )
		{
			if( MusicView.configUtils.getInteger( "music_page_switch_music" ) == VIVO_MUSIC_TYPE )
			{
				if( topDrawables != null )
				{
					viewCallBack.onMusicAlbumRegionChange( topDrawables );
				}
			}
		}
		// 以上两个接口忽略 , 由onMusicInfoChange来分发
	};
	private GetAlbumRegionAndLyricsThredManager.CallBack getAlbumRegionThredCallBack = new CallBack() {
		
		@SuppressWarnings( "deprecation" )
		@Override
		public void loadAlbumRegionCompleted(
				MusicData musicData ,
				Bitmap[] topBitmaps )
		{
			if( curMusicData != null && curMusicData.equal( musicData ) )
			{
				BitmapDrawable[] drawables = null;
				if( topBitmaps != null )
				{
					drawables = new BitmapDrawable[2];
					drawables[0] = new BitmapDrawable( topBitmaps[0] );
					drawables[1] = new BitmapDrawable( topBitmaps[1] );
				}
				viewCallBack.onMusicAlbumRegionChange( drawables );
			}
		}
		
		@Override
		public void loadLyricsCompleted(
				MusicData musicData ,
				List<LyricSentence> list )
		{
			if( curMusicData != null && curMusicData.equal( musicData ) )
				viewCallBack.onMusicLyricChange( list , true );
		}
	};
	// gaominghui@2016/10/27 ADD START 获取歌词进度的线程
	private static long sleepMilliseconds = 1000;
	
	/**
	 * 此线程通过音乐服务不断去获取当前歌曲播放的duration和position
	 * 音乐播放，线程开始；音乐暂停，线程结束
	 */
	private class UpdateThread extends Thread
	{
		
		private volatile boolean exitUpdateThread = false;
		
		public void close()
		{
			this.exitUpdateThread = true;
		}
		
		@Override
		public void run()
		{
			super.run();
			while( true )
			{
				if( exitUpdateThread )
				{
					exitUpdateThread = false;
					return;
				}
				if( controlInterface != null )
				{
					curDuration = controlInterface.getDuration();
					curPosition = controlInterface.getPosition();
					if( viewCallBack != null )
					{
						viewCallBack.onMusicPositionChange( curPosition , curDuration );
					}
				}
				try
				{
					Thread.sleep( sleepMilliseconds );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
				}
				if( exitUpdateThread )
				{
					exitUpdateThread = false;
					return;
				}
			}
		}
	}
	
	private UpdateThread updateThread = null;
	
	/**
	 * 启动获取当前歌曲播放的duration和position的线程
	 */
	public synchronized void startUpdatePositionThread()
	{
		if( MusicView.configUtils.getBoolean( "music_page_user_aidl_get_position" , false ) && updateThread == null )
		{
			updateThread = new UpdateThread();
			updateThread.start();
		}
	}
	
	/**
	 * 结束获取当前歌曲播放的duration和position的线程
	 */
	public synchronized void stopUpdatePositionThread()
	{
		if( MusicView.configUtils.getBoolean( "music_page_user_aidl_get_position" , false ) && updateThread != null )
		{
			updateThread.close();
			updateThread.interrupt();
		}
		updateThread = null;
	}
	// gaominghui@2016/10/27 ADD END   获取歌词进度的线程
}
