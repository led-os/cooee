package com.cooee.phenix.widget.timer;


// luomingjun add whole file //桌面时钟
/**
 * 时钟
 */


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RemoteViews;

import com.cooee.framework.utils.StringUtils;
import com.cooee.phenix.LauncherAppState;
import com.cooee.phenix.R;
import com.cooee.phenix.config.defaultConfig.LauncherDefaultConfig;


public class TimeUtil
{
	
	private Context context;
	private int i;
	public static final String BT_REFRESH_ACTION = "com.android.timer.BT_REFRESH_ACTION";
	private Date date = new Date();
	private List<String> lt = new ArrayList<String>();
	int[] ids = {
			R.drawable.widget_time_t0 ,
			R.drawable.widget_time_t1 ,
			R.drawable.widget_time_t2 ,
			R.drawable.widget_time_t3 ,
			R.drawable.widget_time_t4 ,
			R.drawable.widget_time_t5 ,
			R.drawable.widget_time_t6 ,
			R.drawable.widget_time_t7 ,
			R.drawable.widget_time_t8 ,
			R.drawable.widget_time_t9 };
			
	public TimeUtil(
			Context context )
	{
		this.context = context;
	}
	
	public TimeUtil(
			Context context ,
			int index )
	{
		this.context = context;
		this.i = index;
		if( lt.size() > 0 )
		{
			lt.removeAll( lt );
		}
	}
	
	public void updateTimeWidget()
	{
		RemoteViews remoteViews = null;
		Intent btIntent = new Intent().setAction( BT_REFRESH_ACTION );
		PendingIntent btPendingIntent = PendingIntent.getBroadcast( context , 0 , btIntent , PendingIntent.FLAG_UPDATE_CURRENT );
		//Log.i( "andy" , "language = "+language );
		// 获取系统当前日期
		i++;
		Calendar c = Calendar.getInstance();
		int month = c.get( Calendar.MONTH );
		int day = c.get( Calendar.DAY_OF_MONTH );
		int hour;
		if( DateFormat.is24HourFormat( context ) )
		{
			hour = c.get( Calendar.HOUR_OF_DAY );
		}
		else
		{
			hour = c.get( Calendar.HOUR );
			hour = hour == 0 ? 12 : hour;//yangmengchao add	//解决“12小时制时，时间为中午12点时显示为‘00’”的问题。【c_0004703】
		}
		int minute = c.get( Calendar.MINUTE );
		int w = c.get( Calendar.DAY_OF_WEEK ) - 1;
		GregorianCalendar ca = new GregorianCalendar();
		int ap = ca.get( GregorianCalendar.AM_PM );
		if( w < 0 )
		{
			w = 0;
		}
		String languageEnv = getLanguageEnv();
		String week = getResourcetime( w );
		String curdate = "";
		int year = c.get( Calendar.YEAR );//yangmengchao  add  //需求：迅虎增加桌面小部件时间日期格式样式。0为默认样式，1为迅虎样式。默认为0。
		if( languageEnv != null && ( languageEnv.trim().equals( "zh-CN" ) || languageEnv.trim().equals( "zh-TW" ) ) )
		{
			// time中的月份早一个月 所以这的月份要+1
			// zhangjin@2016/04/07 UPD START
			//curdate = ( month + 1 ) + "月" + day + "日" + "  " + week;
			if( LauncherDefaultConfig.HERUNXIN_BIG_LAUNCHER )
			{
				//yangmengchao add start //需求：迅虎增加桌面小部件时间日期格式样式。0为默认样式，1为迅虎样式。默认为0。
				if( LauncherDefaultConfig.CONFIG_TIMER_WIDGET_DATE_STYLE == 1 ) //迅虎桌面小部件时间日期格式样式
				{
					curdate = StringUtils.concat( year , "年" , ( month + 1 ) , "月" , day , "日" );
				}
				else //默认样式
						//yangmengchao add end
				{
					curdate = StringUtils.concat( ( month + 1 ) , "月" , day , "日 " , week );
				}
			}
			else
			{
				//yangmengchao add start //需求：迅虎增加桌面小部件时间日期格式样式。0为默认样式，1为迅虎样式。默认为0。
				if( LauncherDefaultConfig.CONFIG_TIMER_WIDGET_DATE_STYLE == 1 )
				{
					curdate = StringUtils.concat( year , "年" , ( month + 1 ) , "月" , day , "日" );
				}
				else
				//yangmengchao add end
				{
					curdate = StringUtils.concat( ( month + 1 ) , "月" , day , "日  " , week );
				}
			}
			// zhangjin@2016/04/07 UPD END
		}
		else
		{
			//yangmengchao add start //需求：迅虎增加桌面小部件时间日期格式样式。0为默认样式，1为迅虎样式。默认为0。
			if( LauncherDefaultConfig.CONFIG_TIMER_WIDGET_DATE_STYLE == 1 )
			{
				curdate = StringUtils.concat( day , "-" , ( month + 1 ) , "-" , year );
			}
			else
			//yangmengchao add end
			{
				curdate = StringUtils.concat( ( month + 1 ) , "/" , day , "  " , week );
			}
		}
		// 判断当前分钟是否大于10 是 就是当前分钟 否 就在前面加个0 11:07
		String curtime = "";
		if( minute < 10 )
		{
			if( hour < 10 )
			{
				curtime = StringUtils.concat( "0" , hour , ":0" , minute );
			}
			else
			{
				curtime = StringUtils.concat( hour , ":0" , minute );
			}
		}
		else
		{
			if( hour < 10 )
			{
				curtime = StringUtils.concat( "0" , hour , ":" , minute );
			}
			else
			{
				curtime = StringUtils.concat( hour , ":" , minute );
			}
		}
		List<String> s = getIndex( curtime );
		int mRemoteViewLayoutResourceId = R.layout.widget_time_layout_ltr;
		if( LauncherAppState.isLayoutRTL() )
		{
			mRemoteViewLayoutResourceId = R.layout.widget_time_layout_rtl;
		}
		remoteViews = new RemoteViews( context.getPackageName() , mRemoteViewLayoutResourceId );
		remoteViews.setOnClickPendingIntent( R.id.hour_ten , btPendingIntent );
		remoteViews.setOnClickPendingIntent( R.id.hour_dian , btPendingIntent );
		remoteViews.setOnClickPendingIntent( R.id.hour_one , btPendingIntent );
		remoteViews.setOnClickPendingIntent( R.id.minute_ten , btPendingIntent );
		remoteViews.setOnClickPendingIntent( R.id.minute_one , btPendingIntent );
		remoteViews.setImageViewResource( R.id.hour_ten , ids[Integer.parseInt( s.get( 0 ) )] );
		remoteViews.setImageViewResource( R.id.hour_one , ids[Integer.parseInt( s.get( 1 ) )] );
		if(
		//
		( !LauncherDefaultConfig.SWITCH_ENABLE_TIMER_WIDGET_SECOND_HAND_FLASH/* //lvjiangbin add 时钟插件中，小时和分钟之间的“两个点（秒针）”是否闪烁。true为闪烁；false为不闪烁。默认true。 */ )
		//
		|| i % 2 == 0
		//
		)
		{
			remoteViews.setViewVisibility( R.id.hour_dian , View.VISIBLE );
		}
		else
		{
			remoteViews.setViewVisibility( R.id.hour_dian , View.INVISIBLE );
		}
		remoteViews.setImageViewResource( R.id.minute_ten , ids[Integer.parseInt( s.get( 2 ) )] );
		remoteViews.setImageViewResource( R.id.minute_one , ids[Integer.parseInt( s.get( 3 ) )] );
		if( !DateFormat.is24HourFormat( context ) )
		{
			remoteViews.setViewVisibility( R.id.minute_am , View.VISIBLE );
			if( ap == 0 )
			{
				remoteViews.setImageViewResource( R.id.minute_am , R.drawable.widget_time_am );
			}
			else if( ap == 1 )
			{
				remoteViews.setImageViewResource( R.id.minute_am , R.drawable.widget_time_pm );
			}
		}
		else
		{
			remoteViews.setViewVisibility( R.id.minute_am , View.GONE );//不需要AM PM的时候就GONE掉
		}
		remoteViews.setTextViewText( R.id.widget_date , curdate );
		AppWidgetManager manager = AppWidgetManager.getInstance( context );
		manager.updateAppWidget( new ComponentName( context , TimeAppWidgetProvider.class ) , remoteViews );
	}
	
