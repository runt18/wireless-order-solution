package com.wireless.Actions.billHistory;

import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.VerifyPin;
import com.wireless.db.billStatistics.QuerySaleDetails;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.protocol.Terminal;

@SuppressWarnings({ "unused", "rawtypes" , "unchecked"})
public class SalesSubStatisticsAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/json; charset=utf-8");
		
		DBCon dbCon = new DBCon();
		SalesDetail[] saleDetails = {};
		List itemsList = new ArrayList();
		String limit = request.getParameter("limit");
		String start = request.getParameter("start");
		Integer index = Integer.parseInt(start);
		Integer pageSize = Integer.parseInt(limit);
		try{
			dbCon.connect();
			
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
			String pin = request.getParameter("pin");
			String restaurantId = request.getParameter("restaurantID");			
			String dateBeg = request.getParameter("dateBeg");
			String dataEnd = request.getParameter("dataEnd");
			String queryType = request.getParameter("queryType");
			String orderType = request.getParameter("orderType");
			String deptID = request.getParameter("deptID");
			
			pin = pin != null && pin.length() > 0 ? pin.trim() : "";
			restaurantId = restaurantId != null && restaurantId.length() > 0 ? restaurantId.trim() : "";
			dateBeg = dateBeg != null && dateBeg.length() > 0 ? dateBeg.trim() + " 00:00:00" : "";
			dataEnd = dataEnd != null && dataEnd.length() > 0 ? dataEnd.trim() + " 23:59:59" : "";
			queryType = queryType != null && queryType.length() > 0 ? queryType.trim() : "0";
			orderType = orderType != null && orderType.length() > 0 ? orderType.trim() : "0";
			deptID = deptID != null && deptID.length() > 0 ? deptID.trim() : "-1";
			
			Integer qt = Integer.valueOf(queryType), ot = Integer.valueOf(orderType);
			
			if(qt == QuerySaleDetails.QUERY_BY_DEPT){
				saleDetails = QuerySaleDetails.execByDept(dbCon, 
	  					VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF), 
	  					dateBeg, 
	  					dataEnd);	
			}else if(qt == QuerySaleDetails.QUERY_BY_FOOD){
				String[] splitDeptID = deptID.split(",");
				int[] did = new int[splitDeptID.length];
				for(int i = 0; i < splitDeptID.length; i++){
					did[i] = Integer.parseInt(splitDeptID[i]);
				}
				if(did.length == 1 && did[0] == -1){
					did = new int[0];
				}
				saleDetails = QuerySaleDetails.execByFood(dbCon, 
	  					VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF), 
	  					dateBeg, 
	  					dataEnd,
	  					did,
	  					ot);
			}
					
			
		} catch(SQLException e){
			e.printStackTrace();
			
		} finally{
			dbCon.disconnect();
			JSONArray json = null;
			int totalProperty = saleDetails.length;
			if(index != null && pageSize != null){
				pageSize = (pageSize + index) > saleDetails.length ? (pageSize - ((pageSize + index) - saleDetails.length)) : pageSize;
				for(int i = 0; i < pageSize; i++){
					itemsList.add(saleDetails[index + i]);
				}
				
			}else{
				itemsList = Arrays.asList(saleDetails);
			}
			
			if(totalProperty > 0){
				totalProperty++;
				SalesDetail sum = new SalesDetail("汇总");
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
			json = JSONArray.fromObject(itemsList);
//			System.out.println("{totalProperty:" + saleDetails.length + ", root:" + json.toString() + "}");			
			response.getWriter().print("{totalProperty:" + totalProperty + ", root:" + json.toString() + "}");
		}
		
		return null;
	}
	
	
}
