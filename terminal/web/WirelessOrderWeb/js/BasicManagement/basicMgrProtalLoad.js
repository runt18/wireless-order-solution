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
	$("#menuMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/menuMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/menuMgr.png) no-repeat 50%");
		});
	});

	$("#kitchenMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/kitchenMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/kitchenMgr.png) no-repeat 50%");
		});
	});

	$("#departmentMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/departmentMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/departmentMgr.png) no-repeat 50%");
		});
	});

	$("#regionMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/regionMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/regionMgr.png) no-repeat 50%");
		});
	});

	$("#tableMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/tableMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/tableMgr.png) no-repeat 50%");
		});
	});

	$("#tasteMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/tasteMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/tasteMgr.png) no-repeat 50%");
		});
	});

	$("#terminalMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/terminalMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/terminalMgr.png) no-repeat 50%");
		});
	});

	$("#staffMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/staffMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/staffMgr.png) no-repeat 50%");
		});
	});
	
	$("#discountMgr").each(function(){
		$(this).hover(function(){
			$(this).stop().css("background", "url(../../images/discountMgr_select.png) no-repeat 50%");
		},
		function(){
			$(this).stop().css("background", "url(../../images/discountMgr.png) no-repeat 50%");
		});
	});
	
};