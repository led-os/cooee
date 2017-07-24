package com.cooee.phenix.effects;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.util.Log;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.launcherSettings.LauncherAppListEffectFragment;
import com.cooee.phenix.launcherSettings.LauncherEffectFragment;


public class EffectFactory
{
	
	private IEffect iEffect;
	//WangLei start //切页特效可配置
	//WangLei del start
	/*public EffectFactory(
			IEffect iEffect )
	{
		this.iEffect = iEffect;
	}*/
	//WangLei del end
	//WangLei add start
	private static String TAG = "EffectFactory";
	private Context mContext;
	private List<CharSequence> mWorkspaceConfigs;
	//chenliang add start	//优化桌面设置中切页特效的配置方式：1.去掉无用的配置项“workspace_effect_entries”和“applist_effect_entries” 2.去掉了配置项中“workspace_effect_configs_values”和“applist_effect_configs_values”的索引值
	private final static String EFFECT_TYPE_KEY_CLASSIC = "经典";
	private final static String EFFECT_TYPE_KEY_LAMINATED = "层叠";
	private final static String EFFECT_TYPE_KEY_INNER_CUBE = "内立方体";
	private final static String EFFECT_TYPE_KEY_CUBE = "立方体";
	private final static String EFFECT_TYPE_KEY_SECTOR = "扇面";
	private final static String EFFECT_TYPE_KEY_ROLLOVER = "翻转";
	private final static String EFFECT_TYPE_KEY_WINDMILL = "风车";
	private final static String EFFECT_TYPE_KEY_WHEEL = "车轮";
	private final static String EFFECT_TYPE_KEY_FOLD_UNFOLD = "聚散";
	private final static String EFFECT_TYPE_KEY_SNAKE = "贪吃蛇";
	private final static String EFFECT_TYPE_KEY_CHORD = "琴弦";
	private final static String EFFECT_TYPE_KEY_RANDOM = "随机";
	private final static String EFFECT_TYPE_KEY_JERK = "跃动";
	private final static String[] ALL_EFFECT_ENTRIES = {
			EFFECT_TYPE_KEY_CLASSIC ,
			EFFECT_TYPE_KEY_LAMINATED ,
			EFFECT_TYPE_KEY_INNER_CUBE ,
			EFFECT_TYPE_KEY_CUBE ,
			EFFECT_TYPE_KEY_SECTOR ,
			EFFECT_TYPE_KEY_ROLLOVER ,
			EFFECT_TYPE_KEY_WINDMILL ,
			EFFECT_TYPE_KEY_WHEEL ,
			EFFECT_TYPE_KEY_FOLD_UNFOLD ,
			EFFECT_TYPE_KEY_SNAKE ,
			EFFECT_TYPE_KEY_CHORD ,
			EFFECT_TYPE_KEY_RANDOM ,
			EFFECT_TYPE_KEY_JERK };
	//chenliang add end
	
	public EffectFactory(
			IEffect iEffect ,
			Context context )
	{
		this.iEffect = iEffect;
		this.mContext = context;
	}
	//WangLei add end
	//WangLei end
	;
	
	private static List<EffectInfo> allEffects = new ArrayList<EffectInfo>();
	//WangLei add start //桌面和主菜单特效的分离
	private static List<EffectInfo> appListEffects = new ArrayList<EffectInfo>();
	private List<CharSequence> mAppListConfigs;
	//WangLei add end
	;
	
	public List<EffectInfo> getAllEffects()
	{
		return loadEffectsList();
	}
	
	/**
	 * 获取编辑模式下的平滑特效 i_0011941
	 * @return
	 */
	public EffectInfo getStandardEffect()
	{
		for( int i = 0 , count = allEffects.size() ; i < count ; i++ )
		{
			EffectInfo eInfo = allEffects.get( i );
			if( eInfo instanceof CuboidEffect )
			{
				return eInfo;
			}
		}
		return null;
	}
	
