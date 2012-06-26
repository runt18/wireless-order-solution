package com.wireless.Actions.billStatistics;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.VerifyPin;
import com.wireless.db.billStatistics.QuerySaleDetails;
import com.wireless.protocol.Terminal;

public class SalesSubStatisticsAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DBCon dbCon = new DBCon();
		
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
			//String pin = request.getParameter("pin");
			String pin = "244";
			QuerySaleDetails.Result[] saleDetails = QuerySaleDetails.exec(dbCon, 
								  										  VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF), 
								  										  "2012-6-1", 
								  										  "2012-6-30", 
								  										  QuerySaleDetails.QUERY_BY_DEPT, 
								  										  QuerySaleDetails.ORDER_BY_PROFIT);
			System.out.print("");
			
		} catch(SQLException e){
			e.printStackTrace();
			
		} finally{
			dbCon.disconnect();
			//JSONArray json = JSONArray.fromObject(l);
			//response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
	
}
