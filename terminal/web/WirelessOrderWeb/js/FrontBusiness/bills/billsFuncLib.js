function billQueryHandler() {
	
	var gs = billsGrid.getStore();
	if(searchType){
		gs.baseParams['dateBeg'] = duty.getValue().split(salesSubSplitSymbol)[0];
		gs.baseParams['dateEnd'] = duty.getValue().split(salesSubSplitSymbol)[1];
		gs.baseParams['comboPayType'] = Ext.getCmp('comboPayType').getValue();
		gs.baseParams['common'] = Ext.getCmp('textSearchValue').getValue();
		if(isNaN(Ext.getCmp('textTableAliasOrName').getValue())){
			gs.baseParams['tableName'] = Ext.getCmp('textTableAliasOrName').getValue();
		}else{
			gs.baseParams['tableAlias'] = Ext.getCmp('textTableAliasOrName').getValue();
		}
		gs.baseParams['region'] = Ext.getCmp('today_comboRegion').getValue();
	}
	
	gs.baseParams['orderId'] = Ext.getCmp('numberSearchValue').getValue();
	gs.baseParams['seqId'] = Ext.getCmp('tbSeqId').getValue();
	
	sAdditionFilter = Ext.getCmp(searchAdditionFilter).inputValue;	
	
	gs.baseParams['dataType'] = 0;

	gs.baseParams['havingCond'] = sAdditionFilter;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
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
