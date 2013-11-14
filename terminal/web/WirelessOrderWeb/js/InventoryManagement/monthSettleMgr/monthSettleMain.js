

function monthSettleHandler(){
	var stockActionCount = Ext.getDom('labStockAction').innerHTML;
 	var stockTakeCount = Ext.getDom('labStockTake').innerHTML;
 	if(eval(stockActionCount + '+' + stockTakeCount) > 0){
 		Ext.MessageBox.alert('提示', '还有未审核的库单或盘点单');
 	}else{
 		Ext.Ajax.request({
 			url : '../../UpdateCurrentMonth.do',
 			params : {
 				
 			},
 			success : function(res, opt){
				var jr = Ext.decode(res.responseText);
				if(jr.success){
					Ext.ux.showMsg(jr);
					monthSettleWin.hide();
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

function showMonthSettleDetail(){
	Ext.Ajax.request({
		url : '../../QuerySystemSetting.do',
		params : {
			restaurantID : restaurantID
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				//alert(jr.other.systemSetting.setting.intCurrentMonth);
				Ext.getDom('labCurrentMonth').innerHTML = jr.other.systemSetting.setting.intCurrentMonth;
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
	Ext.Ajax.request({
		url : '../../QueryStockTake.do',
		params : {
			
			status : 1
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.getDom('labStockTake').innerHTML = jr.totalProperty;
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
	Ext.Ajax.request({
		url : '../../QueryStockAction.do',
		params : {
			
			status : 1
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.getDom('labStockAction').innerHTML = jr.totalProperty;
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.reponseText));
		}
	});

}

function priceDeltaRenderer(v, m, r, ri, ci, s){
/*	alert(r.get('price'));
	var deltaAmount = r.get('price');
	deltaAmount = deltaAmount < 0 ? "<font color='red' size='4'>" + Ext.ux.txtFormat.gridDou(Math.abs(deltaAmount)) + "</font>" : "<font color='green' size='4'>" + Ext.ux.txtFormat.gridDou(Math.abs(deltaAmount)) + "</font>";
	return deltaAmount+"";*/
	return v + 2;
}

var settle = {
	id : 'monthSettleDetail',
	region : 'north',
	frame : true,
	height : 130,
	border : false,
	bodyStyle : 'font-size:14px;',
	html : '<div align="center" style="font-size:30px;">当前会计月份 : <label id="labCurrentMonth" style="color:green"> </label>&nbsp;月<br> ' +
			'未审核的库单 : <label id="labStockAction" style="color:red" >0</label>&nbsp;张</br>' +
			'未审核的盘点 : <label id="labStockTake" style="color:red" >0</label>&nbsp;张</div>'
};

var monthSettleTree, msm_monthSettleGrid;
var LIMIT_20 = 20;
Ext.onReady(function(){
	
	var cm = new Ext.grid.ColumnModel([
	       new Ext.grid.RowNumberer(),
	       {header: '品项名称 ', dataIndex: 'name'},
	       {header: '当前单价', dataIndex: 'price', align: 'right', editor: new Ext.form.NumberField({allowBlank: false })},
	       {header: '单价', hidden : true, dataIndex: 'price'},
	       {header: '变化点', dataIndex: 'price', align: 'right', renderer: priceDeltaRenderer}
	]);
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url: '../../QueryMaterial.do'}),
		reader : new Ext.data.JsonReader({totalProperty: 'totalProperty', root:'root'},[
				{name: 'id'},
				{name: 'name'},
				{name: 'price'}
				//{name: 'deltaPrice'}
		]),
		baseParams : {
			dataSource : 'monthSettleMaterial'
		}
	});
	
	msm_monthSettleGrid = new Ext.grid.EditorGridPanel({
		title : '货品列表',
		//id : 'deltaReport',
		region : 'center',
/*		border : true,
		frame : true,*/
		store : ds,
		cm : cm,
		clicksToEdit: 1,
		autoSizeColumns: true,
		viewConfig : {
			forceFit : true		
		}
	});
	
	
	ds.load({params:{mType : 1}});
	
	monthSettleTree = new Ext.tree.TreePanel({
		title : '货品归类',
		id : 'msm_materialTypeTree',   
		region : 'west',
		width : 170,
/*		border : false,
		frame : true,*/
		rootVisible : true,
		autoScroll : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryMaterialCate.do',
			baseParams : {
				dataSource : 'monthSettleTree'
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部种类',
	        leaf : false,
	        border : true,
	        mType : '1',
	        listeners : {
	        	load : function(){
	        		var treeRoot = monthSettleTree.getRootNode().childNodes;
	        		if(treeRoot.length > 0){
	        			deptData = [];
	        			for(var i = (treeRoot.length - 1); i >= 0; i--){
	    					if(treeRoot[i].attributes.deptID == 255 || treeRoot[i].attributes.deptID == 253){
	    						monthSettleTree.getRootNode().removeChild(treeRoot[i]);
	    					}
	    				}
	        		}else{
	        			monthSettleTree.getRootNode().getUI().hide();
	        			Ext.Msg.show({
	        				title : '提示',
	        				msg : '加载部门信息失败.',
	        				buttons : Ext.MessageBox.OK
	        			});
	        		}
	        	}
	        }
		}),
		listeners : {
			dblclick : function(e){
				msm_monthSettleGrid.setTitle('货品列表' + '&nbsp;&nbsp;<span style="color:green; font-weight:bold; font-size:13px;">' + e.text + '</span>');
				msm_monthSettleGrid.getStore().load({
					params : {
						type : e.attributes.type,
						mType : e.attributes.mType,
						cateId : e.attributes.cateId
					}
				});
			}
		},
		tbar :	[
		     '->',
		     {
					text : '刷新',
					iconCls : 'btn_refresh',
					handler : function(){
						monthSettleTree.getRootNode().reload();
					}
			}
		 ]
			

	});
	
	var monthSettleCenterPanel = new Ext.Panel({
		id : 'monthSettleCenterPanel',
		title : '成本调整' + '&nbsp;&nbsp;<span style="color:green; font-weight:bold; font-size:13px;">(有未审核的库单或盘点单时不能调整)</span>',
		region : 'center',
		layout : 'border',
		border : false,
		height : 300,
		//disabled : true,
		collapsible : true,
		titleCollapse : true,
		collapsed : true,
		items : [monthSettleTree, msm_monthSettleGrid],
		listeners : {
			beforeexpand : function(p){
				var stockActionCount = Ext.getDom('labStockAction').innerHTML;
			 	var stockTakeCount = Ext.getDom('labStockTake').innerHTML;
			 	if(eval(stockActionCount + '+' + stockTakeCount) > 0){
			 		//Ext.MessageBox.alert('提示', '还有未审核的库单或盘点单');
			 		return false;
			 	}
			},
			expand : function(p){
				Ext.getCmp('winMonthSettle').setHeight(700);
				Ext.getCmp('winMonthSettle').center();
			},
			collapse : function(){
				Ext.getCmp('winMonthSettle').setHeight(215);
				Ext.getCmp('winMonthSettle').center();
			}
		}
	});
	
	new Ext.Panel({
		renderTo : 'divMonthSettle',
		id : 'monthSettlePanel',
		height : 645,
		layout : 'border',
		items : [settle, monthSettleCenterPanel],
		listeners : {
			render : function(){
				showMonthSettleDetail();
			}
		},
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				msm_monthSettleGrid.hide();
			}
		}]
	});
});




