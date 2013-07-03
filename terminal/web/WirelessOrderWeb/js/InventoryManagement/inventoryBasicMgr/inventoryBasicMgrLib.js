/**
 * 初始化原料类别操作窗口
 */
function initOperateMaterialCateWin(){
	if(!operateMaterialCateWin){
		var cateId = new Ext.form.Hidden({
			id : 'hideMaterialCateId'
		});
		var cateName = new Ext.form.TextField({
			id : 'txtMaterialCateName',
			fieldLabel : '类别名称',
			width : 130,
			allowBlank : false
		});
		operateMaterialCateWin = new Ext.Window({
			id : 'operateMaterialCateWin',
			title : '&nbsp;',
			modal : true,
			resizable : false,
			closable : false,
			width : 230,
			items : [{
				xtype : 'form',
				layout : 'form',
				labelWidth : 65,
				frame : true,
				items : [cateId, cateName]
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					operateMaterialCateWin.hide();
				}
			}, {
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnAddMaterialCate').handler();
				}
			}],
			bbar : ['->', {
				text : '保存',
				id : 'btnAddMaterialCate',
				iconCls : 'btn_save',
				handler : function(){
					var dataSource = "";
					if(operateMaterialCateWin.otype == Ext.ux.otype['insert']){
						dataSource = 'insert';
					}else if(operateMaterialCateWin.otype == Ext.ux.otype['update']){
						dataSource = 'update';
					}else{
						return;
					}
					if(!cateName.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMaterialCate.do',
						params : {
							dataSource : dataSource,
							restaurantID : restaurantID,
							cateId : cateId.getValue(),
							name : cateName.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								operateMaterialCateWin.hide();
								materialCateTree.getRootNode().reload();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					operateMaterialCateWin.hide();
				}
			}],
			listeners : {
				show : function(thiz){
					thiz.center();
				}
			}
		});
	}
}
/**
 * 操作原料类别
 * @param c
 */
function operateMaterialCateHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	initOperateMaterialCateWin();
	
	operateMaterialCateWin.otype = c.otype;
	
	var cateId = Ext.getCmp('hideMaterialCateId');
	var cateName = Ext.getCmp('txtMaterialCateName');
	if(c.otype == Ext.ux.otype['insert']){
		operateMaterialCateWin.show();
		cateId.setValue();
		cateName.setValue();
		cateName.clearInvalid();
	}else if(c.otype == Ext.ux.otype['update']){
		var sn = materialCateTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes.cateID == -1){
			Ext.example.msg('提示', '请选中一个原料类别再进行操作.');
			return;
		}
		operateMaterialCateWin.show();
		cateId.setValue(sn.attributes.cateId);
		cateName.setValue(sn.attributes.name);
	}else if(c.otype == Ext.ux.otype['delete']){
		var sn = materialCateTree.getSelectionModel().getSelectedNode();
		if(!sn || sn.attributes.cateID == -1){
			Ext.example.msg('提示', '请选中一个原料类别再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除原料类别?',
			icon: Ext.MessageBox.QUESTION,
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateMaterialCate.do',
						params : {
							dataSource : 'delete',
							restaurantID : restaurantID,
							cateId : sn.attributes.cateId
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								materialCateTree.getRootNode().reload();
								Ext.example.msg(jr.title, jr.msg);
								operateMaterialCateWin.hide();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}
}

/**
 * 初始化原料信息操作窗口
 */
function initOperateMaterialWin(){
	if(!operateMaterialWin){
		var materialId = new Ext.form.Hidden({
			id : 'hideMaterialId'
		});
		var materialName = new Ext.form.TextField({
			id : 'txtMaterialName',
			fieldLabel : '原料名称',
			allowBlank : false
		});
		var materialCate = new Ext.form.ComboBox({
			id : 'txtMaterialCate',
			fieldLabel : '所属类别',
		    store : new Ext.data.JsonStore({
		    	root : 'root',
				fields : ['cateId', 'cateName']
			}),
			valueField : 'cateId',
			displayField : 'cateName',
			mode : 'local',
			triggerAction : 'all',
			typeAhead : true,
			selectOnFocus : true,
			forceSelection : true,
			readOnly : true,
			allowBlank : false
		});
		operateMaterialWin = new Ext.Window({
			id : 'operateMaterialWin',
			title : '&nbsp;',
			modal : true,
			resizable : false,
			closable : false,
			width : 230,
			items : [{
				xtype : 'form',
				layout : 'form',
				labelWidth : 65,
				frame : true,
				defaults : {
					width : 130
				},
				items : [materialId, materialName, materialCate]
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					operateMaterialWin.hide();
				}
			}, {
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnAddMaterial').handler();
				}
			}],
			bbar : ['->', {
				text : '保存',
				id : 'btnAddMaterial',
				iconCls : 'btn_save',
				handler : function(){
					var dataSource = "";
					if(operateMaterialWin.otype == Ext.ux.otype['insert']){
						dataSource = 'insert';
					}else if(operateMaterialWin.otype == Ext.ux.otype['update']){
						dataSource = 'update';
					}else{
						return;
					}
					if(!materialName.isValid() || !materialCate.isValid()){
						return;
					}
					Ext.Ajax.request({
						url : '../../OperateMaterial.do',
						params : {
							dataSource : dataSource,
							pin : pin,
							restaurantID : restaurantID,
							id : materialId.getValue(),
							name : materialName.getValue(),
							cateId : materialCate.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								operateMaterialWin.hide();
								Ext.getCmp('btnSearchMaterial').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					operateMaterialWin.hide();
				}
			}],
			listeners : {
				show : function(thiz){
					thiz.center();
					materialCate.store.loadData(materialCateData);
					
				}
			}
		});
	}
}

/**
 * 操作原料
 * @param c
 */
function operateMaterialHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	initOperateMaterialWin();
	
	operateMaterialWin.otype = c.otype;
	
	var materialId = Ext.getCmp('hideMaterialId');
	var materialName = Ext.getCmp('txtMaterialName');
	var materialCate = Ext.getCmp('txtMaterialCate');
	if(c.otype == Ext.ux.otype['insert']){
		operateMaterialWin.show();
		
		materialId.setValue();
		materialName.setValue();
		materialCate.setValue();
		materialName.clearInvalid();
		materialCate.clearInvalid();
		materialCate.setDisabled(false);
	}else if(c.otype == Ext.ux.otype['update']){
		var data = Ext.ux.getSelData(materialBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条原料信息再进行操作.');
			return;
		}
		operateMaterialWin.show();
		
		materialId.setValue(data['id']);
		materialName.setValue(data['name']);
		materialCate.setValue(data['cateId']);
		
		materialCate.setDisabled(true);
	}else if(c.otype == Ext.ux.otype['delete']){
		var data = Ext.ux.getSelData(materialBasicGrid);
		if(!data){
			Ext.example.msg('提示', '请选中一条原料信息再进行操作.');
			return;
		}
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除原料信息?',
			icon: Ext.MessageBox.QUESTION,
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateMaterial.do',
						params : {
							dataSource : 'delete',
							restaurantID : restaurantID,
							id : data['id']
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								Ext.getCmp('btnSearchMaterial').handler();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}
}