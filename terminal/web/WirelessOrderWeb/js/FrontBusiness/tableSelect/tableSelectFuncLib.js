// 拼台时，获取主/副台号
// 此函数要求页面上有tableMergeList全局变量
function getMergeTable(tableNbr) {
	var mainTblNbr = 0;
	var mergeTblNbr = 0;
	var isFound = false;
	for ( var i = 0; i < tableMergeList.length; i++) {
		if (tableMergeList[i][0] == tableNbr) {
			mainTblNbr = tableNbr;
			mergeTblNbr = tableMergeList[i][1];
			isFound = true;
		}
	}
	if (!isFound) {
		for ( var i = 0; i < tableMergeList.length; i++) {
			if (tableMergeList[i][1] == tableNbr) {
				mainTblNbr = tableMergeList[i][0];
				mergeTblNbr = tableNbr;
			}
		}
	}
	var tblArr = [];
	tblArr.push(mainTblNbr);
	tblArr.push(mergeTblNbr);
	return tblArr;
};

// 取較大最低消
function getMaxMinCostMT(tableNbr) {
	var minCost;
	var tblArray = getMergeTable(tableNbr);
	var table1Index = -1;
	var table2Index = -1;
	for ( var i = 0; i < tableStatusListTS.length; i++) {
		if (tableStatusListTS[i].tableAlias == tblArray[0]) {
			table1Index = i;
		}
		if (tableStatusListTS[i].tableAlias == tblArray[1]) {
			table2Index = i;
		}
	}
	minCost = tableStatusListTS[table1Index].tableMinCost;
	if (minCost < tableStatusListTS[table2Index].tableMinCost) {
		minCost = tableStatusListTS[table2Index].tableMinCost;
	}

	return minCost;
}

// 取較大服務費
function getMaxSerRateMT(tableNbr) {
	var serviceRate;
	var tblArray = getMergeTable(tableNbr);
	var table1Index = -1;
	var table2Index = -1;
	for ( var i = 0; i < tableStatusListTS.length; i++) {
		if (tableStatusListTS[i].tableAlias == tblArray[0]) {
			table1Index = i;
		}
		if (tableStatusListTS[i].tableAlias == tblArray[1]) {
			table2Index = i;
		}
	}
	serviceRate = tableStatusListTS[table1Index].tableServiceRate;
	if (serviceRate < tableStatusListTS[table2Index].tableServiceRate) {
		serviceRate = tableStatusListTS[table2Index].tableServiceRate;
	}

	return serviceRate;
}

// deselect the selected table
var deselectTable = function() {
	if (selectedTable != "") {
		var selectedTableIndex = -1;
		for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
			if (tableStatusListTSDisplay[i].tableAlias == selectedTable) {
				selectedTableIndex = i;
			}
		}
		// if (tableStatusListTSDisplay[selectedTableIndex][2] == "占用") {
		// $("#table" + selectedTable).css("background",
		// "url(../images/table_on_normal.gif) no-repeat 50%");
		// } else {
		// $("#table" + selectedTable).css("background",
		// "url(../images/table_null_normal.gif) no-repeat 50%");
		// }

		if (tableStatusListTSDisplay[selectedTableIndex].tableCategory == CATE_NULL
				&& tableStatusListTSDisplay[selectedTableIndex].tableStatus == TABLE_IDLE) {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_null_normal.png) no-repeat");
		} else if (tableStatusListTSDisplay[selectedTableIndex].tableCategory == CATE_NORMAL
				&& tableStatusListTSDisplay[selectedTableIndex].tableStatus == TABLE_BUSY) {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_on_normal.png) no-repeat");
		} else if (tableStatusListTSDisplay[selectedTableIndex].tableCategory == CATE_MERGER_TABLE
				&& tableStatusListTSDisplay[selectedTableIndex].tableStatus == TABLE_BUSY) {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_on_merge.gif) no-repeat");
		} else if (tableStatusListTSDisplay[selectedTableIndex].tableCategory == CATE_TAKE_OUT
				&& tableStatusListTSDisplay[selectedTableIndex].tableStatus == TABLE_BUSY) {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_on_package.gif) no-repeat");
		} else if (tableStatusListTSDisplay[selectedTableIndex].tableCategory == CATE_JOIN_TABLE
				&& tableStatusListTSDisplay[selectedTableIndex].tableStatus == TABLE_BUSY) {
			$("#table" + selectedTable).css("background",
					"url(../../images/table_on_separate.png) no-repeat");
		}

		// $("#table" + selectedTable).css("height", "32px");
		// $("#table" + selectedTable).css("width", "62px");
		// $("#table" + selectedTable).css("margin", "20px 27px");

		selectedTable = "";
	}
};

