﻿//-------------lib.js------
/**
 * 
 * @param c
 */
function operateTableDataHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefied'){
		return;
	}
	
	var id = Ext.getCmp('hideTableId');
	var alias = Ext.getCmp('numTableAlias');
	var name = Ext.getCmp('txtTableName');
	var region = Ext.getCmp('comboTableRegion');
	var minimumCost = Ext.getCmp('numMinimumCost');
	var serviceRate = Ext.getCmp('numServiceRate');
	
	if(c.otype == Ext.ux.otype['set']){
		var data = typeof c.data == 'undefined' ? {} : c.data;
		var regionData = typeof data.region == 'undefined' ? {} : data.region;  
		id.setValue(data['id']);
		alias.setValue(data['alias']);
		name.setValue(data['name']);
		region.setValue(regionData['id']);
		minimumCost.setValue(typeof data['minimumCost'] == 'undefined' ? 0 : data['minimumCost']);
		serviceRate.setValue(typeof data['serviceRate'] == 'undefined' ? 0 : data['serviceRate']);
		
		alias.clearInvalid();
		name.clearInvalid();
		region.clearInvalid();
		minimumCost.clearInvalid();
		serviceRate.clearInvalid();
	}else if(c.otype == Ext.ux.otype['get']){
		
	}
}
 
/**
 * 新增餐台信息
 */
function insertTableBasicHandler(){
	Ext.getCmp('btnSaveOperateTable').setVisible(true);
	Ext.getCmp('numTableAlias').setDisabled(false);
	tableBasicWin.center();
	tableBasicWin.show();
	tableBasicWin.setTitle('新增餐台信息');
	tableBasicWin.otype = Ext.ux.otype['insert'];
	operateTableDataHandler({
		otype : Ext.ux.otype['set']
	});
}

/**
 * 修改餐台信息
 */
function updateTableBasicHandler(){
	var data = Ext.ux.getSelData(tableBasicGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录再操作.');
		return;
	}
	if(data['statusValue'] == 1){
		Ext.example.msg('提示', '该台正在就餐, 只能查看不允许修改.');
		Ext.getCmp('btnSaveOperateTable').setDisabled(true);		
	}else{
		Ext.getCmp('btnSaveOperateTable').setDisabled(false);
	}
	Ext.getCmp('numTableAlias').setDisabled(true);
	tableBasicWin.center();
	tableBasicWin.show();
	tableBasicWin.setTitle('修改餐台信息');
	tableBasicWin.otype = Ext.ux.otype['update'];
	operateTableDataHandler({
		otype : Ext.ux.otype['set'],
		data : data
	});
	
	
}

/**
 * 删除餐台信息
 */
