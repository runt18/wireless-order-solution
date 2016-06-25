	var currPageIndex = 0, currRowIndex = -1, conditionType = '',
	//多价格数量
	multiFoodPriceCount = 0,
	
	dishesPageRecordCount = 50,
	dishesStaticRecordCount = 11,
	
	kitchenData = [],
	materialCateComboData = [],
	
	materialData = [],
	materialComboData = [],
	materialComboDisplayData = [],
	
	stockStatusData = [[1, '无库存'], [2, '商品出库'], [3, '原料出库']],
		
	mmObj = {
		operation : {
				insert : 'INSERT',
				update : 'UPDATE',
				select : 'SELECT',
				set : 'set',
				get : 'get',
				img : {
					upload : 'UPLOAD',
					del : 'DELETE'
				}
		}	
	},
	
	imgTypeTmp = ['jpg', 'jpeg', 'gif', 'png', 'bmp'],
	
	tabItemsHeight = 440,
	
	foodImageUpdateLoaddingMask,
	
	kitchenTreeForSreach,
	menuGrid,
	foodOperationWin = null,
	foodPricePlanWin = null,
	updateDeptWin, updateKitchenWin, foodRelationOperationWin, pricePlanWin, pricePlanGrid, pricePlanOperatePanel,
	
	bar = {treeId : 'kitchenTreeForSreach', option :[{name : '修改', fn : "floatBarUpdateHandler()"}, {name : '删除', fn : "floatBarDeleteHandler()"}, {name : '置顶', fn : "floatBarSetTopHandler()"}]},
	
	food_pricePlans, tartDeptNode;
	
//-------------lib.js---------
function floatBarDeleteHandler(){
	var tn = Ext.ux.getSelNode(kitchenTreeForSreach);
	if(!tn){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	if(typeof tn.attributes.kid != 'undefined'){
		deleteKitchenHandler(tn);
	}else{
		deleteDeptHandler(tn);
	}
}

function floatBarUpdateHandler(){
	var tn = Ext.ux.getSelNode(kitchenTreeForSreach);
	if(!tn){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	if(typeof tn.attributes.kid != 'undefined'){
		updateKitchenWin.otype = 'update';
		operateKitchenHandler(tn);
	}else{
		updateDeptWin.otype = 'update';
		operateDeptHandler(tn);
	}
}

function floatBarSetTopHandler(){
	var tn = Ext.ux.getSelNode(kitchenTreeForSreach);
	if(!tn){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	kitchenTreeForSreach.fireEvent('nodedrop', 
		{tree : kitchenTreeForSreach, target : tn.parentNode.firstChild, dropNode : tn });
}


function foodRelationWinShow(){
	
	foodRelationOperationWin.show();
	foodRelationOperationWin.center();
	foodRelationOperationWin.setTitle(Ext.ux.getSelData(menuGrid)['name']);
	foodOperationHandler({
		win : 'relation',
		type : mmObj.operation.select
	});
}


function operateKitchenHandler(node){
	var kitchenID = Ext.getCmp('txtKitchenID');
	var kitchenName = Ext.getCmp('txtKitchenName');
	var kitchenDept = Ext.getCmp('comboKitchenDept');	
	var isAllowTemp = Ext.getCmp('comboIsAllowTemp');
	initDeptComboData();
	
	if(updateKitchenWin.otype == 'insert'){
		kitchenID.setValue();
		kitchenName.setValue();
		kitchenName.clearInvalid();
		isAllowTemp.setValue(false);
		node = kitchenTreeForSreach.getSelectionModel().getSelectedNode();
		if(node){
			kitchenDept.setValue(node.attributes.deptID);
		}
		kitchenDept.clearInvalid();
		
	}else if(updateKitchenWin.otype == 'update'){
		
		kitchenID.setValue(node.attributes.kid);
		kitchenName.setValue(node.text);
		kitchenDept.setValue(node.attributes.belongDept);
		isAllowTemp.setValue(node.attributes.isAllowTemp);
		
	}
	updateKitchenWin.show();
	updateKitchenWin.center();
	kitchenName.focus(true, 100);	
	
}

function deleteKitchenHandler(node){
	Ext.Msg.confirm(
		'提示',
		'是否删除: ' + node.text,
		function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateKitchen.do',
					params : {
						kitchenID : node.attributes.kid,
						dataSource : 'removeKitchen'
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, String.format(Ext.ux.txtFormat.deleteSuccess, node.text));
							kitchenTreeForSreach.getRootNode().reload();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
					}
				});
			}
		}
	);
}

function updateKitchenHandler(){
	updateKitchenWin.otype = 'update';
	operateKitchenHandler();
}

function initDeptComboData(){
	var combo_deptData = [];
	var thiz = Ext.getCmp('comboKitchenDept');
	Ext.Ajax.request({
		url : '../../OperateDept.do',
		params : {
			dataSource : 'getByCond'
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			for(var i = 0; i < jr.root.length; i++){
				combo_deptData.push([jr.root[i]['id'], jr.root[i]['name']]);
			}
			thiz.store.loadData(combo_deptData);
		},
		fialure : function(res, opt){
			thiz.store.loadData(combo_deptData);
		}
	});
}

function initKitchenComboData(){
	var combo_kitchenData = [];
	var combo_kitchen = Ext.getCmp('cmbBasicForKitchenAlias');
	Ext.Ajax.request({
		url : '../../QueryKitchen.do',
		params : {
			dataSource : 'normal'
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			for(var i = 0; i < jr.root.length; i++){
				combo_kitchenData.push([jr.root[i]['id'], jr.root[i]['name']]);
			}
			combo_kitchen.store.loadData(combo_kitchenData);
		},
		fialure : function(res, opt){
			combo_kitchen.store.loadData(combo_kitchenData);
		}
	});
}

function kitchenWinShow(){

	updateKitchenWin = Ext.getCmp('dept_updateKitchenWin');
	if(!updateKitchenWin){
		updateKitchenWin = new Ext.Window({
			id : 'dept_updateKitchenWin',
			title : '添加分厨信息',
			closable : false,
			resizable : false,
			modal : true,
			width : 245,
			items : [{
				xtype : 'form',
				layout : 'form',
				width : 245,
				
				labelWidth : 65,
				frame : true,
				items : [{
					xtype : 'hidden',
					id : 'txtKitchenID',
					fieldLabel : '厨房编号'
				}, {
					xtype : 'textfield',
					id : 'txtKitchenName',
					width : 130,
					fieldLabel : '厨房名称',
					allowBlank : false
				}, {
 	    	    	xtype : 'combo',
 	    	    	id : 'comboKitchenDept',
 	    	    	fieldLabel : '所属部门',
 	    	    	width : 130,
 	    	    	store : new Ext.data.SimpleStore({
						fields : [ 'deptID', 'deptName']
					}),
					valueField : 'deptID',
					displayField : 'deptName',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					blankText : '该项部门不能为空.'
 	    	    }, {
 	    	    	xtype : 'combo',
 	    	    	id : 'comboIsAllowTemp',
 	    	    	fieldLabel : '允许临时菜',
 	    	    	width : 130,
 	    	    	value : 0,
 	    	    	store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text'],
						data : [[false,'否'], [true, '是']]
					}),
					valueField : 'value',
					displayField : 'text',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					blankText : '该项不能为空.'
 	    	    }]
			}],
			bbar : [
				'->',
				{
					text : '保存',
					id : 'btnSaveUpdateKitchen',
					iconCls : 'btn_save',
					handler : function(){
						
						var kitchenID = Ext.getCmp('txtKitchenID');
						var kitchenName = Ext.getCmp('txtKitchenName');
						var kitchenDept = Ext.getCmp('comboKitchenDept');
						var isAllowTemp = Ext.getCmp('comboIsAllowTemp');
						var dataSource = '';
						
						if(!kitchenName.isValid() || !kitchenDept.isValid() || !isAllowTemp.isValid()){
							return;
						}
						
						if(updateKitchenWin.otype == 'insert'){
							dataSource = 'addKitchen';
							
						}else if(updateKitchenWin.otype == 'update'){
							dataSource = 'updateKitchen';
						}
						
						var save = Ext.getCmp('btnSaveUpdateKitchen');
						var cancel = Ext.getCmp('btnCancelUpdateKitchen');
						
						save.setDisabled(true);
						cancel.setDisabled(true);
						Ext.Ajax.request({
							url : '../../OperateKitchen.do',
							params : {
								dataSource : dataSource,
								kitchenID : kitchenID.getValue(),
								kitchenName : kitchenName.getValue(),
								deptID : kitchenDept.getValue(),
								isAllowTemp : isAllowTemp.getValue()
							},
							success : function(res, opt){
								var jr = Ext.util.JSON.decode(res.responseText);
								if(jr.success){
									Ext.example.msg(jr.title, jr.msg);
									updateKitchenWin.hide();
									kitchenTreeForSreach.getRootNode().reload();
								}else{
									Ext.ux.showMsg(jr);
								}
								save.setDisabled(false);
								cancel.setDisabled(false);
							},
							failure : function(res, opt) {
								Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
								save.setDisabled(false);
								cancel.setDisabled(false);
							}
						});
						
					}
				}, {
					text : '关闭',
					id : 'btnCancelUpdateKitchen',
					iconCls : 'btn_close',
					handler : function(){
						updateKitchenWin.hide();
					}
				}
			],
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnSaveUpdateKitchen').handler();
				 },
				 scope : this 
			 }]
		});
	}
};

function operateDeptHandler(node){
	var deptId = Ext.getCmp('txtDeptID');
	var deptName = Ext.getCmp("txtDeptName");
	deptName.focus(true, 100);				
	if(updateDeptWin.otype == 'update'){
		if(node.attributes.type == 1){
			Ext.example.msg('提示', '<<font color="red">' + node.text + '</font>>为系统保留部门,不允许修改.');
			return;
		}
		updateDeptWin.setTitle("修改部门信息");
		deptId.setValue(node.attributes.deptID);
		deptName.setValue(node.text);
	}else if(updateDeptWin.otype == 'insert'){
		updateDeptWin.setTitle("添加部门");
		deptId.setValue();
		deptName.setValue();
		deptName.clearInvalid();
	}
	updateDeptWin.show();
	updateDeptWin.center();
	deptName.focus(true, 100);
}

function updateDeptHandler(){
	updateDeptWin.otype = 'update';
	operateDeptHandler();
}

function deleteDeptHandler(node){
	Ext.Msg.confirm(
		'提示',
		'是否删除: ' + node.text,
		function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateDept.do',
					params : {
						deptID : node.attributes.deptID,
						dataSource : 'removeDept'
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, String.format(Ext.ux.txtFormat.deleteSuccess, node.text));
							kitchenTreeForSreach.getRootNode().reload();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
					}
				});
			}
		}
	);
}

function deptWinShow(){
	if(!updateDeptWin){
		updateDeptWin = new Ext.Window({
			title : '添加部门',
			closable : false,
			resizable : false,
			modal : true,
			width : 230,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 65,
				items : [{
					xtype : 'hidden',
					id : 'txtDeptID'
				}, {
					xtype : 'textfield',
					id : 'txtDeptName',
					fieldLabel : '部门名称',
					width : 130
				}]
			}],
			bbar : [
			'->',
			{
				text : '保存',
				id : 'btnSaveUpdateDept',
				iconCls : 'btn_save',
				handler : function(){
					var deptID = Ext.getCmp('txtDeptID');
					var deptName = Ext.getCmp('txtDeptName');
					var dataSource = '';
					if(updateDeptWin.otype == 'insert'){
						dataSource = 'addDept';
					}else if(updateDeptWin.otype == 'update'){
						dataSource = 'updateDept';
					}
					
					Ext.Ajax.request({
						url : '../../OperateDept.do',
						params : {
							dataSource : dataSource,
							deptID : deptID.getValue(),
							deptName : deptName.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							Ext.example.msg(jr.title, jr.msg);
							updateDeptWin.hide();
							kitchenTreeForSreach.getRootNode().reload();
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					updateDeptWin.hide();
				}
			}],
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnSaveUpdateDept').handler();
				 },
				 scope : this 
			 }]
		});
	}

}

/**
 * 菜品搜索
 */
function searchMenuHandler(){
	var baseParams = {
		'isPaging' : true,
		'restaurantId' : restaurantID
	};
	// 分厨
	var sn = kitchenTreeForSreach.getSelectionModel().getSelectedNode();
	if(sn){
		baseParams['kitchen'] = typeof sn.attributes.kid != 'undefined' ? sn.attributes.kid : '';
		baseParams['deptId'] = typeof sn.attributes.deptID != 'undefined' ? sn.attributes.deptID : "";
	}
		
	// 编号, 名称, 拼音, 价钱, 库存管理
		
	var queryType = Ext.getCmp('menu_filter').getValue();
	if(queryType != '全部' && queryType != 0){	
		if(queryType == 1){
			baseParams['alias'] = Ext.getCmp('numfieldForGridSearch').getValue();
			baseParams['operator'] = Ext.getCmp('comboOperatorForGridSearch').getValue();
		}else if(queryType == 2){
			baseParams['name'] = Ext.getCmp('textfieldForGridSearch').getValue();
		}else if(queryType == 3){
			//baseParams['pinyin'] = Ext.getCmp('textfieldForGridSearch').getValue();
		}else if(queryType == 4){
			baseParams['price'] = Ext.getCmp('numfieldForGridSearch').getValue();
			baseParams['operator'] = Ext.getCmp('comboOperatorForGridSearch').getValue();
		}else if(queryType == 6){
			var stock = Ext.getCmp('comboStockStatusForSearch');
			baseParams['stockStatus'] = stock.getRawValue() != '' && stock.getValue() > 0 ? stock.getValue() : '';
		}
	}
	// 状态
	var isSpecial = Ext.getCmp('specialCheckbox').getValue();
	var isRecommend = Ext.getCmp('recommendCheckbox').getValue();
	var isStop = Ext.getCmp('stopCheckbox').getValue();
	var isFree = Ext.getCmp('freeCheckbox').getValue();
	var isCurrPrice = Ext.getCmp('currPriceCheckbox').getValue();
	var isCombination = Ext.getCmp('combinationCheckbox').getValue();
	var isHot = Ext.getCmp('hotCheckbox').getValue();
	var isWeight = Ext.getCmp('weightCheckbox').getValue();
	var isCommission = Ext.getCmp('commissionCheckbox').getValue();
	var isTempFood = Ext.getCmp('tempFoodCheckbox').getValue();
	if(isSpecial)
		baseParams['isSpecial'] = true;
	if(isRecommend)
		baseParams['isRecommend'] = true;
	if(isStop)
		baseParams['isStop'] = true;
	if(isFree)
		baseParams['isFree'] = true;
	if(isCurrPrice)
		baseParams['isCurrPrice'] = true;
	if(isCombination)
		baseParams['isCombination'] = true;
	if(isHot)
		baseParams['isHot'] = true;
	if(isWeight)
		baseParams['isWeight'] = true;
	if(isCommission)
		baseParams['isCommission'] = true;
	if(isTempFood){
		baseParams['isTempFood'] = true;
	}
	
	var gs = menuGrid.getStore();
	gs.baseParams = baseParams;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
//	menuMgrGrid.getSelectionModel().clearSelections();
//	menuMgrGrid.fireEvent('rowclick');
}

