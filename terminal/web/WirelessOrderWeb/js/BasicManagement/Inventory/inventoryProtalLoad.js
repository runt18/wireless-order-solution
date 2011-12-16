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
	$("#supplierMgr")
			.each(
					function() {
						$(this)
								.hover(
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/supplierMgr_select.png) no-repeat 50%");
										},
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/supplierMgr.png) no-repeat 50%");
										});
					});
	
	$("#materialCateMgr")
	.each(
			function() {
				$(this)
						.hover(
								function() {
									$(this)
											.stop()
											.css("background",
													"url(../../images/materialCateMgr_select.png) no-repeat 50%");
								},
								function() {
									$(this)
											.stop()
											.css("background",
													"url(../../images/materialCateMgr.png) no-repeat 50%");
								});
			});

	$("#inventoryMgr")
			.each(
					function() {
						$(this)
								.hover(
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/inventoryMgr_select.png) no-repeat 50%");
										},
										function() {
											$(this)
													.stop()
													.css("background",
															"url(../../images/inventoryMgr.png) no-repeat 50%");
										});
					});
};