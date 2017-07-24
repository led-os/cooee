package com.cooee.phenix.mediapage;


/**
 * 媒体专属页与桌面的公用交互接口
 * @author yangtianyu  2016-7-18
 */
public interface IMediaPlugin
{
	
	/**
	 * 当前页面开始移动
	 * @author yangtianyu 2016-6-29
	 */
	void onPageBeginMoving();
	
	/**
	 * 滑动到本页面
	 * @author yangtianyu 2016-6-29
	 */
	void onPageMoveIn();
	
	/**
	 * 从本页面滑动到其他页面
	 * @author yangtianyu 2016-6-29
	 */
	void onPageMoveOut();
	
	/**
	 * 桌面onPause
	 * @author yangtianyu 2016-7-18
	 */
	void onPause();
	
	/**
	 * 桌面onResume
	 * @author yangtianyu 2016-7-18
	 */
	void onResume();
}
