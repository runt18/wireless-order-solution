package com.wireless.print.content;

import com.wireless.db.shift.QueryShiftDao;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.print.PStyle;
import com.wireless.print.PVar;
import com.wireless.protocol.NumericUtil;
import com.wireless.protocol.Order;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Terminal;

public class ShiftContent extends ConcreteContent {

	private QueryShiftDao.Result _shiftDetail;
	private String _template;
	
	public ShiftContent(QueryShiftDao.Result shiftDetail, String template, Order order, Terminal term, int printType, int style) {
		super(order, term, printType, style);
		_shiftDetail = shiftDetail;
		_template = template;
	}

	@Override
	public String toString(){
		
		if(_printType == Reserved.PRINT_DAILY_SETTLE_RECEIPT || _printType == Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT){
			//replace the "$(title)" with "日结单"
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("日结单", _style).toString());
			
		}else{
			//replace the "$(title)" with "交班对账单"
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("交班对账单", _style).toString());	
		}
		
		//replace $(order_amount) 
		_template = _template.replace("$(order_amount)", Integer.toString(_shiftDetail.orderAmount));
		//replace $(waiter)
		_template = _template.replace(PVar.WAITER_NAME, _term.owner);
		//replace $(on_duty)
		_template = _template.replace("$(on_duty)", _shiftDetail.onDuty);
		//replace $(off_duty)
		_template = _template.replace("$(off_duty)", _shiftDetail.offDuty);
		
		int pos4Item[] = {8, 15, 24};
		int pos2Item = 19;
		if(_style == PStyle.PRINT_STYLE_58MM){
			pos4Item = new int[]{8, 15, 24};
			pos2Item = 19;
			
		}else if(_style == PStyle.PRINT_STYLE_80MM){
			pos4Item = new int[]{12, 24, 36};
			pos2Item = 26;
		}
		
		//generate the shift detail string
		StringBuffer var1 = new StringBuffer();
		var1.append(new Grid4ItemsContent(new String[]{"收款", "账单数", "金额", "实收"}, pos4Item, _printType, _style) + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"现金", 
				 		     Integer.toString(_shiftDetail.cashAmount), 
							 Float.toString(_shiftDetail.cashIncome), 
							 Float.toString(_shiftDetail.cashIncome2)
							}, 
				pos4Item, _printType, _style).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"刷卡", 
				 		     Integer.toString(_shiftDetail.creditCardAmount), 
							 Float.toString(_shiftDetail.creditCardIncome), 
							 Float.toString(_shiftDetail.creditCardIncome2)
							}, 
				pos4Item, _printType, _style).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"会员卡", 
							 Integer.toString(_shiftDetail.memeberCardAmount), 
							 Float.toString(_shiftDetail.memberCardIncome), 
							 Float.toString(_shiftDetail.memberCardIncome2)
							}, 
				pos4Item, _printType, _style).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"签单", 
							 Integer.toString(_shiftDetail.signAmount), 
							 Float.toString(_shiftDetail.signIncome), 
							 Float.toString(_shiftDetail.signIncome2)
							}, 
				pos4Item, _printType, _style).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"挂账", 
							 Integer.toString(_shiftDetail.hangAmount), 
							 Float.toString(_shiftDetail.hangIncome), 
							 Float.toString(_shiftDetail.hangIncome2)
							 }, 
				pos4Item, _printType, _style).toString());
		
		//replace the $(var_1) with the shift detail
		_template = _template.replace(PVar.VAR_1, var1);		
		
		StringBuffer var2 = new StringBuffer();
		var2.append(new Grid2ItemsContent("折扣金额：" + _shiftDetail.discountIncome, pos2Item, "账单数：" + _shiftDetail.discountAmount, _printType, _style) + "\r\n");
		var2.append(new Grid2ItemsContent("赠送金额：" + _shiftDetail.giftIncome, pos2Item, "账单数：" + _shiftDetail.giftAmount, _printType, _style) + "\r\n");
		var2.append(new Grid2ItemsContent("退菜金额：" + _shiftDetail.cancelIncome, pos2Item, "账单数：" + _shiftDetail.cancelAmount, _printType, _style) + "\r\n");
		var2.append(new Grid2ItemsContent("抹数金额：" + _shiftDetail.eraseIncome, pos2Item, "账单数：" + _shiftDetail.eraseAmount, _printType, _style) + "\r\n");
		var2.append(new Grid2ItemsContent("反结帐金额：" + _shiftDetail.paidIncome, pos2Item, "帐单数：" + _shiftDetail.paidAmount, _printType, _style) + "\r\n");
		var2.append(new Grid2ItemsContent("服务费金额：" + _shiftDetail.serviceIncome, pos2Item, "账单数：" + _shiftDetail.serviceAmount, _printType, _style));
		//replace the $(var_2) with the shift detail
		_template = _template.replace(PVar.VAR_2, var2);

		
		StringBuffer var3 = new StringBuffer();
		var3.append(new Grid4ItemsContent(new String[]{ "部门", "折扣", "赠送", "金额" }, pos4Item, _printType, _style).toString());
		for(IncomeByDept deptIncome : _shiftDetail.deptIncome){
			var3.append("\r\n");
			var3.append(new Grid4ItemsContent(
					new String[]{ deptIncome.getDept().name, 
								  Float.toString(deptIncome.getDiscount()),
								  Float.toString(deptIncome.getGift()),
								  Float.toString(deptIncome.getIncome())
								 }, 
					pos4Item, _printType, _style).toString());

		}
		//replace the $(var_3) with the shift detail
		_template = _template.replace(PVar.VAR_3, var3);
		
		//replace the $(var_4) with the shift detail
		_template = _template.replace(PVar.VAR_4, new RightAlignedDecorator("实收总额：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_shiftDetail.totalActual), _style).toString());
		
		return _template;
		
	}
	
}
