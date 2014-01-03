package com.wireless.Actions.weixin.query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.system.BillBoardDao;
import com.wireless.json.JObject;
import com.wireless.pojo.system.BillBoard;

public class WXQueryInfoAction extends DispatchAction{
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String extra = " AND BB.type = " + BillBoard.Type.WX_INFO.getVal() + " ORDER BY created DESC LIMIT 0,1 ";
			jobject.setRoot(BillBoardDao.get(extra));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
