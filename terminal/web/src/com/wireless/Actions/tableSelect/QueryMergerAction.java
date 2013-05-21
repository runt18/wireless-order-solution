package com.wireless.Actions.tableSelect;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.protocol.Terminal;

public class QueryMergerAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)throws Exception {
		
		PrintWriter out = null;

		String jsonResp = "{success:$(result), data:'$(value)'}";
		DBCon dbCon = new DBCon();
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			String pin = request.getParameter("pin");
			
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
			
			String sql = "SELECT table_alias, table2_alias FROM " + 
						 Params.dbName + 
						 ".order WHERE category=" + Order.Category.MERGER_TBL.getVal() +
						 " AND restaurant_id=" + term.restaurantID +
						 " AND total_price IS NULL";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			/**
			 * The json format of table merger looks like below.
			 * ["主餐台号1","副餐台号1"]，["主餐台号2","副餐台号2"]
			 */
			StringBuffer value = new StringBuffer();
			while(dbCon.rs.next()){
				// the string is separated by comma
				if(value.length() != 0){
					value.append("，");
				}
				String jsonMergerTable = "[$(major),$(minor)]";
				jsonMergerTable = jsonMergerTable.replace("$(major)", Integer.toString(dbCon.rs.getInt("table_alias")));
				jsonMergerTable = jsonMergerTable.replace("$(minor)", Integer.toString(dbCon.rs.getInt("table2_alias")));
				// put each json merger table info to the value
				value.append(jsonMergerTable);
			}
			
			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", value);
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}finally{
			
			dbCon.disconnect();
			//Just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}
		return null;
	}
	
}
