package com.cooee.phenix.musicpage.control;


// MusicPage
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import com.android.music.IMediaPlaybackService;
import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicandcamerapage.utils.MusicDataUtils;
import com.cooee.phenix.musicpage.MusicView;
import com.cooee.phenix.musicpage.entity.MusicData;


public class SystemMusic implements MusicControlInterface
{
	
	private static final String TAG = "SystemMusic";
	private final String SERVICECMD = "com.android.music.musicservicecommand";
	private final String CMDNAME = "command";
	private final String CMDTOGGLEPAUSE = "togglepause";
	//	private final String CMDSTOP = "stop";
	private final String CMDPAUSE = "pause";
	private final String CMDPREVIOUS = "previous";
	private final String CMDNEXT = "next";
	private final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
	private final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
	private final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
	private final String NEXT_ACTION = "com.android.music.musicservicecommand.next";
	private final String META_CHANGED = "com.android.music.metachanged";
	private final String QUEUE_CHANGED = "com.android.music.queuechanged";
	private final String PLAY_COMPLETED = "com.android.music.playbackcomplete";
	private final String PLAY_STATE_CHANGED = "com.android.music.playstatechanged";
	private final String REPEAT_SHUFFLE_CHANGED = "com.android.music.repeatshufflechanged";
	private final String POSITION_CHANGED = "com.android.music.position_changed";
	//
	private Activity activity = null;
	private MusicControlCallBack callBack = null;
	private ServiceConnection conn = null;
	private IMediaPlaybackService mediaPlayBackService = null;
	//private ComponentName componentName = null;
	//
	private boolean mediaMounted = true;// sd卡在的
	
	@Override
	public void init(
			Activity activity ,
			MusicControlCallBack callBack )
	{
		this.activity = activity;
		this.callBack = callBack;
		//
		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction( SERVICECMD );
		commandFilter.addAction( TOGGLEPAUSE_ACTION );
		commandFilter.addAction( PAUSE_ACTION );
		commandFilter.addAction( NEXT_ACTION );
		commandFilter.addAction( PREVIOUS_ACTION );
		commandFilter.addAction( META_CHANGED );
		commandFilter.addAction( QUEUE_CHANGED );
		commandFilter.addAction( PLAY_COMPLETED );
		commandFilter.addAction( PLAY_STATE_CHANGED );
		commandFilter.addAction( REPEAT_SHUFFLE_CHANGED );
		commandFilter.addAction( POSITION_CHANGED );
		activity.registerReceiver( receiver , commandFilter );
		//
		IntentFilter mediaFilter = new IntentFilter();
		mediaFilter.addAction( Intent.ACTION_MEDIA_EJECT );
		mediaFilter.addAction( Intent.ACTION_MEDIA_MOUNTED );
		mediaFilter.addAction( Intent.ACTION_MEDIA_UNMOUNTED );
		mediaFilter.addDataScheme( "file" );
		activity.registerReceiver( receiver , mediaFilter );
		//
		startService( activity );
	}
	
	public void finish()
	{
		if( activity != null && conn != null )
			activity.unbindService( conn );
		if( activity != null && receiver != null )
			activity.unregisterReceiver( receiver );
		activity = null;
		conn = null;
		receiver = null;
		callBack = null;
		//componentName = null;
	}
	
	/*private ComponentName getMusicServiceComponentName(
			Activity activity )
	{
		if( componentName == null )
		{
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
		}
		return componentName;
	}*/
	private void bindService(
			ComponentName componentName )
	{
		if( MusicView.configUtils.getBoolean( "music_page_bind_service" , false ) )
		{
			if( conn == null )
			{
				conn = new ServiceConnection() {
					
					@Override
					public void onServiceDisconnected(
							ComponentName name )
					{
						mediaPlayBackService = null;
						callBack.onMusicPause();
						// gaominghui@2016/11/25 ADD START c_0004566与后台音乐服务断开后重新绑定服务
						if( activity != null )
						{
							startService( activity );
						}
						// gaominghui@2016/11/25 ADD END
					}
					
					@Override
					public void onServiceConnected(
							ComponentName name ,
							IBinder service )
					{
						mediaPlayBackService = IMediaPlaybackService.Stub.asInterface( service );
					}
				};
			}
			Intent serviceIntent = new Intent();
			serviceIntent.setComponent( componentName );
			activity.bindService( serviceIntent , conn , 0 );
		}
	}
	
