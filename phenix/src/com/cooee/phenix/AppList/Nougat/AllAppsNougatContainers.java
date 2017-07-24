package com.cooee.phenix.AppList.Nougat;



import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.cooee.phenix.R;
import com.cooee.phenix.Utilities;
import com.cooee.phenix.AppList.Marshmallow.AllAppsContainerView;
import com.cooee.phenix.AppList.Marshmallow.AllAppsGridAdapter;
import com.cooee.phenix.AppList.Marshmallow.AllAppsGridAdapter.onLayoutListener;
import com.cooee.phenix.AppList.Marshmallow.AlphabeticalAppsList;
import com.cooee.phenix.AppList.Marshmallow.AlphabeticalAppsList.AdapterItem;
import com.cooee.phenix.AppList.Marshmallow.AlphabeticalAppsList.MergeAlgorithm;
import com.cooee.phenix.AppList.Nougat.AllAppsNougatSections.OnTouchingLetterChangedListener;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;
import com.cooee.pinyin4j.util.PinYinUtils;


public class AllAppsNougatContainers extends AllAppsContainerView
{
	
	private AllAppsNougatSections mLettersSection;
	
	public AllAppsNougatContainers(
			Context context ,
			AttributeSet attrs )
	{
		this( context , attrs , 0 );
		// TODO Auto-generated constructor stub
	}
	
	public AllAppsNougatContainers(
			Context context ,
			AttributeSet attrs ,
			int defStyleAttr )
	{
		super( context , attrs , defStyleAttr );
		// TODO Auto-generated constructor stub
		mSectionNamesMargin = LauncherDefaultConfig.getDimensionPixelSize( R.dimen.all_apps_nougat_grid_view_start_margin );
		PinYinUtils.setAssetManager( context.getAssets() );
	}
	
	@Override
	protected void onFinishInflate()
	{
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mLettersSection = (AllAppsNougatSections)findViewById( R.id.qs_app_letter );
		mLettersSection.setSections( mApps.getSections() );
		mLettersSection.setOnTouchingLetterChangedListener( new OnTouchingLetterChangedListener() {
			
			@Override
			public void onTouchingLetterChanged(
					int position )
			{
				// TODO Auto-generated method stub
				mAppsRecyclerView.scrollToPositionAtProgress( position * 1.0f / mApps.getSections().size() );
			}
		} );
		mAdapter.setOnlayoutListener( new onLayoutListener() {
			
			@Override
			public void onLayoutCompeleted()
			{
				// TODO Auto-generated method stub
				notifySectionChanged();

			}
		} );
		mAppsRecyclerView.setOnScrollListener( new RecyclerView.OnScrollListener() {
			
			@Override
			public void onScrolled(
					RecyclerView recyclerView ,
					int dx ,
					int dy )
			{
				notifySectionChanged();
			}
		} );
	}
	
	private void notifySectionChanged()
	{
		int firstVisibleItem = ( (GridLayoutManager)mLayoutManager ).findFirstVisibleItemPosition();
		int lastVisibleItem = ( (GridLayoutManager)mLayoutManager ).findLastVisibleItemPosition();
		if( firstVisibleItem >= 0 && lastVisibleItem >= 0 )
		{
			AdapterItem firstItem = mApps.getAdapterItems().get( firstVisibleItem );
			AdapterItem lastItem = mApps.getAdapterItems().get( lastVisibleItem );
			if( mLettersSection != null )
			{
				mLettersSection.setVisiblityIndex( firstItem.sectionName , lastItem.sectionName );
				mLettersSection.invalidate();
			}
		}
	}
	@Override
	public MergeAlgorithm getMergeAlgorithm()
	{
		// TODO Auto-generated method stub
		AlphabeticalAppsList.MergeAlgorithm mergeAlgorithm = new SimpleSectionMergeAlgorithm(
				1 ,
				MIN_ROWS_IN_MERGED_SECTION_PHONE ,
				MAX_NUM_MERGES_PHONE );
		return mergeAlgorithm;
	}
	@Override
	public AllAppsGridAdapter getGridAdapter()
	{
		// TODO Auto-generated method stub
		return new AllAppsNougatGridAdapter( mLauncher , mApps , this , this , this );
	}
	
