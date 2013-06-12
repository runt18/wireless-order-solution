/**
 * 对比操作后的菜品口味是否一样
 */
compareNormalTasteContent = function(c1, c2){
	if(c1 == null || c2 == null || typeof c1 == 'undefined' || typeof c2 == 'undefined'){
		return null;
	}
	
	var checkStatus = true;
	if(c1.length == 0 && c2.length == 0){
		checkStatus = true;
	}else if(c1.length != c2.length){
		checkStatus = false;
	}else if(c1.length == c2.length){
		c1.sort(function(a, b){
			return eval(a['tasteID'] > b['tasteID']) ? 1 : -1;
		});
		c2.sort(function(a, b){
			return eval(a['tasteID'] > b['tasteID']) ? 1 : -1;
		});
		for(var i = 0; i < c1.length; i++){
			if(eval(c1[i]['tasteID'] != c2[i]['tasteID'])){
				checkStatus = false;
				break;
			}
		}
	}
	return checkStatus;
};

function billQueryHandler() {
	var sType= 0; sValue = '', sOperator = '', sAdditionFilter = 0;
	if(searchType == 0){
		sValue = '';
		searchOperator = '';
	}else{
		sValue = searchValue != '' ? Ext.getCmp(searchValue).getValue() : '';
		sOperator = searchOperator != '' ? Ext.getCmp(searchOperator).getValue() : '';
	}
	sType = sValue == '' ? 0 : searchType;
//	sAdditionFilter = Ext.getCmp(searchAdditionFilter).inputValue;	
	var addition =  Ext.query('input[name=\"conditionRadio\"]');
	for(var i = 0; i < addition.length; i++){
		if(addition[i].checked){
			addition = addition[i].value;
			break;
		}
	}
	var gs = billsGrid.getStore();
	gs.baseParams['isPaging'] = true;
	gs.baseParams['restaurantID'] = restaurantID;
	gs.baseParams['pin'] = pin;
	gs.baseParams['type'] = sType;
	gs.baseParams['ope'] = sOperator;
	gs.baseParams['value'] = sValue;
	gs.baseParams['havingCond'] = addition;
	gs.load({
		params : {
			start : 0,
			limit : billsGridDataSize
		}
	});
};

/**
 * 刷新相关折扣信息 
 */
billListRefresh = function(){
	
	var discount = Ext.getCmp('comboDiscount');
	for ( var i = 0; i < orderedData.root.length; i++) {
		var tpItem = orderedData.root[i];
		
		if (tpItem.special == true || tpItem.gift == true || tpItem.temporary == true) {
			// 特价，送，臨時菜 不打折
			tpItem.discount = 1.00;
		} else {
			tpItem.discount = 1.00;
			for(var di = 0; di < discountPlanData.root.length; di++){
				if(discount.getValue() != -1 && discountPlanData.root[di].discount.id == discount.getValue() 
						&& discountPlanData.root[di].kitchen.kitchenID == tpItem.kitchen.kitchenID){
					tpItem.discount = parseFloat(discountPlanData.root[di].rate).toFixed(2);
					break;
				}
			}
		}
	}	
	orderedStore.loadData(orderedData);
};
