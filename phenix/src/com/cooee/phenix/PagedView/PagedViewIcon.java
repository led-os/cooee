package com.cooee.phenix.PagedView;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.phenix.DeviceProfile;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.UnreadHelper;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;


/**
 * An icon on a PagedView, specifically for items in the launcher's paged view (with compound
 * drawables on the top).
 */
public class PagedViewIcon extends FrameLayout
//
implements IOnThemeChanged//zhujieping add，换主题不重启
{
	
	/** A simple callback interface to allow a PagedViewIcon to notify when it has been pressed */
	public static interface PressedCallback
	{
		
		void iconPressed(
				PagedViewIcon icon );
	}
	
	@SuppressWarnings( "unused" )
	private static final String TAG = "PagedViewIcon";
	private static final float PRESS_ALPHA = 0.4f;
	private PagedViewIcon.PressedCallback mPressedCallback;
	private boolean mLockDrawableState = false;
	private Bitmap mIcon;
	// zhangjin@2015/09/09 ADD START
	private static int mTransColor = -1;
	// zhangjin@2015/09/09 ADD END
	private TextView mTextView;
	private CheckBox mCheckBox;
	//zhujieping add start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
	private ImageView mImageView;
	private boolean isUninstallState = false;
	//zhujieping add end
	
	public PagedViewIcon(
			Context context )
	{
		this( context , null );
	}
	
	public PagedViewIcon(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public PagedViewIcon(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
	}
	
	public void onFinishInflate()
	{
		super.onFinishInflate();
		// Ensure we are using the right text size
		mTextView = (TextView)findViewById( R.id.application_icon );
		mCheckBox = (CheckBox)findViewById( R.id.icon_checkbox );
		mImageView = (ImageView)findViewById( R.id.icon_uninstall );//zhujieping add //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
		if(
		//
		LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE != LauncherDefaultConfig.APPLIST_BAR_STYLE_S5
		//
		&& ( LauncherDefaultConfig.CONFIG_APPLIST_BAR_STYLE != LauncherDefaultConfig.APPLIST_BAR_STYLE_S6/* //zhujieping add	//拓展配置项“config_applistbar_style”，添加可配置项3。3为仿S6样式。 */)
		//
		)
		{
			removeView( mCheckBox );
			mCheckBox = null;
			//zhujiepinga dd start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
			removeView( mImageView );
			mImageView = null;
			//zhujieping add end
		}
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		mTextView.setTextSize( TypedValue.COMPLEX_UNIT_SP , grid.getIconTextSize()//
				* LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getAllappsIconScale() );//cheyingkun add	//phenix仿S5效果,主菜单图标缩放比
	}
	
	public void applyFromApplicationInfo(
			AppInfo info ,
			boolean scaleUp ,
			PagedViewIcon.PressedCallback cb )
	{
		mIcon = info.getIconBitmap();
		//xiatian add start	//解决：“主菜单图标上不显示‘未读信息’和‘未接来电’提示”的问题。
		UnreadHelper mUnreadHelper = LauncherAppState.getInstance().getUnreadHelper();
		if( mUnreadHelper != null )
		{
			// zhangjin@2015/12/24 ADD START
			mIcon = mUnreadHelper.getTipsBitmap( getContext() , info , mIcon );
			// zhangjin@2015/12/24 ADD END
			mIcon = mUnreadHelper.getBitmapWithNum( getContext() , info , mIcon );
		}
		//xiatian add end
		mPressedCallback = cb;
		mTextView.setCompoundDrawables( null , Utilities.createIconDrawable( mIcon , LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getAllappsIconScale() ) , null , null );
		//xiatian add start	//图标名称和文件夹名称，是否显示文字阴影。true为显示（详细配置见<style name="WorkspaceIcon">），false为不显示。
		if( LauncherDefaultConfig.SWITCH_ENABLE_TITLE_SHADOW == false )
		{
			mTextView.getPaint().clearShadowLayer();
		}
		//xiatian add end
		setGapBetweenIconAndText();//cheyingkun add	//解决“主菜单列表中应用名称被布局下边界截掉的问题”。【c_0003983】
		mTextView.setText( info.getTitle() );
		setTag( info );
	}
	
	// zhangjin@2015/09/01 ADD START
	public void updateIcon(
			AppInfo info )
	{
		mIcon = info.getIconBitmap();
		mTextView.setCompoundDrawables( null , Utilities.createIconDrawable( mIcon , LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getAllappsIconScale() ) , null , null );
	}
	
	// zhangjin@2015/09/01 ADD END
	public void lockDrawableState()
	{
		mLockDrawableState = true;
	}
	
	public void resetDrawableState()
	{
		mLockDrawableState = false;
		mTextView.post( new Runnable() {
			
			@Override
			public void run()
			{
				mTextView.refreshDrawableState();
			}
		} );
	}
	
	protected void drawableStateChanged()
	{
		super.drawableStateChanged();
		// We keep in the pressed state until resetDrawableState() is called to reset the press
		// feedback
		if( ( mCheckBox != null && mCheckBox.getVisibility() == View.VISIBLE )
				//
				|| isUninstallState )//zhujieping add //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
		{
			return;
		}
		if( isPressed() )
		{
			setAlpha( PRESS_ALPHA );
			if( mPressedCallback != null )
			{
				mPressedCallback.iconPressed( this );
			}
		}
		else if( !mLockDrawableState )
		{
			setAlpha( 1f );
		}
	}
	
	@Override
	public void draw(
			Canvas canvas )
	{
		// If text is transparent, don't draw any shadow
		// zhangjin@2015/09/09 UPD START
		//if( getCurrentTextColor() == getResources().getColor( android.R.color.transparent ) )
		if( mTextView.getCurrentTextColor() == getTransColor() )
		// zhangjin@2015/09/09 UPD END
		{
			mTextView.getPaint().clearShadowLayer();
			super.draw( canvas );
			return;
		}
		// We enhance the shadow by drawing the shadow twice
		//xiatian start	//需求：去除主菜单图标处于选中状态时文字区域的背景。
		//【备注】这段代码的作用，是在该view处于state_focused=true的时候，在文字显示的区域画背景
		//xiatian del start
		//		getPaint().setShadowLayer( BubbleTextView.SHADOW_LARGE_RADIUS , 0.0f , BubbleTextView.SHADOW_Y_OFFSET , BubbleTextView.SHADOW_LARGE_COLOUR );
		//		super.draw( canvas );
		//		canvas.save( Canvas.CLIP_SAVE_FLAG );
		//		canvas.clipRect( getScrollX() , getScrollY() + getExtendedPaddingTop() , getScrollX() + getWidth() , getScrollY() + getHeight() , Region.Op.INTERSECT );
		//		getPaint().setShadowLayer( BubbleTextView.SHADOW_SMALL_RADIUS , 0.0f , 0.0f , BubbleTextView.SHADOW_SMALL_COLOUR );
		//		super.draw( canvas );
		//		canvas.restore();
		//xiatian del end
		super.draw( canvas );//xiatian add
		//xiatian end
	}
	
	// zhangjin@2015/09/01 ADD START
	@Override
	public void setPadding(
			int left ,
			int top ,
			int right ,
			int bottom )
	{
		//Log.i( "PagedViewIcon" , "setPadding!!! mTextView = " + mTextView );
		if( mTextView != null && ( left != mTextView.getPaddingLeft() || right != mTextView.getPaddingRight() || top != mTextView.getPaddingTop() || bottom != mTextView.getPaddingBottom() ) )
		{
			mTextView.setPadding( left , top , right , bottom );
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
	//cheyingkun add start	//解决“主菜单列表中应用名称被布局下边界截掉的问题”。【c_0003983】
	private void setGapBetweenIconAndText()
	{
		int mGapBetweenIconAndText = 0;
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		mGapBetweenIconAndText = (int)( ( grid.getFolderIconHeightSizePx() - grid.getIconHeightSizePx() ) / 2f )//
				+ (int)( grid.getDefaultGapBetweenIconAndText() //cheyingkun add	//默认图标样式下,添加图标和文字之间的间距配置(主菜单图标和名称的间距)【c_0004390】
				* LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getAllappsIconScale() );//cheyingkun add	//phenix仿S5效果,主菜单图标缩放比
		mTextView.setCompoundDrawablePadding( mGapBetweenIconAndText );
	}
	
	//cheyingkun add end
	//zhujieping add start
	public void setCheckboxShow(
			boolean isShow )
	{
		if( mCheckBox != null )
		{
			if( isShow )
				mCheckBox.setVisibility( View.VISIBLE );
			else
				mCheckBox.setVisibility( View.GONE );
			AppInfo appInfo = (AppInfo)getTag();
			if( appInfo.isHideIcon() )
			{
				mCheckBox.setChecked( true );
				mTextView.setAlpha( PRESS_ALPHA );
			}
			else
			{
				mCheckBox.setChecked( false );
				mTextView.setAlpha( 1f );
			}
		}
	}
	
	public void changeCheckedState()
	{
		if( mCheckBox != null )
		{
			if( getTag() instanceof AppInfo )
			{
				AppInfo appInfo = (AppInfo)getTag();
				if( mCheckBox.isChecked() )
				{
					mCheckBox.setChecked( false );
					mTextView.setAlpha( 1 );
					appInfo.setIconHide( getContext() , false );
				}
				else
				{
					mCheckBox.setChecked( true );
					mTextView.setAlpha( PRESS_ALPHA );
					appInfo.setIconHide( getContext() , true );
				}
			}
		}
	}
	
	//zhujieping add end
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		AppInfo info = (AppInfo)getTag();
		mIcon = info.getIconBitmap();
		UnreadHelper mUnreadHelper = LauncherAppState.getInstance().getUnreadHelper();
		if( mUnreadHelper != null )
		{
			mIcon = mUnreadHelper.getTipsBitmap( getContext() , info , mIcon );
			mIcon = mUnreadHelper.getBitmapWithNum( getContext() , info , mIcon );
		}
		post( new Runnable() {
			
			public void run()
			{
				mTextView.setCompoundDrawables( null , Utilities.createIconDrawable( mIcon , LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getAllappsIconScale() ) , null , null );
			}
		} );
	}
	
	//zhujieping add start //更新APPLIST_BAR_STYLE_S6的需求：1、添加“卸载模式”；2、删除“编辑模式”；3、修改“默认排序方式”，由“名称”改为“安装时间”
	public void setUninstallIconShow(
			boolean isShow )
	{
		if( mImageView != null )
		{
			AppInfo appInfo = (AppInfo)getTag();
			if( isShow )
			{
				isUninstallState = true;
			}
			else
			{
				isUninstallState = false;
			}
			if( ( appInfo.getFlags() & AppInfo.DOWNLOADED_FLAG ) != 0 && isShow )
			{
				mImageView.setVisibility( View.VISIBLE );
			}
			else
			{
				mImageView.setVisibility( View.GONE );
			}
		}
	}
	//zhujieping add end
}
