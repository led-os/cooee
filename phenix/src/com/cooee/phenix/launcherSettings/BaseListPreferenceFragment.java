package com.cooee.phenix.launcherSettings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public abstract class BaseListPreferenceFragment extends ListFragment
{
	
	private OnDataChangedListener mOnDataChangedListener;
	
	/**
	 * 所有继承BackHandledFragment的子类都将在这个方法中实现物理Back键按下后的逻辑（保存状态等。。。）
	 * FragmentActivity捕捉到物理返回键点击事件后会首先询问Fragment是否消费该事件
	 * 如果没有Fragment消息时FragmentActivity自己才会消费该事件
	 */
	//xiatian start	//拓展BaseListPreferenceFragment基类，去掉onBackPressed方法，添加onBackPressedWithAnim方法和onBackPressedWithOutAnim方法。
	//	protected abstract boolean onBackPressed();//xiatian del
	//xiatian add start
	protected abstract boolean onBackPressedWithAnim();//WithAnim
	
	protected abstract boolean onBackPressedWithOutAnim();//WithOutAnim
	//xiatian add end
	//xiatian end
	
	private boolean mIsFinishActivityOnBackPressed = false;
	
	public void setIsFinishActivityOnBackPressed(
			boolean mIsFinishActivityOnBackPressed )
	{
		this.mIsFinishActivityOnBackPressed = mIsFinishActivityOnBackPressed;
	}
	
	public boolean isFinishActivityOnBackPressed()
	{
		return mIsFinishActivityOnBackPressed;
	}
	
	public static final class SelectItemInfo
	{
		
		public CharSequence title;
		public CharSequence summary;
		public CharSequence value;
		
		public SelectItemInfo()
		{
			// Empty
		}
	}
	
	@Override
	public View onCreateView(
			LayoutInflater inflater ,
			ViewGroup container ,
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		View mView = inflater.inflate( R.layout.launcher_settings_listfragment_layout , container , false );
		return mView;
		//return super.onCreateView( inflater , container , savedInstanceState );
	}
	
	public boolean saveValue(
			String key ,
			String value )
	{
		SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( getActivity() );
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		if( key.equals( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE_KEY ) )
		{
			editor.putInt( key , Integer.valueOf( value ).intValue() );
		}
		//xiatian add start	//添加配置项“switch_enable_show_workspace_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“桌面滑动类型”菜单。true显示；false不显示。默认false。
		else if( key.equals( LauncherDefaultConfig.CONFIG_WORKSPACE_SCROLL_TYPE_KEY ) )
		{
			LauncherDefaultConfig.SWITCH_ENABLE_WORKSPACE_LOOP_SLIDE = ( Integer.valueOf( value ).intValue() == 0 ? false : true );
			editor.putBoolean( key , LauncherDefaultConfig.SWITCH_ENABLE_WORKSPACE_LOOP_SLIDE );
		}
		//xiatian add end
		//xiatian add start	//添加配置项“switch_enable_show_applist_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“主菜单滑动类型”菜单。true显示；false不显示。默认false。
		else if( key.equals( LauncherDefaultConfig.CONFIG_APPLIST_SCROLL_TYPE_KEY ) )
		{
			LauncherDefaultConfig.SWITCH_ENABLE_APPLIST_LOOP_SLIDE = ( Integer.valueOf( value ).intValue() == 0 ? false : true );
			editor.putBoolean( key , LauncherDefaultConfig.SWITCH_ENABLE_APPLIST_LOOP_SLIDE );
		}
		//xiatian add end
		//xiatian add start	//添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
		else if( key.equals( LauncherDefaultConfig.CONFIG_WIDGET_SCROLL_TYPE_KEY ) )
		{
			LauncherDefaultConfig.SWITCH_ENABLE_WIDGET_LOOP_SLIDE = ( Integer.valueOf( value ).intValue() == 0 ? false : true );
			editor.putBoolean( key , LauncherDefaultConfig.SWITCH_ENABLE_WIDGET_LOOP_SLIDE );
		}
		//xiatian add end
		else
		{
			editor.putString( key , value );
		}
		editor.commit();
		if( mOnDataChangedListener != null )
		{
			mOnDataChangedListener.onDataChanged();
		}
		return true;
	}
	
	public void setOnDataChangedListener(
			OnDataChangedListener onDataChangedListener )
	{
		mOnDataChangedListener = onDataChangedListener;
	}
	
	public interface OnDataChangedListener
	{
		
		void onDataChanged();
	}
	
	protected void exitWithAnim(
			int mAnimId )
	{
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.setCustomAnimations( 0 , mAnimId );
		transaction.hide( this );
		transaction.commit();
	}
	
	//xiatian add start	//桌面设置进入和退出二级菜单的动画，适配“从右往左”显示的语言（例如：“阿拉伯”语）:“从右往左”显示时，从左往右进入二级菜单，从右往左退出二级菜单。
	public boolean isLayoutRtl()
	{
		//WangLei start //bug:i_0011481,0011442  API19以下版本进入桌面设置界面，点击某一项桌面重启或异常停止
		//【原因】ViewParent的getLayoutDirection方法在API19引入，低版本造成找不到方法而报错
		//【解决方案】全部使用View的getLayoutDirection方法适应"从右往左显示"的语言
		//return( getView().getParent().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ); //WangLei del 
		//xiatian start	//整理判断“是否从左往右布局”的方法：由“mView.getLayoutDirection()”改为“getResources().getConfiguration().getLayoutDirection()”
		//xiatian del start
		//		return Tools.isLayoutRTL( getActivity().getWindow().getDecorView() );//WangLei add
		//xiatian del end
		return LauncherAppState.isLayoutRTL();//xiatian add
		//xiatian end
		//WangLei end
	}
	
	//xiatian add end
	//cheyingkun add start	//桌面设置界面支持按键【i_0014557】
	@Override
	public void onResume()
	{
		super.onResume();
		getListView().requestFocus();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		getListView().clearFocus();
	}
	//cheyingkun add end
}
