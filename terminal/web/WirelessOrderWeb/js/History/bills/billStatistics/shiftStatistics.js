var shiftCheckDetpData = [];

var shiftCheckDetpStore = new Ext.data.Store({
	proxy : new Ext.data.MemoryProxy(shiftCheckDetpData),
	reader : new Ext.data.ArrayReader({}, [ {
		name : "deptName"
	}, {
		name : "discount"
	}, {
		name : "gift"
	}, {
		name : "amount"
	} ])
});

// shiftCheckDetpStore.reload();

// 2，栏位模型
var shiftCheckDetpColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "部门",
			sortable : true,
			dataIndex : "deptName",
			width : 100
		}, {
			header : "折扣",
			sortable : true,
			dataIndex : "discount",
			width : 100
		}, {
			header : "赠送",
			sortable : true,
			dataIndex : "gift",
			width : 100
		}, {
			header : "金额",
			sortable : true,
			dataIndex : "amount",
			width : 100
		} ]);

// 3,表格
var shiftCheckDetpGrid = new Ext.grid.GridPanel({
	// title : "已点菜",
	border : false,
	layout : "fit",
	ds : shiftCheckDetpStore,
	cm : shiftCheckDetpColumnModel,
	viewConfig : {
		forceFit : true
	},
	listeners : {

	}
});

var shiftCheckDetpPanel = new Ext.Panel({
	region : "center",
	layout : "fit",
	// height : 200,
	frame : true,
	items : shiftCheckDetpGrid
});

var shiftCheckTablePanel = new Ext.Panel({
	frame : true,
	region : "north",
	height : 440,
	items : [ {
		border : false,
		contentEl : "shiftCheckTableDiv"
	} ]
});

var shiftCheckTableWin = new Ext.Window({
	layout : "border",
	width : 450,
	height : 600,
	closeAction : "hide",
	resizable : false,
	closable : false,
	items : [ shiftCheckTablePanel, shiftCheckDetpPanel ],
	buttons : [
			{
				text : "打印",
				handler : function() {
					var onDuty = shiftStatResultStore.getAt(shiftDtlRowIndex)
							.get("beginTime");
					var offDuty = shiftStatResultStore.getAt(shiftDtlRowIndex)
							.get("endTime");

					Ext.Ajax.request({
						url : "../../PrintOrder.do",
						params : {
							"pin" : pin,
							"printHistoryShift" : 1,
							"onDuty" : onDuty,
							"offDuty" : offDuty
						},
						success : function(response, options) {
							var resultJSON = Ext.util.JSON
									.decode(response.responseText);
							Ext.MessageBox.show({
								msg : resultJSON.data,
								width : 300,
								buttons : Ext.MessageBox.OK
							});

						},
						failure : function(response, options) {
						}
					});
				}
			}, {
				text : "关闭",
				// disabled : true,
				handler : function() {
					shiftCheckTableWin.hide();					
					isPrompt = false;
				}
			} ]
});

// -----------------------------------------------------------------------------
shiftStatWin = new Ext.Window({
	title : "交班记录",
	width : 450,
	height : 101,
//	closeAction : "hide",
	resizable : false,
	modal:true,
	closable:false,
	constrainHeader:true,
	draggable:false,
	layout : "anchor",
	items : [ {
		border : false,
		anchor : "right 99%",
		items : [ {
			layout : "column",
			border : false,
			frame : true,
			anchor : "98%",
			items : [ {
				layout : "form",
				border : false,
				labelSeparator : ' ',
				width : 200,
				labelWidth : 30,
				items : [ {
					xtype : "datefield",
					// format : "y-m-d",
					id : "begDateMStatShift",
					width : 150,
					fieldLabel : "日期"
				} ]
			}, {
				layout : "form",
				border : false,
				labelSeparator : ' ',
				width : 200,
				labelWidth : 30,
				items : [ {
					xtype : "datefield",
					// format : "y-m-d",
					id : "endDateMStatShift",
					width : 150,
					fieldLabel : "至"
				} ]
			} ]
		} ]
	} ],
	buttons : [

			{
				text : "确定",
				handler : function() {

					isPrompt = false;
					shiftStatWin.hide();

					// 保存条件
					var dateFormated = new Date();
					begDateMStatShift = shiftStatWin.findById(
							"begDateMStatShift").getValue();
					if (begDateMStatShift != "") {
						dateFormated = begDateMStatShift;
						begDateMStatShift = dateFormated.format('Y-m-d');
						begDateMStatShift = begDateMStatShift + " 00:00:00";
					}

					endDateMStatShift = shiftStatWin.findById(
							"endDateMStatShift").getValue();
					if (endDateMStatShift != "") {
						dateFormated = endDateMStatShift;
						endDateMStatShift = dateFormated.format('Y-m-d');
						endDateMStatShift = endDateMStatShift + " 23:59:59";
					}

					isPrompt = true;
					shiftStatResultWin.show();

				}
			}, {
				text : "取消",
				handler : function() {
					isPrompt = false;
					shiftStatWin.hide();
				}
			} ],
	listeners : {
		"show" : function(thiz) {

			shiftStatWin.findById("begDateMStatShift").setValue("");
			shiftStatWin.findById("endDateMStatShift").setValue("");

		}
	}
});

