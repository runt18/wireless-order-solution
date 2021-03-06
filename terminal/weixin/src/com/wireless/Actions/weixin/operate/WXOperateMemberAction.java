package com.wireless.Actions.weixin.operate;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.marker.weixin.api.Token;
import org.marker.weixin.api.User;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.auth.AuthorizerToken;

import com.wireless.db.DBCon;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberDao.MemberRank;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.PayOrder;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.exception.WxRestaurantError;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.listener.SessionListener;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.WxMember;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

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
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		final JObject jObject = new JObject();
		String openId = request.getParameter("oid");
		String fromId = request.getParameter("fid");
		String branchId = request.getParameter("branchId");
		final String sessionId = request.getParameter("sessionId");
		final String memberId = request.getParameter("memberId");
		try{
			
			if(sessionId != null && !sessionId.isEmpty()){
				HttpSession session = SessionListener.sessions.get(sessionId);
				if(session != null){
					openId = (String)session.getAttribute("oid");
					fromId = (String)session.getAttribute("fid");
					branchId = (String)session.getAttribute("branchId");
				}else{
					throw new BusinessException(WxRestaurantError.WEIXIN_SESSION_TIMEOUT);
				}
			}
			
			final int rid;
			if(branchId != null && !branchId.isEmpty()){
				rid = Integer.parseInt(branchId);
			}else{
				rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			}
			
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			final MemberDao.ExtraCond extraCond = new MemberDao.ExtraCond();
			if(memberId != null && !memberId.isEmpty()){
				extraCond.setId(Integer.parseInt(memberId));
			}else{
				extraCond.setWeixinSerial(openId);
			}
			
			List<Member> result = MemberDao.getByCond(staff, extraCond, null);
			if(result.isEmpty()){
				throw new BusinessException("对不起，没有找到此会员", MemberError.MEMBER_NOT_EXIST);
			}else{
				jObject.setRoot(MemberDao.getById(staff, result.get(0).getId()));
			}
			
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
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
		String openId = request.getParameter("oid");
		String fromId = request.getParameter("fid");
		int rid = 0;
		try{

			rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			final Restaurant restaurant = RestaurantDao.getById(rid);
			
			final Member member = MemberDao.getByWxSerial(staff, openId);
			
			MemberRank mr = MemberDao.calcMemberRank(StaffDao.getAdminByRestaurant(rid), member.getId());
			
			final int rank = ((mr.getTotal() - mr.getRank()) * 100)/mr.getTotal();
			
			jobject.initTip(true, "操作成功, 已获取微信会员信息.");
			
			final Jsonable j = new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable(member, 0);
					jm.putInt("rank", rank);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			};				
			
			final List<Coupon> couponList = CouponDao.getByCond(StaffDao.getAdminByRestaurant(rid), new CouponDao.ExtraCond().setMember(member.getId()).setStatus(Coupon.Status.ISSUED), null);
			
			final int weixinCard = member.getWeixin().getCard();
			
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("member", j, 0);
					jm.putJsonable("restaurant", restaurant, 0);
					jm.putInt("status", member.getMobile() != null && !member.getMobile().isEmpty()?WxMember.Status.BOUND.getVal():WxMember.Status.INTERESTED.getVal());
					jm.putInt("weixinCard", weixinCard);
					if(!couponList.isEmpty()){
						jm.putBoolean("hasCoupon", true);
					}
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			
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
	 * 完善会员资料
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
		
		String openId = request.getParameter("oid");
		String fromId = request.getParameter("fid");
		String branchId = request.getParameter("branchId");
		final String mobile = request.getParameter("mobile");
		final String name = request.getParameter("name");
		final String birthday = request.getParameter("birthday");
		final String age = request.getParameter("age");
		final String sex = request.getParameter("sex");				
		final String sessionId = request.getParameter("sessionId");
		final JObject jObject = new JObject();
		try{
			if(sessionId != null && !sessionId.isEmpty()){
				HttpSession session = SessionListener.sessions.get(sessionId);
				if(session != null){
					openId = (String)session.getAttribute("oid");
					fromId = (String)session.getAttribute("fid");
					branchId = (String)session.getAttribute("branchId");
				}else{
					throw new BusinessException(WxRestaurantError.WEIXIN_SESSION_TIMEOUT);
				}
			}
			
			final int rid;
			if(branchId != null && !branchId.isEmpty()){
				rid = Integer.parseInt(branchId);
			}else{
				rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			}
			
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			final WxMember.BindBuilder builder = new WxMember.BindBuilder(openId, mobile);
			
			if(name != null && !name.isEmpty()){
				builder.setName(name);
			}
			
			if(birthday != null && !birthday.isEmpty()){
				builder.setBirthday(Member.Age.valueOf(Integer.parseInt(age)).suffix + "-" + birthday);
			}
			
			if(age != null && !age.isEmpty()){
				builder.setAge(Member.Age.valueOf(Integer.parseInt(age)));
			}
			
			if(sex != null && !sex.isEmpty()){
				builder.setSex(Member.Sex.valueOf(Integer.parseInt(sex)));
			}
			
			WxMemberDao.bind(staff, builder);
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}finally{
			response.getWriter().print(jObject.toString());
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
	public ActionForward inpour(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			String openId = request.getParameter("oid");
			String fromId = request.getParameter("fid");
			String orderId = request.getParameter("orderId");
			
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, fromId);
			final Restaurant rest = RestaurantDao.getById(rid);
			Staff staff = StaffDao.getAdminByRestaurant(rid);
			WxMember wxMember;
			if(!WxMemberDao.getByCond(dbCon, staff, new WxMemberDao.ExtraCond().setSerial(openId)).isEmpty()){
				wxMember = WxMemberDao.getByCond(dbCon, staff, new WxMemberDao.ExtraCond().setSerial(openId)).get(0);
			}else{
				throw new BusinessException("查找会员失败, 请重新关注本餐厅");
			}
			
			final Member member = MemberDao.getById(staff, wxMember.getMemberId());
			
			Order.DiscountBuilder builder = Order.DiscountBuilder.build4Member(Integer.parseInt(orderId), member);
			
			OrderDao.discount(staff, builder);
			
			final Order order = PayOrder.calc(staff, Order.PayBuilder.build4Normal(Integer.valueOf(orderId)));
			
			jobject.setExtra(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("order", order, flag);
					jm.putJsonable("member", member, flag);
					jm.putString("restName", rest.getName());
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});
			
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}	
	
	
	public ActionForward afterInpour(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			String openId = request.getParameter("oid");
			String fromId = request.getParameter("fid");
			String orderId = request.getParameter("orderId");
			
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, fromId);
			final Restaurant rest = RestaurantDao.getById(rid);
			Staff staff = StaffDao.getAdminByRestaurant(rid);
			WxMember wxMember;
			if(!WxMemberDao.getByCond(dbCon, staff, new WxMemberDao.ExtraCond().setSerial(openId)).isEmpty()){
				wxMember = WxMemberDao.getByCond(dbCon, staff, new WxMemberDao.ExtraCond().setSerial(openId)).get(0);
			}else{
				throw new BusinessException("查找会员失败, 请重新关注本餐厅");
			}
			
			final Member member = MemberDao.getById(staff, wxMember.getMemberId());
			
			final Order order = PayOrder.calc(staff, Order.PayBuilder.build4Normal(Integer.valueOf(orderId)));
			
			jobject.setExtra(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("order", order, flag);
					jm.putJsonable("member", member, flag);
					jm.putString("restName", rest.getName());
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});
			
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}		
		return null;
	}
	
	//获取微信用户资料
	public ActionForward getUserMsg(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		final String fromId = request.getParameter("fid");
		final String openId = request.getParameter("oid");
		JObject jobject = new JObject();
		
		try{
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			final WxRestaurant wxRestaurant = WxRestaurantDao.get(staff);
			final AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken());
			final Token token = Token.newInstance(authorizerToken);
			
			final User user = User.newInstance(token, openId);
			
			jobject.setRoot(user);
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}finally{
			response.getWriter().print(jobject.toString());
		}	
		
		return null;
	}
	
	
}
