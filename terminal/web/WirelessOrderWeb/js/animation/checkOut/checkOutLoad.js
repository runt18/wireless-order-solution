var Request = new URLParaQuery();

// on page load function
function checkOutOnLoad() {

	// 1,update table status
	restaurantID = Request["restaurantID"];

	var tableNum = Request["tableNbr"];
	var persCount = Request["personCount"];
	document.getElementById("tblNbrDivTS").innerHTML = tableNum
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("perCountDivTS").innerHTML = persCount
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("minCostDivTS").innerHTML = Request["minCost"]
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	if (Request["minCost"] == "0.0") {
		document.getElementById("minCostDivTS").style["visibility"] = "hidden";
		document.getElementById("minCostImgTS").style["visibility"] = "hidden";
	}

	// 2,get the ordered dishes and discount
	// checkOutData [ 厨房编号,"菜名", "口味", 数量, "单价",特,荐,停,送 ]
	// 后台已点菜式 ["菜名",菜名编号,厨房编号,"口味",口味编号,数量,单价,特,荐,停,送]
	// discountData [厨房编号,一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	// 后台折扣率 [厨房编号,"厨房名称",一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	// checkOutDataDisplay ["菜名", "口味", 数量, "单价" ,"折扣率","实价",特,荐,停,送]
	Ext.Ajax
			.request({
				url : "../QueryOrder.do",
				params : {
					"pin" : Request["pin"],
					"tableID" : Request["tableNbr"]
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					if (resultJSON.success == true) {
						// 1,获取已点菜式
						var josnData = resultJSON.data;
						var orderList = josnData.split("，");
						for ( var i = 0; i < orderList.length; i++) {

							var orderInfo = orderList[i].substr(1,
									orderList[i].length - 2).split(",");
							checkOutData.push([ orderInfo[2],// 厨房编号
							orderInfo[0].substr(1, orderInfo[0].length - 2), // 菜名
							orderInfo[3].substr(1, orderInfo[3].length - 2),// 口味
							orderInfo[5],// 数量
							orderInfo[6].substr(1, orderInfo[6].length - 2), // 单价
							orderInfo[7],// 特
							orderInfo[8],// 荐
							orderInfo[9], // 停
							orderInfo[10] // 送
							]);
						}

						// 2,获取折扣率
						Ext.Ajax
								.request({
									url : "../QueryMenu.do",
									params : {
										"pin" : Request["pin"],
										"type" : "3"
									},
									success : function(response, options) {
										var resultJSON = Ext.util.JSON
												.decode(response.responseText);
										if (resultJSON.success == true) {
											var discountJSONData = resultJSON.data;
											var discountList = discountJSONData
													.split("，");
											for ( var i = 0; i < discountList.length; i++) {
												var discountInfo = discountList[i]
														.substr(
																1,
																discountList[i].length - 2)
														.split(",");
												discountData.push([
														discountInfo[0], // 厨房编号
														discountInfo[2],// 一般折扣1
														discountInfo[3],// 一般折扣2
														discountInfo[4],// 一般折扣3
														discountInfo[5],// 会员折扣1
														discountInfo[6],// 会员折扣2
														discountInfo[7] // 会员折扣3
												]);
											}

											// 3,请求口味
											Ext.Ajax
													.request({
														url : "../QueryMenu.do",
														params : {
															"pin" : Request["pin"],
															"type" : "2"
														},
														success : function(
																response,
																options) {
															var resultTasteJSON = Ext.util.JSON
																	.decode(response.responseText);
															if (resultTasteJSON.success == true) {
																var josnTasteData = resultTasteJSON.data;
																var tasteList = josnTasteData
																		.split("，");

																for ( var i = 0; i < tasteList.length; i++) {
																	var tasteInfo = tasteList[i]
																			.substr(
																					1,
																					tasteList[i].length - 2)
																			.split(
																					",");
																	// 后台格式：[1,"加辣","￥2.50"]，[2,"少盐","￥0.00"]，[3,"少辣","￥5.00"]
																	// 前后台格式有差异，口味编号前台存储放在最后一位
																	dishTasteData
																			.push([
																					tasteInfo[1]
																							.substr(
																									1,
																									tasteInfo[1].length - 2), // 口味
																					tasteInfo[2]
																							.substr(
																									1,
																									tasteInfo[2].length - 2), // 价钱
																					tasteInfo[0] // 口味编号
																			]);
																}

																// 4,显示
																for ( var i = 0; i < checkOutData.length; i++) {
																	var KitchenNum = checkOutData[i][0];
																	var discountRate = 1;
																	for ( var j = 0; j < discountData.length; j++) {
																		if (KitchenNum == discountData[j][0]) {
																			// 默认“一般”的“折扣1”
																			discountRate = discountData[j][1];
																		}
																	}

																	var tastePrice = 0;
																	for ( var j = 0; j < dishTasteData.length; j++) {
																		if (dishTasteData[j][0] == checkOutData[i][2]) {
																			tastePrice = parseFloat(dishTasteData[j][1]
																					.substr(
																							1,
																							dishTasteData[j][1].length - 1));
																		}
																	}

																	// 总价 = （原料价
																	// * 折扣率 +
																	// 口味价）* 数量
																	if (checkOutData[i][5] == "true"
																			|| checkOutData[i][8] == "true") {
																		// 特价，不打折
																		var price = parseFloat(checkOutData[i][4]
																				.substring(1))
																				* checkOutData[i][3];
																	} else {
																		// 非特价
																		var price = ((parseFloat(checkOutData[i][4]
																				.substring(1)) - tastePrice)
																				* discountRate + tastePrice)
																				* checkOutData[i][3];
																	}
																	var priceDisplay = checkOutData[i][4]
																			.substring(
																					0,
																					1)
																			+ price
																					.toFixed(2);

																	// 送 -- 折扣率
																	// --1
																	if (checkOutData[i][8] == "true"
																			|| checkOutData[i][5] == "true") {
																		checkOutDataDisplay
																				.push([
																						checkOutData[i][1],// 菜名
																						checkOutData[i][2],// 口味
																						checkOutData[i][3],// 数量
																						checkOutData[i][4],// 单价
																						parseFloat(
																								"1")
																								.toFixed(
																										2),// 折扣率
																						priceDisplay, // 实价
																						checkOutData[i][5],// 特
																						checkOutData[i][6],// 荐
																						checkOutData[i][7], // 停
																						checkOutData[i][8] // 送
																				]);
																	} else {
																		checkOutDataDisplay
																				.push([
																						checkOutData[i][1],// 菜名
																						checkOutData[i][2],// 口味
																						checkOutData[i][3],// 数量
																						checkOutData[i][4],// 单价
																						parseFloat(
																								discountRate)
																								.toFixed(
																										2),// 折扣率
																						priceDisplay, // 实价
																						checkOutData[i][5],// 特
																						checkOutData[i][6],// 荐
																						checkOutData[i][7], // 停
																						checkOutData[i][8] // 送
																				]);
																	}
																}
																// 根据“特荐停”重新写菜名
																for ( var i = 0; i < checkOutDataDisplay.length; i++) {
																	if (checkOutDataDisplay[i][6] == "true") {
																		// 特
																		checkOutDataDisplay[i][0] = checkOutDataDisplay[i][0]
																				+ "<img src='../images/icon_tip_te.gif'></img>";
																	}
																	if (checkOutDataDisplay[i][7] == "true") {
																		// 荐
																		checkOutDataDisplay[i][0] = checkOutDataDisplay[i][0]
																				+ "<img src='../images/icon_tip_jian.gif'></img>";
																	}
																	if (checkOutDataDisplay[i][8] == "true") {
																		// 停
																		checkOutDataDisplay[i][0] = checkOutDataDisplay[i][0]
																				+ "<img src='../images/icon_tip_ting.gif'></img>";
																	}
																	if (checkOutDataDisplay[i][9] == "true") {
																		// 送
																		checkOutDataDisplay[i][0] = checkOutDataDisplay[i][0]
																				+ "<img src='../images/forFree.png'></img>";
																	}
																}

																checkOutStore
																		.reload();

																// 4,算总价
																var totalCount = 0;
																var forFreeCount = 0;
																for ( var i = 0; i < checkOutDataDisplay.length; i++) {
																	var singleCount = parseFloat(checkOutDataDisplay[i][5]
																			.substr(1));
																	if (checkOutDataDisplay[i][9] == "true") {
																		// forFreeCount
																		// =
																		// forFreeCount
																		// +
																		// singleCount;

																		// for
																		// free
																		// count
																		// dont
																		// need
																		// discount
																		forFreeCount = forFreeCount
																				+ parseFloat(checkOutData[i][4]
																						.substring(1))
																				* checkOutData[i][3];
																	} else {
																		totalCount = totalCount
																				+ singleCount;
																	}
																}
																totalCount = totalCount
																		.toFixed(2);
																forFreeCount = forFreeCount
																		.toFixed(2);
																originalTotalCount = totalCount;
																document
																		.getElementById("totalCount").innerHTML = totalCount;
																document
																		.getElementById("forFree").innerHTML = forFreeCount;
																// document
																// .getElementById("actualCount").value
																// = "0.00";
																document
																		.getElementById("shouldPay").innerHTML = totalCount;

																// 4,（尾数处理）
																// 后台：["餐厅名称","餐厅信息","电话1","电话2","地址",$(尾数处理),$(自动补打)]
																// 前台：restaurantData
																// ，格式一样
																Ext.Ajax
																		.request({
																			url : "../QueryRestaurant.do",
																			params : {
																				"restaurantID" : restaurantID
																			},
																			success : function(
																					response,
																					options) {
																				var resultJSON = Ext.util.JSON
																						.decode(response.responseText);
																				if (resultJSON.success == true) {
																					var dataInfo = resultJSON.data;
																					var restaurantInfo = dataInfo
																							.split(",");
																					restaurantData
																							.push([
																									restaurantInfo[0]
																											.substr(
																													1,
																													restaurantInfo[0].length - 2),// 餐厅名称
																									restaurantInfo[1]
																											.substr(
																													1,
																													restaurantInfo[1].length - 2),// 餐厅信息
																									restaurantInfo[2]
																											.substr(
																													1,
																													restaurantInfo[2].length - 2),// 电话1
																									restaurantInfo[3]
																											.substr(
																													1,
																													restaurantInfo[3].length - 2),// 电话2
																									restaurantInfo[4]
																											.substr(
																													1,
																													restaurantInfo[4].length - 2),// 地址
																									restaurantInfo[5],// 尾数处理
																									restaurantInfo[6] // 自动补打
																							]);

																					var sPay = document
																							.getElementById("shouldPay").innerHTML;

																					// 5,最低消费处理
																					var minCost = Request["minCost"];
																					if (parseFloat(minCost) > parseFloat(sPay)) {
																						sPay = minCost;
																						Ext.MessageBox
																								.show({
																									msg : "消费额小于最低消费额，是否继续结帐？",
																									width : 300,
																									buttons : Ext.MessageBox.YESNO,
																									fn : function(
																											btn) {
																										if (btn == "no") {
																											location.href = "TableSelect.html?pin="
																													+ Request["pin"]
																													+ "&restaurantID="
																													+ restaurantID;
																										}
																									}
																								});
																					}

																					// 6,尾数处理
																					if (restaurantData[0][5] == 1) {
																						sPay = sPay
																								.substr(
																										0,
																										sPay
																												.indexOf("."))
																								+ ".00";
																					} else if (restaurantData[0][5] == 2) {
																						sPay = parseFloat(
																								sPay)
																								.toFixed(
																										0)
																								+ ".00";
																					}
																					document
																							.getElementById("shouldPay").innerHTML = sPay;
																					document
																							.getElementById("actualCount").value = sPay;
																					document
																							.getElementById("change").innerHTML = "0.00";
																				} else {
																					var dataInfo = resultJSON.data;
																					Ext.MessageBox
																							.show({
																								msg : dataInfo,
																								width : 300,
																								buttons : Ext.MessageBox.OK
																							});
																				}
																			},
																			failure : function(
																					response,
																					options) {
																			}
																		});

															} else {
																var dataTasteInfo = resultTasteJSON.data;
																Ext.MessageBox
																		.show({
																			msg : dataTasteInfo,
																			width : 300,
																			buttons : Ext.MessageBox.OK
																		});
															}
														},
														failure : function(
																response,
																options) {
														}
													});

										} else {
											var dataInfo = resultJSON.data;
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

function moneyCount(opt) {
	var shouldPay = document.getElementById("shouldPay").innerHTML;
	var actualPay = document.getElementById("actualCount").value;
	var minCost = Request["minCost"];
	var serviceRate = document.getElementById("serviceCharge").value;
	var totalCount = document.getElementById("totalCount").innerHTML;

	var totalCount_out = "0.00";
	var shouldPay_out = "0.00";
	var change_out = "0.00";

	if (restaurantData[0] != undefined) {
		// “应收”加上服务费
		if (parseFloat(totalCount) < parseFloat(minCost)) {
			shouldPay_out = parseFloat(minCost)
					* (1 + parseFloat(serviceRate) / 100);
		} else {
			shouldPay_out = parseFloat(originalTotalCount)
					* (1 + parseFloat(serviceRate) / 100);
		}

		// “应收”尾数处理
		if (restaurantData[0][5] == 1) {
			if ((shouldPay_out + "").indexOf(".") != -1) {
				shouldPay_out = (shouldPay_out + "").substr(0,
						(shouldPay_out + "").indexOf("."))
						+ ".00";
			} else {
				shouldPay_out = shouldPay_out + ".00";
			}
		} else if (restaurantData[0][5] == 2) {
			shouldPay_out = parseFloat(shouldPay_out).toFixed(0) + ".00";
		} else {
			shouldPay_out = parseFloat(shouldPay_out).toFixed(2);
		}

		// “合计”加上服务费
		totalCount_out = (parseFloat(originalTotalCount) * (1 + parseFloat(serviceRate) / 100))
				.toFixed(2);

		// “找零”计算
		if (actualPay != "" && actualPay != "0.00") {
			if (opt == "button") {
				change_out = (parseFloat(actualPay) - parseFloat(shouldPay_out))
						.toFixed(2);
			} else {
				change_out = "0.00";
			}
		}

		document.getElementById("totalCount").innerHTML = totalCount_out;
		document.getElementById("shouldPay").innerHTML = shouldPay_out;
		document.getElementById("change").innerHTML = change_out;
		if (opt == "button") {
		} else {
			document.getElementById("actualCount").value = shouldPay_out;
		}
	}
};