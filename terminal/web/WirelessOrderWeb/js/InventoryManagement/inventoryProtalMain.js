// ******************************************************************************************************
Ext
		.onReady(function() {
			// 解决ext中文传入后台变问号问题
			Ext.lib.Ajax.defaultPostHeader += '; charset=utf-8';
			Ext.QuickTips.init();

			// ******************************************************************************************************

			var pushBackBut = new Ext.ux.ImageButton({
				imgPath : "../../images/UserLogout.png",
				imgWidth : 50,
				imgHeight : 50,
				tooltip : "返回",
				handler : function(btn) {
					location.href = "../PersonLogin.html?restaurantID="
							+ restaurantID + "&isNewAccess=false&pin="
							+ currPin;
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

			var centerPanel = new Ext.Panel({
				region : "center",
				frame : true,
				autoScroll : true,
				tbar : new Ext.Toolbar({
					height : 55,
					items : [ "->", pushBackBut, {
						text : "&nbsp;&nbsp;&nbsp;",
						disabled : true
					}, logOutBut ]
				}),
				items : [ {
					border : false,
					contentEl : "protal"
				} ]
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
