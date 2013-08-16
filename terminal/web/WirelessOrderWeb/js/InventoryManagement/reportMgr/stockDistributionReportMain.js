var pushBackBut = new Ext.ux.ImageButton({
	imgPath : "../../images/UserLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "返回",
	handler : function(btn){
		location.href = "InventoryProtal.html?"+ strEncode('restaurantID=' + restaurantID, 'mi');
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : "../../images/ResLogout.png",
	imgWidth : 50,
	imgHeight : 50,
	tooltip : "登出",
	handler : function(btn){
		
	}
});

var materialTypeDate = [[1,'商品'],[2,'原料']];
var materialTypeComb = new Ext.form.ComboBox({
	fidldLabel : '类型:',
	forceSelection : true,
	width : 110,
	id : 'materialType',
	value : 1,
	store : new Ext.data.SimpleStore({
		fields : [ 'value', 'text' ],
		data : materialTypeDate
	}),
	valueField : 'value',
	displayField : 'text',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	readOnly : true	,
	listeners : {
        select : function(combo, record, index){  
        	materialCateComb.reset();
        	materialComb.allowBlank = true;
        	materialComb.reset();
        	materialCateStore.load({  
	            params: {  
	            	type : combo.value,  
	            	dataSource : 'normal'
	            }  
            });     
        	materialStore.load({
        		params: {
        			cateType : combo.value,
        			dataSource : 'normal'
        		}
        	});
		}  
	}
	
});
var materialCateStore = new Ext.data.Store({
	//proxy : new Ext.data.MemoryProxy(data),
	proxy : new Ext.data.HttpProxy({url:'../../QueryMaterialCate.do?restaurantID=' + restaurantID}),
	reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
         {name : 'id'},
         {name : 'name'}
	])
});
materialCateStore.load({  
    params: {  
    	type : materialTypeComb.value,  
    	dataSource : 'normal'
    }
}); 
var materialCateComb = new Ext.form.ComboBox({
	fidldLabel : '类别:',
	forceSelection : true,
	width : 110,
	id : 'materialCate',
	store : materialCateStore,
	valueField : 'id',
	displayField : 'name',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	//blankText: '不能为空', 
	readOnly : true,
	listeners : {
        select : function(combo, record, index){ 
        	materialComb.allowBlank = true;
        	materialComb.reset();
        	materialStore.load({  
	            params: {  
	            	cateType : materialTypeComb.value,
	            	cateId : combo.value,  
	            	dataSource : 'normal'
	            }  
            });     
		}

	}
	
});
var materialStore = new Ext.data.Store({
	//proxy : new Ext.data.MemoryProxy(data),
	proxy : new Ext.data.HttpProxy({url:'../../QueryMaterial.do?restaurantID=' + restaurantID}),
	reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
		{name : 'id'},
		{name : 'name'},
		{name : 'pinyin'}
	])
});
materialStore.load({  
    params: { 
    	cateType : materialTypeComb.value,
    	dataSource : 'normal'
    }  
}); 
var materialComb = new Ext.form.ComboBox({
	fidldLabel : '货品:',
	forceSelection : true,
	width : 110,
	listWidth : 250,
	height : 200,
	maxHeight : 300,
	id : 'materialId',
	store : materialStore,
	valueField : 'id',
	displayField : 'name',
	typeAhead : true,
	mode : 'local',
	triggerAction : 'all',
	selectOnFocus : true,
	tpl:'<tpl for=".">' 
		+ '<div class="x-combo-list-item" style="height:18px;">'
		+ '{id} -- {name} -- {pinyin}'
		+ '</div>'
		+ '</tpl>',
	listeners : {
		beforequery : function(e){
			var combo = e.combo; 
			if(!e.forceAll){
				var value = e.query; 
				combo.store.filterBy(function(record,id){
					return record.get('name').indexOf(value) != -1 
							|| (record.get('id')+'').indexOf(value) != -1 
							|| record.get('pinyin').indexOf(value.toUpperCase()) != -1;
				});  
				combo.expand(); 
				combo.select(0, true);
				return false; 
			}
		}
	}
	
});
var deptTree;
var stockDistributionGrid;
Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();
	Ext.form.Field.prototype.msgTarget = 'side';
	
	deptTree = new Ext.tree.TreePanel({
		title : '部门信息',
		id : 'deptTree',   
		region : 'west',
		width : 200,
		border : false,
		rootVisible : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryDeptTree.do?time=' + new Date(),
			baseParams : {
				'restaurantID' : restaurantID
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部部门',
	        leaf : false,
	        border : true,
	        deptID : ' ',
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
		listeners : {
			click : function(e){
				Ext.getDom('dept').innerHTML = e.text;
			},
			dblclick : function(e){
				Ext.getCmp('btnSearch').handler();
			}
		},
		tbar :	[
		     '->',
		     {
					text : '刷新',
					iconCls : 'btn_refresh',
					handler : function(){
						deptTree.getRootNode().reload();
					}
			}
		 ]
			

	});
	
	
	var stockDetail = new Ext.grid.ColumnModel([
		{header:'部门', dataIndex:'dept.name', width:220},
		{header:'品项名称', dataIndex:'materialName', width:220, hidden:true},
		{header:'数量', dataIndex:'stock', width:220},
		{header:'成本单价', dataIndex:'price', width:220},
		{header:'成本金额', dataIndex:'cost', width:220}
	                                            
	    ]);
	    stockDetail.defaultSortable = true;
	var ds = new Ext.data.GroupingStore({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterialDept.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
			 {name : 'dept.name'},
	         {name : 'materialName'},
	         {name : 'stock'},
	         {name : 'price'},
	         {name : 'cost'}

		]),
		sortInfo:{field: 'materialName', direction: "ASC"},
		groupField:'materialName'
	});
	var date = new Date();
	date.setMonth(date.getMonth()-1);
	var distributionReportBar = new Ext.Toolbar({
		items : [
 		{
			xtype : 'tbtext',
			text : String.format(
				Ext.ux.txtFormat.typeName,
				'部门','dept','全部部门'
			)
		},
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类型:'},
		materialTypeComb,
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '类别:'},
		materialCateComb,
		{xtype : 'tbtext', text : '&nbsp;'},
		{xtype : 'tbtext', text : '货品:'},
		materialComb,

		'->', {
				text : '展开/收缩',
				iconCls : 'icon_tb_toggleAllGroups',
				handler : function(){
					stockDistributionGrid.getView().toggleAllGroups();
				} 
			}, {
			text : '搜索',
			id : 'btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				var deptID = '';
					var sn = deptTree.getSelectionModel().getSelectedNode();
					
					var stockds = stockDistributionGrid.getStore();
					stockds.baseParams['deptId'] = !sn ? deptID : sn.attributes.deptID;
					stockds.baseParams['cateType'] = Ext.getCmp('materialType').getValue();
					stockds.baseParams['cateId'] = Ext.getCmp('materialCate').getValue();
					stockds.baseParams['materialId'] = Ext.getCmp('materialId').getValue();
					stockds.load({
						params : {
							start : 0,
							limit : 13
						}
					});
			}
		},{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
		]
	});
	var pagingBar = new Ext.PagingToolbar({
		   pageSize : 13,	//显示记录条数
		   store : ds,	//定义数据源
		   displayInfo : true,	//是否显示提示信息
		   displayMsg : "显示第 {0} 条到 {1} 条记录，共 {2} 条",
		   emptyMsg : "没有记录"
	});
	stockDistributionGrid = new Ext.grid.GridPanel({
		title : '库存分布明细',
		id : 'grid',
		region : 'center',
		height : '500',
		border : true,
		frame : true,
        animCollapse: false,
		store : ds,
		cm : stockDetail,
		view : new Ext.grid.GroupingView({
            forceFit: true,
            groupTextTpl: '{text} ({[values.rs.length]}条记录)'
        }),
		tbar : distributionReportBar,
		bbar : pagingBar
	});
	ds.load({params:{start:0,limit:13}});
	
	var stockDetailReport = new Ext.Panel({
		title : '报表管理',
		region : 'center',//渲染到
		layout : 'border',//布局
		frame : true, 
		//margins : '5 5 5 5',
		//子集
		items : [deptTree, stockDistributionGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    '->',
			    pushBackBut, 
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
				logOutBut 
			]
		})
	});
	getOperatorName("../../");
	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : 
		[{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},stockDetailReport,{
			region : 'south',
			height : 30,
			frame : true,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	});
});

