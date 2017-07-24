package com.cooee.framework.stackblur;


import android.graphics.Rect;

import com.cooee.framework.stackblur.BlurHelper.BlurCallbacks;


/**
 * 参数配置
 */
public class BlurOptions
{
	
	/**
	 * scaleFactor 模糊的缩放因子，必须>=1f
	 * 对于模糊算法的一种优化，现将需要模糊的oriView进行缩放，然后进行模糊，减少计算量，增大效率，但是会削弱效果
	 */
	public float scaleFactor = 8f;
	/**
	 * radius 模糊半径，必须 >=1
	 */
	public int radius = 3;
	/**
	 * captureWallPaper 是否模糊壁纸
	 */
	public boolean captureWallPaper = true;
	/**
	 * src 需要模糊的坐标矩形 
	 */
	public Rect src = null;
	/**
	 * callbacks 回调
	 */
	public BlurCallbacks callbacks = null;
}
