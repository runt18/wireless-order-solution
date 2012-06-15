package com.wireless.Actions.dishesOrder;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.QueryOrder;
import com.wireless.db.QueryShift;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Util;

public class QueryOrderAction extends Action {

//	private static final long serialVersionUID = 1L;

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		int tableID = 0;
		int orderID = 0;
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. 1st example, query order by
			 * table id pin=0x1 & tableID=201 2nd example, query order by order
			 * id pin=0x01 & orderID=40 pin : the pin the this terminal tableID
			 * : the order with this table ID to query
			 */
			String pin = request.getParameter("pin");
			String queryType = request.getParameter("queryType");

			Order order = null;
			if (request.getParameter("tableID") != null) {
				tableID = Integer.parseInt(request.getParameter("tableID"));
				order = QueryOrder.exec(Long.parseLong(pin),
						Terminal.MODEL_STAFF, tableID);

			} else if (request.getParameter("orderID") != null) {
				orderID = Integer.parseInt(request.getParameter("orderID"));
				if (queryType.equals("History")) {
					order = QueryOrder.execByID(orderID,
							QueryShift.QUERY_HISTORY);
				} else {
					order = QueryOrder
							.execByID(orderID, QueryShift.QUERY_TODAY);
				}
			}

			jsonResp = jsonResp.replace("$(result)", "true");

			if (order.foods.length == 0) {
				jsonResp = jsonResp.replace("$(value)", "");
			} else {
				StringBuffer value = new StringBuffer();
				for (int i = 0; i < order.foods.length; i++) {
					/**
					 * The json to order food looks like below. 
					 * ["菜名", 菜名编号, 厨房编号, "口味", 口味编号, 数量, 单价, 是否特价, 是否推荐, 是否停售, 是否赠送, 折扣率,
					 * 口味编号2, 口味编号3, 口味价钱, 是否时价, 是否临时菜, 流水号, 折扣额]
					 */
					String jsonOrderFood = "[\"$(food)\",$(food_id),$(kitchen),\"$(taste)\",$(taste_id),"
							+ "$(count),\"$(unit_price)\",$(special),$(recommend),$(soldout),"
							+ "$(gift),$(discount),$(taste_id2),$(taste_id3),\"$(taste_price)\","
							+ "$(currPrice),$(temporary),$(seq_id),$(total_discount)]";
					jsonOrderFood = jsonOrderFood.replace("$(food)",
							order.foods[i].name);
					jsonOrderFood = jsonOrderFood.replace("$(food_id)",
							new Integer(order.foods[i].aliasID).toString());
					jsonOrderFood = jsonOrderFood.replace("$(kitchen)",
							new Short(order.foods[i].kitchen.aliasID)
									.toString());
					jsonOrderFood = jsonOrderFood.replace("$(taste)",
							order.foods[i].getTastePref().replaceAll(",", "；"));
					// FIX ME!!!
					jsonOrderFood = jsonOrderFood.replace("$(taste_id)",
							Integer.toString(order.foods[i].tastes[0].aliasID));
					jsonOrderFood = jsonOrderFood.replace("$(count)",
							Util.float2String2(order.foods[i].getCount()));
					// float unitPrice = order.foods[i].getPrice() +
					// order.foods[i].getTastePrice();
					jsonOrderFood = jsonOrderFood.replace("$(unit_price)",
							Util.CURRENCY_SIGN + order.foods[i].getPrice());
					jsonOrderFood = jsonOrderFood
							.replace("$(taste_price)", Util.CURRENCY_SIGN
									+ order.foods[i].getTastePrice());
					jsonOrderFood = jsonOrderFood.replace("$(special)",
							order.foods[i].isSpecial() ? "true" : "false");
					jsonOrderFood = jsonOrderFood.replace("$(recommend)",
							order.foods[i].isRecommend() ? "true" : "false");
					jsonOrderFood = jsonOrderFood.replace("$(soldout)",
							order.foods[i].isSellOut() ? "true" : "false");
					jsonOrderFood = jsonOrderFood.replace("$(gift)",
							order.foods[i].isGift() ? "true" : "false");
					jsonOrderFood = jsonOrderFood.replace("$(discount)",
							order.foods[i].getDiscount().toString());
					jsonOrderFood = jsonOrderFood.replace("$(taste_id2)",
							Integer.toString(order.foods[i].tastes[1].aliasID));
					jsonOrderFood = jsonOrderFood.replace("$(taste_id3)",
							Integer.toString(order.foods[i].tastes[2].aliasID));
					jsonOrderFood = jsonOrderFood.replace("$(currPrice)",
							order.foods[i].isCurPrice() ? "true" : "false");
					jsonOrderFood = jsonOrderFood.replace("$(temporary)",
							order.foods[i].isTemporary ? "true" : "false");
					jsonOrderFood = jsonOrderFood.replace("$(seq_id)",
							Integer.toString(order.seqID));
					jsonOrderFood = jsonOrderFood.replace("$(total_discount)",
							order.foods[i].calcDiscountPrice().toString());
					
					// put each json order food info to the value
					value.append(jsonOrderFood);
					// the string is separated by comma
					if (i != order.foods.length - 1) {
						value.append("，");
					}
				}
				jsonResp = jsonResp.replace("$(value)", value);
			}

		} catch (BusinessException e) {

			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				e.printStackTrace();
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");

			} else if (e.errCode == ErrorCode.TABLE_NOT_EXIST) {
				e.printStackTrace();
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", tableID
						+ "号餐台信息不存在，请重新确认");

			} else if (e.errCode == ErrorCode.TABLE_IDLE) {
				jsonResp = jsonResp.replace("$(result)", "true");
				jsonResp = jsonResp.replace("$(value)", "NULL");

			} else if (e.errCode == ErrorCode.MENU_EXPIRED) {
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)",
						"菜谱信息与服务器不匹配，请与餐厅负责人确认或重新更新菜谱");

			} else if (e.errCode == ErrorCode.ORDER_NOT_EXIST) {
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", orderID
						+ "号账单信息不存在，请重新确认");

			} else {
				e.printStackTrace();
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", "没有获取到" + tableID
						+ "号餐台的账单信息，请重新确认");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			// just for debug
			// System.out.println(jsonResp);
			out.write(jsonResp);
		}

		return null;
	}

}
