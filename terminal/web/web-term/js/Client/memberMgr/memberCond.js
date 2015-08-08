
var memberCondBasicGrid, memberBasicWin, adjustPointWin, memberCouponWin,m_memberTypeWin, m_searchAdditionFilter = 'create',
	memberCondWin,
	memberCond_obj = {treeId : 'tree_memberCond', option : [{name:'修改', fn:"updateMemberCond()"},{name:'删除', fn:"deleteMemberCond()"}]};

/**
 * 修改memberCond
 */
function updateMemberCond(){
	var sn = Ext.ux.getSelNode(memberCondTree);

	Ext.Ajax.request({
		url : '../../OperateMemberCond.do',
		params : {
			dataSource : "queryById",
			id : sn.id
		},
		success : function(response, options) {
			var jr = Ext.util.JSON.decode(response.responseText);
			if(jr.success){
				memberCondWin.operationType = 'update';
				operateMemberCondData(jr.root[0]);
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(response, options) {
			
		}		
	});
}

/**
 * 删除条件
 */
function deleteMemberCond(){
	var sn = Ext.ux.getSelNode(memberCondTree);
	if(sn != null){
		Ext.Msg.confirm(
			'提示',
			'是否刪除' + sn.text + '?',
			function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperateMemberCond.do',
						params : {
							dataSource : "deleteById",
							id : sn.id
						},
						success : function(response, options) {
							var jr = Ext.util.JSON.decode(response.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								memberCondTree.getRootNode().reload();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(response, options) {
							
						}		
					});
				}
			}
		);

	}else{
		Ext.ux.showMsg({title:"提示", msg:"请选择一个条件"});
	}	
}

/**
 * 根据条件分析会员
 */
function queryMembersByCond(c){
	var gs = memberCondBasicGrid.getStore();
	gs.baseParams['memberType'] = Ext.getCmp('memberSearchByType').getValue();
	gs.baseParams['memberCondMinConsume'] = Ext.getCmp('textTotalMinMemberCost').getValue();
	gs.baseParams['memberCondMaxConsume'] = Ext.getCmp('textTotalMaxMemberCost').getValue();
	gs.baseParams['memberCondMinAmount'] = Ext.getCmp('textTotalMinMemberCostCount').getValue();
	gs.baseParams['memberCondMaxAmount'] = Ext.getCmp('textTotalMaxMemberCostCount').getValue();
	gs.baseParams['memberCondMinBalance'] = Ext.getCmp('textMinMemberBalance').getValue();
	gs.baseParams['memberCondMaxBalance'] = Ext.getCmp('textMaxMemberBalance').getValue();
	gs.baseParams['memberCondBeginDate'] = Ext.util.Format.date(Ext.getCmp('dateSearchDateBegin').getValue(), 'Y-m-d 00:00:00');
	gs.baseParams['memberCondEndDate'] = Ext.util.Format.date(Ext.getCmp('dateSearchDateEnd').getValue(), 'Y-m-d 23:59:59');
	
	if(c && c.searchByCond){
		gs.baseParams['memberCondId'] = c.searchByCond;
	}else{
		gs.baseParams['memberCondId'] = '';
	}
	
	gs.load({
		params : {
			start : 0,
			limit : 30
		}
	});	
}

/**
 * 初始化筛选
 */
