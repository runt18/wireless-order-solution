package com.wireless.print.content.concrete;

import com.wireless.pojo.billStatistics.CouponUsage;
import com.wireless.pojo.billStatistics.IncomeByBook;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByPay.PaymentIncome;
import com.wireless.pojo.billStatistics.member.IncomeByCharge;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.PVar;
import com.wireless.print.content.decorator.CenterAlignedDecorator;
import com.wireless.print.content.decorator.ExtraFormatDecorator;
import com.wireless.print.content.decorator.RightAlignedDecorator;
import com.wireless.server.WirelessSocketServer;

public class ShiftContent extends ConcreteContent {

	private final ShiftDetail mShiftDetail;
	private String mTemplate;
	private final String mWaiter;
	
	public ShiftContent(ShiftDetail shiftDetail, String waiter, PType printType, PStyle style) {
		super(printType, style);
		mShiftDetail = shiftDetail;
		mTemplate = WirelessSocketServer.printTemplates.get(PType.PRINT_SHIFT_RECEIPT).get(style);
		mWaiter = waiter;
	}

	@Override
	public String toString(){
		
		if(mPrintType == PType.PRINT_DAILY_SETTLE_RECEIPT || mPrintType == PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT){
			//replace the "$(title)" with "日结单"
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(  
														new CenterAlignedDecorator("日结单", getStyle()).toString(), getStyle(), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
		}else if(mPrintType == PType.PRINT_SHIFT_RECEIPT || mPrintType == PType.PRINT_TEMP_SHIFT_RECEIPT || mPrintType == PType.PRINT_HISTORY_SHIFT_RECEIPT){
			//replace the "$(title)" with "交班对账单"
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(  
														new CenterAlignedDecorator("交班对账单", getStyle()).toString(), getStyle(), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
		}else if(mPrintType == PType.PRINT_PAYMENT_RECEIPT || mPrintType == PType.PRINT_HISTORY_PAYMENT_RECEIPT){
			//replace the "$(title)" with "交班对账单"
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(  
														new CenterAlignedDecorator("交款对账单", getStyle()).toString(), getStyle(), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
		}
		
		//replace $(order_amount) 
		mTemplate = mTemplate.replace("$(order_amount)", Integer.toString(mShiftDetail.getOrderAmount()));
		//replace $(waiter)
		mTemplate = mTemplate.replace(PVar.WAITER_NAME, mWaiter);
		//replace $(on_duty)
		mTemplate = mTemplate.replace("$(on_duty)", mShiftDetail.getOnDuty());
		//replace $(off_duty)
		mTemplate = mTemplate.replace("$(off_duty)", mShiftDetail.getOffDuty());
		
		int pos4Item[] = {8, 15, 24};
		int pos2Item = 19;
		if(mStyle == PStyle.PRINT_STYLE_58MM){
			pos4Item = new int[]{8, 15, 24};
			pos2Item = 19;
			
		}else if(mStyle == PStyle.PRINT_STYLE_80MM){
			pos4Item = new int[]{12, 24, 36};
			pos2Item = 26;
		}
		
		//generate the shift detail string
		StringBuilder var1 = new StringBuilder();
		var1.append(new Grid4ItemsContent(new String[]{"收款", "账单数", "金额", "实收"}, pos4Item, mPrintType, mStyle) + SEP);
		
		for(PaymentIncome paymentIncome : mShiftDetail.getIncomeByPay().getPaymentIncomes()){
			var1.append(new Grid4ItemsContent(
							new String[]{ 
								paymentIncome.getPayType().getName(), 
					 			Integer.toString(paymentIncome.getAmount()), 
							    Float.toString(paymentIncome.getTotal()), 
								Float.toString(paymentIncome.getActual())
							}, 
						pos4Item, mPrintType, mStyle).toString() + SEP);
		}
		
		
		//replace the $(var_1) with the shift detail
		mTemplate = mTemplate.replace(PVar.VAR_1, var1);		
		
		StringBuilder var2 = new StringBuilder();
		var2.append(new Grid2ItemsContent("折扣金额：" + mShiftDetail.getDiscountIncome(), pos2Item, "账单数：" + mShiftDetail.getDiscountAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("赠送金额：" + mShiftDetail.getGiftIncome(), pos2Item, "账单数：" + mShiftDetail.getGiftAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("退菜金额：" + mShiftDetail.getCancelIncome(), pos2Item, "账单数：" + mShiftDetail.getCancelAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("抹数金额：" + mShiftDetail.getEraseIncome(), pos2Item, "账单数：" + mShiftDetail.getEraseAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("反结帐金额：" + mShiftDetail.getPaidIncome(), pos2Item, "帐单数：" + mShiftDetail.getPaidAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("服务费金额：" + mShiftDetail.getServiceIncome(), pos2Item, "账单数：" + mShiftDetail.getServiceAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("优惠券金额：" + mShiftDetail.getCouponIncome(), pos2Item, "账单数：" + mShiftDetail.getCouponAmount(), getStyle()) + SEP);
		//var2.append(new Grid2ItemsContent("会员优惠金额：" + mShiftDetail.getMemberPriceIncome(), pos2Item, "账单数：" + mShiftDetail.getMemberPriceAmount(), getStyle()));
		mTemplate = mTemplate.replace(PVar.VAR_2, var2);

		
		if(mPrintType == PType.PRINT_PAYMENT_RECEIPT || mPrintType == PType.PRINT_HISTORY_PAYMENT_RECEIPT){
			mTemplate = mTemplate.replace(PVar.VAR_3, "");
		}else{
			StringBuilder var3 = new StringBuilder();
			var3.append(new Grid4ItemsContent(new String[]{ "部门", "折扣", "赠送", "金额" }, pos4Item, mPrintType, mStyle).toString());
			for(IncomeByDept deptIncome : mShiftDetail.getDeptIncome()){
				var3.append(SEP);
				var3.append(new Grid4ItemsContent(
						new String[]{ deptIncome.getDept().getName(), 
									  Float.toString(deptIncome.getDiscount()),
									  Float.toString(deptIncome.getGift()),
									  Float.toString(deptIncome.getIncome())
									 }, 
						pos4Item, mPrintType, mStyle).toString());

			}
			//replace the $(var_3) with the shift detail
			mTemplate = mTemplate.replace(PVar.VAR_3, var3);
		}

		
		//replace the $(var_4) with the shift detail
		mTemplate = mTemplate.replace(PVar.VAR_4, new ExtraFormatDecorator( 
													new RightAlignedDecorator("实收总额：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mShiftDetail.getTotalActual()), mStyle).toString(),
														mStyle, ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
		
		//replace the $(var_5) with the charge income
		IncomeByCharge chargeIncome = mShiftDetail.getIncomeByCharge();
		if(chargeIncome.getTotalAccountCharge() != 0 || chargeIncome.getTotalAccountRefund() != 0){
			StringBuilder chargeInfo = new StringBuilder();
			
			chargeInfo.append(mSeperatorLine).append(SEP);
			if(chargeIncome.getChargeAmount() > 0){
				chargeInfo.append(new CenterAlignedDecorator("会员充值(" + chargeIncome.getChargeAmount() + "次)", getStyle()).toString()).append(SEP);
			}else{
				chargeInfo.append(new CenterAlignedDecorator("会员充值", getStyle()).toString()).append(SEP);
			}
			chargeInfo.append(new Grid2ItemsContent("实收金额：" + chargeIncome.getTotalActualCharge(), "账户充额：" + chargeIncome.getTotalAccountCharge(), getStyle())).append(SEP);
			chargeInfo.append("现金实收：" + chargeIncome.getActualCashCharge()).append(SEP);
			chargeInfo.append("刷卡实收：" + chargeIncome.getActualCreditCardCharge()).append(SEP);
			chargeInfo.append(SEP);
			if(chargeIncome.getRefundAmount() > 0){
				chargeInfo.append(new CenterAlignedDecorator("会员退款(" + chargeIncome.getRefundAmount() + "次)", getStyle()).toString()).append(SEP);
			}else{
				chargeInfo.append(new CenterAlignedDecorator("会员退款", getStyle()).toString()).append(SEP);
			}
			chargeInfo.append(new Grid2ItemsContent("实退金额：" + chargeIncome.getTotalActualRefund(), "账户扣额：" + chargeIncome.getTotalAccountRefund(), getStyle())).append(SEP);
			
			mTemplate = mTemplate.replace(PVar.VAR_5, chargeInfo.toString());
		}else{
			mTemplate = mTemplate.replace(PVar.VAR_5, "");
		}
		
		//replace the $(var_6) with the book income
		IncomeByBook bookIncome = mShiftDetail.getIncomeByBook();
		if(bookIncome.getIncome() > 0){
			StringBuilder bookInfo = new StringBuilder();
			bookInfo.append(mSeperatorLine);
			bookInfo.append("预订金额：" + bookIncome.getIncome()).append(SEP);
			mTemplate = mTemplate.replace(PVar.VAR_6, bookInfo.toString());
		}else{
			mTemplate = mTemplate.replace(PVar.VAR_6, "");
		}
		
		//replace the $(var_7) with the coupon usage
		if(mShiftDetail.hasCouponUsage()){
			StringBuilder couponUsage = new StringBuilder();
			couponUsage.append(mSeperatorLine).append(SEP);
			couponUsage.append(new CenterAlignedDecorator("优惠券统计", mStyle)).append(SEP).append(SEP);
			for(CouponUsage.Usage used : mShiftDetail.getCouponUsage().getUsed()){
				couponUsage.append("用券【" + used.getName() + "】" + used.getAmount() + "张, 共￥" + used.getPrice()).append(SEP);
			}
			for(CouponUsage.Usage issue : mShiftDetail.getCouponUsage().getIssued()){
				couponUsage.append("发券【" + issue.getName() + "】" + issue.getAmount() + "张, 共￥" + issue.getPrice()).append(SEP);
			}
			mTemplate = mTemplate.replace(PVar.VAR_7, couponUsage);
		}else{
			mTemplate = mTemplate.replace(PVar.VAR_7, "");
		}
		return mTemplate;
		
	}
	
}
