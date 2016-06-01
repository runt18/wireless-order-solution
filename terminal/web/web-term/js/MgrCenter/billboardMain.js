var btnGetBack = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = '../MgrCenter.html';
	}
});
var btnLogout = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});

var btnAddBillboard = new Ext.ux.ImageButton({
	imgPath : '../../images/AddCount.png',
	imgWidth : 50,
	imgHeight : 50,
	handler : function(btn){
		operateBillboard({otype:'insert'});
	}
});


function initWinBillboard(){
	if(!winBillboard){
		var id = new Ext.form.Hidden({value:0});
		var title = new Ext.form.TextField({
			fieldLabel : '标题',
			allowBlank : false,
			width : 200
		});
		var desc = new  Ext.form.TextArea({
			fieldLabel : '内容',
			allowBlank : false,
			width : 200,
			height : 130
		});
		var type = new Ext.form.ComboBox({
			fieldLabel : '类型',
			readOnly : false,
			forceSelection : true,
			width : 200,
			value : 0,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[1, '系统公告'], [2, '餐厅通知']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(thiz){
					if(thiz.getValue() == 2){
						restaurant.setDisabled(false);
					}else{
						restaurant.setDisabled(true);
						restaurant.setValue();
					}
				}
			}
		});
		var restaurant = new Ext.form.ComboBox({
			disabled : true,
			fieldLabel : '餐厅选择',
			readOnly : false,
			forceSelection : true,
			width : 200,
			store : new Ext.data.JsonStore({
				fields : [ 'id', 'name' ],
				root : 'root',
			}),
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true
		});
		var expired = new Ext.form.DateField({
			fieldLabel : '过期时间',
			width : 200,
			readOnly : false,
			allowBlank : false,
			minValue : new Date(),
			format : 'Y-m-d'
		});
		var btnSave = new Ext.Button({
			text : '保存',
			iconCls : 'btn_save',
			handler : function(){
				if(!title.isValid() || !desc.isValid() || !expired.isValid()){
					return;
				}
				if(type.getValue() == 2 && !restaurant.isValid()){
					return;
				}
				
				Ext.Ajax.request({
					url : '../../OperateBillboard.do',
					params : {
						dataSource : winBillboard.otype,
						id : id.getValue(),
						rid : restaurant.getValue(),
						title : title.getValue(),
						desc : edit.getValue(),
						expired : expired.getValue().getTime()
					},
					success : function(response, options){
						var jr = Ext.decode(response.responseText);
						Ext.ux.showMsg(jr);
						if(jr.success){
							winBillboard.hide();
							Ext.getCmp('btnSearchForBillboardGrid').handler();
						}
					},
					failure : function(response, options){
						Ext.ux.showMsg(Ext.decode(response.responseText));
					}
				});
				
			}
		});
		var btnClose = new Ext.Button({
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				winBillboard.hide();
			}
		});
		
		winBillboard = new Ext.Window({
			title : '&nbsp;',
			width : 800,
			height : 700,
			modal : true,
			resiza : false,
			closable : false,
			contentEl : 'divOperateBillboard',
			items : [{
				xtype : 'form',
				layout : 'form',
				labelWidth : 60,
				frame : true,
				items : [id, type, restaurant, title, desc, expired]
			}],
			bbar : ['->', btnSave, btnClose],
			listeners : {
				show : function(thiz){
					thiz.center();
					restaurant.store.loadData(restaurantData);
				}
			}
		});
		winBillboard.initData = {
			set : function(data){
				data = data == null ? {} : data;
				id.setValue(data['id']);
				if(data['typeVal'] === 2){
					type.setValue(data['typeVal']);
					type.fireEvent('select', type);
					restaurant.setValue(data['restaurant']);
				}else{
					type.setValue(1);
					type.fireEvent('select', type);
				}
				
				title.setValue(data['title']);
				desc.setValue(data['desc']);
				expired.setValue(data['expired'] > 0 ? new Date(data['expired']) : undefined);					
				
				title.clearInvalid();
				desc.clearInvalid();
				expired.clearInvalid();
			},
			clear : function(){
				this.set({});
			}
		};
	}
	return winBillboard;
}

