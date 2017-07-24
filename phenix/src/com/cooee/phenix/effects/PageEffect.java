package com.cooee.phenix.effects;


/**
 * 特效： 扇面
 */
import android.content.Context;
import android.graphics.Camera;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;

import com.cooee.phenix.LauncherAppState;


public class PageEffect extends EffectInfo
{
	
	private final float ROTATION = 90.0f;
	
	public PageEffect(
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
	
	//		@Override
	//		public void getTransformationMatrix(
	//				IEffect view ,
	//				float offset ,
	//				int pageWidth ,
	//				int pageHeight ,
	//				float distance ,
	//				boolean overScroll ,
	//				boolean overScrollLeft )
	//		{
	//			float absOffset = Math.abs( offset );
	//			view.setCameraDistance( distance );
	//			view.setTranslationX( pageWidth * offset );
	//			view.setPivotX( 0f );
	//			view.setRotationY( -offset * 120.0f );
	//		//			if( offset < 0 )
	//		//			{
	//		//				view.setAlpha( 1 - absOffset );
	//		//			}
	//		}
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
			if( !isLastOrFirstPage )
			{
				v.setTranslationX( pageWidth * scrollProgress );
			}
			else
			{
				v.setTranslationX( 0 );
			}
			if( scrollProgress != 0 && Math.abs( scrollProgress ) != 1 )
			{
				float rotation = ROTATION * scrollProgress;
				// zhangjin@2015/09/08 UPD START
				//v.setCameraDistance( distance * 2 );
				// gaominghui@2016/12/14 ADD START
				if( Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN )
				{
					if( v.getCameraDistance() != distance * 2 )
					{
						v.setCameraDistance( distance * 2 );
					}
				}
				else
				{
					v.setCameraDistance( distance * 2 );
				}
				// gaominghui@2016/12/14 ADD END
				// zhangjin@2015/09/08 UPD END
				float mPivotX = v.getMeasuredWidth();
				//xiatian start	//整理判断“是否从左往右布局”的方法：由“mView.getLayoutDirection()”改为“getResources().getConfiguration().getLayoutDirection()”
				//				if( Tools.isLayoutRTL( v ) == false )//xiatian del
				if( LauncherAppState.isLayoutRTL() == false )//xiatian add 
				//xiatian end
				{
					mPivotX = 0f;
				}
				v.setPivotX( mPivotX );
				v.setPivotY( pageHeight / 2 );
				v.setRotationY( -rotation );
				v.setAlpha( 1 - Math.abs( scrollProgress ) );
			}
			else
			{
				v.setRotationY( 0 );
				v.setTranslationX( 0 );
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
			mView.setAlpha( 1 );
		}
		super.stopEffecf();
	}
}
