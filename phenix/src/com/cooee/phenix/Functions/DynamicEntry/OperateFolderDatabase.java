package com.cooee.phenix.Functions.DynamicEntry;


import java.net.URISyntaxException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherModel;
import com.cooee.phenix.LauncherProvider.DatabaseHelper;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.LauncherSettings.Favorites;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.ShortcutInfo;


public class OperateFolderDatabase
{
	
	public ArrayList<ItemInfo> getmFolderInfos()
	{
		return mFolderInfos;
	}
	
	public static final String OPERATE_FAVORITE = "operate_folder";
	public static final String TEMP_FAVORITE = "temp_favorites";//智能分类时，favorite表保存到的那张表的表名
	private static SQLiteDatabase db = null;
	private static long mMaxItemId = OperateDynamicMain.OPERATE_MINID;
	private ArrayList<ItemInfo> mFolderInfos = new ArrayList<ItemInfo>();
	private OperateDynamicMain mOperateDynamicMain = null;
	
	public OperateFolderDatabase(
			SQLiteDatabase db ,
			OperateDynamicMain operateDynamicMain )
	{
		this.db = db;
		mOperateDynamicMain = operateDynamicMain;
		mMaxItemId = initializeMaxItemId();
	}
	
	private static long generateNewItemId()
	{
		mMaxItemId += 1;
		return mMaxItemId;
	}
	
	private long initializeMaxItemId()
	{
		//<phenix modify> liuhailin@2015-01-22 del begin
		//Cursor c = db.rawQuery( "SELECT MAX(_id) FROM favorites" , null );
		Cursor c = db.rawQuery( StringUtils.concat( "SELECT MAX(_id) FROM " , OPERATE_FAVORITE ) , null );
		//<phenix modify> liuhailin@2015-01-22 del end
		// get the result
		final int maxIdIndex = 0;
		long id = -1;
		if( c != null && c.moveToNext() )
		{
			id = c.getLong( maxIdIndex );
		}
		if( c != null )
		{
			c.close();
		}
		if( id == -1 )
		{
			throw new RuntimeException( "Error: could not query max item id" );
		}
		return id;
	}
	
