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
};