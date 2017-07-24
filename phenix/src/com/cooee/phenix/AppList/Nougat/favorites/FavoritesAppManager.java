package com.cooee.phenix.AppList.Nougat.favorites;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.net.TrafficStats;
import android.util.AttributeSet;
import android.util.Xml;

import com.cooee.framework.utils.ThreadUtil;
import com.cooee.phenix.R;
import com.cooee.phenix.AppList.Marshmallow.AlphabeticalAppsList;
import com.cooee.phenix.data.AppInfo;



public class FavoritesAppManager
{
	
	private static FavoritesAppManager mInstance = null;
	private final String TAG_DEFAULT_FAVORITES = "default_favorites";
	private final String TAG_FAVORITE = "favorite";
	private static final int TRAFFIC_MAX_NUM = 50;
	private FavoritesAppData appData = null;

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
	
	public FavoritesAppManager()
	{
		appData = new FavoritesAppData();
	}
	
	public FavoritesAppData getFavoritesAppData()
	{
		return appData;
	}
	/**
	 * @author hp 常用应用和常用联系人（新功能） hp@2015/09/26 ADD START
	 * @param allApps ArrayList<ApplicationInfo>
	 */
	public void loadDefaultFavoritesIfNecessary(
			Context mContext ,
			List<AppInfo> allApps )
	{
		SharedPreferences sp = mContext.getSharedPreferences( "favorites_apps" , Context.MODE_PRIVATE );
		if( !sp.getBoolean( "empty_table_apps_created" , false ) )
		{
			loadDefaultFavorites( mContext , allApps );
			sp.edit().putBoolean( "empty_table_apps_created" , true ).commit();
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
			List<AppInfo> allApps )
	{
		HashMap<String , Long> trafficMap = getTrafficInfos( mContext );
		ArrayList<String> names = new ArrayList<String>();
		String packageName;
		try
		{
			XmlResourceParser parser = mContext.getResources().getXml( R.xml.default_favorites );
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
			ArrayList<FavoritesComponentKey> datas = new ArrayList<FavoritesComponentKey>();
			for( int i = 0 ; i < names.size() ; i++ )
			{
				if( count >= FavoritesAppData.DEFAULT_FAVORITE_NUM )
				{
					break;
				}
				for( AppInfo app : allApps )
				{
					if( app.getComponentName().getPackageName().equals( names.get( i ) ) )
					{
						ComponentName comp = app.getComponentName();
						FavoritesComponentKey componentKey = new FavoritesComponentKey( comp );
						num = ( FavoritesAppData.DEFAULT_FAVORITE_NUM - count ) * 2;
						componentKey.launchTimes = num;
						count++;
						datas.add( componentKey );
						break;
					}
				}
			}
			FavoritesAppData.saveFavoritesToDatabase( mContext , datas );
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
	
	public void updateFavoritesApps(
			Context context )
	{
		if( appData != null )
		{
			appData.updateFavoritesApps( context );
		}
	}
	
	public void loadAndBindApps(
			final Context context ,
			final AlphabeticalAppsList apps )
	{
		ThreadUtil.execute( new Runnable() {

			public void run()
			{
				loadDefaultFavoritesIfNecessary( context , apps.getApps() );
				if( appData != null )
				{
					appData.setApps( apps );
					appData.addAppsData( appData.getFavoritesDatas( context ) );
				}
				updateFavoritesApps( context );
			}
		} );
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
	
	public void updateTimes(
			ComponentName name )
	{
		if( appData != null )
			appData.updateTimes( name );
	}
	
	public AppInfo getApplicationInfoFromAll(
			ComponentName name )
	{
		if( appData != null )
		{
			return appData.getApplicationInfoFromAll( name );
		}
		return null;
	}
	
	public boolean isNewAdd()
	{
		if( appData != null )
		{
			return appData.isNewAdd();
		}
		return false;
	}
	
	public void setNewAdd(
			boolean isNewAdd )
	{
		if( appData != null )
		{
			appData.setNewAdd( isNewAdd );
		}
	}
	
	public boolean isUpdate()
	{
		if( appData != null )
		{
			return appData.isUpdate();
		}
		return false;
	}
	
	public void setUpdate(
			boolean isUpdate )
	{
		if( appData != null )
		{
			appData.setUpdate( isUpdate );
		}
	}
	
	public void dayDecrease()
	{
		if( appData != null )
		{
			appData.dayDecrease();
		}
	}
	
	public boolean isHaveCategoryLauncher(
			ComponentName componentName )
	{
		if( appData != null )
		{
			AppInfo info = appData.getApplicationInfoFromAll( componentName );
			if( info != null )
			{
				return true;
			}
		}
		return false;
	}
}
