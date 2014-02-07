/**
 * 添加菜品, 绑定数据
 */
function bindGridData(_c){
	var grid = _c.grid, record = _c.record;
	var isAlreadyOrderd = true;
	var sindex = 0;
	for ( var i = 0; i < grid.order.orderFoods.length; i++) {
		var temp = grid.order.orderFoods[i];
		if (temp.id == record.data.id) {
			if(temp.tasteGroup.normalTasteContent.length == 0){
				temp.count += (typeof _c.count == 'number' ? _c.count : 1);
				isAlreadyOrderd = false;
				sindex = i;
				break;
			}
		}
	}
	if(isAlreadyOrderd){
		grid.order.orderFoods.push({
			id : record.data.id,
			alias : record.data.alias,
			name : record.data.name,
			unitPrice : record.data.unitPrice,
			acturalPrice : record.data.unitPrice,
			kitchen : record.data['kitchen'],
			status : record.data['status'],
			count : typeof _c.count == 'number' ? _c.count : 1,
			orderDateFormat : new Date().format('Y-m-d H:i:s'),
			waiter : Ext.getDom('optName').innerHTML,
			dataType : typeof _c.dataType == 'number' ? _c.dataType : 2,
			temporary : false,
			hangup : false,
			tasteGroup : {
				groupId : 0,
				tastePref : '无口味',
				normalTaste : null,
				normalTasteContent : [],
				tempTaste : null
			}
		});
		sindex = grid.getStore().getCount();
	}
	grid.getStore().loadData({root:grid.order.orderFoods});
	if(typeof _c.callBack != 'undefined'){
		_c.callBack(grid, _c);
	}
	grid.getSelectionModel().selectRow(sindex);
};
/**
 * 操作菜品数量
 * @param _c
 */
function foodAmountOperateHandler(_c){
	if(typeof _c.otype != 'number' || typeof _c.count != 'number'){
		return;
	}
	var data = Ext.ux.getSelData(_c.grid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录后再操作!');
		return;
	}
	var sindex = null, newCount = 0;
	for(var i = 0; i < _c.grid.order.orderFoods.length; i++){
		var temp = _c.grid.order.orderFoods[i];
		if(data.id == temp.id){
			if(compareTasteGroup(data.tasteGroup, temp.tasteGroup)){
				sindex = i;
				newCount = eval(temp.count + _c.count);
				if(newCount == 0){
					// 如果操作后的菜品数量少于1的时候, 确定是否删除菜品
					foodAmountDeleteHandler();
				}else{
					if(_c.otype == 0){
						temp.count = newCount;
					}else{
						temp.count = _c.count;
					}
					_c.grid.getStore().loadData({root:_c.grid.order.orderFoods});
					_c.grid.getSelectionModel().selectRow(sindex);
				}
				break;
			}
		}
	}
}
/**
 * 删除
 */
function foodAmountDeleteHandler(){
	var data = Ext.ux.getSelData(orderedGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录后再操作!');
		return;
	}
	Ext.Msg.show({
		title : '重要',
		msg : '是否删除该菜品?',
		icon: Ext.Msg.QUESTION,
		buttons : Ext.Msg.YESNO,
		fn : function(btn){
			if(btn == 'yes'){
				var temp;
				for(var i = 0; i < orderedGrid.order.orderFoods.length; i++){
					temp = orderedGrid.order.orderFoods[i];
					if(data.id == temp.id){
						if(compareTasteGroup(data.tasteGroup, temp.tasteGroup)){
							orderedGrid.order.orderFoods.splice(i, 1);
							orderedGrid.getStore().loadData({root:orderedGrid.order.orderFoods});
							break;
						}
					}
				}
				temp = null;
			}
		}
	});
}
/**
 * 设置数量
 */
