package com.wireless.Actions.menuMgr.basic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class QueryMenuMgrAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		JObject jobject = new JObject();
		List<Food> root = null;
		
		String imageBrowseDefaultFile = this.getServlet().getInitParameter("imageBrowseDefaultFile");
		String imageBrowsePath = "http://" + getServlet().getInitParameter("oss_bucket_image") 
				+ "." + getServlet().getInitParameter("oss_outer_point");
		
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String kitchen = request.getParameter("kitchen");
			String deptId = request.getParameter("deptId");
			String operqtor = request.getParameter("operator");
			String alias = request.getParameter("alias");
			String name = request.getParameter("name");
			String pinyin = request.getParameter("pinyin");
			String price = request.getParameter("price");
			String stockStatus = request.getParameter("stockStatus");
			
			String isSpecial = request.getParameter("isSpecial");
			String isRecommend = request.getParameter("isRecommend");
			String isStop = request.getParameter("isStop");
			String isFree = request.getParameter("isFree");
			String isCurrPrice = request.getParameter("isCurrPrice");
			String isCombination = request.getParameter("isCombination");
			String isHot = request.getParameter("isHot");
			String isWeight = request.getParameter("isWeight");
			String isCommission = request.getParameter("isCommission");
			String isTempFood = request.getParameter("isTempFood");
			List<String> statusList = new ArrayList<String>();
			
			String extraCond = "";
			//****************  基本条件处理
			if(operqtor != null && !operqtor.trim().isEmpty() && !operqtor.equals("")){
				try{
					switch(Integer.valueOf(operqtor)){
						case 1:
							operqtor = "=";
							break;
						case 2:
							operqtor = ">=";
							break;
						case 3:
							operqtor = "<=";
							break;
						default:
							operqtor = "=";
					}					
				}catch(NumberFormatException e){
					operqtor = "=";
				}
			}
			if(kitchen != null && !kitchen.trim().isEmpty() && !kitchen.equals("-1")){
				extraCond += (" AND FOOD.kitchen_id = " + kitchen);
			}
			if(deptId != null && !deptId.trim().isEmpty()){
				extraCond += (" AND DEPT.dept_id = " + deptId);
			}
			if(name != null && !name.trim().isEmpty() && !name.equals("")){
				extraCond += (" AND FOOD.name like '%" + name.trim() + "%'");
			}
			if(pinyin != null && !pinyin.trim().isEmpty() && !pinyin.equals("")){
				extraCond += (" AND FOOD.pinyin like '" + pinyin.trim() + "%'");
			}
			if(price != null && !price.trim().isEmpty() && !price.equals("")){
				extraCond += (" AND FOOD.price " + operqtor + price);
			}
			if(stockStatus != null && !stockStatus.trim().isEmpty()){
				extraCond += (" AND FOOD.stock_status = " + stockStatus);
			}
			if(alias != null && !alias.trim().isEmpty() && !alias.equals("")){
				extraCond += (" AND FOOD.food_alias " + operqtor + alias);
			}
			//****************  菜品状态处理
			if(isSpecial != null && isSpecial.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.SPECIAL + ") <> 0");
			}
			if(isRecommend != null && isRecommend.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.RECOMMEND + ") <> 0");
			}
			if(isStop != null && isStop.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.SELL_OUT + ") <> 0");
			}
			if(isFree != null && isFree.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.GIFT + ") <> 0");
			}
			if(isCurrPrice != null && isCurrPrice.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.CUR_PRICE + ") <> 0");
			}
			if(isCombination != null && isCombination.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.COMBO + ") <> 0");
			}
			if(isHot != null && isHot.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.HOT + ") <> 0");
			}
			if(isWeight != null && isWeight.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.WEIGHT + ") <> 0");
			}
			if(isCommission != null && isCommission.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.COMMISSION + ") <> 0");
			}
			if(isTempFood != null && isTempFood.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.TEMP + ") <> 0");
			}
			
			if(!statusList.isEmpty()){
				String strStatus = "";
				for(int i = 0; i < statusList.size(); i++){
					if(i > 0)
						strStatus += " OR ";
					strStatus += statusList.get(i);
				}
				strStatus = (" AND (" + strStatus + ")");
				extraCond += strStatus;
			}
			
			root = new FoodList(FoodDao.getPureByCond(staff, extraCond, null), Food.BY_ALIAS);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				root = DataPaging.getPagingData(root, isPaging, start, limit);
				jobject.setRoot(root);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
