Ext.onReady(function(){
	var beginDate = new Ext.form.DateField({
		id: 'beginDate_combo_summary',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var endDate = new Ext.form.DateField({
		id : 'endDate_combo_summary',
		xtype : 'datafield',
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
			//TODO
		}
	});
	
	var memberType_combo_summary = new Ext.form.ComboBox({
		id : 'memberType_combo_summary',
		width : 90,
		forceSelection : true,
		readOnly : false,
		store : new Ext.data.JsonStore({
			root : 'root',
			fields : ['id', 'name']
		}),
		valueField : 'id',
		displayField : 'name',
		listClass : ' x-menu ',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				Ext.Ajax.request({
					url : '../../QueryMemberType.do?',
					params : {
						isCookie : true,
						dataSource : 'normal',
						restaurantID : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							jr.root.unshift({id:-1, name:'全部'});
						}else{
							Ext.example.msg('异常', '会员类型数据加载失败, 请联系客服人员.');
						}
						thiz.store.loadData(jr);
						thiz.setValue(-1);
						
					},
					failure : function(res, opt){
						thiz.store.loadData({root:[{typeId:-1, name:'全部'}]});
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
				//TODO
			}
		}
	});
	
	//门店选择
	var branch_combo_summary = new Ext.form.ComboBox({
		id : 'branch_combo_summary',
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
			select : function(isJump){
				//加载市别
				var hour = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../OperateBusinessHour.do',
					params : {
						dataSource : 'getByCond',
						branchId : branch_combo_coupon.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						for(var i = 0; i < jr.root.length; i++){
							hour.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
						}
						
						hour.push([-2, '自定义']);
						
						Ext.getCmp('coupon_comboBusinessHour').setDisabled(false);
						Ext.getCmp('coupon_comboBusinessHour').store.loadData(hour);
						Ext.getCmp('coupon_comboBusinessHour').setValue(-1);
					}
				});
				
				//加载会员类型
				Ext.Ajax.request({
					url : '../../QueryMemberType.do?',
					params : {
						isCookie : true,
						dataSource : 'normal',
						restaurantID : restaurantID
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							jr.root.unshift({id:-1, name:'全部'});
						}else{
							Ext.example.msg('异常', '会员类型数据加载失败, 请联系客服人员.');
						}
						thiz.store.loadData(jr);
						thiz.setValue(-1);
						
					},
					failure : function(res, opt){
						thiz.store.loadData({root:[{typeId:-1, name:'全部'}]});
						thiz.setValue(-1);
					}
				});
			}
		}
	});	
	
	var summaryTbar = [{
		xtype : 'tbtext',
		width : 10
	}, {
		xtype :'tbtext',
		text : '会员手机/会员卡号/会员名'
	}, {
		xtype : 'textfield',
		id : 'memberName_textfield_summary',
		width : 150
	}, '->', {
		text : '搜索',
		id : 'summary_search',
		iconCls : 'btn_search',
		handler : function(){
		
		}
	}, {
		text : '导出',
		iconCls : 'icon_tb_exoprt_excel',
		handler : function(){
		
		}
	}];
	
	//头部
	var statisticsTbar = Ext.ux.initTimeBar({
		beginDate : beginDate, 
		endDate : endDate,
		dateCombo : dataCombo, 
		tbarType : 1, 
		statistic : 'summary_', 
		callback : function businessHourSelect(){
			}
	}).concat(summaryTbar);
	
	//summaryGrid的栏目
	var cm = new Ext.grid.ColumnModel([
	 	new Ext.grid.RowNumberer(),
	 	{header : '会员姓名', dataIndex : 'member.name'},
	 	{header : '会员手机', dataIndex : 'member.mobile'},
	 	{header : '会员卡号', dataIndex : 'member.card'},
	 	{header : '充值实收', dataIndex : 'chargeMoney'},
	 	{header : '充值实充', dataIndex : 'chargeActual'},
	 	{header : '取款实退', dataIndex : 'refundMoney'},
	 	{header : '取款实扣', dataIndex : 'refundActual'},
	 	{header : '消费基础扣额', dataIndex : 'consumeBase'},
	 	{header : '消费赠送扣额', dataIndex : 'consumeExtra'},
	]);
	
	
	
	
	
	
});