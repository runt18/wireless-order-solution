package com.wireless.Actions.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.system.BillBoardDao;
import com.wireless.json.JObject;
import com.wireless.pojo.system.BillBoard;

public class QueryBillboardAction extends DispatchAction{

	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			String extra = "";
			if(isPaging != null && isPaging.trim().equals("true")){
				jobject.setTotalProperty(BillBoardDao.getCount(dbCon, extra));
			}
			jobject.setRoot(BillBoardDao.get(dbCon, extra + " LIMIT " + start + "," + limit));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 餐厅登陆公告
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward loginInfo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String extra = " AND BB.expired >= NOW() AND BB.type = " + BillBoard.Type.SYSTEM.getVal() + " ORDER BY created DESC ";
			jobject.setRoot(BillBoardDao.get(extra));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 员工登陆公告
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward persionLoginInfo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String extra = " AND BB.expired >= NOW() AND BB.restaurant_id = " + request.getParameter("rid") + " ORDER BY created DESC ";
			jobject.setRoot(BillBoardDao.get(extra));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 微信促销信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward WXSales(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String extra = " AND BB.type = 3 AND BB.restaurant_id = " + request.getParameter("rid") + " ORDER BY created DESC ";
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
