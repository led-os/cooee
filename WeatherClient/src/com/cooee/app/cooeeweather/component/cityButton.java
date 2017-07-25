package com.cooee.app.cooeeweather.component;


import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Button;


public class cityButton extends Button
{
	
	private DisplayMetrics dm = new DisplayMetrics();
	
	public cityButton(
			Context context )
	{
		super( context );
		WindowManager mWm = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE );
		mWm.getDefaultDisplay().getMetrics( dm );
	}
	
	public cityButton(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		// 获得屏幕大小
		WindowManager mWm = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE );
		mWm.getDefaultDisplay().getMetrics( dm );
	}
	
	@Override
	public void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		/* if (dm.widthPixels == 480 && dm.heightPixels == 854) {
		     setMeasuredDimension(480+43, 480+43);
		 } else if(dm.widthPixels == 800) {
		 	 //setMeasuredDimension(80, 80);
		 	// setBackgroundResource(R.drawable.city_item_bg);
		     super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		 }
		 else{
		 	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		 }
		}*/
		/**
		 * 横竖屏适配 竖屏以宽为依据 横屏以高为依据 【因为有些手机有虚拟菜单 高度（竖屏）无法正确测量】
		 * fulijuan add 2017/5/9 start
		 */
		if( dm.widthPixels == 720 || dm.heightPixels == 720 )
		{
			setMeasuredDimension( 190 , 250 );
		}
		else
			if( dm.widthPixels == 1080 || dm.heightPixels == 1080 )
		{
			setMeasuredDimension( 240 , 320 );
		}
		else
				if( dm.widthPixels == 480 || dm.heightPixels == 480 )
		{
			setMeasuredDimension( 135 , 150 );
		}
		else
					if( dm.widthPixels == 540 || dm.heightPixels == 540 )
		{
			setMeasuredDimension( 160 , 190 );
		}
		else
						if( dm.widthPixels == 320 || dm.heightPixels == 320 )
		{
			setMeasuredDimension( 90 , 120 );
		}
		else
							if( dm.widthPixels == 800 )
		{
			setMeasuredDimension( 206 , 260 );
		}
		else
		{
			setMeasuredDimension( 130 , 168 );
		}
		/**
		 * 横屏适配
		 * fulijuan add 2017/5/9 end
		 */
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
