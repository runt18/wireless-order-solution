function stockOperateRenderer(){
	return ''
		+ '<a>审核</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a>冲红</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a>删除</a>';
}

function stockDetailTotalPriceRenderer(v, m, r, ri, ci, s){
	return Ext.ux.txtFormat.gridDou(r.get('amount') * r.get('price'));
}
function stockTypeRenderer(v, m, r, ri, ci, s){
	return r.get('typeText') + ' -- ' +r.get('subTypeText');
}
function stockInRenderer(v, m, r, ri, ci, s){
	var display = '', t = r.get('typeValue'), st = r.get('subTypeValue');
	if(t == 1){
		if(st == 1 || st == 2 || st == 3 || st == 7){
			display = r.get('deptIn')['name'];
		}
	}else if(t == 2){
		if(st == 4){
			display = r.get('supplier')['name'];
		}else if(st == 5){
			display = r.get('deptIn')['name'];
		}
	}
	return display;
}

function stockOutRenderer(v, m, r, ri, ci, s){
	var display = '', t = r.get('typeValue'), st = r.get('subTypeValue');
	if(t == 1){
		if(st == 1){
			display = r.get('supplier')['name'];
		}else if(st == 5){
			display = r.get('deptOut')['name'];
		}
	}else if(t == 2){
		display = r.get('deptOut')['name'];
	}
	return display;
}

