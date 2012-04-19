var protalFuncReg = function() {
	$("#historyStatistics").each(
			function() {
				$(this).bind(
						"click",
						function() {
							if (!isPrompt) {
								location.href = "HistoryStatistics.html?pin="
										+ pin + "&restaurantID="
										+ restaurantID;
							}
						});
			});

};