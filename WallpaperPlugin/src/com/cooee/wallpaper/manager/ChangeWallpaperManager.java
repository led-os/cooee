package com.cooee.wallpaper.manager;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.cooee.wallpaper.R;
import com.cooee.wallpaper.data.DownloadList;
import com.cooee.wallpaper.data.KmobAdverManager;
import com.cooee.wallpaper.data.UmengStatistics;
import com.cooee.wallpaper.host.util.DownloadDialog;
import com.cooee.wallpaper.util.ThreadUtil;
import com.cooee.wallpaper.util.Tools;
import com.cooee.wallpaper.wrap.DynamicImageView;
import com.cooee.wallpaper.wrap.IWallpaperCallbacks;
import com.cooee.wallpaperManager.WallpaperManagerBase;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.KmobManager;
import com.umeng.analytics.MobclickAgent;


// cheyingkun add whole file //一键换壁纸需求。（剩余：动态图标、自定义事件统计）
@SuppressLint( "NewApi" )
public class ChangeWallpaperManager implements OnClickListener , DownloadList.DownloadWallpaperCallbacks , KmobAdverManager.KmobAdWallpaperCallbacks
{
	
	private Context mContext;
	private static Context mContainerContext;
	private static ChangeWallpaperManager mInstance;
	/**保存当前壁纸成功*/
	private final int MESSAGE_SAVE_WALLPAPER_SUCCESS = 1;
	/**保存当前壁纸完毕失败*/
	private final int MESSAGE_SAVE_WALLPAPER_FAIL = 2;
	/**设置壁纸成功*/
	private final int MESSAGE_CHANGE_WALLPAPER_SUCCESS = 3;
	/**点击WAIT_TIME后，没有获取到网络壁纸，直接设置本地壁纸*/
	private final int MESSAGE_CHANGE_TIME_OUT = 4;
	private final int MESSAGE_CHANGE_FAIL = 5;
	private final int MESSAGE_SHOW_SUCCESS_VIEW = 6;
	private int WAIT_TIME = 8000;
	private final int BANNER_ID = 1000;
	/**获取成功的网络壁纸图片*/
	/**本地壁纸的下标*/
	private int lastWallpaperIndexByFileOrAsset = 0;
	//
	//
	//
	/**路径带优化*/
	/**默认壁纸存放路径为assets/launcher/wallpapers/*/
	public static final String WALLPAPER_PAHT = "launcher/wallpapers";
	//系统目录下的壁纸路径
	public static String CUSTOM_WALLPAPER_PATH = "/system/wallpapers";
	//sd卡中保存的网络壁纸路径
	private final String SAVE_WALLPAPER_BITMAP_PATH = "/pl_ad_wallpapers";
	//壁纸保存到sd卡的路径
	private final String SAVE_SDCARD_PATH = "/Image";
	private ArrayList<WallPaperFile> mWallpapers = new ArrayList<WallPaperFile>( 24 );
	private int mDefaultWallpaperId = -1;
	/**res资源中无默认壁纸时，assets中第一张壁纸作为默认壁纸*/
	private WallPaperFile mDefaultWallPaperFile = null;
	private Resources mDefaultWallpaperRes = null;
	//
	/**更换壁纸成功后,显示出来的view*/
	private View changeWallpaperSuccessView;
	private ChangeWallpaperInterface mChangeWallpaperInterface;
	/**上次的壁纸,保存成文件，换回去时再设置文件为壁纸，如果保存图片的话，设置壁纸时间比较长*/
	private String lastWallpaperPath;
	private Bitmap lastBitmap;
	/**等待设置壁纸*/
	boolean isWaitingSetWallpaper = false;
	private static IWallpaperCallbacks mInterface;
	//
	//	private View showView;//cheyingkun add	//一键换壁纸(动态图标)
	private DownloadList mDownloadList;
	private AdBaseView mWallpaperAdverView = null;
	private boolean isAdverViewShow = false;//用来标记广告是否展示过
	private boolean isSettingWallpaper = false;
	public static boolean SWITCH_ENABLE_UMENG = true;
	public static boolean SWITCH_ENABLE_ADS = true;
	public static boolean SWITCH_ENABEL_ADS_ONLINE = false;//服务器上广告的开关
	private boolean SWITCH_ENABLE_DEBUG = true;
	private DynamicImageView mDynamicView;
	private final String BEAUTY_CENTER_PACKAGE_NAME = "com.iLoong.base.themebox";
	private final String BEAUTY_CENTER_CLASS_NAME = "com.coco.theme.themebox.MainActivity";
	private final String NANO_PACKAGE_NAME = "com.cooeeui.wallpaper";
	private final String NANO_CLASS_NAME = "com.cooeeui.wallpaper.OnlineWallpaperActivity";
	public final static int ONLINE_WALLPAPER_FROM_NANO = 1;
	public final static int ONLINE_WALLPAPER_FROM_BEAUTY_CENTER = 2;
	public static int Online_wallpaper_from = 1;//1表示读取Nano壁纸中的资源，2表示获取美化中心的资源 
	public boolean result = false;
	
