package com.cooee.favorites.news;


import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;


public class ViewPagerAdapter extends PagerAdapter
{
	
	ArrayList<PullToRefreshLayout> viewLists;
	private NewsViewPager mViewPager;
	
	public ViewPagerAdapter(
			NewsViewPager pager ,
			ArrayList<PullToRefreshLayout> newsLists )
	{
		mViewPager = pager;
		viewLists = newsLists;
	}
	
	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub  
		return viewLists.size();
	}
	
	@Override
	public boolean isViewFromObject(
			View arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub  
		return arg0 == arg1;
	}
	
	@Override
	public void destroyItem(
			View view ,
			int position ,
			Object object )
	{
		Log.v( "zjp" , "destroyItem position = " + position );
		( (ViewPager)view ).removeView( (View)object );
	}
	
	@Override
	public Object instantiateItem(
			View view ,
			int position )
	{
		Log.v( "zjp" , "instantiateItem position = " + position );
		if( viewLists.get( position ).getParent() == null )
			( (ViewPager)view ).addView( viewLists.get( position ) , 0 );
		return viewLists.get( position );
	}
	
	@Override
	public int getItemPosition(
			Object object )
	{
		// TODO Auto-generated method stub
		if( mViewPager.getCurrentPage() != object )
		{
			return POSITION_NONE;
		}
		else
		{
			return POSITION_UNCHANGED;
		}
	}
}
