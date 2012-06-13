var supplierMultSelectDS = new Ext.data.SimpleStore({
	fields : [ "retrunValue", "supplierAlias", "displayText" ],
	data : []
});

supplierStatWin = new Ext.Window(
		{
			title : "供应商统计",
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
							id : "begDateMStatSupplier",
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
							id : "endDateMStatSupplier",
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
					name : "supplierMultSelectMStat",
					id : "supplierMultSelectMStat",
					fromStore : supplierMultSelectDS,
					dataFields : [ "retrunValue", "displayText" ],
					toData : [ [ "", "" ] ],
					msHeight : 250,
					valueField : "retrunValue",
					displayField : "displayText",
					imagePath : "../../extjs/multiselect/images/",
					toLegend : "已选择供应商",
					fromLegend : "可选择供应商"
				} ]
			} ],
			buttons : [
					{
						text : "全选",
						handler : function() {
							var i = supplierStatWin
									.findById("supplierMultSelectMStat").fromMultiselect.view;
							i.selectRange(0, i.store.length);
							supplierStatWin.findById("supplierMultSelectMStat")
									.fromTo();
							supplierStatWin.findById("supplierMultSelectMStat").toMultiselect.view
									.clearSelections();
						}
					},
					{
						text : "清空",
						handler : function() {
							supplierStatWin.findById("supplierMultSelectMStat")
									.reset();
						}
					},
					{
						text : "确定",
						handler : function() {

							var selectCount = supplierStatWin
									.findById("supplierMultSelectMStat").toMultiselect.store
									.getCount();

							if (selectCount != 0) {
								isPrompt = false;
								supplierStatWin.hide();

								var selectSuppliers = "";
								for ( var i = 0; i < selectCount; i++) {
									var selectItem = supplierStatWin
											.findById("supplierMultSelectMStat").toMultiselect.store
											.getAt(i).get("retrunValue");
									// if (selectItem != "") {
									selectSuppliers = selectSuppliers
											+ selectItem + ",";
									// }
								}
								// 去掉最后一个逗号
								selectSuppliers = selectSuppliers.substring(0,
										selectSuppliers.length - 1);

								// 保存条件
								var dateFormated = new Date();
								supplierStaticBeginDate = supplierStatWin
										.findById("begDateMStatSupplier")
										.getValue();
								if (supplierStaticBeginDate != "") {
									dateFormated = supplierStaticBeginDate;
									supplierStaticBeginDate = dateFormated
											.format('Y-m-d');
								}

								supplierStaticEndDate = supplierStatWin
										.findById("endDateMStatSupplier")
										.getValue();
								if (supplierStaticEndDate != "") {
									dateFormated = supplierStaticEndDate;
									supplierStaticEndDate = dateFormated
											.format('Y-m-d');
								}

								supplierStaticString = selectSuppliers;

								isPrompt = true;
								supplierStatResultWin.show();

							} else {
								Ext.MessageBox.show({
									msg : "至少需要选择一个供应商进行统计",
									width : 300,
									buttons : Ext.MessageBox.OK
								});
							}
						}
					}, {
						text : "取消",
						handler : function() {
							isPrompt = false;
							supplierStatWin.hide();
						}
					} ],
			listeners : {
				"show" : function(thiz) {

				
					supplierStatWin.findById("supplierMultSelectMStat").reset();
					supplierStatWin.findById("begDateMStatSupplier").setValue(
							"");
					supplierStatWin.findById("endDateMStatSupplier").setValue(
							"");

					supplierMultSelectDS.loadData(supplierData);
				}
			}
		});

// 结果框
// 前台：[日期，廚房名稱，現金，銀行卡，會員卡，掛賬，簽單，合計]
var supplierStatResultStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../supplierStatistics.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "supplierID"
	}, {
		name : "supplierName"
	}, {
		name : "inAmount"
	}, {
		name : "returnAmount"
	}, {
		name : "payAmount"
	}, {
		name : "message"
	} ])
});

// 2，栏位模型
var supplierStatResultColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(), {
			header : "供应商",
			sortable : true,
			dataIndex : "supplierName",
			width : 100
		}, {
			header : "进货金额",
			sortable : true,
			dataIndex : "inAmount",
			width : 80
		}, {
			header : "退货金额",
			sortable : true,
			dataIndex : "returnAmount",
			width : 80
		}, {
			header : "应付金额",
			sortable : true,
			dataIndex : "payAmount",
			width : 80
		} ]);

var supplierStatResultGrid = new Ext.grid.GridPanel({
	xtype : "grid",
	anchor : "99%",
	border : false,
	ds : supplierStatResultStore,
	cm : supplierStatResultColumnModel,
	sm : new Ext.grid.RowSelectionModel({
		singleSelect : true
	}),
	viewConfig : {
		forceFit : true
	},
	bbar : new Ext.PagingToolbar({
		pageSize : supplierStaticRecordCount,
		store : supplierStatResultStore,
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
supplierStatResultGrid.getStore().on('beforeload', function() {

	// 输入查询条件参数
	this.baseParams = {
		"pin" : pin,
		"supplierIDs" : supplierStaticString,
		"dateBegin" : supplierStaticBeginDate,
		"dateEnd" : supplierStaticEndDate,
		"StatisticsType" : "History"
	};

});

// 为store配置load监听器(即load完后动作)
supplierStatResultGrid.getStore().on('load', function() {
	if (supplierStatResultGrid.getStore().getTotalCount() != 0) {
		var msg = this.getAt(0).get("message");
		if (msg != "normal") {
			Ext.MessageBox.show({
				msg : msg,
				width : 300,
				buttons : Ext.MessageBox.OK
			});
			this.removeAll();
		} else {
			supplierStatResultGrid.getStore().each(function(record) {
				// 供應商顯示
				for ( var i = 0; i < supplierData.length; i++) {
					if (record.get("supplierID") == supplierData[i][0]) {
						record.set("supplierName", supplierData[i][2]);
					}
					if (record.get("supplierID") == "SUM") {
						record.set("supplierName", "汇总");
					}
				}

				// 提交，去掉修改標記
				record.commit();
			});
		}
	}
});

supplierStatResultWin = new Ext.Window({
	title : "统计结果",
	width : 800,
	height : 370,
	closeAction : "hide",
	resizable : false,
	layout : "fit",
	items : supplierStatResultGrid,
	buttons : [ {
		text : "打印",
		handler : function() {

		}
	}, {
		text : "退出",
		handler : function() {
			isPrompt = false;
			supplierStatResultWin.hide();
		}
	} ],
	listeners : {
		"show" : function(thiz) {
			supplierStatResultGrid.getStore().reload({
				params : {
					start : 0,
					limit : supplierStaticRecordCount
				}
			});
		}
	}
});
