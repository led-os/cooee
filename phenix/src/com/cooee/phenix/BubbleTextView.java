/* Copyright (C) 2008 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License. */
package com.cooee.phenix;


import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TextView;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.function.DynamicEntry.DLManager.Constants;
import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.ResourceUtils;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.Functions.DynamicEntry.OperateDynamicMain;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.data.ItemInfo;
import com.cooee.phenix.data.ShortcutInfo;
import com.cooee.phenix.data.VirtualInfo;
import com.cooee.theme.ThemeManager;


/**
 * TextView that draws a bubble behind the text. We cannot use a LineBackgroundSpan
 * because we want to make the bubble taller than the text and TextView's clip is
 * too aggressive.
 */
public class BubbleTextView extends TextView
//
implements IOnThemeChanged//zhujieping add ,换主题不重启
{
	
	public static final float SHADOW_LARGE_RADIUS = 4.0f;
	public static final float SHADOW_SMALL_RADIUS = 1.75f;
	public static final float SHADOW_Y_OFFSET = 2.0f;
	public static final int SHADOW_LARGE_COLOUR = 0xDD000000;
	public static final int SHADOW_SMALL_COLOUR = 0xCC000000;
	static final float PADDING_H = 8.0f;
	static final float PADDING_V = 3.0f;
	private int mPrevAlpha = -1;
	private HolographicOutlineHelper mOutlineHelper;
	private final Canvas mTempCanvas = new Canvas();
	private final Rect mTempRect = new Rect();
	private boolean mDidInvalidateForPressedState;
	private Bitmap mPressedOrFocusedBackground;
	private int mFocusedOutlineColor;
	private int mFocusedGlowColor;
	private int mPressedOutlineColor;
	private int mPressedGlowColor;
	private int mTextColor;
	private boolean mShadowsEnabled = true;
	private boolean mIsTextVisible;
	private boolean mBackgroundSizeChanged;
	private Drawable mBackground;
	private boolean mStayPressed;
	private CheckLongPressHelper mLongPressHelper;
	private Drawable mLoadDrawable = null;//对于运营文件夹中的图标，未下载的应用要添加下载图标 wanghongjian add
	private Drawable mInstallDrawable = null;//对于运营文件夹中的图标，以下载的未安装的应用要添加安装图标 wanghongjian add
	private Drawable mPauseDrawable = null;
	private Drawable mDownloadingDrawable = null;
	private int mOperateIconState = Constants.DL_STATUS_NOTDOWN;//对于运营文件夹中要下载的图标，判断是否下载完毕了 wanghongjian add
	private static Drawable mCompoundDrawablesTop = null;//xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
	private static Paint mPaintToDrawOperateHot;
	private static Drawable mHotBackground;
	private Rect mHotRect = new Rect();
	private final int MAX_ICON_WIDTH = 162;
	private final int MIN_OPERATE_WIDTH = 40;
	// zhangjin@2015/09/09 ADD START
	private static int mTransColor = -1;
	
	// zhangjin@2015/09/09 ADD END\
	public BubbleTextView(
			Context context )
	{
		// zhangjin@2016/05/05 UPD START
		//super( context );
		//init();
		this( context , null , 0 );
		// zhangjin@2016/05/05 UPD END
	}
	
	public BubbleTextView(
			Context context ,
			AttributeSet attrs )
	{
		// zhangjin@2016/05/05 UPD START
		//super( context , attrs );
		//init();
		this( context , attrs , 0 );
		// zhangjin@2016/05/05 UPD END
	}
	
	public BubbleTextView(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		init();
	}
	
	public void onFinishInflate()
	{
		super.onFinishInflate();
		// Ensure we are using the right text size
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		setTextSize( TypedValue.COMPLEX_UNIT_SP , grid.getIconTextSize() );
		setTextColor( getResources().getColor( R.color.workspace_icon_text_color ) );
		//cheyingkun add start	//完善飞利浦图标样式时桌面图标名称左右边距逻辑。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//桌面图标名称的左右间距（不使用“config_icon_title_and_foldericon_title_padding_left”和“config_icon_title_and_foldericon_title_padding_right”）。
			Resources mResources = getResources();
			int mPaddingLeft = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_icon_title_and_foldericon_title_padding_left );
			int mPaddingTop = getPaddingTop();
			int mPaddingRight = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_item_style_1_icon_title_and_foldericon_title_padding_right );
			int mPaddingBottom = getPaddingBottom();
			setPadding( mPaddingLeft , mPaddingTop , mPaddingRight , mPaddingBottom );
		}
		//cheyingkun add end
	}
	
	private void init()
	{
		mLongPressHelper = new CheckLongPressHelper( this );
		mBackground = getBackground();
		mOutlineHelper = HolographicOutlineHelper.obtain( getContext() );
		final Resources res = getContext().getResources();
		mFocusedOutlineColor = mFocusedGlowColor = mPressedOutlineColor = mPressedGlowColor = res.getColor( R.color.outline_color );
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		//		setShadowLayer( SHADOW_LARGE_RADIUS , 0.0f , SHADOW_Y_OFFSET , SHADOW_LARGE_COLOUR );//xiatian del
		initTitleShadow();//xiatian add
		//xiatian end
		initConfig();//gaominghui add	//5.0及其以上手机，实现TextView文字渐隐
	}
	
	public void applyFromShortcutInfo(
			ShortcutInfo info ,
			IconCache iconCache )
	{
		Bitmap b = info.getIcon( iconCache );
		//xiatian add start	//添加“图标上显示‘未读信息’和‘未接来电’提示”的功能。
		UnreadHelper mUnreadHelper = LauncherAppState.getInstance().getUnreadHelper();
		if( mUnreadHelper != null )
		{
			// zhangjin@2015/12/24 ADD START
			b = mUnreadHelper.getTipsBitmap( getContext() , info , b );
			// zhangjin@2015/12/24 ADD END
			b = mUnreadHelper.getBitmapWithNum( getContext() , info , b );
		}
		//xiatian add end
		//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		//xiatian del start
		//		LauncherAppState app = LauncherAppState.getInstance();
		//		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		//		setCompoundDrawables( null , Utilities.createIconDrawable( b ) , null , null );
		//		setCompoundDrawablePadding( (int)( ( grid.folderIconSizePx - grid.iconSizePx ) / 2f ) );
		//xiatian del end
		//xiatian add start
		setIcon( iconCache , b );
		setGapBetweenIconAndText();
		//xiatian add end
		//xiatian end
		setText( info.getTitle() );
		setTag( info );
		//运营文件夹中要下载的图标，若此时数据库中保存的数据已经为下载完成，则要显示安装图标 wanghongjian add
		if( info.getIntent() != null )
		{
			setOperateIconLoadDone( info.getIntent().getIntExtra( OperateDynamicMain.SHOW_OPERATE_ICON_DOWN_OR_INSTALL_KEY , Constants.DL_STATUS_NOTDOWN ) );
		}
	}
	
	public void applyFromApplicationInfo(
			AppInfo info )
	{
		setIcon( null , info.getIconBitmap() );
		setText( info.getTitle() );
		// We don't need to check the info since it's not a ShortcutInfo
		super.setTag( info );
		// Verify high res immediately
	}
	
	// zhangjin@2015/09/01 ADD START
	public void updateIcon(
			ShortcutInfo info ,
			IconCache iconCache )
	{
		Bitmap b = info.getIcon( iconCache );
		setIcon( iconCache , b );
	}
	
	// zhangjin@2015/09/01 ADD END
	@Override
	protected boolean setFrame(
			int left ,
			int top ,
			int right ,
			int bottom )
	{
		if( getLeft() != left || getRight() != right || getTop() != top || getBottom() != bottom )
		{
			mBackgroundSizeChanged = true;
		}
		return super.setFrame( left , top , right , bottom );
	}
	
	@Override
	protected boolean verifyDrawable(
			Drawable who )
	{
		return who == mBackground || super.verifyDrawable( who );
	}
	
	@Override
	public void setTag(
			Object tag )
	{
		if( tag != null && tag instanceof ItemInfo )
		{
			LauncherModel.checkItemInfo( (ItemInfo)tag );
		}
		super.setTag( tag );
	}
	
	@Override
	protected void drawableStateChanged()
	{
		if( isPressed() )
		{
			// In this case, we have already created the pressed outline on ACTION_DOWN,
			// so we just need to do an invalidate to trigger draw
			if( !mDidInvalidateForPressedState )
			{
				//cheyingkun add start	//按键选中图标时，图标是否高亮。true为高亮，false为不高亮。默认true。【c_0004474】
				if( isFocused() && !LauncherDefaultConfig.SWITCH_ENABLE_WORKSPACE_ICON_HIGHLIGHT_WHEN_SELECTED )
				{
					mPressedOrFocusedBackground = null;
				}
				//cheyingkun add end
				setCellLayoutPressedOrFocusedIcon();
			}
		}
		else
		{
			//xiatian add start	//fix bug：解决“点击底边栏一个图标后并快速滑动手指，该图标会被拖起来”的问题。【i_0010270】
			//【问题原因】图标被按下时，延时启动了一个Runnable，Runnable到时之前没被取消的话，就会触发长按消息。
			//【解决方案】手指划出图标区域后，取消该Runnable
			cancelLongPress();
			//xiatian add end
			// Otherwise, either clear the pressed/focused background, or create a background
			// for the focused state
			final boolean backgroundEmptyBefore = mPressedOrFocusedBackground == null;
			if( !mStayPressed )
			{
				mPressedOrFocusedBackground = null;
			}
			if( isFocused() )
			{
				//cheyingkun add start	//按键选中图标时，图标是否高亮。true为高亮，false为不高亮。默认true。【c_0004474】
				if( !LauncherDefaultConfig.SWITCH_ENABLE_WORKSPACE_ICON_HIGHLIGHT_WHEN_SELECTED )
				{
					mPressedOrFocusedBackground = null;
				}
				else
				//cheyingkun add end
				{
					if( getLayout() == null )
					{
						// In some cases, we get focus before we have been layed out. Set the
						// background to null so that it will get created when the view is drawn.
						mPressedOrFocusedBackground = null;
					}
					else
					{
						mPressedOrFocusedBackground = createGlowingOutline( mTempCanvas , mFocusedGlowColor , mFocusedOutlineColor );
					}
					mStayPressed = false;
					setCellLayoutPressedOrFocusedIcon();
				}
			}
			final boolean backgroundEmptyNow = mPressedOrFocusedBackground == null;
			if( !backgroundEmptyBefore && backgroundEmptyNow )
			{
				setCellLayoutPressedOrFocusedIcon();
			}
		}
		Drawable d = mBackground;
		if( d != null && d.isStateful() )
		{
			d.setState( getDrawableState() );
		}
		super.drawableStateChanged();
	}
	
	/**
	 * Draw this BubbleTextView into the given Canvas.
	 *
	 * @param destCanvas the canvas to draw on
	 * @param padding the horizontal and vertical padding to use when drawing
	 */
	private void drawWithPadding(
			Canvas destCanvas ,
			int padding )
	{
		final Rect clipRect = mTempRect;//clipRect为图标Pressed或Focused时，边缘模糊效果区域
		getDrawingRect( clipRect );
		int mIconHeight = 0;
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			mIconHeight = getExtendedPaddingTop();
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			mIconHeight = getHeight();
		}
		//xiatian add end
		clipRect.bottom = mIconHeight - (int)BubbleTextView.PADDING_V + getLayout().getLineTop( 0 );
		// Draw the View into the bitmap.
		// The translate of scrollX and scrollY is necessary when drawing TextViews, because
		// they set scrollX and scrollY to large values to achieve centered text
		destCanvas.save();
		destCanvas.scale( getScaleX() , getScaleY() , ( getWidth() + padding ) / 2 , ( getHeight() + padding ) / 2 );
		destCanvas.translate( -getScrollX() + padding / 2 , -getScrollY() + padding / 2 );
		destCanvas.clipRect( clipRect , Op.REPLACE );
		draw( destCanvas );
		destCanvas.restore();
	}
	
	/**
	 * Returns a new bitmap to be used as the object outline, e.g. to visualize the drop location.
	 * Responsibility for the bitmap is transferred to the caller.
	 */
	private Bitmap createGlowingOutline(
			Canvas canvas ,
			int outlineColor ,
			int glowColor )
	{
		final int padding = mOutlineHelper.mMaxOuterBlurRadius;
		final Bitmap b = Bitmap.createBitmap( getWidth() + padding , getHeight() + padding , Bitmap.Config.ARGB_8888 );
		canvas.setBitmap( b );
		drawWithPadding( canvas , padding );
		mOutlineHelper.applyExtraThickExpensiveOutlineWithBlur( b , canvas , glowColor , outlineColor );
		canvas.setBitmap( null );
		return b;
	}
	
	@Override
	public boolean onTouchEvent(
			MotionEvent event )
	{
		// Call the superclass onTouchEvent first, because sometimes it changes the state to
		// isPressed() on an ACTION_UP
		boolean result = super.onTouchEvent( event );
		switch( event.getAction() )
		{
			case MotionEvent.ACTION_DOWN:
				// So that the pressed outline is visible immediately when isPressed() is true,
				// we pre-create it on ACTION_DOWN (it takes a small but perceptible amount of time
				// to create it)
				if( mPressedOrFocusedBackground == null )
				{
					// huwenhao@2015/05/22 ADD START
					//比较耗时，且发生在触摸过程中，待优化
					mPressedOrFocusedBackground = createGlowingOutline( mTempCanvas , mPressedGlowColor , mPressedOutlineColor );
					// huwenhao@2015/05/22 ADD END
				}
				//cheyingkun add start	//按键选中图标时，图标是否高亮。true为高亮，false为不高亮。默认true。【c_0004474】
				if( isFocused() && !LauncherDefaultConfig.SWITCH_ENABLE_WORKSPACE_ICON_HIGHLIGHT_WHEN_SELECTED )
				{
					mPressedOrFocusedBackground = null;
				}
				//cheyingkun add end
				// Invalidate so the pressed state is visible, or set a flag so we know that we
				// have to call invalidate as soon as the state is "pressed"
				if( isPressed() )
				{
					mDidInvalidateForPressedState = true;
					setCellLayoutPressedOrFocusedIcon();
				}
				else
				{
					mDidInvalidateForPressedState = false;
				}
				//xiatian add start	//fix bug：解决“智能分类文件夹中的运营出来的所有推荐应用的图标和所有‘更多应用’图标（智能分类文件夹和非智能分类文件夹中）会响应长按事件”的问题。
				//【问题原因】图标被按下时，View.java和BubbleTextView.java分别延时启动了一个Runnable，Runnable到时之前没被取消的话，就会触发长按消息。
				//【解决方案】智能分类文件夹中的运营出来的所有推荐应用的图标和所有‘更多应用’图标（智能分类文件夹和非智能分类文件夹中）,不启动这两个Runnable
				Object mTag = getTag();
				if( mTag instanceof ShortcutInfo )
				{
					ShortcutInfo mShortcutInfo = (ShortcutInfo)mTag;
					if( mShortcutInfo.isOperateVirtualItem() )
					{
						//【备注 - 1】
						//	1、此处不能调用mLongPressHelper.postCheckForLongPress()，确保不启动BubbleTextView.java中的Runnable，即mLongPressHelper中的CheckLongPressHelper。
						//	2、并且同时要确保停止掉View.java中的Runnable，即CheckLongPressHelper
						//【原因】
						//	由于super.onTouchEvent的ACTION_DOWN中注册View.java中的CheckLongPressHelper，若不在run之前使其停止，依然会上发performLongClick()消息，导致该view相应长按事件。						
						//【备注 - 2】
						//	要对该view设置setLongClickable(false),来确保不响应View.java中的CheckLongPressHelper。
						//【原因】
						//	若view是isLongClickable()为false的状态，则会导致该view在super.onTouchEvent的ACTION_DOWN中不会注册View.java中的CheckLongPressHelper。
						//【备注 - 3】
						//	此处不能调用super.cancelLongPress()来停止View.java中的CheckLongPressHelper。
						//【原因】
						//	1、若调用super.cancelLongPress()会同时停止View.java中的CheckLongPressHelper和CheckForTap。
						//	2、虽然取消CheckLongPressHelper会导致系统不上发onLongClick消息，这个情况是我们需要的。
						//	3、但是取消CheckForTap会导致系统不上发onClick消息，这个情况不是我们需要的。
						break;
					}
				}
				//xiatian add end
				mLongPressHelper.postCheckForLongPress();
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				// If we've touched down and up on an item, and it's still not "pressed", then
				// destroy the pressed outline
				if( !isPressed() )
				{
					mPressedOrFocusedBackground = null;
				}
				mLongPressHelper.cancelLongPress();
				break;
		}
		return result;
	}
	
	void setStayPressed(
			boolean stayPressed )
	{
		mStayPressed = stayPressed;
		if( !stayPressed )
		{
			mPressedOrFocusedBackground = null;
		}
		//cheyingkun add start	//按键选中图标时，图标是否高亮。true为高亮，false为不高亮。默认true。【c_0004474】
		if( isFocused() && !LauncherDefaultConfig.SWITCH_ENABLE_WORKSPACE_ICON_HIGHLIGHT_WHEN_SELECTED )
		{
			mPressedOrFocusedBackground = null;
		}
		//cheyingkun add end
		setCellLayoutPressedOrFocusedIcon();
	}
	
	void setCellLayoutPressedOrFocusedIcon()
	{
		if( getParent() instanceof ShortcutAndWidgetContainer )
		{
			ShortcutAndWidgetContainer parent = (ShortcutAndWidgetContainer)getParent();
			if( parent != null )
			{
				CellLayout layout = (CellLayout)parent.getParent();
				layout.setPressedOrFocusedIcon( ( mPressedOrFocusedBackground != null ) ? this : null );
			}
		}
		// zhangjin@2016/05/12 ADD START
		ViewParent parent = getParent();
		if( parent != null && parent.getParent() instanceof BubbleTextShadowHandler )
		{
			( (BubbleTextShadowHandler)parent.getParent() ).setPressedIcon( this , mPressedOrFocusedBackground );
		}
		// zhangjin@2016/05/12 ADD END
	}
	
	void clearPressedOrFocusedBackground()
	{
		mPressedOrFocusedBackground = null;
		setCellLayoutPressedOrFocusedIcon();
	}
	
	Bitmap getPressedOrFocusedBackground()
	{
		return mPressedOrFocusedBackground;
	}
	
	int getPressedOrFocusedBackgroundPadding()
	{
		return mOutlineHelper.mMaxOuterBlurRadius / 2;
	}
	
	@Override
	protected void onSizeChanged(
			int w ,
			int h ,
			int oldw ,
			int oldh )
	{
		super.onSizeChanged( w , h , oldw , oldh );
		if( this.getTag() instanceof ShortcutInfo )
		{
			ShortcutInfo shortcutInfo = (ShortcutInfo)this.getTag();
			if( ( shortcutInfo.isOperateVirtualItem() || shortcutInfo.isOperateIconItem() ) && shortcutInfo.getIntent().getAction() == OperateDynamicMain.OPERATE_DYNAMIC_FOLDER )
			{
				ThemeManager mThemeManager = ThemeManager.getInstance();
				if( mLoadDrawable == null )
				{
					//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
					//					mLoadDrawable = getContext().getResources().getDrawable( R.drawable.operate_folder_load_icon );//xiatian del
					//xiatian add start
					if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
					{
						//xiatian add start	//需求：运营文件夹中的下载提示和安装提示图标，跟着主题走。
						if( mThemeManager != null && ( mThemeManager.currentThemeIsSystemTheme() == false ) )
						{
							mLoadDrawable = mThemeManager.getDrawableFromResource( "operate_folder_load_icon" , R.drawable.operate_folder_load_icon );
						}
						else
						//xiatian add end
						{
							mLoadDrawable = getContext().getResources().getDrawable( R.drawable.operate_folder_load_icon );
						}
					}
					else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
					{
						int operate_folder_load_icon_id = ResourceUtils.getDrawableResourceIdByReflectIfNecessary(
						//
								R.drawable.operate_folder_load_icon , //
								getContext().getResources() , //
								getContext().getPackageName() , //
								"operate_folder_load_icon_item_style_1" //
						//
								);
						if( operate_folder_load_icon_id <= 0 )
						{
							operate_folder_load_icon_id = R.drawable.operate_folder_load_icon;
						}
						mLoadDrawable = getContext().getResources().getDrawable( operate_folder_load_icon_id );
						//						Utilities.resizeIconDrawable( mLoadDrawable );
						mLoadDrawable.setBounds( 0 , 0 , Utilities.sIconTextureWidth , mLoadDrawable.getIntrinsicHeight() );
					}
					//xiatian add end
					//xiatian end
				}
				if( mInstallDrawable == null )
				{
					//xiatian start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
					//					mInstallDrawable = getContext().getResources().getDrawable( R.drawable.operate_folder_install_icon );//xiatian del
					//xiatian add start
					if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
					{
						//xiatian add start	//需求：运营文件夹中的下载提示和安装提示图标，跟着主题走。
						if( mThemeManager != null && ( mThemeManager.currentThemeIsSystemTheme() == false ) )
						{
							mInstallDrawable = mThemeManager.getDrawableFromResource( "operate_folder_install_icon" , R.drawable.operate_folder_install_icon );
						}
						else
						//xiatian add end
						{
							mInstallDrawable = getContext().getResources().getDrawable( R.drawable.operate_folder_install_icon );
						}
					}
					else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
					{
						int operate_folder_install_icon_id = ResourceUtils.getDrawableResourceIdByReflectIfNecessary(
						//
								R.drawable.operate_folder_install_icon , //
								getContext().getResources() , //
								getContext().getPackageName() , //
								"operate_folder_install_icon_item_style_1" //
						//
								);
						if( operate_folder_install_icon_id <= 0 )
						{
							operate_folder_install_icon_id = R.drawable.operate_folder_install_icon;
						}
						mInstallDrawable = getContext().getResources().getDrawable( operate_folder_install_icon_id );
						//						Utilities.resizeIconDrawable( mInstallDrawable );
						mInstallDrawable.setBounds( 0 , 0 , Utilities.sIconTextureWidth , mInstallDrawable.getIntrinsicHeight() );
					}
					//xiatian add end
					//xiatian end
				}
				if( mPauseDrawable == null )
				{
					if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
					{
						if( mThemeManager != null )
						{
							mPauseDrawable = mThemeManager.getDrawableFromResource( "operate_folder_pause_icon" , R.drawable.operate_folder_pause_icon );
						}
					}
					else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
					{
						int operate_folder_install_icon_id = ResourceUtils.getDrawableResourceIdByReflectIfNecessary(
						//
								R.drawable.operate_folder_pause_icon , //
								getContext().getResources() , //
								getContext().getPackageName() , //
								"operate_folder_pause_icon_item_style_1" //
						//
								);
						if( operate_folder_install_icon_id <= 0 )
						{
							operate_folder_install_icon_id = R.drawable.operate_folder_pause_icon;
						}
						mPauseDrawable = getContext().getResources().getDrawable( operate_folder_install_icon_id );
						//						Utilities.resizeIconDrawable( mPauseDrawable );
						mPauseDrawable.setBounds( 0 , 0 , Utilities.sIconTextureWidth , mPauseDrawable.getIntrinsicHeight() );
					}
					//xiatian add end
					//xiatian end
				}
				if( mDownloadingDrawable == null )
				{
					if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
					{
						if( mThemeManager != null )
						{
							mDownloadingDrawable = mThemeManager.getDrawableFromResource( "operate_folder_downloading" , R.drawable.operate_folder_downloading );
						}
					}
					else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
					{
						int operate_folder_install_icon_id = ResourceUtils.getDrawableResourceIdByReflectIfNecessary(
						//
								R.drawable.operate_folder_downloading , //
								getContext().getResources() , //
								getContext().getPackageName() , //
								"operate_folder_downloading_icon_item_style_1" //
						//
								);
						if( operate_folder_install_icon_id <= 0 )
						{
							operate_folder_install_icon_id = R.drawable.operate_folder_downloading;
						}
						mDownloadingDrawable = getContext().getResources().getDrawable( operate_folder_install_icon_id );
						//						Utilities.resizeIconDrawable( mDownloadingDrawable );
						mDownloadingDrawable.setBounds( 0 , 0 , Utilities.sIconTextureWidth , mDownloadingDrawable.getIntrinsicHeight() );
					}
				}
				if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )//xiatian add	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
				{
					mLoadDrawable.setBounds( createOperateIconStateRect( mLoadDrawable.getIntrinsicWidth() ) );
					mInstallDrawable.setBounds( createOperateIconStateRect( mInstallDrawable.getIntrinsicWidth() ) );
					mPauseDrawable.setBounds( createOperateIconStateRect( mPauseDrawable.getIntrinsicWidth() ) );
					mDownloadingDrawable.setBounds( createOperateIconStateRect( mDownloadingDrawable.getIntrinsicWidth() ) );
				}
			}
			createHotBackgroudRect();
		}
	}
	
	/**
	 * 创建运营文件夹安装图标的矩形区域
	 * @return
	 */
	private Rect createOperateIconStateRect(
			int width )
	{
		int iconSize = Utilities.sIconWidth > 0 ? Utilities.sIconWidth : LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getIconWidthSizePx();
		width = width * iconSize / MAX_ICON_WIDTH;
		if( width < MIN_OPERATE_WIDTH )
		{
			width = MIN_OPERATE_WIDTH;
		}
		Rect rect = new Rect();
		Drawable textDrawable = getCompoundDrawables()[1];
		if( textDrawable != null )
		{
			int textDrawableWidth = textDrawable.getIntrinsicWidth();
			int stateDrawableWidth;
			if( textDrawableWidth > width )
			{
				stateDrawableWidth = width;
			}
			else
			{
				stateDrawableWidth = textDrawableWidth;
			}
			int stateDrawableHeight = stateDrawableWidth;
			int left = this.getWidth() - ( this.getWidth() - textDrawableWidth ) / 2 - stateDrawableWidth;
			int right = left + stateDrawableWidth;
			int top = getPaddingTop() + ( textDrawableWidth - stateDrawableHeight );
			int bottom = top + stateDrawableHeight;
			rect.set( left , top , right , bottom );
		}
		return rect;
	}
	
	private void createHotBackgroudRect()
	{
		if( mHotBackground == null )
		{
			mHotBackground = getResources().getDrawable( R.drawable.icon_and_folder_icon_tip_hot_bg_shape );
			mHotBackground.setBounds( new Rect( 0 , 0 , mHotBackground.getIntrinsicWidth() , mHotBackground.getIntrinsicHeight() ) );
		}
		int width = mHotBackground.getIntrinsicWidth();
		int height = mHotBackground.getIntrinsicHeight();
		Drawable textDrawable = getCompoundDrawables()[1];
		int textDrawableWidth = textDrawable.getIntrinsicWidth();
		// gaominghui@2016/12/14 ADD START
		int maxLines = 1;
		if( Build.VERSION.SDK_INT < 16 )
		{
			maxLines = TextViewCompat.getMaxLines( this );
		}
		else
		{
			maxLines = getMaxLines();
		}
		int textHeight = (int)( getPaint().getFontMetrics().bottom - getPaint().getFontMetrics().top ) * maxLines;
		// gaominghui@2016/12/14 ADD END
		int bodyHeight = textHeight + getCompoundDrawablePadding() + textDrawableWidth;
		int translateX = this.getWidth() - ( this.getWidth() - textDrawableWidth ) / 2 - width / 2;
		int translateY = ( this.getHeight() - bodyHeight ) / 2 - height / 2;
		if( translateX + width > getWidth() )
		{
			translateX = getWidth() - width;
		}
		if( translateY < 0 )
		{
			translateY = 0;
		}
		mHotRect.set( translateX , translateY , width + translateX , height + translateY );//因为mHotBackground是static类型，若有多个icon显示hot标，其中一个位置发生变化所有都会变化，因此位置信息私有化
		//		Rect r = new Rect( translateX , translateY , width + translateX , height + translateY );
		//		mHotBackground.setBounds( r );
	}
	
	/**
	 * 绘制运营文件夹的hot图标
	 * @param canvas
	 */
	private void drawOperateHot(
			Canvas canvas )
	{
		if( !Constants.NEW_ICON_DISPLAY_NUM )
		{
			return;
		}
		if( this.getTag() instanceof ShortcutInfo )
		{
			ShortcutInfo shortcutInfo = (ShortcutInfo)this.getTag();
			if( shortcutInfo.getShortcutType() == LauncherSettings.Favorites.SHORTCUT_TYPE_OPERATE_DYNAMIC && shortcutInfo.getIntent().getBooleanExtra( OperateDynamicUtils.DYNAMIC_HOT , false ) )//虚链接不显示下载的图标
			{
				String drawText = "N";
				if( mPaintToDrawOperateHot == null )
				{
					mPaintToDrawOperateHot = new Paint();
					mPaintToDrawOperateHot.setAntiAlias( true );
					mPaintToDrawOperateHot.setColor( Color.WHITE );
					DisplayMetrics metrics = getResources().getDisplayMetrics();
					final float density = metrics.density;
					mPaintToDrawOperateHot.setTextSize( 13 * density );
				}
				canvas.save();
				canvas.translate( mHotRect.left + getScrollX() , mHotRect.top + getScrollY() );
				mHotBackground.draw( canvas );
				int textWidth = (int)mPaintToDrawOperateHot.measureText( drawText , 0 , drawText.length() );
				int x = (int)( ( mHotBackground.getIntrinsicWidth() - textWidth ) / 2.0f );
				FontMetrics fm = mPaintToDrawOperateHot.getFontMetrics();
				int y = (int)( ( mHotBackground.getIntrinsicHeight() - ( fm.descent - fm.ascent ) ) / 2.0f - fm.ascent );
				canvas.drawText( drawText , x , y , mPaintToDrawOperateHot );
				canvas.restore();
			}
		}
	}
	
	/**
	 * 绘制运营文件夹的图标，为下载或者安装
	 * @param canvas
	 */
	private void drawOperateDrawable(
			Canvas canvas )
	{
		if( this.getTag() instanceof ShortcutInfo )
		{
			ShortcutInfo shortcutInfo = (ShortcutInfo)this.getTag();
			if( ( shortcutInfo.isOperateVirtualItem() || shortcutInfo.isOperateIconItem() ) && shortcutInfo.getIntent().getAction() == OperateDynamicMain.OPERATE_DYNAMIC_FOLDER )//虚链接不显示下载的图标
			{
				Drawable drawable = null;
				if( mOperateIconState == Constants.DL_STATUS_SUCCESS )//如果已经下载完成，则绘制安装图标
				{
					drawable = mInstallDrawable;
				}
				else if( mOperateIconState == Constants.DL_STATUS_PAUSE || mOperateIconState == Constants.DL_STATUS_FAIL )
				{
					drawable = mPauseDrawable;
				}
				else if( mOperateIconState == Constants.DL_STATUS_ING )
				{
					drawable = mDownloadingDrawable;
				}
				else
				{
					drawable = mLoadDrawable;
				}
				if( drawable != null )
				{
					final int scrollX = getScrollX();
					final int scrollY = getScrollY();
					if( ( scrollX | scrollY ) == 0 )
					{
						drawable.draw( canvas );
					}
					else
					{
						canvas.translate( scrollX , scrollY );
						drawable.draw( canvas );
						canvas.translate( -scrollX , -scrollY );
					}
				}
			}
			drawOperateHot( canvas );
		}
	}
	
	@Override
	public void draw(
			Canvas canvas )
	{
		if( !mShadowsEnabled )
		{
			super.draw( canvas );
			drawOperateDrawable( canvas );
			return;
		}
		final Drawable background = mBackground;
		if( background != null )
		{
			final int scrollX = getScrollX();
			final int scrollY = getScrollY();
			if( mBackgroundSizeChanged )
			{
				background.setBounds( 0 , 0 , getRight() - getLeft() , getBottom() - getTop() );
				mBackgroundSizeChanged = false;
			}
			if( ( scrollX | scrollY ) == 0 )
			{
				background.draw( canvas );
			}
			else
			{
				canvas.translate( scrollX , scrollY );
				background.draw( canvas );
				canvas.translate( -scrollX , -scrollY );
			}
		}
		// If text is transparent, don't draw any shadow
		// zhangjin@2015/09/09 UPD START
		//if( getCurrentTextColor() == getResources().getColor( android.R.color.transparent ) )
		if( getCurrentTextColor() == getTransColor() )
		// zhangjin@2015/09/09 UPD END
		{
			getPaint().clearShadowLayer();
			super.draw( canvas );
			drawOperateDrawable( canvas );
			return;
		}
		// We enhance the shadow by drawing the shadow twice
		//xiatian add //【备注】这段代码的作用，是在该view处于state_focused=true的时候，在文字显示的区域画背景
		//<初始化的时候已经有一层阴影,这一段我们现在没用这个需求，OI也去掉了> hongqingquan@2015-04-09 modify begin
		//		getPaint().setShadowLayer( SHADOW_LARGE_RADIUS , 0.0f , SHADOW_Y_OFFSET , SHADOW_LARGE_COLOUR );
		//		super.draw( canvas );
		//		canvas.save( Canvas.CLIP_SAVE_FLAG );
		//		canvas.clipRect( getScrollX() , getScrollY() + getExtendedPaddingTop() , getScrollX() + getWidth() , getScrollY() + getHeight() , Region.Op.INTERSECT );
		//		getPaint().setShadowLayer( SHADOW_SMALL_RADIUS , 0.0f , 0.0f , SHADOW_SMALL_COLOUR );
		//		super.draw( canvas );
		//		canvas.restore();
		super.draw( canvas );
		//<初始化的时候已经有一层阴影,这一段我们现在没用这个需求，OI也去掉了> hongqingquan@2015-04-09 modify end
		drawOperateDrawable( canvas );
	}
	
	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		if( mBackground != null )
			mBackground.setCallback( this );
	}
	
	@Override
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		if( mBackground != null )
			mBackground.setCallback( null );
	}
	
	@Override
	public void setTextColor(
			int color )
	{
		mTextColor = color;
		super.setTextColor( color );
	}
	
	public void setShadowsEnabled(
			boolean enabled )
	{
		mShadowsEnabled = enabled;
		getPaint().clearShadowLayer();
		invalidate();
	}
	
	public void setTextVisibility(
			boolean visible )
	{
		Resources res = getResources();
		if( visible )
		{
			super.setTextColor( mTextColor );
		}
		else
		{
			super.setTextColor( res.getColor( android.R.color.transparent ) );
		}
		mIsTextVisible = visible;
	}
	
	public boolean isTextVisible()
	{
		return mIsTextVisible;
	}
	
	@Override
	protected boolean onSetAlpha(
			int alpha )
	{
		if( mPrevAlpha != alpha )
		{
			mPrevAlpha = alpha;
			super.onSetAlpha( alpha );
		}
		return true;
	}
	
	@Override
	public void cancelLongPress()
	{
		super.cancelLongPress();
		mLongPressHelper.cancelLongPress();
	}
	
	@Override
	public void setScaleX(
			float scaleX )
	{
		// TODO Auto-generated method stub
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Log.d( "i_0011202" , "zjp this = " + getText() + " setScaleX = " + scaleX );
			if( scaleX != getScaleX() )
			{
				printCallStatck();
			}
		}
		super.setScaleX( scaleX );
	}
	
	private void printCallStatck()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
		{
			Throwable ex = new Throwable();
			StackTraceElement[] stackElements = ex.getStackTrace();
			if( stackElements != null )
			{
				for( int i = 0 ; i < stackElements.length ; i++ )
				{
					StackTraceElement mStackTraceElement = stackElements[i];
					Log.v(
							"printCallStatck" ,
							StringUtils.concat( "zjp " , mStackTraceElement.getFileName() , File.separator , mStackTraceElement.getMethodName() , File.separator , mStackTraceElement.getLineNumber() ) );
				}
			}
		}
	}
	
	public void setOperateIconLoadDone(
			int state )
	{
		this.mOperateIconState = state;
	}
	
	public int getOperateIconLoadDone()
	{
		return mOperateIconState;
	}
	
	//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
	private void setIcon(
			IconCache iconCache ,
			Bitmap mBitmap )
	{
		Drawable top = null;
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			top = Utilities.createIconDrawable( mBitmap );
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			top = getItemStyle1CompoundDrawablesTop();//要设置一个透明的图片（使得CompoundDrawables不为空），否则text的位置（调用setCompoundDrawablePadding）无效。因为text的位置是相对于CompoundDrawables的位置。
			// gaominghui@2016/12/14 ADD START
			//setBackground( Utilities.createIconDrawable( mBitmap ) );
			setBackgroundDrawable( Utilities.createIconDrawable( mBitmap ) );
			// gaominghui@2016/12/14 ADD END
		}
		//xiatian add end
		setCompoundDrawables( null , top , null , null );
	}
	
	public void setGapBetweenIconAndText()
	{
		int mGapBetweenIconAndText = 0;
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。	
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			//cheyingkun add start	//自定义桌面布局
			if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
			{//自定义布局开关
				if( grid.isCustomLayoutNormalIcon() )
				{//小图标
					mGapBetweenIconAndText = grid.getDefaultGapBetweenIconAndText();
				}
				else
				{//大图标
					mGapBetweenIconAndText = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.config_icon_padding_gop_text_and_icon_big_icon );
				}
			}
			else
			//cheyingkun add end
			{
				mGapBetweenIconAndText = (int)( ( grid.getFolderIconHeightSizePx() - grid.getIconHeightSizePx() ) / 2f )//
						+ grid.getDefaultGapBetweenIconAndText();//cheyingkun add	//默认图标样式下,添加图标和文字之间的间距配置(桌面图标和名称的间距)【c_0004390】
			}
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			mGapBetweenIconAndText = getGapBetweenIconAndTextBySystemFontSize();//调整文字显示的位置//待修改
		}
		//xiatian add end
		setCompoundDrawablePadding( mGapBetweenIconAndText );
	}
	
	private Drawable getItemStyle1CompoundDrawablesTop()
	{//(LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE)专用
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE != BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			return null;
		}
		if( mCompoundDrawablesTop == null )
		{
			Bitmap mBitmap = Utilities.createIconBitmap(
					R.color.config_item_style_1_temp_icon ,
					getContext() ,
					Utilities.sIconWidth ,
					Utilities.sIconHeight ,
					Utilities.sIconTextureWidth ,
					Utilities.sIconTextureHeight );
			mCompoundDrawablesTop = Utilities.createIconDrawable( mBitmap );
		}
		return mCompoundDrawablesTop;
	}
	
	public Drawable getIcon()
	{
		Drawable ret = null;
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			ret = getCompoundDrawables()[1];
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			ret = getBackground();
		}
		//xiatian add end
		return ret;
	}
	
	public Drawable getIconForPerview()
	{
		Drawable ret = null;
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			ret = getCompoundDrawables()[1];
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			ret = getBackground();
		}
		//xiatian add end
		return ret;
	}
	
	public int getIconHeight()
	{
		int ret = 0;
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			ret = getCompoundDrawables()[1].getIntrinsicHeight();
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			ret = getHeight();
		}
		//xiatian add end
		return ret;
	}
	
	public int getIconWidth()
	{
		int ret = 0;
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
		{
			ret = getCompoundDrawables()[1].getIntrinsicWidth();
		}
		else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{
			ret = getWidth();
		}
		//xiatian add end
		return ret;
	}
	
	private void initTitleShadow()
	{
		//文字阴影
		if( LauncherDefaultConfig.SWITCH_ENABLE_TITLE_SHADOW )
		{
			setShadowLayer( SHADOW_LARGE_RADIUS , 0.0f , SHADOW_Y_OFFSET , SHADOW_LARGE_COLOUR );
		}
		else
		{
			getPaint().clearShadowLayer();
		}
	}
	
	//xiatian add end
	@Override
	public int getPaddingTop()
	{
		//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
		if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
		{//文件夹预览图（一个图标覆盖到另一个图标或者文件夹上会生成文件夹预览图）的图标在绘制时的中心点的y坐标
			return 0;
		}
		//xiatian add end
		return super.getPaddingTop();
	}
	
	public boolean isShowTextInCellLayout(
			boolean mIsHotseatCellLayout )
	{
		boolean ret = true;
		if( mIsHotseatCellLayout )
		{
			//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_NORMAL )
			{
				//xiatian add start	//底边栏图标是否显示名称。true为显示名称；false为不显示。默认为false。
				if( LauncherDefaultConfig.SWITCH_ENABLE_HOTSEAT_ITEM_SHOW_TITLE )
				{//yangmengchao add  start     //添加配置项“switch_enable_hotseat_allapps_button_show_title_when_hotseat_item_show_title”，底边栏显示图标名称前提下，主菜单入口图标是否显示名称。true为显示名称；false为不显示。默认为true。
					if( getId() == R.id.all_apps_button )
					{
						if( LauncherDefaultConfig.SWITCH_ENABLE_HOTSEAT_ALLAPPS_BUTTON_SHOW_TITLE_WHEN_HOTSEAT_ITEM_SHOW_TITLE )
						{
							ret = true;
						}
						else
						{
							ret = false;
						}
					}
					else
					{
						ret = true;
					}
					//yangmengchao add end     //添加配置项“switch_enable_hotseat_allapps_button_show_title_when_hotseat_item_show_title”，底边栏显示图标名称前提下，主菜单入口图标是否显示名称。true为显示名称；false为不显示。默认为true。
				}
				else
				//xiatian add end
				{
					ret = false;
				}
			}
			else if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE )
			{
				ret = true;
			}
		}
		//xiatian add end
		return ret;
	}
	
	// zhangjin@2015/09/01 ADD START
	@Override
	public void setPadding(
			int left ,
			int top ,
			int right ,
			int bottom )
	{
		// TODO Auto-generated method stub
		if( left != this.getPaddingLeft() || right != this.getPaddingRight() || top != this.getPaddingTop() || bottom != this.getPaddingBottom() )
		{
			super.setPadding( left , top , right , bottom );
		}
	}
	
	// zhangjin@2015/09/01 ADD END
	// zhangjin@2015/09/09 ADD START
	public int getTransColor()
	{
		if( mTransColor == -1 )
		{
			mTransColor = getResources().getColor( android.R.color.transparent );
		}
		return mTransColor;
	}
	
	// zhangjin@2015/09/09 ADD END
	//cheyingkun add start	//解决“改变系统字体后，飞利浦图标样式下，文件夹和图标名称偏移”的问题。【c_0003610】
	/**
	 * 根据系统自己大小获取图片文字间距的比例(飞利浦图标样式中)
	 * @return
	 */
	private int getGapBetweenIconAndTextBySystemFontSize()
	{
		float fontSize = getResources().getConfiguration().fontScale;
		String gapBetweenIconAndText = "1";
		if( fontSize == LauncherDefaultConfig.SYSTEM_FONT_SIZE_SMALL )
		{
			gapBetweenIconAndText = LauncherDefaultConfig.getString( R.string.config_item_style_1_gap_between_icon_and_text_small );
		}
		else if( fontSize == LauncherDefaultConfig.SYSTEM_FONT_SIZE_NORMAL )
		{
			gapBetweenIconAndText = LauncherDefaultConfig.getString( R.string.config_item_style_1_gap_between_icon_and_text_normal );
		}
		else if( fontSize == LauncherDefaultConfig.SYSTEM_FONT_SIZE_LARGE )
		{
			gapBetweenIconAndText = LauncherDefaultConfig.getString( R.string.config_item_style_1_gap_between_icon_and_text_large );
		}
		else if( fontSize == LauncherDefaultConfig.SYSTEM_FONT_SIZE_HUGE )
		{
			gapBetweenIconAndText = LauncherDefaultConfig.getString( R.string.config_item_style_1_gap_between_icon_and_text_huge );
		}
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		return (int)( grid.getSignleViewAvailableHeightPx() * -Float.valueOf( gapBetweenIconAndText ) );
	}
	//cheyingkun add end
	;
	
	//gaominghui add start	//5.0及其以上手机，实现TextView文字渐隐
	private Shader mShaderWithoutShadow = null;
	private Shader mShaderWithShadow = null;
	private float mOneTextWidth = -1;
	private int cellWidthPx = -1;//每个图标格子的宽度
	private static Paint mPaintToDrawTitleShadow;
	
	private void initConfig()
	{
		if(
		//
		( LauncherDefaultConfig.SWITCH_ENABLE_SHOW_ICON_TITLE_FADE_OUT )
		//
		&& ( Build.VERSION.SDK_INT >= 21 )
		//
		)
		{
			LauncherAppState app = LauncherAppState.getInstance();
			DynamicGrid mDynamicGrid = app.getDynamicGrid();
			if( mDynamicGrid != null )
			{
				DeviceProfile grid = mDynamicGrid.getDeviceProfile();
				if( grid != null )
				{
					cellWidthPx = grid.getCellWidthPx();
				}
			}
		}
	}
	
	private boolean isTitleNeedToShowShadow()
	{
		boolean ret = false;
		Paint mCurPaint = getPaint();
		if(
		//
		( getEllipsize() == null )
		//
		&& ( getLineCount() == 1 )
		//
		&& ( mCurPaint.measureText( getText().toString() ) > cellWidthPx )
		//
		)
		{
			ret = true;
		}
		return ret;
	}
	//gaominghui add end
	;
	
	/**
	 *
	 * @see android.widget.TextView#onDraw(android.graphics.Canvas)
	 * @auther gaominghui  2015年12月30日
	 */
	@Override
	protected void onDraw(
			Canvas canvas )
	{
		//gaominghui add start	//5.0及其以上手机，实现TextView文字渐隐
		if(
		//
		( LauncherDefaultConfig.SWITCH_ENABLE_SHOW_ICON_TITLE_FADE_OUT )
		//
		&& ( Build.VERSION.SDK_INT >= 21 )
		//
		&& isTitleNeedToShowShadow()
		//
		)
		{
			Paint mCurPaint = getPaint();
			setGravity( Gravity.LEFT );
			if( mOneTextWidth == -1 )
			{
				mOneTextWidth = mCurPaint.measureText( "a" );
			}
			//文字阴影
			if( !LauncherDefaultConfig.SWITCH_ENABLE_TITLE_SHADOW )
			{
				if( mShaderWithoutShadow == null )
				{
					mShaderWithoutShadow = new LinearGradient( cellWidthPx - mOneTextWidth , 0 , getWidth() , 0 , getCurrentTextColor() , Color.TRANSPARENT , TileMode.CLAMP );
				}
				mCurPaint.setShader( mShaderWithoutShadow );
			}
			else
			{
				mCurPaint.clearShadowLayer();
				canvas.save();
				if( mPaintToDrawTitleShadow == null )
				{
					mPaintToDrawTitleShadow = new Paint();
					mPaintToDrawTitleShadow.setTextSize( mCurPaint.getTextSize() );
					mPaintToDrawTitleShadow.setAntiAlias( true );
					mPaintToDrawTitleShadow.setTypeface( mCurPaint.getTypeface() );
					mPaintToDrawTitleShadow.setShadowLayer( SHADOW_LARGE_RADIUS , 0 , SHADOW_Y_OFFSET , SHADOW_LARGE_COLOUR );
				}
				if( mShaderWithShadow == null )
				{
					mShaderWithShadow = new LinearGradient( cellWidthPx - mOneTextWidth * 3 , 0 , getWidth() , 0 , SHADOW_LARGE_COLOUR , Color.TRANSPARENT , TileMode.CLAMP );
				}
				mPaintToDrawTitleShadow.setShader( mShaderWithShadow );
				setTextColor( getCurrentTextColor() );
				canvas.drawText( getText().toString() , 0 , getBaseline() , mPaintToDrawTitleShadow );
				canvas.restore();
				if( mShaderWithoutShadow == null )
				{
					mShaderWithoutShadow = new LinearGradient( cellWidthPx - mOneTextWidth , 0 , getWidth() , 0 , getCurrentTextColor() , Color.TRANSPARENT , TileMode.CLAMP );
				}
				mCurPaint.setShader( mShaderWithoutShadow );
			}
		}
		super.onDraw( canvas );
	}
	
	// zhangjin@2016/05/05 ADD START
	/**
	 * Interface to be implemented by the grand parent to allow click shadow effect.
	 */
	public static interface BubbleTextShadowHandler
	{
		
		void setPressedIcon(
				BubbleTextView icon ,
				Bitmap background );
	}
	
	// zhangjin@2016/05/05 ADD END
	//zhujieping add start
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		if( ( arg0 instanceof IconCache ) == false )
		{
			return;
		}
		if( ( arg1 instanceof Handler ) == false )
		{
			return;
		}
		final ShortcutInfo mShortcutInfo = (ShortcutInfo)getTag();
		if( mShortcutInfo == null )
		{
			return;
		}
		if( mShortcutInfo.getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL )
		{
			//【备注】
			//	1、对于虚图标，其onThemeChanged的作用是：将对应的CacheEntry加入mCache
			//	2、然后在ShortcutInfo.onThemeChanged中直接获取对应CacheEntry，更新ShortcutInfo的参数“mIcon”
			VirtualInfo mVirtualInfo = (VirtualInfo)mShortcutInfo.makeVirtual();
			mVirtualInfo.onThemeChanged( arg0 , arg1 );
		}
		mShortcutInfo.onThemeChanged( (IconCache)arg0 , arg1 );
		Runnable r = new Runnable() {
			
			public void run()
			{
				final Bitmap mBitmap = mShortcutInfo.getIcon();
				if( mBitmap != null )
				{
					setIcon( null , mBitmap );//对原图片的释放统一放到iconcache的onrecyle方法中，在主线程释放造成线程不同步
				}
			}
		};
		( (Handler)arg1 ).post( r );
	}
	//zhujieping add end
}
