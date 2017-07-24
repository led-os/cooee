package com.cooee.phenix.editmode;


import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.editmode.EditModeEntity.onItemClickListener;
import com.cooee.phenix.editmode.item.EditModelItem;


public class EditModeItemAdapter extends RecyclerView.Adapter<EditModeItemAdapter.ItemViewHolder>
{
	
	private Context mContext;
	private ArrayList<EditModelItem> mItemList = null;
	private onItemClickListener mListener = null;
	
	public EditModeItemAdapter(
			Context context ,
			ArrayList<EditModelItem> list )
	{
		mContext = context;
		mItemList = list;
	}
	
	public class ItemViewHolder extends ViewHolder implements OnClickListener
	{
		
		TextView mTitle;
		ImageView mImageView;
		View rootView;
		View mParent;
		
		public ItemViewHolder(
				View itemView ,
				View parent )
		{
			super( itemView );
			// TODO Auto-generated constructor stub
			this.rootView = itemView;
			mParent = parent;
			mTitle = (TextView)itemView.findViewById( R.id.edit_entry_item_textView );
			mImageView = (ImageView)itemView.findViewById( R.id.edit_entry_item_imageView );
			rootView.setOnClickListener( this );
		}
		
		@Override
		public void onClick(
				View v )
		{
			// TODO Auto-generated method stub
			if( mListener != null )
			{
				mListener.onItemClick( v , getPosition() );
			}
		}
	}
	
	@Override
	public int getItemCount()
	{
		// TODO Auto-generated method stub
		return mItemList.size();
	}
	
	@Override
	public ItemViewHolder onCreateViewHolder(
			ViewGroup parent ,
			int arg1 )
	{
		// TODO Auto-generated method stub
		ItemViewHolder holder = new ItemViewHolder( LayoutInflater.from( mContext ).inflate( R.layout.edit_entry_item_layout , parent , false ) , parent );
		return holder;
	}
	
	@Override
	public void onBindViewHolder(
			ItemViewHolder holder ,
			int postion )
	{
		// TODO Auto-generated method stub
		EditModelItem item = mItemList.get( postion );
		if( !TextUtils.isEmpty( item.getTitle() ) )
		{
			holder.mTitle.setText( item.getTitle() );
			holder.mTitle.setVisibility( View.VISIBLE );
		}
		else
		{
			holder.mTitle.setVisibility( View.INVISIBLE );
		}
		Bitmap bmp = item.getBitmap();
		if( bmp != null && !bmp.isRecycled() )
		{
			holder.mImageView.setImageBitmap( bmp );
		}
		holder.rootView.setSelected( item.isSelected() );
		if( holder.mParent != null )
		{
			String tag = ( holder.mParent ).getTag().toString();
			int paddingLeft = 0;
			int paddingRight = 0;
			if( EditModeEntity.TAB_THEME_KEY.equals( tag ) )
			{
				if( postion == 0 )
				{
					paddingLeft = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_theme_item_padding_horizon );
					paddingRight = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_theme_item_gap );
				}
				else if( postion == getItemCount() - 1 )
				{
					paddingRight = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_theme_item_padding_horizon );
					paddingLeft = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_theme_item_gap );
				}
				else
				{
					paddingLeft = paddingRight = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_theme_item_gap );
				}
				int imagePadding = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_theme_item_imageview_padding );
				holder.mImageView.setPadding( imagePadding , imagePadding , imagePadding , imagePadding );
			}
			else
			{
				if( postion == 0 )
				{
					paddingLeft = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_wallpaper_item_padding_horizon );
					paddingRight = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_wallpaper_item_gap );
				}
				else if( postion == getItemCount() - 1 )
				{
					paddingRight = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_wallpaper_item_padding_horizon );
					paddingLeft = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_wallpaper_item_gap );
				}
				else
				{
					paddingLeft = paddingRight = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_wallpaper_item_gap );
				}
				holder.mImageView.setPadding( 0 , 0 , 0 , 0 );
			}
			holder.rootView.setPadding( paddingLeft , 0 , paddingRight , 0 );
		}

	}
	
	public void setOnItemClickListener(
			onItemClickListener mListener )
	{
		this.mListener = mListener;
	}

}
