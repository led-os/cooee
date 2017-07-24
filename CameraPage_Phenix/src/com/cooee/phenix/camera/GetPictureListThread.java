package com.cooee.phenix.camera;


// CameraPage
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.camera.control.CameraControl;
import com.cooee.phenix.camera.entity.PictureInfo;
import com.cooee.phenix.camera.utils.BitmapUtils;


public class GetPictureListThread extends Thread
{
	
	private String photoSavaPath = null;
	private CallBack callBack = null;
	//
	private SimpleDateFormat format = null;
	private Context mContext = null;
	
	@SuppressLint( "SimpleDateFormat" )
	public GetPictureListThread(
			Context context ,
			String photoSavaPath ,
			CallBack callBack )
	{
		this.photoSavaPath = photoSavaPath;
		this.callBack = callBack;
		this.mContext = context;
		//
		format = new SimpleDateFormat( "yyyyMMddHHmmss" );
	}
	
	@SuppressWarnings( "deprecation" )
	@Override
	public void run()
	{
		super.run();
		//
		// YANGTIANYU@2016/07/20 ADD START
		// 临时保存一个只存储路径与日期信息的图片数组用于排序
		// 图片在排序后取出,避免资源浪费和大量图片造成的OOM
		List<PictureInfo> tmpList = new ArrayList<PictureInfo>();
		// YANGTIANYU@2016/07/20 ADD END
		List<PictureInfo> list = new ArrayList<PictureInfo>();
		if( !TextUtils.isEmpty( photoSavaPath ) )
		{
			File saveDir = new File( photoSavaPath );
			if( saveDir.exists() )
			{
				File[] files = saveDir.listFiles();
				if( files != null && files.length > 0 )
				{
					for( File file : files )
					{
						if( file.exists() && file.isFile() )
						{
							try
							{
								String fileName = file.getName();
								fileName = fileName.replace( "IMG" , "" ).replace( ".jpg" , "" );
								Date date = format.parse( fileName );
								//
								int month = date.getMonth() + 1;
								int day = date.getDate();
								String dat = String.format(
										CameraControl.dateFormat ,
										date.getYear() + 1900 ,
										month < 10 ? StringUtils.concat( "0" , month ) : month ,
										day < 10 ? StringUtils.concat( "0" , day ) : day );
								int hours = date.getHours();
								int minutes = date.getMinutes();
								String time = String.format(
										CameraControl.timeFormat ,
										hours < 10 ? StringUtils.concat( "0" , hours ) : hours ,
										minutes < 10 ? StringUtils.concat( "0" , minutes ) : minutes );
								//
								PictureInfo info = new PictureInfo( dat , time , date.getDay() , file.getAbsolutePath() , date.getTime() );
								tmpList.add( info );
							}
							catch( Exception e )
							{
								e.printStackTrace();
							}
						}
					}
					if( tmpList != null && tmpList.size() != 0 )
					{
						Collections.sort( tmpList , new Comparator<PictureInfo>() {
							
							@Override
							public int compare(
									PictureInfo lhs ,
									PictureInfo rhs )
							{
								if( !( lhs instanceof PictureInfo && rhs instanceof PictureInfo ) )
								{
									return 0;
								}
								if( lhs.getTimeMillis() - rhs.getTimeMillis() > 0 )
									return -1;
								return 1;
							}
						} );
						// YANGTIANYU@2016/07/20 ADD START
						// 排序完成后依次取数组中的项并获取其对应的图片
						// 到达显示上限后停止获取数据
						PictureInfo pictureInfo = null;
						for( int i = 0 ; i < tmpList.size() ; i++ )
						{
							pictureInfo = tmpList.get( i );
							pictureInfo.setDrawable( BitmapUtils.getBitmapDrawableByPath( mContext , pictureInfo.getPicturePath() , 2 , false ) );
							list.add( pictureInfo );
							if( list.size() >= CameraControl.SHOW_PICTURE_COUNT )
								break;
						}
						tmpList.clear();
						// YANGTIANYU@2016/07/20 ADD END
					}
					callBack( list );
				}
				else
					callBack( list );
			}
			else
				callBack( list );
		}
		else
			callBack( list );
	}
	
	private void callBack(
			List<PictureInfo> list )
	{
		if( this.callBack != null )
			this.callBack.loadPictureListCompleted( list );
	}
	
	public interface CallBack
	{
		
		public void loadPictureListCompleted(
				List<PictureInfo> list );
	}
}
