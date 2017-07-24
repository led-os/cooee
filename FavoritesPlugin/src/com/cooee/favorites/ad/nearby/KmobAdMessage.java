package com.cooee.favorites.ad.nearby;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.cooee.favorites.FavoriteConfigString;
import com.cooee.favorites.R;
import com.cooee.favorites.manager.FavoritesManager;
import com.cooee.uniex.wrap.FavoritesConfig;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;


public class KmobAdMessage
{
	
	private static final String TAG = "KmobAdMessage";
	public static String[] adplaceId = null;
	//	public static final String adTwo = "20151223111241281";//桌面广告id
	public static final int MSG_DOWNLOAD_IMAG_SUCEESS = 0; //下载图片成功
	public static final int MSG_DOWNLOAD_IMAG_FAILURE = 1;//下载图片失败
	public static final int MSG_REQUEST_DATA_FAILURE = 2;//请求数据失败
	public static final int MSG_REFRESH_HAS_DATA = 3;//有数据刷新时立马刷新
	public static final int MSG_REFRESH_DATA_SUCEESS = 4;//有数据刷新时立马刷新
	public static final int MSG_FRESHAPP_COUNT = 4;//桌面FreshApp广告显示数量
	public static final int MSG_FRESHSUGGESTION_COUNT = 4;//负一屏新潮推荐广告显示数量
	public static final String AD_GET_LAST_TIME_KEY = "ad_get_last_time_key";
	public static final String AD_GET_DELAY_TIME_KEY = "ad_get_delay_time_key";
	public static final String AD_GET_FIRST = "ad_get_first";
	/**
	* 原生广告
	*/
	private static AdBaseView[] mNativeViewArray = new AdBaseView[4];
	
