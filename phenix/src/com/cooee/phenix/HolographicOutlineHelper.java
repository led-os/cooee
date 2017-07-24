package com.cooee.phenix;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class HolographicOutlineHelper
{
	
	private final Paint mHolographicPaint = new Paint();
	private final Paint mBlurPaint = new Paint();
	private final Paint mErasePaint = new Paint();
	public int mMaxOuterBlurRadius;
	public int mMinOuterBlurRadius;
	private BlurMaskFilter mExtraThickOuterBlurMaskFilter;
	private BlurMaskFilter mThickOuterBlurMaskFilter;
	private BlurMaskFilter mMediumOuterBlurMaskFilter;
	private BlurMaskFilter mThinOuterBlurMaskFilter;
	private BlurMaskFilter mThickInnerBlurMaskFilter;
	private BlurMaskFilter mExtraThickInnerBlurMaskFilter;
	private BlurMaskFilter mMediumInnerBlurMaskFilter;
	private static final int THICK = 0;
	private static final int MEDIUM = 1;
	private static final int EXTRA_THICK = 2;
	static HolographicOutlineHelper INSTANCE;
	
	private HolographicOutlineHelper(
			Context context )
	{
		final float scale = LauncherAppState.getInstance().getScreenDensity();
		mMinOuterBlurRadius = (int)( scale * 1.0f );
		mMaxOuterBlurRadius = (int)( scale * 12.0f );
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		//		float mExtraThickOuterBlurRadius = 12.0f;//xiatian del
		float mExtraThickOuterBlurRadius = getExtraThickOuterBlurRadius();//xiatian add
		//xiatian end
		mExtraThickOuterBlurMaskFilter = new BlurMaskFilter( scale * mExtraThickOuterBlurRadius , BlurMaskFilter.Blur.OUTER );
		mThickOuterBlurMaskFilter = new BlurMaskFilter( scale * 6.0f , BlurMaskFilter.Blur.OUTER );
		mMediumOuterBlurMaskFilter = new BlurMaskFilter( scale * 2.0f , BlurMaskFilter.Blur.OUTER );
		mThinOuterBlurMaskFilter = new BlurMaskFilter( scale * 1.0f , BlurMaskFilter.Blur.OUTER );
		mExtraThickInnerBlurMaskFilter = new BlurMaskFilter( scale * 6.0f , BlurMaskFilter.Blur.NORMAL );
		mThickInnerBlurMaskFilter = new BlurMaskFilter( scale * 4.0f , BlurMaskFilter.Blur.NORMAL );
		mMediumInnerBlurMaskFilter = new BlurMaskFilter( scale * 2.0f , BlurMaskFilter.Blur.NORMAL );
		mHolographicPaint.setFilterBitmap( true );
		mHolographicPaint.setAntiAlias( true );
		mBlurPaint.setFilterBitmap( true );
		mBlurPaint.setAntiAlias( true );
		mErasePaint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.DST_OUT ) );
		mErasePaint.setFilterBitmap( true );
		mErasePaint.setAntiAlias( true );
		// zhangjin@2016/05/12 ADD START
		Resources res = context.getResources();
		mShadowBlurMaskFilter = new BlurMaskFilter( LauncherDefaultConfig.getFloatDimension( R.dimen.blur_size_click_shadow ) , BlurMaskFilter.Blur.NORMAL );
		// zhangjin@2016/05/12 ADD END
	}
	
	public static HolographicOutlineHelper obtain(
			Context context )
	{
		if( INSTANCE == null )
		{
			INSTANCE = new HolographicOutlineHelper( context );
		}
		return INSTANCE;
	}
	
	/**
	 * Returns the interpolated holographic highlight alpha for the effect we want when scrolling
	 * pages.
	 */
	public static float highlightAlphaInterpolator(
			float r )
	{
		float maxAlpha = 0.6f;
		return (float)Math.pow( maxAlpha * ( 1.0f - r ) , 1.5f );
	}
	
	/**
	 * Returns the interpolated view alpha for the effect we want when scrolling pages.
	 */
	public static float viewAlphaInterpolator(
			float r )
	{
		final float pivot = 0.95f;
		if( r < pivot )
		{
			return (float)Math.pow( r / pivot , 1.5f );
		}
		else
		{
			return 1.0f;
		}
	}
	
	/**
	 * Applies a more expensive and accurate outline to whatever is currently drawn in a specified
	 * bitmap.
	 */
	void applyExpensiveOutlineWithBlur(
			Bitmap srcDst ,
			Canvas srcDstCanvas ,
			int color ,
			int outlineColor ,
			int thickness )
	{
		applyExpensiveOutlineWithBlur( srcDst , srcDstCanvas , color , outlineColor , true , thickness );
	}
	
	void applyExpensiveOutlineWithBlur(
			Bitmap srcDst ,
			Canvas srcDstCanvas ,
			int color ,
			int outlineColor ,
			boolean clipAlpha ,
			int thickness )
	{
		// We start by removing most of the alpha channel so as to ignore shadows, and
		// other types of partial transparency when defining the shape of the object
		if( clipAlpha )
		{
			int[] srcBuffer = new int[srcDst.getWidth() * srcDst.getHeight()];
			srcDst.getPixels( srcBuffer , 0 , srcDst.getWidth() , 0 , 0 , srcDst.getWidth() , srcDst.getHeight() );
			for( int i = 0 ; i < srcBuffer.length ; i++ )
			{
				final int alpha = srcBuffer[i] >>> 24;
				if( alpha < 188 )
				{
					srcBuffer[i] = 0;
				}
			}
			srcDst.setPixels( srcBuffer , 0 , srcDst.getWidth() , 0 , 0 , srcDst.getWidth() , srcDst.getHeight() );
		}
		Bitmap glowShape = srcDst.extractAlpha();
		// calculate the outer blur first
		BlurMaskFilter outerBlurMaskFilter;
		switch( thickness )
		{
			case EXTRA_THICK:
				outerBlurMaskFilter = mExtraThickOuterBlurMaskFilter;
				break;
			case THICK:
				outerBlurMaskFilter = mThickOuterBlurMaskFilter;
				break;
			case MEDIUM:
				outerBlurMaskFilter = mMediumOuterBlurMaskFilter;
				break;
			default:
				throw new RuntimeException( "Invalid blur thickness" );
		}
		mBlurPaint.setMaskFilter( outerBlurMaskFilter );
		int[] outerBlurOffset = new int[2];
		Bitmap thickOuterBlur = glowShape.extractAlpha( mBlurPaint , outerBlurOffset );
		if( thickness == EXTRA_THICK )
		{
			mBlurPaint.setMaskFilter( mMediumOuterBlurMaskFilter );
		}
		else
		{
			mBlurPaint.setMaskFilter( mThinOuterBlurMaskFilter );
		}
		int[] brightOutlineOffset = new int[2];
		Bitmap brightOutline = glowShape.extractAlpha( mBlurPaint , brightOutlineOffset );
		// calculate the inner blur
		srcDstCanvas.setBitmap( glowShape );
		srcDstCanvas.drawColor( 0xFF000000 , PorterDuff.Mode.SRC_OUT );
		BlurMaskFilter innerBlurMaskFilter;
		switch( thickness )
		{
			case EXTRA_THICK:
				innerBlurMaskFilter = mExtraThickInnerBlurMaskFilter;
				break;
			case THICK:
				innerBlurMaskFilter = mThickInnerBlurMaskFilter;
				break;
			case MEDIUM:
				innerBlurMaskFilter = mMediumInnerBlurMaskFilter;
				break;
			default:
				throw new RuntimeException( "Invalid blur thickness" );
		}
		mBlurPaint.setMaskFilter( innerBlurMaskFilter );
		int[] thickInnerBlurOffset = new int[2];
		Bitmap thickInnerBlur = glowShape.extractAlpha( mBlurPaint , thickInnerBlurOffset );
		// mask out the inner blur
		srcDstCanvas.setBitmap( thickInnerBlur );
		srcDstCanvas.drawBitmap( glowShape , -thickInnerBlurOffset[0] , -thickInnerBlurOffset[1] , mErasePaint );
		srcDstCanvas.drawRect( 0 , 0 , -thickInnerBlurOffset[0] , thickInnerBlur.getHeight() , mErasePaint );
		srcDstCanvas.drawRect( 0 , 0 , thickInnerBlur.getWidth() , -thickInnerBlurOffset[1] , mErasePaint );
		// draw the inner and outer blur
		srcDstCanvas.setBitmap( srcDst );
		srcDstCanvas.drawColor( 0 , PorterDuff.Mode.CLEAR );
		mHolographicPaint.setColor( color );
		srcDstCanvas.drawBitmap( thickInnerBlur , thickInnerBlurOffset[0] , thickInnerBlurOffset[1] , mHolographicPaint );
		srcDstCanvas.drawBitmap( thickOuterBlur , outerBlurOffset[0] , outerBlurOffset[1] , mHolographicPaint );
		// draw the bright outline
		mHolographicPaint.setColor( outlineColor );
		srcDstCanvas.drawBitmap( brightOutline , brightOutlineOffset[0] , brightOutlineOffset[1] , mHolographicPaint );
		// cleanup
		srcDstCanvas.setBitmap( null );
		brightOutline.recycle();
		thickOuterBlur.recycle();
		thickInnerBlur.recycle();
		glowShape.recycle();
	}
	
	void applyExtraThickExpensiveOutlineWithBlur(
			Bitmap srcDst ,
			Canvas srcDstCanvas ,
			int color ,
			int outlineColor )
	{
		applyExpensiveOutlineWithBlur( srcDst , srcDstCanvas , color , outlineColor , EXTRA_THICK );
	}
	
	void applyThickExpensiveOutlineWithBlur(
			Bitmap srcDst ,
			Canvas srcDstCanvas ,
			int color ,
			int outlineColor )
	{
		applyExpensiveOutlineWithBlur( srcDst , srcDstCanvas , color , outlineColor , THICK );
	}
	
	void applyMediumExpensiveOutlineWithBlur(
			Bitmap srcDst ,
			Canvas srcDstCanvas ,
			int color ,
			int outlineColor ,
			boolean clipAlpha )
	{
		applyExpensiveOutlineWithBlur( srcDst , srcDstCanvas , color , outlineColor , clipAlpha , MEDIUM );
	}
	
	void applyMediumExpensiveOutlineWithBlur(
			Bitmap srcDst ,
			Canvas srcDstCanvas ,
			int color ,
			int outlineColor )
	{
		applyExpensiveOutlineWithBlur( srcDst , srcDstCanvas , color , outlineColor , MEDIUM );
	}
	
	private float getExtraThickOuterBlurRadius()
	{
		float ret = 0;
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			//cheyingkun add start	//解决“底边栏图标高亮时，白色光圈被截掉”的问题【i_0014426】
			//根据cell格子的大小和图标大小的差值,设置光圈的半径.因为外面统一乘了屏幕密度,这里除一下
			final float scale = LauncherAppState.getInstance().getScreenDensity();
			int edgeMarginPx = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getEdgeMarginPx();
			ret = edgeMarginPx / scale;
			//cheyingkun add end
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//模糊半径过大的话，底边栏图标在显示模糊边框时，模糊边框会由于超出底边栏区域范围而被切边。
			ret = 5.5f;
		}
		//xiatian add end
		return ret;
	}
	
	// zhangjin@2016/05/12 ADD START
	private final Canvas mCanvas = new Canvas();
	private final SparseArray<Bitmap> mBitmapCache = new SparseArray<Bitmap>( 4 );
	private final BlurMaskFilter mShadowBlurMaskFilter;
	
	Bitmap createMediumDropShadow(
			BubbleTextView view )
	{
		Drawable icon = view.getIcon();
		if( icon == null )
		{
			return null;
		}
		Rect rect = icon.getBounds();
		int bitmapWidth = (int)( rect.width() * view.getScaleX() );
		int bitmapHeight = (int)( rect.height() * view.getScaleY() );
		int key = ( bitmapWidth << 16 ) | bitmapHeight;
		Bitmap cache = mBitmapCache.get( key );
		if( cache == null )
		{
			cache = Bitmap.createBitmap( bitmapWidth , bitmapHeight , Bitmap.Config.ARGB_8888 );
			mCanvas.setBitmap( cache );
			mBitmapCache.put( key , cache );
		}
		else
		{
			mCanvas.setBitmap( cache );
			mCanvas.drawColor( Color.TRANSPARENT , PorterDuff.Mode.CLEAR );
		}
		mCanvas.save( Canvas.MATRIX_SAVE_FLAG );
		mCanvas.scale( view.getScaleX() , view.getScaleY() );
		mCanvas.translate( -rect.left , -rect.top );
		icon.draw( mCanvas );
		mCanvas.restore();
		mCanvas.setBitmap( null );
		mBlurPaint.setMaskFilter( mShadowBlurMaskFilter );
		return cache.extractAlpha( mBlurPaint , null );
	}
	// zhangjin@2016/05/12 ADD END
}
