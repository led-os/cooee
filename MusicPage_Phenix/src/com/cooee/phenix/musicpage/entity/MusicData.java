package com.cooee.phenix.musicpage.entity;


import com.cooee.framework.utils.StringUtils;


// MusicPage
public class MusicData
{
	
	private long id = 0;//The unique ID for a row
	private String title = "";//The title of the content
	private long size = 0;//The size of the file in bytes
	private String album = "";//The album the audio file is from, if any
	private long album_id = 0;//The id of the album the audio file is from, if any
	private String artist = "";//The artist who created the audio file, if any
	private long artist_id = 0;//The id of the artist who created the audio file, if any
	private long bookmark = 0;//The position, in ms, playback was at when playback for this file was last stopped
	private String composer = "";//The composer of the audio file, if any
	private long duration = 0;//The duration of the audio file, in ms
	private int track = 0;//The track number of this song on the album, if any. This number encodes both the track number and the disc number. For multi-disc sets, this number will be 1xxx for tracks on the first disc, 2xxx for tracks on the second disc, etc.
	private int year = 0;//The year the audio file was recorded, if any
	private String data = "";
	private long position = 0;
	
	public String getData()
	{
		return data;
	}
	
	public void setData(
			String data )
	{
		if( data == null )
			data = "";
		this.data = data;
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setId(
			long id )
	{
		this.id = id;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(
			String title )
	{
		if( title == null )
			title = "";
		this.title = title;
	}
	
	public long getSize()
	{
		return size;
	}
	
	public void setSize(
			long size )
	{
		this.size = size;
	}
	
	public String getAlbum()
	{
		return album;
	}
	
	public void setAlbum(
			String album )
	{
		if( album == null )
			album = "";
		this.album = album;
	}
	
	public long getAlbum_id()
	{
		return album_id;
	}
	
	public void setAlbum_id(
			long album_id )
	{
		this.album_id = album_id;
	}
	
	public String getArtist()
	{
		return artist;
	}
	
	public void setArtist(
			String artist )
	{
		if( artist == null )
			artist = "";
		this.artist = artist;
	}
	
	public long getArtist_id()
	{
		return artist_id;
	}
	
	public void setArtist_id(
			long artist_id )
	{
		this.artist_id = artist_id;
	}
	
	public long getBookmark()
	{
		return bookmark;
	}
	
	public void setBookmark(
			long bookmark )
	{
		this.bookmark = bookmark;
	}
	
	public String getComposer()
	{
		return composer;
	}
	
	public void setComposer(
			String composer )
	{
		if( composer == null )
			composer = "";
		this.composer = composer;
	}
	
	public long getDuration()
	{
		return duration;
	}
	
	public void setDuration(
			long duration )
	{
		this.duration = duration;
	}
	
	public int getTrack()
	{
		return track;
	}
	
	public void setTrack(
			int track )
	{
		this.track = track;
	}
	
	public int getYear()
	{
		return year;
	}
	
	public void setYear(
			int year )
	{
		this.year = year;
	}
	
	public boolean equal(
			MusicData data )
	{
		boolean result = true;
		if( data == null || !this.getTitle().equals( data.getTitle() ) || !this.getArtist().equals( data.getArtist() ) || !this.getAlbum().equals( data.getAlbum() ) )
			result = false;
		return result;
	}
	
	/**
	 * @return the position
	 */
	public long getPosition()
	{
		return position;
	}
	
	/**
	 * @param position the position to set
	 */
	public void setPosition(
			long position )
	{
		this.position = position;
	}
	
	public void reset()
	{
		id = 0;//The unique ID for a row
		title = "";//The title of the content
		size = 0;//The size of the file in bytes
		album = "";//The album the audio file is from, if any
		album_id = 0;//The id of the album the audio file is from, if any
		artist = "";//The artist who created the audio file, if any
		artist_id = 0;//The id of the artist who created the audio file, if any
		bookmark = 0;//The position, in ms, playback was at when playback for this file was last stopped
		composer = "";//The composer of the audio file, if any
		duration = 0;//The duration of the audio file, in ms
		track = 0;//The track number of this song on the album, if any. This number encodes both the track number and the disc number. For multi-disc sets, this number will be 1xxx for tracks on the first disc, 2xxx for tracks on the second disc, etc.
		year = 0;//The year the audio file was recorded, if any
		data = "";
		position = 0;
	}
	
	/**
	 *
	 * @see java.lang.Object#toString()
	 * @auther gaominghui  2016年12月26日
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return StringUtils.concat(
				"MusicData( id:" ,
				getId() ,
				"; title:" ,
				getTitle() ,
				"; size:" ,
				getSize() ,
				"; album:" ,
				getAlbum() ,
				"; album_id:" ,
				getAlbum_id() ,
				"; artist:" ,
				getArtist() ,
				"; artist_id:" ,
				getArtist_id() ,
				"; duration:" ,
				getDuration() ,
				"; position:" ,
				getPosition() ,
				")" );
	}
}
