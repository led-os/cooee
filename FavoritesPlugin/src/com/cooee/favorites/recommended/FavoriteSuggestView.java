package com.cooee.favorites.recommended;


import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.FavoritesPlugin;
import com.cooee.favorites.R;
import com.cooee.favorites.apps.FavoritesAppManager;
import com.cooee.favorites.data.AppInfo;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.umeng.analytics.MobclickAgent;


public class FavoriteSuggestView extends LinearLayout
{
	
	LinearLayout mSuggersViewApp;
	RelativeLayout textLayout;
	TextView mTextview;
	
	public FavoriteSuggestView(
			Context context ,
			AttributeSet attrs )
	{
		super( context );
		setOrientation( VERTICAL );
		// TODO Auto-generated constructor stub
	}
	
	public FavoriteSuggestView(
			Context context )
	{
		super( context );
		setOrientation( VERTICAL );
		//		Log.v( "lvjingbin" , "FavoriteSuggestView oncreat" );
	}
	
	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		LinearLayout.LayoutParams mFristLP = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , FavoritesManager.getInstance().getFirstLinearHeight() );
		textLayout = (RelativeLayout)findViewById( R.id.textLayout );
		//cheyingkun add start	//优化推荐区域显示逻辑
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableAppsKey() , FavoriteConfigString.isEnableAppsDefaultValue() ) )
		{
			textLayout.setVisibility( VISIBLE );
			//cheyingkun add end
			mTextview = (TextView)findViewById( R.id.title_recommend );
			mTextview.setIncludeFontPadding( false );
			mTextview.setTextSize( TypedValue.COMPLEX_UNIT_PX , (int)getContext().getResources().getDimension( R.dimen.favorites_title_text_size ) );
		}
		/*常用应用第一行*/
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableAppsKey() , FavoriteConfigString.isEnableAppsDefaultValue() ) )
		{
			mSuggersViewApp = (LinearLayout)findViewById( R.id.suggersviewapp );
			mSuggersViewApp.setVisibility( VISIBLE );//cheyingkun add	//优化推荐区域显示逻辑
			mSuggersViewApp.setLayoutParams( mFristLP );
		}
	}
	
	public void addALLDataToAPP(
			final CopyOnWriteArrayList<AppInfo> dataList )
	{
		if( !FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableAppsKey() , FavoriteConfigString.isEnableAppsDefaultValue() ) )
		{
			return;
		}
		for( AppInfo data : dataList )
		{
			out:
			{
				if( mSuggersViewApp != null )
				{
					for( int i = 0 ; i < mSuggersViewApp.getChildCount() ; i++ )
					{
						if( mSuggersViewApp.getChildAt( i ).getVisibility() == INVISIBLE )
						{
							initLineView( (FavoriteIconView)mSuggersViewApp.getChildAt( i ) , data.getTitle() , data.getIconBitmap() , data.getIntent() );
							//							mSuggersViewApp.setVisibility( VISIBLE );//cheyingkun del	//解决“常用应用添加删除第一行时,推荐应用显示异常”的问题。
							break out;
						}
					}
				}
			}
		}
	}
	
	public void removeAllAppView()
	{
		Log.v( "lvjiangbin" , "removeAllAppView" );
		//		mTextview.setText( getResources().getString( R.string.title_Show_more ) );
		//		if( mSuggersViewAppSecond != null )
		//		{
		//			mSuggersViewAppSecond.removeAllViews();
		//		}
		createIconView( mSuggersViewApp );
	}
	
	private void createIconView(
			ViewGroup view )
	{
		if( view == null )
		{
			return;
		}
		for( int i = 0 ; i < view.getChildCount() ; i++ )
		{
			FavoriteIconView favoriteIconView = (FavoriteIconView)view.getChildAt( i );
			favoriteIconView.setIcon( (Drawable)null );
			favoriteIconView.setVisibility( INVISIBLE );
		}
		//		int count = view.getChildCount();
		//		for( int i = 0 ; i < 4 - count ; i++ )
		//		{
		//			FavoriteIconView favoriteIconView = new FavoriteIconView( getContext() );
		//			view.addView( favoriteIconView );
		//			view.setVisibility( INVISIBLE );
		//		}
	}
	
	private void initLineView(
			FavoriteIconView view ,
			String title ,
			Bitmap bitmap ,
			final Intent intent )
	{
		view.setIcon( bitmap );
		view.setText( title );
		//		view.setIntent( intent );
		view.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				if( !FavoritesManager.getInstance().isNewsExpandedMode() )//cheyingkun add	//新闻全屏模式,推荐和附近区域不响应点击事件
				{
					handleStartActivity( intent );
				}
			}
		} );
	}
	
	public void handleStartActivity(
			Intent intent )
	{
		//		getThreadPool().execute( new Runnable() {
		//			
		//			@Override
		//			public void run()
		//			{
		try
		{
			//cheyingkun add start	//添加友盟统计自定义事件
			FavoritesManager favoritesManager = FavoritesManager.getInstance();
			FavoritesConfig config = favoritesManager.getConfig();
			{//-1屏应用
				if( config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
				{
					MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "application_express_click" );
				}
				try
				{
					StatisticsExpandNew.onCustomEvent(
							FavoritesManager.getInstance().getContainerContext() ,
							"application_express_click" ,
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
								"application_express_click" ,
								FavoritesPlugin.SN ,
								FavoritesPlugin.APPID ,
								CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName );
					}
					catch( NoSuchMethodError e1 )
					{
						StatisticsExpandNew.onCustomEvent(
								FavoritesManager.getInstance().getContainerContext() ,
								"application_express_click" ,
								FavoritesPlugin.PRODUCTTYPE ,
								FavoritesPlugin.PluginPackageName );
					}
				}
			}
			//cheyingkun add end
			if( intent.getAction() != null && intent.getAction().equals( "com.android.contacts.action.QUICK_CONTACT" ) )
			{
				ContactsContract.QuickContact.showQuickContact( getContext() , FavoriteSuggestView.this , intent.getData() , ContactsContract.QuickContact.MODE_SMALL , null );
			}
			else
				getContext().startActivity( intent );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		//			}
		//		} );
	}
	
	//	private boolean isAppAnyMore()
	//	{
	//		for( int i = 0 ; i < mSuggersViewAppSecond.getChildCount() ; i++ )
	//		{
	//			View view = mSuggersViewAppSecond.getChildAt( i );
	//			if( view != null && view.getVisibility() == VISIBLE )
	//			{
	//				return true;
	//			}
	//		}
	//		return false;
	//	}
	//	
	//	private boolean isCentactAnyMore()
	//	{
	//		for( int i = 0 ; i < mSuggersViewCentactSecond.getChildCount() ; i++ )
	//		{
	//			View view = mSuggersViewCentactSecond.getChildAt( i );
	//			if( view != null && view.getVisibility() == VISIBLE )
	//			{
	//				return true;
	//			}
	//		}
	//		return false;
	//	}
	public boolean isAnyMore(
			ViewGroup viewGroup )
	{
		if( viewGroup == null )
		{
			return false;
		}
		for( int i = 0 ; i < viewGroup.getChildCount() ; i++ )
		{
			View view = viewGroup.getChildAt( i );
			if( view != null && view.getVisibility() == VISIBLE )
			{
				return true;
			}
		}
		return false;
	}
	
	private int textLayoutHeight = 0;//cheyingkun add	//推荐title布局的高度。
	
	//chenchen add start
	/**获取推荐区域的高度
	 * 
	 * @return
	 */
	public int getSuggestHeight()
	{
		if( getVisibility() == View.GONE )
		{
			return 0;
		}
		int textLayoutHeight = getTextLayoutHeight();
		int iconSize = FavoritesManager.getInstance().getFirstLinearHeight();
		Log.d( "TAG" , "Suggest iconSize" + iconSize );
		//		int padding = FavoriteMainView.ICON_PADDING_TOP_BOTTOM * 4;
		//		Log.d( "TAG" , "Suggest padding:" + padding );
		int lineSize = 0;
		Log.d( "TAG" , "Suggest lineSize" + lineSize );
		int index = 0;
		if( mSuggersViewApp != null )
		{
			index++;
		}
		Log.d( "TAG" , "index" + index );
		return textLayoutHeight + iconSize * index;
	}
	
	//chenchen add edd
	//cheyingkun add start	//推荐title布局的高度。
	/**
	 * 推荐title区域高度
	 * @return
	 */
	private int getTextLayoutHeight()
	{
		if( textLayoutHeight == 0 )
		{
			int textSize = (int)getContext().getResources().getDimension( R.dimen.favorites_title_text_size );
			int textPadding = FavoriteMainView.TITLE_PADDING_TOP + FavoriteMainView.TITLE_PADDING_BOTTOM;
			Drawable drawable = getResources().getDrawable( R.drawable.favorite_show_more );
			int minimumHeight = drawable.getMinimumHeight();
			textLayoutHeight = ( Math.max( minimumHeight , textSize ) + textPadding );
		}
		return textLayoutHeight;
	}
	
	//cheyingkun add end
	//cheyingkun add start	//服务器关闭酷生活后，释放资源。
	public void clearFavoritesView()
	{
		if( mSuggersViewApp != null )
		{
			FavoritesAppManager.getInstance().clearFavoritesView();
		}
	}
	
	//cheyingkun add end
	//cheyingkun add start	//酷生活支持动态修改图标大小
	public void onIconSizeChanged(
			int changedSize )
	{
		int firstLinearHeight = FavoritesManager.getInstance().getFirstLinearHeight();
		Log.d( "" , "cyk firstLinearHeight: " + firstLinearHeight );
		LinearLayout.LayoutParams mFristLP = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , firstLinearHeight );
		if( mSuggersViewApp != null )
		{
			mSuggersViewApp.setLayoutParams( mFristLP );
			onIconSizeChangedSetIcon( mSuggersViewApp );
		}
		refreshLayoutByIconSizeChanged( changedSize );
	}
	
	private void refreshLayoutByIconSizeChanged(
			int changedSize )
	{
		Log.d( "" , "cyk refreshLayoutByIconSizeChanged 0 " );
		int firstLinearHeight = FavoritesManager.getInstance().getFirstLinearHeight();
		Log.d( "" , "cyk firstLinearHeight: " + firstLinearHeight );
		if( mSuggersViewApp != null && isAnyMore( mSuggersViewApp ) )
		{
			float mSuggersViewAppY = mSuggersViewApp.getY();
			mSuggersViewApp.setY( mSuggersViewAppY );
		}
		Log.d( "" , "cyk refreshLayoutByIconSizeChanged 1 " );
	}
	
	private void onIconSizeChangedSetIcon(
			LinearLayout mLinearLayout )
	{
		if( mLinearLayout != null )
		{
			int childCount = mLinearLayout.getChildCount();
			for( int i = 0 ; i < childCount ; i++ )
			{
				FavoriteIconView mFavoriteIconView = (FavoriteIconView)mLinearLayout.getChildAt( i );
				if( mFavoriteIconView.getVisibility() == View.VISIBLE )
				{
					Drawable drawable = mFavoriteIconView.getCompoundDrawables()[1];
					mFavoriteIconView.setIcon( drawable );
				}
			}
		}
	}
	//cheyingkun add end
}
