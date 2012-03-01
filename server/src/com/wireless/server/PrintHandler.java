package com.wireless.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.wireless.db.QuerySetting;
import com.wireless.db.QueryShift;
import com.wireless.dbObject.Setting;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Mode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPrintOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Shift;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Terminal;
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
	private Socket[] _socks = null;
	private Restaurant _restaurant = null;
	private Terminal _term = null;
	private byte[] _funcCodes = null;
	
	public PrintHandler(Order orderInfo, Socket[] socks, int conf, Restaurant restaurant, Terminal term){
		_orderInfo = orderInfo;
		_socks = socks;
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
		if((conf & Reserved.PRINT_ALL_EXTRA_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_ALL_EXTRA_FOOD);
		}
		if((conf & Reserved.PRINT_EXTRA_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_EXTRA_FOOD);
		}
		if((conf & Reserved.PRINT_ALL_CANCELLED_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_ALL_CANCELLED_FOOD);
		}
		if((conf & Reserved.PRINT_CANCELLED_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_CANCELLED_FOOD);
		}
		if((conf & Reserved.PRINT_ALL_HURRIED_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_ALL_HURRIED_FOOD);
		}
		if((conf & Reserved.PRINT_TRANSFER_TABLE_2) != 0){
			if(orderInfo.table.aliasID != orderInfo.oriTbl.aliasID){
				printFuncs.add(Reserved.PRINT_TRANSFER_TABLE);
			}
		}
		if((conf & Reserved.PRINT_TEMP_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_TEMP_RECEIPT);
		}
		if((conf & Reserved.PRINT_SHIFT_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_SHIFT_RECEIPT);
		}
		if((conf & Reserved.PRINT_TEMP_SHIFT_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_TEMP_SHIFT_RECEIPT);
		}
		_funcCodes = new byte[printFuncs.size()];
		for(int i = 0; i < _funcCodes.length; i++){
			_funcCodes[i] = printFuncs.get(i);
		}
		_restaurant = restaurant;
		_term = term;
	}
	
	/**
	 * In this method, the print handler gets the template according to the print
	 * function code, send the print request and waits for an ACK (means successfully)
	 * or a NAK (means failed). If an IO error occurs, we close this connection and remove
	 * it from the printer connections.  
	 */
	public void run(){
		
		if(_funcCodes.length == 0){
			return;
		}
		
		LinkedList<PrintRequest> printReqs = new LinkedList<PrintRequest>();
		
		try{
			
			/**
			 * Generate each print request and have them stored in a queue.
			 */			
			for(int i = 0; i < _funcCodes.length; i++){	
				try{
					printReqs.add(new PrintRequest(_funcCodes[i], genPrintReq(_funcCodes[i])));
				}catch(PrintError e){
					e.printStackTrace();
				}catch(UnsupportedEncodingException e){
					e.printStackTrace();
				}
			}
			
			/**
			 * Enumerate the print request queue to send client all the requests.
			 * Remove the request from the queue if succeed to send the request.
			 */
			Iterator<PrintRequest> iter = printReqs.iterator();
			while(iter.hasNext()){
				
				PrintRequest reqPrint = iter.next();
				
				boolean isPrintOK = false;
				for(int i = 0; i < _socks.length; i++){
					if(_socks[i] == null){
						continue;
					}
					try{						
						/**
						 * Since socket would be shared with the print actions to the same restaurant,
						 * make the communication to the socket in synchronized mode.
						 */
						synchronized(_socks[i]){
							
							/**
							 * Check to see whether the printer connection is connected before performing the print action.
							 * Remove the socket if the connection has been disconnected.
							 */
							_socks[i].sendUrgentData(0);
							
							//send the print request
							send(_socks[i].getOutputStream(), reqPrint.pack);
							
							//_socks[i].sendUrgentData(0);
							
							//any socket works means print OK
							isPrintOK = true;							
							
							//receive the response and timeout after 10s
							ProtocolPackage respPrint = recv(_socks[i].getInputStream(), 10 * 1000);
							//check whether the response's sequence equals to the request's
							if(respPrint.header.seq == reqPrint.pack.header.seq){
								if(respPrint.header.mode != Mode.PRINT || respPrint.header.type != Type.ACK){
									throw new IOException("The Print Response(printType=" + reqPrint.conf + ")from printer server is not an ACK.");
								}							
							}
						}
						
					}catch(IOException e){
						e.printStackTrace();
						
						try{
							_socks[i].close();
							//throw an PrintSocketException
							//throw new PrintSocketException("The printer socket has been disconnected.");	
							
						}catch(IOException e2){
							
						}finally{
							//remove the invalid socket
							synchronized(WirelessSocketServer.printerConnections){
								ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(_restaurant.id);
								if(printerConn != null){
									printerConn.remove(_socks[i]);
									if(printerConn.size() == 0){
										WirelessSocketServer.printerConnections.remove(_restaurant.id);
									}
								}
							}		
							_socks[i] = null;
						}		
					}
				}	
				
				//remove the print request content if print OK
				if(isPrintOK){
					iter.remove();
				}
			}		
			
		}finally{
			/**
			 * Finally store the unprinted request,
			 * so that reprint these request while client re-connect to print server.
			 */
			storePrintLosses(printReqs);
		}
	}
	
	/**
	 * Store the unprinted requests to the list
	 * @param printType 
	 * 			the print type
	 * @param reqPrint
	 * 			the print request
	 */	
	private void storePrintLosses(List<PrintRequest> printReqs){
		synchronized(WirelessSocketServer.printLosses){
			
			LinkedList<ProtocolPackage> printLosses = WirelessSocketServer.printLosses.get(_restaurant.id);
			
			Iterator<PrintRequest> iter = printReqs.iterator();
			while(iter.hasNext()){
				PrintRequest reqPrint = iter.next();
				if(reqPrint != null){
					if(printLosses != null){
						/**
						 * Insert the unprinted request to the list,
						 * and remove the 1st one if the requests exceed a specific amount. 
						 */
						printLosses.add(reqPrint.pack);
						if(printLosses.size() > 50){
							printLosses.removeFirst();
						}
					}else{
						/**
						 * If the list does NOT exist before,
						 * create a new link list to hold the unprinted request
						 */
						printLosses = new LinkedList<ProtocolPackage>();
						printLosses.add(reqPrint.pack);
						WirelessSocketServer.printLosses.put(_restaurant.id, printLosses);
					}
				}
			}
		}
	}
	
	/**
	 * Send the print request to client.
	 * @param conn
	 * 			the socket connection
	 * @param printType
	 * 			the print type
	 * @param reqPrint
	 * 			the print request
	 * @throws IOException
	 * 			Throws if either of cases below.<br>
	 * 			- The response from client to this print is NAK.<br>
	 * 	 		- Fail to open input/output stream to this socket.<br>
	 * 			- Fail to read/write the content to this socket.<br>
	 */
