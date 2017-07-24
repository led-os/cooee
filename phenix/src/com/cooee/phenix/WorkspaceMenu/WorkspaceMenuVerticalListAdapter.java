// xiatian add whole file //需求：拓展配置项“config_menu_click_style”，添加可配置项2。2为打开“竖直列表”样式的桌面菜单。
package com.cooee.phenix.WorkspaceMenu;


import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooee.phenix.R;


public class WorkspaceMenuVerticalListAdapter extends ArrayAdapter<WorkspaceMenuVerticalListItemInfo>
{
	
	private LayoutInflater mInflater;
	private ArrayList<WorkspaceMenuVerticalListItemInfo> mItemInfoList = new ArrayList<WorkspaceMenuVerticalListItemInfo>();
	
	public WorkspaceMenuVerticalListAdapter(
			Context context ,
			ArrayList<WorkspaceMenuVerticalListItemInfo> objects )
	{
		super( context , 0 , objects );
		mInflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		mItemInfoList = objects;
	}
	
	@Override
	public int getCount()
	{
		return mItemInfoList.size();
	}
	
	@Override
	public WorkspaceMenuVerticalListItemInfo getItem(
			int position )
	{
		if( position < 0 || position >= mItemInfoList.size() )
		{
			return null;
		}
		return super.getItem( position );
	}
	
	@Override
	public View getView(
			int position ,
			View convertView ,
			ViewGroup parent )
	{
		View view = null;
		WorkspaceMenuVerticalListItemInfo mListItemInfo = getItem( position );
		if( convertView == null )
		{
			view = mInflater.inflate( R.layout.workspace_menu_vertical_list_item , parent , false );
		}
		else
		{
			view = convertView;
		}
		view.setTag( mListItemInfo );
		//mIconView
		ImageView mIconView = (ImageView)view.findViewById( R.id.workspace_menu_vertical_list_item_icon_id );
		if( false )//【（WorkspaceMenuVerticalList）备注：待扩展（添加配置，可以不显示menu list item的图片）】
			mIconView.setVisibility( View.GONE );
		else
			mIconView.setImageResource( mListItemInfo.getIconResouceId() );
		//mTitleView
		TextView mTitleView = (TextView)view.findViewById( R.id.workspace_menu_vertical_list_item_title_id );
		mTitleView.setText( mListItemInfo.getTitleResouceId() );
		return view;
	}
}