function deleteTableBasicHandler(){
	var data = Ext.ux.getSelData(tableBasicGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录再操作.');
		return;
	}
	if(data['statusValue'] == 1){
		Ext.example.msg('提示', '操作失败, 该餐台正在就餐, 不允许删除.');
		return;
	}
	Ext.Msg.show({
		title : '重要',
		msg : '是否删除餐台信息',
		buttons : Ext.Msg.YESNO,
		icon: Ext.Msg.QUESTION,
		fn : function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateTable.do',
					params : {
						'dataSource' : 'delete',
						
						id : data['id']
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnSearchForTable').handler();
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


//---------------
//-------------load

 
function tableBasicGridOperateRenderer(v, m, r, ri, ci, s){
	return ''
		+ '<a href="javascript:updateTableBasicHandler();">修改</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:deleteTableBasicHandler();">删除</a>';
}

/**
 * 
 */
function initTree(){
	regionTree = new Ext.tree.TreePanel({
		title : '区域',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		root : new Ext.tree.AsyncTreeNode({
			text : '全部',
			regionId : -1,
			loader : new Ext.tree.TreeLoader({
				dataUrl : '../../QueryRegion.do',
				baseParams : {
					dataSource : 'tree'
				}
			}),
			listeners : {
				load : function(thiz, node, response){
					for(var i = 0; i < thiz.childNodes.length; i++){
						var temp = thiz.childNodes[i];
						comboRegionData.push([temp.attributes['regionId'], temp.attributes['regionName']]);
					}
					Ext.getCmp('comboTableRegion').store.loadData(comboRegionData);
				}
			}
		}),
		tbar : [ '->', {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(e) {
				var node = regionTree.getSelectionModel().getSelectedNode();
				if (!node || node.attributes.regionId == -1) {
					Ext.example.msg('提示', '操作失败, 请选择一个区域再进行修改.');
					return;
				}
				var operateRegionWin = Ext.getCmp('operateRegionWin');
				if(!operateRegionWin){
					operateRegionWin = new Ext.Window({
						id : 'operateRegionWin',
						title : '修改区域信息',
						modal : true,
						closable : false,
						resizeble : false,
						width : 250,
						items : [{
							layout : 'form',
							labelWidth : 60,
							frame : true,
							items : [{
								xtype : 'hidden',
								id : 'hideRegionId'
							}, {
								xtype : 'textfield',
								id : 'txtRegionName',
								fieldLabel : '区域名称',
								allowBlank : false
							}]
						}],
						keys : [{
							key : Ext.EventObject.ENTER,
							scope : this,
							fn : function(){
								Ext.getCmp('btnSaveOperateRegoin').handler();
							}
						}, {
							key : Ext.EventObject.ESC,
							scope : this,
							fn : function(){
								operateRegionWin.hide();
							}
						}],
						bbar : ['->', {
							text : '保存',
							id : 'btnSaveOperateRegoin',
							iconCls : 'btn_save',
							handler : function(){
								var id = Ext.getCmp('hideRegionId');
								var name = Ext.getCmp('txtRegionName');
								if(!name.isValid()){
									return;
								}
								Ext.Ajax.request({
									url : '../../OperateRegion.do',
									params : {
										dataSource : 'update',
										
										id : id.getValue(),
										name : name.getValue()
									},
									success : function(res, opt){
										var jr = Ext.decode(res.responseText);
										if(jr.success){
											Ext.example.msg(jr.title, jr.msg);
											operateRegionWin.hide();
											regionTree.getRootNode().reload();
											Ext.getCmp('btnSearchForTable').handler();
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
								operateRegionWin.hide();
							}
						}]
					});
				}
				operateRegionWin.show();
				operateRegionWin.center();
				Ext.getCmp('hideRegionId').setValue(node.attributes.regionId);
				Ext.getCmp('txtRegionName').setValue(node.attributes.regionName);
			}
		}, {
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function() {
				Ext.getDom('displaySearchRegion').innerHTML = '----';
				regionTree.getRootNode().reload();
				Ext.getCmp('btnSearchForTable').handler();
			}
		}],
		listeners : {
			render : function(thiz){
				thiz.getRootNode().reload();
			},
			click : function(node){
				Ext.getDom('displaySearchRegion').innerHTML = node.text ;
			},
			dblclick : function(){
				Ext.getCmp('btnSearchForTable').handler();
			}
		}
	});
}
/**
 * 
 */
function initGrid(){
	var tableBasicGridTbar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '区域', 'displaySearchRegion', '----')
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;编号:'
		}, {
			xtype : 'numberfield',
			id : 'numSearchForTableAlias',
			width : 100,
			style : 'text-align:left;'
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;名称:'
		}, {
			xtype : 'textfield',
			id : 'txtSearchForTableName',
			width : 100
		}, '->', {
			text : '搜索',
			id : 'btnSearchForTable',
			iconCls : 'btn_search',
			handler : function(){
				var node = regionTree.getSelectionModel().getSelectedNode();
				var gs = tableBasicGrid.getStore();
				if (node && node.attributes.regionId != -1) {
					gs.baseParams['regionId'] = node.attributes.regionId;
				}else{
					gs.baseParams['regionId'] = '';
				}
				gs.baseParams['alias'] = Ext.getCmp('numSearchForTableAlias').getValue();
				gs.baseParams['name'] = Ext.getCmp('txtSearchForTableName').getValue();
				gs.load({
					params : {
						start : 0,
						limit : tableBasicGrid.getBottomToolbar().pageSize
					}
				});
			}
		}]
	});
	tableBasicGrid = createGridPanel(
		'tableBasicGrid',
		'餐台管理',
		'',
		'',
		'../../QueryTable.do',
		[
			[false, false, false, true], 
			['餐台编号', 'alias'],
			['区域', 'region.name'],
			['餐台名称', 'name'],
			['最低消费', 'minimumCost',,'right', 'Ext.ux.txtFormat.gridDou'],
			['服务费率', 'serviceRate',,'right', 'Ext.ux.txtFormat.gridDou'],
			['就餐人数', 'customNum',,'right'],
			['餐台状态', 'statusText',,'center'],
			['操作', 'operate', 200 ,'center', 'tableBasicGridOperateRenderer']
		],
		TableRecord.getKeys(),
		[['isPaging', true], ['restaurantID', restaurantID],  ['dataSource', 'normal']],
		GRID_PADDING_LIMIT_20,
		'',
		tableBasicGridTbar
	);	
	tableBasicGrid.region = 'center';
	tableBasicGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnSearchForTable').handler();
		}
	}];
	
	tableBasicGrid.on('render', function(){
		Ext.getCmp('btnSearchForTable').handler();
	});
	tableBasicGrid.on('rowdblclick', function(){
		updateTableBasicHandler();
	});
	
	
}
/**
 * 
 */
