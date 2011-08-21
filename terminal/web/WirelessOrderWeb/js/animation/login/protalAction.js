var protalFuncReg = function() {
	$("#order").each(
			function() {
				$(this).bind(
						"click",
						function() {
							if (currPin != "") {
								location.href = "TableSelect.html?pin="
										+ currPin + "&restaurantID="
										+ restaurantID;
							}
						});
			});

	$("#system")
			.each(
					function() {
						$(this)
								.bind(
										"click",
										function() {
											if (currPin != "") {
												// can not go back
//												location.href = "SystemConfig.html?restaurantID="
//														+ restaurantID;
											}
										});
					});

	$("#bill").each(function() {
		$(this).bind("click", function() {
			if (currPin != "") {

				// 密码校验
				billVerifyWin.show();
			}
		});
	});

	$("#logout").each(function() {
		$(this).bind("click", function() {
			currPin = "";
			isVerified = false;
			personLoginWin.show();
		});
	});
};