package com.cooee.favorites.news;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.cooee.dynamicload.internal.DLIntent;
import com.cooee.dynamicload.internal.DLPluginManager;
import com.cooee.dynamicload.utils.DLConstants;
import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.FavoritesPlugin;
import com.cooee.favorites.R;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.news.NewsAdapter.onNotifyClickListener;
import com.cooee.favorites.news.data.NewsItem;
import com.cooee.favorites.utils.NetworkAvailableUtils;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.kmob.kmobsdk.KmobManager;
import com.kmob.kmobsdk.NativeAdData;
import com.umeng.analytics.MobclickAgent;


public class PullToRefreshLayout extends RelativeLayout
{
	
	public static final String TAG = "PullToRefreshLayout";
	// 初始状态  
	public static final int INIT = 0;
	// 释放刷新  
	public static final int RELEASE_TO_REFRESH = 1;
	// 正在刷新  
	public static final int REFRESHING = 2;
	// 释放加载  
	public static final int RELEASE_TO_LOAD = 3;
	// 正在加载  
	public static final int LOADING = 4;
	// 操作完毕  
	public static final int DONE = 5;
	// 当前状态  
	private int state = INIT;
	// 刷新回调接口  
	private OnRefreshListener mListener;
	// 刷新成功  
	public static final int SUCCEED = 0;
	// 刷新失败  
	public static final int FAIL = 1;
	// 按下Y坐标，上一个事件点Y坐标  
	private float downY , lastY;
	// 下拉的距离。注意：pullDownY和pullUpY不可能同时不为0  
	public float pullDownY = 0;
	// 上拉的距离  
	private float pullUpY = 0;
	// 释放刷新的距离  
	private float refreshDist = 200;
	// 释放加载的距离  
	private float loadmoreDist = 200;
	private MyTimer timer;
	// 回滚速度
	public float MOVE_SPEED = 8;
	// 第一次执行布局  
	private boolean isLayout = false;
	// 在刷新过程中滑动操作  
	private boolean isTouch = false;
	// 手指滑动距离与下拉头的滑动距离比，中间会随正切函数变化  
	private float radio = 2;
	// 下拉头  
	private View refreshView;
	// 正在刷新的图标  
	private View refreshingView;
	// 刷新结果：成功或失败  
	private TextView refreshStateTextView;
	// 上拉头  
	private View loadmoreView;
	// 正在加载的图标  
	private View loadingView;
	// 加载结果：成功或失败  
	private TextView loadStateTextView;
	private ListView pullableView;
	// 过滤多点触碰  
	private int mEvents;
	// 这两个变量用来控制pull的方向，如果不加控制，当情况满足可上拉又可下拉时没法下拉  
	private boolean canPullDown = true;
	private boolean canPullUp = true;
	private NewsAdapter mAdapter;
	private String categoryId = null;
	private OnScrollListener mScrollListener;
	private static int firstVisible = 10;
	/** 
	 * 执行自动回滚的handler 
	 */
	Handler updateHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			// 回弹速度随下拉距离moveDeltaY增大而增大  
			MOVE_SPEED = (float)( 8 + 5 * Math.tan( Math.PI / 2 / getMeasuredHeight() * ( pullDownY + Math.abs( pullUpY ) ) ) );
			if( !isTouch )
			{
				// 正在刷新，且没有往上推的话则悬停，显示"正在刷新..."  
				if( state == REFRESHING && pullDownY <= refreshDist )
				{
					pullDownY = refreshDist;
					timer.cancel();
					requestLayout();
					return;
				}
				else if( state == LOADING && -pullUpY <= loadmoreDist )
				{
					pullUpY = -loadmoreDist;
					timer.cancel();
					requestLayout();
					return;
				}
			}
			if( pullDownY > 0 )
				pullDownY -= MOVE_SPEED;
			else if( pullUpY < 0 )
				pullUpY += MOVE_SPEED;
			if( pullDownY < 0 )
			{
				// 已完成回弹  
				pullDownY = 0;
				// 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态  
				if( state != REFRESHING && state != LOADING )
					changeState( INIT );
				timer.cancel();
			}
			if( pullUpY > 0 )
			{
				// 已完成回弹  
				pullUpY = 0;
				// 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态  
				if( state != REFRESHING && state != LOADING )
					changeState( INIT );
				timer.cancel();
			}
			if( pullDownY == 0 && pullUpY == 0 )
			{
				changeState( INIT );
				timer.cancel();
			}
			// 刷新布局,会自动调用onLayout  
			requestLayout();
		}
	};
	
	public void setOnRefreshListener(
			OnRefreshListener listener )
	{
		mListener = listener;
	}
	
	public PullToRefreshLayout(
			Context context )
	{
		super( context );
		initView( context );
	}
	
	public PullToRefreshLayout(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		initView( context );
	}
	
	public PullToRefreshLayout(
			Context context ,
			AttributeSet attrs ,
			int defStyle )
	{
		super( context , attrs , defStyle );
		initView( context );
	}
	
	private void initView(
			Context context )
	{
		timer = new MyTimer( updateHandler );
		//		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
		//		{
		//			setBackgroundColor( getContext().getResources().getColor( R.color.item_bg_color_s5 ) );
		//		}
		//		else
		//		{
		//			setBackgroundColor( getContext().getResources().getColor( R.color.item_bg_color ) );
		//		}
	}
	
	private void hide()
	{
		timer.schedule( 25 );
	}
	
	public void setListData(
			final NewsModel mModel ,
			RequestQueue queue ,
			ArrayList<NewsItem> lists ,
			String cateId ,
			onNotifyClickListener listener )
	{
		if( pullableView == null )
			pullableView = (ListView)getChildAt( 1 );
		this.categoryId = cateId;
		mAdapter = new NewsAdapter( getContext() , queue , lists );
		mAdapter.setClickListener( listener );
		pullableView.setDivider( null );
		pullableView.setAdapter( mAdapter );
		pullableView.setOnScrollListener( new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(
					AbsListView view ,
					int scrollState )
			{
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onScroll(
					AbsListView view ,
					int firstVisibleItem ,
					int visibleItemCount ,
					int totalItemCount )
			{
				// TODO Auto-generated method stub
				if( mScrollListener != null )
				{
					if( firstVisibleItem > firstVisible )
					{
						mScrollListener.showTopView( categoryId );
					}
					else
					{
						mScrollListener.hideTopView( categoryId );
					}
				}
			}
		} );
		pullableView.setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				// TODO Auto-generated method stub
				//				// TODO Auto-generated method stub
				if( position < 0 || position >= mAdapter.getCount() )//说明数据、界面不一致，更新。
				{
					mAdapter.notifyDataSetChanged();
					return;
				}
				NewsItem item = (NewsItem)mAdapter.getItem( position );
				if( item != null )
				{
					if( item.getNewsUrl() != null )
					{
						handleStartUrl( item );
						if( mModel != null )
						{
							mModel.clickNews( item );//点击新闻，当达到一定条数时回传给今日头条
						}
						FavoritesConfig config = FavoritesManager.getInstance().getConfig();
						if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
						{
							MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "freshnews_click" );
						}
						try
						{
							StatisticsExpandNew.onCustomEvent(
									FavoritesManager.getInstance().getContainerContext() ,
									"freshnews_click" ,
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
										"freshnews_click" ,
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
										"freshnews_click" ,
										FavoritesPlugin.PRODUCTTYPE ,
										FavoritesPlugin.PluginPackageName );
							}
						}
					}
					else
					{
						if( item.getOtherInfo() != null )
						{
							try
							{
								KmobManager.onClickDone( item.getOtherInfo().getString( NativeAdData.AD_ID_TAG ) , item.getOtherInfo().toString() , true );
								FavoritesConfig config = FavoritesManager.getInstance().getConfig();
								if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
								{
									MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "Ad_click" );
								}
								StatisticsExpandNew.onCustomEvent(
										FavoritesManager.getInstance().getContainerContext() ,
										"Ad_click" ,
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
											"Ad_click" ,
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
											"Ad_click" ,
											FavoritesPlugin.PRODUCTTYPE ,
											FavoritesPlugin.PluginPackageName );
								}
							}
							catch( JSONException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		} );
	}
	
	public static void setFirstVisible(
			int first )
	{
		firstVisible = first;
	}
	
	private void handleStartUrl(
			NewsItem item )
	{
		if( !NetworkAvailableUtils.isNetworkAvailable( FavoritesManager.getInstance().getContainerContext() ) )
		{
			Context pluginContext = FavoritesManager.getInstance().getPluginContext();
			CharSequence text = pluginContext.getResources().getText( R.string.internet_err );
			Toast.makeText( FavoritesManager.getInstance().getContainerContext() , text , Toast.LENGTH_SHORT ).show();
			return;
		}
		final int uid = android.os.Process.myUid();
		if( Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && ( uid == 0 || uid == android.os.Process.SYSTEM_UID ) )//sdk>21之后，webview在android.os.Process.SYSTEM_UID和android.os.Process.ROOT_UID 这两个进程中会抛出异常，无法显示,这时打开浏览器
		{
			Uri uri = Uri.parse( item.getNewsUrl() );
			Intent intent = new Intent( Intent.ACTION_VIEW , uri );
			( (Activity)FavoritesManager.getInstance().getContainerContext() ).startActivity( intent );
		}
		else
		{
			DLIntent intent = new DLIntent( Intent.ACTION_MAIN );
			intent.setPluginPackage( FavoritesPlugin.PluginPackageName );
			intent.setPluginClass( NewsActivity.class.getName() );
			intent.putExtra( DLConstants.EXTRA_PACKAGE , FavoritesPlugin.PluginPackageName );
			intent.putExtra( DLConstants.EXTRA_CLASS , NewsActivity.class.getName() );
			intent.putExtra( "com.cooee.news.url" , item.getNewsUrl() );
			intent.putExtra( "com.cooee.news.title" , getContext().getString( R.string.news_title ) );
			if( item.getSite() != null )
			{
				intent.putExtra( "com.cooee.news.share" , item.getTitle() + item.getNewsUrl() + "(@ " + item.getSite() + ")" );
			}
			else
			{
				intent.putExtra( "com.cooee.news.share" , item.getTitle() + item.getNewsUrl() );
			}
			intent.setFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
			//			intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
			DLPluginManager.getInstance( FavoritesManager.getInstance().getContainerContext() ).startPluginActivity( FavoritesManager.getInstance().getContainerContext() , intent );
			if( FavoritesManager.getInstance().getContainerContext() instanceof Activity )
			{
				( (Activity)FavoritesManager.getInstance().getContainerContext() ).overridePendingTransition( 0 , 0 );//屏蔽系统的activity启动动画（这里直接设置无效，无法访问到plugin中的anim.xml），在activity中给view设置动画
			}
		}
	}
	
	public void notifyDataSetChanged()
	{
		if( mAdapter != null )
		{
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public void moveListViewToTop()
	{
		if( pullableView != null )
		{
			pullableView.setSelection( 0 );
		}
	}
	
	public String getCategoryId()
	{
		return categoryId;
	}
	
	public void startRefresh()
	{
	}
	
	/** 
	 * 完成刷新操作，显示刷新结果。注意：刷新完成后一定要调用这个方法 
	 */
	/** 
	 * @param refreshResult 
	 *            PullToRefreshLayout.SUCCEED代表成功，PullToRefreshLayout.FAIL代表失败 
	 */
	public void refreshFinish(
			int refreshResult )
	{
		if( refreshingView != null )
			refreshingView.setVisibility( View.GONE );
		switch( refreshResult )
		{
			case SUCCEED:
				// 刷新成功  
				break;
			case FAIL:
			default:
				// 刷新失败  
				break;
		}
		changeState( DONE );
		hide();
	}
	
	/** 
	 * 加载完毕，显示加载结果。注意：加载完成后一定要调用这个方法 
	 *  
	 * @param refreshResult 
	 *            PullToRefreshLayout.SUCCEED代表成功，PullToRefreshLayout.FAIL代表失败 
	 */
	public void loadmoreFinish(
			int refreshResult )
	{
		loadingView.setVisibility( View.GONE );
		switch( refreshResult )
		{
			case SUCCEED:
				// 加载成功  
				break;
			case FAIL:
			default:
				// 加载失败  
				break;
		}
		changeState( DONE );
		hide();
	}
	
	private void changeState(
			int to )
	{
		state = to;
		switch( state )
		{
			case INIT:
				// 下拉布局初始状态  
				refreshStateTextView.setText( R.string.pull_to_refresh );
				// 上拉布局初始状态  
				loadStateTextView.setText( R.string.pullup_to_load );
				break;
			case RELEASE_TO_REFRESH:
				// 释放刷新状态  
				refreshStateTextView.setText( R.string.release_to_refresh );
				Log.v( "news" , "RELEASE_TO_REFRESH get = " + getCategoryId() );
				break;
			case REFRESHING:
				// 正在刷新状态  
				//				refreshingView.setVisibility( View.VISIBLE );
				refreshStateTextView.setText( R.string.refreshing );
				break;
			case RELEASE_TO_LOAD:
				// 释放加载状态  
				loadStateTextView.setText( R.string.release_to_load );
				break;
			case LOADING:
				// 正在加载状态  
				//				loadingView.setVisibility( View.VISIBLE );
				loadStateTextView.setText( R.string.loading );
				break;
			case DONE:
				// 刷新或加载完毕，啥都不做  
				break;
		}
	}
	
	/** 
	 * 不限制上拉或下拉 
	 */
	private void releasePull()
	{
		canPullDown = true;
		canPullUp = true;
	}
	
	/* 
	 * （非 Javadoc）由父控件决定是否分发事件，防止事件冲突 
	 *  
	 * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent) 
	 */
	@Override
	public boolean dispatchTouchEvent(
			MotionEvent ev )
	{
		switch( ev.getActionMasked() )
		{
			case MotionEvent.ACTION_DOWN:
				downY = ev.getY();
				lastY = downY;
				//				timer.cancel();
				mEvents = 0;
				releasePull();
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_POINTER_UP:
				// 过滤多点触碰  
				mEvents = -1;
				break;
			case MotionEvent.ACTION_MOVE:
				if( mEvents == 0 )
				{
					if( canPullDown() && canPullDown && state != LOADING )
					{
						// 可以下拉，正在加载时不能下拉  
						// 对实际滑动距离做缩小，造成用力拉的感觉  
						pullDownY = pullDownY + ( ev.getY() - lastY ) / radio;
						if( pullDownY < 0 )
						{
							pullDownY = 0;
							canPullDown = false;
							canPullUp = true;
						}
						if( pullDownY > getMeasuredHeight() )
							pullDownY = getMeasuredHeight();
						if( state == REFRESHING )
						{
							// 正在刷新的时候触摸移动  
							isTouch = true;
						}
					}
					else if( canPullUp() && canPullUp && state != REFRESHING )
					{
						// 可以上拉，正在刷新时不能上拉  
						pullUpY = pullUpY + ( ev.getY() - lastY ) / radio;
						if( pullUpY > 0 )
						{
							pullUpY = 0;
							canPullDown = true;
							canPullUp = false;
						}
						if( pullUpY < -getMeasuredHeight() )
							pullUpY = -getMeasuredHeight();
						if( state == LOADING )
						{
							// 正在加载的时候触摸移动  
							isTouch = true;
						}
					}
					else
						releasePull();
				}
				else
					mEvents = 0;
				lastY = ev.getY();
				// 根据下拉距离改变比例  
				radio = (float)( 2 + 2 * Math.tan( Math.PI / 2 / getMeasuredHeight() * ( pullDownY + Math.abs( pullUpY ) ) ) );
				requestLayout();
				if( pullDownY <= refreshDist && ( state == RELEASE_TO_REFRESH || state == DONE ) )
				{
					// 如果下拉距离没达到刷新的距离且当前状态是释放刷新，改变状态为下拉刷新  
					changeState( INIT );
				}
				if( pullDownY >= refreshDist && state == INIT )
				{
					// 如果下拉距离达到刷新的距离且当前状态是初始状态刷新，改变状态为释放刷新  
					changeState( RELEASE_TO_REFRESH );
				}
				// 下面是判断上拉加载的，同上，注意pullUpY是负值  
				if( -pullUpY <= loadmoreDist && state == RELEASE_TO_LOAD )
				{
					changeState( INIT );
				}
				if( -pullUpY >= loadmoreDist && state == INIT )
				{
					changeState( RELEASE_TO_LOAD );
				}
				// 因为刷新和加载操作不能同时进行，所以pullDownY和pullUpY不会同时不为0，因此这里用(pullDownY +  
				// Math.abs(pullUpY))就可以不对当前状态作区分了  
				if( ( pullDownY + Math.abs( pullUpY ) ) > 8 )
				{
					// 防止下拉过程中误触发长按事件和点击事件  
					ev.setAction( MotionEvent.ACTION_CANCEL );
				}
				break;
			case MotionEvent.ACTION_UP:
				if( pullDownY > refreshDist || -pullUpY > loadmoreDist )
					// 正在刷新时往下拉（正在加载时往上拉），释放后下拉头（上拉头）不隐藏  
					isTouch = false;
				if( state == RELEASE_TO_REFRESH )
				{
					changeState( REFRESHING );
					// 刷新操作  
					if( mListener != null )
						mListener.onRefresh( this );
				}
				else if( state == RELEASE_TO_LOAD )
				{
					changeState( LOADING );
					// 加载操作  
					if( mListener != null )
						mListener.onLoadMore( this );
				}
				hide();
			default:
				break;
		}
		// 事件分发交给父类  
		super.dispatchTouchEvent( ev );
		return true;
	}
	
	private void initView()
	{
		// 初始化下拉布局  
		refreshStateTextView = (TextView)refreshView.findViewById( R.id.state_tv );
		refreshingView = refreshView.findViewById( R.id.refreshing_icon );
		// 初始化上拉布局  
		loadStateTextView = (TextView)loadmoreView.findViewById( R.id.loadstate_tv );
		loadingView = loadmoreView.findViewById( R.id.loading_icon );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
		{
			refreshStateTextView.setTextColor( getContext().getResources().getColor( R.color.news_text_color_s5 ) );
			loadStateTextView.setTextColor( getContext().getResources().getColor( R.color.news_text_color_s5 ) );
		}
	}
	
	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		refreshView = findViewById( R.id.refresh_head );
		pullableView = (ListView)findViewById( R.id.content_view );
		loadmoreView = findViewById( R.id.load_more );
		initView();
	}
	
	@Override
	protected void onLayout(
			boolean changed ,
			int l ,
			int t ,
			int r ,
			int b )
	{
		if( !isLayout )
		{
			// 这里是第一次进来的时候做一些初始化  
			isLayout = true;
			refreshDist = ( (ViewGroup)refreshView ).getChildAt( 0 ).getMeasuredHeight();
			loadmoreDist = ( (ViewGroup)loadmoreView ).getChildAt( 0 ).getMeasuredHeight();
		}
		// 改变子控件的布局，这里直接用(pullDownY + pullUpY)作为偏移量，这样就可以不对当前状态作区分  
		refreshView.layout( 0 , (int)( pullDownY + pullUpY ) - refreshView.getMeasuredHeight() , refreshView.getMeasuredWidth() , (int)( pullDownY + pullUpY ) );
		pullableView.layout( 0 , (int)( pullDownY + pullUpY ) , pullableView.getMeasuredWidth() , (int)( pullDownY + pullUpY ) + pullableView.getMeasuredHeight() );
		loadmoreView.layout(
				0 ,
				(int)( pullDownY + pullUpY ) + pullableView.getMeasuredHeight() ,
				loadmoreView.getMeasuredWidth() ,
				(int)( pullDownY + pullUpY ) + pullableView.getMeasuredHeight() + loadmoreView.getMeasuredHeight() );
	}
	
	class MyTimer
	{
		
		private Handler handler;
		private Timer timer;
		private MyTask mTask;
		
		public MyTimer(
				Handler handler )
		{
			this.handler = handler;
			timer = new Timer();
		}
		
		public void schedule(
				long period )
		{
			if( mTask != null )
			{
				mTask.cancel();
				mTask = null;
			}
			mTask = new MyTask( handler );
			timer.schedule( mTask , 0 , period );
		}
		
		public void cancel()
		{
			if( mTask != null )
			{
				mTask.cancel();
				mTask = null;
			}
		}
		
		class MyTask extends TimerTask
		{
			
			private Handler handler;
			
			public MyTask(
					Handler handler )
			{
				this.handler = handler;
			}
			
			@Override
			public void run()
			{
				handler.obtainMessage().sendToTarget();
			}
		}
	}
	
	public boolean isScrollToTop()
	{
		if( pullableView != null )
		{
			if( pullableView.getChildCount() == 0 )
			{
				return true;
			}
			else if( pullableView.getFirstVisiblePosition() == 0 && pullableView.getChildCount() > 0 && pullableView.getChildAt( 0 ).getTop() >= 0 )
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean canPullDown()
	{
		//		if( pullableView != null )
		//		{
		//			if( pullableView.getChildCount() == 0 )
		//			{
		//				return true;
		//			}
		//			else if( pullableView.getFirstVisiblePosition() == 0 && pullableView.getChildCount() > 0 && pullableView.getChildAt( 0 ).getTop() >= 0 )
		//			{
		//				return true;
		//			}
		//		}
		return false;//不支持下拉刷新
	}
	
	public boolean canPullUp()
	{
		if( pullableView != null )
		{
			/*if( ( pullableView ).getCount() == 0 )
			{
				return true;
			}
			else*/if( pullableView.getCount() > 0 && ( pullableView ).getLastVisiblePosition() == ( pullableView.getCount() - 1 ) )
			{
				if( pullableView.getChildAt( pullableView.getLastVisiblePosition() - pullableView.getFirstVisiblePosition() ) != null && pullableView.getChildAt(
						pullableView.getLastVisiblePosition() - pullableView.getFirstVisiblePosition() ).getBottom() <= pullableView.getMeasuredHeight() )
					return true;
			}
		}
		return false;
	}
	
	public int getCurrentNewsCount()
	{
		if( mAdapter == null )
		{
			return 0;
		}
		return mAdapter.getCount();
	}
	
	public void setScrollListener(
			OnScrollListener mScrollListener )
	{
		this.mScrollListener = mScrollListener;
	}
	
	public interface OnRefreshListener
	{
		
		/** 
		 * 刷新操作 
		 */
		void onRefresh(
				PullToRefreshLayout pullToRefreshLayout );
		
		/** 
		 * 加载操作 
		 */
		void onLoadMore(
				PullToRefreshLayout pullToRefreshLayout );
	}
	
	public interface OnScrollListener
	{
		
		public void showTopView(
				String categoryId );
		
		public void hideTopView(
				String categoryId );
	}
}
