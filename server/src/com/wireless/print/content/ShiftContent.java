package com.wireless.print.content;

import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.PVar;
import com.wireless.server.WirelessSocketServer;

public class ShiftContent extends ConcreteContent {

	private final ShiftDetail _shiftDetail;
	private String _template;
	
	public ShiftContent(ShiftDetail shiftDetail, String waiter, PType printType, PStyle style) {
		super(null, waiter, printType, style);
		_shiftDetail = shiftDetail;
		_template = WirelessSocketServer.printTemplates.get(PType.PRINT_SHIFT_RECEIPT).get(style);
	}

	@Override
	public String toString(){
		
		if(mPrintType == PType.PRINT_DAILY_SETTLE_RECEIPT || mPrintType == PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT){
			//replace the "$(title)" with "日结单"
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("日结单", mStyle).toString());
			
		}else{
			//replace the "$(title)" with "交班对账单"
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("交班对账单", mStyle).toString());	
		}
		
		//replace $(order_amount) 
		_template = _template.replace("$(order_amount)", Integer.toString(_shiftDetail.getOrderAmount()));
		//replace $(waiter)
		_template = _template.replace(PVar.WAITER_NAME, _waiter);
		//replace $(on_duty)
		_template = _template.replace("$(on_duty)", _shiftDetail.getOnDuty());
		//replace $(off_duty)
		_template = _template.replace("$(off_duty)", _shiftDetail.getOffDuty());
		
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
		StringBuffer var1 = new StringBuffer();
		var1.append(new Grid4ItemsContent(new String[]{"收款", "账单数", "金额", "实收"}, pos4Item, mPrintType, mStyle) + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"现金", 
				 		     Integer.toString(_shiftDetail.getCashAmount()), 
							 Float.toString(_shiftDetail.getCashTotalIncome()), 
							 Float.toString(_shiftDetail.getCashActualIncome())
							}, 
				pos4Item, mPrintType, mStyle).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"刷卡", 
				 		     Integer.toString(_shiftDetail.getCreditCardAmount()), 
							 Float.toString(_shiftDetail.getCreditTotalIncome()), 
							 Float.toString(_shiftDetail.getCreditActualIncome())
							}, 
				pos4Item, mPrintType, mStyle).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"会员", 
							 Integer.toString(_shiftDetail.getMemberCardAmount()), 
							 Float.toString(_shiftDetail.getMemberTotalIncome()), 
							 Float.toString(_shiftDetail.getMemberActualIncome())
							}, 
				pos4Item, mPrintType, mStyle).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"签单", 
							 Integer.toString(_shiftDetail.getSignAmount()), 
							 Float.toString(_shiftDetail.getSignTotalIncome()), 
							 Float.toString(_shiftDetail.getSignActualIncome())
							}, 
				pos4Item, mPrintType, mStyle).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"挂账", 
							 Integer.toString(_shiftDetail.getHangAmount()), 
							 Float.toString(_shiftDetail.getHangTotalIncome()), 
							 Float.toString(_shiftDetail.getHangActualIncome())
							 }, 
				pos4Item, mPrintType, mStyle).toString());
		
		//replace the $(var_1) with the shift detail
		_template = _template.replace(PVar.VAR_1, var1);		
		
		StringBuffer var2 = new StringBuffer();
		var2.append(new Grid2ItemsContent("折扣金额：" + _shiftDetail.getDiscountIncome(), pos2Item, "账单数：" + _shiftDetail.getDiscountAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("赠送金额：" + _shiftDetail.getGiftIncome(), pos2Item, "账单数：" + _shiftDetail.getGiftAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("退菜金额：" + _shiftDetail.getCancelIncome(), pos2Item, "账单数：" + _shiftDetail.getCancelAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("抹数金额：" + _shiftDetail.getEraseIncome(), pos2Item, "账单数：" + _shiftDetail.getEraseAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("反结帐金额：" + _shiftDetail.getPaidIncome(), pos2Item, "帐单数：" + _shiftDetail.getPaidAmount(), getStyle()) + SEP);
		var2.append(new Grid2ItemsContent("服务费金额：" + _shiftDetail.getServiceIncome(), pos2Item, "账单数：" + _shiftDetail.getServiceAmount(), getStyle()) + SEP);
		var2.append(mSeperatorLine + "会员充值" + SEP);
		var2.append(new Grid2ItemsContent("现金：" + _shiftDetail.getChargeByCash(), pos2Item, "刷卡：" + _shiftDetail.getChargeByCreditCard(), getStyle()));
		//replace the $(var_2) with the shift detail
		_template = _template.replace(PVar.VAR_2, var2);

		
		StringBuffer var3 = new StringBuffer();
		var3.append(new Grid4ItemsContent(new String[]{ "部门", "折扣", "赠送", "金额" }, pos4Item, mPrintType, mStyle).toString());
		for(IncomeByDept deptIncome : _shiftDetail.getDeptIncome()){
			var3.append("\r\n");
			var3.append(new Grid4ItemsContent(
					new String[]{ deptIncome.getDept().getName(), 
								  Float.toString(deptIncome.getDiscount()),
								  Float.toString(deptIncome.getGift()),
								  Float.toString(deptIncome.getIncome())
								 }, 
					pos4Item, mPrintType, mStyle).toString());

		}
		//replace the $(var_3) with the shift detail
		_template = _template.replace(PVar.VAR_3, var3);
		
		//replace the $(var_4) with the shift detail
		_template = _template.replace(PVar.VAR_4, new RightAlignedDecorator("实收总额：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_shiftDetail.getTotalActual()), mStyle).toString());
		
		return _template;
		
	}
	
}
