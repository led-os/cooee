// xiatian add whole file //OperateFolder
package com.cooee.weather.download;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.cooee.weather.download.DownloadHelper.DownloadListener;


public class DownloadUtils
{
	
	public static final String FOLDER_rCN = "foldercname";
	public static final String FOLDER_rTW = "folderbname";
	public static final String FOLDER_default = "folderename";
	public static final String APP_rCN = "appchname";
	public static final String APP_rTW = "appbigname";
	public static final String APP_default = "appname";
	private static HttpClient customerHttpClient;
	private static final String CHARSET = HTTP.UTF_8;
	
	public static String getFolderName(
			String local )
	{
		if( local.contains( "CN" ) )
			return FOLDER_rCN;
		else if( local.contains( "TW" ) )
			return FOLDER_rTW;
		else
			return FOLDER_default;
	}
	
	public static String getAppName(
			String local )
	{
		if( local.contains( "CN" ) )
			return APP_rCN;
		else if( local.contains( "TW" ) )
			return APP_rTW;
		else
			return APP_default;
	}
	
	public static String getMD5EncruptKey(
			String logInfo )
	{
		String res = null;
		MessageDigest messagedigest;
		try
		{
			messagedigest = MessageDigest.getInstance( "MD5" );
		}
		catch( NoSuchAlgorithmException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		messagedigest.update( logInfo.getBytes() );
		res = bufferToHex( messagedigest.digest() );
		// Log.v("http", "getMD5EncruptKey res =  " + res);
		return res;
	}
	
	protected static char hexDigits[] = { '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' , '9' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f' };
	
	private static String bufferToHex(
			byte bytes[] )
	{
		return bufferToHex( bytes , 0 , bytes.length );
	}
	
	private static String bufferToHex(
			byte bytes[] ,
			int m ,
			int n )
	{
		StringBuffer stringbuffer = new StringBuffer( 2 * n );
		int k = m + n;
		for( int l = m ; l < k ; l++ )
		{
			appendHexPair( bytes[l] , stringbuffer );
		}
		return stringbuffer.toString();
	}
	
	private static void appendHexPair(
			byte bt ,
			StringBuffer stringbuffer )
	{
		char c0 = hexDigits[( bt & 0xf0 ) >> 4]; // 取字节中高 4 位的数字转换, >>>
													// 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同
		char c1 = hexDigits[bt & 0xf]; // 取字节中低 4 位的数字转换
		stringbuffer.append( c0 );
		stringbuffer.append( c1 );
	}
	
	// 0:文件不存在，1：文件存在但不完整，2：文件完整
	public static int verifyAPKFile(
			Context context ,
			String path )
	{
		File packageFile = new File( path );
		if( packageFile.exists() )
		{
			PackageManager pm = context.getPackageManager();
			PackageInfo info = pm.getPackageArchiveInfo( path , PackageManager.GET_ACTIVITIES );
			if( info != null )
			{
				return 2;
			}
			else
				return 1;
		}
		else
			return 0;
	}
	
	// 安装APK文件
	public static void installAPKFile(
			Context activity ,
			DownloadListener listener ,
			String path )
	{
		Intent intent = new Intent();
		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
		intent.setAction( android.content.Intent.ACTION_VIEW );
		intent.setDataAndType( Uri.fromFile( new File( path ) ) , "application/vnd.android.package-archive" );
		activity.startActivity( intent );
		DownloadProxy.getInstance( activity ).addListener( listener );
	}
	
	public static boolean isNetworkAvailable(
			Context context )
	{
		try
		{
			ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE );
			NetworkInfo info = cm.getActiveNetworkInfo();
			return( info != null && info.isConnected() );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public static long getDownloadLength(
			String url )
	{
		long length = 0;
		try
		{
			String urlName;
			urlName = url;
			URL realUrl = new URL( urlName );
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			conn.setReadTimeout( 30000 );
			conn.connect();
			length = conn.getContentLength();
			return length;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return length;
	}
	
	public static String getSDPath()
	{
		File SDdir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals( android.os.Environment.MEDIA_MOUNTED );
		if( sdCardExist )
		{
			SDdir = Environment.getExternalStorageDirectory();
		}
		if( SDdir != null )
		{
			return SDdir.toString();
		}
		else
		{
			return null;
		}
	}
	
	public static InputStream sendDownload(
			String url ,
			long start ,
			long length )
	{
		try
		{
			String urlName;
			urlName = url;
			URL realUrl = new URL( urlName );
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setReadTimeout( 30000 );
			conn.setRequestProperty( "accept" , "*/*" );
			conn.setRequestProperty( "connection" , "Keep-Alive" );
			conn.setRequestProperty( "user-agent" , "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)" );
			conn.setAllowUserInteraction( true );
			// 设置当前线程下载的起点，终点
			conn.setRequestProperty( "Range" , "bytes=" + start + "-" + length );
			// 建立实际的连接
			conn.connect();
			return conn.getInputStream();
		}
		catch( Exception e )
		{
			// System.out.println("发送GET请求出现异常！" + e);
			e.printStackTrace();
		}
		return null;
	}
	
	public static InputStream sendGet(
			String url ,
			String params )
	{
		BufferedReader in = null;
		try
		{
			String urlName;
			if( ( params != null ) && ( !"".equals( params ) ) )
			{
				urlName = url + "?" + params;
			}
			else
			{
				urlName = url;
			}
			URL realUrl = new URL( urlName );
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setReadTimeout( 30000 );
			conn.setRequestProperty( "accept" , "*/*" );
			conn.setRequestProperty( "connection" , "Keep-Alive" );
			conn.setRequestProperty( "user-agent" , "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)" );
			// 建立实际的连接
			conn.connect();
			// 获取所有响应头字段
			return conn.getInputStream();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally
		{
			try
			{
				if( in != null )
				{
					in.close();
				}
			}
			catch( IOException ex )
			{
				ex.printStackTrace();
			}
		}
		return null;
	}
	
	public static String getShellID(
			Context context )
	{
		//		String id = CooeeSdk.cooeeGetCooeeId( context );
		//		Log.i( "OPFolder" , "shell id=" + id );
		//		return id;
		return "";
	}
	
	public static String OPERATE_FOLDER_CLIENT_VERSION = "0.0.0";
	
	public static String getVersion()
	{
		String clientVersionCode = DownloadProxy.VERSION_CODE;
		// String interfaceVersionCode =
		// ConfigBase.OPERATE_FOLDER_CLIENT_VERSION;
		String interfaceVersionCode = OPERATE_FOLDER_CLIENT_VERSION;
		return clientVersionCode + "." + interfaceVersionCode;
	}
	
	public static synchronized HttpClient getHttpClient()
	{
		if( null == customerHttpClient )
		{
			HttpParams params = new BasicHttpParams();
			// 设置一些基本参数
			HttpProtocolParams.setVersion( params , HttpVersion.HTTP_1_1 );
			HttpProtocolParams.setContentCharset( params , CHARSET );
			HttpProtocolParams.setUseExpectContinue( params , true );
			HttpProtocolParams.setUserAgent( params , "Android 2.2.1" );
			// 超时设置
			/* 从连接池中取连接的超时时间 */
			ConnManagerParams.setTimeout( params , 5000 );
			/* 连接超时 */
			HttpConnectionParams.setConnectionTimeout( params , 10000 );
			/* 请求超时 */
			HttpConnectionParams.setSoTimeout( params , 10000 );
			// 设置我们的HttpClient支持HTTP和HTTPS两种模式
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register( new Scheme( "http" , (SocketFactory)PlainSocketFactory.getSocketFactory() , 80 ) );
			schReg.register( new Scheme( "https" , (SocketFactory)SSLSocketFactory.getSocketFactory() , 443 ) );
			// 使用线程安全的连接管理来创建HttpClient
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager( params , schReg );
			customerHttpClient = new DefaultHttpClient( conMgr , params );
		}
		return customerHttpClient;
	}
	
	public static String[] post(
			Context context ,
			String url ,
			String content )
	{
		if( isNetworkAvailable( context ) == false )
		{
			Log.v( "http" , "network unavailable---" );
			return null;
		}
		try
		{
			StringEntity entity = new StringEntity( content , HTTP.UTF_8 );
			HttpPost request = new HttpPost( url );
			request.setHeader( "Content-Type" , "application/json; charset=UTF-8" );
			request.setEntity( entity );
			HttpClient client = getHttpClient();
			HttpResponse response = client.execute( request );
			if( response.getStatusLine().getStatusCode() == HttpStatus.SC_OK )
			{
				System.out.println( "SC_OK" );
				HttpEntity resEntity = response.getEntity();
				String strResult = ( resEntity == null ) ? null : EntityUtils.toString( resEntity , CHARSET );
				String[] res = new String[2];
				res[0] = strResult;
				res[1] = resEntity.getContentLength() + "";
				return res;
			}
			else
			{
				System.out.println( "no _SC_OK" );
				HttpEntity resEntity = response.getEntity();
				String strResult = ( resEntity == null ) ? null : EntityUtils.toString( resEntity , CHARSET );
				Log.v( "http" , "customerHttpClient post error = " + response.getStatusLine().getStatusCode() + " " + strResult );
				return null;
			}
		}
		catch( UnsupportedEncodingException e )
		{
			Log.v( "http" , "UnsupportedEncodingException...." + e.toString() );
		}
		catch( ClientProtocolException e )
		{
			Log.v( "http" , "ClientProtocolException...." + e.toString() );
		}
		catch( IOException e )
		{
			Log.v( "http" , "IOException...." + e.toString() );
		}
		return null;
	}
	
	private static Handler mHandler = new Handler();
	
	public static void toast(
			final Context activity ,
			final String text )
	{
		if( activity == null || text == null )
		{
			return;
		}
		//		activity.runOnUiThread( new Runnable() {
		//			
		//			@Override
		//			public void run()
		//			{
		mHandler.post( new Runnable() {
			
			@Override
			public void run()
			{
				Toast.makeText( activity , text , Toast.LENGTH_SHORT ).show();
			}
		} );
		//				Toast.makeText( activity , text , Toast.LENGTH_SHORT ).show();
		//			}
		//		} );
	}
}
