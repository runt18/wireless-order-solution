// keyboard select handler
var dishKeyboardSelect = function(relateItemId) {
	var curDishNbr = Ext.getCmp("orderNbr").getValue() + "";

	if (curDishNbr == "") {
		dishesDisplayDataShow.length = 0;
		for ( var i = 0; i < dishesDisplayData.length; i++) {
			dishesDisplayDataShow.push( [ dishesDisplayData[i][0],
					dishesDisplayData[i][1], dishesDisplayData[i][2] ]);
		}
	} else {
		dishesDisplayDataShow.length = 0;
		for ( var i = 0; i < dishesDisplayData.length; i++) {
			if ((dishesDisplayData[i][1] + "").substring(0, curDishNbr.length) == curDishNbr) {
				dishesDisplayDataShow.push( [ dishesDisplayData[i][0],
						dishesDisplayData[i][1], dishesDisplayData[i][2] ]);
			}
		}
	}

	dishesDisplayStore.reload();
};

// on page load function
function dishNbrOnLoad() {

	// keyboard input table number
	$("#orderNbr").bind("keyup", dishKeyboardSelect);
};