package cool.sdk.kuso;


import java.util.ArrayList;


public class KusoData
{
	
	public static final boolean C3_DEFAULT = true;
	public static final boolean C4_DEFAULT = true;
	private boolean c0;//true:显示  false:是否显示使用本地配置 
	private int c1;//更新间隔（分钟）
	private String c2;//配置版本号 
	private boolean c3 = C3_DEFAULT;//打开搜索结果方式 true:浏览器 false:webview 修改默认值为true by jubingcheng 20160615
	private boolean c4 = C4_DEFAULT;//是否显示运营页 true:显示 false:不显示 修改默认值为true by jubingcheng 20160425
	private String c5 = "http://nanohome.cn/searchPageForeign/searchPage/index.html";//webview的url(默认)
	private ArrayList<String> c6;//热词列表
	private int c7 = 1;//国内搜索引擎框架 修改默认值 by jubingcheng 20160425
	private int c8 = 1;//国外搜索引擎框架 修改默认值 by jubingcheng 20160425
	private ArrayList<KusoEngineInfo> engines;
	
	public KusoData()
	{
		engines = new ArrayList<KusoEngineInfo>();
		c6 = new ArrayList<String>();
	}
	
	public boolean isC0()
	{
		return c0;
	}
	
	public void setC0(
			boolean c0 )
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
	
	public boolean isC3()
	{
		return c3;
	}
	
	public void setC3(
			boolean c3 )
	{
		this.c3 = c3;
	}
	
	public ArrayList<KusoEngineInfo> getEngines()
	{
		return engines;
	}
	
	public boolean isC4()
	{
		return c4;
	}
	
	public void setC4(
			boolean c4 )
	{
		this.c4 = c4;
	}
	
	public String getC5()
	{
		return c5;
	}
	
	public void setC5(
			String c5 )
	{
		this.c5 = c5;
	}
	
	public ArrayList<String> getC6()
	{
		return c6;
	}
	
	public int getC7()
	{
		return c7;
	}
	
	public void setC7(
			int c7 )
	{
		this.c7 = c7;
	}
	
	public int getC8()
	{
		return c8;
	}
	
	public void setC8(
			int c8 )
	{
		this.c8 = c8;
	}
	
	public void setEngines(
			ArrayList<KusoEngineInfo> engines )
	{
		this.engines = engines;
	}
	
	public void setData(
			KusoData data )
	{
		c0 = data.c0;
		c1 = data.c1;
		c2 = data.c2;
		c3 = data.c3;
		c4 = data.c4;
		c5 = data.c5;
		c7 = data.c7;
		c8 = data.c8;
		for( int i = 0 ; i < data.engines.size() ; i++ )
		{
			KusoEngineInfo info = new KusoEngineInfo();
			info.setData( data.engines.get( i ) );
			engines.add( info );
		}
		for( int i = 0 ; i < data.c6.size() ; i++ )
		{
			c6.add( data.c6.get( i ) );
		}
	}
}
