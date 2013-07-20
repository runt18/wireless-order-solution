// deselect the selected table
function deselectTable() {
	if (selectedTable != "") {
		var temp = null;
		for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
			temp = tableStatusListTSDisplay[i];
			if (temp.alias == selectedTable) {
				if(temp.statusValue == TABLE_IDLE){
					$("#table" + selectedTable).css("background",
						"url(../../images/table_null_normal.png) no-repeat");
				}else{
					if(temp.categoryValue == CATE_NORMAL){
						$("#table" + selectedTable).css("background",
							"url(../../images/table_on_normal.png) no-repeat");
					} else if (temp.categoryValue == CATE_MERGER_TABLE){
						$("#table" + selectedTable).css("background",
							"url(../../images/table_on_merge.gif) no-repeat");
					} else if (temp.categoryValue == CATE_TAKE_OUT){
						$("#table" + selectedTable).css("background",
							"url(../../images/table_on_package.gif) no-repeat");
					} else if (temp.categoryValue == CATE_JOIN_TABLE){
						$("#table" + selectedTable).css("background",
							"url(../../images/table_on_separate.png) no-repeat");
					} else if (temp.categoryValue == CATE_GROUP_TABLE){
						$("#table" + selectedTable).css("background",
							"url(../../images/table_on_merge.gif) no-repeat");
					}
				}
				/*
				if (tableStatusListTSDisplay[i].category == CATE_NULL
						&& tableStatusListTSDisplay[i].status == TABLE_IDLE) {
					$("#table" + selectedTable).css("background",
							"url(../../images/table_null_normal.png) no-repeat");
				} else if (tableStatusListTSDisplay[i].category == CATE_NORMAL
						&& tableStatusListTSDisplay[i].status == TABLE_BUSY) {
					$("#table" + selectedTable).css("background",
							"url(../../images/table_on_normal.png) no-repeat");
				} else if (tableStatusListTSDisplay[i].category == CATE_MERGER_TABLE
						&& tableStatusListTSDisplay[i].status == TABLE_BUSY) {
					$("#table" + selectedTable).css("background",
							"url(../../images/table_on_merge.gif) no-repeat");
				} else if (tableStatusListTSDisplay[i].category == CATE_TAKE_OUT
						&& tableStatusListTSDisplay[i].status == TABLE_BUSY) {
					$("#table" + selectedTable).css("background",
							"url(../../images/table_on_package.gif) no-repeat");
				} else if (tableStatusListTSDisplay[i].category == CATE_JOIN_TABLE
						&& tableStatusListTSDisplay[i].status == TABLE_BUSY) {
					$("#table" + selectedTable).css("background",
							"url(../../images/table_on_separate.png) no-repeat");
				} else if (tableStatusListTSDisplay[i].category == CATE_GROUP_TABLE
						&& tableStatusListTSDisplay[i].status == TABLE_BUSY) {
					$("#table" + selectedTable).css("background",
							"url(../../images/table_on_merge.gif) no-repeat");
				}
				*/
				
				selectedTable = "";
				break;
			}
		}
	}
};

// select a table
function selectTable(tableNbr) {
	var temp = null;
	for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
		temp = tableStatusListTSDisplay[i];
		if (temp.alias == tableNbr) {
			if(temp.statusValue == TABLE_IDLE){
				document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_null_normal_selected.png) no-repeat";
			}else{
				if(temp.categoryValue == CATE_NORMAL){
					document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_normal_selected.png) no-repeat";
				} else if (temp.categoryValue == CATE_MERGER_TABLE){
					document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_merge_selected.png) no-repeat";
				} else if (temp.categoryValue == CATE_TAKE_OUT){
					document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_package_selected.png) no-repeat";
				} else if (temp.categoryValue == CATE_JOIN_TABLE){
					document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_separate_selected.png) no-repeat";
				} else if (temp.categoryValue == CATE_GROUP_TABLE){
					document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_merge_selected.png) no-repeat";
				}
			}
			/*
			if (tableStatusListTSDisplay[i].category == CATE_NULL
					&& tableStatusListTSDisplay[i].status == TABLE_IDLE) {
				document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_null_normal_selected.png) no-repeat";
			} else if (tableStatusListTSDisplay[i].category == CATE_NORMAL
					&& tableStatusListTSDisplay[i].status == TABLE_BUSY) {
				document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_normal_selected.png) no-repeat";
			} else if (tableStatusListTSDisplay[i].category == CATE_MERGER_TABLE
					&& tableStatusListTSDisplay[i].status == TABLE_BUSY) {
				document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_merge_selected.png) no-repeat";
			} else if (tableStatusListTSDisplay[i].category == CATE_TAKE_OUT
					&& tableStatusListTSDisplay[i].status == TABLE_BUSY) {
				document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_package_selected.png) no-repeat";
			} else if (tableStatusListTSDisplay[i].category == CATE_JOIN_TABLE
					&& tableStatusListTSDisplay[i].status == TABLE_BUSY) {
				document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_separate_selected.png) no-repeat";
			} else if (tableStatusListTSDisplay[i].category == CATE_GROUP_TABLE
					&& tableStatusListTSDisplay[i].status == TABLE_BUSY) {
				document.getElementById("table" + tableNbr).style["background"] = "url(../../images/table_on_merge_selected.png) no-repeat";
			}
			*/
			selectedTable = tableNbr;
			break;
		}
	}
	
};

