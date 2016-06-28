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
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.util.DateUtil;

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
				tree.append(",date:'" + DateUtil.formatToDate(list.get(i).getEndExpired()) + "'");
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
					tree.append(",date:'" + DateUtil.formatToDate(list.get(i).getEndExpired()) + "'");
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

}
