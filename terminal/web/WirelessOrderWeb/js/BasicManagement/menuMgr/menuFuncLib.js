/**
 * 菜品搜索
 */
function searchMenuHandler(){
	var baseParams = {
		'isPaging' : true,
		'pin' : pin,
		'restaurantId' : restaurantID
	};
	// 分厨
	var sn = kitchenTreeForSreach.getSelectionModel().getSelectedNode();
	if(sn && sn.attributes.aliasId >= 0)
		baseParams['kitchen'] = sn.attributes.aliasId;
	// 编号, 名称, 拼音, 价钱, 库存管理
	var queryType = Ext.getCmp('filter').getValue();
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
	
	var foWinTab = Ext.getCmp('foodOperationWinTab');
	var activeTab = foWinTab.getActiveTab();
	var selData = Ext.ux.getSelData('menuMgrGrid');
	c.data = selData;
	
	if(typeof(activeTab) == 'undefined'){
		return;
	}
	
	if(activeTab.getId() == 'basicOperationTab'){
		if(c.type == mmObj.operation.insert){
//			resetbBasicOperation();
		}else if(c.type == mmObj.operation.update){
			updateBasicHandler(c);
		}else if(c.type == mmObj.operation.select){
			if(!selData){
				return;
			}
			resetbBasicOperation(selData);
		}
	}else if(activeTab.getId() == 'tasteOperationTab'){
		if(c.type == mmObj.operation.update){
			updateTasteHandler(c);
		}else if(c.type == mmObj.operation.select){
			if(!selData){
				return;
			}
			if(parseInt(selData.tasteRefType) == 1){
				Ext.getCmp('rdoTasteTypeSmart').setValue(true);
				Ext.getDom('rdoTasteTypeSmart').onclick();
			}else if(parseInt(selData.tasteRefType) == 2){
				Ext.getCmp('rdoTasteTypeManual').setValue(true);
				Ext.getDom('rdoTasteTypeManual').onclick();
			}
			var ctgd = Ext.getCmp('commonTasteGrid').getStore();
			ctgd.load();
		}
	}else if(activeTab.getId() == 'materialOperationTab'){
		if(c.type == mmObj.operation.update){
			updateMaterialHandler(c);
		}else if(c.type == mmObj.operation.select){
			if(!selData){
				return;
			}
			Ext.getCmp('txtMaterialNameSearch').setValue('');
			Ext.getCmp('btnSearchForAllMaterialGridTbar').handler();
			var hmgd = Ext.getCmp('haveMaterialGrid').getStore();
			hmgd.load();
		}
	}else if(activeTab.getId() == 'combinationOperationTab'){
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
			cfgd.load();
		}
	}	
};

/**
 * 
 * @param active
 * @param type
 */
function foodOperation(active, type){
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
	foWin.show();
	foWin.center();
	
	var btnAddForOW = Ext.getCmp('btnAddForOW');
	var btnAppForOW = Ext.getCmp('btnAppForOW');
	var btnSaveForOW = Ext.getCmp('btnSaveForOW');
	var btnCloseForOW = Ext.getCmp('btnCloseForOW');
	var btnRefreshForOW = Ext.getCmp('btnRefreshForOW');
	var btnPreviousFood = Ext.getCmp('btnPreviousFood');
	var btnNextFood = Ext.getCmp('btnNextFood');
	var txtFoodPriceExplain = Ext.getCmp('txtFoodPriceExplain');
	
	if(typeof(type) == 'string' && type == mmObj.operation.insert){
		foWin.setTitle('添加菜品');
		btnAddForOW.setVisible(true);
		btnAppForOW.setVisible(false);
		btnSaveForOW.setVisible(false);
		btnPreviousFood.setVisible(false);
		btnNextFood.setVisible(false);		
		txtFoodPriceExplain.setVisible(false);
		
		btnAddForOW.setDisabled(false);
		btnAppForOW.setDisabled(true);
		btnSaveForOW.setDisabled(true);
		
		resetbBasicOperation();
		Ext.getCmp('combinationFoodGrid').getStore().removeAll();
		Ext.getDom('txtDisplayCombinationFoodPrice').innerHTML = '0.00';
		Ext.getDom('txtDisplayCombinationFoodPriceAmount').innerHTML = '0.00';
		Ext.getCmp('txtMiniAllFoodNameSearch').setValue('');
		Ext.getCmp('btnSearchForAllFoodMiniGridTbar').handler();
	}else if(typeof(type) == 'string' && type == mmObj.operation.update){
		foWin.setTitle(selRowData.name);
		btnAddForOW.setVisible(false);
		btnAppForOW.setVisible(true);
		btnSaveForOW.setVisible(true);
		btnPreviousFood.setVisible(true);
		btnNextFood.setVisible(true);
		txtFoodPriceExplain.setVisible(true);
		
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
	
	if(typeof active == 'string'){
		if(Ext.getCmp(active)){
			var foWinTab = Ext.getCmp('foodOperationWinTab');
			if(!foWinTab.getActiveTab() || foWinTab.getActiveTab().getId() == active){
				foWinTab.fireEvent('tabchange');
			}else{
				foWinTab.setActiveTab(active);
			}
		}
	}
	
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