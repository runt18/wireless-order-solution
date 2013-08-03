package com.wireless.Actions.orderMgr;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.staffMgr.Staff;

public class QueryDetailAction2 extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		DBCon dbCon = new DBCon();
		
		int orderID = 0;
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			/**
			 * The parameters looks like below.
			 * pin=0x1 & orderID=40
			 */
			String pin = request.getParameter("pin");
			
			orderID = Integer.parseInt(request.getParameter("orderID"));
			
			dbCon.connect();
			
			Staff term = StaffDao.verify(Integer.parseInt(pin));
			
			int nCount = 0;
			StringBuffer value = new StringBuffer();
			
			String sql = "SELECT a.*, b.name AS kitchen_name FROM " + Params.dbName + ".order_food a, " + Params.dbName + ".kitchen b " +
						 "WHERE order_id=" + orderID + " AND a.kitchen=b.alias_id AND b.restaurant_id=" + term.getRestaurantId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				// the string is separated by comma
				if(nCount != 0){
					value.append("，");
				}
				
				/**
				 * The json to each order detail looks like below
				 * [日期,名称,单价,数量,折扣,口味,口味价钱,厨房,服务员,备注]
				 */
				String jsonOrderDetail = "[$(order_date),$(food_name),$(unit_price),$(amount),$(discount)," +
										 "$(taste_pref),$(taste_price),$(kitchen),$(waiter),$(comment)]";
				jsonOrderDetail = jsonOrderDetail.replace("$(order_date)", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbCon.rs.getTimestamp("order_date")));
				jsonOrderDetail = jsonOrderDetail.replace("$(food_name)", dbCon.rs.getString("name"));
				jsonOrderDetail = jsonOrderDetail.replace("$(unit_price)", Float.toString(dbCon.rs.getFloat("unit_price")));
				jsonOrderDetail = jsonOrderDetail.replace("$(amount)", Float.toString(dbCon.rs.getFloat("order_count")));
				jsonOrderDetail = jsonOrderDetail.replace("$(discount)", Float.toString(dbCon.rs.getFloat("discount")));
				jsonOrderDetail = jsonOrderDetail.replace("$(taste_pref)", dbCon.rs.getString("taste").replaceAll(",", "；"));
				jsonOrderDetail = jsonOrderDetail.replace("$(taste_price)", Float.toString(dbCon.rs.getFloat("taste_price")));
				jsonOrderDetail = jsonOrderDetail.replace("$(kitchen)", dbCon.rs.getString("kitchen_name"));
				jsonOrderDetail = jsonOrderDetail.replace("$(waiter)", dbCon.rs.getString("waiter"));
				String comment = dbCon.rs.getString("comment");
				jsonOrderDetail = jsonOrderDetail.replace("$(comment)", comment != null ? comment : "");
				// put each json order info to the value
				value.append(jsonOrderDetail);
				
				nCount++;
				
			}
			if(nCount == 0){
				jsonResp = jsonResp.replace("$(value)", "");
			}else{
				jsonResp = jsonResp.replace("$(value)", value);
			}
			dbCon.rs.close();
			
			jsonResp = jsonResp.replace("$(result)", "true");
			
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");		
			if(e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED){
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");	
				
			}else if(e.getErrCode() == ProtocolError.TERMINAL_EXPIRED){
				jsonResp = jsonResp.replace("$(value)", "终端已过期，请重新确认");	
				
			}else{
				jsonResp = jsonResp.replace("$(value)", "没有获取到账单(id=" + orderID + ")的详细信息，请重新确认");	
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}catch(IOException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}finally{
			dbCon.disconnect();
			//just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}
		
		return null;
	}
}
