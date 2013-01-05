var memberNbrInputWin = new Ext.Window({
	layout : 'fit',
	width : 240,
	height : 100,
	closeAction : 'hide',
	buttonAlign : 'center',
	resizable : false,
	items : [ {
		layout : 'form',
		labelWidth : 60,
		border : false,
		frame : true,
		items : [ {
			xtype : 'numberfield',
			fieldLabel : '会员证号',
			id : 'memberNbrInput',
			width : 140
		} ]
	} ],
	buttons : [{
		text : '确定',
		handler : function() {
			var memberNbr = memberNbrInputWin.findById('memberNbrInput').getValue();
			memberNbrInputWin.findById('memberNbrInput').setValue('');
			if (memberNbr != '') {
				getMemberInfo(memberNbr);
				checkOutForm.buttons[2].show();
			}
		}
	}, {
		text : '取消',
		handler : function() {
			memberNbrInputWin.hide();
			checkOutForm.buttons[2].hide();
			memberNbrInputWin.findById('memberNbrInput').setValue('');
		}
	}],
	listeners : {
		beforehide : function(thiz) {
			if (!checkOutForm.findById('memberInfoPanel').isVisible()) {
				discountKindComb.setValue('一般');
				checkOurListRefresh();
				actualMemberID = -1;
			}
		}
	}
});

// membership select comb
var discountKindComb = new Ext.form.ComboBox({
	fieldLabel : '结账方式',
	labelStyle : 'font-size:14px;font-weight:bold;',
	disabled : true,
	readOnly : true,
	forceSelection : true,
	value : '一般',
	id : 'payTpye',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : [[ '0', '一般' ], [ '1', '会员' ]]
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	allowBlank : false,
	listeners : {
		select : function(combo, record, index) {
			if (record.get('text') == '一般') {
				mBalance = -1;
				checkOurListRefresh();
				checkOutForm.buttons[2].hide();
				checkOutForm.findById('memberInfoPanel').hide();
			} else {
				memberNbrInputWin.show();
			}
		}
	}
});

var checkOutMainPanel = new Ext.Panel({
	width : 1000,
	style : 'margin:0 auto',
//	autoScroll : true,
	layout : 'fit',
	items : [new Ext.Panel({
		xtype : 'panel',
		hidden : true
	})]
});

