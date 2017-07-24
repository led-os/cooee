package com.cooee.phenix.editmode.provider;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.editmode.EditModeEntity;
import com.cooee.phenix.editmode.item.EditModelItem;
import com.cooee.phenix.editmode.item.EditModelWallpaperItem;
import com.cooee.phenix.editmode.provider.WallpaperUtils.WallPaperFile;
import com.cooee.theme.ThemeManager;
import com.cooee.util.Tools;


public class EditWallpaperProvider implements EditModelProviderBase
{
	
	/**更换壁纸的uMeng统计的key*/
	private final String wallpaperChangeUMengKey = "wallpaper_change";//cheyingkun add	//添加友盟统计自定义事件
	private float ratio = 214f / 172;//壁纸的宽和高的比例
	
	@Override
	public ArrayList<EditModelItem> loadAllModelData(
			Context context ,
			String key )
	{
		int width = ( context.getResources().getDisplayMetrics().widthPixels - LauncherDefaultConfig.getDimensionPixelSize( R.dimen.edit_mode_wallpaper_item_padding_horizon ) * 2 - LauncherDefaultConfig
				.getDimensionPixelSize( R.dimen.edit_mode_wallpaper_item_gap ) * ( ( EditModeEntity.ITME_SUM - 1 ) * 2 ) ) / EditModeEntity.ITME_SUM;
		int height = (int)( width / ratio );
		ArrayList<EditModelItem> mEditModelDatas = new ArrayList<EditModelItem>();
		ArrayList<WallPaperFile> mWallPapers = WallpaperUtils.loadWallpaperInfo( context );
		for( int i = 0 ; i < mWallPapers.size() ; i++ )
		{
			WallPaperFile wf = mWallPapers.get( i );
			Bitmap bmp = WallpaperUtils.getSmallWallpaper( context , wf , width , height );
			EditModelItem item = getEditModelItem( wf , bmp , key , i );
			mEditModelDatas.add( item );
		}
		if( LauncherDefaultConfig.getBoolean( R.bool.switch_enable_show_more_in_edit_mode_hotseat_v2_wallpaper ) )//xiatian add	//添加配置项“switch_enable_show_more_in_edit_mode_hotseat_v2_wallpaper”，是否在“编辑模式底边栏二级界面（壁纸）”中显示“more”按钮。true显示；false不显示。默认true。（config_edit_mode_button_enter_wallpaper_style为5有效）
		{
			Drawable drawable = context.getResources().getDrawable( R.drawable.edit_wallpaper_more );
			Bitmap moreBitmap = Tools.drawableToBitmap( drawable , width , height );
			//bitmapdrawable中的bitmap不能释放，res.getDrawable这个方法每次得到的drawable对象不一样，但是((BitmapDrawable)drawable).getBitmap()是同一个值
			//		if( drawable != null )
			//		{
			//			Tools.recycleDrawable( drawable );
			//			drawable = null;
			//		}
			EditModelWallpaperItem beautyModelItem = new EditModelWallpaperItem();//获得美化中心的ModelItem数据
			beautyModelItem.setKey( key );
			beautyModelItem.setBitmap( moreBitmap );
			beautyModelItem.setPackageNameKey( ThemeManager.BEAUTY_CENTER_PACKAGE_NAME );
			mEditModelDatas.add( beautyModelItem );
		}
		return mEditModelDatas;
	}
	
	/**
	 * 通过传入的参数拼接EditModelItem
	 * @param wallpaperFileName 
	 * @param position
	 * @return
	 */
	private EditModelItem getEditModelItem(
			WallPaperFile file ,
			Bitmap bmp ,
			String key ,
			int position )
	{
		EditModelWallpaperItem editModelData = new EditModelWallpaperItem();
		editModelData.setWallPaperFile( file );
		editModelData.setPackageNameKey( file.getFileName() );
		editModelData.setKey( key );
		editModelData.setBitmap( bmp );
		editModelData.setUMengKey( wallpaperChangeUMengKey );//cheyingkun add	//添加友盟统计自定义事件
		return editModelData;
	}
	
	@Override
	public ArrayList<EditModelItem> addNewModelData(
			Context context ,
			List<?> themesinfo ,
			String key )
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void updateModeItem(
			ArrayList<EditModelItem> lists )
	{
		// TODO Auto-generated method stub
	}
}
