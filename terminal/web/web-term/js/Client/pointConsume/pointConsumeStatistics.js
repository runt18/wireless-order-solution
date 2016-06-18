Ext.onReady(function(){
	
	var beginDate = new Ext.form.DateField({
		id : 'beginDate_combo_pointConsumeStatistics',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var endDate = new Ext.form.DateField({
		id : 'endDate_combo_pointConsumeStatistics', 
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	//日期组件
	var dataCombo = Ext.ux.createDateCombo({
		beginDate : beginDate,
		endDate : endDate,
		callback : function(){
			Ext.getCmp('search_btn_point').handler();
		}
	});
	
	//操作类型
	var operateType_combo_point = new Ext.form.ComboBox({
		id : 'operateType_combo_point',
		readOnly : false,
		forceSelection : true,
		width : 123,
		listWidth :120,
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
				$.ajax({
					url : '../../OperateMemberOperation.do',
					type : 'post',
					dataType : 'json',
					data : {
						dataSource : 'getMemberOperationType'
					},
					success : function(jr){
						var data = [[-1, '全部']];
						if(jr.success){
							for(var i = 0; i < jr.root[0].operateType.length; i++){
								data.push([jr.root[0].operateType[i]['value'], jr.root[0].operateType[i]['name']]);
							}
						}
						
						thiz.store.loadData(data);
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
				Ext.getCmp('search_btn_point').handler();
			}
		}
	});
	
	//门店类型
	var branch_combo_point = new Ext.form.ComboBox({
		id : 'branch_combo_point',
		readOnly : false,
		forceSelection : true,
		width : 123,
		listWidth :120,
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
				var data = [];
				Ext.Ajax.request({
					url : '../../OperateRestaurant.do',
					params : {
						dataSource : 'getByCond',
						id : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						if(jr.root[0].typeVal != '2'){
							var data = [];
							data.push([jr.root[0]['id'], jr.root[0]['name']]);
							thiz.store.loadData(data);
							thiz.setValue(jr.root[0].id);
							thiz.fireEvent('select');
						}else{
							var data = [[-1, '全部']];
							data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
							
							for(var i = 0; i < jr.root[0].branches.length; i++){
								data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
							}
							
							thiz.store.loadData(data);
							thiz.setValue(-1);
							thiz.fireEvent('select');
						}
					}
				});
			},
			select : function(){
				Ext.getCmp('search_btn_point').handler();
			}
		}
	});
	
	
	
	
	var businessHour;
	var pointConsumeTbarItem = [{
		xtype : 'tbtext',
		width : 10
	}, {
		xtype : 'tbtext',
		text : '会员手机/会员卡号/会员名'
	}, {
		xtype : 'textfield',
		id : 'pointMember_textfield_point',
		width : 150
	}, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;操作类型'
	}, operateType_combo_point, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;选择门店'
	}, branch_combo_point, '->', {
		text : '搜索',
		id : 'search_btn_point',
		iconCls : 'btn_search',
		handler : function(e, a){
			if(!beginDate.isValid() || !endDate.isValid){
				return;
			}
			
			var store = pointConsumePanel.getStore();
			store.baseParams['pointChanged'] = 'true';
			store.baseParams['dataSource'] = 'history';
			store.baseParams['onDuty'] = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00');
			store.baseParams['offDuty'] = Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59');
			store.baseParams['fuzzy'] = Ext.getCmp('pointMember_textfield_point').getValue();
			store.baseParams['branchId'] = branch_combo_point.getValue();
			store.baseParams['detailOperate'] = operateType_combo_point.getValue();
			store.baseParams['isPaging'] = 'true';
			store.load({
				params : {
					start : 0,
					limit : 30
				}
			});
			
			
		}
	}, {
		text : '导出',
		iconCls : 'icon_tb_exoprt_excel',
		handler : function(){
			var url = '../../{0}?dataSource={1}&pointChanged={2}&onDuty={3}&offDuty={4}&fuzzy={5}&branchId={6}&detailOperate={7}';
		
			url = String.format(
				url,
				'ExportHistoryStatisticsToExecl.do',
				'pointConsume',
				'true',
				Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00'),
				Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59'),
				Ext.getCmp('pointMember_textfield_point').getValue(),
				branch_combo_point.getValue(),
				operateType_combo_point.getValue()
			);
			
			window.location = url;
			
		}
	}]
	
	var pointConsumeTbar = Ext.ux.initTimeBar({
		beginDate : beginDate,
		endDate : endDate,
		dateCombo : dataCombo,
		tbarType : 2,
		statistic : 'pointConsume_',
		callback : function businessHourSelect(){
			hours  = null;
		}
	}).concat(pointConsumeTbarItem);
	
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '操作日期', dataIndex : 'operateDateFormat', width : 30},
		{header : '账单号', dataIndex : 'orderId', width : 30},
		{header : '会员名称', dataIndex : 'member.name',width : 20},
		{header : '会员手机', dataIndex : 'member.mobile', width : 20},
		{header : '会员卡号', dataIndex : 'member.memberCard', width : 20},
		{header : '积分余额', dataIndex : 'remainingPoint',width : 20},
		{header : '变动积分', dataIndex : 'deltaPoint',width : 20},
		{header : '操作类型', dataIndex : 'operateTypeText',width : 20},
		{header : '操作人', dataIndex : 'staffName',width : 20},
		{header : '门店名称', dataIndex : 'branchName',width : 20},
		{header : '备注', dataIndex : 'comment'}
	]);
	
	//默认排序
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../QueryMemberOperation.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root', idProperty : ''}, [
			 {name : 'operateDateFormat'},
			 {name : 'orderId'},
             {name : 'member.name'},
             {name : 'member.mobile'},
             {name : 'member.memberCard'},
             {name : 'remainingPoint'},
             {name : 'deltaPoint'},
             {name : 'operateTypeText'},
             {name : 'staffName'},
             {name : 'branchName'},
             {name : 'coupon'},
             {name : 'comment'}
		])
	});
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : 30,
		store : ds,
		displayInfo : true,
		displayMsg : "显示第{0} 条到 {1} 条记录, 共 {2}条",
		emptyMsg : " 没有记录"
	});
	
	var pointConsumePanel = new Ext.grid.GridPanel({
		border : false,
		frame : false,
		store : ds,	
		cm : cm,
		viewConfig : {
			forceFit : true
		},
		loadMask : {
			msg : '数据加载中,请稍后....'
		},
		tbar : pointConsumeTbar,
		bbar : pagingBar
	});
	

	//定义couponGrid的位置
	pointConsumePanel.region = 'center';
	
	pointConsumePanel.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('search_btn_point').handler();
		}
	}];
	
	var paginBar = new Ext.PagingToolbar({
		pageSize : 30,
		store : ds,
		displayInfo : true,
		displayMsg : "显示第{0} 条到{1}条记录, 共{2}条" 	,
		emptyMsg : "没有记录"
	});
	
	new Ext.Panel({
		renderTo : 'body_div_couponEffectStatistics',
		width : parseInt(Ext.getDom('body_div_couponEffectStatistics').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('body_div_couponEffectStatistics').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
		frame : true,
		items : [pointConsumePanel]
	});
	
	dataCombo.setValue(1);
	dataCombo.fireEvent('select', dataCombo, null, 1);
});