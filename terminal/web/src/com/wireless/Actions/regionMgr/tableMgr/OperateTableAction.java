package com.wireless.Actions.regionMgr.tableMgr;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.json.JObject;
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
import com.wireless.pojo.util.WebParams;
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
	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String name = request.getParameter("name");
			String alias = request.getParameter("alias");
			String minimumCost = request.getParameter("minimumCost");
			String regionId = request.getParameter("regionId");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Table.InsertBuilder builder = new Table.InsertBuilder(Integer.parseInt(alias), Region.RegionId.valueOf(Short.parseShort(regionId)))
					.setMiniCost(Integer.valueOf(minimumCost))
					.setTableName(name);
			TableDao.insert(staff, builder);
			jobject.initTip(true, "操作成功, 已添加新餐台信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
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
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			String name = request.getParameter("name");
			String regionId = request.getParameter("regionId"); 
			String minimumCost = request.getParameter("minimumCost");
			
			Table.UpdateBuilder builder = new Table.UpdateBuilder(Integer.valueOf(id)).setMiniCost(Integer.valueOf(minimumCost))
												   .setRegionId(Region.RegionId.valueOf(Short.valueOf(regionId)))
												   .setTableName(name);
			TableDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
			jobject.initTip(true, "操作成功, 已修改餐台信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
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
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			TableDao.deleteById(StaffDao.verify(Integer.parseInt(pin)), Integer.valueOf(id));
			jobject.initTip(true, "操作成功, 已删除餐台信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
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
	public ActionForward batch(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String beginAlias = request.getParameter("beginAlias");
			String endAlias = request.getParameter("endAlias");
			String skips = request.getParameter("skips");
			String regionId = request.getParameter("regionId");
			
			Table.BatchInsertBuilder insertBuilder = new Table.BatchInsertBuilder(Integer.parseInt(beginAlias), Integer.parseInt(endAlias), Region.RegionId.valueOf(Integer.parseInt(regionId)));
			if(skips != null && !skips.isEmpty()){
				String skipNum[] = skips.split(","); 
				for (String num : skipNum) {
					if(num.equals("4")){
						insertBuilder.setSkip4(true);
					}else if(num.equals("7")){
						insertBuilder.setSkip7(true);
					}
				}
			}
			
			TableDao.insert(StaffDao.verify(Integer.parseInt(pin)), insertBuilder);
			
			jobject.initTip(true, "操作成功, 已批量添加餐台信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward transTable(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			
			int srcTblAlias = Integer.parseInt(request.getParameter("oldTableAlias"));
			int destTblAlias = Integer.parseInt(request.getParameter("newTableAlias"));

			// print the transfer table receipt
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTransTbl(staff, new Table.TransferBuilder(new Table.AliasBuilder(srcTblAlias), new Table.AliasBuilder(destTblAlias))));
			if(resp.header.type == Type.ACK){
				jobject.initTip(true, "操作成功, 原 " + srcTblAlias + " 号台转至新 " + destTblAlias + " 号台成功.");
			}else{
				jobject.initTip(false, new Parcel(resp.body).readParcel(ErrorCode.CREATOR).getDesc());
			}
			
		} finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward apartTable(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		String tableID = request.getParameter("tableID");
		String suffix = request.getParameter("suffix");
		try {
			List<Table> list = new ArrayList<>();
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, Order.InsertBuilder.newInstance4Join(new Table.Builder(Integer.parseInt(tableID)), Table.InsertBuilder4Join.Suffix.valueOf(suffix, 0)), PrintOption.DO_NOT_PRINT));
			if(resp.header.type == Type.ACK){
				Table joinedTbl = new Parcel(resp.body).readParcel(Table.CREATOR);
				list.add(TableDao.getById(staff, joinedTbl.getId()));
				jobject.setRoot(list);
				jobject.initTip(true, ("下单成功."));
			}else if(resp.header.type == Type.NAK){
				throw new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
				
			}
		} catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);

		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}		
	
}
