var receivablesStatResultGrid, mrd_panelMemberOperationContent;
var mrd_search_comboOperateType, mrd_search_memberType, mrd_search_comboOperateType, mrd_search_memerbCard
	,mrd_search_onDuty, mrd_search_offDuty, mrd_search_memberName, mrd_search_dateCombo;
var mrd_modal = true;


receivablesStaticRecordCount = 93;
var highChart;

var businessPanelHeight = 0, panelDrag = false;

var southPanel;

var tempLoadMask = new Ext.LoadMask(document.body, {
	msg : '正在获取信息, 请稍候......',
	remove : true
});


function newDate(str) { 
	str = str.split('-'); 
	var date = new Date(); 
	date.setUTCFullYear(str[0], str[1] - 1, str[2]); 
	date.setUTCHours(0, 0, 0, 0); 
	return date; 
} 

function loadBusinessStatistic(x){
	var date = newDate(x).getTime();
	businessStatWin = new Ext.Window({
		title : '营业统计 -- <font style="color:green;">历史</font>',
		width : 885,
		height : 580,
		closable : false,
		modal : true,
		resizable : false,	
		layout: 'fit',
		bbar : ['->', {
			text : '关闭',
			iconCls : 'btn_close',
			handler : function(){
				businessStatWin.destroy();
			}
		}],
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				businessStatWin.destroy();
			}
		}],
		listeners : {
			hide : function(thiz){
				thiz.body.update('');
			},
			show : function(thiz){
				thiz.load({
					autoLoad : false,
					url : '../window/history/businessStatistics.jsp',
					scripts : true,
					nocache : true,
					text : '功能加载中, 请稍后......',
					params : {
						dataSource : 'history',
						dutyRange : "range",
						offDuty : date,
						onDuty : date,
						queryPattern : 3
					}
				});
			}
		}
	});
	businessStatWin.show();
	businessStatWin.center();
}

