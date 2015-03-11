package com.wireless.Actions.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
//		String isPaging = request.getParameter("isPaging");
//		String start = request.getParameter("start");
//		String limit = request.getParameter("limit");
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			jobject.setRoot(BillBoardDao.getByCond(dbCon, null));
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
		String rid = request.getParameter("rid");
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			List<BillBoard> bbs = new ArrayList<>();
			bbs.addAll(BillBoardDao.getByCond(dbCon, new BillBoardDao.ExtraCond().setType(BillBoard.Type.SYSTEM)));
			bbs.addAll(BillBoardDao.getByCond(dbCon, new BillBoardDao.ExtraCond().setRestaurant(Integer.parseInt(rid)))); 
			
			Collections.sort(bbs, new Comparator<BillBoard>() {
				@Override
				public int compare(BillBoard b1, BillBoard b2) {
					if(b1.getStatus().getVal() > b2.getStatus().getVal()){
						return 1;
					}else if(b1.getStatus().getVal() < b2.getStatus().getVal()){
						return -1;
					}else{
						return (int) (b2.getCreated() - b1.getCreated());
					}
				}
	        });
			
			jobject.setRoot(bbs);
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
//			jobject.setRoot(BillBoardDao.get(extra));
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
//			jobject.setRoot(BillBoardDao.get(extra));
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
