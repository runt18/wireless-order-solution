package com.wireless.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wireless.order.QueryOrder;
import com.wireless.protocol.Order;
import com.wireless.protocol.Util;

/**
 * Servlet implementation class QueryOrderServlet
 */
@WebServlet("/QueryOrderServlet")
public class QueryOrderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QueryOrderServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String pin = request.getParameter("pin");
		short tableID = Short.parseShort(request.getParameter("table"));
		
		String jsonResp = "{success:$(result), data:'$(value)'}";		
		try{
			
			Order order = QueryOrder.exec(pin, tableID);		
			jsonResp = jsonResp.replace("$(result)", "true");
			
			if(order.foods.length == 0){
				jsonResp = jsonResp.replace("$(value)", "");
			}else{
				StringBuffer value = new StringBuffer();
				for(int i = 0; i < order.foods.length; i++){
					String jsonOrderFood = "[\"$(name)\",\"$(taste)\",$(count),\"$(unit)\"]";
					jsonOrderFood = jsonOrderFood.replace("$(name)", order.foods[i].name);
					jsonOrderFood = jsonOrderFood.replace("$(taste)", order.foods[i].taste.preference);
					jsonOrderFood = jsonOrderFood.replace("$(count)", order.foods[i].count2String());
					jsonOrderFood = jsonOrderFood.replace("$(unit)", Util.price2String(order.foods[i].price2(), Util.INT_MASK_2));
					
					//pub each json order food info to the value
					value.append(jsonOrderFood);
					//the string is separated by comma
					if(i != order.foods.length - 1){
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
