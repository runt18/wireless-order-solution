package com.wireless.Actions.represent;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.represent.RepresentChainDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.member.represent.RepresentChain;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class OperateRepresentChainAction extends DispatchAction{
	
	/**
	 * 获取全部关系链
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final String pin = (String)request.getAttribute("pin");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
//		final String restaurantId = request.getParameter("restaurantId");
//		final String subscribeDate = request.getParameter("subscribeDate");
		final String onDuty = request.getParameter("dateBegin");
		final String offDuty = request.getParameter("dateEnd");
		final String recommenedFuzzy = request.getParameter("recommendFuzzy");
		final String subscribeFuzzy = request.getParameter("subscribeFuzzy");
		final JObject jObject = new JObject();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final RepresentChainDao.ExtraCond extraCond = new RepresentChainDao.ExtraCond();
			
			if(recommenedFuzzy != null && !recommenedFuzzy.isEmpty()){
				extraCond.setRecommendFuzzy(recommenedFuzzy);
			}
			
			if(subscribeFuzzy != null && !subscribeFuzzy.isEmpty()){
				extraCond.setSubscribeFuzzy(subscribeFuzzy);
			}
			
			if(onDuty != null && !onDuty.isEmpty() && offDuty != null && !offDuty.isEmpty()){
				extraCond.setRange(onDuty, offDuty);
			}
			
			List<RepresentChain> presentChainList = RepresentChainDao.getByCond(staff, extraCond);
			
			jObject.setTotalProperty(presentChainList.size());
			
			RepresentChain chainStatistics = new RepresentChain(0);
			float recommendMoneyAmount = 0;
			int recommendPointAmount = 0;
			float subscribeMoneyAmount = 0;
			int subscribePointAmount = 0;
			
			for(RepresentChain chain : presentChainList){
				recommendMoneyAmount += chain.getRecommendMoney();
				recommendPointAmount += chain.getRecommendPoint();
				subscribeMoneyAmount += chain.getSubscribeMoney();
				subscribePointAmount += chain.getSubscribePoint();
			}
			
			if(!start.isEmpty() && !limit.isEmpty()){
				presentChainList = DataPaging.getPagingData(presentChainList, true, start, limit);
			}
			
			if(presentChainList.size() > 0){
				chainStatistics.setRecommendMoney(recommendMoneyAmount);
				chainStatistics.setRecommendPoint(recommendPointAmount);
				chainStatistics.setSubscribeMoney(subscribeMoneyAmount);
				chainStatistics.setSubscribePoint(subscribePointAmount);
				presentChainList.add(chainStatistics);
			}

			jObject.setRoot(presentChainList);
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
