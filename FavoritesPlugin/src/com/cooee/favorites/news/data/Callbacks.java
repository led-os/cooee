package com.cooee.favorites.news.data;


import java.util.ArrayList;


public interface Callbacks
{
	
	public final int SUCCESS = 0;
	public final int FAIL = 1;
	public final int NO_UPDATE_NEWS = 2;
	public final int NO_MORE_NEWS = 3;
	public final int STOP = 4;
	
	public void NewsStartRefreshing(
			String categoryId );
	
	public void NewsRefreshEnd(
			String categoryId ,
			int state );
	
	public void NewsFetchEnd(
			String categoryId ,
			int state );
	
	public void onNativeAdShow(
			NewsItem item );
	
	public void NetworkError(
			String categoryId );
	
	public void updateCountryName(
			String name );
	
	public void updateCategroy(
			int state ,
			ArrayList<CategoryItem> categoryList );
	
	public void notifyCountryChanged(
			String countryName ,
			String countryCode );
}
