package com.cooee.favorites.recommended;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.FavoritesPlugin;
import com.cooee.favorites.R;
import com.cooee.favorites.data.AppInfo;
import com.cooee.favorites.data.NearByItem;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.favorites.utils.CooeeLocation;
import com.cooee.favorites.utils.NetworkAvailableUtils;
import com.cooee.shell.sdk.CooeeSdk;
import com.cooee.statistics.StatisticsExpandNew;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.kmob.kmobsdk.KmobManager;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.umeng.analytics.MobclickAgent;

import cool.sdk.common.CoolMethod;


public class FavoriteNearbyView extends LinearLayout implements TencentLocationListener , OnCancelListener
{
	
	LinearLayout mNearbyView;
	private final int[] ICON = new int[]{ R.drawable.favorites_recommend_atm , R.drawable.favorites_recommend_bus , R.drawable.favorites_recommend_cate , R.drawable.favorites_recommend_groupon };
	private final int[] TITLE = new int[]{ R.string.recommend_atm_title , R.string.recommend_bus_title , R.string.recommend_cate_title , R.string.recommend_groupon_title };
	private boolean isLoad = false;
	private String itude[] = new String[2];
	private String mParams = "null";
	private Handler mHandler = new Handler();
	private final String[] NEAR_ONCLICK_STR = new String[]{ "near_atm_click" , "near_bus_station_click" , "near_food_click" , "near_group_buying_click" };//cheyingkun add	//添加附近点击统计和滑入-1屏统计
	private ArrayList<FavoriteIconView> mFavoriteIconViewList = new ArrayList<FavoriteIconView>();
	private FavoriteNearbyDialog mDialog;
	private CooeeLocation mCooeeLocation = null;
	
