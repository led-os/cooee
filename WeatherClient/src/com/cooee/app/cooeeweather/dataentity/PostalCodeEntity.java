package com.cooee.app.cooeeweather.dataentity;


public class PostalCodeEntity
{
	
	// field name
	public static final String POSTAL_CODE = "postalCode";
	public static final String USER_ID = "userid";
	public static final String CITY_NUM = "city_num";
	public static final String ENTITY_ID = "entityid";
	public static final String LONG = "long";
	public static final String LAT = "lat";
	public static final String LOCATION_NAME = "weatherlocationname";
	public static final String LOCATION_CODE = "weatherlocationcode";
	public static final String AUTO_LOCATE = "auto_locate";
	// projection
	public static final String[] projection = new String[]{ POSTAL_CODE , USER_ID , ENTITY_ID , LONG , LAT , LOCATION_NAME , LOCATION_CODE , CITY_NUM , AUTO_LOCATE };
	// field
	private String postalCode;
	private String userId;
	private String city_num;
	private boolean auto_locate;
	
	public String getPostalCode()
	{
		return postalCode;
	}
	
	public void setPostalCode(
			String postalCode )
	{
		this.postalCode = postalCode;
	}
	
	public String getUserId()
	{
		return userId;
	}
	
	public void setUserId(
			String userId )
	{
		this.userId = userId;
	}
	
	public String getCityNum()
	{
		return city_num;
	}
	
	public void setCityNum(
			String city_num )
	{
		this.city_num = city_num;
	}
	
	public boolean isAuto_locate()
	{
		return auto_locate;
	}
	
	public void setAuto_locate(
			boolean auto_locate )
	{
		this.auto_locate = auto_locate;
	}
}
