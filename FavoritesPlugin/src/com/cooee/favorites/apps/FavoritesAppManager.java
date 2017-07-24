package com.cooee.favorites.apps;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.cooee.favorites.R;
import com.cooee.favorites.data.AppInfo;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.utils.FavoritesConstants;
import com.cooee.uniex.wrap.IFavoritesGetData;


public class FavoritesAppManager
{
	
	private static FavoritesAppManager mInstance = null;
	private final String TAG_DEFAULT_FAVORITES = "default_favorites";
	private final String TAG_FAVORITE = "favorite";
	private static final int TRAFFIC_MAX_NUM = 50;
	/**
	 * 如果不为空  表明bitmap要自己维护  请自己释放常用应用图标内存
	 */
	private IFavoritesGetData mIFavoritesGetData = null;
	
	public static FavoritesAppManager getInstance()
	{
		if( mInstance == null )
		{
			synchronized( FavoritesAppManager.class )
			{
				if( mInstance == null )
				{
					mInstance = new FavoritesAppManager();
				}
			}
		}
		return mInstance;
	}
	
	/**
	 * @author hp 常用应用和常用联系人（新功能） hp@2015/09/26 ADD START
	 * @param allApps ArrayList<ApplicationInfo>
	 */
	public void loadDefaultFavoritesIfNecessary(
			CopyOnWriteArrayList<AppInfo> allApps )
	{
		Context mContext = FavoritesManager.getInstance().getContainerContext();
		SharedPreferences sp = mContext.getSharedPreferences( FavoritesConstants.FAVORITES_APPS_PREFS , Context.MODE_PRIVATE );
		if( !sp.getBoolean( FavoritesConstants.EMPTY_TABLE_APPS_CREATED , false ) )
		{
			loadDefaultFavorites( FavoritesManager.getInstance().getPluginContext() , allApps );
			sp.edit().putBoolean( FavoritesConstants.EMPTY_TABLE_APPS_CREATED , true ).commit();
		}
	}
	
