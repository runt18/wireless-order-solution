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
												location.href = "SystemConfig.html?restaurantID="
														+ restaurantID;
											}
										});
					});

	$("#bill").each(
			function() {
				$(this).bind(
						"click",
						function() {
							if (currPin != "") {
								location.href = "Bills.html?restaurantID="
										+ restaurantID + "&pin=" + currPin;
							}
						});
			});
};