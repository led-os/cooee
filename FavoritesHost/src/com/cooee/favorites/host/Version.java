package com.cooee.favorites.host;


/**
 * 酷生活版本控制
 * @author Administrator
 *
 */
public class Version
{
	
	//	//	//	//	//	//	//	//	//	//huwenhao@2016/03/24 UPD START
	//	//	//	//	//	//	//	//	//	public static final int HOST_VERSION_CODE = 2;
	//	//	//	//	//	//	//	//	//	public static final int PLUGIN_VERSION_CODE = 41500;
	//	//	//	//	//	//	//	//	//	//huwenhao@2016/03/24 UPD END
	//	//	//	//	//	//	//	//	//huwenhao@2016/04/14 UPD START
	//	//	//	//	//	//	//	//	public static final int HOST_VERSION_CODE = 3;√
	//	//	//	//	//	//	//	//	public static final int PLUGIN_VERSION_CODE = 41701;//host版本号由2升级为3时，最近一个插件的版本号为41701
	//	//	//	//	//	//	//	//	//huwenhao@2016/04/14 UPD END
	//	//	//	//	//	//	//	//cheyingkun add start	//酷生活引导页
	//	//	//	//	//	//	//	public static final int HOST_VERSION_CODE = 4;
	//	//	//	//	//	//	//	public static final int PLUGIN_VERSION_CODE = 41778;//host版本号由3升级为4时，最近一个插件的版本号为41778
	//	//	//	//	//	//	//	//cheyingkun add end
	//	//	//	//	//	//	//huwenhao@2016/04/21 UPD START
	//	//	//	//	//	//	public static final int HOST_VERSION_CODE = 5;//预注册多个activity和service，以备后续开发使用【胡文浩】
	//	//	//	//	//	//	public static final int PLUGIN_VERSION_CODE = 41823;//host版本号由4升级为5时，最近一个插件的版本号为41823
	//	//	//	//	//	//	//huwenhao@2016/04/21 UPD END
	//	//	//	//	//	//cheyingkun add start	//修改酷生活S5引导页动画。
	//	//	//	//	//	public static final int HOST_VERSION_CODE = 6;//修改酷生活S5引导页动画。
	//	//	//	//	//	public static final int PLUGIN_VERSION_CODE = 41838;//host版本号由5升级为6时，最近一个插件的版本号为41838
	//	//	//	//	//	//cheyingkun add start
	//	//	//	//	//xiatian add start	//sohu news（修改全屏切换到半屏的逻辑：由“向上滑时，若新闻在第一条，则退出全屏”改为“1、back键；2、搜狐新闻通知桌面（搜狐新闻中大幅度下滑），onNewPluginIntent中调用exitSoHuNewsExpandedMode”）
	//	//	//	//	public static final int HOST_VERSION_CODE = 7;//favorites.jar添加接口“exitSoHuNewsExpandedMode”。
	//	//	//	//	public static final int PLUGIN_VERSION_CODE = 41935;//host版本号由6升级为7时，最近一个插件的版本号为41935
	//	//	//	//	//xiatian add end
	//	//	//	//cheyingkun add start	//服务器关闭酷生活后，释放资源。
	//	//	//	public static final int HOST_VERSION_CODE = 8;//favorites.jar添加接口“clearFavoritesView”。
	//	//	//	public static final int PLUGIN_VERSION_CODE = 42013;//host版本号由7升级为8时，最近一个插件的版本号为42013
	//	//	//	//cheyingkun add end
	//	//	//xiatian add start	//sohu news（修改点击返回键退出酷生活的逻辑：使用搜狐新闻的时候，点击返回键退出酷生活的逻辑由“点击返回键就退出酷生活”改为“当搜狐新闻为半屏状态时，点击返回键才退出酷生活”）
	//	//	public static final int HOST_VERSION_CODE = 9;//favorites.jar添加接口“isNeedExitFavorites”。
	//	//	public static final int PLUGIN_VERSION_CODE = 42091;//host版本号由8升级为9时，最近一个插件的版本号为42091
	//	//	//xiatian add end
	//	//cheyingkun add start	//解决“调整时间和日期后,酷生活常用应用显示的动态图标不更新”的问题【i_0014330】
	//	//	public static final int HOST_VERSION_CODE = 10;//favorites.jar添加接口“updateFavoritesAppsIcon”。
	//	//	public static final int PLUGIN_VERSION_CODE = 42836;//host版本号由9升级为10时，最近一个插件的版本号为42836
	//	//cheyingkun add end
	//	//	public static final int HOST_VERSION_CODE = 11;//favorites.jar添加接口“getFavoriteState”。获取当前是搜狐新闻还是cooee新闻，以及包括展开和折叠状态
	//	//	public static final int PLUGIN_VERSION_CODE = 42962;//host版本号由10升级为11时，最近一个插件的版本号为42962
	//	public static final int HOST_VERSION_CODE = 12;//favorites.jar添加接口“IFavoritesReady”。通知桌面酷生活初始化完成
	//	public static final int PLUGIN_VERSION_CODE = 43051;//host版本号由11升级为12时，最近一个插件的版本号为42963
	//xiatian add start	//sohu news（删除搜狐新闻）
	public static final int HOST_VERSION_CODE = 13;//favorites.jar删除接口“exitSoHuNewsExpandedMode”和“isNeedExitFavorites”。
	public static final int PLUGIN_VERSION_CODE = 44130;//host版本号由12升级为13时，最近一个插件的版本号为44130
	//xiatian add end
}
