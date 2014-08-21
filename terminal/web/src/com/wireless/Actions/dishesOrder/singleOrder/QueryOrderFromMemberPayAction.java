package com.wireless.Actions.dishesOrder.singleOrder;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.orderMgr.PayOrder;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.client.Member;
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
			String sv = request.getParameter("sv");
			//0 : 根据手机或卡号; 1 : 根据手机号
			String s_type = request.getParameter("st");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			

			
			List<Member> membersByType;
			
			MemberDao.ExtraCond extra = new MemberDao.ExtraCond();
			
			if(sv != null && !sv.trim().isEmpty()){
				
				if(Integer.parseInt(s_type) == 0){
					extra.setMobileOrCard(sv);
					membersByType = MemberDao.getByCond(staff, extra, null);					
				}else{
					extra.setMobile(sv);
					membersByType = MemberDao.getByCond(staff, extra, null);
					
					if(membersByType.isEmpty()){
						extra.setMobile(null);
						extra.setCard(sv);
						membersByType = MemberDao.getByCond(staff, extra, null);
					}
				}

				if(membersByType.isEmpty()){
					throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
				}
			}else{
				throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
			}
			
			Order.PayBuilder payBuilder = Order.PayBuilder.build4Member(Integer.valueOf(orderID), membersByType.get(0), Order.PayType.CASH);
			if(discountId != null && !discountId.trim().isEmpty()){
				payBuilder.setDiscountId(Integer.valueOf(discountId));
			}else{
				payBuilder.setDiscountId(membersByType.get(0).getMemberType().getDefaultDiscount().getId());
			}
			
			if(couponId != null && !couponId.trim().isEmpty() && !couponId.equals("-1")){
				payBuilder.setCouponId(Integer.parseInt(couponId));
			}
			final Order order = PayOrder.calc(staff, payBuilder);
			
//			final List<Coupon> coupons = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(m.getId()).setStatus(Coupon.Status.DRAWN), null);
//			final List<Coupon> coupons = new ArrayList<>();
//			final Member m;			
			final List<Member> members = membersByType;
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();

					if(members.size() > 1){
						jm.putJsonableList("members", members, 0);
					}else{
						
						try {
							jm.putJsonable("member", MemberDao.getByMobile(staff, members.get(0).getMobile()), 0);
						} catch (SQLException | BusinessException e) {
							e.printStackTrace();
						}						
					}
					jm.putJsonable("newOrder", order, 0);
//					jm.putJsonableList("coupons", coupons, 0);
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
