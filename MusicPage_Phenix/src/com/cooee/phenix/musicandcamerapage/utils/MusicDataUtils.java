package com.cooee.phenix.musicandcamerapage.utils;


// MusicPage CameraPage
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicpage.MusicView;
import com.cooee.phenix.musicpage.R;
import com.cooee.phenix.musicpage.entity.MusicData;


public class MusicDataUtils
{
	
	private static final String[] getMusicDataProjection = new String[]{
			MediaStore.Audio.Media._ID ,
			MediaStore.Audio.Media.TITLE ,
			MediaStore.Audio.Media.SIZE ,
			MediaStore.Audio.Media.ALBUM ,
			MediaStore.Audio.Media.ALBUM_ID ,
			MediaStore.Audio.Media.ARTIST ,
			MediaStore.Audio.Media.ARTIST_ID ,
			MediaStore.Audio.Media.BOOKMARK ,
			MediaStore.Audio.Media.COMPOSER ,
			MediaStore.Audio.Media.DURATION ,
			MediaStore.Audio.Media.TRACK ,
			MediaStore.Audio.Media.YEAR ,
			MediaStore.Audio.Media.DATA };
	
	public static MusicData getMusicData(
			Context context ,
			String sqlWhere ,
			String[] sqlArgs )
	{
		if( context == null || sqlWhere == null || sqlArgs == null )
			return null;
		ContentResolver contentResolver = context.getContentResolver();
		if( contentResolver == null )
			return null;
		Cursor cursor = null;
		if( EnvironmentUtils.isExternalStorageAvailable() )
		{
			cursor = contentResolver.query( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI , getMusicDataProjection , sqlWhere , sqlArgs , null );
		}
		else
		{
			cursor = contentResolver.query( MediaStore.Audio.Media.INTERNAL_CONTENT_URI , getMusicDataProjection , sqlWhere , sqlArgs , null );
		}
		MusicData data = new MusicData();
		if( cursor != null )
		{
			if( cursor.moveToFirst() )
			{
				int idIndex = cursor.getColumnIndex( MediaStore.Audio.Media._ID );
				int titleIndex = cursor.getColumnIndex( MediaStore.Audio.Media.TITLE );
				int sizeIndex = cursor.getColumnIndex( MediaStore.Audio.Media.SIZE );
				int albumIndex = cursor.getColumnIndex( MediaStore.Audio.Media.ALBUM );
				int albumIdIndex = cursor.getColumnIndex( MediaStore.Audio.Media.ALBUM_ID );
				int artistIndex = cursor.getColumnIndex( MediaStore.Audio.Media.ARTIST );
				int artistIdIndex = cursor.getColumnIndex( MediaStore.Audio.Media.ARTIST_ID );
				int bookmarkIndex = cursor.getColumnIndex( MediaStore.Audio.Media.BOOKMARK );
				int composerIndex = cursor.getColumnIndex( MediaStore.Audio.Media.COMPOSER );
				int durationIndex = cursor.getColumnIndex( MediaStore.Audio.Media.DURATION );
				int trackIndex = cursor.getColumnIndex( MediaStore.Audio.Media.TRACK );
				int yearIndex = cursor.getColumnIndex( MediaStore.Audio.Media.YEAR );
				int dataIndex = cursor.getColumnIndex( MediaStore.Audio.Media.DATA );
				//
				data.setId( cursor.getLong( idIndex ) );
				data.setTitle( cursor.getString( titleIndex ) );
				data.setSize( cursor.getLong( sizeIndex ) );
				data.setAlbum( cursor.getString( albumIndex ) );
				data.setAlbum_id( cursor.getLong( albumIdIndex ) );
				data.setArtist( cursor.getString( artistIndex ) );
				data.setArtist_id( cursor.getLong( artistIdIndex ) );
				data.setBookmark( cursor.getLong( bookmarkIndex ) );
				data.setComposer( cursor.getString( composerIndex ) );
				data.setDuration( cursor.getLong( durationIndex ) );
				data.setTrack( cursor.getInt( trackIndex ) );
				data.setYear( cursor.getInt( yearIndex ) );
				data.setData( cursor.getString( dataIndex ) );
				if( data.getArtist().equals( "<unknown>" ) )
				{
					StringBuffer buffer = new StringBuffer();
					buffer.append( "<" );
					buffer.append( context.getString( R.string.music_page_unknown ) );
					buffer.append( ">" );
					data.setArtist( buffer.toString() );
					buffer = null;
				}
			}
			cursor.close();
		}
		return data;
	}
	
	public static MusicData getMusicDataByParamId(
			Context context ,
			long paramId )
	{
		if( paramId == -1 )
			return null;
		return getMusicData( context , " _id=? " , new String[]{ String.valueOf( paramId ) } );
	}
	
	public static MusicData getMusicDataByData(
			Context context ,
			String data )
	{
		if( TextUtils.isEmpty( data ) )
			return null;
		return getMusicData( context , StringUtils.concat( MediaStore.Audio.Media.DATA , "=?" ) , new String[]{ data } );
	}
	
	public static MusicData getMusicDataByArtistAndTrack(
			Context context ,
			String artistName ,
			String trackName )
	{
		if( TextUtils.isEmpty( artistName ) || TextUtils.isEmpty( trackName ) )
			return null;
		return getMusicData( context , StringUtils.concat( MediaStore.Audio.Media.ARTIST , "=?" , " and " , MediaStore.Audio.Media.TITLE , "=?" ) , new String[]{ artistName , trackName } );
	}
	
	// gaominghui@2016/10/29 ADD 抽出来两个公用方法
	public static ComponentName getMusicServiceComponentName(
			Activity activity )
	{
		ComponentName componentName = null;
		PackageManager packageManager = activity.getPackageManager();
		List<String> music_service_array = MusicView.configUtils.getStringArray( "music_page_music_services" );
		for( int i = 0 ; i < music_service_array.size() ; i++ )
		{
			String serviceStr = music_service_array.get( i );
			String[] serviceComponentArray = serviceStr.split( ";" );
			if( serviceComponentArray.length >= 2 )
			{
				Intent intent = new Intent();
				intent.setClassName( serviceComponentArray[0] , serviceComponentArray[1] );
				List<ResolveInfo> resoveInfos = packageManager.queryIntentServices( intent , 0 );
				if( resoveInfos.size() > 0 )
				{
					componentName = new ComponentName( serviceComponentArray[0] , serviceComponentArray[1] );
				}
			}
		}
		return componentName;
	}
	
	public static boolean isMusicServiceStarted(
			Activity activity ,
			ComponentName componentName )
	{
		boolean started = false;
		if( componentName != null )
		{
			ActivityManager manager = (ActivityManager)activity.getSystemService( Context.ACTIVITY_SERVICE );
			for( RunningServiceInfo service : manager.getRunningServices( Integer.MAX_VALUE ) )
			{
				if( componentName != null )
				{
					if( service.service.getPackageName().equals( componentName.getPackageName() ) && service.service.getClassName().equals( componentName.getClassName() ) )
					{
						started = true;
						break;
					}
				}
			}
		}
		return started;
	}
	// gaominghui@2016/10/29 ADD END  抽出来两个公用方法
}
