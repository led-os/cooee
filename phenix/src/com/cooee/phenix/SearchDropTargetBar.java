package com.cooee.phenix;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


/* Ths bar will manage the transition between the search bar and the delete drop targets so that each of the individual IconDropTargets don't have to. */
public class SearchDropTargetBar extends FrameLayout implements DragController.DragListener ,
		//
		DragCellLayoutListener //zhujieping add //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
{
	
	private static final int sTransitionInDuration = 200;
	private static final int sTransitionOutDuration = 175;
	private ObjectAnimator mDropTargetBarAnim;
	private ObjectAnimator mSearchBarAnim;
	private static final AccelerateInterpolator sAccelerateInterpolator = new AccelerateInterpolator();
	private boolean mIsSearchBarHidden;
	private View mSearchBar;
	private View mDropTargetBar;
	private ButtonDropTarget mInfoDropTarget;
	private ButtonDropTarget mDeleteDropTarget;
	private int mBarHeight;
	private boolean mDeferOnDragEnd = false;
	private Drawable mPreviousBackground;
	private boolean mEnableDropDownDropTargets;
	private DropTargetListener mDropTargetListener;
	private boolean isOnDrag = false;//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
	
	public SearchDropTargetBar(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
	}
	
	public SearchDropTargetBar(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
	}
	
	public void setup(
			Launcher launcher ,
			DragController dragController )
	{
		dragController.addDragListener( this );
		dragController.addDragListener( mInfoDropTarget );
		dragController.addDragListener( mDeleteDropTarget );
		dragController.addDropTarget( mInfoDropTarget );
		dragController.addDropTarget( mDeleteDropTarget );
		dragController.setFlingToDeleteDropTarget( mDeleteDropTarget );
		mInfoDropTarget.setLauncher( launcher );
		mDeleteDropTarget.setLauncher( launcher );
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		{
			mSearchBar = launcher.getSearchBar();
			if( mEnableDropDownDropTargets )
			{
				mSearchBarAnim = LauncherAnimUtils.ofFloat( mSearchBar , "translationY" , 0 , -mBarHeight );
			}
			else
			{
				mSearchBarAnim = LauncherAnimUtils.ofFloat( mSearchBar , "alpha" , 1f , 0f );
			}
			setupAnimation( mSearchBarAnim , mSearchBar );
		}
	}
	
	private void prepareStartAnimation(
			View v )
	{
		// Enable the hw layers before the animation starts (will be disabled in the onAnimationEnd
		// callback below)
		v.setLayerType( View.LAYER_TYPE_HARDWARE , null );
	}
	
	private void setupAnimation(
			ObjectAnimator anim ,
			final View v )
	{
		anim.setInterpolator( sAccelerateInterpolator );
		anim.setDuration( sTransitionInDuration );
		anim.addListener( new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationEnd(
					Animator animation )
			{
				if( mDropTargetListener != null )
				{
					mDropTargetListener.dropTargetAnimEnd();
				}
				//cheyingkun add start	//解决“禁用谷歌应用回到launcher会黑屏”的问题。（根据log添加保护）【c_0004164】
				if( v == null )
				{
					return;
				}
				//cheyingkun add end
				v.setLayerType( View.LAYER_TYPE_NONE , null );
			}
		} );
	}
	
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		// Get the individual components
		mDropTargetBar = findViewById( R.id.drop_target_bar_id );
		mInfoDropTarget = (ButtonDropTarget)mDropTargetBar.findViewById( R.id.info_target_text );
		mDeleteDropTarget = (ButtonDropTarget)mDropTargetBar.findViewById( R.id.delete_target_text );
		mInfoDropTarget.setSearchDropTargetBar( this );
		mDeleteDropTarget.setSearchDropTargetBar( this );
		mEnableDropDownDropTargets = LauncherDefaultConfig.getBoolean( R.bool.config_useDropTargetDownTransition );
		// Create the various fade animations
		if( mEnableDropDownDropTargets )
		{
			LauncherAppState app = LauncherAppState.getInstance();
			DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
			mBarHeight = grid.getSearchBarSpaceHeightPx();
			mDropTargetBar.setTranslationY( -mBarHeight );
			mDropTargetBarAnim = LauncherAnimUtils.ofFloat( mDropTargetBar , "translationY" , -mBarHeight , 0f );
		}
		else
		{
			mDropTargetBar.setAlpha( 0f );
			mDropTargetBarAnim = LauncherAnimUtils.ofFloat( mDropTargetBar , "alpha" , 0f , 1f );
		}
		setupAnimation( mDropTargetBarAnim , mDropTargetBar );
	}
	
	public void finishAnimations()
	{
		prepareStartAnimation( mDropTargetBar );
		mDropTargetBarAnim.reverse();
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE && mSearchBarAnim != null )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		{
			prepareStartAnimation( mSearchBar );
			mSearchBarAnim.reverse();
		}
	}
	
	// zhujieping@2015/03/25 ADD START
	public void stopSearchBarAnim()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
				&& mSearchBarAnim != null && mSearchBarAnim.isRunning() )
		{
			mSearchBarAnim.cancel();
		}
	}
	
	// zhujieping@2015/03/25 ADD END
	/*
	 * Shows and hides the search bar.
	 */
	public void showSearchBar(
			boolean animated )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		{
			boolean needToCancelOngoingAnimation = mSearchBarAnim.isRunning() && !animated;
			if( !mIsSearchBarHidden && !needToCancelOngoingAnimation )
				return;
			//zhujieping add start //当拖动图标时，搜索栏不显示 //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
			if( isOnDrag )
				return;
			//zhujieping add end
			if( animated )
			{
				prepareStartAnimation( mSearchBar );
				mSearchBarAnim.reverse();
			}
			else
			{
				mSearchBarAnim.cancel();
				if( mEnableDropDownDropTargets )
				{
					mSearchBar.setTranslationY( 0 );
				}
				else
				{
					mSearchBar.setAlpha( 1f );
				}
			}
		}
		mIsSearchBarHidden = false;
	}
	
	public void setSearchBarIfHide(
			boolean hide )
	{
		stopSearchBarAnim();
		mIsSearchBarHidden = hide;
		//zhujieping add start,searchbar的状态应和标志位一致
		if( mSearchBar != null )
		{
			if( hide )
			{
				if( mEnableDropDownDropTargets )
				{
					mSearchBar.setTranslationY( -mBarHeight );
				}
				else
				{
					mSearchBar.setAlpha( 0f );
				}
			}
			else
			{
				if( mEnableDropDownDropTargets )
				{
					mSearchBar.setTranslationY( 0 );
				}
				else
				{
					mSearchBar.setAlpha( 1f );
				}
			}
		}
		//zhujieping add end
	}
	
	public void hideSearchBar(
			boolean animated )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
		{
			boolean needToCancelOngoingAnimation = mSearchBarAnim.isRunning() && !animated;
			if( mIsSearchBarHidden && !needToCancelOngoingAnimation )
				return;
			if( mSearchBar != null )//cheyingkun add	//解决“禁用谷歌应用回到launcher会黑屏”的问题。（根据log添加保护）【c_0004164】
			{
				if( animated )
				{
					prepareStartAnimation( mSearchBar );
					mSearchBarAnim.start();
				}
				else
				{
					mSearchBarAnim.cancel();
					if( mEnableDropDownDropTargets )
					{
						mSearchBar.setTranslationY( -mBarHeight );
					}
					else
					{
						mSearchBar.setAlpha( 0f );
					}
				}
			}
		}
		mIsSearchBarHidden = true;
	}
	
	/*
	 * Gets various transition durations.
	 */
	public int getTransitionInDuration()
	{
		return sTransitionInDuration;
	}
	
	public int getTransitionOutDuration()
	{
		return sTransitionOutDuration;
	}
	
	/*
	 * DragController.DragListener implementation
	 */
	@Override
	public void onDragStart(
			DragSource source ,
			Object info ,
			int dragAction )
	{
		// Animate out the search bar, and animate in the drop target bar
		//xiatian add start	//解决“开关‘switch_enable_search_bar_common_page’为false的前提下，切页后长按可卸载（可删除）图标时，不显示垃圾筐”的问题。【c_0004654】
		//【问题原因】
		//1、由于switch_enable_search_bar_common_page为false时，Workspace.java的updateWorkspaceItemsStateOnEndMovingInNormalMode方法中，会将搜索栏设置到屏幕外
		//2、由于垃圾筐的父view是搜索栏，故而切页后垃圾筐也显示在屏幕外
		//【解决方案】onDragStart时，将搜索栏复位，从而确保垃圾筐位置正常。
		setTranslationX( 0 );
		//xiatian add end
		prepareStartAnimation( mDropTargetBar );
		mDropTargetBarAnim.start();
		if( !mIsSearchBarHidden )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE && mSearchBarAnim != null )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
			{
				prepareStartAnimation( mSearchBar );
				mSearchBarAnim.start();
			}
			bringToFront();//cheyingkun add	//文件夹需求(文件夹内显示卸载框)
		}
		isOnDrag = true;//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
	}
	
	public void deferOnDragEnd()
	{
		mDeferOnDragEnd = true;
	}
	
	// zhujieping@2015/03/27 DEL START，删除无效代码
	//public void onDragEndWithoutSearchbar()
	//{
	//	prepareStartAnimation( mDropTargetBar );
	//	mDropTargetBarAnim.reverse();
	//}
	// zhujieping@2015/03/27 DEL END
	// zhujieping@2015/03/26 ADD START
	public void onDragEnd(
			boolean isSearchBarHidden )
	{
		if( !mDeferOnDragEnd )
		{
			// Restore the search bar, and animate out the drop target bar
			prepareStartAnimation( mDropTargetBar );
			mDropTargetBarAnim.reverse();
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE && mSearchBarAnim != null )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
			{
				if( !isSearchBarHidden )
				{
					prepareStartAnimation( mSearchBar );
					mSearchBarAnim.reverse();
				}
			}
		}
		else
		{
			mDeferOnDragEnd = false;
		}
		isOnDrag = false;//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
	}
	
	// zhujieping@2015/03/26 ADD END
	@Override
	public void onDragEnd()
	{
		if( !mDeferOnDragEnd )
		{
			// Restore the search bar, and animate out the drop target bar
			prepareStartAnimation( mDropTargetBar );
			mDropTargetBarAnim.reverse();
			if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE && mSearchBarAnim != null )//cheyingkun add	//解决“6.0手机关闭桌面搜索、配置全局搜索，桌面显示搜索且界面异常”的问题
			{
				if( !mIsSearchBarHidden )
				{
					prepareStartAnimation( mSearchBar );
					mSearchBarAnim.reverse();
				}
			}
		}
		else
		{
			mDeferOnDragEnd = false;
		}
		isOnDrag = false;//zhujieping add //拓展配置项“config_applist_in_and_out_anim_style”，添加可配置项2。2是仿S8进入主菜单的动画。
	}
	
	public void onSearchPackagesChanged(
			boolean searchVisible ,
			boolean voiceVisible )
	{
		if( mSearchBar != null )
		{
			Drawable bg = mSearchBar.getBackground();
			if( bg != null && ( !searchVisible && !voiceVisible ) )
			{
				// Save the background and disable it
				mPreviousBackground = bg;
				mSearchBar.setBackgroundResource( 0 );
			}
			else if( mPreviousBackground != null && ( searchVisible || voiceVisible ) )
			{
				// Restore the background
				// gaominghui@2016/12/14 ADD START 兼容android 4.0
				//mSearchBar.setBackground( mPreviousBackground );
				mSearchBar.setBackgroundDrawable( mPreviousBackground );
				// gaominghui@2016/12/14 ADD END 兼容android 4.0
			}
		}
	}
	
	public Rect getSearchBarBounds()
	{
		if( mSearchBar != null )
		{
			final int[] pos = new int[2];
			mSearchBar.getLocationOnScreen( pos );
			final Rect rect = new Rect();
			rect.left = pos[0];
			rect.top = pos[1];
			rect.right = pos[0] + mSearchBar.getWidth();
			rect.bottom = pos[1] + mSearchBar.getHeight();
			return rect;
		}
		else
		{
			return null;
		}
	}
	
	public DeleteDropTarget getDeleteDropTarget()
	{
		return (DeleteDropTarget)mDeleteDropTarget;
	}
	
	//cheyingkun add start	//修改运营酷搜逻辑(改为锁屏时桌面重启)
	//	/**
	//	 * 重新加载搜索框时,改变了搜索框的view,所以动画需要重新赋值
	//	 * @param launcher
	//	 */
	//	public void setupByReloadGlobalIcons(
	//			Launcher launcher )
	//	{
	//		mSearchBar = launcher.getSearchBar();
	//		if( mEnableDropDownDropTargets )
	//		{
	//			mSearchBarAnim = LauncherAnimUtils.ofFloat( mSearchBar , "translationY" , 0 , -mBarHeight );
	//		}
	//		else
	//		{
	//			mSearchBarAnim = LauncherAnimUtils.ofFloat( mSearchBar , "alpha" , 1f , 0f );
	//		}
	//		setupAnimation( mSearchBarAnim , mSearchBar );
	//	}
	//cheyingkun add end
	public void setDropTargetListener(
			DropTargetListener mDropTargetListener )
	{
		this.mDropTargetListener = mDropTargetListener;
	}
	
	public interface DropTargetListener
	{
		
		public void dropTargetAnimEnd();
	}
	
	//xiatian add start	//需求：功能页（“酷生活”、“相机页”和“音乐页”）的位置支持配置（详见BaseDefaultConfig.java中的“FUNCTION_PAGES_POSITION_XXX”）。
	public void forceHideSearchBarWithAnim()
	{//1、停止正在播放的搜索栏隐藏动画并复位；2、重新再来一遍搜索栏消失动画；3、【不涉及】标志位“mIsSearchBarHidden”。
		if( LauncherDefaultConfig.SWITCH_ENABLE_SEARCH_BAR_COMMON_PAGE )
		{
			if( mSearchBar == null )
			{
				return;
			}
			if( mSearchBarAnim.isRunning() )
			{
				//停止正在播放的搜索栏动画
				mSearchBarAnim.cancel();
				//复位
				mSearchBar.setAlpha( 1f );
				//重新再来一遍搜索栏消失动画
				prepareStartAnimation( mSearchBar );
				mSearchBarAnim.start();
			}
		}
	}
	//xiatian add end
	//zhujieping add start //添加配置项“config_empty_screen_id_in_drawer”，双层模式下，配置空白页id，如果该配置不为空，拖动图标等操作行成的空白页不删除，进入编辑模式，可添加、删除页面（仿s5）
	private boolean isEnterDeleteDropTarget = false;
	
	public void setDeleteDropTargetVisibility(
			boolean isVisible )
	{
		if( isVisible )
		{
			setTranslationX( 0 );
			prepareStartAnimation( mDropTargetBar );
			mDropTargetBarAnim.start();
			mDeleteDropTarget.setDropTargetVisible( false , true );
			mInfoDropTarget.setDropTargetVisible( false );
		}
		else
		{
			mDeleteDropTarget.setDropTargetVisible( false , false );
		}
	}
	@Override
	public boolean isInDeleteDropTarget(
			MotionEvent ev )
	{
		// TODO Auto-generated method stub
		int[] location = new int[2];
		getLocationOnScreen( location );
		if( ( ev.getRawX() > location[0] && location[0] + getWidth() > ev.getRawX() )
				//
				&& ( ev.getRawY() > location[1] && location[1] + getHeight() > ev.getRawY() )
		//
		)
		{
			if( !isEnterDeleteDropTarget )
			{
				isEnterDeleteDropTarget = true;
				mDeleteDropTarget.onDragEnter( null );
			}
			return true;
		}
		else
		{
			if( isEnterDeleteDropTarget )
			{
				isEnterDeleteDropTarget = false;
				mDeleteDropTarget.onDragExit( null );
			}
			return false;
		}

	}
	
	@Override
	public void onDropDeleteDropTarget()
	{
		// TODO Auto-generated method stub
		mDeleteDropTarget.onDragExit( null );
	}
	//zhujieping add end

}
