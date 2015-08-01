//mcs:memberChargeStatistics
var mcs_grid, mcs_search_memberType
	,mcs_search_onDuty, mcs_search_offDuty, mcs_search_memberName, mcs_search_dateCombo;
var mcs_modal = true;

var mcs_highChart, mcs_PanelHeight = 0, mcs_panelDrag = false;

var mcs_southPanel;

function mcs_showChart(){
	var dateBegin = Ext.util.Format.date(mcs_search_onDuty.getValue(), 'Y-m-d');
	var dateEnd = Ext.util.Format.date(mcs_search_offDuty.getValue(), 'Y-m-d');
	
	var chartData;
	$.ajax({
		url : '../../QueryMemberStatistics.do',
		type : 'post',
		dataType : 'json',
		data : {
			dataSource :'createdStatistics',
			dateBegin : dateBegin,
			dateEnd : dateEnd + " 23:59:59"
		},
		async : false,
		success : function(data){
			chartData = eval('(' + data.other.businessChart + ')');
		},
		error : function(xhr){
		}
	});
	
	
	mcs_highChart = new Highcharts.Chart({
		plotOptions : {
			line : {
				cursor : 'pointer',
				dataLabels : {
					enabled : true,
					style : {
						fontWeight: 'bold', 
						color: 'green' 
					}
				}
			}
		},
        chart: {  
        	renderTo: 'memberChargeChart'
    	}, 
        title: {
            text: '<b>会员开卡走势图（'+ dateBegin +'至'+ dateEnd +'）</b>'
        },
        labels: {
        	items : [{
        		html : '<b>日均开卡数:' + chartData.avgCount + ' 个</b>',
	        	style : {left :/*($('#memberChargeChart').width()*0.80)*/'0px', top: '0px'}
        	}]
        },
        xAxis: {
            categories: chartData.xAxis,
            labels : {
            	formatter : function(){
            		return this.value.substring(5);
            	}
            }
        },
        yAxis: {
        	min: 0,
            title: {
                text: '数量 (个)'
            },
            plotLines: [{
                value: 0,
                width: 2,
                color: '#808080'
            }]
        },
        tooltip: {
//	        	crosshairs: true,
            formatter: function() {
                return '<b>' + this.series.name + '</b><br/>'+
                    this.x +': '+ '<b>'+this.y+'</b> ';
            }
        },
//	        series : [{  
//	            name: 'aaaaaa',  
//	            data: [6, 9, 2, 7, 13, 21, 10]
//	        }],
        series : chartData.ser,
        exporting : {
        	enabled : true
        },
        credits : {
        	enabled : false
        }
	});
}


function mcs_initBusinessReceipsGrid(c){
	
	mcs_search_memberType = new Ext.form.ComboBox({
		id : 'mcs_search_memberType',
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
				mcs_searchMemberOperation();
			}
		}
	});
	mcs_search_onDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(),
		hideParent : true,
		hidden : mcs_modal ? false : true,
		readOnly : false,
		allowBlank : false
	});
	mcs_search_offDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(),
		hideParent : true,
		hidden : mcs_modal ? false : true,
		readOnly : false,
		allowBlank : false
	});
	mcs_search_dateCombo = Ext.ux.createDateCombo({
		beginDate : mcs_search_onDuty,
		endDate : mcs_search_offDuty,
		callback : function(){
			mcs_searchMemberOperation();
		}
	});
	mcs_search_memberName = new Ext.form.TextField({
		xtype : 'textfield',
		width : 100
		
	});
	var mcs_mo_tbar = new Ext.Toolbar({
		height : 26,
		items : [{ 
			xtype : 'tbtext', 
			text : (mcs_modal ? '&nbsp;&nbsp;日期:&nbsp;' : ' ')
		}, mcs_search_dateCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		}, mcs_search_onDuty, { 
			xtype : 'tbtext',
			text : (mcs_modal ? '&nbsp;至&nbsp;' : ' ')
		}, mcs_search_offDuty, 
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员类型:'
		}, mcs_search_memberType, 
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;手机号/卡号/会员名称:'
		}, mcs_search_memberName, '->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(e){
				mcs_searchMemberOperation();
			}
		}, {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				mcs_search_memberType.setValue(-1);
				mcs_search_memberName.setValue();
				mcs_searchMemberOperation();
			}
			
		}, '-', {
				text : '导出',
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					var onDuty = '', offDuty = '';
					onDuty = Ext.util.Format.date(mcs_search_onDuty.getValue(), 'Y-m-d 00:00:00');
					offDuty = Ext.util.Format.date(mcs_search_offDuty.getValue(), 'Y-m-d 23:59:59');
					
					var memberType = mcs_search_memberType.getRawValue() != '' ? mcs_search_memberType.getValue() : '';
					var url = '../../{0}?memberType={1}&dataSource={2}&dateBegin={3}&dateEnd={4}&memberCardOrMobileOrName={5}';
					url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							memberType > 0 ? memberType : '', 
							'memberList',
							onDuty,
							offDuty,
							mcs_search_memberName.getValue()
						);
					window.location = url;
				}
			}]
	});
