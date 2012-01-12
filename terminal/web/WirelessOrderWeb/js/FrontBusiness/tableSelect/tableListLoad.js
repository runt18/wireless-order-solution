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
			$("#list").animate({
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

	// tableStatusListTS.push( [ "100", 4, "占用" ]);
	// tableStatusListTS.push( [ "101", 1, "空桌" ]);
	// tableStatusListTS.push( [ "102", 6, "空桌" ]);
	// tableStatusListTS.push( [ "103", 2, "空桌" ]);
	// tableStatusListTS.push( [ "104", 5, "空桌" ]);
	// tableStatusListTS.push( [ "105", 2, "占用" ]);
	// tableStatusListTS.push( [ "106", 4, "占用" ]);
	// tableStatusListTS.push( [ "107", 1, "空桌" ]);
	// tableStatusListTS.push( [ "108", 6, "空桌" ]);
	// tableStatusListTS.push( [ "109", 2, "占用" ]);
	// tableStatusListTS.push( [ "110", 5, "占用" ]);
	// tableStatusListTS.push( [ "111", 2, "空桌" ]);
	// for ( var i = 112; i <= 145; i++) {
	// tableStatusListTS.push( [ i, 2, "空桌" ]);
	// }
	// for ( var i = 200; i <= 201; i++) {
	// tableStatusListTS.push( [ i, 2, "占用" ]);
	// }
	// tableStatusListTS.push( [ "1081", 2, "占用" ]);
	// tableStatusListTS.push( [ "1082", 5, "占用" ]);
	// tableStatusListTS.push( [ "1083", 2, "空桌" ]);

	var Request = new URLParaQuery();
	pin = Request["pin"];

	// 后台：["餐台1编号","餐台1人数","占用","餐台1名称","一般",0]，["餐台2编号","餐台2人数","空桌","餐台2名称","外卖",300.50]
	// 后台：[ID，別名編號，名稱，區域，人數，狀態，種類，最低消費]
	// 页面：tableStatusListTS和后台一致
	Ext.Ajax
			.request({
				url : "../../QueryTable.do",
				params : {
					"pin" : pin,
					"type" : 0,
					"isPaging" : false,
					"isCombo" : false
				},
				success : function(response, options) {
					resultJSON = Ext.util.JSON.decode(response.responseText);

					var rootData = resultJSON.root;
					// if (resultJSON.success == true) {
					if (rootData[0].message == "normal") {
						for ( var i = 0; i < rootData.length; i++) {

							// 1 ********** initial the table info **********
							var thisTblNbr = rootData[i].tableAlias;
							var thisPerNbr = rootData[i].tableCustNbr;

							var thisStatus;
							if (rootData[i].tableStatus == 0) {
								thisStatus = "空桌";
							} else {
								thisStatus = "占用";
							}

							var thisName = rootData[i].tableName;

							var thisCategory;
							if (rootData[i].tableCategory == 1) {
								thisCategory = "一般";
							} else if (rootData[i].tableCategory == 2) {
								thisCategory = "外卖";
							} else if (rootData[i].tableCategory == 3) {
								thisCategory = "并台";
							} else if (rootData[i].tableCategory == 4) {
								thisCategory = "拼台";
							} else {
								thisCategory = "一般";
							}

							var thisMincost = rootData[i].tableMinCost;

							tableStatusListTS.push([ thisTblNbr,// 餐台编号
							thisPerNbr,// 餐台人数
							thisStatus, // 状态
							thisName,// 餐台名称
							thisCategory, // 餐台类型
							thisMincost // 最低消费
							]);

						}

						// 2 ********** create table list **********
						// 2.1,get the page total count
						var pageTotalCount = parseInt(tableStatusListTS.length / 24);
						if (pageTotalCount * 24 != tableStatusListTS.length) {
							pageTotalCount = pageTotalCount + 1;
						}
						// 2.2,create the list
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
								currListCount = tableStatusListTS.length - i
										* 24;
							} else {
								currListCount = 24;
							}
							var indexInRow = 0;
							for ( var j = i * 24; j < i * 24 + currListCount; j++) {
								var liNode = document.createElement("li");
								liNode.id = "table" + tableStatusListTS[j][0];

								if (tableStatusListTS[j][4] == "一般"
										&& tableStatusListTS[j][2] == "空桌") {
									liNode.className = "normal_null";
								} else if (tableStatusListTS[j][4] == "一般"
										&& tableStatusListTS[j][2] == "占用") {
									liNode.className = "normal_on";
								} else if (tableStatusListTS[j][4] == "拼台"
										&& tableStatusListTS[j][2] == "空桌") {
									liNode.className = "merge_null";
								} else if (tableStatusListTS[j][4] == "拼台"
										&& tableStatusListTS[j][2] == "占用") {
									liNode.className = "merge_on";
								} else if (tableStatusListTS[j][4] == "外卖"
										&& tableStatusListTS[j][2] == "空桌") {
									liNode.className = "package_null";
								} else if (tableStatusListTS[j][4] == "外卖"
										&& tableStatusListTS[j][2] == "占用") {
									liNode.className = "package_on";
								} else if (tableStatusListTS[j][4] == "并台"
										&& tableStatusListTS[j][2] == "空桌") {
									liNode.className = "separate_null";
								} else if (tableStatusListTS[j][4] == "并台"
										&& tableStatusListTS[j][2] == "占用") {
									liNode.className = "separate_on";
								}

								liNode.innerHTML = tableStatusListTS[j][0];
								ulNode.appendChild(liNode);
								indexInRow = indexInRow + 1;
								if (indexInRow == 6) {
									var placeHolderNode = document
											.createElement("li");
									placeHolderNode.className = "placeHolder";
									ulNode.appendChild(placeHolderNode);
									indexInRow = 0;
								}
							}
						}

						// 3.3, create page count
						var pageIndex = 1;
						var pageIndexSpan = document
								.getElementById("pageIndexTL");
						pageIndexSpan.innerHTML = pageIndex + "";
						var pageTotalCountSpan = document
								.getElementById("totalCountTL");
						pageTotalCountSpan.innerHTML = "&nbsp;&nbsp;/&nbsp;&nbsp;"
								+ pageTotalCount + "";

						// 4 ********** get the general table count
						// **********
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

						// 5 ********** register the event handler for the
						// table
						// icon **********
						// mouse over & mouse off -- heightlight the icon
						$(".table_list li").each(function() {
							$(this).hover(function() {
								$(this).stop().animate({
									marginTop : "5px"
								}, 200);
							}, function() {
								$(this).stop().animate({
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
																	// personCountInputWin
																	// .show();
																	// for
																	// forward
																	// the page
																	// 只有空台才要输入人数，只有“一般”类型才有空台，固定category为1
																	// default
																	// person
																	// count 1
																	location.href = "OrderMain.html?tableNbr="
																			+ selectedTable
																			+ "&personCount=1"
																			+ "&tableStat=free"
																			+ "&category=1"
																			+ "&tableNbr2=0"
																			+ "&pin="
																			+ pin
																			+ "&restaurantID="
																			+ restaurantID
																			+ "&minCost="
																			+ tableStatusListTS[tableIndex][5];
																} else {

																	location.href = "CheckOut.html?tableNbr="
																			+ selectedTable
																			+ "&personCount="
																			+ tableStatusListTS[tableIndex][1]
																			+ "&pin="
																			+ pin
																			+ "&restaurantID="
																			+ restaurantID
																			+ "&minCost="
																			+ tableStatusListTS[tableIndex][5];
																}
															});
										});

						// click - 1,change the status info; 2,heightlight
						// the icon
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

																// update
																// status
																document
																		.getElementById("tblNbrDivTS").innerHTML = tableStatusListTS[tableIndex][0]
																		+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
																document
																		.getElementById("perCountDivTS").innerHTML = tableStatusListTS[tableIndex][1]
																		+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
																document
																		.getElementById("tblStatusDivTS").innerHTML = tableStatusListTS[tableIndex][2];

																// table
																// select
																var currTableNbr = $(
																		this)
																		.attr(
																				"id")
																		.substring(
																				5);
																if (currTableNbr != selectedTable) {
																	deselectTable();
																	selectTable(currTableNbr);
																}
															});
										});

						// keyboard input table number
						$("#tableNumber").bind("keyup", tableKeyboardSelect);
					} else {
						Ext.MessageBox.show({
							msg : rootData[0].message,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
				},
				failure : function(response, options) {
				}
			});

	// get the table merge info
	// 后台：["主餐台号1","副餐台号1"]，["主餐台号2","副餐台号2"]
	// 前台: tableMergeList 与后台一致
	Ext.Ajax.request({
		url : "../../QueryMerger.do",
		params : {
			"pin" : pin
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				var data = resultJSON.data;
				var mergeInfoList = data.split("，");
				for ( var i = 0; i < mergeInfoList.length; i++) {
					var tableInfo = mergeInfoList[i].substr(1,
							mergeInfoList[i].length - 2).split(",");
					tableMergeList.push([ tableInfo[0], tableInfo[1] ]);
				}
			} else {
				Ext.MessageBox.show({
					msg : resultJSON.data,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
		}
	});

	// update the operator name
	getOperatorName(pin, "../../");

	// 随机刷新
	var x = 300000;
	var y = 60000;
	var rand = parseInt(Math.random() * (x - y + 1) + y);
	setInterval(function() {
		location.reload();
	}, rand);

};