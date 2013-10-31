var mpo_memberDetailData;
var mpo_orderFoodGrid;
var mpo_memberPayOrderSreachMemberCardWin;
var mpo_memberPayOrderRechargeWin;
var mpo_payMannerData = [[1, '现金'], [2, '刷卡'], [3, '会员余额']];
Ext.onReady(function(){
	var contetntDiv = document.getElementById('divMemberPayOrderContent');
	if(orderID == null){
		contetntDiv.innerHTML = '操作失败, 获取账单信息失败, 请联系客服人员.';
		return false;
	}
	var pe = contetntDiv.parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	
	mpo_orderFoodGrid = createGridPanel(
		'mpo_orderFoodGrid',
		'账单列表',
		'',
		'',
		'',
		[
			[true, false, false, false], 
			['菜品', 'displayFoodName',230],
			['口味', 'tasteGroup.tastePref',230],
			['数量', 'count',,'right', 'Ext.ux.txtFormat.gridDou'],
			['原总价', 'totalPriceBeforeDiscount',,'right', 'Ext.ux.txtFormat.gridDou'],
			['折扣率', 'discount',,'right', 'Ext.ux.txtFormat.gridDou'],
			['折后总价', 'totalPrice',,'right', 'Ext.ux.txtFormat.gridDou']
		],
		OrderFoodRecord.getKeys(),
		[],
		0
	);
	mpo_orderFoodGrid.region = 'center';
	mpo_orderFoodGrid.getStore().on('load', function(thiz, records){
		for(var i = 0; i < records.length; i++){
			Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name');
			if(i % 2 == 0){
				mpo_orderFoodGrid.getView().getRow(i).style.backgroundColor = '#DDD';//FFE4B5
			}
		}	
	});
	
	var mpo_memeberCardAliasID = new Ext.form.NumberField({
			xtype : 'numberfield',
			id : 'mpo_numMemberCardAliasForPayOrder',
//			inputType : 'password',
			fieldLabel : '会员卡',
			style : 'font-weight: bold; color: #FF0000;',
			width : 100,
//			maxLength : 10,
//			maxLengthText : '请输入10位会员卡号',
//			minLength : 10,
//			minLengthText : '请输入10位会员卡号',
//			allowBlank : false,
//			blankText : '会员卡不能为空, 请刷卡.',
//			enableKeyEvents: true,
			listeners : {
				render : function(thiz){
					thiz.setDisabled(false);
					new Ext.KeyMap('mpo_numMemberCardAliasForPayOrder', [{
						key : 13,
						scope : this,
						fn : function(){
							memberPayOrderToLoadData({otype:'card'});
						}
					}]);
				}
			}
	});
	var secondStepPanelSouth = {
		id : 'mpo_secondStepPanelSouth',
		region : 'south',
		frame : false,
		border : false,
		height : 32,
		bodyStyle : 'font-size:26px;text-align:left;',
		html : '收款 : <input id="mpo_txtPayMoneyForPayOrder" type="text" disabled="disabled" value="0.00" style="color:red;height: 27px;width:120px;font-size :26px;font-weight: bolder;" />'
	};
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
			items : [{
				items : [{
					xtype : 'numberfield',
					id : 'mpo_numMemberMobileForPayOrder',
					fieldLabel : '手机号码',
					disabled : false,
					listeners : {
						render : function(thiz){
							thiz.setDisabled(false);
							new Ext.KeyMap('mpo_numMemberMobileForPayOrder', [{
								key : 13,
								scope : this,
								fn : function(){
									memberPayOrderToLoadData({otype:'mobile'});
								}
							}]);
						}
					}
				}]
			}, {
				items : [mpo_memeberCardAliasID]
			}, {
				xtype : 'panel',
				html : ['',
//				    '<input type="button" value="查找" onClick="memberPayOrderSreachMemberCard()" style="cursor:pointer; width:50px; " />',
//				    '&nbsp;&nbsp;',
				    '<input type="button" value="读手机号码" onClick="memberPayOrderToLoadData({otype:\'mobile\'})" style="cursor:pointer; width:80px; " />',
				    '&nbsp;&nbsp;',
				    '<input type="button" value="读会员卡" onClick="memberPayOrderToLoadData({otype:\'card\'})" style="cursor:pointer; width:70px;" />',
//				    '&nbsp;&nbsp;',
//				    '<input type="button" value="充值" onClick="memberPayOrderRecharge()" style="cursor:pointer; width:50px;" />',
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
			}, 
/*			{
				items : [{
					id : 'mpo_txtDiscountForPayOrder',
					fieldLabel : '折扣方案'
				}]
			}, */
			{
				items : [{
					xtype : 'combo',
					id : 'mpo_txtDiscountForPayOrder',
					readOnly : true,
					forceSelection : true,
					disabled : false,
					fieldLabel : '折扣方案',
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					listeners : {
						select : function(thiz){
							Ext.Ajax.request({
								url : '../../QueryOrderFromMemberPay.do',
								params : {
									
									discountId : thiz.getValue(),
									orderID : orderID
								},
								success : function(res, opt){
									var jr = Ext.decode(res.responseText);
									if(jr.success){
										var no = jr.other.newOrder;
										jr.other.member = mpo_memberDetailData.member; 
										memberPayOrderToBindData({
											data : jr
										});	
										mpo_memberDetailData.newOrder = no;
										Ext.getCmp('mpo_txtMemberPriceForPayOrder').setValue(no['actualPrice'].toFixed(2));
										Ext.getDom('mpo_txtPayMoneyForPayOrder').value = no['actualPrice'].toFixed(2);
									}else{
										Ext.example.msg(jr.title, jr.msg);
									}
								},
								failure : function(res, opt){
									Ext.ux.showMsg(Ext.decode(res.responseText));
								}
							});
						}
					}
				}]
			},
			{
				hidden : true,
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
		mpo_orderFoodGrid, secondStepPanelSouth]
	});
});