var checkOutForm = new Ext.form.FormPanel({
	frame : true,
	border : false,
	items : [ {
		layout : 'column',
		border : false,
		items : [ {
			html : '<div>&nbsp;&nbsp;</div>',
			id : 'placeHolderCOF1',
			width : 150
		}, {
			layout : 'form',
			border : false,
			labelSeparator : '：',
			labelWidth : 30,
			width : 300,
			items : [ discountKindComb ]
		}, {
			layout : 'form',
			border : false,
			width : 300,
			items : [{
				xtype : 'combo',
				id : 'comboDiscount',
				fieldLabel : '折扣方案',
				labelStyle : 'font-size:14px;font-weight:bold;',
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
					select : function(thiz, record, index) {
//						checkOurListRefresh();
						calcDiscountID = thiz.getValue();
						refreshCheckOutData();
					}
				}
			}]
		}, {
			layout : 'form',
			border : false,
			width : 300,
			items : [{
				xtype : 'combo',
				id : 'comboPricePlan',
				fieldLabel : '价格方案',
				labelStyle : 'font-size:14px;font-weight:bold;',
				readOnly : true,
				forceSelection : true,
				store : new Ext.data.JsonStore({
					root : 'root',
					fields : ['id', 'name', 'status', 'items']
				}),
				valueField : 'id',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				listeners : {
					select : function(thiz, record, index) {
//						checkOurListRefresh();
						calcPricePlanID = thiz.getValue();
						refreshCheckOutData();
					}
				}
			}]
		}]
	}, {
		layout : 'column',
		border : false,
		items : [ {
			html : '<div>&nbsp;&nbsp;</div>',
			id : 'placeHolderCOF2',
			hidden : true,
			width : 150
		}, {
			layout : 'fit',
			id : 'memberInfoPanel',
			width : 980,
			contentEl : 'memberInfo',
			hidden : true,
			listeners : {
				hide : function(thiz) {
					checkOutForm.findById('placeHolderCOF2').hide();
				},
				show : function(thiz) {
					checkOutForm.findById('placeHolderCOF2').show();
				}
			}
		} ]
	}, 
	checkOutMainPanel, 
	{
		layout : 'column',
		border : false,
		items : [ {
			html : '<div>&nbsp;&nbsp;</div>',
			id : 'placeHolderCOF3',
			width : 150
		}, {
			border : false,
			contentEl : 'payInfo'
		} ]
	}, {
		layout : 'column',
		border : false,
		items : [ {
			html : '<div>&nbsp;&nbsp;</div>',
			id : 'placeHolderCOF4',
			width : 150
		}, {
			layout : 'form',
			border : false,
			labelSeparator : '：',
			labelWidth : 60,
			width : 150,
			items : [ {
				xtype : 'numberfield',
				id : 'numCustomNum',
				fieldLabel : '就餐人数',
				minValue : 1,
				width : 57,
				disabled : true
			} ]
		}, {
			layout : 'form',
			border : false,
			labelSeparator : '：',
			labelWidth : 40,
			width : 850,
			items : [ {
				xtype : 'textfield',
				fieldLabel : '备注',
				id : 'remark',
				anchor : '%100'
			} ]
		} ]
	} ],
	buttons : [ {
		text : '现金结账',
		disabled : true,
		handler : function() {
			setFormButtonStatus(true);
			paySubmit(1);
		}
	}, {
		text : '刷卡结账',
		disabled : true,
		handler : function() {
			setFormButtonStatus(true);
			paySubmit(2);
		}
	}, {
		text : '会员卡结账',
		hidden : true,
		handler : function() {
			setFormButtonStatus(true);
			paySubmit(3);
		}
	}, {
		text : '签单',
		disabled : true,
		handler : function() {
			setFormButtonStatus(true);
			paySubmit(4);
		}
	}, {
		text : '挂账',
		disabled : true,
		handler : function() {
			setFormButtonStatus(true);
			paySubmit(5);
		}
	}, {
		text : '暂结',
		disabled : true,
		handler : function() {
			setFormButtonStatus(true);
			paySubmit(6);
		}
	}, {
		text : '返回',
		disabled : true,
		handler : function() {
			var Request = new URLParaQuery();
			location.href = 'TableSelect.html?pin=' + Request['pin'] + '&restaurantID=' + restaurantID;
		}
	}],
	listeners : {
		afterlayout : function(thiz) {
			thiz.findById('placeHolderCOF1').setWidth((thiz.getInnerWidth() - 1000) / 2);
			thiz.findById('placeHolderCOF2').setWidth((thiz.getInnerWidth() - 1000) / 2);
			thiz.findById('placeHolderCOF3').setWidth((thiz.getInnerWidth() - 1000) / 2);
			thiz.findById('placeHolderCOF4').setWidth((thiz.getInnerWidth() - 1000) / 2);
			checkOutMainPanel.setHeight(thiz.getInnerHeight() - gridHeightOffset);
			if(eval(category == 4)){
				if(tableGroupTab != null && typeof tableGroupTab != 'undefined'){
					tableGroupTab.setHeight(checkOutMainPanel.getInnerHeight());					
				}
			}else{
				if(checkOutGrid != null && typeof checkOutGrid != 'undefined'){
					checkOutGrid.setHeight(checkOutMainPanel.getInnerHeight());					
				}
			}
			checkOutMainPanel.doLayout();	
		}
	}
});

var checkOutCenterPanel = new Ext.Panel({
	title : '&nbsp;',
	region : 'center',
	id : 'checkOutCenterPanel',
	layout : 'fit',
	items : [ checkOutForm ]
});