function recipe_showChart(){
	var dateBegin = Ext.util.Format.date(mrd_search_onDuty.getValue(), 'Y-m-d');
	var dateEnd = Ext.util.Format.date(mrd_search_offDuty.getValue(), 'Y-m-d');
	
	var chartData;
	$.ajax({
		url : '../../QueryMemberStatistics.do',
		type : 'post',
		dataType : 'json',
		data : {
			dataSource :'chargeStatistics',
			dateBegin : dateBegin,
			dateEnd : dateEnd
		},
		async : false,
		success : function(data){
			chartData = eval('(' + data.other.businessChart + ')');
		},
		error : function(xhr){
		}
	});
	
	
	highChart = new Highcharts.Chart({
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
        	renderTo: 'businessReceiptsChart'
    	}, 
        title: {
            text: '<b>会员充值走势图（'+ dateBegin +'至'+ dateEnd +'）</b>'
        },
        labels: {
        	enabled : false,
        	items : [{
        		//html : '<b>总营业额:' + chartData.totalMoney + ' 元</b><br><b>日均收入:' + chartData.avgMoney + ' 元</b><br><b>日均账单:' + chartData.avgCount + ' 张</b>',
        		html : '',
	        	style : {left :/*($('#businessReceiptsChart').width()*0.80)*/'0px', top: '0px'}
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
                text: '金额 (元)'
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

var receivablesStatResultGrid, receipts_dateCombo;
function initBusinessReceipsGrid(c){
	
	mrd_search_memberType = new Ext.form.ComboBox({
		id : 'mrd_search_memberType',
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
				mrd_searchMemberOperation();
			}
		}
	});
	mrd_search_memerbCard = new Ext.form.NumberField({
		width : 100,
		style : 'text-align:left;'
	});
	mrd_search_onDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(),
		hideParent : true,
		hidden : mrd_modal ? false : true,
		readOnly : false,
		allowBlank : false
	});
	mrd_search_offDuty = new Ext.form.DateField({
		xtype : 'datefield',
		width : 100,
		format : 'Y-m-d',
		maxValue : new Date(),
		hideParent : true,
		hidden : mrd_modal ? false : true,
		readOnly : false,
		allowBlank : false
	});
	mrd_search_dateCombo = Ext.ux.createDateCombo({
		beginDate : mrd_search_onDuty,
		endDate : mrd_search_offDuty,
		callback : function(){
			mrd_searchMemberOperation();
		}
	});
	mrd_search_memberName = new Ext.form.TextField({
		xtype : 'textfield',
		width : 100
		
	});
	var mrd_mo_tbar = new Ext.Toolbar({
		height : 26,
		items : [{ 
			xtype : 'tbtext', 
			text : (mrd_modal ? '&nbsp;&nbsp;日期:&nbsp;' : ' ')
		}, mrd_search_dateCombo, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;'
		}, mrd_search_onDuty, { 
			xtype : 'tbtext',
			text : (mrd_modal ? '&nbsp;至&nbsp;' : ' ')
		}, mrd_search_offDuty, 
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;会员类型:'
		}, mrd_search_memberType, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;收款方式:'			
		},{
			xtype : 'combo',
			forceSelection : true,
			width : 80,
			id : 'recharge_comboPayType',
			store : new Ext.data.JsonStore({
				fields : [ 'id', 'name' ]
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
					Ext.Ajax.request({
						url : '../../QueryPayType.do?',
						params : {
							dataSource : 'allPayType'
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								jr.root.unshift({id:-1, name:'全部'});
								thiz.store.loadData(jr.root);
								thiz.setValue(-1);
							}
						},
						failure : function(res, opt){
							thiz.store.loadData({root:[{typeId:-1, name:'全部'}]});
							thiz.setValue(-1);
						}
					});
				},
				select : function(){
					mrd_searchMemberOperation();
				}
			}				
		}, 
		{
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;手机号/卡号/会员名称:'
		}, mrd_search_memberName, '->', {
			text : '搜索',
			iconCls : 'btn_search',
			handler : function(e){
				mrd_searchMemberOperation();
			}
		}, {
			text : '重置',
			iconCls : 'btn_refresh',
			handler : function(e){
				mrd_search_memberType.setValue(-1);
				mrd_search_memberName.setValue();
				mrd_searchMemberOperation();
			}
			
		}, '-', {
				text : '导出',
//				hidden : true,
				iconCls : 'icon_tb_exoprt_excel',
				handler : function(){
					var radio = document.getElementsByName('mrd_search_radioDataSource');
					var dataSource = 'today';
					for(var i = 0; i < radio.length; i++){
						if(radio[i].checked == true){
							dataSource = radio[i].value;
							break;
						}
					}
					var onDuty = '', offDuty = '';
					if(dataSource == 'history'){
						if(!mrd_search_onDuty.isValid() || !mrd_search_offDuty.isValid()){
							Ext.example.msg('提示', '操作失败, 请选择搜索时间段.');
							return;
						}
						onDuty = Ext.util.Format.date(mrd_search_onDuty.getValue(), 'Y-m-d 00:00:00');
						offDuty = Ext.util.Format.date(mrd_search_offDuty.getValue(), 'Y-m-d 23:59:59');
					}
					var memberType = mrd_search_memberType.getRawValue() != '' ? mrd_search_memberType.getValue() : '';
					var url = '../../{0}?memberType={1}&dataSource={2}&onDuty={3}&offDuty={4}&fuzzy={5}&dataSources={6}&detailOperate={7}&operateType=2&payType={8}';
					url = String.format(
							url, 
							'ExportHistoryStatisticsToExecl.do', 
							memberType > 0 ? memberType : '', 
							'rechargeDetail',
							onDuty,
							offDuty,
							mrd_search_memberName.getValue(),
							dataSource,
							Ext.getCmp('recharge_comboPayType').getValue()
						);
					window.location = url;
				}
			}]
	});
	receivablesStatResultGrid = createGridPanel(
		'receivablesStatResultGrid',
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
			['充值方式', 'payTypeText'],
			['操作人', 'staffName', 90, 'center'],
		],
		MemberOperationRecord.getKeys(),
		[ ['isPaging', true]],
		GRID_PADDING_LIMIT_20,
		'',
		mrd_mo_tbar
	);
	receivablesStatResultGrid.region = "center";
	receivablesStatResultGrid.frame = false;
	receivablesStatResultGrid.border = false;
	receivablesStatResultGrid.on('render', function(thiz){
		mrd_search_dateCombo.setValue(1);
		mrd_search_dateCombo.fireEvent('select', mrd_search_dateCombo, null, 1);
	});
	

	receivablesStatResultGrid.getStore().on('load', function(store, records, options){
		if(store.getCount() > 0){
			var sumRow = receivablesStatResultGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < receivablesStatResultGrid.getColumnModel().getColumnCount(); i++){
				var sumCell = receivablesStatResultGrid.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
			receivablesStatResultGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			receivablesStatResultGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			receivablesStatResultGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			receivablesStatResultGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			receivablesStatResultGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			receivablesStatResultGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
		}
	});
}

