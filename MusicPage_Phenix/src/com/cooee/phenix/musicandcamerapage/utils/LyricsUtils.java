package com.cooee.phenix.musicandcamerapage.utils;


// MusicPage CameraPage
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicpage.MusicView;
import com.cooee.phenix.musicpage.entity.LyricSentence;
import com.cooee.phenix.musicpage.entity.MusicData;


public class LyricsUtils
{
	
	private static final Pattern pattern = Pattern.compile( "(?<=\\[).*?(?=\\])" );
	
	public static File getLyricsFile(
			MusicData musicData )
	{
		File Lyrics = getLyricsFileByMusicDirectory( musicData.getData() );
		if( Lyrics == null )
			Lyrics = getLyricsFileByConfigDirectory( musicData );
		return Lyrics;
	}
	
	public static File getLyricsFileByMusicDirectory(
			String path )
	{
		File Lyrics = null;
		if( path != null )
		{
			if( path.contains( ".mp3" ) || path.contains( ".flac" ) || path.contains( ".FLAC" ) || path.contains( ".MP3" ) || path.contains( ".aac" ) || path.contains( ".AAC" ) || path
					.contains( ".AMR" ) || path.contains( ".amr" ) || path.contains( ".OGG" ) || path.contains( ".ogg" ) || path.contains( ".PCM" ) || path.contains( ".pcm" ) || path
					.contains( ".M4A" ) || path.contains( ".m4a" ) || path.contains( ".WAV" ) || path.contains( ".wav" ) || path.contains( ".AWB" ) || path.contains( ".awb" ) || path
					.contains( ".wma" ) || path.contains( ".WMA" ) || path.contains( ".MID" ) || path.contains( ".mid" ) || path.contains( ".SMF" ) || path.contains( ".smf" ) || path
					.contains( ".imy" ) || path.contains( ".IMY" ) )
			{
				path = path.replace( ".mp3" , ".lrc" ).replace( ".flac" , ".lrc" ).replace( ".FLAC" , ".lrc" ).replace( ".MP3" , ".lrc" ).replace( ".aac" , ".lrc" ).replace( ".AAC" , ".lrc" )
						.replace( ".amr" , ".lrc" ).replace( ".AMR" , ".lrc" ).replace( ".AMR" , ".lrc" ).replace( ".OGG" , ".lrc" ).replace( ".ogg" , ".lrc" ).replace( ".pcm" , ".lrc" )
						.replace( ".PCM" , ".lrc" ).replace( ".M4A" , ".lrc" ).replace( ".m4a" , ".lrc" ).replace( ".WAV" , ".lrc" ).replace( ".wav" , ".lrc" ).replace( ".AWB" , ".lrc" )
						.replace( ".awb" , ".lrc" ).replace( ".WMA" , ".lrc" ).replace( ".wma" , ".lrc" ).replace( ".SMF" , ".lrc" ).replace( ".smf" , ".lrc" ).replace( ".IMY" , ".lrc" )
						.replace( ".imy" , ".lrc" );
				File file = new File( path );
				if( file != null && file.exists() )
				{
					Lyrics = file;
				}
			}
		}
		return Lyrics;
	}
	
	public static File getLyricsFileByConfigDirectory(
			MusicData musicData )
	{
		File Lyrics = null;
		if( musicData != null )
		{
			List<String> configLyricsDirectory = MusicView.configUtils.getStringArray( "music_page_lyrics_directory" , null );
			if( configLyricsDirectory != null )
			{
				List<String> LyricsNameList = getLyricsNameList( musicData );
				for( String directoryPath : configLyricsDirectory )
				{
					for( String lyricsName : LyricsNameList )
					{
						String path = manyStringCombine( directoryPath , lyricsName , ".lrc" );
						File file = new File( path );
						if( file != null && file.exists() )
						{
							Lyrics = file;
							break;
						}
					}
					if( Lyrics != null )
						break;
				}
			}
		}
		return Lyrics;
	}
	
	public static List<String> getLyricsNameList(
			MusicData musicData )
	{
		List<String> LyricsNameList = new ArrayList<String>();
		LyricsNameList.add( manyStringCombine( musicData.getArtist() , " — " , musicData.getTitle() ) );
		LyricsNameList.add( manyStringCombine( musicData.getArtist() , "—" , musicData.getTitle() ) );
		LyricsNameList.add( manyStringCombine( musicData.getTitle() , " — " , musicData.getArtist() ) );
		LyricsNameList.add( manyStringCombine( musicData.getTitle() , "—" , musicData.getArtist() ) );
		LyricsNameList.add( manyStringCombine( musicData.getArtist() , " - " , musicData.getTitle() ) );
		LyricsNameList.add( manyStringCombine( musicData.getArtist() , "-" , musicData.getTitle() ) );
		LyricsNameList.add( manyStringCombine( musicData.getTitle() , " - " , musicData.getArtist() ) );
		LyricsNameList.add( manyStringCombine( musicData.getTitle() , "-" , musicData.getArtist() ) );
		LyricsNameList.add( musicData.getTitle() );
		return LyricsNameList;
	}
	
