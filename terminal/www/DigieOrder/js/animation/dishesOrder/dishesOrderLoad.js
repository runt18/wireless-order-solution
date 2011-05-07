// keyboard select handler
var dishKeyboardSelect = function(relateItemId) {
	if (relateItemId == "orderNbr") {
		var curDishNbr = Ext.getCmp("orderNbr").getValue() + "";

		if (curDishNbr == "") {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				dishesDisplayDataShow.push( [ dishesDisplayData[i][0],
						dishesDisplayData[i][1], dishesDisplayData[i][2],
						dishesDisplayData[i][3] ]);
			}
		} else {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				if ((dishesDisplayData[i][1] + "").substring(0,
						curDishNbr.length) == curDishNbr) {
					dishesDisplayDataShow.push( [ dishesDisplayData[i][0],
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
						dishesDisplayDataShow.push( [ dishesDisplayData[i][0],
								dishesDisplayData[i][1],
								dishesDisplayData[i][2],
								dishesDisplayData[i][3] ]);
					}
				} else {
					dishesDisplayDataShow.length = 0;
					for ( var i = 0; i < dishesDisplayData.length; i++) {
						if ((dishesDisplayData[i][2] + "").substring(0,
								curDishSpell.length) == curDishSpell) {
							dishesDisplayDataShow.push( [
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
	var Request = new URLParaQuery();
	var tableNbr = Request["tableNbr"];
	var personCount = Request["personCount"];
	document.getElementById("tblNbrDivTS").innerHTML = tableNbr
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("perCountDivTS").innerHTML = personCount
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
};

// 以点菜式数据
// 格式：[菜名，口味，数量，单价，操作，实价]
orderedData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "", "￥56.2" ]);
orderedData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "", "￥56.2" ]);
function orderedDishesOnLoad() {
	var Request = new URLParaQuery();
	Ext.Ajax.request( {
		url : "../findMutilTableCol.do",
		params : {
			"pin" : Request["pin"],
			"tableID" : Request["tableNbr"]
		},
		success : function(response, options) {
			//Ext.Msg.alert("", "<b>删单成功！</b>");
	},
	failure : function(response, options) {
	}
	});
};