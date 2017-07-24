/** Created by Spreadtrum */
package com.cooee.phenix.effects;


import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;


public class FadeEffect extends EffectInfo
{
	
	public FadeEffect(
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
		if( offset == 0.0F || offset >= 1.0F || offset <= -1.0F )
			return false;
		Matrix tMatrix = transformation.getMatrix();
		float absOffset = Math.abs( offset );
		float mAlpha = ( 1.0F - absOffset ) * 0.7F + 0.3F;
		transformation.setAlpha( mAlpha );
		tMatrix.setScale( mAlpha , mAlpha , viewiew.getWidth() / 2.0f , viewiew.getHeight() / 2.0f );
		transformation.setTransformationType( Transformation.TYPE_BOTH );
		//        transformation.setTransformationType(Transformation.TYPE_ALPHA);
		return true;
	}
	
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
		float absOffset = Math.abs( offset );
		float alpha = 1.0F - 1.2f * absOffset;
		float xTranslation = pageWidth * offset;
		if( overScroll )
		{
			xTranslation = 0f;
			alpha = alpha * 2f;
		}
		view.setTranslationX( xTranslation );
		view.setAlpha( alpha );
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
		return 240;
	}
}