	public static String manyStringCombine(
			String ... pStrings )
	{
		StringBuilder builder = new StringBuilder();
		for( String string : pStrings )
		{
			builder.append( string );
		}
		return builder.toString();
	}
	
	public static File saveLyrics(
			MusicData musicData ,
			String Lyrics ,
			String lyricsSavePath )
	{
		File lyricFile = null;
		try
		{
			File destDir = new File( lyricsSavePath );
			if( !destDir.exists() )
			{
				destDir.mkdirs();
			}
			String path = manyStringCombine( lyricsSavePath , musicData.getTitle() , "-" + musicData.getArtist() , ".lrc" );
			lyricFile = new File( path );
			if( lyricFile.createNewFile() )
			{
				FileOutputStream fileOutputStream = new FileOutputStream( lyricFile );
				fileOutputStream.write( Lyrics.getBytes() );
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			lyricFile = null;
		}
		return lyricFile;
	}
	
	/**
	 * @param file
	 *            需要判断编码格式的文件
	 * @return 编码格式
	 * @throws Exception 
	 */
	public static String getEncoding(
			File file ) throws Exception
	{
		FileInputStream lrcFis = new FileInputStream( file );
		BufferedInputStream lrcBuffIn = new BufferedInputStream( lrcFis );
		lrcBuffIn.mark( 3 );
		byte[] first3Bytes = new byte[4];
		lrcBuffIn.read( first3Bytes );
		lrcBuffIn.reset();
		lrcFis.close();
		lrcBuffIn.close();
		if( first3Bytes[0] == (byte)0xEF && first3Bytes[1] == (byte)0xBB && first3Bytes[2] == (byte)0xBF )
		{// utf-8
			return "utf-8";
		}
		else if( first3Bytes[0] == (byte)0xFF && first3Bytes[1] == (byte)0xFE )
		{
			return "unicode";
		}
		else if( first3Bytes[0] == (byte)0xFE && first3Bytes[1] == (byte)0xFF )
		{
			return "utf-16be";
		}
		else if( first3Bytes[0] == (byte)0xFF && first3Bytes[1] == (byte)0xFF )
		{
			return "utf-16le";
		}
		else if( isUtf8Bytes( file ) )
		{
			return "utf-8";
		}
		else
		{
			return "GBK";
		}
	}
	
	/**
	 * 判断无BOM的UTF-8
	 * @param file 歌词文件
	 * @return 文件为UTF-8格式则返回true 否则返回false
	 * @throws Exception
	 */
	public static boolean isUtf8Bytes(
			File file ) throws Exception
	{
		byte[] fisBuf = null;
		byte[] bufReadBuf = null;
		FileInputStream lrcFis = new FileInputStream( file );
		BufferedReader lrcBuffReader = new BufferedReader( new InputStreamReader( new FileInputStream( file ) , "UTF-8" ) );
		String lrcTmp = null;
		while( ( lrcTmp = lrcBuffReader.readLine() ) != null )
		{
			bufReadBuf = lrcTmp.getBytes( "UTF-8" );
			fisBuf = new byte[bufReadBuf.length];
			lrcFis.read( fisBuf );
			lrcFis.read();//去掉换行符
			if( !lrcTmp.equals( new String( fisBuf , "UTF-8" ) ) )
			{
				lrcFis.close();
				lrcBuffReader.close();
				return false;
			}
		}
		lrcFis.close();
		lrcBuffReader.close();
		return true;
	}
	
	public static List<LyricSentence> getLyricSentencesByFile(
			File lyricsFile )
	{
		List<LyricSentence> list = null;
		BufferedReader br = null;
		try
		{
			br = new BufferedReader( new InputStreamReader( new FileInputStream( lyricsFile ) , getEncoding( lyricsFile ) ) );
			StringBuilder sb = new StringBuilder();
			String temp = null;
			while( ( temp = br.readLine() ) != null )
			{
				sb.append( temp ).append( "\n" );
			}
			list = getLyricSentencesByContent( sb.toString() );
			sb.delete( 0 , sb.length() );
		}
		catch( Exception ex )
		{
			list = null;
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				br.close();
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
			}
		}
		return list;
	}
	
