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
			limit : billRecordCount
		}
	});
};

var billListRefresh = function() {
	var discountValue = billGenModForm.getForm().findField("discountRadio")
			.getGroupValue();
	discountType = 0;
	// 获取discountType
	if (discountValue == "discount1") {
		discountType = 1;
	} else if (discountValue == "discount2") {
		discountType = 2;
	} else {
		discountType = 3;
	}
	// 一般、会员、0、1
	// 获取payType
	var payTypeValue = billGenModForm.findById("payTpye").getValue();
	var discountIndex = -1;
	// discountData [厨房编号,一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	if ((payTypeValue == "一般" || payTypeValue == 0) && discountType == 1) {
		discountIndex = 1;
		payType = 1;
	} else if ((payTypeValue == "一般" || payTypeValue == 0) && discountType == 2) {
		discountIndex = 2;
		payType = 1;
	} else if ((payTypeValue == "一般" || payTypeValue == 0) && discountType == 3) {
		discountIndex = 3;
		payType = 1;
	} else if ((payTypeValue == "会员" || payTypeValue == 1) && discountType == 1) {
		discountIndex = 4;
		payType = 2;
	} else if ((payTypeValue == "会员" || payTypeValue == 1) && discountType == 2) {
		discountIndex = 5;
		payType = 2;
	} else if ((payTypeValue == "会员" || payTypeValue == 1) && discountType == 3) {
		discountIndex = 6;
		payType = 2;
	}

	// 显示
	for ( var i = 0; i < orderedData.length; i++) {
		var KitchenNum = orderedData[i][7];
		var discountRate = 1;
		for ( var j = 0; j < discountData.length; j++) {
			if (KitchenNum == discountData[j][0]) {
				discountRate = discountData[j][discountIndex];
			}
		}
		// alert(orderedData[i][12]);
		if (orderedData[i][9] == "true" || orderedData[i][12] == "true"
				|| orderedData[i][18] == "true") {
			// 特价，送，臨時菜 不打折
			orderedData[i][13] = "1.0";
		} else {
			// 非 特价，送，臨時菜
			orderedData[i][13] = discountRate;
		}
	}
	orderedStore.reload();
};
