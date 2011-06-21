function kitchenSelectLoad() {
	var Request = new URLParaQuery();
	// 后台：[厨房编号,"厨房名称",一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	// 前台：kitchenData：[厨房编号,厨房名称]
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
						// get the kitchen data
						var josnData = resultJSON.data;
						var keichenList = josnData.split("，");
						for ( var i = 0; i < keichenList.length; i++) {
							var keichenInfo = keichenList[i].substr(1,
									keichenList[i].length - 2).split(",");
							kitchenData.push([ keichenInfo[0],// 厨房编号
							keichenInfo[1].substr(1, keichenInfo[1].length - 2) // 厨房名称
							]);
						}

						// update the button
						for ( var i = 0; i < kitchenData.length; i++) {
							document.getElementById("kitchen"
									+ kitchenData[i][0]).innerHTML = kitchenData[i][1];
							document.getElementById("kitchen"
									+ kitchenData[i][0]).title = kitchenData[i][1];
						}

						// bind the click function to the button
						$(".ketchenSelect")
								.each(
										function() {
											if ($(this).attr("title") != "") {
												$(this)
														.bind(
																"click",
																function() {

																	dishesDisplayDataShow.length = 0;
																	for ( var i = 0; i < dishesDisplayData.length; i++) {
																		if (dishesDisplayData[i][4] == $(
																				this)
																				.attr(
																						"id")
																				.substr(
																						7)) {
																			dishesDisplayDataShow
																					.push([
																							dishesDisplayData[i][0],
																							dishesDisplayData[i][1],
																							dishesDisplayData[i][2],
																							dishesDisplayData[i][3],
																							dishesDisplayData[i][4],
																							dishesDisplayData[i][5],
																							dishesDisplayData[i][6],
																							dishesDisplayData[i][7] ]);
																		}
																	}

																	dishesDisplayStore
																			.reload();
																});
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

}