/**
 * 修改菜品相关信息,暂时包括:基础信息,关联口味,关联食材,关联套菜
 */
foodOperationHandler = function(c){
	c = c == null || typeof(c) == 'undefined' ? {type:''} : c;
	
	var selData = Ext.ux.getSelData('menuMgrGrid');
	c.data = selData;
	if(c.win == 'foodDetail'){
		if(c.type == mmObj.operation.insert){
			resetbBasicOperation();
		}else if(c.type == mmObj.operation.update){
			updateBasicHandler(c);
		}else if(c.type == mmObj.operation.select){
			if(!selData){
				return;
			}
			(selData);
		}
	}else if(c.win == 'relation'){
		var cfgd = Ext.getCmp('combinationFoodGrid').getStore();
		if(c.type == mmObj.operation.insert){
			
		}else if(c.type == mmObj.operation.update){
			updateCombinationHandler(c);
		}else if(c.type == mmObj.operation.select){
			if(!selData){
				return;
			}
			Ext.getCmp('txtMiniAllFoodNameSearch').setValue('');
			Ext.getCmp('btnSearchForAllFoodMiniGridTbar').handler();
			cfgd.baseParams['foodID'] = selData.id;
			cfgd.baseParams['restaurantID'] = restaurantID;
			cfgd.load();
		}
	}	
};

/**
 * 
 * @param active
 * @param type
 */
function foodOperation(type){
	var foWin = Ext.getCmp('foodOperationWin');
	var selRowData = Ext.ux.getSelData('menuMgrGrid');
	
	if(typeof(type) == 'string' && type == mmObj.operation.insert){
	
	}else if(typeof(type) == 'string' && type == mmObj.operation.update){	
		if(!selRowData){
			Ext.example.msg('提示','请选中一道菜品再操作!');
			return;
		}
	}else{
		return;
	}
	
	// 标志是否新添加菜品
	foWin.operation = type;
	initKitchenComboData();
	foWin.show();
	foWin.center();
	
	var btnAddForOW = Ext.getCmp('btnAddForOW');
	var btnAppForOW = Ext.getCmp('btnAppForOW');
	var btnSaveForOW = Ext.getCmp('btnSaveForOW');
	var btnCloseForOW = Ext.getCmp('btnCloseForOW');
	var btnRefreshForOW = Ext.getCmp('btnRefreshForOW');
	var btnPreviousFood = Ext.getCmp('btnPreviousFood');
	var btnNextFood = Ext.getCmp('btnNextFood');
	//恢复价格可用
	Ext.getCmp('numBasicForPrice').enable();
	
	Ext.getCmp('food_pricePlans').removeAll();
	
	for (var i = 0; i < food_pricePlans.length; i++) {
		var checkBoxId = 'chbForFoodAlias' + food_pricePlans[i].id,  numberfieldId = 'numBasicForPrice' + food_pricePlans[i].id;
		
		Ext.getCmp('food_pricePlans').add({
	 		columnWidth : .12,	
	 	    items : [{
	 	    	xtype : 'checkbox',
	 	    	id : checkBoxId,
	 	    	relativePrice : numberfieldId,
	 	    	hideLabel : true,
	 	    	listeners : {
	 	    		render : function(thiz){
	 	    			thiz.getEl().dom.parentNode.style.paddingTop = '5px';
	 	    		},
	 	    		check : function(checkbox, checked){
	 	    			var numForAlias = Ext.getCmp(checkbox.relativePrice);
						if(checked){
							numForAlias.enable();
							numForAlias.input_disabled = false;
							numForAlias.focus(true, 100);
						}else{
							numForAlias.disable();
							numForAlias.input_disabled = true;
							numForAlias.clearInvalid();
						}
					},
					//解决第一次点击无效
					focus : function(thiz){
						var numForAlias = Ext.getCmp(thiz.relativePrice);
						if(document.getElementById(thiz.id).checked){
							numForAlias.disable();
						}else{
							numForAlias.enable();
							numForAlias.focus(true, 100);
						}
					}
	 	    	}
	 	    }]			 		
	 	});
		
		Ext.getCmp('food_pricePlans').add({
	 		columnWidth : .88,
	 		items : [{
	 			xtype : 'numberfield',
	 	    	id : numberfieldId,
	 	    	fieldLabel : food_pricePlans[i].name,
	 	    	style : 'text-align:right;',
	 	    	decimalPrecision : 2,
	 	    	allowBlank : false,
	 	    	maxValue : 99999.99,
	 	    	minValue : 0.00,
	 	    	width : 80,
	 	    	disabled : true,
	 	    	input_disabled : true,//防止tab键也可以focus已经disable的输入框
	 	    	validator : function(v){
	 	    		if(v >= 0.00 && v <= 99999.99){
	 	    	    	return true;
	 	    	    }else{
	 	    	    	return '价格需在 0.00  至 99999.99 之间!';
	 	    	    }
	 	    	},
	 	    	listeners : {
	 	    		focus : function(thiz){
	 	    			if(thiz.input_disabled){
	 	    				thiz.setDisabled(true);
	 	    				thiz.input_disabled = true;
	 	    			}
	 	    		}
	 	    	}
	 		}]		
		});
		
		Ext.getCmp('food_pricePlans').add({
			 columnWidth : 1	
		})		
		
	}	
	
	if(typeof(type) == 'string' && type == mmObj.operation.insert){
		foWin.setTitle('添加菜品');
		foodOperationWin.otype = mmObj.operation.insert;
		btnAddForOW.setVisible(true);
		btnAppForOW.setVisible(false);
		btnSaveForOW.setVisible(false);
		btnPreviousFood.setVisible(false);
		btnNextFood.setVisible(false);		
		
		btnAddForOW.setDisabled(false);
		btnAppForOW.setDisabled(true);
		btnSaveForOW.setDisabled(true);
		
		resetbBasicOperation();
		
	}else if(typeof(type) == 'string' && type == mmObj.operation.update){
		Ext.Ajax.request({
			url : "../../QueryMenu.do",
			params : {
				dataSource : 'getFoodPrices',
				foodId : selRowData.id
			},
			success : function(response){
				var jr = Ext.util.JSON.decode(response.responseText);
				for (var i = 0; i < jr.root.length; i++) {
					if(jr.root[i].price >= 0){
						document.getElementById('chbForFoodAlias' + jr.root[i].id).checked = true;
						Ext.getCmp('numBasicForPrice' + jr.root[i].id).setValue(jr.root[i].price);	
						Ext.getCmp('numBasicForPrice' + jr.root[i].id).enable();
						Ext.getCmp('numBasicForPrice' + jr.root[i].id).input_disabled = false;
					}
				}
			},
			failure : function(){
			
			}
		});		
		
		Ext.Ajax.request({
			url : "../../QueryMenu.do",
			params : {
				dataSource : 'getMultiPrices',
				foodId : selRowData.id
			},
			success : function(response){
				var jr = Ext.util.JSON.decode(response.responseText);
				if(jr.success && jr.root.length > 0){
					multiFoodPriceCount = jr.root.length;
					for (var i = 1; i <= jr.root.length; i++) {
						var unitNameId = 'multiPriceUnit' + i,  unitPriceId = 'multiPriceValue' + i;
						
						Ext.getCmp('food_multiPrice').add({
							cls : 'multiClass'+i,
					 		columnWidth : 1	 		
					 	});								
						
						Ext.getCmp('food_multiPrice').add({
							cls : 'multiClass'+i,
					 		columnWidth : .15,
					 		items : [{
					 			xtype : 'label',
					 	    	html : '&nbsp;'
					 		}]		 		
					 	});	
	
						Ext.getCmp('food_multiPrice').add({
							cls : 'multiClass'+i,
					 		columnWidth : .3,
					 		items : [{
					 			xtype : 'numberfield',
					 			id : unitPriceId,
					 	    	maxValue : 65535,
					 	    	value : jr.root[i-1].price,
					 	    	cls : 'multiPriceValue',
					 	    	style : 'text-align:right',
					 	    	width : 85,
					 	    	hideLabel : true
					 		}]		 		
					 	});	
						
						Ext.getCmp('food_multiPrice').add({
							cls : 'multiClass'+i,
					 		columnWidth : .03,
					 		items : [{
					 			xtype : 'label',
					 			style : 'font-size:16px;',
					 	    	text : '/'
					 		}]		 		
					 	});	
						
						Ext.getCmp('food_multiPrice').add({
							cls : 'multiClass'+i,
					 		columnWidth : .3,
					 		items : [{
					 			xtype : 'textfield',
					 			id : unitNameId,
					 	    	value : jr.root[i-1].unit,
					 	    	width : 85,
					 	    	cls : 'multiPriceName',
					 	    	hideLabel : true
					 		}]	 		 		
					 	});		
						
						Ext.getCmp('food_multiPrice').add({
							cls : 'multiClass'+i,
					 		columnWidth : .2,
					 		items : [{
						    	xtype : 'button',
						    	text : '删除',
						    	multiIndex : i,
						    	iconCls : 'btn_delete',
						    	handler : function(e){
						    		deleteMultiPriceHandler(e);
						    	}
					 		}] 		 		
					 	});								
						
					}
					
					Ext.getCmp('food_multiPrice').doLayout();
					
					$('.multiPriceName').attr('placeholder', '单位名称');
					$('.multiPriceValue').attr('placeholder', '价格');
				
					isHasMultiPrice();
				}
			},
			failure : function(){
			
			}
		});	
		
		
		resetbBasicOperation(selRowData);
		foWin.setTitle(selRowData.name);
		foodOperationWin.otype = mmObj.operation.update;
		btnAddForOW.setVisible(false);
		btnAppForOW.setVisible(true);
		btnSaveForOW.setVisible(true);
		btnPreviousFood.setVisible(true);
		btnNextFood.setVisible(true);
		
		btnAddForOW.setDisabled(true);
		btnAppForOW.setDisabled(false);
		btnSaveForOW.setDisabled(false);
		btnPreviousFood.setDisabled(false);
		btnNextFood.setDisabled(false);
		
		if(!Ext.getCmp('menuMgrGrid').getSelectionModel().hasPrevious()){
			btnPreviousFood.setDisabled(true);
			btnNextFood.setDisabled(false);
		}else if(!Ext.getCmp('menuMgrGrid').getSelectionModel().hasNext()){
			btnPreviousFood.setDisabled(false);
			btnNextFood.setDisabled(true);
		}
	}
	
	btnCloseForOW.setVisible(true);
	btnCloseForOW.setDisabled(false);
	
	btnRefreshForOW.setVisible(true);
	btnRefreshForOW.setDisabled(false);
	
};
/**
 * 
 * @param p
 */
function refreshInfoGrid(p){
	if(p.getId() == 'materialGridTab'){
		if(!Ext.ux.getSelData('menuMgrGrid')){
			Ext.getCmp('materialGrid').getStore().removeAll();
			return;
		}
		Ext.getCmp('materialGrid').getStore().load();
	}else if(p.getId() == 'tasteGridTab'){
		if(!Ext.ux.getSelData('menuMgrGrid')){
			Ext.getCmp('tasteGrid').getStore().removeAll();
			return;
		}
		Ext.getCmp('tasteGrid').getStore().load();
	}else if(p.getId() == 'combinationGridTab'){
		if(!Ext.ux.getSelData('menuMgrGrid')){
			Ext.getCmp('combinationGrid').getStore().removeAll();
			return;
		}
		Ext.getCmp('combinationGrid').getStore().load();
	}
};
//-----------------


//------------------comb
function combinationOperationRenderer(){
	return '<a href="javascript:combinationDeleteHandler()">删除</a>';
};

function combinationDeleteHandler(){
	var cmg = Ext.getCmp('combinationFoodGrid');
	cmg.getStore().remove(cmg.getSelectionModel().getSelections()[0]);
	cmg.getView().refresh();
};

function combinationDisplaySumHandler(){
	var cfg = Ext.getCmp('combinationFoodGrid').getStore();
	var sumPrice = 0, sumAmount = 0, itemSumPrice = 0;
	for(var i = 0; i < cfg.getCount(); i++){
		itemSumPrice = cfg.getAt(i).get('unitPrice') * cfg.getAt(i).get('amount');
		cfg.getAt(i).set('sumPrice', itemSumPrice);
		sumPrice += itemSumPrice;
		sumAmount += cfg.getAt(i).get('amount');
	}
	Ext.getDom('txtDisplayCombinationFoodPrice').innerHTML = parseFloat(sumPrice).toFixed(2);
	Ext.getDom('txtDisplayCombinationFoodPriceAmount').innerHTML = parseFloat(sumAmount).toFixed(2);
};

