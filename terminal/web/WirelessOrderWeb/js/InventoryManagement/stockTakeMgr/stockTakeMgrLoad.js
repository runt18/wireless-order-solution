function stockTakeGridOperateRenderer(v, m, r, ri, ci, s){
	if(r.get('statusValue') == 1){
		return ''
			+ '<a href="javascript:updateStockTakeHandler();">修改</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:auditStockTakeHandler();">审核</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:cancelStockTakeHandler();">取消</a>';
	}else{
		return '<a href="javascript:updateStockTakeHandler();">查看</a>';
	}
}

function actualAmountRenderer(v, m, r, ri, ci, s){
	return Ext.ux.txtFormat.gridDou(v)
		+ '<a href="javascript:" onClick="menuOperateActualAmount.showAt([event.clientX, event.clientY])"><img src="../../images/icon_tb_setting.png" title="设置实际盘点数量"/></a>&nbsp;';
}
function shortageAmountRenderer(v, m, r, ri, ci, s){
	var deltaAmount = r.get('actualAmount') - r.get('expectAmount');
	return deltaAmount < 0 ? Ext.ux.txtFormat.gridDou(Math.abs(deltaAmount)) : 0;
}
function overageAmountRenderer(v, m, r, ri, ci, s){
	var deltaAmount = r.get('actualAmount') - r.get('expectAmount');
	return deltaAmount > 0 ? Ext.ux.txtFormat.gridDou(Math.abs(deltaAmount)) : 0;
}

function initTree(){
	deptTree = new Ext.tree.TreePanel({
		title : '部门',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryDeptTree.do',
			baseParams : {
				'restaurantID' : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部部门',
	        leaf : false,
	        border : true,
	        deptID : -1
		}),
		tbar : ['->', {
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function(){
				deptTree.getRootNode().reload();
			}
		}]
	});
}
function initGrid(){
	var stockTakeGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '搜索',
			id : 'btnSearchForStockTake',
			iconCls : 'btn_search',
			handler : function(){
				
				var gs = stockTakeGrid.getStore();
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		}]
	});
	stockTakeGrid = createGridPanel(
		'stockTakeGrid',
		'盘点任务列表',
		'',
		'',
		'../../QueryStockTake.do',
		[
			[true, false, false, true],
			['盘点日期', 'startDateFormat'],
			['仓库', 'dept.name'],
			['货品类型', 'cateTypeText'],
			['盘点状态', 'statusText'],
			['审核人', 'approver'],
			['审核时间', 'finishDateFormat',,,'function(v, m, r, ri, ci, s){if(r.get("statusValue")==2){return r.get("finishDateFormat")}else{return "";}}'],
			['备注', 'comment'],
			['操作', 'operate', ,'center', 'stockTakeGridOperateRenderer']
		],
		StockTakeRecord.getKeys(),
		[['isPaging', true], ['pin',pin], ['restaurantId', restaurantID], ['dataSource', 'normal']],
		GRID_PADDING_LIMIT_20,
		'',
		stockTakeGridTbar
	);
	stockTakeGrid.region = 'center';
	stockTakeGrid.on('render', function(){
		Ext.getCmp('btnSearchForStockTake').handler();
	});
	stockTakeGrid.on('rowdblclick', function(){
		updateStockTakeHandler();
	});
	
}

