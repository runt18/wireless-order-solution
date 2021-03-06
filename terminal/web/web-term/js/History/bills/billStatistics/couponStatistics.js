Ext.onReady(function(){
	var isDuty = false;
	var beginDate = new Ext.form.DateField({
		id: 'beginDate_combo_coupon',
		xtype : 'datefield',
		format : 'Y-m-d',
		width : 100,
		maxValue : new Date(),
		readOnly : false,
		allowBlank : false
	});
	
	var endDate = new Ext.form.DateField({
		id : 'endDate_combo_coupon',
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
			Ext.getCmp('coupon_btnSearch').handler();
		}
	});
	//操作类型的select
	var couponType = new Ext.form.ComboBox({
		xtype : 'combo',
		id : 'couponType_combo_couponStatistics',
		forceSelection : true,
		width : 100,
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
				$.ajax({
					url : '../../OperateCoupon.do',
					type :'post',
					dataType : 'json',
					data : {
						dataSource : 'getOperateType'
					},
					success : function(jr){
						var data = [['', '全部']];
						if(jr.success){
							for(var i = 0; i < jr.root[0].operateType.length; i++){
								data.push([jr.root[0].operateType[i]['value'], jr.root[0].operateType[i]['name']]);
							}
						}
						thiz.store.loadData(data);
						thiz.setValue('');
					}
				
				})
						
			},
			select : function(){
				Ext.getCmp('coupon_btnSearch').handler();
			}
		}
			
	});
	
	
	
	//员工的select
	var couponStaff = new Ext.form.ComboBox({
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
				var data = [[-1, '全部']];
				Ext.Ajax.request({
					url : '../../QueryStaff.do',
					params : {privileges : '1004'},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						for(var i = 0; i < jr.root.length; i++){
							data.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
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
			select : function(){
				Ext.getCmp('coupon_btnSearch').handler();
			}
		}
	});
	
	//优惠券类型的选择
	var coupon = new Ext.form.ComboBox({
		id : 'coupon_combo_couponStatistics',
		readOnly : false,
		forceSelection : true,
		width : 103,
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
						
					},
					fialure : function(res, opt){
						thiz.store.loadData(data);
						thiz.setValue('');
					}
				});
				
			},
			select : function(){
				Ext.getCmp('coupon_btnSearch').handler();
			}
		}
	});
	
	//门店选择
	var branch_combo_coupon = new Ext.form.ComboBox({
		id : 'branch_combo_coupon',
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
				if(branch_combo_coupon.getValue() == -1){
					couponStaff.setDisabled(true);
					couponStaff.setValue(-1);
					
					Ext.getCmp('coupon_comboBusinessHour').setDisabled(true);
					Ext.getCmp('coupon_comboBusinessHour').setValue(-1);
					
				}else{
					//加载操作人员
					var staff = [[-1, '全部']];
					Ext.Ajax.request({
						url : '../../QueryStaff.do',
						params : {
							branchId : branch_combo_coupon.getValue()
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							
							for(var i = 0; i < jr.root.length; i++){
								staff.push([jr.root[i]['staffID'], jr.root[i]['staffName']]);
							}
							
							couponStaff.setDisabled(false);
							couponStaff.store.loadData(staff);
							couponStaff.setValue(-1);
						}
					});
					
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
				}
				
				if(!isJump){
					isDuty = false;
					Ext.getCmp('coupon_btnSearch').handler();	
				}else{
					isDuty = true;
				}
			}
		}
	});
	
	
	//toorbar
	var couponStatisticsTbarItem = [{
		xtype : 'tbtext',
		width : 10
	}, {
		xtype : 'tbtext',
		text : '会员手机/会员卡号/会员名'
	}, {
		xtype : 'textfield',
		id : 'memeberName_textfield',
		width : 150
	}, '->', {
		text : '搜索',
		id : 'coupon_btnSearch',
		iconCls : 'btn_search',
		handler : function(e, aa){
			if(!beginDate.isValid() || !endDate.isValid){
				return;
			}
			
			businessHour =  Ext.ux.statistic_oBusinessHourData({type : 'get', statistic : 'coupon_'}).data;
			
			var memeberName = Ext.getCmp('memeberName_textfield');
			
			var store = couponGrid.getStore();
			store.baseParams['dataSource'] = 'getOperations',
			store.baseParams['beginDate'] = Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00');
			store.baseParams['endDate'] = Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59');
			store.baseParams['staffId'] = couponStaff.getValue() < 0 ? '' : couponStaff.getValue();
			store.baseParams['opening'] = businessHour.opening != '00:00' ? businessHour.opening : '';
			store.baseParams['isDuty'] = isDuty;
			store.baseParams['ending'] = businessHour.ending != '00:00' ? businessHour.ending : '';
			store.baseParams['couponTypeId'] = coupon.getValue();
			store.baseParams['branchId'] = branch_combo_coupon.getValue();
			
			if(couponType.getValue() == 'issue' || couponType.getValue() == 'use' ){
				store.baseParams['operate'] = null;
				store.baseParams['operateType'] = couponType.getValue();
			}else{
				store.baseParams['operateType'] = null;
				store.baseParams['operate'] = couponType.getValue();
			}
			store.baseParams['memberFuzzy'] = memeberName.getValue();
			store.load({
				params : {
					start : 0,
					limit : 20
				}
			});
			
			if(couponStaff.getValue() && couponStaff.getValue() != -1){
				StaffName = '操作人: ' + couponStaff.getEl().dom.value;
			}else{
				StaffName = '';
			}
			

		}
	}, {
		text : '导出',
		iconCls : 'icon_tb_exoprt_excel',
		handler : function(){
			var url = '../../{0}?dataSource={1}&beginDate={2}&endDate={3}&staffId={4}&opening={5}&isDuty={6}&ending={7}&couponTypeId={8}&branchId={9}&operate={10}&operateType={11}&memberFuzzy={12}';
			url = String.format(
				url,
				'ExportHistoryStatisticsToExecl.do',
				'couponDetail',
				Ext.util.Format.date(beginDate.getValue(), 'Y-m-d 00:00:00'),
				Ext.util.Format.date(endDate.getValue(), 'Y-m-d 23:59:59'),
				couponStaff.getValue() < 0 ? '' : couponStaff.getValue(),
				businessHour.opening != '00:00' ? businessHour.opening : '',
				isDuty,
				businessHour.ending != '00:00' ? businessHour.ending : '', 
				coupon.getValue(),
				branch_combo_coupon.getValue(),
				couponType.getValue() == 'issue' || couponType.getValue() == 'use' ? null : couponType.getValue(),
				couponType.getValue() == 'issue' || couponType.getValue() == 'use' ? couponType.getValue() : null,
				Ext.getCmp('memeberName_textfield').getValue()
			);
			window.location = url;
		}
	}];
	
	
	//头部
	var statisticsTbar = Ext.ux.initTimeBar({
		beginDate : beginDate, 
		endDate : endDate,
		dateCombo : dataCombo, 
		tbarType : 1, 
		statistic : 'coupon_', 
		callback : function businessHourSelect(){
			}
	}).concat(couponStatisticsTbarItem);
	
	
	
	//couponGrid的栏目
	var cm = new Ext.grid.ColumnModel([
	    new Ext.grid.RowNumberer(),
	    {header : '操作日期', dataIndex : 'operateDate'},
	    {header : '门店名称', dataIndex : 'restaurantName'},
	    {header : '优惠券', dataIndex : 'couponName'},
	    {header : '面额', dataIndex : 'couponPrice', renderer : Ext.ux.txtFormat.gridDou},
	    {header : '操作类型', dataIndex : 'operateText'},
	    {header : '关联信息', dataIndex : 'associateId', renderer : couponLinkOrderId},
	    {header : '会员', dataIndex : 'memberName', renderer : couponMemberName},
	    {header : '操作人', dataIndex : 'operateStaff'},
	    {header : '备注', dataIndex : 'comment'}
   ]);
	
	//默认排序
	cm.defaultSortable = true;
	
	//couponGrid的数据源
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url : '../../OperateCoupon.do'}),
		reader : new Ext.data.JsonReader({totalProperty : 'totalProperty', root : 'root', idProperty : ''}, [
             {name : 'operateDate'},
             {name : 'restaurantName'},
             {name : 'couponName'},
             {name : 'couponPrice'},
             {name : 'operateText'},
             {name : 'associateId'},
             {name : 'operateStaff'},
             {name : 'memberName'},
             {name : 'comment'}
        ]),
        listeners : {
        	load : function(store, records, options){
        		if(store.getCount() > 0){
        			var sumRow = couponGrid.getView().getRow(store.getCount() - 1);
        			sumRow.style.backgroundColor = '#EEEEEE';
        			for(var i = 0; i < couponGrid.getColumnModel().getColumnCount(); i++){
        				var sumCell = couponGrid.getView().getCell(store.getCount() -1, i);
        				sumCell.style.fontSize = '15px';
        				sumCell.style.fontWeight = 'bold';
        				sumCell.style.color= 'green';
        			}
        			couponGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
        			couponGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
        			couponGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
        			couponGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
        			couponGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
        			couponGrid.getView().getCell(store.getCount()-1, 9).innerHTML = '--';
        		}
        		$('#couponStatistics_div_cpStatistics').find('.couponLinkId').each(function(index, element){
        			element.onclick = function(){
        				couponShowDetail($(element).text());
        			}
        		});
        	}
        }
	});
	
	var pagingBar = new Ext.PagingToolbar({
		pageSize : 20,
		store : ds,
		displayInfo : true,
		displayMsg : "显示第{0} 条到 {1} 条记录, 共 {2}条",
		emptyMsg : " 没有记录"
	});
	
	//couponGrid表格
	var couponGrid = new Ext.grid.GridPanel({
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
		bbar : pagingBar,
		listeners : {
			render : function(){
				var secondToolBar = new Ext.Toolbar({
					items : [{
						xtype : 'tbtext',
						text : '操作人员:'
					}, couponStaff,{
						xtype : 'tbtext',
						width : 10
					}, {
						xtype : 'tbtext',
						text : '操作类型 :'
					}, couponType, {
						xtype : 'tbtext',
						width : 10
					},{
						xtype : 'tbtext',
						text : '优惠券类型'
					}, coupon,  {
						xtype : 'tbtext',
						width : 10
					},{
						xtype : 'tbtext',
						text : '门店选择'
					}, branch_combo_coupon]
				});
				secondToolBar.render(couponGrid.tbar);
			}
		}
	});
	
	//定义couponGrid的位置
	couponGrid.region = 'center';

	couponGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('coupon_btnSearch').handler();
		}
	}];
	
	var couponDetailPanel = new Ext.Panel({
		title : '优惠券明细',
		layout : 'border',
		region : 'center',
		frame : true,
		items : [couponGrid]
	});
	
	//关联链接
	function couponLinkOrderId(v){
		return '<a class="couponLinkId">' + v + '</a>';
	}
	
	
	//会员名称
	function couponMemberName(v){
		if(v){
			return '<font>'+ v + '</font>';
		}else{
			return '<font>无</font>'
		}
	}
	

	//显示账单窗口
	function couponShowDetail(associateId){
		couponWin = new Ext.Window({
			layout : 'fit',
			title : '查看会员价账单',
			width : 510,
			height : 550,
			resizable : false,
			closable : false,
			modal : true,
			bbar : ['->', {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function() {
					couponWin.destroy();
				}
			}],
			keys : [{
				key : Ext.EventObject.ESC,
				scope : this,
				fn : function(){
					couponWin.destroy();
				}
			}],
			listeners : {
				show : function(thiz) {
					thiz.load({
						url : '../window/history/viewBillDetail.jsp', 
						scripts : true,
						method : 'post'
					});
					thiz.center();	
					thiz.orderId = associateId;
					thiz.queryType = 'History';
					
				}
			}
		});
		couponWin.show();
		couponWin.center();
	}
	
	
	new Ext.Panel({
		renderTo : 'couponStatistics_div_cpStatistics',
		id : 'couponStatisticsPanel',
		width : parseInt(Ext.getDom('couponStatistics_div_cpStatistics').parentElement.style.width.replace(/px/g, '')),
		height : parseInt(Ext.getDom('couponStatistics_div_cpStatistics').parentElement.style.height.replace(/px/g, '')),
		layout : 'border',
		frame : true,
		items : [couponDetailPanel]
	});
	
	dataCombo.setValue(1);
	dataCombo.fireEvent('select', dataCombo, null, 1);
	
});