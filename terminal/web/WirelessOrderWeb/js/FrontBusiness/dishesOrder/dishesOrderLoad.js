//刪退菜處理函數
function dishOptTasteHandler(rowIndex) {
	if (dishOrderCurrRowIndex_ != -1) {

		if (orderedData[rowIndex][18] == "1") {
			Ext.MessageBox.show({
				msg : "已点菜品不能修改口味",
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		} else if (orderedData[rowIndex][20] == "true") {
			Ext.MessageBox.show({
				msg : "临时菜不支持口味选择",
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		} else {
			dishOrderCurrRowIndex_ = rowIndex;
			dishTasteWindow.show();
		}
	}
};

function dishGridRefresh() {

	if (orderedData.length != 0) {
		// 底色处理，已点菜式原色底色
		for ( var i = 0; i < orderedData.length; i++) {
			if (orderedData[i][18] == "1") {
				orderedGrid.getView().getRow(i).style.backgroundColor = "#FFFF93";
			} else if (orderedData[i][18] == "2") {
				orderedGrid.getView().getRow(i).style.backgroundColor = "#FFE4CA";
			} else {

			}
		}

		// 底色处理，已点菜式原色底色
		for ( var i = 0; i < orderedData.length; i++) {
			if (orderedData[i][18] == "1") {
				document.getElementById("tasteLink" + i).onclick = function() {
					Ext.MessageBox.show({
						msg : "已点菜品不能修改口味",
						width : 300,
						buttons : Ext.MessageBox.OK
					});
					return false;
				};
			} else if (orderedData[i][18] == "2") {
				// document.getElementById("tasteLink" + i).onclick =
				// dishOptTasteHandler(dishOrderCurrRowIndex_);
			} else {

			}
		}
	}
};

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

// on page load function
function dishNbrOnLoad() {
	
	// keyboard input dish number
	$("#orderNbr").bind("keyup", function() {
		dishKeyboardSelect("orderNbr");
	});
};

function dishSpellOnLoad() {
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
										dishesDisplayData[i][8],
										dishesDisplayData[i][9] ]);
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
											dishesDisplayData[i][8],
											dishesDisplayData[i][9] ]);
								}
							}
						}
						dishesDisplayStore.reload();
					});
}

function tableStuLoad() {
	
	var Request = new URLParaQuery();
	if(Request["category"] == CATE_TAKE_OUT){
		orderedForm.buttons[1].setDisabled(true);
	}
	
	
	
	// update table status
	var Request = new URLParaQuery();

	// 对"拼台""外卖"，台号特殊处理
	var tableNbr = "000";
	if (category == "4") {
		dishesOrderNorthPanel.findById("tableNbrFrom").setWidth(140);
		tableNbr = Request["tableNbr"] + "，" + Request["tableNbr2"];
	} else if (category == "2" && Request["tableStat"] == "free") {
		tableNbr = "外卖";
	} else {
		tableNbr = Request["tableNbr"];
	}

	var personCount = Request["personCount"];

	document.getElementById("tblNbrDivTS").innerHTML = tableNbr;
	// + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	dishesOrderNorthPanel.findById("tablePersonCount").setValue(personCount);
	document.getElementById("minCostDivTS").innerHTML = Request["minCost"];
	// +"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	if (Request["minCost"] == "0") {
		document.getElementById("minCostDivTS").style["display"] = "none";
		document.getElementById("minCostImgTS").style["display"] = "none";
		// document.getElementById("minCostPlaceHolder").style["visibility"] =
		// "hidden";
	}
	document.getElementById("serviceRateDivTS").innerHTML = (Request["serviceRate"] * 100)
			+ "%";
	// + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	if (Request["serviceRate"] == "0") {
		document.getElementById("serviceRateDivTS").style["display"] = "none";
		document.getElementById("serviceRateImgTS").style["display"] = "none";
	}

	// update label new or mod
	var Request = new URLParaQuery();
	var status = Request["tableStat"];
	if (status == "free") {
		dishesOrderNorthPanel
				.setTitle("<div style='font-size:18px;padding-left:2px'>新下单<div>");
	} else {
		dishesOrderNorthPanel
				.setTitle("<div style='font-size:18px;padding-left:2px'>改单<div>");
		// ext-gen338
		// document.getElementById("ext-gen338").alt = "退菜";

	}

	// update the operator name
	getOperatorName(Request["pin"], "../../");
};

