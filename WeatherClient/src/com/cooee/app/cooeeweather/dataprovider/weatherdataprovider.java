package com.cooee.app.cooeeweather.dataprovider;


// import com.cooee.weather.dataentity.CitysEntity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import com.cooee.app.cooeeweather.dataentity.PostalCodeEntity;
import com.cooee.app.cooeeweather.dataentity.SettingEntity;
import com.cooee.app.cooeeweather.dataentity.weatherdataentity;
import com.cooee.app.cooeeweather.dataentity.weatherforecastentity;
import com.cooee.app.cooeeweather.dataprovider.MsnWeatherModel.MsnXMLDataField;
import com.cooee.app.cooeeweather.filehelp.Log;


public class weatherdataprovider extends ContentProvider
{
	
	// Content URI
	public static final String AUTHORITY = "com.cooee.app.cooeeweather.dataprovider";
	private static final String TAG = "com.cooee.weather.dataprovider.WeatherDataProvider";
	// table weather name
	public static final String TABLE_WEATHERDATA = "weather_data";
	// table weather detail name;
	private static final String TABLE_WEATHERDATA_FORECAST = "weather_forecast";
	// table postalCode and userId
	private static final String TABLE_POSTALCODE = "postalCode";
	// table setting
	private static final String TABLE_SETTING = "setting";
	private static final String TABLE_SREACH = "sreach";
	private static final String TABLE_WC = "areaid";
	//   private static final String TABLE_MAINCITY = "maincity";
	// table citys
	//private static final String TABLE_CITYS = "citys";
	private static final String TABLE_CITYS = "CITY_LIST"; //sxd 新浪城市数据�?
	private static final String DB_PATH = "/data/data/com.cooee.widget.samweatherclock/databases/";
	private static final String DB_NAME = "city_db.db";
	private static final String FOREIGN_DB_NAME = "city_list.db";
	
	public enum WEATHER_CONDITION
	{
	}
	
	public static String cur_city;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase CitysDb;
	private SQLiteDatabase ForeignCitysDb;
	
	//private CitysDbHelper citysDbHelper;
	public static class WeatherDataColumns implements BaseColumns
	{
		
		public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/weather" );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/weather";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/weather";
		public static final int MATCH = 101;
	}
	
	public static class WeatherDetailColumns implements BaseColumns
	{
		
		public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/weather/*/detail" );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/weather/*/detail";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/weather/*/detail";
		public static final int MATCH = 102;
	}
	
	public static class PostalCodeColumns implements BaseColumns
	{
		
		public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/postalCode" );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/postalCode";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/postalCode";
		public static final int MATCH = 103;
	}
	
	public static class SettingColumns implements BaseColumns
	{
		
		public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/setting" );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/setting";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/setting";
		public static final int MATCH = 104;
	}
	
	public static class CitysColumns implements BaseColumns
	{
		
		public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/citys" );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/citys";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/citys";
		public static final int MATCH = 105;
	}
	
	private Object lock = new Object();
	
	@Override
	public int delete(
			Uri uri ,
			String selection ,
			String[] selectionArgs )
	{
		synchronized( lock )
		{
			Log.d( TAG , "delete uri=" + uri + ", selection = " + selection );
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			int count = 0;
			switch( uriMatcher.match( uri ) )
			{
				case WeatherDataColumns.MATCH:
				{
					count = db.delete( TABLE_WEATHERDATA , selection , selectionArgs );
					break;
				}
				case WeatherDetailColumns.MATCH:
				{
					count = db.delete( TABLE_WEATHERDATA_FORECAST , selection , selectionArgs );
					break;
				}
				case PostalCodeColumns.MATCH:
				{
					count = db.delete( TABLE_POSTALCODE , selection , selectionArgs );
					break;
				}
				case SettingColumns.MATCH:
				{
					count = db.delete( TABLE_SETTING , selection , selectionArgs );
					break;
				}
				case AreaIdColumns.MATCH:
				{
					count = db.delete( TABLE_WC , selection , selectionArgs );
					break;
				}
				case SreachColumns.MATCH:
				{
					count = db.delete( TABLE_SREACH , selection , selectionArgs );
					break;
				}
				default:
					throw new UnsupportedOperationException();
			}
			Log.v( TAG , "delete count = " + count );
			//db.close();
			return count;
		}
	}
	
