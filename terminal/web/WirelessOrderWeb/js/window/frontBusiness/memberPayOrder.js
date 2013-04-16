var mpo_memberDetailData;
var mpo_orderFoodGrid;
var mpo_memberPayOrderSreachMemberCardWin;
var mpo_memberPayOrderRechargeWin;
Ext.onReady(function(){
	var contetntDiv = document.getElementById('divMemberPayOrderContent');
	if(orderID == null){
		contetntDiv.innerHTML = '操作失败, 获取账单信息失败, 请联系客服人员.';
		return false;
	}
	var pe = contetntDiv.parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var mpo_memeberCardAliasID = {
		xtype : 'numberfield',
		id : 'mpo_numMemberCardAliasForPayOrder',
		inputType : 'password',
		fieldLabel : '请刷卡' + Ext.ux.txtFormat.xh,
		disabled : false,
		style : 'font-weight: bold; color: #FF0000;',
		maxLength : 10,
		maxLengthText : '请输入10位会员卡号',
		minLength : 10,
		minLengthText : '请输入10位会员卡号',
		width : 100,
		allowBlank : false,
		blankText : '会员卡不能为空, 请刷卡.',
		disabled : false,
		listeners : {
			render : function(e){
				
			}
		}
	};
	
	mpo_orderFoodGrid = createGridPanel(
		'mpo_orderFoodGrid',
		'账单列表',
		'',
		'',
		'',
		[
			[true, false, false, false], 
			['菜品', 'displayFoodName',230],
			['口味', 'tastePref',230],
			['数量', 'count',,'right', 'Ext.ux.txtFormat.gridDou'],
			['原总价', 'totalPriceBeforeDiscount',,'right', 'Ext.ux.txtFormat.gridDou'],
			['折扣率', 'discount',,'right', 'Ext.ux.txtFormat.gridDou'],
			['折后总价', 'totalPrice',,'right', 'Ext.ux.txtFormat.gridDou']
		],
		['displayFoodName', 'foodName', 'tastePref', 'count', 'totalPrice', 'totalPriceBeforeDiscount', 'discount',
		 'special', 'recommend', 'stop', 'gift', 'currPrice', 'combination', 'temporary', 'hot', 'weight'],
		[],
		0,
		''
	);
	mpo_orderFoodGrid.region = 'center';
	mpo_orderFoodGrid.getStore().on('load', function(thiz, records){
		for(var i = 0; i < records.length; i++){
			Ext.ux.formatFoodName(records[i], 'displayFoodName', 'foodName');
			if(i % 2 == 0){
				mpo_orderFoodGrid.getView().getRow(i).style.backgroundColor = '#DDD';//FFE4B5
			}
		}	
	});
	
	new Ext.Panel({
		renderTo : 'divMemberPayOrderContent',
		width : mw,
		height : mh,
		frame : true,
		layout : 'border',
		items : [{
			xtype : 'panel',
			region : 'north',
//			width : 400,
			height : 110,
			layout : 'column',
			defaults : {
				xtype : 'panel',
				layout : 'form',
				labelWidth : 60,
				labelAlign : 'right',
				columnWidth : .25,
				defaults : {
					xtype : 'textfield',
					width : 100,
					disabled : true
				}
			},
			items : [ {
				items : [mpo_memeberCardAliasID]
			}, {
				xtype : 'panel',
				html : ['',
				    '<input type="button" value="查找" onClick="memberPayOrderSreachMemberCard()" style="cursor:pointer; width:50px; " />',
				    '&nbsp;&nbsp;',
				    '<input type="button" value="读卡" onClick="memberPayOrderToLoadData()" style="cursor:pointer; width:50px;" />',
				    '&nbsp;&nbsp;',
				    '<input type="button" value="充值" onClick="memberPayOrderRecharge()" style="cursor:pointer; width:50px;" />',
				    ''
				].join('')
			}, {
				items : [{
					id : 'mpo_txtNameForPayOrder',
					fieldLabel : '会员名称'
				}]
			}, {
				items : [{
					id : 'mpo_txtTypeForPayOrder',
					fieldLabel : '会员类型'
				}]
			}, {
				items : [{
					id : 'mpo_txtTotalBalanceForPayOrder',
					fieldLabel : '余额总额'
				}]
			},  {
				items : [{
					id : 'mpo_txtBaseBalanceForPayOrder',
					fieldLabel : '基础余额'
				}]
			}, {
				items : [{
					id : 'mpo_txtExtraBalanceForPayOrder',
					fieldLabel : '赠送余额'
				}]
			}, {
				items : [{
					id : 'mpo_txtTotalPointForPayOrder',
					fieldLabel : '剩余积分'
				}]
			}, {
				items : [{
					id : 'mpo_txtDiscountForPayOrder',
					fieldLabel : '折扣方案'
				}]
			}, {
				items : [{
					id : 'mpo_txtDiscountRateForPayOrder',
					fieldLabel : '折扣率'
				}]
			}, {
				items : [{
					id : 'mpo_txtOrderPriceForPayOrder',
					fieldLabel : '账单原价'
				}]
			}, {
				items : [{
					id : 'mpo_txtMemberPriceForPayOrder',
					fieldLabel : '会员价'
				}]
			}, {
				items : [{
					xtype : 'combo',
					id : 'mpo_comPayMannerForPayOrder',
					readOnly : true,
					forceSelection : true,
					fieldLabel : '收款方式',
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true
				}]
			}, {
				items : [{
					id : 'mpo_txtPayMoneyForPayOrder',
					fieldLabel : '收款金额'
				}]
			}, {
				items : [{
					xtype : 'numberfield',
					id : 'mpo_numCustomNumberForPayOrder',
					fieldLabel : '就餐人数',
					value : 1,
					allowBlank : false,
					maxValue : 65535,
					minValue : 1,
					disabled : false
				}]
			}]
		}, 
		mpo_orderFoodGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				memberPayOrderToLoadData();
			}
		}]
	});
});

