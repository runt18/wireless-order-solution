Ext.onReady(function(){
	var stockOut = [[-1, '全部'], [11, '配送发货'], [13, '配送退货']];
	var stockIn = [[-1, '全部'], [12, '配送收货'], [14, '配送回收']];
	var stockType = {
		STOCKIN : {val : 1, desc : '入库'},
		STOCKOUT : {val : 2, desc : '出库'}
	}
	var limitCount = 20;

	function stockDetailHandler(orderID) {
		billDetailWin.show();
		billDetailWin.setTitle('库存单 ');
		billDetailWin.center();
	};

	
	var sDetail_materialStore = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url:'../../QueryMaterial.do?restaurantID=' + restaurantID}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
	         {name : 'id'},
	         {name : 'name'},
	         {name : 'pinyin'}
		])
	});
	
	sDetail_materialStore.load({  
	    params: { 
	    	dataSource : 'normal'
	    }  
	}); 
	
	var materialComb;
	materialComb = new Ext.form.ComboBox({
		forceSelection : true,
		width : 100,
		listWidth : 250,
		maxheight : 300,
		store : sDetail_materialStore,
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
				Ext.getCmp('btnSearch_distributionDelta').handler();
			}
			
		}
	});

	function renderFormat(v){
		if(typeof(v) != 'string'){
			v = Ext.ux.txtFormat.gridDou(v);
		}
		return v;
	}

	
	Ext.form.Field.prototype.msgTarget = 'side';
	
	//定义列模型
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header:'货品', dataIndex:'material', renderer : function(data){
			return data.name ? data.name : '';
		}},
		{header:'配送发货单', dataIndex:'distributionSends', align : 'center', renderer : function(data){
         	var result;
         	for(var i = 0; i < data.length; i++){
         		if(!result){
	         		result = '<a href="javascript:void(0);" data-type="distributionSend">' + data[i].id + '</a>';
	         	}else{
	         		result += ',' + '<a href="javascript:void(0);" data-type="distributionSend">' + data[i].id + '</a>';
	         	}
         	}
         	return result;
		}},
		{header:'配送发货数量', dataIndex:'distributionSendAmount', align : 'center', renderer : function(data){
//			return '<a href="jacascript:void(0);" data-type="distributionSendAmount">' + data + '</a>';		
			return data;
		}},
		{header:'配送收货单', dataIndex:'distributionReceives', align : 'center', renderer : function(data){
         	var result;
         	for(var i = 0; i < data.length; i++){
         		if(!result){
	         		result = '<a href="javascript:void(0);" data-type="distributionReceive">' + data[i].id + '</a>';
	         	}else{
	         		result += ',' + '<a href="javascript:void(0);" data-type="distributionReceive">' + data[i].id + '</a>';
	         	}
         	}
			return result;
		}},
		{header:'配送收货数量', dataIndex:'distributionReceiveAmount', align : 'center', renderer : function(data){
//			return '<a href="jacascript:void(0);" data-type="distributionReceiveAmount">' + data + '</a>';		
			return data;
		}},
		{header: '发货差异数', dataIndex : 'sendDeltaAmount', align : 'center'},
		{header:'配送退货单', dataIndex:'distributionReturns', align : 'center', renderer : function(data){
         	var result;
         	for(var i = 0; i < data.length; i++){
         		if(!result){
	         		result = '<a href="javascript:void(0);" data-type="distributionReturn">' + data[i].id + '</a>';
	         	}else{
	         		result += ',' + '<a href="javascript:void(0);" data-type="distributionReturn">' + data[i].id + '</a>';
	         	}
         	}
			return result;
		}},
		{header:'配送退货数量', dataIndex:'distributionReturnAmount', align : 'center', renderer : function(data){
//			return '<a href="jacascript:void(0);" data-type="distributionReturnAmount">' + data + '</a>';	
			return data;
		}},
		{header:'配送回收单', dataIndex:'distributionRecoverys', align : 'center', renderer : function(data){
			var result;
			for(var i = 0; i < data.length; i++){
				if(!result){
					result = '<a href="javascript:void(0);" data-type="distributionRecovery">' + data[i].id + '</a>';
				}else{
					result += ',' + '<a href="javascript:void(0);" data-type="distributionRecovery">' + data[i].id + '</a>';
				}
			}
			return result;
		}},
		{header:'配送回收数量', dataIndex:'distributionRecoveryAmount', align : 'center', renderer : function(data){
//			return '<a href="jacascript:void(0);" data-type="distributionRecoveryAmount">' + data + '</a>';	
			return data;
		}},
		{header:'退货差异数', dataIndex:'returnDeltaAmount', align : 'center'}
	]);
	cm.defaultSortable = true;
	//数据加载器
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url:'../../QueryDistributionDelta.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
			{name : 'material'},
			{name : 'distributionSends'},
			{name : 'distributionSendAmount'},
			{name : 'distributionReceives'},
			{name : 'distributionReceiveAmount'},
			{name : 'sendDeltaAmount'},
			{name : 'distributionReturns'},
			{name : 'distributionReturnAmount'},
			{name : 'distributionRecoverys'},
			{name : 'distributionRecoveryAmount'},
			{name : 'returnDeltaAmount'}
		])
	});
	
	//日期默认为本月
	var date = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
	
	var dateBegin = new Ext.form.DateField({
		id : 'beginDate_combo_distributionDelta',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var dateEnd = new Ext.form.DateField({
		id : 'endDate_combo_distributionDelta',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readyOnly : false,
		allowBlank : false
	});
	
	var dateCombo;
	dateCombo = Ext.ux.createDateCombo({
		beginDate : dateBegin,
		endDate : dateEnd,
		callback : function(){
			Ext.getCmp('btnSearch_distributionDelta').handler();
		}
	});
	
	var deltaBar = new Ext.Toolbar({
		items : [{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;日期：'
		},
		dateCombo,'　',
		dateBegin,{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;至&nbsp;&nbsp;'
		},
		dateEnd, {
			xtype : 'tbtext',
			text : '&nbsp;'
		},{
			xtype : 'tbtext',
			text : '货品:'
		},materialComb,{
			xtype:'tbtext',
			text:'&nbsp;&nbsp;单据编号:'
		},{
			xtype : 'textfield',
			width : 100,
			id : 'fuzzyId_distributionDelta'
		},{
			xtype:'tbtext',
			text:'&nbsp;&nbsp;'
		},{
			xtype : 'tbtext',
			text : '&nbsp;差异数：&nbsp;'
		},{
			xtype : 'textfield',
			width : 100,
			id : 'minSendDeltaAmount_textfield_distriburionDelta'
		},{
			xtype : 'tbtext',
			text : '&nbsp;-&nbsp;'
		},{
			xtype : 'textfield',
			width : 100,
			id : 'maxSendDeltaAmount_textfield_distributionDelta'
		},{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		},'->', {
			text : '刷新',
			id : 'btnSearch_distributionDelta',
			iconCls : 'btn_refresh',
			handler : function(){
				var gridPanelStore = stockDeltaGrid.store;
				gridPanelStore.baseParams['beginDate'] = Ext.getCmp('beginDate_combo_distributionDelta').getValue();
				gridPanelStore.baseParams['endDate'] = Ext.getCmp('endDate_combo_distributionDelta').getValue();
//				gridPanelStore.baseParams['materialCate'] = materialCateComb.getValue();
//				gridPanelStore.baseParams['materialType'] = materialTypeComb.getValue();
				gridPanelStore.baseParams['materialId'] = materialComb.getValue();
				gridPanelStore.baseParams['fuzzyId'] = Ext.getCmp('fuzzyId_distributionDelta').getValue();
				gridPanelStore.baseParams['minDeltaAmount'] = Ext.getCmp('minSendDeltaAmount_textfield_distriburionDelta').getValue();
				gridPanelStore.baseParams['maxDeltaAmount'] = Ext.getCmp('maxSendDeltaAmount_textfield_distributionDelta').getValue();
//				gridPanelStore.baseParams['minReturnDeltaAmount'] = Ext.getCmp('minReturnDeltaAmount_textfiled_distributionDelta').getValue();
//				gridPanelStore.baseParams['maxReturnDeltaAmount'] = Ext.getCmp('maxReturnDeltaAmount_textfield_distributionDelta').getValue();
//				stockDeltaGrid.store.load({
//					baseParams : {
//						start : 0,
//						limit : limitCount
//					}
//				});
				gridPanelStore.load({
					params : {
						start : 0,
						limit : limitCount
					}
				});
			}
		} ,{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
		]
	});
	
	var deltaSecondBar = new Ext.Toolbar({
		items : [
//			{
//			xtype:'tbtext',
//			text:'&nbsp;&nbsp;'
//		},{
//			xtype : 'tbtext',
//			text : '&nbsp;差异数：&nbsp;'
//		},{
//			xtype : 'textfield',
//			width : 100,
//			id : 'minSendDeltaAmount_textfield_distriburionDelta'
//		},{
//			xtype : 'tbtext',
//			text : '&nbsp;-&nbsp;'
//		},{
//			xtype : 'textfield',
//			width : 100,
//			id : 'maxSendDeltaAmount_textfield_distributionDelta'
//		},{
//			xtype : 'tbtext',
//			text : '&nbsp;&nbsp;'
//		}
//		,{
//			xtype : 'tbtext',
//			text : '&nbsp;退货差异数：&nbsp;'
//		},{
//			xtype : 'textfield',
//			width : 100,
//			id : 'minReturnDeltaAmount_textfiled_distributionDelta'
//		},{
//			xtype : 'tbtext',
//			text : '&nbsp;-&nbsp;'
//		},{
//			xtype : 'textfield',
//			width : 100,
//			id : 'maxReturnDeltaAmount_textfield_distributionDelta'
//		}
		]
	});	
	
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : limitCount,
		store : ds,
		displayInfo : true,
		displayMsg : '显示第 {0} 条到 {1} 条记录，共 {2} 条',
		emptyMsg : '没有记录'
	});

	var stockDeltaGrid;
	stockDeltaGrid = new Ext.grid.GridPanel({
		title : '配送差异表',
		id : 'distribution_delta',
		region : 'center',
		height : '500',
		border : true,
		frame : true,
		store : ds,
		cm : cm,
		viewConfig : {
			forceFit : true
		},
		loadMask : {
			msg : '正在读取数据中...'
		},
		keys : {
			key : 13, //enter键
			scope : this,
			fn : function(){
				Ext.getCmp('btnSearch_distributionDelta').handler();
			}
		},
		tbar : deltaBar,
		bbar : pagingBar,
		listeners : {
			render : function(){
				deltaSecondBar.render(stockDeltaGrid.tbar);
			}
		}
	});
	
	
	//汇总
	stockDeltaGrid.getStore().on('load', function(store, records, options){
		$('[data-type=distributionSend]').each(function(index, el){
			el.onclick = function(){
				var beginDate = Ext.getCmp('beginDate_combo_distributionDelta').getValue();
				var endDate = Ext.getCmp('endDate_combo_distributionDelta').getValue();
//				Ext.ux.addTab('stockDistributionAction', '配送任务', 'StockChainManagement_Module/StockDistributionAction.html', function(){
//					Ext.getCmp('beginDate_combo_distribution').setValue(beiginDate);
//					Ext.getCmp('beginEnd_combo_distribution').setValue(endDate);
//					Ext.getCmp('fuzzId_distribution').setValue(el.innerText);
//					Ext.getCmp('stockType_distribution').setValue(2);
//					Ext.getCmp('stockType_distribution').fireEvent('select');
//					Ext.getCmp('subType_distritbuion').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val);
//					Ext.getCmp('searchBtn_distribution').handler();
//				});
				$.ajax({
					url : '../../OperateStockDistribution.do',
					type : 'post',
					data : {
						dataSource : 'getByCond',
						beginDate : beginDate,
						endDate : endDate,
						id : el.innerText,
						stockType : 2,
						subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val,
						containsDetail : true
					},
					dataType : 'json',
					success : function(res, status, req){
						var stockDistributionShower;
						stockDistributionShower = Ext.stockDistributionAction.newInstance({
							subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val,
							cateType : res.root[0].stockAction.cateTypeValue,
							oriId : res.root[0].stockAction.oriStockId,
							oriDate : res.root[0].stockAction.oriStockDateFormat,
							comment : res.root[0].stockAction.comment,
							appover : res.root[0].stockAction.approverName,
							appoverDate : res.root[0].stockAction.approverDateFormat,
							operator : res.root[0].stockAction.operatorName,
							operateDate : res.root[0].stockAction.oriStockDateFormat,
							associateId : res.associateId ? res.associateId : '',
							actionType : res.root[0].stockAction.typeValue,
							stockInRestaurant : res.root[0].stockInRestaurant.id,
							stockInRestaurantText : res.root[0].stockInRestaurant.name,
							stockOutRestaurant : res.root[0].stockOutRestaurant.id,
							stockOutRestaurantText : res.root[0].stockOutRestaurant.name,
							details : res.root[0].stockAction.stockDetails,
							actualPrice : res.root[0].stockAction.actualPrice,
							isOnlyShow : true,
							checkWithOutMsg : true,
							callback : function(param){
								Ext.example.msg('温磬提示', '查看模式下不能修改库单');		
							}
						}).open();
						
					},
					error : function(req, status, err){
					
					}
				});
			}
		});
		
		$('[data-type=distributionReceive]').each(function(index, el){
			el.onclick = function(){
				var beginDate = Ext.getCmp('beginDate_combo_distributionDelta').getValue();
				var endDate = Ext.getCmp('endDate_combo_distributionDelta').getValue();
//				Ext.ux.addTab('stockDistributionAction', '配送任务', 'StockChainManagement_Module/StockDistributionAction.html', function(){
//					Ext.getCmp('beginDate_combo_distribution').setValue(beiginDate);
//					Ext.getCmp('beginEnd_combo_distribution').setValue(endDate);
//					Ext.getCmp('fuzzId_distribution').setValue(el.innerText);
//					Ext.getCmp('stockType_distribution').setValue(1);
//					Ext.getCmp('stockType_distribution').fireEvent('select');
//					Ext.getCmp('subType_distritbuion').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val);
//					Ext.getCmp('searchBtn_distribution').handler();
//				});
				$.ajax({
					url : '../../OperateStockDistribution.do',
					type : 'post',
					data : {
						dataSource : 'getByCond',
						beginDate : beginDate,
						endDate : endDate,
						id : el.innerText,
						stockType : 1,
						subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val,
						containsDetail : true,
						isGroupDistirbution : true
					},
					dataType : 'json',
					success : function(res, status, req){
						var stockDistributionShower;
						stockDistributionShower = Ext.stockDistributionAction.newInstance({
							subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val,
							cateType : res.root[0].stockAction.cateTypeValue,
							oriId : res.root[0].stockAction.oriStockId,
							oriDate : res.root[0].stockAction.oriStockDateFormat,
							comment : res.root[0].stockAction.comment,
							appover : res.root[0].stockAction.approverName,
							appoverDate : res.root[0].stockAction.approverDateFormat,
							operator : res.root[0].stockAction.operatorName,
							operateDate : res.root[0].stockAction.oriStockDateFormat,
							associateId : res.associateId ? res.associateId : '',
							actionType : res.root[0].stockAction.typeValue,
							stockInRestaurant : res.root[0].stockInRestaurant.id,
							stockInRestaurantText : res.root[0].stockInRestaurant.name,
							stockOutRestaurant : res.root[0].stockOutRestaurant.id,
							stockOutRestaurantText : res.root[0].stockOutRestaurant.name,
							details : res.root[0].stockAction.stockDetails,
							actualPrice : res.root[0].stockAction.actualPrice,
							isOnlyShow : true,
							checkWithOutMsg : true,
							callback : function(param){
								Ext.example.msg('温磬提示', '查看模式下不能修改库单');		
							}
						}).open();
						
					},
					error : function(req, status, err){
					
					}
				});
			}
		});
		
		$('[data-type=distributionReturn]').each(function(index, el){
			el.onclick = function(){
				var beginDate = Ext.getCmp('beginDate_combo_distributionDelta').getValue();
				var endDate = Ext.getCmp('endDate_combo_distributionDelta').getValue();
//				Ext.ux.addTab('stockDistributionAction', '配送任务', 'StockChainManagement_Module/StockDistributionAction.html', function(){
//					Ext.getCmp('beginDate_combo_distribution').setValue(beiginDate);
//					Ext.getCmp('beginEnd_combo_distribution').setValue(endDate);
//					Ext.getCmp('fuzzId_distribution').setValue(el.innerText);
//					Ext.getCmp('stockType_distribution').setValue(2);
//					Ext.getCmp('stockType_distribution').fireEvent('select');
//					Ext.getCmp('subType_distritbuion').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RETURN.val);
//					Ext.getCmp('searchBtn_distribution').handler();
//				});
				
				$.ajax({
					url : '../../OperateStockDistribution.do',
					type : 'post',
					data : {
						dataSource : 'getByCond',
						beginDate : beginDate,
						endDate : endDate,
						id : el.innerText,
						stockType : 2,
						subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RETURN.val,
						containsDetail : true,
						isGroupDistirbution : true
					},
					dataType : 'json',
					success : function(res, status, req){
						var stockDistributionShower;
						stockDistributionShower = Ext.stockDistributionAction.newInstance({
							subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RETURN.val,
							cateType : res.root[0].stockAction.cateTypeValue,
							oriId : res.root[0].stockAction.oriStockId,
							oriDate : res.root[0].stockAction.oriStockDateFormat,
							comment : res.root[0].stockAction.comment,
							appover : res.root[0].stockAction.approverName,
							appoverDate : res.root[0].stockAction.approverDateFormat,
							operator : res.root[0].stockAction.operatorName,
							operateDate : res.root[0].stockAction.oriStockDateFormat,
							associateId : res.associateId ? res.associateId : '',
							actionType : res.root[0].stockAction.typeValue,
							stockInRestaurant : res.root[0].stockInRestaurant.id,
							stockInRestaurantText : res.root[0].stockInRestaurant.name,
							stockOutRestaurant : res.root[0].stockOutRestaurant.id,
							stockOutRestaurantText : res.root[0].stockOutRestaurant.name,
							details : res.root[0].stockAction.stockDetails,
							actualPrice : res.root[0].stockAction.actualPrice,
							isOnlyShow : true,
							checkWithOutMsg : true,
							callback : function(param){
								Ext.example.msg('温磬提示', '查看模式下不能修改库单');		
							}
						}).open();
						
					},
					error : function(req, status, err){
					
					}
				});
			}
		});
		
		$('[data-type=distributionRecovery]').each(function(index, el){
			el.onclick = function(){
				var beginDate = Ext.getCmp('beginDate_combo_distributionDelta').getValue();
				var endDate = Ext.getCmp('endDate_combo_distributionDelta').getValue();
//				Ext.ux.addTab('stockDistributionAction', '配送任务', 'StockChainManagement_Module/StockDistributionAction.html', function(){
//					Ext.getCmp('beginDate_combo_distribution').setValue(beiginDate);
//					Ext.getCmp('beginEnd_combo_distribution').setValue(endDate);
//					Ext.getCmp('fuzzId_distribution').setValue(el.innerText);
//					Ext.getCmp('stockType_distribution').setValue(1);
//					Ext.getCmp('stockType_distribution').fireEvent('select');
//					Ext.getCmp('subType_distritbuion').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val);
//					Ext.getCmp('searchBtn_distribution').handler();
//				});
				
				$.ajax({
					url : '../../OperateStockDistribution.do',
					type : 'post',
					data : {
						dataSource : 'getByCond',
						beginDate : beginDate,
						endDate : endDate,
						id : el.innerText,
						stockType : 1,
						subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val,
						containsDetail : true
					},
					dataType : 'json',
					success : function(res, status, req){
						var stockDistributionShower;
						stockDistributionShower = Ext.stockDistributionAction.newInstance({
							subType : Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val,
							cateType : res.root[0].stockAction.cateTypeValue,
							oriId : res.root[0].stockAction.oriStockId,
							oriDate : res.root[0].stockAction.oriStockDateFormat,
							comment : res.root[0].stockAction.comment,
							appover : res.root[0].stockAction.approverName,
							appoverDate : res.root[0].stockAction.approverDateFormat,
							operator : res.root[0].stockAction.operatorName,
							operateDate : res.root[0].stockAction.oriStockDateFormat,
							associateId : res.associateId ? res.associateId : '',
							actionType : res.root[0].stockAction.typeValue,
							stockInRestaurant : res.root[0].stockInRestaurant.id,
							stockInRestaurantText : res.root[0].stockInRestaurant.name,
							stockOutRestaurant : res.root[0].stockOutRestaurant.id,
							stockOutRestaurantText : res.root[0].stockOutRestaurant.name,
							details : res.root[0].stockAction.stockDetails,
							actualPrice : res.root[0].stockAction.actualPrice,
							isOnlyShow : true,
							checkWithOutMsg : true,
							callback : function(param){
								Ext.example.msg('温磬提示', '查看模式下不能修改库单');		
							}
						}).open();
						
					},
					error : function(req, status, err){
					
					}
				});
			}
		});
		
		$('[data-type=distributionSendAmount]').each(function(index, el){
			el.onclick = function(){
				var beginDate = Ext.getCmp('beginDate_combo_distributionDelta').getValue();
				var endDate = Ext.getCmp('endDate_combo_distributionDelta').getValue();
				var selectCol = stockDeltaGrid.getSelectionModel().getSelected();
				Ext.ux.addTab('distributionDetailReport', '配送明细', 'StockChainManagement_Module/DistributionDetailReport.html', function(){
					Ext.getCmp('beginDate_combo_distributionDetail').setValue(beginDate);
					Ext.getCmp('endDate_combo_distributionDetail').setValue(endDate);
					Ext.getCmp('fuzzyId_distributionDetail').setValue(selectCol['data']['distributionSends'][0]['id']);
					Ext.getCmp('stockType_distributionDetail').setValue(2);
					Ext.getCmp('stockType_distributionDetail').fireEvent('select');
					Ext.getCmp('subType_distritbuionDetail').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_SEND.val);
					Ext.getCmp('btnSearch_distributionDetail').handler();
				});
			}
		});
		
		$('[data-type=distributionReceiveAmount]').each(function(index, el){
			el.onclick = function(){
				var beginDate = Ext.getCmp('beginDate_combo_distributionDelta').getValue();
				var endDate = Ext.getCmp('endDate_combo_distributionDelta').getValue();
				var selectCol = stockDeltaGrid.getSelectionModel().getSelected();
				Ext.ux.addTab('distributionDetailReport', '配送任务', 'StockChainManagement_Module/DistributionDetailReport.html', function(){
					Ext.getCmp('beginDate_combo_distributionDetail').setValue(beginDate);
					Ext.getCmp('endDate_combo_distributionDetail').setValue(endDate);
					Ext.getCmp('fuzzyId_distributionDetail').setValue(selectCol['data']['distributionSends'][0]['id']);
					Ext.getCmp('stockType_distributionDetail').setValue(1);
					Ext.getCmp('stockType_distributionDetail').fireEvent('select');
					Ext.getCmp('subType_distritbuionDetail').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECEIVE.val);
					Ext.getCmp('btnSearch_distributionDetail').handler();
				});
			}
		});
		
		$('[data-type=distributionReturnAmount]').each(function(index, el){
			el.onclick = function(){
				var beginDate = Ext.getCmp('beginDate_combo_distributionDelta').getValue();
				var endDate = Ext.getCmp('endDate_combo_distributionDelta').getValue();
				var selectCol = stockDeltaGrid.getSelectionModel().getSelected();
				Ext.ux.addTab('distributionDetailReport', '配送任务', 'StockChainManagement_Module/DistributionDetailReport.html', function(){
					Ext.getCmp('beginDate_combo_distributionDetail').setValue(beginDate);
					Ext.getCmp('endDate_combo_distributionDetail').setValue(endDate);
					Ext.getCmp('fuzzyId_distributionDetail').setValue(selectCol['data']['distributionSends'][0]['id']);
					Ext.getCmp('stockType_distributionDetail').setValue(2);
					Ext.getCmp('stockType_distributionDetail').fireEvent('select');
					Ext.getCmp('subType_distritbuionDetail').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RETURN.val);
					Ext.getCmp('btnSearch_distributionDetail').handler();
				});
			}
		});
		
		$('[data-type=distributionRecoveryAmount]').each(function(index, el){
			el.onclick = function(){
				var beginDate = Ext.getCmp('beginDate_combo_distributionDelta').getValue();
				var endDate = Ext.getCmp('endDate_combo_distributionDelta').getValue();
				var selectCol = stockDeltaGrid.getSelectionModel().getSelected();
				Ext.ux.addTab('distributionDetailReport', '配送任务', 'StockChainManagement_Module/DistributionDetailReport.html', function(){
					Ext.getCmp('beginDate_combo_distributionDetail').setValue(beginDate);
					Ext.getCmp('endDate_combo_distributionDetail').setValue(endDate);
					Ext.getCmp('fuzzyId_distributionDetail').setValue(selectCol['data']['distributionSends'][0]['id']);
					Ext.getCmp('stockType_distributionDetail').setValue(1);
					Ext.getCmp('stockType_distributionDetail').fireEvent('select');
					Ext.getCmp('subType_distritbuionDetail').setValue(Ext.stockDistributionAction.stockSubType.values.DISTRIBUTION_RECOVERY.val);
					Ext.getCmp('btnSearch_distributionDetail').handler();
				});
			}
		});
		
		var sumRow = null;
		if(store.getCount() > 0){

			sumRow = stockDeltaGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < stockDeltaGrid.getColumnModel().getColumnCount(); i++){
				var sumCell = stockDeltaGrid.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
			
			
			stockDeltaGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			stockDeltaGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
//			stockDeltaGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			stockDeltaGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
//			stockDeltaGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			stockDeltaGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			stockDeltaGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			stockDeltaGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
			stockDeltaGrid.getView().getCell(store.getCount()-1, 11).innerHTML = '--';
//			stockDeltaGrid.getView().getCell(store.getCount()-1, 15).innerHTML = '--';
		}
		
	});	
	
	
   new Ext.Panel({
		renderTo : 'mainContainer_div_distributionDelta',
		width : parseInt(Ext.getDom('mainContainer_div_distributionDelta').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('mainContainer_div_distributionDelta').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',//布局
		items : [stockDeltaGrid],
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
			}
		}],
		listeners : {
			render : function(){
			}
		}
	});
   
	dateCombo.setValue(1);
	dateCombo.fireEvent('select');
	Ext.getCmp('btnSearch_distributionDelta').handler();
});