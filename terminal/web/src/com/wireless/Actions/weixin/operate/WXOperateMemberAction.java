package com.wireless.Actions.weixin.operate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.sms.VerifySMSDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WeixinMemberError;
import com.wireless.json.JObject;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.sms.VerifySMS;
import com.wireless.pojo.sms.VerifySMS.ExpiredPeriod;
import com.wireless.pojo.sms.VerifySMS.InsertBuilder;
import com.wireless.pojo.sms.VerifySMS.VerifyBuilder;

public class WXOperateMemberAction extends DispatchAction {
	
	/**
	 * 获取微信会员信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getInfo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		DBCon dbCon = null;
		try{
			String openId = request.getParameter("oid");
			String formId = request.getParameter("fid");
			dbCon = new DBCon();
			dbCon.connect();
			int mid = WeixinMemberDao.getBoundMemberIdByWeixin(dbCon, openId, formId);
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
			Restaurant restaurant = RestaurantDao.getById(dbCon, rid);
			Member member = MemberDao.getMemberById(dbCon, StaffDao.getStaffs(dbCon, rid).get(0), mid);
			jobject.initTip(true, "操作成功, 已获取微信会员信息.");
			jobject.getOther().put("member", member);
			jobject.getOther().put("restaurant", restaurant);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 获取短信验证码
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getVerifyCode(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		DBCon dbCon = null;
		try{
			String mobile = request.getParameter("mobile");
			dbCon = new DBCon();
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			InsertBuilder builder = new InsertBuilder(ExpiredPeriod.MINUTE_10);
			int lastId = VerifySMSDao.insert(dbCon, builder);
			VerifySMS sms = VerifySMSDao.getById(dbCon, lastId);
			//
			String formId = request.getParameter("fid");
			Restaurant restaurant = RestaurantDao.getById(WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, formId));
			com.wireless.util.sms.CodeSMS.Status ss = com.wireless.util.sms.CodeSMS.send(mobile, sms.getCode(), restaurant.getName());
//			com.wireless.util.sms.TextSMS.Status ss = com.wireless.util.sms.TextSMS.sendCodeSMS(mobile, sms.getCode(), restaurant.getName());
			if(ss.isSuccess()){
				dbCon.conn.commit();
				jobject.initTip(true, "操作成功, 已发送短信验证码, 请注意查看.");
				jobject.getOther().put("code", sms);
			}else{
				dbCon.conn.rollback();
				System.out.println(ss.getMsg());
				jobject.initTip(false, "操作失败, 未知错误, 请稍候再试.");
			}
		}catch(BusinessException e){
			dbCon.conn.rollback();
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			dbCon.conn.rollback();
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward bind(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		DBCon dbCon = null;
		try{
			String openId = request.getParameter("oid");
			String formId = request.getParameter("fid");
			String codeId = request.getParameter("codeId");
			String code = request.getParameter("code");
			
			dbCon = new DBCon();
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			// 验证验证码
			VerifySMSDao.verify(dbCon, new VerifyBuilder(Integer.valueOf(codeId), Integer.valueOf(code)));
			
			// 绑定会员信息
			int mid = 0;
			try{
				mid = WeixinMemberDao.getBoundMemberIdByWeixin(dbCon, openId, formId);
			}catch(BusinessException e){
				if(e.getCode() == WeixinMemberError.WEIXIN_MEMBER_NOT_BOUND.getCode()){
					int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
					WeixinMemberDao.bindNewMember(dbCon, 
						new Member.InsertBuilder(
							restaurantId,
							request.getParameter("name"),
							request.getParameter("mobile"),
							MemberTypeDao.getWeixinMemberType(dbCon, StaffDao.getStaffs(dbCon, WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, formId)).get(0)).getTypeId(), 
							Member.Sex.valueOf(Integer.valueOf(request.getParameter("sex")))
						), 
						openId, 
						formId
					);
				}else{
					WeixinMemberDao.bindExistMember(dbCon, mid, openId, formId);
				}
			}catch(Exception e){
				throw e;
			}
			dbCon.conn.commit();
			jobject.initTip(true, "操作成功, 已绑定会员信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
