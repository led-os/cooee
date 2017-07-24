package com.cooee.favorites.view;


import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.R;
import com.cooee.favorites.clings.FavoritesClingsView;
import com.cooee.favorites.manager.AdPlaceIdManager;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.utils.Tools;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.cooee.uniex.wrap.IFavoriteClings;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;


/**
 * cheyingkun add whole view	//酷生活引导页
 * @author cheyingkun 酷生活引导页和酷生活内容的父类
 */
public class FavoritesViewGroupsParent extends FrameLayout
{
	
	//酷生活内容
	private FavoritesViewGroup view;
	//酷生活引导页
	private FavoritesClingsView mFavoritesClingsView;
	private boolean isStartFavoritesShowAnim = false;//cheyingkun add	//修改酷生活S5引导页动画。
	private final String TAG = "FavoritesViewGroupsParent";
	
	public FavoritesViewGroupsParent(
			Context context ,
			FavoritesViewGroup view ,
			FavoritesClingsView mFavoritesClingsView )
	{
		super( context );
		this.view = view;
		this.mFavoritesClingsView = mFavoritesClingsView;
		//显示酷生活引导页
		FavoritesConfig config = FavoritesManager.getInstance().getConfig();
		boolean switchEnableClings = config.getBoolean( FavoriteConfigString.getEnableFavoritesClingsKey() , FavoriteConfigString.isEnableFavoritesClingsDefaultValue() );
		if( switchEnableClings && this.mFavoritesClingsView != null )
		{
			addFavoritesClingsView();
		}
		//酷生活内容
		if( this.view != null )
		{
			int launcherSearchBarHeight = 0;
			int statusBarHeight = FavoritesManager.getInstance().getStatusBarHeight();
			//计算FavoritesViewGroup的上边距
			if( FavoritesManager.getInstance().favoriteShowLauncherSearch() )
			{
				launcherSearchBarHeight = config.getInt( FavoriteConfigString.getLauncherSearchBarHeightKey() , FavoriteConfigString.getLauncherSearchBarHeightDefaultValue() );
			}
			FrameLayout.LayoutParams layoutParams = (android.widget.FrameLayout.LayoutParams)this.view.getLayoutParams();
			if( layoutParams == null )
			{
				layoutParams = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT , FrameLayout.LayoutParams.MATCH_PARENT );
			}
			//cheyinkgun add start	//酷生活适配VERSION==4的版本(该版本之前,酷生活页面的上边距由桌面计算,之后由酷生活自己计算)
			if( FavoritesManager.getInstance().isMoreThanTheVersion( 4 , 41785 ) )//41778的下一个版本支持该功能
			//cheyinkgun add end
			{
				layoutParams.topMargin = statusBarHeight + launcherSearchBarHeight;
			}
			view.setLayoutParams( layoutParams );
			this.addView( this.view , layoutParams );
		}
		setBackgroundColor( Color.argb( 0 , 1 , 1 , 1 ) );//cheyingkun add	//解决“新闻没刷出来之前,（1.点击显示更多2.进入新闻全屏并快速下滑）酷生活显示异常”的问题【i_0014097】
	}
	
	//cheyingkun add start	//酷生活引导页
	private int screenWidth = -1;
	
	private void addFavoritesClingsView()
	{
		if( screenWidth == -1 )
		{
			DisplayMetrics metric = new DisplayMetrics();
			( (Activity)FavoritesManager.getInstance().getContainerContext() ).getWindowManager().getDefaultDisplay().getMetrics( metric );
			screenWidth = metric.widthPixels;
		}
		this.addView( this.mFavoritesClingsView , ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.MATCH_PARENT );
		//设置其他viewx为负数
		if( this.view != null )
		{
			this.view.setX( -screenWidth );
			this.view.setFavoriteClingsShowX( -screenWidth );//cheyingkun add	//修改酷生活S5引导页动画。
		}
	}
	
	/**
	 * 移除酷生活引导页动画
	 */
	public void removeFavoritesClingsAnimation()
	{
		if( FavoritesManager.getInstance().isShowFavoriteClings() )
		{
			//cheyingkun add start	//修改酷生活S5引导页动画。
			ObjectAnimator removeClingsViewAnimation = mFavoritesClingsView.getRemoveAnimation();
			removeClingsViewAnimation.addUpdateListener( new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(
						ValueAnimator animation )
				{
					float scaleValues = getResources().getInteger( R.integer.favorite_clings_dismiss_scale_threshold_start_favoritesView_ainm );
					float scaleX = mFavoritesClingsView.getScaleX();
					int tmpScaleX = (int)( scaleX * 100 );
					Log.d( "" , "cyk scaleValues : " + scaleValues + " tmpScaleX: " + tmpScaleX );
					if( scaleValues >= tmpScaleX )
					{
						FavoritesViewGroupsParent.this.view.setX( 0 );
						startAnimation();
					}
				}
			} );
			removeClingsViewAnimation.start();
			//cheyingkun add end
		}
	}
	
	/**
	 * 获取酷生活非引导页view的动画
	 */
	private AnimatorSet getFavoritesViewShowAnimation()
	{
		//酷生活内容
		if( this.view != null )
		{
			return view.getFavoritesViewShowAnimation();
		}
		return null;
	}
	
	//cheyingkun add start	//修改酷生活S5引导页动画。
	private void startAnimation()
	{
		if( isStartFavoritesShowAnim )
		{
			return;
		}
		//动画集合
		AnimatorSet showViewAnimatorSet = new AnimatorSet();
		//酷生活view动画
		AnimatorSet favoritesViewShowAnimationSet = getFavoritesViewShowAnimation();
		if( favoritesViewShowAnimationSet != null )
		{
			showViewAnimatorSet.playTogether( favoritesViewShowAnimationSet );
		}
		//酷生活显示桌面的搜索,则获取桌面搜索动画
		FavoritesManager mFavoritesManager = FavoritesManager.getInstance();
		if( mFavoritesManager.favoriteShowLauncherSearch() )
		{
			//桌面搜索动画
			IFavoriteClings mIFavoriteClings = FavoritesManager.getInstance().getIFavoriteClings();
			if( mIFavoriteClings != null )
			{//原生桌面,返回动画给酷生活;非原生桌面 自己做动画,返回空
				ObjectAnimator searchBarAnim = mIFavoriteClings.removeFavoritesClingsAnimation( getResources().getInteger( R.integer.favorites_view_anim_show_launcher_search_duration ) );
				if( searchBarAnim != null )
				{
					searchBarAnim.setInterpolator( new DecelerateInterpolator() );
					showViewAnimatorSet.playTogether( searchBarAnim );
				}
			}
		}
		showViewAnimatorSet.setDuration( getResources().getInteger( R.integer.favorites_view_anim_show_duration ) );
		showViewAnimatorSet.addListener( new AnimatorListenerAdapter() {
			
			public void onAnimationStart(
					android.animation.Animator animation )
			{
				isStartFavoritesShowAnim = true;
			};
			
			public void onAnimationEnd(
					android.animation.Animator animation )
			{
				isStartFavoritesShowAnim = false;
			};
			
			public void onAnimationCancel(
					android.animation.Animator animation )
			{
				isStartFavoritesShowAnim = false;
			};
		} );
		showViewAnimatorSet.start();
	}
	
	//fulijuan add start		//需求（演示版）：酷生活一级界面添加开屏广告（进入酷生活五次，就请求一次广告）
	private AdBaseView mrsplashView;
	private int getAdState = 0;//0表示初始，1表示获取中，2表示成功
	private int mShowCount = 0;
	private ImageView mClose = null;
	
	public void onShow()
	{
		mShowCount++;
		Log.v( "zjp" , "onshow mShowCount = " + mShowCount );
		if( mrsplashView != null && mrsplashView.getParent() != null )
		{
			//当广告显示的时候，搜索栏显示在广告下面
			getContext().sendBroadcast( new Intent( "com.cooee.favorites.searchbar.disable" ) );
		}
	}
	
	public void onHide()
	{
		Log.v( "zjp" , "mrsplashView = " + mrsplashView );
		post( new Runnable() {
			
			public void run()
			{
				if( mrsplashView != null && mrsplashView.getParent() != null )
				{
					removeView( mrsplashView );
					mrsplashView.onDestroy();
					mrsplashView = null;
					mShowCount = 0;
					getContext().sendBroadcast( new Intent( "com.cooee.favorites.searchbar.enable" ) );
					mClose.setVisibility( View.GONE );
				}
				if( mrsplashView == null || getAdState == 0 )
				{
					createRsplashAdView();
				}

			}
		} );
		postDelayed( new Runnable() {
			
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( mShowCount >= 4 )
				{
					if( getAdState == 2 && mrsplashView != null && mrsplashView.getParent() == null )
					{
						FrameLayout.LayoutParams params;
						if( mClose == null )
						{
							mClose = new ImageView( getContext() );
							mClose.setImageResource( R.drawable.rsplash_close );
							params = new FrameLayout.LayoutParams( Tools.dip2px( getContext() , 20 ) , Tools.dip2px( getContext() , 20 ) );
							params.topMargin = FavoritesManager.getInstance().getStatusBarHeight();
							params.gravity = Gravity.RIGHT;
							addView( mClose , params );
							mClose.setOnClickListener( new OnClickListener() {
								
								
								@Override
								public void onClick(
										View v )
								{
									// TODO Auto-generated method stub
									Log.v( "zjp" , "close onclick" );
									if( mrsplashView != null && mrsplashView.getParent() != null )
									{
										removeView( mrsplashView );
										mrsplashView.onDestroy();
										mrsplashView = null;
										mShowCount = 0;
										getContext().sendBroadcast( new Intent( "com.cooee.favorites.searchbar.enable" ) );
										mClose.setVisibility( View.GONE );
									}
								}
							} );
						}
						mClose.setVisibility( View.VISIBLE );
						params = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.WRAP_CONTENT , FrameLayout.LayoutParams.WRAP_CONTENT );
						params.topMargin = FavoritesManager.getInstance().getStatusBarHeight();
						addView( mrsplashView , indexOfChild( mClose ) , params );
						Log.v( "zjp" , "add mrsplashView = " + mrsplashView );
					}
				}
			}
		} , 350 );
	}
	
	public void createRsplashAdView()
	{
		Context context = getContext();
		if( mrsplashView != null )
		{
			mrsplashView.onDestroy();
			mrsplashView = null;
		}
		Log.v( "zjp" , "createRsplashAdView" );
		KmobManager.setContext( context );
		getAdState = 1;
		mrsplashView = KmobManager.createRsplash( AdPlaceIdManager.getInstance().getDemoAdWhenOnShowId() , context , getWidth() , getHeight() - FavoritesManager.getInstance().getStatusBarHeight() );
		mrsplashView.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				Log.v( "zjp" , "mrsplashView onclick" );
			}
		} );
		mrsplashView.addAdViewListener( new AdViewListener() {
			
			@Override
			public void onAdShow(
					String info )
			{
				Log.v( "zjp" , "AdViewListener onAdShow info " + info );
			}
			
			@Override
			public void onAdReady(
					String space_id )
			{
				Log.v( "zjp" , "AdViewListener onAdReady space_id " + space_id );
				getAdState = 2;
			}
			
			@Override
			public void onAdFailed(
					String reason )
			{
				Log.v( "zjp" , "AdViewListener onAdFailed reason " + reason );
				getAdState = 0;
			}
			
			@Override
			public void onAdClick(
					String arg0 )
			{
				Log.v( "zjp" , "AdViewListener onAdClick arg0 " + arg0 );
				//				RsplashActivity.this.finish();
			}
			
			@Override
			public void onAdClose(
					String info )
			{
				Log.v( "" , "AdViewListener onAdClose arg0 " + info );
			}
			
			@Override
			public void onAdCancel(
					String info )
			{
				Log.v( "" , "AdViewListener onAdCancel arg0 " + info );
			}
		} );
	}
	//fulijuan add end
	
}
