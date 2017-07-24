package com.cooee.phenix;


import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cooee.framework.stackblur.BlurHelper;
import com.cooee.framework.stackblur.BlurHelper.BlurCallbacks;
import com.cooee.framework.stackblur.BlurOptions;
import com.cooee.framework.stackblur.FuzzyBackGround;
import com.cooee.framework.stackblur.FuzzyBackGroundCallBack;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.AppList.Marshmallow.AllAppsTransitionController;
import com.cooee.phenix.Folder.Folder;
import com.cooee.phenix.Folder.FolderIcon;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.util.TouchController;


/**
 * A ViewGroup that coordinates dragging across its descendants
 */
public class DragLayer extends FrameLayout implements ViewGroup.OnHierarchyChangeListener , FuzzyBackGround , BlurCallbacks
{
	
	// huwenhao@2015/05/20 ADD START 
	// debug相关
	private long frameStart = 0;
	private int frames = 0;
	private int fps;
	private Paint debugPaint = null;
	// huwenhao@2015/05/20 ADD END
	private DragController mDragController;
	private int[] mTmpXY = new int[2];
	private int mXDown , mYDown;
	private Launcher mLauncher;
	// Variables relating to resizing widgets
	private final ArrayList<AppWidgetResizeFrame> mResizeFrames = new ArrayList<AppWidgetResizeFrame>();
	private AppWidgetResizeFrame mCurrentResizeFrame;
	// Variables relating to animation of views after drop
	private ValueAnimator mDropAnim = null;
	private ValueAnimator mFadeOutAnim = null;
	private TimeInterpolator mCubicEaseOutInterpolator = new DecelerateInterpolator( 1.5f );
	private DragView mDropView = null;
	private int mAnchorViewInitialScrollX = 0;
	private View mAnchorView = null;
	private Rect mHitRect = new Rect();
	private int mWorkspaceIndex = -1;
	private int mSearchDropTargetBarIndex = -1;
	public static final int ANIMATION_END_DISAPPEAR = 0;
	public static final int ANIMATION_END_FADE_OUT = 1;
	public static final int ANIMATION_END_REMAIN_VISIBLE = 2;
	private TouchCompleteListener mTouchCompleteListener;
	private final Rect mInsets = new Rect();
	private Rect hitRect = new Rect();
	private Bitmap mFuzzyBackGround = null;
	private FuzzyBackGroundCallBack mFuzzyBackGroundCallBack = null;
	private BlurOptions mBlurOptions = null;
	Rect mChildRectForDispatchDraw = null;
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	private float mBackgroundAlpha = 0;
	private AllAppsTransitionController mAllAppsController;
	private TouchController mActiveController;
	//zhujieping add end
	/**
	 * Used to create a new DragLayer from XML.
	 *
	 * @param context The application's context.
	 * @param attrs The attributes set containing the Workspace's customization values.
	 */
	public DragLayer(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		// Disable multitouch across the workspace/all apps/customize tray
		setMotionEventSplittingEnabled( false );
		setChildrenDrawingOrderEnabled( true );
		setOnHierarchyChangeListener( this );
		Resources mResources = getResources();
		mLeftHoverDrawable = mResources.getDrawable( R.drawable.page_hover_left_holo );
		mRightHoverDrawable = mResources.getDrawable( R.drawable.page_hover_right_holo );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG_FPS )
		{
			frameStart = System.nanoTime();
		}
	}
	
	public void setup(
			Launcher launcher ,
			DragController controller )
	{
		mLauncher = launcher;
		mDragController = controller;
	}
	
	@Override
	public boolean dispatchKeyEvent(
			KeyEvent event )
	{
		return mDragController.dispatchKeyEvent( event ) || super.dispatchKeyEvent( event );
	}
	
	@Override
	protected boolean fitSystemWindows(
			Rect insets )
	{
		final int n = getChildCount();
		for( int i = 0 ; i < n ; i++ )
		{
			final View child = getChildAt( i );
			final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)child.getLayoutParams();
			if( child instanceof Insettable )
			{
				( (Insettable)child ).setInsets( insets );
			}
			else
			{
				flp.topMargin += ( insets.top - mInsets.top );
				flp.leftMargin += ( insets.left - mInsets.left );
				flp.rightMargin += ( insets.right - mInsets.right );
				flp.bottomMargin += ( insets.bottom - mInsets.bottom );
			}
			child.setLayoutParams( flp );
		}
		mInsets.set( insets );
		return true; // I'll take it from here
	}
	
	private boolean isEventOverFolderTextRegion(
			Folder folder ,
			MotionEvent ev )
	{
		getDescendantRectRelativeToSelf( folder.getEditTextRegion() , mHitRect );
		if( mHitRect.contains( (int)ev.getX() , (int)ev.getY() ) )
		{
			return true;
		}
		return false;
	}
	
	private boolean isEventOverFolder(
			Folder folder ,
			MotionEvent ev )
	{
		// zhujieping@2015/06/17 UPD START
		//		getDescendantRectRelativeToSelf( folder , mHitRect );
		folder.getDescendantRectRelativeToSelf( mHitRect );
		// zhujieping@2015/06/17 UPD END
		if( mHitRect.contains( (int)ev.getX() , (int)ev.getY() ) )
		{
			return true;
		}
		//cheyingkun add start	//文件夹推荐应用
		folder.getDescendantRectRelativeToSelfMore( mHitRect );
		if( mHitRect.contains( (int)ev.getX() , (int)ev.getY() ) )
		{
			return true;
		}
		//cheyingkun add end	//文件夹推荐应用
		return false;
	}
	
	private boolean handleTouchDown(
			MotionEvent ev ,
			boolean intercept )
	{
		hitRect.set( 0 , 0 , 0 , 0 );
		int x = (int)ev.getX();
		int y = (int)ev.getY();
		for( AppWidgetResizeFrame child : mResizeFrames )
		{
			child.getHitRect( hitRect );
			if( hitRect.contains( x , y ) )
			{
				if( child.beginResizeIfPointInRegion( x - child.getLeft() , y - child.getTop() ) )
				{
					mCurrentResizeFrame = child;
					mXDown = x;
					mYDown = y;
					requestDisallowInterceptTouchEvent( true );
					return true;
				}
			}
		}
		Folder currentFolder = mLauncher.getWorkspace().getOpenFolder();
		if( currentFolder != null && intercept )
		{
			if( !mLauncher.isFolderClingVisible() )
			{
				if( currentFolder.isEditingName() )
				{
					if( !isEventOverFolderTextRegion( currentFolder , ev ) )
					{
						currentFolder.dismissEditingName();
						return true;
					}
				}
				getDescendantRectRelativeToSelf( currentFolder , hitRect );
				if( !isEventOverFolder( currentFolder , ev ) )
				{
					mLauncher.closeFolder();
					return true;
				}
			}
			//xiatian add start	//需求：在文件夹引导界面，除了响应引导界面的确认按钮之外，不响应其他事件（1、文件夹修改名字；2、文件夹图标长按；3、打开文件夹内应用）
			else
			{
				getDescendantRectRelativeToSelf( currentFolder , hitRect );
				if( isEventOverFolder( currentFolder , ev ) )
				{
					currentFolder.playSoundEffect( SoundEffectConstants.CLICK );//点击声音
					//				currentFolder.performHapticFeedback( HapticFeedbackConstants.VIRTUAL_KEY );//点击震动
					return true;
				}
			}
			//xiatian add end
		}
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(
			MotionEvent ev )
	{
		int action = ev.getAction();
		if( action == MotionEvent.ACTION_DOWN )
		{
			//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
			if( mAllAppsController != null )
				mAllAppsController.cancelDiscoveryAnimation();
			//zhujieping add end
			if( handleTouchDown( ev , true ) )
			{
				return true;
			}
		}
		//xiatian add start	//fix bug：解决“文件夹打开后，点击图标后，快速滑动至文件夹区域之外，这时图标在文件夹外部区域，相应长按事件”的问题。【i_0010683】
		else if( action == MotionEvent.ACTION_MOVE )
		{
			if( handleTouchMove( ev , true ) )
			{
				return true;
			}
		}
		//xiatian add end
		else if( action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL )
		{
			if( mTouchCompleteListener != null )
			{
				mTouchCompleteListener.onTouchComplete();
			}
			mTouchCompleteListener = null;
		}
		clearAllResizeFrames();
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		mActiveController = null;
		if( mDragController.onInterceptTouchEvent( ev ) )
		{
			mActiveController = mDragController;
			return true;
		}
		if( mAllAppsController != null && mAllAppsController.onInterceptTouchEvent( ev ) )
		{
			mActiveController = mAllAppsController;
			return true;
		}
		//zhujieping add end
		return false;
	}
	
	@Override
	public boolean onInterceptHoverEvent(
			MotionEvent ev )
	{
		return false;
	}
	
	@Override
	public boolean onRequestSendAccessibilityEvent(
			View child ,
			AccessibilityEvent event )
	{
		Folder currentFolder = mLauncher.getWorkspace().getOpenFolder();
		if( currentFolder != null )
		{
			if( child == currentFolder )
			{
				return super.onRequestSendAccessibilityEvent( child , event );
			}
			// Skip propagating onRequestSendAccessibilityEvent all for other children
			// when a folder is open
			return false;
		}
		return super.onRequestSendAccessibilityEvent( child , event );
	}
	
	@Override
	public void addChildrenForAccessibility(
			ArrayList<View> childrenForAccessibility )
	{
		Folder currentFolder = mLauncher.getWorkspace().getOpenFolder();
		if( currentFolder != null )
		{
			// Only add the folder as a child for accessibility when it is open
			childrenForAccessibility.add( currentFolder );
		}
		else
		{
			super.addChildrenForAccessibility( childrenForAccessibility );
		}
	}
	
	@Override
	public boolean onHoverEvent(
			MotionEvent ev )
	{
		// If we've received this, we've already done the necessary handling
		// in onInterceptHoverEvent. Return true to consume the event.
		return false;
	}
	
	@Override
	public boolean onTouchEvent(
			MotionEvent ev )
	{
		boolean handled = false;
		int action = ev.getAction();
		int x = (int)ev.getX();
		int y = (int)ev.getY();
		if( action == MotionEvent.ACTION_DOWN )
		{
			if( handleTouchDown( ev , false ) )
			{
				return true;
			}
		}
		else if( action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL )
		{
			if( mTouchCompleteListener != null )
			{
				mTouchCompleteListener.onTouchComplete();
			}
			mTouchCompleteListener = null;
		}
		if( mCurrentResizeFrame != null )
		{
			handled = true;
			switch( action )
			{
				case MotionEvent.ACTION_MOVE:
					mCurrentResizeFrame.visualizeResizeForDelta( x - mXDown , y - mYDown );
					break;
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					mCurrentResizeFrame.visualizeResizeForDelta( x - mXDown , y - mYDown );
					mCurrentResizeFrame.onTouchUp();
					mCurrentResizeFrame = null;
			}
		}
		if( handled )
			return true;
		//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
		if( mActiveController != null )
		{
			return mActiveController.onTouchEvent( ev );
		}
		//zhujieping add end
		return false;
	}
	
	/**
	 * Determine the rect of the descendant in this DragLayer's coordinates
	 *
	 * @param descendant The descendant whose coordinates we want to find.
	 * @param r The rect into which to place the results.
	 * @return The factor by which this descendant is scaled relative to this DragLayer.
	 */
	public float getDescendantRectRelativeToSelf(
			View descendant ,
			Rect r )
	{
		mTmpXY[0] = 0;
		mTmpXY[1] = 0;
		float scale = getDescendantCoordRelativeToSelf( descendant , mTmpXY );
		r.set( mTmpXY[0] , mTmpXY[1] , (int)( mTmpXY[0] + scale * descendant.getMeasuredWidth() ) , (int)( mTmpXY[1] + scale * descendant.getMeasuredHeight() ) );
		return scale;
	}
	
	public float getLocationInDragLayer(
			View child ,
			int[] loc )
	{
		loc[0] = 0;
		loc[1] = 0;
		return getDescendantCoordRelativeToSelf( child , loc );
	}
	
	public float getDescendantCoordRelativeToSelf(
			View descendant ,
			int[] coord )
	{
		return getDescendantCoordRelativeToSelf( descendant , coord , false );
	}
	
	/**
	 * Given a coordinate relative to the descendant, find the coordinate in this DragLayer's
	 * coordinates.
	 *
	 * @param descendant The descendant to which the passed coordinate is relative.
	 * @param coord The coordinate that we want mapped.
	 * @param includeRootScroll Whether or not to account for the scroll of the root descendant:
	 *          sometimes this is relevant as in a child's coordinates within the root descendant.
	 * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
	 *         this scale factor is assumed to be equal in X and Y, and so if at any point this
	 *         assumption fails, we will need to return a pair of scale factors.
	 */
	public float getDescendantCoordRelativeToSelf(
			View descendant ,
			int[] coord ,
			boolean includeRootScroll )
	{
		return Utilities.getDescendantCoordRelativeToParent( descendant , this , coord , includeRootScroll );
	}
	
	/**
	 * Inverse of {@link #getDescendantCoordRelativeToSelf(View, int[])}.
	 */
	public float mapCoordInSelfToDescendent(
			View descendant ,
			int[] coord )
	{
		return Utilities.mapCoordInSelfToDescendent( descendant , this , coord );
	}
	
	public void getViewRectRelativeToSelf(
			View v ,
			Rect r )
	{
		int[] loc = new int[2];
		getLocationInWindow( loc );
		int x = loc[0];
		int y = loc[1];
		v.getLocationInWindow( loc );
		int vX = loc[0];
		int vY = loc[1];
		int left = vX - x;
		int top = vY - y;
		r.set( left , top , left + v.getMeasuredWidth() , top + v.getMeasuredHeight() );
	}
	
	@Override
	public boolean dispatchUnhandledMove(
			View focused ,
			int direction )
	{
		//dispatchUnhandledMove
		//xiatian add start	//解决“关闭开关switch_enable_response_onkeylistener的前提下，adb模拟KEYCODE_DPAD_LEFT和KEYCODE_DPAD_RIGHT时，会切页”的问题。
		//【问题原因】
		//	1、adb模拟KEYCODE_DPAD_LEFT和KEYCODE_DPAD_RIGHT时，底层会调用DragLayer.java的dispatchUnhandledMove方法
		//	2、进而调用PagedView.java的dispatchUnhandledMove方法：判断条件成立时，导致桌面切页
		//【解决方案】不支持按键机时，不处理底层调用“dispatchUnhandledMove”方法的相关逻辑。
		if( LauncherDefaultConfig.SWITCH_ENABLE_RESPONSE_ONKEYLISTENER == false )
		{
			return true;
		}
		//xiatian add end
		return mDragController.dispatchUnhandledMove( focused , direction );
	}
	
	public static class LayoutParams extends FrameLayout.LayoutParams
	{
		
		public int x , y;
		public boolean customPosition = false;
		
		/**
		 * {@inheritDoc}
		 */
		public LayoutParams(
				int width ,
				int height )
		{
			super( width , height );
		}
		
		public void setWidth(
				int width )
		{
			this.width = width;
		}
		
		public int getWidth()
		{
			return width;
		}
		
		public void setHeight(
				int height )
		{
			this.height = height;
		}
		
		public int getHeight()
		{
			return height;
		}
		
		public void setX(
				int x )
		{
			this.x = x;
		}
		
		public int getX()
		{
			return x;
		}
		
		public void setY(
				int y )
		{
			this.y = y;
		}
		
		public int getY()
		{
			return y;
		}
	}
	
	protected void onLayout(
			boolean changed ,
			int l ,
			int t ,
			int r ,
			int b )
	{
		super.onLayout( changed , l , t , r , b );
		int count = getChildCount();
		for( int i = 0 ; i < count ; i++ )
		{
			View child = getChildAt( i );
			final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)child.getLayoutParams();
			if( flp instanceof LayoutParams )
			{
				final LayoutParams lp = (LayoutParams)flp;
				if( lp.customPosition )
				{
					child.layout( lp.x , lp.y , lp.x + lp.width , lp.y + lp.height );
				}
			}
		}
	}
	
	public void clearAllResizeFrames()
	{
		if( mResizeFrames.size() > 0 )
		{
			for( AppWidgetResizeFrame frame : mResizeFrames )
			{
				frame.commitResize();
				removeView( frame );
			}
			mResizeFrames.clear();
		}
	}
	
	public boolean hasResizeFrames()
	{
		return mResizeFrames.size() > 0;
	}
	
	public boolean isWidgetBeingResized()
	{
		return mCurrentResizeFrame != null;
	}
	
	public void addResizeFrame(
			ItemInfo itemInfo ,
			LauncherAppWidgetHostView widget ,
			CellLayout cellLayout )
	{
		AppWidgetResizeFrame resizeFrame = new AppWidgetResizeFrame( getContext() , widget , cellLayout , this );
		LayoutParams lp = new LayoutParams( -1 , -1 );
		lp.customPosition = true;
		addView( resizeFrame , lp );
		mResizeFrames.add( resizeFrame );
		resizeFrame.snapToWidget( false );
	}
	
	public void animateViewIntoPosition(
			DragView dragView ,
			final View child )
	{
		animateViewIntoPosition( dragView , child , null );
	}
	
	public void animateViewIntoPosition(
			DragView dragView ,
			final int[] pos ,
			float alpha ,
			float scaleX ,
			float scaleY ,
			int animationEndStyle ,
			Runnable onFinishRunnable ,
			int duration )
	{
		Rect r = new Rect();
		getViewRectRelativeToSelf( dragView , r );
		final int fromX = r.left;
		final int fromY = r.top;
		animateViewIntoPosition( dragView , fromX , fromY , pos[0] , pos[1] , alpha , 1 , 1 , scaleX , scaleY , onFinishRunnable , animationEndStyle , duration , null );
	}
	
	public void animateViewIntoPosition(
			DragView dragView ,
			final View child ,
			final Runnable onFinishAnimationRunnable )
	{
		animateViewIntoPosition( dragView , child , -1 , onFinishAnimationRunnable , null );
	}
	
	public void animateViewIntoPosition(
			DragView dragView ,
			final View child ,
			int duration ,
			final Runnable onFinishAnimationRunnable ,
			View anchorView )
	{
		ShortcutAndWidgetContainer parentChildren = (ShortcutAndWidgetContainer)child.getParent();
		CellLayout.LayoutParams lp = (CellLayout.LayoutParams)child.getLayoutParams();
		parentChildren.measureChild( child );
		Rect r = new Rect();
		getViewRectRelativeToSelf( dragView , r );
		int coord[] = new int[2];
		float childScale = child.getScaleX();
		coord[0] = lp.x + (int)( child.getMeasuredWidth() * ( 1 - childScale ) / 2 );
		coord[1] = lp.y + (int)( child.getMeasuredHeight() * ( 1 - childScale ) / 2 );
		// Since the child hasn't necessarily been laid out, we force the lp to be updated with
		// the correct coordinates (above) and use these to determine the final location
		float scale = getDescendantCoordRelativeToSelf( (View)child.getParent() , coord );
		// We need to account for the scale of the child itself, as the above only accounts for
		// for the scale in parents.
		scale *= childScale;
		int toX = coord[0];
		int toY = coord[1];
		if( child instanceof TextView )
		{
			TextView tv = (TextView)child;
			// The child may be scaled (always about the center of the view) so to account for it,
			// we have to offset the position by the scaled size.  Once we do that, we can center
			// the drag view about the scaled child view.
			toY += Math.round( scale * tv.getPaddingTop() );//xiatian add note	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。//BubbleTextView重载方法“getPaddingTop”
			toY -= dragView.getMeasuredHeight() * ( 1 - scale ) / 2;
			toX -= ( dragView.getMeasuredWidth() - Math.round( scale * child.getMeasuredWidth() ) ) / 2;
		}
		else if( child instanceof FolderIcon )
		{
			// Account for holographic blur padding on the drag view
			toY += Math.round( scale * ( child.getPaddingTop() - dragView.getDragRegionTop() ) );
			toY -= scale * Workspace.DRAG_BITMAP_PADDING / 2;
			toY -= ( 1 - scale ) * dragView.getMeasuredHeight() / 2;
			// Center in the x coordinate about the target's drawable
			toX -= ( dragView.getMeasuredWidth() - Math.round( scale * child.getMeasuredWidth() ) ) / 2;
		}
		else
		{
			toY -= ( Math.round( scale * ( dragView.getHeight() - child.getMeasuredHeight() ) ) ) / 2;
			toX -= ( Math.round( scale * ( dragView.getMeasuredWidth() - child.getMeasuredWidth() ) ) ) / 2;
		}
		final int fromX = r.left;
		final int fromY = r.top;
		child.setVisibility( INVISIBLE );
		Runnable onCompleteRunnable = new Runnable() {
			
			public void run()
			{
				child.setVisibility( VISIBLE );
				if( onFinishAnimationRunnable != null )
				{
					onFinishAnimationRunnable.run();
				}
			}
		};
		animateViewIntoPosition( dragView , fromX , fromY , toX , toY , 1 , 1 , 1 , scale , scale , onCompleteRunnable , ANIMATION_END_DISAPPEAR , duration , anchorView );
	}
	
	public void animateViewIntoPosition(
			final DragView view ,
			final int fromX ,
			final int fromY ,
			final int toX ,
			final int toY ,
			float finalAlpha ,
			float initScaleX ,
			float initScaleY ,
			float finalScaleX ,
			float finalScaleY ,
			Runnable onCompleteRunnable ,
			int animationEndStyle ,
			int duration ,
			View anchorView )
	{
		Rect from = new Rect( fromX , fromY , fromX + view.getMeasuredWidth() , fromY + view.getMeasuredHeight() );
		Rect to = new Rect( toX , toY , toX + view.getMeasuredWidth() , toY + view.getMeasuredHeight() );
		animateView( view , from , to , finalAlpha , initScaleX , initScaleY , finalScaleX , finalScaleY , duration , null , null , onCompleteRunnable , animationEndStyle , anchorView );
	}
	
	/**
	 * This method animates a view at the end of a drag and drop animation.
	 *
	 * @param view The view to be animated. This view is drawn directly into DragLayer, and so
	 *        doesn't need to be a child of DragLayer.
	 * @param from The initial location of the view. Only the left and top parameters are used.
	 * @param to The final location of the view. Only the left and top parameters are used. This
	 *        location doesn't account for scaling, and so should be centered about the desired
	 *        final location (including scaling).
	 * @param finalAlpha The final alpha of the view, in case we want it to fade as it animates.
	 * @param finalScale The final scale of the view. The view is scaled about its center.
	 * @param duration The duration of the animation.
	 * @param motionInterpolator The interpolator to use for the location of the view.
	 * @param alphaInterpolator The interpolator to use for the alpha of the view.
	 * @param onCompleteRunnable Optional runnable to run on animation completion.
	 * @param fadeOut Whether or not to fade out the view once the animation completes. If true,
	 *        the runnable will execute after the view is faded out.
	 * @param anchorView If not null, this represents the view which the animated view stays
	 *        anchored to in case scrolling is currently taking place. Note: currently this is
	 *        only used for the X dimension for the case of the workspace.
	 */
	public void animateView(
			final DragView view ,
			final Rect from ,
			final Rect to ,
			final float finalAlpha ,
			final float initScaleX ,
			final float initScaleY ,
			final float finalScaleX ,
			final float finalScaleY ,
			int duration ,
			final Interpolator motionInterpolator ,
			final Interpolator alphaInterpolator ,
			final Runnable onCompleteRunnable ,
			final int animationEndStyle ,
			View anchorView )
	{
		// Calculate the duration of the animation based on the object's distance
		final float dist = (float)Math.sqrt( Math.pow( to.left - from.left , 2 ) + Math.pow( to.top - from.top , 2 ) );
		final Resources res = getResources();
		final float maxDist = (float)LauncherDefaultConfig.getInt( R.integer.config_dropAnimMaxDist );
		// If duration < 0, this is a cue to compute the duration based on the distance
		if( duration < 0 )
		{
			duration = LauncherDefaultConfig.getInt( R.integer.config_dropAnimMaxDuration );
			if( dist < maxDist )
			{
				duration *= mCubicEaseOutInterpolator.getInterpolation( dist / maxDist );
			}
			duration = Math.max( duration , LauncherDefaultConfig.getInt( R.integer.config_dropAnimMinDuration ) );
		}
		// Fall back to cubic ease out interpolator for the animation if none is specified
		TimeInterpolator interpolator = null;
		if( alphaInterpolator == null || motionInterpolator == null )
		{
			interpolator = mCubicEaseOutInterpolator;
		}
		// Animate the view
		final float initAlpha = view.getAlpha();
		final float dropViewScale = view.getScaleX();
		AnimatorUpdateListener updateCb = new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(
					ValueAnimator animation )
			{
				final float percent = (Float)animation.getAnimatedValue();
				final int width = view.getMeasuredWidth();
				final int height = view.getMeasuredHeight();
				float alphaPercent = alphaInterpolator == null ? percent : alphaInterpolator.getInterpolation( percent );
				float motionPercent = motionInterpolator == null ? percent : motionInterpolator.getInterpolation( percent );
				float initialScaleX = initScaleX * dropViewScale;
				float initialScaleY = initScaleY * dropViewScale;
				float scaleX = finalScaleX * percent + initialScaleX * ( 1 - percent );
				float scaleY = finalScaleY * percent + initialScaleY * ( 1 - percent );
				float alpha = finalAlpha * alphaPercent + initAlpha * ( 1 - alphaPercent );
				float fromLeft = from.left + ( initialScaleX - 1f ) * width / 2;
				float fromTop = from.top + ( initialScaleY - 1f ) * height / 2;
				int x = (int)( fromLeft + Math.round( ( ( to.left - fromLeft ) * motionPercent ) ) );
				int y = (int)( fromTop + Math.round( ( ( to.top - fromTop ) * motionPercent ) ) );
				int xPos = x - mDropView.getScrollX() + ( mAnchorView != null ? ( mAnchorViewInitialScrollX - mAnchorView.getScrollX() ) : 0 );
				int yPos = y - mDropView.getScrollY();
				mDropView.setTranslationX( xPos );
				mDropView.setTranslationY( yPos );
				mDropView.setScaleX( scaleX );
				mDropView.setScaleY( scaleY );
				mDropView.setAlpha( alpha );
			}
		};
		animateView( view , updateCb , duration , interpolator , onCompleteRunnable , animationEndStyle , anchorView );
	}
	
	public void animateView(
			final DragView view ,
			AnimatorUpdateListener updateCb ,
			int duration ,
			TimeInterpolator interpolator ,
			final Runnable onCompleteRunnable ,
			final int animationEndStyle ,
			View anchorView )
	{
		// Clean up the previous animations
		if( mDropAnim != null )
			mDropAnim.cancel();
		if( mFadeOutAnim != null )
			mFadeOutAnim.cancel();
		// Show the drop view if it was previously hidden
		mDropView = view;
		mDropView.cancelAnimation();
		mDropView.resetLayoutParams();
		// Set the anchor view if the page is scrolling
		if( anchorView != null )
		{
			mAnchorViewInitialScrollX = anchorView.getScrollX();
		}
		mAnchorView = anchorView;
		// Create and start the animation
		mDropAnim = new ValueAnimator();
		mDropAnim.setInterpolator( interpolator );
		mDropAnim.setDuration( duration );
		mDropAnim.setFloatValues( 0f , 1f );
		mDropAnim.addUpdateListener( updateCb );
		mDropAnim.addListener( new AnimatorListenerAdapter() {
			
			public void onAnimationEnd(
					Animator animation )
			{
				if( onCompleteRunnable != null )
				{
					onCompleteRunnable.run();
				}
				switch( animationEndStyle )
				{
					case ANIMATION_END_DISAPPEAR:
						clearAnimatedView();
						break;
					case ANIMATION_END_FADE_OUT:
						fadeOutDragView();
						break;
					case ANIMATION_END_REMAIN_VISIBLE:
						break;
				}
			}
		} );
		mDropAnim.start();
	}
	
	public void clearAnimatedView()
	{
		if( mDropAnim != null )
		{
			mDropAnim.cancel();
		}
		// zhujieping@2015/04/27 ADD START
		//Clean up the previous animations
		if( mFadeOutAnim != null )
		{
			mFadeOutAnim.cancel();
		}
		// zhujieping@2015/04/27 ADD END
		if( mDropView != null )
		{
			mDragController.onDeferredEndDrag( mDropView );
		}
		mDropView = null;
		invalidate();
	}
	
	public View getAnimatedView()
	{
		return mDropView;
	}
	
	private void fadeOutDragView()
	{
		mFadeOutAnim = new ValueAnimator();
		mFadeOutAnim.setDuration( 150 );
		mFadeOutAnim.setFloatValues( 0f , 1f );
		mFadeOutAnim.removeAllUpdateListeners();
		mFadeOutAnim.addUpdateListener( new AnimatorUpdateListener() {
			
			public void onAnimationUpdate(
					ValueAnimator animation )
			{
				if( mDropView != null )//添加保护
				{
					final float percent = (Float)animation.getAnimatedValue();
					float alpha = 1 - percent;
					mDropView.setAlpha( alpha );
				}
			}
		} );
		mFadeOutAnim.addListener( new AnimatorListenerAdapter() {
			
			public void onAnimationEnd(
					Animator animation )
			{
				if( mDropView != null )
				{
					mDragController.onDeferredEndDrag( mDropView );
				}
				mDropView = null;
				invalidate();
			}
		} );
		mFadeOutAnim.start();
	}
	
	@Override
	public void onChildViewAdded(
			View parent ,
			View child )
	{
		updateChildIndices();
	}
	
	@Override
	public void onChildViewRemoved(
			View parent ,
			View child )
	{
		updateChildIndices();
	}
	
	private void updateChildIndices()
	{
		if( mLauncher != null )
		{
			mWorkspaceIndex = indexOfChild( mLauncher.getWorkspace() );
			mSearchDropTargetBarIndex = indexOfChild( mLauncher.getSearchDropTargetBar() );
		}
	}
	
	@Override
	protected int getChildDrawingOrder(
			int childCount ,
			int i )
	{
		// TODO: We have turned off this custom drawing order because it now effects touch
		// dispatch order. We need to sort that issue out and then decide how to go about this.
		if( true || mWorkspaceIndex == -1 || mSearchDropTargetBarIndex == -1 || mLauncher.getWorkspace().isDrawingBackgroundGradient() )
		{
			return i;
		}
		// This ensures that the workspace is drawn above the hotseat and SearchDropTargetBar,
		// except when the workspace is drawing a background gradient, in which
		// case we want the workspace to stay behind these elements.
		if( i == mSearchDropTargetBarIndex )
		{
			return mWorkspaceIndex;
		}
		else if( i == mWorkspaceIndex )
		{
			return mSearchDropTargetBarIndex;
		}
		else
		{
			return i;
		}
	}
	
	private boolean mInScrollArea;
	private Drawable mLeftHoverDrawable;
	private Drawable mRightHoverDrawable;
	
	void onEnterScrollArea(
			int direction )
	{
		mInScrollArea = true;
		invalidate();
	}
	
	void onExitScrollArea()
	{
		mInScrollArea = false;
		invalidate();
	}
	
	/**
	 * Note: this is a reimplementation of View.isLayoutRtl() since that is currently hidden api.
	 */
	private boolean isLayoutRtl()
	{
		// gaominghui@2016/12/14 ADD START
		//return( getLayoutDirection() == LAYOUT_DIRECTION_RTL );
		//xiatian start	//整理判断“是否从左往右布局”的方法：由“mView.getLayoutDirection()”改为“getResources().getConfiguration().getLayoutDirection()”
		//		return Tools.isLayoutRTL( this );//xiatian del
		return LauncherAppState.isLayoutRTL();//xiatian add 
		//xiatian end
		// gaominghui@2016/12/14 ADD END
	}
	
	@Override
	protected void dispatchDraw(
			Canvas canvas )
	{
		long time = 0;
		// zhangjin@2015/09/09 ADD START
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG_FPS )
		{
			time = System.nanoTime();
		}
		// zhangjin@2015/09/09 ADD END
		super.dispatchDraw( canvas );
		if( mInScrollArea )
		{
			Workspace workspace = mLauncher.getWorkspace();
			int width = getMeasuredWidth();
			if( mChildRectForDispatchDraw == null )
			{
				mChildRectForDispatchDraw = new Rect();
			}
			else
			{
				mChildRectForDispatchDraw.set( 0 , 0 , 0 , 0 );
			}
			getDescendantRectRelativeToSelf( workspace.getChildAt( 0 ) , mChildRectForDispatchDraw );
			int page = workspace.getNextPage();
			final boolean isRtl = isLayoutRtl();
			CellLayout leftPage = (CellLayout)workspace.getChildAt( isRtl ? page + 1 : page - 1 );
			CellLayout rightPage = (CellLayout)workspace.getChildAt( isRtl ? page - 1 : page + 1 );
			if( leftPage != null && leftPage.getIsDragOverlapping() )
			{
				mLeftHoverDrawable.setBounds( 0 , mChildRectForDispatchDraw.top , mLeftHoverDrawable.getIntrinsicWidth() , mChildRectForDispatchDraw.bottom );
				mLeftHoverDrawable.draw( canvas );
			}
			else if( rightPage != null && rightPage.getIsDragOverlapping() )
			{
				mRightHoverDrawable.setBounds( width - mRightHoverDrawable.getIntrinsicWidth() , mChildRectForDispatchDraw.top , width , mChildRectForDispatchDraw.bottom );
				mRightHoverDrawable.draw( canvas );
			}
		}
		// huwenhao@2015/05/20 ADD START
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG_FPS )
		{
			if( time - frameStart > 1000000000 )
			{
				fps = frames;
				frames = 0;
				frameStart = time;
			}
			frames++;
			if( debugPaint == null )
			{
				debugPaint = new Paint();
				debugPaint.setColor( Color.RED );
				debugPaint.setTextSize( 30 );
			}
			canvas.drawText( StringUtils.concat( "fps:" , fps ) , 20 , 50 , debugPaint );
			//invalidate();
		}
		// huwenhao@2015/05/20 ADD END
	}
	
	public void setTouchCompleteListener(
			TouchCompleteListener listener )
	{
		mTouchCompleteListener = listener;
	}
	
	public interface TouchCompleteListener
	{
		
		public void onTouchComplete();
	}
	
	//xiatian add start	//fix bug：解决“文件夹打开后，点击图标后，快速滑动至文件夹区域之外，这时图标在文件夹外部区域，相应长按事件”的问题。【i_0010683】
	private boolean handleTouchMove(
			MotionEvent ev ,
			boolean intercept )
	{
		Folder currentFolder = mLauncher.getWorkspace().getOpenFolder();
		if( currentFolder != null )
		{
			hitRect.set( 0 , 0 , 0 , 0 );
			getDescendantRectRelativeToSelf( currentFolder , hitRect );
			if( !isEventOverFolder( currentFolder , ev ) )
			{
				//【问题原因】图标被按下时，延时启动了一个Runnable，Runnable到时之前没被取消的话，就会触发长按消息。
				//【解决方案】手指划出文件夹区域后，取消该Runnable
				CellLayout mCellLayout = currentFolder.getContent();
				if( mCellLayout != null )
				{
					mCellLayout.cancelLongPress();
				}
				if( !mLauncher.isFolderClingVisible() && intercept )
				{
					return true;
				}
			}
		}
		return false;
	}
	
	//xiatian add end
	//获取模糊背景
	@Override
	public void getFuzzyBackGround(
			FuzzyBackGroundCallBack fuzzyBackGroundCallBack )
	{
		mFuzzyBackGroundCallBack = fuzzyBackGroundCallBack;
		if( mBlurOptions == null )
			mBlurOptions = new BlurOptions();
		mBlurOptions.captureWallPaper = true;
		mBlurOptions.radius = 15;
		mBlurOptions.callbacks = this;
		BlurHelper.blurViewNonUiTread( getContext() , this , mBlurOptions );
	}
	
	@Override
	public void blurCompleted(
			Bitmap bluredBitmap )
	{
		if( mFuzzyBackGround != null && !mFuzzyBackGround.isRecycled() )
		{
			mFuzzyBackGround.recycle();
			mFuzzyBackGround = null;
		}
		mFuzzyBackGround = bluredBitmap;
		mFuzzyBackGroundCallBack.setFuzzBackGround( mFuzzyBackGround );
	}
	
	//获取模糊壁纸
	@Override
	public void getFuzzyWallpaper(
			FuzzyBackGroundCallBack fuzzyBackGroundCallBack )
	{
		mFuzzyBackGroundCallBack = fuzzyBackGroundCallBack;
		if( mBlurOptions == null )
			mBlurOptions = new BlurOptions();
		mBlurOptions.captureWallPaper = true;
		mBlurOptions.radius = 15;
		mBlurOptions.callbacks = this;
		BlurHelper.blurWallpaperNonUiTread( getContext() , mBlurOptions );
	};
	
	@Override
	public void addView(
			View child )
	{
		// TODO Auto-generated method stub
		super.addView( child );
	}
	
	@Override
	public void addView(
			View child ,
			int index ,
			android.view.ViewGroup.LayoutParams params )
	{
		// TODO Auto-generated method stub
		super.addView( child , index , params );
	}
	
	@Override
	public void addView(
			View child ,
			android.view.ViewGroup.LayoutParams params )
	{
		// TODO Auto-generated method stub
		super.addView( child , params );
	}
	
	@Override
	public void addView(
			View child ,
			int index )
	{
		// TODO Auto-generated method stub
		super.addView( child , index );
	}
	
	@Override
	public void addView(
			View child ,
			int width ,
			int height )
	{
		// TODO Auto-generated method stub
		super.addView( child , width , height );
	}
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	public boolean isEventOverView(
			View view ,
			MotionEvent ev )
	{
		getDescendantRectRelativeToSelf( view , mHitRect );
		return mHitRect.contains( (int)ev.getX() , (int)ev.getY() );
	}
	
	public boolean isEventOverPageIndicator(
			MotionEvent ev )
	{
		getDescendantRectRelativeToSelf( mLauncher.getWorkspace().getPageIndicator() , mHitRect );
		return mHitRect.contains( (int)ev.getX() , (int)ev.getY() );
	}
	
	public boolean isEventOverHotseat(
			MotionEvent ev )
	{
		return isEventOverView( mLauncher.getHotseat() , ev );
	}
	
	public Rect getInsets()
	{
		return mInsets;
	}
	public void invalidateScrim()
	{
		if( mBackgroundAlpha > 0.0f )
		{
			invalidate();
		}
	}
	
	public void setBackgroundAlpha(
			float alpha )
	{
		if( alpha != mBackgroundAlpha )
		{
			mBackgroundAlpha = alpha;
			invalidate();
		}
	}
	
	public float getBackgroundAlpha()
	{
		return mBackgroundAlpha;
	}
	
	public void setupAllAppsTransitionController(
			AllAppsTransitionController allAppsTransitionController )
	{
		mAllAppsController = allAppsTransitionController;
	}
	//zhujieping add end
}
