var mpo_memberDetailData;
var mpo_orderFoodGrid;
var mpo_memberPayOrderSreachMemberCardWin;
var mpo_memberPayOrderRechargeWin;
var mpo_payMannerData = [[1, '现金'], [2, '刷卡'], [3, '会员余额']];
var mpo_servicePlanData = [], mpo_pricePlanData = [];


function reloadMemberPay(){
	Ext.Ajax.request({
		url : '../../QueryOrderFromMemberPay.do',
		params : {
			st : 1,
			sv :  Ext.getCmp('mpo_numMemberMobileForPayOrder').getValue(),
			discountID : Ext.getCmp('mpo_txtDiscountForPayOrder').getValue(),
			pricePlanId : Ext.getCmp('mpo_txtPricePlanForPayOrder').getValue(),
			servicePlanId : Ext.getCmp('mpo_comboServicePlan').getValue(),
			couponId : Ext.getCmp('mpo_couponForPayOrder').getValue(),
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
				checkOut_actualPrice = no['actualPrice'];
				Ext.getDom('mpo_txtPayMoneyForPayOrder').text = no['actualPrice'].toFixed(2);
			}else{
				Ext.example.msg(jr.title, jr.msg);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});	
}

Ext.onReady(function(){
	var contetntDiv = document.getElementById('divMemberPayOrderContent');
	if(orderID == null){
		contetntDiv.innerHTML = '操作失败, 获取账单信息失败, 请联系客服人员.';
		return false;
	}
	
	loadSystemSetting();
	
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
	
/*	var mpo_memeberCardAliasID = new Ext.form.NumberField({
			xtype : 'numberfield',
			id : 'mpo_numMemberCardAliasForPayOrder',
//			inputType : 'password',
			fieldLabel : '会员卡',
			style : 'font-weight:bold;color:#FF0000;',
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
	});*/
	var secondStepPanelSouth = {
		id : 'mpo_secondStepPanelSouth',
		region : 'south',
		frame : false,
		border : false,
		height : 32,
		bodyStyle : 'font-size:26px;text-align:left;',
		html : '收款 : <span id="mpo_txtPayMoneyForPayOrder"  style="color:red;height:27px;width:120px;font-size:26px;font-weight:bolder;">0.00</span>'+
				'<div id="div_memberShowEraseQuota" style="display:none;float:right">抹数金额(上限:￥<font id="font_showMemberEraseQuota" style="color:red;"></font>)：' +
				'￥<span id="mp_spanHasEraseQuota"></span></div>'
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
			height : 95,
			layout : 'column',
			defaults : {
				layout : 'form',
				labelWidth : 70,
				labelAlign : 'right',
				columnWidth : .2,
				defaults : {
					xtype : 'label',
					width : 80,
					style : 'fontSize:14px;'
				}
			},
			items : [{
				columnWidth : .4,
				labelWidth : 120,
				items : [{
					xtype : 'numberfield',
					id : 'mpo_numMemberMobileForPayOrder',
					fieldLabel : '<font style="font-size:18px;">手机号/卡号</font>',
					disabled : false,
					width : 150,
					height : 30,
					style : 'font-size:20px;color:Brown',					
//					value : '手机号/卡号',
					listeners : {
						render : function(thiz){
							thiz.setDisabled(false);
							new Ext.KeyMap('mpo_numMemberMobileForPayOrder', [{
								key : 13,
								scope : this,
								fn : function(){
									memberPayOrderToLoadData({otype:0});
								}
							}]);
						}
					}
				}]
			}, {
				xtype : 'panel',
				html : ['',
//				    '<input type="button" value="查找" onClick="memberPayOrderSreachMemberCard()" style="cursor:pointer; width:50px; " />',
//				    '&nbsp;&nbsp;',
				    '<input type="button" value="读取会员" onClick="memberPayOrderToLoadData({otype:0})" style="cursor:pointer;width:90px;font-size:18px;" />'
//				    '&nbsp;&nbsp;',
//				    '<input type="button" value="充值" onClick="memberPayOrderRecharge()" style="cursor:pointer; width:50px;" />',
				].join('')
			}, {
				columnWidth : 1
			},{
				items : [{
					id : 'mpo_txtNameForPayOrder',
					fieldLabel : '&nbsp;&nbsp;&nbsp;会员名称',
					text : '----'
				}]
			}, {
				items : [{
					id : 'mpo_txtTypeForPayOrder',
					fieldLabel : '会员类型',
					text : '----'
				}]
			},{
				items : [{
					id : 'mpo_txtTotalBalanceForPayOrder',
					fieldLabel : '会员余额',
					text : '----'
				}]
			},{
				items : [{
					id : 'mpo_txtTotalPointForPayOrder',
					fieldLabel : '会员积分',
					text : '----'
				}]
			}, {
				items : [{
					xtype : 'numberfield',
					id : 'mpo_numCustomNumberForPayOrder',
					fieldLabel : '就餐人数',
					value : 1,
					width : 50,
					allowBlank : false,
					maxValue : 65535,
					minValue : 1,
					disabled : false
				}]
			}, {
				columnWidth : 1
			}, 
			{
				items : [{
					xtype : 'combo',
					id : 'mpo_txtDiscountForPayOrder',
					readOnly : false,
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
								url : '../../OperateDiscount.do',
								params : {
									dataSource : 'setDiscount',
									orderId : orderMsg.id, 
									memberId : mpo_memberDetailData.member.id,
									discountId : thiz.getValue() 
								},
								success : function(res){
									var jr = Ext.decode(res.responseText);
									if(jr.success){
										reloadMemberPay();
									}else{
										Ext.example.msg(jr.title, jr.msg);
									}
								},
								failure : function(res){}
							});							
							
						}
					}
				}]
			}, 
			{
				items : [{
					xtype : 'combo',
					id : 'mpo_txtPricePlanForPayOrder',
					readOnly : false,
					forceSelection : true,
					disabled : false,
					fieldLabel : '价格方案',
					store : new Ext.data.JsonStore({
						fields : [ 'id', 'name' ]
					}),
					valueField : 'id',
					displayField : 'name',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					listeners : {
						select : function(thiz){
							reloadMemberPay();
						}
					}
				}]
			},{
				items : [{
					xtype : 'combo',
					id : 'mpo_comboServicePlan',
					width : 85,
					readOnly : false,
					fieldLabel : '服务费方案',
					forceSelection : true,
					store : new Ext.data.SimpleStore({
						fields : ['planId', 'name']
					}),
					valueField : 'planId',
					displayField : 'name',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					listeners : {
						select : function(thiz){
							reloadMemberPay();
						}
					}
				}]
			}, {
				items : [{
					xtype : 'combo',
					id : 'mpo_comPayMannerForPayOrder',
					readOnly : false,
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
					xtype : 'combo',
					id : 'mpo_couponForPayOrder',
					readOnly : false,
					forceSelection : true,
					disabled : false,
					fieldLabel : '<font style="color:red;font-weight:bold">＊</font>优惠劵',
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
							reloadMemberPay();
						}
					}
				}]
			}]
		}, 
		mpo_orderFoodGrid, secondStepPanelSouth],
		listeners : {
			render : function(){
				Ext.getCmp('mpo_numMemberMobileForPayOrder').focus(true, 100);
			}
		}
	});
	
	Ext.getCmp('mpo_couponForPayOrder').getEl().up('.x-form-item').setDisplayed(false);
});