	private static void addItemToOperateFolderDatabase(
			Context context ,
			final SQLiteDatabase db ,
			final ItemInfo item )
	{
		final ContentValues values = new ContentValues();
		item.onAddToDatabase( values );
		long id = generateNewItemId();
		item.setId( id );
		values.put( LauncherSettings.Favorites._ID , id );
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				//tempdb.beginTransaction();
				db.insert( OPERATE_FAVORITE , null , values );
				//tempdb.endTransaction();
			}
		} );
	}
	
	/**
	 * 更新文件夹移动后的数据
	 * @return
	 */
	public void updateOperateFolderDB(
			ItemInfo item )
	{
		final ContentValues values = new ContentValues();
		item.onAddToDatabase( values );
		final String id = String.valueOf( item.getId() );
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				db.update( OPERATE_FAVORITE , values , StringUtils.concat( LauncherSettings.Favorites._ID , "=?" ) , new String[]{ id } );
			}
		} );
	}
	
	/**
	 * 判断是否存在运营文件夹
	 * @return
	 */
	public boolean isExitOperateFolderDB()
	{
		Cursor c = db.query( OPERATE_FAVORITE , null , null , null , null , null , null );
		if( c != null && c.moveToNext() )
		{
			c.close();
			return true;
		}
		return false;
	}
	
	/**
	 * 删除该表中所有的数据
	 */
	public void clearFolderAllData()
	{
		mMaxItemId = OperateDynamicMain.OPERATE_MINID;
		mFolderInfos.clear();
		deleteAllDataBase();
	}
	
	public void deleteAllDataBase()
	{
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				db.delete( OPERATE_FAVORITE , null , null );
			}
		} );
	}
	
	/**
	 * 删除单独某一条记录
	 * @param _id
	 */
	public static void delete(
			final ItemInfo itemInfo )
	{
		//		final String title = shortcutInfo.getTitle();
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				String[] updateTables = new String[]{ OPERATE_FAVORITE , TEMP_FAVORITE , DatabaseHelper.getFavoritesTabName() };
				for( String table : updateTables )
				{
					long ids = 0;
					if( itemInfo instanceof ShortcutInfo )
					{
						ShortcutInfo shortcutInfo = (ShortcutInfo)itemInfo;
						ids = findIdByIntent( table , shortcutInfo.getIntent() );//查找对应的id，zhujieping
					}
					else if( itemInfo instanceof FolderInfo )
					{
						FolderInfo folderInfo = (FolderInfo)itemInfo;
						ids = findIdByTitle( table , folderInfo.getTitleString() );
					}
					if( ids > 0 )
						db.delete( table , StringUtils.concat( LauncherSettings.Favorites._ID , "=?" ) , new String[]{ String.valueOf( ids ) } );
				}
			}
		} );
	}
	
	private static long findIdByTitle(
			String tab ,
			String title )
	{
		try
		{
			Cursor c = db.query( tab , null , StringUtils.concat( Favorites.ITEM_TYPE , " = ?" ) , new String[]{ String.valueOf( Favorites.ITEM_TYPE_FOLDER ) } , null , null , null );
			if( c != null )
			{
				int idIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites._ID );
				int titleIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.TITLE );
				while( c.moveToNext() )
				{
					String title2 = c.getString( titleIndex );
					if( title2.equals( title ) )
					{
						long id = c.getLong( idIndex );
						return id;
					}
				}
				c.close();
			}
		}
		catch( Exception e )
		{
		}
		return 0;
	}
	
	/**
	 * 通过intent中的包名将运营文件夹中的数据读取出来，因为此时运营文件夹的id和launcher中favorite的id是不一样的
	 * @return
	 */
	public static long findIdByIntent(
			String tab ,
			Intent intent )
	{
		if( intent != null )
		{
			String pkgName = intent.getStringExtra( OperateDynamicMain.PKGNAME_ID );
			Object[] obj = findIdByPackageName( tab , pkgName );
			if( obj != null && obj.length == 2 )
			{
				return (Long)obj[0];
			}
		}
		return 0;
	}
	
	public static Object[] findIdByPackageName(
			String tab ,
			String pkgName )
	{
		if( pkgName != null )
		{
			try
			{
				Cursor c = db.query( tab , null , StringUtils.concat( Favorites.ITEM_TYPE , " = ?" ) , new String[]{ String.valueOf( Favorites.ITEM_TYPE_SHORTCUT ) } , null , null , null );
				int intentIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.INTENT );
				int idIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites._ID );
				if( c != null )
				{
					while( c.moveToNext() )
					{
						String intentString = c.getString( intentIndex );
						try
						{
							Intent intent2 = Intent.parseUri( intentString , 0 );
							String pkgName2 = intent2.getStringExtra( OperateDynamicMain.PKGNAME_ID );
							if( pkgName.equals( pkgName2 ) )
							{
								long id = c.getLong( idIndex );
								return new Object[]{ id , intentString };
							}
						}
						catch( URISyntaxException e )
						{
							e.printStackTrace();
						}
					}
					c.close();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 通过数据库中的数据将文件夹数据和图标数据都读取出来
	 */
	public void getFolderInfoAndShourtInfoByDb()
	{
		Cursor c = db.rawQuery( StringUtils.concat( "select * from " , OPERATE_FAVORITE ) , null );
		mFolderInfos.clear();
		ArrayList<String> listPkg = new ArrayList<String>();
		if( c != null )
		{
			final int idIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites._ID );
			final int intentIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.INTENT );
			final int titleIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.TITLE );
			final int iconIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ICON );
			final int containerIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CONTAINER );
			final int itemTypeIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.ITEM_TYPE );
			final int screenIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.SCREEN );
			final int cellXIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CELLX );
			final int cellYIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.CELLY );
			final int operateIntent = c.getColumnIndexOrThrow( LauncherSettings.Favorites.OPERATE_INTENT );
			long id;
			while( c.moveToNext() )
			{
				int itemType = c.getInt( itemTypeIndex );
				id = c.getLong( idIndex );
				ItemInfo item = null;
				switch( itemType )
				{
					case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
					case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
						item = getShortcutInfo( c , iconIndex , titleIndex , intentIndex , containerIndex );
						if( item instanceof ShortcutInfo )
						{
							ShortcutInfo shortcutInfo = (ShortcutInfo)item;
							if( shortcutInfo.getIntent() != null )
							{
								String pkgName = shortcutInfo.getIntent().getStringExtra( OperateDynamicMain.PKGNAME_ID );
								if( pkgName == null || LauncherAppState.isApkInstalled( pkgName ) || listPkg.indexOf( pkgName ) != -1 )
								{
									delete( shortcutInfo );
									continue;
								}
								listPkg.add( pkgName );
								if( item.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP )
								{
									mFolderInfos.add( item );
								}
								else
								{
									addShortCutInfoToFolderInfo( shortcutInfo );
								}
							}
						}
						break;
					case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
						item = getFolderInfo( c , titleIndex , containerIndex , id , itemType , screenIndex , cellXIndex , cellYIndex , operateIntent );
						mFolderInfos.add( (FolderInfo)item );
						break;
				}
			}
			c.close();
		}
	}
	
	private void addShortCutInfoToFolderInfo(
			ShortcutInfo item )
	{
		for( int i = 0 ; i < mFolderInfos.size() ; i++ )
		{
			if( mFolderInfos.get( i ).getId() == item.getContainer() )
			{
				( (FolderInfo)( mFolderInfos.get( i ) ) ).add( item );
				return;
			}
		}
	}
	
	private FolderInfo getFolderInfo(
			Cursor c ,
			int titleIndex ,
			int containerIndex ,
			long id ,
			int itemType ,
			int screenIndex ,
			int cellXIndex ,
			int cellYIndex ,
			int operateIntentIndex )
	{
		new FolderInfo();
		String title = c.getString( titleIndex );
		long container = c.getLong( containerIndex );
		long screenId = c.getLong( screenIndex );
		int cellX = c.getInt( cellXIndex );
		int cellY = c.getInt( cellYIndex );
		String operateIntentString = c.getString( operateIntentIndex );
		Intent opIntent = null;
		try
		{
			opIntent = Intent.parseUri( operateIntentString , 0 );
		}
		catch( URISyntaxException e )
		{
			e.printStackTrace();
		}
		FolderInfo folderInfo = OperateDynamicMain.createFolderInfo( container , itemType , title , screenId , cellX , cellY , opIntent );
		folderInfo.setId( id );
		folderInfo.setScreenId( screenId );
		folderInfo.setCellX( cellX );
		folderInfo.setCellY( cellY );
		return folderInfo;
	}
	
	private ShortcutInfo getShortcutInfo(
			Cursor c ,
			int iconIndex ,
			int titleIndex ,
			int intentIndex ,
			int containerIndex )
	{
		byte[] bytes = c.getBlob( iconIndex );
		String title = c.getString( titleIndex );
		Intent intent = null;
		try
		{
			intent = Intent.parseUri( c.getString( intentIndex ) , 0 );
		}
		catch( URISyntaxException e )
		{
			e.printStackTrace();
		}
		Bitmap bitmap = null;
		if( intent != null )
		{
			String pkgKey = intent.getStringExtra( OperateDynamicMain.PKGNAME_ID );
			bitmap = mOperateDynamicMain.getAllIconBitmap().get( pkgKey );
			if( bitmap == null || bitmap.isRecycled() )
			{
				bitmap = BitmapFactory.decodeByteArray( bytes , 0 , bytes.length , null );
				mOperateDynamicMain.getAllIconBitmap().put( pkgKey , bitmap );
			}
		}
		long container = c.getLong( containerIndex );
		ShortcutInfo shortcutInfo = OperateDynamicMain.createShortcutInfo( bitmap , title , intent , container );
		return shortcutInfo;
	}
	
	/**
	 * 查询运营文件夹中数据库中的version值
	 * @return
	 */
	public String findOperateFolderVersion()
	{
		String version = null;
		Cursor c = db.query( OPERATE_FAVORITE , null , null , null , null , null , null );
		if( c != null )
		{
			while( c.moveToNext() )
			{
				final int operateIntentIndex = c.getColumnIndexOrThrow( LauncherSettings.Favorites.OPERATE_INTENT );
				String operateIntentString = c.getString( operateIntentIndex );
				if( operateIntentString != null )
				{
					try
					{
						Intent operateIntent = Intent.parseUri( operateIntentString , 0 );
						version = operateIntent.getStringExtra( OperateDynamicMain.FOLDER_VERSION );
						if( version != null )
						{
							if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
								Log.v( "" , StringUtils.concat( "version findOperateFolderVersion " , version ) );
							return version;
						}
					}
					catch( URISyntaxException e )
					{
						e.printStackTrace();
					}
				}
			}
			c.close();
		}
		return version;
	}
	
	public static void addFolderInfosToDatabase(
			Context mContext ,
			SQLiteDatabase db ,
			ArrayList<ItemInfo> allFolderInfos )
	{
		for( int i = 0 ; i < allFolderInfos.size() ; i++ )
		{
			if( allFolderInfos.get( i ) instanceof FolderInfo )
			{
				FolderInfo folderInfo = (FolderInfo)allFolderInfos.get( i );
				addItemToOperateFolderDatabase( mContext , db , folderInfo );
				for( int j = 0 ; j < folderInfo.getContents().size() ; j++ )
				{
					ShortcutInfo shortcutInfo = folderInfo.getContents().get( j );
					Intent it = shortcutInfo.getIntent();
					shortcutInfo.setContainer( folderInfo.getId() );
					if( it != null && it.getStringExtra( OperateDynamicMain.PKGNAME_ID ) != null )
					{
						addItemToOperateFolderDatabase( mContext , db , shortcutInfo );
					}
				}
			}
			else if( allFolderInfos.get( i ) instanceof ShortcutInfo )
			{
				ShortcutInfo shortcutInfo = (ShortcutInfo)allFolderInfos.get( i );
				Intent it = shortcutInfo.getIntent();
				if( it != null && it.getStringExtra( OperateDynamicMain.PKGNAME_ID ) != null )
					addItemToOperateFolderDatabase( mContext , db , shortcutInfo );
			}
		}
	}
	
	/**
	 * 将文件夹中的数据都加载launcher的favorite的数据库中
	 * @param mContext
	 * @param allFolderInfos
	 */
	public static void addItemToFavoriteDatabase(
			Context mContext ,
			ItemInfo info )
	{
		if( info instanceof FolderInfo )
		{
			FolderInfo folderInfo = (FolderInfo)info;
			LauncherModel.addItemToDatabase( mContext , folderInfo , folderInfo.getContainer() , folderInfo.getScreenId() , folderInfo.getCellX() , folderInfo.getCellY() , false );
			for( int j = 0 ; j < folderInfo.getContents().size() ; j++ )
			{
				ShortcutInfo shortcutInfo = folderInfo.getContents().get( j );
				LauncherModel.addItemToDatabase( mContext , shortcutInfo , folderInfo.getId() , shortcutInfo.getScreenId() , shortcutInfo.getCellX() , shortcutInfo.getCellY() , false );
				//xiatian add start	//将运营文件夹的item视为第三方图标，加背板、蒙版和盖板。
				//在写入数据库之后，修改图片
				shortcutInfo.setIcon( Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( shortcutInfo.getIcon() , LauncherAppState.getInstance().getContext() , true ) );
				//xiatian add end
			}
		}
		else if( info instanceof ShortcutInfo )
		{
			LauncherModel.addItemToDatabase( mContext , info , info.getContainer() , info.getScreenId() , info.getCellX() , info.getCellY() , false );
			//xiatian add start	//将运营文件夹的item视为第三方图标，加背板、蒙版和盖板。
			//在写入数据库之后，修改图片
			ShortcutInfo shortcutInfo = (ShortcutInfo)info;
			shortcutInfo.setIcon( Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( shortcutInfo.getIcon() , LauncherAppState.getInstance().getContext() , true ) );
			//xiatian add end
		}
	}
	
	public int findOperateFolderAllShortCutSize()
	{
		int size = 0;
		Cursor c = db.query( OPERATE_FAVORITE , null , StringUtils.concat( Favorites.ITEM_TYPE , " = ?" ) , new String[]{ String.valueOf( Favorites.ITEM_TYPE_SHORTCUT ) } , null , null , null );
		if( c != null )
		{
			size = c.getCount();
			c.close();
		}
		return size;
	}
	
	/**
	 * 更新运营Icon运营文件夹中的Intent的数据库
	 * @param shortcutInfo
	 * @param container 
	 */
	public void updateIconIntentDataBase(
			final long id ,
			final String table ,
			ItemInfo itemInfo )
	{
		if( id <= 0 )
		{
			return;
		}
		final ContentValues values = new ContentValues();
		if( itemInfo instanceof ShortcutInfo )
		{
			values.put( LauncherSettings.Favorites.INTENT , ( (ShortcutInfo)itemInfo ).getIntent().toUri( 0 ) );
			//			ShortcutInfo.writeBitmap( values , ( (ShortcutInfo)itemInfo ).getIcon() );//不更新图片，运营的数据库中保存的是原始图片
		}
		else if( itemInfo instanceof FolderInfo )
		{
			values.put( LauncherSettings.Favorites.OPERATE_INTENT , ( (FolderInfo)itemInfo ).getOperateIntent().toUri( 0 ) );
		}
		db.update( table , values , StringUtils.concat( LauncherSettings.Favorites._ID , "=?" ) , new String[]{ String.valueOf( id ) } );
	}
	
	public void updateIconIntentDataBase(
			final String table ,
			final ItemInfo itemInfo )
	{
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				long id = -1;
				if( itemInfo instanceof ShortcutInfo )
				{
					ShortcutInfo shortcutInfo = (ShortcutInfo)itemInfo;
					id = findIdByIntent( table , shortcutInfo.getIntent() );//查找对应的id，zhujieping
				}
				else if( itemInfo instanceof FolderInfo )
				{
					FolderInfo folderInfo = (FolderInfo)itemInfo;
					id = findIdByTitle( table , folderInfo.getTitleString() );
				}
				updateIconIntentDataBase( id , table , itemInfo );
			}
		} );
	}
	
	public void updateIconIntentDataBase(
			ItemInfo info )
	{
		String[] updateTables = new String[]{ OPERATE_FAVORITE , TEMP_FAVORITE , DatabaseHelper.getFavoritesTabName() };
		for( String table : updateTables )
		{
			updateIconIntentDataBase( table , info );
		}
	}
	
	public void updateStateDataBase(
			final String pkgName ,
			final int state )
	{
		AsyncTask.execute( new Runnable() {
			
			@Override
			public void run()
			{
				String[] updateTables = new String[]{ OPERATE_FAVORITE , TEMP_FAVORITE , "favorites_drawer" , "favorites_core" };
				for( String table : updateTables )
				{
					Object[] obj = findIdByPackageName( table , pkgName );//查找对应的id，zhujieping
					if( obj != null && obj.length == 2 )
					{
						long id = (Long)obj[0];
						if( id <= 0 )
						{
							continue;
						}
						try
						{
							Intent intent = Intent.parseUri( (String)obj[1] , 0 );
							intent.putExtra( OperateDynamicMain.SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY , state );
							final ContentValues values = new ContentValues();
							values.put( LauncherSettings.Favorites.INTENT , intent.toUri( 0 ) );
							db.update( table , values , StringUtils.concat( LauncherSettings.Favorites._ID , "=?" ) , new String[]{ String.valueOf( id ) } );
						}
						catch( URISyntaxException e )
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		} );
	}
}
