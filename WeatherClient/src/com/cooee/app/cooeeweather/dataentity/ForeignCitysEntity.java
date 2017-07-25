package com.cooee.app.cooeeweather.dataentity;


public class ForeignCitysEntity
{
	
	// field name
	public static final String ID = "_ID";
	public static final String COUNTRY_EN = "COUNTRY_EN";
	public static final String CITY_EN = "CITY_EN";
	public static final String COUNTRY_ZH = "COUNTRY_ZH";
	public static final String CITY_ZH = "CITY_ZH";
	public static final int ID_INDEX = 0;
	public static final int CITY_EN_INDEX = 1;
	public static final int COUNTRY_EN_INDEX = 2;
	// projection_abroad_en
	public static final String[] projection_abroad_en = new String[]{ ID , CITY_EN , COUNTRY_EN };
	public static final String[] projection_abroad_zh = new String[]{ ID , CITY_ZH , COUNTRY_ZH };
	// filed
	private String name;
	
	/**
	 * @param �ַ�
	 * @return ǰ׺
	 */
	public static String getPrefix(
			String name )
	{
		int index = name.indexOf( "." );
		if( index == -1 )
		{
			return name;
		}
		else
		{
			return name.substring( 0 , index );
		}
	}
	
	/**
	 * @param �ַ�
	 * @return ��׺
	 */
	public static String getSuffix(
			String name )
	{
		int index = name.indexOf( "." );
		if( index == -1 )
		{
			return name;
		}
		else
		{
			return name.substring( index + 1 , name.length() );
		}
	}
	
	public void setName(
			String name )
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
}
