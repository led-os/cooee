package com.iLoong.launcher.MList;


import java.util.HashMap;
import java.util.Map;

import android.content.Context;


public class MeApkDlMgrBuilder
{
	
	static private Map<String , MeApkDownloadManager> MeApkDownloadManagerMap = null;
	
	public static MeApkDownloadManager Build(
			Context context ,
			String apkmoudleName ,
			int entryId )
	{
		String CurManagerKey = "MeDlMgr" + entryId;
		if( null == MeApkDownloadManagerMap )
		{
			MeApkDownloadManagerMap = new HashMap<String , MeApkDownloadManager>();
		}
		MeApkDownloadManager CurManager = MeApkDownloadManagerMap.get( CurManagerKey );
		if( null == CurManager )
		{
			CurManager = new MeApkDownloadManager( context , apkmoudleName , entryId );
			MeApkDownloadManagerMap.put( CurManagerKey , CurManager );
		}
		return CurManager;
	}
	
	public static MeApkDownloadManager GetMeApkDownloadManager(
			int entryId )
	{
		String CurManagerKey = "MeDlMgr" + entryId;
		if( null == MeApkDownloadManagerMap )
		{
			return null;
		}
		MeApkDownloadManager CurManager = MeApkDownloadManagerMap.get( CurManagerKey );
		return CurManager;
	}
	
	public static Map<String , MeApkDownloadManager> GetAllMeApkDownloadManager()
	{
		return MeApkDownloadManagerMap;
	}
}
