//----------------------load--------
function priceBaiscGridRenderer(){
	return ''
		   + '<a href="javascript:updateFoodPricePlanWinHandler()">修改</a>';
};

function initData(){
	Ext.Ajax.request({
		url : '../../QueryMenu.do',
		params : {
			dataSource : 'kitchens',
			restaurantID : restaurantID,
			type : 3
		},
		success : function(res, opt) {
			var jr = Ext.decode(res.responseText);
			if(jr.success == true) {
				kitchenData = jr;
				kitchenData.root.unshift({
					id : -1,
					name : '全部'
				});
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt) {
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
};

function initTree(){
	var tbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				insertPricePlanWinHandler();
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				updatePricePlanWinHandler();
			}
		}, {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				deletePricePlanWinHandler();
			}
		}, {
			text : '刷新',
			id : 'btnRefreshPricePlanTree',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getDom('pricePlanShowName').innerHTML = '----';
				pricePlanTree.getRootNode().reload();
			}
		}]
	});
	
	pricePlanTree = new Ext.tree.TreePanel({
		id : 'prp_pricePlanTree',
		title : '方案',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		tbar : tbar,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryPricePlanTree.do',
			baseParams : {
				
				restaurantID : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部方案',
	        leaf : false,
	        border : true,
	        pricePlanID : '-1',
	        pricePlanName : '全部方案',
	        listeners : {
	        	expand : function(e){
	        		pricePlanData.root = [];
	        		for(var i = 0; i < e.childNodes.length; i++){
	        			var temp = e.childNodes[i];
	        			if(temp.attributes['statusValue'] == 1){
	        				temp.setText(temp.attributes['pricePlanName']+'<font color="red">(默认方案)</font>');
	        				temp.select();
	        				temp.fireEvent('click', temp);
	        				temp.fireEvent('dblclick', temp);	
	        			}
	        			pricePlanData.root.push({
        					pricePlanID : temp.attributes['pricePlanID'],
        					pricePlanName : temp.attributes['pricePlanName'],
        					statusValue : temp.attributes['statusValue']
        				});	
	        		}
	        	}
	        }
		}),
		listeners : {
			click : function(e){
				Ext.getDom('pricePlanShowName').innerHTML = e.attributes['pricePlanName'];
				Ext.getCmp('btnSearchFoodPricePlan').handler();
			}
		}
	});	
};