function memberCondTreeInit(){
	var memberTypeTreeTbar = new Ext.Toolbar({
		items : ['->',{
			text : '添加',
			iconCls : 'btn_add',
			handler : function(){
				memberCondWin.operationType = 'insert';
				operateMemberCondData();
			}			
			
		},{
			text : '刷新',
			id : 'btnRefreshMemberType',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getDom('memberCondName').innerHTML = '----';
				memberCondTree.getRootNode().reload();
			}
		}]
	});
	
	memberCondTree = new Ext.tree.TreePanel({
		id : 'tree_memberCond',
		title : '筛选条件',
//		region : 'west',
		region : 'center',
		width : 240,
		height : 220,
		border : true,
		rootVisible : true,
		singleExpand : true,
		autoScroll : true,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../OperateMemberCond.do',
			baseParams : {
				dataSource : 'memberCondTree'
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			text : '全部条件',
			leaf : false,
			border : true,
			expanded : true,
			id : -1,
			listeners : {
				load : function(thiz){
/*					memberTypeData.root = [];
					for(var i = 0; i < thiz.childNodes.length; i++){
						memberTypeData.root.push({
							id : thiz.childNodes[i].attributes['memberTypeId'],
							name : thiz.childNodes[i].attributes['memberTypeName'],
							attributeValue : thiz.childNodes[i].attributes['attributeValue'],
							chargeRate : thiz.childNodes[i].attributes['chargeRate']
						});
					}*/
				}
			}
		}),
		tbar : memberTypeTreeTbar,
		listeners : {
	    	click : function(e){
	    		if(e.id == -1){
	    			return;
	    		}
	    		
	    		queryMembersByCond({searchByCond:e.id});
	    		Ext.getDom('memberCondName').innerHTML = e.text;
	    		
	    		Ext.Ajax.request({
	    			url : '../../OperateMemberCond.do',
	    			params : {
	    				dataSource : "queryById",
	    				id : e.id
	    			},
	    			success : function(response, options) {
	    				var jr = Ext.util.JSON.decode(response.responseText);
	    				if(jr.success){
	    		    		Ext.getCmp('memberSearchByType').setValue(jr.root[0].memberType);
	    		    		Ext.getCmp('textTotalMinMemberCost').setValue(jr.root[0].minConsumeMoney > 0?jr.root[0].minConsumeMoney:'');
	    		    		Ext.getCmp('textTotalMaxMemberCost').setValue(jr.root[0].maxConsumeMoney > 0?jr.root[0].maxConsumeMoney:'');
	    		    		Ext.getCmp('textTotalMinMemberCostCount').setValue(jr.root[0].minConsumeAmount > 0?jr.root[0].minConsumeAmount:'');
	    		    		Ext.getCmp('textTotalMaxMemberCostCount').setValue(jr.root[0].maxConsumeAmount > 0?jr.root[0].maxConsumeAmount:'');
	    		    		Ext.getCmp('textMinMemberBalance').setValue(jr.root[0].minBalance > 0?jr.root[0].minBalance:'');
	    		    		Ext.getCmp('textMaxMemberBalance').setValue(jr.root[0].maxBalance > 0?jr.root[0].maxBalance:'');
	    		    		Ext.getCmp('dateSearchDateBegin').setValue(jr.root[0].beginDate);
	    		    		Ext.getCmp('dateSearchDateEnd').setValue(jr.root[0].endDate);
	    					
    		    			if(jr.root[0].minConsumeMoney > 0 && jr.root[0].maxConsumeMoney == 0){
    		    				//大于
    		    				Ext.getCmp('cboConsumeMoneyBar').setValue(1);
    		    				Ext.getCmp('cboConsumeMoneyBar').fireEvent('select', null, null, 0);
    		    			}else if(jr.root[0].minConsumeMoney == 0 && jr.root[0].maxConsumeMoney > 0){
    		    				//小于
    		    				Ext.getCmp('cboConsumeMoneyBar').setValue(2);
    		    				Ext.getCmp('cboConsumeMoneyBar').fireEvent('select', null, null, 1);
    		    			}else if(jr.root[0].minConsumeMoney > 0 && jr.root[0].maxConsumeMoney > 0){
    		    				//介于
    		    				Ext.getCmp('cboConsumeMoneyBar').setValue(3);
    		    				Ext.getCmp('cboConsumeMoneyBar').fireEvent('select', null, null, 2);
    		    			}
	    				}else{
	    					Ext.ux.showMsg(jr);
	    				}
	    			},
	    			failure : function(response, options) {
	    				
	    			}		
	    		});

	    		
	    	}
	    }
	});
};

