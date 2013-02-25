package com.wireless.ui.main;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.terminal.WirelessOrder;
import com.wireless.ui.neworder.ChangeOrderScreen;

class UpdateOrderPopup extends PopupScreen implements FieldChangeListener, IPostQueryOrder {
	private ButtonField _okBtn;
	private ButtonField _cancelBtn;
	private EditField _tableIDEdt;
	private UpdateOrderPopup _self = this;

	UpdateOrderPopup() {
		super(new VerticalFieldManager());
		add(new LabelField("输入需要改单的台号", LabelField.USE_ALL_WIDTH
				| DrawStyle.LEFT));
		add(new SeparatorField());
		_tableIDEdt = new EditField("台号：", "", 5, TextField.NO_NEWLINE
				| TextField.NO_LEARNING | EditField.FILTER_NUMERIC);
		add(_tableIDEdt);
		add(new SeparatorField());
		_okBtn = new ButtonField("确定", ButtonField.CONSUME_CLICK);
		_okBtn.setChangeListener(this);
		_cancelBtn = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		_cancelBtn.setChangeListener(this);
		HorizontalFieldManager _hfm = new HorizontalFieldManager(
				Manager.FIELD_HCENTER);
		_hfm.add(_okBtn);
		_hfm.add(_cancelBtn);
		add(_hfm);
	}

	public void fieldChanged(Field field, int context) {
		if (field == _okBtn) {
			execute();
		} else if (field == _cancelBtn) {
			close();
		}
	}

	protected boolean keyChar(char c, int status, int time) {
		if (c == Characters.ESCAPE) {
			close();
			return true;
		} else if (c == Characters.ENTER) {
			execute();
			return true;
		} else {
			return super.keyChar(c, status, time);
		}
	}

	private void execute() {
		if (_tableIDEdt.getText().equals("")) {
			Dialog.alert("请输入需要改单的台号");
			_tableIDEdt.setFocus();
			return;
		}

		close();
		// Send command to get the bill according to the table ID
		// and then open the change order screen
		UiApplication.getUiApplication().pushScreen( new QueryOrderPopup(Integer.parseInt(_tableIDEdt.getText()),
																		 Type.QUERY_ORDER_BY_TBL, _self));

	}

	public void postQueryOrder(ProtocolPackage response) {
		if (response.header.type == Type.ACK) {
			//Order _order = RespQueryOrderParser.parse(response, WirelessOrder.foodMenu);
			Order order = new Order();
			order.createFromParcel(new Parcel(response.body));
			
			OrderFood[] foods = order.getOrderFoods();
			for(int i = 0; i < foods.length; i++){
				for(int j = 0; j < WirelessOrder.foodMenu.foods.length; j++){
					if(foods[i].getAliasId() == WirelessOrder.foodMenu.foods[j].getAliasId()){
						foods[i].copyFrom(WirelessOrder.foodMenu.foods[j]);
						break;
					}
				}

				if(foods[i].getTasteGroup().hasNormalTaste()){
					Taste[] normal = foods[i].getTasteGroup().getNormalTastes();
					for(int j = 0; j < normal.length; j++){
						for(int k = 0; k < WirelessOrder.foodMenu.tastes.length; k++){
							if(normal[j].equals(WirelessOrder.foodMenu.tastes[k])){
								normal[j].copyFrom(WirelessOrder.foodMenu.tastes[k]);
								continue;
							}
						}
						
						for(int k = 0; k < WirelessOrder.foodMenu.specs.length; k++){
							if(normal[j].equals(WirelessOrder.foodMenu.specs[k])){
								normal[j].copyFrom(WirelessOrder.foodMenu.specs[k]);
								continue;
							}
						}
						
						for(int k = 0; k < WirelessOrder.foodMenu.styles.length; k++){
							if(normal[j].equals(WirelessOrder.foodMenu.styles[k])){
								normal[j].copyFrom(WirelessOrder.foodMenu.styles[k]);
								continue;
							}
						}
					}
				}
			}
			
			UiApplication.getUiApplication().pushScreen(new ChangeOrderScreen(order));
		} else {
			if (response.header.reserved == ErrorCode.TABLE_IDLE) {
				Dialog.alert(_tableIDEdt + "号台还未下单");
			} else if (response.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
				Dialog.alert(_tableIDEdt + "号台信息不存在");
			} else if (response.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
				Dialog.alert("终端没有登记到餐厅，请联系管理人员。");
			} else if (response.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
				Dialog.alert("终端已过期，请联系管理人员。");
			} else {
				Dialog.alert("未确定的异常错误(" + response.header.reserved + ")");
			}
		}
	}
}
