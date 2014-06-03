package com.wireless.print.content;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.PVar;
import com.wireless.print.content.FoodDetailContent.DisplayConfig;
import com.wireless.print.content.FoodDetailContent.DisplayItem;
import com.wireless.server.WirelessSocketServer;

public class ReceiptContent extends ConcreteContent {

	private final Restaurant mRestaurant;
	private final int mReceiptStyle;
	private String mTemplate;
	private final String mWaiter;
	private final Order mOrder;

	public ReceiptContent(int receiptStyle, Restaurant restaurant, Order order, String waiter, PType printType, PStyle style) {
		super(printType, style);
		mTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_RECEIPT).get(style);
		mRestaurant = restaurant;
		mReceiptStyle = receiptStyle;
		mWaiter = waiter;
		mOrder = order;
	}

	@Override 
	public String toString(){
		if(mPrintType == PType.PRINT_RECEIPT){
			//generate the title and replace the "$(title)" with it
			mTemplate = mTemplate.replace(PVar.TITLE, new CenterAlignedDecorator(mOrder.isRepaid() ? "�����ʵ�" : "���ʵ�", mStyle).toString());
			//replace the $(restaurant)
			mTemplate = mTemplate.replace(PVar.RESTAURANT, new CenterAlignedDecorator(mRestaurant.getName(), mStyle).toString());
			//generate the total price string and replace the $(var_2) with this string
			mTemplate = mTemplate.replace(PVar.VAR_2, buildTotalPrice(false));
			
		}else if(mPrintType == PType.PRINT_TEMP_RECEIPT){
			//generate the title and replace the "$(title)" with it
			mTemplate = mTemplate.replace(PVar.TITLE, new CenterAlignedDecorator("�ݽᵥ", mStyle).toString());
			//replace the $(restaurant)
			mTemplate = mTemplate.replace(PVar.RESTAURANT, new CenterAlignedDecorator(mRestaurant.getName(), mStyle).toString());
			//generate the total price string and replace the $(var_2) with this string
			mTemplate = mTemplate.replace(PVar.VAR_2, buildTotalPrice(true));						
		}
		
		//replace the "$(order_id)"
		mTemplate = mTemplate.replace(PVar.ORDER_ID, Integer.toString(mOrder.getId()));
		
		//replace the "$(print_date)"
		mTemplate = mTemplate.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
		
		//replace the "$(pay_manner)"
		String payManner;
		if(mOrder.isPayByCash()){
			payManner = "(�ֽ�)";			
		}else if(mOrder.isPayByCreditCard()){
			payManner = "(ˢ��)";			
		}else if(mOrder.isPayByHang()){
			payManner = "(����)";			
		}else if(mOrder.isPayByMember()){
			payManner = "(��Ա��)";			
		}else if(mOrder.isPayBySign()){
			payManner = "(ǩ��)";			
		}else{
			payManner = "(�ֽ�)";	
		}
		mTemplate = mTemplate.replace(PVar.PAY_MANNER, payManner);
		
		//replace the "$(order_cate)"
		mTemplate = mTemplate.replace(PVar.ORDER_CATE, "");
		
		//replace the "$(seq_id)"
		mTemplate = mTemplate.replace(PVar.SEQ_ID, Integer.toString(mOrder.getSeqId()));
		
		//replace the "$(service_rate)"
		int serviceRate = NumericUtil.float2Int(mOrder.getServiceRate());
		mTemplate = mTemplate.replace(PVar.SERVICE_RATE, (serviceRate == 0 ? "" : "(" + serviceRate + "%�����" + ")"));					
		
		//replace the "$(waiter)"
		mTemplate = mTemplate.replace(PVar.WAITER_NAME, mWaiter);
		
		String tblInfo = mOrder.getDestTbl().getName().trim().length() == 0 ? Integer.toString(mOrder.getDestTbl().getAliasId()) : mOrder.getDestTbl().getName();
		//replace the "$(var_5)"
		mTemplate = mTemplate.replace(PVar.VAR_5, 
							new Grid2ItemsContent("��̨��" + tblInfo, 
												  "������" + mOrder.getCustomNum(), 
												  getStyle()).toString());
		
		
		//generate the order food list and replace the $(var_1) with the ordered foods
		mTemplate = mTemplate.replace(PVar.VAR_1, new FoodListContent(buildReciptFormat(), mOrder.getOrderFoods(), mPrintType, mStyle).toString());
		
		//replace the $(var_3) with the actual price
		mTemplate = mTemplate.replace(PVar.VAR_3, new RightAlignedDecorator("ʵ�ս�" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.getActualPrice()), mStyle).toString());
		
		//generate the comment and replace the $(var_3)
		if(mOrder.getComment().isEmpty()){
			mTemplate = mTemplate.replace(PVar.VAR_4, "");
		}else{
			mTemplate = mTemplate.replace(PVar.VAR_4, "��ע��" + mOrder.getComment());
		}
		
		return mTemplate;
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
		
		StringBuilder line1 = new StringBuilder();
		line1.append("Ӧ�գ�" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.calcPriceBeforeDiscount()))
			 .append("  ")
			 .append("���ͣ�" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.calcGiftPrice()));
	
		StringBuilder line2 = new StringBuilder();
		if(mOrder.isPayByCash() && !isTempReceipt && mOrder.getReceivedCash() != 0){
			float chargeMoney = NumericUtil.roundFloat(mOrder.getReceivedCash() - mOrder.getActualPrice());
			
			java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

			line2.append("���㣺" + NumericUtil.CURRENCY_SIGN + df.format(chargeMoney))
				 .append("  ")
				 .append("�տ" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.getReceivedCash()));
			
		}

		StringBuilder line3 = new StringBuilder();
		if(mOrder.calcDiscountPrice() != 0){
			line3.append("�ۿۣ�" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.calcDiscountPrice()));
		}
		
		if(mOrder.getErasePrice() > 0){
			if(line3.length() > 0){
				line3.append("  ");
			}
			line3.append("Ĩ����" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.getErasePrice()));
		}
		
		StringBuilder line4 = new StringBuilder();
		if(mOrder.getCouponPrice() > 0){
			line3.append("�Ż�ȯ��" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.getCouponPrice()));
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
	public DisplayConfig buildReciptFormat(){
		if((mReceiptStyle & Setting.RECEIPT_DEF) == Setting.RECEIPT_DEF){
			return FoodDetailContent.DISPLAY_CONFIG_4_RECEIPT;
			
		}else{
			DisplayConfig config = new DisplayConfig(FoodDetailContent.DISPLAY_CONFIG_4_RECEIPT);
			if((mReceiptStyle & Setting.RECEIPT_STATUS) == 0){
				config.mask(DisplayItem.STATUS);
			}
			if((mReceiptStyle & Setting.RECEIPT_AMOUNT) == 0){
				config.mask(DisplayItem.AMOUNT);
			}
			if((mReceiptStyle & Setting.RECEIPT_DISCOUNT) == 0){
				config.mask(DisplayItem.DISCOUNT);
			}
			return config;
		}
	}
	
}
