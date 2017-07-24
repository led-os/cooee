package com.cooee.favorites.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cooee.favorites.manager.FavoritesManager;


public class FavoritesHelper extends SQLiteOpenHelper
{
	
	private static String DB_NAME = "favorites.db";
	public static final String TABLE_FAVORITES_APPS = "favorites_apps";//常用应用和常用联系人（新功能） hp@2015/09/29 ADD START
	public static final String TABLE_FAVORITES_CONTACTS = "favorites_contacts";
	public static final String TABLE_FAVORITES_AD = "favorites_ad";//负一屏广告 zhengkai
	static final String PARAMETER_NOTIFY = "notify";
	private static final String TAG = "FavoritesHelper";
	
	public FavoritesHelper(
			Context context )
	{
		super( context , DB_NAME , null , 1 );
	}
	
	@Override
	public void onCreate(
			SQLiteDatabase db )
	{
		//Create table
		String sql = "CREATE TABLE " + TABLE_FAVORITES_APPS + "(" + "_id INTEGER PRIMARY KEY," + "title TEXT," + "intent TEXT," + "launchTimes TEXT);";
		Log.e( TAG , " FavoritesHelper onCreate create table" );
		db.execSQL( sql );
		sql = "CREATE TABLE " + TABLE_FAVORITES_CONTACTS + "(" + "_id INTEGER PRIMARY KEY," + "contact_id TEXT," + "display_name TEXT," + "lookup_key TEXT," + "callTimes TEXT);";
		Log.e( TAG , " FavoritesHelper onCreate create table" );
		db.execSQL( sql );
		sql = "CREATE TABLE " + TABLE_FAVORITES_AD + "(" + "_id INTEGER PRIMARY KEY," + "title TEXT," + "ad_place_id TEXT," + "ad_id TEXT," + "ad_icon TEXT," + "ad_data TEXT);";
		db.execSQL( sql );
	}
	
	@Override
	public void onUpgrade(
			SQLiteDatabase db ,
			int oldVersion ,
			int newVersion )
	{
		Log.e( TAG , " FavoritesHelper onUpgrade update oldVersion: " + oldVersion + " newVersion: " + newVersion );
		checkPluginDateBase();//cheyingkun add	//解决“LauncherPhenix_V1.1.0.41895.20160516.apk之前版本，热更新最新版酷生活后，运营附近广告桌面重启”的问题【i_0014690】
	}
	
	//cheyingkun add start	//解决“LauncherPhenix_V1.1.0.41895.20160516.apk之前版本，热更新最新版酷生活后，运营附近广告桌面重启”的问题【i_0014690】
	public void checkPluginDateBase()
	{
		//酷生活41884之后,添加附近广告
		if( !FavoritesManager.getInstance().isMoreThanTheVersion( -1 , 41884 ) )
		{
			SQLiteDatabase db = getReadableDatabase();
			Log.i( TAG , " FavoritesHelper checkPluginUpDate 0" );
			db.execSQL( "DROP TABLE IF EXISTS " + TABLE_FAVORITES_AD );
			Log.d( TAG , " FavoritesHelper checkPluginUpDate 1" );
			db.execSQL( "CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES_AD + "(" + "_id INTEGER PRIMARY KEY," + "title TEXT," + "ad_place_id TEXT," + "ad_id TEXT," + "ad_icon TEXT," + "ad_data TEXT);" );
			Log.e( TAG , " FavoritesHelper checkPluginUpDate 2" );
		}
	}
	
	//cheyingkun add end
	public static final class Contacts
	{
		
		public static final String _ID = "_id";
		static final String PARAMETER_NOTIFY = "notify";
		public static final String CONTACT_ID = "contact_id";
		public static final String NAME = "display_name";
		public static final String LOOKUP = "lookup_key";
		public static final String CALLTIMES = "callTimes";
	}
	
	public static final class Favorites
	{
		
		public static final String _ID = "_id";
		public static final String INTENT = "intent";
		public static final String TITLE = "title";
		public static final String LAUNCH_TIMES = "launchTimes";
	}
	
	/**
	 * 广告名称
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String AD_TITLE = "title";
	/**
	 * 广告位id
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String AD_PLACE_ID = "ad_place_id";
	/**
	 * 广告id
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String AD_ID = "ad_id";
	/**
	 * zhengkai @2015/11/06 ADD END
	 * 广告图片
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String AD_ICON = "ad_icon";
	/**
	 * zhengkai @2015/11/06 ADD END
	 * 广告数据
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String AD_DATA = "ad_data";
}
