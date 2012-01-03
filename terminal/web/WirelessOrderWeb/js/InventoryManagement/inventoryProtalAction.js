var protalFuncReg = function() {
	$("#supplierMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				 location.href = "SupplierManagement.html?pin="
				 + currPin + "&restaurantID="
				 + restaurantID;
			}
		});
	});
	
	$("#materialCateMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				 location.href = "MaterialCateManagement.html?pin="
				 + currPin + "&restaurantID="
				 + restaurantID;
			}
		});
	});

	$("#inventoryMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "InventoryManagement.html?pin="
					 + currPin + "&restaurantID="
					 + restaurantID;
			}
		});
	});

};