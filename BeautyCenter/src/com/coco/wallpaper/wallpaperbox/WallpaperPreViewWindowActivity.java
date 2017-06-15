package com.coco.wallpaper.wallpaperbox;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.coco.lock2.lockbox.LockInformation;
import com.coco.lock2.lockbox.util.ContentConfig;
import com.coco.lock2.lockbox.util.LockManager;
import com.coco.theme.themebox.service.ThemesDB;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Tools;
import com.iLoong.base.themebox.R;


public class WallpaperPreViewWindowActivity extends Activity implements View.OnClickListener
{
	
	private ImageView mImageView;
	private ImageView mContentImageView;
	private ImageView mGuideView;
	private ViewPager mViewPager;
	private Button btnSetWallpaper;
	PreviewViewPagerAdapter mPreviewViewPagerAdapter;
	private Bitmap mBitmap;
	//private Bitmap newBitmap;
	private Bitmap mContentLockBitmap;
	private Bitmap mContentLauncherBitmap;
	private Boolean isSetDeskTopWallpaper = true;
	private String imagePath = null;
	private String wallpapers_from_other_apk = null;
	private String current_other_apk_res_name = "";
	private int position = 0;
	private String wallpaperPath = "launcher/wallpapers";
	private String mSelectString = null;
	private Boolean isWallpaperInformation = false;
	private ComponentName currentLock;
	private static Boolean isLockSupportChangeWallpaper = false;
	private static Boolean isEnableLauncherTakeScreenShot = false;
	private int count = -1;
	private Context mContext;
	
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		setContentView( R.layout.wallpaper_preview_window );
		mContext = this;
		isEnableLauncherTakeScreenShot = getIntent().getBooleanExtra( "isenablelaunchertakescreenshot" , false );
		imagePath = getIntent().getStringExtra( "imagePath" );
		wallpapers_from_other_apk = getIntent().getStringExtra( "wallpapers_from_other_apk" );
		current_other_apk_res_name = getIntent().getStringExtra( "current_other_apk_res_name" );
		position = getIntent().getIntExtra( "position" , 0 );
		mSelectString = getIntent().getStringExtra( "wallpaper" );
		isWallpaperInformation = getIntent().getBooleanExtra( "isWallpaperInformation" , false );
		//getAndShowSerialzableObeject();
		RecycledBitmap();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( this );
		count = preferences.getInt( "wallpaperPreviewCount" , 0 );
		mGuideView = (ImageView)findViewById( R.id.guideView );
		mViewPager = (ViewPager)findViewById( R.id.previewWindowPager );
		mViewPager.setVisibility( View.VISIBLE );
		btnSetWallpaper = (Button)findViewById( R.id.setwallpaper );
		btnSetWallpaper.setOnClickListener( this );
		btnSetWallpaper.setVisibility( View.GONE );
		//获取桌面截图
		//mContentLauncherBitmap = Tools.getPurgeableBitmap( getLauncherBgPath() , -1 , -1 );
		mContentLauncherBitmap = Tools.getPurgeableBitmap( "/data/data/" + ThemesDB.LAUNCHER_PACKAGENAME + "/files/launcherthumb.png" , -1 , -1 );
		//mContentLauncherBitmapTemp = Bitmap.createBitmap( mContentLauncherBitmap , 0 , 80 , mContentLauncherBitmap.getWidth() , mContentLauncherBitmap.getHeight() - 80 );
		isLockSupportChangeWallpaper = getCurrentLockInfoDZ();
		if( ( isLockSupportChangeWallpaper && FunctionConfig.isEnableShowApplyLockWallpaper() ) && isEnableLauncherTakeScreenShot && count == 0 )
		{
			mGuideView.setVisibility( View.VISIBLE );
			SharedPreferences.Editor mEditor = preferences.edit();
			mEditor.putInt( "wallpaperPreviewCount" , 1 );
			mEditor.commit();
		}
		else
		{
			mGuideView.setVisibility( View.GONE );
		}
		mPreviewViewPagerAdapter = new PreviewViewPagerAdapter( this );
		mViewPager.setAdapter( mPreviewViewPagerAdapter );
		mViewPager.setOnPageChangeListener( new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(
					int position )
			{
				// TODO Auto-generated method stub
				if( position == 0 && isEnableLauncherTakeScreenShot )
				{
					btnSetWallpaper.setText( R.string.apply_to_desktop );
					isSetDeskTopWallpaper = true;
				}
				else
				{
					btnSetWallpaper.setText( R.string.apply_to_lockwallpaper );
					isSetDeskTopWallpaper = false;
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
		if( ( isLockSupportChangeWallpaper && FunctionConfig.isEnableShowApplyLockWallpaper() ) && !isEnableLauncherTakeScreenShot )
		{
			btnSetWallpaper.setText( R.string.apply_to_lockwallpaper );
			isSetDeskTopWallpaper = false;
		}
		else
		{
			btnSetWallpaper.setText( R.string.apply_to_desktop );
			isSetDeskTopWallpaper = true;
		}
	}
	
	private String getLauncherBgPath()
	{
		boolean hasSD = Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED );
		if( hasSD )
		{
			File sdCardDir = Environment.getExternalStorageDirectory();
			// SDCard目录：/mnt/sdcard  
			String fileName = "/launcherthumb.png";
			String sdcardPath = sdCardDir.getAbsolutePath();
			String saveFilePaht = sdcardPath + File.separator + fileName;
			return saveFilePaht;
		}
		return "";
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
			if( destContent.getLockStyleValue().equals( String.valueOf( Settings.System.getInt( mContext.getContentResolver() , "system.settings.lockstyle" , -1 ) ) ) )
			{
				AssetManager asset = dstContext.getResources().getAssets();
				InputStream is = null;
				try
				{
					is = asset.open( "background.png" );
					mContentLockBitmap = Tools.getPurgeableBitmap( is , -1 , -1 );
					if( mContentLockBitmap != null )
					{
						if( is != null )
						{
							is.close();
						}
					}
				}
				catch( Exception e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
		if( currentLock.getPackageName().equals( "com.third.test" ) )
		{
			currentLock = mgr.queryCurrentThirdPartyLock();
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
			AssetManager asset = dstContext.getResources().getAssets();
			InputStream is = null;
			try
			{
				is = asset.open( "background.png" );
				mContentLockBitmap = Tools.getPurgeableBitmap( is , -1 , -1 );
				if( mContentLockBitmap != null )
				{
					if( is != null )
					{
						is.close();
					}
				}
			}
			catch( Exception e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
	}
	
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		RecycledBitmap();
		super.onDestroy();
	}
	
	private void RecycledBitmap()
	{
		if( mBitmap != null && !mBitmap.isRecycled() )
		{
			mBitmap.recycle();
			mBitmap = null;
		}
		//		if( newBitmap != null && !newBitmap.isRecycled() )
		//		{
		//			newBitmap.recycle();
		//			newBitmap = null;
		//		}
		if( mContentLockBitmap != null && !mContentLockBitmap.isRecycled() )
		{
			mContentLockBitmap.recycle();
			mContentLockBitmap = null;
		}
		if( mContentLauncherBitmap != null && !mContentLauncherBitmap.isRecycled() )
		{
			mContentLauncherBitmap.recycle();
			mContentLauncherBitmap = null;
		}
	}
	
	public class PreviewViewPagerAdapter extends PagerAdapter
	{
		
		private LayoutInflater mLayoutInflater;
		
		PreviewViewPagerAdapter(
				Context context )
		{
			mLayoutInflater = getLayoutInflater();
		}
		
		@Override
		public Object instantiateItem(
				ViewGroup container ,
				int position )
		{ //这个方法用来实例化页卡         
			View imageLayout = mLayoutInflater.inflate( R.layout.preview_window_item , container , false );
			mImageView = (ImageView)imageLayout.findViewById( R.id.previewImage );
			mContentImageView = (ImageView)imageLayout.findViewById( R.id.previewContent );
			mBitmap = getImageBitmap();
			if( mBitmap == null || mBitmap.isRecycled() )
			{
				//mImageView.setImage;//这里需要修改
			}
			else
			{
				mImageView.setImageBitmap( mBitmap );
			}
			if( position == 0 && isEnableLauncherTakeScreenShot )
			{
				//				if( mContentLauncherBitmap != null )
				//				{
				//					newBitmap = Bitmap.createBitmap( mContentLauncherBitmap , 0 , 80 , mContentLauncherBitmap.getWidth() , mContentLauncherBitmap.getHeight() - 80 );
				//				}
				mContentImageView.setImageBitmap( mContentLauncherBitmap );
			}
			else
			{
				mContentImageView.setImageBitmap( mContentLockBitmap );
			}
			( (ViewPager)container ).addView( imageLayout , 0 );
			return imageLayout;
		}
		
		@Override
		public int getCount()
		{
			// TODO Auto-generated method stub
			if( ( isLockSupportChangeWallpaper && FunctionConfig.isEnableShowApplyLockWallpaper() ) && isEnableLauncherTakeScreenShot )
			{
				return 2;
			}
			return 1;
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
			// TODO Auto-generated method stub
			return arg0 == arg1;//官方提示这样写  
		}
	}
	
	private Bitmap getImageBitmap()
	{
		Log.v( "WallpaperPreViewWindowActivity" , "imagePath == " + imagePath );
		if( wallpapers_from_other_apk != null && current_other_apk_res_name != null && imagePath == null )
		{
			try
			{
				Context remountContext = mContext.createPackageContext( wallpapers_from_other_apk , Context.CONTEXT_IGNORE_SECURITY );
				Resources res = remountContext.getResources();
				try
				{
					int drawable = res.getIdentifier( current_other_apk_res_name , "drawable" , wallpapers_from_other_apk );
					return Tools.drawableToBitmap( res.getDrawable( drawable ) );
				}
				catch( IllegalArgumentException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch( NameNotFoundException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
		else if( imagePath.contains( wallpaperPath ) )
		{
			InputStream is = null;
			Context remoteContext;
			try
			{
				remoteContext = WallpaperPreViewWindowActivity.this.createPackageContext( ThemesDB.LAUNCHER_PACKAGENAME , Context.CONTEXT_IGNORE_SECURITY );
				AssetManager asset = remoteContext.getResources().getAssets();
				try
				{
					is = asset.open( imagePath );
					return Tools.getPurgeableBitmap( is , -1 , -1 );
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
			catch( NameNotFoundException e1 )
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}
		}
		else
		{
			return Tools.getPurgeableBitmap( imagePath , -1 , -1 );
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(
			MotionEvent ev )
	{
		mGuideView.setVisibility( View.GONE );
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent( ev );
	}
	
	@Override
	public void onClick(
			View v )
	{
		// TODO Auto-generated method stub
		if( v.getId() == R.id.setwallpaper )
		{
			if( isSetDeskTopWallpaper )
			{
				final ProgressDialog dialog = new ProgressDialog( this );
				dialog.setMessage( getString( R.string.changingWallpaper ) );
				dialog.setCancelable( false );
				dialog.show();
				{
					Intent it = new Intent( "com.coco.wallpaper.update" );
					it.putExtra( "isDesktopWall" , true );
					it.putExtra( "wallpaper" , mSelectString );
					sendBroadcast( it );
					new Thread( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							try
							{
								Thread.sleep( 200 );
							}
							catch( InterruptedException e )
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if( mBitmap != null && !mBitmap.isRecycled() )
							{
								selsectWallpaper( mBitmap );
							}
							btnSetWallpaper.post( new Runnable() {
								
								@Override
								public void run()
								{
									// TODO Auto-generated method stub
									dialog.dismiss();
									// finish();
									if( isWallpaperInformation )
									{
										sendBroadcast( new Intent( "com.cooee.scene.wallpaper.change" ) );
									}
									Toast.makeText( WallpaperPreViewWindowActivity.this , R.string.toast_setwallpaper_success , Toast.LENGTH_SHORT ).show();
								}
							} );
						}
					} ).start();
				}
			}
			else
			{
				final ProgressDialog dialog = new ProgressDialog( this );
				dialog.setMessage( getString( R.string.changingWallpaper ) );
				dialog.setCancelable( false );
				dialog.show();
				new Thread( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						//Bitmap newBitmap = PathTool.compressBitmap( mBitmap , getResources().getDisplayMetrics().widthPixels , getResources().getDisplayMetrics().heightPixels );
						boolean temp = false;
						if( FunctionConfig.getLockWallpaperPath() == null || FunctionConfig.getLockWallpaperPath().equals( "" ) )
						{
							temp = Tools.saveMyBitmap( "/data/data/com.iLoong.base.themebox/lockwallpapers" , mBitmap );
						}
						else
						{
							temp = Tools.saveMyBitmap( FunctionConfig.getLockWallpaperPath() , mBitmap );
						}
						final boolean result = temp;
						btnSetWallpaper.post( new Runnable() {
							
							@Override
							public void run()
							{
								// TODO Auto-generated method stub
								dialog.dismiss();
								if( result )
								{
									Toast.makeText( WallpaperPreViewWindowActivity.this , R.string.toast_setwallpaper_success , Toast.LENGTH_SHORT ).show();
								}
								else
								{
									Toast.makeText( WallpaperPreViewWindowActivity.this , R.string.apply_fail , Toast.LENGTH_SHORT ).show();
								}
							}
						} );
					}
				} ).start();
			}
		}
		if( v.getId() == R.id.guideView )
		{
			mGuideView.setVisibility( View.GONE );
		}
	}
	
	public void selsectWallpaper(
			Bitmap bmp )
	{
		WallpaperManager wpm = (WallpaperManager)this.getSystemService( Context.WALLPAPER_SERVICE );
		try
		{
			if( bmp != null )
			{
				Log.v( "wallpaperinfo" , "bmp = " + bmp.getWidth() + " , " + bmp.getHeight() );
				wpm.setBitmap( bmp );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
