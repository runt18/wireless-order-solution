var orderGroupWin;
function loadDatqaForOrderGroup(){
	Ext.Ajax.request({
		url : '../../QueryOrderGroup.do',
		params : {
			restaurantID : restaurantID,
			queryType : 0,
			status : 0
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			var cgData = {root:[]}, egData = {root:[]};
			for(var i = 0; i < jr.root.length; i++){
				if(jr.root[i].category == 4){
					for(var k = 0; k < jr.root[i].childOrder.length; k++){
						jr.root[i].childOrder[k].parentID = jr.root[i].id;
						egData.root.push(jr.root[i].childOrder[k]);
					}
				}else{
					jr.root[i].parentID = '';
					cgData.root.push(jr.root[i]);
				}
			}
			Ext.getCmp('westGridPanel').getStore().removeAll();
			Ext.getCmp('centerGridPanel').getStore().loadData(cgData);
			Ext.getCmp('eastGridPanel').getStore().loadData(egData);
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
};

westGridPanelDelete = function(v){
	var gp = Ext.getCmp('westGridPanel');
	gp.getStore().each(function(r){
		if(r.get('tableAlias') == v){
			gp.getStore().remove(r);
			gp.getView().refresh();
			return false;
		}
	});
};
centerGridPanelInsert = function(ri){
	var gp = Ext.getCmp('centerGridPanel');
	gp.fireEvent('rowdblclick', gp, ri);
};
eastGridPanelInsertGroup = function(ri){
	var gp = Ext.getCmp('eastGridPanel');
	gp.fireEvent('rowdblclick', gp, ri);
};

westGridPanelRenderer = function(v, md, r, ri, ci, store){
	return ''
		   + '<a href="javascript:westGridPanelDelete('+r.get('tableAlias')+');">删除</a>';
};
centerGridPanelRenderer = function(v, md, r, ri, ci, store){
	return ''
	   + '<a href="javascript:centerGridPanelInsert('+ri+');">添加</a>';
};
eastGridPanelRenderer = function(v, md, r, ri, ci, store){
	return ''
		   + '<a href="javascript:eastGridPanelInsertGroup('+ri+');">添加该组</a>'
		   + '&nbsp;&nbsp;'
		   + '<a href="javascript:Ext.getCmp(\'btnCancelTableGroup\').handler();">取消该组</a>';
};

function oOrderGroup(){
	if(!orderGroupWin){
		var westGridPanel = createGridPanel(
			'westGridPanel',
			'已选择餐桌',
			'',
			300,
			'../../QueryMember.do',
			[
				[true, false, false, false], 
				['编号', 'tableAlias', 60],
				['名称', 'tableName', 120],
				['操作', 'operation', 60, 'center', 'westGridPanelRenderer']
			],
			['tableID', 'tableAlias', 'tableName'],
			[['isPaging', false], ['restaurantID', restaurantID], ['pin', pin]],
			30,
			''
		);
		westGridPanel.region = 'west';
		
		var centerGridPanel = createGridPanel(
			'centerGridPanel',
			'普通餐桌',
			'',
			'',
			'',
			[
				[true, false, false, false], 
				['编号', 'tableAlias', 60],
				['名称', 'tableName', 120],
				['操作', 'operation', 60, 'center', 'centerGridPanelRenderer']
			],
			['tableID', 'tableAlias', 'tableName'],
			[['isPaging', false], ['restaurantID', restaurantID], ['pin', pin]],
			30,
			''
		);
		centerGridPanel.region = 'center';
		centerGridPanel.on('rowdblclick', function(thiz, ri, e){
			var sr = thiz.getStore().getAt(ri);
			var check = true;
			westGridPanel.getStore().each(function(r){
				if(r.get('tableID') == sr.get('tableID')){
					Ext.example.msg('提示', '已添加该餐桌信息, 请重新选择.');
					check = false;
					return false;
				}
			});
			if(check){
				westGridPanel.getStore().insert(0, sr);
				westGridPanel.getView().refresh();
				thiz.getStore().remove(sr);
				thiz.getView().refresh();
			}
		});
		
		var eastPanelTbar = new Ext.Toolbar({
			items : ['->', {
				text : '展开/收缩',
				iconCls : 'icon_tb_toggleAllGroups',
				handler : function(){
					eastPanel.getView().toggleAllGroups();
				}
			}, {
				text : '取消',
				id : 'btnCancelTableGroup',
				iconCls : 'btn_delete',
				handler : function(){
					var sd = Ext.ux.getSelData(eastPanel);
					if(!sd){
						Ext.example.msg('提示', '请选择一个团体餐桌后再操作.');
						return;
					}
					Ext.Msg.show({
						title : '提示',
						msg : '是否取消选中餐桌组信息?',
						icon : Ext.Msg.QUESTION,
						buttons : Ext.Msg.YESNO,
						fn : function(e){
							if(e == 'yes'){
								var loading = new Ext.LoadMask(document.body, {
								    msg  : '正在更新已点菜列表,请稍等......',
								    disabled : false,
								    removeMask : true
								});
								loading.show();
								Ext.Ajax.request({
									url : '../../CancelOrderGroup.do',
									params : {
										pin : pin,
										orderID : sd.parentID
									},
									success : function(res, opt){
										loading.hide();
										var jr = Ext.decode(res.responseText);
										if(jr.success){
											loadDatqaForOrderGroup();
										}
										Ext.ux.showMsg(jr);
										getData();
									},
									failure : function(res, opt){
										loading.hide();
										Ext.ux.showMsg(Ext.decode(res.responseText));
									}
								});
							}
						}
					});
				}
			}]
		});
		
		var eastPanel = createGridPanel(
			'eastGridPanel',
			'团体餐桌',
			'',
			350,
			'',
			[
				[true, false, false, false], 
				['编号', 'tableAlias', 60],
				['名称', 'tableName', 120],
				['操作', 'operation', 130, 'center', 'eastGridPanelRenderer'],
				['parentID', 'parentID', 10]
			],
			['tableID', 'tableAlias', 'tableName', 'parentID'],
			[['isPaging', false], ['restaurantID', restaurantID], ['pin', pin]],
			30,
			{ name : 'parentID', hide : true, sort : 'tableAlias' },
			eastPanelTbar
		);
		eastPanel.region = 'east';
		eastPanel.view.groupTextTpl = '餐桌数量:{[values.rs.length]}';
		eastPanel.on('rowdblclick', function(thiz, ri, e){
			var sr = thiz.getStore().getAt(ri);
			var check = true;
			westGridPanel.getStore().each(function(r){
				if(typeof r.get('parentID') == 'number'){
					if(r.get('parentID') == sr.get('parentID')){
						Ext.example.msg('提示', '已添加该餐桌组信息, 请重新选择.');
					}else{
						Ext.example.msg('提示', '已添加其他餐桌组信息, 请重新选择.');
					}
					check = false;
					return false;
				}
			});
			if(check){
				for(var i = thiz.getStore().getCount() - 1; i >= 0; i--){
					if(thiz.getStore().getAt(i).get('parentID') == sr.get('parentID')){
						westGridPanel.getStore().insert(0, thiz.getStore().getAt(i));
						westGridPanel.getView().refresh();
						thiz.getStore().remove(thiz.getStore().getAt(i));
					}
				}
				thiz.getView().refresh();
			}
		});
		
		orderGroupWin = new Ext.Window({
			modal : true,
			closable : false,
			resizable : false,
			width : 950,
			height : 500,
			layout : 'border',
			items : [westGridPanel, centerGridPanel, eastPanel],
			bbar : ['->', {
				text : '重置',
				iconCls : 'btn_refresh',
				handler : function(){
					loadDatqaForOrderGroup();
				}
			}, {
				text : '确定',
				iconCls : 'btn_save',
				handler : function(e){
					var tables = [], otype = 0, parentID = 0;
					if(westGridPanel.getStore().getCount() == 0){
						Ext.example.msg('提示', '请选择餐桌后再操作.');
						return;
					}
					westGridPanel.getStore().each(function(r){
						tables.push({
							id : r.get('tableID'),
							alias : r.get('tableAlias')
						});
						if(otype == 0 && typeof r.get('parentID') == 'number' && eval(r.get('parentID') > 0)){
							parentID = r.get('parentID');
							otype = 1;
						}
					});
					Ext.Ajax.request({
						url : '../../UpdateOrderGroup.do',
						params : {
							pin : pin,
							restaurantID : restaurantID,
							otype : otype,
							tables : Ext.encode(tables),
							parentID : parentID
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								loadDatqaForOrderGroup();
								location.href = "CheckOut.html?"
									+ "orderID=" + (otype == 1 ? parentID : jr.other.orderID)
									+ "&pin=" + pin
									+ "&restaurantID=" + restaurantID
									+ "&category=" + 4;
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
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					orderGroupWin.hide();
				}
			}],
			listeners : {
				show : function(thiz){
					westGridPanel.getStore().removeAll();
					loadDatqaForOrderGroup();
					thiz.center();
				}
			}
		});
	}
	orderGroupWin.show();
	
};