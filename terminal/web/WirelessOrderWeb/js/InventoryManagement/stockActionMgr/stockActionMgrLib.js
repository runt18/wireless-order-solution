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
						if(confirm('入库单类型已更改, 确定继续将清空原操作信息.')){
							stockTaskNavWin.stockType = select;
							// 更换类型后重置相关信息
							operateStockActionBasic({
				    			otype : Ext.ux.otype['set']
				    		});
							break;
						}else{
							for(var i = 0; i < type.length; i++){
								var temp = type[i].value.split(',');
								if(eval(temp[0] == ws[0]) && eval(temp[1] == ws[1]) && eval(temp[2] == ws[2])){
									 type[i].checked = true;
								}
							}
						}
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
			var stockTypeList = stockTaskNavWin.stockType.split(',');
			var stockType = stockTypeList[0], stockCate = stockTypeList[1], stockSubType = stockTypeList[2];
			var diaplayTitle = '';
			
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
		if(index <= 0){
			btnPrevious.setDisabled(true);
		}else{
			btnPrevious.setDisabled(false);
		}
		
		// 处理导航按钮触发事件, 设置导航页面或提交操作
		if(index >= 0 && index <= 1 ){
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
				var data = operateStockActionBasic({
	    			otype : Ext.ux.otype['get']
	    		}).data;
				
				alert(data.detail.getCount())
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
	
	var deptIn = Ext.getCmp('comboDeptInForStockActionBasic');
	var supplier = Ext.getCmp('comboSupplierForStockActionBasic');
	var deptOut = Ext.getCmp('comboDeptOutForStockActionBasic');
	var oriStockId = Ext.getCmp('txtOriStockIdForStockActionBasic');
	var oriStockDate = Ext.getCmp('datetOriStockDateForStockActionBasic');
	var comment = Ext.getCmp('txtCommentForStockActionBasic');
	var spproverName = Ext.getCmp('txtSpproverNameForStockActionBasic');
	var spproverDate = Ext.getCmp('dateSpproverDateForStockActionBasic');
	var operatorName = Ext.getCmp('txtOperatorNameForStockActionBasic');
	var operatorDate = Ext.getCmp('dateOperatorDateForStockActionBasic');
	
//		'id', 'restaurantId', 'oriStockId', 'oriStockDateFormat',  'cateTypeText', 'cateTypeValue',
//		'deptIn', 'deptOut', 'supplier', 'approverName', 'approverDateFormat', 'operatorName', 'amount', 'price', 'totalAmount', 
//		'totalPrice', 'typeValue', 'typeText', 'statusValue', 'statusText', 'comment', 'subTypeValue', 'subTypeText'
	if(c.otype == Ext.ux.otype['set']){
		var data = c.data == null || typeof c.data == 'undefined' ? {} : c.data;
		var deptInData = data.deptIn == null || typeof data.deptIn == 'undefined' ? {} : data.deptIn;
		var supplierData = data.supplier == null || typeof data.supplier == 'undefined' ? {} : data.supplier;
		var deptOutData = data.deptOut == null || typeof data.deptOut == 'undefined' ? {} : data.deptOut;
		deptIn.setValue(deptInData['id']);
		supplier.setValue(supplierData['id']);
		deptOut.setValue(deptOutData['id']);
		oriStockId.setValue(data['oriStockId']);
		oriStockDate.setValue(data['oriStockDate']);
		comment.setValue(data['comment']);
		spproverName.setValue(data['spproverName']);
		spproverDate.setValue(data['spproverDate']);
		operatorName.setValue(data['operatorName']);
		operatorDate.setValue(data['operatorDate']);
		
		if(typeof data.detail != 'undefined' && data.detail.length > 0){
			
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
		deptOut.clearInvalid();
		amount.clearInvalid();
		price.clearInvalid();
	}else if(c.otype == Ext.ux.otype['get']){
		c.data = {
			deptIn : {
				id : deptIn.getValue(),
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