function initControl(){
	var stockBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '单据类型'
		}, {
			xtype : 'combo',
			id : 'comboSearchForStockType',
			readOnly : true,
			forceSelection : true,
			width : 100,
			value : 1,
			store : new Ext.data.SimpleStore({
				data : [[1, '入库单'], [2, '出库单']],
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false
		}, '->', {
			text : '搜索',
			id : 'btnSearchForStockBasicMsg',
			iconCls : 'btn_search',
			handler : function(e){
				var st = Ext.getCmp('comboSearchForStockType').getValue();
				var gs = stockBasicGrid.getStore();
				gs.baseParams['stockType'] = st;
				gs.load({
					params : {
						start : 0,
						limit : stockBasicGrid.getBottomToolbar().pageSize
					}
				});
			}
		}]
	});
	stockBasicGrid = createGridPanel(
		'stockBasicGrid',
		'',
		'',
		'',
		'../../QueryStockAction.do',
		[
			[true, false, false, true],
			//['货单编号', 'id', 60],
			['货单类型', 'typeText',,,'stockTypeRenderer'],
			['货品类型', 'cateTypeText', 60],
			['原始编号', 'oriStockId'],
			['收货仓/供应商', 'center',,,'stockInRenderer'],
			['出库仓/供应商', 'center',,,'stockOutRenderer'],
			['数量', 'amount',60,'right','Ext.ux.txtFormat.gridDou'],
			['金额', 'price',60,'right','Ext.ux.txtFormat.gridDou'],
			['经办人', 'approverName', 80],
			['制单人', 'operatorName', 80],
			['时间', 'oriStockDateFormat', 130],
			['审核状态', 'statusText', 60, 'center'],
			['操作', 'center', 200, 'center', 'stockOperateRenderer']
		],
		StockRecord.getKeys(),
		[['isPaging', true], ['pin',pin], ['restaurantId', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		stockBasicGridTbar
	);
	stockBasicGrid.region = 'center';
	stockBasicGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnSearchForStockBasicMsg').handler();
		}
	}];
	stockBasicGrid.on('render', function(thiz){
		Ext.getCmp('btnSearchForStockBasicMsg').handler();
	});
	
	var firstStepPanel = new Ext.Panel({
    	mt : '新增货单共二步, <span style="color:#000;">现为第一步:选择单据类型</font>',
        index : 0,
        frame : true,
        items : [{
        	xtype : 'fieldset',
        	title : '入库单',
        	layout : 'column',
        	height : Ext.isIE ? 150 : 138,
        	items : [{
        		columnWidth : .2,
        		html : '&nbsp;'
        	}, {
	        	columnWidth : .15,
	        	xtype : 'fieldset',
	        	title : '商品入库',
	        	height : Ext.isIE ? 100 : 115,
	        	bodyStyle : 'padding:3px 0px 0px 10px; ',
	        	items : [{
	        		xtype : 'radio',
	        		inputValue : [1,1,1],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品采购'
	        	}, {
	        		xtype : 'radio',
	        		inputValue : [1,1,2],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品调拨'
	        	}, {
	        		xtype : 'radio',
	        		inputValue : [1,1,3],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品报溢'
	        	}]
	        }, {
	        	columnWidth : .25,
	        	html : '&nbsp;'
	        }, {
	        	columnWidth : .15,
	        	xtype : 'fieldset',
	        	bodyStyle : 'padding:3px 0px 0px 10px; ',
	        	title : '原料入库',
	        	height : Ext.isIE ? 100 : 115,
	        	items : [{
	        		xtype : 'radio',
	        		inputValue : [1,2,1],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料采购'
	        	}, {
	        		xtype : 'radio',
	        		inputValue : [1,2,2],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料调拨'
	        	}, {
	        		xtype : 'radio',
	        		inputValue : [1,2,3],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料报溢'
	        	}]
	        }]
        }, {
        	xtype : 'fieldset',
        	title : '出库单',
        	layout : 'column',
        	height : Ext.isIE ? 150 : 138,
        	items : [{
        		columnWidth : .2,
        		html : '&nbsp;'
        	}, {
	        	columnWidth : .15,
	        	xtype : 'fieldset',
	        	title : '商品出库',
	        	height : Ext.isIE ? 100 : 115,
	        	bodyStyle : 'padding:3px 0px 0px 10px; ',
	        	items : [{
	        		xtype : 'radio',
	        		inputValue : [2,1,4],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品退货'
	        	}, {
	        		xtype : 'radio',
	        		inputValue : [2,1,5],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品调拨'
	        	}, {
	        		xtype : 'radio',
	        		inputValue : [2,1,6],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品报损'
	        	}]
	        }, {
        		columnWidth : .25,
        		html : '&nbsp;'
        	}, {
	        	columnWidth : .15,
	        	xtype : 'fieldset',
	        	bodyStyle : 'padding:3px 0px 0px 10px; ',
	        	title : '原料出库',
	        	height : Ext.isIE ? 100 : 115,
	        	items : [{
	        		xtype : 'radio',
	        		inputValue : [2,2,4],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料退货'
	        	}, {
	        		xtype : 'radio',
	        		inputValue : [2,2,5],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料调拨'
	        	}, {
	        		xtype : 'radio',
	        		inputValue : [2,2,6],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料报损'
	        	}]
	        }]
        }]
    });
	
	var secondStepPanelNorth = {
		title : '货单基础信息',
    	region : 'north',
    	height : 120,
    	frame : true,
    	items : [{
    		id : 'displayPanelForStockTitle',
    		height : 30,
    		bodyStyle : 'font-size:18px;text-align:center;',
    		html : '-----'
    	}, {
    		xtype : 'panel',
    		layout : 'column',
    		defaults : {
    			xtype : 'form',
    			layout : 'form',
    			style : 'width:218px;',
    			labelWidth : 60,
    			columnWidth : .25,
    			defaults : { width : 120 }
    		},
    		items : [{
    			id : 'displayPanelForDeptIn',
    			items : [{
    				id : 'comboDeptInForStockActionBasic',
    				xtype : 'combo',
    				fieldLabel : '收货仓',
    				readOnly : true,
    				forceSelection : true,
    				width : 103,
    				listWidth : 120,
    				store : new Ext.data.JsonStore({
    					url: '../../QueryDept.do?',
    					baseParams : {
    						dataSource : 'normal',
    						pin : pin
    					},
    					root : 'root',
    					fields : DeptRecord.getKeys()
    				}),
    				valueField : 'id',
    				displayField : 'name',
    				typeAhead : true,
    				mode : 'local',
    				triggerAction : 'all',
    				selectOnFocus : true,
    				allowBlank : false,
    				blankText : '收货仓不允许为空.',
    				listeners : {
    					render : function(thiz){
    						thiz.store.load();
    					}
    				}
    			}]
    		}, {
    			id : 'displayPanelForSupplier',
    			items : [{
    				id : 'comboSupplierForStockActionBasic',
    				xtype : 'combo',
    				fieldLabel : '供应商',
    				readOnly : true,
    				forceSelection : true,
    				width : 103,
    				listWidth : 120,
    				store : new Ext.data.JsonStore({
    					url: '../../QuerySupplier.do?pin='+pin,
    					root : 'root',
    					fields : SupplierRecord.getKeys()
    				}),
    				valueField : 'supplierID',
    				displayField : 'name',
    				typeAhead : true,
    				mode : 'local',
    				triggerAction : 'all',
    				selectOnFocus : true,
    				allowBlank : false,
    				blankText : '供应商不允许为空.',
    				listeners : {
    					render : function(thiz){
    						thiz.store.load();
    					}
    				}
    			}]
    		}, {
    			id : 'displayPanelForDeptOut',
    			items : [{
    				id : 'comboDeptOutForStockActionBasic',
    				xtype : 'combo',
    				fieldLabel : '出货仓',
    				readOnly : true,
    				forceSelection : true,
    				width : 103,
    				listWidth : 120,
    				store : new Ext.data.JsonStore({
    					url: '../../QueryDept.do?',
    					baseParams : {
    						dataSource : 'normal',
    						pin : pin
    					},
    					root : 'root',
    					fields : DeptRecord.getKeys()
    				}),
    				valueField : 'id',
    				displayField : 'name',
    				typeAhead : true,
    				mode : 'local',
    				triggerAction : 'all',
    				selectOnFocus : true,
    				allowBlank : false,
    				blankText : '收货仓不允许为空.',
    				listeners : {
    					render : function(thiz){
    						thiz.store.load();
    					}
    				}
    			}]
    		}, {
    			items : [{
    				id : 'txtOriStockIdForStockActionBasic',
    				xtype : 'textfield',
    				fieldLabel : '原始单号'
    			}]
    		}, {
    			items : [{
    				id : 'datetOriStockDateForStockActionBasic',
    				xtype : 'datefield',
    				width : 103,
    				fieldLabel : '日期',
    				maxValue : new Date(),
    				format : 'Y-m-d',
    				readOnly : true
    			}]
    		}, {
    			columnWidth : 1,
    			style : 'width:100%;',
    			items : [{
    				id : 'txtCommentForStockActionBasic',
    				xtype : 'textfield',
    				width : 774,
    				fieldLabel : '备注'
    			}]
    		}, {
    			items : [{
    				id : 'txtSpproverNameForStockActionBasic',
    				xtype : 'textfield',
    				fieldLabel : '审核人',
    				disabled : true
    			}]
    		}, {
    			items : [{
    				id : 'dateSpproverDateForStockActionBasic',
    				xtype : 'textfield',
    				fieldLabel : '审核日期',
    				disabled : true
    			}]
    		}, {
    			items : [{
    				id : 'txtOperatorNameForStockActionBasic',
    				xtype : 'textfield',
    				fieldLabel : '制单人',
    				disabled : true
    			}]
    		}, {
    			items : [{
    				id : 'dateOperatorDateForStockActionBasic',
    				xtype : 'textfield',
    				fieldLabel : '制单日期',
    				disabled : true
    			}]
    		}]
    	}]
    };
	secondStepPanelCenter = createGridPanel(
		'secondStepPanelCenter',
		'货品列表',
		'',
		'',
		'',
		[
			[true, false, false, false], 
			['品名', 'material.name'],
			['类别名称', 'material.cateName'],
			['数量', 'amount',,'right', 'Ext.ux.txtFormat.gridDou'],
			['单价', 'price',,'right', 'Ext.ux.txtFormat.gridDou'],
			['总价', 'totalPrice',,'right', 'stockDetailTotalPriceRenderer'],
			['操作', '', 180, 'center']
		],
		StockDetailRecord.getKeys(),
		[['isPaging', true], ['pin',pin], ['restaurantId', restaurantID], ['stockStatus', 3]],
		GRID_PADDING_LIMIT_20,
		''
	);
	secondStepPanelCenter.region = 'center';
	
	var secondStepPanelWest = {
        title : '添加货品',
        layout : 'form',
    	region : 'west',
    	frame : true,
    	width : 200,
    	labelWidth : 60,
    	defaults : {
    		width : 120
    	},
    	buttonAlign : 'center',
    	buttons : [{
    		text : '添加',
    		handler : function(e){
    			var material = Ext.getCmp('comboSelectMaterialForStockAction');
    			var amount = Ext.getCmp('numSelectCountForStockAction');
    			var price = Ext.getCmp('numSelectPriceForStockAction');
    			
    			var newRecord = null;
    			for(var i=0, temp=material.store, sv=material.getValue(); i<temp.getCount(); i++){
    				if(temp.getAt(i).get('id') == sv){
    					newRecord = temp.getAt(i);
    					break;
    				}
    			}
    			
    			var detail = secondStepPanelCenter.getStore();
    			var has = false;
    			for(var i=0; i < detail.getCount(); i++){
    				if(detail.getAt(i).get('id') == newRecord.get('id')){
    					detail.getAt(i).set('amount', detail.getAt(i).get('amount') + amount.getValue());
    					detail.getAt(i).set('price', price.getValue());
    					has = true;
    					break;
    				}
    			}
    			if(!has){
    				detail.add(new StockDetailRecord({
    					material : newRecord.data,
    					id : newRecord.get('id'),
    					'material.cateName' : newRecord.get('cateName'),
    					'material.name' : newRecord.get('name'),
    					amount : amount.getValue(),
    					price : price.getValue()
    				}));
    			}
    		}
    	}, {
    		text : '重置',
    		handler : function(e){
    			
    		}
    	}],
    	items : [{
			xtype : 'combo',
			id : 'comboSelectMaterialForStockAction',
			fieldLabel : '货品',
			forceSelection : true,
			width : 103,
			listWidth : 153,
			height : 200,
			maxHeight : 300,
			store : new Ext.data.JsonStore({
				url : '../../QueryMaterial.do',
				baseParams : {
					dataSource : 'normal',
					pin : pin,
					restaurantID : restaurantID
				},
				root : 'root',
				fields : MaterialRecord.getKeys()
			}),
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			tpl:'<tpl for=".">' 
				+ '<div class="x-combo-list-item" style="height:18px;">'
				+ '{cateName} -- {name}'
				+ '</div>'
				+ '</tpl>',
			listeners : {
				beforequery : function(e){ 
					var combo = e.combo; 
					if(!e.forceAll){ 
						var value = e.query; 
						combo.store.filterBy(function(record,id){ 
							var text = record.get(combo.displayField); 
							return (text.indexOf(value)!=-1);
						}); 
						combo.expand(); 
						combo.select(0, true);
						return false; 
					}
				},
				select : function(thiz){
					var newRecord = null;
	    			for(var i=0, temp=thiz.store, sv=thiz.getValue(); i<temp.getCount(); i++){
	    				if(temp.getAt(i).get('id') == sv){
	    					newRecord = temp.getAt(i);
	    					break;
	    				}
	    			}
	    			var price = Ext.getCmp('numSelectPriceForStockAction');
	    			price.setValue(newRecord.get('price'));
				}
			}
		}, {
			id : 'numSelectCountForStockAction',
    		xtype : 'numberfield',
    		fieldLabel : '数量',
    		maxValue : 65535
    	}, {
    		id : 'numSelectPriceForStockAction',
    		xtype : 'numberfield',
    		fieldLabel : '单价'
    	}]
    };
	var secondStepPanelSouth = {
		region : 'south',
		frame : true,
		height : 30,
		bodyStyle : 'font-size:18px;text-align:center;',
		html : '合计:'
	};
	
	var secondStepPanel = new Ext.Panel({
    	mt : '新增货单共二步, <span style="color:#000;">现为第二步:填写单据信息</font>',
        index : 1,
        layout : 'border',
        width : '100%',
        items : [secondStepPanelNorth, secondStepPanelCenter, secondStepPanelWest, secondStepPanelSouth]
    });
	
	stockTaskNavWin = new Ext.Window({
		id : 'stockTaskNavWin',
		title : '新增货单共二步, <span style="color:#000;">现为第一步:选择单据类型</font>',
		width : 900,
		height : 500,
		modal : true,
		closable : false,
		resizable : false,
	    layout : 'card',
	    activeItem : 0,
	    defaults : {
	        border:false
	    },
	    bbar: ['->', {
	    	text : '上一步',
	    	id : 'btnPreviousForStockNav',
	    	iconCls : 'btn_previous',
	    	change : -1,
	    	disabled : true,
	    	handler : function(e){
	    		stockTaskNavHandler(e);
	    	}
	    }, '-', {
    		text : '下一步',
    		id : 'btnNextForStockNav',
    		iconCls : 'btn_next',
    		change : 1,
    		handler : function(e){
    			stockTaskNavHandler(e);
	    	}
    	}, '-', {
    		text : '取消',
    		iconCls : 'btn_cancel',
    		handler : function(){
    			stockTaskNavWin.hide();
    		}
    	}],
    	keys : [{
    		key : Ext.EventObject.ESC,
    		scope : this,
    		fn : function(){
    			stockTaskNavWin.hide();
    		}
    	}],
    	items: [firstStepPanel, secondStepPanel, {
	    	mt : '新增货单共二步, <span style="color:#000;">现为第三步:提交等待审核</font>',
	        index : 2,
	        html: '<h1>Congratulations!</h1><p>Step 2 of 3 - Complete</p>'
	    }],
	    listeners : {
	    	show : function(thiz){
	    		thiz.center();
	    	},
	    	hide : function(thiz){
	    		/***** 重置操作导航 *****/
	    		// 设置默认页
	    		thiz.getLayout().setActiveItem(0);
	    		// 恢复导航按钮
	    		stockTaskNavWin.stockType = null;
	    		var btnPrevious = Ext.getCmp('btnPreviousForStockNav');
	    		var btnNext = Ext.getCmp('btnNextForStockNav');
	    		btnPrevious.setDisabled(true);
	    		btnNext.setDisabled(false);
	    		btnNext.setText('下一步');
	    		// 清空已入库单类型
	    		var sot = Ext.query('input[name=radioStockOrderType]');
	    		for(var i = 0; i < sot.length; i++){
	    			sot[i].checked = false;
	    		}
	    		// 清空单据基础信息
	    		operateStockActionBasic({
	    			otype : Ext.ux.otype['set']
	    		});
	    	}
	    }
	});
}



/**
 * 
 */
function loadData(){
	// 加载供应商数据
//	Ext.Ajax.request({
//		url : '../../QuerySupplier.do',
//		params : {
//			pin : pin,
//			start : 0,
//			limit : 10
//		},
//		success : function(res, opt){
//			var jr = Ext.decode(res.responseText);
//			Supplier = jr;
//		}
//	});
}