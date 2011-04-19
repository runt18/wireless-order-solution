// keyboard select handler
var dishKeyboardSelect = function(relateItemId) {
	if (relateItemId == "orderNbr") {
		var curDishNbr = Ext.getCmp("orderNbr").getValue() + "";

		if (curDishNbr == "") {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				dishesDisplayDataShow.push( [ dishesDisplayData[i][0],
						dishesDisplayData[i][1], dishesDisplayData[i][2],
						dishesDisplayData[i][3] ]);
			}
		} else {
			dishesDisplayDataShow.length = 0;
			for ( var i = 0; i < dishesDisplayData.length; i++) {
				if ((dishesDisplayData[i][1] + "").substring(0,
						curDishNbr.length) == curDishNbr) {
					dishesDisplayDataShow.push( [ dishesDisplayData[i][0],
							dishesDisplayData[i][1], dishesDisplayData[i][2],
							dishesDisplayData[i][3] ]);
				}
			}
		}

		dishesDisplayStore.reload();
	}
};

// on page load function
function dishNbrOnLoad() {
	// keyboard input dish number
	$("#orderNbr").bind("keyup", function() {
		dishKeyboardSelect("orderNbr");
	});
};

function dishSpellOnLoad() {
	// keyboard input dish spell
	$("#orderSpell").bind(
			"keyup",
			function() {
				var curDishSpell = Ext.getCmp("orderSpell").getValue()
						.toUpperCase()
						+ "";
				if (curDishSpell == "") {
					dishesDisplayDataShow.length = 0;
					for ( var i = 0; i < dishesDisplayData.length; i++) {
						dishesDisplayDataShow.push( [ dishesDisplayData[i][0],
								dishesDisplayData[i][1],
								dishesDisplayData[i][2],
								dishesDisplayData[i][3] ]);
					}
				} else {
					dishesDisplayDataShow.length = 0;
					for ( var i = 0; i < dishesDisplayData.length; i++) {
						if ((dishesDisplayData[i][2] + "").substring(0,
								curDishSpell.length) == curDishSpell) {
							dishesDisplayDataShow.push( [
									dishesDisplayData[i][0],
									dishesDisplayData[i][1],
									dishesDisplayData[i][2],
									dishesDisplayData[i][3] ]);
						}
					}
				}
				dishesDisplayStore.reload();
			});
}