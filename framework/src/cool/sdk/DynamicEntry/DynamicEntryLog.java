package cool.sdk.DynamicEntry;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import cool.sdk.log.LogHelper;


public class DynamicEntryLog
{
	
	private static final String ACTION_CONFIG_COMPLETE = "3305";
	private static final String ACTION_ENTRY_CLICK = "3306";
	private static final String ACTION_DELETE = "3307";
	private static final String ACTION_ACTIVE = "3308";
	
	public synchronized static void LogConfigComplete(
			Context context ,
			String list_time )
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put( "Action" , ACTION_CONFIG_COMPLETE );
			json.put( "h12" , DynamicEntry.h12 );
			json.put( "h13" , DynamicEntry.h13 );
			json.put( "p1" , list_time );
		}
		catch( JSONException e )
		{
		}
		LogHelper.Item( context , json , null );
	}
	
	private static String keyLogDynamicEntryClick = "LogDynamicEntryClick";
	
	private static List<String> getLogDynamicEntryClickJsonList(
			Context context )
	{
		List<String> arrayList = new ArrayList<String>();
		String jsonList = DynamicEntryHelper.getInstance( context ).getString( keyLogDynamicEntryClick );
		if( jsonList == null )
		{
			return arrayList;
		}
		try
		{
			JSONArray jsonArray = new JSONArray( jsonList );
			for( int i = 0 ; i < jsonArray.length() ; i++ )
			{
				arrayList.add( jsonArray.get( i ).toString() );
			}
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arrayList;
	}
	
	private static void setLogDynamicEntryClickJsonList(
			Context context ,
			List<String> jsonList )
	{
		try
		{
			JSONArray jsonArray = new JSONArray();
			for( String item : jsonList )
			{
				jsonArray.put( new JSONObject( item ) );
			}
			DynamicEntryHelper.getInstance( context ).setValue( keyLogDynamicEntryClick , jsonArray.toString() );
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized static void applyLogDynamicEntryClickArray(
			Context context )
	{
		try
		{
			String str = DynamicEntryHelper.getInstance( context ).getString( keyLogDynamicEntryClick , "[]" );
			JSONArray jsonArray = new JSONArray( str );
			JSONArray resultArray = new JSONArray();
			for( int i = 0 ; i < jsonArray.length() ; i++ )
			{
				JSONObject json = jsonArray.getJSONObject( i );
				long times = DynamicEntryHelper.getInstance( context ).getLong( json.toString() , 0L );
				if( times > 0 )
				{
					DynamicEntryHelper.getInstance( context ).setValue( json.toString() , (Long)null );
					json.put( "p3" , times );
					resultArray.put( json );
				}
			}
			if( resultArray.length() > 0 )
			{
				LogHelper.Item( context , null , resultArray );
			}
		}
		catch( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized static void LogDynamicEntryActive(
			Context context ,
			int type )
	{
		String version = DynamicEntryHelper.getInstance( context ).getString( "c3" );
		if( version == null )
		{
			return;
		}
		JSONObject json = new JSONObject();
		try
		{
			json.put( "Action" , ACTION_ACTIVE );
			json.put( "h12" , DynamicEntry.h12 );
			json.put( "h13" , DynamicEntry.h13 );
			json.put( "p1" , type );
			json.put( "p2" , version );
		}
		catch( JSONException e )
		{
		}
		LogHelper.Item( context , json , null );
	}
	
	public synchronized static void LogDynamicEntryClick(
			final Context context ,
			final int id ,
			final String name ,
			final int times )
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put( "Action" , ACTION_ENTRY_CLICK );
			json.put( "h12" , DynamicEntry.h12 );
			json.put( "h13" , DynamicEntry.h13 );
			json.put( "p1" , id );
			json.put( "p2" , name );
			//json.put( "p3" , times );
		}
		catch( JSONException e )
		{
		}
		List<String> getJsonList = getLogDynamicEntryClickJsonList( context );
		if( !getJsonList.contains( json.toString() ) )
		{
			getJsonList.add( json.toString() );
			setLogDynamicEntryClickJsonList( context , getJsonList );
		}
		long click = DynamicEntryHelper.getInstance( context ).getLong( json.toString() , 0L );
		DynamicEntryHelper.getInstance( context ).setValue( json.toString() , click + times );
		//LogHelper.Item( context , json , null );
	}
	
	public synchronized static void LogDynamicEntryDelete(
			final Context context ,
			final int type ,
			final int id ,
			final String name ,
			final String pkgNameOrUrl )
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put( "Action" , ACTION_DELETE );
			json.put( "h12" , DynamicEntry.h12 );
			json.put( "h13" , DynamicEntry.h13 );
			json.put( "p1" , type );
			json.put( "p2" , id );
			json.put( "p3" , name );
			json.put( "p4" , pkgNameOrUrl );
		}
		catch( JSONException e )
		{
		}
		LogHelper.Item( context , json , null );
	}
}