	@Override
	public String getType(
			Uri uri )
	{
		switch( uriMatcher.match( uri ) )
		{
			case WeatherDataColumns.MATCH:
				return WeatherDataColumns.CONTENT_ITEM_TYPE;
			case WeatherDetailColumns.MATCH:
				return WeatherDetailColumns.CONTENT_TYPE;
			case PostalCodeColumns.MATCH:
				return PostalCodeColumns.CONTENT_ITEM_TYPE;
			case SettingColumns.MATCH:
			{
				return SettingColumns.CONTENT_ITEM_TYPE;
			}
			case CitysColumns.MATCH:
			{
				return CitysColumns.CONTENT_ITEM_TYPE;
			}
			case AreaIdColumns.MATCH:
			{
				return AreaIdColumns.CONTENT_ITEM_TYPE;
			}
			case SreachColumns.MATCH:
			{
				return SreachColumns.CONTENT_ITEM_TYPE;
			}
		}
		throw new IllegalStateException();
	}
	
	@Override
	public Uri insert(
			Uri uri ,
			ContentValues values )
	{
		synchronized( lock )
		{
			Log.d( TAG , "insert() with uri=" + uri );
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Uri resultUri = null;
			switch( uriMatcher.match( uri ) )
			{
				case WeatherDataColumns.MATCH:
				{
					long rowId = db.insert( TABLE_WEATHERDATA , weatherdataentity.POSTALCODE , values );
					if( rowId != -1 )
					{
						resultUri = ContentUris.withAppendedId( WeatherDataColumns.CONTENT_URI , rowId );
					}
					break;
				}
				case WeatherDetailColumns.MATCH:
				{
					// Insert a forecast into a specific widget
					long rowId = db.insert( TABLE_WEATHERDATA_FORECAST , null , values );
					if( rowId != -1 )
					{
						resultUri = ContentUris.withAppendedId( WeatherDetailColumns.CONTENT_URI , rowId );
					}
					break;
				}
				case PostalCodeColumns.MATCH:
				{
					long rowId = db.insert( TABLE_POSTALCODE , PostalCodeEntity.USER_ID , values );
					if( rowId != -1 )
					{
						resultUri = ContentUris.withAppendedId( PostalCodeColumns.CONTENT_URI , rowId );
					}
					break;
				}
				case SettingColumns.MATCH:
				{
					long rowId = db.insert( TABLE_SETTING , null , values );
					if( rowId != -1 )
					{
						resultUri = ContentUris.withAppendedId( SettingColumns.CONTENT_URI , rowId );
					}
					break;
				}
				case AreaIdColumns.MATCH:
				{
					long rowId = db.insert( TABLE_WC , null , values );
					if( rowId != -1 )
					{
						resultUri = ContentUris.withAppendedId( AreaIdColumns.CONTENT_URI , rowId );
					}
					break;
				}
				case SreachColumns.MATCH:
				{
					long rowId = db.insert( TABLE_SREACH , null , values );
					if( rowId != -1 )
					{
						resultUri = ContentUris.withAppendedId( SreachColumns.CONTENT_URI , rowId );
					}
					break;
				}
				default:
					throw new UnsupportedOperationException();
			}
			// Notify any listeners that the data backing the content provider has
			// changed, and return
			// the number of rows affected.
			getContext().getContentResolver().notifyChange( uri , null );
			//db.close();
			return resultUri;
		}
	}
	
	// �?��数据库是否有�?
	private static boolean checkDataBase(
			String dbName )
	{
		//        SQLiteDatabase checkDB = null;
		String myPath = DB_PATH + dbName;
		File dbFile = new File( myPath );
		if( dbFile != null && dbFile.exists() && !dbFile.isDirectory() )
		{
			return true;
		}
		else
		{
			return false;
		}
		//        try {
		//            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		//        } catch (Exception e) {
		//            Log.v("Database", "Error");
		//            e.printStackTrace();
		//        }
		//        if (checkDB != null&&checkDB.isOpen()) {
		//            checkDB.close();
		//        }
		//        Log.v(TAG, "check citys DataBase checkDB = " + checkDB);
		//        return checkDB != null ? true : false;
	}
	
