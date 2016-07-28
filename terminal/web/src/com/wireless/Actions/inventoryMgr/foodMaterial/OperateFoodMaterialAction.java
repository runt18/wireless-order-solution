package com.wireless.Actions.inventoryMgr.foodMaterial;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.inventoryMgr.FoodMaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.FoodMaterial;

public class OperateFoodMaterialAction extends DispatchAction{

	/**
	 * 更新菜品原料配置
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantId = (String) request.getAttribute("restaurantID");
			String foodId = request.getParameter("foodId");
			String content = request.getParameter("content");
			
			List<FoodMaterial> list = new ArrayList<FoodMaterial>();
			FoodMaterial item = null;
			// 解释前台数据格式
			String[] sstr = content.split("<sp>");
			String[] strItem = null;
			if(sstr.length == 1){
				if(!sstr[0].trim().isEmpty()){
					strItem = sstr[0].split(",");
					item = new FoodMaterial(Integer.valueOf(restaurantId), 
							Integer.valueOf(foodId), 
							Integer.valueOf(strItem[0].trim()),
							Float.valueOf(strItem[1].trim())
							);
					list.add(item);
				}
			}else if(sstr.length > 1){
				for(int i = 0; i < sstr.length; i++){
					strItem = sstr[i].split(",");
					item = new FoodMaterial(Integer.valueOf(restaurantId), 
							Integer.valueOf(foodId), 
							Integer.valueOf(strItem[0].trim()),
							Float.valueOf(strItem[1].trim())
							);
					list.add(item);
				}
			}
			item = null;
			strItem = null;
			FoodMaterialDao.update(Integer.valueOf(foodId), list);
			jobject.initTip(true, "操作成功, 已保存菜品原料配置信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
