


var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'InventoryProtal.html?restaurantID=' + restaurantID + '&pin=' + pin;
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});

Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();
	Ext.form.Field.prototype.msgTarget = 'side';
	//定义列模型
	var cm = new Ext.grid.ColumnModel([
	         new Ext.grid.RowNumberer(),
	         {header:'品行编号', dataIndex:'materialId', width:63 },
	         {header:'品行名称', dataIndex:'materialName', width:100},
	         {header:'期初数量', dataIndex:'primeAmount', width:63},
	         {header:'入库采购', dataIndex:'stockIn', width:63},
	         {header:'入库调拨', dataIndex:'stockInTransfer', width:63},
	         {header:'入库报溢', dataIndex:'stockSpill', width:63},
	         {header:'入库盘盈', dataIndex:'stockTakeMore', width:63},
	         {header:'入库小计', dataIndex:'stockInAmount', width:63},
	         {header:'出库退货', dataIndex:'stockOut', width:63},
	         {header:'出库调拨', dataIndex:'stockOutTransfer', width:63},
	         {header:'出库报损', dataIndex:'stockDamage', width:63},
	         {header:'出库盘亏', dataIndex:'stockTakeLess', width:63},
	         {header:'出库消耗', dataIndex:'useUp', width:63},
	         {header:'出库小计', dataIndex:'stockOutAmount', width:63},
	         {header:'期末数量', dataIndex:'finalAmount', width:63},
	         {header:'期末单价', dataIndex:'finalPrice', width:63},
	         {header:'期末金额', dataIndex:'finalMoney', width:63}]);
	 cm.defaultSortable = true;
			
			
	//数据加载器
	var ds = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryReport.do?pin=' + pin}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
		         {name : 'materialId'},
		         {name : 'materialName'},
		         {name : 'primeAmount'},
		         {name : 'stockIn'},
		         {name : 'stockInTransfer'},
		         {name : 'stockSpill'},
		         {name : 'stockTakeMore'},
		         {name : 'stockInAmount'},
		         {name : 'stockOut'},
		         {name : 'stockOutTransfer'},
		         {name : 'stockDamage'},
		         {name : 'stockTakeLess'},
		         {name : 'useUp'},
		         {name : 'stockOutAmount'},
		         {name : 'finalAmount'},
		         {name : 'finalPrice'},
		         {name : 'finalMoney'}
		])
	});
	
	var date = new Date();
	date.setMonth(date.getMonth()-1);
	date.setDate(01);
	var stockTakeBar = new Ext.Toolbar({
		items : [
		{
			xtype : 'tbtext',
			text : String.format(
				Ext.ux.txtFormat.typeName,
				'货品','cateTypeValue','全部货品'
			)
		},

		{xtype : 'tbtext', text : '查看日期:'},
		{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
		{
			xtype : 'datefield',
			id : 'beginDate',
			allowBlank : false,
			format : 'Y-m-d',
			value : date,
			maxValue : new Date(),
			width : 100
		}, {
			xtype : 'label',
			id : 'to',
			text : ' 至 '
		}, {
			xtype : 'datefield',
			id : 'endDate',
			allowBlank : false,
			format : 'Y-m-d',
			value : new Date(),
			maxValue : new Date(),
			width : 100
		},'->', {
			text : '搜索',
			id : 'btnSearch',
			iconCls : 'btn_search',
			handler : function(){
				var cateType = '-1', cateId = '-1';
				var sgs = stockReportGrid.getStore();
				var rn = stockReportTree.getSelectionModel().getSelectedNode();
				if(!rn){
					cateType = '-1';
				}else{
					if(rn.attributes.typeId){
						cateType = rn.attributes.typeId;
					}else{
						cateId = rn.attributes.cateId;
					}
				}
				sgs.baseParams['beginDate'] = Ext.getCmp('beginDate').getValue().format('Y-m-d');
				sgs.baseParams['endDate'] = Ext.getCmp('endDate').getValue().format('Y-m-d');
				
				//load两种加载方式,远程和本地
				sgs.load({
					params : {
						start : 0,
						limit : 10,
						cateType : cateType,
						cateId : cateId
					}
				});
			}
		},{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
		]
	});
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : 10,
		store : ds,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : '没有记录'
	});

	
	
	var stockReportTree = new Ext.tree.TreePanel({
		title : '货品类型',
		id : 'tree',
		region : 'west',
		width : '200',
		border : false,
		frame : true, //采用渲染
		rootVisible : true,//显示根节点 
		autoScroll : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader: new Ext.tree.TreeLoader({
			dataUrl : '../../QueryMaterialCate.do?',
			baseParams : {
				dataSource : 'tree',
				restaurantID : restaurantID
			}
		}),
        root: new Ext.tree.AsyncTreeNode({
            expanded: true,
            text : '全部货品',
            typeId : '-1',
            leaf : false,
            children: [{
            	text : '原料',
            	typeId: '2',
            	leaf : false
            },{
                text: '商品',
                typeId: '1',
                leaf: true
            }]
            	
        }),
		listeners : {
			click : function(e){
				//var node = this.getSelectionModel().getSelectedNode();
				Ext.getDom('cateTypeValue').innerHTML = e.text;
			},
			dblclick : function(e){
				Ext.getCmp('btnSearch').handler();
			}
		}
		
	

	});
	
	
	var stockReportGrid = new Ext.grid.GridPanel({
		title : '进销存汇总',
		id : 'grid',
		region : 'center',
		height : '500',
		border : true,
		frame : true,
		store : ds,
		cm : cm,
		tbar : stockTakeBar,
		bbar : pagingBar

	});
	ds.load({params:{start:0,limit:10}});
   var stockReport = new Ext.Panel({
		title : '报表管理',
		region : 'center',//渲染到
		layout : 'border',//布局
		frame : true, 
		//margins : '5 5 5 5',
		//子集
		items : [stockReportTree,stockReportGrid],
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
	
	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : 
		[{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : '<h4 style="padding:10px;font-size:150%;float:left;">无线点餐网页终端</h4>',
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},stockReport,{
			region : 'south',
			height : 30,
			frame : true,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	});
});