﻿var btnAddMaterialCate = new Ext.ux.ImageButton({
	imgPath : ' ',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加原料类别',
	handler : function(btn){
		operateMaterialCateHandler({otype:Ext.ux.otype['insert']});
	}
});

var btnAddMaterial = new Ext.ux.ImageButton({
	imgPath : ' ',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '添加原料',
	handler : function(btn){
		operateMaterialHandler({otype:Ext.ux.otype['insert']});
	}
});

var btnGetBack = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'InventoryProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
	}
});

var btnLoginOut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});


Ext.onReady(function(){
	
	initControl();
	
	var centerPanel = new Ext.Panel({
		title : '原料资料管理',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [materialCateTree, materialBasicGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddMaterialCate, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, btnAddMaterial, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, '->', btnGetBack, {
			    xtype : 'tbtext',
				text : '&nbsp;&nbsp;'
			}, btnLoginOut ]
		})
	});
	
	initMainView(null,centerPanel,null);
	getOperatorName(pin, "../../");
	/*
	Ext.getDoc().on('contextmenu', function(e){
		e.stopEvent();
		var menu = new Ext.menu.Menu({
			items : [{
				text : '类别添加',
				iconCls : 'btn_add',
				handler : function(){
					operateMaterialCateHandler({otype:Ext.ux.otype['insert']});
				}
			}, {
				text : '类别修改',
				iconCls : 'btn_edit',
				handler : function(){
					operateMaterialCateHandler({otype:Ext.ux.otype['update']});
				}
			}, {
				text : '类别删除',
				iconCls : 'btn_delete',
				handler : function(){
					operateMaterialCateHandler({otype:Ext.ux.otype['delete']});
				}
			}, '-', {
				text : '原料添加',
				iconCls : 'btn_add',
				handler : function(){
					operateMaterialHandler({otype:Ext.ux.otype['insert']});
				}
			}, {
				text : '原料修改',
				iconCls : 'btn_edit',
				handler : function(){
					operateMaterialHandler({otype:Ext.ux.otype['update']});
				}
			}, {
				text : '原料删除',
				iconCls : 'btn_delete',
				handler : function(){
					operateMaterialHandler({otype:Ext.ux.otype['delete']});
				}
			}]
		});
		menu.showAt(e.getXY());
	});
	*/
});