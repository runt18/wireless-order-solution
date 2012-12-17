package com.wireless.Actions.orderMgr;

import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.wireless.db.orderMgr.OrderDao;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.util.JObject;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryTodayAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<Order> list = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String restaurantID = request.getParameter("restaurantID");
			String ope = request.getParameter("ope");
			String cond = request.getParameter("havingCond");
			int type = Integer.parseInt(request.getParameter("type"));
			String filterVal = request.getParameter("value");
			
			StringBuffer extra = new StringBuffer(), orderBy = new StringBuffer();
			extra.append(" AND A.restaurant_id = " + restaurantID);
			String havingCond = null, filterCondition = null;
			
			if(ope != null && !ope.trim().isEmpty()){
				int opeType = Integer.parseInt(ope);
				
				if(opeType == 1){
					ope = "=";
				}else if(opeType == 2){
					ope = ">=";
				}else if(opeType == 3){
					ope = "<=";
				}else{
					ope = "=";
				}
			}else{
				ope = "=";
			}
			
			if(cond != null && !cond.trim().isEmpty()){
				int condType = Integer.valueOf(cond);
				if(condType == 1){
					//是否有反结帐
					havingCond = " HAVING A.status = 2 ";
				}else if(condType == 2){
					//是否有折扣
					havingCond = " HAVING A.discount_price > 0 ";
				}else if(condType == 3){
					//是否有赠送
					havingCond = " HAVING A.gift_price > 0 ";
				}else if(condType == 4){
					//是否有退菜
					havingCond = " HAVING A.cancel_price > 0 ";
				}else if(condType == 5){
					//是否有抹数
					havingCond = " HAVING A.erase_price > 0 ";				
				}else{
					havingCond = "";
				}
			}
			
			if(type == 1){
				//按账单号
				filterCondition = " AND A.id" + ope + filterVal;
			}else if(type == 2){
				//按流水号
				if(havingCond.equals("")){
					havingCond = " HAVING A.seq_id " + ope + filterVal;
				} else {
					havingCond = havingCond + " AND A.seq_id " + ope + filterVal;
				}
				filterCondition = "";
			}else if(type == 3){
				//按台号
				filterCondition = " AND A.table_alias" + ope + filterVal;
			}else if(type == 4){
				//按时间
				filterCondition = " AND A.order_date" + ope + "'" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " " + filterVal + "'";
			}else if(type == 5){
				//按类型
				filterCondition = " AND A.category" + ope + filterVal;
			}else if(type == 6){
				//按结帐方式
				filterCondition = " AND A.type" + ope + filterVal;
			}else if(type == 7){
				//按金额
				filterCondition = " AND A.total_price" + ope + filterVal;
			}else if(type == 8){
				//按实收
				filterCondition = " AND A.total_price_2" + ope + filterVal;
			}else{
				filterCondition = "";
			}
			
			extra.append(filterCondition);
			extra.append(" GROUP BY A.id ");
			extra.append(havingCond);
			orderBy.append(" ORDER BY A.seq_id ASC ");		
			
			Map<String, Object> paramsSet = new HashMap<String, Object>();
			paramsSet.put(WebParams.SQL_PARAMS_EXTRA, extra);
			paramsSet.put(WebParams.SQL_PARAMS_ORDERBY, orderBy);
			list = OrderDao.getOrderByToday(paramsSet);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(list != null){
				Order sum = new Order();
				for(int i = 0; i < list.size(); i++){
					sum.setTotalPrice(sum.getTotalPrice() + list.get(i).getTotalPrice());
					sum.setActuralPrice(sum.getActuralPrice() + list.get(i).getActuralPrice());
				}
				jobject.setTotalProperty(list.size());
				list = DataPaging.getPagingData(list, isPaging, start, limit);
				list.add(sum);
				jobject.setRoot(list);
			}
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
//	public ActionForward execute(ActionMapping mapping, ActionForm form,
//			HttpServletRequest request, HttpServletResponse response)
//			throws Exception {
//		
//		DBCon dbCon = new DBCon();
//		String jsonResp = "{success:$(result), data:'$(value)'}";
//		try {
//			dbCon.connect();
//			// 解决后台中文传到前台乱码
//			response.setContentType("text/json; charset=utf-8");
//			/**
//			 * The parameters looks like below.
//			 * 1st example, filter the order whose id equals 321 
//			 * pin=0x1 & type=1 & ope=1 & value=321
//			 * 
//			 * 2nd example, filter the order date greater than or equal 2011-7-14 14:30:00
//			 * pin=0x1 & type=3 & ope=2 & value=2011-7-14 14:30:00
//			 * 
//			 * 3rd example, filter the orders have been paid before
//			 * pin=0x1 & havingCond=1
//			 * 
//			 * pin : the pin the this terminal
//			 * type : the type is one of the values below.
//			 * 		  0 - 全部显示
//			 *  	  1 - 按账单号
//			 *  	  2 - 按流水号
//			 *  	  3 - 按台号
//			 *  	  4 - 按时间
//			 *  	  5 - 按类型
//			 *  	  6 - 按结帐方式
//			 *  	  7 - 按金额
//			 *   	  8 - 按实收
//			 * ope : the operator is one of the values below.
//			 * 		  1 - 等于
//			 * 		  2 - 大于等于
//			 * 		  3 - 小于等于
//			 * value : the value to search, the content is depending on the type
//			 * havingCond : the having condition is one of the values below.
//			 * 		  0 - 无
//			 * 		  1 - 是否有反结帐
//			 * 		  2 - 是否有折扣
//			 * 		  3 - 是否有赠送
//			 * 		  4 - 是否有退菜	
//			 */
//			String pin = request.getParameter("pin");
//			
//			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
//			
//			//get the type to filter
//			int type = Integer.parseInt(request.getParameter("type"));
//			
//			//get the operator to filter
//			String ope = request.getParameter("ope");
//			if(ope != null && !ope.trim().isEmpty()){
//				int opeType = Integer.parseInt(ope);
//				
//				if(opeType == 1){
//					ope = "=";
//				}else if(opeType == 2){
//					ope = ">=";
//				}else if(opeType == 3){
//					ope = "<=";
//				}else{
//					ope = "=";
//				}
//			}else{
//				ope = "=";
//			}
//			//get the value to filter
//			String filterVal = request.getParameter("value");
//			//get the having condition to filter
//			int cond = Integer.parseInt(request.getParameter("havingCond"));
//			String havingCond = null;
//			if(cond == 1){
//				//是否有反结帐
//				havingCond = " HAVING SUM(IF(B.is_paid, 1, 0)) > 0 ";
//			}else if(cond == 2){
//				//是否有折扣
//				havingCond = " HAVING MIN(B.discount) < 1 ";
//			}else if(cond == 3){
//				//是否有赠送
//				havingCond = " HAVING SUM(B.food_status & " + Food.GIFT + ") > 0 ";
//			}else if(cond == 4){
//				//是否有退菜
//				havingCond = " HAVING MIN(B.order_count) < 0 ";
//			}else if(cond == 5){
//				//是否有抹数
//				havingCond = " HAVING MIN(A.erase_price) > 0 ";				
//			}else{
//				havingCond = "";
//			}
//			
//			//combine the operator and filter value
//			String filterCondition = null;
//			
//			if(type == 1){
//				//按账单号
//				filterCondition = " AND A.id" + ope + filterVal;
//			}else if(type == 2){
//				//按流水号
//				if(havingCond.equals("")){
//					havingCond = " HAVING MAX(A.seq_id) " + ope + filterVal;
//				} else {
//					havingCond = havingCond + " AND MAX(A.seq_id) " + ope + filterVal;
//				}
//				filterCondition = "";
//			}else if(type == 3){
//				//按台号
//				filterCondition = " AND A.table_alias" + ope + filterVal;
//			}else if(type == 4){
//				//按时间
//				filterCondition = " AND A.order_date" + ope + "'" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " " + filterVal + "'";
//			}else if(type == 5){
//				//按类型
//				filterCondition = " AND A.category" + ope + filterVal;
//			}else if(type == 6){
//				//按结帐方式
//				filterCondition = " AND A.type" + ope + filterVal;
//			}else if(type == 7){
//				//按金额
//				filterCondition = " AND A.total_price" + ope + filterVal;
//			}else if(type == 8){
//				//按实收
//				filterCondition = " AND A.total_price_2" + ope + filterVal;
//			}else{
//				filterCondition = "";
//			}
//			
//			/**
//			 * Select all the today orders matched the conditions below.
//			 * 1 - belong to this restaurant
//			 * 2 - has been paid
//			 * 3 - match extra filter condition
//			 */
//			String sql = " SELECT " +
//					 " A.id, MAX(A.seq_id) AS seq_id, " +
//					 " MAX(A.table_alias) AS table_alias, MAX(A.order_date) AS order_date, MAX(A.category) AS category, " +
//					 " MAX(A.type) AS type, MAX(A.total_price) AS total_price, MAX(A.total_price_2) AS total_price_2, " +
//					 " MAX(A.table2_alias) AS table2_alias, MAX(A.custom_num) AS custom_num, MAX(A.service_rate) AS service_rate, " +
//					 " MAX(A.gift_price) AS gift_price, MAX(A.member_id) AS member_id, MAX(A.member) AS member, " +
//					 " MAX(A.comment) AS comment, MAX(A.waiter) AS waiter, " +
//					 "(CASE WHEN MIN(B.discount) < 1 THEN 1 ELSE 0 END) AS is_discount, " +
//					 "(CASE WHEN SUM(B.food_status & " + Food.GIFT + ") <= 0 THEN 0 ELSE 1 END) AS is_gift, " +
//					 "(CASE WHEN SUM(IF(B.is_paid, 1, 0)) <= 0 THEN 0 ELSE 1 END) AS is_paid, " +
//					 "(CASE WHEN MIN(B.order_count) < 0 THEN 1 ELSE 0 END) AS is_cancel, " +
//					 "(CASE WHEN MIN(A.erase_price) > 0 THEN 1 ELSE 1 END ) AS is_erase " +
//					 " FROM " + 
//					 Params.dbName + ".order A, " +
//					 Params.dbName + ".order_food B " +
//					 " WHERE " +
//					 " A.id = B.order_id " +
//					 " AND A.restaurant_id=" + term.restaurantID + " " +
//					 " AND A.total_price IS NOT NULL " +
//					 filterCondition +
//					 " GROUP BY A.id " +
//					 havingCond +
//					 " ORDER BY seq_id ASC ";		
//			
//			dbCon.rs = dbCon.stmt.executeQuery(sql);
//			jsonResp = jsonResp.replace("$(result)", "true");
//			StringBuffer value = new StringBuffer();	
//			int nCount = 0;
//			while(dbCon.rs.next()){
//				// the string is separated by comma
//				if(nCount != 0){
//					value.append("，");
//				}
//				/**
//				 * The json to each order looks like below
//				 * ["账单号", "台号", "日期", "类型", "结帐方式", "金额", "实收", "台号2",
//				 * "就餐人数", "最低消", "服务费率", "会员编号", "会员姓名", "账单备注",
//				 * "赠券金额", "结帐类型", "折扣类型", "服务员", 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
//				 */
//				String jsonOrder = "[\"$(order_id)\",\"$(table_alias)\",\"$(order_date)\",\"$(order_cate)\"," +
//								   "\"$(pay_manner)\",\"$(total_price)\",\"$(actual_income)\"," +
//								   "\"$(table2_id)\",\"$(custom_num)\",\"$(min_cost)\"," +
//								   "\"$(service_rate)\",\"$(member_id)\",\"$(member)\",\"$(comment)\"," +
//								   "\"$(gift_price)\",\"$(pay_type)\",\"$(discount_type)\",\"$(waiter)\"," +
//								   "$(isPaid),$(isDiscount),$(isGift),$(isCancel),\"$(seq_id)\"]";
//				jsonOrder = jsonOrder.replace("$(order_id)", Long.toString(dbCon.rs.getLong("id")));
//				jsonOrder = jsonOrder.replace("$(table_alias)", Integer.toString(dbCon.rs.getInt("table_alias")));
//				jsonOrder = jsonOrder.replace("$(order_date)", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbCon.rs.getTimestamp("order_date")));
//				jsonOrder = jsonOrder.replace("$(order_cate)", Util.toOrderCate(dbCon.rs.getShort("category")));
//				jsonOrder = jsonOrder.replace("$(pay_manner)", Util.toPayManner(dbCon.rs.getShort("type")));
//				float totalPrice = (float)Math.round(dbCon.rs.getFloat("total_price") * (1 + dbCon.rs.getFloat("service_rate")) * 100) / 100;
//				jsonOrder = jsonOrder.replace("$(total_price)", Float.toString(totalPrice));
//				jsonOrder = jsonOrder.replace("$(actual_income)", Float.toString(dbCon.rs.getFloat("total_price_2")));
//				jsonOrder = jsonOrder.replace("$(table2_id)", Integer.toString(dbCon.rs.getInt("table2_alias")));
//				jsonOrder = jsonOrder.replace("$(custom_num)", Integer.toString(dbCon.rs.getInt("custom_num")));
//				jsonOrder = jsonOrder.replace("$(min_cost)", "0");
//				jsonOrder = jsonOrder.replace("$(service_rate)", Byte.toString((byte)(dbCon.rs.getFloat("service_rate") * 100)));
//				jsonOrder = jsonOrder.replace("$(gift_price)", Float.toString(dbCon.rs.getFloat("gift_price")));
//				String memberID = dbCon.rs.getString("member_id");
//				jsonOrder = jsonOrder.replace("$(member_id)", memberID != null ? memberID : "");
//				String member = dbCon.rs.getString("member");
//				jsonOrder = jsonOrder.replace("$(member)", member != null ? member : "");
//				String comment = dbCon.rs.getString("comment");
//				jsonOrder = jsonOrder.replace("$(comment)", comment != null ? comment.replace("，", " ") : "");
//				jsonOrder = jsonOrder.replace("$(pay_type)", dbCon.rs.getString("member") == null ? "1" : "2");
//				jsonOrder = jsonOrder.replace("$(discount_type)", "0");
//				jsonOrder = jsonOrder.replace("$(waiter)", dbCon.rs.getString("waiter"));
//				jsonOrder = jsonOrder.replace("$(isPaid)", String.valueOf(dbCon.rs.getInt("is_paid")));
//				jsonOrder = jsonOrder.replace("$(isDiscount)", String.valueOf(dbCon.rs.getInt("is_discount")));
//				jsonOrder = jsonOrder.replace("$(isGift)", String.valueOf(dbCon.rs.getInt("is_gift")));
//				jsonOrder = jsonOrder.replace("$(isCancel)", String.valueOf(dbCon.rs.getInt("is_cancel")));
//				jsonOrder = jsonOrder.replace("$(seq_id)", Long.toString(dbCon.rs.getLong("seq_id")));
//				// put each json order info to the value
//				value.append(jsonOrder);
//				nCount++;
//			}
//			if(nCount == 0){
//				jsonResp = jsonResp.replace("$(value)", "");
//			}else{
//				jsonResp = jsonResp.replace("$(value)", value);
//			}
//			dbCon.rs.close();
//		}catch(BusinessException e){
//			e.printStackTrace();
//			jsonResp = jsonResp.replace("$(result)", "false");		
//			if(e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED){
//				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");	
//			}else if(e.errCode == ErrorCode.TERMINAL_EXPIRED){
//				jsonResp = jsonResp.replace("$(value)", "终端已过期，请重新确认");	
//			}else{
//				jsonResp = jsonResp.replace("$(value)", "没有获取到当日账单信息，请重新确认");	
//			}
//		}catch(SQLException e){
//			e.printStackTrace();
//			jsonResp = jsonResp.replace("$(result)", "false");
//			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
//		}catch(Exception e){
//			e.printStackTrace();
//			jsonResp = jsonResp.replace("$(result)", "false");
//			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
//		}finally{
//			dbCon.disconnect();
//			response.getWriter().write(jsonResp);
//		}
//		return null;
//	}
	
}