Ext.onReady(function() {
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();
	
	var centerPanelCO = new Ext.Panel({
		id : 'centerPanelDO',
		region : 'center',
		border : false,
		margins : '0 0 0 0',
		layout : 'border',
		items : [ checkOutCenterPanel ]
	});
	
	new Ext.Viewport({
		layout : 'border',
		items : [{
			region : 'north',
		    bodyStyle : 'background-color:#DFE8F6;',
			html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4><div id="optName" class="optName"></div>',
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},
		centerPanelCO,
		{
			region : 'south',
			height : 30,
			layout : 'form',
			frame : true,
			border : false,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	});
	
	if(eval(category == 4)){
		tableGroupTab = new Ext.TabPanel({
			frame : true,
			enableTabScroll : true,
			height : checkOutMainPanel.getInnerHeight(),
			activeTab : 0,
			listeners : {
				tabchange : function(thiz, stab){
					for(var i = 0; i < stab.getStore().getCount(); i++){
						if(i % 2 == 0){
							stab.getView().getRow(i).style.backgroundColor = '#DDD';
						}
					}
				}
			}
		});
		checkOutMainPanel.add(tableGroupTab);
		checkOutMainPanel.doLayout();
	}else{
		checkOutColumnModel = new Ext.grid.ColumnModel([
			new Ext.grid.RowNumberer(), {
				header : '菜名',
				dataIndex : 'displayFoodName',
				width : 230
			}, {
				header : '口味',
				dataIndex : 'tastePref',
				width : 130
			}, {
				header : '口味价钱',
				dataIndex : 'tastePrice',
				width : 70,
				align : 'right',
				renderer : Ext.ux.txtFormat.gridDou
			}, {
				header : '数量',
				dataIndex : 'count',
				width : 70,
				align : 'right',
				renderer : Ext.ux.txtFormat.gridDou
			}, {
				header : '单价',
				dataIndex : 'unitPrice',
				width : 70,
				align : 'right',
				renderer : Ext.ux.txtFormat.gridDou
			}, {
				header : '折扣率',
				dataIndex : 'discount',
				width : 70,
				align : 'right',
				renderer : Ext.ux.txtFormat.gridDou
			}, {
				header : '总价',
				dataIndex : 'totalPrice',
				width : 80,
				align : 'right',
				renderer : Ext.ux.txtFormat.gridDou
			}, {
				header : '时间',
				dataIndex : 'orderDateFormat',
				width : 130
			}, {
				header : '服务员',
				dataIndex : 'waiter',
				width : 80
			}
		]);                                                	
		checkOutStore = new Ext.data.Store({
			autoLoad : false,
			proxy : new Ext.data.MemoryProxy(),
			reader : new Ext.data.JsonReader(Ext.ux.readConfig, 
				[{
					name : 'displayFoodName'
				}, {
					name : 'foodName'
				}, {
					name : 'tastePref'
				}, {
					name : 'tastePrice'
				}, {
					name : 'count'
				}, {
					name : 'unitPrice'
				}, {
					name : 'discount'
				}, {
					name : 'totalPrice'
				}, {
					name : 'orderDateFormat'
				}, {
					name : 'waiter'
				}, {
					name : 'special'
				}, {
					name : 'recommend'
				}, {
					name : 'stop'
				}, {
					name : 'gift'
				}, {
					name : 'weight'
				}, {
					name : 'currPrice'
				}, {
					name : 'combination'
				}, {
					name : 'temporary'
				}, {
					name : 'tmpTastePrice'
				}]
			),
			listeners : {
				load : function(thiz, records){
					if(checkOutGrid.isVisible()){
						for(var i = 0; i < records.length; i++){
							Ext.ux.formatFoodName(records[i], 'displayFoodName', 'foodName');
							if(i % 2 == 0){
								checkOutGrid.getView().getRow(i).style.backgroundColor = '#DDD';//FFE4B5
							}
						}				
					}
				}
			}
		});
		checkOutGrid = new Ext.grid.GridPanel({
			title : '账单列表',
			border : true,
			frame : true,
			autoScroll : true,
			width : 1000,
			height : checkOutMainPanel.getInnerHeight(),
			ds : checkOutStore,
			cm : checkOutColumnModel
		});
		// 加载界面
		checkOutMainPanel.add(checkOutGrid);
		checkOutMainPanel.doLayout();
	}
	
});

setFormButtonStatus = function(_s){
	checkOutForm.buttons[0].setDisabled(_s);
	checkOutForm.buttons[1].setDisabled(_s);
	checkOutForm.buttons[2].setDisabled(_s);
	checkOutForm.buttons[3].setDisabled(_s);
	checkOutForm.buttons[4].setDisabled(_s);
	checkOutForm.buttons[5].setDisabled(_s);
	checkOutForm.buttons[6].setDisabled(_s);
};
