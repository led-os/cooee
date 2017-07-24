package com.cooee.phenix;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.theme.ThemeManager;
import com.cooee.util.Tools;
import com.cooee.utils.NDK_CooeeBlur;


/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities
{
	
	private static final String TAG = "Launcher.Utilities";
	public static final boolean ATLEAST_JB_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
	public static final boolean ATLEAST_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	public static final boolean ATLEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= 23;
	public static int sIconWidth = -1;
	public static int sIconHeight = -1;
	public static int sIconTextureWidth = -1;
	public static int sIconTextureHeight = -1;
	//xiatian del start	//整理代码，删除不用的参数
	//	private static final Paint sBlurPaint = new Paint();
	//	private static final Paint sGlowColorPressedPaint = new Paint();
	//	private static final Paint sGlowColorFocusedPaint = new Paint();
	//	private static final Paint sDisabledPaint = new Paint();
	//xiatian del end
	private static final Rect sOldBounds = new Rect();
	private static final Canvas sCanvas = new Canvas();
	static
	{
		sCanvas.setDrawFilter( new PaintFlagsDrawFilter( Paint.DITHER_FLAG , Paint.FILTER_BITMAP_FLAG ) );
	}
	static int sColors[] = { 0xffff0000 , 0xff00ff00 , 0xff0000ff };
	static int sColorIndex = 0;
	private static Bitmap mItemStyle1ThirdPartyIconBg = null;//xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
	// zhangjin@2016/05/05 ADD START
	private static final Pattern sTrimPattern = Pattern.compile( "^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$" );
	
	// zhangjin@2016/05/05 ADD END
	/**
	 * Returns a FastBitmapDrawable with the icon, accurately sized.
	 */
	public static Drawable createIconDrawable(
			Bitmap icon )
	{
		FastBitmapDrawable d = new FastBitmapDrawable( icon );
		d.setFilterBitmap( true );
		resizeIconDrawable( d );
		return d;
	}
	
	/**
	 * Resizes an icon drawable to the correct icon size.
	 */
	public static void resizeIconDrawable(
			Drawable icon )
	{
		icon.setBounds( 0 , 0 , sIconTextureWidth , sIconTextureHeight );
	}
	
	//cheyingkun add start	//主菜单图标缩放比。默认为1。
	/**
	 * Returns a FastBitmapDrawable with the icon, accurately sized.
	 */
	public static Drawable createIconDrawable(
			Bitmap icon ,
			float scale )
	{
		FastBitmapDrawable d = new FastBitmapDrawable( icon );
		d.setFilterBitmap( true );
		resizeIconDrawable( d , scale );
		return d;
	}
	
	/**
	 * Resizes an icon drawable to the correct icon size.
	 */
	public static void resizeIconDrawable(
			Drawable icon ,
			float scale )
	{
		icon.setBounds( 0 , 0 , (int)( sIconTextureWidth * scale ) , (int)( sIconTextureHeight * scale ) );
	}
	
	//cheyingkun add end
	/**
	 * Returns a Bitmap representing the thumbnail of the specified Bitmap.
	 *
	 * @param bitmap The bitmap to get a thumbnail of.
	 * @param context The application's context.
	 *
	 * @return A thumbnail for the specified bitmap or the bitmap itself if the
	 *         thumbnail could not be created.
	 */
	public static Bitmap resampleIconBitmap(
			Bitmap bitmap ,
			Context context )
	{
		synchronized( sCanvas )
		{ // we share the statics :-(
			if( sIconWidth == -1 )
			{
				initStatics( context );
			}
			if( bitmap.getWidth() == sIconWidth && bitmap.getHeight() == sIconHeight )
			{
				return bitmap;
			}
			else
			{
				final Resources resources = context.getResources();
				return createIconBitmap( new BitmapDrawable( resources , bitmap ) , context , sIconWidth , sIconHeight , sIconTextureWidth , sIconTextureHeight , true );
			}
		}
	}
	
	/**
	 * Given a coordinate relative to the descendant, find the coordinate in a parent view's
	 * coordinates.
	 *
	 * @param descendant The descendant to which the passed coordinate is relative.
	 * @param root The root view to make the coordinates relative to.
	 * @param coord The coordinate that we want mapped.
	 * @param includeRootScroll Whether or not to account for the scroll of the descendant:
	 *          sometimes this is relevant as in a child's coordinates within the descendant.
	 * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
	 *         this scale factor is assumed to be equal in X and Y, and so if at any point this
	 *         assumption fails, we will need to return a pair of scale factors.
	 */
	public static float getDescendantCoordRelativeToParent(
			View descendant ,
			View root ,
			int[] coord ,
			boolean includeRootScroll )
	{
		ArrayList<View> ancestorChain = new ArrayList<View>();
		float[] pt = { coord[0] , coord[1] };
		View v = descendant;
		while( v != root && v != null )
		{
			ancestorChain.add( v );
			v = (View)v.getParent();
		}
		ancestorChain.add( root );
		float scale = 1.0f;
		int count = ancestorChain.size();
		for( int i = 0 ; i < count ; i++ )
		{
			View v0 = ancestorChain.get( i );
			// For TextViews, scroll has a meaning which relates to the text position
			// which is very strange... ignore the scroll.
			if( v0 != descendant || includeRootScroll )
			{
				pt[0] -= v0.getScrollX();
				pt[1] -= v0.getScrollY();
			}
			v0.getMatrix().mapPoints( pt );
			pt[0] += v0.getLeft();
			pt[1] += v0.getTop();
			scale *= v0.getScaleX();
		}
		coord[0] = (int)Math.round( pt[0] );
		coord[1] = (int)Math.round( pt[1] );
		return scale;
	}
	
	/**
	 * Inverse of {@link #getDescendantCoordRelativeToSelf(View, int[])}.
	 */
	public static float mapCoordInSelfToDescendent(
			View descendant ,
			View root ,
			int[] coord )
	{
		ArrayList<View> ancestorChain = new ArrayList<View>();
		float[] pt = { coord[0] , coord[1] };
		View v = descendant;
		while( v != root )
		{
			ancestorChain.add( v );
			v = (View)v.getParent();
		}
		ancestorChain.add( root );
		float scale = 1.0f;
		Matrix inverse = new Matrix();
		int count = ancestorChain.size();
		for( int i = count - 1 ; i >= 0 ; i-- )
		{
			View ancestor = ancestorChain.get( i );
			View next = i > 0 ? ancestorChain.get( i - 1 ) : null;
			pt[0] += ancestor.getScrollX();
			pt[1] += ancestor.getScrollY();
			if( next != null )
			{
				pt[0] -= next.getLeft();
				pt[1] -= next.getTop();
				next.getMatrix().invert( inverse );
				inverse.mapPoints( pt );
				scale *= next.getScaleX();
			}
		}
		coord[0] = (int)Math.round( pt[0] );
		coord[1] = (int)Math.round( pt[1] );
		return scale;
	}
	
	private static void initStatics(
			Context context )
	{
		//xiatian start	//整理代码，修改设置icon尺寸的逻辑（设置icon尺寸应该是在grid初始化之后）
		//xiatian del start
		//		final Resources resources = context.getResources();
		//		setIconSize( (int)resources.getDimension( R.dimen.app_icon_size ) );
		//xiatian del end
		//xiatian add start
		DynamicGrid mDynamicGrid = LauncherAppState.getInstance().getDynamicGrid();
		if( mDynamicGrid == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "initStatics-(mDynamicGrid==null)" );
			return;
		}
		DeviceProfile grid = mDynamicGrid.getDeviceProfile();
		if( grid == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , "initStatics-(DeviceProfile==null)" );
			return;
		}
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		//		setIconSize( grid.getIconWidthSizePx() );//xiatian del
		//xiatian add start
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			setIconSize( grid.getIconWidthSizePx() , grid.getIconHeightSizePx() );
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			setIconSize( grid.getSignleViewAvailableWidthPx() , grid.getSignleViewAvailableHeightPx() );
		}
		//xiatian add end
		//xiatian end
		//xiatian add start
		//xiatian end
		//xiatian del start	//整理代码，删除不用的参数
		//		final DisplayMetrics metrics = resources.getDisplayMetrics();
		//		final float density = metrics.density;
		//		sBlurPaint.setMaskFilter( new BlurMaskFilter( 5 * density , BlurMaskFilter.Blur.NORMAL ) );
		//		sGlowColorPressedPaint.setColor( 0xffffc300 );
		//		sGlowColorFocusedPaint.setColor( 0xffff8e00 );
		//		ColorMatrix cm = new ColorMatrix();
		//		cm.setSaturation( 0.2f );
		//		sDisabledPaint.setColorFilter( new ColorMatrixColorFilter( cm ) );
		//		sDisabledPaint.setAlpha( 0x88 );
		//xiatian del end
	}
	
	public static void setIconSize(
			int widthPx ,
			int heightPx //xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
	)
	{
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		//xiatian del start
		//		sIconWidth = sIconHeight = widthPx;
		//		sIconTextureWidth = sIconTextureHeight = widthPx;
		//xiatian del end
		//xiatian add start
		sIconWidth = sIconTextureWidth = widthPx;
		sIconHeight = sIconTextureHeight = heightPx;
		//xiatian add end
		//xiatian end
	}
	
	public static void scaleRect(
			Rect r ,
			float scale )
	{
		if( scale != 1.0f )
		{
			r.left = (int)( r.left * scale + 0.5f );
			r.top = (int)( r.top * scale + 0.5f );
			r.right = (int)( r.right * scale + 0.5f );
			r.bottom = (int)( r.bottom * scale + 0.5f );
		}
	}
	
	public static void scaleRectAboutCenter(
			Rect r ,
			float scale )
	{
		int cx = r.centerX();
		int cy = r.centerY();
		r.offset( -cx , -cy );
		Utilities.scaleRect( r , scale );
		r.offset( cx , cy );
	}
	
	public static void startActivityForResultSafely(
			Activity activity ,
			Intent intent ,
			int requestCode )
	{
		try
		{
			activity.startActivityForResult( intent , requestCode );
		}
		catch( ActivityNotFoundException e )
		{
			Toast.makeText( activity , R.string.activity_not_found , Toast.LENGTH_SHORT ).show();
		}
		catch( SecurityException e )
		{
			Toast.makeText( activity , R.string.activity_not_found , Toast.LENGTH_SHORT ).show();
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , StringUtils.concat(
						"Launcher does not have the permission to launch " ,
						intent.toUri( 0 ) ,
						". Make sure to create a MAIN intent-filter for the corresponding activity or use the exported attribute for this activity." ) , e );
		}
	}
	
	//<phenix modify> liuhailin@2015-01-26 modify begin
	public static Bitmap combineIcon(
			Context context ,
			Bitmap icon ,
			Bitmap bg ,
			Bitmap mask ,
			Bitmap cover ,
			boolean isNeedScale ,
			float mIconPaddingBgBottomPersent ,
			boolean mIsRecycleIconSource ,//是否回收icon
			boolean mIsRecycleOtherSource //是否回收背板、蒙版和盖板
	)
	{
		if( icon == null )
			throw new RuntimeException( "icon must not be empty" );
		Bitmap result = null;
		synchronized( sCanvas )
		{ // we share the statics :-(
			if( sIconWidth == -1 )
			{
				initStatics( context );
			}
			int textureWidth = sIconTextureWidth;
			int textureHeight = sIconTextureHeight;
			final Bitmap bitmap = Bitmap.createBitmap( textureWidth , textureHeight , Bitmap.Config.ARGB_8888 );
			final Canvas canvas = sCanvas;
			canvas.setBitmap( bitmap );
			Paint paint = new Paint();
			float mScale = 1f;
			//adjust bitmap start
			float mOffsetX = 0;
			float mOffsetY = 0;
			AdjustBitmapRet ret = null;
			//adjust bitmap end
			//bg
			if( bg != null )
			{
				//adjust bitmap
				ret = adjustBitmapWhenCombineIcon( bg , sIconTextureWidth , sIconTextureHeight , mIsRecycleOtherSource );
				bg = ret.mBitmap;
				mOffsetX = ret.mOffsetX;
				mOffsetY = ret.mOffsetY;
				//adjust bitmap
				canvas.drawBitmap( bg , mOffsetX , mOffsetY , paint );
				if( ( isNeedScale ) && ( ThemeManager.getInstance() != null ) && ( ThemeManager.getInstance().getCurrentThemeDescription() != null ) )
				{
					mScale = (float)( ThemeManager.getInstance().getInt( "theme_thirdapk_icon_scaleFactor" , 80 ) ) / 100f;
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
						Log.d( TAG , StringUtils.concat( "theme_thirdapk_icon_scaleFactor = " , mScale ) );
				}
				if( mIsRecycleOtherSource )
				{
					if( !bg.isRecycled() )
					{
						bg.recycle();
					}
				}
			}
			//理论上，icon和mask先组合成一个位图层来确定icon的显示部分。
			int saveLayer = -1;
			if( mask != null )
			{
				saveLayer = canvas.saveLayer(
						0 ,
						0 ,
						textureWidth ,
						textureHeight ,
						null ,
						Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG );
			}
			//icon
			if( mScale != 1f )
			{
				icon = Tools.resizeBitmap( icon , mScale );
				mOffsetX = ( textureWidth - icon.getWidth() ) / 2;
				mOffsetY = ( textureHeight - icon.getHeight() ) / 2;
				canvas.drawBitmap( icon , mOffsetX , mOffsetY , paint );
			}
			else
			{
				//adjust bitmap
				ret = adjustBitmapWhenCombineIcon( icon , sIconTextureWidth , (int)( sIconTextureHeight * ( 1 - mIconPaddingBgBottomPersent ) ) , mIsRecycleIconSource );
				icon = ret.mBitmap;
				mOffsetX = ret.mOffsetX;
				mOffsetY = ret.mOffsetY;
				//adjust bitmap
				canvas.drawBitmap( icon , mOffsetX , mOffsetY , paint );
			}
			if( mIsRecycleIconSource )
			{
				if( icon != null && !icon.isRecycled() )
				{
					icon.recycle();
				}
			}
			//mask
			if( saveLayer != -1 )
			{
				paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.DST_IN ) );
				//adjust bitmap
				ret = adjustBitmapWhenCombineIcon( mask , sIconTextureWidth , sIconTextureHeight , mIsRecycleOtherSource );
				mask = ret.mBitmap;
				mOffsetX = ret.mOffsetX;
				mOffsetY = ret.mOffsetY;
				//adjust bitmap
				canvas.drawBitmap( mask , mOffsetX , mOffsetY , paint );
				paint.setXfermode( null );
				if( mIsRecycleOtherSource )
				{
					if( !mask.isRecycled() )
					{
						mask.recycle();
					}
				}
				canvas.restoreToCount( saveLayer );
			}
			//cover
			if( cover != null )
			{
				//adjust bitmap
				ret = adjustBitmapWhenCombineIcon( cover , sIconTextureWidth , sIconTextureHeight , mIsRecycleOtherSource );
				cover = ret.mBitmap;
				mOffsetX = ret.mOffsetX;
				mOffsetY = ret.mOffsetY;
				//adjust bitmap
				canvas.drawBitmap( cover , mOffsetX , mOffsetY , paint );
				if( mIsRecycleOtherSource )
				{
					if( !cover.isRecycled() )
					{
						cover.recycle();
					}
				}
			}
			//
			result = bitmap.copy( Config.ARGB_8888 , true );
			//
			if( bitmap != null && !bitmap.isRecycled() )
				bitmap.recycle();
			int i = 0;
			i++;
			return result;
		}
	}
	
	/**
	 * 切除透明像素
	 * @param bitmap 要处理的bitmap
	 * @param holdBitmapSize 是否保持原有大小(true 返回和原图一样大小的bitmap 不放大 带透明像素,false 返回不带透明像素的bitmap)
	 * @return
	 */
	public static Bitmap cutTransparentPixels(
			Bitmap bitmap ,
			boolean holdBitmapSize )
	{
		//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "cyk_bug : c_0003400" , " Utilities cutTransparentPixels " );
		}
		//cheyingkun add end
		Rect temp = NDK_CooeeBlur.ImageMarginSize( bitmap , 64 );//64=(int)(255/4),此为产品确认值
		if( temp != null )
		{
			int bitmapWidth = bitmap.getWidth();
			int bitmapHeight = bitmap.getHeight();
			int cutX = temp.left;
			int cutY = temp.top;
			//cheyingkun start	//解决“切除透明像素时，右边和下边图标被切掉一个像素”的问题。【0003646】
			//cheyingkun del start
			//			int cutWidth = temp.right - cutX;
			//			int cutHeight = temp.bottom - cutY;
			//cheyingkun del end
			//cheyingkun add start
			int cutWidth = temp.right - cutX + 1;
			int cutHeight = temp.bottom - cutY + 1;
			//cheyingkun add end
			//cheyingkun end
			//
			if( cutX != 0 || cutY != 0 || bitmapWidth != cutWidth || bitmapHeight != cutHeight )
			{
				//cheyingkun start	//小于默认配置的图标大小的应用图标不放大，防止图标模糊。
				//cheyingkun del start
				//				float scaleWidth = bitmapWidth * 1.0f / cutWidth;
				//				float scaleHeight = bitmapHeight * 1.0f / cutHeight;
				//				//宽高用同一比例缩放 , change by shlt@2014/12/17 ADD START
				//				float scale = scaleWidth;
				//				if( scaleWidth > scaleHeight )
				//				{
				//					scale = scaleHeight;
				//				}
				//				float translateX = ( bitmapWidth - scale * cutWidth ) / 2;
				//				float translateY = ( bitmapHeight - scale * cutHeight ) / 2;
				//				Matrix matrix = new Matrix();
				//				matrix.postScale( scale , scale );
				//				//宽高用同一比例缩放 , change by shlt@2014/12/17 ADD END
				//				Bitmap cutBitmap = Bitmap.createBitmap( bitmap , cutX , cutY , cutWidth , cutHeight , matrix , true );
				//cheyingkun del end
				//cheyingkun add start
				float translateX = ( bitmapWidth - cutWidth ) / 2;
				float translateY = ( bitmapHeight - cutHeight ) / 2;
				//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
				Bitmap cutBitmap = null;
				try
				{
					cutBitmap = Bitmap.createBitmap( bitmap , cutX , cutY , cutWidth , cutHeight );
				}
				catch( Exception e )
				{
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.d( "cyk_bug : c_0003400" , StringUtils.concat(
								" Utilities cutTransparentPixels:return cutBitmap--bitmapWidth:" ,
								bitmapWidth ,
								"-bitmapHeight: " ,
								bitmapHeight ,
								"-cutX: " ,
								cutX ,
								"-cutY: " ,
								cutY ,
								"-cutWidth: " ,
								cutWidth ,
								"-cutHeight: " ,
								cutHeight ) );
					}
					return bitmap;
				}
				//cheyingkun add end
				//cheyingkun add end
				//cheyingkun end
				if( bitmap != null && !bitmap.isRecycled() )
				{
					bitmap.recycle();
				}
				//cheyingkun add start	//是否优先获取高分辨率图标（图标显示清晰）。true为优先获取高分辨率图标，没有高分辨图标则获取低分辨率图标；false为直接获取当前分辨率图标。默认为false。
				if( !holdBitmapSize )
				{
					//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
					if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					{
						Log.d( "cyk_bug : c_0003400" , StringUtils.concat( " Utilities cutTransparentPixels return cutBitmap: " , cutBitmap.getWidth() , " * " , cutBitmap.getHeight() ) );
					}
					//cheyingkun add end
					return cutBitmap;
				}
				//cheyingkun add end
				//保证有效显示区域居中 , change by shlt@2014/12/18 ADD START
				Bitmap newBitmap = Bitmap.createBitmap( bitmapWidth , bitmapHeight , Config.ARGB_8888 );
				Canvas canvas = new Canvas( newBitmap );
				canvas.drawBitmap( cutBitmap , translateX , translateY , null );
				if( cutBitmap != null && !cutBitmap.isRecycled() )
				{
					cutBitmap.recycle();
				}
				//保证有效显示区域居中 , change by shlt@2014/12/18 ADD END
				bitmap = newBitmap;
				canvas = null;
				//				matrix = null;//cheyingkun del	//小于默认配置的图标大小的应用图标不放大，防止图标模糊。
			}
			temp = null;
		}
		return bitmap;
	}
	//<phenix modify> liuhailin@2015-01-26 modify end
	;
	
	public static Bitmap createIconBitmap(
			Bitmap mBitmapSource ,
			Context context ,
			int mIconDestWidth ,
			int mIconDestHeight ,
			int mBitmapDestWidth ,
			int mBitmapDestHeight ,
			boolean isIconCenter ,//在宽带为mIconTextureWidth，高度为mIconTextureHeight的图中，icon是等比例居中显示，还是拉伸后铺满显示
			boolean mIsRecycleBitmapSource )
	{
		if( mBitmapSource == null )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "createIconBitmap" , "mBitmapSource == null" );
			return null;
		}
		Bitmap ret = createIconBitmap(
		//
				new BitmapDrawable( context.getResources() , mBitmapSource ) ,//
				context ,//
				mIconDestWidth ,//
				mIconDestHeight ,//
				mBitmapDestWidth ,//
				mBitmapDestHeight ,//
				isIconCenter //
		//
		);
		if( ( mIsRecycleBitmapSource == true ) && ( mBitmapSource != ret ) && ( mBitmapSource.isRecycled() == false ) )
		{
			mBitmapSource.recycle();
		}
		return ret;
	}
	
	/**
	 * Returns a bitmap suitable for the all apps view.
	 */
	public static Bitmap createIconBitmap(
			Drawable icon ,
			Context context ,
			int mIconDestWidth ,
			int mIconDestHeight ,
			int mBitmapDestWidth ,
			int mBitmapDestHeight ,
			boolean isIconCenter //在宽带为mIconTextureWidth，高度为mIconTextureHeight的图中，icon是等比例居中显示，还是拉伸后铺满显示
	)
	{
		synchronized( sCanvas )
		{ // we share the statics :-(
			if( mIconDestWidth == -1 )
			{
				initStatics( context );
				return createIconBitmap( icon , context , sIconWidth , sIconHeight , sIconTextureWidth , sIconTextureHeight , true );
			}
			int width = mIconDestWidth;
			int height = mIconDestHeight;
			if( icon instanceof PaintDrawable )
			{
				PaintDrawable painter = (PaintDrawable)icon;
				painter.setIntrinsicWidth( width );
				painter.setIntrinsicHeight( height );
			}
			else if( icon instanceof BitmapDrawable )
			{
				// Ensure the bitmap has a density.
				BitmapDrawable bitmapDrawable = (BitmapDrawable)icon;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				if( bitmap.getDensity() == Bitmap.DENSITY_NONE )
				{
					bitmapDrawable.setTargetDensity( context.getResources().getDisplayMetrics() );
				}
			}
			int sourceWidth = icon.getIntrinsicWidth();
			int sourceHeight = icon.getIntrinsicHeight();
			if( sourceWidth > 0 && sourceHeight > 0 )
			{
				//cheyingkun start	//小于默认配置的图标大小的应用图标不放大，防止图标模糊。
				//cheyingkun del start
				//				// Scale the icon proportionally to the icon dimensions
				//				final float ratio = (float)sourceWidth / sourceHeight;
				//				if( sourceWidth > sourceHeight )
				//				{
				//					height = (int)( width / ratio );
				//				}
				//				else if( sourceHeight > sourceWidth )
				//				{
				//					width = (int)( height * ratio );
				//				}
				//cheyingkun del end
				//cheyingkun add start
				final float scaleWidth = 1.0f * width / sourceWidth;
				final float scaleHeight = 1.0f * height / sourceHeight;
				if( scaleWidth >= 1 && scaleHeight >= 1 //原图宽高都小(直接使用原图宽高,不放大)
						&& !LauncherDefaultConfig.SWITCH_ENABLE_ENLARGE_ICON_SIZE_WHEN_SOURCE_SIZE_LESS_THEN_DEST_SIZE )//cheyingkun add	//当图标尺寸小于目标尺寸时，是否放大图标尺寸（放大会导致图标模糊）。true为放大；false为不放大。默认为false。
				{
					width = sourceWidth;
					height = sourceHeight;
				}
				else
				{
					float minScale = Math.min( scaleWidth , scaleHeight );//宽高比例中小的一个
					width = (int)( sourceWidth * minScale );
					height = (int)( sourceHeight * minScale );
				}
				//cheyingkun add end
				//cheyingkun end
			}
			// no intrinsic size --> use default size
			int textureWidth = mBitmapDestWidth;
			int textureHeight = mBitmapDestHeight;
			final Bitmap bitmap = Bitmap.createBitmap( textureWidth , textureHeight , Bitmap.Config.ARGB_8888 );
			final Canvas canvas = sCanvas;
			canvas.setBitmap( bitmap );
			final int left = ( textureWidth - width ) / 2;
			final int top = ( textureHeight - height ) / 2;
			@SuppressWarnings( "all" )
			// suppress dead code warning
			final boolean debug = false;
			if( debug )
			{
				// draw a big box for the icon for debugging
				canvas.drawColor( sColors[sColorIndex] );
				if( ++sColorIndex >= sColors.length )
					sColorIndex = 0;
				Paint debugPaint = new Paint();
				debugPaint.setColor( 0xffcccc00 );
				canvas.drawRect( left , top , left + width , top + height , debugPaint );
			}
			sOldBounds.set( icon.getBounds() );
			if( isIconCenter )
			{
				icon.setBounds( left , top , left + width , top + height );
			}
			else
			{
				icon.setBounds( 0 , 0 , 0 + textureWidth , 0 + textureHeight );
			}
			icon.draw( canvas );
			icon.setBounds( sOldBounds );
			canvas.setBitmap( null );
			return bitmap;
		}
	}
	
	public static Bitmap createIconBitmapWhenItemIsThemeThirdPartyItem(
			Object mObject ,
			Context context ,
			boolean mIsRecycleBitmapSource//对于Drawable无效
	)
	{
		return createIconBitmapWhenItemIsThemeThirdPartyItem( mObject , context , mIsRecycleBitmapSource , true );//为true表示图片较小的不做拉伸处理
	}
	
	// zhangjin@2015/09/02 ADD START
	public static Bitmap createIconBitmapWhenItemIsThemeThirdPartyItem(
			Object mObject ,
			Context context ,
			boolean mIsRecycleBitmapSource ,//对于Drawable无效
			boolean isIconCenter )
	{
		return createIconBitmapWhenItemIsThemeThirdPartyItem( mObject , context , mIsRecycleBitmapSource , isIconCenter , true );
	}
	
	// zhangjin@2015/09/02 ADD END
	public static Bitmap createIconBitmapWhenItemIsThemeThirdPartyItem(
			Object mObject ,
			Context context ,
			boolean mIsRecycleBitmapSource ,//对于Drawable无效
			boolean isIconCenter ,
			boolean isBgRandom//背托几个图片是否随机获取
	)
	{
		//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "cyk_bug : c_0003400" , " Utilities createIconBitmapWhenItemIsThemeThirdPartyItem " );
		}
		//cheyingkun add end
		//cheyingkun add start	//解决“和兴一部桑飞项目，刷机第一次开机默认配置文件夹中图标缺失”的问题。【c_0003400】
		if( sIconWidth == -1 )
		{
			initStatics( context );
		}
		//cheyingkun add end
		if( !( ( mObject instanceof Bitmap ) || ( mObject instanceof Drawable ) ) )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "createIconBitmapWhenItemIsThemeThirdPartyItem" , "source error" );
			return null;
		}
		Bitmap ret = null;
		//生成icon
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{//按桌面图片的大小生成原始图片
			if( mObject instanceof Bitmap )
			{
				ret = createIconBitmap( (Bitmap)mObject , context , sIconWidth , sIconHeight , sIconTextureWidth , sIconTextureHeight , isIconCenter , mIsRecycleBitmapSource );
			}
			else
			{
				ret = createIconBitmap( (Drawable)mObject , context , sIconWidth , sIconHeight , sIconTextureWidth , sIconTextureHeight , isIconCenter );
			}
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			//1、生成原图一半大小的图A
			//2、生成原图两倍大小的图B
			//3、对进行模糊半径为36的高斯模糊，生成图C
			//4、将A居中画到C上，生成图D
			//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			{
				Log.d( "cyk_bug : c_0003400" , " Utilities createIconBitmapWhenItemIsThemeThirdPartyItem   BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE  " );
			}
			//cheyingkun add end
			ret = createIconBitmapWhenItemStyle1( mObject , context , mIsRecycleBitmapSource );
		}
		//xiatian add end
		//cheyingkun add start	//为bug c_0003400添加log（开启配置后“switch_enable_debug”生效），以便定位。
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "cyk_bug : c_0003400" , " Utilities createIconBitmapWhenItemIsThemeThirdPartyItem 去除图片icon透明像素 " );
		}
		//cheyingkun add end
		//去除图片icon透明像素
		//cheyingkun start	//是否优先获取高分辨率图标（图标显示清晰）。true为优先获取高分辨率图标，没有高分辨图标则获取低分辨率图标；false为直接获取当前分辨率图标。默认为false。
		//		ret = cutTransparentPixels( ret );//cheyingkun del
		ret = cutTransparentPixels( ret , false );//cheyingkun add
		//cheyingkun end
		//合成新图片[由下至上依次画背板、图标、蒙版、盖板]
		//背板:非系统主题时，从目录"assets/theme/iconbg/"中随机获取icon_0~icon_n；若当前主题没有背板或者为系统主题时，则使用R.drawable.icon_theme_third_party_item_bg
		//蒙版:非系统主题时，从目录"assets/theme/iconbg/"中随机获取mask_0~mask_n；若当前主题没有背板或者为系统主题时，则使用R.drawable.icon_theme_third_party_item_mask
		//盖板:非系统主题时，从目录"assets/theme/iconbg/"中随机获取icon_cover_plate_0~icon_cover_plate_n；若当前主题没有背板或者为系统主题时，则使用R.drawable.icon_theme_third_party_item_cover
		ThemeManager tm = ThemeManager.getInstance();
		if( tm != null )
		{
			boolean isNeedScale = true;
			//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{
				isNeedScale = false;
			}
			//xiatian add end
			ret = combineIcon( context , ret , tm.getIconBg( isBgRandom ) , tm.getIconMask( isBgRandom ) , tm.getIconCover( isBgRandom ) , isNeedScale , 0 , true , false );
		}
		return ret;
	}
	
	//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
	public static Bitmap createIconBitmapWhenItemStyle1(
			Object mObject ,
			Context context ,
			boolean mIsRecycleBitmapSource )
	{
		//1、生成原图一半大小的图A
		//2、生成原图两倍大小的图B
		//3、对B进行模糊半径为36(客户需求)的高斯模糊，生成图C
		//4、将A居中画到C上，生成图D
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE != BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			return null;
		}
		if( !( ( mObject instanceof Bitmap ) || ( mObject instanceof Drawable ) ) )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "createIconBitmapWhen" , StringUtils.concat( "source error" , mObject ) );
			return null;
		}
		Bitmap ret = null;
		Bitmap mBitmap = null;
		int mIconDestWidth = 0;
		int mIconDestHeight = 0;
		if( mObject instanceof Bitmap )
		{
			//需要生成两张bitmap,createIconBitmap方法，用完会回收。
			Bitmap mSourceBitmap = (Bitmap)mObject;
			int sourceWidth = mSourceBitmap.getWidth();
			int sourceHeight = mSourceBitmap.getHeight();
			//生成原图一半大小的图A
			mIconDestWidth = (int)( sIconWidth * LauncherDefaultConfig.ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE );
			mIconDestHeight = (int)( sIconWidth * LauncherDefaultConfig.ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE );
			mBitmap = createIconBitmap( mSourceBitmap , context , mIconDestWidth , mIconDestHeight , mIconDestWidth , mIconDestHeight , true , false );
			//生成原图两倍大小的图B
			mIconDestWidth = 2 * sourceWidth;
			mIconDestHeight = 2 * sourceHeight;
			ret = createIconBitmap( mSourceBitmap , context , mIconDestWidth , mIconDestHeight , mIconDestWidth , mIconDestHeight , false , false );
			if( mIsRecycleBitmapSource && ( mSourceBitmap != mBitmap ) && ( mBitmap != ret ) && mSourceBitmap.isRecycled() == false )
			{
				mSourceBitmap.recycle();
			}
		}
		else
		{
			Drawable mSourceDrawable = (Drawable)mObject;
			int sourceWidth = mSourceDrawable.getIntrinsicWidth();
			int sourceHeight = mSourceDrawable.getIntrinsicHeight();
			//生成原图一半大小的图A
			mIconDestWidth = (int)( sIconWidth * LauncherDefaultConfig.ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE );
			mIconDestHeight = (int)( sIconWidth * LauncherDefaultConfig.ITEM_STYLE_1_THIRD_PARTY_ICON_SCALE );
			mBitmap = createIconBitmap( mSourceDrawable , context , mIconDestWidth , mIconDestHeight , mIconDestWidth , mIconDestHeight , true );
			//生成原图两倍大小的图B
			mIconDestWidth = 2 * sourceWidth;
			mIconDestHeight = 2 * sourceHeight;
			ret = createIconBitmap( mSourceDrawable , context , mIconDestWidth , mIconDestHeight , mIconDestWidth , mIconDestHeight , false );
		}
		//对模糊背景B加一层背板，解决B有透明像素时，效果不好。
		ThemeManager tm = ThemeManager.getInstance();
		if( tm != null )
		{
			Bitmap bg = getItemStyle1ThirdPartyIconBg( context , mIconDestWidth , mIconDestHeight , mIconDestWidth , mIconDestHeight );
			ret = combineIcon( context , ret , bg , null , null , false , 0 , true , false );
		}
		//对B进行模糊半径为36(客户需求)的高斯模糊，生成图C
		//			BlurOptions option = new BlurOptions();
		//			option.radius = 36;
		//			option.captureWallPaper = false;
		//			option.src = new Rect( 0 , 0 , ret.getWidth() , ret.getHeight() );
		//			ret = BlurHelper.fastBlur( ret , option );
		NDK_CooeeBlur.ImageBlur_blurBitMap( ret , 36 );
		//将A居中画到C上，生成图D
		if( tm != null )
		{
			float mIconPaddingBgBottomPersent = (float)LauncherDefaultConfig.getInt( R.integer.config_item_style_1_third_party_icon_padding_bg_bottom_percent ) / 100f;
			ret = combineIcon( context , mBitmap , ret , null , null , false , mIconPaddingBgBottomPersent , true , true );
		}
		return ret;
	}
	
	private static Bitmap getItemStyle1ThirdPartyIconBg(
			Context mContext ,
			int mIconDestWidth ,
			int mIconDestHeight ,
			int mBitmapDestWidth ,
			int mBitmapDestHeight )
	{//(LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE)专用
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE != BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			return null;
		}
		if( mItemStyle1ThirdPartyIconBg == null )
		{
			mItemStyle1ThirdPartyIconBg = createIconBitmap( R.color.config_item_style_1_third_party_icon_bg , mContext , mIconDestWidth , mIconDestHeight , mIconDestWidth , mIconDestHeight );
		}
		return mItemStyle1ThirdPartyIconBg;
	}
	//xiatian add end
	;
	
	public static AdjustBitmapRet adjustBitmapWhenCombineIcon(
			Bitmap mBitmapSource ,
			int mIconDestWidth ,
			int mIconDestHeight ,
			boolean mIsRecycleBitmapSource )
	{
		AdjustBitmapRet ret = new AdjustBitmapRet();
		int sourceWidth = mBitmapSource.getWidth();
		int sourceHeight = mBitmapSource.getHeight();
		if( sourceWidth > mIconDestWidth && sourceHeight > mIconDestHeight )
		{
			ret.mBitmap = Bitmap.createBitmap( mBitmapSource , ( sourceWidth - mIconDestWidth ) / 2 , ( sourceHeight - mIconDestHeight ) / 2 , mIconDestWidth , mIconDestHeight );
			if( mIsRecycleBitmapSource && mBitmapSource != null && !mBitmapSource.isRecycled() )
			{
				mBitmapSource.recycle();
			}
		}
		else if( sourceWidth < mIconDestWidth && sourceHeight < mIconDestHeight )
		{
			ret.mOffsetX = ( mIconDestWidth - sourceWidth ) / 2;
			ret.mOffsetY = ( mIconDestHeight - sourceHeight ) / 2;
			ret.mBitmap = mBitmapSource;
		}
		else
		{
			//cheyingkun add start	//修改桌面默认配置
			ret.mOffsetX = ( mIconDestWidth - sourceWidth ) / 2;
			ret.mOffsetY = ( mIconDestHeight - sourceHeight ) / 2;
			if( ret.mOffsetX < 0 )
			{
				ret.mOffsetX = 0;
			}
			if( ret.mOffsetY < 0 )
			{
				ret.mOffsetY = 0;
			}
			//cheyingkun add end
			ret.mBitmap = mBitmapSource;
		}
		return ret;
	}
	
	public static Bitmap createIconBitmap(
			int mColorId ,//R.color.xxx
			Context context ,
			int mIconDestWidth ,
			int mIconDestHeight ,
			int mBitmapDestWidth ,
			int mBitmapDestHeight )
	{
		synchronized( sCanvas )
		{
			final Bitmap bitmap = Bitmap.createBitmap( mBitmapDestWidth , mBitmapDestHeight , Bitmap.Config.ARGB_8888 );
			final Canvas canvas = sCanvas;
			canvas.setBitmap( bitmap );
			final int left = ( mBitmapDestWidth - mIconDestWidth ) / 2;
			final int top = ( mBitmapDestHeight - mIconDestHeight ) / 2;
			Paint debugPaint = new Paint();
			debugPaint.setColor( context.getResources().getColor( mColorId ) );
			canvas.drawRect( left , top , left + mIconDestWidth , top + mIconDestHeight , debugPaint );
			canvas.setBitmap( null );
			return bitmap;
		}
	}
	
	//xiatian add start	//适配5.1全局搜索（5.1的全局搜索是将AppWidgetHostView加到mSearchDropTargetBar中），5.1以下的全局搜索机制通不过5.1系统的cts。
	/**
	 * Returns a widget with category {@link AppWidgetProviderInfo#WIDGET_CATEGORY_SEARCHBOX}
	 * provided by the same package which is set to be global search activity.
	 * If widgetCategory is not supported, or no such widget is found, returns the first widget
	 * provided by the package.
	 */
	@TargetApi( Build.VERSION_CODES.JELLY_BEAN_MR1 )
	public static AppWidgetProviderInfo getSearchWidgetProvider(
			Context context )
	{
		SearchManager searchManager = (SearchManager)context.getSystemService( Context.SEARCH_SERVICE );
		ComponentName searchComponent = searchManager.getGlobalSearchActivity();
		if( searchComponent == null )
			return null;
		String providerPkg = searchComponent.getPackageName();
		AppWidgetProviderInfo defaultWidgetForSearchPackage = null;
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( context );
		for( AppWidgetProviderInfo info : appWidgetManager.getInstalledProviders() )
		{
			if( info.provider.getPackageName().equals( providerPkg ) )
			{
				if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 )
				{
					if( ( info.widgetCategory & AppWidgetProviderInfo.WIDGET_CATEGORY_SEARCHBOX ) != 0 )
					{
						return info;
					}
					else if( defaultWidgetForSearchPackage == null )
					{
						defaultWidgetForSearchPackage = info;
					}
				}
				else
				{
					return info;
				}
			}
		}
		return defaultWidgetForSearchPackage;
	}
	
	//xiatian add end
	//cheyingkun add start	//phenix1.1稳定版移植酷生活
	public static void mapToList(
			HashMap<ComponentName , Bitmap> source ,
			List key ,
			List listValue )
	{
		Iterator it = source.keySet().iterator();
		while( it.hasNext() )
		{
			Object k = it.next();
			key.add( k );
			listValue.add( source.get( k ) );
		}
	}
	
	//cheyingkun add end
	// zhangjin@2016/05/05 ADD START
	@TargetApi( Build.VERSION_CODES.JELLY_BEAN_MR1 )
	public static boolean isRtl(
			Resources res )
	{
		return ATLEAST_JB_MR1 && ( res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL );
	}
	
	/**
	 * Trims the string, removing all whitespace at the beginning and end of the string.
	 * Non-breaking whitespaces are also removed.
	 */
	public static String trim(
			CharSequence s )
	{
		if( s == null )
		{
			return null;
		}
		// Just strip any sequence of whitespace or java space characters from the beginning and end
		Matcher m = sTrimPattern.matcher( s );
		return m.replaceAll( "$1" );
	}
	
	public static int pxFromDp(
			float size ,
			DisplayMetrics metrics )
	{
		return (int)Math.round( TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP , size , metrics ) );
	}
	// zhangjin@2016/05/05 ADD END
	//zhujieping add start //需求：进入主菜单动画类型。0为android4.4的launcher3进入主菜单动画类型；1为android7.0的launcher3进入主菜单动画类型。默认为0。
	/**
	 * Ensures that a value is within given bounds. Specifically:
	 * If value is less than lowerBound, return lowerBound; else if value is greater than upperBound,
	 * return upperBound; else return value unchanged.
	 */
	public static int boundToRange(
			int value ,
			int lowerBound ,
			int upperBound )
	{
		return Math.max( lowerBound , Math.min( value , upperBound ) );
	}
	
	/**
	 * @see #boundToRange(int, int, int).
	 */
	public static float boundToRange(
			float value ,
			float lowerBound ,
			float upperBound )
	{
		return Math.max( lowerBound , Math.min( value , upperBound ) );
	}
	
	public static int[] getCenterDeltaInScreenSpace(
			View v0 ,
			View v1 ,
			int[] delta )
	{
		int[] sLoc0 = new int[2];
		int[] sLoc1 = new int[2];
		v0.getLocationInWindow( sLoc0 );
		v1.getLocationInWindow( sLoc1 );
		sLoc0[0] += ( v0.getMeasuredWidth() * v0.getScaleX() ) / 2;
		sLoc0[1] += ( v0.getMeasuredHeight() * v0.getScaleY() ) / 2;
		sLoc1[0] += ( v1.getMeasuredWidth() * v1.getScaleX() ) / 2;
		sLoc1[1] += ( v1.getMeasuredHeight() * v1.getScaleY() ) / 2;
		if( delta == null )
		{
			delta = new int[2];
		}
		delta[0] = sLoc1[0] - sLoc0[0];
		delta[1] = sLoc1[1] - sLoc0[1];
		return delta;
	}
	//zhujieping add end
}

class AdjustBitmapRet
{
	
	Bitmap mBitmap = null;
	float mOffsetX = 0;
	float mOffsetY = 0;
}
