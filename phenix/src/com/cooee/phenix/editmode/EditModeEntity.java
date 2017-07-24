package com.cooee.phenix.editmode;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.editmode.interfaces.IEditControlCallBack;
import com.cooee.phenix.editmode.item.EditModelItem;
import com.cooee.phenix.editmode.item.EditModelWallpaperItem;
import com.cooee.phenix.editmode.provider.RecursiveFileObserver;
import com.cooee.phenix.editmode.provider.RecursiveFileObserver.FileUpdateListener;


public class EditModeEntity extends FrameLayout implements FileUpdateListener , IOnThemeChanged//换主题不重启
{
	
	private RecyclerView mRecyclerView = null;
	private View mSpringIndicator = null;
	private ValueAnimator mSpringAlphaAnimator = null;
	private Map<String , MyAsyncTack> editLoadTask = new HashMap<String , EditModeEntity.MyAsyncTack>();
	private ArrayList<EditModelItem> mItemList = new ArrayList<EditModelItem>();
	private EditModeItemAdapter mEditModeItemAdapter = null;
	private EditModeModel mModel;
	public static final String TAB_THEME_KEY = "EditThemeProvider";
	public static final String TAB_WALLPAPER_KEY = "EditWallpaperProvider";
	public static final int ITME_SUM = 4;
	private onItemClickListener mOnItemClickListener = null;
	private IEditControlCallBack mEditControlCallBack = null;
	private HashMap<String , ArrayList<EditModelItem>> mCacheMap = new HashMap<String , ArrayList<EditModelItem>>();
	private RecursiveFileObserver mFileObserver;
	private final static String FILE_DIR = "Coco/Wallpaper/App";
	private String currentKey = null;
	private int mAlternateDuration = 150;
	private ValueAnimator valueAnimation;
	private float originY = -1;
	
	public EditModeEntity(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		mModel = new EditModeModel( context );
	}
	
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		mSpringIndicator = this.findViewById( R.id.editmode_progress );
		removeView( mSpringIndicator );
		mRecyclerView = (RecyclerView)findViewById( R.id.recyclerview_horizontal );
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager( getContext() );
		linearLayoutManager.setOrientation( LinearLayoutManager.HORIZONTAL );
		mRecyclerView.setLayoutManager( linearLayoutManager );
		mEditModeItemAdapter = new EditModeItemAdapter( getContext() , mItemList );
		mRecyclerView.setAdapter( mEditModeItemAdapter );
		mOnItemClickListener = new onItemClickListener() {
			
			@Override
			public void onItemClick(
					View v ,
					int position )
			{
				// TODO Auto-generated method stub
				EditModelItem item = mItemList.get( position );
				item.onItemClick( mEditControlCallBack , getContext() );
			}
		};

