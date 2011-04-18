Ext.onReady(function() {
	// 解决ext中文传入后台变问号问题
		Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
		Ext.QuickTips.init();

		// person count input pop window
		personCountInputWin = new Ext.Window(
				{
					layout : "fit",
					width : 177,
					height : 100,
					closeAction : "hide",
					resizable : false,
					items : [ {
						layout : "form",
						labelWidth : 30,
						border : false,
						frame : true,
						items : [ {
							xtype : "numberfield",
							fieldLabel : "人数",
							id : "personCountInput",
							width : 110
						} ]
					} ],
					buttons : [
							{
								text : "确定",
								handler : function() {
									var inputCount = personCountInputWin
											.findById("personCountInput")
											.getValue();
									if (inputCount != 0 && inputCount != "") {
										personCountInputWin.hide();

										// update data
										var tableIndex = -1;
										for ( var i = 0; i < tableStatusListTS.length; i++) {
											if (tableStatusListTS[i][0] == selectedTable) {
												tableIndex = i;
											}
										}
										tableStatusListTS[tableIndex][1] = inputCount;
										tableStatusListTS[tableIndex][2] = "占用";

										// update status output
										document
												.getElementById("perCountDivTS").innerHTML = inputCount
												+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
										document
												.getElementById("tblStatusDivTS").innerHTML = "占用";

										// for forward the page
										location.href = "OrderMain.html?tableNbr="
												+ selectedTable
												+ "&personCount=" + inputCount;

									}
								}
							}, {
								text : "取消",
								handler : function() {
									personCountInputWin.hide();
								}
							} ]
				});

		// ***************tableSelectNorthPanel******************
		// soft key board
		var softKBKeyHandlerTS = function(relateItemId, number) {
			var currValue = tableSelectNorthPanel.findById(relateItemId)
					.getValue();
			tableSelectNorthPanel.findById(relateItemId).setValue(
					currValue + "" + number);
		};

		softKeyBoardTS = new Ext.Window( {
			layout : "fit",
			width : 177,
			height : 100,
			closeAction : "hide",
			resizable : false,
			closable : false,
			x : 41,
			y : 146,
			items : [ {
				layout : "form",
				labelSeparator : '：',
				labelWidth : 40,
				frame : true,
				buttonAlign : "left",
				items : [
				// {
				// xtype : "numberfield",
				// fieldLabel : "桌号",
				// width : 85
				// },
				{
					layout : "column",
					border : false,
					items : [
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "1",
									xtype : "button",
									handler : function() {
										softKBKeyHandlerTS("tableNumber", "1");
										tableKeyboardSelect();
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "2",
									xtype : "button",
									handler : function() {
										softKBKeyHandlerTS("tableNumber", "2");
										tableKeyboardSelect();
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "3",
									xtype : "button",
									handler : function() {
										softKBKeyHandlerTS("tableNumber", "3");
										tableKeyboardSelect();
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "4",
									xtype : "button",
									handler : function() {
										softKBKeyHandlerTS("tableNumber", "4");
										tableKeyboardSelect();
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "5",
									xtype : "button",
									handler : function() {
										softKBKeyHandlerTS("tableNumber", "5");
										tableKeyboardSelect();
									}
								} ]
							},
							{
								layout : "form",
								width : 30,
								border : false,
								items : [ {
									text : "6",
									xtype : "button",
									handler : function() {
										softKBKeyHandlerTS("tableNumber", "6");
										tableKeyboardSelect();
									}
								} ]
							},
							{
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
							},
							{
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
							},
							{
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
							},
							{
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
							},
							{
								layout : "form",
								width : 60,
								border : false,
								items : [ {
									text : "&nbsp;确 认&nbsp;",
									xtype : "button",
									handler : function() {
										softKeyBoardTS.hide();
									}
								} ]
							},
							{
								layout : "form",
								width : 60,
								border : false,
								items : [ {
									text : "&nbsp;清 空&nbsp;",
									xtype : "button",
									handler : function() {
										tableSelectNorthPanel.findById(
												"tableNumber").setValue("");
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

		var tableSelectNorthPanel = new Ext.form.FormPanel( {
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
						width : 237,
						labelWidth : 30,
						style : "padding-top:7px;",
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
					},
					// {
							// width : 80,
							// border : false,
							// // layout : "fit",
							// style : "padding-top:7px;",
							// items : [ {
							// xtype : "button",
							// // style : "margin-left:7px;",
							// tooltip : "软键盘",
							// width : 30,
							// text : "K",
							// listeners : {
							// "click" : function() {
							// softKeyBoardTS.show();
							// }
							// }
							// } ]
							// },
							{
								width : 800,
								contentEl : "tableSumInfo"
							} ]
				} ]
			} ]
		});

		// ***************tableSelectCenterPanel******************
		var tableSelectCenterPanel = new Ext.Panel( {
			region : "center",
			layout : "fit",
			bodyStyle : "background-color:#d8ebef;padding-top:4%",
			contentEl : "tableDisplay",
			autoScroll : true
		});

		// ***************tableSelectSouthPanel******************
		var tableSelectSouthPanel = new Ext.Panel(
				{
					region : "south",
					layout : "fit",
					height : 35,
					border : false,
					bodyStyle : "background-color:#d8ebef;padding-left:20px;",
					contentEl : "tableStatusTS"
				});

		// *************整体布局*************
		var dishesOrderImgBut = new Ext.ux.ImageButton( {
			imgPath : "../images/im48x48.png",
			imgWidth : 50,
			imgHeight : 50,
			tooltip : "点菜",
			handler : function(btn) {
				if (selectedTable != "") {
					var tableIndex = -1;
					for ( var i = 0; i < tableStatusListTS.length; i++) {
						if (tableStatusListTS[i][0] == selectedTable) {
							tableIndex = i;
						}
					}
					location.href = "OrderMain.html?tableNbr=" + selectedTable
							+ "&personCount="
							+ tableStatusListTS[tableIndex][1];
				}
			}
		});

		var checkOutImgBut = new Ext.ux.ImageButton( {
			imgPath : "../images/extlogo48.png",
			imgWidth : 50,
			imgHeight : 50,
			tooltip : "结账",
			handler : function(btn) {
				if (selectedTable != "") {
					var tableIndex = -1;
					for ( var i = 0; i < tableStatusListTS.length; i++) {
						if (tableStatusListTS[i][0] == selectedTable) {
							tableIndex = i;
						}
					}
					if (tableStatusListTS[tableIndex][2] == "空桌") {
						Ext.Msg.alert("", "<b>此桌没有下单，不能结账！</b>");
					} else {
						location.href = "CheckOut.html?tableNbr="
								+ selectedTable + "&personCount="
								+ tableStatusListTS[tableIndex][1];
					}
				}
			}
		});

		var orderDeleteImgBut = new Ext.ux.ImageButton( {
			imgPath : "../images/im48x48.png",
			imgWidth : 50,
			imgHeight : 50,
			tooltip : "删单",
			handler : function(btn) {
				var tableIndex = -1;
				for ( var i = 0; i < tableStatusListTS.length; i++) {
					if (tableStatusListTS[i][0] == selectedTable) {
						tableIndex = i;
					}
				}
				if (tableStatusListTS[tableIndex][2] == "空桌") {
					Ext.Msg.alert("", "<b>此桌没有下单，不能删单！</b>");
				} else {
					// ...................;
			Ext.Msg.alert("", "<b>删单成功！</b>");
		}
	}
		});

		var centerTabPanel = new Ext.Panel( {
			region : "center",
			tbar : new Ext.Toolbar( {
				height : 55,
				items : [ {
					text : " ",
					disabled : true
				}, dishesOrderImgBut, {
					text : "&nbsp;&nbsp;&nbsp;",
					disabled : true
				}, checkOutImgBut, dishesOrderImgBut, {
					text : "&nbsp;&nbsp;&nbsp;",
					disabled : true
				}, orderDeleteImgBut ]
			}),
			layout : "border",
			border : false,
			items : [ tableSelectNorthPanel, tableSelectCenterPanel,
					tableSelectSouthPanel ]
		});

		var viewport = new Ext.Viewport(
				{
					layout : "border",
					id : "viewport",
					items : [
							{
								region : "north",
								html : "<div style='padding:10px; background-color:#A9D0F5'><h4 style='font-size:150%'>无线点餐网页终端<h4></div>",
								height : 50,
								margins : '0 0 5 0'
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

		// -------------------- 浏览器大小改变 -------------------------------
		// 1,调整colDisplayFormUQ中表格的高度
		// Ext.EventManager.onWindowResize( function() {
		// if (centerTabPanel.getActiveTab().getId() == "dishesOrderTab") {
		// dishesDisplayGrid.setHeight(dishesOrderEastPanel.findById(
		// "dishesOrderForm").getInnerHeight() - 50);
		// }
		// // dataSortGridUQ.setHeight(150);
		// });
	});
