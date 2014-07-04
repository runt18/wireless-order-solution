function initAddLevelWin(){
	memberLevelAddWin = new Ext.Window({
		id : 'memberLevelAddWin',
		title : '添加等级',
		closable : false,
		modal : true,
		resizable : false,
		width : 250,
		keys : [{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				memberLevelAddWin.hide();
			}
		}],
		bbar : [{
			text : '删除',
			id : 'btn_memberLevelDel',
			iconCls : 'btn_delete',
			handler : function(){
				deleteMemberLevel();
			}
		},'->',{
			text : '保存',
			id : 'btn_memberLevelAdd',
			iconCls : 'btn_save',
			handler : function(){
				var pointThreshold = Ext.getCmp('txtPointThreshold');
				var combo_memberTypeId = Ext.getCmp('combo_memberLevel_mType');
				
				if(!pointThreshold.isValid() || !combo_memberTypeId.isValid()){
					return;
				}
				Ext.Ajax.request({
					url : '../../OperateMemberLevel.do',
					params : {
						id : Ext.getCmp('txtMemberLevelId').getValue(),
						pointThreshold : pointThreshold.getValue(),
						memberTypeId : combo_memberTypeId.getValue(),
						dataSource : memberLevelAddWin.otype
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.ux.showMsg(jr);
							memberLevelAddWin.hide();
							mlm_showChart();
						}else{
							Ext.ux.showMsg(jr);
						}
						
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
					}
				});
			}
		}, {
			text : '取消',
			id : 'btn_cancelMmemberLevelWin',
			iconCls : 'btn_close',
			handler : function(){
				memberLevelAddWin.hide();
			}
		}],
		keys: [{
			key : Ext.EventObject.ENTER,
			scope : this,
			fn : function(){
				Ext.getCmp('btn_memberLevelAdd').handler();
			}
		},{
			key : Ext.EventObject.ESC,
			scope : this,
			fn : function(){
				memberLevelAddWin.hide();
			}
		}],
		listeners : {
			show : function(){
				var data = {};
				if(memberLevelAddWin.otype == 'update'){
					data = memberLevelDetail;
					Ext.getCmp('btn_memberLevelDel').show();
				}else if(memberLevelAddWin.otype == 'insert'){
					data = {};
					Ext.getCmp('btn_memberLevelDel').hide();
				}
				operateMemberLevelData({
					type : 'SET',
					data : data
				});
			},
			hide : function(){
				memberLevelDetail="";
			}
		},
		items : [{
			layout : 'form',
			labelWidth : 60,
			width : 250,
			border : false,
			frame : true,
			items : [{
				xtype : 'numberfield',
				fieldLabel : '积分',
				id : 'txtPointThreshold',
				allowBlank : false,
				width : 130,
				validator : function(v){
					if(Ext.util.Format.trim(v).length > 0){
						return true;
					}else{
						return '积分不能为空.';
					}
				}
			},{
				xtype : 'combo',
				id : 'combo_memberLevel_mType',
				fieldLabel : '会员类型',
				readOnly : false,
				forceSelection : true,
				width : 130,
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
				allowBlank : false
			}, {
				xtype : 'hidden',
				id : 'txtMemberLevelId'
			}]
		}]
	});
}
function initMemberTypeData(){
	combo_memberTypeData = [];
	var thiz = Ext.getCmp('combo_memberLevel_mType');
	Ext.Ajax.request({
		url : '../../QueryMemberType.do',
		params : {
			dataSource : 'notBelongType'
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			for(var i = 0; i < jr.root.length; i++){
				combo_memberTypeData.push([jr.root[i]['id'], jr.root[i]['name']]);
			}
			thiz.store.loadData(combo_memberTypeData);
		},
		fialure : function(res, opt){
			thiz.store.loadData(combo_memberTypeData);
		}
	});
}

