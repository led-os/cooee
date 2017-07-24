package com.cooee.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class BaseAdapter<T> extends android.widget.BaseAdapter
{
	
	protected List<T> mList = new ArrayList<T>();
	protected Activity mActivity = null;;
	protected LayoutInflater mInflater = null;
	
	public BaseAdapter(
			Activity context )
	{
		this.mActivity = context;
		this.mInflater = mActivity.getLayoutInflater();
	}
	
	@Override
	public int getCount()
	{
		if( mList != null )
			return mList.size();
		else
			return 0;
	}
	
	@Override
	public Object getItem(
			int position )
	{
		return mList == null ? null : mList.get( position );
	}
	
	@Override
	public long getItemId(
			int position )
	{
		return position;
	}
	
	/**
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	@Override
	public abstract View getView(
			int position ,
			View convertView ,
			ViewGroup parent );
	
	public void setList(
			List<T> list )
	{
		setListNoRefresh( list );
		notifyDataSetChanged();
	}
	
	public void setList(
			T[] list )
	{
		setList( Arrays.asList( list ) );
	}
	
	public void setListNoRefresh(
			List<T> list )
	{
		this.mList.clear();
		if( list != null )
		{
			this.mList.addAll( list );
		}
	}
	
	public List<T> getList()
	{
		return mList;
	}
	
	public void releaseResource()
	{
		mList = null;
		mActivity = null;
		mInflater = null;
	}
}
