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

memberTypeWinInit = function(){
	memberTypeWin = new Ext.Window({
		title : 'memberTypeWin',
		closable : false,
		modal : true,
		resizable : false,
		width : 235,
		items : [{
			xtype : 'form',
			layout : 'form',
			frame : true,
			labelWidth : 70,
			labelAlign : 'right',
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
				allowBlank : false,
				blankText : '类型名称不能为空.',
				validator : function(v){
					if(Ext.util.Format.trim(v).length == 0){
						return '类型名称不能为空.';
					}else{
						return true;
					}
				}
			}, {
				xtype : 'combo',
				id : 'comboAttribute',
				fieldLabel : '会员属性' + Ext.ux.txtFormat.xh,
				readOnly : true,
				forceSelection : true,
				value : 0,
				store : new Ext.data.SimpleStore({
					fields : [ 'value', 'text' ],
					data : memberAttributeData
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				blankText : '会员属性不能为空.',
				listeners : {
					render : function(e){
						e.setValue(0);
						e.fireEvent('select', e);
					},
					select : function(e, rocord, index){
						var cr = Ext.getCmp('numChargeRate');
						var er = Ext.getCmp('numExchangeRate');
						if(e.getValue() == 0){
							cr.setValue(1);
							er.setValue(1);
							cr.setDisabled(false);
							er.setDisabled(false);
						}else if(e.getValue() == 2){
							cr.setValue(0);
							er.setValue(0);
							cr.setDisabled(true);
							er.setDisabled(true);
						}
						cr.clearInvalid();
						er.clearInvalid();
					}
				}
			}, {
				xtype : 'label',
				style : 'color:green;font-szie:12px;',
				text : '说明:  选择优惠属性只使用该会员类型的折扣信息, 会员属性则需要使用会员资料中基本金额、赠送金额、积分等相关信息'
			}, {
				xtype : 'numberfield',
				id : 'numChargeRate',
				fieldLabel : '充值比率' + Ext.ux.txtFormat.xh,
				value : 1.00,
				minValue : 0.00,
				minText : '充值比率最小为 0.00.',
				decimalPrecision : 2,
				allowBlank : false,
				blankText : '充值比率不能为空.',
				selectOnFocus : true,
				disabled : true
			}, {
				xtype : 'label',
				style : 'color:green;font-szie:12px;',
				text : '说明:  如充值实收  100.00元  可当  150.00元  (基本金额100.00,赠送金额50.00)使用,则充值比率为  1:1.5,充值比率输入  1.5, 默认比率为1 '
			}, {
				xtype : 'numberfield',
				id : 'numExchangeRate',
				fieldLabel : '积分比率' + Ext.ux.txtFormat.xh,
				value : 1.00,
				minValue : 0.00,
				minText : '积分比率最小为 0.00.',
				allowBlank : false,
				blankText : '积分比率不能为空.',
				selectOnFocus : true,
				disabled : true
			}, {
				xtype : 'label',
				style : 'color:green;font-szie:12px;',
				text : '说明:  如充值实收  100.00元 可赠送  130.00分, 则比率 1:1.3, 积分比率输入1.3, 比率为0则不赠送, 默认比率为1 '
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
				xtype : 'label',
				style : 'color:green;font-szie:12px;',
				text : '说明:  折扣类型, 选择折扣方案结账时则根据已选方案相关设置进行, 选择全单折扣结账时则该账单所有菜品按统一折扣'
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
				fieldLabel : '折扣率' + Ext.ux.txtFormat.xh,
				maxValue : 1,
				maxText : '折扣率最大为 1,1为不打折',
				minValue : 0.00,
				minText : '折扣率最小为 0.00',
				allowBlank : false,
				blankText : '折扣率不能为空.',
				selectOnFocus : true
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
				
				var odid = Ext.ux.getSelData(memberTypeGrid)['discount.id'];
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
				
				var save = Ext.getCmp('btnSaveMemberType');
				var close = Ext.getCmp('btnCloseMemberType');
				
				save.setDisabled(true);
				close.setDisabled(true);
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
						save.setDisabled(false);
						close.setDisabled(false);
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.decode(res.responseText));
						save.setDisabled(false);
						close.setDisabled(false);
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
};

/**
 * 
 */
memberTypeInit = function(){
	getOperatorName(pin, '../../');
	memberTypeWinInit();
};

