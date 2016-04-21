Ext.ux.businessHourComboData = [['00', '00' ], ['01', '01' ], ['02', '02' ], ['03', '03' ], ['04', '04' ], ['05', '05'], ['06', '06'], ['07', '07'], ['08', '08'], ['09', '09'], ['10', '10'], ['11', '11'], ['12', '12']];

/**
 * 初始化时间工具栏
 * @param {} c
 * @return {}
 */
Ext.ux.initTimeBar = function(c){
	var businessHourData = [[-1,'全天']];
	$.ajax({
		url : '../../OperateBusinessHour.do',
		data : {
			dataSource : 'getByCond'
		},
		type : 'post',
		async : false,
		success : function(jr, status, xhr){
			for(var i = 0; i < jr.root.length; i++){
				businessHourData.push([jr.root[i]['id'], jr.root[i]['name'], jr.root[i]['opening'], jr.root[i]['ending']]);
			}
			businessHourData.push([-2,'自定义']);
		},
		error : function(result, status, xhr){
		}
	});
	
	
	var timeBar;
	//返回一条完整工具栏
	if(c.tbarType == 0){
		timeBar = new Ext.Toolbar({
			id : c.statistic+'TimeBar',
			hidden : false,
			height : 28,
			items : [
				{xtype:'tbtext',text:'&nbsp;日期:'}, c.dateCombo, 
			    {xtype:'tbtext',text:'&nbsp;'},  c.beginDate,
			    {xtype:'tbtext',text:'&nbsp;至&nbsp;'}, c.endDate, 
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
				{xtype : 'tbtext', text : '市别:'},
				{
					xtype : 'combo',
					forceSelection : true,
					width : 90,
					value : -1,
					id : c.statistic+'comboBusinessHour',
					store : new Ext.data.SimpleStore({
						fields : ['id', 'name']
					}),
					valueField : 'id',
					displayField : 'name',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					readOnly : false,
					listeners : {
						render : function(thiz){
							thiz.store.loadData(businessHourData);
							thiz.setValue(-1);
						},
						select : function(thiz, record, index){
							Ext.ux.statistic_oBusinessHourData({data : record.json, type : 'set', statistic : c.statistic});
							if(c.callback && typeof c.callback == 'function'){
								c.callback();
							}
							if(record.data.id != -2){
								Ext.getCmp(c.statistic+'btnSearch').handler();
							}
							
						}
					}
				},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
				{xtype : 'tbtext', id : c.statistic+'txtBusinessHourBegin', text : '<font style="color:green; font-size:20px">00:00</font>'},
			    buildBusinessAPMCombo(c.statistic+'comboBusinessBeginApm'),
			    buildBusinessHourCombo(c.statistic+'comboBegionHour'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessHourBeginText',hidden : true,text : '时'},
				buildBusinessMinCombo(c.statistic+'comboBegionMin'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessMinBeginText',hidden : true,text : '分'},
				{
					xtype : 'tbtext',
					hidden : false,
					text : '&nbsp;至&nbsp;'
				}, 
				{xtype : 'tbtext', id : c.statistic+'txtBusinessHourEnd', text : '<font style="color:green; font-size:20px">00:00</font>'},
			    buildBusinessAPMCombo(c.statistic+'comboBusinessEndApm'),
				buildBusinessHourCombo(c.statistic+'comboEndHour'),
				{xtype : 'tbtext',id:c.statistic+'txtBusinessHourEndText',hidden : true, text : '时'},
				buildBusinessMinCombo(c.statistic+'comboEndMin'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessMinEndText', hidden : true,text : '分'},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'}
			]
		});
	}else if(c.tbarType == 2){//只返回日期栏
		timeBar = [
				{xtype:'tbtext',text:'&nbsp;日期:'}, c.dateCombo, 
			    {xtype:'tbtext',text:'&nbsp;'},  c.beginDate,
			    {xtype:'tbtext',text:'&nbsp;至&nbsp;'}, c.endDate, 
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'}
				];
	
	}else{ //返回包含时间工具栏的数组用于拼接
		timeBar = [
				{xtype:'tbtext',text:'日期:'}, c.dateCombo, 
			    {xtype:'tbtext',text:'&nbsp;'},  c.beginDate,
			    {xtype:'tbtext',text:'&nbsp;至&nbsp;'}, c.endDate, 
			    {xtype : 'tbtext', text : '&nbsp;&nbsp;'},
			    {xtype : 'tbtext', text : '市别:'},
				{
					xtype : 'combo',
					forceSelection : true,
					width : 90,
					value : -1,
					id : c.statistic+'comboBusinessHour',
					store : new Ext.data.SimpleStore({
						fields : ['id', 'name']
					}),
					valueField : 'id',
					displayField : 'name',
					typeAhead : true,
					mode : 'local',
					triggerAction : 'all',
					selectOnFocus : true,
					readOnly : false,
					listeners : {
						render : function(thiz){
							thiz.store.loadData(businessHourData);
							thiz.setValue(-1);
						},
						select : function(thiz, record, index){
							Ext.ux.statistic_oBusinessHourData({data : record.json, type : 'set', statistic : c.statistic});
							if(c.callback && typeof c.callback == 'function'){
								c.callback();
							}
							if(record.data.id != -2){
								Ext.getCmp(c.statistic+'btnSearch').handler();
							}
							
						}
					}
				},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'},
				{xtype : 'tbtext', id : c.statistic+'txtBusinessHourBegin', text : '<font style="color:green; font-size:20px">00:00</font>'},
			    buildBusinessAPMCombo(c.statistic+'comboBusinessBeginApm'),
				 buildBusinessHourCombo(c.statistic+'comboBegionHour'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessHourBeginText',hidden : true,text : '时'},
				buildBusinessMinCombo(c.statistic+'comboBegionMin'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessMinBeginText',hidden : true,text : '分'},
				{
					xtype : 'tbtext',
					hidden : false,
					text : '&nbsp;至&nbsp;'
				}, 
				{xtype : 'tbtext', id : c.statistic+'txtBusinessHourEnd', text : '<font style="color:green; font-size:20px">00:00</font>'},
			    buildBusinessAPMCombo(c.statistic+'comboBusinessEndApm'),
				 buildBusinessHourCombo(c.statistic+'comboEndHour'),
				{xtype : 'tbtext',id:c.statistic+'txtBusinessHourEndText',hidden : true, text : '时'},
				buildBusinessMinCombo(c.statistic+'comboEndMin'),
				{xtype : 'tbtext', id:c.statistic+'txtBusinessMinEndText', hidden : true,text : '分'},
				{xtype : 'tbtext', text : '&nbsp;&nbsp;'}];
	}

	return timeBar;
};

//生成上下午combo
function buildBusinessAPMCombo(id){
	return {
    		xtype : 'combo',
    		width : 50,
    		value : 0,
    		id : id,
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
	};
			    	
};
//生成小时combo
function buildBusinessHourCombo(id){
	return {
			xtype : 'combo',
			forceSelection : true,
			width : 40,
			value : '00',
			id : id,
			hidden : true,
			store : new Ext.data.SimpleStore({
				fields : [ 'value', 'text' ],
				data : Ext.ux.businessHourComboData
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			readOnly : false
	};
};
//生成分钟combo
function buildBusinessMinCombo(id){
	return {
			xtype : 'combo',
			forceSelection : true,
			width : 40,
			value : '00',
			id : id,
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
	};
};

//市别操作
Ext.ux.statistic_oBusinessHourData = function(c){
	if(c == null || c.type == null || typeof c.type == 'undefined')
		return;
	var data = {};
	var apmBegin = Ext.getCmp(c.statistic+'comboBusinessBeginApm');
	var openingHour = Ext.getCmp((c.statistic+'comboBegionHour'));
	var openingMin = Ext.getCmp((c.statistic+'comboBegionMin'));
	var txtBusinessHourBegin = Ext.getCmp(c.statistic+'txtBusinessHourBegin');
	
	var apmEnd = Ext.getCmp(c.statistic+'comboBusinessEndApm');
	var endingHour = Ext.getCmp(c.statistic+'comboEndHour');
	var endingMin = Ext.getCmp(c.statistic+'comboEndMin');
	var txtBusinessHourEnd = Ext.getCmp(c.statistic+'txtBusinessHourEnd');
	
	
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
		Ext.getCmp(c.statistic+'txtBusinessHourBeginText').hide();
		Ext.getCmp(c.statistic+'txtBusinessHourEndText').hide();
		Ext.getCmp(c.statistic+'txtBusinessMinBeginText').hide();
		Ext.getCmp(c.statistic+'txtBusinessMinEndText').hide();	
		
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
				Ext.getCmp(c.statistic+'txtBusinessHourBeginText').show();
				Ext.getCmp(c.statistic+'txtBusinessHourEndText').show();
				Ext.getCmp(c.statistic+'txtBusinessMinBeginText').show();
				Ext.getCmp(c.statistic+'txtBusinessMinEndText').show();
			}
			
		}
	}else if(c.type == 'get'){
		openingHour = openingHour.getValue();
		endingHour = endingHour.getValue();
		
		data.businessHourType = Ext.getCmp(c.statistic+'comboBusinessHour').getValue();
		
		if(parseInt(data.businessHourType) != -1){
			if(apmBegin.getValue() == 1){
				openingHour = parseInt(openingHour) + 12;
			}
			
			if(apmEnd.getValue() == 1){
				endingHour = parseInt(endingHour) + 12;
			}
			
			data.opening = openingHour + ':' + openingMin.getValue();
			
			data.ending = endingHour + ':' + endingMin.getValue();			
		}else{
			data.opening = '';
			
			data.ending = '';	
		}

		c.data = data;
	}
	return c;
};

