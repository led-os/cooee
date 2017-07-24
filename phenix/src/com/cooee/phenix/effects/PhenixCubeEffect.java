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
 * 特效：跃动
 * @author temp
 *
 */
public class PhenixCubeEffect extends EffectInfo
{
	
	public PhenixCubeEffect(
			int id ,
			IEffect iEffect )
	{
		super( id , iEffect );
	}
	
	public PhenixCubeEffect(
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
		//Log.d( "MM" , " view" + view + " isLastOrFirstPage " + isLastOrFirstPage + " scrollProgress " + scrollProgress + " distance " + distance );
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
				v.setPivotX( scrollProgress < 0 ? 0 : pageWidth );
				//				v.setPivotY( pageHeight * 0.5f );
				v.setPivotY( 0 );
				float transY = Math.abs( scrollProgress ) * pageHeight * 0.6f;
				v.setTranslationY( transY );
				float scale = 1.0f - 0.6f * Math.abs( scrollProgress );
				v.setScaleX( scale );
				v.setScaleY( scale );
				float rotation = -80.0f * scrollProgress;
				v.setRotationY( rotation );
				//Log.d( "MM" , " transY " + transY + " setScaleY " + scale + " rotation " + rotation + " pageHeight " + pageHeight);
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
			// zhangjin@2015/07/27 i_11926 ADD START
			mView.setTranslationY( 0 );
			mView.setScaleX( 1.0f );
			mView.setScaleY( 1.0f );
			mView.setPivotY( mView.getHeight() / 2 );
			// zhangjin@2015/07/27 ADD END
		}
		super.stopEffecf();
	}
}
