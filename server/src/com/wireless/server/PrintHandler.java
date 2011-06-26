package com.wireless.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Mode;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPrintOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;

/**
 * The print handler is used for handling the print request.
 * Usually, the print request would be sent after inserting or being paid order,
 * it can run in synchronized or asynchronous mode.
 * In the synchronized mode, the order request must wait until the print request is done,
 * and responds with an ACK or NAK to terminal, so that let the terminal know whether succeed in printing or not.
 * While in the asynchronous mode, the order request returns immediately regardless of the print request,
 * that means the terminal doesn't care the status of print request any more.
 * And the synchronized and asynchronous mode can be chosen by setting the reserved byte of the protocol header. 
 */
public class PrintHandler extends Handler implements Runnable{
	
	private Order _orderInfo = null;
	private Socket _pConn = null;
	private Restaurant _restaurant = null;
	private String _waiter = null;
	private byte[] _funcCodes = null;
	
	public PrintHandler(Order orderInfo, Socket conn, byte conf, Restaurant restaurant, String waiter){
		_orderInfo = orderInfo;
		_pConn = conn;
		ArrayList<Byte> printFuncs = new ArrayList<Byte>();
		if((conf & Reserved.PRINT_ORDER_2) != 0){
			printFuncs.add(Reserved.PRINT_ORDER); 
		}
		if((conf & Reserved.PRINT_ORDER_DETAIL_2) != 0){
			printFuncs.add(Reserved.PRINT_ORDER_DETAIL);
		}
		if((conf & Reserved.PRINT_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_RECEIPT);
		}
		if((conf & Reserved.PRINT_EXTRA_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_EXTRA_FOOD);
		}
		if((conf & Reserved.PRINT_CANCELLED_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_CANCELLED_FOOD);
		}
		if((conf & Reserved.PRINT_TEMP_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_TEMP_RECEIPT);
		}
		_funcCodes = new byte[printFuncs.size()];
		for(int i = 0; i < _funcCodes.length; i++){
			_funcCodes[i] = printFuncs.get(i);
		}
		_restaurant = restaurant;
		_waiter = waiter;
	}
	
