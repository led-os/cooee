package com.cooee.phenix.camera.inte;


import java.util.List;

import com.cooee.phenix.kmob.ad.KmobAdData;


public interface IAdDisplayer
{
	
	/**
	 * 当前是否正在展示广告
	 * @return 正在展示为true 否则为false
	 * @author yangtianyu 2016-7-1
	 */
	boolean isShowing();
	
	/**
	 * 获取广告已经展示的次数
	 * @return 广告已经展示的次数
	 * @author yangtianyu 2016-7-1
	 */
	long getTimesAdShown();
	
	/**
	 * 增加一个广告数据
	 * @param newAdItem 新的广告数据
	 * @author yangtianyu 2016-7-1
	 */
	void addAdItem(
			KmobAdData newAdItem );
	
	/**
	 * 通知广告展示页,广告数据已经更新,需要批量更换
	 * @param newAdList 新的广告数据数组
	 * @author yangtianyu 2016-7-1
	 */
	void notifyAdChanged(
			List<KmobAdData> newAdList );
	
	/**
	 * 隐藏广告展示页后,广告展示页需要进行的一些处理
	 * (不管是处于广告所在的页面时隐藏广告,还是切换到非广告所在页面)
	 * @author yangtianyu 2016-7-1
	 */
	void hideAdView();
	
	/**
	 * 显示广告展示页后,广告展示页需要进行的一些处理
	 * @author yangtianyu 2016-7-1
	 */
	void showAdView();
	
	/**
	 * 点击广告展示页
	 * @author yangtianyu 2016-7-1
	 */
	void onClick();
	
	/**
	 * 不再需要显示时,回收资源
	 * @author yangtianyu 2016-7-1
	 */
	void dispose();
}