/**
 * 
 * @param _c
 */
function memberPayOrderToBindData(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
//	var memberCard = Ext.getCmp('mpo_numMemberCardAliasForPayOrder');
	var name = Ext.getCmp('mpo_txtNameForPayOrder');
	var type = Ext.getCmp('mpo_txtTypeForPayOrder');
	var totalBalance = Ext.getCmp('mpo_txtTotalBalanceForPayOrder');
	var totalPoint = Ext.getCmp('mpo_txtTotalPointForPayOrder');
	var baseBalance = Ext.getCmp('mpo_txtBaseBalanceForPayOrder');
	var extraBalance = Ext.getCmp('mpo_txtExtraBalanceForPayOrder');
	var discount = Ext.getCmp('mpo_txtDiscountForPayOrder');
	var discountRate = Ext.getCmp('mpo_txtDiscountRateForPayOrder');
	var orderPrice = Ext.getCmp('mpo_txtOrderPriceForPayOrder');
	var memberPrice = Ext.getCmp('mpo_txtMemberPriceForPayOrder');
	var payManner = Ext.getCmp('mpo_comPayMannerForPayOrder');
	var payMoney = Ext.getCmp('mpo_txtPayMoneyForPayOrder');
	var customNum = Ext.getCmp('mpo_numCustomNumberForPayOrder');
	
	var data = typeof _c.data == 'undefined' || typeof _c.data.other == 'undefined' ? {} : _c.data.other;
	
	var member = typeof data.member == 'undefined' ? {} : data.member;
	var client = typeof member.client == 'undefined' ? {} : member.client;
	var memberType = typeof member.memberType == 'undefined' ? {} : member.memberType;
	var discountMsg = typeof memberType.discount == 'undefined' ? {} : memberType.discount;
	
	var newOrder = typeof data.newOrder == 'undefined' ? {} : data.newOrder;
	
	name.setValue(client['name']);
	type.setValue(memberType['name']);
	totalBalance.setValue(member['totalBalance']);
	totalPoint.setValue(member['point']);
	baseBalance.setValue(member['baseBalance']);
	extraBalance.setValue(member['extraBalance']);
	customNum.setValue(typeof newOrder['customNum'] == 'undefined' || newOrder['customNum'] < 1 ? 1 : newOrder['customNum']);
	
	if(eval(memberType['discountType'] == 0)){
		discount.setValue(discountMsg['name']);
		discountRate.setValue('--');
	}else if(eval(memberType['discountType'] == 1)){
//		discount.setValue('全单' + (memberType['discountRate'] * 10) + '折');
		discount.setValue('全单折扣');
		discountRate.setValue(memberType['discountRate']);
	}else{
		discount.setValue();
		discountRate.setValue();
	}
	
	payManner.setDisabled(false);
	if(eval(memberType['attributeValue'] == 2) || eval(client['clientTypeID'] == 0)){
		payManner.store.loadData([[1, '现金'], [2, '刷卡']]);
		payManner.setValue(1);
	}else{
		payManner.store.loadData([[3, '会员余额']]);
		payManner.setValue(3);
		payManner.setDisabled(true);
	}
	
	if(typeof newOrder['orderFoods'] != 'undefined'){
		mpo_orderFoodGrid.getStore().loadData({root:newOrder['orderFoods']});
		orderPrice.setValue(newOrder['totalPrice'].toFixed(2));
		memberPrice.setValue(newOrder['acturalPrice'].toFixed(2));
		payMoney.setValue(newOrder['acturalPrice'].toFixed(2));
	}else{
		mpo_orderFoodGrid.getStore().removeAll();
		orderPrice.setValue();
		memberPrice.setValue();
		payMoney.setValue();
	}
}