	public List<String> getIndex(
			String s )
	{
		if( lt.size() > 0 )
		{
			lt.removeAll( lt );
		}
		for( int i = 0 ; i < s.length() ; i++ )
		{
			if( s.substring( i , i + 1 ).equals( ":" ) )
			{
				continue;
			}
			else
			{
				lt.add( s.substring( i , i + 1 ) );
			}
		}
		return lt;
	}
	
	private String getLanguageEnv()
	{
		Locale l = Locale.getDefault();
		String language = l.getLanguage();
		String country = l.getCountry().toLowerCase();
		if( "zh".equals( language ) )
		{
			if( "cn".equals( country ) )
			{
				language = "zh-CN";
			}
			else if( "tw".equals( country ) )
			{
				language = "zh-TW";
			}
		}
		else if( "pt".equals( language ) )
		{
			if( "br".equals( country ) )
			{
				language = "pt-BR";
			}
			else if( "pt".equals( country ) )
			{
				language = "pt-PT";
			}
		}
		return language;
	}
	
	public String getResourcetime(
			int index )
	{
		String str = "";
		if( index == 0 )
		{
			str = LauncherDefaultConfig.getString( R.string.Sunday );
		}
		else if( index == 1 )
		{
			str = LauncherDefaultConfig.getString( R.string.Monday );
		}
		else if( index == 2 )
		{
			str = LauncherDefaultConfig.getString( R.string.Tuesday );
		}
		else if( index == 3 )
		{
			str = LauncherDefaultConfig.getString( R.string.Wednesday );
		}
		else if( index == 4 )
		{
			str = LauncherDefaultConfig.getString( R.string.Thursday );
		}
		else if( index == 5 )
		{
			str = LauncherDefaultConfig.getString( R.string.Friday );
		}
		else if( index == 6 )
		{
			str = LauncherDefaultConfig.getString( R.string.Saturday );
		}
		return str;
	}
}
