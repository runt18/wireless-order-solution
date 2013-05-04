package com.wireless.Actions.menuMgr.basic;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.menuMgr.FoodBasic;
import com.wireless.protocol.Food;
import com.wireless.protocol.Terminal;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class QueryMenuMgrAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int index = 0;
		int pageSize = 0;
		if (!(start == null)) {
			index = Integer.parseInt(start);
			pageSize = Integer.parseInt(limit);
		}
		
		Food[] foods = new Food[0];
		
		JObject jobject = new JObject();
		List<FoodBasic> list = new ArrayList<FoodBasic>();
		List<FoodBasic> root = new ArrayList<FoodBasic>();
		FoodBasic item = null;	
		
		// 是否分頁
		String isPaging = request.getParameter("isPaging");
		isPaging = isPaging == null || isPaging.trim().length() == 0 ? "false" : isPaging;

		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
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
			
			String paramsType = request.getParameter("type");
			
			if(paramsType == null || paramsType.trim().length() == 0){
				return null;
			}
			
			// get the type to filter
			int type = Integer.parseInt(paramsType);

			// get the operator to filter
			String ope = request.getParameter("ope");
			if (ope != null && ope.trim().length() > 0) {
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
			filterVal = filterVal == null || filterVal.trim().length() == 0 ? "" : filterVal;

			// combine the operator and filter value
			String filterCondition = null;

			if (type == 1) {
				// 按编号
				filterCondition = " AND FOOD.food_alias " + ope + filterVal;
			} else if (type == 2) {
				// 按名称
				filterCondition = " AND FOOD.name like '%" + filterVal + "%'";
			} else if (type == 3) {
				// 按拼音
				filterCondition = " AND FOOD.pinyin like '" + filterVal + "%'";
			} else if (type == 4) {
				// 按价格
				filterCondition = " AND FOOD.unit_price " + ope + filterVal;
			} else if (type == 5) {
				// 按厨房
				filterCondition = " AND FOOD.kitchen_alias " + ope + filterVal;
			} else {
				// 全部
				filterCondition = "";
			}
			
//			String orderClause = " ORDER BY FOOD.food_id DESC";
			String orderClause = " ORDER BY FOOD.food_alias ";
			
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
			foods = QueryMenu.queryPureFoods(filterCondition + " AND FOOD.restaurant_id=" + term.restaurantID, orderClause);
			
			String imageBrowseDefaultFile = this.getServlet().getInitParameter("imageBrowseDefaultFile");
			String imageBrowsePath = this.getServlet().getInitParameter("imageBrowsePath");
			
			for(int i= 0; i < foods.length; i++){
				Food tp = foods[i];
				item = new FoodBasic();
				item.setRestaurantID(tp.getRestaurantId());
				item.setFoodID((int)tp.getFoodId());
				item.setAliasID(tp.getAliasId());
				item.setFoodName(tp.getName());
				item.setPinyin(tp.getPinyin());
				item.setUnitPrice(tp.getPrice());
				item.getKitchen().setId((int)tp.getKitchen().getId());
				item.getKitchen().setAliasId(tp.getKitchen().getAliasId());
				item.getKitchen().setName(tp.getKitchen().getName() == null || tp.getKitchen().getName().trim().length() == 0 ? "空" : tp.getKitchen().getName());
				item.setStatus(tp.getStatus());
				item.setTasteRefType(tp.getTasteRefType());
				item.setDesc(tp.desc);
				item.setImg(tp.image == null || tp.image.trim().length() == 0 ? imageBrowseDefaultFile : (imageBrowsePath + "/" + tp.getRestaurantId() + "/" + tp.image));
				list.add(item);
			}

		} catch (BusinessException e) {
			e.printStackTrace();
			if (e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED) {
				jobject.initTip(false, "没有获取到餐厅信息,请重新确认");
			} else if (e.getErrCode() == ProtocolError.TERMINAL_EXPIRED) {
				jobject.initTip(false, "终端已过期,请重新确认");
			} else {
				jobject.initTip(false, "没有获取到菜谱信息,请重新确认");
			}
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			dbCon.disconnect();

			if (!jobject.isSuccess()) {
				jobject.setTotalProperty(0);
				jobject.setRoot(null);
			} else {
				
				if(isPaging != null && isPaging.trim().equals("true")){
					// 特荐停送時 筛选
					String isSpecial = request.getParameter("isSpecial");
					String isRecommend = request.getParameter("isRecommend");
					String isFree = request.getParameter("isFree");
					String isStop = request.getParameter("isStop");
					String isCurPrice = request.getParameter("isCurrPrice");
					String isCombination = request.getParameter("isCombination");
					String isHot = request.getParameter("isHot");
					String isWeight = request.getParameter("isWeight");
					isSpecial = isSpecial == null || isSpecial.trim().isEmpty() ? "false" : isSpecial.trim();
					isRecommend = isRecommend == null || isRecommend.trim().isEmpty()? "false" : isRecommend.trim();
					isFree = isFree == null || isFree.trim().isEmpty() ? "false" : isFree.trim();
					isStop = isStop == null || isStop.trim().isEmpty() ? "false" : isStop.trim();
					isCurPrice = isCurPrice == null || isCurPrice.trim().isEmpty() ? "false" : isCurPrice.trim();
					isCombination = isCombination == null || isCombination.trim().isEmpty() ? "false" : isCombination.trim();
					isHot = isHot == null || isHot.trim().isEmpty() ? "false" : isHot.trim();
					isWeight = isWeight == null || isWeight.trim().isEmpty() ? "false" : isWeight.trim();
					
					if (isSpecial.equals("false") && isRecommend.equals("false")
							&& isFree.equals("false") && isStop.equals("false") 
							&& isCurPrice.equals("false") && isCombination.equals("false")
							&& isHot.equals("false") && isWeight.equals("false") ) {
						jobject.setTotalProperty(list.size());
						jobject.setRoot(list);
					} else {
						for(int i = list.size() - 1; i >= 0; i--){
							FoodBasic temp = list.get(i);
							if((isSpecial.equals("true") && Boolean.valueOf(isSpecial) == temp.isSpecial())
									|| (isRecommend.equals("true") && Boolean.valueOf(isRecommend) == temp.isRecommend())
									|| (isFree.equals("true") && Boolean.valueOf(isFree) == temp.isGift()) 
									|| (isStop.equals("true") && Boolean.valueOf(isStop) == temp.isStop()) 
									|| (isCurPrice.equals("true") && Boolean.valueOf(isCurPrice) == temp.isCurrPrice())
									|| (isCombination.equals("true") && Boolean.valueOf(isCombination) == temp.isCombination())
									|| (isHot.equals("true") && Boolean.valueOf(isHot) == temp.isHot())
									|| (isWeight.equals("true") && Boolean.valueOf(isWeight) == temp.isWeight())
								){
								
							}else{
								list.remove(i);
							}
						}
					}
					
					pageSize = (index + pageSize) > list.size() ? (pageSize - ((index + pageSize) - list.size())) : pageSize;
					for(int i = 0; i < pageSize; i++){
						root.add(list.get(index + i));
					}
					
					jobject.setTotalProperty(list.size());
					jobject.setRoot(root);
				}else{
					root = list;
					jobject.setTotalProperty(list.size());
					jobject.setRoot(root);
				}
			}
			
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
}
