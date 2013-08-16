var protalFuncReg = function() {
	$("#frontBusiness").each(function(){
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "FrontBusiness_Module/FrontBusinessProtal.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
			}
		});
	});

	$("#system").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				// 密码校验
				//systemVerifyWin.show();
				location.href = "System_Module/SystemProtal.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
				isPrompt = true;
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
				// 密码校验
				location.href = "BasicManagement_Module/BasicMgrProtal.html?" + strEncode("restaurantID=" + restaurantID, "mi");
				isPrompt = true;
			}
		});
	});

	$("#inventoryManagement").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "InventoryManagement_Module/InventoryProtal.html?" + strEncode("restaurantID="+restaurantID, "mi");
				isPrompt = true;
			}
		});
	});

	$("#history").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = 'History_Module/HistoryStatistics.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
				isPrompt = true;
			}
		});
	});
	
	$("#member").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				window.location.href = 'Client_Module/ClientMain.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
				isPrompt = true;
			}
		});
	});
};