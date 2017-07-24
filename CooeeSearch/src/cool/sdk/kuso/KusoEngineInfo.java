package cool.sdk.kuso;


public class KusoEngineInfo
{
	
	private String r1;//引擎ID
	
	@Override
	public String toString()
	{
		return "KusoEngineInfo [r1=" + r1 + ", r2=" + r2 + ", r3=" + r3 + ", r4=" + r4 + ", r5=" + r5 + ", r6=" + r6 + ", r7=" + r7 + "]";
	}
	
	private String r2;//引擎名字
	private String r3;//引擎icon,这里指icon的url地址
	private String r4;//搜索引擎查询url的头
	private String r5;//搜索关键字的名字参数
	private boolean r6;//是否为默认引擎
	private boolean r7;//是否被选中
	
	public boolean isR7()
	{
		return r7;
	}
	
	public void setR7(
			boolean r7 )
	{
		this.r7 = r7;
	}
	
	public String getR1()
	{
		return r1;
	}
	
	public void setR1(
			String r1 )
	{
		this.r1 = r1;
	}
	
	public String getR2()
	{
		return r2;
	}
	
	public void setR2(
			String r2 )
	{
		this.r2 = r2;
	}
	
	public String getR3()
	{
		return r3;
	}
	
	public void setR3(
			String r3 )
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
	
	public boolean isR6()
	{
		return r6;
	}
	
	public void setR6(
			boolean r6 )
	{
		this.r6 = r6;
	}
	
	public void setData(
			KusoEngineInfo info )
	{
		r1 = info.r1;
		r2 = info.r2;
		r3 = info.r3;
		r4 = info.r4;
		r5 = info.r5;
		r6 = info.r6;
		r7 = info.r7;
	}
}