	private void serviceCommand(
			Activity activity ,
			String command )
	{
		ComponentName componentName = MusicDataUtils.getMusicServiceComponentName( activity );
		if( componentName != null )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "andy" , "serviceCommand!!" );
			Intent serviceIntent = new Intent();
			serviceIntent.setComponent( componentName );
			if( MusicDataUtils.isMusicServiceStarted( activity , componentName ) && !MusicView.configUtils.getBoolean( "music_page_start_service_whenever_started" ) )
			{
				if( command != null )
				{
					Intent intent = new Intent( SERVICECMD );
					intent.putExtra( CMDNAME , command );
					activity.sendBroadcast( intent );
				}
			}
			else
			{
				if( command != null )
				{
					serviceIntent.putExtra( CMDNAME , command );
				}
				// gaominghui@2016/07/21 ADD START 防止有的手机系统音乐服务不开放报错
				/*activity.startService( serviceIntent );
				bindService( componentName );*/
				try
				{
					activity.startService( serviceIntent );
				}
				catch( SecurityException e )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , "startMusicService startService SecurityException!!" );
				}
				try
				{
					bindService( componentName );
				}
				catch( SecurityException e )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , "startMusicService bindService SecurityException!!" );
				}
				// gaominghui@2016/07/21 ADD END 防止有的手机系统音乐服务不开放报错
			}
		}
	}
	
	// 播放控制 , start
	private void startService(
			Activity activity )
	{
		serviceCommand( activity , null );
	}
	
	@Override
	public void play()
	{
		if( mediaMounted )
			serviceCommand( activity , CMDTOGGLEPAUSE );
		else
			callBack.onMusicPause();
	}
	
	@Override
	public void pause()
	{
		serviceCommand( activity , CMDPAUSE );
	}
	
	@Override
	public void next()
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( "andy" , "next!!" );
		serviceCommand( activity , CMDNEXT );
	}
	
	@Override
	public void previous()
	{
		serviceCommand( activity , CMDPREVIOUS );
	}
	
	@Override
	public void seek(
			long position ,
			long duration )
	{
		try
		{
			if( mediaPlayBackService == null )
				return;
			mediaPlayBackService.seek( position );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void enterClient(
			MusicData curMusicData ,
			boolean isPlaying )
	{
		try
		{
			Intent intent = null;
			PackageManager packageManager = activity.getPackageManager();
			ArrayList<String> activitys = new ArrayList<String>();
			if( curMusicData != null && isPlaying )
			{
				activitys = MusicView.configUtils.getStringArray( "music_page_music_back_activity" );
			}
			else
			{
				activitys = MusicView.configUtils.getStringArray( "music_page_music_activity" );
			}
			for( int i = 0 ; i < activitys.size() ; i++ )
			{
				String serviceStr = activitys.get( i );
				String[] serviceComponentarray = serviceStr.split( ";" );
				if( serviceComponentarray.length >= 2 )
				{
					intent = new Intent();
					intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
					intent.setClassName( serviceComponentarray[0] , serviceComponentarray[1] );
					List<ResolveInfo> resoveInfos = packageManager.queryIntentActivities( intent , 0 );
					if( resoveInfos.size() > 0 )
					{
						break;
					}
					else
					{
						intent = null;
					}
				}
			}
			// gaominghui@2017/02/08 ADD START 0014248: 【音乐页】米4手机音乐页点击进光盘进入音乐列表，直接报音乐停止运行，过滤掉MI4手机，规避掉系统音乐报停问题
			if( Build.BRAND.contains( "Xiaomi" ) && Build.MODEL.contains( "MI 4LTE" ) )
				return;
			// gaominghui@2017/02/08 ADD END 0014248: 【音乐页】米4手机音乐页点击进光盘进入音乐列表，直接报音乐停止运行，过滤掉MI4手机，规避掉系统音乐报停问题
			if( intent != null && curMusicData != null )
			{
				Uri personUri = ContentUris.withAppendedId( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI , curMusicData.getId() );
				intent.setDataAndType( personUri , "audio/*" );
				intent.setData( null );
				activity.startActivity( intent );
			}
			else
			{
				intent.setAction( android.content.Intent.ACTION_VIEW );
				activity.startActivity( intent );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	// 播放控制 , end 
	// 回调 , start
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(
				Context context ,
				Intent intent )
		{
			String action = intent.getAction();
			MusicView.logI( StringUtils.concat( "onReceive action:" , action ) );
			if( SERVICECMD.equals( action ) )
			{
			}
			else if( META_CHANGED.equals( action ) )
			{
				onMetaChange( activity , intent );
			}
			else if( PLAY_STATE_CHANGED.equals( action ) )
			{
				onPlayStateChanged( intent );
			}
			else if( POSITION_CHANGED.equals( action ) )
			{
				onPositionChange( intent );
			}
			else if( SERVICECMD.equals( action ) )
			{
			}
			else if( TOGGLEPAUSE_ACTION.equals( action ) )
			{
			}
			else if( PAUSE_ACTION.equals( action ) )
			{
			}
			else if( NEXT_ACTION.equals( action ) )
			{
			}
			else if( PREVIOUS_ACTION.equals( action ) )
			{
			}
			else if( QUEUE_CHANGED.equals( action ) )
			{
			}
			else if( PLAY_COMPLETED.equals( action ) )
			{
			}
			else if( REPEAT_SHUFFLE_CHANGED.equals( action ) )
			{
			}
			//
			else if( Intent.ACTION_MEDIA_EJECT.equals( action ) )
			{
				mediaMounted = false;
			}
			else if( Intent.ACTION_MEDIA_MOUNTED.equals( action ) )
			{
				mediaMounted = true;
			}
			else if( Intent.ACTION_MEDIA_UNMOUNTED.equals( action ) )
			{
			}
		}
	};
	
	private void onPlayStateChanged(
			Intent intent )
	{
		boolean playing = intent.getBooleanExtra( "playing" , false );
		if( MusicView.configUtils.getBoolean( "music_page_bind_service" , false ) && mediaPlayBackService != null )
		{
			try
			{
				playing = mediaPlayBackService.isPlaying();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		if( playing )
			callBack.onMusicPlay();
		else
			callBack.onMusicPause();
	}
	
	private void onMetaChange(
			Activity activity ,
			Intent intent )
	{
		long paramId = intent.getLongExtra( "id" , -1 );
		MusicData musicData = MusicDataUtils.getMusicDataByParamId( activity , paramId );
		callBack.onMusicInfoChange( activity , musicData );
		if( musicData == null )
			callBack.onMusicPause();
	}
	
	private void onPositionChange(
			Intent intent )
	{
		Bundle extras = intent.getExtras();
		if( extras != null )
		{
			long position = extras.getLong( "position" , 0 );
			long duration = extras.getLong( "duration" , 0 );
			callBack.onMusicPositionChange( position , duration );
		}
	}
	
	// 回调 , end
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#getPosition()
	 * @auther gaominghui  2016年10月27日
	 */
	@Override
	public long getPosition()
	{
		long position = -1;
		if( mediaPlayBackService != null )
			try
			{
				position = mediaPlayBackService.position();
			}
			catch( Exception e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return position;
	}
	
	/**
	 *
	 * @see com.cooee.phenix.musicpage.control.MusicControlInterface#getDuration()
	 * @auther gaominghui  2016年10月27日
	 */
	@Override
	public long getDuration()
	{
		long duration = -1;
		if( mediaPlayBackService != null )
			try
			{
				duration = mediaPlayBackService.duration();
			}
			catch( Exception e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return duration;
	}
}
