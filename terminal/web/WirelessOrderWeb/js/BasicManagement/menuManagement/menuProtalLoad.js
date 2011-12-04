// on page load function
function loginOnLoad() {

	var Request = new URLParaQuery();
	restaurantID = Request["restaurantID"];

	// for local test
	if (restaurantID == undefined) {
		restaurantID = "11";
	}

	// protal function register
	protalFuncReg();

	// update the operator name
	if (currPin != "") {
		getOperatorName(currPin, "../../");
	}

	// mouse over & mouse off -- heightlight the icon
	$("#menuMgr")
			.each(
					function() {
						$(this)
								.hover(
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/menuMgr_select.png) no-repeat 50%");
										},
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/menuMgr.png) no-repeat 50%");
										});
					});

	$("#kitchenMgr")
			.each(
					function() {
						$(this)
								.hover(
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/kitchenMgr_select.png) no-repeat 50%");
										},
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/kitchenMgr.png) no-repeat 50%");
										});
					});

	$("#departmentMgr")
			.each(
					function() {
						$(this)
								.hover(
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/departmentMgr_select.png) no-repeat 50%");
										},
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/departmentMgr.png) no-repeat 50%");
										});
					});
};