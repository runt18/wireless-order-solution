var protalFuncReg = function() {
	$("#order").each(
			function() {
				$(this).bind(
						"click",
						function() {
							if (!isPrompt) {
								location.href = "TableSelect.html?pin="
										+ currPin + "&restaurantID="
										+ restaurantID;
							}
						});
			});

	$("#bill").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				// 密码校验
				billVerifyWin.show();
				isPrompt = true;
			}
		});
	});
	
	$("#shift").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				// 密码校验
				isPrompt = true;
				shiftVerifyWin.show();
			}
		});
	});

	$("#dailySettle").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				// 密码校验
				isPrompt = true;
				dailySettleVerifyWin.show();
			}
		});
	});

};