	public static List<LyricSentence> getLyricSentencesByContent(
			String content )
	{
		List<LyricSentence> list = null;
		try
		{
			BufferedReader br = new BufferedReader( new StringReader( content ) );
			String temp = null;
			list = new ArrayList<LyricSentence>();
			while( ( temp = br.readLine() ) != null )
			{
				parseLine( temp.trim() , list );
			}
			br.close();
			// 读进来以后就排序了
			Collections.sort( list , new Comparator<LyricSentence>() {
				
				public int compare(
						LyricSentence o1 ,
						LyricSentence o2 )
				{
					if( !( o1 instanceof LyricSentence && o2 instanceof LyricSentence ) )
					{
						return 0;
					}
					return (int)( o1.getFromTime() - o2.getFromTime() );
				}
			} );
			int size = list.size();
			for( int i = 0 ; i < size ; i++ )
			{
				LyricSentence next = null;
				if( i + 1 < size )
				{
					next = list.get( i + 1 );
				}
				LyricSentence now = list.get( i );
				if( next != null )
				{
					now.setToTime( next.getFromTime() - 1 );
				}
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		return list;
	}
	
	public static void parseLine(
			String line ,
			List<LyricSentence> list )
	{
		if( !line.equals( "" ) )
		{
			Matcher matcher = pattern.matcher( line );
			List<String> temp = new ArrayList<String>();
			int lastIndex = -1;// 最后一个时间标签的下标
			int lastLength = -1;// 最后一个时间标签的长度
			while( matcher.find() )
			{
				String s = matcher.group();
				int index = line.indexOf( StringUtils.concat( "[" , s , "]" ) );
				if( lastIndex != -1 && index - lastIndex > lastLength + 2 )
				{
					// 如果大于上次的大小，则中间夹了别的内容在里面
					// 这个时候就要分段了
					String content = line.substring( lastIndex + lastLength + 2 , index );
					for( String str : temp )
					{
						long t = parseTime( str );
						if( t != -1 )
						{
							list.add( new LyricSentence( content , t ) );
						}
					}
					temp.clear();
				}
				temp.add( s );
				lastIndex = index;
				lastLength = s.length();
			}
			// 如果列表为空，则表示本行没有分析出任何标签
			if( !temp.isEmpty() )
			{
				try
				{
					int length = lastLength + 2 + lastIndex;
					String content = line.substring( length > line.length() ? line.length() : length );
					// if (Config.getConfig().isCutBlankChars()) {
					// content = content.trim();
					// }
					// 当已经有了偏移量的时候，就不再分析了
					/*
					 * if (content.equals("") && offset == 0) { for (String s :
					 * temp) { int of = parseOffset(s); if (of != Integer.MAX_VALUE)
					 * { offset = of; info.setOffset(offset); break;// 只分析一次 } }
					 * return; }
					 */
					for( String s : temp )
					{
						long t = parseTime( s );
						if( t != -1 )
						{
							list.add( new LyricSentence( content , t ) );
						}
					}
				}
				catch( Exception ex )
				{
					ex.printStackTrace();
				}
			}
		}
	}
	
	public static long parseTime(
			String time )
	{
		String[] ss = time.split( "\\:|\\." );
		// 如果 是两位以后，就非法了
		if( ss.length < 2 )
		{
			return -1;
		}
		else if( ss.length == 2 )
		{// 如果正好两位，就算分秒
			try
			{
				// 先看有没有一个是记录了整体偏移量的
				/*
				 * if (offset == 0 && ss[0].equalsIgnoreCase("offset")) {
				 * offset = Integer.parseInt(ss[1]); info.setOffset(offset);
				 * System.err.println("整体的偏移量:" + offset); return -1; }
				 */
				int min = Integer.parseInt( ss[0] );
				int sec = Integer.parseInt( ss[1] );
				if( min < 0 || sec < 0 || sec >= 60 )
				{
					throw new RuntimeException( "数字不合法!" );
				}
				// System.out.println("time" + (min * 60 + sec) * 1000L);
				return ( min * 60 + sec ) * 1000L;
			}
			catch( Exception exe )
			{
				return -1;
			}
		}
		else if( ss.length == 3 )
		{// 如果正好三位，就算分秒，十毫秒
			try
			{
				int min = Integer.parseInt( ss[0] );
				int sec = Integer.parseInt( ss[1] );
				int mm = Integer.parseInt( ss[2] );
				// gaominghui@2016/12/07 ADD START 解决vivo手机音乐播放器下载下来的音乐歌词文件格式与普通歌词文件显示的时间不一致导致的歌词进度不对的bug【自测】
				long returnValue = 0l;
				if( ss[2].length() == 2 )
				{
					if( min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99 )
					{
						throw new RuntimeException( "数字不合法!" );
					}
					returnValue = ( min * 60 + sec ) * 1000L + mm * 10;
				}
				else if( ss[2].length() == 3 )
				{
					if( min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 999 )
					{
						throw new RuntimeException( "数字不合法!" );
					}
					returnValue = ( min * 60 + sec ) * 1000L + mm;
				}
				return returnValue;
				// gaominghui@2016/12/07 ADD END 解决vivo手机音乐播放器下载下来的音乐歌词文件格式与普通歌词文件显示的时间不一致导致的歌词进度不对的bug【自测】
			}
			catch( Exception exe )
			{
				return -1;
			}
		}
		else
		{// 否则也非法
			return -1;
		}
	}
}
