package com.cooee.phenix.effects;


import android.content.Context;
import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;

import com.cooee.phenix.CellLayout;
import com.cooee.phenix.ShortcutAndWidgetContainer;


// 琴弦
public class StringsEffect extends EffectInfo
{
	
	private int[] lOcationOnScreen = new int[2];
	
	public StringsEffect(
			int id ,
			IEffect effect )
	{
		super( id , effect );
	}
	
	public StringsEffect(
			int id ,
			boolean flag ,
			IEffect effect )
	{
		super( id , flag , effect );
	}
	
	@Override
	public boolean getCellLayoutChildStaticTransformation(
			ViewGroup viewGroup ,
			View viewiew ,
			Transformation transformation ,
			Camera camera ,
			float offset )
	{
		return true;
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
		return 200;
	}
	
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
		curView.getLocationOnScreen( lOcationOnScreen );
		if( curView instanceof CellLayout )
		{
			CellLayout celllayout;
			celllayout = (CellLayout)curView;
			ShortcutAndWidgetContainer mShortcutAndWidgetContainer = celllayout.getShortcutsAndWidgets();
			if( !isLastOrFirstPage )
			{
				if( Math.abs( offset ) <= 0.5f )
				{
					celllayout.setTranslationX( pageWidth * offset );
				}
				else
				{
					celllayout.setAlpha( 0f );
					celllayout.setTranslationX( 0 );
				}
			}
			else
			{
				celllayout.setTranslationX( 0 );
			}
			if( mAllEffectViews != null && !mAllEffectViews.contains( celllayout ) )
			{
				mAllEffectViews.add( celllayout );
			}
			if( celllayout.celllayoutxy == null )
			{
				celllayout.celllayoutxy = new View[celllayout.getCountX()][celllayout.getCountY()];
				for( int i = 0 ; i < celllayout.getCountY() ; i++ )
				{
					for( int j = 0 ; j < celllayout.getCountX() ; j++ )
					{
						int ij = ( i * celllayout.getCountX() + j );
						if( mShortcutAndWidgetContainer.getChildAt( ij ) == null )
							continue;
						// zhangjin@2015/09/16 UPD START
						//int L = mShortcutAndWidgetContainer.getChildAt( ij ).getLeft();
						//int T = mShortcutAndWidgetContainer.getChildAt( ij ).getTop();
						//int x = L / celllayout.getCellWidth();
						//int y = T / celllayout.getCellHeight();
						//celllayout.celllayoutxy[L / celllayout.getCellWidth()][T / celllayout.getCellHeight()] = mShortcutAndWidgetContainer.getChildAt( ij );
						celllayout.celllayoutxy[j][i] = mShortcutAndWidgetContainer.getChildAt( ij );
						// zhangjin@2015/09/16 UPD END
					}
				}
			}
			for( int i = 0 ; i < celllayout.getCountY() ; i++ )
			{
				for( int j = 0 ; j < celllayout.getCountX() ; j++ )
				{
					View view = celllayout.celllayoutxy[j][i];
					if( view == null )
						continue;
					view.setPivotX( view.getWidth() / 2 );
					view.setPivotY( view.getHeight() / 2 );
					if( offset == 0 || offset == -1 || offset == 1 || ( !isLoop() && ( lOcationOnScreen[0] > ( pageWidth ) || lOcationOnScreen[0] < -( pageWidth ) ) ) )
					{
						view.setAlpha( 1 );
						view.setTranslationX( 0 );
						view.setRotationY( 0 );
					}
					else
					{
						if( Math.abs( offset ) <= 0.5f )//当前页
						{
							view.setAlpha( 1 );
							celllayout.setAlpha( 1f );
							float rotation = -offset * ( 180f /*+ i*22.5f*/);
							if( isLastOrFirstPage )
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
							}
							view.setRotationY( rotation );
						}
						else
						{
							view.setAlpha( 0 );
							view.setRotation( 0 );
						}
					}
				}
			}
			if( offset == 0 || Math.abs( offset ) == 1 )
			{
				celllayout.setAlpha( 1f );
				celllayout.celllayoutxy = null;
			}
		}
	}
	
	@Override
	public void stopEffecf()
	{
		for( int i = 0 ; i < mAllEffectViews.size() ; i++ )
		{
			View mView = mAllEffectViews.get( i );
			if( mView instanceof CellLayout )
			{
				CellLayout mCellLayout = (CellLayout)mView;
				mCellLayout.celllayoutxy = null;
				mCellLayout.setAlpha( 1f );
				mCellLayout.setTranslationX( 0 );
				ShortcutAndWidgetContainer group = (ShortcutAndWidgetContainer)mCellLayout.getChildrenLayout();
				for( int j = 0 ; j < group.getChildCount() ; j++ )
				{
					View mViewChild = group.getChildAt( j );
					if( mViewChild != null )
					{
						mViewChild.setAlpha( 1 );
						mViewChild.setTranslationX( 0 );
						mViewChild.setRotationY( 0 );
					}
				}
			}
		}
		super.stopEffecf();
	}
}
