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
			Ext.getCmp('summary_search').handler();
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
				Ext.getCmp('summary_search').handler();
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
							
							branch_combo_summary.store.loadData(data);
							branch_combo_summary.setValue(jr.root[0].id);
							branch_combo_summary.fireEvent('select');
						}else{
							var data = [[-1, '全部']];
							data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
							
							for(var i = 0; i < jr.root[0].branches.length; i++){
								data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
							}
							
							branch_combo_summary.store.loadData(data);
							branch_combo_summary.setValue(-1);
							branch_combo_summary.fireEvent('select');
						}
						
						
					}
				});
			},
			select : function(isJump){
				//加载会员类型
				Ext.Ajax.request({
					url : '../../QueryMemberType.do?',
					params : {
						isCookie : true,
						dataSource : 'normal',
						branchId : branch_combo_summary.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							jr.root.unshift({id:-1, name:'全部'});
						}else{
							Ext.example.msg('异常', '会员类型数据加载失败, 请联系客服人员.');
						}
						memberType_combo_summary.store.loadData(jr);
						memberType_combo_summary.setValue(-1);
						
					},
					failure : function(res, opt){
						memberType_combo_summary.store.loadData({root:[{typeId:-1, name:'全部'}]});
						memberType_combo_summary.setValue(-1);
					}
				});
				
				Ext.getCmp('summary_search').handler();
			}
		}
	});	
	
	var summaryTbar = [{
		xtype : 'tbtext',
		width : 10
	}, {
		xtype : 'tbtext',
		text : '会员类型'
	}, memberType_combo_summary,{
		xtype : 'tbtext',
		width : 10
	}, {
		xtype : 'tbtext',
		text : '门店选择'
	}, branch_combo_summary, {
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
			if(!beginDate.isValid() || !endDate.isValid){
				return;
			}
			var onDuty = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00');
			var offDuty = Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59');
			
			var memberName = Ext.getCmp('memberName_textfield_summary');
			var memberType = Ext.getCmp('memberType_combo_summary');
			
			var branchId = Ext.getCmp('branch_combo_summary');
			
			var store = summaryGrid.getStore();
			store.baseParams['dataSource'] = 'getMemberSummary';
			store.baseParams['fuzzy'] = memberName.getValue();
			store.baseParams['memberTypeId'] = memberType.getValue();
			store.baseParams['branchId'] = branchId.getValue();
			store.baseParams['onDuty'] = onDuty;
			store.baseParams['offDuty'] = offDuty;
			store.load({
				params : {
					start : 0,
					limit : 20
				}
			})
		
		}
	}, {
		text : '导出',
		iconCls : 'icon_tb_exoprt_excel',
		handler : function(){
			var url = '../../{0}?dataSource={1}&onDuty={2}&offDuty={3}&branchId={4}&memberTypeId={5}&fuzzy={6}';
			url = String.format(
				url,
				'ExportHistoryStatisticsToExecl.do',
				'memberSummary',
				Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00'),
				Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59'),
				Ext.getCmp('branch_combo_summary').getValue(),
				Ext.getCmp('memberType_combo_summary').getValue(),
				Ext.getCmp('memberName_textfield_summary').getValue()
			)
			window.location = url;
			
		}
	}];
	
	//头部
	var statisticsTbar = Ext.ux.initTimeBar({
		beginDate : beginDate, 
		endDate : endDate,
		dateCombo : dataCombo, 
		tbarType : 2, 
		statistic : 'summary_', 
		callback : function businessHourSelect(){
			}
	}).concat(summaryTbar);
	
	//summaryGrid的栏目
	var cm = new Ext.grid.ColumnModel([
	 	new Ext.grid.RowNumberer(),
	 	{header : '会员姓名', dataIndex : 'member.name', renderer : memberFuzzy},
	 	{header : '会员手机', dataIndex : 'member.mobile', renderer : memberFuzzy},
	 	{header : '会员卡号', dataIndex : 'member.memberCard', renderer : memberFuzzy},
	 	{header : '充值实收', dataIndex : 'chargeActual', renderer : memberCharge},
	 	{header : '充值实充', dataIndex : 'chargeMoney', renderer : memberCharge},
	 	{header : '取款实退', dataIndex : 'refundActual', renderer : memberRefund},
	 	{header : '取款实扣', dataIndex : 'refundMoney', renderer : memberRefund},
	 	{header : '消费基础扣额', dataIndex : 'consumeBase', renderer : memberConsume},
	 	{header : '消费赠送扣额', dataIndex : 'consumeExtra', renderer : memberConsume},
	 	{header : '消费额', dataIndex : 'consumeTotal', renderer : memberConsume},
	 	{header : '剩余金额', dataIndex : 'remainingBalance'},
	 	{header : '积分变动', dataIndex : 'changedPoint', renderer : changePoint},
	 	{header : '剩余积分', dataIndex : 'remainingPoint', renderer : changePoint}
	]);
	
	//默认排序
	cm.defaultSortable = true;
	
	//summaryGrid的数据源
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../OperateMemberOperation.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'}, [
			{name : 'member.name'},
			{name : 'member.mobile'},
			{name : 'member.memberCard'},
			{name : 'chargeMoney'},
			{name : 'chargeActual'},
			{name : 'refundMoney'},
			{name : 'refundActual'},
			{name : 'consumeBase'},
			{name : 'consumeExtra'},
			{name : 'consumeTotal'},
			{name : 'remainingBalance'},
			{name : 'changedPoint'},
			{name : 'remainingPoint'}
		])
	});
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : 20,
		store : ds,
		displayInfo : true,
		displayMsg : "显示第{0} 条到 {1} 条记录, 共 {2}条",
		emptyMsg : " 没有记录"
	});
	
	var summaryGrid = new Ext.grid.GridPanel({
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
		tbar : statisticsTbar,
		bbar : pagingBar
	});
	
	summaryGrid.region = 'center';
	
	summaryGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			//TODO
		}
	}];
	
	summaryGrid.getStore().on('load', function(store, records, options){
		if(records.length > 0){
			//会员姓名、会员手机、会员卡号点击事件
			$('#divMemberSummaryStatistics').find('.memberFuzzy').each(function(index, element){
				element.onclick = function(){
					linkMemberStatistics('memberFuzzy', $(element).attr('value'));
				}
			});	
			
			//充值
			$('#divMemberSummaryStatistics').find('.memberCharge').each(function(index, element){
				element.onclick = function(){
					linkMemberStatistics('memberCharge', $(element).attr('value'));
				}
			});
			
			//取款
			$('#divMemberSummaryStatistics').find('.memberRefund').each(function(index, element){
				element.onclick = function(){
					linkMemberStatistics('memberRefund', $(element).attr('value'));
				}
			});
			
			//消费
			$('#divMemberSummaryStatistics').find('.memberConsume').each(function(index, element){
				element.onclick = function(){
					linkMemberStatistics('memberConsume', $(element).attr('value'));
				}
			});
			
			//积分
			$('#divMemberSummaryStatistics').find('.changePoint').each(function(index, element){
				element.onclick = function(){
					linkMemberStatistics('changePoint', $(element).attr('value'));
				}
			});
			
		}
	})
	
	var memberSummaryPanel = new Ext.Panel({
		title : '会员汇总',
		layout : 'border',
		region : 'center',
		frame : true,
		items : [summaryGrid]
	});
	
	
	new Ext.Panel({
		renderTo : 'divMemberSummaryStatistics',
		id : 'couponStatisticsPanel',
		width : parseInt(Ext.getDom('divMemberSummaryStatistics').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('divMemberSummaryStatistics').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
		frame : true,
		items : [memberSummaryPanel]
	});
	
	dataCombo.setValue(1);
	dataCombo.fireEvent('select', dataCombo, null, 1);
	
	//会员姓名、会员手机、会员卡号
	function memberFuzzy(value, object, store){
		if(value == ''){
			return '无'
		}else{
			return '<a class="memberFuzzy" value="'+ value +'">'+ value +'</a>';
		}
	}
	
	//充值
	function memberCharge(value, object, store){
		return '<a class="memberCharge" value="'+ store.json.member.name +'">'+ value + '</a>';	
	}
	
	//取款
	function memberRefund(value, object, store){
		return '<a class="memberRefund" value="'+ store.json.member.name +'">'+ value + '</a>';	
	}
	
	//消费
	function memberConsume(value, object, store){
		return '<a class="memberConsume" value="'+ store.json.member.name +'">'+ value + '</a>';	
	}
	
	//积分
	function changePoint(value, object, store){
		return '<a class="changePoint" value="'+ store.json.member.name +'">'+ value + '</a>';	
	}
	
	//跳转
	function linkMemberStatistics(type, memberFuzzy){
		var beginDate = Ext.getCmp('beginDate_combo_summary').getValue();
		var endDate = Ext.getCmp('endDate_combo_summary').getValue();
		
		var memberType = Ext.getCmp('memberType_combo_summary').getValue();
		var branchId = Ext.getCmp('branch_combo_summary').getValue();
		
		var memberSummaryLoading = new Ext.LoadMask(document.body, {
			msg : '正在获取数据...'
		});
		
		if(type == 'memberFuzzy'){////会员姓名、会员手机、会员卡号
			Ext.ux.addTab('memberMgrMain', '会员管理', 'Client_Module/MemberManagement.html', function(){
				memberSummaryLoading.show();
				(function(){
					if(!Ext.getCmp('member_btnHeightSearch').hidden){
						Ext.getCmp('member_btnHeightSearch').handler();
					}
					
					if(branchId != -1){
						Ext.getCmp('branch_combo_memberMgrMain').setValue(branchId);	
					}else{
						Ext.getCmp('branch_combo_memberMgrMain').setValue(null);	
					}
					
					Ext.getCmp('numberSearchByMemberPhoneOrCardOrName').setValue(memberFuzzy);
					
					setTimeout(function(){
						Ext.getCmp('searchMember_btn_member').handler();
					}, 200);
					
					memberSummaryLoading.hide();
				})();
			});
			
		}else if(type == 'memberCharge'){//充值
			Ext.ux.addTab('memberChargeStatistics', '充值统计', 'Client_Module/memberChargeStatistics.html', function(){
				memberSummaryLoading.show();
				(function(){
					
					Ext.getCmp('beginDate_combo_memberCharge').setValue(beginDate);
					Ext.getCmp('endDate_combo_memberCharge').setValue(endDate);	

					Ext.getCmp('mcs_search_memberType').setValue(memberType);
					
					Ext.getCmp('mcs_search_memberName').setValue(memberFuzzy);
					
					Ext.getCmp('branch_combo_memberCharge').setValue(branchId);	
					
					Ext.getCmp('memberChargeSearchBtn').handler();
					memberSummaryLoading.hide();
				})();
			});
			
		}else if(type == "memberRefund"){//取款
			Ext.ux.addTab('memberRefundStatistics', '充值统计', 'Client_Module/memberRefundStatistics.html', function(){
				memberSummaryLoading.show();
				(function(){
					Ext.getCmp('beginDate_combo_memberRefund').setValue(beginDate);
					Ext.getCmp('endDate_combo_memberRefund').setValue(endDate);	
					
					Ext.getCmp('mrs_search_memberType').setValue(memberType);
					
					Ext.getCmp('mrs_search_memberName').setValue(memberFuzzy);
					
					Ext.getCmp('branch_combo_memberRefund').setValue(branchId);	
					
					Ext.getCmp('memberRefundSearchBtn').handler();
					memberSummaryLoading.hide();
					
				})();
			});
		}else if(type == "memberConsume"){//消费
			Ext.ux.addTab('memberConsumeStatistics', '消费统计', 'Client_Module/memberConsumeStatistics.html', function(){
				memberSummaryLoading.show();
				(function(){
					Ext.getCmp('mcus_search_onDuty').setValue(beginDate);
					Ext.getCmp('mcus_search_offDuty').setValue(endDate);			
				
					Ext.getCmp('mcus_search_memberType').setValue(memberType);
					
					Ext.getCmp('mcus_search_memberName').setValue(memberFuzzy);
					
					Ext.getCmp('branch_combo_memberConsume').setValue(branchId);	
					
					Ext.getCmp('memberConsume_btn_search').handler();
					memberSummaryLoading.hide();
					
				})();
			});
		}else if(type = "changePoint"){//积分
			Ext.ux.addTab('pointConsumeStatistics', '积分统计', 'Client_Module/pointConsumeStatistics.html', function(){
				memberSummaryLoading.show();
				(function(){
					Ext.getCmp('beginDate_combo_pointConsumeStatistics').setValue(beginDate);
					Ext.getCmp('endDate_combo_pointConsumeStatistics').setValue(endDate);	
									
					Ext.getCmp('pointMember_textfield_point').setValue(memberFuzzy);
					
					Ext.getCmp('branch_combo_point').setValue(branchId);	
					
					Ext.getCmp('search_btn_point').handler();
					memberSummaryLoading.hide();
					
				})();
			});
		}
		
		
		
		
	}
	
});