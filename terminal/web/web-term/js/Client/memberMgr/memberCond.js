
var memberCondBasicGrid, 
	memberBasicWin, 
	adjustPointWin, 
	memberCouponWin,
	m_memberTypeWin, 
	m_searchAdditionFilter = 'create',
	memberCondWin,
	memberCond_obj = { treeId : 'tree_memberCond', option : [{name : '修改', fn : "updateMemberCond()"}, {name : '删除', fn : "deleteMemberCond()" }] };


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
				showMemberCondWin(jr.root[0]);
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
	gs.baseParams['memberType'] = Ext.getCmp('comboMemberType4CondBar').getValue();
	gs.baseParams['minCost4CondWin'] = Ext.getCmp('minCost4CondBar').getValue();
	gs.baseParams['maxCost4CondWin'] = Ext.getCmp('maxCost4CondBar').getValue();
	gs.baseParams['minAmount4CondWin'] = Ext.getCmp('minAmount4CondBar').getValue();
	gs.baseParams['maxAmount4CondWin'] = Ext.getCmp('maxAmount4CondBar').getValue();
	gs.baseParams['minBalance4CondWin'] = Ext.getCmp('minBalance4CondBar').getValue();
	gs.baseParams['maxBalance4CondWin'] = Ext.getCmp('maxBalance4CondBar').getValue();
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

function refreshConsumeCost(){
	//修改弹出框
	var value = Ext.getCmp('comboCost4CondWin').getValue();
	if(value == 1){
		//大于
		Ext.getCmp('betweenCost4CondWin').hide();
		Ext.getCmp('minCost4CondWin').show();
		Ext.getCmp('maxCost4CondWin').hide();
		$('#betweenCost4CondWin').hide();
		$('#minCost4CondWin').show();
		$('#maxCost4CondWin').hide();
		Ext.getCmp('minCost4CondWin').focus(true, 100);
	}else if(value == 2){
		//小于
		Ext.getCmp('betweenCost4CondWin').hide();
		Ext.getCmp('maxCost4CondWin').show();
		Ext.getCmp('minCost4CondWin').hide();
		$('#betweenCost4CondWin').hide();
		$('#maxCost4CondWin').show();
		$('#minCost4CondWin').hide();
		Ext.getCmp('maxCost4CondWin').focus(true, 100);
	}else if(value == 3){
		//介于
		Ext.getCmp('betweenCost4CondWin').show();
		Ext.getCmp('minCost4CondWin').show();
		Ext.getCmp('maxCost4CondWin').show();
		$('#betweenCost4CondWin').show();
		$('#minCost4CondWin').show();
		$('#maxCost4CondWin').show();
		Ext.getCmp('minCost4CondWin').focus(true, 100);
	}else{
		//无设定
		Ext.getCmp('betweenCost4CondWin').show();
		Ext.getCmp('minCost4CondWin').show();
		Ext.getCmp('maxCost4CondWin').show();
		$('#betweenCost4CondWin').show();
		$('#minCost4CondWin').show();
		$('#maxCost4CondWin').show();
	}
	
	
	//ToolBar
	value = Ext.getCmp('comboConsumeMoney4CondBar').getValue();
	if(value == 1){
		//小于
		Ext.getCmp('betweenCost4CondBar').hide();
		Ext.getCmp('minCost4CondBar').show();
		Ext.getCmp('maxCost4CondBar').hide();
		
	}else if(value == 2){
		//大于
		Ext.getCmp('betweenCost4CondBar').hide();
		Ext.getCmp('maxCost4CondBar').show();
		Ext.getCmp('minCost4CondBar').hide();
	}else if(value == 3){
		//介于
		Ext.getCmp('betweenCost4CondBar').show();
		Ext.getCmp('minCost4CondBar').show();
		Ext.getCmp('maxCost4CondBar').show();
		$('#betweenCost4CondBar').show();
		$('#minCost4CondBar').show();
		$('#maxCost4CondBar').show();
	}else{
		//无设定
		Ext.getCmp('betweenCost4CondBar').show();
		Ext.getCmp('minCost4CondBar').show();
		Ext.getCmp('maxCost4CondBar').show();
		$('#betweenCost4CondBar').show();
		$('#minCost4CondBar').show();
		$('#maxCost4CondBar').show();
	}
}