function loadSystemSetting(_c){
	if(_c == null || typeof _c == 'undefined'){
		_c = {};
	}
	Ext.Ajax.request({
		url : '../../QuerySystemSetting.do',
		success : function(response, options) {
			var jr = Ext.decode(response.responseText);
			if (jr.success == true) {
				restaurantData = jr.other.systemSetting;
				if(restaurantData.setting.eraseQuota > 0){
					new Ext.form.NumberField({
						id : 'txtMemberEraseQuota',
						width : 95,
						height : 30,
						style : 'font-size:20px;background: #f9f9c0 repeat-x 0 0;',
						renderTo : 'mp_spanHasEraseQuota',
						listeners : {
							'render': {
							    fn: function(c){
							        c.getEl().on(
							            'keyup',
							            function() {
							            		var shouldPay = checkOut_actualPrice - c.getEl().dom.value;
							            		shouldPay = shouldPay < 0 ? 0 : shouldPay;
							            		$('#mpo_txtPayMoneyForPayOrder').html(checkDot(shouldPay)?shouldPay.toFixed(2) : shouldPay);
							            }
							        );
							    },
							    scope: this
							 
							}						
						}
					});					
					
					Ext.getDom('div_memberShowEraseQuota').style.display = 'block';
					Ext.getDom('font_showMemberEraseQuota').innerHTML = parseInt(restaurantData.setting.eraseQuota);
				}else{
					Ext.getDom('div_memberShowEraseQuota').style.display = 'none';
					Ext.getDom('font_showMemberEraseQuota').innerHTML = '';
				}
			} else {
				jr.success = true;
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response,options) {
			Ext.ux.showMsg(Ext.decode(jr));
		},
		callback : _c.callback
	});
}


/**
 * 
 * @param _c
 */
function memberPayOrderToBindData(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	var servicePlan = Ext.getCmp('mpo_comboServicePlan');
	
	if(mpo_servicePlanData.length == 0){
		Ext.Ajax.request({
			url : '../../QueryServicePlan.do',
			params : {dataSource : 'planTree'},
			success : function(res, opt){
				var jr = Ext.decode(res.responseText);
				var defaultId='', reserved='';
				for(var i = 0; i < jr.length; i++){
					mpo_servicePlanData.push([jr[i]['planId'], jr[i]['text']]);
					if(jr[i]['status'] == 2){
						defaultId = jr[i]['planId'];
					}else if(jr[i]['type'] == 2){
						reserved = jr[i]['planId'];
					}
				}
				if(defaultId == ''){
					defaultId = reserved;
				}
				servicePlan.store.loadData(mpo_servicePlanData);
				
				servicePlan.setValue(defaultId);
			},
			fialure : function(res, opt){
				servicePlan.store.loadData(mpo_servicePlanData);
			}
		});		
	}

	
	var name = Ext.getCmp('mpo_txtNameForPayOrder');
	var type = Ext.getCmp('mpo_txtTypeForPayOrder');
	var totalBalance = Ext.getCmp('mpo_txtTotalBalanceForPayOrder');
	var totalPoint = Ext.getCmp('mpo_txtTotalPointForPayOrder');
	var discountCbo = Ext.getCmp('mpo_txtDiscountForPayOrder');
	var pricePlanCbo = Ext.getCmp('mpo_txtPricePlanForPayOrder');
	
	var payManner = Ext.getCmp('mpo_comPayMannerForPayOrder');
	var payMoney = $('#mpo_txtPayMoneyForPayOrder');
	var customNum = Ext.getCmp('mpo_numCustomNumberForPayOrder');
	
	if(Ext.getCmp('txtMemberEraseQuota')){
		Ext.getCmp('txtMemberEraseQuota').setValue();
	}
	
	
	var coupon = Ext.getCmp('mpo_couponForPayOrder');
	
	var data = typeof _c.data == 'undefined' || typeof _c.data.other == 'undefined' ? {} : _c.data.other;
	
	var member = typeof data.member == 'undefined' ? {} : data.member;
	var memberType = typeof member.memberType == 'undefined' ? {} : member.memberType;
	var discountMsg = typeof memberType.discount == 'undefined' ? {} : memberType.discount;
	var discountMsgs = typeof memberType.discounts == 'undefined' ? {discounts:[{id:-1, name:'全部'}]} : memberType.discounts;
	var newOrder = typeof data.newOrder == 'undefined' ? {} : data.newOrder;
	
	var coupons = typeof data.coupons == 'undefined' ? null : data.coupons;
	
	coupon.getEl().up('.x-form-item').setDisplayed(false);
	pricePlanCbo.getEl().up('.x-form-item').setDisplayed(false);
	
	if(coupons){
		coupon.getEl().up('.x-form-item').setDisplayed(true);
		var list = [[-1,'不使用']];
		for (var i = 0; i < coupons.length; i++) {
			list.push([coupons[i].couponId, coupons[i].couponType.name]);
		}
		if(coupon.store.getCount() == 0){
			coupon.store.loadData(list);
		}
	}
	
	
	name.setText(typeof member['name'] != 'undefined'?member['name']:'----');
	type.setText(typeof memberType['name'] != 'undefined'?memberType['name']:'----');
	totalBalance.setText(typeof member['totalBalance'] != 'undefined'?member['totalBalance']:'----');
	totalPoint.setText(typeof member['point'] != 'undefined'?member['point']:'----');
	customNum.setValue(typeof newOrder['customNum'] == 'undefined' || newOrder['customNum'] < 1 ? 1 : newOrder['customNum']);

	
	var discounts = [];
	for (var i = 0; i < discountMsgs.length; i++) {
		discounts.push([discountMsgs[i].id, discountMsgs[i].name]);
	}
	
	if(!discountCbo.getValue()){
		discountCbo.store.loadData(discounts);
		discountCbo.setValue(discountMsg['id']);
	} 
	
	if(mpo_pricePlanData.length == 0){
		if(memberType.pricePlans && memberType.pricePlans.length > 0){
			pricePlanCbo.getEl().up('.x-form-item').setDisplayed(true);
			mpo_pricePlanData = memberType.pricePlans; 
			memberType.pricePlans.unshift({id : '-1', name : '普通价'});
			pricePlanCbo.store.loadData(memberType.pricePlans);
			pricePlanCbo.setValue(memberType.pricePlan.id);
		}		
	}else{
		pricePlanCbo.getEl().up('.x-form-item').setDisplayed(true);
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
		payMoney.html(newOrder['actualPrice'].toFixed(2));
	}else{
		mpo_orderFoodGrid.getStore().removeAll();
		payMoney.html('0.00');
	}
}

/**
 * 
 * @param c
 */
function memberPayOrderToLoadData(c){
	if(typeof Ext.ux.select_getMemberByCertainWin != 'undefined'){
		Ext.ux.select_getMemberByCertainWin.hide();
	}
	c = c == null || typeof c == 'undefined' ? {} : c;
	
	var mobile = Ext.getCmp('mpo_numMemberMobileForPayOrder');
//	var memberCard = Ext.getCmp('mpo_numMemberCardAliasForPayOrder');
	if(!mobile.getValue()){
		return;
	}
	
	if(c.mobile){
		mobile.setValue(c.mobile);
	}
	var tempLoadMask = new Ext.LoadMask(document.body, {
		msg : '正在读取会员结账相关信息, 请稍候......',
		remove : true
	});
	tempLoadMask.show();
//	memberPayOrderToBindData();
	Ext.Ajax.request({
		url : '../../QueryOrderFromMemberPay.do',
		params : {
			orderID : orderID,
			st : c.sType,
			sv : mobile.getValue()
		},
		success : function(res, opt){
			tempLoadMask.hide();
			var jr = Ext.decode(res.responseText);
			if(jr.success){
//				if(jr.other.member.statusValue == 0){
					mpo_memberDetailData = jr.other;
//					Ext.getCmp('mpo_couponForPayOrder').setValue();
					Ext.getCmp('mpo_txtDiscountForPayOrder').setValue();
					if(jr.other.members && jr.other.members.length > 1){
						c.callback = memberPayOrderToLoadData;
						Ext.ux.select_getMemberByCertain(c);
					}else{
						mpo_servicePlanData = [];
						mpo_pricePlanData = [];
						memberPayOrderToBindData({
							data : jr
						});						
					}
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
	var choosePricePlan = Ext.getCmp('mpo_txtPricePlanForPayOrder');
	var chooseCoupon = Ext.getCmp('mpo_couponForPayOrder');
	var eraseQuota = document.getElementById("txtMemberEraseQuota");
	
	if(!payManner.isValid() || !customNum.isValid()){
		return;
	}
				// 抹数金额
	if(eraseQuota && !isNaN(eraseQuota.value) && eraseQuota.value >=0 && eraseQuota.value > restaurantData.setting.eraseQuota){
		Ext.Msg.alert("提示", "<b>抹数金额大于设置上限，不能结帐！</b>");
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
			orderID : order['id'],
			cashIncome : order['actualPrice'],
			payType : 2,
			couponID : chooseCoupon.getValue(),
			payManner : payManner.getValue(),
			tempPay : _c.tempPay,
			discountID : chooseDiscount.getValue(),
			memberID : member['id'],
			comment : '',
			serviceRate : (order['serviceRate'] * 100),
			eraseQuota : eraseQuota?(eraseQuota.value?eraseQuota.value:0):0,
			pricePlanID : choosePricePlan.getValue(),
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