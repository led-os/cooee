package com.search.kuso;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooee.pinyin4j.util.PinYinUtils;
import com.cooee.search.NativeData;
import com.cooee.search.R;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;
import com.kmob.kmobsdk.NativeAdData;
import com.search.kuso.Contacts.SearchByType;
import com.search.kuso.ContactsHelper.OnContactsLoad;
import com.search.kuso.MusicLoader.MusicInfo;
import com.search.kuso.dao.HistoryRecordDao;
import com.search.kuso.data.ConfigData;
import com.t9search.model.PinyinUnit;
import com.t9search.model.T9PinyinUnit;
import com.t9search.util.PinyinUtil;

import cool.sdk.common.CoolMethod;
import cool.sdk.download.CoolDLMgr;
import cool.sdk.download.manager.dl_info;
import cool.sdk.kuso.KuSoHelper;
import cool.sdk.kuso.KusoData;
import cool.sdk.kuso.KusoEngineInfo;


public class SearchT9Main extends Activity
{
	
	private static final String SWITCH_C3 = "KUSO_SWITCH_C3";
	private static final String SWITCH_C4 = "KUSO_SWITCH_C4";
	private static final String KEY_ENABLE_HAOSOU = "enable_haosou";
	private static final String KEY_EXPLORER_PACKAGE_NAME = "KUSO_EXPLORER_PACKAGE_NAME";
	private static final String KEY_EXPLORER_CLASS_NAME = "KUSO_EXPLORER_CLASS_NAME";
	public static String APPId = "";
	private EditText kuso_search_edit;
	private LinearLayout kuso_iconfont_sousuo_button;
	private LinearLayout history_record_clear;
	private ListView lv_history_record;
	private HistoryRecordDao dao;
	private List<String> searchResult;
	private ArrayList<String> viewResult = new ArrayList<String>();// 保存最近搜索的十条历史记录
	private ArrayAdapter<String> adapter; // 历史记录的适配器
	private ImageView current_logo;
	private String engine_head;// 引擎的url
	private String result;
	private MyListAdapter myListAdapter;//本地搜索结果的adapter
	private ListView listView1; // 本地搜索结果的listview
	private KusoData kusoData;
	private String uriString; // 图片的url
	private RelativeLayout bottom;//历史记录界面
	private ArrayList<KusoEngineInfo> list;
	private SharedPreferences sp;
	private int k;//保存在sp中的标志当前引擎的位置
	private ImageView kuso_engine_triangle;
	private ImageView kuso_local_serach_current_logo;//当没有获取到服务器数据时主页面的logo显示
	private WebView webView;
	private int r;
	private String path;
	private CoolDLMgr dlMgr;
	private ContactsHelper contactsHelper;
	private InputMethodManager imm;
	private boolean isFirstIn = true;
	private String adUrl = "";
	private AdBaseView mNativeView;
	private boolean mWebLoadFinish = false;
	//cheyingkun add start	//搜索界面背景模糊
	private static Bitmap mBlurBG;
	private static View searchT9MainBlurView;
	
