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
				marginTop : "10px",
				paddingBottom : "10px"
			},200);
		}, function() {
			$(this).stop().animate( {
				marginTop : "20px",
				paddingBottom : "0px"
			},200);
		});
	});
	
//	$(".table_list li").each(function() {
//		$(this).hover(function() {
////			this.style["background"] = "url(../images/table_on_selected.png) no-repeat 50%";
////			$(this).css("height","40px");
////			$(this).css("width","70px");
////			$(this).css("margin","12px 23px");
//			
////			$(this).stop().fadeTo(200,0.1,function(){
////				//$(this).css("background","url(../images/table_on_selected.png) no-repeat 50%");
////				this.style["background"] = "url(../images/table_on_selected.png) no-repeat 50%";
////				$(this).css("height","40px");
////				$(this).css("width","70px");
////				//margin: 20px 27px; width: 62px; height: 32px;
////				$(this).css("margin","12px 23px");
////				$(this).fadeTo(200,1);
////			});
//			
//		}, function() {
////			$(this).css("background","url(../images/table_on.gif) no-repeat 50%");
////			$(this).css("height","32px");
////			$(this).css("width","62px");
////			$(this).css("margin","20px 27px");
//			
////			$(this).stop().fadeTo(200,0.1,function(){
////				$(this).css("background","url(../images/table_on.gif) no-repeat 50%");
////				$(this).css("height","32px");
////				$(this).css("width","62px");
////				$(this).css("margin","20px 27px");
////				$(this).fadeTo(200,1);
////			});
//			
//		});
//	});

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

											// update status
											document
													.getElementById("tblNbrDivTS").innerHTML = tableStatusListTS[tableIndex][0]
													+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
											document
													.getElementById("perCountDivTS").innerHTML = tableStatusListTS[tableIndex][1]
													+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
											document
													.getElementById("tblStatusDivTS").innerHTML = tableStatusListTS[tableIndex][2];
											
											// icon animation
											this.style["background"] = "url(../images/table_on_selected.png) no-repeat 50%";
											$(this).css("height","40px");
											$(this).css("width","70px");
											$(this).css("margin","12px 23px");
										});
					});
};