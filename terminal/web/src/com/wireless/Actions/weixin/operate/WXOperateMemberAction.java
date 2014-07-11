package com.wireless.Actions.weixin.operate;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberDao.MemberRank;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.sms.VerifySMSDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.sms.VerifySMS;
import com.wireless.pojo.sms.VerifySMS.ExpiredPeriod;
import com.wireless.pojo.sms.VerifySMS.InsertBuilder;
import com.wireless.pojo.sms.VerifySMS.VerifyBuilder;
import com.wireless.util.sms.SMS;

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
	public ActionForward getInfo(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			String openId = request.getParameter("oid");
			String formId = request.getParameter("fid");
			dbCon.connect();
			int mid = WeixinMemberDao.getBoundMemberIdByWeixin(dbCon, openId, formId);
			int rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
			final Restaurant restaurant = RestaurantDao.getById(dbCon, rid);
			
			final Member member = MemberDao.getById(dbCon, StaffDao.getStaffs(dbCon, rid).get(0), mid);
			MemberRank mr = MemberDao.calcMemberRank(StaffDao.getStaffs(dbCon, rid).get(0), mid);
			DecimalFormat df = new DecimalFormat("#.00");
			final String rank = df.format(mr.getRank()/mr.getTotal()) + "%";
			
			jobject.initTip(true, "操作成功, 已获取微信会员信息.");
			
			final Jsonable j = new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable(member, 0);
					jm.putString("rank", rank);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			};
			
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("member", j, 0);
					jm.putJsonable("restaurant", restaurant, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			
			
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
			dbCon.disconnect();
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
	public ActionForward getVerifyCode(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			String mobile = request.getParameter("mobile");
			String fromId = request.getParameter("fid");

			final VerifySMS sms = VerifySMSDao.getById(dbCon, VerifySMSDao.insert(dbCon, new InsertBuilder(ExpiredPeriod.MINUTE_10)));
			int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, fromId);
			SMS.send(dbCon, StaffDao.getAdminByRestaurant(restaurantId), mobile, new SMS.Msg4Verify(sms.getCode()));
			dbCon.conn.commit();
			
			jobject.initTip(true, "操作成功, 已发送短信验证码, 请注意查看.");
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("code", sms, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			
		}catch(BusinessException e ){
			dbCon.conn.rollback();
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(IOException e){
			dbCon.conn.rollback();
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(Exception e){
			dbCon.conn.rollback();
			e.printStackTrace();
			jobject.initTip(e);
			
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward bind(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			String openId = request.getParameter("oid");
			String formId = request.getParameter("fid");
			String codeId = request.getParameter("codeId");
			String code = request.getParameter("code");
			
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			// 验证验证码
			VerifySMSDao.verify(dbCon, new VerifyBuilder(Integer.valueOf(codeId), Integer.valueOf(code)));
			
			WeixinMemberDao.bind(request.getParameter("mobile"), openId, formId);
			
			dbCon.conn.commit();
			jobject.initTip(true, "操作成功, 已绑定会员信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 重新绑定手机号码
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward rebind(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			String openId = request.getParameter("oid");
			String formId = request.getParameter("fid");
			String codeId = request.getParameter("codeId");
			String code = request.getParameter("code");
			
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			// 验证验证码
			VerifySMSDao.verify(dbCon, new VerifyBuilder(Integer.valueOf(codeId), Integer.valueOf(code)));
			// 修改手机号码
			WeixinMemberDao.updateMobile(dbCon, request.getParameter("mobile"), openId, formId);
			
			dbCon.conn.commit();
			jobject.initTip(true, "操作成功, 已重新绑定手机号码.");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
