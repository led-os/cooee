package com.iLoong.launcher.MList;


import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;


public class Utility
{
	
	public static void setListViewHeightBasedOnChildren(
			ListView listView ,
			int j )
	{
		Long Time1 , Time2;
		ListAdapter listAdapter = listView.getAdapter();
		if( listAdapter == null )
		{
			// pre-condition
			return;
		}
		int totalHeight = 0;
		// int total = 0;
		Time1 = System.currentTimeMillis();
		// for (int i = 0; i < listAdapter.getCount(); i++) {
		// View listItem = listAdapter.getView(i, null, listView);
		// listItem.measure(0, 0);
		// // total = listItem.getMeasuredHeight();
		// totalHeight += listItem.getMeasuredHeight();
		// }
		if( listAdapter.getCount() != 0 )
		{
			View listItem = listAdapter.getView( 0 , null , listView );
			listItem.measure( 0 , 0 );
			totalHeight = listItem.getMeasuredHeight() * j;
		}
		else
		{
			totalHeight = 0;
		}
		Time2 = System.currentTimeMillis();
		Log.e( "Time3" , ( Time2 - Time1 ) + "" );
		// totalHeight = total*j;
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + ( listView.getDividerHeight() * ( listAdapter.getCount() - 1 ) );
		listView.setLayoutParams( params );
	}
	
	public static int setListViewHeightBasedOnChildren1(
			ListView listView )
	{
		ListAdapter listAdapter = listView.getAdapter();
		if( listAdapter == null )
		{
			// pre-condition
			return 0;
		}
		int totalHeight = 0;
		for( int i = 0 ; i < listAdapter.getCount() ; i++ )
		{
			View listItem = listAdapter.getView( i , null , listView );
			listItem.measure( 0 , 0 );
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + ( listView.getDividerHeight() * ( listAdapter.getCount() - 1 ) );
		// listView.setLayoutParams(params);
		return totalHeight;
	}
}
