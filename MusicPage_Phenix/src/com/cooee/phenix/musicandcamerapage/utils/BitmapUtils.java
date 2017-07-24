package com.cooee.phenix.musicandcamerapage.utils;


// MusicPage CameraPage
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.musicpage.R;


public class BitmapUtils
{
	
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
				Bitmap artBitmap = null;
				if( bm != null )
				{
					if( bm.getConfig() == null )
					{
						artBitmap = bm.copy( Bitmap.Config.RGB_565 , false );
						bm.recycle();
					}
				}
				return artBitmap;
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
	
	public static void saveBitmap(
			Bitmap bitmap ,
			String name )
	{
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.e( "tianyu" , "保存图片" );
		File f = new File( android.os.Environment.getExternalStorageDirectory() , name );
		if( f.exists() )
		{
			f.delete();
		}
		try
		{
			FileOutputStream out = new FileOutputStream( f );
			bitmap.compress( Bitmap.CompressFormat.PNG , 90 , out );
			out.flush();
			out.close();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "tianyu" , "已经保存" );
		}
		catch( FileNotFoundException e )
		{
			e.printStackTrace();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "tianyu" , StringUtils.concat( "e0 = " , e.toString() ) );
		}
		catch( IOException e )
		{
			e.printStackTrace();
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "tianyu" , StringUtils.concat( "e1 = " , e.toString() ) );
		}
	}
	
	public static Bitmap mask(
			Context context ,
			Bitmap album ,
			boolean recycle )
	{
		if( album == null )
			return null;
		Bitmap mask = BitmapFactory.decodeResource( context.getResources() , R.drawable.music_page_default_mask );
		Bitmap scaledbmp = Bitmap.createScaledBitmap( album , mask.getWidth() , mask.getHeight() , true );
		if( album != null && !album.isRecycled() )
			album.recycle();
		album = scaledbmp;
		Canvas canvas = new Canvas( album );
		Paint paint = new Paint();
		paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.DST_OUT ) );
		canvas.drawBitmap( mask , 0 , 0 , paint );
		paint.setXfermode( null );
		Bitmap bmp = album.copy( Config.ARGB_8888 , true );
		if( recycle )
		{
			if( mask != null && !mask.isRecycled() )
				mask.recycle();
			if( album != null && !album.isRecycled() )
				album.recycle();
		}
		return bmp;
	}
	
	public static Bitmap[] combineBitmap(
			Context context ,
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
			bitmaps = new Bitmap[2];
			Paint paint = new Paint();
			paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.DST_OVER ) );
			paint.setColor( Color.BLACK );
			paint.setStyle( Paint.Style.FILL );
			//*******************************第一张图片********************************************//
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
			float albumBitmapStartX = ( turntables.getWidth() - albumBitmap.getWidth() ) / 2F;
			float albumBitmapStartY = ( turntables.getHeight() - albumBitmap.getHeight() ) / 2F;
			Bitmap bitmapUp = Bitmap.createBitmap( turntables.getWidth() , turntables.getHeight() , Config.ARGB_8888 );
			Canvas canvas = new Canvas( bitmapUp );
			canvas.drawBitmap( albumBitmap , albumBitmapStartX , albumBitmapStartY , paint );
			canvas.drawBitmap( turntables , 0 , 0 , paint );
			canvas.save();
			bitmaps[0] = bitmapUp;
			//*******************************第二张点击变暗的图片********************************************//
			// gaominghui@2016/07/09 ADD START 画一张黑色图片作为底，再把画笔设置透明度，封面画上去
			Bitmap blackAlumbBitmap = Bitmap.createBitmap( albumBitmap.getWidth() , albumBitmap.getHeight() , Config.ARGB_8888 );
			Canvas blackCanvas = new Canvas( blackAlumbBitmap );
			Paint blackPaint = new Paint();
			blackPaint.setColor( Color.BLACK );
			blackPaint.setStyle( Paint.Style.FILL );
			blackCanvas.drawRect( 0 , 0 , albumBitmap.getWidth() , albumBitmap.getHeight() , blackPaint );
			blackPaint.setAlpha( (int)( 255 * albumBitmapDownAlpha ) );
			blackCanvas.drawBitmap( albumBitmap , 0 , 0 , blackPaint );
			blackCanvas.save();
			// gaominghui@2016/07/09 ADD END 画一张黑色图片作为底，再把画笔设置透明度，封面画上去
			blackAlumbBitmap = mask( context , blackAlumbBitmap , recycle ); //mask这张图片
			if( blackAlumbBitmap == null )
				return null;
			if( !blackAlumbBitmap.isMutable() )
			{
				Bitmap tmp = blackAlumbBitmap.copy( Config.ARGB_8888 , true );
				blackAlumbBitmap.recycle();
				blackAlumbBitmap = tmp;
			}
			if( !turntables.isMutable() )
			{
				Bitmap tmp = turntables.copy( Config.ARGB_8888 , true );
				turntables.recycle();
				turntables = tmp;
			}
			Bitmap bitmapDown = Bitmap.createBitmap( turntables.getWidth() , turntables.getHeight() , Config.ARGB_8888 );
			Canvas canvasDown = new Canvas( bitmapDown );
			canvasDown.drawBitmap( blackAlumbBitmap , albumBitmapStartX , albumBitmapStartY , paint );
			canvasDown.drawBitmap( turntables , 0 , 0 , paint );
			paint.setXfermode( null );
			canvas.save();
			bitmaps[1] = bitmapDown;
		}
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
		bm.compress( Bitmap.CompressFormat.PNG , 100 , baos );
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
	
	@SuppressWarnings( "deprecation" )
	public static BitmapDrawable getBitmapDrawableByPath(
			String path ,
			int SampleSize )
	{
		Options options = null;
		if( SampleSize != 1 )
		{
			options = new Options();
			options.inSampleSize = 2;//图片宽高都为原来的二分之一，即图片为原来的四分之一   
		}
		BitmapDrawable drawable = null;
		if( path != null && !"".equals( path ) && !"null".equals( path ) )
		{
			Bitmap bitmap = BitmapFactory.decodeFile( path , options );
			drawable = new BitmapDrawable( bitmap );
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
	
	public static Bitmap[] maskAdBitmap(
			Bitmap adBmp ,
			Bitmap defaultTurntableBitmap ,
			Bitmap defaultAlumbBitmap )
	{
		if( adBmp == null || defaultTurntableBitmap == null || defaultAlumbBitmap == null )//gaominghui add  //解决“音乐页广告展示完后，调节时间后，切页至音乐页，桌面重启”的问题【i_0014944】
			return null;
		Bitmap orig = adBmp;
		Bitmap[] adBitmaps = new Bitmap[2];
		adBmp = Bitmap.createScaledBitmap( orig , defaultTurntableBitmap.getWidth() , defaultTurntableBitmap.getHeight() , true );
		if( adBmp != orig )
			orig.recycle();
		Bitmap adUpBitmap = defaultTurntableBitmap.copy( Config.ARGB_8888 , true );
		Canvas canvasUp = new Canvas( adUpBitmap );
		Paint paint = new Paint();
		paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.SRC_IN ) );
		canvasUp.drawBitmap( adBmp , 0 , 0 , paint );
		paint.setXfermode( null );
		float albumBitmapStartX = ( adUpBitmap.getWidth() - defaultAlumbBitmap.getWidth() ) / 2F;
		float albumBitmapStartY = ( adUpBitmap.getHeight() - defaultAlumbBitmap.getHeight() ) / 2F;
		canvasUp.drawBitmap( defaultAlumbBitmap , albumBitmapStartX , albumBitmapStartY , paint );
		canvasUp.save();
		adBitmaps[0] = adUpBitmap;
		////////////////////////////////////////////////////////////
		Bitmap adDownBitmap = defaultTurntableBitmap.copy( Config.ARGB_8888 , true );
		Canvas canvasDown = new Canvas( adDownBitmap );
		paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.SRC_IN ) );
		canvasDown.drawBitmap( adBmp , 0 , 0 , paint );
		paint.setXfermode( null );
		Bitmap alumbBitmap = maskDefaultAlumbBitmap( defaultAlumbBitmap );
		if( !alumbBitmap.isMutable() )
		{
			Bitmap tmp = alumbBitmap.copy( Config.ARGB_8888 , true );
			alumbBitmap.recycle();
			alumbBitmap = tmp;
		}
		canvasDown.drawBitmap( alumbBitmap , albumBitmapStartX , albumBitmapStartY , paint );
		canvasDown.save();
		adBitmaps[1] = adDownBitmap;
		adBmp.recycle();
		alumbBitmap.recycle();
		return adBitmaps;
	}
	
	/**
	 *该方法是为了创建mask一张默认封面的那张红点点的图片
	 * @param bitmap
	 * @return
	 * @author gaominghui 2016年7月15日
	 */
	public static Bitmap maskDefaultAlumbBitmap(
			Bitmap bitmap )
	{
		Bitmap maskBitmap = Bitmap.createBitmap( bitmap.getWidth() , bitmap.getHeight() , Config.ARGB_8888 );
		Canvas canvas = new Canvas( maskBitmap );
		Paint paint = new Paint();
		paint.setColor( Color.BLACK );
		paint.setStyle( Paint.Style.FILL );
		canvas.drawRect( 0 , 0 , bitmap.getWidth() , bitmap.getHeight() , paint );
		paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.DST_IN ) );
		canvas.drawBitmap( bitmap , 0 , 0 , paint );
		paint.setXfermode( null );
		paint.setAlpha( (int)( 255 * 0.5F ) );
		canvas.drawBitmap( bitmap , 0 , 0 , paint );
		canvas.save();
		return maskBitmap;
	}
}
