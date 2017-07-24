package com.cooee.phenix;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class MemoryDumpActivity extends Activity
{
	
	private static final String TAG = "MemoryDumpActivity";
	
	@Override
	public void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
	}
	
	public static String zipUp(
			ArrayList<String> paths )
	{
		final int BUFSIZ = 256 * 1024; // 256K
		final byte[] buf = new byte[BUFSIZ];
		final String zipfilePath = String.format( "%s/hprof-%d.zip" , Environment.getExternalStorageDirectory() , System.currentTimeMillis() );
		ZipOutputStream zos = null;
		try
		{
			OutputStream os = new FileOutputStream( zipfilePath );
			zos = new ZipOutputStream( new BufferedOutputStream( os ) );
			for( String filename : paths )
			{
				InputStream is = null;
				try
				{
					is = new BufferedInputStream( new FileInputStream( filename ) );
					ZipEntry entry = new ZipEntry( filename );
					zos.putNextEntry( entry );
					int len;
					while( 0 < ( len = is.read( buf , 0 , BUFSIZ ) ) )
					{
						zos.write( buf , 0 , len );
					}
					zos.closeEntry();
				}
				finally
				{
					is.close();
				}
			}
		}
		catch( IOException e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "error zipping up profile data" , e );
			return null;
		}
		finally
		{
			if( zos != null )
			{
				try
				{
					zos.close();
				}
				catch( IOException e )
				{
					// ugh, whatever
				}
			}
		}
		return zipfilePath;
	}
	
	public static void dumpHprofAndShare(
			final Context context ,
			MemoryTracker tracker )
	{
		final StringBuilder body = new StringBuilder();
		final ArrayList<String> paths = new ArrayList<String>();
		final int myPid = android.os.Process.myPid();
		final int[] pids_orig = tracker.getTrackedProcesses();
		final int[] pids_copy = Arrays.copyOf( pids_orig , pids_orig.length );
		for( int pid : pids_copy )
		{
			MemoryTracker.ProcessMemInfo info = tracker.getMemInfo( pid );
			if( info != null )
			{
				body.append( "pid " ).append( pid ).append( ":" ).append( " up=" ).append( info.getUptime() ).append( " pss=" ).append( info.currentPss ).append( " uss=" ).append( info.currentUss )
						.append( "\n" );
			}
			if( pid == myPid )
			{
				final String path = String.format( "%s/launcher-memory-%d.ahprof" , Environment.getExternalStorageDirectory() , pid );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , "Dumping memory info for process " + pid + " to " + path );
				try
				{
					android.os.Debug.dumpHprofData( path ); // will block
				}
				catch( IOException e )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.e( TAG , "error dumping memory:" , e );
				}
				paths.add( path );
			}
		}
		String zipfile = zipUp( paths );
		if( zipfile == null )
			return;
		Intent shareIntent = new Intent( Intent.ACTION_SEND );
		shareIntent.setType( "application/zip" );
		final PackageManager pm = context.getPackageManager();
		shareIntent.putExtra( Intent.EXTRA_SUBJECT , String.format( "Launcher memory dump (%d)" , myPid ) );
		String appVersion;
		try
		{
			appVersion = pm.getPackageInfo( context.getPackageName() , 0 ).versionName;
		}
		catch( PackageManager.NameNotFoundException e )
		{
			appVersion = "?";
		}
		body.append( "\nApp version: " ).append( appVersion ).append( "\nBuild: " ).append( Build.DISPLAY ).append( "\n" );
		shareIntent.putExtra( Intent.EXTRA_TEXT , body.toString() );
		final File pathFile = new File( zipfile );
		final Uri pathUri = Uri.fromFile( pathFile );
		shareIntent.putExtra( Intent.EXTRA_STREAM , pathUri );
		context.startActivity( shareIntent );
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		startDump( this , new Runnable() {
			
			@Override
			public void run()
			{
				finish();
			}
		} );
	}
	
	public static void startDump(
			final Context context )
	{
		startDump( context , null );
	}
	
	public static void startDump(
			final Context context ,
			final Runnable andThen )
	{
		final ServiceConnection connection = new ServiceConnection() {
			
			public void onServiceConnected(
					ComponentName className ,
					IBinder service )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.v( TAG , "service connected, dumping..." );
				dumpHprofAndShare( context , ( (MemoryTracker.MemoryTrackerInterface)service ).getService() );
				context.unbindService( this );
				if( andThen != null )
					andThen.run();
			}
			
			public void onServiceDisconnected(
					ComponentName className )
			{
			}
		};
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( TAG , "attempting to bind to memory tracker" );
		context.bindService( new Intent( context , MemoryTracker.class ) , connection , Context.BIND_AUTO_CREATE );
	}
}
