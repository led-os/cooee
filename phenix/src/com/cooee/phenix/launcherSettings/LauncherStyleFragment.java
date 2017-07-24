package com.cooee.phenix.launcherSettings;


import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
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
import com.cooee.phenix.util.SystemAction;
import com.cooee.util.DefaultDialog;
import com.kmob.kmobsdk.KmobManager;


public class LauncherStyleFragment extends BaseListPreferenceFragment
{
	
	private static final String TAG = "LauncherStyleFragment";
	private ImageView backButton;
	private TextView title;
	private String mKey;
	private String currentValue;
	private int mClickedEntryIndex;
	private List<SelectItemInfo> mItemInfoList = new ArrayList<SelectItemInfo>();
	private CharSequence[] mEntries;//显示项内容
	private CharSequence[] msubEntries;//显示副项内容
	private CharSequence[] mEntryValues;//显示项存储对应的Value值
	private static final String IS_NEED_TO_RESTART = "mIsNeedToRestart";//xiatian add	//fix bug：解决“在桌面设置中的切换桌面模式界面，点击‘确认切换桌面模式’提示框的确认按钮后，提示框有时没关闭”的问题。【i_0010668】
	
	@Override
	public void onActivityCreated(
			Bundle savedInstanceState )
	{
		// TODO Auto-generated method stub
		super.onActivityCreated( savedInstanceState );
		if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.d( TAG , "[LauncherStyleFragment] onActivityCreated" );
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
					showConfirmDialog( String.valueOf( position ) );
				}
			}
		} );
		setListAdapter( new ListSelectAdapter( getActivity() , mItemInfoList ) );
	}
	
	private void initData()
	{
		mEntries = LauncherDefaultConfig.getStringArray( R.array.launcher_stytle_entries );
		msubEntries = LauncherDefaultConfig.getStringArray( R.array.launcher_stytle_sub_entries );
		mEntryValues = LauncherDefaultConfig.getStringArray( R.array.launcher_stytle_entry_values );
		mKey = LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE_KEY;
		currentValue = String.valueOf( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE );
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
		title.setText( R.string.select_launcher_style );
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
	
	/**
	 * 确认对话框的显示
	 * @param value
	 */
	private void showConfirmDialog(
			final String value )
	{
		//cheyingkun start	//统一phenix桌面弹出提示框的风格。
		//cheyingkun del start
		//		final ConfirmDialog mConfirmDialog = new ConfirmDialog( getActivity() , R.style.base_dialog_style );
		//		mConfirmDialog.setTitle( R.string.confirm_change_launcher_style );
		//		mConfirmDialog.setContentText( R.string.change_style_will_restar );
		//cheyingkun del end
		//cheyingkun add start
		final DefaultDialog mDefaultDialog = new DefaultDialog( getActivity() );
		mDefaultDialog.setTitle( R.string.confirm_change_launcher_style );
		mDefaultDialog.setContentText( R.string.change_style_will_restar );
		mDefaultDialog.setPositiveButtonText( R.string.positive );
		mDefaultDialog.setNegativeButtonText( R.string.negative );
		//cheyingkun add end
		//xiatian add start	//fix bug：解决“在桌面设置中的切换桌面模式界面，点击‘确认切换桌面模式’提示框的确认按钮后，提示框有时没关闭”的问题。【i_0010668】
		OnDismissListener mOnDismissListener = new OnDismissListener() {
			
			@Override
			public void onDismiss(
					DialogInterface dialog )
			{
				if( dialog instanceof DefaultDialog )
				{
					DefaultDialog mConfirmDialog = (DefaultDialog)dialog;
					boolean mIsNeedToRestart = false;
					Bundle mTag = mConfirmDialog.getTag();
					if( mTag != null )
					{
						mIsNeedToRestart = mTag.getBoolean( IS_NEED_TO_RESTART , false );
					}
					if( mIsNeedToRestart )
					{
						mConfirmDialog.setTag( null );
						KmobManager.clearAllDl();//cheyingkun add	//解决“点击文件夹下方推荐应用下载，下载过程中切换模式，此时在点击此应用下载，提示正在下载中。状态栏无下载显示。”的问题。【i_0013284】
						// wanghongjian@2015/04/27 UPD START 当切换桌面模式准备重启的时候，要将状态栏中launcher提示下载的提示给关闭 bug:0011115
						( (NotificationManager)( getActivity().getApplicationContext().getSystemService( Context.NOTIFICATION_SERVICE ) ) ).cancelAll();
						// wanghongjian@2015/04/27 UPD END
						SystemAction.RestartSystem( getActivity() );//zhujieping add,这里启动activity调用启动launcher的方法,启动launcher的方法需和按home键启动的方式一致，如果不启动activity直接调用，重启过程中会出现黑屏状态
					}
				}
			}
		};
		mDefaultDialog.setOnDismissListener( mOnDismissListener );
		//xiatian add end
		//cheyingkun del start
		/*mConfirmDialog.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClickPositive(
					View v )
			{
				//				mConfirmDialog.dismiss();//xiatian del	//fix bug：解决“在桌面设置中的切换桌面模式界面，点击‘确认切换桌面模式’提示框的确认按钮后，提示框有时没关闭”的问题。【i_0010668】
				currentValue = value;
				setItemChecked( value );
				saveValue( mKey , value );
				//cheyingkun start	//解决“重启手机后，切换单双层模式，对话框弹出后点击确定，桌面很久才进行模式切换”的问题。【i_0010381】
				//【问题原因】重启手机后，进行广播的发送和接受会比较慢。以前逻辑是点击确定后发送桌面重启广播，重启开机一段时间内，发送广播到接受广播时间会很久（自测数据大概20秒左右）
				//【解决方案】桌面模式切换不发送广播，直接进行桌面重启。
				//				getActivity().sendBroadcast( new Intent( ThemeReceiver.ACTION_LAUNCHER_RESTART ) );//cheyingkun del
				//cheyingkun add start
				//xiatian start	//fix bug：解决“在桌面设置中的切换桌面模式界面，点击‘确认切换桌面模式’提示框的确认按钮后，提示框有时没关闭”的问题。【i_0010668】
				//				restartLauncher();//xiatian del
				//xiatian add start
				Bundle mTag = new Bundle();
				mTag.putBoolean( IS_NEED_TO_RESTART , true );
				mConfirmDialog.setTag( mTag );
				mConfirmDialog.dismiss();
				//xiatian add end
				//xiatian end
				//cheyingkun add end
				//cheyingkun end
			}
			
			@Override
			public void onClickNegative(
					View v )
			{
				// TODO Auto-generated method stub
			}
		} );
		mConfirmDialog.show();*/
		//cheyingkun del end
		//cheyingkun add start
		mDefaultDialog.setOnClickListener( new DefaultDialog.OnClickListener() {
			
			@Override
			public void onClickPositive(
					View v )
			{
				//				mConfirmDialog.dismiss();//xiatian del	//fix bug：解决“在桌面设置中的切换桌面模式界面，点击‘确认切换桌面模式’提示框的确认按钮后，提示框有时没关闭”的问题。【i_0010668】
				currentValue = value;
				setItemChecked( value );
				saveValue( mKey , value );
				//cheyingkun start	//解决“重启手机后，切换单双层模式，对话框弹出后点击确定，桌面很久才进行模式切换”的问题。【i_0010381】
				//【问题原因】重启手机后，进行广播的发送和接受会比较慢。以前逻辑是点击确定后发送桌面重启广播，重启开机一段时间内，发送广播到接受广播时间会很久（自测数据大概20秒左右）
				//【解决方案】桌面模式切换不发送广播，直接进行桌面重启。
				//				getActivity().sendBroadcast( new Intent( ThemeReceiver.ACTION_LAUNCHER_RESTART ) );//cheyingkun del
				//cheyingkun add start
				//xiatian start	//fix bug：解决“在桌面设置中的切换桌面模式界面，点击‘确认切换桌面模式’提示框的确认按钮后，提示框有时没关闭”的问题。【i_0010668】
				//				restartLauncher();//xiatian del
				//xiatian add start
				Bundle mTag = new Bundle();
				mTag.putBoolean( IS_NEED_TO_RESTART , true );
				mDefaultDialog.setTag( mTag );
				mDefaultDialog.dismiss();
				//xiatian add end
				//xiatian end
				//cheyingkun add end
				//cheyingkun end
			}
			
			@Override
			public void onClickNegative(
					View v )
			{
			}
			
			@Override
			public void onClickExit(
					View v )
			{
			}
		} );
		mDefaultDialog.show();
		//cheyingkun add end
		//cheyingkun end
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
