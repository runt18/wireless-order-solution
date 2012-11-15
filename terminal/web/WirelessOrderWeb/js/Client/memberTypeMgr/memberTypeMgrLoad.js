dataInit = function(){
	Ext.Ajax.request({
		url : '../../QueryDiscountTree.do',
		params : {
			restaurantID : restaurantID,
			pin : pin
		},
		success : function(res, opt){
			discountData = eval(res.responseText);
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
};

memberTypeLoad = function(){
	
};


memberTypeWinInit = function(){
	if(!memberTypeWin){
		memberTypeWin = new Ext.Window({
			title : 'memberTypeWin',
			closable : false,
			modal : true,
			resizable : false,
			width : 230,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 65,
				defaults : {
					width : 130
				},
				items : [{
					xtype : 'hidden',
					id : 'numTypeID',
					fieldLabel : '类型编号'
				}, {
					xtype : 'textfield',
					id : 'txtTypeName',
					fieldLabel : '类型名称' + Ext.ux.txtFormat.xh,
					validator : function(v){
						if(Ext.util.Format.trim(v).length == 0){
							return '类型名称不能为空.';
						}else{
							return true;
						}
					}
				}, {
					xtype : 'numberfield',
					id : 'numChargeRate',
					fieldLabel : '充值比率' + Ext.ux.txtFormat.xh,
					minValue : 0.01,
					minText : '充值比率最小为 0.01.',
					decimalPrecision : 2,
					allowBlank : false,
					blankText : '充值比率不能为空.',
					selectOnFocus : true
				}, {
					xtype : 'numberfield',
					id : 'numExchangeRate',
					fieldLabel : '积分比率' + Ext.ux.txtFormat.xh,
					minValue : 0.01,
					minText : '积分比率最小为 0.01.',
					allowBlank : false,
					blankText : '积分比率不能为空.',
					selectOnFocus : true
				}, {
					xtype : 'combo',
					id : 'comboDiscountType',
					fieldLabel : '折扣方式' + Ext.ux.txtFormat.xh,
					readOnly : true,
					forceSelection : true,
					value : 0,
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : discountTypeData
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					blankText : '折扣方式不能为空.',
					listeners : {
						select : function(thiz, record, index){
							var discount = Ext.getCmp('comboDiscount');
							var discountRate = Ext.getCmp('numDiscountRate');
							if(index == 0){
								discount.setDisabled(false);
								discountRate.setDisabled(true);
							}else if(index == 1){
								discount.setDisabled(true);
								discountRate.setDisabled(false);
							}
							discount.setValue();
							discountRate.setValue();
							discount.clearInvalid();
							discountRate.clearInvalid();
						}
					}
				}, {
					xtype : 'combo',
					id : 'comboDiscount',
					fieldLabel : '折扣方案' + Ext.ux.txtFormat.xh,
					readOnly : true,
					forceSelection : true,
					allowBlank : false,
					blankText : '折扣方案不能为空.',
					store : new Ext.data.JsonStore({
						fields : [ 'discountID', 'text' ]
					}),
					valueField : 'discountID',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true
				}, {
					xtype : 'numberfield',
					id : 'numDiscountRate',
					fieldLabel : '折扣率',
					maxValue : 1,
					maxText : '折扣率最大为 1,1为不打折',
					minValue : 0.00,
					minText : '折扣率最小为 0.00',
					allowBlank : false,
					blankText : '折扣率不能为空.',
					selectOnFocus : true
				}, {
					xtype : 'combo',
					id : 'comboAttribute',
					fieldLabel : '会员属性' + Ext.ux.txtFormat.xh,
					readOnly : true,
					forceSelection : true,
					value : 0,
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : memberAttributData
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					blankText : '会员属性不能为空.'
				}]
			}],
			bbar : ['->', {
				text : '保存',
				id : 'btnSaveMemberType',
				iconCls : 'btn_save',
				handler : function(e){
					var typeID = Ext.getCmp('numTypeID');
					var typeName = Ext.getCmp('txtTypeName');
					var chargeRate = Ext.getCmp('numChargeRate');
					var exchangeRate = Ext.getCmp('numExchangeRate');
					var discountType = Ext.getCmp('comboDiscountType');
					var discount = Ext.getCmp('comboDiscount');
					var discountRate = Ext.getCmp('numDiscountRate');
					var attribute = Ext.getCmp('comboAttribute');
					
					var odid = Ext.ux.getSelData(memberTypeGrid.getId())['discountID'];
					var actionURL = '';
					
					if(memberTypeWin.otype == mtObj.operation['insert']){
						actionURL = '../../InsertMemberType.do';
					}else if(memberTypeWin.otype == mtObj.operation['update']){
						actionURL = '../../UpdateMemberType.do';
					}
					
					if(!typeName.isValid() || !chargeRate.isValid() || !exchangeRate.isValid() || !discountType.isValid() || !attribute.isValid()){
						return;
					}
					if(discountType.getValue() == 0){
						if(!discount.isValid())
							return;
					}else if(discountType.getValue() == 1){
						if(!discountRate.isValid())
							return;
					}
					
					Ext.Ajax.request({
						url : actionURL,
						params : {
							restaurantID : restaurantID,
							typeID : typeID.getValue(),
							typeName : typeName.getValue(),
							discountID : discountType.getValue() == 0 ? discount.getValue() : odid,
							discountRate : discountRate.getValue(),
							discountType : discountType.getValue(),
							exchangeRate : exchangeRate.getValue(),
							chargeRate : chargeRate.getValue(),
							attr : attribute.getValue(),
							oldDiscountID : odid
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								memberTypeWin.hide();
								memberTypeGrid.getStore().reload();
							}else{
								Ext.ux.showMsg(jr);								
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				id : 'btnCloseMemberType',
				iconCls : 'btn_close',
				handler : function(e){
					memberTypeWin.hide();
				}
			}],
			listeners : {
				show : function(e){
					Ext.getCmp('comboDiscount').store.loadData(discountData);
				}
			},
			keys : [{
				key : Ext.EventObject.ENTER,
				fn : function(){
					Ext.getCmp('btnSaveMemberType').handler();
				},
				scope : this
			}, {
				key : Ext.EventObject.ESC,
				fn : function(){
					Ext.getCmp('btnCloseMemberType').handler();
				},
				scope : this
			}]
		});
	}
};


memberTypeInit = function(){
	
	getOperatorName(pin, '../../');
	memberTypeWinInit();
	
//	Ext.Ajax.request({
//	url : '../../QueryDiscountTree.do',
//	params : {
//		restaurantID : restaurantID,
//		pin : pin
//	},
//	success : function(res, opt){
//		alert(res.responseText);
//	},
//	failure : function(res, opt){
//		Ext.ux.showMsg(Ext.decode(res.responseText));
//	}
//});
	
};

