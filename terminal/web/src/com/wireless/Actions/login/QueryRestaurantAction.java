package com.wireless.Actions.login;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.system.Setting;

public class QueryRestaurantAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("text/json; charset=utf-8");
		DBCon dbCon = new DBCon();
		String jsonResp = "{success:$(result), data:'$(value)'}";
		try {
			int restaurantID = Integer.parseInt(request.getParameter("restaurantID"));
			dbCon.connect();
			Restaurant restaurant = RestaurantDao.queryById(dbCon, restaurantID);
//			Setting setting = QuerySetting.exec(dbCon, restaurantID) ;
			Setting setting = SystemDao.getSetting(dbCon, restaurantID) ;
			
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
			value = value.replace("$(name)", restaurant.getRestaurantName());	
			/**
			 * Replace the "\r\n", "\r" or "\n" with "<br>"
			 */
			String info = restaurant.getRestaurantInfo().replace("\r\n", "<br>");
			info = info.replace("\n", "<br>");
			info = info.replace("\r", "<br>");
			value = value.replace("$(info)", info);	
			value = value.replace("$(tele_1)", restaurant.getTele1());	
			value = value.replace("$(tele_2)", restaurant.getTele2());	
			value = value.replace("$(addr)", restaurant.getAddress());	
			value = value.replace("$(price_tail)", Integer.toString(setting.getPriceTail().getValue()));	
			jsonResp = jsonResp.replace("$(value)", value);
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "餐厅信息不存在，请重新确认");
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jsonResp);
		}
		return null;
	}
}
