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
	imgPath : '../../images/UserLogout.png',
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
			readOnly : true,
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
			fieldLabel : '类型',
			readOnly : true,
			forceSelection : true,
			width : 200,
			store : new Ext.data.JsonStore({
				fields : [ 'id', 'name' ],
				root : 'root',
				data : restaurantData
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
			readOnly : true,
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
						type : type.getValue(),
						rid : restaurant.getValue(),
						title : title.getValue(),
						desc : desc.getValue(),
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
			width : 300,
			modal : true,
			resiza : false,
			closable : false,
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
					restaurant.setValue(data['restaurant']['id']);
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
	initWinBillboard();
	
	winBillboard.otype = c.otype;
	
	if(c.otype == 'insert'){
		winBillboard.initData.clear();
		winBillboard.show();
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
		
	}
}

function loadRestaurantData(){
	Ext.Ajax.request({
		url : '../../QueryRestaurants.do',
		params : {
			isCookie : true
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
			['公告内容', 'desc', '', '', 'function(v){ return v.substring(0, 10) + "..."; }'],
			['发布时间', 'createdFormat', 100],
			['过期时间', 'expiredFormat', 100],
			['操作', 'operation', 100, 'center', 'bbGridOperateRenderer']
		],
		['id', 'title', 'desc', 'created', 'createdFormat', 'expired', 'expiredFormat', 'typeVal', 'typeDesc', 'restaurant'],
		[['isPaging', true], ['dataSource', 'normal']],
		20,
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