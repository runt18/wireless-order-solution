function billQuery(in_queryTpye, in_operator, in_condition, in_additionalFilter) {
	Ext.Ajax.request({
		url : "../../QueryToday.do",
		params : {
			restaurantID : restaurantID,
			"pin" : pin,
			"type" : in_queryTpye,
			"ope" : in_operator,
			"value" : in_condition,
			"havingCond" : in_additionalFilter
		},
		success : function(response, options) {
			var resultJSON = Ext.decode(response.responseText);
			if (resultJSON.success == true) {
				billsData = resultJSON;
//				var josnData = resultJSON.data;
//				if (josnData != "") {
//					var billList = josnData.split("，");
//					billsData.length = 0;
//					for ( var i = 0; i < billList.length; i++) {
//						var billInfo = billList[i].substr(1, billList[i].length - 2).split(",");
//						// 格式：["账单号", "台号", "日期", "类型", "结帐方式", "金额",
//						// "实收", "台号2","就餐人数", "最低消", "服务费率", "会员编号",
//						// "会员姓名", "账单备注","赠券金额", "结帐类型", "折扣类型", "服务员",
//						// 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
//						// 后台格式：["账单号", "台号", "日期", "类型", "结帐方式", "金额",
//						// "实收", "台号2","就餐人数", "最低消", "服务费率", "会员编号",
//						// "会员姓名", "账单备注","赠券金额", "结帐类型", "折扣类型", "服务员",
//						// 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
//						billsData.push([
//						    billInfo[0].substr(1, billInfo[0].length - 2),// 账单号
//							billInfo[1].substr(1, billInfo[1].length - 2),// 台号
//							billInfo[2].substr(1, billInfo[2].length - 2),// 日期
//							billInfo[3].substr(1, billInfo[3].length - 2),// 类型
//							billInfo[4].substr(1, billInfo[4].length - 2), // 结帐方式
//							billInfo[5].substr(1, billInfo[5].length - 2), // 金额
//							billInfo[6].substr(1, billInfo[6].length - 2), // 实收
//							billInfo[7].substr(1, billInfo[7].length - 2), // 台号2
//							billInfo[8].substr(1, billInfo[8].length - 2), // 就餐人数
//							billInfo[9].substr(1, billInfo[9].length - 2), // 最低消
//							billInfo[10].substr(1, billInfo[10].length - 2), // 服务费率
//							billInfo[11].substr(1, billInfo[11].length - 2), // 会员编号
//							billInfo[12].substr(1, billInfo[12].length - 2), // 会员姓名
//							billInfo[13].substr(1, billInfo[13].length - 2), // 账单备注
//							billInfo[14].substr(1, billInfo[14].length - 2), // 赠券金额
//							billInfo[15].substr(1, billInfo[15].length - 2), // 结帐类型
//							billInfo[16].substr(1, billInfo[16].length - 2), // 折扣类型
//							billInfo[17].substr(1, billInfo[17].length - 2), // 服务员
//							billInfo[18], // 是否反結帳
//							billInfo[19], // 是否折扣
//							billInfo[20], // 是否赠送
//							billInfo[21], // 是否退菜
//							billInfo[22].substr(1, billInfo[22].length - 2) // 流水号
//						]);
//					}
//					// sum the prices
					
//				} else {
//					billsData.length = 0;
//				}
				
				var sumShouldPay = 0;
				var sumActualPay = 0;
				for ( var i = 0; i < billsData.root.length; i++) {
					sumShouldPay = sumShouldPay + parseFloat(billsData.root[i]['totalPrice']);
					sumActualPay = sumActualPay + parseFloat(billsData.root[i]['acturalPrice']);
				}
				document.getElementById("shouldPaySum").innerHTML = sumShouldPay .toFixed(2);
				document.getElementById("actualPaySum").innerHTML = sumActualPay.toFixed(2);
				billsStore.loadData(billsData);
			}
		},
		failure : function(response, options) {
			
		}
	});
};

function billQueryHandler() {

	currRowIndex = -1;
	var sType= 0; sValue = '', sOperator = '', sAdditionFilter = 0;
	if(searchType == 0){
		sValue = '';
		searchOperator = '';
	}else{
		sValue = searchValue != '' ? Ext.getCmp(searchValue).getValue() : '';
		sOperator = searchOperator != '' ? Ext.getCmp(searchOperator).getValue() : '';
	}
	sType = sValue == '' ? 0 : searchType;
	sAdditionFilter = Ext.getCmp(searchAdditionFilter).inputValue;	
	billQuery(sType, sOperator, sValue, sAdditionFilter);
	
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
