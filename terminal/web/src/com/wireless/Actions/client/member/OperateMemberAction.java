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
import org.marker.weixin.api.BaseAPI;

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
	 * 新增会员
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject();
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
			final String age = request.getParameter("age");
			
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
			
			if(age != null && !age.isEmpty() && !age.equals("-1")){
				builder.setAge(Member.Age.valueOf(Integer.parseInt(age)));
			}
			
			int memberID = MemberDao.insert(staff, builder);
			jObject.initTip(true, "操作成功, 新会员资料已添加.");
			
			if(firstActualCharge != null && !firstActualCharge.isEmpty()){
				if(firstCharge.isEmpty()){
					firstCharge = "0";
				}
				MemberOperation mo = MemberDao.charge(staff, memberID, Float.valueOf(firstCharge), Float.valueOf(firstActualCharge), ChargeType.valueOf(Integer.valueOf(rechargeType)));
				jObject.setMsg(jObject.getMsg() + "会员充值成功.");
				if(isPrint != null && Boolean.valueOf(isPrint)){
					try{
						ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildMemberReceipt(staff, mo.getId()).build());
						if(resp.header.type == Type.ACK){
							jObject.setMsg(jObject.getMsg() + "打印充值信息成功.");
						}else{
							jObject.setMsg(jObject.getMsg() + "打印充值信息失败.");
						}
					}catch(IOException e){
						e.printStackTrace();
						jObject.setMsg(jObject.getMsg() + "打印操作请求失败.");
					}
				}
				if(sendSms != null && Boolean.valueOf(sendSms)){
					try{
						//Send SMS.
						SMS.send(staff, mo.getMemberMobile(), new Msg4Charge(mo));
						jObject.setMsg(jObject.getMsg() + "充值短信发送成功.");
					}catch(Exception e){
						jObject.setMsg(jObject.getMsg() + "充值短信发送失败(" + e.getMessage() + ")");
						e.printStackTrace();
					}
				}
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 修改会员
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject();
		try{
			final String pin = (String)request.getAttribute("pin");
			final String id = request.getParameter("id");
			final String name = request.getParameter("name");
			final String mobile = request.getParameter("mobile");
			final String memberTypeId = request.getParameter("memberTypeId");
			final String sex = request.getParameter("sex");
			final String memberCard = request.getParameter("memberCard");
			final String birthday = request.getParameter("birthday");
			final String tele = request.getParameter("telt");
			final String addr = request.getParameter("addr");
			final String age = request.getParameter("age");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final Member.UpdateBuilder builder = new Member.UpdateBuilder(Integer.valueOf(id));
			
			if(name != null){
				builder.setName(name);
			}
			
			if(mobile != null){
				builder.setMobile(mobile);
			}
			
			if(memberTypeId != null && !memberTypeId.isEmpty()){
				builder.setMemberType(Integer.valueOf(memberTypeId));
			}
			
			if(sex != null && !sex.isEmpty()){
				builder.setSex(Member.Sex.valueOf(Integer.valueOf(sex)));
			}
					
			if(birthday != null && !birthday.isEmpty()){
				builder.setBirthday(birthday);
			}
											
			if(memberCard != null){
				builder.setMemberCard(memberCard);
			}
			
			if(tele != null && !tele.isEmpty()){
				builder.setTele(tele);
			}
								
			if(addr != null && !addr.isEmpty()){
				builder.setContactAddr(addr);
			}
			
			if(age != null && !age.isEmpty()){
				builder.setAge(Member.Age.valueOf(Integer.parseInt(age)));
			}
			
			MemberDao.update(staff, builder);
			jObject.initTip(true, "操作成功, 会员资料已修改.");
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 删除会员
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		JObject jObject = new JObject();
		try{
			final String pin = (String)request.getAttribute("pin");
			final String id = request.getParameter("id");
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MemberDao.deleteById(staff, Integer.valueOf(id));
			jObject.initTip(true, "操作成功, 会员资料已删除.");
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
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
		
		JObject jObject = new JObject();
		try{
			final String pin = (String)request.getAttribute("pin");
			final String memberId = request.getParameter("memberID");
			final String rechargeMoney = request.getParameter("rechargeMoney");
			final String payMannerMoney = request.getParameter("payMannerMoney");
			final String rechargeType = request.getParameter("rechargeType");
			final String comment = request.getParameter("comment");
			final String isPrint = request.getParameter("isPrint");
			final String sendSms = request.getParameter("sendSms");
			final String orientedPrinters = request.getParameter("orientedPrinter");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final MemberOperation mo = MemberDao.charge(staff, Integer.valueOf(memberId), Float.valueOf(payMannerMoney), Float.valueOf(rechargeMoney), ChargeType.valueOf(Integer.valueOf(rechargeType)));
			mo.setComment(comment);
			jObject.initTip(true, "操作成功, 会员充值成功.");
			if(isPrint != null && Boolean.valueOf(isPrint)){
				try{
					final ReqPrintContent reqPrint = ReqPrintContent.buildMemberReceipt(staff, mo.getId());
					if(orientedPrinters != null && !orientedPrinters.isEmpty()){
						for(String orientedPrinter : orientedPrinters.split(",")){
							reqPrint.addPrinter(Integer.parseInt(orientedPrinter));
						}
					}
					ProtocolPackage resp = ServerConnector.instance().ask(reqPrint.build());
					if(resp.header.type == Type.ACK){
						jObject.setMsg(jObject.getMsg() + "打印充值信息成功.");
					}else{
						jObject.setMsg(jObject.getMsg() + "打印充值信息失败.");
					}
				}catch(IOException e){
					e.printStackTrace();
					jObject.setMsg(jObject.getMsg() + "打印操作请求失败.");
				}
			}
			
			if(sendSms != null && Boolean.valueOf(sendSms)){
				try{
					//Send SMS.
					SMS.send(staff, mo.getMemberMobile(), new Msg4Charge(mo));
					jObject.setMsg(jObject.getMsg() + "充值短信发送成功.");
				}catch(Exception e){
					jObject.setMsg(jObject.getMsg() + "充值短信发送失败(" + e.getMessage() + ")");
					e.printStackTrace();
				}
			}
			
			final String serverName;
			if(request.getServerName().equals("e-tones.net")){
				serverName = "wx.e-tones.net";
			}else{
				serverName = request.getServerName();
			}
			
			//Perform to send the weixin charge msg to member.
			if(MemberDao.getById(staff, Integer.parseInt(memberId)).hasWeixin()){
				new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							BaseAPI.doPost("http://" + serverName + "/wx-term/WxNotifyMember.do?dataSource=charge&moId=" + mo.getId() + "&staffId=" + staff.getId(), "");
						} catch (Exception ignored) {
							ignored.printStackTrace();
						}
					}
					
				}).run();
			}

			
		}catch(BusinessException e){	
			e.printStackTrace();
			jObject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
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
		}catch(BusinessException | SQLException e){
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
	 * 补打微信电子卡
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward patchWxCard(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final JObject jobject = new JObject();
		final String destMemberId = request.getParameter("memberId");
		final String wxMemberId = request.getParameter("wxMemberId");
		final String pin = (String)request.getAttribute("pin");

		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MemberDao.patchWxCard(staff, Integer.parseInt(destMemberId), Integer.parseInt(wxMemberId));
			jobject.initTip(true, "补打微信电子卡成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
