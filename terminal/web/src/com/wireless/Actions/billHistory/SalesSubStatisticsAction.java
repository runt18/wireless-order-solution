package com.wireless.Actions.billHistory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.billStatistics.SaleDetailsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;

public class SalesSubStatisticsAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		SalesDetail[] saleDetails = {};
		List<SalesDetail> itemsList = new ArrayList<SalesDetail>();
		String isPaging = request.getParameter("isPaging");
		String limit = request.getParameter("limit");
		String start = request.getParameter("start");
		JObject jobject = new JObject();
		String dataType = request.getParameter("dataType");
		String queryType = request.getParameter("queryType");
		try{
			/**
			 * The parameters looks like below.
			 * 1st example: 按部门查询
			 * pin=0x1 & dateBeg="2012-5-12" & dateEnd="2012-6-12" & queryType=0 & orderType=0 
			 * 
			 * 2nd example: 查询所有菜品
			 * pin=0x1 & dateBeg="2012-5-12" & dateEnd="2012-6-12" & queryType=1 & deptID=-1 & orderType=0 
			 * 
			 * pin : the pin the this terminal
			 * 
			 * dateBeg : the begin date to query
			 * 
			 * dateEnd : the end date to query
			 * 
			 * queryType : "0" means "按部门查询"
			 * 			   "1" means "按菜品查询"
			 * 
			 * deptID : the department id to query in case of "按菜品查询",
			 * 			the value less than means all the foods
			 * 
			 * orderType : "0" means "按毛利排序"
			 * 			   "1" means "按销量排序"
			 * 
			 */
			String pin = (String)request.getAttribute("pin");
			String restaurantId = (String)request.getAttribute("restaurantID");		
			String dateBeg = request.getParameter("dateBeg");
			String dateEnd = request.getParameter("dateEnd");
			String orderType = request.getParameter("orderType");
			String deptID = request.getParameter("deptID");
			String foodName = request.getParameter("foodName");
			String region = request.getParameter("region");
			
			pin = pin != null && pin.length() > 0 ? pin.trim() : "";
			restaurantId = restaurantId != null && restaurantId.length() > 0 ? restaurantId.trim() : "";
			dataType = dataType != null && dataType.length() > 0 ? dataType.trim() : "1";
			queryType = queryType != null && queryType.length() > 0 ? queryType.trim() : "0";
			orderType = orderType != null && orderType.length() > 0 ? orderType.trim() : "1";
			deptID = deptID != null && deptID.length() > 0 ? deptID.trim() : "-1";
			
			Integer qt = Integer.valueOf(queryType), ot = Integer.valueOf(orderType);
			DateType dt = DateType.valueOf(Integer.valueOf(dataType));
			
				
			if(dt.isHistory()){
				dateBeg = dateBeg != null && dateBeg.length() > 0 ? dateBeg.trim() + " 00:00:00" : "";
				dateEnd = dateEnd != null && dateEnd.length() > 0 ? dateEnd.trim() + " 23:59:59" : "";
			}
			
			String extraCond = null;
			if(region != null && !region.equals("-1")){
				extraCond = " AND O.region_id = " + region;
			}
			if(qt == SaleDetailsDao.QUERY_BY_DEPT){
				saleDetails = SaleDetailsDao.execByDept(
						StaffDao.verify(Integer.parseInt(pin)), 
	  					dateBeg, 
	  					dateEnd,
	  					dt,
	  					extraCond);
				
			}else if(qt == SaleDetailsDao.QUERY_BY_FOOD){
				String[] splitDeptID = deptID.split(",");
				int[] did = new int[splitDeptID.length];
				for(int i = 0; i < splitDeptID.length; i++){
					did[i] = Integer.parseInt(splitDeptID[i]);
				}
				if(did.length == 1 && did[0] == -1){
					did = new int[0];
				}
				saleDetails = SaleDetailsDao.execByFood(
						StaffDao.verify(Integer.parseInt(pin)), 
	  					dateBeg, 
	  					dateEnd,
	  					did,
	  					ot,
	  					dt,
	  					foodName,
	  					Integer.parseInt(region));
			}else if(qt == SaleDetailsDao.QUERY_BY_KITCHEN){
				saleDetails = SaleDetailsDao.getByKitchen(
						StaffDao.verify(Integer.parseInt(pin)), 
						dateBeg, 
						dateEnd, 
						dt,
						extraCond);
			}
				
		} catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		} finally{
			itemsList = DataPaging.getPagingData(new ArrayList<SalesDetail>(Arrays.asList(saleDetails)), isPaging, start, limit);
			
			if(queryType != null && !queryType.equals("2") && saleDetails.length > 0){
				SalesDetail sum = new SalesDetail();
				Food fb = new Food(0);
				fb.setName("汇总");
				sum.setFood(fb);
				Department dept = new Department(0);
				dept.setName("汇总");
				sum.setDept(dept);
				for(SalesDetail tp : saleDetails){
					sum.setIncome(sum.getIncome() + tp.getIncome());
					sum.setDiscount(sum.getDiscount() + tp.getDiscount());
					sum.setGifted(sum.getGifted() + tp.getGifted());
					sum.setCost(sum.getCost() + tp.getCost());
					sum.setProfit(sum.getProfit() + tp.getProfit());
					sum.setSalesAmount(sum.getSalesAmount() + tp.getSalesAmount());				
				}
				if(sum.getIncome() != 0.00){
					sum.setProfitRate(sum.getProfit() / sum.getIncome());
					sum.setCostRate(sum.getCost() / sum.getIncome());
				}
				itemsList.add(sum);
			}
			
			jobject.setTotalProperty(saleDetails.length);
			jobject.setRoot(itemsList);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
}
