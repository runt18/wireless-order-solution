package com.wireless.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.wireless.order.PrintFault;
import com.wireless.protocol.Reserved;

/**
 * Servlet implementation class Print
 */
@WebServlet("/Print")
public class PrintServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PrintServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String token = request.getParameter("token");
		int orderID = Integer.parseInt(request.getParameter("order_id"));
		byte conf = 0;
		
		String param = request.getParameter("print_sync");
		if(param != null){
			if(Byte.parseByte(param) == 0){
				conf &= ~Reserved.PRINT_SYNC;
			}else{
				conf |= Reserved.PRINT_SYNC;
			}
		}else{
			conf |= Reserved.PRINT_SYNC;
		}
		
		param = request.getParameter("print_order");
		if(param != null){
			if(Byte.parseByte(param) == 0){
				conf &= ~Reserved.PRINT_ORDER_2;
			}else{
				conf |= Reserved.PRINT_ORDER_2;
			}
		}else{
			conf &= ~Reserved.PRINT_ORDER_2;
		}
		
		param = request.getParameter("print_detail");
		if(param != null){
			if(Byte.parseByte(param) == 0){
				conf &= ~Reserved.PRINT_ORDER_DETAIL_2;
			}else{
				conf |= Reserved.PRINT_ORDER_DETAIL_2;
			}
		}else{
			conf &= ~Reserved.PRINT_ORDER_DETAIL_2;
		}
		
		param = request.getParameter("print_receipt");
		if(param != null){
			if(Byte.parseByte(param) == 0){
				conf &= ~Reserved.PRINT_RECEIPT_2;
			}else{
				conf |= Reserved.PRINT_RECEIPT_2;
			}
		}else{
			conf &= ~Reserved.PRINT_RECEIPT_2;
		}
		
		response.setContentType("text/html;charset=GB2312");
		
		Gson gson = new Gson();
		try{
			com.wireless.order.Print.exec(token, orderID, conf);
			response.getWriter().print(gson.toJson("打印" + orderID + "号账单成功"));
			
		}catch(PrintFault e){
			if(e.errType == PrintFault.ORDER_NOT_EXIST){
				response.getWriter().print(gson.toJson("打印失败，" + orderID + "号账单不存在"));
				
			}else if(e.errType == PrintFault.DB_ERROR){
				response.getWriter().print(gson.toJson("打印失败，与数据库通信未成功"));
				
			}else if(e.errType == PrintFault.SOCKET_ERROR){
				response.getWriter().print(gson.toJson("打印失败，与服务器通信未成功"));
				
			}else{
				response.getWriter().print(gson.toJson("打印失败"));
			}
			
		}catch(Exception e){
			
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
