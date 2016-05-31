package com.wireless.Actions.inventoryMgr.stockAction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.AuditBuilder;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockActionDetail;

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
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String oriStockId = request.getParameter("oriStockId");
		final String oriStockDate = request.getParameter("oriStockDate");
		final String comment = request.getParameter("comment");
		final String typeValue = request.getParameter("type");
		final String subTypeValue = request.getParameter("subType");
		final String cateValue = request.getParameter("cate");
		final String actualPriceVal = request.getParameter("actualPrice");
		final String detail = request.getParameter("detail");
		
		final String deptIn = request.getParameter("deptIn");
		final String deptOut = request.getParameter("deptOut");
		final String supplier = request.getParameter("supplier");
		
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final MaterialCate.Type cate = MaterialCate.Type.valueOf(Integer.valueOf(cateValue));
		final StockAction.SubType subType = StockAction.SubType.valueOf(Integer.valueOf(subTypeValue));
		final StockAction.Type type = StockAction.Type.valueOf(Integer.valueOf(typeValue));
		
		try{
			final float actualPrice;
			if(actualPriceVal != null && !actualPriceVal.isEmpty()){
				actualPrice = Float.parseFloat(actualPriceVal);
			}else{
				actualPrice = 0;
			}
			InsertBuilder builder = null;
			
			if(type == StockAction.Type.STOCK_IN){
				if(subType == StockAction.SubType.STOCK_IN){
					builder = StockAction.InsertBuilder.newStockIn(staff.getRestaurantId(), Long.valueOf(oriStockDate), actualPrice);
				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
					builder = StockAction.InsertBuilder.newStockInTransfer(staff.getRestaurantId());
				}else if(subType == StockAction.SubType.SPILL){
					builder = StockAction.InsertBuilder.newSpill(staff.getRestaurantId());
				}
			}else if(type == StockAction.Type.STOCK_OUT){
				if(subType == StockAction.SubType.STOCK_OUT){
					builder = StockAction.InsertBuilder.newStockOut(staff.getRestaurantId(), Long.valueOf(oriStockDate), actualPrice);
				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
					builder = StockAction.InsertBuilder.newStockOutTransfer(staff.getRestaurantId());
				}else if(subType == StockAction.SubType.DAMAGE){
					builder = StockAction.InsertBuilder.newDamage(staff.getRestaurantId());
				}
			}
			
			builder.setOperatorId(staff.getId()).setOperator(staff.getName());
			
			if(oriStockId != null && !oriStockId.isEmpty()){
				builder.setOriStockId(oriStockId);
			}

			if(oriStockDate != null && !oriStockDate.isEmpty()){
				builder.setOriStockDate(Long.valueOf(oriStockDate));
			}
			
			if(comment != null && !comment.isEmpty()){
				builder.setComment(comment);
			}
			
			if(deptIn != null && !deptIn.isEmpty()){
				builder.setDeptIn(Short.valueOf(deptIn));
			}
			
			if(deptOut != null && !deptOut.isEmpty()){
				builder.setDeptOut(Short.valueOf(deptOut));
			}
			
			if(supplier != null && !supplier.isEmpty()){
				builder.setSupplierId(Integer.valueOf(supplier));
			}
			
			if(cateValue != null && !cateValue.isEmpty()){
				builder.setCateType(cate);
			}
			
//			if(type == StockAction.Type.STOCK_IN){
//				if(subType == StockAction.SubType.STOCK_IN){
//					// 采购  
//					builder = StockAction.InsertBuilder.newStockIn(staff.getRestaurantId(), Long.valueOf(oriStockDate), actualPrice)
//							.setOriStockId(oriStockId)
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setCateType(cate)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setSupplierId(Integer.valueOf(supplier));
//				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
//					// 入库调拨
//					builder = StockAction.InsertBuilder.newStockInTransfer(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setDeptOut(Short.valueOf(deptOut))
//							.setCateType(cate);
//				}else if(subType == StockAction.SubType.SPILL){
//					// 报溢
//					builder = StockAction.InsertBuilder.newSpill(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setCateType(cate);
//				}
//			}else if(type == StockAction.Type.STOCK_OUT){
//				if(subType == StockAction.SubType.STOCK_OUT){
//					// 退货
//					builder = StockAction.InsertBuilder.newStockOut(staff.getRestaurantId(), Long.valueOf(oriStockDate), actualPrice)
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setCateType(cate)
//							.setDeptOut(Short.valueOf(deptOut))
//							.setSupplierId(Integer.valueOf(supplier));
//				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
//					// 出库调拨
//					builder = StockAction.InsertBuilder.newStockOutTransfer(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setDeptOut(Short.valueOf(deptOut))
//							.setCateType(cate);
//				}else if(subType == StockAction.SubType.DAMAGE){
//					// 报损
//					builder = StockAction.InsertBuilder.newDamage(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptOut(Short.valueOf(deptOut))
//							.setCateType(cate);
//				}
//			}
			
			if(detail != null && !detail.isEmpty()){
				String[] content = detail.split("<sp>");
				for(String temp : content){
					String[] item = temp.split("<spst>");
					builder.addDetail(new StockActionDetail(Integer.valueOf(item[0]), Float.valueOf(item[1]), Float.valueOf(item[2])));
				}
			}
			int id = StockActionDao.insert(staff, builder);
			List<StockAction> root = new ArrayList<StockAction>();
			StockAction stockAction = new StockAction(builder);
			stockAction.setId(id);
			root.add(stockAction);
			jObject.setRoot(root);
			jObject.initTip(true, "操作成功, 已录入新库存单信息.");
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
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
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		final JObject jobject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String oriStockId = request.getParameter("oriStockId");
		final String oriStockDate = request.getParameter("oriStockDate");
		final String comment = request.getParameter("comment");
		final String typeValue = request.getParameter("type");
		final String subTypeValue = request.getParameter("subType");
		final String cateValue = request.getParameter("cate");
		final String actualPrice = request.getParameter("actualPrice");
		final String detail = request.getParameter("detail");
		
		final String deptIn = request.getParameter("deptIn");
		final String deptOut = request.getParameter("deptOut");
		final String supplier = request.getParameter("supplier");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		try{
			
			MaterialCate.Type cate = MaterialCate.Type.valueOf(Integer.valueOf(cateValue));
			StockAction.SubType subType = StockAction.SubType.valueOf(Integer.valueOf(subTypeValue));
			StockAction.Type type = StockAction.Type.valueOf(Integer.valueOf(typeValue));
			
			InsertBuilder builder = null;
			
			
			if(type == StockAction.Type.STOCK_IN){
				if(subType == StockAction.SubType.STOCK_IN){
					builder = StockAction.InsertBuilder.newStockIn(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.valueOf(actualPrice));
				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
					builder = StockAction.InsertBuilder.newStockInTransfer(staff.getRestaurantId());
				}else if(subType == StockAction.SubType.SPILL){
					builder = StockAction.InsertBuilder.newSpill(staff.getRestaurantId());
				}
			}else if(type == StockAction.Type.STOCK_OUT){
				if(subType == StockAction.SubType.STOCK_OUT){
					builder = StockAction.InsertBuilder.newStockOut(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.valueOf(actualPrice));
				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
					builder = StockAction.InsertBuilder.newStockOutTransfer(staff.getRestaurantId());
				}else if(subType == StockAction.SubType.DAMAGE){
					builder = StockAction.InsertBuilder.newDamage(staff.getRestaurantId());
				}
			}
			
			builder.setOperatorId(staff.getId()).setOperator(staff.getName());
			
			if(oriStockId != null && !oriStockId.isEmpty()){
				builder.setOriStockId(oriStockId);
			}

			if(oriStockDate != null && !oriStockDate.isEmpty()){
				builder.setOriStockDate(Long.valueOf(oriStockDate));
			}
			
			if(comment != null && !comment.isEmpty()){
				builder.setComment(comment);
			}
			
			if(deptIn != null && !deptIn.isEmpty()){
				builder.setDeptIn(Short.valueOf(deptIn));
			}
			
			if(deptOut != null && !deptOut.isEmpty()){
				builder.setDeptOut(Short.valueOf(deptOut));
			}
			
			if(supplier != null && !supplier.isEmpty()){
				builder.setSupplierId(Integer.valueOf(supplier));
			}
			
			if(cateValue != null && !cateValue.isEmpty()){
				builder.setCateType(cate);
			}
//			if(type == StockAction.Type.STOCK_IN){
//				if(subType == StockAction.SubType.STOCK_IN){
//					// 采购  
//					builder = StockAction.InsertBuilder.newStockIn(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.parseFloat(actualPrice))
//							.setOriStockId(oriStockId)
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setCateType(cate)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setSupplierId(Integer.valueOf(supplier));
//				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
//					// 入库调拨
//					builder = StockAction.InsertBuilder.newStockInTransfer(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setDeptOut(Short.valueOf(deptOut))
//							.setCateType(cate);
//				}else if(subType == StockAction.SubType.SPILL){
//					// 报溢
//					builder = StockAction.InsertBuilder.newSpill(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setCateType(cate);
//				}
//			}else if(type == StockAction.Type.STOCK_OUT){
//				if(subType == StockAction.SubType.STOCK_OUT){
//					// 退货
//					builder = StockAction.InsertBuilder.newStockOut(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.parseFloat(actualPrice))
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setCateType(cate)
//							.setDeptOut(Short.valueOf(deptOut))
//							.setSupplierId(Integer.valueOf(supplier));
//				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
//					// 出库调拨
//					builder = StockAction.InsertBuilder.newStockOutTransfer(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setDeptOut(Short.valueOf(deptOut))
//							.setCateType(cate);
//				}else if(subType == StockAction.SubType.DAMAGE){
//					// 报损
//					builder = StockAction.InsertBuilder.newDamage(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setCateType(cate);
//				}
//			}
			if(detail != null && !detail.isEmpty()){
				String[] content = detail.split("<sp>");
				for(String temp : content){
					String[] item = temp.split("<spst>");
					builder.addDetail(new StockActionDetail(Integer.valueOf(item[0]), Float.valueOf(item[1]), Float.valueOf(item[2])));
				}
			}
			StockActionDao.update(staff, Integer.valueOf(id), builder);
			jobject.initTip(true, "操作成功, 已修改库存单信息.");
		}catch(BusinessException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
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
	public ActionForward reaudit(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String oriStockId = request.getParameter("oriStockId");
		final String oriStockDate = request.getParameter("oriStockDate");
		final String comment = request.getParameter("comment");
		final String typeValue = request.getParameter("type");
		final String subTypeValue = request.getParameter("subType");
		final String cateValue = request.getParameter("cate");
		final String actualPrice = request.getParameter("actualPrice");
		final String detail = request.getParameter("detail");
		
		final String deptIn = request.getParameter("deptIn");
		final String deptOut = request.getParameter("deptOut");
		final String supplier = request.getParameter("supplier");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		try{
			MaterialCate.Type cate = MaterialCate.Type.valueOf(Integer.valueOf(cateValue));
			StockAction.SubType subType = StockAction.SubType.valueOf(Integer.valueOf(subTypeValue));
			StockAction.Type type = StockAction.Type.valueOf(Integer.valueOf(typeValue));
			
			InsertBuilder builder = null;
			
			if(type == StockAction.Type.STOCK_IN){
				if(subType == StockAction.SubType.STOCK_IN){
					builder = StockAction.InsertBuilder.newStockIn(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.valueOf(actualPrice));
				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
					builder = StockAction.InsertBuilder.newStockInTransfer(staff.getRestaurantId());
				}else if(subType == StockAction.SubType.SPILL){
					builder = StockAction.InsertBuilder.newSpill(staff.getRestaurantId());
				}
			}else if(type == StockAction.Type.STOCK_OUT){
				if(subType == StockAction.SubType.STOCK_OUT){
					builder = StockAction.InsertBuilder.newStockOut(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.valueOf(actualPrice));
				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
					builder = StockAction.InsertBuilder.newStockOutTransfer(staff.getRestaurantId());
				}else if(subType == StockAction.SubType.DAMAGE){
					builder = StockAction.InsertBuilder.newDamage(staff.getRestaurantId());
				}
			}
			
			builder.setOperatorId(staff.getId()).setOperator(staff.getName());
			
			if(oriStockId != null && !oriStockId.isEmpty()){
				builder.setOriStockId(oriStockId);
			}

			if(oriStockDate != null && !oriStockDate.isEmpty()){
				builder.setOriStockDate(Long.valueOf(oriStockDate));
			}
			
			if(comment != null && !comment.isEmpty()){
				builder.setComment(comment);
			}
			
			if(deptIn != null && !deptIn.isEmpty()){
				builder.setDeptIn(Short.valueOf(deptIn));
			}
			
			if(deptOut != null && !deptOut.isEmpty()){
				builder.setDeptOut(Short.valueOf(deptOut));
			}
			
			if(supplier != null && !supplier.isEmpty()){
				builder.setSupplierId(Integer.valueOf(supplier));
			}
			
			if(cateValue != null && !cateValue.isEmpty()){
				builder.setCateType(cate);
			}
			
			
			
//			if(type == StockAction.Type.STOCK_IN){
//				if(subType == StockAction.SubType.STOCK_IN){
//					// 采购  
//					builder = StockAction.InsertBuilder.newStockIn(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.parseFloat(actualPrice))
//							.setOriStockId(oriStockId)
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setCateType(cate)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setSupplierId(Integer.valueOf(supplier));
//				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
//					// 入库调拨
//					builder = StockAction.InsertBuilder.newStockInTransfer(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setDeptOut(Short.valueOf(deptOut))
//							.setCateType(cate);
//				}else if(subType == StockAction.SubType.SPILL){
//					// 报溢
//					builder = StockAction.InsertBuilder.newSpill(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setCateType(cate);
//				}
//			}else if(type == StockAction.Type.STOCK_OUT){
//				if(subType == StockAction.SubType.STOCK_OUT){
//					// 退货
//					builder = StockAction.InsertBuilder.newStockOut(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.parseFloat(actualPrice))
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setCateType(cate)
//							.setDeptOut(Short.valueOf(deptOut))
//							.setSupplierId(Integer.valueOf(supplier));
//				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
//					// 出库调拨
//					builder = StockAction.InsertBuilder.newStockOutTransfer(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setDeptOut(Short.valueOf(deptOut))
//							.setCateType(cate);
//				}else if(subType == StockAction.SubType.DAMAGE){
//					// 报损
//					builder = StockAction.InsertBuilder.newDamage(staff.getRestaurantId())
//							.setOriStockId(oriStockId)
//							.setOriStockDate(Long.valueOf(oriStockDate))
//							.setOperatorId(staff.getId()).setOperator(staff.getName())
//							.setComment(comment)
//							.setDeptIn(Short.valueOf(deptIn))
//							.setCateType(cate);
//				}
//			}
			
			if(detail != null && !detail.isEmpty()){
				String[] content = detail.split("<sp>");
				for(String temp : content){
					String[] item = temp.split("<spst>");
					builder.addDetail(new StockActionDetail(Integer.valueOf(item[0]), Float.valueOf(item[1]), Float.valueOf(item[2])));
				}
			}
			
			StockActionDao.reAuditStockAction(staff, Integer.valueOf(id), builder);
			jObject.initTip(true, "操作成功, 已反审核库存单信息.");
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
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
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		try{
			StockActionDao.deleteStockActionById(staff, Integer.valueOf(id));
			jObject.initTip(true, "操作成功, 已删除库存单信息.");
		}catch(BusinessException e){
			jObject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
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
	public ActionForward audit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		try{
			AuditBuilder builder = StockAction.AuditBuilder.newStockActionAudit(Integer.valueOf(id))
					.setApprover(staff.getName())
					.setApproverId((int) staff.getId());
			StockActionDao.audit(staff, builder);
			jObject.initTip(true, "操作成功, 已审核库存单信息.");
		}catch(BusinessException e){
			jObject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}

	/**
	 * 判定是否盘点中
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward checkStockTake(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		try{
			
			if(StockActionDao.checkStockTake(staff)){
				jObject.initTip(true, "操作成功, 继续添加信息.");
			}
		}catch(BusinessException e){
			jObject.initTip(false, JObject.TIP_TITLE_DEFAULT, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 检查是否可以反审核
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward checkReAudit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String stockActionId = request.getParameter("stockActionId");
		final JObject jObject = new JObject();
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		try{

			StockAction sa = StockActionDao.getById(staff, Integer.parseInt(stockActionId), true);
			
			long dateTime = StockActionDao.getStockActionInsertTime(staff);
			
			if(dateTime == 0){//没有盘点或者日结
				jObject.initTip(true, "");
			}else{
				if(dateTime < sa.getApproverDate()){//审核时间在盘点或日结之后
					jObject.initTip(true, "");
				}else{
					jObject.initTip(false, "");
				}				
			}
			
		}catch(BusinessException e){
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
