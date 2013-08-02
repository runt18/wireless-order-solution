var protalFuncReg = function() {
	$("#menuMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "MenuManagement.html?pin="
							+ currPin + "&restaurantID="
							+ restaurantID;
			}
		});
	});

	$("#kitchenMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "KitchenManagement.html?pin="
							+ currPin + "&restaurantID="
							+ restaurantID;
			}
		});
	});

	$("#departmentMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "DepartmentManagement.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
			}
		});
	});
	
	$("#regionMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "RegionManagement.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
			}
		});
	});
	
	$("#tableMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "TableManagement.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
			}
		});
	});
	
	$("#tasteMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "TasteManagement.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
			}
		});
	});
	
	$("#terminalMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "TerminalManagement.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
			}
		});
	});
	
	$("#staffMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "StaffManagement.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
			}
		});
	});
	
	$("#discountMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "DiscountManagement.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
			}
		});
	});
	
	$("#priceMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "PriceManagement.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
			}
		});
	});
	
	$("#cancelReasonMgr").each(function() {
		$(this).bind("click", function() {
			cancelReasonWin.show();
			Ext.getCmp('btnRefreshCRGrid').handler();
		});
	});
	
	$("#printScheme").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "PrintScheme.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
			}
		});
	});

};