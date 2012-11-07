
memberTypeWinInit = function(){
	if(!memberTypeWin){
		memberTypeWin = new Ext.Window({
			title : 'memberTypeWin',
			closable : false,
			modal : true,
			resizable : false,
			width : 230,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 65,
				defaults : {
					width : 130
				},
				items : [{
					xtype : 'numberfield',
					fieldLabel : '类型编号'
				}, {
					xtype : 'textfield',
					fieldLabel : '类型名称'
				}, {
					xtype : 'numberfield',
					fieldLabel : '充值比率'
				}, {
					xtype : 'numberfield',
					fieldLabel : '积分比率'
				}, {
					xtype : 'combo',
					id : 'comboSearchType222',
					fieldLabel : '折扣方式',
					readOnly : true,
					forceSelection : true,
					value : 0,
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : [[0, '折扣方案'], [1, '全单折扣']]
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					listeners : {
						select : function(thiz, record, index){
							
						}
					}
				}, {
					xtype : 'combo',
					id : 'comboSearchType22f2',
					fieldLabel : '折扣方案',
					readOnly : true,
					forceSelection : true,
					value : 0,
					store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text' ],
						data : []
					}),
					valueField : 'value',
					displayField : 'text',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true
				}, {
					xtype : 'numberfield',
					fieldLabel : '折扣率'
				}]
			}],
			bbar : ['->', {
				text : '保存',
				id : '',
				iconCls : 'btn_save',
				handler : function(e){
					
				}
			}, {
				text : '关闭',
				id : 'asd',
				iconCls : 'btn_close',
				handler : function(e){
					memberTypeWin.hide();
				}
			}]
		});
	}
};

memberTypeInit = function(){
	getOperatorName(pin, '../../');
	memberTypeWinInit();
	
//	Ext.Ajax.request({
//	url : '../../QueryClientTypeTree.do',
//	params : {
//		restaurantID : restaurantID
//	},
//	success : function(res, opt){
//		alert(res.responseText);
//	},
//	failure : function(res, opt){
//		Ext.ux.showMsg(Ext.decode(res.responseText));
//	}
//});
	
};