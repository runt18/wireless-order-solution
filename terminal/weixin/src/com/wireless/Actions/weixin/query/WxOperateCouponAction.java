package com.wireless.Actions.weixin.query;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.MemberDao;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.CouponOperationDao;
import com.wireless.db.promotion.CouponDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.member.WxMember;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.staffMgr.Staff;

public class WxOperateCouponAction extends DispatchAction{
	
	public ActionForward getDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String fromId = request.getParameter("fid");
		final String oid = request.getParameter("oid");
		final String operate = request.getParameter("operate");
		final String associateId = request.getParameter("associateId");
		final String limit = request.getParameter("limit");
		
		final JObject jObject = new JObject();
		try{
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			final WxMember wxMember = WxMemberDao.getBySerial(staff, oid);
			
			final CouponOperationDao.ExtraCond extraCond = new CouponOperationDao.ExtraCond().setMember(wxMember.getMemberId());
			if(operate != null && !operate.isEmpty()){
				for(String each : operate.split(",")){
					if(each.equalsIgnoreCase("order_use")){
						extraCond.addOperation(CouponOperation.Operate.ORDER_USE);
					}else if(each.equalsIgnoreCase("order_issue")){
						extraCond.addOperation(CouponOperation.Operate.ORDER_ISSUE);
					}
				}
			}
			
			if(associateId != null && !associateId.isEmpty()){
				extraCond.setAssociateId(Integer.parseInt(associateId));
			}
			
			List<CouponOperation> result = CouponOperationDao.getByCond(staff, extraCond);
			
			if(limit != null && !limit.isEmpty()){
				if(result.size() > Integer.parseInt(limit)){
					result = result.subList(0, Integer.parseInt(limit));
				}
			}
			
			Collections.sort(result, CouponOperation.BY_DATE);
			
			jObject.setRoot(result);
			
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		final String fromId = request.getParameter("fid");
		final String oid = request.getParameter("oid");
		final String couponId = request.getParameter("cid");
		final String promotionId = request.getParameter("pid");
		final String status = request.getParameter("status");
		final String filter = request.getParameter("filter");
		
		JObject jObject = new JObject();
		try{
			
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			CouponDao.ExtraCond extraCond = new CouponDao.ExtraCond();
			
			if(couponId != null && !couponId.isEmpty()){
				extraCond.setId(Integer.parseInt(couponId));
			}
			
			if(status != null && !status.isEmpty()){
				if(status.equalsIgnoreCase("issued")){
					extraCond.setStatus(Coupon.Status.ISSUED);
				}else if(status.equals("used")){
					extraCond.setStatus(Coupon.Status.USED);
				}
			}
			
			if(promotionId != null && !promotionId.isEmpty()){
				extraCond.setPromotion(Integer.parseInt(promotionId));
			}
			
			if(oid != null && !oid.isEmpty()){
				extraCond.setMember(MemberDao.getByWxSerial(staff, oid));
			}
			
			if(filter != null && !filter.isEmpty()){
				extraCond.setFilter(ExtraCond.Filter.valueOf(filter));
			}
			
			final List<Coupon> result = CouponDao.getByCond(staff, extraCond, null);
			for(Coupon coupon : result){
				if(coupon.getCouponType().hasImage()){
					coupon.getCouponType().setImage(OssImageDao.getById(staff, coupon.getCouponType().getImage().getId()));
				}
			}
			
			jObject.setRoot(result);
			
		}catch(SQLException e){
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
	
	public ActionForward getById(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		String fromId = request.getParameter("fid");
		
		final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
		final Staff staff = StaffDao.getAdminByRestaurant(rid);
		
		JObject jobject = new JObject();
		try{
			jobject.setRoot(CouponDao.getById(staff, Integer.parseInt(request.getParameter("cid"))));
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
	
}
