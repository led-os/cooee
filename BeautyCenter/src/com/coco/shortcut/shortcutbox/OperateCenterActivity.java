package com.coco.shortcut.shortcutbox;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.coco.download.DownloadList;
import com.coco.theme.themebox.ActivityManager;
import com.coco.theme.themebox.DownloadApkContentService;
import com.coco.theme.themebox.MainActivity;
import com.coco.theme.themebox.PullToRefreshView;
import com.coco.theme.themebox.PullToRefreshView.OnFooterRefreshListener;
import com.coco.theme.themebox.PullToRefreshView.OnHeaderRefreshListener;
import com.coco.theme.themebox.util.DownModule;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Log;
import com.coco.theme.themebox.util.Tools;
import com.iLoong.base.themebox.R;
import com.umeng.analytics.MobclickAgent;


public class OperateCenterActivity extends Activity implements OnHeaderRefreshListener , OnFooterRefreshListener
{
	
	private Context mContext;
	private GridView gridViewLocal;
	private GridView gridViewHot;
	private OperateGridLocalAdapter localAdapter;
	private OperateGridHotAdapter hotAdapter;
	private ViewPager gridPager;
	private GridPagerAdapter pagerAdapter;
	private DownModule downModule;
	private final int INDEX_LOCAL = 0;
	private final int INDEX_HOT = 1;
	private View hotView;
	private PullToRefreshView mPullToRefreshView;
	private boolean footerRefresh = false;
	private boolean headerRefresh = false;
	private Handler handler = new Handler();
	private boolean listRefresh = false;
	private static boolean interneterr = false;
	private BroadcastReceiver packageReceiver = null;
	
