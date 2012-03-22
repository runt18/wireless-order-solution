package com.wireless.Actions.menuMgr;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Util;

import com.wireless.db.QueryMenu;

public class QueryMenuMgrAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
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
		List chooseList = new ArrayList();
		HashMap rootMap = new HashMap();

		boolean isError = false;

		// 是否分頁
		String isPaging = request.getParameter("isPaging");

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
			 * pin : the pin the this terminal type : the type is one of the
			 * values below. 0 - 全部全部 1 - 编号 2 - 名称 3 - 拼音 4 - 价格 5 - 厨房 ope :
			 * the operator is one of the values below. 1 - 等于 2 - 大于等于 3 - 小于等于
			 * value : the value to search, the content is depending on the type
			 * isSpecial : additional condition. isRecommend : additional
			 * condition. isFree : additional condition. isStop : additional
			 * condition.
			 */

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

			if (type == 1) {
				// 按编号
				filterCondition = " AND alias_id " + ope + filterVal;
			} else if (type == 2) {
				// 按名称
				filterCondition = " AND name like '%" + filterVal + "%'";
			} else if (type == 3) {
				// 按拼音
				filterCondition = " AND pinyin like '" + filterVal + "%'";
			} else if (type == 4) {
				// 按价格
				filterCondition = " AND unit_price " + ope + filterVal;
			} else if (type == 5) {
				// 按厨房
				filterCondition = " AND kitchen " + ope + filterVal;
			} else {
				// 全部
				filterCondition = "";
			}
			
			String orderClause = " ORDER BY alias_id ";
			
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);
			Food[] foods = QueryMenu.execFoods(Long.parseLong(pin),
					Terminal.MODEL_STAFF, filterCondition, orderClause);

			// 格式：[编号，名称，拼音，价格，厨房，特价，推荐，停售，赠送，時價]
			for (int i = 0; i < foods.length; i++) {
				//
				// String sql = " SELECT id FROM "
				// + Params.dbName + ".food WHERE restaurant_id = "
				// + term.restaurant_id + " AND material_id = " + materialID;
				// dbCon.rs = dbCon.stmt.executeQuery(sql);
				// dbCon.rs.next();
				// float totalStock = dbCon.rs.getFloat("stock");
				// float thisPrice = dbCon.rs.getFloat("price");
				// dbCon.rs.close();

				HashMap resultMap = new HashMap();
				resultMap.put("foodID", new Long(foods[i].foodID).toString());
				resultMap.put("dishNumber",
						new Integer(foods[i].aliasID).toString());
				resultMap.put("dishName", foods[i].name);
				resultMap.put("dishSpill", foods[i].pinyin);
				resultMap.put("dishPrice",
						Util.float2String(foods[i].getPrice()));
				resultMap.put("dishSpill", foods[i].pinyin);
				resultMap
						.put("kitchen", new Short(foods[i].kitchen).toString());
				resultMap.put("special", foods[i].isSpecial());
				resultMap.put("recommend", foods[i].isRecommend());
				resultMap.put("stop", foods[i].isSellOut());
				resultMap.put("free", foods[i].isGift());
				resultMap.put("currPrice", foods[i].isCurPrice());
				resultMap.put("message", "normal");

				resultList.add(resultMap);
			}

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				resultMap.put("message", "没有获取到餐厅信息，请重新确认");
			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
				resultMap.put("message", "终端已过期，请重新确认");
			} else {
				resultMap.put("message", "没有获取到菜谱信息，请重新确认");
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
				// 特荐停送時 筛选
				String isSpecial = request.getParameter("isSpecial");
				String isRecommend = request.getParameter("isRecommend");
				String isFree = request.getParameter("isFree");
				String isStop = request.getParameter("isStop");
				String isCurrPrice = request.getParameter("isCurrPrice");
				if (isSpecial.equals("false") && isRecommend.equals("false")
						&& isFree.equals("false") && isStop.equals("false")
						&& isCurrPrice.equals("false")) {
					for (int i = 0; i < resultList.size(); i++) {
						chooseList.add(resultList.get(i));
					}
				} else {
					for (int i = 0; i < resultList.size(); i++) {
						if ((isSpecial.equals("true") && Boolean
								.parseBoolean(((HashMap) (resultList.get(i)))
										.get("special").toString()))
								|| (isRecommend.equals("true") && Boolean
										.parseBoolean(((HashMap) (resultList
												.get(i))).get("recommend")
												.toString()))
								|| (isFree.equals("true") && Boolean
										.parseBoolean(((HashMap) (resultList
												.get(i))).get("free")
												.toString()))
								|| (isStop.equals("true") && Boolean
										.parseBoolean(((HashMap) (resultList
												.get(i))).get("stop")
												.toString()))
								|| (isCurrPrice.equals("true") && Boolean
										.parseBoolean(((HashMap) (resultList
												.get(i))).get("currPrice")
												.toString()))) {
							chooseList.add(resultList.get(i));
						}
					}
				}

				if (isPaging.equals("true")) {
					// 分页
					for (int i = index; i < pageSize + index; i++) {
						try {
							outputList.add(chooseList.get(i));
						} catch (Exception e) {
							// 最后一页可能不足一页，会报错，忽略
						}
					}
				} else {
					for (int i = 0; i < chooseList.size(); i++) {
						outputList.add(chooseList.get(i));

					}
				}
				rootMap.put("root", outputList);
			}

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			String outputJson = "{\"totalProperty\":" + chooseList.size() + ","
					+ obj.toString().substring(1);

			//System.out.println(outputJson);

			out.write(outputJson);

		}
		return null;
	}
}
