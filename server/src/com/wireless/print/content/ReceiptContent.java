package com.wireless.print.content;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.db.QuerySetting;
import com.wireless.dbObject.Setting;
import com.wireless.print.PFormat;
import com.wireless.print.PVar;
import com.wireless.protocol.Order;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Util;

public class ReceiptContent extends ConcreteContent {

	private Restaurant _restaurant;
	private String _template;
	
	public ReceiptContent(Restaurant restaurant, String template, Order order, Terminal term, int printType, int style) {
		super(order, term, printType, style);
		_restaurant = restaurant;
		_template = template;
	}

	@Override 
	public String toString(){
		//get the receipt style to print
		int receiptStyle = Setting.RECEIPT_DEF;
		try{
			receiptStyle = QuerySetting.exec(_order.restaurantID).receiptStyle;
		}catch(SQLException e){}
		
		if(_printType == Reserved.PRINT_RECEIPT){
			//generate the title and replace the "$(title)" with it
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator(_order.isPaid ? "反结帐单" : "结帐单", _style).toString());
			//replace the $(restaurant)
			_template = _template.replace(PVar.RESTAURANT, new CenterAlignedDecorator(_restaurant.name != null ? _restaurant.name : "", _style).toString());
			//generate the total price string and replace the $(var_2) with this string
			_template = _template.replace(PVar.VAR_2, genTotalPrice(false));
			
		}else if(_printType == Reserved.PRINT_TEMP_RECEIPT){
			//generate the title and replace the "$(title)" with it
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("暂结单", _style).toString());
			//replace the $(restaurant)
			_template = _template.replace(PVar.RESTAURANT, new CenterAlignedDecorator(_restaurant.name != null ? _restaurant.name : "", _style).toString());
			//generate the total price string and replace the $(var_2) with this string
			_template = _template.replace(PVar.VAR_2, genTotalPrice(true));						
		}
		
		//replace the "$(order_id)"
		_template = _template.replace(PVar.ORDER_ID, Integer.toString(_order.id));
		
		//replace the "$(print_date)"
		_template = _template.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		
		//replace the "$(pay_manner)"
		String payManner;
		if(_order.pay_manner == Order.MANNER_CASH){
			payManner = "现金";			
		}else if(_order.pay_manner == Order.MANNER_CREDIT_CARD){
			payManner = "刷卡";			
		}else if(_order.pay_manner == Order.MANNER_HANG){
			payManner = "挂账";			
		}else if(_order.pay_manner == Order.MANNER_MEMBER){
			payManner = "会员卡";			
		}else if(_order.pay_manner == Order.MANNER_SIGN){
			payManner = "签单";			
		}else{
			payManner = "现金";	
		}
		_template = _template.replace(PVar.PAY_MANNER, payManner);
		
		//replace the "$(seq_id)"
		_template = _template.replace(PVar.SEQ_ID, Integer.toString(_order.seqID));
		
		//replace the "$(service_rate)"
		int serviceRate = Util.float2Int(_order.getServiceRate());
		_template = _template.replace(PVar.SERVICE_RATE, (serviceRate == 0 ? "" : "(" + serviceRate + "%服务费" + ")"));					
		
		//replace the "$(waiter)"
		_template = _template.replace(PVar.WAITER_NAME, _term.owner);
		
		//replace the "$(var_5)"
		_template = _template.replace(PVar.VAR_5, 
							new Grid2ItemsContent("餐台：" + _order.destTbl.aliasID + (_order.destTbl.name.length() == 0 ? "" : ("(" + _order.destTbl.name + ")")), 
												  "人数：" + _order.customNum, 
												  _printType, 
												  _style).toString());
		
		//generate the order food list and replace the $(var_1) with the ordered foods
		_template = _template.replace(PVar.VAR_1, new FoodListContent(genReciptFormat(receiptStyle), _order.foods, _style).toString());
		
		//replace the $(var_3) with the actual price
		_template = _template.replace(PVar.VAR_3, new RightAlignedDecorator("实收金额：" + Util.CURRENCY_SIGN + Util.float2String(_order.getActualPrice()), _style).toString());
		
		//generate the comment and replace the $(var_3)
		if(_order.comment != null && _order.comment.trim().length() != 0){
			_template = _template.replace(PVar.VAR_4, "备注：" + _order.comment);
		}else{
			_template = _template.replace(PVar.VAR_4, "");
		}
		
		return _template;
	}
	
	/**
	 * Generate the total price to print.
	 * The style to total price is as below.<br>
	 * --------------------------
	 *   赠送：￥0.00   实收：￥245.00
	 *   收款：￥250    找零：￥5.00 
	 * @param style one of the print style
	 * @return the generated sting for total price
	 */
	private String genTotalPrice(boolean isTempReceipt){
		
		String line1 = "$(gifted)  $(total_price)";
		String actualPrice = "实收：" + Util.CURRENCY_SIGN + Util.float2String(_order.getActualPrice());
		String gifted = "赠送：" + Util.CURRENCY_SIGN + Util.float2String(_order.calcGiftPrice());

		line1 = line1.replace("$(gifted)", gifted);
		line1 = line1.replace("$(total_price)", actualPrice);		
	
		String line2;
		if(_order.pay_manner == Order.MANNER_CASH && !isTempReceipt && _order.getCashIncome().floatValue() != 0){
			float chargeMoney = _order.getCashIncome().floatValue() - _order.getActualPrice().floatValue();
			chargeMoney = (float)Math.round(chargeMoney * 100) / 100;
			
			java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

			String chargeBack = "找零：" + Util.CURRENCY_SIGN + df.format(chargeMoney);
			String cashIncome = "收款：" + Util.CURRENCY_SIGN + Util.float2String(_order.getCashIncome());			
			
			line2 = "$(cashIncome)  $(chargeBack)";
			
			line2 = line2.replace("$(cashIncome)", cashIncome);
			line2 = line2.replace("$(chargeBack)", chargeBack);
			
		}else{
			line2 = "";
		}

		String line3;
		Float discount = _order.calcDiscountPrice();
		if(discount != 0){
			line3 = "$(discount)";
			line3 = line3.replace("$(discount)", "折扣：" + Util.CURRENCY_SIGN + Util.float2String(discount));
		}else{
			line3 = "";
		}
		
		String var = new RightAlignedDecorator(line1, _style).toString() +
					 (line2.length() != 0 ? "\r\n" + new RightAlignedDecorator(line2, _style) : "").toString() +
					 (line3.length() != 0 ? "\r\n" + new RightAlignedDecorator(line3, _style) : "").toString();
		
		try{
			var = new String(var.getBytes("GBK"), "GBK");
		}catch(UnsupportedEncodingException e){}
		
		return var;
	}
	
	/**
	 * Generate the receipt style to print. 
	 * @param receiptStyle
	 * @return the string format
	 */
	public String genReciptFormat(int receiptStyle){
		if((receiptStyle & Setting.RECEIPT_DEF) == Setting.RECEIPT_DEF){
			return PFormat.RECEIPT_FORMAT_DEF;
			
		}else{
			String format = PFormat.RECEIPT_FORMAT_DEF;
			if((receiptStyle & Setting.RECEIPT_STATUS) == 0){
				format = format.replace(PVar.FOOD_STATUS, "");
			}
			if((receiptStyle & Setting.RECEIPT_AMOUNT) == 0){
				format = format.replace(PVar.FOOD_AMOUNT, "");
			}
			if((receiptStyle & Setting.RECEIPT_DISCOUNT) == 0){
				format = format.replace(PVar.FOOD_DISCOUNT, "");
			}
			return format;
		}
	}
	
}
