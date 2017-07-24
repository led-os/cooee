package com.cooee.phenix.launcherSettings;


import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.Category.CategoryParse;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.UmengStatistics;
import com.cooee.phenix.Functions.Category.OperateHelp;
import com.cooee.phenix.Functions.DefaultLauncherGuide.DefaultLauncherGuideManager;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.launcherSettings.BaseListPreferenceFragment.OnDataChangedListener;
import com.cooee.update.UpdateActivity;
import com.cooee.update.UpdateUiManager;
import com.umeng.analytics.MobclickAgent;


public class LauncherSettingsActivity extends PreferenceBaseSettingActivity implements OnDataChangedListener
{
	
	private final String TAG = "LauncherSettingsActivity";
	private BaseListPreferenceFragment settingPreferenceFragment;
	private LauncherEffectFragment launcherEffectFragment;//特效设置界面
	private LauncherStyleFragment launcherStyleFragment;//桌面模式设置界面
	private LauncherClassificationFragment launcherClassificationFragment;//智能分类设置界面
	private SharedPreferences mSharedPrefs;
	private List<CharSequence> efffects;//特效设置副标题内容
	private String[] styles;//桌面模式设置副标题内容
	//WangLei add start //桌面和主菜单特效的分离
	private LauncherAppListEffectFragment appListEffectFragment; //主菜单特效设置界面
	private List<CharSequence> appListEffects; //主菜单特效设置副标题内容	
	//WangLei add end
	;
	/**进入二级菜单是否做动画*/
	private boolean isNeedAnim = true;//cheyingkun add	//修改桌面默认配置
	//cheyingkun add start	//和兴六部图标大小变化需求
	private String[] app_icon_sizes;//图标大小切换副标题内容
	private LauncherAppIconSizeFragment launcherAppIconSizeFragment;//图标大小设置界面
	//cheyingkun add end	//和兴六部图标大小变化需求
	private HashMap<Integer , Boolean> onBackPressedFinishSettingsMap = new HashMap<Integer , Boolean>();
	//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
	private WorkspaceScrollTypeFragment mWorkspaceScrollTypeFragment;//“桌面滑动类型”二级界面
	private String[] mWorkspaceScrollTypeEntries;//“桌面滑动类型”菜单副标题
	//xiatian add end
	;
	//xiatian add start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
	private ApplistScrollTypeFragment mApplistScrollTypeFragment;//“主菜单滑动类型”二级界面
	private String[] mApplistScrollTypeEntries;//“主菜单滑动类型”菜单副标题
	//xiatian add end
	;
	//xiatian add start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
	private WidgetScrollTypeFragment mWidgetScrollTypeFragment;//“小组件滑动类型”二级界面
	private String[] mWidgetScrollTypeEntries;//“小组件滑动类型”菜单副标题
	//xiatian add end
	;
	
	@Override
	public void onBuildHeaders(
			List<Header> headers )
	{
		// TODO Auto-generated method stub
		//super.onBuildHeaders( headers );
		loadHeadersFromResource( R.xml.launcher_settings_header , headers );
		initData( headers );
	}
	
