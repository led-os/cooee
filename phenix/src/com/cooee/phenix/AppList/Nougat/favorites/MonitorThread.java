package com.cooee.phenix.AppList.Nougat.favorites;


import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;



/**
 * 
 * @author 吕江滨
 * 
 */
public class MonitorThread extends Thread
{
	
	private static final String TAG = "MonitorThread";
	private Context mContext;
	private static final int TIME_INTERVAL_ONCE = 600;// 每次更新启动的间隔次数。10分钟
	private static final int TIME_INTERVAL_FLUSH = 1800;// 检查常用应用数据库更新的间隔次数。30分钟
	private static boolean isRun;
	private static int times = 0;
	private static ComponentName lastName = null;
	public static HashMap<ComponentName , Long> countMap = new HashMap<ComponentName , Long>();
	public static boolean isDataChanged = false;
	
	public MonitorThread(
			Context context )
	{
		super( TAG );
		mContext = context;
		isRun = true;
		if( isDataChanged )
		{
			countMap.clear();
			isDataChanged = false;
		}
	}
	

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		super.run();
		while( isRun && mContext != null )
		{
			ComponentName pn = null;
			//		String pack = "";
			if( mContext != null )
			{
				pn = RunningAppHelper.getTopAppPckageName( mContext );
				//				pack = Tools.getForegroundApp();
			}
			if( pn != null )
			{
				//
				if( FavoritesAppManager.getInstance().getApplicationInfoFromAll( pn ) != null )
				{
					if( pn.equals( lastName ) )
					{
						updateTimes( pn );
					}
					else
					{
						FavoritesAppManager.getInstance().updateTimes( pn );
					}
				}
				//
				lastName = pn;
				if( FavoritesAppManager.getInstance().isNewAdd() )
				{
					FavoritesAppManager.getInstance().updateFavoritesApps( mContext );
					FavoritesAppManager.getInstance().setNewAdd( false );
				}
				//
				if( times > TIME_INTERVAL_FLUSH )
				{
					times = 0;
					if( FavoritesAppManager.getInstance().isUpdate() )
					{
						FavoritesAppManager.getInstance().updateFavoritesApps( mContext );
						FavoritesAppManager.getInstance().setUpdate( false );
					}
				}
				try
				{
					Thread.sleep( 1000 );
				}
				catch( InterruptedException e )
				{
					continue;
				}
				times++;
			}
		}
		if( isDataChanged )
		{
			countMap.clear();
			isDataChanged = false;
		}
		lastName = null;
		times = 0;
	}
	
	public static boolean isRun()
	{
		return isRun;
	}
	
	public static void setRun(
			boolean isRun )
	{
		MonitorThread.isRun = isRun;
	}
	
	private void updateTimes(
			ComponentName name )
	{
		if( countMap.containsKey( name ) )
		{
			long c = countMap.get( name );
			c++;
			if( c % TIME_INTERVAL_ONCE == 0 )
			{
				FavoritesAppManager.getInstance().updateTimes( name );
			}
			countMap.put( name , c );
		}
		else
		{
			countMap.put( name , 1l );
		}
	}
}
