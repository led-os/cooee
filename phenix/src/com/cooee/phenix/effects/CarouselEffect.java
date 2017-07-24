package com.cooee.phenix.effects;


import android.content.Context;
import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;


/**
 * 切页特效:旋转木马
 * @author tangliang
 *
 */
public class CarouselEffect extends EffectInfo
{
	
	/**true=左木马   false=右木马*/
	private boolean flag;
	
	public CarouselEffect(
			int id ,
			IEffect effect )
	{
		super( id , effect );
	}
	
	public CarouselEffect(
			int id ,
			boolean flag ,
			IEffect effect )
	{
		super( id , flag , effect );
		this.flag = flag;
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
		//		float mViewHalfWidth = mViewWidth / 2.0F;
		//		float mViewHalfHeight = viewiew.getMeasuredHeight() / 2.0F;
		//		float absOffset = Math.abs( offset );
		//		if( offset == 0.0F || offset >= 1.0F || offset <= -1.0F )
		//			return false;
		//		transformation.setAlpha( 1.0F - absOffset );
		//		camera.save();
		//		float xTranslate = ( -mViewHalfWidth ) * absOffset / 3.0F;
		//		float zTranslate = -mViewHalfWidth * offset;
		//		camera.translate( xTranslate , mViewHalfHeight , zTranslate );
		//		float yRotate = 30.0F * ( -offset );
		//		camera.rotateY( yRotate );
		//		camera.getMatrix( tMatrix );
		//		camera.restore();
		//		float xPost = mViewWidth * offset;
		//		tMatrix.postTranslate( xPost , mViewHalfHeight );
		//		transformation.setTransformationType( Transformation.TYPE_BOTH );
		return true;
	}
	
	/**
	 * 切页特效
	 */
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
		float mAlpha = 1.0F - absOffset * 0.9f;
		float yRotation = ( flag ? 90.0F * ( -offset ) : 90.0F * ( offset ) );
		float xTranslation = pageWidth * offset;
		//边界页效果控制:只做一半特效
		if( overScroll )
		{
			xTranslation = 0f;
			yRotation = yRotation * 0.5f;//优化:边界页只能转45度
		}
		view.setCameraDistance( distance );
		view.setPivotY( 0 );
		view.setPivotX( flag ? 0.0f : pageWidth );
		view.setTranslationX( xTranslation );
		view.setRotationY( yRotation );
		view.setAlpha( mAlpha );
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
}
