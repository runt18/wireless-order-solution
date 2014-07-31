var co_memberCard = new Ext.form.NumberField({
	width : 100,
	inputType : 'password',
	style : 'text-align:left;font-weight: bold;color: #FF0000;',
	maxLength : 10,
	maxLengthText : '请输入10位会员卡号',
	minLength : 10,
	minLengthText : '请输入10位会员卡号',
	allowBlank : false,
	blankText : '会员卡不能为空, 请刷卡.'
});

var checkOutMainPanelTbar = new Ext.Toolbar({
	height : 30,
	items : [{
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;<font style="font-size:18px;">当前折扣方案:</font>&nbsp;<span id="spanDisplayCurrentDiscount" style="color:rgb(21, 66, 139); font-weight:bold;font-size:18px; ">&nbsp;&nbsp;</span>'
	}, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;&nbsp;<font style="font-size:18px;">折扣方案:</font>&nbsp;&nbsp;'
	}, {
		xtype : 'combo',
		id : 'comboDiscount',
		labelStyle : 'font-size:14px;font-weight:bold;',
		readOnly : false,
		width : 120,
		forceSelection : true,
		store : new Ext.data.JsonStore({
			root : 'root',
			fields : [ 'id', 'name']
		}),
		valueField : 'id',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			select : function(thiz, record, index) {
				calcDiscountID = thiz.getValue();
				refreshCheckOutData();
			}
		}
	},{
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font style="font-size:18px;">当前服务费:</font>&nbsp;<span id="spanDisplayCurrentServiceRate" style="color:rgb(21, 66, 139); font-weight:bold;font-size:18px; ">&nbsp;&nbsp;</span>'
	}, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;<font style="font-size:18px;">服务费方案:</font>'
	},{
		xtype : 'combo',
		id : 'comboServicePlan',
		width : 100,
		readOnly : false,
		forceSelection : true,
		store : new Ext.data.SimpleStore({
			fields : ['planId', 'name']
		}),
		valueField : 'planId',
		displayField : 'name',
		typeAhead : true,
		mode : 'local',
		triggerAction : 'all',
		selectOnFocus : true,
		listeners : {
			render : function(thiz){
				var data = [];
				Ext.Ajax.request({
					url : '../../QueryServicePlan.do',
					params : {dataSource : 'planTree'},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						var defaultId='';
						for(var i = 0; i < jr.length; i++){
							data.push([jr[i]['planId'], jr[i]['text']]);
							if(jr[i]['status'] == 2){
								defaultId = jr[i]['planId'];
							}
						}
						thiz.store.loadData(data);
						
						thiz.setValue(defaultId);
					},
					fialure : function(res, opt){
						thiz.store.loadData(data);
					}
				});
			},
			select : function(){
				loadTableData();
			}
		}
	}]
});

var checkOutMainPanel = new Ext.Panel({
	title : '&nbsp;',
//	width : 1000,
	style : 'margin:0 auto',
	tbar : checkOutMainPanelTbar,
	layout : 'fit',
	frame : true,
	items : [new Ext.Panel({
		xtype : 'panel',
		hidden : true
	})]
});

var checkOutForm = new Ext.form.FormPanel({
	frame : true,
	border : false,
	items : [
	checkOutMainPanel
//	, 
//	{
//		layout : 'column',
//		border : false,
//		items : [{
//			html : '<div>&nbsp;&nbsp;</div>',
//			id : 'placeHolderCOF3',
//			width : 150
//		}, {
//			border : false,
//			contentEl : 'payInfo'
//		} ]
//	}
/*	, {
		layout : 'column',
		border : false,
		items : [ {
			html : '<div>&nbsp;&nbsp;</div>',
			id : 'placeHolderCOF4',
			width : 150
		},{
			layout : 'form',
			border : false,
			labelSeparator : '：',
			labelWidth : 40,
			width : 450,
			items : [ {
				xtype : 'textarea',
				fieldLabel : '备注',
				id : 'remark',
				anchor : '%100'
			} ]
		} ]
	} */
	],
	buttonAlign : 'center',
	buttons : [ 
/*	{
		text : '签单',
		disabled : true,
		handler : function() {
			paySubmit(4);
		}
	}, {
		text : '挂账',
		disabled : true,
		handler : function() {
			paySubmit(5);
		}
	}
	, {
		text : '会员结账',
		disabled : true,
		handler : function() {
			memberPay();
		}
	},{
		text : '现金结账(+)',
		disabled : true,
		handler : function() {
			paySubmit(1);
		}
	}, {
		text : '刷卡结账',
		disabled : true,
		handler : function() {
			paySubmit(2);
		}
	}, {
		text : '会员卡结账',
		hidden : true,
		handler : function() {
			paySubmit(3);
		}
	}, {
		text : '暂结(-)',
		disabled : true,
		handler : function() {
			paySubmit(6);
		}
	}, {
		text : '返回',
		disabled : true,
		handler : function() {
			location.href = 'TableSelect.html';
		}
	}*/
	],
	listeners : {
		afterlayout : function(thiz) {
//			thiz.findById('placeHolderCOF1').setWidth((thiz.getInnerWidth() - 1000) / 2);
//			thiz.findById('placeHolderCOF2').setWidth((thiz.getInnerWidth() - 989) / 2);
//			thiz.findById('placeHolderCOF3').setWidth((thiz.getInnerWidth() - 989) / 2);
//			thiz.findById('placeHolderCOF4').setWidth((thiz.getInnerWidth() - 989) / 2);
			checkOutMainPanel.setHeight(thiz.getInnerHeight());
			if(eval(category == 4)){
				if(tableGroupTab != null && typeof tableGroupTab != 'undefined'){
					tableGroupTab.setHeight(checkOutMainPanel.getInnerHeight());					
				}
			}else{
				if(checkOutGrid != null && typeof checkOutGrid != 'undefined'){
					checkOutGrid.setHeight(checkOutMainPanel.getInnerHeight());					
				}
			}
			checkOutMainPanel.doLayout();	
		}
	}
});