/**
 * 
 * @param _c
 */
function memberPayOrderToLoadData(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
	var cardAlias = Ext.getCmp('mpo_numMemberCardAliasForPayOrder');
	if(typeof _c.memberCard != 'undefined'){
		cardAlias.setValue(_c.memberCard);
	}else{
		if(!cardAlias.isValid()){
			return;
		}
	}
	var tempLoadMask = new Ext.LoadMask(document.body, {
		msg : '正在读取会员结账相关信息, 请稍候......',
		remove : true
	});
	tempLoadMask.show();
	memberPayOrderToBindData();
	Ext.Ajax.request({
		url : '../../QueryOrderFromMemberPay.do',
		params : {
			pin : pin,
			restaurantID : restaurantID,
			orderID : orderID,
			memberCard : cardAlias.getValue()
		},
		success : function(res, opt){
			tempLoadMask.hide();
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				if(jr.other.member.statusValue == 0){
					mpo_memberDetailData = jr.other;
					memberPayOrderToBindData({
						data : jr
					});					
				}else{
					Ext.example.msg('提示', '该会员已冻结, 请选择其他会员.');
				}
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			rd_mask_load_recharge.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}

function initMmeberPayOrderSreachMemberCard(){
	if(!mpo_memberPayOrderSreachMemberCardWin){
		mpo_memberPayOrderSreachMemberCardWin = new Ext.Window({
			id : 'mpo_memberPayOrderSreachMemberCardWin',
			title : '查找会员',
			closable : false,
			modal : true,
			resizable : false,
			width : 800,
			height : 430,
			layout : 'border',
			items : [{
				xtype : 'panel',
				border : false,
				region : 'center'
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					mpo_memberPayOrderSreachMemberCardWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
					thiz.body.update('');
				},
				show : function(thiz){
					thiz.center();
					thiz.load({
						url : '../window/client/searchMemberCard.jsp',
						scripts : true,
						params : {
							callback : 'memberPayOrderSreachMemberCardCallback'
						}
					});
				}
			},
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					mpo_memberPayOrderSreachMemberCardWin.hide();
				}
			}]
		});
	}
}

/**
 * 
 */
function memberPayOrderSreachMemberCard(){
	initMmeberPayOrderSreachMemberCard();
	mpo_memberPayOrderSreachMemberCardWin.show();
}

/**
 * 
 * @param data
 * @param _c
 */
function memberPayOrderSreachMemberCardCallback(data, _c){
	mpo_memberPayOrderSreachMemberCardWin.hide();
	Ext.getCmp('mpo_numMemberCardAliasForPayOrder').setValue(data['memberCard.aliasID']);
	memberPayOrderToLoadData();
}
/**
 * 
 */
