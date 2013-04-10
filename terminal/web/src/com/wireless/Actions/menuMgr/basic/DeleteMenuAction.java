package com.wireless.Actions.menuMgr.basic;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodBasicDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.FoodBasic;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class DeleteMenuAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setContentType("text/json; charset=utf-8");
		
		FoodBasic fb = new FoodBasic();
		JObject jobject = new JObject();
		
		try {			
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
			
			fb.setFoodID(Integer.parseInt(foodID));
			fb.setRestaurantID(Integer.parseInt(restaurantID));
			
			// 获取菜品图片信息,删除菜品成功删除菜品相关信息
			fb = FoodBasicDao.getFoodBasicImage(fb);
			
			FoodBasicDao.deleteFood(fb);
			jobject.initTip(true, "操作成功,已删除菜品相关信息.");
			
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
	private void deleteImageFile(FoodBasic fb) throws Exception{
		try{
			if(fb.getImg() != null && fb.getImg().trim().length() > 0){
				String imageUploadPath = this.getServlet().getInitParameter("imageUploadPath");
				imageUploadPath = imageUploadPath + File.separator + fb.getRestaurantID() + File.separator + fb.getImg();
				File img = new File(imageUploadPath);
				if(img.exists()){
					img.delete();
				}
			}
		}catch(Exception e){
			throw e;
		}
	}
	
	
}
