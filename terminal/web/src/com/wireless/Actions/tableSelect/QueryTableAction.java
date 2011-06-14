package com.wireless.Actions.tableSelect;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.QueryTable;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class QueryTableAction extends Action {

	private static final long serialVersionUID = 1L;

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		PrintWriter out = null;

		String jsonResp = "{success:$(result), data:'$(value)'}";
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			
			Table[] tables = QueryTable.exec(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF);
			jsonResp = jsonResp.replace("$(result)", "true");
			// format the table results into response string in the form of JSON
			if (tables.length == 0) {
				jsonResp = jsonResp.replace("$(value)", "");
			} else {

				StringBuffer value = new StringBuffer();
				for (int i = 0; i < tables.length; i++) {
					/**
					 * The json format of table query looks like below.
					 * ["餐台1编号"，"餐台1名称"，"餐台1人数"，"占用"，"一般"]，["餐台2编号"，"餐台2名称"，"餐台2人数"，"空桌"，"外卖"]
					 */
					String jsonTable = "[\"$(alias_id)\",\"$(alias_name)\",\"$(custom_num)\",\"$(status)\",\"$(category)\"]";
					jsonTable = jsonTable.replace("$(alias_id)", new Short(tables[i].alias_id).toString());
					jsonTable = jsonTable.replace("$(custom_num)", new Short(tables[i].custom_num).toString());
					
					if(tables[i].name != null){
						jsonTable = jsonTable.replace("$(alias_name)", tables[i].name);						
					}else{
						jsonTable = jsonTable.replace("$(alias_name)", "");		
					}
					
					if(tables[i].status == Table.TABLE_BUSY) {
						jsonTable = jsonTable.replace("$(status)", "占用");
					}else{
						jsonTable = jsonTable.replace("$(status)", "空桌");
					}
					
					if(tables[i].category == Order.CATE_NORMAL){
						jsonTable = jsonTable.replace("$(category)", "一般");
						
					}else if(tables[i].category == Order.CATE_TAKE_OUT){
						jsonTable = jsonTable.replace("$(category)", "外卖");
						
					}else if(tables[i].category == Order.CATE_JOIN_TABLE){
						jsonTable = jsonTable.replace("$(category)", "并台");
						
					}else if(tables[i].category == Order.CATE_MERGER_TABLE){
						jsonTable = jsonTable.replace("$(category)", "拼台");
						
					}else{
						jsonTable = jsonTable.replace("$(category)", "一般");
					}
					
					// put each json table info to the value
					value.append(jsonTable);
					// the string is separated by comma
					if (i != tables.length - 1) {
						value.append("，");
					}
				}

				jsonResp = jsonResp.replace("$(value)", value);
			}

		} catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			if(e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED){
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");				
			}else{
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅的餐台信息，请重新确认");
			}
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}catch(IOException e){
			e.printStackTrace();
			
		}finally{
			//Just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}
		return null;
	}

}