var combinationFoodGrid = new Ext.grid.EditorGridPanel({
	title : '<center>已关联菜品<font color="red">(关联菜品即可设为套菜,否则留空)</font></center>',
	id : 'combinationFoodGrid',
	columnWidth : .55,
	height : tabItemsHeight,
	loadMask : { msg: '数据请求中，请稍后...' },
	frame : true,
	trackMouseOver : true,
	viewConfig : {
		forceFit : true
	},
	sm : new Ext.grid.RowSelectionModel({singleSelect:true}),
	cm : new Ext.grid.ColumnModel([
	    new Ext.grid.RowNumberer(),
	    {header:'菜名', dataIndex:'name', width:200},
	    {header:'价格',  dataIndex:'unitPrice', align:'right', width:80, renderer:Ext.ux.txtFormat.gridDou},
	    {
	    	header : '份数',
	    	dataIndex : 'amount',
	    	width : 80,
	    	align : 'right',
	    	renderer : Ext.ux.txtFormat.gridDou,
	    	editor : new Ext.form.NumberField({
	    		maxLength : 8,
	    		maxLengthText : '长度不能超过8位',
	    		minValue : 1,
	    		maxValue : 65535,
	    		allowBlank : false,
	    		style : 'color:green; font-weight:bold;',
	    		validator : function(v){
	    	    		if(/^\d+$/.test(v)){
	    	    			return true;
	    	    		}else{
	    	    			return '输入有误,份数只能是正整数!';
	    	    		}
	    	    	}
	    	})
	    },
	    {header:'成本',  dataIndex:'sumPrice', align:'right', width:90, renderer:Ext.ux.txtFormat.gridDou},
	    {header:'操作', align:'center', renderer:combinationOperationRenderer}
	]),
	ds : new Ext.data.JsonStore({
		url : '../../QueryFoodCombination.do',
		root : 'root',
		fields : ComboFoodRecord.getKeys(),
		listeners : {
			beforeload : function(){
/*				var selData = Ext.ux.getSelData('menuMgrGrid');
				this.baseParams['foodID'] = selData.id;
				this.baseParams['restaurantID'] = restaurantID;*/
			},
			load : function(){
				combinationDisplaySumHandler();
			},
			add : function(){
				//combinationDisplaySumHandler();
			},
			remove : function(){
				combinationDisplaySumHandler();
			},
			update : function(){
				combinationDisplaySumHandler();
			}
		}
	}),
	bbar : new Ext.Toolbar({
		height : 26,
		items : [{
			xtype:'tbtext', 
			text:String.format(Ext.ux.txtFormat.barTitle, '总计')
		}, '->', {
			xtype:'tbtext', 
			text:String.format(Ext.ux.txtFormat.barMsg, '总成本', 'txtDisplayCombinationFoodPrice', '0.00')
		}, {
			xtype:'tbtext', 
			text:String.format(Ext.ux.txtFormat.barMsg, '总份数', 'txtDisplayCombinationFoodPriceAmount', '0.00')
		}]
	})
});

var allFoodMiniGridTbar = new Ext.Toolbar({
	height : 26,
	items : [{ xtype:'tbtext', text:'菜名搜索:'}, {
		xtype : 'textfield',
		id : 'txtMiniAllFoodNameSearch',
		width : 100
	}, '->', {
		text : '搜索',
		id : 'btnSearchForAllFoodMiniGridTbar',
		iconCls : 'btn_search',
		handler : function(){
			var mafn = Ext.getCmp('txtMiniAllFoodNameSearch').getValue().trim();
			var afmgs = allFoodMiniGrid.getStore();
			afmgs.baseParams['name'] = mafn;
			afmgs.load({
				params : {
					limit : GRID_PADDING_LIMIT_20,
					start : 0
				}
			});
		}
	}]
});

var allFoodMiniGrid = createGridPanel(
    'allFoodMiniGrid',
    '<center>所有菜品</center>',
    415,
    '',
    '../../QueryMenuMgr.do',
    [
	    [true, false, false, true], 
	    ['编号', 'alias', 70] , 
	    ['菜名', 'name', 200] , 
	    ['价格', 'unitPrice', '', 'right', Ext.ux.txtFormat.gridDou]
	],
	FoodBasicRecord.getKeys(),
    [ ['restaurantId', restaurantID], ['isPaging', true]],
    GRID_PADDING_LIMIT_20,
    '',
    allFoodMiniGridTbar
);
allFoodMiniGrid.columnWidth = .44;
allFoodMiniGrid.getBottomToolbar().displayMsg = '共&nbsp;{2}&nbsp;条记录';
/*allFoodMiniGrid.on('resize', function(thiz){
	thiz.setHeight(tabItemsHeight);
});*/
allFoodMiniGrid.on('rowdblclick', function(thiz){
	var cfd = Ext.getCmp('combinationFoodGrid');
	var sr = thiz.getSelectionModel().getSelected();
	var selData = Ext.ux.getSelData(menuGrid);
	var cv = true;
	if(sr.get('id') == selData.id){
		Ext.example.msg('提示','添加失败,套菜不能包含原菜!');
		return;
	}
	if(Ext.ux.cfs.isCombo(sr.get('status'))){
		Ext.example.msg('提示','添加失败,套菜不能关联套菜!');
		return;
	}
	cfd.getStore().each(function(r){
		if(r.get('id') == sr.get('id')){
			cv = false;
			r.set('amount', parseFloat(r.get('amount') + 1));
			return;
		}
	});
	if(cv){
		sr.set('amount', 0);
		cfd.getStore().add(sr);
		sr.set('amount', parseFloat(sr.get('amount') + 1));
		cfd.getView().refresh();
	}
});
allFoodMiniGrid.keys = [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearchForAllFoodMiniGridTbar').handler();
			}
		}];
var combinationOperationPanel = new Ext.Panel({
	id : 'combinationOperationPanel',
	frame : true,
	border : false,
	layout : 'column',
	items : [
	    combinationFoodGrid,
	    {xtype:'panel', columnWidth:.01, html:'&nbsp;'},
	    allFoodMiniGrid
	]
});

/**
 * 修改菜品关联套菜
 */
updateCombinationHandler = function(c){
//	Ext.example.msg('提示','修改菜品关联套菜!');
	var foodID = c.data.id;
	var status = c.data.status;
	var comboContent = '';
	
	var cfg = Ext.getCmp('combinationFoodGrid').getStore();
	for(var i = 0; i < cfg.getCount(); i++){
		comboContent += (i > 0 ? '<split>' : '');
		comboContent += (cfg.getAt(i).get('id') + ',' + cfg.getAt(i).get('amount'));
	}
	
	setButtonStateOne(true);
	
	Ext.Ajax.request({
		url : '../../UpdateFoodCombination.do',
		params : {
			foodID : foodID,
			restaurantID : restaurantID,
			status : status,
			comboContent : comboContent
		},
		success : function(response, options){
			var jr = Ext.util.JSON.decode(response.responseText);
			if(eval(jr.success)){
				Ext.example.msg(jr.title, jr.msg);
				if(c.hide == true){
					Ext.getCmp('foodRelationOperationWin').hide();
				}else{
					cfg.load();					
				}
				Ext.getCmp('menuMgrGrid').getStore().reload();
			}else{
				Ext.ux.showMsg(jr);
			}
			setButtonStateOne(false);
		},
		failure : function(response, options) {
			var jr = Ext.util.JSON.decode(response.responseText);
			Ext.ux.showMsg(jr);
			setButtonStateOne(false);
		}
	});
	
};
//----------------


//----------------taste----
tasteCalcRenderer = function(val, metadata, record){
	if(val == 0){
		return '按价格';
	}else if(val == 1){
		return '按比例';
	}
};

tasteOperationRenderer = function(value, cellmeta, record, rowIndex, columnIndex, store){
	return '<a href="javascript:tasteDeleteHandler()">删除</a>';
};

tasteDeleteHandler = function(){
	commonTasteGrid.getStore().remove(commonTasteGrid.getSelectionModel().getSelections()[0]);
	commonTasteGrid.getView().refresh();
};

var commonTasteGridTbar = new Ext.Toolbar({
	height : 26,
	items : [{
		xtype:'tbtext', 
		text:'&nbsp;关联方式:&nbsp;'
	}, {
		xtype : 'radio',
    	id : 'rdoTasteTypeSmart',
    	name : 'rdoTasteType',
    	boxLabel : '智能',
    	width : 60,
    	inputValue : 1,
    	listeners : {
    		render : function(e){
    			Ext.getDom(e.getId()).onclick = function(){
    				if(e.getValue()){
    					Ext.getCmp('allTasteGrid').setDisabled(true);
    					commonTasteGrid.getColumnModel().setColumnWidth(6, 80);
    					commonTasteGrid.getColumnModel().setHidden(6, true);
	    				mmObj.rdoTasteType = e.getRawValue();
	    			}
    			};
    		}
    	}
    }, {
    	xtype : 'radio',
    	id : 'rdoTasteTypeManual',
    	name : 'rdoTasteType',
    	boxLabel : '人工',
    	width : 60,
    	inputValue : 2,
    	listeners : {
    		render : function(e){
    			Ext.getDom(e.getId()).onclick = function(){
    				if(e.getValue()){
    					Ext.getCmp('allTasteGrid').setDisabled(false);
    					Ext.getCmp('allTasteGrid').getSelectionModel().selectFirstRow();
    					commonTasteGrid.getColumnModel().setHidden(6, false);
    					var sv = Ext.getDom('txtTasteNameSearch');
    					if(sv.value != ''){
    						sv.value = '';
    						sv.onkeyup();
    					}
	    				mmObj.rdoTasteType = e.getRawValue();
	    			}
    			};
    		}
    	}
    }]
});



var allTasteGridTbar = new Ext.Toolbar({
	height : 26,
	items : [{
		xtype:'tbtext', text:'&nbsp;口味名搜索:&nbsp;'
	}, {
		xtype : 'textfield',
    	id : 'txtTasteNameSearch',
    	width : 100,
    	listeners : {
    		render : function(e){
    			Ext.getDom('txtTasteNameSearch').onkeyup = function(){
    				var txtTasteName = Ext.getCmp('txtTasteNameSearch').getValue().trim();
    				var store = allTasteGrid.getStore();
    				var selModel = allTasteGrid.getSelectionModel();
    				var searchData = {root:[]}, orderByData = [], otherData = [], selIndex = [];
    				if(selModel.getSelections().length > 0){
    					selModel.clearSelections();
    				}
    				if(txtTasteName.length == 0){
    					for(var i = 0; i < store.getCount(); i++){
	    					var selRow = allTasteGrid.getView().getRow(i);
	    					selRow.style.backgroundColor = '#FFFFFF';
	    				}
    					return;
    				}
    				for(var i = 0; i < store.getCount(); i++){
    					if(store.getAt(i).data.name.indexOf(txtTasteName) >= 0 ){
    						orderByData.push(store.getAt(i).data);	    						
    					}else{
    						otherData.push(store.getAt(i).data);
    					}
    				}
    				for(var i = 0; i < orderByData.length; i++){
    					searchData.root.push(orderByData[i]);
    					selIndex.push(i);
    				}
    				for(var i = 0; i < otherData.length; i++){
    					searchData.root.push(otherData[i]);
    				}
    				store.loadData(searchData);
    				for(var i = 0; i < searchData.root.length; i++){
    					var selRow = allTasteGrid.getView().getRow(i);
    					if(i < orderByData.length){
    						selRow.style.backgroundColor = '#FFFF00';
    					}else{
    						selRow.style.backgroundColor = '#FFFFFF';
    					}
    				}
    			};
    		}
    	}
	}]
});

