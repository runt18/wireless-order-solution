package com.wireless.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wireless.order.QueryMenu;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Util;

/**
 * Servlet implementation class QueryMenuServlet
 */
@WebServlet("/QueryMenuServlet")
public class QueryMenuServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private final int QUERY_FOOD = 1;
	private final int QUERY_TASTE = 2;
	private final int QUERY_KITCHEN = 3;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QueryMenuServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String pin = request.getParameter("pin");
		int type = Integer.parseInt(request.getParameter("type"));
		
		String jsonResp = "{success:$(result), data:'$(value)'}";		
		try{
			FoodMenu foodMenu = QueryMenu.exec(pin);
			
			jsonResp = jsonResp.replace("$(result)", "true");
			StringBuffer value = new StringBuffer();
			if(type == QUERY_FOOD){
				for(int i = 0; i < foodMenu.foods.length; i++){
					String jsonFood = "[ $(alias_id), \"$(name)\", \"$(unit)\" ]";
					jsonFood = jsonFood.replace("$(alias_id)", new Integer(foodMenu.foods[i].alias_id).toString());
					jsonFood = jsonFood.replace("$(name)", foodMenu.foods[i].name);
					jsonFood = jsonFood.replace("$(unit)", Util.price2String(foodMenu.foods[i].price, Util.INT_MASK_2));
					//pub each json food info to the value
					value.append(jsonFood);
					//the string is separated by comma
					if(i != foodMenu.foods.length - 1){
						value.append("，");
					}
				}				
				
			}else if(type == QUERY_TASTE){
				for(int i = 0; i < foodMenu.tastes.length; i++){
					String jsonTaste = "[ $(alias_id), \"$(name)\", \"$(unit)\" ]";
					jsonTaste = jsonTaste.replace("$(alias_id)", new Integer(foodMenu.tastes[i].alias_id).toString());
					jsonTaste = jsonTaste.replace("$(name)", foodMenu.tastes[i].preference);
					jsonTaste = jsonTaste.replace("$(unit)", Util.price2String(foodMenu.tastes[i].price, Util.INT_MASK_2));
					//pub each json taste info to the value
					value.append(jsonTaste);
					//the string is separated by comma
					if(i != foodMenu.tastes.length - 1){
						value.append("，");
					}
				}
				
				
			}else if(type == QUERY_KITCHEN){
				
			}
			
			jsonResp = jsonResp.replace("$(value)", value);
			
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
