
	$("#supplierMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				 location.href = "SupplierManagement.html?pin="
				 + currPin + "&restaurantID="
				 + restaurantID;
			}
		});
	});
	$("#materialMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				 location.href = "RawMaterial.html?pin="
				 + currPin + "&restaurantID="
				 + restaurantID;
			}
		});
	});