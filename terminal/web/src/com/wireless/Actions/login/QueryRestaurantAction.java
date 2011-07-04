package com.wireless.Actions.login;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.QueryRestaurant;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Restaurant;

public class QueryRestaurantAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		PrintWriter out = null;

		String jsonResp = "{success:$(result), data:'$(value)'}";
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			int restaurantID = Integer.parseInt(request.getParameter("restaurantID"));
			Restaurant restaurant = QueryRestaurant.exec(restaurantID);
			
			jsonResp = jsonResp.replace("$(result)", "true");
			/**
			 * The json restaurant format looks like below
			 * ["餐厅名称","餐厅信息","电话1","电话2","地址",$(尾数处理),$(自动补打)]
			 * $(尾数处理) - "0" means "不处理"
			 * 				 "1" means "小数抹零"
			 * 				 "2" means "小数四舍五入"
			 * $(自动补打) - "0" means "关闭自动补打"
			 * 				 "1" means "开启自动补打"
			 */
			String value = "\"$(name)\",\"$(info)\",\"$(tele_1)\",\"$(tele_2)\",\"$(addr)\",$(price_tail),$(auto_reprint)";
			value = value.replace("$(name)", restaurant.name);	
			value = value.replace("$(info)", restaurant.info.replace("\n", "<br>"));	
			value = value.replace("$(tele_1)", restaurant.tele_1);	
			value = value.replace("$(tele_2)", restaurant.tele_2);	
			value = value.replace("$(addr)", restaurant.addr);	
			value = value.replace("$(price_tail)", Integer.toString(restaurant.setting.price_tail));	
			value = value.replace("$(auto_reprint)", restaurant.setting.auto_reprint ? "1" : "0");	
			jsonResp = jsonResp.replace("$(value)", value);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "餐厅信息不存在，请重新确认");
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}catch(IOException e){
			e.printStackTrace();
			
		}finally{
			//Just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}
		return null;
	}
}
