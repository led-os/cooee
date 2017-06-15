package com.coco.lock2.lockbox;


import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.coco.download.DownloadList;
import com.coco.lock2.lockbox.preview.PreviewHotActivity;
import com.coco.lock2.lockbox.util.LockManager;
import com.coco.lock2.lockbox.util.PathTool;
import com.coco.theme.themebox.ContentFactory;
import com.coco.theme.themebox.DownloadApkContentService;
import com.coco.theme.themebox.PullToRefreshView;
import com.coco.theme.themebox.util.DownModule;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Log;
import com.iLoong.base.themebox.R;


public class TabLockFactory implements ContentFactory
{
	
	private final String LOG_TAG = "TabLockContentFactory";
	private final int INDEX_LOCAL = 0;
	private final int INDEX_HOT = 1;
	private GridView gridViewLocal;
	private GridView gridViewHot;
	private GridLocalAdapter localAdapter;
	private GridHotAdapter hotAdapter;
	private ViewPager gridPager;
	private DownModule downModule;
	private GridPagerAdapter pagerAdapter;
	private Context mContext;
	private View hotView;
	private PullToRefreshView mPullToRefreshView;
	private boolean footerRefresh = false;
	private boolean headerRefresh = false;
	private Handler handler = new Handler();
	private boolean listRefresh = false;
	private static boolean interneterr = false;
	private RadioButton HotButton;
	private RadioButton LocalButton;
	
	@Override
	public void changeTab(
			int tab )
	{
		// TODO Auto-generated method stub
		if( tab == INDEX_LOCAL )
		{
			if( LocalButton != null && LocalButton.getVisibility() == View.VISIBLE )
			{
				LocalButton.setChecked( true );
				gridPager.setCurrentItem( INDEX_LOCAL , false );
			}
		}
		else
		{
			if( HotButton != null && HotButton.getVisibility() == View.VISIBLE )
			{
				HotButton.setChecked( true );
				gridPager.setCurrentItem( INDEX_HOT , false );
			}
		}
	}
	
	@Override
	public void reloadView()
	{
		// TODO Auto-generated method stub
	}
	
	public TabLockFactory(
			Context context ,
			DownModule module )
	{
		mContext = context;
		downModule = module;
		PathTool.makeDirApp();
	}
	
	public void onDestroy()
	{
		unregisterReceiver();
		if( localAdapter != null )
		{
			localAdapter.onDestory();
		}
		if( hotAdapter != null )
		{
			hotAdapter.onDestory();
		}
		Log.d( LOG_TAG , "TabLockFactory onDestroy end" );
	}
	
	public void unregisterReceiver()
	{
		if( packageReceiver != null )
		{
			mContext.unregisterReceiver( packageReceiver );
		}
	}
	
	public BaseAdapter getLocalAdapter()
	{
		return localAdapter;
	}
	
