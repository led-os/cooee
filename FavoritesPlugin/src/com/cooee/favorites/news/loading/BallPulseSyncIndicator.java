package com.cooee.favorites.news.loading;


import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.cooee.favorites.R;


/**
 * Created by Jack on 2015/10/19.
 */
public class BallPulseSyncIndicator extends BaseIndicatorController
{
	
	private final int PointCount = 5;
	float[] translateYFloats = new float[PointCount];
	private final int DELAY = 90;
	private float mCircleSpacing = 10;
	private float mCircleRadius = 10;
	private float mCircleJumpHeight = 20;
	private final int[] circles = new int[PointCount];
	
	public BallPulseSyncIndicator(
			Context context )
	{
		mCircleSpacing = context.getResources().getDimension( R.dimen.news_loading_circle_spacing );
		mCircleRadius = context.getResources().getDimension( R.dimen.news_loading_circle_radius );
		mCircleJumpHeight = context.getResources().getDimension( R.dimen.news_loading_circle_jump_height );
		circles[0] = context.getResources().getColor( R.color.loading_circle_1 );
		circles[1] = context.getResources().getColor( R.color.loading_circle_2 );
		circles[2] = context.getResources().getColor( R.color.loading_circle_3 );
		circles[3] = context.getResources().getColor( R.color.loading_circle_4 );
		circles[4] = context.getResources().getColor( R.color.loading_circle_5 );
	}
	
	@Override
	public void draw(
			Canvas canvas ,
			Paint paint )
	{
		float x = getWidth() / 2 - ( mCircleRadius * 2 + mCircleSpacing ) * 2;
		for( int i = 0 ; i < PointCount ; i++ )
		{
			if( translateYFloats[i] > 0 )
			{
				canvas.save();
				float translateX = x + ( mCircleRadius * 2 ) * i + mCircleSpacing * i;
				canvas.translate( translateX , translateYFloats[i] );
				paint.setColor( circles[i] );
				canvas.drawCircle( 0 , 0 , mCircleRadius , paint );
				canvas.restore();
			}
		}
	}
	
	@Override
	public List<Animator> createAnimation()
	{
		List<Animator> animators = new ArrayList<Animator>();
		for( int i = 0 ; i < PointCount ; i++ )
		{
			final int index = i;
			ValueAnimator scaleAnim = ValueAnimator.ofFloat( getHeight() / 2 , ( getHeight() / 2 - mCircleJumpHeight ) > 0 ? ( getHeight() / 2 - mCircleJumpHeight ) : 0 , getHeight() / 2 );
			scaleAnim.setDuration( 1000 );
			scaleAnim.setRepeatCount( -1 );
			scaleAnim.setStartDelay( DELAY * i );
			scaleAnim.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(
						ValueAnimator animation )
				{
					translateYFloats[index] = (Float)animation.getAnimatedValue();
					postInvalidate();
				}
			} );
			scaleAnim.start();
			animators.add( scaleAnim );
		}
		return animators;
	}
}
