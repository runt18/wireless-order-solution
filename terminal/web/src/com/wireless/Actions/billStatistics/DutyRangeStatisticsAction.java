package com.wireless.Actions.billStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.QueryDutyRange;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.util.DataPaging;
import com.wireless.util.DataType;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class DutyRangeStatisticsAction extends DispatchAction {
	
	/**
	 * 当日交班记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward today(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<DutyRange> list = null;
		try{
			String pin = request.getParameter("pin");
			list = QueryDutyRange.getDutyRangeByToday(Long.valueOf(pin));
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
			e.printStackTrace();
		}finally{
			jobject.setRoot(list);
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
	/**
	 * 历史交班记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward history(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<DutyRange> list = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>();
			paramsSet.put(DataType.HISTORY, DataType.HISTORY.getValue());
//			paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, start);
//			paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, limit);
			paramsSet.put("pin", pin);
			paramsSet.put("restaurantID", restaurantID);
			paramsSet.put("onDuty", onDuty);
			paramsSet.put("offDuty", offDuty);
			
			list = QueryDutyRange.getDutyRange(paramsSet);
			
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
		}finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(DataPaging.getPagingData(list, isPaging, start, limit));
			}
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
	
	
	
	/*
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int index = Integer.parseInt(start);
		int pageSize = Integer.parseInt(limit);

		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> outputList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> rootMap = new HashMap<String, Object>();

		boolean isError = false;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			*//**
			 * The parameters looks like below. 1st example, filter the order
			 * whose id equals 321 pin=0x1 & type=1 & ope=1 & value=321 2nd
			 * example, filter the order date greater than or equal 2011-7-14
			 * 14:30:00 pin=0x1 & type=3 & ope=2 & value=2011-7-14 14:30:00
			 * 
			 * pin : the pin the this terminal foodIDs : array
			 * "food1,food2,food3" dateBegin: dateEnd :
			 *//*

			String pin = request.getParameter("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			// get the query condition
			String dateBegin = request.getParameter("dateBegin");
			String dateEnd = request.getParameter("dateEnd");
			String StatisticsType = request.getParameter("StatisticsType");

			String tableName = "";
			if (StatisticsType.equals("Today")) {
				tableName = "shift";
			} else if (StatisticsType.equals("History")) {
				tableName = "shift_history";
			}

			String condition = " ";
			if (!dateBegin.equals("")) {
				condition = condition + " AND off_duty >= '" + dateBegin + "' ";
			}
			if (!dateEnd.equals("")) {
				condition = condition + " AND off_duty <= '" + dateEnd + "' ";
			}
			condition = condition + " AND restaurant_id =  "
					+ term.restaurantID;

			String orderClause = " ORDER BY off_duty ";

			String sql = " SELECT id, restaurant_id, name, on_duty, off_duty FROM "
					+ Params.dbName
					+ "."
					+ tableName
					+ " WHERE restaurant_id = "
					+ term.restaurantID
					+ " "
					+ condition + orderClause;

			dbCon.rs = dbCon.stmt.executeQuery(sql);

			while (dbCon.rs.next()) {
				HashMap<String, Object> resultMap = new HashMap<String, Object>();
				*//**
				 * 
				 *//*
				resultMap.put("staff", dbCon.rs.getString("name"));
				resultMap.put("beginTime", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(dbCon.rs
						.getTimestamp("on_duty")));
				resultMap.put("endTime", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(dbCon.rs
						.getTimestamp("off_duty")));
				// resultMap.put("beginTime", dbCon.rs.getDate("on_duty"));
				// resultMap.put("endTime", dbCon.rs.getDate("off_duty"));

				resultMap.put("message", "normal");

				resultList.add(resultMap);

			}
			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				resultMap.put("message", "没有获取到餐厅信息，请重新确认");

			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
				resultMap.put("message", "终端已过期，请重新确认");

			} else {
				resultMap.put("message", "没有获取到信息，请重新确认");
			}
			resultList.add(resultMap);
			isError = true;
		} catch (SQLException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;

		} catch (IOException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;

		} finally {
			dbCon.disconnect();

			if (isError) {
				rootMap.put("root", resultList);
			} else {
				// 分页
				for (int i = index; i < pageSize + index; i++) {
					try {
						outputList.add(resultList.get(i));
					} catch (Exception e) {
						// 最后一页可能不足一页，会报错，忽略
					}
				}

				rootMap.put("root", outputList);
			}

			JsonConfig jsonConfig = new JsonConfig();
			// // 解决日期类型显示问题
			// jsonConfig.registerJsonValueProcessor(java.util.Date.class,
			// new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
			// jsonConfig.registerJsonValueProcessor(java.sql.Timestamp.class,
			// new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			String outputJson = "{\"totalProperty\":" + resultList.size() + ","
					+ obj.toString().substring(1);

			// System.out.println(outputJson);

			out.write(outputJson);
		}

		return null;
	}
	
	*/
}