	private static void copyDatabase(
			Context context ,
			String db_name )
	{
		// 拷贝assets中的数据�?
		String outFileName;
		InputStream myInput;
		Log.v( TAG , "copyDatabase" );
		if( checkDataBase( db_name ) == false )
		{
			try
			{
				myInput = context.getAssets().open( db_name );
				outFileName = DB_PATH + db_name;
				// �?��目录
				File f = new File( DB_PATH );
				if( !f.exists() )
				{
					f.mkdirs();
					Log.v( TAG , "mkdir: " + DB_PATH );
				}
				// �?��文件
				f = new File( outFileName );
				if( !f.exists() )
				{
					f.createNewFile();
					Log.v( TAG , "create new file: " + outFileName );
				}
				OutputStream myOutput = new FileOutputStream( outFileName );
				byte[] buffer = new byte[1024];
				int length;
				while( ( length = myInput.read( buffer ) ) > 0 )
				{
					myOutput.write( buffer , 0 , length );
				}
				myOutput.flush();
				myOutput.close();
				myInput.close();
				Log.v( TAG , "copy complete." );
			}
			catch( IOException e )
			{
				e.printStackTrace();
				Log.v( TAG , "error" );
			}
		}
	}
	
	/*    private static void deleteDatabase(Context context) {
	        String filename = DB_PATH + DB_NAME;
	        File f = new File(filename);
	        if (f.exists()) {
	            f.delete();
	        }
	    }*/
	@Override
	public boolean onCreate()
	{
		dbHelper = new DatabaseHelper( getContext() );
		copyDatabase( getContext() , DB_NAME );
		copyDatabase( getContext() , FOREIGN_DB_NAME );
		// gaominghui@2016/12/22 UPD START
		File file = new File( DB_PATH + DB_NAME );
		if( file.exists() && !file.isDirectory() )
		{
			CitysDb = SQLiteDatabase.openDatabase( DB_PATH + DB_NAME , null , SQLiteDatabase.OPEN_READONLY );
		}
		File foreignFile = new File( DB_PATH + FOREIGN_DB_NAME );
		if( foreignFile.exists() && !foreignFile.isDirectory() )
		{
			ForeignCitysDb = SQLiteDatabase.openDatabase( DB_PATH + FOREIGN_DB_NAME , null , SQLiteDatabase.OPEN_READONLY );
		}
		// gaominghui@2016/12/22 UPD END
		//CitysDb = SQLiteDatabase.openDatabase( DB_PATH + DB_NAME , null , SQLiteDatabase.OPEN_READONLY );
		//ForeignCitysDb = SQLiteDatabase.openDatabase( DB_PATH + FOREIGN_DB_NAME , null , SQLiteDatabase.OPEN_READONLY );
		// citysDbHelper = new CitysDbHelper(getContext());
		return ( dbHelper == null ) ? false : true;
	}
	
	private Cursor queryCitys(
			Uri uri ,
			String[] projection ,
			String selection ,
			String[] selectionArgs ,
			String sortOrder )
	{
		// SQLiteDatabase db = citysDbHelper.getReadableDatabase();
		// �?��数据库是否存�?
		copyDatabase( getContext() , DB_NAME );
		copyDatabase( getContext() , FOREIGN_DB_NAME );
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String limit = null;
		qb.setTables( TABLE_CITYS );
		// 屏蔽三级城市
		// if (weatherwebservice.dataSourceFlag != WeatherDataSource.WEATHER_CN)
		// {
		// selection = selection + " AND " + " NOT " + CitysEntity.NAME +
		// " LIKE " + "'%.%'";
		// }
		if( projection.length <= 1 )
		{
			return qb.query( CitysDb , projection , selection , selectionArgs , null , null , sortOrder , limit );
		}
		else
		{
			return qb.query( ForeignCitysDb , projection , selection , selectionArgs , null , null , sortOrder , limit );
		}
	}
	
