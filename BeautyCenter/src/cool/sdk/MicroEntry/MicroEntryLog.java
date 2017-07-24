package cool.sdk.MicroEntry;


import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import cool.sdk.log.LogHelper;


public class MicroEntryLog
{
	
	private static final String ACTION_ACTIVE = "3205";
	private static final String ACTION_DELETE = "3206";
	private static Object syncObj = new Object();
	
	public static class MicroEntryLogItem
	{
		
		public int type;
		public int id;
		public String name;
	}
	
	private static void LogItem(
			Context context ,
			String action ,
			MicroEntryLogItem item )
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put( "Action" , action );
			json.put( "h12" , MicroEntry.h12 );
			json.put( "h13" , MicroEntry.h13 );
			json.put( "p1" , item.type );
			json.put( "p2" , item.id );
			json.put( "p3" , item.name );
		}
		catch( JSONException e )
		{
		}
		LogHelper.Item( context , json , null );
	}
	
	private static void LogItem(
			Context context ,
			String action ,
			List<MicroEntryLogItem> itemList )
	{
		JSONArray jsonArray = new JSONArray();
		for( MicroEntryLogItem item : itemList )
		{
			JSONObject json = new JSONObject();
			try
			{
				json.put( "Action" , action );
				json.put( "h12" , MicroEntry.h12 );
				json.put( "h13" , MicroEntry.h13 );
				json.put( "p1" , item.type );
				json.put( "p2" , item.id );
				json.put( "p3" , item.name );
			}
			catch( JSONException e )
			{
			}
			jsonArray.put( json );
		}
		LogHelper.Item( context , null , jsonArray );
	}
	
	public static void LogActive(
			Context context ,
			MicroEntryLogItem item )
	{
		synchronized( syncObj )
		{
			LogItem( context , ACTION_ACTIVE , item );
		}
	}
	
	public static void LogActive(
			Context context ,
			List<MicroEntryLogItem> itemList )
	{
		synchronized( syncObj )
		{
			LogItem( context , ACTION_ACTIVE , itemList );
		}
	}
	
	public static void LogDelete(
			Context context ,
			MicroEntryLogItem item )
	{
		synchronized( syncObj )
		{
			LogItem( context , ACTION_DELETE , item );
		}
	}
	
	public static void LogDelete(
			Context context ,
			List<MicroEntryLogItem> itemList )
	{
		synchronized( syncObj )
		{
			LogItem( context , ACTION_DELETE , itemList );
		}
	}
}
