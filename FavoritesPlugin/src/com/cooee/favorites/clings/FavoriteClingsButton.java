package com.cooee.favorites.clings;


import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


/**
 * cheyingkun add whole file	//酷生活引导页动画。
 * @author cheyingkun
 */
public class FavoriteClingsButton extends Button
{
	
	private Paint mRipplePaint = null;
	//圆环2
	private float mRippleRadius2 = 0;//当前半径
	private ValueAnimator mStartAnimator2 = null;//动画
	float scaleValue2;//动画进度
	//圆环1
	private float mRippleRadius1 = 0;//当前半径
	private ValueAnimator mStartAnimator1 = null;//动画
	float scaleValue1;//动画进度
	//圆环0
	private float mRippleRadius0 = 0;//当前半径
	private ValueAnimator mStartAnimator0 = null;//动画
	float scaleValue0;//动画进度
	//
	int baseColor;//内圆颜色
	boolean showRings = true;
	//配置内容start
	float defaultRadius = 0;//内圆半径
	int defaultRadiusAlpha = 0;//内圆透明度
	int defaultDuration = 0;//引导页按钮水波纹效果的动画时间
	int ring1StartDelay = 0;//圆环1动画延迟
	int ring2StartDelay = 0;//圆环2动画延迟
	int ring3StartDelay = 0;//圆环3动画延迟
	float radius1Zoom = 0;//圆环1缩放倍数
	float radius2Zoom = 0;//圆环2缩放倍数
	float radius3Zoom = 0;//圆环3缩放倍数
	float defaultRadiusRing = -1;
	
	//配置内容end
	public FavoriteClingsButton(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		initRipple();
	}
	
	public FavoriteClingsButton(
			Context context )
	{
		super( context );
		initRipple();
	}
	
	private void initRipple()
	{
		intDefaultConfig();//默认配置初始化
		mRipplePaint = new Paint();
		mRipplePaint.setAntiAlias( true );
		mRipplePaint.setStyle( Paint.Style.FILL_AND_STROKE );
		mRipplePaint.setColor( Color.argb( defaultRadiusAlpha , 255 , 255 , 255 ) );
		baseColor = mRipplePaint.getColor();
		setBackgroundColor( Color.argb( 0 , 0 , 0 , 0 ) );
		//开始动画
		showRings = true;
		startRippleEffect0();
		startRippleEffect1();
		startRippleEffect2();
		setClickable( true );
		setFocusable( true );
		//触摸监听
		setOnTouchListener( new OnTouchListener() {
			
			@Override
			public boolean onTouch(
					View v ,
					MotionEvent event )
			{
				switch( event.getAction() )
				{
					case MotionEvent.ACTION_DOWN:
						if( isOnTounchDefaultRadius( event ) )
						{
							stopRippleEffect();
							showRings = false;
							FavoriteClingsButton.this.invalidate();
						}
						break;
					case MotionEvent.ACTION_MOVE:
						if( !showRings && !isOnTounchDefaultRadius( event ) )
						{
							showRings = true;
							startRippleEffect0();
							startRippleEffect1();
							startRippleEffect2();
						}
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						if( !showRings )
						{
							showRings = true;
							startRippleEffect0();
							startRippleEffect1();
							startRippleEffect2();
						}
						break;
					default:
						break;
				}
				return false;
			}
		} );
	}
	
