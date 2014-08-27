wx.sales.btnInsert = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddForBigBar.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加促销信息',
	handler : function(btn){
		wx.sales.operateSalseData({otype:'insert'});
	}
});

wx.sales.win = null;
wx.sales.operateSalseData = function(c){
	if(!wx.sales.win){
		var id = new Ext.form.Hidden({
			value : 0
		});
		var title = new Ext.form.TextField({
			fieldLabel : '标题',
			width : 425,
			allowBlank : false
		});
		var edit = new Ext.form.HtmlEditor({
			fieldLabel : '公告内容',
			width : 430,
			height : 420,
			allowBlank : false,
			enableAlignments: false,
	        enableColors: true,
	        enableFont: false,
	        enableFontSize: true,
	        enableFormat: true,
	        enableLinks: false,
	        enableLists: false,
	        enableSourceEdit: true,
	        plugins : [new Ext.ux.plugins.HEInsertImage({
	        	url : '../../WXOperateMaterial.do?dataSource=upload&time=' + new Date().getTime()
	        })]
		});
		var btnPreview = new Ext.Button({
			text : '预览',
			listeners : {
				render : function(thiz){
					thiz.getEl().setWidth(100, true);
				}
			},
			handler : function(){
				if(!title.isValid() || !edit.isValid()){
					return;
				}
				
				center.body.update('<div style="text-align:center; font-size: 30px; font-weight: bold; word-wrap:break-word; color: #D2691E;">' + title.getValue() + '</div>'
					+ edit.getValue()
				);
			}
		});
		var btnClear = new Ext.Button({
			text : '清空',
			listeners : {
				render : function(thiz){
					thiz.getEl().setWidth(100, true);
				}
			},
			handler : function(){
				id.setValue(0);
				title.setValue();
				edit.setValue();
				center.body.update(edit.getValue());
				
				id.clearInvalid();
				title.clearInvalid();
				edit.clearInvalid();
			}
		});
		var btnSave = new Ext.Button({
			text : '保存',
			listeners : {
				render : function(thiz){
					thiz.getEl().setWidth(100, true);
				}
			},
			handler : function(){
				if(!title.isValid() || !edit.isValid()){
					return;
				}
				wx.lm.show();
				Ext.Ajax.request({
					url : '../../OperateBillboard.do',
					params : {
						dataSource : wx.sales.win.otype,
						type : 3,
						id : id.getValue(),
						title : title.getValue(),
						desc : edit.getValue(),
						rid : restaurantID,
						expired : new Date().getTime()
					},
					success : function(res, opt){
						wx.lm.hide();
						var jr = Ext.decode(res.responseText);
						Ext.ux.showMsg(jr);
						if(jr.success){
							wx.sales.win.hide();
							Ext.getCmp('wx.sales.grid.btnSearch').handler();
						}
					},
					fialure : function(res, opt){
						wx.lm.hide();
						Ext.ux.showMsg(res.responseText);
					}
				});
			}
		});
		var btnClose = new Ext.Button({
			text : '关闭',
			listeners : {
				render : function(thiz){
					thiz.getEl().setWidth(100, true);
				}
			},
			handler : function(){
				wx.sales.win.hide();
			}
		});
		var west = new Ext.form.FormPanel({
			region : 'west',
			width : 510,
			labelWidth : 60, 
			items : [id, title, edit],
			buttonAlign : 'center',
			buttons : [btnPreview, btnClear, btnSave, btnClose]
		});
		var center = new Ext.Panel({
			region : 'center',
			style : 'background-color: #fff; border: 1px solid #ccc; padding: 5px 5px 5px 5px;',
			bodyStyle : 'overflow-y: auto; word-wrap:break-word;',
			html : '&nbsp;'
		});
		
		wx.sales.win = new Ext.Window({
			title : '&nbsp;',
			closable : false,
			resizeble : false,
			modal : true,
			width : 850,
			items : [{
				layout : 'border',
				frame : true,
				border : false,
				height : 500,
				items : [west, center]
			}],
			listeners : {
				hide : function(thiz){
					wx.sales.clearValue();
				}
			}
		});
		
		wx.sales.setValue = function(ds){
			id.setValue(ds.id);
			title.setValue(ds.title);
			edit.setValue(ds.desc);
			btnPreview.handler();
			edit.focus();
		};
		wx.sales.clearValue = function(){
			btnClear.handler();
		};
	}
	
	wx.sales.win.otype = c.otype;
	if(c.otype == 'insert'){
		
		wx.sales.win.show();
	}else if(c.otype == 'update'){
		var data = Ext.ux.getSelData(wx.sales.grid);
		if(!data){
			Ext.example.show('提示', '请选中一条记录.');
			return;
		}
		wx.sales.win.show();
		
		wx.sales.setValue({
			id : data['id'],
			title : data['title'],
			desc : data['desc']
		});
	}else if(c.otype == 'delete'){
		
	}
		
};
wx.sales.gridDescRenderer = function(v){
	return v.length > 50 ? v.replace(/<\/?[^>]*>/g,"").substring(0,50) + "..." : v.replace(/<\/?[^>]*>/g,"");
};
wx.sales.gridOperateRenderer = function(){
	return '<a href="javascript:wx.sales.operateSalseData({otype:\'update\'});">修改</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:wx.sales.operateSalseData({otype:\'delete\'});">删除</a>';
};
wx.sales.grid = createGridPanel(
	'wx.sales.grid',
	'',
	'',
	'',
	'../../QueryBillboard.do',
	[
		[true, false, false, true], 
		['发布日期', 'expiredFormat', 50],
		['标题', 'title', 80],
		['内容', 'desc',,,'wx.sales.gridDescRenderer'],
		['操作', 'operate', 30, 'center', 'wx.sales.gridOperateRenderer']
	],
	['id', 'expired', 'expiredFormat', 'title', 'desc'],
	[ ['isPaging', true], ['rid', restaurantID], ['dataSource', 'WXSales']],
	GRID_PADDING_LIMIT_20,
	'',
	new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '搜索',
			id : 'wx.sales.grid.btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				var gs = wx.sales.grid.getStore();
				gs.load({
					params : {
						start : 0,
						limit : wx.sales.grid.getBottomToolbar().pageSize
					}
				});
			}
		}, {
			text : '添加',
			iconCls : 'btn_add',
			handler : function(thiz){
				wx.sales.operateSalseData({otype:'insert'});
			}
		}]
	})
);
wx.sales.grid.region = 'center';
wx.sales.grid.on('render', function(){
	Ext.getCmp('wx.sales.grid.btnSearch').handler();
});
wx.sales.grid.on('rowdblclick', function(){
	wx.sales.operateSalseData({otype:'update'});
});


Ext.onReady(function(){
	
	new Ext.Panel({
		renderTo : 'divSalesMainView',
		layout : 'fit',
		height : parseInt(Ext.getDom('divSalesMainView').parentElement.style.height.replace(/px/g,'')),
		border : false,
		items : [{
			layout : 'border',
			border : false,
			items : [wx.sales.grid]
		}],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    wx.sales.btnInsert
			]
		}),
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				
			}
		}]
	});
});