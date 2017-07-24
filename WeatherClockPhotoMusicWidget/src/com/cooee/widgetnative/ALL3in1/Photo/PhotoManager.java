package com.cooee.widgetnative.ALL3in1.Photo;


import java.io.InputStream;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;

import com.cooee.widgetnative.ALL3in1.R;
import com.cooee.widgetnative.ALL3in1.base.WidgetManager;
import com.cooee.widgetnative.ALL3in1.base.WidgetProvider;
import com.cooee.widgetnative.ALL3in1.Photo.activity.PickPicDialogActivity;
import com.cooee.widgetnative.ALL3in1.Photo.utils.BitmapUtils;


public class PhotoManager
{
	
	private float WIDGET_WIDTH = 110;
	private float WIDGET_HEIGHT = 110;
	private static PhotoManager mPhotoManager = null;
	public static final String TAG = "PhotoManager";
	/**当前显示的图片的Uri，为空则说明当前显示的为默认图片*/
	private Uri mCurImageUri = null;
	/**上一张图片的uri*/
	private Uri mLastImageUri = null;
	/**单张图片默认返回的选中图片Uri字段*/
	public final static String PICTURE_URI = "picture_uri";
	/**记录图片目录的SharedPreferences*/
	private SharedPreferences photoPreferences = null;
	/**是否正在更换图片*/
	private boolean isChanging = false;
	/**是否正在打开activity界面*/
	public boolean isOpenActivity = false;
	public Bitmap photoBitmap = null;
	public Bitmap lastBitmap;
	private Context mContext = null;
	/**图片文件夹监听者*/
	private ImageFileObserver mImageObserver = null;
	private boolean isShowChooseAlbum = true;
	
	public static PhotoManager getInstance(
			Context context )
	{
		if( mPhotoManager == null && context != null )
		{
			synchronized( TAG )
			{
				if( mPhotoManager == null && context != null )
				{
					mPhotoManager = new PhotoManager( context );
				}
			}
		}
		return mPhotoManager;
	}
	
	private PhotoManager(
			Context context )
	{
		//初始化配置
		initConfig( context );
		//初始化view
		initRemoteViews( context );
		loadLastBitmap( context );
	}
	
	private void initConfig(
			Context context )
	{
		if( mContext == null )
		{
			mContext = context;
			photoBitmap = BitmapFactory.decodeResource( mContext.getResources() , R.drawable.widget_photo_bg );
			WIDGET_WIDTH = mContext.getResources().getDimensionPixelSize( R.dimen.photo_width_min );
			WIDGET_HEIGHT = mContext.getResources().getDimensionPixelSize( R.dimen.photo_height_min );
			isShowChooseAlbum = mContext.getResources().getBoolean( R.bool.isShowChooseAlbum );
		}
	}
	
	public void initRemoteViews(
			Context context )
	{
		RemoteViews mRemoteViews = WidgetManager.getInstance( context ).getRemoteViews();
		if( mRemoteViews != null )
		{
			Intent intentClick = new Intent( WidgetProvider.CLICK_PHOTO_LAYOUT );
			PendingIntent pendingIntent = PendingIntent.getBroadcast( context , 0 , intentClick , 0 );
			mRemoteViews.setOnClickPendingIntent( R.id.photo_show_num1_up , pendingIntent );
		}
	}
	
	public void loadLastBitmap(
			Context context )
	{
		//从SharedPreferences中获取选中的文件夹路径
		photoPreferences = context.getSharedPreferences( "uri_photo" , 0 );
		String picPath = photoPreferences.getString( PICTURE_URI , null );
		if( picPath != null )
		{
			Uri picUri = Uri.parse( picPath );
			modifyPicListByPicture( picUri );
			changeImgBitmapByUri();
		}
	}
	
	public boolean isShowChooseAlbum()
	{
		return isShowChooseAlbum;
	}
	
	public boolean ifShowViewImg()
	{
		boolean ifShowViewImg = false;
		ifShowViewImg = mCurImageUri != null;
		return ifShowViewImg;
	}
	
	public Uri getCurUri()
	{
		return mCurImageUri;
	}
	
	/***********************切换图片 start*****************************/
	public void setChangeImageIntent(
			Intent intent )
	{
		if( intent != null )
		{
			int requestCode = intent.getIntExtra( PickPicDialogActivity.INTENT_REQUEST , PickPicDialogActivity.ALBUM );
			switch( requestCode )
			{
				case PickPicDialogActivity.PICTURE:
					setNewSinglePic( intent , mContext );
					break;
				default:
					break;
			}
			Log.d( "" , " cyk changeImmediately intent: " + intent );
			boolean isChanged = changeImgBitmapByUri();
			if( isChanged )
			{
				//设置图片
				WidgetManager.getInstance( mContext ).updateAppWidget();
			}
		}
	}
	
