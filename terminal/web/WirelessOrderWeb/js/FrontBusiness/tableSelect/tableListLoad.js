function getData() {

	var Request = new URLParaQuery();
	pin = Request["pin"];

	tableStatusListTS.length = 0;
	tableStatusListTSDisplay.length = 0;
	tableMergeList.length = 0;

	// 后台：["餐台1编号","餐台1人数","占用","餐台1名称","一般",0]，["餐台2编号","餐台2人数","空桌","餐台2名称","外卖",300.50]
	// 后台：[ID，別名編號，名稱，區域，人數，狀態，種類，最低消費]
	// 页面：tableStatusListTS和后台一致
	Ext.Ajax.request({
		url : "../../QueryTable.do",
		params : {
			"pin" : pin,
			"type" : 0,
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			resultJSON = Ext.util.JSON.decode(response.responseText);

			var rootData = resultJSON.root;
			// if (resultJSON.success == true) {
			if (rootData.length != 0) {
				if (rootData[0].message == "normal") {
					// for ( var i = 0; i < rootData.length; i++) {
					//
					// // 1 ********** initial the table info **********
					// var thisTblNbr = rootData[i].tableAlias;
					// var thisPerNbr = rootData[i].tableCustNbr;
					//
					// var thisStatus;
					// if (rootData[i].tableStatus == 0) {
					// thisStatus = "空桌";
					// } else {
					// thisStatus = "占用";
					// }
					//
					// var thisName = rootData[i].tableName;
					//
					// var thisCategory;
					// if (rootData[i].tableCategory == 1) {
					// thisCategory = "一般";
					// } else if (rootData[i].tableCategory == 2) {
					// thisCategory = "外卖";
					// } else if (rootData[i].tableCategory == 3) {
					// thisCategory = "并台";
					// } else if (rootData[i].tableCategory == 4) {
					// thisCategory = "拼台";
					// } else {
					// thisCategory = "一般";
					// }
					//
					// var thisMincost = rootData[i].tableMinCost;
					//
					// tableStatusListTS.push([ thisTblNbr,// 餐台编号
					// thisPerNbr,// 餐台人数
					// thisStatus, // 状态
					// thisName,// 餐台名称
					// thisCategory, // 餐台类型
					// thisMincost, // 最低消费
					// rootData[i].tableRegion // 區域代碼
					// ]);
					//
					// tableStatusListTSDisplay.push([ thisTblNbr,// 餐台编号
					// thisPerNbr,// 餐台人数
					// thisStatus, // 状态
					// thisName,// 餐台名称
					// thisCategory, // 餐台类型
					// thisMincost, // 最低消费
					// rootData[i].tableRegion // 區域代碼
					// ]);
					//
					// }

					tableStatusListTSDisplay = rootData.slice(0);
					tableStatusListTS = rootData.slice(0);

					tableListReflash(null);

					// keyboard input table number
					$("#tableNumber").bind("keyup", tableKeyboardSelect);
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
		}
	});

	// get the table merge info
	// 后台：["主餐台号1","副餐台号1"]，["主餐台号2","副餐台号2"]
	// 前台: tableMergeList 与后台一致
	Ext.Ajax.request({
		url : "../../QueryMerger.do",
		params : {
			"pin" : pin
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				var data = resultJSON.data;
				var mergeInfoList = data.split("，");
				for ( var i = 0; i < mergeInfoList.length; i++) {
					var tableInfo = mergeInfoList[i].substr(1,
							mergeInfoList[i].length - 2).split(",");
					tableMergeList.push([ tableInfo[0], tableInfo[1] ]);
				}
			} else {
				Ext.MessageBox.show({
					msg : resultJSON.data,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
		}
	});

};

// on page load function
function tableSelectOnLoad() {

	getData();

	// 展開區域樹節點
	regionTree.expandAll();

	// update the operator name
	getOperatorName(pin, "../../");

	// 随机刷新
	var x = 300000;
	var y = 60000;
	var rand = parseInt(Math.random() * (x - y + 1) + y);
	setInterval(function() {
		// location.reload();
		getData();
	}, rand);

};