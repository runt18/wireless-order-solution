package com.wireless.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wireless.order.ServerConnector;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqCancelOrder;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.ReqQueryOrder;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Type;


/**
 * Servlet implementation class SampleServlet
 */
@WebServlet("/SampleServlet")
public class SampleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SampleServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	private void cancelOrder(){
		try{
			//the table alias id
			short table = 100;
			ProtocolPackage _resp = ServerConnector.instance().ask(new ReqCancelOrder(table));
			if(_resp.header.type == Type.ACK){
				System.out.print("删台成功");
			}else{
				System.out.print("删台失败");
			}
		}catch(IOException e){
			System.err.println(e.getMessage());
		}catch(Exception e){
			System.err.println(e.getMessage());			
		}
	}
	
	private Order queryOrder() throws Exception{
		short tableID = 100;
		ProtocolPackage _resp = ServerConnector.instance().ask(new ReqQueryOrder(tableID));
		if(_resp.header.type == Type.ACK){
			/**
			 * the order doesn't contain the food's and taste's name and price
			 */
			Order order = RespParser.parseQueryOrder(_resp);
			return order;
		}else{
			if(_resp.header.reserved == ErrorCode.ORDER_NOT_EXIST){
				throw new Exception(tableID + "号台还未下单");
				
			}else if(_resp.header.reserved == ErrorCode.TABLE_NOT_EXIST){				
				throw new Exception(tableID + "号台信息不存在");
				
			}else if(_resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED){
				throw new Exception("终端没有登记到餐厅，请联系管理人员。");
				
			}else if(_resp.header.reserved == ErrorCode.TERMINAL_EXPIRED){
				throw new Exception("终端已过期，请联系管理人员。");	
				
			}else{
				throw new Exception("未确定的异常错误(" + _resp.header.reserved + ")");
			}
		}
	}
	
	private FoodMenu queryMenu() throws Exception{
		ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMenu());
		if(resp.header.type == Type.ACK){
			FoodMenu foodMenu = RespParser.parseQueryMenu(resp);
			return foodMenu;
		}else{
			if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
				throw new Exception("终端没有登记到餐厅，请联系管理人员。");
			}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
				throw new Exception("终端已过期，请联系管理人员。");
			} else {
				throw new Exception("菜谱下载失败，请检查网络信号或重新连接。");
			}
		}
	}
	
}
