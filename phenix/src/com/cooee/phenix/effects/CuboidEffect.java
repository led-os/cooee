/** Created by Spreadtrum */
package com.cooee.phenix.effects;


import android.content.Context;
import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;


public class CuboidEffect extends EffectInfo
{
	
	public CuboidEffect(
			int id ,
			IEffect effect )
	{
		super( id , effect );
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
	
	/* SPRD: Fix bug258437 @{*/
	@Override
	public void getTransformationMatrix(
			IEffect view ,
			float offset ,
			int pageWidth ,
			int pageHeight ,
			float distance ,
			boolean overScroll ,
			boolean overScrollLeft ,
			boolean isLastOrFirstPage )
	{
		/* @ */
		if( view instanceof View )
		{
			View v = (View)view;
			if( isLastOrFirstPage )
			{
				if( mAllEffectViews != null && !mAllEffectViews.contains( v ) )
				{
					mAllEffectViews.add( v );
				}
				offset = Math.max( -maxScroll , Math.min( maxScroll , offset ) );
				int translateX = (int)( -pageWidth * offset );
				v.setTranslationX( translateX );
			}
			else
			{
				if( !overScroll && v.getTranslationX() != 0 )
				{
					v.setTranslationX( 0 );
				}
			}
		}
	}
	
	@Override
	public void stopEffecf()
	{
		for( int i = 0 ; i < mAllEffectViews.size() ; i++ )
		{
			mAllEffectViews.get( i ).setTranslationX( 0 );
		}
		super.stopEffecf();
	}
}
