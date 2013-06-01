package com.wireless.Actions.deptMgr.kitchenMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.protocol.Terminal;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryKitchenAction extends DispatchAction {
	
	/**
	 * 树形数据格式
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		StringBuffer jsb = new StringBuffer();
		try{
			String pin = request.getParameter("pin");
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			String extraCond = "", orderClause = "";
			extraCond += (" AND KITCHEN.restaurant_id = " + term.restaurantID);
			extraCond += (" AND KITCHEN.kitchen_alias <> 253 AND KITCHEN.kitchen_alias <> 255 ");
			List<Kitchen> list = KitchenDao.getKitchens(term, extraCond, orderClause);
			for(int i = 0; i < list.size(); i++){
				if(i>0)
					jsb.append(",");
				jsb.append("{");
				jsb.append("leaf:true");
				jsb.append(",text:'" + list.get(i).getName() + "'");
				jsb.append(",alias:" + list.get(i).getAliasId());
				jsb.append(",name:'" + list.get(i).getName() + "'");
				jsb.append(",kid:" + list.get(i).getId());
				jsb.append("}");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("[" + jsb.toString() + "]");
		}
		return null;
	}
	
	/**
	 * 普通数据格式
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<Kitchen> root = null;
		
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String pin = request.getParameter("pin");
			String deptID = request.getParameter("deptID");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			String extraCond = "", orderClause = "";
			
			extraCond += (" AND KITCHEN.restaurant_id = " + term.restaurantID);
			extraCond += (" AND KITCHEN.kitchen_alias <> 253 AND KITCHEN.kitchen_alias <> 255 ");
			if(deptID != null && !deptID.trim().isEmpty() && !deptID.equals("-1")){
				extraCond += (" AND DEPT.dept_id = " + deptID);
			}
			
			orderClause = " ORDER BY KITCHEN.kitchen_alias ";
			
			root = KitchenDao.getKitchens(term, extraCond, orderClause);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/*
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int index = 0;
		int pageSize = 0;
		if (!(start == null)) {
			index = Integer.parseInt(start);
			pageSize = Integer.parseInt(limit);
		}

		List resultList = new ArrayList();
		List outputList = new ArrayList();
		// List chooseList = new ArrayList();
		HashMap rootMap = new HashMap();

		boolean isError = false;
		// 是否分頁
		String isPaging = request.getParameter("isPaging");

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
			 * pin : the pin the this terminal type : the type is one of the
			 * values below. 0 - 全部 1 - 名称 2 - 电话 3 - 地址 ope : the operator is
			 * one of the values below. 1 - 等于 2 - 大于等于 3 - 小于等于 value : the
			 * value to search, the content is depending on the type isSpecial :
			 * additional condition. isRecommend : additional condition. isFree
			 * : additional condition. isStop : additional condition.
			 *//*

			String pin = request.getParameter("pin");
			String deptID = request.getParameter("deptID");
			String kitchenName = request.getParameter("kitchenName");
			
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);

			String sql = " SELECT kitchen_id, kitchen_alias, name, dept_id, is_allow_temp "
					+ " FROM " + Params.dbName + ".kitchen "
					+ " WHERE restaurant_id = " + term.restaurantID 
					+ (deptID == null || deptID.trim().equals("") || deptID == "" || deptID.equals("-1") || deptID == "-1" ? "" : (" AND dept_id = " + deptID))
					+ (kitchenName == null || kitchenName.trim().equals("") ? "" : " AND name like '%" + kitchenName + "%' ");
			dbCon.rs = dbCon.stmt.executeQuery(sql);

			while (dbCon.rs.next()) {
				HashMap resultMap = new HashMap();
				*//**
				 * The json to each order looks like below 分廚編號，名稱，一般折扣１，一般折扣２，一般折扣３，會員折扣１，會員折扣２，會員折扣３，部門
				 *//*
				resultMap.put("kitchenID", dbCon.rs.getInt("kitchen_id"));
				resultMap.put("kitchenAlias", dbCon.rs.getInt("kitchen_alias"));
				resultMap.put("kitchenName", dbCon.rs.getString("name"));
				resultMap.put("department", dbCon.rs.getInt("dept_id"));
				resultMap.put("isAllowTemp", dbCon.rs.getInt("is_allow_temp"));

				resultMap.put("message", "normal");

				resultList.add(resultMap);

			}
			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			if (e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED) {
				resultMap.put("message", "没有获取到餐厅信息，请重新确认");
			} else if (e.getErrCode() == ProtocolError.TERMINAL_EXPIRED) {
				resultMap.put("message", "终端已过期，请重新确认");
			} else {
				resultMap.put("message", "没有获取到分厨信息，请重新确认");
			}
			resultList.add(resultMap);
			isError = true;
		} catch (SQLException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;
		} catch (IOException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;
		} finally {
			dbCon.disconnect();

			if (isError) {
				rootMap.put("root", resultList);
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

			String outputJson = "{\"totalProperty\":" + resultList.size() + "," + obj.toString().substring(1);

			//System.out.println(outputJson);

			out.write(outputJson);

		}
		return null;
	}
	*/
}