// select a table
var selectTable = function(tableNbr) {
	var tableIndex = -1;
	for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
		if (tableStatusListTSDisplay[i].tableAlias == tableNbr) {
			tableIndex = i;
		}
	}

	if (tableStatusListTSDisplay[tableIndex].tableCategory == CATE_NULL
			&& tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_IDLE) {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_null_normal_selected.png) no-repeat";
	} else if (tableStatusListTSDisplay[tableIndex].tableCategory == CATE_NORMAL
			&& tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_BUSY) {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_normal_selected.png) no-repeat";
	} else if (tableStatusListTSDisplay[tableIndex].tableCategory == CATE_MERGER_TABLE
			&& tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_BUSY) {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_merge_selected.png) no-repeat";
	} else if (tableStatusListTSDisplay[tableIndex].tableCategory == CATE_TAKE_OUT
			&& tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_BUSY) {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_package_selected.png) no-repeat";
	} else if (tableStatusListTSDisplay[tableIndex].tableCategory == CATE_JOIN_TABLE
			&& tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_BUSY) {
		document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_separate_selected.png) no-repeat";
	}

	// $("#table" + tableNbr).css("height", "40px");
	// $("#table" + tableNbr).css("width", "70px");
	// $("#table" + tableNbr).css("margin", "12px 23px");

	selectedTable = tableNbr;
};

