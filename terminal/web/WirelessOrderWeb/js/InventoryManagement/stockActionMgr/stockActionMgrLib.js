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
		
		/***** 第二步, 选择入库单类型, 预处理 *****/
		if(act.index == 0){
			var select = null;
			var type = Ext.query('input[name=\"radioStockOrderType\"]');
			for(var i = 0; i < type.length; i++){
				if(type[i].checked){
					select = type[i].value;
					break;
				}
			}
			if(stockTaskNavWin.stockType == null || typeof stockTaskNavWin.stockType == 'undefined'){
				if(select){
					stockTaskNavWin.stockType = select;
				}else{
					Ext.example.msg('提示', '请选择一项入库单.');
					return;
				}
			}else{
				var ss = select.split(',');
				var ws = stockTaskNavWin.stockType.split(',');

				for(var i = 0; i < ss.length; i++){
					if(ss[i] != ws[i]){
//						if(stockTaskNavWin.otype == Ext.ux.otype['insert']){
							if(confirm('入库单类型已更改, 确定继续将清空原操作信息.')){
								// 更换类型后重置相关信息
								operateStockActionBasic({
									otype : Ext.ux.otype['set']
								});
								stockTaskNavWin.stockType = select;
								break;
							}else{
								for(var i = 0; i < type.length; i++){
									var temp = type[i].value.split(',');
									if(eval(temp[0] == ws[0]) && eval(temp[1] == ws[1]) && eval(temp[2] == ws[2])){
										type[i].checked = true;
									}
								}
							}
//						}
					}
				}
			}
		}
		if(index == 1){
			/***** 第二步, 根据用户选择入库单类型设置相关信息 *****/
			// stockTypeList -> [[0]出入库类型, [1]货品类型, [2]货单类型]
			var titleDom = Ext.getCmp('displayPanelForStockTitle');
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
					moneyPanel.setDisabled(true);
					priceDom.getEl().up('.x-form-item').setDisplayed(false);
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
					deptInDom.hide();
					supplierDom.show();
					deptOutDom.show();
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
					moneyPanel.setDisabled(true);
					priceDom.getEl().up('.x-form-item').setDisplayed(false);
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

				}
			}
			titleDom.body.update(diaplayTitle);
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
		
		/** 处理导航按钮触发事件, 设置导航页面或提交操作 **/
		if(index >= 0 && index <= 1 ){
			// 切换步骤
			stockTaskNavWin.getLayout().setActiveItem(index);
			var smfos = Ext.getCmp('comboSelectMaterialForStockAction');
			smfos.setValue();
			smfos.store.load({
				params : {
					cateType : stockTaskNavWin.stockType.split(',')[1]
				}
			});
			stockTaskNavWin.setTitle(stockTaskNavWin.getLayout().activeItem.mt);
		}else{
			if(index > 1){
				// 完成
				var id = Ext.getCmp('hideStockActionId');
				var deptIn = Ext.getCmp('comboDeptInForStockActionBasic');
				var supplier = Ext.getCmp('comboSupplierForStockActionBasic');
				var deptOut = Ext.getCmp('comboDeptOutForStockActionBasic');
				var oriStockId = Ext.getCmp('txtOriStockIdForStockActionBasic');
				var oriStockDate = Ext.getCmp('datetOriStockDateForStockActionBasic');
				var comment = Ext.getCmp('txtCommentForStockActionBasic');
				var actualPrice = Ext.getDom('txtActualPrice');
				var detail = '';
				if(!oriStockDate.isValid()){
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
						pin : pin,
						id : id.getValue(),
						deptIn : deptIn.getValue(),
						supplier : supplier.getValue(),
						deptOut : deptOut.getValue(),
						oriStockId : oriStockId.getValue(),
						oriStockDate : oriStockDate.getValue().getTime(),
						comment : comment.getValue(),
						type : stockType,
						cate : stockCate,
						subType : stockSubType,
						actualPrice : actualPrice.value,
						detail : detail
					},
					success : function(res, opt){
						var jr = Ext.decode(res.responseText);
						if(jr.success){
							Ext.example.msg(jr.title, jr.msg);
							stockTaskNavWin.hide();
							Ext.getCmp('comboSearchForStockType').setValue(stockType);
							Ext.getCmp('comboSearchForCateType').setValue(stockCate);
							
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
	//Ext.MessageBox.alert(actualPrice);
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
		deptIn.setValue(deptInData['id']);
		supplier.setValue(supplierData['supplierID']);
		deptOut.setValue(deptOutData['id']);
		oriStockId.setValue(data['oriStockId']);
		oriStockDate.setValue(data['oriStockDateFormat']);
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
		if(data['statusValue'] == 2){
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
			'dataSource' : 'checkStockTake',
			pin : pin
		},
		success : function(res, opt){
			var jr = Ext.decode(res.responseText);
			if(jr.success){
				stockTaskNavWin.otype = Ext.ux.otype['insert'];
				stockTaskNavWin.center();
				stockTaskNavWin.show();
				stockTaskNavWin.setTitle(stockTaskNavWin.getLayout().activeItem.mt);
				Ext.getCmp('secondStepPanelWest').setDisabled(false);
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
 * 修改出入库单信息
 */
function updateStockActionHandler(){
	var data = Ext.ux.getSelData(stockBasicGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录后再操作.');
		return;
	}
	stockTaskNavWin.center();
	stockTaskNavWin.show();
	
	if(data['statusValue'] == 1){
		stockTaskNavWin.otype = Ext.ux.otype['update'];
		stockTaskNavWin.setTitle('修改库存单信息');
		operateStockActionBasic({
			otype : Ext.ux.otype['set'],
			data : data
		});
		Ext.getCmp('btnPreviousForStockNav').setDisabled(true);
		if(data['subTypeValue'] == 1 || data['subTypeValue'] == 4){
			Ext.getDom('txtActualPrice').disabled = false;
		}
		
		if(data['subTypeValue'] == 9){
			Ext.getCmp('btnNextForStockNav').setDisabled(true);
			Ext.getCmp('secondStepPanelWest').setDisabled(true);
			Ext.getCmp('btnAuditStockAction').hide();
		}else{
			Ext.getCmp('secondStepPanelWest').setDisabled(false);
			Ext.getCmp('btnAuditStockAction').show();
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
		Ext.getCmp('secondStepPanelWest').setDisabled(true);
		Ext.getDom('txtActualPrice').disabled = true;
		Ext.getCmp('btnAuditStockAction').hide();
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
						pin : pin,
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
						pin : pin,
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
	var data = Ext.ux.getSelData(secondStepPanelCenter);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录后再操作.');
	}
	
	for(var i = 0; i < secondStepPanelCenter.getStore().getCount(); i++){
		var temp = secondStepPanelCenter.getStore().getAt(i);
		if(temp.get('material.id') == data['material.id']){
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
}