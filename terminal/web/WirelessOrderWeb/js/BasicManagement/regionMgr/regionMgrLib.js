/**
 * 
 * @param c
 */
function operateTableDataHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefied'){
		return;
	}
	
	var id = Ext.getCmp('hideTableId');
	var alias = Ext.getCmp('numTableAlias');
	var name = Ext.getCmp('txtTableName');
	var region = Ext.getCmp('comboTableRegion');
	var minimumCost = Ext.getCmp('numMinimumCost');
	var serviceRate = Ext.getCmp('numServiceRate');
	
	if(c.otype == Ext.ux.otype['set']){
		var data = typeof c.data == 'undefined' ? {} : c.data;
		var regionData = typeof data.region == 'undefined' ? {} : data.region;  
		id.setValue(data['id']);
		alias.setValue(data['alias']);
		name.setValue(data['name']);
		region.setValue(regionData['id']);
		minimumCost.setValue(typeof data['minimumCost'] == 'undefined' ? 0 : data['minimumCost']);
		serviceRate.setValue(typeof data['serviceRate'] == 'undefined' ? 0 : data['serviceRate']);
		
		alias.clearInvalid();
		name.clearInvalid();
		region.clearInvalid();
		minimumCost.clearInvalid();
		serviceRate.clearInvalid();
	}else if(c.otype == Ext.ux.otype['get']){
		
	}
}
 
/**
 * 新增餐台信息
 */
function insertTableBasicHandler(){
	Ext.getCmp('btnSaveOperateTable').setVisible(true);
	Ext.getCmp('numTableAlias').setDisabled(false);
	tableBasicWin.center();
	tableBasicWin.show();
	tableBasicWin.setTitle('新增餐台信息');
	tableBasicWin.otype = Ext.ux.otype['insert'];
	operateTableDataHandler({
		otype : Ext.ux.otype['set']
	});
}

/**
 * 修改餐台信息
 */
function updateTableBasicHandler(){
	var data = Ext.ux.getSelData(tableBasicGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录再操作.');
		return;
	}
	if(data['statusValue'] == 1){
		Ext.example.msg('提示', '该台正在就餐, 只能查看不允许修改.');
		Ext.getCmp('btnSaveOperateTable').setDisabled(true);		
	}else{
		Ext.getCmp('btnSaveOperateTable').setDisabled(false);
	}
	Ext.getCmp('numTableAlias').setDisabled(true);
	tableBasicWin.center();
	tableBasicWin.show();
	tableBasicWin.setTitle('修改餐台信息');
	tableBasicWin.otype = Ext.ux.otype['update'];
	operateTableDataHandler({
		otype : Ext.ux.otype['set'],
		data : data
	});
	
	
}

/**
 * 删除餐台信息
 */
function deleteTableBasicHandler(){
	var data = Ext.ux.getSelData(tableBasicGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录再操作.');
		return;
	}
	if(data['statusValue'] == 1){
		Ext.example.msg('提示', '操作失败, 该餐台正在就餐, 不允许删除.');
		return;
	}
	Ext.Msg.show({
		title : '重要',
		msg : '是否删除餐台信息',
		buttons : Ext.Msg.YESNO,
		icon: Ext.Msg.QUESTION,
		fn : function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateTable.do',
					params : {
						'dataSource' : 'delete',
						
						id : data['id']
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnSearchForTable').handler();
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
}

