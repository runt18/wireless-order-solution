﻿// keyboard select handler
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
						dishesDisplayData[i][7], dishesDisplayData[i][8] ]);
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
							dishesDisplayData[i][7], dishesDisplayData[i][8] ]);
				}
			}
		}

		dishesDisplayStore.reload();
	}
};

// on page load function
function billModifyOnLoad() {

	// update the operator name
	getOperatorName(pin);

	// keyboard input dish number
	$("#orderNbr").bind("keyup", function() {
		dishKeyboardSelect("orderNbr");
	});

	// keyboard input dish spell
	$("#orderSpell")
			.bind(
					"keyup",
					function() {
						var curDishSpell = Ext.getCmp("orderSpell").getValue()
								.toUpperCase()
								+ "";
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
										dishesDisplayData[i][8] ]);
							}
						} else {
							dishesDisplayDataShow.length = 0;
							for ( var i = 0; i < dishesDisplayData.length; i++) {
								if ((dishesDisplayData[i][2] + "").substring(0,
										curDishSpell.length).toUpperCase() == curDishSpell) {
									dishesDisplayDataShow.push([
											dishesDisplayData[i][0],
											dishesDisplayData[i][1],
											dishesDisplayData[i][2],
											dishesDisplayData[i][3],
											dishesDisplayData[i][4],
											dishesDisplayData[i][5],
											dishesDisplayData[i][6],
											dishesDisplayData[i][7],
											dishesDisplayData[i][8] ]);
								}
							}
						}
						dishesDisplayStore.reload();
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
	if (Request["minCost"] == "0.0") {
		document.getElementById("minCostDivTS").style["visibility"] = "hidden";
		document.getElementById("minCostImgTS").style["visibility"] = "hidden";
	}

	// update label new or mod
	// dishesOrderNorthPanel
	// .setTitle("<div style='font-size:18px;padding-left:2px'>帐单修改<div>");

	// 已点菜式查询
	Ext.Ajax
			.request({
				url : "../QueryOrder.do",
				params : {
					"pin" : pin,
					"orderID" : Request["orderID"]
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					if (resultJSON.success == true) {
						if (resultJSON.data != "NULL") {
							var josnData = resultJSON.data;
							var orderList = josnData.split("，");
							for ( var i = 0; i < orderList.length; i++) {
								var orderInfo = orderList[i].substr(1,
										orderList[i].length - 2).split(",");
								orderedData.push([
										orderInfo[0].substr(1,
												orderInfo[0].length - 2), // 菜名
										orderInfo[3].substr(1,
												orderInfo[3].length - 2),// 口味
										orderInfo[5],// 数量
										orderInfo[6].substr(1,
												orderInfo[6].length - 2),// 单价
										"",// 操作
										orderInfo[6].substr(1,
												orderInfo[6].length - 2),// 实价
										orderInfo[1],// 菜名编号
										orderInfo[2],// 厨房编号
										orderInfo[4], // 口味编号
										orderInfo[7],// 特
										orderInfo[8],// 荐
										orderInfo[9], // 停
										orderInfo[10], // 送
										orderInfo[11] // 折扣率
								]);
							}

							// 根据“特荐停”重新写菜名
							for ( var i = 0; i < orderedData.length; i++) {
								if (orderedData[i][9] == "true") {
									// 特
									orderedData[i][0] = orderedData[i][0]
											+ "<img src='../images/icon_tip_te.gif'></img>";
								}
								if (orderedData[i][10] == "true") {
									// 荐
									orderedData[i][0] = orderedData[i][0]
											+ "<img src='../images/icon_tip_jian.gif'></img>";
								}
								if (orderedData[i][11] == "true") {
									// 停
									orderedData[i][0] = orderedData[i][0]
											+ "<img src='../images/icon_tip_ting.gif'></img>";
								}
								if (orderedData[i][12] == "true") {
									// 送
									orderedData[i][0] = orderedData[i][0]
											+ "<img src='../images/forFree.png'></img>";
								}
							}

							// // “并台”特殊处理，如果并台+新下单，清空已点菜式
							// if (Request["category"] == "3"
							// && Request["tableStat"] == "free") {
							// orderedData.length = 0;
							// }

							orderedStore.reload();
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

	// 当前帐单信息查询
	Ext.Ajax
			.request({
				url : "../QueryToday.do",
				params : {
					"pin" : pin,
					"type" : 0,
					"ope" : 1,
					"value" : ""
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
							// 格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型"]
							// 后台格式：["账单号","台号","日期","类型","结帐方式","金额","实收","台号2","就餐人数","最低消","服务费率","会员编号","会员姓名","账单备注","赠券金额","结帐类型","折扣类型"]
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
		url : "../QueryMenu.do",
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
					discountInfo[2],// 一般折扣1
					discountInfo[3],// 一般折扣2
					discountInfo[4],// 一般折扣3
					discountInfo[5],// 会员折扣1
					discountInfo[6],// 会员折扣2
					discountInfo[7] // 会员折扣3
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
				url : "../QueryMenu.do",
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
							// 格式：[菜名，菜名编号，菜名拼音，单价，厨房编号,特,荐,停,送]
							// 后台格式：[厨房编号,"菜品名称",菜品编号,"菜品拼音","￥菜品单价",特,荐,停,送]
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
									menuInfo[8] // 送
							]);
						}
						// 根据“特荐停”重新写菜名
						for ( var i = 0; i < dishesDisplayData.length; i++) {
							if (dishesDisplayData[i][5] == "true") {
								// 特
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../images/icon_tip_te.gif'></img>";
							}
							if (dishesDisplayData[i][6] == "true") {
								// 荐
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../images/icon_tip_jian.gif'></img>";
							}
							if (dishesDisplayData[i][7] == "true") {
								// 停
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../images/icon_tip_ting.gif'></img>";
							}
							if (dishesDisplayData[i][8] == "true") {
								// 送
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../images/forFree.png'></img>";
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
									dishesDisplayData[i][8] ]);
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
function tasteOnLoad() {
	var Request = new URLParaQuery();
	Ext.Ajax.request({
		url : "../QueryMenu.do",
		params : {
			"pin" : pin,
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
					// 后台格式：[1,"加辣","￥2.50"]，[2,"少盐","￥0.00"]，[3,"少辣","￥5.00"]
					// 前后台格式有差异，口味编号前台存储放在最后一位
					dishTasteData.push([
							tasteInfo[1].substr(1, tasteInfo[1].length - 2), // 口味
							tasteInfo[2].substr(1, tasteInfo[2].length - 2), // 价钱
							tasteInfo[0] // 口味编号
					]);
				}
				dishTasteStore.reload();
			}
		},
		failure : function(response, options) {
		}
	});
};