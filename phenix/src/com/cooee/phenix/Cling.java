package com.cooee.phenix;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.FocusFinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cooee.phenix.Folder.Folder;


public class Cling extends FrameLayout implements Insettable , View.OnClickListener , View.OnLongClickListener , View.OnTouchListener
{
	
	static final String FIRST_RUN_CLING_DISMISSED_KEY = "cling_gel.first_run.dismissed";
	static final String WORKSPACE_CLING_DISMISSED_KEY = "cling_gel.workspace.dismissed";
	static final String FOLDER_CLING_DISMISSED_KEY = "cling_gel.folder.dismissed";
	private static String FIRST_RUN_PORTRAIT = "first_run_portrait";
	private static String WORKSPACE_PORTRAIT = "workspace_portrait";
	private static String FOLDER_PORTRAIT = "folder_portrait";
	private static float FIRST_RUN_CIRCLE_BUFFER_DPS = 60;
	private static float WORKSPACE_INNER_CIRCLE_RADIUS_DPS = 50;
	private static float WORKSPACE_OUTER_CIRCLE_RADIUS_DPS = 60;
	private static float WORKSPACE_CIRCLE_Y_OFFSET_DPS = 30;
	private Launcher mLauncher;
	private boolean mIsInitialized;
	private String mDrawIdentifier;
	private Drawable mBackground;
	private int[] mTouchDownPt = new int[2];
	private Drawable mFocusedHotseatApp;
	private ComponentName mFocusedHotseatAppComponent;
	private Rect mFocusedHotseatAppBounds;
	private Paint mErasePaint;
	private Paint mBubblePaint;
	private Paint mDotPaint;
	private View mScrimView;
	private int mBackgroundColor;
	private final Rect mInsets = new Rect();
	//<phenix modify> liuhailin@2015-03-18 modify begin
	private Bitmap eraseBg = null;
	//<phenix modify> liuhailin@2015-03-18 modify end
	;
	DisplayMetrics mDisplayMetricsForDispatchDraw = null;
	Rect mBubbleContentRect = null;
	
	public Cling(
			Context context )
	{
		this( context , null , 0 );
	}
	