	// zhangjin@2015/07/28 ADD END
	public EffectInfo getEffect(
			int id )
	{
		if( id == 0 )
		{
			return null; // if is is 0 , we return null , and mean we do not need animation for workspace
		}
		if( allEffects.isEmpty() )
		{
			loadEffectsList();
		}
		for( int i = 0 , count = allEffects.size() ; i < count ; i++ )
		{
			EffectInfo eInfo = allEffects.get( i );
			if( eInfo.id == id )
			{
				return eInfo;
			}
		}
		return null;
	}
	
	//	public static EffectInfo getCurrentEffect(
	//			Context context )
	//	{
	//		SharedPreferences mSpaceTypeShared = context.getSharedPreferences( WorkspaceStyleSettings.WORKSPACE_STYLE , Context.MODE_PRIVATE );
	//		int id = mSpaceTypeShared.getInt( WorkspaceStyleSettings.KEY_ANIMATION_STYLE , 0 );
	//		for( int i = 0 , count = allEffects.size() ; i < count ; i++ )
	//		{
	//			EffectInfo eInfo = allEffects.get( i );
	//			if( eInfo.id == id )
	//			{
	//				return eInfo;
	//			}
	//		}
	//		return null;
	//	}
	private List<EffectInfo> loadEffectsList()
	{
		allEffects.clear();
		//		DefaultEffect defaultEffect = new DefaultEffect( 0 );
		//		allEffects.add( defaultEffect );
		//				TurnOverEffect turnOverEffect = new TurnOverEffect( 10 );
		//				allEffects.add( turnOverEffect );
		//		ClassicEffect classicEffect = new ClassicEffect( 0 );
		//		allEffects.add( classicEffect );
		//WangLei start //切页特效可配置
		//WangLei del start
		/*CuboidEffect cuboidEffect = new CuboidEffect( 1 , iEffect );//无效果(默认)
		allEffects.add( cuboidEffect );
		LayerEffect layerEffect = new LayerEffect( 2 , iEffect );//层叠
		allEffects.add( layerEffect );
		CubeEffect cubeInEffect = new CubeEffect( 3 , true , iEffect );//内立方
		allEffects.add( cubeInEffect );
		CubeEffect cubeOutEffect = new CubeEffect( 4 , false , iEffect );//外立方
		allEffects.add( cubeOutEffect );
		PageEffect pageEffect = new PageEffect( 5 , iEffect ); //扇面
		allEffects.add( pageEffect );
		OverturnEffect overturnEffect = new OverturnEffect( 6 , iEffect );//翻转
		allEffects.add( overturnEffect );
		RotateEffect rotateEffect = new RotateEffect( 7 , iEffect );//风车
		allEffects.add( rotateEffect );
		//		WindmillEffect windmillEffect = new WindmillEffect( 7 );//风车(没用到)
		//		allEffects.add( windmillEffect );
		WheelEffect wheelEffect = new WheelEffect( 8 , iEffect );//车轮
		allEffects.add( wheelEffect );
		PartingEffect partingEffect = new PartingEffect( 9 , iEffect );//聚散
		allEffects.add( partingEffect );
		SnakeEffect snakeEffect = new SnakeEffect( 10 , iEffect );//没用到 假的贪吃蛇
		allEffects.add( snakeEffect );
		StringsEffect stringsEffect = new StringsEffect( 11 , iEffect );//琴弦
		allEffects.add( stringsEffect );*/
		//WangLei del end
		//WangLei add start 
		mWorkspaceConfigs = LauncherEffectFragment.getEntryConfigList( mContext );
		EffectInfo effectInfo = null;
		for( int i = 0 ; i < mWorkspaceConfigs.size() ; i++ )
		{
			effectInfo = getEffectByEntry( mWorkspaceConfigs.get( i ) , i );
			if( effectInfo != null )
			{
				allEffects.add( effectInfo );
			}
		}
		//WangLei add end
		//WangLei end
		//		CarouselEffect carouselLeftEffect = new CarouselEffect( 5 , true );//旋转木马
		//		allEffects.add( carouselLeftEffect );
		//		FadeEffect fadeEffect = new FadeEffect( 2 );//没用到
		//		allEffects.add( fadeEffect );
		return allEffects;
	}
	
