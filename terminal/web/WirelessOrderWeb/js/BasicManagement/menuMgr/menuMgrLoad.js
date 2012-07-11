function loadAllMaterial() {
	materialComboData = [];
	Ext.Ajax
			.request({
				url : "../../QueryMaterialMgr.do",
				params : {
					"pin" : pin,
					"type" : 0,
					"ope" : 1,
					"value" : "",
					"isPaging" : false,
					"isWarning" : false,
					"isDanger" : false
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					// 格式：[食材ID，食材名稱,種類]
					// 后台格式：[id 编号 名称 库存量 价格（￥） 预警阀值 危险阀值 ,種類]
					var rootData = resultJSON.root;
					if (rootData.length != 0) {
						if (rootData[0].message == "normal") {
							for ( var i = 0; i < rootData.length; i++) {
								materialComboData.push([
										rootData[i].materialID,
										rootData[i].materialName,
										rootData[i].cateID ]);
							}
							for ( var i = 0; i < materialComboData.length; i++) {
								if (materialComboData[i][2] == materialComboData[0][2]) {
									materialComboDisplayData.push([
											materialComboData[i][0],
											materialComboData[i][1],
											materialComboData[i][2] ]);
								}
							}
							materialAddStore.loadData(materialComboDisplayData);
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

function loadMaterialCate() {
	materialCateComboData = [];
	Ext.Ajax.request({
		url : "../../QueryMaterialCate.do",
		params : {
			"pin" : pin,
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// 格式：[编号，名称]
			// 后台格式：[编号，名称]
			var rootData = resultJSON.root;
			if (rootData.length != 0) {
				if (rootData[0].message == "normal") {
					for ( var i = 0; i < rootData.length; i++) {
						materialCateComboData.push([ rootData[i].cateID,
								rootData[i].cateName ]);
					}
					materialCateCombAdd.store.loadData(materialCateComboData);
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
					keichenInfo[2], // 厨房名称
					keichenInfo[1] // 厨房id
					]);
				}
				kitchenTypeData.push([ 255, "空", -1 ]);
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
	loadMaterialCate();
};
