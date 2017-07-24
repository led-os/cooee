package com.cooee.favorites.recommended;


import java.util.concurrent.CopyOnWriteArrayList;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.R;
import com.cooee.favorites.data.AppInfo;
import com.cooee.favorites.data.NearByItem;
import com.cooee.favorites.manager.FavoritesManager;


public class FavoriteMainView extends LinearLayout
{
	
	private FavoriteNearbyView mFavoriteNearbyView;
	private FavoriteSuggestView mFavoriteSuggestView;
	//	private static Handler mHandler;
	public static int ICON_PADDING_TOP_BOTTOM = 0;
	public static int TITLE_PADDING_TOP = 0;
	public static int TITLE_PADDING_BOTTOM = 0;
	public static int TITLE_PADDING_LIFT_RIGHT = 0;
	public static int LINE_PADDING_LIFT_RIGHT = 0;
	public static int TITLE_TEXT_SIZE = 0;
	public static int ICON_TEXT_SIZE = 0;//cheyingkun add	//酷生活界面优化(常用应用未加载出来时,预留高度)
	private Activity main;
	
	//	private int mDefHeight = 0;
	public FavoriteMainView(
			Context context )
	{
		super( context );
		setOrientation( LinearLayout.VERTICAL );
		//		mDefHeight = (int)getContext().getResources().getDimension( R.dimen.def_recommended_height );
		//		setBackgroundColor( android.graphics.Color.RED );
		ICON_PADDING_TOP_BOTTOM = (int)getContext().getResources().getDimension( R.dimen.icon_padding_top );
		TITLE_PADDING_TOP = (int)getContext().getResources().getDimension( R.dimen.title_padding_top );
		TITLE_PADDING_BOTTOM = (int)getContext().getResources().getDimension( R.dimen.title_padding_bottom );
		TITLE_PADDING_LIFT_RIGHT = (int)getContext().getResources().getDimension( R.dimen.title_padding_lift_right );
		LINE_PADDING_LIFT_RIGHT = (int)getContext().getResources().getDimension( R.dimen.line_padding_lift_right );
		TITLE_TEXT_SIZE = (int)getContext().getResources().getDimension( R.dimen.favorites_title_text_size );
		ICON_TEXT_SIZE = (int)getContext().getResources().getDimension( R.dimen.icon_text_size );//cheyingkun add	//酷生活界面优化(常用应用未加载出来时,预留高度)
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableAppsKey() , FavoriteConfigString.isEnableAppsDefaultValue() ) )
		{
			LayoutInflater inflater = LayoutInflater.from( context ).cloneInContext( context );
			if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
			{
				mFavoriteSuggestView = (FavoriteSuggestView)inflater.inflate( R.layout.favorite_all_icon_s5 , null );
			}
			else
			{
				mFavoriteSuggestView = (FavoriteSuggestView)inflater.inflate( R.layout.favorite_all_icon , null );
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT );
			mFavoriteSuggestView.setLayoutParams( layoutParams );
			addView( mFavoriteSuggestView );
		}
		if( FavoritesManager.getInstance().isNearbyShow( context ) )
		{
			mFavoriteNearbyView = new FavoriteNearbyView( context );
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT );
			mFavoriteNearbyView.setLayoutParams( layoutParams );
			addView( mFavoriteNearbyView );
		}
		main = (Activity)FavoritesManager.getInstance().getContainerContext();
		//		mHandler = new Handler();
	}
	
	public void bindApp(
			final CopyOnWriteArrayList<AppInfo> data )
	{
		main.runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				if( mFavoriteSuggestView != null )
				{
					mFavoriteSuggestView.removeAllAppView();
					LinearLayout.LayoutParams layoutParams;
					if( data.size() > 0 )
					{
						layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT );
						mFavoriteSuggestView.setLayoutParams( layoutParams );
						mFavoriteSuggestView.setVisibility( View.VISIBLE );
					}
					else
					{
						mFavoriteSuggestView.setVisibility( View.GONE );
					}
					Log.v( "lvjiangbin" , "bindApp data = " + data.size() );
					mFavoriteSuggestView.addALLDataToAPP( data );
				}
			}
		} );
	}
	
	//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
	//把附近和应用分开,动态图标可以只刷新应用,不刷新附近
	public void bindNearby()
	{
		main.runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				if( mFavoriteNearbyView != null )
				{
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT );
					mFavoriteNearbyView.setLayoutParams( layoutParams );
					mFavoriteNearbyView.initLineView();
					// zhangjin@2016/06/08 ADD START
					FavoritesManager.getInstance().bindNearByLocal();
					// zhangjin@2016/06/08 ADD END
				}
			}
		} );
	}
	
	//cheyingkun add end
	public void bindAdData(
			AppInfo item ,
			int index )
	{
		if( mFavoriteNearbyView != null )//cheyingkun add	//解决“服务器关闭附近，调整时间后，桌面重启”的问题【i_0014373】
		{
			mFavoriteNearbyView.setAdView( item , index );
		}
	}
	
	// zhangjin@2016/06/08 ADD START
	public void bindNearByLocal(
			NearByItem item ,
			int index )
	{
		if( mFavoriteNearbyView != null )//cheyingkun add	//解决“服务器关闭附近，调整时间后，桌面重启”的问题【i_0014373】
		{
			mFavoriteNearbyView.setLocalView( item , index );
		}
	}
	
	// zhangjin@2016/06/08 ADD END
	public void addNearbyView()
	{
		if( mFavoriteNearbyView == null )
		{
			Log.v( "COOL" , "addNearbyView" );
			mFavoriteNearbyView = new FavoriteNearbyView( getContext() );
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT );
			mFavoriteNearbyView.setLayoutParams( layoutParams );
			addView( mFavoriteNearbyView );
			mFavoriteNearbyView.initLineView();
		}
	}
	
	public void removeNearbyView()
	{
		if( mFavoriteNearbyView != null )
		{
			mFavoriteNearbyView.removeAllViews();
			removeView( mFavoriteNearbyView );
			mFavoriteNearbyView.destroyDrawingCache();
			mFavoriteNearbyView = null;
		}
	}
	
	public void onIconSizeChanged(
			final int changedSize )
	{
		main.runOnUiThread( new Runnable() {
			
			/**
			 * 
			 */
			@Override
			public void run()
			{
				//cheyingkun add start	//酷生活支持动态修改图标大小
				if( mFavoriteSuggestView != null )
				{
					mFavoriteSuggestView.onIconSizeChanged( changedSize );
					mFavoriteSuggestView.requestLayout();
				}
				if( mFavoriteNearbyView != null )
				{
					mFavoriteNearbyView.onIconSizeChanged( changedSize );
					mFavoriteNearbyView.requestLayout();
				}
				FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)FavoriteMainView.this.getLayoutParams();
				if( layoutParams != null )
				{
					layoutParams.height = getMainViewHeight();
					FavoriteMainView.this.setLayoutParams( layoutParams );
				}
				//cheyingkun add end
			}
		} );
	}
	
	//cheyingkun add start	//修改酷生活S5引导页动画。
	public AnimatorSet getFavoritesViewShowAnimation()
	{
		if( mFavoriteSuggestView == null && mFavoriteNearbyView == null )
		{
			return null;
		}
		FavoritesManager mFavoritesManager = FavoritesManager.getInstance();
		//动画延迟
		int favoritesViewAnimShowDelay = FavoritesManager.getInstance().getFavoritesViewAnimShowDelay();
		//动画延迟系数
		int showFavoritesViewDelayNum = mFavoritesManager.getShowFavoritesViewDelayNum();
		AnimatorSet mainViewAnimatorSet = new AnimatorSet();
		//联系人、应用动画
		if( mFavoriteSuggestView != null )
		{
			ObjectAnimator mFavoriteSuggestViewAnim = new ObjectAnimator();
			PropertyValuesHolder mFavoriteSuggestViewX = PropertyValuesHolder.ofFloat( "x" , 0 );
			mFavoriteSuggestViewAnim.setInterpolator( new DecelerateInterpolator() );
			mFavoriteSuggestViewAnim.setTarget( mFavoriteSuggestView );
			mFavoriteSuggestViewAnim.setValues( mFavoriteSuggestViewX );
			showFavoritesViewDelayNum++;
			mFavoriteSuggestViewAnim.setStartDelay( favoritesViewAnimShowDelay * showFavoritesViewDelayNum );
			mainViewAnimatorSet.playTogether( mFavoriteSuggestViewAnim );
		}
		//附近动画
		if( mFavoriteNearbyView != null )
		{
			PropertyValuesHolder mFavoriteNearbyViewX = PropertyValuesHolder.ofFloat( "x" , 0 );
			ObjectAnimator mFavoriteNearbyViewAnim = new ObjectAnimator();
			mFavoriteNearbyViewAnim.setInterpolator( new DecelerateInterpolator() );
			mFavoriteNearbyViewAnim.setTarget( mFavoriteNearbyView );
			mFavoriteNearbyViewAnim.setValues( mFavoriteNearbyViewX );
			showFavoritesViewDelayNum++;
			mFavoriteNearbyViewAnim.setStartDelay( favoritesViewAnimShowDelay * showFavoritesViewDelayNum );
			mainViewAnimatorSet.playTogether( mFavoriteNearbyViewAnim );
		}
		mFavoritesManager.setShowFavoritesViewDelayNum( showFavoritesViewDelayNum );
		return mainViewAnimatorSet;
	}
	
	public void setFavoriteClingsShowX(
			int x )
	{
		if( mFavoriteSuggestView != null )
		{
			mFavoriteSuggestView.setX( x );
		}
		if( mFavoriteNearbyView != null )
		{
			mFavoriteNearbyView.setX( x );
		}
	}
	
	//cheyingkun add end
	@Override
	protected void onLayout(
			boolean changed ,
			int l ,
			int t ,
			int r ,
			int b )
	{
		// TODO Auto-generated method stub
		super.onLayout( changed , l , t , r , b );
	}
	
	//chenchen add start
	/**
	 * 重新加载整个headView的高度
	 */
	@Override
	protected void onMeasure(
			int widthMeasureSpec ,
			int heightMeasureSpec )
	{
		// TODO Auto-generated method stub
		//xiatian start	//添加非空保护（解决：“关闭推荐联系人、推荐应用和附近后，桌面起不来”的问题【c_0004267】）
		//xiatian del start
		//		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec( getContext().getResources().getDisplayMetrics().widthPixels , View.MeasureSpec.EXACTLY );
		//		int childheightMeasureSpec = MeasureSpec.makeMeasureSpec( mFavoriteSuggestView.getSuggestHeight() , View.MeasureSpec.EXACTLY );
		//		mFavoriteSuggestView.measure( childWidthMeasureSpec , childheightMeasureSpec );
		//		childheightMeasureSpec = MeasureSpec.makeMeasureSpec( mFavoriteNearbyView.getNearbyHeight() , View.MeasureSpec.EXACTLY );
		//		mFavoriteNearbyView.measure( childWidthMeasureSpec , childheightMeasureSpec );
		//xiatian del end
		//xiatian add start
		int mWidthPixels = getContext().getResources().getDisplayMetrics().widthPixels;
		if( mFavoriteSuggestView != null )
		{
			int mFavoriteSuggestViewWidthMeasureSpec = MeasureSpec.makeMeasureSpec( mWidthPixels , View.MeasureSpec.EXACTLY );
			int mFavoriteSuggestViewHeightMeasureSpec = MeasureSpec.makeMeasureSpec( mFavoriteSuggestView.getSuggestHeight() , View.MeasureSpec.EXACTLY );
			mFavoriteSuggestView.measure( mFavoriteSuggestViewWidthMeasureSpec , mFavoriteSuggestViewHeightMeasureSpec );
		}
		if( mFavoriteNearbyView != null )
		{
			int mFavoriteNearbyViewWidthMeasureSpec = MeasureSpec.makeMeasureSpec( mWidthPixels , View.MeasureSpec.EXACTLY );
			int mFavoriteNearbyViewHeightMeasureSpec = MeasureSpec.makeMeasureSpec( mFavoriteNearbyView.getNearbyHeight() , View.MeasureSpec.EXACTLY );
			mFavoriteNearbyView.measure( mFavoriteNearbyViewWidthMeasureSpec , mFavoriteNearbyViewHeightMeasureSpec );
		}
		//xiatian add end
		//xiatian end
		setMeasuredDimension( mWidthPixels , getMainViewHeight() );
	}
	
	/**
	 * 获取整个headView的高度
	 * @return
	 */
	public int getMainViewHeight()
	{
		int ret = 0;
		if( mFavoriteSuggestView != null )//xiatian add	//添加非空保护（解决：“关闭推荐联系人、推荐应用和附近后，桌面起不来”的问题【c_0004267】）
		{
			ret += mFavoriteSuggestView.getSuggestHeight();
		}
		if( mFavoriteNearbyView != null )//xiatian add	//添加非空保护（解决：“关闭推荐联系人、推荐应用和附近后，桌面起不来”的问题【c_0004267】）
		{
			ret += mFavoriteNearbyView.getNearbyHeight();
		}
		return ret;
	}
	//chenchen add edd
	;
	
	//cheyingkun add start	//服务器关闭酷生活后，释放资源。
	public void clearFavoritesView()
	{
		if( mFavoriteSuggestView != null )
		{
			mFavoriteSuggestView.clearFavoritesView();
		}
		this.removeAllViews();
	}
	
	//cheyingkun add start	//解决“服务器关闭附近，附近处显示空白”的问题【i_0014374】
	public void notifyNearbyChange()
	{
		int mainViewHeight = getMainViewHeight();//推荐区域总高度
		float headerViewHeight = FavoritesManager.getInstance().getHeaderViewHeight();//推荐区域显示高度
		Log.d( "" , "cyk mainViewHeight: " + mainViewHeight + " headerViewHeight: " + headerViewHeight );
		//高度的差值表示当前模式下和四行全部显示时的差值,即:隐藏掉行的高度.也就是附近被初始化出来之后需要向上位移的高度
		//(这个差值和favoritesManager中的initViews里,初始化调整酷生活viewY值,都是为了计算隐藏掉的行数高度,后续优化整理)
		if( mFavoriteNearbyView != null )
		{
			if( mFavoriteNearbyView.getY() <= 0 )
			{
				mFavoriteNearbyView.setY( headerViewHeight - mainViewHeight );
			}
		}
	}
	//cheyingkun add end
}
