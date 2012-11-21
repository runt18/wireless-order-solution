package com.wireless.Actions.billHistory;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Terminal;
import com.wireless.util.Util;

public class QueryHistoryAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int index = 0;
		int pageSize = 0;
		if (!(start == null)) {
			index = Integer.parseInt(start);
			pageSize = Integer.parseInt(limit);
		}

		String queryType = request.getParameter("queryType");

		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> outputList = new ArrayList<HashMap<String, Object>>();
		// List chooseList = new ArrayList();
		HashMap<String, Object> rootMap = new HashMap<String, Object>();

		boolean isError = false;
		// 是否分頁
		String isPaging = request.getParameter("isPaging");
		// 是否combo
		// String isCombo = request.getParameter("isCombo");

		DBCon dbCon = new DBCon();

		// String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. 1st example, filter the order
			 * whose id equals 321 pin=0x1 & type=1 & ope=1 & value=321
			 * 
			 * 2nd example, filter the order date greater than or equal
			 * 2011-7-14 14:30:00 pin=0x1 & type=3 & ope=2 & value=2011-7-14
			 * 14:30:00
			 * 
			 * 3rd example, filter the orders have been paid before pin=0x1 &
			 * havingCond=1
			 * 
			 * pin : the pin the this terminal type : the type is one of the
			 * values below. 0 - 全部显示 1 - 按账单号 2 - 按流水号 3 - 按台号 4 - 按日期 5 - 按类型
			 * 6 - 按结帐方式 7 - 按金额 8 - 按实收 9 - 最近日结 ope : the operator is one of
			 * the values below. 1 - 等于 2 - 大于等于 3 - 小于等于 value : the value to
			 * search, the content is depending on the type havingCond : the
			 * having condition is one of the values below. 0 - 无 1 - 是否有反结帐 2 -
			 * 是否有折扣 3 - 是否有赠送 4 - 是否有退菜
			 */
			String pin = request.getParameter("pin");

			dbCon.connect();

			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			String sql = "";
			String havingCond = "";
			if (queryType.equals("normal")) {

				// get the type to filter
				int type = Integer.parseInt(request.getParameter("type"));

				// get the operator to filter
				String ope = request.getParameter("ope");
				int opeType = 1;
				if (ope != null) {
					opeType = Integer.parseInt(ope);

					if (opeType == 1) {
						ope = "=";
					} else if (opeType == 2) {
						ope = ">=";
					} else if (opeType == 3) {
						ope = "<=";
					} else {
						ope = "=";
					}
				} else {
					ope = "";
				}

				// get the value to filter
				String filterVal = request.getParameter("value");

				// get the having condition to filter
				int cond = Integer.parseInt(request.getParameter("havingCond"));
				havingCond = null;
				if (cond == 1) {
					// 是否有反结帐
					havingCond = " HAVING SUM(IF(B.is_paid, 1, 0)) > 0 ";
				} else if (cond == 2) {
					// 是否有折扣
					havingCond = " HAVING MIN(B.discount) < 1 ";
				} else if (cond == 3) {
					// 是否有赠送
					havingCond = " HAVING SUM(B.food_status & " + Food.GIFT
							+ ") > 0 ";
				} else if (cond == 4) {
					// 是否有退菜
					havingCond = " HAVING MIN(order_count) < 0 ";
				} else {
					havingCond = "";
				}

				// combine the operator and filter value
				String filterCondition = null;

				if (type == 1) {
					// 按账单号
					filterCondition = " AND A.id" + ope + filterVal;
				} else if (type == 2) {
					// 按流水号
					if (havingCond.equals("")) {
						havingCond = " HAVING MAX(A.seq_id) " + ope + filterVal;
					} else {
						havingCond = havingCond + " AND MAX(A.seq_id) " + ope
								+ filterVal;
					}
					filterCondition = "";
				} else if (type == 3) {
					// 按台号
					filterCondition = " AND A.table_alias" + ope + filterVal;
				} else if (type == 4) {
					// 按日期
					if (opeType == 1) {
						filterCondition = " AND A.order_date >= '" + filterVal
								+ " 00:00:00' AND A.order_date <= '"
								+ filterVal + " 23:59:59'";

					} else if (opeType == 3) {
						filterCondition = " AND A.order_date" + ope + "'"
								+ filterVal + " 23:59:59'";
					} else {
						filterCondition = " AND A.order_date" + ope + "'"
								+ filterVal + " 00:00:00'";
					}
				} else if (type == 5) {
					// 按类型
					filterCondition = " AND A.category" + ope + filterVal;
				} else if (type == 6) {
					// 按结帐方式
					filterCondition = " AND A.type" + ope + filterVal;
				} else if (type == 7) {
					// 按金额
					filterCondition = " AND A.total_price" + ope + filterVal;
				} else if (type == 8) {
					// 按实收
					filterCondition = " AND A.total_price_2" + ope + filterVal;
				} else if (type == 9) {
					// 最近日结(显示最近一次日结记录的时间区间内的账单信息)
					sql = " SELECT on_duty, off_duty FROM " + Params.dbName
							+ ".daily_settle_history "
							+ " WHERE restaurant_id=" + term.restaurantID
							+ " ORDER BY id DESC " + " LIMIT 1 ";
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					if (dbCon.rs.next()) {
						filterCondition = " AND A.order_date BETWEEN "
								+ "'"
								+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
										.format(dbCon.rs
												.getTimestamp("on_duty"))
								+ "'"
								+ " AND "
								+ "'"
								+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
										.format(dbCon.rs
												.getTimestamp("off_duty"))
								+ "'";

					} else {
						filterCondition = "";
					}
					dbCon.rs.close();
				} else {
					filterCondition = "";
				}

				/**
				 * Select all the today orders matched the conditions below. 1 -
				 * belong to this restaurant 2 - has been paid 3 - match extra
				 * filter condition
				 */
				sql = " SELECT "
						+ " A.id, MAX(A.seq_id) AS seq_id, "
						+ " MAX(A.table_alias) AS table_alias, MAX(A.order_date) AS order_date, MAX(A.category) AS category, "
						+ " MAX(A.type) AS type, MAX(A.total_price) AS total_price, MAX(A.total_price_2) AS total_price_2, "
						+ " MAX(A.table2_alias) AS table2_alias, MAX(A.custom_num) AS custom_num, MAX(A.service_rate) AS service_rate, "
						+ " MAX(A.gift_price) AS gift_price, MAX(A.member_id) AS member_id, MAX(A.member) AS member, "
						+ " MAX(A.comment) AS comment, MAX(A.waiter) AS waiter, "
						+ "(CASE WHEN MIN(B.discount) < 1 THEN 1 ELSE 0 END) AS is_discount, "
						+ "(CASE WHEN SUM(B.food_status & "
						+ Food.GIFT
						+ ") <= 0 THEN 0 ELSE 1 END) AS is_gift, "
						+ "(CASE WHEN SUM(IF(B.is_paid, 1, 0)) <= 0 THEN 0 ELSE 1 END) AS is_paid, "
						+ "(CASE WHEN MIN(order_count) < 0 THEN 1 ELSE 0 END) AS is_cancel"
						+ " FROM " + Params.dbName + ".order_history A, "
						+ Params.dbName + ".order_food_history B " + " WHERE "
						+ " A.id = B.order_id " + " AND A.restaurant_id="
						+ term.restaurantID + " " + filterCondition
						+ " GROUP BY A.id " + havingCond 
						+ " ORDER BY order_date ASC ";

			} else {
				// get the query condition
				String dateBegin = request.getParameter("dateBegin");
				String dateEnd = request.getParameter("dateEnd");
				String amountBegin = request.getParameter("amountBegin");
				String amountEnd = request.getParameter("amountEnd");
				String seqNumBegin = request.getParameter("seqNumBegin");
				String seqNumEnd = request.getParameter("seqNumEnd");
				String tableNumber = request.getParameter("tableNumber");
				String payManner = request.getParameter("payManner");
				String tableType = request.getParameter("tableType");

				// combine the operator and filter value
				String filterCondition = "";
				if (!dateBegin.equals("")) {
					filterCondition = " AND A.order_date>='" + dateBegin
							+ " 00:00:00" + "' ";
				}

				if (!dateEnd.equals("")) {
					filterCondition = filterCondition + " AND A.order_date<='"
							+ dateEnd + " 23:59:59" + "' ";
				}

				if (!amountBegin.equals("")) {
					filterCondition = filterCondition + " AND A.total_price>="
							+ amountBegin;
				}

				if (!amountEnd.equals("")) {
					filterCondition = filterCondition + " AND A.total_price<="
							+ amountEnd;
				}

				if (!seqNumBegin.equals("")) {
					if (havingCond.equals("")) {
						havingCond = " HAVING MAX(A.seq_id) >= " + seqNumBegin;
					} else {
						havingCond = havingCond + " AND MAX(A.seq_id) >= "
								+ seqNumBegin;
					}
				}

				if (!seqNumEnd.equals("")) {
					if (havingCond.equals("")) {
						havingCond = " HAVING MAX(A.seq_id) <= " + seqNumEnd;
					} else {
						havingCond = havingCond + " AND MAX(A.seq_id) <= "
								+ seqNumEnd;
					}
				}

				if (!tableNumber.equals("")) {
					filterCondition = filterCondition + " AND A.table_alias="
							+ tableNumber;
				}

				// db:[ [ "1", "现金" ], [ "2", "刷卡" ], [ "3", "会员卡" ],[ "4", "签单"
				// ],
				// [ "5", "挂账" ] ]
				if (!payManner.equals("6")) {
					filterCondition = filterCondition + " AND A.type="
							+ payManner;
				}

				// db:[ [ "1", "一般" ], [ "2", "外卖" ], [ "3", "并台" ], [ "4", "拼台"
				// ] ]
				if (!tableType.equals("6")) {
					filterCondition = filterCondition + " AND A.category="
							+ tableType;
				}

				/**
				 * Select all the today orders matched the conditions below. 1 -
				 * belong to this restaurant 2 - has been paid 3 - match extra
				 * filter condition
				 */
				sql = " SELECT "
						+ " A.id, MAX(A.seq_id) AS seq_id, "
						+ " MAX(A.table_alias) AS table_alias, MAX(A.order_date) AS order_date, MAX(A.category) AS category, "
						+ " MAX(A.type) AS type, MAX(A.total_price) AS total_price, MAX(A.total_price_2) AS total_price_2, "
						+ " MAX(A.table2_alias) AS table2_alias, MAX(A.custom_num) AS custom_num, MAX(A.service_rate) AS service_rate, "
						+ " MAX(A.gift_price) AS gift_price, MAX(A.member_id) AS member_id, MAX(A.member) AS member, "
						+ " MAX(A.comment) AS comment, MAX(A.waiter) AS waiter, "
						+ "(CASE WHEN MIN(B.discount) < 1 THEN 1 ELSE 0 END) AS is_discount, "
						+ "(CASE WHEN SUM(B.food_status & "
						+ Food.GIFT
						+ ") <= 0 THEN 0 ELSE 1 END) AS is_gift, "
						+ "(CASE WHEN SUM(IF(B.is_paid, 1, 0)) <= 0 THEN 0 ELSE 1 END) AS is_paid, "
						+ "(CASE WHEN MIN(order_count) < 0 THEN 1 ELSE 0 END) AS is_cancel"
						+ " FROM " + Params.dbName + ".order_history A, "
						+ Params.dbName + ".order_food_history B " + " WHERE "
						+ " A.id = B.order_id " + " AND A.restaurant_id="
						+ term.restaurantID + " " + filterCondition
						+ " GROUP BY A.id " + havingCond
						+ " ORDER BY order_date ASC ";
			}

			dbCon.rs = dbCon.stmt.executeQuery(sql);

			while (dbCon.rs.next()) {
				/**
				 * The json to each order looks like below ["账单号", "台号", "日期",
				 * "类型", "结帐方式", "金额", "实收", "台号2", "就餐人数", "最低消", "服务费率",
				 * "会员编号", "会员姓名", "账单备注", "赠券金额", "结帐类型", "折扣类型", "服务员", 是否反結帳,
				 * 是否折扣, 是否赠送, 是否退菜, "流水号"]
				 */

				HashMap<String, Object> resultMap = new HashMap<String, Object>();

				resultMap.put("orderID", Long.toString(dbCon.rs.getLong("id")));
				resultMap.put("tableAlias",
						Integer.toString(dbCon.rs.getInt("table_alias")));
				resultMap.put("orderDate", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(dbCon.rs
						.getTimestamp("order_date")));
				resultMap.put("orderCategory",
						Util.toOrderCate(dbCon.rs.getShort("category")));
				resultMap.put("payManner",
						Util.toPayManner(dbCon.rs.getShort("type")));

				float totalPrice = (float) Math.round(dbCon.rs
						.getFloat("total_price")
						* (1 + dbCon.rs.getFloat("service_rate")) * 100) / 100;
				resultMap.put("totalPrice", Float.toString(totalPrice));

				resultMap.put("actualIncome",
						Float.toString(dbCon.rs.getFloat("total_price_2")));
				resultMap.put("table2Alias",
						Integer.toString(dbCon.rs.getInt("table2_alias")));
				resultMap.put("customerNum",
						Integer.toString(dbCon.rs.getInt("custom_num")));
				resultMap.put("minCost", "0");
				resultMap.put("serviceRate", Byte.toString((byte) (dbCon.rs
						.getFloat("service_rate") * 100)));
				resultMap.put("giftPrice",
						Float.toString(dbCon.rs.getFloat("gift_price")));

				String memberID = dbCon.rs.getString("member_id");
				resultMap.put("memberID", memberID != null ? memberID : "");

				String member = dbCon.rs.getString("member");
				resultMap.put("member", member != null ? member : "");

				String comment = dbCon.rs.getString("comment");
				resultMap.put("comment", comment != null ? comment : "");

				resultMap.put("payType",
						dbCon.rs.getString("member") == null ? "1" : "2");
				resultMap.put("discountType",
						"0");
				resultMap.put("staff", dbCon.rs.getString("waiter"));
				resultMap.put("isPaid",
						String.valueOf(dbCon.rs.getInt("is_paid")));
				resultMap.put("isDiscount",
						String.valueOf(dbCon.rs.getInt("is_discount")));
				resultMap.put("isGift",
						String.valueOf(dbCon.rs.getInt("is_gift")));
				resultMap.put("isCancel",
						String.valueOf(dbCon.rs.getInt("is_cancel")));
				resultMap.put("seqID",
						Long.toString(dbCon.rs.getLong("seq_id")));

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
				resultMap.put("message", "没有获取到部门信息，请重新确认");
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

//			System.out.println(outputJson);
			out.write(outputJson);

		}

		return null;
	}

}