	/**
	 * In this method, the print handler gets the template according to the print
	 * function code, send the print request and waits for an ACK (means successfully)
	 * or a NAK (means failed). If an IO error occurs, we close this connection and remove
	 * it from the printer connections.  
	 * @throws PrintLogicException 
	 * 				throws if fails to perform any type of actions
	 * @throws PrintSocketException 
	 * 				throws if the print connection socket is invalid
	 */
	public void run2() throws PrintLogicException, PrintSocketException{
		/**
		 * Check to see whether the printer connection is connected before 
		 * performing the print action.
		 * Remove the socket and throw an PrintSocketException if the connection has been disconnected.
		 */
		try{
			_pConn.sendUrgentData(0);
		}catch(IOException e){
			try{
				_pConn.close();
				//throw an PrintSocketException
				throw new PrintSocketException("The printer socket has been disconnected.");	
				
			}catch(IOException ex){
				
			}finally{
				//remove the invalid socket
				synchronized(WirelessSocketServer.printerConnections){
					ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_restaurant.id));
					if(printerConn != null){
						printerConn.remove(_pConn);
						if(printerConn.size() == 0){
							WirelessSocketServer.printerConnections.remove(new Integer(_restaurant.id));
						}
					}
				}		
			}
		
		}
		
		/**
		 * Since more than one type of print actions could be performed in print handler at a time, 
		 * we need a list to hold all the error information.
		 * After performing all the print actions, throw the print logic exception if the error list is NOT empty.
		 */
		ArrayList<PrintError> errList = new ArrayList<PrintError>();
		for(int i = 0; i < _funcCodes.length; i++){

			try{
				doPrint(_funcCodes[i], _restaurant);
				
			}catch(PrintError e){
				//hold the print error to the list
				errList.add(e);
					
			}catch(IOException e){
				//hold the print error to the list
				errList.add(new PrintError(_funcCodes[i], e.getMessage()));
			}
		}
			
		//throw the print logic exception if the error list is NOT empty
		if(errList.size() != 0){
			throw new PrintLogicException(errList.toArray(new PrintError[errList.size()]));
		}

	}
	
	/**
	 * Do the specific print action.
	 * @param printType 
	 * 				the print type to perform
	 * @param restaurant 
	 * 				the related restaurant information used for print template
	 * @throws PrintError
	 * 				throws if one of the cases below.<br>
	 * 				- The corresponding template is NOT found.<br>
	 * 				- The response from client to this print is NAK.<br>
	 * @throws IOException
	 * 				throws if one of the cases below.<br>
	 * 				- Fail to open input/output stream to this socket.<br>
	 * 				- Fail to read/write the content to this socket.<br>
	 * 				- Timeout to wait for the response from client.<br>
	 */
	private void doPrint(byte printType, Restaurant restaurant) throws PrintError, IOException{
		
		//get the print templates according to the function code
		HashMap<Integer, String> templates = null;
		if(printType == Reserved.PRINT_ORDER){
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_ORDER);
			
		}else if(printType == Reserved.PRINT_ORDER_DETAIL || printType == Reserved.PRINT_EXTRA_FOOD || printType == Reserved.PRINT_CANCELLED_FOOD){
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_ORDER_DETAIL);
			
		}else if(printType == Reserved.PRINT_RECEIPT || printType == Reserved.PRINT_TEMP_RECEIPT){
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_RECEIPT);
		}
		
		//StringBuffer printContent = new StringBuffer();
		HashMap<byte[], byte[]> printContent = new HashMap<byte[], byte[]>(); 
		int totalBytes = 0;
		//enumerate to get each style of print template to this function code 
		Iterator<Integer> iter = templates.keySet().iterator();
		while(iter.hasNext()){	
			Integer style = iter.next();
			byte[] bytesToPrint = null;
			String printTemplate = templates.get(style);
			if(printTemplate != null){
				//replace the $(custom_num)
				printTemplate = printTemplate.replace(PVar.CUSTOM_NUM, new Integer(_orderInfo.custom_num).toString());
				//replace the $(order_id)
				printTemplate = printTemplate.replace(PVar.ORDER_ID, new Integer(_orderInfo.id).toString());
				//replace the $(waiter)
				printTemplate = printTemplate.replace(PVar.WAITER_NAME, _waiter);
				//replace the $(print_date)
				printTemplate = printTemplate.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
				//replace the $(print_time)
				printTemplate = printTemplate.replace(PVar.PRINT_TIME, new SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
				//replace the $(restaurant)
				printTemplate = printTemplate.replace(PVar.RESTAURANT, restaurant.name != null ? new String(restaurant.name.getBytes("GBK"), "GBK") : "");
				//replace the $(table)
				printTemplate = printTemplate.replace(PVar.TABLE_ID, Integer.toString(_orderInfo.table_id) + 
																			  (_orderInfo.table_name.length() == 0 ? "" : "(" + _orderInfo.table_name + ")"));
			}
			
			//handle the print request to order
			if(printType == Reserved.PRINT_ORDER){
				if(printTemplate != null){
					//generate the title and replace the "$(title)" with it
					printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("点菜单", style.intValue()));
					//generate the order food list and replace the $(var_1) with the ordered foods
					printTemplate = printTemplate.replace(PVar.VAR_1, genOrderFoodList(style.intValue()));
					bytesToPrint = printTemplate.getBytes("GBK");
				}else{
					throw new PrintError(printType, "The template to print order not be found.");
				}
				
			//handle the print request to order detail and extra food detail
			}else if(printType == Reserved.PRINT_ORDER_DETAIL || printType == Reserved.PRINT_EXTRA_FOOD || printType == Reserved.PRINT_CANCELLED_FOOD){
				if(printTemplate != null){
					/**
					 * Generate the content of order detail, the body looks like below.
					 * <order_detail_1> : <order_detail_2> : ...
					 * Each order detail looks like below.
					 * kitchen : len[2] : content
					 * kitchen - 1-byte indicating the kitchen no
					 * len[2] - 2-byte indicating the length of detail content
					 * content - the order detail content
					 */
					int totalOrderDetail = 0;
					byte[][] orderDetailHeaders = new byte[_orderInfo.foods.length][3];
					byte[][] orderDetailBodys = new byte[_orderInfo.foods.length][];
					for(int idx = 0; idx < _orderInfo.foods.length; idx++){
						//append the food to order detail if not belong any kitchen
						if(_orderInfo.foods[idx].kitchen != Kitchen.KITCHEN_NULL){
							String tmp = new String(printTemplate);
							//generate the title and replace the "$(title)" with it
							if(printType == Reserved.PRINT_ORDER_DETAIL){
								tmp = tmp.replace(PVar.TITLE, genCentralTitle("点菜单(详细)", style.intValue()));
							}else if(printType == Reserved.PRINT_EXTRA_FOOD){
								tmp = tmp.replace(PVar.TITLE, genCentralTitle("加菜单(详细)", style.intValue()));
							}else if(printType == Reserved.PRINT_CANCELLED_FOOD){
								tmp = tmp.replace(PVar.TITLE, genCentralTitle("退菜单(详细)", style.intValue()));
							}else{
								tmp = tmp.replace(PVar.TITLE, genCentralTitle("点菜单(详细)", style.intValue()));
							}
							//generate the order food detail info and replace the $(var_1) with it
							tmp = tmp.replace(PVar.VAR_1, genOrderFoodDetail(_orderInfo.foods[idx], style.intValue()));
							//assign the order detail's body
							orderDetailBodys[idx] = tmp.getBytes("GBK");									
							//assign the kitchen 
							orderDetailHeaders[idx][0] = (byte)_orderInfo.foods[idx].kitchen;
							//assign the length of order detail to its header
							orderDetailHeaders[idx][1] = (byte)((orderDetailBodys[idx].length) & 0x000000FF);
							orderDetailHeaders[idx][2] = (byte)(((orderDetailBodys[idx].length) >> 8) & 0x000000FF);
							//sum the size to order detail
							totalOrderDetail += orderDetailHeaders[idx].length + orderDetailBodys[idx].length;
						}
					}
					
					//allocate the memory for bytes to print
					bytesToPrint = new byte[totalOrderDetail];
					int offset = 0;
					for(int idx = 0; idx < _orderInfo.foods.length; idx++){
						if(_orderInfo.foods[idx].kitchen != Kitchen.KITCHEN_NULL){
							//append the header to the bytes to print
							System.arraycopy(orderDetailHeaders[idx], 0, bytesToPrint, offset, orderDetailHeaders[idx].length);
							offset += orderDetailHeaders[idx].length;
							//append the body to the bytes print
							System.arraycopy(orderDetailBodys[idx], 0, bytesToPrint, offset, orderDetailBodys[idx].length);
							offset += orderDetailBodys[idx].length;
						}
					}					
					
				}else{
					throw new PrintError(printType, "The template to print order detail not be found.");
				}
				
			//handle the print request to receipt
			}else if(printType == Reserved.PRINT_RECEIPT || printType == Reserved.PRINT_TEMP_RECEIPT){
				if(printTemplate != null){
					if(printType == Reserved.PRINT_RECEIPT){
						//generate the title and replace the "$(title)" with it
						printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("结帐单", style.intValue()));
						//generate the total price string and replace the $(var_2) with this string
						printTemplate = printTemplate.replace(PVar.VAR_2, genTotalPrice(style.intValue(), true));
					}else if(printType == Reserved.PRINT_TEMP_RECEIPT){
						//generate the title and replace the "$(title)" with it
						printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("暂结单", style.intValue()));
						//generate the total price string and replace the $(var_2) with this string
						printTemplate = printTemplate.replace(PVar.VAR_2, genTotalPrice(style.intValue(), false));						
					}
					//replace the "$(pay_manner)"
					String payManner;
					if(_orderInfo.pay_manner == Order.MANNER_CASH){
						payManner = "现金";			
					}else if(_orderInfo.pay_manner == Order.MANNER_CREDIT_CARD){
						payManner = "刷卡";			
					}else if(_orderInfo.pay_manner == Order.MANNER_HANG){
						payManner = "挂账";			
					}else if(_orderInfo.pay_manner == Order.MANNER_MEMBER){
						payManner = "会员卡";			
					}else if(_orderInfo.pay_manner == Order.MANNER_SIGN){
						payManner = "签单";			
					}else{
						payManner = "现金";	
					}
					printTemplate = printTemplate.replace(PVar.PAY_MANNER, payManner);
					//replace the "$(service_rate)"
					printTemplate = printTemplate.replace(PVar.SERVICE_RATE, (_orderInfo.service_rate == 0 ? "免" : _orderInfo.service_rate + "%"));
					//generate the order food list and replace the $(var_1) with the ordered foods
					printTemplate = printTemplate.replace(PVar.VAR_1, genOrderFoodList(style.intValue()));
					
					bytesToPrint = printTemplate.getBytes("GBK");
					
				}else{
					throw new PrintError(printType, "The template to print receipt not be found.");
				}
				
			}else{
				bytesToPrint = new byte[0];
			}
			/**
			 * Generate this style to the print content.
			 * Each single print content looks like below
			 * style : len[2] : print_content
			 * style - 1-byte indicating one of the printer style
			 * len[2] - 2-byte indicating the length of following print content
			 * print_content - the print content
			 */
			//generate the header to this single style print content
			byte[] header = new byte[3];
			header[0] = (byte)(style.intValue() & 0x000000FF);
			header[1] = (byte)(bytesToPrint.length & 0x000000FF);
			header[2] = (byte)((bytesToPrint.length >> 8) & 0x000000FF);
			//sum the total bytes
			totalBytes += header.length + bytesToPrint.length;
			//append this style of print content
			printContent.put(header, bytesToPrint);
		}
		/**
		 * Generate the entire body for the print request, 
		 * the body consist of all styles of print content.
		 * The entire body looks like as below 
		 * <Print_1> : <Print_2> : ... : <Print_n>
		 * <Print_n>
		 * style : len[2] : print_content
		 */
		byte[] body = new byte[totalBytes];
		Iterator<byte[]> iter1 = printContent.keySet().iterator();
		int cnt = 0;
		while(iter1.hasNext()){
			//copy the header to body
			byte[] header = iter1.next();
			System.arraycopy(header, 0, body, cnt, header.length);
			cnt += header.length;
			//copy the print content to body
			byte[] bytesToPrint = printContent.get(header);
			System.arraycopy(bytesToPrint, 0, body, cnt, bytesToPrint.length);
			cnt += bytesToPrint.length;
		}
		
		ProtocolPackage reqPrint = null;
		if(body.length != 0){
			reqPrint = new ReqPrintOrder(body, _orderInfo, printType);
		}		
		
		/**
		 * Since socket would be shared with the print actions to the same restaurant,
		 * make the communication to the socket in synchronized mode.
		 */
		synchronized(_pConn){
			InputStream in = new BufferedInputStream(_pConn.getInputStream());
			OutputStream out = new BufferedOutputStream(_pConn.getOutputStream());
			
			//send the print request
			send(out, reqPrint);	
			
			//receive the response and timeout after 10s
			ProtocolPackage respPrint = recv(in, 10 * 1000);
			//check whether the response's sequence equals to the request's
			if(respPrint.header.seq == reqPrint.header.seq){
				if(respPrint.header.mode != Mode.PRINT || respPrint.header.type != Type.ACK){
					throw new PrintError(printType, "Response from printer server is not an ACK.");
				}							
			}
		}
	}
	
	
	/**
	 * Just invoke the run2 method to send the print request.
	 * Here the run method is for the "Runnable" interface, since the
	 * print handler might run in a new thread if it's asynchronous.
	 */
	public void run(){
		try{
			run2();
		}catch(PrintLogicException e){
			e.printStackTrace();
		}catch(PrintSocketException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate a single line of order food to print.
	 * The style to this food list is like below.<br>
	 * -----------------------------------<br>
	 * Food-Taste(1)(8.5折,荐)      $32.00<br>
	 * -----------------------------------<br>
	 * @param foodInfo the food info to print
	 * @param style one of the printer style
	 * @return the generated single line of order food string
	 */
	private String genOrderFoodDetail(Food foodInfo, int style){
		int len;
		if(style == PStyle.PRINT_STYLE_58MM){
			len = PStyle.LEN_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			len = PStyle.LEN_80MM;
		}else{
			len = PStyle.LEN_58MM;
		}
		String food = "$(name)$(taste)($(count))$(status)$(space)$(unit_price)";
		food = food.replace("$(name)", foodInfo.name);
		food = food.replace("$(count)", foodInfo.getCount().toString());
		String foodPrice = Util.CURRENCY_SIGN + Util.float2String(foodInfo.totalPrice2());
		
		String taste = "";
		if(foodInfo.taste.alias_id != Taste.NO_TASTE){
			taste = "-" + foodInfo.taste.preference;
			food = food.replace("$(taste)", taste);				
		}else{
			food = food.replace("$(taste)", "");
		}
		
		String status = "";
		if(!foodInfo.isSpecial() && foodInfo.discount != 100){
			status = new Float((float)foodInfo.discount / 10).toString();
			status = status + "折";
		}
		if(foodInfo.isSpecial()){
			if(status.length() == 0){
				status = "特";
			}else{
				status = status + ",特";
			}
		}
		if(foodInfo.isRecommend()){
			if(status.length() == 0){
				status = "荐";
			}else{
				status = status + ",荐";
			}
		}
		
		if(status.length() != 0){
			status = "(" + status + ")";
		}
		
		food = food.replace("$(status)", status);
		
		try{
			int nSpace = len - 
						 foodInfo.name.getBytes("GBK").length - /* the food name */
						 taste.getBytes("GBK").length - /* the food taste preference */
						 4 - /* the parentheses for the amount of the order food */
						 foodInfo.getCount().toString().length() - /* the amount of the order food */ 
						 status.getBytes("GBK").length - /* the amount of the discount */
						 foodPrice.length(); /* the food price */
			
			StringBuffer space = new StringBuffer();
			for(int cnt = 0; cnt < nSpace; cnt++){
				space.append(' ');
			}
			food = food.replace("$(space)", space);
			food = food.replace("$(unit_price)", foodPrice);

			food = new String(food.getBytes("GBK"), "GBK");
//			food += '\r';
//			food += '\n';
			
		}catch(UnsupportedEncodingException e){}	
		
		return food;
	}
	
	/**
	 * Generate the order food list to print.
	 * The style to this food list is like below.<br>
	 * --------------------------------<br>
	 * Food1-Taste(1)(8.5折)     $32.00<br>
	 * Food2-Taste(1)(特)        $23.50<br>
	 * Food3-Taste(1)(特,荐)     $45.45<br>
	 * --------------------------------<br>
	 * @param style one of the print style
	 * @return the generated order food list string
	 */
	private String genOrderFoodList(int style){
		StringBuffer var1 = new StringBuffer();
		for(int idx = 0; idx < _orderInfo.foods.length; idx++){
			var1.append(genOrderFoodDetail(_orderInfo.foods[idx], style) + (idx < _orderInfo.foods.length - 1 ? "\r\n" : ""));
		}
		return var1.toString();
	}
	
	/**
	 * Generate the total price to print.
	 * The style to total price is as below.<br>
	 * --------------------------
	 *               合计：￥245.00
	 * 现金实收：￥250    找零：￥5.00 
	 * @param style one of the print style
	 * @return the generated sting for total price
	 */
	private String genTotalPrice(int style, boolean isTempReceipt){
		int len;
		if(style == PStyle.PRINT_STYLE_58MM){
			len = PStyle.LEN_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			len = PStyle.LEN_80MM;
		}else{
			len = PStyle.LEN_58MM;
		}
		
		String total = "$(space)合计：$(total_price)";
		String totalPrice = Util.CURRENCY_SIGN + Util.float2String(_orderInfo.getActualPrice());

		int nSpace = len - 8 - totalPrice.length();
		StringBuffer space = new StringBuffer();
		for(int i = 0; i < nSpace; i++){
			space.append(' ');
		}
		total = total.replace("$(space)", space);
		total = total.replace("$(total_price)", totalPrice);		
	
		String cashInfo;
		if(_orderInfo.pay_manner == Order.MANNER_CASH && !isTempReceipt){
			float chargeMoney = _orderInfo.getCashIncome().floatValue() - _orderInfo.getActualPrice().floatValue();
			chargeMoney = (float)Math.round(chargeMoney * 100) / 100;
			
			 java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

			String chargeBack = Util.CURRENCY_SIGN + df.format(chargeMoney);
			String cashIncome = Util.CURRENCY_SIGN + Util.float2String(_orderInfo.getCashIncome());
			
			cashInfo = "$(space)实收：$(cashIncome) 找零：$(chargeBack)";
			
			nSpace = len - 8 - cashIncome.length() - 8 - chargeBack.length();
			space.delete(0, space.length());
			for(int i = 0; i < nSpace; i++){
				space.append(' ');
			}
			cashInfo = cashInfo.replace("$(space)", space);
			cashInfo = cashInfo.replace("$(cashIncome)", cashIncome);
			cashInfo = cashInfo.replace("$(chargeBack)", chargeBack);
			
		}else{
			cashInfo = "";
		}
		
		String var = total + "\r\n" + cashInfo;
		
		try{
			var = new String(var.getBytes("GBK"), "GBK");
		}catch(UnsupportedEncodingException e){}
		
		return var;
	}
	
	/**
	 * Generate the title aligned to center.
	 * @param title the title
	 * @param style one of the print style
	 * @return the title aligned to center
	 */
	private String genCentralTitle(String title, int style){
		int len;
		if(style == PStyle.PRINT_STYLE_58MM){
			len = PStyle.LEN_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			len = PStyle.LEN_80MM;
		}else{
			len = PStyle.LEN_58MM;
		}

		String var = "$(space_left)$(title)";
		try{
			//calculate the amount left spaces
			//and replace the $(space_left)
			int nLSpace = (len - title.getBytes("GBK").length) / 2;
			StringBuffer space = new StringBuffer();
			for(int i = 0; i < nLSpace; i++){
				space.append(' ');
			}
			var = var.replace("$(space_left)", space);
			
			//replace the $(title)
			var = var.replace("$(title)", title);
			
			var = new String(var.getBytes("GBK"), "GBK");
		}catch(UnsupportedEncodingException e){}
		return var;
	}
}

final class PVar{
	final static String TITLE = "$(title)";
	final static String RESTAURANT = "$(restaurant)";
	final static String ORDER_ID = "$(order_id)";
	final static String WAITER_NAME = "$(waiter)";
	final static String PRINT_DATE = "$(print_date)";
	final static String PRINT_TIME = "$(print_time)";
	final static String SERVICE_RATE = "$(service_rate)";
	final static String PAY_MANNER = "$(pay_manner)";
	final static String TABLE_ID = "$(table)";
	final static String CUSTOM_NUM = "$(custom_num)";
	final static String VAR_1 = "$(var_1)";
	final static String VAR_2 = "$(var_2)";
	final static String VAR_3 = "$(var_3)";
}

final class PStyle{
	final static int PRINT_STYLE_UNKNOWN = 0;
	final static int PRINT_STYLE_58MM = 1;
	final static int PRINT_STYLE_80MM = 2;
	final static int LEN_58MM = 33;
	final static int LEN_80MM = 48;
}
