var pushBackBut = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'BasicMgrProtal.html?'+strEncode("restaurantID=" + restaurantID, "mi");
	}
});

var logOutBut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});
 
//var json = {items : [{xtype : "checkbox", name : 'pType',boxLabel : '下单详细',hideLabel : true,inputValue : 2}]};

/*var jsons = '{items : [{xtype : "checkbox", name : "pType",boxLabel : "cccc", hideLabel : true, inputValue : ' + id + '}]},' + 
			'{items : [{xtype : "checkbox", name : "pType",boxLabel : "hhhh", hideLabel : true, inputValue : ' + id + '}]},' +
			'{items : [{xtype : "checkbox", name : "pType",boxLabel : "gggg", hideLabel : true, inputValue : ' + id + '}]}';*/

Ext.Ajax.request({
	url : '../../QueryKitchen.do',
	params : {
		dataSource : 'normal',
	},
	success : function(res, opt){
		var jr = Ext.decode(res.responseText);
		
		if(jr.success){
			for ( var i = 0; i < jr.root.length; i++) {
				
				var k = jr.root[i];
				var c = {items : [{xtype : "checkbox", name : "kitchen",boxLabel : k.name , hideLabel : true, inputValue :  k.alias }]};
				
				Ext.getCmp('allKitchen').add(c);
				//solveIE换行时格式错乱
				if((i+1)%6 == 0){
					Ext.getCmp('allKitchen').add({columnWidth : 1});
				}
				Ext.getCmp('allKitchen').doLayout();
			
			}
			
		}else{
			Ext.ux.showMsg(jr);
		}

	},
	failure : function(res, opt){
		Ext.ux.showMsg(Ext.decode(res.responseText));
	}

	
});
Ext.Ajax.request({
	url : '../../QueryDept.do',
	params : {
		dataSource : 'normal',
	},
	success : function(res, opt){
		var jr = Ext.decode(res.responseText);
		
		if(jr.success){
			for ( var i = 0; i < jr.root.length; i++) {
				
				var d = jr.root[i];
				var c = {items : [{xtype : "radio", name : "dept",boxLabel : d.name , hideLabel : true, inputValue :  d.id }]};
				
				Ext.getCmp('allDept').add(c);
				Ext.getCmp('allDept').doLayout();
			
			}
			
		}else{
			Ext.ux.showMsg(jr);
		}

	},
	failure : function(res, opt){
		Ext.ux.showMsg(Ext.decode(res.responseText));
	}

	
});

Ext.Ajax.request({
	url : '../../QueryRegion.do',
	params : {
		dataSource : 'normal',
	},
	success : function(res, opt){
		var jr = Ext.decode(res.responseText);
		
		if(jr.success){
			for ( var i = 0; i < jr.root.length; i++) {

				var r = jr.root[i];
				var c = {items : [{xtype : "checkbox", name : "region", boxLabel : r.name , hideLabel : true, inputValue :  r.id }]};

				Ext.getCmp('allRegion').add(c);
				if((i+1)%6 == 0){
					Ext.getCmp('allRegion').add({columnWidth : 1});
				}
				Ext.getCmp('allRegion').doLayout();
			
			}
			
		}else{
			Ext.ux.showMsg(jr);
		}

	},
	failure : function(res, opt){
		Ext.ux.showMsg(Ext.decode(res.responseText));
	}

	
});


