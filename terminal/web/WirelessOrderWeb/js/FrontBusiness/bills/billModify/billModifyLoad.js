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

	// update label new or mod
	// dishesOrderNorthPanel
	// .setTitle("<div style='font-size:18px;padding-left:2px'>帐单修改<div>");

	// 已点菜式查询
	// 格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,折扣率,￥口味价钱,口味编号2,口味编号3,時,是否临时菜,菜名ORIG]
	// 后台：["菜名",菜名编号,厨房编号,"口味",口味编号,数量,￥单价,是否特价,是否推荐,是否停售,是否赠送,折扣率,口味编号2,口味编号3,￥口味价钱,時,是否临时菜]
	Ext.Ajax.request({
		url : "../../QueryOrder.do",
		params : {
			"pin" : pin,
			"orderID" : Request["orderID"],
			"queryType": "Today"
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			
			if (resultJSON.success == true) {					
				orderedData = resultJSON;
				
				// 根据“特荐停”重新写菜名
				for ( var i = 0; i < orderedData.root.length; i++) {
					var tpItem = orderedData.root[i];
					if (tpItem.special == true) {
						// 特
						tpItem.foodName = tpItem.foodName + "<img src='../../images/icon_tip_te.png'></img>";
					}
					if (tpItem.recommed == true) {
						// 荐
						tpItem.foodName = tpItem.foodName + "<img src='../../images/icon_tip_jian.png'></img>";
					}
					if (tpItem.soldout == true) {
						// 停
						tpItem.foodName = tpItem.foodName + "<img src='../../images/icon_tip_ting.png'></img>";
					}
					if (tpItem.gift == true) {
						// 赠
						tpItem.foodName = tpItem.foodName + "<img src='../../images/forFree.png'></img>";
					}
					if (tpItem.currPrice == true) {
						// 時
						tpItem.foodName = tpItem.foodName + "<img src='../../images/currPrice.png'></img>";
					}
					if (tpItem.temporary == true) {
						// 臨
						tpItem.foodName = tpItem.foodName + "<img src='../../images/tempDish.png'></img>";
					}
				}
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
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					if (resultJSON.success == true) {
						var josnData = resultJSON.data;
						var billList = josnData.split("，");
						for ( var i = 0; i < billList.length; i++) {
							var billInfo = billList[i].substr(1,
									billList[i].length - 2).split(",");
							// 格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员", 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
							// 后台格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型","服务员", 是否反結帳, 是否折扣, 是否赠送, 是否退菜, "流水号"]
							if (billInfo[0].substr(1, billInfo[0].length - 2) == Request["orderID"]) {
								// 备注
								billComment = billInfo[13].substr(1,
										billInfo[13].length - 2);
								billGenModForm.findById("remark").setValue(
										billComment);
								// 结帐类型
								var payTpyeField = billGenModForm
										.findById("payTpye");
								var payTpyeValue = billInfo[15].substr(1,
										billInfo[15].length - 2);
								if (payTpyeValue == "1") {
									payTpyeField.setValue("一般");
								} else if (payTpyeValue == "2") {
									payTpyeField.setValue("会员");
								}
								// 折扣类型
								var discountTypeField = billGenModForm
										.getForm().findField("discountRadio");
								var discountTypeValue = billInfo[16].substr(1,
										billInfo[16].length - 2);
								if (discountTypeValue == "1") {
									discountTypeField.setValue("discount1");
								} else if (discountTypeValue == "2") {
									discountTypeField.setValue("discount2");
								} else if (discountTypeValue == "3") {
									discountTypeField.setValue("discount3");
								}
								// 服务费率
								billGenModForm
										.findById("serviceRate")
										.setValue(
												billInfo[10]
														.substr(
																1,
																billInfo[10].length - 2));
								// 结帐方式
								var payTypeField = billGenModForm.getForm()
										.findField("payManner");
								var payTypeValue = billInfo[4].substr(1,
										billInfo[4].length - 2);
								// var discountValue = billGenModForm.getForm()
								// .findField("payType").getGroupValue();
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

	// 2,折扣率查询
	Ext.Ajax.request({
		url : "../../QueryMenu.do",
		params : {
			"pin" : pin,
			"type" : "3"
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				var discountJSONData = resultJSON.data;
				var discountList = discountJSONData.split("，");
				for ( var i = 0; i < discountList.length; i++) {
					var discountInfo = discountList[i].substr(1,
							discountList[i].length - 2).split(",");
					discountData.push([ discountInfo[0], // 厨房编号
					discountInfo[3],// 一般折扣1
					discountInfo[4],// 一般折扣2
					discountInfo[5],// 一般折扣3
					discountInfo[6],// 会员折扣1
					discountInfo[7],// 会员折扣2
					discountInfo[8], // 会员折扣3
					discountInfo[1] // 厨房id
					]);
				}
			} else {
				var dataInfo = resultJSON.data;
				// Ext.Msg.alert(tableData);
				Ext.MessageBox.show({
					msg : dataInfo,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
		}
	});

};

// 菜谱
function orderedMenuOnLoad() {
	var Request = new URLParaQuery();
	Ext.Ajax
			.request({
				url : "../../QueryMenu.do",
				params : {
					"pin" : pin,
					"type" : "1"
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					if (resultJSON.success == true) {
						var josnData = resultJSON.data;
						var menuList = josnData.split("，");
						for ( var i = 0; i < menuList.length; i++) {
							var menuInfo = menuList[i].substr(1,
									menuList[i].length - 2).split(",");
							// 格式：[菜名，菜名编号，菜名拼音，单价，厨房编号,特,荐,停,送,時]
							// 后台格式：[厨房编号,"菜品名称",菜品编号,"菜品拼音","￥菜品单价",特,荐,停,送,時]
							// 前后台格式有差异，厨房编号前台存储放在最后一位
							dishesDisplayData.push([
									menuInfo[1].substr(1,
											menuInfo[1].length - 2),// 菜名
									menuInfo[2],// 菜名编号
									menuInfo[3].substr(1,
											menuInfo[3].length - 2),// 菜名拼音
									menuInfo[4].substr(1,
											menuInfo[4].length - 2),// 单价
									menuInfo[0], // 厨房编号
									menuInfo[5], // 特
									menuInfo[6], // 荐
									menuInfo[7], // 停
									menuInfo[8], // 送
									menuInfo[9] // 時
							]);
						}
						// 根据“特荐停”重新写菜名
						for ( var i = 0; i < dishesDisplayData.length; i++) {
							if (dishesDisplayData[i][5] == "true") {
								// 特
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../../images/icon_tip_te.png'></img>";
							}
							if (dishesDisplayData[i][6] == "true") {
								// 荐
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../../images/icon_tip_jian.png'></img>";
							}
							if (dishesDisplayData[i][7] == "true") {
								// 停
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../../images/icon_tip_ting.png'></img>";
							}
							if (dishesDisplayData[i][8] == "true") {
								// 送
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../../images/forFree.png'></img>";
							}
							if (dishesDisplayData[i][9] == "true") {
								// 時
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../../images/currPrice.png'></img>";
							}
						}
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
									dishesDisplayData[i][9] ]);
						}
						dishesDisplayStore.reload();
					}
				},
				failure : function(response, options) {
				}
			});
};

// 口味
// dishTasteData.push([ "咸死你", "￥8" ]);
// 后台：[口味编号,口味分类,口味名称,价钱,比例,计算方式]
// “口味分类”的值如下： 0 - 口味 ， 1 - 做法， 2 - 规格
// “计算方式”的值如下：0 - 按价格，1 - 按比例
// 前台：[口味编号,口味分类,口味名称,价钱,比例,计算方式，计算方式显示，选择]
function tasteOnLoad() {
	var Request = new URLParaQuery();
	Ext.Ajax.request({
		url : "../../QueryMenu.do",
		params : {
			"pin" : Request["pin"],
			"type" : "2"
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				var josnData = resultJSON.data;
				var tasteList = josnData.split("，");
				for ( var i = 0; i < tasteList.length; i++) {
					var tasteInfo = tasteList[i].substr(1,
							tasteList[i].length - 2).split(",");
					var countTypeDescr;
					if (tasteInfo[5] == "0") {
						countTypeDescr = "按价格";
					} else {
						countTypeDescr = "按比例";
					}

					if (tasteInfo[1] == "0") {
						dishTasteDataTas.push([ tasteInfo[0], // 口味编号
						tasteInfo[1], // 口味分类
						tasteInfo[2], // 口味名称
						tasteInfo[3], // 价钱
						tasteInfo[4], // 比例
						tasteInfo[5], // 计算方式
						countTypeDescr, // 计算方式显示
						false // 选择
						]);
					} else if (tasteInfo[1] == "1") {
						dishTasteDataPar.push([ tasteInfo[0], // 口味编号
						tasteInfo[1], // 口味分类
						tasteInfo[2], // 口味名称
						tasteInfo[3], // 价钱
						tasteInfo[4], // 比例
						tasteInfo[5], // 计算方式
						countTypeDescr, // 计算方式显示
						false // 选择
						]);
					} else {
						dishTasteDataSiz.push([ tasteInfo[0], // 口味编号
						tasteInfo[1], // 口味分类
						tasteInfo[2], // 口味名称
						tasteInfo[3], // 价钱
						tasteInfo[4], // 比例
						tasteInfo[5], // 计算方式
						countTypeDescr, // 计算方式显示
						false // 选择
						]);
					}

				}
				dishTasteStoreTas.reload();
				dishTasteStorePar.reload();
				dishTasteStoreSiz.reload();
			}
		},
		failure : function(response, options) {
		}
	});
};