		mEditModeItemAdapter.setOnItemClickListener( mOnItemClickListener );
		mFileObserver = new RecursiveFileObserver( FILE_DIR );
		mFileObserver.startAllWatching();
		mFileObserver.setFileUpdateListener( this );
		mAlternateDuration = LauncherDefaultConfig.getInt( R.integer.config_editmode_secondary_duration );
	}
	
	/**
	 * 通过key值显示对应的listView，若是已经加载过数据，则不需要显示SpringIndicator，若还未加载过数据，则需要显示SpringIndicator
	 * @param key
	 */
	public void showHorizontalListView(
			String key ,
			View brother ,
			boolean isAnim )
	{
		if( valueAnimation != null && valueAnimation.isRunning() )
		{
			return;
		}
		MyAsyncTack loadTask = editLoadTask.get( key );
		if( loadTask != null )
		{
			if( !loadTask.isLoadFinish() )
				return;
		}
		if( originY == -1 )
		{
			originY = brother.getY();
		}
		if( isAnim )
		{
			oneAlternateWithTwo( brother , this );
		}
		else
		{
			brother.setVisibility( View.GONE );
			setY( originY );
			setAlpha( 1.0f );
			setVisibility( View.VISIBLE );
		}
		currentKey = key;
		if( loadTask == null )//如果此时未加载过该View，则先执行异步数据加载
		{
			mItemList.clear();
			mEditModeItemAdapter.notifyDataSetChanged();
			MyAsyncTack myAsyncTack = new MyAsyncTack( true );
			myAsyncTack.execute( key );
			editLoadTask.put( key , myAsyncTack );
		}
		else
		{
			if( !key.equals( mRecyclerView.getTag() ) )
			{
				loadListViewByData( mCacheMap.get( key ) , key );
			}
		}
	}
	
	/**
	 * 通过获得的数据加载出对应的HorizontalListView
	 * @param editModelItems
	 * @param key
	 */
	private void loadListViewByData(
			ArrayList<EditModelItem> editModelItems ,
			String key )
	{
		if( key.equals( currentKey ) )
		{
			mItemList.clear();
			mItemList.addAll( editModelItems );
			mEditModeItemAdapter.notifyDataSetChanged();
			mRecyclerView.setTag( key );
		}
	}
	
	public void onAppsUpdate(
			String pkg ,
			String action )
	{
		if( !TextUtils.isEmpty( pkg ) )
		{
			if( pkg.startsWith( "com.coco.themes." ) )
			{
				if( mCacheMap.get( TAB_THEME_KEY ) == null || mCacheMap.size() == 0 )//说明主题还没有查找过数据显示，等到需要显示时会有更新的
				{
					return;
				}
				if( Intent.ACTION_PACKAGE_ADDED.equals( action ) )
				{
					ArrayList<EditModelItem> items = mModel.loadAddedPackageEditModelItem( pkg , TAB_THEME_KEY );
					int size = mCacheMap.get( TAB_THEME_KEY ).size();
					mCacheMap.get( TAB_THEME_KEY ).addAll( size - getMustBeInLastItemsSum( TAB_THEME_KEY ) , items );
				}
				else if( Intent.ACTION_PACKAGE_REMOVED.equals( action ) )
				{
					for( EditModelItem item : mCacheMap.get( TAB_THEME_KEY ) )
					{
						if( item.getPackageNameKey().equals( pkg ) )
						{
							mCacheMap.get( TAB_THEME_KEY ).remove( item );
							break;
						}
					}
				}
				if( TAB_THEME_KEY.equals( mRecyclerView.getTag() ) )
				{
					mItemList.clear();
					mItemList.addAll( mCacheMap.get( TAB_THEME_KEY ) );
					mEditModeItemAdapter.notifyDataSetChanged();
				}
			}
		}
	}
	
	private class MyAsyncTack extends AsyncTask<String , Integer , Object>
	{
		
		// YANGTIANYU@2015/12/28 ADD START
		/**是否完成了编辑页标签内容的载入*/
		private boolean isLoadFinish = false;
		// YANGTIANYU@2015/12/28 ADD END
		private String mKey = null;
		private boolean mShowSpringIndicator = true;//判断是否要显示SpringIndicator动画
		
		public MyAsyncTack(
				boolean showSpringIndicator )
		{
			mShowSpringIndicator = showSpringIndicator;
		}
		
		@Override
		protected Object doInBackground(
				String ... params )
		{
			mKey = params[0];
			return mModel.loadEditModelItemByKey( mKey );
		}
		@Override
		protected void onCancelled(//当任务被取消，请求的数据没有作用，进行释放
				Object result )
		{
			// TODO Auto-generated method stub
			super.onCancelled( result );
			if( result instanceof ArrayList<?> )
			{
				recycleBeforeData( (ArrayList<EditModelItem>)result );
			}
		}
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			if( mShowSpringIndicator )
			{
				if( mSpringIndicator.getParent() == null )
					EditModeEntity.this.addView( mSpringIndicator );
			}
		}
		
		@Override
		protected void onPostExecute(
				Object result )
		{
			super.onPostExecute( result );
			long duration = 400;
			if( result instanceof ArrayList<?> )
			{
				//更新数据之前先将先前的释放
				if( mCacheMap.get( mKey ) != null )
				{
					recycleBeforeData( mCacheMap.get( mKey ) );
				}
				mCacheMap.put( mKey , (ArrayList<EditModelItem>)result );
				loadListViewByData( (ArrayList<EditModelItem>)result , mKey );
			}
			if( mShowSpringIndicator )
			{
				// zhangjin@2015/12/29 DEL START
				//mSpringIndicator.stopPageScroll();
				// zhangjin@2015/12/29 DEL END
				if( mSpringAlphaAnimator != null && mSpringAlphaAnimator.isRunning() )
				{
					mSpringAlphaAnimator.cancel();
					mSpringAlphaAnimator = null;
				}
				if( mSpringAlphaAnimator == null )
				{
					mSpringAlphaAnimator = ValueAnimator.ofFloat( 1f , 0f );
					mSpringAlphaAnimator.setDuration( duration );
				}
				mSpringAlphaAnimator.addListener( new AnimatorListener() {
					
					@Override
					public void onAnimationStart(
							Animator animation )
					{
					}
					
					@Override
					public void onAnimationRepeat(
							Animator animation )
					{
					}
					
					@Override
					public void onAnimationEnd(
							Animator animation )
					{
						mSpringAlphaAnimator = null;
						EditModeEntity.this.removeView( mSpringIndicator );
						mSpringIndicator.setAlpha( 1f );
					}
					
					@Override
					public void onAnimationCancel(
							Animator animation )
					{
					}
				} );
				mSpringAlphaAnimator.addUpdateListener( new AnimatorUpdateListener() {
					
					@Override
					public void onAnimationUpdate(
							ValueAnimator animation )
					{
						float alpha = (Float)animation.getAnimatedValue();
						mSpringIndicator.setAlpha( alpha );
					}
				} );
				mSpringAlphaAnimator.start();
			}
			// YANGTIANYU@2015/12/28 ADD START
			// 【i_0013126】
			isLoadFinish = true;
			// YANGTIANYU@2015/12/28 ADD END
		}
		
		public boolean isLoadFinish()
		{
			return isLoadFinish;
		}
	}
	
	private int getMustBeInLastItemsSum(
			//有些item必须在列表的最后，有新增时，需要加在这个之前
			String key )
	{
		if( TAB_THEME_KEY.equals( key ) )//主题包括默认主题和美化中心
		{
			return 2;
		}
		else if( TAB_WALLPAPER_KEY.equals( key ) )//壁纸包括美化中心
		{
			return 1;
		}
		return 0;
	}
	
	/**
	 * 删除对应Key的HorizontalListView中的view
	 * @param key
	 * @param removePkgs
	 */
	public void removeApp(
			String key ,
			List<String> removePkgs )
	{
	}
	
	public boolean onBackPressed(
			ViewGroup parent )
	{
		if( this.getVisibility() == View.VISIBLE )
		{
			if( valueAnimation != null && valueAnimation.isRunning() )
			{
				exitSecondaryEditMode( parent );
				return true;
			}
			View brother = parent.findViewById( R.id.overview_panel_button );
			brother.setVisibility( View.VISIBLE );
			oneAlternateWithTwo( this , brother );
			return true;
		}
		return false;
	}
	
	/**
	 * 退出编辑模式的时候要做出的一些操作
	 */
	public void exitSecondaryEditMode(
			ViewGroup parent )//父节点
	{
		if( this.getVisibility() == View.VISIBLE )
		{
			if( valueAnimation != null && valueAnimation.isRunning() )
			{
				valueAnimation.cancel();
			}
			this.setVisibility( View.GONE );
			View brother = parent.findViewById( R.id.overview_panel_button );
			brother.setY( originY );
			brother.setVisibility( View.VISIBLE );
			brother.setAlpha( 1.0f );
			//			parent.removeView( this );//zhujieping del,不能从父view中移除，否则监听不到sd卡中文件夹的变化
		}
	}
	
	public void changeViewSelect(
			String key ,
			int itemIndex )
	{
	}
	
	public void setEditControlCallBack(
			IEditControlCallBack mEditControlCallBack )
	{
		this.mEditControlCallBack = mEditControlCallBack;
	}
	
	public interface onItemClickListener
	{
		
		public void onItemClick(
				View v ,
				int position );
	}
	
	@Override
	public void updateWallpaperList()
	{
		// TODO Auto-generated method stub
		//这里先不着急对以前的数据释放，等加载出新内容之后释放
		post( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( editLoadTask.get( TAB_WALLPAPER_KEY ) != null )
				{
					if( !editLoadTask.get( TAB_WALLPAPER_KEY ).isLoadFinish() )
					{
						editLoadTask.get( TAB_WALLPAPER_KEY ).cancel( true );
					}
					MyAsyncTack myAsyncTack = new MyAsyncTack( true );
					myAsyncTack.execute( TAB_WALLPAPER_KEY );
					editLoadTask.put( TAB_WALLPAPER_KEY , myAsyncTack );
				}
			}
		} );
	}
	
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		if( mCacheMap.get( TAB_THEME_KEY ) != null )
		{
			mModel.updateEditModelItem( TAB_THEME_KEY , mCacheMap.get( TAB_THEME_KEY ) );
			if( TAB_THEME_KEY.equals( mRecyclerView.getTag() ) )
			{
				post( new Runnable() {
					
					public void run()
					{
						mEditModeItemAdapter.notifyDataSetChanged();
					}
				} );
			}
		}
	}
	
	private void oneAlternateWithTwo(
			final View one ,
			final View two )
	{
		if( one.getVisibility() != View.VISIBLE )
			one.setVisibility( View.VISIBLE );
		if( two.getVisibility() != View.VISIBLE )
			two.setVisibility( View.VISIBLE );
		valueAnimation = ValueAnimator.ofFloat( 0 , 1 );
		valueAnimation.setDuration( mAlternateDuration );
		valueAnimation.start();
		valueAnimation.addUpdateListener( new AnimatorUpdateListener() {
			
			
			@Override
			public void onAnimationUpdate(
					ValueAnimator animation )
			{
				float yValue2 = (Float)animation.getAnimatedValue();
				one.setY( yValue2 * one.getHeight() + originY );
				one.setAlpha( 1 - yValue2 );
				two.setAlpha( yValue2 );
				two.setY( originY + ( 1 - yValue2 ) * one.getHeight() );
			}
		} );
		valueAnimation.addListener( new AnimatorListener() {
			
			
			@Override
			public void onAnimationStart(
					Animator animation )
			{
				// TODO Auto-generated method stub

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
				one.setVisibility( View.GONE );
			}
			
			@Override
			public void onAnimationCancel(
					Animator animation )
			{
				// TODO Auto-generated method stub
				two.setY( originY );
				two.setAlpha( 1 );
			}
		} );
	}
	
	private void recycleBeforeData(
			ArrayList<EditModelItem> list )
	{
		for( EditModelItem item : list )//更新壁纸数据，将之前的图片释放
		{
			if( item.getBitmap() != null && !item.getBitmap().isRecycled() )
			{
				item.getBitmap().recycle();
				item.setBitmap( null );
			}
			if( item instanceof EditModelWallpaperItem )
			{
				( (EditModelWallpaperItem)item ).setWallPaperFile( null );
			}
		}
	}

}