	/**
	 * 设置新的单张图片
	 * @param intent
	 * @author yangtianyu 2016-4-11
	 */
	private void setNewSinglePic(
			Intent intent ,
			Context context )
	{
		String picturePath = intent.getStringExtra( PICTURE_URI );
		saveUri( picturePath , PICTURE_URI , context );
		Uri picUri = Uri.parse( picturePath );
		modifyPicListByPicture( picUri );
	}
	
	/**
	 * 保存Uri信息,桌面重启时使用,移除插件后无效
	 * 只保存专辑或单张图片中的一个字段
	 * @param uri 专辑或图片的Uri信息
	 * @param key 专辑或图片的字段名
	 * @author yangtianyu 2016-4-11
	 */
	private void saveUri(
			String uri ,
			String key ,
			Context context )
	{
		photoPreferences = context.getSharedPreferences( "uri_photo" , Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE );
		SharedPreferences.Editor editor = photoPreferences.edit();
		if( PICTURE_URI.equals( key ) )
		{
			editor.putString( PICTURE_URI , uri );
		}
		editor.commit();
	}
	
	/**
	 * 清空相册文件夹记录
	 * @author yangtianyu 2016-3-16
	 */
	private void clearSharedPreferences(
			Context mContext )
	{
		photoPreferences = mContext.getSharedPreferences( "uri_photo" , Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE );
		SharedPreferences.Editor editor = photoPreferences.edit();
		editor.clear();
		editor.commit();
	}
	
	/**
	 * 根据单张图片Uri来更改图片Uri数组,并增加文件监听
	 * @param picUri 图片Uri
	 * @author yangtianyu 2016-4-11
	 */
	private void modifyPicListByPicture(
			Uri picUri )
	{
		mCurImageUri = picUri;
		addFileObserver( uriToPath( picUri ) );
	}
	
	private String uriToPath(
			Uri picUri )
	{
		long picId = ContentUris.parseId( picUri );
		final String[] projectionPhotos = { MediaStore.Images.Media.DATA };
		Cursor cursor = MediaStore.Images.Media.query(
				mContext.getContentResolver() ,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI ,
				projectionPhotos ,
				MediaStore.Images.Media._ID + "=" + picId ,
				null ,
				MediaStore.Images.Media.DATE_TAKEN + " DESC" );
		if( cursor != null && cursor.moveToFirst() )
		{
			return cursor.getString( 0 );
		}
		return null;
	}
	
	/**
	 * 通过uri获取bitmap
	 * @return 如果uri改变,则返回true.否则返回false
	 */
	private synchronized boolean changeImgBitmapByUri()
	{
		isChanging = true;
		Bitmap newImage = null;
		// 展示相册图片
		Log.d( "" , "cyk mCurImageUri: " + mCurImageUri );
		//
		//
		// 当前显示的图片与目录中的这张图片不同
		Log.d( "" , "cyk mLastImageUri: " + mLastImageUri + " mCurImageUri: " + mCurImageUri );
		if( mCurImageUri == null )
		{
			mCurImageUri = null;
			mLastImageUri = null;
			newImage = photoBitmap;
		}
		else if( mCurImageUri != null && !mCurImageUri.equals( mLastImageUri ) )
		{
			// YANGTIANYU@2016/04/08 UPD START
			//newImage = getBitmapFromFile( image_dir_name + "/" + nextImagePath , (int)width , (int)height );
			newImage = getBitmapFromFile( mCurImageUri , (int)WIDGET_WIDTH , (int)WIDGET_HEIGHT );
			// YANGTIANYU@2016/04/08 UPD END
			if( newImage != null )
			{
				mLastImageUri = mCurImageUri;
			}
		}
		Log.d( "" , "cyk newImage: " + newImage );
		if( newImage != null )
		{
			Log.d( TAG , "cyk getCompositeBitmap " );
			//释放上次显示的图片
			if( lastBitmap != null && !lastBitmap.isRecycled() && lastBitmap != photoBitmap )
			{
				lastBitmap.recycle();
			}
			lastBitmap = getCompositeBitmap( newImage , WIDGET_WIDTH , WIDGET_HEIGHT );
			isChanging = false;
			return true;
		}
		isChanging = false;
		return false;
	}
	
