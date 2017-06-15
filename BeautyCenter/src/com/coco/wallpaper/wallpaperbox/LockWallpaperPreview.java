package com.coco.wallpaper.wallpaperbox;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coco.lock2.lockbox.LockInformation;
import com.coco.lock2.lockbox.util.ContentConfig;
import com.coco.lock2.lockbox.util.LockManager;
import com.coco.theme.themebox.ActivityManager;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Log;
import com.coco.theme.themebox.util.Tools;
import com.iLoong.base.themebox.R;


public class LockWallpaperPreview extends Activity implements AdapterView.OnItemSelectedListener , OnClickListener
{
	
	private String wallpaperPath = "launcher/lockwallpapers";
	private String customLockWallpaperPath;
	private boolean useCustomLockWallpaper = false;
	private Gallery mGallery;
	private ImageView mImageView;
	private Bitmap mBitmap;
	private ArrayList<String> mThumbs = new ArrayList<String>( 24 );
	private List<Drawable> localBmp = new ArrayList<Drawable>();
	private Map<String , Drawable> wallpaperMap = new HashMap<String , Drawable>();
	private Boolean isLoadComplete = false;
	private ViewPager mViewPager;
	private Drawable currentDrawable = null;
	private Drawable defaultDrawable = null;
	private WallpaperLoader mLoader;
	private Context mThemeContext;
	ImageAdapter mAdapter;
	LinearLayout setwallpaper;
	LocalViewPagerAdapter mLocalViewPagerAdapter;
	private RelativeLayout relativeNormal;
	private ImageButton delete;
	int position = 0;
	private ComponentName currentLock;
	private Boolean isLockSupportChangeWallpaper = false;
	// @2014/12/18 ADD START
	/**
	 * 从主题盒子进程读取配置文件中的这个开关的状态（我们是在主题盒子中创建了一个sharedpreferences）remove_enable_support_lockwallpaper_judge:(兴软)去掉关于第三方锁屏或者系统锁屏能否支持锁屏壁纸的判断 true是去掉判断，false是代码中回去判断能否这是锁屏壁纸
	 */
	private SharedPreferences preferences;
	private Boolean preResult = false;
	
