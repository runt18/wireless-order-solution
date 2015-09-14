package com.wireless.Actions.billHistory;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.billStatistics.DailyGeneralDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.ShiftGeneral;
import com.wireless.util.DataPaging;

public class DailySettleStatisticsAction extends Action {
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JObject jobject = new JObject();
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		List<ShiftGeneral> list = null;
		
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			list = DailyGeneralDao.getByRange(StaffDao.verify(Integer.parseInt(pin)), onDuty, offDuty);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(DataPaging.getPagingData(list, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
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

			String pin = (String)request.getAttribute("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			// get the query condition
			String dateBegin = request.getParameter("dateBegin");
			String dateEnd = request.getParameter("dateEnd");

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
					+ ".daily_settle_history"
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
