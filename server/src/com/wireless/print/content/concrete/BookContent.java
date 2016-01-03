package com.wireless.print.content.concrete;

import java.text.SimpleDateFormat;

import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.content.decorator.CenterAlignedDecorator;
import com.wireless.print.content.decorator.ExtraFormatDecorator;

public class BookContent extends ConcreteContent {

	private final Book book;
	
	public BookContent(Book wxOrder, PStyle style) {
		super(PType.PRINT_BOOK, style);
		this.book = wxOrder;
	}

	@Override
	public String toString(){
		final StringBuilder sb = new StringBuilder();
		
		sb.append(new ExtraFormatDecorator(
				new CenterAlignedDecorator(book.getSource() == Book.Source.WEIXIN ? "΢��Ԥ��" : "Ԥ��", getStyle()), ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
		sb.append(mSeperatorLine);

		sb.append(new ExtraFormatDecorator(
				"Ԥ��ʱ�䣺" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(book.getBookDate()), mStyle,
				ExtraFormatDecorator.LARGE_FONT_V_1X));
		
		sb.append(SEP);

		final StringBuilder bookTable = new StringBuilder();
		if(book.getRegion().isEmpty()){
			for(Table table : book.getTables()){
				if(bookTable.length() > 0){
					bookTable.append(",");
				}
				bookTable.append(table.getName());
			}
		}else{
			bookTable.append(book.getRegion());
		}
		
		sb.append(new ExtraFormatDecorator(
				"Ԥ����̨��" + bookTable, mStyle,
				ExtraFormatDecorator.LARGE_FONT_V_1X));
		
		sb.append(SEP).append(SEP);

		sb.append(new ExtraFormatDecorator(
					new Grid2ItemsContent(
						"Ԥ���ˣ�" + book.getMember(), 
						"Ԥ���绰��" + book.getTele(), 
						mStyle).toString(),
				    mStyle,
				    ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
		
		sb.append(SEP);

		sb.append(new ExtraFormatDecorator(
				new Grid2ItemsContent(
					"Ԥ��������" + book.getAmount() + "��", 
					"Ԥ����" + NumericUtil.float2String2(book.getMoney()),
					mStyle).toString(),
			    mStyle,
			    ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
	
		sb.append(SEP);

		if(book.hasStaff()){
			sb.append(new ExtraFormatDecorator(
					"�����ˣ�" + book.getStaff().getName(), mStyle,
				    ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
		
			sb.append(SEP);
		}
		
		if(book.hasComment()){
			sb.append(new ExtraFormatDecorator("��ע��" + book.getComment(), mStyle, ExtraFormatDecorator.LARGE_FONT_VH_1X)).append(SEP);
		}
		
		if(book.hasOrder()){
			sb.append(mSeperatorLine);
			for(OrderFood of: book.getOrder().getOrderFoods()){
				sb.append(new ExtraFormatDecorator(
							new Grid2ItemsContent(of.getName(), NumericUtil.float2String2(of.getCount()), mStyle).toString(),
							mStyle,
							ExtraFormatDecorator.LARGE_FONT_V_1X)).append(SEP);
			}
		}
		
		sb.append(SEP).append(SEP).append(SEP).append(SEP).append(SEP).append(CUT);

		return sb.toString();
	}
}
