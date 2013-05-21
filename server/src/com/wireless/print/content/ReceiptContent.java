package com.wireless.print.content;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.PFormat;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.PVar;
import com.wireless.server.WirelessSocketServer;

public class ReceiptContent extends ConcreteContent {

	private final Restaurant _restaurant;
	private final int _receiptStyle;
	private String _template;
	
	public ReceiptContent(int receiptStyle, Restaurant restaurant, Order order, String waiter, PType printType, PStyle style) {
		super(order, waiter, printType, style);
		_template = WirelessSocketServer.printTemplates.get(PType.PRINT_RECEIPT).get(style);
		_restaurant = restaurant;
		_receiptStyle = receiptStyle;
	}

	@Override 
	public String toString(){
		
		if(mPrintType == PType.PRINT_RECEIPT){
			//generate the title and replace the "$(title)" with it
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator(_order.isRepaid() ? "�����ʵ�" : "���ʵ�", mStyle).toString());
			//replace the $(restaurant)
			_template = _template.replace(PVar.RESTAURANT, new CenterAlignedDecorator(_restaurant.getName(), mStyle).toString());
			//generate the total price string and replace the $(var_2) with this string
			_template = _template.replace(PVar.VAR_2, buildTotalPrice(false));
			
		}else if(mPrintType == PType.PRINT_TEMP_RECEIPT){
			//generate the title and replace the "$(title)" with it
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("�ݽᵥ", mStyle).toString());
			//replace the $(restaurant)
			_template = _template.replace(PVar.RESTAURANT, new CenterAlignedDecorator(_restaurant.getName(), mStyle).toString());
			//generate the total price string and replace the $(var_2) with this string
			_template = _template.replace(PVar.VAR_2, buildTotalPrice(true));						
		}
		
		//replace the "$(order_id)"
		_template = _template.replace(PVar.ORDER_ID, Integer.toString(_order.getId()));
		
		//replace the "$(print_date)"
		_template = _template.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		
		//replace the "$(pay_manner)"
		String payManner;
		if(_order.isPayByCash()){
			payManner = "(�ֽ�)";			
		}else if(_order.isPayByCreditCard()){
			payManner = "(ˢ��)";			
		}else if(_order.isPayByHang()){
			payManner = "(����)";			
		}else if(_order.isPayByMember()){
			payManner = "(��Ա��)";			
		}else if(_order.isPayBySign()){
			payManner = "(ǩ��)";			
		}else{
			payManner = "(�ֽ�)";	
		}
		_template = _template.replace(PVar.PAY_MANNER, payManner);
		
		//replace the "$(order_cate)"
		String orderCate;
		if(_order.isMerged()){
			orderCate = "(��̨)";
		}else{
			orderCate = "";
		}
		_template = _template.replace(PVar.ORDER_CATE, orderCate);
		
		//replace the "$(seq_id)"
		_template = _template.replace(PVar.SEQ_ID, Integer.toString(_order.getSeqId()));
		
		//replace the "$(service_rate)"
		int serviceRate = NumericUtil.float2Int(_order.getServiceRate());
		_template = _template.replace(PVar.SERVICE_RATE, (serviceRate == 0 ? "" : "(" + serviceRate + "%�����" + ")"));					
		
		//replace the "$(waiter)"
		_template = _template.replace(PVar.WAITER_NAME, _waiter);
		
		StringBuffer tblInfo = new StringBuffer();
		if(_order.hasChildOrder()){
			for(Order childOrder : _order.getChildOrder()){
				tblInfo.append(childOrder.getDestTbl().getAliasId() + (childOrder.getDestTbl().getName().trim().length() == 0 ? "" : ("(" + _order.getDestTbl().getName() + ")"))).append(",");
			}
			if(tblInfo.length() > 0){
				tblInfo.deleteCharAt(tblInfo.length() - 1);
			}
			//replace the "$(var_5)"
			_template = _template.replace(PVar.VAR_5, "��̨��" + tblInfo + "(��" + _order.getCustomNum() + "��)");
			
		}else{
			tblInfo.append(_order.getDestTbl().getAliasId() + (_order.getDestTbl().getName().trim().length() == 0 ? "" : ("(" + _order.getDestTbl().getName() + ")")));
			//replace the "$(var_5)"
			_template = _template.replace(PVar.VAR_5, 
								new Grid2ItemsContent("��̨��" + tblInfo, 
													  "������" + _order.getCustomNum(), 
													  getStyle()).toString());

		}
		
		
		//generate the order food list and replace the $(var_1) with the ordered foods
		_template = _template.replace(PVar.VAR_1, new FoodListContent(buildReciptFormat(), _order.getOrderFoods(), mStyle).toString());
		
		//replace the $(var_3) with the actual price
		_template = _template.replace(PVar.VAR_3, new RightAlignedDecorator("ʵ�ս�" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_order.getActualPrice()), mStyle).toString());
		
		//generate the comment and replace the $(var_3)
		if(_order.getComment().isEmpty()){
			_template = _template.replace(PVar.VAR_4, "");
		}else{
			_template = _template.replace(PVar.VAR_4, "��ע��" + _order.getComment());
		}
		
		return _template;
	}
	
	/**
	 * Generate the total price to print.
	 * The style to total price is as below.<br>
	 * --------------------------
	 *   ���ͣ���0.00   Ӧ�գ���245.00
	 *   �տ��250    ���㣺��5.00 
	 * @param style one of the print style
	 * @return the generated sting for total price
	 */
	private String buildTotalPrice(boolean isTempReceipt){
		
		String line1 = "$(gifted)  $(total_price)";
		String actualPrice = "Ӧ�գ�" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_order.calcPriceBeforeDiscount());
		String gifted = "���ͣ�" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_order.calcGiftPrice());

		line1 = line1.replace("$(gifted)", gifted);
		line1 = line1.replace("$(total_price)", actualPrice);		
	
		String line2;
		if(_order.isPayByCash() && !isTempReceipt && _order.getReceivedCash() != 0){
			float chargeMoney = NumericUtil.roundFloat(_order.getReceivedCash() - _order.getActualPrice());
			
			java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

			String chargeBack = "���㣺" + NumericUtil.CURRENCY_SIGN + df.format(chargeMoney);
			String cashIncome = "�տ" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_order.getReceivedCash());			
			
			line2 = "$(cashIncome)  $(chargeBack)";
			
			line2 = line2.replace("$(cashIncome)", cashIncome);
			line2 = line2.replace("$(chargeBack)", chargeBack);
			
		}else{
			line2 = "";
		}

		StringBuffer line3 = new StringBuffer();
		Float discount = _order.calcDiscountPrice();
		if(discount != 0){
			line3.append("�ۿۣ�" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(discount));
		}
		
		if(_order.getErasePrice() > 0){
			if(line3.length() > 0){
				line3.append("  ");
			}
			line3.append("Ĩ����" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String((float)_order.getErasePrice()));
		}
		
		String var = new RightAlignedDecorator(line1, mStyle).toString() +
					 (line2.length() != 0 ? "\r\n" + new RightAlignedDecorator(line2, mStyle) : "").toString() +
					 (line3.length() != 0 ? "\r\n" + new RightAlignedDecorator(line3.toString(), mStyle) : "").toString();
		
		try{
			var = new String(var.getBytes("GBK"), "GBK");
		}catch(UnsupportedEncodingException e){}
		
		return var;
	}
	
	/**
	 * Generate the receipt style to print. 
	 * @param _receiptStyle
	 * @return the string format
	 */
	public String buildReciptFormat(){
		if((_receiptStyle & Setting.RECEIPT_DEF) == Setting.RECEIPT_DEF){
			return PFormat.RECEIPT_FORMAT_DEF;
			
		}else{
			String format = PFormat.RECEIPT_FORMAT_DEF;
			if((_receiptStyle & Setting.RECEIPT_STATUS) == 0){
				format = format.replace(PVar.FOOD_STATUS, "");
			}
			if((_receiptStyle & Setting.RECEIPT_AMOUNT) == 0){
				format = format.replace(PVar.FOOD_AMOUNT, "");
			}
			if((_receiptStyle & Setting.RECEIPT_DISCOUNT) == 0){
				format = format.replace(PVar.FOOD_DISCOUNT, "");
			}
			return format;
		}
	}
	
}