	//cheyingkun add end
	public FavoriteNearbyView(
			Context context )
	{
		super( context );
		setOrientation( LinearLayout.VERTICAL );
		//		setBackgroundColor( android.graphics.Color.RED );
		TextView text = new TextView( context );
		//		LinearLayout.LayoutParams layoutParams =   new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		text.setText( getResources().getString( R.string.title_near ) );
		text.setIncludeFontPadding( false );
		text.setTextSize( TypedValue.COMPLEX_UNIT_PX , (int)getContext().getResources().getDimension( R.dimen.favorites_title_text_size ) );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
		{
			text.setTextColor( getContext().getResources().getColor( R.color.favorites_text_color_s5 ) );
		}
		else
		{
			text.setTextColor( getContext().getResources().getColor( R.color.favorites_text_color ) );
		}
		text.setPadding( FavoriteMainView.TITLE_PADDING_LIFT_RIGHT , FavoriteMainView.TITLE_PADDING_TOP , FavoriteMainView.TITLE_PADDING_LIFT_RIGHT , FavoriteMainView.TITLE_PADDING_BOTTOM );
		addView( text );
		mNearbyView = new LinearLayout( context );
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , FavoritesManager.getInstance().getFirstLinearHeight() );
		mNearbyView.setPadding( 0 , 0 , 0 , 0 );
		mNearbyView.setLayoutParams( layoutParams );
		if( FavoritesManager.getInstance().getConfig().getBoolean( FavoriteConfigString.getEnableIsS5Key() , FavoriteConfigString.isEnableFavoritesS5DefaultValue() ) )
		{
			mNearbyView.setBackgroundColor( getContext().getResources().getColor( R.color.item_bg_color_s5 ) );
		}
		else
		{
			mNearbyView.setBackgroundColor( getContext().getResources().getColor( R.color.item_bg_color ) );
		}
		mNearbyView.setOrientation( LinearLayout.HORIZONTAL );
		addView( mNearbyView );
		mCooeeLocation = new CooeeLocation( context );
		mCooeeLocation.setTencentLocationListener( this );
		mCooeeLocation.setTryNum( 3 );
	}
	
	public void initLineView()
	{
		if( !isLoad )
		{
			for( int i = 0 ; i < 4 ; i++ )
			{
				final String title = getContext().getResources().getString( TITLE[i] );
				OnClickListener listener = new OnClickListener() {
					
					@Override
					public void onClick(
							View v )
					{
						if( !FavoritesManager.getInstance().isNewsExpandedMode() )//cheyingkun add	//新闻全屏模式,推荐和附近区域不响应点击事件
						{
							mParams = title;
							locationUpdates();
						}
					}
				};
				FavoriteIconView favoriteIconView = new FavoriteIconView( getContext() );
				favoriteIconView.setPadding( 0 , FavoriteMainView.ICON_PADDING_TOP_BOTTOM , 0 , FavoriteMainView.ICON_PADDING_TOP_BOTTOM );
				favoriteIconView.setOnClickListener( listener );
				favoriteIconView.setIcon( getContext().getResources().getDrawable( ICON[i] ) );
				favoriteIconView.setText( title );
				mNearbyView.addView( favoriteIconView );
				mFavoriteIconViewList.add( favoriteIconView );
			}
			isLoad = true;
		}
	}
	
	public void setAdView(
			final AppInfo info ,
			int index )
	{
		if( mFavoriteIconViewList.size() != 4 )//非第一次 恰好启动的时候时间更新，mFavoriteIconViewList可能没有初始化
		{
			return;
		}
		FavoriteIconView favoriteIconView = mFavoriteIconViewList.get( index );
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				if( !FavoritesManager.getInstance().isNewsExpandedMode() )//cheyingkun add	//新闻全屏模式,推荐和附近区域不响应点击事件
				{
					if( info.getAdType().equals( AppInfo.KMOB_AD ) )
					{
						Log.v( "lvjiangbin" , "info.getClickIntent() = " + info.getAdData() );
						KmobManager.onClickDone( info.getAdId() , info.getAdData() , true );
					}
				}
			}
		};
		favoriteIconView.setOnClickListener( listener );
		favoriteIconView.setIcon( info.getIconBitmap() );
		favoriteIconView.setText( info.getTitle() );
	}
	
	private void startSearch()
	{
		Log.v( "lvjiangbin" , "LocationThread 1" );
		if( !NetworkAvailableUtils.isNetworkAvailable( FavoritesManager.getInstance().getContainerContext() ) || ( itude[0] == null || itude[1] == null ) )
		{
			post( new Runnable() {
				
				@Override
				public void run()
				{
					// TODO Auto-generated method stub
					Context pluginContext = FavoritesManager.getInstance().getPluginContext();
					CharSequence text = pluginContext.getResources().getText( R.string.internet_err );
					Toast.makeText( FavoritesManager.getInstance().getContainerContext() , text , Toast.LENGTH_SHORT ).show();
				}
			} );
			return;
		}
		//cheyingkun add start	//添加友盟统计自定义事件
		FavoritesManager favoritesManager = FavoritesManager.getInstance();
		FavoritesConfig config = favoritesManager.getConfig();
		//统计附近下面item的点击
		HashMap<String , String> map = new HashMap<String , String>();
		map.put( "near_onClick" , mParams );
		if( config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
		{
			MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , "near_" , map );
		}
		try
		{
			StatisticsExpandNew.onCustomEvent(
					FavoritesManager.getInstance().getContainerContext() ,
					"near_" ,
					FavoritesPlugin.SN ,
					FavoritesPlugin.APPID ,
					CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
					FavoritesPlugin.PRODUCTTYPE ,
					FavoritesPlugin.PluginPackageName ,
					FavoritesPlugin.UPLOAD_VERSION + "" ,
					new JSONObject( map ) );
		}
		catch( NoSuchMethodError e )
		{
			try
			{
				StatisticsExpandNew.onCustomEvent(
						FavoritesManager.getInstance().getContainerContext() ,
						"near_" ,
						FavoritesPlugin.SN ,
						FavoritesPlugin.APPID ,
						CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
						FavoritesPlugin.PRODUCTTYPE ,
						FavoritesPlugin.PluginPackageName ,
						new JSONObject( map ) );
			}
			catch( NoSuchMethodError e1 )
			{
				StatisticsExpandNew.onCustomEvent( FavoritesManager.getInstance().getContainerContext() , "near_" , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName , new JSONObject(
						map ) );
			}
		}
		//cheyingkun add start	//添加附近点击统计和滑入-1屏统计
		//附近下面item点击分为四个统计点分别统计
		final String title0 = getContext().getResources().getString( TITLE[0] );
		final String title1 = getContext().getResources().getString( TITLE[1] );
		final String title2 = getContext().getResources().getString( TITLE[2] );
		final String title3 = getContext().getResources().getString( TITLE[3] );
		String umengId = null;
		if( title0 != null && title0.equals( mParams ) )
		{
			umengId = NEAR_ONCLICK_STR[0];
		}
		else if( title1 != null && title1.equals( mParams ) )
		{
			umengId = NEAR_ONCLICK_STR[1];
		}
		else if( title2 != null && title2.equals( mParams ) )
		{
			umengId = NEAR_ONCLICK_STR[2];
		}
		else if( title3 != null && title3.equals( mParams ) )
		{
			umengId = NEAR_ONCLICK_STR[3];
		}
		if( umengId != null )
		{
			if( config.getBoolean( FavoriteConfigString.getEnableUmengKey() , FavoriteConfigString.isEnableUmengDefaultValue() ) )
			{
				MobclickAgent.onEvent( FavoritesManager.getInstance().getContainerContext() , umengId );
			}
			try
			{
				StatisticsExpandNew.onCustomEvent(
						FavoritesManager.getInstance().getContainerContext() ,
						umengId ,
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
							umengId ,
							FavoritesPlugin.SN ,
							FavoritesPlugin.APPID ,
							CooeeSdk.cooeeGetCooeeId( FavoritesManager.getInstance().getContainerContext() ) ,
							FavoritesPlugin.PRODUCTTYPE ,
							FavoritesPlugin.PluginPackageName );
				}
				catch( NoSuchMethodError e1 )
				{
					StatisticsExpandNew.onCustomEvent( FavoritesManager.getInstance().getContainerContext() , umengId , FavoritesPlugin.PRODUCTTYPE , FavoritesPlugin.PluginPackageName );
				}
			}
		}
		//cheyingkun add end
		//cheyingkun add end
		String query = mParams;
		//			String address = CooeeLocationTool.getInstance( getContext().getApplicationContext() ).getLocation( itude );
		if( itude[0] == null || itude[1] == null )
		{
			itude[0] = "31.204055632862";
			itude[1] = "121.41117785465";
		}
		Log.v( "web" , "itude[0] =  " + itude[0] + " itude[1] = " + itude[1] );
		String curLan = Locale.getDefault().toString();
		String imsi = CoolMethod.getImsi( getContext().getApplicationContext() );
		//cheyingkun add start	//解决“默认配置谷歌地图时,中文状态下点击服务提示未安装应用”的问题。
		//中文情况下的字符串
		StringBuffer sbCN = new StringBuffer();
		//非中文情况下的字符串
		StringBuffer sbOther = new StringBuffer();
		sbCN.append( "http://api.map.baidu.com/place/search?" ).append( "query=" ).append( query ).append( "&location=" ).append( itude[0] ).append( "," ).append( itude[1] )
				.append( "&radius=1000&output=html" );
		sbOther.append( "http://www.google.cn/maps/search/" ).append( query ).append( "/@" ).append( itude[0] ).append( "," ).append( itude[1] ).append( ",15z" );
		//当前是否使用中文字符串
		boolean sbCurrentIsCN = false;
		//当前使用的字符串
		StringBuffer sbCurrent = new StringBuffer();
		//cheyingkun add emd
		if( curLan.equals( "zh_CN" ) || curLan.equals( "zh_TW" ) || curLan.equals( "zh_HK" ) || imsi.startsWith( "460" ) )//国内使用百度地图
		{
			sbCurrent.append( "http://api.map.baidu.com/place/search?" ).append( "query=" ).append( query ).append( "&location=" ).append( itude[0] ).append( "," ).append( itude[1] )
					.append( "&radius=1000&output=html" );
			sbCurrentIsCN = true;//cheyingkun add	//解决“默认配置谷歌地图时,中文状态下点击服务提示未安装应用”的问题。
		}
		else
		{
			sbCurrent.append( "http://www.google.cn/maps/search/" ).append( query ).append( "/@" ).append( itude[0] ).append( "," ).append( itude[1] ).append( ",15z" );
			//			sb.append( "https://maps.googleapis.com/maps/api/place/search/json?" ).append( "&location=" ).append( itude[0] ).append( "," ).append( itude[1] ).append( "&radius=" ).append( 1000 )
			//					.append( "&types=" ).append( query ).append( "&keyword=" ).append( query ).append( "&language=" ).append( "en" ).append( "&sensor=true&key=" )
			//					.append( "AIzaSyBzAclzC3NPT61rjUrr7DJtNO-ZNS4VbB0&output=html" );
			sbCurrentIsCN = false;//cheyingkun add	//解决“默认配置谷歌地图时,中文状态下点击服务提示未安装应用”的问题。
		}
		Log.v( "web" , "uri " + sbCurrent.toString() );
		//cheyingkun add start	//解决“默认配置谷歌地图时,中文状态下点击服务提示未安装应用”的问题。
		//中文情况下的字符串
		Intent intentCN = new Intent( Intent.ACTION_VIEW );
		Uri uriCN = Uri.parse( sbCN.toString() );
		intentCN.setData( uriCN );
		//非中文情况下的字符串
		Intent intentOther = new Intent( Intent.ACTION_VIEW );
		Uri uriOther = Uri.parse( sbOther.toString() );
		intentOther.setData( uriOther );
		//当前使用的字符串
		Intent intentCurrent = new Intent( Intent.ACTION_VIEW );
		Uri uriCurrent = Uri.parse( sbCurrent.toString() );
		intentCurrent.setData( uriCurrent );
		//1、如果没配置打开的包类名,直接使用当前intent打开
		//2、如果配置打开的包类名,先判断intentCurrent是否存在
		//2.1、如果不存在,判断另一个url的intent是否存在
		//2.2、如果不存在,直接使用当前intent不配置包类名打开
		//cheyingkun add end
		String browser = FavoritesManager.getInstance().getConfig().getString( FavoriteConfigString.getDefaultBrowserKey() , FavoriteConfigString.getDefaultBrowserValue() );
		if( !browser.equals( "" ) && !browser.equals( "null" ) )
		{
			String[] newsplitstr = browser.split( "/" );
			intentCurrent.setPackage( newsplitstr[0] );
			//cheyingkun add start	//解决“默认配置谷歌地图时,中文状态下点击服务提示未安装应用”的问题。
			Log.d( "web" , "intent 0 : " + intentCurrent );
			//判断当前使用的intent是否存在
			ComponentName componentNameCurrent = intentCurrent.resolveActivity( getContext().getPackageManager() );
			if( componentNameCurrent == null )
			{
				if( sbCurrentIsCN )
				{
					intentCurrent = intentOther;
				}
				else
				{
					intentCurrent = intentCN;
				}
				intentCurrent.setPackage( newsplitstr[0] );
				Log.d( "web" , "intent 1 : " + intentCurrent );
				//
				componentNameCurrent = intentCurrent.resolveActivity( getContext().getPackageManager() );
				if( componentNameCurrent == null )
				{
					if( sbCurrentIsCN )
					{
						intentCurrent = intentCN;
					}
					else
					{
						intentCurrent = intentOther;
					}
					Log.d( "web" , "intent 2 : " + intentCurrent );
				}
			}
			//cheyingkun add end
		}
		else
		{
			intentCurrent.addCategory( Intent.CATEGORY_DEFAULT );
		}
		//		intent.setData( Uri.parse( url ) );
		//		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		getContext().startActivity( intentCurrent );
	}
	
	public void onIconSizeChanged(
			int changedSize )
	{
		if( mNearbyView != null )
		{
			//cheyingkun add start	//酷生活支持动态修改图标大小
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT , FavoritesManager.getInstance().getFirstLinearHeight() );
			mNearbyView.setPadding( 0 , 0 , 0 , 0 );
			mNearbyView.setLayoutParams( layoutParams );
			//cheyingkun add end
			FavoriteIconView favoriteIconView;
			int count = mNearbyView.getChildCount();
			for( int i = 0 ; i < count ; i++ )
			{
				View view = mNearbyView.getChildAt( i );
				if( view instanceof FavoriteIconView )
				{
					favoriteIconView = (FavoriteIconView)view;
					favoriteIconView.setIcon( getContext().getResources().getDrawable( ICON[i] ) );
				}
			}
			//cheyingkun add start	//酷生活支持动态修改图标大小
			float headerViewHeight = FavoritesManager.getInstance().getHeaderViewHeight();
			int nearbyHeight = getNearbyHeight();
			this.setY( headerViewHeight - nearbyHeight );
			//cheyingkun add end
		}
	}
	
	private void locationUpdates()
	{
		if( !NetworkAvailableUtils.isNetworkAvailable( FavoritesManager.getInstance().getContainerContext() ) )
		{
			// TODO Auto-generated method stub
			Context pluginContext = FavoritesManager.getInstance().getPluginContext();
			CharSequence text = pluginContext.getResources().getText( R.string.internet_err );
			Toast.makeText( FavoritesManager.getInstance().getContainerContext() , text , Toast.LENGTH_SHORT ).show();
			return;
		}
		int error = mCooeeLocation.startLocation();
		Log.v( "lvjiangbin" , "LocationThread 5 error = " + error );
		//cheyingkun add start	//注册定位监听器失败时,添加提示信息
		Context pluginContext = FavoritesManager.getInstance().getPluginContext();
		CharSequence text = pluginContext.getResources().getText( R.string.internet_err );
		if( error == 0 )
		{
			Log.v( "lvjiangbin" , "LocationThread 6 成功注册监听器  " );
			if( mDialog != null )
			{
				mDialog.dismiss();
				mDialog = null;
			}
			mDialog = new FavoriteNearbyDialog( getContext() );
			mDialog.setOnCancelListener( this );
			mDialog.show();
			mHandler.removeCallbacks( runnable );
			mHandler.postDelayed( runnable , getContext().getResources().getInteger( R.integer.location_of_waiting_time ) * 1000 );
		}
		else
		{
			Toast.makeText( FavoritesManager.getInstance().getContainerContext() , text + "error :" + error , Toast.LENGTH_SHORT ).show();
		}
		//cheyingkun add end
	}
	
	@Override
	public void onLocationChanged(
			TencentLocation arg0 ,
			int error ,
			String arg2 )
	{
		Log.v( "lvjiangbin" , "LocationThread error" + error );
		Log.v( "lvjiangbin" , "LocationThread arg2" + arg2 );
		if( TencentLocation.ERROR_OK == error )
		{
			itude[0] = "" + arg0.getLatitude();
			itude[1] = "" + arg0.getLongitude();
			startSearch();
		}
		else
		{
			Context pluginContext = FavoritesManager.getInstance().getPluginContext();
			CharSequence text = pluginContext.getResources().getText( R.string.locate_failed );
			Toast.makeText( FavoritesManager.getInstance().getContainerContext() , text , Toast.LENGTH_SHORT ).show();
		}
		if( mDialog != null && mDialog.isShowing() )
		{
			mDialog.dismiss();
		}
	}
	
	@Override
	public void onStatusUpdate(
			String arg0 ,
			int arg1 ,
			String arg2 )
	{
		// TODO Auto-generated method stub
	}
	
	/**
	 * 获取附近区域的高度
	 * @return
	 */
	//chenchen add start
	public int getNearbyHeight()
	{
		int textSize = (int)getContext().getResources().getDimension( R.dimen.favorites_title_text_size );
		int textPadding = FavoriteMainView.TITLE_PADDING_TOP + FavoriteMainView.TITLE_PADDING_BOTTOM;
		int iconSize = FavoritesManager.getInstance().getFirstLinearHeight();//FavoritesAppData.iconSize
		Log.d( "TAG" , "Nearby iconSize:" + iconSize );
		//		int padding = FavoriteMainView.ICON_PADDING_TOP_BOTTOM * 2;
		//		Log.d( "TAG" , "Nearby padding:" + padding );
		int nearbyHeight = iconSize + textSize + textPadding;
		Log.d( "TAG" , "Nearby nearbyHeight:" + nearbyHeight );
		return nearbyHeight;
	}
	
	//chenchen add edd
	//cheyingkun add start	//添加折叠和显示更多交互动画。
	/**
	 * 显示更多时，附近区域的动画
	 * @param changeY 
	 * @return
	 */
	public ObjectAnimator getStartShowMoreOrFoldingAnim(
			float changeY )
	{
		if( changeY == 0 )
		{
			return null;
		}
		PropertyValuesHolder nearbyY = PropertyValuesHolder.ofFloat( "y" , this.getY() , this.getY() + changeY );
		ObjectAnimator nearbyAnim = new ObjectAnimator();
		nearbyAnim.setTarget( this );
		nearbyAnim.setValues( nearbyY );
		return nearbyAnim;
	}
	
	public void initViewsYForShowMoreAndFolding(
			float changeY )
	{
		this.setY( this.getY() + changeY );
	}
	
	//cheyingkun add end
	// zhangjin@2016/06/08 ADD START
	public void setLocalView(
			final NearByItem info ,
			int index )
	{
		FavoriteIconView favoriteIconView = mFavoriteIconViewList.get( index );
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				if( !FavoritesManager.getInstance().isNewsExpandedMode() )//cheyingkun add	//新闻全屏模式,推荐和附近区域不响应点击事件
				{
					if( TextUtils.isEmpty( info.getCmp() ) == false )
					{
						ComponentName cmp = ComponentName.unflattenFromString( info.getCmp() );
						if( cmp != null )
						{
							Intent intent = new Intent();
							intent.setComponent( cmp );
							if( info.getExtra() != null )
							{
								ArrayList<String> list = info.getExtra();
								for( int i = 0 ; i < list.size() ; i++ )
								{
									String str = list.get( i );
									String[] keyValue = new String[2];
									keyValue = str.split( "-" );
									if( keyValue[0] != null && !keyValue[0].equals( "" ) && keyValue[1] != null && !keyValue[1].equals( "" ) )
									{
										Log.v( "lvjiangbin" , "key = " + keyValue[0] + "-value =" + keyValue[1] );
										intent.putExtra( keyValue[0] , keyValue[1] );
									}
								}
							}
							getContext().startActivity( intent );
						}
					}
					else if( TextUtils.isEmpty( info.getUrl() ) == false )
					{
						Intent intent = new Intent( Intent.ACTION_VIEW );
						Uri uri = Uri.parse( info.getUrl() );
						intent.setData( uri );
						intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
						getContext().startActivity( intent );
					}
				}
			}
		};
		favoriteIconView.setText( info.getTitle() );
		favoriteIconView.setIcon( info.getBmp() );
		favoriteIconView.setOnClickListener( listener );
	}
	
	// zhangjin@2016/06/08 ADD END
	Runnable runnable = new Runnable() {
		
		@Override
		public void run()
		{
			if( mDialog != null && mDialog.isShowing() )
			{
				//Log.v( "lvjiangbin" , "时间到" );
				mDialog.dismiss();
				Context pluginContext = FavoritesManager.getInstance().getPluginContext();
				CharSequence text = pluginContext.getResources().getText( R.string.locate_failed );
				Toast.makeText( FavoritesManager.getInstance().getContainerContext() , text , Toast.LENGTH_SHORT ).show();
			}
		}
	};
	
	@Override
	public void onCancel(
			DialogInterface dialog )
	{
		//Log.v( "lvjiangbin" , "onCancel" );
		if( mCooeeLocation != null )
			mCooeeLocation.stopLocation();
	}
	
	//cheyingkun add start	//解决“常用应用添加删除第一行时,推荐应用显示异常”的问题。（逻辑完善）
	public void showViewYLog()
	{
		Log.d( "" , "cyk FavoriteNearbyView: " + this.getY() + " h: " + this.getHeight() );
	}
	//cheyingkun add end
}
