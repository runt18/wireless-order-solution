Ext.onReady(function(){
	//会员分析条件的TreePanel
	var memberCondTree;
	var memberCondBasicGrid; 
	var	memberCondWin;

	/**
	 * 修改memberCond
	 */
	function updateMemberCond(){
		var sn = Ext.ux.getSelNode(memberCondTree);
	
		Ext.Ajax.request({
			url : '../../OperateMemberCond.do',
			params : {
				dataSource : "getById",
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
		gs.baseParams['memberType'] = Ext.getCmp('memberType_combo_memberCond').getValue();
		gs.baseParams['minCost4CondWin'] = Ext.getCmp('minCost_numField_memberCond').getValue();
		gs.baseParams['maxCost4CondWin'] = Ext.getCmp('maxCost_numField_memberCond').getValue();
		gs.baseParams['minAmount4CondWin'] = Ext.getCmp('minAmount_numField_memberCond').getValue();
		gs.baseParams['maxAmount4CondWin'] = Ext.getCmp('maxAmount4CondBar').getValue();
		gs.baseParams['minBalance4CondWin'] = Ext.getCmp('minBalance_numField_memberCond').getValue();
		gs.baseParams['maxBalance4CondWin'] = Ext.getCmp('maxBalance_numField_memberCond').getValue();
		gs.baseParams['memberCondBeginDate'] = Ext.util.Format.date(Ext.getCmp('srchBegin_dateField_memberCond').getValue(), 'Y-m-d 00:00:00');
		gs.baseParams['memberCondEndDate'] = Ext.util.Format.date(Ext.getCmp('srchEnd_dateField_memberCond').getValue(), 'Y-m-d 23:59:59');
		gs.baseParams['sex'] = Ext.getCmp('memberCondSex_combo_memebercond').getValue();
		gs.baseParams['age'] = Ext.getCmp('memberCondAge_combo_memebercond').getValue();
		if(Ext.getCmp('isBind_checkbox_memberCond').getValue() == '0'){
			gs.baseParams['isRaw'] = false;
		}else if(Ext.getCmp('isBind_checkbox_memberCond').getValue() == '1'){
			gs.baseParams['isRaw'] = true;
		}else{
			gs.baseParams['isRaw'] = Ext.getCmp('isBind_checkbox_memberCond').getValue();
		}
		
		gs.baseParams['memberCondMinCharge'] = Ext.getCmp('memberMinCharge_numberField_memberCond').getValue();
		gs.baseParams['memberCondMaxCharge'] = Ext.getCmp('memberMaxCharge_numberField_memberCond').getValue();
			
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
			Ext.getCmp('betweenCost_numField_memberCond').hide();
			Ext.getCmp('minCost_numField_memberCond').show();
			Ext.getCmp('maxCost_numField_memberCond').hide();
			
		}else if(value == 2){
			//大于
			Ext.getCmp('betweenCost_numField_memberCond').hide();
			Ext.getCmp('maxCost_numField_memberCond').show();
			Ext.getCmp('minCost_numField_memberCond').hide();
		}else if(value == 3){
			//介于
			Ext.getCmp('betweenCost_numField_memberCond').show();
			Ext.getCmp('minCost_numField_memberCond').show();
			Ext.getCmp('maxCost_numField_memberCond').show();
			$('#betweenCost_numField_memberCond').show();
			$('#minCost_numField_memberCond').show();
			$('#maxCost_numField_memberCond').show();
		}else{
			//无设定
			Ext.getCmp('betweenCost_numField_memberCond').show();
			Ext.getCmp('minCost_numField_memberCond').show();
			Ext.getCmp('maxCost_numField_memberCond').show();
			$('#betweenCost_numField_memberCond').show();
			$('#minCost_numField_memberCond').show();
			$('#maxCost_numField_memberCond').show();
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
		value = Ext.getCmp('consumptionAmount_combo_memberCond').getValue();
		if(value == 1){
			//小于
			Ext.getCmp('betweenAmount_text_memberCond').hide();
			Ext.getCmp('minAmount_numField_memberCond').show();
			Ext.getCmp('maxAmount4CondBar').hide();
		}else if(value == 2){
			//大于
			Ext.getCmp('betweenAmount_text_memberCond').hide();
			Ext.getCmp('maxAmount4CondBar').show();
			Ext.getCmp('minAmount_numField_memberCond').hide();
		}else if(value == 3){
			//介于
			Ext.getCmp('betweenAmount_text_memberCond').show();
			Ext.getCmp('minAmount_numField_memberCond').show();
			Ext.getCmp('maxAmount4CondBar').show();
			$('#betweenCost_numField_memberCond').show();
			$('#minAmount_numField_memberCond').show();
			$('#maxAmount4CondBar').show();
		}else{
			//无设定
			Ext.getCmp('betweenAmount_text_memberCond').show();
			Ext.getCmp('minAmount_numField_memberCond').show();
			Ext.getCmp('maxAmount4CondBar').show();
			$('#betweenCost_numField_memberCond').show();
			$('#minAmount_numField_memberCond').show();
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
			Ext.getCmp('betweenBalance_text_memberCond').hide();
			Ext.getCmp('minBalance_numField_memberCond').show();
			Ext.getCmp('maxBalance_numField_memberCond').hide();
		}else if(value == 2){
			//大于
			Ext.getCmp('betweenBalance_text_memberCond').hide();
			Ext.getCmp('minBalance_numField_memberCond').show();
			Ext.getCmp('maxBalance_numField_memberCond').hide();
		}else if(value == 3){
			//介于
			Ext.getCmp('betweenBalance_text_memberCond').show();
			Ext.getCmp('minBalance_numField_memberCond').show();
			Ext.getCmp('maxBalance_numField_memberCond').show();
			$('#betweenCost_numField_memberCond').show();
			$('#minBalance_numField_memberCond').show();
			$('#maxBalance_numField_memberCond').show();
		}else{
			//无设定
			Ext.getCmp('betweenBalance_text_memberCond').show();
			Ext.getCmp('minBalance_numField_memberCond').show();
			Ext.getCmp('maxBalance_numField_memberCond').show();
			$('#betweenCost_numField_memberCond').show();
			$('#minBalance_numField_memberCond').show();
			$('#maxBalance_numField_memberCond').show();
		}
		
	}
	
	function refreshCharge(){
		//ToolBar
		value = Ext.getCmp('comboCharge4CondBar_combo_memeberCond').getValue();
		if(value == 1){
			Ext.getCmp('betweenCharge_text_memberCond').hide();
			Ext.getCmp('minCharge_numField_memberCond').show();
			Ext.getCmp('maxCharge_numField_memberCond').hide();
		}else if(value == 2){
			//小于
			Ext.getCmp('betweenCharge_text_memberCond').hide();
			Ext.getCmp('minCharge_numField_memberCond').hide();
			Ext.getCmp('maxCharge_numField_memberCond').show();
		}else if(value == 3){
			//介于
			Ext.getCmp('betweenCharge_text_memberCond').show();
			Ext.getCmp('minCharge_numField_memberCond').show();
			Ext.getCmp('maxCharge_numField_memberCond').show();
		}else{
			//无设定
			Ext.getCmp('betweenCharge_text_memberCond').show();
			Ext.getCmp('minCharge_numField_memberCond').show();
			Ext.getCmp('maxCharge_numField_memberCond').show();
		}
		
		//修改弹出框
		var value = Ext.getCmp('memberCharge_combo_memberCond').getValue();
		if(value == 1){
			//大于
			Ext.getCmp('memberChargeWith_label_memberCond').hide();
			Ext.getCmp('memberMinCharge_numberField_memberCond').show();
			Ext.getCmp('memberMaxCharge_numberField_memberCond').hide();
			Ext.getCmp('memberChargeWith_label_memberCond').enable();
			Ext.getCmp('memberMinCharge_numberField_memberCond').enable();
			Ext.getCmp('memberMaxCharge_numberField_memberCond').enable();
			$('#memberChargeWith_label_memberCond').hide();
			$('#memberMinCharge_numberField_memberCond').show();
			$('#memberMaxCharge_numberField_memberCond').hide();
			Ext.getCmp('memberMinCharge_numberField_memberCond').focus(true, 100);
		}else if(value == 2){
			//小于
			Ext.getCmp('memberChargeWith_label_memberCond').hide();
			Ext.getCmp('memberMinCharge_numberField_memberCond').hide();
			Ext.getCmp('memberMaxCharge_numberField_memberCond').show();
			Ext.getCmp('memberChargeWith_label_memberCond').enable();
			Ext.getCmp('memberMinCharge_numberField_memberCond').enable();
			Ext.getCmp('memberMaxCharge_numberField_memberCond').enable();
			$('#memberChargeWith_label_memberCond').hide();
			$('#memberMinCharge_numberField_memberCond').hide();
			$('#memberMaxCharge_numberField_memberCond').show();
			Ext.getCmp('memberMaxCharge_numberField_memberCond').focus(true, 100);	
		}else if(value == 3){
			//介于
			Ext.getCmp('memberChargeWith_label_memberCond').show();
			Ext.getCmp('memberMinCharge_numberField_memberCond').show();
			Ext.getCmp('memberMaxCharge_numberField_memberCond').show();
			Ext.getCmp('memberChargeWith_label_memberCond').enable();
			Ext.getCmp('memberMinCharge_numberField_memberCond').enable();
			Ext.getCmp('memberMaxCharge_numberField_memberCond').enable();
			$('#memberChargeWith_label_memberCond').show();
			$('#memberMinCharge_numberField_memberCond').show();
			$('#memberMaxCharge_numberField_memberCond').show();
			Ext.getCmp('memberMinCharge_numberField_memberCond').focus(true, 100);
		}else{
			//无设定
			Ext.getCmp('memberChargeWith_label_memberCond').show();
			Ext.getCmp('memberMinCharge_numberField_memberCond').show();
			Ext.getCmp('memberMaxCharge_numberField_memberCond').show();
			Ext.getCmp('memberChargeWith_label_memberCond').disable();
			Ext.getCmp('memberMinCharge_numberField_memberCond').disable();
			Ext.getCmp('memberMaxCharge_numberField_memberCond').disable();
			$('#memberChargeWith_label_memberCond').show();
			$('#memberMinCharge_numberField_memberCond').show();
			$('#memberMaxCharge_numberField_memberCond').show();
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
				iconCls : 'btn_refresh',
				handler : function(){
					Ext.getDom('memberCondName').innerHTML = '----';
					Ext.getDom('isBind_tbtext_memberCond').innerHTML = '无设定';
					Ext.getDom('memeberAge_tbtext_memberCond').innerHTML = '无设定';
					Ext.getDom('memeberSex_tbtext_memberCond').innerHTML = '无设定';
					memberCondTree.getRootNode().reload();
				}
			}]
		});
		
		memberCondTree = new Ext.tree.TreePanel({
			id : 'condition_tree_memberCond',
			title : 	'筛选条件',
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
		    		
		    		clickTree = e.text;
		    		clickTreeId = e.id;
		    		Ext.Ajax.request({
		    			url : '../../OperateMemberCond.do',
		    			params : {
		    				dataSource : "getById",
		    				id : e.id
		    			},	
		    			success : function(response, options) {
		    				var jr = Ext.util.JSON.decode(response.responseText);
		    				if(jr.success){
		    		    		Ext.getCmp('memberType_combo_memberCond').setValue(jr.root[0].memberType);
		    		    		Ext.getCmp('minCost_numField_memberCond').setValue(jr.root[0].minConsumeMoney > 0 ? jr.root[0].minConsumeMoney:'');
		    		    		Ext.getCmp('maxCost_numField_memberCond').setValue(jr.root[0].maxConsumeMoney > 0 ? jr.root[0].maxConsumeMoney:'');
		    		    		Ext.getCmp('minAmount_numField_memberCond').setValue(jr.root[0].minConsumeAmount > 0 ? jr.root[0].minConsumeAmount:'');
		    		    		Ext.getCmp('maxAmount4CondBar').setValue(jr.root[0].maxConsumeAmount > 0 ? jr.root[0].maxConsumeAmount:'');
		    		    		Ext.getCmp('minBalance_numField_memberCond').setValue(jr.root[0].minBalance > 0 ? jr.root[0].minBalance : '');
		    		    		Ext.getCmp('maxBalance_numField_memberCond').setValue(jr.root[0].maxBalance > 0 ? jr.root[0].maxBalance : '');
		    		    		Ext.getCmp('srchBegin_dateField_memberCond').setValue(jr.root[0].beginDate);
		    		    		Ext.getCmp('srchEnd_dateField_memberCond').setValue(jr.root[0].endDate);
		    		    		Ext.getCmp('minCharge_numField_memberCond').setValue(jr.root[0].minCharge > 0 ? jr.root[0].minCharge : '');
		    		    		Ext.getCmp('maxCharge_numField_memberCond').setValue(jr.root[0].maxCharge > 0 ? jr.root[0].maxCharge : '');
		    		    		
		    		    		var maxFansAmount = jr.root[0].maxFansAmount;
		    		    		var minFansAmount = jr.root[0].minFansAmount;
		    		    		if(maxFansAmount > 0 && minFansAmount > 0){
		    		    			Ext.getCmp('showFansAmountConfig_combo_memberCond').setValue('between');
		    		    		}else if(maxFansAmount > 0 && minFansAmount <= 0){
		    		    			Ext.getCmp('showFansAmountConfig_combo_memberCond').setValue('max');
		    		    		}else if(maxFansAmount <= 0 && minFansAmount > 0){
		    		    			Ext.getCmp('showFansAmountConfig_combo_memberCond').setValue('min');
		    		    		}else{
		    		    			Ext.getCmp('showFansAmountConfig_combo_memberCond').setValue('null');
		    		    		}
		    		    		Ext.getCmp('showFansAmountMax_numberfield_memberCond').setValue(maxFansAmount > 0 ? maxFansAmount : '');
		    		    		Ext.getCmp('showFansAmountMin_numberfield_memberCond').setValue(minFansAmount > 0 ? minFansAmount : '');
		    		    		
		    		    		if(typeof jr.root[0].isRaw == 'undefined'){
		    		    			Ext.getDom('isBind_tbtext_memberCond').innerHTML  = '无设定';
		    		    		}else{
		    		    			if(jr.root[0].isRaw){
			    		    			Ext.getDom('isBind_tbtext_memberCond').innerHTML  = '未绑定';
			    		    		}else{
			    		    			Ext.getDom('isBind_tbtext_memberCond').innerHTML = '已绑定';
			    		    		}
		    		    		}
		    		    		
		    		    		if(jr.root[0].ageText){
		    		    			Ext.getDom('memeberAge_tbtext_memberCond').innerHTML = jr.root[0].ageText;
		    		    		}else{
		    		    			Ext.getDom('memeberAge_tbtext_memberCond').innerHTML = '无设定';
		    		    		}
		    		    		
		    		    		if(jr.root[0].sexText){
		    		    			Ext.getDom('memeberSex_tbtext_memberCond').innerHTML = jr.root[0].sexText;
		    		    		}else{
		    		    			Ext.getDom('memeberSex_tbtext_memberCond').innerHTML = '无设定';
		    		    		}
		    		    		
		    		    		
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
	    		    				Ext.getCmp('consumptionAmount_combo_memberCond').setValue(1);
	    		    			}else if(jr.root[0].minConsumeAmount == 0 && jr.root[0].maxConsumeAmount > 0){
	    		    				//小于
	    		    				Ext.getCmp('consumptionAmount_combo_memberCond').setValue(2);
	    		    			}else if(jr.root[0].minConsumeAmount > 0 && jr.root[0].maxConsumeAmount > 0){
	    		    				//介于
	    		    				Ext.getCmp('consumptionAmount_combo_memberCond').setValue(3);
	    		    			}else{
	    		    				//无设定
	    		    				Ext.getCmp('consumptionAmount_combo_memberCond').setValue(0);
	    		    			}
	    		    			refreshConsumeAmount();
	    		    			
	    		    			//充值
	    		    			if(jr.root[0].minCharge > 0 && jr.root[0].maxCharge == 0){
	    		    				//充值大于
	    		    				Ext.getCmp('comboCharge4CondBar_combo_memeberCond').setValue(1);
	    		    			}else if(jr.root[0].minCharge == 0 && jr.root[0].maxCharge > 0){
	    		    				//充值小于
	    		    				Ext.getCmp('comboCharge4CondBar_combo_memeberCond').setValue(2);
	    		    			}else if(jr.root[0].minCharge > 0 && jr.root[0].maxCharge > 0){
	    		    				//充值介于
	    		    				Ext.getCmp('comboCharge4CondBar_combo_memeberCond').setValue(3);
	    		    			}else{
	    		    				//充值无设定
	    		    				Ext.getCmp('comboCharge4CondBar_combo_memeberCond').setValue(0);
	    		    			}
	    		    			refreshCharge();
	    		    			
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
			    				Ext.getCmp('memberType_combo_memberCond').setValue(jr.root[0].memberType ? jr.root[0].memberType : -1);
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
			disabled : true,
			id : 'srchBegin_dateField_memberCond',
			format : 'Y-m-d',
			width : 100,
			readOnly : false
		});
		var member_endDate = new Ext.form.DateField({
			xtype : 'datefield',
			disabled : true,
			id : 'srchEnd_dateField_memberCond',
			format : 'Y-m-d',
			width : 100,
			readOnly : false
		});
		var member_dateCombo = Ext.ux.createDateCombo({
			width : 75,
			disabled : true,
			data : [[3, '近一个月'], [12, '近二个月'], [4, '近三个月'], [9, '近半年']],
			beginDate : member_beginDate,
			endDate : member_endDate
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
					id : 'memberType_combo_memberCond',
					width : 100,
					disabled : true,
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
				
				}),'->', {
					text : '批量发券',
					iconCls : 'btn_edit_all',
					handler : function(){
						var couponCm = new Ext.grid.ColumnModel([
	                      	new Ext.grid.CheckboxSelectionModel(),
						    {header : '编号', dataIndex : 'coupon.id', hidden :true},
						    {header : '名称', dataIndex : 'coupon.name'}
					    ]);
						var couponDs = new Ext.data.Store({
							proxy : new Ext.data.HttpProxy({url : '../../OperatePromotion.do'}),
							reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root'},[
      						     {name : 'coupon.id'},
      						     {name : 'coupon.name'}
      						])
						});
						couponDs.baseParams = {
								dataSource : 'getByCond',
								status : 'progress'
						};
						couponDs.load();
						
						var selectCouponGrid = new Ext.grid.GridPanel({
							id : 'selectCoupon',
							border : true,
							height : 300,
							frame : true,
							store : couponDs,
							cm : couponCm,
							sm : new Ext.grid.CheckboxSelectionModel(),
							viewConfig : {
								forceFit : true
							}
						});
						
						var issueCouponWin = new Ext.Window({
							title : '选择发放优惠券',
							modal : true,
							layout : 'fit',
							width : 300,
							height : 500,
							closeAction : 'hide',
							items : [selectCouponGrid],
							buttons : [{
								text : '确定',
								handler : function(){
									var couponId = [];
									var couponName = [];
									var selected = Ext.getCmp('selectCoupon').getSelectionModel().getSelections();
									for(var i = 0; i < selected.length; i++){
										couponId.push(selected[i].id + ',1');
										couponName.push(selected[i].json.coupon.name);
									}
									
									var store = memberCondBasicGrid.getStore();
									var amount = store.getCount();
									if(selected.length != 0){
										Ext.MessageBox.confirm('警示框', '您共发送【'+ amount + '张优惠券】，优惠券类型是:【'+ couponName.join(',') 
												+'】，发送的会员类型是 :【' + clickTree + '】', function(btn){
													if(btn == 'yes'){
														Ext.Ajax.request({
															url : '../../OperateCoupon.do',
															params : {
																issueMode : 3,
																dataSource : 'issue',
																promotions : couponId.join(';'),
																condId : clickTreeId
															},
															success : function(response){
																issueCouponWin.close();
															}
														});
														
													}
										});
									}else{
										Ext.example.msg('提示', '请选择优惠券!');
									}
									
								}
							}]
						});
						issueCouponWin.show();
					}
				},{ 
					text : '导出',
					iconCls : 'icon_tb_exoprt_excel',
					handler : function(e){
						var url = '../../{0}?memberType={1}&MinTotalMemberCost={2}&MaxTotalMemberCost={3}&consumptionMinAmount={4}&consumptionMaxAmount={5}'+
								'&memberMinBalance={6}&memberMaxBalance={7}&dateBegin={8}&dateEnd={9}&dataSource={10}';
						url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							Ext.getCmp('memberType_combo_memberCond').getValue(),
							Ext.getCmp('minCost_numField_memberCond').getValue(),
							Ext.getCmp('maxCost_numField_memberCond').getValue(),
							Ext.getCmp('minAmount_numField_memberCond').getValue(),
							Ext.getCmp('maxAmount4CondBar').getValue(),
							Ext.getCmp('minBalance_numField_memberCond').getValue(),
							Ext.getCmp('maxBalance_numField_memberCond').getValue(),
							Ext.util.Format.date(Ext.getCmp('srchBegin_dateField_memberCond').getValue(), 'Y-m-d 00:00:00'),
							Ext.util.Format.date(Ext.getCmp('srchEnd_dateField_memberCond').getValue(), 'Y-m-d 23:59:59'),
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
					disabled : true,
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
							Ext.getCmp('minCost_numField_memberCond').setValue();
							Ext.getCmp('maxCost_numField_memberCond').setValue();
							refreshConsumeCost();
						}
					}
				},
				{
					xtype : 'numberfield',
					id : 'minCost_numField_memberCond',
					disabled : true,
					width : 60
				},
				{
					xtype : 'tbtext',
					id : 'betweenCost_numField_memberCond',
					text : '&nbsp;-&nbsp;',
					hidden : true
				},			
				{
					xtype : 'numberfield',
					id : 'maxCost_numField_memberCond',
					disabled : true,
					width : 60,
					hidden : true
				},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
				{xtype : 'tbtext', text : '消费次数:'},
				{
					id : 'consumptionAmount_combo_memberCond',
					xtype : 'combo',
					disabled : true,
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
							Ext.getCmp('minAmount_numField_memberCond').setValue();
							Ext.getCmp('maxAmount4CondBar').setValue();
							refreshConsumeAmount();
						}
					}
				},
				{
					xtype : 'numberfield',
					id : 'minAmount_numField_memberCond',
					disabled : true,
					width : 50
				},
				{
					id : 'betweenAmount_text_memberCond',
					xtype : 'tbtext',
					text : '&nbsp;-&nbsp;'
				},			
				{
					xtype : 'numberfield',
					id : 'maxAmount4CondBar',
					disabled : true,
					width : 50
				},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'},	
				{xtype : 'tbtext', text : '余额:'},
				{
					id : 'comboBalance4CondBar',
					xtype : 'combo',
					disabled : true,
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
							Ext.getCmp('minBalance_numField_memberCond').setValue();
							Ext.getCmp('maxBalance_numField_memberCond').setValue();
							refreshBalance();
						}
					}
				},
				{
					xtype : 'numberfield',
					id : 'minBalance_numField_memberCond',
					disabled : true,
					width : 50
				},
				{
					id : 'betweenBalance_text_memberCond',
					xtype : 'tbtext',
					text : '&nbsp;-&nbsp;'
				},			
				{
					xtype : 'numberfield',
					id : 'maxBalance_numField_memberCond',
					disabled : true,
					width : 50
				}]
			
		});	
		
		
		
		var memberCond3ndTBar = new Ext.Toolbar({
			height : 28,		
			items : [
					{
						xtype : 'tbtext',
						text : String.format(Ext.ux.txtFormat.longerTypeName, '绑定手机', 'isBind_tbtext_memberCond', '----')
					},
					{
						xtype : 'tbtext',
						text : String.format(Ext.ux.txtFormat.longerTypeName, '年龄段', 'memeberAge_tbtext_memberCond', '----')
					},
					{
						xtype : 'tbtext',
						text : String.format(Ext.ux.txtFormat.longerTypeName, '性别', 'memeberSex_tbtext_memberCond', '----')
					},
					{xtype : 'tbtext', text : '充值额:'},
					{
						id : 'comboCharge4CondBar_combo_memeberCond',
						xtype : 'combo',
						disabled : true,
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
								Ext.getCmp('minCharge_numField_memberCond').setValue();
								Ext.getCmp('maxCharge_numField_memberCond').setValue();
								refreshBalance();
							}
						}
					},
					{
						xtype : 'numberfield',
						id : 'minCharge_numField_memberCond',
						disabled : true,
						width : 50
					},
					{
						id : 'betweenCharge_text_memberCond',
						xtype : 'tbtext',
						text : '&nbsp;-&nbsp;'
					},			
					{
						xtype : 'numberfield',
						id : 'maxCharge_numField_memberCond',
						disabled : true,
						width : 50
					},
					{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
					{
						xtype : 'label',
						text : '粉丝数:'
					},
					{	//TODO
						xtype : 'combo',
						store : new Ext.data.SimpleStore({
							fields : ['value', 'text'],
							data : [['max', '小于'], ['min', '大于'], ['between', '介于'], ['null', '无设定']]
						}),
						valueField : 'value',
						displayField : 'text',
						id : 'showFansAmountConfig_combo_memberCond',
						disabled : true,
						typeAhead : true,
						mode : 'local',
						triggerAction : 'all',
						width: 80
					},
					{
						id : 'showFansAmountMin_numberfield_memberCond',
						xtype : 'numberfield',
						width: 50,
						disabled : true
					},{
						xtype : 'label',
						text : '-'
					},{
						id : 'showFansAmountMax_numberfield_memberCond',
						xtype : 'numberfield',
						width : 50,
						disabled : true
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
				['年龄段','ageText'],
				['性别','sexText'],
				['粉丝数', 'fansAmount'],
				['创建时间','createDateFormat'],
				['消费次数', 'consumptionAmount',,'right', 'Ext.ux.txtFormat.gridDou'],
				['最近消费','lastConsumption',150],
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
			[memberCond1stTBar, memberCond2ndTBar,memberCond3ndTBar]
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
		//修改时候显示会员原来设置的条件
		Ext.getCmp('condId_hidden_memberCond').setValue(data.id);
		Ext.getCmp('txtMemberCondName').setValue(data.name);
		Ext.getCmp('memberCondByType').setValue(data.memberType ? data.memberType : -1);
		Ext.getCmp('minCost4CondWin').setValue(data.minConsumeMoney && data.minConsumeMoney > 0 ? data.minConsumeMoney : "");
		Ext.getCmp('maxCost4CondWin').setValue(data.maxConsumeMoney && data.maxConsumeMoney > 0 ? data.maxConsumeMoney : "");
		Ext.getCmp('minAmount4CondWin').setValue(data.minConsumeAmount && data.minConsumeAmount > 0 ? data.minConsumeAmount : "");
		Ext.getCmp('maxAmount4CondWin').setValue(data.maxConsumeAmount && data.maxConsumeAmount > 0 ? data.maxConsumeAmount : "");
		Ext.getCmp('minBalance4CondWin').setValue(data.minBalance && data.minBalance > 0 ? data.minBalance : "");
		Ext.getCmp('maxBalance4CondWin').setValue(data.maxBalance && data.maxBalance > 0 ? data.maxBalance : "");
		
		//根据数据设置选择条件
		if(data.maxFansAmount > 0 && data.minFansAmount > 0){
			Ext.getCmp('fansAmount_combo_memberCond').setValue('between');
		}else if(data.maxFansAmount > 0 && data.minFansAmount <= 0){
			Ext.getCmp('fansAmount_combo_memberCond').setValue('max');
		}else if(data.maxFansAmount <= 0 && data.minFansAmount > 0){
			Ext.getCmp('fansAmount_combo_memberCond').setValue('min');
		}else{
			Ext.getCmp('fansAmount_combo_memberCond').setValue('null');
		}
		Ext.getCmp('fansAmount_combo_memberCond').fireEvent('select');
		Ext.getCmp('fansAmountMin_numberField_memberCond').setValue(data.minFansAmount > 0 ? data.minFansAmount : '');
		Ext.getCmp('fansAmountMax_numberField_memberCond').setValue(data.maxFansAmount > 0 ? data.maxFansAmount : '');
		
		if(data.rangeType == null){
			Ext.getCmp('memberCondDateRegion').setValue(null);
		}else{
			Ext.getCmp('memberCondDateRegion').setValue(data.rangeType);
		}
		
		
		if(typeof data.sex == 'undefined'){
			Ext.getCmp('memberCondSex_combo_memebercond').setValue(-1);
		}else{
			Ext.getCmp('memberCondSex_combo_memebercond').setValue(data.sex);
		}
		
		if(typeof data.age == 'undefined'){
			Ext.getCmp('memberCondAge_combo_memebercond').setValue(-1);
		}else{
			Ext.getCmp('memberCondAge_combo_memebercond').setValue(data.age);
		}	
		
		Ext.getCmp('memberMinCharge_numberField_memberCond').setValue(data.minCharge && data.minCharge > 0 ? data.minCharge : "");
		Ext.getCmp('memberMaxCharge_numberField_memberCond').setValue(data.maxCharge && data.maxCharge > 0 ? data.maxCharge : "");
		if(typeof data.isRaw == 'undefined'){
			Ext.getCmp('isBind_checkbox_memberCond').setValue(-1);
		}else{
			if(data.isRaw){
				Ext.getCmp('isBind_checkbox_memberCond').setValue(1);
			}else{
				Ext.getCmp('isBind_checkbox_memberCond').setValue(0);
			}
		}
			
		if(data.name){
			//修改
			if(data.minConsumeMoney > 0 && data.maxConsumeMoney == 0){
				//消费金额大于
				Ext.getCmp('comboCost4CondWin').setValue(1);
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
			
			if(data.minCharge > 0 && data.maxCharge == 0){
				Ext.getCmp('memberCharge_combo_memberCond').setValue(1);
			}else if(data.minCharge == 0 && data.maxCharge > 0){
				Ext.getCmp('memberCharge_combo_memberCond').setValue(2);
			}else if(data.minCharge > 0 && data.maxCharge > 0){
				Ext.getCmp('memberCharge_combo_memberCond').setValue(3);
			}else{
				Ext.getCmp('memberCharge_combo_memberCond').setValue(0);
			}
			refreshCharge();
			
			if(data.minConsumeAmount > 0 && data.maxConsumeAmount == 0){
				//消费次数大于
				Ext.getCmp('comboAmount4CondWin').setValue(1);
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
			Ext.getCmp('isBind_checkbox_memberCond').setValue(-1);
			Ext.getCmp('isBind_checkbox_memberCond').fireEvent('select', null, null, -1);
			
			//新增
			Ext.getCmp('comboCost4CondWin').setValue(0);
			Ext.getCmp('comboCost4CondWin').fireEvent('select', null, null, 0);
			
			Ext.getCmp('memberCharge_combo_memberCond').setValue(0);
			Ext.getCmp('memberCharge_combo_memberCond').fireEvent('select', null, null, 0);
			
			Ext.getCmp('memberCondSex_combo_memebercond').setValue(-1);
			Ext.getCmp('memberCondSex_combo_memebercond').fireEvent('select', null, null, -1);
			
			Ext.getCmp('memberCondAge_combo_memebercond').setValue(-1);
			Ext.getCmp('memberCondAge_combo_memebercond').fireEvent('select', null, null, -1);
			
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
			id : 'condId_hidden_memberCond'
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
		} ,comboMemberType4CondWin,{
			xtype : 'label',
			columnWidth : 0.2,
			text : '',
			width : 10,
			hidden : true
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '绑定手机:'
		}, {
			columnWidth : 0.2,
			id : 'isBind_checkbox_memberCond',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : -1,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[-1, '无设定'], [0, '已绑定'], [1, '未绑定']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '充值金额:'
		}, {
			columnWidth : 0.2,
			id : 'memberCharge_combo_memberCond',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : 0,
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
					Ext.getCmp('memberMinCharge_numberField_memberCond').setValue();
					Ext.getCmp('memberMaxCharge_numberField_memberCond').setValue();
					refreshCharge();	
				}
			}
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'memberMinCharge_numberField_memberCond'
		},{
			xtype : 'label',
			id : 'memberChargeWith_label_memberCond',
			text : ' ~ ',
			hidden : true
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'memberMaxCharge_numberField_memberCond'
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false
		},{
			columnWidth : 0.2,
			xtype : 'label',
			text : '消费金额:'
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
			id : 'minCost4CondWin'
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
			text : '粉丝数:'
		},{
			columnWidth : 0.2,
			id : 'fansAmount_combo_memberCond',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				render : function(thiz){
					thiz.store.loadData([['min', '大于'], ['max', '小于'], ['between', '介于'], ['null', '无设定']]);
					thiz.setValue('null');
					thiz.fireEvent('select');
				},
				select : function(){
					if(Ext.getCmp('fansAmount_combo_memberCond').getValue() == 'null'){
						Ext.getCmp('fansAmountMin_numberField_memberCond').show();
						Ext.getCmp('fansAmountMax_numberField_memberCond').show();
						Ext.getCmp('fansAmountMin_numberField_memberCond').disable();
						Ext.getCmp('fansAmountMax_numberField_memberCond').disable();
						Ext.getCmp('fansAmount_label_memberCond').show();
					}else if(Ext.getCmp('fansAmount_combo_memberCond').getValue() == 'min'){
						Ext.getCmp('fansAmountMin_numberField_memberCond').enable();
						Ext.getCmp('fansAmountMin_numberField_memberCond').show();
						Ext.getCmp('fansAmountMax_numberField_memberCond').hide();
						Ext.getCmp('fansAmount_label_memberCond').hide();
					}else if(Ext.getCmp('fansAmount_combo_memberCond').getValue() == 'max'){
						Ext.getCmp('fansAmountMax_numberField_memberCond').enable();
						Ext.getCmp('fansAmountMin_numberField_memberCond').hide();
						Ext.getCmp('fansAmountMax_numberField_memberCond').show();
						Ext.getCmp('fansAmount_label_memberCond').hide();
					}else{
						Ext.getCmp('fansAmountMin_numberField_memberCond').enable();
						Ext.getCmp('fansAmountMax_numberField_memberCond').enable();
						Ext.getCmp('fansAmountMin_numberField_memberCond').show();
						Ext.getCmp('fansAmountMax_numberField_memberCond').show();
						Ext.getCmp('fansAmount_label_memberCond').show();
					}
					
					Ext.getCmp('fansAmountMin_numberField_memberCond').setValue('');
					Ext.getCmp('fansAmountMax_numberField_memberCond').setValue('');
					
				}
			}
		},{
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'fansAmountMin_numberField_memberCond'
		},{
			xtype : 'label',
			text : '~',
			id : 'fansAmount_label_memberCond'
		},{
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'fansAmountMax_numberField_memberCond'
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
			id : 'minAmount4CondWin'
		},{
			id : 'betweenAmount4CondWin',
			xtype : 'label',
			text : ' ~ '
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'maxAmount4CondWin'
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
			id : 'minBalance4CondWin'
		},{
			id : 'betweenBalance4CondWin',
			xtype : 'label',
			text : ' ~ '
		}, {
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'maxBalance4CondWin'
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
			value : null,
			width : 80, 
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[null, '无设定'], [1, '近一个月'], [2, '近二个月'], [3, '近三个月'], [4, '自定义']]
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
			readOnly : false			
		},{
			columnWidth : 0.3,
			xtype : 'datefield',	
			id : 'memberCondEndDate',
			format : 'Y-m-d',
			width : 100,
			readOnly : false			
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false		
		},{
			columnWidth : 0.3,
			xtype : 'label',
			text : '距离最近消费天数:'
		},{
			columnWidth : 0.2,
			id : 'costDay_combo_memberCond',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			width : 80,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				render : function(thiz){
					thiz.store.loadData([['null', '无设定'], ['max', '大于'], ['min', '小于']]);
					thiz.setValue('null');
					if(Ext.getCmp('costDay_combo_memberCond').getValue() == 'null'){
						Ext.getCmp('costDay_numberField_memeberCond').disable();
					}else{
						Ext.getCmp('costDay_numberField_memeberCond').enable();
					}
				},
				select : function(){
					if(Ext.getCmp('costDay_combo_memberCond').getValue() == 'null'){
						Ext.getCmp('costDay_numberField_memeberCond').disable();
					}else{
						Ext.getCmp('costDay_numberField_memeberCond').enable();
					}
					Ext.getCmp('costDay_numberField_memeberCond').setValue('');
					
				}
			}
		},{
			columnWidth : 0.3,
			xtype : 'numberfield',
			id : 'costDay_numberField_memeberCond'
		},{
			columnWidth : 0.1,
			xtype : 'label',
			text : '天'
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false		
		},{
			columnWidth : 0.3,
			xtype : 'label',
			text : '性别:'
		},{
			columnWidth : 0.2,
			id : 'memberCondSex_combo_memebercond',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : -1,
			width : 80, 
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[-1, '无设定'], [0, '男'], [1, '女']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true
		},{
			columnWidth : 1,
			style :'margin-bottom:5px;',
			border : false		
		},{
			columnWidth : 0.3,
			xtype : 'label',
			text : '年龄段:'
		},{
			columnWidth : 0.2,
			id : 'memberCondAge_combo_memebercond',
			xtype : 'combo',
			readOnly : false,
			forceSelection : true,
			value : -1,
			width : 80, 
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text'],
				data : [[-1, '无设定'], [5, '00后'], [4, '90后'],[3, '80后'], [2, '70后'],[1, '60后'],[6, '50后']]
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true
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
				var id = Ext.getCmp('condId_hidden_memberCond').getValue();
				var name = Ext.getCmp('txtMemberCondName').getValue();
				var memberType = Ext.getCmp('memberCondByType').getValue();
				
				//充值金额
				var memberChargeType = Ext.getCmp('memberCharge_combo_memberCond').getValue();
				var memberChargemMin = Ext.getCmp('memberMinCharge_numberField_memberCond').getValue();
				var memberChargeMax = Ext.getCmp('memberMaxCharge_numberField_memberCond').getValue();
				var minCharge;
				var maxCharge;
				if(memberChargeType == 1){//大于
					minCharge = memberChargemMin;
					maxCharge = 0;
				}else if(memberChargeType == 2){//小于
					minCharge = 0;
					maxCharge = memberChargeMax;
				}else if(memberChargeType == 3){//介于
					minCharge = memberChargemMin;
					maxCharge = memberChargeMax;
				}else{//无限定
					minCharge = 0;
					maxCharge = 0;
				}
				
				
				
				var minCost4CondWin = Ext.getCmp('minCost4CondWin').getValue();
				var maxCost4CondWin = Ext.getCmp('maxCost4CondWin').getValue();
				var cost4CondWinType = Ext.getCmp('comboCost4CondWin').getValue();
				var minCost;
				var maxCost;
				if(cost4CondWinType == 1){//大于
					minCost = minCost4CondWin;
					maxCost = 0;
				}else if(cost4CondWinType == 2){//小于
					minCost = 0;
					maxCost = maxCost4CondWin;
				}else if(cost4CondWinType == 3){//介于
					minCost = minCost4CondWin;
					maxCost = maxCost4CondWin;
				}else{//无限定
					minCost = 0;
					maxCost = 0;
				}
				
				var minAmount4CondWin = Ext.getCmp('minAmount4CondWin').getValue();
				var maxAmount4CondWin = Ext.getCmp('maxAmount4CondWin').getValue();
				var	amount4CondWinType = Ext.getCmp('comboAmount4CondWin').getValue();
				var minAmount;
				var maxAmount;
				if(amount4CondWinType == 1){//大于
					minAmount = minAmount4CondWin;
					maxAmount = 0;
					
				}else if(amount4CondWinType == 2){//小于
					minAmount = 0;
					maxAmount = maxAmount4CondWin;
					
				}else if(amount4CondWinType == 3){//介于
					minAmount = minAmount4CondWin;
					maxAmount = maxAmount4CondWin;
					
				}else{//无限定
					minAmount = 0;
					maxAmount = 0;
				}
				
				//粉丝数设定
				var setFansConfig = Ext.getCmp('fansAmount_combo_memberCond').getValue();
				var minFansAmount = Ext.getCmp('fansAmountMin_numberField_memberCond').getValue();
				var maxFansAmount = Ext.getCmp('fansAmountMax_numberField_memberCond').getValue();
				var fansMinData;
				var fansMaxData;
				if(setFansConfig == 'min'){
					fansMinData = minFansAmount;
					fansMaxData = 0;
				}else if(setFansConfig == 'max'){
					fansMinData = 0;
					fansMaxData = maxFansAmount;
				}else if(setFansConfig == 'between'){
					fansMinData = minFansAmount;
					fansMaxData = maxFansAmount;
				}
				
				
				var minBalance4CondWin = Ext.getCmp('minBalance4CondWin').getValue();
				var maxBalance4CondWin = Ext.getCmp('maxBalance4CondWin').getValue();
				var balance4CondWinType = Ext.getCmp('comboBalance4CondWin').getValue();
				var minBalance;
				var maxBalance;
				if(balance4CondWinType == 1){//大于
					minBalance = minBalance4CondWin;
					maxBalance = 0;
					
				}else if(balance4CondWinType == 2){//小于
					minBalance = 0;
					maxBalance = maxBalance4CondWin;
					
				}else if(balance4CondWinType == 3){//介于
					minBalance = minBalance4CondWin;
					maxBalance = maxBalance4CondWin;
					
				}else{//无限定
					minBalance = 0;
					maxBalance = 0;
				}
				
				var memberCondDateRegion = Ext.getCmp('memberCondDateRegion').getValue();
				var memberCondBeginDate = Ext.getCmp('memberCondBeginDate').getValue();
				var memberCondEndDate = Ext.getCmp('memberCondEndDate').getValue();
				//距离最近消费天数
				var costDayType = Ext.getCmp('costDay_combo_memberCond').getValue();
				var costDay = Ext.getCmp('costDay_numberField_memeberCond').getValue();
				var maxDay;
				var minDay;
				if(costDayType == 'max'){
					minDay = costDay;
					maxDay = 0;
					
				}else if(costDayType == 'min'){
					minDay = 0;
					maxDay = costDay;
					
				}else{
					minDay = 0;
					maxDay = 0;
				}
				var sex = Ext.getCmp('memberCondSex_combo_memebercond').getValue();
				var age = Ext.getCmp('memberCondAge_combo_memebercond').getValue();
				var bindValve = Ext.getCmp('isBind_checkbox_memberCond').getValue();
				var isRaw;
				if(bindValve == '0'){
					isRaw = false;
				}else if(bindValve == '1'){
					isRaw = true;
				}else{
					isRaw = bindValve;
				}
				
				Ext.Ajax.request({
					url : '../../OperateMemberCond.do',
					params : {
						dataSource : memberCondWin.operationType,
						id : id,
						name : name,
						memberType : memberType,
						memberCondMinConsume : minCost,
						memberCondMaxConsume : maxCost,
						memberCondMinAmount : minAmount,
						memberCondMaxAmount : maxAmount,
						memberCondMinBalance : minBalance,
						memberCondMaxBalance : maxBalance,
						memberCondDateRegion : memberCondDateRegion,
						memberCondBeginDate : memberCondBeginDate,
						memberCondEndDate : memberCondEndDate,
						minLastConsumption : minDay,
						maxLastConsumption : maxDay,
						sex : sex,
						age : age,
						memberCondMinCharge : minCharge,
						memberCondMaxCharge : maxCharge,
						isRaw : isRaw,
						minFansAmount : fansMinData,
						maxFansAmount : fansMaxData
					},
					success : function(response, options) {
						var jr = Ext.util.JSON.decode(response.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							memberCondWin.hide();
							memberCondTree.getRootNode().reload();
							
							setTimeout(function(){
	 							for(var i = 0; i < memberCondTree.getRootNode().childNodes.length; i++){
									var node = memberCondTree.getRootNode().childNodes[i];
									if(node.id == id){
										node.select();
										node.fireEvent('click', node);
										break;
									}
								}
							}, 500);
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
	
	(function init(){
		memberCondTreeInit();
		memberCondGridInit();
		
		var memberPromotionOperationPanel = new Ext.Panel({
			layout : 'border',
			width : 240,
			frame : false,
			region : 'west',
			items : [memberCondTree]
		});
	
		new Ext.Panel({
			renderTo : 'body_div_memberCond',
			height : parseInt(Ext.getDom('body_div_memberCond').parentElement.style.height.replace(/px/g,'')),
			layout : 'border',
			items : [memberPromotionOperationPanel, memberCondBasicGrid],
			autoScroll : true
		});
	 
		showFloatOption({ treeId : 'condition_tree_memberCond', option : [{name : '修改', fn : updateMemberCond}, {name : '删除', fn : deleteMemberCond }] });
	})();

	

});
