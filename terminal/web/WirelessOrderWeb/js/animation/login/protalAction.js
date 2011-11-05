var protalFuncReg = function() {
	$("#order").each(
			function() {
				$(this).bind(
						"click",
						function() {
							if (currPin != "" && !isPrompt) {
								location.href = "TableSelect.html?pin="
										+ currPin + "&restaurantID="
										+ restaurantID;
							}
						});
			});

	$("#system").each(function() {
		$(this).bind("click", function() {
			if (currPin != "" && !isPrompt) {
				// can not go back
				// location.href = "SystemConfig.html?restaurantID="
				// + restaurantID;
			}
		});
	});

	$("#bill").each(function() {
		$(this).bind("click", function() {
			if (currPin != "" && !isPrompt) {

				// 密码校验
				billVerifyWin.show();
				isPrompt = true;
			}
		});
	});

	$("#logout").each(function() {
		$(this).bind("click", function() {
			if(!isPrompt){
				currPin = "";
				document.getElementById("optName").innerHTML = "";
				isVerified = false;
				personLoginWin.show();
			}
		});
	});
	
	$("#shift").each(function() {
		$(this).bind("click", function() {	
			if (currPin != "" && !isPrompt){
				isPrompt = true;
				shiftVerifyWin.show();
			}
		});
	});
	
	$("#menu").each(function() {
		$(this).bind("click", function() {	
			if (currPin != "" && !isPrompt) {
				location.href = "BasicManagement_Module/MenuManagement.html?pin="
						+ currPin + "&restaurantID="
						+ restaurantID;
			}
		});
	});
	
	$("#inventory").each(function() {
		$(this).bind("click", function() {	
			if (currPin != "" && !isPrompt) {
				location.href = "BasicManagement_Module/MenuManagement.html?pin="
						+ currPin + "&restaurantID="
						+ restaurantID;
			}
		});
	});
};