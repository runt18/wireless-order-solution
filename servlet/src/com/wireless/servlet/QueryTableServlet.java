package com.wireless.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wireless.order.QueryTable;
import com.wireless.protocol.Table;


/**
 * Servlet implementation class QueryTableServlet
 */
@WebServlet("/QueryTableServlet")
public class QueryTableServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QueryTableServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String pin = request.getParameter("pin");
		
		String jsonResp = "{success:$(result), data:'$(value)'}";		
		try{

			Table[] tables = QueryTable.exec(pin);
			jsonResp = jsonResp.replace("$(result)", "true");
			//format the table results into response string in the form of JSON
			if(tables.length == 0){
				jsonResp = jsonResp.replace("$(value)", "");
			}else{
				
				StringBuffer value = new StringBuffer(); 
				for(int i = 0; i < tables.length; i++){
					String jsonTable = "[ \"$(alias_id)\", \"$(custom_num)\", \"$(status)\" ]";
					jsonTable = jsonTable.replace("$(alias_id)", new Short(tables[i].alias_id).toString());
					jsonTable = jsonTable.replace("$(custom_num)", new Short(tables[i].custom_num).toString());
					if(tables[i].status == Table.TABLE_BUSY){
						jsonTable = jsonTable.replace("$(status)", "占用");
					}else{
						jsonTable = jsonTable.replace("$(status)", "空桌");
					}
					//pub each json table info to the value
					value.append(jsonTable);
					//the string is separated by comma
					if(i != tables.length - 1){
						value.append("，");
					}
				}
				
				jsonResp = jsonResp.replace("$(value)", value);
			}
			
		}catch(Exception e){
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", e.getMessage());
		}
		
		response.setContentType("text/html;charset=GB2312");
		response.getWriter().print(jsonResp);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