	private void initData(
			List<Header> headers )
	{
		//mSharedPrefs = getSharedPreferences( LauncherAppState.getSharedPreferencesKey() , Context.MODE_PRIVATE );
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences( this );
		efffects = LauncherEffectFragment.getEntriesList( this );
		styles = LauncherDefaultConfig.getStringArray( R.array.launcher_stytle_entries );
		appListEffects = LauncherAppListEffectFragment.getEntriesList( this ); //WangLei add //桌面和主菜单特效的分离
		//cheyingkun add start	//和兴六部图标大小变化需求
		if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
		{
			app_icon_sizes = LauncherDefaultConfig.getStringArray( R.array.launcher_app_icon_size );
		}
		//cheyingkun add end	//和兴六部图标大小变化需求
		//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_SHOW_WORKSPACE_SCROLL_TYPE_IN_LAUNCHER_SETTINGS )
		{
			mWorkspaceScrollTypeEntries = LauncherDefaultConfig.getStringArray( R.array.workspace_scroll_type_entries );
		}
		//xiatian add end
		//xiatian add start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
		if(
		//
		( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
		//
		&& LauncherDefaultConfig.SWITCH_ENABLE_SHOW_APPLIST_SCROLL_TYPE_IN_LAUNCHER_SETTINGS
		//
		)
		{
			mApplistScrollTypeEntries = LauncherDefaultConfig.getStringArray( R.array.applist_scroll_type_entries );
		}
		//xiatian add end
		//xiatian add start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
		if( LauncherDefaultConfig.SWITCH_ENABLE_SHOW_WIDGET_SCROLL_TYPE_IN_LAUNCHER_SETTINGS )
		{
			mWidgetScrollTypeEntries = LauncherDefaultConfig.getStringArray( R.array.widget_scroll_type_entries );
		}
		//xiatian add end
		refleshSummary( headers );
		LauncherAppState.getInstance().setLauncherSettingsActivity( this );//xiatian add	//fix bug：解决“在phenix桌面设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home回到默认桌面后，桌面特效为之前的桌面特效（没有保存刚刚在屏幕切页特效界面中的选择）”的问题。【i_0010438】
		//cheyingkun add start	//修改桌面默认配置
		Intent intent = getIntent();
		//获取intent里的资源id
		int headerId = intent.getIntExtra( "headerId" , HEADER_ID_UNDEFINED );
		boolean mIsOnBackPressedFinishSettings = intent.getBooleanExtra( "onBackPressedFinishSettings" , false );//cheyingkun add	//phenix仿S5效果,编辑模式底部按钮配置
		if( HEADER_ID_UNDEFINED != headerId )
		{
			isNeedAnim = false;
		}
		//cheyingkun add start	//优化编辑模式底边栏配置桌面设置二级界面的方式
		//获取intent里进入二级界面功能的下标(因为是可配的,所以配置资源id太麻烦,id会变)
		int headerIndex = intent.getIntExtra( "headerIndex" , HEADER_ID_UNDEFINED );
		if( headerIndex < HEADER_ID_UNDEFINED || headerIndex > HEADER_ID_APPLIST_EFFECT_IN_DRAWER )
		{
			headerIndex = HEADER_ID_UNDEFINED;
		}
		int heardIdByIndex = getHeardIdByIndex( headerIndex );
		if( heardIdByIndex != -1 )
		{
			isNeedAnim = false;
			headerId = heardIdByIndex;
			onBackPressedFinishSettingsMap.put( Integer.valueOf( heardIdByIndex ) , Boolean.valueOf( mIsOnBackPressedFinishSettings ) );
		}
		//cheyingkun add end
		switchToHeaderInner( headerId );
		//cheyingkun add end
	}
	
	private void refleshSummary(
			List<Header> headers )
	{
		//<i_0010319> liuhailin@2015-03-06 del begin
		int i = 0;
		while( i < headers.size() )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , StringUtils.concat( "i:" , i ) );
			Header mHeader = headers.get( i );
			if( mHeader.id == R.id.setting_effect )
			{
				//WangLei add start //桌面和主菜单特效的分离
				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
				{
					/**双层模式时隐藏统一的屏幕切页特效设置选项*/
					mHeaders.remove( i );
				}
				else
				{
					//WangLei add end
					String value = mSharedPrefs.getString( LauncherDefaultConfig.getString( R.string.setting_key_launcher_effects ) , "0" );
					mHeader.summary = efffects.get( Integer.parseInt( value ) );
				}
			}
			if( mHeader.id == R.id.setting_launcher_stytle )
			{
				if( !LauncherDefaultConfig.SWITCH_ENABLE_SHOW_LAUNCHER_STYLE_MENU_IN_LAUNCHER_SETTING )
					mHeaders.remove( i );
			}
			//xiatian add start	//桌面图标显示的样式（详见BaseDefaultConfig.java中的“ITEM_STYLE_XXX”）。
			if( LauncherDefaultConfig.CONFIG_ITEM_STYLE == BaseDefaultConfig.ITEM_STYLE_ICON_EXTENDS_INTO_TITLE && mHeader.id == R.id.setting_launcher_stytle && mHeader.visable == true )
			{//关联一些配置
				mHeaders.remove( i );
			}
			//xiatian add end
			// liwenxia@2016/07/19 ADD START  //设置默认桌面引导
			if( ( LauncherDefaultConfig.SWITCH_ENABLE_SET_TO_DEFAULT_LAUNCHER_GUIDE == false || DefaultLauncherGuideManager.getInstance().isDefaultLauncher( getApplicationContext() ) || DefaultLauncherGuideManager
					.getInstance().isOnlyLauncher( getApplicationContext() ) ) && R.id.setting_launcher_home == mHeader.id )
			{
				mHeaders.remove( i );
			}
			// liwenxia@2016/07/19 ADD END
			if( mHeader.id == R.id.setting_launcher_stytle )
			{
				mHeader.summary = styles[LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE];
			}
			//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
			if( mHeader.id == R.id.setting_workspace_scroll_type_id )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_SHOW_WORKSPACE_SCROLL_TYPE_IN_LAUNCHER_SETTINGS )
				{
					mHeader.summary = mWorkspaceScrollTypeEntries[LauncherDefaultConfig.SWITCH_ENABLE_WORKSPACE_LOOP_SLIDE ? 1 : 0];
				}
				else
				{
					mHeaders.remove( i );
				}
			}
			//xiatian add end
			//xiatian add start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
			if( mHeader.id == R.id.setting_applist_scroll_type_id )
			{
				if(
				//
				( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
				//
				&& LauncherDefaultConfig.SWITCH_ENABLE_SHOW_APPLIST_SCROLL_TYPE_IN_LAUNCHER_SETTINGS
				//
				)
				{
					mHeader.summary = mApplistScrollTypeEntries[LauncherDefaultConfig.SWITCH_ENABLE_APPLIST_LOOP_SLIDE ? 1 : 0];
				}
				else
				{
					mHeaders.remove( i );
				}
			}
			//xiatian add end
			//xiatian add start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
			if( mHeader.id == R.id.setting_widget_scroll_type_id )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_SHOW_WIDGET_SCROLL_TYPE_IN_LAUNCHER_SETTINGS )
				{
					mHeader.summary = mWidgetScrollTypeEntries[LauncherDefaultConfig.SWITCH_ENABLE_WIDGET_LOOP_SLIDE ? 1 : 0];
				}
				else
				{
					mHeaders.remove( i );
				}
			}
			//xiatian add end
			if( mHeader.id == R.id.setting_launcher_classification )
			{
				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER )
				{
					headers.remove( i );
				}
				else
				{
					if( !CategoryParse.canShowCategory() )
					{
						headers.remove( i );
					}
					else
					{
						//分类成功时，会记录下时间，根据是否有记录来判断是否分类，并显示在下方
						String value = mSharedPrefs.getString( OperateHelp.ClassificationTime , null );
						if( value == null )
						{
							mHeader.summary = LauncherDefaultConfig.getString( R.string.hava_not_category );
						}
						else
						{
							try
							{
								long time = Long.parseLong( value );
								value = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( time ); //转换时间
							}
							catch( Exception e )
							{
							}
							finally
							{
								mHeader.summary = getResources().getString( R.string.last_category_time , value );//本地化，待修改。
							}
						}
					}
				}
			}
			//WangLei add start //桌面和主菜单特效的分离
			if( mHeader.id == R.id.setting_workspace_effect ) //双层模式桌面切页特效设置
			{
				if( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
				{
					/**单层模式隐藏桌面切页特效设置*/
					mHeaders.remove( i );
				}
				else
				{
					String value = mSharedPrefs.getString( LauncherDefaultConfig.getString( R.string.setting_key_workspace_effect ) , "0" );
					mHeader.summary = efffects.get( Integer.parseInt( value ) );
				}
			}
			if( mHeader.id == R.id.setting_applist_effect ) //双层模式主菜单切页特效
			{
				// zhangjin@2016/05/13 UPD START
				if(
				//
				( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_CORE )
				//
				|| ( LauncherDefaultConfig.CONFIG_APPLIST_STYLE != LauncherDefaultConfig.APPLIST_SYTLE_KITKAT /* //zhujieping add	//需求：拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。 */)
				//
				)
				// zhangjin@2016/05/13 UPD END
				{
					/**单层模式隐藏主菜单切页特效设置*/
					mHeaders.remove( i );
				}
				else
				{
					String value = mSharedPrefs.getString( LauncherDefaultConfig.getString( R.string.setting_key_applist_effect ) , "0" );
					mHeader.summary = appListEffects.get( Integer.parseInt( value ) );
				}
			}
			//WangLei add end
			//cheyingkun add start	//和兴六部图标大小变化需求
			if( mHeader.id == R.id.setting_app_icon_size )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT )
				{
					String value = mSharedPrefs.getString( LauncherDefaultConfig.getString( R.string.setting_key_app_icon_size ) , "0" );
					mHeader.summary = app_icon_sizes[Integer.parseInt( value )];
				}
				else
				{
					mHeaders.remove( i );
				}
			}
			//cheyingkun add end	//和兴六部图标大小变化需求
			// zhangjin@2015/12/17 ADD START
			if( mHeader.id == R.id.setting_about_launcher )
			{
				if( showUpdateMenu() == false )
				{
					mHeaders.remove( i );
				}
			}
			// zhangjin@2015/12/17 ADD END
			if( i < headers.size() && headers.get( i ) == mHeader )
			{
				i++;
			}
		}
		//<i_0010319> liuhailin@2015-03-06 del end
	}
	
	// zhangjin@2015/12/17 ADD START
	private boolean showUpdateMenu()
	{
		boolean isShow = false;
		if( LauncherDefaultConfig.LAUNCHER_UPDATE )
		{
			isShow = true;
		}
		else
		{
			isShow = UpdateUiManager.getInstance().getMenuState();
		}
		return isShow;
	}
	
	// zhangjin@2015/12/17 ADD END
	@Override
	public void onHeaderClick(
			Header header ,
			int position )
	{
		// TODO Auto-generated method stub
		if( header.id != HEADER_ID_UNDEFINED )
		{
			switchToHeaderInner( header.id );
		}
	}
	
	private BaseListPreferenceFragment getCurrentPreferenceFragment(
			int fragmentId )
	{
		boolean mIsOnBackPressedFinishSettings = false;
		if( onBackPressedFinishSettingsMap.containsKey( fragmentId ) )
		{
			mIsOnBackPressedFinishSettings = onBackPressedFinishSettingsMap.get( fragmentId );
		}
		switch( fragmentId )
		{
			case R.id.setting_effect:
				if( launcherEffectFragment == null )
				{
					//cheyingkun add start	//添加友盟统计自定义事件(桌面设置切页特效)
					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
					{
						MobclickAgent.onEvent( this , UmengStatistics.ENTER_DESKTOP_SLIDE_BY_LAUNCHER_SETTING );
					}
					//cheyingkun add end
					launcherEffectFragment = new LauncherEffectFragment();
					launcherEffectFragment.setIsFinishActivityOnBackPressed( mIsOnBackPressedFinishSettings );//cheyingkun add	//phenix仿S5效果,编辑模式底部按钮配置
					launcherEffectFragment.setOnDataChangedListener( this );
				}
				return launcherEffectFragment;
				//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
			case R.id.setting_workspace_scroll_type_id:
				if( mWorkspaceScrollTypeFragment == null )
				{
					//					//cheyingkun add start	//添加友盟统计自定义事件(桌面设置桌面模式)
					//					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
					//					{
					//						MobclickAgent.onEvent( this , UmengStatistics.ENTER_LAUNCHER_STYLEBY_LAUNCHER_SETTING );
					//					}
					//					//cheyingkun add end
					mWorkspaceScrollTypeFragment = new WorkspaceScrollTypeFragment();
					mWorkspaceScrollTypeFragment.setIsFinishActivityOnBackPressed( mIsOnBackPressedFinishSettings );//cheyingkun add	//phenix仿S5效果,编辑模式底部按钮配置
					mWorkspaceScrollTypeFragment.setOnDataChangedListener( this );
				}
				return mWorkspaceScrollTypeFragment;
				//xiatian add end
				//xiatian add start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
			case R.id.setting_applist_scroll_type_id:
				if( mApplistScrollTypeFragment == null )
				{
					//					//cheyingkun add start	//添加友盟统计自定义事件(桌面设置桌面模式)
					//					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
					//					{
					//						MobclickAgent.onEvent( this , UmengStatistics.ENTER_LAUNCHER_STYLEBY_LAUNCHER_SETTING );
					//					}
					//					//cheyingkun add end
					mApplistScrollTypeFragment = new ApplistScrollTypeFragment();
					mApplistScrollTypeFragment.setIsFinishActivityOnBackPressed( mIsOnBackPressedFinishSettings );//cheyingkun add	//phenix仿S5效果,编辑模式底部按钮配置
					mApplistScrollTypeFragment.setOnDataChangedListener( this );
				}
				return mApplistScrollTypeFragment;
				//xiatian add end
				//xiatian add start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
			case R.id.setting_widget_scroll_type_id:
				if( mWidgetScrollTypeFragment == null )
				{
					//					//cheyingkun add start	//添加友盟统计自定义事件(桌面设置桌面模式)
					//					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
					//					{
					//						MobclickAgent.onEvent( this , UmengStatistics.ENTER_LAUNCHER_STYLEBY_LAUNCHER_SETTING );
					//					}
					//					//cheyingkun add end
					mWidgetScrollTypeFragment = new WidgetScrollTypeFragment();
					mWidgetScrollTypeFragment.setIsFinishActivityOnBackPressed( mIsOnBackPressedFinishSettings );//cheyingkun add	//phenix仿S5效果,编辑模式底部按钮配置
					mWidgetScrollTypeFragment.setOnDataChangedListener( this );
				}
				return mWidgetScrollTypeFragment;
				//xiatian add end
			case R.id.setting_launcher_stytle:
				if( launcherStyleFragment == null )
				{
					//cheyingkun add start	//添加友盟统计自定义事件(桌面设置桌面模式)
					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
					{
						MobclickAgent.onEvent( this , UmengStatistics.ENTER_LAUNCHER_STYLEBY_LAUNCHER_SETTING );
					}
					//cheyingkun add end
					launcherStyleFragment = new LauncherStyleFragment();
					launcherStyleFragment.setIsFinishActivityOnBackPressed( mIsOnBackPressedFinishSettings );//cheyingkun add	//phenix仿S5效果,编辑模式底部按钮配置
					launcherStyleFragment.setOnDataChangedListener( this );
				}
				return launcherStyleFragment;
			case R.id.setting_launcher_classification:
				if( launcherClassificationFragment == null )
				{
					//cheyingkun add start	//添加友盟统计自定义事件(桌面设置智能分类)
					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG && isNeedAnim )
					{
						MobclickAgent.onEvent( this , UmengStatistics.ENTER_CATEGORY_BY_LAUNCHER_SETTING );
					}
					//cheyingkun add end
					launcherClassificationFragment = new LauncherClassificationFragment();
					launcherClassificationFragment.setIsFinishActivityOnBackPressed( mIsOnBackPressedFinishSettings );//cheyingkun add	//phenix仿S5效果,编辑模式底部按钮配置
					//launcherClassificationFragment.setOnDataChangedListener( this );
				}
				return launcherClassificationFragment;
				//WangLei add start //桌面和主菜单特效的分离
			case R.id.setting_workspace_effect: //双层模式桌面切页特效设置
				if( launcherEffectFragment == null )
				{
					//cheyingkun add start	//添加友盟统计自定义事件(桌面设置切页特效)
					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
					{
						MobclickAgent.onEvent( this , UmengStatistics.ENTER_DESKTOP_SLIDE_BY_LAUNCHER_SETTING );
					}
					//cheyingkun add end
					launcherEffectFragment = new LauncherEffectFragment();
					launcherEffectFragment.setIsFinishActivityOnBackPressed( mIsOnBackPressedFinishSettings );//cheyingkun add	//phenix仿S5效果,编辑模式底部按钮配置
					launcherEffectFragment.setOnDataChangedListener( this );
				}
				return launcherEffectFragment;
			case R.id.setting_applist_effect: //双层模式主菜单切页特效设置
				if( appListEffectFragment == null )
				{
					//cheyingkun add start	//添加友盟统计自定义事件(桌面设置切页特效)
					if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
					{
						MobclickAgent.onEvent( this , UmengStatistics.ENTER_DESKTOP_SLIDE_BY_LAUNCHER_SETTING );
					}
					//cheyingkun add end
					appListEffectFragment = new LauncherAppListEffectFragment();
					appListEffectFragment.setIsFinishActivityOnBackPressed( mIsOnBackPressedFinishSettings );//cheyingkun add	//phenix仿S5效果,编辑模式底部按钮配置
					appListEffectFragment.setOnDataChangedListener( this );
				}
				return appListEffectFragment;
				//WangLei add end
				//cheyingkun add start	//和兴六部图标大小变化需求
			case R.id.setting_app_icon_size:
				if( LauncherDefaultConfig.SWITCH_ENABLE_CUSTOM_LAYOUT && launcherAppIconSizeFragment == null )
				{
					launcherAppIconSizeFragment = new LauncherAppIconSizeFragment();
					launcherAppIconSizeFragment.setIsFinishActivityOnBackPressed( mIsOnBackPressedFinishSettings );//cheyingkun add	//phenix仿S5效果,编辑模式底部按钮配置
					launcherAppIconSizeFragment.setOnDataChangedListener( this );
				}
				return launcherAppIconSizeFragment;
				//cheyingkun add end	//和兴六部图标大小变化需求
				// zhangjin@2015/12/17 ADD START
			case R.id.setting_about_launcher:
				Intent intent = new Intent();
				intent.setComponent( new ComponentName( this , UpdateActivity.class ) );
				intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				intent.addFlags( Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
				startActivity( intent );
				//cheyingkun add start	//自更新添加友盟统计
				if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
				{//桌面设置里的自更新
					HashMap<String , String> map = new HashMap<String , String>();
					map.put( UmengStatistics.UPDATE_BY_SELF_TITLE , UmengStatistics.UPDATE_BY_SELF_LAUNCHER_SETTING );
					MobclickAgent.onEvent( getApplicationContext() , UmengStatistics.UPDATE_BY_SELF , map );
				}
				//cheyingkun add end	//自更新添加友盟统计
				return null;
				// zhangjin@2015/12/17 ADD END
				//cheyingkun add start	//设置默认桌面引导
			case R.id.setting_launcher_home:
				//cheyingkun add start	//添加友盟统计自定义事件
				if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
				{
					/**设置默认桌面*/
					MobclickAgent.onEvent( getApplicationContext() , "set_default_desktop" );
				}
				//cheyingkun add end
				( (Launcher)LauncherAppState.getActivityInstance() ).showWorkspace( false );
				DefaultLauncherGuideManager.getInstance().showLauncherSelecetDialog( getApplicationContext() );
				onBackPressed();
				return null;
				//cheyingkun add end
			default:
				return null;
		}
	}
	
	private void switchToHeaderInner(
			int fragmentId )
	{
		settingPreferenceFragment = getCurrentPreferenceFragment( fragmentId );
		if( settingPreferenceFragment != null )
		{
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			if( isNeedAnim )//cheyingkun add	//修改桌面默认配置
			{
				//xiatian start	//桌面设置进入和退出二级菜单的动画，适配“从右往左”显示的语言（例如：“阿拉伯”语）:“从右往左”显示时，从左往右进入二级菜单，从右往左退出二级菜单。
				//			transaction.setCustomAnimations( R.anim.anim_launcher_settings_sub_menu_enter , 0 );//xiatian del
				//xiatian add start
				if( isLayoutRtl() )
				{
					transaction.setCustomAnimations( R.anim.anim_launcher_settings_sub_menu_l2r_enter , 0 );
				}
				else
				{
					transaction.setCustomAnimations( R.anim.anim_launcher_settings_sub_menu_r2l_enter , 0 );
				}
				//xiatian add end
				//xiatian end
			}
			isNeedAnim = true;//cheyingkun add	//修改桌面默认配置
			if( !settingPreferenceFragment.isAdded() )
			{
				transaction.add( R.id.fragment_content , settingPreferenceFragment );
			}
			else
			{
				transaction.show( settingPreferenceFragment );
				settingPreferenceFragment.getListView().requestFocus();//cheyingkun add	//桌面设置界面支持按键【i_0014557】
			}
			mListView.setFocusable( false );//cheyingkun add	//桌面设置界面支持按键【i_0014557】
			transaction.commit();
		}
	}
	
	@Override
	public void onDataChanged()
	{
		// TODO Auto-generated method stub
		refleshSummary( mHeaders );
		notifyDataSetChanged();
	}
	
	@Override
	public void onBackPressed()
	{
		//cheyingkun add start	//phenix仿S5效果,编辑模式底部按钮配置
		if(
		//
		( settingPreferenceFragment != null )
		//
		&& ( settingPreferenceFragment.isHidden() == false )
		//
		&& ( settingPreferenceFragment.isFinishActivityOnBackPressed() )
		//
		)
		{
			finish();
			return;
		}
		//cheyingkun add end
		// TODO Auto-generated method stub
		//xiatian start	//拓展BaseListPreferenceFragment基类，去掉onBackPressed方法，添加onBackPressedWithAnim方法和onBackPressedWithOutAnim方法。
		//xiatian del start
		//		if( settingPreferenceFragment != null && !settingPreferenceFragment.isHidden() )
		//		{
		//			//Fragment的返回键响应
		//			settingPreferenceFragment.onBackPressed();
		//		}
		//		else
		//xiatian del end
		if( !onBackPressedWithAnim() )//xiatian add
		//xiatian end
		{
			super.onBackPressed();
		}
	}
	
	//xiatian add start	//拓展BaseListPreferenceFragment基类，去掉onBackPressed方法，添加onBackPressedWithAnim方法和onBackPressedWithOutAnim方法。
	public boolean onBackPressedWithAnim()
	{
		//cheyingkun add start	//桌面设置界面支持按键【i_0014557】
		mListView.setFocusable( true );
		mListView.requestFocus();
		//cheyingkun add end
		if( settingPreferenceFragment != null && !settingPreferenceFragment.isHidden() )
		{
			//Fragment的返回键响应
			settingPreferenceFragment.onBackPressedWithAnim();
			settingPreferenceFragment.getListView().clearFocus();//cheyingkun add	//桌面设置界面支持按键【i_0014557】
			return true;
		}
		return false;
	}
	
	public void onBackPressedWithOutAnim()
	{
		if( settingPreferenceFragment != null && !settingPreferenceFragment.isHidden() )
		{
			//Fragment的返回键响应
			settingPreferenceFragment.onBackPressedWithOutAnim();
		}
	}
	//xiatian add end
	;
	
	@Override
	protected void onDestroy()
	{
		onBackPressedWithOutAnim();//xiatian add	//fix bug：解决“在phenix桌面没有设置为默认桌面的前提下，在桌面设置的二级界面（屏幕切页特效界面、切换桌面模式界面以及智能分类界面）中，按home回到默认桌面后，当前界面没有保存Fragment的状态（并且特效界面没有保存当前选中特效）”的问题。
		LauncherAppState.getInstance().setLauncherSettingsActivity( null );//xiatian add	//fix bug：解决“在phenix桌面设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home回到默认桌面后，桌面特效为之前的桌面特效（没有保存刚刚在屏幕切页特效界面中的选择）”的问题。【i_0010438】
		super.onDestroy();
	}
	
	//xiatian add start	//fix bug：解决“在phenix桌面设置为默认桌面的前提下，在桌面设置的屏幕切页特效界面中，按home回到默认桌面后，桌面特效为之前的桌面特效（没有保存刚刚在屏幕切页特效界面中的选择）”的问题。【i_0010438】
	public void saveWorkspaceEfffect()
	{
		if( ( settingPreferenceFragment != null ) && ( settingPreferenceFragment instanceof LauncherEffectFragment ) && ( !settingPreferenceFragment.isHidden() ) )
		{
			LauncherEffectFragment mLauncherEffectFragment = (LauncherEffectFragment)settingPreferenceFragment;
			mLauncherEffectFragment.saveValue();
		}
		//WangLei add start //桌面和主菜单特效的分离
		else if( ( settingPreferenceFragment != null ) && ( settingPreferenceFragment instanceof LauncherAppListEffectFragment ) && ( !settingPreferenceFragment.isHidden() ) )
		{
			LauncherAppListEffectFragment mAllAppEffectFragment = (LauncherAppListEffectFragment)settingPreferenceFragment;
			mAllAppEffectFragment.saveValue();
		}
		//WangLei add end
	}
	//xiatian add end
	;
	
	//cheyingkun add start	//优化编辑模式底边栏配置桌面设置二级界面的方式
	private int getHeardIdByIndex(
			int headerIndex )
	{
		int id = -1;
		switch( headerIndex )
		{
			case HEADER_ID_WORKSPACE_EFFECT_IN_CORE://单层桌面，桌面切页特效
				id = R.id.setting_effect;
				break;
			case HEADER_ID_LAUNCHER_STYLE://桌面模式（单双层切换）
				id = R.id.setting_launcher_stytle;
				break;
			case HEADER_ID_CLASSIFICATION://智能分类
				id = R.id.setting_launcher_classification;
				break;
			case HEADER_ID_WORKSPACE_EFFECT_IN_DRAWER://双层桌面，桌面切页特效
				id = R.id.setting_workspace_effect;
				break;
			case HEADER_ID_APPLIST_EFFECT_IN_DRAWER://双层桌面，主菜单切页特效
				id = R.id.setting_applist_effect;
				break;
			default:
				break;
		}
		return id;
	}
	//cheyingkun add end
	;
	
	//cheyingkun add start	//桌面设置界面支持按键【i_0014557】(关于桌面进入后返回焦点异常,添加该处修改)
	@Override
	protected void onResumeFragments()
	{
		super.onResumeFragments();
		mListView.requestFocus();
	}
	//cheyingkun add end
}