	//WangLei add start //切页特效可配置
	/**根据配置切页特效获取切页特效对象*/
	public EffectInfo getEffectByEntry(
			CharSequence charSequence ,
			int configIndex )
	{
		int entryIndex = getEntryIndex( charSequence );
		if( entryIndex == -1 )
		{
			return null;
		}
		EffectInfo effectInfo = null;
		switch( entryIndex )
		{
			case 0:
				effectInfo = new CuboidEffect( configIndex + 1 , iEffect );//平滑
				break;
			case 1:
				effectInfo = new LayerEffect( configIndex + 1 , iEffect );//层叠
				break;
			case 2:
				effectInfo = new CubeEffect( configIndex + 1 , true , iEffect );//内立方
				break;
			case 3:
				effectInfo = new CubeEffect( configIndex + 1 , false , iEffect );//外立方
				break;
			case 4:
				effectInfo = new PageEffect( configIndex + 1 , iEffect ); //扇面
				break;
			case 5:
				effectInfo = new OverturnEffect( configIndex + 1 , iEffect );//翻转
				break;
			case 6:
				effectInfo = new RotateEffect( configIndex + 1 , iEffect );//风车
				break;
			case 7:
				effectInfo = new WheelEffect( configIndex + 1 , iEffect );//车轮
				break;
			case 8:
				effectInfo = new PartingEffect( configIndex + 1 , iEffect );//聚散
				break;
			case 9:
				effectInfo = new SnakeEffect( configIndex + 1 , iEffect );//没用到 假的贪吃蛇
				break;
			case 10:
				effectInfo = new StringsEffect( configIndex + 1 , iEffect );//琴弦
				break;
			case 11:
				//Random 
				break;
			case 12:
				effectInfo = new PhenixCubeEffect( configIndex + 1 , iEffect );//跃动
				break;
		}
		return effectInfo;
	}
	
	/**获取配置的某一项特效在所有特效序列里的索引*/
	public int getEntryIndex(
			CharSequence charSequence )
	{
		if( charSequence == null || charSequence.length() == 0 )
		{
			if( LauncherDefaultConfig.SWITCH_ENABLE_DEBUG )
				Log.d( TAG , "entry is null or entry's length is 0" );
			return -1;
		}
		for( int i = 0 ; i < ALL_EFFECT_ENTRIES.length ; i++ )
		{
			if( charSequence.equals( ALL_EFFECT_ENTRIES[i] ) )
			{
				return i;
			}
		}
		return -1;
	}
	
	/**当前选择的特效是否是随机选项*/
	public boolean isRandomEffect(
			int id )
	{
		if( mWorkspaceConfigs == null )
		{
			mWorkspaceConfigs = LauncherEffectFragment.getEntryConfigList( mContext );
		}
		if( id < 0 && id >= mWorkspaceConfigs.size() )
		{
			return true;
		}
		return( mWorkspaceConfigs.get( id ).equals( EFFECT_TYPE_KEY_RANDOM ) );
	}
	
	/**从现有的特效集合里随机返回一个*/
	public EffectInfo getRandomEffect()
	{
		if( allEffects.isEmpty() )
		{
			loadEffectsList();
		}
		int effectNum = allEffects.size();
		int index = new Random().nextInt( effectNum );
		return allEffects.get( index );
	}
	//WangLei add end
	;
	
