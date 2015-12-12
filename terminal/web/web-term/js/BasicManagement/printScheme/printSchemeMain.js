

Ext.onReady(function(){
	//初始化打印机和打印方案控件
	init();
	
	var addPrintFunc, printerTree, kitchenTree, printerWin;


function formatName(v){
	if(v.length > 6){
		
		v = v.substring(0, 6);
	}
	return v;
}

function loadInformation(){
	Ext.Ajax.request({
		url : '../../QueryDept.do',
		params : {
			dataSource : 'normal',
			isDept : true
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			
			if(jr.success){
				for ( var i = 0; i < jr.root.length; i++) {
					
					var d = jr.root[i];
					var c = {items : [{xtype : "checkbox", name : "dept",boxLabel : formatName(d.name) , hideLabel : true, inputValue :  d.id }]};
					
					Ext.getCmp('allDept').add(c);
					if((i+1)%4 == 0){
						Ext.getCmp('allDept').add({columnWidth : 1});
					}
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
			dataSource : 'normal'
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			
			if(jr.success){
				for ( var i = 0; i < jr.root.length; i++) {
	
					var r = jr.root[i];
					var c = {items : [{xtype : "checkbox", name : "region", boxLabel : formatName(r.name) , hideLabel : true, inputValue :  r.id }]};
	
					Ext.getCmp('allRegion').add(c);
					if((i+1)%4 == 0){
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
	
	kitchenTree = new Ext.tree.TreePanel({
		id : 'kitchens',
		height : 380,
		hidden : true,
		border : true,
		rootVisible : true,
		frame : false,
		cls : 'font',
		autoScroll : true,
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		loader : new Ext.tree.TreeLoader({
			dataUrl : '../../QueryKitchen.do',
			baseParams : {
				dataSource : 'printKitchenTree'
			},
			listeners : {
				load : function(thiz, node, res){
					node.eachChild(function(child) { 
						if(child.hasChildNodes()){
							child.eachChild(function(childSon) { 
								setParentNodeCheckState(childSon);
							});
						}
					}); 
					//treePanel长度过长问题
					$('#kitchens .x-panel-body-noheader').height(380);
				}
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部',
			leaf : false,
			printerId : -1,
			border : true
		}),
		listeners : {
			checkchange:function(node, checked){
				node.expand(); 
				node.attributes.checked = checked; 
				node.eachChild(function(child) { 
					child.ui.toggleCheck(checked);  
					child.attributes.checked = checked; 
					child.fireEvent('checkchange', child, checked); 
				}); 
				setParentNodeCheckState(node);
			}			
		}
	});		
	
}

//厨房树checkbox列表
function setParentNodeCheckState(node){
	var parentNode = node.parentNode;  
	if (parentNode != null) {  
		var checkBox = parentNode.getUI().checkbox;  
		var isAllChildChecked = true;  
		var someChecked = false; 
		var childCount = parentNode.childNodes.length;  
		for (var i = 0; i < childCount; i++) {  
			var child = parentNode.childNodes[i]; 
			if (child.attributes.checked) { 
				someChecked = true; 
			}	else if (child.getUI().checkbox.indeterminate == true && child.getUI().checkbox.checked == false) { 
				someChecked = true;  
				isAllChildChecked = false; 
				break; 
			}else { 
				isAllChildChecked = false; 
			}  
		}
		
		if (isAllChildChecked && someChecked) {
			parentNode.attributes.checked = true;
			if (checkBox != null) {
				checkBox.indeterminate = false;
				checkBox.checked = true;
			}
			
		}else if (someChecked) {
			parentNode.attributes.checked = false;
			if (checkBox != null) {
				checkBox.indeterminate = true;
				checkBox.checked = false;
			}
			
		}else{
			parentNode.attributes.checked = false;
			if (checkBox != null) {
				checkBox.indeterminate = false;
				checkBox.checked = false;
			}
		}
		this.setParentNodeCheckState(parentNode);
		
	}

}

function init(){
	//部门列表 & 区域列表 & 厨房列表
	loadInformation();
	
	//打印机方案操作
	addPrintFunc = new Ext.Window({
		title : '添加方案',
		id : 'addPrintFuncWin',
		closable : false,
		resizable : true,
		modal : true,
		width : 700,
		height : 550,
		listeners : {
			show : function(thiz){
				thiz.doLayout();
			}		
		},
		bbar : ['->',{
			xtype : 'checkbox',
			id : 'chkIsNeedToAdd',
			checked : true,
			boxLabel : '打印加菜总单'			
		},{
			xtype : 'checkbox',
			id : 'chkIsNeedToCancel',
			checked : true,
			boxLabel : '打印退菜总单'			
		},'-',{
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
				
				//选厨房
				var allKitchen = Ext.getDom('chkAllKitchen');
				var kitchens = '';
				if(!allKitchen.checked){
					var checkedNodes = kitchenTree.getChecked();
					for(var i=0;i < checkedNodes.length; i++){
						if(checkedNodes[i].attributes.isKitchen){
							if(kitchens != ''){
								kitchens += ',';
							}
							kitchens += checkedNodes[i].attributes.kid;
						}
					}	
				}
				
				//选部门
				var allDept = Ext.getDom('chkAllDept');
				var depts = '';
				if(!allDept.checked){
					var dept = document.getElementsByName('dept');
					for ( var i = 0; i < dept.length; i++) {
						if(dept[i].checked && depts == ''){
							depts += dept[i].value;
						}else if(dept[i].checked && depts != ''){
							depts += (',' + dept[i].value);
						}
					}
				}
	
	
				//选区域
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
						isNeedToAdd : Ext.getDom('chkIsNeedToAdd').checked,
						isNeedToCancel : Ext.getDom('chkIsNeedToCancel').checked, 
						dataSource : 'insert'
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.getCmp('printFunc_grid').store.reload();
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
			
		},{
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
				
				//选厨房
				var allKitchen = Ext.getDom('chkAllKitchen');
				var kitchens = '';
				if(!allKitchen.checked){
					var checkedNodes = kitchenTree.getChecked();
					for(var i=0;i < checkedNodes.length; i++){
						if(checkedNodes[i].attributes.isKitchen){
							if(kitchens != ''){
								kitchens += ',';
							}
							kitchens += checkedNodes[i].attributes.kid;
						}
					}	
				}
				
				//选部门
				var allDept = Ext.getDom('chkAllDept');
				var depts = '';
				if(!allDept.checked){
					var dept = document.getElementsByName('dept');
					for ( var i = 0; i < dept.length; i++) {
						if(dept[i].checked && depts == ''){
							depts += dept[i].value;
						}else if(dept[i].checked && depts != ''){
							depts += (',' + dept[i].value);
						}
					}
				}
	
	
				//选区域
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
					var ss = Ext.getCmp('printFunc_grid').getSelectionModel().getSelected();
					funcId = ss.data.printFuncId;
					dataSource = 'update';
				}
				else return;
				
				
				var sn = Ext.getCmp('printerTree').getSelectionModel().getSelectedNode();
				var repeat = Ext.getCmp('txtRepeat').getValue();
				var printComment = Ext.getCmp('printComment').getValue();
				
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
						isNeedToAdd : Ext.getDom('chkIsNeedToAdd').checked,
						isNeedToCancel : Ext.getDom('chkIsNeedToCancel').checked, 
						comment : printComment,
						dataSource : dataSource
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							sn.fireEvent('click', sn);
							Ext.example.msg(jr.title, jr.msg);
							
							Ext.getCmp('printFunc_grid').getStore().reload();
						}else{
							Ext.ux.showMsg(jr);
						}
						addPrintFunc.close();
						init();
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
				addPrintFunc.close();
				init();
			}
		}],
		layout : 'border',
		items : [{
			border : false,
			frame : true,
			region : 'center',
			height : 550,
			items : [{
				layout : 'column',
				frame : true,
				defaults : {
					columnWidth : .5,
					layout : 'form',
					labelWidth : 60
				},
				items : [{
					columnWidth : 0.6,
					items : [{
						xtype : 'tbtext',
						text : String.format(
							Ext.ux.txtFormat.attrName,
							'打印机','lblPrinterName',''
						)
					}]
				}, {
					columnWidth : 0.4,
					items : [{
						xtype : 'textfield',
						id : 'txtRepeat',
						fieldLabel : '打印数',
						width : 70,
						value : 1,
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
					columnWidth : .25,
					layout : 'form',
					labelWidth : 70
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
						checked : true,
						boxLabel : '点菜总单',
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
						id : 'radioCancelOrder',
						inputValue : 8,
						hideLabel : true,
						checked : true,
						boxLabel : '退菜总单',
						listeners : {
							check  : function(thiz, checked){
								if(checked){
									showPanel(thiz.inputValue);
								}
							},
							//第一次点击bug
							focus : function(thiz){
								thiz.fireEvent('check', thiz, true)
							}
						}
					}]
				},{
					items : [{
						xtype : 'radio',
						name : 'pType',
						inputValue : 2,
						hideLabel : true,
						boxLabel : '点菜分单',
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
								}
							}
						}
					}]
					
				},{
					items : [{
						xtype : 'radio',
						name : 'pType',
						inputValue : 16,
						hideLabel : true,
						boxLabel : '转菜',
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
						inputValue : 19,
						hideLabel : true,
						boxLabel : '微信订单',
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
						inputValue : 18,
						hideLabel : true,
						boxLabel : '客显',
						listeners : {
							check  : function(thiz, checked){
								if(checked){
									showPanel(thiz.inputValue);
								}
							}
						}
					}]
				}]
			},{
				layout : 'column',
				id : 'depts',
				frame : true,
				defaults : {
					columnWidth : .25,
					layout : 'form',
					labelWidth : 70
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
						columnWidth : .25,
						layout : 'form',
						labelWidth : 70
					}
				}]
			},{
				layout : 'column',
				id : 'regions',
				frame : true,
				defaults : {
					columnWidth : .25,
					layout : 'form',
					labelWidth : 70
				},
				items : [{
					columnWidth : 1,
					xtype : 'label',
					text : '请选择区域:'
				},{
					items : [{
						xtype : 'checkbox',
		//				name : 'pType',
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
						columnWidth : .25,
						layout : 'form',
						labelWidth : 70
					}
				}]
			},{
				layout : 'column',
				id : 'kitchensTree',
				frame : true,
				defaults : {
					columnWidth : .25,
					layout : 'form',
					labelWidth : 70
				},
				items : [{
					columnWidth : 1,
					xtype : 'label',
					text : '请选择厨房:'
				},{
					items : [{
						xtype : 'checkbox',
		//				name : 'pType',
						id : 'chkAllKitchen',
						hideLabel : true,
						boxLabel : '所有厨房',
						listeners : {
							focus : function(){
								Ext.getCmp('kitchens').enable();
							},
							check : function(checkbox, checked){
								if(checked){
									Ext.getCmp('kitchens').disable();
								}else{
									Ext.getCmp('kitchens').enable();
								}
								
							}
						}
					}]
				}]
			}, kitchenTree]			
		},{
			id : 'paperDemoCmp',
			title : '打单示例',
			region : 'east',
			layout : 'border',
			height : 550,
			width : 300,
			items : [{
				id : 'showPrintPaper',
				border : false,
				region : 'center',
				bodyStyle : 'background:url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/%E7%82%B9%E8%8F%9C%E6%80%BB%E5%8D%95.jpg) no-repeat center center;'
			}, {
				region : 'south',
				id : 'printCommentPanel',
				height : 55,
				frame : true,
				layout : 'form',
				labelWidth : 80,
				items : [{
					xtype : 'label',
					text : '单尾结束语:'
				}, {
					xtype : 'textfield',
					style : 'margin-top:5px;',
					id : 'printComment',
					width : 280,
					hideLabel : true
				}]
			}]
		}]
	});
	
	
	function opePrintName(btn){
		Ext.getCmp('txtPrinterAlias').setValue(btn.getText());
		Ext.getCmp('txtPrinterAlias').focus();		
	}

	//打印机操作
	if(!printerWin){
		printerWin = new Ext.Window({
			id : 'print_addPrinter',
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
					var printerAlias;
					var isEnabled = Ext.getCmp('enabled');
					var styles = document.getElementsByName('pStyle');
					var printerId = Ext.getCmp('printerId');
					var orientedType  = document.getElementsByName('isAll');
					
					var dataSource = '';
					
					//机型
					var style = '';
					for (var i = 0; i < styles.length; i++) {
						if(styles[i].checked){
							style = styles[i].value;
						}
					}
					//停用
					if(isEnabled.checked){
						isEnabled = 'true';
					}else{
						isEnabled = 'false';
					}
					
					//面向类型
					var orientedTypeId = '';
					for(var i = 0; i < orientedType.length; i++){
						if(orientedType[i].checked){
							orientedTypeId = orientedType[i].value; 
						}
					}
					
					if(!Ext.getCmp('txtPrinterName').isValid()){
						return;
					}
					
					if(printerWin.operationType == 'insert'){
						dataSource = 'insert';
					}else if(printerWin.operationType == 'update'){
						dataSource = 'update';
					}else return;
					
					//别名input为空时,从radio中选择别名
					if(Ext.getCmp('txtPrinterAlias').getValue()){
						printerAlias = Ext.getCmp('txtPrinterAlias').getValue();
					}else{
						var names = document.getElementsByName('pName');
						for (var i = 0; i < names.length; i++) {
							if(names[i].checked){
								printerAlias = names[i].value;
								break;
							}
						}					
					}
					
					Ext.Ajax.request({
						url : '../../OperatePrinter.do',
						params : {
							printerName : printerName.getValue(),
							printerAlias : printerAlias,
							style : style,
							printerId : printerId.getValue(),
							isEnabled : isEnabled,
							oriented : orientedTypeId,
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
				labelWidth : 50,
				frame : true,
				defaultType : 'textfield',
				items : [{
					id : 'txtPrinterName',
					fieldLabel : '打印机',
					width : 130,
					allowBlank : false,
					validator : function(v){
						if(Ext.util.Format.trim(v).length > 0){
							return true;
						}else{
							return '名称不能为空.';
						}
					}
				},{
					xtype : 'fieldset',
					title : '选择名称',
					autoHeight : true,
					labelWidth : 40,
					items : [{
						layout : 'column',
						frame : false,
						defaults : {
							columnWidth : .25,
							layout : 'form'
						},
						items : [{
							items : [{
								xtype : 'button',
								text : '收银',
								width : 50,
								height : 30,
								handler : function(btn){
									Ext.getCmp('txtPrinterAlias').setValue(btn.getText());
									Ext.getCmp('txtPrinterAlias').focus();
								}
							}]
						}, {
							items : [{
								xtype : 'button',
								text : '中厨',
								width : 50,
								height : 30,
								handler : function(btn){
									Ext.getCmp('txtPrinterAlias').setValue(btn.getText());
									Ext.getCmp('txtPrinterAlias').focus();
								}
							}]
						}, {
							items : [{
								xtype : 'button',
								text : '点心',
								width : 50,
								height : 30,
								handler : function(btn){
									Ext.getCmp('txtPrinterAlias').setValue(btn.getText());
									Ext.getCmp('txtPrinterAlias').focus();
								}
							}]
						}, {
							items : [{
								xtype : 'button',
								text : '水吧',
								width : 50,
								height : 30,
								handler : function(btn){
									Ext.getCmp('txtPrinterAlias').setValue(btn.getText());
									Ext.getCmp('txtPrinterAlias').focus();
								}
							}]
						},{
							columnWidth : 1,
							style : 'margin-bottom:5px;'
						}, {
							items : [{
								xtype : 'button',
								text : '海鲜',
								width : 50,
								height : 30,
								handler : function(btn){
									Ext.getCmp('txtPrinterAlias').setValue(btn.getText());
									Ext.getCmp('txtPrinterAlias').focus();
								}
							}]
						}, {
							items : [{
								xtype : 'button',
								text : '地哩',
								width : 50,
								height : 30,
								handler : function(btn){
									Ext.getCmp('txtPrinterAlias').setValue(btn.getText());
									Ext.getCmp('txtPrinterAlias').focus();
								}
							}]
						},{
							columnWidth : 1,
							style : 'margin-bottom:5px;'
						}]
					}, {
						xtype : 'textfield',
						id : 'txtPrinterAlias',
						fieldLabel : '别名',
						width : 130
					}]
					
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
						boxLabel : '58mm'
					}, {
						xtype : 'radio',
						name : 'pStyle',
						inputValue : 3,
						hideLabel : true,
						boxLabel : '76mm'
					}, {
						xtype : 'radio',
						name : 'pStyle',
						inputValue : 4,
						hideLabel : true,
						boxLabel : '标签(50mm * 40mm)'
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
						boxLabel : '可用'
					},{
						id : 'unEnabled',
						name : 'isEnabled',
						inputValue : 'false',
						hideLabel : true,
						boxLabel : '停用'
					}]
				}, {
					xtype : 'fieldset',
					title : '选择类型',
					autoHeight : true,
					defaultType : 'radio',
					hideLabels : true,
					items : [{
						id : 'selectAll',
						name : 'isAll',
						inputValue : 1,
						hideLabel : true,
						checked : true,
						boxLabel : '面向全部'
					},{
						id : 'selectOnly',
						name : 'isAll',
						inputValue : 2,
						hideLabel : true,
						boxLabel : '面向特定'
					}]
				}, {
					xtype : 'hidden',
					id : 'printerId'
				}]
			}]
			
		});			
	}
	
	
}


function showPanel(v){
	//获取退菜btn
	var cancelFoodBtn = Ext.getCmp('chkIsNeedToCancel');
	var addFoodBtn = Ext.getCmp('chkIsNeedToAdd');
	
	var paperDemoCmp = Ext.query("#showPrintPaper .x-panel-body")[0];
	if(v == 1 || v ==8){//总单
		Ext.getCmp('kitchens').hide();
		Ext.getCmp('kitchensTree').hide();
		Ext.getCmp('depts').show();
		Ext.getCmp('regions').show();
		
		
		if(v == 1){
			Ext.getCmp('printCommentPanel').show();
			paperDemoCmp.style.backgroundImage = 'url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/%E7%82%B9%E8%8F%9C%E6%80%BB%E5%8D%95.jpg)';
		}else{
			Ext.getCmp('printCommentPanel').hide();
			paperDemoCmp.style.backgroundImage = 'url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/%E7%82%B9%E8%8F%9C%E6%80%BB%E5%8D%95.jpg)';
		}
		
		cancelFoodBtn.hide();
		addFoodBtn.hide();
//		cancelFoodBtn.show();
//		cancelFoodBtn.setBoxLabel('打印退菜总单');
//		addFoodBtn.show();
//		addFoodBtn.setBoxLabel('打印加菜总单');
	}else if(v == 2 || v == 5){//分单
		Ext.getCmp('kitchens').show();
		Ext.getCmp('kitchensTree').show();
		Ext.getCmp('depts').hide();
		Ext.getCmp('regions').hide();
		Ext.getCmp('printCommentPanel').hide();
		paperDemoCmp.style.backgroundImage = 'url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/fendan.jpg)';
		cancelFoodBtn.show();
		cancelFoodBtn.setBoxLabel('打印退菜分单');
		addFoodBtn.show();
		addFoodBtn.setBoxLabel('打印加菜分单');
	}else if(v == 18){//客显
		Ext.getCmp('kitchens').hide();
		Ext.getCmp('kitchensTree').hide();
		Ext.getCmp('depts').hide();
		Ext.getCmp('regions').hide();
		Ext.getCmp('printCommentPanel').hide();
//		paperDemoCmp.style.backgroundImage = 'url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/fendan.jpg)';
	}else{
		Ext.getCmp('kitchens').hide();
		Ext.getCmp('kitchensTree').hide();
		Ext.getCmp('depts').hide();
		Ext.getCmp('regions').show();
		cancelFoodBtn.hide();
		addFoodBtn.hide();
		
		if(v == 127){//暂结
			Ext.getCmp('printCommentPanel').show();
			paperDemoCmp.style.backgroundImage = 'url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/%E6%9A%82%E7%BB%93%E5%8D%95.jpg)';
		}else if(v == 3){//结账
			paperDemoCmp.style.backgroundImage = 'url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/jiezhang.jpg)';
			Ext.getCmp('printCommentPanel').show();
		}else if(v == 6){//转台
			paperDemoCmp.style.backgroundImage = 'url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/%E8%BD%AC%E5%8F%B0.jpg)';
			Ext.getCmp('printCommentPanel').hide();
		}else if(v == 9){//催菜
			paperDemoCmp.style.backgroundImage = 'url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/%E5%82%AC%E8%8F%9C.jpg)';
			Ext.getCmp('printCommentPanel').hide();			
		}else if(v == 16){//转菜
			paperDemoCmp.style.backgroundImage = 'url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/transferFood2.jpg)';
			Ext.getCmp('printCommentPanel').hide();			
		}else if(v == 19){
			paperDemoCmp.style.backgroundImage = 'url(http://digie-image-real.oss-cn-hangzhou.aliyuncs.com/PrintSample/wxOrder.jpg)';
			Ext.getCmp('printCommentPanel').hide();		
		}
	}
	Ext.getCmp('paperDemoCmp').doLayout();
	
	paperDemoCmp.style.backgroundSize = 'cover';
	
	//防止切换时格式错乱
	Ext.getCmp('addPrintFuncWin').center();

} 

function deletePrintFuncOperationHandler(){
	var ss = Ext.getCmp('printFunc_grid').getSelectionModel().getSelected();
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
								Ext.getCmp('printFunc_grid').store.reload();
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

//打印方案初始化
function printFuncOperactionHandler(c){
	if(c.type == 'undefined'){
		return;
	}
	if(!Ext.getCmp('addPrintFuncWin')){
		init();
	}
	var pType = document.getElementsByName('pType');
	var dept = document.getElementsByName('dept');
	var region = document.getElementsByName('region');
	var repeat = Ext.getCmp('txtRepeat');
	var comment = Ext.getCmp('printComment');
	//区域重置
	for ( var i = 0; i < region.length; i++) {
		if(region[i].checked){
			region[i].checked = false;
		}
	}
	
	var sn = printerTree.getSelectionModel().getSelectedNode();
	var ss = Ext.getCmp('printFunc_grid').getSelectionModel().getSelected();
	if(c.type == 'update'){
		kitchenTree.loader.dataUrl = "../../QueryKitchen.do";
		kitchenTree.loader.baseParams = {dataSource : 'printKitchenTree4Update', schemeId : ss.data.printFuncId, printerId:sn.id};
	}
	
	addPrintFunc.show();
	
	Ext.getDom('lblPrinterName').innerHTML = sn.attributes.name + " " + sn.attributes.alias;
	if(c.type == 'insert'){
		addPrintFunc.setTitle('添加方案');
		addPrintFunc.operationType = c.type;
		Ext.getCmp('txtRepeat').setValue(1);
		
		Ext.getCmp('printerType').show();
		Ext.getCmp('btnSaveNext').show();
		
		document.getElementById('radioOrder').checked = true;
		Ext.getCmp('radioOrder').fireEvent('check', Ext.getCmp('radioOrder'), true);
		Ext.getCmp('allDept').disable();
		Ext.getCmp('chkAllRegion').fireEvent('check', Ext.getCmp('chkAllRegion'), true);
		Ext.getCmp('chkAllKitchen').fireEvent('check', Ext.getCmp('chkAllKitchen'), true);
		
		Ext.getDom('chkAllDept').checked = true;
		Ext.getDom('chkAllRegion').checked = true;
		Ext.getDom('chkAllKitchen').checked = true;
		
	}else{
		if(ss == null){
			Ext.MessageBox.alert('提示', '请选中一个方案再进行操作.');
			return;
		}
		
		//判断是打印加菜还是退菜
		var pTypeText = '';
		if(ss.data.pTypeValue == 1){//总单
			pTypeText = 'summary';
		}else if(ss.data.pTypeValue == 2){//分单
			pTypeText = 'detail';
		}
		if(pTypeText){
			Ext.Ajax.request({
				url : '../../OperatePrintFunc.do',
				params : {
					printerId : ss.data.printerId,
					dataSource : 'isEnable',
					pType : pTypeText
				},
				success : function(res, opt){
					var jr = Ext.decode(res.responseText);
					if(jr.success){
						//是否有加菜
						if(jr.other.add){
							Ext.getDom('chkIsNeedToAdd').checked = true;
						}else{
							Ext.getDom('chkIsNeedToAdd').checked = false;
						}
						//是否有退菜
						if(jr.other.cancel){
							Ext.getDom('chkIsNeedToCancel').checked = true;
						}else{
							Ext.getDom('chkIsNeedToCancel').checked = false;
						}
					}
				},
				failure : function(res, opt){
					Ext.ux.showMsg(Ext.decode(res.responseText));
				}
			});	
		}
		
		
		
		if(c.type == 'update'){
			addPrintFunc.setTitle('修改方案');
		}else{
			addPrintFunc.setTitle('查看方案');
		}
		

		
		addPrintFunc.operationType = c.type;
		Ext.getCmp('kitchens').setHeight(435);
		Ext.getCmp('kitchensTree').hide();
		
		Ext.getCmp('printerType').hide();
		Ext.getCmp('btnSaveNext').hide();
		
		
		//功能选中
		var pTypeValue = ss.data.pTypeValue;
		for ( var i = 0; i < pType.length; i++) {
			if(pType[i].value == pTypeValue){
				pType[i].checked = true;
				pType[i].click();
				//切换图片
				showPanel(pType[i].value);
			}
		}
		
		//部门选中
		var deptValue = ss.data.deptValue;
		if(!deptValue == ''){
			deptValue = ss.data.deptValue.split(",");
			Ext.getDom('chkAllDept').checked = false;
			Ext.getCmp('chkAllDept').fireEvent('check', Ext.getCmp('chkAllDept'), false);
			for ( var i = 0; i < dept.length; i++) {
				for ( var j = 0; j < deptValue.length; j++) {
					if(deptValue[j] == dept[i].value){
						dept[i].checked = true;
						break;
					}
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
						break;
					}
				}
			}

		}

		//厨房是否全选
		if(ss.data.kitchenValues == ''){
			Ext.getDom('chkAllKitchen').checked = true;
			Ext.getCmp('chkAllKitchen').fireEvent('check', Ext.getCmp('chkAllKitchen'), true);
		}
		
		repeat.setValue(ss.data.repeat);
		
		comment.setValue(ss.data.comment);
	
	}
	addPrintFunc.center();
}

//打印机初始化
function operatePrinterHandler(c){
	if(c == null || typeof c == 'undefined' || typeof c.otype == 'undefined'){
		return;
	}
	var printerName = Ext.getCmp('txtPrinterName');
	if(c.otype == 'insert'){
		printerWin.setTitle('添加打印机');
		
		printerName.setValue();
		printerName.clearInvalid();
		Ext.getCmp('txtPrinterAlias').fireEvent('focus', Ext.getCmp('txtPrinterAlias'));
		Ext.getCmp('txtPrinterAlias').setValue();
		Ext.getCmp('txtPrinterAlias').clearInvalid();

		printerWin.operationType = c.otype;
		printerWin.show();
		document.getElementById('enabled').checked = true;
		document.getElementById('rdo80mm').checked = true;
		document.getElementById('selectAll').checked = true;
		printerName.focus(true, 100);
	}else if(c.otype == 'update'){
		var sn = Ext.ux.getSelNode(printerTree);
		if(!sn){
			Ext.example.msg('提示', '请选中一个打印机再进行操作.');
			return;
		}
		printerWin.operationType = c.otype;
		printerWin.show();
		printerWin.setTitle('修改打印机');
		printerName.setValue(sn.attributes.name);
		Ext.getCmp('txtPrinterAlias').fireEvent('focus', Ext.getCmp('txtPrinterAlias'));
		Ext.getCmp('txtPrinterAlias').setValue(sn.attributes.alias);
		Ext.getCmp('printerId').setValue(sn.id);
		var styles = document.getElementsByName('pStyle');
		var orientedType = document.getElementsByName('isAll');
		
		if(styles[0].value == sn.attributes.styleValue){
			styles[0].checked = true;
		}else if(styles[1].value == sn.attributes.styleValue){
			styles[1].checked = true;
		}else if(styles[2].value == sn.attributes.styleValue){
			styles[2].checked = true;
		}else if(styles[3].value == sn.attributes.styleValue){
			styles[3].checked = true;
		}
		
		if(orientedType[0].value == sn.attributes.orientedValue){
			orientedType[0].checked = true;
		}else{
			orientedType[1].checked = true;
		}
		
		
		if(sn.attributes.isEnabled){
			document.getElementById('enabled').checked = true;
		}else{
			document.getElementById('unEnabled').checked = true;
		}
		printerName.focus(true, 100);
		
	}else if(c.otype == 'delete'){
		var sn = Ext.ux.getSelNode(printerTree);
		if(!sn){
			Ext.example.msg('提示', '请选中一个打印机再进行操作.');
			return;
		}
		
		Ext.Msg.show({
			title : '重要',
			msg : '是否删除打印机: '+sn.attributes.name,
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
								Ext.example.msg(jr.title, String.format(Ext.ux.txtFormat.deleteSuccess, sn.attributes.name));
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
	

	var opt = function(){
		return "<a class=\"printUpdateLink\">" + "<img src='../../images/Modify.png'/>修改</a>"
		 +"&nbsp;&nbsp;"
		 + "<a class=\"printDeleteLink\">" + "<img src='../../images/del.png'/>删除</a>";
	};
	
	printerTree = new Ext.tree.TreePanel({
		title : '打印机',
		id : 'printerTree',
		region : 'west',
		width : 250,
		border : true,
		rootVisible : true,
		frame : true,
		cls : 'font',
		bodyStyle : 'backgroundColor:#FFFFFF; border:1px solid #99BBE8;',
		tbar : new Ext.Toolbar({
			height : 26,
			items : ['->',{
				text : '添加',
				iconCls : 'btn_add',
				handler : function(){
					operatePrinterHandler({otype:'insert'});
				}
			}, {
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
			dataUrl : '../../OperatePrinter.do',
			baseParams : {
				dataSource : 'printerTree'
			},
			listeners : {
				load : function(thiz, node, res){
					var rn = printerTree.getRootNode().childNodes;
					var node = rn[0];
	        		if(node != null){
	        			node.select();
	        			node.fireEvent('click', node);
					}					
				}
			}
		}),
		root : new Ext.tree.AsyncTreeNode({
			expanded : true,
			text : '全部',
			leaf : false,
			printerId : -1,
			border : true
		}),
		listeners : {
			click : function(e){
				if(e.attributes.printerId > 0){
					Ext.getDom('tbPrinterName').innerHTML = e.attributes.name + " " + e.attributes.alias;
					ds.load({
						params : {
							'printerId' : e.attributes.printerId
						}
					});
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
		{header : '功能', dataIndex : 'pTypeText'},
		{header : '厨房', dataIndex : 'kitchens', width : 280, renderer : tooLength},
		{header : '部门', dataIndex : 'dept', width : 220},
		{header : '区域', dataIndex : 'regions', width : 256, renderer : tooLength},
		{header : '打印数', dataIndex : 'repeat'},
		{header : '操作', id:'operation', dataIndex : 'opt', renderer : opt, width : 160}
	]);
	
	cm.defaultSortable = true;
	
	var ds = new Ext.data.Store({
		proxy : new Ext.data.HttpProxy({url:'../../QueryPrintFunc.do'}),
		reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root : 'root'}, [
		    {name : 'printFuncId'},
		    {name : 'printerId'},
		    {name : 'pTypeValue'},
		    {name : 'pTypeText'},
		    {name : 'kitchens'},
		    {name : 'kitchenValues'},
		    {name : 'dept'},
		    {name : 'deptValue'},
		    {name : 'regions'},
		    {name : 'regionValues'},
		    {name : 'repeat'},
		    {name : 'comment'},
		    {name : 'isIncludeCancel'}
		]),
		listeners : {
			load : function(store, records, options){
				$('#divPrint').find('.printUpdateLink').each(function(index, element){
					element.onclick = function(){
						printFuncOperactionHandler({type : 'update'});
					};
				});
				$('#divPrint').find('.printDeleteLink').each(function(index, element){
					element.onclick = function(){
						deletePrintFuncOperationHandler();
					};					
				});
				
			}
		}
		
	});
	
	var printFuncGrid = new Ext.grid.GridPanel({
		title : '打印方案',
		id : 'printFunc_grid',
		region : 'center',
		height : '500',
		border : true,
		frame : true,
		cm : cm,
		store : ds,
		autoExpandColumn : 'operation',
		viewConfig : {
			forceFit : true
		},
		tbar : new Ext.Toolbar({
			items : [
		 		{
		 			xtype : 'tbtext',
		 			text : String.format(
		 				Ext.ux.txtFormat.attrName,
		 				'打印机','tbPrinterName','----'
		 			)
		 		},'->',{
		 			text : '添加',
		 			iconCls : 'btn_add',
		 			handler : function(){
		 				printFuncOperactionHandler({type : 'insert'});
		 			}
		 		},{
		 			text : '&nbsp;&nbsp;'
		 		}
			 ]
		}),
		listeners : {
			dblclick  : function(){
				printFuncOperactionHandler({type : 'update'});
			}
		}
	});
	
	new Ext.Panel({
		id : 'printPanel',
		renderTo : 'divPrint',
		width : parseInt(Ext.getDom('divPrint').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divPrint').parentElement.style.height.replace(/px/g,'')),
		layout : 'border',
		frame : true,
		items : [printerTree, printFuncGrid]
	});
	
	showFloatOption({
		treeId : 'printerTree', 
		option :[{
			name : '修改', 
			fn : function(){
				operatePrinterHandler({otype:'update'});	
			}
			}, {
			name : '删除', 
			fn : function(){
				operatePrinterHandler({otype:'delete'});
			}}]});
	
});