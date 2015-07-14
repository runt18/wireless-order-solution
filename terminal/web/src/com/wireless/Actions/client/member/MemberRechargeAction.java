package com.wireless.Actions.client.member;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.member.MemberDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class MemberRechargeAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String memberID = request.getParameter("memberID");
			String rechargeMoney = request.getParameter("rechargeMoney");
			String payMoney = request.getParameter("payMoney");
			String rechargeType = request.getParameter("rechargeType");
			String isPrint = request.getParameter("isPrint");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			MemberOperation mo = MemberDao.charge(staff, Integer.valueOf(memberID), Float.valueOf(payMoney), Float.valueOf(rechargeMoney), ChargeType.valueOf(Integer.valueOf(rechargeType)));
			if(mo == null || mo.getId() == 0){
				jobject.initTip(false, JObject.TIP_TITLE_ERROE, 9998, "操作失败, 会员充值未成功, 未知错误, 请联系客服人员.");
			}else{
				jobject.initTip(true, "操作成功, 会员充值成功.");
				if(isPrint != null && Boolean.valueOf(isPrint)){
					try{
						ReqPrintContent reqPrintContent = ReqPrintContent.buildMemberReceipt(staff, mo.getId());
						if(reqPrintContent != null){
							ProtocolPackage resp = ServerConnector.instance().ask(reqPrintContent);
							if(resp.header.type == Type.ACK){
								jobject.setMsg(jobject.getMsg() + "打印充值信息成功.");
							}else{
								jobject.setMsg(jobject.getMsg() + "打印充值信息失败.");
							}
						}
					}catch(IOException e){
						e.printStackTrace();
						jobject.setMsg(jobject.getMsg() + "操作请求错误, 请联系客服人员.");
					}
				}
			}
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
