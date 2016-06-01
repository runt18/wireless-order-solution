package com.wireless.Actions.dishesOrder.singleOrder;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.member.MemberDao;
import com.wireless.db.orderMgr.PayOrder;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.staffMgr.Staff;

public class QueryOrderFromMemberPayAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String orderID = request.getParameter("orderID");
			//String pricePlanId = request.getParameter("pricePlanId");
			String sv = request.getParameter("sv");
			//0: 模糊搜索, 1 : 根据手机号, 2: 微信卡号, 3:实体卡号
			String s_type = request.getParameter("st");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			

			
			List<Member> membersByType = null;
			
			MemberDao.ExtraCond extra = new MemberDao.ExtraCond();
			
			if(sv != null && !sv.trim().isEmpty()){
				
				if(s_type == null || s_type.isEmpty() || Integer.parseInt(s_type) == 0){
					extra.setFuzzyName(sv);
					membersByType = MemberDao.getByCond(staff, extra, null);					
				}else if(Integer.parseInt(s_type) == 1){
					extra.setMobile(sv);
					membersByType = MemberDao.getByCond(staff, extra, null);
				}else if(Integer.parseInt(s_type) == 2){
					extra.setWeixinCard(sv);
					membersByType = MemberDao.getByCond(staff, extra, null);
				}else if(Integer.parseInt(s_type) == 3){
					extra.setCard(sv);
					membersByType = MemberDao.getByCond(staff, extra, null);
				}

				if(membersByType.isEmpty()){
					throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
				}
			}else{
				throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
			}
			membersByType.set(0, MemberDao.getById(staff, membersByType.get(0).getId()));
//			int discount = 0;
//			if(discountID != null && !discountID.isEmpty()){
//				discount = Integer.parseInt(discountID);
//			}
			Order.PayBuilder payBuilder;
			PayType payType;
			if(membersByType.get(0).getMemberType().getAttribute() == MemberType.Attribute.POINT){
				payType = PayType.CASH;
			}else{
				payType = PayType.MEMBER;
			}
			
			payBuilder = Order.PayBuilder.build4Member(Integer.valueOf(orderID), payType).setSms(false);
			
			
//			if(pricePlanId != null && !pricePlanId.trim().isEmpty() && !pricePlanId.equals("-1")){
//				payBuilder.setPricePlanId(Integer.parseInt(pricePlanId));
//			}else{
//				if(membersByType.get(0).getMemberType().getDefaultPrice() != null){
//					payBuilder.setPricePlanId(membersByType.get(0).getMemberType().getDefaultPrice().getId());
//				}				
//			}
			
			final Order order = PayOrder.calc(staff, payBuilder);
			
			final List<Member> members = membersByType;
			
			final List<Coupon> coupons = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(members.get(0).getId()).setStatus(Coupon.Status.ISSUED), null);
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();

					if(members.size() > 1){
						jm.putJsonableList("members", members, 0);
					}else{
						jm.putJsonable("member", members.get(0), 0);					
					}
					jm.putJsonable("newOrder", order, 0);
					if(!coupons.isEmpty()){
						jm.putJsonableList("coupons", coupons, Coupon.COUPON_JSONABLE_SIMPLE);
					}
					
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
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
