var salesSubQueryType = 0;
var salesSubDeptId = -1;
var SALESSUB_PAGE_LIMIT = 22;
var businessHourComboData = [['00', '00' ], ['01', '01' ], ['02', '02' ], ['03', '03' ], ['04', '04' ], ['05', '05'], ['06', '06'], ['07', '07'], ['08', '08'], ['09', '09'], ['10', '10'], ['11', '11'], ['12', '12']];

//生成上下午combo
function buildBusinessAPMCombo(id){
	return {
    		xtype : 'combo',
    		width : 50,
    		value : 0,
    		id : id,
    		forceSelection : true,
			hideLabel : true,
			hidden : true,
    		store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [[0, '上午'], [1, '下午' ]]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			readOnly : false
	};
			    	
}
//生成小时combo
function buildBusinessHourCombo(id){
	return {
			xtype : 'combo',
			forceSelection : true,
			width : 40,
			value : '00',
			id : id,
			hidden : true,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : businessHourComboData
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			readOnly : false
	};
}
//生成分钟combo
function buildBusinessMinCombo(id){
	return {
			xtype : 'combo',
			forceSelection : true,
			width : 40,
			value : '00',
			id : id,
			hidden : true,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : [['00', '00'], ['30', '30']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			readOnly : false
	};
}
//初始化区域combo
function initRegionCombo(statistic){
	var combo = {
		xtype : 'combo',
		forceSelection : true,
		width : 90,
		value : -1,
		id : statistic+'comboRegion',
		store : new Ext.data.SimpleStore({
			fields : ['id', 'name']
		}),
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		allowBlank : false,
		readOnly : false,
		listeners : {
			render : function(thiz){
				var data = [[-1,'全部']];
				Ext.Ajax.request({
					url : '../../QueryRegion.do',
					params : {
						dataSource : 'normal'
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
			select : function(thiz, record, index){
				Ext.getCmp(statistic+'salesSubBtnSearch').handler();
			}
		}
	};
	return combo;
}

//市别操作
function statistic_oBusinessHourData(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var apmBegin = Ext.getCmp(c.statistic+'comboBusinessBeginApm');
	var openingHour = Ext.getCmp((c.statistic+'comboBegionHour'));
	var openingMin = Ext.getCmp((c.statistic+'comboBegionMin'));
	var txtBusinessHourBegin = Ext.getCmp(c.statistic+'txtBusinessHourBegin');
	
	var apmEnd = Ext.getCmp(c.statistic+'comboBusinessEndApm');
	var endingHour = Ext.getCmp(c.statistic+'comboEndHour');
	var endingMin = Ext.getCmp(c.statistic+'comboEndMin');
	var txtBusinessHourEnd = Ext.getCmp(c.statistic+'txtBusinessHourEnd');
	
	
	if(c.type == 'set'){
		data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		
		txtBusinessHourBegin.show();
		txtBusinessHourEnd.show();
		
		openingHour.hide();
		endingHour.hide();
		openingMin.hide();
		endingMin.hide();
		apmBegin.hide();
		apmEnd.hide();
		Ext.getCmp(c.statistic+'txtBusinessHourBeginText').hide();
		Ext.getCmp(c.statistic+'txtBusinessHourEndText').hide();
		Ext.getCmp(c.statistic+'txtBusinessMinBeginText').hide();
		Ext.getCmp(c.statistic+'txtBusinessMinEndText').hide();	
		
		if(typeof data[2] != 'undefined'){
			
			txtBusinessHourBegin.setText('<font style="color:green; font-size:20px">'+data[2]+'</font>');
			txtBusinessHourEnd.setText('<font style="color:green; font-size:20px">'+data[3]+'</font>');
			
			beginTimes = data[2].split(':');
			endTimes = data[3].split(':');
			
			if(parseInt(beginTimes[0]) > 12){
				apmBegin.setValue(1);
				var openingHourValue = parseInt(beginTimes[0]) - 12;
				openingHourValue = openingHourValue > 9 ? openingHourValue+'' : '0'+openingHourValue;
				openingHour.setValue(openingHourValue);			
			}else{
				apmBegin.setValue(0);
				openingHour.setValue(beginTimes[0]);
			}
			
			if(parseInt(endTimes[0]) > 12){
				apmEnd.setValue(1);
				var endingHourValue = parseInt(endTimes[0]) - 12;
				endingHourValue = endingHourValue > 9 ? endingHourValue+'' : '0'+endingHourValue;
				endingHour.setValue(endingHourValue);		
			}else{
				apmEnd.setValue(0);
				endingHour.setValue(endTimes[0]);
			}
			
			openingMin.setValue(beginTimes[1]);
			
			endingMin.setValue(endTimes[1]);
		}else{
			txtBusinessHourBegin.setText('<font style="color:green; font-size:20px">00:00</font>');
			txtBusinessHourEnd.setText('<font style="color:green; font-size:20px">00:00</font>');	
			
			if(data[0] == -2){
				txtBusinessHourBegin.hide();
				txtBusinessHourEnd.hide();
				
				openingHour.setValue('00');
				endingHour.setValue('00');
				openingMin.setValue('00');
				endingMin.setValue('00');
				apmBegin.setValue(0);
				apmEnd.setValue(0);				
				
				openingHour.show();
				endingHour.show();
				openingMin.show();
				endingMin.show();
				apmBegin.show();
				apmEnd.show();
				Ext.getCmp(c.statistic+'txtBusinessHourBeginText').show();
				Ext.getCmp(c.statistic+'txtBusinessHourEndText').show();
				Ext.getCmp(c.statistic+'txtBusinessMinBeginText').show();
				Ext.getCmp(c.statistic+'txtBusinessMinEndText').show();
			}
			
		}
	}else if(c.type == 'get'){
		openingHour = openingHour.getValue();
		endingHour = endingHour.getValue();
		
		if(apmBegin.getValue() == 1){
			openingHour = parseInt(openingHour) + 12;
		}
		
		if(apmEnd.getValue() == 1){
			endingHour = parseInt(endingHour) + 12;
		}
		
		data.opening = openingHour + ':' + openingMin.getValue();
		
		data.ending = endingHour + ':' + endingMin.getValue();
		
		data.businessHourType = Ext.getCmp(c.statistic+'comboBusinessHour').getValue();
		
		c.data = data;
	}
	return c;
};

/**
 * 初始化时间工具栏
 * @param {} c
 * @return {}
 */
function initTimeBar(c){
	
	var timeBar;
	//返回一条完整工具栏
	if(c.tbarType == 0){
		timeBar = new Ext.Toolbar({
			id : c.statistic+'TimeBar',
			hidden : false,
			height : 28,
			items : [
				{xtype:'tbtext',text:'&nbsp;日期:'}, c.dateCombo, 
			    {xtype:'tbtext',text:'&nbsp;'},  c.beginDate,
			    {xtype:'tbtext',text:'&nbsp;至&nbsp;'}, c.endDate, 
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
				{xtype : 'tbtext', text : '市别:'},
				{
					xtype : 'combo',
					forceSelection : true,
					width : 90,
					value : -1,
					id : c.statistic+'comboBusinessHour',
					store : new Ext.data.SimpleStore({
						fields : ['id', 'name']
					}),
					valueField : 'id',
					displayField : 'name',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					readOnly : false,
					listeners : {
						render : function(thiz){
							var data = [[-1,'全天']];
							Ext.Ajax.request({
								url : '../../QueryBusinessHour.do',
								success : function(res, opt){
									var jr = Ext.decode(res.responseText);
									for(var i = 0; i < jr.root.length; i++){
										data.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
									}
									data.push([-2,'自定义']);
									thiz.store.loadData(data);
									thiz.setValue(-1);
								},
								fialure : function(res, opt){
									thiz.store.loadData(data);
									thiz.setValue(-1);
								}
							});
						},
						select : function(thiz, record, index){
							statistic_oBusinessHourData({data : record.json, type : 'set', statistic : c.statistic});
							if(record.data.id != -2){
								Ext.getCmp(c.statistic+'salesSubBtnSearch').handler();
							}
						}
					}
				},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
				{xtype : 'tbtext', id : c.statistic+'txtBusinessHourBegin', text : '<font style="color:green; font-size:20px">00:00</font>'},
			    buildBusinessAPMCombo(c.statistic+'comboBusinessBeginApm'),
			    buildBusinessHourCombo(c.statistic+'comboBegionHour'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessHourBeginText',hidden : true,text : '时'},
				buildBusinessMinCombo(c.statistic+'comboBegionMin'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessMinBeginText',hidden : true,text : '分'},
				{
					xtype : 'tbtext',
					hidden : false,
					text : '&nbsp;至&nbsp;'
				}, 
				{xtype : 'tbtext', id : c.statistic+'txtBusinessHourEnd', text : '<font style="color:green; font-size:20px">00:00</font>'},
			    buildBusinessAPMCombo(c.statistic+'comboBusinessEndApm'),
				buildBusinessHourCombo(c.statistic+'comboEndHour'),
				{xtype : 'tbtext',id:c.statistic+'txtBusinessHourEndText',hidden : true, text : '时'},
				buildBusinessMinCombo(c.statistic+'comboEndMin'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessMinEndText', hidden : true,text : '分'},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
			]
		});
	}else{ //返回包含时间工具栏的数组用于拼接
		timeBar = [
				{xtype:'tbtext',text:'日期:'}, c.dateCombo, 
			    {xtype:'tbtext',text:'&nbsp;'},  c.beginDate,
			    {xtype:'tbtext',text:'&nbsp;至&nbsp;'}, c.endDate, 
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    {xtype : 'tbtext', text : '市别:'},
				{
					xtype : 'combo',
					forceSelection : true,
					width : 90,
					value : -1,
					id : c.statistic+'comboBusinessHour',
					store : new Ext.data.SimpleStore({
						fields : ['id', 'name']
					}),
					valueField : 'id',
					displayField : 'name',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					allowBlank : false,
					readOnly : false,
					listeners : {
						render : function(thiz){
							var data = [[-1,'全天']];
							Ext.Ajax.request({
								url : '../../QueryBusinessHour.do',
								success : function(res, opt){
									var jr = Ext.decode(res.responseText);
									for(var i = 0; i < jr.root.length; i++){
										data.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
									}
									data.push([-2,'自定义']);
									thiz.store.loadData(data);
									thiz.setValue(-1);
								},
								fialure : function(res, opt){
									thiz.store.loadData(data);
									thiz.setValue(-1);
								}
							});
						},
						select : function(thiz, record, index){
							statistic_oBusinessHourData({data : record.json, type : 'set', statistic : c.statistic});
							if(record.data.id != -2){
								Ext.getCmp(c.statistic+'salesSubBtnSearch').handler();
							}

						}
					}
				},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
				{xtype : 'tbtext', id : c.statistic+'txtBusinessHourBegin', text : '<font style="color:green; font-size:20px">00:00</font>'},
			    buildBusinessAPMCombo(c.statistic+'comboBusinessBeginApm'),
				 buildBusinessHourCombo(c.statistic+'comboBegionHour'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessHourBeginText',hidden : true,text : '时'},
				buildBusinessMinCombo(c.statistic+'comboBegionMin'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessMinBeginText',hidden : true,text : '分'},
				{
					xtype : 'tbtext',
					hidden : false,
					text : '&nbsp;至&nbsp;'
				}, 
				{xtype : 'tbtext', id : c.statistic+'txtBusinessHourEnd', text : '<font style="color:green; font-size:20px">00:00</font>'},
			    buildBusinessAPMCombo(c.statistic+'comboBusinessEndApm'),
				 buildBusinessHourCombo(c.statistic+'comboEndHour'),
				{xtype : 'tbtext',id:c.statistic+'txtBusinessHourEndText',hidden : true, text : '时'},
				buildBusinessMinCombo(c.statistic+'comboEndMin'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessMinEndText', hidden : true,text : '分'},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'}];
	}

	return timeBar;
}

function orderFoodStatPanelInit(){
	orderFoodStatPanelDeptTree = new Ext.tree.TreePanel({
		id : 'orderFoodStatPanelDeptTree',
		region : 'west',
		rootVisible : true,
		frame : true,
		width : 150,	
		animate : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader:new Ext.tree.TreeLoader({    
			dataUrl:'../../QueryDeptTree.do?time='+new Date(),
	        baseParams : {
	        	'restaurantID' : restaurantID
			}
	    }),
		root: new Ext.tree.AsyncTreeNode({
			expanded : true,
            text : '全部',
            leaf : false,
            deptID : '-1'
		}),
		tbar : new Ext.Toolbar({
			height : 26,
			items : []
		}),
        listeners : {
        	load : function(){
        		var treeRoot = orderFoodStatPanelDeptTree.getRootNode().childNodes;
        		for(var i = (treeRoot.length - 1); i >= 0; i--){
					if(treeRoot[i].attributes.deptID == 253){
						orderFoodStatPanelDeptTree.getRootNode().removeChild(treeRoot[i]);
					}
				}
        	},
        	click : function(e){
        		Ext.getDom('lab_salesSubDept_food').innerHTML = e.text;
        		//FIXME 部门多选
/*        		if(e.attributes.deptID == '' || e.attributes.deptID == '-1'){
        			salesSubDeptId = '';
        			if(e.hasChildNodes()){
        				for(var i = 0; i < e.childNodes.length; i++){
        					salesSubDeptId += (i > 0 ? ',' : '');
        					salesSubDeptId += e.childNodes[i].attributes.deptID;
        				}
        			}
        		}else{
        			salesSubDeptId = e.attributes.deptID;
        		}*/	     
        		salesSubDeptId = e.attributes.deptID;
        	},
        	dblclick : function(e){
        		Ext.getCmp('foodStatistic_salesSubBtnSearch').handler();
        	}
        }
	});
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 90,
		readOnly : false,
		maxValue : new Date(),
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	var endDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 90,
		maxValue : new Date(),
		readOnly : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('foodStatistic_salesSubBtnSearch').handler();
		}
	});
	var foodName = new Ext.form.TextField({
		width : 100
	});

	var orderFoodStatPanelGridTbarItem = [{
			xtype : 'tbtext',
			text : String.format(Ext.ux.txtFormat.typeName, '部门', 'lab_salesSubDept_food', '----')
		},
	    {xtype:'tbtext',text:'&nbsp;&nbsp;菜品:'}, foodName,
		{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
		{xtype : 'tbtext', text : '区域:'},
		initRegionCombo('foodStatistic_'),
	    '->', {
		text : '搜索',
		iconCls : 'btn_search',
		id : 'foodStatistic_salesSubBtnSearch',
		handler : function(){
			var bd = beginDate.getValue();
			var ed = endDate.getValue();
			if(bd == '' && ed == ''){
				dateCombo.setValue(0);
				dateCombo.fireEvent('select',dateCombo,null,0);
				return;
			}else if(bd != '' && ed == ''){
				Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
			}else if(bd == '' && ed != ''){
				Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
			}
			var gs = orderFoodStatPanelGrid.getStore();
			var data = statistic_oBusinessHourData({type : 'get', statistic : 'foodStatistic_'}).data;
			gs.baseParams['dateBeg'] = beginDate.getRawValue();
			gs.baseParams['dateEnd'] = endDate.getRawValue();
			gs.baseParams['deptID'] = salesSubDeptId;
			gs.baseParams['foodName'] = foodName.getValue();
			gs.baseParams['region'] = Ext.getCmp("foodStatistic_comboRegion").getValue();
			if(parseInt(data.businessHourType) != -1){
				gs.baseParams['opening'] = data.opening;
				gs.baseParams['ending'] = data.ending;
			}else{
				gs.baseParams['opening'] = '';
				gs.baseParams['ending'] = '';			
			}
			gs.load({
				params : {
					start : 0,
					limit : SALESSUB_PAGE_LIMIT
				}
			});
		}
	}, '-', {
		text : '导出',
		iconCls : 'icon_tb_exoprt_excel',
		handler : function(){
			var bd = beginDate.getValue();
			var ed = endDate.getValue();
			if(bd == '' && ed == ''){
				dateCombo.setValue(0);
				dateCombo.fireEvent('select',dateCombo,null,0);
			}else if(bd != '' && ed == ''){
				Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
			}else if(bd == '' && ed != ''){
				Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
			}
			
			var opening, ending;
			var businessHour = statistic_oBusinessHourData({type : 'get', statistic : 'foodStatistic_'}).data;
			if(parseInt(businessHour.businessHourType) != -1){
				opening = businessHour.opening;
				ending = businessHour.ending;
			}else{
				opening = '';
				ending = '';
			}
			
			var url = '../../{0}?region={1}&dataSource={2}&onDuty={3}&offDuty={4}&deptID={5}&foodName={6}&opening={7}&ending={8}';
			url = String.format(
					url, 
					'ExportHistoryStatisticsToExecl.do', 
					Ext.getCmp("foodStatistic_comboRegion").getValue(), 
					'salesFoodDetail',
					beginDate.getValue().format('Y-m-d 00:00:00'),
					endDate.getValue().format('Y-m-d 23:59:59'),
					salesSubDeptId,
					foodName.getValue(),
					opening,
					ending
				);
			window.location = url;
		}
	}];
	
	var orderFoodStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : orderFoodStatPanelGridTbarItem.concat()
	});
	
	orderFoodStatPanelGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../SalesSubStatistics.do',
		[[true, false, false, true], 
         ['菜品','food.name', 150], 
         ['销量','salesAmount','','right','Ext.ux.txtFormat.gridDou'], 
         ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
         ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
         ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou'],
         ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], 
         ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou'],
         ['均价','avgPrice','','right','Ext.ux.txtFormat.gridDou'], 
         ['单位成本','avgCost','','right','Ext.ux.txtFormat.gridDou']
		],
		SalesSubStatRecord.getKeys().concat(['food', 'food.name']),
		[ ['isPaging', true], ['dataType', 1], ['queryType', 1]],
		SALESSUB_PAGE_LIMIT,
		'',
		[orderFoodStatPanelGridTbar, initTimeBar({beginDate:beginDate, endDate:endDate,dateCombo:dateCombo,statistic : 'foodStatistic_',tbarType: 0})]
	);
	orderFoodStatPanelGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('foodStatistic_salesSubBtnSearch').handler();
		}
	}];
	orderFoodStatPanelGrid.region = 'center';
	orderFoodStatPanelGrid.on('render', function(){
		dateCombo.setValue(1);
		dateCombo.fireEvent('select', dateCombo, null, 1);
	});
	orderFoodStatPanelGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = orderFoodStatPanelGrid.getView().getRow(store.getCount()-1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < orderFoodStatPanelGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';
				sumRow.style.color = 'green';
			}
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 10).innerHTML = '--';
			orderFoodStatPanelGrid.getView().getCell(store.getCount()-1, 11).innerHTML = '--';
		}
	});
	orderFoodStatPanel = new Ext.Panel({
		title : '菜品统计',
		layout : 'border',
		items : [orderFoodStatPanelDeptTree, orderFoodStatPanelGrid]
	});	
}

