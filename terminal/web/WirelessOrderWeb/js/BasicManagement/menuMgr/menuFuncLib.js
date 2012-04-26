function loadFoodMaterial() {
	// 關聯食材
	var foodID = menuStore.getAt(currRowIndex).get("foodID");

	Ext.Ajax.request({
		url : "../../QueryFoodMaterial.do",
		params : {
			"pin" : pin,
			"foodID" : foodID
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			var root = resultJSON.root;
			if (root[0].message == "normal") {
				materialData.length = 0;
				if (root.length == 1 && root[0].materialNumber == "NO_DATA") {
				} else {
					for ( var i = 0; i < root.length; i++) {
						materialData.push([ root[i].materialID,
								root[i].materialNumber, root[i].materialName,
								root[i].materialCost ]);
					}
				}
				materialStore.reload();
			} else {
				Ext.MessageBox.show({
					msg : root[0].message,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}

		},
		failure : function(response, options) {
		}
	});
}