	/**
	 * 获取默认的常用的应用
	 * @param mContext
	 * @param allApps
	 * @author hp 常用应用和常用联系人（新功能） hp@2015/09/26 ADD START 
	 */
	private void loadDefaultFavorites(
			Context mContext ,
			CopyOnWriteArrayList<AppInfo> allApps )
	{
		HashMap<String , Long> trafficMap = getTrafficInfos( mContext );
		ArrayList<String> names = new ArrayList<String>();
		String packageName;
		try
		{
			XmlResourceParser parser = FavoritesManager.getInstance().getPluginContext().getResources().getXml( R.xml.default_favorites );
			AttributeSet attrs = Xml.asAttributeSet( parser );
			beginDocument( parser , TAG_DEFAULT_FAVORITES );
			final int depth = parser.getDepth();
			int type;
			int favoriteCount = 0;
			while( ( ( type = parser.next() ) != XmlPullParser.END_TAG || parser.getDepth() > depth ) && type != XmlPullParser.END_DOCUMENT )
			{
				if( type != XmlPullParser.START_TAG )
				{
					continue;
				}
				final String name = parser.getName();
				TypedArray a = mContext.obtainStyledAttributes( attrs , R.styleable.Favorite );
				//
				if( TAG_FAVORITE.equals( name ) && favoriteCount < FavoritesAppData.DEFAULT_FAVORITE_NUM )
				{
					packageName = a.getString( R.styleable.Favorite_packageName );
					long num = 0;
					String pn = null;
					for( AppInfo app : allApps )
					{
						if( app.getComponentName().getPackageName().equals( packageName ) )
						{
							pn = app.getComponentName().getPackageName();
							num = ( FavoritesAppData.DEFAULT_FAVORITE_NUM - favoriteCount ) * 3;
							if( trafficMap.containsKey( pn ) )
							{
								num += trafficMap.get( pn );
								trafficMap.put( pn , num );
							}
							else
							{
								trafficMap.put( pn , num );
							}
							favoriteCount++;
							break;
						}
					}
				}
				a.recycle();
			}
			for( String key : trafficMap.keySet() )
			{
				names.add( key );
			}
			//
			Collections.sort( names , new TrafficComparator( trafficMap ) );
			//
			int num = 0;
			int count = 0;
			String pn = null;
			for( int i = 0 ; i < names.size() ; i++ )
			{
				if( count >= FavoritesAppData.DEFAULT_FAVORITE_NUM )
				{
					break;
				}
				for( AppInfo app : allApps )
				{
					pn = app.getComponentName().getPackageName();
					if( pn.equals( names.get( i ) ) )
					{
						num = ( FavoritesAppData.DEFAULT_FAVORITE_NUM - count ) * 2;
						app.launchTimes = num;
						FavoritesManager.getInstance().addFavoritesToDatabase( mContext , app );
						count++;
						break;
					}
				}
			}
		}
		catch( XmlPullParserException e )
		{
			e.printStackTrace();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public void updateFavoritesApps()
	{
		FavoritesAppData.saveFavoritesToDatabase( FavoritesManager.getInstance().getContainerContext() );
		bindFavoritesApps();
	}
	
	public void bindFavoritesApps()
	{
		// huwenhao@2016/03/25 UPD START
		// 如果放在异步线程，可能很晚才会更新bitmap，在更新之前会一直使用旧的bitmap，但是旧的bitmap在主题切换完成之后会回收掉，这样会导致绘制已回收的bitmap而重启
		//AsyncTask.execute( new Runnable() {
		//	
		//	@Override
		//	public void run()
		//	{
		//		FavoritesAppData.sort( FavoritesAppData.datas );
		//		Log.v( "lvjiangbin" , "bindFavoritesApps mIFavoritesGetData=" + mIFavoritesGetData );
		//		//如果要负一平主动获取图片 只要设置此不为空 即可
		//		if( mIFavoritesGetData != null )
		//		{
		//			for( int i = 0 ; i < FavoritesAppData.datas.size() ; i++ )
		//			{
		//				Log.v( "lvjiangbin" , "bindFavoritesApps 2  mIFavoritesGetData=" + mIFavoritesGetData );
		//				AppInfo appinfo = FavoritesAppData.datas.get( i );
		//				Bitmap bitmap = mIFavoritesGetData.getIcon( appinfo.getIntent() );
		//				Log.v( "lvjiangbin" , "bindFavoritesApps 2  bitmap=" + bitmap );
		//				appinfo.setIconBitmap( bitmap );
		//			}
		//		}
		//		FavoritesManager.getInstance().bindApp( FavoritesAppData.datas );
		//	}
		//} );
		FavoritesAppData.sort( FavoritesAppData.datas );
		if( mIFavoritesGetData == null )
		{
			FavoritesManager.getInstance().bindApp( FavoritesAppData.datas );
			FavoritesManager.getInstance().bindNearby();//cheyingkun add	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
		}
		else
		{
			AsyncTask.execute( new Runnable() {
				
				@Override
				public void run()
				{
					Log.v( "lvjiangbin" , "bindFavoritesApps mIFavoritesGetData=" + mIFavoritesGetData );
					//如果要负一平主动获取图片 只要设置此不为空 即可
					for( int i = 0 ; i < FavoritesAppData.datas.size() ; i++ )
					{
						//Log.v( "lvjiangbin" , "bindFavoritesApps 2  mIFavoritesGetData=" + mIFavoritesGetData );
						AppInfo appinfo = FavoritesAppData.datas.get( i );
						Intent intent = new Intent();
						intent.setComponent( appinfo.getComponentName() );
						Log.v( "lvjiangbin" , "bindFavoritesApps  " + intent );
						Bitmap bitmap = mIFavoritesGetData.getIcon( intent );
						Bitmap tempCopy = Bitmap.createBitmap( bitmap , 0 , 0 , bitmap.getWidth() , bitmap.getHeight() );
						//Log.v( "lvjiangbin" , "bindFavoritesApps 2  bitmap=" + bitmap );
						//Log.v( "lvjiangbin" , "bindFavoritesApps 2  tempCopy=" + tempCopy );
						if( appinfo.getIconBitmap() != null && !appinfo.getIconBitmap().isRecycled() )
						{
							appinfo.getIconBitmap().recycle();
						}
						if( bitmap != null && !bitmap.isRecycled() )
						{
							bitmap.recycle();
							bitmap = null;
						}
						appinfo.setIconBitmap( tempCopy );
					}
					FavoritesManager.getInstance().bindApp( FavoritesAppData.datas );
					FavoritesManager.getInstance().bindNearby();//cheyingkun add	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
				}
			} );
		}
		// huwenhao@2016/03/25 UPD END
	}
	
	/**
	 * 比较器（降序）
	 * @author hp 常用应用和常用联系人（新功能） hp@2015/09/26 ADD START 
	 *
	 */
	private class TrafficComparator implements Comparator<String>
	{
		
		private HashMap<String , Long> mMap;
		
		public TrafficComparator(
				HashMap<String , Long> map )
		{
			mMap = map;
		}
		
		@Override
		public int compare(
				String lhs ,
				String rhs )
		{
			if( mMap.get( lhs ) < mMap.get( rhs ) )
				return -1;
			if( mMap.get( lhs ) > mMap.get( rhs ) )
				return 1;
			return 0;
		}
	}
	
	/**
	 * 获取使用流量的应用信息
	 * @param context
	 * @return
	 * @author hp 常用应用和常用联系人（新功能） hp@2015/09/26 ADD START  
	 */
	private HashMap<String , Long> getTrafficInfos(
			Context context )
	{
		final PackageManager pm = context.getPackageManager();
		List<PackageInfo> packinfos = pm.getInstalledPackages( PackageManager.GET_PERMISSIONS );
		HashMap<String , Long> trafficMap = new HashMap<String , Long>();
		HashSet<Integer> uidSet = new HashSet<Integer>();
		ArrayList<String> names = new ArrayList<String>();
		for( PackageInfo packinfo : packinfos )
		{
			String[] permissions = packinfo.requestedPermissions;
			if( permissions != null && permissions.length > 0 )
			{
				for( String permission : permissions )
				{
					if( "android.permission.INTERNET".equals( permission ) )
					{
						int uid = packinfo.applicationInfo.uid;
						long num = TrafficStats.getUidRxBytes( uid );
						if( num > 150000 && !uidSet.contains( uid ) )//150000流量量
						{
							uidSet.add( uid );
							trafficMap.put( packinfo.packageName , num );
						}
						break;
					}
				}
			}
		}
		//
		for( String n : trafficMap.keySet() )
		{
			names.add( n );
		}
		Collections.sort( names , new TrafficComparator( trafficMap ) );
		//
		trafficMap.clear();
		for( int i = 0 ; i < names.size() ; i++ )
		{
			if( i >= TRAFFIC_MAX_NUM )
			{
				break;
			}
			trafficMap.put( names.get( i ) , (long)( TRAFFIC_MAX_NUM - i ) * 7 );
		}
		return trafficMap;
	}
	
	/**
	 * @param parser
	 * @param firstElementName
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @author hp 常用应用和常用联系人（新功能） hp@2015/09/26 ADD START  
	 */
	private void beginDocument(
			XmlPullParser parser ,
			String firstElementName ) throws XmlPullParserException , IOException
	{
		int type;
		while( ( type = parser.next() ) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT )
		{
			;
		}
		if( type != XmlPullParser.START_TAG )
		{
			throw new XmlPullParserException( "No start tag found" );
		}
		if( !parser.getName().equals( firstElementName ) )
		{
			throw new XmlPullParserException( "Unexpected start tag: found " + parser.getName() + ", expected " + firstElementName );
		}
	}
	
	public IFavoritesGetData getFavoritesGetDataCallBack()
	{
		return mIFavoritesGetData;
	}
	
	public void setFavoritesGetDataCallBack(
			IFavoritesGetData favoritesGetData )
	{
		mIFavoritesGetData = favoritesGetData;
	}
	
	//cheyingkun add start	//服务器关闭酷生活后，释放资源。
	public void clearFavoritesView()
	{
		if( mIFavoritesGetData != null )
		{
			for( int i = 0 ; i < FavoritesAppData.datas.size() ; i++ )
			{
				AppInfo appinfo = FavoritesAppData.datas.get( i );
				Bitmap bitmap = appinfo.getIconBitmap();
				if( bitmap != null && !bitmap.isRecycled() )
				{
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
	}
	//cheyingkun add end
}
