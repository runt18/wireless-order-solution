package com.wireless.Actions.tableSelect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.order.QueryTable;
import com.wireless.protocol.Table;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class QueryTableAction extends Action {

	private static final long serialVersionUID = 1L;

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// 解决后台中文传到前台乱码
		response.setContentType("text/json; charset=utf-8");

		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String pin = request.getParameter("pin");

		String jsonResp = "{success:$(result), data:'$(value)'}";
		try {

			Table[] tables = QueryTable.exec(pin);
			jsonResp = jsonResp.replace("$(result)", "true");
			// format the table results into response string in the form of JSON
			if (tables.length == 0) {
				jsonResp = jsonResp.replace("$(value)", "");
			} else {

				StringBuffer value = new StringBuffer();
				for (int i = 0; i < tables.length; i++) {
					String jsonTable = "[\"$(alias_id)\",\"$(custom_num)\",\"$(status)\"]";
					jsonTable = jsonTable.replace("$(alias_id)", new Short(
							tables[i].alias_id).toString());
					jsonTable = jsonTable.replace("$(custom_num)", new Short(
							tables[i].custom_num).toString());
					if (tables[i].status == Table.TABLE_BUSY) {
						jsonTable = jsonTable.replace("$(status)", "占用");
					} else {
						jsonTable = jsonTable.replace("$(status)", "空桌");
					}
					// pub each json table info to the value
					value.append(jsonTable);
					// the string is separated by comma
					if (i != tables.length - 1) {
						value.append("，");
					}
				}

				jsonResp = jsonResp.replace("$(value)", value);
			}

		} catch (Exception e) {
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", e.getMessage());
		}

		out.write(jsonResp);
		return null;
	}

}
