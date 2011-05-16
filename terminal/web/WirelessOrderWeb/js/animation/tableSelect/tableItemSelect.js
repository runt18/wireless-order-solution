// deselect the selected table
var deselectTable = function() {
	if (selectedTable != "") {
		var selectedTableIndex = -1;
		for ( var i = 0; i < tableStatusListTS.length; i++) {
			if (tableStatusListTS[i][0] == selectedTable) {
				selectedTableIndex = i;
			}
		}
		if (tableStatusListTS[selectedTableIndex][2] == "占用") {
			$("#table" + selectedTable).css("background",
					"url(../images/table_on.gif) no-repeat 50%");
		} else {
			$("#table" + selectedTable).css("background",
					"url(../images/table_null.gif) no-repeat 50%");
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

	if (tableStatusListTS[tableIndex][2] == "占用") {
		document.getElementById("table" + tableNbr).style["background"] = "url(../images/table_on_selected.png) no-repeat 50%";
	} else {
		document.getElementById("table" + tableNbr).style["background"] = "url(../images/table_null_selected.png) no-repeat 50%";
	}

	$("#table" + tableNbr).css("height", "40px");
	$("#table" + tableNbr).css("width", "70px");
	$("#table" + tableNbr).css("margin", "12px 23px");
	
	selectedTable = tableNbr;
};