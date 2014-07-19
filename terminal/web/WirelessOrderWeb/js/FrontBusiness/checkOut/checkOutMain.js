﻿var co_memberCard = new Ext.form.NumberField({
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
	height : 26,
	items : [{
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;&nbsp;折扣方案:&nbsp;&nbsp;'
	}, {
		xtype : 'combo',
		id : 'comboDiscount',
		labelStyle : 'font-size:14px;font-weight:bold;',
		readOnly : false,
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
	}, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;当前折扣方案:&nbsp;<span id="spanDisplayCurrentDiscount" style="color:rgb(21, 66, 139); font-weight:bold;font-size:18px; ">&nbsp;&nbsp;</span>'
	}, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;服务费:'
	},{
		xtype : 'numberfield',
		id : 'serviceCharge',
		width : 40,
		listeners : {
			'render': {
			    fn: function(c){
			        c.getEl().on(
			            'keyup',
			            function() {
			            	loadTableData();
			            }
			        );
			    },
			    scope: this
			 
			}						
		}
		
	},{
		xtype : 'tbtext',
		text : '%'
	}, '->', {
		text : '返回',
		handler : function(){
			location.href = 'TableSelect.html';
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

Ext.onReady(function() {
	initMainView(
		new Ext.Panel({
			region : 'west',
			width : 360,
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
			    ['菜名', 'displayFoodName', 220] , 
			    ['口味', 'tasteGroup.tastePref', 130] , 
			    ['口味价钱', 'tasteGroup.tastePrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['数量', 'count', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
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
//		checkOutGrid.style = 'backgroundColor:#FFFFFF;border:1px solid #99BBE8;';
		checkOutGrid.stripeRows = true;
		checkOutGrid.getStore().on('load', function(thiz, records){
			if(checkOutGrid.isVisible()){
				for(var i = 0; i < records.length; i++){
					Ext.ux.formatFoodName(records[i], 'displayFoodName', 'name');
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
//	checkOutForm.buttons[0].setDisabled(_s);
//	checkOutForm.buttons[1].setDisabled(_s);
//	checkOutForm.buttons[2].setDisabled(_s);
//	checkOutForm.buttons[3].setDisabled(_s);
//	checkOutForm.buttons[4].setDisabled(_s);
//	checkOutForm.buttons[5].setDisabled(_s);
//	checkOutForm.buttons[6].setDisabled(_s);
//	checkOutForm.buttons[7].setDisabled(_s);
	var $testDoc = $('#divWestPayOrderGeneral input[type=button]');
	for (var i = 0; i < $testDoc.length; i++) {
		if(_s){
			$testDoc[i].setAttribute('disabled', 'disabled');
		}else{
			$testDoc[i].removeAttribute('disabled');
		}
		
	}
//	$('#divWestPayOrderGeneral input[type=button]')[0].setAttribute('disabled', 'disabled');
	
	var btnSave = Ext.getCmp('btnSaveForConfirmCashPayWin');
	if(btnSave) btnSave.setDisabled(_s);
	var btnClose = Ext.getCmp('btnCloseForConfirmCashPayWin');
	if(btnClose) btnClose.setDisabled(_s);
};