/**
 * 
 * @param _c
 */
function memberPayOrderToBindData(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
	var mobile = Ext.getCmp('mpo_numMemberMobileForPayOrder');
	var memberCard = Ext.getCmp('mpo_numMemberCardAliasForPayOrder');
	
	var name = Ext.getCmp('mpo_txtNameForPayOrder');
	var type = Ext.getCmp('mpo_txtTypeForPayOrder');
	var totalBalance = Ext.getCmp('mpo_txtTotalBalanceForPayOrder');
	var totalPoint = Ext.getCmp('mpo_txtTotalPointForPayOrder');
	var baseBalance = Ext.getCmp('mpo_txtBaseBalanceForPayOrder');
	var extraBalance = Ext.getCmp('mpo_txtExtraBalanceForPayOrder');
	var discountCbo = Ext.getCmp('mpo_txtDiscountForPayOrder');
	var discountRate = Ext.getCmp('mpo_txtDiscountRateForPayOrder');
	var orderPrice = Ext.getCmp('mpo_txtOrderPriceForPayOrder');
	var memberPrice = Ext.getCmp('mpo_txtMemberPriceForPayOrder');
	var payManner = Ext.getCmp('mpo_comPayMannerForPayOrder');
	var payMoney = Ext.getDom('mpo_txtPayMoneyForPayOrder');
	var customNum = Ext.getCmp('mpo_numCustomNumberForPayOrder');
	
	var data = typeof _c.data == 'undefined' || typeof _c.data.other == 'undefined' ? {} : _c.data.other;
	
	var member = typeof data.member == 'undefined' ? {} : data.member;
	var memberType = typeof member.memberType == 'undefined' ? {} : member.memberType;
	var discountMsg = typeof memberType.discount == 'undefined' ? {} : memberType.discount;
	var discountMsgs = typeof memberType.discounts == 'undefined' ? {discounts:[{id:-1, name:'全部'}]} : memberType.discounts;
	var newOrder = typeof data.newOrder == 'undefined' ? {} : data.newOrder;
	
	mobile.setValue(member['mobile']);
	memberCard.setValue(member['memberCard']);
	name.setValue(member['name']);
	type.setValue(memberType['name']);
	totalBalance.setValue(member['totalBalance']);
	totalPoint.setValue(member['point']);
	baseBalance.setValue(member['baseBalance']);
	extraBalance.setValue(member['extraBalance']);
	customNum.setValue(typeof newOrder['customNum'] == 'undefined' || newOrder['customNum'] < 1 ? 1 : newOrder['customNum']);

	discountRate.setValue('--');
	
	var discounts = [];
	for (var i = 0; i < discountMsgs.length; i++) {
		discounts.push([discountMsgs[i].id, discountMsgs[i].name]);
	}
	
	if(discountCbo.getValue() == ""){
		discountCbo.store.loadData(discounts);
		discountCbo.setValue(discountMsg['id']);
	}

	
	payManner.setDisabled(true);
	if(memberType['attributeValue'] == 1){
		payManner.store.loadData([mpo_payMannerData[0], mpo_payMannerData[1]]);
		payManner.setValue(1);
		payManner.setDisabled(false);

	}else if(memberType['attributeValue'] == 0){
		payManner.store.loadData([mpo_payMannerData[2]]);
		payManner.setValue(3);
	}
	
	if(typeof newOrder['orderFoods'] != 'undefined'){
		mpo_orderFoodGrid.getStore().loadData({root:newOrder['orderFoods']});
		orderPrice.setValue(newOrder['actualPriceBeforeDiscount'].toFixed(2));
		memberPrice.setValue(newOrder['actualPrice'].toFixed(2));
		payMoney.value = newOrder['actualPrice'].toFixed(2);
	}else{
		mpo_orderFoodGrid.getStore().removeAll();
		orderPrice.setValue();
		memberPrice.setValue();
		payMoney.value = "";
	}
}

