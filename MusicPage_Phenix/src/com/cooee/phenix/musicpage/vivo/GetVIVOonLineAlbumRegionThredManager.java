/***/
package com.cooee.phenix.musicpage.vivo;


import android.app.Activity;
import android.graphics.Bitmap;

import com.cooee.phenix.musicpage.entity.MusicData;


/**
 * @author gaominghui 2016年11月10日
 */
public class GetVIVOonLineAlbumRegionThredManager
{
	
	private static GetVIVOOnLineAlbumRegionThred thread = null;
	
	public synchronized static void start(
			Activity activity ,
			MusicData musicData ,
			CallBack callBack ,
			Bitmap bitmap )
	{
		if( thread == null || !thread.getMusicData().equal( musicData ) )
		{
			stop();
			thread = new GetVIVOOnLineAlbumRegionThred( activity , musicData , callBack , bitmap );
			thread.start();
		}
	}
	
	public synchronized static void stop()
	{
		if( thread != null )
		{
			thread.release();
			thread = null;
		}
	}
	
	//
	public interface CallBack
	{
		
		public void loadVivoOnlineAlbumRegionCompleted(
				MusicData musicData ,
				Bitmap[] topBitmaps );
	}
}
