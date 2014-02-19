package com.wireless.print.content;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.PFormat;
import com.wireless.print.PVar;
import com.wireless.server.WirelessSocketServer;

public class ReceiptContent extends ConcreteContent {

	private final Restaurant mRestaurant;
	private final int mReceiptStyle;
	private String mTemplate;
	
	public ReceiptContent(int receiptStyle, Restaurant restaurant, Order order, String waiter, PType printType, PStyle style) {
		super(order, waiter, printType, style);
		mTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_RECEIPT).get(style);
		mRestaurant = restaurant;
		mReceiptStyle = receiptStyle;
	}

	@Override 
	public String toString(){
		if(mPrintType == PType.PRINT_RECEIPT){
			//generate the title and replace the "$(title)" with it
			mTemplate = mTemplate.replace(PVar.TITLE, new CenterAlignedDecorator(mOrder.isRepaid() ? "反结帐单" : "结帐单", mStyle).toString());
			//replace the $(restaurant)
			mTemplate = mTemplate.replace(PVar.RESTAURANT, new CenterAlignedDecorator(mRestaurant.getName(), mStyle).toString());
			//generate the total price string and replace the $(var_2) with this string
			mTemplate = mTemplate.replace(PVar.VAR_2, buildTotalPrice(false));
			
		}else if(mPrintType == PType.PRINT_TEMP_RECEIPT){
			//generate the title and replace the "$(title)" with it
			mTemplate = mTemplate.replace(PVar.TITLE, new CenterAlignedDecorator("暂结单", mStyle).toString());
			//replace the $(restaurant)
			mTemplate = mTemplate.replace(PVar.RESTAURANT, new CenterAlignedDecorator(mRestaurant.getName(), mStyle).toString());
			//generate the total price string and replace the $(var_2) with this string
			mTemplate = mTemplate.replace(PVar.VAR_2, buildTotalPrice(true));						
		}
		
		//replace the "$(order_id)"
		mTemplate = mTemplate.replace(PVar.ORDER_ID, Integer.toString(mOrder.getId()));
		
		//replace the "$(print_date)"
		mTemplate = mTemplate.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		
		//replace the "$(pay_manner)"
		String payManner;
		if(mOrder.isPayByCash()){
			payManner = "(现金)";			
		}else if(mOrder.isPayByCreditCard()){
			payManner = "(刷卡)";			
		}else if(mOrder.isPayByHang()){
			payManner = "(挂账)";			
		}else if(mOrder.isPayByMember()){
			payManner = "(会员卡)";			
		}else if(mOrder.isPayBySign()){
			payManner = "(签单)";			
		}else{
			payManner = "(现金)";	
		}
		mTemplate = mTemplate.replace(PVar.PAY_MANNER, payManner);
		
		//replace the "$(order_cate)"
		mTemplate = mTemplate.replace(PVar.ORDER_CATE, "");
		
		//replace the "$(seq_id)"
		mTemplate = mTemplate.replace(PVar.SEQ_ID, Integer.toString(mOrder.getSeqId()));
		
		//replace the "$(service_rate)"
		int serviceRate = NumericUtil.float2Int(mOrder.getServiceRate());
		mTemplate = mTemplate.replace(PVar.SERVICE_RATE, (serviceRate == 0 ? "" : "(" + serviceRate + "%服务费" + ")"));					
		
		//replace the "$(waiter)"
		mTemplate = mTemplate.replace(PVar.WAITER_NAME, mWaiter);
		
		StringBuilder tblInfo = new StringBuilder();
		
		tblInfo.append(mOrder.getDestTbl().getAliasId() + (mOrder.getDestTbl().getName().trim().length() == 0 ? "" : ("(" + mOrder.getDestTbl().getName() + ")")));
		//replace the "$(var_5)"
		mTemplate = mTemplate.replace(PVar.VAR_5, 
							new Grid2ItemsContent("餐台：" + tblInfo, 
												  "人数：" + mOrder.getCustomNum(), 
												  getStyle()).toString());
		
		
		//generate the order food list and replace the $(var_1) with the ordered foods
		mTemplate = mTemplate.replace(PVar.VAR_1, new FoodListContent(buildReciptFormat(), mOrder.getOrderFoods(), mStyle).toString());
		
		//replace the $(var_3) with the actual price
		mTemplate = mTemplate.replace(PVar.VAR_3, new RightAlignedDecorator("实收金额：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.getActualPrice()), mStyle).toString());
		
		//generate the comment and replace the $(var_3)
		if(mOrder.getComment().isEmpty()){
			mTemplate = mTemplate.replace(PVar.VAR_4, "");
		}else{
			mTemplate = mTemplate.replace(PVar.VAR_4, "备注：" + mOrder.getComment());
		}
		
		return mTemplate;
	}
	
	/**
	 * Generate the total price to print.
	 * The style to total price is as below.<br>
	 * --------------------------
	 *   赠送：￥0.00   应收：￥245.00
	 *   收款：￥250    找零：￥5.00 
	 * @param style one of the print style
	 * @return the generated sting for total price
	 */
	private String buildTotalPrice(boolean isTempReceipt){
		
		StringBuilder line1 = new StringBuilder();
		line1.append("应收：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.calcPriceBeforeDiscount()))
			 .append("  ")
			 .append("赠送：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.calcGiftPrice()));
	
		StringBuilder line2 = new StringBuilder();
		if(mOrder.isPayByCash() && !isTempReceipt && mOrder.getReceivedCash() != 0){
			float chargeMoney = NumericUtil.roundFloat(mOrder.getReceivedCash() - mOrder.getActualPrice());
			
			java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

			line2.append("找零：" + NumericUtil.CURRENCY_SIGN + df.format(chargeMoney))
				 .append("  ")
				 .append("收款：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.getReceivedCash()));
			
		}

		StringBuilder line3 = new StringBuilder();
		if(mOrder.calcDiscountPrice() != 0){
			line3.append("折扣：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.calcDiscountPrice()));
		}
		
		if(mOrder.getErasePrice() > 0){
			if(line3.length() > 0){
				line3.append("  ");
			}
			line3.append("抹数：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.getErasePrice()));
		}
		
		StringBuilder line4 = new StringBuilder();
		if(mOrder.getCouponPrice() > 0){
			line3.append("优惠券：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.getCouponPrice()));
		}
		
		String var = new RightAlignedDecorator(line1.toString(), mStyle).toString() +
					 (line2.length() != 0 ? SEP + new RightAlignedDecorator(line2.toString(), mStyle) : "").toString() +
					 (line3.length() != 0 ? SEP + new RightAlignedDecorator(line3.toString(), mStyle) : "").toString() +
					 (line4.length() != 0 ? SEP + new RightAlignedDecorator(line3.toString(), mStyle) : "").toString();
		
		try{
			var = new String(var.getBytes("GBK"), "GBK");
		}catch(UnsupportedEncodingException e){}
		
		return var;
	}
	
	/**
	 * Generate the receipt style to print. 
	 * @param mReceiptStyle
	 * @return the string format
	 */
	public String buildReciptFormat(){
		if((mReceiptStyle & Setting.RECEIPT_DEF) == Setting.RECEIPT_DEF){
			return PFormat.RECEIPT_FORMAT_DEF;
			
		}else{
			String format = PFormat.RECEIPT_FORMAT_DEF;
			if((mReceiptStyle & Setting.RECEIPT_STATUS) == 0){
				format = format.replace(PVar.FOOD_STATUS, "");
			}
			if((mReceiptStyle & Setting.RECEIPT_AMOUNT) == 0){
				format = format.replace(PVar.FOOD_AMOUNT, "");
			}
			if((mReceiptStyle & Setting.RECEIPT_DISCOUNT) == 0){
				format = format.replace(PVar.FOOD_DISCOUNT, "");
			}
			return format;
		}
	}
	
}
