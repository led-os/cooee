package com.cooee.favorites.view;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.FavoritesPlugin;
import com.cooee.favorites.R;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.news.NewsView;
import com.cooee.favorites.recommended.FavoriteMainView;

import cool.sdk.search.SearchActivityManager;


public class FavoritesViewGroup extends FrameLayout
{
	
	public interface StickCallback
	{
		
		public void onStick(
				boolean stick );
		
		public boolean hasScrollToHead();
		
		public boolean canScrollToHead();
		
		public boolean isNeedHandlerTouchEvent(
				float x ,
				float y );
	}
	
	private static final String TAG = "FavoritesViewGroup";
	private OnTouchListener mTouchListener;
	private StickCallback stickCallback;
	private View headerView;//滑动过程中隐藏
	private View stickView;//滑动过程中移动到顶部
	private ValueAnimator anim;
	private boolean mIsExpanded = true;
	private int headerHeight = -1;
	private float ratio = 1;//1表示完全展开，0表示stickView被置顶（即：新闻全屏状态）
	protected final static int TOUCH_STATE_REST = 0;
	protected final static int TOUCH_STATE_SCROLLING = 1;
	protected int mTouchState = TOUCH_STATE_REST;
	private boolean fakeTouch = false;
	private Context mContext = null;
	private View mSearch;
	private int mSearchHeight = 0;
	private Rect mRect = new Rect();
	
	public FavoritesViewGroup(
			Context context )
	{
		super( context , null );
	}
	
	public FavoritesViewGroup(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		mContext = context;
		mTouchListener = new ExpandTouchListener();
		setOnTouchListener( mTouchListener );
	}
	
