//-------------lib.js------------
function NewDate(str) { 
	str = str.split('-'); 
	var date = new Date(); 
	date.setUTCFullYear(str[0], str[1] - 1, str[2]); 
	date.setUTCHours(0, 0, 0, 0); 
	return date; 
} 

	var secondStepPanelSouth;
	if(!secondStepPanelSouth){
		secondStepPanelSouth = new Ext.Panel({
			id : 'secondStepPanelSouth',
			region : 'south',
			frame : true,
			height : 35,
			bodyStyle : 'font-size:18px;text-align:center;',
			contentEl : 'stockActionTotalPrice'
		});
	}
/**
 * 处理导航
 * @param e
 */
function stockTaskNavHandler(e){
	if(typeof stockTaskNavWin != 'undefined'){
		var btnPrevious = Ext.getCmp('btnPreviousForStockNav');
		var btnNext = Ext.getCmp('btnNextForStockNav');
		var act = stockTaskNavWin.getLayout().activeItem;
		var index = e.change + act.index;
		
		/***** 第一步, 选择入库单类型, 预处理 *****/
		if(act.index == 0 || index == 0){
			var select = null;
			var type = Ext.query('input[name=\"radioStockOrderType\"]');
			for(var i = 0; i < type.length; i++){
				if(type[i].checked){
					select = type[i].value;
					break;
				}
			}
			// 切换步骤
			stockTaskNavWin.getLayout().setActiveItem(index);
			
			if(stockTaskNavWin.stockType == null || typeof stockTaskNavWin.stockType == 'undefined'){
				if(select){
					stockTaskNavWin.stockType = select;
				}else{
					Ext.example.msg('提示', '请选择一项入库单.');
					return;
				}
			}else{
				
				if(select){
					var ss = select.split(',');
					var ws = stockTaskNavWin.stockType.split(',');
	
					for(var i = 0; i < ss.length; i++){
						if(ss[i] != ws[i]){
							//FIXME 暂不用确定是否改变
							operateStockActionBasic({
								otype : Ext.ux.otype['set']
							});
							stockTaskNavWin.stockType = select;
/*							Ext.Msg.confirm(
								'提示',
								'入库单类型已更改, 确定继续将清空原操作信息.',
								function(e){
									if(e == 'yes'){
										// 更换类型后重置相关信息
										operateStockActionBasic({
											otype : Ext.ux.otype['set']
										});
										stockTaskNavWin.stockType = select;
									}else{
										for(var i = 0; i < type.length; i++){
											var temp = type[i].value.split(',');
											if(eval(temp[0] == ws[0]) && eval(temp[1] == ws[1]) && eval(temp[2] == ws[2])){
												type[i].checked = true;
											}
										}											
									}
								},
								this
							);*/
						}
					}
				}
			}
		}
		if(index >= 1){
			/***** 第二步, 根据用户选择入库单类型设置相关信息 *****/
			
			/** 处理导航按钮触发事件, 设置导航页面或提交操作 **/
			if(index >= 0 && index <= 1 ){
				// 切换步骤
				stockTaskNavWin.getLayout().setActiveItem(index);
				var smfos = Ext.getCmp('comboSelectMaterialForStockAction');
				var stockActionDate = Ext.getCmp('datetOriStockDateForStockActionBasic');
				stockActionDate.clearInvalid();
				
				smfos.setValue();
				smfos.clearInvalid();
				smfos.store.load({
					params : {
						cateType : stockTaskNavWin.stockType.split(',')[1]
					}
				});
				stockTaskNavWin.setTitle(stockTaskNavWin.getLayout().activeItem.mt);
			}else{
				if(index > 1){
					// 完成时的操作
					var id = Ext.getCmp('hideStockActionId');
					var deptIn = Ext.getCmp('comboDeptInForStockActionBasic');
					var supplier = Ext.getCmp('comboSupplierForStockActionBasic');
					var deptOut = Ext.getCmp('comboDeptOutForStockActionBasic');
					var oriStockId = Ext.getCmp('txtOriStockIdForStockActionBasic');
					var oriStockDate = Ext.getCmp('datetOriStockDateForStockActionBasic');
					var comment = Ext.getCmp('txtCommentForStockActionBasic');
					var actualPrice = Ext.getDom('txtActualPrice');
					var detail = '';
					if(!oriStockDate.getValue()){
						if(oriStockDate.isValid()){
						
						}
						return;
					}
					if(parseInt(Ext.getDom('txtActualPrice').value) > parseInt(Ext.getDom('txtTotalPrice').value)){
						Ext.example.msg("错误", "实际金额不能大于总金额");
						return;
					}
					
					var stockTypeList = stockTaskNavWin.stockType.split(',');
					var stockType = stockTypeList[0], stockCate = stockTypeList[1], stockSubType = stockTypeList[2];
					if(stockType == 1){
						if(stockSubType == 1){
							if(!deptIn.isValid() || !supplier.isValid()){
								return;
							}
						}else if(stockSubType == 2){
							if(!deptIn.isValid() || !deptOut.isValid()){
								return;
							}
						}else if(stockSubType == 3){
							if(!deptIn.isValid()){
								return;
							}
						}
					}else if(stockType == 2){
						if(stockSubType == 4){
							if(!supplier.isValid() || !deptOut.isValid()){
								return;
							}
						}else if(stockSubType == 5){
							if(!deptIn.isValid() || !deptOut.isValid()){
								return;
							}
						}else if(stockSubType == 6){
							if(!deptOut.isValid()){
								return;
							}
						}
					}
					
					if(secondStepPanelCenter.getStore().getCount() == 0){
						Ext.example.msg('提示', '操作失败, 请选中货品信息.');
						return;
					}
					//防止重复点击
					btnNext.setDisabled(true);
					
					for(var i = 0; i < secondStepPanelCenter.getStore().getCount(); i++){
						var temp = secondStepPanelCenter.getStore().getAt(i);
						if(i>0){
							detail+='<sp>';
						}
						detail+=(temp.get('material.id')+'<spst>'+temp.get('price')+'<spst>'+temp.get('amount'));
					}
					Ext.Ajax.request({
						url : '../../OperateStockAction.do',
						params : {
							'dataSource' : stockTaskNavWin.otype.toLowerCase(),
							
							id : id.getValue(),
							deptIn : deptIn.getValue(),
							supplier : supplier.getValue(),
							deptOut : deptOut.getValue(),
							oriStockId : oriStockId.getValue(),
							oriStockDate : oriStockDate.getValue().getTime() + 86000000,
							comment : comment.getValue(),
							type : stockType,
							cate : stockCate,
							subType : stockSubType,
							actualPrice : actualPrice.value,
							detail : detail
						},
						success : function(res, opt){
							btnNext.setDisabled(false);
							var jr = Ext.decode(res.responseText);
							if(jr.success){
								Ext.example.msg(jr.title, jr.msg);
								stockTaskNavWin.hide();
								Ext.getCmp('comboSearchForStockType').setValue(stockType);
								Ext.getCmp('comboSearchForStockType').fireEvent('select', Ext.getCmp('comboSearchForStockType'));
								Ext.getCmp('sam_comboSearchForSubType').setValue(stockSubType);
								Ext.getCmp('sam_comboSearchForCateType').setValue(stockCate);
								
								Ext.getCmp('btnSearchForStockBasicMsg').handler();
								
							}else{
								Ext.ux.showMsg(jr);
							}
						},
						failure : function(res, opt){
							btnNext.setDisabled(false);
							Ext.ux.showMsg(Ext.decode(res.responseText));
						}
					});
				}
			}			
			
			var deptInDom = Ext.getCmp('displayPanelForDeptIn');
			var supplierDom = Ext.getCmp('displayPanelForSupplier');
			var deptOutDom = Ext.getCmp('displayPanelForDeptOut');
			var priceDom = Ext.getCmp('numSelectPriceForStockAction'); 
			var moneyPanel = Ext.getCmp('secondStepPanelSouth');
			var column = Ext.getCmp('secondStepPanelCenter').getColumnModel();
			var stockTypeList = stockTaskNavWin.stockType.split(',');
			var stockType = stockTypeList[0], stockCate = stockTypeList[1], stockSubType = stockTypeList[2];
			var diaplayTitle = '';
			
			//Ext.getDom('labActualPrice').style.display="block";
			if(stockType == 1){
				// 入库单
				if(stockSubType == 1){
					// 采购
					if(stockCate == 1){
						// 商品
						diaplayTitle = String.format("入库 -- 商品采购单");
					}else if(stockCate == 2){
						// 原料 
						diaplayTitle = String.format("入库 -- 原料采购单");
					}
					Ext.getDom('txtActualPrice').disabled = false;

					// 控制选择货仓
					deptInDom.show();
					supplierDom.show();
					deptOutDom.hide();
					if(document.getElementById('displayPanelForDeptIn')){
						document.getElementById('displayPanelForDeptIn').style.display = 'block';
					}
					if(document.getElementById('displayPanelForSupplier')){
						document.getElementById('displayPanelForSupplier').style.display = 'block';
					}
				}else if(stockSubType == 2){
					// 调拨
					if(stockCate == 1){
						// 商品
						diaplayTitle = String.format("入库 -- 商品调拨单");
					}else if(stockCate == 2){
						// 原料 
						diaplayTitle = String.format("入库 -- 原料调拨单");
					}
					// 控制选择货仓
					deptInDom.show();
					supplierDom.hide();
					
					deptOutDom.show();
					if(document.getElementById('displayPanelForDeptOut')){
						document.getElementById('displayPanelForDeptOut').style.display = 'block';
					}
					
					moneyPanel.setDisabled(true);
					priceDom.getEl()? priceDom.getEl().up('.x-form-item').setDisplayed(false) : '';
					column.setHidden(3, true);
					column.setHidden(4, true);
				}else if(stockSubType == 3){
					// 报溢
					if(stockCate == 1){
						// 商品
						diaplayTitle = String.format("入库 -- 商品报溢单");
						
					}else if(stockCate == 2){
						// 原料 
						diaplayTitle = String.format("入库 -- 原料报溢单");
						
					}
					// 控制选择货仓
					deptInDom.show();
					supplierDom.hide();
					deptOutDom.hide();
					
					priceDom.getEl()? priceDom.getEl().up('.x-form-item').setDisplayed(false) : '';
					column.setRenderer(3,'');
				}else if(stockSubType == 7){
					if(stockCate == 1){
						// 商品
						diaplayTitle = String.format("入库 -- 商品盘盈");
					}else if(stockCate == 2){
						// 原料 
						diaplayTitle = String.format("入库 -- 原料盘盈");
					}
					// 控制选择货仓
					deptInDom.show();
					supplierDom.hide();
					deptOutDom.hide();
				}
			}else if(stockType == 2){
				// 出库单
				if(stockSubType == 4){
					// 退货
					if(stockCate == 1){
						// 商品
						diaplayTitle = String.format("出库 -- 商品退货单");
						
					}else if(stockCate == 2){
						// 原料 
						diaplayTitle = String.format("出库 -- 原料退货单");
						
					}
					Ext.getDom('txtActualPrice').disabled = false;
					// 控制选择货仓
					
					deptOutDom.show();
					deptInDom.hide();
					supplierDom.show();
					if(document.getElementById('displayPanelForDeptOut')){
						document.getElementById('displayPanelForDeptOut').style.display = 'block';
					}					
					
				}else if(stockSubType == 5){
					// 调拨
					if(stockCate == 1){
						// 商品
						diaplayTitle = String.format("出库 -- 商品调拨单");
					}else if(stockCate == 2){
						// 原料 
						diaplayTitle = String.format("出库 -- 原料调拨单");
					}
					// 控制选择货仓
					deptInDom.show();
					supplierDom.hide();
					deptOutDom.show();
					
					if(document.getElementById('displayPanelForDeptOut')){
						document.getElementById('displayPanelForDeptOut').style.display = 'block';
					}		
					
					moneyPanel.setDisabled(true);
					priceDom.getEl()? priceDom.getEl().up('.x-form-item').setDisplayed(false) : '';
					column.setHidden(3, true);
					column.setHidden(4, true);
				}else if(stockSubType == 6){
					// 报损
					if(stockCate == 1){
						// 商品
						diaplayTitle = String.format("出库 -- 商品报损单");
					}else if(stockCate == 2){
						// 原料 
						diaplayTitle = String.format("出库 -- 原料报损单");
					}
					// 控制选择货仓
					deptInDom.hide();
					supplierDom.hide();
					deptOutDom.show();
					
					if(document.getElementById('displayPanelForDeptOut')){
						document.getElementById('displayPanelForDeptOut').style.display = 'block';
					}		
					
					priceDom.getEl()? priceDom.getEl().up('.x-form-item').setDisplayed(false) : '';
					column.setRenderer(3, '');
				}else if(stockSubType == 8){
					// 报损
					if(stockCate == 1){
						// 商品
						diaplayTitle = String.format("出库 -- 商品盘亏");
					}else if(stockCate == 2){
						// 原料 
						diaplayTitle = String.format("出库 -- 原料盘亏");
					}
					// 控制选择货仓
					deptInDom.hide();
					supplierDom.hide();
					deptOutDom.show();
					if(document.getElementById('displayPanelForDeptOut')){
						document.getElementById('displayPanelForDeptOut').style.display = 'block';
					}		
				}else if(stockSubType == 9){
					// 报损
					if(stockCate == 1){
						// 商品
						diaplayTitle = String.format("出库 -- 商品消耗");
					}else if(stockCate == 2){
						// 原料 
						diaplayTitle = String.format("出库 -- 原料消耗");
					}
					// 控制选择货仓
					deptInDom.hide();
					supplierDom.hide();
					deptOutDom.show();
					if(document.getElementById('displayPanelForDeptOut')){
						document.getElementById('displayPanelForDeptOut').style.display = 'block';
					}		
				}
			}
			//刷新组件
			secondStepPanelNorth.doLayout();
			
			if(stockTaskNavWin.otype != Ext.ux.otype['insert']){
				var sn = Ext.getCmp('stockBasicGrid').getSelectionModel().getSelected();
				document.getElementById('stockActionTitle').innerHTML = diaplayTitle + '<label style="margin-left:50px">库单编号: ' + sn.data.id + '</label>';
			}else{
				document.getElementById('stockActionTitle').innerHTML = diaplayTitle;
			}
			document.getElementById('stockActionTitle').style.display = 'block';
			document.getElementById('stockActionTotalPrice').style.display = 'block';
		}
		
		// 设置导航按钮信息
		if(index >= 1){
			btnNext.setText('完成');
		}else{
			btnNext.setText('下一步');
		}
		if(index >= 1){
			btnPrevious.setDisabled(false);
		}else{
			btnPrevious.setDisabled(true);
		}
		
	}else{
		Ext.Msg.show({
			title : '错误',
			msg : '操作失败, 系统参数错误, 请刷新页面后重试.'
		});
	}
}

