package com.cooee.phenix.musicpage;


// MusicPage
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.phenix.musicpage.entity.LyricDrawLine;
import com.cooee.phenix.musicpage.entity.LyricSentence;


public class CooeeLyricsTextView extends View
{
	
	private static final String TAG = "CooeeLyricsTextView";
	private float lineSpacing = 15F;
	private float curFontHeight = 16F;
	private float otherFontHeight = 10F;
	private float curFontSize = 16F;
	private float otherFontSize = 10F;
	private int preFontColor = 0x888888;
	private int curFontColor = 0x00ff00;
	private int nextFontColor = 0xffffff;
	private float moveSet = 1F;
	private int scrollWaitTime = 300;
	private Paint textPaint = null;
	private String message = null;
	private LyricSentence entityLyricSentence = new LyricSentence( "" );
	private List<LyricSentence> list = null;
	private List<LyricDrawLine> lyricDrawLines = null;
	private int curIndex = 0;
	private long position = 0L;
	private long duration = 0L;
	//	private boolean newLineBeginning = false;
	private float newLineMoveSet = 0F;
	private float newLineMaxMoveSet = 0f;
	//
	private boolean isFastLocate = false;
	private boolean startResetLyrics = false;
	
	public CooeeLyricsTextView(
			Context context )
	{
		this( context , null );
	}
	
	public CooeeLyricsTextView(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public CooeeLyricsTextView(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		//
		list = new ArrayList<LyricSentence>( 0 );
		lyricDrawLines = new ArrayList<LyricDrawLine>( 0 );
		textPaint = new Paint();
		textPaint.setAntiAlias( true ); //消除锯齿  
		textPaint.setStyle( Paint.Style.FILL ); //设置空心  
		textPaint.setTypeface( Typeface.DEFAULT ); //设置字体  
		//
		//textPaint.setShadowLayer( 1 , 2 , 2 , ColorUtils.getLyricsColor( "0x696969" , color.black ) );
	}
	
	public void setCurFontSize(
			float fontSize )
	{
		if( fontSize > 0 )
		{
			this.curFontSize = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_SP , fontSize , this.getContext().getResources().getDisplayMetrics() );
			//
			textPaint.setTextSize( curFontSize );
			FontMetrics fm = textPaint.getFontMetrics();
			curFontHeight = ( Math.abs( fm.bottom ) + Math.abs( fm.top ) );
			fm = null;
		}
	}
	
