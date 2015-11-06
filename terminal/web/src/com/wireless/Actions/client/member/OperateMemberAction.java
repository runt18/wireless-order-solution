package com.wireless.Actions.client.member;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.member.MemberDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.sccon.ServerConnector;
import com.wireless.sms.SMS;
import com.wireless.sms.msg.Msg4Charge;

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
			final String pin = (String)request.getAttribute("pin");
			final String name = request.getParameter("name");
			final String mobile = request.getParameter("mobile");
			final String memberTypeId = request.getParameter("memberTypeId");
			final String sex = request.getParameter("sex");
			final String memberCard = request.getParameter("memberCard");
			final String birthday = request.getParameter("birthday");
			final String company = request.getParameter("company");
			final String addr = request.getParameter("addr");
			final String referrer = request.getParameter("referrer");
			final String isPrint = request.getParameter("isPrint");
			final String sendSms = request.getParameter("sendSms");
			String firstCharge = request.getParameter("firstCharge");
			final String firstActualCharge = request.getParameter("firstActualCharge");
			final String rechargeType = request.getParameter("rechargeType");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final Member.InsertBuilder builder = new Member.InsertBuilder(name, Integer.parseInt(memberTypeId));
			
			if(mobile != null && !mobile.isEmpty()){
				builder.setMobile(mobile);
			}
			
			if(memberCard != null && !memberCard.isEmpty()){
				builder.setMemberCard(memberCard);
			}	
			
			if(referrer != null && !referrer.isEmpty()){
				builder.setReferrer(Integer.parseInt(referrer));
			}
			
			if(birthday != null && !birthday.isEmpty()){
				builder.setBirthday(DateUtil.parseDate(birthday));
			}
			
			if(company != null && !company.isEmpty()){
				builder.setCompany(company);
			}
			
			if(addr != null && !addr.isEmpty()){
				builder.setContactAddr(addr);
			}
			
			if(sex != null && !sex.isEmpty()){
				builder.setSex(Member.Sex.valueOf(Integer.valueOf(sex)));
			}
			
			
			int memberID = MemberDao.insert(staff, builder);
			jobject.initTip(true, "操作成功, 新会员资料已添加.");
			
			if(firstActualCharge != null && !firstActualCharge.isEmpty()){
				if(firstCharge.isEmpty()){
					firstCharge = "0";
				}
				MemberOperation mo = MemberDao.charge(staff, memberID, Float.valueOf(firstCharge), Float.valueOf(firstActualCharge), ChargeType.valueOf(Integer.valueOf(rechargeType)));
				jobject.setMsg(jobject.getMsg() + "会员充值成功.");
				if(isPrint != null && Boolean.valueOf(isPrint)){
					try{
						ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildMemberReceipt(staff, mo.getId()).build());
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
				if(sendSms != null && Boolean.valueOf(sendSms)){
					try{
						//Send SMS.
						SMS.send(staff, mo.getMemberMobile(), new Msg4Charge(mo));
						jobject.setMsg(jobject.getMsg() + "充值短信发送成功.");
					}catch(Exception e){
						jobject.setMsg(jobject.getMsg() + "充值短信发送失败(" + e.getMessage() + ")");
						e.printStackTrace();
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
											    .setMemberType(Integer.valueOf(memberTypeId))
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
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
			String sendSms = request.getParameter("sendSms");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			MemberOperation mo = MemberDao.charge(staff, Integer.valueOf(memberID), Float.valueOf(payMannerMoney), Float.valueOf(rechargeMoney), ChargeType.valueOf(Integer.valueOf(rechargeType)));
			mo.setComment(comment);
			jobject.initTip(true, "操作成功, 会员充值成功.");
			if(isPrint != null && Boolean.valueOf(isPrint)){
				try{
					ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildMemberReceipt(staff, mo.getId()).build());
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
/*			for(Cookie cookie : request.getCookies()){
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
			}*/
			if(sendSms != null && Boolean.valueOf(sendSms)){
				try{
					//Send SMS.
					SMS.send(staff, mo.getMemberMobile(), new Msg4Charge(mo));
					jobject.setMsg(jobject.getMsg() + "充值短信发送成功.");
				}catch(Exception e){
					jobject.setMsg(jobject.getMsg() + "充值短信发送失败(" + e.getMessage() + ")");
					e.printStackTrace();
				}
			}			
			
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
				jobject.initTip(false, JObject.TIP_TITLE_ERROE, 9998, "操作失败, 会员取款未成功, 未知错误, 请联系客服人员.");
			}else{
				jobject.initTip(true, "操作成功, 会员取款成功.");
				if(isPrint != null && Boolean.valueOf(isPrint)){
					try{
						ReqPrintContent reqPrintContent = ReqPrintContent.buildMemberReceipt(staff, mo.getId());
						if(reqPrintContent != null){
							ProtocolPackage resp = ServerConnector.instance().ask(reqPrintContent.build());
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
			jobject.initTip4Exception(e);
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
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
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
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 关注会员
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
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
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 微信会员绑定前确认会员
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward checkMember(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		String mobile = request.getParameter("mobile");
		String card = request.getParameter("card");
		
		DBCon dbCon = null;
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			dbCon = new DBCon();
			dbCon.connect();
			
			Member dest = new Member(0);
			dest.setMobile(mobile);
			dest.setMemberCard(card);
			
			final List<Member> destsMatched = new ArrayList<Member>();
			//Get the member matched mobile.
			if(dest.hasMobile()){
				try{
					destsMatched.add(MemberDao.getByMobile(dbCon, staff, dest.getMobile()));
				}catch(BusinessException ignored){
					ignored.printStackTrace();
				}
			}
			//Get the member matched card.
			if(dest.hasMemberCard()){
				try{
					destsMatched.add(MemberDao.getByCard(dbCon, staff, dest.getMemberCard()));
				}catch(BusinessException ignored){
					ignored.printStackTrace();
				}
			}
			
			if(destsMatched.size() == 0){
				dest = null;
			}else if(destsMatched.size() == 1){
				dest = destsMatched.get(0);
			}else if(destsMatched.size() == 2){
				if(destsMatched.get(0).equals(destsMatched.get(1))){
					dest = destsMatched.get(0);
				}else{
					throw new BusinessException("绑定的手机和会员卡号分别属于两个不同的会员", MemberError.BIND_FAIL);
				}
			}
			
			jobject.setRoot(dest);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 绑定原卡到微信会员
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward bindWxMember(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		String memberId = request.getParameter("id");
		String orderId = request.getParameter("orderId");
		String mobile = request.getParameter("mobile");
		String card = request.getParameter("card");
		String name = request.getParameter("name");
		String sex = request.getParameter("sex");
		String birthday = request.getParameter("birthday");
		String memberType = request.getParameter("type");
		
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Member.BindBuilder builder = new Member.BindBuilder(Integer.parseInt(memberId), mobile, card);
			builder.setBirthday(DateUtil.parseDate(birthday));
			builder.setName(name);
			builder.setSex(Member.Sex.valueOf(Integer.parseInt(sex)));
			builder.setMemberType(Integer.parseInt(memberType));
			
			int mId = MemberDao.bind(staff, builder);
			
			if(orderId != null && !orderId.isEmpty()){
				Member member = MemberDao.getById(staff, mId);
				
				OrderDao.discount(staff, Order.DiscountBuilder.build4Member(Integer.parseInt(orderId), member));
			}
			
			jobject.initTip(true, "微信会员绑定成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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
