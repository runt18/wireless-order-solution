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

import com.wireless.db.QueryMenu;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Util;

public class QueryMenuAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response) throws Exception {

		String jsonResp = "{success:$(result), data:'$(value)'}";
		
		PrintWriter out = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			/**
			 * The parameters looks like below.
			 * e.g. pin=0x1 & type=1 
			 * pin : the pin the this terminal
			 * type : "1" means to query foods 
			 * 		  "2" means to query tastes
			 * 		  "3" means to query kitchens
			 */
			
			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			/**
			 * The value to type means which item to query.
			 * 1 - Food
			 * 2 - Taste
			 * 3 - Kitchen
			 */
			short type = Short.parseShort(request.getParameter("type"));
			
			if(type == 1){
				Food[] foods = QueryMenu.execFoods(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF);
				jsonResp = jsonResp.replace("$(value)", toJson(foods));
				
			}else if(type == 2){
				Taste[] tastes = QueryMenu.execTastes(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF);
				jsonResp = jsonResp.replace("$(value)", toJson(tastes));
				
			}else if(type == 3){
				Kitchen[] kitchens = QueryMenu.execKitchens(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF);
				jsonResp = jsonResp.replace("$(value)", toJson(kitchens));
				
			}else{
				throw new BusinessException(ErrorCode.UNKNOWN);
			}
			
