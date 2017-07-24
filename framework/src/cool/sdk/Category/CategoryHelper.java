package cool.sdk.Category;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cooee.framework.config.defaultConfig.BaseDefaultConfig;
import com.cooee.framework.function.Category.CategoryParse;
import com.cooee.framework.function.DynamicEntry.OperateDynamicUtils;
import com.cooee.framework.utils.StringUtils;

import cool.sdk.download.manager.dl_info;


// floder category , change by shlt@2014/12/08 UPD
public class CategoryHelper extends CategoryUpdate
{
	
	public interface CategoryListener
	{
		
		public void onRecommendIconDownload(
				int fid );
		
		public void onCategoryRecommendListChange(
				String version );
		
		public void addOrRemoveCategoryEntry(
				boolean isAdd );
	}
	
	private CategoryListener mListener;
	//cheyingkun end
	Context mContext = null;
	
	protected CategoryHelper(
			Context context )
	{
		super( context );
		mContext = context;
	}
	
	private static CategoryHelper instance = null;
	
	public static CategoryHelper getInstance(
			Context context )
	{
		if( instance == null )
		{
			synchronized( CategoryHelper.class )
			{
				if( instance == null )
				{
					instance = new CategoryHelper( context );
				}
			}
		}
		return instance;
	}
	
	public void setListener(
			CategoryListener listener )
	{
		this.mListener = listener;
	}
	
	@Override
	public JSONArray getIdList()
	{
		// TODO Auto-generated method stub
		JSONArray idList = new JSONArray();
		//先简单粗暴的把所有的一级目录提交
		idList.put( -1 );
		idList.put( 0 );
		idList.put( 800 );
		idList.put( 801 );
		idList.put( 802 );
		idList.put( 803 );
		idList.put( 804 );
		idList.put( 805 );
		idList.put( 806 );
		idList.put( 807 );
		idList.put( 808 );
		return idList;
	}
	
	// 获取是否处于智能分类状态：true:处于智能分类  false:不处于智能分类
	@Override
	public boolean isCategoryState()
	{
		// TODO Auto-generated method stub
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( mContext );
		if( sp.getString( "classificationTime" , null ) != null )
		{
			return true;
		}
		return false;
	}
	
	// 获取DefaultLayout的配置 0: 禁止 1:显式 2：运营出来
	@Override
	protected int getConfigType()
	{
		// TODO Auto-generated method stub
		return BaseDefaultConfig.CONFIG_CATEGORY_TYPE;//DefaultLayout.enable_icon_category;
	}
	
	public void UpdateMapData()
	{
		try
		{
			lockMap.readLock().lock();
			if( !treeMap.isEmpty() )
			{
				treeMap.clear();
			}
			dbTool.getAllTree();
			if( !dictMap.isEmpty() )
			{
				dictMap.clear();
			}
			dbTool.getAllDict();
			if( !cateinfoMap.isEmpty() )
			{
				cateinfoMap.clear();
			}
			dbTool.getAllCateInfo();
			if( !RecommendInfoMap.isEmpty() )
			{
				RecommendInfoMap.clear();
			}
			dbTool.getAllRecommendInfo();
		}
		finally
		{
			lockMap.readLock().unlock();
		}
		if( RecommendInfoMap.isEmpty() )
		{
			decodeRecommendListJsonObject();
		}
	}
	
	//此API仅供测试使用
	public void ClearAllData()
	{
		setValue( "cateinfoFloderIDJSONArray" , (String)null );
		setValue( "Recommend_list" , (String)null );
		setValue( "Recommend_c2" , (String)null );
		setValue( "Recommend_c3" , (String)null );
		setValue( "Recommend_c4" , (String)null );
		setValue( "c0" , (String)null );
		setValue( "c1" , (String)null );
		setValue( "c2" , (String)null );
		setValue( "c3" , (String)null );
		setValue( "IsDidForeCategoryReques" , (String)null );
		setValue( KEY_CategoryRequest_START_TIME , (Long)null );
		setValue( "doCategoryStatisticsActive" , (Long)null );
		setValue( "doCategoryStatisticsRecommendActive" , (Long)null );
		dbTool.CleanAllTables();
	}
	
