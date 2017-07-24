// xiatian add whole file //CheckIntegrity（添加：检查“微入口模块”完整性）
package com.cooee.framework.CheckIntegrity;


import java.util.ArrayList;

import com.cooee.CheckIntegrity.CheckIntegrityModeBase;


public class CheckIntegrityModeMicroEntry implements CheckIntegrityModeBase
{
	
	@Override
	public ArrayList<String> getNeed2CheckActivityList()
	{
		ArrayList<String> mNeed2CheckActivityList = new ArrayList<String>();
		mNeed2CheckActivityList.add( "com.iLoong.launcher.MList.ApkMangerActivity" );
		mNeed2CheckActivityList.add( "com.iLoong.launcher.MList.MainActivity" );
		mNeed2CheckActivityList.add( "com.iLoong.launcher.MList.MEServiceActivity" );
		//xiatian add start	//微入口不检查以下几个Activity
		//【备注】由于以下四个Activity在AndroidManifest.xml中申明时，android:enabled为"false"，所以查不到。
		//		mNeed2CheckActivityList.add( "com.iLoong.launcher.MList.Main_FirstActivity" );
		//		mNeed2CheckActivityList.add( "com.iLoong.launcher.MList.Main_SecondActivity" );
		//		mNeed2CheckActivityList.add( "com.iLoong.launcher.MList.Main_ThreeActivity" );
		//		mNeed2CheckActivityList.add( "com.iLoong.launcher.MList.Main_FourthActicity" );
		//xiatian add end
		return mNeed2CheckActivityList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckAnimList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckArrayList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckAssetsList()
	{
		ArrayList<String> mNeed2CheckAssetsList = new ArrayList<String>();
		mNeed2CheckAssetsList.add( "cool_ml_NoNet.htm" );
		mNeed2CheckAssetsList.add( "cool_ml_noNetwork.png" );
		return mNeed2CheckAssetsList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckAttrList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckBoolList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckColorList()
	{
		ArrayList<String> mNeed2CheckColorList = new ArrayList<String>();
		mNeed2CheckColorList.add( "cool_ml_apk_manager_divider_between_tab_and_list_color" );
		mNeed2CheckColorList.add( "cool_ml_apk_manager_title_bar_bg_color" );
		mNeed2CheckColorList.add( "cool_ml_apk_manager_title_bar_title_text_color" );
		mNeed2CheckColorList.add( "cool_ml_manager_download_item_app_name_text_color" );
		mNeed2CheckColorList.add( "cool_ml_manager_download_item_info_text_color" );
		mNeed2CheckColorList.add( "cool_ml_manager_install_item_app_name_text_color" );
		mNeed2CheckColorList.add( "cool_ml_manager_install_item_info_text_color" );
		mNeed2CheckColorList.add( "cool_ml_apk_manager_install_item_install_button_text_color" );
		mNeed2CheckColorList.add( "cool_ml_apk_manager_install_item_run_button_text_color" );
		mNeed2CheckColorList.add( "cool_ml_apk_manager_download_and_install_list_bg_color" );
		return mNeed2CheckColorList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckDimenList()
	{
		ArrayList<String> mNeed2CheckDimenList = new ArrayList<String>();
		mNeed2CheckDimenList.add( "cool_ml_apk_manager_title_bar_height" );
		mNeed2CheckDimenList.add( "cool_ml_apk_manager_title_bar_back_button_padding_left" );
		mNeed2CheckDimenList.add( "cool_ml_apk_manager_title_bar_title_text_size" );
		mNeed2CheckDimenList.add( "cool_ml_apk_manager_title_bar_gap_x_between_back_button_and_title" );
		mNeed2CheckDimenList.add( "cool_ml_apk_manager_tab_bar_title_text_size" );
		mNeed2CheckDimenList.add( "cool_ml_manager_download_item_app_name_text_size" );
		mNeed2CheckDimenList.add( "cool_ml_manager_download_item_info_text_size" );
		mNeed2CheckDimenList.add( "cool_ml_manager_install_item_app_name_text_size" );
		mNeed2CheckDimenList.add( "cool_ml_manager_install_item_info_text_size" );
		return mNeed2CheckDimenList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckDrawableList()
	{
		ArrayList<String> mNeed2CheckDrawableList = new ArrayList<String>();
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_download_and_install_item_bg_selector" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_download_item_progressbar_layerlist" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_install_item_install_button_selector" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_install_item_run_button_selector" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_title_bar_back_button_selector" );
		mNeed2CheckDrawableList.add( "cool_ml_pageselect_button_underline" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_download_item_progressbar_bg" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_download_item_progressbar_progress" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_install_item_install_button_focus" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_install_item_install_button_normal" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_install_item_run_button_focus" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_install_item_run_button_normal" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_title_bar_back_button_focus" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_title_bar_back_button_normal" );
		mNeed2CheckDrawableList.add( "cool_ml_bg_item_bottom_normal" );
		mNeed2CheckDrawableList.add( "cool_ml_bg_item_top_normal" );
		mNeed2CheckDrawableList.add( "cool_ml_discalmer_bg" );
		mNeed2CheckDrawableList.add( "cool_ml_discalmer_del" );
		mNeed2CheckDrawableList.add( "cool_ml_download_install" );
		mNeed2CheckDrawableList.add( "cool_ml_icon_btn_list_download" );
		mNeed2CheckDrawableList.add( "cool_ml_icon_btn_list_pause" );
		mNeed2CheckDrawableList.add( "cool_ml_icon_btn_list_waiting_download" );
		mNeed2CheckDrawableList.add( "cool_ml_know" );
		mNeed2CheckDrawableList.add( "cool_ml_ku_store" );
		mNeed2CheckDrawableList.add( "cool_ml_no_data" );
		mNeed2CheckDrawableList.add( "cool_ml_notify" );
		mNeed2CheckDrawableList.add( "cool_ml_software" );
		mNeed2CheckDrawableList.add( "cool_ml_underline_press" );
		mNeed2CheckDrawableList.add( "cool_ml_underline_unpress" );
		mNeed2CheckDrawableList.add( "cool_ml_webview_loading_press" );
		mNeed2CheckDrawableList.add( "cool_ml_wonderful_game" );
		mNeed2CheckDrawableList.add( "cool_ml_know_small" );
		mNeed2CheckDrawableList.add( "cool_ml_ku_store_small" );
		mNeed2CheckDrawableList.add( "cool_ml_notify_small" );
		mNeed2CheckDrawableList.add( "cool_ml_software_small" );
		mNeed2CheckDrawableList.add( "cool_ml_wonderful_game_small" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_download_and_install_item_bg_focus" );
		mNeed2CheckDrawableList.add( "cool_ml_apk_manager_download_and_install_item_bg_normal" );
		return mNeed2CheckDrawableList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckIdList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckIntegerList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckLayoutList()
	{
		ArrayList<String> mNeed2CheckLayoutList = new ArrayList<String>();
		mNeed2CheckLayoutList.add( "cool_ml_activity_main" );
		mNeed2CheckLayoutList.add( "cool_ml_apk_download_view" );
		mNeed2CheckLayoutList.add( "cool_ml_apk_install_view" );
		mNeed2CheckLayoutList.add( "cool_ml_apk_manager" );
		mNeed2CheckLayoutList.add( "cool_ml_disclaimer_dialog" );
		mNeed2CheckLayoutList.add( "cool_ml_dwonload_notification" );
		mNeed2CheckLayoutList.add( "cool_ml_manager_download_listview" );
		mNeed2CheckLayoutList.add( "cool_ml_manager_install_listview" );
		mNeed2CheckLayoutList.add( "cool_ml_onlongclick_listview_download" );
		mNeed2CheckLayoutList.add( "cool_ml_onlongclick_listview_install" );
		mNeed2CheckLayoutList.add( "cool_ml_page_header" );
		mNeed2CheckLayoutList.add( "cool_ml_webview_loading_dlg" );
		return mNeed2CheckLayoutList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckLibList()
	{
		ArrayList<String> lib = new ArrayList<String>();
		lib.add( "com.iLoong.launcher.MList.MainActivity" );
		return lib;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckMipmapList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckPermissionList()
	{
		ArrayList<String> mNeed2CheckPermissionList = new ArrayList<String>();
		mNeed2CheckPermissionList.add( "android.permission.INTERNET" );
		mNeed2CheckPermissionList.add( "android.permission.WRITE_EXTERNAL_STORAGE" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_NETWORK_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.READ_PHONE_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.READ_SMS" );
		mNeed2CheckPermissionList.add( "android.permission.READ_CALL_LOG" );
		mNeed2CheckPermissionList.add( "android.permission.READ_CONTACTS" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_WIFI_STATE" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_FINE_LOCATION" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_MOCK_LOCATION" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_COARSE_LOCATION" );
		mNeed2CheckPermissionList.add( "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" );
		mNeed2CheckPermissionList.add( "android.permission.SYSTEM_ALERT_WINDOW" );
		return mNeed2CheckPermissionList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckReceiverList()
	{
		ArrayList<String> mNeed2CheckReceiverList = new ArrayList<String>();
		mNeed2CheckReceiverList.add( "cool.sdk.update.manager.UpdateReceiver" );
		mNeed2CheckReceiverList.add( "com.iLoong.launcher.MList.MyReceiver" );
		mNeed2CheckReceiverList.add( "com.iLoong.launcher.MList.CooeeMsgReceiver" );
		return mNeed2CheckReceiverList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckServiceList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckStringList()
	{
		ArrayList<String> mNeed2CheckStringList = new ArrayList<String>();
		mNeed2CheckStringList.add( "cool_ml_app_name1" );
		mNeed2CheckStringList.add( "cool_ml_app_name2" );
		mNeed2CheckStringList.add( "cool_ml_app_name3" );
		mNeed2CheckStringList.add( "cool_ml_app_name4" );
		mNeed2CheckStringList.add( "cool_ml_donwloadorinstall_manager" );
		mNeed2CheckStringList.add( "cool_ml_install_manager" );
		mNeed2CheckStringList.add( "cool_ml_donwload_manager" );
		mNeed2CheckStringList.add( "cool_ml_download_failed" );
		mNeed2CheckStringList.add( "cool_ml_network_not_available" );
		mNeed2CheckStringList.add( "cool_ml_storage_not_available" );
		mNeed2CheckStringList.add( "cool_ml_download_jixu" );
		mNeed2CheckStringList.add( "cool_ml_download_quxiao" );
		mNeed2CheckStringList.add( "cool_ml_install_jixu" );
		mNeed2CheckStringList.add( "cool_ml_install_quxiao" );
		mNeed2CheckStringList.add( "cool_ml_new_content" );
		mNeed2CheckStringList.add( "cool_ml_more_content" );
		mNeed2CheckStringList.add( "cool_ml_dl_ing" );
		mNeed2CheckStringList.add( "cool_ml_dl_ing_text" );
		mNeed2CheckStringList.add( "cool_ml_dl_stop" );
		mNeed2CheckStringList.add( "cool_ml_dl_stop_text" );
		mNeed2CheckStringList.add( "cool_ml_dl_sucess" );
		mNeed2CheckStringList.add( "cool_ml_dl_sucess_text" );
		mNeed2CheckStringList.add( "cool_ml_dl_failed" );
		mNeed2CheckStringList.add( "cool_ml_dl_failed_text" );
		mNeed2CheckStringList.add( "cool_ml_dl_installed" );
		mNeed2CheckStringList.add( "cool_ml_dl_installed_text" );
		mNeed2CheckStringList.add( "cool_ml_install_file_not_exsit" );
		mNeed2CheckStringList.add( "cool_ml_disclaimer_title" );
		mNeed2CheckStringList.add( "cool_ml_disclaimer_desc" );
		mNeed2CheckStringList.add( "cool_ml_disclaimer_update" );
		mNeed2CheckStringList.add( "cool_ml_apk_manager_install_item_install_button_text" );
		mNeed2CheckStringList.add( "cool_ml_apk_manager_install_item_run_button_text" );
		mNeed2CheckStringList.add( "cool_ml_apk_manager_download_item_state_wait" );
		mNeed2CheckStringList.add( "cool_ml_apk_manager_download_item_state_pause" );
		return mNeed2CheckStringList;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckStyleList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckStyleableList()
	{
		return null;
	}
	
	@Override
	public ArrayList<String> getNeed2CheckXmlList()
	{
		return null;
	}
}