	/**
	 * 
	 * @param context
	 * @param queue 
	 * @param adPlaceId 广告位id
	 * @param handler
	 * @param isActivity 是否为FreshApp
	 * @param adCount 广告数量
	 */
	public static void getKmobMessage(
			final Context context ,
			final RequestQueue queue ,
			final Handler handler ,
			final int index )
	{
		handler.postDelayed( new Runnable() {
			
			@Override
			public void run()
			{
				Log.w( TAG , "NativeAdActivity getKmobMessage index: " + index );
				if( mNativeViewArray[index] != null )
					mNativeViewArray[index].onDestroy();
				mNativeViewArray[index] = KmobManager.createNative( adplaceId[index] , context , 1 );
				mNativeViewArray[index].addAdViewListener( new AdViewListener() {
					
					@Override
					public void onAdReady(
							final String arg0 )
					{
						Log.d( TAG , "NativeAdActivity getKmobMessage onAdReady index: " + index + " arg0: " + arg0 );
						JSONArray array = null;
						if( arg0 != null )//NativeAdData
						{
							try
							{
								array = new JSONArray( arg0 );
							}
							catch( JSONException e )
							{
								// TODO Auto-generated catch block
								try
								{
									JSONObject obj = new JSONObject( arg0 );//后台返回的只有1个广告时，返回的是jasonobject，不是jasonarray
									array = new JSONArray();
									array.put( obj );
								}
								catch( JSONException e1 )
								{
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							int count = array.length();
							if( count > 0 )
							{
								ImageRequest request = null;
								try
								{
									final JSONObject obj = array.getJSONObject( 0 );
									final NearbyAdInfo item = new NearbyAdInfo();
									request = new ImageRequest( obj.getString( item.getAdlogo() ) , new Response.Listener<Bitmap>() {
										
										@Override
										public void onResponse(
												final Bitmap response )
										{
											new Thread( new Runnable() {
												
												@Override
												public void run()
												{
													//													FavoritesManager.getInstance().deleteFavoritesAdFromDatabase( context , index + 1 );
													//													Log.w( TAG , "deleteFavoritesAdFromDatabase id_ = : " + index + 1 );
													Bitmap resizedBitmap = generateQuickContactIcon( response );
													try
													{
														if( FavoritesManager.getInstance().isAdDatabaseExit( context , obj.getString( item.getAdplaceid() ) ) )
														{
															FavoritesManager.getInstance().updateFavoritesAdToDatabase(
																	context ,
																	obj.getString( item.getHeadline() ) ,
																	adplaceId[index] ,
																	obj.getString( item.getAdid() ) ,
																	resizedBitmap ,
																	arg0 );
														}
														else
														{
															FavoritesManager.getInstance().addFavoritesAdToDatabase(
																	context ,
																	obj.getString( item.getHeadline() ) ,
																	adplaceId[index] ,
																	obj.getString( item.getAdid() ) ,
																	resizedBitmap ,
																	arg0 );
														}
														Message message = new Message();
														message.what = MSG_DOWNLOAD_IMAG_SUCEESS;
														message.arg1 = index;
														message.obj = obj.getString( item.getAdplaceid() );
														handler.sendMessage( message );
													}
													catch( JSONException e )
													{
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
												}
											} ).start();
										}
									} , 0 , 0 , null , Bitmap.Config.RGB_565 , new Response.ErrorListener() {
										
										public void onErrorResponse(
												VolleyError arg0 )
										{
											handler.sendEmptyMessage( MSG_DOWNLOAD_IMAG_FAILURE );
										};
									} );
								}
								catch( JSONException e )
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								queue.add( request );
							}
						}
						else
						{
							handler.sendEmptyMessage( MSG_REQUEST_DATA_FAILURE );
						}
					}
					
					@Override
					public void onAdFailed(
							String arg0 )
					{
						handler.sendEmptyMessage( MSG_REQUEST_DATA_FAILURE );
						Log.w( TAG , "NativeAdActivity onAdFailed info: " + arg0 );
					}
					
					@Override
					public void onAdClose(
							String arg0 )
					{
						Log.d( TAG , "NativeAdActivity getKmobMessage onAdClose index: " + index + " arg0: " + arg0 );
					}
					
					@Override
					public void onAdClick(
							String arg0 )
					{
						Log.d( TAG , "NativeAdActivity getKmobMessage onAdClick index: " + index + " arg0: " + arg0 );
					}
					
					@Override
					public void onAdCancel(
							String arg0 )
					{
						Log.d( TAG , "NativeAdActivity getKmobMessage onAdCancel index: " + index + " arg0: " + arg0 );
					}
					
					@Override
					public void onAdShow(
							String info )
					{
						Log.d( TAG , "NativeAdActivity getKmobMessage onAdShow index: " + index + " info: " + info );
					}
				} );
			}
		} ,
				5000 );
	}
	
	private static Bitmap generateQuickContactIcon(
			Bitmap bitmapSrc )
	{
		Log.d( "fav" , "generateQuickContactIcon" );
		Context context = FavoritesManager.getInstance().getPluginContext();
		BitmapDrawable drawable = (BitmapDrawable)context.getResources().getDrawable( R.drawable.favorites_contact_mask );
		FavoritesConfig config = FavoritesManager.getInstance().getConfig();
		int mIconSize = config.getInt( FavoriteConfigString.getLauncherIconSizePxKey() , FavoriteConfigString.getLauncherIconSizePxDefaultValue() );
		// Setup the drawing classes
		Bitmap bitmap = Bitmap.createBitmap( mIconSize , mIconSize , Bitmap.Config.ARGB_8888 );
		Canvas canvas = new Canvas( bitmap );
		Paint paint = new Paint();
		Bitmap photo;
		Bitmap mask;
		Bitmap tempBitmap = bitmapSrc;
		if( tempBitmap == null || tempBitmap.isRecycled() )
		{
			return null;
		}
		photo = Bitmap.createScaledBitmap( tempBitmap , mIconSize , mIconSize , true );
		mask = Bitmap.createScaledBitmap( drawable.getBitmap() , mIconSize , mIconSize , true );
		canvas.drawBitmap( photo , 0 , 0 , paint );
		paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.DST_IN ) );
		canvas.drawBitmap( mask , 0 , 0 , paint );
		paint.setXfermode( null );
		if( tempBitmap != photo && !photo.isRecycled() )
		{
			photo.recycle();
			photo = null;
		}
		if( drawable.getBitmap() != mask && !mask.isRecycled() )
		{
			mask.recycle();
			mask = null;
		}
		//		if( !bitmap.isRecycled() )
		//		{
		//			bitmap.recycle();
		//			bitmap = null;
		//		}
		Log.d( "fav" , "generateQuickContactIcon bitmap =  " + bitmap );
		return bitmap;
	}
	
	static public void setAdPlaceId(
			String[] id )
	{
		adplaceId = id;
	}
}
