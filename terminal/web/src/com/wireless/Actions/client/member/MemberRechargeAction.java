package com.wireless.Actions.client.member;

import java.io.IOException;

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
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqPackage;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.protocol.Terminal;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class MemberRechargeAction extends Action implements PinGen{
	
	Terminal term;
	public long getDeviceId() {
		return term.pin;
	}
	public short getDeviceType() {
		return term.modelID;
	}
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String memberID = request.getParameter("memberID");
			String rechargeMoney = request.getParameter("rechargeMoney");
			String rechargeType = request.getParameter("rechargeType");
			String isPrint = request.getParameter("isPrint");
			
			term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			MemberOperation mo = MemberDao.charge(term, Integer.valueOf(memberID), Float.valueOf(rechargeMoney), ChargeType.valueOf(Integer.valueOf(rechargeType)));
			if(mo == null || mo.getId() == 0){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 会员充值未成功, 未知错误, 请联系客服人员.");
			}else{
				jobject.initTip(true, "操作成功, 会员充值成功.");
				if(isPrint != null && Boolean.valueOf(isPrint)){
					try{
						ReqPrintContent reqPrintContent = ReqPrintContent.buildReqPrintMemberReceipt(mo.getId());
						if(reqPrintContent != null){
							ReqPackage.setGen(this);
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
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(JSONObject.fromObject(jobject).toString());
		}
		return null;
	}
}