	public void setup(
			View headerView ,
			View listView )
	{
		if( headerView != null )
		{
			this.headerView = headerView;
			//chenchen add start
			int mainViewHeight = ( (FavoriteMainView)headerView ).getMainViewHeight();
			Log.d( "TAG" , "mainViewHeight" + mainViewHeight );
			Log.d( "TAG" , "headerView" + headerView );
			//chenchen add edd
			this.addView( this.headerView , ViewGroup.LayoutParams.MATCH_PARENT , mainViewHeight );
		}
		if( listView != null )
		{
			if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableAppsKey() , FavoriteConfigString.isEnableAppsDefaultValue() )//
					|| FavoritesManager.getInstance().isNearbyShow( mContext ) )
			{
				ratio = 1;
			}
			else
			{
				ratio = 0;
			}
			this.stickView = listView;
			this.addView( this.stickView , ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT );
		}
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableFavoritesSearchKey() , FavoriteConfigString.isEnableFavoritesSearchDefaultValue() ) )
		{
			//			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			FrameLayout.LayoutParams layoutParams;
			LayoutInflater inflater = LayoutInflater.from( mContext ).cloneInContext( mContext );
			mSearch = inflater.inflate( R.layout.favorite_search , null );
			if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() ) )
			{
				layoutParams = new FrameLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , (int)mContext.getResources().getDimension( R.dimen.favorites_simple_launcher_search_height ) );
				layoutParams.setMargins(
						FavoriteMainView.TITLE_PADDING_LIFT_RIGHT ,
						(int)mContext.getResources().getDimension( R.dimen.favorites_simple_launcher_search_top_margin ) ,
						FavoriteMainView.TITLE_PADDING_LIFT_RIGHT ,
						(int)mContext.getResources().getDimension( R.dimen.favorites_simple_launcher_search_bottom_margin ) );
				mSearch.setBackgroundResource( R.drawable.search_bar_simple_launcher_bg );
				ImageView button = (ImageView)mSearch.findViewById( R.id.imageButton1 );
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)button.getLayoutParams();
				params.width = (int)mContext.getResources().getDimension( R.dimen.favorites_simple_launcher_search_icon_height );
				params.height = (int)mContext.getResources().getDimension( R.dimen.favorites_simple_launcher_search_icon_height );
				button.setBackgroundResource( R.drawable.search_button_simple_launcher );
			}
			else
			{
				layoutParams = new FrameLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT );
				layoutParams.setMargins( FavoriteMainView.TITLE_PADDING_LIFT_RIGHT , 0 , FavoriteMainView.TITLE_PADDING_LIFT_RIGHT , 0 );
			}
			//		mSearch = new FavoriteSearchView( context );
			mSearch.setLayoutParams( layoutParams );
			View search_button = mSearch.findViewById( R.id.search_button_container );//cheyingkun add	//酷生活界面优化(酷生活搜索框点击效果)
			search_button.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					startSearchActivity();
				}
			} );
			addView( mSearch );
		}
	}
	
	public void addNewsView(
			View view )
	{
		if( view != null )
		{
			this.stickView = view;
			this.addView( this.stickView , ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT );
			if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableAppsKey() , FavoriteConfigString.isEnableAppsDefaultValue() )//
					|| FavoritesManager.getInstance().isNearbyShow( mContext ) )
			{
				ratio = 1;
			}
			else
			{
				ratio = 0;
			}
			int height = getHeaderViewHeight();
			if( headerHeight != height )
			{
				headerHeight = height;
			}
			stickView.setY( mSearchHeight + headerHeight * ratio );//放在附近等的下面
			mIsExpanded = true;
		}
	}
	
	/**
	 * 获取headerView的实际高度
	 */
	public int getHeaderViewHeight()
	{
		if( headerView == null )
		{
			return 0;
		}
		int height = ( (FavoriteMainView)headerView ).getMainViewHeight();//headerView的最大高度
		return height;
	}
	
	public void removeNewsView()
	{
		if( this.stickView != null && headerView != null )
		{
			ratio = 1;
			stickView.setY( mSearchHeight + headerHeight * ratio );
			headerView.setY( mSearchHeight + headerHeight * ratio - headerHeight );
			headerView.setPivotX( this.getWidth() / 2 );
			headerView.setPivotY( mSearchHeight + headerHeight );
			float scale = 0.8f + ratio * 0.2f;
			headerView.setScaleX( scale );
			headerView.setScaleY( scale );
			float alpha = ratio * ratio;
			headerView.setAlpha( alpha );
			if( stickView instanceof ViewGroup )
			{
				( (ViewGroup)stickView ).removeAllViews();
			}
			removeView( stickView );
			stickView = null;
			setStickCallback( null );
		}
	}
	
	public void setStickCallback(
			StickCallback callback )
	{
		this.stickCallback = callback;
	}
	
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		// TODO Auto-generated method stub
		super.onMeasure( widthMeasureSpec , heightMeasureSpec );
		if( headerView == null )
			return;
		int height = headerView.getMeasuredHeight();
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableFavoritesSearchKey() , FavoriteConfigString.isEnableFavoritesSearchDefaultValue() ) )
		{
			mSearchHeight = mSearch.getMeasuredHeight();
			mRect.set( 0 , mSearchHeight , getMeasuredWidth() , getMeasuredHeight() );
		}
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableNewsKey() , FavoriteConfigString.isEnableNewsDefaultValue() ) )
		{
			if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableFavoritesSearchKey() , FavoriteConfigString.isEnableFavoritesSearchDefaultValue() ) )//listview的那个高度应该是正规view的高度减去search的高度，否则下面有部分显示不出来
			{
				int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec( stickView.getMeasuredWidth() , View.MeasureSpec.EXACTLY );
				int childheightMeasureSpec = MeasureSpec.makeMeasureSpec( stickView.getMeasuredHeight() - mSearch.getMeasuredHeight() , View.MeasureSpec.EXACTLY );
				stickView.measure( childWidthMeasureSpec , childheightMeasureSpec );
			}
		}
		if( headerHeight == -1 )
		{
			headerHeight = height;
			update( true );
		}
		//cheyingkun add start	//解决“服务器关闭附近，附近处显示空白”的问题【i_0014374】
		else
		{
			update( false );
		}
		//cheyingkun add end
	}
	
	@Override
	protected void dispatchDraw(
			Canvas canvas )
	{
		if( ratio != 1f && FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableFavoritesSearchKey() , FavoriteConfigString.isEnableFavoritesSearchDefaultValue() ) )
		{
			canvas.save();
			canvas.clipRect( mRect );
		}
		super.dispatchDraw( canvas );
		if( ratio != 1f && FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableFavoritesSearchKey() , FavoriteConfigString.isEnableFavoritesSearchDefaultValue() ) )
		{
			canvas.restore();
			drawChild( canvas , mSearch , 0 );
		}
	}
	
	private void update(
			boolean isOnMeasure )
	{
		if( headerView == null )
			return;
		//cheyingkun add start	//添加折叠和显示更多交互动画。
		if( isOnMeasure )
		{
			if( stickView != null )
			{
				stickView.setY( stickView.getY() + mSearchHeight + headerHeight * ratio );
			}
		}
		else
		{
			int height = getHeaderViewHeight();
			if( headerHeight != height )
			{
				headerHeight = height;
			}
			if( stickView != null )
				stickView.setY( mSearchHeight + headerHeight * ratio );
		}
		//cheyingkun add end
		headerView.setY( mSearchHeight + headerHeight * ratio - headerHeight );
		headerView.setPivotX( this.getWidth() / 2 );
		headerView.setPivotY( mSearchHeight + headerHeight );
		float scale = 0.8f + ratio * 0.2f;
		headerView.setScaleX( scale );
		headerView.setScaleY( scale );
		float alpha = ratio * ratio;
		headerView.setAlpha( alpha );
		if( stickView != null && stickView instanceof NewsView )
		{
			( (NewsView)stickView ).update( ratio , alpha , scale , mSearchHeight );
		}
		if( stickCallback != null )
		{
			if( ratio == 1 && headerHeight > 0 )
				stickCallback.onStick( false );
			else if( ratio == 0 && headerHeight > 0 )
				stickCallback.onStick( true );
			else if( headerHeight == 0 )//headerHeight = 0说明只显示新闻，新闻为展开的情况
				stickCallback.onStick( true );
		}
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableFavoritesSearchKey() , FavoriteConfigString.isEnableFavoritesSearchDefaultValue() ) )
		{
			invalidate();
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(
			MotionEvent ev )
	{
		//		Log.d( TAG , "fav onInterceptTouchEvent:" + ev.getAction() );
		if( mTouchListener == null )
			return true;
		fakeTouch = true;
		boolean res = mTouchListener.onTouch( this , ev );
		fakeTouch = false;
		return res;
	}
	
	@Override
	public void requestDisallowInterceptTouchEvent(
			boolean disallowIntercept )
	{
		Log.d( TAG , "fav request disallow" );
		super.requestDisallowInterceptTouchEvent( disallowIntercept );
	}
	
	public void expandFavorites()
	{
		if( !mIsExpanded )
			( (ExpandTouchListener)mTouchListener ).expand();
	}
	
	private class ExpandTouchListener implements OnTouchListener
	{
		
		private float mLastTouchY;
		private float mTouchDownY;
		private final DecelerateInterpolator DECELERATOR = new DecelerateInterpolator();
		private final AnimListener ANIM_LISTENER = new AnimListener();
		private final AnimatorUpdateListener ANIM_UPDATE_LISTENER = new AnimUpdateListener();
		private final int ANIMATE_TO_COLLAPSE = 0;
		private final int ANIMATE_TO_EXPAND = 1;
		private final float DRAG_THRESHOLD = 0.2f;
		protected static final int INVALID_POINTER = -1;
		protected int mActivePointerId = INVALID_POINTER;
		protected int mTouchSlop;
		
		public ExpandTouchListener()
		{
			final ViewConfiguration configuration = ViewConfiguration.get( getContext() );
			if( configuration != null )
			{
				mTouchSlop = (int)( configuration.getScaledTouchSlop() );
			}
			else
				mTouchSlop = 10;
		}
		
		@Override
		public boolean onTouch(
				View v ,
				MotionEvent e )
		{
			//			Log.d( TAG , "fav onTouchEvent:" + e.getAction() );
			if( anim != null && anim.isRunning() )
			{
				resetTouchState();
				return true;
			}
			switch( e.getAction() )
			{
				case MotionEvent.ACTION_DOWN:
					final float y = e.getRawY();
					mTouchDownY = y;
					mLastTouchY = y;
					mActivePointerId = e.getPointerId( 0 );
					break;
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					if( mTouchState != TOUCH_STATE_SCROLLING )
					{
						break;
					}
					int animateTo = ANIMATE_TO_EXPAND;
					if( Math.abs( e.getRawY() - mTouchDownY ) > DRAG_THRESHOLD * headerHeight )
					{
						if( !mIsExpanded )
						{
							boolean openingDirection = e.getRawY() > mTouchDownY;
							if( openingDirection )
							{
								mIsExpanded = true;
								animateTo = ANIMATE_TO_EXPAND;
							}
							else
							{
								mIsExpanded = false;
								animateTo = ANIMATE_TO_COLLAPSE;
							}
						}
						else
						{
							boolean closingDirection = e.getRawY() < mTouchDownY;
							if( closingDirection )
							{
								mIsExpanded = false;
								animateTo = ANIMATE_TO_COLLAPSE;
							}
							else
							{
								mIsExpanded = true;
								animateTo = ANIMATE_TO_EXPAND;
							}
						}
					}
					else
					{
						animateTo = mIsExpanded ? ANIMATE_TO_EXPAND : ANIMATE_TO_COLLAPSE;
					}
					if( animateTo == ANIMATE_TO_EXPAND )
					{
						expand();
					}
					else
					{
						collapse();
					}
					resetTouchState();
					break;
				case MotionEvent.ACTION_MOVE:
					if( mTouchState == TOUCH_STATE_REST )
					{
						determineScrollingStart( e );
					}
					if( mTouchState == TOUCH_STATE_SCROLLING )
					{
						final float y2 = e.getRawY();
						final float dy = y2 - mLastTouchY;
						float newRatio = ratio + dy / headerHeight;
						if( newRatio < 0 )
						{
							newRatio = 0;
						}
						else if( newRatio > 1 )
						{
							newRatio = 1;
						}
						mLastTouchY = y2;
						if( newRatio != ratio )
						{
							ratio = newRatio;
							update( false );
						}
					}
					break;
			}
			if( !fakeTouch )
				return true;
			return mTouchState == TOUCH_STATE_SCROLLING;
		}
		
		/*
		 * Determines if we should change the touch state to start scrolling after the
		 * user moves their touch point too far.
		 */
		protected void determineScrollingStart(
				MotionEvent ev )
		{
			/*
			 * Locally do absolute value. mLastMotionX is set to the y value
			 * of the down event.
			 */
			final int pointerIndex = ev.findPointerIndex( mActivePointerId );
			if( pointerIndex == -1 )
				return;
			// Disallow scrolling if we started the gesture from outside the viewport
			final float y = ev.getRawY();
			final int yDiff = (int)Math.abs( y - mTouchDownY );
			final int touchSlop = Math.round( mTouchSlop );
			boolean yMoved = yDiff > touchSlop;
			if( stickCallback != null )
			{
				if( ratio == 0 && stickCallback.isNeedHandlerTouchEvent( ev.getRawX() , ev.getRawY() ) )
				{
					final ViewParent parent = getParent();
					if( parent != null )
					{
						parent.requestDisallowInterceptTouchEvent( true );
						return;
					}
				}
			}
			if( yMoved )
			{
				Log.d( "fav" , "fav yMoved:" + mIsExpanded + "," + ( stickCallback != null ? stickCallback.hasScrollToHead() : null ) + "," + ( y - mTouchDownY ) );
				if(
				//
				( stickCallback == null )
				//
				|| ( headerView == null )
				//
				|| ( headerView != null && headerView.getHeight() == 0 )
				//
				)
				{
					return;
				}
				if(
				//
				( mIsExpanded )
				//
				&& ( stickCallback != null && stickCallback.canScrollToHead() )
				//
				)
				{
					startScroll();
				}
				else
				{
					if( !mIsExpanded && stickCallback != null && stickCallback.hasScrollToHead() && y > mTouchDownY )
					{
						startScroll();
					}
				}
			}
		}
		
		private void startScroll()
		{
			// Scroll if the user moved far enough along the Y axis
			mTouchState = TOUCH_STATE_SCROLLING;
			final ViewParent parent = getParent();
			if( parent != null )
			{
				parent.requestDisallowInterceptTouchEvent( true );
			}
			Log.d( "fav" , "fav disallow" );
		}
		
		private void resetTouchState()
		{
			mTouchState = TOUCH_STATE_REST;
			mActivePointerId = INVALID_POINTER;
		}
		
		public void collapse()
		{//新闻由半屏到全屏
			mIsExpanded = false;
			if( anim != null )
				anim.cancel();
			anim = ValueAnimator.ofFloat( ratio , ANIMATE_TO_COLLAPSE );
			anim.setInterpolator( DECELERATOR );
			anim.addListener( ANIM_LISTENER );
			anim.addUpdateListener( ANIM_UPDATE_LISTENER );
			anim.start();
		}
		
		public void expand()
		{//新闻由全屏到半屏
			mIsExpanded = true;
			if( anim != null )
				anim.cancel();
			anim = ValueAnimator.ofFloat( ratio , ANIMATE_TO_EXPAND );
			anim.setInterpolator( DECELERATOR );
			anim.addListener( ANIM_LISTENER );
			anim.addUpdateListener( ANIM_UPDATE_LISTENER );
			anim.start();
		}
	}
	
	private class AnimListener implements AnimatorListener
	{
		
		@Override
		public void onAnimationEnd(
				Animator animation )
		{
		}
		
		@Override
		public void onAnimationRepeat(
				Animator animation )
		{
		}
		
		@Override
		public void onAnimationStart(
				Animator animation )
		{
		}
		
		@Override
		public void onAnimationCancel(
				Animator animation )
		{
		}
	}
	
	private class AnimUpdateListener implements AnimatorUpdateListener
	{
		
		@Override
		public void onAnimationUpdate(
				ValueAnimator animation )
		{
			ratio = (Float)animation.getAnimatedValue();
			update( false );
		}
	}
	
	private void startSearchActivity()
	{
		Bundle appSearchData = new Bundle();
		appSearchData.putString( "source" , "launcher-search" );
		Context containerContext = FavoritesManager.getInstance().getContainerContext();//桌面
		Context proxyContext = FavoritesManager.getInstance().getProxyContext();//custom:proxy else:桌面
		SearchActivityManager.getInstance( containerContext ).setHostPackageName( FavoritesPlugin.PluginPackageName );
		SearchActivityManager.getInstance( containerContext ).startSearchActivity( proxyContext , null , false , appSearchData , null );
		SearchActivityManager.getInstance( containerContext ).setHostPackageName( null );
	}
	
	//cheyingkun add start	//修改酷生活S5引导页动画。
	private float translatehX = 0;
	
	public AnimatorSet getFavoritesViewShowAnimation()
	{
		if( headerView == null && stickView == null )
		{
			return null;
		}
		FavoritesManager mFavoritesManager = FavoritesManager.getInstance();
		//动画延迟
		int favoritesViewAnimShowDelay = FavoritesManager.getInstance().getFavoritesViewAnimShowDelay();
		//动画集合
		AnimatorSet favoritesViewGroupAnimatorSet = new AnimatorSet();
		if( mSearch != null )
		{
			//搜索动画
			PropertyValuesHolder mSearchX = PropertyValuesHolder.ofFloat( "x" , mSearch.getX() + translatehX );
			ObjectAnimator mSearchAnim = new ObjectAnimator();
			mSearchAnim.setInterpolator( new DecelerateInterpolator() );
			mSearchAnim.setTarget( mSearch );
			mSearchAnim.setValues( mSearchX );
			//动画延迟系数
			int showFavoritesViewDelayNum = mFavoritesManager.getShowFavoritesViewDelayNum();
			showFavoritesViewDelayNum++;
			mFavoritesManager.setShowFavoritesViewDelayNum( showFavoritesViewDelayNum );
			mSearchAnim.setStartDelay( favoritesViewAnimShowDelay * showFavoritesViewDelayNum );
			favoritesViewGroupAnimatorSet.playTogether( mSearchAnim );
		}
		if( headerView != null )
		{
			//联系人、应用、附近的动画集合
			FavoriteMainView view = (FavoriteMainView)headerView;
			AnimatorSet animatorSet = view.getFavoritesViewShowAnimation();
			if( animatorSet != null )
			{
				favoritesViewGroupAnimatorSet.playTogether( animatorSet );
			}
		}
		if( stickView != null )
		{
			//新闻动画
			PropertyValuesHolder stickViewX = PropertyValuesHolder.ofFloat( "x" , stickView.getX() + translatehX );
			ObjectAnimator stickViewAnim = new ObjectAnimator();
			stickViewAnim.setInterpolator( new DecelerateInterpolator() );
			stickViewAnim.setTarget( stickView );
			stickViewAnim.setValues( stickViewX );
			//动画延迟系数
			int showFavoritesViewDelayNum = mFavoritesManager.getShowFavoritesViewDelayNum();
			showFavoritesViewDelayNum++;
			mFavoritesManager.setShowFavoritesViewDelayNum( showFavoritesViewDelayNum );
			stickViewAnim.setStartDelay( favoritesViewAnimShowDelay * showFavoritesViewDelayNum );
			favoritesViewGroupAnimatorSet.playTogether( stickViewAnim );
		}
		return favoritesViewGroupAnimatorSet;
	}
	
	public void setFavoriteClingsShowX(
			int x )
	{
		translatehX = -x;
		if( mSearch != null )
		{
			mSearch.setX( x );
		}
		if( headerView != null )
		{
			FavoriteMainView view = (FavoriteMainView)headerView;
			view.setFavoriteClingsShowX( x );
		}
		if( stickView != null )
		{
			stickView.setX( x );
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//添加折叠和显示更多交互动画。
	public void updateHeaderHeight()
	{
		update( false );
	}
	//cheyingkun add end
	;
	
	public boolean isNewsExpandedMode()
	{//新闻是否为全屏（展开）状态
		return( ratio == 0 );
	}
	
	public boolean isNewsCollapseMode()
	{//新闻是否为半屏（折叠）状态
		return( ratio == 1 );
	}
	
	//cheyingkun add start	//酷生活支持动态修改图标大小
	public void onIconSizeChanged(
			FavoriteMainView mFavoriteMainView ,
			View mNewsView )
	{
		removeAllViews();
		setup( mFavoriteMainView , mNewsView );
		update( false );
	}
	
	//cheyingkun add end
	//cheyingkun add start	//解决“常用应用添加删除第一行时,推荐应用显示异常”的问题。（逻辑完善）
	public void showViewYLog()
	{
		Log.d( "" , "cyk FavoritesViewGroup: " + this.getY() + " h: " + this.getHeight() );
	};
	//cheyingkun add end
}
