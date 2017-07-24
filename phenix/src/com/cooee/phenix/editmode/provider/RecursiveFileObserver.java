package com.cooee.phenix.editmode.provider;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Environment;
import android.os.FileObserver;


/**
 * 文件观察器，用于监视Coco/Wallpaper/App目录和目录下文件的删除事件
 * @author qinxu
 *
 */
public class RecursiveFileObserver
{
	
	public static final int CHANGES_ONLY = FileObserver.CREATE | FileObserver.DELETE | FileObserver.DELETE_SELF | FileObserver.MODIFY | FileObserver.MOVE_SELF | FileObserver.MOVED_FROM | FileObserver.MOVED_TO;
	//监听目录的数组
	private String[] mMonitorDirArray;
	//监听目录的路径
	private Map<String , String> mMonitorDirPath;
	private Map<String , SingleFileObserver> mObservers;
	//需要监听的路径
	String mPath;
	int mMask;
	//延时响应，如果有多个图片被删除，通过延时响应来保证只有一次重新更新
	private final long delay = 500;
	//计时器
	private Timer timer = null;
	private String mRootPath;
	private FileUpdateListener iFileUpdateListener = null;
	
	/**
	 * path为要监控的路径，前后不要带‘/’
	 */
	public RecursiveFileObserver(
			String path )
	{
		this( path , CHANGES_ONLY );
	}
	
	/**
	 * path为要监控的路径，前后不要带‘/’
	 */
	public RecursiveFileObserver(
			String path ,
			int mask )
	{
		mPath = path;
		mMask = mask;
		mMonitorDirPath = new HashMap<String , String>();
		mObservers = new HashMap<String , SingleFileObserver>();
		mRootPath = getExternalPath();
		String tempStr = "root" + File.separator + mPath;
		mMonitorDirArray = tempStr.trim().split( "/" );
		monitorDirPath();
		getNewFolder();
	}
	
	private void monitorDirPath()
	{
		String tempPath = "";
		for( int i = 0 ; i < mMonitorDirArray.length ; i++ )
		{
			// 监听 该几个文件夹目录，原先的算法有问题 现改掉 /storage/emulated/0, /storage/emulated/0/Coco,/storage/emulated/0/Coco/Wallpaper,/storage/emulated/0/Coco/Wallpaper/App wanghongjian add 【bug:i_0011712】
			if( i != 0 )
			{
				tempPath += File.separator + mMonitorDirArray[i];
			}
			mMonitorDirPath.put( mMonitorDirArray[i] , mRootPath + tempPath );
		}
	}
	
	private void startNameWatching(
			String name )
	{
		if( !Arrays.asList( mMonitorDirArray ).contains( name ) )
		{
			return;
		}
		if( mObservers.get( name ) != null )
		{
			mObservers.get( name ).stopWatching();
		}
		mObservers.get( name ).startWatching();
	}
	
	public void startAllWatching()
	{
		if( !mObservers.isEmpty() )
		{
			for( int i = 0 ; i < mMonitorDirArray.length ; i++ )
			{
				if( null != mObservers.get( mMonitorDirArray[i] ) )
				{
					mObservers.get( mMonitorDirArray[i] ).stopWatching();
				}
			}
			mObservers.clear();
		}
		for( int j = 0 ; j < mMonitorDirArray.length ; j++ )
		{
			mObservers.put( mMonitorDirArray[j] , new SingleFileObserver( mMonitorDirPath.get( mMonitorDirArray[j] ) ) );
			mObservers.get( mMonitorDirArray[j] ).startWatching();
		}
	}
	
	private void stopNameWatching(
			String name )
	{
		if( !Arrays.asList( mMonitorDirArray ).contains( name ) )
		{
			return;
		}
		if( mObservers.get( name ) != null )
		{
			mObservers.get( name ).stopWatching();
		}
	}
	
	private String getExternalPath()
	{
		String path = "/storage/emulated/0";
		if( new File( path ).exists() )
			return path;
		try
		{
			path = Environment.getExternalStorageDirectory().getCanonicalPath();
		}
		catch( IOException e )
		{
			path = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		path += File.separator;
		return path;
	}
	
	/**
	 * 判断文件夹是否存在,如果不存在则创建文件夹
	 */
	private void getNewFolder()
	{
		for( int j = 1 ; j < mMonitorDirArray.length ; j++ )
		{
			File file = new File( mMonitorDirPath.get( mMonitorDirArray[j] ) );
			File filetemp = new File( file.getAbsolutePath() + System.currentTimeMillis() + Math.random() * 10 );
			//判断文件夹是否存在,如果不存在则创建文件夹
			if( !file.exists() )
			{
				if( !filetemp.exists() )
				{
					boolean dl = filetemp.mkdirs();
					if( dl )
					{
						filetemp.renameTo( file );
						filetemp.delete();
					}
					else
					{
						dl = file.mkdirs();
					}
				}
				else
				{
					filetemp.renameTo( file );
					filetemp.delete();
				}
			}
			else if( !file.isDirectory() )
			{
				file.delete();
				file.mkdirs();//mkdir不能创建多个目录
			}
		}
	}
	
	public void setFileUpdateListener(
			FileUpdateListener iFileUpdate )
	{
		this.iFileUpdateListener = iFileUpdate;
	}
	
	/**
	 * Monitor single directory and dispatch all events to its parent, with full
	 * path.
	 */
	class SingleFileObserver extends FileObserver
	{
		
		String mPath;
		
		public SingleFileObserver(
				String path )
		{
			this( path , CHANGES_ONLY );
			mPath = path;
			//			Log.i( "qinxu" , "SingleFileObserver path = " + path );
		}
		
		public SingleFileObserver(
				String path ,
				int mask )
		{
			super( path , mask );
			mPath = path;
		}
		
		@Override
		public void onEvent(
				int event ,
				String path )
		{
			int el = event & FileObserver.ALL_EVENTS;
			if( ( el == CREATE || el == MOVED_TO ) )
			{
				if( timer != null )
				{
					//取消定时器
					timer.cancel();
				}
				if( Arrays.asList( mMonitorDirArray ).contains( path ) )
				{
					startNameWatching( path );
				}
				if( path.contains( "com.coco.wallpaper" ) )
				{
					//创建定时器
					timer = new Timer();
					timer.schedule( new Task() , delay );
				}
			}
			else if( el == DELETE || el == DELETE_SELF )
			{
				if( timer != null )
				{
					//取消定时器
					timer.cancel();
				}
				if( Arrays.asList( mMonitorDirArray ).contains( path ) || path.contains( "com.coco.wallpaper" ) )
				{
					stopNameWatching( path );
					//创建定时器
					timer = new Timer();
					timer.schedule( new Task() , delay );
					//startAllWatching();
				}
			}
		}
	}
	
	private class Task extends TimerTask
	{
		
		@Override
		public void run()
		{
			//取消
			this.cancel();
			timer = null;
			getNewFolder();
			if( iFileUpdateListener != null )
				iFileUpdateListener.updateWallpaperList();//当在壁纸文件夹下有文件更新的时候，则需要将壁纸都更新一下
		}
	}
	
	public interface FileUpdateListener
	{
		
		public void updateWallpaperList();
	}
}
