Ext.onReady(function(){
	
	var contetntDiv = document.getElementById('divMemberPayOrderContent');
	if(orderID == null){
		contetntDiv.innerHTML = '操作失败, 获取账单信息失败, 请联系客服人员.';
		return false;
	}
	var pe = contetntDiv.parentElement;
	var mw = parseInt(pe.style.width);
	var mh = parseInt(pe.style.height);
	
	var memeberCardAliasID = {
		xtype : 'numberfield',
		id : 'md_numMemberCardAliasForMemberDetail',
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
	
	var mpo_orderFoodGrid = createGridPanel(
		'mpo_orderFoodGrid',
		'账单列表',
		'',
		'',
		'',
		[
			[true, false, false, false], 
			['菜品', 'typeID',230],
			['口味', 'name',230],
			['数量', 'chargeRate',,'right', 'Ext.ux.txtFormat.gridDou'],
			['原总价', 'exchangeRate',,'right', 'Ext.ux.txtFormat.gridDou'],
			['折后总价', 'discountType',,'right', 'Ext.ux.txtFormat.gridDou']
		],
		[],
		[],
		0,
		''
	);
	mpo_orderFoodGrid.region = 'center';
	
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
				items : [memeberCardAliasID]
			}, {
				xtype : 'panel',
				html : ['',
				    '<input type="button" value="查找" onClick="" style="cursor:pointer; width:50px; " />',
				    '&nbsp;&nbsp;',
				    '<input type="button" value="读卡" onClick="memberDetailLoadData()" style="cursor:pointer; width:50px;" />',
				    '&nbsp;&nbsp;',
				    '<input type="button" value="充值" onClick="" style="cursor:pointer; width:50px;" />',
				    ''
				].join('')
			}, {
				items : [{
					id : 'md_txtNameForMemberDetail',
					fieldLabel : '会员名称'
				}]
			}, {
				items : [{
					id : 'md_txtTypeForMemberDetail',
					fieldLabel : '会员类型'
				}]
			}, {
				items : [{
					id : 'md_txtTotalBalanceForMemberDetail',
					fieldLabel : '余额总额'
				}]
			},  {
				items : [{
					id : 'md_txtBaseBalanceForMemberDetail',
					fieldLabel : '基础余额'
				}]
			}, {
				items : [{
					id : 'md_txtExtraBalanceForMemberDetail',
					fieldLabel : '赠送余额'
				}]
			}, {
				items : [{
					id : 'md_txtTotalPointForMemberDetail',
					fieldLabel : '剩余积分'
				}]
			}, {
				items : [{
					id : 'md_txtDiscountForMemberDetail',
					fieldLabel : '折扣方案'
				}]
			}, {
				items : [{
					id : 'md_txtDiscountRateForMemberDetail',
					fieldLabel : '折扣率'
				}]
			}, {
				items : [{
					id : 'md_txtOrderPriceForMemberDetail',
					fieldLabel : '账单原价'
				}]
			}, {
				items : [{
					id : 'md_txtMemberPriceForMemberDetail',
					fieldLabel : '会员价'
				}]
			}, {
				items : [{
					xtype : 'combo',
					id : 'md_txtPayMannerForMemberDetail',
					readOnly : true,
					forceSelection : true,
					value : 0,
					fieldLabel : '收款方式',
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [[0, '会员余额'], [1, '现金'], [2, '刷卡']]
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
					id : 'md_txtPayMoneyForMemberDetail',
					fieldLabel : '收款金额'
				}]
			}]
		}, 
		mpo_orderFoodGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				memberDetailLoadData({
					callback : function(data){
						var tempLoadMask = new Ext.LoadMask(document.body, {
							msg : '正在读取账单信息, 请稍候......',
							remove : true
						});
						memberDetailBindData({
							data : data.root[0]
						});
					}
				});
			}
		}]
	});
});

/**
 * 
 * @param _c
 */
function memberDetailBindData(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
//	var memberCard = Ext.getCmp('md_numMemberCardAliasForMemberDetail');
	var name = Ext.getCmp('md_txtNameForMemberDetail');
	var type = Ext.getCmp('md_txtTypeForMemberDetail');
	var totalBalance = Ext.getCmp('md_txtTotalBalanceForMemberDetail');
	var totalPoint = Ext.getCmp('md_txtTotalPointForMemberDetail');
	var baseBalance = Ext.getCmp('md_txtBaseBalanceForMemberDetail');
	var extraBalance = Ext.getCmp('md_txtExtraBalanceForMemberDetail');
	var discount = Ext.getCmp('md_txtDiscountForMemberDetail');
	var disocuntRate = Ext.getCmp('md_txtDiscountRateForMemberDetail');
	var orderPrice = Ext.getCmp('md_txtOrderPriceForMemberDetail');
	var memberPrice = Ext.getCmp('md_txtMemberPriceForMemberDetail');
	var payManner = Ext.getCmp('md_txtPayMannerForMemberDetail');
	var payMoney = Ext.getCmp('md_txtPayMoneyForMemberDetail');
	
	var data = typeof _c.data == 'undefined' ? {} : _c.data;
	var client = typeof data.client == 'undefined' ? {} : data.client;
	var memberType = typeof data.memberType == 'undefined' ? {} : data.memberType;
	
	name.setValue(client['name']);
	type.setValue(memberType['name']);
	totalBalance.setValue(data['totalBalance']);
	totalPoint.setValue(data['point']);
	baseBalance.setValue(data['baseBalance']);
	extraBalance.setValue(data['extraBalance']);
	
}

/**
 * 
 * @param _c
 */
function memberDetailLoadData(_c){
	_c = _c == null || typeof _c == 'undefined' ? {} : _c;
	
	var cardAlias = Ext.getCmp('md_numMemberCardAliasForMemberDetail');
	if(typeof _c.memberCard != 'undefined'){
		cardAlias.setValue(_c.memberCard);
	}else{
		if(!cardAlias.isValid()){
			return;
		}
	}
	var tempLoadMask = new Ext.LoadMask(document.body, {
		msg : '正在读取会员卡信息, 请稍候......',
		remove : true
	});
	tempLoadMask.show();
	Ext.Ajax.request({
//		url : '../../QueryMember.do',
		url : '../../QueryOrderFromMemberPay.do',
		params : {
			pin : pin,
			restaurantID : restaurantID,
			orderID : orderID,
			memberCard : cardAlias.getValue()
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				md_memberDetailData = jr;
				if(typeof _c.callback != 'undefined'){
					_c.callback(jr);
				}
			}else{
				Ext.ux.showMsg(jr);
			}
			tempLoadMask.hide();
		},
		failure : function(res, opt){
			rd_mask_load_recharge.hide();
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}