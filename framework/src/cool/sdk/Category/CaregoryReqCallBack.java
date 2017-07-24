package cool.sdk.Category;


import java.util.List;


// floder category , change by shlt@2014/12/08 UPD
public interface CaregoryReqCallBack
{
	
	void ReqFailed(
			CaregoyReqType type ,
			String Msg );
	
	void ReqSucess(
			CaregoyReqType type ,
			List<String> appList );
}
