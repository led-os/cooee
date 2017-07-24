package com.cooee.phenix.AppList.Marshmallow;


import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cooee.phenix.BubbleTextView;
import com.cooee.phenix.Launcher;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.phenix.data.AppInfo;
import com.cooee.phenix.util.Thunk;


/**
 * The grid view adapter of all the apps.
 */
public class AllAppsGridAdapter extends RecyclerView.Adapter<AllAppsGridAdapter.ViewHolder>
{
	
	public static final String TAG = "AppsGridAdapter";
	private static final boolean DEBUG = false;
	// A section break in the grid
	public static final int SECTION_BREAK_VIEW_TYPE = 0;
	// A normal icon
	public static final int ICON_VIEW_TYPE = 1;
	// A prediction icon
	public static final int PREDICTION_ICON_VIEW_TYPE = 2;
	// The message shown when there are no filtered results
	public static final int EMPTY_SEARCH_VIEW_TYPE = 3;
	// A divider that separates the apps list and the search market button
	public static final int SEARCH_MARKET_DIVIDER_VIEW_TYPE = 4;
	// The message to continue to a market search when there are no filtered results
	public static final int SEARCH_MARKET_VIEW_TYPE = 5;
	String mEmptySearchStr = null;
	String mMarketSearchStr = null;
	private onLayoutListener mListener;//zhujieping add  //拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。
	public static final String FAVORITES_TAG = "@";
	
	/**
	 * ViewHolder for each icon.
	 */
	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		
		public View mContent;
		
