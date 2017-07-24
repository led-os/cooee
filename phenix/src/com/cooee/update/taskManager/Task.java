package com.cooee.update.taskManager;


import java.util.ArrayList;
import java.util.List;


public abstract class Task
{
	
	protected List<Listener> mListeners;
	protected TaskParam mParam;
	protected Boolean mRun = false;
	protected int mTaskId = -1;
	
	/**
	 * 需要在runInBack函数中，主动调用 publishProgress publishResult
	 */
	public abstract void runInBack();
	
	public Task()
	{
		mListeners = new ArrayList<Listener>();
	}
	
	public Task(
			TaskParam param )
	{
		mListeners = new ArrayList<Listener>();
		mParam = param;
	}
	
	public void onInnerResult(
			TaskResult result )
	{
		// TODO Auto-generated method stub
		for( Listener listener : mListeners )
		{
			listener.onResult( result );
		}
	}
	
	public void onInnerProgress(
			Object ... progress )
	{
		// TODO Auto-generated method stub
		for( Listener listener : mListeners )
		{
			listener.onProgress( progress );
		}
	}
	
	protected void publishProgress(
			Object ... progress )
	{
		TaskManager.publishProgress( this , progress );
	}
	
	protected void publishResult(
			TaskResult result )
	{
		TaskManager.publishResult( this , result );
		setRunState( false );
	}
	
	public void addListener(
			Listener listener )
	{
		if( listener != null )
		{
			mListeners.add( listener );
		}
	}
	
	public void removeListener(
			Listener listener )
	{
		mListeners.remove( listener );
	}
	
	public void removeListenerByClass(
			Class target )
	{
		for( int i = mListeners.size() - 1 ; i >= 0 ; i-- )
		{
			Listener listener = mListeners.get( i );
			if( listener.getClass() == target )
			{
				mListeners.remove( listener );
			}
		}
	}
	
	public void clearListeners()
	{
		mListeners.clear();
	}
	
	public void setRunState(
			boolean state )
	{
		mRun = state;
	}
	
	public boolean getRunState()
	{
		return mRun;
	}
	
	public int getTaskId()
	{
		return mTaskId;
	}
	
	public void onCancel()
	{
	}
}