/**
 * 
 * @param c
 */
function memberPayOrderToLoadData(c){
	c = c == null || typeof c == 'undefined' ? {} : c;
	
	var moblie = Ext.getCmp('mpo_numMemberMobileForPayOrder');
	var memberCard = Ext.getCmp('mpo_numMemberCardAliasForPayOrder');
	var sv = '';
	if(c.otype == 'mobile'){
		if(!moblie.isValid()){
			return;
		}
		sv = moblie.getValue();
	}else if(c.otype == 'card'){
		if(typeof c.memberCard != 'undefined'){
			memberCard.setValue(_c.memberCard);
		}else{
			if(!memberCard.isValid()){
				return;
			}
			sv = memberCard.getValue();
		}
	}else{
		return;
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
			
			restaurantID : restaurantID,
			orderID : orderID,
			st : c.otype,
			sv : sv
		},
		success : function(res, opt){
			tempLoadMask.hide();
			var jr = Ext.decode(res.responseText);
			if(jr.success){
//				if(jr.other.member.statusValue == 0){
					mpo_memberDetailData = jr.other;
					memberPayOrderToBindData({
						data : jr
					});					
//				}else{
//					Ext.example.msg('提示', '该会员已冻结, 请选择其他会员.');
//				}
			}else{
//				Ext.ux.showMsg(jr);
				Ext.example.msg(jr.title, jr.msg);
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
		Ext.example.msg('提示', '操作失败, 请先读取会员信息.');
		return;
	}

	var member = mpo_memberDetailData != null && typeof mpo_memberDetailData != 'undefined' ? mpo_memberDetailData.member : {};
	member = typeof member == 'undefined' ? {} : member;
	var memberType = typeof member.memberType == 'undefined' ? {} : member.memberType;
	var client = typeof member.client == 'undefined' ? {} : member.client;
	var order = mpo_memberDetailData.newOrder;
	
	var payManner = Ext.getCmp('mpo_comPayMannerForPayOrder');
	var customNum = Ext.getCmp('mpo_numCustomNumberForPayOrder');
	var chooseDiscount = Ext.getCmp('mpo_txtDiscountForPayOrder');
	
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
			cashIncome : order['actualPrice'],
			payType : 2,
			discountID : chooseDiscount.getValue(),
			payManner : payManner.getValue(),
			tempPay : _c.tempPay,
			memberID : member['id'],
			comment : '',
			serviceRate : order['serviceRate'],
			eraseQuota : 0,
			pricePlanID : order['pricePlan']['id'],
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