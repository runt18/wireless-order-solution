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
				Ext.Ajax.request({
					url : '../../OperateDiscount.do',
					params : {
						dataSource : 'setDiscount',
						orderId : orderMsg.id, 
						discountId : calcDiscountID 
					},
					success : function(res){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							refreshCheckOutData();
						}else{
							Ext.example.msg(jr.title, jr.msg);
						}
					},
					failure : function(res){}
				});
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
						var defaultId='', reserved='';
						for(var i = 0; i < jr.length; i++){
							data.push([jr[i]['planId'], jr[i]['text']]);
							if(jr[i]['status'] == 2){
								defaultId = jr[i]['planId'];
							}else if(jr[i]['type'] == 2){
								reserved = jr[i]['planId'];
							}
						}
						if(defaultId == ''){
							defaultId = reserved;
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
	}, {
		xtype : 'tbtext',
		text : '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font style="font-size:16px;">人数:</font>'
	},{
		xtype : 'numberfield',
		id : 'numCustomNum',
		width : 50
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
	items : [checkOutMainPanel],
	buttonAlign : 'center',
	buttons : [],
	listeners : {
		afterlayout : function(thiz) {
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

function renderTimeFormat(v){
	return v.substring(10);
}

Ext.onReady(function() {
	Ext.getDom('remark').value="";
	
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
			    ['菜名', 'displayFoodName', 200,,'fnFoodNameFormat'] , 
			    ['口味', 'tasteGroup.tastePref', 100] , 
			    ['口味价钱', 'tasteGroup.tastePrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['数量', 'count', 70, 'right', 'orderCountFormat'],
			    ['单价', 'unitPrice', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['折扣率', 'discount', 70, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['总价', 'totalPrice', 80, 'right', 'Ext.ux.txtFormat.gridDou'],
			    ['时间', 'orderDateFormat', 80,,'renderTimeFormat'],
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
	
	loadWeixinOrderWin();
	
	//快捷键
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
			if(isMixedPay){
				Ext.getCmp('btnTempMixedPayInputRecipt').handler();
			}else{
				paySubmit(6);
			}
			
		}
	}]);
	
	
	Ext.Ajax.request({
		url : '../../QueryPayType.do',
		params : {dataSource : 'exceptMember'},
		success : function(res){
			var jr = Ext.decode(res.responseText);
			payTypeData = jr.root;
		},
		failure : function(){}
	});
	
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