	@Override
	protected void onRecommendIconDownload(
			int fid )
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , StringUtils.concat( "onRecommendIconDownload:" , fid ) );
		//		CategoryParse.getInstance().addRecommendate( fid );
		//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
		//		OperateHelp.getInstance( mLauncher ).updateRecommendApkIcon( fid );//cheyingkun del
		if( mListener != null )
		{
			mListener.onRecommendIconDownload( fid );
		}
		//cheyingkun end
	}
	
	@Override
	protected void onCategoryBGRequestComplete()
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , "onCategoryBGRequestComplete" );
		CategoryParse.getInstance().bgRequestComplete( mContext );
	}
	
	@Override
	protected void onCategoryRecommendConfigChange(
			String version )
	{
		// TODO Auto-generated method stub
		if( DEFAULT_VERSION.equals( version ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "COOL" , "onCategoryRecommendConfigChange first" );
		}
		else
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "COOL" , "onCategoryRecommendConfigChange" );
		}
	}
	
	@Override
	protected void onCategoryRecommendListChange(
			String version )
	{
		// TODO Auto-generated method stub
		if( DEFAULT_VERSION.equals( version ) )
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "COOL" , "onCategoryRecommendListChange first" );
		}
		else
		{
			if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.v( "COOL" , "onCategoryRecommendListChange" );
			//cheyingkun start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
			//			OperateHelp.getInstance( mLauncher ).loadAndInsertRecommendDate();//cheyingkun del
			if( mListener != null )
			{
				mListener.onCategoryRecommendListChange( version );
			}
			//cheyingkun end
		}
	}
	
	@Override
	protected void onCategoryRecommendIconChange()
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , "onCategoryRecommendIconChange" );
	}
	
	public List<RecommendApkInfo> getRecommendApkInfoList(
			int fid )
	{
		try
		{
			lockMap.readLock().lock();
			List<RecommendApkInfo> recommendApkInfoList = new ArrayList<RecommendApkInfo>();
			RecommendInfo recommendInfo = RecommendInfoMap.get( fid );
			if( recommendInfo != null )
			{
				Map<String , RecommendApkInfo> infoMap = recommendInfo.getApkinfoMap();
				for( int i = 0 ; i < infoMap.size() ; i++ )
				{
					String itemKey = StringUtils.concat( recommendInfo.getFolderID() , "_" , i );
					RecommendApkInfo itemVal = infoMap.get( itemKey );
					if( itemVal != null )
					{
						//						dl_info info = getCoolDLMgrIcon().IconGetInfo( itemVal.getPkgName() );
						dl_info info = null;
						if( itemVal.getApkType() == OperateDynamicUtils.VIRTUAL_LINK )
						{
							info = getCoolDLMgrIcon().UrlGetInfo( itemVal.getLinkAddr() );
						}
						else
						{
							info = getCoolDLMgrIcon().IconGetInfo( itemVal.getPkgName() , "drawable" );
						}
						if( info != null && info.IsDownloadSuccess() )
						{
							itemVal.setApkIconpath( info.getFilePath() );
							//智能分类数据加载异常(推荐数据经常不能显示，原因是推荐apk的icon没有下载完成) , change by shlt@2015/01/15 DEL START
							// recommendApkInfoList.add( itemVal );//test,暂时添加，等待SDK提供新接口在修改
							//智能分类数据加载异常(推荐数据经常不能显示，原因是推荐apk的icon没有下载完成) , change by shlt@2015/01/15 DEL END
						}
						//智能分类数据加载异常(推荐数据经常不能显示，原因是推荐apk的icon没有下载完成) , change by shlt@2015/01/15 ADD START
						recommendApkInfoList.add( itemVal );
						//智能分类数据加载异常(推荐数据经常不能显示，原因是推荐apk的icon没有下载完成) , change by shlt@2015/01/15 ADD END
					}
				}
			}
			return recommendApkInfoList;
		}
		finally
		{
			lockMap.readLock().unlock();
		}
	}
	
	//cheyingkun del start	//解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
	//	public static void setLauncher(
	//			Launcher launcher )
	//	{
	//		mLauncher = launcher;
	//	}
	//cheyingkun del end
	private boolean iconDownloadFinish(
			RecommendApkInfo apkInfo ,
			List<dl_info> iconList )
	{
		String findPkgName = null;
		String pkgName = apkInfo.getPkgName();
		for( dl_info info : iconList )
		{
			findPkgName = (String)info.getValue( "p2" );
			if( findPkgName != null && findPkgName.equals( pkgName ) )
			{
				if( info != null && info.IsDownloadSuccess() )
				{
					apkInfo.setApkIconpath( info.getFilePath() );
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}
	
	public List<RecommendApkInfo> getRecommendApkInfoList(
			int fid ,
			List<dl_info> iconList )
	{
		try
		{
			lockMap.readLock().lock();
			List<RecommendApkInfo> recommendApkInfoList = new ArrayList<RecommendApkInfo>();
			if( iconList == null || iconList.size() <= 0 )
			{
				return recommendApkInfoList;
			}
			RecommendInfo recommendInfo = RecommendInfoMap.get( fid );
			ArrayList<String> nameList = new ArrayList<String>();
			if( recommendInfo != null && iconList != null )
			{
				Map<String , RecommendApkInfo> infoMap = recommendInfo.getApkinfoMap();
				for( int i = 0 ; i < infoMap.size() ; i++ )
				{
					String itemKey = StringUtils.concat( recommendInfo.getFolderID() , "_" , i );
					RecommendApkInfo itemVal = infoMap.get( itemKey );
					if( itemVal != null )
					{
						if( iconDownloadFinish( itemVal , iconList ) )
						{
							if( nameList.contains( itemVal.getPkgName() ) )
							{
								continue;
							}
							else
							{
								nameList.add( itemVal.getPkgName() );
							}
							recommendApkInfoList.add( itemVal );
						}
					}
				}
			}
			return recommendApkInfoList;
		}
		finally
		{
			lockMap.readLock().unlock();
		}
	}
	
	@Override
	protected void onCategoryRecommendChange()
	{
		// TODO Auto-generated method stub
		if( BaseDefaultConfig.SWITCH_ENABLE_DEBUG )
			Log.v( "COOL" , "onCategoryRecommendChange" );
		//		CsProxy.getInstance().onCategoryRecommendIconChange();
		//		CsProxy.getInstance().onCategoryRecommendConfigChange();
	}
	
	public void addOrRemoveCategoryEntry(
			boolean isAdd )
	{
		if( mListener != null )
		{
			mListener.addOrRemoveCategoryEntry( isAdd );
		}
	}
}
