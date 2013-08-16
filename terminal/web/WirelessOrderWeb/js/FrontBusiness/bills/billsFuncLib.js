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
function billListRefresh(){
	var discount = Ext.getCmp('comboDiscount');
	for ( var i = 0; i < orderedGrid.order.orderFoods.length; i++) {
		var temp = orderedGrid.order.orderFoods[i];
		temp.discount = 1.00;
		temp.unitPrice = temp.actualPrice;
		if(Ext.ux.cfs.isSpecial(temp) || Ext.ux.cfs.isSpecial(temp) || temp.isTemporary){
			
		}else{
			for(var di = 0; di < discountPlanData.root.length; di++){
				if(discountPlanData.root[di].discount.id == discount.getValue() 
						&& discountPlanData.root[di].kitchen.id == temp.kitchen.id){
					temp.discount = parseFloat(discountPlanData.root[di].rate).toFixed(2);
					temp.unitPrice = temp.actualPrice * temp.discount;
					break;
				}
			}
		}
	}
	orderedGrid.getStore().loadData({root:orderedGrid.order.orderFoods});
};
