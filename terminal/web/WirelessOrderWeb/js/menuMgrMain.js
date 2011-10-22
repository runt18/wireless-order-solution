var orderStatiBut = new Ext.ux.ImageButton({
	imgPath : "../images/modifyBill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "点菜统计",
	handler : function(btn) {

	}
});

var dishAddBut = new Ext.ux.ImageButton({
	imgPath : "../images/viewBill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "添加新菜",
	handler : function(btn) {

	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "PersonLogin.html?restaurantID=" + restaurantID
				+ "&isNewAccess=false&pin=" + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn) {
	}
});

// ------------------ north ------------------------
// combom
var filterTypeData = [ [ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ],
		[ "3", "拼音" ], [ "4", "价格" ], [ "5", "厨房" ] ];
var filterTypeComb = new Ext.form.ComboBox({
	fieldLabel : "过滤",
	forceSelection : true,
	width : 100,
	value : "全部",
	id : "filter",
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : filterTypeData
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

			// ------------------dymatic field-------------------
			var conditionText = new Ext.form.TextField({
				hideLabel : true,
				id : "conditionText",
				allowBlank : false,
				width : 120
			});

			var conditionNumber = new Ext.form.NumberField({
				hideLabel : true,
				id : "conditionNumber",
				allowBlank : false,
				width : 120
			});

			var conditionDate = new Ext.form.TimeField({
				hideLabel : true,
				id : "conditionDate",
				allowBlank : false,
				format : "H:i:s",
				width : 120
			});

			var kitchenTypeData = [ [ "1", "明档" ], [ "2", "烧味" ],
					[ "3", "海鲜" ], [ "4", "厨房4" ], [ "5", "厨房5" ],
					[ "6", "厨房6" ], [ "7", "厨房7" ], [ "8", "厨房8" ],
					[ "9", "厨房9" ], [ "10", "厨房10" ], [ "11", "空" ] ];
			var kitchenTypeComb = new Ext.form.ComboBox({
				hideLabel : true,
				forceSelection : true,
				width : 120,
				 value : "明档",
				id : "kitchenTypeComb",
				store : new Ext.data.SimpleStore({
					fields : [ "value", "text" ],
					data : kitchenTypeData
				}),
				valueField : "value",
				displayField : "text",
				typeAhead : true,
				mode : "local",
				triggerAction : "all",
				selectOnFocus : true,
				allowBlank : false
			});

			// ------------------remove field-------------------
			if (conditionType == "text") {
				searchForm.remove("conditionText");
			} else if (conditionType == "number") {
				searchForm.remove("conditionNumber");
			} else if (conditionType == "date") {
				searchForm.remove("conditionDate");
			} else if (conditionType == "kitchenTypeComb") {
				searchForm.remove("kitchenTypeComb");
			} 

			// ------------------ add field -------------------
			operatorComb.setDisabled(false);
			//[ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ],[ "3", "拼音" ], [ "4", "价格" ], [ "5", "厨房" ]
			if (index == 0) {
				// 全部
				// searchForm.add(conditionText);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 1) {
				// 编号
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 2) {
				// 名称
				searchForm.add(conditionText);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 3) {
				// 拼音
				searchForm.add(conditionText);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				conditionType = "text";
			} else if (index == 4) {
				// 价格
				searchForm.add(conditionNumber);
				conditionType = "number";
			} else if (index == 5) {
				// 厨房
				searchForm.add(kitchenTypeComb);
				operatorComb.setValue("等于");
				operatorComb.setDisabled(true);
				//payTypeComb.setValue("现金");
				conditionType = "kitchenTypeComb";
			} 

			billsQueryCondPanel.doLayout();
		}
	}
});

var operatorData = [ [ "1", "等于" ], [ "2", "大于等于" ], [ "3", "小于等于" ] ];
var operatorComb = new Ext.form.ComboBox({
	hideLabel : true,
	forceSelection : true,
	width : 100,
	value : "等于",
	id : "operator",
	disabled : true,
	store : new Ext.data.SimpleStore({
		fields : [ "value", "text" ],
		data : operatorData
	}),
	valueField : "value",
	displayField : "text",
	typeAhead : true,
	mode : "local",
	triggerAction : "all",
	selectOnFocus : true,
	allowBlank : false
});

// dymatic form
var searchForm = new Ext.Panel({
	border : false,
	width : 130,
	id : "searchForm",
	items : [ {
		xtype : "textfield",
		hideLabel : true,
		id : "conditionText",
		allowBlank : false,
		width : 120
	} ]
});

