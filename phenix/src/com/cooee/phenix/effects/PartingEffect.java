package com.cooee.phenix.effects;


import android.content.Context;
import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;

import com.cooee.phenix.CellLayout;
import com.cooee.phenix.ShortcutAndWidgetContainer;


/**
 * 切页特效:聚散
 * @author: lixiaopeng
 */
public class PartingEffect extends EffectInfo
{
	
	float tx = 0;
	float ty = 0;
	
	public PartingEffect(
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
	
	//	@Override
	//	public void getTransformationMatrix(
	//			IEffect curView ,
	//			float offset ,
	//			int pageWidth ,
	//			int pageHeight ,
	//			float distance ,
	//			boolean overScroll ,
	//			boolean overScrollLeft )
	//	{
	//		float absOffset = Math.abs( offset );
	//		PagedViewCellLayout pagedViewCellLayout = null;
	//		if( curView instanceof PagedViewCellLayout )
	//		{
	//			pagedViewCellLayout = (PagedViewCellLayout)curView;
	//			int xCount = pagedViewCellLayout.getCellCountX();
	//			int yCount = pagedViewCellLayout.getCellCountY();
	//			int cellWidth_CellLayout = 0;
	//			int cellHeight_CellLayout = 0;
	//			//行列间隙
	//			int widthGap_CellLayou = 0;
	//			int heightGap_CellLayou = 0;
	//			int allHeight_CellLayou = 0;
	//			if( pagedViewCellLayout.getChildOnPageAt( 0 ) != null )
	//			{
	//				cellWidth_CellLayout = pagedViewCellLayout.getCellWidth();
	//				cellHeight_CellLayout = pagedViewCellLayout.getCellHeight();
	//				widthGap_CellLayou = pagedViewCellLayout.getCellWidthGap();
	//				heightGap_CellLayou = pagedViewCellLayout.getCellHeightGap();
	//				allHeight_CellLayou = cellHeight_CellLayout + heightGap_CellLayou;
	//				Log.v( "" , "------" + widthGap_CellLayou + "," + heightGap_CellLayou );
	//			}
	//			for( int i = 0 ; i < yCount ; i++ )
	//			{
	//				for( int j = 0 ; j < xCount ; j++ )//i5,j4
	//				{
	//					int ij = ( i * xCount + j );
	//					if( offset > 0 )
	//					{
	//						if( pagedViewCellLayout.getChildOnPageAt( ij ) != null )
	//						{
	//							//							Log.v( "" , "------" + pagedViewCellLayout.getChildOnPageAt( ij ).getLeft() );
	//							//							float xX = -pagedViewCellLayout.getChildOnPageAt( ij ).getWidth() / 2 - pagedViewCellLayout.getChildOnPageAt( ij ).getLeft();
	//							//							float yY = (int)( pagedViewCellLayout.getHeight() / 2f - pagedViewCellLayout.getChildOnPageAt( ij ).getTop() );
	//							//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationX( xX * absOffset * 4 );
	//							//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationY( yY * absOffset * 4 );
	//							//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationX( ( -180 * j * offset * 2 + pageWidth * offset ) );
	//							//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationY( ( pageHeight / 2 - 217 * i ) * offset * 2 );
	//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationX( ( -( pageWidth / 3 - 2 * heightGap_CellLayou ) * j * offset * 2 ) + 0.75f * pageWidth * offset );
	//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationY( ( 2 * allHeight_CellLayou - allHeight_CellLayou * i ) * absOffset * 2 );
	//						}
	//					}
	//					else if( offset < 0 )
	//					{
	//						if( pagedViewCellLayout.getChildOnPageAt( ij ) != null )
	//						{
	//							//							float xX = -pagedViewCellLayout.getChildOnPageAt( ij ).getWidth() / 2 - pagedViewCellLayout.getChildOnPageAt( ij ).getLeft();
	//							//							float yY = (int)( pagedViewCellLayout.getHeight() / 2f - pagedViewCellLayout.getChildOnPageAt( ij ).getTop() );
	//							//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationX( -xX * offset * 4 );
	//							//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationY( yY * absOffset * 4 );
	//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationX( ( pageWidth / 3 - 2 * heightGap_CellLayou ) * j * offset * 2 - 0.75f * pageWidth * offset );
	//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationY( ( 2 * allHeight_CellLayou - allHeight_CellLayou * i ) * absOffset * 2 );
	//						}
	//					}
	//				}
	//			}
	//		}
	//		else if( curView instanceof PagedViewGridLayout )
	//		{
	//			PagedViewGridLayout pagedViewGridLayout = (PagedViewGridLayout)curView;
	//			int xCount = pagedViewGridLayout.getCellCountX();
	//			int yCount = pagedViewGridLayout.getCellCountY();
	//			//			float y = 0f;
	//			//			if( pagedViewGridLayout.getChildOnPageAt( 2 ) != null )
	//			//			{
	//			//				y = pagedViewGridLayout.getChildOnPageAt( 2 ).getY() + 1 / 2 * pagedViewGridLayout.getChildOnPageAt( 2 ).getHeight();
	//			//			}
	//			for( int i = 0 ; i < yCount ; i++ )
	//			{
	//				for( int j = 0 ; j < xCount ; j++ )
	//				{
	//					int ij = ( i * xCount + j );
	//					if( offset > 0 )
	//					{
	//						if( pagedViewGridLayout.getChildOnPageAt( ij ) != null )
	//						{
	//							float xX = -pagedViewGridLayout.getChildOnPageAt( ij ).getWidth() / 2 - pagedViewGridLayout.getChildOnPageAt( ij ).getLeft();
	//							float yY = (int)( pagedViewGridLayout.getHeight() / 2f - pagedViewGridLayout.getChildOnPageAt( ij ).getTop() );
	//							//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationX( ( -( pageWidth / 3 - 2 * heightGap_CellLayou ) * j * offset * 2 ) + 0.75f * pageWidth * offset );
	//							//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationY( ( 434 - allHeight_CellLayou * i ) * absOffset * 2 );
	//							pagedViewGridLayout.getChildOnPageAt( ij ).setTranslationX( xX * absOffset * 2 );
	//							pagedViewGridLayout.getChildOnPageAt( ij ).setTranslationY( yY * absOffset * 2 );
	//							//							pagedViewGridLayout.getChildOnPageAt( ij ).setTranslationX( ( -16 - 364 * j ) * offset * 2f + 0.5f * pageWidth * offset );
	//							//							pagedViewGridLayout.getChildOnPageAt( ij ).setTranslationY( ( 434 - 336 * i ) * offset * 2f );
	//						}
	//					}
	//					else if( offset < 0 )
	//					{
	//						if( pagedViewGridLayout.getChildOnPageAt( ij ) != null )
	//						{
	//							//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationX( ( pageWidth / 3 - 2 * heightGap_CellLayou ) * j * offset * 2 - 0.75f * pageWidth * offset );
	//							//							pagedViewCellLayout.getChildOnPageAt( ij ).setTranslationY( ( 434 - allHeight_CellLayou * i ) * absOffset * 2 );
	//							pagedViewGridLayout.getChildOnPageAt( ij ).setTranslationX( ( 16 + 364 * j ) * offset * 2f - 0.5f * pageWidth * offset );
	//							pagedViewGridLayout.getChildOnPageAt( ij ).setTranslationY( ( 434 - 336 * i ) * absOffset * 2 );
	//						}
	//					}
	//				}
	//			}
	//		}
	//		else if( curView instanceof CellLayout )
	//		{
	//			CellLayout cellLayout = null;
	//			cellLayout = (CellLayout)curView;
	//			ShortcutAndWidgetContainer group = (ShortcutAndWidgetContainer)cellLayout.getChildrenLayout();
	//			int xCount = cellLayout.getCountX();
	//			int yCount = cellLayout.getCountY();
	//			int iconWidth = 0;
	//			int iconHeight = 0;
	//			float y = 0f;
	//			//			if( andWidgetContainer.getChildAt( 0 , 2 ) != null )
	//			//			{
	//			//				y = andWidgetContainer.getChildAt( 0 , 2 ).getY() + 1 / 2 * andWidgetContainer.getChildAt( 0 , 2 ).getHeight();
	//			//			}
	//			//			else
	//			//			{
	//			//				y = 434;
	//			//			}
	//			for( int i = 0 ; i < group.getChildCount() ; i++ )
	//			{
	//				//				for( int j = 0 ; j < xCount ; j++ )
	//				//				{
	//				//					int ij = ( i * xCount + j );
	//				if( group.getChildAt( i ) != null )
	//				{
	//					iconWidth = group.getChildAt( i ).getWidth();
	//					iconHeight = group.getChildAt( i ).getHeight();
	//				}
	//				//				if( offset > 0 )
	//				//				{
	//				if( offset > 0 )
	//				{
	//					if( group.getChildAt( i ) != null )
	//					{
	//						float xX = -group.getChildAt( i ).getWidth() / 2 - group.getChildAt( i ).getLeft();
	//						float yY = group.getHeight() / 2f - group.getChildAt( i ).getTop();
	//						group.getChildAt( i ).setTranslationX( xX * offset * 2 + pageWidth * offset );
	//						group.getChildAt( i ).setTranslationY( yY * absOffset * 2 );
	//					}
	//				}
	//				else
	//				{
	//					if( group.getChildAt( i ) != null )
	//					{
	//						float xX = -group.getChildAt( i ).getWidth() / 2 - group.getChildAt( i ).getLeft();
	//						float yY = group.getHeight() / 2f - group.getChildAt( i ).getTop();
	//						group.getChildAt( i ).setTranslationX( xX * absOffset * 2 + pageWidth * absOffset );
	//						group.getChildAt( i ).setTranslationY( yY * absOffset * 2 );
	//					}
	//				}
	//			}
	//		}
	//	}
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
		float absOffset = Math.abs( offset );
		if( curView instanceof CellLayout )
		{
			CellLayout cellLayout = null;
			cellLayout = (CellLayout)curView;
			if( mAllEffectViews != null && !mAllEffectViews.contains( cellLayout ) )
			{
				mAllEffectViews.add( cellLayout );
			}
			ShortcutAndWidgetContainer group = (ShortcutAndWidgetContainer)cellLayout.getChildrenLayout();
			if( isLastOrFirstPage )
			{
				curView.setTranslationX( -offset * pageWidth * maxScroll );
			}
			else
			{
				curView.setTranslationX( 0 );
			}
			for( int i = 0 ; i < group.getChildCount() ; i++ )
			{
				if( group.getChildAt( i ) != null )
				{
					if( offset == 0 || absOffset == 1 )
					{
						group.getChildAt( i ).setTranslationX( 0 );
						group.getChildAt( i ).setTranslationY( 0 );
					}
					else
					{
						tx = ( -group.getChildAt( i ).getWidth() / 2 - group.getChildAt( i ).getLeft() ) * offset * 2 + pageWidth * absOffset;
						ty = ( group.getHeight() / 2f - group.getChildAt( i ).getTop() ) * absOffset * 2;
						group.getChildAt( i ).setTranslationX( tx );
						group.getChildAt( i ).setTranslationY( ty );
					}
				}
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
				mCellLayout.setTranslationX( 0 );
				ShortcutAndWidgetContainer group = (ShortcutAndWidgetContainer)mCellLayout.getChildrenLayout();
				for( int j = 0 ; j < group.getChildCount() ; j++ )
				{
					View mViewChild = group.getChildAt( j );
					if( mViewChild != null )
					{
						mViewChild.setTranslationX( 0 );
						mViewChild.setTranslationY( 0 );
					}
				}
			}
		}
		super.stopEffecf();
	}
}
