package cool.sdk.Category;


import java.util.Locale;


// floder category , change by shlt@2014/12/08 UPD
public class RecommendApkInfo
{
	
	private String pkgName;
	private int apkType;
	private String apkDLInfo;
	private String apkVersionCode;
	private String apkVersionName;
	private int apkSize;
	private String apkCN;
	private String apkEN;
	private String apkFN;
	private String apkIconpath;
	private int flag;
	private String webLinkPkg;
	private String linkAddr;//虚链接icon下载地址
	
	public RecommendApkInfo(
			String pkgName ,
			int apkType ,
			String apkDLInfo ,
			String apkVersionCode ,
			String apkVersionName ,
			int apkSize ,
			String apkCN ,
			String apkEN ,
			String apkFN ,
			String apkIconpath ,
			int flag ,
			String webLinkPkg ,
			String linkAddr )
	{
		// TODO Auto-generated constructor stub
		this.pkgName = pkgName;
		this.apkType = apkType;
		this.apkDLInfo = apkDLInfo;
		this.apkVersionCode = apkVersionCode;
		this.apkVersionName = apkVersionName;
		this.apkSize = apkSize;
		this.apkCN = apkCN;
		this.apkEN = apkEN;
		this.apkFN = apkFN;
		this.apkIconpath = apkIconpath;
		this.flag = flag;
		this.webLinkPkg = webLinkPkg;
		this.linkAddr = linkAddr;
	}
	
	public String getPkgName()
	{
		return pkgName;
	}
	
	public int getApkType()
	{
		return apkType;
	}
	
	public String getApkDLInfo()
	{
		return apkDLInfo;
	}
	
	public String getApkVersionCode()
	{
		return apkVersionCode;
	}
	
	public String getApkVersionName()
	{
		return apkVersionName;
	}
	
	public int getApkSize()
	{
		return apkSize;
	}
	
	public String getApkCN()
	{
		return apkCN;
	}
	
	public String getApkEN()
	{
		return apkEN;
	}
	
	public String getApkFN()
	{
		return apkFN;
	}
	
	public String getApkIconpath()
	{
		return apkIconpath;
	}
	
	public void setApkIconpath(
			String apkIconpath )
	{
		this.apkIconpath = apkIconpath;
	}
	
	public int getFlag()
	{
		return flag;
	}
	
	public String getWebLinkPkg()
	{
		return webLinkPkg;
	}
	
	public String getLinkAddr()
	{
		return linkAddr;
	}
	
	public String getTitle()
	{
		if( Locale.CHINA.equals( Locale.getDefault() ) )
		{
			return apkCN;
		}
		else if( Locale.TAIWAN.equals( Locale.getDefault() ) )
		{
			return apkFN;
		}
		return apkEN;
	}
}
