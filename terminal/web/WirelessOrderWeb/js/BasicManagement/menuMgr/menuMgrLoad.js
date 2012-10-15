function loadAllMaterial() {
	materialComboData = [];
	Ext.Ajax.request({
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
										rootData[i].cateID 
								]);
							}
							for ( var i = 0; i < materialComboData.length; i++) {
								if (materialComboData[i][2] == materialComboData[0][2]) {
									materialComboDisplayData.push([
											materialComboData[i][0],
											materialComboData[i][1],
											materialComboData[i][2]
									]);
								}
							}
//							materialAddStore.loadData(materialComboDisplayData);
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
						materialCateComboData.push([ 
						    rootData[i].cateID,
							rootData[i].cateName 
						]);
					}
//					materialCateCombAdd.store.loadData(materialCateComboData);
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
	
	// 獲取廚房信息，以便顯示廚房描述
	// 后台：[厨房编号,"厨房名称",一般折扣1,一般折扣2,一般折扣3,会员折扣1,会员折扣2,会员折扣3]
	// 前台：kitchenData：[厨房编号,厨房名称]
	Ext.Ajax.request({
		url : "../../QueryMenu.do",
		params : {
			pin : pin,
			restaurantID : restaurantID,
			type : 3
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				kitchenTypeData = resultJSON.root;
			} else {
				Ext.MessageBox.show({
					msg : resultJSON.msg,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
			
		}
	});

	loadAllMaterial();
	loadMaterialCate();
};