var addPrintFunc = new Ext.Window({
	title : '添加方案',
	id : 'addPrintFuncWin',
	closable : false,
	resizable : true,
	modal : true,
	width : 650,
	autoHeight : true,
	bbar : [{
		text : '应用',
		id : 'btnSaveNext',
		iconCls : 'btn_app',
		handler : function(){
			if(!Ext.getCmp('txtRepeat').isValid()){
				return;
			}
			
			var pType = document.getElementsByName('pType');

			for ( var i = 0; i < pType.length; i++) {
				if(pType[i].checked){
					pType = pType[i].value;
					break;
				}
			}
			var allKitchen = Ext.getDom('chkAllKitchen');
			var kitchens = '';
			
			if(!allKitchen.checked){
				var kitchen = document.getElementsByName('kitchen');
				//添加厨房
				for ( var i = 0; i < kitchen.length; i++) {
					if(kitchen[i].checked && kitchens == ''){
						kitchens += kitchen[i].value;
					}else if(kitchen[i].checked && kitchens != ''){
						kitchens += (',' + kitchen[i].value);
					}
				}

			}
			
			
			var allDept = Ext.getDom('chkAllDept');
			var depts = '';
			if(!allDept.checked){
				var dept = document.getElementsByName('dept');
				//添加部门
				for ( var i = 0; i < dept.length; i++) {
					if(dept[i].checked){
						depts += dept[i].value;
					}
				}
			}


			var allRegion = Ext.getDom('chkAllRegion');
			var regions = '';
			if(!allRegion.checked){
				var region = document.getElementsByName('region');
				for ( var i = 0; i < region.length; i++) {
					if(region[i].checked && regions == ''){
						regions += region[i].value;
					}else if(region[i].checked && regions != ''){
						regions += (',' + region[i].value);
					}
				}
			}
			
			var sn = Ext.getCmp('printerTree').getSelectionModel().getSelectedNode();
			var repeat = Ext.getCmp('txtRepeat').getValue();
			Ext.Ajax.request({
				url : '../../OperatePrintFunc.do',
				params : {
					repeat : repeat,
					pType : pType,
					kitchens : kitchens,
					dept : depts,
					regions : regions,
					printerId : sn.attributes.printerId,
					dataSource : 'insert'
				},
				success : function(res, opt){
					var jr = Ext.decode(res.responseText);
					if(jr.success){
						Ext.getCmp('grid').store.reload();
						Ext.example.msg(jr.title, jr.msg);
					}else{
						Ext.ux.showMsg(jr);
					}
				},
				failure : function(res, opt){
					Ext.ux.showMsg(Ext.decode(res.responseText));
				}
			});
			
			
		}
		
	},'->',{
		text : '保存',
		id : 'btnSaveFunc',
		iconCls : 'btn_save',
		handler : function(e){
			if(!Ext.getCmp('txtRepeat').isValid()){
				return;
			}

			var pType = document.getElementsByName('pType');

			for ( var i = 0; i < pType.length; i++) {
				if(pType[i].checked){
					pType = pType[i].value;
					break;
				}
			}
			var allKitchen = Ext.getDom('chkAllKitchen');
			var kitchens = '';
			
			if(!allKitchen.checked){
				var kitchen = document.getElementsByName('kitchen');
				//添加厨房
				for ( var i = 0; i < kitchen.length; i++) {
					if(kitchen[i].checked && kitchens == ''){
						kitchens += kitchen[i].value;
					}else if(kitchen[i].checked && kitchens != ''){
						kitchens += (',' + kitchen[i].value);
					}
				}

			}
			
			
			var allDept = Ext.getDom('chkAllDept');
			var depts = '';
			if(!allDept.checked){
				var dept = document.getElementsByName('dept');
				//添加部门
				for ( var i = 0; i < dept.length; i++) {
					if(dept[i].checked){
						depts += dept[i].value;
					}
				}
			}


			var allRegion = Ext.getDom('chkAllRegion');
			var regions = '';
			if(!allRegion.checked){
				var region = document.getElementsByName('region');
				for ( var i = 0; i < region.length; i++) {
					if(region[i].checked && regions == ''){
						regions += region[i].value;
					}else if(region[i].checked && regions != ''){
						regions += (',' + region[i].value);
					}
				}
			}
			
			
			
			var dataSource = '';
			var funcId = '';
			if(addPrintFunc.operationType == 'insert'){
				
				dataSource = 'insert';
			}
			else if(addPrintFunc.operationType == 'update'){
				var ss = Ext.getCmp('grid').getSelectionModel().getSelected();
				funcId = ss.data.printFuncId;
				dataSource = 'update';
			}
			else return;
			
			
			var sn = Ext.getCmp('printerTree').getSelectionModel().getSelectedNode();
			var repeat = Ext.getCmp('txtRepeat').getValue();
			Ext.Ajax.request({
				url : '../../OperatePrintFunc.do',
				params : {
					repeat : repeat,
					pType : pType,
					kitchens : kitchens,
					dept : depts,
					regions : regions,
					printerId : sn.attributes.printerId,
					funcId : funcId,
					dataSource : dataSource
				},
				success : function(res, opt){
					var jr = Ext.decode(res.responseText);
					if(jr.success){
						Ext.getCmp('grid').store.reload();
						Ext.example.msg(jr.title, jr.msg);
					}else{
						Ext.ux.showMsg(jr);
					}
					addPrintFunc.hide();
				},
				failure : function(res, opt){
					Ext.ux.showMsg(Ext.decode(res.responseText));
				}
			});
			
	
		}		
		
	},{
		text : '取消',
		id : 'btnCloseFunc',
		iconCls : 'btn_close',
		handler : function(){
			addPrintFunc.hide();
		}
	}],
	items : [{
		layout : 'column',
		frame : true,
		defaults : {
			columnWidth : .16,
			layout : 'form',
			labelWidth : 70,
		},
		items : [{
			columnWidth : 0.4,
			items : [{
				xtype : 'tbtext',
				text : String.format(
					Ext.ux.txtFormat.attrName,
					'打印机','lblPrinterName',''
				)
			}]
		}, {
			columnWidth : 0.5,
			items : [{
				xtype : 'textfield',
				id : 'txtRepeat',
				fieldLabel : '打印数',
				width : 100,
				allowBlank : false,
				listeners : {
					focus : function(thiz){
						thiz.focus(true, 100);
					}
				}
			}]
		}]
	},{
		layout : 'column',
		id : 'printerType',
		frame : true,
		defaults : {
			columnWidth : .16,
			layout : 'form',
			labelWidth : 70,
		},
		items : [{
			columnWidth : 1,
			xtype : 'label',
			text : '请选择功能:'
		},{
			
			items : [{
				xtype : 'radio',
				name : 'pType',
				id : 'radioOrder',
				inputValue : 1,
				hideLabel : true,
				boxLabel : '下单',
				listeners : {
					check  : function(thiz, checked){
						if(checked){
							showPanel(thiz.inputValue);
						}
					}
				}
			}]
		},{
			items : [{
				xtype : 'radio',
				name : 'pType',
				inputValue : 2,
				hideLabel : true,
				boxLabel : '下单详细',
				listeners : {
					check  : function(thiz, checked){
						if(checked){
							showPanel(thiz.inputValue);
						}
					}
				}
			}]
			
		},{
			items : [{
				xtype : 'radio',
				name : 'pType',
				inputValue : 8,
				hideLabel : true,
				boxLabel : '退菜',
				listeners : {
					check  : function(thiz, checked){
						if(checked){
							showPanel(thiz.inputValue);
							document.getElementById('chkAllRegion').checked = true;
							return ;
							
						}
					},
				}
			}]
		},{
			items : [{
				xtype : 'radio',
				name : 'pType',
				inputValue : 5,
				hideLabel : true,
				boxLabel : '退菜详细',
				listeners : {
					check  : function(thiz, checked){
						if(checked){
							showPanel(thiz.inputValue);
						}
					}
				}
			}]
			
		},{
			items : [{
				xtype : 'radio',
				name : 'pType',
				inputValue : 3,
				hideLabel : true,
				boxLabel : '结账',
				listeners : {
					check  : function(thiz, checked){
						if(checked){
							showPanel(thiz.inputValue);
							die;
						}
					}
				} 
			}]
		},{
			items : [{
				xtype : 'radio',
				name : 'pType',
				inputValue : 127,
				hideLabel : true,
				boxLabel : '暂结',
				listeners : {
					check  : function(thiz, checked){
						if(checked){
							showPanel(thiz.inputValue);
							die;
						}
					}
				}
			}]
			
		},{
			items : [{
				xtype : 'radio',
				name : 'pType',
				inputValue : 6,
				hideLabel : true,
				boxLabel : '转台',
				listeners : {
					check  : function(thiz, checked){
						if(checked){
							showPanel(thiz.inputValue);
							die;
						}
					}
				}
			}]
		},{
			items : [{
				xtype : 'radio',
				name : 'pType',
				inputValue : 9,
				hideLabel : true,
				boxLabel : '催菜',
				listeners : {
					check  : function(thiz, checked){
						if(checked){
							showPanel(thiz.inputValue);
							die;
						}
					}
				}
			}]
			
		}]
	},{
		layout : 'column',
		id : 'kitchens',
		frame : true,
		defaults : {
			columnWidth : .16,
			layout : 'form',
			labelWidth : 70,
		},
		items : [{
			columnWidth : 1,
			xtype : 'label',
			text : '请选择厨房:'
		},{
			items : [{
				xtype : 'checkbox',
				name : 'kitchens',
				id : 'chkAllKitchen',
				hideLabel : true,
				boxLabel : '所有厨房',
				listeners : {
					focus : function(){
						//第一次初始化控件时,解决点击无效
						Ext.getCmp('allKitchen').enable();
					},
					check : function(checkbox, checked){
						if(checked){
							Ext.getCmp('allKitchen').disable();
						}else{
							Ext.getCmp('allKitchen').enable();
							
						}
						
					}
				}
			}]	
		},{
			//所有厨房
			columnWidth : 1,
			layout : 'column',
			id : 'allKitchen',
			defaults : {
				columnWidth : .16,
				layout : 'form',
				labelWidth : 70,
			}
		}]
	},{
		layout : 'column',
		id : 'depts',
		frame : true,
		defaults : {
			columnWidth : .16,
			layout : 'form',
			labelWidth : 70,
		},
		items : [{
			columnWidth : 1,
			xtype : 'label',
			text : '请选择部门:'
		},{
			items : [{
				xtype : 'checkbox',
				name : 'allDepts',
				id : 'chkAllDept',
				hideLabel : true,
				boxLabel : '所有部门',
				listeners : {
					focus : function(){
						Ext.getCmp('allDept').enable();
					},
					check : function(checkbox, checked){
						if(checked){
							Ext.getCmp('allDept').disable();
						}else{
							Ext.getCmp('allDept').enable();
						}
						
					}
				}
			}]
		},{
			//所有部门
			columnWidth : 1,
			layout : 'column',
			id : 'allDept',
			defaults : {
				columnWidth : .16,
				layout : 'form',
				labelWidth : 70,
			}
		}]
	},{
		layout : 'column',
		id : 'regions',
		frame : true,
		defaults : {
			columnWidth : .16,
			layout : 'form',
			labelWidth : 70,
		},
		items : [{
			columnWidth : 1,
			xtype : 'label',
			text : '请选择区域:'
		},{
			items : [{
				xtype : 'checkbox',
				name : 'pType',
				id : 'chkAllRegion',
				hideLabel : true,
				boxLabel : '所有区域',
				listeners : {
					focus : function(){
						Ext.getCmp('allRegion').enable();
					},
					check : function(checkbox, checked){
						if(checked){
							Ext.getCmp('allRegion').disable();
						}else{
							Ext.getCmp('allRegion').enable();
						}
						
					}
				}
			}]
		},{
			//所有区域
			columnWidth : 1,
			layout : 'column',
			id : 'allRegion',
			defaults : {
				columnWidth : .16,
				layout : 'form',
				labelWidth : 70,
			}
		}]
	}]
	
});