/**
 * 处理数据
 * @param c
 * @returns
 */
function operateStockActionBasic(c){
	if(c == null || c.otype == null || typeof c.otype == 'undefined')
		return;
	
	var id = Ext.getCmp('hideStockActionId');
	var deptIn = Ext.getCmp('comboDeptInForStockActionBasic');
	var supplier = Ext.getCmp('comboSupplierForStockActionBasic');
	var deptOut = Ext.getCmp('comboDeptOutForStockActionBasic');
	var oriStockId = Ext.getCmp('txtOriStockIdForStockActionBasic');
	var oriStockDate = Ext.getCmp('datetOriStockDateForStockActionBasic');
	var comment = Ext.getCmp('txtCommentForStockActionBasic');
	var approverName = Ext.getCmp('txtSpproverNameForStockActionBasic');
	var approverDate = Ext.getCmp('dateSpproverDateForStockActionBasic');
	var operatorName = Ext.getCmp('txtOperatorNameForStockActionBasic');
	var operatorDate = Ext.getCmp('dateOperatorDateForStockActionBasic');
	var actualPrice = Ext.getDom('txtActualPrice');
	if(c.otype == Ext.ux.otype['set']){
		var data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		var deptInData = data.deptIn == null || typeof data.deptIn == 'undefined' ? {} : data.deptIn;
		var supplierData = data.supplier == null || typeof data.supplier == 'undefined' ? {} : data.supplier;
		var deptOutData = data.deptOut == null || typeof data.deptOut == 'undefined' ? {} : data.deptOut;
		if(typeof data.typeValue != 'undefined' && typeof data.cateTypeValue != 'undefined' && typeof data.subTypeValue != 'undefined'){
			stockTaskNavWin.stockType = data.typeValue+','+data.cateTypeValue+','+data.subTypeValue;
			stockTaskNavWin.getLayout().setActiveItem(0);
			var type = Ext.query('input[name=\"radioStockOrderType\"]');
			for(var i = 0; i < type.length; i++){
				var select = type[i].value.split(',');
				if(eval(data.typeValue == select[0] && data.cateTypeValue == select[1] && data.subTypeValue == select[2])){
					type[i].checked = true;
					break;
				}
			}
			var btnNext = Ext.getCmp('btnNextForStockNav');
			btnNext.handler(btnNext);
		}else{
			stockTaskNavWin.stockType = null;  
		}
		
		id.setValue(data['id']);
		
		setTimeout(function(){
			if(typeof deptInData['id'] != "undefined"){
				deptIn.setValue(deptInData['id']);
			}
			
			if(supplierData['supplierID']){
				supplier.setValue(supplierData['supplierID']);
			}
		}, 400);
		
		if(data['oriStockDateFormat']){
			oriStockDate.setValue(data['oriStockDateFormat']);
		}else{
			oriStockDate.setValue(new Date());
			
		}
		
		deptOut.setValue(deptOutData['id']);
		oriStockId.setValue(data['oriStockId']);		
		comment.setValue(data['comment']);
		operatorName.setValue(data['operatorName']);
		operatorDate.setValue(data['birthDateFormat']);
		actualPrice.value = data['actualPrice'];
		
		if(actualPrice.value == 'undefined'){
			actualPrice.value = "";
			Ext.getDom('txtTotalAmount').value = "";
			Ext.getDom('txtTotalPrice').value = "";
		}else{
			actualPrice.value = data['actualPrice'] + "$"; 
		}
		actualPrice.disabled = true;
		//审核或反审核显示审核人
		if(data['statusValue'] == 2 || data['statusValue'] == 3){
			approverName.setValue(data['approverName']);
			approverDate.setValue(data['approverDateFormat']);
		}else{
			approverName.setValue();
			approverDate.setValue();
		}
		
		if(typeof data.stockDetails != 'undefined' && data.stockDetails.length > 0){
			for(var i = 0; i < data.stockDetails.length; i++){
				var temp = data.stockDetails[i];
				secondStepPanelCenter.getStore().add(new StockDetailRecord({
					id : temp['id'],
					'material.id' : temp['id'],
					'material.name' : temp['materialName'],
					'material.id' : temp['materialId'],
					amount : temp['amount'],
					price : temp['price']
				}));
			}
		}else{
			secondStepPanelCenter.getStore().removeAll();
		}
		
		actualPrice.value = actualPrice.value.substring(0, (actualPrice.value.length-1));
		
		deptIn.clearInvalid();
		supplier.clearInvalid();
		deptOut.clearInvalid();
		
		var material = Ext.getCmp('comboSelectMaterialForStockAction');
		var amount = Ext.getCmp('numSelectCountForStockAction');
		var price = Ext.getCmp('numSelectPriceForStockAction');
		material.setValue();
		amount.setValue();
		price.setValue();
		material.clearInvalid();
		amount.clearInvalid();
		price.clearInvalid();
	}else if(c.otype == Ext.ux.otype['get']){
		c.data = {
			id : id.getValue(),
			deptIn : {
				id : deptIn.getValue()
			},
			supplier : {
				id : supplier.getValue()
			},
			deptOut : {
				id : deptOut.getValue()
			},
			oriStockId : oriStockId.getValue(),
			oriStockDate : oriStockDate.getValue(),
			comment : comment.getValue(),
			detail : secondStepPanelCenter.getStore()
		};
	}
	return c;
}
/**
 * 新增库存单信息
 */
