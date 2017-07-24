package com.coco.scene.scenebox;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
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


public class SceneGridLocalAdapter extends BaseAdapter
{
	
	private List<SceneInformation> localList = new ArrayList<SceneInformation>();
	private Context context;
	private Bitmap imgDefaultThumb;
	private DownModule downThumb;
	private Set<String> packageNameSet = new HashSet<String>();
	private ComponentName currentScene = new ComponentName( "" , "" );
	//	private Set<ImageView> recycle = new HashSet<ImageView>();;
	private PageTask pageTask;
	private LruCache<String , Bitmap> mMemoryCache;
	
	public SceneGridLocalAdapter(
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
	
	public void onDestory()
	{
		for( SceneInformation info : localList )
		{
			info.disposeThumb();
			info = null;
		}
		if( imgDefaultThumb != null && !imgDefaultThumb.isRecycled() )
		{
			imgDefaultThumb.recycle();
		}
	}
	
	private List<SceneInformation> queryPackage()
	{
		packageNameSet.clear();
		// localList.clear();
		List<SceneInformation> localList = new ArrayList<SceneInformation>();
		SceneService themeSv = new SceneService( context );
		List<SceneInformation> installList = themeSv.queryInstallList();
		for( SceneInformation info : installList )
		{
			info.setThumbImage( context , info.getPackageName() , info.getClassName() );
			addBitmapToMemoryCache( info.getPackageName() , info.getThumbImage() );
			if( info.isNeedLoadDetail() )
			{
				new PageItemTask().execute( info );
			}
			localList.add( info );
			packageNameSet.add( info.getPackageName() );
		}
		currentScene = themeSv.queryCurrentScene();
		return localList;
	}
	
	public void reloadPackage()
	{
		// queryPackage();
		// ((Activity)context).runOnUiThread(new Runnable(){
		// @Override
		// public void run() {
		// notifyDataSetChanged();
		// }
		// });
		if( pageTask != null && pageTask.getStatus() != PageTask.Status.FINISHED )
		{
			pageTask.cancel( true );
		}
		pageTask = (PageTask)new PageTask().execute();
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
		SceneInformation info = localList.get( findIndex );
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
		return localList.size();
	}
	
	@Override
	public Object getItem(
			int position )
	{
		return localList.get( position );
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
		// TODO Auto-generated method stub
		// View retView = convertView;
		// if (retView == null) {
		// retView = View.inflate(context, R.layout.grid_item_large, null);
		// }
		ViewHolder viewHolder = null;
		if( convertView != null )
		{
			viewHolder = (ViewHolder)convertView.getTag();
			viewHolder.viewName.setText( "" );
			viewHolder.viewThumb.setImageBitmap( imgDefaultThumb );
			viewHolder.imageCover.setVisibility( View.INVISIBLE );
			viewHolder.imageUsed.setVisibility( View.INVISIBLE );
			viewHolder.barPause.setVisibility( View.INVISIBLE );
			viewHolder.barDownloading.setVisibility( View.INVISIBLE );
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
			viewHolder.imageUsed.setVisibility( View.INVISIBLE );
			int itemWidth = (int)( context.getResources().getDisplayMetrics().widthPixels / 3 - 6 * 2 * context.getResources().getDisplayMetrics().density );
			viewHolder.viewThumb.setLayoutParams( new RelativeLayout.LayoutParams(
					(int)( itemWidth + 6 * 2 * context.getResources().getDisplayMetrics().density ) ,
					(int)( itemWidth / 0.6f + 6 * 2 * context.getResources().getDisplayMetrics().density ) ) );
		}
		SceneInformation Info = (SceneInformation)getItem( position );
		Bitmap imgThumb = mMemoryCache.get( Info.getPackageName() );//Info.getThumbImage();
		//		recycle.add( viewHolder.viewThumb );
		//		Tools.Recyclebitmap( imgDefaultThumb , imgThumb , viewHolder.viewThumb , recycle );
		if( imgThumb == null || imgThumb.isRecycled() )
		{
			imgThumb = imgDefaultThumb;
		}
		viewHolder.viewThumb.setImageBitmap( imgThumb );
		viewHolder.viewName.setText( Info.getDisplayName() );
		if( currentScene.getPackageName().equals( Info.getPackageName() ) && currentScene.getClassName().equals( Info.getClassName() ) )
		{
			viewHolder.imageCover.setVisibility( View.VISIBLE );
			viewHolder.imageUsed.setVisibility( View.VISIBLE );
		}
		else
		{
			viewHolder.imageCover.setVisibility( View.INVISIBLE );
			viewHolder.imageUsed.setVisibility( View.INVISIBLE );
		}
		if( Info.isInstalled( context ) || Info.getDownloadStatus() == DownloadStatus.StatusFinish )
		{
			viewHolder.barPause.setVisibility( View.INVISIBLE );
			viewHolder.barDownloading.setVisibility( View.INVISIBLE );
		}
		else
		{
			viewHolder.imageCover.setVisibility( View.VISIBLE );
			if( Info.getDownloadStatus() == DownloadStatus.StatusDownloading )
			{
				viewHolder.barDownloading.setVisibility( View.VISIBLE );
				viewHolder.barPause.setVisibility( View.INVISIBLE );
				viewHolder.barDownloading.setProgress( Info.getDownloadPercent() );
			}
			else
			{
				viewHolder.barDownloading.setVisibility( View.INVISIBLE );
				viewHolder.barPause.setVisibility( View.VISIBLE );
				viewHolder.barPause.setProgress( Info.getDownloadPercent() );
			}
		}
		convertView.setTag( viewHolder );
		return convertView;
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
	
	public class PageItemTask extends AsyncTask<SceneInformation , Integer , SceneInformation>
	{
		
		public PageItemTask()
		{
		}
		
		@Override
		protected void onPostExecute(
				SceneInformation lockInfo )
		{
			notifyDataSetChanged();
		}
		
		@Override
		protected void onPreExecute()
		{
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		@Override
		protected SceneInformation doInBackground(
				SceneInformation ... params )
		{
			// TODO Auto-generated method stub
			SceneInformation themeInfo = (SceneInformation)params[0];
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
				downThumb.downloadThumb( themeInfo.getPackageName() , DownloadList.Scene_Type );
			}
			addBitmapToMemoryCache( themeInfo.getPackageName() , themeInfo.getThumbImage() );
			return themeInfo;
		}
	}
	
	public class PageTask extends AsyncTask<String , Integer , List<SceneInformation>>
	{
		
		public PageTask()
		{
		}
		
		@Override
		protected void onPostExecute(
				List<SceneInformation> result )
		{
			// TODO Auto-generated method stub
			packageNameSet.clear();
			for( SceneInformation info : localList )
			{
				info.disposeThumb();
				info = null;
			}
			localList.clear();
			localList.addAll( result );
			if( result != null )
			{
				for( SceneInformation info : result )
				{
					packageNameSet.add( info.getPackageName() );
				}
			}
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
		protected List<SceneInformation> doInBackground(
				String ... params )
		{
			// TODO Auto-generated method stub
			List<SceneInformation> result = queryPackage();
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