function mrd_searchMemberOperation(){
	var onDuty = '', offDuty = '';
	onDuty = Ext.util.Format.date(mrd_search_onDuty.getValue(), 'Y-m-d 00:00:00');
	offDuty = Ext.util.Format.date(mrd_search_offDuty.getValue(), 'Y-m-d 23:59:59');
	
	var memberType = mrd_search_memberType.getRawValue() != '' ? mrd_search_memberType.getValue() : '';
	
	var gs = receivablesStatResultGrid.getStore();
	gs.baseParams['dataSource'] = 'history';
	gs.baseParams['memberType'] = memberType > 0 ? memberType : '';
	gs.baseParams['fuzzy'] = mrd_search_memberName.getValue();
	gs.baseParams['operateType'] = 2;
	gs.baseParams['chargeType'] = Ext.getCmp('recharge_comboPayType').getValue();
	gs.baseParams['onDuty'] = onDuty;
	gs.baseParams['offDuty'] = offDuty;
	gs.baseParams['total'] = true;
	gs.load({
		params : {
			start : 0,
			limit : GRID_PADDING_LIMIT_20
		}
	});
	//每日充值统计
	recipe_showChart();
}

function changeChartWidth(w,h){
	if(highChart != undefined){
		highChart.setSize(w, h);
	}
	
}

var receipts_setStatisticsDate = function(){
	if(sendToPageOperation){
		Ext.getCmp('receipts_dateSearchDateBegin').setValue(sendToStatisticsPageBeginDate);
		Ext.getCmp('receipts_dateSearchDateEnd').setValue(sendToStatisticsPageEndDate);	
		receipt_hours = sendToStatisticsPageHours;
		
		Ext.getCmp('businessReceipt_btnSearch').handler();
		Ext.getCmp('businessReceipt_txtBusinessHourBegin').setText('<font style="color:green; font-size:20px">'+receipt_hours.openingText+'</font>');
		Ext.getCmp('businessReceipt_txtBusinessHourEnd').setText('<font style="color:green; font-size:20px">'+receipt_hours.endingText+'</font>');
		Ext.getCmp('businessReceipt_comboBusinessHour').setValue(sendToStatisticsPageHours.hourComboValue);
		
		sendToPageOperation = false;		
	}

};

var receipt_hours;

Ext.onReady(function(){
	
	southPanel = new Ext.Panel({
		contentEl : 'businessReceiptsChart',
		region : 'south'
	});
	
	initBusinessReceipsGrid({data : null});
	
	new Ext.Panel({
		renderTo : 'divBusinessReceiptStatistics',//渲染到
		id : 'businessReceiptStatisticsPanel',
		//solve不跟随窗口的变化而变化
		width : parseInt(Ext.getDom('divBusinessReceiptStatistics').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divBusinessReceiptStatistics').parentElement.style.height.replace(/px/g,'')),
		layout:'border',
		frame : true, //边框
		items : [receivablesStatResultGrid,southPanel]
	});
	
	businessPanelHeight = receivablesStatResultGrid.getHeight();
	
	var rz = new Ext.Resizable(receivablesStatResultGrid.getEl(), {
        wrap: true, //在构造Resizable时自动在制定的id的外边包裹一层div
        minHeight:100, //限制改变的最小的高度
        pinned:false, //控制可拖动区域的显示状态，false是鼠标悬停在拖拉区域上才出现
        handles: 's',//设置拖拉的方向（n,s,e,w,all...）
        listeners : {
        	resize : function(thiz, w, h, e){
        		panelDrag = true;
        	}
        }
    });
    rz.on('resize', receivablesStatResultGrid.syncSize, receivablesStatResultGrid);//注册事件(作用:将调好的大小传个scope执行)
	
	receivablesStatResultGrid.on('bodyresize', function(e, w, h){
		var chartHeight;
		if(h < businessPanelHeight){
			chartHeight = 250 + (businessPanelHeight - h);
		}else{
			chartHeight = 250 + (h - businessPanelHeight);
		}
		changeChartWidth(w,chartHeight);
		
		if(southPanel.getEl()){
			southPanel.getEl().setTop((h+55)) ;
		}
		
		if(panelDrag){
			southPanel.setHeight(chartHeight);
		}
		
		receivablesStatResultGrid.getEl().parent().setWidth(w);
		receivablesStatResultGrid.doLayout();
		
	});
//	Ext.getCmp('businessReceiptsStatistics').updateStatisticsDate = receipts_setStatisticsDate;
	
});
