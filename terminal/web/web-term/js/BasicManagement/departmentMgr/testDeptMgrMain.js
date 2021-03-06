

/**
 * 修改部门信息
 */
function updateKitchen(){
	if(!Ext.ux.getSelData(kitchenGrid)){
		Ext.example.msg('提示', '请选中一个分厨再进行操作.');
		return;
	}
	updateKitchenWin = Ext.getCmp('dept_updateKitchenWin');
	if(!updateKitchenWin){
		updateKitchenWin = new Ext.Window({
			id : 'dept_updateKitchenWin',
			title : '修改分厨信息',
			closable : false,
			resizable : false,
			modal : true,
			width : 245,			
			items : [{
				xtype : 'form',
				layout : 'form',
				width : 245,
				labelWidth : 65,
				frame : true,
				items : [{
					xtype : 'hidden',
					id : 'txtKitchenID',
					fieldLabel : '厨房编号'
				}, {
					xtype : 'textfield',
					id : 'txtKitchenName',
					width : 130,
					fieldLabel : '厨房名称',
					allowBlank : false
				}, {
 	    	    	xtype : 'combo',
 	    	    	id : 'comboKitchenDept',
 	    	    	fieldLabel : '所属部门',
 	    	    	width : 130,
 	    	    	store : new Ext.data.JsonStore({
						fields : [ 'deptID', 'deptName', 'type'],
						root : 'root'
					}),
					valueField : 'deptID',
					displayField : 'deptName',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					blankText : '该项部门不能为空.'
 	    	    }, {
 	    	    	xtype : 'combo',
 	    	    	id : 'comboIsAllowTemp',
 	    	    	fieldLabel : '允许临时菜',
 	    	    	width : 130,
 	    	    	value : 0,
 	    	    	store : new Ext.data.SimpleStore({
						fields : [ 'value', 'text'],
						data : [[0,'否'], [1, '是']]
					}),
					valueField : 'value',
					displayField : 'text',
					mode : 'local',
					triggerAction : 'all',
					typeAhead : true,
					selectOnFocus : true,
					forceSelection : true,
					allowBlank : false,
					blankText : '该项不能为空.'
 	    	    }]
			}],
			bbar : [
				'->',
				{
					text : '保存',
					id : 'btnSaveUpdateKitchen',
					iconCls : 'btn_save',
					handler : function(){
						
						var kitchenID = Ext.getCmp('txtKitchenID');
						var kitchenName = Ext.getCmp('txtKitchenName');
						var kitchenDept = Ext.getCmp('comboKitchenDept');
						var isAllowTemp = Ext.getCmp('comboIsAllowTemp');
						
						if(!kitchenName.isValid() || !kitchenDept.isValid() || !isAllowTemp.isValid()){
							return;
						}
						
						var save = Ext.getCmp('btnSaveUpdateKitchen');
						var cancel = Ext.getCmp('btnCancelUpdateKitchen');
						
						save.setDisabled(true);
						cancel.setDisabled(true);
						Ext.Ajax.request({
							url : '../../UpdateKitchen.do',
							params : {
								restaurantID : restaurantID,
								kitchenID : kitchenID.getValue(),
								kitchenName : kitchenName.getValue(),
								deptID : kitchenDept.getValue(),
								isAllowTemp : isAllowTemp.getValue()
							},
							success : function(res, opt){
								var jr = Ext.util.JSON.decode(res.responseText);
								if(jr.success){
									Ext.example.msg(jr.title, jr.msg);
									updateKitchenWin.hide();
									Ext.getCmp('btnSearchKitchen').handler();
								}else{
									Ext.ux.showMsg(jr);
								}
								save.setDisabled(false);
								cancel.setDisabled(false);
							},
							failure : function(res, opt) {
								Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
								save.setDisabled(false);
								cancel.setDisabled(false);
							}
						});
						
					}
				}, {
					text : '关闭',
					id : 'btnCancelUpdateKitchen',
					iconCls : 'btn_close',
					handler : function(){
						updateKitchenWin.hide();
					}
				}
			],
			listeners : {
				show : function(){
					var sd = Ext.ux.getSelData(kitchenGrid);
					var kitchenID = Ext.getCmp('txtKitchenID');
					var kitchenName = Ext.getCmp('txtKitchenName');
					var kitchenDept = Ext.getCmp('comboKitchenDept');
					var isAllowTemp = Ext.getCmp('comboIsAllowTemp');
					
					var root = {root:[]};
					for(var i = 0; i < deptData.length; i++){
						if(deptData[i].type == 0)
							root.root.push(deptData[i]);
					}
					kitchenDept.store.loadData(root);
					
					kitchenID.setValue(sd.id);
					kitchenName.setValue(sd.name);
					kitchenDept.setValue(sd.dept.id);
					isAllowTemp.setValue(sd.isAllowTmp);
					kitchenName.focus(true, 100);
				}
			},
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnSaveUpdateKitchen').handler();
				 },
				 scope : this 
			 }]
		});
	}
	
	updateKitchenWin.show();
	updateKitchenWin.center();
};

