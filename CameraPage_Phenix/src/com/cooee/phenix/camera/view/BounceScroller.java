package com.cooee.phenix.camera.view;


/**
 * Created by suliyea on 16/4/14.
 */
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.camera.inte.IOverScrollListener;


public class BounceScroller extends RelativeLayout implements IOverScrollListener
{
	
	public static final String TAG = "BounceScroller";
	public static final int DEFAULT_MIN_DURATION = 250;
	private static final int DEFAULT_MAX_DURATION = 500;
	
	public static enum State
	{
		/**通过动画变化到正常显示ListView*/
		STATE_FIT_CONTENT ,
		/**完全显示出上方刷新动画界面之类的情况（然而我们并没有这样的界面,所以其实用不到）*/
		STATE_SHOW ,
		/**滑动到超过ListView的情况,也就是回弹前多拉出来的时候*/
		STATE_OVER ,
		/**通过动画变化到完全显示出上方刷新动画界面之类的情况（然而我们也用不到这个,理由同上）*/
		STATE_FIT_EXTRAS ,
		/**手指抬起后,惯性滑动到超过界限,跟over可能会冲突*/
		STATE_FLING_OVER_SCROLL
	};
	
	protected State mState = State.STATE_FIT_CONTENT;
	private Bouncer mBouncer = new Bouncer();
	private BounceListener mListener;
	private View mContentView;
	private int mLastEventY;
	private int mLastTargetTop;
	private View mHeaderView;
	protected int mHeaderHeight;
	private View mFooterView;
	private int mFooterHeight;
	private boolean overScrolled;
	private boolean pullingHeader;
	private boolean pullingFooter;
	private boolean headerBounce = true;
	private boolean footerBounce = true;
	private boolean isOnthouchDown = false;
	private int remainOffset;
	private View mTargetView;
	private TimeInterpolator mInterpolator;
	private long mTimeBase = 0;
	
	public BounceScroller(
			Context context )
	{
		this( context , null );
	}
	
