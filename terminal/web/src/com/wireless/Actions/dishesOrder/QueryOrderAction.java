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
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Util;


public class QueryOrderAction extends Action {

	private static final long serialVersionUID = 1L;

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// 解决后台中文传到前台乱码
		response.setContentType("text/json; charset=utf-8");

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String pin = request.getParameter("pin");
		short tableID = Short.parseShort(request.getParameter("tableID"));

		String jsonResp = "{success:$(result), data:'$(value)'}";
		
		try {

			Order order = QueryOrder.exec(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF, tableID);
			jsonResp = jsonResp.replace("$(result)", "true");

			if (order.foods.length == 0) {
				jsonResp = jsonResp.replace("$(value)", "");
			} else {
				StringBuffer value = new StringBuffer();
				for (int i = 0; i < order.foods.length; i++) {
					String jsonOrderFood = "[\"$(name)\",\"$(taste)\",$(count),\"$(unit)\"]";
					jsonOrderFood = jsonOrderFood.replace("$(name)",
							order.foods[i].name);
					jsonOrderFood = jsonOrderFood.replace("$(taste)",
							order.foods[i].taste.preference);
					jsonOrderFood = jsonOrderFood.replace("$(count)",
							order.foods[i].count2String());
					jsonOrderFood = jsonOrderFood.replace("$(unit)", Util
							.price2String(order.foods[i].price2(),
									Util.INT_MASK_2));

					// pub each json order food info to the value
					value.append(jsonOrderFood);
					// the string is separated by comma
					if (i != order.foods.length - 1) {
						value.append("，");
					}
				}
				jsonResp = jsonResp.replace("$(value)", value);
			}

		} catch (BusinessException e) {
			jsonResp = jsonResp.replace("$(result)", "false");			
			jsonResp = jsonResp.replace("$(value)", e.getMessage());
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
		}


		System.out.println(jsonResp);

		out.write(jsonResp);
		return null;
	}

}