kitchenOperationRenderer = function(){
	return '<a href="javascript:updateKitchen()">修改</a>';
};

function updateDeptHandler(){
	var node = Ext.ux.getSelNode(deptTree);
	
	if(!node || node.attributes.deptID == -1){
		Ext.example.msg('提示', '请选中一个部门再进行操作.');
		return;
	}
	
	if(node.attributes.type == 1){
		Ext.example.msg('提示', '<<font color="red">' + node.text + '</font>>为系统保留部门,不允许修改.');
		return;
	}
	
	if(!updateDeptWin){
		updateDeptWin = new Ext.Window({
			title : '修改部门信息',
			closable : false,
			resizable : false,
			modal : true,
			width : 230,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 65,
				items : [{
					xtype : 'textfield',
					id : 'txtDeptID',
					fieldLabel : '部门编号',
					readOnly : false,
					disabled : true,
					width : 130
				}, {
					xtype : 'textfield',
					id : 'txtDeptName',
					fieldLabel : '部门名称',
					width : 130
				}]
			}],
			bbar : [
			'->',
			{
				text : '保存',
				id : 'btnSaveUpdateDept',
				iconCls : 'btn_save',
				handler : function(){
					var deptID = Ext.getCmp('txtDeptID');
					var deptName = Ext.getCmp('txtDeptName');
					
					Ext.Ajax.request({
						url : '../../UpdateDepartment.do',
						params : {
							restaurantID : restaurantID,
							deptID : deptID.getValue(),
							deptName : deptName.getValue()
						},
						success : function(res, opt){
							var jr = Ext.util.JSON.decode(res.responseText);
							Ext.example.msg(jr.title, jr.msg);
							updateDeptWin.hide();
							deptTree.getRootNode().reload();
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
						}
					});
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					updateDeptWin.hide();
				}
			}],
			listeners : {
				show : function(){
					var node = Ext.ux.getSelNode(deptTree);
					Ext.getCmp('txtDeptID').setValue(node.attributes.deptID);
					Ext.getCmp('txtDeptName').setValue(node.text);
					
					var deptName = Ext.getCmp("txtDeptName");
					deptName.focus(true, 100);
				}
			},
			keys : [{
				 key : Ext.EventObject.ENTER,
				 fn : function(){ 
					 Ext.getCmp('btnSaveUpdateDept').handler();
				 },
				 scope : this 
			 }]
		});
	}
	
	updateDeptWin.show();
	updateDeptWin.center();
}

