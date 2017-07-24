// xiatian add whole file //德盛伟业需求：添加uni3上的壁纸选择界面，并在系统的壁纸设置界面中显示。
package com.iLoong.launcher.desktop;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.wallpaperManager.WallpaperManagerBase;
import com.iLoong.launcher.desktop.WallpaperLMLockerSettingListDialog.OnWallpaperSettingItemClickListener;


public class WallpaperChooser extends Activity implements AdapterView.OnItemSelectedListener , OnClickListener
{
	
	private static final String TAG = "Launcher.WallpaperChooser";//
	private static final String wallpaperPath = "launcher/wallpapers";
	private static String customWallpaperPath;
	private boolean useCustomWallpaper = false;
	private Gallery mGallery;
	private ImageView mImageView;
	private boolean mIsWallpaperSet;
	private Bitmap mBitmap;
	private ArrayList<String> mThumbs;
	private ArrayList<String> mImages;
	private WallpaperLoader mLoader;
	private ArrayList<String> downloadThumbs;
	private ArrayList<String> downloadImages;
	private String downloadWallpaperPath = StringUtils.concat( Environment.getExternalStorageDirectory().getAbsolutePath() , "/Coco/Wallpaper/App" );
	private BroadcastReceiver mReceiver = null;//zhujieping add //添加配置项“finish_activity_when_set_uni3_wallpaper_successfully”，当com.iLoong.launcher.desktop.WallpaperChooser配置enable为true时，true为关闭activity，false为不关闭activity，弹出设置成功的toast。	
	
