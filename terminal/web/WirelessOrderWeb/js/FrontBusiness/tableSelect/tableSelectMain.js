﻿// *************整体布局*************
var dishesOrderImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/InsertOrder.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "点菜",
	handler : function(btn) {
		if (selectedTable != "") {
			var tableIndex = -1;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (tableStatusListTSDisplay[i].tableAlias == selectedTable) {
					tableIndex = i;
				}
			}
			if (tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_BUSY) {
				var category = "X";
				var tableNbr = "XXX";
				var tableNbr2 = "XXX";
				if (tableStatusListTSDisplay[tableIndex].tableCategory == CATE_NORMAL) {
					category = CATE_NORMAL;
					tableNbr = selectedTable;
					tableNbr2 = "0";
				} else if (tableStatusListTSDisplay[tableIndex].tableCategory == CATE_TAKE_OUT) {
					category = CATE_TAKE_OUT;
					tableNbr = selectedTable;
					tableNbr2 = "0";
				} else if (tableStatusListTSDisplay[tableIndex].tableCategory == CATE_JOIN_TABLE) {
					category = CATE_JOIN_TABLE;
					tableNbr = selectedTable;
					tableNbr2 = "0";
				} else if (tableStatusListTSDisplay[tableIndex].tableCategory == CATE_MERGER_TABLE) {
					category = CATE_MERGER_TABLE;
					var tblArray = getMergeTable(selectedTable);
					tableNbr = tblArray[0];
					tableNbr2 = tblArray[1];
				}

				var minCost;
				var serviceRate;
				if (category != CATE_MERGER_TABLE) {
					minCost = tableStatusListTSDisplay[tableIndex].tableMinCost;
					serviceRate = tableStatusListTSDisplay[tableIndex].tableServiceRate;
				} else {
					minCost = getMaxMinCostMT(selectedTable);
					serviceRate = getMaxSerRateMT(selectedTable);
				}

				location.href = "OrderMain.html?tableNbr="
						+ tableNbr
						+ "&personCount="
						+ tableStatusListTSDisplay[tableIndex].tableCustNbr
						+ "&tableStat=used" + "&category="
						+ category + "&tableNbr2="
						+ tableNbr2 + "&pin=" + pin
						+ "&restaurantID=" + restaurantID
						+ "&minCost=" + minCost
						+ "&serviceRate=" + serviceRate;
			} else if (tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_IDLE) {
				// for forward the page
				// 只有空台才要输入人数，只有“一般”类型才有空台，固定category为1
				// default person count 1
				location.href = "OrderMain.html?tableNbr="
						+ selectedTable
						+ "&personCount=1"
						+ "&tableStat=free"
						+ "&category="
						+ CATE_NORMAL
						+ "&tableNbr2=0"
						+ "&pin="
						+ pin
						+ "&restaurantID="
						+ restaurantID
						+ "&minCost="
						+ tableStatusListTSDisplay[tableIndex].tableMinCost
						+ "&serviceRate="
						+ tableStatusListTSDisplay[tableIndex].tableServiceRate;

			}
		}
	}
});

var checkOutImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/PayOrder.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "结账",
	handler : function(btn) {
		if (selectedTable != "") {
			var tableIndex = -1;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (tableStatusListTSDisplay[i].tableAlias == selectedTable) {
					tableIndex = i;
				}
			}
			if (tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_IDLE) {
				Ext.Msg.alert("", "<b>此桌没有下单，不能结账！</b>");
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

				location.href = "CheckOut.html?"
						+ "tableID=" + selectedTable
						+ "&pin=" + pin 
						+ "&restaurantID=" + restaurantID;
//						+ "&personCount=" + tableStatusListTSDisplay[tableIndex].tableCustNbr
//						+ "&minCost=" + minCost 
//						+ "&serviceRate=" + serviceRate;
			}
		}
	}
});
				
var orderDeleteImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/DeleteOrder.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "删单",
	handler : function(btn) {
		if (selectedTable != "") {
			var tableIndex = -1;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (tableStatusListTSDisplay[i].tableAlias == selectedTable) {
					tableIndex = i;
				}
			}
			if (tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_IDLE) {
				Ext.Msg.alert("", "<b>此桌没有下单，不能删单！</b>");
			} else {
				dishPushBackWin.show();
			}
		}
	}
});				

var tableChangeImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/TableChange.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "转台",
	handler : function(btn) {
		if (selectedTable != "") {
			var tableIndex = -1;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (tableStatusListTSDisplay[i].tableAlias == selectedTable) {
					tableIndex = i;
				}
			}
			if (tableStatusListTSDisplay[tableIndex].tableStatus == TABLE_BUSY
					&& tableStatusListTSDisplay[tableIndex].tableCategory == CATE_NORMAL) {
				tableChangeWin.show();
			} else {
				if (tableStatusListTSDisplay[tableIndex].tableStatus != TABLE_BUSY) {
					Ext.Msg.alert("", "<b>空台不能转台！</b>");
				} else {
					Ext.Msg.alert("", "<b>该台不允许转台！</b>");
				}
			}
		}
	}
});				

var tableMergeImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/TableMerage.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "拼台",
	handler : function(btn) {
		alert(btn.tooltip)
		if (selectedTable != "") {
			
		}
	}
});				

var tableSepImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/TableSeparate.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "并台",
	handler : function(btn) {
		alert(btn.tooltip)
	}
});				

var packageImgBut = new Ext.ux.ImageButton({
	imgPath : "../../images/Package.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "合并结账",
	handler : function(btn) {
		oOrderGroup();
	}
});		
		
var selTabContentGrid = null;
var selTabContentWin = null;
var btnOrderDetail = new Ext.ux.ImageButton({
	imgPath : '../../images/TableOrderDetail.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '查看明细',
	handler : function(e) {
		if (selectedTable != '') {
			var selTabContent = null;
			for ( var i = 0; i < tableStatusListTSDisplay.length; i++) {
				if (tableStatusListTSDisplay[i].tableAlias == selectedTable) {
					selTabContent = tableStatusListTSDisplay[i];
				}
			}
			if(parseInt(selTabContent.tableStatus) != 1){
				return;
			}
			
			var pageSize = 300;
			var cmData = [[true,false,false],['日期','order_date',150],['名称','food_name',150],['单价','unit_price',80, 'right', 'Ext.ux.txtFormat.gridDou'],
			              ['数量','amount', 80, 'right', 'Ext.ux.txtFormat.gridDou'], ['折扣','discount',80, 'right', 'Ext.ux.txtFormat.gridDou'],['口味','taste_pref'],
			              ['口味价钱','taste_price',80, 'right', 'Ext.ux.txtFormat.gridDou'],['厨房','kitchen'],['服务员','waiter']];
			var url = '../../QueryDetail.do?tiem=' + new Date();
			var readerData = ['order_date','food_name','unit_price','amount','discount','taste_pref','taste_price','kitchen','waiter','comment','isPaid','isDiscount','isGift','isReturn','message'];
			var baseParams = [['pin', pin], ['queryType', 'TodayByTbl'], ['tableAlias', selTabContent.tableAlias], ['restaurantID', restaurantID]];							
			var id = 'selTabConten_grid';
			var title = '';
			var height = 400;
			var width = '';
			var groupName = '';
			if(!selTabContentGrid){					
				selTabContentGrid = createGridPanel(id,title,height,width,url,cmData,readerData,baseParams,pageSize,groupName,null);
				selTabContentGrid.bbar = '';
				selTabContentGrid.getStore().on('load', function(store, records, options){
					for(var i = 0; i < records.length; i++){
						if(eval(records[i].get('amount') < 0)){
							var sumRow = selTabContentGrid.getView().getRow(i);
							sumRow.style.backgroundColor = '#FF0000';
						}
					}
				});
			}
			
			if(!selTabContentWin){
				selTabContentWin = new Ext.Window({
					title : '',
					resizable : false,
					closable : false,
					constrainHeader : true,
//					modal : true,
//					draggable : false,
					width : 1150,
					items : [selTabContentGrid],
					buttons : [
					{
						text : '关闭',
						handler : function(){
							selTabContentWin.hide();
						}
					}
					]
				});
			}
			
			selTabContentWin.setTitle(String.format('餐台号:&nbsp;<font color="red">{0}</font>&nbsp;&nbsp;&nbsp;餐台名:&nbsp;<font color="red">{1}</font>&nbsp;&nbsp;&nbsp;用餐人数:&nbsp;<font color="red">{2}</font>'
					,selectedTable, selTabContent.tableName, selTabContent.tableCustNbr));
			selTabContentWin.show(true);
			var tpStore = selTabContentGrid.getStore();
			tpStore.baseParams.tableAlias = selTabContent.tableAlias;
			tpStore.baseParams.pin = pin;
			tpStore.baseParams.queryType = 'TodayByTbl';
			tpStore.baseParams.restaurantID = restaurantID;
			tpStore.baseParams.tableAlias = selTabContent.tableAlias;
			tpStore.load({
				params : {
					limit : pageSize,
					start : 0
				}
			});
			
		}
	}
});		
		
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		location.href = "FrontBusinessProtal.html?restaurantID="
				+ restaurantID + "&pin=" + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn) {
	}
});	

