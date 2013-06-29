function billQueryHandler() {
	var sType = searchType, sValue = '', sOperator = '', sAdditionFilter = 0;
	var onDuty = '', offDuty = '';
	if(sType == 0){
		sValue = '';
		searchOperator = '';
	}else if(sType == 4){
		var temp = searchValue.split(searchSubSplitSymbol);
		onDuty = Ext.getCmp(temp[0]).getValue().format('Y-m-d 00:00:00');
		offDuty = Ext.getCmp(temp[1]).getValue().format('Y-m-d 23:59:59');
		sValue = onDuty + '<split>' + offDuty;
	}else if(searchType == 9){
		sValue = '';
	}else{
		sValue = searchValue != '' ? Ext.getCmp(searchValue).getValue() : '';
		sOperator = searchOperator != '' ? Ext.getCmp(searchOperator).getValue() : '';
		if(typeof sValue == 'string' && sValue == ''){
			sType = 0;
			sValue = '';
		}
	}
	sAdditionFilter = Ext.getCmp(searchAdditionFilter).inputValue;	
	var gs = billsGrid.getStore();
	gs.baseParams['isPaging'] = true;
	gs.baseParams['restaurantID'] = restaurantID;
	gs.baseParams['pin'] = pin;
	gs.baseParams['type'] = sType;
	gs.baseParams['ope'] = sOperator;
	gs.baseParams['value'] = sValue;
	gs.baseParams['havingCond'] = sAdditionFilter;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
};