package com.wireless.Actions.client.member;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class MemberRechargeAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			String memberID = request.getParameter("memberID");
			String memberCardAlias = request.getParameter("memberCardAlias");
			String rechargeMoney = request.getParameter("rechargeMoney");
			String rechargeType = request.getParameter("rechargeType");
			String payMannerMoney = request.getParameter("payMannerMoney");
			String comment = request.getParameter("comment");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			MemberOperation mp = new MemberOperation();
			mp.setRestaurantID(Integer.valueOf(restaurantID));
			mp.setMemberID(Integer.valueOf(memberID));
			mp.setMemberCardAlias(memberCardAlias.trim());
			mp.setChargeMoney(Float.valueOf(rechargeMoney));
			mp.setChargeType(Short.valueOf(rechargeType));
			mp.setPayMoney(Float.valueOf(payMannerMoney));
			mp.setStaffID(term.id);
			mp.setStaffName(term.owner);
			mp.setComment(comment.trim());
			
			int count = MemberDao.recharge(mp);
			if(count == 0){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 会员充值未成功, 未知错误, 请联系客服人员.");
			}else{
				jobject.initTip(true, "操作成功, 会员充值成功.");
			}
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getMessage());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(JSONObject.fromObject(jobject).toString());
		}
		return null;
	}
	
}
