package com.wireless.Actions.dishesOrder;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryMenuAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response) throws Exception {

		JObject jobject = new JObject();
		List root = new ArrayList();
		
		String restaurantID = request.getParameter("restaurantID");
		String type = request.getParameter("type");
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String cond = null;
		String orderBy = null;
		
		try {
			
			response.setContentType("text/json; charset=utf-8");
			
			/**
			 * The parameters looks like below.
			 * e.g. pin=0x1 & type=1 
			 * pin : the pin the this terminal
			 * type : "1" means to query foods 
			 * 		  "2" means to query tastes
			 * 		  "3" means to query kitchens
			 */
			
			
			
			try{
				Integer.parseInt(restaurantID);
				Integer.parseInt(type);
			}catch(Exception e){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, 9998, "操作失败, 获取餐厅编号或操作类型失败.");
				return null;
			}
			
			if(type.trim().equals("1")){
				orderBy = " order by A.food_alias";
				cond = " and A.restaurant_id = " + restaurantID;
				String searchType = request.getParameter("searchType");
				String searchValue = request.getParameter("searchValue");
				if(searchType != null && searchValue != null){
					if(searchType.equals("0")){
						if(searchValue.equals("254")){
							cond += "";
						}else{
							cond += " and A.kitchen_alias = " + searchValue;
						}
					}else if(searchType.equals("1")){
						cond += " and A.name like '%" + searchValue.trim() + "%'";
					}else if(searchType.equals("2")){
						cond += " and A.pinyin like '%" + searchValue.trim() + "%'";
					}else if(searchType.equals("3")){
						cond += " and A.food_alias like '" + searchValue.trim() + "%'";
					}
				}
				root = MenuDao.getFood(cond, orderBy);
			}else if(type.trim().equals("2")){
				root = MenuDao.getFoodTaste(Integer.parseInt(restaurantID));
			}else if(type.trim().equals("3")){
				root = MenuDao.getKitchen(Integer.parseInt(restaurantID));
			}else if(type.trim().equals("4")){
				root = MenuDao.getDepartment(Integer.parseInt(restaurantID));
			}
			
//			Terminal term = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			
//			List<FoodBasic> fl = MenuDao.getFood(term.restaurantID);
//			List<FoodTaste> tl = MenuDao.getFoodTaste(term.restaurantID);
//			List<Kitchen> kl = MenuDao.getKitchen(term.restaurantID);
//			List<Department> dl = MenuDao.getDepartment(term.restaurantID);
			
//			if(type == 1){
//				Food[] foods = QueryMenu.queryFoods("AND FOOD.restaurant_id=" + term.restaurantID, null);
//				jsonResp = jsonResp.replace("$(value)", toJson(foods));
//				
//			}else if(type == 2){
//				Taste[] tastes = QueryMenu.queryTastes(Short.MIN_VALUE, "AND restaurant_id=" + term.restaurantID, null);
//				jsonResp = jsonResp.replace("$(value)", toJson(tastes));
//				
//			}else if(type == 3){
//				Kitchen[] kitchens = QueryMenu.queryKitchens("AND restaurant_id=" + term.restaurantID, null);
//				jsonResp = jsonResp.replace("$(value)", toJson(kitchens));
//				
//			}else if(type == 4){
//				Kitchen[] kitchens = QueryMenu.queryKitchens("AND restaurant_id=" + term.restaurantID, null);
//				jsonResp = toJsonCombo(kitchens);
//			}else{
//				throw new BusinessException(ErrorCode.UNKNOWN);
//			}
//			
//			if(type != 4){
//				jsonResp = jsonResp.replace("$(result)", "true");
//			}
						
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
		}finally{
			if(isPaging != null && isPaging.trim().equals("true") && start != null && limit != null){
				int pageSize = Integer.parseInt(limit);
				int index = Integer.parseInt(start);
				List tempRoot = new ArrayList();
				pageSize = (index + pageSize) > root.size() ? (pageSize - ((index + pageSize) - root.size())) : pageSize;
				for(int i = 0; i < pageSize; i++){
					tempRoot.add(root.get(index + i));
				}
				jobject.setTotalProperty(root.size());
				jobject.setRoot(tempRoot);
			}else{
				jobject.setTotalProperty(root.size());
				jobject.setRoot(root);
			}
			
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
	/**
	 * Convert the foods to jasn format
	 * @param foods
	 * @return
	 */
//	private String toJson(Food[] foods){
//		if(foods.length == 0){
//			return "";
//		}else{
//			StringBuffer value = new StringBuffer();
//			for (int i = 0; i < foods.length; i++) {
//				/**
//				 * The json format to each food item looks like below.
//				 * [厨房编号,"菜品名称",菜品编号,"菜品拼音","￥菜品单价",是否特价,是否推荐,是否停售,是否赠送,是否時價]
//				 */
//				String jsonFood = "[$(kitchen_id),\"$(name)\",$(alias_id),\"$(pinyin)\",\"$(unit)\",$(special),$(recommend),$(soldout),$(gift),$(currPrice)]";
//				jsonFood = jsonFood.replace("$(kitchen_id)", new Short(foods[i].kitchen.aliasID).toString());
//				jsonFood = jsonFood.replace("$(name)", foods[i].name);
//				jsonFood = jsonFood.replace("$(alias_id)", new Integer(foods[i].aliasID).toString());
//				jsonFood = jsonFood.replace("$(pinyin)", foods[i].pinyin);
//				jsonFood = jsonFood.replace("$(unit)", Util.CURRENCY_SIGN + Util.float2String(foods[i].getPrice()));
//				jsonFood = jsonFood.replace("$(special)", foods[i].isSpecial() ? "true" : "false");
//				jsonFood = jsonFood.replace("$(recommend)", foods[i].isRecommend() ? "true" : "false");
//				jsonFood = jsonFood.replace("$(soldout)", foods[i].isSellOut() ? "true" : "false");
//				jsonFood = jsonFood.replace("$(gift)", foods[i].isGift() ? "true" : "false");
//				jsonFood = jsonFood.replace("$(currPrice)", foods[i].isCurPrice() ? "true" : "false");
//				// put each json food info to the value
//				value.append(jsonFood);
//				// the string is separated by comma
//				if (i != foods.length - 1) {
//					value.append("，");
//				}
//			}
//			return value.toString();
//		}
//	}
	
	/**
	 * Convert the taste to json format
	 * @param tastes
	 * @return
	 */
//	private String toJson(Taste[] tastes){
//		
//		StringBuffer value = new StringBuffer();
//
//		for (int i = 0; i < tastes.length; i++) {
//			/**
//			 * The json format to each taste item looks like below.
//			 * [口味编号,口味分类,口味名称,价钱,比例,计算方式]
//			 * “口味分类”的值如下： 0 - 口味 ， 1 - 做法，  2 - 规格
//			 * “计算方式”的值如下：0 - 按价格，1 - 按比例 
//			 */
//			String jsonTaste = "[$(taste_id),$(taste_cate),$(taste_pref),$(taste_price),$(taste_rate),$(calc_type)]";
//			jsonTaste = jsonTaste.replace("$(taste_id)", Integer.toString((tastes[i].aliasID)));
//			jsonTaste = jsonTaste.replace("$(taste_cate)", Integer.toString((tastes[i].category)));
//			jsonTaste = jsonTaste.replace("$(taste_pref)", tastes[i].getPreference().replace(",", ";"));
//			jsonTaste = jsonTaste.replace("$(taste_price)", Util.float2String(tastes[i].getPrice()));
//			jsonTaste = jsonTaste.replace("$(taste_rate)", tastes[i].getRate().toString());
//			jsonTaste = jsonTaste.replace("$(calc_type)", Integer.toString(tastes[i].calc));
//
//			// put each json taste info to the value
//			value.append(jsonTaste);
//			if(i + 1 != tastes.length){
//				// the string is separated by comma
//				value.append("，");
//			}
//		}			
//
//		return value.toString();
//	}
	
	/**
	 * Convert the kitchen to json format
	 * @param kitchens
	 * @return
	 */
//	private String toJson(Kitchen[] kitchens){
//		if(kitchens.length == 0){
//			return "";			
//		}else{
//			StringBuffer value = new StringBuffer();
//			for(int i = 0; i < kitchens.length; i++){
//				/**
//				 * The json format to each kitchen looks like below.
//				 * [厨房编号,厨房id,"厨房名称",一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
//				 */
//				String jsonKitchen = "[$(kitchenAlias),$(kitchenId),$(name),$(dist1),$(dist2),$(dist3),$(memDist1),$(memDist2),$(memDist3)]";
////				jsonKitchen = jsonKitchen.replace("$(kitchenAlias)", new Short(kitchens[i].aliasID).toString());
////				jsonKitchen = jsonKitchen.replace("$(kitchenId)", new Long(kitchens[i].kitchenID).toString());
////				jsonKitchen = jsonKitchen.replace("$(name)", kitchens[i].name);
////				jsonKitchen = jsonKitchen.replace("$(dist1)", kitchens[i].getDist1().toString());
////				jsonKitchen = jsonKitchen.replace("$(dist2)", kitchens[i].getDist2().toString());
////				jsonKitchen = jsonKitchen.replace("$(dist3)", kitchens[i].getDist3().toString());
////				jsonKitchen = jsonKitchen.replace("$(memDist1)", kitchens[i].getMemDist1().toString());
////				jsonKitchen = jsonKitchen.replace("$(memDist2)", kitchens[i].getMemDist2().toString());
////				jsonKitchen = jsonKitchen.replace("$(memDist3)", kitchens[i].getMemDist3().toString());
//				
//				
//				// put each json kitchen info to the value
//				value.append(jsonKitchen);
//				// the string is separated by comma
//				if (i != kitchens.length - 1) {
//					value.append("，");
//				}
//			}
//			return value.toString();
//		}
//	}
	
	/**
	 * Convert the kitchen to json format
	 * @param kitchens for combobox
	 * @return
	 */
//	private String toJsonCombo(Kitchen[] kitchens){
//		String outString = "{\"root\":[";
//		
//		if(kitchens.length == 0){
//					
//		}else{
//			for(int i = 0; i < kitchens.length; i++){
//				
////				outString = outString + "{value:"+ new Short(kitchens[i].aliasID).toString()+ ",";
////				outString = outString + "text:'" + kitchens[i].name + "',id:'" + kitchens[i].kitchenID + "'},";
//
//			}
//			//outString = outString.substring(0, outString.length()-1);
//			outString = outString + "{value:255,text:'空'}";
//
//		}
//		outString = outString + "]}";
//		return outString;
//	}
}
