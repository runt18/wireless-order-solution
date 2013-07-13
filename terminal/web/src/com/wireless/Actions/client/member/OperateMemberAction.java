package com.wireless.Actions.client.member;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.Member;
import com.wireless.protocol.Terminal;
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
			String pin = request.getParameter("pin");
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
			
			Terminal term = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			
			Member.InsertBuilder ib = new Member.InsertBuilder(term.restaurantID, name, mobile, Integer.valueOf(memberTypeId), Member.Sex.valueOf(Integer.valueOf(sex)));
			ib.setBirthday(birthday)
			  .setMemberCard(memberCard)
			  .setTele(tele)
			  .setIdCard(idCard)
			  .setCompany(company)
			  .setTastePref(tastePref)
			  .setTaboo(taboo)
			  .setContactAddr(addr)
			  .setComment(comment);
			
			MemberDao.insert(term, ib);
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
			String pin = request.getParameter("pin");
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
			
			Terminal term = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			
			Member.UpdateBuilder ub = new Member.UpdateBuilder(Integer.valueOf(id), 
				term.restaurantID, 
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
			
			MemberDao.update(term, ub);
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
			String pin = request.getParameter("pin");
			String memberId = request.getParameter("memberId");
			String point = request.getParameter("point");
			String adjust = request.getParameter("adjust");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			MemberDao.adjustPoint(term, Integer.valueOf(memberId), Integer.valueOf(point), Member.AdjustType.valueOf(Integer.valueOf(adjust)));
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
			String pin = request.getParameter("pin");
			String memberId = request.getParameter("memberId");
			String point = request.getParameter("point");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			MemberDao.pointConsume(term, Integer.valueOf(memberId), Integer.valueOf(point));
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
