package com.cooee.phenix.camera.control;


// CameraPage
import java.util.LinkedList;
import java.util.List;

import com.cooee.phenix.camera.control.CameraControlTask.RunningTask;
import com.cooee.phenix.camera.control.CameraControlTask.TaskFinishCallBack;


public class CameraControlTaskManager
{
	
	private static List<CameraControlTask> list = null;
	private static TaskFinishCallBack callBack = null;
	
	private static void init()
	{
		if( list == null )
			list = new LinkedList<CameraControlTask>();
		if( callBack == null )
			callBack = new TaskFinishCallBack() {
				
				@Override
				public void finish(
						CameraControlTask controlTask )
				{
					if( list != null )
					{
						list.remove( controlTask );
						if( list.size() > 0 )
						{
							CameraControlTask task = list.get( 0 );
							if( !task.isAlive() )
								task.start();
						}
					}
				}
			};
	}
	
	private static void finish()
	{
		if( list != null )
			list.clear();
		list = null;
		callBack = null;
	}
	
	public synchronized static void startTask(
			RunningTask task )
	{
		init();
		//
		CameraControlTask controlTask = new CameraControlTask( task , callBack );
		list.add( controlTask );
		if( list.size() == 1 )
		{
			controlTask.start();
		}
	}
	
	public synchronized static void stopAllTask(
			RunningTask task )
	{
		finish();
		if( task != null )
		{
			CameraControlTask controlTask = new CameraControlTask( task , null );
			controlTask.start();
		}
	}
}
