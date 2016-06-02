package com.wireless.Actions.regionMgr.tableMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.pack.req.ReqTransTbl;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class OperateTableAction extends DispatchAction{

	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String name = request.getParameter("name");
		final String alias = request.getParameter("alias");
		final String minimumCost = request.getParameter("minimumCost");
		final String regionId = request.getParameter("regionId");		
		final JObject jObject = new JObject();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final Table.InsertBuilder builder = new Table.InsertBuilder(Integer.parseInt(alias), Region.RegionId.valueOf(Short.parseShort(regionId)))
														 .setMiniCost(Integer.valueOf(minimumCost))
														 .setTableName(name);
			TableDao.insert(staff, builder);
			jObject.initTip(true, "操作成功, 已添加新餐台信息.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
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
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String name = request.getParameter("name");
		final String regionId = request.getParameter("regionId"); 
		final String minimumCost = request.getParameter("minimumCost");		
		final JObject jObject = new JObject();
		try{

			final Table.UpdateBuilder builder = new Table.UpdateBuilder(Integer.valueOf(id));
			if(minimumCost != null && !minimumCost.isEmpty()){
				builder.setMiniCost(Integer.valueOf(minimumCost));
			}
			
			if(regionId != null && !regionId.isEmpty()){
				builder.setRegionId(Region.RegionId.valueOf(Short.valueOf(regionId)));
			}
												  
			if(name != null && !name.isEmpty()){
				builder.setTableName(name);
			}
												   
			TableDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
			jObject.initTip(true, "操作成功, 已修改餐台信息.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
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
	public ActionForward delete(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final JObject jObject = new JObject();
		try{

			TableDao.deleteById(StaffDao.verify(Integer.parseInt(pin)), Integer.valueOf(id));
			jObject.initTip(true, "操作成功, 已删除餐台信息.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 批量增加
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward batch(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String beginAlias = request.getParameter("beginAlias");
		final String endAlias = request.getParameter("endAlias");
		final String skips = request.getParameter("skips");
		final String regionId = request.getParameter("regionId");
		final JObject jObject = new JObject();
		try{

			final Table.BatchInsertBuilder batchBuilder = new Table.BatchInsertBuilder(Integer.parseInt(beginAlias), Integer.parseInt(endAlias), Region.RegionId.valueOf(Integer.parseInt(regionId)));
			if(skips != null && !skips.isEmpty()){
				String skipNum[] = skips.split(","); 
				for (String num : skipNum) {
					if(num.equals("4")){
						batchBuilder.setSkip4(true);
					}else if(num.equals("7")){
						batchBuilder.setSkip7(true);
					}
				}
			}
			
			TableDao.insert(StaffDao.verify(Integer.parseInt(pin)), batchBuilder);
			
			jObject.initTip(true, "操作成功, 已批量添加餐台信息.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 转台
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward transTable(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final int srcTblId = Integer.parseInt(request.getParameter("oldTableId"));
		final int destTblId = Integer.parseInt(request.getParameter("newTableId"));
		final String orientedPrinter = request.getParameter("orientedPrinter");		
		final JObject jObject = new JObject();
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			final Table.TransferBuilder builder = new Table.TransferBuilder(new Table.Builder(srcTblId), new Table.Builder(destTblId));
			//加载特定打印机
			if(orientedPrinter != null && !orientedPrinter.isEmpty()){
				for(String printerId : orientedPrinter.split(",")){
					builder.addPrinter(Integer.parseInt(printerId));
				}
			}

			// print the transfer table receipt
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTransTbl(staff, builder));
			if(resp.header.type == Type.ACK){
				jObject.initTip(true, "转台成功.");
			}else{
				jObject.initTip(false, new Parcel(resp.body).readParcel(ErrorCode.CREATOR).getDesc());
			}
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		} finally {
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 拆台
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward apartTable(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		String tableID = request.getParameter("tableID");
		String suffix = request.getParameter("suffix");
		String comment = request.getParameter("comment");
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, Order.InsertBuilder.newInstance4Join(new Table.Builder(Integer.parseInt(tableID)), Table.InsertBuilder4Join.Suffix.valueOf(suffix, 0)).setComment(comment), PrintOption.DO_NOT_PRINT));
			if(resp.header.type == Type.ACK){
				Table joinedTbl = new Parcel(resp.body).readParcel(Table.CREATOR);
				jobject.setRoot(TableDao.getById(staff, joinedTbl.getId()));
				jobject.initTip(true, ("下单成功."));
			}else if(resp.header.type == Type.NAK){
				throw new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
				
			}
		} catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);

		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}		
	
	/**
	 * 撤台
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward cancelTable(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			
			final int tableId = Integer.parseInt(request.getParameter("tableId"));

			// print the transfer table receipt
			OrderDao.cancel(staff, new Table.Builder(tableId));
			jObject.initTip(true, "撤台成功.");
			
		} catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);

		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 并台
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward mergeTable(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String tables = request.getParameter("tables");
		final JObject jObject = new JObject();
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			
			final String[] tablesArray = tables.split(",");
			
			final Order.MergeBuilder builder = new Order.MergeBuilder(Integer.parseInt(tablesArray[0]));
			
			if(tablesArray.length > 1){
				for (int i = 1; i < tablesArray.length; i++) {
					builder.add(Integer.parseInt(tablesArray[i]));
				}
			}
			
			final int mergeTable = OrderDao.merge(staff, builder);
			
			jObject.setExtra(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putInt("mergeTable", mergeTable);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					
				}
			});
			
			jObject.initTip(true, "并台成功.");
			
		} catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);

		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jObject.toString());
		}
		return null;
	}	
	
}