// keyboard select handler
var tableKeyboardSelect = function() {
	var curTableNbr = Ext.getCmp("tableNumber").getValue() + "";
	var hasTable = false;
	var tableIndex = -1;
	for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
		if (tableStatusListTSDisplay[i].tableAlias == curTableNbr) {
			hasTable = true;
			tableIndex = i;
		}
	}
	if (hasTable) {
		var tableId = "table" + curTableNbr;
		// select the icon
		$("#" + tableId).trigger("click");

		var curItemIndex = parseInt(document.getElementById("pageIndexTL").innerHTML);
		var forwardIndex = parseInt(tableIndex / 32) + 1;
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

var tableListReflash = function(node) {
		
	var currNodeId;
	if (node != null && node != undefined) {
		// 響應點擊樹節點，先觸發，后改變選擇
		currNodeId = node.id;
//		alert('node.attributes:  '+node.attributes.tableStatus);
	} else {
		if (regionTree.getSelectionModel().getSelectedNode() != undefined) {
			currNodeId = regionTree.getSelectionModel().getSelectedNode().id;
		} else {
			currNodeId = "regionTreeRoot";
		}
	}

	// 標記改為未選擇餐桌
	selectedTable = "";
	tableStatusListTSDisplay.length = 0;
	if (currNodeId == "regionTreeRoot") {
		tableStatusListTSDisplay = tableStatusListTS.slice(0);
	} else {
		for ( var i = 0; i < tableStatusListTS.length; i++) {
			if ("region" + tableStatusListTS[i].tableRegion == currNodeId) {
				tableStatusListTSDisplay.push({
					"tableAlias" : tableStatusListTS[i].tableAlias,// 餐台编号
					"tableCustNbr" : tableStatusListTS[i].tableCustNbr,// 餐台人数
					"tableStatus" : tableStatusListTS[i].tableStatus, // 状态
					"tableName" : tableStatusListTS[i].tableName,// 餐台名称
					"tableCategory" : tableStatusListTS[i].tableCategory, // 餐台类型
					"tableMinCost" : tableStatusListTS[i].tableMinCost, // 最低消费
					"tableRegion" : tableStatusListTS[i].tableRegion, // 區域代碼
					"tableServiceRate" : tableStatusListTS[i].tableServiceRate, // 服務費率
				});
			}
		}
	}	
	
	// 改變區域名稱
	var regionNameSpan = document.getElementById("listRegionName");
	if (currNodeId == "regionTreeRoot") {
		regionNameSpan.innerHTML = "全部区域";
	} else {
		if (node != null) {
			regionNameSpan.innerHTML = node.text;
		} else {
			regionNameSpan.innerHTML = regionTree.getSelectionModel().getSelectedNode().text;
		}
	}
	
	if(selectedStatus != null && parseInt(selectedStatus) >= 0){
		var tpList = new Array();		
		for(var i = 0; i < tableStatusListTSDisplay.length; i++){
			if(parseInt(tableStatusListTSDisplay[i].tableStatus) == parseInt(selectedStatus)){
				tpList.push(tableStatusListTSDisplay[i]);
				
			}			
		}
//		alert('list:   '+tpList.length);
		tableStatusListTSDisplay = tpList.slice(0);
		var ns = parseInt(selectedStatus) == TABLE_IDLE ? '空闲' : parseInt(selectedStatus) == TABLE_BUSY ? '就餐' : '';		
		regionNameSpan.innerHTML = regionNameSpan.innerHTML + '__<font color="">' + ns + '</font>';
	}
	
	// 2 ********** create table list **********
	// 2.1,get the page total count
	var pageTotalCount = parseInt(tableStatusListTSDisplay.length / 32);
	if (pageTotalCount * 32 != tableStatusListTSDisplay.length) {
		pageTotalCount = pageTotalCount + 1;
	}
	// 2.2,create the list
	var longTblList = document.getElementById("list");

	// 刪除已有節點
	var childNodes = longTblList.childNodes;
	var childLength = childNodes.length;
	if (childNodes.length != 0) {
		for ( var i = 0; i < childLength; i++) {
			longTblList.removeChild(longTblList.firstChild);
		}
	}
	restore();

	for ( var i = 0; i < pageTotalCount; i++) {
		var itemNode = document.createElement("div");
		itemNode.className = "item";
		longTblList.appendChild(itemNode);

		var ulNode = document.createElement("ul");
		ulNode.className = "table_list";
		itemNode.appendChild(ulNode);

		var currListCount = -1;
		if ((tableStatusListTSDisplay.length - i * 32) < 32) {
			currListCount = tableStatusListTSDisplay.length - i * 32;
		} else {
			currListCount = 32;
		}
		var indexInRow = 0;
		for ( var j = i * 32; j < i * 32 + currListCount; j++) {
			var liNode = document.createElement("li");
			liNode.id = "table" + tableStatusListTSDisplay[j].tableAlias;

			if (tableStatusListTSDisplay[j].tableCategory == CATE_NULL
					&& tableStatusListTSDisplay[j].tableStatus == TABLE_IDLE) {
				liNode.className = "normal_null";
			} else if (tableStatusListTSDisplay[j].tableCategory == CATE_NORMAL
					&& tableStatusListTSDisplay[j].tableStatus == TABLE_BUSY) {
				liNode.className = "normal_on";
			} else if (tableStatusListTSDisplay[j].tableCategory == CATE_MERGER_TABLE
					&& tableStatusListTSDisplay[j].tableStatus == TABLE_BUSY) {
				liNode.className = "merge_on";
			} else if (tableStatusListTSDisplay[j].tableCategory == CATE_TAKE_OUT
					&& tableStatusListTSDisplay[j].tableStatus == TABLE_BUSY) {
				liNode.className = "package_on";
			} else if (tableStatusListTSDisplay[j].tableCategory == CATE_JOIN_TABLE
					&& tableStatusListTSDisplay[j].tableStatus == TABLE_BUSY) {
				liNode.className = "separate_on";
			}

			liNode.innerHTML = tableStatusListTSDisplay[j].tableName + "<br>"
					+ tableStatusListTSDisplay[j].tableAlias;
			ulNode.appendChild(liNode);
			indexInRow = indexInRow + 1;
			if (indexInRow == 8) {
				var placeHolderNode = document.createElement("li");
				placeHolderNode.className = "placeHolder";
				ulNode.appendChild(placeHolderNode);
				indexInRow = 0;
			}
		}
	}

	// 3.3, create page count
	var pageIndex = 1;
	var pageIndexSpan = document.getElementById("pageIndexTL");
	pageIndexSpan.innerHTML = pageIndex + "";
	var pageTotalCountSpan = document.getElementById("totalCountTL");
	pageTotalCountSpan.innerHTML = "&nbsp;&nbsp;/&nbsp;&nbsp;" + pageTotalCount + "";

	// 4 ********** get the general table count
	// **********
	var totalCount = tableStatusListTSDisplay.length;
	var usedCount = 0;
	var freeCount = 0;
	for ( var i = 0; i < totalCount; i++) {
		if (tableStatusListTSDisplay[i].tableStatus == TABLE_BUSY) {
			usedCount = usedCount + 1;
		} else {
			freeCount = freeCount + 1;
		}
	}
	
	if(selectedStatus == null){
		document.getElementById("allTblDivTS").innerHTML = totalCount
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		document.getElementById("usedTbltDivTS").innerHTML = usedCount
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		document.getElementById("freeTblDivTS").innerHTML = freeCount;
	}
	// 5 ********** register the event handler for the
	// table
	// icon **********
	// mouse over & mouse off -- heightlight the icon
	$(".table_list li[class!='placeHolder']").each(function() {
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
	$(".table_list li").each(
					function() {
						$(this).bind("dblclick", function() {
											var tableIndex = -1;
											for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
												if (tableStatusListTSDisplay[i].tableAlias == selectedTable) {
													tableIndex = i;
												}
											}

											if (tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_IDLE) {
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
														+ tableStatusListTSDisplay[tableIndex].tableMinCost
														+ "&serviceRate="
														+ tableStatusListTSDisplay[tableIndex].tableServiceRate;
											} else {
												var minCost;
												var serviceRate;
												if (tableStatusListTSDisplay[tableIndex].tableCategory != CATE_MERGER_TABLE) {
													minCost = tableStatusListTSDisplay[tableIndex].tableMinCost;
													serviceRate = tableStatusListTSDisplay[tableIndex].tableServiceRate;
												} else {
													minCost = getMaxMinCostMT(selectedTable);
													serviceRate = getMaxSerRateMT(selectedTable);
												}

												location.href = "CheckOut.html?tableNbr="
														+ selectedTable
														+ "&personCount="
														+ tableStatusListTSDisplay[tableIndex].tableCustNbr
														+ "&pin="
														+ pin
														+ "&restaurantID="
														+ restaurantID
														+ "&minCost="
														+ minCost
														+ "&serviceRate="
														+ serviceRate;
											}
										});
					});

	// click - 1,change the status info; 2,heightlight
	// the icon
	$(".table_list li").each(
					function() {
						$(this).bind("click", function() {
											var tableId = this.id;
											var tableIndex = -1;
											for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
												if (tableStatusListTSDisplay[i].tableAlias == tableId
														.substr(5)) {
													tableIndex = i;
												}
											}

											// update
											// status
											document.getElementById("tblNbrDivTS").innerHTML = tableStatusListTSDisplay[tableIndex].tableAlias
													+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
											document.getElementById("perCountDivTS").innerHTML = tableStatusListTSDisplay[tableIndex].tableCustNbr
													+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
											document.getElementById("tblStatusDivTS").innerHTML = tableStatusListTSDisplay[tableIndex].tableStatus;

											// table
											// select
											var currTableNbr = $(this).attr("id").substring(5);
											if (currTableNbr != selectedTable) {
												deselectTable();
												selectTable(currTableNbr);
											}
										});
					});

};

switchTableStatus = function(_s){	
	selectedStatus = _s;
	var regionTree = Ext.getCmp('regionTree');
	var node = regionTree.getSelectionModel().getSelectedNode();
	if(node != null && typeof(node) != 'undefined'){
		node.attributes.tableStatus = _s;
	}
	tableListReflash(node);
};