		public ViewHolder(
				View v )
		{
			super( v );
			mContent = v;
		}
	}
	
	/**
	 * A subclass of GridLayoutManager that overrides accessibility values during app search.
	 */
	public class AppsGridLayoutManager extends GridLayoutManager
	{
		
		public AppsGridLayoutManager(
				Context context )
		{
			super( context , 1 , GridLayoutManager.VERTICAL , false );
		}
		
		@Override
		public int getRowCountForAccessibility(
				RecyclerView.Recycler recycler ,
				RecyclerView.State state )
		{
			if( mApps.hasNoFilteredResults() )
			{
				// Disregard the no-search-results text as a list item for accessibility
				return 0;
			}
			else
			{
				return super.getRowCountForAccessibility( recycler , state );
			}
		}
		
		//zhujieping add start //拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。
		@Override
		public void onLayoutChildren(
				Recycler recycler ,
				State state )
		{
			// TODO Auto-generated method stub
			super.onLayoutChildren( recycler , state );
			if( mListener != null )
			{
				mListener.onLayoutCompeleted();
			}
		}
		//zhujieping add end
		
	}
	
	/**
	 * Helper class to size the grid items.
	 */
	public class GridSpanSizer extends GridLayoutManager.SpanSizeLookup
	{
		
		public GridSpanSizer()
		{
			super();
			setSpanIndexCacheEnabled( true );
		}
		
		@Override
		public int getSpanSize(
				int position )
		{
			switch( mApps.getAdapterItems().get( position ).viewType )
			{
				case AllAppsGridAdapter.ICON_VIEW_TYPE:
				case AllAppsGridAdapter.PREDICTION_ICON_VIEW_TYPE:
					return 1;
				default:
					// Section breaks span the full width
					return mAppsPerRow;
			}
		}
	}
	
	/**
	 * Helper class to draw the section headers
	 */
	public class GridItemDecoration extends RecyclerView.ItemDecoration
	{
		
		private static final boolean DEBUG_SECTION_MARGIN = false;
		private static final boolean FADE_OUT_SECTIONS = false;
		private HashMap<String , PointF> mCachedSectionBounds = new HashMap<String , PointF>();
		private Rect mTmpBounds = new Rect();
		
		@Override
		public void onDraw(
				Canvas c ,
				RecyclerView parent ,
				RecyclerView.State state )
		{
			if( mApps.hasFilter() || mAppsPerRow == 0 )
			{
				return;
			}
			if( DEBUG_SECTION_MARGIN )
			{
				Paint p = new Paint();
				p.setColor( 0x33ff0000 );
				c.drawRect( mBackgroundPadding.left , 0 , mBackgroundPadding.left + mSectionNamesMargin , parent.getMeasuredHeight() , p );
			}
			List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();
			boolean hasDrawnPredictedAppsDivider = false;
			boolean showSectionNames = mSectionNamesMargin > 0;
			int childCount = parent.getChildCount();
			int lastSectionTop = 0;
			int lastSectionHeight = 0;
			for( int i = 0 ; i < childCount ; i++ )
			{
				View child = parent.getChildAt( i );
				ViewHolder holder = (ViewHolder)parent.getChildViewHolder( child );
				if( !isValidHolderAndChild( holder , child , items ) )
				{
					continue;
				}
				if( shouldDrawItemDivider( holder , items ) && !hasDrawnPredictedAppsDivider )
				{
					// Draw the divider under the predicted apps
					//zhujieping add start //常用应用最上面也显示横线
					int top = child.getTop() + child.getHeight() + mPredictionBarDividerOffset;
					int parentTop = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_list_top_bottom_padding );
					c.drawLine(
							mBackgroundPadding.left ,
							child.getTop() - parentTop + mPredictedAppsDividerPaint.getStrokeWidth() ,
							parent.getWidth() - mBackgroundPadding.right ,
							child.getTop() - parentTop + mPredictedAppsDividerPaint.getStrokeWidth() ,
							mPredictedAppsDividerPaint );
					//zhujieping add end
					c.drawLine( mBackgroundPadding.left , top , parent.getWidth() - mBackgroundPadding.right , top , mPredictedAppsDividerPaint );
					hasDrawnPredictedAppsDivider = true;
				}
				else if( showSectionNames && shouldDrawItemSection( holder , i , items ) )
				{
					// At this point, we only draw sections for each section break;
					int viewTopOffset = ( 2 * child.getPaddingTop() );
					int pos = holder.getPosition();
					AlphabeticalAppsList.AdapterItem item = items.get( pos );
					AlphabeticalAppsList.SectionInfo sectionInfo = item.sectionInfo;
					// Draw all the sections for this index
					String lastSectionName = item.sectionName;
					for( int j = item.sectionAppIndex ; j < sectionInfo.numApps ; j++ , pos++ )
					{
						AlphabeticalAppsList.AdapterItem nextItem = items.get( pos );
						String sectionName = nextItem.sectionName;
						if( nextItem.sectionInfo != sectionInfo )
						{
							break;
						}
						if( j > item.sectionAppIndex && sectionName.equals( lastSectionName ) )
						{
							continue;
						}
						// Find the section name bounds
						PointF sectionBounds = getAndCacheSectionBounds( sectionName );
						// Calculate where to draw the section
						int sectionBaseline = (int)( viewTopOffset + sectionBounds.y );
						int x = mIsRtl ? parent.getWidth() - mBackgroundPadding.left - mSectionNamesMargin : mBackgroundPadding.left;
						x += (int)( ( mSectionNamesMargin - sectionBounds.x ) / 2f );
						int y = child.getTop() + sectionBaseline;
						// Determine whether this is the last row with apps in that section, if
						// so, then fix the section to the row allowing it to scroll past the
						// baseline, otherwise, bound it to the baseline so it's in the viewport
						int appIndexInSection = items.get( pos ).sectionAppIndex;
						int nextRowPos = Math.min( items.size() - 1 , pos + mAppsPerRow - ( appIndexInSection % mAppsPerRow ) );
						AlphabeticalAppsList.AdapterItem nextRowItem = items.get( nextRowPos );
						boolean fixedToRow = !sectionName.equals( nextRowItem.sectionName );
						if( !fixedToRow )
						{
							y = Math.max( sectionBaseline , y );
						}
						// In addition, if it overlaps with the last section that was drawn, then
						// offset it so that it does not overlap
						if( lastSectionHeight > 0 && y <= ( lastSectionTop + lastSectionHeight ) )
						{
							y += lastSectionTop - y + lastSectionHeight;
						}
						// Draw the section header
						if( FADE_OUT_SECTIONS )
						{
							int alpha = 255;
							if( fixedToRow )
							{
								alpha = Math.min( 255 , (int)( 255 * ( Math.max( 0 , y ) / (float)sectionBaseline ) ) );
							}
							mSectionTextPaint.setAlpha( alpha );
						}
						c.drawText( sectionName , x , y , mSectionTextPaint );
						lastSectionTop = y;
						lastSectionHeight = (int)( sectionBounds.y + mSectionHeaderOffset );
						lastSectionName = sectionName;
					}
					i += ( sectionInfo.numApps - item.sectionAppIndex );
				}
			}
		}
		
		@Override
		public void getItemOffsets(
				Rect outRect ,
				View view ,
				RecyclerView parent ,
				RecyclerView.State state )
		{
			// Do nothing
		}
		
		/**
		 * Given a section name, return the bounds of the given section name.
		 */
		private PointF getAndCacheSectionBounds(
				String sectionName )
		{
			PointF bounds = mCachedSectionBounds.get( sectionName );
			if( bounds == null )
			{
				mSectionTextPaint.getTextBounds( sectionName , 0 , sectionName.length() , mTmpBounds );
				bounds = new PointF( mSectionTextPaint.measureText( sectionName ) , mTmpBounds.height() );
				mCachedSectionBounds.put( sectionName , bounds );
			}
			return bounds;
		}
		
		/**
		 * Returns whether we consider this a valid view holder for us to draw a divider or section for.
		 */
		private boolean isValidHolderAndChild(
				ViewHolder holder ,
				View child ,
				List<AlphabeticalAppsList.AdapterItem> items )
		{
			// Ensure item is not already removed
			GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams)child.getLayoutParams();
			if( lp.isItemRemoved() )
			{
				return false;
			}
			// Ensure we have a valid holder
			if( holder == null )
			{
				return false;
			}
			// Ensure we have a holder position
			int pos = holder.getPosition();
			if( pos < 0 || pos >= items.size() )
			{
				return false;
			}
			return true;
		}
		
		/**
		 * Returns whether to draw the divider for a given child.
		 */
		private boolean shouldDrawItemDivider(
				ViewHolder holder ,
				List<AlphabeticalAppsList.AdapterItem> items )
		{
			int pos = holder.getPosition();
			boolean result = items.get( pos ).viewType == AllAppsGridAdapter.PREDICTION_ICON_VIEW_TYPE;
			//zhujieping add start //6.0主菜单显示常用应用并显示分隔线
			AlphabeticalAppsList.AdapterItem item = items.get( pos );
			if( item != null && FAVORITES_TAG.equals( item.sectionName ) )
			{
				result = true;
			}
			//zhujieping add end
			return result;
		}
		
		/**
		 * Returns whether to draw the section for the given child.
		 */
		private boolean shouldDrawItemSection(
				ViewHolder holder ,
				int childIndex ,
				List<AlphabeticalAppsList.AdapterItem> items )
		{
			int pos = holder.getPosition();
			AlphabeticalAppsList.AdapterItem item = items.get( pos );
			// Ensure it's an icon
			if( item.viewType != AllAppsGridAdapter.ICON_VIEW_TYPE )
			{
				return false;
			}
			// Draw the section header for the first item in each section
			return ( childIndex == 0 ) || ( items.get( pos - 1 ).viewType == AllAppsGridAdapter.SECTION_BREAK_VIEW_TYPE );
		}
	}
	
	private Launcher mLauncher;
	private LayoutInflater mLayoutInflater;
	@Thunk
	protected AlphabeticalAppsList mApps;
	protected GridLayoutManager mGridLayoutMgr;
	protected GridSpanSizer mGridSizer;
	protected RecyclerView.ItemDecoration mItemDecoration;
	private View.OnTouchListener mTouchListener;
	private View.OnClickListener mIconClickListener;
	private View.OnLongClickListener mIconLongClickListener;
	@Thunk
	protected final Rect mBackgroundPadding = new Rect();
	@Thunk
	protected int mPredictionBarDividerOffset;
	@Thunk
	protected int mAppsPerRow;
	@Thunk
	protected boolean mIsRtl;
	// The text to show when there are no search results and no market search handler.
	private String mEmptySearchMessage;
	// The name of the market app which handles searches, to be used in the format str
	// below when updating the search-market view.  Only needs to be loaded once.
	private String mMarketAppName;
	// The text to show when there is a market app which can handle a specific query, updated
	// each time the search query changes.
	private String mMarketSearchMessage;
	// The intent to send off to the market app, updated each time the search query changes.
	private Intent mMarketSearchIntent;
	// The last query that the user entered into the search field
	private String mLastSearchQuery;
	// Section drawing
	@Thunk
	protected int mSectionNamesMargin;
	@Thunk
	protected int mSectionHeaderOffset;
	@Thunk
	protected Paint mSectionTextPaint;
	@Thunk
	protected Paint mPredictedAppsDividerPaint;
	
	public AllAppsGridAdapter(
			Launcher launcher ,
			AlphabeticalAppsList apps ,
			View.OnTouchListener touchListener ,
			View.OnClickListener iconClickListener ,
			View.OnLongClickListener iconLongClickListener )
	{
		Resources res = launcher.getResources();
		mLauncher = launcher;
		mApps = apps;
		mEmptySearchMessage = LauncherDefaultConfig.getString( R.string.all_apps_loading_message );
		mGridSizer = new GridSpanSizer();
		mGridLayoutMgr = new AppsGridLayoutManager( launcher );
		mGridLayoutMgr.setSpanSizeLookup( mGridSizer );
		mLayoutInflater = LayoutInflater.from( launcher );
		mTouchListener = touchListener;
		mIconClickListener = iconClickListener;
		mIconLongClickListener = iconLongClickListener;
		initAboutDecoration( res );
		// Resolve the market app handling additional searches
		updateMarketAppName( launcher );
	}
	
	public void initAboutDecoration(
			Resources res )
	{

		mItemDecoration = new GridItemDecoration();
		mSectionTextPaint = new Paint();
		mSectionTextPaint.setTextSize( LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_nougat_section_size ) );
		mSectionTextPaint.setColor( res.getColor( R.color.all_apps_grid_section_text_color ) );
		mSectionTextPaint.setAntiAlias( true );
		mSectionNamesMargin = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_grid_view_start_margin );
		mSectionHeaderOffset = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_grid_section_y_offset );
		mPredictedAppsDividerPaint = new Paint();
		mPredictedAppsDividerPaint.setStrokeWidth( Utilities.pxFromDp( 1f , res.getDisplayMetrics() ) );
		mPredictedAppsDividerPaint.setColor( res.getColor( R.color.all_apps_favorites_divider_color ) );
		mPredictedAppsDividerPaint.setAntiAlias( true );
		mPredictionBarDividerOffset = ( -LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_prediction_icon_bottom_padding ) + LauncherDefaultConfig
				.getDimensionPixelSize( R.dimen.all_apps_icon_top_bottom_padding ) ) / 2;
	}
	/**
	 * Sets the number of apps per row.
	 */
	public void setNumAppsPerRow(
			int appsPerRow )
	{
		mAppsPerRow = appsPerRow;
		mGridLayoutMgr.setSpanCount( appsPerRow );
	}
	
	/**
	 * Sets whether we are in RTL mode.
	 */
	public void setRtl(
			boolean rtl )
	{
		mIsRtl = rtl;
	}
	
	/**
	 * Sets the last search query that was made, used to show when there are no results and to also
	 * seed the intent for searching the market.
	 */
	public void setLastSearchQuery(
			String query )
	{
		mLastSearchQuery = query;
		if( mEmptySearchStr == null )
		{
			mEmptySearchStr = LauncherDefaultConfig.getString( R.string.all_apps_no_search_results );
		}
		mEmptySearchMessage = String.format( mEmptySearchStr , query );
		if( mMarketAppName != null )
		{
			if( mMarketSearchStr == null )
			{
				mMarketSearchStr = LauncherDefaultConfig.getString( R.string.all_apps_search_market_message );
			}
			mMarketSearchMessage = String.format( mMarketSearchStr , mMarketAppName );
			mMarketSearchIntent = createMarketSearchIntent( query );
		}
		else
		{
			if( mMarketSearchIntent != null && mMarketSearchIntent.getData() != null )
			{
				mMarketSearchIntent.setData( null );
			}
		}
	}
	
	/**
	 * Notifies the adapter of the background padding so that it can draw things correctly in the
	 * item decorator.
	 */
	public void updateBackgroundPadding(
			Rect padding )
	{
		mBackgroundPadding.set( padding );
	}
	
	/**
	 * Returns the grid layout manager.
	 */
	public GridLayoutManager getLayoutManager()
	{
		return mGridLayoutMgr;
	}
	
	/**
	 * Returns the item decoration for the recycler view.
	 */
	public RecyclerView.ItemDecoration getItemDecoration()
	{
		// We don't draw any headers when we are uncomfortably dense
		return mItemDecoration;
	}
	
	@Override
	public ViewHolder onCreateViewHolder(
			ViewGroup parent ,
			int viewType )
	{
		switch( viewType )
		{
			case SECTION_BREAK_VIEW_TYPE:
				return new ViewHolder( new View( parent.getContext() ) );
			case ICON_VIEW_TYPE:
			{
				BubbleTextView icon = (BubbleTextView)mLayoutInflater.inflate( R.layout.all_apps_icon , parent , false );
				icon.setOnTouchListener( mTouchListener );
				icon.setOnClickListener( mIconClickListener );
				icon.setOnLongClickListener( mIconLongClickListener );
				icon.setTextColor( parent.getContext().getResources().getColor( R.color.quantum_panel_text_color ) );
				// zhangjin@2016/05/05 DEL START
				//icon.setLongPressTimeout( ViewConfiguration.get( parent.getContext() )
				//		.getLongPressTimeout() );
				// zhangjin@2016/05/05 DEL END
				icon.setFocusable( true );
				return new ViewHolder( icon );
			}
			case PREDICTION_ICON_VIEW_TYPE:
			{
				BubbleTextView icon = (BubbleTextView)mLayoutInflater.inflate( R.layout.all_apps_prediction_bar_icon , parent , false );
				icon.setOnTouchListener( mTouchListener );
				icon.setOnClickListener( mIconClickListener );
				icon.setOnLongClickListener( mIconLongClickListener );
				// zhangjin@2016/05/05 DEL START
				//icon.setLongPressTimeout( ViewConfiguration.get( parent.getContext() )
				//		.getLongPressTimeout() );
				// zhangjin@2016/05/05 DEL END
				icon.setFocusable( true );
				return new ViewHolder( icon );
			}
			case EMPTY_SEARCH_VIEW_TYPE:
				return new ViewHolder( mLayoutInflater.inflate( R.layout.all_apps_empty_search , parent , false ) );
			case SEARCH_MARKET_DIVIDER_VIEW_TYPE:
				return new ViewHolder( mLayoutInflater.inflate( R.layout.all_apps_search_market_divider , parent , false ) );
			case SEARCH_MARKET_VIEW_TYPE:
				View searchMarketView = mLayoutInflater.inflate( R.layout.all_apps_search_market , parent , false );
				if( LauncherDefaultConfig.SWITCH_ENABLE_MARSHMALLOW_MAINMENU_SEARCH )//xiatian add	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
				{
					searchMarketView.setOnClickListener( new View.OnClickListener() {
						
						@Override
						public void onClick(
								View v )
						{
							// zhangjin@2016/05/05 DEL START
							//mLauncher.startSearchFromAllApps( v , mMarketSearchIntent , mLastSearchQuery );
							// zhangjin@2016/05/05 DEL END
							//xiatian add start	//安卓6.0主菜单时，主菜单搜索栏是否使用安卓6.0主菜单搜索栏功能。true为使用安卓6.0主菜单搜索栏功能，false为使用酷搜。默认为false。
							if( mLauncher != null && mMarketSearchIntent != null )
							{
								mLauncher.startActivitySafely( v , mMarketSearchIntent , null );
							}
							//xiatian add end
						}
					} );
				}
				return new ViewHolder( searchMarketView );
			default:
				throw new RuntimeException( "Unexpected view type" );
		}
	}
	
	@Override
	public void onBindViewHolder(
			ViewHolder holder ,
			int position )
	{
		switch( holder.getItemViewType() )
		{
			case ICON_VIEW_TYPE:
			{
				AppInfo info = mApps.getAdapterItems().get( position ).appInfo;
				BubbleTextView icon = (BubbleTextView)holder.mContent;
				icon.applyFromApplicationInfo( info );
				break;
			}
			case PREDICTION_ICON_VIEW_TYPE:
			{
				AppInfo info = mApps.getAdapterItems().get( position ).appInfo;
				BubbleTextView icon = (BubbleTextView)holder.mContent;
				icon.applyFromApplicationInfo( info );
				break;
			}
			case EMPTY_SEARCH_VIEW_TYPE:
				TextView emptyViewText = (TextView)holder.mContent;
				emptyViewText.setText( mEmptySearchMessage );
				emptyViewText.setGravity( mApps.hasNoFilteredResults() ? Gravity.CENTER : Gravity.START | Gravity.CENTER_VERTICAL );
				break;
			case SEARCH_MARKET_VIEW_TYPE:
				TextView searchView = (TextView)holder.mContent;
				if( mMarketSearchIntent != null && mMarketSearchIntent.getData() != null )
				{
					searchView.setVisibility( View.VISIBLE );
					searchView.setGravity( mApps.hasNoFilteredResults() ? Gravity.CENTER : Gravity.START | Gravity.CENTER_VERTICAL );
					searchView.setText( mMarketSearchMessage );
				}
				else
				{
					searchView.setVisibility( View.GONE );
				}
				break;
		}
	}
	
	@Override
	public int getItemCount()
	{
		return mApps.getAdapterItems().size();
	}
	
	@Override
	public int getItemViewType(
			int position )
	{
		AlphabeticalAppsList.AdapterItem item = mApps.getAdapterItems().get( position );
		return item.viewType;
	}
	
	/**
	 * Creates a new market search intent.
	 */
	private Intent createMarketSearchIntent(
			String query )
	{
		if( mMarketSearchIntent == null )
		{
			mMarketSearchIntent = new Intent( Intent.ACTION_VIEW );
		}
		if( mMarketSearchIntent.getData() != null )
		{
			mMarketSearchIntent.setData( null );
		}
		Uri marketSearchUri = Uri.parse( "market://search" ).buildUpon().appendQueryParameter( "q" , query ).build();
		mMarketSearchIntent.setData( marketSearchUri );
		return mMarketSearchIntent;
	}
	
	private void updateMarketAppName(
			Launcher mLauncher )
	{
		if( mLauncher != null )
		{
			PackageManager pm = mLauncher.getPackageManager();
			ResolveInfo marketInfo = pm.resolveActivity( createMarketSearchIntent( "" ) , PackageManager.MATCH_DEFAULT_ONLY );
			if( marketInfo != null )
			{
				mMarketAppName = marketInfo.loadLabel( pm ).toString();
			}
			else
			{
				mMarketAppName = null;
			}
			notifyDataSetChanged();
		}
	}
	
	//xiatian add start	//解决“关于安卓6.0主菜单搜索栏进行搜索后，显示的“进入应用市场搜索”按钮。启动桌面后：（1）之前没有支持应用市场的apk前提下，安装支持应用市场的apk后，按钮不显示；（2）卸载唯一的支持应用市场的apk后，按钮不隐藏”的问题。
	public void notifyAppsChanged()
	{
		if( mLauncher != null )
		{
			updateMarketAppName( mLauncher );
		}
	}
	//xiatian add end
	//zhujieping add start //拓展配置项“config_applist_style”，添加可配置项2。2是7.0的主菜单样式。
	public void setOnlayoutListener(
			onLayoutListener mListener )
	{
		this.mListener = mListener;
	}
	public interface onLayoutListener
	{
		public void onLayoutCompeleted();
	}
	//zhujieping add end
}
