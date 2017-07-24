package com.cooee.util;


import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class MapUtils
{
	
	public interface MapTraversalCallBack
	{
		
		void findObject(
				Object object );
	}
	
	@SuppressWarnings( { "rawtypes" , "unchecked" } )
	public static void traversalMap(
			Map map ,
			MapTraversalCallBack callBack )
	{
		if( map != null && map.size() > 0 )
		{
			Iterator<Entry> it = map.entrySet().iterator();
			while( it.hasNext() )
			{
				Map.Entry entry = (Map.Entry)it.next();
				callBack.findObject( entry.getValue() );
			}
		}
	}
}