/*	mcs_grid = createGridPanel(
		'mcs_grid',
		'',
		'',
		'',
		'../../QueryMemberOperation.do',
		[
			[true, false, false, true],
			['日期', 'operateDateFormat'],
			['操作类型', 'operateTypeText', 90, 'center'],
			['会员名称', 'member.name', 60],
			['实收金额', 'deltaBaseMoney', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
			['账户充额', 'deltaTotalMoney', 60, 'right', 'Ext.ux.txtFormat.gridDou'],
			['剩余金额', 'remainingTotalMoney'],
			['充值方式', 'chargeTypeText'],
			['操作人', 'staffName', 90, 'center'],
		],
		MemberOperationRecord.getKeys(),
		[ ['isPaging', true]],
		GRID_PADDING_LIMIT_20,
		'',
		mcs_mo_tbar
	);*/
	
	mcs_grid = createGridPanel(
			'mcs_grid',
			'',
			'',
			'',
			'../../QueryMemberStatistics.do',
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
			[['isPaging', true],  ['dataSource', 'createdMember']],
			GRID_PADDING_LIMIT_20,
			'',
			[mcs_mo_tbar]
		);	
	mcs_grid.region = "center";
	mcs_grid.frame = false;
	mcs_grid.border = false;
	mcs_grid.on('render', function(thiz){
		mcs_search_dateCombo.setValue(1);
		mcs_search_dateCombo.fireEvent('select', mcs_search_dateCombo, null, 1);
	});
	

/*	mcs_grid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = mcs_grid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < mcs_grid.getColumnModel().getColumnCount(); i++){
				var sumCell = mcs_grid.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
			mcs_grid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			mcs_grid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			mcs_grid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			mcs_grid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			mcs_grid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			mcs_grid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
		}
	});*/
}

function mcs_searchMemberOperation(){
	var onDuty = '', offDuty = '';
	onDuty = Ext.util.Format.date(mcs_search_onDuty.getValue(), 'Y-m-d 00:00:00');
	offDuty = Ext.util.Format.date(mcs_search_offDuty.getValue(), 'Y-m-d 23:59:59');
	
	var memberType = mcs_search_memberType.getRawValue() != '' ? mcs_search_memberType.getValue() : '';
	
	var gs = mcs_grid.getStore();
	gs.baseParams['memberType'] = memberType > 0 ? memberType : '';
	gs.baseParams['memberCardOrMobileOrName'] = mcs_search_memberName.getValue();
	gs.baseParams['dateBegin'] = onDuty;
	gs.baseParams['dateEnd'] = offDuty;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
	//每日充值统计
	mcs_showChart();
}

function mcs_changeChartWidth(w,h){
	if(mcs_highChart != undefined){
		mcs_highChart.setSize(w, h);
	}
	
}

Ext.onReady(function(){
	
	mcs_southPanel = new Ext.Panel({
		contentEl : 'memberChargeChart',
		region : 'south'
	});
	
	mcs_initBusinessReceipsGrid({data : null});
	
	new Ext.Panel({
		renderTo : 'divMemberChargeStatistics',//渲染到
		id : 'businessReceiptStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divMemberChargeStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divMemberChargeStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		items : [mcs_grid,mcs_southPanel]
	});
	
	mcs_PanelHeight = mcs_grid.getHeight();
	
	var mcs_rz = new Ext.Resizable(mcs_grid.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		mcs_panelDrag = true;
        	}
        }
    });
    mcs_rz.on('resize', mcs_grid.syncSize, mcs_grid);//注册事件(作用:将调好的大小传个scope执行)
	
	mcs_grid.on('bodyresize', function(e, w, h){
		var chartHeight;
		if(h < mcs_PanelHeight){
			chartHeight = 250 + (mcs_PanelHeight - h);
		}else{
			chartHeight = 250 + (h - mcs_PanelHeight);
		}
		mcs_changeChartWidth(w,chartHeight);
		
		if(mcs_southPanel.getEl()){
			mcs_southPanel.getEl().setTop((h+55)) ;
		}
		
		if(mcs_panelDrag){
			mcs_southPanel.setHeight(chartHeight);
		}
		
		mcs_grid.getEl().parent().setWidth(w);
		mcs_grid.doLayout();
		
	});
	
});
