package com.wireless.Actions.menuMgr.combo;

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
import com.wireless.pojo.staffMgr.Staff;

public class UpdateFoodCombinationAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		JObject jobject = new JObject();
		try{
			
			final String pin = (String)request.getAttribute("pin");
			
			final String foodID = request.getParameter("foodID");
			final String comboContent = request.getParameter("comboContent");
			final String branchId = request.getParameter("branchId");
			
			Staff staff;		
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}else{
				staff = StaffDao.verify(Integer.parseInt(pin));
			}		
			
			if(foodID == null || foodID.trim().length() == 0){
				jobject.initTip(false, "操作失败,获取菜品信息失败!");
				return null;
			}
			
			Food.ComboBuilder comboBuilder = new Food.ComboBuilder(Integer.parseInt(foodID));
			if(!comboContent.isEmpty()){
				String[] sl = comboContent.split("<split>");
				if(sl != null && sl.length != 0){
					for(int i = 0; i < sl.length; i++){
						String[] temp = sl[i].split(",");
						comboBuilder.addChild(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
					}
				}
			}
			FoodDao.buildCombo(staff, comboBuilder);
			jobject.initTip(true, "操作成功,已修改套菜关联信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
}
