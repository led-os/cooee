/**
 * @file CateBloomJni.java
 * @brief
 * @author TangJunxing, 385749807@qq.com
 * @version 0.1alpha
 * @date 2014-12-23
 */
package cool.sdk.Category;


import com.cooee.framework.utils.StringUtils;


public class CateBloomJni
{
	
	protected static native long initpath(
			String block_path );
	
	protected static native long initbytes(
			byte[] buffer ,
			int len );
	
	protected static native int pname2caid(
			long blocks ,
			String pname );
	
	protected static native void free(
			long blocks );
	
	protected static native String[][] getCateFrames();
	
	static String version = "v7";
	static
	{
		System.loadLibrary( StringUtils.concat( "cate_bloom_jni_" , version ) );
	}
}