function showPanel(v){

	if(v == 1 || v ==8){
		Ext.getCmp('kitchens').hide();
		Ext.getCmp('depts').show();
		Ext.getCmp('regions').show();
	}else if(v == 2 || v == 5){
		Ext.getCmp('kitchens').show();
		Ext.getCmp('depts').hide();
		Ext.getCmp('regions').hide();
	}else{
		Ext.getCmp('kitchens').hide();
		Ext.getCmp('depts').hide();
		Ext.getCmp('regions').show();
	}
	//solve切换时格式错乱
	Ext.getCmp('addPrintFuncWin').center();

} 


var printerWin = new Ext.Window({
	title : '添加打印机',
	closable : false,
	resizable : true, 
	modal : true,
	width : 260,
	bbar : ['->',{
		text : '保存',
		id : 'btnSavePrinter',
		iconCls : 'btn_save',
		handler : function(e){
			var printerName = Ext.getCmp('txtPrinterName');
			var printerAlias = Ext.getCmp('txtPrinterAlias');
			var isEnabled = Ext.getCmp('enabled');
			var styles = document.getElementsByName('pStyle');
			
			var dataSource = '';
			var printerId = '';
			var style = '';
			
			for ( var i = 0; i < styles.length; i++) {
				if(styles[i].checked){
					style = styles[i].value;
				}
			}
			if(isEnabled.checked){
				isEnabled = 'true';
			}else{
				isEnabled = 'false';
			}
			if(!Ext.getCmp('txtPrinterName').isValid()){
				return;
			}
			if(printerWin.operationType == 'insert'){
				dataSource = 'insert';
			}else if(printerWin.operationType == 'update'){
				var sn = Ext.getCmp('printerTree').getSelectionModel().getSelectedNode();
				printerId = sn.attributes.printerId;
				dataSource = 'update';
			}
			else return;
			Ext.Ajax.request({
				url : '../../OperatePrinter.do',
				params : {
					printerName : printerName.getValue(),
					printerAlias : printerAlias.getValue(),
					style : style,
					printerId : printerId,
					isEnabled : isEnabled,
					dataSource : dataSource
				},
				success : function(res, opt){
					var jr = Ext.decode(res.responseText);
					printerWin.hide();
					Ext.ux.showMsg(jr);
					printerTree.getRootNode().reload();
				},
				failure : function(res, opt){
					Ext.ux.showMsg(Ext.decode(res.responseText));
				}
			});
			
			
			
		}
	},{
		text : '取消',
		id : 'btnCloseFunc',
		iconCls : 'btn_close',
		handler : function(){
			printerWin.hide();
		}
	}],
	items : [{
		xtype : 'form',
		labelAlign : 'left',
		labelWidth : 70,
		frame : true,
		defaultType : 'textfield',
		items : [{
			id : 'txtPrinterName',
			fieldLabel : '打印机',
			width : 130,
			allowBlank : false
		},{
			id : 'txtPrinterAlias',
			fieldLabel : '别名',
			width : 130
		},{
			xtype : 'fieldset',
			title : '机型',
			autoHeight : true,
			defaultType : 'radio',
			hideLabels : true,
			items : [{
				name : 'pStyle',
				inputValue : 2,
				id : 'rdo80mm',
				checked : true,
				hideLabel : true,
				boxLabel : '80mm'
				
			},{
				xtype : 'radio',
				name : 'pStyle',
				inputValue : 1,
				hideLabel : true,
				boxLabel : '50mm'
			}]
			
		},{
			xtype : 'fieldset',
			title : '状态',
			autoHeight : true,
			defaultType : 'radio',
			hideLabels : true,
			items : [{
				id : 'enabled',
				name : 'isEnabled',
				inputValue : 'true',
				hideLabel : true,
				checked : true,
				boxLabel : '可用',
			},{
				id : 'unEnabled',
				name : 'isEnabled',
				inputValue : 'false',
				hideLabel : true,
				boxLabel : '停用',
			}]
		}]
	}]
	
});