	public Cling(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public Cling(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		TypedArray a = context.obtainStyledAttributes( attrs , R.styleable.Cling , defStyle , 0 );
		mDrawIdentifier = a.getString( R.styleable.Cling_drawIdentifier );
		a.recycle();
		setClickable( true );
	}
	
	void init(
			Launcher l ,
			View scrim )
	{
		if( !mIsInitialized )
		{
			mLauncher = l;
			mScrimView = scrim;
			mBackgroundColor = 0xdd000000;
			setOnLongClickListener( this );
			setOnClickListener( this );
			setOnTouchListener( this );
			mErasePaint = new Paint();
			mErasePaint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.MULTIPLY ) );
			mErasePaint.setColor( 0xFFFFFF );
			mErasePaint.setAlpha( 0 );
			mErasePaint.setAntiAlias( true );
			int circleColor = getResources().getColor( R.color.first_run_cling_circle_background_color );
			mBubblePaint = new Paint();
			mBubblePaint.setColor( circleColor );
			mBubblePaint.setAntiAlias( true );
			mDotPaint = new Paint();
			mDotPaint.setColor( 0x72BBED );
			mDotPaint.setAntiAlias( true );
			mIsInitialized = true;
		}
	}
	
	void setFocusedHotseatApp(
			int drawableId ,
			int appRank ,
			ComponentName cn ,
			String title ,
			String description )
	{
		// Get the app to draw
		Resources r = getResources();
		int appIconId = drawableId;
		Hotseat hotseat = mLauncher.getHotseat();
		if( hotseat != null && appIconId > -1 && appRank > -1 && !title.isEmpty() && !description.isEmpty() )
		{
			// Set the app bounds
			int x = hotseat.getCellXFromOrder( appRank );
			int y = hotseat.getCellYFromOrder( appRank );
			Rect pos = hotseat.getCellCoordinates( x , y );
			LauncherAppState app = LauncherAppState.getInstance();
			DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
			mFocusedHotseatApp = getResources().getDrawable( appIconId );
			mFocusedHotseatAppComponent = cn;
			mFocusedHotseatAppBounds = new Rect( pos.left , pos.top , pos.left + Utilities.sIconTextureWidth , pos.top + Utilities.sIconTextureHeight );
			Utilities.scaleRectAboutCenter( mFocusedHotseatAppBounds , ( grid.getHotseatIconSize() / grid.getIconSize() ) );
			// Set the title
			TextView v = (TextView)findViewById( R.id.focused_hotseat_app_title );
			if( v != null )
			{
				v.setText( title );
			}
			// Set the description
			v = (TextView)findViewById( R.id.focused_hotseat_app_description );
			if( v != null )
			{
				v.setText( description );
			}
			// Show the bubble
			View bubble = findViewById( R.id.focused_hotseat_app_bubble );
			bubble.setVisibility( View.VISIBLE );
		}
	}
	
	void show(
			boolean animate ,
			int duration )
	{
		setVisibility( View.VISIBLE );
		setLayerType( View.LAYER_TYPE_HARDWARE , null );
		if( mDrawIdentifier.equals( WORKSPACE_PORTRAIT ) )
		{
			View content = getContent();
			content.setAlpha( 0f );
			content.animate().alpha( 1f ).setDuration( duration ).setListener( null ).start();
			setAlpha( 1f );
		}
		else
		{
			if( animate )
			{
				buildLayer();
				setAlpha( 0f );
				animate().alpha( 1f ).setInterpolator( new AccelerateInterpolator() ).setDuration( duration ).setListener( null ).start();
			}
			else
			{
				setAlpha( 1f );
			}
		}
		// Show the scrim if necessary
		if( mScrimView != null )
		{
			mScrimView.setVisibility( View.VISIBLE );
			mScrimView.setAlpha( 0f );
			mScrimView.animate().alpha( 1f ).setDuration( duration ).setListener( null ).start();
		}
		setFocusableInTouchMode( true );
		post( new Runnable() {
			
			public void run()
			{
				setFocusable( true );
				requestFocus();
			}
		} );
	}
	
	void hide(
			final int duration ,
			final Runnable postCb )
	{
		if( mDrawIdentifier.equals( FIRST_RUN_PORTRAIT ) )
		{
			View content = getContent();
			content.animate().alpha( 0f ).setDuration( duration ).setListener( new AnimatorListenerAdapter() {
				
				public void onAnimationEnd(
						Animator animation )
				{
					// We are about to trigger the workspace cling, so don't do anything else
					setVisibility( View.GONE );
					postCb.run();
				};
			} ).start();
		}
		else
		{
			animate().alpha( 0f ).setDuration( duration ).setListener( new AnimatorListenerAdapter() {
				
				public void onAnimationEnd(
						Animator animation )
				{
					// We are about to trigger the workspace cling, so don't do anything else
					setVisibility( View.GONE );
					postCb.run();
				};
			} ).start();
		}
		// Show the scrim if necessary
		if( mScrimView != null )
		{
			mScrimView.animate().alpha( 0f ).setDuration( duration ).setListener( new AnimatorListenerAdapter() {
				
				public void onAnimationEnd(
						Animator animation )
				{
					mScrimView.setVisibility( View.GONE );
				};
			} ).start();
		}
	}
	
	void cleanup()
	{
		mBackground = null;
		mIsInitialized = false;
	}
	
	public void bringScrimToFront()
	{
		if( mScrimView != null )
		{
			mScrimView.bringToFront();
		}
	}
	
	@Override
	public void setInsets(
			Rect insets )
	{
		mInsets.set( insets );
		// zhujieping@2015/04/23 UPDATE START,launcher增加属性WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS，因此要多算状态栏的高度
		int statusBarHeight = 0;
		if( mLauncher != null )
			statusBarHeight = mLauncher.getStatusBarHeight( false );
		setPadding( insets.left , insets.top + statusBarHeight , insets.right , insets.bottom );
		// zhujieping@2015/04/23 UPDATE END
	}
	
	View getContent()
	{
		return findViewById( R.id.content );
	}
	
	String getDrawIdentifier()
	{
		return mDrawIdentifier;
	}
	
	@Override
	public View focusSearch(
			int direction )
	{
		return this.focusSearch( this , direction );
	}
	
	@Override
	public View focusSearch(
			View focused ,
			int direction )
	{
		return FocusFinder.getInstance().findNextFocus( this , focused , direction );
	}
	
	@Override
	public boolean onHoverEvent(
			MotionEvent event )
	{
		return false;
	}
	
	@Override
	public boolean onTouchEvent(
			android.view.MotionEvent event )
	{
		if( mDrawIdentifier.equals( FOLDER_PORTRAIT ) )
		{
			Folder f = mLauncher.getWorkspace().getOpenFolder();
			if( f != null )
			{
				Rect r = new Rect();
				f.getHitRect( r );
				if( r.contains( (int)event.getX() , (int)event.getY() ) )
				{
					return false;
				}
			}
		}
		return super.onTouchEvent( event );
	};
	
	@Override
	public boolean onTouch(
			View v ,
			MotionEvent ev )
	{
		if( ev.getAction() == MotionEvent.ACTION_DOWN )
		{
			mTouchDownPt[0] = (int)ev.getX();
			mTouchDownPt[1] = (int)ev.getY();
		}
		return false;
	}
	
	@Override
	public void onClick(
			View v )
	{
		if( mDrawIdentifier.equals( WORKSPACE_PORTRAIT ) )
		{
			if( mFocusedHotseatAppBounds != null && mFocusedHotseatAppBounds.contains( mTouchDownPt[0] , mTouchDownPt[1] ) )
			{
				// Launch the activity that is being highlighted
				Intent intent = new Intent( Intent.ACTION_MAIN );
				intent.setComponent( mFocusedHotseatAppComponent );
				intent.addCategory( Intent.CATEGORY_LAUNCHER );
				// gaominghui@2016/12/14 ADD START
				if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
				{
					mLauncher.startActivity( intent , null );
				}
				else
				{
					mLauncher.startActivity( intent );
				}
				// gaominghui@2016/12/14 ADD END
				mLauncher.dismissWorkspaceCling( this );
				//<phenix modify> liuhailin@2015-03-18 begin
				if( eraseBg != null && !eraseBg.isRecycled() )
				{
					eraseBg.recycle();
					eraseBg = null;
				}
				//<phenix modify> liuhailin@2015-03-18 end
			}
		}
	}
	
	@Override
	public boolean onLongClick(
			View v )
	{
		if( mDrawIdentifier.equals( WORKSPACE_PORTRAIT ) )
		{
			mLauncher.dismissWorkspaceCling( null );
			return true;
		}
		return false;
	}
	
	@Override
	protected void dispatchDraw(
			Canvas canvas )
	{
		if( mIsInitialized )
		{
			canvas.save();
			// Draw the background
			//<phenix modify> liuhailin@2015-03-18 del begin
			//Bitmap eraseBg = null;
			//<phenix modify> liuhailin@2015-03-18 del end
			if( mDisplayMetricsForDispatchDraw == null )
			{
				mDisplayMetricsForDispatchDraw = new DisplayMetrics();
			}
			mLauncher.getWindowManager().getDefaultDisplay().getMetrics( mDisplayMetricsForDispatchDraw );
			if( mScrimView != null )
			{
				// Skip drawing the background
				mScrimView.setBackgroundColor( mBackgroundColor );
			}
			else if( mBackground != null )
			{
				mBackground.setBounds( 0 , 0 , getMeasuredWidth() , getMeasuredHeight() );
				mBackground.draw( canvas );
			}
			else if( mDrawIdentifier.equals( WORKSPACE_PORTRAIT ) )
			{
				// Initialize the draw buffer (to allow punching through)
				//<phenix modify> liuhailin@2015-03-18 modify begin
				if( eraseBg == null )
				{
					eraseBg = Bitmap.createBitmap( getMeasuredWidth() , getMeasuredHeight() , Bitmap.Config.ARGB_8888 );
					Canvas eraseCanvas = new Canvas( eraseBg );
					eraseCanvas.drawColor( mBackgroundColor );
					int offset = DynamicGrid.pxFromDp( WORKSPACE_CIRCLE_Y_OFFSET_DPS , mDisplayMetricsForDispatchDraw );
					mErasePaint.setAlpha( (int)( 128 ) );
					eraseCanvas.drawCircle(
							mDisplayMetricsForDispatchDraw.widthPixels / 2 ,
							mDisplayMetricsForDispatchDraw.heightPixels / 2 - offset ,
							DynamicGrid.pxFromDp( WORKSPACE_OUTER_CIRCLE_RADIUS_DPS , mDisplayMetricsForDispatchDraw ) ,
							mErasePaint );
					mErasePaint.setAlpha( 0 );
					eraseCanvas.drawCircle(
							mDisplayMetricsForDispatchDraw.widthPixels / 2 ,
							mDisplayMetricsForDispatchDraw.heightPixels / 2 - offset ,
							DynamicGrid.pxFromDp( WORKSPACE_INNER_CIRCLE_RADIUS_DPS , mDisplayMetricsForDispatchDraw ) ,
							mErasePaint );
				}
				//<phenix modify> liuhailin@2015-03-18 modify end
			}
			else
			{
				canvas.drawColor( mBackgroundColor );
			}
			// Draw everything else
			//<phenix modify> liuhailin@2015-03-18 modify begin
			//DisplayMetrics metrics = new DisplayMetrics();
			//	mLauncher.getWindowManager().getDefaultDisplay().getMetrics( metrics );
			//<phenix modify> liuhailin@2015-03-18 modify end
			float alpha = getAlpha();
			View content = getContent();
			if( content != null )
			{
				alpha *= content.getAlpha();
			}
			if( mDrawIdentifier.equals( FIRST_RUN_PORTRAIT ) )
			{
				// Draw the circle
				View bubbleContent = findViewById( R.id.bubble_content );
				if( mBubbleContentRect == null )
				{
					mBubbleContentRect = new Rect();
				}
				bubbleContent.getGlobalVisibleRect( mBubbleContentRect );
				mBubblePaint.setAlpha( (int)( 255 * alpha ) );
				float buffer = DynamicGrid.pxFromDp( FIRST_RUN_CIRCLE_BUFFER_DPS , mDisplayMetricsForDispatchDraw );
				canvas.drawCircle( mDisplayMetricsForDispatchDraw.widthPixels / 2 , mBubbleContentRect.centerY() , ( bubbleContent.getMeasuredWidth() + buffer ) / 2 , mBubblePaint );
			}
			else if( mDrawIdentifier.equals( WORKSPACE_PORTRAIT ) )
			{
				//<phenix modify> liuhailin@2015-03-18 del begin
				//int offset = DynamicGrid.pxFromDp( WORKSPACE_CIRCLE_Y_OFFSET_DPS , metrics );
				//mErasePaint.setAlpha( (int)( 128 ) );
				//eraseCanvas.drawCircle( metrics.widthPixels / 2 , metrics.heightPixels / 2 - offset , DynamicGrid.pxFromDp( WORKSPACE_OUTER_CIRCLE_RADIUS_DPS , metrics ) , mErasePaint );
				//mErasePaint.setAlpha( 0 );
				//eraseCanvas.drawCircle( metrics.widthPixels / 2 , metrics.heightPixels / 2 - offset , DynamicGrid.pxFromDp( WORKSPACE_INNER_CIRCLE_RADIUS_DPS , metrics ) , mErasePaint );
				if( eraseBg != null )
				{
					canvas.drawBitmap( eraseBg , 0 , 0 , null );
				}
				//eraseCanvas.setBitmap( null );
				//eraseBg = null;
				//<phenix modify> liuhailin@2015-03-18 del end
				// Draw the focused hotseat app icon
				if( mFocusedHotseatAppBounds != null && mFocusedHotseatApp != null )
				{
					mFocusedHotseatApp.setBounds( mFocusedHotseatAppBounds.left , mFocusedHotseatAppBounds.top , mFocusedHotseatAppBounds.right , mFocusedHotseatAppBounds.bottom );
					mFocusedHotseatApp.setAlpha( (int)( 255 * alpha ) );
					mFocusedHotseatApp.draw( canvas );
				}
			}
			canvas.restore();
		}
		// Draw the rest of the cling
		super.dispatchDraw( canvas );
	};
}
