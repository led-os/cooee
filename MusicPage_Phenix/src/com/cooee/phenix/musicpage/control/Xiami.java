package com.cooee.phenix.musicpage.control;


// MusicPage
import java.io.File;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicandcamerapage.utils.LyricsUtils;
import com.cooee.phenix.musicpage.entity.MusicData;
import com.xiami.sdk.OnlineSong;
import com.xiami.sdk.XiamiSDK;
import com.xiami.sdk.callback.OnlineSongCallback;
import com.xiami.sdk.callback.OnlineSongsCallback;
import com.xiami.sdk.callback.StringCallback;


public class Xiami
{
	
	private final String KEY = "a09aaa6cc23c0b6efa718624a06c3881";
	private final String SECRET = "45489a9c8894ea2cfa167b8ec1066f90";
	private XiamiSDK mXiamiSDK;
	
	public Xiami(
			Context context )
	{
		mXiamiSDK = new XiamiSDK( context , KEY , SECRET );
	}
	
	public interface FindLyricCallBack
	{
		
		public void finish(
				File lyricFile );
	}
	
	public void findLyric(
			final MusicData music ,
			final String lyricsSavePath ,
			final FindLyricCallBack callBack )
	{
		//		mXiamiSDK.getLrcByName( music.getTitle() , music.getArtist() , new StringCallback() {
		//			
		//			@Override
		//			public void onResponse(
		//					int failedCode ,
		//					final String results )
		//			{
		//				if( TextUtils.isEmpty( results ) )
		//				{
		//					if( fuzzySearch )
		//					{
		//						searchSong( music , lyricsSavePath , callBack );
		//					}
		//					else
		//					{
		//						callBack.finish( null );
		//					}
		//				}
		//				else
		//				{
		//					File lyricFile = LyricsUtils.saveLyrics( music , results , lyricsSavePath );
		//					callBack.finish( lyricFile );
		//				}
		//			}
		//		} );
		findSongByName( music , lyricsSavePath , callBack );
	}
	
	private void findSongByName(
			final MusicData music ,
			final String lyricsSavePath ,
			final FindLyricCallBack callBack )
	{
		if( mXiamiSDK != null )
		{
			mXiamiSDK.findSongByName( music.getTitle() , music.getArtist() , new OnlineSongCallback() {
				
				@Override
				public void onResponse(
						int failedCode ,
						OnlineSong results )
				{
					if( failedCode == 0 && results != null )
					{
						long musicId = results.getSongId();
						getLrcBySongId( music , lyricsSavePath , callBack , musicId , true );
					}
					else
					{
						searchSong( music , lyricsSavePath , callBack );
					}
				}
			} );
		}
	}
	
	private void getLrcBySongId(
			final MusicData music ,
			final String lyricsSavePath ,
			final FindLyricCallBack callBack ,
			final long musicId ,
			final boolean fuzzySearch )
	{
		if( mXiamiSDK != null )
		{
			mXiamiSDK.getLrcBySongId( musicId , new StringCallback() {
				
				@Override
				public void onResponse(
						int failedCode ,
						final String results )
				{
					if( TextUtils.isEmpty( results ) )
					{
						if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
							Log.i( "MusicView" , StringUtils.concat( "Xiami getLrcBySongId fuzzySearch:" , fuzzySearch ) );
						if( fuzzySearch )
							searchSong( music , lyricsSavePath , callBack );
						else
							callBack.finish( null );
					}
					else
					{
						File lyricFile = LyricsUtils.saveLyrics( music , results , lyricsSavePath );
						callBack.finish( lyricFile );
					}
				}
			} );
		}
	}
	
	private void searchSong(
			final MusicData music ,
			final String lyricsSavePath ,
			final FindLyricCallBack callBack )
	{
		if( mXiamiSDK != null )
		{
			mXiamiSDK.searchSong( music.getTitle() , 10 , 1 , new OnlineSongsCallback() {
				
				@Override
				public void onResponse(
						final int failedCode ,
						final List<OnlineSong> results )
				{
					if( failedCode == 0 && results != null && results.size() == 1 )
					{
						//					music.setTitle( results.get( 0 ).getSongName() );
						//					music.setArtist( results.get( 0 ).getArtistName() );
						//					findLyric( music , false , lyricsSavePath , callBack );
						getLrcBySongId( music , lyricsSavePath , callBack , results.get( 0 ).getSongId() , false );
					}
					else
					{
						callBack.finish( null );
					}
				}
			} );
		}
	}
}
