package com.cooee.phenix.camera.utils;


// MusicPage CameraPage
import java.util.ArrayList;
import java.util.List;


public class TimerUtils
{
	
	private static TimerThread thread = null;
	private static long sleepMilliseconds = 1000;
	private static List<TimerCallBack> timerCallBacks = null;
	
	public synchronized static void setSleepMilliseconds(
			long milliseconds )
	{
		if( milliseconds < 0 )
			sleepMilliseconds = 1000;
		else
			sleepMilliseconds = milliseconds;
	}
	
	public synchronized static void startTimer(
			TimerCallBack callBack )
	{
		if( timerCallBacks == null )
			timerCallBacks = new ArrayList<TimerCallBack>();
		if( !timerCallBacks.contains( callBack ) && callBack != null )
			timerCallBacks.add( callBack );
		if( thread == null )
			thread = new TimerThread();
		if( !thread.isAlive() )
			thread.start();
	}
	
	public synchronized static void removeCallBack(
			TimerCallBack callBack )
	{
		if( timerCallBacks != null )
			if( timerCallBacks.contains( callBack ) )
				timerCallBacks.remove( callBack );
		//
		stopTimer();
	}
	
	private static void stopTimer()
	{
		if( timerCallBacks == null || timerCallBacks.size() == 0 )
		{
			timerCallBacks = null;
			if( thread != null )
			{
				thread.exit = true;
				thread = null;
			}
		}
	}
	
	private static class TimerThread extends Thread
	{
		
		public boolean exit = false;
		
		@Override
		public void run()
		{
			super.run();
			for( ; ; )
			{
				if( exit )
					break;
				//
				try
				{
					Thread.sleep( sleepMilliseconds );
				}
				catch( InterruptedException e )
				{
					e.printStackTrace();
					break;
				}
				//
				if( exit )
					break;
				//
				List<TimerCallBack> callBacks = timerCallBacks;
				if( callBacks != null )
				{
					//int size = callBacks.size();
					for( int i = 0 ; i < callBacks.size() ; i++ )
					{
						TimerCallBack callBack = callBacks.get( i );
						if( callBack != null )
							callBack.timing();
					}
				}
			}
		}
	}
	
	public interface TimerCallBack
	{
		
		/**
		 * go to here in thread
		 */
		public void timing();
	}
}
