package com.cooee.search;


import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


public class SearchHotWords
{
	
	private final String TAG = "SearchHotWords";
	private ArrayList<String> mHotWordList = new ArrayList<String>();
	private final String URL = "http://nanohome.cn/get_keywords/geo_getcitywords.php";
	private RequestQueue mQueue;
	private Callbacks mCallbacks;
	private int mCount = 0;
	private final int MESSAGE_UPDATE_WORD = 0;//通知更新
	private final int MESSAGE_REQUEST_HOT_WORDS = 1;//热词是两小时更新一次，每次更新100个
	private long mUpdateDelta = 72 * 1000;//界面上热词更新的时间间隔
	private long mRequestDelta = 120 * 60 * 1000;//请求更新的时间间隔
	private Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(
				Message msg )
		{
			// TODO Auto-generated method stub
			if( msg.what == MESSAGE_UPDATE_WORD )
			{
				String words = getHotWords();
				if( mCallbacks != null && words != null )
				{
					mCallbacks.updateHotWords( words );
				}
				mHandler.sendEmptyMessageDelayed( MESSAGE_UPDATE_WORD , mUpdateDelta );
			}
			else if( msg.what == MESSAGE_REQUEST_HOT_WORDS )
			{
				requestHotWords();
				mHandler.sendEmptyMessageDelayed( MESSAGE_REQUEST_HOT_WORDS , mRequestDelta );//每隔两小时去服务器更新一次热词
			}
		}
	};
	
	public SearchHotWords(
			RequestQueue queue )
	{
		mQueue = queue;
		mHandler.sendEmptyMessage( MESSAGE_REQUEST_HOT_WORDS );
	}
	
	public SearchHotWords(
			Context context )
	{
		mQueue = Volley.newRequestQueue( context.getApplicationContext() );
		mHandler.sendEmptyMessage( MESSAGE_REQUEST_HOT_WORDS );
	}
	
	public void setCallbacks(
			Callbacks cb )
	{
		this.mCallbacks = cb;
	}
	
	public synchronized void requestHotWords()
	{
		Log.i( TAG , "postEntity" );
		JsonObjectRequest request = new JsonObjectRequest( URL , new Response.Listener<JSONObject>() {
			
			@Override
			public void onResponse(
					JSONObject reponse )
			{
				if( reponse != null )
				{
					try
					{
						mCount = 0;
						JSONArray array = reponse.getJSONArray( "keyword" );
						mHotWordList.clear();
						for( int i = 0 ; i < array.length() ; i++ )
						{
							mHotWordList.add( array.getString( i ) );
						}
						if( mHandler.hasMessages( MESSAGE_UPDATE_WORD ) )//热词获取到，通知界面更新
						{
							mHandler.removeMessages( MESSAGE_UPDATE_WORD );
						}
						mHandler.sendEmptyMessage( MESSAGE_UPDATE_WORD );
					}
					catch( JSONException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} , new Response.ErrorListener() {
			
			@Override
			public void onErrorResponse(
					VolleyError error )
			{
				Log.v( TAG , "search hotword response error " + error.getMessage() , error );
				if( mCount < 3 )//不成功的话，再次请求，但不超过三次
				{
					mCount++;
					requestHotWords();
				}
			}
		} );
		mQueue.add( request );
	}
	
	public int getHotWordsSize()
	{
		return mHotWordList.size();
	}
	
	public String getHotWords()
	{
		if( mHotWordList.size() == 0 )
		{
			return null;
		}
		Random random = new Random( System.currentTimeMillis() );
		int index = random.nextInt( mHotWordList.size() );
		return mHotWordList.get( index );
	}
	
	public void setUpdateDelta(
			long delta )
	{
		mUpdateDelta = delta;
	}
	
	public void setRequestDelta(
			long delta )
	{
		mRequestDelta = delta;
	}
	
	public interface Callbacks
	{
		
		public void updateHotWords(
				String words );
	}
}