function insertStockActionHandler(){
	Ext.Ajax.request({
		url : '../../OperateStockAction.do',
		params : {
			'dataSource' : 'checkStockTake'
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				stockTaskNavWin.otype = Ext.ux.otype['insert'];
				stockTaskNavWin.center();
				stockTaskNavWin.show();
				stockTaskNavWin.setTitle(stockTaskNavWin.getLayout().activeItem.mt);
				Ext.getCmp('sam_secondStepPanelWest').setDisabled(false);
				//货品添加可用
				Ext.getCmp('comboSelectMaterialForStockAction').setDisabled(false);
			}else{
				Ext.ux.showMsg(jr);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}

/**
 * 检查是否可以反审核
 */
function isableToReAudit(){
	var data = Ext.ux.getSelData(stockBasicGrid);
	Ext.Ajax.request({
		url : '../../OperateStockAction.do',
		params : {
			'dataSource' : 'checkReAudit',
			
			stockActionId : data['id']
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				updateStockActionHandler({reAudit:true});
			}else{
				Ext.ux.showMsg({
					code : 9999,
					title : '提示',
					msg : '库单审核时间小于最后盘点时间, 不能反审核'
				});
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}


/**
 * 修改出入库单信息
 */
function updateStockActionHandler(c){
	c = c || {};
	var data = Ext.ux.getSelData(stockBasicGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录后再操作.');
		return;
	}
	stockTaskNavWin.center();
	stockTaskNavWin.show();
	
	if(data['statusValue'] == 1 || c.reAudit){
		stockTaskNavWin.otype = Ext.ux.otype['update'];
		if(c.reAudit){
			stockTaskNavWin.otype = "reaudit"
		}
		stockTaskNavWin.setTitle('修改库存单信息');
		operateStockActionBasic({
			otype : Ext.ux.otype['set'],
			data : data
		});
		Ext.getCmp('btnPreviousForStockNav').setDisabled(true);
		//货品添加可用
		Ext.getCmp('comboSelectMaterialForStockAction').setDisabled(false);
		if(data['subTypeValue'] == 1 || data['subTypeValue'] == 4){
			Ext.getDom('txtActualPrice').disabled = false;
		}
		
		if(data['subTypeValue'] == 9){
			Ext.getCmp('btnNextForStockNav').setDisabled(true);
			Ext.getCmp('sam_secondStepPanelWest').setDisabled(true);
			Ext.getCmp('btnAuditStockAction').hide();
		}else{
			Ext.getCmp('sam_secondStepPanelWest').setDisabled(false);
			Ext.getCmp('btnAuditStockAction').hide();
		}
	}else{
		stockTaskNavWin.otype = Ext.ux.otype['select'];
		stockTaskNavWin.setTitle('查看库存单信息');
		operateStockActionBasic({
			otype : Ext.ux.otype['set'],
			data : data
		});
		Ext.getCmp('btnPreviousForStockNav').setDisabled(true);
		Ext.getCmp('btnNextForStockNav').setDisabled(true);
		Ext.getCmp('sam_secondStepPanelWest').setDisabled(true);
		//货品添加禁用
		Ext.getCmp('comboSelectMaterialForStockAction').setDisabled(true);
		Ext.getDom('txtActualPrice').disabled = true;
		Ext.getCmp('btnAuditStockAction').hide();
		Ext.getCmp('datetOriStockDateForStockActionBasic').clearInvalid();
	}
}
/**
 * 删除库存单信息
 */
function deleteStockActionHandler(){
	var data = Ext.ux.getSelData(stockBasicGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录后再操作.');
		return;
	}
	if(data['statusValue'] != 1){
		Ext.example.msg('提示', '操作失败, 已审核单据不能删除.');
		return;
	}
	Ext.Msg.show({
		title : '重要',
		msg : '是否删除库存单',
		buttons : Ext.Msg.YESNO,
		icon: Ext.Msg.QUESTION,
		fn : function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateStockAction.do',
					params : {
						'dataSource' : 'delete',
						
						id : data['id']
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
//							stockTaskNavWin.hide();
							stockBasicGrid.getStore().reload();
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
	});
}
/**
 * 审核库存单信息
 */
function auditStockActionHandler(){
	var data = Ext.ux.getSelData(stockBasicGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录后再操作.');
	}
	if(data['statusValue'] != 1){
		Ext.example.msg('提示', '操作失败, 该库存单已审核.');
		return;
	}
	Ext.Msg.show({
		title : '重要',
		msg : '是否审核库存单',
		buttons : Ext.Msg.YESNO,
		icon: Ext.Msg.QUESTION,
		fn : function(e){
			if(e == 'yes'){
				Ext.Ajax.request({
					url : '../../OperateStockAction.do',
					params : {
						'dataSource' : 'audit',
						id : data['id']
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							stockTaskNavWin.hide();
							Ext.getCmp('btnSearchForStockBasicMsg').handler();
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
	});
}
/**
 * 设置货品数量
 * @param c
 */
function setAmountForStockActionDetail(c){
	if(!currRecordMaterialId){
		Ext.example.msg('提示', '操作失败, 请选中一条记录后再操作.');
	}
	
	for(var i = 0; i < secondStepPanelCenter.getStore().getCount(); i++){
		var temp = secondStepPanelCenter.getStore().getAt(i);
		if(temp.get('material.id') == currRecordMaterialId){
			if(c.otype == Ext.ux.otype['delete']){
				secondStepPanelCenter.getStore().remove(temp);
			}else if(c.otype == Ext.ux.otype['set']){
				temp.set('amount', c.amount);
			}else{
				var na = temp.get('amount') + c.amount;
				if(na <= 0){
					secondStepPanelCenter.getStore().remove(temp);
				}else{
					temp.set('amount', na);
				}
			}
			break;
		}
	}
	currRecordMaterialId = null;
}

/**
 * grid导出表格
 */
function exportExcel(){
	var sn = Ext.getCmp('stockBasicGrid').getSelectionModel().getSelected();
	var url = '../../{0}?pin={1}&id={2}&dataSource={3}';
	url = String.format(
		url,
		'ExportHistoryStatisticsToExecl.do',
		-10,
		sn.data.id,
		'stockAction'
	);
	window.location = url;
}
/**
 * 新增, 修改时导出表格
 * @param id
 */
function operateExportExcel(id){
	var url = '../../{0}?pin={1}&id={2}&dataSource={3}';
	url = String.format(
		url,
		'ExportHistoryStatisticsToExecl.do',
		-10,
		id,
		'stockAction'
	);
	window.location = url;
}

function getCurrentDay(){
	Ext.Ajax.request({
		url : '../../QueryCurrentMonth.do',
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.other.minDay){
				Ext.getCmp('datetOriStockDateForStockActionBasic').setMinValue(NewDate(jr.other.minDay));
			}
			if(NewDate(jr.other.currentDay) < new Date()){
				Ext.getCmp('datetOriStockDateForStockActionBasic').setValue(NewDate(jr.other.currentDay));
				Ext.getCmp('datetOriStockDateForStockActionBasic').setMaxValue(NewDate(jr.other.currentDay));
			}else{
				Ext.getCmp('datetOriStockDateForStockActionBasic').setMaxValue(new Date());
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
		
	});
}

function showCurrentMonth(){
	Ext.Ajax.request({
		url : '../../QueryCurrentMonth.do',
		params : {
			restaurantID : restaurantID
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				Ext.getDom('sam_labCurrentMonth').innerHTML = jr.msg;
			}else{
				var date = new Date();
				Ext.getDom('sam_labCurrentMonth').innerHTML = (date.getMonth() + 1);
			}
		},
		failure : function(res, opt){
			Ext.ux.showMsg(Ext.decode(res.responseText));
		}
	});
}
//--------------

//---------------load

function stockOperateRenderer(v, m, r, ri, ci, s){
	if(r.get('statusValue') == 1){
		if(r.get('subTypeValue') == 9){
			return '<a href="javascript:updateStockActionHandler();">查看</a>';
		}else{
			return ''
			+ '<a href="javascript:exportExcel();">导出</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:updateStockActionHandler();">修改</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:auditStockActionHandler();">审核</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:deleteStockActionHandler();">删除</a>';
		}
	}else if(r.get('statusValue') == 4){
		return ''
			+ '<a href="javascript:exportExcel();">导出</a>'
			+ '&nbsp;&nbsp;&nbsp;&nbsp;'
			+ '<a href="javascript:updateStockActionHandler();">查看</a>';
	}else{
		return ''
		+ '<a href="javascript:isableToReAudit();">反审核</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:exportExcel();">导出</a>'
		+ '&nbsp;&nbsp;&nbsp;&nbsp;'
		+ '<a href="javascript:updateStockActionHandler();">查看</a>';		
	}
}

function showStockCountMenuHandler(event){
	if(!menuStockDetailAmount){
		showStockCountMenu();
	}
	menuStockDetailAmount.showAt([event.clientX, event.clientY]);
}

function stockDetailTotalCountRenderer(v, m, r, ri, ci, s){
	if(stockTaskNavWin.otype == Ext.ux.otype['select']){
		return Ext.ux.txtFormat.gridDou(r.get('amount'));
	}else{
		return Ext.ux.txtFormat.gridDou(r.get('amount'))
			+ '<a href="javascript:setAmountForStockActionDetail({amount:1});"><img src="../../images/btnAdd.gif" title="数量+1"/></a>&nbsp;'
			+ '<a href="javascript:setAmountForStockActionDetail({amount:-1});"><img src="../../images/btnDelete.png" title="数量-1"/></a>&nbsp;'
			+ '<a href="javascript:" onClick="showStockCountMenuHandler(event)"><img src="../../images/icon_tb_setting.png" title="设置数量"/></a>&nbsp;'
			+ '<a href="javascript:setAmountForStockActionDetail({otype:Ext.ux.otype[\'delete\']});"><img src="../../images/btnCancel.png" title="删除该记录"/></a>'
			+ '';
	}
}
function stockDetailTotalPriceRenderer(v, m, r, ri, ci, s){
	return Ext.ux.txtFormat.gridDou(r.get('amount') * r.get('price'));
}


function stockDetailOpertionRenderer(){
	return "<a href=\"javascript:void(0);\" onclick=\"setAmountForStockActionDetail({otype:Ext.ux.otype[\'delete\']})\">" + "<img src='../../images/del.png'/>删除</a>";
}


function showUnitPriceMenuHandler(event){
	if(!menuStockDetailPrice){
		showUnitPriceMenu();
	}
	menuStockDetailPrice.showAt([event.clientX, event.clientY]);
}

function stockDetailPriceRenderer(v, m, r, ri, ci, s){
	if(stockTaskNavWin.otype == Ext.ux.otype['select']){
		return Ext.ux.txtFormat.gridDou(r.get('price'));
	}else{
		return Ext.ux.txtFormat.gridDou(r.get('price'))
		+ '<a href="javascript:" onClick="showUnitPriceMenuHandler(event)"><img src="../../images/icon_tb_setting.png" title="设置单价"/></a>&nbsp;';
	}
}
function stockTypeRenderer(v, m, r, ri, ci, s){
	return r.get('typeText') + ' -- ' +r.get('subTypeText');
}
function stockInRenderer(v, m, r, ri, ci, s){
	var display = '', t = r.get('typeValue'), st = r.get('subTypeValue');
	if(t == 1){
		if(st == 1 || st == 2 || st == 3 || st == 7){
			display = r.get('deptIn')['name'];
		}
	}else if(t == 2){
		if(st == 4){
			display = r.get('supplier')['name'];
		}else if(st == 5){
			display = r.get('deptIn')['name'];
		}
	}
	return display;
}

function stockOutRenderer(v, m, r, ri, ci, s){
	var display = '', t = r.get('typeValue'), st = r.get('subTypeValue');
	if(t == 1){
		if(st == 1){
			display = r.get('supplier')['name'];
		}else if(st == 2){
			display = r.get('deptOut')['name'];
		}
	}else if(t == 2){
		display = r.get('deptOut')['name'];
	}
	return display;
}

function displayPrice(v){
	if(v == 0){
		return "----";
	}else{
		return Ext.ux.txtFormat.gridDou(v);
	}
}
function IsNum(e) {
	    var k = window.event ? e.keyCode : e.which;
	    if (((k >= 48) && (k <= 57)) || k == 8 || k == 0) {
	    } else {
	        if (window.event) {
	            window.event.returnValue = false;
	        }
	        else {
	            e.preventDefault();
	        }
	    }
} 
function initControl(){
	var stockInDate = [[-1, '全部'], [1, '采购'], [2, '入库调拨'], [3, '报溢'], [7, '盘盈']];
	var stockOutDate = [[-1, '全部'], [4, '退货'], [5, '出库调拨'], [6, '报损'], [8, '盘亏'], [9, '消耗']];
	
	var stockBasicGridTbar = new Ext.Toolbar({
		height : 26,
		items : [{
			xtype : 'tbtext',
			text : '货单类型:'
		}, {
			xtype : 'combo',
			id : 'comboSearchForStockType',
			readOnly : false,
			forceSelection : true,
			width : 60,
			value : 1,
			store : new Ext.data.SimpleStore({
				data : winParams.st,
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			listeners : {
				select : function(thiz){
					var subType = Ext.getCmp('sam_comboSearchForSubType');
					if(thiz.getValue() == 1){
						subType.store.loadData(stockInDate);
						subType.setValue(1);
					}else{
						subType.store.loadData(stockOutDate);
						subType.setValue(-1);
					}
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;业务类型:'
		}, {
			xtype : 'combo',
			id : 'sam_comboSearchForSubType',
			readOnly : false,
			forceSelection : true,
			width : 90,
			value : 1,
			store : new Ext.data.SimpleStore({
				data : stockInDate,
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				select : function(){
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;货品类型:'
		}, {
			xtype : 'combo',
			id : 'sam_comboSearchForCateType',
			readOnly : false,
			forceSelection : true,
			width : 60,
			value : -1,
			store : new Ext.data.SimpleStore({
				data : winParams.cate,
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			listeners : {
				select : function(){
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;仓库:'
		}, {
			xtype : 'combo',
			id : 'sam_comboSearchForDept',
			fieldLabel : '仓库',
			width : 100,
			readOnly : false,
			forceSelection : true,
			store : new Ext.data.SimpleStore({
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			blankText : '盘点仓库不允许为空.',
			listeners : {
				select : function(){
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				},
				render : function(thiz){
					var data = [[-1,'全部']];
					Ext.Ajax.request({
						url : '../../QueryDept.do',
						params : {
							dataSource : 'normal'
							
						},
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.root.length; i++){
								data.push([jr.root[i]['id'], jr.root[i]['name']]);
							}
							thiz.store.loadData(data);
							thiz.setValue(-1);
						},
						fialure : function(res, opt){
							thiz.store.loadData(data);
							thiz.setValue(-1);
						}
					});
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;审核状态:'
		}, {
			xtype : 'combo',
			id : 'sam_comboSearchForStockStatus',
			readOnly : false,
			forceSelection : true,
			width : 80,
			value : -1,
			store : new Ext.data.SimpleStore({
				data : [[-1, '全部'], [1, '未审核'], [2, '审核通过'], [3, ' 冲红']],
				fields : ['value', 'text']
			}),
			valueField : 'value',
			displayField : 'text',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			allowBlank : false,
			listeners : {
				select : function(){
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;供应商:'
		}, {
			xtype : 'combo',
			id : 'sam_comboSearchForSupplier',
			readOnly : false,
			forceSelection : true,
			width : 103,
			listWidth : 120,
			store : new Ext.data.SimpleStore({
				fields : ['supplierID', 'name']
			}),
			valueField : 'supplierID',
			displayField : 'name',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true,
			listeners : {
				render : function(thiz){
					var data = [[-1,'全部']];
					Ext.Ajax.request({
						url : '../../QuerySupplier.do',
						success : function(res, opt){
							var jr = Ext.decode(res.responseText);
							for(var i = 0; i < jr.root.length; i++){
								data.push([jr.root[i]['supplierID'], jr.root[i]['name']]);
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
					Ext.getCmp('btnSearchForStockBasicMsg').handler();
				}
			}
			
		}, {
			xtype : 'tbtext',
			text : '&nbsp;&nbsp;货单编号/原始单号:'
		}, {
			xtype : 'textfield',
			id : 'comboSearchForOriStockId',
			width : 80
		}, '->', {
			text : '重置',
			id : 'btnReload',
			iconCls : 'btn_refresh',
			handler : function(){
				Ext.getCmp('comboSearchForStockType').setValue(1);
				Ext.getCmp('sam_comboSearchForSubType').setValue(1);
				Ext.getCmp('sam_comboSearchForCateType').setValue(1);
				Ext.getCmp('sam_comboSearchForDept').setValue(-1);
				Ext.getCmp('sam_comboSearchForStockStatus').setValue(-1);
				Ext.getCmp('sam_comboSearchForSupplier').setValue(-1);
				Ext.getCmp('comboSearchForOriStockId').setValue();
				Ext.getCmp('btnSearchForStockBasicMsg').handler();
				//location.reload(false);
			}
		}, {
			text : '搜索',
			id : 'btnSearchForStockBasicMsg',
			iconCls : 'btn_search',
			handler : function(e){
				var st = Ext.getCmp('comboSearchForStockType');
				var cate = Ext.getCmp('sam_comboSearchForCateType');
				var dept = Ext.getCmp('sam_comboSearchForDept');
				var oriStockId = Ext.getCmp('comboSearchForOriStockId');
				var status = Ext.getCmp('sam_comboSearchForStockStatus');
				var supplier = Ext.getCmp('sam_comboSearchForSupplier');
				var subType = Ext.getCmp('sam_comboSearchForSubType');
				
				var gs = stockBasicGrid.getStore();
				gs.baseParams['stockType'] = st.getValue();
				gs.baseParams['cateType'] = cate.getValue();
				gs.baseParams['dept'] = dept.getValue();
				gs.baseParams['oriStockId'] = oriStockId.getValue();
				gs.baseParams['status'] = status.getValue() != -1 ? status.getValue() : '';
				gs.baseParams['supplier'] = supplier.getValue();
				gs.baseParams['subType'] = subType.getValue();
				gs.load({
					params : {
						start : 0,
						limit : stockBasicGrid.getBottomToolbar().pageSize
					}
				});
			}
		}]
	});
	
	stockBasicGrid = createGridPanel(
		'stockBasicGrid',
		'',
		'',
		'',
		'../../QueryStockAction.do',
		[
			[true, false, false, true],
			['货单编号', 'id', 50],
			['货单类型', 'typeText',,,'stockTypeRenderer'],
			['货品类型', 'cateTypeText', 50],
			['原始单号', 'oriStockId'],
			['时间', 'oriStockDateFormat', 65],
			['出库仓/供应商', 'center', 65,,'stockOutRenderer'],
			['收货仓/供应商', 'center', 65,,'stockInRenderer'],
			['数量', 'amount',60,'right','Ext.ux.txtFormat.gridDou'],
			['应收金额', 'price',80,'right', displayPrice],
			['实际金额', 'actualPrice', 80, 'right', displayPrice],
			['制单人', 'operatorName', 60],
			['审核人', 'approverName', 60],
			['审核状态', 'statusText', 60, 'center'],
			['操作', 'center', 150, 'center', 'stockOperateRenderer']
		],
		StockRecord.getKeys(),
		[['isPaging', true],  ['restaurantId', restaurantID]],
		GRID_PADDING_LIMIT_20,
		'',
		stockBasicGridTbar
	);
	stockBasicGrid.region = 'center';
	
	
	stockBasicGrid.keys = [{
		key : Ext.EventObject.ENTER,
		scope : this,
		fn : function(){
			Ext.getCmp('btnSearchForStockBasicMsg').handler();
		}
	}];
	stockBasicGrid.on('render', function(thiz){
		//Ext.getCmp('btnSearchForStockBasicMsg').handler();
	});
	stockBasicGrid.on('rowdblclick', function(){
		updateStockActionHandler();		
	});
	stockBasicGrid.getStore().on('load', function(store, records, options){
		var sumRow;
		for(var i = 0; i < records.length; i++){
			if(eval(records[i].get('statusValue') != 1)){
				sumRow = stockBasicGrid.getView().getRow(i);
				sumRow.style.backgroundColor = '#DDD';
				sumRow = null;
			}
		}
		sumRow = null;
		if(store.getCount() > 0){

			sumRow = stockBasicGrid.getView().getRow(store.getCount() - 1);	
			sumRow.style.backgroundColor = '#EEEEEE';			
			for(var i = 0; i < stockBasicGrid.getColumnModel().getColumnCount(); i++){
				var sumCell = stockBasicGrid.getView().getCell(store.getCount() - 1, i);
				sumCell.style.fontSize = '15px';
				sumCell.style.fontWeight = 'bold';
				sumCell.style.color = 'green';
			}
			
			
			stockBasicGrid.getView().getCell(store.getCount()-1, 1).innerHTML = '汇总';
			stockBasicGrid.getView().getCell(store.getCount()-1, 2).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 3).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 4).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 5).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 6).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 7).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 8).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 11).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 12).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 13).innerHTML = '--';
			stockBasicGrid.getView().getCell(store.getCount()-1, 14).innerHTML = '--';
			
		}
		
	});
	if(!firstStepPanel){
		firstStepPanel = new Ext.Panel({
	    	mt : '操作货单共二步, <span style="color:#000;">现为第一步:选择单据类型</font>',
	        index : 0,
	        frame : true,
	        items : [{
	        	xtype : 'fieldset',
	        	title : '入库单',
	        	layout : 'column',
	        	height : Ext.isIE ? 150 : 138,
	        	items : [{
	        		columnWidth : .2,
	        		items : [{
	        			html : '&nbsp;'
	        		}, {
	        			layout : 'form',
	            		hidden : true,
	            		items : [{
	            			xtype : 'radio',
	    	        		inputValue : [1,1,7],
	    	        		name : 'radioStockOrderType',
	    	        		hideLabel : true,
	    	        		boxLabel : '商品盘盈'
	            		}, {
	            			xtype : 'radio',
	    	        		inputValue : [2,1,8],
	    	        		name : 'radioStockOrderType',
	    	        		hideLabel : true,
	    	        		boxLabel : '商品盘亏'
	            		}, {
	            			xtype : 'radio',
	    	        		inputValue : [2,1,9],
	    	        		name : 'radioStockOrderType',
	    	        		hideLabel : true,
	    	        		boxLabel : '商品消耗'
	            		}, {
	            			xtype : 'radio',
	    	        		inputValue : [2,2,7],
	    	        		name : 'radioStockOrderType',
	    	        		hideLabel : true,
	    	        		boxLabel : '原料盘盈'
	            		}, {
	            			xtype : 'radio',
	    	        		inputValue : [2,2,8],
	    	        		name : 'radioStockOrderType',
	    	        		hideLabel : true,
	    	        		boxLabel : '原料盘亏'
	            		}, {
	            			xtype : 'radio',
	    	        		inputValue : [2,2,9],
	    	        		name : 'radioStockOrderType',
	    	        		hideLabel : true,
	    	        		boxLabel : '原料消耗'
	            		}]
	        		}]
	        	}, {
		        	columnWidth : .15,
		        	xtype : 'fieldset',
		        	title : '商品入库',
		        	height : Ext.isIE ? 100 : 115,
		        	bodyStyle : 'padding:3px 0px 0px 10px; ',
		        	items : [{
		        		id : 'radioStockOrderTypeGoodIn',
		        		xtype : 'radio',
		        		inputValue : [1,1,1],
		        		name : 'radioStockOrderType',
		        		checked : true,
		        		hideLabel : true,
		        		boxLabel : '商品采购'
		        	}, {
		        		xtype : 'radio',
		        		inputValue : [1,1,2],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '商品调拨'
		        	}, {
		        		xtype : 'radio',
		        		inputValue : [1,1,3],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '商品报溢'
		        	}]
		        }, {
		        	columnWidth : .25,
		        	html : '&nbsp;'
		        }, {
		        	columnWidth : .15,
		        	xtype : 'fieldset',
		        	bodyStyle : 'padding:3px 0px 0px 10px; ',
		        	title : '原料入库',
		        	height : Ext.isIE ? 100 : 115,
		        	items : [{
		        		xtype : 'radio',
		        		inputValue : [1,2,1],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '原料采购'
		        	}, {
		        		xtype : 'radio',
		        		inputValue : [1,2,2],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '原料调拨'
		        	}, {
	//	        		hidden : true,
	//	        		hideParent : true,
		        		xtype : 'radio',
		        		inputValue : [1,2,3],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '原料报溢'
		        	}]
		        }]
	        }, {
	        	xtype : 'fieldset',
	        	title : '出库单',
	        	layout : 'column',
	        	height : Ext.isIE ? 150 : 138,
	        	items : [{
	        		columnWidth : .2,
	        		html : '&nbsp;'
	        	}, {
		        	columnWidth : .15,
		        	xtype : 'fieldset',
		        	title : '商品出库',
		        	height : Ext.isIE ? 100 : 115,
		        	bodyStyle : 'padding:3px 0px 0px 10px; ',
		        	items : [{
		        		xtype : 'radio',
		        		inputValue : [2,1,4],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '商品退货'
		        	}, {
		        		xtype : 'radio',
		        		inputValue : [2,1,5],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '商品调拨'
		        	}, {
		        		xtype : 'radio',
		        		inputValue : [2,1,6],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '商品报损'
		        	}]
		        }, {
	        		columnWidth : .25,
	        		html : '&nbsp;'
	        	}, {
		        	columnWidth : .15,
		        	xtype : 'fieldset',
		        	bodyStyle : 'padding:3px 0px 0px 10px; ',
		        	title : '原料出库',
		        	height : Ext.isIE ? 100 : 115,
		        	items : [{
		        		xtype : 'radio',
		        		inputValue : [2,2,4],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '原料退货'
		        	}, {
		        		xtype : 'radio',
		        		inputValue : [2,2,5],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '原料调拨'
		        	}, {
		        		xtype : 'radio',
		        		inputValue : [2,2,6],
		        		name : 'radioStockOrderType',
		        		hideLabel : true,
		        		boxLabel : '原料报损'
		        	}]
		        }]
	        }]
	    });
	}
	
	if(!secondStepPanelNorth){
		secondStepPanelNorth = new Ext.Panel({
			title : '货单基础信息',
			id : 'sam_secondStepPanelNorth',
	    	region : 'north',
	    	height : 143,
	    	frame : true,
	    	items : [{
	    		id : 'displayPanelForStockActionTitle',
	    		height : 30,
	    		bodyStyle : 'font-size:18px;text-align:center;',
	    		contentEl : 'stockActionTitle'
		    	}
		    	, {
					xtype : 'hidden',
					id : 'hideStockActionId'
		    	},{
		    		xtype : 'panel',
		    		layout : 'column',
		    		defaults : {
	    			xtype : 'form',
	    			layout : 'form',
	    			style : 'width:218px;',
	    			labelWidth : 60,
	    			columnWidth : .24,
	    			defaults : { width : 120 }
	    		},
	    		items : [{
	    			id : 'displayPanelForDeptIn',
	    			items : [{
	    				id : 'comboDeptInForStockActionBasic',
	    				xtype : 'combo',
	    				fieldLabel : '收货仓',
	    				readOnly : false,
	    				forceSelection : true,
	    				width : 103,
	    				listWidth : 120,
	    				store : new Ext.data.JsonStore({
	    					url: '../../QueryDept.do?',
	    					baseParams : {
	    						dataSource : 'normal'
	    						
	    					},
	    					root : 'root',
	    					fields : DeptRecord.getKeys()
	    				}),
	    				valueField : 'id',
	    				displayField : 'name',
	    				typeAhead : true,
	    				mode : 'local',
	    				triggerAction : 'all',
	    				selectOnFocus : true,
	    				allowBlank : false,
	    				blankText : '收货仓不允许为空.',
	    				listeners : {
	    					render : function(thiz){
	    						thiz.store.load();
	    					}
	    				}
	    			}]
	    		}, {
	    			id : 'displayPanelForSupplier',
	    			items : [{
	    				id : 'comboSupplierForStockActionBasic',
	    				xtype : 'combo',
	    				fieldLabel : '供应商',
	    				readOnly : false,
	    				forceSelection : true,
	    				width : 103,
	    				listWidth : 120,
	    				store : new Ext.data.JsonStore({
	    					url: '../../QuerySupplier.do',
	    					root : 'root',
	    					fields : SupplierRecord.getKeys()
	    				}),
	    				valueField : 'supplierID',
	    				displayField : 'name',
	    				typeAhead : true,
	    				mode : 'local',
	    				triggerAction : 'all',
	    				selectOnFocus : true,
	    				allowBlank : false,
	    				blankText : '供应商不允许为空.',
	    				listeners : {
	    					render : function(thiz){
	    						thiz.store.load();
	    					}
	    				}
	    			}]
	    		}, {
	    			id : 'displayPanelForDeptOut',
	    			items : [{
	    				id : 'comboDeptOutForStockActionBasic',
	    				xtype : 'combo',
	    				fieldLabel : '出货仓',
	    				readOnly : false,
	    				forceSelection : true,
	    				width : 103,
	    				listWidth : 120,
	    				store : new Ext.data.JsonStore({
	    					url: '../../QueryDept.do?',
	    					baseParams : {
	    						dataSource : 'normal'
	    						
	    					},
	    					root : 'root',
	    					fields : DeptRecord.getKeys()
	    				}),
	    				valueField : 'id',
	    				displayField : 'name',
	    				typeAhead : true,
	    				mode : 'local',
	    				triggerAction : 'all',
	    				selectOnFocus : true,
	    				allowBlank : false,
	    				blankText : '收货仓不允许为空.',
	    				listeners : {
	    					render : function(thiz){
	    						thiz.store.load();
	    					}
	    				}
	    			}]
	    		}, {
	    			items : [{
	    				id : 'txtOriStockIdForStockActionBasic',
	    				xtype : 'textfield',
	    				fieldLabel : '原始单号'
	    			}]
	    		}, {
	    			items : [{
					id : 'datetOriStockDateForStockActionBasic',
					xtype : 'datefield',
					width : 103,
					fieldLabel : '日期',
					value : new Date(),
					format : 'Y-m-d',
					readOnly : false,
					allowBlank : false,
					blankText : '日期不能为空, 且小于当前会计月月底并大于该月最后一次盘点时间.',
					listeners : {
						blur : function(thiz){
							thiz.clearInvalid();		
						}
					}
				}]
	    		}, {
	    			columnWidth : 1,
	    			style : 'width:100%;',
	    			items : [{
	    				id : 'txtCommentForStockActionBasic',
	    				xtype : 'textfield',
	    				width : 774,
	    				fieldLabel : '备注'
	    			}]
	    		}, {
	    			items : [{
	    				id : 'txtSpproverNameForStockActionBasic',
	    				xtype : 'textfield',
	    				fieldLabel : '审核人',
	    				disabled : true
	    			}]
	    		}, {
	    			items : [{
	    				id : 'dateSpproverDateForStockActionBasic',
	    				xtype : 'textfield',
	    				fieldLabel : '审核日期',
	    				disabled : true
	    			}]
	    		}, {
	    			items : [{
	    				id : 'txtOperatorNameForStockActionBasic',
	    				xtype : 'textfield',
	    				fieldLabel : '制单人',
	    				disabled : true
	    			}]
	    		}, {
	    			items : [{
	    				id : 'dateOperatorDateForStockActionBasic',
	    				xtype : 'textfield',
	    				fieldLabel : '制单日期',
	    				disabled : true
	    			}]
	    		}]
	    	}]
	    });
	}
	var stockAddMarterialGridTbar = new Ext.Toolbar({
			height : 26,
			items : [{
				xtype : 'tbtext',
				text : '选择货品:&nbsp;&nbsp;'
			},{
				xtype : 'combo',
				id : 'comboSelectMaterialForStockAction',
				fieldLabel : '选择货品',
				forceSelection : true,
				listWidth : 250,
				height : 200,
				maxHeight : 300,
				store : new Ext.data.JsonStore({
					url : '../../QueryMaterial.do',
					baseParams : {
						dataSource : 'normal',
						
						restaurantID : restaurantID
					},
					root : 'root',
					fields : MaterialRecord.getKeys()
				}),
				valueField : 'id',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				tpl:'<tpl for=".">' 
					+ '<div class="x-combo-list-item" style="height:18px;">'
					+ '{id} -- {name} -- {pinyin}'
					+ '</div>'
					+ '</tpl>',
				listeners : {
					beforequery : function(e){ 
						var combo = e.combo; 
						if(!e.forceAll){ 
							var value = e.query; 
							combo.store.filterBy(function(record,id){
								return record.get('name').indexOf(value) != -1 
										|| (record.get('id')+'').indexOf(value) != -1 
										|| record.get('pinyin').indexOf(value.toUpperCase()) != -1;
							}); 
							combo.expand(); 
							combo.select(0, true);
							return false; 
						}
					},
					select : function(thiz){
						thiz.focus(true, 100);
						var newRecord = null;
		    			for(var i=0, temp=thiz.store, sv=thiz.getValue(); i<temp.getCount(); i++){
		    				if(temp.getAt(i).get('id') == sv){
		    					newRecord = temp.getAt(i);
		    					break;
		    				}
		    			}
						var detail = secondStepPanelCenter.getStore();
		    			var has = false;
		    			for(var i=0; i < detail.getCount(); i++){
		    				if(detail.getAt(i).get('material.id') == newRecord.get('id')){
		    					detail.getAt(i).set('amount', detail.getAt(i).get('amount') + 1);
		    					has = true;
		    					break;
		    				}
		    			}
		    			if(!has){
		    				detail.add(new StockDetailRecord({
		    					material : newRecord.data,
		    					id : newRecord.get('id'),
		    					'material.id' : newRecord.get('id'),
		    					'material.cateName' : newRecord.get('cateName'),
		    					'material.name' : newRecord.get('name'),
		    					amount : 1,
		    					price : newRecord.get('price')
		    				}));
		    			}
					}
				}
			}]
	});
    
	if(!secondStepPanelCenter){
		
		var cm = new Ext.grid.ColumnModel([
	       new Ext.grid.RowNumberer(),
		   {header:'货品名称',dataIndex:'material.name'},
		   {header:'数量',dataIndex:'amount',align : 'right',
				editor: new Ext.form.NumberField({
						id : 'stockDetailCountEditor',
				      allowNegative: false,
				      selectOnFocus:true,
				      enableKeyEvents: true
				})},
		   {header:'单价',dataIndex:'price',align : 'right',
		   		editor: new Ext.form.NumberField({
				      allowNegative: false,
				      selectOnFocus:true,
				      enableKeyEvents: true
				})},
		   {header:'总价',dataIndex:'totalPrice',align : 'right', renderer : stockDetailTotalPriceRenderer},
		   {header:'操作',align : 'center', renderer : stockDetailOpertionRenderer}
	       ]);
	  	cm.defaultSortable = true;
	  	
		var bbds = new Ext.data.Store({
			proxy : new Ext.data.MemoryProxy({}),
			reader : new Ext.data.JsonReader({totalProperty:'totalProperty', root:'root'},[
				{name : 'id'},
		        {name : 'stockActionId'},
		        {name : 'stock'},
		        {name : 'price'},
		        {name : 'amount'},
		        {name : 'totalPrice'},
		        {name : 'material.cateName'},
		        {name : 'material.name'},
		        {name : 'material'},
		        {name : 'material.id'}
			])
		});
		secondStepPanelCenter = new Ext.grid.EditorGridPanel({
			title : '货品列表',
			id : 'secondStepPanelCenter',
		    //height : '500',
		    border : false,
		    frame : false,
		    store : bbds,
		    cm : cm,
		    viewConfig : {
		    	forceFit : true
		    },
		    tbar : stockAddMarterialGridTbar,
		    listeners : {
		    	rowmousedown: function(g, index, e){  
		            currRecordMaterialId = secondStepPanelCenter.getStore().getAt(index).get('material.id');
		    	}
		    }
		});
		
	}

	secondStepPanelCenter.region = 'center';
	secondStepPanelCenter.getStore().on('load', function(thiz, rs){
		var totalPrice = 0, amount = 0;
		var txtActualPrice = Ext.getDom('txtActualPrice').value;
		
		for(var i = 0; i < secondStepPanelCenter.getStore().getCount(); i++){
			totalPrice += (Math.round(parseFloat(secondStepPanelCenter.getStore().getAt(i).get('amount') * secondStepPanelCenter.getStore().getAt(i).get('price')) * 100) / 100);
			amount += secondStepPanelCenter.getStore().getAt(i).get('amount');
		}
		Ext.getDom('txtTotalAmount').value = amount;
		Ext.getDom('txtTotalPrice').value = totalPrice.toFixed(2);
		//修改库单则不改变实际金额
		if(txtActualPrice.indexOf('$') < 0){
			Ext.getDom('txtActualPrice').value = totalPrice.toFixed(2);
		}
		
	});
	secondStepPanelCenter.getStore().on('add', function(thiz, rs){
		secondStepPanelCenter.getStore().fireEvent('load', thiz, rs);
	});
	secondStepPanelCenter.getStore().on('remove', function(thiz, rs){
		secondStepPanelCenter.getStore().fireEvent('load', thiz, rs);
	});
	secondStepPanelCenter.getStore().on('update', function(thiz, rs){
		secondStepPanelCenter.getStore().fireEvent('load', thiz, rs);
	});
	
	Ext.getCmp('stockDetailCountEditor').hide();
	
	var secondStepPanelWest = Ext.getCmp('sam_secondStepPanelWest');
	if(!secondStepPanelWest){
		secondStepPanelWest = new Ext.Panel({
	        title : '添加货品',
	        id : 'sam_secondStepPanelWest',
	        layout : 'form',
	    	frame : true,
	    	width : 220,
	    	labelWidth : 60,
	    	defaults : {
	    		width : 120
	    	},
	    	items : [{
				xtype : 'combo',
				fieldLabel : '货品',
				forceSelection : true,
				listWidth : 250,
				height : 200,
				maxHeight : 300,
				store : new Ext.data.JsonStore({
					url : '../../QueryMaterial.do',
					baseParams : {
						dataSource : 'normal',
						restaurantID : restaurantID
					},
					root : 'root',
					fields : MaterialRecord.getKeys()
				}),
				valueField : 'id',
				displayField : 'name',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				selectOnFocus : true,
				allowBlank : false,
				tpl:'<tpl for=".">' 
					+ '<div class="x-combo-list-item" style="height:18px;">'
					+ '{id} -- {name} -- {pinyin}'
					+ '</div>'
					+ '</tpl>',
				listeners : {
					beforequery : function(e){ 
						var combo = e.combo; 
						if(!e.forceAll){ 
							var value = e.query; 
							combo.store.filterBy(function(record,id){
								return record.get('name').indexOf(value) != -1 
										|| (record.get('id')+'').indexOf(value) != -1 
										|| record.get('pinyin').indexOf(value.toUpperCase()) != -1;
							}); 
							combo.expand(); 
							combo.select(0, true);
							return false; 
						}
					},
					select : function(thiz){
						var price = Ext.getCmp('numSelectPriceForStockAction');
		    			var count = Ext.getCmp('numSelectCountForStockAction');
						var stockTypeList = stockTaskNavWin.stockType.split(',');
						var stockSubType = stockTypeList[2];
						if(stockSubType == 3 || stockSubType == 6 || stockSubType == 2 || stockSubType == 5){
							Ext.Ajax.request({
								url : '../../QueryMaterial.do',
								params : {
									dataSource : 'normal',
									
									restaurantID : restaurantID,
									materialId : thiz.getValue()
								},
								success : function(res, opt){
									var jr = Ext.decode(res.responseText);
									if(jr.success){
										price.setValue(jr.root[0].price);
										price.setDisabled(true);
									}else{
										Ext.ux.showMsg(jr);
									}
								},
								failure : function(res, opt){
									Ext.ux.showMsg(Ext.decode(res.responseText));
								}
							});
						}else{
							price.setValue(0);
						}
	
		    			count.setValue(1);
		    			count.focus(true, 100);
					}
				}
			}, {
				id : 'numSelectCountForStockAction',
	    		xtype : 'numberfield',
	    		fieldLabel : '数量',
	    		maxValue : 65535,
	    		allowBlank : false
	    	},
	    	{
	    		id : 'numSelectPriceForStockAction',
	    		fieldLabel : '单价',
	    		xtype : 'numberfield',
	    		allowBlank : false,
	    		listeners : {
	    			focus : function(thiz){
						 Ext.getCmp('numSelectPriceForStockAction').focus(true, 100);
	    			}
	    		}
	    	}],
	    	buttonAlign : 'center',
	    	buttons : [{
	    		text : '添加',
	    		handler : function(e){
	    			var material = Ext.getCmp('comboSelectMaterialForStockAction');
	    			var amount = Ext.getCmp('numSelectCountForStockAction');
	    			var price = Ext.getCmp('numSelectPriceForStockAction');
	    			var stockTypeList = stockTaskNavWin.stockType.split(',');
	    			var subType = stockTypeList[2];
	    			if(subType != 2 && subType != 5){
	    				if(!material.isValid() || !amount.isValid() || !price.isValid()){
		    				Ext.example.msg('提示', '请输入货品单价.');
		    				price.focus(price, 100);
		    				return;
	    				}
	    			}
	
	    			var newRecord = null;
	    			for(var i=0, temp=material.store, sv=material.getValue(); i<temp.getCount(); i++){
	    				if(temp.getAt(i).get('id') == sv){
	    					newRecord = temp.getAt(i);
	    					break;
	    				}
	    			}
	    			
	    			var detail = secondStepPanelCenter.getStore();
	    			var has = false;
	    			for(var i=0; i < detail.getCount(); i++){
	    				if(detail.getAt(i).get('material.id') == newRecord.get('id')){
	    					detail.getAt(i).set('amount', detail.getAt(i).get('amount') + amount.getValue());
	    					detail.getAt(i).set('price', price.getValue());
	    					has = true;
	    					break;
	    				}
	    			}
	    			if(!has){
	    				detail.add(new StockDetailRecord({
	    					material : newRecord.data,
	    					id : newRecord.get('id'),
	    					'material.id' : newRecord.get('id'),
	    					'material.cateName' : newRecord.get('cateName'),
	    					'material.name' : newRecord.get('name'),
	    					amount : amount.getValue(),
	    					price : price.getValue()
	    				}));
	    			}
	    		}
	    	}, {
	    		text : '重置',
	    		hidden : true,
	    		handler : function(e){
	    			var material = Ext.getCmp('comboSelectMaterialForStockAction');
	    			var amount = Ext.getCmp('numSelectCountForStockAction');
	    			var price = Ext.getCmp('numSelectPriceForStockAction');
	    			material.setValue();
	    			amount.setValue();
	    			price.setValue();
	    		}
	    	}]
	    });
	}



	
	var secondStepPanel = Ext.getCmp('stock_secondStepPanel');
	if(!secondStepPanel){
		secondStepPanel = new Ext.Panel({
			id : 'stock_secondStepPanel',
	    	mt : '操作货单共二步, <span style="color:#000;">现为第二步:填写单据信息</font>',
	        index : 1,
	        width : '100%',
	        layout : 'border',
	        items : [secondStepPanelNorth, secondStepPanelCenter, secondStepPanelSouth]
	    });
	}
	if(!stockTaskNavWin){
		stockTaskNavWin = new Ext.Window({
			id : 'stockTaskNavWin',
			//title : '新增货单共二步, <span style="color:#000;">现为第一步:选择单据类型</font>',
			width : 900,
			height : 500,
			modal : true,
			closable : false,
			resizable : false,
		    layout : 'card',
		    activeItem : 0,
		    defaults : {
		        border:false
		    },
		    bbar: ['->', {
		    	text : '上一步',
		    	id : 'btnPreviousForStockNav',
		    	iconCls : 'btn_previous',
		    	change : -1,
		    	disabled : true,
		    	handler : function(e){
		    		stockTaskNavHandler(e);
		    	}
		    }, {
	    		text : '下一步',
	    		id : 'btnNextForStockNav',
	    		iconCls : 'btn_next',
	    		change : 1,
	    		handler : function(e){
	    			stockTaskNavHandler(e);
	    			Ext.getCmp('sam_secondStepPanelWest').setWidth(230);
	    			Ext.getCmp('stock_secondStepPanel').doLayout();
		    	}
	    	}, {
	    		text : '审核',
	    		iconCls : 'btn_refresh',
	    		id : 'btnAuditStockAction',
	    		hidden : true,
	    		handler : function(){
	    			auditStockActionHandler();
	    		}
	    	}, {
	    		text : '取消',
	    		iconCls : 'btn_cancel',
	    		handler : function(){
	    			Ext.getCmp('numSelectPriceForStockAction').setDisabled(false);
	    			Ext.getCmp('numSelectPriceForStockAction').getEl()?Ext.getCmp('numSelectPriceForStockAction').getEl().up('.x-form-item').setDisplayed(true) : ''; 
					Ext.getCmp('secondStepPanelSouth').setDisabled(false);
					var column = Ext.getCmp('secondStepPanelCenter').getColumnModel();
					column.setHidden(3, false);
					column.setHidden(4, false);
	    			stockTaskNavWin.hide();
	    		}
	    	}],
	    	keys : [{
	    		key : Ext.EventObject.ESC,
	    		scope : this,
	    		fn : function(){
	    			stockTaskNavWin.hide();
	    		}
	    	}],
	    	items: [firstStepPanel, secondStepPanel],
		    listeners : {
		    	show : function(thiz){
		    		Ext.getCmp('comboSupplierForStockActionBasic').store.load();
		    		thiz.center();
		    	},
		    	hide : function(thiz){
		    		/***** 重置操作导航 *****/
		    		// 设置默认页
		    		thiz.getLayout().setActiveItem(0);
		    		// 恢复导航按钮
		    		stockTaskNavWin.stockType = null;
		    		var btnPrevious = Ext.getCmp('btnPreviousForStockNav');
		    		var btnNext = Ext.getCmp('btnNextForStockNav');
		    		btnPrevious.setDisabled(true);
		    		btnNext.setDisabled(false);
		    		btnNext.setText('下一步');
		    		// 清空已入库单类型
//		    		var sot = Ext.query('input[name=radioStockOrderType]');
//		    		for(var i = 0; i < sot.length; i++){
//		    			sot[i].checked = false;
//		    		}
//		    		Ext.getCmp('radioStockOrderTypeGoodIn').setValue(true);
		    		// 清空单据基础信息
		    		operateStockActionBasic({
		    			otype : Ext.ux.otype['set']
		    		});
		    	}
		    }
		});
	}
}

function showStockCountMenu(){
	menuStockDetailAmount = new Ext.menu.Menu({
		id : 'menuStockDetailAmount',
		hideOnClick : false,
		items : [new Ext.Panel({
			frame : true,
			width : 150,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 30,
				items : [{
					xtype : 'numberfield',
					id : 'numStockDetailAmountSetting',
					fieldLabel : '数量',
					width : 80,
					validator : function(v){
						if(v >= 0.01 && v <= 65535){
							return true;
						}else{
							return '菜品数量在0.01 ~ 65535 之间.';
						}
					} 
				}]
			}],
			bbar : ['->', {
				text : '确定',
				id : 'btnSaveStockDetailAmount',
				iconCls : 'btn_save',
				handler : function(e){
					var amount = Ext.getCmp('numStockDetailAmountSetting');
					if(amount.isValid()){						
						secondStepPanelCenter.getSelectionModel().getSelected().set('amount', amount.getValue());
						menuStockDetailAmount.hide();
					}
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					menuStockDetailAmount.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnSaveStockDetailAmount').handler();
				}
			}]
		})],
		listeners : {
			show : function(){
				var data = Ext.ux.getSelData(secondStepPanelCenter);
				var amount = Ext.getCmp('numStockDetailAmountSetting');
				amount.setValue(data['amount']);
				amount.clearInvalid();
				amount.focus(true, 100);
			}
		}
	});
	menuStockDetailAmount.render(document.body);
}

function showUnitPriceMenu(){
	menuStockDetailPrice = new Ext.menu.Menu({
		id : 'menuStockDetailPrice',
		hideOnClick : false,
		items : [new Ext.Panel({
			frame : true,
			width : 150,
			items : [{
				xtype : 'form',
				layout : 'form',
				frame : true,
				labelWidth : 30,
				items : [{
					xtype : 'numberfield',
					id : 'numStockDetailPrice',
					fieldLabel : '金额',
					width : 80,
					validator : function(v){
						if(v >= 1 && v <= 65535){
							return true;
						}else{
							return '金额在 1 ~ 65535 之间.';
						}
					} 
				}]
			}],
			bbar : ['->', {
				text : '确定',
				id : 'btnSaveStockDetailPrice',
				iconCls : 'btn_save',
				handler : function(e){
					var price = Ext.getCmp('numStockDetailPrice');
					if(price.isValid()){						
						secondStepPanelCenter.getSelectionModel().getSelected().set('price', price.getValue());
						menuStockDetailPrice.hide();
					}
				}
			}, {
				text : '关闭',
				iconCls : 'btn_close',
				handler : function(e){
					menuStockDetailPrice.hide();
				}
			}],
			keys : [{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnSaveStockDetailPrice').handler();
				}
			}]
		})],
		listeners : {
			show : function(){
				var data = Ext.ux.getSelData(secondStepPanelCenter);
				var price = Ext.getCmp('numStockDetailPrice');
				price.setValue(data['price']);
				price.clearInvalid();
				price.focus(true, 100);
			}
		}
	});
	menuStockDetailPrice.render(document.body);
}

//---------------
var btnAddStockOrder = new Ext.ux.ImageButton({
	imgPath : '../../images/btnAddStockAction.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '新建货单',
	handler : function(btn){
		insertStockActionHandler();
		getCurrentDay();
	}
});

/*var btnGetBack = new Ext.ux.ImageButton({
	imgPath : '../../images/UserLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '返回',
	handler : function(btn){
		location.href = 'InventoryProtal.html?'+ strEncode('restaurantID=' + restaurantID, 'mi');
	}
});*/

var btnLoginOut = new Ext.ux.ImageButton({
	imgPath : '../../images/ResLogout.png',
	imgWidth : 50,
	imgHeight : 50,
	tooltip : '登出',
	handler : function(btn){
		
	}
});

Ext.onReady(function(){
	//
	getCurrentDay();
	initControl();
	
	new Ext.Panel({
		title : '库存任务管理' + '&nbsp;&nbsp;<label style="color:#800000;font-weight:bold">当前会计月份是&nbsp;<label id="sam_labCurrentMonth" style="color:green;font-weight: bold;font-size:15px"> </label>&nbsp;月</label>' ,
		renderTo : 'divStockAction',
		width : parseInt(Ext.getDom('divStockAction').parentElement.style.width.replace(/px/g,'')),
		height : parseInt(Ext.getDom('divStockAction').parentElement.style.height.replace(/px/g,'')),
		frame : true,
		layout : 'border',
		items : [stockBasicGrid],
		tbar : new Ext.Toolbar({
			height : 55,
			items : [btnAddStockOrder]
		})
	});
	showCurrentMonth();
	stockTaskNavWin.render(document.body);
	
	Ext.getCmp('comboDeptInForStockActionBasic').store.load();
	Ext.getCmp('comboDeptOutForStockActionBasic').store.load();
	
	Ext.getCmp('btnSearchForStockBasicMsg').handler();
	
	showUnitPriceMenu();
	
	//关闭再打开页面时, id重复
	if($("div[id=stockActionTotalPrice]").length > 1){
		$("div[id=stockActionTotalPrice]").eq(0).remove();
	}
	
});