//----------------basic
var basicOperationPanel = new Ext.Panel({
	id : 'basicOperationPanel',
	frame : true,
	border : false,
	layout : 'fit',
	items : [{
		layout : 'column',
		items : [{
			columnWidth : .35,
		 	layout : 'column',
		 	defaults : {
		 		xtype : 'panel',
		 		layout : 'form',
		 		labelWidth : 40
		 	},
		 	items : [{
		 		columnWidth : 1,
		 		items : [{
		 			xtype : 'textfield',
		 			id : 'txtBasicForFoodName',
		 			fieldLabel : '菜名',
		 			allowBlank : false,
		 			width : 240
		 		}]
		 	}, {
		 		columnWidth : 1
		 	}, {
		 		columnWidth : .5,
		 	    items : [{
		 	    	xtype : 'numberfield',
		 	    	id : 'numBasicForPrice',
		 	    	style : 'text-align:right;',
		 	    	fieldLabel : '价格',
		 	    	decimalPrecision : 2,
		 	    	allowBlank : false,
		 	    	maxValue : 99999.99,
		 	    	minValue : 0.00,
		 	    	width : 85,
		 	    	validator : function(v){
		 	    		if(v >= 0.00 && v <= 99999.99){
		 	    	    	return true;
		 	    	    }else{
		 	    	    	return '价格需在 0.00  至 99999.99 之间!';
		 	    	    }
		 	    	}
		 	    }]
		 	}, {
				columnWidth : .5,	
				id : 'food_pricePlans',
				layout : 'column',
			 	defaults : {
			 		xtype : 'panel',
			 		layout : 'form',
			 		labelWidth : 40
			 	},
			 	items : [{
			 		columnWidth : .12,	
			 	    items : [{
			 	    	xtype : 'checkbox',
	//		 	    	id : 'chbForFoodAlias',
			 	    	hideLabel : true,
			 	    	width : 16,
			 	    	listeners : {
			 	    		render : function(thiz){
			 	    			thiz.getEl().dom.parentNode.style.paddingTop = '5px';
			 	    		},			 	    		
			 	    		check : function(checkbox, checked){
			 	    			var numForAlias = Ext.getCmp('numBasicForFoodAliasID');
								if(checked){
									numForAlias.enable();
									numForAlias.focus(true, 100);
								}else{
									numForAlias.disable();
								}
							},
							focus : function(){
								var numForAlias = Ext.getCmp('numBasicForFoodAliasID');
								if(document.getElementById('chbForFoodAlias').checked){
									
									numForAlias.disable();
								}else{
									numForAlias.enable();
									numForAlias.focus(true, 100);
								}
							}
			 	    	}
			 	    }]			 		
			 	}, {
			 		columnWidth : .88,
			 		items : [{
			 			xtype : 'numberfield',
	//		 	    	id : 'numBasicForFoodAliasID',
			 	    	fieldLabel : '会员价',
			 	    	maxValue : 65535,
			 	    	minValue : 1,
			 	    	width : 80,
			 	    	disabled : true,
			 	    	validator : function(v){
			 	    		if(v > 0 && v <= 65535 && v.indexOf('.') == -1){
			 	    	    	return true;
			 	    	    }else{
			 	    	    	return '编号需在 1  至 65535 之间,且为整数!';
			 	    	    }
			 	    	}
			 		}]
			 	}, {
			 		columnWidth : 1
			 	},{
			 		columnWidth : .12,	
			 	    items : [{
			 	    	xtype : 'checkbox',
	//		 	    	id : 'chbForFoodAlias',
			 	    	hideLabel : true,
			 	    	width : 16,
			 	    	listeners : {
			 	    		render : function(thiz){
			 	    			thiz.getEl().dom.parentNode.style.paddingTop = '5px';
			 	    		},			 	    		
			 	    		check : function(checkbox, checked){
			 	    			var numForAlias = Ext.getCmp('numBasicForFoodAliasID');
								if(checked){
									numForAlias.enable();
									numForAlias.focus(true, 100);
								}else{
									numForAlias.disable();
								}
							},
							focus : function(){
								var numForAlias = Ext.getCmp('numBasicForFoodAliasID');
								if(document.getElementById('chbForFoodAlias').checked){
									
									numForAlias.disable();
								}else{
									numForAlias.enable();
									numForAlias.focus(true, 100);
								}
							}
			 	    	}
			 	    }]			 		
			 	}, {
			 		columnWidth : .88,
			 		items : [{
			 			xtype : 'numberfield',
	//		 	    	id : 'numBasicForFoodAliasID',
			 	    	fieldLabel : '会员价',
			 	    	maxValue : 65535,
			 	    	minValue : 1,
			 	    	width : 80,
			 	    	disabled : true,
			 	    	validator : function(v){
			 	    		if(v > 0 && v <= 65535 && v.indexOf('.') == -1){
			 	    	    	return true;
			 	    	    }else{
			 	    	    	return '编号需在 1  至 65535 之间,且为整数!';
			 	    	    }
			 	    	}
			 		}]
			 	}]
		 	},{
		 		columnWidth : .15,
		 		items : [{
		 			xtype : 'label',
		 	    	text : '多单位:'
		 		}]
		 	},{
		 		columnWidth : .3,
		 		items : [{
			    	xtype : 'button',
			    	text : '添加单位',
			    	width : 85,
			    	iconCls : 'btn_add',
			    	handler : function(){
			    		optMultiPriceHandler();
			    	}
		 		}]
		 	},{
		 		columnWidth : 1,
		 		style : 'margin-top:3px;'
		 	}, {
				columnWidth : 1,	
				id : 'food_multiPrice',
				layout : 'column',
			 	defaults : {
			 		xtype : 'panel',
			 		layout : 'form',
			 		labelWidth : 40
			 	},
			 	items : []
		 	},{
		 		columnWidth : 1,
		 		html : '&nbsp;'
		 	}, {
		 		columnWidth : .5,
		 		items : [{
		 			xtype : 'hidden',
		 			id : 'txtBasicForPinyin'
		 		}]
		 	}, {
		 		columnWidth : .5,
		 	    items : [{
		 	    	xtype : 'combo',
		 	       	id : 'cmbBasicForKitchenAlias',
		 	    	fieldLabel : '厨房',
		 	    	width : 86,
		 	    	listWidth : 99,
		 	    	store : new Ext.data.SimpleStore({
						fields : ['id', 'name']
					}),
					valueField : 'id',
					displayField : 'name',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					readOnly : false
		 	    }]
		 	},{
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'selectPrintRestaurant_checkBox_menuMgrMain',
		 	    	hideLabel : true,
		 	    	width : 25,
		 	    	listeners : {
		 	    		check : function(checkbox, checked){
 	    					var selectRestaurant = Ext.getCmp('restaurant_checkbox_menuMgrMain');
		 	    			
		 	    			if(checked){
		 	    				selectRestaurant.enable();
							}else{
								selectRestaurant.disable();
							}
						}
		 	    	}
		 	    }]
		 	},{
		 		columnWidth : .5,
		 		items : [{
		 			xtype : 'combo',
		 	    	id : 'restaurant_checkbox_menuMgrMain',
		 	    	fieldLabel : '打印厨房',
		 	    	width : 80,
		 	    	listWidth : 99,
		 	    	labelStyle : 'width:55px;',
		 	    	store : new Ext.data.SimpleStore({
						fields : ['id', 'name']
					}),
					valueField : 'id',
					displayField : 'name',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					readOnly : false,
					listeners : {
						render : function(thiz){
							var kitchenData = [];
							Ext.Ajax.request({
								url : '../../QueryKitchen.do',
								params : {
									dataSource : 'normal'
								},
								success : function(res, opt){
									var jr = Ext.decode(res.responseText);
									for(var i = 0; i < jr.root.length; i++){
										kitchenData.push([jr.root[i]['id'], jr.root[i]['name']]);
									}
									thiz.store.loadData(kitchenData);
									thiz.setValue(jr.root[0].id);
									Ext.getCmp('restaurant_checkbox_menuMgrMain').disable();
								},
								fialure : function(res, opt){
									thiz.store.loadData(kitchenData);
								}
							});
						}
					}
		 		}]
		 	},{
		 		columnWidth : .5,
		 	    items : [{
		 	    	xtype : 'combo',
		 	    	hidden : true,
		 	    	width : 86,
		 	    	listWidth : 99,
		 	    	store : new Ext.data.SimpleStore({
						fields : ['id', 'name']
					}),
					valueField : 'id',
					displayField : 'name',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					readOnly : false
		 	    }]
		 	},
			{
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForFoodAlias',
		 	    	hideLabel : true,
		 	    	width : 10,
		 	    	listeners : {
		 	    		check : function(checkbox, checked){
		 	    			var numForAlias = Ext.getCmp('numBasicForFoodAliasID');
							if(checked){
								numForAlias.enable();
								numForAlias.focus(true, 100);
							}else{
								numForAlias.disable();
							}
						},
						focus : function(){
							var numForAlias = Ext.getCmp('numBasicForFoodAliasID');
							if(document.getElementById('chbForFoodAlias').checked){
								
								numForAlias.disable();
							}else{
								numForAlias.enable();
								numForAlias.focus(true, 100);
							}
						}
		 	    	}
		 	    }]
		 	}, {
		 		columnWidth : .5,
		 		items : [{
		 			xtype : 'numberfield',
		 	    	id : 'numBasicForFoodAliasID',
		 	    	fieldLabel : '助记码',
		 	    	labelStyle : 'margin-left:12px;',
		 	    	maxValue : 65535,
		 	    	minValue : 1,
		 	    	width : 80,
		 	    	disabled : true,
		 	    	validator : function(v){
		 	    		if(v > 0 && v <= 65535 && v.indexOf('.') == -1){
		 	    	    	return true;
		 	    	    }else{
		 	    	    	return '编号需在 1  至 65535 之间,且为整数!';
		 	    	    }
		 	    	}
		 		}]
		 	},{
		 		columnWidth : 1,
		 	    items : [{
		 	    	xtype : 'textarea',
		 	    	id : 'txtBasicForDesc',
		 	    	fieldLabel : '简介',
		 	    	width : 253,
		 	    	height : 100,
		 	    	maxLength : 500,
		 	    	maxLengthText : '简介最多输入500字,可以为空.'
		 	    }]
		 	}, {
		 		columnWidth : .13,
		 	    items : [{html:'状态:'}]
		 	}, {
		 		columnWidth : .17,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicSpecial',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="特价" src="../../images/icon_tip_te.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .17,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicRecommend',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="推荐" src="../../images/icon_tip_jian.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .17,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicFree',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="赠送" src="../../images/forFree.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .17,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicStop',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="停售" src="../../images/icon_tip_ting.png"></img>'
		 	   	}]
		 	}, {
		 		columnWidth : .17,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicLimit',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="限量沽清" src="../../images/limitCount.png"></img>',
		 	    	listeners : {
		 	    		check : function(checkbox, checked){
							if(checked){
								Ext.getCmp('numLimitCount').getEl().up('.x-form-item').setDisplayed(true);
								Ext.getCmp('numLimitCount').focus(true, 100);
							}else{
								Ext.getCmp('numLimitCount').getEl().up('.x-form-item').setDisplayed(false);
							}
						}
		 	    	}
		 	    }]
		 	}, {
		 		columnWidth : 1
		 	}, {
		 		columnWidth : .13,
		 	    items : [{html:'&nbsp;&nbsp;&nbsp;&nbsp;'}]
		 	}, {
		 		columnWidth : .17,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicCurrPrice',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="时价" src="../../images/currPrice.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .17,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicHot',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="热销" src="../../images/hot.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .17,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicWeight',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="称重" src="../../images/weight.png"></img>'
		 	    }]
		 	}, {
		 		columnWidth : .17,
		 	    items : [{
		 	    	xtype : 'checkbox',
		 	    	id : 'chbForBasicCommission',
		 	    	hideLabel : true,
		 	    	boxLabel : '<img title="提成" src="../../images/commission.png"></img>',
		 	    	listeners : {
		 	    		check : function(checkbox, checked){
							if(checked){
								Ext.getCmp('numCommission').getEl().up('.x-form-item').setDisplayed(true);
								Ext.getCmp('numCommission').focus(true, 100);
							}else{
								Ext.getCmp('numCommission').getEl().up('.x-form-item').setDisplayed(false);
							}
						}
		 	    	}
		 	    }]
		 	},{
		 		columnWidth : .5,
		 		labelWidth : 35,
		 		items : [{
		 	    	xtype : 'numberfield',
		 	    	id : 'numCommission',
		 	    	style : 'text-align:right;',
		 	    	fieldLabel : '提成',
		 	    	decimalPrecision : 2,
		 	    	allowBlank : false,
		 	    	maxValue : 99999.99,
		 	    	minValue : 0.00,
		 	    	width : 60,
		 	    	validator : function(v){
		 	    		if(v >= 0.00 && v <= 99999.99){
		 	    	    	return true;
		 	    	    }else{
		 	    	    	return '价格需在 0.00  至 99999.99 之间!';
		 	    	    }
		 	    	}
		 	    }]
		 	},{
		 		columnWidth : .5,
		 		labelWidth : 60,
		 		items : [{
		 	    	xtype : 'numberfield',
		 	    	id : 'numLimitCount',
		 	    	style : 'text-align:right;',
		 	    	fieldLabel : '限量沽清',
		 	    	decimalPrecision : 2,
		 	    	allowBlank : false,
		 	    	maxValue : 99999.99,
		 	    	minValue : 0.00,
		 	    	width : 70,
		 	    	validator : function(v){
		 	    		if(v >= 0.00 && v <= 99999.99){
		 	    	    	return true;
		 	    	    }else{
		 	    	    	return '数量需在 0.00  至 99999.99 之间!';
		 	    	    }
		 	    	}
		 	    }]
		 	}]
		}, {
			columnWidth : .65,
			labelWidth : 60,
			items : [ new Ext.BoxComponent({
				xtype : 'box',
		 	    id : 'foodBasicImg',
		 	    name : 'foodBasicImg',
		 	    width : 555,
		 	    height : 420,
		 	    autoEl : {
		 	    	tag : 'img',
		 	    	title : '菜品图预览.',
		 	    	style : 'filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale); width:555; height:420; cursor:hand;'
		 	    }
			}), {
				tag : 'div',
		 	    height : Ext.isIE ? 5 : 10
		 	}, {
		 		xtype : 'panel',
		 	    layout : 'column',
		 	    items : [{
		 	    	columnWidth : 1
		 	    },{
		 	    	xtype : 'form',
		 	    	layout : 'form',
		 	    	labelWidth : 60,
		 	    	columnWidth : .7,
		 	    	url : '../../ImageFileUpload.do',
		 	    	id : 'imgFileUploadForm',
		 	    	fileUpload : true,
		 	    	items : [{}],
		 	    	listeners : {
		 	    		render : function(e){
		 	    			Ext.getDom(e.getId()).setAttribute('enctype', 'multipart/form-data');
			 	  		}
		 	    	}
		 	    }, {
		 	    	xtype : 'panel',
		 	    	columnWidth : .14,
		 	    	items : [{
		 	    		xtype : 'button',
		 	    		id : 'btnUploadFoodImage',
		 	    		hidden : true,
		 	        	text : '上传图片',
		 	        	handler : function(e){
		 	        		var check = true;
		 	        		var img = '';
		 	        		if(Ext.isIE){
		 	        			var file = Ext.getDom('txtImgFile');
		 	        			file.select();
		 	        			img = document.selection.createRange().text;
		 	        		}else{
		 	        			var file = Ext.getDom('txtImgFile'); 
				 	        	img = file.value;
		 	        		}
		 	        		if(typeof(img) != 'undefined' && img.length > 0){
		 	        			var index = img.lastIndexOf('.');
				 	        	var type = img.substring(index+1, img.length);
				 	        	check = false;
				 	        	for(var i = 0; i < imgTypeTmp.length; i++){
				 	        		if(type.toLowerCase() == imgTypeTmp[i].toLowerCase()){
				 	        			check = true;
					 	        	   	break;
					 	        	}
				 	        	}
		 	        		}else{
		 	        			check = false;
		 	        		}
		 	        		 
		 	        		if(check){
		 	        			var selData = Ext.ux.getSelData('menuMgrGrid');
				 	        	selData.arrt = {
				 	        	    type : mmObj.operation.img.upload
				 	        	};
				 	        	uploadFoodImage(selData);
		 	        		}else{
		 	        			Ext.example.msg('提示', '上传图片失败,未选择图片或图片类型正确.');
		 	        		}
		 	        	 }
		 	    	 }]
		 	    }, {
		 	    	xtype : 'panel',
		 	    	columnWidth : .14,
		 	    	items : [{
		 	    		xtype : 'button',
		 	        	id : 'btnDeleteFoodImage',
		 	        	text : '删除图片',
		 	        	handler : function(e){
		 	        		var selData = Ext.ux.getSelData('menuMgrGrid');
		 	        		if(!selData)
		 	        			return;
		 	        	    
		 	        		if(!selData.img){
		 	        			Ext.example.msg('提示', '该菜品没有图片,无需删除.');
		 	        			return;
		 	        		}
		 	        		 
		 	        		Ext.Msg.confirm('提示', '是否确定删除菜品图片?', function(e){
		 	        			if(e == 'yes'){
									Ext.Ajax.request({
										url : '../../OperateImage.do',
										params : {
											dataSource : 'deleteFoodImg',
											foodId : selData.id
										},
										success : function(res, opt){
											var jr = Ext.util.JSON.decode(res.responseText);
											if(jr.success){
												Ext.example.msg(jr.title, jr.msg);
												refreshFoodImageMsg();
											}else{
												Ext.ux.showMsg(jr);
											}
										},
										failure : function(res, opt){
											Ext.ux.showMsg(Ext.decode(res.responseText));
										}
									});
		 	        			 }
		 	        		}, this);
		 	        	}
		 	    	}]
		 		}]
			}]
		}]
	}]
});

/**
 * 添加多单位
 */
