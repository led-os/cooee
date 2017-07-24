package com.cooee.phenix.musicpage.control;


// MusicPage
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;

import com.cooee.phenix.musicpage.entity.LyricSentence;
import com.cooee.phenix.musicpage.entity.MusicData;


public interface MusicControlCallBack
{
	
	public void onMusicPlay();
	
	public void onMusicPause();
	
	public void onMusicInfoChange(
			Activity activity ,
			MusicData musicData );
	
	public void onMusicAlbumRegionChange(
			BitmapDrawable[] topDrawables );
	
	public void onMusicPositionChange(
			long position ,
			long duration );
	
	public void onMusicLyricChange(
			List<LyricSentence> list ,
			boolean isLoadComplete );
}