function initWin(){
	tableBasicWin = new Ext.Window({
		title : '&nbsp;',
		closable : false,
		modal : true,
		resizeble : false,
		width : 250,
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				tableBasicWin.hide();
			}
		}],
		items : [{
			layout : 'form',
			labelWidth : 60,
			frame : true,
			defaults : {
				width : 150
			},
			items : [{
				xtype : 'hidden',
				id : 'hideTableId'
			}, {
				xtype : 'numberfield',
				id : 'numTableAlias',
				fieldLabel : '编号',
				allowBlank : false,
				disabled : true
			}, {
				xtype : 'textfield',
				id : 'txtTableName',
				fieldLabel : '名称'
			}, {
				xtype : 'combo',
				id : 'comboTableRegion',
				fieldLabel : '所属区域',
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
				allowBlank : false
			}, {
				xtype : 'numberfield',
				id : 'numMinimumCost',
				fieldLabel : '最低消费',
				allowBlank : false,
				minValue : 0,
				value : 0
			}, {
				xtype : 'numberfield',
				id : 'numServiceRate',
				fieldLabel : '服务费率',
				allowBlank : false,
				value : 0,
				validator : function(v){
					if(v < 0 || v > 1){
						return "服务费率在 0.00 至 1.00 之间.";
					}else{
						return true;
					}
				}
			}]
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSaveOperateTable').handler();
			}
		}, {
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				tableBasicWin.hide();
			}
		}],
		bbar : ['->', {
			text : '保存',
			id : 'btnSaveOperateTable',
			iconCls : 'btn_save',
			handler : function(){
				var id = Ext.getCmp('hideTableId');
				var alias = Ext.getCmp('numTableAlias');
				var name = Ext.getCmp('txtTableName');
				var region = Ext.getCmp('comboTableRegion');
				var minimumCost = Ext.getCmp('numMinimumCost');
				var serviceRate = Ext.getCmp('numServiceRate');
				
				if(!region.isValid() || !minimumCost.isValid() || !serviceRate.isValid()){
					return;
				}
				if(tableBasicWin.otype == Ext.ux.otype['insert']){
					if(!alias.isValid()){
						return;
					}
				}
				
				Ext.Ajax.request({
					url : '../../OperateTable.do',
					params : {
						dataSource : tableBasicWin.otype.toLowerCase(),
						
						alias : alias.getValue(),
						id : id.getValue(),
						name : name.getValue(),
						regionId : region.getValue(),
						minimumCost : minimumCost.getValue(),
						serviceRate : serviceRate.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							tableBasicWin.hide();
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnSearchForTable').handler();
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
				tableBasicWin.hide();
			}
		}]
	});
	tableBasicWin.render(document.body);
}

//----------
var btnAddTable = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加新餐台',
	handler : function(btn){
		insertTableBasicHandler();
	}
});

var btnGetBack = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'BasicMgrProtal.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
	}
});

var btnLoginOut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});

Ext.onReady(function(){
	//
	initTree();
	//
	initGrid();
	getOperatorName("../../");
	//
	initWin();
	
	new Ext.Panel({
		title : '区域管理',
		renderTo : 'divRegion',
		frame : true,
		layout : 'border',
		width : parseInt(Ext.getDom('divRegion').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divRegion').parentElement.style.height.replace(/px/g,'')),
		items : [regionTree, tableBasicGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddTable, '->', btnGetBack, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, btnLoginOut ]
		})
	});
	
});