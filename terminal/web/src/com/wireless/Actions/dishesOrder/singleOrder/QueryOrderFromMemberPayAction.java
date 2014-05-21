package com.wireless.Actions.dishesOrder.singleOrder;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.coupon.CouponDao;
import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.coupon.Coupon;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;

public class QueryOrderFromMemberPayAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			String orderID = request.getParameter("orderID");
			String discountId = request.getParameter("discountId");
			String couponId = request.getParameter("couponId");
			String st = request.getParameter("st");
			String sv = request.getParameter("sv");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final Member m;
			if(st != null && st.trim().equals("mobile")){
				m = MemberDao.getByMobile(staff, sv);
			}else if(st != null && st.trim().equals("card")){
				m = MemberDao.getByCard(staff, sv);				
			}else{
				throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
			}
			
			Order.PayBuilder payBuilder = Order.PayBuilder.build4Member(Integer.valueOf(orderID), m, Order.PayType.CASH);
			if(discountId != null && !discountId.trim().isEmpty()){
				payBuilder.setDiscountId(Integer.valueOf(discountId));
			}else{
				payBuilder.setDiscountId(m.getMemberType().getDefaultDiscount().getId());
			}
			
			if(couponId != null && !couponId.trim().isEmpty() && !couponId.equals("-1")){
				payBuilder.setCouponId(Integer.parseInt(couponId));
			}
			final Order order = PayOrder.calc(staff, payBuilder);
			
			final List<Coupon> coupons = CouponDao.getAvailByMember(staff, m.getId());
			
			jobject.setExtra(new Jsonable(){

				@Override
				public Map<String, Object> toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("member", m, 0);
					jm.putJsonable("newOrder", order, 0);
					jm.putJsonableList("coupons", coupons, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			
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

}