	//cheyingkun add end
	@Override
	protected void onCreate(
			Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		this.overridePendingTransition( R.anim.kuso_fade_in_fast , R.anim.kuso_fade_out_fast );
		long startTime = System.currentTimeMillis();
		PinYinUtils.setAssetManager( getAssets() );//cheyingkun add	//pinyin4j相关代码整理到uibase.jar中
		new ConfigData( this ); //解析
		Log.v( "ZZY" , "createTime11=" + ( System.currentTimeMillis() - startTime ) );
		//view
		//cheyingkun start	//搜索界面背景模糊
		//		setContentView( R.layout.kuso_search_main_layout );//cheyingkun del
		//cheyingkun add start
		if( searchT9MainBlurView == null )
		{
			searchT9MainBlurView = View.inflate( getApplication() , R.layout.kuso_search_main_layout , null );
		}
		ViewParent mViewParent = searchT9MainBlurView.getParent();
		if( mViewParent != null )
		{
			Log.v( "SearchT9Main" , "onCreate - [searchT9MainBlurView.getParent() != null] - finish and return" );
			if( mViewParent instanceof ViewGroup )
			{
				ViewGroup mViewGroup = (ViewGroup)mViewParent;
				mViewGroup.removeView( searchT9MainBlurView );
			}
			finish();
			return;
		}
		setContentView( searchT9MainBlurView );
		if( mBlurBG != null )
		{
			searchT9MainBlurView.setBackgroundDrawable( new BitmapDrawable( mBlurBG ) );
		}
		//cheyingkun add end
		//cheyingkun end
		kuso_search_edit = (EditText)findViewById( R.id.kuso_search_edit );
		kuso_iconfont_sousuo_button = (LinearLayout)findViewById( R.id.kuso_iconfont_sousuo_button );
		current_logo = (ImageView)findViewById( R.id.kuso_current_logo );
		dao = new HistoryRecordDao( SearchT9Main.this );
		lv_history_record = (ListView)findViewById( R.id.kuso_lv_history_record );
		bottom = (RelativeLayout)findViewById( R.id.kuso_bottom );
		webView = (WebView)findViewById( R.id.kuso_webview );
		kuso_local_serach_current_logo = (ImageView)findViewById( R.id.kuso_local_serach_current_logo );
		getData();
		adapter = new ArrayAdapter<String>( this , R.layout.kuso_history_item , R.id.kuso_tv_history_item , viewResult );
		lv_history_record.setAdapter( adapter );
		kusoData = KuSoHelper.getInstance( this ).getKusoData();
		imm = (InputMethodManager)getSystemService( INPUT_METHOD_SERVICE );
		if( this.getIntent().getBooleanExtra( SWITCH_C4 , KusoData.C4_DEFAULT ) || kusoData.isC0() )//是否显示
		{
			/*
			 * 如果是第一次进入应用，显示默认引擎，否则显示sp中保存的引擎
			 */
			dlMgr = KuSoHelper.getInstance( this.getApplicationContext() ).getCoolDLMgrIcon();
			sp = getSharedPreferences( "sp_setting" , 0 );
			list = new ArrayList<KusoEngineInfo>();
			list.addAll( kusoData.getEngines() );
			k = sp.getInt( "k" , -1 );
			if( k < 0 )
			{
				Log.i( "firstIn" , "第一次运行" + k );
				if( kusoData.getEngines().size() > 0 )
				{
					for( int i = 0 ; i < list.size() ; i++ )
					{
						if( list.get( i ).isR6() )//是否默认引擎
						{
							engine_head = list.get( i ).getR4() + list.get( i ).getR5();
							uriString = list.get( i ).getR3();
							dl_info dl = dlMgr.UrlGetInfo( uriString );
							if( dl != null && dl.IsDownloadSuccess() )
							{
								path = dl.getFilePath();
								Log.i( "path" , "path--" + path );
								Bitmap bit = BitmapFactory.decodeFile( path );
								current_logo.setImageBitmap( bit );
								System.out.println( bit );
							}
							else
							{
								setLogo(); //第一次没有更新到服务器数据时显示本地配置的logo
							}
							Editor editor = sp.edit();
							editor.putInt( "k" , i );
							editor.putString( "head" , uriString );
							editor.putString( "engine_url" , engine_head );
							editor.commit();
						}
					}
				}
			}
			else
			{
				uriString = sp.getString( "head" , "" );
				engine_head = sp.getString( "engine_url" , "" );
				if( null != uriString || !"".equals( uriString ) )
				{
					ArrayList<String> uriList = new ArrayList<String>();
					for( int i = 0 ; i < list.size() ; i++ )
					{
						String uriItem = list.get( i ).getR3();
						uriList.add( uriItem );
					}
					if( uriList.contains( uriString ) )
					{
						dl_info dl = dlMgr.UrlGetInfo( uriString );
						if( dl != null && dl.IsDownloadSuccess() )
						{
							path = dl.getFilePath();
							Log.i( "path" , path );
							Bitmap bit = BitmapFactory.decodeFile( path );
							current_logo.setImageBitmap( bit );
						}
					}
					else
					//之前选择的引擎已经不存在后台配置中了
					{
						for( int i = 0 ; i < list.size() ; i++ )
						{
							if( list.get( i ).isR6() )
							{
								engine_head = list.get( i ).getR4() + list.get( i ).getR5();
								uriString = list.get( i ).getR3();
								dl_info dl = dlMgr.UrlGetInfo( uriString );
								if( dl != null && dl.IsDownloadSuccess() )
								{
									path = dl.getFilePath();
									Log.i( "path" , path );
									Bitmap bit = BitmapFactory.decodeFile( path );
									current_logo.setImageBitmap( bit );
									k = i;
								}
								Editor editor = sp.edit();
								editor.putInt( "k" , i );
								editor.putString( "head" , uriString );
								editor.putString( "engine_url" , engine_head );
								editor.commit();
							}
						}
					}
				}
				else
				{
					setLogo();
				}
			}
			kuso_search_edit.clearFocus();
			imm.hideSoftInputFromWindow( kuso_search_edit.getWindowToken() , 0 );
			if( null == path || ( "" ).equals( path ) )
			{
				boolean switch_c4 = this.getIntent().getBooleanExtra( SWITCH_C4 , KusoData.C4_DEFAULT );//桌面传来的开关，true：未请求到服务器数据时，默认打开运营页
				showOperatePage( switch_c4 );
			}
			else
			{
				showOperatePage( kusoData.isC4() );
			}
			//随机展示热词中的一个
			if( kusoData.getC6().size() > 0 )
			{
				Random rd = new Random();
				r = rd.nextInt( kusoData.getC6().size() );
				Log.i( "COOL" , "热词长度：" + kusoData.getC6().size() );
				result = kusoData.getC6().get( r ).trim();
				if( path != null || !"".equals( path ) )
				{
					kuso_search_edit.setText( SearchT9Main.this.getResources().getString( R.string.kuso_hot ) + result );
					kuso_search_edit.setTextColor( SearchT9Main.this.getResources().getColor( R.color.kuso_choose_search_engine_text_color ) );
				}
			}
			else
			{
				String hot = getIntent().getStringExtra( SearchManager.QUERY );
				if( hot != null )
				{
					result = hot;
					kuso_search_edit.setText( hot );
					kuso_search_edit.setTextColor( SearchT9Main.this.getResources().getColor( R.color.kuso_choose_search_engine_text_color ) );
				}
				else
				{
					kuso_search_edit.setText( "" );
				}
			}
			kuso_search_edit.setLongClickable( false );
			kuso_search_edit.setOnFocusChangeListener( new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(
						View v ,
						boolean hasFocus )
				{
					if( hasFocus && isFirstIn )
					{//无--有
						if( kusoData.getC6().size() > 0 )
						{
							kuso_search_edit.setText( "" );
						}
						kuso_search_edit.setTextColor( SearchT9Main.this.getResources().getColor( R.color.kuso_engine_name_text ) );
						if( myListAdapter.isEmpty() )
						{
							listView1.setVisibility( View.GONE );
							webView.setVisibility( View.GONE );
							bottom.setVisibility( View.VISIBLE );
						}
						else
						{
							listView1.setVisibility( View.VISIBLE );
							webView.setVisibility( View.GONE );
							bottom.setVisibility( View.GONE );
						}
						if( TextUtils.isEmpty( kuso_search_edit.getText() ) )
						{
							kuso_iconfont_sousuo_button.setVisibility( View.GONE );
						}
						else
						{
							kuso_iconfont_sousuo_button.setVisibility( View.VISIBLE );
						}
						isFirstIn = false;
					}
					else
					{//--有--无
						kuso_search_edit.setText( result );
					}
				}
			} );
			/*
			 * 点击当前logo，跳到搜索引擎的设置界面
			 */
			current_logo.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(
						View v )
				{
					Intent intent = new Intent( SearchT9Main.this , SearchSettingActivity.class );
					try
					{
						startActivityForResult( intent , 1 );
					}
					catch( ActivityNotFoundException e )
					{
						// TODO: handle exception
						Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_activity_not_found ) , Toast.LENGTH_SHORT ).show();
						Log.e( "SearchT9Main" , "Activity not found! intent:" + intent , e );
					}
				}
			} );
		}
		else
		{//隐藏---
			//打开键盘
			kuso_search_edit.setFocusable( true );
			kuso_search_edit.setFocusableInTouchMode( true );
			kuso_search_edit.requestFocus();
			InputMethodManager imm = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE );
			imm.toggleSoftInput( 0 , InputMethodManager.HIDE_NOT_ALWAYS );
			imm.showSoftInput( this.getCurrentFocus() , 0 );
		}
		/*
		 * 输入框
		 */
		kuso_search_edit.addTextChangedListener( new TextWatcher() {
			
			@Override
			public void onTextChanged(
					CharSequence s ,
					int start ,
					int before ,
					int count )
			{
				result = s.toString().trim();
				myListAdapter.searchContentNoWeb( result );
				myListAdapter.notifyDataSetInvalidated();
				if( myListAdapter.isEmpty() )
				{
					listView1.setVisibility( View.GONE );
					webView.setVisibility( View.GONE );
					bottom.setVisibility( View.VISIBLE );
					//未输入右边搜索logo不显示
					kuso_iconfont_sousuo_button.setVisibility( View.GONE );//cheyingkun add	//需求:仿谷歌搜索
				}
				else
				{
					listView1.setVisibility( View.VISIBLE );
					webView.setVisibility( View.GONE );
					bottom.setVisibility( View.GONE );
					//输入后,显示右边搜索logo
					kuso_iconfont_sousuo_button.setVisibility( View.VISIBLE );//cheyingkun add	//需求:仿谷歌搜索
				}
			}
			
			@Override
			public void beforeTextChanged(
					CharSequence s ,
					int start ,
					int count ,
					int after )
			{
			}
			
			@Override
			public void afterTextChanged(
					Editable s )
			{
			}
		} );
		Log.v( "ZZY" , "createTime=" + ( System.currentTimeMillis() - startTime ) );
		listView1 = (ListView)findViewById( R.id.kuso_listview1 );//本地搜索结果
		if( myListAdapter == null )
		{
			myListAdapter = new MyListAdapter();
			myListAdapter.loadContent();
		}
		myListAdapter.initContent();
		listView1.setAdapter( myListAdapter );
		listView1.setOnItemClickListener( new OnItemClickListener() {
			
			@Override
			public void onItemClick(
					AdapterView<?> arg0 ,
					View arg1 ,
					int arg2 ,
					long arg3 )
			{
				myListAdapter.onClick( arg2 );
			}
		} );
		/*
		 * 搜索icon
		 */
		kuso_iconfont_sousuo_button.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				gotoUrl( result );
			}
		} );
		/*
		 *输入框
		 */
		kuso_search_edit.setOnKeyListener( new View.OnKeyListener() {
			
			@Override
			public boolean onKey(
					View v ,
					int keyCode ,
					KeyEvent event )
			{
				if( keyCode == KeyEvent.KEYCODE_ENTER )
				{
					kuso_search_edit.setFocusable( true );
					kuso_search_edit.requestFocus();
					kuso_search_edit.setText( kuso_search_edit.getText().toString().trim() );
					kuso_search_edit.setSelection( kuso_search_edit.getText().toString().trim().length() );
					gotoUrl( kuso_search_edit.getText().toString().trim() );
				}
				return false;
			}
		} );
		/*
		 * 当没有拿到服务器数据，不能跳到设置界面，用默认引擎，灭掉下三角图标
		 */
		kuso_engine_triangle = (ImageView)findViewById( R.id.kuso_engine_triangle );
		if( null == path || ( "" ).equals( path ) ) //---没有拿到时
		{
			kuso_engine_triangle.setVisibility( View.GONE );
			current_logo.setClickable( false );
			kuso_local_serach_current_logo.setVisibility( View.VISIBLE );
			current_logo.setVisibility( View.GONE );
			//			bottom.setVisibility( View.VISIBLE );//test
			// jubingcheng@2016/06/15 UPD START 搜索结果打开方式可配
			//kusoData.setC3( true );
			kusoData.setC3( this.getIntent().getBooleanExtra( SWITCH_C3 , KusoData.C3_DEFAULT ) );
			// jubingcheng@2016/06/15 UPD END
			//根据语言设置logo
			setLogo();
		}
		else
		{ //----有显示
			current_logo.setVisibility( View.VISIBLE );
			kuso_local_serach_current_logo.setVisibility( View.GONE );
			kuso_engine_triangle.setVisibility( View.VISIBLE );
			current_logo.setClickable( true );
			kuso_iconfont_sousuo_button.setClickable( true );
		}
		/*
		 * 点击拿到历史记录条目--先显示本地结果，本地为空则跳转搜索
		 */
		lv_history_record.setOnItemClickListener( new OnItemClickListener() {
			
			private String history_item;
			
			@Override
			public void onItemClick(
					AdapterView<?> parent ,
					View view ,
					int position ,
					long id )
			{
				history_item = adapter.getItem( position );
				kuso_search_edit.setText( history_item );
				kuso_search_edit.setSelection( history_item.length() );//设置光标位置跟随
				viewResult.remove( history_item );
				viewResult.add( 0 , history_item );
				dao.delete( history_item );
				dao.insert( history_item );
				if( myListAdapter.isEmpty() || myListAdapter.itemList.size() == 1 )
				{
					bottom.setVisibility( View.VISIBLE );
					listView1.setVisibility( View.GONE );
					//					webView.setVisibility( View.VISIBLE );
					gotoUrl( history_item );
				}
				else
				{
					listView1.setVisibility( View.VISIBLE );
				}
				adapter.notifyDataSetChanged();
			}
		} );
		history_record_clear = (LinearLayout)findViewById( R.id.kuso_history_record_clear );
		history_record_clear.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(
					View v )
			{
				dao.clearAllRecord();
				getData();
				adapter.notifyDataSetChanged();
			}
		} );
	}
	
	/**
	 * 开关（服务器配置的c4参数，或者是桌面传来的开关）控制是否展示运营页：
	 * 展示运营页时，设置运营页webview相应属性，并加载webview的url--服务器配置了c5时，url同步服务器，否则为默认url
	 * @param 
	 */
	private void showOperatePage(
			boolean isC4 )
	{
		if( isC4 )
		{
			kuso_search_edit.clearFocus();//输入法的弹起与隐藏 同步每一个服务器配置项对应的情况
			bottom.setVisibility( View.GONE );
			webView.setVisibility( View.VISIBLE );
			bindJsClass( webView );
			setClient();
			getAdInfo();
			webView.loadUrl( kusoData.getC5() ); //默认第一次进入主页面显示的webview
		}
		else
		{
			bottom.setVisibility( View.VISIBLE );
			if( kusoData.getC6().size() > 0 )
			{
				kuso_search_edit.clearFocus();
				imm.hideSoftInputFromWindow( kuso_search_edit.getWindowToken() , 0 );
			}
			else
			{
				kuso_search_edit.requestFocus();
				imm.toggleSoftInput( 0 , InputMethodManager.HIDE_NOT_ALWAYS );
			}
		}
	}
	
	private String adplaceid;
	private String adid;
	private String clickurl;
	private String interactiontype;
	private String open_type;
	private String package_name;
	private String click_record_url;
	private String headline;
	private String download;
	private String summary;
	private String adlogo;
	private String adsid = "";
	
	private void getAdInfo()
	{
		KmobManager.setContext( this.getApplicationContext() ); //gaominghui add kmob初始化context
		// zhangjin@2016/03/29 ADD START
		KmobManager.setChannel( getSnFromConfig( this ) );
		// zhangjin@2016/03/29 ADD END
		mNativeView = KmobManager.createNative( getAdsid() , this , 1 );
		Log.i( KmobManager.LOGTAG , "getAdsid  Adsid = " + getAdsid() );
		mNativeView.addAdViewListener( new AdViewListener() {
			
			@Override
			public void onAdShow(
					String info )
			{
			}
			
			@Override
			public void onAdReady(
					String info )
			{
				Log.w( KmobManager.LOGTAG , "NativeAdActivity onAdReady info " + info );
				addAdView( info );
			}
			
			@Override
			public void onAdFailed(
					String reason )
			{
				Log.w( KmobManager.LOGTAG , "NativeAdActivity onAdFailed info " + reason );
			}
			
			@Override
			public void onAdClose(
					String info )
			{
				Log.w( KmobManager.LOGTAG , "NativeAdActivity onAdClose info " + info );
			}
			
			@Override
			public void onAdClick(
					String info )
			{
				Log.w( KmobManager.LOGTAG , "NativeAdActivity onAdClick info " + info );
			}
			
			@Override
			public void onAdCancel(
					String info )
			{
				Log.w( KmobManager.LOGTAG , "NativeAdActivity onAdCancel info " + info );
			}
		} );
	}
	
	/**
	 * 根据应用不同的appId，改变广告位id
	 * @return
	 */
	private String getAdsid()
	{
		String appId = getAppId( this );
		if( appId.equals( "441" ) )
		{
			adsid = "20160129040118441"; //phenix桌面
		}
		else if( appId.equals( "461" ) )
		{
			adsid = "20160129040122461";//UNI4桌面
		}
		else if( appId.equals( "281" ) )
		{
			adsid = "20160129040124281";//UNI3桌面
		}
		else if( appId.equals( "481" ) )
		{
			adsid = "20160129050132481";//新闻悬浮窗
		}
		else if( appId.equals( "503" ) )
		{
			adsid = "20160301090324503";//搜索插件
		}
		return adsid;
	}
	
	/**
	 * 获得app Id
	 * @return
	 */
	private String getAppId(
			Context context )
	{
		try
		{
			String key = "KMobAd_APP_ID";
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo( context.getPackageName() , PackageManager.GET_META_DATA );
			if( appInfo.metaData.containsKey( key ) )
			{
				Object msgKey = appInfo.metaData.get( key );
				String msg = "";
				if( msgKey instanceof Integer )
				{
					msg = appInfo.metaData.getInt( key ) + "";
				}
				else if( msgKey instanceof String )
				{
					msg = appInfo.metaData.getString( key );
				}
				return msg;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		Log.i( KmobManager.LOGTAG , "APPId getAdAppId " + APPId );
		return APPId;
	}
	
	/**
	* 通过传入的info解析数据，来展示合适的广告
	*/
	private void addAdView(
			String info )
	{
		ArrayList<NativeData> nativeAdDatas = createNativeDataByInfo( info );
		for( int i = 0 ; i < nativeAdDatas.size() ; i++ )
		{
			NativeData nativeData = nativeAdDatas.get( i );
			adplaceid = nativeData.getAdplaceid();
			adid = nativeData.getAdid();
			clickurl = nativeData.getClickurl();
			interactiontype = nativeData.getInteractiontype();
			open_type = nativeData.getOpen_type();
			package_name = nativeData.getPkgname();
			click_record_url = nativeData.getClick_record_url();
			headline = nativeData.getHeadline();
			download = nativeData.getDownload();
			summary = nativeData.getSummary();
			adlogo = nativeData.getAdlogo();
			String cimg = nativeData.getCtimg();
			Log.v( KmobManager.LOGTAG , "cimg = " + cimg );
			try
			{
				JSONArray ctimgArray = new JSONArray( cimg );
				if( ctimgArray != null && ctimgArray.length() > 0 )
				{
					JSONObject object = ctimgArray.getJSONObject( 0 );
					String url = object.getString( "url" );
					String imgwidth = object.getString( "width" );
					String imgHeight = object.getString( "height" );
					adUrl = url;
					Log.v( KmobManager.LOGTAG , "imgurl " + url + " imgwidth " + imgwidth + " imgHeight " + imgHeight );
				}
			}
			catch( Exception e )
			{
			}
			if( mWebLoadFinish && adUrl != null && !adUrl.equals( "" ) )
			{
				webView.loadUrl( "javascript:addImg('" + adUrl + "');" );
			}
		}
	}
	
	/**
	 * 通过传入的ifo生成很多个nativeAdData，如果是一个广告，则info类型为JsonObject，多个广告类型，则为JsonArray，建议解析的时候先进行检测
	 */
	private ArrayList<NativeData> createNativeDataByInfo(
			String info )
	{
		ArrayList<NativeData> allData = new ArrayList<NativeData>();
		if( info != null )
		{
			try
			{
				JSONObject object = new JSONObject( info );//此时若不是jsonObject，则会抛出异常
				NativeData adData = createNativeData( object );
				allData.add( adData );
			}
			catch( Exception e )
			{
				try
				{
					JSONArray array = new JSONArray( info );
					for( int i = 0 ; i < array.length() ; i++ )
					{
						JSONObject object = array.getJSONObject( i );
						NativeData adData = createNativeData( object );
						allData.add( adData );
					}
				}
				catch( Exception e2 )
				{
				}
			}
		}
		return allData;
	}
	
	private void setLogo()
	{
		String curLan = Locale.getDefault().toString();
		Context context = getApplicationContext();
		String imsi = CoolMethod.getImsi( context );
		kuso_local_serach_current_logo.setVisibility( View.VISIBLE );
		String[] engine = ConfigData.engine;
		if( getIntent().getBooleanExtra( KEY_ENABLE_HAOSOU , false ) )
		{
			engine = ConfigData.engine_haosou;
		}
		if( curLan.equals( "zh_CN" ) || curLan.equals( "zh_TW" ) || curLan.equals( "zh_HK" ) || imsi.startsWith( "460" ) )
		{
			engine_head = engine[0];
			kuso_local_serach_current_logo.setImageResource( R.drawable.kuso_shenma_icon_unpressed );
		}
		else
		{
			engine_head = engine[1];
			kuso_local_serach_current_logo.setImageResource( R.drawable.kuso_google_logo_unpressed );
		}
	}
	
	public interface IKuSoIsGoogleCallbacks
	{
		
		public void notifyIsGoogle(
				boolean isGoogle );
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		getData();
		adapter.notifyDataSetChanged();
		if( mNativeView != null )
		{
			mNativeView.onResume();
		}
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		if( webView != null )
		{
			webView.stopLoading();
			canelDialog();
		}
		if( mNativeView != null )
		{
			mNativeView.onStop();
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if( contactsHelper != null )
		{
			contactsHelper.setContactsChanged( true );
		}
		//cheyingkun add start	//搜索界面背景模糊
		if( searchT9MainBlurView != null )
		{
			searchT9MainBlurView.setBackgroundDrawable( null );
			searchT9MainBlurView = null;
		}
		if( SearchT9Main.mBlurBG != null && !SearchT9Main.mBlurBG.isRecycled() )
		{
			SearchT9Main.mBlurBG.recycle();
			SearchT9Main.mBlurBG = null;
		}
		//cheyingkun add end
		if( mNativeView != null )
		{
			mNativeView.onDestroy();
		}
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		if( mNativeView != null )
		{
			mNativeView.onPause();
		}
	}
	
	private void bindJsClass(
			WebView webView )
	{
		webView.setScrollBarStyle( View.SCROLLBARS_INSIDE_OVERLAY );
		webView.setBackgroundColor( Color.parseColor( "#e0e0df" ) );
		WebSettings webSettings = webView.getSettings();
		// webSettings.setLightTouchEnabled( true );
		webSettings.setLoadWithOverviewMode( true );
		webSettings.setAllowFileAccess( true );
		webSettings.setDomStorageEnabled( true );
		webSettings.setAppCacheEnabled( true );
		webSettings.setUseWideViewPort( true );
		webSettings.setLoadWithOverviewMode( true );
		webSettings.setUseWideViewPort( true );
		webSettings.setLoadWithOverviewMode( true );
		webSettings.setJavaScriptEnabled( true );
		webView.addJavascriptInterface( new JavaScriptObject() , "nano" );
	}
	
	@Override
	protected void onActivityResult(
			int requestCode ,
			int resultCode ,
			Intent data )
	{
		if( resultCode == 200 )
		{
			uriString = sp.getString( "head" , "" );
			engine_head = sp.getString( "engine_url" , "" );
			if( uriString != null && !uriString.equals( "" ) )
			{
				//				CoolDLMgr dlMgr = KuSoHelper.getInstance( this.getApplicationContext() ).getCoolDLMgrIcon();
				dl_info dl = dlMgr.UrlGetInfo( uriString );
				if( dl != null && dl.IsDownloadSuccess() )
				{
					path = dl.getFilePath();
					Bitmap bit = BitmapFactory.decodeFile( path );
					current_logo.setImageBitmap( bit );
				}
			}
		}
	}
	
	public void getData()
	{
		viewResult.clear();
		searchResult = dao.queryAllSearch();
		// viewResult = new String[searchResult.size()];
		int j = 0;
		for( int i = searchResult.size() - 1 ; i >= 0 ; i-- )
		{
			// viewResult[i] = searchResult.get( j );
			viewResult.add( searchResult.get( j ) );
			j++;
		}
	}
	
	private void gotoUrl(
			final String result )
	{
		imm.hideSoftInputFromWindow( kuso_search_edit.getWindowToken() , 0 );
		if( result != null && !"".equals( result ) )
		{
			// +++++++++++++++++++++++判断是用webview打开还是浏览器+++++++++++++++++++++++++++++++++++++++
			if( kusoData.isC3() )
			{// 浏览器打开
				// jubingcheng@2016/06/15 UPD START 使用浏览器打开搜索结果时可指定使用哪个浏览器
				//Intent intent = new Intent( Intent.ACTION_VIEW );
				Intent intent = new Intent();
				String explorerPkgName = this.getIntent().getStringExtra( KEY_EXPLORER_PACKAGE_NAME );
				String explorerClsName = this.getIntent().getStringExtra( KEY_EXPLORER_CLASS_NAME );
				if( explorerPkgName != null && explorerClsName != null )
				{
					ComponentName explorerName = new ComponentName( explorerPkgName , explorerClsName );
					intent.setComponent( explorerName );
				}
				else if( explorerPkgName != null && explorerClsName == null )
				{
					intent.setPackage( explorerPkgName );
					intent.setAction( Intent.ACTION_VIEW );
				}
				else
				{
					intent.setAction( Intent.ACTION_VIEW );
				}
				// jubingcheng@2016/06/15 UPD END				
				Uri content_uri = Uri.parse( engine_head + result );
				intent.setData( content_uri );
				intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				try
				{
					startActivity( intent );
				}
				catch( ActivityNotFoundException e )
				{
					Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_activity_not_found ) , Toast.LENGTH_SHORT ).show();
					Log.e( "SearchT9Main" , "Activity not found! intent:" + intent , e );
				}
				catch( SecurityException e )
				{
					Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_not_allowed_to_start_activity ) , Toast.LENGTH_SHORT ).show();
					Log.e( "SearchT9Main" , "Not allowed to start activity! intent:" + intent , e );
				}
			}
			else
			{// webview打开
				kuso_search_edit.clearFocus();//避免点击热词后即搜索框onfocus的时候冲突
				listView1.setVisibility( View.GONE );
				bottom.setVisibility( View.GONE );
				webView.setVisibility( View.VISIBLE );
				bindJsClass( webView );
				webView.loadUrl( engine_head + result );
				setClient();
				// 覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
			}
			if( result != null && !"".equals( result ) && !viewResult.contains( result ) )
			{
				//拿到搜索内容插入到历史记录中
				dao.insert( result );
				viewResult.add( 0 , result );
			}
			else if( viewResult.contains( result ) )
			{
				viewResult.remove( result );
				viewResult.add( 0 , result );
				dao.delete( result );
				dao.insert( result );
			}
			adapter.notifyDataSetChanged();
		}
	}
	
	private void setClient()
	{
		webView.setWebViewClient( new WebViewClient() {
			
			//重写shouldOverrideUrlLoading方法，使点击链接后不使用其他的浏览器打开。
			public boolean shouldOverrideUrlLoading(
					WebView view ,
					String url )
			{
				//如果不需要其他对点击链接事件的处理返回true，否则返回false 
				// 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
				if( kusoData.isC3() )
				{
					final Intent intent = new Intent( Intent.ACTION_VIEW , Uri.parse( url ) );
					try
					{
						startActivity( intent );
					}
					catch( ActivityNotFoundException e )
					{
						Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_activity_not_found ) , Toast.LENGTH_SHORT ).show();
						Log.e( "SearchT9Main" , "Activity not found! intent:" + intent , e );
					}
					catch( SecurityException e )
					{
						Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_not_allowed_to_start_activity ) , Toast.LENGTH_SHORT ).show();
						Log.e( "SearchT9Main" , "Not allowed to start activity! intent:" + intent , e );
					}
					return false;
				}
				else
				{
					view.loadUrl( url );
					webView.setVisibility( View.VISIBLE );
					listView1.setVisibility( View.GONE );
					bottom.setVisibility( View.GONE );
					return true;
				}
			}
			
			public void onReceivedError(
					WebView view ,
					int errorCode ,
					String description ,
					String failingUrl )
			{
				super.onReceivedError( view , errorCode , description , failingUrl );
				Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_sync_failure ) , Toast.LENGTH_SHORT ).show();
			};
			
			public void onLoadResource(
					WebView view ,
					String url )
			{
				super.onLoadResource( view , url );
				Log.v( "ZZY" , "url=" + url );
			};
			
			public void onPageStarted(
					WebView view ,
					String url ,
					android.graphics.Bitmap favicon )
			{
				super.onPageStarted( view , url , favicon );
				setDialog();
			};
			
			public void onPageFinished(
					WebView view ,
					String url )
			{
				canelDialog();
			};
		} );
		/**
		 * zenLauncher Start
		 */
		WebChromeClient webChromeClient = new WebChromeClient() {
			
			@Override
			public void onProgressChanged(
					WebView view ,
					int newProgress )
			{
				if( newProgress >= 90 )
				{
					mWebLoadFinish = true;
					if( mWebLoadFinish && adUrl != null && !adUrl.equals( "" ) )
					{
						webView.loadUrl( "javascript:addImg('" + adUrl + "');" );
					}
				}
				super.onProgressChanged( view , newProgress );
			}
		};
		webView.setWebChromeClient( webChromeClient );
		/**
		 * zenLauncher End
		 */
		webView.setDownloadListener( new DownloadListener() {
			
			@Override
			public void onDownloadStart(
					String url ,
					String userAgent ,
					String contentDisposition ,
					String mimetype ,
					long contentLength )
			{
				Uri uri = Uri.parse( url );
				Intent intent = new Intent( Intent.ACTION_VIEW , uri );
				try
				{
					startActivity( intent );
				}
				catch( ActivityNotFoundException e )
				{
					Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_activity_not_found ) , Toast.LENGTH_SHORT ).show();
					Log.e( "SearchT9Main" , "Activity not found! intent:" + intent , e );
				}
				catch( SecurityException e )
				{
					Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_not_allowed_to_start_activity ) , Toast.LENGTH_SHORT ).show();
					Log.e( "SearchT9Main" , "Not allowed to start activity! intent:" + intent , e );
				}
			}
		} );
	}
	
	//	@JavascriptInterface
	public void setDialog()
	{
		if( hasWindowFocus() && !isFinishing() )
			WebviewLoadingDlg.ShowDlg( this );
	}
	
	//	@JavascriptInterface
	public void canelDialog()
	{
		WebviewLoadingDlg.CloseDlg();
	}
	
	@Override
	public boolean onKeyUp(
			int keyCode ,
			KeyEvent event )
	{
		if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
		{
			if( webView.getVisibility() == View.VISIBLE )
			{
				if( webView.canGoBack() )
				{
					webView.goBack();
					return true;
				}
			}
			else
			{
				if( kusoData.isC4() )
				{//显示运营页就返回到运营页
					webView.setVisibility( View.VISIBLE );
					bottom.setVisibility( View.GONE );
				}
				else
				{//否则到历史记录页
					bottom.setVisibility( View.VISIBLE );
					listView1.setVisibility( View.GONE );
				}
			}
		}
		return super.onKeyUp( keyCode , event );
	}
	
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++==
	abstract class myItemBase
	{
		
		View convertView;
		
		abstract View getConvertView(
				ViewGroup parent );
		
		boolean match(
				String text ,
				List<PinyinUnit> srcUnit )
		{
			return false;
		}
		
		boolean T9Match(
				String name ,
				List<PinyinUnit> srcUnit )
		{
			PinyinUnit src = srcUnit.get( 0 );
			if( src.isPinyin() )
			{
				return false;
			}
			//			Log.e( "" , "whj T9Match srcUnit size " + srcUnit.size() + " name " + name );
			String srcNumber = src.getT9PinyinUnitIndex().get( 0 ).getNumber();
			String srcPY = src.getT9PinyinUnitIndex().get( 0 ).getPinyin().toLowerCase();
			List<PinyinUnit> appSrcUnit = new ArrayList<PinyinUnit>();
			PinyinUtil.chineseStringToPinyinUnit( name , appSrcUnit );
			if( srcNumber.length() > appSrcUnit.size() )
			{
				return false;
			}
			for( int i = 0 ; i < srcNumber.length() ; i++ )
			{
				char number = srcNumber.charAt( i );
				char py = srcPY.charAt( i );
				T9PinyinUnit appT9Unit = appSrcUnit.get( i ).getT9PinyinUnitIndex().get( 0 );
				char appNumber = appT9Unit.getNumber().charAt( 0 );
				char appPY = appT9Unit.getPinyin().charAt( 0 );
				if( number == py )
				{
					//输入的是数字，只需要比较number
					if( appNumber != number )
					{
						return false;
					}
				}
				else
				{
					if( appNumber != number || appPY != py )
					{
						return false;
					}
				}
			}
			return true;
		}
		
		void onClickItem()
		{
		}
		
		boolean isEnabled()
		{
			return true;
		}
	};
	
	class MyListAdapter extends BaseAdapter
	{
		
		class myItemDivider extends myItemBase
		{
			
			@Override
			public View getConvertView(
					ViewGroup parent )
			{
				if( convertView == null )
				{
					convertView = getLayoutInflater().inflate( R.layout.kuso_divider_horizontal , parent , false );
				}
				return convertView;
			}
			
			@Override
			boolean isEnabled()
			{
				return false;
			}
		}
		
		/**
		 * 去除空格
		 * @param str
		 * @return
		 */
		private String removeAllSpace(
				String str )
		{
			String tmpstr = str.replace( " " , "" );
			return tmpstr;
		}
		
		// 应用
		class myItemApp extends myItemBase
		{
			
			ActivityInfo activityInfo;
			String name;
			List<PinyinUnit> dstUnit;
			
			public myItemApp(
					ActivityInfo activityInfo )
			{
				this.activityInfo = activityInfo;
				dstUnit = new ArrayList<PinyinUnit>();
				String title = activityInfo.loadLabel( getPackageManager() ).toString();
				/*	if( title.contains( "点传" ) )
					{
						String ss = title.trim();
						for( int i = 0 ; i < title.length() ; i++ )
						{
							char ch = title.charAt( i );
							if( ch == ' ' )//应用名称前后的空格
							{
								Log.e( "" , "whj title i " + i );
							}
							Log.w( "" , "whj title ch " + ch );
						}
						Log.v( "" , "whj title " + title.length() + " ss " + ss.length() );
					}*/
				name = removeAllSpace( title );
				PinyinUtil.chineseStringToPinyinUnit( name , dstUnit );
				//				Log.v( "" , "whj myItemApp name " + name + " dstUnit " + dstUnit.size() );
			}
			
			@Override
			boolean match(
					String text ,
					List<PinyinUnit> srcUnit )
			{
				if( T9Match( name , srcUnit ) )
				{
					return true;
				}
				else
				{
					//					Log.v( "" , "whj name.toLowerCase() " + name.toLowerCase() + " name " + name + " text.toLowerCase() " + text.toLowerCase() + " text " + text );
					return name.toLowerCase().contains( text.toLowerCase() );
				}
			}
			
			@Override
			public View getConvertView(
					ViewGroup parent )
			{
				if( convertView == null )
				{
					convertView = getLayoutInflater().inflate( R.layout.kuso_search_list_item_app , parent , false );
					Drawable icon = activityInfo.loadIcon( getPackageManager() );
					ImageView iconView = (ImageView)convertView.findViewById( R.id.kuso_search_list_item_icon );
					TextView textView = (TextView)convertView.findViewById( R.id.kuso_search_list_item_title );
					iconView.setImageDrawable( icon );
					textView.setText( name );
				}
				return convertView;
			}
			
			@Override
			void onClickItem()
			{
				imm.hideSoftInputFromWindow( kuso_search_edit.getWindowToken() , 0 );
				//cheyingkun add	//解决“点进酷搜后，输入T搜索出来应用列表中点联系人无法打开”的问题
				//cheyingkun del start
				//				Intent intent = new Intent( Intent.ACTION_VIEW );
				//				intent.setClassName( activityInfo.packageName , activityInfo.name );
				//cheyingkun del end
				//cheyingkun add start
				Intent intent = new Intent( Intent.ACTION_MAIN );
				intent.addCategory( Intent.CATEGORY_LAUNCHER );
				intent.setClassName( activityInfo.applicationInfo.packageName , activityInfo.name );
				//cheyingkun add end
				//cheyingkun end
				try
				{
					startActivity( intent );
				}
				catch( ActivityNotFoundException e )
				{
					Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_activity_not_found ) , Toast.LENGTH_SHORT ).show();
					Log.e( "SearchT9Main" , "Activity not found! intent:" + intent , e );
				}
				catch( SecurityException e )
				{
					Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_not_allowed_to_start_activity ) , Toast.LENGTH_SHORT ).show();
					Log.e( "SearchT9Main" , "Not allowed to start activity! intent:" + intent , e );
				}
			}
		}
		
		/*
		 * 联系人
		 */
		class myItemContacts extends myItemBase
		{
			
			Contacts contact;
			
			public myItemContacts(
					Contacts contact )
			{
				this.contact = contact;
			}
			
			@Override
			View getConvertView(
					ViewGroup parent )
			{
				if( convertView == null )
				{
					convertView = getLayoutInflater().inflate( R.layout.kuso_search_list_item_contact , parent , false );
					TextView textViewTitle = (TextView)convertView.findViewById( R.id.kuso_search_list_item_title );
					textViewTitle.setText( contact.getName() );
				}
				return convertView;
			}
			
			@Override
			boolean match(
					String text ,
					List<PinyinUnit> srcUnit )
			{
				StringBuffer chineseKeyWord = new StringBuffer();// In order to get Chinese KeyWords.Of course it's maybe not Chinese characters.
				if( T9Match( contact.getName() , srcUnit ) || contact.getName().toLowerCase().contains( text ) )
				{
					contact.setSearchByType( SearchByType.SearchByName );
					contact.setMatchKeywords( chineseKeyWord.toString() );
					chineseKeyWord.delete( 0 , chineseKeyWord.length() );
					return true;
				}
				else
				{
					return contact.getName().toLowerCase().contains( text.toLowerCase() );
				}
			}
			
			@Override
			void onClickItem()
			{
				Intent intent = new Intent( Intent.ACTION_VIEW , Uri.withAppendedPath( ContactsContract.Contacts.CONTENT_URI , String.valueOf( contact.getContactsID() ) ) );
				try
				{
					startActivity( intent );
				}
				catch( ActivityNotFoundException e )
				{
					Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_activity_not_found ) , Toast.LENGTH_SHORT ).show();
					Log.e( "SearchT9Main" , "Activity not found! intent:" + intent , e );
				}
				catch( SecurityException e )
				{
					Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_not_allowed_to_start_activity ) , Toast.LENGTH_SHORT ).show();
					Log.e( "SearchT9Main" , "Not allowed to start activity! intent:" + intent , e );
				}
			}
		}
		
		/*
		 * Music
		 */
		class myItemMusic extends myItemBase
		{
			
			int audioId;
			String musicPath;
			String musicArtist;
			String musicName;
			
			public myItemMusic(
					int audioId ,
					String musicName ,
					String musicArtist ,
					String musicPath )
			{
				this.audioId = audioId;
				this.musicName = musicName;
				this.musicArtist = musicArtist;
				this.musicPath = musicPath;
			}
			
			@Override
			boolean match(
					String text ,
					List<PinyinUnit> srcUnit )
			{
				if( T9Match( musicName , srcUnit ) || musicName.toLowerCase().contains( text ) )
				{
					return true;
				}
				else
				{
					return musicName.toLowerCase().contains( text.toLowerCase() );
				}
			}
			
			@Override
			View getConvertView(
					ViewGroup parent )
			{
				if( convertView == null )
				{
					convertView = getLayoutInflater().inflate( R.layout.kuso_search_list_item_music , parent , false );
					TextView text1 = (TextView)convertView.findViewById( R.id.kuso_search_list_item_title );
					text1.setText( musicName );
				}
				return convertView;
			}
			
			@Override
			void onClickItem()
			{
				Intent intent = new Intent( Intent.ACTION_VIEW , Uri.withAppendedPath( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI , String.valueOf( audioId ) ) );
				try
				{
					startActivity( intent );
				}
				catch( ActivityNotFoundException e )
				{
					Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_activity_not_found ) , Toast.LENGTH_SHORT ).show();
					Log.e( "SearchT9Main" , "Activity not found! intent:" + intent , e );
				}
				catch( SecurityException e )
				{
					Toast.makeText( getApplicationContext() , getResources().getString( R.string.kuso_not_allowed_to_start_activity ) , Toast.LENGTH_SHORT ).show();
					Log.e( "SearchT9Main" , "Not allowed to start activity! intent:" + intent , e );
				}
			}
		}
		
		//cheyingkun add start	//需求:仿谷歌搜索
		/*
		 * 浏览器
		 */
		class myItemBrowser extends myItemBase
		{
			
			@Override
			boolean match(
					String text ,
					List<PinyinUnit> srcUnit )
			{
				return true;
			}
			
			@Override
			View getConvertView(
					ViewGroup parent )
			{
				if( convertView == null )
				{
					convertView = getLayoutInflater().inflate( R.layout.kuso_search_list_item_browser , parent , false );
				}
				return convertView;
			}
			
			@Override
			void onClickItem()
			{
				gotoUrl( result );
			}
		}
		
		//cheyingkun add end
		// 应用
		List<myItemApp> allAppList = new ArrayList<myItemApp>();
		// 联系人
		List<myItemContacts> allContactsList = new ArrayList<myItemContacts>();
		// 音乐
		List<myItemMusic> allMusicList = new ArrayList<myItemMusic>();
		// 当前
		Stack<myItemBase> itemList = new Stack<myItemBase>();
		int itemListCnt;
		
		public void onClick(
				int index )
		{
			itemList.get( index ).onClickItem();
		}
		
		void initContent()
		{
			itemList.clear();
			itemListCnt = 0;
		}
		
		// 应用
		void loadContentApp()
		{
			Intent intent = new Intent();
			intent.setAction( Intent.ACTION_MAIN );
			intent.addCategory( Intent.CATEGORY_LAUNCHER );
			PackageManager pm = getPackageManager();
			List<ResolveInfo> resolveList = pm.queryIntentActivities( intent , 0 );
			List<myItemApp> _allAppList = new ArrayList<myItemApp>();
			for( ResolveInfo info : resolveList )
			{
				myItemApp item = new myItemApp( info.activityInfo );
				_allAppList.add( item );
			}
			allAppList = _allAppList;
		}
		
		// 音乐
		void loadContentMusic()
		{
			MusicLoader loader = MusicLoader.instance( SearchT9Main.this.getContentResolver() );
			List<myItemMusic> _allMusicList = new ArrayList<myItemMusic>();
			List<MusicInfo> musicList = loader.getMusicList();
			for( MusicInfo info : musicList )
			{
				_allMusicList.add( new myItemMusic( info.musicId , info.musicName , info.musicArtist , info.musicPath ) );
			}
			allMusicList = _allMusicList;
		}
		
		// 联系人
		void loadContentContacts()
		{
			List<Contacts> contacts = contactsHelper.getBaseContacts();
			List<myItemContacts> _allContactsList = new ArrayList<myItemContacts>();
			for( Contacts contact : contacts )
			{
				_allContactsList.add( new myItemContacts( contact ) );
			}
			allContactsList = _allContactsList;
		}
		
		void loadContent()
		{
			new Thread() {
				
				public void run()
				{
					loadContentApp();
					loadContentMusic();
				};
			}.start();
			contactsHelper = ContactsHelper.getInstance( SearchT9Main.this );
			contactsHelper.setOnContactsLoad( new OnContactsLoad() {
				
				@Override
				public void onContactsLoadSuccess()
				{
					loadContentContacts();
				}
				
				@Override
				public void onContactsLoadFailed()
				{
				}
			} );
			contactsHelper.startLoadContacts();
		}
		
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		synchronized void searchContentNoWeb(
				String text )
		{
			initContent();
			if( text != null && text.length() > 0 )
			{
				List<PinyinUnit> srcUnit = new ArrayList<PinyinUnit>();
				PinyinUtil.chineseStringToPinyinUnit( text , srcUnit );
				// 应用
				for( myItemApp app : allAppList )
				{
					if( app.match( text , srcUnit ) )
					{
						itemList.add( app );
						itemList.add( new myItemDivider() );
					}
				}
				if( !itemList.isEmpty() )
				{
					if( itemList.peek() instanceof myItemDivider )
						itemList.pop();
				}
				// 联系人
				for( myItemContacts contact : allContactsList )
				{
					if( contact.match( text , srcUnit ) )
					{
						itemList.add( contact );
						itemList.add( new myItemDivider() );
					}
				}
				if( !itemList.isEmpty() )
				{
					if( itemList.peek() instanceof myItemDivider )
						itemList.pop();
				}
				// 音乐
				for( myItemMusic music : allMusicList )
				{
					if( music.match( text , srcUnit ) )
					{
						itemList.add( music );
						itemList.add( new myItemDivider() );
					}
				}
				if( !itemList.isEmpty() )
				{
					if( itemList.peek() instanceof myItemDivider )
						itemList.pop();
				}
				//搜索选项最后添加一项,在网页搜索输入内容
				itemList.add( new myItemBrowser() );//cheyingkun add	//需求:仿谷歌搜索
			}
			itemListCnt = itemList.size();
		}
		
		@Override
		public boolean isEnabled(
				int position )
		{
			return itemList.get( position ).isEnabled();
		}
		
		@Override
		public int getCount()
		{
			return itemList.size();
		}
		
		@Override
		public Object getItem(
				int position )
		{
			return itemList.get( position );
		}
		
		@Override
		public long getItemId(
				int position )
		{
			return position;
		}
		
		@Override
		public View getView(
				int position ,
				View convertView ,
				ViewGroup parent )
		{
			myItemBase item = itemList.get( position );
			return item.getConvertView( parent );
		}
	}
	
	//cheyingkun add start	//需求:仿谷歌搜索
	/**是否是谷歌搜索标志位*/
	public static boolean isGoogle(
			Context context )
	{
		if( context == null )
		{
			return false;
		}
		//
		String engine_head;
		SharedPreferences sp = context.getSharedPreferences( "sp_setting" , 0 );
		KusoData kusoData = KuSoHelper.getInstance( context ).getKusoData();
		ArrayList<KusoEngineInfo> list = new ArrayList<KusoEngineInfo>();
		list.addAll( kusoData.getEngines() );
		int k = sp.getInt( "k" , -1 );
		if( k < 0 )
		{
			if( kusoData.getEngines().size() > 0 )
			{
				for( int i = 0 ; i < list.size() ; i++ )
				{
					if( list.get( i ).isR6() )//是否默认引擎
					{
						engine_head = list.get( i ).getR4() + list.get( i ).getR5();
						if( engine_head.contains( "www.google." ) )
						{
							return true;
						}
						else
						{
							return false;
						}
					}
				}
			}
		}
		else
		{
			engine_head = sp.getString( "engine_url" , "" );
			if( engine_head.contains( "www.google." ) )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		//本地判断
		String curLan = Locale.getDefault().toString();
		String imsi = CoolMethod.getImsi( context );
		if( curLan.equals( "zh_CN" ) || curLan.equals( "zh_TW" ) || curLan.equals( "zh_HK" ) || imsi.startsWith( "460" ) )
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	//cheyingkun add end
	/**
	 * 给js调用的Java接口
	 * @author huanghui
	 *
	 */
	public class JavaScriptObject
	{
		
		private final Handler mHandler = new Handler();
		
		// gaominghui@2017/01/05 ADD START 兼容android 4.0
		//@JavascriptInterface
		// gaominghui@2017/01/05 ADD END 兼容android 4.0
		public void clickOnAndroid()
		{
			mHandler.post( new Runnable() {
				
				@Override
				public void run()
				{
					// This gets executed on the UI thread so it can safely modify Views
					KmobManager.onClickDone( adplaceid , adid , clickurl , interactiontype , open_type , package_name , click_record_url , headline , download , summary , adlogo );
				}
			} );
		}
		
		// gaominghui@2017/01/05 ADD START 兼容android 4.0
		//@JavascriptInterface
		// gaominghui@2017/01/05 ADD END 兼容android 4.0
		public void notifyAdShow()
		{
			mHandler.post( new Runnable() {
				
				@Override
				public void run()
				{
					// This gets executed on the UI thread so it can safely modify Views
					KmobManager.onNativeAdShow( adplaceid , adid );
				}
			} );
		}
	}
	
	/**
	 * 通过广告传入的数据生成一个NativeAdData
	 */
	private NativeData createNativeData(
			JSONObject object )
	{
		String summary = "";
		String headline = "";
		String adcategory = "";
		String appRating = "";
		String adlogo = "";
		String details = "";
		String adlogoWidth = "";
		String adlogoHeight = "";
		String review = "";
		String appinstalls = "";
		String download = "";
		String adplaceid = "";
		String adid = "";
		String clickurl = "";
		String interactiontype = "";
		String open_type = "";
		String hurl = "";
		String hdetailurl = "";
		String pkgname = "";
		String appsize = "";
		String version = "";
		String versionname = "";
		String ctimg = "";
		String hiimg = "";
		String click_record_url = "";
		try
		{
			if( object.has( NativeAdData.SUMMARY_TAG ) )
			{
				summary = object.getString( NativeAdData.SUMMARY_TAG );
			}
			if( object.has( NativeAdData.HEADLINE_TAG ) )
			{
				headline = object.getString( NativeAdData.HEADLINE_TAG );
			}
			if( object.has( NativeAdData.ADCATEGORY_TAG ) )
			{
				adcategory = object.getString( NativeAdData.ADCATEGORY_TAG );
			}
			if( object.has( NativeAdData.APPRATING_TAG ) )
			{
				appRating = object.getString( NativeAdData.APPRATING_TAG );
			}
			if( object.has( NativeAdData.ADLOGO_TAG ) )
			{
				adlogo = object.getString( NativeAdData.ADLOGO_TAG );
			}
			if( object.has( NativeAdData.DETAILS_TAG ) )
			{
				details = object.getString( NativeAdData.DETAILS_TAG );
			}
			if( object.has( NativeAdData.ADLOGO_WIDTH_TAG ) )
			{
				adlogoWidth = object.getString( NativeAdData.ADLOGO_WIDTH_TAG );
			}
			if( object.has( NativeAdData.ADLOGO_HEIGHT_TAG ) )
			{
				adlogoHeight = object.getString( NativeAdData.ADLOGO_HEIGHT_TAG );
			}
			if( object.has( NativeAdData.REVIEW_TAG ) )
			{
				review = object.getString( NativeAdData.REVIEW_TAG );
			}
			if( object.has( NativeAdData.APPINSTALLS_TAG ) )
			{
				appinstalls = object.getString( NativeAdData.APPINSTALLS_TAG );
			}
			if( object.has( NativeAdData.DOWNLOAD_TAG ) )
			{
				download = object.getString( NativeAdData.DOWNLOAD_TAG );
			}
			if( object.has( NativeAdData.ADPLACE_ID_TAG ) )
			{
				adplaceid = object.getString( NativeAdData.ADPLACE_ID_TAG );
			}
			if( object.has( NativeAdData.AD_ID_TAG ) )
			{
				adid = object.getString( NativeAdData.AD_ID_TAG );
			}
			if( object.has( NativeAdData.CLICKURL_TAG ) )
			{
				clickurl = object.getString( NativeAdData.CLICKURL_TAG );
			}
			if( object.has( NativeAdData.INTERACTION_TYPE_TAG ) )
			{
				interactiontype = object.getString( NativeAdData.INTERACTION_TYPE_TAG );
			}
			if( object.has( NativeAdData.OPEN_TYPE_TAG ) )
			{
				open_type = object.getString( NativeAdData.OPEN_TYPE_TAG );
			}
			if( object.has( NativeAdData.HURL_TAG ) )
			{
				hurl = object.getString( NativeAdData.HURL_TAG );
			}
			if( object.has( NativeAdData.HDETAILURL_TAG ) )
			{
				hdetailurl = object.getString( NativeAdData.HDETAILURL_TAG );
			}
			if( object.has( NativeAdData.PKGNAME_TAG ) )
			{
				pkgname = object.getString( NativeAdData.PKGNAME_TAG );
			}
			if( object.has( NativeAdData.APPSIZE_TAG ) )
			{
				appsize = object.getString( NativeAdData.APPSIZE_TAG );
			}
			if( object.has( NativeAdData.VERSION_TAG ) )
			{
				version = object.getString( NativeAdData.VERSION_TAG );
			}
			if( object.has( NativeAdData.VERSIONNAME_TAG ) )
			{
				versionname = object.getString( NativeAdData.VERSIONNAME_TAG );
			}
			if( object.has( NativeAdData.CTIMG_TAG ) )
			{
				ctimg = object.getString( NativeAdData.CTIMG_TAG );
			}
			if( object.has( NativeAdData.HIIMG_TAG ) )
			{
				hiimg = object.getString( NativeAdData.HIIMG_TAG );
			}
			if( object.has( NativeAdData.CLICK_RECORD_URL_TAG ) )
			{
				click_record_url = object.getString( NativeAdData.CLICK_RECORD_URL_TAG );
			}
			return new NativeData(
					summary ,
					headline ,
					adcategory ,
					appRating ,
					adlogo ,
					details ,
					adlogoWidth ,
					adlogoHeight ,
					review ,
					appinstalls ,
					download ,
					adplaceid ,
					adid ,
					clickurl ,
					interactiontype ,
					open_type ,
					hurl ,
					hdetailurl ,
					pkgname ,
					appsize ,
					version ,
					versionname ,
					ctimg ,
					hiimg ,
					click_record_url );
		}
		catch( Exception e )
		{
			Log.e( "KMOB" , "addAdView e " + e.toString() );
		}
		return null;
	}
	
	//cheyingkun add start	//搜索界面背景模糊	
	public static void setBlurBG(
			Bitmap mBlurBG )
	{
		if( mBlurBG == null )
		{
			return;
		}
		if( searchT9MainBlurView != null )
		{
			searchT9MainBlurView.setBackgroundDrawable( new BitmapDrawable( mBlurBG ) );
		}
		SearchT9Main.mBlurBG = mBlurBG;
	}
	
	//cheyingkun add end
	// zhangjin@2016/03/29 ADD START
	private static String mSn = "";
	
	public static String getSnFromConfig(
			Context context )
	{
		if( TextUtils.isEmpty( mSn ) == false )
		{
			return mSn;
		}
		String p04 = "";
		InputStream is = null;
		try
		{
			is = context.getAssets().open( "config.ini" );
			BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
			StringBuilder sb = new StringBuilder();
			String line;
			while( ( line = reader.readLine() ) != null )
			{
				sb.append( line );
			}
			JSONObject json = new JSONObject( sb.toString() );
			JSONObject jo = json.getJSONObject( "config" );
			p04 = jo.getString( "serialno" );
		}
		catch( Exception e )
		{
			// e.printStackTrace();
			// KpshLog.e(e.toString());
		}
		finally
		{
			if( is != null )
			{
				try
				{
					is.close();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		mSn = p04;
		return mSn;
	}
	// zhangjin@2016/03/29 ADD END
}
