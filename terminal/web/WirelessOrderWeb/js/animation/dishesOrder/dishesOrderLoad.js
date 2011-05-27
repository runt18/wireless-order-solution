// keyboard select handler
var dishKeyboardSelect = function(relateItemId) {
	if (relateItemId == "orderNbr") {
		var curDishNbr = Ext.getCmp("orderNbr").getValue() + "";

		if (curDishNbr == "") {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				dishesDisplayDataShow.push([ dishesDisplayData[i][0],
						dishesDisplayData[i][1], dishesDisplayData[i][2],
						dishesDisplayData[i][3] ]);
			}
		} else {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				if ((dishesDisplayData[i][1] + "").substring(0,
						curDishNbr.length) == curDishNbr) {
					dishesDisplayDataShow.push([ dishesDisplayData[i][0],
							dishesDisplayData[i][1], dishesDisplayData[i][2],
							dishesDisplayData[i][3] ]);
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
	$("#orderSpell").bind(
			"keyup",
			function() {
				var curDishSpell = Ext.getCmp("orderSpell").getValue()
						.toUpperCase()
						+ "";
				if (curDishSpell == "") {
					dishesDisplayDataShow.length = 0;
					for ( var i = 0; i < dishesDisplayData.length; i++) {
						dishesDisplayDataShow.push([ dishesDisplayData[i][0],
								dishesDisplayData[i][1],
								dishesDisplayData[i][2],
								dishesDisplayData[i][3] ]);
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
									dishesDisplayData[i][3] ]);
						}
					}
				}
				dishesDisplayStore.reload();
			});
}

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

function tableStuLoad() {
	// update table status
	var Request = new URLParaQuery();
	var tableNbr = Request["tableNbr"];
	var personCount = Request["personCount"];
	document.getElementById("tblNbrDivTS").innerHTML = tableNbr
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("perCountDivTS").innerHTML = personCount
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

	// update label new or mod
	var Request = new URLParaQuery();
	var status = Request["tableStat"];
	if (status == "free") {
		dishesOrderNorthPanel.setTitle("<div style='font-size:18px;padding-left:2px'>新下单<div>");
	} else {
		dishesOrderNorthPanel.setTitle("<div style='font-size:18px;padding-left:2px'>改单<div>");
	}
};

// 以点菜式数据
// 格式：[菜名，口味，数量，单价，操作，实价，菜名编号，厨房编号，口味编号]
// orderedData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "", "￥56.2" ]);
// orderedData.push([ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "", "￥56.2" ]);
// 后台：[菜名,菜名编号,厨房编号,口味,口味编号,数量,单价]
function orderedDishesOnLoad() {
	var Request = new URLParaQuery();
	Ext.Ajax.request({
		url : "../QueryOrder.do",
		params : {
			"pin" : Request["pin"],
			"tableID" : Request["tableNbr"]
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				if (resultJSON.data != "NULL") {
					var josnData = resultJSON.data;
					var orderList = josnData.split("，");
					for ( var i = 0; i < orderList.length; i++) {
						var orderInfo = orderList[i].substr(1,
								orderList[i].length - 2).split(",");
						orderedData
								.push([
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
										orderInfo[4] // 口味编号
								]);
					}
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
};

// 菜谱
function orderedMenuOnLoad() {
	var Request = new URLParaQuery();
	Ext.Ajax
			.request({
				url : "../QueryMenu.do",
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
							// 格式：[菜名，菜名编号，菜名拼音，单价，厨房编号]
							// 后台格式：[厨房编号,"菜品名称",菜品编号,"菜品拼音","￥菜品单价"]
							// 前后台格式有差异，厨房编号前台存储放在最后一位
							dishesDisplayData.push([
									menuInfo[1].substr(1,
											menuInfo[1].length - 2),// 菜名
									menuInfo[2],// 菜名编号
									menuInfo[3].substr(1,
											menuInfo[3].length - 2),// 菜名拼音
									menuInfo[4].substr(1,
											menuInfo[4].length - 2),// 单价
									menuInfo[0] // 厨房编号
							]);
						}
						for ( var i = 0; i < dishesDisplayData.length; i++) {
							dishesDisplayDataShow.push([
									dishesDisplayData[i][0],
									dishesDisplayData[i][1],
									dishesDisplayData[i][2],
									dishesDisplayData[i][3],
									dishesDisplayData[i][4] ]);
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