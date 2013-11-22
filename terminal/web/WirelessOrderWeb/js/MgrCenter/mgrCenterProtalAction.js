var protalFuncReg = function() {
	$("#deviceMgr").each(function(){
		$(this).bind("click", function() {
			var href =  "MgrCenter_Module/DeviceMgr.html";
			location.href = href;
		});
	});

	$("#restaurantMgr").each(function() {
		$(this).bind("click", function() {
			var href = "MgrCenter_Module/RestaurantMgr.html";
			location.href = href;
				
		});
	});
	
	bindActiveEvent("billboardMgr",
		"url(../images/deviceMgr_select.png) no-repeat 50%",
		"url(../images/deviceMgr.png) no-repeat 50%",
		"MgrCenter_Module/BillboardMgr.html"
	);
};