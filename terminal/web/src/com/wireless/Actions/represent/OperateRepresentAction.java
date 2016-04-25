package com.wireless.Actions.represent;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.represent.RepresentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.member.represent.Represent;
import com.wireless.pojo.staffMgr.Staff;

public class OperateRepresentAction extends DispatchAction{
	
	/**
	 * 更新【我要代言】内容
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String title = request.getParameter("title");
		final String imageId = request.getParameter("imageId");
		final String slogon = request.getParameter("slogon");
		final String finishDate = request.getParameter("finishDate");
		final String recommendPoint = request.getParameter("recommendPoint");
		final String recommendMoney = request.getParameter("recommendMoney");
		final String subscribePoint = request.getParameter("subscribePoint");
		final String subscribeMoney = request.getParameter("subscribeMoney");
		final String commissionRate = request.getParameter("commissionRate");
		final JObject jObject = new JObject();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final Represent.UpdateBuilder builder = new Represent.UpdateBuilder(Integer.parseInt(id));
			
			//标题
			if(title != null){
				builder.setTitle(title);
			}
			
			//imageId
			if(imageId != null && !imageId.isEmpty()){
				builder.setImage(Integer.parseInt(imageId));
			}
			
			//宣传语
			if(slogon != null){
				builder.setSlogon(slogon);
			}
			
			//结束时间
			if(finishDate != null && !finishDate.isEmpty()){
				builder.setFinishDate(finishDate);
			}
			
			//推荐积分
			if(recommendPoint != null && !recommendPoint.isEmpty()){
				builder.setRecommendPoint(Integer.parseInt(recommendPoint));
			}
			
			//推荐金额
			if(recommendMoney != null && !recommendMoney.isEmpty()){
				builder.setRecommendMoney(Float.parseFloat(recommendMoney));
			}
			
			//关注积分
			if(subscribePoint != null && !subscribePoint.isEmpty()){
				builder.setSubscribePoint(Integer.parseInt(subscribePoint));
			}
			
			//关注金额
			if(subscribeMoney != null && !subscribeMoney.isEmpty()){
				builder.setSubscribeMoney(Float.parseFloat(subscribeMoney));
			}
			
			//设置佣金比例
			if(commissionRate != null && !commissionRate.isEmpty()){
				builder.setCommissionRate(Float.parseFloat(commissionRate));
			}
			
			RepresentDao.update(staff, builder);
			jObject.initTip(true, "修改成功");
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 获取【我要代言】内容
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final JObject jObject = new JObject();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final RepresentDao.ExtraCond extraCond = new RepresentDao.ExtraCond();
			
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			
			jObject.setRoot(RepresentDao.getByCond(staff, extraCond));
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