getIsPaidDisplay = function(_val){
	return eval(_val) == true ? '是' : '否';
};

var regionTree, dishPushBackWin, tableChangeWin;
Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
	Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
	Ext.QuickTips.init();
	
	// table change pop window
	tableChangeWin = new Ext.Window({
		layout : "fit",
		width : 200,
		height : 100,
		closeAction : "hide",
		closable : false,
		resizable : false,
		items : [{
			layout : "form",
			labelWidth : 30,
			border : false,
			frame : true,
			items : [{
				xtype : "textfield",
				fieldLabel : "转至",
				id : "tableChangeInput",
				width : 110
			}]
		}],
		buttons : [{
			text : "确定",
			id : 'btnTableChange',
			handler : function() {
				var inputTableNbr = tableChangeWin.findById("tableChangeInput").getValue();
				if (inputTableNbr != "") {
					var tableIndex = -1;
					for( var i = 0; i < tableStatusListTS.length; i++){
						if (tableStatusListTS[i].tableAlias == inputTableNbr) {
							tableIndex = i;
						}
					}
					
					if(tableIndex == -1){
						Ext.Msg.alert("", "<b>您输入的台号不存在！</b>");
					}else if(tableStatusListTS[tableIndex].tableStatus == TABLE_BUSY) {
						Ext.Msg.alert("", "<b>您输入的台号为就餐状态，不能转台！</b>");
					} else {
						tableChangeWin.hide();
						Ext.Ajax.request({
							url : "../../TransTable.do",
							params : {
								"pin" : pin,
								"oldTableAlias" : selectedTable,
								"newTableAlias" : inputTableNbr
							},
							success : function(response, options) {
								var resultJSON = Ext.decode(response.responseText);
								if (resultJSON.success == true){
									Ext.MessageBox.show({
										msg : resultJSON.data,
										width : 300,
										buttons : Ext.MessageBox.OK,
										fn : function() {
											getData();
										}
									});
								} else {
									Ext.MessageBox.show({
										msg : resultJSON.data,
										width : 300,
										buttons : Ext.MessageBox.OK,
										fn : function(){
											location.reload();
										}
									});
								}
							},
							failure : function(response, options) {
								
							}
						});
					}
				}
			}
		}, {
			text : "取消",
			handler : function(){
				tableChangeWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnTableChange').handler();
			}
		}],
		listeners : {
			show : function(thiz){
				tableChangeWin.findById("tableChangeInput").setValue("");
				var f = Ext.get("tableChangeInput");
				f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
			}
		}
	});
	
	// ***************tableSelectNorthPanel******************
	// soft key board
	var softKBKeyHandlerTS = function(relateItemId, number) {
		var currValue = tableSelectNorthPanel.findById(relateItemId).getValue();
		tableSelectNorthPanel.findById(relateItemId).setValue(currValue + "" + number);
	};
	
	softKeyBoardTS = new Ext.Window({
		layout : "fit",
		width : 117,
		height : 118,
		closeAction : "hide",
		resizable : false,
		// closable : false,
		x : 56,
		y : 146,
		items : [{
			layout : "form",
			labelSeparator : '：',
			labelWidth : 40,
			frame : true,
			buttonAlign : "left",
			items : [{
				layout : "column",
				border : false,
				items : [{
					layout : "form",
					width : 30,
					border : false,
					items : [{
						text : "1",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "1");
							tableKeyboardSelect();
						}
					}]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [{
						text : "2",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "2");
							tableKeyboardSelect();
						}
					}]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [{
						text : "3",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "3");
							tableKeyboardSelect();
						}
					}]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [{
						text : "4",
						xtype : "button",
						handler : function(){
							softKBKeyHandlerTS("tableNumber", "4");
							tableKeyboardSelect();
						}
					}]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [{
						text : "5",
						xtype : "button",
						handler : function(){
							softKBKeyHandlerTS("tableNumber", "5");
							tableKeyboardSelect();
						}
					}]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [ {
						text : "6",
						xtype : "button",
						handler : function(){
							softKBKeyHandlerTS("tableNumber", "6");
							tableKeyboardSelect();
						}
					} ]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [ {
						text : "7",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "7");
							tableKeyboardSelect();
						}
					} ]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [ {
						text : "8",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "8");
							tableKeyboardSelect();
						}
					} ]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [ {
						text : "9",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "9");
							tableKeyboardSelect();
						}
					} ]
				}, {
					layout : "form",
					width : 30,
					border : false,
					items : [ {
						text : "0",
						xtype : "button",
						handler : function() {
							softKBKeyHandlerTS("tableNumber", "0");
							tableKeyboardSelect();
						}
					} ]
				}, {
					layout : "form",
					width : 60,
					border : false,
					items : [ {
						text : "&nbsp;清 空&nbsp;",
						xtype : "button",
						handler : function() {
							tableSelectNorthPanel.findById("tableNumber").setValue("");
							tableKeyboardSelect();
						}
					} ]
				} ]
			} ]
		} ],
		listeners : {
			show : function(thiz) {
				var f = Ext.get("tableNumber");
				f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
			}
		}
	});
	
	dishPushBackWin = new Ext.Window({
		layout : "fit",
		width : 200,
		height : 100,
		closeAction : "hide",
		closable : false,
		resizable : false,
		items : [ {
			layout : "form",
			labelWidth : 30,
			border : false,
			frame : true,
			items : [ {
				xtype : "textfield",
				inputType : "password",
				fieldLabel : "密码",
				id : "dishPushBackPwd",
				width : 110
				} ]
		} ],
		buttons : [{
			text : "确定",
			id : 'btnDeleteTableOrder',
			handler : function() {
				var dishPushBackPwd = Ext.getCmp("dishPushBackPwd").getValue();
				Ext.getCmp("dishPushBackPwd").setValue("");
				var pwdTrans
				if (dishPushBackPwd != "") {
					pwdTrans = MD5(dishPushBackPwd);
				} else {
					pwdTrans = dishPushBackPwd;
				}
				dishPushBackWin.hide();
				Ext.Ajax.request({
					url : "../../VerifyPwd.do",
					params : {
						"pin" : Request["pin"],
						"type" : 1,
						"pwd" : pwdTrans
					},
					success : function(response, options) {
						var resultJSON = Ext.decode(response.responseText);
						if (resultJSON.success == true) {
							Ext.Ajax.request({
								url : "../../CancelOrder.do",
								params : {
									"pin" : pin,
									"tableAlias" : selectedTable
								},
								success : function(response, options) {
									var resultJSON1 = Ext.decode(response.responseText);
									if (resultJSON1.success == true) {
										Ext.MessageBox.show({
											msg : resultJSON1.data,
											width : 300,
											buttons : Ext.MessageBox.OK,
											fn : function() {
												location.reload();
											}
										});
									} else {
										Ext.MessageBox.show({
											msg : resultJSON.data,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
									}
								},
								failure : function(response, options) {
									
								}
							});
							
							Ext.MessageBox.show({
								msg : resultJSON.data,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						} else {
							Ext.MessageBox.show({
								msg : resultJSON.data,
								width : 300,
								buttons : Ext.MessageBox.OK
							});
						}
					},
					failure : function(response, options){
						
					}
				});
			}
		}, {
			text : "取消",
			handler : function() {
				dishPushBackWin.hide();
				dishPushBackWin.findById("dishPushBackPwd").setValue("");
			}
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnDeleteTableOrder').handler();
			}
		}],
		listeners : {
			show : function(thiz) {
				var f = Ext.get("dishPushBackPwd");
				f.focus.defer(100, f); // 万恶的EXT！为什么这样才可以！？！？
			}
		}
	});	

	var tableSelectNorthPanel = new Ext.form.FormPanel({
		region : "north",
		frame : true,
		height : 45,
		labelSeparator : '：',
		labelWidth : 30,
		border : false,
		items : [ {
			border : false,
			layout : "form",
			// style : "padding-top:10px;padding-left:20px;",
			items : [ {
				layout : "column",
				border : false,
				anchor : '98%',
				labelSeparator : '：',
				items : [ {
					layout : "form",
					width : 185,
					labelWidth : 30,
					style : "padding-top:7px;padding-left:15px;",
					border : false,
					items : [ {
						xtype : "numberfield",
						fieldLabel : "<b>桌号</b>",
						name : "tableNumber",
						id : "tableNumber",
						anchor : "90%",
						listeners : {
							focus : function(thiz) {
								softKeyBoardTS.show();
							}
						}
					} ]
				}, {
					width : 800,
					contentEl : "tableSumInfo"
				} ]
			} ]
		} ]
	});

	// ***************tableSelectCenterPanel******************
	var tableSelectCenterPanel = new Ext.Panel({
		region : "center",
		layout : "border",
		border : false,
		bodyStyle : "background-color:#d8ebef;",
		items : [ {
			region : "center",
			border : false,
			// bodyStyle : "background-color:#d8ebef;padding-top:4%",
			bodyStyle : "background-color:#d8ebef;",
			contentEl : "tableDisplay",
			autoScroll : true
		}, {
			region : "south",
			border : false,
			bodyStyle : "background-color:#d8ebef;",
			height : 60,
			contentEl : "tableListPageCount"
		}, {
			region : "north",
			border : false,
			bodyStyle : "background-color:#d8ebef;",
			height : 40,
			contentEl : "tableListRegionInfo"
		} ]

	});			

	// ***************tableSelectEastPanel******************
	var regionTreeRoot = new Ext.tree.AsyncTreeNode({
		id : "regionTreeRoot",
		text : "全部区域",
		loader : new Ext.tree.TreeLoader({
			url : "../../QueryRegion.do",
			listeners : {
				"beforeload" : function(treeloader, node) {
					treeloader.baseParams = {
						"pin" : pin,
						"isPaging" : false,
						"isCombo" : false,
						"isTree" : true
					};
				}
			}
		})
	});			

	regionTree = new Ext.tree.TreePanel({
		id : 'regionTree',
		autoScroll : true,
		animate : true,
		root : regionTreeRoot,
		rootVisible : true,
		border : false,
		lines : true,
		collapsed : false,
		containerScroll : true,
		listeners : {
			"click" : function(node, event) {
				selectedStatus = null;
				node.attributes.tableStatus = null;
				tableListReflash(node);
			}
		}
	});			

	var tableSelectWestPanel = new Ext.Panel({
		region : "west",
		title : "区域",
		width : 160,
		border : true,
		margins : '0 0 0 5',
		collapsible : true,
		collapsed : false,
		titleCollapse : true,
		split : true,
		items : regionTree
	});			

	var centerTabPanel = new Ext.Panel({
		region : "center",
		tbar : new Ext.Toolbar({
			height : 55,
			items : [ {
				text : "&nbsp;&nbsp;&nbsp;",
				xtype : 'tbtext'
			}, 
			dishesOrderImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'}, 
			checkOutImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'}, 
			orderDeleteImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'}, 
			tableChangeImgBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'}, 
//			tableMergeImgBut, 
//			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext'},
//			tableSepImgBut, 
//			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' },
			packageImgBut,
			{ xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;'},
			btnOrderDetail,
			"->",
			pushBackBut, 
			{text : "&nbsp;&nbsp;&nbsp;", xtype : 'tbtext' }, 
			logOutBut ]
		}),
		layout : "border",
		items : [ tableSelectNorthPanel, tableSelectCenterPanel, tableSelectWestPanel]
	});

	new Ext.Viewport({
		layout : "border",
		id : "viewport",
		items : [{
			region : "north",
			bodyStyle : "background-color:#DFE8F6;",
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},
		centerTabPanel,
		{
			region : "south",
			height : 30,
			layout : "form",
			frame : true,
			border : false,
			html : "<div style='font-size:11pt; text-align:center;'><b>版权所有(c) 2011 智易科技</b></div>"
		} ]
	});
});
