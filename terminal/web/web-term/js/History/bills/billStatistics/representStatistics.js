Ext.onReady(function(){
	var hours;
	
	var dateBegin = new Ext.form.DateField({
		id : 'beginDate_combo_represent',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var dateEnd = new Ext.form.DateField({
		id : 'beginEnd_combo_represent',
		xtype : 'datefield',
		format : 'Y-m-d',
		maxValue : new Date(),
		readyOnly : false,
		allowBlank : false
	});
	
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : dateBegin,
		endDate : dateEnd,
		callback : function(){
			Ext.getCmp('represent_btnSearch').handler();
		}
	});
	
	//门店
//	var regionCombo = new Ext.form.ComboBox({
//		id : '',
//		readOnly : false,
//		width : 103,
//		listWidth : 120,
//		store : new Ext.data.SimpleStore({
//			fields : ['id', 'name']
//		}),
//		valueField : 'id',
//		displayField : 'name',
//		forceSelection : true,
//		selectOnFocus : true,
////		typeAhead : true,
//		triggerAction : 'all',
//		mode : 'local',
//		listeners : {
//			render : function(thiz){
//				var data = [];
//				Ext.Ajax.request({
//					url : '../../OperateRestaurant.do',
//					params : {
//						dataSource : 'getByCond',
//						id : restaurantID
//					},
//					success : function(res, opt){
//						var jr = Ext.decode(res.responseText);
//						
//						if(jr.root[0].typeVal != '2'){
//							data.push([jr.root[0]['id'], jr.root[0]['name']]);
//						}else{
//							data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
//						}
//						
//						for(var i = 0; i < jr.root[0].branches.length; i++){
//							data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
//						}
//						
//						thiz.store.loadData(data);
//						thiz.setValue(jr.root[0].id);
//					}
//				});
//			}
//		}
//	});
	
//	var representToolbar = Ext.ux.initTimeBar({
//		beginDate : dateBegin,
//		endDate : dateEnd,
//		dateCombo : dateCombo,
//		tbarType : 1,
//		statistic : 'represent_',
//		callbackground : function businessHourSelect(){
//			hours = null;
//		}
//	}).concat(representToolbar2);
	var representToolbar = new Ext.Toolbar({
		items : [
		{
			xtype : 'label',
			text : '日期:'
		},
		dateCombo,'　',
		dateBegin,{
			xtype : 'label',
			text : '　至　'
		},
		dateEnd,{
			xtype : 'label',
			text : '　推荐人:'
		},{
			id : 'recommendFuzzy_textfield_representStatistics',
			xtype : 'textfield',
			listeners : {
//				keydown : function(e){
//					console.log(e.keyCode);
//					console.log(0);
//				},
//				focus : function(){
//					console.log('focus');
//				}
			}
		},{
			xtype : 'label',
			text : '　关注人:'
		},{
			id : 'subscribeFuzzy_textfield_representStatistics',
			xtype : 'textfield'
		},'->',{
			text : '搜索',
			id : 'represent_btnSearch',
			iconCls : 'btn_search',
			handler : function(thiz){
				var dateBegin = Ext.util.Format.date(Ext.getCmp('beginDate_combo_represent').getValue(), 'Y-m-d');
				var dateEnd = Ext.util.Format.date(Ext.getCmp('beginEnd_combo_represent').getValue(), 'Y-m-d');
				var recommendFuzzy = Ext.getCmp('recommendFuzzy_textfield_representStatistics').getValue();
				var subscribeFuzzy = Ext.getCmp('subscribeFuzzy_textfield_representStatistics').getValue();
				var store = Ext.getCmp('datePanel_gridPanel_representStatistics').getStore();
				store.baseParams['dataSource'] = 'getByCond';
				store.baseParams['dateBegin'] = dateBegin;
				store.baseParams['dateEnd'] = dateEnd;
				store.baseParams['recommendFuzzy'] = recommendFuzzy;
				store.baseParams['subscribeFuzzy'] = subscribeFuzzy;
				store.load();
				
			}
		},'-',{
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel'
		}]
	});
	
	var pagingbar = new Ext.PagingToolbar({
		pageSize : 10,
		store : store,
		displayInfo : true,
		displayMsg : '显示第{0} 条到{1} 条记录，共{2}条',
		emptyMsg : '没有记录'
	}); 
	
	var cm = new Ext.grid.ColumnModel([
	                                   
				new Ext.grid.RowNumberer(),
				{
					header : '日期',
					dataIndex : 'subscribeDate'
				},{
					header : '推荐人',
					dataIndex : 'recommendMember'
				},{
					header : '推荐金额',
					dataIndex : 'recommendMoney'
				},{
					header : '推荐积分',
					dataIndex : 'recommendPoint'
				},{
					header : '关注人',
					dataIndex : 'subscribeMember'
				},{
					header : '关注金额',
					dataIndex : 'subscribeMoney'
				},{
					header : '关注积分',
					dataIndex : 'subscribePoint'
				}]);
	
	var store = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../QueryRepresent.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [{
			name : 'subscribeDate'
		},{
			name : 'recommendMember'
		},{
			name : 'recommendMoney'
		},{
			name : 'recommendPoint'
		},{
			name : 'subscribeMember'
		},{
			name : 'subscribeMoney'
		},{
			name : 'subscribePoint'
		}]),
		baseParams : {
			start : 0,
			limit : 20
		}
	});
	
	
	var representPanel = new Ext.grid.GridPanel({
		id : 'datePanel_gridPanel_representStatistics',
		frame : false,
		autoScroll : true,
		height : parseInt(Ext.getDom('representView_div_representStatistics').parentElement.style.height.replace(/px/g, '')) - 40,
		width : parseInt(Ext.getDom('representView_div_representStatistics').parentElement.style.width.replace(/px/g, '')),
		viewConfig : {
			forceFit : true
		},
		cm : cm,
		loadMask : {
			msg : '数据加载中,请稍后....'
		},
		store : store,
		tbar : representToolbar,
		bbar : pagingbar,
		keys : {
			key : Ext.EventObject.ENTER,
			fn : function(){
				Ext.getCmp('represent_btnSearch').handler();
			}
		}
	});
	
	
	store.on('load', function(){
		if(store.getCount() > 0){
			var sumRow = representPanel.getView().getRow(store.getCount() - 1);
			sumRow.style.backgroundColor = '#EEEEEE';
			for(var i = 0; i < representPanel.getColumnModel().getColumnCount(); i++){
				var sumCell = representPanel.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
			
			representPanel.getView().getCell(store.getCount() - 1 , 1).innerHTML = '汇总';
			representPanel.getView().getCell(store.getCount() - 1, 2).innerHTML = '--';
			representPanel.getView().getCell(store.getCount() - 1, 5).innerHTML = '--';
		}
	});
	
	new Ext.Panel({
		title : '代言统计',
		frame : true,
		renderTo : 'representView_div_representStatistics',
//		width : parseInt(Ext.getDom('representView_div_representStatistics').parentElement.style.width.replace(/px/g, '')),
//		height : parseInt(Ext.getDom('representView_div_representStatistics').parentElement.style.height.replace(/px/g, '')),
		items : [representPanel]
		
	});
	
	dateCombo.setValue('1');
	dateCombo.fireEvent('select', dateCombo, null, 1);
});