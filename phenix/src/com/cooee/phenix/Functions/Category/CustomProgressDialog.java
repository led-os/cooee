package com.cooee.phenix.Functions.Category;


import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import com.cooee.phenix.R;


// cheyingkun add start //解决“在其他桌面安装应用，phenix几率性异常停止运行”的问题。【i_0010424】
/**
 * 自定义滚动dialog
 * @author cheyingkun
 */
public class CustomProgressDialog extends Dialog
{
	
	public CustomProgressDialog(
			Context context ,
			String strMessage )
	{
		this( context , R.style.CategoryCustomProgressDialog , strMessage );
	}
	
	public CustomProgressDialog(
			Context context ,
			int theme ,
			String strMessage )
	{
		super( context , theme );
		this.setContentView( R.layout.default_loading_progress );
		this.getWindow().getAttributes().gravity = Gravity.CENTER;
		TextView tvMsg = (TextView)this.findViewById( R.id.startLoader_state );
		if( tvMsg != null )
		{
			tvMsg.setText( strMessage );
		}
	}
	//	@Override
	//	public void onWindowFocusChanged(
	//			boolean hasFocus )
	//	{
	//		if( !hasFocus )
	//		{
	//			dismiss();
	//		}
	//	}
}
//cheyingkun add end