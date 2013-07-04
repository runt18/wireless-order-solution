package com.wireless.Actions.inventoryMgr.stockTake;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.StockTakeDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.pojo.stockMgr.StockTake.CateType;
import com.wireless.pojo.stockMgr.StockTake.InsertStockTakeBuilder;
import com.wireless.pojo.stockMgr.StockTake.UpdateStockTakeBuilder;
import com.wireless.pojo.stockMgr.StockTakeDetail.InsertStockTakeDetail;
import com.wireless.protocol.Terminal;
import com.wireless.util.WebParams;

public class OperateStockTakeAction extends DispatchAction{

	/**
	 * 新增盘点任务
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String cateType = request.getParameter("cateType");
			String dept = request.getParameter("dept");
			String comment = request.getParameter("comment");
			String cateId = request.getParameter("cateId");
			String detail = request.getParameter("detail");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			InsertStockTakeBuilder builder = new InsertStockTakeBuilder(term.restaurantID)
				.setCateType(CateType.valueOf(Integer.valueOf(cateType)))
				.setDept(new Department(term.restaurantID, Short.valueOf(dept), null))
				.setOperatorId((int) term.pin).setOperator(term.owner)
				.setComment(comment);
			if(cateId != null && !cateId.trim().isEmpty()){
				builder.setCateId(Integer.valueOf(cateId));
			}
			
			String[] content = detail.split("<sp>");
			for(String temp : content){
				String[] item = temp.split("<spst>");
				builder.addStockTakeDetail(new InsertStockTakeDetail().setMaterialId(Integer.valueOf(item[0])).setActualAmount(Float.valueOf(item[1])).build());
			}
			
			StockTakeDao.insertStockTake(term, builder);
			jobject.initTip(true, "操作成功, 已增加新盘点任务.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
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
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String id = request.getParameter("id");
			String cateType = request.getParameter("cateType");
			String dept = request.getParameter("dept");
			String comment = request.getParameter("comment");
			String cateId = request.getParameter("cateId");
			String detail = request.getParameter("detail");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			InsertStockTakeBuilder builder = new InsertStockTakeBuilder(term.restaurantID)
				.setCateType(CateType.valueOf(Integer.valueOf(cateType)))
				.setDept(new Department(term.restaurantID, Short.valueOf(dept), null))
				.setOperatorId((int) term.pin).setOperator(term.owner)
				.setComment(comment);
			if(cateId != null && !cateId.trim().isEmpty()){
				builder.setCateId(Integer.valueOf(cateId));
			}
			
			String[] content = detail.split("<sp>");
			for(String temp : content){
				String[] item = temp.split("<spst>");
				builder.addStockTakeDetail(new InsertStockTakeDetail().setMaterialId(Integer.valueOf(item[0])).setActualAmount(Float.valueOf(item[1])).build());
			}
			
			StockTakeDao.updateStockTake(term, Integer.valueOf(id), builder);
			jobject.initTip(true, "操作成功, 已修改盘点任务内容.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
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
	public ActionForward cancel(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String id = request.getParameter("id");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			StockTakeDao.deleteStockTakeById(term, Integer.valueOf(id));
			jobject.initTip(true, "操作成功, 已取消选中盘点任务.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
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
	public ActionForward audit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String id = request.getParameter("id");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			UpdateStockTakeBuilder uBuilder = StockTake.UpdateStockTakeBuilder.newAudit(Integer.valueOf(id))
					.setApproverId((int)term.pin).setApprover(term.owner);
			
			StockTakeDao.auditStockTake(term, uBuilder);
			jobject.initTip(true, "操作成功, 已审核选中盘点任务.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
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
	public ActionForward miss(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String id = request.getParameter("id");
			String miss = request.getParameter("miss");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			if(miss.equals("1")){
				StockTakeDao.keep(term, Integer.valueOf(id));
			}else if(miss.equals("0")){
				StockTakeDao.reset(term, Integer.valueOf(id));
			}
			jobject.initTip(true, "操作成功, 已处理该盘点任务盘漏货品.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
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
	public ActionForward checkCurrentMonth(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			if(StockTakeDao.beforeInsertStockTake(term)){
				jobject.initTip(true, "操作成功, 会计月验证通过.");				
			}else{
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, WebParams.TIP_CODE_ERROE, "操作失败, 未知错误.");
			}
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
}
