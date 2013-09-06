var protalFuncReg = function() {
	$("#frontBusiness").each(function(){
		$(this).bind("click", function() {
			if (!isPrompt) {
				var href =  "FrontBusiness_Module/FrontBusinessProtal.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
				verifyStaff('../', 1000, href);
			}
		});
	});

	$("#system").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				var href = "System_Module/SystemProtal.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
				verifyStaff('../', 6000, href);
				
			}
		});
	});

	$("#logout").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				currPin = "";
				document.getElementById("optName").innerHTML = "";
				isVerified = false;
				personLoginWin.show();
			}
		});
	});

	$("#basicManagement").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				var href = "BasicManagement_Module/BasicMgrProtal.html?" + strEncode("restaurantID=" + restaurantID, "mi");
				verifyStaff('../', 2000, href);
				
			}
		});
	});

	$("#inventoryManagement").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				var href = "InventoryManagement_Module/InventoryProtal.html?" + strEncode("restaurantID="+restaurantID, "mi");
				verifyStaff('../', 3000, href);
			}
		});
	});

	$("#history").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				var href = 'History_Module/HistoryStatistics.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
				verifyStaff('../', 4000, href);
			}
		});
	});
	
	$("#member").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				var href = 'Client_Module/ClientMain.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
				verifyStaff('../', 5000, href);
				//isPrompt = true;
			}
		});
	});
};