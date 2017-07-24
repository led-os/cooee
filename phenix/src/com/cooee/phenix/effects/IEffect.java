package com.cooee.phenix.effects;


import android.view.View;
import android.view.ViewGroup;


/**
 * 特效接口
 * @author temp
 *
 */
public interface IEffect
{
	
	int getCountX();//水平子视图数目
	
	int getCountY();//竖直子视图数目
	
	View getView();//实现类
	
	int getMeasuredWidth();//宽度
	
	int getMeasuredHeight();//高度
	
	int getChildrenCellX(
			//Horizontal location of the item in the grid
			int index );
	
	int getChildrenCellY(
			//Vertical location of the item in the grid.
			int index );
	
	void setCameraDistance(
			//没用到
			float f );
	
	float getCameraDistance();
	
	void setPivotY(
			//y轴中心坐标
			float i );
	
	void setPivotX(
			//x轴中心坐标
			float i );
	
	void setTranslationX(
			//x位移
			float xTranslation );
	
	void setTranslationY(
			//y位移
			float xTranslation );
	
	void setRotationY(
			//y轴倾斜度
			float yRotation );
	
	void setAlpha(
			//透明度
			float mAlpha );
	
	void setRotationX(
			//x轴倾斜度
			float f );
	
	void setRotation(
			//倾斜度
			float f );
	
	void setScaleX(
			//水平缩放
			float f );
	
	void setScaleY(
			//竖直缩放
			float f );
	
	View getChildOnPageAt(
			//根据索引得到子视图
			int i );
	
	int getCellWidth();//子视图宽度
	
	int getCellHeight();//子视图高度
	
	int getCellWidthGap();//子视图宽间距
	
	int getCellHeightGap();//子视图高间距
	
	void getLocationOnScreen(
			//在屏幕上的坐标
			int[] lOcationOnScreen );
	
	int getPageChildCount();//每页总数目
	
	ViewGroup getChildrenLayout();//每页的viewgroup
}
