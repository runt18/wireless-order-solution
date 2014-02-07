var billGenModForm = new Ext.Panel({
	region : 'north',
	height : 62,
	frame : true,
	layout : 'column',
	defaults : {
		xtype : 'panel',
		layout : 'column',
		columnWidth : 1,
		defaults : {
			xtype : 'form',
			layout : 'form',
			labelWidth : 60,
			width : 220,
			defaults : {
				width : 130
			}
		}
	},
	items : [{
		items : [{
			items : [{
				xtype : 'textfield',
				id : 'txtSettleTypeFormat',
				fieldLabel : '结账方式',
				value : '一般/会员',
				disabled : true
			}]
		}, {
			items : [{
				xtype : 'combo',
				id : 'comboDiscount',
				fieldLabel : '折扣方案',
				readOnly : true,
				forceSelection : true,
				store : new Ext.data.JsonStore({
					root : 'root',
					fields : [ 'discountID', 'discountName']
				}),
				valueField : 'discountID',
				displayField : 'discountName',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				listeners : {
					select : function(combo, record, index) {
						billListRefresh();
					}
				}
			}]
		}, {
			width : 135,
			items : [{
				xtype : 'numberfield',
				id : 'numErasePrice',
				fieldLabel : '抹数金额',
				width : 60,
				minValue : 0,
				value : 0
			}]
		}, {
			xtype : 'panel',
			width : 150,
			id : 'panelShowEraseQuota',
			style : 'font-size:18px;',
			html : '上限:￥<font id="fontShowEraseQuota" style="color:red;">0.00</font>'
		}, {
			width : 130,
			items : [{
				xtype : 'numberfield',
				width : 60,
				fieldLabel : '服务费',
				id : 'serviceRate',
				allowBlank : false,
				validator : function(v) {
					if (v < 0 || v > 100 || v.indexOf('.') != -1) {
						return '服务费率范围是0%至100%,且为整数.';
					} else {
						return true;
					}
				}
			}]
		}, {
			items : [{
				xtype : 'panel',
				style : 'font-size:18px;',
				html : '%'
			}]
		}]
	}, {
		defaults : {
			labelWidth : 1,
			labelSeparator : ' '
		},
		items : [{
			xtype : 'label',
			width : 65,
			text : '收款方式:'
		}, {
			width : 80,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '现金结账',
				inputValue : '1'
			}]
		}, {
			width : 80,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '刷卡结账',
				inputValue : '2'
			}]
		}, {
			width : 80,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '会员消费',
				inputValue : '3',
				disabled : true
			}]
		}, {
			width : 60,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '签单',
				inputValue : '4'
			}]
		}, {
			width : 75,
			items : [{
				xtype : 'radio',
				name : 'radioPayType',
				boxLabel : '挂账',
				inputValue : '5'
			}]
		}, {
			xtype : 'form',
			labelWidth : 60,
			labelSeparator : ':',
			items : [{
				xtype : 'textfield',
				id : 'remark',
				fieldLabel : '备注',
				width : 420
			}]
		}]
	}]
});

Ext.onReady(function(){
	billModifyOnLoad();
	
	var centerPanelDO = new Ext.Panel({
		id : "centerPanelDO",
		region : "center",
		border : false,
		layout : "border",
		items : [ orderedGrid, dishesOrderEastPanel, billGenModForm ]
	});
	
	var billModCenterPanel = new Ext.Panel({
		id : "billModCenterPanel",
		region : "center",
		layout : "border",
		frame : true,
		title : '&nbsp;<span style="padding-left:2px; color:red;">' + orderID + '</span>&nbsp;号帐单修改',
		items : [ centerPanelDO ]
	});
	
	initMainView(null, billModCenterPanel, null);
	getOperatorName("../../");
});
