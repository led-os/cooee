package com.cooee.favorites.news;


import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.FavoritesPlugin;
import com.cooee.favorites.R;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.news.NewsAdapter.onNotifyClickListener;
import com.cooee.favorites.news.PullToRefreshLayout.OnRefreshListener;
import com.cooee.favorites.news.data.Callbacks;
import com.cooee.favorites.news.data.CategoryItem;
import com.cooee.favorites.news.data.NewsItem;
import com.cooee.favorites.utils.Tools;
import com.cooee.favorites.view.FavoritesViewGroup;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.cooee.uniex.wrap.IFavoritesReady;
import com.umeng.analytics.MobclickAgent;


public class NewsView extends RelativeLayout implements View.OnClickListener , FavoritesViewGroup.StickCallback
{
	
	private FrameLayout mHeader;
	private Context mContext;
	private View mCategoryFailed;
	private ImageView mTop;
	private View networkError;
	private RequestQueue mQueue;
	private HashMap<String , ArrayList<NewsItem>> mNewsList = new HashMap<String , ArrayList<NewsItem>>();
	private NewsModel mModel;
	private int TitleTextSize = 10;
	private int horizonMargin = 10;
	//	private int topBottomMargin = 10;
	public static int TITLE_PADDING_TOP = 0;
	public static int TITLE_PADDING_BOTTOM = 0;
	private int scrollTopMarginBootom = 10;
	private int NETERROR_IMAGE = 60;
	private int TOP_SIZE = 30;
	private final int HEADER_ID = 1000;
	private final int NEWS_ID = 1001;
	private final int TOP_ID = 1004;
	private ValueAnimator valueAnimation;
	private Drawable refreshDrawable;
	public static final int START_REFRESH = 0;
	public static final int END_REFRESH = 1;
	public static final int LOAD_MORE_SUCCESS = 0;
	public static final int LOAD_MORE_FAIL = 1;
	private Handler mHandler = new Handler();
	private FrameLayout mMain;
	private Drawable spinner;
	private View mCollapseTitle;
	private View mExpandTitle;
	private final int RADIO_BUTTON_ID_START = 3001;
	private RadioGroup mCategoryGroup;
	private ArrayList<PullToRefreshLayout> pageviewLists = new ArrayList<PullToRefreshLayout>();
	private ViewPagerAdapter mViewPagerAdapter;
	private NewsViewPager mViewPager;
	private int alternateDuration = 400;
	//	private ProgressBar mProgressBar;
	private HorizontalScrollView mScrollView;
	private long enterTime = -1;
	private float mRatio = 1;//初始时非展开状态，为1
	private ImageView mRefresh;
	private ValueAnimator rotateAnimation;
	int location[] = new int[2];
	
