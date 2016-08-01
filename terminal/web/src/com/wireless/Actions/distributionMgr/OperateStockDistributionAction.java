package com.wireless.Actions.distributionMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.distributionMgr.StockDistributionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.distributionMgr.StockDistribution;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DataPaging;

public class OperateStockDistributionAction extends DispatchAction{
	
	/**
	 * 库单录入
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String stockInRestaurant = request.getParameter("stockInRestaurant");
		final String stockOutRestaurant = request.getParameter("stockOutRestaurant");
		final String oriId = request.getParameter("oriId");
		final String oriDate = request.getParameter("oriDate");
		final String comment = request.getParameter("comment");
		final String associateId = request.getParameter("associateId");
		final String subType = request.getParameter("subType");
		final String detail = request.getParameter("detail");
		final String cateType = request.getParameter("cateType");
		final String actualPrice = request.getParameter("actualPrice");
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final StockAction.InsertBuilder stockActionBuilder;
			final StockDistribution.InsertBuilder insertBuilder;
			if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_SEND.getVal()){
				//建立builder
				stockActionBuilder = StockAction.InsertBuilder.newDistributionSend();
				insertBuilder = StockDistribution.InsertBuilder.newDistributionSend(stockActionBuilder);
				if(associateId != null && !associateId.isEmpty()){
					insertBuilder.setAssociateId(Integer.parseInt(associateId));
				}
				//设置单店出货部门
				insertBuilder.getBuilder().setDeptOut(DepartmentDao.getByType(StaffDao.getAdminByRestaurant(Integer.parseInt(stockOutRestaurant)), Department.Type.WARE_HOUSE).get(0));
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_RECEIVE.getVal()){
				stockActionBuilder = StockAction.InsertBuilder.newDistributionReceive();
				insertBuilder = StockDistribution.InsertBuilder.newDistributionRecevie(stockActionBuilder, Integer.parseInt(associateId));
				insertBuilder.getBuilder().setDeptIn(DepartmentDao.getByType(StaffDao.getAdminByRestaurant(Integer.parseInt(stockInRestaurant)), Department.Type.WARE_HOUSE).get(0));
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_RETURN.getVal()){
				stockActionBuilder = StockAction.InsertBuilder.newDistributionReturn();
				insertBuilder = StockDistribution.InsertBuilder.newDistributionReturn(stockActionBuilder, Integer.parseInt(associateId));
				insertBuilder.getBuilder().setDeptOut(DepartmentDao.getByType(StaffDao.getAdminByRestaurant(Integer.parseInt(stockOutRestaurant)), Department.Type.WARE_HOUSE).get(0));
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_RECOVERY.getVal()){
				stockActionBuilder = StockAction.InsertBuilder.newDistributionRecovery();
				insertBuilder = StockDistribution.InsertBuilder.newDistributionRecovery(stockActionBuilder, Integer.parseInt(associateId));
				insertBuilder.getBuilder().setDeptIn(DepartmentDao.getByType(StaffDao.getAdminByRestaurant(Integer.parseInt(stockInRestaurant)), Department.Type.WARE_HOUSE).get(0));
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_APPLY.getVal()){
				stockActionBuilder = StockAction.InsertBuilder.newDistributionApply();
				insertBuilder = StockDistribution.InsertBuilder.newDistributionApply(stockActionBuilder);
			}else{
				throw new IllegalArgumentException("没有选择库单的类型 ,不能建立库单");
			}
			
			stockActionBuilder.setOperatorId(staff.getId()).setOperator(staff.getName());
			
			if(actualPrice != null && !actualPrice.isEmpty()){
				insertBuilder.getBuilder().setInitActualPrice(Float.parseFloat(actualPrice));
			}
			
			if(oriId != null && !oriId.isEmpty()){
				insertBuilder.getBuilder().setOriStockId(oriId);
			}
			
			if(oriDate != null && !oriDate.isEmpty()){
				insertBuilder.getBuilder().setOriStockDate(DateUtil.parseDate(oriDate));
			}
			
			if(comment != null && !comment.isEmpty()){
				insertBuilder.getBuilder().setComment(comment);
			}
			
			if(cateType != null && !cateType.isEmpty()){
				insertBuilder.getBuilder().setCateType(Integer.valueOf(cateType));
			}
			
			if(detail != null && !detail.isEmpty()){
				String[] content = detail.split("<sp>");
				for(String temp : content){
					String[] item = temp.split("<spst>");
					insertBuilder.getBuilder().addDetail(new StockActionDetail(Integer.valueOf(item[0]), Float.valueOf(item[1]), Float.valueOf(item[2])));
				}
			}
			
			if(stockInRestaurant != null && !stockInRestaurant.isEmpty()){
				insertBuilder.setStockInRestaurant(Integer.parseInt(stockInRestaurant));
				insertBuilder.getBuilder().setStockInRestaurantId(Integer.parseInt(stockInRestaurant));
			}else{
				throw new BusinessException("配送库单必须输入收货门店");
			}
			
			if(stockOutRestaurant != null && !stockOutRestaurant.isEmpty()){
				insertBuilder.setStockOutRestaurant(Integer.parseInt(stockOutRestaurant));
				insertBuilder.getBuilder().setStockOutRestaurantId(Integer.parseInt(stockOutRestaurant));
			}else{
				throw new BusinessException("配送库单必须输入出货门店");
			}
			
			StockDistributionDao.insert(staff, insertBuilder);
			jObject.initTip(true, "库单录入成功");
				
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip(e);
		} finally {
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	/**
	 * 查询功能
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String cateType = request.getParameter("cateType");
		final String id = request.getParameter("id");
		final String stockActionId = request.getParameter("stockActionId");
		final String fuzzId = request.getParameter("fuzzId");
		final String stockType = request.getParameter("stockType");
		final String oriStockId = request.getParameter("oriStockId");
		final String subType = request.getParameter("subType");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String isHistory = request.getParameter("isHistory");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String comment = request.getParameter("comment");
//		final String isWithOutSum = request.getParameter("isWithOutSum");
		final String cateId = request.getParameter("cateId");
		final String stockInRestaurant = request.getParameter("stockInRestaurant");
		final String stockOutRestaurant = request.getParameter("stockOutRestaurant");
		final String distributionStatus = request.getParameter("distributionStatus");
		final String actionStatus = request.getParameter("actionStatus");
		final String associateId = request.getParameter("associateId");
		final String containsDetail = request.getParameter("containsDetail");
		final String restaurantId = request.getParameter("restaurantId");
		final String isGroupDistirbution = request.getParameter("isGroupDistirbution");
		final String isWidthOutUnAudit = request.getParameter("isWidthOutUnAudit");
		final String isBranchConcatGroup = request.getParameter("isBranchConcatGroup");
		
		try {
			final StockDistributionDao.ExtraCond extraCond = new StockDistributionDao.ExtraCond();
			final StockActionDao.ExtraCond extraCond4StockAction = new StockActionDao.ExtraCond();
			final Staff staff;
			if(restaurantId != null && !restaurantId.isEmpty() && Integer.parseInt(restaurantId) > 0){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(restaurantId));
			}else{
				staff = StaffDao.verify(Integer.parseInt(pin));
			}
			
			if(stockActionId != null && !stockActionId.isEmpty()){
				extraCond.setStockActionId(Integer.parseInt(stockActionId));
			}
			
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			
			if(stockInRestaurant != null && !stockInRestaurant.isEmpty() && Integer.parseInt(stockInRestaurant) > 0){
				extraCond.setStockInRestaurantId(Integer.parseInt(stockInRestaurant));
			}
			
			if(stockOutRestaurant != null && !stockOutRestaurant.isEmpty() && Integer.parseInt(stockOutRestaurant) > 0){
				extraCond.setStockOutRestaurantId(Integer.parseInt(stockOutRestaurant));
			}

			if(associateId != null && !associateId.isEmpty()){
				extraCond.setAssociateId(Integer.parseInt(associateId));
			}
			
			if(distributionStatus != null && !distributionStatus.isEmpty() && Integer.parseInt(distributionStatus) > 0){
				extraCond.setStatus(Integer.parseInt(distributionStatus));
			}
			
			if(cateType != null && !cateType.isEmpty() && Integer.parseInt(cateType) > 0){
				extraCond4StockAction.setMaterialCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)));
			}
			
			if(cateId != null && !cateId.isEmpty()){
				extraCond4StockAction.setCateId(Integer.parseInt(cateId));
			}
			
			if(subType != null && !subType.isEmpty() && Integer.parseInt(subType) > 0){
				extraCond4StockAction.addSubType(StockAction.SubType.valueOf(Integer.parseInt(subType)));
			}
			
			if(stockType != null && !stockType.isEmpty()){
				extraCond4StockAction.setType(StockAction.Type.valueOf(Integer.parseInt(stockType)));
			}
			
			if(isBranchConcatGroup != null && !isBranchConcatGroup.isEmpty()){
				extraCond.setIsBranchConcatGroup(true);
			}
			
			if(isHistory != null && !isHistory.isEmpty() && Boolean.parseBoolean(isHistory)){
				if(beginDate != null && !beginDate.trim().isEmpty() && endDate != null && !endDate.isEmpty()){
					extraCond4StockAction.setOriDate(beginDate, endDate);
				}else{
					extraCond4StockAction.setHistory(true);
				}
			}else{
				// 只能查询当前会计月份数据
				extraCond4StockAction.setCurrentMonth(true);
			}
			
			if(fuzzId != null && !fuzzId.isEmpty()){
//				extraCond4StockAction.setFuzzId(fuzzId);
				extraCond.setFuzzyId(Integer.parseInt(fuzzId));
			}
			
			if(oriStockId != null && !oriStockId.isEmpty()){
				extraCond4StockAction.setOriId(oriStockId);
			}
			
			if(comment != null && !comment.isEmpty()){
				extraCond4StockAction.setComment(comment);
			}
			
			if(isWidthOutUnAudit != null && !isWidthOutUnAudit.isEmpty() && Boolean.parseBoolean(isWidthOutUnAudit)){
				extraCond4StockAction.addStatus(StockAction.Status.AUDIT).addStatus(StockAction.Status.RE_AUDIT).addStatus(StockAction.Status.FINAL);
			}else{
				if(actionStatus != null && !actionStatus.isEmpty() && Integer.parseInt(actionStatus) > 0){
					extraCond4StockAction.addStatus(StockAction.Status.valueOf(Integer.parseInt(actionStatus)));
				}
			}
			
			if(containsDetail != null && !containsDetail.isEmpty()){
				extraCond.setContainDetails(Boolean.parseBoolean(containsDetail));
			}
			
			if(isGroupDistirbution != null && !isGroupDistirbution.isEmpty()){
				extraCond.setIsGroupDistribution(Boolean.parseBoolean(isGroupDistirbution));
			}else{
				extraCond.setIsBranchConcatGroup(true);
			}
			
			extraCond.setCond4StockAction(extraCond4StockAction);
			
			List<StockDistribution> result = StockDistributionDao.getByCond(staff, extraCond);
			
			jObject.setTotalProperty(result.size());
			
			//分页
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				result = DataPaging.getPagingData(result, true, start, limit);
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
	
	/**
	 * 初始化配送货品
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward initStockDistribution(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final String pin = (String)request.getAttribute("pin");
		final JObject jObject = new JObject();
		try {
			StockDistributionDao.init(StaffDao.verify(Integer.parseInt(pin)));
			jObject.initTip(true, "初始化配送成功");
		} catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
		} finally {
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	/**
	 * 货品配送同步
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward sync(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final String pin = (String)request.getAttribute("pin");
		final JObject jObject = new JObject();
		try {
			StockDistributionDao.sync(StaffDao.verify(Integer.parseInt(pin)));
			jObject.initTip(true, "配送同步成功");
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip(e);
		} finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 审核
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward audit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final JObject jObject = new JObject();
		try {
			StockDistributionDao.audit(StaffDao.verify(Integer.parseInt(pin)), new StockDistribution.AuditBuilder(Integer.parseInt(id)));
			jObject.initTip(true, "审核成功");
			
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip(e);
		} finally{
			response.getWriter().println(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 删除
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward deleteById(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final JObject jObject = new JObject();
		final String id = request.getParameter("id");
		final String pin = (String)request.getAttribute("pin");
		try {
			StockDistributionDao.deleteById(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(id));
			jObject.initTip(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip(e);
		} finally{
			response.getWriter().println(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 更新
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String stockActionId = request.getParameter("stockActionId");
		final String stockInRestaurant = request.getParameter("stockInRestaurant");
		final String stockOutRestaurant = request.getParameter("stockOutRestaurant");
		final String oriId = request.getParameter("oriId");
		final String oriDate = request.getParameter("oriDate");
		final String comment = request.getParameter("comment");
//		final String associateId = request.getParameter("associateId");
		final String subType = request.getParameter("subType");
		final String detail = request.getParameter("detail");
//		final String cateType = request.getParameter("cateType");
		final String actualPrice = request.getParameter("actualPrice");
		
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final StockAction.UpdateBuilder builder2StockAction;
			final StockDistribution.UpdateBuilder builder2Distribution;
			
			if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_SEND.getVal()){
				builder2StockAction = StockAction.UpdateBuilder.newDistributionSend(Integer.parseInt(stockActionId));
				builder2Distribution = StockDistribution.UpdateBuilder.newDistributionSend(Integer.parseInt(id), builder2StockAction);
			
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_RECEIVE.getVal()){
				builder2StockAction = StockAction.UpdateBuilder.newDistributionReceive(Integer.parseInt(stockActionId));
				builder2Distribution = StockDistribution.UpdateBuilder.newDistributionRecevie(Integer.parseInt(id), builder2StockAction);
			
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_RETURN.getVal()){
				builder2StockAction = StockAction.UpdateBuilder.newDistributionReturn(Integer.parseInt(stockActionId));
				builder2Distribution = StockDistribution.UpdateBuilder.newDistributionReturn(Integer.parseInt(id), builder2StockAction);
			
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_RECOVERY.getVal()){
				builder2StockAction = StockAction.UpdateBuilder.newDistributionRecovery(Integer.parseInt(stockActionId));
				builder2Distribution = StockDistribution.UpdateBuilder.newDistributionRecovery(Integer.parseInt(id), builder2StockAction);
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_APPLY.getVal()){
				builder2StockAction = StockAction.UpdateBuilder.newDistributionApply(Integer.parseInt(stockActionId));
				builder2Distribution = StockDistribution.UpdateBuilder.newDistributionApply(Integer.parseInt(id), builder2StockAction);
			}else{
				throw new IllegalArgumentException("没有选择库单的类型 ,不能建立库单");
			}
			
			builder2StockAction.setOperatorId(staff.getId()).setOperator(staff.getName());
			
			if(stockInRestaurant != null && !stockInRestaurant.isEmpty()){
				builder2Distribution.setStockInRestaurant(Integer.parseInt(stockInRestaurant));
			}
			
			if(stockOutRestaurant != null && !stockOutRestaurant.isEmpty()){
				builder2Distribution.setStockOutRestaurant(Integer.parseInt(stockOutRestaurant));
			}

			if(actualPrice != null && !actualPrice.isEmpty()){
				builder2StockAction.setInitActualPrice(Float.parseFloat(actualPrice));
			}
			
			if(oriId != null && !oriId.isEmpty()){
				builder2StockAction.setOriStockId(oriId);
			}
			
			if(oriDate != null && !oriDate.isEmpty()){
				builder2StockAction.setOriStockDate(DateUtil.parseDate(oriDate));
			}
			
			if(comment != null && !comment.isEmpty()){
				builder2StockAction.setComment(comment);
			}
			
			if(detail != null && !detail.isEmpty()){
				String[] content = detail.split("<sp>");
				for(String temp : content){
					String[] item = temp.split("<spst>");
					builder2StockAction.addDetail(new StockActionDetail(Integer.valueOf(item[0]), Float.valueOf(item[1]), Float.valueOf(item[2])));
				}
			}
			
			StockDistributionDao.update(staff, builder2Distribution);
			jObject.initTip(true, "库单更新成功");
		} catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
		} finally {
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 反审核
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward reAudit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)throws Exception{
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String stockActionId = request.getParameter("stockActionId");
		final String stockInRestaurant = request.getParameter("stockInRestaurant");
		final String stockOutRestaurant = request.getParameter("stockOutRestaurant");
		final String oriId = request.getParameter("oriId");
		final String oriDate = request.getParameter("oriDate");
		final String comment = request.getParameter("comment");
//		final String associateId = request.getParameter("associateId");
		final String subType = request.getParameter("subType");
		final String detail = request.getParameter("detail");
//		final String cateType = request.getParameter("cateType");
		final String actualPrice = request.getParameter("actualPrice");
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final StockAction.ReAuditBuilder builder2StockAction;
			final StockDistribution.ReAuditBuilder builder2Distribution;
			
			if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_SEND.getVal()){
				builder2StockAction = StockAction.ReAuditBuilder.newDistributionSend(Integer.parseInt(stockActionId));
				builder2Distribution = StockDistribution.ReAuditBuilder.newDistributionSend(Integer.parseInt(id), builder2StockAction);
//				builder2Distribution.getBuilder().setDeptOut(DepartmentDao.getByType(StaffDao.getAdminByRestaurant(Integer.parseInt(stockOutRestaurant)), Department.Type.WARE_HOUSE).get(0));
			
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_RECEIVE.getVal()){
				builder2StockAction = StockAction.ReAuditBuilder.newDistributionReceive(Integer.parseInt(stockActionId));
				builder2Distribution = StockDistribution.ReAuditBuilder.newDistributionRecevie(Integer.parseInt(id), builder2StockAction);
//				builder2Distribution.getBuilder().setDeptIn(DepartmentDao.getByType(StaffDao.getAdminByRestaurant(Integer.parseInt(stockInRestaurant)), Department.Type.WARE_HOUSE).get(0));
			
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_RETURN.getVal()){
				builder2StockAction = StockAction.ReAuditBuilder.newDistributionReturn(Integer.parseInt(stockActionId));
				builder2Distribution = StockDistribution.ReAuditBuilder.newDistributionReturn(Integer.parseInt(id), builder2StockAction);
//				builder2Distribution.getBuilder().setDeptIn(DepartmentDao.getByType(StaffDao.getAdminByRestaurant(Integer.parseInt(stockInRestaurant)), Department.Type.WARE_HOUSE).get(0));
			
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_RECOVERY.getVal()){
				builder2StockAction = StockAction.ReAuditBuilder.newDistributionRecovery(Integer.parseInt(stockActionId));
				builder2Distribution = StockDistribution.ReAuditBuilder.newDistributionRecovery(Integer.parseInt(id), builder2StockAction);
//				builder2Distribution.getBuilder().setDeptIn(DepartmentDao.getByType(StaffDao.getAdminByRestaurant(Integer.parseInt(stockInRestaurant)), Department.Type.WARE_HOUSE).get(0));
			}else if(Integer.parseInt(subType) == StockAction.SubType.DISTRIBUTION_APPLY.getVal()){
				builder2StockAction = StockAction.ReAuditBuilder.newDistributionApply(Integer.parseInt(stockActionId));
				builder2Distribution = StockDistribution.ReAuditBuilder.newDistributionApply(Integer.parseInt(id), builder2StockAction);
			}else{
				throw new IllegalArgumentException("没有选择库单的类型 ,不能建立库单");
			}
			
			builder2StockAction.setOperatorId(staff.getId()).setOperator(staff.getName());
			
			if(stockInRestaurant != null && !stockInRestaurant.isEmpty()){
				builder2Distribution.setStockInRestaurant(Integer.parseInt(stockInRestaurant));
			}
			
			if(stockOutRestaurant != null && !stockOutRestaurant.isEmpty()){
				builder2Distribution.setStockOutRestaurant(Integer.parseInt(stockOutRestaurant));
			}
			
			if(actualPrice != null && !actualPrice.isEmpty()){
				builder2StockAction.setInitActualPrice(Float.parseFloat(actualPrice));
			}
			
			if(oriId != null && !oriId.isEmpty()){
				builder2StockAction.setOriStockId(oriId);
			}
			
			if(oriDate != null && !oriDate.isEmpty()){
				builder2StockAction.setOriStockDate(DateUtil.parseDate(oriDate));
			}
			
			if(comment != null && !comment.isEmpty()){
				builder2StockAction.setComment(comment);
			}
			
			if(detail != null && !detail.isEmpty()){
				String[] content = detail.split("<sp>");
				for(String temp : content){
					String[] item = temp.split("<spst>");
					builder2StockAction.addDetail(new StockActionDetail(Integer.valueOf(item[0]), Float.valueOf(item[1]), Float.valueOf(item[2])));
				}
			}
			
			StockDistributionDao.reAudit(staff, builder2Distribution);
			jObject.initTip(true, "库单反审核成功成功");
			
		} catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
		} finally {
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
