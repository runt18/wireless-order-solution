// --------------check-out center panel-----------------
// 1，数据
// 格式:[菜名，口味，数量，单价，非会员折扣率，会员折扣率，总价]
var checkOutData = [];
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 2, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 3, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 4, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 5, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 2, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 3, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 4, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 5, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "100%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 2, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 3, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 4, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 5, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2", "100%", "50%" ]);

var nonMemberTotalCount = 0.0;
var nonMemberSingleCountArray = [];
var memberTotalCount = 0.0;
var memberSingleCountArray = [];
for ( var i = 0; i < checkOutData.length; i++) {
	// non member
	var singleCount = parseFloat(checkOutData[i][3].substring(1))
			* (parseFloat(checkOutData[i][4].substring(0,
					checkOutData[i][4].length)) / 100) * checkOutData[i][2];
	singleCount = singleCount.toFixed(2);
	// nonMemberSingleCountArray.push(checkOutData[i][3] + " × "
	// + checkOutData[i][4] + " × " + checkOutData[i][2] + " = " + "￥<b>"
	// + singleCount + "</b>");
	nonMemberSingleCountArray.push("￥<b>" + singleCount + "</b>");
	nonMemberTotalCount = parseFloat(nonMemberTotalCount)
			+ parseFloat(singleCount);
	nonMemberTotalCount = nonMemberTotalCount.toFixed(2);

	// member
	singleCount = parseFloat(checkOutData[i][3].substring(1))
			* (parseFloat(checkOutData[i][5].substring(0,
					checkOutData[i][5].length)) / 100) * checkOutData[i][2];
	singleCount = singleCount.toFixed(2);
	// memberSingleCountArray.push(checkOutData[i][3] + " × "
	// + checkOutData[i][5] + " × " + checkOutData[i][2] + " = " + "￥<b>"
	// + singleCount + "</b>");
	memberSingleCountArray.push("￥<b>" + singleCount + "</b>");
	memberTotalCount = parseFloat(memberTotalCount) + parseFloat(singleCount);
	memberTotalCount = memberTotalCount.toFixed(2);
}

var checkOutDataDisplay = [];
for ( var i = 0; i < checkOutData.length; i++) {
	checkOutDataDisplay.push( [ checkOutData[i][0], checkOutData[i][1],
			checkOutData[i][2], checkOutData[i][3], checkOutData[i][4],
			nonMemberSingleCountArray[i] ]);
}

checkOutDataDisplay.push( [
		"",
		"",
		"",
		"",
		"<div style='font-size:18px;font-weight:bold;'>合计</div>",
		"<div style='font-size:18px;font-weight:bold;'>￥" + nonMemberTotalCount
				+ "</div>" ]);

// 2，表格的数据store
var checkOutStore = new Ext.data.Store( {
	proxy : new Ext.data.MemoryProxy(checkOutDataDisplay),
	reader : new Ext.data.ArrayReader( {}, [ {
		name : "dishName"
	}, {
		name : "dishTaste"
	}, {
		name : "dishCount"
	}, {
		name : "dishPrice"
	}, {
		name : "dishDiscount"
	}, {
		name : "dishTotalPrice"
	} ])
});

checkOutStore.reload();

// 3，栏位模型
var checkOutColumnModel = new Ext.grid.ColumnModel( [
		new Ext.grid.RowNumberer(), {
			header : "菜名",
			sortable : true,
			dataIndex : "dishName",
			id : "dishNameCOCM",
			width : 230
		}, {
			header : "口味",
			sortable : true,
			dataIndex : "dishTaste",
			width : 160
		}, {
			header : "数量",
			sortable : true,
			dataIndex : "dishCount",
			width : 160
		}, {
			header : "单价",
			sortable : true,
			dataIndex : "dishPrice",
			width : 160
		}, {
			header : "打折率",
			sortable : true,
			dataIndex : "dishDiscount",
			width : 160
		}, {
			header : "总价",
			sortable : true,
			dataIndex : "dishTotalPrice",
			width : 220
		} ]);

// 4，表格
var checkOutGrid = new Ext.grid.GridPanel( {
	title : "菜式",
	width : 1000,
	style : "margin:0 auto",
	// height : 400,
	autoScroll : "true",
	xtype : "grid",
	// anchor : "99%",
	// height : 500,
	ds : checkOutStore,
	cm : checkOutColumnModel,
	autoExpandColumn : "dishNameCOCM"
});

var discountKindData = [ [ "0", "非会员" ], [ "1", "会员" ] ];

