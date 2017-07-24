package com.cooee.phenix.effects;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;


public abstract class EffectInfo
{
	
	public final int id;
	public boolean flag;
	public IEffect iEffect;
	public float maxScroll = 0.5f;
	public List<View> mAllEffectViews = new ArrayList<View>();
	private boolean isLoop = false;
	
	public EffectInfo(
			int id ,
			IEffect iEffect )
	{
		this.id = id;
		this.iEffect = iEffect;
	}
	
	public EffectInfo(
			int id ,
			boolean flag ,
			IEffect iEffect )
	{
		this.id = id;
		this.flag = flag;
		this.iEffect = iEffect;
	}
	
	public abstract boolean getCellLayoutChildStaticTransformation(
			ViewGroup viewGroup ,
			View viewiew ,
			Transformation transformation ,
			Camera camera ,
			float offset );
	
	public abstract boolean getWorkspaceChildStaticTransformation(
			ViewGroup viewGroup ,
			View viewiew ,
			Transformation transformation ,
			Camera camera ,
			float offset );
	
	public abstract Scroller getScroller(
			Context context );
	
	public abstract int getSnapTime();
	
	public abstract void getTransformationMatrix(
			IEffect curView ,
			float offset ,
			int pageWidth ,
			int pageHeight ,
			float distance ,
			boolean overScroll ,
			boolean overScrollLeft ,
			boolean isLastOrFirstPage );
	
	public float getWorkspaceOvershootInterpolatorTension()
	{
		return 0f;
	}
	
	public void stopEffecf()
	{
		//chenliang add start	解决“在第一页为普通页面，最后一页为功能页，并开启桌面循环切页的前提下，快速的从普通页滑到最后一页，再滑到第一页时，应用图标不及时显示”的问题。【i_0014985】
		if( mAllEffectViews.isEmpty() == false )
		{//添加集合值非空判断
			mAllEffectViews.clear();
		}
		//chenliang add end
	}
	
	public int getViewNameId(
			View v )
	{
		if( v != null && v.getParent() instanceof ViewGroup )
		{
			ViewGroup viewGroup = (ViewGroup)v.getParent();
			for( int i = 0 ; i < viewGroup.getChildCount() ; i++ )
			{
				View child = viewGroup.getChildAt( i );
				if( v == child )
				{
					return i;
				}
			}
		}
		return -1;
	}
	
	public void setMaxScroll(
			float max )
	{
		maxScroll = max;
	}
	
	public boolean isLoop()
	{
		return(
		//
		isLoop
		//
		//		&& ( mAllEffectViews.size() > 1/* 解决“开启桌面循环切页的前提下，当桌面只有一个页面时，滑动页面过程中，动画异常”的问题。【c_0004649】 */)
		//
		);
	}
	
	public void setLoop(
			boolean isLoop )
	{
		this.isLoop = isLoop;
	}
	
	public void stopCellLayoutChildTransformation(
			View v )
	{
	}
}
