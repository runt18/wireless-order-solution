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
				systemVerifyWin.show();
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
				menuVerifyWin.show();
				isPrompt = true;
			}
		});
	});

	$("#inventoryManagement").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				// 密码校验
				inventoryVerifyWin.show();
				isPrompt = true;
			}
		});
	});

	$("#history").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				// 密码校验
				historyVerifyWin.show();
				isPrompt = true;
			}
		});
	});
	
	$("#member").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
//				window.location.href = 'Client_Module/ClientMain.html?'
//										+ 'pin=' + currPin
//										+ '&restaurantID=' + restaurantID;
				memberVerifyWin.show();
				isPrompt = true;
			}
		});
	});
};