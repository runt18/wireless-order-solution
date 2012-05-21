// 结果框
// 前台：[编号	日期	账单数	现金(￥)	刷卡(￥)	会员卡(￥)	签单(￥)	挂账(￥)	折扣额(￥)	赠送额(￥)	退菜额(￥)	反结帐额(￥)	服务费(￥)	金额(￥)	实收(￥)]
var businessStatResultStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../businessStatistics.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "date"
	}, {
		name : "orderCount"
	}, {
		name : "cash"
	}, {
		name : "bankCard"
	}, {
		name : "memberCard"
	}, {
		name : "credit"
	}, {
		name : "sign"
	}, {
		name : "discount"
	}, {
		name : "gift"
	}, {
		name : "return"
	}, {
		name : "paid"
	}, {
		name : "service"
	}, {
		name : "totalPrice"
	}, {
		name : "actualPrice"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var businessStatResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "日期",
			sortable : true,
			dataIndex : "date",
			width : 100
		}, {
			header : "账单数",
			sortable : true,
			dataIndex : "orderCount",
			width : 80
		}, {
			header : "现金(￥)",
			sortable : true,
			dataIndex : "cash",
			width : 80
		}, {
			header : "	刷卡(￥)",
			sortable : true,
			dataIndex : "bankCard",
			width : 80
		}, {
			header : "会员卡（￥）",
			sortable : true,
			dataIndex : "memberCard",
			width : 80
		}, {
			header : "挂账（￥）",
			sortable : true,
			dataIndex : "credit",
			width : 80
		}, {
			header : "签单（￥）",
			sortable : true,
			dataIndex : "sign",
			width : 80
		}, {
			header : "折扣额（￥）",
			sortable : true,
			dataIndex : "discount",
			width : 80
		}, {
			header : "赠送额（￥）",
			sortable : true,
			dataIndex : "gift",
			width : 80
		}, {
			header : "退菜额(￥)",
			sortable : true,
			dataIndex : "return",
			width : 80
		}, {
			header : "反结帐额(￥)",
			sortable : true,
			dataIndex : "paid",
			width : 80
		}, {
			header : "金额(￥)",
			sortable : true,
			dataIndex : "totalPrice",
			width : 100
		}, {
			header : "实收(￥)",
			sortable : true,
			dataIndex : "actualPrice",
			width : 100
		} ]);

var businessStatResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : businessStatResultStore,
	cm : businessStatResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	viewConfig : {
		forceFit : true
	},
	bbar : new Ext.PagingToolbar({
		pageSize : businessStaticRecordCount,
		store : businessStatResultStore,
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
businessStatResultGrid.getStore().on('beforeload', function() {

	// 输入查询条件参数
	this.baseParams = {
		"pin" : pin,
		"dateBegin" : businessStaticBeginDate,
		"dateEnd" : businessStaticEndDate,
		"StatisticsType" : "History"
	};

});

// 为store配置load监听器(即load完后动作)
businessStatResultGrid.getStore().on('load', function() {
	if (businessStatResultGrid.getStore().getTotalCount() != 0) {
		var msg = this.getAt(0).get("message");
		if (msg != "normal") {
			Ext.MessageBox.show({
				msg : msg,
				width : 300,
				buttons : Ext.MessageBox.OK
			});
			this.removeAll();
		} else {
			businessStatResultGrid.getStore().each(function(record) {
				// // 區域顯示
				// for ( var i = 0; i < regionMultSelectData.length; i++) {
				// if (record.get("regionID") == regionMultSelectData[i][0]) {
				// record
				// .set(
				// "regionDisplay",
				// regionMultSelectData[i][1]);
				// }
				// if (record.get("regionID") == "SUM") {
				// record
				// .set(
				// "regionDisplay",
				// "");
				// }
				// }

				// 提交，去掉修改標記
				record.commit();
			});
		}
	}
});

businessStatResultWin = new Ext.Window({
	title : "统计结果",
	width : 1200,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : businessStatResultGrid,
	buttons : [ {
		text : "退出",
		handler : function() {
			isPrompt = false;
			businessStatResultWin.hide();
		}
	} ],
	listeners : {
		"show" : function(thiz) {
			businessStatResultGrid.getStore().reload({
				params : {
					start : 0,
					limit : businessStaticRecordCount
				}
			});
		}
	}
});

// -----------------------------------------------------------------------------
businessStatWin = new Ext.Window({
	title : "营业统计",
	width : 450,
	height : 101,
	closeAction : "hide",
	resizable : false,
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
					id : "businessStaticBeginDate",
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
					id : "businessStaticEndDate",
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
					businessStatWin.hide();

					// 保存条件
					var dateFormated = new Date();
					businessStaticBeginDate = businessStatWin.findById(
							"businessStaticBeginDate").getValue();
					if (businessStaticBeginDate != "") {
						dateFormated = businessStaticBeginDate;
						businessStaticBeginDate = dateFormated.format('Y-m-d');
						businessStaticBeginDate = businessStaticBeginDate
								+ " 00:00:00";
					}

					businessStaticEndDate = businessStatWin.findById(
							"businessStaticEndDate").getValue();
					if (businessStaticEndDate != "") {
						dateFormated = businessStaticEndDate;
						businessStaticEndDate = dateFormated.format('Y-m-d');
						businessStaticEndDate = businessStaticEndDate
								+ " 23:59:59";
					}

					isPrompt = true;
					businessStatResultWin.show();

				}
			}, {
				text : "取消",
				handler : function() {
					isPrompt = false;
					businessStatWin.hide();
				}
			} ],
	listeners : {
		"show" : function(thiz) {

			businessStatWin.findById("businessStaticBeginDate").setValue("");
			businessStatWin.findById("businessStaticEndDate").setValue("");

		}
	}
});