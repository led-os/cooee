/***/
package com.cooee.phenix.musicandcamerapage.utils;


import android.view.animation.Interpolator;


/**
 * @author gaominghui 2016年6月25日
 */
public class AccelerateDecelerateInterpolator implements Interpolator
{
	
	/**
	 *
	 * @see android.animation.TimeInterpolator#getInterpolation(float)
	 * @auther gaominghui  2016年6月25日
	 */
	@Override
	public float getInterpolation(
			float input )
	{
		// TODO Auto-generated method stub
		float value = (float)( Math.pow( input - 1 , 5 ) + 1 );
		return value;
	}
}
