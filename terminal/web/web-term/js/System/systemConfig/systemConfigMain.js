Ext.onReady(function() {
			var tailForm = new Ext.Panel({
				// region : "center",
				anchor : "right 65%",
				title : "<div style='font-size:20px;'>结账尾数处理</div>",
				contentEl : "tail"
			});

			var printForm = new Ext.Panel({
				// region : "south",
				// height : 100,
				anchor : "right 35%",
				title : "<div style='font-size:20px;'>账单打印</div>",
				contentEl : "print"
			});

			var centerPanel = new Ext.Panel({
				region : "center",
				layout : "border",
				frame : true,
				items : [
				// {
				// region : "center",
				// layout : "border",
				// border : false,
				// items : [ tailForm, printForm ]
				// }
				{
					region : "center",
					layout : "anchor",
					border : false,
					items : [ tailForm, printForm ]
				}, {
					layout : "form",
					region : "south",
					buttonAlign : "center",
					height : 60,
					itesm : [ {} ],
					buttons : [ {
						text : "保存"
					}, {
						text : "返回"
					} ]
				}, {
					region : "north",
					border : false,
					// frame : true,
					height : 60,
					contentEl : "title"
				} ]
			});

			var viewport = new Ext.Viewport(
					{
						layout : "border",
						id : "viewport",
						items : [
								{
									region : "north",
									html : "<div style='padding:10px; background-color:#DFE8F6;'><h4 style='font-size:150%'>无线点餐网页终端<h4></div>",
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