	/***********************切换图片 end*****************************/
	/***********************改变图片的Runnable*****************************/
	private Bitmap getCompositeBitmap(
			Bitmap newImage ,
			float width ,
			float height )
	{
		if( newImage == null )
		{
			return photoBitmap;
		}
		Bitmap minbmp = newImage;
		if( newImage.getWidth() != width || newImage.getHeight() != height )
		{
			minbmp = ThumbnailUtils.extractThumbnail( newImage , (int)width , (int)height );
		}
		if( minbmp != null )
		{
			Bitmap dstBitmap = BitmapUtils.adaptive( photoBitmap , (int)WIDGET_WIDTH , (int)WIDGET_HEIGHT );
			Bitmap roundedCornerBitmap = BitmapUtils.onCompositeImages( minbmp , dstBitmap , PorterDuff.Mode.DST_IN );
			if( !minbmp.isRecycled() && !minbmp.equals( photoBitmap ) )
			{
				minbmp.recycle();
			}
			if( !newImage.isRecycled() && !newImage.equals( photoBitmap ) )//新换的图不是默认图,才去释放
			{
				newImage.recycle();
			}
			if( !dstBitmap.isRecycled() && !dstBitmap.equals( photoBitmap ) )
			{
				dstBitmap.recycle();
			}
			return roundedCornerBitmap;
		}
		return photoBitmap;
	}
	
	private Bitmap getBitmapFromFile(
			Uri uri ,
			int width ,
			int height )
	{
		Bitmap bmp = null;
		if( null != uri )
		{
			BitmapFactory.Options opts = null;
			InputStream is = null;
			InputStream is2 = null;
			if( width > 0 && height > 0 )
			{
				opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				try
				{
					is = mContext.getContentResolver().openInputStream( uri );
					is2 = mContext.getContentResolver().openInputStream( uri );
					BitmapFactory.decodeStream( is , null , opts );
					is.close();
				}
				catch( Exception e )
				{
					e.printStackTrace();
					mCurImageUri = null;
				}
				opts.inSampleSize = calculateInSampleSize( opts , 480 , 800 );
				opts.inJustDecodeBounds = false;
				opts.inInputShareable = true;
				opts.inPurgeable = true;
			}
			try
			{
				bmp = BitmapFactory.decodeStream( is2 , null , opts );
				if( is2 != null )
				{
					is2.close();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
				mCurImageUri = null;
			}
		}
		return bmp;
	}
	
	private int calculateInSampleSize(
			BitmapFactory.Options options ,
			int reqWidth ,
			int reqHeight )
	{
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if( height > reqHeight || width > reqWidth )
		{
			final int heightRatio = Math.round( (float)height / (float)reqHeight );
			final int widthRatio = Math.round( (float)width / (float)reqWidth );
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
	
	/***********************改变图片的Runnable end*****************************/
	/***********************文件监听 start*****************************/
	class ImageFileObserver extends FileObserver
	{
		
		public ImageFileObserver(
				String path )
		{
			super( path );
		}
		
		@Override
		public void onEvent(
				int event ,
				String path )
		{
			final int action = event & FileObserver.ALL_EVENTS;
			//单张图片,做了任何操作Uri都不同了,直接清除
			switch( action )
			{
				case FileObserver.DELETE:
				case FileObserver.MODIFY:
				case FileObserver.DELETE_SELF:
					if( mCurImageUri != null )
					{
						mCurImageUri = null;
						clearSharedPreferences( mContext );
					}
					boolean isChanged = changeImgBitmapByUri();
					Log.i( TAG , "cyk ImageFileObserver isChanged: " + isChanged );
					if( isChanged )
					{
						//设置图片
						WidgetManager.getInstance( mContext ).updateAppWidget();
					}
					break;
			}
		}
	}
	
	/**
	 * 增加文件监听
	 * @param fileOrDir 单张图片文件或文件夹的路径
	 * @author yangtianyu 2016-4-11
	 */
	private void addFileObserver(
			String fileOrDir )
	{
		Log.i( TAG , "cyk fileOrDir: " + fileOrDir );
		if( fileOrDir == null )
		{
			mCurImageUri = null;
			mLastImageUri = null;
			return;
		}
		if( mImageObserver != null )
		{
			mImageObserver.stopWatching();
			mImageObserver = null;
		}
		mImageObserver = new ImageFileObserver( fileOrDir );
		mImageObserver.startWatching();
	}
	
	/***********************文件监听 end*****************************/
	public synchronized void onclick()
	{
		Log.d( TAG , "cyk PhotoManager onclick isChanging: " + isChanging + " isOpenActivity: " + isOpenActivity );
		if( !isChanging && !isOpenActivity )
		{
			isOpenActivity = true;
			Intent dialogActivity = new Intent( mContext , PickPicDialogActivity.class );
			dialogActivity.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
			mContext.startActivity( dialogActivity );
		}
	}
	
	public void setLastBitmap()
	{
		if( lastBitmap != null && !lastBitmap.isRecycled() )
		{
			RemoteViews mRemoteViews = WidgetManager.getInstance( mContext ).getRemoteViews();
			mRemoteViews.setImageViewBitmap( R.id.photo_show_num1_up , lastBitmap );
		}
	}
}
