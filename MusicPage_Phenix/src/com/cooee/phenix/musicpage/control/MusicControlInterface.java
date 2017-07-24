package com.cooee.phenix.musicpage.control;


// MusicPage
import android.app.Activity;

import com.cooee.phenix.musicpage.entity.MusicData;


public interface MusicControlInterface
{
	
	public void init(
			Activity activity ,
			MusicControlCallBack callBack );
	
	public void play();
	
	public void pause();
	
	public void next();
	
	public void previous();
	
	public void seek(
			long position ,
			long duration );
	
	public long getPosition();
	
	public long getDuration();
	
	public void enterClient(
			MusicData curMusicData ,
			boolean isPlaying );
	
	public void finish();
}
