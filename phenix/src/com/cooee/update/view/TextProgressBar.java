package com.cooee.update.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class TextProgressBar extends ProgressBar
{
	
	protected String mText = "";
	private TextPaint m_textPaint;
	private boolean m_showText = true;
	private int m_textoffsetX = 5;
	private int m_textoffsetY = 5;
	
	public TextProgressBar(
			Context context )
	{
		this( context , null );
	}
	
	public TextProgressBar(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public TextProgressBar(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		// create a default progress bar indicator text paint used for drawing
		// the
		// text on to the canvas
		m_textPaint = new TextPaint( Paint.ANTI_ALIAS_FLAG );
		m_textPaint.density = getResources().getDisplayMetrics().density;
		m_textPaint.setColor( Color.WHITE );
		m_textPaint.setTextAlign( Align.CENTER );
		m_textPaint.setTextSize( 10 );
		// get the styleable attributes as defined in the xml
		TypedArray a = context.obtainStyledAttributes( attrs , R.styleable.TextProgressBar , defStyle , 0 );
		if( a != null )
		{
			m_textPaint.setTextSize( a.getDimension( R.styleable.TextProgressBar_textSize , 10 ) );
			m_textPaint.setColor( a.getColor( R.styleable.TextProgressBar_textColor , Color.BLACK ) );
			int alignIndex = ( a.getInt( R.styleable.TextProgressBar_textAlign , 1 ) );
			if( alignIndex == 0 )
			{
				m_textPaint.setTextAlign( Align.LEFT );
			}
			else if( alignIndex == 1 )
			{
				m_textPaint.setTextAlign( Align.CENTER );
			}
			else if( alignIndex == 2 )
			{
				m_textPaint.setTextAlign( Align.RIGHT );
			}
			int textStyle = ( a.getInt( R.styleable.TextProgressBar_textStyle , 0 ) );
			if( textStyle == 0 )
			{
				m_textPaint.setTextSkewX( 0.0f );
				m_textPaint.setFakeBoldText( false );
			}
			else if( textStyle == 1 )
			{
				m_textPaint.setTextSkewX( 0.0f );
				m_textPaint.setFakeBoldText( true );
			}
			else if( textStyle == 2 )
			{
				m_textPaint.setTextSkewX( -0.25f );
				m_textPaint.setFakeBoldText( false );
			}
			m_textoffsetX = (int)a.getDimension( R.styleable.TextProgressBar_textoffsetX , 0 );
			m_textoffsetY = (int)a.getDimension( R.styleable.TextProgressBar_textoffsetY , 0 );
			a.recycle();
		}
	}
	
	public void setText(
			CharSequence text )
	{
		m_showText = true;
		mText = text.toString();
	}
	
	public final void setText(
			int resid )
	{
		setText( getContext().getResources().getText( resid ) );
	}
	
	/**
	 * Set the text color
	 * 
	 * @param color
	 */
	public void setTextColor(
			int color )
	{
		m_textPaint.setColor( color );
	}
	
	/**
	 * Set the text size.
	 * 
	 * @param size
	 */
	public void setTextSize(
			float size )
	{
		m_textPaint.setTextSize( size );
	}
	
	/**
	 * Set the text bold.
	 * 
	 * @param bold
	 */
	public void setTextBold(
			boolean bold )
	{
		m_textPaint.setFakeBoldText( true );
	}
	
	/**
	 * Set the alignment of the text.
	 * 
	 * @param align
	 */
	public void setTextAlign(
			Align align )
	{
		m_textPaint.setTextAlign( align );
	}
	
	public void hideText()
	{
		m_showText = false;
	}
	
	public void showText()
	{
		m_showText = true;
	}
	
	/**
	 * Set the paint object used to draw the text on to the canvas.
	 * 
	 * @param paint
	 */
	public void setPaint(
			TextPaint paint )
	{
		m_textPaint = paint;
	}
	
	@Override
	protected synchronized void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		// TODO Auto-generated method stub
		super.onMeasure( widthMeasureSpec , heightMeasureSpec );
	}
	
	@Override
	protected void onLayout(
			boolean changed ,
			int left ,
			int top ,
			int right ,
			int bottom )
	{
		// TODO Auto-generated method stub
		super.onLayout( changed , left , top , right , bottom );
	}
	
	@Override
	protected synchronized void onDraw(
			Canvas canvas )
	{
		super.onDraw( canvas );
		if( m_showText )
		{
			canvas.drawText( mText , getWidth() / 2 , getHeight() / 2 + m_textPaint.getTextSize() / 2 + m_textoffsetY , m_textPaint );
		}
	}
	
	@Override
	public synchronized void setProgress(
			int progress )
	{
		super.setProgress( progress );
		// the setProgress super will not change the details of the progress bar
		// anymore so we need to force an update to redraw the progress bar		
		updateProgressBar();
		invalidate();
	}
	
	private float getScale(
			int progress )
	{
		float scale = getMax() > 0 ? (float)progress / (float)getMax() : 0;
		return scale;
	}
	
	/**
	 * Instead of using clipping regions to uncover the progress bar as the
	 * progress increases we increase the drawable regions for the progress bar
	 * and pattern overlay. Doing this gives us greater control and allows us to
	 * show the rounded cap on the progress bar.
	 */
	private void updateProgressBar()
	{
		//step 1 
		Drawable progressDrawable = getProgressDrawable();
		//step 2		
		//*
		if( progressDrawable != null && progressDrawable instanceof LayerDrawable )
		{
			LayerDrawable d = (LayerDrawable)progressDrawable;
			final float scale = getScale( getProgress() );
			// get the progress bar and update it's size
			Drawable progressBar = d.findDrawableByLayerId( R.id.progress );
			Drawable back = d.findDrawableByLayerId( android.R.id.background );
			if( back != null )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "MM" , " back " + back.getBounds() );
			}
			final int width = d.getBounds().right - d.getBounds().left;
			if( progressBar != null )
			{
				Rect progressBarBounds = progressBar.getBounds();
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "MM" , " old " + d.getBounds() );
				progressBarBounds.right = progressBarBounds.left + (int)( width * scale + 0.5f );
				progressBar.setBounds( progressBarBounds );
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( "MM" , " now " + progressBarBounds );
			}
		}
	}
	
	@Override
	protected void onSizeChanged(
			int w ,
			int h ,
			int oldw ,
			int oldh )
	{
		// TODO Auto-generated method stub		
		super.onSizeChanged( w , h , oldw , oldh );
		updateProgressBar();
	}
}
