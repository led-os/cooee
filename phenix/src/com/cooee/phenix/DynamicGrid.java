package com.cooee.phenix;


import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


class DeviceProfileQuery
{
	
	float widthDps;
	float heightDps;
	float value;
	PointF dimens;
	
	DeviceProfileQuery(
			float w ,
			float h ,
			float v )
	{
		widthDps = w;
		heightDps = h;
		value = v;
		dimens = new PointF( w , h );
	}
}

public class DynamicGrid
{
	
	@SuppressWarnings( "unused" )
	private static final String TAG = "DynamicGrid";
	private DeviceProfile mProfile;
	private float mMinWidth;
	private float mMinHeight;
	
	public static float dpiFromPx(
			int size ,
			DisplayMetrics metrics )
	{
		float densityRatio = (float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
		return( size / densityRatio );
	}
	
	public static int pxFromDp(
			float size ,
			DisplayMetrics metrics )
	{
		return (int)Math.round( TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP , size , metrics ) );
	}
	
	public static int pxFromSp(
			float size ,
			DisplayMetrics metrics )
	{
		return (int)Math.round( TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_SP , size , metrics ) );
	}
	
	public DynamicGrid(
			Context context ,
			Resources resources ,
			int minWidthPx ,
			int minHeightPx ,
			int widthPx ,
			int heightPx ,
			int awPx ,
			int ahPx )
	{
		DisplayMetrics dm = resources.getDisplayMetrics();
		ArrayList<DeviceProfile> deviceProfiles = new ArrayList<DeviceProfile>();
		boolean hasAA = ( ( LauncherDefaultConfig.CONFIG_LAUNCHER_STYLE == LauncherDefaultConfig.LAUNCHER_STYLE_DRAWER ) || LauncherDefaultConfig.ENABLE_HOTSEAT_FUNCTION_BUTTON );
		// Our phone profiles include the bar sizes in each orientation
		deviceProfiles.add( new DeviceProfile( "Super Short Stubby" , 255 , 300 , 2 , 3 , 48 , 13 , ( hasAA ? 5 : 4 ) , 48 ) );
		deviceProfiles.add( new DeviceProfile( "Shorter Stubby" , 255 , 400 , 3 , 3 , 48 , 13 , ( hasAA ? 5 : 4 ) , 48 ) );
		deviceProfiles.add( new DeviceProfile( "Short Stubby" , 275 , 420 , 3 , 4 , 48 , 13 , ( hasAA ? 5 : 4 ) , 48 ) );
		deviceProfiles.add( new DeviceProfile( "Stubby" , 255 , 450 , 3 , 4 , 48 , 13 , ( hasAA ? 5 : 4 ) , 48 ) );
		deviceProfiles.add( new DeviceProfile( "Nexus S" , 296 , 491.33f , 4 , 4 , 48 , 13 , ( hasAA ? 5 : 4 ) , 48 ) );
		deviceProfiles.add( new DeviceProfile( "Nexus 4" , 359 , 518 , 4 , 4 , 60 , 13 , ( hasAA ? 5 : 4 ) , 56 ) );
		// The tablet profile is odd in that the landscape orientation
		// also includes the nav bar on the side
		deviceProfiles.add( new DeviceProfile( "Nexus 7" , 575 , 904 , 6 , 6 , 72 , 14.4f , 7 , 60 ) );
		// Larger tablet profiles always have system bars on the top & bottom
		deviceProfiles.add( new DeviceProfile( "Nexus 10" , 727 , 1207 , 5 , 8 , 80 , 14.4f , 9 , 64 ) );
		/*
		deviceProfiles.add(new DeviceProfile("Nexus 7",
		        600, 960,  5, 5,  72, 14.4f,  5, 60));
		deviceProfiles.add(new DeviceProfile("Nexus 10",
		        800, 1280,  5, 5,  80, 14.4f, (hasAA ? 7 : 6), 64));
		 */
		deviceProfiles.add( new DeviceProfile( "20-inch Tablet" , 1527 , 2527 , 7 , 7 , 100 , 20 , 7 , 72 ) );
		mMinWidth = dpiFromPx( minWidthPx , dm );
		mMinHeight = dpiFromPx( minHeightPx , dm );
		mProfile = new DeviceProfile( context , deviceProfiles , mMinWidth , mMinHeight , widthPx , heightPx , awPx , ahPx , resources );
	}
	
	public DeviceProfile getDeviceProfile()
	{
		return mProfile;
	}
	
	public String toString()
	{
		return StringUtils.concat(
				"-------- DYNAMIC GRID ------- \n" ,
				"Wd(MinWidthDps):" ,
				mProfile.getMinWidthDps() ,
				"-Hd(MinWidthDps):" ,
				mProfile.getMinHeightDps() ,
				"-W(WidthPx):" ,
				mProfile.getWidthPx() ,
				"-H(HeightPx):" ,
				mProfile.getHeightPx() ,
				"-[r(NumRows):" ,
				mProfile.getNumRows() ,
				"-c(NumColumns):" ,
				mProfile.getNumColumns() ,
				"-iws(IconWidthSizePx):" ,
				mProfile.getIconWidthSizePx() ,
				"-ihs(IconHeightSizePx):" ,
				mProfile.getIconHeightSizePx() ,
				"-its(IconTextSize):" ,
				mProfile.getIconTextSize() ,
				"-cw(CellWidthPx):" ,
				mProfile.getCellWidthPx() ,
				"-ch(CellHeightPx):" ,
				mProfile.getCellHeightPx() ,
				"-hc(NumHotseatIcons):" ,
				mProfile.getNumHotseatIcons() ,
				"]" );
	}
}
