package com.wireless.Actions.inventoryMgr.stockAction;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockTake;
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
	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String oriStockId = request.getParameter("oriStockId");
			String oriStockDate = request.getParameter("oriStockDate");
			String comment = request.getParameter("comment");
			String typeValue = request.getParameter("type");
			String subTypeValue = request.getParameter("subType");
			String cateValue = request.getParameter("cate");
			String actualPrice = request.getParameter("actualPrice");
			String detail = request.getParameter("detail");
			
			String deptIn = request.getParameter("deptIn");
			String deptOut = request.getParameter("deptOut");
			String supplier = request.getParameter("supplier");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MaterialCate.Type cate = MaterialCate.Type.valueOf(Integer.valueOf(cateValue));
			StockAction.SubType subType = StockAction.SubType.valueOf(Integer.valueOf(subTypeValue));
			StockAction.Type type = StockAction.Type.valueOf(Integer.valueOf(typeValue));
			
			InsertBuilder builder = null;
			if(type == StockAction.Type.STOCK_IN){
				if(subType == StockAction.SubType.STOCK_IN){
					// 采购  
					builder = StockAction.InsertBuilder.newStockIn(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.parseFloat(actualPrice))
							.setOriStockId(oriStockId)
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setCateType(cate)
							.setDeptIn(Short.valueOf(deptIn))
							.setSupplierId(Integer.valueOf(supplier));
				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
					// 入库调拨
					builder = StockAction.InsertBuilder.newStockInTransfer(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}else if(subType == StockAction.SubType.SPILL){
					// 报溢
					builder = StockAction.InsertBuilder.newSpill(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setCateType(cate);
				}
			}else if(type == StockAction.Type.STOCK_OUT){
				if(subType == StockAction.SubType.STOCK_OUT){
					// 退货
					builder = StockAction.InsertBuilder.newStockOut(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.parseFloat(actualPrice))
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setCateType(cate)
							.setDeptOut(Short.valueOf(deptOut))
							.setSupplierId(Integer.valueOf(supplier));
				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
					// 出库调拨
					builder = StockAction.InsertBuilder.newStockOutTransfer(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}else if(subType == StockAction.SubType.DAMAGE){
					// 报损
					builder = StockAction.InsertBuilder.newDamage(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
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
			int id = StockActionDao.insertStockAction(staff, builder);
			List<StockAction> root = new ArrayList<StockAction>();
			StockAction stockAction = new StockAction(builder);
			stockAction.setId(id);
			root.add(stockAction);
			jobject.setRoot(root);
			jobject.initTip(true, "操作成功, 已录入新库存单信息.");
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
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			String oriStockId = request.getParameter("oriStockId");
			String oriStockDate = request.getParameter("oriStockDate");
			String comment = request.getParameter("comment");
			String typeValue = request.getParameter("type");
			String subTypeValue = request.getParameter("subType");
			String cateValue = request.getParameter("cate");
			String actualPrice = request.getParameter("actualPrice");
			String detail = request.getParameter("detail");
			
			String deptIn = request.getParameter("deptIn");
			String deptOut = request.getParameter("deptOut");
			String supplier = request.getParameter("supplier");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MaterialCate.Type cate = MaterialCate.Type.valueOf(Integer.valueOf(cateValue));
			StockAction.SubType subType = StockAction.SubType.valueOf(Integer.valueOf(subTypeValue));
			StockAction.Type type = StockAction.Type.valueOf(Integer.valueOf(typeValue));
			
			InsertBuilder builder = null;
			if(type == StockAction.Type.STOCK_IN){
				if(subType == StockAction.SubType.STOCK_IN){
					// 采购  
					builder = StockAction.InsertBuilder.newStockIn(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.parseFloat(actualPrice))
							.setOriStockId(oriStockId)
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setCateType(cate)
							.setDeptIn(Short.valueOf(deptIn))
							.setSupplierId(Integer.valueOf(supplier));
				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
					// 入库调拨
					builder = StockAction.InsertBuilder.newStockInTransfer(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}else if(subType == StockAction.SubType.SPILL){
					// 报溢
					builder = StockAction.InsertBuilder.newSpill(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setCateType(cate);
				}
			}else if(type == StockAction.Type.STOCK_OUT){
				if(subType == StockAction.SubType.STOCK_OUT){
					// 退货
					builder = StockAction.InsertBuilder.newStockOut(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.parseFloat(actualPrice))
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setCateType(cate)
							.setDeptOut(Short.valueOf(deptOut))
							.setSupplierId(Integer.valueOf(supplier));
				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
					// 出库调拨
					builder = StockAction.InsertBuilder.newStockOutTransfer(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}else if(subType == StockAction.SubType.DAMAGE){
					// 报损
					builder = StockAction.InsertBuilder.newDamage(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
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
			StockActionDao.updateStockAction(staff, Integer.valueOf(id), builder);
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
	public ActionForward reaudit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			String oriStockId = request.getParameter("oriStockId");
			String oriStockDate = request.getParameter("oriStockDate");
			String comment = request.getParameter("comment");
			String typeValue = request.getParameter("type");
			String subTypeValue = request.getParameter("subType");
			String cateValue = request.getParameter("cate");
			String actualPrice = request.getParameter("actualPrice");
			String detail = request.getParameter("detail");
			
			String deptIn = request.getParameter("deptIn");
			String deptOut = request.getParameter("deptOut");
			String supplier = request.getParameter("supplier");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MaterialCate.Type cate = MaterialCate.Type.valueOf(Integer.valueOf(cateValue));
			StockAction.SubType subType = StockAction.SubType.valueOf(Integer.valueOf(subTypeValue));
			StockAction.Type type = StockAction.Type.valueOf(Integer.valueOf(typeValue));
			
			InsertBuilder builder = null;
			if(type == StockAction.Type.STOCK_IN){
				if(subType == StockAction.SubType.STOCK_IN){
					// 采购  
					builder = StockAction.InsertBuilder.newStockIn(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.parseFloat(actualPrice))
							.setOriStockId(oriStockId)
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setCateType(cate)
							.setDeptIn(Short.valueOf(deptIn))
							.setSupplierId(Integer.valueOf(supplier));
				}else if(subType == StockAction.SubType.STOCK_IN_TRANSFER){
					// 入库调拨
					builder = StockAction.InsertBuilder.newStockInTransfer(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}else if(subType == StockAction.SubType.SPILL){
					// 报溢
					builder = StockAction.InsertBuilder.newSpill(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setCateType(cate);
				}
			}else if(type == StockAction.Type.STOCK_OUT){
				if(subType == StockAction.SubType.STOCK_OUT){
					// 退货
					builder = StockAction.InsertBuilder.newStockOut(staff.getRestaurantId(), Long.valueOf(oriStockDate), Float.parseFloat(actualPrice))
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setCateType(cate)
							.setDeptOut(Short.valueOf(deptOut))
							.setSupplierId(Integer.valueOf(supplier));
				}else if(subType == StockAction.SubType.STOCK_OUT_TRANSFER){
					// 出库调拨
					builder = StockAction.InsertBuilder.newStockOutTransfer(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
							.setComment(comment)
							.setDeptIn(Short.valueOf(deptIn))
							.setDeptOut(Short.valueOf(deptOut))
							.setCateType(cate);
				}else if(subType == StockAction.SubType.DAMAGE){
					// 报损
					builder = StockAction.InsertBuilder.newDamage(staff.getRestaurantId())
							.setOriStockId(oriStockId)
							.setOriStockDate(Long.valueOf(oriStockDate))
							.setOperatorId(staff.getId()).setOperator(staff.getName())
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
			StockActionDao.reAuditStockAction(staff, Integer.valueOf(id), builder);
			jobject.initTip(true, "操作成功, 已反审核库存单信息.");
		}catch(BusinessException e){
			jobject.initTip(e);
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
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			StockActionDao.deleteStockActionById(staff, Integer.valueOf(id));
			jobject.initTip(true, "操作成功, 已删除库存单信息.");
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
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			AuditBuilder builder = StockAction.AuditBuilder.newStockActionAudit(Integer.valueOf(id))
					.setApprover(staff.getName())
					.setApproverId((int) staff.getId());
			StockActionDao.auditStockAction(staff, builder);
			jobject.initTip(true, "操作成功, 已审核库存单信息.");
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
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward checkStockTake(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(StockActionDao.checkStockTake(staff)){
				jobject.initTip(true, "操作成功, 继续添加信息.");
			}
		}catch(BusinessException e){
			jobject.initTip(false, JObject.TIP_TITLE_DEFAULT, e.getErrCode().getCode(), e.getDesc());
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
	 * 检查是否可以反审核
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward checkReAudit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			String pin = (String)request.getAttribute("pin");
			String restaurantID = (String)request.getAttribute("restaurantID");
			String stockActionId = request.getParameter("stockActionId");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			StockAction sa = StockActionDao.getStockAndDetailById(staff, Integer.parseInt(stockActionId));
			
			
			dbCon.connect();
			String selectMaxDate = "SELECT MAX(date) as date FROM (SELECT  MAX(date_add(month, interval 1 MONTH)) date FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + restaurantID + 
					" UNION ALL " +
					" SELECT finish_date AS date FROM " + Params.dbName + ".stock_take WHERE restaurant_id = " + restaurantID + " AND (status = " + StockTake.Status.AUDIT.getVal() + " OR status = " + StockTake.Status.CHECKING.getVal() + ")) M";
			dbCon.rs = dbCon.stmt.executeQuery(selectMaxDate);
			final Timestamp minDay;
			if(dbCon.rs.next()){
				if(dbCon.rs.getTimestamp("date") != null){
					minDay = dbCon.rs.getTimestamp("date");
				}else{
					minDay = null;
				}
			}else{
				minDay = null;
			}
			if(minDay != null){
				if(minDay.getTime() < sa.getApproverDate()){
					jobject.initTip(true, "");
				}else{
					jobject.initTip(false, "");
				}
			}else{
				jobject.initTip(true, "");
			}
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