//	private void sendPrintReq(Socket conn, byte printType, ProtocolPackage reqPrint) throws IOException{
//		/**
//		 * Since socket would be shared with the print actions to the same restaurant,
//		 * make the communication to the socket in synchronized mode.
//		 */
//		synchronized(conn){
//			InputStream in = new BufferedInputStream(conn.getInputStream());
//			OutputStream out = new BufferedOutputStream(conn.getOutputStream());
//			
//			//send the print request
//			send(out, reqPrint);
//			
//			//receive the response and timeout after 10s
//			ProtocolPackage respPrint = recv(in, 10 * 1000);
//			//check whether the response's sequence equals to the request's
//			if(respPrint.header.seq == reqPrint.header.seq){
//				if(respPrint.header.mode != Mode.PRINT || respPrint.header.type != Type.ACK){
//					throw new IOException("The Print Response(printType=" + printType + ")from printer server is not an ACK.");
//				}							
//			}
//		}
//	}
	
	/**
	 * Do the specific print action.
	 * @param printType 
	 * 				the print type to perform
	 * @param restaurant 
	 * 				the related restaurant information used for print template
	 * @throws PrintError
	 * 				throws if one of the cases below.<br>
	 * 				- The corresponding template is NOT found.<br> 				
	 * @throws UnsupportedEncodingException
	 *
	 */
	private ProtocolPackage genPrintReq(byte printType) throws PrintError, UnsupportedEncodingException{
		
		//get the print templates according to the function code
		HashMap<Integer, String> templates = null;
		if(printType == Reserved.PRINT_ORDER || printType == Reserved.PRINT_ALL_CANCELLED_FOOD || 
				printType == Reserved.PRINT_ALL_EXTRA_FOOD || printType == Reserved.PRINT_ALL_HURRIED_FOOD){
			//the template to order
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_ORDER);
			
		}else if(printType == Reserved.PRINT_ORDER_DETAIL || printType == Reserved.PRINT_EXTRA_FOOD || printType == Reserved.PRINT_CANCELLED_FOOD){
			//the template to order detail
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_ORDER_DETAIL);
			
		}else if(printType == Reserved.PRINT_RECEIPT || printType == Reserved.PRINT_TEMP_RECEIPT){
			//the template to receipt
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_RECEIPT);
			
		}else if(printType == Reserved.PRINT_TRANSFER_TABLE){
			//the template to table transfer
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_TRANSFER_TABLE);
			
		}else if(printType == Reserved.PRINT_SHIFT_RECEIPT || printType == Reserved.PRINT_TEMP_SHIFT_RECEIPT){
			//the template to shift
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_SHIFT_RECEIPT);
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

				int len;
				if(style == PStyle.PRINT_STYLE_58MM){
					len = PStyle.LEN_58MM;
				}else if(style == PStyle.PRINT_STYLE_80MM){
					len = PStyle.LEN_80MM;
				}else{
					len = PStyle.LEN_58MM;
				}
				
				String category;
				if(_orderInfo.category == Order.CATE_JOIN_TABLE){
					category = "(并台)";
				}else if(_orderInfo.category == Order.CATE_MERGER_TABLE){
					category = "(拼台)";
				}else if(_orderInfo.category == Order.CATE_TAKE_OUT){
					category = "(外卖)";
				}else{
					category = "";
				}
				//replace the $(order_id)
				String orderID = Integer.toString(_orderInfo.id) + category;
				printTemplate = printTemplate.replace(PVar.ORDER_ID, orderID);
				
				//replace the $(print_date)
				String printDate = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
				printTemplate = printTemplate.replace(PVar.PRINT_DATE, printDate);
				
				//replace the $(print_time)
				String printTime = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
				printTemplate = printTemplate.replace(PVar.PRINT_TIME, printTime);
				
				//replace the $(space_1) between order id and date time 
				//only applied to 80mm
				if(style == PStyle.PRINT_STYLE_80MM){
					int nSpace = len - 
					 			 10 - orderID.getBytes("GBK").length -		/* 账单号：3391 */
					 			 8 - printDate.getBytes("GBK").length - printTime.getBytes("GBK").length;  	/* 时间：2011-07-14 13:34:13 */

					StringBuffer space = new StringBuffer();
					for(int i = 0; i < nSpace; i++){
						space.append(" ");
					}
					printTemplate = printTemplate.replace("$(space_1)", space);
				}
				
				//replace the $(waiter)
				printTemplate = printTemplate.replace(PVar.WAITER_NAME, _term.owner);
				
				//replace the $(restaurant)
				printTemplate = printTemplate.replace(PVar.RESTAURANT, _restaurant.name != null ? new String(_restaurant.name.getBytes("GBK"), "GBK") : "");
				
				//replace the $(custom_num)
				String customNum = Integer.toString(_orderInfo.custom_num);
				printTemplate = printTemplate.replace(PVar.CUSTOM_NUM, customNum);
				
				//replace the $(table)
				String table = Integer.toString(_orderInfo.table.aliasID) + 
				  			   ((_orderInfo.table.name != null && _orderInfo.table.name.length() == 0) ? "" : "(" + _orderInfo.table.name + ")");
				printTemplate = printTemplate.replace(PVar.TABLE_ID, table);
				
				//replace the $(space_2) between custom number and table
				int nSpace = len - 
							 8 - table.getBytes("GBK").length -		/* 餐台：101(菊花厅) */
							 8 - customNum.getBytes("GBK").length +	/* 人数：3 */
							 2; 	
				StringBuffer space = new StringBuffer();
				for(int i = 0; i < nSpace; i++){
					space.append(" ");
				}
				printTemplate = printTemplate.replace("$(space_2)", space);
				
				//handle the print request to order
				if(printType == Reserved.PRINT_ORDER || printType == Reserved.PRINT_ALL_CANCELLED_FOOD || 
						printType == Reserved.PRINT_ALL_EXTRA_FOOD || printType == Reserved.PRINT_ALL_HURRIED_FOOD){
					//generate the title and replace the "$(title)" with it
					if(printType == Reserved.PRINT_ORDER){
						printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("点菜总单", null, style.intValue()));						
					}else if(printType == Reserved.PRINT_ALL_EXTRA_FOOD){
						printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("加菜总单", null, style.intValue()));
					}else if(printType == Reserved.PRINT_ALL_CANCELLED_FOOD){
						char[] format = { 0x1D, 0x21, 0x02 };
						printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("退  菜  总  单", format, style.intValue()));
					}else if(printType == Reserved.PRINT_ALL_HURRIED_FOOD){
						char[] format = { 0x1D, 0x21, 0x02 };
						printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("催  菜  总  单", format, style.intValue()));
					}else{
						printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("点菜总单", null, style.intValue()));
					}
					//generate the order food list and replace the $(var_1) with the ordered foods
					printTemplate = printTemplate.replace(PVar.VAR_1, genOrderFoodList(PFormat.RECEIPT_FORMAT_DEF, style.intValue()));
					bytesToPrint = printTemplate.getBytes("GBK");
					
				//handle the print request to order detail and extra food detail
				}else if(printType == Reserved.PRINT_ORDER_DETAIL || printType == Reserved.PRINT_EXTRA_FOOD || printType == Reserved.PRINT_CANCELLED_FOOD){
					
					//replace the $(space_3) between the table and waiter in order detail
					nSpace = len - 
							 6 - table.getBytes("GBK").length - 	/* 餐台:101(菊花厅) */
							 8 - _term.owner.getBytes("GBK").length -  	/* 服务员：李颖宜  */
							 2;
					
					space.delete(0, space.length());
					for(int i = 0; i < nSpace; i++){
						space.append(" ");
					}
					printTemplate = printTemplate.replace("$(space_3)", space);
					
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
							if(_orderInfo.foods[idx].hangStatus == OrderFood.FOOD_IMMEDIATE){
								tmp = tmp.replace(PVar.TITLE, genCentralTitle("即起单(详细)", null, style.intValue()));
								
							}else if(printType == Reserved.PRINT_ORDER_DETAIL){
								tmp = tmp.replace(PVar.TITLE, genCentralTitle("点菜" + 
										(_orderInfo.foods[idx].hangStatus == OrderFood.FOOD_HANG_UP ? "叫起" : "") +
																			  "单(详细)", null, style.intValue()));
								
							}else if(printType == Reserved.PRINT_EXTRA_FOOD){
								tmp = tmp.replace(PVar.TITLE, genCentralTitle("加菜" +
										(_orderInfo.foods[idx].hangStatus == OrderFood.FOOD_HANG_UP ? "叫起" : "") +
																			  "单(详细)", null, style.intValue()));
								
							}else if(printType == Reserved.PRINT_CANCELLED_FOOD){
								char[] format = { 0x1D, 0x21, 0x02 };
								tmp = tmp.replace(PVar.TITLE, genCentralTitle("退菜单(详细)", format, style.intValue()));
								
							}else{
								tmp = tmp.replace(PVar.TITLE, genCentralTitle("点菜单(详细)", null, style.intValue()));
							}
							//generate the order food detail info and replace the $(var_1) with it
							tmp = tmp.replace(PVar.VAR_1, genOrderFoodDetail(PFormat.RECEIPT_FORMAT_DEF, _orderInfo.foods[idx], style.intValue()));
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
									
				//handle the print request to receipt
				}else if(printType == Reserved.PRINT_RECEIPT || printType == Reserved.PRINT_TEMP_RECEIPT){
					//get the receipt style to print
					int receiptStyle = Setting.RECEIPT_DEF;
					try{
						receiptStyle = QuerySetting.exec(_term.restaurant_id).receiptStyle;
					}catch(SQLException e){}
					
					if(printType == Reserved.PRINT_RECEIPT){
						//generate the title and replace the "$(title)" with it
						printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("结帐单", null, style.intValue()));
						//generate the total price string and replace the $(var_2) with this string
						printTemplate = printTemplate.replace(PVar.VAR_2, genTotalPrice(style.intValue(), receiptStyle, false));
					}else if(printType == Reserved.PRINT_TEMP_RECEIPT){
						//generate the title and replace the "$(title)" with it
						printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("暂结单", null, style.intValue()));
						//generate the total price string and replace the $(var_2) with this string
						printTemplate = printTemplate.replace(PVar.VAR_2, genTotalPrice(style.intValue(), receiptStyle, true));						
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
					int serviceRate = Util.float2Int(_orderInfo.getServiceRate());
					printTemplate = printTemplate.replace(PVar.SERVICE_RATE, (serviceRate == 0 ? "" : "(" + serviceRate + "%服务费" + ")"));					
					
					//generate the order food list and replace the $(var_1) with the ordered foods
					printTemplate = printTemplate.replace(PVar.VAR_1, genOrderFoodList(PFormat.genReciptFormat(receiptStyle), style.intValue()));
					
					//generate the comment and replace the $(var_3)
					if(_orderInfo.comment != null){
						if(_orderInfo.comment.trim().length() != 0){
							printTemplate = printTemplate.replace(PVar.VAR_3, "备注：" + _orderInfo.comment);
						}else{
							printTemplate = printTemplate.replace(PVar.VAR_3, "");
						}
					}else{
						printTemplate = printTemplate.replace(PVar.VAR_3, "");
					}

					bytesToPrint = printTemplate.getBytes("GBK");
					
				}else if(printType == Reserved.PRINT_TRANSFER_TABLE){
				
					//generate the title and replace the "$(title)" with it
					printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("转台单", null, style.intValue()));

					//replace the $(var_1) with the table transfer message
					String msg = _orderInfo.oriTbl.aliasID + "号餐台转至" + _orderInfo.table.aliasID + "号餐台";
					printTemplate = printTemplate.replace(PVar.VAR_1, msg);
					
					bytesToPrint = printTemplate.getBytes("GBK");
					
				}else if(printType == Reserved.PRINT_SHIFT_RECEIPT || printType == Reserved.PRINT_TEMP_SHIFT_RECEIPT){
					try{
						Shift shift;
						if(printType == Reserved.PRINT_SHIFT_RECEIPT){
							shift = QueryShift.exec2(_term.pin, _term.modelID);
						}else if(printType == Reserved.PRINT_TEMP_SHIFT_RECEIPT){
							shift = QueryShift.exec(_term.pin, _term.modelID);
						}else{
							shift = QueryShift.exec2(_term.pin, _term.modelID);
						}
						//generate the title and replace the "$(title)" with it
						printTemplate = printTemplate.replace(PVar.TITLE, genCentralTitle("交班对账单", null, style.intValue()));
						//replace $(order_amount) 
						printTemplate = printTemplate.replace("$(order_amount)", Integer.toString(shift.orderAmount));
						//replace $(on_duty)
						printTemplate = printTemplate.replace("$(on_duty)", shift.onDuty);
						//replace $(off_duty)
						printTemplate = printTemplate.replace("$(off_duty)", shift.offDuty);
						//replace $(totalDiscount)
						printTemplate = printTemplate.replace("$(totalDiscount)", shift.totalDiscount.toString());
						//replace $(totalGift)
						printTemplate = printTemplate.replace("$(totalGift)", shift.totalGift.toString());
						//replace $(totalActual)
						printTemplate = printTemplate.replace("$(totalActual)", shift.totalActual.toString());
						//generate the shift detail string
						StringBuffer shiftDetail = new StringBuffer();
						shiftDetail.append(genShiftDetail("收款方式", "金额(￥)", "实收(￥)", style) + "\r\n");
						shiftDetail.append(genShiftDetail("现金", shift.totalCash.toString(), shift.totalCash2.toString(), style) + "\r\n");
						shiftDetail.append(genShiftDetail("刷卡", shift.totalCreditCard.toString(), shift.totalCreditCard2.toString(), style) + "\r\n");
						shiftDetail.append(genShiftDetail("会员卡", shift.totalMemberCard.toString(), shift.totalMemberCard.toString(), style) + "\r\n");
						shiftDetail.append(genShiftDetail("签单", shift.totalSign.toString(), shift.totalSign2.toString(), style) + "\r\n");
						shiftDetail.append(genShiftDetail("挂账", shift.totalHang.toString(), shift.totalHang2.toString(), style));
						//replace the $(var_1) with the shift detail
						printTemplate = printTemplate.replace(PVar.VAR_1, shiftDetail);
						
						
					}catch(BusinessException e){
						throw new PrintError(printType, "Fail to print shift receipt due to failed shift info query.");
					}catch(SQLException e){
						throw new PrintError(printType, "Fail to print shift receipt due to failed shift info query.");
					}
					
					bytesToPrint = printTemplate.getBytes("GBK");
					
				}else{
					bytesToPrint = new byte[0];
				}
				
				/**
				 * Generate the header to the print content for this style.
				 * Each single print content looks like below
				 * style : region : order_id[4] : len : order_date : len[2] : print_content
				 * style - 1-byte indicating one of the printer style
				 * region - 1-byte indicating the region to this print request
				 * order_id[4] - 4-byte indicating the order id
				 * len - the length to order_date 
				 * order_date - the order date represented as string
				 * len[2] - 2-byte indicating the length of following print content
				 * print_content - the print content
				 */
				
				byte[] orderDate = (printDate + " " + printTime).getBytes("GBK");			

				int headerLen = 1 + /* style takes up 1 byte */
								1 + /* region takes up 1 byte */
								4 + /* order id takes up 4 bytes */
								1 + /* length of order date takes up 1 byte */
								orderDate.length  + /* length of value to order date */
								2  /* length of print content takes up 2 bytes */;
				byte[] header = new byte[headerLen];
				//assign the style
				header[0] = (byte)(style.intValue() & 0x000000FF);
				//assign the region
				header[1] = (byte)(_orderInfo.region.regionID);
				//assign the order id
				header[2] = (byte)(_orderInfo.id & 0x000000FF);
				header[3] = (byte)((_orderInfo.id & 0x0000FF00) >> 8);
				header[4] = (byte)((_orderInfo.id & 0x00FF0000) >> 16);
				header[5] = (byte)((_orderInfo.id & 0xFF000000) >> 24);
				//assign the length of order date
				header[6] = (byte)orderDate.length;
				//assign the value of order date
				System.arraycopy(orderDate, 0, header, 7, orderDate.length);
				//assign the length of print content
				header[7 + orderDate.length] = (byte)(bytesToPrint.length & 0x000000FF);
				header[7 + orderDate.length + 1] = (byte)((bytesToPrint.length >> 8) & 0x000000FF);
				//sum the total bytes
				totalBytes += header.length + bytesToPrint.length;
				//append this style of print content
				printContent.put(header, bytesToPrint);
			}			


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
		
		return reqPrint;

	}
	
	
	/**
	 * Generate a single of the shift info as below.<br>
	 * -----------------------------------<br>
	 * ￥245.0         ￥423.0          ￥590<br>
	 * -----------------------------------<br>
	 * @param s1 the 1st string to show
	 * @param s2 the 2nd string to show
	 * @param s3 the 3rd string to show
	 * @param style the style to print
	 * @return the shift detail
	 */
	private String genShiftDetail(String s1, String s2, String s3, int style){
		int pos1 = 12;
		int pos2 = 23;
		if(style == PStyle.PRINT_STYLE_58MM){
			pos1 = 12;
			pos2 = 23;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			pos1 = 20;
			pos2 = 38;
		}
		
		try{
			StringBuffer space1 = new StringBuffer();
			int nString = pos1 - s1.getBytes("GBK").length;
			for(int i = 0; i < nString; i++){
				space1.append(" ");
			}
			
			StringBuffer space2 = new StringBuffer();
			nString = pos2 - s1.getBytes("GBK").length - space1.length() - s2.getBytes("GBK").length;
			for(int i = 0; i < nString; i++){
				space2.append(" ");
			}
			
			return s1 + space1 + s2 + space2 + s3;
			
		}catch(UnsupportedEncodingException e){
			return "Unsupported Encoding";
		}
	}
	
	/**
	 * Generate a single line of order food to print.
	 * The style to this food list is like below.<br>
	 * --------------------------------------------<br>
	 * (临)(叫)Food-Taste(1)(8.5折)(特,荐)    $32.00<br>
	 * --------------------------------------------<br>
	 * @param format the string format
	 * @param foodInfo the food info to print
	 * @param style one of the printer style
	 * @return the generated single line of order food string
	 */
	private String genOrderFoodDetail(String format, OrderFood foodInfo, int style){
		int len;
		if(style == PStyle.PRINT_STYLE_58MM){
			len = PStyle.LEN_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			len = PStyle.LEN_80MM;
		}else{
			len = PStyle.LEN_58MM;
		}
		String detailString = new String(format);
		detailString = detailString.replace(PFormat.FOOD_NAME, foodInfo.name);
		detailString = detailString.replace(PFormat.FOOD_AMOUNT, "(" + Util.float2String2(foodInfo.getCount()) + ")");
		String foodPrice = Util.CURRENCY_SIGN + Util.float2String(foodInfo.calcPrice2());
		
		String taste = "";
		if(foodInfo.tastePref.equals(Taste.NO_PREFERENCE)){
			detailString = detailString.replace(PFormat.FOOD_TASTE, "");
		}else{
			taste = "-" + foodInfo.tastePref;
			detailString = detailString.replace(PFormat.FOOD_TASTE, taste);				
		}

		String discount;
		if(!foodInfo.isSpecial() && foodInfo.getDiscount() != 1){
			discount = "(" + new Float(foodInfo.getDiscount() * 10).toString() + "折)";
			detailString = detailString.replace(PFormat.FOOD_DISCOUNT, discount);
		}else{
			detailString = detailString.replace(PFormat.FOOD_DISCOUNT, "");
			discount = "";
		}
		
		String status = "";
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
		if(foodInfo.isGift()){
			if(status.length() == 0){
				status = "赠";
			}else{
				status = status + ",赠";
			}
		}
		if(status.length() != 0){
			status = "(" + status + ")";
		}
		
		detailString = detailString.replace(PFormat.FOOD_STATUS, status);
		
		String hangStatus;
		if(foodInfo.hangStatus == OrderFood.FOOD_HANG_UP){
			hangStatus = "(叫)";
		}else if(foodInfo.hangStatus == OrderFood.FOOD_IMMEDIATE){
			hangStatus = "(即)";
		}else{
			hangStatus = "";
		}
		detailString = detailString.replace(PFormat.HANG_STATUS, hangStatus);
		
		String tempStatus;
		if(foodInfo.isTemporary){
			tempStatus = "(临)";
		}else{
			tempStatus = "";
		}
		detailString = detailString.replace(PFormat.TEMP_STATUS, tempStatus);
		
		try{
			int nSpace = len - 
						 (format.contains(PFormat.FOOD_NAME) ? foodInfo.name.getBytes("GBK").length : 0) - 	/* the food name */
						 (format.contains(PFormat.FOOD_TASTE) ? taste.getBytes("GBK").length : 0) - 		/* the food taste preference */
						 4 - 																				/* the parentheses for the amount of the order food */
						 (format.contains(PFormat.FOOD_AMOUNT) ? Util.float2String2(foodInfo.getCount()).length() : 0) - /* the amount of the order food */
						 (format.contains(PFormat.FOOD_DISCOUNT) ? discount.getBytes("GBK").length : 0) - 	/* the discount of the order food */
						 (format.contains(PFormat.FOOD_STATUS) ? status.getBytes("GBK").length : 0) - 		/* the amount of the discount */
						 (format.contains(PFormat.HANG_STATUS) ? hangStatus.getBytes().length : 0) - 		/* the hang status of the food */
						 (format.contains(PFormat.TEMP_STATUS) ? tempStatus.getBytes().length : 0) - 		/* the temporary status of the food */
						 (format.contains(PFormat.FOOD_UNIT_PRICE) ? foodPrice.length() : 0);				/* the food price */
			
			StringBuffer space = new StringBuffer();
			for(int cnt = 0; cnt < nSpace; cnt++){
				space.append(' ');
			}
			detailString = detailString.replace(PFormat.SPACE, space);
			detailString = detailString.replace(PFormat.FOOD_UNIT_PRICE, foodPrice);

			detailString = new String(detailString.getBytes("GBK"), "GBK");
//			food += '\r';
//			food += '\n';
			
		}catch(UnsupportedEncodingException e){}	
		
		return detailString;
	}
	
	/**
	 * Generate the order food list to print.
	 * The style to this food list is like below.<br>
	 * --------------------------------<br>
	 * Food1-Taste(1)		     $32.00<br>
	 * Food2-Taste(1)	         $23.50<br>
	 * Food3-Taste(1)	 	     $45.45<br>
	 * --------------------------------<br>
	 * @param the string format
	 * @param style one of the print style
	 * @return the generated order food list string
	 */
	private String genOrderFoodList(String format, int style){
		StringBuffer var1 = new StringBuffer();
		for(int idx = 0; idx < _orderInfo.foods.length; idx++){
			var1.append(genOrderFoodDetail(format, _orderInfo.foods[idx], style) + (idx < _orderInfo.foods.length - 1 ? "\r\n" : ""));
		}
		return var1.toString();
	}
	
	/**
	 * Generate the total price to print.
	 * The style to total price is as below.<br>
	 * --------------------------
	 *   赠送：￥0.00   应收：￥245.00
	 *   实收：￥250    找零：￥5.00 
	 * @param style one of the print style
	 * @return the generated sting for total price
	 */
	private String genTotalPrice(int style, int receiptStyle, boolean isTempReceipt){
		int len;
		if(style == PStyle.PRINT_STYLE_58MM){
			len = PStyle.LEN_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			len = PStyle.LEN_80MM;
		}else{
			len = PStyle.LEN_58MM;
		}
		
		String total = "$(space)$(gifted)  $(total_price)";
		String totalPrice = "应收：" + Util.CURRENCY_SIGN + Util.float2String(_orderInfo.getActualPrice());
		String gifted = "赠送：" + Util.CURRENCY_SIGN + Util.float2String(_orderInfo.calcGiftPrice());

		int nSpace = 0;
		try{
			nSpace = len 
			 - gifted.getBytes("GBK").length 		/* 赠送  */ 
			 - 3 									/* space */
			 - totalPrice.getBytes("GBK").length; 	/* 应收 */			
		}catch(UnsupportedEncodingException e){
			
		}
		StringBuffer space = new StringBuffer();
		for(int i = 0; i < nSpace; i++){
			space.append(' ');
		}
		total = total.replace("$(space)", space);
		total = total.replace("$(gifted)", gifted);
		total = total.replace("$(total_price)", totalPrice);		
	
		String cashInfo;
		if(_orderInfo.pay_manner == Order.MANNER_CASH && !isTempReceipt && _orderInfo.getCashIncome().floatValue() != 0){
			float chargeMoney = _orderInfo.getCashIncome().floatValue() - _orderInfo.getActualPrice().floatValue();
			chargeMoney = (float)Math.round(chargeMoney * 100) / 100;
			
			java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

			String chargeBack = "找零：" + Util.CURRENCY_SIGN + df.format(chargeMoney);
			String cashIncome = "实收：" + Util.CURRENCY_SIGN + Util.float2String(_orderInfo.getCashIncome());
			
			
			cashInfo = "$(space)$(cashIncome)  $(chargeBack)";
			
			try{
				nSpace = len 
						- cashIncome.getBytes("GBK").length 	/* 实收 */ 
						- 3 									/* space */
						- chargeBack.getBytes("GBK").length; 	/* 找零  */
			}catch(UnsupportedEncodingException e){
				
			}
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

		String discountInfo;
		Float discount = _orderInfo.calcDiscountPrice();
		if(discount != 0){
			discountInfo = "$(space)$(discount)";
			String discountPrice = "折扣：" + Util.CURRENCY_SIGN + Util.float2String(discount);
			try{
				nSpace = len 
				 		 - discountPrice.getBytes("GBK").length /* 折扣 */
						 - 1;
			}catch(UnsupportedEncodingException e){}
			space.delete(0, space.length());
			for(int i = 0; i < nSpace; i++){
				space.append(' ');
			}
			discountInfo = discountInfo.replace("$(space)", space);
			discountInfo = discountInfo.replace("$(discount)", discountPrice);
			discountInfo += "\r\n";
		}else{
			discountInfo = "";
		}
		
		String var = discountInfo + total + "\r\n" + cashInfo;
		
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
	private String genCentralTitle(String title, char[] format, int style){
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
			
			if(format != null){
				var = new String(format) + var;
			}
			
			var = new String(var.getBytes("GBK"), "GBK");
						
		}catch(UnsupportedEncodingException e){}
		
		return var;
	}	

}

