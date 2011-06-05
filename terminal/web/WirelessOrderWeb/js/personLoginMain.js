Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// emplComboData = [ [ "0x1", "一般" ], [ "0x2", "会员" ] ];
			// alert(emplComboData);
			// var emplStore = new Ext.data.SimpleStore({
			// fields : [ "value", "text" ],
			// data : emplComboData
			// });

			var emplStore = new Ext.data.Store({
				proxy : new Ext.data.MemoryProxy(emplComboData),
				reader : new Ext.data.ArrayReader({}, [ {
					name : "value"
				}, {
					name : "text"
				} ])
			});

			var centerPanel = new Ext.Panel(
					{
						region : "center",
						frame : true,
						items : [
								{
									html : "<div>&nbsp;&nbsp;</div>",
									id : "placeHolderCOF1",
									height : 200
								},
								{
									layout : "form",
									frame : true,
									labelSeparator : "：",
									style : "margin:0 auto",
									title : "<div style='font-size:18px;padding-left:2px'>员工登陆<div>",
									collapsible : false,
									buttonAlign : "center",
									labelWidth : 60,
									width : 280,
									height : 140,
									defaults : {
										width : 200
									},
									items : [
											{
												xtype : "combo",
												fieldLabel : "<img src='../images/user.png'/ style='float:left'>&nbsp;姓名",
												id : "empName",
												forceSelection : true,
												// value : "一般",
												store : emplStore,
												valueField : "value",
												displayField : "text",
												typeAhead : true,
												mode : "local",
												triggerAction : "all",
												selectOnFocus : true,
												allowBlank : false
											},
											{
												xtype : "textfield",
												inputType : "password",
												fieldLabel : "<img src='../images/password.png' style='float:left'/>&nbsp;密码",
												id : "empPassword"
											} ],
									buttons : [ {
										text : '提交'

									}, {
										text : '重置'
									} ]
								} ],
						listeners : {
							render : function(thiz) {
								emplStore.reload();
							}
						}
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