// 以点菜式数据
// 菜品状态: 1：已点，2：新点，3：修改
// 格式：[菜名，口味，数量，￥单价，操作，￥实价，菜名编号，厨房编号，口味编号1,特,荐,停,送,￥口味价钱,口味编号2,口味编号3,￥口味价钱,菜品状态,時,是否临时菜,菜名ORIG]
// orderedData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "", "￥56.2" ]);
// orderedData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "", "￥56.2" ]);
// 后台：["菜名",菜名编号,厨房编号,"口味",口味编号,数量,单价,是否特价,是否推荐,是否停售,是否赠送,折扣率,口味编号2,
// 口味编号3,口味价钱,是否时价,是否临时菜]
function orderedDishesOnLoad() {
	var Request = new URLParaQuery();
	// 外卖不查询已点菜式
	if (category == "2" && Request["tableStat"] == "free") {
	} else {
		Ext.Ajax
				.request({
					url : "../../QueryOrder.do",
					params : {
						"pin" : Request["pin"],
						"tableID" : Request["tableNbr"]
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
									// 实价 = 单价 + 口味价钱								
									var singlePrice = parseFloat(orderInfo[6]
											.substr(2, orderInfo[6].length - 3));
									var tastePrice = parseFloat(orderInfo[14]
											.substr(2, orderInfo[14].length - 3));
									var acturalPrice = 0.0;
									acturalPrice = singlePrice + tastePrice;
									acturalPrice = "￥"
											+ acturalPrice.toFixed(1);
									orderedData.push([
											orderInfo[0].substr(1,orderInfo[0].length - 2), // 菜名
											orderInfo[3].substr(1,orderInfo[3].length - 2),// 口味
											orderInfo[5],// 数量
											orderInfo[6].substr(1,orderInfo[6].length - 2),// 单价											
											"",// 操作
											acturalPrice,// 实价											
											orderInfo[19],// 时间
											orderInfo[20],// 服务员
											orderInfo[1],// 菜名编号
											orderInfo[2],// 厨房编号
											orderInfo[4], // 口味编号1
											orderInfo[7],// 特
											orderInfo[8],// 荐
											orderInfo[9], // 停
											orderInfo[10], // 送
											tastePrice,// 口味价钱
											orderInfo[12],// 口味编号2
											orderInfo[13], // 口味编号3
											"1",// 菜品状态
											orderInfo[15], // 時
											orderInfo[16], // 是否临时菜
											orderInfo[0].substr(1,orderInfo[0].length - 2), // 菜名ORIG
											orderInfo[21],// 是否临时口味
											orderInfo[22],// 临时口味
											orderInfo[23],// 临时口味价钱
											orderInfo[24] // 临时口味编号
									]);
								}

								// 根据“特荐停”重新写菜名
								for ( var i = 0; i < orderedData.length; i++) {
									if (orderedData[i][11] == "true") {
										// 特
										orderedData[i][0] = orderedData[i][0]
												+ "<img src='../../images/icon_tip_te.gif'></img>";
									}
									if (orderedData[i][12] == "true") {
										// 荐
										orderedData[i][0] = orderedData[i][0]
												+ "<img src='../../images/icon_tip_jian.gif'></img>";
									}
									if (orderedData[i][13] == "true") {
										// 停
										orderedData[i][0] = orderedData[i][0]
												+ "<img src='../../images/icon_tip_ting.gif'></img>";
									}
									if (orderedData[i][14] == "true") {
										// 送
										orderedData[i][0] = orderedData[i][0]
												+ "<img src='../../images/forFree.png'></img>";
									}
									if (orderedData[i][19] == "true") {
										// 時
										orderedData[i][0] = orderedData[i][0]
												+ "<img src='../../images/currPrice.png'></img>";
									}
									if (orderedData[i][20] == "true") {
										// 臨
										orderedData[i][0] = orderedData[i][0]
												+ "<img src='../../images/tempDish.png'></img>";
									}
								}

								// “并台”特殊处理，如果并台+新下单，清空已点菜式
								if (Request["category"] == "3"
										&& Request["tableStat"] == "free") {
									orderedData.length = 0;
								}

								orderedStore.reload();
								// 底色处理，已点菜式原色底色
								dishGridRefresh();
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
	}

	// upate the tool bar
	if (Request["tableStat"] == "free") {
		// orderedGrid.getTopToolbar().addSeparator();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addItem(countAddImgBut);
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addItem(countMinusImgBut);
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addSpacer();
		// orderedGrid.getTopToolbar().addItem(countEqualImgBut);

	} else {
		orderedGrid.getTopToolbar().addSeparator();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addItem(printTotalImgBut);
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addSpacer();
		orderedGrid.getTopToolbar().addItem(printDetailImgBut);
	}
};

// 菜谱
function orderedMenuOnLoad() {
	var Request = new URLParaQuery();
	Ext.Ajax
			.request({
				url : "../../QueryMenu.do",
				params : {
					"pin" : Request["pin"],
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
										+ "<img src='../../images/icon_tip_te.gif'></img>";
							}
							if (dishesDisplayData[i][6] == "true") {
								// 荐
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../../images/icon_tip_jian.gif'></img>";
							}
							if (dishesDisplayData[i][7] == "true") {
								// 停
								dishesDisplayData[i][0] = dishesDisplayData[i][0]
										+ "<img src='../../images/icon_tip_ting.gif'></img>";
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