function kitchenGroupTextTpl(rs){
	return '部门:'+rs[0].get('dept.name');
}

function kitchenStatPanelInit(){
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 90,
		maxValue : new Date(),
		readOnly : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	var endDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 90,
		maxValue : new Date(),
		readOnly : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('kitchenStatistic_salesSubBtnSearch').handler();
		}
	});
	
	var kitchenStatPanelGridTbarItem = [
			{xtype : 'tbtext', text : '区域:'},
			initRegionCombo('kitchenStatistic_'),'->', {
			text : '展开/收缩',
			iconCls : 'icon_tb_toggleAllGroups',
			handler : function(){
				kitchenStatPanelGrid.getView().toggleAllGroups();
			}
		}, '-', {
			text : '搜索',
			id : 'kitchenStatistic_salesSubBtnSearch',
			iconCls : 'btn_search',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					endDate.setValue(new Date());
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var gs = kitchenStatPanelGrid.getStore();
				var data = statistic_oBusinessHourData({type : 'get', statistic : 'kitchenStatistic_'}).data;
				gs.baseParams['dateBeg'] = beginDate.getRawValue();
				gs.baseParams['dateEnd'] = endDate.getRawValue();
				gs.baseParams['region'] = Ext.getCmp("kitchenStatistic_comboRegion").getValue();
				if(parseInt(data.businessHourType) != -1){
					gs.baseParams['opening'] = data.opening;
					gs.baseParams['ending'] = data.ending;
				}else{
					gs.baseParams['opening'] = '';
					gs.baseParams['ending'] = '';					
				}
				gs.load();
				kitchenStatPanelGrid.getView().expandAllGroups();
			}
		}, '-', {
			text : '导出',
	//			hidden : true,
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var opening, ending;
				var businessHour = statistic_oBusinessHourData({type : 'get', statistic : 'kitchenStatistic_'}).data;
				if(parseInt(businessHour.businessHourType) != -1){
					opening = businessHour.opening;
					ending = businessHour.ending;
				}else{
					opening = '';
					ending = '';
				}
				
				var url = '../../{0}?region={1}&dataSource={2}&onDuty={3}&offDuty={4}&opening={5}&ending={6}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						Ext.getCmp("kitchenStatistic_comboRegion").getValue(), 
						'salesByKitchen',
						beginDate.getValue().format('Y-m-d 00:00:00'),
						endDate.getValue().format('Y-m-d 23:59:59'),
						opening,
						ending
					);
				window.location = url;
			}
		}];
	
	var kitchenStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [initTimeBar({beginDate:beginDate, endDate:endDate,dateCombo:dateCombo, tbarType : 1, statistic : 'kitchenStatistic_'}).concat(kitchenStatPanelGridTbarItem)]
	});
	
	kitchenStatPanelGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../SalesSubStatistics.do',
		[[true, false, false, false], 
	     ['分厨','kitchen.name'], 
	     ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou'],
	     ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], 
         ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou'],
	     ['dept.id','dept.id', 10]
		],
		SalesSubStatRecord.getKeys().concat(['dept', 'dept.id', 'dept.name', 'kitchen', 'kitchen.name']),
		[['dataType', 1], ['queryType', 2]],
		SALESSUB_PAGE_LIMIT,
		{
			name : 'dept.id',
			hide : true,
			sort : 'dept.id'
		},
		kitchenStatPanelGridTbar
	);
	kitchenStatPanelGrid.view = new Ext.grid.GroupingView({   
        forceFit:true,   
        groupTextTpl : '{[kitchenGroupTextTpl(values.rs)]}'
    });
	kitchenStatPanelGrid.on('render', function(){
		dateCombo.setValue(1);
		dateCombo.fireEvent('select', dateCombo, null, 1);
	});
	kitchenStatPanel = new Ext.Panel({
		title : '分厨统计',
		layout : 'fit',
		items : [kitchenStatPanelGrid]
	});	
}

