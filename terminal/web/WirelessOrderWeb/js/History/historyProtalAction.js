var protalFuncReg = function() {
	$("#historyStatistics").each(
			function() {
				$(this).bind(
						"click",
						function() {
							if (!isPrompt) {
								location.href = "HistoryStatistics.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
							}
						});
			});

};