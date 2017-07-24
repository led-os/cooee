package com.cooee.phenix.effects;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;

import com.cooee.phenix.CellLayout;
import com.cooee.phenix.ShortcutAndWidgetContainer;


@SuppressLint( "NewApi" )
public class SnakeEffect extends EffectInfo
{
	
	int count_CellLayout = 0;
	//一共几行几列
	int xCount_CellLayout = 0;
	int yCount_CellLayout = 0;
	//每个格子宽高
	int cellWidth_CellLayout = 0;
	int cellHeight_CellLayout = 0;
	//行列间隙
	int widthGap_CellLayou = 0;
	int heightGap_CellLayou = 0;
	//每行宽度
	int allWidth_CellLayou = 0;
	//每拐弯一次 需要经历的Y方向变化
	int allHeight_CellLayou = 0;
	int mLong = 0;
	int mLongYet = 0;
	
	public SnakeEffect(
			int id ,
			IEffect effect )
	{
		super( id , effect );
	}
	
	public SnakeEffect(
			int id ,
			boolean flag ,
			IEffect effect )
	{
		super( id , flag , effect );
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean getCellLayoutChildStaticTransformation(
			ViewGroup viewGroup ,
			View viewiew ,
			Transformation transformation ,
			Camera camera ,
			float offset )
	{
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Scroller getScroller(
			Context context )
	{
		// TODO Auto-generated method stub
		return new Scroller( context );
	}
	
	@Override
	public int getSnapTime()
	{
		// TODO Auto-generated method stub
		return 200;
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
		//		Log.v( "lvjiangbin" , "overScroll = " + overScroll );//是否往右滑动
		//		Log.v( "lvjiangbin" , "offset = " + offset );//是否往右滑动
		allWidth_CellLayou = ( cellWidth_CellLayout + widthGap_CellLayou ) * ( xCount_CellLayout - 1 );
		allHeight_CellLayou = cellHeight_CellLayout + heightGap_CellLayou;
		mLong = allWidth_CellLayou * yCount_CellLayout + ( yCount_CellLayout - 1 ) * ( cellHeight_CellLayout + heightGap_CellLayou );//总线路长度
		mLongYet = (int)( mLong * -offset );//偏移长度
		if( effect instanceof CellLayout )
		{
			if( mAllEffectViews != null && !mAllEffectViews.contains( effect ) )
			{
				mAllEffectViews.add( (CellLayout)effect );
			}
			if( !isLastOrFirstPage )
			{
				( effect ).setTranslationX( offset * pageWidth );
			}
			else
			{
				( effect ).setTranslationX( 0 );
			}
			View pagedViewIcon = null;
			ViewGroup pagedViewCellLayoutChildren = null;
			pagedViewCellLayoutChildren = effect.getChildrenLayout();
			count_CellLayout = effect.getPageChildCount();
			xCount_CellLayout = effect.getCountX();
			yCount_CellLayout = effect.getCountY();
			cellWidth_CellLayout = effect.getCellWidth();
			cellHeight_CellLayout = effect.getCellHeight();
			widthGap_CellLayou = effect.getCellWidthGap();
			heightGap_CellLayou = effect.getCellHeightGap();
			allWidth_CellLayou = ( cellWidth_CellLayout + widthGap_CellLayou ) * ( xCount_CellLayout - 1 );
			allHeight_CellLayou = cellHeight_CellLayout + heightGap_CellLayou;
			mLong = allWidth_CellLayou * yCount_CellLayout + ( yCount_CellLayout - 1 ) * ( cellHeight_CellLayout + heightGap_CellLayou ) + cellHeight_CellLayout + heightGap_CellLayou;//总线路长度
			mLongYet = (int)( mLong * -offset );
			for( int i = 0 ; i < count_CellLayout ; i++ ) //in
			{
				pagedViewIcon = pagedViewCellLayoutChildren.getChildAt( i );
				if( pagedViewIcon == null )
					continue;
				//每个图标在布局中 几行几列  0 0开始
				int X_CellLayouChildren = effect.getChildrenCellX( i );
				int Y_CellLayouChildren = effect.getChildrenCellY( i );
				//当前图标所在长度位置
				int mLongYetChild = X_CellLayouChildren * ( cellWidth_CellLayout + widthGap_CellLayou ) + Y_CellLayouChildren * ( cellHeight_CellLayout + heightGap_CellLayou ) + Y_CellLayouChildren * allWidth_CellLayou;
				/*  路线如下
				------------------
								 |
				------------------
				|
				------------------
								 |
				------------------
				|
				------------------
				 */
				if( Y_CellLayouChildren % 2 != 0 )//奇数行 长度应当从右边算起
				{
					mLongYetChild = ( xCount_CellLayout - X_CellLayouChildren - 1 ) * ( cellWidth_CellLayout + widthGap_CellLayou ) + Y_CellLayouChildren * ( cellHeight_CellLayout + heightGap_CellLayou ) + Y_CellLayouChildren * allWidth_CellLayou;
				}
				//给长度加上偏移量
				mLongYetChild += mLongYet;
				//根据目前长度得出X 和 Y偏移量   (我们默认一行包含  一行+拐弯   先计算在第几行  然后在计算 是在拐弯还是没有拐弯)
				//行数
				int xx = mLongYetChild / ( allWidth_CellLayou + allHeight_CellLayou );
				//在这一行上的位置
				int xxX = mLongYetChild % ( allWidth_CellLayou + allHeight_CellLayou );
				//是否在拐弯区域
				boolean isInCorner = xxX > allWidth_CellLayou;
				//确定要偏移的ＸＹ坐标
				int x_offset = 0;
				int y_offset = 0;
				if( offset == 0 || offset <= -0.85f || offset >= 0.85f )
				{
					pagedViewIcon.setTranslationX( 0 );
					pagedViewIcon.setTranslationY( 0 );
					effect.setTranslationX( 0 );
				}
				if( offset != 0 && Math.abs( offset ) != 1 )
				{
					if( isInCorner )
					{
						//计算Y方向偏移
						y_offset = ( xx * allHeight_CellLayou + ( xxX - allWidth_CellLayou/*拐弯处Y偏移量*/) ) - pagedViewIcon.getTop();
						//计算X方向偏移
						if( xx % 2 != 0 )
						{
							x_offset = ( 0 ) - pagedViewIcon.getLeft();
						}
						else
						{
							x_offset = allWidth_CellLayou - pagedViewIcon.getLeft();
						}
					}
					else
					{
						y_offset = ( xx * allHeight_CellLayou ) - pagedViewIcon.getTop();
						if( xx % 2 != 0 )
						{
							x_offset = ( allWidth_CellLayou - xxX ) - pagedViewIcon.getLeft();
						}
						else
						{
							x_offset = ( xxX ) - pagedViewIcon.getLeft();
						}
					}
					pagedViewIcon.setTranslationX( x_offset );
					pagedViewIcon.setTranslationY( y_offset );
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
