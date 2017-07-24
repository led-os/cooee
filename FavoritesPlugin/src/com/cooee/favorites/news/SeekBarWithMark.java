package com.cooee.favorites.news;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cooee.favorites.R;


/**
 * 下方带有刻度的SeekBar
 *
 * @author EC
 */
public class SeekBarWithMark extends LinearLayout
{
	
	private static final String TAG = SeekBarWithMark.class.getSimpleName();
	private static final boolean DEBUG = false;
	//
	private SeekBar mSeekBar;
	//装载刻度框的Layout
	private LinearLayout mBottomLL;
	//根据 mMarkItemNum 来选择，整除(mMarkItemNum - 1)最好
	private int MAX = 80;
	//刻度的数目，默认9
	private int mMarkItemNum = 9;
	//相邻刻度的间距
	private int mSpacing = MAX / ( mMarkItemNum - 1 );
	//当前选中的刻度
	private int nowMarkItem;
	//刻度的下标名称数组
	private String[] mMarkDescArray;
	//
	private OnSelectItemListener mSelectItemListener;
	//是否第一次渲染
	private boolean isFirstInflate = true;
	//SeekBar的ProgressDrawable
	private Drawable mProgressDrawable;
	//SeekBar的Thumb
	private Drawable mThumbDrawable;
	//刻度的文字大小
	private float mMarkTextSize = 13;
	
	private SeekBarWithMark(
			Context context )
	{
		super( context );
	}
	
	public SeekBarWithMark(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public SeekBarWithMark(
			Context context ,
			AttributeSet attrs ,
			int defStyleAttr )
	{
		super( context , attrs , defStyleAttr );
		this.mMarkItemNum = 4;//a.getInteger( attr , 9 );
		this.MAX = ( mMarkItemNum - 1 ) * 10;
		this.mSpacing = MAX / ( mMarkItemNum - 1 );
		this.mMarkDescArray = new String[]{
				getResources().getString( R.string.font_size_small ) ,
				getResources().getString( R.string.font_size_middle ) ,
				getResources().getString( R.string.font_size_large ) ,
				getResources().getString( R.string.font_size_largest ) };
		this.mProgressDrawable = getResources().getDrawable( R.drawable.slider_bg );
		this.mThumbDrawable = getResources().getDrawable( R.drawable.thumb );
		this.mMarkTextSize = getResources().getDimension( R.dimen.font_type_size );
	}
	
	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		init();
	}
	
	private void init()
	{
		//
		if( this.mMarkDescArray.length < this.mMarkItemNum )
		{
			throw new RuntimeException( "刻度的下标的名称数组length不能小于刻度的数目" );
		}
		//
		this.setOrientation( LinearLayout.VERTICAL );
		this.setGravity( Gravity.CENTER );
		this.mSeekBar = (SeekBar)findViewById( R.id.seekBar );
		this.mSeekBar.setMax( MAX );
		if( mProgressDrawable != null )
		{
			this.mSeekBar.setProgressDrawable( mProgressDrawable );
		}
		//设置Thumb
		if( mThumbDrawable != null )
		{
			this.mSeekBar.setThumb( mThumbDrawable );
		}
		this.mSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
			
			private int shouldInProgress;
			
			@Override
			public void onProgressChanged(
					SeekBar seekBar ,
					int progress ,
					boolean fromUser )
			{
				//
				nowMarkItem = Math.round( progress * 1.0f / mSpacing );
				shouldInProgress = mSpacing * nowMarkItem;
				//
				SeekBarWithMark.this.mSeekBar.setProgress( shouldInProgress );
				//
				if( DEBUG )
				{
					Log.e( TAG , "progress---" + progress );
				}
			}
			
			@Override
			public void onStartTrackingTouch(
					SeekBar seekBar )
			{
			}
			
			@Override
			public void onStopTrackingTouch(
					SeekBar seekBar )
			{
				if( DEBUG )
				{
					Log.e( TAG , "shouldInProgress---" + shouldInProgress );
				}
				//
				if( mSelectItemListener != null )
				{
					mSelectItemListener.selectItem( nowMarkItem , mMarkDescArray[nowMarkItem] );
				}
			}
		} );
		////
		mBottomLL = new LinearLayout( getContext() );
		mBottomLL.setOrientation( LinearLayout.HORIZONTAL );
		LayoutParams mBottomLLLp = new LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT );
		mBottomLLLp.setMargins( 0 , (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP , 4 , getResources().getDisplayMetrics() ) , 0 , 0 );
		mBottomLL.setLayoutParams( mBottomLLLp );
		//设置和mSeekBar的padding值一致
		mBottomLL.setPadding( this.mSeekBar.getPaddingLeft() , 0 , this.mSeekBar.getPaddingRight() , 0 );
		//
		this.addView( mBottomLL );
		//
		addAllMarkItem();
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
		//
		if( isFirstInflate )
		{
			isFirstInflate = false;
			//
			ViewGroup bottomLL = (ViewGroup)this.getChildAt( 1 );
			View view = bottomLL.getChildAt( 0 );
			//
			int bottomLLWidth = bottomLL.getWidth();
			int width = view.getWidth();
			ViewGroup.LayoutParams layoutParams = mSeekBar.getLayoutParams();
			layoutParams.width = bottomLLWidth - width;
			this.mSeekBar.setLayoutParams( layoutParams );
		}
	}
	
	/**
	 * 添加 刻度
	 */
	private void addAllMarkItem()
	{
		if( mBottomLL == null )
		{
			throw new RuntimeException( "装载刻度框的Layout不能为null" );
		}
		//
		mBottomLL.removeAllViews();
		TextView textView = null;
		LayoutParams tvLp = null;
		for( int i = 0 ; i < mMarkItemNum ; i++ )
		{
			textView = new TextView( getContext() );
			//这个width只能设为0dp，可不能设为 LayoutParams.WRAP_CONTENT ，否则它就会使得textView不都是一般大了，会影响刻度精准
			tvLp = new LayoutParams( 0 , LayoutParams.WRAP_CONTENT );
			tvLp.weight = 1;
			tvLp.gravity = Gravity.CENTER;
			textView.setGravity( Gravity.CENTER );
			textView.setLayoutParams( tvLp );
			textView.setTextSize( TypedValue.COMPLEX_UNIT_PX , mMarkTextSize );
			textView.setText( mMarkDescArray[i] );
			textView.setTextColor( 0xff8c8c8c );
			mBottomLL.addView( textView );
		}
	}
	
	public SeekBar getSeekBar()
	{
		return mSeekBar;
	}
	
	/**
	 * 设置监听选中刻度
	 *
	 * @param l
	 */
	public void setOnSelectItemListener(
			OnSelectItemListener l )
	{
		this.mSelectItemListener = l;
	}
	
	public interface OnSelectItemListener
	{
		
		void selectItem(
				int nowSelectItemNum ,
				String val );
	}
	
	@Override
	public void setEnabled(
			boolean enabled )
	{
		super.setEnabled( enabled );
		mSeekBar.setEnabled( enabled );
	}
	
	/**
	 * 设置选中
	 *
	 * @param selectItemNum 0代表第一个刻度，1代表第二个刻度，以此类推
	 */
	public void selectMarkItem(
			int selectItemNum )
	{
		//设置当前选中
		nowMarkItem = selectItemNum;
		//
		int shouldInProgress = mSpacing * selectItemNum;
		//
		mSeekBar.setProgress( shouldInProgress );
	}
	
	/**
	 * 当前选中刻度
	 *
	 * @return
	 */
	public int getNowMarkItem()
	{
		return nowMarkItem;
	}
}
