package com.cooee.phenix.editmode.provider;


import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.editmode.item.EditModelItem;
import com.cooee.phenix.editmode.item.EditModelThemeItem;
import com.cooee.theme.ThemeManager;
import com.cooee.theme.ThemeReceiver;
import com.cooee.util.Tools;


public class EditThemeProvider implements EditModelProviderBase
{
	
	private final String themeAction = "com.coco.themes";
	/**更换主题的uMeng统计的key*/
	private final String themeChangeUMengKey = "theme_change";//cheyingkun add	//添加友盟统计自定义事件
	private String currentThemePkgName = null;
	private int itemWidth = -1;
	
	@Override
	public ArrayList<EditModelItem> loadAllModelData(
			Context context ,
			String key )
	{
		currentThemePkgName = ThemeManager.getInstance().getCurrentThemeDescription().componentName.getPackageName();
		itemWidth = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_theme_item_imageview_width );
		if( itemWidth < 0 )
		{
			itemWidth = Utilities.sIconWidth;
		}
		Intent intent = new Intent( themeAction , null );
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> themesinfo = packageManager.queryIntentActivities( intent , 0 );
		ArrayList<EditModelItem> mEditModelDatas = addNewModelData( context , themesinfo , key );
		addDefaultTheme( mEditModelDatas , context , key );
		if( LauncherDefaultConfig.getBoolean( R.bool.switch_enable_show_more_in_edit_mode_hotseat_v2_theme ) )//xiatian add	//添加配置项“switch_enable_show_more_in_edit_mode_hotseat_v2_theme”，是否在“编辑模式底边栏二级界面（主题）”中显示“more”按钮。true显示；false不显示。默认true。（config_edit_mode_button_enter_theme_style为1有效）
		{
			addBeautyCenter( mEditModelDatas , context , key );
		}
		return mEditModelDatas;
	}
	
	/**
	 * 添加默认主题
	 * @param mEditModelDatas
	 */
	private void addDefaultTheme(
			ArrayList<EditModelItem> mEditModelDatas ,
			Context context ,
			String key )
	{
		String title = context.getResources().getString( R.string.edit_theme_default_theme );
		Drawable drawable = context.getResources().getDrawable( R.drawable.edit_theme_default );//bitmapdrawable中的bitmap不能释放，res.getDrawable这个方法每次得到的drawable对象不一样，但是((BitmapDrawable)drawable).getBitmap()是同一个值
		Bitmap defaultIcon = Tools.drawableToBitmap( drawable , itemWidth , itemWidth );
		Intent themeintent = createThemeIntent( context.getPackageName() );
		mEditModelDatas.add( getEditModelItem( context.getPackageName() , title , themeintent , key , defaultIcon ) );
	}
	
	/**
	 * 添加美化中心
	 * @param mEditModelDatas
	 */
	private void addBeautyCenter(
			ArrayList<EditModelItem> mEditModelDatas ,
			Context context ,
			String key )
	{
		Drawable drawable = context.getResources().getDrawable( R.drawable.edit_theme_more );//bitmapdrawable中的bitmap不能释放，res.getDrawable这个方法每次得到的drawable对象不一样，但是((BitmapDrawable)drawable).getBitmap()是同一个值
		Bitmap moreIcon = Tools.drawableToBitmap( drawable , itemWidth , itemWidth );
		String title = context.getResources().getString( R.string.edit_more );
		ComponentName componentName = new ComponentName( ThemeManager.BEAUTY_CENTER_PACKAGE_NAME , "com.cooee.BeautyCenter.tabTheme" );
		Intent themeintent = new Intent();
		themeintent.setComponent( componentName );
		EditModelItem beautyModelItem = getEditModelItem( ThemeManager.BEAUTY_CENTER_PACKAGE_NAME , title , themeintent , key , moreIcon );//获得美化中心的ModelItem数据
		mEditModelDatas.add( beautyModelItem );
	}
	
	/**
	 * 通过传入的参数获得EditModelItem
	 * @param bitmap
	 * @param title
	 * @param intent
	 * @return
	 */
	private EditModelItem getEditModelItem(
			String pkg ,
			String title ,
			Intent intent ,
			String key ,
			Bitmap bmp )
	{
		EditModelThemeItem editModelData = new EditModelThemeItem();
		editModelData.setTitle( title );
		editModelData.setKey( key );
		editModelData.setThemeIntent( intent );
		editModelData.setPackageNameKey( pkg );
		if( currentThemePkgName != null && currentThemePkgName.equals( pkg ) )
		{
			editModelData.setSelected( true );
		}
		else
		{
			editModelData.setSelected( false );
		}
		editModelData.setUMengKey( themeChangeUMengKey );//cheyingkun add	//添加友盟统计自定义事件
		editModelData.setBitmap( bmp );
		return editModelData;
	}
	
	@Override
	public ArrayList<EditModelItem> addNewModelData(
			Context context ,
			List<?> themesinfo ,
			String key )
	{
		ArrayList<EditModelItem> editModelDatas = new ArrayList<EditModelItem>();
		PackageManager packageManager = context.getPackageManager();
		for( int i = 0 ; i < themesinfo.size() ; i++ )
		{
			ResolveInfo info = (ResolveInfo)themesinfo.get( i );
			String title = info.loadLabel( packageManager ).toString();
			Intent themeintent = createThemeIntent( info.activityInfo.packageName );
			Drawable drawable = info.loadIcon( packageManager );
			// YANGTIANYU@2015/11/27 UPD START
			//Bitmap icon = Tools.drawableToSmallBitamp( drawable );
			int iconSize = itemWidth;
			Bitmap icon = Tools.drawableToBitmap( drawable , iconSize , iconSize );
			editModelDatas.add( getEditModelItem( info.activityInfo.packageName , title , themeintent , key , icon ) );
		}
		return editModelDatas;
	}
	
	/**
	* 根据包名生成主题Intent
	*/
	private Intent createThemeIntent(
			String pkgName )
	{
		Intent themeintent = new Intent( ThemeReceiver.ACTION_LAUNCHER_APPLY_THEME );
		themeintent.putExtra( "theme_status" , 1 );
		themeintent.putExtra( "theme" , pkgName );
		return themeintent;
	}
	
	@Override
	public void updateModeItem(
			ArrayList<EditModelItem> list )
	{
		// TODO Auto-generated method stub
		currentThemePkgName = ThemeManager.getInstance().getCurrentThemeDescription().componentName.getPackageName();
		for( EditModelItem item : list )
		{
			if( currentThemePkgName != null && currentThemePkgName.equals( item.getPackageNameKey() ) )
			{
				item.setSelected( true );
			}
			else
			{
				item.setSelected( false );
			}
		}
	}
}
