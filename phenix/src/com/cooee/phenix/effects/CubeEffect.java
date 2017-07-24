/** Created by Spreadtrum */
package com.cooee.phenix.effects;


import android.content.Context;
import android.graphics.Camera;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;


/**
 * 特效：内外盒子
 * @author temp
 *
 */
public class CubeEffect extends EffectInfo
{
	
	public CubeEffect(
			int id ,
			IEffect iEffect )
	{
		super( id , iEffect );
	}
	
	public CubeEffect(
			int id ,
			boolean flag ,
			IEffect iEffect )
	{
		super( id , flag , iEffect );
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
		//		float mViewHalfWidth = viewiew.getMeasuredWidth() / 2.0F;
		//		float mViewHalfHeight = viewiew.getMeasuredHeight() / 2.0F;
		//		if( offset == 0.0F || offset >= 1.0F || offset <= -1.0F )
		//			return false;
		//		transformation.setAlpha( 1.0F - Math.abs( offset ) );
		//		// viewiew.setAlpha(1.0F - Math.abs(offset));
		//		camera.save();
		//		camera.translate( 0.0F , 0.0F , mViewHalfWidth );
		//		camera.rotateY( -90.0F * offset );
		//		camera.translate( 0.0F , 0.0F , -mViewHalfWidth );
		//		camera.getMatrix( tMatrix );
		//		camera.restore();
		//		tMatrix.preTranslate( -mViewHalfWidth , -mViewHalfHeight );
		//		float transOffset = ( 1.0F + 2.0F * offset ) * mViewHalfWidth;
		//		tMatrix.postTranslate( transOffset , mViewHalfHeight );
		//		transformation.setTransformationType( Transformation.TYPE_BOTH );
		return false;
	}
	
	//	@Override
	//	public void getTransformationMatrix(
	//			IEffect view ,
	//			float offset ,
	//			int pageWidth ,
	//			int pageHeight ,
	//			float distance ,
	//			boolean overScroll ,
	//			boolean overScrollLeft )
	//	{
	//		float absOffset = Math.abs( offset );
	//		float yRotation = 0;
	//		float xTranslation = 0f;
	//		view.setPivotX( offset < 0 ? 0 : pageWidth );
	//		view.setPivotY( pageHeight >> 1 );
	//		if( flag )
	//		{
	//			yRotation = 90.0f * offset;
	//			if( overScroll )
	//			{
	//				if( absOffset < 0.3 )
	//				{
	//					xTranslation = pageWidth * offset;
	//					view.setTranslationX( xTranslation );
	//					view.setRotationY( yRotation );
	//				}
	//			}
	//			else
	//			{
	//				view.setTranslationX( xTranslation );
	//				view.setRotationY( yRotation );
	//			}
	//		}
	//		else
	//		{
	//			yRotation = -90.0f * offset;
	//			//边界页效果
	//			if( overScroll )
	//			{
	//				if( absOffset < 0.3 )
	//				{
	//					xTranslation = -pageWidth * offset;
	//					view.setTranslationX( xTranslation );
	//					view.setRotationY( yRotation );
	//				}
	//			}
	//			//非边界页效果
	//			else
	//			{
	//				view.setTranslationX( xTranslation );
	//				view.setRotationY( yRotation );
	//			}
	//		}
	//	}
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
			if( scrollProgress == 0 || Math.abs( scrollProgress ) == 1 )
			{
				v.setRotationY( 0 );
				v.setTranslationX( 0 );
			}
			else
			{
				if( isLastOrFirstPage )
				{
					v.setTranslationX( -scrollProgress * pageWidth );
				}
				else
				{
					v.setTranslationX( 0 );
				}
				float rotation = ( flag ? 80.0f : -80.0f ) * scrollProgress;
				// zhangjin@2015/07/24 UPD START
				//if( flag )
				//{
				//	v.setCameraDistance( distance * 4 );
				//}
				if( flag )
				{
					// zhangjin@2015/09/08 UPD START
					//v.setCameraDistance( distance * 4 );
					// gaominghui@2016/12/14 ADD START
					if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
					{
						if( v.getCameraDistance() != distance * 4 )
						{
							v.setCameraDistance( distance * 4 );
						}
					}
					else
					{
						v.setCameraDistance( distance * 4 );
					}
					// gaominghui@2016/12/14 ADD END
					// zhangjin@2015/09/08 UPD END
				}
				else
				{
					distance = distance * 0.4f;
					float camd = 2 * distance - distance * Math.abs( scrollProgress );
					// zhangjin@2015/09/08 UPD START
					//v.setCameraDistance( camd );
					// gaominghui@2016/12/14 ADD START
					if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
					{
						if( v.getCameraDistance() != camd )
						{
							v.setCameraDistance( camd );
						}
					}
					else
					{
						v.setCameraDistance( camd );
					}
					// gaominghui@2016/12/14 ADD END
					// zhangjin@2015/09/08 UPD END
				}
				// zhangjin@2015/07/24 UPD END
				v.setPivotX( scrollProgress < 0 ? 0 : pageWidth );
				v.setPivotY( pageHeight * 0.5f );
				v.setRotationY( rotation );
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
			mView.setRotationY( 0 );
		}
		super.stopEffecf();
	}
}
