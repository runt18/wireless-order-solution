package com.wireless.print.content.concrete;

import com.wireless.pojo.billStatistics.IncomeByCharge;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByPay.PaymentIncome;
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
			//replace the "$(title)" with "�սᵥ"
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(  
														new CenterAlignedDecorator("�սᵥ", getStyle()).toString(), getStyle(), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
		}else if(mPrintType == PType.PRINT_SHIFT_RECEIPT || mPrintType == PType.PRINT_TEMP_SHIFT_RECEIPT || mPrintType == PType.PRINT_HISTORY_SHIFT_RECEIPT){
			//replace the "$(title)" with "������˵�"
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(  
														new CenterAlignedDecorator("������˵�", getStyle()).toString(), getStyle(), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
		}else if(mPrintType == PType.PRINT_PAYMENT_RECEIPT || mPrintType == PType.PRINT_HISTORY_PAYMENT_RECEIPT){
			//replace the "$(title)" with "������˵�"
			mTemplate = mTemplate.replace(PVar.TITLE, new ExtraFormatDecorator(  
														new CenterAlignedDecorator("������˵�", getStyle()).toString(), getStyle(), ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
			
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
		var1.append(new Grid4ItemsContent(new String[]{"�տ�", "�˵���", "���", "ʵ��"}, pos4Item, mPrintType, mStyle) + SEP);
		
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
		var2.append(new Grid2ItemsContent("�ۿ۽�" + mShiftDetail.getDiscountIncome(), pos2Item, "�˵�����" + mShiftDetail.getDiscountAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("���ͽ�" + mShiftDetail.getGiftIncome(), pos2Item, "�˵�����" + mShiftDetail.getGiftAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("�˲˽�" + mShiftDetail.getCancelIncome(), pos2Item, "�˵�����" + mShiftDetail.getCancelAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("Ĩ����" + mShiftDetail.getEraseIncome(), pos2Item, "�˵�����" + mShiftDetail.getEraseAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("�����ʽ�" + mShiftDetail.getPaidIncome(), pos2Item, "�ʵ�����" + mShiftDetail.getPaidAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("����ѽ�" + mShiftDetail.getServiceIncome(), pos2Item, "�˵�����" + mShiftDetail.getServiceAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("�Ż�ȯ��" + mShiftDetail.getCouponIncome(), pos2Item, "�˵�����" + mShiftDetail.getCouponAmount(), getStyle()) + SEP);
		mTemplate = mTemplate.replace(PVar.VAR_2, var2);

		
		if(mPrintType == PType.PRINT_PAYMENT_RECEIPT || mPrintType == PType.PRINT_HISTORY_PAYMENT_RECEIPT){
			mTemplate = mTemplate.replace(PVar.VAR_3, "");
		}else{
			StringBuilder var3 = new StringBuilder();
			var3.append(new Grid4ItemsContent(new String[]{ "����", "�ۿ�", "����", "���" }, pos4Item, mPrintType, mStyle).toString());
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
													new RightAlignedDecorator("ʵ���ܶ" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(mShiftDetail.getTotalActual()), mStyle).toString(),
														mStyle, ExtraFormatDecorator.LARGE_FONT_V_1X).toString());
		
		IncomeByCharge incomeByCharge = mShiftDetail.getIncomeByCharge();
		if(incomeByCharge.getTotalAccountCharge() != 0 || incomeByCharge.getTotalAccountRefund() != 0){
			StringBuilder chargeStat = new StringBuilder();
			
			chargeStat.append(mSeperatorLine);
			if(incomeByCharge.getChargeAmount() > 0){
				chargeStat.append(new CenterAlignedDecorator("��Ա��ֵ(" + incomeByCharge.getChargeAmount() + "��)", getStyle()).toString()).append(SEP);
			}else{
				chargeStat.append(new CenterAlignedDecorator("��Ա��ֵ", getStyle()).toString()).append(SEP);
			}
			chargeStat.append(new Grid2ItemsContent("ʵ�ս�" + incomeByCharge.getTotalActualCharge(), "�˻���" + incomeByCharge.getTotalAccountCharge(), getStyle())).append(SEP);
			chargeStat.append("�ֽ�ʵ�գ�" + incomeByCharge.getActualCashCharge()).append(SEP);
			chargeStat.append("ˢ��ʵ�գ�" + incomeByCharge.getActualCreditCardCharge()).append(SEP);
			chargeStat.append(SEP);
			if(incomeByCharge.getRefundAmount() > 0){
				chargeStat.append(new CenterAlignedDecorator("��Ա�˿�(" + incomeByCharge.getRefundAmount() + "��)", getStyle()).toString()).append(SEP);
			}else{
				chargeStat.append(new CenterAlignedDecorator("��Ա�˿�", getStyle()).toString()).append(SEP);
			}
			chargeStat.append(new Grid2ItemsContent("ʵ�˽�" + incomeByCharge.getTotalActualRefund(), "�˻��۶" + incomeByCharge.getTotalAccountRefund(), getStyle())).append(SEP);
			
			mTemplate = mTemplate.replace(PVar.VAR_5, chargeStat.toString());
		}else{
			mTemplate = mTemplate.replace(PVar.VAR_5, "");
		}
		
		return mTemplate;
		
	}
	
}