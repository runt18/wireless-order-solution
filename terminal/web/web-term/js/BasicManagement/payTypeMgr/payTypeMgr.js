var payTypeWin, payTypeOperatePanel, payTypeGrid;

//付款方式管理
function payTypeRenderer (v, m, r, ri, ci, s){
	if(r.get('typeValue') == 2){
		return '----';
	}else{
		return ''
			   + '<a href="javascript:updatePayTypeHandler()">修改</a>'
			   + '&nbsp;&nbsp;&nbsp;&nbsp;'
			   + '<a href="javascript:deletePayTypeHandler()">删除</a>';	
	}

}

function updatePayTypeHandler(){
	payTypeOperationHandler({
		type : bmObj.operation['update']
	});
}

function deletePayTypeHandler(){
	payTypeOperationHandler({
		type : bmObj.operation['delete']
	});
}

function payTypeOperationHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	payTypeOperatePanel.otype = c.type;
	
	if(c.type == bmObj.operation['insert']){
		oPayTypeData({
			type : bmObj.operation['set']
		});
		payTypeOperatePanel.setTitle('添加付款方式');
		payTypeOperatePanel.show();
		payTypeWin.syncSize();
		payTypeWin.doLayout();
	}else if(c.type == bmObj.operation['update']){
		var sd = Ext.ux.getSelData(payTypeGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个付款方式再进行操作.');
			payTypeOperatePanel.hide();
			payTypeWin.doLayout();
			return;
		}
		oPayTypeData({
			type : bmObj.operation['set'],
			data : sd
		});
		payTypeOperatePanel.setTitle('修改付款方式');
		payTypeOperatePanel.show();
		payTypeWin.syncSize();
		payTypeWin.doLayout();
	}else if(c.type == bmObj.operation['delete']){
		var sd = Ext.ux.getSelData(payTypeGrid);
		if(!sd){
			Ext.example.msg('提示', '请选中一个付款方式再进行操作.');
			payTypeOperatePanel.hide();
			payTypeWin.doLayout();
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除付款方式: ' + sd.name ,
			buttons :Ext.Msg.YESNO,
			icon: Ext.MessageBox.QUESTION,
			fn : function(btn){
				if(btn == 'yes'){
					Ext.Ajax.request({
						url : '../../OperatePayType.do',
						params : {
							dataSource : 'delete',
							id : sd['id']
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								payTypeGrid.getStore().reload();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}else{
		Ext.example.msg('错误', '未知操作类型, 请联系管理员');
	}
};
function oPayTypeData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var id = Ext.getCmp('numPayTypeID');
	var name = Ext.getCmp('txtPayType');
	if(c.type == bmObj.operation['set']){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		id.setValue(data['id']);
		name.setValue(data['name']);
	}else if(c.type == bmObj.operation['get']){
		data = {
			id : id.getValue(),
			name : name.getValue()
		};
		c.data = data;
	}
	name.clearInvalid();
	name.focus(true, 100);
	return c;
};
function initPayTypeWin(){
	
	if(!payTypeWin){
		var payTypeGridTbar = new Ext.Toolbar({
			height : 26,
			items : ['->', {
				text : '添加',
				iconCls : 'btn_add',
				handler : function(){
					payTypeOperationHandler({
						type : bmObj.operation['insert']
					});
				}
			}, '-', {
				text : '修改',
				iconCls : 'btn_edit',
				handler : function(){
					updatePayTypeHandler();
				}
			}, '-', {
				text : '删除',
				iconCls : 'btn_delete',
				handler : function(){
					deletePayTypeHandler();
				}
			}]
		});
		payTypeGrid = createGridPanel(
			'payTypeGrid',
			'',
			'',
			'',
			'../../OperatePayType.do',
			[
				[true, false, false, false], 
				['名称', 'name'],
				['操作', 'operation', 60, 'center', 'payTypeRenderer']
			],
			['id', 'name', 'typeValue'],
			[['dataSource', 'getByCond'], ['designed', 'true'], ['extra', 'true']],
			0,
			'',
			payTypeGridTbar
		);
		payTypeGrid.region = 'center';
		
		payTypeOperatePanel = new Ext.Panel({
			title : '&nbsp;',
			hidden : true,
			frame : true,
			region : 'south',
			layout : 'column',
			autoHeight : true,
			defaults : {
				xtype : 'form',
				layout : 'form',
				labelWidth : 35
			},
			items : [{
				columnWidth : 1,
				items : [{
					xtype : 'textfield',
					id : 'txtPayType',
					width : 260,
					fieldLabel : '名称',
					allowBlank : false,
					blankText : '名称不能为空.',
					validator : function(v){
						if(Ext.util.Format.trim(v).length > 0){
							return true;
						}else{
							return '名称不能为空.';
						}
					}
				}]
			}, {
	//			columnWidth : .4,
				hidden : true,
				items : [{
					xtype : 'numberfield',
					id : 'numPayTypeID',
					fieldLabel : '编号',
					width : 60,
					disabled : true
				}]
			}
			],
			buttonAlign : 'center',
			buttons : [{
				text : '保存',
				handler : function(){
					var txtPayType = Ext.getCmp('txtPayType');
					if(!txtPayType.isValid()){
						return;
					}
					var payTypeData = oPayTypeData({
						type :  bmObj.operation['get']
					}).data;
					
					var action='../../OperatePayType.do', params={};
					
					if(payTypeOperatePanel.otype == bmObj.operation['insert']){
						params.dataSource = 'insert';
					}else if(payTypeOperatePanel.otype == bmObj.operation['update']){
						params.dataSource = 'update';
					}else{
						return;
					}
					
					params.id = payTypeData['id'];
					params.name = payTypeData['name'];
					
					Ext.Ajax.request({
						url : action,
						params : params,
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								payTypeGrid.getStore().reload();
								Ext.getCmp('btnClosePayType').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '取消',
				id : 'btnClosePayType',
				handler : function(){
					payTypeOperatePanel.hide();
					payTypeWin.doLayout();
				}
			}]
		});
		
		payTypeWin = new Ext.Window({
			title : '付款方式管理',
			modal : true,
			resizable : false,
			closable : false,
			draggable : false,
			width : 350,
			height : 390,
			layout : 'border',
			items : [payTypeGrid, payTypeOperatePanel],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					payTypeWin.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					payTypeWin.hide();
				}
			}],
			listeners : {
				beforeshow : function(){
					payTypeOperatePanel.hide();
				}
			}
		});	
	}
	

};


function getPayType(){
	Ext.Ajax.request({
		url : "../../OperatePayType.do",
		params : {
			dataSource : 'getByCond',
			designed : true,
			extra : true
		},
		success : function(response){
			var jr = Ext.util.JSON.decode(response.responseText);
			shop_payType = jr.root;
			initPayTypeWin();
			payTypeWin.show();
			payTypeGrid.getStore().loadData(jr);			
		},
		failure : function(){
		
		}
	});
}