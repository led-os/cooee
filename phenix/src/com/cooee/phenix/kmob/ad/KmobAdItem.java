package com.cooee.phenix.kmob.ad;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.Volley;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.kmob.kmobsdk.NativeAdData;


public class KmobAdItem
{
	
	private static final String TAG = "KmobAdItem";
	private JSONArray mAdJsonArray;
	private ImageContainer mImageContainer;
	private ImageLoader mImageLoader;
	private Context mContext;
	public static KmobAdData mClickTestData;
	private List<String> mAdUrlList = null;
	private List<KmobAdData> mAdList = null;
	private boolean isGetting = false;
	private BitmapCache mAdBmpCache;
	private IKmobCallback mAdCallback;
	
	public KmobAdItem(
			Context context ,
			IKmobCallback adCallback )
	{
		this.mContext = context;
		this.mAdCallback = adCallback;
		RequestQueue mQueue = Volley.newRequestQueue( context.getApplicationContext() );
		mAdBmpCache = new BitmapCache( context );
		mImageLoader = new ImageLoader( mQueue , mAdBmpCache );
	}
	
	public void updateAdData(
			JSONArray array )
	{
		isGetting = true;
		if( mAdJsonArray == null )
		{
			mAdJsonArray = array;
		}
		else
		{
			if( array != null )
			{
				for( int index = 0 ; index < array.length() ; index++ )
				{
					try
					{
						mAdJsonArray.put( mAdJsonArray.length() , array.getJSONObject( index ) );
					}
					catch( JSONException e )
					{
						e.printStackTrace();
					}
				}
			}
		}
		try
		{
			addAdItem();
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}
	}
	
	public void addAdItem() throws JSONException
	{
		if( mAdJsonArray != null && mAdJsonArray.length() > 0 )
		{
			KmobAdData adData = null;
			if( mAdList == null )
			{
				mAdList = new ArrayList<KmobAdData>();
			}
			else
			{
				mAdList.clear();
			}
			for( int index = 0 ; index < mAdJsonArray.length() ; index++ )
			{
				adData = createNativeData( mAdJsonArray.getJSONObject( index ) );
				if( adData == null )
					continue;
				mAdList.add( adData );
			}
			mAdCallback.loadAdDataFinish( mAdList );
			int length = mAdJsonArray.length();
			for( int index = 0 ; index < length ; index++ )
			{
				if( Build.VERSION.SDK_INT > 19 )
					mAdJsonArray.remove( 0 );
				else
					JSONArray_Remove( 0 , mAdJsonArray );
			}
		}
	}
	
