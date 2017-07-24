package com.search.kuso.adapter;


import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooee.search.R;

import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.manager.dl_info;
import cool.sdk.kuso.KuSoHelper;
import cool.sdk.kuso.KusoEngineInfo;


public class EngineAdapter extends BaseAdapter
{
	
	private List<KusoEngineInfo> list;
	private Context context;
	SharedPreferences sp;
	private int i;
	
	class ViewHolder
	{
		
		ImageView engineIcon;
		TextView engineName;
		ImageView radioimage;
	}
	
	public EngineAdapter(
			List<KusoEngineInfo> list ,
			Context context )
	{
		this.list = list;
		this.context = context;
		sp = context.getSharedPreferences( "sp_setting" , Context.MODE_PRIVATE );
	}
	
	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return list.size();
	}
	
	@Override
	public Object getItem(
			int position )
	{
		// TODO Auto-generated method stub
		return list.get( position );
	}
	
	@Override
	public long getItemId(
			int position )
	{
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public View getView(
			final int position ,
			View convertView ,
			ViewGroup parent )
	{
		// TODO Auto-generated method stub
		ViewHolder holder;
		if( convertView == null )
		{
			convertView = LayoutInflater.from( context ).inflate( R.layout.kuso_serach_engine_item , null );
			holder = new ViewHolder();
			holder.engineIcon = (ImageView)convertView.findViewById( R.id.kuso_engine_logo );
			holder.engineName = (TextView)convertView.findViewById( R.id.kuso_engine_name );
			holder.radioimage = (ImageView)convertView.findViewById( R.id.kuso_radio_engine_state );
			convertView.setTag( holder );
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}
		CoolDLMgr dlMgr = KuSoHelper.getInstance( context ).getCoolDLMgrIcon();
		dl_info dl = dlMgr.UrlGetInfo( list.get( position ).getR3() );
		if( dl != null && dl.IsDownloadSuccess() )
		{
			String path = dl.getFilePath();
			Log.i( "path" , path );
			Bitmap bit = BitmapFactory.decodeFile( path );
			holder.engineIcon.setImageBitmap( bit );
			holder.engineName.setText( list.get( position ).getR2() );
			if( position == sp.getInt( "k" , 0 ) )
			{
				list.get( position ).setR7( true );
			}
			if( list.get( position ).isR7() )
			{
				holder.radioimage.setImageResource( R.drawable.kuso_selected_icon );
			}
			else
			{
				holder.radioimage.setImageResource( R.drawable.kuso_unselected_icon );
			}
		}
		return convertView;
	}
}