function operateBillboard(c){
//	initWinBillboard();
	operateWXInfo();
	
	winBillboard.otype = c.otype;
	
	if(c.otype == 'insert'){
		winBillboard.initData.clear();
		winBillboard.show();
		Ext.getCmp('title4Billboard').focus();
	}else if(c.otype == 'update'){
		var data = Ext.ux.getSelData(billboradGrid);
		if(!data){
			Ext.ux.showMsg({
				msg : '请选中一条记录再进行操作.'
			});
			return;
		}
		winBillboard.initData.set(data);
		winBillboard.show();
	}else if(c.otype == 'delete'){
		var data = Ext.ux.getSelData(billboradGrid);
		if(!data){
			Ext.ux.showMsg({
				msg : '请选中一条记录再进行操作.'
			});
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除公告信息?(不可恢复)',
			buttons : Ext.Msg.YESNO,
			fn : function(btn){
				if(btn == 'yes'){
					var mask = new Ext.LoadMask(document.body, {
						msg : '操作请求中, 请稍候...'
					});
					mask.show();
					Ext.Ajax.request({
						url : '../../OperateBillboard.do',
						params : {
							dataSource : 'delete',
							id : data['id']
						},
						success : function(response, options){
							mask.hide();
							var jr = Ext.decode(response.responseText);
							Ext.ux.showMsg(jr);
							if(jr.success){
								Ext.getCmp('btnSearchForBillboardGrid').handler();
							}
						},
						failure : function(response, options){
							mask.hide();
							Ext.ux.showMsg(Ext.decode(response.responseText));
						}
					});
				}
			}
		});
	}
}

