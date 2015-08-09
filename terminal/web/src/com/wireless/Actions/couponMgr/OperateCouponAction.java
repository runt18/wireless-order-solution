package com.wireless.Actions.couponMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.member.MemberDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.promotion.Coupon;

public class OperateCouponAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String typeId = request.getParameter("typeId");
		String memberTypes = request.getParameter("memberTypes");
		String promotionId = request.getParameter("promotionId");
		
		String pin = (String) request.getAttribute("pin");
		String[] memberType = null;
		JObject jobject = new JObject();
		try{
			Coupon.CreateBuilder builder = new Coupon.CreateBuilder(Integer.parseInt(typeId), Integer.parseInt(promotionId));
			memberType = memberTypes.split(",");
			for (int i = 0; i < memberType.length; i++) {
				for (Member m : MemberDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), new MemberDao.ExtraCond().setMemberType(Integer.parseInt(memberType[i])), null)) {
					builder.addMember(m.getId());
				}
			}
//			CouponDao.create(StaffDao.verify(Integer.parseInt(pin)), builder);
			
			jobject.initTip(true, "发放成功");
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
	
	public ActionForward draw(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String couponId = request.getParameter("couponId");
		
		String formId = request.getParameter("fid");
		int rid = 0;
		DBCon dbCon = new DBCon();
		dbCon.connect();
		rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
		
		JObject jobject = new JObject();
		try{
			CouponDao.draw(StaffDao.getByRestaurant(dbCon, rid).get(0), Integer.parseInt(couponId), Coupon.DrawType.MANUAL);
			
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
	
//	public ActionForward sendCoupon(ActionMapping mapping, ActionForm form,
//			HttpServletRequest request, HttpServletResponse response)
//			throws Exception {
//		String membersData = request.getParameter("membersData");
//		String coupon = request.getParameter("coupon");
//		
//		String pin = (String) request.getAttribute("pin");
//		String[] membersDatas = null;
//		JObject jobject = new JObject();
//		try{
//			Coupon.CreateBuilder builder = new Coupon.CreateBuilder(Integer.parseInt(coupon));
//			membersDatas = membersData.split(",");
//			for (int i = 0; i < membersDatas.length; i++) {
//				builder.addMemberId(Integer.parseInt(membersDatas[i]));
//			}
//			CouponDao.create(StaffDao.verify(Integer.parseInt(pin)), builder);
//			
///*			try{
//				//Send SMS.
//				SMS.send(StaffDao.verify(Integer.parseInt(pin)), mo.getMemberMobile(), new SMS.Msg4Charge(mo));
//				jobject.setMsg(jobject.getMsg() + "充值短信发送成功.");
//			}catch(Exception e){
//				jobject.setMsg(jobject.getMsg() + "充值短信发送失败(" + e.getMessage() + ")");
//				e.printStackTrace();
//			}*/
//			
//			jobject.initTip(true, "发放成功");
//		}catch(BusinessException e){
//			e.printStackTrace();
//			jobject.initTip(e);
//		}catch(SQLException e){
//			e.printStackTrace();
//			jobject.initTip(e);
//		}catch(Exception e){
//			e.printStackTrace();
//			jobject.initTip(e);
//		}finally{
//			response.getWriter().print(jobject.toString());
//		}
//		
//		return null;
//	}	

}