function initWin(){
	var stockTakeWinNorth = {
		region : 'north',
		height : 120,
		frame : true,
		items : [{
    		id : 'displayPanelForStockTakeTitle',
    		height : 30,
    		bodyStyle : 'font-size:18px;text-align:center;',
    		html : '原料盘点单'
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
    				id : 'hideStockTakeId'
    			}]
    		}, {
    			items : [{
    				xtype : 'combo',
    				id : 'comboStockTakeDept',
    				fieldLabel : '仓库',
    				readOnly : true,
    				forceSelection : true,
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
    				blankText : '盘点仓库不允许为空.',
    				listeners : {
    					render : function(thiz){
    						thiz.store.load();
    					}
    				}
    			}]
    		}, {
    			items : [{
    				xtype : 'combo',
    				id : 'comboMaterialCate',
    				fieldLabel : '货品类型',
    				readOnly : true,
    				forceSelection : true,
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
    					select : function(thiz){
    						var material = Ext.getCmp('comboSelectMaterialForStockTake');
    						var detail = Ext.getCmp('comboMaterialCateId');
    						detail.store.baseParams['type'] = thiz.getValue();
    						detail.store.load();
    						detail.setValue();
    						material.store.baseParams['cateType'] = thiz.getValue();
    						material.store.baseParams['cateId'] = '';
    						material.store.load();
    					}
    				}
    			}]
    		}, {
    			items : [{
    				xtype : 'combo',
    				id : 'comboMaterialCateId',
    				fieldLabel : '类别',
    				forceSelection : true,
    				store : new Ext.data.JsonStore({
    					url : '../../QueryMaterialCate.do',
    					root : 'root',
    					baseParams : {
    						dataSource : 'normal',
    						restaurantID : restaurantID
    					},
    					fields : MaterialCateRecord.getKeys()
    				}),
    				valueField : 'id',
    				displayField : 'name',
    				typeAhead : true,
    				mode : 'local',
    				triggerAction : 'all',
    				selectOnFocus : true,
    				listeners : {
    					render : function(thiz){
    						var cate = Ext.getCmp('comboMaterialCate');
    						cate.setValue(1);
    						cate.fireEvent('select', cate);
    					},
    					blur : function(thiz){
    						if(thiz.getValue() != thiz.getRawValue() && thiz.getRawValue() == ''){
    							thiz.setValue();
    							thiz.fireEvent('select', thiz);
    						}
    					},
    					select : function(thiz){
    						var material = Ext.getCmp('comboSelectMaterialForStockTake');
    						material.store.baseParams['cateId'] = thiz.getValue();
    						material.store.load();
    					}
    				}
    			}]
    		}, {
    			columnWidth : 1,
    			items : [{
    				xtype : 'textfield',
    				fieldLabel : '备注',
    				width : 774,
    				id : 'txtStockTakeComment'
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				id : 'txtStockTakeApprover',
    				fieldLabel : '审核人',
    				disabled : true
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				id : 'txtStockTakeApproverDate',
    				fieldLabel : '审核时间',
    				disabled : true
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				id : 'txtStockTakeOperator',
    				fieldLabel : '盘点人',
    				disabled : true
    			}]
    		}, {
    			items : [{
    				xtype : 'textfield',
    				id : 'txtStockTakeOperatorDate',
    				fieldLabel : '盘点时间',
    				disabled : true
    			}]
    		}]
    	}]
	};
	var stockTakeWinWest = {
		title : '添加货品',
		region : 'west',
		width : 200,
		frame : true,
		layout : 'form',
    	labelWidth : 60,
    	defaults : {
    		width : 120
    	},
    	items : [{
			xtype : 'combo',
			id : 'comboSelectMaterialForStockTake',
			fieldLabel : '货品',
			forceSelection : true,
			listWidth : 250,
			height : 200,
			maxHeight : 300,
			store : new Ext.data.JsonStore({
				url : '../../QueryMaterial.do',
				root : 'root',
				baseParams : {
					dataSource : 'normal',
					pin : pin,
					restaurantID : restaurantID
				},
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
				+ '{cateName} -- {id} -- {name} -- {pinyin}'
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
					var actualAmount = Ext.getCmp('numActualAmountForStockAction');
					actualAmount.setValue();
					actualAmount.focus(actualAmount, 100);
				}
			}
		}, {
			id : 'numActualAmountForStockAction',
    		xtype : 'numberfield',
    		fieldLabel : '盘点数',
    		maxValue : 65535,
    		value : 0
    	}],
    	buttonAlign : 'center',
    	buttons : [{
    		text : '添加',
    		handler : function(e){
    			var material = Ext.getCmp('comboSelectMaterialForStockTake');
    			var actualAmount = Ext.getCmp('numActualAmountForStockAction');
    			for(var i=0; i< material.store.getCount(); i++){
    				var temp = material.store.getAt(i);
    				if(temp.get('id') == material.getValue()){
    					var gs = stockTakeWinCenter.getStore();
    					var has = false;
    					for(var j=0; j<gs.getCount(); j++){
    						if(gs.getAt(j).get('material')['id'] == temp.get('id')){
    							has = true;
    							break;
    						}
    					}
    					if(has){
    						Ext.example.msg('提示', '操作失败, 该货品已存在, 请重新选择.');
    					}else{
    						gs.add(new StockTakeDetailRecord({
    							'material.name' : temp.get('name'),
    							material : {
    								id : temp.get('id'),
    								name : temp.get('name')
    							},
    							expectAmount : temp.get('stock'),
    							actualAmount : actualAmount.getRawValue() == '' ? 0 : actualAmount.getValue()
    						}));
    					}
    					break;
    				}
    			}
    			material.setValue();
    			actualAmount.setValue();
    		}
    	}]
	};
	var stockTakeWinCenter = createGridPanel(
		'stockTakeWinCenter',
		'货品列表',
		'',
		'',
		'',
		[
			[true, false, false, false], 
			['品名', 'material.name', 130],
			['盘点数', 'actualAmount',,'right', 'actualAmountRenderer'],
			['账面数', 'expectAmount',80,'right', 'Ext.ux.txtFormat.gridDou'],
			['盘亏数', 'deltaAmount',80,'right', 'shortageAmountRenderer'],
			['盘盈数', 'deltaAmount',80,'right', 'overageAmountRenderer']
		],
		StockTakeDetailRecord.getKeys(),
		[['isPaging', true], ['pin',pin], ['restaurantId', restaurantID], ['stockStatus', 3]],
		GRID_PADDING_LIMIT_20,
		''
	);
	stockTakeWinCenter.region = 'center';
	
	stockTakeWin = new Ext.Window({
		title : '查看盘点任务',
		width : 900,
		height : 500,
		modal : true,
		resize : false,
		closable : false,
		layout : 'border',
		items : [stockTakeWinNorth, stockTakeWinWest, stockTakeWinCenter],
		bbar : ['->', {
			text : '保存',
			id : 'btnSaveStockTake',
			iconCls : 'btn_save',
			handler : function(){
				var id = Ext.getCmp('hideStockTakeId');
				var dept = Ext.getCmp('comboStockTakeDept');
				var cate = Ext.getCmp('comboMaterialCate');
				var cateId = Ext.getCmp('comboMaterialCateId');
				var comment = Ext.getCmp('txtStockTakeComment');
				
				if(!dept.isValid() || !cate.isValid()){
					return;
				}
				
				var detail = '';
				for(var i = 0; i < stockTakeWinCenter.getStore().getCount(); i++){
					var temp = stockTakeWinCenter.getStore().getAt(i);
					if(i > 0){
						detail += '<sp>';
					}
					detail += (temp.get('material')['id'] + '<spst>' + temp.get('actualAmount'));
				}
				if(detail == ''){
					Ext.example.msg('提示', '操作失败, 请填写盘点货品信息.');
					return;
				}
				
				Ext.Ajax.request({
					url : '../../OperateStockTake.do',
					params : {
						dataSource : stockTakeWin.otype.toLowerCase(),
						pin : pin,
						id : id.getValue(),
						dept : dept.getValue(),
						cateType : cate.getValue(),
						cateId : cateId.getValue() > 0 && cateId.getRawValue() ? cateId.getValue() : '',
						comment : comment.getValue(),
						detail : detail
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							stockTakeWin.hide();
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnSearchForStockTake').handler();
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
			text : '取消',
			iconCls : 'btn_cancel',
			handler : function(){
				stockTakeWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				stockTakeWin.hide();
			}
		}],
		listeners : {
			hide : function(){
				Ext.getCmp('comboSelectMaterialForStockTake').setValue();
    			Ext.getCmp('numActualAmountForStockAction').setValue();
			},
			show : function(thiz){
				thiz.center();
			}
		}
	});
}
/**
 * 
 */
function initDetailActualAmountMenu(){
	menuOperateActualAmount = new Ext.menu.Menu({
		id : 'menuOperateActualAmount',
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
					id : 'numOperateActualAmount',
					fieldLabel : '数量',
					width : 80,
					validator : function(v){
						if(v >= 1 && v <= 65535){
							return true;
						}else{
							return '菜品数量在 1 ~ 65535 之间.';
						}
					} 
				}]
			}],
			bbar : ['->', {
				text : '确定',
				id : 'btnSaveOperateActualAmount',
				iconCls : 'btn_save',
				handler : function(e){
					var amount = Ext.getCmp('numOperateActualAmount');
					if(!amount.isValid()){
						return;
					}
					Ext.getCmp('stockTakeWinCenter').getSelectionModel().getSelected().set('actualAmount', amount.getValue());
					menuOperateActualAmount.hide();
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					menuOperateActualAmount.hide();
				}
			}]
		}), {hideOnClick : false})],
		listeners : {
			show : function(){
				var amount = Ext.getCmp('numOperateActualAmount');
				amount.setValue(Ext.getCmp('stockTakeWinCenter').getSelectionModel().getSelected().get('actualAmount'));
				amount.clearInvalid();
				amount.focus.defer(100, amount);
			}
		},
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSaveOperateActualAmount').handler();
			}
		}, {
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				menuOperateActualAmount.hide();
			}
		}]
	});
	menuOperateActualAmount.render(document.body);
}