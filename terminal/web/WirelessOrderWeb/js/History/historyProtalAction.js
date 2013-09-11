var protalFuncReg = function() {
	$("#historyStatistics").each(
			function() {
				$(this).bind(
						"click",
						function() {
							location.href = "HistoryStatistics.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
						});
			});

};