function memberCondGridInit(){
	var member_beginDate = new Ext.form.DateField({
		xtype : 'datefield',	
		id : 'dateSearchDateBegin',
		format : 'Y-m-d',
		width : 100,
		readOnly : false,
	});
	var member_endDate = new Ext.form.DateField({
		xtype : 'datefield',
		id : 'dateSearchDateEnd',
		format : 'Y-m-d',
		width : 100,
		readOnly : false,
	});
	var member_dateCombo = Ext.ux.createDateCombo({
		width : 75,
		data : [[3, '近一个月'], [12, '近二个月'], [4, '近三个月'], [9, '近半年']],
		beginDate : member_beginDate,
		endDate : member_endDate,
		callback : function(){
			if(member_searchType){
				Ext.getCmp('btnSearchMember').handler();
			}
		}
	});
	
	var memberCondBasicGridExcavateMemberTbar = new Ext.Toolbar({
		height : 28,		
		items : [{
				xtype : 'tbtext',
				text : String.format(Ext.ux.txtFormat.longerTypeName, '条件名称', 'memberCondName', '----')
			},
			{xtype : 'tbtext', text : '日期:&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			member_dateCombo,
			{xtype : 'tbtext', text : '&nbsp;'},
			member_beginDate,
			{
				xtype : 'label',
				hidden : false,
				id : 'tbtextDisplanZ',
				text : ' 至 '
			}, 
			member_endDate,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
			{xtype : 'tbtext', text : '会员类型:'},
			new Ext.form.ComboBox({
				id : 'memberSearchByType',
				width : 100,
				readOnly : false,
				forceSelection : true,
				value : '=',
				store : new Ext.data.JsonStore({
					root : 'root',
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
						Ext.Ajax.request({
							url : '../../QueryMemberType.do?',
							params : {
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
			
			}), '->',{
				text : '搜索',
				id : 'btnSearchMember',
				iconCls : 'btn_search',
				handler : function(){
					queryMembersByCond();
					
				}
			}, '-', {
				text : '导出',
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(e){
					var url = '../../{0}?memberType={1}&MinTotalMemberCost={2}&MaxTotalMemberCost={3}&consumptionMinAmount={4}&consumptionMaxAmount={5}'+
							'&memberMinBalance={6}&memberMaxBalance={7}&dateBegin={8}&dateEnd={9}&dataSource={10}';
					url = String.format(
						url, 
						'ExportHistoryStatisticsToExecl.do', 
						Ext.getCmp('memberSearchByType').getValue(),
						Ext.getCmp('textTotalMinMemberCost').getValue(),
						Ext.getCmp('textTotalMaxMemberCost').getValue(),
						Ext.getCmp('textTotalMinMemberCostCount').getValue(),
						Ext.getCmp('textTotalMaxMemberCostCount').getValue(),
						Ext.getCmp('textMinMemberBalance').getValue(),
						Ext.getCmp('textMaxMemberBalance').getValue(),
						Ext.util.Format.date(Ext.getCmp('dateSearchDateBegin').getValue(), 'Y-m-d 00:00:00'),
						Ext.util.Format.date(Ext.getCmp('dateSearchDateEnd').getValue(), 'Y-m-d 23:59:59'),
						'memberList'
					);
					
					window.location = url;
				}
			}]
		
	});
	
	var memberCondBasicGridSortTbar = new Ext.Toolbar({
		height : 28,		
		items : [{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
			{xtype : 'tbtext', text : '消费次数:'},
			{
				xtype : 'numberfield',
				id : 'textTotalMinMemberCostCount',
				width : 50
			},
			{
				xtype : 'tbtext',
				text : '&nbsp;-&nbsp;'
			},			
			{
				xtype : 'numberfield',
				id : 'textTotalMaxMemberCostCount',
				width : 50
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
			{xtype : 'tbtext', text : '余额:'},
			{
				xtype : 'numberfield',
				id : 'textMinMemberBalance',
				width : 50
			},
			{
				xtype : 'tbtext',
				text : '&nbsp;-&nbsp;'
			},			
			{
				xtype : 'numberfield',
				id : 'textMaxMemberBalance',
				width : 50
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},			
			{xtype : 'tbtext', text : '消费金额:'}, {
				id : 'cboConsumeMoneyBar',
				xtype : 'combo',
				readOnly : false,
				forceSelection : true,
				value : 1,
				width : 80,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : [[1, '大于'], [2, '小于'], [3, '介于']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				listeners : {
					select : function(combo, record, index){
//						Ext.getCmp('textTotalMinMemberCost').setValue();
//						Ext.getCmp('textTotalMaxMemberCost').setValue();
						if(index == 0){
							Ext.getCmp('betweenConsume').hide();
							Ext.getCmp('textTotalMinMemberCost').show();
							Ext.getCmp('textTotalMaxMemberCost').hide();
						}else if(index == 1){
							Ext.getCmp('betweenConsume').hide();
							Ext.getCmp('textTotalMaxMemberCost').show();
							Ext.getCmp('textTotalMinMemberCost').hide();
						}else if(index == 2){
							Ext.getCmp('betweenConsume').show();
							Ext.getCmp('textTotalMinMemberCost').show();
							Ext.getCmp('textTotalMaxMemberCost').show();
							$('#betweenConsume').show();
							$('#textTotalMinMemberCost').show();
							$('#textTotalMaxMemberCost').show();
						}
					}
				}
			},
			{
				xtype : 'numberfield',
				id : 'textTotalMinMemberCost',
				width : 60
			},
			{
				xtype : 'tbtext',
				id : 'betweenConsume',
				text : '&nbsp;-&nbsp;',
				hidden : true
			},			
			{
				xtype : 'numberfield',
				id : 'textTotalMaxMemberCost',
				width : 60,
				hidden : true
			}]
		
	});	
	
	memberCondBasicGrid = createGridPanel(
		'memberCondBasicGrid',
		'会员信息',
		'',
		'',
		'../../QueryMember.do',
		[
			[true, false, false, true],
			['名称', 'name'],
			['类型', 'memberType.name'],
			['创建时间','createDateFormat'],
			['消费次数', 'consumptionAmount',,'right', 'Ext.ux.txtFormat.gridDou'],
			['消费总额', 'totalConsumption',,'right', 'Ext.ux.txtFormat.gridDou'],
			['累计积分', 'totalPoint',,'right', 'Ext.ux.txtFormat.gridDou'],
			['当前积分', 'point',,'right', 'Ext.ux.txtFormat.gridDou'],
			['总充值额', 'totalCharge',,'right'],
			['账户余额', 'totalBalance',,'right'],
			['手机号码', 'mobile', 125],
			['会员卡号', 'memberCard', 125]
		],
		MemberBasicRecord.getKeys(),
		[['isPaging', true],['dataSource', 'byMemberCond']],
		100,
		'',
		[memberCondBasicGridExcavateMemberTbar, memberCondBasicGridSortTbar]
	);	
	memberCondBasicGrid.region = 'center';
	memberCondBasicGrid.loadMask = { msg : '数据加载中，请稍等...' };
	
	memberCondBasicGrid.on('rowdblclick', function(e){
		updateMemberHandler();
	});
	
	memberCondBasicGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){ 
			Ext.getCmp('btnSearchMember').handler();
		}
	}];
};

/**
 * 操作会员分析数据
 * @param data
 */
function operateMemberCondData(data){
	data = data || {};
	Ext.getCmp('memberCondId').setValue(data.id);
	Ext.getCmp('txtMemberCondName').setValue(data.name);
	Ext.getCmp('memberCondByType').setValue(data.memberType);
	Ext.getCmp('memberCondMinConsume').setValue(data.minConsumeMoney && data.minConsumeMoney>0?data.minConsumeMoney:"");
	Ext.getCmp('memberCondMaxConsume').setValue(data.maxConsumeMoney && data.maxConsumeMoney>0?data.maxConsumeMoney:"");
	Ext.getCmp('memberCondMinAmount').setValue(data.minConsumeAmount && data.minConsumeAmount>0?data.minConsumeAmount:"");
	Ext.getCmp('memberCondMaxAmount').setValue(data.maxConsumeAmount && data.maxConsumeAmount>0?data.maxConsumeAmount:"");
	Ext.getCmp('memberCondMinBalance').setValue(data.minBalance && data.minBalance>0?data.minBalance:"");
	Ext.getCmp('memberCondMaxBalance').setValue(data.maxBalance && data.maxBalance>0?data.maxBalance:"");
	Ext.getCmp('memberCondDateRegion').setValue(data.rangeType);
	
	if(data.name){
		if(data.minConsumeMoney > 0 && data.maxConsumeMoney == 0){
			//大于
			Ext.getCmp('cboConsumeMoney').setValue(1);
			Ext.getCmp('cboConsumeMoney').fireEvent('select', null, null, 0);
		}else if(data.minConsumeMoney == 0 && data.maxConsumeMoney > 0){
			//小于
			Ext.getCmp('cboConsumeMoney').setValue(2);
			Ext.getCmp('cboConsumeMoney').fireEvent('select', null, null, 1);
		}else if(data.minConsumeMoney > 0 && data.maxConsumeMoney > 0){
			//介于
			Ext.getCmp('cboConsumeMoney').setValue(3);
			Ext.getCmp('cboConsumeMoney').fireEvent('select', null, null, 2);
		}
	}else{
		Ext.getCmp('cboConsumeMoney').setValue(1);
		Ext.getCmp('cboConsumeMoney').fireEvent('select', null, null, 0);
	}
	
	if(data.rangeType && data.rangeType == 4){
		Ext.getCmp('memberCondBeginDate').setValue(data.beginDate);
		Ext.getCmp('memberCondEndDate').setValue(data.endDate);	
		
		Ext.getCmp('memberCondBeginDate').enable();
		Ext.getCmp('memberCondEndDate').enable();	
	}else{
		Ext.getCmp('memberCondBeginDate').setValue();
		Ext.getCmp('memberCondEndDate').setValue();		
		
		Ext.getCmp('memberCondBeginDate').disable();
		Ext.getCmp('memberCondEndDate').disable();	
	}
	
	memberCondWin.show();
	Ext.getCmp('txtMemberCondName').focus(true, 100);
	
}

if(!memberCondWin){
	
	var mc_search_memberType = new Ext.form.ComboBox({
		columnWidth : 0.3,
		id : 'memberCondByType',
		xtype : 'combo',
		readOnly : false,
		forceSelection : true,
		value : '=',
		store : new Ext.data.JsonStore({
			root : 'root',
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
				Ext.Ajax.request({
					url : '../../QueryMemberType.do?',
					params : {
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
	
	memberCondWin = new Ext.Window({
		title : '添加分析条件',
		width : 400,
		closeAction : 'hide',
		xtype : 'panel',
		layout : 'column',
		frame : true,
		modal : true,
		items :[{
			xtype : 'hidden',
			id : 'memberCondId'
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '筛选名称:'
		} ,{
			columnWidth : 0.3,
			xtype : 'textfield',
			id : 'txtMemberCondName',
			allowBlank : false,
			blankText : '名称不允许为空'
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '会员类型:'
		} ,mc_search_memberType,{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '消费金额:',
		}, {
			columnWidth : 0.2,
			id : 'cboConsumeMoney',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : 1,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[1, '大于'], [2, '小于'], [3, '介于']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(combo, record, index){
					Ext.getCmp('memberCondMinConsume').setValue();
					Ext.getCmp('memberCondMaxConsume').setValue();
					if(index == 0){
						Ext.getCmp('betweenMemberCondConsume').hide();
						Ext.getCmp('memberCondMinConsume').show();
						Ext.getCmp('memberCondMaxConsume').hide();
					}else if(index == 1){
						Ext.getCmp('betweenMemberCondConsume').hide();
						Ext.getCmp('memberCondMaxConsume').show();
						Ext.getCmp('memberCondMinConsume').hide();
					}else if(index == 2){
						Ext.getCmp('betweenMemberCondConsume').show();
						Ext.getCmp('memberCondMinConsume').show();
						Ext.getCmp('memberCondMaxConsume').show();
						$('#betweenMemberCondConsume').show();
						$('#memberCondMinConsume').show();
						$('#memberCondMaxConsume').show();
					}
				}
			}
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'memberCondMinConsume',
		},{
			xtype : 'label',
			id : 'betweenMemberCondConsume',
			text : ' ~ ',
			hidden : true
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'memberCondMaxConsume',
			hidden : true
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '消费次数:'
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'memberCondMinAmount',
		},{
			xtype : 'label',
			text : ' ~ ',
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'memberCondMaxAmount',
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '余额:'
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'memberCondMinBalance',
		},{
			xtype : 'label',
			text : ' ~ ',
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'memberCondMaxBalance',
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false		
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '日期:'
		}, {
			columnWidth : 0.2,
			id : 'memberCondDateRegion',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : 1,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[1, '近一个月'], [2, '近二个月'], [3, '近三个月'], [4, '自定义']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(combo, record, index){
					//切换到自定义时开启日期应用
					if(record.data.value == 4){
						Ext.getCmp('memberCondBeginDate').enable();
						Ext.getCmp('memberCondEndDate').enable();	
					}else{
						Ext.getCmp('memberCondBeginDate').disable();
						Ext.getCmp('memberCondEndDate').disable();
					}
				}
			}
		},{
			columnWidth : 0.3,
			xtype : 'datefield',	
			id : 'memberCondBeginDate',
			format : 'Y-m-d',
			width : 100,
			readOnly : false,			
		},{
			columnWidth : 0.3,
			xtype : 'datefield',	
			id : 'memberCondEndDate',
			format : 'Y-m-d',
			width : 100,
			readOnly : false,			
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false		
		}],
		bbar : ['->',{
			text : '保存',
			id : 'btnSaveMemberCond',
			iconCls : 'btn_save',
			handler : function(){
				var id = Ext.getCmp('memberCondId').getValue();
				var name = Ext.getCmp('txtMemberCondName').getValue();
				var memberType = Ext.getCmp('memberCondByType').getValue();
				var memberCondMinConsume = Ext.getCmp('memberCondMinConsume').getValue();
				var memberCondMaxConsume = Ext.getCmp('memberCondMaxConsume').getValue();
				var memberCondMinAmount = Ext.getCmp('memberCondMinAmount').getValue();
				var memberCondMaxAmount = Ext.getCmp('memberCondMaxAmount').getValue();
				var memberCondMinBalance = Ext.getCmp('memberCondMinBalance').getValue();
				var memberCondMaxBalance = Ext.getCmp('memberCondMaxBalance').getValue();
				var memberCondDateRegion = Ext.getCmp('memberCondDateRegion').getValue();
				var memberCondBeginDate = Ext.getCmp('memberCondBeginDate').getValue();
				var memberCondEndDate = Ext.getCmp('memberCondEndDate').getValue();
				
				Ext.Ajax.request({
					url : '../../OperateMemberCond.do',
					params : {
						dataSource : memberCondWin.operationType,
						id : id,
						name : name,
						memberType : memberType,
						memberCondMinConsume : memberCondMinConsume,
						memberCondMaxConsume : memberCondMaxConsume,
						memberCondMinAmount : memberCondMinAmount,
						memberCondMaxAmount : memberCondMaxAmount,
						
						memberCondMinBalance : memberCondMinBalance,
						memberCondMaxBalance : memberCondMaxBalance,
						memberCondDateRegion : memberCondDateRegion,
						memberCondBeginDate : memberCondBeginDate,
						memberCondEndDate : memberCondEndDate
					},
					success : function(response, options) {
						var jr = Ext.util.JSON.decode(response.responseText);
						if(jr.success){
							Ext.example.msg(jr.title,jr.msg);
							memberCondWin.hide();
							memberCondTree.getRootNode().reload();
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(response, options) {
						
					}
				});
			}
		}, {
			text : '取消',
			id : 'btnCloseMemberCond',
			iconCls : 'btn_close',
			handler : function(){
				memberCondWin.hide();
			}
		}],
		listeners : {
			'show' : function(thiz){
			}
		},
		keys : [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp("btnSaveMemberCond").handler();
			}
		}]
	});
	
}

Ext.onReady(function(){
	
	memberCondTreeInit();
	memberCondGridInit();
	
	var memberTypePanel = new Ext.Panel({
		layout : 'border',
		width : 240,
		frame : false,
		region : 'west',
		items : [memberCondTree
				, new Ext.Panel({
					id : 'memberTypeLevelChartsPanel',
					title : '会员操作',
					region : 'south',
					contentEl : 'divMemberTypeLevelCharts'
				})
		]
	});
	
	new Ext.Panel({
		renderTo : 'divMember',
		id : 'memberMgrPanel',
//		width : parseInt(Ext.getDom('divMember').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMember').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [memberTypePanel, memberCondBasicGrid],
		autoScroll : true
	});
	 
	showFloatOption(memberCond_obj);
	
	
});
