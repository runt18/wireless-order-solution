﻿function loadAllMaterial() {
	materialComboData = [];
	Ext.Ajax.request({
		url : "../../QueryMaterialMgr.do",
		params : {
			"pin" : pin,
			"type" : 0,
			"ope" : 1,
			"value" : "",
			"isPaging" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// 格式：[食材ID，食材別名，食材名稱]
			// 后台格式：[id 编号 名称 库存量 价格（￥） 预警阀值 危险阀值]
			var rootData = resultJSON.root;
			if (rootData.length != 0) {
				if (rootData[0].message == "normal") {
					for ( var i = 0; i < rootData.length; i++) {
						materialComboData.push([ rootData[i].materialID,
								rootData[i].materialName ]);
					}
					materialAddStore.loadData(materialComboData);
				} else {
					Ext.MessageBox.show({
						msg : rootData[0].message,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			}
		},
		failure : function(response, options) {
			Ext.MessageBox.show({
				msg : " Unknown page error ",
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		}
	});
}

function loadAllDishes() {
	dishMultSelectData = [];
	Ext.Ajax.request({
		url : "../../QueryMenu.do",
		params : {
			"pin" : pin,
			"type" : "1"
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				var josnData = resultJSON.data;
				var menuList = josnData.split("，");
				for ( var i = 0; i < menuList.length; i++) {
					var menuInfo = menuList[i]
							.substr(1, menuList[i].length - 2).split(",");
					// 格式：[菜品编号，菜品名称]
					// 后台格式：[厨房编号,"菜品名称",菜品编号,"菜品拼音","￥菜品单价",特,荐,停,送,時]
					dishMultSelectData.push([ menuInfo[2],// 菜名编号
					menuInfo[1].substr(1, menuInfo[1].length - 2) // 菜名
					]);
				}
			}
		},
		failure : function(response, options) {
		}
	});
}

// on page load function
function menuMgrOnLoad() {

	// update the operator name
	getOperatorName(pin, "../../");

	searchForm.remove("conditionText");
	operatorComb.setDisabled(true);

	// 獲取廚房信息，以便顯示廚房描述
	// 后台：[厨房编号,"厨房名称",一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	// 前台：kitchenData：[厨房编号,厨房名称]
	Ext.Ajax.request({
		url : "../../QueryMenu.do",
		params : {
			"pin" : Request["pin"],
			"type" : "3"
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				// get the kitchen data
				var josnData = resultJSON.data;
				var keichenList = josnData.split("，");
				for ( var i = 0; i < keichenList.length; i++) {
					var keichenInfo = keichenList[i].substr(1,
							keichenList[i].length - 2).split(",");
					kitchenTypeData.push([ keichenInfo[0],// 厨房编号
					keichenInfo[1].substr(1, keichenInfo[1].length - 2) // 厨房名称
					]);
				}
				kitchenTypeData.push([ 255, "空" ]);
				menuStore.reload();
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

	loadAllDishes();
	loadAllMaterial();
};
