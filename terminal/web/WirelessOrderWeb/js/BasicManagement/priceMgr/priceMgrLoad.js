initTree = function(){
	var tbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '添加',
			hidden : true,
			iconCls : 'btn_add',
			handler : function(){
				insertPricePlanWinHandler();
			}
		}, {
			text : '修改',
			iconCls : 'btn_edit',
			handler : function(){
				updatePricePlanWinHandler();
			}
		}, {
			text : '删除',
			iconCls : 'btn_delete',
			handler : function(){
				deletePricePlanWinHandler();
			}
		}, {
			text : '刷新',
			id : 'btnRefreshPricePlanTree',
			iconCls : 'btn_refresh',
			handler : function(){
				pricePlanTree.getRootNode().reload();
			}
		}]
	});
	
	pricePlanTree = new Ext.tree.TreePanel({
		region : 'west',
		width : 200,
		border : true,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		tbar : tbar,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryPricePlanTree.do',
			baseParams : {
				pin : pin,
				restaurantID : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部方案',
	        leaf : false,
	        border : true,
	        pricePlanID : '-1',
	        listeners : {
	        	load : function(e){
	        		pricePlanData.root = [];
	        		for(var i = 0; i < e.childNodes.length; i++){
	        			var temp = e.childNodes[i];
	        			if(temp.attributes['status'] == 1){
	        				temp.setText(temp.attributes['pricePlanName']+'<font color="red">(活动方案)</font>');
	        			}
	        			pricePlanData.root.push({
        					pricePlanID : temp.attributes['pricePlanID'],
        					pricePlanName : temp.attributes['pricePlanName'],
        					status : temp.attributes['status']
        				});	
	        		}
	        	}
	        }
		})
	});	
};

initGrid = function(){
	var tbar = new Ext.Toolbar({
		height : 26,
		items : ['->', {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(){
				priceBaiscGrid.getStore().reload();
			}
		}]
	});
	priceBaiscGrid = createGridPanel(
		'priceBaiscGrid',
		'菜品价格',
		'',
		'',
		'../../QueryDiscountPlan.do',
		[
			[true, false, true, true], 
			['方案编号', 'discount.id'] , 
			['方案名称', 'discount.name'],
			['分厨名称', 'kitchen.kitchenName'], 
			['折扣率', 'rate', 50, 'right' , 'Ext.ux.txtFormat.gridDou'],
			['操作', 'operation', '', 'center', '']
		],
		['discount.id', 'discount.name', 'discount.status', 'kitchen.kitchenID', 'kitchen.kitchenName', 'rate', 'planID'],
		[['pin',pin], ['isPaging', true], ['restaurantID', restaurantID]],
		50,
		'',
		tbar
	);	
	priceBaiscGrid.region = 'center';
};

initWin = function(){
	oPricePlanWin = new Ext.Window({
		title : '添加方案',
		modal : true,
		resizable : false,
		closable : false,
		width : 230,
		items : [{
			xtype : 'form',
			layout : 'form',
			frame : true,
			labelWidth : 70,
			labelAlign : 'right',
			defaults : {
				width : 110
			},
			items : [{
				xtype : 'hidden',
				id : 'txtPricePlanID'
			}, {
				xtype : 'textfield',
				id : 'txtPricePlanName',
				fieldLabel : '方案名称',
				allowBlank : false,
				blankText : '方案名称不能为空.',
				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0){
						return true;
					}else{
						return '方案名称不能为空.';
					}
				}
			}, {
				xtype : 'combo',
				id : 'comboPricePlanStatus',
				fieldLabel : '状态',
				forceSelection : true,
				value : 0,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : pricePlanStatusData
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				readOnly : true
			}, {
				xtype : 'combo',
				id : 'comboCopyPricePlan',
				fieldLabel : '复制方案',
				forceSelection : true,
				store : new Ext.data.JsonStore({
					root : 'root',
					fields : ['pricePlanID', 'pricePlanName']
				}),
				valueField : 'pricePlanID',
				displayField : 'pricePlanName',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true
			}]
		}],
		bbar : ['->', {
			text : '保存',
			id : 'btnSavePricePlan',
			iconCls : 'btn_save',
			handler : function(){
				var name = Ext.getCmp('txtPricePlanName');
				var status = Ext.getCmp('comboPricePlanStatus');
				
				var pricePlan = operationPricePlanData({ 
					type : pmObj.operation['get'] 
				}).data;
				var action = '';
				if(oPricePlanWin.otype == pmObj.operation['insert']){
					if(!name.isValid()){
						return;
					}
					action = '../../InsertPricePlan.do';
					pricePlan.id = pricePlan.copyID == 0 ? '' : pricePlan.copyID;
				}else if(oPricePlanWin.otype == pmObj.operation['update']){
					if(!status.isValid() || !status.isValid()){
						return;
					}
					action = '../../UpdatePricePlan.do';
				}else{
					return;
				}
				// 删除多余字段
				(delete pricePlan.copyID);
				Ext.Ajax.request({
					url : action,
					params : {
						restaurantID : restaurantID,
						pricePlan : Ext.encode(pricePlan)
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							oPricePlanWin.hide();
							Ext.example.msg(jr.title, jr.msg);
							Ext.getCmp('btnRefreshPricePlanTree').handler();
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
			id : 'btnCloseOPricePlanWin',
			iconCls : 'btn_close',
			handler : function(){
				oPricePlanWin.hide();
			}
		}],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btnSavePricePlan').handler();
			}
		}, {
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				Ext.getCmp('btnCloseOPricePlanWin').handler();
			}
		}],
		listeners : {
			show : function(){
				Ext.getCmp('comboCopyPricePlan').store.loadData(pricePlanData);
			}
		}
	});
};















