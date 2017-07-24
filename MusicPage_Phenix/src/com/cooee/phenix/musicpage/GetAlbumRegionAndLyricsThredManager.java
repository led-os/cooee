package com.cooee.phenix.musicpage;


// MusicPage
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;

import com.cooee.phenix.musicpage.control.Xiami;
import com.cooee.phenix.musicpage.entity.LyricSentence;
import com.cooee.phenix.musicpage.entity.MusicData;


public class GetAlbumRegionAndLyricsThredManager
{
	
	private static GetAlbumRegionAndLyricsThred thred = null;
	
	public synchronized static void start(
			Activity activity ,
			Xiami xiami ,
			MusicData musicData ,
			CallBack callBack ,
			boolean getLyrics )
	{
		if( thred == null || !thred.getMusicData().equal( musicData ) )
		{
			stop();
			thred = new GetAlbumRegionAndLyricsThred( activity , xiami , musicData , callBack , getLyrics );
			thred.start();
		}
	}
	
	public synchronized static void stop()
	{
		if( thred != null )
		{
			thred.release();
			thred = null;
		}
	}
	
	//
	public interface CallBack
	{
		
		public void loadAlbumRegionCompleted(
				MusicData musicData ,
				Bitmap[] topBitmaps );
		
		public void loadLyricsCompleted(
				MusicData musicData ,
				List<LyricSentence> list );
	}
}
