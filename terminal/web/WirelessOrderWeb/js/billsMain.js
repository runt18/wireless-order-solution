function billOptModifyHandler(rowindex) {
	// "51","100","2011-07-26
	// 23:23:41","一般","现金","100.44","150.0","0","3","0","0.0","","","","0.0","1","1"
	var tableNbr = billsData[rowindex][1];
	var tableNbr2 = billsData[rowindex][7];
	var category = billsData[rowindex][3];
	var orderID = billsData[rowindex][0];
	var personCount = billsData[rowindex][8];
	var discountType = billsData[rowindex][16];
	var payType = billsData[rowindex][15];
	var give = billsData[rowindex][14];
	var payManner = billsData[rowindex][4];
	var serviceRate = billsData[rowindex][10];
	var memberID = billsData[rowindex][11];
	var comment = billsData[rowindex][13];
	var minCost = billsData[rowindex][9];
	location.href = "BillModify.html?pin=" + pin + "&restaurantID="
			+ restaurantID + "&category=" + category + "&tableNbr=" + tableNbr
			+ "&tableNbr2=" + tableNbr2 + "&personCount=" + personCount
			+ "&minCost=" + minCost + "&orderID=" + orderID + "&give=" + give
			+ "&payType=" + payType + "&discountType=" + discountType
			+ "&payManner=" + payManner + "&serviceRate=" + serviceRate
			+ "&memberID=" + memberID + "&comment=" + comment;
};

var modifyBillBut = new Ext.ux.ImageButton({
	imgPath : "../images/modifyBill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "修改",
	handler : function(btn) {
		if (currRowIndex != -1) {
			billOptModifyHandler(currRowIndex);
		}
	}
});

var viewBillBut = new Ext.ux.ImageButton({
	imgPath : "../images/viewBill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "查看",
	handler : function(btn) {
	}
});

var detailBillBut = new Ext.ux.ImageButton({
	imgPath : "../images/detailBill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "明细",
	handler : function(btn) {

	}
});

function printBillFunc(rowInd) {
	Ext.Ajax.request({
		url : "../PrintOrder.do",
		params : {
			"pin" : pin,
			"orderID" : billsData[rowInd][0],
			"printReceipt" : 1
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			// currRowIndex = -1;
			Ext.MessageBox.show({
				msg : resultJSON.data,
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		},
		failure : function(response, options) {
		}
	});
};

var printBillImgBut = new Ext.ux.ImageButton({
	imgPath : "../images/printBill.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "补打结账",
	handler : function(btn) {
		if (currRowIndex != -1) {
			printBillFunc(currRowIndex);
		}
	}
});

// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "PersonLogin.html?restaurantID=" + restaurantID;
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

// north
var billsQueryCondPanel = new Ext.Panel({
	region : "north",
	border : false,
	height : 37,
	contentEl : "queryCondition"
});

// center
function billOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:billOptModifyHandler(" + rowIndex
			+ ")\">" + "<img src='../images/Modify.png'/>修改</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a>" + "<img src='../images/del.png'/>查看</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a>" + "<img src='../images/Modify.png'/>明细</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:printBillFunc(" + rowIndex + ")\">"
			+ "<img src='../images/Modify.png'/>补打</a>" + "</center>";
};

// 1，表格的数据store
var billsStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(billsData),
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

billsStore.reload();

// 2，栏位模型
var billsColumnModel = new Ext.grid.ColumnModel([ new Ext.grid.RowNumberer(), {
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
	width : 270,
	renderer : billOpt
} ]);

Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// 3,表格
			var billsGrid = new Ext.grid.GridPanel({
				title : "帐单",
				xtype : "grid",
				anchor : "99%",
				region : "center",
				border : false,
				ds : billsStore,
				cm : billsColumnModel,
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
					items : [ billsQueryCondPanel, billsGrid ]
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ modifyBillBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, viewBillBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, detailBillBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, printBillImgBut, "->", pushBackBut, {
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