	/*public void getAdBitmap(
			String url )
	{
		//Log.i( "AlbumFrontView" , "KmobAdItem getAdBitmap url = " + url );
		mImageLoader.get( url , new AdImageListener( url ) );
	}*/
	public void getAdBitmap(
			List<String> urlList )
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "KmobAdItem getAdBitmap urlList.size = " , urlList.size() ) );
		mAdUrlList = urlList;
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( TAG , StringUtils.concat( "KmobAdItem getAdBitmap mAdUrlList.size = " , mAdUrlList.size() ) );
		List<String> tempList = new ArrayList<String>();
		for( int i = 0 ; i < urlList.size() ; i++ )
		{
			tempList.add( urlList.get( i ) );
		}
		for( int i = 0 ; i < tempList.size() ; i++ )
		{
			String url = tempList.get( i );
			mImageLoader.get( url , new AdImageListener( url ) );
		}
	}
	
	/**
	 * 通过广告传入的数据生成一个NativeAdData
	 * @param object
	 * @return
	 */
	private KmobAdData createNativeData(
			JSONObject object )
	{
		String summary = "";
		String headline = "";
		String adcategory = "";
		String appRating = "";
		String adlogo = "";
		String details = "";
		String adlogoWidth = "";
		String adlogoHeight = "";
		String review = "";
		String appinstalls = "";
		String download = "";
		String adplaceid = "";
		String adid = "";
		String clickurl = "";
		String interactiontype = "";
		String open_type = "";
		String hurl = "";
		String hdetailurl = "";
		String pkgname = "";
		String appsize = "";
		String version = "";
		String versionname = "";
		String ctimg = "";
		String hiimg = "";
		String click_record_url = "";
		try
		{
			if( object.has( NativeAdData.SUMMARY_TAG ) )
			{
				summary = object.getString( NativeAdData.SUMMARY_TAG );
			}
			if( object.has( NativeAdData.HEADLINE_TAG ) )
			{
				headline = object.getString( NativeAdData.HEADLINE_TAG );
			}
			if( object.has( NativeAdData.ADCATEGORY_TAG ) )
			{
				adcategory = object.getString( NativeAdData.ADCATEGORY_TAG );
			}
			if( object.has( NativeAdData.APPRATING_TAG ) )
			{
				appRating = object.getString( NativeAdData.APPRATING_TAG );
			}
			if( object.has( NativeAdData.ADLOGO_TAG ) )
			{
				adlogo = object.getString( NativeAdData.ADLOGO_TAG );
			}
			if( object.has( NativeAdData.DETAILS_TAG ) )
			{
				details = object.getString( NativeAdData.DETAILS_TAG );
			}
			if( object.has( NativeAdData.ADLOGO_WIDTH_TAG ) )
			{
				adlogoWidth = object.getString( NativeAdData.ADLOGO_WIDTH_TAG );
			}
			if( object.has( NativeAdData.ADLOGO_HEIGHT_TAG ) )
			{
				adlogoHeight = object.getString( NativeAdData.ADLOGO_HEIGHT_TAG );
			}
			if( object.has( NativeAdData.REVIEW_TAG ) )
			{
				review = object.getString( NativeAdData.REVIEW_TAG );
			}
			if( object.has( NativeAdData.APPINSTALLS_TAG ) )
			{
				appinstalls = object.getString( NativeAdData.APPINSTALLS_TAG );
			}
			if( object.has( NativeAdData.DOWNLOAD_TAG ) )
			{
				download = object.getString( NativeAdData.DOWNLOAD_TAG );
			}
			if( object.has( NativeAdData.ADPLACE_ID_TAG ) )
			{
				adplaceid = object.getString( NativeAdData.ADPLACE_ID_TAG );
			}
			if( object.has( NativeAdData.AD_ID_TAG ) )
			{
				adid = object.getString( NativeAdData.AD_ID_TAG );
			}
			if( object.has( NativeAdData.CLICKURL_TAG ) )
			{
				clickurl = object.getString( NativeAdData.CLICKURL_TAG );
			}
			if( object.has( NativeAdData.INTERACTION_TYPE_TAG ) )
			{
				interactiontype = object.getString( NativeAdData.INTERACTION_TYPE_TAG );
			}
			if( object.has( NativeAdData.OPEN_TYPE_TAG ) )
			{
				open_type = object.getString( NativeAdData.OPEN_TYPE_TAG );
			}
			if( object.has( NativeAdData.HURL_TAG ) )
			{
				hurl = object.getString( NativeAdData.HURL_TAG );
			}
			if( object.has( NativeAdData.HDETAILURL_TAG ) )
			{
				hdetailurl = object.getString( NativeAdData.HDETAILURL_TAG );
			}
			if( object.has( NativeAdData.PKGNAME_TAG ) )
			{
				pkgname = object.getString( NativeAdData.PKGNAME_TAG );
			}
			if( object.has( NativeAdData.APPSIZE_TAG ) )
			{
				appsize = object.getString( NativeAdData.APPSIZE_TAG );
			}
			if( object.has( NativeAdData.VERSION_TAG ) )
			{
				version = object.getString( NativeAdData.VERSION_TAG );
			}
			if( object.has( NativeAdData.VERSIONNAME_TAG ) )
			{
				versionname = object.getString( NativeAdData.VERSIONNAME_TAG );
			}
			if( object.has( NativeAdData.CTIMG_TAG ) )
			{
				String temp = object.getString( NativeAdData.CTIMG_TAG );
				JSONArray array = new JSONArray( temp );
				if( array != null && array.length() > 0 )
				{
					ctimg = array.getJSONObject( 0 ).getString( "url" );
				}
			}
			if( object.has( NativeAdData.HIIMG_TAG ) )
			{
				String temp = object.getString( NativeAdData.HIIMG_TAG );
				JSONArray array = new JSONArray( temp );
				if( array != null && array.length() > 0 )
				{
					hiimg = array.getJSONObject( 0 ).getString( "url" );
				}
			}
			if( object.has( NativeAdData.CLICK_RECORD_URL_TAG ) )
			{
				click_record_url = object.getString( NativeAdData.CLICK_RECORD_URL_TAG );
			}
			return new KmobAdData(
					summary ,
					headline ,
					adcategory ,
					appRating ,
					adlogo ,
					details ,
					adlogoWidth ,
					adlogoHeight ,
					review ,
					appinstalls ,
					download ,
					adplaceid ,
					adid ,
					clickurl ,
					interactiontype ,
					open_type ,
					hurl ,
					hdetailurl ,
					pkgname ,
					appsize ,
					version ,
					versionname ,
					ctimg ,
					hiimg ,
					click_record_url ,
					object.toString() );
		}
		catch( Exception e )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( "KMOB" , StringUtils.concat( "addAdView e " , e.toString() ) );
		}
		return null;
	}
	
	private void JSONArray_Remove(
			int index ,
			JSONArray array )
	{
		if( index < 0 )
			return;
		Field valuesField;
		try
		{
			valuesField = JSONArray.class.getDeclaredField( "values" );
			valuesField.setAccessible( true );
			List<Object> values = (List<Object>)valuesField.get( array );
			if( index >= values.size() )
				return;
			values.remove( index );
		}
		catch( NoSuchFieldException e )
		{
			e.printStackTrace();
		}
		catch( IllegalAccessException e )
		{
			e.printStackTrace();
		}
		catch( IllegalArgumentException e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the image for the view if it isn't already loaded.
	 * @param isInLayoutPass True if this was invoked from a layout pass, false otherwise.
	 */
	private void loadImageIfNecessary(
			String url )
	{
		// if the URL to be loaded in this view is empty, cancel any old requests and clear the
		// currently loaded image.
		if( TextUtils.isEmpty( url ) )
		{
			if( mImageContainer != null )
			{
				mImageContainer.cancelRequest();
				mImageContainer = null;
			}
			return;
		}
		// if there was an old request in this view, check if it needs to be canceled.
		if( mImageContainer != null && mImageContainer.getRequestUrl() != null )
		{
			if( mImageContainer.getRequestUrl().equals( url ) )
			{
				// if the request is from the same URL, return.
				return;
			}
			else
			{
				// if there is a pre-existing request, cancel it if it's fetching a different URL.
				mImageContainer.cancelRequest();
			}
		}
		// The pre-existing content of this view didn't match the current URL. Load the new image
		// from the network.
		ImageContainer newContainer = mImageLoader.get( url , new AdImageListener( url ) );
		// update the ImageContainer to be the new bitmap container.
		mImageContainer = newContainer;
	}
	
	private class AdImageListener implements ImageListener
	{
		
		private String adUrl = null;
		
		public AdImageListener(
				String url )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "AdImageListener url = " , url ) );
			this.adUrl = url;
		}
		
		@Override
		public void onErrorResponse(
				VolleyError error )
		{
			if( mAdUrlList != null )
			{
				mAdUrlList.remove( this.adUrl );
				if( mAdUrlList.isEmpty() )
					isGetting = false;
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.e( TAG , error.getMessage() , error );
		}
		
		@Override
		public void onResponse(
				final ImageContainer response ,
				boolean isImmediate )
		{
			// If this was an immediate response that was delivered inside of a layout
			// pass do not set the image immediately as it will trigger a requestLayout
			// inside of a layout. Instead, defer setting the image by posting back to
			// the main thread.
			//Log.i( TAG , "response = " + response );
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( "TAG" , "KmobAdItem onResponse!! " );
			Bitmap responseBmp = response.getBitmap();
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "response.getRequestUrl()  =  " , response.getRequestUrl() ) );
			if( responseBmp != null )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.i( TAG , "-------onResponse()-------2 " );
				mAdCallback.loadAdBmpFinish( responseBmp , response.getRequestUrl() );
				if( mAdUrlList != null )
				{
					mAdUrlList.remove( response.getRequestUrl() );
					if( mAdUrlList.isEmpty() )
						isGetting = false;
				}
			}
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.i( TAG , StringUtils.concat( "mAdUrlList.size =  " , mAdUrlList.size() , "; isGetting = " , isGetting , "; KmobAdItem = " , KmobAdItem.this ) );
		}
	}
	
	public boolean isGetting()
	{
		return isGetting;
	}
	
	public void onDestroy()
	{
		if( mAdUrlList != null )
		{
			mAdUrlList.clear();
			mAdUrlList = null;
		}
		if( mAdList != null )
		{
			for( int i = 0 ; i < mAdList.size() ; i++ )
			{
				if( mAdList.get( i ).getAdHiimg() != null )
				{
					mAdList.get( i ).getAdHiimg().recycle();
				}
			}
			mAdList.clear();
			mAdList = null;
		}
	}
}