// 结果框
// 前台：[交班人 ,开始时间, 结束时间, 操作]
var shiftStatResultStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../shiftStat.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "staff"
	}, {
		name : "beginTime"
	}, {
		name : "endTime"
	}, {
		name : "operator"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
// operation handler
function shiftStatDetalHandler(rowIndex) {

	var onDuty = shiftStatResultStore.getAt(rowIndex).get("beginTime");
	var offDuty = shiftStatResultStore.getAt(rowIndex).get("endTime");

	Ext.Ajax
			.request({
				url : "../../shiftStatDetail.do",
				params : {
					"pin" : pin,
					"onDuty" : onDuty,
					"offDuty" : offDuty,
					"StatisticsType" : "History"
				},
				success : function(response, options) {
					var resultJSON = Ext.util.JSON
							.decode(response.responseText);
					// 
					var rootData = resultJSON.root;
					if (rootData[0].message == "normal") {				
						
						document.getElementById("shiftTitle").innerHTML = "交班对账表";
						document.getElementById("shiftOperator").innerHTML = shiftStatResultStore
								.getAt(rowIndex).get("staff");
						document.getElementById("shiftBillCount").innerHTML = rootData[0].allBillCount;
						document.getElementById("shiftStartTime").innerHTML = onDuty;
						document.getElementById("shiftEndTime").innerHTML = offDuty;

						// 現金
						document.getElementById("billCount1").innerHTML = rootData[0].cashBillCount;
						document.getElementById("amount1").innerHTML = rootData[0].cashAmount;
						document.getElementById("actual1").innerHTML = rootData[0].cashActual;

						// 刷卡
						document.getElementById("billCount2").innerHTML = rootData[0].creditBillCount;
						document.getElementById("amount2").innerHTML = rootData[0].creditAmount;
						document.getElementById("actual2").innerHTML = rootData[0].creditActual;

						// 會員卡
						document.getElementById("billCount3").innerHTML = rootData[0].memberBillCount;
						document.getElementById("amount3").innerHTML = rootData[0].memberAmount;
						document.getElementById("actual3").innerHTML = rootData[0].memberActual;

						// 簽單
						document.getElementById("billCount4").innerHTML = rootData[0].signBillCount;
						document.getElementById("amount4").innerHTML = rootData[0].signAmount;
						document.getElementById("actual4").innerHTML = rootData[0].signActual;

						// 掛賬
						document.getElementById("billCount5").innerHTML = rootData[0].hangBillCount;
						document.getElementById("amount5").innerHTML = rootData[0].hangAmount;
						document.getElementById("actual5").innerHTML = rootData[0].hangActual;

						// 合計
						var sumAmout = rootData[0].cashAmount
								+ rootData[0].creditAmount
								+ rootData[0].memberAmount
								+ rootData[0].signAmount
								+ rootData[0].hangAmount;
						var sumActual = rootData[0].cashActual
								+ rootData[0].creditActual
								+ rootData[0].memberActual
								+ rootData[0].signActual
								+ rootData[0].hangActual;
						document.getElementById("amountSum").innerHTML = sumAmout
								.toFixed(2);
						document.getElementById("actualSum").innerHTML = sumActual
								.toFixed(2);

						// --------------
						// 抹数
						document.getElementById("eraseAmount").innerHTML = rootData[0].eraseAmount.toFixed(2);
						document.getElementById("eraseIncome").innerHTML = rootData[0].eraseBillCount;
						
						// 折扣
						document.getElementById("discountAmount").innerHTML = rootData[0].discountAmount;
						document.getElementById("discountBillCount").innerHTML = rootData[0].discountBillCount;

						// 赠送
						document.getElementById("giftAmount").innerHTML = rootData[0].giftAmount;
						document.getElementById("giftBillCount").innerHTML = rootData[0].giftBillCount;

						// 退菜
						document.getElementById("returnAmount").innerHTML = rootData[0].returnAmount;
						document.getElementById("returnBillCount").innerHTML = rootData[0].returnBillCount;

						// 反结帐
						document.getElementById("repayAmount").innerHTML = rootData[0].repayAmount;
						document.getElementById("repayBillCount").innerHTML = rootData[0].repayBillCount;

						// 服务费
						document.getElementById("serviceAmount").innerHTML = rootData[0].serviceAmount;
						
						shiftCheckDetpData.length = 0;
						var deptInfos = rootData[0].deptInfos;
						for ( var i = 0; i < deptInfos.length; i++) {
							shiftCheckDetpData.push([ deptInfos[i].deptName,
									deptInfos[i].deptDiscount,
									deptInfos[i].deptGift,
									deptInfos[i].deptAmount ]);
						}

						shiftCheckDetpStore.reload();

					} else {
						shiftStatShowDiv(false);
						Ext.MessageBox.show({
							msg : rootData[0].message,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
					}
				},
				failure : function(response, options) {
					shiftStatShowDiv(false);
					Ext.MessageBox.show({
						msg : " Unknown page error ",
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			});

	shiftCheckTableWin.show();

};

function shiftStatPrintHandler(rowIndex) {

	var onDuty = shiftStatResultStore.getAt(rowIndex).get("beginTime");
	var offDuty = shiftStatResultStore.getAt(rowIndex).get("endTime");

	Ext.Ajax.request({
		url : "../../PrintOrder.do",
		params : {
			"pin" : pin,
			"printHistoryShift" : 1,
			"onDuty" : onDuty,
			"offDuty" : offDuty
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
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

function shiftStatOpt(value, cellmeta, record, rowIndex, columnIndex, store) {
	return "<center><a href=\"javascript:shiftStatDetalHandler(" + rowIndex
			+ ")\">" + "<img src='../../images/Modify.png'/>详细</a>"
			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
			+ "<a href=\"javascript:shiftStatPrintHandler(" + rowIndex + ")\">"
			+ "<img src='../../images/del.png'/>补打</a>" + "</center>";
};

var shiftStatResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "交班人",
			sortable : true,
			dataIndex : "staff",
			width : 80
		}, {
			header : "开始时间",
			sortable : true,
			dataIndex : "beginTime",
			width : 120
		}, {
			header : "结束时间",
			sortable : true,
			dataIndex : "endTime",
			width : 120
		}, {
			header : "<center>操作</center>",
			sortable : true,
			dataIndex : "operator",
			width : 230,
			renderer : shiftStatOpt
		} ]);

var shiftStatResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : shiftStatResultStore,
	cm : shiftStatResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	viewConfig : {
		forceFit : true
	},
	listeners : {
		rowclick : function(thiz, rowIndex, e) {
			shiftDtlRowIndex = rowIndex;
		}
	},
	bbar : new Ext.PagingToolbar({
		pageSize : shiftStaticRecordCount,
		store : shiftStatResultStore,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : "没有记录"
	}),
	autoScroll : true,
	loadMask : {
		msg : "数据加载中，请稍等..."
	}
});

// 为store配置beforeload监听器
shiftStatResultGrid.getStore().on('beforeload', function() {

	// 输入查询条件参数
	this.baseParams = {
		"pin" : pin,
		"dateBegin" : begDateMStatShift,
		"dateEnd" : endDateMStatShift,
		"StatisticsType" : "History"
	};

});

// 为store配置load监听器(即load完后动作)
shiftStatResultGrid.getStore().on('load', function() {
	if (shiftStatResultGrid.getStore().getTotalCount() != 0) {
		var msg = this.getAt(0).get("message");
		if (msg != "normal") {
			Ext.MessageBox.show({
				msg : msg,
				width : 300,
				buttons : Ext.MessageBox.OK
			});
			this.removeAll();
		}
	}
});

shiftStatResultWin = new Ext.Window({
	title : "统计结果",
	width : 800,
	height : 370,
//	closeAction : "hide",
	resizable : false,
	modal:true,
	closable:false,
	constrainHeader:true,
	draggable:false,
	layout : "fit",
	items : shiftStatResultGrid,
	buttons : [ {
		text : "退出",
		handler : function() {
			isPrompt = false;
			shiftStatResultWin.hide();
		}
	} ],
	listeners : {
		"show" : function(thiz) {
			shiftStatResultGrid.getStore().reload({
				params : {
					start : 0,
					limit : shiftStaticRecordCount
				}
			});
		}
	}
});

shiftStatShowDiv = function(_st){
	var id = ['billSum','billView','billViewAddInfo','shiftCheckTableDiv','billSum'];
	var styleDisplay = 'none';
	styleDisplay = _st ? 'block' : styleDisplay;
	for(var i = 0; i < id.length; i++){
		Ext.get('billSum').setStyle({ display : styleDisplay });
	}
};
