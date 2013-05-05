// on page load function
function loginOnLoad() {
	// update the operator name
	getOperatorName(pin, "../../");

	// mouse over & mouse off -- heightlight the icon
	$("#supplierMgr").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../../images/supplierMgr_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../../images/supplierMgr.png) no-repeat 50%");
		});
	});
	
	$("#inventoryBasicMgr").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../../images/inventoryBasicMgr_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../../images/inventoryBasicMgr.png) no-repeat 50%");
		});
	});
	
	$("#supplierMgr").each(function() {
		$(this).bind("click", function() {
			location.href = "SupplierManagement.html?"
				 + "pin=" + pin 
				 + "&restaurantID=" + restaurantID;
		});
	});
	$("#inventoryBasicMgr").each(function() {
		$(this).bind("click", function() {
				 location.href = "InventoryBasicManagement.html?"
				 + "pin=" + pin 
				 + "&restaurantID=" + restaurantID;
		});
	});
};