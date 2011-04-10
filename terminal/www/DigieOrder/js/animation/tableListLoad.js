var tableStatusListTS = [];

function tableSelectOnLoad() {

	tableStatusListTS.push( [ "100", 4, "占用" ]);
	tableStatusListTS.push( [ "101", 1, "空桌" ]);
	tableStatusListTS.push( [ "102", 6, "空桌" ]);
	tableStatusListTS.push( [ "103", 2, "空桌" ]);
	tableStatusListTS.push( [ "104", 5, "空桌" ]);

	$(".table_list li").each( function() {
		$(this).bind("dblclick", function() {
			location.href = "OrderMain.html";
		});
	});

	$(".table_list li").each( function() {
		$(this).bind("click", function() {
			var tableId = this.id;
			var tableIndex = -1;
		});
	});
};
