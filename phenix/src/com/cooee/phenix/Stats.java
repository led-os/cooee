package com.cooee.phenix;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.ItemInfo;


public class Stats
{
	
	private static final boolean DEBUG_BROADCASTS = false;
	private static final String TAG = "Phenix Launcher/Stats";
	private static final boolean LOCAL_LAUNCH_LOG = false;
	public static final String ACTION_LAUNCH = "com.cooee.phenix.action.LAUNCH";
	public static final String PERM_LAUNCH = "com.cooee.phenix.permission.RECEIVE_LAUNCH_BROADCASTS";
	public static final String EXTRA_INTENT = "intent";
	public static final String EXTRA_CONTAINER = "container";
	public static final String EXTRA_SCREEN = "screen";
	public static final String EXTRA_CELLX = "cellX";
	public static final String EXTRA_CELLY = "cellY";
	// zhangjin@2016/05/05 ADD START
	public static final String SOURCE_EXTRA_CONTAINER = "container";
	public static final String SOURCE_EXTRA_CONTAINER_PAGE = "container_page";
	public static final String SOURCE_EXTRA_SUB_CONTAINER = "sub_container";
	public static final String SOURCE_EXTRA_SUB_CONTAINER_PAGE = "sub_container_page";
	public static final String CONTAINER_ALL_APPS = "all_apps";
	public static final String SUB_CONTAINER_FOLDER = "folder";
	public static final String SUB_CONTAINER_ALL_APPS_A_Z = "a-z";
	public static final String SUB_CONTAINER_ALL_APPS_PREDICTION = "prediction";
	public static final String SUB_CONTAINER_ALL_APPS_SEARCH = "search";
	// zhangjin@2016/05/05 ADD END
	private static final String LOG_FILE_NAME = "launches.log";
	private static final int LOG_VERSION = 1;
	private static final int LOG_TAG_VERSION = 0x1;
	private static final int LOG_TAG_LAUNCH = 0x1000;
	private static final String STATS_FILE_NAME = "stats.log";
	private static final int STATS_VERSION = 1;
	private static final int INITIAL_STATS_SIZE = 100;
	// TODO: delayed/batched writes
	private static final boolean FLUSH_IMMEDIATELY = true;
	private final Launcher mLauncher;
	DataOutputStream mLog;
	ArrayList<String> mIntents;
	ArrayList<Integer> mHistogram;
	
