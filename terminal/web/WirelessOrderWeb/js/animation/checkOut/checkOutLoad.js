// 从url获取当前桌信息
function URLParaQuery() {
	var name, value, i;
	var str = location.href;
	var num = str.indexOf("?")
	str = str.substr(num + 1);
	var arrtmp = str.split("&");
	for (i = 0; i < arrtmp.length; i++) {
		num = arrtmp[i].indexOf("=");
		if (num > 0) {
			name = arrtmp[i].substring(0, num);
			value = arrtmp[i].substr(num + 1);
			this[name] = value;
		}
	}
}

// on page load function
function checkOutOnLoad() {

	// 1,update table status
	var Request = new URLParaQuery();
	var tableNum = Request["tableNbr"];
	var persCount = Request["personCount"];
	document.getElementById("tblNbrDivTS").innerHTML = tableNum
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("perCountDivTS").innerHTML = persCount
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

	// 2,get the ordered dishes and discount
	// checkOutData [ 厨房编号,"菜名", "口味", 数量, "单价" ]
	// 后台已点菜式 ["菜名",菜名编号,厨房编号,"口味",口味编号,数量,单价]
	// discountData [厨房编号,一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	// 后台折扣率 [厨房编号,"厨房名称",一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	// checkOutDataDisplay ["菜名", "口味", 数量, "单价" ,"折扣率","实价"]
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
							orderInfo[6].substr(1, orderInfo[6].length - 2) // 单价
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

											// 3,显示
											for ( var i = 0; i < checkOutData.length; i++) {
												var KitchenNum = checkOutData[i][0];
												var discountRate = 1;
												for ( var j = 0; j < discountData.length; j++) {
													if (KitchenNum == discountData[j][0]) {
														// 默认“一般”的“折扣1”
														discountRate = discountData[j][1];
													}
												}

												var price = parseFloat(checkOutData[i][4]
														.substring(1))
														* discountRate
														* checkOutData[i][3];
												var priceDisplay = checkOutData[i][4]
														.substring(0, 1)
														+ price.toFixed(2);

												checkOutDataDisplay.push([
														checkOutData[i][1],
														checkOutData[i][2],
														checkOutData[i][3],
														checkOutData[i][4],
														discountRate,
														priceDisplay ]);
											}

											checkOutStore.reload();

											// 4,算总价
											var totalCount = 0;
											for ( var i = 0; i < checkOutDataDisplay.length; i++) {
												var singleCount = parseFloat(checkOutDataDisplay[i][5]
														.substr(1));
												totalCount = totalCount
														+ singleCount;
											}
											totalCount = totalCount.toFixed(2);
											document
													.getElementById("totalCount").innerHTML = "<div style='font-size:18px;font-weight:bold;'>合计：       ￥"
													+ totalCount + "</div>";
											checkOutForm
													.findById("actualCount")
													.setValue(totalCount);

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