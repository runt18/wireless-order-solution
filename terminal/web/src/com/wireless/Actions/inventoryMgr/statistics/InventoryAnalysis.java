package com.wireless.Actions.inventoryMgr.statistics;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.Actions.inventoryMgr.materialManagement.RestaurantInventory;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.dbObject.MaterialDetail;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;
import com.wireless.util.BusinessUtil;

public class InventoryAnalysis extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		List resultList = new ArrayList();
		HashMap rootMap = new HashMap();

		boolean isError = false;

		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. 1st example, filter the order
			 * whose id equals 321 pin=0x1 & type=1 & ope=1 & value=321 2nd
			 * example, filter the order date greater than or equal 2011-7-14
			 * 14:30:00 pin=0x1 & type=3 & ope=2 & value=2011-7-14 14:30:00
			 * 
			 * pin : the pin the this terminal foodIDs : array
			 * "food1,food2,food3" dateBegin: dateEnd :
			 */

			String pin = request.getParameter("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			// get the query condition
			String beginDate = request.getParameter("beginDate");
			String endDate = request.getParameter("endDate");
			String materialCates = request.getParameter("materialCates");
			String departments = request.getParameter("departments");

			RestaurantInventory restaurantInventory = new RestaurantInventory();

			String beginDt, endDt;
			// 期初
			if (beginDate.equals("")) {
				beginDt = "1900-01-01 00:00:00";
			} else {
				beginDt = beginDate + " 00:00:00";
			}
			ArrayList beginCountList = restaurantInventory.getInventoryPoint(
					pin, beginDt, departments, materialCates);

			// 期末
			if (!endDate.equals("")) {
				endDt = endDate + " 23:59:59";
			} else {
				endDt = "";
			}
			ArrayList endCountList = restaurantInventory.getInventoryPoint(pin,
					endDt, departments, materialCates);

			if (!beginDate.equals("")) {
				beginDate = beginDate + " 00:00:00";
			}
			if (!endDate.equals("")) {
				endDate = endDate + " 23:59:59";
			}

			// 进货数量
			ArrayList inCountList = restaurantInventory
					.getInventoryPeriodDetail(pin, beginDate, endDate,
							departments, materialCates, 5);

			// 退货数量
			ArrayList returnCountList = restaurantInventory
					.getInventoryPeriodDetail(pin, beginDate, endDate,
							departments, materialCates, 3);

			// 出仓数量
			ArrayList outCountList = restaurantInventory
					.getInventoryPeriodDetail(pin, beginDate, endDate,
							departments, materialCates, 4);

			// 报损数量
			ArrayList lostCountList = restaurantInventory
					.getInventoryPeriodDetail(pin, beginDate, endDate,
							departments, materialCates, 1);

			// 消耗数量
			ArrayList costCountList = restaurantInventory
					.getInventoryPeriodDetail(pin, beginDate, endDate,
							departments, materialCates, 0);

			// 调入数量
			ArrayList changeInCountList = restaurantInventory
					.getInventoryPeriodDetail(pin, beginDate, endDate,
							departments, materialCates, 7);

			// 调出数量
			ArrayList changeOutCountList = restaurantInventory
					.getInventoryPeriodDetail(pin, beginDate, endDate,
							departments, materialCates, 6);

			// 盘点损益
			ArrayList checkCountList = restaurantInventory
					.getInventoryPeriodDetail(pin, beginDate, endDate,
							departments, materialCates, 8);

			String cateString = "";
			BusinessUtil businessUtil = new BusinessUtil();
			if (materialCates.equals("")) {
				ArrayList cateList = businessUtil.getAllMaterialCate(pin);
				for (int i = 0; i < cateList.size(); i++) {
					cateString = cateString + cateList.get(i) + ",";
				}
				cateString = cateString.substring(0, cateString.length() - 1);
			} else {
				cateString = materialCates;
			}

			int groupID = 1;
			int materialIndex = -1;
			float endCount = 0;
			ArrayList allMaterialList = new ArrayList();
			String sqlPrice = "";
			String allMaterialSql = "";
			allMaterialSql = " select a.material_id, a.name as material_name, a.cate_id, b.name as cate_name"
					+ " from  "
					+ Params.dbName
					+ ".material a, "
					+ Params.dbName
					+ ".material_cate b where a.restaurant_id = "
					+ term.restaurant_id
					+ " and a.cate_id in ("
					+ cateString
					+ ") and a.restaurant_id = b.restaurant_id and a.cate_id = b.cate_id ";

			dbCon.rs = dbCon.stmt.executeQuery(allMaterialSql);
			while (dbCon.rs.next()) {
				ArrayList mate = new ArrayList();
				mate.add(dbCon.rs.getInt("material_id"));
				mate.add(dbCon.rs.getString("material_name"));
				mate.add(dbCon.rs.getInt("cate_id"));
				mate.add(dbCon.rs.getString("cate_name"));
				allMaterialList.add(mate);
			}

			for (int i = 0; i < allMaterialList.size(); i++) {

				ArrayList countList = (ArrayList) allMaterialList.get(i);

				HashMap resultMap = new HashMap();
				resultMap.put("materialCateID", countList.get(2));
				resultMap.put("materialCateName", countList.get(3));
				resultMap.put("groupID", groupID);
				resultMap.put("groupDescr", "");
				resultMap.put("materialID", countList.get(0));
				resultMap.put("materialName", countList.get(1));

				materialIndex = -1;
				for (int j = 0; j < beginCountList.size(); j++) {
					int thisMaterialId = Integer
							.parseInt(((ArrayList) beginCountList.get(j))
									.get(1).toString());
					int thisCateId = Integer
							.parseInt(((ArrayList) beginCountList.get(j))
									.get(3).toString());
					if (thisMaterialId == Integer.parseInt(countList.get(0)
							.toString())
							&& thisCateId == Integer.parseInt(countList.get(2)
									.toString())) {
						materialIndex = j;
					}
				}
				if (materialIndex == -1) {
					resultMap.put("countBegin", 0);
				} else {
					resultMap.put("countBegin", ((ArrayList) beginCountList
							.get(materialIndex)).get(0));
				}

				materialIndex = -1;
				for (int j = 0; j < inCountList.size(); j++) {
					int thisMaterialId = Integer
							.parseInt(((ArrayList) inCountList.get(j)).get(1)
									.toString());
					int thisCateId = Integer.parseInt(((ArrayList) inCountList
							.get(j)).get(3).toString());
					if (thisMaterialId == Integer.parseInt(countList.get(0)
							.toString())
							&& thisCateId == Integer.parseInt(countList.get(2)
									.toString())) {
						materialIndex = j;
					}
				}
				if (materialIndex == -1) {
					resultMap.put("inCount", 0);
				} else {
					resultMap
							.put("inCount", ((ArrayList) inCountList
									.get(materialIndex)).get(0));
				}

				materialIndex = -1;
				for (int j = 0; j < returnCountList.size(); j++) {
					int thisMaterialId = Integer
							.parseInt(((ArrayList) returnCountList.get(j)).get(
									1).toString());
					int thisCateId = Integer
							.parseInt(((ArrayList) returnCountList.get(j)).get(
									3).toString());
					if (thisMaterialId == Integer.parseInt(countList.get(0)
							.toString())
							&& thisCateId == Integer.parseInt(countList.get(2)
									.toString())) {
						materialIndex = j;
					}
				}
				if (materialIndex == -1) {
					resultMap.put("returnCount", 0);
				} else {
					resultMap.put("returnCount", ((ArrayList) returnCountList
							.get(materialIndex)).get(0));
				}

				materialIndex = -1;
				for (int j = 0; j < outCountList.size(); j++) {
					int thisMaterialId = Integer
							.parseInt(((ArrayList) outCountList.get(j)).get(1)
									.toString());
					int thisCateId = Integer.parseInt(((ArrayList) outCountList
							.get(j)).get(3).toString());
					if (thisMaterialId == Integer.parseInt(countList.get(0)
							.toString())
							&& thisCateId == Integer.parseInt(countList.get(2)
									.toString())) {
						materialIndex = j;
					}
				}
				if (materialIndex == -1) {
					resultMap.put("outCount", 0);
				} else {
					resultMap.put("outCount", ((ArrayList) outCountList
							.get(materialIndex)).get(0));
				}

				materialIndex = -1;
				for (int j = 0; j < lostCountList.size(); j++) {
					int thisMaterialId = Integer
							.parseInt(((ArrayList) lostCountList.get(j)).get(1)
									.toString());
					int thisCateId = Integer
							.parseInt(((ArrayList) lostCountList.get(j)).get(3)
									.toString());
					if (thisMaterialId == Integer.parseInt(countList.get(0)
							.toString())
							&& thisCateId == Integer.parseInt(countList.get(2)
									.toString())) {
						materialIndex = j;
					}
				}
				if (materialIndex == -1) {
					resultMap.put("lostCount", 0);
				} else {
					resultMap.put("lostCount", ((ArrayList) lostCountList
							.get(materialIndex)).get(0));
				}

				materialIndex = -1;
				for (int j = 0; j < costCountList.size(); j++) {
					int thisMaterialId = Integer
							.parseInt(((ArrayList) costCountList.get(j)).get(1)
									.toString());
					int thisCateId = Integer
							.parseInt(((ArrayList) costCountList.get(j)).get(3)
									.toString());
					if (thisMaterialId == Integer.parseInt(countList.get(0)
							.toString())
							&& thisCateId == Integer.parseInt(countList.get(2)
									.toString())) {
						materialIndex = j;
					}
				}
				if (materialIndex == -1) {
					resultMap.put("costCount", 0);
				} else {
					resultMap.put("costCount", ((ArrayList) costCountList
							.get(materialIndex)).get(0));
				}

				materialIndex = -1;
				for (int j = 0; j < changeInCountList.size(); j++) {
					int thisMaterialId = Integer
							.parseInt(((ArrayList) changeInCountList.get(j))
									.get(1).toString());
					int thisCateId = Integer
							.parseInt(((ArrayList) changeInCountList.get(j))
									.get(3).toString());
					if (thisMaterialId == Integer.parseInt(countList.get(0)
							.toString())
							&& thisCateId == Integer.parseInt(countList.get(2)
									.toString())) {
						materialIndex = j;
					}
				}
				if (materialIndex == -1) {
					resultMap.put("changeInCount", 0);
				} else {
					resultMap.put("changeInCount",
							((ArrayList) changeInCountList.get(materialIndex))
									.get(0));
				}

				materialIndex = -1;
				for (int j = 0; j < changeOutCountList.size(); j++) {
					int thisMaterialId = Integer
							.parseInt(((ArrayList) changeOutCountList.get(j))
									.get(1).toString());
					int thisCateId = Integer
							.parseInt(((ArrayList) changeOutCountList.get(j))
									.get(3).toString());
					if (thisMaterialId == Integer.parseInt(countList.get(0)
							.toString())
							&& thisCateId == Integer.parseInt(countList.get(2)
									.toString())) {
						materialIndex = j;
					}
				}
				if (materialIndex == -1) {
					resultMap.put("changeOutCount", 0);
				} else {
					resultMap.put("changeOutCount",
							((ArrayList) changeOutCountList.get(materialIndex))
									.get(0));
				}

				materialIndex = -1;
				for (int j = 0; j < checkCountList.size(); j++) {
					int thisMaterialId = Integer
							.parseInt(((ArrayList) checkCountList.get(j))
									.get(1).toString());
					int thisCateId = Integer
							.parseInt(((ArrayList) checkCountList.get(j))
									.get(3).toString());
					if (thisMaterialId == Integer.parseInt(countList.get(0)
							.toString())
							&& thisCateId == Integer.parseInt(countList.get(2)
									.toString())) {
						materialIndex = j;
					}
				}
				if (materialIndex == -1) {
					resultMap.put("checkCount", 0);
				} else {
					resultMap.put("checkCount", ((ArrayList) checkCountList
							.get(materialIndex)).get(0));
				}

				materialIndex = -1;
				for (int j = 0; j < endCountList.size(); j++) {
					int thisMaterialId = Integer
							.parseInt(((ArrayList) endCountList.get(j)).get(1)
									.toString());
					int thisCateId = Integer.parseInt(((ArrayList) endCountList
							.get(j)).get(3).toString());
					if (thisMaterialId == Integer.parseInt(countList.get(0)
							.toString())
							&& thisCateId == Integer.parseInt(countList.get(2)
									.toString())) {
						materialIndex = j;
					}
				}
				if (materialIndex == -1) {
					endCount = 0;
					resultMap.put("countEnd", 0);
				} else {
					endCount = Float.parseFloat(((ArrayList) endCountList
							.get(materialIndex)).get(0).toString());
					resultMap.put("countEnd", endCount);
				}

				sqlPrice = " select price from " + Params.dbName
						+ ".material_dept where restaurant_id = "
						+ term.restaurant_id + " and material_id = "
						+ countList.get(0);
				dbCon.rs2 = dbCon.conn.createStatement().executeQuery(sqlPrice);
				float thisPrice = 0;
				while (dbCon.rs2.next()) {
					thisPrice = dbCon.rs2.getFloat("price");
				}
				resultMap.put("price", thisPrice);
				resultMap.put("totalPrice",
						(float) Math.round(thisPrice * endCount * 100) / 100);

				resultMap.put("message", "normal");

				resultList.add(resultMap);

				groupID = groupID + 1;

			}

			if (resultList.size() == 0) {
				HashMap resultMap = new HashMap();
				resultMap.put("materialCateID", "NO_DATA");
				resultMap.put("message", "normal");
				resultList.add(resultMap);
			}

			// dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
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
				rootMap.put("root", resultList);
			}

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			// String outputJson = "{\"totalProperty\":" + resultList.size() +
			// ","
			// + obj.toString().substring(1);

			String outputJson = obj.toString();

			out.write(outputJson);
		}

		return null;
	}
}
