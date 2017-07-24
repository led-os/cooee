package com.cooee.wallpaper.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class WallpaperDbHelper extends SQLiteOpenHelper
{
	
	private static final String LOG_TAG = "DbHelper";
	private static final String DATABASE_NAME = "wallpapers.db";
	private static final int DATABASE_VERSION = 1;
	public static boolean lock_db = false;
	
	public WallpaperDbHelper(
			Context cxt )
	{
		super( cxt , DATABASE_NAME , null , DATABASE_VERSION );
	}
	
	@Override
	public void onCreate(
			SQLiteDatabase db )
	{
		Log.i( LOG_TAG , "onCreate," + DATABASE_NAME + "," + DATABASE_VERSION );
		db.execSQL( DownloadList.getCreateSql() );
	}
	
	@Override
	public void onUpgrade(
			SQLiteDatabase db ,
			int oldVersion ,
			int newVersion )
	{
		Log.i( LOG_TAG , String.format( "onUpgrade,dbName=%s,old=%d,new=%d" , DATABASE_NAME , oldVersion , newVersion ) );
		db.execSQL( DownloadList.getDropSql() );
		db.execSQL( DownloadList.getCreateSql() );
	}
	
	@Override
	public synchronized SQLiteDatabase getReadableDatabase()
	{
		// Log.e("db", "getRead");
		while( lock_db )
		{
			try
			{
				this.wait( 50 );
			}
			catch( InterruptedException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		SQLiteDatabase db = null;
		boolean exception = true;
		while( exception )
		{
			try
			{
				exception = false;
				db = super.getReadableDatabase();
			}
			catch( Exception e )
			{
				e.printStackTrace();
				exception = true;
				try
				{
					this.wait( 10 );
				}
				catch( InterruptedException e1 )
				{
					e1.printStackTrace();
				}
			}
		}
		lock_db = true;
		return db;
	}
	
	@Override
	public synchronized SQLiteDatabase getWritableDatabase()
	{
		// Log.e("db", "getWrite");
		while( lock_db )
		{
			try
			{
				this.wait( 50 );
			}
			catch( InterruptedException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		SQLiteDatabase db = null;
		boolean exception = true;
		while( exception )
		{
			try
			{
				exception = false;
				db = super.getWritableDatabase();
			}
			catch( Exception e )
			{
				e.printStackTrace();
				exception = true;
				try
				{
					this.wait( 10 );
				}
				catch( InterruptedException e1 )
				{
					e1.printStackTrace();
				}
			}
		}
		lock_db = true;
		return db;
	}
}