function initGrid(){
	var tbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '方案', 'pricePlanShowName', '----')
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		}, {
			xtype : 'tbtext',
			text : '过滤:'
		}, {
			xtype : 'combo',
			id : 'comboSearchType',
			readOnly : false,
			forceSelection : true,
			width : 100,
			value : 0,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[0, '全部'], [1, '菜品编号'], [2, '菜品价格'], [3, '菜品名称'], [4, '厨房']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(thiz, record, index){
					var textValue = Ext.getCmp('txtSearchTextValue');
					var operator = Ext.getCmp('comboOperator');
					var numberValue = Ext.getCmp('txtSearchNumberValue');
					var kitchen = Ext.getCmp('comboSearchKitchen');
					pmObj.searchType = index;
					if(index == 0){
						textValue.setVisible(false);
						operator.setVisible(false);
						numberValue.setVisible(false);
						kitchen.setVisible(false);
						pmObj.searchValue = '';
					}else if(index == 1){
						textValue.setVisible(false);
						operator.setVisible(true);
						numberValue.setVisible(true);
						kitchen.setVisible(false);
						operator.setValue(1);
						numberValue.setValue();
						pmObj.searchValue = operator.getId()+'<|>'+numberValue.getId();
					}else if(index == 2){
						textValue.setVisible(false);
						operator.setVisible(true);
						numberValue.setVisible(true);
						kitchen.setVisible(false);
						operator.setValue(1);
						numberValue.setValue();
						pmObj.searchValue = operator.getId()+'<|>'+numberValue.getId();
					}else if(index == 3){
						textValue.setVisible(true);
						operator.setVisible(false);
						numberValue.setVisible(false);
						kitchen.setVisible(false);
						textValue.setValue();
						pmObj.searchValue = textValue.getId();
					}else if(index == 4){
						textValue.setVisible(false);
						operator.setVisible(false);
						numberValue.setVisible(false);
						kitchen.setVisible(true);
						kitchen.store.loadData(kitchenData);
						kitchen.setValue(-1);
						pmObj.searchValue = kitchen.getId();
					}
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		}, {
			xtype : 'textfield',
			id : 'txtSearchTextValue',
			hidden : true,
			width : 100
		}, {
			xtype : 'combo',
			hidden : true,
			hideLabel : true,
			forceSelection : true,
			width : 100,
			id : 'comboOperator',
			value : 0,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[1,'等于'], [2,'大于等于'], [3,'小于等于']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			readOnly : false
		}, {
			xtype : 'numberfield',
			id : 'txtSearchNumberValue',
			style : 'text-align:left;',
			hidden : true,
			width : 100
		}, {
			xtype : 'combo',
			id : 'comboSearchKitchen',
			hidden : true,
			forceSelection : true,
			readOnly : false,
			width : 100,
			store : new Ext.data.JsonStore({
				root : 'root',
				fields : [ 'id', 'name' ]
			}),
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(){
					Ext.getCmp('btnSearchFoodPricePlan').handler();
				}
			}
		}, '->', {
			text : '搜索',
			id : 'btnSearchFoodPricePlan',
			iconCls : 'btn_search',
			handler : function(){
				var pricePlanNode = pricePlanTree.getSelectionModel().getSelectedNode();
				var sOperator = '', sValue = '', sPrciePlan = '';
				if(pmObj.searchType == 0){
					sOperator = '';
					sValue = '';
				}else if(pmObj.searchType == 1 || pmObj.searchType == 2){
					var temp = pmObj.searchValue.split('<|>');
					sOperator = Ext.getCmp(temp[0]).getValue();
					sValue = Ext.getCmp(temp[1]).getValue();
				}else{
					sValue = Ext.getCmp(pmObj.searchValue).getValue();
				}
				if(pricePlanNode != null && typeof pricePlanNode.attributes['pricePlanID'] == 'number'){
					sPrciePlan = pricePlanNode.attributes['pricePlanID'];
				}else{
					sPrciePlan = '';
				}
				var gs = priceBaiscGrid.getStore();
				gs.baseParams['searchType'] = pmObj.searchType;
				gs.baseParams['searchOperator'] = sOperator;
				gs.baseParams['searchValue'] = sValue;
				gs.baseParams['searchPrciePlan'] = sPrciePlan;
				gs.load({
					params : {
						start : 0,
						limit : GRID_PADDING_LIMIT_20
					}
				});
			}
		}, {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getCmp('comboSearchType').setValue(0);
				Ext.getCmp('comboSearchType').fireEvent('select', null, null, 0);
				priceBaiscGrid.getStore().baseParams['searchPrciePlan'] = null;
				Ext.getCmp('btnSearchFoodPricePlan').handler();
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				updateFoodPricePlanWinHandler();
			}
		}]
	});
	priceBaiscGrid = createGridPanel(
		'priceBaiscGrid',
		'菜品价格',
		'',
		'',
		'../../QueryFoodPricePlan.do',
		[
			[true, false, false, true], 
			//['方案编号', 'planId'] , 
			['方案名称', 'pricePlan.name'], 
			['菜品编号', 'foodAlias'],
			['菜品名称', 'foodName'], 
			['菜品价格', 'unitPrice',,'right', 'Ext.ux.txtFormat.gridDou'], 
			['厨房名称', 'kitchenName'],
			['操作', 'operation', '', 'center', 'priceBaiscGridRenderer']
		],
		FoodPricePlanRecord.getKeys(),
		[ ['isPaging', true], ['restaurantID', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		tbar
	);	
	priceBaiscGrid.region = 'center';
	priceBaiscGrid.on('rowdblclick', function(){
		updateFoodPricePlanWinHandler();
	});
	
	priceBaiscGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnSearchFoodPricePlan').handler();
		}
	}];
};

