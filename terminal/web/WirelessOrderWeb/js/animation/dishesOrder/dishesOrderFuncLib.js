var choosenTasteRefresh = function() {
	if (choosenTasteDisplay.length == 0) {
		document.getElementById("choTaste").innerHTML = "已选择口味：";
	} else {
		var displayString = "";
		for ( var i = 0; i < choosenTasteDisplay.length; i++) {
			displayString = displayString + choosenTasteDisplay[i][1] + "；";
		}
		displayString = displayString.substring(0, displayString.length - 1);
		document.getElementById("choTaste").innerHTML = "已选择口味："
				+ displayString;
	}
};