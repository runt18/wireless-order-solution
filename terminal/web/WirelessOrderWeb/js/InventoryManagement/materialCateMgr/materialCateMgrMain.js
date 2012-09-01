﻿// --
var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn) {
		var isChange = false;
		materialCateGrid.getStore().each(function(record) {
			if (record.isModified("cateName") == true) {
				isChange = true;
			}
		});

		if (isChange) {
			Ext.MessageBox.show({
				msg : "修改尚未保存，是否确认返回？",
				width : 300,
				buttons : Ext.MessageBox.YESNO,
				fn : function(btn) {
					if (btn == "yes") {
						location.href = "InventoryProtal.html?restaurantID="
								+ restaurantID + "&pin=" + pin;
					}
				}
			});
		} else {
			location.href = "InventoryProtal.html?restaurantID=" + restaurantID
					+ "&pin=" + pin;
		}
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

// 1，表格的数据store
var materialCateStore = new Ext.data.Store({
	proxy : new Ext.data.HttpProxy({
		url : "../../QueryMaterialCate.do"
	}),
	reader : new Ext.data.JsonReader({
		totalProperty : "totalProperty",
		root : "root"
	}, [ {
		name : "cateID"
	}, {
		name : "cateName"
	}, {
		name : "message"
	} ])
});

// menuStore.reload();

// 2，栏位模型
var materialCateColumnModel = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		// {
		// header : "编号",
		// sortable : true,
		// dataIndex : "cateID",
		// width : 80
		// },
		{
			header : "名称",
			sortable : true,
			dataIndex : "cateName",
			width : 100,
			editor : new Ext.form.TextField({
				allowBlank : false
			})
		} ]);

// -------------- layout ---------------
var materialCateGrid;
Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ---------------------表格--------------------------
			materialCateGrid = new Ext.grid.EditorGridPanel({
				// title : "部门",
				xtype : "grid",
				anchor : "99%",
				region : "center",
				frame : true,
				margins : '0 5 0 0',
				ds : materialCateStore,
				cm : materialCateColumnModel,
				sm : new Ext.grid.RowSelectionModel({
					singleSelect : true
				}),
				viewConfig : {
					forceFit : true
				},
				listeners : {
					"rowclick" : function(thiz, rowIndex, e) {
						currRowIndex = rowIndex;
					}
				},
				tbar : [ {
					text : '保存修改',
					tooltip : '保存修改',
					iconCls : 'save',
					handler : function() {

						// 修改記錄格式:id field_separator name
						// field_separator
						// phone field_separator contact field_separator
						// address
						// record_separator id field_separator name
						// field_separator phone field_separator contact
						// field_separator address
						var modfiedArr = [];
						materialCateGrid.getStore().each(
								function(record) {
									if (record.isModified("cateName") == true) {
										modfiedArr.push(record.get("cateID")
												+ " field_separator "
												+ record.get("cateName"));
									}
								});

						if (modfiedArr.length != 0) {
							// 獲取分頁表格的當前頁碼！神技！！！
							var toolbar = materialCateGrid.getBottomToolbar();
							currPageIndex = toolbar.readPage(toolbar
									.getPageData());

							var modCates = "";
							for ( var i = 0; i < modfiedArr.length; i++) {
								modCates = modCates + modfiedArr[i]
										+ " record_separator ";
							}
							modCates = modCates.substring(0,
									modCates.length - 18);

							Ext.Ajax.request({
								url : "../../UpdateMaterialCate.do",
								params : {
									"pin" : pin,
									"modCates" : modCates
								},
								success : function(response, options) {
									var resultJSON = Ext.util.JSON
											.decode(response.responseText);
									if (resultJSON.success == true) {
										materialCateStore.reload({
											params : {
												start : (currPageIndex - 1)
														* pageRecordCount,
												limit : pageRecordCount
											}
										});

										var dataInfo = resultJSON.data;
										Ext.MessageBox.show({
											msg : dataInfo,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
									} else {
										var dataInfo = resultJSON.data;
										Ext.MessageBox.show({
											msg : dataInfo,
											width : 300,
											buttons : Ext.MessageBox.OK
										});
									}
								},
								failure : function(response, options) {
								}
							});
						}
					}
				} ],
				bbar : new Ext.PagingToolbar({
					pageSize : pageRecordCount,
					store : materialCateStore,
					displayInfo : true,
					displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
					emptyMsg : "没有记录"
				}),
				autoScroll : true,
				loadMask : {
					msg : "数据加载中，请稍等..."
				},
				listeners : {
					"render" : function(thiz) {
						materialCateStore.reload({
							params : {
								start : 0,
								limit : pageRecordCount
							}
						});
					},
					"rowclick" : function(thiz, rowIndex, e) {
						currRowIndex = rowIndex;
					}
				}
			});

			// 为store配置beforeload监听器
			materialCateGrid.getStore().on('beforeload', function() {

				// 输入查询条件参数
				this.baseParams = {
					"pin" : pin,
					"isPaging" : true,
					"isCombo" : false
				};

			});

			// 为store配置load监听器(即load完后动作)
			materialCateGrid.getStore().on('load', function() {
				if (materialCateGrid.getStore().getTotalCount() != 0) {
					var msg = this.getAt(0).get("message");
					if (msg != "normal") {
						Ext.MessageBox.show({
							msg : msg,
							width : 300,
							buttons : Ext.MessageBox.OK
						});
						this.removeAll();
					} else {
					}
				}
			});
			// ---------------------end 表格--------------------------

			var centerPanel = new Ext.Panel({
				region : "center",
				layout : "fit",
				frame : true,
				items : [ {
					layout : "border",
					title : "<div style='font-size:20px;'>食材种类管理<div>",
					items : materialCateGrid
				} ],
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ "->", pushBackBut, {
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
									bodyStyle : "background-color:#DFE8F6;",
									html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
									height : 50,
									border : false,
									margins : '0 0 0 0'
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