function refreshConsumeAmount(){
	//修改弹出框
	var value = Ext.getCmp('comboAmount4CondWin').getValue();
	if(value == 1){
		//小于
		Ext.getCmp('betweenAmount4CondWin').hide();
		Ext.getCmp('minAmount4CondWin').show();
		Ext.getCmp('maxAmount4CondWin').hide();
		$('#betweenAmount4CondWin').hide();
		$('#minAmount4CondWin').show();
		$('#maxAmount4CondWin').hide();
		Ext.getCmp('minAmount4CondWin').focus(true, 100);
	}else if(value == 2){
		//大于
		Ext.getCmp('betweenAmount4CondWin').hide();
		Ext.getCmp('maxAmount4CondWin').show();
		Ext.getCmp('minAmount4CondWin').hide();
		$('#betweenAmount4CondWin').hide();
		$('#maxAmount4CondWin').show();
		$('#minAmount4CondWin').hide();
		Ext.getCmp('maxAmount4CondWin').focus(true, 100);
	}else if(value == 3){
		//介于
		Ext.getCmp('betweenAmount4CondWin').show();
		Ext.getCmp('minAmount4CondWin').show();
		Ext.getCmp('maxAmount4CondWin').show();
		$('#betweenAmount4CondWin').show();
		$('#minAmount4CondWin').show();
		$('#maxAmount4CondWin').show();
		Ext.getCmp('minAmount4CondWin').focus(true, 100);
	}else{
		//无设定
		Ext.getCmp('betweenAmount4CondWin').show();
		Ext.getCmp('minAmount4CondWin').show();
		Ext.getCmp('maxAmount4CondWin').show();
		$('#betweenAmount4CondWin').show();
		$('#minAmount4CondWin').show();
		$('#maxAmount4CondWin').show();
	}
	
	//ToolBar
	value = Ext.getCmp('comboAmount4CondBar').getValue();
	if(value == 1){
		//小于
		Ext.getCmp('betweenAmount4CondBar').hide();
		Ext.getCmp('minAmount4CondBar').show();
		Ext.getCmp('maxAmount4CondBar').hide();
	}else if(value == 2){
		//大于
		Ext.getCmp('betweenAmount4CondBar').hide();
		Ext.getCmp('maxAmount4CondBar').show();
		Ext.getCmp('minAmount4CondBar').hide();
	}else if(value == 3){
		//介于
		Ext.getCmp('betweenAmount4CondBar').show();
		Ext.getCmp('minAmount4CondBar').show();
		Ext.getCmp('maxAmount4CondBar').show();
		$('#betweenCost4CondBar').show();
		$('#minAmount4CondBar').show();
		$('#maxAmount4CondBar').show();
	}else{
		//无设定
		Ext.getCmp('betweenAmount4CondBar').show();
		Ext.getCmp('minAmount4CondBar').show();
		Ext.getCmp('maxAmount4CondBar').show();
		$('#betweenCost4CondBar').show();
		$('#minAmount4CondBar').show();
		$('#maxAmount4CondBar').show();
	}
}


