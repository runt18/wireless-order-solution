package com.wireless.print.content;

import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.PVar;
import com.wireless.server.WirelessSocketServer;
import com.wireless.util.NumericUtil;

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
			//replace the "$(title)" with "�սᵥ"
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("�սᵥ", mStyle).toString());
			
		}else{
			//replace the "$(title)" with "������˵�"
			_template = _template.replace(PVar.TITLE, new CenterAlignedDecorator("������˵�", mStyle).toString());	
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
		var1.append(new Grid4ItemsContent(new String[]{"�տ�", "�˵���", "���", "ʵ��"}, pos4Item, mPrintType, mStyle) + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"�ֽ�", 
				 		     Integer.toString(_shiftDetail.getCashAmount()), 
							 Float.toString(_shiftDetail.getCashTotalIncome()), 
							 Float.toString(_shiftDetail.getCashActualIncome())
							}, 
				pos4Item, mPrintType, mStyle).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"ˢ��", 
				 		     Integer.toString(_shiftDetail.getCreditCardAmount()), 
							 Float.toString(_shiftDetail.getCreditTotalIncome()), 
							 Float.toString(_shiftDetail.getCreditActualIncome())
							}, 
				pos4Item, mPrintType, mStyle).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"��Ա��", 
							 Integer.toString(_shiftDetail.getMemberCardAmount()), 
							 Float.toString(_shiftDetail.getMemberTotalIncome()), 
							 Float.toString(_shiftDetail.getMemberActualIncome())
							}, 
				pos4Item, mPrintType, mStyle).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"ǩ��", 
							 Integer.toString(_shiftDetail.getSignAmount()), 
							 Float.toString(_shiftDetail.getSignTotalIncome()), 
							 Float.toString(_shiftDetail.getSignActualIncome())
							}, 
				pos4Item, mPrintType, mStyle).toString() + "\r\n");
		
		var1.append(new Grid4ItemsContent(
				new String[]{"����", 
							 Integer.toString(_shiftDetail.getHangAmount()), 
							 Float.toString(_shiftDetail.getHangTotalIncome()), 
							 Float.toString(_shiftDetail.getHangActualIncome())
							 }, 
				pos4Item, mPrintType, mStyle).toString());
		
		//replace the $(var_1) with the shift detail
		_template = _template.replace(PVar.VAR_1, var1);		
		
		StringBuffer var2 = new StringBuffer();
		var2.append(new Grid2ItemsContent("�ۿ۽�" + _shiftDetail.getDiscountIncome(), pos2Item, "�˵�����" + _shiftDetail.getDiscountAmount(), getStyle()) + "\r\n");
		var2.append(new Grid2ItemsContent("���ͽ�" + _shiftDetail.getGiftIncome(), pos2Item, "�˵�����" + _shiftDetail.getGiftAmount(), getStyle()) + "\r\n");
		var2.append(new Grid2ItemsContent("�˲˽�" + _shiftDetail.getCancelIncome(), pos2Item, "�˵�����" + _shiftDetail.getCancelAmount(), getStyle()) + "\r\n");
		var2.append(new Grid2ItemsContent("Ĩ����" + _shiftDetail.getEraseIncome(), pos2Item, "�˵�����" + _shiftDetail.getEraseAmount(), getStyle()) + "\r\n");
		var2.append(new Grid2ItemsContent("�����ʽ�" + _shiftDetail.getPaidIncome(), pos2Item, "�ʵ�����" + _shiftDetail.getPaidAmount(), getStyle()) + "\r\n");
		var2.append(new Grid2ItemsContent("����ѽ�" + _shiftDetail.getServiceIncome(), pos2Item, "�˵�����" + _shiftDetail.getServiceAmount(), getStyle()));
		//replace the $(var_2) with the shift detail
		_template = _template.replace(PVar.VAR_2, var2);

		
		StringBuffer var3 = new StringBuffer();
		var3.append(new Grid4ItemsContent(new String[]{ "����", "�ۿ�", "����", "���" }, pos4Item, mPrintType, mStyle).toString());
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
		_template = _template.replace(PVar.VAR_4, new RightAlignedDecorator("ʵ���ܶ" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(_shiftDetail.getTotalActual()), mStyle).toString());
		
		return _template;
		
	}
	
}
