﻿package com.coco.theme.themebox;


import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coco.download.DownloadList;
import com.coco.theme.themebox.database.model.DownloadStatus;
import com.coco.theme.themebox.service.ThemeService;
import com.coco.theme.themebox.util.DownModule;
import com.iLoong.base.themebox.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;


public class ThemeGridLocalAdapter extends BaseAdapter
{
	
	private List<ThemeInformation> localList = new ArrayList<ThemeInformation>();
	private Context context;
	private Bitmap imgDefaultThumb;
	private DownModule downThumb;
	private ComponentName currentTheme = null;
	private ComponentName currentNewTheme = null;
	private PageTask pageTask = null;
	private ReloadCurrentThemeTask mReloadCurrentThemeTask = null;
	private LruCache<String , Bitmap> mMemoryCache;
	DisplayImageOptions options;
	
	public ThemeGridLocalAdapter(
			Context cxt ,
			DownModule down )
	{
		context = cxt;
		downThumb = down;
		//add by liuhailin begin
		options = new DisplayImageOptions.Builder().showStubImage( R.drawable.default_img ).showImageForEmptyUri( R.drawable.default_img ).showImageOnFail( R.drawable.default_img ).cacheInMemory()
				.cacheOnDisc().bitmapConfig( Bitmap.Config.RGB_565 ).build();
		//		options = new DisplayImageOptions.Builder().showStubImage( R.drawable.default_img ).showImageForEmptyUri( R.drawable.default_img ).showImageOnFail( R.drawable.default_img )
		//				.bitmapConfig( Bitmap.Config.RGB_565 ).build();
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
	
	private List<ThemeInformation> queryPackage()
	{
		// packageNameSet.clear();
		// localList.clear();
		//Log.i( "minghui" , "queryPackage()。。。" );
		List<ThemeInformation> localList = new ArrayList<ThemeInformation>();
		ThemeService themeSv = new ThemeService( context );
		List<ThemeInformation> installList = themeSv.queryInstallList();
		//Log.i( "minghui" , "installList = " + installList.toString() );
		for( ThemeInformation info : installList )
		{
			//info.setThumbImage( context , info.getPackageName() , info.getClassName() );
			addBitmapToMemoryCache( info.getPackageName() , info.getThumbImage() );
			if( info.getThumbImage() == null )
			{
				new PageItemTask().execute( info );
			}
			else
			{
				handler.post( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						notifyDataSetChanged();
					}
				} );
			}
			localList.add( info );
			// packageNameSet.add(info.getPackageName());
		}
		currentTheme = themeSv.queryCurrentTheme();
		return localList;
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
	
	public void onDestory()
	{
		for( ThemeInformation info : localList )
		{
			info.disposeThumb();
			info = null;
		}
		if( imgDefaultThumb != null && !imgDefaultThumb.isRecycled() )
		{
			imgDefaultThumb.recycle();
		}
	}
	
	public void reloadCurrent()
	{
		if( mReloadCurrentThemeTask != null && mReloadCurrentThemeTask.getStatus() != PageTask.Status.FINISHED )
		{
			mReloadCurrentThemeTask.cancel( true );
		}
		mReloadCurrentThemeTask = (ReloadCurrentThemeTask)new ReloadCurrentThemeTask().execute();
	}
	
	public void reloadPackage()
	{
		if( pageTask != null && pageTask.getStatus() != PageTask.Status.FINISHED )
		{
			pageTask.cancel( true );
		}
		pageTask = (PageTask)new PageTask().execute();
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
		return localList.size();
	}
	
	@Override
	public Object getItem(
			int position )
	{
		// Log.e("test", "PageItemTask getItem:" + position);
		return localList.get( position );
	}
	
