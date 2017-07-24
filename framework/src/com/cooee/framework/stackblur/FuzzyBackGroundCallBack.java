package com.cooee.framework.stackblur;


import android.graphics.Bitmap;


public interface FuzzyBackGroundCallBack
{
	
	/**
	 * view 模糊回调     
	 * @param bitmap  一般情况下  此bitmap使用来设置背景的    请在此背景不可见时候 将背景设为null 停止引用此bitmap
	 */
	void setFuzzBackGround(
			Bitmap bitmap );
}
