package com.wireless.Actions.client.member;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.sccon.ServerConnector;
import com.wireless.sms.SMS;
import com.wireless.sms.msg.Msg4Charge;
import com.wireless.util.WebParams;

public class OperateMemberAction extends DispatchAction{
	
	/**
	 * 新增
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String name = request.getParameter("name");
			String mobile = request.getParameter("mobile");
			String memberTypeId = request.getParameter("memberTypeId");
			String sex = request.getParameter("sex");
			String memberCard = request.getParameter("memberCard");
			String birthday = request.getParameter("birthday");
//			String tele = request.getParameter("telt");
			String company = request.getParameter("company");
			String addr = request.getParameter("addr");
			String isPrint = request.getParameter("isPrint");
			String firstCharge = request.getParameter("firstCharge");
			String firstActualCharge = request.getParameter("firstActualCharge");
			String rechargeType = request.getParameter("rechargeType");
					
//			String privateComment = request.getParameter("privateComment");
//			String publicComment = request.getParameter("publicComment");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Member.InsertBuilder ib = null;
			if(memberCard != null && !memberCard.isEmpty()){
				ib = Member.InsertBuilder.build4Card(name, memberCard, Integer.valueOf(memberTypeId)); 
				if(mobile != null && !mobile.isEmpty()){
					ib.setMobile(mobile);
				}
			}else if(mobile != null && !mobile.isEmpty()){
				ib = Member.InsertBuilder.build4Mobile(name, mobile, Integer.valueOf(memberTypeId));
				if(memberCard != null && !memberCard.isEmpty()){
					ib.setMemberCard(memberCard);
				}				
			}
			
			ib.setBirthday(DateUtil.parseDate(birthday))
			  .setSex(Member.Sex.valueOf(Integer.valueOf(sex)))
			  .setMemberCard(memberCard)
			  .setCompany(company)
			  .setContactAddr(addr)
			  .setSex(Member.Sex.valueOf(Integer.valueOf(sex)));
			
			int memberID = MemberDao.insert(staff, ib);
			jobject.initTip(true, "操作成功, 新会员资料已添加.");
			
			if(firstActualCharge != null && !firstActualCharge.isEmpty()){
				if(firstCharge.isEmpty()){
					firstCharge = "0";
				}
				MemberOperation mo = MemberDao.charge(staff, memberID, Float.valueOf(firstCharge), Float.valueOf(firstActualCharge), ChargeType.valueOf(Integer.valueOf(rechargeType)));
				jobject.setMsg(jobject.getMsg() + "会员充值成功.");
				if(isPrint != null && Boolean.valueOf(isPrint)){
					try{
						ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildReqPrintMemberReceipt(staff, mo.getId()));
						if(resp.header.type == Type.ACK){
							jobject.setMsg(jobject.getMsg() + "打印充值信息成功.");
						}else{
							jobject.setMsg(jobject.getMsg() + "打印充值信息失败.");
						}
					}catch(IOException e){
						e.printStackTrace();
						jobject.setMsg(jobject.getMsg() + "打印操作请求失败.");
					}
				}
				for(Cookie cookie : request.getCookies()){
				    if(cookie.getName().equals((request.getServerName() + "_chargeSms"))){
				    	if(cookie.getValue().equals("true")){
							try{
								//Send SMS.
								SMS.send(staff, mo.getMemberMobile(), new Msg4Charge(mo));
								jobject.setMsg(jobject.getMsg() + "充值短信发送成功.");
							}catch(Exception e){
								jobject.setMsg(jobject.getMsg() + "充值短信发送失败(" + e.getMessage() + ")");
								e.printStackTrace();
							}
				    	}
					    break;
				    }
				}
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 修改
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			String name = request.getParameter("name");
			String mobile = request.getParameter("mobile");
			String memberTypeId = request.getParameter("memberTypeId");
			String sex = request.getParameter("sex");
			String memberCard = request.getParameter("memberCard");
			String birthday = request.getParameter("birthday");
			String tele = request.getParameter("telt");
			String addr = request.getParameter("addr");
//			String privateComment = request.getParameter("privateComment");
			//String publicComment = request.getParameter("publicComment");
			String phonePublicComment = request.getParameter("phonePublicComment");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Member.UpdateBuilder ub = new Member.UpdateBuilder(Integer.valueOf(id))
											    .setName(name)
											    .setMobile(mobile)
											    .setMemberTypeId(Integer.valueOf(memberTypeId))
											    .setSex(Member.Sex.valueOf(Integer.valueOf(sex)))
											    .setBirthday(birthday)
											    .setMemberCard(memberCard)
											    .setTele(tele)
//											    .setPrivateComment(privateComment)
											    .setContactAddr(addr);
			if(phonePublicComment != null && !phonePublicComment.isEmpty()){
				ub.setPublicComment(phonePublicComment);
			}
			
			MemberDao.update(staff, ub);
			jobject.initTip(true, "操作成功, 会员资料已修改.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 删除
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MemberDao.deleteById(staff, Integer.valueOf(id));
			jobject.initTip(true, "操作成功, 会员资料已删除.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 充值
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward charge(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String memberID = request.getParameter("memberID");
			String rechargeMoney = request.getParameter("rechargeMoney");
			String payMannerMoney = request.getParameter("payMannerMoney");
			String rechargeType = request.getParameter("rechargeType");
			String comment = request.getParameter("comment");
			String isPrint = request.getParameter("isPrint");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			MemberOperation mo = MemberDao.charge(staff, Integer.valueOf(memberID), Float.valueOf(payMannerMoney), Float.valueOf(rechargeMoney), ChargeType.valueOf(Integer.valueOf(rechargeType)));
			mo.setComment(comment);
			jobject.initTip(true, "操作成功, 会员充值成功.");
			if(isPrint != null && Boolean.valueOf(isPrint)){
				try{
					ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildReqPrintMemberReceipt(staff, mo.getId()));
					if(resp.header.type == Type.ACK){
						jobject.setMsg(jobject.getMsg() + "打印充值信息成功.");
					}else{
						jobject.setMsg(jobject.getMsg() + "打印充值信息失败.");
					}
				}catch(IOException e){
					e.printStackTrace();
					jobject.setMsg(jobject.getMsg() + "打印操作请求失败.");
				}
			}
			for(Cookie cookie : request.getCookies()){
			    if(cookie.getName().equals((request.getServerName() + "_chargeSms"))){
			    	if(cookie.getValue().equals("true")){
						try{
							//Send SMS.
							SMS.send(staff, mo.getMemberMobile(), new Msg4Charge(mo));
							jobject.setMsg(jobject.getMsg() + "充值短信发送成功.");
						}catch(Exception e){
							jobject.setMsg(jobject.getMsg() + "充值短信发送失败(" + e.getMessage() + ")");
							e.printStackTrace();
						}
			    	}
				    break;
			    }
			}

			
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	/**
	 * 取款
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward takeMoney(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			String memberID = request.getParameter("memberID");
			String rechargeMoney = request.getParameter("takeMoney");
			String payMannerMoney = request.getParameter("payMannerMoney");
			String comment = request.getParameter("comment");
			String isPrint = request.getParameter("isPrint");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			MemberOperation mo = MemberDao.refund(staff, Integer.valueOf(memberID), Float.valueOf(payMannerMoney), Float.valueOf(rechargeMoney));
			if(comment != null){
				mo.setComment(comment);
			}
			if(mo == null || mo.getId() == 0){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 会员取款未成功, 未知错误, 请联系客服人员.");
			}else{
				jobject.initTip(true, "操作成功, 会员取款成功.");
				if(isPrint != null && Boolean.valueOf(isPrint)){
					try{
						ReqPrintContent reqPrintContent = ReqPrintContent.buildReqPrintMemberReceipt(staff, mo.getId());
						if(reqPrintContent != null){
							ProtocolPackage resp = ServerConnector.instance().ask(reqPrintContent);
							if(resp.header.type == Type.ACK){
								jobject.setMsg(jobject.getMsg() + "打印取款信息成功.");
							}else{
								jobject.setMsg(jobject.getMsg() + "打印取款信息失败.");
							}
						}
					}catch(IOException e){
						e.printStackTrace();
						jobject.setMsg(jobject.getMsg() + " 打印操作请求失败, 请联系客服人员.");
					}
				}
			}
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 积分调整
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward adjustPoint(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String memberId = request.getParameter("memberId");
			String point = request.getParameter("point");
			String adjust = request.getParameter("adjust");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MemberDao.adjustPoint(staff, Integer.valueOf(memberId), Integer.valueOf(point), Member.AdjustType.valueOf(Integer.valueOf(adjust)));
			jobject.initTip(true, "操作成功, 会员积分调整成功.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 积分消费
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward consumePoint(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String memberId = request.getParameter("memberId");
			String point = request.getParameter("point");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MemberDao.pointConsume(staff, Integer.valueOf(memberId), Integer.valueOf(point));
			jobject.initTip(true, "操作成功, 会员积分消费成功.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
	public ActionForward interestedMember(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String attendtion = request.getParameter("attendtion");
			String memberId = request.getParameter("memberId");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(Integer.parseInt(attendtion) == 0){
				MemberDao.cancelInterestedIn(staff, Integer.parseInt(memberId));
				jobject.initTip(true, "取消成功");
			}else{
				MemberDao.interestedIn(staff, Integer.parseInt(memberId));
				jobject.initTip(true, "关注成功");
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
