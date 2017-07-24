package com.cooee.update.taskManager;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class TaskManager
{
	
	private static String TAG = "TaskManager";
	private static TaskHandler mHandler = new TaskHandler();
	private static final int MESSAGE_POST_RESULT = 0x1;
	private static final int MESSAGE_POST_PROGRESS = 0x2;
	private static final int POOL_SIZE = 5;
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		
		private final AtomicInteger mCount = new AtomicInteger( 1 );
		
		public Thread newThread(
				Runnable r )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "TaskManager #" , mCount.get() ) );
			return new Thread( r , StringUtils.concat( "TaskManager #" , mCount.getAndIncrement() ) );
		}
	};
	private static ThreadPoolExecutor mExecutor = new ThreadPoolExecutor( POOL_SIZE , POOL_SIZE , 10 , TimeUnit.SECONDS , new LinkedBlockingQueue<Runnable>() , sThreadFactory );
	private static int TASK_ID = 0;
	private static Map<Integer , InnerTask> mTasks = new HashMap<Integer , InnerTask>();
	
	/**
	 * 把线程放到线程池里面 创建task对象里面存放了httptask和线程定义的id 在run方法里面会调用publishresult（)方法
	 * 调用者的runInback()方法
	 */
	public static int execute(
			Task httpTask )
	{
		TASK_ID++;
		InnerTask task = new InnerTask( TASK_ID , httpTask );
		mExecutor.execute( task );
		mTasks.put( TASK_ID , task );
		httpTask.mTaskId = TASK_ID;
		return TASK_ID;
	}
	
	public static void publishProgress(
			Task httpTask ,
			Object ... progress )
	{
		mHandler.obtainMessage( MESSAGE_POST_PROGRESS , new Result( httpTask , progress ) ).sendToTarget();
	}
	
	public static void publishResult(
			Task httpTask ,
			TaskResult result )
	{
		mHandler.obtainMessage( MESSAGE_POST_RESULT , new Result( httpTask , result ) ).sendToTarget();
	}
	
	public static void cancel(
			int taskId )
	{
		InnerTask task = mTasks.get( taskId );
		if( task != null )
		{
			task.cancel();
			mTasks.remove( taskId );
		}
	}
	
	public static void cancelAll()
	{
		Collection<InnerTask> tasks = mTasks.values();
		for( InnerTask tempTask : tasks )
		{
			tempTask.cancel();
		}
		mTasks.clear();
	}
	
	/**
	 * 传进来的HttpTask或者继承类 返回result
	 */
	private static class InnerTask implements Runnable
	{
		
		private Task mHttpTask;
		private Integer mTaskId;
		private volatile Thread mRunner;
		
		public InnerTask(
				Integer taskId ,
				Task httpTask )
		{
			mHttpTask = httpTask;
			mTaskId = taskId;
		}
		
		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			mRunner = Thread.currentThread();
			mHttpTask.setRunState( true );
			mHttpTask.runInBack();
		}
		
		public void cancel()
		{
			mHttpTask.setRunState( false );
			mHttpTask.onCancel();
			mRunner.interrupt();
		}
	}
	
	private static class TaskHandler extends Handler
	{
		
		@Override
		public void handleMessage(
				Message msg )
		{
			// TODO Auto-generated method stub
			Result result = (Result)msg.obj;
			switch( msg.what )
			{
				case MESSAGE_POST_RESULT:
					result.mTask.onInnerResult( (TaskResult)result.mData[0] );
					break;
				case MESSAGE_POST_PROGRESS:
					result.mTask.onInnerProgress( result.mData );
					break;
			}
		}
	}
	
	private static class Result
	{
		
		final Task mTask;
		final Object[] mData;
		
		Result(
				Task task ,
				Object ... data )
		{
			mTask = task;
			mData = data;
		}
	}
}