final class PrintRequest{
	byte conf;
	ProtocolPackage pack;
	
	PrintRequest(){
		
	}
	
	PrintRequest(byte conf, ProtocolPackage pack){
		this.conf = conf;
		this.pack = pack;
	}
}

final class PFormat{
	final static String TEMP_STATUS = "$(temp_status)";
	final static String HANG_STATUS = "$(hang_status)";
	final static String FOOD_NAME = "$(name)";
	final static String FOOD_DISCOUNT = "$(discount)";
	final static String FOOD_TASTE = "$(taste)";
	final static String FOOD_AMOUNT = "$(count)";
	final static String FOOD_STATUS = "$(status)";
	final static String SPACE = "$(space)";
	final static String FOOD_UNIT_PRICE = "$(unit_price)";
	
	//$(temp_status)$(hang_status)$(name)$(taste)$(count)$(discount)$(status)$(space)$(unit_price)	
	final static String RECEIPT_FORMAT_DEF = TEMP_STATUS + HANG_STATUS + FOOD_NAME + FOOD_TASTE + FOOD_AMOUNT + FOOD_DISCOUNT + FOOD_STATUS + SPACE + FOOD_UNIT_PRICE;
	//$(name)($(count))$(unit_price)
	final static String FORMAT_2 = FOOD_NAME + FOOD_TASTE + "(" + FOOD_AMOUNT + ")" + SPACE + FOOD_UNIT_PRICE;

	/**
	 * Generate the receipt style to print. 
	 * @param receiptStyle
	 * @return the string format
	 */
	static String genReciptFormat(int receiptStyle){
		if((receiptStyle & Setting.RECEIPT_DEF) == Setting.RECEIPT_DEF){
			return PFormat.RECEIPT_FORMAT_DEF;
			
		}else{
			String format = PFormat.RECEIPT_FORMAT_DEF;
			if((receiptStyle & Setting.RECEIPT_STATUS) == 0){
				format = format.replace(PFormat.FOOD_STATUS, "");
			}
			if((receiptStyle & Setting.RECEIPT_AMOUNT) == 0){
				format = format.replace(PFormat.FOOD_AMOUNT, "");
			}
			if((receiptStyle & Setting.RECEIPT_DISCOUNT) == 0){
				format = format.replace(PFormat.FOOD_DISCOUNT, "");
			}
			return format;
		}
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