	private void intDefaultConfig()
	{
		defaultRadius = getResources().getDimension( com.cooee.favorites.R.dimen.favorite_clings_button_default_radius );
		defaultRadiusAlpha = (int)( getResources().getInteger( com.cooee.favorites.R.integer.favorite_clings_button_default_alpha ) * 1.0f / 100 * 255 );
		defaultDuration = getResources().getInteger( com.cooee.favorites.R.integer.favorite_clings_button_anim_duration );
		ring1StartDelay = getResources().getInteger( com.cooee.favorites.R.integer.favorite_clings_ring1_delay );
		ring2StartDelay = getResources().getInteger( com.cooee.favorites.R.integer.favorite_clings_ring2_delay );
		ring3StartDelay = getResources().getInteger( com.cooee.favorites.R.integer.favorite_clings_ring3_delay );
		radius1Zoom = getResources().getInteger( com.cooee.favorites.R.integer.favorite_clings_ring1_zoom ) * 1.0f / 100;
		radius2Zoom = getResources().getInteger( com.cooee.favorites.R.integer.favorite_clings_ring2_zoom ) * 1.0f / 100;
		radius3Zoom = getResources().getInteger( com.cooee.favorites.R.integer.favorite_clings_ring3_zoom ) * 1.0f / 100;
	}
	
	private boolean isOnTounchDefaultRadius(
			MotionEvent event )
	{
		int width = FavoriteClingsButton.this.getMeasuredWidth();
		int height = FavoriteClingsButton.this.getMeasuredHeight();
		float x = event.getX();//0-w
		float y = event.getY();//0-h
		if( x < ( width - defaultRadius * 2 ) / 2 || x > ( width + defaultRadius * 2 ) / 2 //
				|| y < ( height - defaultRadius * 2 ) / 2 || y > ( height + defaultRadius * 2 ) / 2 )
		{
			return false;
		}
		return true;
	}
	
	@Override
	public void draw(
			Canvas canvas )
	{
		super.draw( canvas );
		defaultRadiusRing = FavoriteClingsButton.this.getWidth() / 4;
		if( this.getWidth() > 0 && this.getHeight() > 0 )
		{
			drawRippleDrawable( canvas );
		}
	}
	
	Point mRipplePoint = null;
	
	/**
	 * 引导页水波纹效果
	 * @param canvas
	 */
	private void drawRippleDrawable(
			Canvas canvas )
	{
		if( mRipplePoint == null )
		{
			mRipplePoint = new Point( this.getWidth() / 2 , this.getHeight() / 2 );
		}
		else
		{
			mRipplePoint.set( this.getWidth() / 2 , this.getHeight() / 2 );
		}
		mRipplePaint.setColor( baseColor );
		if( showRings )
		{
			int baseAlpha = Color.alpha( baseColor );
			mRipplePaint.setAntiAlias( true ); //消除锯齿
			mRipplePaint.setStyle( Paint.Style.STROKE ); //绘制空心圆 
			//圆环0
			if( mRippleRadius0 > 0 )
			{
				int newAlpha = (int)( baseAlpha - baseAlpha * scaleValue0 );
				//大圆-渐隐
				float ringWidth0 = mRippleRadius0 - defaultRadius; //设置圆环宽度
				//绘制圆环
				mRipplePaint.setColor( baseColor );
				mRipplePaint.setAlpha( newAlpha );
				mRipplePaint.setStrokeWidth( ringWidth0 );
				canvas.drawCircle( mRipplePoint.x , mRipplePoint.y , defaultRadius + ringWidth0 / 2 , mRipplePaint );
			}
			//圆环1
			if( mRippleRadius1 > 0 )
			{
				int newAlpha = (int)( baseAlpha - baseAlpha * scaleValue1 );
				//大圆-渐隐
				float ringWidth1 = mRippleRadius1 - defaultRadius; //设置圆环宽度
				//绘制圆环
				mRipplePaint.setColor( baseColor );
				mRipplePaint.setAlpha( newAlpha );
				mRipplePaint.setStrokeWidth( ringWidth1 );
				canvas.drawCircle( mRipplePoint.x , mRipplePoint.y , defaultRadius + ringWidth1 / 2 , mRipplePaint );
			}
			//圆环2
			if( mRippleRadius2 > 0 )
			{
				int newAlpha = (int)( baseAlpha - baseAlpha * scaleValue2 );
				//大圆-渐隐
				float ringWidth2 = mRippleRadius2 - defaultRadius; //设置圆环宽度
				//绘制圆环
				mRipplePaint.setColor( baseColor );
				mRipplePaint.setAlpha( newAlpha );
				mRipplePaint.setStrokeWidth( ringWidth2 );
				canvas.drawCircle( mRipplePoint.x , mRipplePoint.y , defaultRadius + ringWidth2 / 2 , mRipplePaint );
			}
		}
		//小圆
		mRipplePaint.setStyle( Paint.Style.FILL_AND_STROKE );
		mRipplePaint.setStrokeWidth( 0 );
		mRipplePaint.setColor( baseColor );
		mRipplePaint.setAlpha( defaultRadiusAlpha * 3 );
		canvas.drawCircle( mRipplePoint.x , mRipplePoint.y , defaultRadius , mRipplePaint );
	}
	
