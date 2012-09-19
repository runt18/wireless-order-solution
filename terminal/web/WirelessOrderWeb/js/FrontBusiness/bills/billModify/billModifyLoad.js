// keyboard select handler
var dishKeyboardSelect = function(relateItemId) {
	if (relateItemId == "orderNbr") {
		var curDishNbr = Ext.getCmp("orderNbr").getValue() + "";

		if (curDishNbr == "") {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				dishesDisplayDataShow.push([ dishesDisplayData[i][0],
						dishesDisplayData[i][1], dishesDisplayData[i][2],
						dishesDisplayData[i][3], dishesDisplayData[i][4],
						dishesDisplayData[i][5], dishesDisplayData[i][6],
						dishesDisplayData[i][7], dishesDisplayData[i][8],
						dishesDisplayData[i][9] ]);
			}
		} else {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				if ((dishesDisplayData[i][1] + "").substring(0,
						curDishNbr.length) == curDishNbr) {
					dishesDisplayDataShow.push([ dishesDisplayData[i][0],
							dishesDisplayData[i][1], dishesDisplayData[i][2],
							dishesDisplayData[i][3], dishesDisplayData[i][4],
							dishesDisplayData[i][5], dishesDisplayData[i][6],
							dishesDisplayData[i][7], dishesDisplayData[i][8],
							dishesDisplayData[i][9] ]);
				}
			}
		}

		dishesDisplayStore.reload();
	}
};

function dishSpellOnLoad() {
	// keyboard input dish spell
	$("#orderSpell").bind("keyup", function() {
		var curDishSpell = Ext.getCmp("orderSpell").getValue().toUpperCase() + "";
		if (curDishSpell == "") {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				dishesDisplayDataShow.push([
					dishesDisplayData[i][0],
					dishesDisplayData[i][1],
					dishesDisplayData[i][2],
					dishesDisplayData[i][3],
					dishesDisplayData[i][4],
					dishesDisplayData[i][5],
					dishesDisplayData[i][6],
					dishesDisplayData[i][7],
					dishesDisplayData[i][8],
					dishesDisplayData[i][9] 
				]);
			}
		} else {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				if ((dishesDisplayData[i][2] + "").substring(0, curDishSpell.length).toUpperCase() == curDishSpell) {
					dishesDisplayDataShow.push([
						dishesDisplayData[i][0],
						dishesDisplayData[i][1],
						dishesDisplayData[i][2],
						dishesDisplayData[i][3],
						dishesDisplayData[i][4],
						dishesDisplayData[i][5],
						dishesDisplayData[i][6],
						dishesDisplayData[i][7],
						dishesDisplayData[i][8],
						dishesDisplayData[i][9] 
					]);
				}
			}
		}
		dishesDisplayStore.reload();
	});
}

// on page load function
function billModifyOnLoad() {

	// update the operator name
	getOperatorName(pin, "../../");

	// keyboard input dish number
	$("#orderNbr").bind("keyup", function() {
		dishKeyboardSelect("orderNbr");
	});

	// update table status
	// 对"拼台""外卖"，台号特殊处理
	var tableNbr = "000";
	if (category == "拼台") {
		tableNbr = Request["tableNbr"] + "，" + Request["tableNbr2"];
	} else if (category == "外卖" /* && Request["tableStat"] == "free" */) {
		tableNbr = "外卖";
	} else {
		tableNbr = Request["tableNbr"];
	}

	var personCount = Request["personCount"];
	document.getElementById("tblNbrDivTS").innerHTML = tableNbr
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("perCountDivTS").innerHTML = personCount
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("minCostDivTS").innerHTML = Request["minCost"]
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	if (Request["minCost"] == "0") {
		document.getElementById("minCostDivTS").style["display"] = "none";
		document.getElementById("minCostImgTS").style["display"] = "none";
	}
	document.getElementById("serviceRateDivTS").innerHTML = Request["serviceRate"]
			+ "%" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	if (Request["serviceRate"] == "0") {
		document.getElementById("serviceRateDivTS").style["display"] = "none";
		document.getElementById("serviceRateImgTS").style["display"] = "none";
	}

	// 已点菜式查询
	// 格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,折扣率,￥口味价钱,口味编号2,口味编号3,時,是否临时菜,菜名ORIG]
	// 后台：["菜名",菜名编号,厨房编号,"口味",口味编号,数量,￥单价,是否特价,是否推荐,是否停售,是否赠送,折扣率,口味编号2,口味编号3,￥口味价钱,時,是否临时菜]
	Ext.Ajax.request({
		url : "../../QueryOrder.do",
		params : {
			pin : pin,
			restaurantID : restaurantID,
			orderID : Request["orderID"],
			queryType: 'Today'
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			
			if (resultJSON.success == true) {					
				orderedData = resultJSON;				
				orderedStore.loadData(orderedData);
			} else {
				Ext.MessageBox.show({
					msg : resultJSON.msg,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
			
		}
	});

	// 当前帐单信息查询
	Ext.Ajax.request({
		url : "../../QueryToday.do",
		params : {
			"pin" : pin,
			"type" : 0,
			"ope" : 1,
			"value" : "",
			"havingCond" : 0
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				var josnData = resultJSON.data;
				var billList = josnData.split("，");
				for ( var i = 0; i < billList.length; i++) {
					var billInfo = billList[i].substr(1, billList[i].length - 2).split(",");
					// 格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员", 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
					// 后台格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员", 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
					if (billInfo[0].substr(1, billInfo[0].length - 2) == Request["orderID"]) {
						// 备注
						billComment = billInfo[13].substr(1, billInfo[13].length - 2);
						billGenModForm.findById("remark").setValue(billComment);
						// 结帐类型
						var payTpyeField = billGenModForm.findById("payTpye");
						var payTpyeValue = billInfo[15].substr(1, billInfo[15].length - 2);
						if (payTpyeValue == "1") {
							payTpyeField.setValue("一般");
						} else if (payTpyeValue == "2") {
							payTpyeField.setValue("会员");
						}
						// 折扣类型
						var discountTypeField = billGenModForm.getForm().findField("discountRadio");
						var discountTypeValue = billInfo[16].substr(1, billInfo[16].length - 2);
						if (discountTypeValue == "1") {
							discountTypeField.setValue("discount1");
						} else if (discountTypeValue == "2") {
							discountTypeField.setValue("discount2");
						} else if (discountTypeValue == "3") {
							discountTypeField.setValue("discount3");
						}
						// 服务费率
						billGenModForm.findById("serviceRate").setValue(billInfo[10].substr(1, billInfo[10].length - 2));
						// 结帐方式
						var payTypeField = billGenModForm.getForm().findField("payManner");
						var payTypeValue = billInfo[4].substr(1, billInfo[4].length - 2);
						
						if (payTypeValue == "现金") {
							payTypeField.setValue("cashPay");
						} else if (payTypeValue == "刷卡") {
							payTypeField.setValue("cardPay");
						} else if (payTypeValue == "挂账") {
							payTypeField.setValue("handPay");
						} else if (payTypeValue == "会员卡") {
							payTypeField.setValue("memberPay");
						} else if (payTypeValue == "签单") {
							payTypeField.setValue("signPay");
						}

					}
				}
			}
		},
		failure : function(response, options) {
			
		}
	});

};
