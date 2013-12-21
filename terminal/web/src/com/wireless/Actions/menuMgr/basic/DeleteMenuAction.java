package com.wireless.Actions.menuMgr.basic;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class DeleteMenuAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		Food fb = new Food();
		JObject jobject = new JObject();
		
		try {			
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = request.getParameter("restaurantID");
			String foodID = request.getParameter("foodID");
			
			if(restaurantID == null || restaurantID.trim().length() == 0){
				jobject.initTip(false, "操作失败,获取餐厅信息失败.");
				return null;
			}
			if(foodID == null || foodID.trim().length() == 0){
				jobject.initTip(false, "操作失败,获取菜品信息失败.");
				return null;
			}
			
			fb.setFoodId(Integer.parseInt(foodID));
			fb.setRestaurantId(Integer.parseInt(restaurantID));
			
			// 获取菜品图片信息, 删除菜品成功删除菜品相关信息
			fb = FoodDao.getFoodBasicImage(fb);
			
			FoodDao.deleteFood(fb);
			
			jobject.initTip(true, "操作成功, 已删除菜品相关信息.");
			try{
				deleteImageFile(fb);
			}catch(Exception e){
				jobject.setMsg(jobject.getMsg() + "但删除菜品图片失败.");
			}
		} catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}

		return null;
	}
	
	/**
	 * 
	 * @param fb
	 * @throws Exception
	 */
	private void deleteImageFile(Food fb) throws Exception{
		if(fb.getImage() != null && fb.getImage().trim().length() > 0){
			String imageUploadPath = this.getServlet().getInitParameter("imageUploadPath");
			imageUploadPath = imageUploadPath + File.separator + fb.getRestaurantId() + File.separator + fb.getImage();
			File img = new File(imageUploadPath);
			if(img.exists()){
				img.delete();
			}
		}
	}
	
}
