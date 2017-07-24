package com.cooee.phenix.editmode.entryitemview;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cooee.phenix.R;


public class EntryItemView extends LinearLayout
{
	
	protected Drawable mSelectDrawable = null;
	protected ImageView mImageView = null;
	
	public EntryItemView(
			Context context )
	{
		super( context );
	}
	
	public EntryItemView(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
	}
	
	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mSelectDrawable = getContext().getResources().getDrawable( R.drawable.edit_select_bg );
		mImageView = (ImageView)findViewById( R.id.edit_entry_item_imageView );
	}
	
	/**
	 * 判断EntryItemView动画是否结束，现有位移Y是否置位0，后期再有动画再扩展
	 * @return
	 */
	public boolean isAnimRunning()
	{
		boolean isTranslation = this.getTranslationY() == 0;//位移是否为0
		return isTranslation;
	}
	
	/**
	 * 复位EntryItemView执行动画前的参数，目前有TranslationY()，后期好扩展
	 * @param itemViewHeight TranslationY的初始值
	 */
	public void resetViewData(
			int itemViewHeight )
	{
		this.setTranslationY( itemViewHeight );
	}
	
	@Override
	protected void onLayout(
			boolean changed ,
			int l ,
			int t ,
			int r ,
			int b )
	{
		super.onLayout( changed , l , t , r , b );
		int bgWidth = mImageView.getWidth() / 2;
		int bgHeight = mImageView.getHeight() / 2;
		int left = ( this.getWidth() - mImageView.getWidth() ) / 2 + mImageView.getWidth() / 2;
		int top = mImageView.getTop();
		int right = left + bgWidth;
		int bottom = top + bgHeight;
		mSelectDrawable.setBounds( left , top , right , bottom );
		invalidate();
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		// TODO Auto-generated method stub
		//		widthMeasureSpec = MeasureSpec.makeMeasureSpec(  getResources().getDisplayMetrics().widthPixels - margin * 2 ) / EditModeEntity.ITME_SUM , MeasureSpec.EXACTLY );
		super.onMeasure( widthMeasureSpec , heightMeasureSpec );
	}
	
	private boolean isDrawSelectBg()
	{
		return mSelectDrawable != null && this.isSelected();
	}
	
	@Override
	protected void dispatchDraw(
			Canvas canvas )
	{
		super.dispatchDraw( canvas );
		if( isDrawSelectBg() )
		{
			mSelectDrawable.draw( canvas );
		}
	}
	
	public String getText()
	{
		TextView tv = (TextView)findViewById( R.id.edit_entry_item_textView );
		return tv.getText().toString();
	}
}
