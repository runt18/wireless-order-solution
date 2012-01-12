// deselect the selected table
var deselectTable = function() {
	if (selectedTable != "") {
		var selectedTableIndex = -1;
		for ( var i = 0; i < tableStatusListTS.length; i++) {
			if (tableStatusListTS[i][0] == selectedTable) {
				selectedTableIndex = i;
			}
		}
		// if (tableStatusListTS[selectedTableIndex][2] == "占用") {
		// $("#table" + selectedTable).css("background",
		// "url(../images/table_on_normal.gif) no-repeat 50%");
		// } else {
		// $("#table" + selectedTable).css("background",
		// "url(../images/table_null_normal.gif) no-repeat 50%");
		// }

		if (tableStatusListTS[selectedTableIndex][4] == "一般"
				&& tableStatusListTS[selectedTableIndex][2] == "空桌") {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_null_normal.png) no-repeat 50%");
		} else if (tableStatusListTS[selectedTableIndex][4] == "一般"
				&& tableStatusListTS[selectedTableIndex][2] == "占用") {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_on_normal.png) no-repeat 50%");
		} else if (tableStatusListTS[selectedTableIndex][4] == "拼台"
				&& tableStatusListTS[selectedTableIndex][2] == "空桌") {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_null_merge.png) no-repeat 50%");
		} else if (tableStatusListTS[selectedTableIndex][4] == "拼台"
				&& tableStatusListTS[selectedTableIndex][2] == "占用") {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_on_merge.gif) no-repeat 50%");
		} else if (tableStatusListTS[selectedTableIndex][4] == "外卖"
				&& tableStatusListTS[selectedTableIndex][2] == "空桌") {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_null_package.png) no-repeat 50%");
		} else if (tableStatusListTS[selectedTableIndex][4] == "外卖"
				&& tableStatusListTS[selectedTableIndex][2] == "占用") {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_on_package.gif) no-repeat 50%");
		} else if (tableStatusListTS[selectedTableIndex][4] == "并台"
				&& tableStatusListTS[selectedTableIndex][2] == "空桌") {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_null_separate.png) no-repeat 50%");
		} else if (tableStatusListTS[selectedTableIndex][4] == "并台"
				&& tableStatusListTS[selectedTableIndex][2] == "占用") {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_on_separate.gif) no-repeat 50%");
		}

		$("#table" + selectedTable).css("height", "32px");
		$("#table" + selectedTable).css("width", "62px");
		$("#table" + selectedTable).css("margin", "20px 27px");

		selectedTable = "";
	}
};

// select a table
var selectTable = function(tableNbr) {
	var tableIndex = -1;
	for ( var i = 0; i < tableStatusListTS.length; i++) {
		if (tableStatusListTS[i][0] == tableNbr) {
			tableIndex = i;
		}
	}

	if (tableStatusListTS[tableIndex][4] == "一般"
			&& tableStatusListTS[tableIndex][2] == "空桌") {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_null_normal_selected.png) no-repeat 50%";
	} else if (tableStatusListTS[tableIndex][4] == "一般"
			&& tableStatusListTS[tableIndex][2] == "占用") {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_normal_selected.png) no-repeat 50%";
	} else if (tableStatusListTS[tableIndex][4] == "拼台"
			&& tableStatusListTS[tableIndex][2] == "空桌") {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_null_merge_selected.png) no-repeat 50%";
	} else if (tableStatusListTS[tableIndex][4] == "拼台"
			&& tableStatusListTS[tableIndex][2] == "占用") {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_merge_selected.png) no-repeat 50%";
	} else if (tableStatusListTS[tableIndex][4] == "外卖"
			&& tableStatusListTS[tableIndex][2] == "空桌") {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_null_package_selected.png) no-repeat 50%";
	} else if (tableStatusListTS[tableIndex][4] == "外卖"
			&& tableStatusListTS[tableIndex][2] == "占用") {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_package_selected.png) no-repeat 50%";
	} else if (tableStatusListTS[tableIndex][4] == "并台"
			&& tableStatusListTS[tableIndex][2] == "空桌") {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_null_separate_selected.png) no-repeat 50%";
	} else if (tableStatusListTS[tableIndex][4] == "并台"
			&& tableStatusListTS[tableIndex][2] == "占用") {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_separate_selected.png) no-repeat 50%";
	}

	$("#table" + tableNbr).css("height", "40px");
	$("#table" + tableNbr).css("width", "70px");
	$("#table" + tableNbr).css("margin", "12px 23px");

	selectedTable = tableNbr;
};