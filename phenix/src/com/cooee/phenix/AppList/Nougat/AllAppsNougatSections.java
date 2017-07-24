package com.cooee.phenix.AppList.Nougat;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cooee.phenix.R;
import com.cooee.phenix.AppList.Marshmallow.AlphabeticalAppsList.SectionInfo;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.util.Tools;


public class AllAppsNougatSections extends View
{

	
	private int NOMAL_COLOR = 0xffafafaf;
	private int HIGHLIGHT_COLOR = 0xffffffff;
	OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	float scopeHeiht;
	float singleHeight = 0;
	float verticalPadding;
	int verticalMargin = 0;
	private ArrayList<SectionInfo> mOrderedLetters;
	int choose = -1;
	Paint paint = new Paint();
	float fontHeight;
	float fontBottom;
	int fontSize = 10;
	private String mFirstVisiblitySection = null;
	private String mLastVisiblitySection = null;
	private int preSectionSize = 0;
	private int preHeight = 0;
	private Bitmap favoritesSectionSelected;
	private Bitmap favoritesSectionNormal;
	
	public AllAppsNougatSections(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		NOMAL_COLOR = context.getResources().getColor( R.color.all_apps_grid_section_text_unselected_color );
		HIGHLIGHT_COLOR = context.getResources().getColor( R.color.all_apps_grid_section_text_selected_color );
		fontSize = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_nougat_section_size );
		verticalMargin = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_nougat_section_vertical_margin );
	}
	
	public AllAppsNougatSections(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public AllAppsNougatSections(
			Context context )
	{
		this( context , null );
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		// TODO Auto-generated method stub
		super.onMeasure( widthMeasureSpec , heightMeasureSpec );
		int height = MeasureSpec.getSize( heightMeasureSpec );
		if( preHeight != height )
		{
			calculateHeight();
			preHeight = height;
		}
	}
	
	public void setSections(
			List<SectionInfo> orderedLetters )
	{
		mOrderedLetters = (ArrayList<SectionInfo>)orderedLetters;
		paint.setAntiAlias( true );
		paint.setTextSize( fontSize );
		FontMetrics fm = paint.getFontMetrics();
		// 计算出文字的大小
		fontHeight = (float)Math.ceil( fm.bottom - fm.top );
		preSectionSize = orderedLetters.size();
		fontBottom = paint.getFontMetrics().bottom;
		if( LauncherDefaultConfig.SWITCH_ENABLE_NOUGAT_MAINMENU_FAVORITES_APPS )
		{
			favoritesSectionSelected = Tools.drawableToBitmap( getResources().getDrawable( R.drawable.applist_nougat_favorites_section_selected ) , (int)fontHeight , (int)fontHeight );
			favoritesSectionNormal = Tools.drawableToBitmap( getResources().getDrawable( R.drawable.applist_nougat_favorites_section_normal ) , (int)fontHeight , (int)fontHeight );
		}
		if( preSectionSize > 0 )
		{
			calculateHeight();
		}
	}
	
	public void setVisiblityIndex(
			String firstIndex ,
			String lastIndex )
	{
		this.mFirstVisiblitySection = firstIndex;
		this.mLastVisiblitySection = lastIndex;
	}
	
	private void calculateHeight()
	{
		verticalPadding = ( getMeasuredHeight() - verticalMargin * 2 - mOrderedLetters.size() * fontHeight ) / mOrderedLetters.size();
		singleHeight = fontHeight + verticalPadding;
		scopeHeiht = mOrderedLetters.size() * fontHeight + ( mOrderedLetters.size() - 1 ) * verticalPadding;
	}
	
	@Override
	protected void onDraw(
			Canvas canvas )
	{
		super.onDraw( canvas );
		if( mOrderedLetters == null || mOrderedLetters.size() == 0 )
		{
			return;
		}
		if( mFirstVisiblitySection == null || mLastVisiblitySection == null )
		{
			return;
		}
		if( mOrderedLetters != null && preSectionSize != mOrderedLetters.size() )
		{
			preSectionSize = mOrderedLetters.size();
			calculateHeight();
		}
		int width = getWidth();
		for( int i = 0 ; i < mOrderedLetters.size() ; i++ )
		{

			SectionInfo info = mOrderedLetters.get( i );
			float baseY = ( getMeasuredHeight() - scopeHeiht ) / 2 + ( singleHeight - verticalPadding - fontBottom ) + i * singleHeight;
			String section = info.firstAppItem.sectionName;
			boolean isSelected = false;
			if( ( "#".equals( section ) && "#".equals(
					mLastVisiblitySection ) ) || ( section.compareTo( mFirstVisiblitySection ) >= 0 && ( "#".equals( mLastVisiblitySection ) || section.compareTo( mLastVisiblitySection ) <= 0 ) ) )
			{
				paint.setColor( HIGHLIGHT_COLOR );
				isSelected = true;
			}
			else
			{
				paint.setColor( NOMAL_COLOR );
				isSelected = false;
			}
			if( section.equals( AllAppsNougatGridAdapter.FAVORITES_TAG ) && favoritesSectionSelected != null && favoritesSectionNormal != null )
			{
				float xPos = ( width - favoritesSectionSelected.getWidth() ) / 2;
				baseY = baseY - favoritesSectionSelected.getHeight();
				if( baseY < 0 )
				{
					baseY = 0;
				}
				paint.setColor( HIGHLIGHT_COLOR );
				if( isSelected )
				{
					canvas.drawBitmap( favoritesSectionSelected , xPos , baseY , paint );
				}
				else
				{
					canvas.drawBitmap( favoritesSectionNormal , xPos , baseY , paint );
				}
			}
			else
			{
				float xPos = ( width - paint.measureText( section ) ) / 2;
				canvas.drawText( section , xPos , baseY , paint );
			}

		}
	}
	
	@Override
	public boolean dispatchTouchEvent(
			MotionEvent event )
	{
		final int action = event.getAction();
		final float y = event.getY();
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		final int c = (int)( ( y - ( getHeight() - scopeHeiht ) / 2 ) / singleHeight );
		switch( action )
		{
			case MotionEvent.ACTION_DOWN:
				if( oldChoose != c && listener != null )
				{
					if( c >= 0 && c < mOrderedLetters.size() )
					{
						listener.onTouchingLetterChanged( c );
						choose = c;
						invalidate();
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if( oldChoose != c && listener != null )
				{
					if( c >= 0 && c < mOrderedLetters.size() )
					{
						listener.onTouchingLetterChanged( c );
						choose = c;
						invalidate();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				choose = -1;
				invalidate();
				break;
		}
		return true;
	}
	
	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener )
	{
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}
	
	public interface OnTouchingLetterChangedListener
	{
		
		
		public void onTouchingLetterChanged(
				int position );
	}
}
