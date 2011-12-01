function loadAllMaterial() {
	materialData.length = 0;
	materialComboData.length = 0;
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
						materialData.push([ rootData[i].materialID,
								rootData[i].materialAlias,
								rootData[i].materialName ]);
						materialComboData.push([ rootData[i].materialID,
								rootData[i].materialName ]);
					}
				} else {
					Ext.MessageBox.show({
						msg : rootData[0].message,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			}
			inventoryInStatMSDS.loadData(materialComboData);
			inventoryOutStatMSDS.loadData(materialComboData);
			inventoryChangeStatMSDS.loadData(materialComboData);
			inventoryCostStatMSDS.loadData(materialComboData);
			inventoryAllStatMSDS.loadData(materialComboData);
			inventoryCheckStatMSDS.loadData(materialComboData);
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

function loadAllsupplier() {
	supplierData = [];
	supplierComboData = [];
	Ext.Ajax.request({
		url : "../../QuerySupplierMgr.do",
		params : {
			"pin" : pin,
			"type" : 0,
			"value" : "",
			"isPaging" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// 格式：[供應商ID，供應商別名，供應商名称]
			// 后台格式：[供應商ID，供應商別名，名稱，電話，聯係人，地址]
			var rootData = resultJSON.root;
			if (rootData.length != 0) {
				if (rootData[0].message == "normal") {
					supplierComboData.push([ "-1", "全部" ]);
					for ( var i = 0; i < rootData.length; i++) {
						supplierData.push([ rootData[i].supplierID,
								rootData[i].supplierAlias,
								rootData[i].supplierName ]);
						supplierComboData.push([ rootData[i].supplierID,
								rootData[i].supplierName ]);
					}
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

function loadDepartment() {
	departmentData = [];
	Ext.Ajax.request({
		url : "../../QueryDepartment.do",
		params : {
			"pin" : pin,
			"isPaging" : false,
			"isCombo" : false
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// 格式：[部門编号，部門名称]
			// 后台格式：[部門编号，部門名称]
			var rootData = resultJSON.root;
			if (rootData.length != 0) {
				if (rootData[0].message == "normal") {
					for ( var i = 0; i < rootData.length; i++) {
						departmentData.push([ rootData[i].deptID,
								rootData[i].deptName ]);
					}
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
function inventoryMgrOnLoad() {

	// update the operator name
	getOperatorName(pin, "../../");

	searchForm.remove("conditionText");
	operatorComb.setDisabled(true);

	//loadAllMaterial();
	loadAllsupplier();
	loadDepartment();
};
