package com.wireless.Actions.client.member;

import java.io.IOException;

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
import com.wireless.sccon.ServerConnector;
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String name = request.getParameter("name");
			String mobile = request.getParameter("mobile");
			String memberTypeId = request.getParameter("memberTypeId");
			String sex = request.getParameter("sex");
			String memberCard = request.getParameter("memberCard");
			String birthday = request.getParameter("birthday");
			String tele = request.getParameter("telt");
			String idCard = request.getParameter("idCard");
			String company = request.getParameter("company");
			String tastePref = request.getParameter("tastePref");
			String taboo = request.getParameter("taboo");
			String addr = request.getParameter("addr");
			String comment = request.getParameter("comment");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Member.InsertBuilder ib = new Member.InsertBuilder(staff.getRestaurantId(), name, mobile, Integer.valueOf(memberTypeId), Member.Sex.valueOf(Integer.valueOf(sex)));
			ib.setBirthday(birthday)
			  .setMemberCard(memberCard)
			  .setTele(tele)
			  .setIdCard(idCard)
			  .setCompany(company)
			  .setTastePref(tastePref)
			  .setTaboo(taboo)
			  .setContactAddr(addr)
			  .setComment(comment);
			
			MemberDao.insert(staff, ib);
			jobject.initTip(true, "操作成功, 新会员资料已添加.");
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
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
			String idCard = request.getParameter("idCard");
			String company = request.getParameter("company");
			String tastePref = request.getParameter("tastePref");
			String taboo = request.getParameter("taboo");
			String addr = request.getParameter("addr");
			String comment = request.getParameter("comment");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Member.UpdateBuilder ub = new Member.UpdateBuilder(Integer.valueOf(id), 
				staff.getRestaurantId(), 
				name, 
				mobile, 
				Integer.valueOf(memberTypeId), 
				Member.Sex.valueOf(Integer.valueOf(sex))
			);
			ub.setBirthday(birthday)
			  .setMemberCard(memberCard)
			  .setTele(tele)
			  .setIdCard(idCard)
			  .setCompany(company)
			  .setTastePref(tastePref)
			  .setTaboo(taboo)
			  .setContactAddr(addr)
			  .setComment(comment);
			
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
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
	public ActionForward charge(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String memberID = request.getParameter("memberID");
			String rechargeMoney = request.getParameter("rechargeMoney");
			String rechargeType = request.getParameter("rechargeType");
			String isPrint = request.getParameter("isPrint");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			MemberOperation mo = MemberDao.charge(staff, Integer.valueOf(memberID), Float.valueOf(rechargeMoney), ChargeType.valueOf(Integer.valueOf(rechargeType)));
			if(mo == null || mo.getId() == 0){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 会员充值未成功, 未知错误, 请联系客服人员.");
			}else{
				jobject.initTip(true, "操作成功, 会员充值成功.");
				if(isPrint != null && Boolean.valueOf(isPrint)){
					try{
						ReqPrintContent reqPrintContent = ReqPrintContent.buildReqPrintMemberReceipt(staff, mo.getId());
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
						jobject.setMsg(jobject.getMsg() + " 打印操作请求失败, 请联系客服人员.");
					}
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
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
}
