package com.cooee.favorites.recommended;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.R;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.uniex.wrap.FavoritesConfig;


public class FavoriteIconView extends TextView
{
	
	public FavoriteIconView(
			Context context ,
			AttributeSet attrs )
	{
		super( context , attrs );
		//		Log.v( "lvjiangbin" , "FavoriteIconView  FavoritesAppData.iconSize  = " + FavoritesAppData.iconSize );
		//		RuntimeException e = new RuntimeException( "leon is here" );
		//		e.fillInStackTrace();
		//		Log.i( "lvjiangbin" , "xxx" , e );
		initData();//cheyinkgun add	//酷生活界面优化(常用应用未加载出来时,预留高度)
	}
	
	//	private Intent intent;
	public FavoriteIconView(
			Context context )
	{
		super( context );
		//		Log.v( "lvjiangbin" , "FavoriteIconView  FavoritesAppData.iconSize  = " + FavoritesAppData.iconSize );
		//		RuntimeException e = new RuntimeException( "leon is here" );
		//		e.fillInStackTrace();
		//		Log.i( "lvjiangbin" , "xxx" , e );
		initData();
	}
	
	//cheyinkgun add start	//酷生活界面优化(常用应用未加载出来时,预留高度)
	public void initData()
	{
		setGravity( Gravity.CENTER );
		FavoritesConfig config = FavoritesManager.getInstance().getConfig();
		int mIconSize = config.getInt( FavoriteConfigString.getLauncherIconSizePxKey() , FavoriteConfigString.getLauncherIconSizePxDefaultValue() );
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( mIconSize , LinearLayout.LayoutParams.MATCH_PARENT );
		layoutParams.weight = 1;
		setLayoutParams( layoutParams );
		setVisibility( INVISIBLE );
		setLines( 1 );
		//		setPadding( 0 , FavoriteMainView.ICON_PADDING_TOP_BOTTOM , 0 , FavoriteMainView.ICON_PADDING_TOP_BOTTOM );
		setTextSize( TypedValue.COMPLEX_UNIT_PX , (int)getContext().getResources().getDimension( R.dimen.icon_text_size ) );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
		{
			setTextColor( getContext().getResources().getColor( R.color.favorites_icon_text_color_s5 ) );
		}
		else
		{
			setTextColor( getContext().getResources().getColor( R.color.favorites_icon_text_color ) );
		}
		setIncludeFontPadding( false );
	}
	
	//cheyinkgun add end
	public void setIcon(
			Bitmap bitmap )
	{
		if( bitmap == null || bitmap.isRecycled() )
		{
			return;
		}
		FavoritesConfig config = FavoritesManager.getInstance().getConfig();
		int iconSize = config.getInt( FavoriteConfigString.getLauncherIconSizePxKey() , FavoriteConfigString.getLauncherIconSizePxDefaultValue() );
		Log.v( "lvjiangbin" , "setIcon  bitmap = " + bitmap.getWidth() );
		Log.v( "lvjiangbin" , "setIcon  FavoritesAppData.iconSize = " + iconSize );
		Drawable textDrawable = getCompoundDrawables()[1];
		if( textDrawable != null )
		{
			textDrawable.setCallback( null );
		}
		textDrawable = new BitmapDrawable( bitmap );
		textDrawable.setBounds( 0 , 0 , iconSize , iconSize );
		setCompoundDrawables( null , textDrawable , null , null );
		setCompoundDrawablePadding( (int)getContext().getResources().getDimension( R.dimen.icon_text_space ) );
		setVisibility( VISIBLE );
	}
	
	public void setIcon(
			Drawable drawable )
	{
		Drawable textDrawable = getCompoundDrawables()[1];
		if( textDrawable != null )
		{
			textDrawable.setCallback( null );
		}
		FavoritesConfig config = FavoritesManager.getInstance().getConfig();
		int iconSize = config.getInt( FavoriteConfigString.getLauncherIconSizePxKey() , FavoriteConfigString.getLauncherIconSizePxDefaultValue() );
		//cheyingkun start	//酷生活支持动态修改图标大小
		//cheyingkun del start
		//		if( drawable == null )
		//		{
		//			return;
		//		}
		//cheyingkun del end
		if( drawable != null )//cheyingkun add
		//cheyingkun end
		{
			drawable.setBounds( 0 , 0 , iconSize , iconSize );
		}
		setCompoundDrawables( null , drawable , null , null );
		setCompoundDrawablePadding( (int)getContext().getResources().getDimension( R.dimen.icon_text_space ) );
		setVisibility( VISIBLE );
	}
	//	public void setIntent(
	//			Intent intent )
	//	{
	//		this.intent = intent;
	//	}
}
