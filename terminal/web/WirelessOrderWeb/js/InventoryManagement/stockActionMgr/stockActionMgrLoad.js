function stockOperateRenderer(v, m, r, ri, ci, s){
	if(r.get('statusValue') == 1){
		if(r.get('subTypeValue') == 9){
			return '<a href="javascript:updateStockActionHandler();">查看</a>';
		}else{
			return ''
			+ '<a href="javascript:exportExcel();">导出</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:updateStockActionHandler();">修改</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:auditStockActionHandler();">审核</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:deleteStockActionHandler();">删除</a>';
		}
	}else{
		return ''
			+ '<a href="javascript:exportExcel();">导出</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:updateStockActionHandler();">查看</a>';
	}
}

function stockDetailTotalCountRenderer(v, m, r, ri, ci, s){
	if(stockTaskNavWin.otype == Ext.ux.otype['select']){
		return Ext.ux.txtFormat.gridDou(r.get('amount'));
	}else{
		return Ext.ux.txtFormat.gridDou(r.get('amount'))
			+ '<a href="javascript:setAmountForStockActionDetail({amount:1});"><img src="../../images/btnAdd.gif" title="数量+1"/></a>&nbsp;'
			+ '<a href="javascript:setAmountForStockActionDetail({amount:-1});"><img src="../../images/btnDelete.png" title="数量-1"/></a>&nbsp;'
			+ '<a href="javascript:" onClick="menuStockDetailAmount.showAt([event.clientX, event.clientY])"><img src="../../images/icon_tb_setting.png" title="设置数量"/></a>&nbsp;'
			+ '<a href="javascript:setAmountForStockActionDetail({otype:Ext.ux.otype[\'delete\']});"><img src="../../images/btnCancel.png" title="删除该记录"/></a>'
			+ '';
	}
}
function stockDetailTotalPriceRenderer(v, m, r, ri, ci, s){
	return Ext.ux.txtFormat.gridDou(r.get('amount') * r.get('price'));
}
function stockDetailPriceRenderer(v, m, r, ri, ci, s){
	if(stockTaskNavWin.otype == Ext.ux.otype['select']){
		return Ext.ux.txtFormat.gridDou(r.get('price'));
	}else{
		return Ext.ux.txtFormat.gridDou(r.get('price'))
		+ '<a href="javascript:" onClick="menuStockDetailPrice.showAt([event.clientX, event.clientY])"><img src="../../images/icon_tb_setting.png" title="设置单价"/></a>&nbsp;';
	}
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
		}else if(st == 2){
			display = r.get('deptOut')['name'];
		}
	}else if(t == 2){
		display = r.get('deptOut')['name'];
	}
	return display;
}
function IsNum(e) {
	    var k = window.event ? e.keyCode : e.which;
	    if (((k >= 48) && (k <= 57)) || k == 8 || k == 0) {
	    } else {
	        if (window.event) {
	            window.event.returnValue = false;
	        }
	        else {
	            e.preventDefault();
	        }
	    }
} 
function initControl(){
	var stockInDate = [[-1, '全部'], [1, '采购'], [2, '入库调拨'], [3, '报溢'], [7, '盘盈']];
	var stockOutDate = [[-1, '全部'], [4, '退货'], [5, '出库调拨'], [6, '报损'], [8, '盘亏'], [9, '消耗']];
	
	var stockBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '货单类型:'
		}, {
			xtype : 'combo',
			id : 'comboSearchForStockType',
			readOnly : true,
			forceSelection : true,
			width : 100,
			value : 1,
			store : new Ext.data.SimpleStore({
				data : winParams.st,
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			listeners : {
				select : function(thiz){
					var subType = Ext.getCmp('comboSearchForSubType');
					if(thiz.getValue() == 1){
						subType.store.loadData(stockInDate);
						subType.setValue(-1);
					}else{
						subType.store.loadData(stockOutDate);
						subType.setValue(-1);
					}
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;业务类型:'
		}, {
			xtype : 'combo',
			id : 'comboSearchForSubType',
			readOnly : true,
			forceSelection : true,
			width : 100,
			value : -1,
			store : new Ext.data.SimpleStore({
				data : stockInDate,
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(){
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;货品类型:'
		}, {
			xtype : 'combo',
			id : 'comboSearchForCateType',
			readOnly : true,
			forceSelection : true,
			width : 100,
			value : 1,
			store : new Ext.data.SimpleStore({
				data : winParams.cate,
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			listeners : {
				select : function(){
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;仓库:'
		}, {
			xtype : 'combo',
			id : 'comboSearchForDept',
			fieldLabel : '仓库',
			width : 100,
			readOnly : true,
			forceSelection : true,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			blankText : '盘点仓库不允许为空.',
			listeners : {
				select : function(){
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				},
				render : function(thiz){
					var data = [[-1,'全部']];
					Ext.Ajax.request({
						url : '../../QueryDept.do',
						params : {
							dataSource : 'normal',
							
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.root.length; i++){
								data.push([jr.root[i]['id'], jr.root[i]['name']]);
							}
							thiz.store.loadData(data);
							thiz.setValue(-1);
						},
						fialure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;审核状态:'
		}, {
			xtype : 'combo',
			id : 'comboSearchForStockStatus',
			readOnly : true,
			forceSelection : true,
			width : 100,
			value : -1,
			store : new Ext.data.SimpleStore({
				data : [[-1, '全部'], [1, '未审核'], [2, '审核通过'], [3, ' 冲红']],
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			listeners : {
				select : function(){
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;供应商:'
		}, {
			xtype : 'combo',
			id : 'comboSearchForSupplier',
			readOnly : true,
			forceSelection : true,
			width : 103,
			listWidth : 120,
			store : new Ext.data.SimpleStore({
				fields : ['supplierID', 'name']
			}),
			valueField : 'supplierID',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				render : function(thiz){
					var data = [[-1,'全部']];
					Ext.Ajax.request({
						url : '../../QuerySupplier.do',
						params : {
							
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.root.length; i++){
								data.push([jr.root[i]['supplierID'], jr.root[i]['name']]);
							}
							thiz.store.loadData(data);
							thiz.setValue(-1);
						},
						fialure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
			
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;货单编号/原始单号:'
		}, {
			xtype : 'textfield',
			id : 'comboSearchForOriStockId',
			width : 100
		}, '->', {
			text : '重置',
			id : 'btnReload',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getCmp('comboSearchForStockType').setValue(1);
				Ext.getCmp('comboSearchForSubType').setValue(-1);
				Ext.getCmp('comboSearchForCateType').setValue(1);
				Ext.getCmp('comboSearchForDept').setValue(-1);
				Ext.getCmp('comboSearchForStockStatus').setValue(-1);
				Ext.getCmp('comboSearchForSupplier').setValue(-1);
				Ext.getCmp('comboSearchForOriStockId').setValue();
				Ext.getCmp('btnSearchForStockBasicMsg').handler();
				//location.reload(false);
			}
		}, {
			text : '搜索',
			id : 'btnSearchForStockBasicMsg',
			iconCls : 'btn_search',
			handler : function(e){
				var st = Ext.getCmp('comboSearchForStockType');
				var cate = Ext.getCmp('comboSearchForCateType');
				var dept = Ext.getCmp('comboSearchForDept');
				var oriStockId = Ext.getCmp('comboSearchForOriStockId');
				var status = Ext.getCmp('comboSearchForStockStatus');
				var supplier = Ext.getCmp('comboSearchForSupplier');
				var subType = Ext.getCmp('comboSearchForSubType');
				
				var gs = stockBasicGrid.getStore();
				gs.baseParams['stockType'] = st.getValue();
				gs.baseParams['cateType'] = cate.getValue();
				gs.baseParams['dept'] = dept.getValue();
				gs.baseParams['oriStockId'] = oriStockId.getValue();
				gs.baseParams['status'] = status.getValue() != -1 ? status.getValue() : '';
				gs.baseParams['supplier'] = supplier.getValue();
				gs.baseParams['subType'] = subType.getValue();
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
			['货单编号', 'id', 50],
			['货单类型', 'typeText',,,'stockTypeRenderer'],
			['货品类型', 'cateTypeText', 50],
			['原始单号', 'oriStockId'],
			['时间', 'oriStockDateFormat', 65],
			['出库仓/供应商', 'center', 65,,'stockOutRenderer'],
			['收货仓/供应商', 'center', 65,,'stockInRenderer'],
			['数量', 'amount',60,'right','Ext.ux.txtFormat.gridDou'],
			['应收金额', 'price',80,'right','Ext.ux.txtFormat.gridDou'],
			['实际金额', 'actualPrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
			['审核人', 'approverName', 60],
			['审核状态', 'statusText', 60, 'center'],
			['制单人', 'operatorName', 60],
			['操作', 'center', 150, 'center', 'stockOperateRenderer']
		],
		StockRecord.getKeys(),
		[['isPaging', true],  ['restaurantId', restaurantID]],
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
	stockBasicGrid.on('rowdblclick', function(){
		updateStockActionHandler();		
	});
	stockBasicGrid.getStore().on('load', function(store, records, options){
		var sumRow;
		for(var i = 0; i < records.length; i++){
			if(eval(records[i].get('statusValue') != 1)){
				sumRow = stockBasicGrid.getView().getRow(i);
				sumRow.style.backgroundColor = '#DDD';
				sumRow = null;
			}
		}
		sumRow = null;
		if(store.getCount() > 0){
			sumRow = stockBasicGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < stockBasicGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = stockBasicGrid.getView().getCell(store.getCount() - 1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
			stockBasicGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			stockBasicGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			//stockBasicGrid.getView().getCell(store.getCount()-1, 10).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 11).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 12).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 13).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 14).innerHTML = '--';
		}
	});
	
	firstStepPanel = new Ext.Panel({
    	mt : '操作货单共二步, <span style="color:#000;">现为第一步:选择单据类型</font>',
        index : 0,
        frame : true,
        items : [{
        	xtype : 'fieldset',
        	title : '入库单',
        	layout : 'column',
        	height : Ext.isIE ? 150 : 138,
        	items : [{
        		columnWidth : .2,
        		items : [{
        			html : '&nbsp;'
        		}, {
        			layout : 'form',
            		hidden : true,
            		items : [{
            			xtype : 'radio',
    	        		inputValue : [1,1,7],
    	        		name : 'radioStockOrderType',
    	        		hideLabel : true,
    	        		boxLabel : '商品盘盈'
            		}, {
            			xtype : 'radio',
    	        		inputValue : [2,1,8],
    	        		name : 'radioStockOrderType',
    	        		hideLabel : true,
    	        		boxLabel : '商品盘亏'
            		}, {
            			xtype : 'radio',
    	        		inputValue : [2,1,9],
    	        		name : 'radioStockOrderType',
    	        		hideLabel : true,
    	        		boxLabel : '商品消耗'
            		}, {
            			xtype : 'radio',
    	        		inputValue : [2,2,7],
    	        		name : 'radioStockOrderType',
    	        		hideLabel : true,
    	        		boxLabel : '原料盘盈'
            		}, {
            			xtype : 'radio',
    	        		inputValue : [2,2,8],
    	        		name : 'radioStockOrderType',
    	        		hideLabel : true,
    	        		boxLabel : '原料盘亏'
            		}, {
            			xtype : 'radio',
    	        		inputValue : [2,2,9],
    	        		name : 'radioStockOrderType',
    	        		hideLabel : true,
    	        		boxLabel : '原料消耗'
            		}]
        		}]
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
//	        		hidden : true,
//	        		hideParent : true,
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
    			items : [{
    				xtype : 'hidden',
    				id : 'hideStockActionId'
    			}]
    		}, {
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
    					url: '../../QuerySupplier.do',
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
    				readOnly : true,
    				allowBlank : false,
    				blankText : '日期不能为空, 且小于当前会计月月底并大于该月最后一次盘点时间.'
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
			['品名', 'material.name', 130],
			['数量', 'amount',130,'right', 'stockDetailTotalCountRenderer'],
			['单价', 'price',80,'right', 'stockDetailPriceRenderer'],
			['总价', 'totalPrice',80,'right', 'stockDetailTotalPriceRenderer']
		],
		StockDetailRecord.getKeys(),
		[['isPaging', true],  ['restaurantId', restaurantID], ['stockStatus', 3]],
		GRID_PADDING_LIMIT_20,
		''
	);
	secondStepPanelCenter.region = 'center';
	secondStepPanelCenter.getStore().on('load', function(thiz, rs){
		var totalPrice = 0, amount = 0;
		var txtActualPrice = Ext.getDom('txtActualPrice').value;
		
		for(var i = 0; i < secondStepPanelCenter.getStore().getCount(); i++){
			totalPrice += (Math.round(parseFloat(secondStepPanelCenter.getStore().getAt(i).get('amount') * secondStepPanelCenter.getStore().getAt(i).get('price')) * 100) / 100);
			amount += secondStepPanelCenter.getStore().getAt(i).get('amount');
		}
		Ext.getDom('txtTotalAmount').value = amount;
		Ext.getDom('txtTotalPrice').value = totalPrice;
		if(txtActualPrice.indexOf('$') > 0){
			Ext.getDom('txtActualPrice').value = txtActualPrice.substring(0, (txtActualPrice.length-1));
		}else{
			Ext.getDom('txtActualPrice').value = totalPrice;
		}
		
	});
	secondStepPanelCenter.getStore().on('add', function(thiz, rs){
		secondStepPanelCenter.getStore().fireEvent('load', thiz, rs);
	});
	secondStepPanelCenter.getStore().on('remove', function(thiz, rs){
		secondStepPanelCenter.getStore().fireEvent('load', thiz, rs);
	});
	secondStepPanelCenter.getStore().on('update', function(thiz, rs){
		secondStepPanelCenter.getStore().fireEvent('load', thiz, rs);
	});
	
	var secondStepPanelWest = {
        title : '添加货品',
        id : 'secondStepPanelWest',
        layout : 'form',
    	region : 'west',
    	frame : true,
    	width : 200,
    	labelWidth : 60,
    	defaults : {
    		width : 120
    	},
    	items : [{
			xtype : 'combo',
			id : 'comboSelectMaterialForStockAction',
			fieldLabel : '货品',
			forceSelection : true,
			width : 103,
			listWidth : 250,
			height : 200,
			maxHeight : 300,
			store : new Ext.data.JsonStore({
				url : '../../QueryMaterial.do',
				baseParams : {
					dataSource : 'normal',
					
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
			allowBlank : false,
			tpl:'<tpl for=".">' 
				+ '<div class="x-combo-list-item" style="height:18px;">'
				+ '{id} -- {name} -- {pinyin}'
				+ '</div>'
				+ '</tpl>',
			listeners : {
				beforequery : function(e){ 
					var combo = e.combo; 
					if(!e.forceAll){ 
						var value = e.query; 
						combo.store.filterBy(function(record,id){
							return record.get('name').indexOf(value) != -1 
									|| (record.get('id')+'').indexOf(value) != -1 
									|| record.get('pinyin').indexOf(value.toUpperCase()) != -1;
						}); 
						combo.expand(); 
						combo.select(0, true);
						return false; 
					}
				},
				select : function(thiz){
//					var newRecord = null;
//	    			for(var i=0, temp=thiz.store, sv=thiz.getValue(); i<temp.getCount(); i++){
//	    				if(temp.getAt(i).get('id') == sv){
//	    					newRecord = temp.getAt(i);
//	    					break;
//	    				}
//	    			}
					var price = Ext.getCmp('numSelectPriceForStockAction');
	    			var count = Ext.getCmp('numSelectCountForStockAction');
					var stockTypeList = stockTaskNavWin.stockType.split(',');
					var stockSubType = stockTypeList[2];
					if(stockSubType == 3 || stockSubType == 6){
						Ext.Ajax.request({
							url : '../../QueryMaterial.do',
							params : {
								dataSource : 'normal',
								
								restaurantID : restaurantID,
								materialId : thiz.getValue()
							},
							success : function(res, opt){
							var jr = Ext.decode(res.responseText);
								if(jr.success){
									price.setValue(jr.root[0].price);
									price.setDisabled(true);
								}else{
									Ext.ux.showMsg(jr);
								}
							},
							failure : function(res, opt){
								Ext.ux.showMsg(Ext.decode(res.responseText));
							}
						});
					}else{
						price.setValue(0);
					}

	    			count.setValue(1);
	    			count.focus(true, 100);
				}
			}
		}, {
			id : 'numSelectCountForStockAction',
    		xtype : 'numberfield',
    		fieldLabel : '数量',
    		maxValue : 65535,
    		allowBlank : false
    	},
    	{
    		id : 'numSelectPriceForStockAction',
    		fieldLabel : '单价',
    		xtype : 'numberfield',
    		allowBlank : false,
    		listeners : {
    			focus : function(thiz){
					 Ext.getCmp('numSelectPriceForStockAction').focus(true, 100);
    			}
    		}
    	}	
/*			new Ext.form.NumberField({
				id : 'numSelectPriceForStockAction',
	    		fieldLabel : '单价',
	    		xtype : 'numberfield',
	    		allowBlank : false,
	    		value : '3',
	    		listeners : {
	    			focus : function(thiz){
						 Ext.getCmp('numSelectPriceForStockAction').focus(true, 100);
	    			}
	    		}
			})*/

    	],
    	buttonAlign : 'center',
    	buttons : [{
    		text : '添加',
    		handler : function(e){
    			var material = Ext.getCmp('comboSelectMaterialForStockAction');
    			var amount = Ext.getCmp('numSelectCountForStockAction');
    			var price = Ext.getCmp('numSelectPriceForStockAction');
    			var stockTypeList = stockTaskNavWin.stockType.split(',');
    			var subType = stockTypeList[2];
    			if(subType != 2 && subType != 5){
    				if(!material.isValid() || !amount.isValid() || !price.isValid()){
	    				Ext.example.msg('提示', '请输入货品单价.');
	    				price.focus(price, 100);
	    				return;
    				}
    			}

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
    				if(detail.getAt(i).get('material.id') == newRecord.get('id')){
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
    					'material.id' : newRecord.get('id'),
    					'material.cateName' : newRecord.get('cateName'),
    					'material.name' : newRecord.get('name'),
    					amount : amount.getValue(),
    					price : price.getValue()
    				}));
    			}
    		}
    	}, {
    		text : '重置',
    		hidden : true,
    		handler : function(e){
    			var material = Ext.getCmp('comboSelectMaterialForStockAction');
    			var amount = Ext.getCmp('numSelectCountForStockAction');
    			var price = Ext.getCmp('numSelectPriceForStockAction');
    			material.setValue();
    			amount.setValue();
    			price.setValue();
    		}
    	}]
    };
	var secondStepPanelSouth = {
		id : 'secondStepPanelSouth',
		region : 'south',
		frame : true,
		height : 30,
		bodyStyle : 'font-size:18px;text-align:center;',
		html : '总数量小计:<input id="txtTotalAmount" type="text" disabled="disabled" style="height: 20px;width:90px;font-size :18px;font-weight: bolder;" />' +
			'&nbsp;&nbsp;&nbsp; 总金额:<input id="txtTotalPrice" type="text" disabled="disabled" style="height: 20px;width:90px;font-size :18px;font-weight: bolder;" />' +
			'&nbsp;&nbsp;&nbsp;<label id="labActualPrice" >实际金额:</label><input id="txtActualPrice" disabled="disabled" type="text" style=" height: 20px;width:90px;font-size :18px;font-weight: bolder; color:red"/>'
	};
/*	var secondStepPanelSouth = new Ext.Panel({
		id : 'secondStepPanelSouth',
		region : 'south',
		frame : true,
		height : 30,
		item : [{
    		id : 'displayaaaa',
    		height : 30,
    		bodyStyle : 'font-size:18px;text-align:center;',
    		html : '-----'
    	},{
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
    				xtype : 'text',
    				id : 'hideStockActionId',
    				text : 'bbbbb'
    			}]
    		}]
    	}]
	});*/
	
	var secondStepPanel = new Ext.Panel({
    	mt : '操作货单共二步, <span style="color:#000;">现为第二步:填写单据信息</font>',
        index : 1,
        layout : 'border',
        width : '100%',
        items : [secondStepPanelNorth, secondStepPanelCenter, secondStepPanelWest, secondStepPanelSouth]
    });
	
	stockTaskNavWin = new Ext.Window({
		id : 'stockTaskNavWin',
		//title : '新增货单共二步, <span style="color:#000;">现为第一步:选择单据类型</font>',
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
	    }, {
    		text : '下一步',
    		id : 'btnNextForStockNav',
    		iconCls : 'btn_next',
    		change : 1,
    		handler : function(e){
    			stockTaskNavHandler(e);
	    	}
    	}, {
    		text : '审核',
    		iconCls : 'btn_refresh',
    		id : 'btnAuditStockAction',
    		hidden : true,
    		handler : function(){
    			auditStockActionHandler();
    		}
    	}, {
    		text : '取消',
    		iconCls : 'btn_cancel',
    		handler : function(){
    			Ext.getCmp('numSelectPriceForStockAction').setDisabled(false);
    			Ext.getCmp('numSelectPriceForStockAction').getEl().up('.x-form-item').setDisplayed(true); 
				Ext.getCmp('secondStepPanelSouth').setDisabled(false);
				var column = Ext.getCmp('secondStepPanelCenter').getColumnModel();
				column.setHidden(3, false);
				column.setHidden(4, false);
				column.setRenderer(3, stockDetailPriceRenderer);
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
    	items: [firstStepPanel, secondStepPanel],
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
	
	menuStockDetailPrice = new Ext.menu.Menu({
		id : 'menuStockDetailPrice',
		hideOnClick : false,
		items : [new Ext.menu.Adapter(new Ext.Panel({
			frame : true,
			width : 150,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 30,
				items : [{
					xtype : 'numberfield',
					id : 'numStockDetailPrice',
					fieldLabel : '金额',
					width : 80,
					validator : function(v){
						if(v >= 1 && v <= 65535){
							return true;
						}else{
							return '金额在 1 ~ 65535 之间.';
						}
					} 
				}]
			}],
			bbar : ['->', {
				text : '确定',
				id : 'btnSaveStockDetailPrice',
				iconCls : 'btn_save',
				handler : function(e){
					var price = Ext.getCmp('numStockDetailPrice');
					if(price.isValid()){						
						secondStepPanelCenter.getSelectionModel().getSelected().set('price', price.getValue());
						menuStockDetailPrice.hide();
					}
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					menuStockDetailPrice.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnSaveStockDetailPrice').handler();
				}
			}]
		}), {hideOnClick : false})],
		listeners : {
			show : function(){
				var data = Ext.ux.getSelData(secondStepPanelCenter);
				var price = Ext.getCmp('numStockDetailPrice');
				price.setValue(data['price']);
				price.clearInvalid();
				price.focus.defer(100, price);
			}
		}
	});
	menuStockDetailPrice.render(document.body);
	
	menuStockDetailAmount = new Ext.menu.Menu({
		id : 'menuStockDetailPrice',
		hideOnClick : false,
		items : [new Ext.menu.Adapter(new Ext.Panel({
			frame : true,
			width : 150,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 30,
				items : [{
					xtype : 'numberfield',
					id : 'numStockDetailAmountSetting',
					fieldLabel : '数量',
					width : 80,
					validator : function(v){
						if(v >= 0.01 && v <= 65535){
							return true;
						}else{
							return '菜品数量在0.01 ~ 65535 之间.';
						}
					} 
				}]
			}],
			bbar : ['->', {
				text : '确定',
				id : 'btnSaveStockDetailAmount',
				iconCls : 'btn_save',
				handler : function(e){
					var amount = Ext.getCmp('numStockDetailAmountSetting');
					if(amount.isValid()){						
						secondStepPanelCenter.getSelectionModel().getSelected().set('amount', amount.getValue());
						menuStockDetailAmount.hide();
					}
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					menuStockDetailAmount.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnSaveStockDetailAmount').handler();
				}
			}]
		}), {hideOnClick : false})],
		listeners : {
			show : function(){
				var data = Ext.ux.getSelData(secondStepPanelCenter);
				var amount = Ext.getCmp('numStockDetailAmountSetting');
				amount.setValue(data['amount']);
				amount.clearInvalid();
				amount.focus.defer(100, amount);
			}
		}
	});
	menuStockDetailAmount.render(document.body);
}

/**
 * 
 */
function loadData(){
	
}
