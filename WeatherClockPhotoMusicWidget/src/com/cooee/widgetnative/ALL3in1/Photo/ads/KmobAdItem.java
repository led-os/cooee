package com.cooee.widgetnative.ALL3in1.Photo.ads;




public class KmobAdItem
{
	//	private static final String TAG = "KmobAdItem";
	//	private JSONArray mAdJsonArray;
	//	private ImageContainer mImageContainer;
	//	private ImageLoader mImageLoader;
	//	private Context mContext;
	//	private PhotoView mPhotoView;
	//	public static KmobAdData mClickTestData;
	//	private Map<String , KmobAdData> mAdInfoMap = null;
	//	private boolean isGetting = false;
	//	private BitmapCache mAdBmpCache;
	//	
	//	public KmobAdItem(
	//			Context context ,
	//			PhotoView photoView )
	//	{
	//		this.mContext = context;
	//		this.mPhotoView = photoView;
	//		RequestQueue mQueue = Volley.newRequestQueue( context.getApplicationContext() );
	//		mAdBmpCache = new BitmapCache( context );
	//		mImageLoader = new ImageLoader( mQueue , mAdBmpCache );
	//	}
	//	
	//	public void updateAdData(
	//			JSONArray array )
	//	{
	//		isGetting = true;
	//		if( mAdJsonArray == null )
	//		{
	//			mAdJsonArray = array;
	//		}
	//		else
	//		{
	//			if( array != null )
	//			{
	//				for( int index = 0 ; index < array.length() ; index++ )
	//				{
	//					try
	//					{
	//						mAdJsonArray.put( mAdJsonArray.length() , array.getJSONObject( index ) );
	//					}
	//					catch( JSONException e )
	//					{
	//						e.printStackTrace();
	//					}
	//				}
	//			}
	//		}
	//		try
	//		{
	//			addAdItem();
	//		}
	//		catch( JSONException e )
	//		{
	//			e.printStackTrace();
	//		}
	//		if( mAdInfoMap == null || mAdInfoMap.isEmpty() )
	//			isGetting = false;
	//	}
	//	
	//	public void addAdItem() throws JSONException
	//	{
	//		if( mAdJsonArray != null && mAdJsonArray.length() > 0 )
	//		{
	//			KmobAdData adData = null;
	//			for( int index = 0 ; index < mAdJsonArray.length() ; index++ )
	//			{
	//				adData = createNativeData( mAdJsonArray.getJSONObject( index ) );
	//				if( adData == null )
	//					continue;
	//				if( mAdInfoMap == null )
	//					mAdInfoMap = new HashMap<String , KmobAdData>();
	//				mAdInfoMap.put( adData.getHiimg() , adData );
	//				mImageLoader.get( adData.getHiimg() , new AdImageListener( adData.getHiimg() ) );
	//			}
	//			int length = mAdJsonArray.length();
	//			for( int index = 0 ; index < length ; index++ )
	//			{
	//				if( Build.VERSION.SDK_INT > 19 )
	//					mAdJsonArray.remove( 0 );
	//				else
	//					JSONArray_Remove( 0 , mAdJsonArray );
	//			}
	//		}
	//	}
	//	
	//	/**
	//	 * 通过广告传入的数据生成一个NativeAdData
	//	 * @param object
	//	 * @return
	//	 */
	//	private KmobAdData createNativeData(
	//			JSONObject object )
	//	{
	//		String summary = "";
	//		String headline = "";
	//		String adcategory = "";
	//		String appRating = "";
	//		String adlogo = "";
	//		String details = "";
	//		String adlogoWidth = "";
	//		String adlogoHeight = "";
	//		String review = "";
	//		String appinstalls = "";
	//		String download = "";
	//		String adplaceid = "";
	//		String adid = "";
	//		String clickurl = "";
	//		String interactiontype = "";
	//		String open_type = "";
	//		String hurl = "";
	//		String hdetailurl = "";
	//		String pkgname = "";
	//		String appsize = "";
	//		String version = "";
	//		String versionname = "";
	//		String ctimg = "";
	//		String hiimg = "";
	//		String click_record_url = "";
	//		try
	//		{
	//			if( object.has( NativeAdData.SUMMARY_TAG ) )
	//			{
	//				summary = object.getString( NativeAdData.SUMMARY_TAG );
	//			}
	//			if( object.has( NativeAdData.HEADLINE_TAG ) )
	//			{
	//				headline = object.getString( NativeAdData.HEADLINE_TAG );
	//			}
	//			if( object.has( NativeAdData.ADCATEGORY_TAG ) )
	//			{
	//				adcategory = object.getString( NativeAdData.ADCATEGORY_TAG );
	//			}
	//			if( object.has( NativeAdData.APPRATING_TAG ) )
	//			{
	//				appRating = object.getString( NativeAdData.APPRATING_TAG );
	//			}
	//			if( object.has( NativeAdData.ADLOGO_TAG ) )
	//			{
	//				adlogo = object.getString( NativeAdData.ADLOGO_TAG );
	//			}
	//			if( object.has( NativeAdData.DETAILS_TAG ) )
	//			{
	//				details = object.getString( NativeAdData.DETAILS_TAG );
	//			}
	//			if( object.has( NativeAdData.ADLOGO_WIDTH_TAG ) )
	//			{
	//				adlogoWidth = object.getString( NativeAdData.ADLOGO_WIDTH_TAG );
	//			}
	//			if( object.has( NativeAdData.ADLOGO_HEIGHT_TAG ) )
	//			{
	//				adlogoHeight = object.getString( NativeAdData.ADLOGO_HEIGHT_TAG );
	//			}
	//			if( object.has( NativeAdData.REVIEW_TAG ) )
	//			{
	//				review = object.getString( NativeAdData.REVIEW_TAG );
	//			}
	//			if( object.has( NativeAdData.APPINSTALLS_TAG ) )
	//			{
	//				appinstalls = object.getString( NativeAdData.APPINSTALLS_TAG );
	//			}
	//			if( object.has( NativeAdData.DOWNLOAD_TAG ) )
	//			{
	//				download = object.getString( NativeAdData.DOWNLOAD_TAG );
	//			}
	//			if( object.has( NativeAdData.ADPLACE_ID_TAG ) )
	//			{
	//				adplaceid = object.getString( NativeAdData.ADPLACE_ID_TAG );
	//			}
	//			if( object.has( NativeAdData.AD_ID_TAG ) )
	//			{
	//				adid = object.getString( NativeAdData.AD_ID_TAG );
	//			}
	//			if( object.has( NativeAdData.CLICKURL_TAG ) )
	//			{
	//				clickurl = object.getString( NativeAdData.CLICKURL_TAG );
	//			}
	//			if( object.has( NativeAdData.INTERACTION_TYPE_TAG ) )
	//			{
	//				interactiontype = object.getString( NativeAdData.INTERACTION_TYPE_TAG );
	//			}
	//			if( object.has( NativeAdData.OPEN_TYPE_TAG ) )
	//			{
	//				open_type = object.getString( NativeAdData.OPEN_TYPE_TAG );
	//			}
	//			if( object.has( NativeAdData.HURL_TAG ) )
	//			{
	//				hurl = object.getString( NativeAdData.HURL_TAG );
	//			}
	//			if( object.has( NativeAdData.HDETAILURL_TAG ) )
	//			{
	//				hdetailurl = object.getString( NativeAdData.HDETAILURL_TAG );
	//			}
	//			if( object.has( NativeAdData.PKGNAME_TAG ) )
	//			{
	//				pkgname = object.getString( NativeAdData.PKGNAME_TAG );
	//			}
	//			if( object.has( NativeAdData.APPSIZE_TAG ) )
	//			{
	//				appsize = object.getString( NativeAdData.APPSIZE_TAG );
	//			}
	//			if( object.has( NativeAdData.VERSION_TAG ) )
	//			{
	//				version = object.getString( NativeAdData.VERSION_TAG );
	//			}
	//			if( object.has( NativeAdData.VERSIONNAME_TAG ) )
	//			{
	//				versionname = object.getString( NativeAdData.VERSIONNAME_TAG );
	//			}
	//			if( object.has( NativeAdData.CTIMG_TAG ) )
	//			{
	//				String temp = object.getString( NativeAdData.CTIMG_TAG );
	//				JSONArray array = new JSONArray( temp );
	//				if( array != null && array.length() > 0 )
	//				{
	//					ctimg = array.getJSONObject( 0 ).getString( "url" );
	//				}
	//			}
	//			if( object.has( NativeAdData.HIIMG_TAG ) )
	//			{
	//				String temp = object.getString( NativeAdData.HIIMG_TAG );
	//				JSONArray array = new JSONArray( temp );
	//				if( array != null && array.length() > 0 )
	//				{
	//					hiimg = array.getJSONObject( 0 ).getString( "url" );
	//				}
	//			}
	//			if( object.has( NativeAdData.CLICK_RECORD_URL_TAG ) )
	//			{
	//				click_record_url = object.getString( NativeAdData.CLICK_RECORD_URL_TAG );
	//			}
	//			return new KmobAdData(
	//					summary ,
	//					headline ,
	//					adcategory ,
	//					appRating ,
	//					adlogo ,
	//					details ,
	//					adlogoWidth ,
	//					adlogoHeight ,
	//					review ,
	//					appinstalls ,
	//					download ,
	//					adplaceid ,
	//					adid ,
	//					clickurl ,
	//					interactiontype ,
	//					open_type ,
	//					hurl ,
	//					hdetailurl ,
	//					pkgname ,
	//					appsize ,
	//					version ,
	//					versionname ,
	//					ctimg ,
	//					hiimg ,
	//					click_record_url );
	//		}
	//		catch( Exception e )
	//		{
	//			Log.e( "KMOB" , "addAdView e " + e.toString() );
	//		}
	//		return null;
	//	}
	//	
	//	private void JSONArray_Remove(
	//			int index ,
	//			JSONArray array )
	//	{
	//		if( index < 0 )
	//			return;
	//		Field valuesField;
	//		try
	//		{
	//			valuesField = JSONArray.class.getDeclaredField( "values" );
	//			valuesField.setAccessible( true );
	//			List<Object> values = (List<Object>)valuesField.get( array );
	//			if( index >= values.size() )
	//				return;
	//			values.remove( index );
	//		}
	//		catch( NoSuchFieldException e )
	//		{
	//			e.printStackTrace();
	//		}
	//		catch( IllegalAccessException e )
	//		{
	//			e.printStackTrace();
	//		}
	//		catch( IllegalArgumentException e )
	//		{
	//			e.printStackTrace();
	//		}
	//	}
	//	
	//	/**
	//	 * Loads the image for the view if it isn't already loaded.
	//	 * @param isInLayoutPass True if this was invoked from a layout pass, false otherwise.
	//	 */
	//	private void loadImageIfNecessary(
	//			String url )
	//	{
	//		// if the URL to be loaded in this view is empty, cancel any old requests and clear the
	//		// currently loaded image.
	//		if( TextUtils.isEmpty( url ) )
	//		{
	//			if( mImageContainer != null )
	//			{
	//				mImageContainer.cancelRequest();
	//				mImageContainer = null;
	//			}
	//			return;
	//		}
	//		// if there was an old request in this view, check if it needs to be canceled.
	//		if( mImageContainer != null && mImageContainer.getRequestUrl() != null )
	//		{
	//			if( mImageContainer.getRequestUrl().equals( url ) )
	//			{
	//				// if the request is from the same URL, return.
	//				return;
	//			}
	//			else
	//			{
	//				// if there is a pre-existing request, cancel it if it's fetching a different URL.
	//				mImageContainer.cancelRequest();
	//			}
	//		}
	//		// The pre-existing content of this view didn't match the current URL. Load the new image
	//		// from the network.
	//		ImageContainer newContainer = mImageLoader.get( url , new AdImageListener( url ) );
	//		// update the ImageContainer to be the new bitmap container.
	//		mImageContainer = newContainer;
	//	}
	//	
	//	private class AdImageListener implements ImageListener
	//	{
	//		
	//		private String adUrl = null;
	//		
	//		public AdImageListener(
	//				String url )
	//		{
	//			Log.i( TAG , "AdImageListener url = " + url );
	//			this.adUrl = url;
	//		}
	//		
	//		@Override
	//		public void onErrorResponse(
	//				VolleyError error )
	//		{
	//			if( mAdInfoMap != null )
	//			{
	//				mAdInfoMap.remove( this.adUrl );
	//				if( mAdInfoMap.isEmpty() )
	//					isGetting = false;
	//			}
	//			Log.e( TAG , error.getMessage() , error );
	//		}
	//		
	//		@Override
	//		public void onResponse(
	//				final ImageContainer response ,
	//				boolean isImmediate )
	//		{
	//			// If this was an immediate response that was delivered inside of a layout
	//			// pass do not set the image immediately as it will trigger a requestLayout
	//			// inside of a layout. Instead, defer setting the image by posting back to
	//			// the main thread.
	//			Bitmap responseBmp = response.getBitmap();
	//			if( responseBmp != null && mAdInfoMap != null )
	//			{
	//				Bitmap minbmp = ThumbnailUtils.extractThumbnail( responseBmp , (int)PhotoView.WIDGET_WIDTH , (int)PhotoView.WIDGET_HEIGHT );
	//				addAdInfoToList( response.getRequestUrl() , minbmp );
	//			}
	//			if( mAdInfoMap == null || mAdInfoMap.isEmpty() )
	//				isGetting = false;
	//		}
	//	}
	//	
	//	private void addAdInfoToList(
	//			String key ,
	//			Bitmap adImg )
	//	{
	//		if( mAdInfoMap != null && mAdInfoMap.get( key ) != null )
	//		{
	//			KmobAdData adData = mAdInfoMap.get( key );
	//			adData.setAdHiimg( adImg );
	//			List<KmobAdData> adList = mPhotoView.getmCurAdList();
	//			adList.add( adData );
	//			mAdInfoMap.remove( key );
	//		}
	//	}
	//	
	//	public boolean isGetting()
	//	{
	//		return isGetting;
	//	}
	//	
	//	public void onDestroy()
	//	{
	//		if( mAdInfoMap != null )
	//		{
	//			KmobAdData adData = null;
	//			Set<String> keys = mAdInfoMap.keySet();
	//			for( Iterator<String> iterator = keys.iterator() ; iterator.hasNext() ; )
	//			{
	//				String key = (String)iterator.next();
	//				adData = mAdInfoMap.get( key );
	//				if( adData != null && adData.getAdHiimg() != null )
	//				{
	//					adData.getAdHiimg().recycle();
	//				}
	//			}
	//			mAdInfoMap.clear();
	//			mAdInfoMap = null;
	//		}
	//		/*if( mAdBmpCache != null )
	//		{
	//			mAdBmpCache.onDestroy();
	//		}*/
	//	}
}
