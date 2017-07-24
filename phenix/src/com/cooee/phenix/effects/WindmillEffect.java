package com.cooee.phenix.effects;


import android.content.Context;
import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;


/**
 * 切页特效:风车
 * @author tangliang
 *
 */
public class WindmillEffect extends EffectInfo
{
	
	public WindmillEffect(
			int id ,
			IEffect iEffect )
	{
		super( id , iEffect );
	}
	
	@Override
	public boolean getCellLayoutChildStaticTransformation(
			ViewGroup viewGroup ,
			View viewiew ,
			Transformation transformation ,
			Camera camera ,
			float offset )
	{
		return false;
	}
	
	@Override
	public boolean getWorkspaceChildStaticTransformation(
			ViewGroup viewGroup ,
			View viewiew ,
			Transformation transformation ,
			Camera camera ,
			float offset )
	{
		return false;
	}
	
	@Override
	public Scroller getScroller(
			Context context )
	{
		return null;
	}
	
	@Override
	public int getSnapTime()
	{
		return 0;
	}
	
	/**
	 * 切页特效
	 */
	@Override
	public void getTransformationMatrix(
			IEffect curView ,
			float offset ,
			int pageWidth ,
			int pageHeight ,
			float distance ,
			boolean overScroll ,
			boolean overScrollLeft ,
			boolean isLastOrFirstPage )
	{
		float absOffest = Math.abs( offset );
		float halfWidth = pageWidth >> 1;
		float halfHeight = pageHeight >> 1;
		float quarterHeight = pageHeight >> 3;
		float rotation = -40f * offset;
		//边界页效果
		if( !isLoop() && overScroll )
		{
			if( absOffest < 0.3 )
			{
				curView.setPivotX( halfWidth );
				curView.setPivotY( -halfHeight );
				curView.setRotation( rotation );
			}
		}
		//非边界页效果
		else
		{
			curView.setPivotX( halfWidth );
			curView.setPivotY( quarterHeight );
			curView.setRotation( rotation );
		}
	}
}