var checkOutCenterPanel = new Ext.Panel({
	region : 'center',
	id : 'checkOutCenterPanel',
	layout : 'fit',
	border : false,
	items : [ checkOutForm ]
});

function orderCountFormat(v, m, r, ri, ci, s){
	if(Ext.ux.cfs.isWeigh(r.get('status'))){
		v = '<font style="color:green; font-size:18px;">' + v + '</font>';
	}
	return v;
}

function fnFoodNameFormat(v, m, r, ri, ci, s){
	if(Ext.ux.cfs.isWeigh(r.get('status'))){
		v = '<font style="color:green; font-size:18px;font-weight:bold">' + v + '&nbsp;</font>'+'[称重确认]';
	}
	return v;
}

Ext.onReady(function() {
	initMainView(
		new Ext.Panel({
			region : 'west',
			width : 400,
			frame : true,
			title : '收款',
			contentEl : 'divWestPayOrderGeneral'
		})
	, new Ext.Panel({
		id : 'centerPanelDO',
		region : 'center',
		border : false,
		margins : '0 0 0 0',
		layout : 'border',
		items : [ checkOutCenterPanel ]
	}), null);
	
	
	if(eval(category == 4)){
		tableGroupTab = new Ext.TabPanel({
			enableTabScroll : true,
			height : checkOutMainPanel.getInnerHeight(),
			activeTab : 0,
			listeners : {
				tabchange : function(thiz, stab){
					for(var i = 0; i < stab.getStore().getCount(); i++){
						if(i % 2 == 0){
							stab.getView().getRow(i).style.backgroundColor = '#DDD';
						}
					}
				}
			}
		});
		checkOutMainPanel.add(tableGroupTab);
		checkOutMainPanel.doLayout();
	}else{
		checkOutGrid = createGridPanel(
			'',
			'',
			checkOutMainPanel.getInnerHeight(),
		    '',
		    '',
		    [
			    [true, false, false, false], 
			    ['菜名', 'displayFoodName', 220,,'fnFoodNameFormat'] , 
			    ['口味', 'tasteGroup.tastePref', 130] , 
			    ['口味价钱', 'tasteGroup.tastePrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['数量', 'count', 70, 'right', 'orderCountFormat'],
			    ['单价', 'unitPrice', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['折扣率', 'discount', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['总价', 'totalPrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['时间', 'orderDateFormat', 130],
			    ['服务员', 'waiter', 80]
			],
			OrderFoodRecord.getKeys(),
		    [],
		    30,
		    ''
		);
		checkOutGrid.frame = false;
		checkOutGrid.stripeRows = true;
		checkOutGrid.getStore().on('load', function(thiz, records){
			if(checkOutGrid.isVisible()){
				for(var i = 0; i < records.length; i++){
					Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name', 1);
					if(Ext.ux.cfs.isWeigh(records[i].get('status'))){
						checkOutGrid.getView().getRow(i).style.backgroundColor = 'SkyBlue';
					}
						
				}				
			}
		});
		
		
		// 加载界面
		checkOutMainPanel.add(checkOutGrid);
		checkOutMainPanel.doLayout();
	}
	
	if(Ext.ux.getCookie(document.domain+'_calcReturn') == 'true'){
		Ext.getDom('chkCalcReturn').checked = true;
	}else{
		Ext.getDom('chkCalcReturn').checked = false;
	}	
	
	new Ext.KeyMap(document.body, [{
		key: 107,
		scope : this,
		fn: function(){
			fnRemberIsFastOrInput();
		}
	}, {
		key: 109,
		scope : this,
		fn: function(){
			paySubmit(6);
		}
	}]);
	Ext.ux.checkSmStat();
	
});

function setFormButtonStatus(_s){
	var $testDoc = $('#divWestPayOrderGeneral input[type=button]');
	for (var i = 0; i < $testDoc.length; i++) {
		if(_s){
			$testDoc[i].setAttribute('disabled', 'disabled');
		}else{
			$testDoc[i].removeAttribute('disabled');
		}
		
	}
	
	var btnSave = Ext.getCmp('btnSaveForConfirmCashPayWin');
	if(btnSave) btnSave.setDisabled(_s);
	var btnClose = Ext.getCmp('btnCloseForConfirmCashPayWin');
	if(btnClose) btnClose.setDisabled(_s);
};
