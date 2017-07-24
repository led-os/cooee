package com.cooee.phenix;


import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.DropTarget.DragObject;
import com.cooee.phenix.Folder.FolderIcon;
import com.cooee.phenix.PagedView.PagedView;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.CellInfo;
import com.cooee.phenix.data.FolderInfo;
import com.cooee.phenix.data.LauncherAppWidgetInfo;
import com.cooee.phenix.data.PendingAddItemInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.util.TouchController;


/**
 * Class for initiating a drag within a view or across multiple views.
 */
public class DragController
//
implements TouchController //zhujieping add //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
{
	
	private static final String TAG = "Launcher.DragController";
	/** Indicates the drag is a move.  */
	public static int DRAG_ACTION_MOVE = 0;
	/** Indicates the drag is a copy.  */
	public static int DRAG_ACTION_COPY = 1;
	private static final int SCROLL_DELAY = 500;
	private static final int RESCROLL_DELAY = PagedView.PAGE_SNAP_ANIMATION_DURATION + 150;
	private static final boolean PROFILE_DRAWING_DURING_DRAG = false;
	private static final int SCROLL_OUTSIDE_ZONE = 0;
	private static final int SCROLL_WAITING_IN_ZONE = 1;
	static final int SCROLL_NONE = -1;
	static final int SCROLL_LEFT = 0;
	static final int SCROLL_RIGHT = 1;
	private static final float MAX_FLING_DEGREES = 35f;
	private Launcher mLauncher;
	private Handler mHandler;
	// temporaries to avoid gc thrash
	private Rect mRectTemp = new Rect();
	private final int[] mCoordinatesTemp = new int[2];
	/** Whether or not we're dragging. */
	private boolean mDragging;
	/** X coordinate of the down event. */
	private int mMotionDownX;
	/** Y coordinate of the down event. */
	private int mMotionDownY;
	/** the area at the edge of the screen that makes the workspace go left
	 *   or right while you're dragging.
	 */
	private int mScrollZone;
	private DropTarget.DragObject mDragObject;
	/** Who can receive drop events */
	private ArrayList<DropTarget> mDropTargets = new ArrayList<DropTarget>();
	private ArrayList<DragListener> mListeners = new ArrayList<DragListener>();
	private DropTarget mFlingToDeleteDropTarget;
	/** The window token used as the parent for the DragView. */
	private IBinder mWindowToken;
	/** The view that will be scrolled when dragging to the left and right edges of the screen. */
	private View mScrollView;
	private View mMoveTarget;
	private DragScroller mDragScroller;
	private int mScrollState = SCROLL_OUTSIDE_ZONE;
	private ScrollRunnable mScrollRunnable = new ScrollRunnable();
	private DropTarget mLastDropTarget;
	private InputMethodManager mInputMethodManager;
	private int mLastTouch[] = new int[2];
	private long mLastTouchUpTime = -1;
	private int mDistanceSinceScroll = 0;
	private int mTmpPoint[] = new int[2];
	private Rect mDragLayerRect = new Rect();
	protected int mFlingToDeleteThresholdVelocity;
	private VelocityTracker mVelocityTracker;
	
	/**
	 * Interface to receive notifications when a drag starts or stops
	 */
	interface DragListener
	{
		
		/**
		 * A drag has begun
		 *
		 * @param source An object representing where the drag originated
		 * @param info The data associated with the object that is being dragged
		 * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
		 *        or {@link DragController#DRAG_ACTION_COPY}
		 */
		void onDragStart(
				DragSource source ,
				Object info ,
				int dragAction );
		
		/**
		 * The drag has ended
		 */
		void onDragEnd();
	}
	
	/**
	 * Used to create a new DragLayer from XML.
	 *
	 * @param context The application's context.
	 */
	public DragController(
			Launcher launcher )
	{
		Resources r = launcher.getResources();
		mLauncher = launcher;
		mHandler = new Handler();
		mScrollZone = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.scroll_zone );
		mVelocityTracker = VelocityTracker.obtain();
		float density = r.getDisplayMetrics().density;
		mFlingToDeleteThresholdVelocity = (int)( LauncherDefaultConfig.getInt( R.integer.config_flingToDeleteMinVelocity ) * density );
	}
	
	public boolean dragging()
	{
		return mDragging;
	}
	
	/**
	 * Starts a drag.
	 *
	 * @param v The view that is being dragged
	 * @param bmp The bitmap that represents the view being dragged
	 * @param source An object representing where the drag originated
	 * @param dragInfo The data associated with the object that is being dragged
	 * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
	 *        {@link #DRAG_ACTION_COPY}
	 * @param dragRegion Coordinates within the bitmap b for the position of item being dragged.
	 *          Makes dragging feel more precise, e.g. you can clip out a transparent border
	 */
	public void startDrag(
			View v ,
			Bitmap bmp ,
			DragSource source ,
			Object dragInfo ,
			int dragAction ,
			Point extraPadding ,
			float initialDragViewScale )
	{
		int[] loc = mCoordinatesTemp;
		mLauncher.getDragLayer().getLocationInDragLayer( v , loc );
		int viewExtraPaddingLeft = extraPadding != null ? extraPadding.x : 0;
		int viewExtraPaddingTop = extraPadding != null ? extraPadding.y : 0;
		int dragLayerX = loc[0] + v.getPaddingLeft() + viewExtraPaddingLeft + (int)( ( initialDragViewScale * bmp.getWidth() - bmp.getWidth() ) / 2 );
		int dragLayerY = loc[1] + v.getPaddingTop() + viewExtraPaddingTop + (int)( ( initialDragViewScale * bmp.getHeight() - bmp.getHeight() ) / 2 );
		startDrag( bmp , dragLayerX , dragLayerY , source , dragInfo , dragAction , null , null , initialDragViewScale );
		if( dragAction == DRAG_ACTION_MOVE )
		{
			v.setVisibility( View.GONE );
		}
	}
	
	/**
	 * Starts a drag.
	 *
	 * @param b The bitmap to display as the drag image.  It will be re-scaled to the
	 *          enlarged size.
	 * @param dragLayerX The x position in the DragLayer of the left-top of the bitmap.
	 * @param dragLayerY The y position in the DragLayer of the left-top of the bitmap.
	 * @param source An object representing where the drag originated
	 * @param dragInfo The data associated with the object that is being dragged
	 * @param dragAction The drag action: either {@link #DRAG_ACTION_MOVE} or
	 *        {@link #DRAG_ACTION_COPY}
	 * @param dragRegion Coordinates within the bitmap b for the position of item being dragged.
	 *          Makes dragging feel more precise, e.g. you can clip out a transparent border
	 */
	public void startDrag(
			Bitmap b ,
			int dragLayerX ,
			int dragLayerY ,
			DragSource source ,
			Object dragInfo ,
			int dragAction ,
			Point dragOffset ,
			Rect dragRegion ,
			float initialDragViewScale )
	{
		if( PROFILE_DRAWING_DURING_DRAG )
		{
			android.os.Debug.startMethodTracing( "Launcher" );
		}
		// Hide soft keyboard, if visible
		if( mInputMethodManager == null )
		{
			mInputMethodManager = (InputMethodManager)mLauncher.getSystemService( Context.INPUT_METHOD_SERVICE );
		}
		mInputMethodManager.hideSoftInputFromWindow( mWindowToken , 0 );
		for( DragListener listener : mListeners )
		{
			listener.onDragStart( source , dragInfo , dragAction );
		}
		final int registrationX = mMotionDownX - dragLayerX;
		final int registrationY = mMotionDownY - dragLayerY;
		final int dragRegionLeft = dragRegion == null ? 0 : dragRegion.left;
		final int dragRegionTop = dragRegion == null ? 0 : dragRegion.top;
		mDragging = true;
		mDragObject = new DropTarget.DragObject();
		mDragObject.dragComplete = false;
		mDragObject.xOffset = mMotionDownX - ( dragLayerX + dragRegionLeft );
		mDragObject.yOffset = mMotionDownY - ( dragLayerY + dragRegionTop );
		mDragObject.dragSource = source;
		mDragObject.dragInfo = dragInfo;
		final DragView dragView = mDragObject.dragView = new DragView( mLauncher , b , registrationX , registrationY , 0 , 0 , b.getWidth() , b.getHeight() , initialDragViewScale );
		if( dragOffset != null )
		{
			dragView.setDragVisualizeOffset( new Point( dragOffset ) );
		}
		if( dragRegion != null )
		{
			dragView.setDragRegion( new Rect( dragRegion ) );
		}
		mLauncher.getDragLayer().performHapticFeedback( HapticFeedbackConstants.LONG_PRESS );
		dragView.show( mMotionDownX , mMotionDownY );
		handleMoveEvent( mMotionDownX , mMotionDownY );
	}
	
	/**
	 * Draw the view into a bitmap.
	 */
	Bitmap getViewBitmap(
			View v )
	{
		v.clearFocus();
		v.setPressed( false );
		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing( false );
		// Reset the drawing cache background color to fully transparent
		// for the duration of this operation
		int color = v.getDrawingCacheBackgroundColor();
		v.setDrawingCacheBackgroundColor( 0 );
		float alpha = v.getAlpha();
		v.setAlpha( 1.0f );
		if( color != 0 )
		{
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();
		if( cacheBitmap == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat( "failed getViewBitmap view(" + v , ")" ) , new RuntimeException() );
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap( cacheBitmap );
		// Restore the view
		v.destroyDrawingCache();
		v.setAlpha( alpha );
		v.setWillNotCacheDrawing( willNotCache );
		v.setDrawingCacheBackgroundColor( color );
		return bitmap;
	}
	
	/**
	 * Call this from a drag source view like this:
	 *
	 * <pre>
	 *  @Override
	 *  public boolean dispatchKeyEvent(KeyEvent event) {
	 *      return mDragController.dispatchKeyEvent(this, event)
	 *              || super.dispatchKeyEvent(event);
	 * </pre>
	 */
	public boolean dispatchKeyEvent(
			KeyEvent event )
	{
		return mDragging;
	}
	
	public boolean isDragging()
	{
		return mDragging;
	}
	
	/**
	 * Stop dragging without dropping.
	 */
	public void cancelDrag()
	{
		if( mDragging )
		{
			if( mLastDropTarget != null )
			{
				mLastDropTarget.onDragExit( mDragObject );
			}
			mDragObject.deferDragViewCleanupPostAnimation = false;
			mDragObject.cancelled = true;
			mDragObject.dragComplete = true;
			mDragObject.dragSource.onDropCompleted( null , mDragObject , false , false );
		}
		endDrag();
	}
	
	// zhujieping@2015/05/12 ADD START
	public void onPackagesRemoved(
			ArrayList<String> packages ,
			Context context )
	{
		if( mDragObject != null )
		{
			Object rawDragInfo = mDragObject.dragInfo;
			if( rawDragInfo instanceof ShortcutInfo )
			{
				ShortcutInfo dragInfo = (ShortcutInfo)rawDragInfo;
				for( String pkg : packages )
				{
					if( dragInfo != null && dragInfo.getIntent() != null && dragInfo.getIntent().getComponent() != null )
					{
						boolean isSameComponent = dragInfo.getIntent().getComponent().getPackageName().equals( pkg );
						if( isSameComponent )
						{
							cancelDrag();
							return;
						}
					}
				}
			}
			else if( rawDragInfo instanceof AppInfo )
			{
				AppInfo dragInfo = (AppInfo)rawDragInfo;
				for( String pkg : packages )
				{
					if( dragInfo != null && dragInfo.getIntent() != null && dragInfo.getIntent().getComponent() != null )
					{
						boolean isSameComponent = dragInfo.getIntent().getComponent().getPackageName().equals( pkg );
						if( isSameComponent )
						{
							cancelDrag();
							mLauncher.showWorkspaceIfNotEidtMode();
							return;
						}
					}
				}
			}
			else if( rawDragInfo instanceof LauncherAppWidgetInfo )
			{
				LauncherAppWidgetInfo dragInfo = (LauncherAppWidgetInfo)rawDragInfo;
				for( String pkg : packages )
				{
					if( dragInfo != null && dragInfo.getProviderComponentName() != null )
					{
						boolean isSamePackage = dragInfo.getProviderComponentName().getPackageName().equals( pkg );
						if( isSamePackage )
						{
							cancelDrag();
							return;
						}
					}
				}
			}
			else if( rawDragInfo instanceof PendingAddItemInfo )
			{
				PendingAddItemInfo dragInfo = (PendingAddItemInfo)rawDragInfo;
				for( String pkg : packages )
				{
					if( dragInfo != null && dragInfo.getComponentName() != null )
					{
						boolean isSamePackage = dragInfo.getComponentName().getPackageName().equals( pkg );
						if( isSamePackage )
						{
							cancelDrag();
							return;
						}
					}
				}
			}
		}
	}
	
	// zhujieping@2015/05/12 ADD END
	public void onAppsRemoved(
			ArrayList<AppInfo> appInfos ,
			Context context )
	{
		// Cancel the current drag if we are removing an app that we are dragging
		if( mDragObject != null )
		{
			Object rawDragInfo = mDragObject.dragInfo;
			if( rawDragInfo instanceof ShortcutInfo )
			{
				ShortcutInfo dragInfo = (ShortcutInfo)rawDragInfo;
				for( AppInfo info : appInfos )
				{
					// Added null checks to prevent NPE we've seen in the wild
					//zhujieping modify,添加保护，例如“1X1的'快捷拨号'的dragInfo.getIntent().getComponent()为空。
					//if( dragInfo != null && dragInfo.getIntent() != null  )
					if( dragInfo != null && dragInfo.getIntent() != null && dragInfo.getIntent().getComponent() != null )
					{
						boolean isSameComponent = dragInfo.getIntent().getComponent().equals( info.getComponentName() );
						if( isSameComponent )
						{
							cancelDrag();
							return;
						}
					}
				}
			}
			//cheyingkun add start	//解决“桌面为双层模式时长按主菜单应用图标时PC端卸载该应用，卸载成功后松手，该图标未消失显示为机器人”的问题。（bug：0010077）
			else if( rawDragInfo instanceof AppInfo )
			{
				AppInfo dragInfo = (AppInfo)rawDragInfo;
				for( AppInfo info : appInfos )
				{
					//zhujieping modify,添加保护
					//if( dragInfo != null && dragInfo.getIntent() != null )
					if( dragInfo != null && dragInfo.getIntent() != null && dragInfo.getIntent().getComponent() != null )
					{
						boolean isSameComponent = dragInfo.getIntent().getComponent().equals( info.getComponentName() );
						if( isSameComponent )
						{
							cancelDrag();
							mLauncher.showWorkspaceIfNotEidtMode();
							return;
						}
					}
				}
			}
			//cheyingkun add end
			// zhujieping@2015/03/30 ADD START
			//长按桌面“酷狗音乐”插件同时并在PC机上卸载“酷狗音乐”，报“桌面停止运行”【i_0010743】
			else if( rawDragInfo instanceof LauncherAppWidgetInfo )
			{
				LauncherAppWidgetInfo dragInfo = (LauncherAppWidgetInfo)rawDragInfo;
				for( AppInfo info : appInfos )
				{
					if( dragInfo != null && dragInfo.getProviderComponentName() != null )
					{
						boolean isSamePackage = dragInfo.getProviderComponentName().getPackageName().equals( info.getComponentName().getPackageName() );
						if( isSamePackage )
						{
							cancelDrag();
							return;
						}
					}
				}
			}
			// zhujieping@2015/03/30 ADD END
			//cheyingkun add start	//解决“小部件界面长按一个小部件不松手，pc端卸载小部件，卸载成功后松手，桌面停止运行。”的问题。【i_0010985】
			else if( rawDragInfo instanceof PendingAddItemInfo )
			{
				PendingAddItemInfo dragInfo = (PendingAddItemInfo)rawDragInfo;
				for( AppInfo info : appInfos )
				{
					if( dragInfo != null && dragInfo.getComponentName() != null )
					{
						boolean isSamePackage = dragInfo.getComponentName().getPackageName().equals( info.getComponentName().getPackageName() );
						if( isSamePackage )
						{
							cancelDrag();
							return;
						}
					}
				}
			}
			//cheyingkun add end
		}
	}
	
	private void endDrag()
	{
		if( mDragging )
		{
			mDragging = false;
			clearScrollRunnable();
			boolean isDeferred = false;
			if( mDragObject.dragView != null )
			{
				isDeferred = mDragObject.deferDragViewCleanupPostAnimation;
				if( !isDeferred )
				{
					mDragObject.dragView.remove();
				}
				mDragObject.dragView = null;
			}
			// Only end the drag if we are not deferred
			if( !isDeferred )
			{
				for( DragListener listener : mListeners )
				{
					listener.onDragEnd();
				}
			}
		}
		releaseVelocityTracker();
	}
	
	/**
	 * This only gets called as a result of drag view cleanup being deferred in endDrag();
	 */
	void onDeferredEndDrag(
			DragView dragView )
	{
		dragView.remove();
		if( mDragObject.deferDragViewCleanupPostAnimation )
		{
			// If we skipped calling onDragEnd() before, do it now
			for( DragListener listener : mListeners )
			{
				listener.onDragEnd();
			}
		}
	}
	
	void onDeferredEndFling(
			DropTarget.DragObject d )
	{
		d.dragSource.onFlingToDeleteCompleted();
	}
	
	/**
	 * Clamps the position to the drag layer bounds.
	 */
	private int[] getClampedDragLayerPos(
			float x ,
			float y )
	{
		mLauncher.getDragLayer().getLocalVisibleRect( mDragLayerRect );
		mTmpPoint[0] = (int)Math.max( mDragLayerRect.left , Math.min( x , mDragLayerRect.right - 1 ) );
		mTmpPoint[1] = (int)Math.max( mDragLayerRect.top , Math.min( y , mDragLayerRect.bottom - 1 ) );
		return mTmpPoint;
	}
	
	long getLastGestureUpTime()
	{
		if( mDragging )
		{
			return System.currentTimeMillis();
		}
		else
		{
			return mLastTouchUpTime;
		}
	}
	
	void resetLastGestureUpTime()
	{
		mLastTouchUpTime = -1;
	}
	
	/**
	 * Call this from a drag source view.
	 */
	public boolean onInterceptTouchEvent(
			MotionEvent ev )
	{
		@SuppressWarnings( "all" )
		// suppress dead code warning
		final boolean debug = false;
		if( debug )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( Launcher.TAG , StringUtils.concat( "DragController.onInterceptTouchEvent--" , " mDragging=" , mDragging , "ev=" + ev ) );
		}
		// Update the velocity tracker
		acquireVelocityTrackerAndAddMovement( ev );
		final int action = ev.getAction();
		final int[] dragLayerPos = getClampedDragLayerPos( ev.getX() , ev.getY() );
		final int dragLayerX = dragLayerPos[0];
		final int dragLayerY = dragLayerPos[1];
		switch( action )
		{
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_DOWN:
				// Remember location of down touch
				mMotionDownX = dragLayerX;
				mMotionDownY = dragLayerY;
				mLastDropTarget = null;
				break;
			case MotionEvent.ACTION_UP:
				mLastTouchUpTime = System.currentTimeMillis();
				if( mDragging )
				{
					PointF vec = isFlingingToDelete( mDragObject.dragSource );
					if( !DeleteDropTarget.willAcceptDrop( mDragObject.dragInfo ) )
					{
						vec = null;
					}
					if( vec != null )
					{
						dropOnFlingToDeleteTarget( dragLayerX , dragLayerY , vec );
					}
					else
					{
						drop( dragLayerX , dragLayerY );
					}
				}
				endDrag();
				break;
			case MotionEvent.ACTION_CANCEL:
				cancelDrag();
				break;
		}
		return mDragging;
	}
	
	/**
	 * Sets the view that should handle move events.
	 */
	void setMoveTarget(
			View view )
	{
		mMoveTarget = view;
	}
	
	public boolean dispatchUnhandledMove(
			View focused ,
			int direction )
	{
		return mMoveTarget != null && mMoveTarget.dispatchUnhandledMove( focused , direction );
	}
	
	private void clearScrollRunnable()
	{
		mHandler.removeCallbacks( mScrollRunnable );
		if( mScrollState == SCROLL_WAITING_IN_ZONE )
		{
			mScrollState = SCROLL_OUTSIDE_ZONE;
			mScrollRunnable.setDirection( SCROLL_RIGHT );
			mDragScroller.onExitScrollArea();
			mLauncher.getDragLayer().onExitScrollArea();
		}
	}
	
	private void handleMoveEvent(
			int x ,
			int y )
	{
		mDragObject.dragView.move( x , y );
		// Drop on someone?
		final int[] coordinates = mCoordinatesTemp;
		DropTarget dropTarget = findDropTarget( x , y , coordinates );
		mDragObject.x = coordinates[0];
		mDragObject.y = coordinates[1];
		checkTouchMove( dropTarget );
		// Check if we are hovering over the scroll areas
		mDistanceSinceScroll += Math.sqrt( Math.pow( mLastTouch[0] - x , 2 ) + Math.pow( mLastTouch[1] - y , 2 ) );
		mLastTouch[0] = x;
		mLastTouch[1] = y;
		checkScrollState( x , y );
	}
	
	public void forceTouchMove()
	{
		int[] dummyCoordinates = mCoordinatesTemp;
		DropTarget dropTarget = findDropTarget( mLastTouch[0] , mLastTouch[1] , dummyCoordinates );
		mDragObject.x = dummyCoordinates[0];
		mDragObject.y = dummyCoordinates[1];
		checkTouchMove( dropTarget );
	}
	
	private void checkTouchMove(
			DropTarget dropTarget )
	{
		if( dropTarget != null )
		{
			if( mLastDropTarget != dropTarget )
			{
				if( mLastDropTarget != null )
				{
					mLastDropTarget.onDragExit( mDragObject );
				}
				dropTarget.onDragEnter( mDragObject );
			}
			dropTarget.onDragOver( mDragObject );
		}
		else
		{
			if( mLastDropTarget != null )
			{
				mLastDropTarget.onDragExit( mDragObject );
			}
		}
		mLastDropTarget = dropTarget;
	}
	
	private void checkScrollState(
			int x ,
			int y )
	{
		final int slop = ViewConfiguration.get( mLauncher ).getScaledWindowTouchSlop();
		final int delay = mDistanceSinceScroll < slop ? RESCROLL_DELAY : SCROLL_DELAY;
		final DragLayer dragLayer = mLauncher.getDragLayer();
		// gaominghui@2016/12/14 ADD START
		//xiatian start	//整理判断“是否从左往右布局”的方法：由“mView.getLayoutDirection()”改为“getResources().getConfiguration().getLayoutDirection()”
		//		final boolean isRtl = Tools.isLayoutRTL( dragLayer );//xiatian del
		final boolean isRtl = LauncherAppState.isLayoutRTL();//xiatian add 
		//xiatian end
		//final boolean isRtl = ( dragLayer.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL );
		// gaominghui@2016/12/14 ADD END
		final int forwardDirection = isRtl ? SCROLL_RIGHT : SCROLL_LEFT;
		final int backwardsDirection = isRtl ? SCROLL_LEFT : SCROLL_RIGHT;
		if( x < mScrollZone )
		{
			if( mScrollState == SCROLL_OUTSIDE_ZONE )
			{
				mScrollState = SCROLL_WAITING_IN_ZONE;
				if( mDragScroller.onEnterScrollArea( x , y , forwardDirection ) )
				{
					dragLayer.onEnterScrollArea( forwardDirection );
					mScrollRunnable.setDirection( forwardDirection );
					mHandler.postDelayed( mScrollRunnable , delay );
				}
			}
		}
		else if( x > mScrollView.getWidth() - mScrollZone )
		{
			if( mScrollState == SCROLL_OUTSIDE_ZONE )
			{
				mScrollState = SCROLL_WAITING_IN_ZONE;
				if( mDragScroller.onEnterScrollArea( x , y , backwardsDirection ) )
				{
					dragLayer.onEnterScrollArea( backwardsDirection );
					mScrollRunnable.setDirection( backwardsDirection );
					mHandler.postDelayed( mScrollRunnable , delay );
				}
			}
		}
		else
		{
			clearScrollRunnable();
		}
	}
	
	/**
	 * Call this from a drag source view.
	 */
	public boolean onTouchEvent(
			MotionEvent ev )
	{
		if( !mDragging )
		{
			return false;
		}
		// Update the velocity tracker
		acquireVelocityTrackerAndAddMovement( ev );
		final int action = ev.getAction();
		final int[] dragLayerPos = getClampedDragLayerPos( ev.getX() , ev.getY() );
		final int dragLayerX = dragLayerPos[0];
		final int dragLayerY = dragLayerPos[1];
		switch( action )
		{
			case MotionEvent.ACTION_DOWN:
				// Remember where the motion event started
				mMotionDownX = dragLayerX;
				mMotionDownY = dragLayerY;
				if( ( dragLayerX < mScrollZone ) || ( dragLayerX > mScrollView.getWidth() - mScrollZone ) )
				{
					mScrollState = SCROLL_WAITING_IN_ZONE;
					mHandler.postDelayed( mScrollRunnable , SCROLL_DELAY );
				}
				else
				{
					mScrollState = SCROLL_OUTSIDE_ZONE;
				}
				handleMoveEvent( dragLayerX , dragLayerY );
				break;
			case MotionEvent.ACTION_MOVE:
				if( ev.getPointerCount() == 1 )//cheyingkun add	//解决“第一个手指长按桌面图标，另一个手指在按住桌面空白处不松开，松开第一个手指后再点击桌面任意空白处，图标闪烁”的问题。【i_0011723】
				{
					handleMoveEvent( dragLayerX , dragLayerY );
				}
				break;
			case MotionEvent.ACTION_UP:
				// Ensure that we've processed a move event at the current pointer location.
				handleMoveEvent( dragLayerX , dragLayerY );
				mHandler.removeCallbacks( mScrollRunnable );
				if( mDragging )
				{
					PointF vec = isFlingingToDelete( mDragObject.dragSource );
					if( !DeleteDropTarget.willAcceptDrop( mDragObject.dragInfo ) )
					{
						vec = null;
					}
					if( vec != null )
					{
						dropOnFlingToDeleteTarget( dragLayerX , dragLayerY , vec );
					}
					else
					{
						drop( dragLayerX , dragLayerY );
					}
				}
				endDrag();
				break;
			case MotionEvent.ACTION_CANCEL:
				mHandler.removeCallbacks( mScrollRunnable );
				cancelDrag();
				break;
		}
		return true;
	}
	
	/**
	 * Determines whether the user flung the current item to delete it.
	 *
	 * @return the vector at which the item was flung, or null if no fling was detected.
	 */
	private PointF isFlingingToDelete(
			DragSource source )
	{
		if( mFlingToDeleteDropTarget == null )
			return null;
		if( !source.supportsFlingToDelete() )
			return null;
		ViewConfiguration config = ViewConfiguration.get( mLauncher );
		mVelocityTracker.computeCurrentVelocity( 1000 , config.getScaledMaximumFlingVelocity() );
		if( mVelocityTracker.getYVelocity() < mFlingToDeleteThresholdVelocity )
		{
			// Do a quick dot product test to ensure that we are flinging upwards
			PointF vel = new PointF( mVelocityTracker.getXVelocity() , mVelocityTracker.getYVelocity() );
			PointF upVec = new PointF( 0f , -1f );
			float theta = (float)Math.acos( ( ( vel.x * upVec.x ) + ( vel.y * upVec.y ) ) / ( vel.length() * upVec.length() ) );
			if( theta <= Math.toRadians( MAX_FLING_DEGREES ) )
			{
				return vel;
			}
		}
		return null;
	}
	
	private void dropOnFlingToDeleteTarget(
			float x ,
			float y ,
			PointF vel )
	{
		final int[] coordinates = mCoordinatesTemp;
		mDragObject.x = coordinates[0];
		mDragObject.y = coordinates[1];
		// Clean up dragging on the target if it's not the current fling delete target otherwise,
		// start dragging to it.
		if( mLastDropTarget != null && mFlingToDeleteDropTarget != mLastDropTarget )
		{
			mLastDropTarget.onDragExit( mDragObject );
		}
		// Drop onto the fling-to-delete target
		boolean accepted = false;
		mFlingToDeleteDropTarget.onDragEnter( mDragObject );
		// We must set dragComplete to true _only_ after we "enter" the fling-to-delete target for
		// "drop"
		mDragObject.dragComplete = true;
		mFlingToDeleteDropTarget.onDragExit( mDragObject );
		if( mFlingToDeleteDropTarget.acceptDrop( mDragObject ) )
		{
			mFlingToDeleteDropTarget.onFlingToDelete( mDragObject , mDragObject.x , mDragObject.y , vel );
			accepted = true;
		}
		mDragObject.dragSource.onDropCompleted( (View)mFlingToDeleteDropTarget , mDragObject , true , accepted );
	}
	
	private void drop(
			float x ,
			float y )
	{
		final int[] coordinates = mCoordinatesTemp;
		final DropTarget dropTarget = findDropTarget( (int)x , (int)y , coordinates );
		mDragObject.x = coordinates[0];
		mDragObject.y = coordinates[1];
		boolean accepted = false;
		if( dropTarget != null )
		{
			mDragObject.dragComplete = true;
			dropTarget.onDragExit( mDragObject );
			if( dropTarget.acceptDrop( mDragObject ) )
			{
				dropTarget.onDrop( mDragObject );
				accepted = true;
			}
		}
		mDragObject.dragSource.onDropCompleted( (View)dropTarget , mDragObject , false , accepted );
	}
	
	private DropTarget findDropTarget(
			int x ,
			int y ,
			int[] dropCoordinates )
	{
		final Rect r = mRectTemp;
		final ArrayList<DropTarget> dropTargets = mDropTargets;
		final int count = dropTargets.size();
		for( int i = count - 1 ; i >= 0 ; i-- )
		{
			DropTarget target = dropTargets.get( i );
			if( !target.isDropEnabled() || target.getVisibility() != View.VISIBLE )//view不显示，不能作为接受view的target
				continue;
			target.getHitRectRelativeToDragLayer( r );
			mDragObject.x = x;
			mDragObject.y = y;
			if( r.contains( x , y ) )
			{
				dropCoordinates[0] = x;
				dropCoordinates[1] = y;
				mLauncher.getDragLayer().mapCoordInSelfToDescendent( (View)target , dropCoordinates );
				return target;
			}
		}
		return null;
	}
	
	public void setDragScoller(
			DragScroller scroller )
	{
		mDragScroller = scroller;
	}
	
	public void setWindowToken(
			IBinder token )
	{
		mWindowToken = token;
	}
	
	/**
	 * Sets the drag listner which will be notified when a drag starts or ends.
	 */
	public void addDragListener(
			DragListener l )
	{
		mListeners.add( l );
	}
	
	/**
	 * Remove a previously installed drag listener.
	 */
	public void removeDragListener(
			DragListener l )
	{
		mListeners.remove( l );
	}
	
	/**
	 * Add a DropTarget to the list of potential places to receive drop events.
	 */
	public void addDropTarget(
			DropTarget target )
	{
		mDropTargets.add( target );
	}
	
	public void addDropTargetAtIndex(
			DropTarget target ,
			int index )
	{
		if( index >= 0 && index < mDropTargets.size() )
		{
			mDropTargets.add( index , target );
		}
		else
		{
			mDropTargets.add( target );
		}
	}
	
	/**
	 * Don't send drop events to <em>target</em> any more.
	 */
	public void removeDropTarget(
			DropTarget target )
	{
		mDropTargets.remove( target );
	}
	
	/**
	 * Sets the current fling-to-delete drop target.
	 */
	public void setFlingToDeleteDropTarget(
			DropTarget target )
	{
		mFlingToDeleteDropTarget = target;
	}
	
	private void acquireVelocityTrackerAndAddMovement(
			MotionEvent ev )
	{
		if( mVelocityTracker == null )
		{
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement( ev );
	}
	
	private void releaseVelocityTracker()
	{
		if( mVelocityTracker != null )
		{
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}
	
	/**
	 * Set which view scrolls for touch events near the edge of the screen.
	 */
	public void setScrollView(
			View v )
	{
		mScrollView = v;
	}
	
	DragView getDragView()
	{
		return mDragObject.dragView;
	}
	
	private class ScrollRunnable implements Runnable
	{
		
		private int mDirection;
		
		ScrollRunnable()
		{
		}
		
		public void run()
		{
			if( mDragScroller != null )
			{
				if( mDirection == SCROLL_LEFT )
				{
					//cheyingkun start //光感循环切页(德盛伟业)
					//					mDragScroller.scrollLeft();//cheyingkun del
					mDragScroller.scrollLeft( false );//cheyingkun add
					//cheyingkun end
				}
				else
				{
					//cheyingkun start //光感循环切页(德盛伟业)
					//					mDragScroller.scrollRight();//cheyingkun del
					mDragScroller.scrollRight( false );//cheyingkun add
					//cheyingkun end
				}
				mScrollState = SCROLL_OUTSIDE_ZONE;
				mDistanceSinceScroll = 0;
				mDragScroller.onExitScrollArea();
				mLauncher.getDragLayer().onExitScrollArea();
				if( isDragging() )
				{
					// Check the scroll again so that we can requeue the scroller if necessary
					checkScrollState( mLastTouch[0] , mLastTouch[1] );
				}
			}
		}
		
		void setDirection(
				int direction )
		{
			mDirection = direction;
		}
	}
	
	//cheyingkun add start	//解决：拖拽文件夹时不松手,pc端卸载文件夹内图标,拖动的图标和影子没更新的问题（bug:0009717）
	public DragObject getDragObject()
	{
		return mDragObject;
	}
	
	/**
	 * 当卸载应用时,根据传入的app信息,更新拖拽的view
	 * @param appInfos
	 * @param context
	 * @param appsRemovedInDraggingFolder 删除的应用是否在 正在拖拽的文件夹内
	 */
	public void onAppsRemoved(
			ArrayList<AppInfo> appInfos ,
			Context context ,
			boolean appsRemovedInDraggingFolder )
	{
		// Cancel the current drag if we are removing an app that we are dragging
		if( mDragObject != null )
		{
			Object rawDragInfo = mDragObject.dragInfo;
			if( rawDragInfo instanceof ShortcutInfo )
			{
				ShortcutInfo dragInfo = (ShortcutInfo)rawDragInfo;
				for( AppInfo info : appInfos )
				{
					/* SPRD: fix bug 297768 null pointer of dragInfo.intent.getComponent(). @{ */
					// Added null checks to prevent NPE we've seen in the wild
					if( dragInfo != null && dragInfo.getIntent() != null && dragInfo.getIntent().getComponent() != null )
					{
						boolean isSameComponent = dragInfo.getIntent().getComponent().equals( info.getComponentName() );
						if( isSameComponent )
						{
							cancelDrag();
							return;
						}
					}
					/* @} */
				}
			}
			else if( rawDragInfo instanceof FolderInfo )
			{
				FolderInfo mFolderInfo = (FolderInfo)rawDragInfo;
				if( mFolderInfo != null )
				{
					CellInfo dragInfo = mLauncher.getWorkspace().getDragInfo();
					if( dragInfo != null )
					{
						if( appsRemovedInDraggingFolder )//如果删除的app在正在拖拽的文件夹中
						{
							View view = dragInfo.getCell();//拿到拖拽的view
							if( view != null && view instanceof FolderIcon )//如果不为空且是文件夹图标
							{
								//cheyingkun start	//打开usb存储设备,图标灰色,长按灰色图标不松手,拔掉usb线时不再停止拖拽,改为更新dragview
								//								mLauncher.getWorkspace().updateDragViewFolderIcon( (FolderIcon)view );//更新文件夹图标//cheyingkun del
								mLauncher.getWorkspace().updateDragViewAndDragOutline( view );//cheyingkun add
								//cheyingkun end
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 判断删除的app是否在正在拖拽的文件夹内
	 * @param appInfos
	 * @param context
	 * @return 如果删除的app在正在拖拽的文件夹内,返回true,否则返回false
	 */
	public boolean isRemovedAppsInDraggingFolder(
			ArrayList<AppInfo> appInfos ,
			Context context )
	{
		if( mDragObject != null )
		{
			Object rawDragInfo = mDragObject.dragInfo;
			if( rawDragInfo instanceof FolderInfo )//如果是文件夹信息
			{
				FolderInfo mFolderInfo = (FolderInfo)rawDragInfo;
				for( AppInfo info : appInfos )//循环删除的app信息
				{
					if( mFolderInfo != null )
					{
						CellInfo dragInfo = mLauncher.getWorkspace().getDragInfo();//拿到拖拽的cellInfo
						if( dragInfo != null )
						{
							ArrayList<ShortcutInfo> mShortcutInfoList = mFolderInfo.getContents();//拿到文件夹内包含的ShortcutInfo
							for( ShortcutInfo shortcutInfo : mShortcutInfoList )//循环文件夹包含的ShortcutInfo列表
							{
								if( shortcutInfo != null//
										&& shortcutInfo.getIntent() != null//
										&& shortcutInfo.getIntent().getComponent() != null //
										&& shortcutInfo.getIntent().getComponent().equals( info.getComponentName() ) )//如果拖拽的view在文件夹内
								{
									View view = dragInfo.getCell();
									if( view != null && view instanceof FolderIcon )//如果拖拽的view是文件夹,返回true
									{
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	//cheyingkun add end
}
