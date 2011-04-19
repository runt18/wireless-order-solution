// keyboard select handler
var tableKeyboardSelect = function() {
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

		var curItemIndex = parseInt(document.getElementById("pageIndexTL").innerHTML);
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
};

// on page load function
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
	// 1,get the page total count
	var pageTotalCount = parseInt(tableStatusListTS.length / 24);
	if (pageTotalCount * 24 != tableStatusListTS.length) {
		pageTotalCount = pageTotalCount + 1;
	}
	// 2,create the list
	var longTblList = document.getElementById("list");
	for ( var i = 0; i < pageTotalCount; i++) {
		var itemNode = document.createElement("div");
		itemNode.className = "item";
		longTblList.appendChild(itemNode);

		var ulNode = document.createElement("ul");
		ulNode.className = "table_list";
		itemNode.appendChild(ulNode);

		var currListCount = -1;
		if ((tableStatusListTS.length - i * 24) < 24) {
			currListCount = tableStatusListTS.length - i * 24;
		} else {
			currListCount = 24;
		}
		var indexInRow = 0;
		for ( var j = i * 24; j < i * 24 + currListCount; j++) {
			var liNode = document.createElement("li");
			liNode.id = "table" + tableStatusListTS[j][0];
			if (tableStatusListTS[j][2] == "占用") {
				liNode.className = "on";
			}
			liNode.innerHTML = tableStatusListTS[j][0];
			ulNode.appendChild(liNode);
			indexInRow = indexInRow + 1;
			if (indexInRow == 6) {
				var placeHolderNode = document.createElement("li");
				placeHolderNode.className = "placeHolder";
				ulNode.appendChild(placeHolderNode);
				indexInRow = 0;
			}
		}
	}

	// 3, create page count
	var pageIndex = 1;
	var pageIndexSpan = document.getElementById("pageIndexTL");
	pageIndexSpan.innerHTML = pageIndex + "";
	var pageTotalCountSpan = document.getElementById("totalCountTL");
	pageTotalCountSpan.innerHTML = "&nbsp;&nbsp;/&nbsp;&nbsp;" + pageTotalCount
			+ "";

	// ********** get the general table count **********
	var totalCount = tableStatusListTS.length;
	var usedCount = 0;
	var freeCount = 0;
	for ( var i = 0; i < totalCount; i++) {
		if (tableStatusListTS[i][2] == "占用") {
			usedCount = usedCount + 1;
		} else {
			freeCount = freeCount + 1;
		}
	}
	document.getElementById("allTblDivTS").innerHTML = totalCount
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("usedTbltDivTS").innerHTML = usedCount
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	document.getElementById("freeTblDivTS").innerHTML = freeCount;

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

												location.href = "CheckOut.html?tableNbr="
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
	$("#tableNumber").bind("keyup", tableKeyboardSelect);
};