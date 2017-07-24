package com.cooee.phenix.camera.utils;


// MusicPage CameraPage
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextPaint;
import android.text.format.DateFormat;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.camera.CameraView;
import com.cooee.phenix.camera.R;


public class BitmapUtils
{
	
	/**保存照片时，照片尺寸需要增加的高,以便显示背景,单位dp*/
	private final static int PICTURE_HEIGHT_ADDITION = 42;
	/**保存的照片背景*/
	private static Drawable mPictureBgDrawable = null;
	/**保存的照片中的文字画笔*/
	private static TextPaint mPhotoPaint;
	
	public static Drawable getAssetsImageToDrawable(
			String path )
	{
		Drawable bg = null;
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( path );
		try
		{
			if( inputStream != null )
			{
				bg = Drawable.createFromStream( inputStream , null );
				inputStream.close();
				inputStream = null;
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return bg;
	}
	
	public static Bitmap getArtwork(
			Context context ,
			long song_id ,
			long album_id )
	{
		if( album_id < 0 )
		{
			if( song_id >= 0 )
			{
				Bitmap bm = getArtworkFromFile( context , song_id , -1 );
				if( bm != null )
				{
					return bm;
				}
			}
			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId( Uri.parse( "content://media/external/audio/albumart" ) , album_id );
		if( uri != null )
		{
			InputStream in = null;
			try
			{
				in = res.openInputStream( uri );
				Bitmap bmp = BitmapFactory.decodeStream( in , null , new BitmapFactory.Options() );
				return bmp;
			}
			catch( FileNotFoundException ex )
			{
				Bitmap bm = getArtworkFromFile( context , song_id , album_id );
				if( bm != null )
				{
					if( bm.getConfig() == null )
					{
						bm = bm.copy( Bitmap.Config.RGB_565 , false );
					}
				}
				return bm;
			}
			finally
			{
				try
				{
					if( in != null )
					{
						in.close();
					}
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static Bitmap getArtworkFromFile(
			Context context ,
			long songid ,
			long albumid )
	{
		Bitmap bm = null;
		if( albumid < 0 && songid < 0 )
		{
			throw new IllegalArgumentException( "Must specify an album or a song id" );
		}
		try
		{
			if( albumid < 0 )
			{
				Uri uri = Uri.parse( StringUtils.concat( "content://media/external/audio/media/" , songid , "/albumart" ) );
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor( uri , "r" );
				if( pfd != null )
				{
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor( fd );
				}
			}
			else
			{
				Uri uri = ContentUris.withAppendedId( Uri.parse( "content://media/external/audio/albumart" ) , albumid );
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor( uri , "r" );
				if( pfd != null )
				{
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor( fd );
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return bm;
	}
	
	public static Bitmap[] combineBitmap(
			Bitmap albumBitmap ,
			Bitmap turntables ,
			float albumBitmapDownAlpha ,
			boolean recycle )
	{
		Bitmap[] bitmaps = null;
		if( albumBitmap == null || albumBitmap.isRecycled() || turntables == null || turntables.isRecycled() || albumBitmapDownAlpha < 0 || albumBitmapDownAlpha > 1 )
		{
			bitmaps = null;
		}
		else
		{
			//
			bitmaps = new Bitmap[2];
			Paint paint = new Paint();
			paint.setColor( Color.BLACK );
			paint.setStyle( Paint.Style.FILL );
			//
			if( !albumBitmap.isMutable() )
			{
				Bitmap tmp = albumBitmap.copy( Config.ARGB_8888 , true );
				albumBitmap.recycle();
				albumBitmap = tmp;
			}
			if( !turntables.isMutable() )
			{
				Bitmap tmp = turntables.copy( Config.ARGB_8888 , true );
				turntables.recycle();
				turntables = tmp;
			}
			//
			float albumBitmapStartX = ( turntables.getWidth() - albumBitmap.getWidth() ) / 2F;
			float albumBitmapStartY = ( turntables.getHeight() - albumBitmap.getHeight() ) / 2F;
			//
			Bitmap bitmapUp = Bitmap.createBitmap( turntables.getWidth() , turntables.getHeight() , Config.ARGB_8888 );
			Canvas canvas = new Canvas( bitmapUp );
			canvas.drawBitmap( albumBitmap , albumBitmapStartX , albumBitmapStartY , paint );
			canvas.drawBitmap( turntables , 0 , 0 , paint );
			canvas.save();
			bitmaps[0] = bitmapUp;
			//
			Bitmap bitmapDown = Bitmap.createBitmap( turntables.getWidth() , turntables.getHeight() , Config.ARGB_8888 );
			canvas = new Canvas( bitmapDown );
			canvas.drawRect( albumBitmapStartX , albumBitmapStartY , albumBitmap.getWidth() + albumBitmapStartX , albumBitmap.getHeight() + albumBitmapStartY , paint );
			paint.setAlpha( (int)( 255 * albumBitmapDownAlpha ) );
			canvas.drawBitmap( albumBitmap , albumBitmapStartX , albumBitmapStartY , paint );
			paint.setAlpha( 255 );
			canvas.drawBitmap( turntables , 0 , 0 , paint );
			canvas.save();
			bitmaps[1] = bitmapDown;
		}
		//
		if( recycle )
		{
			if( albumBitmap != null && !albumBitmap.isRecycled() )
				albumBitmap.recycle();
			if( turntables != null && !turntables.isRecycled() )
				turntables.recycle();
		}
		//
		return bitmaps;
	}
	
	public static Bitmap rotateBitmap(
			Bitmap bitmap ,
			float angle ,
			float targetWidth ,
			float targetHeight ,
			boolean shouldRecycle )
	{
		if( bitmap == null )
		{
			return null;
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		//
		Matrix mtx = new Matrix();
		mtx.postRotate( angle );
		if( angle == -90F )
			mtx.postScale( -1 , 1 );
		if( w != targetHeight || h != targetWidth )
		{
			float widthScale = targetWidth / h;
			float heightScale = targetHeight / w;
			mtx.postScale( widthScale , heightScale );
		}
		Bitmap newBitmap = Bitmap.createBitmap( bitmap , 0 , 0 , w , h , mtx , true );
		if( shouldRecycle )
		{
			bitmap.recycle();
		}
		return newBitmap;
	}
	
	public static byte[] Bitmap2Bytes(
			Bitmap bm ,
			boolean shouldRecycle )
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress( Bitmap.CompressFormat.JPEG , 100 , baos );
		byte[] bytes = baos.toByteArray();
		try
		{
			baos.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return bytes;
	}
	
	/**
	 * 初始化照片背景
	 */
	private static void initPictureBg(
			Context context )
	{
		if( mPictureBgDrawable != null )
		{
			return;
		}
		try
		{
			XmlResourceParser localXmlResourceParser = context.getResources().getXml( R.xml.camera_page_ninebg );
			mPictureBgDrawable = Drawable.createFromXml( context.getResources() , localXmlResourceParser );
			return;
		}
		catch( Exception exception )
		{
			exception.printStackTrace();
		}
	}
	
	/**
	 * 初始化日期画笔
	 **/
	private static void initPaint()
	{
		if( mPhotoPaint == null )
		{
			mPhotoPaint = new TextPaint();
			mPhotoPaint.setStyle( Paint.Style.FILL );
			mPhotoPaint.setColor( Color.argb( 0xff , 0xb1 , 0xb1 , 0xb1 ) );
			mPhotoPaint.setDither( true );
			mPhotoPaint.setAntiAlias( true );
		}
	}
	
	/**
	 * 获取文件名所对应的日期
	 * @param strDate 文件名
	 * @return 文件名所对应的日期,格式为yyyy.MM.dd
	 * @author yangtianyu 2016-6-27
	 */
	private static CharSequence getPhotoDate(
			String strDate )
	{
		if( strDate == null || "".equals( strDate ) )
			return "";
		SimpleDateFormat format = new SimpleDateFormat( "yyyyMMddHHmmss" );
		strDate = strDate.replace( "IMG" , "" ).replace( ".jpg" , "" );
		Date date = null;
		try
		{
			date = format.parse( strDate );
		}
		catch( ParseException e )
		{
			e.printStackTrace();
			return "";
		}
		return DateFormat.format( "yyyy.MM.dd" , date.getTime() );
	}
	
	/**
	 * 将一张无多余内容的照片图片与背景、日期和描述文字等内容合成一张图片
	 * 然后将其保存到指定文件路径的文件中
	 * 保存照片的逻辑如果要进行修改,需要同步修改获取照片的方法 
	 * @see getBitmapDrawableByPath
	 * @param context 上下文
	 * @param photoBmp 照片图片
	 * @param photoText 描述文字
	 * @param photoPath 照片绝对路径（包含文件夹路径）
	 * @param recycle 是否回收照片图片
	 * @return 保存后的文件对象,保存不成功则返回null
	 * @author yangtianyu 2016-7-28
	 */
	public static File savePhoto(
			Context context ,
			Bitmap photoBmp ,
			String photoText ,
			String photoPath ,
			boolean recycle )
	{
		if( context == null || photoPath == null || photoPath == null || "".equals( photoPath ) )
			return null;
		File file = new File( photoPath );
		FileOutputStream stream = null;
		int width = photoBmp.getWidth();
		int height = photoBmp.getHeight();
		//带背景的宽高,创建图片(宽度 = 照片的宽 + 边距 * 2)
		int picW = (int)( width + context.getResources().getDimension( R.dimen.camera_page_picture_margin_bg_left_and_right ) * 2 );
		int picH = (int)( height + ViewUtils.dp2px( context , PICTURE_HEIGHT_ADDITION ) );
		final Bitmap newBitmap = Bitmap.createBitmap( picW , picH , Bitmap.Config.ARGB_8888 );
		Canvas canvas = new Canvas( newBitmap );
		initPictureBg( context );
		int fgLeft = ( picW - width ) / 2;
		//将图片画到背景前面
		// 设置背景
		if( mPictureBgDrawable != null )
		{
			mPictureBgDrawable.setBounds( 0 , 0 , picW , picH );
			// 将图片画到Canvas上
			mPictureBgDrawable.draw( canvas );
		}
		canvas.save();
		// 左上方偏移 fgLeft量后开始绘制
		canvas.drawBitmap( photoBmp , fgLeft , fgLeft , null );
		// 获取文件名中的日期（即拍照时的日期）
		CharSequence date = getPhotoDate( file.getName() );
		//初始化pait
		initPaint();
		int size = (int)context.getResources().getDimension( R.dimen.camera_page_picture_text_size );
		mPhotoPaint.setTextSize( size );
		int textX = (int)( picW - mPhotoPaint.measureText( date.toString() ) - fgLeft );
		int textY = picH - (int)context.getResources().getDimension( R.dimen.camera_page_text_margin_bg_bottom );
		canvas.drawText( date.toString() , textX , textY , mPhotoPaint );
		if( !"".equals( photoText ) && photoText != null )
		{
			canvas.drawText( photoText , fgLeft , textY , mPhotoPaint );
		}
		canvas.restore();
		// 写入文件
		try
		{
			stream = new FileOutputStream( file );
			stream.write( BitmapUtils.Bitmap2Bytes( newBitmap , true ) );
			stream.flush();
			// 释放资源
			if( photoBmp != null && !photoBmp.isRecycled() && recycle )
				photoBmp.recycle();
		}
		catch( IOException e )
		{
			e.printStackTrace();
			//
			if( file != null && file.exists() )
				file.delete();
			return null;
		}
		finally
		{
			if( stream != null )
				try
				{
					stream.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
		}
		return file;
	}
	
	/**
	 * 从图片中截取照片部分内容（图片为相机页保存的带背景与拍摄时间等信息的照片）
	 * @param context 上下文
	 * @param bitmap 含有背景与拍摄时间等信息的照片图片
	 * @param scale 图片在取出后的缩放比（未缩放则为1）
	 * @param recycle 是否回收原图
	 * @return 截取后的照片图片
	 * @author yangtianyu 2016-7-28
	 */
	public static Bitmap cropPhotoFromBmp(
			Context context ,
			Bitmap bitmap ,
			int scale ,
			boolean recycle )
	{
		if( bitmap == null )
			return null;
		int width = (int)context.getResources().getDimension( R.dimen.camera_page_camera_preview_layout_width ) / scale;
		int height = (int)context.getResources().getDimension( R.dimen.camera_page_camera_preview_layout_height ) / scale;
		int fgLeft = ( bitmap.getWidth() - width ) / 2;
		fgLeft = fgLeft > 0 ? fgLeft : 0;
		Bitmap sourceBitmap = bitmap;
		//cheyingkun add start	//解决“打开相机页，桌面重启”的问题
		int bitmapWidth = sourceBitmap.getWidth();
		int bitmapHeight = sourceBitmap.getHeight();
		if( width > bitmapWidth || height > bitmapHeight )
		{
			width = bitmapWidth;
			height = bitmapHeight;
		}
		//cheyingkun add end
		bitmap = Bitmap.createBitmap( sourceBitmap , fgLeft , fgLeft , width , height );
		if( bitmap != sourceBitmap && !sourceBitmap.isRecycled() && recycle )
		{
			sourceBitmap.recycle();
		}
		return bitmap;
	}
	
	/**
	 * 根据图片路径获取照片drawable,可以传入缩放的比例,也可以去除照片中的白色背景
	 * 如果照片合成方式发生变化,需要同步更改本方法中去除背景的逻辑 
	 * @see savePhoto
	 * @param context 上下文
	 * @param path 要获取的照片的路径
	 * @param SampleSize 获取照片后需要缩放的比例,长宽都以该比例进行缩放,如SampleSize为2,宽高都会变为原图的二分之一
	 * @param isWithBg 是否需要保留照片背景,true为保留,false则不保留
	 * @return 路径所对应图片经过处理后的drawable
	 * @author yangtianyu 2016-7-12
	 */
	public static BitmapDrawable getBitmapDrawableByPath(
			Context context ,
			String path ,
			int SampleSize ,
			boolean isWithBg )
	{
		Options options = null;
		if( SampleSize != 1 )
		{
			options = new Options();
			options.inSampleSize = SampleSize;//图片宽高都为原来的二分之一，即图片为原来的四分之一   
		}
		BitmapDrawable drawable = null;
		if( path != null && !"".equals( path ) && !"null".equals( path ) )
		{
			Bitmap bitmap = BitmapFactory.decodeFile( path , options );
			if( !isWithBg && context != null )
			{
				bitmap = cropPhotoFromBmp( context , bitmap , SampleSize , true );
			}
			drawable = new BitmapDrawable( context.getResources() , bitmap );
		}
		return drawable;
	}
	
	public static void recycleBitmapDrawable(
			Drawable drawable )
	{
		if( drawable != null && drawable instanceof BitmapDrawable )
		{
			Bitmap bitmap = ( (BitmapDrawable)drawable ).getBitmap();
			if( bitmap != null && !bitmap.isRecycled() )
				bitmap.recycle();
		}
	}
	
	/**
	 * 根据给定的bitmap和大小生成一个可以铺满size，但是不改变比例的图片
	 * 
	 * @param bitmap
	 *            位图
	 * @param newSize
	 *            新的大小
	 * @param recycle
	 *            是否回收
	 * @return
	 */
	public static Bitmap resizeBitmap(
			Bitmap bitmap ,
			int[] newSize ,
			boolean recycle )
	{
		if( bitmap == null )
		{
			return null;
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if( ( newSize[0] == w ) && ( newSize[1] == h ) )
		{
			return bitmap;
		}
		float ratio = Math.max( 1.0f * newSize[0] / w , 1.0f * newSize[1] / h );
		// 新的大小
		int k = Math.round( ratio * bitmap.getWidth() );
		int l = Math.round( ratio * bitmap.getHeight() );
		// 创建一个新的位图
		Bitmap newBitmap = Bitmap.createScaledBitmap( bitmap , k , l , true );
		if( recycle && newBitmap != bitmap )
		{
			bitmap.recycle();
		}
		return newBitmap;
	}
	
	/**
	 * 旋转图片
	 * 
	 * @param bitmap 位图
	 * @param angle 旋转的角度
	 * @param isMirror 是否需要镜像翻转
	 * @param recycle 是否释放原有位图
	 * @return
	 */
	public static Bitmap rotateBitmap(
			Bitmap bitmap ,
			float angle ,
			boolean isMirror ,
			boolean recycle )
	{
		if( bitmap == null )
		{
			CameraView.logI( "rotateBitmap bitmap == null" );
			return null;
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		//
		Matrix mtx = new Matrix();
		mtx.postRotate( angle );
		if( isMirror )
			mtx.postScale( -1 , 1 );
		Bitmap bitmap2 = Bitmap.createBitmap( bitmap , 0 , 0 , w , h , mtx , true );
		//释放已有位图
		if( recycle )
		{
			bitmap.recycle();
		}
		return bitmap2;
	}
	
	/**
	 * 获得bitmap的配置
	 * 
	 * @param bitmap
	 *            位图
	 * @return 位图配置
	 */
	public static Bitmap.Config getConfig(
			Bitmap bitmap )
	{
		if( bitmap == null )
		{
			return Bitmap.Config.ARGB_8888;
		}
		return bitmap.getConfig();
	}
	
	/**
	 * 根据给定的bitmap和大小生成一个截取给定大小的图片,该图片的区域在给定bitmap中居中
	 * 
	 * @param bitmap
	 *            位图
	 * @param newSize
	 *            新的大小
	 * @param recycle
	 *            是否回收
	 * @return
	 */
	public static Bitmap cropCenter(
			Bitmap bitmap ,
			int[] newSize ,
			boolean recycle )
	{
		if( bitmap == null )
		{
			return null;
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if( ( newSize[0] == w ) && ( newSize[1] == h ) )
		{
			return bitmap;
		}
		// 创建一个新的位图
		Bitmap newBitmap = Bitmap.createBitmap( newSize[0] , newSize[1] , getConfig( bitmap ) );
		// 新的大小
		Canvas canvas = new Canvas( newBitmap );
		// 位移
		canvas.translate( ( newSize[0] - w ) / 2.0F , ( newSize[1] - h ) / 2.0F );
		canvas.drawBitmap( bitmap , 0.0F , 0.0F , new Paint( 6 ) );
		if( recycle )
		{
			bitmap.recycle();
		}
		return newBitmap;
	}
}