function loadRestaurantData(){
	Ext.Ajax.request({
		url : '../../OperateRestaurant.do',
		params : {
			dataSource : 'getByCond'
		},
		success : function(response, options){
			var jr = Ext.decode(response.responseText);
			if(jr.success){
				restaurantData = jr;
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response, options){
			Ext.ux.showMsg(Ext.decode(response.responseText));
		}
	});
}

function bbGridOperateRenderer(){
	return ''
		   + '<a href="javascript:operateBillboard({otype:\'update\'})">修改</a>'
		   + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
		   + '<a href="javascript:operateBillboard({otype:\'delete\'})">删除</a>';
};

function bbGridOperateRenderer2(v){
	return v.substring(0, 10) + "..."; 
}

function operateWXInfo(){
	if(!winBillboard){
		var id = new Ext.form.Hidden({value:0});
		var title = new Ext.form.TextField({
			id : 'title4Billboard',
			fieldLabel : '标题',
			allowBlank : false,
			width : 200
		});
		var type = new Ext.form.ComboBox({
			fieldLabel : '类型',
			readOnly : false,
			forceSelection : true,
			width : 200,
			value : 1,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[1, '系统公告'], [2, '餐厅通知']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(thiz){
					if(thiz.getValue() == 2){
						restaurant.setDisabled(false);
					}else{
						restaurant.setDisabled(true);
						restaurant.setValue();
					}
				}
			}
		});
		var restaurant = new Ext.form.ComboBox({
			disabled : true,
			fieldLabel : '餐厅选择',
			readOnly : false,
			forceSelection : true,
			width : 200,
			store : new Ext.data.JsonStore({
				fields : [ 'id', 'name' ],
				root : 'root',
			}),
			valueField : 'id',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true
		});
		
		var expired = new Ext.form.DateField({
			fieldLabel : '过期时间',
			width : 200,
			readOnly : false,
			allowBlank : false,
			minValue : new Date(),
			format : 'Y-m-d'
		});		
		
		var edit = new Ext.form.HtmlEditor({
			fieldLabel : '餐厅简介',
			hideLabel : true,
			width : 500,
			height : 600,
			enableAlignments: false,
	        enableColors: true,
	        enableFont: false,
	        enableFontSize: true,
	        enableFormat: true,
	        enableLinks: false,
	        enableLists: false,
	        enableSourceEdit: true,
//	        fontFamilies: ["宋体", "隶书", "黑体"],
	        plugins : [new Ext.ux.plugins.HEInsertImage({
	        	url : '../../OperateImage.do?dataSource=upload&ossType=8'
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
				center.body.update(edit.getValue());
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
				edit.setValue();
				center.body.update(edit.getValue());
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
				if(!title.isValid() || !expired.isValid()){
					return;
				}
				if(type.getValue() == 2 && !restaurant.isValid()){
					return;
				}
				
				Ext.Ajax.request({
					url : '../../OperateBillboard.do',
					params : {
						dataSource : winBillboard.otype,
						id : id.getValue(),
						rid : restaurant.getValue(),
						title : title.getValue(),
						desc : edit.getValue(),
						expired : expired.getValue()
					},
					success : function(response, options){
						var jr = Ext.decode(response.responseText);
						Ext.ux.showMsg(jr);
						if(jr.success){
							winBillboard.hide();
							Ext.getCmp('btnSearchForBillboardGrid').handler();
						}
					},
					failure : function(response, options){
						Ext.ux.showMsg(Ext.decode(response.responseText));
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
				winBillboard.hide();
			}
		});
		var west = new Ext.form.FormPanel({
			region : 'west',
			width : 510,
			items : [id, title, type, restaurant, expired, edit],
			buttonAlign : 'center',
			buttons : [btnPreview, btnClear, btnSave, btnClose]
		});
		var center = new Ext.Panel({
			id : 'showDivHtml',
			region : 'center',
			style : 'background-color: #fff; border: 1px solid #ccc; padding: 5px 5px 5px 5px;',
			bodyStyle : 'overflow-y: auto; word-wrap:break-word;',
			html : '&nbsp;'
		});
		
		winBillboard = new Ext.Window({
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
				show : function(){
				}
			}
		});
		
		winBillboard.initData = {
				set : function(data){
					data = data == null ? {} : data;
					id.setValue(data['id']);
					if(data['typeVal'] === 2){
						type.setValue(data['typeVal']);
						type.fireEvent('select', type);
						restaurant.setValue(data['restaurant']);
					}else{
						type.setValue(1);
						type.fireEvent('select', type);
					}
					
					title.setValue(data['title']);
					expired.setValue(data['expired'] > 0 ? new Date(data['expired']) : undefined);					
					edit.setValue(data['desc']);
					
					title.clearInvalid();
					expired.clearInvalid();
				},
				clear : function(){
					this.set({});
				}
			};
	}
}



Ext.onReady(function(){
	loadRestaurantData();
	
	var billboradGridTbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '搜索',
			id : 'btnSearchForBillboardGrid',
			iconCls : 'btn_search',
			handler : function(){
				var gs = billboradGrid.getStore();
				gs.load({
					params : {
						start : 0,
						limit : 20
					}
				});
			}
		}]
	});
	billboradGrid = createGridPanel(
		'billboradGrid',
		'',
		'',
		'',
		'../../QueryBillboard.do',
		[
			[true, false, true, true], 
			['类型', 'typeDesc', 50, 'center'],
			['公告标题', 'title', 100],
			['公告内容', 'desc', '', '', bbGridOperateRenderer2],
			['发布时间', 'createdFormat', 100],
			['过期时间', 'expiredFormat', 100],
			['操作', 'operation', 100, 'center', bbGridOperateRenderer]
		],
		['id', 'title', 'desc', 'created', 'createdFormat', 'expired', 'expiredFormat', 'typeVal', 'typeDesc', 'restaurant'],
		[['isPaging', true], ['dataSource', 'normal']],
		'',
		'',
		billboradGridTbar
	);
	billboradGrid.on('rowdblclick', function(){
		operateBillboard({otype:'update'});
	});
	var center = new Ext.Panel({
		region : 'center',
		frame : true,
		autoScroll : true,
		layout : 'fit',
		items : [billboradGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
				btnAddBillboard,
				'->',
				btnGetBack, 
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    btnLogout 
			]
		})
	});
	
	initMainView(null, center, null);
	
});