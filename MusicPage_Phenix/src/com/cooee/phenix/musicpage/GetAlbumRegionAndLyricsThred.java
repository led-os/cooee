package com.cooee.phenix.musicpage;


// MusicPage
import java.io.File;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cooee.dynamicload.utils.LOG;
import com.cooee.phenix.musicandcamerapage.utils.BitmapUtils;
import com.cooee.phenix.musicandcamerapage.utils.LyricsUtils;
import com.cooee.phenix.musicandcamerapage.utils.NetWorkUtils;
import com.cooee.phenix.musicpage.GetAlbumRegionAndLyricsThredManager.CallBack;
import com.cooee.phenix.musicpage.control.Xiami;
import com.cooee.phenix.musicpage.control.Xiami.FindLyricCallBack;
import com.cooee.phenix.musicpage.entity.LyricSentence;
import com.cooee.phenix.musicpage.entity.MusicData;


public class GetAlbumRegionAndLyricsThred extends Thread
{
	
	private boolean exit = false;
	//
	private MusicData musicData = null;
	private CallBack callBack = null;
	private Activity activity = null;
	private Xiami xiami = null;
	private boolean getLyrics = false;
	
	protected GetAlbumRegionAndLyricsThred(
			Activity activity ,
			Xiami xiami ,
			MusicData musicData ,
			CallBack callBack ,
			boolean getLyrics )
	{
		this.musicData = musicData;
		this.activity = activity;
		this.callBack = callBack;
		this.xiami = xiami;
		this.getLyrics = getLyrics;
	}
	
	protected MusicData getMusicData()
	{
		return musicData;
	}
	
	protected void release()
	{
		exit = true;
		//
		activity = null;
		callBack = null;
		musicData = null;
		xiami = null;
	}
	
	@Override
	public void run()
	{
		//MusicView.logI( "GetAlbumRegionAndLyricsThred , start , musicData : " + musicData.getTitle() );
		try
		{
			super.run();
			//			Thread.sleep( 500 );
			//
			if( !exit && getLyrics )
				lyrics();
			//
			if( !exit )
				bitmap();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	private void lyrics()
	{
		
		if( MusicView.configUtils.getBoolean( "music_page_show_lyrics" , false ) )
		{
			File Lyrics = LyricsUtils.getLyricsFile( musicData );
			if( exit )
			{
				return;
			}
			if( Lyrics == null )
			{
				if( xiami != null )
					if( NetWorkUtils.isNetworkAvailable( activity ) )
					{
						//
						if( exit )
						{
							return;
						}
						xiami.findLyric( musicData , MusicView.configUtils.getStringArray( "music_page_lyrics_directory" , null ).get( 0 ) , new FindLyricCallBack() {
							
							@Override
							public void finish(
									File lyricFile )
							{
								if( lyricFile != null )
								{
									if( exit )
									{
										return;
									}
									List<LyricSentence> list = LyricsUtils.getLyricSentencesByFile( lyricFile );
									if( exit )
									{
										return;
									}
									MusicView.logI( "found_the_lyrics" );
									loadLyricsCompleted( musicData , list );
								}
								else
								{
									if( exit )
									{
										return;
									}
									loadLyricsCompleted( musicData , null );
								}
							}
						} );
					}
					else
					{
						if( exit )
						{
							return;
						}
						loadLyricsCompleted( musicData , null );
						MusicView.logI( "net_work_error" );
					}
			}
			else
			{
				if( exit )
				{
					return;
				}
				List<LyricSentence> list = LyricsUtils.getLyricSentencesByFile( Lyrics );
				if( exit )
				{
					return;
				}
				loadLyricsCompleted( musicData , list );
			}
		}
	}
	
	private void loadLyricsCompleted(
			MusicData musicData ,
			List<LyricSentence> list )
	{
		if( callBack != null )
			callBack.loadLyricsCompleted( musicData , list );
	}
	
	private void bitmap()
	{
		//cur album
		Bitmap albumBitmap = BitmapUtils.getArtwork( activity , musicData.getId() , musicData.getAlbum_id() );
		if( exit )
		{
			if( albumBitmap != null && !albumBitmap.isRecycled() )
				albumBitmap.recycle();
			return;
		}
		albumBitmap = BitmapUtils.mask( activity , albumBitmap , true );
		if( exit )
		{
			if( albumBitmap != null && !albumBitmap.isRecycled() )
				albumBitmap.recycle();
			return;
		}
		//combine
		Bitmap turntables = BitmapFactory.decodeResource( activity.getResources() , R.drawable.music_page_turntable_bg_anim );
		if( exit )
		{
			if( albumBitmap != null && !albumBitmap.isRecycled() )
				albumBitmap.recycle();
			if( turntables != null && !turntables.isRecycled() )
				turntables.recycle();
			return;
		}
		Bitmap[] topBitmaps = BitmapUtils.combineBitmap( activity , albumBitmap , turntables , 0.5F , true );
		if( exit )
		{
			if( topBitmaps != null )
				for( Bitmap bitmap : topBitmaps )
				{
					if( bitmap != null && !bitmap.isRecycled() )
						bitmap.recycle();
				}
			return;
		}
		//
		loadAlbumRegionCompleted( musicData , topBitmaps );
	}
	
	private void loadAlbumRegionCompleted(
			MusicData musicData ,
			Bitmap[] topBitmaps )
	{
		if( callBack != null )
			callBack.loadAlbumRegionCompleted( musicData , topBitmaps );
	}
}
