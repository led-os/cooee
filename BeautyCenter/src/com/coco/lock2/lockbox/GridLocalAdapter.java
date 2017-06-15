package com.coco.lock2.lockbox;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtils;
import com.coco.download.DownloadList;
import com.coco.lock2.lockbox.util.LockManager;
import com.coco.theme.themebox.ThemeInformation;
import com.coco.theme.themebox.database.model.DownloadStatus;
import com.coco.theme.themebox.util.DownModule;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Log;
import com.iLoong.base.themebox.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;


public class GridLocalAdapter extends BaseAdapter
{
	
	private List<LockInformation> localList = new ArrayList<LockInformation>();
	private Context context;
	private ComponentName currentLock;
	private ComponentName currentThirdPartyLock;
	private DownModule downThumb;
	private Set<String> packageNameSet = new HashSet<String>();
	private PageTask pageTask = null;
	private LruCache<String , Bitmap> mMemoryCache;
	DisplayImageOptions options;
	private boolean currentLockModeIsSecure = false;
	private int currentQualityMode;
	private Bitmap imgDefaultThumb;
	private Handler handler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			// TODO Auto-generated method stub
			if( msg.what == 0 )
			{
				notifyDataSetChanged();
			}
			super.handleMessage( msg );
		}
	};
	
	//	private PageItemTask itemTask = null;
	public GridLocalAdapter(
			Context cxt ,
			DownModule down )
	{
		context = cxt;
		downThumb = down;
		//add by liuhailin begin
		options = new DisplayImageOptions.Builder().showStubImage( R.drawable.default_img ).showImageForEmptyUri( R.drawable.default_img ).showImageOnFail( R.drawable.default_img ).cacheInMemory()
				.cacheOnDisc().bitmapConfig( Bitmap.Config.RGB_565 ).build();
		//add by liuhailin end
		imgDefaultThumb = ( (BitmapDrawable)cxt.getResources().getDrawable( R.drawable.default_img ) ).getBitmap();
		int maxMemory = (int)Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory / 16;
		mMemoryCache = new LruCache<String , Bitmap>( cacheSize );
		if( pageTask != null && pageTask.getStatus() != PageTask.Status.FINISHED )
		{
			pageTask.cancel( true );
		}
		pageTask = (PageTask)new PageTask().execute();
	}
	
	private List<LockInformation> queryPackage()
	{
		//packageNameSet.clear();
		//localList.clear();
		/*List<LockInformation> locList = new ArrayList<LockInformation>();
		LockManager mgr = new LockManager( context );
		currentLock = mgr.queryCurrentLock();
		currentThirdPartyLock = mgr.queryCurrentThirdPartyLock();
		List<LockInformation> installList = mgr.queryInstallList();
		for( LockInformation infor : installList )
		{
			//infor.setThumbImage( context , infor.getPackageName() , infor.getClassName() );
			//addBitmapToMemoryCache( infor.getPackageName() , infor.getThumbImage() );
			if( infor.getThumbImage() == null )
			{
				new PageItemTask().execute( infor );
			}
			locList.add( infor );
			// packageNameSet.add(infor.getPackageName());
		}
		return locList;*/
		List<LockInformation> lcList = new ArrayList<LockInformation>();
		LockManager mgr = new LockManager( context );
		//Log.i( "Lock" , "GridLocalAdapter before queryCurrentLock..." );
		currentLock = mgr.queryCurrentLock();
		//Log.i( "Lock" , "GridLocalAdapter finish queryCurrentLock..." );
		currentThirdPartyLock = mgr.queryCurrentThirdPartyLock();
		//Log.i( "Lock" , "GridLocalAdapter finish queryInstallList..." );
		List<LockInformation> installList = mgr.queryInstallList();
		//Log.i( "Lock" , "GridLocalAdapter finish queryInstallList..." );
		for( ThemeInformation info : localList )
		{
			info.disposeThumb();
			info = null;
		}
		localList.clear();
		for( LockInformation infor : installList )
		{
			//infor.setThumbImage( context , infor.getPackageName() , infor.getClassName() );
			//addBitmapToMemoryCache( infor.getPackageName() , infor.getThumbImage() );
			if( infor.getThumbImage() == null )
			{
				new PageItemTask().execute( infor );
			}
			lcList.add( infor );
			localList.add( infor );
			handler.post( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					notifyDataSetChanged();
				}
			} );
			// packageNameSet.add(infor.getPackageName());
		}
		return lcList;
	}
	
	public void onDestory()
	{
		for( LockInformation info : localList )
		{
			info.disposeThumb();
			info = null;
		}
		//		if( imgDefaultThumb != null && !imgDefaultThumb.isRecycled() )
		//		{
		//			imgDefaultThumb.recycle();
		//		}
	}
	
	public void reloadPackage()
	{
		// queryPackage();
		if( pageTask != null && pageTask.getStatus() != PageTask.Status.FINISHED )
		{
			pageTask.cancel( true );
		}
		pageTask = (PageTask)new PageTask().execute();
	}
	
	public void updateThumb(
			String pkgName )
	{
		int findIndex = findPackageIndex( pkgName );
		if( findIndex < 0 )
		{
			return;
		}
		LockInformation info = localList.get( findIndex );
		info.reloadThumb();
		notifyDataSetChanged();
	}
	
	public void updateDownloadSize(
			String pkgName ,
			long downSize ,
			long totalSize )
	{
		int findIndex = findPackageIndex( pkgName );
		if( findIndex < 0 )
		{
			return;
		}
		LockInformation info = localList.get( findIndex );
		info.setDownloadSize( downSize );
		info.setTotalSize( totalSize );
		notifyDataSetChanged();
	}
	
	public Set<String> getPackageNameSet()
	{
		return packageNameSet;
	}
	
	public boolean containPackage(
			String packageName )
	{
		return findPackageIndex( packageName ) >= 0;
	}
	
	private int findPackageIndex(
			String packageName )
	{
		int i = 0;
		for( i = 0 ; i < localList.size() ; i++ )
		{
			if( packageName.equals( localList.get( i ).getPackageName() ) )
			{
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public int getCount()
	{
		if( !FunctionConfig.isShowSystemLockInLocal() )
		{
			return localList.size();
		}
		else
		{
			return localList.size() + 1;
		}
	}
	
	@Override
	public Object getItem(
			int position )
	{
		if( position < localList.size() )
			return localList.get( position );
		else
			return position;
	}
	
	@Override
	public long getItemId(
			int position )
	{
		return position;
	}
	
	@Override
	public View getView(
			int position ,
			View convertView ,
			ViewGroup parent )
	{
		if( FunctionConfig.isBrzh_setWaitBackgroundView() )
		{
			parent.setBackgroundColor( Color.TRANSPARENT );
		}
		ViewHolder viewHolder = null;
		View retView = convertView;
		if( convertView != null )
		{
			viewHolder = (ViewHolder)convertView.getTag();
			viewHolder.viewName.setText( "" );
			viewHolder.viewThumb.setImageResource( R.drawable.default_img );
			viewHolder.imageCover.setVisibility( View.INVISIBLE );
			viewHolder.imageUsed.setVisibility( View.INVISIBLE );
			viewHolder.barPause.setVisibility( View.INVISIBLE );
			viewHolder.barDownloading.setVisibility( View.INVISIBLE );
		}
		else
		{
			viewHolder = new ViewHolder();
			retView = View.inflate( context , R.layout.grid_item , null );
			viewHolder.viewName = (TextView)retView.findViewById( R.id.textAppName );
			viewHolder.viewThumb = (ImageView)retView.findViewById( R.id.imageThumb );
			viewHolder.imageCover = (ImageView)retView.findViewById( R.id.imageCover );
			viewHolder.imageUsed = (ImageView)retView.findViewById( R.id.imageUsed );
			viewHolder.barPause = (ProgressBar)retView.findViewById( R.id.barPause );
			viewHolder.barDownloading = (ProgressBar)retView.findViewById( R.id.barDownloading );
			viewHolder.imageUsed.setVisibility( View.INVISIBLE );
			int itemWidth = (int)( context.getResources().getDisplayMetrics().widthPixels / 3 - 6 * 2 * context.getResources().getDisplayMetrics().density );
			viewHolder.viewThumb.setLayoutParams( new RelativeLayout.LayoutParams(
					(int)( itemWidth + 6 * 2 * context.getResources().getDisplayMetrics().density ) ,
					(int)( itemWidth / 0.6f + 6 * 2 * context.getResources().getDisplayMetrics().density ) ) );
		}
		if( getItem( position ) instanceof LockInformation )
		{
			LockInformation lockInfo = (LockInformation)getItem( position );
			Bitmap imgThumb = getBitmapFromMemCache( lockInfo.getPackageName() );//themeInfo.getThumbImage();
			if( imgThumb == null || imgThumb.isRecycled() )
			{
				if( imgDefaultThumb.isRecycled() || imgThumb == imgDefaultThumb )
				{
					imgDefaultThumb = ( (BitmapDrawable)context.getResources().getDrawable( R.drawable.default_img ) ).getBitmap();
				}
				imgThumb = imgDefaultThumb;
			}
			viewHolder.viewThumb.setImageBitmap( imgThumb );
			/*try
			{
				Log.v( "GridLocalAdapter" , "GridLocalAdapter lockInfo == " + lockInfo.getDisplayName() );
				ImageLoader.getInstance().displayImage( "file:///" + lockInfo.getThumbImagePath( context , lockInfo.getPackageName() , lockInfo.getClassName() ) , viewHolder.viewThumb , options );
			}
			catch( Exception ex )
			{
				Log.v( "ImageLoader" , "GridLocalAdapter Exception == " + ex.getMessage().toString() );
			}*/
			String displayName = lockInfo.getDisplayName();
			String showName = displayName;
			if( displayName.length() > 10 )
			{
				showName = displayName.substring( 0 , 10 ) + "...";
			}
			viewHolder.viewName.setText( showName );
			// viewName.setText(lockInfo.getDisplayName());
			if( FunctionConfig.isEnable_topwise_style() )
			{
				//如果是系统安全锁
				if( currentLockModeIsSecure )
				{
					if( String.valueOf( currentQualityMode ).equals( lockInfo.getLockStyleValue() ) )
					{
						viewHolder.imageCover.setVisibility( View.VISIBLE );
						viewHolder.imageUsed.setVisibility( View.VISIBLE );
					}
					else
					{
						viewHolder.imageCover.setVisibility( View.INVISIBLE );
						viewHolder.imageUsed.setVisibility( View.INVISIBLE );
					}
				}
				//如果是跳转第三方盒子
				else if( !currentThirdPartyLock.getPackageName().equals( "" ) && !currentThirdPartyLock.getClassName().equals( "" ) )
				{
					if( lockInfo.isComponent( currentThirdPartyLock ) )
					{
						viewHolder.imageCover.setVisibility( View.VISIBLE );
						viewHolder.imageUsed.setVisibility( View.VISIBLE );
					}
					else
					{
						viewHolder.imageCover.setVisibility( View.INVISIBLE );
						viewHolder.imageUsed.setVisibility( View.INVISIBLE );
					}
				}
				//如果是鼎智做的解锁,鼎智的解锁每一个锁屏的"system.settings.lockstyle"值都不一样
				else if( !lockInfo.getLockStyleValue().equals( "" ) && !lockInfo.getLockStyleValue().equals( String.valueOf( StaticClass.LOCK_STYLE_VALUE ) ) )
				{
					if( lockInfo.getLockStyleValue().equals( String.valueOf( Settings.System.getInt( context.getContentResolver() , "system.settings.lockstyle" , -1 ) ) ) )
					{
						viewHolder.imageCover.setVisibility( View.VISIBLE );
						viewHolder.imageUsed.setVisibility( View.VISIBLE );
					}
					else
					{
						viewHolder.imageCover.setVisibility( View.INVISIBLE );
						viewHolder.imageUsed.setVisibility( View.INVISIBLE );
					}
				}
				else
				{
					if( lockInfo.isComponent( currentLock ) )
					{
						viewHolder.imageCover.setVisibility( View.VISIBLE );
						viewHolder.imageUsed.setVisibility( View.VISIBLE );
					}
					else
					{
						viewHolder.imageCover.setVisibility( View.INVISIBLE );
						viewHolder.imageUsed.setVisibility( View.INVISIBLE );
					}
				}
			}
			else
			{
				if( lockInfo.isComponent( currentLock ) )
				{
					viewHolder.imageCover.setVisibility( View.VISIBLE );
					viewHolder.imageUsed.setVisibility( View.VISIBLE );
				}
				else
				{
					viewHolder.imageCover.setVisibility( View.INVISIBLE );
					viewHolder.imageUsed.setVisibility( View.INVISIBLE );
				}
			}
			if( lockInfo.isInstalled( context ) || lockInfo.getDownloadStatus() == DownloadStatus.StatusFinish )
			{
				viewHolder.barPause.setVisibility( View.INVISIBLE );
				viewHolder.barDownloading.setVisibility( View.INVISIBLE );
			}
			else
			{
				viewHolder.imageCover.setVisibility( View.VISIBLE );
				if( lockInfo.getDownloadStatus() == DownloadStatus.StatusDownloading )
				{
					viewHolder.barDownloading.setVisibility( View.VISIBLE );
					viewHolder.barPause.setVisibility( View.INVISIBLE );
					viewHolder.barDownloading.setProgress( lockInfo.getDownloadPercent() );
				}
				else
				{
					viewHolder.barDownloading.setVisibility( View.INVISIBLE );
					viewHolder.barPause.setVisibility( View.VISIBLE );
					viewHolder.barPause.setProgress( lockInfo.getDownloadPercent() );
				}
			}
		}
		else
		{
			viewHolder.viewThumb.setImageResource( R.drawable.default_lock );
			viewHolder.viewName.setText( R.string.system_lock );
			if( currentLock == null )
			{
				viewHolder.imageCover.setVisibility( View.INVISIBLE );
				viewHolder.imageUsed.setVisibility( View.INVISIBLE );
			}
			else
			{
				ComponentName cn = new ComponentName( "com.test" , "com.test.testActivity" );
				if( currentLock.compareTo( cn ) == 0 )
				{
					viewHolder.imageCover.setVisibility( View.VISIBLE );
					viewHolder.imageUsed.setVisibility( View.VISIBLE );
				}
				else
				{
					viewHolder.imageCover.setVisibility( View.INVISIBLE );
					viewHolder.imageUsed.setVisibility( View.INVISIBLE );
				}
			}
			viewHolder.barPause.setVisibility( View.INVISIBLE );
			viewHolder.barDownloading.setVisibility( View.INVISIBLE );
		}
		retView.setTag( viewHolder );
		return retView;
	}
	
	public class ViewHolder
	{
		
		ImageView viewThumb;
		TextView viewName;
		ImageView imageCover;
		ImageView imageUsed;
		ProgressBar barPause;
		ProgressBar barDownloading;
	}
	
	public class PageItemTask extends AsyncTask<LockInformation , Integer , LockInformation>
	{
		
		public PageItemTask()
		{
			// Log.e("test", "PageItemTask");
		}
		
		@Override
		protected void onPostExecute(
				LockInformation lockInfo )
		{
			// TODO Auto-generated method stub
			notifyDataSetChanged();
			//			itemTask = null;
		}
		
		@Override
		protected void onPreExecute()
		{
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected LockInformation doInBackground(
				LockInformation ... params )
		{
			// TODO Auto-generated method stub
			/*for (int i = 0; i < getCount(); i++)*/{
				LockInformation themeInfo = (LockInformation)params[0];
				Bitmap imgThumb = themeInfo.getThumbImage();
				if( imgThumb == null || imgThumb.isRecycled() )
				{
					themeInfo.setThumbImage( context , themeInfo.getPackageName() , themeInfo.getClassName() );
				}
				if( themeInfo.isNeedLoadDetail() )
				{
					if( imgThumb == null || imgThumb.isRecycled() )
					{
						themeInfo.loadDetail( context );
						if( themeInfo.getThumbImage() != null )
						{
							StaticClass.saveMyBitmap( context , themeInfo.getPackageName() , themeInfo.getClassName() , themeInfo.getThumbImage() );
						}
					}
					if( themeInfo.getThumbImage() == null )
					{
						downThumb.downloadThumb( themeInfo.getPackageName() , DownloadList.Lock_Type );
					}
				}
				addBitmapToMemoryCache( themeInfo.getPackageName() , themeInfo.getThumbImage() );
			}
			return null;
		}
	}
	
	public class PageTask extends AsyncTask<String , Integer , List<LockInformation>>
	{
		
		public PageTask()
		{
			// localList.clear();
			// packageNameSet.clear();
		}
		
		@Override
		protected void onPostExecute(
				List<LockInformation> result )
		{
			// TODO Auto-generated method stub
			packageNameSet.clear();
			//注释这里解决0000668BUG 个人中心里面选择解锁点击应用之后，系统提示运行终止	偶现
			//			for( LockInformation info : localList )
			//			{
			//				info.disposeThumb();
			//				info = null;
			//			}
			/*localList.clear();
			localList.addAll( result );
			notifyDataSetChanged();*/
			pageTask = null;
			//			if( itemTask != null && itemTask.getStatus() != PageItemTask.Status.FINISHED )
			//			{
			//				itemTask.cancel( true );
			//			}
			//			itemTask = (PageItemTask)new PageItemTask().execute();
			if( backgroundListener != null )
			{
				backgroundListener.setBackground();
			}
		}
		
		@Override
		protected void onPreExecute()
		{
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected List<LockInformation> doInBackground(
				String ... params )
		{
			// TODO Auto-generated method stub
			if( FunctionConfig.isEnable_topwise_style() )
			{
				Looper.prepare();
				currentLockModeIsSecure = getLockModeInfo();
				Looper.loop();
			}
			List<LockInformation> result = queryPackage();
			return result;
		}
		
		protected boolean getLockModeInfo()
		{
			LockPatternUtils utils = new LockPatternUtils( context );
			if( android.os.Build.VERSION.SDK_INT < 17 )
			{
				if( ( utils.isSecure() ) )
				{
					currentQualityMode = utils.getKeyguardStoredPasswordQuality();
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				if( ( utils.isSecure() || utils.isLockScreenDisabled() ) )
				{
					currentQualityMode = utils.getKeyguardStoredPasswordQuality();
					return true;
				}
				else
				{
					return false;
				}
			}
		}
	}
	
	private BackgroundChangeListener backgroundListener;
	
	public void setBackgroundListener(
			BackgroundChangeListener backgroundListener )
	{
		this.backgroundListener = backgroundListener;
	}
	
	interface BackgroundChangeListener
	{
		
		public void setBackground();
	}
	
	public void addBitmapToMemoryCache(
			String key ,
			Bitmap bitmap )
	{
		if( bitmap == null )
		{
			return;
		}
		if( getBitmapFromMemCache( key ) != null )
		{
			mMemoryCache.remove( key );
		}
		mMemoryCache.put( key , bitmap );
	}
	
	public Bitmap getBitmapFromMemCache(
			String key )
	{
		return mMemoryCache.get( key );
	}
}
