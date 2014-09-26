package com.wireless.Actions.couponMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.promotion.CouponTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.promotion.CouponType;

public class QueryCouponTypeAction extends DispatchAction{

	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		StringBuilder tree = new StringBuilder();
		
		List<CouponType> list = null;
		try{
			list = CouponTypeDao.get(StaffDao.verify(Integer.parseInt(pin)));
			tree.append("[");
			for (int i = 0; i < list.size(); i++) {
				tree.append(i > 0 ? "," : "");
				tree.append("{");
				tree.append("text:'" + list.get(i).getName() + "'");
				tree.append(",typeName:'" + list.get(i).getName() + "'");
				tree.append(",leaf:true");
				tree.append(",couponTypeId:" + list.get(i).getId());
				tree.append(",price:" + list.get(i).getPrice());
				tree.append(",date:'" + list.get(i).getExpiredFormat()+ "'");
				tree.append(",desc:'" + list.get(i).getComment() + "'");
				tree.append(",image:'" + list.get(i).getImage()+ "'");
				if(list.get(i).isExpired()){
					tree.append(",iconCls : 'btn_error'");
					tree.append(",expired : true");
				}
				
				tree.append("}");
			}
			tree.append("]");
		}catch(BusinessException e){
			e.printStackTrace();
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tree.toString());
		}
		return null;
	}
	
	public ActionForward unExpired(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		StringBuilder tree = new StringBuilder();
		
		List<CouponType> list = null;
		try{
			list = CouponTypeDao.get(StaffDao.verify(Integer.parseInt(pin)));
			tree.append("[");
			for (int i = 0; i < list.size(); i++) {
				if(!list.get(i).isExpired()){
					tree.append(i > 0 ? "," : "");
					tree.append("{");
					tree.append("text:'" + list.get(i).getName() + "'");
					tree.append(",typeName:'" + list.get(i).getName() + "'");
					tree.append(",leaf:true");
					tree.append(",couponTypeId:" + list.get(i).getId());
					tree.append(",price:" + list.get(i).getPrice());
					tree.append(",date:'" + list.get(i).getExpiredFormat()+ "'");
					tree.append(",desc:'" + list.get(i).getComment() + "'");
					tree.append(",image:'" + list.get(i).getImage()+ "'");
					tree.append("}");
				}
			}
			tree.append("]");
		}catch(BusinessException e){
			e.printStackTrace();
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tree.toString());
		}
		return null;
	}	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getImage(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		try{
			String pin = (String) request.getAttribute("pin");
			String couponTypeId = request.getParameter("couponTypeId");
			String restaurantID = (String) request.getAttribute("restaurantID");
			CouponType coupon = null;
			if(couponTypeId != null && !couponTypeId.isEmpty()){
				coupon = CouponTypeDao.getById(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(couponTypeId));

			}
			// 获取菜品原图信息,用于更新图片成功之后删除原文件,否则保留原文件
			 	 
			
/*			if(coupon.getImage() != null && !coupon.getImage().isEmpty()){
				final String oldName = "http://" + getServlet().getInitParameter("oss_bucket_image")
						        		+ "." + getServlet().getInitParameter("oss_outer_point") 
						        		+ "/" + restaurantID + "/" + coupon.getImage();
				jobject.setExtra(new Jsonable(){

					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putString("image", oldName);
						return jm;
					}

					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
					
				});				
			}else{
				jobject.initTip(false, "无图片");
			}*/
			

		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