function optMultiPriceHandler(){
	++ multiFoodPriceCount; 
	var unitNameId = 'multiPriceUnit' + multiFoodPriceCount,  unitPriceId = 'multiPriceValue' + multiFoodPriceCount;
	
	Ext.getCmp('food_multiPrice').add({
		cls : 'multiClass'+multiFoodPriceCount,
 		columnWidth : 1	 		
 	});								
	
	Ext.getCmp('food_multiPrice').add({
		cls : 'multiClass'+multiFoodPriceCount,
 		columnWidth : .15,
 		items : [{
 			xtype : 'label',
 	    	html : '&nbsp;'
 		}]		 		
 	});	
	Ext.getCmp('food_multiPrice').add({
		cls : 'multiClass'+multiFoodPriceCount,
		columnWidth : .3,
		items : [{
			xtype : 'numberfield',
			id : unitPriceId,
			cls : 'multiPriceValue',
			style : 'text-align:right',
			maxValue : 65535,
			value : '',
			width : 85,
			hideLabel : true
		}]		 		
	});	
	
	Ext.getCmp('food_multiPrice').add({
		cls : 'multiClass'+multiFoodPriceCount,
 		columnWidth : .03,
 		items : [{
 			xtype : 'label',
 			style : 'font-size:16px;',
 	    	text : '/'
 		}]		 		
 	});		
	
	Ext.getCmp('food_multiPrice').add({
		cls : 'multiClass'+multiFoodPriceCount,
 		columnWidth : .3,
 		items : [{
 			xtype : 'textfield',
 			id : unitNameId,
 			cls : 'multiPriceName',
 	    	value : '',
 	    	width : 85,
 	    	hideLabel : true
 		}]	 		 		
 	});			
	
	Ext.getCmp('food_multiPrice').add({
		cls : 'multiClass'+multiFoodPriceCount,
 		columnWidth : .2,
 		items : [{
	    	xtype : 'button',
	    	text : '删除',
	    	multiIndex : multiFoodPriceCount,
	    	iconCls : 'btn_delete',
	    	handler : function(e){
	    		deleteMultiPriceHandler(e);
	    	}
 		}] 		 		
 	});		
	
	Ext.getCmp('food_multiPrice').doLayout();
	$('.multiPriceName').attr('placeholder', '单位名称');
	$('.multiPriceValue').attr('placeholder', '价格');
	
	isHasMultiPrice();
	Ext.getCmp(unitPriceId).focus();
	
}

/**
 * 删除单个多单位
 * @param e
 */
function deleteMultiPriceHandler(e){
	var cmps = $('.multiClass'+Ext.getCmp(e.id).multiIndex);
	
	for (var i = 0; i < cmps.length; i++) {
		Ext.getCmp('food_multiPrice').remove(cmps[i].getAttribute("id"));
	}
	
	Ext.getCmp('food_multiPrice').doLayout();
	
	isHasMultiPrice();
}

function isHasMultiPrice(){
	if($("div[class*='multiClass']").length == 0){
		Ext.getCmp('numBasicForPrice').enable();
	}else{
		Ext.getCmp('numBasicForPrice').disable();
	}	
}


/**
 * 界面赋值
 */
function resetbBasicOperation(_d){
	var foodName = Ext.getCmp('txtBasicForFoodName');
	var foodAliasID = Ext.getCmp('numBasicForFoodAliasID');
	var foodPinyin = Ext.getCmp('txtBasicForPinyin');
	var foodPrice = Ext.getCmp('numBasicForPrice');
	var foodKitchenAlias = Ext.getCmp('cmbBasicForKitchenAlias');
	var foodDesc = Ext.getCmp('txtBasicForDesc');
	var isSpecial = Ext.getCmp('chbForBasicSpecial');
	var isRecommend = Ext.getCmp('chbForBasicRecommend');
	var isFree = Ext.getCmp('chbForBasicFree');
	var isStop = Ext.getCmp('chbForBasicStop');
	var isCurrPrice = Ext.getCmp('chbForBasicCurrPrice');
	var isHot = Ext.getCmp('chbForBasicHot');
	var isWeight = Ext.getCmp('chbForBasicWeight');
	var isCommission = Ext.getCmp('chbForBasicCommission');
	var isLimit = Ext.getCmp('chbForBasicLimit');
	var img = Ext.getDom('foodBasicImg');
	var btnUploadFoodImage = Ext.getCmp('btnUploadFoodImage');
	var btnDeleteFoodImage = Ext.getCmp('btnDeleteFoodImage');
	var commission = Ext.getCmp('numCommission');
	var limitCount = Ext.getCmp('numLimitCount');
	
	var chkAlias = Ext.getCmp('chbForFoodAlias');
	var data = {};
	commission.getEl().up('.x-form-item').setDisplayed(false);
	limitCount.getEl().up('.x-form-item').setDisplayed(false);
	// 清空图片信息
	refreshFoodImageMsg();
	
	var imgFile = Ext.getCmp('txtImgFile');
	
	if(_d != null && typeof(_d) != 'undefined'){
		data = _d;
		btnUploadFoodImage.setDisabled(false);
		btnDeleteFoodImage.setDisabled(false);
		imgFile.setDisabled(false);

		foodKitchenAlias.setValue(data['kitchen.id']);
	}else{
		data = {};
		btnUploadFoodImage.setDisabled(true);
		btnDeleteFoodImage.setDisabled(true);
		imgFile.setDisabled(true);
		var node = kitchenTreeForSreach.getSelectionModel().getSelectedNode();
		if(node && typeof node.attributes.kid != 'undefined') {
			foodKitchenAlias.setValue(node.attributes.kid);
		}
	}
	
	if(data.printKitchenId){
		Ext.getCmp('selectPrintRestaurant_checkBox_menuMgrMain').setValue(true);
		Ext.getCmp('restaurant_checkbox_menuMgrMain').setValue(data.printKitchenId);
	}else{
		Ext.getCmp('selectPrintRestaurant_checkBox_menuMgrMain').setValue(false);
	}
	
	
	var status = typeof(data.status) == 'undefined' ? 0 : parseInt(data.status);
	
	foodName.setValue(data.name);
	if(data.alias > 0){
		document.getElementById('chbForFoodAlias').checked = true;
		chkAlias.fireEvent('check', chkAlias, true);
		foodAliasID.setValue(data.alias);
	}else{
		foodAliasID.setValue();
	}
	foodPinyin.setValue(data.pinyin);
	foodPrice.setValue(data.unitPrice);
	foodDesc.setValue(data.desc);
	isSpecial.setValue(Ext.ux.cfs.isSpecial(status));
	isRecommend.setValue(Ext.ux.cfs.isRecommend(status));
	isStop.setValue(Ext.ux.cfs.isStop(status));
	isFree.setValue(Ext.ux.cfs.isGift(status));
	isCurrPrice.setValue(Ext.ux.cfs.isCurrPrice(status));
	isHot.setValue(Ext.ux.cfs.isHot(status));
	isWeight.setValue(Ext.ux.cfs.isWeigh(status));
	isCommission.setValue(Ext.ux.cfs.isCommission(status));
	isLimit.setValue(Ext.ux.cfs.isLimit(status));
	Ext.getCmp('split_checkbox_menu').setValue(!Ext.ux.cfs.isSplit(status));
	
	if(Ext.ux.cfs.isCommission(status)){
		isCommission.fireEvent('check', isCommission, true);
	}
	if(Ext.ux.cfs.isLimit(status)){
		isLimit.fireEvent('check', isLimit, true);
	}
	
	commission.setValue(data.commission);
	limitCount.setValue(data.limitCount);
	
	if(typeof(data.img) == 'undefined' || data.img == ''){
		//FIXME 图片hardcore
		img.src = 'http://digie-image-real.oss.aliyuncs.com/nophoto.jpg';
	}else{
		img.src = data.img.image;
	}
	
	for (var i = 0; i < food_pricePlans.length; i++) {
		var checkBoxId = 'chbForFoodAlias' + food_pricePlans[i].id,  numberfieldId = 'numBasicForPrice' + food_pricePlans[i].id;
		if(Ext.getDom(checkBoxId).checked){
			Ext.getDom(checkBoxId).checked = false;
			Ext.getCmp(checkBoxId).fireEvent('check', Ext.getCmp(checkBoxId), false);
			
			Ext.getCmp(numberfieldId).setValue();
			Ext.getCmp(numberfieldId).clearInvalid();
		}
	}
	
	Ext.getCmp('food_multiPrice').removeAll();
	multiFoodPriceCount = 0;
	
	foodName.focus(true, 100);
	foodName.clearInvalid();
	foodPinyin.clearInvalid();
	foodPrice.clearInvalid();
	foodKitchenAlias.clearInvalid();
};

/**
 * 添加菜品基础信息
 */
function addBasicHandler(){
	basicOperationBasicHandler({
		type : mmObj.operation.insert
	});
};

/**
 * 修改菜品基础信息
 */
function updateBasicHandler(c){
	c.type = mmObj.operation.update;
	basicOperationBasicHandler(c);
};

/**
 * 操作菜品基础信息,目前支持添加和修改操作
 */
function basicOperationBasicHandler(c){
	
	if(c == null || typeof(c) == 'undefined' || typeof(c.type) == 'undefined'){
		return;
	}
	
	var actionURL = '';
	if(c.type == mmObj.operation.insert){
		actionURL = '../../InsertMenu.do';
	}else if(c.type == mmObj.operation.update){
		actionURL = '../../UpdateMenu.do';
	}else{
		return;
	}
	
	var foodName = Ext.getCmp('txtBasicForFoodName');
	var foodAliasID = Ext.getCmp('numBasicForFoodAliasID');
	var foodPinyin = Ext.getCmp('txtBasicForPinyin');
	var foodPrice = Ext.getCmp('numBasicForPrice');
	var foodKitchenAlias = Ext.getCmp('cmbBasicForKitchenAlias');
	var foodDesc = Ext.getCmp('txtBasicForDesc');
	var isSpecial = Ext.getCmp('chbForBasicSpecial');
	var isRecommend = Ext.getCmp('chbForBasicRecommend');
	var isFree = Ext.getCmp('chbForBasicFree');
	var isStop = Ext.getCmp('chbForBasicStop');
	var isCurrPrice = Ext.getCmp('chbForBasicCurrPrice');
	var isHot = Ext.getCmp('chbForBasicHot');
	var isWeight = Ext.getCmp('chbForBasicWeight');
	var isCommission = Ext.getCmp('chbForBasicCommission');
	var isLimit = Ext.getCmp('chbForBasicLimit');
	var split;
	if(Ext.getCmp('split_checkbox_menu').checked){
		split = false;
	}else{
		split = true;
	}
	
	
	var commission = Ext.getCmp('numCommission');
	var limitCount = Ext.getCmp('numLimitCount');

	var isCombination = false;
	var comboContent = '';
	var kitchenID = '';
	var cfg = Ext.getCmp('combinationFoodGrid');
	var foodPrices = '';
	var multiFoodPrices = '';
		
	if(!foodName.isValid() || !foodPrice.isValid() || !foodKitchenAlias.isValid()){
//		Ext.getCmp('foodOperationWinTab').setActiveTab('basicOperationTab');
		return;
	}
	
	if(c.type == mmObj.operation.insert){
		if(cfg.getStore().getCount() > 0){
			isCombination = true;
			for(var i = 0; i < cfg.getStore().getCount(); i++){
				comboContent += (i > 0 ? '<split>' : '');
				comboContent += (cfg.getStore().getAt(i).get('id') + ',' + cfg.getStore().getAt(i).get('amount'));
			}
		}
	}else if(c.type == mmObj.operation.update){
		isCombination = (typeof(c.data) != 'undefined' && typeof(c.data.combination) != 'undefined' ? c.data.combination : false);
	}
	
	
	for (var i = 0; i < food_pricePlans.length; i++) {
		var checked = document.getElementById('chbForFoodAlias' + food_pricePlans[i].id).checked;
		if(checked){
			if(foodPrices){
				foodPrices += '&';
			}
			foodPrices += (food_pricePlans[i].id + ',' + Ext.getCmp('numBasicForPrice'+food_pricePlans[i].id).getValue());  
		}
	}
	
	if(multiFoodPriceCount > 0){
		for (var i = 1; i <= multiFoodPriceCount; i++) {
			var unit = Ext.getCmp('multiPriceUnit'+i);
			var price = Ext.getCmp('multiPriceValue'+i);
			//过滤已经删除了的单位价格
			if(unit && price){
				//过滤信息没有填齐全的单位价格
				if(unit.getValue() && parseInt(price.getValue()) >= 0){
					
					if(multiFoodPrices){
						multiFoodPrices += '&';
					}
					multiFoodPrices += (unit.getValue() + "," + price.getValue());						
				}
			
			}
		}		
	}

	if(c.type == mmObj.operation.insert){
		Ext.getCmp('btnAddForOW').setDisabled(true);
		Ext.getCmp('btnCloseForOW').setDisabled(true);
		Ext.getCmp('btnRefreshForOW').setDisabled(true);
	}else if(c.type == mmObj.operation.update){
		setButtonStateOne(true);
	}
	
	var printKitchenId;
	if(Ext.getCmp('selectPrintRestaurant_checkBox_menuMgrMain').checked){
		printKitchenId = Ext.getCmp('restaurant_checkbox_menuMgrMain').getValue();
	}else{
		printKitchenId = '';
	}
	
	
	Ext.Ajax.request({
		url : actionURL,
		params : {
			restaurantID : restaurantID,
			foodID : (typeof(c.data) != 'undefined' && typeof(c.data.id) != 'undefined' ? c.data.id : 0),
			foodName : foodName.getValue().trim(),
			foodAliasID : document.getElementById('chbForFoodAlias').checked?foodAliasID.getValue():'',
			foodPinyin : foodPinyin.getValue(),
			foodPrice : foodPrice.getValue()?foodPrice.getValue() : 0,
			foodPrices : foodPrices,
			kitchenID : foodKitchenAlias.getValue(),
			foodDesc : foodDesc.getValue().trim(),
			isSpecial : isSpecial.getValue(),
			isRecommend : isRecommend.getValue(),
			isFree : isFree.getValue(),
			isStop : isStop.getValue(),
			isCurrPrice : isCurrPrice.getValue(),
			isHot : isHot.getValue(),
			isCombination : isCombination,
			isWeight : isWeight.getValue(),
			isCommission : isCommission.getValue(),
			isLimit : isLimit.getValue(),
			commission : commission.getValue(),
			limitCount : limitCount.getValue(),
			comboContent : comboContent,
			foodImage : foodOperationWin.foodImage,
			multiFoodPrices : multiFoodPrices,
			printKitchenId : printKitchenId,
			split : split
			//TODO split
		},
		success : function(res, opt){
			var jr = Ext.util.JSON.decode(res.responseText);
			if(jr.success == true){
				if(c.type == mmObj.operation.insert){
					if(c.close == true){
						Ext.getCmp('foodOperationWin').hide();
						//FIXME 手动获取grid的最后一页
						var bToolBar = Ext.getCmp('menuMgrGrid').getBottomToolbar();
						bToolBar.moveLast();
					}else{
						Ext.example.msg(jr.title, "添加成功, 请继续添加下一个菜品或关闭");
/*						Ext.Msg.confirm(jr.titile, jr.msg + '\n是否继续添加?', function(e){
							if(e == 'yes'){*/
								var faid = foodAliasID.getValue();
								var kaid = foodKitchenAlias.getValue();	
								
								var	isSpecialValue = isSpecial.getValue();
								var isRecommendValue = isRecommend.getValue();
								var isFreeValue = isFree.getValue();
								var isStopValue = isStop.getValue();
								var isCurrPriceValue = isCurrPrice.getValue();
								var isHotValue = isHot.getValue();
								var isWeightValue = isWeight.getValue();
								var isCommissionValue = isCommission.getValue();
								
								//重置信息
								resetbBasicOperation();
								
								isSpecial.setValue(isSpecialValue);
								isRecommend.setValue(isRecommendValue);
								isFree.setValue(isFreeValue);
								isStop.setValue(isStopValue);
								isCurrPrice.setValue(isCurrPriceValue);
								isHot.setValue(isHotValue);
								isWeight.setValue(isWeightValue);
								isCommission.setValue(isCommissionValue);
								Ext.getCmp('split_checkbox_menu').setValue(split);
								
								
								if(document.getElementById('chbForFoodAlias').checked){
									foodAliasID.setValue(parseInt(faid+1));
								}
								foodKitchenAlias.setValue(kaid);
								
								foodName.focus(true, 100);
	
/*							}else{
								Ext.getCmp('foodOperationWin').hide();
							}
						}, this);*/
					}
				}else if(c.type == mmObj.operation.update){
					if(c.hide == true){
						Ext.getCmp('foodOperationWin').hide();
					}
					
					if(foodOperationWin.uploadImage){
						Ext.example.msg('提示', '图片上传并应用成功');
					}else{
						Ext.example.msg(jr.title, jr.msg);
					}
					
					foodName.setValue(foodName.getValue().trim());
					foodDesc.setValue(foodDesc.getValue().trim());
					Ext.getCmp('menuMgrGrid').getStore().each(function(record){
						if(record.get('id') == c.data.foodID){
							record.set('name', foodName.getValue().trim());
							record.set('pinyin', foodPinyin.getValue());
							record.set('unitPrice', foodPrice.getValue());
							record.set('kitchen.id', kitchenID);
							record.set('kitchen.name', foodKitchenAlias.getRawValue());
							record.set('kitchen.alias', foodKitchenAlias.getValue());
							record.set('desc', foodDesc.getValue().trim());
							record.set('special', isSpecial.getValue());
							record.set('recommend', isRecommend.getValue());
							record.set('gift', isFree.getValue());
							record.set('stop', isStop.getValue());
							record.set('currPrice', isCurrPrice.getValue());
							record.set('hot', isHot.getValue());
							record.set('weight', isWeight.getValue());
							record.set('commission', isCommission.getValue());
							Ext.ux.formatFoodName(record, 'displayFoodName', 'name');
							record.commit();
							return;
						}
					});
				}
			}else{
				Ext.ux.showMsg(jr);
			}
			
			if(c.type == mmObj.operation.insert){
				Ext.getCmp('btnAddForOW').setDisabled(false);
				Ext.getCmp('btnCloseForOW').setDisabled(false);
				Ext.getCmp('btnRefreshForOW').setDisabled(false);
			}else if(c.type == mmObj.operation.update){
				setButtonStateOne(false);
			}
			
			foodOperationWin.foodImage = '';
			foodOperationWin.uploadImage = false;
		},
		failure : function(res, opt) {
			Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
			if(c.type == mmObj.operation.insert){
				Ext.getCmp('btnAddForOW').setDisabled(false);
				Ext.getCmp('btnCloseForOW').setDisabled(false);
				Ext.getCmp('btnRefreshForOW').setDisabled(false);
			}else if(c.type == mmObj.operation.update){
				setButtonStateOne(false);
			}
		}
	});
	
};

