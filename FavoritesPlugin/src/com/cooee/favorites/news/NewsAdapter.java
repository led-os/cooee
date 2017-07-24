package com.cooee.favorites.news;


import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.R;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.news.data.NewsItem;
import com.cooee.favorites.utils.Tools;


public class NewsAdapter extends BaseAdapter
{
	
	private List<NewsItem> newsList;
	private Context mContext;
	private int IMG_MAX_HEIGHT = 80;
	private int ITEM_MIN_HEIGHT = 100;
	private int VERTICAL_MARGIN = 10;
	private int HORIZONTAL_MARGIN = 10;
	private int TITLE_SIZE = 12;
	private ImageLoader imageLoader;
	private onNotifyClickListener mClickListener;
	
	public NewsAdapter(
			Context ctx ,
			RequestQueue queue ,
			List<NewsItem> list )
	{
		mContext = ctx;
		newsList = list;
		imageLoader = new ImageLoader( queue , new BitmapCache( ctx ) );
		IMG_MAX_HEIGHT = Tools.dip2px( mContext , IMG_MAX_HEIGHT );
		ITEM_MIN_HEIGHT = Tools.dip2px( mContext , ITEM_MIN_HEIGHT );
		VERTICAL_MARGIN = Tools.dip2px( mContext , VERTICAL_MARGIN );
		HORIZONTAL_MARGIN = Tools.dip2px( mContext , HORIZONTAL_MARGIN );
		TITLE_SIZE = Tools.dip2px( mContext , TITLE_SIZE );
	}
	
