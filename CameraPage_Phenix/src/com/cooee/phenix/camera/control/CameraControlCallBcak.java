package com.cooee.phenix.camera.control;


import com.cooee.phenix.camera.entity.PictureInfo;


// CameraPage
public interface CameraControlCallBcak
{
	
	/**
	 * 相机打开完成后的回调,进行界面的处理
	 * @param success 相机是否成功打开
	 * @param isBack 是否为后置摄像头
	 * @author yangtianyu 2016-8-2
	 */
	public void openCamera(
			boolean success ,
			boolean isBack );
	
	/**
	 * 相机关闭完成后的回调,进行界面的处理
	 * @author yangtianyu 2016-8-2
	 */
	public void closeCamera();
	
	/**
	 * 更改闪光灯的状态后的回调,一共存在三种状态,关闭、自动和开启。
	 * @param Status 更改后的闪光灯状态,为Parameters.FLASH_MODE_OFF,
	 * Parameters.FLASH_MODE_ON,Parameters.FLASH_MODE_AUTO中的一种
	 * @see #android.hardware.Camera.Parameters
	 * @author yangtianyu 2016-6-24
	 */
	public void changeFlashlight(
			String Status );
	
	public void tackPictureSuccess(
			final PictureInfo info );
	
	public void tackPictureFail();
}