// panel
var menuQueryCondPanel = new Ext.form.FormPanel({
	region : "north",
	border : false,
	height : 23,
	bodyStyle : "margin-top:5px;",
	items : [ {
		layout : "column",
		border : false,
		anchor : "98%",
		items : [ {
			layout : "form",
			labelWidth : 40,
			border : false,
			labelSeparator : '：',
			width : 150,
			items : filterTypeComb
		}, {
			layout : "form",
			border : false,
			width : 110,
			items : operatorComb
		}, searchForm, {
			layout : 'form',
			border : false,
			width : 70,
			items : [ {
				xtype : "button",
				hideLabel : true,
				id : "srchBtn",
				text : "搜索",
				width : 100,
				listeners : {
				// "click" : billQueryHandler
				}
			} ]
		} ]
	} ]
});

// center
// function menuOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
// return "<center><a href=\"javascript:billOptModifyHandler(" + rowIndex
// + ")\">" + "<img src='../images/Modify.png'/>修改</a>"
// + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
// + "<a href=\"javascript:billViewHandler()\">"
// + "<img src='../images/del.png'/>查看</a>"
// + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
// + "<a href=\"javascript:billDetailHandler()\">"
// + "<img src='../images/Modify.png'/>明细</a>"
// + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
// + "<a href=\"javascript:printBillFunc(" + rowIndex + ")\">"
// + "<img src='../images/Modify.png'/>补打</a>" + "</center>";
// };

// 1，表格的数据store
var menuStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(menuData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "billNumber"
	}, {
		name : "tableNumber"
	}, {
		name : "payDate"
	}, {
		name : "billType"
	}, {
		name : "payType"
	}, {
		name : "totalPrice"
	}, {
		name : "acturalPrice"
	}, {
		name : "billOpt"
	}, {
		name : "tableNbr2"
	}, {
		name : "personCount"
	}, {
		name : "minCost"
	}, {
		name : "serviceRate"
	}, {
		name : "memberID"
	}, {
		name : "memberName"
	}, {
		name : "comment"
	}, {
		name : "give"
	} ])
});

menuStore.reload();

// 2，栏位模型
var menuColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(), {
	header : "帐单号",
	sortable : true,
	dataIndex : "billNumber",
	width : 120
}, {
	header : "台号",
	sortable : true,
	dataIndex : "tableNumber",
	width : 120
}, {
	header : "日期",
	sortable : true,
	dataIndex : "payDate",
	width : 120
}, {
	header : "类型",
	sortable : true,
	dataIndex : "billType",
	width : 120
}, {
	header : "结帐方式",
	sortable : true,
	dataIndex : "payType",
	width : 120
}, {
	header : "金额（￥）",
	sortable : true,
	dataIndex : "totalPrice",
	width : 120
}, {
	header : "实收（￥）",
	sortable : true,
	dataIndex : "acturalPrice",
	width : 120
}, {
	header : "<center>操作</center>",
	sortable : true,
	dataIndex : "billOpt",
	width : 270
// ,
// renderer : billOpt
} ]);

Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// 3,表格
			var menuGrid = new Ext.grid.GridPanel({
				title : "菜单",
				xtype : "grid",
				anchor : "99%",
				region : "center",
				border : false,
				ds : menuStore,
				cm : menuColumnModel,
				sm : new Ext.grid.RowSelectionModel({
					singleSelect : true
				}),
				listeners : {
					rowclick : function(thiz, rowIndex, e) {
						currRowIndex = rowIndex;
					}
				}
			});

			var centerPanel = new Ext.Panel({
				region : "center",
				layout : "fit",
				frame : true,
				items : [ {
					layout : "border",
					title : "<div style='font-size:20px;'>帐单信息<div>",
					items : [ menuQueryCondPanel, menuGrid ]
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ orderStatiBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, dishAddBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, "->", pushBackBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, logOutBut ]
				})
			});

			var viewport = new Ext.Viewport(
					{
						layout : "border",
						id : "viewport",
						items : [
								{
									region : "north",
									bodyStyle : "background-color:#A9D0F5",
									html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
									height : 50,
									margins : '0 0 5 0'
								},
								centerPanel,
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
			// Ext.EventManager.onWindowResize(function() {
			// // obj.style[attr]
			// document.getElementById("wrap").style["height"] =
			// (tableSelectCenterPanel
			// .getInnerHeight() - 100)
			// + "px";
			// });
		});
