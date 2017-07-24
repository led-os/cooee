package com.cooee.phenix.editmode.provider;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.cooee.framework.utils.ResourceUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.util.Tools;
import com.cooee.wallpaperManager.WallpaperManagerBase;


public class WallpaperUtils
{
	
	/**默认壁纸存放路径为assets/launcher/wallpapers/*/
	public static final String wallpaperPath = "launcher/wallpapers";
	public static final String custom_wallpapers_path = "/system/wallpapers";
	private static final String TAG = "LauncherWallpaperManager";
	/**连接壁纸英文名和中文名的字符串*/
	public static String strLink = "#";
	//	private Context mContext = null;
	private static int mDefaultWallpaperId = -1;
	private static boolean mHasChangeWallpaperDone = true;
	private static BroadcastReceiver mReceiver = null;
	private static Handler mHandler;
	private static void obtainDefaultWallpaperResAndId(
			Context mContext )
	{
		int id = -1;
		//xiatian start	//需求：默认主题壁纸外置（使用包名为“config_custom_default_wallpaper_package_name”的res/drawable中的资源“config_custom_default_wallpaper_resource_name”），若“config_custom_default_wallpaper_package_name”和“config_custom_default_wallpaper_resource_name”其中有一个配置为空，则使用桌面默认主题的壁纸。
		//				id = mThemeDescription.getResourceID( "default_wallpaper" );//xiatian del
		//xiatian add start
		Resources res = null;
		String custom_default_wallpaper_package_name = LauncherDefaultConfig.getString( R.string.config_custom_default_wallpaper_package_name );
		String custom_default_wallpaper_resource_name = LauncherDefaultConfig.getString( R.string.config_custom_default_wallpaper_resource_name );
		if( !( TextUtils.isEmpty( custom_default_wallpaper_package_name ) || ( TextUtils.isEmpty( custom_default_wallpaper_resource_name ) ) ) )
		{
			Context slaveContext = null;
			try
			{
				slaveContext = mContext.createPackageContext( custom_default_wallpaper_package_name , Context.CONTEXT_IGNORE_SECURITY );
			}
			catch( NameNotFoundException e )
			{
				e.printStackTrace();
			}
			if( slaveContext != null )
			{
				res = slaveContext.getResources();
				id = ResourceUtils.getDrawableResourceIdByReflectIfNecessary( 0 , res , custom_default_wallpaper_package_name , custom_default_wallpaper_resource_name );
			}
		}
		else
		{
			res = mContext.getResources();
			id = res.getIdentifier( "default_wallpaper" , "drawable" , mContext.getPackageName() );
		}
		mDefaultWallpaperId = id;
		if( mReceiver == null )
		{
			mReceiver = new BroadcastReceiver() {
				
				@Override
				public void onReceive(
						Context context ,
						Intent intent )
				{
					// TODO Auto-generated method stub
					if( mHandler != null )
					{
						mHandler.postDelayed( new Runnable() {//延时，防止频繁点击
							
							
							public void run()
							{
								mHasChangeWallpaperDone = true;
							}
						} , 1500 );
					}
					else
					{
						
						mHasChangeWallpaperDone = true;
					}
				}
			};
			IntentFilter filter = new IntentFilter();
			filter.addAction( Intent.ACTION_WALLPAPER_CHANGED );
			mContext.registerReceiver( mReceiver , filter );
		}
	}
	
