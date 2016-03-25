var history_hours;
Ext.onReady(function() {
	
	//历史账单
	var searchAdditionFilter = 'searchAdditionFilterAll';
	
	var billsGrid;
	var foodStatus;
	var historyExtraBar;
	var historyPayTypes;
	var billDetailWin, viewBillWin;
	
	//------------------lib
	function billQueryHandler(c) {
		c = c || {};
		var gs = billsGrid.getStore();
		var params = {
				start : 0,
				limit : GRID_PADDING_LIMIT_20
		};
		gs.baseParams['dateBeg'] = Ext.util.Format.date(Ext.getCmp('dateSearchDateBegin').getValue(), 'Y-m-d 00:00:00');
		gs.baseParams['dateEnd'] = Ext.util.Format.date(Ext.getCmp('dateSearchDateEnd').getValue(), 'Y-m-d 23:59:59');
		gs.baseParams['comboPayType'] = Ext.getCmp('comboPayType').getValue();
		gs.baseParams['common'] = Ext.getCmp('textSearchValue').getValue();
		if(c.isRange){
			gs.baseParams['isRange'] = true;
		}
		if(isNaN(Ext.getCmp('textTableAliasOrName').getValue())){
			gs.baseParams['tableName'] = Ext.getCmp('textTableAliasOrName').getValue();
			gs.baseParams['tableAlias'] = '';
		}else{
			gs.baseParams['tableAlias'] = Ext.getCmp('textTableAliasOrName').getValue();
			gs.baseParams['tableName'] = '';
		}
		gs.baseParams['region'] = Ext.getCmp('history_comboRegion').getValue();
		gs.baseParams['staffId'] = Ext.getCmp('historyBill_combo_staffs').getValue();
		gs.baseParams['branchId'] = branch_combo_history.getValue();
		var businessHour;
		
		if(history_hours){
			businessHour = history_hours;
		}else{
			businessHour = history_oBusinessHourData({type : 'get'}).data;
		}
		
		if(parseInt(businessHour.businessHourType) != -1){
			gs.baseParams['opening'] = businessHour.opening;
			gs.baseParams['ending'] = businessHour.ending;
		}else{
			gs.baseParams['opening'] = '';
		}
		gs.baseParams['orderId'] = Ext.getCmp('numberSearchValue').getValue();
		
		gs.baseParams['seqId'] = Ext.getCmp('numberSearchSeqIdValue').getValue();
		
		sAdditionFilter = Ext.getCmp(searchAdditionFilter).inputValue;	
		
		gs.baseParams['dataType'] = 1;
	
		gs.baseParams['havingCond'] = sAdditionFilter;
		gs.load({
			params : params
		});
	};
	
	function billQueryExportHandler() {
		var url;
		sAdditionFilter = Ext.getCmp(searchAdditionFilter).inputValue;
		var businessHour = history_oBusinessHourData({type : 'get'}).data;
		var opening, ending;
		if(parseInt(businessHour.businessHourType) != -1){
			opening = businessHour.opening;
			ending = businessHour.ending;
		}else{
			opening = '';
			ending = '';
		}
		url = '../../{0}?dateBeg={1}&dateEnd={2}&comboPayType={3}&common={4}&orderId={5}&tableName={6}&tableAlias={7}&region={8}&havingCond={9}&dataSource={10}&dataType={11}&seqId={12}&opening={13}&ending={14}&branchId={15}';
		url = String.format(
			url, 
			'ExportHistoryStatisticsToExecl.do', 
			Ext.util.Format.date(Ext.getCmp('dateSearchDateBegin').getValue(), 'Y-m-d 00:00:00'), 
			Ext.util.Format.date(Ext.getCmp('dateSearchDateEnd').getValue(), 'Y-m-d 23:59:59'),
			Ext.getCmp('comboPayType').getValue(),
			Ext.getCmp('textSearchValue').getValue(),
			Ext.getCmp('numberSearchValue').getValue(),
			Ext.getCmp('textTableAliasOrName').getValue(),
			Ext.getCmp('textTableAliasOrName').getValue(),
			Ext.getCmp('history_comboRegion').getValue(),
			sAdditionFilter,
			'historyOrder',
			1,
			Ext.getCmp('numberSearchSeqIdValue').getValue(),
			opening,
			ending,
			branch_combo_history.getValue()
		);
		window.location = url;
	};
	
	//----------------------load
	function loadAddKitchens() {
		kitchenMultSelectData = [];
		Ext.Ajax.request({
			url : "../../QueryKitchen.do",
			params : {
				"dataSource" : "normal",
				
				"isPaging" : false
			},
			success : function(response, options) {
				var resultJSON = Ext.util.JSON.decode(response.responseText);
				var rootData = resultJSON.root;
				for ( var i = 0; i < rootData.length; i++) {
					kitchenMultSelectData.push([
					    rootData[i].kitchenAlias,
						rootData[i].kitchenName, 
						rootData[i].kitchenID 
					]);
				}
			},
			failure : function(response, options) {
				Ext.MessageBox.show({
					msg : " Unknown page error ",
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		});
	}
	
	// on page load function
	function billHistoryOnLoad() {
		// data init
		loadAddKitchens();
	};
	//-----------
	/* ---------------------------------------------------------------- */
	
	var regionStatBut = new Ext.ux.ImageButton({
		imgPath : "../../images/regionStatis.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "区域统计",
		handler : function(btn) {
			regionStatWin.show();
		}
	});
	
	var discountStatBut = new Ext.ux.ImageButton({
		imgPath : "../../images/discountStatis.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "折扣统计",
		handler : function(btn) {
			discountStatWin.show();
		}
	});
	
	var shiftStatBut = new Ext.ux.ImageButton({
		imgPath : "../../images/shiftStatis.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "交班记录",
		handler : function(btn) {
			dutyRangeStat({statType : 1});
		}
	});
	
	var paymentBut = new Ext.ux.ImageButton({
		imgPath : "../../images/paymentRecord.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "交款记录",
		handler : function(btn) {
			dutyRangeStat({statType : 2});
		}
	});
	
	var dailySettleStatBut = new Ext.ux.ImageButton({
		imgPath : "../../images/dailySettleStatis.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "日结记录",
		handler : function(btn) {
			dailySettleStat();
		}
	});
	
	
	var logOutBut = new Ext.ux.ImageButton({
		imgPath : "../../images/ResLogout.png",
		imgWidth : 50,
		imgHeight : 50,
		tooltip : "登出",
		handler : function(btn) {
			
		}
	});
	
	function showViewBillWin(){
		viewBillWin = new Ext.Window({
			id : 'history_viewBillWin',
			layout : 'fit',
			title : '查看账单',
			width : 510,
			height : 550,
			resizable : false,
			closable : false,
			modal : true,
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function() {
					viewBillWin.destroy();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					viewBillWin.destroy();
				}
			}],
			listeners : {
				show : function(thiz) {
					var sd = Ext.ux.getSelData(billsGrid);
					thiz.load({
						url : '../window/history/viewBillDetail.jsp', 
						scripts : true,
						method : 'post'
					});
					thiz.center();	
					
					thiz.orderId = sd.id;
					thiz.branchId = Ext.getCmp('branch_combo_history').getValue();
				}
			}
		});
	}
	
	function billViewHandler() {
		showViewBillWin();
		viewBillWin.show();
		viewBillWin.center();
	};
	
	function detailIsPaidRenderer(v){
		return eval(v) ? '是' : '否';
	}
	
	var billgodtpStatus = false;
	var billGroupOrderDetailTabPanel = new Ext.TabPanel({
		border : false,
		enableTabScroll : true,
		listeners : {
			tabchange : function(thiz, stab){
				if(billgodtpStatus && thiz.getActiveTab().getId() == stab.getId()){
					stab.getStore().load({
						params : {
							start : 0,
							limit : 10
						}
					});				
				}
			}
		}
	});
	
	function showBillDetailWin(){
		billDetailWin = new Ext.Window({
			layout : 'fit',
			width : 1100,
			height : 440,
			closable : false,
			resizable : false,
			modal : true,
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function() {
					billDetailWin.destroy();
				}
			} ],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					billDetailWin.destroy();
				}
			}],
			listeners : {
				show : function(thiz) {
					var sd = Ext.ux.getSelData(billsGrid);
					thiz.load({
						url : '../window/history/orderDetail.jsp', 
						scripts : true,
						method : 'post'
					});
					thiz.center();	
					
					thiz.orderId = sd.id;
					thiz.foodStatus = foodStatus;
					thiz.branchId = Ext.getCmp('branch_combo_history').getValue();
				}
			}
		});
	}
	
	
	
	function billDetailHandler(orderID) {
		showBillDetailWin();
		billDetailWin.show();
		billDetailWin.setTitle('账单号: ' + orderID);
		billDetailWin.center();
	};
	
	// 打印link
	function printBillFunc(rowInd) {
		var tempMask = new Ext.LoadMask(document.body, {
			msg : '正在打印请稍候.......',
			remove : true
		});
		tempMask.show();
		Ext.Ajax.request({
			url : "../../PrintOrder.do",
			params : {
				
				"orderID" : billsGrid.getStore().getAt(rowInd).get("id"),
				'printType' : 3
			},
			success : function(response, options) {
				tempMask.hide();
				Ext.ux.showMsg(Ext.decode(response.responseText));
			},
			failure : function(response, options) {
				tempMask.hide();
				Ext.ux.showMsg(Ext.decode(response.responseText));
			}
		});
	};
	
	function history_oBusinessHourData(c){
		if(c == null || c.type == null || typeof c.type == 'undefined')
			return;
		var data = {};
		var apmBegin = Ext.getCmp('history_comboBusinessBeginApm');
		var openingHour = Ext.getCmp('history_comboBegionHour');
		var openingMin = Ext.getCmp('history_comboBegionMin');
		var txtBusinessHourBegin = Ext.getCmp('txtBusinessHourBegin');
		
		var apmEnd = Ext.getCmp('history_comboBusinessEndApm');
		var endingHour = Ext.getCmp('history_comboEndHour');
		var endingMin = Ext.getCmp('history_comboEndMin');
		var txtBusinessHourEnd = Ext.getCmp('txtBusinessHourEnd');
		
		
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
			Ext.getCmp('txtBusinessHourBeginText').hide();
			Ext.getCmp('txtBusinessHourEndText').hide();
			Ext.getCmp('txtBusinessMinBeginText').hide();
			Ext.getCmp('txtBusinessMinEndText').hide();		
			
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
					Ext.getCmp('txtBusinessHourBeginText').show();
					Ext.getCmp('txtBusinessHourEndText').show();
					Ext.getCmp('txtBusinessMinBeginText').show();
					Ext.getCmp('txtBusinessMinEndText').show();
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
			
			data.businessHourType = Ext.getCmp('history_comboBusinessHour').getValue();
			
			c.data = data;
		}
		return c;
	};
	
	var historyBill_combo_staffs = new Ext.form.ComboBox({
		id : 'historyBill_combo_staffs',
		readOnly : false,
		forceSelection : true,
		width : 100,
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
				var staffData = [[-1,'全部']];
				Ext.Ajax.request({
					url : '../../QueryStaff.do',
					params : {privileges : 1005},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							staffData.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
						}
						thiz.store.loadData(staffData);
						thiz.setValue(-1);
					},
					fialure : function(res, opt){
						thiz.store.loadData(staffData);
						thiz.setValue(-1);
					}
				});
			},
			select : function(){
					Ext.getCmp('btnSreachForMainOrderGrid').handler();
				}
			}
	});
	

	var history_beginDate = new Ext.form.DateField({
		xtype : 'datefield',	
		id : 'dateSearchDateBegin',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var history_endDate = new Ext.form.DateField({
		xtype : 'datefield',
		id : 'dateSearchDateEnd',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	var history_dateCombo = Ext.ux.createDateCombo({
		width : 90,
		beginDate : history_beginDate,
		endDate : history_endDate,
		callback : function(){
			Ext.getCmp('btnSreachForMainOrderGrid').handler();
		}
	});
	
	//门店选择
	var branch_combo_history = new Ext.form.ComboBox({
		id : 'branch_combo_history',
		readOnly : false,
		forceSelection : true,
		width : 123,
		listWidth : 120,
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
							data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
							 
							for(var i = 0; i < jr.root[0].branches.length; i++){
								data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
							}
						}
						
						thiz.store.loadData(data);
						thiz.setValue(jr.root[0].id);
						thiz.fireEvent('select');
					}
				});
			},
			select : function(isJump){
				//加载区域
				var region = [[-1, '全部']]
				Ext.Ajax.request({
					url : '../../OperateRegion.do',
					params : {
						dataSource : 'getByCond',
						branchId : branch_combo_history.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						for(var i = 0; i < jr.root.length; i++){
							region.push([jr.root[i]['id'], jr.root[i]['name']]);
						}
						
						Ext.getCmp('history_comboRegion').getStore().loadData(region);
						Ext.getCmp('history_comboRegion').setValue(-1);
					}
				});
				
				//加载操作人员
				var staff = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../QueryStaff.do',
					params : {
						branchId : branch_combo_history.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						for(var i = 0; i < jr.root.length; i++){
							staff.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
						}
						
						historyBill_combo_staffs.store.loadData(staff);
						historyBill_combo_staffs.setValue(-1);
					}
				})
				
				
				//加载收款方式
				var payType = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../OperatePayType.do',
					params : {
						dataSource : 'getByCond',
						branchId : branch_combo_history.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						jr.root.unshift({id:-1, name:'全部'});
						Ext.getCmp('comboPayType').getStore().loadData(jr.root);
						Ext.getCmp('comboPayType').setValue(-1);			
					}
				});
				
				//加载市别
				var hour = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../OperateBusinessHour.do',
					params : {
						dataSource : 'getByCond',
						branchId : branch_combo_history.getValue()
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						
						for(var i = 0; i < jr.root.length; i++){
							hour.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
						}
						
						hour.push([-2, '自定义']);
						
						Ext.getCmp('history_comboBusinessHour').getStore().loadData(hour);
						Ext.getCmp('history_comboBusinessHour').setValue(-1);
					}
				});
				if(!isJump){
					Ext.getCmp('btnSreachForMainOrderGrid').handler();	
				}
			}
		}
	});
	
	
	historyExtraBar = new Ext.Toolbar({
		id : 'historyExtraBar',
		height : 28,
		items : [{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '台名/台号:'},
			{
				xtype : 'textfield',
				id : 'textTableAliasOrName',
				hidden : false,
				width : 100
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '收款方式:'},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 70,
				id : 'comboPayType',
				store : new Ext.data.JsonStore({
					fields : [ 'id', 'name' ]
				}),
				valueField : 'id',
				displayField : 'name',
				value : -1,
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				readOnly : false,
				listeners : {
					render : function(thiz){
						$.ajax({
							url : '../../OperatePayType.do',
							type : 'post',
							dataType : 'json',
							data : {
								dataSource : 'getByCond'
							},
							async : false,
							success : function(jr){
								historyPayTypes = jr.root;
								jr.root.unshift({id:-1, name:'全部'});
								thiz.getStore().loadData(jr.root);
								thiz.setValue(-1);								
							}
						});
					},
					select : function(){
							Ext.getCmp('btnSreachForMainOrderGrid').handler();
						}
					}
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '操作人员:'},
			historyBill_combo_staffs,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '区域:'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 90,
				value : -1,
				id : 'history_comboRegion',
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
						
						$.ajax({
							url : '../../OperateRegion.do',
							type : 'post',
							dataType : 'json',
							async : false,
							data : {
								dataSource : 'getByCond'
							},
							success : function(jr){
								for(var i = 0; i < jr.root.length; i++){
									data.push([jr.root[i]['id'], jr.root[i]['name']]);
								}
								thiz.store.loadData(data);
								thiz.setValue(-1);								
							},
							error : function(){
								thiz.store.loadData(data);
								thiz.setValue(-1);								
							}
						});
						
					},
					select : function(){
						Ext.getCmp('btnSreachForMainOrderGrid').handler();
					}
				}
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '备注搜索:'},
			{
				xtype : 'textfield',
				id : 'textSearchValue',
				hidden : false,
				width : 100
			},{
				xtype : 'tbtext',
				text : '&nbsp;&nbsp;门店选择:'
			},
			branch_combo_history
		]
	});
	
	var historyHighTimeBar = new Ext.Toolbar({
		id : 'historyHighTimeBar',
		height : 28,
		items : [
			{xtype : 'tbtext', text : '&nbsp;&nbsp;&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '日期:&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			history_dateCombo,
			{xtype : 'tbtext', text : '&nbsp;'},
			history_beginDate,
			{
				xtype : 'label',
				hidden : false,
				id : 'tbtextDisplanZ',
				text : ' 至 '
			}, 
			history_endDate,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', text : '市别:'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 90,
				value : -1,
				id : 'history_comboBusinessHour',
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
						$.ajax({
							url : '../../OperateBusinessHour.do',
							data : {
								dataSource : 'getByCond'
							},
							type : 'post',
							dataType : 'json',
							async : false,
							success : function(jr){
								for(var i = 0; i < jr.root.length; i++){
									data.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
								}
								data.push([-2,'自定义']);
								thiz.store.loadData(data);
								thiz.setValue(-1);
							},
							error : function(){
								thiz.store.loadData(data);
								thiz.setValue(-1);								
							}
						});
						
					},
					select : function(thiz, record, index){
						history_oBusinessHourData({data : record.json, type : 'set'});
						Ext.getCmp('btnSreachForMainOrderGrid').handler();
					}
				}
			},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			{xtype : 'tbtext', id : 'txtBusinessHourBegin', text : '<font style="color:green; font-size:20px">00:00</font>'},
			{
		    		xtype : 'combo',
		    		width : 50,
		    		value : 0,
		    		id : 'history_comboBusinessBeginApm',
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
		    	
		    },
			{
				xtype : 'combo',
				forceSelection : true,
				width : 40,
				value : '00',
				id : 'history_comboBegionHour',
				hidden : true,
				store : new Ext.data.SimpleStore({
					fields : [ 'value', 'text' ],
					data : [['00', '00' ], ['01', '01' ], ['02', '02' ], ['03', '03' ], ['04', '04' ], ['05', '05'], ['06', '06'], ['07', '07'], ['08', '08'], ['09', '09'], ['10', '10'], ['11', '11'], ['12', '12']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				readOnly : false
			},
			{xtype : 'tbtext', id:'txtBusinessHourBeginText',hidden : true, text : '时'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 40,
				value : '00',
				id : 'history_comboBegionMin',
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
			},
			{xtype : 'tbtext', id:'txtBusinessMinBeginText',hidden : true,text : '分'},
			{
				xtype : 'tbtext',
				hidden : false,
				text : '&nbsp;至&nbsp;'
			}, 
			{xtype : 'tbtext', id : 'txtBusinessHourEnd', text : '<font style="color:green; font-size:20px">00:00</font>'},
			{
		    		xtype : 'combo',
		    		width : 50,
		    		value : 0,
		    		id : 'history_comboBusinessEndApm',
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
		    	
		    },
			{
				xtype : 'combo',
				forceSelection : true,
				width : 40,
				value : '00',
				id : 'history_comboEndHour',
				hidden : true,
				store : new Ext.data.SimpleStore({
					fields : [ 'value', 'text' ],
					data : [['00', '00' ], ['01', '01' ], ['02', '02' ], ['03', '03' ], ['04', '04' ], ['05', '05'], ['06', '06'], ['07', '07'], ['08', '08'], ['09', '09'], ['10', '10'], ['11', '11'], ['12', '12']]
				}),
				valueField : 'value',
				displayField : 'text',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				readOnly : false
			},
			{xtype : 'tbtext', id:'txtBusinessHourEndText',hidden : true, text : '时'},
			{
				xtype : 'combo',
				forceSelection : true,
				width : 40,
				value : '00',
				id : 'history_comboEndMin',
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
			},
			{xtype : 'tbtext', id:'txtBusinessMinEndText', hidden : true,text : '分'},
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
		]
	});
	
	
	
	
	var billsGridTbar = new Ext.Toolbar({
		id : 'historyCommonSBar',
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;账单号:'
		}, 
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
			xtype : 'numberfield',
			id : 'numberSearchValue',
			width : 100
		},
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;&nbsp;&nbsp;流水号:'
		}, 
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
			xtype : 'numberfield',
			id : 'numberSearchSeqIdValue',
			width : 100
		},
		{ xtype:'tbtext', text:'&nbsp;&nbsp;'},
		{
			xtype : 'radio',
			id : 'searchAdditionFilterAll',
			checked : true,
			boxLabel : '全部',
			name : 'conditionRadio',
			inputValue : 0,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '反结帐',
			inputValue : 1,
			listeners : {
				check : function(e){
					if(e.getValue()){
						foodStatus = 'isRepaid';
						searchAdditionFilter = e.getId();
					}
						
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '折扣',
			inputValue : 2,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '赠送',
			inputValue : 3,
			listeners : {
				check : function(e){
					if(e.getValue()){
						foodStatus = 'isGift';
						searchAdditionFilter = e.getId();
					}
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '退菜',
			inputValue : 4,
			listeners : {
				check : function(e){
					if(e.getValue()){
						foodStatus = 'isReturn';
						searchAdditionFilter = e.getId();
					}
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '转菜',
			inputValue : 7,
			listeners : {
				check : function(e){
					if(e.getValue()){
						foodStatus = 'isTransfer';
						searchAdditionFilter = e.getId();
					}
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '抹数',
			inputValue : 5,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		}, { xtype:'tbtext', text:'&nbsp;&nbsp;'}, {
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '优惠劵',
			inputValue : 6,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		},{ xtype:'tbtext', text:'&nbsp;&nbsp;'},{
			xtype : 'radio',
			name : 'conditionRadio',
			boxLabel : '会员价',
			inputValue : 8,
			listeners : {
				check : function(e){
					if(e.getValue())
						searchAdditionFilter = e.getId();
				}
			}
		},
		'->',
		{
			text : '搜索',
			id : 'btnSreachForMainOrderGrid',
			iconCls : 'btn_search',
			handler : function(e){
				billQueryHandler();
			}
		},{
			text : '导出',
			iconCls : 'icon_tb_exoprt_excel',
			handler : function(){
				billQueryExportHandler();
			}
		}]
	});

	billsGrid = createGridPanel(
		'billsGrid',
		'',
		'',
		'',
		'../../QueryOrderStatistics.do',
		[
			[true, false, false, true], 
			['帐单号', 'id'],
			['流水号', 'seqId',70],
			['台号', 'table.alias', 120, null, function(v,m,r){
				if(r.get('table.name') != ''){
					return v + '(' + r.get("table.name") + ')';
				}else{
					return v;
				}
			}],
			['区域', 'table.region.name'],
			['日期', 'orderDateFormat', 150],
//			['账单类型', 'categoryText',,'center'],
			['结账方式', 'settleTypeText', null, 'center'],
			['收款方式', 'payTypeText', null, 'center'],
			['优惠劵金额', 'couponPrice', null, 'right', function(v){
				if(!isNaN(v)){
					return Ext.ux.txtFormat.gridDou(v);
				}else{
					return v; 
				}
			}],
			['原价', 'totalPrice', null, 'right', Ext.ux.txtFormat.gridDou],
			['实收', 'actualPrice', null, 'right', Ext.ux.txtFormat.gridDou],
			['状态', 'statusText', null, 'center', function(v, m, r){
				if(r.get("statusValue") == 2){
					return '<font color=\"#FF0000\">反结账</font>';
				}else{
					return v;
				}
			}],
			['备注', 'comment', null, 'center', function (value, meta, rec, rowIdx, colIdx, ds){
				var subValue = value.length > 6 ? value.substring(0,6) + '...' : value ;
    			return '<div ext:qtitle="" ext:qtip="' + value + '">' + subValue +'</div>';
			}],
			['操作', 'operator', 140, 'center', 	function (value, cellmeta, record, rowIndex, columnIndex, store) {
				return '<a class="checkOrder", orderId="' + record.get('id') + '">' + '查看' + '</a>' +
					   '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' +
					   '<a class="detailOrder", orderId="' + record.get('id') + '">' + '明细' + '</a>' ;
			}]
		],
		OrderRecord.getKeys(),
		[['dataType', 1]],
		GRID_PADDING_LIMIT_20,
		'',
		[billsGridTbar,historyHighTimeBar, historyExtraBar]
	);
	billsGrid.region = 'center';
	
	billsGrid.getStore().on('load', function(store, records, options){
		//账单明细
		$('#grid_panel_historOrders').find('.detailOrder').each(function(index, element){
			element.onclick = function(){
				billDetailHandler($(element).attr('orderId'));
			}
		});
		//账单查看
		$('#grid_panel_historOrders').find('.checkOrder').each(function(index, element){
			element.onclick = function(){
				billViewHandler();
			}
		});
	});
	
	billsGrid.on('render', function(){
		history_dateCombo.setValue(1);
		history_dateCombo.fireEvent('select', history_dateCombo,null,1);
	});
	billsGrid.on('bodyresize', function(e, w, h){
		
	});
	billsGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			billQueryHandler();
		}
	}];
	
	// --------------------------------------------------------------------------
	new Ext.Panel({
		//title : '历史账单管理',
		id : 'grid_panel_historOrders',
		renderTo : 'divHistoryStatistics',
		width : parseInt(Ext.getDom('divHistoryStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divHistoryStatistics').parentElement.style.height.replace(/px/g,'')),
		//region : 'center',
		layout : 'fit',
		items : [ {
			layout : 'border',
			items : [billsGrid]
		} ],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [
			{xtype:'tbtext',text:'&nbsp;'},
			//FIXME
			paymentBut,
			{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			shiftStatBut, 
			{xtype:'tbtext',text:'&nbsp;&nbsp;&nbsp;'},
			dailySettleStatBut
			]
		})
	});
	billHistoryOnLoad();
//	billQueryHandler();
	
	//--------------------------------------------------------------------
	
	//-----------------交班记录---------------------------------
	var dutyRangeStatWin = null, dutyRangeStatPanel = null;
	var statType;
	function dutyRangeStatPrintHandler(rowIndex) {
		var gs = Ext.ux.getSelData(dutyRangeStatPanel);
		var tempMask = new Ext.LoadMask(document.body, {
			msg : '正在打印请稍候.......',
			remove : true
		});
		tempMask.show();
		Ext.Ajax.request({
			url : '../../PrintOrder.do',
			params : {
				
				'printType' : statType == 1 ? 7 : 13,
				'onDuty' : gs['onDutyFormat'],
				'offDuty' : gs['offDutyFormat']
			},
			success : function(response, options) {
				tempMask.hide();
				Ext.ux.showMsg(Ext.decode(response.responseText));
			},
			failure : function(response, options) {
				tempMask.hide();
				Ext.ux.showMsg(Ext.decode(response.responseText));
			}
		});
	};
	
	function dutyRangeStatDetalHandler(){
		var gs = Ext.ux.getSelData(dutyRangeStatPanel);
		if(gs != false){
			dutyRangeStatWin = Ext.getCmp('dutyRangeStatWin');
			if(!dutyRangeStatWin){
				dutyRangeStatWin = new Ext.Window({
					title : '营业统计 -- <font style="color:green;">历史</font> -- '+(statType == 1?'交班人':'交款人')+':&nbsp;<font style="color:red;">' + gs['staffName'] + '</font>',
					id : 'dutyRangeStatWin',
					width : 885,
					height : 600,
					closable : false,
					modal : true,
					resizable : false,	
					layout: 'fit',
					bbar : ['->', {
						text : '关闭',
						iconCls : 'btn_close',
						handler : function(){
							dutyRangeStatWin.destroy();
						}
					}],
					keys : [{
						key : Ext.EventObject.ESC,
						scope : this,
						fn : function(){
							dutyRangeStatWin.destroy();
						}
					}],
					listeners : {
						hide : function(thiz){
							thiz.body.update('');
						},
						show : function(thiz){
							gs = Ext.ux.getSelData(dutyRangeStatPanel);
							thiz.load({
								autoLoad : false,
								url : '../window/history/businessStatistics.jsp',
								scripts : true,
								nocache : true,
								text : '功能加载中, 请稍后......',
								params : {
									d : '_' + new Date().getTime(),
									dataSource : statType == 2?'paymentHistory':'history',
									queryPattern : statType == 2? 5 : 2,
									onDuty : statType == 1?gs['onDuty']:gs['onDutyFormat'],
									offDuty : statType == 1?gs['offDuty']:gs['offDutyFormat'],
									businessStatic : statType,
									staffId : statType == 2?gs['staffId']:''
								}
							});
						}
					}
				});
			}
			dutyRangeStatWin.show();
			dutyRangeStatWin.center();
		}
	};
	
	function dutyRangeStatPanelInit(c){
		var beginDate = new Ext.form.DateField({
			xtype : 'datefield',		
			format : 'Y-m-d',
			width : 100,
			maxValue : new Date(),
			readOnly : false,
			allowBlank : false,
			listeners : {
				blur : function(thiz){									
					Ext.ux.checkDuft(true, thiz.getId(), endDate.getId());
				}
			}
		});
		var endDate = new Ext.form.DateField({
			xtype : 'datefield',
			format : 'Y-m-d',
			width : 100,
			maxValue : new Date(),
			readOnly : false,
			allowBlank : false,
			listeners : {
				blur : function(thiz){									
					Ext.ux.checkDuft(false, beginDate.getId(), thiz.getId());
				}
			}
		});
		
		var branchSelect_combo_dutyRange = new Ext.form.ComboBox({
			id : 'branchSelect_combo_dutyRange',
			readOnly : false,
			forceSelection : true,
			width : 113,
			listWidth : 120,
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
								data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
								
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
							}
						
							thiz.store.loadData(data);
							thiz.setValue(jr.root[0].id);
							thiz.fireEvent('select');
						}
					});	
				}
			}
		});
		
		var dateCombo = Ext.ux.createDateCombo({
			beginDate : beginDate,
			endDate : endDate,
			callback : function(){
				Ext.getCmp('btnRefreshDutyStatRange').handler();
			}
		});
		dutyRangeStatPanelTbar = new Ext.Toolbar({
			items : [{
				xtype:'tbtext',
				text:'日期:'
			}, dateCombo, {
				xtype:'tbtext',
				text:'&nbsp;'
			}, beginDate, {
				xtype:'tbtext',
				text:'&nbsp;至&nbsp;'
			}, endDate, {
				xtype : 'tbtext',
				text : '&nbsp;'
			}, {
				xtype : 'tbtext',
				text : '门店选择:'
			}, branchSelect_combo_dutyRange, {
				xtype:'tbtext',
				text:'&nbsp;'
			}, '->', {
				text : '搜索',
				id : 'btnRefreshDutyStatRange',
				iconCls : 'btn_search',
				handler : function(){
					if(!beginDate.isValid() || !endDate.isValid()){
						return;
					}
					var gs = dutyRangeStatPanel.getStore();
					gs.baseParams['onDuty'] = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00');
					gs.baseParams['offDuty'] = Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59');
					gs.baseParams['branchId'] = Ext.getCmp('branchSelect_combo_dutyRange').getValue();
					gs.load({
						params : {
							start : 0,
							limit : 10
						}
					});
				}
			}]
		});
		var url = '';
		//交班
		if(eval(c.statType == 1)){
			url = '../../DutyRangeStat.do';
		}else if(eval(c.statType == 2)){ //交款
			url = '../../PaymentStat.do';
		}
		
		dutyRangeStatPanel = createGridPanel(
			'duty_panel_historyOrder',
			'',
			'',
			'',
			url,
			[[true, false, false, true], 
		     [c.statType == 1?'交班人':'交款人', 'staffName', 60],
		     ['开始时间', 'onDutyFormat'], 
		     ['结束时间', 'offDutyFormat'], 
		     ['操作','Operation', 100, 'center', function(){
				return '<a class="dutyDetail">详细</a>' +
					   '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' +
					   '<a class="dutyPrint">补打</a>';
			}]
			],
			['staffId','staffName', 'onDuty', 'offDuty', 'onDutyFormat', 'offDutyFormat'],
			[ ['dataSource', 'history'], ['isPaging', true]],
			10,
			null,
			dutyRangeStatPanelTbar
		);
		dutyRangeStatPanel.frame = false;
		dutyRangeStatPanel.border = false; 
		
		dutyRangeStatPanel.getStore().on('load', function(store, records, options){
			//交班详细
			$('#duty_panel_historyOrder').find('.dutyDetail').each(function(index, element){
				element.onclick = function(){
					dutyRangeStatDetalHandler();
				}
			});
			//交班补打
			$('#duty_panel_historyOrder').find('.dutyPrint').each(function(index, element){
				element.onclick = function(){
					dutyRangeStatPrintHandler();
				}
			});
		});
	}
	
	function dutyRangeStatWinInit(c){
		if(!dutyRangeStatPanel || dutyRangeStatPanel == null){
			dutyRangeStatPanelInit(c);
		}
		dutyRangeStatWin = new Ext.Window({
			title : c.statType == 1?'交班记录':'交款记录',
			layout : 'fit',
			resizable : false,
			modal : true,
			closable : false,
			constrainHeader : true,
			width : 600,
			height : 410,
			items : [dutyRangeStatPanel],
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(){
					dutyRangeStatWin.destroy();
					dutyRangeStatPanel.destroy();
					dutyRangeStatWin = null;
					dutyRangeStatPanel = null;
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					dutyRangeStatWin.destroy();
					dutyRangeStatPanel.destroy();
					dutyRangeStatWin = null;
					dutyRangeStatPanel = null;
				}
			}],
			listeners : {
				show : function(){
	//				Ext.getCmp('btnRefreshDutyStatRange').handler();
				}
			}
		});
	}
	
	function dutyRangeStat(c){	
		statType = c.statType;
		if(!dutyRangeStatWin || dutyRangeStatWin == null){
			dutyRangeStatWinInit(c);
		}
		dutyRangeStatWin.show();
		dutyRangeStatWin.center();
	}
	
	//-----------------日结记录------------------------
	var dailySettleStatGrid;
	var dailySettleStatWin;
	function dailySettleStatDetalHandler(){
		var gs = Ext.ux.getSelData(dailySettleStatGrid);
		if(gs != false){
			var dailySettleStatWin = Ext.getCmp('dailySettleStatWin');
			if(!dailySettleStatWin){
				dailySettleStatWin = new Ext.Window({
					title : '营业统计 -- <font style="color:green;">历史</font>',
					id : 'dailySettleStatWin',
					width : 885,
					height : 600,
					closable : false,
					modal : true,
					resizable : false,	
					layout: 'fit',
					bbar : ['->', {
						text : '关闭',
						iconCls : 'btn_close',
						handler : function(){
							dailySettleStatWin.hide();
						}
					}],
					keys : [{
						key : Ext.EventObject.ESC,
						scope : this,
						fn : function(){
							dailySettleStatWin.hide();
						}
					}],
					listeners : {
						hide : function(thiz){
							thiz.body.update('');
						},
						show : function(thiz){
							gs = Ext.ux.getSelData(dailySettleStatGrid);
							thiz.load({
								autoLoad : false,
								url : '../window/history/businessStatistics.jsp',
								scripts : true,
								nocache : true,
								text : '功能加载中, 请稍后......',
								params : {
									d : '_' + new Date().getTime(),
									queryPattern : 2,
									dataSource : 'history',
									onDuty : gs['onDuty'],
									offDuty : gs['offDuty']
								}
							});
						}
					}
				});
			}
			dailySettleStatWin.show();
			dailySettleStatWin.center();
		}
	};
	
	function dailySettleStatPrintHandler() {
		var gs = Ext.ux.getSelData(dailySettleStatGrid);
		if(gs != false){
			var tempMask = new Ext.LoadMask(document.body, {
				msg : '正在打印请稍候.......',
				remove : true
			});
			tempMask.show();
			Ext.Ajax.request({
				url : '../../PrintOrder.do',
				params : {
					
					'printType' : 8,
					onDuty : gs['onDutyFormat'],
					offDuty : gs['offDutyFormat']
				},
				success : function(response, options) {
					tempMask.hide();
					Ext.ux.showMsg(Ext.decode(response.responseText));
				},
				failure : function(response, options){
					tempMask.hide();
					Ext.ux.showMsg(Ext.decode(response.responseText));
				}
			});
		}
	};
	
	
	function dailySettleStatGridInit(){
		var onDuty = new Ext.form.DateField({
			xtype : 'datefield',		
			format : 'Y-m-d',
			width : 100,
			maxValue : new Date(),
			readOnly : false,
			allowBlank : false,
			listeners : {
				blur : function(thiz){									
	//				Ext.ux.checkDuft(true, thiz.getId(), offDuty.getId());
				}
			}
		});
		var offDuty = new Ext.form.DateField({
			xtype : 'datefield',
			format : 'Y-m-d',
			width : 100,
			maxValue : new Date(),
			readOnly : false,
			allowBlank : false,
			listeners : {
				blur : function(thiz){									
	//				Ext.ux.checkDuft(false, onDuty.getId(), thiz.getId());
				}
			}
		});
		
		var branchSelect_combo_dailtSettle = new Ext.form.ComboBox({
			id : 'branchSelect_combo_dailtSettle',
			readOnly : false,
			forceSelection : true,
			width : 113,
			listWidth : 120,
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
								data.push([jr.root[0]['id'], jr.root[0]['name'] + '(集团)']);
								 
								for(var i = 0; i < jr.root[0].branches.length; i++){
									data.push([jr.root[0].branches[i]['id'], jr.root[0].branches[i]['name']]);
								}
							}
							
							thiz.store.loadData(data);
							thiz.setValue(jr.root[0].id);
							thiz.fireEvent('select');
						}
					});
				}
			}
		});
		
		var dateCombo = Ext.ux.createDateCombo({
			beginDate : onDuty,
			endDate : offDuty,
			callback : function(){
				Ext.getCmp('btnSearchBydDilySettleStatGrid').handler();
			}
		});
		
		var dailySettleStatGridTbar = new Ext.Toolbar({
			height : 26,
			items : [{
				xtype : 'tbtext',
				text : '日期:'
			}, dateCombo, {
				xtype : 'tbtext',
				text : '&nbsp;'
			},onDuty, {
				xtype : 'tbtext',
				text : '&nbsp;至&nbsp;'
			}, offDuty, {
				xtype : 'tbtext',
				text : '&nbsp;'
			}, {
				xtype : 'tbtext',
				text : '门店选择'
			}, branchSelect_combo_dailtSettle, '->', {
				text : '搜索',
				id : 'btnSearchBydDilySettleStatGrid',
				iconCls : 'btn_search',
				handler : function(){
					if(!onDuty.isValid() || !offDuty.isValid()){
						return;
					}
					var gs = dailySettleStatGrid.getStore();
					gs.baseParams['onDuty'] = Ext.util.Format.date(onDuty.getValue(), 'Y-m-d 00:00:00');
					gs.baseParams['offDuty'] = Ext.util.Format.date(offDuty.getValue(), 'Y-m-d 23:59:59');
					gs.baseParams['branchId'] = Ext.getCmp('branchSelect_combo_dailtSettle').getValue();
					gs.load({
						params : {
							start : 0,
							limit : 10
						}
					});
				}
			}]
		});
		
		dailySettleStatGrid = createGridPanel(
			'daily_panel_historyOrder',
			'',
			'',
			'',
			'../../dailySettleStat.do',
			[[true, false, false, true], 
			 ['操作人', 'staffName'],
			 ['开始时间', 'onDutyFormat'],
			 ['结束时间', 'offDutyFormat'],
			 ['操作', 'operator', 130, 'center', function(value, cellmeta, record, rowIndex, columnIndex, store){
			 	return '<a class="dailyDetail">详细</a>' +
			 		   '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' +
			 		   '<a class="dailyPrint">补打</a>';
//				return '<a href=\"javascript:dailySettleStatDetalHandler()">详细</a>'
//				+ '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
//				+ '<a href=\"javascript:dailySettleStatPrintHandler()">打印</a>';
			}]
			],
			['staffName', 'onDuty', 'onDutyFormat', 'offDuty', 'offDutyFormat'],
			[ ['isPaging', true], ['restaurantID', restaurantID]],
			15,
			null,
			dailySettleStatGridTbar
		);
		dailySettleStatGrid.frame = false;
		dailySettleStatGrid.border = false;	
		
		dailySettleStatGrid.getStore().on('load', function(store, records, options){
			//日结详细
			$('#daily_panel_historyOrder').find('.dailyDetail').each(function(index, element){
				element.onclick = function(){
					dailySettleStatDetalHandler();
				}
			});
			//日结补打
			$('#daily_panel_historyOrder').find('.dailyPrint').each(function(index, element){
				element.onclick = function(){
					dailySettleStatPrintHandler();
				}
			});
		});
	}
	
	function dailySettleStat(){
		if(!dailySettleStatWin){
			if(!dailySettleStatGrid){
				dailySettleStatGridInit();
			}
			dailySettleStatWin = new Ext.Window({
				title : '日结记录',
				width : 700,
				height : 410,
				resizable : false,
				modal : true,
				closable : false,
				layout : 'fit',
				items : [dailySettleStatGrid],
				bbar : ['->', {
					text : '关闭',
					iconCls : 'btn_close',
					handler : function(){
						dailySettleStatWin.hide();
					}
				}],
				keys : [{
					key : Ext.EventObject.ESC,
					scope : this,
					fn : function(){
						dailySettleStatWin.hide();
					}
				}]
			});
		}
		
		dailySettleStatWin.show();
		dailySettleStatWin.center();
	};
	
	setTimeout(function(){
		Ext.getCmp('btnSreachForMainOrderGrid').handler();
	}, 200);

});