function deptStatPanelInit(){
	var beginDate = new Ext.form.DateField({
		xtype : 'datefield',		
		format : 'Y-m-d',
		width : 90,
		maxValue : new Date(),
		readOnly : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
			}
		}
	});
	var endDate = new Ext.form.DateField({
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 90,
		maxValue : new Date(),
		readOnly : false,
		listeners : {
			blur : function(thiz){									
				Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
			}
		}
	});
	var dateCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('deptStatistic_salesSubBtnSearch').handler();
		}
	});
	
	var deptStatPanelGridTbarItem = [
		{xtype : 'tbtext', text : '区域:'},
		initRegionCombo('deptStatistic_'),
		'->', {
			text : '搜索',
			id : 'deptStatistic_salesSubBtnSearch',
			iconCls : 'btn_search',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					endDate.setValue(new Date());
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				var gs = deptStatPanelGrid.getStore();
				var data = statistic_oBusinessHourData({type : 'get', statistic : 'deptStatistic_'}).data;
				gs.baseParams['dateBeg'] = beginDate.getRawValue();
				gs.baseParams['dateEnd'] = endDate.getRawValue();
				gs.baseParams['region'] = Ext.getCmp("deptStatistic_comboRegion").getValue();
				if(parseInt(data.businessHourType) != -1){
					gs.baseParams['opening'] = data.opening;
					gs.baseParams['ending'] = data.ending;
				}else{
					gs.baseParams['opening'] = '';
					gs.baseParams['ending'] = '';
				}
				
				gs.load();
			}
		}, '-', {
			text : '导出',
//			hidden : true,
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				var bd = beginDate.getValue();
				var ed = endDate.getValue();
				if(bd == '' && ed == ''){
					dateCombo.setValue(0);
					dateCombo.fireEvent('select',dateCombo,null,0);
				}else if(bd != '' && ed == ''){
					Ext.ux.checkDuft(true, beginDate.getId(), endDate.getId());
				}else if(bd == '' && ed != ''){
					Ext.ux.checkDuft(false, beginDate.getId(), endDate.getId());
				}
				
				var opening, ending;
				var businessHour = statistic_oBusinessHourData({type : 'get', statistic : 'deptStatistic_'}).data;
				if(parseInt(businessHour.businessHourType) != -1){
					opening = businessHour.opening;
					ending = businessHour.ending;
				}else{
					opening = '';
					ending = '';
				}				
				
				
				var url = '../../{0}?region={1}&dataSource={2}&onDuty={3}&offDuty={4}&opening={5}&ending={6}';
				url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						Ext.getCmp("deptStatistic_comboRegion").getValue(), 
						'salesByDept',
						beginDate.getValue().format('Y-m-d 00:00:00'),
						endDate.getValue().format('Y-m-d 23:59:59'),
						opening,
						ending
					);
				window.location = url;
			}
		}];
	
	deptStatPanelGridTbar = new Ext.Toolbar({
		height : 26,
		items : [initTimeBar({dateCombo:dateCombo, beginDate: beginDate, endDate:endDate, statistic : 'deptStatistic_'}).concat(deptStatPanelGridTbarItem)]
	});
	
	
	var deptStatPanelGrid = createGridPanel(
		'',
		'',
		'',
		'',
		'../../SalesSubStatistics.do',
		[[true, false, false, false], 
	     ['部门','dept.name'],
	     ['营业额','income',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['折扣额','discount',,'right','Ext.ux.txtFormat.gridDou'], 
	     ['赠送额','gifted',,'right','Ext.ux.txtFormat.gridDou'],
	     ['成本','cost','','right','Ext.ux.txtFormat.gridDou'], 
         ['成本率','costRate','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利','profit','','right','Ext.ux.txtFormat.gridDou'], 
         ['毛利率','profitRate','','right','Ext.ux.txtFormat.gridDou']
		],
		SalesSubStatRecord.getKeys().concat(['dept', 'dept.name']),
		[['dataType', 1], ['queryType', 0]],
		SALESSUB_PAGE_LIMIT,
		null,
		[deptStatPanelGridTbar]
	);
	deptStatPanelGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = deptStatPanelGrid.getView().getRow(store.getCount()-1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < deptStatPanelGrid.getColumnModel().getColumnCount(); i++){
				var sumRow = deptStatPanelGrid.getView().getCell(store.getCount()-1, i);
				sumRow.style.fontSize = '15px';
				sumRow.style.fontWeight = 'bold';		
				sumRow.style.color = 'green';
			}
			deptStatPanelGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			deptStatPanelGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
		}
	});
	deptStatPanelGrid.on('render', function(){
		dateCombo.setValue(1);
		dateCombo.fireEvent('select', dateCombo, null, 1);
	});
	deptStatPanel = new Ext.Panel({
		title : '部门统计',
		layout : 'fit',
		items : [deptStatPanelGrid]
	});
}

function salesSubWinTabPanelInit(){
	if(!orderFoodStatPanel){
		orderFoodStatPanelInit();		
	}
	if(!kitchenStatPanel){
		kitchenStatPanelInit();		
	}
	if(!deptStatPanel){
		deptStatPanelInit();		
	}
	
	salesSubWinTabPanel = new Ext.TabPanel({
		xtype : 'tabpanel',
		region : 'center',
		frame : true,
		activeTab : 0,
		border : false,
		items : [orderFoodStatPanel, kitchenStatPanel, deptStatPanel],
		listeners : {
			tabchange : function(thiz, tab){
			
			}
		}
	});	
}

Ext.onReady(function(){
	if(!salesSubWinTabPanel){
		salesSubWinTabPanelInit();
	}
	salesSubWinTabPanel.setActiveTab(orderFoodStatPanel);
	new Ext.Panel({
		renderTo : 'divSalesSubStatistics',//渲染到
		id : 'salesSubStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divSalesSubStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divSalesSubStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		//子集
		items : [salesSubWinTabPanel]
	});
	
});
