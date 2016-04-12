package com.wireless.Actions.menuMgr.basic;

import java.sql.SQLException;
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
		
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String kitchen = request.getParameter("kitchen");
			String deptId = request.getParameter("deptId");
			String alias = request.getParameter("alias");
			String name = request.getParameter("name");
			String pinyin = request.getParameter("pinyin");
			String price = request.getParameter("price");
			
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
			
			FoodDao.ExtraCond extraCond = new FoodDao.ExtraCond();
			if(kitchen != null && !kitchen.trim().isEmpty() && !kitchen.equals("-1")){
				extraCond.setKitchen(Integer.parseInt(kitchen));
			}
			if(deptId != null && !deptId.trim().isEmpty()){
				extraCond.setDepartment(Integer.parseInt(deptId));
			}
			if(name != null && !name.trim().isEmpty() && !name.equals("")){
				extraCond.setName(name);
			}
			if(pinyin != null && !pinyin.trim().isEmpty() && !pinyin.equals("")){
				extraCond.setPinyin(pinyin);
			}
			if(price != null && !price.trim().isEmpty() && !price.equals("")){
				extraCond.setPrice(Float.parseFloat(price));
			}
			if(alias != null && !alias.trim().isEmpty() && !alias.equals("")){
				extraCond.setAlias(Integer.parseInt(alias));
			}
			//****************  菜品状态处理
			if(isSpecial != null && isSpecial.trim().equals("true")){
				extraCond.addStatus(Food.SPECIAL);
			}
			if(isRecommend != null && isRecommend.trim().equals("true")){
				extraCond.addStatus(Food.RECOMMEND);
			}
			if(isStop != null && isStop.trim().equals("true")){
				extraCond.addStatus(Food.SELL_OUT);
			}
			if(isFree != null && isFree.trim().equals("true")){
				extraCond.addStatus(Food.GIFT);
			}
			if(isCurrPrice != null && isCurrPrice.trim().equals("true")){
				extraCond.addStatus(Food.CUR_PRICE);
			}
			if(isCombination != null && isCombination.trim().equals("true")){
				extraCond.addStatus(Food.COMBO);
			}
			if(isHot != null && isHot.trim().equals("true")){
				extraCond.addStatus(Food.HOT);
			}
			if(isWeight != null && isWeight.trim().equals("true")){
				extraCond.addStatus(Food.WEIGHT);
			}
			if(isCommission != null && isCommission.trim().equals("true")){
				extraCond.addStatus(Food.COMMISSION);
			}
			if(isTempFood != null && isTempFood.trim().equals("true")){
				extraCond.addStatus(Food.TEMP);
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
			jobject.initTip4Exception(e);
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