	//WangLei add start //桌面和主菜单特效的分离
	/**生成所配置的主菜单特效对象*/
	private void loadAppEffectList()
	{
		appListEffects.clear();
		mAppListConfigs = LauncherAppListEffectFragment.getEntryConfigList( mContext );
		EffectInfo effectInfo = null;
		for( int i = 0 ; i < mAppListConfigs.size() ; i++ )
		{
			effectInfo = getEffectByEntry( mAppListConfigs.get( i ) , i );
			if( effectInfo != null )
			{
				appListEffects.add( effectInfo );
			}
		}
	}
	
	/**根据Id获取主菜单特效对象*/
	public EffectInfo getAppEffect(
			int id )
	{
		if( id == 0 )
		{
			return null;
		}
		if( appListEffects.isEmpty() )
		{
			loadAppEffectList();
		}
		for( int i = 0 , count = appListEffects.size() ; i < count ; i++ )
		{
			EffectInfo effectInfo = appListEffects.get( i );
			if( effectInfo.id == id )
			{
				return effectInfo;
			}
		}
		return null;
	}
	
	/**当前选择的特效是否是随机选项*/
	public boolean isAllAppRandomEffect(
			int id )
	{
		if( mAppListConfigs == null )
		{
			mAppListConfigs = LauncherAppListEffectFragment.getEntryConfigList( mContext );
		}
		if( id < 0 || id >= mAppListConfigs.size() )
		{
			return true;
		}
		return( mAppListConfigs.get( id ).equals( EFFECT_TYPE_KEY_RANDOM ) );
	}
	
	/**从主菜单特效集合里随机返回一个特效对象*/
	public EffectInfo getAppRandomEffectInfo()
	{
		if( appListEffects.isEmpty() )
		{
			loadAppEffectList();
		}
		int effectNum = appListEffects.size();
		int index = new Random().nextInt( effectNum );
		return appListEffects.get( index );
	}
	//WangLei add end
	//chenliang add start //优化桌面设置中切页特效的配置方式：1.去掉无用的配置项“workspace_effect_entries”和“applist_effect_entries” 2.去掉了配置项中“workspace_effect_configs_values”和“applist_effect_configs_values”的索引值
	/**显示项内容*/
	public static List<CharSequence> getEffectEntries(
			List<CharSequence> mEntryConfigList )
	{
		List<CharSequence> mEffectEntiesList = new ArrayList<CharSequence>();
		for( int i = 0 ; i < mEntryConfigList.size() ; i++ )
		{
			CharSequence mEffectTypeKey = mEntryConfigList.get( i );
			int mEffectTitleId = -1;
			if( EFFECT_TYPE_KEY_JERK.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_jerk;
			}
			else if( EFFECT_TYPE_KEY_CLASSIC.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_classic;
			}
			else if( EFFECT_TYPE_KEY_LAMINATED.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_laminated;
			}
			else if( EFFECT_TYPE_KEY_INNER_CUBE.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_inner_cube;
			}
			else if( EFFECT_TYPE_KEY_CUBE.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_cube;
			}
			else if( EFFECT_TYPE_KEY_SECTOR.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_sector;
			}
			else if( EFFECT_TYPE_KEY_ROLLOVER.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_rollover;
			}
			else if( EFFECT_TYPE_KEY_WINDMILL.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_windmill;
			}
			else if( EFFECT_TYPE_KEY_WHEEL.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_wheel;
			}
			else if( EFFECT_TYPE_KEY_FOLD_UNFOLD.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_fold_unfold;
			}
			else if( EFFECT_TYPE_KEY_SNAKE.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_snake;
			}
			else if( EFFECT_TYPE_KEY_CHORD.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_chord;
			}
			else if( EFFECT_TYPE_KEY_RANDOM.equals( mEffectTypeKey ) )
			{
				mEffectTitleId = R.string.effect_random;
			}
			else
			{
				throw new IllegalStateException( StringUtils.concat( TAG , "unkown mEffectTypeKey:" , mEffectTypeKey ) );
			}
			mEffectEntiesList.add( LauncherDefaultConfig.getString( mEffectTitleId ) );
		}
		return mEffectEntiesList;
	}
	//chenliang add end
}
