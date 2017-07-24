package com.cooee.favorites.news;


import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.FavoritesPlugin;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.news.data.Callbacks;
import com.cooee.favorites.news.data.CategoryItem;
import com.cooee.favorites.news.data.NewsData;
import com.cooee.favorites.news.data.NewsItem;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.kmob.kmobsdk.KmobManager;
import com.kmob.kmobsdk.NativeAdData;
import com.umeng.analytics.MobclickAgent;


public class NewsModel implements Callbacks
{
	
	private NewsView mView;
	private NewsData mData;
	public static final String TAG = "news";
	
	public NewsModel(
			Context cxt ,
			RequestQueue queue ,
			HashMap<String , ArrayList<NewsItem>> list ,
			NewsView view )
	{
		mView = view;
		mData = new NewsData( cxt , queue , list , this );
	}
	
	public void newsRefresh(
			String categoryId )
	{
		try
		{
			if( mData != null )
			{
				String mFront = mData.getFrontRequestCategoryAndStop( categoryId );
				if( mFront != null )
				{
					mView.updateNews( mFront , STOP );
				}
				mData.refresh( categoryId );
			}
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void newsFetch(
			String categoryId )
	{
		try
		{
			if( mData != null )
				mData.fetch( categoryId );
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateCountry(
			String country )
	{
		if( mData != null )
		{
			mData.stopRefresh();//停止之前的刷新,否则连续点几次国家刷新的是最新点击的国家的新闻
			mData.updateCountry( country );
			clearAllData();//切换国家后，清除已有的数据(因为头条和hubii的刷新方式不一致)
		}
	}
	
	@Override
	public void NewsStartRefreshing(
			String categoryId )
	{
		// TODO Auto-generated method stub
		mView.refreshAnimation( categoryId , NewsView.START_REFRESH );
	}
	
	@Override
	public void NewsRefreshEnd(
			String categoryId ,
			int state )
	{
		// TODO Auto-generated method stub
		Log.v( "model" , "updateNews" );
		if( mView != null )
		{
			mView.updateNews( categoryId , state );
			mView.refreshAnimation( categoryId , NewsView.END_REFRESH );
		}
	}
	
	@Override
	public void NewsFetchEnd(
			String categoryId ,
			int state )
	{
		// TODO Auto-generated method stub
		if( mView != null )
		{
			mView.updateNews( categoryId , state );
			mView.endFetch( categoryId );
		}
	}
	
	@Override
	public void onNativeAdShow(
			NewsItem item )
	{
		// TODO Auto-generated method stub
		if( item != null )
		{
			JSONObject obj = item.getOtherInfo();
			try
			{
				if( obj != null )
				{
					KmobManager.onNativeAdShow( obj.getString( NativeAdData.ADPLACE_ID_TAG ) , obj.getString( NativeAdData.AD_ID_TAG ) );
					FavoritesConfig config = FavoritesManager.getInstance().getConfig();
					if( config != null && config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
					{
						MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "Ad_show" );
					}
					try
					{
						StatisticsExpandNew.onCustomEvent(
								FavoritesManager.getInstance().getContainerContext() ,
								"Ad_show" ,
								FavoritesPlugin.SN ,
								FavoritesPlugin.APPID ,
								CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName ,
								FavoritesPlugin.UPLOAD_VERSION + "" ,
								null );
					}
					catch( NoSuchMethodError e )
					{
						try
						{
							StatisticsExpandNew.onCustomEvent(
									FavoritesManager.getInstance().getContainerContext() ,
									"Ad_show" ,
									FavoritesPlugin.SN ,
									FavoritesPlugin.APPID ,
									CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
									FavoritesPlugin.PRODUCTTYPE ,
									FavoritesPlugin.PluginPackageName );
						}
						catch( NoSuchMethodError e1 )
						{
							StatisticsExpandNew.onCustomEvent( FavoritesManager.getInstance().getContainerContext() , "Ad_show" , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName );
						}
					}
				}
			}
			catch( JSONException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void NetworkError(
			String categoryId )
	{
		// TODO Auto-generated method stub
		if( mView != null )
			mView.showNetworkError( categoryId );
	}
	
	@Override
	public void updateCountryName(
			String name )
	{
		if( mView != null )
			mView.setTextName( name );
	}
	
	public void updateNewsSource(
			int source )
	{
		// TODO Auto-generated method stub
		String result = null;
		if( mData != null )
			result = mData.setNewsDataSource( source );
		if( result != null )
		{
			if( mView != null )
				mView.updateCountry( result );
		}
	}
	
	public void adplaceChanged(
			String place )
	{
		if( mData != null )
			mData.updateAdplace( place );
	}
	
	public JSONArray getCountryList()
	{
		if( mData != null )
		{
			return mData.getCountryList();
		}
		return null;
	}
	
	public void clickNews(
			NewsItem item )
	{
		if( mData != null )
		{
			mData.addClickItem( item );
		}
	}
	
	public void clearAllData()
	{
		if( mData != null )
		{
			mData.clearAllData();
		}
	}
	
	@Override
	public void updateCategroy(
			int state ,
			ArrayList<CategoryItem> categoryList )
	{
		// TODO Auto-generated method stub
		if( mView != null )
		{
			mView.updateCategory( state , categoryList );
		}
	}
	
	@Override
	public void notifyCountryChanged(
			String countryName ,
			String countryCode )
	{
		// TODO Auto-generated method stub
		if( mView != null )
			mView.NotifyCountryChanged( countryName , countryCode );
	}
	
	public void CheckCountryIfChanged()
	{
		if( mData != null )
		{
			mData.checkCountryIfChanged();
		}
	}
}
