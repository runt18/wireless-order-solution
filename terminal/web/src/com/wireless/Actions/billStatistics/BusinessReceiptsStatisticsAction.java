package com.wireless.Actions.billStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.billStatistics.BusinessStatisticsDao;
import com.wireless.pojo.billStatistics.BusinessStatistics;
import com.wireless.util.DataPaging;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class BusinessReceiptsStatisticsAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		JObject jobject = new JObject();
		List<BusinessStatistics> root = null;
		try{
			String pin = (String) request.getSession().getAttribute("pin");
			String restaurantID = request.getParameter("restaurantID");
			String onDuty = request.getParameter("dateBegin");
			String offDuty = request.getParameter("dateEnd");
			Map<String, Object> params = new HashMap<String, Object>();
			
			params.put("pin", pin);
			params.put("restaurantID", restaurantID);
			params.put("onDuty", onDuty);
			params.put("offDuty", offDuty);
			root = BusinessStatisticsDao.getBusinessReceiptsStatisticsByHistory(params);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				
				BusinessStatistics sum = new BusinessStatistics();
				for(BusinessStatistics temp : root){
					sum.setCashAmount(sum.getCashAmount() + temp.getCashAmount());
					sum.setCashIncome2(sum.getCashIncome2() + temp.getCashIncome2());
					sum.setCreditCardAmount(sum.getCreditCardAmount() + temp.getCreditCardAmount());
					sum.setCreditCardIncome2(sum.getCreditCardIncome2() + temp.getCreditCardIncome2());
					sum.setSignAmount(sum.getSignAmount() + temp.getSignAmount());
					sum.setSignIncome2(sum.getSignIncome2() + temp.getSignIncome2());
					sum.setHangAmount(sum.getHangAmount() + temp.getHangAmount());
					sum.setHangIncome2(sum.getHangIncome2() + temp.getHangIncome2());
					
					sum.setEraseAmount(sum.getEraseAmount() + temp.getEraseAmount());
					sum.setEraseIncome(sum.getEraseIncome() + temp.getEraseIncome());
					sum.setDiscountAmount(sum.getDiscountAmount() + temp.getDiscountAmount());
					sum.setDiscountIncome(sum.getDiscountIncome() + temp.getDiscountIncome());
					sum.setGiftAmount(sum.getGiftAmount() + temp.getGiftAmount());
					sum.setGiftIncome(sum.getGiftIncome() + temp.getGiftIncome());
					sum.setCancelAmount(sum.getCancelAmount() + temp.getCancelAmount());
					sum.setCancelIncome(sum.getCancelIncome() + temp.getCancelIncome());
					sum.setPaidIncome(sum.getPaidIncome() + temp.getPaidIncome());
					
					sum.setTotalPrice(sum.getTotalPrice() + temp.getTotalPrice());
					sum.setTotalPrice2(sum.getTotalPrice2() + temp.getTotalPrice2());
					
					sum.setOrderAmount(sum.getOrderAmount() + temp.getOrderAmount());
				}
				root = DataPaging.getPagingData(root, isPaging, start, limit);
				root.add(sum);
				jobject.setRoot(root);
			}
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
}
