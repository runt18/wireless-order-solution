package com.wireless.Actions.tableSelect;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryTable;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqPrintOrder2;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.sccon.ServerConnector;

public class TransTableAction extends Action implements PinGen {

	private long _pin = 0;

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		PrintWriter out = null;

		String jsonResp = "{success:$(result), data:'$(value)'}";
		DBCon dbCon = new DBCon();
		String oldTableID = "", newTableID = "";
		Table oldTable = null;
		Table newTable = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. e.g. pin=0x1 & newTableID=201 &
			 * oldTableID=101" pin : the pin the this terminal newTableID : the
			 * table id to transfer oldTableID : the table id to be transferred
			 */
			String pin = request.getParameter("pin");
			
			_pin = Long.parseLong(pin);

			oldTableID = request.getParameter("oldTableID");
			newTableID = request.getParameter("newTableID");

			dbCon.connect();
			oldTable = QueryTable.exec(dbCon, _pin, Terminal.MODEL_STAFF,
					Integer.parseInt(oldTableID));

			newTable = QueryTable.exec(dbCon, _pin, Terminal.MODEL_STAFF,
					Integer.parseInt(newTableID));

			/**
			 * Need to assure two conditions before table transfer 1 - the old
			 * table remains in busy 2 - the new table is idle now
			 */
			if (oldTable.status == Table.TABLE_IDLE) {
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", oldTable.alias_id
						+ "号台是空闲状态，可能已经结帐，请跟餐厅经理确认");

			} else if (newTable.status == Table.TABLE_BUSY) {
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", newTable.alias_id
						+ "号台是就餐状态，请跟餐厅经理确认");

			} else {

				int orderID = com.wireless.db.Util.getUnPaidOrderID(dbCon, oldTable);

				// update the order
				String sql = "UPDATE "
						+ Params.dbName
						+ ".order SET "
						+ "table_id="
						+ newTable.alias_id
						+ ((newTable.name == null) ? " " : ", "
								+ "table_name='" + newTable.name + "'")
						+ " WHERE id=" + orderID;
				dbCon.stmt.execute(sql);

				// update the new table status to busy
				sql = "SELECT category, custom_num FROM " + Params.dbName + 
				  	  ".table WHERE restaurant_id=" + oldTable.restaurantID +
				  	  " AND alias_id=" + oldTable.alias_id;
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					short category = dbCon.rs.getShort("category");
					short customNum = dbCon.rs.getShort("custom_num");
					sql = "UPDATE " + Params.dbName + ".table SET " +
						  "status=" + Table.TABLE_BUSY + ", " +
						  "category=" + category + ", " +
						  "custom_num=" + customNum + 
						  " WHERE restaurant_id=" + newTable.restaurantID + 
						  " AND alias_id=" + newTable.alias_id;
					dbCon.stmt.execute(sql);
				}
				dbCon.rs.close();
			

				// update the original table status to idle
				sql = "UPDATE " + Params.dbName + ".table SET status="
						+ Table.TABLE_IDLE + "," + "custom_num=NULL,"
						+ "category=NULL" + " WHERE restaurant_id="
						+ oldTable.restaurantID + " AND alias_id="
						+ oldTable.alias_id;
				dbCon.stmt.execute(sql);
				
				jsonResp = jsonResp.replace("$(result)", "true");
				jsonResp = jsonResp.replace("$(value)", oldTable.alias_id
						+ "号台转至" + newTable.alias_id + "号台成功");

				// print the transfer table receipt
				ReqPackage.setGen(this);
				ServerConnector.instance().ask(
						new ReqPrintOrder2(Reserved.PRINT_TRANSFER_TABLE_2,
								orderID, oldTable.alias_id, newTable.alias_id));

			}

		} catch (BusinessException e) {
			jsonResp = jsonResp.replace("$(result)", "false");
			if (oldTable == null) {
				jsonResp = jsonResp.replace("$(value)", oldTableID + "号台信息不存在");
			} else if (newTable == null) {
				jsonResp = jsonResp.replace("$(value)", newTableID + "号台信息不存在");
			} else {
				jsonResp = jsonResp.replace("$(value)", oldTable.alias_id
						+ "号台转至" + newTable.alias_id + "号台不成功");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");

		} finally {

			dbCon.disconnect();
			// Just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}
		return null;
	}

	@Override
	public long getDeviceId() {
		return _pin;
	}

	@Override
	public short getDeviceType() {
		return Terminal.MODEL_STAFF;
	}
}
