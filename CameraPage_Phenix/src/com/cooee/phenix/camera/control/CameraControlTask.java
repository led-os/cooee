package com.cooee.phenix.camera.control;


// CameraPage
public class CameraControlTask extends Thread
{
	
	private RunningTask task = null;
	private TaskFinishCallBack callBack = null;
	
	public CameraControlTask(
			RunningTask task ,
			TaskFinishCallBack callBack )
	{
		this.task = task;
		this.callBack = callBack;
	}
	
	@Override
	public void run()
	{
		super.run();
		//
		if( task != null )
			task.task();
		//
		if( callBack != null )
			callBack.finish( this );
	}
	
	public interface RunningTask
	{
		
		public void task();
	}
	
	public interface TaskFinishCallBack
	{
		
		public void finish(
				CameraControlTask controlTask );
	}
}
