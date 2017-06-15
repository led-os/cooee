package com.coco.theme.themebox;


import android.widget.TabHost;

import com.coco.theme.themebox.PullToRefreshView.OnFooterRefreshListener;
import com.coco.theme.themebox.PullToRefreshView.OnHeaderRefreshListener;


public interface ContentFactory extends TabHost.TabContentFactory , OnHeaderRefreshListener , OnFooterRefreshListener
{
	
	void onDestroy();
	
	void changeTab(
			int tab );
	
	void reloadView();
}
