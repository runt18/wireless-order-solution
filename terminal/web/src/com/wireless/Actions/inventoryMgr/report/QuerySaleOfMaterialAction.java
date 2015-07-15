package com.wireless.Actions.inventoryMgr.report;

import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.inventoryMgr.SaleOfMaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockInitDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.SaleOfMaterial;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class QuerySaleOfMaterialAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String beginDate = request.getParameter("beginDate");
			String endDate = "";
			String materialId = request.getParameter("materialId");
			String deptId = request.getParameter("deptId");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			endDate = beginDate + "-31";
			beginDate += "-01";
			
			long stockInitDateTime = StockInitDao.getInitDate(staff);
			
			if(stockInitDateTime > 0){
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(stockInitDateTime);
				String stockInitDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-01";
				
				if(DateUtil.parseDate(stockInitDate) == DateUtil.parseDate(beginDate)){
					beginDate = DateUtil.format(stockInitDateTime);
				}else{
					beginDate += "-01";
				}
			}else{
				
			}
			
			
			
			String extra = " AND OF.order_date BETWEEN '" + beginDate + "' AND '" + endDate + " 23:59:59'";
				
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extra += " AND K.dept_id = " + deptId;
			}
			
			List<SaleOfMaterial> list = SaleOfMaterialDao.saleOfMaterialList(staff, Integer.parseInt(materialId), extra, null);

			jobject.setTotalProperty(list.size());
			
			if(list.size() > 0){
				SaleOfMaterial sum = new SaleOfMaterial();
				sum.setFood(new Food(-1));
				for (SaleOfMaterial saleOfMaterial : list) {
					sum.setConsume(sum.getConsume() + saleOfMaterial.getConsume());
				}
				list.add(sum);
			}
			
			
			jobject.setRoot(list);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;

	}
}