	@Override
	public Cursor query(
			Uri uri ,
			String[] projection ,
			String selection ,
			String[] selectionArgs ,
			String sortOrder )
	{
		synchronized( lock )
		{
			// 城市数据�?
			if( uriMatcher.match( uri ) == CitysColumns.MATCH )
			{
				return queryCitys( uri , projection , selection , selectionArgs , sortOrder );
			}
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			/**
			 * 天气公用模块支持 , start
			 */
			switch( uriMatcher.match( uri ) )
			{
				case QueryCityColumns.MATCH:
					System.out.println( "shlt , QueryCityColumns" );
					return db.query(
							" setting , weather_data " ,
							new String[]{ " maincity as nameChina , maincity as nameEnglish , lastUpdateTime as updateTime " } ,
							"city = maincity or postalCode = maincity" ,
							null ,
							null ,
							null ,
							null );
				case QueryCurWeatherColumns.MATCH:
					System.out.println( "shlt , QueryCurWeatherColumns" );
					return db.query(
							" setting , weather_data " ,
							new String[]{ " condition , tempC as curTC, tempH as highTC, tempL as lowTC , (tempC*9.0/5+32) as curFC, (tempH*9.0/5+32) as highFC, (tempL*9.0/5+32) as lowFC " } ,
							"city = maincity or postalCode = maincity" ,
							null ,
							null ,
							null ,
							null );
				case QueryFutureWeatherColumns.MATCH:
					System.out.println( "shlt , QueryFutureWeatherColumns" );
					return db.query(
							" setting , weather_forecast " ,
							new String[]{ " condition , dayOfWeek , hight as highTC, low as lowTC , (hight*9.0/5+32) as highFC, (low*9.0/5+32) as lowFC " } ,
							"city = maincity or postalCode = maincity" ,
							null ,
							null ,
							null ,
							null );
			}
			/**
			 * 天气公用模块支持 , end
			 */
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			String limit = null;
			Log.v( TAG , "query uri = " + uri + ", match = " + uriMatcher.match( uri ) );
			switch( uriMatcher.match( uri ) )
			{
				case WeatherDataColumns.MATCH:
				{
					qb.setTables( TABLE_WEATHERDATA );
					break;
				}
				case WeatherDetailColumns.MATCH:
				{
					// Pick all the forecasts for given widget, sorted by date and
					// importance
					qb.setTables( TABLE_WEATHERDATA_FORECAST );
					sortOrder = BaseColumns._ID + " ASC";
					break;
				}
				case PostalCodeColumns.MATCH:
				{
					qb.setTables( TABLE_POSTALCODE );
					break;
				}
				case SettingColumns.MATCH:
				{
					qb.setTables( TABLE_SETTING );
					break;
				}
				case AreaIdColumns.MATCH:
				{
					qb.setTables( TABLE_WC );
					break;
				}
				case SreachColumns.MATCH:
				{
					qb.setTables( TABLE_SREACH );
					break;
				}
			}
			return qb.query( db , projection , selection , selectionArgs , null , null , sortOrder , limit );
		}
	}
	
