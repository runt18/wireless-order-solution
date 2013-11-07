var protalFuncReg = function() {
	$("#frontBusiness").each(function(){
		$(this).bind("click", function() {
			var href =  "FrontBusiness_Module/FrontBusinessProtal.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
			verifyStaff('../', 1000, function(){
				location.href = href;
			});
		});
	});

	$("#system").each(function() {
		$(this).bind("click", function() {
			var href = "System_Module/SystemProtal.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
			verifyStaff('../', 6000, href);
				
		});
	});

	$("#logout").each(function() {
		$(this).bind("click", function() {
			currPin = "";
			document.getElementById("optName").innerHTML = "";
			Ext.Ajax.request({
				url : '../LoginOut.do',
				success : function(){
					verifyLogin();
					personLoginWin.show();
				},
				failure : function(){
					
				}
			});
			isVerified = false;
				
		});
	});

	$("#basicManagement").each(function() {
		$(this).bind("click", function() {
			var href = "BasicManagement_Module/BasicMgrProtal.html?" + strEncode("restaurantID=" + restaurantID, "mi");
			verifyStaff('../', 2000, href);
			
		});
	});

	$("#inventoryManagement").each(function() {
		$(this).bind("click", function() {
			var href = "InventoryManagement_Module/InventoryProtal.html?" + strEncode("restaurantID="+restaurantID, "mi");
			verifyStaff('../', 5000, href);
		});
	});

	$("#history").each(function() {
		$(this).bind("click", function() {
			var href = 'History_Module/HistoryStatistics.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
			verifyStaff('../', 4000, href);
		});
	});
	
	$("#member").each(function() {
		$(this).bind("click", function() {
			var href = 'Client_Module/ClientMain.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
			verifyStaff('../', 3000, href);
		});
	});
};