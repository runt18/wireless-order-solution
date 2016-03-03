Ext.onReady(function(){
	var materialCateData = {root:[]};
	
	var logOutBut = new Ext.ux.ImageButton({
		imgPath : '../../images/ResLogout.png',
		imgWidth : 50,
		imgHeight : 50,
		tooltip : '登出',
		handler : function(btn){
			
		}
	});
	
	var deptComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 110,
		maxheight : 300,
		store : new Ext.data.SimpleStore({
			fields : ['id', 'name']
		}),
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				var data = [[-1,'全部']];
				Ext.Ajax.request({
					url : '../../OperateDept.do',
					params: { 
				    	dataSource : 'getByCond',
				    	inventory : true
				    }, 
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['id'], jr.root[i]['name']]);
						}
						thiz.store.loadData(data);
						thiz.setValue(-1);
					},
					fialure : function(res, opt){
						thiz.store.loadData(data);
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
				Ext.getCmp('search_btn_stockReport').handler();		
			}
			
		}
	});
	
	var materialStore = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterial.do?restaurantID=' + restaurantID}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
		         {name : 'id'},
		         {name : 'cateName'},
		         {name : 'name'},
		         {name : 'pinyin'}
		]),
		listeners : {
			load : function(thiz, records, ops){
				var PersonRecord = Ext.data.Record.create([
			         {name : 'id'},
			         {name : 'cateName'},
			         {name : 'name'},
			         {name : 'pinyin'}				
				]);
				var newRecord= new PersonRecord({cateName: "货品",id: -1,name: "全部",pinyin: "QB"});   
				thiz.insert(0,newRecord); 
			}
		}
	});
	materialStore.load({  
	    params: { 
	    	dataSource : 'normal'
	    }  
	}); 
	var materialComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 110,
		listWidth : 250,
		maxheight : 300,
		store : materialStore,
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		tpl:'<tpl for=".">' 
			+ '<div class="x-combo-list-item" style="height:18px;">'
			+ '{id} -- {cateName} -- {name} -- {pinyin}'
			+ '</div>'
			+ '</tpl>',
		listeners : {
			beforequery : function(e){
				var combo = e.combo;
				if(!e.forceAll){
					var value = e.query;
					combo.store.filterBy(function(record){
						return record.get('name').indexOf(value) != -1
								|| (record.get('id')+'').indexOf(value) != -1
								|| record.get('pinyin').indexOf(value.toUpperCase()) != -1;
					});
					combo.expand();
					combo.select(0, true);
					return false;
				
				}
			},
			select : function(){
				Ext.getCmp('search_btn_stockReport').handler();		
			}
			
		}
	});
	
	var stockReportTree;

	Ext.form.Field.prototype.msgTarget = 'side';
	

	totalStyle = function(v){
		return "<font color='red' size='3'>"+ Ext.ux.txtFormat.gridDou(v) + "</font>" ;
	};
	amountStyle = function(v){
		return "<font color='green' size='3'>"+ Ext.ux.txtFormat.gridDou(v) + "</font>" ;
	};
	//定义列模型
	var cm = new Ext.grid.ColumnModel([
         new Ext.grid.RowNumberer(),
         {header:'品行编号', dataIndex:'materialId'},
         {header:'品行名称', dataIndex:'materialName',align:'left', width : 130},
         {header:'期初数量', dataIndex:'primeAmount', align:'right', renderer:totalStyle},
         {header:'入库采购', dataIndex:'stockIn', align:'right'},
         {header:'入库调拨', dataIndex:'stockInTransfer', align:'right'},
         {header:'入库报溢', dataIndex:'stockSpill', align:'right'},
         {header:'入库盘盈', dataIndex:'stockTakeMore', align:'right'},
         {header:'入库小计', dataIndex:'stockInAmount', align:'right', renderer:amountStyle},
         {header:'出库退货', dataIndex:'stockOut', align:'right'},
         {header:'出库调拨', dataIndex:'stockOutTransfer', align:'right'},
         {header:'出库报损', dataIndex:'stockDamage', align:'right'},
         {header:'出库盘亏', dataIndex:'stockTakeLess', align:'right'},
         {header:'出库消耗', dataIndex:'useUp', align:'right'},
         {header:'出库小计', dataIndex:'stockOutAmount', align:'right', renderer:amountStyle},
         {header:'期末数量', dataIndex:'finalAmount', align:'right', renderer:totalStyle},
         {header:'期末单价', dataIndex:'finalPrice', align:'right', renderer:Ext.ux.txtFormat.gridDou},
         {header:'期末金额', dataIndex:'finalMoney', align:'right', renderer:totalStyle}]);
	 cm.defaultSortable = true;
			
			
	//数据加载器
	var ds = new Ext.data.Store({
		//proxy : new Ext.data.MemoryProxy(data),
		proxy : new Ext.data.HttpProxy({url:'../../QueryReport.do'}),
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

	var stockTakeTbar = new Ext.Toolbar({
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
			id : 'beginDate_dateField_stockReport',
			allowBlank : false,
			maxValue : new Date(),
			value : new Date(),
            width:100,  
            plugins: 'monthPickerPlugin',  
            format: 'Y-m'
		},
		{xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{ xtype:'tbtext', text:'品项:'},
		materialComb,
		{xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{ xtype:'tbtext', text:'部门:'},
		deptComb,
		'->', {
			text : '搜索',
			id : 'search_btn_stockReport',
			iconCls : 'btn_search',
			handler : function(){
				var cateType = '', cateId = '', materialId = '';
				var sgs = stockReportGrid.getStore();
				var rn = stockReportTree.getSelectionModel().getSelectedNode();
				if(!rn){
					cateType = '';
				}else{
					if(rn.attributes.cate){
						cateType = rn.attributes.cate;
					}else{
						cateId = rn.attributes.cateId;
					}
				}
				if(materialComb.getValue() != ''){
					materialId = materialComb.getValue();
				}
				sgs.baseParams['beginDate'] = Ext.getCmp('beginDate_dateField_stockReport').getValue().format('Y-m');
				sgs.baseParams['cateType'] = cateType;
				sgs.baseParams['cateId'] = cateId;
				sgs.baseParams['materialId'] = materialId;
				sgs.baseParams['deptId'] = deptComb.getValue();
				//load两种加载方式,远程和本地
				sgs.load({
					params : {
						start : 0,
						limit : 17
					}
				});
			}
		}, '-', {
				text : '导出',
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					var cateType = '', cateId = '', materialId = '';
					var rn = stockReportTree.getSelectionModel().getSelectedNode();
					if(!rn){
						cateType = '';
					}else{
						if(rn.attributes.typeId){
							cateType = rn.attributes.typeId;
						}else{
							cateId = rn.attributes.cateId;
						}
					}
					if(materialComb.getValue() != ''){
						materialId = materialComb.getValue();
					}
					
					var url = "../../{0}?beginDate={1}&endDate={2}&cateId={3}&materialId={4}&cateType={5}&deptId={6}&dataSource={7}";
					url = String.format(
						url,
						'ExportHistoryStatisticsToExecl.do',
						Ext.getCmp('beginDate_dateField_stockReport').getValue().format('Y-m'),
						'',
						cateId,
						materialId,
						cateType,
						deptComb.getValue(),
						'stockCollect'
					);
					window.location = url;
				}
			},{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
		]
	});
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : 17,
		store : ds,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : '没有记录'
	});

	
	
	 stockReportTree = new Ext.tree.TreePanel({
		title : '货品类型',
		region : 'west',
		width : 160,
		border : false,
		frame : true, //采用渲染
		rootVisible : false,//显示根节点 
		autoScroll : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader: new Ext.tree.TreeLoader({
			dataUrl : '../../QueryMaterialCate.do',
			baseParams : {
				dataSource : 'tree',
				restaurantID : restaurantID
			}
		}),
        root: new Ext.tree.AsyncTreeNode({
            expanded: true,
            text : '全部货品',
            typeId : '',
            leaf : false
        }),
		listeners : {
			click : function(e){
				//var node = this.getSelectionModel().getSelectedNode();
				Ext.getDom('cateTypeValue').innerHTML = e.text;
				Ext.getCmp('search_btn_stockReport').handler();
				var cateType = '', cateId = '';
				var rn = stockReportTree.getSelectionModel().getSelectedNode();
				if(!rn){
					cateType = '';
				}else{
					if(rn.attributes.typeId){
						cateType = rn.attributes.typeId;
					}else{
						cateId = rn.attributes.cateId;
					}
				};
				materialComb.reset();
	        	materialStore.load({  
		            params: {  
		            	cateType : cateType,
		            	cateId : cateId,  
		            	dataSource : 'normal'
		            }  
	            }); 
			}
		}
	});
	
	
	var stockReportGrid = new Ext.grid.GridPanel({
		title : '进销存汇总',
		region : 'center',
//		width : 1400,
		height : '500',
		store : ds,
		cm : cm,
/*		viewConfig : {
			forceFit : true
		},*/
		loadMask : { msg: '数据请求中,请稍等......' },
		tbar : stockTakeTbar,
		bbar : pagingBar

	});
	
	
	stockReportGrid.getStore().on('load', function(store, records, options){
		
		if(store.getCount() > 0){
			var sumRow = stockReportGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			sumRow.style.color = 'green';
			for(var i = 0; i < stockReportGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = stockReportGrid.getView().getCell(store.getCount() - 1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';					
			}
			stockReportGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			stockReportGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 10).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 11).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 12).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 13).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 14).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 15).innerHTML = '--';
			stockReportGrid.getView().getCell(store.getCount()-1, 16).innerHTML = '--';
			
		}
	});
	
    new Ext.Panel({
		renderTo : 'report_div_stockReport',
		height : parseInt(Ext.getDom('report_div_stockReport').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',//布局
		//margins : '5 5 5 5',
		//子集
		items : [stockReportTree,stockReportGrid]
	});
	
    //加载页面执行查询
    Ext.getCmp('search_btn_stockReport').handler();
});