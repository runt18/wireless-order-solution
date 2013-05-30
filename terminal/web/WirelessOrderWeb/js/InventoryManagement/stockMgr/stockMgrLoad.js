function initControl(){
	var stockBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(e){
				
			}
		}]
	});
	stockBasicGrid = createGridPanel(
		'stockBasicGrid',
		'',
		'',
		'',
		'../../QueryMenuMgr.do',
		[
			[true, false, false, true], 
			['单据编号', ''],
			['原始编号', ''],
			['供应商(仓库)', ''],
			['收货仓', ''],
			['数量', ''],
			['金额', ''],
			['经办人', ''],
			['制单人', ''],
			['单据时间', ''],
			['厨房', ''],
			['操作']
		],
		FoodBasicRecord.getKeys(),
		[['isPaging', true], ['pin',pin], ['restaurantId', restaurantID], ['stockStatus', 3]],
		GRID_PADDING_LIMIT_20,
		'',
		stockBasicGridTbar
	);
	stockBasicGrid.region = 'center';
	
	var firstStepPanel = new Ext.Panel({
    	mt : '新增货单共三步, <span style="color:#000;">现为第一步:选择单据类型</font>',
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
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品采购单'
	        	}, {
	        		xtype : 'radio',
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品调拨单'
	        	}, {
	        		xtype : 'radio',
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品报溢单'
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
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料采购单'
	        	}, {
	        		xtype : 'radio',
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料调拨单'
	        	}, {
	        		xtype : 'radio',
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料报溢单'
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
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品退货单'
	        	}, {
	        		xtype : 'radio',
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品调拨单'
	        	}, {
	        		xtype : 'radio',
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '商品报损单'
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
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料退货单'
	        	}, {
	        		xtype : 'radio',
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料调拨单'
	        	}, {
	        		xtype : 'radio',
	        		name : 'radioStockOrderType',
	        		hideLabel : true,
	        		boxLabel : '原料报损单'
	        	}]
	        }]
        }]
    });
	
	var secondStepPanelNorth = {
		title : '货单基础信息',
    	region : 'north',
    	height : 150,
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
    				xtype : 'textfield',
    				fieldLabel : '供应商'
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
		StockDetailRocerd.getKeys(),
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
		style : 'text-align:center;font-size:23px;',
		html : '合计:'
	};
	
	var secondStepPanel = new Ext.Panel({
    	mt : '新增货单共三步, <span style="color:#000;">现为第二步:填写单据信息</font>',
        index : 1,
        layout : 'border',
        width : '100%',
        items : [secondStepPanelNorth, secondStepPanelCenter, secondStepPanelWest, secondStepPanelSouth]
    });
	
	stockTaskNavWin = new Ext.Window({
		id : 'stockTaskNavWin',
		title : '新增货单共三步, <span style="color:#000;">现为第一步:选择单据类型</font>',
		width : 900,
		height : 500,
		modal : true,
		closable : false,
		resizable : false,
	    layout : 'card',
	    activeItem : 1,
	    defaults : {
	        border:false
	    },
	    bbar: ['->', {
	    	text : '上一步',
	    	iconCls : 'btn_previous',
	    	change : -1,
	    	disbled : true,
	    	handler : function(e){
	    		stockTaskNavHandler(e);
	    	}
	    }, '-', {
    		text : '下一步',
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
    	listeners : {
    		show : function(e){
    			e.center();
    		}
    	},
    	items: [firstStepPanel, secondStepPanel, {
	    	mt : '新增货单共三步, <span style="color:#000;">现为第三步:提交等待审核</font>',
	        index : 2,
	        html: '<h1>Congratulations!</h1><p>Step 3 of 4 - Complete</p>'
	    }]
	});
}