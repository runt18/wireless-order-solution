package com.wireless.Actions.printScheme;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.printScheme.PrintFuncDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.staffMgr.Staff;

public class QueryPrintFuncAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		List<PrintFunc> root ;
		try{
			String pin = (String)request.getAttribute("pin");
			String printerId = request.getParameter("printerId");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			root = PrintFuncDao.getByCond(staff, new PrintFuncDao.ExtraCond().setPrinter(Integer.parseInt(printerId)));
			List<PrintFunc> roots = new ArrayList<PrintFunc>();

			boolean detail = true;
			for (PrintFunc printFunc : root) {
				if(printFunc.getType() == PType.PRINT_ORDER_DETAIL || printFunc.getType() == PType.PRINT_CANCELLED_FOOD_DETAIL){
					if(printFunc.getType() == PType.PRINT_ORDER_DETAIL ){
						if(detail){
							roots.add(printFunc);
							detail = false;
						}
					}else if(printFunc.getType() == PType.PRINT_CANCELLED_FOOD_DETAIL){
						if(detail){
							printFunc.setType(PType.PRINT_ORDER_DETAIL);
							roots.add(printFunc);
							detail = false;
						}						
					}
				}else{
					roots.add(printFunc);
				}
				
			}
			
			
			jobject.setTotalProperty(roots.size());
			jobject.setRoot(roots);
			
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