	@Override
	public int update(
			Uri uri ,
			ContentValues values ,
			String selection ,
			String[] selectionArgs )
	{
		synchronized( lock )
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			int ret = -1;
			Log.d( TAG , "update() with uri = " + uri );
			switch( uriMatcher.match( uri ) )
			{
				case WeatherDataColumns.MATCH:
				{
					ret = db.update( TABLE_WEATHERDATA , values , selection , selectionArgs );
					break;
				}
				case WeatherDetailColumns.MATCH:
				{
					ret = db.update( TABLE_WEATHERDATA_FORECAST , values , selection , null );
					break;
				}
				case PostalCodeColumns.MATCH:
				{
					ret = db.update( TABLE_POSTALCODE , values , selection , selectionArgs );
					break;
				}
				case SettingColumns.MATCH:
				{
					ret = db.update( TABLE_SETTING , values , selection , selectionArgs );
					break;
				}
				case AreaIdColumns.MATCH:
				{
					ret = db.update( TABLE_WC , values , selection , selectionArgs );
					break;
				}
				case SreachColumns.MATCH:
				{
					ret = db.update( TABLE_SREACH , values , selection , selectionArgs );
					break;
				}
				default:
					throw new UnsupportedOperationException();
			}
			// Notify any listeners that the data backing the content provider has
			// changed, and return
			// the number of rows affected.
			getContext().getContentResolver().notifyChange( uri , null );
			//db.close();
			return ret;
		}
	}
	
	/*    private static class CitysDbHelper extends SQLiteOpenHelper {
	        private static final String DATABASE_NAME = "city_db.db";
	        private static final int DATABASE_VERSION = 6;

	        private final String TAG = weatherdataprovider.TAG + ".CitysDbHelper";

	        private Context mContext;

	        public CitysDbHelper(Context context) {
	            super(context, DATABASE_NAME, null, DATABASE_VERSION);

	            mContext = context;
	        }

	        @Override
	        public void onCreate(SQLiteDatabase db) {
	        }

	        @Override
	        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	            int version = oldVersion;

	            if (version != DATABASE_VERSION) {
	                Log.w(TAG, "onUpgrade oldVersion = " + oldVersion
	                        + ", newVersion = " + newVersion
	                        + " ,DATABASE_VERSION = " + DATABASE_VERSION);

	                deleteDatabase(mContext);

	                copyDatabase(mContext);
	            }
	        }
	    }*/
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		
		private final String TAG = weatherdataprovider.TAG + ".DatabaseHelper";
		private static final String DATABASE_NAME = "forecasts.db";
		private static final int DATABASE_VERSION = 6;
		
		public DatabaseHelper(
				Context context )
		{
			super( context , DATABASE_NAME , null , DATABASE_VERSION );
			Log.v( "TAG" , "DatabaseHelper(context = " + context + ")" );
		}
		
		@Override
		public void onCreate(
				SQLiteDatabase db )
		{
			db.execSQL( "CREATE TABLE " + TABLE_WEATHERDATA + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY," + weatherdataentity.CITY + " TEXT," + weatherdataentity.UPDATE_MILIS + " INTEGER," + weatherdataentity.IS_CONFIGURED + " INTEGER," + weatherdataentity.POSTALCODE + " TEXT," + weatherdataentity.FORECASTDATE + " INTEGER," + weatherdataentity.CONDITION + " TEXT," + weatherdataentity.TEMPF + " INTEGER," + weatherdataentity.TEMPC + " INTEGER," + weatherdataentity.TEMPH + " INTEGER," + weatherdataentity.TEMPL + " INTEGER," + weatherdataentity.HUMIDITY + " TEXT," + weatherdataentity.ICON + " TEXT," + weatherdataentity.WINDCONDITION + " TEXT," + weatherdataentity.LUNARCALENDAR + " TEXT," + weatherdataentity.ULTRAVIOLETRAY + " TEXT," + weatherdataentity.WEATHERTIME + " TEXT," + weatherdataentity.ENTITYID + " TEXT," + weatherdataentity.DEGREETYPE + " TEXT," + weatherdataentity.LOCATIONCODE + " TEXT," + weatherdataentity.TIMEPOSTMARK + " TEXT," + weatherdataentity.LAST_UPDATE_TIME + " INTEGER);" );
			db.execSQL( "CREATE TABLE " + TABLE_WEATHERDATA_FORECAST + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY," + weatherforecastentity.CITY + " TEXT," + weatherforecastentity.POSTALCODE + " TEXT," + weatherforecastentity.DAYOFWEEK + " TEXT," + weatherforecastentity.LOW + " INTEGER," + weatherforecastentity.HIGHT + " INTEGER," + weatherforecastentity.ICON + " TEXT," + weatherforecastentity.CONDITION + " TEXT);" );
			db.execSQL( "CREATE TABLE " + TABLE_POSTALCODE + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY," + PostalCodeEntity.POSTAL_CODE + " TEXT," + PostalCodeEntity.USER_ID + " TEXT," + PostalCodeEntity.ENTITY_ID + " TEXT," + PostalCodeEntity.LONG + " TEXT," + PostalCodeEntity.LAT + " TEXT," + PostalCodeEntity.LOCATION_NAME + " TEXT," + PostalCodeEntity.LOCATION_CODE + " TEXT," + PostalCodeEntity.CITY_NUM + " TEXT," + PostalCodeEntity.AUTO_LOCATE + " TEXT);" );
			db.execSQL( "CREATE TABLE " + TABLE_SETTING + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY," + SettingEntity.UPDATE_WHEN_OPEN + " INTEGER," + SettingEntity.UPDATE_REGULARLY + " INTEGER," + SettingEntity.UPDATE_INTERVAL + " INTEGER," + SettingEntity.MAINCITY + " TEXT," + SettingEntity.SOUND_ENABLE + " INTEGER);" );
			db.execSQL( "CREATE TABLE " + TABLE_WC + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY," + MsnXMLDataField.ENTITY_ID + " TEXT," + MsnXMLDataField.LOCATION_NAME + " TEXT," + MsnXMLDataField.LOCATION_CODE + " TEXT," + MsnXMLDataField.LONG + " TEXT," + MsnXMLDataField.LAT + " TEXT," + MsnXMLDataField.SREACHKEY + " TEXT);" );
			db.execSQL( "CREATE TABLE " + TABLE_SREACH + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY," + MsnXMLDataField.ENTITY_ID + " TEXT," + MsnXMLDataField.LONG + " TEXT," + MsnXMLDataField.LAT + " TEXT," + MsnXMLDataField.LOCATION_NAME + " TEXT," + MsnXMLDataField.LOCATION_CODE + " TEXT," + MsnXMLDataField.DEGREE_TYPE + " TEXT," + MsnXMLDataField.TIMEZONE + " TEXT," + MsnXMLDataField.ZIPCODE + " TEXT," + MsnXMLDataField.ALERT + " TEXT," + MsnXMLDataField.TIMEPOSTMARK + " TEXT," + MsnXMLDataField.WINDSPEED + " TEXT," + MsnXMLDataField.WINDDISPLAY + " TEXT," + MsnXMLDataField.SHORTDAY + " TEXT," + MsnXMLDataField.DAY + " TEXT," + MsnXMLDataField.HUNIDITY + " TEXT," + MsnXMLDataField.FEELSLIKE + " TEXT," + MsnXMLDataField.OBSERVATION_POINT + " TEXT," + MsnXMLDataField.OBSERVATION_TIME + " TEXT," + MsnXMLDataField.DATE + " TEXT," + MsnXMLDataField.SKY_TEXT + " TEXT," + MsnXMLDataField.TEMPERATURE_CURRENT + " TEXT," + MsnXMLDataField.PRECIP + "0 TEXT," + MsnXMLDataField.SKY_TEXT_DAY + "0 TEXT," + MsnXMLDataField.TEMPERATURE_HIGH + "0 TEXT," + MsnXMLDataField.TEMPERATURE_LOW + "0 TEXT," + MsnXMLDataField.PRECIP + "1 TEXT," + MsnXMLDataField.SKY_TEXT_DAY + "1 TEXT," + MsnXMLDataField.TEMPERATURE_HIGH + "1 TEXT," + MsnXMLDataField.TEMPERATURE_LOW + "1 TEXT," + MsnXMLDataField.PRECIP + "2 TEXT," + MsnXMLDataField.SKY_TEXT_DAY + "2 TEXT," + MsnXMLDataField.TEMPERATURE_HIGH + "2 TEXT," + MsnXMLDataField.TEMPERATURE_LOW + "2 TEXT," + MsnXMLDataField.PRECIP + "3 TEXT," + MsnXMLDataField.SKY_TEXT_DAY + "3 TEXT," + MsnXMLDataField.TEMPERATURE_HIGH + "3 TEXT," + MsnXMLDataField.TEMPERATURE_LOW + "3 TEXT," + MsnXMLDataField.PRECIP + "4 TEXT," + MsnXMLDataField.SKY_TEXT_DAY + "4 TEXT," + MsnXMLDataField.TEMPERATURE_HIGH + "4 TEXT," + MsnXMLDataField.TEMPERATURE_LOW + "4 TEXT);" );
		}
		
		@Override
		public void onDowngrade(
				SQLiteDatabase db ,
				int oldVersion ,
				int newVersion )
		{
			int version = oldVersion;
			if( version != DATABASE_VERSION )
			{
				Log.w( TAG , "onDowngrade oldVersion = " + oldVersion + ", newVersion = " + newVersion + " ,DATABASE_VERSION = " + DATABASE_VERSION );
				Log.w( TAG , "Destroying old data during upgrade." );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_WEATHERDATA );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_WEATHERDATA_FORECAST );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_POSTALCODE );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_SETTING );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_WC );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_SREACH );
				onCreate( db );
			}
		}
		
		@Override
		public void onUpgrade(
				SQLiteDatabase db ,
				int oldVersion ,
				int newVersion )
		{
			int version = oldVersion;
			if( version != DATABASE_VERSION )
			{
				Log.w( TAG , "onUpgrade oldVersion = " + oldVersion + ", newVersion = " + newVersion + " ,DATABASE_VERSION = " + DATABASE_VERSION );
				Log.w( TAG , "Destroying old data during upgrade." );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_WEATHERDATA );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_WEATHERDATA_FORECAST );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_POSTALCODE );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_SETTING );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_WC );
				db.execSQL( "DROP TABLE IF EXISTS " + TABLE_SREACH );
				onCreate( db );
			}
		}
	}
	
	/**
	 * Matcher used to filter an incoming {@link Uri}.
	 */
	private static final UriMatcher uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
	static
	{
		uriMatcher.addURI( AUTHORITY , "weather/*" , WeatherDataColumns.MATCH );
		uriMatcher.addURI( AUTHORITY , "weather/*/detail" , WeatherDetailColumns.MATCH );
		uriMatcher.addURI( AUTHORITY , "postalCode" , PostalCodeColumns.MATCH );
		uriMatcher.addURI( AUTHORITY , "setting" , SettingColumns.MATCH );
		uriMatcher.addURI( AUTHORITY , "citys" , CitysColumns.MATCH );
		uriMatcher.addURI( AUTHORITY , "sreach" , SreachColumns.MATCH );
		uriMatcher.addURI( AUTHORITY , "areaid" , AreaIdColumns.MATCH );
		uriMatcher.addURI( AUTHORITY , "querycity" , QueryCityColumns.MATCH );
		uriMatcher.addURI( AUTHORITY , "querycurweather" , QueryCurWeatherColumns.MATCH );
		uriMatcher.addURI( AUTHORITY , "queryfutureweather" , QueryFutureWeatherColumns.MATCH );
	}
	
	public static class AreaIdColumns implements BaseColumns
	{
		
		public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/areaid" );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/areaid";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/areaid";
		public static final int MATCH = 106;
	}
	
	public static class SreachColumns implements BaseColumns
	{
		
		public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/sreach" );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/sreach";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/sreach";
		public static final int MATCH = 107;
	}
	
	/**
	 * 天气公用模块支持 , start
	 */
	public static final String DB_LISTENER_URI = "content://" + AUTHORITY + "/dblisterner";
	public static final String QUERY_CITY_URI = "content://" + AUTHORITY + "/querycity";
	public static final String QUERY_CUR_WEATHER_URI = "content://" + AUTHORITY + "/querycurweather";
	public static final String QUERY_FUTURE_WEATHER_URI = "content://" + AUTHORITY + "/queryfutureweather";
	
	public static class QueryCityColumns implements BaseColumns
	{
		
		public static final Uri CONTENT_URI = Uri.parse( QUERY_CITY_URI );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/querycity";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/querycity";
		public static final int MATCH = 1107;
	}
	
	public static class QueryCurWeatherColumns implements BaseColumns
	{
		
		public static final Uri CONTENT_URI = Uri.parse( QUERY_CUR_WEATHER_URI );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/querycurweather";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/querycurweather";
		public static final int MATCH = 1108;
	}
	
	public static class QueryFutureWeatherColumns implements BaseColumns
	{
		
		public static final Uri CONTENT_URI = Uri.parse( QUERY_FUTURE_WEATHER_URI );
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/queryfutureweather";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/queryfutureweather";
		public static final int MATCH = 1109;
	}
	/**
	 * 天气公用模块支持 , end
	 */
}
