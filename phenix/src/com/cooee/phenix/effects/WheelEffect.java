package com.cooee.phenix.effects;


import android.content.Context;
import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;

import com.cooee.phenix.CellLayout;
import com.cooee.phenix.ShortcutAndWidgetContainer;


// 车轮
public class WheelEffect extends EffectInfo
{
	
	int[] lOcationOnScreen = new int[2];
	int count = 0;
	float x = 0;
	float y = 0;
	float angle = 0;
	float tempx = 0f;//目标坐标距离
	float tempy = 0f;
	float radius = 0;
	float offsetX2 = 0;
	float rotation = 0;
	float tx = 0;
	float ty = 0;
	
	public WheelEffect(
			int id ,
			IEffect effect )
	{
		super( id , effect );
	}
	
	public WheelEffect(
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
	//		int count = 0;
	//		float x = 0;
	//		float y = 0;
	//		float angle = 0;
	//		float tempx = 0f;//目标坐标距离
	//		float tempy = 0f;
	//		float radius = 0;
	//		int[] lOcationOnScreen = new int[2];
	//		curView.getLocationOnScreen( lOcationOnScreen );
	//		if( curView instanceof PagedViewCellLayout )
	//		{
	//			count = ( (PagedViewCellLayout)curView ).getPageChildCount();
	//		}
	//		else if( curView instanceof PagedViewGridLayout )
	//		{
	//			count = ( (PagedViewGridLayout)curView ).getPageChildCount();
	//		}
	//		else if( curView instanceof CellLayout )
	//		{
	//			LayoutParams mLayoutParams;
	//			CellLayout celllayout;
	//			celllayout = (CellLayout)curView;
	//			mLayoutParams = celllayout.getLayoutParams();
	//			ShortcutAndWidgetContainer mShortcutAndWidgetContainer = celllayout.getShortcutsAndWidgets();
	//			count = mShortcutAndWidgetContainer.getChildCount();
	//			angle = 360f / count;
	//			tempx = 0f;//目标坐标距离
	//			tempy = 0f;
	//			radius = pageWidth / 5f;
	//			int xCount = celllayout.getCountX();
	//			int yCount = celllayout.getCountY();
	//			int xWidth = celllayout.getCellWidth();
	//			int xHeight = celllayout.getCellHeight();
	//			float offsetX2 = Math.abs( offset * 3f );
	//			for( int i = 0 ; i < count ; i++ )
	//			{
	//				View child = mShortcutAndWidgetContainer.getChildAt( i );
	//				if( child == null )
	//					continue;
	//				x = child.getLeft();
	//				y = child.getTop();
	//				tempx = (float)( radius * Math.cos( ( angle * i - 360 * offset ) * ( Math.PI / 180f ) ) );
	//				tempy = (float)( radius * Math.sin( ( angle * i - 360 * offset ) * ( Math.PI / 180f ) ) );
	//				child.setPivotX( child.getWidth() / 2 );
	//				child.setPivotY( 0 );
	//				if( Math.abs( offsetX2 ) < 1 )
	//				{
	//					child.setRotation( ( angle * i + 270f - 360 * offset ) * offsetX2 );
	//					child.setTranslationX( ( tempx + pageWidth / 2 - x - child.getWidth() / 2 ) * offsetX2 );
	//					child.setTranslationY( ( tempy + pageHeight / 2 - y ) * offsetX2 );
	//					//						curView.setRotation( -360 * offset );
	//				}
	//				else
	//				{
	//					child.setRotation( ( angle * i + 270f - 360 * offset ) );
	//					child.setTranslationX( ( tempx + pageWidth / 2 - x - child.getWidth() / 2 ) );
	//					child.setTranslationY( ( tempy + pageHeight / 2 - y ) );
	//					//						curView.setRotation( -360 * offset );
	//				}
	//				if( offset == 0 || offset == -1 || offset == 1 || lOcationOnScreen[0] >= ( pageWidth - 20 ) || lOcationOnScreen[0] <= -( pageWidth - 20 ) )
	//				{
	//					child.setRotation( 0 );
	//					child.setTranslationX( 0 );
	//					child.setTranslationY( 0 );
	//				}
	//			}
	//		}
	//		//////////////////////////////////////////////////////////
	//		//		angle = 360f / count;
	//		//		tempx = 0f;//目标坐标距离
	//		//		tempy = 0f;
	//		//		radius = pageWidth / 4f;
	//		//		float offsetX2 = Math.abs( offset * 3f );
	//		//		for( int i = 0 ; i < count ; i++ )
	//		//		{
	//		//			View child = null;
	//		//			if( curView instanceof PagedViewCellLayout )
	//		//			{
	//		//				child = ( (PagedViewCellLayout)curView ).getChildOnPageAt( i );
	//		//			}
	//		//			else if( curView instanceof PagedViewGridLayout )
	//		//			{
	//		//				child = ( (PagedViewGridLayout)curView ).getChildOnPageAt( i );
	//		//			}
	//		//			else
	//		//			{
	//		//				return;
	//		//			}
	//		//			if( child == null )
	//		//				continue;
	//		//			x = child.getLeft();
	//		//			y = child.getTop();
	//		//			tempx = (float)( radius * Math.cos( ( angle * i - 360 * offset ) * ( Math.PI / 180f ) ) );
	//		//			tempy = (float)( radius * Math.sin( ( angle * i - 360 * offset ) * ( Math.PI / 180f ) ) );
	//		//			if( i == 0 )
	//		//			{
	//		//				Log.v( "lvjiangbin" , "getTranslationX = " + child.getTranslationX() );
	//		//				Log.v( "lvjiangbin" , "xxx = " + x );
	//		//				Log.v( "lvjiangbin" , "offsetX2 = " + offsetX2 );
	//		//				Log.v( "lvjiangbin" , "offset = " + offset );
	//		//				Log.v( "lvjiangbin" , "tempx = " + tempx );
	//		//				Log.v( "lvjiangbin" , "Rotation = " + child.getRotation() );
	//		//				Log.v( "lvjiangbin" , "pageWidth = " + pageWidth );
	//		//				Log.v( "lvjiangbin" , "lOcationOnScreen[0] = " + lOcationOnScreen[0] );
	//		//				Log.v( "lvjiangbin" , "lOcationOnScreen[1] = " + lOcationOnScreen[1] );
	//		//				//					Log.v( "lvjiangbin" , "((PagedViewCellLayout) curView).getLeft() = " + ((PagedViewCellLayout) curView).getLeft() );
	//		//			}
	//		//			//				curView.setPivotX( curView.getWidth() / 2 - child.getWidth() / 2 );
	//		//			//				curView.setPivotY( curView.getHeight() / 2 );
	//		//			child.setPivotX( child.getWidth() / 2 );
	//		//			child.setPivotY( 0 );
	//		//			if( Math.abs( offsetX2 ) < 1 )
	//		//			{
	//		//				child.setRotation( ( angle * i + 270f - 360 * offset ) * offsetX2 );
	//		//				child.setTranslationX( ( tempx + pageWidth / 2 - x - child.getWidth() / 2 ) * offsetX2 );
	//		//				child.setTranslationY( ( tempy + pageHeight / 2 - y ) * offsetX2 );
	//		//				//					curView.setRotation( -360 * offset );
	//		//			}
	//		//			else
	//		//			{
	//		//				child.setRotation( ( angle * i + 270f - 360 * offset ) );
	//		//				child.setTranslationX( ( tempx + pageWidth / 2 - x - child.getWidth() / 2 ) );
	//		//				child.setTranslationY( ( tempy + pageHeight / 2 - y ) );
	//		//				//					curView.setRotation( -360 * offset );
	//		//			}
	//		//			if( offset == 0 || offset == -1 || offset == 1 || lOcationOnScreen[0] >= ( pageWidth - 20 ) || lOcationOnScreen[0] <= -( pageWidth - 20 ) )
	//		//			{
	//		//				child.setRotation( 0 );
	//		//				child.setTranslationX( 0 );
	//		//				child.setTranslationY( 0 );
	//		//			}
	//		//		}
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
		curView.getLocationOnScreen( lOcationOnScreen );
		if( curView instanceof CellLayout )
		{
			if( isLastOrFirstPage )
			{
				curView.setTranslationX( -offset * pageWidth );
			}
			else
			{
				curView.setTranslationX( 0 );
			}
			CellLayout celllayout;
			celllayout = (CellLayout)curView;
			if( mAllEffectViews != null && !mAllEffectViews.contains( celllayout ) )
			{
				mAllEffectViews.add( celllayout );
			}
			ShortcutAndWidgetContainer mShortcutAndWidgetContainer = celllayout.getShortcutsAndWidgets();
			count = mShortcutAndWidgetContainer.getChildCount();
			angle = 360f / count;
			tempx = 0f;//目标坐标距离
			tempy = 0f;
			radius = pageWidth / 20f;
			offsetX2 = Math.abs( offset * 3f );
			for( int i = 0 ; i < count ; i++ )
			{
				View child = mShortcutAndWidgetContainer.getChildAt( i );
				if( child == null )
					continue;
				if( offset == 0 || offset == -1 || offset == 1 || ( !isLoop() && ( lOcationOnScreen[0] >= ( pageWidth - 20 ) || lOcationOnScreen[0] <= -( pageWidth - 20 ) ) ) )
				{
					child.setRotation( 0 );
					child.setTranslationX( 0 );
					child.setTranslationY( 0 );
				}
				else
				{
					x = child.getLeft();
					y = child.getTop();
					//					tempx = (float)( radius * Math.cos( ( angle * i - 360 * offset ) * ( Math.PI / 180f ) ) );
					//					tempy = (float)( radius * Math.sin( ( angle * i - 360 * offset ) * ( Math.PI / 180f ) ) );
					child.setPivotX( child.getWidth() / 2 );
					child.setPivotY( 0 );
					if( Math.abs( offsetX2 ) < 1 )
					{
						rotation = ( angle * i + 270f - 360 * offset ) * offsetX2;
						tx = ( tempx + pageWidth / 2 - x - child.getWidth() / 2 ) * offsetX2;
						ty = ( tempy + pageHeight / 2 - y ) * offsetX2;
					}
					else
					{
						rotation = angle * i + 270f - 360 * offset;
						tx = tempx + pageWidth / 2 - x - child.getWidth() / 2;
						ty = tempy + pageHeight / 2 - y;
					}
					child.setRotation( rotation );
					child.setTranslationX( tx );
					child.setTranslationY( ty );
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
						mViewChild.setRotation( 0 );
					}
				}
			}
		}
		super.stopEffecf();
	}
}
