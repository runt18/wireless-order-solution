//combo.el.parent().parent().parent().first();
function stockOutTypeRenderer(v, m, r, ri, ci, s){
	var display = '';
	
	return display;
}
function stockInTypeRenderer(v, m, r, ri, ci, s){
	var display = '';
	if(r.get('typeValue') == 1){
		if(r.get('subTypeValue') == 1 || r.get('subTypeValue') == 5){
			display = r.get('supplierName');
		}else{
			display = r.get('deptInName');
		}
	}else if(r.get('typeValue') == 2){
		
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
				if(st == 1){
					stockBasicGrid.getColumnModel().setColumnHeader(4, '出库仓(供应商)');
					stockBasicGrid.getColumnModel().setColumnHeader(5, '收货仓');
				}else{
					stockBasicGrid.getColumnModel().setColumnHeader(4, '收货仓');
					stockBasicGrid.getColumnModel().setColumnHeader(5, '出库仓(供应商)');
				}
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
		'../../QueryStock.do',
		[
			[true, false, false, true],
			['货单编号', 'id', 60],
			['货单类型', 'typeText', 60],
			['原始编号', 'oriStockId'],
			['出库仓(供应商)', 'stockOutTypeRenderer'],
			['收货仓', '',,,'stockInTypeRenderer'],
			['数量', 'amount',60,'right','Ext.ux.txtFormat.gridDou'],
			['金额', 'price',60,'right','Ext.ux.txtFormat.gridDou'],
			['经办人', 'approverName', 80],
			['制单人', 'operatorName', 80],
			['时间', 'oriStockDateFormat', 130],
			['是否审核', 'statusText', 60],
			['操作', '', 200, 'center']
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
	        		inputValue : winParams.st[0][0],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[0][0][2]
	        	}, {
	        		xtype : 'radio',
	        		inputValue : winParams.st[1][0],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[1][0][2]
	        	}, {
	        		xtype : 'radio',
	        		inputValue : winParams.st[2][0],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[2][0][2]
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
	        		inputValue : winParams.st[3][0],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[3][0][2]
	        	}, {
	        		xtype : 'radio',
	        		inputValue : winParams.st[4][0],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[4][0][2]
	        	}, {
	        		xtype : 'radio',
	        		inputValue : winParams.st[5][0],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[5][0][2]
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
	        		inputValue : winParams.st[0][1],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[0][1][2]
	        	}, {
	        		xtype : 'radio',
	        		inputValue : winParams.st[1][1],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[1][1][2]
	        	}, {
	        		xtype : 'radio',
	        		inputValue : winParams.st[2][1],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[2][1][2]
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
	        		inputValue : winParams.st[3][1],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[3][1][2]
	        	}, {
	        		xtype : 'radio',
	        		inputValue : winParams.st[4][1],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[4][1][2]
	        	}, {
	        		xtype : 'radio',
	        		inputValue : winParams.st[5][1],
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : winParams.st[5][1][2]
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
    		height : 30,
    		bodyStyle : 'font-size:18px;text-align:center;',
    		html : 'XXXXXX2单&nbsp;状态'
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
    			items : [{
    				xtype : 'combo',
    				fieldLabel : '供应商',
    				readOnly : true,
    				forceSelection : true,
    				width : 103,
    				listWidth : 120,
    				store : new Ext.data.JsonStore({
    					url: '../../QuerySupplier.do?pin='+pin,
    					root : 'root',
    					fields : ['supplierID', 'name']
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
    			items : [{
    				xtype : 'textfield',
    				fieldLabel : '收货仓'
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				fieldLabel : '原始单号'
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				fieldLabel : '日期'
    			}]
    		}, {
    			columnWidth : 1,
    			style : 'width:100%;',
    			items : [{
    				xtype : 'textfield',
    				width : 774,
    				fieldLabel : '备注'
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				fieldLabel : '审核人',
    				disabled : true
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				fieldLabel : '审核日期',
    				disbled : true
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				fieldLabel : '制单人',
    				disbled : true
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				fieldLabel : '制单日期',
    				disbled : true
    			}]
    		}]
    	}]
    };
	var secondStepPanelCenter = createGridPanel(
		'secondStepPanelCenter',
		'货品列表',
		'',
		'',
		'',
		[
			[true, false, false, false], 
			['品名', 'stock.name'],
			['类别名称', 'stock.cate.name'],
			['数量', 'amount'],
			['单价', 'price'],
			['总价', 'totalPrice'],
			['操作', '', 180, 'center']
		],
		StockDetailRecord.getKeys(),
		[['isPaging', true], ['pin',pin], ['restaurantId', restaurantID], ['stockStatus', 3]],
		GRID_PADDING_LIMIT_20,
		''
	);
	secondStepPanelCenter.region = 'center';
	
	var materialBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			id : 'btnSearchForMaterialBasicGridTbar',
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(){
				materialBasicGrid.getStore().load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		}]
	});
	var materialBasicGrid = createGridPanel(
		'materialBasicGrid',
		'',
		300,
		380,
		'../../QueryMaterial.do',
		[
			[true, false, false, true], 
			['原料名称', 'name', 150],
			['所属类别', 'cateName'],
			['单位成本', 'price']
		],
		['id', 'name', 'cateId', 'cateName', 'stock', 'price', 'statusValue', 'statusText'],
		[['isPaging', true], ['pin',pin], ['restaurantID', restaurantID], ['dataSource', 'normal'], ['cateType', 1]],
		GRID_PADDING_LIMIT_20,
		'',
		materialBasicGridTbar
	);	
	materialBasicGrid.getBottomToolbar().displayMsg = '每页&nbsp;'+materialBasicGrid.getBottomToolbar().pageSize+'&nbsp;条';
	materialBasicGrid.on('rowdblclick', function(){
		
	});
	var tf = new Ext.form.TriggerField({
		readOnly : true,
		fieldLabel : '品项',
		hideOnClick : false,
		menu : new Ext.menu.Menu({
			items : [new Ext.menu.Adapter(materialBasicGrid)],
			listeners : {
				show : function(){
					Ext.getCmp('btnSearchForMaterialBasicGridTbar').handler();
				}
			}
		}),
		onTriggerClick : function(){
			if(!this.disabled){
	    		this.menu.show(this.el, 'tl-bl?');
	    	}
		},
		listeners : {
			render : function(thiz){
				thiz.setWidth(103, true);
			}
		}
	}, {hideClick:false});
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
    			
    		}
    	}, {
    		text : '重置',
    		handler : function(e){
    			
    		}
    	}],
    	items : [tf, {
    		xtype : 'numberfield',
    		fieldLabel : '数量'
    	}, {
    		xtype : 'numberfield',
    		fieldLabel : '单价'
    	}]
    };
	var secondStepPanelSouth = {
		region : 'south',
		frame : true,
		height : 30,
		style : 'text-align:center;',
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
    			var nav = Ext.getCmp('stockTaskNavWin').getLayout().activeItem;
    			if(nav.index >= 1){
    				alert('完成提交......');
    			}else{
    				stockTaskNavHandler(e);
    			}
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
    	listeners : {
    		show : function(e){
    			e.center();
    		}
    	},
    	items: [firstStepPanel, secondStepPanel, {
	    	mt : '新增货单共二步, <span style="color:#000;">现为第三步:提交等待审核</font>',
	        index : 2,
	        html: '<h1>Congratulations!</h1><p>Step 2 of 3 - Complete</p>'
	    }],
	    listeners : {
	    	show : function(){
	    		
	    	},
	    	hide : function(thiz){
	    		thiz.getLayout().setActiveItem(0);
	    		var sot = Ext.query('input[name=radioStockOrderType]');
	    		for(var i = 0; i < sot.length; i++){
	    			sot[i].checked = false;
	    		}
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