	public void setOtherFontSize(
			float fontSize )
	{
		if( fontSize > 0 )
		{
			this.otherFontSize = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_SP , fontSize , this.getContext().getResources().getDisplayMetrics() );
			//
			textPaint.setTextSize( otherFontSize );
			FontMetrics fm = textPaint.getFontMetrics();
			otherFontHeight = ( Math.abs( fm.bottom ) + Math.abs( fm.top ) );
			fm = null;
		}
	}
	
	public void initViewHeight()
	{
		LayoutParams params = this.getLayoutParams();
		int height = (int)( this.lineSpacing * 5 + curFontHeight + otherFontHeight * 4 );
		if( params instanceof RelativeLayout.LayoutParams )
		{
			( (RelativeLayout.LayoutParams)params ).height = height;
		}
		else if( params instanceof LinearLayout.LayoutParams )
		{
			( (LinearLayout.LayoutParams)params ).height = height;
		}
		else if( params instanceof FrameLayout.LayoutParams )
		{
			( (FrameLayout.LayoutParams)params ).height = height;
		}
		this.setLayoutParams( params );
	}
	
	public void setScrollWaitTime(
			int scrollWaitTime )
	{
		this.scrollWaitTime = scrollWaitTime;
	}
	
	public void setLineSpacing(
			float lineSpacing )
	{
		this.lineSpacing = lineSpacing;
	}
	
	public void setFontColor(
			int preFontColor ,
			int curFontColor ,
			int nextFontColor )
	{
		this.preFontColor = preFontColor;
		this.curFontColor = curFontColor;
		this.nextFontColor = nextFontColor;
	}
	
	public void setMoveSet(
			float moveSet )
	{
		if( moveSet > 1 )
			this.moveSet = moveSet;
	}
	
	public List<LyricSentence> getLyricList()
	{
		return this.list;
	}
	
	public void setLyricSentence(
			List<LyricSentence> list )
	{
		//MusicView.logI( "shlt , CooeeLyricsTextView , setLyricSentence  : " + ( list == null || list.size() == 0 ? 0 : list.get( 0 ).getContent() ) );
		//
		this.curIndex = 0;
		this.position = 0;
		this.list.clear();
		this.lyricDrawLines.clear();
		//Log.i( "MusicView" , "setLyricSentence list = " + list );
		if( list != null && list.size() > 0 )
		{
			this.list.addAll( list );
			long time = 0;
			int index = 0;
			//
			time = this.list.get( index ).getFromTime();
			this.list.add( index , new LyricSentence( "" , 0 , time ) );
			//
			this.list.add( new LyricSentence( "" , 0 , 0 ) );
		}
		else
		{
			/*if( messageId == -1 )
				this.message = null;
			else
				this.message = this.getResources().getString( messageId );*/
			postInvalidate();
		}
	}
	
	public void setFastLocate(
			boolean isFastlocate )
	{
		this.isFastLocate = isFastlocate;
	}
	
	public synchronized void setPosition(
			long position ,
			long duration )
	{
		synchronized( this )
		{
			if( this.curIndex >= this.list.size()//
					|| this.list.get( this.curIndex ) == null//
					|| ( this.list.get( this.curIndex ).isInTime( position ) && this.duration == duration ) //
					|| startResetLyrics )
				return;
			startResetLyrics = true;
			if( list != null )
			{
				LyricSentence sentence = list.get( list.size() - 2 );
				if( sentence != null && sentence.getToTime() != duration )
				{
					sentence.setToTime( duration );
				}
				sentence = list.get( list.size() - 1 );
				if( sentence != null && sentence.getToTime() != duration )
				{
					sentence.setFromTime( duration );
					sentence.setToTime( duration );
				}
			}
			//
			this.lyricDrawLines.clear();
			//
			boolean findLine = false;
			if( position < this.position )
			{
				for( int i = this.curIndex ; i > -1 ; i-- )
					if( resetCurLyrics( position , i ) )
					{
						findLine = true;
						break;
					}
			}
			else
			{
				for( int i = this.curIndex ; i < this.list.size() ; i++ )
					if( resetCurLyrics( position , i ) )
					{
						findLine = true;
						break;
					}
			}
			if( !findLine )
			{
				//MusicView.logI( "shlt , CooeeLyricsTextView , resetCurLyrics , chongtouzhaoqi" );
				for( int i = 0 ; i < this.list.size() ; i++ )
					if( resetCurLyrics( position , i ) )
						break;
			}
			//
			this.position = position;
			this.duration = duration;
			this.newLineMoveSet = 0F;
			//			this.newLineBeginning = true;
			//
			if( this.lyricDrawLines.size() != 0 )
			{
				postInvalidate();
			}
			startResetLyrics = false;
			//MusicView.logI( "shlt , CooeeLyricsTextView , resetCurLyrics , end" );
		}
	}
	
	private boolean resetCurLyrics(
			long position ,
			int i )
	{
		LyricSentence sentence = list.get( i );
		if( sentence.isInTime( position ) )
		{
			float y = 0;
			int textColor = nextFontColor;
			float fontSize = otherFontSize;
			float fontHeight = otherFontHeight;
			int alpha = 255;
			for( int j = i - 2 ; j <= i + 2 ; j++ )
			{
				if( 0 <= j && j < list.size() )
					sentence = list.get( j );
				else
					sentence = entityLyricSentence;
				if( j == i )
				{
					textColor = curFontColor;
					fontHeight = curFontHeight;
					fontSize = curFontSize;
					alpha = 255;
				}
				else if( j < i )
				{
					textColor = preFontColor;
					fontHeight = otherFontHeight;
					fontSize = otherFontSize;
					alpha = 127;
				}
				else
				{
					textColor = nextFontColor;
					fontHeight = otherFontHeight;
					fontSize = otherFontSize;
					alpha = 127;
				}
				y = drawLyricSentence( sentence , y , fontHeight , fontSize , alpha , textColor , ( j == i - 2 ) );
			}
			this.curIndex = i;
			return true;
		}
		return false;
	}
	
	private float drawLyricSentence(
			LyricSentence lyricSentence ,
			float y ,
			float fontHeight ,
			float fontSize ,
			int alpha ,
			int textColor ,
			boolean computeNewLineMaxMoveSet )
	{
		float preF = y;
		y += fontHeight;
		if( lyricSentence != null )
		{
			String text = lyricSentence.getContent();
			text = ( text == null ? "" : text );
			float[] widths = new float[text.length()];
			textPaint.setTextSize( fontSize );
			textPaint.getTextWidths( text , widths );
			float width = 0;
			int textStartIndex = 0;
			int textDndIndex = 0;
			for( int i = 0 ; i < widths.length ; i++ )
			{
				float curCharWidth = widths[i];
				width += curCharWidth;
				if( width >= this.getWidth() )
				{
					textDndIndex = i;
					LyricDrawLine line = new LyricDrawLine(
							text ,
							textStartIndex ,
							textDndIndex ,
							( this.getWidth() - ( width - curCharWidth ) ) / 2 + this.getPaddingLeft() ,
							y ,
							textColor ,
							fontHeight ,
							fontSize ,
							alpha );
					this.lyricDrawLines.add( line );
					width = curCharWidth;
					y += fontHeight;
					textStartIndex = textDndIndex;
				}
			}
			if( width != 0 )
			{
				textDndIndex = text.length();
				LyricDrawLine line = new LyricDrawLine( text ,//
						textStartIndex ,
						textDndIndex ,
						( this.getWidth() - width ) / 2 + this.getPaddingLeft() ,
						y ,
						textColor ,
						fontHeight ,
						fontSize ,
						alpha );
				this.lyricDrawLines.add( line );
			}
		}
		y += lineSpacing;
		if( computeNewLineMaxMoveSet )
		{
			newLineMaxMoveSet = y - preF;
			if( newLineMaxMoveSet > ( otherFontHeight + lineSpacing ) )
				newLineMaxMoveSet += 2;
		}
		return y;
	}
	
	@Override
	protected void onDraw(
			Canvas canvas )
	{
		if( this.lyricDrawLines.size() == 0 /*&& message == null*/)
			return;
		/*if( message != null )
			drawMessage( canvas );*/
		drawLyrics( canvas );
	}
	
	private void drawLyrics(
			Canvas canvas )
	{
		if( !isFastLocate )
		{
			{
				if( this.newLineMoveSet == 0 )
				{
					this.postDelayed( new Runnable() {
						
						@Override
						public void run()
						{
							newLineMoveSet = 0.0000000001F;
							postInvalidate();
						}
					} , scrollWaitTime );
				}
				else if( this.newLineMoveSet <= newLineMaxMoveSet )
				{
					this.newLineMoveSet += moveSet;
					this.postInvalidate();
				}
				else
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.i( "MusicView" , "CooeeLyricsTextView , newLineMoveSet , finish" );
				}
			}
		}
		for( int i = 0 ; i < lyricDrawLines.size() ; i++ )
		{
			LyricDrawLine line = lyricDrawLines.get( i );
			textPaint.setColor( line.getTextColor() );
			textPaint.setTextSize( line.getFontSize() );
			//textPaint.setAlpha( line.getAlpha() );
			canvas.drawText( line.getText() , line.getStart() , line.getEnd() , line.getX() , ( line.getY() - this.newLineMoveSet ) , textPaint );
		}
	}
}
