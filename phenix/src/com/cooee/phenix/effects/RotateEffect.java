/** Created by Spreadtrum */
package com.cooee.phenix.effects;


import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;


/**
 * 特效：风车
 * @author temp
 *
 */
public class RotateEffect extends EffectInfo
{
	
	private boolean mUp = true;//表示向上张开角度还是向下张开角度旋转
	private static final float TRANSITION_SCREEN_ROTATION = 25f;//旋转的角度
	
	public RotateEffect(
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
		float mViewWidth = viewiew.getMeasuredWidth();
		float mViewHeight = viewiew.getMeasuredHeight();
		Matrix tMatrix = transformation.getMatrix();
		float offsetDegree = -offset * 45.0F;
		tMatrix.setRotate( offsetDegree , mViewWidth / 2.0F , mViewHeight );
		transformation.setTransformationType( Transformation.TYPE_MATRIX );
		return false;
	}
	
	@Override
	public void getTransformationMatrix(
			IEffect view ,
			float scrollProgress ,
			int pageWidth ,
			int pageHeight ,
			float distance ,
			boolean overScroll ,
			boolean overScrollLeft ,
			boolean isLastOrFirstPage )
	{
		if( view instanceof View )
		{
			View v = (View)view;
			if( mAllEffectViews != null && !mAllEffectViews.contains( v ) )
			{
				mAllEffectViews.add( v );
			}
			float rotation = ( mUp ? TRANSITION_SCREEN_ROTATION : -TRANSITION_SCREEN_ROTATION ) * scrollProgress;
			if( scrollProgress != 0 || Math.abs( scrollProgress ) != 1 )
			{
				float rotatePoint = ( pageWidth * 0.5f ) / (float)Math.tan( Math.toRadians( (double)( TRANSITION_SCREEN_ROTATION * 0.5f ) ) );
				v.setPivotX( pageWidth * 0.5f );
				if( mUp )
				{
					v.setPivotY( -rotatePoint );
				}
				else
				{
					v.setPivotY( pageHeight + rotatePoint );
				}
			}
			v.setRotation( rotation );
			if( !isLastOrFirstPage )
			{
				v.setTranslationX( pageWidth * scrollProgress );
			}
			else
			{
				v.setTranslationX( 0 );
			}
		}
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
		return 0;
	}
	
	@Override
	public void stopEffecf()
	{
		for( int i = 0 ; i < mAllEffectViews.size() ; i++ )
		{
			View mView = mAllEffectViews.get( i );
			mView.setTranslationX( 0 );
			mView.setRotation( 0 );
		}
		super.stopEffecf();
	}
	
	@Override
	public void stopCellLayoutChildTransformation(
			View child )
	{
		// TODO Auto-generated method stub
		super.stopCellLayoutChildTransformation( child );
		if( child != null )
		{
			child.setTranslationX( 0 );
			child.setRotation( 0 );
		}
	}
}