	public interface ChangeWallpaperInterface
	{
		
		public void showSuccessView();
		
		public void hideSuccessView(
				boolean isAnim ,
				boolean isFinish );
		
		public void finish();
		
		public boolean isFinishing();
	}
	
	public void setChangeWallpaperInterface(
			ChangeWallpaperInterface mChangeWallpaperInterface )
	{
		this.mChangeWallpaperInterface = mChangeWallpaperInterface;
	}
	
	private ChangeWallpaperManager(
			Context mContext )
	{
		this.mContext = mContext;
		mDownloadList = new DownloadList( mContext , this );
		KmobAdverManager.getKmobAdverManager( mContext ).setKmobAdWallpaperCallbacks( this );
		WAIT_TIME = mContext.getResources().getInteger( R.integer.one_key_wallpaper_wait_time );
		ThreadUtil.execute( new Runnable() {
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				loadWallpaperInfo();
			}
		} );
	}
	
	public static ChangeWallpaperManager getInstance(
			Context mContext )
	{
		if( mInstance == null )
		{
			synchronized( ChangeWallpaperManager.class )
			{
				if( mInstance == null )
				{
					mInstance = new ChangeWallpaperManager( mContext );
				}
			}
		}
		return mInstance;
	}
	
	private Handler mHandler = new Handler() {
		
		public void handleMessage(
				Message msg )
		{
			if( msg.what == MESSAGE_SAVE_WALLPAPER_SUCCESS )
			{
				Toast.makeText( mContext , R.string.save_wallpaper_success , Toast.LENGTH_SHORT ).show();
			}
			else if( msg.what == MESSAGE_SAVE_WALLPAPER_FAIL )
			{
				Toast.makeText( mContext , R.string.save_wallpaper_fail , Toast.LENGTH_SHORT ).show();
			}
			else if( msg.what == MESSAGE_CHANGE_WALLPAPER_SUCCESS )
			{
				result = true;
				stopOneKeyChangeWallpaper();
				//				mHandler.sendEmptyMessageDelayed( MESSAGE_SHOW_SUCCESS_VIEW , 1600 );//代码无效，已经在动画结束时，调用
			}
			else if( msg.what == MESSAGE_CHANGE_TIME_OUT )
			{
				if( isWaitingSetWallpaper )
				{
					isWaitingSetWallpaper = false;
					ThreadUtil.execute( new Runnable() {
						
						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							changeLocalWallpaper();
							Log.v( "operateWallpaperData" , "timeout changeLocalWallpaper" );
						}
					} );
				}
			}
			else if( msg.what == MESSAGE_CHANGE_FAIL )
			{
				result = false;
				stopOneKeyChangeWallpaper();
				Toast.makeText( mContext , R.string.change_wallpaper_fail , Toast.LENGTH_SHORT ).show();
				if( mChangeWallpaperInterface != null )
				{
					mChangeWallpaperInterface.finish();
				}
			}
			else if( msg.what == MESSAGE_SHOW_SUCCESS_VIEW )
			{
				if( mChangeWallpaperInterface != null )
				{
					//					initWallpaperChangedView();//显示时获取已经去初始化了
					mChangeWallpaperInterface.showSuccessView();
				}
			}
			Log.v( "operateWallpaperData" , "mHandler id = " + msg.what );
		};
	};
	
	public boolean getChangeWallpaperResult()
	{
		return result;
	}
	
	/**
	 * 开始一键换壁纸
	 * @param intent
	 * @param v
	 */
	public void startOneKeyChangeWallpaper(
			DynamicImageView v )
	{
		if( isSettingWallpaper )
		{
			return;
		}
		isSettingWallpaper = true;
		//cheyingkun add start	//一键换壁纸(友盟统计)
		if( SWITCH_ENABLE_UMENG )
		{
			MobclickAgent.onEvent( mContainerContext , UmengStatistics.click_one_key_change_wallpapper );
		}
		//cheyingkun add end
		isWaitingSetWallpaper = true;
		//1.图标开始动画
		//cheyingkun add start	//一键换壁纸(动态图标)
		if( v != null )
		{
			mDynamicView = v;
			v.playAnim();
		}
		//cheyingkun add end
		//2.保存当前壁纸
		getCurrentWallpaperBitmap();
		//3.获取美化中心的在线壁纸
		mDownloadList.getWallpaperOnLine();
		//4.获取广告
		if( SWITCH_ENABLE_ADS || SWITCH_ENABEL_ADS_ONLINE )
		{
			if( mWallpaperAdverView == null || !isAdverViewShow )//若没有广告或者广告已经展示过，则重新获取
			{
				mWallpaperAdverView = null;
				isAdverViewShow = false;
				KmobAdverManager.getKmobAdverManager( mContext ).initWallpaperAdverAdver();
			}
		}
		mHandler.sendEmptyMessageDelayed( MESSAGE_CHANGE_TIME_OUT , WAIT_TIME );
	}
	
	//cheyingkun add start	//一键换壁纸(动态图标)
	/**
	 * 结束一键换壁纸
	 */
	public void stopOneKeyChangeWallpaper()
	{
		isSettingWallpaper = false;
		Log.v( "operateWallpaperData" , "stopOneKeyChangeWallpaper = " + mDynamicView );
		if( mDynamicView != null )
		{
			mDynamicView.stopAnim();
		}
	}
	
	//cheyingkun add end
	/**
	 * 一键换壁纸
	 */
	private void changeLocalWallpaper()
	{
		if( lastWallpaperIndexByFileOrAsset >= mWallpapers.size() || lastWallpaperIndexByFileOrAsset < 0 )
		{
			lastWallpaperIndexByFileOrAsset = 0;
		}
		if( setWallpaper( lastWallpaperIndexByFileOrAsset ) )
		{
			lastWallpaperIndexByFileOrAsset++;
			mHandler.sendEmptyMessage( MESSAGE_CHANGE_WALLPAPER_SUCCESS );
		}
		else
		{
			mHandler.sendEmptyMessage( MESSAGE_CHANGE_FAIL );
		}
	}
	
	/**
	
	/**
	 * 修改壁纸之后,初始化显示三个按钮的view
	 */
	private void initWallpaperChangedView()
	{
		if( changeWallpaperSuccessView == null )
		{
			changeWallpaperSuccessView = LayoutInflater.from( mContext ).cloneInContext( mContext ).inflate( R.layout.change_wallpaper_success , null );
			Log.v( "zjp" , "mContext = " + mContext + " changeWallpaperSuccessView = " + changeWallpaperSuccessView );
			View cancle = changeWallpaperSuccessView.findViewById( R.id.change_wallpaper_item_cancle );
			View saveGallery = changeWallpaperSuccessView.findViewById( R.id.change_wallpaper_item_save_gallery );
			View more = changeWallpaperSuccessView.findViewById( R.id.change_wallpaper_item_more );
			//			View banner_cancle = changeWallpaperSuccessView.findViewById( R.id.banner_cancle );
			cancle.setOnClickListener( this );
			saveGallery.setOnClickListener( this );
			more.setOnClickListener( this );
			//			banner_cancle.setOnClickListener( this );
		}
		FrameLayout banner = (FrameLayout)changeWallpaperSuccessView.findViewById( R.id.banner );
		if( ( SWITCH_ENABLE_ADS || SWITCH_ENABEL_ADS_ONLINE ) && mWallpaperAdverView != null && mWallpaperAdverView.getParent() == null )
		{
			mWallpaperAdverView.setId( BANNER_ID );
			mWallpaperAdverView.setOnClickListener( this );
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT , FrameLayout.LayoutParams.WRAP_CONTENT );
			banner.addView( mWallpaperAdverView , 0 , params );
			banner.setVisibility( View.VISIBLE );
			isAdverViewShow = true;
			Log.v( "operateWallpaperData" , "ad show" );
			if( SWITCH_ENABLE_UMENG )
			{
				MobclickAgent.onEvent( mContainerContext , UmengStatistics.one_key_change_wallpapper_AD_pv );
			}
		}
		else
		{
			banner.setVisibility( View.GONE );
			Log.v( "operateWallpaperData" , "ad hide" );
		}
		ImageView cancle = (ImageView)changeWallpaperSuccessView.findViewById( R.id.change_wallpaper_item_cancle_img );
		if( lastWallpaperPath != null || lastBitmap != null )
		{
			cancle.setImageResource( R.drawable.onekey_changewallpaper_changeback_selector );
		}
		else
		{
			cancle.setImageResource( R.drawable.onekey_changewallpaper_changeback_pressed );
		}
	}
	
	/**
	 * 预览界面点击事件
	 */
	public void onClick(
			View v )
	{
		switch( v.getId() )
		{
			case R.id.change_wallpaper_item_cancle:
				//换回去
				if( mChangeWallpaperInterface != null )
				{
					mChangeWallpaperInterface.hideSuccessView( true , true );
				}
				Log.v( "operateWallpaperData" , "onclick lastBitmap = " + lastWallpaperPath );
				if( lastWallpaperPath == null && lastBitmap == null )
				{
					Toast.makeText( mContext , R.string.change_back_no_support , Toast.LENGTH_SHORT ).show();
					return;
				}
				ThreadUtil.execute( new Runnable() {
					
					@Override
					public void run()
					{
						if( lastWallpaperPath != null || lastBitmap != null )
						{
							Log.v( "operateWallpaperData" , "changeback start" );
							isSettingWallpaper = true;
							try
							{
								if( lastWallpaperPath != null )
								{
									InputStream stream = new FileInputStream( lastWallpaperPath );
									InputStream newStream = new FileInputStream( lastWallpaperPath );
									WallpaperManagerBase.getInstance( mContext ).setWallpaperAndDimension( stream , newStream );
								}
								else
								{
									WallpaperManagerBase.getInstance( mContext ).setWallpaperAndDimensionByBitmap( lastBitmap , true );
									if( Build.VERSION.SDK_INT >= 14 )
										WallpaperManager.getInstance( mContext ).forgetLoadedWallpaper();
								}
							}
							catch( FileNotFoundException e )
							{
								// TODO Auto-generated catch block
								WallpaperManagerBase.getInstance( mContext ).setWallpaperAndDimensionByBitmap( lastBitmap , true );
								if( Build.VERSION.SDK_INT >= 14 )
									WallpaperManager.getInstance( mContext ).forgetLoadedWallpaper();
							}
							//cheyingkun add start	//一键换壁纸(友盟统计)
							if( SWITCH_ENABLE_UMENG )
							{
								MobclickAgent.onEvent( mContainerContext , UmengStatistics.one_key_change_wallpapper_back );
							}
							isSettingWallpaper = false;
							//cheyingkun add end
							Log.v( "operateWallpaperData" , "changeback end" );
						}
					}
				} );
				break;
			case R.id.change_wallpaper_item_save_gallery:
				//保存到本地壁纸
				if( mChangeWallpaperInterface != null )
				{
					mChangeWallpaperInterface.hideSuccessView( true , true );
				}
				ThreadUtil.execute( new Runnable() {
					
					@Override
					public void run()
					{
						if( SWITCH_ENABLE_DEBUG )
						{
							Log.d( "operateWallpaperData" , " change_wallpaper_item_save_gallery " );
						}
						WallpaperManager wallpaperManager = WallpaperManager.getInstance( mContext );
						// 获取当前壁纸
						Drawable wallpaperDrawable = wallpaperManager.getDrawable();
						// 将Drawable,转成Bitmap
						Bitmap currentBitmap = ( (BitmapDrawable)wallpaperDrawable ).getBitmap();
						SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddHHmmss" );
						Date d1 = new Date( System.currentTimeMillis() );
						String sdcardPath = getSDCardPath();
						String result = null;
						if( sdcardPath != null )
						{
							result = saveBitmap( currentBitmap , sdcardPath + SAVE_SDCARD_PATH , format.format( d1 ) + ".jpg" , true );
						}
						if( currentBitmap != null && !currentBitmap.isRecycled() )
						{
							if( Build.VERSION.SDK_INT >= 14 )
								wallpaperManager.forgetLoadedWallpaper();
							currentBitmap.recycle();
							currentBitmap = null;
						}
						if( result != null )
						{
							Message msg = new Message();
							msg.what = MESSAGE_SAVE_WALLPAPER_SUCCESS;
							mHandler.sendMessage( msg );
						}
						else
						{
							Message msg = new Message();
							msg.what = MESSAGE_SAVE_WALLPAPER_FAIL;
							mHandler.sendMessage( msg );
						}
						//cheyingkun add start	//一键换壁纸(友盟统计)
						if( SWITCH_ENABLE_UMENG )
						{
							MobclickAgent.onEvent( mContainerContext , UmengStatistics.one_key_change_wallpapper_save );
						}
						//cheyingkun add end
					}
				} );
				break;
			case R.id.change_wallpaper_item_more:
				//进入美化中心
				if( mChangeWallpaperInterface != null )
				{
					mChangeWallpaperInterface.hideSuccessView( false , false );
				}
				enterBeautyCenter();
				//					mOneKeyChangeWallpaperInterface.onClickOnekeyChangeWallpaperMoreView();
				//cheyingkun add start	//一键换壁纸(友盟统计)
				if( SWITCH_ENABLE_UMENG )
				{
					MobclickAgent.onEvent( mContainerContext , UmengStatistics.one_key_change_wallpapper_more );
				}
				//cheyingkun add end
				break;
			case BANNER_ID:
				if( mChangeWallpaperInterface != null )
				{
					mChangeWallpaperInterface.hideSuccessView( true , true );
				}
				if( v.getTag() != null )
				{
					String info = v.getTag().toString();
					KmobManager.onClickDone( info , true );
				}
				if( SWITCH_ENABLE_UMENG )
				{
					MobclickAgent.onEvent( mContainerContext , UmengStatistics.click_one_key_change_wallpapper_AD );
				}
				break;
			default:
				break;
		}
	}
	
	public View getInitedSucessView()
	{
		initWallpaperChangedView();
		return changeWallpaperSuccessView;
	}
	
	/**
	 * @return 预览界面的view
	 */
	public View getChangeWallpaperSuccessAfterView()
	{
		return changeWallpaperSuccessView;
	}
	
	public void removeAdverView()
	{
		FrameLayout banner = (FrameLayout)changeWallpaperSuccessView.findViewById( R.id.banner );
		if( mWallpaperAdverView != null && mWallpaperAdverView.getParent() == banner )
		{
			banner.removeView( mWallpaperAdverView );
			mWallpaperAdverView = null;
		}
	}
	
	/**获取当前壁纸*/
	@SuppressLint( "NewApi" )
	public void getCurrentWallpaperBitmap()
	{
		ThreadUtil.execute( new Runnable() {
			
			@Override
			public void run()
			{
				//修改标志位
				if( SWITCH_ENABLE_DEBUG )
				{
					Log.d( "operateWallpaperData" , "getCurrentWallpaperBitmap start" );
				}
				WallpaperManager wallpaperManager = WallpaperManager.getInstance( mContext );
				if( lastWallpaperPath != null )
				{
					File file = new File( lastWallpaperPath );
					if( file != null && file.exists() )
					{
						file.delete();
					}
					lastWallpaperPath = null;
				}
				if( lastBitmap != null && !lastBitmap.isRecycled() )
				{
					if( Build.VERSION.SDK_INT >= 14 )
						wallpaperManager.forgetLoadedWallpaper();
					lastBitmap.recycle();
					lastBitmap = null;
				}
				// 获取当前壁纸
				WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
				if( wallpaperInfo == null )
				{
					Drawable wallpaperDrawable = wallpaperManager.getDrawable();
					// 将Drawable,转成Bitmap
					lastBitmap = ( (BitmapDrawable)wallpaperDrawable ).getBitmap();
					if( !lastBitmap.isRecycled() )
						lastWallpaperPath = saveBitmap( lastBitmap , mContext.getFilesDir().toString() , "lastwallpaper.temp" , false );
				}
				if( SWITCH_ENABLE_DEBUG )
				{
					Log.d( "operateWallpaperData" , "getCurrentWallpaperBitmap end" );
				}
			}
		} );
	}
	
	public String saveBitmap(
			Bitmap mBitmap ,
			String filepath ,
			String mSaveName ,
			boolean isToGallery )
	{
		//保存Bitmap   
		FileOutputStream fos = null;
		try
		{
			File path = new File( filepath );
			//文件  
			String bitmappath = filepath + "/" + mSaveName;
			File file = new File( bitmappath );
			if( !path.exists() )
			{
				path.mkdirs();
			}
			if( !file.exists() )
			{
				file.createNewFile();
			}
			fos = new FileOutputStream( file );
			if( fos != null )
			{
				mBitmap.compress( Bitmap.CompressFormat.JPEG , 90 , fos );
				fos.flush();
				fos.close();
			}
			if( isToGallery )
			{
				try
				{
					MediaStore.Images.Media.insertImage( mContext.getContentResolver() , file.getAbsolutePath() , mSaveName , null );
				}
				catch( FileNotFoundException e )
				{
					e.printStackTrace();
				}
			}
			// 最后通知图库更新
			//			mContext.sendBroadcast( new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE , Uri.parse( "file://" + filepath ) ) );
			return bitmappath;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			if( fos != null )
			{
				try
				{
					fos.flush();
					fos.close();
				}
				catch( IOException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getSDCardPath()
	{
		if( mContext == null )
		{
			return null;
		}
		File sdcardDir = null;
		//获取T卡是否准备就绪  
		if( Tools.isSDCardExist() )
		{
			sdcardDir = Environment.getExternalStorageDirectory();
			return sdcardDir.toString();
		}
		Toast.makeText( mContext , R.string.msg_insert_SD , Toast.LENGTH_LONG ).show();
		return null;
	}
	
	@Override
	public void changeDownloadImage(
			Bitmap bitmap ,
			String pkgname )
	{
		// TODO Auto-generated method stub
		Log.v( "operateWallpaperData" , "changeDownloadImage = " + bitmap + " " + pkgname );
		WallPaperFile file = new WallPaperFile( DownloadList.getExistFile( mContext , pkgname ) , WallPaperFileFromEnum.download );
		mWallpapers.add( file );
		if( isWaitingSetWallpaper )
		{
			isWaitingSetWallpaper = false;
			if( setWallpaper( mWallpapers.size() - 1 ) )
			{
				mHandler.sendEmptyMessage( MESSAGE_CHANGE_WALLPAPER_SUCCESS );
			}
			else
			{
				changeLocalWallpaper();
				Log.v( "operateWallpaperData" , "changeDownloadImage changeLocalWallpaper" );
			}
		}
		if( bitmap != null && !bitmap.isRecycled() )
		{
			bitmap.recycle();
		}
	}
	
	@Override
	public void changeLocalImage()
	{
		// TODO Auto-generated method stub
		if( isWaitingSetWallpaper )
		{
			isWaitingSetWallpaper = false;
			changeLocalWallpaper();
			Log.v( "operateWallpaperData" , "changeLocalImage changeLocalWallpaper" );
		}
	}
	
	@Override
	public void notifyKmobAdDataChanged(
			AdBaseView bannerView )
	{
		// TODO Auto-generated method stub
		mWallpaperAdverView = bannerView;
		isAdverViewShow = false;
	}
	
	/**描述壁纸来源*/
	public enum WallPaperFileFromEnum
	{
		assets , custom , otherapk , download
	}
	
	public class WallPaperFile
	{
		
		/**壁纸文件名*/
		String fileName;
		/**壁纸来源*/
		WallPaperFileFromEnum fileFrom;
		/**apk壁纸资源ID*/
		int fileDrawable;
		
		public WallPaperFile(
				String fileName ,
				WallPaperFileFromEnum from )
		{
			this.fileName = fileName;
			this.fileFrom = from;
		}
		
		public WallPaperFile(
				String fileName ,
				WallPaperFileFromEnum from ,
				String postfix )
		{
			this.fileName = fileName;
			this.fileFrom = from;
		}
		
		public WallPaperFile(
				int drawable )
		{
			this.fileDrawable = drawable;
			this.fileFrom = WallPaperFileFromEnum.otherapk;
		}
		
		public String getFileName()
		{
			return fileName;
		}
	}
	
	/**
	 * 获取默认壁纸存放的resource和Id，可能是我们launcher的，也可能是第三方apk的
	 * @return
	 */
	//	private void obtainDefaultWallpaperResAndId()
	//	{
	//		int id = -1;
	//		//xiatian start	//需求：默认主题壁纸外置（使用包名为“config_custom_default_wallpaper_package_name”的res/drawable中的资源“config_custom_default_wallpaper_resource_name”），若“config_custom_default_wallpaper_package_name”和“config_custom_default_wallpaper_resource_name”其中有一个配置为空，则使用桌面默认主题的壁纸。
	//		//				id = mThemeDescription.getResourceID( "default_wallpaper" );//xiatian del
	//		//xiatian add start
	//		Resources res = null;
	//		String custom_default_wallpaper_package_name = WallpaperManagerBase.CONFIG_CUSTOM_DEFAULT_WALLPAPER_PACKAGE_NAME;
	//		String custom_default_wallpaper_resource_name = WallpaperManagerBase.CONFIG_CUSTOM_DEFAULT_WALLPAPER_RESOURCE_NAME;
	//		if( !( TextUtils.isEmpty( custom_default_wallpaper_package_name ) || ( TextUtils.isEmpty( custom_default_wallpaper_resource_name ) ) ) )
	//		{
	//			Context slaveContext = null;
	//			try
	//			{
	//				slaveContext = mContext.createPackageContext( custom_default_wallpaper_package_name , Context.CONTEXT_IGNORE_SECURITY );
	//			}
	//			catch( NameNotFoundException e )
	//			{
	//				e.printStackTrace();
	//			}
	//			if( slaveContext != null )
	//			{
	//				res = slaveContext.getResources();
	//				id = ResourceUtils.getDrawableResourceIdByReflectIfNecessary( 0 , res , custom_default_wallpaper_package_name , custom_default_wallpaper_resource_name );
	//			}
	//		}
	//		else
	//		{
	//			res = mContext.getResources();
	//			id = res.getIdentifier( "default_wallpaper" , "drawable" , mContext.getPackageName() );
	//		}
	//		mDefaultWallpaperId = id;
	//		mDefaultWallpaperRes = res;
	//	}
	/**
	 * 获取assets/launcher/wallpapers目录下的第一张壁纸大图作为默认壁纸
	 * @author yangtianyu 2015-11-13
	 */
	private void obtainDefaultWallPaperFile()
	{
		try
		{
			String[] wallpapers = null;
			AssetManager assManager = mContext.getResources().getAssets();
			wallpapers = assManager.list( WALLPAPER_PAHT );
			if( wallpapers != null && wallpapers.length > 0 )
			{
				for( String name : wallpapers )
				{
					//如果不是以_small结尾的则是大壁纸
					if( name.endsWith( ".jpg" ) && !name.substring( 0 , name.length() - ".jpg".length() ).endsWith( "_small" ) )
					{
						mDefaultWallPaperFile = new WallPaperFile( name , WallPaperFileFromEnum.assets );
						return;
					}
					else if( name.endsWith( ".png" ) && !name.substring( 0 , name.length() - ".png".length() ).endsWith( "_small" ) )
					{
						mDefaultWallPaperFile = new WallPaperFile( name , WallPaperFileFromEnum.assets , ".png" );
						return;
					}
				}
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public void loadWallpaperInfo()
	{
		String[] wallpapers = null;
		//		//1.加载apk大壁纸:从apk的res中读壁纸  现有的壁纸名称方案不适用于apk壁纸
		//		if( mDefaultWallpaperId != 0 )
		//		{
		//			WallPaperFile file = new WallPaperFile( mDefaultWallpaperId );
		//			mWallpapers.add( file );
		//		}
		//		if( mDefaultWallPaperFile != null )
		//		{
		//			mWallpapers.add( mDefaultWallPaperFile );
		//		}
		//2.加载本地大壁纸:本地大壁纸名称格式设计成00com.coco.wallpaper.英文名#中文名.jpg 其中00表示两位数字用于壁纸排序
		File dir = new File( CUSTOM_WALLPAPER_PATH );
		try
		{
			//2.1从指定目录读取
			if( dir.exists() && dir.isDirectory() )
			{
				wallpapers = dir.list();
				for( String name : wallpapers )
				{
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸 tangliang 2015-1-23 ADD START
					File filetemp = new File( CUSTOM_WALLPAPER_PATH + "/" + name.substring( 0 , name.length() - ".jpg".length() ) + "_small.jpg" );
					if( filetemp.exists() && !name.substring( 0 , name.length() - ".jpg".length() ).endsWith( "_small" ) )
					{
						WallPaperFile file = new WallPaperFile( name , WallPaperFileFromEnum.custom );
						mWallpapers.add( file );
					}
					filetemp = new File( CUSTOM_WALLPAPER_PATH + "/" + name.substring( 0 , name.length() - ".png".length() ) + "_small.png" );
					if( filetemp.exists() && !name.substring( 0 , name.length() - ".png".length() ).endsWith( "_small" ) )
					{
						WallPaperFile file = new WallPaperFile( name , WallPaperFileFromEnum.custom , ".png" );
						mWallpapers.add( file );
					}
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸 tangliang 2015-1-23 ADD END
				}
			}
			//2.2从assets中指定目录读取
			else
			{
				AssetManager assManager = mContext.getResources().getAssets();
				wallpapers = assManager.list( WALLPAPER_PAHT );
				for( String name : wallpapers )
				{
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸 tangliang 2015-1-23 ADD START
					if( name.endsWith( ".jpg" ) && !name.substring( 0 , name.length() - ".jpg".length() ).endsWith( "_small" ) )
					{
						WallPaperFile file = new WallPaperFile( name , WallPaperFileFromEnum.assets );
						mWallpapers.add( file );
					}
					else if( name.endsWith( ".png" ) && !name.substring( 0 , name.length() - ".png".length() ).endsWith( "_small" ) )
					{
						WallPaperFile file = new WallPaperFile( name , WallPaperFileFromEnum.assets , ".png" );
						mWallpapers.add( file );
					}
					//修改匹配大壁纸逻辑:如果不是以_small结尾的则是大壁纸 tangliang 2015-1-23 ADD END
				}
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		try
		{
			String sdCardPath = getSDCardPath();
			if( sdCardPath != null )
			{
				File sdWallpaperFile = new File( sdCardPath + SAVE_WALLPAPER_BITMAP_PATH );
				if( sdWallpaperFile.exists() && sdWallpaperFile.isDirectory() )
				{
					wallpapers = sdWallpaperFile.list();
					for( String name : wallpapers )
					{
						if( name.endsWith( ".jpg" ) || name.endsWith( ".png" ) || name.endsWith( ".tupian" ) )
						{
							WallPaperFile wFile = new WallPaperFile( sdCardPath + SAVE_WALLPAPER_BITMAP_PATH + "/" + name , WallPaperFileFromEnum.download );
							mWallpapers.add( wFile );
						}
					}
				}
			}
			File sdWallpaperFile = new File( mContext.getFilesDir().toString() + SAVE_WALLPAPER_BITMAP_PATH );
			if( sdWallpaperFile.exists() && sdWallpaperFile.isDirectory() )
			{
				wallpapers = sdWallpaperFile.list();
				for( String name : wallpapers )
				{
					if( name.endsWith( ".tupian" ) )
					{
						WallPaperFile wFile = new WallPaperFile( mContext.getFilesDir().toString() + SAVE_WALLPAPER_BITMAP_PATH + "/" + name , WallPaperFileFromEnum.download );
						mWallpapers.add( wFile );
					}
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public boolean setWallpaper(
			int position )
	{
		InputStream stream = null;
		InputStream newStream = null;
		AssetManager asset = mContext.getResources().getAssets();
		try
		{
			WallPaperFile file = mWallpapers.get( position );
			Log.v( "wallpaper" , "filename:" + file.fileName + "" + " from:" + file.fileFrom + " position:" + position );
			if( file.fileFrom == WallPaperFileFromEnum.assets )
			{
				stream = asset.open( WALLPAPER_PAHT + "/" + file.fileName );
				newStream = asset.open( WALLPAPER_PAHT + "/" + file.fileName );
			}
			else if( file.fileFrom == WallPaperFileFromEnum.custom )
			{
				try
				{
					stream = new FileInputStream( CUSTOM_WALLPAPER_PATH + "/" + file.fileName );
					newStream = new FileInputStream( CUSTOM_WALLPAPER_PATH + "/" + file.fileName );
				}
				catch( FileNotFoundException e )
				{
					e.printStackTrace();
					mWallpapers.remove( file );
					return false;
				}
			}
			else if( file.fileFrom == WallPaperFileFromEnum.otherapk )
			{
				//对于存在于apk中的壁纸，选择设置壁纸为通过drawable的id去设置壁纸,不要通过生成bitmap再设置壁纸，太慢，而且会容易造成内存溢出 wanghongjian add bug:【i_0011709】
				WallpaperManagerBase.getInstance( mContext ).setWallpaperById( file.fileDrawable );
				//				Bitmap bitmap = Tools.drawableToBitmap( mDefaultWallpaperRes.getDrawable( file.fileDrawable ) );
				//				if( bitmap != null && !bitmap.isRecycled() )
				//				{
				//					WallpaperManagerBase.getInstance( mContext ).setWallpaperAndDimensionByBitmap( bitmap , true );
				//				}
			}
			else if( file.fileFrom == WallPaperFileFromEnum.download )
			{
				try
				{
					stream = new FileInputStream( file.fileName );
					newStream = new FileInputStream( file.fileName );
				}
				catch( FileNotFoundException e )
				{
					e.printStackTrace();
					mWallpapers.remove( file );
					return false;
				}
			}
			if( file.fileFrom != WallPaperFileFromEnum.otherapk )
			{
				if( stream != null && newStream != null )
				{
					WallpaperManagerBase.getInstance( mContext ).setWallpaperAndDimension( stream , newStream );
				}
				if( SWITCH_ENABLE_UMENG )
				{
					if( file.fileFrom != WallPaperFileFromEnum.download )
					{
						MobclickAgent.onEvent( mContainerContext , UmengStatistics.one_key_change_native_wallpapper );
					}
					else
					{
						MobclickAgent.onEvent( mContainerContext , UmengStatistics.one_key_change_network_wallpapper );
					}
				}
			}
			return true;
		}
		catch( Exception e )
		{
			Log.e( "operateWallpaperData" , "Failed to set wallpaper: " + e );
			return false;
		}
		finally
		{
			if( stream != null )
			{
				try
				{
					stream.close();
					stream = null;
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
			if( newStream != null )
			{
				try
				{
					newStream.close();
					newStream = null;
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void enterBeautyCenter()
	{
		Log.v( "zjp" , "enterBeautyCenter Online_wallpaper_from = " + Online_wallpaper_from );
		if( Online_wallpaper_from == ONLINE_WALLPAPER_FROM_NANO )
		{
			ComponentName nanao = new ComponentName( NANO_PACKAGE_NAME , NANO_CLASS_NAME );//没有美化中心，则默认下载nano壁纸，打开nano壁纸
			if( Tools.isApkInstalled( mContext , nanao ) == false )
			{
				if( mChangeWallpaperInterface != null )
				{
					if( mChangeWallpaperInterface.isFinishing() )//因为下方的dialog依附于activity，若activity正在做要结束的动画的话就不要启动dialog了
					{
						return;
					}
				}
				DownloadDialog.downloadApkCooeeDialog( mContainerContext , mContext.getResources().getString( R.string.overview_panel_button_nano_wallpaper ) , NANO_PACKAGE_NAME , true );
				if( SWITCH_ENABLE_UMENG )
				{
					MobclickAgent.onEvent( mContainerContext , UmengStatistics.one_key_change_wallpapper_download_nano );
				}
			}
			else
			{
				Intent mIntent = new Intent();
				mIntent.setComponent( nanao );
				mContext.startActivity( mIntent );
				if( mChangeWallpaperInterface != null )
					mChangeWallpaperInterface.finish();
			}
		}
		else if( Online_wallpaper_from == ONLINE_WALLPAPER_FROM_BEAUTY_CENTER )
		{
			if( mInterface != null )//说明依附于我们桌面，回调桌面的接口
			{
				mInterface.enterBeautyCenter( null );
				if( mChangeWallpaperInterface != null )
					mChangeWallpaperInterface.finish();
			}
			else
			{
				ComponentName mComponentName = new ComponentName( BEAUTY_CENTER_PACKAGE_NAME , BEAUTY_CENTER_CLASS_NAME );
				if( Tools.isApkInstalled( mContext , mComponentName ) == false )
				{
					if( mChangeWallpaperInterface != null )
					{
						if( mChangeWallpaperInterface.isFinishing() )//因为下方的dialog依附于activity，若activity正在做要结束的动画的话就不要启动dialog了
						{
							return;
						}
					}
					DownloadDialog.downloadApkCooeeDialog(
							mContainerContext ,
							mContext.getResources().getString( R.string.overview_panel_button_beauty_center_string ) ,
							BEAUTY_CENTER_PACKAGE_NAME ,
							true );
					if( SWITCH_ENABLE_UMENG )
					{
						MobclickAgent.onEvent( mContainerContext , UmengStatistics.one_key_change_wallpapper_download_peesonal_center );
					}
				}
				else
				{
					Intent mIntent = new Intent();
					mIntent.setComponent( mComponentName );
					Bundle bundle = new Bundle(); // 创建Bundle对象
					bundle.putString( "launcherPackageName" , mContext.getPackageName() );
					bundle.putBoolean( "disableSetWallpaperDimensions" , WallpaperManagerBase.get_disableSetWallpaperDimensions() );
					bundle.putBoolean( "enableShowWidget" , false );
					bundle.putBoolean( "enableShowTheme" , false );
					bundle.putString( "customWallpaperPath" , CUSTOM_WALLPAPER_PATH );//xiatian add	//德盛伟业需求：本地化默认壁纸路径。客户可配置的桌面壁纸路径，如"/system/wallpapers"，再在该路径下放置客户的壁纸图片。配置为空则显示"\assets\launcher\wallpapers"中的壁纸。
					mIntent.putExtras( bundle );
					mContext.startActivity( mIntent );
					if( mChangeWallpaperInterface != null )
						mChangeWallpaperInterface.finish();
				}
			}
		}
	}
	
	public void closeFolder()
	{
		if( mInterface != null )
		{
			mInterface.closeFolder();
		}
	}
	
	public static void setWallpaperInterface(
			IWallpaperCallbacks intance )
	{
		mInterface = intance;
	}
	
	public static Context getContainerContext()
	{
		return mContainerContext;
	}
	
	public static void setContainerContext(
			Context mContainerContext )
	{
		ChangeWallpaperManager.mContainerContext = mContainerContext;
	}
}