// keyboard select handler
var tableKeyboardSelect = function() {
	var curTableNbr = Ext.getCmp("tableNumber").getValue() + "";
	var hasTable = false;
	var tableIndex = -1;
	for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
		if (tableStatusListTSDisplay[i].aliasId == curTableNbr) {
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

function tableListReflash(node) {
	var currNodeId;
	if (node != null && node != undefined && typeof node != 'undefined') {
		currNodeId = node.attributes.regionId;
	} else {
		if (regionTree.getSelectionModel().getSelectedNode()) {
			currNodeId = regionTree.getSelectionModel().getSelectedNode().attributes.regionId;
		} else {
			currNodeId = -1;
		}
	}
	
	selectedTable = "";
	tableStatusListTSDisplay.length = 0;
	if (currNodeId == -1) {
		tableStatusListTSDisplay = tableStatusListTS.slice(0);
	} else {
		for ( var i = 0; i < tableStatusListTS.length; i++) {
			if (tableStatusListTS[i].region.id == currNodeId) {
				tableStatusListTSDisplay.push(tableStatusListTS[i]);
			}
		}
	}
	
	var regionNameSpan = document.getElementById("listRegionName");
	if (currNodeId == -1) {
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
			if(parseInt(tableStatusListTSDisplay[i].statusValue) == parseInt(selectedStatus)){
				tpList.push(tableStatusListTSDisplay[i]);
				
			}			
		}
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
			var tempData = tableStatusListTSDisplay[j];
			var liNode = document.createElement("li");
			liNode.id = "table" + tempData.alias;
			
			if(tempData.statusValue == TABLE_IDLE){
				liNode.className = "normal_null";
			}else{
				if(tempData.categoryValue == CATE_NORMAL){
					liNode.className = "normal_on";
				} else if (tempData.categoryValue == CATE_MERGER_TABLE){
					liNode.className = "merge_on";
				} else if (tempData.categoryValue == CATE_TAKE_OUT){
					liNode.className = "package_on";
				} else if (tempData.categoryValue == CATE_JOIN_TABLE){
					liNode.className = "separate_on";
				} else if (tempData.categoryValue == CATE_GROUP_TABLE){
					liNode.className = "merge_on";
				}
			}
			
/*			if (tableStatusListTSDisplay[j].category == CATE_NULL
					&& tableStatusListTSDisplay[j].status == TABLE_IDLE) {
				liNode.className = "normal_null";
			} else if (tableStatusListTSDisplay[j].category == CATE_NORMAL
					&& tableStatusListTSDisplay[j].status == TABLE_BUSY) {
				liNode.className = "normal_on";
			} else if (tableStatusListTSDisplay[j].category == CATE_MERGER_TABLE
					&& tableStatusListTSDisplay[j].status == TABLE_BUSY) {
				liNode.className = "merge_on";
			} else if (tableStatusListTSDisplay[j].category == CATE_TAKE_OUT
					&& tableStatusListTSDisplay[j].status == TABLE_BUSY) {
				liNode.className = "package_on";
			} else if (tableStatusListTSDisplay[j].category == CATE_JOIN_TABLE
					&& tableStatusListTSDisplay[j].status == TABLE_BUSY) {
				liNode.className = "separate_on";
			} else if (tableStatusListTSDisplay[j].category == CATE_GROUP_TABLE
					&& tableStatusListTSDisplay[j].status == TABLE_BUSY) {
				liNode.className = "merge_on";
			}
*/
			liNode.innerHTML = tempData.name + "<br>" + tempData.alias;
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
	var totalCount = tableStatusListTSDisplay.length;
	var usedCount = 0;
	var freeCount = 0;
	for ( var i = 0; i < totalCount; i++) {
		if (tableStatusListTSDisplay[i].statusValue == TABLE_BUSY) {
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
	$(".table_list li").each(function() {
		$(this).bind("dblclick", function() {
			var temp = null;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				temp = tableStatusListTSDisplay[i];
				if (temp.alias == selectedTable) {
					if (temp.statusValue == TABLE_IDLE) {
						location.href = "OrderMain.html?"
							+ "pin=" + pin
							+ "&restaurantID=" + restaurantID
							+ "&tableAliasID=" + selectedTable
							+ "&ts=0"
							+ "&category=1"
							+ "&minCost=" + temp.minimumCost
							+ "&serviceRate=" + temp.serviceRate;
					} else {
						location.href = "CheckOut.html?"
							+ "tableID=" + selectedTable
							+ "&pin=" + pin
							+ "&restaurantID=" + restaurantID;
					}
					break;
				}
			}

		});
	});
												

	$(".table_list li").each(function() {
		$(this).bind("click", function() {
			var tableId = this.id;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (eval(tableStatusListTSDisplay[i].alias == tableId.substr(5))) {
					document.getElementById("tblNbrDivTS").innerHTML = tableStatusListTSDisplay[i].alias
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
					document.getElementById("perCountDivTS").innerHTML = tableStatusListTSDisplay[i].customNum
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
					document.getElementById("tblStatusDivTS").innerHTML = tableStatusListTSDisplay[i].statusText;
					break;
				}
			}
			
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

function memberPointConsume(c){
	if(c == null || typeof c == ' undefined' || c.otype == 'undefined'){
		return;
	}
	
	var mobile = Ext.getCmp('numMemberMobileForConsumePoint');
	var card = Ext.getCmp('numMemberCardForConsumePoint');
	var consumePoint = Ext.getCmp('numConsumePointForConsumePoint');
	if(c.otype == 1){
		// load data
		if(typeof c.read != 'number' && c.read != 1 && c != 2){
			Ext.example.msg('提示', '操作失败, 程序异常, 请联系客服人员.');
			return;			
		}else if(c.read == 1 && (mobile.getRawValue() == '' || !mobile.isValid())){
			Ext.example.msg('提示', '请输入11位手机号码.');
			mobile.focus(mobile, true);
			return;
		}else if(c.read == 2 && card.getRawValue() == ''){
			Ext.example.msg('提示', '会员卡号.');
			card.focus(mobile, true);
			return;
		}
		Ext.Ajax.request({
			url : '../../QueryMember.do',
			params : {
				dataSource : 'normal',
				restaurantID : restaurantID,
				mobile : c.read == 1 ? mobile.getValue() : '',
				memberCard : c.read == 2 ? card.getValue() : '',
			},
			success : function(res, opt){
				var jr = Ext.decode(res.responseText);
				if(jr.success){
					if(jr.root.length == 1){
						memberPointConsumeWin.member = jr.root[0];
						memberPointConsumeWinSetData(memberPointConsumeWin.member);
						Ext.example.msg('提示', '<font style="color:red;">'+memberPointConsumeWin.member['name']+'</font> 会员信息读取成功.');
					}else{
						Ext.example.msg('提示', '该会员信息不存在, 请重新输入条件后读取.');
						memberPointConsumeWin.member = null;
						memberPointConsumeWinSetData();
					}
				}else{
					Ext.ux.showMsg(jr);
				}
			},
			failure : function(res, opt){
				Ext.ux.showMsg(Ext.decode(res.responseText));
			}
		});
	}else if(c.otype == 2){
		if(memberPointConsumeWin.member == null){
			Ext.example.msg('提示', '请先输入手机号码或会员卡号读取会员信息.');
			return;
		}
		if(!consumePoint.isValid()){
			Ext.example.msg('提示', '请输入小于当前积分的消费积分的数值.');
			return;
		}
		Ext.Ajax.request({
			url : '../../OperateMember.do',
			params : {
				dataSource : 'consumePoint',
				pin : pin,
				memberId : memberPointConsumeWin.member['id'],
				point : consumePoint.getValue()
			},
			success : function(res, opt){
				var jr = Ext.decode(res.responseText);
				if(jr.success){
					Ext.Msg.show({
						title : '会员消费成功',
						buttons : Ext.Msg.OK,
						msg : '<div style="font-size:30px; max-width:320px; color: #15428B;">原有积分:' + Ext.util.Format.usMoney(memberPointConsumeWin.member['point']).replace('$', '')
							+'<br>消费积分:' + Ext.util.Format.usMoney(consumePoint.getValue()).replace('$', '')
							+'<br>当前积分:' + Ext.util.Format.usMoney(memberPointConsumeWin.member['point'] - consumePoint.getValue()).replace('$', '')
							+'</div>'
					});
					memberPointConsumeWin.hide();
				}else{
					Ext.ux.showMsg(jr);
				}
			},
			failure : function(res, opt){
				Ext.ux.showMsg(Ext.decode(res.reponseText));
			}
		});
	}
}
/**
 * 
 * @param data
 */
function memberPointConsumeWinSetData(data){
	data = data == null || typeof data == 'undefined' ? {} : data;
	var name = Ext.getCmp('numMemberNameForConsumePoint');
	var memberType = Ext.getCmp('numMemberTypeForConsumePoint');
	var mobile = Ext.getCmp('numMemberMobileForConsumePoint');
	var card = Ext.getCmp('numMemberCardForConsumePoint');
	var point = Ext.getCmp('numMemberPointForConsumePoint');
	
	name.setValue(data['name']);
	memberType.setValue(typeof data['memberType'] != 'undefined' ? data['memberType']['name'] : '');
	mobile.setValue(data['mobile']);
	card.setValue(data['memberCard']);
	point.setValue(data['point']);
}