	/**
	 * 判断是否联网
	 */
	public boolean IsHaveInternet(
			Context context )
	{
		try
		{
			ConnectivityManager manger = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
			NetworkInfo info = manger.getActiveNetworkInfo();
			return( info != null && info.isConnected() );
		}
		catch( Exception e )
		{
			return false;
		}
	}
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		ActivityManager.pushActivity( this );
		super.onCreate( savedInstanceState );
		PathTool.makeDirApp();
		mContext = this;
		downModule = DownModule.getInstance( this );
		setContentView( getContentView() );
		DownloadList.getInstance( this ).startUICenterLog( "0035" , null , null );
	}
	
	private View getContentView()
	{
		long preTime = System.currentTimeMillis();
		View result = View.inflate( mContext , R.layout.theme_main , null );
		gridViewLocal = (GridView)( View.inflate( mContext , R.layout.lock_grid , null ) );
		View local = ( View.inflate( mContext , R.layout.lock_grid_include_empty , null ) );
		// gridViewLocal = (GridView) (View.inflate(mContext,
		// R.layout.lock_grid, null));
		final ImageView empty = (ImageView)local.findViewById( android.R.id.empty );
		gridViewLocal = (GridView)local.findViewById( android.R.id.list );
		localAdapter = new OperateGridLocalAdapter( mContext , downModule );
		localAdapter.setBackgroundListener( new OperateGridLocalAdapter.BackgroundChangeListener() {
			
			@Override
			public void setBackground()
			{
				// TODO Auto-generated method stub
				if( localAdapter.getCount() == 0 )
				//				{
				//					empty.setVisibility( View.VISIBLE );
				//					gridViewLocal.setVisibility( View.GONE );
				//				}
				//				else
				{
					empty.setVisibility( View.GONE );
					gridViewLocal.setVisibility( View.VISIBLE );
				}
			}
		} );
		gridViewLocal.setAdapter( localAdapter );
		gridViewLocal.setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View v ,
					int position ,
					long id )
			{
				OperateInformation themeInfo = (OperateInformation)parent.getItemAtPosition( position );
				Intent i = new Intent();
				i.putExtra( UtilsBase.EXTRA_PACKAGE_NAME , themeInfo.getPackageName() );
				Log.v( "************" , "000000000000packname = " + themeInfo.getPackageName() );
				i.putExtra( UtilsBase.EXTRA_CLASS_NAME , themeInfo.getClassName() );
				i.setClass( mContext , OperateContentPreviewActivity.class );
				// i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				mContext.startActivity( i );
			}
		} );
		// 热门主题
		hotView = View.inflate( mContext , R.layout.lock_grid_hot , null );
		mPullToRefreshView = (PullToRefreshView)hotView.findViewById( R.id.main_pull_refresh_view );
		mPullToRefreshView.setOnHeaderRefreshListener( this );
		mPullToRefreshView.setOnFooterRefreshListener( this );
		gridViewHot = (GridView)hotView.findViewById( R.id.gridViewLock );
		hotAdapter = new OperateGridHotAdapter( mContext , downModule );
		// hotAdapter.queryPackage(localAdapter.getPackageNameSet());
		gridViewHot.setAdapter( hotAdapter );
		// ViewPager
		gridPager = (ViewPager)result.findViewById( R.id.themeGridPager );
		pagerAdapter = new GridPagerAdapter( local , hotView );
		pagerAdapter.setGridView( gridViewHot );
		gridPager.setAdapter( pagerAdapter );
		if( hotAdapter.showProgress() || downModule.isRefreshOperateList() )
		{
			downModule.downloadOperateList();
			if( IsHaveInternet( mContext ) )
			{
				interneterr = false;
				listRefresh = true;
				handler.postDelayed( new Runnable() {
					
					@Override
					public void run()
					{
						downModule.stopOperateList();
						if( listRefresh )
						{
							if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
							{
								if( gridPager.getCurrentItem() == INDEX_HOT )
									Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
							}
						}
						if( pagerAdapter != null && pagerAdapter.viewDownloading != null )
						{
							pagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
						}
					}
				} , 1000 * 30 );
			}
			else
			{
				if( gridPager.getCurrentItem() == INDEX_HOT )
					Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
				interneterr = true;
				if( pagerAdapter != null && pagerAdapter.viewDownloading != null )
				{
					pagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
				}
			}
		}
		gridViewHot.setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View v ,
					int position ,
					long id )
			{
				OperateInformation infor = (OperateInformation)parent.getItemAtPosition( position );
				Intent i = new Intent();
				i.putExtra( UtilsBase.EXTRA_PACKAGE_NAME , infor.getPackageName() );
				i.putExtra( UtilsBase.EXTRA_CLASS_NAME , infor.getClassName() );
				i.setClass( mContext , OperateContentPreviewActivity.class );
				// i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				mContext.startActivity( i );
			}
		} );
		gridPager.setOverScrollMode( View.OVER_SCROLL_NEVER );
		Log.v( "time" , "lockcreate = " + ( System.currentTimeMillis() - preTime ) + "" );
		// 热门按钮
		final RadioButton themeHotButton = (RadioButton)result.findViewById( R.id.btnHotTheme );
		final RadioButton themeLocalButton = (RadioButton)result.findViewById( R.id.btnLocalTheme );
		themeLocalButton.setText( R.string.text_lcoal_operate );
		themeHotButton.setText( R.string.text_hot_opertate );
		themeHotButton.setChecked( true );
		gridPager.setCurrentItem( INDEX_HOT , true );
		gridPager.setOnPageChangeListener( new OnPageChangeListener() {
			
			@Override
			public void onPageScrollStateChanged(
					int arg0 )
			{
			}
			
			@Override
			public void onPageScrolled(
					int arg0 ,
					float arg1 ,
					int arg2 )
			{
			}
			
			@Override
			public void onPageSelected(
					int index )
			{
				if( index == INDEX_LOCAL )
				{
					themeLocalButton.toggle();
				}
				else if( index == INDEX_HOT )
				{
					if( com.coco.theme.themebox.StaticClass.isAllowDownload( mContext ) )
					{
						// 无网�?
						if( IsHaveInternet( mContext ) == false )
						{
							Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
							if( pagerAdapter != null && pagerAdapter.viewDownloading != null )
							{
								pagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
							}
						}
						else
						{
							if( pagerAdapter != null && pagerAdapter.viewDownloading != null )
							{
								pagerAdapter.viewDownloading.setVisibility( View.VISIBLE );
							}
							Log.v( "Tab" , "count = " + hotAdapter.getCount() );
							if( hotAdapter.getCount() == 0 )
							{
								pagerAdapter.notifyDataSetChanged();
								downModule.downloadList();
								if( IsHaveInternet( mContext ) )
								{
									listRefresh = true;
									handler.postDelayed( new Runnable() {
										
										@Override
										public void run()
										{
											downModule.stopOperateList();
											if( listRefresh )
											{
												if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
												{
													Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
												}
											}
											if( pagerAdapter.viewDownloading != null )
											{
												pagerAdapter.viewDownloading.setVisibility( View.INVISIBLE );
											}
										}
									} , 1000 * 30 );
								}
							}
						}
					}
					else
					{
						Toast.makeText( mContext , R.string.sdcard_not_available , Toast.LENGTH_SHORT ).show();
					}
					themeHotButton.toggle();
				}
			}
		} );
		// 热门
		themeHotButton.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View arg0 )
			{
				gridPager.setCurrentItem( INDEX_HOT , true );
			}
		} );
		// 本地按钮
		themeLocalButton.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View arg0 )
			{
				gridPager.setCurrentItem( INDEX_LOCAL , true );
			}
		} );
		packageReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(
					Context context ,
					Intent intent )
			{
				String actionName = intent.getAction();
				if( Intent.ACTION_PACKAGE_REMOVED.equals( actionName ) )
				{
					String packageName = intent.getData().getSchemeSpecificPart();
					if( localAdapter.containPackage( packageName ) )
					{
						localAdapter.reloadPackage();
						hotAdapter.reloadPackage();
					}
				}
				else if( Intent.ACTION_PACKAGE_ADDED.equals( actionName ) )
				{
					localAdapter.reloadPackage();
					hotAdapter.reloadPackage();
				}
				else if( actionName.equals( UtilsBase.ACTION_THUMB_CHANGED ) )
				{
					localAdapter.updateThumb( intent.getStringExtra( UtilsBase.EXTRA_PACKAGE_NAME ) );
					hotAdapter.updateThumb( intent.getStringExtra( UtilsBase.EXTRA_PACKAGE_NAME ) );
				}
				else if( actionName.equals( UtilsBase.ACTION_HOT_CHANGED ) )
				{
					localAdapter.reloadPackage();
					hotAdapter.reloadPackage();
					hotAdapter.setShowProgress( false );
					pagerAdapter.notifyDataSetChanged();
					if( listRefresh )
					{
						listRefresh = false;
					}
					if( footerRefresh )
					{
						mPullToRefreshView.onFooterRefreshComplete();
						footerRefresh = false;
					}
					if( headerRefresh )
					{
						mPullToRefreshView.onHeaderRefreshComplete();
						headerRefresh = false;
					}
				}
				else if( actionName.equals( UtilsBase.ACTION_DOWNLOAD_STATUS_CHANGED ) )
				{
					localAdapter.reloadPackage();
					hotAdapter.reloadPackage();
				}
				else if( actionName.equals( UtilsBase.ACTION_DOWNLOAD_SIZE_CHANGED ) )
				{
					hotAdapter.updateDownloadSize(
							intent.getStringExtra( UtilsBase.EXTRA_PACKAGE_NAME ) ,
							intent.getIntExtra( UtilsBase.EXTRA_DOWNLOAD_SIZE , 0 ) ,
							intent.getIntExtra( UtilsBase.EXTRA_TOTAL_SIZE , 0 ) );
				}
				else if( actionName.equals( UtilsBase.ACTION_START_DOWNLOAD_APK ) )
				{
					String curdownApkname = intent.getStringExtra( UtilsBase.EXTRA_PACKAGE_NAME );
					// downModule.downloadApk(curdownApkname);
					Intent it = new Intent();
					it.putExtra( "name" , intent.getStringExtra( "apkname" ) );
					it.putExtra( "packageName" , curdownApkname );
					it.putExtra( "type" , DownloadList.Operate_Type );
					it.putExtra( "status" , "download" );
					it.setClass( mContext , DownloadApkContentService.class );
					mContext.startService( it );
					hotAdapter.notifyDataSetChanged();
				}
				else if( actionName.equals( UtilsBase.ACTION_PAUSE_DOWNLOAD_APK ) )
				{
					String packName = intent.getStringExtra( UtilsBase.EXTRA_PACKAGE_NAME );
					// downModule.stopDownApk(packName);
					Intent it = new Intent();
					it.putExtra( "packageName" , packName );
					it.putExtra( "type" , DownloadList.Operate_Type );
					it.putExtra( "name" , intent.getStringExtra( "apkname" ) );
					it.putExtra( "status" , "pause" );
					it.setClass( mContext , DownloadApkContentService.class );
					mContext.startService( it );
					hotAdapter.notifyDataSetChanged();
					Log.v( "********" , "receive packName = " + packName );
				}
			}
		};
		// 注册删除事件
		IntentFilter pkgFilter = new IntentFilter();
		pkgFilter.addAction( Intent.ACTION_PACKAGE_REMOVED );
		pkgFilter.addAction( Intent.ACTION_PACKAGE_ADDED );
		pkgFilter.addDataScheme( "package" );
		mContext.registerReceiver( packageReceiver , pkgFilter );
		// 下载成功
		IntentFilter screenFilter1 = new IntentFilter();
		screenFilter1.addAction( UtilsBase.ACTION_START_DOWNLOAD_APK );
		screenFilter1.addAction( UtilsBase.ACTION_THUMB_CHANGED );
		screenFilter1.addAction( UtilsBase.ACTION_HOT_CHANGED );
		screenFilter1.addAction( UtilsBase.ACTION_DOWNLOAD_SIZE_CHANGED );
		screenFilter1.addAction( UtilsBase.ACTION_DOWNLOAD_STATUS_CHANGED );
		screenFilter1.addAction( UtilsBase.ACTION_PAUSE_DOWNLOAD_APK );
		mContext.registerReceiver( packageReceiver , screenFilter1 );
		return result;
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		ActivityManager.popupActivity( this );
		super.onDestroy();
		if( packageReceiver != null )
			mContext.unregisterReceiver( packageReceiver );
		if( localAdapter != null )
		{
			localAdapter.onDestory();
		}
		if( hotAdapter != null )
		{
			hotAdapter.onDestory();
		}
		//		if( PlatformInfo.getInstance( this ).isSupportViewLock() || !FunctionConfig.isDisplayLock() )
		//		{
		//			Message msg = mDelayedStopHandler.obtainMessage();
		//			mDelayedStopHandler.sendMessageDelayed( msg , 0 );
		//		}
		//		else
		//		{
		//			Boolean lockScreen = PreferenceManager.getDefaultSharedPreferences( this ).getBoolean( com.coco.lock2.lockbox.StaticClass.ENABLE_LOCK , false );
		//			LockManager mgr = new LockManager( this );
		//			if( !lockScreen || !mgr.isenableCooeeLock() )//没有移植包的情况，锁屏开关关闭或者当前锁屏不存在，退出线程
		//			{
		//				Message msg = mDelayedStopHandler.obtainMessage();
		//				mDelayedStopHandler.sendMessageDelayed( msg , 0 );
		//			}
		//		}
	}
	
	private Handler mDelayedStopHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			Log.v( "themebox" , "themebox  kill" );
			if( Tools.isServiceRunning( mContext , "com.coco.theme.themebox.DownloadApkContentService" ) || Tools.isServiceRunning( mContext , "com.coco.theme.themebox.update.UpdateService" ) )
			{
				Message m = mDelayedStopHandler.obtainMessage();
				mDelayedStopHandler.sendMessageDelayed( m , 10000 );
				return;
			}
			try
			{
				// @2015/03/11 ADD START 友盟统计美华中心退出
				if( FunctionConfig.isUmengStatistics_key() )
				{
					MainActivity.statisticsExitBeautyCenter( mContext );
				}
				// @2015/03/11 ADD END
				System.exit( 0 );
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
			}
		}
	};
	
	private static class GridPagerAdapter extends PagerAdapter
	{
		
		private final String LOG_TAG = "GridPagerAdapter";
		private View gridLocal;
		private GridView gridHot;
		private View hotView;
		private View viewDownloading = null;
		private OperateGridHotAdapter hotAdapter;
		
		public GridPagerAdapter(
				View local ,
				View view )
		{
			gridLocal = local;
			hotView = view;
		}
		
		public void setGridView(
				GridView hot )
		{
			gridHot = hot;
			hotAdapter = (OperateGridHotAdapter)gridHot.getAdapter();
		}
		
		@Override
		public void destroyItem(
				ViewGroup container ,
				int position ,
				Object object )
		{
			Log.d( LOG_TAG , "destroyItem,pos" + position );
			if( viewDownloading != null && isViewFromObject( viewDownloading , object ) )
			{
				container.removeView( viewDownloading );
				viewDownloading = null;
			}
		}
		
		@Override
		public int getItemPosition(
				Object object )
		{
			if( viewDownloading != null && isViewFromObject( viewDownloading , object ) && !hotAdapter.showProgress() )
			{
				return PagerAdapter.POSITION_NONE;
			}
			return PagerAdapter.POSITION_UNCHANGED;
		}
		
		@Override
		public int getCount()
		{
			return 2;
		}
		
		@Override
		public Object instantiateItem(
				ViewGroup container ,
				int position )
		{
			Log.d( LOG_TAG , "instantiateItem,pos=" + position );
			if( position == 0 )
			{
				container.addView( gridLocal );
				return gridLocal;
			}
			if( hotAdapter.showProgress() )
			{
				viewDownloading = View.inflate( container.getContext() , R.layout.grid_item_downloading , null );
				if( interneterr )
				{
					viewDownloading.setVisibility( View.GONE );
				}
				else
				{
					viewDownloading.setVisibility( View.VISIBLE );
				}
				container.addView( viewDownloading );
				return viewDownloading;
			}
			// ((ViewPager) container).addView(gridHot);
			( (ViewPager)container ).addView( hotView );
			return hotView;
		}
		
		@Override
		public boolean isViewFromObject(
				View view ,
				Object object )
		{
			return view == ( object );
		}
		
		@Override
		public void restoreState(
				Parcelable state ,
				ClassLoader loader )
		{
		}
		
		@Override
		public Parcelable saveState()
		{
			return null;
		}
	}
	
	@Override
	public void onFooterRefresh(
			PullToRefreshView view )
	{
		Log.v( "PullToRefreshView" , "tablock_onFooterRefresh" );
		if( gridPager.getCurrentItem() == INDEX_HOT )
		{
			if( IsHaveInternet( mContext ) )
			{
				footerRefresh = true;
				downModule.downloadOperateList();
				handler.postDelayed( new Runnable() {
					
					@Override
					public void run()
					{
						downModule.stopOperateList();
						if( footerRefresh )
						{
							mPullToRefreshView.onFooterRefreshComplete();
							footerRefresh = false;
							if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
							{
								Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
							}
						}
						if( headerRefresh )
						{
							mPullToRefreshView.onHeaderRefreshComplete();
							headerRefresh = false;
							if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
							{
								Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
							}
						}
					}
				} , 1000 * 30 );
			}
			else
			{
				Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
				mPullToRefreshView.onFooterRefreshComplete();
			}
		}
	}
	
	@Override
	public void onHeaderRefresh(
			PullToRefreshView view )
	{
		Log.v( "PullToRefreshView" , "tablock_onFooterRefresh" );
		if( gridPager.getCurrentItem() == INDEX_HOT )
		{
			Log.v( "onHeaderRefresh" , "**************" );
			if( IsHaveInternet( mContext ) )
			{
				headerRefresh = true;
				downModule.downloadOperateList();
				handler.postDelayed( new Runnable() {
					
					@Override
					public void run()
					{
						Log.v( "onHeaderRefresh" , "Run footerRefresh = " + footerRefresh + " headerRefresh = " + headerRefresh );
						downModule.stopOperateList();
						if( footerRefresh )
						{
							mPullToRefreshView.onFooterRefreshComplete();
							footerRefresh = false;
							if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
							{
								Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
							}
						}
						if( headerRefresh )
						{
							mPullToRefreshView.onHeaderRefreshComplete();
							headerRefresh = false;
							if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
							{
								Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
							}
						}
					}
				} , 1000 * 30 );
			}
			else
			{
				Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
				mPullToRefreshView.onHeaderRefreshComplete();
			}
		}
	}
}
