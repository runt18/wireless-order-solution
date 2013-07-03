package com.wireless.Actions.inventoryMgr.report;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.StockReportDao;
import com.wireless.db.system.SystemDao;
import com.wireless.json.JObject;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;
import com.wireless.util.WebParams;

public class QueryReportAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		JObject jobject = null;
		try{
			String pin = request.getParameter("pin");
			String start = request.getParameter("start");
			String limit = request.getParameter("limit");
			String beginDate = request.getParameter("beginDate");
			String endDate = request.getParameter("endDate");
			String cateType = request.getParameter("cateType");
			String cateId = request.getParameter("cateId");
			
			Terminal mTerminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			//String orderClause = " LIMIT " + Integer.parseInt(start) + ", " + Integer.parseInt(limit);
			List<StockReport> stockReports = null ;
			List<StockReport> stockReportPage = null ;
			int roots = 0;
			String extra = "";
			extra += " AND S.status = 2";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			if(beginDate == null || cateType == null){
					

				long current = SystemDao.getCurrentMonth(mTerminal);
				Calendar c = Calendar.getInstance();
				c.setTime(new Date(current));
				c.add(Calendar.MONTH, -1);
				int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
				long lastDate = DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + day);
				stockReports = StockReportDao.getStockCollectByTime(mTerminal, sdf.format(c.getTime()), sdf.format(new Date(lastDate)), null);
				
			}else{
				if(cateType.equals("-1") && cateId.equals("-1")){
					stockReports = StockReportDao.getStockCollectByTime(mTerminal, beginDate, endDate, null);
				}else if(!cateType.equals("-1") && cateId.equals("-1")){
					extra += " AND S.cate_type = " + cateType;
					stockReports = StockReportDao.getStockCollectByTypes(mTerminal, beginDate, endDate, extra, null);
				}else{
					extra += " AND M.cate_id = " + cateId; 
					stockReports = StockReportDao.getStockCollectByTypes(mTerminal, beginDate, endDate, extra, null);
				}

			}

			if(stockReports == null){
				roots = 0;
			}else{
				roots = stockReports.size();
				int plus = Integer.parseInt(start)+Integer.parseInt(limit);
				if(plus > roots){
					plus = roots;
				}
				stockReportPage = stockReports.subList(Integer.parseInt(start), plus);
			}
			jobject = new JObject(roots, stockReportPage);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;

	}
	
}
