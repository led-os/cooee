package com.cooee.phenix.AppList.Nougat;


import java.util.HashMap;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.cooee.phenix.Launcher;
import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.AppList.Marshmallow.AllAppsGridAdapter;
import com.cooee.phenix.AppList.Marshmallow.AlphabeticalAppsList;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.util.Tools;


/**
 * The grid view adapter of all the apps.
 */
public class AllAppsNougatGridAdapter extends AllAppsGridAdapter
{

	
	private Paint mSectionBackgroundPaint;
	private int mRadius;
	private int mSpace;


	public AllAppsNougatGridAdapter(
			Launcher launcher ,
			AlphabeticalAppsList apps ,
			OnTouchListener touchListener ,
			OnClickListener iconClickListener ,
			OnLongClickListener iconLongClickListener )
	{
		super( launcher , apps , touchListener , iconClickListener , iconLongClickListener );
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initAboutDecoration(
			Resources res )
	{
		// TODO Auto-generated method stub
		mSpace = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_nougat_item_y_padding );
		mRadius = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_nougat_section_radius );
		mItemDecoration = new GridNougatItemDecoration( res , mSpace );
		mSectionBackgroundPaint = new Paint();
		mSectionBackgroundPaint.setAntiAlias( true );
		mSectionBackgroundPaint.setColor( res.getColor( R.color.all_apps_nougat_grid_section_background_color ) );
		mSectionTextPaint = new Paint();
		mSectionTextPaint.setTextSize( LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_nougat_grid_section_size ) );
		mSectionTextPaint.setColor( res.getColor( R.color.all_apps_nougat_grid_section_text_color ) );
		mSectionTextPaint.setAntiAlias( true );
		mSectionNamesMargin = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_nougat_section_x_margin );
		mSectionHeaderOffset = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_nougat_section_margin_top );
		mPredictedAppsDividerPaint = new Paint();
		mPredictedAppsDividerPaint.setStrokeWidth( Utilities.pxFromDp( 1f , res.getDisplayMetrics() ) );
		mPredictedAppsDividerPaint.setColor( res.getColor( R.color.all_apps_nougat_section_divider_color ) );
		mPredictedAppsDividerPaint.setAntiAlias( true );
		mPredictionBarDividerOffset = ( -LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_prediction_icon_bottom_padding ) + LauncherDefaultConfig
				.getDimensionPixelSize( R.dimen.all_apps_icon_top_bottom_padding ) ) / 2;

	}
	
	@Override
	public ViewHolder onCreateViewHolder(
			ViewGroup parent ,
			int viewType )
	{
		ViewHolder holder = super.onCreateViewHolder( parent , viewType );
		switch( viewType )
		{

			case ICON_VIEW_TYPE:
			case PREDICTION_ICON_VIEW_TYPE:
			{
				holder.mContent.setPadding( holder.mContent.getPaddingLeft() , holder.mContent.getPaddingTop() , holder.mContent.getPaddingRight() , holder.mContent.getPaddingBottom() + mSpace );
				break;
			}
			default:
				break;
		}
		return holder;
	}
	public class GridNougatItemDecoration extends RecyclerView.ItemDecoration
	{
		
		private static final boolean FADE_OUT_SECTIONS = false;
		private HashMap<String , PointF> mCachedSectionBounds = new HashMap<String , PointF>();
		private Rect mTmpBounds = new Rect();
		private int mSpace = 0;
		private Bitmap favoritesSection = null;
		
		public GridNougatItemDecoration(
				Resources res ,
				int space )
		{
			mSpace = space;
			if( LauncherDefaultConfig.SWITCH_ENABLE_NOUGAT_MAINMENU_FAVORITES_APPS )
				favoritesSection = Tools.drawableToBitmap( res.getDrawable( R.drawable.applist_nougat_favorites_section ) , mRadius * 2 , mRadius * 2 );
		}

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
			List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();
			boolean showSectionNames = true;
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
				if( showSectionNames && shouldDrawItemSection( holder , i , items ) )
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
						if( sectionName.equals( FAVORITES_TAG ) )
						{
							sectionName = "#";
						}
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
						//						x += (int)( ( mSectionNamesMargin + sectionBounds.x ) );
						if( !mIsRtl )
							x += (int)( ( -mSectionNamesMargin - sectionBounds.x ) );
						else
							x += (int)( ( mSectionNamesMargin + sectionBounds.x ) );
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
						if( nextItem.sectionName.equals( FAVORITES_TAG ) && favoritesSection != null )
						{
							c.drawBitmap( favoritesSection , x - sectionBounds.x / 2 , y - sectionBounds.y / 2 - mRadius , mSectionTextPaint );
						}
						else
						{
							c.drawCircle( x + sectionBounds.x / 2 , y - sectionBounds.y / 2 , mRadius , mSectionBackgroundPaint );
							c.drawText( sectionName , x , y , mSectionTextPaint );
						}
						float divider = y - sectionBounds.y - viewTopOffset;
						if( showDrawItemDivider( holder , i , items ) )
						{
							if( !mIsRtl )
								c.drawLine( mBackgroundPadding.left , divider , parent.getWidth() - mBackgroundPadding.right , divider , mPredictedAppsDividerPaint );
							else
								c.drawLine( mBackgroundPadding.right , divider , parent.getWidth() - mBackgroundPadding.left , divider , mPredictedAppsDividerPaint );
							
						}
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
		
		private boolean showDrawItemDivider(
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
			if( LauncherDefaultConfig.SWITCH_ENABLE_NOUGAT_MAINMENU_FAVORITES_APPS )
			{
				if( item.sectionName.equals( FAVORITES_TAG ) )
				{
					return false;
				}
			}
			else
			{
				if( item.sectionName.equals( mApps.getSections().get( 0 ).firstAppItem.sectionName ) )
				{
					return false;
				}
			}
			return ( childIndex == 0 ) || ( items.get( pos - 1 ).viewType == AllAppsGridAdapter.SECTION_BREAK_VIEW_TYPE );
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

}