/**
 * 图片操作
 */
function uploadFoodImage(c){
	var params = {};
	if(typeof(c.arrt) == 'undefined' || typeof(c.arrt.type) == 'undefined'){
		Ext.example.msg('提示', '操作失败, 获取图片操作类型失败, 请联系客服人员.');
		return;
	}
	if(c.arrt.type != mmObj.operation.img.upload){
		Ext.example.msg('提示', '操作失败, 获取图片操作类型失败, 请联系客服人员.');
		return;
	}
	
	var btnUploadFoodImage = Ext.getCmp('btnUploadFoodImage');
	var btnDeleteFoodImage = Ext.getCmp('btnDeleteFoodImage');
	
	btnUploadFoodImage.setDisabled(true);
	btnDeleteFoodImage.setDisabled(true);
	setButtonStateOne(true);
	
	if(!foodImageUpdateLoaddingMask){
		foodImageUpdateLoaddingMask = new Ext.LoadMask(document.body, {
			msg  : '图片操作中，请稍候......'
		});
	}
	foodImageUpdateLoaddingMask.show();		
	
	Ext.Ajax.request({
		url : '../../OperateImage.do?dataSource=upload&ossType=4',
		isUpload : true,
		form : Ext.getCmp('imgFileUploadForm').getForm().getEl(),
		success : function(response, options){
			foodImageUpdateLoaddingMask.hide();
			var jr = Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,''));
			if(eval(jr.success)){
				if(c.arrt.type == mmObj.operation.img.upload){
					foodOperationWin.foodImage = jr.root[0].imageId;
					Ext.getCmp('menuMgrGrid').getStore().each(function(record){
						if(record.get('id') == c.id){
							record.set('img', jr.root[0].image);
							record.commit();
							return;
						}
					});				
				}
				
				if(typeof(c.arrt) != 'undefined' && typeof(c.arrt.type) != 'undefined' && c.arrt.type == mmObj.operation.img.del)
					refreshFoodImageMsg();
				
				//Ext.example.msg(jr.title, jr.msg);  不提示成功, 直接应用
				Ext.getCmp('btnAppForOW').handler();
				
			}else{
				Ext.ux.showMsg(jr);
			}
			btnUploadFoodImage.setDisabled(false);
			btnDeleteFoodImage.setDisabled(false);
			setButtonStateOne(false);
		},
		failure : function(response, options){
			foodImageUpdateLoaddingMask.hide();
			btnUploadFoodImage.setDisabled(false);
			btnDeleteFoodImage.setDisabled(false);
			setButtonStateOne(false);
			Ext.ux.showMsg(Ext.decode(response.responseText.replace(/<\/?[^>]*>/g,'')));
	   	}
     });
};

/**
 * 
 */
function refreshFoodImageMsg(){
	var img = Ext.getDom('foodBasicImg');
	var imgFile = Ext.getCmp('txtImgFile');
	var imgForm = Ext.getCmp('imgFileUploadForm');
	img.src = '../../images/nophoto.jpg';
	if(imgFile){
		imgForm.remove('txtImgFile');
	}
	imgFile = getImageFile();
	imgForm.add(imgFile);
	imgForm.doLayout();
	Ext.getCmp('basicOperationPanel').doLayout();
	//Ext.getCmp('testimg').doLayout();
};

/**
 * 
 */
function getImageFile(){
	var img = new Ext.form.TextField({
		xtype : 'textfield',
		id : 'txtImgFile',
		name : 'txtImgFile',
		fieldLabel : '选择图片',
		height : 22,
		inputType : 'file',
		listeners : {
			render : function(e){
				Ext.get('txtImgFile').on('change', function(){
					try{
						if(Ext.isIE){
	 						var img = Ext.getDom('foodBasicImg');
	 						var file = Ext.getDom('txtImgFile');
	 						file.select();
	 						var imgURL = document.selection.createRange().text;
	 						if(imgURL && imgURL.length > 0){
	 							var index = imgURL.lastIndexOf('.');
 	        	    			var type = imgURL.substring(index+1, img.length);
 	        	    			var check = false;
 	        	    			for(var i = 0; i < imgTypeTmp.length; i++){
 	        	    				if(type.toLowerCase() == imgTypeTmp[i].toLowerCase()){
 	        	    					check = true;
 	        	    					break;
 	        	    				}
 	        	    			}
 	        	    			if(check){
 	        	    				img.src = Ext.BLANK_IMAGE_URL;
 	        	    				img.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgURL;								 	        	    				
 	        	    			}else{
 	        	    				file.select();
 	        	    				document.execCommand('Delete');
 	        	    				Ext.example.msg('提示', '操作失败,选择的图片类型不正确,请重新选择!');
 	        	    			}
	 						}
	 					}else{
	 						var img = Ext.getDom('foodBasicImg');
	 						var file = Ext.getDom('txtImgFile'); 
	 						if(file.files && file.files[0]){
	 							var index = file.value.lastIndexOf('.');
 	        	    			var type = file.value.substring(index+1, img.length);
 	        	    			var check = false;
 	        	    			for(var i = 0; i < imgTypeTmp.length; i++){
 	        	    				if(type.toLowerCase() == imgTypeTmp[i].toLowerCase()){
 	        	    					check = true;
 	        	    					break;
 	        	    				}
 	        	    			}
 	        	    			if(check){
 	        	    				var reader = new FileReader();
 	        	    				reader.onload = function(evt){img.src = evt.target.result;};
 	        	    				reader.readAsDataURL(file.files[0]);
 	        	    				
 	        	    				//获取图片后, 确认无错误立即上传
 	        	    				foodOperationWin.uploadImage = true;
 	        	    				Ext.getCmp('btnUploadFoodImage').handler();
 	        	    				
 	        	    				
 	        	    			}else{
 	        	    				file.value = '';
 	        	    				Ext.example.msg('提示', '操作失败,选择的图片类型不正确,请重新选择!');
 	        	    			}
	 						}
	 					}
					} catch(e){
						Ext.example.msg('提示', '操作失败,无法获取图片信息.请换浏览器后重试.');
					}
				}, this);
			}
		}
	});
	return img;
};

//------------------------------------------------------------------
function operationFoodPricePlanData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var foodID = Ext.getCmp('hideFoodPricePlanID');
	var pricePlanID = Ext.getCmp('hidePricePlanID');
	var pricePlanName = Ext.getCmp('txtFoodPricePlanName');
	var unitPrice = Ext.getCmp('numFoodPricePlanUnitPrice');
	if(c.type == mmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		foodID.setValue(data['foodId']);
		pricePlanID.setValue(data['planId']);
		pricePlanName.setValue(data['pricePlan.name']);
		unitPrice.setValue(data['unitPrice']);
	}else if(c.type == mmObj.operation['get']){
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

function fppGridUpdateHandler(){
	var sd = Ext.ux.getSelData('fppGrid');
	if(!sd){
		Ext.example.msg('提示', '请选择一个方案价格再进行操作.');
		return;
	}
	Ext.getCmp('fppOperationPanel').show();
	foodPricePlanWin.doLayout();
	operationFoodPricePlanData({
		type : mmObj.operation['set'],
		data : sd
	});
};

function fppGridOperationRenderer(){
	return '<a href="javascript:fppGridUpdateHandler()">修改</>';
};

function fppOperation(){
	if(!foodPricePlanWin){
		var fppGridTbar = new Ext.Toolbar({
			height : 26,
			items : ['->', {
				text : '刷新',
				id : 'btnRefreshFPPGrid',
				iconCls : 'btn_refresh',
				handler : function(){
					var gs = fppGrid.getStore();
					gs.removeAll();
					
					oPanel.hide();
					foodPricePlanWin.doLayout();
					
					gs.baseParams['searchValue'] = foodPricePlanWin.foodData['alias'];
					gs.load();
				}
			}, {
				text : '修改',
				iconCls : 'btn_edit',
				handler : function(){
					fppGridUpdateHandler();
				}
			}]
		});
		fppGrid = createGridPanel(
			'fppGrid',
			'',
			'',
			'',
			'../../QueryFoodPricePlan.do',
			[
				[true, false, false, false], 
				['方案', 'pricePlan.name'],
				['价格', 'unitPrice', 60, 'right', Ext.ux.txtFormat.gridDou],
				['操作', 'operation', 80, 'center', fppGridOperationRenderer]
			],
			FoodPricePlan.getKeys(),
			[['restaurantID', restaurantID], ['searchType', 1], ['searchValue', 0]],
			0,
			'',
			fppGridTbar
		);
		fppGrid.region = 'center';
		
		oPanel = new Ext.Panel({
			id : 'fppOperationPanel',
			frame : true,
			region : 'south',
			layout : 'column',
			autoHeight : true,
			defaults : {
				xtype : 'form',
				layout : 'form',
				labelWidth : 35
			},
			items : [{
				xtype : 'hidden',
				id : 'hidePricePlanID'
			}, {
				xtype : 'hidden',
				id : 'hideFoodPricePlanID'
			}, {
				columnWidth : .5,
				items : [{
					xtype : 'textfield',
					id : 'txtFoodPricePlanName',
					fieldLabel : '方案',
					width : 100,
					disabled : true
				}]
			}, {
				columnWidth : .5,
				items : [{
					xtype : 'numberfield',
					id : 'numFoodPricePlanUnitPrice',
					fieldLabel : '价格',
					width : 100,
					allowBlank : false,
					blankText : '价格不能为空.',
					validator : function(v){
						if(v >= 0 && v < 65535){
							return true;
						}else{
							return '价格不能为空.';
						}
					}
				}]
			}],
			buttonAlign : 'center',
			buttons : [{
				text : '保存',
				handler : function(){
					var unitPrice = Ext.getCmp('numFoodPricePlanUnitPrice');
					if(!unitPrice.isValid()){
						return;
					}
					var foodPricePlan = operationFoodPricePlanData({
						type : mmObj.operation['get']
					}).data;
					
					Ext.Ajax.request({
						url : '../../UpdateFoodPricePlan.do',
						params : {
							foodPricePlan : Ext.encode(foodPricePlan)
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnRefreshFPPGrid').handler();
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
				id : 'btnCloseFoodPricePlanPanel',
				handler : function(){
					oPanel.hide();
					foodPricePlanWin.doLayout();
				}
			}]
		});
		
		foodPricePlanWin = new Ext.Window({
			title : '&nbsp;',
			modal : true,
			resizable : false,
			closable : false,
			draggable : false,
			width : 350,
			height : 390,
			layout : 'border',
			items : [fppGrid, oPanel],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					foodPricePlanWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					foodPricePlanWin.hide();
				}
			}],
			listeners : {
				beforeshow : function(){
					oPanel.hide();
				},
				show : function(){
					Ext.getCmp('btnRefreshFPPGrid').handler();
				},
				hide : function(){
					fppGrid.getStore().removeAll();
				}
			}
		});
	}
	var sd = Ext.ux.getSelData(menuGrid);
	foodPricePlanWin.foodData = sd;
	foodPricePlanWin.setTitle(sd['name']+' -- 所有价格方案信息');
	foodPricePlanWin.show();
	
};
//------------------
//-----------load----------------

function initKitchenTreeForSreach(){
	
	kitchenTreeForSreach = new Ext.tree.TreePanel({
		id : 'kitchenTreeForSreach',
		region : 'west',
		frame : true,
		width : 200,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		enableDD : true,
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryKitchen.do',
			baseParams : {
				dataSource : 'deptKitchenTree'
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			text : '全部',
			expanded : true
		}),
		tbar : ['->', {
			text : '刷新',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getDom('showTypeForSearchKitchen').innerHTML = '----';
				kitchenTreeForSreach.getRootNode().reload();
			}
		}],
		listeners : {
			click : function(e){
				Ext.getDom('showTypeForSearchKitchen').innerHTML = e.text;
				searchMenuHandler();
				Ext.getCmp('menuMgrGrid').getSelectionModel().clearSelections();
				Ext.getCmp('menuMgrGrid').fireEvent('rowclick');
			},
			startdrag : function(t, n, e){
				if(!n.hasChildNodes()){
					startDeptNode = n.parentNode;
				}
			}
			,
			nodedrop : function(e){
				var url = '', params = {};
				//部门之间拖动
				if(typeof e.dropNode.attributes.deptID != 'undefined'){
					if(typeof e.target.attributes.deptID != 'undefined'){
						url = '../../OperateDept.do';
						params = {
							dataSource : 'swap',
							deptA : e.dropNode.attributes.deptID,
							deptB : e.target.attributes.deptID
						};
					}else{
						url = '../../OperateDept.do';
						params = {
							dataSource : 'swap',
							deptA : e.dropNode.attributes.deptID,
							deptB : e.target.parentNode.attributes.deptID
						};
					}
				//节点之间
				}else{
					if(typeof e.target.attributes.deptID != 'undefined'){
						url = '../../UpdateKitchen.do';
						params = {
							kitchenID : e.dropNode.attributes.kid,
							isAllowTemp : e.dropNode.attributes.isAllowTemp,
							deptID : e.target.attributes.deptID
						};
					}else{
						//不同部门
						if(e.target.parentNode != startDeptNode){
							url = '../../UpdateKitchen.do';
							params = {
								kitchenID : e.dropNode.attributes.kid,
								isAllowTemp : e.dropNode.attributes.isAllowTemp,
								deptID : e.target.parentNode.attributes.deptID,
								kitchenB : e.target.attributes.kid,
								move : true
							};
						}else{
							url = '../../OperateKitchen.do',
							params = {
								dataSource : 'swap',
								kitchenA : e.dropNode.attributes.kid,
								kitchenB : e.target.attributes.kid
							};
						}

					}
				}
				
				Ext.Ajax.request({
					url : url,
					params : params,
					success : function(res, opt){
						kitchenTreeForSreach.getRootNode().reload();
					},
					failure : function(res, opt){
						Ext.ux.show(Ext.decode(res.responseText));
					}
				});
			}
		}
	});

	
}
function menuIsHaveImage(value, cellmeta, record, rowIndex, columnIndex, store){
	var style = '', content = '';
	if(record.get('img')){
		style = 'style="color:green;"';
		content = '已上传';		
	}else{
		content = '未设置';
	}
	return '<a href=\"javascript:btnFood.handler()" ' + style + ' >' + content + '</a>';
};