	public BounceScroller(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		mInterpolator = new DecelerateInterpolator();
		remainOffset = 0;
	}
	
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		if( getChildCount() > 0 )
		{
			mContentView = getChildAt( 0 );
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(
			MotionEvent event )
	{
		if( !isEnabled() )
		{
			return true;
		}
		if( mContentView == null && !eventInView( event , mContentView ) )
		{
			return super.dispatchTouchEvent( event );
		}
		int action = event.getAction();
		if( takeTouchEvent( event ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( System.currentTimeMillis() , " takeTouchEvent " , action ) );
		}
		else
		{
			boolean result = super.dispatchTouchEvent( event );
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( System.currentTimeMillis() , " dispatchTouchEvent " , result ) );
		}
		if( action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP )
		{
			overScrolled = false;
			mTimeBase = 0;
			pullingHeader = false;
			pullingFooter = false;
			mLastEventY = 0;
			mLastTargetTop = 0;
			mTargetView = null;
			isOnthouchDown = false;
			return true;
		}
		else if( action == MotionEvent.ACTION_DOWN )
		{
			// cancel bounce if exists
			mBouncer.cancel();
			mTargetView = getTargetView( mContentView , event );
			mTimeBase = 0;
			isOnthouchDown = true;
		}
		else if( action == MotionEvent.ACTION_MOVE )
		{
			int eventOffset = (int)event.getY() - mLastEventY;
			if( !overScrolled )
			{
				if( mHeaderView != null && mHeaderView.getBottom() > 0 && eventOffset < 0 )
				{
					overScrolled = true;
				}
				else if( mFooterView != null && mFooterView.getTop() < getBottom() && eventOffset > 0 )
				{
					overScrolled = true;
				}
				else
				{
					overScrolled = false;
				}
			}
			if( mTargetView != null && mTargetView.getVisibility() != View.VISIBLE )
			{
				mTargetView = getTargetView( mContentView , event );
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "update mTargetView:" , mTargetView.getId() ) );
				mTimeBase = 0;
				overScrolled = false;
			}
			else
			{
				int targetTop = getViewTop( mTargetView );
				int viewOffset = targetTop - mLastTargetTop;
				if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.d( TAG , StringUtils.concat( "targetTop:" , targetTop , "-viewOffset:" , viewOffset , "-eventOffset:" , eventOffset , "-mTimeBase:" , mTimeBase ) );
				if( eventOffset != 0 && viewOffset == 0 && !overScrolled )
				{
					long currentTime = System.currentTimeMillis();
					remainOffset += eventOffset;
					if( mTimeBase == 0 )
					{
						mTimeBase = currentTime;
					}
					else if( currentTime - mTimeBase > 50 )
					{
						overScrolled = true;
						mTimeBase = 0;
					}
				}
				else if( eventOffset != 0 && viewOffset != 0 )
				{
					mTimeBase = 0;
				}
				if( remainOffset != 0 && overScrolled )
				{
					if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "do remainOffset:" , remainOffset ) );
					onOffset( remainOffset );
					remainOffset = 0;
				}
			}
		}
		mLastTargetTop = getViewTop( mTargetView );
		mLastEventY = (int)event.getY();
		return true;
	}
	
	protected void onDraw(
			Canvas canvas )
	{
		super.onDraw( canvas );
		final boolean debug = false;
		if( debug )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( System.currentTimeMillis() , " onDraw" ) );
		}
	}
	
	private int getViewTop(
			View view )
	{
		if( view == null )
		{
			return 0;
		}
		int[] location = new int[2];
		view.getLocationOnScreen( location );
		return location[1];
	}
	
	private boolean takeTouchEvent(
			MotionEvent event )
	{
		if( !headerBounce && !footerBounce )
		{
			return false;
		}
		int action = event.getAction();
		int contentTop = mContentView.getTop();
		if( action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL )
		{
			if( contentTop > 0 )
			{
				int offset = contentTop;
				if( mHeaderView != null && offset > mHeaderHeight / 2 )
				{
					offset = offset - mHeaderHeight;
					mBouncer.recover( true , offset , State.STATE_FIT_EXTRAS );
				}
				else
				{
					mBouncer.recover( true , offset , State.STATE_FIT_CONTENT );
				}
			}
			else if( contentTop < 0 )
			{
				int offset = mContentView.getBottom() - getBottom();
				if( mFooterView != null && ( offset + mFooterHeight / 2 ) < 0 )
				{
					offset = offset + mFooterHeight;
					mBouncer.recover( false , offset , State.STATE_FIT_EXTRAS );
				}
				else
				{
					// add by suliyea 防止上拉回弹时候不能回复到原位.
					if( offset != contentTop )
					{
						offset = contentTop;
					}
					mBouncer.recover( false , offset , State.STATE_FIT_CONTENT );
				}
			}
		}
		else if( action == MotionEvent.ACTION_MOVE )
		{
			int offset = (int)( event.getY() - mLastEventY );
			return onOffset( offset );
		}
		return false;
	}
	
	private boolean onOffset(
			int offset )
	{
		offset = offset / 2;
		boolean handled = false;
		int contentTop = mContentView.getTop();
		if( headerBounce && !handled && contentTop >= 0 && !pullingFooter )
		{
			handled |= pullHeader( offset );
		}
		if( footerBounce && !handled && contentTop <= 0 && !pullingHeader )
		{
			handled |= pullFooter( offset );
		}
		if( handled && mListener != null )
		{
			mListener.onScrollDelta( offset );
		}
		return handled;
	}
	
	private void setState(
			boolean header ,
			State newState )
	{
		String position = header ? "header" : "footer";
		if( newState == mState )
		{
			return;
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( position , " setState from " , mState , " to " , newState ) );
		mState = newState;
		if( mListener != null )
		{
			mListener.onState( header , newState );
		}
	}
	
	private boolean pullHeader(
			int offset )
	{
		int scrollY = mContentView.getScrollY();
		int curTop = mContentView.getTop();
		// pull header
		if( !overScrolled || scrollY > 0 || ( offset < 0 && scrollY == 0 && curTop <= 0 ) )
		{
			return false;
		}
		pullingHeader = true;
		int nextTop = curTop + offset;
		if( nextTop <= 0 )
		{
			offset = -curTop;
			overScrolled = false;
			mTimeBase = 0;
			nextTop = 0;
			pullingHeader = false;
			if( mState != State.STATE_FIT_CONTENT )
			{
				setState( true , State.STATE_FIT_CONTENT );
			}
		}
		else if( nextTop > 0 && nextTop <= mHeaderHeight )
		{
			if( ( mState != State.STATE_SHOW ) )
			{
				setState( true , State.STATE_SHOW );
			}
		}
		else if( nextTop > mHeaderHeight )
		{
			if( mState != State.STATE_OVER )
			{
				setState( true , State.STATE_OVER );
			}
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "pullHeader:" , offset , "-nextTop:" , nextTop ) );
		offsetContent( offset );
		return true;
	}
	
	private boolean pullFooter(
			int offset )
	{
		int curBottom = mContentView.getBottom();
		int conBottom = this.getBottom();
		// pull footer
		if( !overScrolled || ( offset > 0 && conBottom <= curBottom ) )
		{
			return false;
		}
		pullingFooter = true;
		int nextBottom = curBottom + offset;
		if( nextBottom >= conBottom )
		{
			offset = conBottom - curBottom;
			overScrolled = false;
			mTimeBase = 0;
			nextBottom = conBottom;
			pullingFooter = false;
			if( mState != State.STATE_FIT_CONTENT )
			{
				setState( false , State.STATE_FIT_CONTENT );
			}
		}
		else if( nextBottom < conBottom && nextBottom >= ( conBottom - mFooterHeight ) )
		{
			if( ( mState != State.STATE_SHOW ) )
			{
				setState( false , State.STATE_SHOW );
			}
		}
		else if( nextBottom < ( conBottom - mFooterHeight ) )
		{
			if( mState != State.STATE_OVER )
			{
				setState( false , State.STATE_OVER );
			}
		}
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , StringUtils.concat( "pullFooter:" , offset , "-nextBottom:" , nextBottom ) );
		offsetContent( offset );
		return true;
	}
	
	private class Bouncer implements AnimatorUpdateListener , AnimatorListener
	{
		
		private ValueAnimator mAnimator;
		private int mLastOffset;
		private boolean isHeader;
		private State mTargetState;
		private boolean mCanceled;
		
		public void recover(
				boolean header ,
				int offset ,
				State state )
		{
			int duration = Math.max( (int)( Math.abs( offset * 1.5 ) ) , DEFAULT_MIN_DURATION );
			duration = Math.min( duration , DEFAULT_MAX_DURATION );
			recover( header , offset , state , duration );
		}
		
		public void recover(
				boolean header ,
				int offset ,
				State state ,
				int duration )
		{
			cancel();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "recover offset:" , offset , "-state:" , state ) );
			mCanceled = false;
			isHeader = header;
			mTargetState = state;
			mAnimator = new ValueAnimator();
			mAnimator.setIntValues( 0 , offset );
			mLastOffset = 0;
			mAnimator.setDuration( duration );
			mAnimator.setRepeatCount( 0 );
			if( mInterpolator == null )
			{
				mInterpolator = new DecelerateInterpolator();
			}
			mAnimator.setInterpolator( mInterpolator );
			mAnimator.addListener( this );
			mAnimator.addUpdateListener( this );
			mAnimator.start();
		}
		
		public void cancel()
		{
			if( mAnimator != null && mAnimator.isRunning() )
			{
				mAnimator.cancel();
			}
			mAnimator = null;
		}
		
		@Override
		public void onAnimationUpdate(
				ValueAnimator va )
		{
			int currentOffset = (Integer)va.getAnimatedValue();
			int delta = mLastOffset - currentOffset;
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "recover delta:" , delta , "-currentOffset:" , currentOffset ) );
			offsetContent( delta );
			mLastOffset = currentOffset;
			if( mListener != null )
			{
				int contentOffset = mContentView.getTop();
				mListener.onOffset( isHeader , contentOffset );
				mListener.onScrollDelta( delta );
			}
		}
		
		@Override
		public void onAnimationStart(
				Animator animation )
		{
		}
		
		@Override
		public void onAnimationEnd(
				Animator animation )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "onAnimationEnd" );
			mAnimator = null;
			if( !mCanceled )
			{
				setState( isHeader , mTargetState );
				if( mTargetState == State.STATE_FLING_OVER_SCROLL )
				{
					resetState();
				}
			}
		}
		
		@Override
		public void onAnimationCancel(
				Animator animation )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "onAnimationCancel" );
			mCanceled = true;
		}
		
		@Override
		public void onAnimationRepeat(
				Animator animation )
		{
		}
	}
	
	@Override
	protected void onLayout(
			boolean changed ,
			int left ,
			int top ,
			int right ,
			int bottom )
	{
		int contentTop = 0;
		int contentBottom = 0;
		if( mContentView != null )
		{
			contentTop = mContentView.getTop();
			contentBottom = contentTop + this.getMeasuredHeight();
			mContentView.layout( 0 , contentTop , right , contentBottom );
		}
		if( mHeaderView != null )
		{
			int headerTop = contentTop - mHeaderHeight;
			mHeaderView.layout( 0 , headerTop , right , headerTop + mHeaderHeight );
		}
		if( mFooterView != null )
		{
			int footerTop = contentBottom;
			mFooterView.layout( 0 , footerTop , right , footerTop + mFooterHeight );
		}
	}
	
	private boolean offsetContent(
			int offset )
	{
		if( mContentView != null )
		{
			mContentView.offsetTopAndBottom( offset );
		}
		if( mHeaderView != null )
		{
			mHeaderView.offsetTopAndBottom( offset );
		}
		if( mFooterView != null )
		{
			mFooterView.offsetTopAndBottom( offset );
		}
		invalidate();
		return true;
	}
	
	public boolean attach(
			View view )
	{
		if( view == null )
		{
			return false;
		}
		ViewGroup parent = (ViewGroup)view.getParent();
		ViewGroup.LayoutParams params = parent.getLayoutParams();
		int index = parent.indexOfChild( view );
		parent.removeView( view );
		parent.addView( this , index , params );
		index = 0;
		if( mHeaderView != null )
		{
			index = 1;
		}
		params = new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT );
		addView( view , index , params );
		this.mContentView = view;
		return true;
	}
	
	public void resetState()
	{
		if( mState != State.STATE_FIT_EXTRAS && mState != State.STATE_FLING_OVER_SCROLL )
		{
			return;
		}
		if( mContentView == null )
		{
			return;
		}
		int offset = mContentView.getTop();
		if( offset != 0 )
		{
			boolean header = true;
			if( offset < 0 )
			{
				header = false;
			}
			mBouncer.recover( header , offset , State.STATE_FIT_CONTENT );
		}
	}
	
	public BounceScroller enableHeader(
			boolean bounce )
	{
		this.headerBounce = bounce;
		return this;
	}
	
	public BounceScroller enableFooter(
			boolean bounce )
	{
		this.footerBounce = bounce;
		return this;
	}
	
	public BounceScroller setHeaderView(
			View view )
	{
		if( mHeaderView != null )
		{
			removeView( mHeaderView );
			mHeaderView = null;
		}
		mHeaderView = view;
		if( mHeaderView != null )
		{
			mHeaderView.measure( MeasureSpec.UNSPECIFIED , MeasureSpec.UNSPECIFIED );
			mHeaderHeight = mHeaderView.getMeasuredHeight();
			LayoutParams params = new LayoutParams( LayoutParams.MATCH_PARENT , mHeaderHeight );
			addView( mHeaderView , 0 , params );
		}
		return this;
	}
	
	public View getHeaderView()
	{
		return mHeaderView;
	}
	
	public BounceScroller setFooterView(
			View view )
	{
		if( mFooterView != null )
		{
			removeView( mFooterView );
			mFooterView = null;
		}
		mFooterView = view;
		if( mFooterView != null )
		{
			mFooterView.measure( MeasureSpec.UNSPECIFIED , MeasureSpec.UNSPECIFIED );
			mFooterHeight = mFooterView.getMeasuredHeight();
			LayoutParams params = new LayoutParams( LayoutParams.MATCH_PARENT , mHeaderHeight );
			addView( mFooterView , 0 , params );
		}
		return this;
	}
	
	public View getFooterView()
	{
		return mFooterView;
	}
	
	public BounceScroller setListener(
			BounceListener listener )
	{
		this.mListener = listener;
		return this;
	}
	
	public BounceScroller setInterpolator(
			TimeInterpolator interpolator )
	{
		this.mInterpolator = interpolator;
		return this;
	}
	
	private View getTargetView(
			View target ,
			MotionEvent event )
	{
		View view = null;
		if( target == null )
		{
			return view;
		}
		if( !eventInView( event , target ) )
		{
			return view;
		}
		if( !( target instanceof ViewGroup ) )
		{
			view = target;
			return view;
		}
		if( target instanceof AdapterView )
		{
			AdapterView<?> parent = (AdapterView<?>)target;
			int first = parent.getFirstVisiblePosition();
			int last = parent.getLastVisiblePosition();
			for( int index = 0 ; index <= ( last - first ) ; ++index )
			{
				View child = parent.getChildAt( index );
				if( !eventInView( event , child ) )
				{
					continue;
				}
				if( !( child instanceof ViewGroup ) )
				{
					view = child;
					return view;
				}
				view = getTargetView( child , event );
				// stop search in current view group
				return view;
			}
		}
		else if( target instanceof ViewGroup )
		{
			ViewGroup parent = (ViewGroup)target;
			int childCount = parent.getChildCount();
			// with z-order
			for( int index = childCount - 1 ; index >= 0 ; --index )
			{
				View child = parent.getChildAt( index );
				if( !eventInView( event , child ) )
				{
					continue;
				}
				if( !( child instanceof ViewGroup ) )
				{
					view = child;
					return view;
				}
				view = getTargetView( child , event );
				// stop search in current view group
				return view;
			}
		}
		// set view as group self
		view = target;
		return view;
	}
	
	private boolean eventInView(
			MotionEvent event ,
			View view )
	{
		if( event == null || view == null )
		{
			return false;
		}
		int eventX = (int)event.getRawX();
		int eventY = (int)event.getRawY();
		int[] location = new int[2];
		view.getLocationOnScreen( location );
		int width = view.getWidth();
		int height = view.getHeight();
		int left = location[0];
		int top = location[1];
		int right = left + width;
		int bottom = top + height;
		Rect rect = new Rect( left , top , right , bottom );
		boolean contains = rect.contains( eventX , eventY );
		return contains;
	}
	
	@Override
	public void overScrollBy(
			int deltaY )
	{
		if( !isOnthouchDown )
		{
			flingOverScroll( deltaY );
		}
	}
	
	/**
	 * 惯性滑动时超出边界的动画
	 * @param offset
	 * @author yangtianyu 2016-7-8
	 */
	private void flingOverScroll(
			int offset )
	{
		if( mState != State.STATE_FIT_CONTENT )
		{
			return;
		}
		if( offset != 0 )
		{
			boolean header = true;
			if( offset < 0 )
			{
				header = false;
			}
			//			int duration = Math.abs( offset );
			mBouncer.recover( header , offset , State.STATE_FLING_OVER_SCROLL );
		}
	}
	
	public interface BounceListener
	{
		
		public void onState(
				boolean header ,
				BounceScroller.State state );
		
		public void onOffset(
				boolean header ,
				int offset );
		
		/**
		 * 所有滚动的变化值,不管是手动拖拽或动画效果
		 * @param delta
		 * @author yangtianyu 2016-7-11
		 */
		public void onScrollDelta(
				int delta );
	}
}
