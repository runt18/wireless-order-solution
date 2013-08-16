/**********************************************************************/
function discountTypeRenderer(val){
	for(var i = 0; i < discountTypeData.length; i++){
		if(eval(discountTypeData[i][0] == val)){
			return discountTypeData[i][1];
		}
	}
};
function discountRateRenderer(val, m, r){
	if(r.get('discountTypeValue') == 0){
		return '--';
	}else{
		return Ext.ux.txtFormat.gridDou(val);
	}
};
function discountRenderer(val, m, r){
	if(r.get('discountTypeValue') == 1){
		return '--';
	}else{
		return val;
	}
};
function memberAttributeRenderer(val){
	for(var i = 0; i < memberAttributeData.length; i++){
		if(eval(memberAttributeData[i][0] == val)){
			return memberAttributeData[i][1];
		}
	}
};
function memberTypeRenderer(){
	return ''
		   + '<a href="javascript:updateMemberTypeHandler()">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:deleteMemberTypeHandler()">删除</a>';
};

function dataInit(){
	Ext.Ajax.request({
		url : '../../QueryDiscountTree.do',
		params : {
			restaurantID : restaurantID,
			
		},
		success : function(res, opt){
			discountData = eval(res.responseText);
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
};
/**
 * 
 */
function memberTypeWinInit(){
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
				xtype : 'numberfield',
				id : 'numExchangeRate',
				fieldLabel : '积分比率' + Ext.ux.txtFormat.xh,
				value : 1,
				minValue : 0.00,
				allowBlank : false,
				blankText : '积分比率不能为空.',
				selectOnFocus : true
			}, {
				xtype : 'label',
				style : 'color:green;font-szie:12px;',
				text : '说明:  使用会员结账时, 消费金额兑换积分的利率, 如金额 100 元兑换 150 积分, 则输入 1.5, 默认 1.'
			}, {
				xtype : 'numberfield',
				id : 'numInitialPoint',
				fieldLabel : '初始积分' + Ext.ux.txtFormat.xh,
				value : 1,
				minValue : 0.00,
				allowBlank : false,
				blankText : '初始积分不能为空.',
				selectOnFocus : true
			}, {
				xtype : 'label',
				style : 'color:green;font-szie:12px;',
				text : '说明:  新会员赠送积分.'
			}, {
				xtype : 'combo',
				id : 'comboAttribute',
				fieldLabel : '会员属性' + Ext.ux.txtFormat.xh,
				readOnly : true,
				forceSelection : true,
				width : 130,
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
						if(e.getValue() == 0){
							cr.setValue(1);
							cr.setDisabled(false);
						}else if(e.getValue() == 1){
							cr.setValue(0);
							cr.setDisabled(true);
						}
						cr.clearInvalid();
					}
				}
			}, {
				xtype : 'label',
				style : 'color:green;font-szie:12px;',
				text : '说明:  所有属性都可使用积分功能, 积分类型只使用该会员类型的折扣信息, 充值则可使用会员资料中基本金额、赠送金额等更多信息'
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
				text : '说明:  如充值实收  100.00元  可当  150.00元  (基本金额100.00,赠送金额50.00),则充值比率为  1:1.5,充值比率输入  1.5, 默认 1 '
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
				var initialPoint = Ext.getCmp('numInitialPoint');
				var discountType = Ext.getCmp('comboDiscountType');
				var discount = Ext.getCmp('comboDiscount');
				var discountRate = Ext.getCmp('numDiscountRate');
				var attribute = Ext.getCmp('comboAttribute');
				
				var odid = Ext.ux.getSelData(memberTypeGrid)['discount.id'];
				
				if(!typeName.isValid() || !chargeRate.isValid() || !exchangeRate.isValid() 
						|| !initialPoint.isValid() || !discountType.isValid() || !attribute.isValid()){
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
					url : '../../OperateMemberType.do',
					params : {
						dataSource : memberTypeWin.otype.toLowerCase(),
						restaurantID : restaurantID,
						typeID : typeID.getValue(),
						typeName : typeName.getValue(),
						discountID : discountType.getValue() == 0 ? discount.getValue() : odid,
						discountRate : discountRate.getValue(),
						discountType : discountType.getValue(),
						exchangeRate : exchangeRate.getValue(),
						initialPoint : initialPoint.getValue(),
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
function initMemberTypeGrid(){
	var memberTypeGridTbar = new Ext.Toolbar({
		items : [{
			xtype: 'tbtext',
			text : '过滤:'
		}, {
			xtype : 'combo',
			id : 'comboSearchType',
			readOnly : true,
			forceSelection : true,
			width : 80,
			value : 0,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[0, '全部'], [1, '类型名称'], [2, '折扣方式'], [3, '会员属性']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(thiz, record, index){
					var searchType = Ext.getCmp('txtSearchTypeName');
					var searchDiscountType = Ext.getCmp('comboSearchDiscountType');
					var searchAttribute = Ext.getCmp('comboSearchMemberAttribute');
					
					if(index == 0){
						searchType.setVisible(false);
						searchDiscountType.setVisible(false);
						searchAttribute.setVisible(false);
						mtObj.searchValue = null;
					}else if(index == 1){
						searchType.setVisible(true);
						searchDiscountType.setVisible(false);
						searchAttribute.setVisible(false);
						searchType.setValue();
						mtObj.searchValue = searchType.getId();
					}else if(index == 2){
						searchType.setVisible(false);
						searchDiscountType.setVisible(true);
						searchAttribute.setVisible(false);
						searchDiscountType.setValue(0);
						mtObj.searchValue = searchDiscountType.getId();
					}else if(index == 3){
						searchType.setVisible(false);
						searchDiscountType.setVisible(false);
						searchAttribute.setVisible(true);
						searchAttribute.setValue(0);
						mtObj.searchValue = searchAttribute.getId();
					}
					
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		}, {
			xtype : 'textfield',
			id : 'txtSearchTypeName',
			width : 150,
			hidden : true
		}, {
			xtype : 'combo',
			id : 'comboSearchDiscountType',
			width : 150,
			hidden : true,
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
			selectOnFocus : true
		}, {
			xtype : 'combo',
			id : 'comboSearchMemberAttribute',
			width : 150,
			hidden : true,
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
			selectOnFocus : true
		},
		'->', 
		{
			text : '搜索',
			id : 'btnSearchMemberType',
			iconCls : 'btn_search',
			handler : function(e){
				var searchType = Ext.getCmp('comboSearchType').getValue();
				var searchValue = Ext.getCmp(mtObj.searchValue);
				
				var gs = memberTypeGrid.getStore();
				gs.baseParams['name'] = searchType == 1 ? searchValue.getValue() : '';
				gs.baseParams['discountType'] = searchType == 2 ? searchValue.getValue() : '';
				gs.baseParams['attr'] = searchType == 3 ? searchValue.getValue() : '';
				
				gs.load();
			}
		}, {
			text : '添加',
			id : 'btnInsertMemberType',
			iconCls : 'btn_add',
			handler : function(e){
				insertMemberTypeHandler();
			}
		}, {
			text : '修改',
			id : 'btnUpdateMemberType',
			iconCls : 'btn_edit',
			handler : function(e){
				updateMemberTypeHandler();
			}
		}, {
			text : '删除',
			id : 'btnDeleteMemberType',
			iconCls : 'btn_delete',
			handler : function(e){
				deleteMemberTypeHandler();
			}
		}]
	});

	memberTypeGrid = createGridPanel(
		'memberTypeGrid',
		'',
		'',
		'',
		'../../QueryMemberType.do',
		[
			[true, false, false, false], 
			//['类型编号', 'typeID'],
			['类型名称', 'name'],
			['会员属性', 'attributeValue',,, 'memberAttributeRenderer'],
			['充值比率', 'chargeRate',,'right', 'function(v, md, r){ if(r.get("attributeValue") == 0){ return Ext.ux.txtFormat.gridDou(v); }else{ return "-"; } }'],
			['初始积分', 'initialPoint',,'right', 'Ext.ux.txtFormat.gridDou'],
			['积分比率', 'exchangeRate',,'right', 'Ext.ux.txtFormat.gridDou'],
			['折扣方式', 'discountTypeText'],
			['折扣率', 'discountRate',,'right', 'discountRateRenderer'],
			['折扣方案', 'discount.name',,, 'discountRenderer'],
			['操作', 'operation', 200, 'center', 'memberTypeRenderer']
		],
		['id','name','chargeRate','exchangeRate','discountTypeValue', 'discountTypeText','discountRate','attributeValue',
		 'discount', 'discount.id', 'discount.name', 'discount.status', 'initialPoint'],
		[ ['isPaging', true], ['restaurantID', restaurantID], ['dataSource', 'normal']],
		30,
		'',
		memberTypeGridTbar
	);	
	memberTypeGrid.region = 'center';
	memberTypeGrid.on('render', function(thiz){
		Ext.getCmp('btnSearchMemberType').handler();
	});
	memberTypeGrid.on('rowdblclick', function(){
		updateMemberTypeHandler();
	});
	memberTypeGrid.keys = [{
		key : Ext.EventObject.ENTER,
		fn : function(){
			Ext.getCmp('btnSearchMemberType').handler();
		},
		scope : this
	}];
}