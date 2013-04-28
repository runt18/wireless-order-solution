package com.wireless.Actions.tableSelect;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.system.Terminal;
import com.wireless.util.DataPaging;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class QueryTableAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		List<Table> tables = null;
		
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String pin = request.getParameter("pin");
			
			tables = TableDao.getTables(VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF), null, null);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			
		}finally{
			if(tables != null){
				jobject.setTotalProperty(tables.size());
				jobject.setRoot(DataPaging.getPagingData(tables, isPaging, start, limit));
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

		PrintWriter out = null;

		DBCon dbCon = new DBCon();

		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int index = 0;
		int pageSize = 0;
		if (!(start == null)) {
			index = Integer.parseInt(start);
			pageSize = Integer.parseInt(limit);
		}

		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> outputList = new ArrayList<HashMap<String, Object>>();
		// List chooseList = new ArrayList();
		HashMap<String, List<HashMap<String, Object>>> rootMap = new HashMap<String, List<HashMap<String, Object>>>();

		boolean isError = false;
		// 是否分頁
		String isPaging = request.getParameter("isPaging");
		// 是否combo
		String isCombo = request.getParameter("isCombo");

		// String jsonResp = "{success:$(result), data:'$(value)'}";
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			String pin = request.getParameter("pin");

			// get the type to filter
			int type = Integer.parseInt(request.getParameter("type"));

			// get the operator to filter
			String ope = request.getParameter("ope");
			if (ope != null) {
				int opeType = Integer.parseInt(ope);

				if (opeType == 1) {
					ope = "=";
				} else if (opeType == 2) {
					ope = ">=";
				} else if (opeType == 3) {
					ope = "<=";
				} else {
					// 不可能到这里
					ope = "=";
				}
			} else {
				// 不可能到这里
				ope = "";
			}

			// get the value to filter
			String filterVal = request.getParameter("value");

			// combine the operator and filter value
			String filterCondition = null;
			// [ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ], [ "3", "区域" ], [
			// "4", "状态 " ]
			if (type == 1) {
				// 按编号
				filterCondition = " AND table_alias " + ope + filterVal;
			} else if (type == 2) {
				// 按名称
				filterCondition = " AND name like '%" + filterVal + "%'";
			} else if (type == 3) {
				// 按区域
				filterCondition = " AND region_id " + ope + filterVal;
			} else if (type == 4) {
				// 按状态
				filterCondition = " AND status " + ope + filterVal;
			} else {
				// 全部
				filterCondition = "";
			}

			dbCon.connect();
			Table[] tables = QueryTable.exec(Long.parseLong(pin),
					Terminal.MODEL_STAFF, filterCondition, "");

			for (int i = 0; i < tables.length; i++) {
				// ID，別名編號，名稱，區域，人數，狀態，種類，最低消費
				HashMap<String, Object> resultMap = new HashMap<String, Object>();

				resultMap.put("tableID", tables[i].getTableId());
				resultMap.put("tableAlias", tables[i].getAliasId());
				resultMap.put("tableName", tables[i].getName());
				resultMap.put("tableRegion", tables[i].regionID);
				resultMap.put("tableCustNbr", tables[i].getCustomNum());
				resultMap.put("tableStatus", tables[i].getStatus());
				resultMap.put("tableCategory", tables[i].getCategory());
				resultMap.put("tableMinCost", tables[i].getMinimumCost());
				resultMap.put("tableServiceRate", tables[i].getServiceRate());

				resultMap.put("message", "normal");

				resultList.add(resultMap);

			}

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

			String outString = "";

			if (isError) {
				rootMap.put("root", resultList);
			} else if (isCombo.equals("true")) {
				outString = "{\"root\":[";

				if (resultList.size() == 0) {

				} else {
					for (int i = 0; i < resultList.size(); i++) {

						outString = outString +
									"{tableID:"	+ resultList.get(i).get("tableID").toString() + ",";
						outString = outString +
								    "tableName:'" + resultList.get(i).get("tableName").toString() + "'},";

					}
					outString = outString.substring(0, outString.length() - 1);

				}
				outString = outString + "]}";
			} else {
				if (isPaging.equals("true")) {
					// 分页
					for (int i = index; i < pageSize + index; i++) {
						try {
							outputList.add(resultList.get(i));
						} catch (Exception e) {
							// 最后一页可能不足一页，会报错，忽略
						}
					}
				} else {
					for (int i = 0; i < resultList.size(); i++) {
						outputList.add(resultList.get(i));
					}
				}
				rootMap.put("root", outputList);
			}

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			String outputJson = "{\"totalProperty\":" + resultList.size() + ","
					+ obj.toString().substring(1);

			if (isCombo.equals("true")) {
				//System.out.println(outString);
				out.write(outString);
			} else {
				//System.out.println(outputJson);
				out.write(outputJson);
			}

		}
		return null;
	}

	*/
	
}
