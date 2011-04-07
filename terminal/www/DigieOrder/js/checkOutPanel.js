// --------------check-out center panel-----------------
// 1，数据
var checkOutData = [];
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);
checkOutData.push( [ "酸菜鱼", "只要酸菜不要鱼", 1, "￥56.2" ]);

checkOutData.push( [ "", "", "", "合计：￥100" ]);

// 2，表格的数据store
var checkOutStore = new Ext.data.Store( {
	proxy : new Ext.data.MemoryProxy(checkOutData),
	reader : new Ext.data.ArrayReader( {}, [ {
		name : "dishName"
	}, {
		name : "dishTaste"
	}, {
		name : "dishCount"
	}, {
		name : "dishPrice"
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
		} ]);

// 4，表格
var checkOutGrid = new Ext.grid.GridPanel( {
	title : "菜式",
	width : 800,
	style : "margin:0 auto",
	// height : 400,
	xtype : "grid",
	//anchor : "99%",
	// height : 500,
	ds : checkOutStore,
	cm : checkOutColumnModel,
	autoExpandColumn : "dishNameCOCM"
});

var checkOutForm = new Ext.form.FormPanel(
		{
			frame : true,
			border : false,
			items : [
					{
						id : "checkOutInfoMsg",
						html : "<br/><font size=3pt,style='padding-bottom:10pt;'>当前台号可以结账</font>"
					}, checkOutGrid ],
			buttons : [ {
				text : "结账",
				handler : function() {
				}
			} ],
			listeners : {
				afterlayout : function(thiz) {
					checkOutGrid.setHeight(thiz.getInnerHeight() - 30);
				}
			}
		});

var checkOutCenterPanel = new Ext.Panel( {
	region : "center",
	id : "checkOutCenterPanel",
	title : "结账",
	layout : "fit",
	items : [ checkOutForm ]
});

// --------------check-out north panel-----------------
var checkOutNorthPanel = new Ext.form.FormPanel( {
	id : "checkOutNorthPanel",
	region : "north",
	height : 100,
	border : false,
	frame : true,
	// autoScroll : true,
	buttonAlign : "left",
	items : [ {
		layout : "column",
		border : false,
		anchor : '98%',
		labelSeparator : '：',
		items : [ {
			layout : "form",
			width : 230,
			labelWidth : 60,
			border : false,
			items : [ {
				xtype : "textfield",
				fieldLabel : "桌号",
				name : "tableNumberCO",
				id : "tableNumberCO",
				anchor : "90%"
			} ]
		}, {
			layout : "form",
			width : 150,
			labelWidth : 40,
			border : false,
			items : [ {
				xtype : "numberfield",
				fieldLabel : "人数",
				name : "personCountCO",
				id : "personCountCO",
				disabled : true,
				anchor : "90%"
			} ]
		} ]
	}, {
		layout : "column",
		border : false,
		anchor : '98%',
		labelSeparator : '：',
		items : [ {
			layout : "form",
			width : 230,
			labelWidth : 60,
			border : false,
			items : [ {
				xtype : "textfield",
				fieldLabel : "下单时间",
				name : "orderTimeCO",
				id : "orderTimeCO",
				disabled : true,
				anchor : "90%"
			} ]
		}, {
			layout : "form",
			width : 150,
			labelWidth : 40,
			border : false,
			items : [ {
				xtype : "textfield",
				fieldLabel : "操作人",
				name : "orderOperatorCO",
				id : "orderOperatorCO",
				disabled : true,
				anchor : "90%"
			} ]
		} ]
	} ],
	buttons : [ {
		text : "提交"
	} ]
});