function foodAmountSetHandler(_c){
	var data = Ext.ux.getSelData(orderedGrid);
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条记录后再操作!');
		return;
	}
	if(!menuOperateFoodAmount){
		menuOperateFoodAmount = new Ext.menu.Menu({
			id : 'menuOperateFoodAmount',
			hideOnClick : false,
			items : [new Ext.menu.Adapter(new Ext.Panel({
				frame : true,
				width : 150,
				items : [{
					xtype : 'form',
					layout : 'form',
					frame : true,
					labelWidth : 30,
					items : [{
						xtype : 'numberfield',
						id : 'numOperateFoodAmount',
						fieldLabel : '数量',
						width : 80,
						validator : function(v){
							if(v >= 1 && v <= 255){
								return true;
							}else{
								return '菜品数量在 1 ~ 255 之间.';
							}
						} 
					}]
				}],
				bbar : ['->', {
					text : '确定',
					id : 'btnSaveOperateFoodAmount',
					iconCls : 'btn_save',
					handler : function(e){
						var count = Ext.getCmp('numOperateFoodAmount');
						if(!count.isValid()){
							return;
						}
						foodAmountOperateHandler({
							otype : 1,
							count : count.getValue(),
							grid : orderedGrid
						});
						Ext.getCmp('btnCloseOperationFoodCount').handler();
					}
				}, {
					text : '关闭',
					id : 'btnCloseOperationFoodCount',
					iconCls : 'btn_close',
					handler : function(e){
						Ext.menu.MenuMgr.get('menuOperateFoodAmount').hide();
					}
				}]
			}), {hideOnClick : false})],
			keys : [{
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnSaveOperateFoodAmount').handler();
				}
			}, {
				key : Ext.EventObject.ENTER,
				scope : this,
				fn : function(){
					Ext.getCmp('btnCloseOperationFoodCount').handler();
				}
			}]
		});
		menuOperateFoodAmount.render(document.body);
	}
	var count = Ext.getCmp('numOperateFoodAmount');
	count.setValue(data['count']);
	count.clearInvalid();
	count.focus.defer(100, count);
	menuOperateFoodAmount.showAt([_c.x, _c.y]);
}
/**
 * 提交修改
 */
