var co_memberCard = new Ext.form.NumberField({
	width : 100,
	inputType : 'password',
	style : 'text-align:left; font-weight: bold; color: #FF0000;',
	maxLength : 10,
	maxLengthText : '请输入10位会员卡号',
	minLength : 10,
	minLengthText : '请输入10位会员卡号',
	allowBlank : false,
	blankText : '会员卡不能为空, 请刷卡.'
});

var checkOutMainPanelTbar = new Ext.Toolbar({
	height : 26,
	items : [{
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;&nbsp;折扣方案:&nbsp;&nbsp;'
	}, {
		xtype : 'combo',
		id : 'comboDiscount',
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
				calcDiscountID = thiz.getValue();
				refreshCheckOutData();
			}
		}
	}, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;价格方案:&nbsp;&nbsp;'
	}, {
		xtype : 'combo',
		id : 'comboPricePlan',
		labelStyle : 'font-size:14px;font-weight:bold;',
		readOnly : true,
		forceSelection : true,
		store : new Ext.data.JsonStore({
			root : 'root',
			fields : ['id', 'name', 'statusValue', 'foodPricePlan']
		}),
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			select : function(thiz, record, index) {
				calcPricePlanID = thiz.getValue();
				refreshCheckOutData();
			}
		}
	}]
});

var checkOutMainPanel = new Ext.Panel({
	title : '&nbsp;',
	width : 1000,
	style : 'margin:0 auto',
	tbar : checkOutMainPanelTbar,
	layout : 'fit',
	frame : true,
	items : [new Ext.Panel({
		xtype : 'panel',
		hidden : true
	})]
});

var checkOutForm = new Ext.form.FormPanel({
	frame : true,
	border : false,
	items : [
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
		text : '会员结账',
		disabled : true,
		handler : function() {
			memberPay();
		}
	},{
		text : '现金结账',
		disabled : true,
		handler : function() {
			paySubmit(1);
		}
	}, {
		text : '刷卡结账',
		disabled : true,
		handler : function() {
			paySubmit(2);
		}
	}, {
		text : '会员卡结账',
		hidden : true,
		handler : function() {
			paySubmit(3);
		}
	}, {
		text : '签单',
		disabled : true,
		handler : function() {
			paySubmit(4);
		}
	}, {
		text : '挂账',
		disabled : true,
		handler : function() {
			paySubmit(5);
		}
	}, {
		text : '暂结',
		disabled : true,
		handler : function() {
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
//			thiz.findById('placeHolderCOF1').setWidth((thiz.getInnerWidth() - 1000) / 2);
//			thiz.findById('placeHolderCOF2').setWidth((thiz.getInnerWidth() - 989) / 2);
			thiz.findById('placeHolderCOF3').setWidth((thiz.getInnerWidth() - 989) / 2);
			thiz.findById('placeHolderCOF4').setWidth((thiz.getInnerWidth() - 989) / 2);
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
		/*
		checkOutColumnModel = new Ext.grid.ColumnModel([
			new Ext.grid.RowNumberer(), {
				header : '菜名',
				dataIndex : 'displayFoodName',
				width : 230
			}, {
				header : '口味',
				dataIndex : 'tastePref',
				width : 230
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
			}, 
			{
				header : '服务员',
				dataIndex : 'waiter'
			}
		]);                                                	
		checkOutStore = new Ext.data.Store({
			autoLoad : false,
			proxy : new Ext.data.MemoryProxy(),
			reader : new Ext.data.JsonReader(Ext.ux.readConfig, 
			[{
				name : 'aliasID'
			}, {
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
			}, {
				name : 'kitchenID'	
			}]),
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
			style : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
			autoScroll : true,
			width : 988,
			height : checkOutMainPanel.getInnerHeight(),
			ds : checkOutStore,
			cm : checkOutColumnModel
		});
		*/
		checkOutGrid = createGridPanel(
			'',
			'',
			checkOutMainPanel.getInnerHeight(),
		    988,
		    '',
		    [
			    [true, false, false, false], 
			    ['菜名', 'displayFoodName', 230] , 
			    ['口味', 'tasteGroup.tastePref', 130] , 
			    ['口味价钱', 'tasteGroup.tastePrice', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['数量', 'count', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['单价', 'unitPrice', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['折扣率', 'discount', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['总价', 'totalPrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['时间', 'orderDateFormat', 130],
			    ['服务员', 'waiter', 80]
			],
			OrderFoodRecord.getKeys(),
		    [['restaurantID', restaurantID]],
		    30,
		    ''
		);
		checkOutGrid.style = 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;';
		checkOutGrid.getStore().on('load', function(thiz, records){
			if(checkOutGrid.isVisible()){
				for(var i = 0; i < records.length; i++){
					Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name');
					if(i % 2 == 0){
						checkOutGrid.getView().getRow(i).style.backgroundColor = '#DDD';
					}
				}				
			}
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
	checkOutForm.buttons[7].setDisabled(_s);
};
