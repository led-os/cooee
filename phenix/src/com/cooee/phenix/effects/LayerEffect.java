/** Created by Spreadtrum */
/**
 * 切页特效:层叠
 * 
 * 
 */
package com.cooee.phenix.effects;


import android.content.Context;
import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;


public class LayerEffect extends EffectInfo
{
	
	public LayerEffect(
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
		//		Matrix tMatrix = transformation.getMatrix();
		//		float mViewWidth = viewiew.getMeasuredWidth();
		//		float mViewHeight = viewiew.getMeasuredHeight();
		//		if( offset == 0.00F || offset >= 0.99F || offset <= -0.99F )
		//			return false;
		//		transformation.setAlpha( 1.0F - offset );
		//		//viewiew.setAlpha(1.0F - offset);
		//		float level = 0.4F * ( 1.0F - offset );
		//		float scale = 0.6F + level;
		//		tMatrix.setScale( scale , scale );
		//		float xPost = 0.4F * offset * mViewWidth * 3.0F;
		//		float yPost = 0.4F * offset * mViewHeight * 0.5F;
		//		tMatrix.postTranslate( xPost , yPost );
		//		transformation.setTransformationType( Transformation.TYPE_BOTH );
		//		return true;
		return false;
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
		if( view instanceof View && mAllEffectViews != null && !mAllEffectViews.contains( view ) )
		{
			mAllEffectViews.add( ( (View)view ) );
		}
		float value = 1;
		if( offset < 0 )
		{
			value = 1 - Math.abs( offset );
			if( isLastOrFirstPage )
			{
				offset = Math.max( -maxScroll , offset );
				if( view instanceof View )
				{
					( (View)view ).invalidate();
				}
				view.setTranslationX( 0 );
			}
			else
			{
				view.setTranslationX( pageWidth * offset );
			}
		}
		else
		{
			if( isLastOrFirstPage )
			{
				offset = Math.min( maxScroll , offset );
				view.setTranslationX( -pageWidth * offset );
			}
			else
			{
				view.setTranslationX( 0 );
			}
		}
		view.setPivotX( pageWidth * 0.5f );
		view.setPivotY( pageHeight * 0.5f );
		view.setAlpha( value );
		view.setScaleX( value );
		view.setScaleY( value );
	}
	
	@Override
	public Scroller getScroller(
			Context context )
	{
		return new Scroller( context );
	}
	
	@Override
	public int getSnapTime()
	{
		return 220;
	}
	
	@Override
	public void stopEffecf()
	{
		for( int i = 0 ; i < mAllEffectViews.size() ; i++ )
		{
			View mView = mAllEffectViews.get( i );
			mView.setTranslationX( 0 );
			mView.setAlpha( 1 );
			mView.setScaleX( 1 );
			mView.setScaleY( 1 );
		}
		super.stopEffecf();
	}
}