	/**
	 * 判断是否联网
	 */
	public boolean IsHaveInternet(
			final Context context )
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
	public View createTabContent(
			String tag )
	{
		// if (!StaticClass.isLockBoxInstalled(mContext)) {
		// return createLockDown();
		// }   
		View result = View.inflate( mContext , R.layout.lock_main , null );
		View containHot = result.findViewById( R.id.containHot );
		HotButton = (RadioButton)result.findViewById( R.id.btnHotLock );
		LocalButton = (RadioButton)result.findViewById( R.id.btnLocalLock );
		long preTime = System.currentTimeMillis();
		// 本地锁屏
		View local = ( View.inflate( mContext , R.layout.lock_grid_include_empty , null ) );
		final ImageView empty = (ImageView)local.findViewById( android.R.id.empty );
		gridViewLocal = (GridView)local.findViewById( android.R.id.list );
		if( FunctionConfig.isBrzh_setWaitBackgroundView() )
		{
			gridViewLocal.setBackgroundResource( R.drawable.brzh_viewpager_bg );
		}
		localAdapter = new GridLocalAdapter( mContext , downModule );
		localAdapter.setBackgroundListener( new GridLocalAdapter.BackgroundChangeListener() {
			
			@Override
			public void setBackground()
			{
				// TODO Auto-generated method stub
				if( localAdapter.getCount() == 0 )
				{
					empty.setVisibility( View.VISIBLE );
					gridViewLocal.setVisibility( View.GONE );
				}
				else
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
				Object item = parent.getItemAtPosition( position );
				if( item instanceof LockInformation )
				{
					LockInformation lockInfo = (LockInformation)parent.getItemAtPosition( position );
					Intent i = new Intent();
					if( FunctionConfig.isEnable_topwise_style() && lockInfo.isThirdPartyLock() )
					{
						LockManager mgr = new LockManager( mContext );
						if( !lockInfo.isSecure() )//是否为图案解锁、PIN码解锁、密码解锁
						{ //记录第三方盒子跳转,因为需要点击后就做"使用中"的标记
							mgr.applyThirdPartyLock( lockInfo.getPackageName() , lockInfo.getClassName() , "" );
							int value = StaticClass.LOCK_STYLE_VALUE;
							try
							{
								value = Integer.parseInt( lockInfo.getLockStyleValue() );
								Log.v( "PreviewHotActivity" , "lockStyleValue =" + lockInfo.getLockStyleValue() );
							}
							catch( Exception ex )
							{
								Log.v( "PreviewHotActivity" , ex.getMessage() );
								value = StaticClass.LOCK_STYLE_VALUE;
							}
							Settings.System.putInt( mContext.getContentResolver() , "system.settings.lockstyle" , value );
						}
						gridViewLocal.postDelayed( new Runnable() {
							
							@Override
							public void run()
							{
								// TODO Auto-generated method stub
								mContext.sendBroadcast( new Intent( "com.coco.lock.action.DEFAULT_LOCK_CHANGED" ) );
							}
						} , 500 );
						i.setClassName( lockInfo.getThirdPackageName() , lockInfo.getThirdClassName() );
					}
					else
					{
						i.putExtra( StaticClass.EXTRA_PACKAGE_NAME , lockInfo.getPackageName() );
						i.putExtra( StaticClass.EXTRA_CLASS_NAME , lockInfo.getClassName() );
						i.putExtra( "ishowmore" , FunctionConfig.isThemeMoreShow() );// 朵唯风格，不显示更多的信息内容
						i.putExtra( "isshare" , FunctionConfig.isShareVisible() );
						i.setClass( mContext , PreviewHotActivity.class );
						// i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					}
					mContext.startActivity( i );
				}
				else
				{
					LockManager lockmgr = new LockManager( mContext );
					lockmgr.applyLock( "com.test" , "com.test.testActivity" , "com.test.testWrap" );
					new Thread() {
						
						@Override
						public void run()
						{
							localAdapter.reloadPackage();
						}
					}.start();
				}
			}
		} );
		// // 热门锁屏
		// gridViewHot = (GridView) View.inflate(mContext,
		// R.layout.lock_grid,
		// null).findViewById(R.id.gridViewLock);
		//		// 热门锁屏
		if( com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
		{
			hotView = View.inflate( mContext , R.layout.lock_grid_hot , null );
			mPullToRefreshView = (PullToRefreshView)hotView.findViewById( R.id.main_pull_refresh_view );
			mPullToRefreshView.setOnHeaderRefreshListener( this );
			mPullToRefreshView.setOnFooterRefreshListener( this );
			gridViewHot = (GridView)hotView.findViewById( R.id.gridViewLock );
			if( FunctionConfig.isBrzh_setWaitBackgroundView() )
			{
				gridViewHot.setBackgroundResource( R.drawable.brzh_viewpager_bg );
			}
			hotAdapter = new GridHotAdapter( mContext , downModule );
			// hotAdapter.queryPackage(localAdapter.getPackageNameSet());
			handler.postDelayed( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					gridViewHot.setAdapter( hotAdapter );
				}
			} , 300 );
			gridPager = (ViewPager)result.findViewById( R.id.gridPager );
			pagerAdapter = new GridPagerAdapter( local , hotView );
			pagerAdapter.setGridView( gridViewHot );
			gridPager.setAdapter( pagerAdapter );
			if( hotAdapter.showProgress() || downModule.isRefreshList() )
			{
				downModule.downloadList();
				if( IsHaveInternet( mContext ) )
				{
					interneterr = false;
					listRefresh = true;
					handler.postDelayed( new Runnable() {
						
						@Override
						public void run()
						{
							downModule.stopDownlist();
							if( listRefresh )
							{
								if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
								{
									if( gridPager.getCurrentItem() == INDEX_HOT )
										Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
								}
								listRefresh = false;
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
					LockInformation infor = (LockInformation)parent.getItemAtPosition( position );
					Intent i = new Intent();
					i.putExtra( StaticClass.EXTRA_PACKAGE_NAME , infor.getPackageName() );
					i.putExtra( StaticClass.EXTRA_CLASS_NAME , infor.getClassName() );
					i.putExtra( "ishowmore" , FunctionConfig.isThemeMoreShow() );
					i.putExtra( "isshare" , FunctionConfig.isShareVisible() );
					i.putExtra( "CustomRootPath" , PathTool.getCustomRootPath() );
					i.setClass( mContext , PreviewHotActivity.class );
					mContext.startActivity( i );
				}
			} );
			// // ViewPager
			// gridPager = (ViewPager) result.findViewById(R.id.gridPager);
			// pagerAdapter = new GridPagerAdapter(gridViewLocal, gridViewHot,
			// mContext);
			// gridPager.setAdapter(pagerAdapter);
			// ViewPager
			gridPager.setOverScrollMode( View.OVER_SCROLL_NEVER );
			Log.v( "time" , "lockcreate = " + ( System.currentTimeMillis() - preTime ) + "" );
			// 热门按钮
			final RadioButton hotButton = (RadioButton)result.findViewById( R.id.btnHotLock );
			final RadioButton localButton = (RadioButton)result.findViewById( R.id.btnLocalLock );
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
						localButton.toggle();
					}
					else if( index == INDEX_HOT )
					{
						if( StaticClass.isAllowDownload( mContext ) )
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
												downModule.stopDownlist();
												if( listRefresh )
												{
													if( com.coco.theme.themebox.util.FunctionConfig.isPromptVisible() )
													{
														Toast.makeText( mContext , R.string.internet_unusual , Toast.LENGTH_SHORT ).show();
													}
													listRefresh = false;
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
						hotButton.toggle();
					}
				}
			} );
			// 热门
			hotButton.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View arg0 )
				{
					gridPager.setCurrentItem( INDEX_HOT , true );
				}
			} );
			// 本地按钮
			localButton.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View arg0 )
				{
					gridPager.setCurrentItem( INDEX_LOCAL , true );
				}
			} );
		}
		else
		{
			if( FunctionConfig.isdoovStyle() )
			{
				containHot.setVisibility( View.GONE );
			}
			else
			{
				containHot.setVisibility( View.VISIBLE );
				final RadioButton lockHotButton = (RadioButton)result.findViewById( R.id.btnHotLock );
				final RadioButton lockLocalButton = (RadioButton)result.findViewById( R.id.btnLocalLock );
				lockHotButton.setVisibility( View.GONE );
			}
			gridPager = (ViewPager)result.findViewById( R.id.gridPager );
			pagerAdapter = new GridPagerAdapter( local );
			gridPager.setAdapter( pagerAdapter );
			gridPager.setOverScrollMode( View.OVER_SCROLL_NEVER );
		}
		// 监听设置按钮
		ImageButton btnSetting = (ImageButton)result.findViewById( R.id.btnSetting );
		if( FunctionConfig.isLockSetVisible() )
		{
			if( !PlatformInfo.getInstance( mContext ).isSupportViewLock() )
			{
				btnSetting.setVisibility( View.VISIBLE );
			}
			else
			{
				btnSetting.setVisibility( View.INVISIBLE );
			}
			btnSetting.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View arg0 )
				{
					Intent intentSetting = new Intent();
					intentSetting.setClassName( mContext.getPackageName() , StaticClass.LOCKBOX_SETTING_ACTIVITY );
					mContext.startActivity( intentSetting );
				}
			} );
		}
		else
		{
			btnSetting.setVisibility( View.INVISIBLE );
		}
		packageReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(
					Context context ,
					Intent intent )
			{
				String actionName = intent.getAction();
				Log.d( LOG_TAG , String.format( "action=%s" , actionName ) );
				if( Intent.ACTION_PACKAGE_REMOVED.equals( actionName ) )
				{
					String packageName = intent.getData().getSchemeSpecificPart();
					if( localAdapter.containPackage( packageName ) )
					{
						Log.d( LOG_TAG , "ACTION_PACKAGE_REMOVED = true" );
						localAdapter.reloadPackage();
						if( com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
							hotAdapter.reloadPackage( localAdapter.getPackageNameSet() );
						// DownloadTab.getInstance(mContext).startUICenterLog(DownloadTab.ACTION_UNINSTALL_LOG,"0",packageName);
					}
				}
				else if( Intent.ACTION_PACKAGE_ADDED.equals( actionName ) )
				{
					String packageName = intent.getData().getSchemeSpecificPart();
					Log.d( LOG_TAG , "ACTION_PACKAGE_ADDED = " + packageName );
					if( isLockApplication( packageName ) )
					{
						Log.d( LOG_TAG , "isLockApplication = true" );
						localAdapter.reloadPackage();
						if( com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
						{
							hotAdapter.reloadPackage( localAdapter.getPackageNameSet() );
						}
					}
				}
				else if( actionName.equals( StaticClass.ACTION_THUMB_CHANGED ) )
				{
					if( com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
						hotAdapter.updateThumb( intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ) );
					localAdapter.updateThumb( intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ) );
				}
				else if( actionName.equals( StaticClass.ACTION_HOTLIST_CHANGED ) )
				{
					if( com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
					{
						hotAdapter.reloadPackage( localAdapter.getPackageNameSet() );
						hotAdapter.setShowProgress( false );
						pagerAdapter.notifyDataSetChanged();
					}
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
				else if( actionName.equals( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED ) )
				{
					if( com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
						hotAdapter.reloadPackage( localAdapter.getPackageNameSet() );
				}
				else if( actionName.equals( StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED ) )
				{
					if( com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
						hotAdapter.updateDownloadSize(
								intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME ) ,
								intent.getIntExtra( StaticClass.EXTRA_DOWNLOAD_SIZE , 0 ) ,
								intent.getIntExtra( StaticClass.EXTRA_TOTAL_SIZE , 0 ) );
				}
				else if( actionName.equals( StaticClass.ACTION_START_DOWNLOAD_APK ) )
				{
					String curdownApkname = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
					// downModule.downloadApk(curdownApkname);
					Intent it = new Intent();
					it.putExtra( "name" , intent.getStringExtra( "apkname" ) );
					it.putExtra( "packageName" , curdownApkname );
					it.putExtra( "type" , DownloadList.Lock_Type );
					it.putExtra( "status" , "download" );
					//<c_0000707> liuhailin@2014-08-11 modify begin
					it.putExtra( "className" , intent.getStringExtra( StaticClass.EXTRA_CLASS_NAME ) );
					it.putExtra( "CustomRootPath" , PathTool.getCustomRootPath() );
					//<c_0000707> liuhailin@2014-08-11 modify end
					it.setClass( mContext , DownloadApkContentService.class );
					mContext.startService( it );
					if( com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
						hotAdapter.notifyDataSetChanged();
				}
				else if( actionName.equals( StaticClass.ACTION_PAUSE_DOWNLOAD_APK ) )
				{
					String packName = intent.getStringExtra( StaticClass.EXTRA_PACKAGE_NAME );
					// downModule.stopDownApk(packName);
					Intent it = new Intent();
					it.putExtra( "packageName" , packName );
					it.putExtra( "type" , DownloadList.Lock_Type );
					it.putExtra( "name" , intent.getStringExtra( "apkname" ) );
					it.putExtra( "status" , "pause" );
					it.setClass( mContext , DownloadApkContentService.class );
					mContext.startService( it );
					if( com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
						hotAdapter.notifyDataSetChanged();
					Log.v( "********" , "receive packName = " + packName );
				}
				else if( actionName.equals( StaticClass.ACTION_DEFAULT_LOCK_CHANGED ) )
				{
					// new Thread() {
					// @Override
					// public void run() {
					// localAdapter.reloadPackage();
					// }
					// }.start();
					localAdapter.reloadPackage();
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
		IntentFilter screenFilter = new IntentFilter();
		screenFilter.addAction( StaticClass.ACTION_START_DOWNLOAD_APK );
		screenFilter.addAction( StaticClass.ACTION_THUMB_CHANGED );
		screenFilter.addAction( StaticClass.ACTION_HOTLIST_CHANGED );
		screenFilter.addAction( StaticClass.ACTION_DOWNLOAD_SIZE_CHANGED );
		screenFilter.addAction( StaticClass.ACTION_DOWNLOAD_STATUS_CHANGED );
		screenFilter.addAction( StaticClass.ACTION_PAUSE_DOWNLOAD_APK );
		screenFilter.addAction( StaticClass.ACTION_DEFAULT_LOCK_CHANGED );
		mContext.registerReceiver( packageReceiver , screenFilter );
		return result;
	}
	
	private boolean isLockApplication(
			String packageName )
	{
		try
		{
			PackageInfo pi = mContext.getPackageManager().getPackageInfo( packageName , 0 );
			Intent resolveIntent = new Intent( StaticClass.ACTION_LOCK_VIEW );
			resolveIntent.addCategory( Intent.CATEGORY_INFO );
			resolveIntent.setPackage( pi.packageName );
			List<ResolveInfo> activities = mContext.getPackageManager().queryIntentActivities( resolveIntent , 0 );
			if( activities.size() == 1 )
			{
				return true;
			}
			resolveIntent.setAction( "com.coco.third.lock.action.VIEW" );
			resolveIntent.addCategory( Intent.CATEGORY_INFO );
			resolveIntent.setPackage( pi.packageName );
			activities = mContext.getPackageManager().queryIntentActivities( resolveIntent , 0 );
			if( activities.size() == 1 )
			{
				return true;
			}
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public GridHotAdapter getHotLockAdapter()
	{
		return hotAdapter;
	}
	
	private BroadcastReceiver packageReceiver = null;
	
	/**
	 * ViewPager适配�?
	 */
	private class GridPagerAdapter extends PagerAdapter
	{
		
		private final String LOG_TAG = "GridPagerAdapter";
		private View local;
		private GridView gridHot;
		private View hotView;
		private View viewDownloading = null;
		private GridHotAdapter hotAdapter;
		
		// public GridPagerAdapter(GridView local, GridView hot, Context
		// context) {
		// gridLocal = local;
		// gridHot = hot;
		// hotAdapter = (GridHotAdapter) gridHot.getAdapter();
		// }
		public GridPagerAdapter(
				View local ,
				View view )
		{
			this.local = local;
			hotView = view;
		}
		
		public GridPagerAdapter(
				View gridViewLocal )
		{
			// TODO Auto-generated constructor stub
			this.local = gridViewLocal;
		}
		
		public void setGridView(
				GridView hot )
		{
			gridHot = hot;
			hotAdapter = (GridHotAdapter)gridHot.getAdapter();
			if( hotAdapter == null )
			{
				hotAdapter = TabLockFactory.this.hotAdapter;
			}
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
			if( !com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
			{
				return 1;
			}
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
				container.addView( local );
				return local;
			}
			if( com.coco.theme.themebox.util.FunctionConfig.isHotLockVisible() )
			{
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
	
	private void refreshList()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( mContext );
		long refresh = prefs.getLong( "refresh" , 0 );
		if( System.currentTimeMillis() - refresh < 1000 * 100 && System.currentTimeMillis() - refresh > 0 )
		{
			handler.postDelayed( new Runnable() {
				
				@Override
				public void run()
				{
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
			} , 1000 * 10 );
		}
		else
		{
			prefs.edit().putLong( "refresh" , System.currentTimeMillis() ).commit();
			downModule.downloadList();
			handler.postDelayed( new Runnable() {
				
				@Override
				public void run()
				{
					downModule.stopDownlist();
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
				refreshList();
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
				refreshList();
			}
			else
			{
				Toast.makeText( mContext , R.string.internet_err , Toast.LENGTH_SHORT ).show();
				mPullToRefreshView.onHeaderRefreshComplete();
			}
		}
	}
}