	@Override
	protected void onUpdateBackgroundAndPaddings(
			Rect searchBarBounds ,
			Rect padding )
	{
		// TODO Auto-generated method stub
		boolean isRtl = Utilities.isRtl( getResources() );
		// TODO: Use quantum_panel instead of quantum_panel_shape
		ColorDrawable background = new ColorDrawable( getContext().getResources().getColor( R.color.all_apps_nougat_background_color ) );
		Rect bgPadding = new Rect();
		background.getPadding( bgPadding );
		setBackgroundDrawable( background );
		mRevealView.setBackgroundDrawable( background.getConstantState().newDrawable() );
		// gaominghui@2016/12/14 ADD END 兼容android4.0
		mContent.setPadding( 0 , padding.top , 0 , padding.bottom );
		mContainerView.setPadding( 0 , 0 , 0 , 0 );
		mContainerView.setVisibility( View.VISIBLE );
		int startInset = Math.max( mSectionNamesMargin , mAppsRecyclerView.getMaxScrollbarWidth() );
		if( isRtl )
		{
			mAppsRecyclerView.setPadding( 0 , 0 , padding.right + startInset , 0 );
		}
		else
		{
			mAppsRecyclerView.setPadding( padding.left + startInset , 0 , 0 , 0 );
		}
		mAppsRecyclerView.updateBackgroundPadding( bgPadding );
		bgPadding.left = padding.left + startInset;
		mAdapter.updateBackgroundPadding( bgPadding );
		//		 Inset the search bar to fit its bounds above the container
		onUpdateSearchBarBackgroundAndPaddings( searchBarBounds );
	}

	final class SimpleSectionMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm
	{
		private int mMinAppsPerRow;
		private int mMinRowsInMergedSection;
		private int mMaxAllowableMerges;
		private CharsetEncoder mAsciiEncoder;
		
		public SimpleSectionMergeAlgorithm(
				int minAppsPerRow ,
				int minRowsInMergedSection ,
				int maxNumMerges )
		{
			mMinAppsPerRow = minAppsPerRow;
			mMinRowsInMergedSection = minRowsInMergedSection;
			mMaxAllowableMerges = maxNumMerges;
			mAsciiEncoder = Charset.forName( "US-ASCII" ).newEncoder();
		}
		
		@Override
		public boolean continueMerging(
				AlphabeticalAppsList.SectionInfo section ,
				AlphabeticalAppsList.SectionInfo withSection ,
				int sectionAppCount ,
				int numAppsPerRow ,
				int mergeCount )
		{
			// Don't merge the predicted apps
			if( section.firstAppItem.viewType != AllAppsGridAdapter.ICON_VIEW_TYPE )
			{
				return false;
			}
			// Continue merging if the number of hanging apps on the final row is less than some
			// fixed number (ragged), the merged rows has yet to exceed some minimum row count,
			// and while the number of merged sections is less than some fixed number of merges
			int rows = sectionAppCount / numAppsPerRow;
			int cols = sectionAppCount % numAppsPerRow;
			// Ensure that we do not merge across scripts, currently we only allow for english and
			// native scripts so we can test if both can just be ascii encoded
			boolean isCrossScript = false;
			if( section.firstAppItem != null && withSection.firstAppItem != null )
			{
				isCrossScript = mAsciiEncoder.canEncode( section.firstAppItem.sectionName ) != mAsciiEncoder.canEncode( withSection.firstAppItem.sectionName );
			}
			return ( 0 < cols && cols < mMinAppsPerRow ) && rows < mMinRowsInMergedSection && mergeCount < mMaxAllowableMerges && !isCrossScript;
		}
	}
	
	
}
