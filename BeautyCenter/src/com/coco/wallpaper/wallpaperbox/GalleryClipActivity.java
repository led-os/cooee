package com.coco.wallpaper.wallpaperbox;


import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.coco.lock2.lockbox.util.ContentConfig;
import com.coco.lock2.lockbox.util.LockManager;
import com.coco.theme.themebox.util.FunctionConfig;
import com.coco.theme.themebox.util.Tools;
import com.iLoong.base.themebox.R;


public class GalleryClipActivity extends Activity implements IWallpaperDialog , View.OnClickListener
{
	
	private ImageView imageView;
	private ImageButton backBtn;
	private Button setDeskTopWallpaper;
	private Button setLockWallpaper;
	private Button btn_setWallpaper_brzh;
	private Bitmap bmp;
	private ComponentName currentLock;
	private static Boolean isLockSupportChangeWallpaper = false;
	private WallpaperInfo infos;
	// @gaominghui2015/09/02 ADD START brzh改设置壁纸的显示方式
	private boolean brzh_setWallpaper = false;
	private Context mContext;
	
	// @gaominghui2015/09/02 ADD END
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onCreate( savedInstanceState );
		mContext = this;
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		infos = new WallpaperInfo( this );
		setContentView( R.layout.clip_gallery_image );
		File sdcardTempFile = new File( PathTool.getClipFilePath() );
		imageView = (ImageView)findViewById( R.id.clipImage );
		imageView.setVisibility( View.VISIBLE );
		bmp = BitmapFactory.decodeFile( sdcardTempFile.getAbsolutePath() );
		imageView.setImageBitmap( bmp );
		backBtn = (ImageButton)findViewById( R.id.btnReturn );
		backBtn.setOnClickListener( this );
		setDeskTopWallpaper = (Button)findViewById( R.id.setDeskTopWallpaper );
		setLockWallpaper = (Button)findViewById( R.id.setLockWallpaper );
		// @gaominghui2015/09/02 ADD START brzh改设置壁纸的显示方式
		brzh_setWallpaper = FunctionConfig.isEnable_apply_desktopwallpaper_lockwallpaper();
		if( brzh_setWallpaper )
		{
			btn_setWallpaper_brzh = (Button)findViewById( R.id.setWallpaper_brzh );
			btn_setWallpaper_brzh.setVisibility( View.VISIBLE );
			setDeskTopWallpaper.setVisibility( View.GONE );
			setLockWallpaper.setVisibility( View.GONE );
			btn_setWallpaper_brzh.setOnClickListener( this );
		}
		else
		{
			if( FunctionConfig.isEnableShowApplyLockWallpaper() )
			{
				setLockWallpaper.setVisibility( View.VISIBLE );
			}
			else
			{
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.WRAP_CONTENT );
				lp.setMargins( 50 , 0 , 50 , 0 );
				setLockWallpaper.setVisibility( View.GONE );
				setDeskTopWallpaper.setLayoutParams( lp );
			}
		}
		// @gaominghui2015/09/02 ADD END
		infos.setDisableSetWallpaperDimensions( FunctionConfig.getDisableSetWallpaperDimensions() );
		setDeskTopWallpaper.setOnClickListener( this );
		setLockWallpaper.setOnClickListener( this );
		isLockSupportChangeWallpaper = getcurrentLockInfo();
	}
	
	private Boolean getcurrentLockInfo()
	{
		LockManager mgr = new LockManager( this );
		currentLock = mgr.queryCurrentLock();
		Context dstContext = null;
		//第三方锁屏也要支持换壁纸,第三方换壁纸也要从我们的路径读取图片
		if( "com.third.test".equals( currentLock.getPackageName() ) )
		{
			return true;
		}
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
	
	@Override
	public void onClick(
			View v )
	{
		// TODO Auto-generated method stub
		if( v == backBtn )
		{
			finish();
		}
		else if( v == setDeskTopWallpaper )
		{
			setDesktopWallpaper();
		}
		else if( v == setLockWallpaper )
		{
			//如果去掉关于第三方锁屏或者系统锁屏能否支持锁屏壁纸的判断没有开我在去判断是否支持锁屏壁纸
			Log.v( "lvjiangbin" , "a  " + FunctionConfig.class );
			if( FunctionConfig.isRemove_enable_support_lockwallpaper_judge() )
			{
				isLockSupportChangeWallpaper = true;
			}
			if( !isLockSupportChangeWallpaper )
			{
				Toast.makeText( GalleryClipActivity.this , R.string.lockwallpaper_apply_tost , Toast.LENGTH_SHORT ).show();
				return;
			}
			setLockWallpaper();
		}
		// @gaominghui2015/09/02 ADD START brzh改设置壁纸的显示方式
		else if( v == findViewById( R.id.setWallpaper_brzh ) )
		{
			WallpaperDialog wallpaperDialog = new WallpaperDialog( this );
			wallpaperDialog.show( getFragmentManager() , "wallpaperDialog" );
		}
		// @gaominghui2015/09/02 ADD END
	}
	
	/**
	 *
	 * @author gaominghui 2015年9月2日
	 */
	@Override
	public void setLockWallpaper()
	{
		final ProgressDialog setLockWallpaperDialog = new ProgressDialog( this );
		setLockWallpaperDialog.setSecondaryProgress( ProgressDialog.STYLE_SPINNER );
		setLockWallpaperDialog.setMessage( this.getResources().getString( R.string.changingWallpaper ) );
		setLockWallpaperDialog.setCancelable( false );
		setLockWallpaperDialog.show();
		new Thread( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				//Bitmap newBitmap = PathTool.compressBitmap( bmp , getResources().getDisplayMetrics().widthPixels , getResources().getDisplayMetrics().heightPixels );
				boolean temp = false;
				final String time = String.valueOf( System.currentTimeMillis() );
				if( FunctionConfig.getLockWallpaperPath() == null || FunctionConfig.getLockWallpaperPath().equals( "" ) )
				{
					temp = Tools.saveMyBitmap( "/data/data/com.iLoong.base.themebox/lockwallpapers" , bmp );
				}
				else
				{
					if( brzh_setWallpaper )
					{
						temp = Tools.saveWallpaperBitmap( FunctionConfig.getLockWallpaperPath() , "/lock" + time + ".png" , bmp );
					}
					else
					{
						temp = Tools.saveMyBitmap( FunctionConfig.getLockWallpaperPath() , bmp );
					}
				}
				final boolean result = temp;
				setLockWallpaper.post( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						setLockWallpaperDialog.dismiss();
						if( result )
						{
							if( brzh_setWallpaper )
							{
								Settings.System.putString( mContext.getContentResolver() , "keyguard_wallpaper" , FunctionConfig.getLockWallpaperPath() + "/lock" + time + ".png" );
							}
							else
							{
								Toast.makeText( GalleryClipActivity.this , R.string.toast_setwallpaper_success , Toast.LENGTH_SHORT ).show();
							}
						}
						else
						{
							Toast.makeText( GalleryClipActivity.this , R.string.apply_fail , Toast.LENGTH_SHORT ).show();
						}
					}
				} );
			}
		} ).start();
	}
	
	/**
	 *
	 * @author gaominghui 2015年9月2日
	 */
	@Override
	public void setDesktopWallpaper()
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
				selsectWallpaper( PathTool.getClipFilePath() );
				setDeskTopWallpaper.post( new Runnable() {
					
					@Override
					public void run()
					{
						// TODO Auto-generated method stub
						dialog.dismiss();
						{
							Toast.makeText( GalleryClipActivity.this , R.string.toast_setwallpaper_success , Toast.LENGTH_SHORT ).show();
						}
						if( FunctionConfig.isEnableMoveTaskBackAfterSetDeskWallpaper() )
						{
							GalleryClipActivity.this.moveTaskToBack( true );
						}
					}
				} );
			}
		} ).start();
	}
	
	public void selsectWallpaper(
			String Path )
	{
		infos.setWallpaperByPath( Path );
		//		WallpaperManager wpm = (WallpaperManager)this.getSystemService( Context.WALLPAPER_SERVICE );
		//		try
		//		{
		//			if( bmp != null )
		//			{
		//				wpm.setBitmap( bmp );
		//			}
		//		}
		//		catch( Exception e )
		//		{
		//			e.printStackTrace();
		//		}
	}
}