	@Override
	public long getItemId(
			int position )
	{
		return position;
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
	
	@Override
	public View getView(
			int position ,
			View convertView ,
			ViewGroup parent )
	{
		parent.setBackgroundColor( Color.TRANSPARENT );
		ViewHolder viewHolder = null;
		if( convertView != null )
		{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		else
		{
			viewHolder = new ViewHolder();
			convertView = View.inflate( context , R.layout.grid_item , null );
			viewHolder.viewName = (TextView)convertView.findViewById( R.id.textAppName );
			viewHolder.viewThumb = (ImageView)convertView.findViewById( R.id.imageThumb );
			viewHolder.imageCover = (ImageView)convertView.findViewById( R.id.imageCover );
			viewHolder.imageUsed = (ImageView)convertView.findViewById( R.id.imageUsed );
			viewHolder.barPause = (ProgressBar)convertView.findViewById( R.id.barPause );
			viewHolder.barDownloading = (ProgressBar)convertView.findViewById( R.id.barDownloading );
			int itemWidth = (int)( context.getResources().getDisplayMetrics().widthPixels / 3 - 6 * 2 * context.getResources().getDisplayMetrics().density );
			viewHolder.viewThumb.setLayoutParams( new RelativeLayout.LayoutParams(
					(int)( itemWidth + 6 * 2 * context.getResources().getDisplayMetrics().density ) ,
					(int)( itemWidth / 0.6f + 6 * 2 * context.getResources().getDisplayMetrics().density ) ) );
		}
		final ThemeInformation themeInfo = (ThemeInformation)getItem( position );
		Bitmap imgThumb = getBitmapFromMemCache( themeInfo.getPackageName() );//themeInfo.getThumbImage();
		//		//		recycle.add( viewHolder.viewThumb );
		//		//		Tools.Recyclebitmap( imgDefaultThumb , imgThumb , viewHolder.viewThumb , recycle );
		if( imgThumb == null || imgThumb.isRecycled() )
		{
			imgThumb = imgDefaultThumb;
		}
		viewHolder.viewThumb.setImageBitmap( imgThumb );
		//ImageLoader.getInstance().displayImage( "file:///" + themeInfo.getThumbImagePath( context , themeInfo.getPackageName() , themeInfo.getClassName() ) , viewHolder.viewThumb , options );
		viewHolder.viewName.setText( themeInfo.getDisplayName() );
		viewHolder.barPause.setVisibility( View.VISIBLE );
		if( currentTheme != null && currentTheme.getPackageName().equals( themeInfo.getPackageName() ) && currentTheme.getClassName().equals( themeInfo.getClassName() ) )
		{
			viewHolder.imageCover.setVisibility( View.VISIBLE );
			viewHolder.imageUsed.setVisibility( View.VISIBLE );
		}
		else
		{
			viewHolder.imageCover.setVisibility( View.INVISIBLE );
			viewHolder.imageUsed.setVisibility( View.INVISIBLE );
		}
		if( themeInfo.isInstalled( context ) || themeInfo.getDownloadStatus() == DownloadStatus.StatusFinish )
		{
			viewHolder.barPause.setVisibility( View.INVISIBLE );
			viewHolder.barDownloading.setVisibility( View.INVISIBLE );
		}
		else
		{
			viewHolder.imageCover.setVisibility( View.VISIBLE );
			if( themeInfo.getDownloadStatus() == DownloadStatus.StatusDownloading )
			{
				viewHolder.barDownloading.setVisibility( View.VISIBLE );
				viewHolder.barPause.setVisibility( View.INVISIBLE );
				viewHolder.barDownloading.setProgress( themeInfo.getDownloadPercent() );
			}
			else
			{
				viewHolder.barDownloading.setVisibility( View.INVISIBLE );
				viewHolder.barPause.setVisibility( View.VISIBLE );
				viewHolder.barPause.setProgress( themeInfo.getDownloadPercent() );
			}
		}
		convertView.setTag( viewHolder );
		return convertView;
	}
	
	public class PageItemTask extends AsyncTask<ThemeInformation , Integer , ThemeInformation>
	{
		
		public PageItemTask()
		{
			// Log.e("test", "PageItemTask");
		}
		
		@Override
		protected void onPostExecute(
				ThemeInformation themeInfo )
		{
			// TODO Auto-generated method stub
			notifyDataSetChanged();
		}
		
		@Override
		protected void onPreExecute()
		{
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected ThemeInformation doInBackground(
				ThemeInformation ... params )
		{
			// TODO Auto-generated method stub
			//			for( int i = 0 ; i < getCount() ; i++ )
			{
				ThemeInformation themeInfo = params[0];
				//Log.i( "minghui" , "doInBackground themeInfo =  " + themeInfo );
				getItemThumb( themeInfo );
			}
			return null;
		}
		
		private void getItemThumb(
				ThemeInformation themeInfo )
		{
			if( themeInfo.getThumbImage() == null )
			{
				themeInfo.setThumbImage( context , themeInfo.getPackageName() , themeInfo.getClassName() );
			}
			if( themeInfo.isNeedLoadDetail() )
			{
				Bitmap imgThumb = themeInfo.getThumbImage();
				if( imgThumb == null )
				{
					themeInfo.loadDetail( context );
					if( themeInfo.getThumbImage() != null )
					{
						StaticClass.saveMyBitmap( context , themeInfo.getPackageName() , themeInfo.getClassName() , themeInfo.getThumbImage() );
					}
				}
				if( themeInfo.getThumbImage() == null )
				{
					downThumb.downloadThumb( themeInfo.getPackageName() , DownloadList.Theme_Type );
				}
			}
			addBitmapToMemoryCache( themeInfo.getPackageName() , themeInfo.getThumbImage() );
		}
	}
	
	public class PageTask extends AsyncTask<String , Integer , List<ThemeInformation>>
	{
		
		public PageTask()
		{
		}
		
		@Override
		protected void onPostExecute(
				List<ThemeInformation> result )
		{
			// TODO Auto-generated method stub
			if( result == null )
			{
				return;
			}
			for( ThemeInformation info : localList )
			{
				info.disposeThumb();
				info = null;
			}
			localList.clear();
			localList.addAll( result );
			notifyDataSetChanged();
			pageTask = null;
			//			if( itemTask != null && itemTask.getStatus() != PageItemTask.Status.FINISHED )
			//			{
			//				itemTask.cancel( true );
			//			}
			//			itemTask = (PageItemTask)new PageItemTask().execute();
		}
		
		@Override
		protected void onPreExecute()
		{
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected List<ThemeInformation> doInBackground(
				String ... params )
		{
			// TODO Auto-generated method stub
			List<ThemeInformation> result = queryPackage();
			return result;
		}
	}
	
	public class ReloadCurrentThemeTask extends AsyncTask<String , Integer , Boolean>
	{
		
		public ReloadCurrentThemeTask()
		{
		}
		
		@Override
		protected void onPostExecute(
				Boolean value )
		{
			// TODO Auto-generated method stub
			if( currentNewTheme.equals( currentTheme ) )
			{
			}
			else
			{
				currentTheme = currentNewTheme;
				notifyDataSetChanged();
			}
		}
		
		@Override
		protected void onPreExecute()
		{
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(
				String ... params )
		{
			// TODO Auto-generated method stub
			ThemeService themeSv = new ThemeService( context );
			currentNewTheme = themeSv.queryCurrentTheme();
			return true;
		}
	}
}