function initWin(){
	oPricePlanWin = Ext.getCmp('pm_oPricePlanWin');
	if(!oPricePlanWin){
		oPricePlanWin = new Ext.Window({
			id : 'pm_oPricePlanWin',
			modal : true,
			resizable : false,
			closable : false,
			width : 238,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 70,
				width : 237,
				defaults : {
					width : 120
				},
				items : [{
					xtype : 'hidden',
					id : 'txtPricePlanID'
				}, {
					xtype : 'textfield',
					id : 'txtPricePlanName',
					fieldLabel : '方案名称',
					allowBlank : false,
					blankText : '方案名称不能为空.',
					validator : function(v){
						if(Ext.util.Format.trim(v).length > 0){
							return true;
						}else{
							return '方案名称不能为空.';
						}
					}
				}, {
					xtype : 'combo',
					id : 'comboPricePlanStatus',
					fieldLabel : '状态',
					forceSelection : true,
					value : 0,
					store : new Ext.data.SimpleStore({
						fields : ['value', 'text'],
						data : pricePlanStatusData
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					readOnly : false
				}, {
					xtype : 'label',
					style : 'color:green;font-szie:12px;',
					text : '说明:  状态为必选项, 且唯一, 当状态为默认则结账时默认使用该价格方案.'
				}, {
					xtype : 'combo',
					id : 'comboCopyPricePlan',
					fieldLabel : '复制方案',
					forceSelection : true,
					store : new Ext.data.JsonStore({
						root : 'root',
						fields : ['pricePlanId', 'pricePlanName']
					}),
					valueField : 'pricePlanId',
					displayField : 'pricePlanName',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true
				}]
			}],
			bbar : ['->', {
				text : '保存',
				id : 'btnSavePricePlan',
				iconCls : 'btn_save',
				handler : function(){
					var name = Ext.getCmp('txtPricePlanName');
					var status = Ext.getCmp('comboPricePlanStatus');
					
					var pricePlan = operationPricePlanData({ 
						type : pmObj.operation['get'] 
					}).data;
					var action = '';
					if(oPricePlanWin.otype == pmObj.operation['insert']){
						if(!name.isValid()){
							return;
						}
						action = '../../InsertPricePlan.do';
						pricePlan.id = pricePlan.copyID == null ? '' : pricePlan.copyID;
					}else if(oPricePlanWin.otype == pmObj.operation['update']){
						if(!status.isValid() || !status.isValid()){
							return;
						}
						action = '../../UpdatePricePlan.do';
					}else{
						return;
					}
					// 删除多余字段
					(delete pricePlan['copyID']);
					Ext.Ajax.request({
						url : action,
						params : {
							
							restaurantID : restaurantID,
							name : pricePlan.name,
							id : pricePlan.id,
							status : pricePlan.statusValue
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								oPricePlanWin.hide();
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnRefreshPricePlanTree').handler();
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
				text : '关闭',
				id : 'btnCloseOPricePlanWin',
				iconCls : 'btn_close',
				handler : function(){
					oPricePlanWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnSavePricePlan').handler();
				}
			}, {
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					Ext.getCmp('btnCloseOPricePlanWin').handler();
				}
			}],
			listeners : {
				show : function(){
					Ext.getCmp('comboCopyPricePlan').store.loadData(pricePlanData);
				}
			}
		});
	}

	oPricePlanWin.render(document.body);
	
	//--------------------------------------------------
	oPriceBasicWin = new Ext.Window({
		modal : true,
		resizable : false,
		closable : false,
		width : 230,
		items : [{
			xtype : 'form',
			layout : 'form',
			frame : true,
			labelWidth : 70,
			labelAlign : 'right',
			defaults : {
				width : 110
			},
			items : [{
				xtype : 'hidden',
				id : 'hideFoodPricePlanID'
			}, {
				xtype : 'hidden',
				id : 'hidePricePlanID'
			}, {
				xtype : 'textfield',
				id : 'txtFoodPricePlanName',
				fieldLabel : '方案名称',
				disabled : true
			}, {
				xtype : 'textfield',
				id : 'txtFoodName',
				fieldLabel : '菜品名称',
				disabled : true
					
			}, {
				xtype : 'numberfield',
				id : 'numFoodUnitPrice',
				fieldLabel : '菜品价格'
			}]
		}],
		bbar : ['->', {
			text : '保存',
			id : 'btnSaveFoodPricePlan',
			iconCls : 'btn_save',
			handler : function(){
				var unitPrice = Ext.getCmp('numFoodUnitPrice');
				if(!unitPrice.isValid()){
					return;
				}
				var foodPricePlan = operationFoodPricePlanData({
					type : pmObj.operation['get']
				}).data;
				
				Ext.Ajax.request({
					url : '../../UpdateFoodPricePlan.do',
					params : {
						foodPricePlan : Ext.encode(foodPricePlan)
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.getCmp('btnCloseFoodPricePlan').handler();
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnSearchFoodPricePlan').handler();
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
			text : '关闭',
			id : 'btnCloseFoodPricePlan',
			iconCls : 'btn_close',
			handler : function(){
				oPriceBasicWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSaveFoodPricePlan').handler();
			}
		}, {
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				Ext.getCmp('btnCloseFoodPricePlan').handler();
			}
		}]
	});
	oPriceBasicWin.render(document.body);
	
};
//-----------------


/**********************************************************************/
insertPricePlanWinHandler = function(){
	pricePlanOperationHandler({
		type : pmObj.operation['insert']
	});
};

updatePricePlanWinHandler = function(){
	pricePlanOperationHandler({
		type : pmObj.operation['update']
	});
};

deletePricePlanWinHandler = function(){
	pricePlanOperationHandler({
		type : pmObj.operation['delete']
	});
};

function pricePlanOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	oPricePlanWin.otype = c.type;
	
	var copyID = Ext.getCmp('comboCopyPricePlan');
	
	if(c.type == pmObj.operation['insert']){
		operationPricePlanData({ 
			type : pmObj.operation['set'] 
		});
		Ext.query('div label[for='+copyID.getId()+']')[0].parentElement.setAttribute('style', "display:block");
		
		oPricePlanWin.setTitle("添加方案");
		oPricePlanWin.show();
		oPricePlanWin.center();
	}else if(c.type == pmObj.operation['update']){
		var sn = Ext.ux.getSelNode(pricePlanTree);
		if(!sn || sn.attributes.pricePlanID == -1){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			return;
		}
		operationPricePlanData({ 
			type : pmObj.operation['set'],
			data : {
				id : sn.attributes['pricePlanID'],
				name : sn.attributes['pricePlanName'],
				statusValue : sn.attributes['statusValue']
			}
		});
		
		Ext.query('div label[for='+copyID.getId()+']')[0].parentElement.setAttribute('style', "display:none");
		
		oPricePlanWin.setTitle("修改方案");
		oPricePlanWin.show();
		oPricePlanWin.center();
	}else if(c.type == pmObj.operation['delete']){
		var sn = Ext.ux.getSelNode(pricePlanTree);
		if(!sn || sn.attributes['pricePlanID'] == -1){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			return;
		}
		if(sn.attributes['status'] == 1){
			Ext.example.msg('提示', '该价格方案为活动状态, 正在使用中的不允许删除.');
			return;
		}
		Ext.Msg.show({
			title : '提示',
			msg : '是否删除方案:<font color="#FF0000">'+sn.text+'</font>?<br/>重要:同时删除该方案下所有菜品价格信息.',
			icon : Ext.Msg.QUESTION,
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../DeletePricePlan.do',
						params : {
							restaurantID : restaurantID,
							id : sn.attributes['pricePlanID']
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, String.format(Ext.ux.txtFormat.deleteSuccess, sn.text));
								Ext.getCmp('btnRefreshPricePlanTree').handler();
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
	}else{
		Ext.example.msg('错误', '未知操作类型, 请联系管理员');
	}
};

function operationPricePlanData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var id = Ext.getCmp('txtPricePlanID');
	var name = Ext.getCmp('txtPricePlanName');
	var copyID = Ext.getCmp('comboCopyPricePlan');
	var status = Ext.getCmp('comboPricePlanStatus');
	if(c.type == pmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		id.setValue(data['id']);
		name.setValue(data['name']);
		copyID.setValue();
		status.setValue(typeof data['statusValue'] == 'undefined' ? 0 : data['statusValue']);
	}else if(c.type == pmObj.operation['get']){
		data = {
			restaurantID : restaurantID,
			name : name.getValue(),
			id : id.getValue(),
			statusValue : status.getValue(),
			copyID : copyID.getRawValue() == '' ? null : copyID.getValue()
		};
		c.data = data;
	}
	name.clearInvalid();
	status.clearInvalid();
	return c;
};

/**********************************************************************/
function updateFoodPricePlanWinHandler(){
	foodPricePlanOperationHandler({
		type : pmObj.operation['update']
	});
};

function foodPricePlanOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	oPriceBasicWin.otype = c.type;
	
	if(c.type == pmObj.operation['update']){
		var sd = Ext.ux.getSelData(priceBaiscGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个菜品再进行操作.');
			return;
		}
		operationFoodPricePlanData({
			type : pmObj.operation['set'],
			data : sd
		});
		oPriceBasicWin.setTitle('修改菜品价格');
		oPriceBasicWin.show();
		oPriceBasicWin.center();
	}else{
		Ext.example.msg('错误', '未知操作类型, 请联系管理员');
	}
};

function operationFoodPricePlanData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var foodID = Ext.getCmp('hideFoodPricePlanID');
	var pricePlanID = Ext.getCmp('hidePricePlanID');
	var pricePlanName = Ext.getCmp('txtFoodPricePlanName');
	var foodName = Ext.getCmp('txtFoodName');
	var unitPrice = Ext.getCmp('numFoodUnitPrice');
	if(c.type == pmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		foodID.setValue(data['foodId']);
		pricePlanID.setValue(data['planId']);
		pricePlanName.setValue(data['pricePlan.name']);
		foodName.setValue(data['foodName']);
		unitPrice.setValue(data['unitPrice']);
	}else if(c.type == pmObj.operation['get']){
		data = {
			restaurantID : restaurantID,
			planId : pricePlanID.getValue(),
			foodId : foodID.getValue(),
			unitPrice : unitPrice.getValue()
		};
		c.data = data;
	}
	unitPrice.clearInvalid();
	return c;
};

/**********************************************************************/
Ext.onReady(function(){
	initData();
	initTree();
	initGrid();
	
	new Ext.Panel({
		renderTo : 'divPrice',
		width : parseInt(Ext.getDom('divPrice').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divPrice').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		frame : true,
		items : [pricePlanTree, priceBaiscGrid]
	});
	
	initWin();
	
});

$(function(){
	var obj = {treeId : 'prp_pricePlanTree', option : [{name : '修改', fn : "updatePricePlanWinHandler()"}, {name : '删除', fn : "deletePricePlanWinHandler()"}]};
	showFloatOption(obj);
});