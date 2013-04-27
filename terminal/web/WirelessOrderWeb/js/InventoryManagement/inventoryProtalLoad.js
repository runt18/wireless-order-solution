// on page load function
function loginOnLoad() {
	// for local test
	if (restaurantID == undefined) {
		restaurantID = "11";
	}

	// update the operator name
	if (pin != "") {
		getOperatorName(pin, "../../");
	}

	// mouse over & mouse off -- heightlight the icon
	$("#supplierMgr").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../../images/supplierMgr_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../../images/supplierMgr.png) no-repeat 50%");
		});
	});
	
	$("#materialCateMgr").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../../images/materialCateMgr_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../../images/materialCateMgr.png) no-repeat 50%");
		});
	});

	$("#materialMgr").each(function() {
		$(this).hover(function() {
			$(this).stop().css("background", "url(../../images/materialMgr_select.png) no-repeat 50%");
		},
		function() {
			$(this).stop().css("background", "url(../../images/materialMgr.png) no-repeat 50%");
		});
	});
	
	$("#supplierMgr").each(function() {
		$(this).bind("click", function() {
			location.href = "SupplierManagement.html?"
				 + "pin=" + pin 
				 + "&restaurantID=" + restaurantID;
		});
	});
	$("#materialMgr").each(function() {
		$(this).bind("click", function() {
				 location.href = "RawMaterial.html?"
				 + "pin=" + pin 
				 + "&restaurantID=" + restaurantID;
		});
	});
};