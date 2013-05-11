/**
 * 菜品搜索
 */
function searchMenuHandler(){
	var queryType = Ext.getCmp('filter').getValue();
	var searchValue = Ext.getCmp(conditionType);
	var queryOperator = 1, queryValue = '';
	if(queryType == '全部' || queryType == 0 || !searchValue || searchValue.getValue().toString().trim() == '' ){	
		queryType = 0;
		queryValue = '';
	}else{
		queryOperator = Ext.getCmp('operator').getValue();
		if (queryOperator == '等于') {
			queryOperator = 1;
		}
		queryValue = searchValue.getValue();
	}
		
	var in_isSpecial = Ext.getCmp('specialCheckbox').getValue();
	var in_isRecommend = Ext.getCmp('recommendCheckbox').getValue();
	var in_isFree = Ext.getCmp('freeCheckbox').getValue();
	var in_isStop = Ext.getCmp('stopCheckbox').getValue();
	var in_isCurrPrice = Ext.getCmp('currPriceCheckbox').getValue();
	var in_isCombination = Ext.getCmp('combinationCheckbox').getValue();
	var in_isHot = Ext.getCmp('hotCheckbox').getValue();
	var in_isWeight = Ext.getCmp('weightCheckbox').getValue();
	
	var sn = kitchenTreeForSreach.getSelectionModel().getSelectedNode();
	var gs = menuGrid.getStore();
	gs.baseParams = {
		'pin' : pin,
		'type' : queryType,
		'ope' : queryOperator,
		'value' : queryValue,
		'kitchenAlias' : sn && eval(sn.attributes.aliasId >= 0) ? sn.attributes.aliasId : '',
		'isSpecial' : in_isSpecial,
		'isRecommend' : in_isRecommend,
		'isFree' : in_isFree,
		'isStop' : in_isStop,
		'isCurrPrice' : in_isCurrPrice,
		'isCombination' : in_isCombination,
		'isHot' : in_isHot,
		'isWeight' : in_isWeight,
		'isPaging' : true
	};
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
//	menuMgrGrid.getSelectionModel().clearSelections();
//	menuMgrGrid.fireEvent('rowclick');
}