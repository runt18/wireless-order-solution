var protalFuncReg = function() {
	$("#frontBusiness").each(
			function() {
				$(this).bind(
						"click",
						function() {
							if (currPin != "" && !isPrompt) {
								location.href = "FrontBusiness_Module/FrontBusinessProtal.html?pin="
										+ currPin + "&restaurantID="
										+ restaurantID;
							}
						});
			});

	$("#system").each(function() {
		$(this).bind("click", function() {
			if (currPin != "" && !isPrompt) {
				location.href = "System_Module/SystemProtal.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
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
			if (currPin != "" && !isPrompt) {
				// 密码校验
				menuVerifyWin.show();
				isPrompt = true;
			}
		});
	});

	$("#inventoryManagement").each(function() {
		$(this).bind("click", function() {
			if (currPin != "" && !isPrompt) {
				// 密码校验
				inventoryVerifyWin.show();
				isPrompt = true;
			}
		});
	});
	
	$("#history").each(function() {
		$(this).bind("click", function() {
			if (currPin != "" && !isPrompt) {
				// 密码校验
				historyVerifyWin.show();
				isPrompt = true;
			}
		});
	});
};