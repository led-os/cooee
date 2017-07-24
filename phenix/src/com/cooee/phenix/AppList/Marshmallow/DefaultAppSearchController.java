package com.cooee.phenix.AppList.Marshmallow;


import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.cooee.phenix.ExtendedEditText;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.util.Thunk;


/**
 * The default search controller.
 */
final class DefaultAppSearchController extends AllAppsSearchBarController
//
implements View.OnClickListener//xiatian add	//优化“2016/05/25 17:50:26”的修改点中关于6.0主菜单搜索栏的修改。
//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
//
, TextWatcher
//
, TextView.OnEditorActionListener
//xiatian add end
{
	
	private View mSearchView;
	private final Launcher mLauncher;//xiatian add	//优化“2016/05/25 17:50:26”的修改点中关于6.0主菜单搜索栏的修改。
	//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
	private static final boolean ALLOW_SINGLE_APP_LAUNCH = true;
	private static final int FADE_IN_DURATION = 175;
	private static final int FADE_OUT_DURATION = 100;
	private static final int SEARCH_TRANSLATION_X_DP = 18;
	private final Context mContext;
	@Thunk
	final InputMethodManager mInputMethodManager;
	private DefaultAppSearchAlgorithm mSearchManager;
	private ViewGroup mContainerView;
	@Thunk
	View mSearchBarContainerView;
	private View mSearchButtonView;
	private View mDismissSearchButtonView;
	@Thunk
	ExtendedEditText mSearchBarEditView;
	@Thunk
	AllAppsRecyclerView mAppsRecyclerView;
	@Thunk
	Runnable mFocusRecyclerViewRunnable = new Runnable() {
		
		@Override
		public void run()
		{
			mAppsRecyclerView.requestFocus();
		}
	};
	//xiatian add end
	private View mSearchBarInputClear;//zhujieping add //android6.0、7.0主菜单搜索（非酷搜）中增加显示“x”
	;
	
	public DefaultAppSearchController(
			Context context ,
			ViewGroup containerView ,
			AllAppsRecyclerView appsRecyclerView )
	{
		mLauncher = (Launcher)context;//xiatian add	//优化“2016/05/25 17:50:26”的修改点中关于6.0主菜单搜索栏的修改。
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
		{
			mContext = context;
			mInputMethodManager = (InputMethodManager)mContext.getSystemService( Context.INPUT_METHOD_SERVICE );
			mContainerView = containerView;
			mAppsRecyclerView = appsRecyclerView;
		}
		else
		//xiatian add end
		{
			mContext = null;
			mInputMethodManager = null;
			mContainerView = null;
			mAppsRecyclerView = null;
		}
	}
	
	@Override
	public View getView(
			ViewGroup parent )
	{
		LayoutInflater inflater = LayoutInflater.from( parent.getContext() );
		mSearchView = inflater.inflate( R.layout.all_apps_search_bar , parent , false );
		mSearchView.setOnClickListener( this );//xiatian add	//优化“2016/05/25 17:50:26”的修改点中关于6.0主菜单搜索栏的修改。
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
		{
			mSearchButtonView = mSearchView.findViewById( R.id.search_button );
			mSearchBarContainerView = mSearchView.findViewById( R.id.search_container );
			mDismissSearchButtonView = mSearchBarContainerView.findViewById( R.id.dismiss_search_button );
			mDismissSearchButtonView.setOnClickListener( this );
			//zhujieping add start  //android6.0、7.0主菜单搜索（非酷搜）中增加显示“x”
			mSearchBarInputClear = mSearchBarContainerView.findViewById( R.id.search_box_input_clear );
			if( mSearchBarInputClear != null )
				mSearchBarInputClear.setOnClickListener( this );
			//zhujieping add end
			mSearchBarEditView = (ExtendedEditText)mSearchBarContainerView.findViewById( R.id.search_box_input );
			mSearchBarEditView.addTextChangedListener( this );
			mSearchBarEditView.setOnEditorActionListener( this );
			mSearchBarEditView.setOnBackKeyListener( new ExtendedEditText.OnBackKeyListener() {
				
				@Override
				public boolean onBackKey()
				{
					// Only hide the search field if there is no query, or if there
					// are no filtered results
					String query = Utilities.trim( mSearchBarEditView.getEditableText().toString() );
					if( query.isEmpty() || mApps.hasNoFilteredResults() )
					{
						hideSearchField( true , mFocusRecyclerViewRunnable );
						return true;
					}
					return false;
				}
			} );
		}
		//xiatian add end
		return mSearchView;
	}
	
	@Override
	protected void onInitialize()
	{
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
		{
			if( mApps != null )
			{
				mSearchManager = new DefaultAppSearchAlgorithm( mApps.getApps() );
			}
		}
		//xiatian add end
	}
	
	@Override
	public void focusSearchField()
	{
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
		{
			if( mSearchBarEditView != null )
			{
				mSearchBarEditView.requestFocus();
			}
			showSearchField();
		}
		//xiatian add end
	}
	
	@Override
	public boolean isSearchFieldFocused()
	{
		boolean ret = false;
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
		{
			if( mSearchBarEditView != null )
			{
				ret = mSearchBarEditView.isFocused();
			}
		}
		//xiatian add end
		return ret;
	}
	
	@Override
	public void reset()
	{
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
		{
			hideSearchField( false , null );
		}
		//xiatian add end
	}
	
	@Override
	public boolean shouldShowPredictionBar()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	//xiatian add start	//优化“2016/05/25 17:50:26”的修改点中关于6.0主菜单搜索栏的修改。
	@Override
	public void onClick(
			View v )
	{
		if( mSearchView != null && v == mSearchView )
		{
			//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
			if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
			{
				showSearchField();
			}
			else
			//xiatian add end
			{
				mLauncher.onClickSearchButton( v );
			}
		}
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		else if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH && mDismissSearchButtonView != null && v == mDismissSearchButtonView )
		{
			hideSearchField( true , mFocusRecyclerViewRunnable );
		}
		//xiatian add end
		//zhujieping add start  //android6.0、7.0主菜单搜索（非酷搜）中增加显示“x”
		else if( mSearchBarInputClear != null && mSearchBarInputClear == v && mSearchBarEditView != null )
		{
			mSearchBarEditView.setText( "" );
		}
		//zhujieping add end
	}
	//xiatian add end
	;
	
	//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
	@Override
	public void beforeTextChanged(
			CharSequence s ,
			int start ,
			int count ,
			int after )
	{
		// Do nothing
	}
	
	@Override
	public void onTextChanged(
			CharSequence s ,
			int start ,
			int before ,
			int count )
	{
		// Do nothing
	}
	
	@Override
	public void afterTextChanged(
			final Editable s )
	{
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
		{
			String query = s.toString();
			if( query.isEmpty() )
			{
				mSearchManager.cancel( true );
				mCb.clearSearchResult();
			}
			else
			{
				mSearchManager.cancel( false );
				mSearchManager.doSearch( query , mCb );
			}
		}
		//xiatian add end
	}
	
	@Override
	public boolean onEditorAction(
			TextView v ,
			int actionId ,
			KeyEvent event )
	{
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
		{
			// Skip if we disallow app-launch-on-enter
			if( !ALLOW_SINGLE_APP_LAUNCH )
			{
				return false;
			}
			// Skip if it's not the right action
			if( actionId != EditorInfo.IME_ACTION_SEARCH )
			{
				return false;
			}
			// Skip if there are more than one icon
			if( mApps.getNumFilteredApps() > 1 )
			{
				return false;
			}
			// Otherwise, find the first icon, or fallback to the search-market-view and launch it
			List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();
			for( int i = 0 ; i < items.size() ; i++ )
			{
				AlphabeticalAppsList.AdapterItem item = items.get( i );
				switch( item.viewType )
				{
					case AllAppsGridAdapter.ICON_VIEW_TYPE:
					case AllAppsGridAdapter.SEARCH_MARKET_VIEW_TYPE:
						mAppsRecyclerView.getChildAt( i ).performClick();
						mInputMethodManager.hideSoftInputFromWindow( mContainerView.getWindowToken() , 0 );
						return true;
				}
			}
		}
		//xiatian add end
		return false;
	}
	
	/**
	 * Focuses the search field.
	 */
	private void showSearchField()
	{
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
		{
			// Show the search bar and focus the search
			final int translationX = Utilities.pxFromDp( SEARCH_TRANSLATION_X_DP , mContext.getResources().getDisplayMetrics() );
			mSearchBarContainerView.setVisibility( View.VISIBLE );
			mSearchBarContainerView.setAlpha( 0f );
			mSearchBarContainerView.setTranslationX( translationX );
			// gaominghui@2016/12/14 ADD START 兼容android4.0
			ViewPropertyAnimator animator = mSearchBarContainerView.animate().alpha( 1f ).translationX( 0 ).setDuration( FADE_IN_DURATION );
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
			{
				animator.withLayer().withEndAction( new Runnable() {
					
					@Override
					public void run()
					{
						finishAnimatorCallback();
					}
				} );
			}
			else
			{
				mSearchBarContainerView.setLayerType( View.LAYER_TYPE_HARDWARE , null );
				animator.setListener( new AnimatorListener() {
					
					@Override
					public void onAnimationStart(
							Animator animation )
					{
						// TODO Auto-generated method stub
					}
					
					@Override
					public void onAnimationRepeat(
							Animator animation )
					{
						// TODO Auto-generated method stub
					}
					
					@Override
					public void onAnimationEnd(
							Animator animation )
					{
						mSearchBarContainerView.setLayerType( View.LAYER_TYPE_NONE , null );
						finishAnimatorCallback();
					}
					
					@Override
					public void onAnimationCancel(
							Animator animation )
					{
						// TODO Auto-generated method stub
					}
				} );
			}
			mSearchButtonView.animate().alpha( 0f ).translationX( -translationX ).setDuration( FADE_OUT_DURATION );
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
			{
				mSearchButtonView.animate().withLayer();
			}
			// gaominghui@2016/12/14 ADD END 兼容android4.0
		}
		//xiatian add end
	}
	
	// gaominghui@2016/12/14 ADD START
	/**
	 *mSearchBarContainerView动画结束之后需要调用的方法，该方法为了兼容android4.0抽出的公共代码
	 * @author gaominghui 2016年12月29日
	 */
	private void finishAnimatorCallback()
	{
		mSearchBarEditView.requestFocus();
		mInputMethodManager.showSoftInput( mSearchBarEditView , InputMethodManager.SHOW_IMPLICIT );
	}
	
	// gaominghui@2016/12/14 ADD END
	/**
	 * Unfocuses the search field.
	 */
	@Thunk
	void hideSearchField(
			boolean animated ,
			final Runnable postAnimationRunnable )
	{
		//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )
		{
			mSearchManager.cancel( true );
			final boolean resetTextField = mSearchBarEditView.getText().toString().length() > 0;
			final int translationX = Utilities.pxFromDp( SEARCH_TRANSLATION_X_DP , mContext.getResources().getDisplayMetrics() );
			if( animated )
			{
				// Hide the search bar and focus the recycler view
				// gaominghui@2016/12/14 ADD START
				if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
				{
					mSearchBarContainerView.animate().alpha( 0f ).translationX( 0 ).setDuration( FADE_IN_DURATION ).withLayer().withEndAction( new Runnable() {
						
						@Override
						public void run()
						{
							mSearchBarContainerView.setVisibility( View.INVISIBLE );
							if( resetTextField )
							{
								mSearchBarEditView.setText( "" );
							}
							mCb.clearSearchResult();
							if( postAnimationRunnable != null )
							{
								postAnimationRunnable.run();
							}
						}
					} );
				}
				else
				{
					mSearchBarContainerView.setLayerType( View.LAYER_TYPE_HARDWARE , null );
					mSearchBarContainerView.animate().alpha( 0f ).translationX( 0 ).setDuration( FADE_IN_DURATION ).setListener( new AnimatorListener() {
						
						@Override
						public void onAnimationStart(
								Animator animation )
						{
							// TODO Auto-generated method stub
						}
						
						@Override
						public void onAnimationRepeat(
								Animator animation )
						{
							// TODO Auto-generated method stub
						}
						
						@Override
						public void onAnimationEnd(
								Animator animation )
						{
							mSearchBarContainerView.setLayerType( View.LAYER_TYPE_NONE , null );
							mSearchBarContainerView.setVisibility( View.INVISIBLE );
							if( resetTextField )
							{
								mSearchBarEditView.setText( "" );
							}
							mCb.clearSearchResult();
							if( postAnimationRunnable != null )
							{
								postAnimationRunnable.run();
							}
						}
						
						@Override
						public void onAnimationCancel(
								Animator animation )
						{
							// TODO Auto-generated method stub
						}
					} );
				}
				// gaominghui@2016/12/14 ADD END
				mSearchButtonView.setTranslationX( -translationX );
				// gaominghui@2016/12/14 ADD START
				mSearchButtonView.animate().alpha( 1f ).translationX( 0 ).setDuration( FADE_OUT_DURATION );
				if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
				{
					mSearchButtonView.animate().withLayer();
				}
				// gaominghui@2016/12/14 ADD END
			}
			else
			{
				mSearchBarContainerView.setVisibility( View.INVISIBLE );
				if( resetTextField )
				{
					mSearchBarEditView.setText( "" );
				}
				mCb.clearSearchResult();
				mSearchButtonView.setAlpha( 1f );
				mSearchButtonView.setTranslationX( 0f );
				if( postAnimationRunnable != null )
				{
					postAnimationRunnable.run();
				}
			}
			mInputMethodManager.hideSoftInputFromWindow( mContainerView.getWindowToken() , 0 );
		}
		//xiatian add end
	}
	//xiatian add end
	;
}
