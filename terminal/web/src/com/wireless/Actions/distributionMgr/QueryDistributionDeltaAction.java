package com.wireless.Actions.distributionMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.distributionMgr.DistributionDeltaDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.distributionMgr.DistributionDelta;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class QueryDistributionDeltaAction extends DispatchAction{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String materialId = request.getParameter("materialId");
		final String fuzzyId = request.getParameter("fuzzyId");
		final String minDeltaAmount = request.getParameter("minDeltaAmount");
		final String maxDeltaAmount = request.getParameter("maxDeltaAmount");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		try {
			DistributionDeltaDao.ExtraCond extraCond = new DistributionDeltaDao.ExtraCond();
			
			if(materialId != null && !materialId.isEmpty()){
				extraCond.setMaterialId(Integer.parseInt(materialId));
			}
			
			if(fuzzyId != null && !fuzzyId.isEmpty()){
				extraCond.setDistributionId(Integer.parseInt(fuzzyId));
			}
			
			if(minDeltaAmount != null && !minDeltaAmount.isEmpty()){
				extraCond.setMinDeltaAmount(Integer.parseInt(minDeltaAmount));
			}
			
			if(maxDeltaAmount != null && !maxDeltaAmount.isEmpty()){
				extraCond.setMaxDeltaAmount(Integer.parseInt(maxDeltaAmount));
			}
			
			if(beginDate != null && !beginDate.isEmpty() && endDate != null && !endDate.isEmpty()){
				extraCond.setRange(beginDate, endDate);
			}
			
			List<DistributionDelta> result = DistributionDeltaDao.getByCond(staff, extraCond);
			
			jObject.setTotalProperty(result.size());
			
			DistributionDelta sum = new DistributionDelta();
			if(result.size() > 0){
				for(DistributionDelta delta : result){
					sum.setDistributionSendAmount(sum.getDistributionSendAmount() + delta.getDistributionSendAmount());
					sum.setDistributionReceiveAmount(sum.getDistributionReceiveAmount() + delta.getDistributionReceiveAmount());
					sum.setDistributionReturnAmount(sum.getDistributionReturnAmount() + delta.getDistributionReturnAmount());
					sum.setDistributionRecoveryAmount(sum.getDistributionRecoveryAmount() + delta.getDistributionRecoveryAmount());
				}
			}
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				result = DataPaging.getPagingData(result, true, start, limit);
			}
			
			if(result.size() > 0){
				result.add(sum);
			}
			
			jObject.setRoot(result);
			
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip(e);
		} finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