var discountKindComb = new Ext.form.ComboBox( {
	fieldLabel : "打折方式",
	labelStyle : "font-size:14px;font-weight:bold;",
	forceSelection : true,
	value : "非会员",
	store : new Ext.data.SimpleStore( {
		fields : [ "value", "text" ],
		data : discountKindData
	}),
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false,
	listeners : {
		select : function(combo, record, index) {
			if (record.get("text") == "非会员") {
				// hide the button
	checkOutForm.buttons[2].hide();

	// update the grid
	checkOutDataDisplay.length = 0;
	for ( var i = 0; i < checkOutData.length; i++) {
		checkOutDataDisplay.push( [ checkOutData[i][0], checkOutData[i][1],
				checkOutData[i][2], checkOutData[i][3], checkOutData[i][4],
				nonMemberSingleCountArray[i] ]);
	}

	checkOutDataDisplay.push( [
			"",
			"",
			"",
			"",
			"<div style='font-size:18px;font-weight:bold;'>合计</div>",
			"<div style='font-size:18px;font-weight:bold;'>￥"
					+ nonMemberTotalCount + "</div>" ]);
} else {
	// show the button
	checkOutForm.buttons[2].show();

	// update the grid
	checkOutDataDisplay.length = 0;
	for ( var i = 0; i < checkOutData.length; i++) {
		checkOutDataDisplay.push( [ checkOutData[i][0], checkOutData[i][1],
				checkOutData[i][2], checkOutData[i][3], checkOutData[i][5],
				memberSingleCountArray[i] ]);
	}

	checkOutDataDisplay.push( [
			"",
			"",
			"",
			"",
			"<div style='font-size:18px;font-weight:bold;'>合计</div>",
			"<div style='font-size:18px;font-weight:bold;'>￥"
					+ memberTotalCount + "</div>" ]);
}
checkOutStore.reload();
}
}
});

var checkOutForm = new Ext.form.FormPanel( {
	frame : true,
	border : false,
	items : [ {
		layout : "column",
		border : false,
		items : [ {
			html : "<div>&nbsp;&nbsp;</div>",
			id : "placeHolderCOF",
			width : 150
		}, {
			layout : "form",
			border : false,
			labelSeparator : '：',
			labelWidth : 30,
			width : 600,
			items : [ discountKindComb ]
		} ]
	}, checkOutGrid ],
	buttons : [ {
		text : "现金结账",
		handler : function() {
		}
	}, {
		text : "信用卡结账",
		handler : function() {
		}
	}, {
		text : "会员卡结账",
		hidden : true,
		handler : function() {
		}
	}, {
		text : "返回",
		handler : function() {
			location.href = "TableSelect.html";
		}
	} ],
	listeners : {
		afterlayout : function(thiz) {
			checkOutGrid.setHeight(thiz.getInnerHeight() - 30);
			thiz.findById("placeHolderCOF").setWidth(
					(thiz.getInnerWidth() - 1000) / 2);
		}
	}
});

var checkOutCenterPanel = new Ext.Panel( {
	region : "center",
	id : "checkOutCenterPanel",
	title : "<div style='font-size:18px;padding-left:2px'>结账<div>",
	layout : "fit",
	items : [ checkOutForm ]
});

// --------------check-out north panel-----------------
var checkOutNorthPanel = new Ext.Panel( {
	id : "checkOutNorthPanel",
	region : "north",
	height : 40,
	border : false,
	layout : "form",
	frame : true,
	contentEl : "tableStatusCO"
});

Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
		Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
		Ext.QuickTips.init();

		// alert(window.location.href
		// .substr(window.location.href.indexOf("=") + 1));
		// *************整体布局*************
		var centerPanelCO = new Ext.Panel( {
			id : "centerPanelDO",
			region : "center",
			border : false,
			margins : "0 5 0 0",
			layout : "border",
			items : [ checkOutCenterPanel, checkOutNorthPanel ]
		});

		var viewport = new Ext.Viewport(
				{
					layout : "border",
					items : [
							{
								region : "north",
								html : "<div style='padding:10px; background-color:#A9D0F5'><h4 style='font-size:150%'>无线点餐网页终端<h4></div>",
								height : 50,
								margins : "0 0 5 0"
							},
							centerPanelCO,
							{
								region : "south",
								height : 30,
								layout : "form",
								frame : true,
								border : false,
								html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
							} ]
				});

		// -------------------- 浏览器大小改变 -------------------------------
		// 1,调整colDisplayFormUQ中表格的高度
		Ext.EventManager.onWindowResize(function() {
			checkOutGrid.setHeight(checkOutCenterPanel.getInnerHeight() - 100);
			// dataSortGridUQ.setHeight(150);
			});
	});
