package com.wireless.Actions.tasteMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.protocol.Terminal;
import com.wireless.util.WebParams;

public class OperateTasteAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String alias = request.getParameter("alias");
			String name = request.getParameter("name");
			String price = request.getParameter("price");
			String rate = request.getParameter("rate");
			String cate = request.getParameter("cate");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			Taste insert = new Taste(Integer.valueOf(alias), 
				term.restaurantID, 
				name, 
				Float.valueOf(price), 
				Float.valueOf(rate), 
				Integer.valueOf(cate)
			);
			
			TasteDao.insert(term, insert);
			jobject.initTip(true, "操作成功, 已添加新口味信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String id = request.getParameter("id");
			String alias = request.getParameter("alias");
			String name = request.getParameter("name");
			String price = request.getParameter("price");
			String rate = request.getParameter("rate");
			String cate = request.getParameter("cate");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			Taste insert = new Taste(Integer.valueOf(id), 
				Integer.valueOf(alias), 
				term.restaurantID, 
				name, 
				Float.valueOf(price), 
				Float.valueOf(rate), 
				Integer.valueOf(cate)
			);
			
			TasteDao.update(term, insert);
			jobject.initTip(true, "操作成功, 已修改口味信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String id = request.getParameter("id");
			TasteDao.delete(VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF), Integer.valueOf(id));
			jobject.initTip(true, "操作成功, 已删除口味信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