	@Override
	public void onCreate(
			Bundle icicle )
	{
		super.onCreate( icicle );
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		ActivityManager.pushActivity( this );
		mThemeContext = this;
		setContentView( R.layout.preview_wallpaper );
		// @2014/12/18 ADD START
		preferences = mThemeContext.getSharedPreferences( "isRemove_enable_support_lockwallpaper_judge" , MODE_PRIVATE );
		preResult = preferences.getBoolean( "key" , false );
		// @2014/12/18 ADD END
		defaultDrawable = mThemeContext.getResources().getDrawable( R.drawable.default_img_large );
		findViewById( R.id.progressBar ).setVisibility( View.GONE );
		mGallery = (Gallery)findViewById( R.id.thumbs );
		delete = (ImageButton)findViewById( R.id.btnDel );
		delete.setVisibility( View.GONE );
		mGallery.setOnItemSelectedListener( this );
		mAdapter = new ImageAdapter( this );
		mGallery.setAdapter( mAdapter );
		mGallery.setCallbackDuringFling( false );
		TextView title = (TextView)findViewById( R.id.wallpaper );
		title.setText( R.string.lock_wallpaper );
		findViewById( R.id.btnReturn ).setOnClickListener( this );
		setwallpaper = (LinearLayout)findViewById( R.id.setwallpaper );
		//<> liuhailin@2014-10-13 modify begin
		findViewById( R.id.setlockwallpaper ).setOnClickListener( this );
		//<> liuhailin@2014-10-13 modify end
		//setwallpaper.setText( R.string.set_lock_wallpaper );
		setwallpaper.setVisibility( View.VISIBLE );
		findViewById( R.id.setdesktopwallpaper ).setOnClickListener( this );
		findViewById( R.id.btnBuy ).setOnClickListener( this );
		findViewById( R.id.btnDownload ).setOnClickListener( this );
		mViewPager = (ViewPager)findViewById( R.id.previewPager );
		mImageView = (ImageView)findViewById( R.id.preview );
		mImageView.setScaleType( ScaleType.CENTER_CROP );
		if( FunctionConfig.isEnablePreviewWallpaperByAdapter() )
		{
			mImageView.setVisibility( View.GONE );
			mLocalViewPagerAdapter = new LocalViewPagerAdapter( this );
			mViewPager.setAdapter( mLocalViewPagerAdapter );
		}
		else
		{
			mImageView.setVisibility( View.VISIBLE );
			mViewPager.setVisibility( View.GONE );
		}
		mViewPager.setOnPageChangeListener( new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(
					int arg0 )
			{
				// TODO Auto-generated method stub
				mGallery.setSelection( arg0 );
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
		isLoadComplete = false;
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				isLockSupportChangeWallpaper = getCurrentLockInfoDZ();
			}
		} ).start();
		initInfo();
		initPreviewButton();//按钮显示初始化
	}
	
	private Boolean getCurrentLockInfoDZ()
	{
		LockManager mgr = new LockManager( this );
		List<LockInformation> installList = mgr.queryInstallList();
		for( LockInformation infor : installList )
		{
			Context dstContext = null;
			try
			{
				dstContext = createPackageContext( infor.getPackageName() , Context.CONTEXT_IGNORE_SECURITY );
			}
			catch( NameNotFoundException e )
			{
				e.printStackTrace();
				return false;
			}
			ContentConfig destContent = new ContentConfig();
			destContent.loadConfig( dstContext , infor.getClassName() );
			if( destContent.getLockStyleValue().equals( String.valueOf( Settings.System.getInt( mThemeContext.getContentResolver() , "system.settings.lockstyle" , -1 ) ) ) )
			{
				return true;
			}
		}
		if( getcurrentLockInfo() )
		{
			return true;
		}
		return false;
	}
	
	private Boolean getcurrentLockInfo()
	{
		LockManager mgr = new LockManager( this );
		currentLock = mgr.queryCurrentLock();
		//第三方锁屏也要支持换壁纸,第三方换壁纸也要从我们的路径读取图片
		if( "com.third.test".equals( currentLock.getPackageName() ) )
		{
			return true;
		}
		Context dstContext = null;
		try
		{
			dstContext = createPackageContext( currentLock.getPackageName() , Context.CONTEXT_IGNORE_SECURITY );
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
			return false;
		}
		ContentConfig destContent = new ContentConfig();
		destContent.loadConfig( dstContext , currentLock.getClassName() );
		if( destContent.getBackgroundPathString().equals( "" ) )
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	private void initInfo()
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				findLockWallpapers();
				runOnUiThread( new Runnable() {
					
					public void run()
					{
						if( mAdapter != null )
						{
							mAdapter.notifyDataSetChanged();
						}
						//此项不需要通知更新，会导致先刷新第一张后再跳转到相应的项
						if( FunctionConfig.isEnablePreviewWallpaperByAdapter() )
						{
							if( mLocalViewPagerAdapter != null )
							{
								mLocalViewPagerAdapter.notifyDataSetChanged();
							}
							mViewPager.setCurrentItem( position , false );
						}
						mGallery.setSelection( position );
					}
				} );
			}
		} ).start();
	}
	
	private void initPreviewButton()
	{
		findViewById( R.id.btnpreview ).setVisibility( View.GONE );
		findViewById( R.id.setdesktopwallpaper ).setVisibility( View.GONE );
		//findViewById( R.id.set_desk_and_lock_wallpaper ).setVisibility( View.GONE );
		//调整按钮的显示布局
		{
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT );
			lp.setMargins( 50 , 0 , 50 , 0 );
			findViewById( R.id.setlockwallpaper ).setLayoutParams( lp );
		}
	}
	
	private void findLockWallpapers()
	{
		//<c_0001306> liuhailin@2014-10-13 modify begin
		if( null != FunctionConfig.getWallpapers_from_other_apk() )
		{
			try
			{
				Context remountContext = mThemeContext.createPackageContext( FunctionConfig.getWallpapers_from_other_apk() , Context.CONTEXT_IGNORE_SECURITY );
				Resources res = remountContext.getResources();
				int count = 30;//Integer.parseInt( FunctionConfig.getOtherApkWallpapersNum() );
				int temp_index = 0;
				for( int i = 1 ; i <= count ; i++ )
				{
					try
					{
						int drawable = res.getIdentifier( "unlock_wallpaper_" + ( i < 10 ? "0" + i : i ) + "_small" , "drawable" , FunctionConfig.getWallpapers_from_other_apk() );
						//int drawablewallpaper = res.getIdentifier( "wallpaper_" + ( i < 10 ? "0" + i : i ) , "drawable" , wallpapers_from_other_apk );
						if( drawable == 0 )
						{
							drawable = res.getIdentifier( "unlock_wallpaper_" + ( i ) + "_thumbnail" , "drawable" , FunctionConfig.getWallpapers_from_other_apk() );
							if( drawable == 0 )
							{
								//<> liuhailin@2014-10-10 modify begin
								//break;
								continue;
								//<> liuhailin@2014-10-10 modify end
							}
							else
							{
								mThumbs.add( "unlock_wallpaper_" + ( i ) );
							}
						}
						else
						{
							mThumbs.add( "unlock_wallpaper_" + ( i < 10 ? "0" + i : i ) );
						}
						localBmp.add( res.getDrawable( drawable ) );
						if( FunctionConfig.isEnablePreviewWallpaperByAdapter() )
						{
							wallpaperMap.put( String.valueOf( temp_index ) , defaultDrawable );
							temp_index++;
						}
					}
					catch( IllegalArgumentException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if( FunctionConfig.isEnablePreviewWallpaperByAdapter() )
				{
					int drawable = res.getIdentifier( "unlock_wallpaper_" + ( position + 1 < 10 ? "0" + position + 1 : position + 1 ) , "drawable" , FunctionConfig.getWallpapers_from_other_apk() );
					if( drawable == 0 )
					{
						drawable = res.getIdentifier( "unlock_wallpaper_" + ( position + 1 ) , "drawable" , FunctionConfig.getWallpapers_from_other_apk() );
						if( drawable == 0 )
						{
							wallpaperMap.put( String.valueOf( position ) , defaultDrawable );
						}
						else
						{
							wallpaperMap.put( String.valueOf( position ) , res.getDrawable( drawable ) );
						}
					}
					else
					{
						wallpaperMap.put( String.valueOf( position ) , res.getDrawable( drawable ) );
					}
				}
			}
			catch( NameNotFoundException e )
			{
				Log.e( "tabwallpaper" , "createPackageContext exception: " + e );
			}
		}
		else
		{
			customLockWallpaperPath = FunctionConfig.getCustomLockWallpaperPath();
			String[] wallpapers = null;
			if( customLockWallpaperPath != null )
			{
				File dir = new File( customLockWallpaperPath );
				if( dir.exists() && dir.isDirectory() )
				{
					useCustomLockWallpaper = true;
					wallpapers = dir.list();
				}
			}
			else
			{
				AssetManager assManager = getResources().getAssets();
				try
				{
					wallpapers = assManager.list( wallpaperPath );
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if( null != wallpapers )
			{
				Tools.getThumblist( wallpapers , mThumbs );
			}
			Collections.sort( mThumbs , new ByStringValue() );
			for( int i = 0 ; i < mThumbs.size() ; i++ )
			{
				//第二个参数1表示加载小的缩略图
				getCustomWallpaperDrawable( mThumbs.get( i ) , 0 , i );
				if( FunctionConfig.isEnablePreviewWallpaperByAdapter() )
				{
					wallpaperMap.put( String.valueOf( i ) , defaultDrawable );
				}
			}
			if( FunctionConfig.isEnablePreviewWallpaperByAdapter() )
			{
				if( mThumbs != null && mThumbs.size() > 0 )
				{
					getCustomWallpaperDrawable( mThumbs.get( position ).replace( "_small" , "" ) , 1 , position );
				}
			}
		}
		//<c_0001306> liuhailin@2014-10-13 modify end
	}
	
	//在显示第一张预览图的同时继续加载其他的预览图,解决进入本地内置壁纸Loading时间长的问题
	private void LoadLocalWallpaperThread()
	{
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				if( FunctionConfig.getWallpapers_from_other_apk() != null )
				{
					try
					{
						Context remountContext = mThemeContext.createPackageContext( FunctionConfig.getWallpapers_from_other_apk() , Context.CONTEXT_IGNORE_SECURITY );
						Resources res = remountContext.getResources();
						int count = 30;//Integer.parseInt( FunctionConfig.getOtherApkWallpapersNum() );
						int temp_index = 0;
						for( int i = 1 ; i <= count ; i++ )
						{
							try
							{
								int drawable = res.getIdentifier( "unlock_wallpaper_" + ( i < 10 ? "0" + i : i ) , "drawable" , FunctionConfig.getWallpapers_from_other_apk() );
								if( drawable == 0 )
								{
									drawable = res.getIdentifier( "unlock_wallpaper_" + ( i ) , "drawable" , FunctionConfig.getWallpapers_from_other_apk() );
									if( drawable == 0 )
									{
										//<> liuhailin@2014-10-10 modify begin
										//break;
										continue;
										//<> liuhailin@2014-10-10 modify end
									}
								}
								Drawable mDrawable = res.getDrawable( drawable );
								wallpaperMap.put( String.valueOf( temp_index ) , mDrawable );
								temp_index++;
							}
							catch( IllegalArgumentException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					catch( NameNotFoundException e )
					{
						Log.e( "tabwallpaper" , "createPackageContext exception: " + e );
					}
				}
				else
				{
					for( int i = 0 ; i < mThumbs.size() ; i++ )
					{
						//第二个参数1表示加载大的预览图
						getCustomWallpaperDrawable( mThumbs.get( i ).replace( "_small" , "" ) , 1 , i );
					}
				}
				runOnUiThread( new Runnable() {
					
					public void run()
					{
						isLoadComplete = true;
						if( mLocalViewPagerAdapter != null )
						{
							mLocalViewPagerAdapter.notifyDataSetChanged();
						}
					}
				} );
			}
		} ).start();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if( mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED )
		{
			mLoader.cancel( true );
			mLoader = null;
		}
		ActivityManager.popupActivity( this );
	}
	
	public void onItemSelected(
			AdapterView parent ,
			View v ,
			int position ,
			long id )
	{
		if( mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED )
		{
			mLoader.cancel();
		}
		mLoader = (WallpaperLoader)new WallpaperLoader().execute( position );
		if( FunctionConfig.isEnablePreviewWallpaperByAdapter() )
		{
			mViewPager.setCurrentItem( position , false );
		}
	}
	
	public void onNothingSelected(
			AdapterView parent )
	{
	}
	
	//<> liuhailin@2014-10-13 modify begin
	public class LocalViewPagerAdapter extends PagerAdapter
	{
		
		private List<View> mListViews;
		private LayoutInflater mLayoutInflater;
		
		LocalViewPagerAdapter(
				LockWallpaperPreview context )
		{
			mLayoutInflater = context.getLayoutInflater();
		}
		
		@Override
		public void destroyItem(
				ViewGroup container ,
				int position ,
				Object object )
		{
			//container.removeView( mListViews.get( position ) );//删除页卡  
			( (ViewPager)container ).removeView( (View)object );
		}
		
		@Override
		public Object instantiateItem(
				ViewGroup container ,
				int position )
		{ //这个方法用来实例化页卡         
			String imagePath = null;
			View imageLayout = mLayoutInflater.inflate( R.layout.wallpaper_preview_pager_item , container , false );
			ImageView imageView = (ImageView)imageLayout.findViewById( R.id.image );
			//container.addView( mListViews.get( position ) , 0 );//添加页卡
			if( position < mThumbs.size() )
			{
				imageView.setImageDrawable( wallpaperMap.get( String.valueOf( position ) ) );//( localWallpaperBmp.get( position ) );//localBmp.get( position ) );
			}
			( (ViewPager)container ).addView( imageLayout , 0 );
			return imageLayout;
		}
		
		@Override
		public int getCount()
		{
			return localBmp.size();//返回页卡的数量  
		}
		
		@Override
		public int getItemPosition(
				Object object )
		{
			// TODO Auto-generated method stub
			return POSITION_NONE;
		}
		
		@Override
		public boolean isViewFromObject(
				View arg0 ,
				Object arg1 )
		{
			return arg0 == arg1;
		}
	}
	
	//<> liuhailin@2014-10-13 modify end
	private class ImageAdapter extends BaseAdapter
	{
		
		private LayoutInflater mLayoutInflater;
		
		ImageAdapter(
				LockWallpaperPreview context )
		{
			mLayoutInflater = context.getLayoutInflater();
		}
		
		public int getCount()
		{
			return mThumbs.size();
		}
		
		public Object getItem(
				int position )
		{
			return position;
		}
		
		public long getItemId(
				int position )
		{
			return position;
		}
		
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			ImageView image = null;
			if( convertView == null )
			{
				image = (ImageView)mLayoutInflater.inflate( R.layout.wallpaper_preview_item , parent , false );
			}
			else
			{
				image = (ImageView)convertView;
			}
			if( position < mThumbs.size() )
			{
				image.setImageDrawable( localBmp.get( position ) );
			}
			Drawable thumbDrawable = image.getDrawable();
			if( thumbDrawable != null )
			{
				thumbDrawable.setDither( true );
			}
			return image;
		}
	}
	
	public void onClick(
			View v )
	{
		if( v.getId() == R.id.btnReturn )
		{
			finish();
		}
		else if( v.getId() == R.id.setlockwallpaper )
		{
			final ProgressDialog dialog = new ProgressDialog( this );
			dialog.setMessage( getString( R.string.changeLockWallpaper ) );
			dialog.setCancelable( false );
			dialog.show();
			new Thread( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					while( mLoader != null && mLoader.getStatus() != WallpaperLoader.Status.FINISHED )
					{
						try
						{
							Thread.sleep( 20 );
						}
						catch( InterruptedException e )
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if( mLoader == null || mLoader.getStatus() == WallpaperLoader.Status.FINISHED )
					{
						// TODO Auto-generated method stub
						//					Bitmap newBitmap = PathTool.compressBitmap(
						//							( (BitmapDrawable)currentDrawable ).getBitmap() ,
						//							getResources().getDisplayMetrics().widthPixels ,
						//							getResources().getDisplayMetrics().heightPixels );
						boolean temp = false;
						if( currentDrawable != null )
						{
							if( FunctionConfig.getLockWallpaperPath() == null || FunctionConfig.getLockWallpaperPath().equals( "" ) )
							{
								temp = Tools.saveMyBitmap( "/data/data/com.iLoong.base.themebox/lockwallpapers" , ( (BitmapDrawable)currentDrawable ).getBitmap() );
							}
							else
							{
								temp = Tools.saveMyBitmap( FunctionConfig.getLockWallpaperPath() , ( (BitmapDrawable)currentDrawable ).getBitmap() );
							}
						}
						final boolean result = temp;
						setwallpaper.post( new Runnable() {
							
							@Override
							public void run()
							{
								// TODO Auto-generated method stub
								dialog.dismiss();
								//如果去掉关于第三方锁屏或者系统锁屏能否支持锁屏壁纸的判断没有开我在去判断是否支持锁屏壁纸
								if( preResult )
								{
									isLockSupportChangeWallpaper = true;
								}
								if( !isLockSupportChangeWallpaper )
								{
									Toast.makeText( LockWallpaperPreview.this , R.string.lockwallpaper_apply_tost , Toast.LENGTH_SHORT ).show();
									return;
								}
								if( result )
								{
									Toast.makeText( LockWallpaperPreview.this , R.string.toast_setwallpaper_success , Toast.LENGTH_SHORT ).show();
								}
								else
								{
									Toast.makeText( LockWallpaperPreview.this , R.string.apply_fail , Toast.LENGTH_SHORT ).show();
								}
							}
						} );
					}
				}
			} ).start();
		}
	}
	
	public Boolean setWallpaperByReflect(
			Bitmap mBitmap ,
			int target )
	{
		WallpaperManager mWallpaperManager = WallpaperManager.getInstance( this );
		Class<?> WallpaperManager = null;
		try
		{
			WallpaperManager = Class.forName( "android.app.WallpaperManager" );
		}
		catch( ClassNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v( "setWallpaperByReflect" , e.getMessage() );
		}
		if( WallpaperManager != null )
		{
			Method getSetBitmap = null;
			try
			{
				getSetBitmap = WallpaperManager.getMethod( "setBitmap" , new Class[]{ Bitmap.class , int.class } );
				//getSetBitmap = WallpaperManager.getMethod( "setBitmap" , new Class[]{ Bitmap.class } );
			}
			catch( SecurityException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v( "setWallpaperByReflect" , e.getMessage() );
			}
			catch( NoSuchMethodException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v( "setWallpaperByReflect" , e.getMessage() );
			}
			Object am = null;
			if( getSetBitmap != null )
			{
				try
				{
					am = getSetBitmap.invoke( mWallpaperManager , mBitmap , target );
					//am = getSetBitmap.invoke( mWallpaperManager , mBitmap );
					return true;
				}
				catch( IllegalArgumentException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.v( "setWallpaperByReflect" , e.getMessage() );
				}
				catch( IllegalAccessException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.v( "setWallpaperByReflect" , e.getMessage() );
				}
				catch( InvocationTargetException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.v( "setWallpaperByReflect" , e.getMessage() );
				}
			}
		}
		return false;
	}
	
	private Boolean setLockWallpaper()//展讯平台方法
	{
		WallpaperManager mWallpaperManager = WallpaperManager.getInstance( this );
		Class<?> WallpaperManager = null;
		try
		{
			WallpaperManager = Class.forName( "android.app.WallpaperManager" );
		}
		catch( ClassNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( WallpaperManager != null )
		{
			Method setBitmap = null;
			try
			{
				setBitmap = WallpaperManager.getMethod( "setBitmap" , Bitmap.class , int.class );
			}
			catch( SecurityException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch( NoSuchMethodException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( setBitmap != null )
			{
				try
				{
					setBitmap.invoke( mWallpaperManager , ( (BitmapDrawable)currentDrawable ).getBitmap() , 1 );
					return true;
				}
				catch( IllegalArgumentException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch( IllegalAccessException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch( InvocationTargetException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	//<> liuhailin@2014-10-13 modify begin
	private void getCustomWallpaperDrawable(
			String str ,
			int type ,
			int index )
	{
		InputStream is = null;
		if( useCustomLockWallpaper )
		{
			try
			{
				is = new FileInputStream( customLockWallpaperPath + "/" + str );
			}
			catch( FileNotFoundException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			AssetManager asset = mThemeContext.getResources().getAssets();
			try
			{
				is = asset.open( wallpaperPath + "/" + str );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
		if( is != null )
		{
			Drawable mDrawable = Drawable.createFromStream( is , "" );
			if( type == 0 )
			{
				localBmp.add( mDrawable );//加载小缩略图
			}
			else
			{
				wallpaperMap.put( String.valueOf( index ) , mDrawable );
			}
			try
			{
				is.close();
			}
			catch( IOException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//<> liuhailin@2014-10-13 modify end
	class WallpaperLoader extends AsyncTask<Integer , Void , Drawable>
	{
		
		BitmapFactory.Options mOptions;
		
		WallpaperLoader()
		{
			mOptions = new BitmapFactory.Options();
			mOptions.inDither = false;
		}
		
		protected Drawable doInBackground(
				Integer ... params )
		{
			if( isCancelled() )
				return null;
			try
			{
				InputStream is = null;
				if( params[0] < mThumbs.size() )
				{
					//<c_0001306> liuhailin@2014-10-13 modify begin
					if( null != FunctionConfig.getWallpapers_from_other_apk() )
					{
						try
						{
							Context remountContext = mThemeContext.createPackageContext( FunctionConfig.getWallpapers_from_other_apk() , Context.CONTEXT_IGNORE_SECURITY );
							Resources res = remountContext.getResources();
							try
							{
								//currentOtherApkWallpaperResName = mThumbs.get( params[0] );
								int drawable = res.getIdentifier( mThumbs.get( params[0] ) , "drawable" , FunctionConfig.getWallpapers_from_other_apk() );
								Drawable mDrawable = res.getDrawable( drawable );
								currentDrawable = mDrawable;
								if( FunctionConfig.isEnablePreviewWallpaperByAdapter() )
								{
									if( wallpaperMap.get( String.valueOf( params[0] ) ).equals( defaultDrawable ) )
									{
										wallpaperMap.put( String.valueOf( params[0] ) , mDrawable );
									}
								}
								return currentDrawable;
							}
							catch( Exception e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						catch( Exception e1 )
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					else
					{
						if( useCustomLockWallpaper )
						{
							try
							{
								is = new FileInputStream( customLockWallpaperPath + "/" + mThumbs.get( params[0] ).replace( "_small" , "" ) );
								//newis = new FileInputStream( customLockWallpaperPath + "/" + mThumbs.get( params[0] ).replace( "_small" , "" ) );
								Drawable mDrawable = Drawable.createFromStream( is , "" );
								currentDrawable = mDrawable;
								if( FunctionConfig.isEnablePreviewWallpaperByAdapter() )
								{
									if( wallpaperMap.get( String.valueOf( params[0] ) ).equals( defaultDrawable ) )
									{
										wallpaperMap.put( String.valueOf( params[0] ) , mDrawable );
									}
								}
							}
							catch( FileNotFoundException e )
							{
								e.printStackTrace();
								return null;
							}
						}
						else
						{
							AssetManager asset = mThemeContext.getResources().getAssets();
							try
							{
								is = asset.open( wallpaperPath + "/" + mThumbs.get( params[0] ).replace( "_small" , "" ) );
								//newis = asset.open( wallpaperPath + "/" + mThumbs.get( params[0] ).replace( "_small" , "" ) );
								Drawable mDrawable = Drawable.createFromStream( is , "" );
								currentDrawable = mDrawable;
								if( FunctionConfig.isEnablePreviewWallpaperByAdapter() )
								{
									if( wallpaperMap.get( String.valueOf( params[0] ) ).equals( defaultDrawable ) )
									{
										wallpaperMap.put( String.valueOf( params[0] ) , mDrawable );
									}
								}
							}
							catch( IOException e )
							{
								e.printStackTrace();
								return null;
							}
						}
					}
					//<c_0001306> liuhailin@2014-10-13 modify end
				}
				if( is == null )
				{
					return null;
				}
				try
				{
					if( is != null )
					{
						is.close();
					}
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
			return currentDrawable;
		}
		
		@Override
		protected void onPostExecute(
				Drawable isSuccess )
		{
			if( FunctionConfig.isEnablePreviewWallpaperByAdapter() && !isLoadComplete )
			{
				LoadLocalWallpaperThread();
				if( mLocalViewPagerAdapter != null )
				{
					mLocalViewPagerAdapter.notifyDataSetChanged();
				}
			}
			else if( !FunctionConfig.isEnablePreviewWallpaperByAdapter() )
			{
				mImageView.setImageDrawable( isSuccess );
			}
		}
		
		void cancel()
		{
			mOptions.requestCancelDecode();
			super.cancel( true );
		}
	}
	
	class ByStringValue implements Comparator<String>
	{
		
		@Override
		public int compare(
				String lhs ,
				String rhs )
		{
			// TODO Auto-generated method stub
			if( lhs.compareTo( rhs ) > 0 )
			{
				return 1;
			}
			else if( lhs.compareTo( rhs ) < 0 )
			{
				return -1;
			}
			return 0;
		}
	}
}