var deptTree;
var updateDeptWin;
var kitchenGrid;
var updateKitchenWin;
var deptTreeFloat_obj = {treeId : 'cancelledFoodDeptTree', option : [{name : '修改', fn : 'updateDeptHandler()'}]};
Ext.onReady(function() {
	
	deptTree = new Ext.tree.TreePanel({
		title : '部门信息',
		id : 'cancelledFoodDeptTree',
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
		frame : true,
		autoScroll:true,
		enableDD : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryKitchen.do',
			baseParams : {
				dataSource : 'deptKitchenTree'
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部部门',
	        leaf : false,
	        border : true,
	        deptID : -1,
	        cls : 'floatBarStyle',
	        listeners : {
	        	load : function(){
	        		var treeRoot = deptTree.getRootNode().childNodes;
	        		if(treeRoot.length > 0){
	        			deptData = [];
	        			for(var i = (treeRoot.length - 1); i >= 0; i--){
	    					if(treeRoot[i].attributes.deptID == 255 || treeRoot[i].attributes.deptID == 253){
	    						deptTree.getRootNode().removeChild(treeRoot[i]);
	    					}
	    				}
	        			for(var i = 0; i < treeRoot.length; i++){
	        				var tp = {};
	        				tp.type = treeRoot[i].attributes.type;
	        				tp.deptID = treeRoot[i].attributes.deptID;
	        				tp.deptName = treeRoot[i].text;
	        				deptData.push(tp);
	        			}
	        			Ext.getCmp('btnRefreshSearchKitchen').handler();
	        		}else{
	        			deptTree.getRootNode().getUI().hide();
	        			Ext.Msg.show({
	        				title : '提示',
	        				msg : '加载部门信息失败.',
	        				buttons : Ext.MessageBox.OK
	        			});
	        		}
	        	}
	        }
		}),
		tbar : [
		    '->',
		    {
				text : '修改',
				iconCls : 'btn_edit',
				handler : function(e){
					updateDeptHandler();					
				}
			}, {
				text : '刷新',
				iconCls : 'btn_refresh',
				handler : function(){
					deptTree.getRootNode().reload();
				}
			}
		],
		listeners : {
			click : function(e){
				Ext.getCmp('btnSearchKitchen').handler();
				Ext.getDom('deptNameShowType').innerHTML = e.text;
			}
		}
	});
	
	var kitchenGridTbar = new Ext.Toolbar({
		height : 26,
		items : [
		    {xtype:'tbtext', text:String.format(Ext.ux.txtFormat.typeName, '部门', 'deptNameShowType', '----')},
		    {xtype:'tbtext', text:'&nbsp;&nbsp;'},
//		    {xtype:'tbtext', text:'分厨名称:'},
		    {
		    	xtype : 'hidden',
		    	id : 'txtSearchKitchenName'
		    },
			'->',
			{
				text : '搜索',
				id : 'btnSearchKitchen',
				iconCls : 'btn_search',
				handler : function(){
					var deptID = '';
					var kitchenName = Ext.getCmp('txtSearchKitchenName').getValue();
					
					var sn = deptTree.getSelectionModel().getSelectedNode();
					deptID = !sn ? deptID : sn.attributes.deptID;
					kitchenName = kitchenName.replace(/(^\s*)|(\s*$)/g, '');
					
					kitchenGrid.getStore().load({
						params : {
							limit : 50,
							start : '0',
							deptID : deptID,
							kitchenName : kitchenName
						}
					});
				}
			}, {
				text : '修改',
				iconCls : 'btn_edit',
				handler : function(){
					updateKitchen();
				}
			}, {
				text : '重置',
				id : 'btnRefreshSearchKitchen',
				iconCls : 'btn_refresh',
				handler : function(){
					deptTree.getSelectionModel().clearSelections();
					Ext.getDom('deptNameShowType').innerHTML = '----';
					Ext.getCmp('txtSearchKitchenName').setValue();
					Ext.getCmp('btnSearchKitchen').handler();
				}
			}
		]
	});
	
	kitchenGrid = createGridPanel(
		'kitchenGrid',
		'分厨信息',
		'',
		'',
		'../../QueryKitchen.do',
		[
		    [true, false, false, false], 
			['厨房编号', 'alias', '50'] , 
			['厨房名称', 'name'] , 
			['所属部门', 'dept.name', '', , 'function(v){if(v.length==0){ v = \"--\"; } return v;}'],
			['是否允许临时菜', 'isAllowTmp', ,'center', 'function(val){return eval(val == 1) ? "是" : "否";}'],
			['操作', 'operation', , 'center', 'kitchenOperationRenderer']
		],
		KitchenRecord.getKeys(),
		//['kitchenID', 'kitchenName', 'department', 'deptName', 'kitchenAlias', 'isAllowTemp'],
		[ ['isPaging', false], ['dataSource', 'normal']],
		0,
		'',
		kitchenGridTbar
	);
	kitchenGrid.region = 'center';
	kitchenGrid.on('rowdblclick', function(){
		updateKitchen();
	});
	
	new Ext.Panel({
		renderTo : 'divDept',
		layout : 'border',
		width : parseInt(Ext.getDom('divDept').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divDept').parentElement.style.height.replace(/px/g,'')),
		items : [deptTree, kitchenGrid]
	});
	
	
//	showFloatOption(deptTreeFloat_obj);
});