function deletePrintFuncOperationHandler(){
	var ss = Ext.getCmp('grid').getSelectionModel().getSelected();
	if(ss != null){
		Ext.Msg.confirm(
			'提示',
			'是否删除方案?',
			function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperatePrintFunc.do',
						params : {
							printFuncId : ss.data.printFuncId,
							dataSource : 'delete'
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.getCmp('grid').store.reload();
								Ext.example.msg(jr.title, jr.msg);
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
						
					
						
					});
				}
			}
		);
	}else{
		Ext.MessageBox.show('提示', '请先选择一个方案再删除');
	}
	
	
}

function printFuncOperactionHandler(c){
	if(c.type == 'undefined'){
		return;
	}
	var pType = document.getElementsByName('pType');
	var kitchen = document.getElementsByName('kitchen');
	var dept = document.getElementsByName('dept');
	var region = document.getElementsByName('region');
	var repeat = Ext.getCmp('txtRepeat');
	//区域重置
	for ( var i = 0; i < region.length; i++) {
		if(region[i].checked){
			region[i].checked = false;
		}
	}
	//厨房重置
	for ( var i = 0; i < kitchen.length; i++) {
		if(kitchen[i].checked){
			kitchen[i].checked = false;
		}
	}
	addPrintFunc.show();
	
	var sn = printerTree.getSelectionModel().getSelectedNode();
	Ext.getDom('lblPrinterName').innerHTML = sn.attributes.name + " " + sn.attributes.alias;
	if(c.type == 'insert'){
		addPrintFunc.setTitle('添加方案');
		addPrintFunc.operationType = c.type;
		Ext.getCmp('txtRepeat').setValue(1);
		
		Ext.getCmp('printerType').show();
		Ext.getCmp('btnSaveNext').show();
		
		document.getElementById('radioOrder').checked = true;
		Ext.getCmp('radioOrder').fireEvent('check', Ext.getCmp('radioOrder'), true);
		//document.getElementsByName('dept')[0].checked = true;
		Ext.getCmp('chkAllDept').fireEvent('check', Ext.getCmp('chkAllDept'), true);
		Ext.getCmp('chkAllRegion').fireEvent('check', Ext.getCmp('chkAllRegion'), true);
		Ext.getCmp('chkAllKitchen').fireEvent('check', Ext.getCmp('chkAllKitchen'), true);
		
		Ext.getDom('chkAllDept').checked = true;
		Ext.getDom('chkAllRegion').checked = true;
		Ext.getDom('chkAllKitchen').checked = true;
		
		
		
	}else{
		var ss = Ext.getCmp('grid').getSelectionModel().getSelected();
		
		if(ss == null){
			Ext.MessageBox.alert('提示', '请选中一个方案再进行操作.');
			return;
		}
		if(c.type == 'update'){
			addPrintFunc.setTitle('修改方案');
		}else{
			addPrintFunc.setTitle('查看方案');
		}
		
		addPrintFunc.operationType = c.type;
		Ext.getCmp('printerType').hide();
		Ext.getCmp('btnSaveNext').hide();
		//功能选中
		var pTypeValue = ss.data.pTypeValue;
		for ( var i = 0; i < pType.length; i++) {
			if(pType[i].value == pTypeValue){
				pType[i].checked = true;
				pType[i].click();
			}
		}
		

		
		//厨房选中
		
		if(!ss.data.kitchenValues == ''){
			Ext.getDom('chkAllKitchen').checked = false;
			Ext.getCmp('chkAllKitchen').fireEvent('check', Ext.getCmp('chkAllKitchen'), false);
			var kitchenValues = ss.data.kitchenValues.split(",");
			for ( var i = 0; i < kitchen.length; i++) {
				for ( var j = 0; j < kitchenValues.length; j++) {
					if(kitchenValues[j] == kitchen[i].value){
						kitchen[i].checked = true;
					}
				}
			}
		}else{
			Ext.getDom('chkAllKitchen').checked = true;
			Ext.getCmp('chkAllKitchen').fireEvent('check', Ext.getCmp('chkAllKitchen'), true);
		}
		
		

		//部门选中
		var deptValue = ss.data.deptValue;
		if(!deptValue == ''){
			Ext.getDom('chkAllDept').checked = false;
			Ext.getCmp('chkAllDept').fireEvent('check', Ext.getCmp('chkAllDept'), false);
			for ( var i = 0; i < dept.length; i++) {
				if(dept[i].value == deptValue){
					dept[i].checked = true;
					dept[i].click();
				}
			}	
		}else{
			Ext.getDom('chkAllDept').checked = true;
			Ext.getCmp('chkAllDept').fireEvent('check', Ext.getCmp('chkAllDept'), true);
		}

		

		
		//区域选中
		if(ss.data.regionValues == ''){
			Ext.getDom('chkAllRegion').checked = true;
			Ext.getCmp('chkAllRegion').fireEvent('check', Ext.getCmp('chkAllRegion'), true);
		}else{
			Ext.getDom('chkAllRegion').checked = false;
			Ext.getCmp('chkAllRegion').fireEvent('check', Ext.getCmp('chkAllRegion'), false);
			var regionValues = ss.data.regionValues.split(",");
			for ( var i = 0; i < region.length; i++) {
				for ( var j = 0; j < regionValues.length; j++) {
					if(regionValues[j] == region[i].value){
						region[i].checked = true;
					}
				}
			}

		}

		
		repeat.setValue(ss.data.repeat);
	
	}
	addPrintFunc.center();
}

function operatePrinterHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	if(c.otype == 'insert'){
		printerWin.setTitle('添加打印机');
		
		Ext.getCmp('txtPrinterName').setValue();
		Ext.getCmp('txtPrinterName').clearInvalid();
		Ext.getCmp('txtPrinterAlias').setValue();
		Ext.getCmp('txtPrinterAlias').clearInvalid();

		printerWin.operationType = c.otype;
		printerWin.show();
		document.getElementById('enabled').checked = true;
		document.getElementById('rdo80mm').checked = true;
	}else if(c.otype == 'update'){
		var sn = printerTree.getSelectionModel().getSelectedNode();
		if(!sn){
			Ext.example.msg('提示', '请选中一个打印机再进行操作.');
			return;
		}
		printerWin.operationType = c.otype;
		printerWin.show();
		printerWin.setTitle('修改打印机');
		Ext.getCmp('txtPrinterName').setValue(sn.attributes.name);
		Ext.getCmp('txtPrinterAlias').setValue(sn.attributes.alias);
		var styles = document.getElementsByName('pStyle');
	
		if(styles[0].value == sn.attributes.styleValue){
			styles[0].checked = true;
		}else{
			styles[1].checked = true;
		}
		if(sn.attributes.isEnabled ){
			document.getElementById('enabled').checked = true;
		}else{
			document.getElementById('unEnabled').checked = true;
		}
		
		
	}else if(c.otype == 'delete'){
		var sn = printerTree.getSelectionModel().getSelectedNode();
		if(!sn){
			Ext.example.msg('提示', '请选中一个打印机再进行操作.');
			return;
		}
		
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除打印机?',
			icon: Ext.MessageBox.QUESTION,
			buttons : Ext.Msg.YESNO,
			fn : function(e){
				if(e == 'yes'){
					Ext.Ajax.request({
						url : '../../OperatePrinter.do',
						params : {
							dataSource : 'delete',
							printerId : sn.attributes.printerId
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								printerTree.getRootNode().reload();
								Ext.example.msg(jr.title, jr.msg);
								printerWin.hide();
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt) {
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}
		});
	}
	
}



var printerTree;
Ext.onReady(function(){
	Ext.BLANK_IMAGE_URL = '../../extjs/resources/images/default/s.gif';
	Ext.QuickTips.init();
	Ext.form.Field.prototype.msgTarget = 'side';
	
	var opt = function(){
		return "<a href = \"javascript:printFuncOperactionHandler({type : 'update'})\">" + "<img src='../../images/Modify.png'/>修改</a>"
		 +"&nbsp;&nbsp;"
		 + "<a href=\"javascript:deletePrintFuncOperationHandler()\">" + "<img src='../../images/del.png'/>删除</a>";
	};
	
	
	
	
	printerTree = new Ext.tree.TreePanel({
		title : '打印机',
		id : 'printerTree',
		region : 'west',
		width : 250,
		border : true,
		rootVisible : false,
		frame : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		tbar : new Ext.Toolbar({
			height : 26,
			items : [{
				text : '添加',
				hidden : true,
				iconCls : 'btn_add',
				handler : function(){
					operatePrinterHandler({otype:'insert'});
				}
			}, '->',{
				text : '修改',
				iconCls : 'btn_edit',
				handler : function(){
					operatePrinterHandler({otype:'update'});
				}
			}, {
				text : '删除',
				iconCls : 'btn_delete',
				handler : function(){
					operatePrinterHandler({otype:'delete'});
				}
			}]
		}),
		
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryPrinterTree.do',
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部',
			leaf : false,
			printerId : -1,
			border : true,
			listeners : {
				load : function(){
					var treeRoot = printerTree.getRootNode().childNodes;
					if(treeRoot.length > 0){
						ds.load({
							params : {
								'printerId' : treeRoot[0].attributes.printerId
							}
						});
/*						for ( var i = 0; i < treeRoot.length; i++) {
							alert(treeRoot[i]);
							treeRoot[i].iconCls = 'btn_add';
						}*/
					}else{
						/*						printerTree.getRootNode().getUI().hide();
						Ext.Msg.show({
							title : '提示',
							msg : '加载打印机失败',
							buttons : Ext.MessageBox.OK
						});*/
					}
					
				}
			}
		}),
		listeners : {
/*			click : function(e){
				Ext.getDom('tbPrinterName').innerHTML = e.attributes.name + " " + e.attributes.alias;
			},*/
			dblclick : function(e){
				Ext.getDom('tbPrinterName').innerHTML = e.attributes.name + " " + e.attributes.alias;
				//var rn = printerTree.getSelectionModel().getSelectedNode();
				ds.load({
					params : {
						'printerId' : e.attributes.printerId
					}
				});
			},
			load : function(node, records){
				
				if(node.firstChild != null){
					
					node.firstChild.select();
					Ext.getDom('tbPrinterName').innerHTML = node.firstChild.attributes.name + " " + node.firstChild.attributes.alias;
				}
				
			}
		}
		
		
	});
	
	function tooLength(v){
		if(v.length > 25){
			var after = v.substring(0,25);
			return after + '....';
		}else{
			return v;
		}
	}
	
	
	var cm = new Ext.grid.ColumnModel([
		new Ext.grid.RowNumberer(),
		{header : '功能', dataIndex : 'pTypeText', width : 100},
		{header : '厨房', dataIndex : 'kitchens', width : 350, renderer : tooLength},
		{header : '部门', dataIndex : 'dept', width : 100},
		{header : '区域', dataIndex : 'regions', width : 320, renderer : tooLength},
		{header : '打印数', dataIndex : 'repeat', width : 80},
		{header : '操作',dataIndex : 'opt', renderer : opt, width : 100}
	]);
	
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url:'../../QueryPrintFunc.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
		    {name : 'printFuncId'},
		    {name : 'pTypeValue'},
		    {name : 'pTypeText'},
		    {name : 'kitchens'},
		    {name : 'kitchenValues'},
		    {name : 'dept'},
		    {name : 'deptValue'},
		    {name : 'regions'},
		    {name : 'regionValues'},
		    {name : 'repeat'}
		])
		
	});
	
	var printFuncGrid = new Ext.grid.GridPanel({
		title : '打印方案',
		id : 'grid',
		region : 'center',
		height : '500',
		border : true,
		frame : true,
		cm : cm,
		store : ds,
		tbar : new Ext.Toolbar({
			items : [
		 		{
		 			xtype : 'tbtext',
		 			text : String.format(
		 				Ext.ux.txtFormat.attrName,
		 				'打印机','tbPrinterName','----'
		 			)
		 		}
			 ]
		}),
		listeners : {
			dblclick  : function(){
				printFuncOperactionHandler({type : 'update'});
			}
		}
	});
	
	var printPanel = new Ext.Panel({
		title : '打印配置',
		id : 'printPanel',
		region : 'center',
		layout : 'border',
		frame : true,
		items : [printerTree, printFuncGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [{
			    	text : '添加打印机',
			    	handler : function(){
			    		operatePrinterHandler({otype : 'insert'});
			    	}
					    		
				},
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
			    {
			    	text : '添加方案',
			    	handler : function(){
			    		printFuncOperactionHandler({type : 'insert'});
			    	}
			    		
			    },
			    '->',
			    pushBackBut, 
			    {xtype:'tbtext',text:'&nbsp;&nbsp;'},
				logOutBut 
			]
		})
		
	});
	getOperatorName("../../");
	new Ext.Viewport({
		layout : 'border',
		id : 'viewport',
		items : 
		[{
			region : 'north',
			bodyStyle : 'background-color:#DFE8F6;',
			html : "<h4 style='padding:10px;font-size:150%;float:left;'>无线点餐网页终端</h4><div id='optName' class='optName'></div>",
			height : 50,
			border : false,
			margins : '0 0 0 0'
		},printPanel,{
			region : 'south',
			height : 30,
			frame : true,
			html : '<div style="font-size:11pt; text-align:center;"><b>版权所有(c) 2011 智易科技</b></div>'
		}]
	});
	
	
	
});