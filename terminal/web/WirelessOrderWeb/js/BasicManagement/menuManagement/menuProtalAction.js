var protalFuncReg = function() {
	$("#menuMgr").each(
			function() {
				$(this).bind(
						"click",
						function() {
							if (!isPrompt) {
								location.href = "MenuManagement.html?pin="
										+ currPin + "&restaurantID="
										+ restaurantID;
							}
						});
			});

	$("#kitchenMgr").each(
			function() {
				$(this).bind(
						"click",
						function() {
							if (!isPrompt) {
								location.href = "KitchenManagement.html?pin="
										+ currPin + "&restaurantID="
										+ restaurantID;
							}
						});
			});

	$("#departmentMgr").each(function() {
		$(this).bind("click", function() {
			if (!isPrompt) {
				location.href = "DepartmentManagement.html?pin="
					+ currPin + "&restaurantID="
					+ restaurantID;
			}
		});
	});

};