function refreshBalance(){
	//修改弹出框
	var value = Ext.getCmp('comboBalance4CondWin').getValue();
	if(value == 1){
		//小于
		Ext.getCmp('betweenBalance4CondWin').hide();
		Ext.getCmp('minBalance4CondWin').show();
		Ext.getCmp('maxBalance4CondWin').hide();
		$('#betweenBalance4CondWin').hide();
		$('#minBalance4CondWin').show();
		$('#maxBalance4CondWin').hide();
		Ext.getCmp('minBalance4CondWin').focus(true, 100);
	}else if(value == 2){
		//大于
		Ext.getCmp('betweenBalance4CondWin').hide();
		Ext.getCmp('maxBalance4CondWin').show();
		Ext.getCmp('minBalance4CondWin').hide();
		$('#betweenBalance4CondWin').hide();
		$('#maxBalance4CondWin').show();
		$('#minBalance4CondWin').hide();
		Ext.getCmp('maxBalance4CondWin').focus(true, 100);
	}else if(value == 3){
		//介于
		Ext.getCmp('betweenBalance4CondWin').show();
		Ext.getCmp('minBalance4CondWin').show();
		Ext.getCmp('maxBalance4CondWin').show();
		$('#betweenBalance4CondWin').show();
		$('#minBalance4CondWin').show();
		$('#maxBalance4CondWin').show();
		Ext.getCmp('minBalance4CondWin').focus(true, 100);
	}else{
		//无设定
		Ext.getCmp('betweenAmount4CondWin').show();
		Ext.getCmp('minBalance4CondWin').show();
		Ext.getCmp('maxBalance4CondWin').show();
		$('#betweenBalance4CondWin').show();
		$('#minBalance4CondWin').show();
		$('#maxBalance4CondWin').show();
	}
	
	//ToolBar
	value = Ext.getCmp('comboBalance4CondBar').getValue();
	if(value == 1){
		//小于
		Ext.getCmp('betweenBalance4CondBar').hide();
		Ext.getCmp('minBalance4CondBar').show();
		Ext.getCmp('maxBalance4CondBar').hide();
	}else if(value == 2){
		//大于
		Ext.getCmp('betweenBalance4CondBar').hide();
		Ext.getCmp('maxAmount4CondBar').show();
		Ext.getCmp('maxBalance4CondBar').hide();
	}else if(value == 3){
		//介于
		Ext.getCmp('betweenBalance4CondBar').show();
		Ext.getCmp('minBalance4CondBar').show();
		Ext.getCmp('maxBalance4CondBar').show();
		$('#betweenCost4CondBar').show();
		$('#minBalance4CondBar').show();
		$('#maxBalance4CondBar').show();
	}else{
		//无设定
		Ext.getCmp('betweenBalance4CondBar').show();
		Ext.getCmp('minBalance4CondBar').show();
		Ext.getCmp('maxBalance4CondBar').show();
		$('#betweenCost4CondBar').show();
		$('#minBalance4CondBar').show();
		$('#maxBalance4CondBar').show();
	}
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
				showMemberCondWin();
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
				expand : function(thiz){
					var rn = memberCondTree.getRootNode().childNodes;
					rn[0].select();
    				rn[0].fireEvent('click', rn[0]);
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
	    		    		Ext.getCmp('comboMemberType4CondBar').setValue(jr.root[0].memberType);
	    		    		Ext.getCmp('minCost4CondBar').setValue(jr.root[0].minConsumeMoney > 0?jr.root[0].minConsumeMoney:'');
	    		    		Ext.getCmp('maxCost4CondBar').setValue(jr.root[0].maxConsumeMoney > 0?jr.root[0].maxConsumeMoney:'');
	    		    		Ext.getCmp('minAmount4CondBar').setValue(jr.root[0].minConsumeAmount > 0?jr.root[0].minConsumeAmount:'');
	    		    		Ext.getCmp('maxAmount4CondBar').setValue(jr.root[0].maxConsumeAmount > 0?jr.root[0].maxConsumeAmount:'');
	    		    		Ext.getCmp('minBalance4CondBar').setValue(jr.root[0].minBalance > 0 ? jr.root[0].minBalance : '');
	    		    		Ext.getCmp('maxBalance4CondBar').setValue(jr.root[0].maxBalance > 0 ? jr.root[0].maxBalance : '');
	    		    		Ext.getCmp('dateSearchDateBegin').setValue(jr.root[0].beginDate);
	    		    		Ext.getCmp('dateSearchDateEnd').setValue(jr.root[0].endDate);
	    					
	    		    		//消费金额
    		    			if(jr.root[0].minConsumeMoney > 0 && jr.root[0].maxConsumeMoney == 0){
    		    				//大于
    		    				Ext.getCmp('comboConsumeMoney4CondBar').setValue(1);
    		    			}else if(jr.root[0].minConsumeMoney == 0 && jr.root[0].maxConsumeMoney > 0){
    		    				//小于
    		    				Ext.getCmp('comboConsumeMoney4CondBar').setValue(2);
    		    			}else if(jr.root[0].minConsumeMoney > 0 && jr.root[0].maxConsumeMoney > 0){
    		    				//介于
    		    				Ext.getCmp('comboConsumeMoney4CondBar').setValue(3);
    		    			}else{
    		    				//无设定
    		    				Ext.getCmp('comboConsumeMoney4CondBar').setValue(0);
    		    			}
    		    			refreshConsumeCost();
    		    			
    		    			//消费次数
    		    			if(jr.root[0].minConsumeAmount > 0 && jr.root[0].maxConsumeAmount == 0){
    		    				//大于
    		    				Ext.getCmp('comboAmount4CondBar').setValue(1);
    		    			}else if(jr.root[0].minConsumeAmount == 0 && jr.root[0].maxConsumeAmount > 0){
    		    				//小于
    		    				Ext.getCmp('comboAmount4CondBar').setValue(2);
    		    			}else if(jr.root[0].minConsumeAmount > 0 && jr.root[0].maxConsumeAmount > 0){
    		    				//介于
    		    				Ext.getCmp('comboAmount4CondBar').setValue(3);
    		    			}else{
    		    				//无设定
    		    				Ext.getCmp('comboAmount4CondBar').setValue(0);
    		    			}
    		    			refreshConsumeAmount();
    		    			
    		    			//余额
    		    			if(jr.root[0].minBalance > 0 && jr.root[0].maxBalance == 0){
    		    				//余额大于
    		    				Ext.getCmp('comboBalance4CondBar').setValue(1);
    		    			}else if(jr.root[0].minBalance == 0 && jr.root[0].maxBalance > 0){
    		    				//余额小于
    		    				Ext.getCmp('comboBalance4CondBar').setValue(2);
    		    			}else if(jr.root[0].minBalance > 0 && jr.root[0].maxBalance > 0){
    		    				//余额介于
    		    				Ext.getCmp('comboBalance4CondBar').setValue(3);
    		    			}else{
    		    				//余额无设定
    		    				Ext.getCmp('comboBalance4CondBar').setValue(0);
    		    			}
		    				refreshBalance();
    		    			
		    				//会员类型
		    				Ext.getCmp('comboMemberType4CondBar').setValue(jr.root[0].memberType ? jr.root[0].memberType : -1);
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
	
	var memberCond1stTBar = new Ext.Toolbar({
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
				id : 'comboMemberType4CondBar',
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
									Ext.example.msg('异常', '会员类型数据加载失败');
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
						Ext.getCmp('comboMemberType4CondBar').getValue(),
						Ext.getCmp('minCost4CondBar').getValue(),
						Ext.getCmp('maxCost4CondBar').getValue(),
						Ext.getCmp('minAmount4CondBar').getValue(),
						Ext.getCmp('maxAmount4CondBar').getValue(),
						Ext.getCmp('minBalance4CondBar').getValue(),
						Ext.getCmp('maxBalance4CondBar').getValue(),
						Ext.util.Format.date(Ext.getCmp('dateSearchDateBegin').getValue(), 'Y-m-d 00:00:00'),
						Ext.util.Format.date(Ext.getCmp('dateSearchDateEnd').getValue(), 'Y-m-d 23:59:59'),
						'memberList'
					);
					
					window.location = url;
				}
			}]
		
	});
	
	var memberCond2ndTBar = new Ext.Toolbar({
		height : 28,		
		items : [{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
			{xtype : 'tbtext', text : '消费金额:'}, 
			{
				id : 'comboConsumeMoney4CondBar',
				xtype : 'combo',
				readOnly : false,
				forceSelection : true,
				value : 1,
				width : 80,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : [[1, '大于'], [2, '小于'], [3, '介于'], [0, '无设定']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				listeners : {
					select : function(combo, record, index){
						Ext.getCmp('minCost4CondBar').setValue();
						Ext.getCmp('maxCost4CondBar').setValue();
						refreshConsumeCost();
					}
				}
			},
			{
				xtype : 'numberfield',
				id : 'minCost4CondBar',
				width : 60
			},
			{
				xtype : 'tbtext',
				id : 'betweenCost4CondBar',
				text : '&nbsp;-&nbsp;',
				hidden : true
			},			
			{
				xtype : 'numberfield',
				id : 'maxCost4CondBar',
				width : 60,
				hidden : true
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '消费次数:'},
			{
				id : 'comboAmount4CondBar',
				xtype : 'combo',
				readOnly : false,
				forceSelection : true,
				value : 1,
				width : 80,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : [[1, '大于'], [2, '小于'], [3, '介于'], [0, '无设定']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				listeners : {
					select : function(combo, record, index){
						Ext.getCmp('minAmount4CondBar').setValue();
						Ext.getCmp('maxAmount4CondBar').setValue();
						refreshConsumeAmount();
					}
				}
			},
			{
				xtype : 'numberfield',
				id : 'minAmount4CondBar',
				width : 50
			},
			{
				id : 'betweenAmount4CondBar',
				xtype : 'tbtext',
				text : '&nbsp;-&nbsp;'
			},			
			{
				xtype : 'numberfield',
				id : 'maxAmount4CondBar',
				width : 50
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
			{xtype : 'tbtext', text : '余额:'},
			{
				id : 'comboBalance4CondBar',
				xtype : 'combo',
				readOnly : false,
				forceSelection : true,
				value : 1,
				width : 80,
				store : new Ext.data.SimpleStore({
					fields : ['value', 'text'],
					data : [[1, '大于'], [2, '小于'], [3, '介于'], [0, '无设定']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				listeners : {
					select : function(combo, record, index){
						Ext.getCmp('minBalance4CondBar').setValue();
						Ext.getCmp('maxBalance4CondBar').setValue();
						refreshBalance();
					}
				}
			},
			{
				xtype : 'numberfield',
				id : 'minBalance4CondBar',
				width : 50
			},
			{
				id : 'betweenBalance4CondBar',
				xtype : 'tbtext',
				text : '&nbsp;-&nbsp;'
			},			
			{
				xtype : 'numberfield',
				id : 'maxBalance4CondBar',
				width : 50
			}			
			]
		
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
		[memberCond1stTBar, memberCond2ndTBar]
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
 * 显示会员筛选条件Window
 * @param data
 */
function showMemberCondWin(data){
	data = data || {};
	Ext.getCmp('memberCondId').setValue(data.id);
	Ext.getCmp('txtMemberCondName').setValue(data.name);
	Ext.getCmp('memberCondByType').setValue(data.memberType ? data.memberType : -1);
	Ext.getCmp('minCost4CondWin').setValue(data.minConsumeMoney && data.minConsumeMoney > 0 ? data.minConsumeMoney : "");
	Ext.getCmp('maxCost4CondWin').setValue(data.maxConsumeMoney && data.maxConsumeMoney > 0 ? data.maxConsumeMoney : "");
	Ext.getCmp('minAmount4CondWin').setValue(data.minConsumeAmount && data.minConsumeAmount > 0 ? data.minConsumeAmount : "");
	Ext.getCmp('maxAmount4CondWin').setValue(data.maxConsumeAmount && data.maxConsumeAmount > 0 ? data.maxConsumeAmount : "");
	Ext.getCmp('minBalance4CondWin').setValue(data.minBalance && data.minBalance > 0 ? data.minBalance : "");
	Ext.getCmp('maxBalance4CondWin').setValue(data.maxBalance && data.maxBalance > 0 ? data.maxBalance : "");
	Ext.getCmp('memberCondDateRegion').setValue(data.rangeType);
	
	if(data.name){
		//修改
		if(data.minConsumeMoney > 0 && data.maxConsumeMoney == 0){
			//消费金额大于
			Ext.getCmp('comboCost4CondWin').setValue(1);
			//Ext.getCmp('comboCost4CondWin').fireEvent('select', null, null, 0);
		}else if(data.minConsumeMoney == 0 && data.maxConsumeMoney > 0){
			//消费金额小于
			Ext.getCmp('comboCost4CondWin').setValue(2);
		}else if(data.minConsumeMoney > 0 && data.maxConsumeMoney > 0){
			//消费金额介于
			Ext.getCmp('comboCost4CondWin').setValue(3);
		}else{
			//消费金额无设定
			Ext.getCmp('comboCost4CondWin').setValue(0);
		}
		refreshConsumeCost();	
		
		if(data.minConsumeAmount > 0 && data.maxConsumeAmount == 0){
			//消费次数大于
			Ext.getCmp('comboAmount4CondWin').setValue(1);
			//Ext.getCmp('comboCost4CondWin').fireEvent('select', null, null, 0);
		}else if(data.minConsumeAmount == 0 && data.maxConsumeAmount > 0){
			//消费次数小于
			Ext.getCmp('comboAmount4CondWin').setValue(2);
		}else if(data.minConsumeAmount > 0 && data.maxConsumeAmount > 0){
			//消费次数介于
			Ext.getCmp('comboAmount4CondWin').setValue(3);
		}else{
			//消费次数无设定
			Ext.getCmp('comboAmount4CondWin').setValue(0);
		}
		refreshConsumeAmount();
		
		if(data.minBalance > 0 && data.maxBalance == 0){
			//余额大于
			Ext.getCmp('comboBalance4CondWin').setValue(1);
			//Ext.getCmp('comboCost4CondWin').fireEvent('select', null, null, 0);
		}else if(data.minBalance == 0 && data.maxBalance > 0){
			//余额小于
			Ext.getCmp('comboBalance4CondWin').setValue(2);
		}else if(data.minBalance > 0 && data.maxBalance > 0){
			//余额介于
			Ext.getCmp('comboBalance4CondWin').setValue(3);
		}else{
			//余额无设定
			Ext.getCmp('comboBalance4CondWin').setValue(0);
		}
		refreshBalance();
		
	}else{
		//新增
		Ext.getCmp('comboCost4CondWin').setValue(0);
		Ext.getCmp('comboCost4CondWin').fireEvent('select', null, null, 0);
		
		Ext.getCmp('comboAmount4CondWin').setValue(0);
		Ext.getCmp('comboAmount4CondWin').fireEvent('select', null, null, 0);
		
		Ext.getCmp('comboBalance4CondWin').setValue(0);
		Ext.getCmp('comboBalance4CondWin').fireEvent('select', null, null, 0);
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

	
	var comboMemberType4CondWin = new Ext.form.ComboBox({
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
		selectOnFocus : true
	});	
	
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
				Ext.example.msg('异常', '会员类型数据加载失败');
			}
			comboMemberType4CondWin.store.loadData(jr);
			comboMemberType4CondWin.setValue(-1);
		},
		failure : function(res, opt){
			comboMemberType4CondWin.store.loadData({root:[{typeId:-1, name:'全部'}]});
			comboMemberType4CondWin.setValue(-1);
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
		} ,comboMemberType4CondWin,
		{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '消费金额:',
		}, {
			columnWidth : 0.2,
			id : 'comboCost4CondWin',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : 1,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[1, '大于'], [2, '小于'], [3, '介于'], [0, '无设定']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(combo, record, index){
					Ext.getCmp('minCost4CondWin').setValue();
					Ext.getCmp('maxCost4CondWin').setValue();
					refreshConsumeCost();
				}
			}
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'minCost4CondWin',
		},{
			xtype : 'label',
			id : 'betweenCost4CondWin',
			text : ' ~ ',
			hidden : true
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'maxCost4CondWin',
			hidden : true
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '消费次数:'
		},{
			columnWidth : 0.2,
			id : 'comboAmount4CondWin',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : 1,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[1, '大于'], [2, '小于'], [3, '介于'], [0, '无设定']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(combo, record, index){
					Ext.getCmp('minAmount4CondWin').setValue();
					Ext.getCmp('maxAmount4CondWin').setValue();
					refreshConsumeAmount();
				}
			}
		},{
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'minAmount4CondWin',
		},{
			id : 'betweenAmount4CondWin',
			xtype : 'label',
			text : ' ~ ',
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'maxAmount4CondWin',
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '余额:'
		},{
			columnWidth : 0.2,
			id : 'comboBalance4CondWin',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : 1,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[1, '大于'], [2, '小于'], [3, '介于'], [0, '无设定']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(combo, record, index){
					Ext.getCmp('minBalance4CondWin').setValue();
					Ext.getCmp('maxBalance4CondWin').setValue();
					refreshBalance();
				}
			}
		},{
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'minBalance4CondWin',
		},{
			id : 'betweenBalance4CondWin',
			xtype : 'label',
			text : ' ~ ',
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'maxBalance4CondWin',
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
				var minCost4CondWin = Ext.getCmp('minCost4CondWin').getValue();
				var maxCost4CondWin = Ext.getCmp('maxCost4CondWin').getValue();
				var minAmount4CondWin = Ext.getCmp('minAmount4CondWin').getValue();
				var maxAmount4CondWin = Ext.getCmp('maxAmount4CondWin').getValue();
				var minBalance4CondWin = Ext.getCmp('minBalance4CondWin').getValue();
				var maxBalance4CondWin = Ext.getCmp('maxBalance4CondWin').getValue();
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
						memberCondMinConsume : minCost4CondWin,
						memberCondMaxConsume : maxCost4CondWin,
						memberCondMinAmount : minAmount4CondWin,
						memberCondMaxAmount : maxAmount4CondWin,
						
						memberCondMinBalance : minBalance4CondWin,
						memberCondMaxBalance : maxBalance4CondWin,
						memberCondDateRegion : memberCondDateRegion,
						memberCondBeginDate : memberCondBeginDate,
						memberCondEndDate : memberCondEndDate
					},
					success : function(response, options) {
						var jr = Ext.util.JSON.decode(response.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							memberCondWin.hide();
							for(var i = 0; i < memberCondTree.getRootNode().childNodes.length; i++){
								var node = memberCondTree.getRootNode().childNodes[i];
								if(node.id == id){
									node.select();
									node.fireEvent('click', node);
								}
							}
							//memberCondTree.getRootNode().reload();
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
	

Ext.onReady(function(){
	
	memberCondTreeInit();
	memberCondGridInit();
	
	var memberPromotionOperationPanel = new Ext.Panel({
		layout : 'border',
		width : 240,
		frame : false,
		region : 'west',
		items : [memberCondTree
				, new Ext.Panel({
					id : 'memberPromotionOperation',
					title : '会员操作',
					region : 'south',
					contentEl : 'divMemberCondOperation'
				})
		]
	});
	
	new Ext.Panel({
		renderTo : 'divMemberCond',
		id : 'memberMgrPanel',
		height : parseInt(Ext.getDom('divMemberCond').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		items : [memberPromotionOperationPanel, memberCondBasicGrid],
		autoScroll : true
	});
	 
	showFloatOption(memberCond_obj);
	
	
});
