/***/
package com.cooee.phenix.musicpage.vivo;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicandcamerapage.utils.BitmapUtils;
import com.cooee.phenix.musicpage.R;
import com.cooee.phenix.musicpage.entity.MusicData;
import com.cooee.phenix.musicpage.vivo.GetVIVOonLineAlbumRegionThredManager.CallBack;


/**
 * @author gaominghui 2016年11月10日
 */
public class GetVIVOOnLineAlbumRegionThred extends Thread
{
	
	private boolean exit = false;
	//
	private MusicData musicData = null;
	private CallBack callBack = null;
	private Activity activity = null;
	private Bitmap alumbBitmap = null;
	
	protected GetVIVOOnLineAlbumRegionThred(
			Activity activity ,
			MusicData musicData ,
			CallBack callBack ,
			Bitmap alumbBitmap )
	{
		this.musicData = musicData;
		this.activity = activity;
		this.callBack = callBack;
		this.alumbBitmap = alumbBitmap;
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
	}
	
	@Override
	public void run()
	{
		//MusicView.logI( "GetAlbumRegionAndLyricsThred , start , musicData : " + musicData.getTitle() );
		try
		{
			super.run();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "Vivo" , StringUtils.concat( "GetVIVOOnLineAlbumRegionThred - run - exit:" , exit ) );
			if( !exit )
				bitmap();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	private void bitmap()
	{
		//cur album
		if( exit )
		{
			if( this.alumbBitmap != null && !this.alumbBitmap.isRecycled() )
				this.alumbBitmap.recycle();
			return;
		}
		this.alumbBitmap = BitmapUtils.mask( activity , this.alumbBitmap , true );
		if( exit )
		{
			if( this.alumbBitmap != null && !this.alumbBitmap.isRecycled() )
				this.alumbBitmap.recycle();
			return;
		}
		//combine
		Bitmap turntables = BitmapFactory.decodeResource( activity.getResources() , R.drawable.music_page_turntable_bg_anim );
		if( exit )
		{
			if( this.alumbBitmap != null && !this.alumbBitmap.isRecycled() )
				this.alumbBitmap.recycle();
			if( turntables != null && !turntables.isRecycled() )
				turntables.recycle();
			return;
		}
		Bitmap[] topBitmaps = BitmapUtils.combineBitmap( activity , this.alumbBitmap , turntables , 0.5F , true );
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
		loadAlbumRegionCompleted( musicData , topBitmaps );
	}
	
	private void loadAlbumRegionCompleted(
			MusicData musicData ,
			Bitmap[] topBitmaps )
	{
		if( callBack != null )
			callBack.loadVivoOnlineAlbumRegionCompleted( musicData , topBitmaps );
	}
}
