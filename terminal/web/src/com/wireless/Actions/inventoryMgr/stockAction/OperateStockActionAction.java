package com.wireless.Actions.inventoryMgr.stockAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockAction.AuditBuilder;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.protocol.Terminal;
import com.wireless.util.WebParams;

public class OperateStockActionAction extends DispatchAction{
	/**
	 * 新增
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
			String oriStockId = request.getParameter("oriStockId");
			String oriStockDate = request.getParameter("oriStockDate");
			String comment = request.getParameter("comment");
			String typeValue = request.getParameter("type");
			String subTypeValue = request.getParameter("subType");
			String cateValue = request.getParameter("cate");
			String detail = request.getParameter("detail");
			
			String deptIn = request.getParameter("deptIn");
			String deptOut = request.getParameter("deptOut");
			String supplier = request.getParameter("supplier");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			StockAction.CateType cate = StockAction.CateType.valueOf(Integer.valueOf(cateValue));
			StockAction.SubType subType = StockAction.SubType.valueOf(Integer.valueOf(subTypeValue));
			StockAction.Type type = StockAction.Type.valueOf(Integer.valueOf(typeValue));
			
			InsertBuilder builder = null;
			if(type == StockAction.Type.STOCK_IN){
				if(subType == StockAction.SubType.STOCK_IN){
					// 采购  
					builder = StockAction.InsertBuilder.newStockIn(term.restaurantID, Long.valueOf(oriStockDate))
							.setOriStockId(oriStockId)
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setCateType(cate)
							.setDeptIn(Short.valueOf(deptIn))
							.setSupplierId(Integer.valueOf(supplier));
				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
					// 入库调拨
					builder = StockAction.InsertBuilder.newStockInTransfer(term.restaurantID)
							.setOriStockId(oriStockId)
							.setOriStockIdDate(Long.valueOf(oriStockDate))
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}else if(subType == StockAction.SubType.SPILL){
					// 报溢
					builder = StockAction.InsertBuilder.newSpill(term.restaurantID)
							.setOriStockId(oriStockId)
							.setOriStockIdDate(Long.valueOf(oriStockDate))
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setCateType(cate);
				}
			}else if(type == StockAction.Type.STOCK_OUT){
				if(subType == StockAction.SubType.STOCK_OUT){
					// 退货
					builder = StockAction.InsertBuilder.newStockOut(term.restaurantID, Long.valueOf(oriStockDate))
							.setOriStockId(oriStockId)
							.setOriStockIdDate(Long.valueOf(oriStockDate))
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setCateType(cate)
							.setDeptOut(Short.valueOf(deptOut))
							.setSupplierId(Integer.valueOf(supplier));
				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
					// 出库调拨
					builder = StockAction.InsertBuilder.newStockOutTransfer(term.restaurantID)
							.setOriStockId(oriStockId)
							.setOriStockIdDate(Long.valueOf(oriStockDate))
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}else if(subType == StockAction.SubType.DAMAGE){
					// 报损
					builder = StockAction.InsertBuilder.newDamage(term.restaurantID)
							.setOriStockId(oriStockId)
							.setOriStockIdDate(Long.valueOf(oriStockDate))
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}
			}
			
			String[] content = detail.split("<sp>");
			for(String temp : content){
				String[] item = temp.split("<spst>");
				builder.addDetail(new StockActionDetail(Integer.valueOf(item[0]), Float.valueOf(item[1]), Float.valueOf(item[2])));
			}
			StockActionDao.insertStockAction(term, builder);
			jobject.initTip(true, "操作成功, 已录入新库存单信息.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

	/**
	 * 修改
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
			String oriStockId = request.getParameter("oriStockId");
			String oriStockDate = request.getParameter("oriStockDate");
			String comment = request.getParameter("comment");
			String typeValue = request.getParameter("type");
			String subTypeValue = request.getParameter("subType");
			String cateValue = request.getParameter("cate");
			String detail = request.getParameter("detail");
			
			String deptIn = request.getParameter("deptIn");
			String deptOut = request.getParameter("deptOut");
			String supplier = request.getParameter("supplier");
			
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			StockAction.CateType cate = StockAction.CateType.valueOf(Integer.valueOf(cateValue));
			StockAction.SubType subType = StockAction.SubType.valueOf(Integer.valueOf(subTypeValue));
			StockAction.Type type = StockAction.Type.valueOf(Integer.valueOf(typeValue));
			
			InsertBuilder builder = null;
			if(type == StockAction.Type.STOCK_IN){
				if(subType == StockAction.SubType.STOCK_IN){
					// 采购  
					builder = StockAction.InsertBuilder.newStockIn(term.restaurantID, Long.valueOf(oriStockDate))
							.setOriStockId(oriStockId)
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setCateType(cate)
							.setDeptIn(Short.valueOf(deptIn))
							.setSupplierId(Integer.valueOf(supplier));
				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
					// 入库调拨
					builder = StockAction.InsertBuilder.newStockInTransfer(term.restaurantID)
							.setOriStockId(oriStockId)
							.setOriStockIdDate(Long.valueOf(oriStockDate))
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}else if(subType == StockAction.SubType.SPILL){
					// 报溢
					builder = StockAction.InsertBuilder.newSpill(term.restaurantID)
							.setOriStockId(oriStockId)
							.setOriStockIdDate(Long.valueOf(oriStockDate))
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setCateType(cate);
				}
			}else if(type == StockAction.Type.STOCK_OUT){
				if(subType == StockAction.SubType.STOCK_OUT){
					// 退货
					builder = StockAction.InsertBuilder.newStockOut(term.restaurantID, Long.valueOf(oriStockDate))
							.setOriStockId(oriStockId)
							.setOriStockIdDate(Long.valueOf(oriStockDate))
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setCateType(cate)
							.setDeptOut(Short.valueOf(deptOut))
							.setSupplierId(Integer.valueOf(supplier));
				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
					// 出库调拨
					builder = StockAction.InsertBuilder.newStockOutTransfer(term.restaurantID)
							.setOriStockId(oriStockId)
							.setOriStockIdDate(Long.valueOf(oriStockDate))
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}else if(subType == StockAction.SubType.DAMAGE){
					// 报损
					builder = StockAction.InsertBuilder.newDamage(term.restaurantID)
							.setOriStockId(oriStockId)
							.setOriStockIdDate(Long.valueOf(oriStockDate))
							.setOperatorId((int)term.pin).setOperator(term.owner)
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setCateType(cate);
				}
			}
			
			String[] content = detail.split("<sp>");
			for(String temp : content){
				String[] item = temp.split("<spst>");
				builder.addDetail(new StockActionDetail(Integer.valueOf(item[0]), Float.valueOf(item[1]), Float.valueOf(item[2])));
			}
			StockActionDao.updateStockAction(term, Integer.valueOf(id), builder);
			jobject.initTip(true, "操作成功, 已修改库存单信息.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
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
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			String id = request.getParameter("id");
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			StockActionDao.deleteStockActionById(term, Integer.valueOf(id));
			jobject.initTip(true, "操作成功, 已删除库存单信息.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
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
			AuditBuilder builder = StockAction.AuditBuilder.newStockActionAudit(Integer.valueOf(id))
					.setApprover(term.owner)
					.setApproverId((int) term.id);
			StockActionDao.auditStockAction(term, builder);
			jobject.initTip(true, "操作成功, 已审核库存单信息.");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