function menuIsBranchId(value, cellmeta, record, rowIndex, columnIndex, store){
	if(record.json.printKitchenId == 0){
		return value;
	}else{
		return value + '<font color="red" style="font-size:16px;"> * </font>'; 
	}
	
}

function menuDishOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return '' 
		 + '<a href=\"javascript:foodRelationWinShow()">套菜关联</a>'
		 + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		 + '<a href=\"javascript:btnFood.handler()">修改</a>'
		 + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		 + '<a href=\"javascript:btnDeleteFood.handler()">删除</a>'
//		 + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
//		 + '<a href=\"javascript:testFn()">test</a>'
		 + '';
};

function initMenuGrid(){
	var menuColumnModel = new Ext.grid.ColumnModel([ 
	    new Ext.grid.RowNumberer(), 
	    {
    		header : '助记码',
    		dataIndex : 'alias',
    		width : 65
    	}, {
    		header : '名称',
    		dataIndex : 'displayFoodName',
    		width : 180
    	}, {
    		header : '拼音',
    		dataIndex : 'pinyin',
    		width : 65
    	}, {
    		header : '价格',
    		dataIndex : 'unitPrice',
    		width : 65,
    		align : 'right',
    		renderer : Ext.ux.txtFormat.gridDou
    	}, {
    		header : '厨房',
    		dataIndex : 'kitchen.name',
    		width : 65,
    		renderer : menuIsBranchId
    	}, {
    		header : '图片状态',
    		width : 65,
    		align : 'center',
    		renderer : menuIsHaveImage
    	}, {
    		header : '操作',
    		dataIndex : 'operator',
    		width : 200,
    		align : 'center',
    		renderer : menuDishOpt
    	}
    ]);                                 
	var menuStore = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({ url : "../../QueryMenuMgr.do" }),
		reader : new Ext.data.JsonReader(Ext.ux.readConfig, FoodBasicRecord.getKeys()),
		listeners : {
			load : function(thiz, records){
				for(var i = 0; i < records.length; i++){
					Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name');
				}
			}
		}
	});
	menuGrid = new Ext.grid.GridPanel({
		id : 'menuMgrGrid',
		region : 'center',
		frame : true,
//		margins : '0 5 0 0',
		ds : menuStore,
		cm : menuColumnModel,
		sm : new Ext.grid.RowSelectionModel({
			singleSelect : true
		}),
		viewConfig : {
			forceFit : true
		},
		bbar : createPagingBar(GRID_PADDING_LIMIT_20, menuStore),
		autoScroll : true,
		loadMask : { msg : '数据加载中，请稍等...' },
		tbar : new Ext.Toolbar({
				height : 26,
				items : [{
					xtype : 'tbtext',
					text : String.format(Ext.ux.txtFormat.typeName, '分厨', 'showTypeForSearchKitchen', '----')
				}, { 
					xtype:'tbtext', 
					text:'过滤:'
				}, menu_filterTypeComb, { 
					xtype:'tbtext', 
					text:'&nbsp;&nbsp;'
				}, {
					xtype : 'combo',
					hidden : true,
					hideLabel : true,
					forceSelection : true,
					width : 100,
					id : 'comboOperatorForGridSearch',
					value : '等于',
					rawValue : 1,
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [[1, '等于'], [2, '大于等于'], [3, '小于等于']]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					readOnly : false,
					allowBlank : false
				}, {
					xtype : 'textfield',
					id : 'textfieldForGridSearch',
					hidden : true,
					width : 120
				}, {
					xtype: 'numberfield',
					id : 'numfieldForGridSearch',
//					style: 'text-align: left;',
					hidden : true,
					width : 120
				}, {
					xtype : 'combo',
					forceSelection : true,
					hidden : true,
					width : 120,
					id : 'kitchenTypeComb',
					store : new Ext.data.JsonStore({
						fields : [ 'alias', 'name' ]
					}),
					valueField : 'alias',
					displayField : 'name',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true
				}, {
					xtype : 'combo',
					forceSelection : true,
					hidden : true,
					width : 120,
					id : 'comboStockStatusForSearch',
					store : new Ext.data.SimpleStore({
						fields : ['value', 'text'],
						data : stockStatusData
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true
				}, { 
					xtype:'tbtext', 
					text:'&nbsp;&nbsp;&nbsp;&nbsp;特价:'
				}, {
					xtype : 'checkbox',
					id : 'specialCheckbox'
				}, { 
					xtype:'tbtext', 
					text:'&nbsp;&nbsp;&nbsp;&nbsp;推荐:'
				}, {
					xtype : 'checkbox',
					id : 'recommendCheckbox'
				}, { 
					xtype:'tbtext', 
					text:'&nbsp;&nbsp;&nbsp;&nbsp;赠送:'
				}, {
					xtype : 'checkbox',
					id : 'freeCheckbox'
				}, { 
					xtype:'tbtext', 
					text:'&nbsp;&nbsp;&nbsp;&nbsp;停售:'
				}, {
					xtype : 'checkbox',
					id : 'stopCheckbox'
				}, { 
					xtype:'tbtext', 
					text:'&nbsp;&nbsp;&nbsp;&nbsp;限量:'
				}, {
					xtype : 'checkbox',
					id : 'limitCheckbox'
				}, { 
					xtype:'tbtext',
					text:'&nbsp;&nbsp;&nbsp;&nbsp;时价:'
				}, {
					xtype : 'checkbox',
					id : 'currPriceCheckbox'
				}, { 
					xtype:'tbtext', 
					text:'&nbsp;&nbsp;&nbsp;&nbsp; 套菜:'
				}, {
					xtype : 'checkbox',
					id : 'combinationCheckbox'
				}, { 
					xtype:'tbtext', 
					text:'&nbsp;&nbsp;&nbsp;&nbsp; 热销:'
				}, {
					xtype : 'checkbox',
					id : 'hotCheckbox'
				}, { 
					xtype:'tbtext',
					text:'&nbsp;&nbsp;&nbsp;&nbsp; 称重:'
				}, {
					xtype : 'checkbox',
					id : 'weightCheckbox'
				}, { 
					xtype:'tbtext',
					text:'&nbsp;&nbsp;&nbsp;&nbsp; 提成:'
				}, {
					xtype : 'checkbox',
					id : 'commissionCheckbox'
				}, { 
					xtype:'tbtext',
					text:'&nbsp;&nbsp;&nbsp;&nbsp; 临时菜:'
				}, {
					xtype : 'checkbox',
					id : 'tempFoodCheckbox'
				}, '->', {
					xtype : 'button',
					hideLabel : true,
					iconCls : 'btn_search',
					id : 'srchBtn',
					text : '搜索',
					width : 100,
					handler : function(thiz, e) {
						searchMenuHandler();
						Ext.getCmp('menuMgrGrid').getSelectionModel().clearSelections();
						Ext.getCmp('menuMgrGrid').fireEvent('rowclick');
					}
				}]
		}),
		listeners : {
			render : function(thiz) {
				searchMenuHandler();
			},
			rowclick : function(thiz, rowIndex, e) {
				/*
				if(!displayInfoPanel.collapsed){
					var selData = Ext.ux.getSelData('menuMgrGrid');
					var selTab = Ext.getCmp('displayInfoPanelTab').getActiveTab();
					if(!selData){
						displayInfoPanel.setTitle('');
					}else{
						displayInfoPanel.setTitle(selData.foodName);
					}
					refreshInfoGrid(selTab);
				}
				*/
			},
			rowdblclick : function(){
				foodOperation(mmObj.operation.update);
			}
		}
	});
}

function initFoodOperationWin(){
	if(!foodOperationWin){
		foodOperationWin = new Ext.Window({
			id : 'foodOperationWin',
			closeAction : 'hide',
			closable : false,
			collapsed : true,
			modal : true,
			resizable : false,
			width : 940,
			height : 510,
			layout : 'fit',
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					foodOperationWin.hide();
				}
			},{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					if(foodOperationWin.operation == mmObj.operation.insert){
						addBasicHandler();
					}else if(foodOperationWin.operation == mmObj.operation.update){
						foodOperationHandler({
			    			win : 'foodDetail',
		    				type : mmObj.operation.update,
		    				hide : true
		    			});
					}
					
				}
			}],
			bbar : [ {
				text : '上一道菜品',
		    	id : 'btnPreviousFood',
		    	iconCls : 'btn_previous',
		    	tooltip : '加载上一道菜品相关信息',
		    	handler : function(){
		    		Ext.getCmp('menuMgrGrid').getSelectionModel().selectPrevious();
		    		Ext.getCmp('foodOperationWin').setTitle(Ext.ux.getSelData('menuMgrGrid').name);
		    		foodOperation(mmObj.operation.update);
		    		Ext.getCmp('btnPreviousFood').setDisabled(!Ext.getCmp('menuMgrGrid').getSelectionModel().hasPrevious());
		    		Ext.getCmp('btnNextFood').setDisabled(false);
		    	}
		    }, {
		    	text : '下一道菜品',
		    	id : 'btnNextFood',
		    	iconCls : 'btn_next',
		    	tooltip : '加载下一道菜品相关信息',
		    	handler : function(){
		    		Ext.getCmp('menuMgrGrid').getSelectionModel().selectNext();
		    		Ext.getCmp('foodOperationWin').setTitle(Ext.ux.getSelData('menuMgrGrid').name);
//		    		Ext.getCmp('foodOperationWinTab').fireEvent('tabchange');
		    		foodOperation(mmObj.operation.update);
		    		Ext.getCmp('btnPreviousFood').setDisabled(false);
		    		Ext.getCmp('btnNextFood').setDisabled(!Ext.getCmp('menuMgrGrid').getSelectionModel().hasNext());
		    	}
		    }, '->', {
				xtype : 'checkbox',
				id : 'split_checkbox_menu',
				checked : false,
				boxLabel : '数量叠加'
			}, {
		    	xtype : 'button',
		    	text : '添加',
		    	id : 'btnAddForOW',
		    	iconCls : 'btn_add',
		    	tooltip : '添加新菜品',
		    	handler : function(){
		    		addBasicHandler();
		    	}
		    }, {
		    	text : '应用',
		    	id : 'btnAppForOW',
		    	iconCls : 'btn_app',
		    	tooltip : '保存修改',
		    	handler : function(){
		    		foodOperationHandler({
		    			win : 'foodDetail',
	    				type : foodOperationWin.otype,
	    				hide : false
	    			});
		    	}
		    }, {
		    	text : '关闭',
		    	id : 'btnCloseForOW',
		    	iconCls : 'btn_close',
		    	tooltip : '关闭窗体',
		    	handler : function(){
		    		foodOperationWin.hide();
		    	}
		    }, {
		    	text : '保存',
		    	id : 'btnSaveForOW',
		    	iconCls : 'btn_save',
		    	tooltip : '保存修改并关闭窗体',
		    	handler : function(){
		    		if(foodOperationWin.otype == mmObj.operation.update){
		    			foodOperationHandler({
			    			win : 'foodDetail',
		    				type : mmObj.operation.update,
		    				hide : true
		    			});
		    		}else{
			    		basicOperationBasicHandler({
		    				type : mmObj.operation.insert,
		    				close : true
		    			});
		    		}

		    	}
		    }, {
		    	text : '重置',
		    	id : 'btnRefreshForOW',
		    	iconCls : 'btn_refresh',
		    	tooltip : '重新加载菜品相关信息',
		    	handler : function(){
		    		var foWinTab = Ext.getCmp('foodOperationWinTab');
		    		if(foodOperationWin.operation == mmObj.operation.insert){
		    			if(foWinTab.getActiveTab().getId() == 'basicOperationTab'){
		    				resetbBasicOperation();
		    			}else if(foWinTab.getActiveTab().getId() == 'combinationOperationTab'){
		    				Ext.getCmp('combinationFoodGrid').getStore().removeAll();
		    				Ext.getCmp('txtMiniAllFoodNameSearch').setValue('');
		    				Ext.getCmp('btnSearchForAllFoodMiniGridTbar').handler();
		    			}
		    		}else{
		    			foWinTab.fireEvent('tabchange');
		    		}
		    	}
		    }],
			listeners : {
				hide : function(){
	    			Ext.getCmp('menuMgrGrid').getStore().reload();
	    			if(document.getElementById('chbForFoodAlias').checked){
	    				document.getElementById('chbForFoodAlias').checked = false;
	    				Ext.getCmp('chbForFoodAlias').fireEvent('check', Ext.getCmp('chbForFoodAlias'), false);
	    			}
	    			
	    		}
			},
			items : [{
		    		xtype : 'panel',
		    		layout : 'fit',
		    		width : 930,
			    	height : Ext.isIE ? 500 : 500,
			    	border : false,
		    		id : 'basicOperationTab',
	    	    	items : [basicOperationPanel]
		    	}]
		});
	}
}


