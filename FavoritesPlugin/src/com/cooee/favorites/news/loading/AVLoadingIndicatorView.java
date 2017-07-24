package com.cooee.favorites.news.loading;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by Jack on 2015/10/15
 *
 .BallPulse,
 .BallGridPulse,
 .BallClipRotate,
 .BallClipRotatePulse,
 .SquareSpin,
 .BallClipRotateMultiple,
 .BallPulseRise,
 .BallRotate,
 .CubeTransition,
 .BallZigZag,
 .BallZigZagDeflect,
 .BallTrianglePath,
 .BallScale,
 .LineScale,
 .LineScaleParty,
 .BallScaleMultiple,
 .BallPulseSync,
 .BallBeat,
 .LineScalePulseOut,
 .LineScalePulseOutRapid,
 .BallScaleRipple,
 .BallScaleRippleMultiple,
 .BallSpinFadeLoader,
 .LineSpinFadeLoader,
 .TriangleSkewSpin,
 .Pacman,
 .BallGridBeat,
 .SemiCircleSpin
 *
 */
public class AVLoadingIndicatorView extends View
{
	
	//indicators
	//Sizes (with defaults in DP)
	public static final int DEFAULT_SIZE = 45;
	//attrs
	Paint mPaint;
	BaseIndicatorController mIndicatorController;
	private boolean mHasAnimation;
	
	public AVLoadingIndicatorView(
			Context context )
	{
		super( context );
		init( null , 0 );
	}
	
	public AVLoadingIndicatorView(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		init( attrs , 0 );
	}
	
	public AVLoadingIndicatorView(
			Context context ,
			AttributeSet attrs ,
			int defStyleAttr )
	{
		super( context , attrs , defStyleAttr );
		init( attrs , defStyleAttr );
	}
	
	private void init(
			AttributeSet attrs ,
			int defStyle )
	{
		mPaint = new Paint();
		mPaint.setStyle( Paint.Style.FILL );
		mPaint.setAntiAlias( true );
		applyIndicator();
	}
	
	private void applyIndicator()
	{
		//		switch( mIndicatorId )
		//		{
		//			case BallPulse:
		//				mIndicatorController = new BallPulseIndicator();
		//				break;
		//			case BallGridPulse:
		//				mIndicatorController = new BallGridPulseIndicator();
		//				break;
		//			case BallClipRotate:
		//				mIndicatorController = new BallClipRotateIndicator();
		//				break;
		//			case BallClipRotatePulse:
		//				mIndicatorController = new BallClipRotatePulseIndicator();
		//				break;
		//			case SquareSpin:
		//				mIndicatorController = new SquareSpinIndicator();
		//				break;
		//			case BallClipRotateMultiple:
		//				mIndicatorController = new BallClipRotateMultipleIndicator();
		//				break;
		//			case BallPulseRise:
		//				mIndicatorController = new BallPulseRiseIndicator();
		//				break;
		//			case BallRotate:
		//				mIndicatorController = new BallRotateIndicator();
		//				break;
		//			case CubeTransition:
		//				mIndicatorController = new CubeTransitionIndicator();
		//				break;
		//			case BallZigZag:
		//				mIndicatorController = new BallZigZagIndicator();
		//				break;
		//			case BallZigZagDeflect:
		//				mIndicatorController = new BallZigZagDeflectIndicator();
		//				break;
		//			case BallTrianglePath:
		//				mIndicatorController = new BallTrianglePathIndicator();
		//				break;
		//			case BallScale:
		//				mIndicatorController = new BallScaleIndicator();
		//				break;
		//			case LineScale:
		//				mIndicatorController = new LineScaleIndicator();
		//				break;
		//			case LineScaleParty:
		//				mIndicatorController = new LineScalePartyIndicator();
		//				break;
		//			case BallScaleMultiple:
		//				mIndicatorController = new BallScaleMultipleIndicator();
		//				break;
		//			case BallPulseSync:
		mIndicatorController = new BallPulseSyncIndicator( getContext() );
		//				break;
		//			case BallBeat:
		//				mIndicatorController = new BallBeatIndicator();
		//				break;
		//			case LineScalePulseOut:
		//				mIndicatorController = new LineScalePulseOutIndicator();
		//				break;
		//			case LineScalePulseOutRapid:
		//				mIndicatorController = new LineScalePulseOutRapidIndicator();
		//				break;
		//			case BallScaleRipple:
		//				mIndicatorController = new BallScaleRippleIndicator();
		//				break;
		//			case BallScaleRippleMultiple:
		//				mIndicatorController = new BallScaleRippleMultipleIndicator();
		//				break;
		//			case BallSpinFadeLoader:
		//				mIndicatorController = new BallSpinFadeLoaderIndicator();
		//				break;
		//			case LineSpinFadeLoader:
		//				mIndicatorController = new LineSpinFadeLoaderIndicator();
		//				break;
		//			case TriangleSkewSpin:
		//				mIndicatorController = new TriangleSkewSpinIndicator();
		//				break;
		//			case Pacman:
		//				mIndicatorController = new PacmanIndicator();
		//				break;
		//			case BallGridBeat:
		//				mIndicatorController = new BallGridBeatIndicator();
		//				break;
		//			case SemiCircleSpin:
		//				mIndicatorController = new SemiCircleSpinIndicator();
		//				break;
		//		}
		mIndicatorController.setTarget( this );
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		int width = measureDimension( dp2px( DEFAULT_SIZE ) , widthMeasureSpec );
		int height = measureDimension( dp2px( DEFAULT_SIZE ) , heightMeasureSpec );
		setMeasuredDimension( width , height );
	}
	
	private int measureDimension(
			int defaultSize ,
			int measureSpec )
	{
		int result = defaultSize;
		int specMode = MeasureSpec.getMode( measureSpec );
		int specSize = MeasureSpec.getSize( measureSpec );
		if( specMode == MeasureSpec.EXACTLY )
		{
			result = specSize;
		}
		else if( specMode == MeasureSpec.AT_MOST )
		{
			result = Math.min( defaultSize , specSize );
		}
		else
		{
			result = defaultSize;
		}
		return result;
	}
	
	@Override
	protected void onDraw(
			Canvas canvas )
	{
		super.onDraw( canvas );
		drawIndicator( canvas );
	}
	
	@Override
	protected void onLayout(
			boolean changed ,
			int left ,
			int top ,
			int right ,
			int bottom )
	{
		super.onLayout( changed , left , top , right , bottom );
		if( !mHasAnimation )
		{
			mHasAnimation = true;
			applyAnimation();
		}
	}
	
	@Override
	public void setVisibility(
			int v )
	{
		if( getVisibility() != v )
		{
			super.setVisibility( v );
			if( v == GONE || v == INVISIBLE )
			{
				mIndicatorController.setAnimationStatus( BaseIndicatorController.AnimStatus.END );
			}
			else
			{
				mIndicatorController.setAnimationStatus( BaseIndicatorController.AnimStatus.START );
			}
		}
	}
	
	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		if( mHasAnimation )
		{
			mIndicatorController.setAnimationStatus( BaseIndicatorController.AnimStatus.START );
		}
	}
	
	@Override
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		mIndicatorController.setAnimationStatus( BaseIndicatorController.AnimStatus.CANCEL );
	}
	
	void drawIndicator(
			Canvas canvas )
	{
		mIndicatorController.draw( canvas , mPaint );
	}
	
	void applyAnimation()
	{
		mIndicatorController.initAnimation();
	}
	
	private int dp2px(
			int dpValue )
	{
		return (int)getContext().getResources().getDisplayMetrics().density * dpValue;
	}
}
