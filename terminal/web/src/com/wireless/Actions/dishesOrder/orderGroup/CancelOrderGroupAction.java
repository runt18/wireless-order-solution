package com.wireless.Actions.dishesOrder.orderGroup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.orderMgr.OrderGroupDao;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class CancelOrderGroupAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String orderID = request.getParameter("orderID");
			if(pin == null){
				jobject.initTip(false, "操作失败, 验证终端有效信息错误, 请联系管理员.");
			}
			if(orderID == null){
				jobject.initTip(false, "操作失败, 获取账单编号错误, 请联系管理员.");
			}
			if(jobject.isSuccess()){
				Order o = new Order();
				o.setId(Integer.valueOf(orderID));
				OrderGroupDao.cancel(VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF), o);
				jobject.initTip(true, "操作成功, 已取消团体餐桌信息.");
			}
		} catch (BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}

}
