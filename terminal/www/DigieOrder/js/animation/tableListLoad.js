function tableSelectOnLoad() {

	tableStatusListTS.push( [ "100", 4, "占用" ]);
	tableStatusListTS.push( [ "101", 1, "空桌" ]);
	tableStatusListTS.push( [ "102", 6, "空桌" ]);
	tableStatusListTS.push( [ "103", 2, "空桌" ]);
	tableStatusListTS.push( [ "104", 5, "空桌" ]);
	tableStatusListTS.push( [ "105", 2, "占用" ]);
	tableStatusListTS.push( [ "106", 4, "占用" ]);
	tableStatusListTS.push( [ "107", 1, "空桌" ]);
	tableStatusListTS.push( [ "108", 6, "空桌" ]);
	tableStatusListTS.push( [ "109", 2, "占用" ]);
	tableStatusListTS.push( [ "110", 5, "占用" ]);
	tableStatusListTS.push( [ "111", 2, "空桌" ]);
	for ( var i = 112; i <= 145; i++) {
		tableStatusListTS.push( [ i, 2, "空桌" ]);
	}
	for ( var i = 200; i <= 201; i++) {
		tableStatusListTS.push( [ i, 2, "占用" ]);
	}
	tableStatusListTS.push( [ "1081", 2, "占用" ]);
	tableStatusListTS.push( [ "1082", 5, "占用" ]);
	tableStatusListTS.push( [ "1083", 2, "空桌" ]);

	// ********** create table list **********
	// n, create page count
	var pageTotalCount = 3;
	var pageIndex = 1;
	var pageIndexSpan = document.getElementById("pageIndexTL");
	pageIndexSpan.innerHTML = pageIndex + "";
	var pageTotalCountSpan = document.getElementById("totalCountTL");
	pageTotalCountSpan.innerHTML = "&nbsp;&nbsp;/&nbsp;&nbsp;" + pageTotalCount
			+ "";

	// ********** register the event handler for the table icon **********
	// mouse over & mouse off -- heightlight the icon
	$(".table_list li").each(function() {
		$(this).hover(function() {
			$(this).stop().animate( {
				marginTop : "5px"
			}, 200);
		}, function() {
			$(this).stop().animate( {
				marginTop : "20px"

			}, 200);
		});
	});

	// double click -- forward the page
	$(".table_list li")
			.each(
					function() {
						$(this)
								.bind(
										"dblclick",
										function() {
											var tableIndex = -1;
											for ( var i = 0; i < tableStatusListTS.length; i++) {
												if (tableStatusListTS[i][0] == selectedTable) {
													tableIndex = i;
												}
											}

											if (tableStatusListTS[tableIndex][2] == "空桌") {
												personCountInputWin.show();
											} else {

												location.href = "OrderMain.html?tableNbr="
														+ selectedTable
														+ "&personCount="
														+ tableStatusListTS[tableIndex][1];
											}
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

											// update status
											document
													.getElementById("tblNbrDivTS").innerHTML = tableStatusListTS[tableIndex][0]
													+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
											document
													.getElementById("perCountDivTS").innerHTML = tableStatusListTS[tableIndex][1]
													+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
											document
													.getElementById("tblStatusDivTS").innerHTML = tableStatusListTS[tableIndex][2];

											// table select
											var currTableNbr = $(this).attr(
													"id").substring(5);
											if (currTableNbr != selectedTable) {
												deselectTable();
												selectTable(currTableNbr);
											}
										});
					});

	// keyboard input table number
	$("#tableNumber").bind(
			"keyup",
			function() {
				var curTableNbr = Ext.getCmp("tableNumber").getValue() + "";
				var hasTable = false;
				var tableIndex = -1;
				for ( var i = 0; i < tableStatusListTS.length; i++) {
					if (tableStatusListTS[i][0] == curTableNbr) {
						hasTable = true;
						tableIndex = i;
					}
				}
				if (hasTable) {
					var tableId = "table" + curTableNbr;
					// select the icon
					$("#" + tableId).trigger("click");

					var curItemIndex = parseInt(document
							.getElementById("pageIndexTL").innerHTML);
					var forwardIndex = parseInt(tableIndex / 24) + 1;
					if (forwardIndex != curItemIndex) {
						// move the list
						$("#list").animate( {
							left : -(forwardIndex - 1) * 800 + "px"
						}, 500, function() {
							left = -(forwardIndex - 1) * 800;
						});

						// change the page index
						$("#pageIndexTL").fadeTo(250, 0.1, function() {
							$(this).html(forwardIndex);
							$(this).fadeTo(250, 1);
						});
					}

				} else {
					deselectTable();
				}
			});

};