	/**
	 * 执行半径变大的动画
	 */
	private void startRippleEffect2()
	{
		if( mStartAnimator2 != null && mStartAnimator2.isRunning() )
		{
			mStartAnimator2.cancel();
		}
		mStartAnimator2 = ValueAnimator.ofFloat( 0f , 1f );
		mStartAnimator2.setRepeatCount( ValueAnimator.INFINITE );
		mStartAnimator2.setDuration( defaultDuration );
		mStartAnimator2.setStartDelay( ring3StartDelay );
		mStartAnimator2.start();
		mStartAnimator2.addUpdateListener( new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(
					ValueAnimator animation )
			{
				scaleValue2 = (Float)animation.getAnimatedValue();
				mRippleRadius2 = scaleValue2 * defaultRadiusRing * ( radius3Zoom - 1 ) + defaultRadius;
				FavoriteClingsButton.this.invalidate();
			}
		} );
	}
	
	private void startRippleEffect1()
	{
		if( mStartAnimator1 != null && mStartAnimator1.isRunning() )
		{
			mStartAnimator1.cancel();
		}
		mStartAnimator1 = ValueAnimator.ofFloat( 0f , 1f );
		mStartAnimator1.setRepeatCount( ValueAnimator.INFINITE );
		mStartAnimator1.setDuration( defaultDuration );
		mStartAnimator1.setStartDelay( ring2StartDelay );
		mStartAnimator1.start();
		mStartAnimator1.addUpdateListener( new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(
					ValueAnimator animation )
			{
				scaleValue1 = (Float)animation.getAnimatedValue();
				mRippleRadius1 = scaleValue1 * defaultRadiusRing * ( radius2Zoom - 1 ) + defaultRadius;
				FavoriteClingsButton.this.invalidate();
			}
		} );
	}
	
	private void startRippleEffect0()
	{
		if( mStartAnimator0 != null && mStartAnimator0.isRunning() )
		{
			mStartAnimator0.cancel();
		}
		mStartAnimator0 = ValueAnimator.ofFloat( 0f , 1f );
		mStartAnimator0.setRepeatCount( ValueAnimator.INFINITE );
		mStartAnimator0.setDuration( defaultDuration );
		mStartAnimator0.setStartDelay( ring1StartDelay );
		mStartAnimator0.start();
		mStartAnimator0.addUpdateListener( new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(
					ValueAnimator animation )
			{
				scaleValue0 = (Float)animation.getAnimatedValue();
				mRippleRadius0 = scaleValue0 * defaultRadiusRing * ( radius1Zoom - 1 ) + defaultRadius;
				FavoriteClingsButton.this.invalidate();
			}
		} );
	}
	
	private void stopRippleEffect()
	{
		if( mStartAnimator0 != null && mStartAnimator0.isRunning() )
		{
			mStartAnimator0.cancel();
			mRippleRadius0 = 0;
		}
		if( mStartAnimator1 != null && mStartAnimator1.isRunning() )
		{
			mStartAnimator1.cancel();
			mRippleRadius1 = 0;
		}
		if( mStartAnimator2 != null && mStartAnimator2.isRunning() )
		{
			mStartAnimator2.cancel();
			mRippleRadius2 = 0;
		}
	}
}
