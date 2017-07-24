package com.cooee.phenix.launcherSettings;


import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


// cheyingkun add whole file //和兴六部图标大小变化需求
public class LauncherAppIconSizeFragment extends BaseListPreferenceFragment
{
	
	private static final String TAG = "LauncherAppSizeFragment";
	private ImageView mBackButton;
	private TextView mTitle;
	private String mKey;
	private String mCurrentValue;
	private int mClickedEntryIndex;
	private List<SelectItemInfo> mItemInfoList = new ArrayList<SelectItemInfo>();
	private CharSequence[] mEntries;//显示项内容
	private CharSequence[] mEntryValues;//显示项存储对应的Value值
	private ListSelectAdapter mListSelectAdapter;
	
	@Override
	public void onActivityCreated(
			Bundle savedInstanceState )
	{
		super.onActivityCreated( savedInstanceState );
		initData();
		findView();
		loadItemInfoList( mItemInfoList );
		initEvent();
		getListView().setDividerHeight( 0 );
		getListView().setChoiceMode( ListView.CHOICE_MODE_SINGLE );
		mListSelectAdapter = new ListSelectAdapter( getActivity() , mItemInfoList );
		setListAdapter( mListSelectAdapter );
	}
	
	private void initData()
	{
		mEntries = LauncherDefaultConfig.getStringArray( R.array.launcher_app_icon_size );
		mEntryValues = LauncherDefaultConfig.getStringArray( R.array.launcher_app_icon_size_entry_values );
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getActivity() );
		Resources res = getActivity().getResources();
		mKey = LauncherDefaultConfig.getString( R.string.setting_key_app_icon_size );
		mCurrentValue = prefs.getString( mKey , "0" );
	}
	
	private void findView()
	{
		mBackButton = (ImageView)getView().findViewById( R.id.list_fragment_back );
		//xiatian add start	//桌面设置进入二级菜单的图片和退出二级菜单的图片，适配“从右往左”显示的语言（例如：“阿拉伯”语）:“从右往左”显示时，进入二级菜单的图片为左箭头，退出二级菜单的图片为右箭头。
		if( isLayoutRtl() )
		{
			mBackButton.setImageDrawable( getResources().getDrawable( R.drawable.launcher_settings_r2l_back ) );
		}
		//xiatian add end
		mTitle = (TextView)getView().findViewById( R.id.list_fragment_title );
		mTitle.setText( R.string.app_icon_size );
	}
	
	public void loadItemInfoList(
			List<SelectItemInfo> itemInfoList )
	{
		if( mEntries == null || mEntries.length == 0 )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "mEntries is null or mEntries's length is 0" );
			return;
		}
		for( int i = 0 ; i < mEntries.length ; i++ )
		{
			SelectItemInfo itemInfo = new SelectItemInfo();
			itemInfo.title = mEntries[i];
			itemInfo.value = mEntryValues[i];
			itemInfoList.add( itemInfo );
		}
	}
	
	private void initEvent()
	{
		mBackButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				if( isFinishActivityOnBackPressed() )
				{
					getActivity().finish();
					return;
				}
				// TODO Auto-generated method stub
				onBackPressedWithAnim();
			}
		} );
		getListView().setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				// TODO Auto-generated method stub
				mCurrentValue = String.valueOf( position );
				mClickedEntryIndex = position;
				getListView().setItemChecked( mClickedEntryIndex , false );
				saveValue( mKey , mCurrentValue );
				restartLauncher();
			}
		} );
	}
	
	private int getValueIndex()
	{
		return findIndexOfValue( mCurrentValue );
	}
	
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
				view = mInflater.inflate( R.layout.launcher_settings_no_summary_list_select_item , parent , false );
				holder.title = (TextView)view.findViewById( R.id.title );
				holder.checkBox = (CheckBox)view.findViewById( R.id.check_box );
				view.setTag( holder );
			}
			else
			{
				view = convertView;
				holder = (Holder)view.getTag();
			}
			holder.title.setText( itemInfo.title );
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
		// TODO Auto-generated method stub
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.hide( this );
		transaction.commitAllowingStateLoss();
		return true;
	}
	
	private void restartLauncher()
	{
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "Launcher" , "exit" );
		Context mContext = getActivity().getApplicationContext();
		// wanghongjian@2015/04/27 UPD START 当切换桌面模式准备重启的时候，要将状态栏中launcher提示下载的提示给关闭 bug:0011115
		( (NotificationManager)( mContext.getSystemService( Context.NOTIFICATION_SERVICE ) ) ).cancelAll();
		// wanghongjian@2015/04/27 UPD END
		final Intent intent = new Intent();
		intent.setClass( mContext , Launcher.class );
		intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
		mContext.startActivity( intent );
		//cheyingkun del start	//解决“切换单双层反应过慢”的问题。【i_0012595】
		//		//cheyingkun add start	//添加友盟统计自定义事件(程序结束前保存友盟统计数据)
		//		if( LauncherDefaultConfig.SWITCH_ENABLE_UMENG )
		//		{
		//			MobclickAgent.onKillProcess( mContext );
		//		}
		//		//cheyingkun add end
		//cheyingkun del end
		System.exit( 0 );
	}
}
