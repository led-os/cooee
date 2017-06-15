package com.coco.theme.themebox.util;


/**
 * 下载图片模式
 * @author Administrator
 *
 */
public class DownImageNode
{
	
	public String packname;//包名
	public DownType downType;//下载类型（枚举类型）
	public String tabType;//标签
	
	public DownImageNode(
			String packname ,
			DownType downType ,
			String type )
	{
		this.packname = packname;
		this.downType = downType;
		this.tabType = type;
	}
}
