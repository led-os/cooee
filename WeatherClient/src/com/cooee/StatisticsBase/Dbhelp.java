package com.cooee.StatisticsBase;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class Dbhelp extends SQLiteOpenHelper
{
	
	private static final int VERSION = 1;
	private String BaseName = "";
	
	public Dbhelp(
			Context context ,
			String name ,
			CursorFactory factory ,
			int version )
	{
		super( context , name , factory , version );
	}
	
	public Dbhelp(
			Context context ,
			String name ,
			int version )
	{
		this( context , name , null , version );
	}
	
	public Dbhelp(
			Context context ,
			String name )
	{
		this( context , name , VERSION );
		BaseName = name;
	}
	
	@Override
	public void onCreate(
			SQLiteDatabase db )
	{
		Log.v( "clear" , "onCreate db" );
		String CREATE_TABLE = "CREATE TABLE SamWeatherClock (_id INTEGER PRIMARY KEY,num INTEGER)";
		db.execSQL( CREATE_TABLE );
	}
	
	@Override
	public void onUpgrade(
			SQLiteDatabase db ,
			int oldVersion ,
			int newVersion )
	{
	}
	
	public boolean onSerch(
			SQLiteDatabase db ,
			String table )
	{
		Cursor cursor = null;
		String sql = "select * from " + table + " where num = 1";
		cursor = db.rawQuery( sql , null );
		Log.v( "clear" , "cursor = " + cursor );
		if( cursor.moveToNext() )
		{
			cursor.close();
			return true;
		}
		else
		{
			cursor.close();
			return false;
		}
	}
	
	public void onCreateTable(
			SQLiteDatabase db ,
			String table )
	{
		String CREATE_TABLE = "CREATE TABLE " + table + " (_id INTEGER PRIMARY KEY,num INTEGER)";
		db.execSQL( CREATE_TABLE );
	}
}