function initFoodRelationOperationWin(){
	if(!foodRelationOperationWin){
		foodRelationOperationWin = new Ext.Window({
			id : 'foodRelationOperationWin',
			closeAction : 'hide',
			closable : false,
			collapsed : true,
			modal : true,
			resizable : false,
			width : 940,
			height : 550,
			layout : 'fit',
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					foodRelationOperationWin.hide();
				}
			}],
			bbar : ['->', {
		    	text : '保存',
		    	id : 'btnSaveForRelation',
		    	iconCls : 'btn_save',
		    	tooltip : '保存修改并关闭窗体',
		    	handler : function(){
		    		foodOperationHandler({
		    			win : 'relation',
	    				type : mmObj.operation.update,
	    				hide : true
	    			});
		    	}
		    },{
		    	text : '关闭',
		    	id : 'btnCloseForRelation',
		    	iconCls : 'btn_close',
		    	tooltip : '关闭窗体',
		    	handler : function(){
		    		foodRelationOperationWin.hide();
		    	}
		    }],
			listeners : {
				hide : function(){
	    			Ext.getCmp('combinationFoodGrid').getStore().removeAll();
	    			Ext.getDom('txtDisplayCombinationFoodPrice').innerHTML = 0.00;
	    			Ext.getDom('txtDisplayCombinationFoodPriceAmount').innerHTML = 0.00;
	    		}
			},
			items : [{
		    		xtype : 'panel',
		    		layout : 'fit',
		    		width : 930,
			    	height : 500,
			    	border : false,
		    		id : 'combinationOperationTab',
	    	    	items : [combinationOperationPanel]
		    	}]
		});
	}
}


var btnAddFood = new Ext.ux.ImageButton({
	imgPath : '../../images/food_add.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加新菜',
	handler : function(btn) {                                                                     
		foodOperation(mmObj.operation.insert);
	}
});

var btnAddKitchen = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddKitchen.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加厨房',
	handler : function(btn) {
		updateKitchenWin.otype = 'insert';
		operateKitchenHandler();
	}
});

var btnAddDept = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddDept.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加部门',
	handler : function(btn) {
		updateDeptWin.otype = 'insert';
		operateDeptHandler();
	}
});

var btnDeleteFood = new Ext.ux.ImageButton({
	imgPath : '',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '删除菜品',
	handler : function(btn) {
		deleteFoodHandler();
	}
});

var btnFood = new Ext.ux.ImageButton({
	imgPath : '',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '修改菜品',
	handler : function(btn) {
		foodOperation(mmObj.operation.update);
	}
});

var btnTaste = new Ext.ux.ImageButton({
	imgPath : '../../images/taste_add.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '口味关联',
	handler : function(btn) {
		foodOperation(mmObj.operation.update);
	}
});

var btnPricePlan = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddPricePlan.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '价格方案',
	handler : function(btn) {
		pricePlanWin.show();

	}
});

var menu_filterTypeComb = new Ext.form.ComboBox({
	forceSelection : true,
	readOnly : false,
	width : 100,
	value : '全部',
	id : 'menu_filter',
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : [[0, '全部'], [1, '助记码'], [2, '名称'],/* [3, '拼音'],*/ [4, '价格']/* ,[5, '厨房'], [6, '库存管理']*/]
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
			var ktCombo = Ext.getCmp('kitchenTypeComb');
			var oCombo = Ext.getCmp('comboOperatorForGridSearch');
			var ct = Ext.getCmp('textfieldForGridSearch');
			var cn = Ext.getCmp('numfieldForGridSearch');
			var stock = Ext.getCmp('comboStockStatusForSearch');
			
			var v = combo.getValue();
			if (v == 0) {
				// 全部
				ktCombo.setVisible(false);
				oCombo.setVisible(false);
				ct.setVisible(false);
				cn.setVisible(false);
				stock.setVisible(false);
				conditionType = '';
			} else if (v == 1 || v == 4) {
				// 编号 或 价格
				ktCombo.setVisible(false);
				oCombo.setVisible(true);
				cn.setVisible(true);
				ct.setVisible(false);
				stock.setVisible(false);
				oCombo.setValue(1);
				cn.setValue('');
				conditionType = cn.getId();
			} else if (v == 2 || v == 3) {
				// 名称 或 拼音
				ktCombo.setVisible(false);
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(true);
				stock.setVisible(false);
				ct.setValue('');
				conditionType = ct.getId();
			} else if (v == 5) {
				// 厨房
				ktCombo.setVisible(true);
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(false);
				stock.setVisible(false);
				ktCombo.store.loadData(kitchenData);
//				ktCombo.setValue(255);	
				conditionType = ktCombo.getId();
			} else if(v == 6){
				ktCombo.setVisible(false);
				oCombo.setVisible(false);
				cn.setVisible(false);
				ct.setVisible(false);
				stock.setVisible(true);
				stock = ktCombo.getId();
			}
		}
	}
});

function deleteFoodHandler() {
	var selData = Ext.ux.getSelData('menuMgrGrid');
	Ext.MessageBox.show({
		msg : '<center>是否确定删除: '+ selData.name +' ？</center>',
		width : 200,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn) {
			if (btn == 'yes') {
				
				Ext.Ajax.request({
					url : '../../DeleteMenu.do',
					params : {
						
						foodID : selData.id,
						restaurantID : restaurantID
					},
					success : function(response, options) {
						var jr = Ext.util.JSON.decode(response.responseText);
						if (eval(jr.success) == true) {
							Ext.example.msg('提示', '菜品(<font color="red">' + selData.name + '</font>)' + jr.msg);
							searchMenuHandler();
							Ext.getCmp('menuMgrGrid').getSelectionModel().clearSelections();
							Ext.getCmp('menuMgrGrid').fireEvent('rowclick');
						} else {
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(response, options) {
						var jr = Ext.util.JSON.decode(response.responseText);
						Ext.ux.showMsg(jr);
					}
				});
			}
		}
	});
};

function setButtonStateOne(s){
	if(typeof s != 'boolean'){
		return;
	}
	Ext.getCmp('btnPreviousFood').setDisabled(s);
	Ext.getCmp('btnNextFood').setDisabled(s);
	Ext.getCmp('btnAppForOW').setDisabled(s);
	Ext.getCmp('btnSaveForOW').setDisabled(s);
	Ext.getCmp('btnCloseForOW').setDisabled(s);
	Ext.getCmp('btnRefreshForOW').setDisabled(s);
};







//价格方案
function pricePlanRenderer (){
	return ''
		   + '<a href="javascript:updatePricePlanHandler()">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:deletePricePlanHandler()">删除</a>';
}

function updatePricePlanHandler(){
	pricePlanOperationHandler({
		type : bmObj.operation['update']
	});
}

function deletePricePlanHandler(){
	pricePlanOperationHandler({
		type : bmObj.operation['delete']
	});
}

function pricePlanOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	pricePlanOperatePanel.otype = c.type;
	
	if(c.type == bmObj.operation['insert']){
		oPricePlanData({
			type : bmObj.operation['set']
		});
		pricePlanOperatePanel.setTitle('添加价格方案');
		pricePlanOperatePanel.show();
		pricePlanWin.syncSize();
		pricePlanWin.doLayout();
	}else if(c.type == bmObj.operation['update']){
		var sd = Ext.ux.getSelData(pricePlanGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			pricePlanOperatePanel.hide();
			pricePlanWin.doLayout();
			return;
		}
		oPricePlanData({
			type : bmObj.operation['set'],
			data : sd
		});
		pricePlanOperatePanel.setTitle('修改方案');
		pricePlanOperatePanel.show();
		pricePlanWin.syncSize();
		pricePlanWin.doLayout();
	}else if(c.type == bmObj.operation['delete']){
		var sd = Ext.ux.getSelData(pricePlanGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个方案再进行操作.');
			pricePlanOperatePanel.hide();
			pricePlanWin.doLayout();
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除方案 ' + sd.name ,
			buttons :Ext.Msg.YESNO,
			icon: Ext.MessageBox.QUESTION,
			fn : function(btn){
				if(btn == 'yes'){
					Ext.Ajax.request({
						url : '../../OperatePricePlan.do',
						params : {
							dataSource : 'delete',
							id : sd['id']
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnCloseCancelPanel').handler();
								pricePlanGrid.getStore().reload();
								getPricePlan();
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
function oPricePlanData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var id = Ext.getCmp('numPricePlanID');
	var name = Ext.getCmp('txtPricePlan');
	if(c.type == bmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		id.setValue(data['id']);
		name.setValue(data['name']);
	}else if(c.type == bmObj.operation['get']){
		data = {
			id : id.getValue(),
			name : name.getValue()
		};
		c.data = data;
	}
	name.clearInvalid();
	name.focus(true, 100);
	return c;
};
function initPricePlanWin(){
	var pricePlanGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				pricePlanOperationHandler({
					type : bmObj.operation['insert']
				});
			}
		}, '-', {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				updatePricePlanHandler();
			}
		}, '-', {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				deletePricePlanHandler();
			}
		}]
	});
	pricePlanGrid = createGridPanel(
		'pricePlanGrid',
		'',
		'',
		'',
		'../../OperatePricePlan.do',
		[
			[true, false, false, false], 
			['名称', 'name'],
			['操作', 'operation', 60, 'center', pricePlanRenderer]
		],
		['id', 'name'],
		[['dataSource', 'getByCond']],
		0,
		'',
		pricePlanGridTbar
	);
	pricePlanGrid.region = 'center';
	
	pricePlanOperatePanel = new Ext.Panel({
		title : '&nbsp;',
		hidden : true,
		frame : true,
		region : 'south',
		layout : 'column',
		autoHeight : true,
		defaults : {
			xtype : 'form',
			layout : 'form',
			labelWidth : 35
		},
		items : [{
			columnWidth : 1,
			items : [{
				xtype : 'textfield',
				id : 'txtPricePlan',
				width : 260,
				fieldLabel : '名称',
				allowBlank : false,
				blankText : '名称不能为空.',
				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0){
						return true;
					}else{
						return '名称不能为空.';
					}
				}
			}]
		}, {
//			columnWidth : .4,
			hidden : true,
			items : [{
				xtype : 'numberfield',
				id : 'numPricePlanID',
				fieldLabel : '编号',
				width : 60,
				disabled : true
			}]
		}
		],
		buttonAlign : 'center',
		buttons : [{
			text : '保存',
			handler : function(){
				var txtPricePlan = Ext.getCmp('txtPricePlan');
				if(!txtPricePlan.isValid()){
					return;
				}
				var pricePlanData = oPricePlanData({
					type :  bmObj.operation['get']
				}).data;
				
				var action, params={};
				
				if(pricePlanOperatePanel.otype == bmObj.operation['insert']){
					action = '../../OperatePricePlan.do';
					params.dataSource = 'insert';
				}else if(pricePlanOperatePanel.otype == bmObj.operation['update']){
					action = '../../OperatePricePlan.do';
					params.dataSource = 'update';
				}else{
					return;
				}
				
				params.id = pricePlanData['id'];
				params.name = pricePlanData['name'];
				
				Ext.Ajax.request({
					url : action,
					params : params,
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnClosePricePlan').handler();
							pricePlanGrid.getStore().reload();
							getPricePlan();
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
			id : 'btnClosePricePlan',
			handler : function(){
				pricePlanOperatePanel.hide();
				pricePlanWin.doLayout();
			}
		}]
	});
	
	pricePlanWin = new Ext.Window({
		title : '价格方案管理',
		modal : true,
		resizable : false,
		closable : false,
		draggable : false,
		width : 350,
		height : 390,
		layout : 'border',
		items : [pricePlanGrid, pricePlanOperatePanel],
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				pricePlanWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				pricePlanWin.hide();
			}
		}],
		listeners : {
			beforeshow : function(){
				pricePlanOperatePanel.hide();
				pricePlanGrid.getStore().reload();
			}
		}
	});
};


function getPricePlan(){
	Ext.Ajax.request({
		url : "../../OperatePricePlan.do",
		params : {
			dataSource : 'getByCond'
		},
		success : function(response){
			var jr = Ext.util.JSON.decode(response.responseText);
			food_pricePlans = jr.root;
		},
		failure : function(){
		
		}
	});
}

Ext.onReady(function() {

	initKitchenTreeForSreach();
	initMenuGrid();
	
	new Ext.Panel({
		renderTo : 'divMenu',
		layout : 'fit',
		height : parseInt(Ext.getDom('divMenu').parentElement.style.height.replace(/px/g,'')),
		items : [ {
			layout : 'border',
			items : [kitchenTreeForSreach, menuGrid]
		} ],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddFood, { 
				xtype:'tbtext', 
				text : '&nbsp;&nbsp;&nbsp;&nbsp;' 
			}, btnAddKitchen, { 
				xtype:'tbtext', 
				text : '&nbsp;&nbsp;&nbsp;&nbsp;' 
			},btnAddDept, { 
				xtype:'tbtext', 
				text : '&nbsp;&nbsp;&nbsp;&nbsp;' 
			},btnPricePlan]
		}),
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){	
				Ext.getCmp('srchBtn').handler(); 
			}
		}]
	});
	
	initFoodOperationWin();
	
	initFoodRelationOperationWin();
	
	foodOperationWin.operation == mmObj.operation.select;
	foodOperationWin.render(document.body);
	
	deptWinShow();
	
	kitchenWinShow();
	
	initDeptComboData();
	initKitchenComboData();
	
	//获取价格方案
	getPricePlan();
	
	//初始化操作价格方案控件
	initPricePlanWin();
	
});
showFloatOption(bar);
