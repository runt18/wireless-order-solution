// on page load function
function loginOnLoad() {

	var Request = new URLParaQuery();
	restaurantID = Request["restaurantID"];

	// for local test
	if (restaurantID == undefined) {
		restaurantID = "18";
	}

	// protal function register
	protalFuncReg();

	// update the operator name
	if (currPin != "") {
		getOperatorName(currPin, "../../");
	}

	// mouse over & mouse off -- heightlight the icon
	$("#order")
			.each(
					function() {
						$(this)
								.hover(
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/order_select.png) no-repeat 50%");
										},
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/order.png) no-repeat 50%");
										});
					});

	$("#bill")
			.each(
					function() {
						$(this)
								.hover(
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/bill_select.png) no-repeat 50%");
										},
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/bill.png) no-repeat 50%");
										});
					});

	$("#shift")
			.each(
					function() {
						$(this)
								.hover(
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/shift_selected.png) no-repeat 50%");
										},
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/shift.png) no-repeat 50%");
										});
					});
	
};