	public Stats(
			Launcher launcher )
	{
		mLauncher = launcher;
		loadStats();
		if( LOCAL_LAUNCH_LOG )
		{
			try
			{
				mLog = new DataOutputStream( mLauncher.openFileOutput( LOG_FILE_NAME , Context.MODE_APPEND ) );
				mLog.writeInt( LOG_TAG_VERSION );
				mLog.writeInt( LOG_VERSION );
			}
			catch( FileNotFoundException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "unable to create stats log: " , e.toString() ) );
				mLog = null;
			}
			catch( IOException e )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "unable to write to stats log: " , e.toString() ) );
				mLog = null;
			}
		}
		if( DEBUG_BROADCASTS )
		{
			launcher.registerReceiver( new BroadcastReceiver() {
				
				@Override
				public void onReceive(
						Context context ,
						Intent intent )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.v( "Stats" , StringUtils.concat( "got broadcast: " , intent.toUri( 0 ) , " for launched intent: " , intent.getStringExtra( EXTRA_INTENT ) ) );
				}
			} , new IntentFilter( ACTION_LAUNCH ) , PERM_LAUNCH , null );
		}
	}
	
	public void incrementLaunch(
			String intentStr )
	{
		int pos = mIntents.indexOf( intentStr );
		if( pos < 0 )
		{
			mIntents.add( intentStr );
			mHistogram.add( 1 );
		}
		else
		{
			mHistogram.set( pos , mHistogram.get( pos ) + 1 );
		}
	}
	
	public void recordLaunch(
			Intent intent )
	{
		recordLaunch( intent , null );
	}
	
	public void recordLaunch(
			Intent intent ,
			final ItemInfo shortcut )
	{
		if( intent == null )
		{
			return;
		}
		intent = new Intent( intent );
		intent.setSourceBounds( null );
		final String flat = intent.toUri( 0 );
		Intent broadcastIntent = new Intent( ACTION_LAUNCH ).putExtra( EXTRA_INTENT , flat );
		if( shortcut != null )
		{
			broadcastIntent.putExtra( EXTRA_CONTAINER , shortcut.getContainer() ).putExtra( EXTRA_SCREEN , shortcut.getScreenId() ).putExtra( EXTRA_CELLX , shortcut.getCellX() )
					.putExtra( EXTRA_CELLY , shortcut.getCellY() );
		}
		mLauncher.sendBroadcast( broadcastIntent , PERM_LAUNCH );
		incrementLaunch( flat );
		if( FLUSH_IMMEDIATELY )
		{
			saveStats();
		}
		if( LOCAL_LAUNCH_LOG && mLog != null )
		{
			try
			{
				mLog.writeInt( LOG_TAG_LAUNCH );
				mLog.writeLong( System.currentTimeMillis() );
				if( shortcut == null )
				{
					mLog.writeShort( 0 );
					mLog.writeShort( 0 );
					mLog.writeShort( 0 );
					mLog.writeShort( 0 );
				}
				else
				{
					mLog.writeShort( (short)shortcut.getContainer() );
					mLog.writeShort( (short)shortcut.getScreenId() );
					mLog.writeShort( (short)shortcut.getCellX() );
					mLog.writeShort( (short)shortcut.getCellY() );
				}
				mLog.writeUTF( flat );
				if( FLUSH_IMMEDIATELY )
				{
					mLog.flush(); // TODO: delayed writes
				}
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
	}
	
	private void saveStats()
	{
		DataOutputStream stats = null;
		try
		{
			stats = new DataOutputStream( mLauncher.openFileOutput( StringUtils.concat( STATS_FILE_NAME , ".tmp" ) , Context.MODE_PRIVATE ) );
			stats.writeInt( STATS_VERSION );
			final int N = mHistogram.size();
			stats.writeInt( N );
			for( int i = 0 ; i < N ; i++ )
			{
				stats.writeUTF( mIntents.get( i ) );
				stats.writeInt( mHistogram.get( i ) );
			}
			stats.close();
			stats = null;
			mLauncher.getFileStreamPath( StringUtils.concat( STATS_FILE_NAME , ".tmp" ) ).renameTo( mLauncher.getFileStreamPath( STATS_FILE_NAME ) );
		}
		catch( FileNotFoundException e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( "unable to create stats data: " + e.toString() ) );
		}
		catch( IOException e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( "unable to write to stats data: " + e.toString() ) );
		}
		finally
		{
			if( stats != null )
			{
				try
				{
					stats.close();
				}
				catch( IOException e )
				{
				}
			}
		}
	}
	
	private void loadStats()
	{
		mIntents = new ArrayList<String>( INITIAL_STATS_SIZE );
		mHistogram = new ArrayList<Integer>( INITIAL_STATS_SIZE );
		DataInputStream stats = null;
		try
		{
			stats = new DataInputStream( mLauncher.openFileInput( STATS_FILE_NAME ) );
			final int version = stats.readInt();
			if( version == STATS_VERSION )
			{
				final int N = stats.readInt();
				for( int i = 0 ; i < N ; i++ )
				{
					final String pkg = stats.readUTF();
					final int count = stats.readInt();
					mIntents.add( pkg );
					mHistogram.add( count );
				}
			}
		}
		catch( FileNotFoundException e )
		{
			// not a problem
		}
		catch( IOException e )
		{
			// more of a problem
		}
		finally
		{
			if( stats != null )
			{
				try
				{
					stats.close();
				}
				catch( IOException e )
				{
				}
			}
		}
	}
	
	/**
	 * Implemented by containers to provide a launch source for a given child.
	 */
	public interface LaunchSourceProvider
	{
		
		void fillInLaunchSourceData(
				Bundle sourceData );
	}
}
