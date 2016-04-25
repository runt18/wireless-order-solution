Ext.onReady(function(){
	var beginDate = new Ext.form.DateField({
		id : 'beginDate_combo_couponEffectStatistics',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var endDate = new Ext.form.DateField({
		id : 'endDate_combo_couponEffectStatistics', 
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
			Ext.getCmp('couponEffect_btn_couponEffectStatistics').handler();
		}
	});
	
	//优惠券类型选择
	var couponType_combo_couponEffect = new Ext.form.ComboBox({
		readOnly : false,
		forceSelection : true,
		width : 73,
		listWidth : 120,
		store : new Ext.data.SimpleStore({
			fields : ['staffID', 'staffName']
		}),
		valueField : 'staffID',
		displayField : 'staffName',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				var data = [['', '全部']];
				Ext.Ajax.request({
					url : '../../OperatePromotion.do',
					params : {
						dataSource : 'getByCond'
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i].coupon['id'], jr.root[i].coupon['name']]);
						}
						thiz.store.loadData(data);
						thiz.setValue('');
						
					}
				});
			},
			select : function(){
				Ext.getCmp('couponEffect_btn_couponEffectStatistics').handler();
			}
		}
	});
	
	
	var branchSelect_combo_couponEffect = new Ext.form.ComboBox({
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
							data.push([jr.root[0]['id'], jr.root[0]['name']]);
						}else{
							data.push(['', '全部'], [jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
							 
							for(var i = 0; i < jr.root[0].branches.length; i++){
								data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
							}
						}
						
						thiz.store.loadData(data);
						if(jr.root[0].typeVal != '2'){
							thiz.setValue(jr.root[0].id);
						}else{
							thiz.setValue('');
						}
						
						thiz.fireEvent('select');
					}
				});
			}, 
			select : function(){
				var data = [['', '全部']];
				Ext.Ajax.request({
					url : '../../OperatePromotion.do',
					params : {
						dataSource : 'getByCond'
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i].coupon['id'], jr.root[i].coupon['name']]);
						}
						couponType_combo_couponEffect.store.loadData(data);
						couponType_combo_couponEffect.setValue('');
						
					}
				});
			}
		}
	});
	
	
	var couponEffectStatisticsToolbar = [{
		xtype : 'tbtext',
		width : 10
	},{
		xtype : 'tbtext',
		text : '优惠券类型选择:'
	}, couponType_combo_couponEffect, {
		xtype : 'tbtext',
		width : 10
	},{
		xtype : 'tbtext',
		text : '门店选择:'
	}, branchSelect_combo_couponEffect, '->', {
		text : '搜索',
		id : 'couponEffect_btn_couponEffectStatistics',
		iconCls : 'btn_search',
		handler : function(e){
			ds.baseParams['dataSource'] = 'calcByCond';
			ds.baseParams['beginDate'] = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00');
			ds.baseParams['endDate'] = Ext.util.Format.date(endDate.getValue(), 'y-m-d 23:59:59');
			ds.baseParams['branchId'] = branchSelect_combo_couponEffect.getValue();
			ds.baseParams['couponId'] = couponType_combo_couponEffect.getValue();
			ds.load({
				params : {
					start : 0,
					limit : 20
				}
			});
		}
	}, {
		text : '导出',
		iconCls : 'icon_tb_exoprt_excel',
		handler : function(){
		
		var url = '../../{0}?dataSource={1}&beginDate={2}&endDate={3}&couponTypeId={4}&branchId={5}';
		url = String.format(
			url,
			'ExportHistoryStatisticsToExecl.do',
			'couponEffectDetail',
			beginDate.getValue().format('Y-m-d 00:00:00'),
			endDate.getValue().format('Y-m-d 23:59:59'),
			couponType_combo_couponEffect.getValue(),
			branchSelect_combo_couponEffect.getValue()
		)
		window.location = url;
		
		}
		
	}]
	
	//头部
	var hours = null;
	var couponEffectTbar = Ext.ux.initTimeBar({
		beginDate : beginDate,
		endDate : endDate,
		dateCombo : dataCombo,
		tbarType : 2,
		statistic : 'couponEffect_',
		callback : function businessHourSelect(){
			hours  = null;
		}
	}).concat(couponEffectStatisticsToolbar);
	
	//couponEffectdGrid的栏目
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '优惠活动名称', dataIndex : 'couponName'},
		{header : '优惠券面额', dataIndex : 'couponPrice', renderer : Ext.ux.txtFormat.gridDou},
		{header : '共发送张数', dataIndex : 'issuedAmount'},
		{header : '优惠券成本', dataIndex : 'issuedPrice', renderer : Ext.ux.txtFormat.gridDou},
		{header : '共使用张数', dataIndex : 'usedAmount'},
		{header : '使用的优惠券总面额', dataIndex : 'usedPrice', renderer : Ext.ux.txtFormat.gridDou},
		{header : '拉动消费次数', dataIndex : 'salesAmount'},
		{header : '拉动消费额', dataIndex : 'effectSales', renderer : Ext.ux.txtFormat.gridDou},
		{header : '操作', dataIndex : 'operate', renderer : couponEffectOperate}
	]);
	
	//默认排序
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../OperateCouponEffect.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root', idProperty : ''}, [
			 {name : 'couponName'},
             {name : 'couponPrice'},
             {name : 'issuedAmount'},
             {name : 'issuedPrice'},
             {name : 'usedAmount'},
             {name : 'usedPrice'},
             {name : 'salesAmount'},
             {name : 'effectSales'},
             {name : 'coupon'}
		]),
		listeners : {
			load : function(store, records, options){
				if(store.getCount() > 0){
					var sumRow = couponEffect.getView().getRow(store.getCount() - 1);
					sumRow.style.backgroundColor = '#EEEEEE';
					for(var i = 0; i < couponEffect.getColumnModel().getColumnCount(); i++){
						var sumCell = couponEffect.getView().getCell(store.getCount() -1, i);
						sumCell.style.fontSize = '15px';
        				sumCell.style.fontWeight = 'bold';
        				sumCell.style.color= 'green';
					}
					couponEffect.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
        			couponEffect.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
        			couponEffect.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
				}
				$('#body_div_couponEffectStatistics').find('.couponEffect').each(function(index, element){
					element.onclick = function(){
						var businessSubStatisticsLoading = new Ext.LoadMask(document.body, {
							msg : '正在获取数据...'
						});
						Ext.ux.addTab('couponStatistics', '优惠券统计 ', 'History_Module/CouponStatistics.html', function(){
							businessSubStatisticsLoading.show();
							
							console.log(element);
							
							(function(){
					
								if(Ext.getCmp('branch_combo_coupon').getValue()){
									
									//设置门店选择的值
									Ext.getCmp('branch_combo_coupon').setValue(branchSelect_combo_couponEffect.getValue());
									
									//设置是跳转页面
									var isJump = true;
									Ext.getCmp('branch_combo_coupon').fireEvent('select', isJump);
									
									(function(){
										if(Ext.getCmp('coupon_comboBusinessHour').getValue()){
											Ext.getCmp('beginDate_combo_coupon').setValue(beginDate.getValue());
											Ext.getCmp('endDate_combo_coupon').setValue(endDate.getValue());
											
											Ext.getCmp('coupon_combo_couponStatistics').setValue(couponType_combo_couponEffect.getValue());
											//设置优惠券类型
											Ext.getCmp('coupon_btnSearch').handler();
											businessSubStatisticsLoading.hide();
											
										}else{
											setTimeout(arguments.callee, 500);
										}
									})();
								}else{
									setTimeout(arguments.callee, 500);
								}
							})();
						});
					}
				});
			}
		}
	});
	
	
	var paginBar = new Ext.PagingToolbar({
		pageSize : 20,
		store : ds,
		displayInfo : true,
		displayMsg : "显示第{0} 条到{1}条记录, 共{2}条",
		emptyMsg : "没有记录"
	});
	
	
	var couponEffect = new Ext.grid.GridPanel({
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
		tbar : couponEffectTbar,
		bbar : paginBar
		
	});
	
	//定义couponGrid的位置
	couponEffect.region = 'center';
	couponEffect.on('render', function(){
		dataCombo.setValue(1);
		dataCombo.fireEvent('select', dataCombo, null, 1);		
	});
	
	couponEffect.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('coupon_btnSearch').handler();
		}
	}];
	
	var couponEffectPanel = new Ext.Panel({
		title : '会员价明细',
		layout : 'border',
		region : 'center',
		frame : true,	
		items : [couponEffect]
	});
	
	new Ext.Panel({
		renderTo : 'body_div_couponEffectStatistics',
		width : parseInt(Ext.getDom('body_div_couponEffectStatistics').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('body_div_couponEffectStatistics').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
		frame : true,
		items : [couponEffectPanel]
	});
	
	Ext.getCmp('couponEffect_btn_couponEffectStatistics').handler();	
	
	
	function couponEffectOperate(a, b, c){
		return '<a class="couponEffect">查看详细</a>'
	}
	
});