	@Override
	public void onCreate(
			Bundle icicle )
	{
		super.onCreate( icicle );
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		findWallpapers();
		setContentView( R.layout.wallpaper_chooser );
		mGallery = (Gallery)findViewById( R.id.gallery );
		mGallery.setAdapter( new ImageAdapter( this ) );
		mGallery.setOnItemSelectedListener( this );
		mGallery.setCallbackDuringFling( false );
		//chenliang add start	//添加配置项“switch_enable_customer_lm_set_lockwallpaper”，增加锁屏壁纸的功能（进入uni3桌面壁纸，点击设置，弹出“设置桌面”、“设置锁屏”和“同时设置”选项）。true为显示，false不显示。默认为false。备注：该功能需要系统底层支持。
		View mSettingButton = null;
		if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOMER_LM_SET_LOCKWALLPAPER )
		{
			mSettingButton = findViewById( R.id.wallpaper_setting );
		}
		else
		{
			mSettingButton = findViewById( R.id.set );
		}
		mSettingButton.setVisibility( View.VISIBLE );
		//chenliang add end
		mSettingButton.setOnClickListener( this );
		mImageView = (ImageView)findViewById( R.id.wallpaper );
		//zhujieping add start ////添加配置项“finish_activity_when_set_uni3_wallpaper_successfully”，当com.iLoong.launcher.desktop.WallpaperChooser配置enable为true时，true为关闭activity，false为不关闭activity，弹出设置成功的toast。	
		if( !LauncherDefaultConfig.FINISH_ACTIVITY_WHEN_SET_UNI3_WALLPAPER_SUCCESSFULLY )
		{
			initReceiver();
		}
		//zhujieping add end
	}
	
	//zhujieping add start //添加配置项“finish_activity_when_set_uni3_wallpaper_successfully”，当com.iLoong.launcher.desktop.WallpaperChooser配置enable为true时，true为关闭activity，false为不关闭activity，弹出设置成功的toast。	
	private void initReceiver()
	{
		if( mReceiver == null )
		{
			mReceiver = new BroadcastReceiver() {
				
				@Override
				public void onReceive(
						Context context ,
						Intent intent )
				{
					// TODO Auto-generated method stub
					if( mImageView != null )
					{
						mImageView.postDelayed( new Runnable() {//延时，防止频繁点击
							
							public void run()
							{
								mIsWallpaperSet = false;
							}
						} , 1500 );
					}
					else
					{
						mIsWallpaperSet = false;
					}
				}
			};
			IntentFilter filter = new IntentFilter();
			filter.addAction( Intent.ACTION_WALLPAPER_CHANGED );
			registerReceiver( mReceiver , filter );
		}
	}
	//zhujieping add end
	
	private void findWallpapers()
	{
		mThumbs = new ArrayList<String>( 24 );
		mImages = new ArrayList<String>( 24 );
		ArrayList<String> mTemp = new ArrayList<String>( 24 );
		ArrayList<String> mFound = new ArrayList<String>( 24 );
		final Resources resources = getResources();
		// Context.getPackageName() may return the "original" package name,
		// com.coesns.launcher2; Resources needs the real package name,
		// com.coesns.launcher2. So we ask Resources for what it thinks the
		// package name should be.
		customWallpaperPath = LauncherDefaultConfig.CONFIG_CUSTOM_WALLPAPERS_PATH;//xiatian add	//德盛伟业需求：本地化默认壁纸路径。客户可配置的桌面壁纸路径，如"/system/wallpapers"，再在该路径下放置客户的壁纸图片。配置为空则显示"\assets\launcher\wallpapers"中的壁纸。
		File dir = null;
		if( TextUtils.isEmpty( customWallpaperPath ) == false )
		{
			dir = new File( customWallpaperPath );
			if( dir.exists() && dir.isDirectory() )
			{
				useCustomWallpaper = true;
			}
		}
		AssetManager assManager = resources.getAssets();
		String[] wallpapers = null;
		try
		{
			if( useCustomWallpaper && dir != null )
			{
				wallpapers = dir.list();
			}
			else
				wallpapers = assManager.list( wallpaperPath );
			for( String name : wallpapers )
			{
				//Log.v( "wallpaper" , name );
				if( !name.contains( "_small" ) )
				{
					mImages.add( name );
				}
				else
				{
					mTemp.add( name );
				}
			}
			for( String name : mImages )
			{
				for( String nameTmp : mTemp )
				{
					if( name.equals( nameTmp.replace( "_small" , "" ) ) )
					{
						mThumbs.add( nameTmp );
						mFound.add( name );
						break;
					}
				}
			}
			mImages.clear();
			mImages.addAll( mFound );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//        final String packageName = resources.getResourcePackageName(R.array.wallpapers);
		//
		//        addWallpapers(resources, packageName, R.array.wallpapers);
		//        addWallpapers(resources, packageName, R.array.extra_wallpapers);
		downloadThumbs = new ArrayList<String>( 24 );
		downloadImages = new ArrayList<String>( 24 );
		mTemp.clear();
		mFound.clear();
		wallpapers = null;
		// Context.getPackageName() may return the "original" package name,
		// com.coesns.launcher2; Resources needs the real package name,
		// com.coesns.launcher2. So we ask Resources for what it thinks the
		// package name should be.
		File file = new File( downloadWallpaperPath );
		if( file.exists() && file.isDirectory() )
		{
			wallpapers = file.list();
			if( wallpapers != null )
			{
				for( String name : wallpapers )
				{
					//Log.v( "wallpaper" , name );
					if( !name.contains( "_small" ) )
					{
						downloadImages.add( name );
					}
					else
					{
						mTemp.add( name );
					}
				}
				for( String name : downloadImages )
				{
					for( String nameTmp : mTemp )
					{
						if( name.equals( nameTmp.replace( "_small" , "" ) ) )
						{
							downloadThumbs.add( nameTmp );
							mFound.add( name );
							break;
						}
					}
				}
				downloadImages.clear();
				downloadImages.addAll( mFound );
				Collections.sort( downloadThumbs , new ByStringValue() );
				Collections.sort( downloadImages , new ByStringValue() );
			}
		}
	}
	
	class ByStringValue implements Comparator<String>
	{
		
		@Override
		public int compare(
				String lhs ,
				String rhs )
		{
			if( !( lhs instanceof String && rhs instanceof String ) )
			{
				return 0;
			}
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
	
	//    private void addWallpapers(Resources resources, String packageName, int list) {
	//        final String[] extras = resources.getStringArray(list);
	//        for (String extra : extras) {
	//            int res = resources.getIdentifier(extra, "drawable", packageName);
	//            if (res != 0) {
	//                final int thumbRes = resources.getIdentifier(extra + "_small",
	//                        "drawable", packageName);
	//
	//                if (thumbRes != 0) {
	//                    mThumbs.add(thumbRes);
	//                    mImages.add(res);
	//                    // Log.d(TAG, "addWallpapers: [" + packageName + "]: " + extra + " (" + res + ")");
	//                }
	//            }
	//        }
	//    }
	@Override
	protected void onResume()
	{
		super.onResume();
		mIsWallpaperSet = false;
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
		//zhujieping add start //添加配置项“finish_activity_when_set_uni3_wallpaper_successfully”，当com.iLoong.launcher.desktop.WallpaperChooser配置enable为true时，true为关闭activity，false为不关闭activity，弹出设置成功的toast。	
		if( mReceiver != null )
		{
			unregisterReceiver( mReceiver );
		}
		//zhujieping add end
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
	}
	
	/*
	 * When using touch if you tap an image it triggers both the onItemClick and
	 * the onTouchEvent causing the wallpaper to be set twice. Ensure we only
	 * set the wallpaper once.
	 */
	//	private void setWallpaperNewDim(
	//			InputStream newDimIs ,
	//			WallpaperManager wpm )
	//	{
	//		//Log.v( "jbc" , "fuckwallpaper WallpaperChooser setWallpaperNewDim()" );
	//		if( newDimIs == null )
	//		{
	//			return;
	//		}
	//		BitmapFactory.Options options = new BitmapFactory.Options();
	//		options.inJustDecodeBounds = true;
	//		BitmapFactory.decodeStream( newDimIs , null , options );
	//		DisplayMetrics displayMetrics = new DisplayMetrics();
	//		iLoongLauncher.getInstance().getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
	//		final int maxDim = Math.max( displayMetrics.widthPixels , displayMetrics.heightPixels );
	//		final int minDim = Math.min( displayMetrics.widthPixels , displayMetrics.heightPixels );
	//		int mWallpaperWidth = options.outWidth;
	//		int mWallpaperHeight = options.outHeight;
	//		//Log.v( "jbc" , "fuckwallpaper WallpaperChoose options.w=" + options.outWidth + " options.h=" + options.outHeight );
	//		float scale = 1;
	//		if( mWallpaperWidth < minDim )
	//		{
	//			scale = (float)minDim / (float)mWallpaperWidth;
	//		}
	//		if( mWallpaperHeight * scale < maxDim )
	//		{
	//			scale = (float)maxDim / (float)mWallpaperHeight;
	//		}
	//		//Log.v( "jbc" , "fuckwallpaper WallpaperChooser getDesired w=" + wpm.getDesiredMinimumWidth() + " h=" + wpm.getDesiredMinimumHeight() );
	//		//Log.v( "jbc" , "fuckwallpaper WallpaperChooser suggestDesired w=" + (int)( mWallpaperWidth * scale ) + " h=" + (int)( mWallpaperHeight * scale ) );
	//		wpm.suggestDesiredDimensions( (int)( mWallpaperWidth * scale ) , (int)( mWallpaperHeight * scale ) );
	//	}
	private void selectWallpaper(
			int position )
	{
		//Log.v( "jbc" , "fuckwallpaper WallpaperChooser selectWallpaper()" );
		if( mIsWallpaperSet )
		{
			return;
		}
		if( mImages.size() == 0 )
		{
			finish();
			return;
		}
		mIsWallpaperSet = true;
		InputStream is = null;
		InputStream newDimIs = null;
		String pathName = null;
		if( position < mThumbs.size() )
		{
			if( useCustomWallpaper )
			{
				try
				{
					pathName = StringUtils.concat( customWallpaperPath , File.separator , mImages.get( position ) );
					is = new FileInputStream( pathName );
					newDimIs = new FileInputStream( pathName );
				}
				catch( FileNotFoundException e )
				{
					e.printStackTrace();
				}
			}
			else
			{
				AssetManager asset = getResources().getAssets();
				try
				{
					pathName = StringUtils.concat( wallpaperPath , File.separator , mImages.get( position ) );
					is = asset.open( pathName );
					newDimIs = asset.open( pathName );
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			try
			{
				pathName = StringUtils.concat( downloadWallpaperPath , File.separator , downloadImages.get( position - mThumbs.size() ) );
				is = new FileInputStream( pathName );
				newDimIs = new FileInputStream( pathName );
			}
			catch( FileNotFoundException e )
			{
				e.printStackTrace();
			}
		}
		//		SharedPreferences prefs = iLoongLauncher.getInstance().getSharedPreferences( "launcher" , Activity.MODE_PRIVATE );
		if( WallpaperManagerBase.getInstance( this ).setWallpaperAndDimension( is , newDimIs ) )
		{
			//			prefs.edit().putLong( "apply_wallpaper_time" , System.currentTimeMillis() ).commit();
			//zhujieping add start //添加配置项“finish_activity_when_set_uni3_wallpaper_successfully”，当com.iLoong.launcher.desktop.WallpaperChooser配置enable为true时，true为关闭activity，false为不关闭activity，弹出设置成功的toast。	
			if( !LauncherDefaultConfig.FINISH_ACTIVITY_WHEN_SET_UNI3_WALLPAPER_SUCCESSFULLY )
			{
				Toast.makeText( this , R.string.set_wallpaper_success , Toast.LENGTH_SHORT ).show();
			}
			else
			//zhujieping add end
			{
				setResult( RESULT_OK );
				finish();
			}
		}
		//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( this );
		//		if( position < mThumbs.size() )
		//		{
		//			pref.edit().putString( "currentWallpaper" , mImages.get( position ) ).commit();
		//			PubProviderHelper.addOrUpdateValue( "wallpaper" , "currentWallpaper" , mImages.get( position ) );
		//		}
		//		else
		//		{
		//			pref.edit().putString( "currentWallpaper" , downloadImages.get( position - mThumbs.size() ) ).commit();
		//			PubProviderHelper.addOrUpdateValue( "wallpaper" , "currentWallpaper" , downloadImages.get( position - mThumbs.size() ) );
		//		}
		//		pref.edit().putBoolean( "cooeechange" , true ).commit();
		//		PubProviderHelper.addOrUpdateValue( "wallpaper" , "cooeechange" , "true" );
	}
	
	public void onNothingSelected(
			AdapterView parent )
	{
	}
	
	private class ImageAdapter extends BaseAdapter
	{
		
		private LayoutInflater mLayoutInflater;
		
		ImageAdapter(
				WallpaperChooser context )
		{
			mLayoutInflater = context.getLayoutInflater();
		}
		
		public int getCount()
		{
			return mThumbs.size() + downloadThumbs.size();
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
			ImageView image;
			if( convertView == null )
			{
				image = (ImageView)mLayoutInflater.inflate( R.layout.wallpaper_item , parent , false );
			}
			else
			{
				image = (ImageView)convertView;
			}
			//            int thumbRes = mThumbs.get(position);
			//            image.setImageResource(thumbRes);
			InputStream is = null;
			if( position < mThumbs.size() )
			{
				if( useCustomWallpaper )
				{
					try
					{
						is = new FileInputStream( StringUtils.concat( customWallpaperPath , File.separator , mThumbs.get( position ) ) );
					}
					catch( FileNotFoundException e )
					{
						e.printStackTrace();
					}
				}
				else
				{
					AssetManager asset = getResources().getAssets();
					try
					{
						is = asset.open( StringUtils.concat( wallpaperPath , File.separator , mThumbs.get( position ) ) );
					}
					catch( IOException e )
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				try
				{
					is = new FileInputStream( StringUtils.concat( downloadWallpaperPath , File.separator , downloadThumbs.get( position - mThumbs.size() ) ) );
				}
				catch( FileNotFoundException e )
				{
					e.printStackTrace();
				}
			}
			image.setImageBitmap( BitmapFactory.decodeStream( is ) );
			Drawable thumbDrawable = image.getDrawable();
			if( thumbDrawable != null )
			{
				thumbDrawable.setDither( true );
			}
			else
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "Error decoding thumbnail resId=" , mThumbs.get( position ) , " for wallpaper #" , position ) );
			}
			return image;
		}
	}
	
	public void onClick(
			View v )
	{
		//chenliang add start	//添加配置项“switch_enable_customer_lm_set_lockwallpaper”，增加锁屏壁纸的功能（进入uni3桌面壁纸，点击设置，弹出“设置桌面”、“设置锁屏”和“同时设置”选项）。true为显示，false不显示。默认为false。备注：该功能需要系统底层支持。
		/*
		 * setStream(InputStream data, Rect visibleCropHint, boolean allowBackup, int which)
		 * Rect visibleCropHint和boolean allowBackup分别是null和false
		 * int which,1是桌面壁纸，2是锁屏壁纸，3是两者都设置
		 */
		if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOMER_LM_SET_LOCKWALLPAPER )
		{
			switch( v.getId() )
			{
				case R.id.wallpaper_setting:
					final int position = mGallery.getSelectedItemPosition();
					new WallpaperLMLockerSettingListDialog( v.getContext() )
							//
							.builder()
							//设置锁屏
							.addSettingItem( R.string.wallpaper_lm_locker_setting_list_item_set_lockscreen , new OnWallpaperSettingItemClickListener() {
								
								@Override
								public void onClick()
								{
									boolean result = selectWallpaperFunctionType( position , 2 );
									setResult( result );
								}
							} )
							//设置桌面
							.addSettingItem( R.string.wallpaper_lm_locker_setting_list_item_set_desktop , new OnWallpaperSettingItemClickListener() {
								
								@Override
								public void onClick()
								{
									boolean result = selectWallpaperFunctionType( position , 1 );
									setResult( result );
								}
							} )
							//同时设置
							.addSettingItem( R.string.wallpaper_lm_locker_setting_list_item_set_both , new OnWallpaperSettingItemClickListener() {
								
								@Override
								public void onClick()
								{
									boolean result = selectWallpaperFunctionType( position , 3 );
									setResult( result );
								}
							} )
							//
							.show();
					break;
				default:
					break;
			}
		}
		//chenliang add end
		else
		{
			selectWallpaper( mGallery.getSelectedItemPosition() );
		}
	}
	
	//chenliang add start	//添加配置项“switch_enable_customer_lm_set_lockwallpaper”，增加锁屏壁纸的功能（进入uni3桌面壁纸，点击设置，弹出“设置桌面”、“设置锁屏”和“同时设置”选项）。true为显示，false不显示。默认为false。备注：该功能需要系统底层支持。
	private void setResult(
			boolean result )
	{
		if( result )
		{
			setResult( RESULT_OK );
			finish();
		}
	}
	//chenliang add end
	
	class WallpaperLoader extends AsyncTask<Integer , Void , Bitmap>
	{
		
		BitmapFactory.Options mOptions;
		
		WallpaperLoader()
		{
			mOptions = new BitmapFactory.Options();
			mOptions.inDither = false;
			mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		}
		
		protected Bitmap doInBackground(
				Integer ... params )
		{
			if( isCancelled() )
				return null;
			try
			{
				//            	 return BitmapFactory.decodeResource(getResources(),
				//                         mImages.get(params[0]), mOptions);
				InputStream is = null;
				if( params[0] < mThumbs.size() )
				{
					if( useCustomWallpaper )
					{
						try
						{
							is = new FileInputStream( StringUtils.concat( customWallpaperPath , File.separator , mImages.get( params[0] ) ) );
						}
						catch( FileNotFoundException e )
						{
							e.printStackTrace();
							return null;
						}
					}
					else
					{
						AssetManager asset = getResources().getAssets();
						try
						{
							is = asset.open( StringUtils.concat( wallpaperPath , File.separator , mImages.get( params[0] ) ) );
						}
						catch( IOException e )
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						}
					}
				}
				else
				{
					try
					{
						is = new FileInputStream( StringUtils.concat( downloadWallpaperPath , File.separator , downloadImages.get( params[0] - mThumbs.size() ) ) );
					}
					catch( FileNotFoundException e )
					{
						e.printStackTrace();
						return null;
					}
				}
				return BitmapFactory.decodeStream( is );
			}
			catch( OutOfMemoryError e )
			{
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(
				Bitmap b )
		{
			if( b == null )
				return;
			if( !isCancelled() && !mOptions.mCancel )
			{
				// Help the GC
				if( mBitmap != null )
				{
					mBitmap.recycle();
				}
				final ImageView view = mImageView;
				view.setImageBitmap( b );
				mBitmap = b;
				final Drawable drawable = view.getDrawable();
				drawable.setFilterBitmap( true );
				drawable.setDither( true );
				view.postInvalidate();
				mLoader = null;
			}
			else
			{
				b.recycle();
			}
		}
		
		void cancel()
		{
			mOptions.requestCancelDecode();
			super.cancel( true );
		}
	}
	
	//chenliang add start	//添加配置项“switch_enable_customer_lm_set_lockwallpaper”，增加锁屏壁纸的功能（进入uni3桌面壁纸，点击设置，弹出“设置桌面”、“设置锁屏”和“同时设置”选项）。true为显示，false不显示。默认为false。备注：该功能需要系统底层支持。
	private boolean selectWallpaperFunctionType(
			int position ,
			int which )
	{
		InputStream is = getWallpaperStream( position );
		if( is != null )
		{
			try
			{
				WallpaperManager wpm = (WallpaperManager)getSystemService( WALLPAPER_SERVICE );
				Method method = wpm.getClass().getMethod( "setStream" , InputStream.class , Rect.class , boolean.class , int.class );
				method.invoke( wpm , is , null , false , which );
				return true;
			}
			catch( Exception e )
			{
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	private InputStream getWallpaperStream(
			int position )
	{
		InputStream is = null;
		if( useCustomWallpaper )
		{
			try
			{
				is = new FileInputStream( customWallpaperPath + "/" + mImages.get( position ) );
			}
			catch( FileNotFoundException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			AssetManager asset = getResources().getAssets();
			try
			{
				is = asset.open( wallpaperPath + "/" + mImages.get( position ) );
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
		}
		return is;
	}
	//chenliang add end
}
