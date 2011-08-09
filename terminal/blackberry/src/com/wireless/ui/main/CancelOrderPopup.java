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

import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.Type;


class CancelOrderPopup extends PopupScreen implements FieldChangeListener, IPostQueryOrder {
	private ButtonField _ok;
	private ButtonField _cancel;
	private EditField _tableID;
	private CancelOrderPopup _self = this;

	CancelOrderPopup() {
		super(new VerticalFieldManager());
		add(new LabelField("输入需要删单的台号", LabelField.USE_ALL_WIDTH | DrawStyle.LEFT));
		add(new SeparatorField());
		_tableID = new EditField("台号：", "", 4, TextField.NO_NEWLINE | TextField.NO_LEARNING | EditField.FILTER_NUMERIC);
		add(_tableID);
		add(new SeparatorField());
		_ok = new ButtonField("确定", ButtonField.CONSUME_CLICK);
		_ok.setChangeListener(this);
		_cancel = new ButtonField("取消", ButtonField.CONSUME_CLICK);
		_cancel.setChangeListener(this);
		HorizontalFieldManager _hfm = new HorizontalFieldManager(Manager.FIELD_HCENTER);
		_hfm.add(_ok);
		_hfm.add(_cancel);
		add(_hfm);
	}

	public void fieldChanged(Field field, int context) {
		if (field == _ok) {
			execute();
		} else if (field == _cancel) {
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
		if (_tableID.getText().equals("")) {
			Dialog.alert("请输入需要删单的台号");
			_tableID.setFocus();
			return;
		} else {
			close();
			UiApplication.getUiApplication().pushScreen(
					new QueryOrderPopup(Short.parseShort(_tableID.getText()),
							Type.QUERY_ORDER_2, _self));
		}
	}

	public void postQueryOrder(ProtocolPackage response) {
		if (response.header.type == Type.ACK) {
			UiApplication.getUiApplication().pushScreen(new CancelOrderPopup2(Short.parseShort(_tableID.getText())));
		} else {
			if (response.header.reserved == ErrorCode.TABLE_IDLE) {
				Dialog.alert(_tableID + "号台还未下单");
			} else if (response.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
				Dialog.alert(_tableID + "号台信息不存在");
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