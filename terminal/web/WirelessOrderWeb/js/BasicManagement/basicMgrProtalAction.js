var protalFuncReg = function() {
	$("#menuMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "MenuManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});

	$("#kitchenMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "KitchenManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});

	$("#departmentMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "DepartmentManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});
	
	$("#regionMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "RegionManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});
	
	$("#tableMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "TableManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});
	
	$("#tasteMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "TasteManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});
	
	$("#terminalMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "TerminalManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});
	
	$("#staffMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "StaffManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});
	
	$("#discountMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "DiscountManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
		});
	});
	
	$("#priceMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "PriceManagement.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
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
				location.href = "PrintScheme.html?"+strEncode("restaurantID="
					+ restaurantID, "mi");
		});
	});
	
	$("#managementCenter").each(function() {
		$(this).bind("click", function() {
				location.href = "RestaurantMgr.html?"+strEncode("restaurantID="
					+ restaurantID, "mi");
		});
	});
	$("#deviceMgr").each(function() {
		$(this).bind("click", function() {
				location.href = "DeviceMgr.html?"+strEncode("restaurantID="
					+ restaurantID, "mi");
		});
	});

};