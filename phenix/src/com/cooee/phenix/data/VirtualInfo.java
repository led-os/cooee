// xiatian add whole file //需求:桌面默认配置中，支持配置虚图标（虚图标配置的应用没安装时，支持下载；配置的应用安装后，正常打开）。
package com.cooee.phenix.data;


import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.graphics.Bitmap;
import android.util.Log;

import com.cooee.framework.theme.IOnThemeChanged;
import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.IconCache;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.LauncherSettings;
import com.cooee.phenix.UmengStatistics;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.theme.ThemeManager;
import com.umeng.analytics.MobclickAgent;


/**
 * Represents a launchable icon on the workspaces and in folders.
 */
public class VirtualInfo extends EnhanceItemInfo
//
implements IOnThemeChanged//zhujieping add,换主题不重启
{
	
	private static final String TAG = "VirtualInfo";
	public static final String VIRTUAL_TYPE = "mVirtualType";
	public static final int VIRTUAL_TYPE_ERROR = -1;//表示虚图标的“异常”图标：intent或者intent中的ComponentName为空；
	public static final int VIRTUAL_TYPE_NORMAL = 0;//表示虚图标的“常规”图标：1、没有安装应用时，点击后提示下载；2、安装应用时，点击打开界面
	public static final int VIRTUAL_TYPE_CATEGORY_ENTRY = 1;//表示虚图标的“智能分类入口”图标：点击打开一个智能分类的界面
	//xiatian add start	//需求：添加“一键换主题”功能（1、虚图标；2、点击后，从已经安装的其他主题中，随机换一个）。
	public static final int VIRTUAL_TYPE_ONE_KEY_APPLY_THEMEM = 2;//表示虚图标的“一键换主题”图标：点击后，从已经安装的其他主题中，随机换一个。
	//xiatian add end
	public static final String IS_VIRTUAL_CAN_UNINSTALL = "mIsCanUninstall";
	//	private static final long serialVersionUID = 1L;
	/**
	 * The intent used to start the activity Or download apk.
	 * (mVirtualType == LauncherSettings.Favorites.VIRTUAL_TYPE_NORMAL)&&(intent.getComponent()!=null)，打开界面或者根据包名下载apk。
	 */
	Intent intent;
	/**
	 * The application icon.
	 */
	protected Bitmap mIcon;
	private int mVirtualType = VIRTUAL_TYPE_ERROR;//虚图标的类型
	private boolean mCanUninstall = true;//虚图标是否可卸载
	//cheyingkun add start	//修改桌面默认配置
	public static final String IS_VIRTUAL_FOLLOW_APP_UNINSTALL = "mIsFollowAppUninstall";//卸载apk是否删除对应的虚图标的key
	private boolean mIsFollowAppUninstall = true;//是否跟随应用卸载删除虚图标
	//cheyingkun add end
	private ShortcutIconResource iconResource;//zhujieping add	//对于配置了图片的ShortcutInfo和VirtualInfo，需要保存图片相关信息
	;
	public static final String IS_ICON_FOLLOW_THEME = "mIsIconFollowTheme";//xiatian add	//需求：添加配置项“mIsIconFollowTheme”，虚图标的显示图标是否跟随主题（从主题中读取相应图标）。true为跟随主题；false为不跟随主题。默认为true。
	
	public VirtualInfo()
	{
		itemType = LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL;
	}
	
	public VirtualInfo(
			ShortcutInfo mShortcutInfo )
	{
		super( mShortcutInfo );
		if( mShortcutInfo.getTitle() == null )
		{
			mShortcutInfo.setTitle( "name = null" );
		}
		super.setTitle( mShortcutInfo.getTitle().toString() );
		setIntent( new Intent( mShortcutInfo.getIntent() ) );
		mIcon = mShortcutInfo.mIcon;
		iconResource = mShortcutInfo.getIconResource();//zhujieping add	//对于配置了图片的ShortcutInfo和VirtualInfo，需要保存图片相关信息
	}
	
	@Override
	public void onAddToDatabase(
			ContentValues values )
	{
		super.onAddToDatabase( values );
		String titleStr = title != null ? title.toString() : null;
		values.put( LauncherSettings.BaseLauncherColumns.TITLE , titleStr );
		//xiatian start	//fix bug：解决“先打开一个应用，再进行智能分类后，数据库中该数据的intent中，多了附加参数‘SourceBounds’”的问题。
		//【引起的问题】
		//		1、桌面中打开微信后进行智能分类
		//		2、清空微信数据后再次打开微信，并在微信的“询问是否创建快捷方式”界面选择创建快捷方式
		//		3、此时，能在桌面创建快捷方式
		//【问题原因】
		//		1、ShortcutInfo的intent中的附加参数‘SourceBounds’，实在launcher.java中的	public void onClick( View v )方法中添加的
		//		2、ShortcutInfo的intent中的附加参数‘SourceBounds’的作用是：在startActivity的时候，将图标在桌面中的位置信息当做附加参数传递出去，以备打开的activity的onCreate方法中使用
		//		3、进行智能分类时，会调用本方法，将intent中的附加参数‘SourceBounds’写入数据库中
		//		4、LauncherModel的static boolean shortcutExists( Context context ,String title ,Intent intent )方法中，由于数据库中的intent多了附加参数‘SourceBounds’导致错误。
		//【解决方案】
		//		本方法中，不将intent中的附加参数‘SourceBounds’在存入数据库。
		//		String uri = intent != null ? intent.toUri( 0 ) : null;//xiatian del
		//xiatian add start
		String uri = null;
		if( intent != null )
		{
			Intent mIntent = new Intent( intent );
			mIntent.setSourceBounds( null );
			uri = mIntent.toUri( 0 );
		}
		//xiatian add end
		//xiatian end
		values.put( LauncherSettings.BaseLauncherColumns.INTENT , uri );
		values.put( LauncherSettings.BaseLauncherColumns.ICON_TYPE , LauncherSettings.BaseLauncherColumns.ICON_TYPE_BITMAP );
		writeBitmap( values , mIcon );
	}
	
	@Override
	public String toString()
	{
		return StringUtils.concat(
				TAG ,
				"(title:" ,
				( title == null ? "null" : title.toString() ) ,
				"-intent:" ,
				( intent == null ? "null" : intent.toUri( 0 ) ) ,
				"-id:" ,
				getId() ,
				"-type:" ,
				getItemType() ,
				"-container:" ,
				getContainer() ,
				"-screen:" ,
				getScreenId() ,
				"-cellX:" ,
				getCellX() ,
				"-cellY:" ,
				getCellY() ,
				"-spanX:" ,
				getSpanX() ,
				"-spanY:" ,
				getSpanY() ,
				"-dropPos:" ,
				getDropPos() ,
				")" );
	}
	
	public void setIntent(
			Intent intent )
	{
		this.intent = intent;
		if( intent != null )
		{
			setVirtualType( intent.getIntExtra( VIRTUAL_TYPE , VIRTUAL_TYPE_ERROR ) );
			setCanUninstall( intent.getBooleanExtra( IS_VIRTUAL_CAN_UNINSTALL , true ) );
			setIsFollowAppUninstall( intent.getBooleanExtra( IS_VIRTUAL_FOLLOW_APP_UNINSTALL , true ) );//cheyingkun add	//修改桌面默认配置
		}
	}
	
	public Intent getIntent()
	{
		return intent;
	}
	
	public void setIcon(
			Bitmap b )
	{
		mIcon = b;
	}
	
	public Bitmap getIcon(
			IconCache iconCache )
	{
		if( mIcon == null )
		{
			updateIcon( iconCache );
		}
		return mIcon;
	}
	
	public Bitmap getIcon()
	{
		return mIcon;
	}
	
	public void updateIcon(
			IconCache iconCache )
	{
		mIcon = iconCache.getIcon( intent );
	}
	
	public void setCanUninstall(
			boolean mCanUninstall )
	{
		this.mCanUninstall = mCanUninstall;
	}
	
	public boolean getCanUninstall()
	{
		return mCanUninstall;
	}
	
	public void setVirtualType(
			int mVirtualType )
	{
		this.mVirtualType = mVirtualType;
	}
	
	public int getVirtualType()
	{
		return mVirtualType;
	}
	
	public boolean onClick(
			Launcher launcher )
	{//false：正常流程（打开应用界面），交给launcher的onClick处理
		boolean ret = false;
		if( mVirtualType == VIRTUAL_TYPE_NORMAL )
		{
			if( intent == null || intent.getComponent() == null )
			{
				if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
					Log.e( TAG , StringUtils.concat( "onClick error: " , toString() ) );
				return false;
			}
			ComponentName mComponentName = intent.getComponent();
			String mPackageName = mComponentName.getPackageName();
			//cheyingkun add start	//添加友盟统计自定义事件(美化中心图标)
			if(
			//
			LauncherDefaultConfig.SWITCH_ENABLE_UMENG
			// 
			&& ( ThemeManager.BEAUTY_CENTER_PACKAGE_NAME.equals( mPackageName ) )
			//
			)
			{
				String mClassName = mComponentName.getClassName();
				if( ThemeManager.BEAUTY_CENTER_TAB_THEME_CLASS_NAME.equals( mClassName ) )
				{//主题
				}
				else if( ThemeManager.BEAUTY_CENTER_TAB_WALLPAPER_CLASS_NAME.equals( mClassName ) )
				{//壁纸
				}
				else if( ThemeManager.BEAUTY_CENTER_TAB_LOCKER_CLASS_NAME.equals( mClassName ) )
				{//锁屏
				}
				MobclickAgent.onEvent( launcher , UmengStatistics.ENTER_BEAUTY_CENTER_BY_ICON );
			}
			//cheyingkun add end
			if( LauncherAppState.isApkInstalled( mPackageName ) == false )
			{
				launcher.downloadApkCooeeDialog( getTitle() , mPackageName , true );
				ret = true;
			}
		}
		else if( mVirtualType == VIRTUAL_TYPE_CATEGORY_ENTRY )
		{
			//cheyingkun add start	//添加友盟统计自定义事件(智能分类图标)
			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			{
				MobclickAgent.onEvent( launcher , UmengStatistics.ENTER_CATEGORY_BY_ICON );
			}
			//cheyingkun add end
			ret = launcher.onClickCategoryEntry();
		}
		//xiatian add start	//需求：添加“一键换主题”功能（1、虚图标；2、点击后，从已经安装的其他主题中，随机换一个）。
		else if( mVirtualType == VIRTUAL_TYPE_ONE_KEY_APPLY_THEMEM )
		{
			//			//cheyingkun add start	//添加友盟统计自定义事件(智能分类图标)
			//			if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
			//			{
			//				MobclickAgent.onEvent( launcher , UmengStatistics.ENTER_CATEGORY_BY_ICON );
			//			}
			//			//cheyingkun add end
			ret = launcher.onClickOneKeyApplyTheme();
		}
		//xiatian add end
		return ret;
	}
	
	//xiatian add start	//整理代码：整理接口willAcceptDrop
	@Override
	public boolean willAcceptDrop()
	{
		boolean ret = false;
		ret = super.willAcceptDrop();
		if( ret == false )
		{
			if( getItemType() == LauncherSettings.Favorites.ITEM_TYPE_VIRTUAL )
			{
				if( getCanUninstall() )
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return ret;
	}
	//xiatian add end
	;
	
	//cheyingkun add start	//修改桌面默认配置
	public boolean getIsFollowAppUninstall()
	{
		return mIsFollowAppUninstall;
	}
	
	public void setIsFollowAppUninstall(
			boolean mIsFollowAppUninstall )
	{
		this.mIsFollowAppUninstall = mIsFollowAppUninstall;
	}
	//cheyingkun add end
	;
	
	//zhujieping add start
	@Override
	public void onThemeChanged(
			Object arg0 ,
			Object arg1 )
	{
		// TODO Auto-generated method stub
		if( ( arg0 instanceof IconCache ) == false )
		{
			return;
		}
		//这里不释放ShortcutInfo的mIcon，释放ShortcutInfo的mIcon的相关操作，放在BubbleTextView的onThemeChanged方法中
		IconCache mIconCache = (IconCache)arg0;
		if( getIntent() != null && getIntent().getComponent() != null )
		{
			boolean mIsIconFollowTheme = intent.getBooleanExtra( VirtualInfo.IS_ICON_FOLLOW_THEME , true );
			Bitmap mBitmap = null;
			if( mIsIconFollowTheme )
			{
				mBitmap = mIconCache.getIcon( intent.getComponent() , getTitle() );
			}
			if( mBitmap == null && iconResource != null )
			{
				mBitmap = Utilities.createIconBitmapWhenItemIsThemeThirdPartyItem( mIconCache.getDrawableFromResource( iconResource ) , LauncherAppState.getInstance().getContext() , false );
			}
			if( mBitmap != null )
			{
				mIconCache.add( getIntent().getComponent() , mBitmap , getTitle() );
			}
		}
	}
	//zhujieping add end
	;
}
