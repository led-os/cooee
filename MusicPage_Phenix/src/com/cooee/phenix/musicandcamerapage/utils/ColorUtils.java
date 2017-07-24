package com.cooee.phenix.musicandcamerapage.utils;


// MusicPage CameraPage
public class ColorUtils
{
	
	private static final int lyrics_scroll_alpha = 0xff000000;
	
	public static int getLyricsColor(
			String lyricsColor ,
			int defaultColor )
	{
		String lyricsColorTmp = lyricsColor.replaceAll( "^0[x|X]" , "" );
		if( null == lyricsColorTmp )
			return defaultColor + lyrics_scroll_alpha;
		else
			return Integer.parseInt( lyricsColorTmp , 16 ) + lyrics_scroll_alpha;
	}
}
