package com.cooee.update.taskManager;


public class Listener
{
	
	/**
	 * 当异步调用时，onResult可能返回两次，第一次为null,第二次为正常的结果
	 * @param result
	 */
	public void onResult(
			TaskResult result )
	{
	}
	
	public void onProgress(
			Object ... progress )
	{
	}
}
