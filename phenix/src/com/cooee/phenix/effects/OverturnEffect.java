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
 * 切页特效:翻转
 * 
 *
 */
public class OverturnEffect extends EffectInfo
{
	
	public OverturnEffect(
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
		return new Scroller( context );
	}
	
	@Override
	public int getSnapTime()
	{
		return 0;
	}
	
	@Override
	public void getTransformationMatrix(
			IEffect effect ,
			float offset ,
			int pageWidth ,
			int pageHeight ,
			float distance ,
			boolean overScroll ,
			boolean overScrollLeft ,
			boolean isLastOrFirstPage )
	{
		if( effect instanceof View )
		{
			View v = (View)effect;
			if( mAllEffectViews != null && !mAllEffectViews.contains( v ) )
			{
				mAllEffectViews.add( v );
			}
			if( offset == 0 || Math.abs( offset ) == 1 )
			{
				v.setRotationY( 0 );
				v.setTranslationX( 0 );
			}
			else
			{
				float rotation = -180.0f * Math.max( -1f , Math.min( 1f , offset ) );
				if( !isLastOrFirstPage )
				{
					if( offset >= -0.5f && offset <= 0.5f )
					{
						v.setTranslationX( pageWidth * offset );
					}
					else
					{
						v.setTranslationX( pageWidth * -10f );
					}
				}
				else
				{
					if( rotation < 0 )
					{
						if( rotation < -90.0f * maxScroll )
						{
							rotation = -90.0f * maxScroll;
						}
					}
					else
					{
						if( rotation > 90.f * maxScroll )
						{
							rotation = 90.f * maxScroll;
						}
					}
					v.setTranslationX( 0 );
				}
				// zhangjin@2015/09/08 UPD START
				//v.setCameraDistance( distance );
				// gaominghui@2016/12/14 ADD START
				if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
				{
					if( v.getCameraDistance() != distance )
					{
						v.setCameraDistance( distance );
					}
				}
				else
				{
					v.setCameraDistance( distance );
				}
				// gaominghui@2016/12/14 ADD END
				// zhangjin@2015/09/08 UPD END
				v.setPivotX( pageWidth * 0.5f );
				v.setPivotY( pageHeight * 0.5f );
				v.setRotationY( rotation );
			}
		}
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
