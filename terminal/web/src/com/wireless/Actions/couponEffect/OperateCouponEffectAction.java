package com.wireless.Actions.couponEffect;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.promotion.CouponEffectDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.promotion.CouponEffect;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class OperateCouponEffectAction extends DispatchAction{
	
	/**
	 * 获取优惠活动效果分析的内容
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception 
	 */
	public ActionForward calcByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final String pin = (String)request.getAttribute("pin");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String couponTypeId = request.getParameter("couponId");
		final String branchId = request.getParameter("branchId");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final JObject jObject = new JObject();
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final CouponEffectDao.ExtraCond extraCond = new CouponEffectDao.ExtraCond();
			
			if(beginDate != null && !beginDate.isEmpty() && endDate != null && !endDate.isEmpty()){
				extraCond.setRange(beginDate, endDate);
			}
			
			if(couponTypeId != null && !couponTypeId.isEmpty()){
				extraCond.setCouponType(Integer.parseInt(couponTypeId));
			}
			
			if(branchId != null && !branchId.isEmpty()){
				extraCond.setBranchId(Integer.parseInt(branchId));
			}
			
			//获取优惠活动的记录
			List<CouponEffect> result = CouponEffectDao.calcByCond(staff, extraCond);
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				jObject.setTotalProperty(result.size());
				CouponEffect total = new CouponEffect(0);
				for(CouponEffect item : result){
					total.setIssuedAmount(item.getIssuedAmount() + total.getIssuedAmount());
					total.setIssuedPrice(item.getIssuedPrice() + total.getIssuedPrice());
					total.setUsedAmount(item.getUsedAmount() + total.getUsedAmount());
					total.setUsedPrice(item.getUsedPrice() + total.getUsedPrice());
					total.setSalesAmount(item.getSalesAmount() + total.getSalesAmount());
					total.setEffectSales(item.getEffectSales() + total.getEffectSales());
				}
				
				result = DataPaging.getPagingData(result, true, start, limit);
				result.add(total);
				
			}
			jObject.setRoot(result);
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	
	
	
}
