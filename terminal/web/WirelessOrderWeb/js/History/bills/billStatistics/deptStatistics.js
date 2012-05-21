var deptMultSelectDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "displayText" ],
	data : []
});

deptStatWin = new Ext.Window(
		{
			title : "部门统计",
			width : 450,
			height : 380,
			closeAction : "hide",
			resizable : false,
			layout : "anchor",
			items : [ {
				border : false,
				anchor : "right 10%",
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
							format : "Y-m-d",
							id : "begDateMStatDept",
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
							format : "Y-m-d",
							id : "endDateMStatDept",
							width : 150,
							fieldLabel : "至"
						} ]
					} ]
				} ]
			}, {
				layout : "form",
				border : false,
				frame : true,
				hideLabels : true,
				anchor : "right 91%",
				items : [ {
					xtype : "itemselector",
					name : "deptMultSelectMStat",
					id : "deptMultSelectMStat",
					fromStore : deptMultSelectDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 250,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择部门",
					fromLegend : "可选择部门"
				} ]
			} ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = deptStatWin.findById("deptMultSelectMStat").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							deptStatWin.findById("deptMultSelectMStat")
									.fromTo();
							deptStatWin.findById("deptMultSelectMStat").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							deptStatWin.findById("deptMultSelectMStat").reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = deptStatWin
									.findById("deptMultSelectMStat").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								deptStatWin.hide();

								var selectDepts = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = deptStatWin
											.findById("deptMultSelectMStat").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectDepts = selectDepts + selectItem
											+ ",";
									// }
								}
								// 去掉最后一个逗号
								selectDepts = selectDepts.substring(0,
										selectDepts.length - 1);

								// 保存条件
								var dateFormated = new Date();
								deptStaticBeginDate = deptStatWin.findById(
										"begDateMStatDept").getValue();
								if (deptStaticBeginDate != "") {
									dateFormated = deptStaticBeginDate;
									deptStaticBeginDate = dateFormated
											.format('Y-m-d');
									deptStaticBeginDate = deptStaticBeginDate
											+ " 00:00:00";
								}

								deptStaticEndDate = deptStatWin.findById(
										"begDateMStatDept").getValue();
								if (deptStaticEndDate != "") {
									dateFormated = deptStaticEndDate;
									deptStaticEndDate = dateFormated
											.format('Y-m-d');
									deptStaticEndDate = deptStaticEndDate
											+ " 00:00:00";
								}

								deptStaticString = selectDepts;

								isPrompt = true;
								deptStatResultWin.show();

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个部门进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							deptStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

					deptStatWin.findById("deptMultSelectMStat").reset();
					deptStatWin.findById("begDateMStatDept").setValue("");
					deptStatWin.findById("endDateMStatDept").setValue("");

					deptMultSelectDS.loadData(deptMultSelectData);
				}
			}
		});

// 结果框
// 前台：[日期，廚房名稱，現金，銀行卡，會員卡，掛賬，簽單，合計]
var deptStatResultStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../deptStatistics.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "deptID"
	}, {
		name : "deptDisplay"
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
		name : "total"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var deptStatResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "名称",
			sortable : true,
			dataIndex : "deptDisplay",
			width : 100
		}, {
			header : "现金（￥）",
			sortable : true,
			dataIndex : "cash",
			width : 80
		}, {
			header : "银行卡（￥）",
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
			header : "折扣（￥）",
			sortable : true,
			dataIndex : "discount",
			width : 80
		}, {
			header : "赠送（￥）",
			sortable : true,
			dataIndex : "gift",
			width : 80
		}, {
			header : "合计（￥）",
			sortable : true,
			dataIndex : "total",
			width : 100
		} ]);

var deptStatResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : deptStatResultStore,
	cm : deptStatResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	viewConfig : {
		forceFit : true
	},
	bbar : new Ext.PagingToolbar({
		pageSize : deptStaticRecordCount,
		store : deptStatResultStore,
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
deptStatResultGrid.getStore().on('beforeload', function() {

	// 输入查询条件参数
	this.baseParams = {
		"pin" : pin,
		"deptIDs" : deptStaticString,
		"dateBegin" : deptStaticBeginDate,
		"dateEnd" : deptStaticEndDate,
		"StatisticsType" : "History"
	};

});

// 为store配置load监听器(即load完后动作)
deptStatResultGrid.getStore().on('load', function() {
	if (deptStatResultGrid.getStore().getTotalCount() != 0) {
		var msg = this.getAt(0).get("message");
		if (msg != "normal") {
			Ext.MessageBox.show({
				msg : msg,
				width : 300,
				buttons : Ext.MessageBox.OK
			});
			this.removeAll();
		} else {
			deptStatResultGrid.getStore().each(function(record) {
				// 部門顯示
				for ( var i = 0; i < deptMultSelectData.length; i++) {
					if (record.get("deptID") == deptMultSelectData[i][0]) {
						record.set("deptDisplay", deptMultSelectData[i][1]);
					}
					if (record.get("deptID") == "SUM") {
						record.set("deptDisplay", "");
					}
				}

				// 提交，去掉修改標記
				record.commit();
			});
		}
	}
});

deptStatResultWin = new Ext.Window({
	title : "统计结果",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : deptStatResultGrid,
	buttons : [ {
		text : "退出",
		handler : function() {
			isPrompt = false;
			deptStatResultWin.hide();
		}
	} ],
	listeners : {
		"show" : function(thiz) {
			deptStatResultGrid.getStore().reload({
				params : {
					start : 0,
					limit : deptStaticRecordCount
				}
			});
		}
	}
});
