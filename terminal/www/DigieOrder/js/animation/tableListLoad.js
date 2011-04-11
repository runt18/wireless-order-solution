var tableStatusListTS = [];

function tableSelectOnLoad() {

	tableStatusListTS.push( [ "100", 4, "占用" ]);
	tableStatusListTS.push( [ "101", 1, "空桌" ]);
	tableStatusListTS.push( [ "102", 6, "空桌" ]);
	tableStatusListTS.push( [ "103", 2, "空桌" ]);
	tableStatusListTS.push( [ "104", 5, "空桌" ]);
	tableStatusListTS.push( [ "105", 2, "占用" ]);

	// ********** register the event handler for the table icon **********
	// mouse over & mouse off -- heightlight the icon
	$(".table_list li").each(function() {
		$(this).hover(function() {
			$(this).stop().animate( {
				marginTop : "10px"
			},200);
		}, function() {
			$(this).stop().animate( {
				marginTop : "20px"
			},200);
		});
	});

	// double click -- forward the page
	$(".table_list li").each(function() {
		$(this).bind("dblclick", function() {
			location.href = "OrderMain.html";
		});
	});

	// click - 1,change the status info; 2,heightlight the icon
	$(".table_list li")
			.each(
					function() {
						$(this)
								.bind(
										"click",
										function() {
											var tableId = this.id;
											var tableIndex = -1;
											for ( var i = 0; i < tableStatusListTS.length; i++) {
												if (tableStatusListTS[i][0] == tableId
														.substr(5)) {
													tableIndex = i;
												}
											}

											document
													.getElementById("tblNbrDivTS").innerHTML = tableStatusListTS[tableIndex][0]
													+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
											document
													.getElementById("perCountDivTS").innerHTML = tableStatusListTS[tableIndex][1]
													+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
											document
													.getElementById("tblStatusDivTS").innerHTML = tableStatusListTS[tableIndex][2];
										});
					});
};