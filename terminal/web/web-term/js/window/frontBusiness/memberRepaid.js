var mpo_memberDetailData;
var mpo_orderFoodGrid;
var mpo_memberPayOrderSreachMemberCardWin;
var mpo_memberPayOrderRechargeWin;
var mpo_payMannerData = [[1, '现金'], [2, '刷卡'], [3, '会员余额']];
var mpo_servicePlanData = [], mpo_pricePlanData = [], mpo_discountData = [], mpo_couponData = [];

function reloadMemberPay(){
	Ext.Ajax.request({
		url : '../../QueryOrderFromMemberPay.do',
		params : {
			st : '',
			sv :  Ext.getCmp('mpo_numMemberMobileForPayOrder').getValue(),
			discountID : Ext.getCmp('mpo_txtDiscountForPayOrder').getValue(),
			pricePlanId : Ext.getCmp('mpo_txtPricePlanForPayOrder').getValue(),
			couponId : Ext.getCmp('mpo_couponForPayOrder').getValue(),
			orderID : orderID
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				var no = jr.other.newOrder;
				jr.other.member = mpo_memberDetailData; 
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
	var contetntDiv = document.getElementById('divMemberRepaidContent');
	
	var pe = contetntDiv.parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	new Ext.Panel({
		renderTo : 'divMemberRepaidContent',
		width : mw,
		height : mh,
		frame : true,
		layout : 'border',
		items : [{
			xtype : 'panel',
			region : 'center',
//			width : 400,
			height : 95,
			layout : 'column',
			defaults : {
				layout : 'form',
				labelWidth : 70,
				labelAlign : 'right',
				columnWidth : .25,
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
					selectOnFocus : true
					//FIXME
/*					listeners : {
						select : function(thiz){
							Ext.Ajax.request({
								url : '../../OperateDiscount.do',
								params : {
									dataSource : 'setDiscount',
									orderId : orderMsg.id, 
									memberId : mpo_memberDetailData.id,
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
					}*/
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
					selectOnFocus : true
				}]
			},{
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
					selectOnFocus : true
				}]
			}]
		}],
		listeners : {
			render : function(){
				Ext.getCmp('mpo_numMemberMobileForPayOrder').focus(true, 100);
			}
		}
	});
	
	Ext.getCmp('mpo_couponForPayOrder').getEl().up('.x-form-item').setDisplayed(false);
});

/**
 * 
 * @param _c
 */
function memberPayOrderToBindData(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;

	
	var name = Ext.getCmp('mpo_txtNameForPayOrder');
	var type = Ext.getCmp('mpo_txtTypeForPayOrder');
	var totalBalance = Ext.getCmp('mpo_txtTotalBalanceForPayOrder');
	var totalPoint = Ext.getCmp('mpo_txtTotalPointForPayOrder');
	var discountCbo = Ext.getCmp('mpo_txtDiscountForPayOrder');
	var pricePlanCbo = Ext.getCmp('mpo_txtPricePlanForPayOrder');
	
	var payMoney = $('#mpo_txtPayMoneyForPayOrder');
	
	if(Ext.getCmp('txtMemberEraseQuota')){
		Ext.getCmp('txtMemberEraseQuota').setValue();
	}
	
	var coupon = Ext.getCmp('mpo_couponForPayOrder');
	
	var data = typeof _c.data == 'undefined' || typeof _c.data.other == 'undefined' ? {} : _c.data.other;
	
	var member = typeof data.member == 'undefined' ? {} : data.member;
	var memberType = typeof member.memberType == 'undefined' ? {} : member.memberType;
	var discountMsg = typeof memberType.discount == 'undefined' ? {} : memberType.discount;
	var discountMsgs = typeof memberType.discounts == 'undefined' ? {discounts:[{id:-1, name:'全部'}]} : memberType.discounts;
	mpo_discountData = discountMsgs;
	var newOrder = typeof data.newOrder == 'undefined' ? {} : data.newOrder;
	
	
	coupon.getEl().up('.x-form-item').setDisplayed(false);
	pricePlanCbo.getEl().up('.x-form-item').setDisplayed(false);
	
//	var coupons = typeof data.coupons == 'undefined' ? null : data.coupons;
//	if(coupons){
//		coupon.getEl().up('.x-form-item').setDisplayed(false);
//		var list = [[-1,'不使用']];
//		mpo_couponData = coupons;
//		for (var i = 0; i < primaryOrderData.other.order.usedCoupons.length; i++) {
//			list.push([primaryOrderData.other.order.usedCoupons[i].couponId, primaryOrderData.other.order.usedCoupons[i].couponType.name]);
//		}
//		if(coupon.store.getCount() == 0){
//			coupon.store.loadData(list);
//		}
//	}
	
	
	name.setText(typeof member['name'] != 'undefined'?member['name']:'----');
	type.setText(typeof memberType['name'] != 'undefined'?memberType['name']:'----');
	totalBalance.setText(typeof member['totalBalance'] != 'undefined'?member['totalBalance']:'----');
	totalPoint.setText(typeof member['point'] != 'undefined'?member['point']:'----');

	
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
		url : '../../QueryMember.do',
		params : {
			dataSource : 'memberRepaid',
			st : c.sType,
			sv : mobile.getValue()
		},
		success : function(res, opt){
			tempLoadMask.hide();
			var jr = Ext.decode(res.responseText);

			if(jr.success){
//				if(jr.other.member.statusValue == 0){
					mpo_memberDetailData = jr.other.member;
//					Ext.getCmp('mpo_couponForPayOrder').setValue();
					Ext.getCmp('mpo_txtDiscountForPayOrder').setValue();
					if(jr.other.members && jr.other.members.length > 1){
						if(!jQuery.isEmptyObject(mpo_memberDetailData)){
							mpo_memberDetailData.hasMember = false;
						}else{
							mpo_memberDetailData = {hasMember : false};
						}
						c.callback = memberPayOrderToLoadData;
						Ext.ux.select_getMemberByCertain(c);
					}else{
						mpo_memberDetailData.hasMember = true;
						mpo_servicePlanData = [];
						mpo_pricePlanData = [];
						memberPayOrderToBindData({
							data : jr
						});						
					}
			}else{
//				Ext.ux.showMsg(jr);
				Ext.example.msg(jr.title, jr.msg);
				mpo_memberDetailData.hasMember = false;
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
function memberPayOrderRecharge(){
	var member = !jQuery.isEmptyObject(mpo_memberDetailData) ? mpo_memberDetailData : {};
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

function getOrderMember(){
	var discountId = Ext.getCmp('mpo_txtDiscountForPayOrder').getValue();
	var couponId = Ext.getCmp('mpo_couponForPayOrder').getValue();
	
	for (var i = 0; i < mpo_discountData.length; i++) {
		if(discountId == mpo_discountData[i].id){
			mpo_memberDetailData.discount = mpo_discountData[i];
			break;
		}
	}
	
	if(couponId){
		for (var i = 0; i < mpo_couponData.length; i++) {
			if(couponId == mpo_couponData[i].couponId){
				mpo_memberDetailData.coupon = mpo_couponData[i];
				break;
			}
		}
	}
	
	mpo_memberDetailData.pricePlanId = Ext.getCmp('mpo_txtPricePlanForPayOrder').getValue();
	
	return mpo_memberDetailData;
	
}