			jsonResp = jsonResp.replace("$(result)", "true");


		}catch(BusinessException e) {
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");		
			if(e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED){
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");	
				
			}else if(e.errCode == ErrorCode.TERMINAL_EXPIRED){
				jsonResp = jsonResp.replace("$(value)", "终端已过期，请重新确认");	
				
			}else{
				jsonResp = jsonResp.replace("$(value)", "没有获取到菜谱信息，请重新确认");	
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}catch(IOException e){
			e.printStackTrace();
			
		}finally{
			//just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);			
		}
		return null;
	}
	
	/**
	 * Convert the foods to jasn format
	 * @param foods
	 * @return
	 */
	private String toJson(Food[] foods){
		if(foods.length == 0){
			return "";
		}else{
			StringBuffer value = new StringBuffer();
			for (int i = 0; i < foods.length; i++) {
				/**
				 * The json format to each food item looks like below.
				 * [厨房编号,"菜品名称",菜品编号,"菜品拼音","￥菜品单价",是否特价,是否推荐,是否停售,是否赠送]
				 */
				String jsonFood = "[$(kitchen_id),\"$(name)\",$(alias_id),\"$(pinyin)\",\"$(unit)\",$(special),$(recommend),$(soldout),$(gift)]";
				jsonFood = jsonFood.replace("$(kitchen_id)", new Short(foods[i].kitchen).toString());
				jsonFood = jsonFood.replace("$(name)", foods[i].name);
				jsonFood = jsonFood.replace("$(alias_id)", new Integer(foods[i].alias_id).toString());
				jsonFood = jsonFood.replace("$(pinyin)", foods[i].pinyin);
				jsonFood = jsonFood.replace("$(unit)", Util.CURRENCY_SIGN + Util.float2String(foods[i].getPrice()));
				jsonFood = jsonFood.replace("$(special)", foods[i].isSpecial() ? "true" : "false");
				jsonFood = jsonFood.replace("$(recommend)", foods[i].isRecommend() ? "true" : "false");
				jsonFood = jsonFood.replace("$(soldout)", foods[i].isSellOut() ? "true" : "false");
				jsonFood = jsonFood.replace("$(gift)", foods[i].isGift() ? "true" : "false");
				// put each json food info to the value
				value.append(jsonFood);
				// the string is separated by comma
				if (i != foods.length - 1) {
					value.append("，");
				}
			}
			return value.toString();
		}
	}
	
	/**
	 * Convert the taste to json format
	 * @param tastes
	 * @return
	 */
	private String toJson(Taste[] tastes){
		
		final String tasteTemplate = "[$(taste_id),$(taste_cate),$(taste_pref),$(taste_price),$(taste_rate),$(calc_type)]";
		
		StringBuffer value = new StringBuffer();
		String jsonTaste = tasteTemplate;
		jsonTaste = jsonTaste.replace("$(taste_id)", "0");
		jsonTaste = jsonTaste.replace("$(taste_cate)", "0");
		jsonTaste = jsonTaste.replace("$(taste_pref)", "无口味");
		jsonTaste = jsonTaste.replace("$(taste_price)", Util.CURRENCY_SIGN	+ "0");
		jsonTaste = jsonTaste.replace("$(taste_rate)", "0");
		jsonTaste = jsonTaste.replace("$(calc_type)", "0");

		value.append(jsonTaste);	
			
		for (int i = 0; i < tastes.length; i++) {
			/**
			 * The json format to each taste item looks like below.
			 * [口味编号,口味分类,口味名称,价钱,比例,计算方式]
			 * “口味分类”的值如下： 0 - 口味 ， 1 - 做法，  2 - 规格
			 * “计算方式”的值如下：0 - 按价格，1 - 按比例 
			 */
			// the string is separated by comma
			value.append("，");
			jsonTaste = tasteTemplate;
			jsonTaste = jsonTaste.replace("$(taste_id)", Integer.toString((tastes[i].alias_id)));
			jsonTaste = jsonTaste.replace("$(taste_cate)", Integer.toString((tastes[i].category)));
			jsonTaste = jsonTaste.replace("$(taste_pref)", tastes[i].preference);
			jsonTaste = jsonTaste.replace("$(taste_price)", Util.CURRENCY_SIGN	+ Util.float2String(tastes[i].getPrice()));
			jsonTaste = jsonTaste.replace("$(taste_rate)", tastes[i].getRate().toString());
			jsonTaste = jsonTaste.replace("$(calc_type)", Integer.toString(tastes[i].calc));

			// put each json taste info to the value
			value.append(jsonTaste);
		}			

		return value.toString();
	}
	
	/**
	 * Convert the kitchen to json format
	 * @param kitchens
	 * @return
	 */
	private String toJson(Kitchen[] kitchens){
		if(kitchens.length == 0){
			return "";			
		}else{
			StringBuffer value = new StringBuffer();
			for(int i = 0; i < kitchens.length; i++){
				/**
				 * The json format to each kitchen looks like below.
				 * [厨房编号,"厨房名称",一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
				 */
				String jsonKitchen = "[$(kitchen_id),\"$(name)\",$(dist1),$(dist2),$(dist3),$(memDist1),$(memDist2),$(memDist3)]";
				jsonKitchen = jsonKitchen.replace("$(kitchen_id)", new Short(kitchens[i].alias_id).toString());
				jsonKitchen = jsonKitchen.replace("$(name)", kitchens[i].name);
				jsonKitchen = jsonKitchen.replace("$(dist1)", new Float((float)kitchens[i].discount / 100).toString());
				jsonKitchen = jsonKitchen.replace("$(dist2)", new Float((float)kitchens[i].discount_2 / 100).toString());
				jsonKitchen = jsonKitchen.replace("$(dist3)", new Float((float)kitchens[i].discount_3 / 100).toString());
				jsonKitchen = jsonKitchen.replace("$(memDist1)", new Float((float)kitchens[i].member_discount_1 / 100).toString());
				jsonKitchen = jsonKitchen.replace("$(memDist2)", new Float((float)kitchens[i].member_discount_2 / 100).toString());
				jsonKitchen = jsonKitchen.replace("$(memDist3)", new Float((float)kitchens[i].member_discount_3 / 100).toString());
				
				// put each json kitchen info to the value
				value.append(jsonKitchen);
				// the string is separated by comma
				if (i != kitchens.length - 1) {
					value.append("，");
				}
			}
			return value.toString();
		}
	}
}
