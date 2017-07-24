package com.cooee.phenix.camera.entity;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.cooee.framework.app.BaseAppState;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.camera.CameraView;
import com.cooee.phenix.camera.R;
import com.cooee.phenix.camera.control.CameraControl;
import com.cooee.phenix.camera.utils.BitmapUtils;


// CameraPage
public class PictureListViewAdapter extends android.widget.BaseAdapter implements AbsListView.OnScrollListener , SectionIndexer
{
	
	protected List<PictureInfo> mList = new LinkedList<PictureInfo>();
	protected Activity mContext;
	protected int scrollState;
	private LayoutInflater layoutInflater = null;
	private int itemFontSize = 15;
	private int entryGalleryFontSize = 20;
	private int itemFontColor = Color.WHITE;
	private int entryGalleryFontColor = Color.WHITE;
	
	public PictureListViewAdapter(
			Activity context )
	{
		this.mContext = context;
		this.layoutInflater = context.getLayoutInflater();
	}
	
	@Override
	public int getCount()
	{
		if( mList == null || mList.size() == 0 )
		{
			int mDefaultPerviewItemNum = 2;
			//xiatain add start	//添加配置项“camera_page_default_perview_item_num”，相机页默认预览图的个数。可配置为1或2。
			mDefaultPerviewItemNum = CameraView.configUtils.getInteger( "camera_page_default_perview_item_num" , 2 );
			if(
			//
			( mDefaultPerviewItemNum < 1 )
			//
			|| ( mDefaultPerviewItemNum > 2 )
			//
			)
			{
				mDefaultPerviewItemNum = 2;
			}
			//xiatain add end
			return mDefaultPerviewItemNum;
		}
		else if( mList.size() < CameraControl.SHOW_PICTURE_COUNT )
			return mList.size();
		else
			return( CameraControl.SHOW_PICTURE_COUNT + 1 );
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
	
	public void setList(
			final List<PictureInfo> list )
	{
		if( list != null )
		{
			// YANGTIANYU@2016/09/20 DEL START
			// 【i_0014483】【i_0014486】对数据链表的改变放入UI线程
			//this.mList.clear();
			//this.mList.addAll( list );
			// YANGTIANYU@2016/09/20 DEL END
			mContext.runOnUiThread( new Runnable() {
				
				@Override
				public void run()
				{
					// YANGTIANYU@2016/09/20 ADD START
					// 【i_0014483】【i_0014486】对数据链表的改变放入UI线程
					mList.clear();
					mList.addAll( list );
					// YANGTIANYU@2016/09/20 ADD END
					notifyDataSetChanged();
				}
			} );
		}
	}
	
	public void setListNoRefresh(
			List<PictureInfo> list )
	{
		this.mList = list;
	}
	
	public List<PictureInfo> getList()
	{
		return mList;
	}
	
	public void setList(
			PictureInfo[] list )
	{
		setList( Arrays.asList( list ) );
		notifyDataSetChanged();
	}
	
	public void showDetail(
			int position ,
			View view )
	{
		// To change body of created methods use File | Settings | File
		// Templates.
	}
	
	public void setScrollState(
			int scrollState )
	{
		this.scrollState = scrollState;
	}
	
	@Override
	public void onScrollStateChanged(
			AbsListView view ,
			int scrollState )
	{
		this.setScrollState( scrollState );
	}
	
	@Override
	public void onScroll(
			AbsListView view ,
			int firstVisibleItem ,
			int visibleItemCount ,
			int totalItemCount )
	{
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}
	
	public Object[] getSections()
	{
		return null;
	}
	
	public int getPositionForSection(
			int section )
	{
		return -1;
	}
	
	public int getSectionForPosition(
			int section )
	{
		return -1;
	}
	
	public void addItem(
			final PictureInfo info ,
			final ListView listView )
	{
		// YANGTIANYU@2016/09/20 DEL START
		// 【i_0014483】【i_0014486】对数据链表的改变放入UI线程
		//this.mList.add( 0 , info );
		//if( this.mList.size() >= CameraControl.SHOW_PICTURE_COUNT )
		//{
		//	for( int i = this.mList.size() - 1 ; i >= CameraControl.SHOW_PICTURE_COUNT ; i-- )
		//	{
		//		PictureInfo old = this.mList.get( i );
		//		if( old != null )
		//			BitmapUtils.recycleBitmapDrawable( old.getDrawable() );
		//	}
		//}
		// YANGTIANYU@2016/09/20 DEL END
		mContext.runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				// YANGTIANYU@2016/09/21 ADD START
				// 【i_0014483】【i_0014486】对数据链表的改变放入UI线程
				mList.add( 0 , info );
				if( mList.size() >= CameraControl.SHOW_PICTURE_COUNT )
				{
					for( int i = mList.size() - 1 ; i >= CameraControl.SHOW_PICTURE_COUNT ; i-- )
					{
						PictureInfo old = mList.get( i );
						if( old != null )
							BitmapUtils.recycleBitmapDrawable( old.getDrawable() );
					}
				}
				// YANGTIANYU@2016/09/21 ADD END
				notifyDataSetChanged();
				//
				// YANGTIANYU@2016/07/13 UPD START
				//listView.setSelection( 0 );
				listView.setSelection( listView.getChildCount() > 1 ? 1 : 0 );
				listView.smoothScrollToPosition( 0 );
				// YANGTIANYU@2016/07/13 UPD END
			}
		} );
	}
	
	public void deleteItem(
			final String path )
	{
		// 【i_0014483】【i_0014486】对数据链表的改变放入UI线程
		mContext.runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				if( !TextUtils.isEmpty( path ) && mList != null && mList.size() > 0 )
				{
					for( int i = 0 ; i < mList.size() ; i++ )
					{
						PictureInfo old = mList.get( i );
						if( old.getPicturePath().equals( path ) )
						{
							if( old != null )
								BitmapUtils.recycleBitmapDrawable( old.getDrawable() );
							//
							mList.remove( i );
							//
							notifyDataSetChanged();
							//
							break;
						}
					}
				}
			}
		} );
	}
	
	public void releaseResource()
	{
		// 【i_0014483】【i_0014486】对数据链表的改变放入UI线程
		mContext.runOnUiThread( new Runnable() {
			
			@Override
			public void run()
			{
				for( int i = 0 ; i < mList.size() ; i++ )
				{
					BitmapDrawable drawable = mList.get( i ).getDrawable();
					if( drawable != null )
					{
						Bitmap bitmap = drawable.getBitmap();
						if( bitmap != null && !bitmap.isRecycled() )
							bitmap.recycle();
					}
				}
				mList.clear();
				notifyDataSetChanged();
			}
		} );
	}
	
	public void setItemFontSize(
			int itemFontSize )
	{
		this.itemFontSize = itemFontSize;
	}
	
	public void setEntryGalleryFontColor(
			int entryGalleryFontColor )
	{
		this.entryGalleryFontColor = entryGalleryFontColor;
	}
	
	public void setEntryGalleryFontSize(
			int entryGalleryFontSize )
	{
		this.entryGalleryFontSize = entryGalleryFontSize;
	}
	
	public void setItemFontColor(
			int itemFontColor )
	{
		this.itemFontColor = itemFontColor;
	}
	
	public View getView(
			int position ,
			View convertView ,
			ViewGroup parent )
	{
		ViewHolder viewHolder;
		if( convertView == null )
		{
			convertView = layoutInflater.inflate( R.layout.camera_page_view_pictures_list_item_layout , parent , false );
			viewHolder = new ViewHolder();
			viewHolder.dateTextView = (TextView)convertView.findViewById( R.id.camera_page_picure_time );
			viewHolder.pictureImageView = (ImageView)convertView.findViewById( R.id.camera_page_picture );
			viewHolder.dateTextView.setTextSize( this.itemFontSize );
			viewHolder.dateTextView.setTextColor( this.itemFontColor );
			convertView.setTag( viewHolder );
		}
		else
		{
			viewHolder = (ViewHolder)convertView.getTag();
			if( !( viewHolder instanceof ViewHolder ) )
			{
				convertView = layoutInflater.inflate( R.layout.camera_page_view_pictures_list_item_layout , parent , false );
				viewHolder = new ViewHolder();
				viewHolder.dateTextView = (TextView)convertView.findViewById( R.id.camera_page_picure_time );
				viewHolder.pictureImageView = (ImageView)convertView.findViewById( R.id.camera_page_picture );
				viewHolder.dateTextView.setTextSize( this.itemFontSize );
				viewHolder.dateTextView.setTextColor( this.itemFontColor );
				convertView.setTag( viewHolder );
			}
		}
		//chenliang add start	//解决“设置强制从右向左显示，相机页布局和广告显示不正常”的问题。【i_0014978】
		ImageView pictureBgImageView = (ImageView)convertView.findViewById( R.id.camera_page_picture_bg );
		int pictureListItemBgId = 0;
		if( BaseAppState.isLayoutRTL() )
		{
			pictureListItemBgId = R.drawable.camera_page_list_item_bg_rtl;
		}
		else
		{
			pictureListItemBgId = R.drawable.camera_page_list_item_bg;
		}
		pictureBgImageView.setBackgroundResource( pictureListItemBgId );
		//chenliang add end
		if( mList == null || mList.size() == 0 )
		{
			viewHolder.pictureImageView.setImageDrawable( null );
			if( position % 2 == 0 )
				viewHolder.pictureImageView.setBackgroundResource( R.drawable.camera_page_default_pic1 );
			else
				viewHolder.pictureImageView.setBackgroundResource( R.drawable.camera_page_default_pic2 );
			//
			viewHolder.dateTextView.setText( null );
		}
		else
		{
			if( position != CameraControl.SHOW_PICTURE_COUNT )
			{
				PictureInfo info = this.mList.get( position );
				// gaominghui@2016/12/14 DEL START
				//viewHolder.pictureImageView.setBackground( null );
				viewHolder.pictureImageView.setBackgroundDrawable( null );
				// gaominghui@2016/12/14 DEL END
				BitmapDrawable drawable = info.getDrawable();
				if( drawable == null || drawable.getBitmap() == null || drawable.getBitmap().isRecycled() )
				{
					CameraView.logI( StringUtils.concat( "adapter , getview , create drawable : " , info.getPicturePath() ) );
					drawable = BitmapUtils.getBitmapDrawableByPath( mContext , info.getPicturePath() , 2 , false );
					info.setDrawable( drawable );
				}
				else
				{
					CameraView.logI( "adapter , getview , get drawable" );
					drawable = info.getDrawable();
				}
				viewHolder.pictureImageView.setImageDrawable( drawable );
				viewHolder.dateTextView.setText( info.getPictureTime() );
			}
			else
			{
				convertView = layoutInflater.inflate( R.layout.camera_page_view_pictures_list_text_item_layout , parent , false );
				TextView textView = (TextView)convertView.findViewById( R.id.camera_page_text_view );
				textView.setTextSize( this.entryGalleryFontSize );
				textView.setTextColor( this.entryGalleryFontColor );
				textView.setText( R.string.camera_page_gallery_text );
			}
		}
		return convertView;
	}
	
	class ViewHolder
	{
		
		TextView dateTextView;
		ImageView pictureImageView;
	}
}