function initMemberPayOrderRechargeWin(){
	if(!mpo_memberPayOrderRechargeWin){
		mpo_memberPayOrderRechargeWin = new Ext.Window({
			id : 'mpo_memberPayOrderRechargeWin',
			title : '会员充值',
			closable : false,
			modal : true,
			resizable : false,
			width : 650,
			height : 430,
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					mpo_memberPayOrderRechargeWin.hide();
				}
			}],
			listeners : {
				hide : function(thiz){
					thiz.body.update('');
				},
				show : function(thiz){
					var member = mpo_memberDetailData != null && typeof mpo_memberDetailData != 'undefined' ? mpo_memberDetailData.member : {};
					member = typeof member == 'undefined' ? {} : member;
					var memberType = typeof member.memberType == 'undefined' ? {} : member.memberType;
					var client = typeof member.client == 'undefined' ? {} : member.client;
					var cardAlias = eval(memberType.attributeValue == 0) && eval(client.clientTypeID != 0) ? member.memberCard.aliasID : ''; 
					thiz.center();
					thiz.load({
						url : '../window/client/recharge.jsp',
						scripts : true,
						params : {
							memberCard : cardAlias
						}
					});
				}
			},
			bbar : [{
				xtype : 'checkbox',
				id : 'mpo_chbPrintRecharge',
				checked : true,
				boxLabel : '打印充值信息'
			}, '->', {
				text : '充值',
				iconCls : 'icon_tb_recharge',
				handler : function(e){
					rechargeControlCenter({
						isPrint : Ext.getCmp('mpo_chbPrintRecharge').getValue(),
						callback : function(_c){
							mpo_memberPayOrderRechargeWin.hide();
							var n = Ext.getCmp('mpo_numMemberCardAliasForPayOrder');
							n.setValue(_c.data.memberCardAlias);
							memberPayOrderToLoadData();
						}
					});
				}
			}, '-', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					mpo_memberPayOrderRechargeWin.hide();
				}
			}]
		});
	}
}
/**
 * 
 */
function memberPayOrderRecharge(){
	var member = mpo_memberDetailData != null && typeof mpo_memberDetailData != 'undefined' ? mpo_memberDetailData.member : {};
	member = typeof member == 'undefined' ? {} : member;
	var memberType = typeof member.memberType == 'undefined' ? {} : member.memberType;
	var client = typeof member.client == 'undefined' ? {} : member.client;
	
	if(typeof member.id == 'undefined'){
		Ext.example.msg('提示', '请先刷卡.');
	}else if(eval(memberType.attributeValue == 0) && eval(client.clientTypeID != 0)){
		initMemberPayOrderRechargeWin();
		mpo_memberPayOrderRechargeWin.show();		
	}else{
		Ext.example.msg('提示', '匿名用户或会员类型优惠属性不能充值, 请重新选择.');
	}
}

/**
 * 结账
 */
function memberPayOrderHandler(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
	if(mpo_memberDetailData == null && typeof mpo_memberDetailData == 'undefined'){
		Ext.example.msg('提示', '操作失败, 请先刷卡.');
		return;
	}

	var member = mpo_memberDetailData != null && typeof mpo_memberDetailData != 'undefined' ? mpo_memberDetailData.member : {};
	member = typeof member == 'undefined' ? {} : member;
	var memberType = typeof member.memberType == 'undefined' ? {} : member.memberType;
	var client = typeof member.client == 'undefined' ? {} : member.client;
	var order = mpo_memberDetailData.newOrder;
	
	var payManner = Ext.getCmp('mpo_comPayMannerForPayOrder');
	var customNum = Ext.getCmp('mpo_numCustomNumberForPayOrder');
	
	if(!payManner.isValid() || !customNum.isValid()){
		return;
	}
	
	if(eval(memberType.attributeValue == 0) && eval(client.clientTypeID != 0)){
		if(eval(member.totalBalance < order.acturalPrice)){
			Ext.example.msg('提示', '操作失败, 会员卡余额不足, 请先充值.');
			return;
		}
	}
	if(typeof _c.disabledButton == 'function'){
		_c.disabledButton();
	}
	Ext.Ajax.request({
		url : "../../PayOrder.do",
		params : {
			pin : _c.pin,
			orderID : order['id'],
			cashIncome : order['acturalPrice'],
			payType : 2,
			discountID : memberType['discount']['id'],
			payManner : payManner.getValue(),
			tempPay : false,
			memberID : member['id'],
			comment : '',
			serviceRate : order['serviceRate'],
			eraseQuota : 0,
			pricePlanID : order['pricePlanID'],
			customNum : customNum.getValue()
		},
		success : function(res, opt){
			if(typeof _c.enbledButton == 'function'){
				_c.enbledButton();
			}
			if(typeof _c.callback != 'undefined'){
				mpo_memberDetailData.newOrder.payMannerDisplay = payManner.getRawValue();
				_c.callback(Ext.decode(res.responseText), mpo_memberDetailData, _c);
			}
		},
		failure : function(res, opt) {
			if(typeof _c.enbledButton == 'function'){
				_c.enbledButton();
			}
			Ext.ux.showMsg(res.responseText);
		}
	});
}