package com.wireless.print.content.concrete;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.print.PVar;
import com.wireless.print.content.concrete.FoodDetailContent.DisplayConfig;
import com.wireless.print.content.concrete.FoodDetailContent.DisplayItem;
import com.wireless.print.content.decorator.CenterAlignedDecorator;
import com.wireless.print.content.decorator.ExtraFormatDecorator;
import com.wireless.print.content.decorator.RightAlignedDecorator;
import com.wireless.server.WirelessSocketServer;

public class ReceiptContent extends ConcreteContent {

	private final Restaurant mRestaurant;
	private final int mReceiptStyle;
	private String mTemplate;
	private final String mWaiter;
	private final Order mOrder;
	private final WxRestaurant mWxRestaurant;
	private String ending;
	private Member member;
	
	public ReceiptContent(int receiptStyle, Restaurant restaurant, WxRestaurant wxRestaurant, Order order, String waiter, PType printType, PStyle style) {
		super(printType, style);
		mTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_RECEIPT).get(style);
		mRestaurant = restaurant;
		mWxRestaurant = wxRestaurant;
		mReceiptStyle = receiptStyle;
		mWaiter = waiter;
		mOrder = order;
	}

	public ReceiptContent setEnding(String ending){
		this.ending = ending;
		return this;
	}
	
	public ReceiptContent setMember(Member member){
		this.member = member;
		return this;
	}
	
	private boolean hasEnding(){
		return ending != null && ending.trim().length() != 0;
	}
	
	@Override 
	public String toString(){
		if(mPrintType == PType.PRINT_RECEIPT){
			//generate the title and replace the "$(title)" with it
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(new CenterAlignedDecorator(mOrder.isRepaid() ? "反结帐单" : "结帐单", mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			//replace the $(restaurant)
			mTemplate = mTemplate.replace(PVar.RESTAURANT, new ExtraFormatDecorator(new CenterAlignedDecorator(mRestaurant.getName(), mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			//generate the total price string and replace the $(var_2) with this string
			mTemplate = mTemplate.replace(PVar.VAR_2, buildTotalPrice(false));
			
		}else if(mPrintType == PType.PRINT_TEMP_RECEIPT){
			//generate the title and replace the "$(title)" with it
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(new CenterAlignedDecorator("暂结单", mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			//replace the $(restaurant)
			mTemplate = mTemplate.replace(PVar.RESTAURANT, new ExtraFormatDecorator(new CenterAlignedDecorator(mRestaurant.getName(), mStyle), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			//generate the total price string and replace the $(var_2) with this string
			mTemplate = mTemplate.replace(PVar.VAR_2, buildTotalPrice(true));						
		}
		
		//replace the "$(order_id)"
		mTemplate = mTemplate.replace(PVar.ORDER_ID, Integer.toString(mOrder.getId()));
		
		//replace the "$(print_date)"
		mTemplate = mTemplate.replace(PVar.PRINT_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
		
		//replace the "$(pay_manner)"
		mTemplate = mTemplate.replace(PVar.PAY_MANNER, "(" + mOrder.getPaymentType().getName() + ")" + (mOrder.hasWxOrder() ? "(微信下单)" : ""));
		
		//replace the "$(order_cate)"
		mTemplate = mTemplate.replace(PVar.ORDER_CATE, "");
		
		//replace the "$(seq_id)"
		mTemplate = mTemplate.replace(PVar.SEQ_ID, Integer.toString(mOrder.getSeqId()));
		
		//replace the "$(service_rate)"
		int serviceRate = NumericUtil.float2Int(mOrder.getServiceRate());
		mTemplate = mTemplate.replace(PVar.SERVICE_RATE, (serviceRate == 0 ? "" : "(" + serviceRate + "%服务费" + ")"));					
		
		//replace the "$(waiter)"
		mTemplate = mTemplate.replace(PVar.WAITER_NAME, mWaiter);
		
		//replace the "$(member_name)"
		if(member != null){
			mTemplate = mTemplate.replace(PVar.MEMBER_NAME, SEP + "会员：" + member.getName());
		}else{
			mTemplate = mTemplate.replace(PVar.MEMBER_NAME, "");
		}
		
		final String tblName;
		if(mOrder.getDestTbl().getAliasId() == 0){
			tblName = mOrder.getDestTbl().getName();
		}else if(mOrder.getDestTbl().getName().isEmpty()){
			tblName = Integer.toString(mOrder.getDestTbl().getAliasId());
		}else{
			tblName = mOrder.getDestTbl().getAliasId() + "(" + mOrder.getDestTbl().getName() + ")";
		}
		
		//replace the "$(var_5)"
		mTemplate = mTemplate.replace(PVar.VAR_5, 
						new ExtraFormatDecorator(
							new Grid2ItemsContent("餐台：" + tblName, 
												  "人数：" + mOrder.getCustomNum(), 
												  getStyle()), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
		
		
		//generate the order food list and replace the $(var_1) with the ordered foods
		mTemplate = mTemplate.replace(PVar.VAR_1, new FoodListContent(buildReciptFormat(), mOrder.getOrderFoods(), mPrintType, mStyle, FoodDetailContent.DetailType.TOTAL).toString());
		
		//replace the $(var_3) with the actual price
		StringBuilder var3 = new StringBuilder();
		final float pureTotal = mOrder.calcPureTotalPrice();
		final float actualTotal = mOrder.getActualPrice();
		if(mPrintType == PType.PRINT_TEMP_RECEIPT && mOrder.hasMember() && actualTotal < pureTotal){
			var3.append(new ExtraFormatDecorator(
							(new String(new char[]{0x1B, 0x61, 0x02}) + "会员价:" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(actualTotal)), mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X).toString())
				.append(SEP).append(new char[]{0x1B, 0x61, 0x00})
				.append(SEP)
				.append(new RightAlignedDecorator("原价:" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(pureTotal) + ", 节省" + NumericUtil.float2String2(pureTotal - actualTotal) + "元", mStyle));
			
		}else if(mPrintType == PType.PRINT_RECEIPT && mOrder.hasMember() && actualTotal < pureTotal){
			var3.append(new ExtraFormatDecorator(
					(new String(new char[]{0x1B, 0x61, 0x02}) + "实收(" + mOrder.getPaymentType().getName() + "):" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(actualTotal)),
					mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X).toString())
				.append(SEP).append(new char[]{0x1B, 0x61, 0x00})
				.append(SEP)
				.append(new RightAlignedDecorator("原价:" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(pureTotal) + ", 节省" + NumericUtil.float2String2(pureTotal - actualTotal) + "元", mStyle));
			
		}else{
			var3.append(new ExtraFormatDecorator(
					(new String(new char[]{0x1B, 0x61, 0x02}) + "实收(" + mOrder.getPaymentType().getName() + "):" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(actualTotal)),
					mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X).toString())
				.append(SEP).append(new char[]{0x1B, 0x61, 0x00});
		}
		

		
		if(mOrder.getPaymentType().isMixed()){
			StringBuilder mixedDetail = new StringBuilder();
			for(Entry<PayType, Float> entry : mOrder.getMixedPayment().getPayments().entrySet()){
				if(mixedDetail.length() == 0){
					mixedDetail.append(entry.getKey().getName() + "：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(entry.getValue()));
				}else{
					mixedDetail.append(" ").append(entry.getKey().getName() + "：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(entry.getValue()));
				}
			}
			var3.append(SEP + new RightAlignedDecorator(mixedDetail.toString(), mStyle));
		}
		mTemplate = mTemplate.replace(PVar.VAR_3, var3.toString());
		
		//generate the comment and replace the $(var_3)
		if(mOrder.getComment().isEmpty()){
			mTemplate = mTemplate.replace(PVar.RECEIPT_COMMENT, "");
		}else{
			mTemplate = mTemplate.replace(PVar.RECEIPT_COMMENT, new RightAlignedDecorator("备注：" + mOrder.getComment(), getStyle()).toString());
		}
		
		if(getPrintType() == PType.PRINT_TEMP_RECEIPT && mWxRestaurant.hasQrCode() && mWxRestaurant.getQrCodeStatus().isNormal() && getStyle() == PStyle.PRINT_STYLE_80MM){
			final String qrCodeContent = mWxRestaurant.getQrCode() + "?" + mOrder.getId();
			mTemplate = mTemplate.replace(PVar.RECEIPT_ENDING, new String(new char[]{0x1B, 0x61, 0x01}) + new QRCodeContent(getPrintType(), getStyle(), qrCodeContent) + new String(new char[]{0x1B, 0x61, 0x00}) +
																		  SEP +
																		  new CenterAlignedDecorator(hasEnding() ? ending : "微信扫一扫", mStyle).toString());

		}else{
			if(hasEnding()){
				mTemplate = mTemplate.replace(PVar.RECEIPT_ENDING, ending);
			}else{
				final StringBuilder receiptEnding = new StringBuilder();
				if(!mRestaurant.getAddress().isEmpty()){
					receiptEnding.append(new CenterAlignedDecorator(mRestaurant.getAddress(), mStyle).toString());
				}
				if(!mRestaurant.getTele1().isEmpty()){
					if(receiptEnding.length() != 0){
						receiptEnding.append(SEP);
					}
					receiptEnding.append(new CenterAlignedDecorator(mRestaurant.getTele1(), mStyle).toString());
				}
				
				if(receiptEnding.length() != 0){
					mTemplate = mTemplate.replace(PVar.RECEIPT_ENDING, receiptEnding.toString());
				}else{
					mTemplate = mTemplate.replace(PVar.RECEIPT_ENDING, new CenterAlignedDecorator("欢迎您再次光临", mStyle).toString());
				}
			}
		}		
		
		if(mPrintType == PType.PRINT_RECEIPT){
			return mTemplate + EJECT;
		}else{
			return mTemplate;
		}
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
		if(mOrder.getPaymentType().isCash() && !isTempReceipt && mOrder.getReceivedCash() != 0){
			float chargeMoney = NumericUtil.roundFloat(mOrder.getReceivedCash() - mOrder.getActualPrice());
			
			line2.append("找零：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(chargeMoney))
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
		if(mOrder.hasCoupon()){
			if(line3.length() > 0){
				line3.append("  ");
			}
			line3.append(mOrder.getCoupon().getName() + "：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mOrder.getCouponPrice()));
		}
		
		StringBuilder line4 = new StringBuilder();
		
		String var = new RightAlignedDecorator(line1.toString(), mStyle).toString() +
					 (line2.length() != 0 ? SEP + new RightAlignedDecorator(line2.toString(), mStyle) : "").toString() +
					 (line3.length() != 0 ? SEP + new RightAlignedDecorator(line3.toString(), mStyle) : "").toString() +
					 (line4.length() != 0 ? SEP + new RightAlignedDecorator(line4.toString(), mStyle) : "").toString();
		
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
