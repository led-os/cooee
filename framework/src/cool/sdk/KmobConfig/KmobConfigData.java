package cool.sdk.KmobConfig;


import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.utils.StringUtils;


public class KmobConfigData
{
	
	private int c0 = 2;//0:全部关闭 用本地配置 1: 全部打开，用服务器给的默认广告位配置 2: 分广告位控制 用服务器给的不同广告位的数据控制
	private int c1 = 2 * 60;//更新服务器配置间隔（分钟）
	private String c2;//配置版本号 
	private KmobAdPlaceIDConfig c3 = new KmobAdPlaceIDConfig();//一个ADC对象，默认广告位控制信息, 如果C0配置了分广告位控制,此ADC对象中id为0
	private Map<String , KmobAdPlaceIDConfig> c4;//如果C0配置了分广告位控制则会有广告位控制信息
	private static KmobConfigData instance = null;
	// YANGTIANYU@2016/07/02 UPD START
	// 广告位ID写在这里之后就需要专属页关联framework,分开写也不好处理,我也没办法
	public final static String CAMERA_ADPLACE_ID = "20160630100625441";
	public final static String LYRIC_ADPLACE_ID = "20160630100658441";
	public final static String ALBUM_ADPLACE_ID = "20160630100636441";
	
	// YANGTIANYU@2016/07/02 UPD END
	protected KmobConfigData()
	{
		c4 = new HashMap<String , KmobAdPlaceIDConfig>();
		addC4Config( CAMERA_ADPLACE_ID , BaseDefaultConfig.SWITCH_ENABLE_CAMERAPAGE_AD_SHOW );
		addC4Config( LYRIC_ADPLACE_ID , BaseDefaultConfig.SWITCH_ENABLE_MUSICPAGE_AD_SHOW );
		addC4Config( ALBUM_ADPLACE_ID , BaseDefaultConfig.SWITCH_ENABLE_MUSICPAGE_AD_SHOW );
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.i( "KmobConfigData" , StringUtils.concat( "c4:" , c4.toString() ) );
	}
	
	/**
	 *
	 * @author gaominghui 2016年6月15日
	 */
	private void addC4Config(
			String adPlaceId ,
			boolean enableShowAd )
	{
		KmobAdPlaceIDConfig c4Temp = new KmobAdPlaceIDConfig();
		c4Temp.setAdplaceId( adPlaceId );//特效广告位
		c4Temp.setOn( enableShowAd );
		c4.put( adPlaceId , c4Temp );
	}
	
	public static KmobConfigData getInstance()
	{
		if( instance == null )
		{
			instance = new KmobConfigData();
		}
		return instance;
	}
	
	public int isC0()
	{
		return c0;
	}
	
	public void setC0(
			int c0 )
	{
		this.c0 = c0;
	}
	
	public int getC1()
	{
		return c1;
	}
	
	public void setC1(
			int c1 )
	{
		this.c1 = c1;
	}
	
	public String getC2()
	{
		return c2;
	}
	
	public void setC2(
			String c2 )
	{
		this.c2 = c2;
	}
	
	public KmobAdPlaceIDConfig isC3()
	{
		return c3;
	}
	
	public void setC3(
			KmobAdPlaceIDConfig c3 )
	{
		this.c3 = c3;
	}
	
	public KmobAdPlaceIDConfig getC4(
			String key )
	{
		KmobAdPlaceIDConfig adPlaceIDConfig = null;
		if( c4 != null && !c4.isEmpty() )
		{
			adPlaceIDConfig = c4.get( key );
		}
		return adPlaceIDConfig;
	}
	
	public Map<String , KmobAdPlaceIDConfig> isC4()
	{
		return c4;
	}
	
	public void setC4(
			Map<String , KmobAdPlaceIDConfig> c4 )
	{
		this.c4 = c4;
	}
	
	public void setData(
			KmobConfigData data )
	{
		c0 = data.c0;
		c1 = data.c1;
		c2 = data.c2;
		c3 = data.c3;
		c4 = data.c4;
	}
	
	public KmobConfigData getData()
	{
		return this;
	}
	
	/**
	 *
	 * @see java.lang.Object#toString()
	 * @auther gaominghui  2016年5月19日
	 */
	@Override
	public String toString()
	{
		return StringUtils.concat( "KmobConfigData [ " , "c0:" , c0 , "-c1:" , c1 , "-c2:" , c2 , "-c3:" , c3 , "-c4:" , c4 , " ]" );
	}
}
