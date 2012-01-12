//function loadAllRegion() {
//	tableData = [];
//	Ext.Ajax.request({
//		url : "../../QueryRegion.do",
//		params : {
//			"pin" : pin,
//			"isPaging" : false,
//			"isCombo" : false
//		},
//		success : function(response, options) {
//			var resultJSON = Ext.util.JSON.decode(response.responseText);
//			// 格式：[ID，名称]
//			// 后台格式：[ID，名稱]
//			var rootData = resultJSON.root;
//			if (rootData[0].message == "normal") {
//				for ( var i = 0; i < rootData.length; i++) {
//					regionData.push([ rootData[i].regionID,
//							rootData[i].regionName ]);
//				}
//
//				regionAddStore.loadData(regionData);
//
//			} else {
//				Ext.MessageBox.show({
//					msg : rootData[0].message,
//					width : 300,
//					buttons : Ext.MessageBox.OK
//				});
//			}
//		},
//		failure : function(response, options) {
//			Ext.MessageBox.show({
//				msg : " Unknown page error ",
//				width : 300,
//				buttons : Ext.MessageBox.OK
//			});
//		}
//	});
//}

function loadAllTaste() {
	tasteData = {};
	Ext.Ajax.request({
		url : "../../QueryTaste.do",
		params : {
			"pin" : pin,
			"type" : "0",
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// 后台：[口味编号,口味分类,口味名称,价钱,比例,计算方式]
			var rootData = resultJSON.root;
			if (rootData[0].message == "normal") {
				tasteData = rootData;

				// 我也說不清這裡為什麽要刷一次，就是找個藉口刷新一下表格
				tasteStore.reload({
					params : {
						start : 0,
						limit : pageRecordCount
					}
				});
			} else {
				Ext.MessageBox.show({
					msg : rootData[0].message,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
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
function tasteMgrOnLoad() {

	// update the operator name
	getOperatorName(pin, "../../");

	searchForm.remove("conditionText");

	//regionCombStore.reload();

	//loadAllRegion();
	loadAllTaste();
};