function operateMemberLevelData(c){
	if(c == null || typeof c == 'undefined' || typeof c.type == 'undefined'){
		return;
	}
	var data = {};
	var pointThreshold = Ext.getCmp('txtPointThreshold');
	var combo_memberTypeId = Ext.getCmp('combo_memberLevel_mType');
	var memberLevelId = Ext.getCmp('txtMemberLevelId');
	if(c.type.toUpperCase() == Ext.ux.otype['set'].toUpperCase()){
		data = c.data;
		pointThreshold.setValue(data['pointThreshold']);
		
		if(data['memberTypeId']){
			combo_memberTypeId.setValue(data['memberTypeId']);
		}else{
			combo_memberTypeId.setValue();
		}
		memberLevelId.setValue(data['id']);
		pointThreshold.focus(true, 100);
		combo_memberTypeId.clearInvalid();
		
	}else if(c.type.toUpperCase() == Ext.ux.otype['get'].toUpperCase()){
		data = {
			memberLevelId : memberLevelId.getValue(),
			memberTypeId : combo_memberTypeId.getValue(),
			pointThreshold : pointThreshold.getValue()
		};
		c.data = data;
	}
	return c;
}

function operateMemberLevel(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	memberLevelAddWin.otype = c.otype;
	if(c.otype == 'insert'){
		memberLevelAddWin.setTitle('添加等级');
		initMemberTypeData();
	}else if(c.otype == 'update'){
		if(!memberLevelDetail){
			Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
			return;
		}
		memberLevelAddWin.setTitle('修改等级');
	}
	memberLevelAddWin.show();
	memberLevelAddWin.center();
}

function deleteMemberLevel(){
	if(!memberLevelDetail){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	Ext.Msg.confirm(
		'提示',
		'是否删除: ' + memberLevelDetail['xAxisText'],
		function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateMemberLevel.do',
					params : {
						id : memberLevelDetail['id'],
						dataSource : 'delete'
					},
					success : function(res, opt){
						var jr = Ext.util.JSON.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, String.format(Ext.ux.txtFormat.deleteSuccess, memberLevelDetail['xAxisText']));
							memberLevelAddWin.hide();
							mlm_showChart();
							
						}else{
							Ext.ux.showMsg(jr);
						}
					},
					failure : function(res, opt){
						Ext.ux.showMsg(Ext.util.JSON.decode(res.responseText));
					}
				});
			}
		}
	);

}

function chart_operateMemberLevel(y, x){
	initMemberTypeData();
	var list = memberLevels;
	for (var i = 0; i < list.length; i++) {
		if(list[i].pointThreshold == y){
			memberLevelDetail = list[i];
			memberLevelDetail['xAxisText'] = x;
			combo_memberTypeData.push([memberLevelDetail['memberTypeId'], memberLevelDetail['memberTypeName']]);
			Ext.getCmp('combo_memberLevel_mType').store.loadData(combo_memberTypeData);
		}
	}
}
function mlm_showChart(){
	var xAxisData, yAxisData = [];
	$.post('../../QueryMemberLevel.do', {dataSource : 'chart'}, function(res){
		memberLevels = res.root;
		chartDatas = eval('(' + res.other.chart + ')');
		xAxisData = chartDatas.xAxis;
		for (var i = 0; i < chartDatas.data.length; i++) {
			yAxisData.push({y : chartDatas.data[i], color : colors[i]});
		}
		var chart = {
	        chart: {  
	        	type: 'column',
	        	renderTo: 'div_memberLevelChart'
	    	}, 
	    	plotOptions : {
				column : {
					allowPointSelect :true,
					cursor : 'pointer',
					dataLabels : {
						enabled : true,
						style : {
							fontWeight: 'bold', 
							color: 'green' 
						}
					},
					events : {
						click : function(e){
							chart_operateMemberLevel(e.point.y, e.point.category);
							operateMemberLevel({otype:'update'});
						}
					}
				}
			},
	        title: {
	            text: '<b>会员等级图</b>'
	        },
	        xAxis: {
	            categories: xAxisData,
	            labels : {
	            	formatter : function(){
	            		return '<b>' + this.value + '</b>';
	            	}
	            }
	        },
	        yAxis: {
	            title: {
	                text: '积分'
	            },
	            plotLines: [{
	                value: 0,
	                width: 2,
	                color: '#808080'
	            }]
	        },
	        tooltip: {
	            formatter: function() {
	                return this.x +': '+ '<b>'+this.y+'分</b> ';
	            }
	        },
	        series : [{
	        	name : '会员升级',
	        	data : yAxisData
	        }],
	        exporting : {
	        	enabled : false
	        },
	        credits : {
	        	enabled : false
	        }
		};
		new Highcharts.Chart(chart);
	});

}

Ext.onReady(function(){
	initAddLevelWin();
	mlm_showChart();
});
