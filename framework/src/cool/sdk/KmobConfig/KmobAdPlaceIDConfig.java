/***/
package cool.sdk.KmobConfig;


import com.cooee.framework.utils.StringUtils;


/**
 * @author gaominghui 2016年5月16日
 */
public class KmobAdPlaceIDConfig
{
	
	private String adplaceId = "";//广告位
	private boolean on = true;//广告开关，false关闭，true打开
	private long shows = 10000;//当日广告位展示次数
	private String showtime = "0-24";//广告展示的起止时间
	private long reqGap = 2 * 60;//请求广告时间间隔
	
	/**
	 * 
	 */
	public KmobAdPlaceIDConfig()
	{
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return the adplaceIdString
	 */
	public String getAdplaceIdString()
	{
		return adplaceId;
	}
	
	/**
	 * @param adplaceIdString the adplaceIdString to set
	 */
	public void setAdplaceId(
			String adplaceIdString )
	{
		this.adplaceId = adplaceIdString;
	}
	
	/**
	 * @return the on
	 */
	public boolean isOn()
	{
		return on;
	}
	
	/**
	 * @param on the on to set
	 */
	public void setOn(
			boolean on )
	{
		this.on = on;
	}
	
	/**
	 * @return the shows
	 */
	public long getShows()
	{
		return shows;
	}
	
	/**
	 * @param shows the shows to set
	 */
	public void setShows(
			long shows )
	{
		this.shows = shows;
	}
	
	/**
	 * @return the showtime
	 */
	public String getShowtime()
	{
		return showtime;
	}
	
	/**
	 * @param showtime the showtime to set
	 */
	public void setShowtime(
			String showtime )
	{
		this.showtime = showtime;
	}
	
	/**
	 * @return the reqGap
	 */
	public long getReqGap()
	{
		return reqGap;
	}
	
	/**
	 * @param reqGap the reqGap to set
	 */
	public void setReqGap(
			long reqGap )
	{
		this.reqGap = reqGap;
	}
	
	/**
	 * @return the adplaceId
	 */
	public String getAdplaceId()
	{
		return adplaceId;
	}
	
	/**
	 *
	 * @see java.lang.Object#toString()
	 * @auther gaominghui  2016年5月16日
	 */
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		return StringUtils.concat( "KmobAdPlaceIDConfig [adplaceId:" , adplaceId , "-on:" , on , "-shows:" , shows , "-showtime:" , showtime , "-reqGap:" , reqGap , "]" );
	}
}