function submitOrderHandler(_c){
	var orderFoods = orderedGrid.order.orderFoods;
	if(orderFoods.length > 0){
		var foodPara = '';
		for ( var i = 0; i < orderFoods.length; i++) {
			foodPara += ( i > 0 ? '<<sh>>' : '');
			if (orderFoods[i].isTemporary) {
				// 临时菜
				var foodname = orderFoods[i].name;
				foodname = foodname.indexOf('<') > 0 ? foodname.substring(0,foodname.indexOf('<')) : foodname;
				foodPara = foodPara 
						+ '[' 
						+ 'true' + '<<sb>>'// 是否临时菜(true)
						+ orderFoods[i].id + '<<sb>>' // 临时菜1编号
						+ foodname + '<<sb>>' // 临时菜1名称
						+ orderFoods[i].count + '<<sb>>' // 临时菜1数量
						+ orderFoods[i].unitPrice + '<<sb>>' // 临时菜1单价(原料單價)
						+ '<<sb>>' // 菜品状态,暂时没用
						+ 3 + '<<sb>>' // 菜品操作状态 1:已点菜 2:新点菜 3:反结账
						+ orderFoods[i].kitchen.id	// 临时菜出单厨房
						+ ']';
			}else{
				// 普通菜
				var normalTaste = '', tmpTaste = '' , tasteGroup = orderFoods[i].tasteGroup;
				for(var j = 0; j < tasteGroup.normalTasteContent.length; j++){
					var t = tasteGroup.normalTasteContent[j];
					normalTaste += ((j > 0 ? '<<stnt>>' : '') + (t.id + '<<stb>>' + t.cateValue + '<<stb>>' + t.cateStatusValue));
				}
				if(tasteGroup.tmpTaste != null && typeof tasteGroup.tmpTaste != 'undefined'){
					if(eval(tasteGroup.tmpTaste.id >= 0))
						tmpTaste = tasteGroup.tmpTaste.price + '<<sttt>>' + tasteGroup.tmpTaste.name  + '<<sttt>>' + tasteGroup.tmpTaste.id+ '<<sttt>>' + tasteGroup.tmpTaste.alias; 				
				}
				foodPara = foodPara 
						+ '['
						+ 'false' + '<<sb>>' // 是否临时菜(false)
						+ orderFoods[i].id + '<<sb>>' // 菜品1编号
						+ orderFoods[i].count + '<<sb>>' // 菜品1数量
						+ (normalTaste + ' <<st>> ' + tmpTaste) + '<<sb>>'
						+ orderFoods[i].kitchen.id + '<<sb>>'// 厨房1编号
						+ '1' + '<<sb>>' // 菜品1折扣
						+ 3  // 菜品操作状态 1:已点菜 2:新点菜 3:反结账
						+ ']';
			}
		}
		foodPara = '{'+ foodPara + '}';
		
		var payMannerOut = null;
		var payManner = document.getElementsByName('radioPayType');
		for(var i = 0; i < payManner.length; i++){
			if(payManner[i].checked == true ){
				payMannerOut = payManner[i].value;
				break;
			}
		}
		
		var serviceRate = Ext.getCmp('serviceRate');
		var commentOut = billGenModForm.findById("remark").getValue();
		var discountID = Ext.getCmp('comboDiscount');
		var erasePrice = Ext.getCmp('numErasePrice');
		
		if(!serviceRate.isValid()){
			Ext.example.msg('提示', '服务费率为1-100的正整数,请重新输入.');
			return;
		}
		if(typeof sysSetting.setting != 'undefined' && erasePrice.getValue() > sysSetting.setting.eraseQuota){
			Ext.example.msg('提示', '抹数金额不能大于系统设置,请重新输入.');
			return;
		}
		
		orderedGrid.buttons[0].setDisabled(true);
		orderedGrid.buttons[1].setDisabled(true);
		Ext.Ajax.request({
			url : "../../UpdateOrder2.do",
			params : {
				isCookie : true,
				"orderID" : orderedGrid.order["id"],
				'tableAlias' : orderedGrid.order.table.alias,
				"category" : orderedGrid.order["categoryValue"],
				"customNum" : orderedGrid.order['customNum'],
				"payType" : orderedGrid.order['settleTypeValue'],
				'discountID' : discountID.getValue(),
				"payManner" : payMannerOut,
				"serviceRate" : serviceRate.getValue(),
				"memberID" : orderedGrid.order['memberID'],
				"comment" : commentOut,
				"foods" : foodPara,
				'erasePrice' : erasePrice.getValue()
			},
			success : function(response, options) {
				var resultJSON = Ext.util.JSON.decode(response.responseText);
				if (resultJSON.success == true) {
					Ext.MessageBox.show({
						msg : resultJSON.data + "，是否打印账单？",
						width : 300,
						buttons : Ext.MessageBox.YESNO,
						fn : function(btn) {
							if (btn == "yes") {
								var tempMask = new Ext.LoadMask(document.body, {
									msg : '正在打印请稍候.......',
									remove : true
								});
								tempMask.show();
								Ext.Ajax.request({
									url : "../../PrintOrder.do",
									params : {
										
										"orderID" : Request["orderID"],
										'printType' : 3
									},
									success : function(response, options) {
										tempMask.hide();
										var jr = Ext.util.JSON.decode(response.responseText);
										Ext.MessageBox.show({
											msg : jr.msg,
											width : 300,
											buttons : Ext.MessageBox.OK,
											fn : function() {
												location.href = "Bills.html";
											}
										});
									},
									failure : function(response, options) {
										tempMask.hide();
										Ext.ux.showMsg(Ext.decode(response.responseText));
									}
								});
							} else {
								location.href = "Bills.html";
							}
						}
					});
				} else {
					orderedGrid.buttons[0].setDisabled(false);
					orderedGrid.buttons[1].setDisabled(false);
					Ext.MessageBox.show({
						msg : resultJSON.data,
						width : 300,
						buttons : Ext.MessageBox.OK
					});
				}
			},
			failure : function(response, options) {
				orderedGrid.buttons[0].setDisabled(false);
				orderedGrid.buttons[1].setDisabled(false);
				Ext.MessageBox.show({
					msg : "Unknow page error",
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		});
	}
}