	/**
	 * 加载壁纸信息存放列表中
	 * @author tangliang 2015-1-21
	 */
	public static ArrayList<WallPaperFile> loadWallpaperInfo(
			Context mContext )
	{
		ArrayList<WallPaperFile> mWallPapers = new ArrayList<WallPaperFile>( 24 );
		String[] wallpapers = null;
		//1.加载apk大壁纸:从apk的res中读壁纸  现有的壁纸名称方案不适用于apk壁纸
		obtainDefaultWallpaperResAndId( mContext );
		if( mDefaultWallpaperId != 0 )
		{
			WallPaperFile file = new WallPaperFile( mDefaultWallpaperId );
			mWallPapers.add( file );
		}
		//2.加载本地大壁纸:本地大壁纸名称格式设计成00com.coco.wallpaper.英文名#中文名.jpg 其中00表示两位数字用于壁纸排序
		File dir = new File( custom_wallpapers_path );
		try
		{
			//2.1从指定目录读取
			if( dir.exists() && dir.isDirectory() )
			{
				wallpapers = dir.list();
				for( String name : wallpapers )
				{
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸 tangliang 2015-1-23 ADD START
					File filetemp = new File( custom_wallpapers_path + "/" + name.substring( 0 , name.length() - ".jpg".length() ) + "_small.jpg" );
					if( filetemp.exists() && !name.substring( 0 , name.length() - ".jpg".length() ).endsWith( "_small" ) )
					{
						WallPaperFile file = new WallPaperFile( name , WallPaperFileFromEnum.custom );
						mWallPapers.add( file );
					}
					filetemp = new File( custom_wallpapers_path + "/" + name.substring( 0 , name.length() - ".png".length() ) + "_small.png" );
					if( filetemp.exists() && !name.substring( 0 , name.length() - ".png".length() ).endsWith( "_small" ) )
					{
						WallPaperFile file = new WallPaperFile( name , WallPaperFileFromEnum.custom , ".png" );
						mWallPapers.add( file );
					}
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸 tangliang 2015-1-23 ADD END
				}
			}
			//2.2从assets中指定目录读取
			else
			{
				AssetManager assManager = mContext.getResources().getAssets();
				wallpapers = assManager.list( wallpaperPath );
				for( String name : wallpapers )
				{
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸 tangliang 2015-1-23 ADD START
					if( name.endsWith( ".jpg" ) && !name.substring( 0 , name.length() - ".jpg".length() ).endsWith( "_small" ) )
					{
						WallPaperFile file = new WallPaperFile( name , WallPaperFileFromEnum.assets );
						mWallPapers.add( file );
					}
					else if( name.endsWith( ".png" ) && !name.substring( 0 , name.length() - ".png".length() ).endsWith( "_small" ) )
					{
						WallPaperFile file = new WallPaperFile( name , WallPaperFileFromEnum.assets , ".png" );
						mWallPapers.add( file );
					}
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸 tangliang 2015-1-23 ADD END
				}
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		//<i_0006868> liuhailin@2014-09-10 modify begin
		//3.加载下载大壁纸 下载大壁纸名称格式为com.coco.wallpaper.英文名#中文名.jpg
		try
		{
			File file = new File( getWallpaperDir() );
			String downloadWallpapers[] = file.list();
			List<WallPaperFile> result = new ArrayList<WallPaperFile>();
			if( downloadWallpapers != null )
			{
				for( String item : downloadWallpapers )
				{
					File filetemp = new File( getWallpaperDir() + "/" + item.substring( 0 , item.length() - ".jpg".length() ) + "_small.jpg" );
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸  tangliang 2015-1-23 ADD START
					if( filetemp.exists() && !item.substring( 0 , item.length() - ".jpg".length() ).endsWith( "_small" ) && !item.contains( ".apk" ) )
					{
						WallPaperFile wFile = new WallPaperFile( item , WallPaperFileFromEnum.download );
						result.add( wFile );
					}
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸  tangliang 2015-1-23 ADD END
					filetemp = new File( getWallpaperDir() + "/" + item.substring( 0 , item.length() - ".png".length() ) + "_small.png" );
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸  tangliang 2015-1-23 ADD START
					if( filetemp.exists() && !item.substring( 0 , item.length() - ".png".length() ).endsWith( "_small" ) && !item.contains( ".apk" ) )
					{
						WallPaperFile wFile = new WallPaperFile( item , WallPaperFileFromEnum.download , ".png" );
						result.add( wFile );
					}
				}
			}
			mWallPapers.addAll( result );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			Log.d( TAG , "get Download wallpaper error :" + e.getMessage() );
		}
		//		Collections.sort( mWallPapers , new ByStringValue() );
		//<i_0006868> liuhailin@2014-09-10 modify end
		return mWallPapers;
	}
	
	public static Bitmap getSmallWallpaper(
			Context mContext ,
			WallPaperFile file ,
			int width ,
			int height )
	{
		InputStream smallInputStream = null;
		AssetManager asset = mContext.getResources().getAssets();
		try
		{
			//1.缓存assets壁纸
			if( file.fileFrom == WallPaperFileFromEnum.assets )
			{
				//修改在assert中查找壁纸的逻辑，当没有small缩略图的时候，生成一张缩略图
				File smallfile = new File( wallpaperPath + "/" + file.smallFileName );
				if( smallfile.exists() )
				{
					smallInputStream = asset.open( wallpaperPath + "/" + file.smallFileName );
					return Tools.inputStream2Bitmap( smallInputStream , width , height );
				}
			}
			//2.缓存默认壁纸
			else if( file.fileFrom == WallPaperFileFromEnum.custom )
			{
				try
				{
					smallInputStream = new FileInputStream( custom_wallpapers_path + "/" + file.smallFileName );
					return Tools.inputStream2Bitmap( smallInputStream , width , height );
				}
				catch( FileNotFoundException e )
				{
					e.printStackTrace();
				}
			}
			//3.加载apk壁纸
			else if( file.fileFrom == WallPaperFileFromEnum.otherapk )
			{
				Context remountContext = mContext.createPackageContext( mContext.getPackageName() , Context.CONTEXT_IGNORE_SECURITY );
				Resources res = remountContext.getResources();
				Drawable drawable = res.getDrawable( file.fileDrawable );//bitmapdrawable中的bitmap不能释放，res.getDrawable这个方法每次得到的drawable对象不一样，但是((BitmapDrawable)drawable).getBitmap()是同一个值
				Bitmap bitmap = Tools.drawableToBitmap( drawable , width , height );
				if( bitmap != null && !bitmap.isRecycled() )
				{
					return bitmap;
				}
			}
			//4.缓存下载壁纸
			else if( file.fileFrom == WallPaperFileFromEnum.download )
			{
				try
				{
					smallInputStream = new FileInputStream( Environment.getExternalStorageDirectory().getAbsolutePath() + "/Coco/Wallpaper/App" + "/" + file.smallFileName );
					return Tools.inputStream2Bitmap( smallInputStream , width , height );
				}
				catch( FileNotFoundException e )
				{
					e.printStackTrace();
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if( smallInputStream != null )
			{
				try
				{
					smallInputStream.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static class WallPaperFile
	{
		
		/**壁纸文件名*/
		private String fileName;
		/**壁纸缩略图文件名*/
		private String smallFileName;
		/**壁纸来源*/
		private WallPaperFileFromEnum fileFrom;
		/**apk壁纸资源ID*/
		private int fileDrawable;
		
		public WallPaperFile(
				String fileName ,
				WallPaperFileFromEnum from )
		{
			this.fileName = fileName;
			this.smallFileName = fileName.substring( 0 , fileName.length() - ".jpg".length() ) + "_small.jpg";
			this.fileFrom = from;
		}
		
		public WallPaperFile(
				String fileName ,
				WallPaperFileFromEnum from ,
				String postfix )
		{
			this.fileName = fileName;
			this.smallFileName = fileName.substring( 0 , fileName.length() - postfix.length() ) + "_small" + postfix;
			this.fileFrom = from;
		}
		
		public WallPaperFile(
				int drawable )
		{
			this.fileDrawable = drawable;
			this.fileFrom = WallPaperFileFromEnum.otherapk;
		}
		
		public String getFileName()
		{
			return fileName;
		}
		
		public String getSmallFileName()
		{
			return smallFileName;
		}
	}
	
	public static class WallPaperFileBitmap
	{
		
		Bitmap fileNameBitmap;
		Bitmap smallFileNameBitmap;
		
		public WallPaperFileBitmap(
				Bitmap fileNameBitmap ,
				Bitmap smallFileNameBitmap )
		{
			this.fileNameBitmap = fileNameBitmap;
			this.smallFileNameBitmap = smallFileNameBitmap;
		}
		
		public Bitmap getFileNameBitmap()
		{
			return fileNameBitmap;
		}
		
		public void setFileNameBitmap(
				Bitmap fileNameBitmap )
		{
			this.fileNameBitmap = fileNameBitmap;
		}
		
		public Bitmap getSmallFileNameBitmap()
		{
			return smallFileNameBitmap;
		}
		
		public void setSmallFileNameBitmap(
				Bitmap smallFileNameBitmap )
		{
			this.smallFileNameBitmap = smallFileNameBitmap;
		}
		
		public void clearAllBitmap()
		{
			if( fileNameBitmap != null && !fileNameBitmap.isRecycled() )
			{
				fileNameBitmap.recycle();
				fileNameBitmap = null;
			}
			if( smallFileNameBitmap != null && !smallFileNameBitmap.isRecycled() )
			{
				smallFileNameBitmap.recycle();
				smallFileNameBitmap = null;
			}
		}
	}
	
	/**描述壁纸来源*/
	public enum WallPaperFileFromEnum
	{
		assets , custom , otherapk , download , sdmod
	}
	
	public static void setWallpaper(
			final Context context ,
			final WallPaperFile file )
	{
		if( mHandler == null )
			mHandler = new Handler();
		if( mHasChangeWallpaperDone )
		{
			AsyncTask.execute( new Runnable() {
				
				
				@Override
				public void run()
				{
					mHasChangeWallpaperDone = false;
					InputStream stream = null;
					InputStream newStream = null;
					AssetManager asset = context.getResources().getAssets();
					try
					{
						if( file.fileFrom == WallPaperFileFromEnum.assets )
						{
							stream = asset.open( wallpaperPath + "/" + file.fileName );
							newStream = asset.open( wallpaperPath + "/" + file.fileName );
						}
						else if( file.fileFrom == WallPaperFileFromEnum.custom )
						{
							try
							{
								stream = new FileInputStream( custom_wallpapers_path + "/" + file.fileName );
								newStream = new FileInputStream( custom_wallpapers_path + "/" + file.fileName );
							}
							catch( FileNotFoundException e )
							{
								e.printStackTrace();
								return;
							}
						}
						else if( file.fileFrom == WallPaperFileFromEnum.otherapk )
						{
							stream = context.getResources().openRawResource( file.fileDrawable );
							newStream = context.getResources().openRawResource( file.fileDrawable );
						}
						else if( file.fileFrom == WallPaperFileFromEnum.download )
						{
							try
							{
								stream = new FileInputStream( getWallpaperDir() + "/" + file.fileName );
								newStream = new FileInputStream( getWallpaperDir() + "/" + file.fileName );
							}
							catch( FileNotFoundException e )
							{
								e.printStackTrace();
								return;
							}
						}
						if( stream != null && newStream != null )
						{
							WallpaperManagerBase.getInstance( context ).setWallpaperAndDimension( stream , newStream );
							WallpaperManager.getInstance( context ).forgetLoadedWallpaper();
						}
					}
					catch( Exception e )
					{
						Log.e( TAG , "Failed to set wallpaper: " + e );
						return;
					}
					finally
					{
						if( stream != null )
						{
							try
							{
								stream.close();
								stream = null;
							}
							catch( IOException e )
							{
								e.printStackTrace();
							}
						}
						if( newStream != null )
						{
							try
							{
								newStream.close();
								newStream = null;
							}
							catch( IOException e )
							{
								e.printStackTrace();
							}
						}

					}

				}
			} );
		}

	}
	
	public static String getWallpaperDir()
	{
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Coco/Wallpaper/App";
	}
}