	@Override
	public boolean isEnabled(
			int position )
	{
		// TODO Auto-generated method stub
		if( newsList.get( position ).getNotifyType() != 0 )
		{
			return false;
		}
		return super.isEnabled( position );
	}
	
	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return newsList.size();
	}
	
	@Override
	public Object getItem(
			int position )
	{
		// TODO Auto-generated method stub
		return newsList.get( position );
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
			int position ,
			View convertView ,
			ViewGroup parent )
	{
		// TODO Auto-generated method stub
		ViewHolder holder;
		if( convertView == null )
		{
			holder = new ViewHolder();
			convertView = holder.getItemView();
			convertView.setTag( holder );
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}
		if( newsList.get( position ).getNotifyType() != 0 )
		{
			holder.mNotify.setVisibility( View.VISIBLE );
			holder.mContent.setVisibility( View.GONE );
			return convertView;
		}
		holder.mNotify.setVisibility( View.GONE );
		holder.mContent.setVisibility( View.VISIBLE );
		if( TextUtils.isEmpty( newsList.get( position ).getTitle() ) )
		{
			holder.mTitle.setVisibility( View.GONE );
		}
		else
		{
			holder.mTitle.setVisibility( View.VISIBLE );
			holder.mTitle.setText( newsList.get( position ).getTitle() );
		}
		//		holder.mTitle.setText( "test" );
		String thumb = newsList.get( position ).getThumbImage();
		String imglist[] = newsList.get( position ).getImageList();
		String hq = newsList.get( position ).getHQImageUrl();
		if( thumb != null && !thumb.equals( "" ) )
		{
			holder.mThumbImage.setImageUrl( thumb , imageLoader );
			holder.mThumbImage.setVisibility( View.VISIBLE );
			for( NetworkImageView img : holder.mImageList )
			{
				img.setVisibility( View.GONE );
			}
			holder.mHQImage.setVisibility( View.GONE );
			holder.mFrameLayout.setVisibility( View.GONE );
			holder.mShowTime_thumb.setText( newsList.get( position ).getShowTime() );
			holder.mSite_thumb.setText( newsList.get( position ).getSite() );
			holder.mShowTime.setVisibility( View.GONE );
			holder.mSiteandComment.setVisibility( View.GONE );
			holder.mSite_thumb.setVisibility( View.VISIBLE );
			holder.mShowTime_thumb.setVisibility( View.VISIBLE );
		}
		else if( hq != null && !hq.equals( "" ) )
		{
			holder.mHQImage.setImageUrl( hq , imageLoader );
			holder.mFrameLayout.setVisibility( View.VISIBLE );
			holder.mHQImage.setVisibility( View.VISIBLE );
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)holder.mHQImage.getLayoutParams();
			if( params != null )
			{
				int imagewidth = newsList.get( position ).getHQWidth();
				int imageheight = newsList.get( position ).getHQHeight();
				params.width = mContext.getResources().getDisplayMetrics().widthPixels - Tools.dip2px( mContext , 30 );//图片左右各有15dp的margin
				if( imagewidth > 0 && imagewidth > 0 )
				{
					params.height = (int)( params.width * 1.0f / imagewidth * imageheight );//按照广告的大小比例显示
				}
				else
				{
					params.height = (int)mContext.getResources().getDimension( R.dimen.news_hq_image_height );
				}
			}
			for( NetworkImageView img : holder.mImageList )
			{
				img.setVisibility( View.GONE );
			}
			holder.mThumbImage.setVisibility( View.GONE );
			holder.mShowTime.setText( newsList.get( position ).getShowTime() );
			holder.mSiteandComment.setText( newsList.get( position ).getSite() );
			holder.mShowTime.setVisibility( View.VISIBLE );
			holder.mSiteandComment.setVisibility( View.VISIBLE );
			holder.mSite_thumb.setVisibility( View.GONE );
			holder.mShowTime_thumb.setVisibility( View.GONE );
		}
		else if( imglist != null && imglist.length == 3 )
		{
			holder.mThumbImage.setVisibility( View.GONE );
			holder.mFrameLayout.setVisibility( View.VISIBLE );
			for( int i = 0 ; i < 3 ; i++ )
			{
				holder.mImageList[i].setImageUrl( imglist[i] , imageLoader );
				holder.mImageList[i].setVisibility( View.VISIBLE );
			}
			holder.mHQImage.setVisibility( View.GONE );
			holder.mShowTime.setText( newsList.get( position ).getShowTime() );
			holder.mSiteandComment.setText( newsList.get( position ).getSite() );
			holder.mShowTime.setVisibility( View.VISIBLE );
			holder.mSiteandComment.setVisibility( View.VISIBLE );
			holder.mSite_thumb.setVisibility( View.GONE );
			holder.mShowTime_thumb.setVisibility( View.GONE );
		}
		else
		{
			holder.mThumbImage.setVisibility( View.GONE );
			for( int i = 0 ; i < 3 ; i++ )
			{
				holder.mImageList[i].setVisibility( View.GONE );
			}
			holder.mHQImage.setVisibility( View.GONE );
			holder.mFrameLayout.setVisibility( View.GONE );
			holder.mShowTime_thumb.setText( newsList.get( position ).getShowTime() );
			holder.mSite_thumb.setText( newsList.get( position ).getSite() );
			holder.mShowTime.setVisibility( View.GONE );
			holder.mSiteandComment.setVisibility( View.GONE );
			holder.mSite_thumb.setVisibility( View.VISIBLE );
			holder.mShowTime_thumb.setVisibility( View.VISIBLE );
		}
		return convertView;
	}
	
	public void setClickListener(
			onNotifyClickListener mClickListener )
	{
		this.mClickListener = mClickListener;
	}
	
	class ViewHolder
	{
		
		View mNotify;
		View mContent;
		TextView mTitle;
		TextView mSiteandComment;
		TextView mShowTime;
		TextView mSite_thumb;
		TextView mShowTime_thumb;
		NetworkImageView mThumbImage;
		NetworkImageView mHQImage;
		NetworkImageView mImageList[];
		View mFrameLayout;
		
		//		final int TITLE_ID = 2001;
		//		final int SITE_ID = 2002;
		public View getItemView()
		{
			LayoutInflater inflater = LayoutInflater.from( mContext ).cloneInContext( mContext );
			View view = null;
			if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableSimpleLauncherKey() , FavoriteConfigString.isEnableSimpleLauncherDefaultValue() ) )
			{
				view = inflater.inflate( R.layout.news_large_item , null );
			}
			else
			{
				if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
				{
					view = inflater.inflate( R.layout.news_item_s5 , null );
				}
				else
				{
					view = inflater.inflate( R.layout.news_item , null );
				}
			}
			mNotify = view.findViewById( R.id.notify_to_refresh );
			mContent = view.findViewById( R.id.news_item_content );
			mThumbImage = (NetworkImageView)view.findViewById( R.id.thumb );
			mTitle = (TextView)view.findViewById( R.id.title );
			mHQImage = (NetworkImageView)view.findViewById( R.id.hqimg );
			NetworkImageView list1 = (NetworkImageView)view.findViewById( R.id.imglist1 );
			NetworkImageView list2 = (NetworkImageView)view.findViewById( R.id.imglist2 );
			NetworkImageView list3 = (NetworkImageView)view.findViewById( R.id.imglist3 );
			mImageList = new NetworkImageView[]{ list1 , list2 , list3 };
			mSiteandComment = (TextView)view.findViewById( com.cooee.favorites.R.id.site );
			mShowTime = (TextView)view.findViewById( com.cooee.favorites.R.id.showtime );
			mFrameLayout = view.findViewById( com.cooee.favorites.R.id.framelayout );
			mSite_thumb = (TextView)view.findViewById( R.id.site_thumb );
			mShowTime_thumb = (TextView)view.findViewById( R.id.showtime_thumb );
			mHQImage.setDefaultImageResId( R.drawable.network_error_large );
			mThumbImage.setDefaultImageResId( R.drawable.network_error_small );
			mImageList[0].setDefaultImageResId( R.drawable.network_error_small );
			mImageList[1].setDefaultImageResId( R.drawable.network_error_small );
			mImageList[2].setDefaultImageResId( R.drawable.network_error_small );
			mNotify.setOnClickListener( new View.OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					// TODO Auto-generated method stub
					if( mClickListener != null )
					{
						mClickListener.onClick( v );
					}
				}
			} );
			//			LinearLayout view = new LinearLayout( mContext );
			//			view.setMinimumHeight( ITEM_MIN_HEIGHT );
			//			view.setOrientation( LinearLayout.HORIZONTAL );
			//			mThumbImage = new NetworkImageView( mContext );
			//			mThumbImage.setScaleType( ScaleType.CENTER_CROP );
			//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , IMG_MAX_HEIGHT );
			//			params.weight = 2;
			//			params.leftMargin = HORIZONTAL_MARGIN;
			//			params.gravity = Gravity.CENTER_VERTICAL;
			//			view.addView( mThumbImage , params );
			//			RelativeLayout right = new RelativeLayout( mContext );
			//			mTitle = new TextView( mContext );
			//			mTitle.setId( TITLE_ID );
			//			RelativeLayout.LayoutParams rparams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
			//			mTitle.setMaxLines( 2 );
			//			mTitle.setTextSize( TITLE_SIZE );
			//			rparams.topMargin = VERTICAL_MARGIN;
			//			rparams.leftMargin = HORIZONTAL_MARGIN;
			//			rparams.rightMargin = HORIZONTAL_MARGIN;
			//			right.addView( mTitle , rparams );
			//			mSiteandComment = new TextView( mContext );
			//			mSiteandComment.setId( SITE_ID );
			//			rparams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
			//			rparams.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.TRUE );
			//			rparams.addRule( RelativeLayout.ALIGN_PARENT_LEFT , RelativeLayout.TRUE );
			//			rparams.leftMargin = HORIZONTAL_MARGIN;
			//			rparams.bottomMargin = VERTICAL_MARGIN;
			//			right.addView( mSiteandComment , rparams );
			//			mShowTime = new TextView( mContext );
			//			rparams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
			//			rparams.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM , RelativeLayout.TRUE );
			//			rparams.addRule( RelativeLayout.ALIGN_PARENT_RIGHT , RelativeLayout.TRUE );
			//			rparams.rightMargin = HORIZONTAL_MARGIN;
			//			rparams.bottomMargin = VERTICAL_MARGIN;
			//			right.addView( mShowTime , rparams );
			//			LinearLayout list = new LinearLayout( mContext );
			//			NetworkImageView img1 = new NetworkImageView( mContext );
			//			NetworkImageView img2 = new NetworkImageView( mContext );
			//			NetworkImageView img3 = new NetworkImageView( mContext );
			//			img1.setScaleType( ScaleType.CENTER_CROP );
			//			img2.setScaleType( ScaleType.CENTER_CROP );
			//			img3.setScaleType( ScaleType.CENTER_CROP );
			//			mImageList = new NetworkImageView[]{ img1 , img2 , img3 };
			//			params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , IMG_MAX_HEIGHT );
			//			params.weight = 1;
			//			list.addView( img1 , params );
			//			params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , IMG_MAX_HEIGHT );
			//			params.weight = 1;
			//			list.addView( img2 , params );
			//			params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , IMG_MAX_HEIGHT );
			//			params.weight = 1;
			//			list.addView( img3 , params );
			//			rparams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
			//			rparams.addRule( RelativeLayout.BELOW , TITLE_ID );
			//			rparams.addRule( RelativeLayout.ABOVE , SITE_ID );
			//			rparams.leftMargin = HORIZONTAL_MARGIN;
			//			rparams.rightMargin = HORIZONTAL_MARGIN;
			//			right.addView( list , rparams );
			//			mHQImage = new NetworkImageView( mContext );
			//			mHQImage.setScaleType( ScaleType.CENTER_CROP );
			//			rparams = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT , IMG_MAX_HEIGHT );
			//			rparams.addRule( RelativeLayout.BELOW , TITLE_ID );
			//			rparams.addRule( RelativeLayout.ABOVE , SITE_ID );
			//			rparams.leftMargin = HORIZONTAL_MARGIN;
			//			rparams.rightMargin = HORIZONTAL_MARGIN;
			//			right.addView( mHQImage , rparams );
			//			params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.MATCH_PARENT );
			//			params.weight = 1;
			//			view.addView( right , params );
			return view;
		}
	}
	
	interface onNotifyClickListener
	{
		
		public void onClick(
				View v );
	}
}
