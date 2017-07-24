// xiatian add whole file //添加配置项“switch_enable_show_widget_scroll_type_in_launcher_settings”，是否在“桌面设置”中显示“小组件滑动类型”菜单。true显示；false不显示。默认false。
package com.cooee.phenix.launcherSettings;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class WidgetScrollTypeFragment extends BaseListPreferenceFragment
{
	
	private static final String TAG = "ApplistScrollTypeFragment";
	private ImageView backButton;
	private TextView title;
	private String mKey;
	private String currentValue;
	private int mClickedEntryIndex;
	private List<SelectItemInfo> mItemInfoList = new ArrayList<SelectItemInfo>();
	private CharSequence[] mEntries;//显示项内容
	private CharSequence[] msubEntries;//显示副项内容
	private CharSequence[] mEntryValues;//显示项存储对应的Value值
	
	@Override
	public void onActivityCreated(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onActivityCreated( savedInstanceState );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "onActivityCreated" );
		initData();
		findView();
		LoadItemInfoList( mItemInfoList );
		getListView().setDividerHeight( 0 );
		getListView().setChoiceMode( ListView.CHOICE_MODE_SINGLE );
		getListView().setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				// TODO Auto-generated method stub
				if( mClickedEntryIndex != position )
				{
					String value = String.valueOf( position );
					currentValue = value;
					setItemChecked( value );
					saveValue( mKey , value );
				}
			}
		} );
		setListAdapter( new ListSelectAdapter( getActivity() , mItemInfoList ) );
	}
	
	private void initData()
	{
		mEntries = LauncherDefaultConfig.getStringArray( R.array.widget_scroll_type_entries );
		msubEntries = LauncherDefaultConfig.getStringArray( R.array.widget_scroll_type_sub_entries );
		mEntryValues = LauncherDefaultConfig.getStringArray( R.array.widget_scroll_type_entry_values );
		mKey = LauncherDefaultConfig.CONFIG_WIDGET_SCROLL_TYPE_KEY;
		currentValue = String.valueOf( LauncherDefaultConfig.SWITCH_ENABLE_WIDGET_LOOP_SLIDE ? 1 : 0 );
	}
	
	private void findView()
	{
		backButton = (ImageView)getView().findViewById( R.id.list_fragment_back );
		//xiatian add start	//桌面设置进入二级菜单的图片和退出二级菜单的图片，适配“从右往左”显示的语言（例如：“阿拉伯”语）:“从右往左”显示时，进入二级菜单的图片为左箭头，退出二级菜单的图片为右箭头。
		if( isLayoutRtl() )
		{
			backButton.setImageDrawable( getResources().getDrawable( R.drawable.launcher_settings_r2l_back ) );
		}
		//xiatian add end
		backButton.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				//cheyingkun add start	//phenix仿S5效果,编辑模式底部按钮配置
				if( isFinishActivityOnBackPressed() )
				{
					getActivity().finish();
					return;
				}
				//cheyingkun add end
				//xiatian start	//拓展BaseListPreferenceFragment基类，去掉onBackPressed方法，添加onBackPressedWithAnim方法和onBackPressedWithOutAnim方法。
				//				onBackPressed();//xiatian del
				onBackPressedWithAnim();//xiatian add
				//xiatian end
			}
		} );
		title = (TextView)getView().findViewById( R.id.list_fragment_title );
		title.setText( R.string.launcher_settings_widget_scroll_type_title );
	}
	
	public void LoadItemInfoList(
			List<SelectItemInfo> mItemInfoList )
	{
		if( mEntries == null )
		{
			return;
		}
		for( int i = 0 ; i < mEntries.length ; i++ )
		{
			SelectItemInfo itemInfo = new SelectItemInfo();
			itemInfo.title = mEntries[i];
			itemInfo.value = mEntryValues[i];
			if( msubEntries != null && msubEntries.length > i )
			{
				itemInfo.summary = msubEntries[i];
			}
			mItemInfoList.add( itemInfo );
		}
	}
	
	public void setItemChecked(
			String value )
	{
		int index = findIndexOfValue( value );
		if( getListView() != null )
		{
			mClickedEntryIndex = index;
			getListView().setItemChecked( index , false );
		}
	}
	
	private int getValueIndex()
	{
		return findIndexOfValue( currentValue );
	}
	
	/**
	 * 通过存储的值找出对应的索引
	 * @param value
	 * @return
	 */
	public int findIndexOfValue(
			String value )
	{
		if( value != null && mItemInfoList != null )
		{
			for( int i = 0 ; i < mItemInfoList.size() ; i++ )
			{
				if( mItemInfoList.get( i ).value.equals( value ) )
				{
					return i;
				}
			}
		}
		return -1;
	}
	
	private class ListSelectAdapter extends ArrayAdapter<SelectItemInfo>
	{
		
		private LayoutInflater mInflater;
		
		private class Holder
		{
			
			TextView title;
			TextView summary;
			CheckBox checkBox;
		}
		
		public ListSelectAdapter(
				Context context ,
				List<SelectItemInfo> objects )
		{
			super( context , 0 , objects );
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( "test" , StringUtils.concat( "count = " , objects.size() ) );
			// TODO Auto-generated constructor stub
			mInflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			mClickedEntryIndex = getValueIndex();
		}
		
		public int getCount()
		{
			// TODO Auto-generated method stub
			return mItemInfoList.size();
		}
		
		@Override
		public SelectItemInfo getItem(
				int position )
		{
			// TODO Auto-generated method stub
			if( position < 0 || position >= mItemInfoList.size() )
			{
				return null;
			}
			return super.getItem( position );
		}
		
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			// TODO Auto-generated method stub
			Holder holder;
			SelectItemInfo itemInfo = getItem( position );
			View view = null;
			if( convertView == null )
			{
				holder = new Holder();
				view = mInflater.inflate( R.layout.launcher_settings_list_select_item , parent , false );
				holder.title = (TextView)view.findViewById( R.id.title );
				holder.summary = (TextView)view.findViewById( R.id.summary );
				holder.checkBox = (CheckBox)view.findViewById( R.id.check_box );
				view.setTag( holder );
			}
			else
			{
				view = convertView;
				holder = (Holder)view.getTag();
			}
			holder.title.setText( itemInfo.title );
			holder.summary.setText( itemInfo.summary );
			if( position == mClickedEntryIndex )
			{
				holder.checkBox.setChecked( true );
			}
			else
			{
				holder.checkBox.setChecked( false );
			}
			return view;
		}
	}
	
	//xiatian start	//拓展BaseListPreferenceFragment基类，去掉onBackPressed方法，添加onBackPressedWithAnim方法和onBackPressedWithOutAnim方法。
	//xiatian del start
	//	@Override
	//	protected boolean onBackPressed()
	//	{
	//		// TODO Auto-generated method stub
	//		FragmentManager fm = getFragmentManager();
	//		FragmentTransaction transaction = fm.beginTransaction();
	//		transaction.setCustomAnimations( 0 , R.anim.fragment_exit );
	//		transaction.hide( this );
	//		transaction.commit();
	//		return true;
	//	}
	//xiatian del end
	//xiatian add start
	@Override
	protected boolean onBackPressedWithAnim()
	{
		//xiatian start	//桌面设置进入和退出二级菜单的动画，适配“从右往左”显示的语言（例如：“阿拉伯”语）:“从右往左”显示时，从左往右进入二级菜单，从右往左退出二级菜单。
		//		exitWithAnim( R.anim.anim_launcher_settings_sub_menu_exit );//xiatian del
		//xiatian add start
		if( isLayoutRtl() )
		{
			exitWithAnim( R.anim.anim_launcher_settings_sub_menu_r2l_exit );
		}
		else
		{
			exitWithAnim( R.anim.anim_launcher_settings_sub_menu_l2r_exit );
		}
		//xiatian add end
		//xiatian end
		return true;
	}
	
	@Override
	protected boolean onBackPressedWithOutAnim()
	{
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.hide( this );
		transaction.commitAllowingStateLoss();//允许Fragment的保存的状态丢失
		return true;
	}
	//xiatian add end
	//xiatian end
	;
}
