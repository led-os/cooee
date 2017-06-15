package com.coco.shortcut.shortcutbox;


import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
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
import com.coco.theme.themebox.util.DownModule;
import com.iLoong.base.themebox.R;


public class OperateGridLocalAdapter extends BaseAdapter
{
	
	private List<OperateInformation> localList = new ArrayList<OperateInformation>();
	private Context context;
	private Bitmap imgDefaultThumb;
	private DownModule downThumb;
	private ComponentName currentTheme = null;
	private PageTask pageTask = null;
	private LruCache<String , Bitmap> mMemoryCache;
	
	public OperateGridLocalAdapter(
			Context cxt ,
			DownModule down )
	{
		context = cxt;
		downThumb = down;
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
	
	private List<OperateInformation> queryPackage()
	{
		// packageNameSet.clear();
		// localList.clear();
		List<OperateInformation> localList = new ArrayList<OperateInformation>();
		OperateService themeSv = new OperateService( context );
		List<OperateInformation> installList = themeSv.queryInstallList();
		for( OperateInformation info : installList )
		{
			info.reloadThumb( context );
			addBitmapToMemoryCache( info.getPackageName() , info.getThumbImage() );
			if( info.getThumbImage() == null )
				new PageItemTask().execute( info );
			localList.add( info );
			// packageNameSet.add(info.getPackageName());
		}
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
		System.out.println( "bitmap = " + bitmap );
		mMemoryCache.put( key , bitmap );
	}
	
	public Bitmap getBitmapFromMemCache(
			String key )
	{
		return mMemoryCache.get( key );
	}
	
	public void onDestory()
	{
		for( OperateInformation info : localList )
		{
			info.disposeThumb();
			info = null;
		}
		if( imgDefaultThumb != null && !imgDefaultThumb.isRecycled() )
		{
			imgDefaultThumb.recycle();
		}
	}
	
	public void updateThumb(
			String pkgName )
	{
		int findIndex = findPackageIndex( pkgName );
		if( findIndex < 0 )
		{
			return;
		}
		OperateInformation info = localList.get( findIndex );
		info.reloadThumb( context );
		addBitmapToMemoryCache( info.getPackageName() , info.getThumbImage() );
		notifyDataSetChanged();
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
	
	@Override
	public View getView(
			int position ,
			View convertView ,
			ViewGroup parent )
	{
		ViewHolder viewHolder = null;
		Log.v( "test" , "PageItemTask: getView position:" + position + " convertView:" + convertView );
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
		final OperateInformation themeInfo = (OperateInformation)getItem( position );
		Bitmap imgThumb = getBitmapFromMemCache( themeInfo.getPackageName() );//themeInfo.getThumbImage();
		//		recycle.add( viewHolder.viewThumb );
		//		Tools.Recyclebitmap( imgDefaultThumb , imgThumb , viewHolder.viewThumb , recycle );
		if( imgThumb == null || imgThumb.isRecycled() )
		{
			imgThumb = imgDefaultThumb;
		}
		viewHolder.viewThumb.setImageBitmap( imgThumb );
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
	
	public class PageItemTask extends AsyncTask<OperateInformation , Integer , OperateInformation>
	{
		
		public PageItemTask()
		{
			// Log.e("test", "PageItemTask");
		}
		
		@Override
		protected void onPostExecute(
				OperateInformation themeInfo )
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
		protected OperateInformation doInBackground(
				OperateInformation ... params )
		{
			// TODO Auto-generated method stub
			//			for( int i = 0 ; i < getCount() ; i++ )
			{
				OperateInformation themeInfo = params[0];
				getItemThumb( themeInfo );
			}
			return null;
		}
		
		private void getItemThumb(
				OperateInformation themeInfo )
		{
			if( themeInfo.getThumbImage() == null )
			{
				themeInfo.reloadThumb( context );
			}
			if( themeInfo.isNeedLoadDetail() )
			{
				Bitmap imgThumb = themeInfo.getThumbImage();
				if( imgThumb == null )
				{
					themeInfo.loadDetail( context );
				}
				if( themeInfo.getThumbImage() == null )
				{
					downThumb.downloadThumb( themeInfo.getPackageName() , DownloadList.Operate_Type );
				}
			}
			addBitmapToMemoryCache( themeInfo.getPackageName() , themeInfo.getThumbImage() );
		}
	}
	
	public class PageTask extends AsyncTask<String , Integer , List<OperateInformation>>
	{
		
		public PageTask()
		{
		}
		
		@Override
		protected void onPostExecute(
				List<OperateInformation> result )
		{
			// TODO Auto-generated method stub
			if( result == null )
			{
				return;
			}
			for( OperateInformation info : localList )
			{
				System.out.println( "info.getbitmap = " + info.getThumbImage() );
				info.disposeThumb();
				info = null;
			}
			localList.clear();
			localList.addAll( result );
			notifyDataSetChanged();
			pageTask = null;
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
		protected List<OperateInformation> doInBackground(
				String ... params )
		{
			// TODO Auto-generated method stub
			List<OperateInformation> result = queryPackage();
			return result;
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
}
