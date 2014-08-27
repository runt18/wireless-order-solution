//--------------load---------
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
function memberTypeRenderer(v,m,r){
	//微信会员不可删除
	if(r.get('type') == 2){
		return ''
	   + '<a href="javascript:updateMemberTypeHandler()">修改</a>';
	}else{
		return ''
	   + '<a href="javascript:updateMemberTypeHandler()">修改</a>'
	   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
	   + '<a href="javascript:deleteMemberTypeHandler()">删除</a>';
	}

};

function dataInit(){
	Ext.Ajax.request({
		url : '../../QueryDiscountTree.do',
		params : {
			restaurantID : restaurantID
			
		},
		success : function(res, opt){
			discountData = eval(res.responseText);

		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
};

function getChecked(checkeds, checkBoxs){
	for (var i = 0; i < checkBoxs.length; i++) {
		if(checkBoxs[i].checked){
			if(checkeds == ""){
				checkeds += checkBoxs[i].value;
			}else{
				checkeds += "," + checkBoxs[i].value;
			}
		}
	}
	return checkeds;
}

function checkLabel(t){
	if(t.length > 5){
		var after = t.substring(0, 5);
		return after;
	}else{
		return t;
	}
}

/**
 * 
 */
function memberTypeWinInit(){
	if(!memberTypeWin){
		memberTypeWin = new Ext.Window({
			id : 'mtm_memberTypeWin',
			title : '会员类型',
			closable : false,
			modal : true,
			resizable : false,
			width : 275,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				width : 275,
				labelWidth : 70,
				labelAlign : 'right',
				defaults : {
					width : 160
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
					value : "",
					selectOnFocus : true
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
					width : 200,
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
					autoWidth : true,
					style : 'color:green;font-szie:12px;width : 250px',
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
					autoWidth : true,
					style : 'color:green;font-szie:12px;',
					text : '说明:充100.00元送50.00元, 充值比率输入  1.5, 默认 1 '
				}, {
					xtype : 'textarea',
					height : 40,
					id : 'txtCommentForMemberType',
					fieldLabel : '特权说明'
				},{
					xtype : 'panel',
					layout : 'column',
					id : 'formMemberDiscount',
					frame : true,
					width : 255,
					defaults : {
						columnWidth : .333,
						layout : 'form',
						labelWidth : 80
					},
					items : [{
						columnWidth : 1,
						xtype : 'label',
						id : 'txtTest',
						style : 'text-align:left;padding-bottom:3px;',
						text : '折扣方案:'
						
					}]
					
				},{
					xtype : 'combo',
					id : 'comboDiscount',
					fieldLabel : '默认方案' + Ext.ux.txtFormat.xh,
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
					selectOnFocus : true,
					listeners : {
						focus  : function(thiz){
							var mDiscountSelectedList = []; 
							var mDiscountSelecteds = document.getElementsByName('memberDiscount');
							for (var i = 0; i < mDiscountSelecteds.length; i++) {
								if(mDiscountSelecteds[i].checked){
									mDiscountSelectedList.push({'discountID':mDiscountSelecteds[i].value,'text':mDiscountSelecteds[i].nextSibling.innerHTML});
								}
								
							}
							thiz.store.loadData(mDiscountSelectedList);
						}
					}
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
					var discount = Ext.getCmp('comboDiscount');
					var attribute = Ext.getCmp('comboAttribute');
					var desc = Ext.getCmp('txtCommentForMemberType');
					
					if(!typeName.isValid() || !chargeRate.isValid() || !exchangeRate.isValid() 
							|| !initialPoint.isValid() || !attribute.isValid() || !discount.isValid()){
						return;
					}
					var memberDiscountCheckeds = "";
					
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
							discountID : discount.getValue(),
							exchangeRate : exchangeRate.getValue(),
							initialPoint : initialPoint.getValue(),
							chargeRate : chargeRate.getValue(),
							attr : attribute.getValue(),
							desc : desc.getValue(),
							memberDiscountCheckeds : getChecked(memberDiscountCheckeds, document.getElementsByName('memberDiscount'))
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
					Ext.getCmp('txtTypeName').enable();
				}
			}],
			listeners : {
				beforeshow : function(e){
					if(document.getElementsByName('memberDiscount').length == 0){
						for (var i = 0; i < discountData.length; i++) {
							var c = {items : [{
								xtype : "checkbox", 
								name : "memberDiscount",
								boxLabel : checkLabel(discountData[i].text) , 
								hideLabel : true, 
								inputValue :  discountData[i].discountID,
								listeners : {
									focus : function(){
										Ext.getCmp('comboDiscount').setValue();
										Ext.getCmp('comboDiscount').clearInvalid();
									},
									check : function(){
										Ext.getCmp('comboDiscount').setValue();
										Ext.getCmp('comboDiscount').clearInvalid();
									}
								}
							}]};
							Ext.getCmp('formMemberDiscount').add(c);
							//solveIE自动换行时格式错乱
							if((i+1)%6 == 0){
								Ext.getCmp('formMemberDiscount').add({columnWidth : 1});
							}
							Ext.getCmp('formMemberDiscount').doLayout();
						}
					}
				},
				hide : function(){
					var discounts = document.getElementsByName('memberDiscount');
					for (var i = 0; i < discounts.length; i++) {
						if(discounts[i].checked){
							discounts[i].checked = false;
						}
					}
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
			id : 'mtm_comboSearchType',
			readOnly : false,
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
			readOnly : false,
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
			readOnly : false,
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
				var searchType = Ext.getCmp('mtm_comboSearchType').getValue();
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
			['折扣方案', 'discount.name',,, 'discountRenderer'],
			['操作', 'operation', 200, 'center', 'memberTypeRenderer']
		],
		['id','name','chargeRate','exchangeRate','discountTypeValue', 'discountTypeText','type','attributeValue',
		 'desc', 'discount', 'discount.id', 'discount.name', 'discount.status', 'initialPoint', 'discounts'],
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
//---------
/**********************************************************************/
function insertMemberTypeHandler(){
	memberTypeOperationHandler({
		type : mtObj.operation['insert']
	});
};
function updateMemberTypeHandler(){
	memberTypeOperationHandler({
		type : mtObj.operation['update']
	});
};
function deleteMemberTypeHandler(){
	memberTypeOperationHandler({
		type : mtObj.operation['delete']
	});
};

function memberTypeOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	memberTypeWin.otype = c.type;
	if(c.type == mtObj.operation['insert']){
		memberTypeWin.setTitle('添加会员类型');
		memberTypeWin.show();
		memberTypeWin.center();
		Ext.getCmp('txtTypeName').focus(true, 100);
		
		bindMemberTypeData({
			discountType : 0,
			chargeRate : 1.00,
			exchangeRate : 1.00,
			initialPoint : 0
		});
		
	}else if(c.type == mtObj.operation['update']){
		var sd = Ext.ux.getSelData(memberTypeGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个会员类型再进行操作.');
			return;
		}
		memberTypeWin.setTitle('修改会员类型');
		memberTypeWin.show();
		
		var discounts = document.getElementsByName('memberDiscount');
		for (var i = 0; i < sd['discounts'].length; i++) {
			for (var j = 0; j < discounts.length; j++) {
				if(sd['discounts'][i].id == discounts[j].value){
					
					discounts[j].checked = true;
				}
			}
		}
		var mDiscountSelectedList = []; 
		var mDiscountSelecteds = document.getElementsByName('memberDiscount');
		for (var i = 0; i < mDiscountSelecteds.length; i++) {
			if(mDiscountSelecteds[i].checked){
				mDiscountSelectedList.push({'discountID':mDiscountSelecteds[i].value,'text':mDiscountSelecteds[i].nextSibling.innerHTML});
			}
			
		}
		Ext.getCmp('comboDiscount').store.loadData(mDiscountSelectedList);
		
		bindMemberTypeData(sd);
		
		memberTypeWin.center();
	}else if(c.type == mtObj.operation['delete']){
		var sd = Ext.ux.getSelData(memberTypeGrid.getId());
		if(!sd){
			Ext.example.msg('提示', '请选中一个会员类型再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '提示',
			msg : '是否删除会员类型?<br><font color="red">提示:如果该类型下已有会员则删除失败.</font>',
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateMemberType.do',
						params : {
							dataSource : 'delete',
							restaurantID : restaurantID,
							
							typeID : sd['id'],
							discountType : sd['discountTypeValue'],
							discountID : sd['discount']['id']
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
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
			}
		});
	}
	
};

function bindMemberTypeData(d){
	var typeID = Ext.getCmp('numTypeID');
	var typeName = Ext.getCmp('txtTypeName');
	var chargeRate = Ext.getCmp('numChargeRate');
	var exchangeRate = Ext.getCmp('numExchangeRate');
	var initialPoint = Ext.getCmp('numInitialPoint');
	var discount = Ext.getCmp('comboDiscount');
	var attribute = Ext.getCmp('comboAttribute');
	var desc = Ext.getCmp('txtCommentForMemberType');
	
	typeID.setValue(d['id']);
	typeName.setValue(d['name']);
	//是微信就不能该名字
	if(d['type'] == 2){
		typeName.disable();
	}
	exchangeRate.setValue(d['exchangeRate']);
	initialPoint.setValue(typeof d['initialPoint'] != 'undefined' ? d['initialPoint'] : 0);
	
	if(typeof d['attributeValue'] == 'undefined'){
		attribute.setValue(1);
	}else{
		attribute.setValue(d['attributeValue']);
	}
	attribute.fireEvent('select', attribute);
	chargeRate.setValue(d['chargeRate']);
	discount.setValue(d['discount.id']);
	desc.setValue(d['desc']);
	
	typeID.clearInvalid();
	typeName.clearInvalid();
	chargeRate.clearInvalid();
	initialPoint.clearInvalid();
	attribute.clearInvalid();
	discount.clearInvalid();
};


/**********************************************************************/
Ext.onReady(function(){
	//
	dataInit();
	//
	initMemberTypeGrid();
	memberTypeWinInit();
	
	new Ext.Panel({
		renderTo : 'divMemberType',
		width : parseInt(Ext.getDom('divMemberType').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMemberType').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [memberTypeGrid],
		autoScroll : true
	});
	 
	

	/*
	var menu = new Ext.menu.Menu({
		items : [{
			text : '新建会员类型',
			handler : function(){
				insertMemberTypeHandler();
			}
		}]
	 });
	Ext.getDoc().on('contextmenu', function(e){
		e.stopEvent();
		menu.showAt(e.getXY());
	});
	*/
});

