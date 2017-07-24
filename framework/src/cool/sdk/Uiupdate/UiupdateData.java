package cool.sdk.Uiupdate;


import org.json.JSONException;
import org.json.JSONObject;


public class UiupdateData
{
	
	private int r1;//更新升级开关 1： 显示更新升级菜单0： 关闭更新升级菜单。
	private long r2;//更新时间间隔 单位：分钟
	private long r3;//服务器上新版本版本号versioncode
	private String r4;//服务器新版本的版本名称versionname
	private String r5;//URL地址（ 如果URL 为空，说明无更新）
	private long r6;//文件大小（单位B）
	private String r7;//更新说明
	private int r8; //IS WIFI
	private long r9;//产品ID
	private int r10;//产品类型
	private long r11;//资源ID(resid)
	// zhangjin@2015/12/02 ADD START
	private int r12;//is silent
	// zhangjin@2015/12/02 ADD END
	private boolean hasDown;
	
	public void setUiupdateData(
			JSONObject json )
	{
		if( json != null )
		{
			this.r1 = json.optInt( "r1" );
			this.r2 = json.optLong( "r2" );
			this.r3 = json.optLong( "r3" );
			this.r4 = json.optString( "r4" );
			this.r5 = json.optString( "r5" );
			this.r6 = json.optLong( "r6" );
			this.r7 = json.optString( "r7" );
			this.r8 = json.optInt( "r8" );
			this.r9 = json.optLong( "r9" );
			this.r10 = json.optInt( "r10" );
			this.r11 = json.optLong( "r11" );
			// zhangjin@2015/12/02 ADD START
			this.r12 = json.optInt( "r12" );
			// zhangjin@2015/12/02 ADD END
		}
	}
	
	public int getR1()
	{
		return r1;
	}
	
	public void setR1(
			int r1 )
	{
		this.r1 = r1;
	}
	
	public long getR2()
	{
		return r2;
	}
	
	public void setR2(
			long r2 )
	{
		this.r2 = r2;
	}
	
	public long getR3()
	{
		return r3;
	}
	
	public void setR3(
			long r3 )
	{
		this.r3 = r3;
	}
	
	public String getR4()
	{
		return r4;
	}
	
	public void setR4(
			String r4 )
	{
		this.r4 = r4;
	}
	
	public String getR5()
	{
		return r5;
	}
	
	public void setR5(
			String r5 )
	{
		this.r5 = r5;
	}
	
	public long getR6()
	{
		return r6;
	}
	
	public void setR6(
			long r6 )
	{
		this.r6 = r6;
	}
	
	public String getR7()
	{
		return r7;
	}
	
	public void setR7(
			String r7 )
	{
		this.r7 = r7;
	}
	
	public int getR8()
	{
		return r8;
	}
	
	public void setR8(
			int r8 )
	{
		this.r8 = r8;
	}
	
	public long getR9()
	{
		return r9;
	}
	
	public void setR9(
			long r9 )
	{
		this.r9 = r9;
	}
	
	public int getR10()
	{
		return r10;
	}
	
	public void setR10(
			int r10 )
	{
		this.r10 = r10;
	}
	
	public long getR11()
	{
		return r11;
	}
	
	public void setR11(
			long r11 )
	{
		this.r11 = r11;
	}
	
	public boolean isHasDown()
	{
		return hasDown;
	}
	
	public void setHasDown(
			boolean hasDown )
	{
		this.hasDown = hasDown;
	}
	
	// zhangjin@2015/12/02 ADD START
	public int getR12()
	{
		return r12;
	}
	
	public void setR12(
			int r12 )
	{
		this.r12 = r12;
	}
	
	// zhangjin@2015/12/02 ADD END
	public JSONObject toJSON()
	{
		JSONObject res = new JSONObject();
		try
		{
			res.put( "r1" , r1 );
			res.put( "r2" , r2 );
			res.put( "r3" , r3 );
			res.put( "r4" , r4 );
			res.put( "r5" , r5 );
			res.put( "r6" , r6 );
			res.put( "r7" , r7 );
			res.put( "r8" , r8 );
			res.put( "r9" , r9 );
			res.put( "r10" , r10 );
			res.put( "r11" , r11 );
			res.put( "hasDown" , hasDown );
			// zhangjin@2015/12/02 ADD START
			res.put( "r12" , r12 );
			// zhangjin@2015/12/02 ADD END
			return res;
		}
		catch( JSONException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