	@SuppressLint( "NewApi" )
	public NewsView(
			Context pluginContext ,
			Context containerContext )
	{
		super( pluginContext );
		// TODO Auto-generated constructor stub
		mContext = pluginContext;
		init( pluginContext );
		mHandler = new Handler();
		mQueue = Volley.newRequestQueue( pluginContext );
		mHeader = getHeaderView( mContext );
		mHeader.setId( HEADER_ID );
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
		params.addRule( RelativeLayout.ALIGN_PARENT_TOP , RelativeLayout.TRUE );
		addView( mHeader , params );
		mMain = new FrameLayout( mContext );
		View content = getNewsContent();
		params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
		mMain.addView( content , params );
		//		networkError = getNetworkErrorView( pluginContext );
		//		params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
		//		params.addRule( RelativeLayout.BELOW , HEADER_ID );
		//		networkError.setVisibility( View.GONE );
		//		mMain.addView( networkError , params );
		mCategoryFailed = getCategoryError( pluginContext );
		params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.MATCH_PARENT );
		mCategoryFailed.setVisibility( View.GONE );
		mMain.addView( mCategoryFailed , params );
		params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
		params.addRule( RelativeLayout.BELOW , HEADER_ID );
		addView( mMain , params );
		//		mModel = new NewsModel( pluginContext , mQueue , mNewsList , this );//这个不着急初始化，等到页面滑动到这页再开始初始化
		mTop = new ImageView( pluginContext );
		mTop.setId( TOP_ID );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() ) )
		{
			PullToRefreshLayout.setFirstVisible( 4 );
			mTop.setImageResource( R.drawable.scroll_to_top_simple_launcher );
		}
		else
		{
			if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
			{
				mTop.setImageResource( R.drawable.scroll_to_top_s5 );
			}
			else
			{
				mTop.setImageResource( R.drawable.scroll_to_top );
			}
		}
		mTop.setOnClickListener( this );
		params = new RelativeLayout.LayoutParams( TOP_SIZE + horizonMargin * 2 , TOP_SIZE + scrollTopMarginBootom * 2 );
		params.addRule( RelativeLayout.ALIGN_PARENT_RIGHT , RelativeLayout.TRUE );
		params.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.TRUE );
		mTop.setPadding( horizonMargin , scrollTopMarginBootom , horizonMargin , scrollTopMarginBootom );
		mTop.setVisibility( View.INVISIBLE );
		addView( mTop , params );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableNewsFoldableKey() , FavoriteConfigString.isEnableNewsFoldableDefaultValue() ) )
		{
			if( !FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getNewsDefaultExpandKey() , FavoriteConfigString.isEnableNewsDefaultValue() ) )
			{
				if( spinner != null )
				{
					spinner.setLevel( 10000 );
					TextView title = (TextView)findViewById( NEWS_ID );
					title.setCompoundDrawables( null , null , spinner , null );
				}
				mMain.setVisibility( View.INVISIBLE );
			}
		}
		refreshNewsIfNotRresh( pluginContext );//刷新新闻放到滑动这页再开始刷新
	}
	
	public void init(
			Context context )
	{
		TitleTextSize = Tools.dip2px( context , TitleTextSize );
		horizonMargin = (int)getContext().getResources().getDimension( R.dimen.title_padding_lift_right );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() ) )
		{
			TITLE_PADDING_TOP = (int)getContext().getResources().getDimension( R.dimen.title_simple_launcher_padding_top );
			TITLE_PADDING_BOTTOM = (int)getContext().getResources().getDimension( R.dimen.title_simple_launcher_padding_bottom );
			TOP_SIZE = Tools.dip2px( context , 50 );
		}
		else
		{
			TITLE_PADDING_TOP = (int)getContext().getResources().getDimension( R.dimen.title_padding_top );
			TITLE_PADDING_BOTTOM = (int)getContext().getResources().getDimension( R.dimen.title_padding_bottom );
			TOP_SIZE = Tools.dip2px( context , TOP_SIZE );
		}
		NETERROR_IMAGE = Tools.dip2px( context , NETERROR_IMAGE );
		scrollTopMarginBootom = (int)getContext().getResources().getDimension( R.dimen.scrolltop_bottom_margin );
	}
	
	public FrameLayout getHeaderView(
			Context context )
	{
		LayoutInflater inflater = LayoutInflater.from( context ).cloneInContext( context );
		FrameLayout mLayout = (FrameLayout)inflater.inflate( R.layout.news_layout , null );
		//		mLayout.setBackgroundColor( 0xffff0000 );
		mCollapseTitle = getCollapseHeaderView( mLayout , context );
		mExpandTitle = getExpandHeaderView( mLayout , context );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableAppsKey() , FavoriteConfigString.isEnableAppsDefaultValue() )//
				|| FavoritesManager.getInstance().isNearbyShow( mContext ) )
		{
			mRatio = 1;
			mExpandTitle.setVisibility( View.GONE );
			mCollapseTitle.setVisibility( View.VISIBLE );
		}
		else
		{
			mRatio = 0;
			mExpandTitle.setVisibility( View.VISIBLE );
			mCollapseTitle.setVisibility( View.GONE );
		}
		return mLayout;
	}
	
	public View getExpandHeaderView(
			FrameLayout parent ,
			Context context )
	{
		mRefresh = (ImageView)parent.findViewById( R.id.refresh );
		refreshDrawable = context.getResources().getDrawable( R.drawable.refresh_rotate );
		int height = mContext.getResources().getDimensionPixelSize( R.dimen.news_refresh_icon_size );
		refreshDrawable.setBounds( 0 , 0 , height , height );
		mRefresh.setImageDrawable( refreshDrawable );
		mRefresh.setOnClickListener( this );
		mScrollView = (HorizontalScrollView)parent.findViewById( R.id.horizonScrollView );
		mScrollView.setHorizontalScrollBarEnabled( false );
		mScrollView.setOverScrollMode( OVER_SCROLL_NEVER );
		mCategoryGroup = (RadioGroup)parent.findViewById( R.id.radiogroup );
		//		mCategoryGroup.setPadding( 0 , TITLE_PADDING_TOP , 0 , TITLE_PADDING_BOTTOM );
		return parent.findViewById( R.id.expand_title );
	}
	
	public View getCollapseHeaderView(
			FrameLayout parent ,
			Context context )
	{
		FavoritesConfig config = FavoritesManager.getInstance().getConfig();
		boolean isSimpleLauncher = config.getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() );
		View layout = parent.findViewById( R.id.collapse_title );
		TextView title = (TextView)parent.findViewById( R.id.news_title );
		title.setIncludeFontPadding( false );
		title.setText( R.string.title_news );
		//		title.setTextSize( TitleTextSize );
		title.setId( NEWS_ID );
		if( isSimpleLauncher )
		{
			title.setTextSize( TypedValue.COMPLEX_UNIT_PX , (int)getContext().getResources().getDimension( R.dimen.favorites_simple_launcher_title_text_size ) );
		}
		else
		{
			title.setTextSize( TypedValue.COMPLEX_UNIT_PX , (int)getContext().getResources().getDimension( R.dimen.favorites_title_text_size ) );
		}
		if( isSimpleLauncher || FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
		{
			title.setTextColor( getContext().getResources().getColor( R.color.favorites_text_color_s5 ) );
		}
		else
		{
			title.setTextColor( getContext().getResources().getColor( R.color.favorites_text_color ) );
		}
		//		title.setGravity( Gravity.CENTER_VERTICAL );
		TextPaint paint = title.getPaint();
		FontMetrics fm = paint.getFontMetrics();
		if( config.getBoolean( FavoriteConfigString.getEnableNewsFoldableKey() , FavoriteConfigString.isEnableNewsFoldableDefaultValue() ) )
		{
			int height = (int)( fm.descent - fm.ascent );
			spinner = context.getResources().getDrawable( R.drawable.spinner_rotate );
			spinner.setBounds( 0 , 0 , height , height );
			title.setCompoundDrawables( null , null , spinner , null );
			title.setOnClickListener( this );
		}
		return layout;
	}
	
	public void updateCountry(
			String name )
	{
		for( String categoryId : mNewsList.keySet() )
		{
			ArrayList<NewsItem> list = mNewsList.get( categoryId );
			list.clear();
			mViewPager.notifyDataSetChanged( categoryId );
		}
		mNewsList.clear();
		pageviewLists.clear();
		mViewPagerAdapter.notifyDataSetChanged();//数据变化，界面也要同步修改，否则出现越界
		mCategoryGroup.removeAllViews();
		if( mModel != null )
		{
			mModel.updateCountry( name );
		}
	}
	
	public View getNewsContent()
	{
		RelativeLayout layout = new RelativeLayout( mContext );
		mViewPager = new NewsViewPager( mContext , pageviewLists );
		mViewPagerAdapter = new ViewPagerAdapter( mViewPager , pageviewLists );
		mViewPager.setAdapter( mViewPagerAdapter );
		mViewPager.addOnPageChangeListener( new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(
					int index )
			{
				// TODO Auto-generated method stub
				if( mCategoryGroup != null )
				{
					mCategoryGroup.check( RADIO_BUTTON_ID_START + index );
					if( index == 0 )
					{
						if( mScrollView.getScrollX() != 0 )
						{
							mScrollView.scrollTo( 0 , 0 );
						}
					}
					else
					{
						if( index > 0 )
						{
							View view = mCategoryGroup.getChildAt( index - 1 );
							if( view.getX() + mScrollView.getWidth() < mCategoryGroup.getWidth() )
							{
								mScrollView.scrollTo( (int)view.getX() , 0 );
							}
							else
							{
								mScrollView.scrollTo( mCategoryGroup.getWidth() - mScrollView.getWidth() , 0 );
							}
						}
					}
				}
				if( mViewPager.getCurrentPage() != null && mViewPager.getCurrentPage().getCurrentNewsCount() == 0 )
				{
					if( mModel != null )
					{
						mModel.newsRefresh( mViewPager.getCurrentPage().getCategoryId() );
					}
				}
			}
			
			@Override
			public void onPageScrolled(
					int arg0 ,
					float arg1 ,
					int arg2 )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onPageScrollStateChanged(
					int arg0 )
			{
				// TODO Auto-generated method stub
			}
		} );
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.MATCH_PARENT );
		layout.addView( mViewPager , params );
		//		mProgressBar = new ProgressBar( mContext );
		//		mProgressBar.setVisibility( View.GONE );
		//		params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
		//		params.addRule( RelativeLayout.CENTER_HORIZONTAL );
		//		layout.addView( mProgressBar , params );
		return layout;
	}
	
	public View getCategoryError(
			Context context )
	{
		RelativeLayout layout = new RelativeLayout( context );
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
		params.addRule( CENTER_IN_PARENT );
		TextView textView = new TextView( context );
		textView.setText( R.string.getcategory_failed );
		textView.setGravity( Gravity.CENTER );
		textView.setIncludeFontPadding( false );
		textView.setTextSize( TypedValue.COMPLEX_UNIT_PX , (int)getContext().getResources().getDimension( R.dimen.news_getcategroy_failed_text_size ) );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
		{
			textView.setTextColor( getContext().getResources().getColor( R.color.favorites_icon_text_color_s5 ) );
			layout.setBackgroundColor( getContext().getResources().getColor( R.color.item_bg_color_s5 ) );
		}
		else
		{
			textView.setTextColor( getContext().getResources().getColor( R.color.favorites_text_color ) );
			layout.setBackgroundColor( getContext().getResources().getColor( R.color.item_bg_color ) );
		}
		textView.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				// TODO Auto-generated method stub
				if( mModel != null )
				{
					mModel.newsRefresh( null );
				}
				if( mCategoryFailed != null )
				{
					mCategoryFailed.setVisibility( View.GONE );
				}
			}
		} );
		layout.addView( textView , params );
		return layout;
	}
	
	public View getNetworkErrorView(
			Context context )
	{
		WindowManager wm = (WindowManager)context.getSystemService( Context.WINDOW_SERVICE );
		int height = wm.getDefaultDisplay().getHeight();
		LinearLayout error = new LinearLayout( context );
		error.setOrientation( LinearLayout.VERTICAL );
		LinearLayout.LayoutParams errorLayoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT , height );
		error.setLayoutParams( errorLayoutParams );
		LinearLayout linear = new LinearLayout( context );
		linear.setOrientation( LinearLayout.VERTICAL );
		ImageView img = new ImageView( context );
		img.setImageResource( R.drawable.network_error );
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT , NETERROR_IMAGE );
		params.gravity = Gravity.CENTER_HORIZONTAL;
		linear.addView( img );
		TextView textView = new TextView( context );
		textView.setText( R.string.internet_err );
		//		textView.setTextSize( Tools.dip2px( context , 8 ) );
		textView.setGravity( Gravity.CENTER );
		textView.setIncludeFontPadding( false );
		textView.setTextSize( TypedValue.COMPLEX_UNIT_PX , (int)getContext().getResources().getDimension( R.dimen.favorites_title_text_size ) );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
		{
			textView.setTextColor( getContext().getResources().getColor( R.color.favorites_icon_text_color_s5 ) );
		}
		else
		{
			textView.setTextColor( getContext().getResources().getColor( R.color.favorites_text_color ) );
		}
		params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT , NETERROR_IMAGE );
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.topMargin = TITLE_PADDING_TOP;
		linear.addView( textView );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
		{
			linear.setBackgroundColor( getContext().getResources().getColor( R.color.item_bg_color_s5 ) );
		}
		else
		{
			linear.setBackgroundColor( getContext().getResources().getColor( R.color.item_bg_color ) );
		}
		linear.setPadding( 0 , Tools.dip2px( context , 15 ) , 0 , Tools.dip2px( context , 40 ) );
		linear.addView( error );
		return linear;
	}
	
	@Override
	public void onClick(
			View v )
	{
		// TODO Auto-generated method stub
		switch( v.getId() )
		{
			case R.id.refresh:
				if( mModel != null )
				{
					if( mViewPager.getCurrentPage() != null )
					{
						mModel.newsRefresh( mViewPager.getCurrentPage().getCategoryId() );
					}
					else
					{
						mModel.newsRefresh( null );
					}
					FavoritesConfig config = FavoritesManager.getInstance().getConfig();
					if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
					{
						MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "freshnewsrefresh_click" );
					}
					try
					{
						StatisticsExpandNew.onCustomEvent(
								FavoritesManager.getInstance().getContainerContext() ,
								"freshnewsrefresh_click" ,
								FavoritesPlugin.SN ,
								FavoritesPlugin.APPID ,
								CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName ,
								FavoritesPlugin.UPLOAD_VERSION + "" ,
								null );
					}
					catch( NoSuchMethodError e )
					{
						try
						{
							StatisticsExpandNew.onCustomEvent(
									FavoritesManager.getInstance().getContainerContext() ,
									"freshnewsrefresh_click" ,
									FavoritesPlugin.SN ,
									FavoritesPlugin.APPID ,
									CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
									FavoritesPlugin.PRODUCTTYPE ,
									FavoritesPlugin.PluginPackageName );
						}
						catch( NoSuchMethodError e1 )
						{
							StatisticsExpandNew.onCustomEvent(
									FavoritesManager.getInstance().getContainerContext() ,
									"freshnewsrefresh_click" ,
									FavoritesPlugin.PRODUCTTYPE ,
									FavoritesPlugin.PluginPackageName );
						}
					}
				}
				break;
			case TOP_ID:
				mViewPager.moveCurrentPageToTop();
				FavoritesConfig config = FavoritesManager.getInstance().getConfig();
				if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
				{
					MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "back_on_top_click" );
				}
				try
				{
					StatisticsExpandNew.onCustomEvent(
							FavoritesManager.getInstance().getContainerContext() ,
							"back_on_top_click" ,
							FavoritesPlugin.SN ,
							FavoritesPlugin.APPID ,
							CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName ,
							FavoritesPlugin.UPLOAD_VERSION + "" ,
							null );
				}
				catch( NoSuchMethodError e )
				{
					try
					{
						StatisticsExpandNew.onCustomEvent(
								FavoritesManager.getInstance().getContainerContext() ,
								"back_on_top_click" ,
								FavoritesPlugin.SN ,
								FavoritesPlugin.APPID ,
								CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName );
					}
					catch( NoSuchMethodError e1 )
					{
						StatisticsExpandNew
								.onCustomEvent( FavoritesManager.getInstance().getContainerContext() , "back_on_top_click" , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName );
					}
				}
				break;
			case NEWS_ID:
				if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableNewsFoldableKey() , FavoriteConfigString.isEnableNewsFoldableDefaultValue() ) )
				{
					if( mMain != null )
					{
						if( mMain.getVisibility() == View.VISIBLE )
						{
							if( v instanceof TextView && spinner != null )
							{
								spinner.setLevel( 10000 );
								( (TextView)v ).setCompoundDrawables( null , null , spinner , null );
							}
							mMain.setVisibility( View.INVISIBLE );
							FavoritesManager.getInstance().expandFavorites();
						}
						else
						{
							if( v instanceof TextView && spinner != null )
							{
								spinner.setLevel( 0 );
								( (TextView)v ).setCompoundDrawables( null , null , spinner , null );
							}
							mMain.setVisibility( View.VISIBLE );
						}
					}
				}
				break;
		}
	}
	
	public void refreshAnimation(
			final String categoryId ,
			final int state )
	{
		mHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( rotateAnimation != null )
				{
					rotateAnimation.cancel();
				}
				if( state == END_REFRESH )
				{
					mViewPager.endRefresh( categoryId );
				}
				else if( state == START_REFRESH )
				{
					if( mCategoryFailed != null && mCategoryFailed.getVisibility() == View.VISIBLE )
					{
						mCategoryFailed.setVisibility( View.GONE );
					}
					boolean result = mViewPager.startRefresh( categoryId );//说明viewpager中的某个子显示了刷新动画，则返回true，返回false则最上层处理
					Log.v( "news" , "refreshAnimation result = " + result + " " + mNewsList.size() + " categoryId = " + categoryId );
					if( rotateAnimation == null )
					{
						rotateAnimation = ValueAnimator.ofInt( 0 , 10000 );
						rotateAnimation.setInterpolator( new LinearInterpolator() );
						rotateAnimation.setDuration( 500 );
						rotateAnimation.setRepeatCount( Animation.INFINITE );
						rotateAnimation.addUpdateListener( new AnimatorUpdateListener() {
							
							@Override
							public void onAnimationUpdate(
									ValueAnimator animation )
							{
								// TODO Auto-generated method stub
								if( refreshDrawable != null )
								{
									int value = (Integer)animation.getAnimatedValue();
									refreshDrawable.setLevel( value );
								}
							}
						} );
					}
					rotateAnimation.start();
				}
			}
		} );
	}
	
	public void endFetch(
			final String categoryId )
	{
		mHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				mViewPager.endFetch( categoryId );
			}
		} );
	}
	
	public void updateNews(
			final String categoryId ,
			final int state )
	{
		mHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( state == Callbacks.SUCCESS )
				{
					if( mViewPager.getVisibility() != View.VISIBLE )
						mViewPager.setVisibility( View.VISIBLE );
					hideNetworkErrorView();
					mViewPager.notifyDataSetChanged( categoryId );
					Log.v( "news" , "updateNews success mCategoryGroup.getCheckedRadioButtonId() = " + mCategoryGroup.getCheckedRadioButtonId() + " categoryId = " + categoryId );
				}
				else if( state == Callbacks.STOP )
				{
					if( categoryId != null )
					{
						mViewPager.endRefresh( categoryId );
						mViewPager.endFetch( categoryId );
					}
				}
				else
				{
					Log.v( "news" , "updateNews state = " + state );
					int msgId = R.string.request_time_out;
					if( state == Callbacks.NO_UPDATE_NEWS )
					{
						msgId = R.string.no_update_news;
					}
					else if( state == Callbacks.NO_MORE_NEWS )
					{
						msgId = R.string.no_more_news;
					}
					Toast.makeText( mContext , mContext.getResources().getString( msgId ) , Toast.LENGTH_LONG ).show();
				}
				if( !FavoritesManager.getInstance().isLoadFinish() )
				{
					int VERSION = FavoritesManager.getInstance().getConfig().getInt( FavoriteConfigString.getHostVersionCodeKey() , FavoriteConfigString.getHostVersionCodeValue() );//cheyingkun add	//酷生活编辑失败,获取host版本
					if( VERSION >= 12 )
					{
						Log.v( "lvjangbin" , "init all view 2" );
						if( FavoritesManager.getInstance().getContainerContext() instanceof IFavoritesReady )
						{
							Log.v( "lvjangbin" , "init all view 3" );
							( (IFavoritesReady)FavoritesManager.getInstance().getContainerContext() ).onFavoritesReady();
						}
					}
				}
			}
		} );
	}
	
	public void showNetworkError(
			final String categoryId )
	{
		mHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( mViewPagerAdapter.getCount() > 0 )
				{
					Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
				}
				else
				{
					mViewPager.setVisibility( View.GONE );
					if( mCategoryFailed != null )
					{
						mCategoryFailed.setVisibility( View.GONE );
					}
					showNetworkErrorView();
				}
				if( rotateAnimation != null )
				{
					rotateAnimation.cancel();
				}
				endFetch( categoryId );
				if( !FavoritesManager.getInstance().isLoadFinish() )
				{
					int VERSION = FavoritesManager.getInstance().getConfig().getInt( FavoriteConfigString.getHostVersionCodeKey() , FavoriteConfigString.getHostVersionCodeValue() );//cheyingkun add	//酷生活编辑失败,获取host版本
					if( VERSION >= 12 )
					{
						Log.v( "lvjangbin" , "init all view 2" );
						if( FavoritesManager.getInstance().getContainerContext() instanceof IFavoritesReady )
						{
							Log.v( "lvjangbin" , "init all view 3" );
							( (IFavoritesReady)FavoritesManager.getInstance().getContainerContext() ).onFavoritesReady();
						}
					}
				}
			}
		} );
	}
	
	public void networkChanged(
			final int type )
	{
		Log.v( "news" , "networkChanged = " + type );
		if( type > 0 )
		{
			if( networkError == null || ( networkError != null && networkError.getVisibility() != View.VISIBLE ) )
			{
				if( mViewPager.getCurrentPage() != null && mViewPager.getCurrentPage().getCurrentNewsCount() == 0 )//当有网络时，当前页没有一条新闻，主动刷新
				{
					if( mModel != null )
					{
						mModel.newsRefresh( mViewPager.getCurrentPage().getCategoryId() );
					}
				}
				if( mModel != null )
					mModel.CheckCountryIfChanged();
			}
			else
			{
				mHandler.post( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						hideNetworkErrorView();
						mViewPager.setVisibility( View.VISIBLE );
						if( mModel != null )
							mModel.newsRefresh( null );
					}
				} );
			}
		}
	}
	
	public NewsViewPager getViewPager()
	{
		return mViewPager;
	}
	
	public void newsSourceChanged(
			final int source )
	{
		mHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( mModel != null )//这里不需要比较，都交给下面的方法执行
				{
					mModel.updateNewsSource( source );
				}
			}
		} );
	}
	
	public void adPlaceChanged(
			String place )
	{
		if( mModel != null )
		{
			mModel.adplaceChanged( place );
		}
	}
	
	public void setTextName(
			final String name )
	{
		// TODO Auto-generated method stub
		mHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				//				TextView expand_country = (TextView)mHeader.findViewById( COUNTRY_ID );//国家已经不显示了
				//				if( expand_country != null )
				//				{
				//					if( mMain.getVisibility() == View.VISIBLE )
				//					{
				//						expand_country.setVisibility( View.VISIBLE );
				//					}
				//					Log.v( "news" , "expand_country = " + expand_country );
				//					expand_country.setText( name.split( "&" )[0] );
				//				}
			}
		} );
	}
	
	//cheyingkun add start	//添加折叠和显示更多交互动画。
	/**
	 * 显示更多、折叠时，新闻区域的动画
	 * @return
	 */
	public ObjectAnimator getStartShowMoreOrFoldingAnim(
			float changeY )
	{
		if( changeY == 0// 
				|| FavoritesManager.getInstance().isNewsExpandedMode() //cheyingkun add start	//新闻全屏模式时添加删除联系人(应用),不改变新闻y值
		)
		{
			return null;
		}
		PropertyValuesHolder newsY = PropertyValuesHolder.ofFloat( "y" , this.getY() , this.getY() + changeY );
		ObjectAnimator newsAnim = new ObjectAnimator();
		newsAnim.setTarget( this );
		newsAnim.setValues( newsY );
		return newsAnim;
	}
	
	public void initViewsYForShowMoreAndFolding(
			float changeY )
	{
		//cheyingkun add start	//新闻全屏模式时添加删除联系人(应用),不改变新闻y值
		if( changeY == 0 || FavoritesManager.getInstance().isNewsExpandedMode() )
		{
			return;
		}
		//cheyingkun add end
		//新闻
		if( this != null )
		{
			this.setY( this.getY() + changeY );
		}
	}
	
	//cheyingkun add end
	//zhujieping add start
	public synchronized void refreshNewsIfNotRresh(
			Context pluginContext )
	{
		if( mModel == null )
		{
			mModel = new NewsModel( pluginContext , mQueue , mNewsList , this );//初始化中，调用到newsdata中refreshnews,刷新新闻
		}
	}
	
	//zhujieping add end
	public void updateCategory(
			int state ,
			final ArrayList<CategoryItem> list )
	{
		if( state == Callbacks.FAIL )
		{
			mHandler.post( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					if( mCategoryFailed != null )
					{
						mCategoryFailed.setVisibility( View.VISIBLE );
					}
					if( rotateAnimation != null )
					{
						rotateAnimation.cancel();
					}
				}
			} );
		}
		else if( state == Callbacks.SUCCESS )
		{
			if( mCategoryGroup != null )
			{
				if( list == null || list.size() == 0 )
				{
					return;
				}
				mHandler.post( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						Log.v( "news" , "updateCategory " );
						if( mCategoryFailed != null )
						{
							mCategoryFailed.setVisibility( View.GONE );
						}
						mCategoryGroup.removeAllViews();
						mNewsList.clear();
						pageviewLists.clear();
						int max_width = mContext.getResources().getDimensionPixelSize( R.dimen.favorites_category_title_max_width );
						int padding = mContext.getResources().getDimensionPixelSize( R.dimen.favorites_category_title_padding );
						for( int index = 0 ; index < list.size() ; index++ )
						{
							CategoryItem item = list.get( index );
							RadioButton button = new RadioButton( mContext );
							button.setButtonDrawable( android.R.color.transparent );
							button.setCompoundDrawables( null , null , null , null );
							button.setBackgroundResource( R.drawable.category_item_selector );
							if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() )//
									|| FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
							{
								button.setTextColor( getContext().getResources().getColorStateList( R.color.category_title_selector_s5 ) );
							}
							else
							{
								button.setTextColor( getContext().getResources().getColorStateList( R.color.category_title_selector ) );
							}
							button.setText( item.getCategoryName() );
							button.setTag( item.getCategoryId() );
							button.setSingleLine();
							button.setEllipsize( TruncateAt.MARQUEE );
							if( index == 0 )
							{
								button.setChecked( true );
							}
							LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.WRAP_CONTENT );
							boolean isSimpleLauncher = FavoritesManager.getInstance().getConfig()
									.getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() );
							if( isSimpleLauncher )
							{
								button.setTextSize( TypedValue.COMPLEX_UNIT_PX , (int)getContext().getResources().getDimension( R.dimen.favorites_simple_launcher_title_text_size ) );
							}
							else
							{
								button.setTextSize( TypedValue.COMPLEX_UNIT_PX , (int)getContext().getResources().getDimension( R.dimen.favorites_title_text_size ) );
							}
							button.setPadding( padding , TITLE_PADDING_TOP , padding , TITLE_PADDING_BOTTOM );
							button.setMaxWidth( max_width );
							button.setGravity( Gravity.CENTER_HORIZONTAL );
							params.gravity = Gravity.CENTER_HORIZONTAL;
							button.setId( RADIO_BUTTON_ID_START + index );
							button.setOnClickListener( new View.OnClickListener() {
								
								@Override
								public void onClick(
										View v )
								{
									// TODO Auto-generated method stub
									Log.v( "news" , "button onclick " + mViewPager.getCurrentItem() + " v.getId() = " + v.getId() );
									if( mViewPager.getCurrentItem() == ( v.getId() - RADIO_BUTTON_ID_START ) )
									{
										mViewPager.moveCurrentPageToTop();
										if( mModel != null )
											mModel.newsRefresh( v.getTag().toString() );
									}
									else
									{
										mViewPager.setCurrentItem( v.getId() - RADIO_BUTTON_ID_START );
									}
								}
							} );
							mCategoryGroup.addView( button , params );
							ArrayList<NewsItem> onecategorylist = new ArrayList<NewsItem>();
							mNewsList.put( item.getCategoryId() , onecategorylist );
							LayoutInflater inflater = LayoutInflater.from( mContext ).cloneInContext( mContext );
							PullToRefreshLayout newsListView = (PullToRefreshLayout)inflater.inflate( R.layout.pulltorefresh , null );
							newsListView.setListData( mModel , mQueue , onecategorylist , item.getCategoryId() , new onNotifyClickListener() {
								
								@Override
								public void onClick(
										View v )
								{
									// TODO Auto-generated method stub
									if( mViewPager != null && mModel != null )
									{
										mViewPager.moveCurrentPageToTop();
										if( mViewPager.getCurrentPage() != null )
										{
											mModel.newsRefresh( mViewPager.getCurrentPage().getCategoryId() );
										}
										else
										{
											mModel.newsRefresh( null );
										}
									}
									else
									{
										if( mModel != null )
											mModel.newsRefresh( null );
									}
								}
							} );
							newsListView.setScrollListener( onScrollListener );
							pageviewLists.add( newsListView );
							newsListView.setOnRefreshListener( new OnRefreshListener() {
								
								@Override
								public void onRefresh(
										PullToRefreshLayout pullToRefreshLayout )
								{
									// TODO Auto-generated method stub
									if( mModel != null )
									{
										mModel.newsRefresh( pullToRefreshLayout.getCategoryId() );
									}
								}
								
								@Override
								public void onLoadMore(
										PullToRefreshLayout pullToRefreshLayout )
								{
									// TODO Auto-generated method stub
									if( mModel != null )
									{
										mModel.newsFetch( pullToRefreshLayout.getCategoryId() );
									}
								}
							} );
						}
						Log.v( "news" , "mCategoryGroup = " + mCategoryGroup.getCheckedRadioButtonId() );
						if( mViewPagerAdapter != null )
						{
							mViewPagerAdapter.notifyDataSetChanged();
							if( mNewsList.size() > 0 )
								mViewPager.setCurrentItem( 0 );
						}
					}
				} );
			}
		}
	}
	
	private PullToRefreshLayout.OnScrollListener onScrollListener = new PullToRefreshLayout.OnScrollListener() {
		
		@Override
		public void showTopView(
				String categoryId )
		{
			// TODO Auto-generated method stub
			if( mViewPager.isCurrentPage( categoryId ) )
			{
				if( mTop != null && mTop.getVisibility() != View.VISIBLE && ( valueAnimation == null || !valueAnimation.isRunning() ) )
				{
					setTopViewShow( true );
				}
			}
		}
		
		@Override
		public void hideTopView(
				String categoryId )
		{
			// TODO Auto-generated method stub
			if( mViewPager.isCurrentPage( categoryId ) )
			{
				if( mTop != null && ( mTop.getVisibility() == View.VISIBLE ) && ( valueAnimation == null || !valueAnimation.isRunning() ) )
				{
					setTopViewShow( false );
				}
			}
		}
	};
	
	private void setTopViewShow(
			final boolean isShow )
	{
		if( isShow )
			valueAnimation = ValueAnimator.ofFloat( getHeight() , getHeight() - ( TOP_SIZE + scrollTopMarginBootom * 2 ) );
		else
			valueAnimation = ValueAnimator.ofFloat( getHeight() - ( TOP_SIZE + scrollTopMarginBootom * 2 ) , getHeight() );
		valueAnimation.setDuration( alternateDuration );
		valueAnimation.start();
		valueAnimation.addUpdateListener( new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(
					ValueAnimator animation )
			{
				if( mTop.getVisibility() != View.VISIBLE )
					mTop.setVisibility( View.VISIBLE );
				float yValue2 = (Float)animation.getAnimatedValue();
				mTop.setY( yValue2 );
			}
		} );
		valueAnimation.addListener( new AnimatorListener() {
			
			@Override
			public void onAnimationStart(
					Animator animation )
			{
				// TODO Auto-generated method stub
				mTop.setVisibility( View.VISIBLE );
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
				// TODO Auto-generated method stub
				if( !isShow )
					mTop.setVisibility( View.GONE );
			}
			
			@Override
			public void onAnimationCancel(
					Animator animation )
			{
				// TODO Auto-generated method stub
			}
		} );
	}
	
	public void update(
			float ratio ,
			float alpha ,
			float scale ,
			float searchHeight )
	{
		mRatio = ratio;
		if( mCollapseTitle != null )
		{
			mCollapseTitle.setScaleX( scale );
			mCollapseTitle.setScaleY( scale );
			mCollapseTitle.setPivotX( getWidth() / 2.0f );
			mCollapseTitle.setPivotY( searchHeight + mCollapseTitle.getHeight() );
			mCollapseTitle.setAlpha( alpha );
			if( ratio > 0 )
			{
				mCollapseTitle.setVisibility( View.VISIBLE );
			}
			else
			{
				mCollapseTitle.setVisibility( View.GONE );
			}
		}
		if( mExpandTitle != null )
		{
			if( ratio < 1 )
			{
				mExpandTitle.setVisibility( View.VISIBLE );
				mExpandTitle.setY( ratio * mCollapseTitle.getHeight() );
			}
			else
			{
				mExpandTitle.setVisibility( View.GONE );
			}
		}
		if( ratio == 0 )
		{
			enterTime = System.currentTimeMillis();
			FavoritesConfig config = FavoritesManager.getInstance().getConfig();
			if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
			{
				MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "news_classification_in" );
			}
			try
			{
				StatisticsExpandNew.onCustomEvent(
						FavoritesManager.getInstance().getContainerContext() ,
						"news_classification_in" ,
						FavoritesPlugin.SN ,
						FavoritesPlugin.APPID ,
						CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
						FavoritesPlugin.PRODUCTTYPE ,
						FavoritesPlugin.PluginPackageName ,
						FavoritesPlugin.UPLOAD_VERSION + "" ,
						null );
			}
			catch( NoSuchMethodError e )
			{
				try
				{
					StatisticsExpandNew.onCustomEvent(
							FavoritesManager.getInstance().getContainerContext() ,
							"news_classification_in" ,
							FavoritesPlugin.SN ,
							FavoritesPlugin.APPID ,
							CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName );
				}
				catch( NoSuchMethodError e1 )
				{
					StatisticsExpandNew.onCustomEvent(
							FavoritesManager.getInstance().getContainerContext() ,
							"news_classification_in" ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName );
				}
			}
		}
		else if( ratio == 1 )
		{
			if( enterTime > 0 )
			{
				HashMap<String , String> map = new HashMap<String , String>();
				map.put( "stay_time" , ( System.currentTimeMillis() - enterTime ) + "" );
				FavoritesConfig config = FavoritesManager.getInstance().getConfig();
				if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
				{
					MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "stay_news_classification" , map );
				}
				try
				{
					StatisticsExpandNew.onCustomEvent(
							FavoritesManager.getInstance().getContainerContext() ,
							"stay_news_classification" ,
							FavoritesPlugin.SN ,
							FavoritesPlugin.APPID ,
							CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName ,
							FavoritesPlugin.UPLOAD_VERSION + "" ,
							new JSONObject( map ) );
				}
				catch( NoSuchMethodError e )
				{
					try
					{
						StatisticsExpandNew.onCustomEvent(
								FavoritesManager.getInstance().getContainerContext() ,
								"stay_news_classification" ,
								FavoritesPlugin.SN ,
								FavoritesPlugin.APPID ,
								CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName ,
								new JSONObject( map ) );
					}
					catch( NoSuchMethodError e1 )
					{
						StatisticsExpandNew.onCustomEvent(
								FavoritesManager.getInstance().getContainerContext() ,
								"stay_news_classification" ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName ,
								new JSONObject( map ) );
					}
				}
				enterTime = -1;
			}
		}
	}
	
	//zhujieping add start
	//	@Override
	//	protected void dispatchDraw(
	//			Canvas canvas )
	//	{
	//		// TODO Auto-generated method stub
	//		super.dispatchDraw( canvas );
	//		if( mModel == null )
	//		{
	//			getLocationOnScreen( location );
	//			if( location[0] > ( -getWidth() + 1 ) && location[0] < getWidth() - 1 )//zhujieping add，该view显示到屏幕上才会被调用，这边防止因为-1屏在未加载成功就滑动桌面，导致onshow方法没有执行，新闻不刷新
	//			{
	//				refreshNewsIfNotRresh( mContext );
	//			}
	//		}
	//	}
	//zhujieping add end
	@Override
	public void onStick(
			boolean stick )
	{
		// TODO Auto-generated method stub
		//		this.collapse = stick;
	}
	
	@Override
	public boolean hasScrollToHead()
	{
		// TODO Auto-generated method stub
		if( mViewPager != null )
		{
			return mViewPager.hasScrollToHead();
		}
		return false;
	}
	
	@Override
	public boolean canScrollToHead()
	{
		// TODO Auto-generated method stub
		if( getVisibility() != View.VISIBLE )
		{
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isNeedHandlerTouchEvent(
			float x ,
			float y )
	{
		// TODO Auto-generated method stub
		mScrollView.getLocationOnScreen( location );
		if( x > location[0] && x < location[0] + mScrollView.getWidth() && y > location[1] && y < location[1] + mScrollView.getHeight() )
		{
			return true;
		}
		return false;
	}
	
	public void NotifyCountryChanged(
			final String countryName ,
			final String countryCode )
	{
		Log.v( "news" , "NotifyCountryChanged countryName = " + countryName + " countryCode = " + countryCode );
		if( TextUtils.isEmpty( countryCode ) )
		{
			return;
		}
		post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				final Dialog mDialog = new Dialog( mContext , R.style.Disclaimer_dialog );
				View view = LayoutInflater.from( mContext ).cloneInContext( mContext ).inflate( R.layout.notify_country_changed , null );
				TextView message = (TextView)view.findViewById( R.id.notifyMessage );
				message.setText( mContext.getResources().getString( R.string.change_country_notify_message , countryName ) );
				Button ok = (Button)view.findViewById( R.id.changecountry_ok );
				Button cancel = (Button)view.findViewById( R.id.changecountry_cancel );
				ok.setOnClickListener( new OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						// TODO Auto-generated method stub
						updateCountry( countryName + "&" + countryCode );
						mDialog.cancel();
					}
				} );
				cancel.setOnClickListener( new OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						// TODO Auto-generated method stub
						mDialog.cancel();
					}
				} );
				mDialog.setCanceledOnTouchOutside( false );
				mDialog.setContentView( view );
				mDialog.show();
				Window window = mDialog.getWindow();
				WindowManager.LayoutParams lp = window.getAttributes();
				lp.gravity = Gravity.CENTER;
				lp.width = mContext.getResources().getDisplayMetrics().widthPixels - 2 * mContext.getResources().getDimensionPixelSize( R.dimen.favorites_dialog_x_margin );//宽高可设置具体大小
				lp.height = LayoutParams.WRAP_CONTENT;
				mDialog.getWindow().setAttributes( lp );
			}
		} );
	}
	
	private void showNetworkErrorView()//显示的时候才加入到父view中
	{
		if( networkError == null )
			networkError = getNetworkErrorView( mContext );
		if( networkError.getParent() == null )
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
			params.addRule( RelativeLayout.BELOW , HEADER_ID );
			mMain.addView( networkError , params );
		}
		networkError.setVisibility( View.VISIBLE );
	}
	
	private void hideNetworkErrorView()//不显示时，从父view中移除
	{
		if( networkError != null )
		{
			networkError.setVisibility( View.GONE );
			if( networkError.getParent() != null && networkError.getParent() instanceof ViewGroup )
			{
				( (ViewGroup)networkError.getParent() ).removeView( networkError );
			}
		}
	}
}
