/**
 * 刷新显示样式, 区别已点菜和新点菜
 * @param _c
 */
function orderGroupDisplayRefresh(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	var grid = null, gs = null;
	if(typeof _c.control == 'string'){
		grid = Ext.getCmp(_c.control);
	}else if(typeof _c.control == 'object'){
		grid = _c.control;
	}else{
		return null;
	}
	if(grid != null){
		gs = grid.getStore();
	}
	if(gs != null){
		
		for(var i = 0; i < gs.getCount(); i++){
			if(gs.getAt(i).data.dataType == 2 || isRepaid){
				frontNewOrderFood.push(gs.getAt(i).data);
			}else{
				
				grid.getView().getRow(i).style.backgroundColor = '#FFFF93';
			}
		}
	}
}

/**
 * 单张账单口味操作
 */
function orderSingleTasteOperationHandler(_c){
	
	var or = Ext.ux.getSelData(_c.grid);
	//新选中的口味
	var htgs = haveTasteGrid.getStore();
	var tasteGroup = {
		tastePref : '无口味',
		normalTasteContent:[],
		normalTaste:{
			name : ''
		}
	};			
	// 修改原数据
	for(var i = 0; i < _c.grid.order.orderFoods.length; i++){
		if(_c.grid.order.orderFoods[i].id == or['id']){
			if(compareTasteGroup(_c.grid.order.orderFoods[i].tasteGroup, or['tasteGroup'])){
				//生成新口味 
				for(var w = 0; w < htgs.data.length; w++){
					var v = htgs.data.get(w).data;
					tasteGroup.normalTaste.name += ((w > 0 || tasteGroup.normalTaste.name ? ';' : '') + v.taste.name);
					tasteGroup.normalTasteContent.push(v.taste);
				}				
				
				tasteGroup.tastePref = tasteGroup.normalTaste.name.length > 0 ? tasteGroup.normalTaste.name : '无口味';
				_c.grid.order.orderFoods[i].tasteGroup = tasteGroup;
				break;
			}
		}
	}
	// 合并菜品重复数据
	var tempData = {root:[]};
	for(var i = 0; i < _c.grid.order.orderFoods.length; i++){
		
		var cs = true;
		for(var j = 0; j < tempData.root.length; j++){
			if(compareDataType(tempData.root[j], _c.grid.order.orderFoods[i])){
				if(compareTasteGroup(tempData.root[j].tasteGroup,  _c.grid.order.orderFoods[i].tasteGroup)){
					if(tempData.root[j].id == _c.grid.order.orderFoods[i].id){
						cs = false;
						tempData.root[j].count += _c.grid.order.orderFoods[i].count;
					}

				}
			}
		}
		if(cs){
			tempData.root.push(_c.grid.order.orderFoods[i]);
		}
	}
	//
	choosenTasteWin.hide();
	//
	_c.grid.order.orderFoods = tempData.root;
	_c.grid.getStore().loadData({root:_c.grid.order.orderFoods});
	//
	orderGroupDisplayRefresh({
		control : _c.grid
	});
};

/**
 * 账单组口味操作
 */
orderGroupTasteOperationHandler = function(_c){
	var active = orderGroupGridTabPanel.getActiveTab();
	if(theGroupIsSingleOrGroup() == 1){
		orderGroupGridTabPanel.items.each(function(itemTab){
			_c.grid = itemTab;
			orderSingleTasteOperationHandler(_c);
		});
	}else{
		_c.grid = active;
		orderSingleTasteOperationHandler(_c);
	}
};

/* ================================================== */
/**
 * 单张账单删除菜品
 */
function orderSingleDeleteFoodOperationHandler(_c){
	var data = Ext.ux.getSelData(_c.grid);
	Ext.MessageBox.show({
		title : '重要',
		msg : '是否删除该菜品?',
		icon: Ext.MessageBox.QUESTION,
		buttons : Ext.MessageBox.YESNO,
		fn : function(btn){
			if(btn == 'yes'){
				for(var i = 0; i < _c.grid.order.orderFoods.length; i++){
					var temp =  _c.grid.order.orderFoods[i];
					//compareDataType对反结账无效
					if(!isRepaid?compareDataType(temp, data):true){
						if(compareTasteGroup(data.tasteGroup, temp.tasteGroup)){
							if(data.id == temp.id){
								if(typeof _c.count == 'number'){
									temp.count += _c.count;								
									if(temp.count <= 0){
										_c.grid.order.orderFoods.splice(i, 1);
									}
								}else{
									_c.grid.order.orderFoods.splice(i, 1);
								}
								break;
							
							}

						}
					}
				}						
				_c.grid.getStore().loadData({root:_c.grid.order.orderFoods});
				// 
				orderGroupDisplayRefresh({
					control : _c.grid
				});
			}
		}
	});
};

/**
 * 账单组删除菜品
 */
function orderOrderDeleteFoodOperationHandler(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	var active = orderGroupGridTabPanel.getActiveTab();
	var data = Ext.ux.getSelData(active);
	if(theGroupIsSingleOrGroup() == 1){
		Ext.MessageBox.show({
			title : '重要',
			msg : '您确定要删除此菜品?<br/>当前是全组操作状态, 将会同时删除组中其他账单已有的菜品信息.',
			icon: Ext.MessageBox.QUESTION,
			buttons : Ext.MessageBox.YESNO,
			fn : function(btn){
				if(btn == 'yes'){
					orderGroupGridTabPanel.items.each(function(itemTab){
						for(var i = 0; i < itemTab.order.orderFoods.length; i++){
							var temp =  itemTab.order.orderFoods[i];
							if(compareDataType(temp, data) == true){
								if(compareTasteGroup(data.tasteGroup, temp.tasteGroup)){
//									if(data.id == temp.id){
										var tempCount = typeof _c.count == 'number' ? _c.count : temp.count * -1;
										var newCount = eval(temp.count + tempCount);
										if(newCount == 0){
											itemTab.order.orderFoods.splice(i, 1);
											Ext.example.msg('提示', String.format('账单: {0}, 已删除菜品: {1}, 数量: {2}', itemTab.order.id, temp.foodName, tempCount), 2);
										}else{
											temp.count = newCount;
										}
										break;
//									}

								}
							}
						}						
						itemTab.getStore().loadData({root:itemTab.order.orderFoods});
						// 
						orderGroupDisplayRefresh({
							control : itemTab
						});
					});
				}
			}
		});
	}else{
		_c.grid = active;
		orderSingleDeleteFoodOperationHandler(_c);
	}
};

/* ================================================== */
/**
 * 菜品数量添加
 */
function orderSingleFoodCountOperationHandler(_c){
	if(typeof _c.otype != 'number' || typeof _c.count != 'number'){
		return;
	}
	var data = Ext.ux.getSelData(_c.grid);
	if(data.dataType == 2 || isRepaid) {
		var sindex = null, newCount = 0;
		for(var i = 0; i < _c.grid.order.orderFoods.length; i++){	
			var temp = _c.grid.order.orderFoods[i];
			if(data.id == temp.id && (temp.dataType == 2 || isRepaid)){
				
				if(isRepaid ? (data.id == temp.id) : compareDataType(temp, data)){	
					
					if(compareTasteGroup(data.tasteGroup, temp.tasteGroup)){
						sindex = i;
						newCount = eval(temp.count + _c.count);
						if(newCount == 0){
							// 如果操作后的菜品数量少于1的时候, 确定是否删除菜品
							orderSingleDeleteFoodOperationHandler(_c);
						}else{
							if(_c.otype == 0){
								temp.count = newCount;
							}else{
								temp.count = _c.count;
							}
							_c.grid.getStore().loadData({root:_c.grid.order.orderFoods});
							orderGroupDisplayRefresh({
								control : _c.grid
							});
							_c.grid.getSelectionModel().selectRow(sindex);
						}
						break;
					}
				}
			}
		}
	}
};

/**
 * 判断菜品数量操作之后是否为空
 * @param _c
 * @returns {Boolean}
 */
function theFoodIsEmpty(_c){
	var grid = _c.grid;
	var data = Ext.ux.getSelData(grid);
	var has = false;
	for(var i = 0; i < grid.order.orderFoods.length; i++){	
		var temp = grid.order.orderFoods[i];
		if(data.foodID == temp.foodID && temp.dataType == 2){
			if(compareDataType(temp, data)){							
				if(compareTasteGroup(data.tasteGroup, temp.tasteGroup)){
					var newCount = eval(temp.count + _c.count);
					if(newCount <= 0){
						has = true;
					}
					break;
				}
			}
		}
	}
	if(typeof _c.callBack == 'function'){
		_c.callBack(has);
	}
	return has;
}

/**
 * 账单组菜品数量添加
 */
function orderGroupFoodCountOperationHandler(_c){
	if(typeof _c.otype != 'number' && typeof _c.count != 'number'){
		return;
	}
	if(theGroupIsSingleOrGroup() == 1){
		var active = orderGroupGridTabPanel.getActiveTab();
		var record = active.getSelectionModel().getSelections()[0];
		var data = record.data;
		theFoodIsEmpty({
			grid : active,
			count : _c.count,
			callBack : function(isEmpty){
				var ofn = function(){
					orderGroupGridTabPanel.items.each(function(itemTab){
						var hasStatus = false;
						for(var i = 0; i < itemTab.order.orderFoods.length; i++){	
							var temp = itemTab.order.orderFoods[i];
							if(data.foodID == temp.foodID && temp.dataType == 2){
								if(compareDataType(temp, data)){							
									if(compareTasteGroup(data.tasteGroup, temp.tasteGroup)){
										hasStatus = true;
										if(_c.otype == 0){
											newCount = eval(temp.count + _c.count);
											if(newCount == 0){
												itemTab.order.orderFoods.splice(i, 1);
												Ext.example.msg('提示', String.format('账单: {0}, 已删除菜品: {1}, 数量: {2}', itemTab.order.id, temp.foodName, _c.count), 2);
											}else{
												temp.count = newCount;
											}
										}else if(_c.otype == 1){
											temp.count = _c.count;
										}
										itemTab.getStore().loadData({root:itemTab.order.orderFoods});
										orderGroupDisplayRefresh({
											control : itemTab
										});
										itemTab.getSelectionModel().selectRow(i);
										break;
									}
								}
							}
						}
						// 是否已存在, 不存在且数量是添加状态, 则新增
						if(_c.count >= 1 && !hasStatus){
							bindGridData({
								grid : itemTab,
								record : record,
								count : _c.otype == 1 ? _c.count : null,
								callBack : function(e, c){
									orderGroupDisplayRefresh({
										control : e
									});
								}
							});
						}
					});
				};
				if(isEmpty){
					Ext.MessageBox.show({
						title : '重要',
						msg : '您确定要删除此菜品?',
						icon: Ext.MessageBox.QUESTION,
						buttons : Ext.MessageBox.YESNO,
						fn : function(btn) {
							if(btn == 'yes'){
//								return true;
								ofn();
							}
						}
					});
				}else{
					ofn();
				}
			}
		});
	}else{
		_c.grid = orderGroupGridTabPanel.getActiveTab();
		orderSingleFoodCountOperationHandler(_c);
	}
};

/**
 * 单张账单添加菜品
 */
function addOrderSingleFoodHandler(_c){
	var r = _c.grid.getStore().getAt(_c.rowIndex);
	if(!r){
		Ext.example.msg('提示', '该编号菜品信息不在当前展示列表, 请重新输入.');
	}else if(Ext.ux.cfs.isStop(r.get('status'))){
		Ext.example.msg('提示', '该菜品已停售, 请重新选择.');
	}else{
		r.isGift = false;
		bindGridData({
			grid : orderSingleGridPanel,
			record : r,
			count : _c.count,
			callBack : function(e, c){
				orderGroupDisplayRefresh({
					control : e
				});
				if(typeof _c.callback == 'function'){
					_c.callback(r);
				}
			}
		});
	}
};

/**
 * 账单组添加菜品
 */
function addOrderGroupFoodHandler(_c){
	var record = _c.grid.getStore().getAt(_c.rowIndex);
	if(record.get('stop') == true){
		Ext.example.msg('提示', '操作失败, 该菜品已停售, 请重新选择.');
		return;
	}
	if(theGroupIsSingleOrGroup() == 1){
		// 全组同时添加菜品
		orderGroupGridTabPanel.items.each(function(itemTab){
			bindGridData({
				grid : itemTab,
				record : record,
				callBack : function(e, c){
					orderGroupDisplayRefresh({
						control : e
					});
				}
			});
		});
	}else{
		// 组中任意一张台添加菜品
		bindGridData({
			grid : orderGroupGridTabPanel.getActiveTab(),
			record : record,
			callBack : function(e, c){
				orderGroupDisplayRefresh({
					control : e
				});
			}
		});
	}
};

/* ================================================== */
/**
 * 
 */
function checkSselectedData(){
	var data = false;
	if (isGroup) {
		data = Ext.ux.getSelData(orderGroupGridTabPanel.getActiveTab());
	}else{
		data = Ext.ux.getSelData(orderSingleGridPanel);
	}
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return false;
	}else if(data.dataType == 1){
		Ext.example.msg('提示', '操作失败, 已点菜不允许修改.');
		return false;
	}else{
		return true;
	}
}


/**
 * 口味操作入口
 */
function orderTasteOperationHandler(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	if (isGroup) {
		orderGroupTasteOperationHandler(_c);
	}else{
		_c.grid = orderSingleGridPanel;
		orderSingleTasteOperationHandler(_c);
	}
};
/**
 * 删除菜品操作入口
 */
function orderDeleteFoodOperationHandler(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	var data = false;
	if (isGroup) {
		data = Ext.ux.getSelData(orderGroupGridTabPanel.getActiveTab());
	}else{
		_c.grid = orderSingleGridPanel;
		data = Ext.ux.getSelData(orderSingleGridPanel);
	}
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
	}else{
		if(data.dataType == 1){
			// 已点菜退菜
			initPasswordWin();
			var winValidPassword = Ext.getCmp('winValidPassword');
			winValidPassword.show();
		}else{
			// 新点菜删除
			if (isGroup) {
				orderOrderDeleteFoodOperationHandler(_c);
			}else{
				orderSingleDeleteFoodOperationHandler(_c);
			}
		}
	}
};

function orderFood_formatFoodName(record, iname, name, type){
	var img = '';
	var status = record.get('status');
	if(Ext.ux.cfs.isSpecial(status))
		img += '&nbsp;<img src="../../images/icon_tip_te.png"></img>';
	if(Ext.ux.cfs.isRecommend(status)) 
		img += '&nbsp;<img src="../../images/icon_tip_jian.png"></img>';
	if(Ext.ux.cfs.isStop(status))
		img += '&nbsp;<img src="../../images/icon_tip_ting.png"></img>';
	if(type == 0){
		if(Ext.ux.cfs.isGift(status) && Ext.ux.staffGift)
			img += '&nbsp;<img src="../../images/forFree.png"></img>';	
	}else if(type == 1){
		if(Ext.ux.cfs.isGift(status) && record.get('isGift'))
			img += '&nbsp;<img src="../../images/forFree.png"></img>';	
	}

	if(Ext.ux.cfs.isCurrPrice(status))
		img += '&nbsp;<img src="../../images/currPrice.png"></img>';
	if(Ext.ux.cfs.isCombo(status))
		img += '&nbsp;<img src="../../images/combination.png"></img>';
	if(Ext.ux.cfs.isHot(status))
		img += '&nbsp;<img src="../../images/hot.png"></img>';
	if(Ext.ux.cfs.isWeigh(status))
		img += '&nbsp;<img src="../../images/weight.png"></img>';
	if(Ext.ux.cfs.isCommission(status))
		img += '&nbsp;<img src="../../images/commission.png"></img>';
	if (record.get('temporary') || record.get('isTemporary'))
		img += '&nbsp;<img src="../../images/tempDish.png"></img>';
	
	record.set(iname, record.get(name) + img);	
	record.commit();
}

function orderGiftFoodOperationHandler(c){
	var record = orderSingleGridPanel.getSelectionModel().getSelected();
	reloadData = c.id;
	for(var i = 0; i < orderSingleGridPanel.order.orderFoods.length; i++){	
		var temp = orderSingleGridPanel.order.orderFoods[i];
		if(record.get('id') == temp.id){
			if(c.checked == true){
//				
				temp.isGift = true;
				record.set('isGift', true);
				orderFood_formatFoodName(record, 'displayFoodName', 'name', 1);
				$('#'+c.id).attr('checked', 'checked');
				
			}else{
				temp.isGift = false;
				record.set('isGift', false);
				orderFood_formatFoodName(record, 'displayFoodName', 'name', 1);
			}	
			break;
		}

	}
	reloadData = null;
	
}
/**
 * 修改菜品数量入口
 */
function orderFoodCountOperationHandler(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	if (isGroup) {
		orderGroupFoodCountOperationHandler(_c);
	}else{
		_c.grid = orderSingleGridPanel;
		orderSingleFoodCountOperationHandler(_c);			
	}
};

/**
 * 添加菜品入口
 */
function addOrderFoodHandler(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	if (isGroup) {
		addOrderGroupFoodHandler(_c);
	}else{
		addOrderSingleFoodHandler(_c);
	}
};
/**
 * 修改菜品数量操作引导
 */
function orderFoodCountRendererHandler(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	var mmenu = Ext.menu.MenuMgr.get('menuOperationFoodCount');
	if(mmenu){
		mmenu.showAt([_c.x, _c.y]);
	}
};

/**
 * 设置菜品状态
 */
function refreshOrderFoodDataType(arr){
	for(var i = 0; i < arr.length; i++){
		arr[i].dataType = 1;
	}
};

/**
 * 口味操作引导
 */
function orderTasteRendererHandler(){
	var data = false;
	if(isGroup){
		data = Ext.ux.getSelData(orderGroupGridTabPanel.getActiveTab());
	}else{
		data = Ext.ux.getSelData(orderSingleGridPanel);
	}
	if(!data){
		Ext.example.msg('提示', '操作失败, 请选中一条数据再进行操作.');
		return;
	}
	if(data.dataType == 1){
		Ext.example.msg('提示', '操作失败, 已点菜不允许修改口味.');
	}else if(data.isTemporary){
		Ext.example.msg('提示', '操作失败, 临时菜不允许修改口味.');
	}else if(data.dataType == 2){
		choosenTasteWin.show();
	}
};

/**
 * 账单操作
 */
function orderOrderGridPanelRenderer(value, cellmeta, record, rowIndex, columnIndex, store){
	return ''
	   + '<a href="javascript:orderTasteRendererHandler()"><img src="../../images/icon_tb_taste.png" border="0" title="选择口味"/></a>'
	   + '&nbsp;&nbsp;&nbsp;'
	   + '<a href="javascript:orderDeleteFoodOperationHandler()"><img src="../../images/btnCancel.png" border="0" title="删除菜品"/></a>'
	   + '';
};
/**
 * 口味设置
 */
function orderOrderGridPanelTasteRenderer(v, cm, r, ri, ci, store){
	if(r.get('dataType') == 2){
		return '<a href="javascript:orderTasteRendererHandler()"><img src="../../images/icon_tb_taste.png" border="0" title="选择口味"/></a>'
			+ '&nbsp;&nbsp;&nbsp;'
			+ v;		
	}else{
		return v;
	}
};

/**
 * 菜品数量增加或删除操作入口渲染
 */
function foodCountAddOrDeleteRenderer(value, cellmeta, record, rowIndex, columnIndex, store){

	if(record.get('dataType') == 2 || isRepaid){
		return ''
			+ Ext.ux.txtFormat.gridDou(value)
			+ '<a href="javascript:orderFoodCountOperationHandler({otype:0,count:1});"><img src="../../images/btnAdd.gif" border="0" title="菜品数量+1"></a>&nbsp;'
			+ '<a href="javascript:orderFoodCountOperationHandler({otype:0,count:-1});"><img src="../../images/btnDelete.png" border="0" title="菜品数量-1"></a>&nbsp;'
			+ '<a onClick="orderFoodCountRendererHandler({x:event.clientX,y:event.clientY})"><img src="../../images/icon_tb_setting.png" border="0" title="菜品数量设置"></a>&nbsp;'
			+ '<a href="javascript:orderDeleteFoodOperationHandler()"><img src="../../images/btnCancel.png" border="0" title="删除菜品"/></a>' ;
	}else{
		return Ext.ux.txtFormat.gridDou(value)
			+ '<a href="javascript:orderDeleteFoodOperationHandler()"><img src="../../images/btnCancel.png" border="0" title="删除菜品"/></a>';
	}
};

function orderGiftRenderer(value, cellmeta, record, rowIndex, columnIndex, store){
	
	giftRender.id ++;
	if(Ext.ux.cfs.isGift(record.get('status'))){
		var checkId= 'checked'+giftRender.id;
		if(record.get('isGift') == true){
			giftRender.checkeds.push(checkId);
		}
		
		value = Ext.ux.staffGift?'&nbsp;<input type="checkbox" id="'+(reloadData?reloadData:checkId)+'" style="height:18px;width:18px;vertical-align: middle;" onclick="orderGiftFoodOperationHandler(this)"/>赠送':'';
	}	
	return value;	

}


/**
 * 判定单签账单组账单操作类型, 1:单一 2:全组, 
 */
function theGroupIsSingleOrGroup(){
	var radio = document.getElementsByName('radioOrderGroupOperationScope');
	for(var i = 0; i < radio.length; i++){
		if(radio[i].checked){
			return radio[i].value;
			break;
		}
	}
};

/**
 * 添加菜品, 绑定数据
 */
function bindGridData(_c){
	var grid = _c.grid, record = _c.record;
	var isAlreadyOrderd = true;
	var sindex = 0;
	for ( var i = 0; i < grid.order.orderFoods.length; i++) {
		var temp = grid.order.orderFoods[i];
		
		if (temp.id == record.data.id && (temp.dataType == 2 || isRepaid)) {
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
			},
			isGift : record.isGift
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
 * 刷新账单信息 
 */
function refreshOrderHandler(c){
	var girdData = orderSingleGridPanel.order.orderFoods;
	var selData = new Array();
	
	var refresh = new Ext.LoadMask(document.body, {
	    msg  : '正在更新已点菜列表,请稍等......',
	    disabled : false,
	    removeMask : true
	});

	for(var i = (girdData.length - 1); i >= 0; i--){
		if(girdData[i].dataType == 2){
			selData.push(girdData[i]);
		}
	}
	refresh.show();
	Ext.Ajax.request({
		url : '../../QueryOrder.do',
		params : {
			'tableID' : tableDate.id
		},
		success : function(response, options) {
			var jr = Ext.util.JSON.decode(response.responseText);
			if (jr.success == true) {
				// 更新菜品状态为已点菜
				refreshOrderFoodDataType(jr.other.order.orderFoods);
				
				for(var i = (selData.length - 1); i >= 0 ; i--){
					jr.other.order.orderFoods.push(selData[i]);
				}
				
//						// 合并重复数据
//						var tempData = {root:[]};
//						for(var i = 0; i < jr.root.length; i++){
//							
//							var cs = true;
//							for(var j = 0; j < tempData.root.length; j++){
//								if(compareDataType(tempData.root[j], jr.root[i])){
//									if(compareTasteGroup(tempData.root[j].tasteGroup,  jr.root[i].tasteGroup)){
//										if(tempData.root[j].id == jr.root[i].id){
//											cs = false;
//											tempData.root[j].count += jr.root[i].count;
//										}
//					
//									}
//								}
//							}
//							if(cs){
//								tempData.root.push(jr.root[i]);
//							}
//						}	
				
				orderSingleGridPanel.order = jr.other.order;
				orderSingleGridPanel.getStore().loadData({root:orderSingleGridPanel.order.orderFoods});
				
				orderGroupDisplayRefresh({
					control : orderSingleGridPanel
				});
				if(!c){
					Ext.example.msg('提示', '已更新已点菜列表,请继续操作.');
				}
			} else {
				Ext.ux.showMsg(jr);
			}
			refresh.hide();
		},
		failure : function(response, options) {
			var rj = Ext.util.JSON.decode(response.responseText);
			Ext.ux.showMsg(rj);
			refresh.hide();
		}
	});
};

/**
 * 根据返回做错误码作相关操作
 */
function refreshOrder(res){
	var href = 'TableSelect.html';
	if(eval(res.code == 14)){
		Ext.MessageBox.confirm('警告', '账单信息已更新,是否刷新已点菜并继续操作?否则返回.', function(btn){
			if(btn == 'yes'){
				refreshOrderHandler();
			}else{
				location.href = href;
			}
		},this);
	}else if(eval(res.code == 3)){
/*		var interval = 3;
		var action = '<br/>点击确定返回或&nbsp;<span id="returnInterval" style="color:red;"></span>&nbsp;之后自动跳转.';
		new Ext.util.TaskRunner().start({
			run: function(){
				if(interval < 1){
					location.href = href;
				}
				Ext.getDom('returnInterval').innerHTML = interval;
				interval--;
		    },
		    interval : 1000
		});
		Ext.MessageBox.show({
			title : res.title,
			msg : ('<center>' + res.msg + '.' + action + '</center>'),
			width : 300,
			buttons : Ext.MessageBox.OK,
			fn : function(){

			}
		});*/
		if(submitType != 6){
			location.href = "TableSelect.html";
		}
	}else if(eval(res.code == Ext.ux.errorCode.ORDER_EXPIRED)){
		Ext.MessageBox.INSERTUPDATE = {yes:"继续提交", no:"刷新账单"};
		Ext.Msg.show({
		   title:'提示',
		   msg: res.msg,
		   buttons: Ext.MessageBox.INSERTUPDATE,
		   fn: function(btn){
			   	if(btn == 'yes'){
			   		submitSingleOrderHandler(res.reCommitData);
			   	}else if(btn == 'no'){
			   		isFree = false;
			   		refreshOrderHandler();
			   	}
		   }
		});
	}else{
		Ext.MessageBox.show({
			title : res.title,
			msg : res.msg,
			width : 300,
			buttons : Ext.MessageBox.OK
		});
	}
};

function submitRepaidOrderMain(_c){
	commitOperate.show();
	var commentOut = '';
	var discountID = Ext.getCmp('comboDiscount');
	var servicePlan = Ext.getCmp('repaid_comboServicePlan');
	var erasePrice = Ext.getCmp('numErasePrice');
	
	if(typeof sysSetting.setting != 'undefined' && erasePrice.getValue() > sysSetting.setting.eraseQuota){
		Ext.example.msg('提示', '抹数金额不能大于系统设置,请重新输入.');
		return;
	}
	orderPanel.buttons[0].setDisabled(true);
	orderPanel.buttons[1].setDisabled(true);
	orderPanel.buttons[5].setDisabled(true);
	
	orderDataModel.tableAlias = _c.grid.order.table.alias;
	orderDataModel.customNum = _c.grid.order['customNum'];
	orderDataModel.orderFoods = _c.grid.order.orderFoods;
	orderDataModel.categoryValue = _c.grid.order["categoryValue"];
	orderDataModel.id = _c.grid.order["id"];
	orderDataModel.orderDate =  _c.grid.order["orderDate"];
	
	var member, discount = discountID.getValue(), settleType = 1, pricePlanId="", couponId="";
	if(re_member){
		//是否手动注入会员
		if(typeof re_member.hasMember != 'undefined'){
			if(re_member.hasMember){
				member = re_member.id;
				discount = re_member.discount.id;
				pricePlanId = re_member.pricePlanId || "";
				settleType = 2;
			}else{
				member = '';
			}
			
		}else{
			member = re_member.id; 
			pricePlanId = Ext.getCmp('repaid_txtPricePlanForPayOrder').getValue();
			settleType = 2;
		}
	}
	
	Ext.Ajax.request({
		url : "../../RepaidOrder.do",
		params : {
			orderId : _c.grid.order["id"],
			memberID : member,
			discountID : discount,
			servicePlan : servicePlan.getValue(),
			payType : _c.commit_payType,
			payType_money : _c.payType_money,
			comment : commentOut,
			erasePrice : erasePrice.getValue(),
			commitOrderData : JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)),
			customNum : _c.grid.order['customNum'],
			pricePlanId : pricePlanId,
			coupons : coupons ? coupons.join(',') : '-1',
			settleType : settleType,
			orientedPrinter : Ext.util.Cookies.get(document.domain + '_printers')		//特定打印机打印
		},
		success : function(response, options) {
			var resultJSON = Ext.util.JSON.decode(response.responseText);
			if (resultJSON.success == true) {
				skip({href : 'Bills.html', msg : resultJSON.data});
			} else {
				commitOperate.hide();
				orderPanel.buttons[0].setDisabled(false);
				orderPanel.buttons[1].setDisabled(false);
				orderPanel.buttons[5].setDisabled(false);
				Ext.MessageBox.show({
					msg : resultJSON.data,
					width : 300,
					buttons : Ext.MessageBox.OK
				});
			}
		},
		failure : function(response, options) {
			commitOperate.hide();
			orderPanel.buttons[0].setDisabled(false);
			orderPanel.buttons[1].setDisabled(false);
			orderPanel.buttons[5].setDisabled(false);
			Ext.MessageBox.show({
				title : '提示',
				msg : "请求超时, 请刷新后再试",
				width : 300,
				buttons : Ext.MessageBox.OK
			});
		}
	});	
}

/**
 * 反结账时调用
 * @param {} _c
 */
function submitRepaidOrderHandler(_c){
	var orderFoods = _c.grid.order.orderFoods;
	if(orderFoods.length > 0){
		//var foodPara = Wireless.ux.createOrder({orderFoods: orderFoods, dataType : 3});
		var commit_payType = Ext.getCmp('repaid_comboPayType').getValue();
		_c.commit_payType = commit_payType;
		if(commit_payType == 100){
/*			var mixedPayMoney = primaryOrderData.other.order.actualPrice;
			for(var pay in payMoneyCalc){
				if(typeof payMoneyCalc[pay] != 'boolean'){
					mixedPayMoney -= payMoneyCalc[pay];
				}
			}					
			
			
			if(mixedPayMoney != 0){
				Ext.example.msg('提示', '混合结账的金额不等于账单的实收金额');
				return;
			}else{

			}	*/
			var payType_money = '';
			for (var i = 0; i < repaid_payType.length; i++) {
				if(Ext.getCmp('repaid_chbForPayType' + repaid_payType[i].id) && Ext.getCmp('repaid_chbForPayType' + repaid_payType[i].id).getValue()){
					if(payType_money){
						payType_money += '&';
					}
					payType_money += (repaid_payType[i].id + ',' + Ext.getCmp('repaid_numForPayType' + repaid_payType[i].id).getValue());
				}
			}
			_c.payType_money = payType_money;
			submitRepaidOrderMain(_c);			
		}else{
			submitRepaidOrderMain(_c);
		}
		
	}
}

/**
 * 单张账单提高操作
 */
function submitSingleOrderHandler(_c){
	var orderFoods = _c.grid.order.orderFoods;
	
	orderDataModel.tableID = tableDate.id;
	orderDataModel.customNum = 1;
	orderDataModel.orderFoods = (typeof _c.commitType != 'undefined'? frontNewOrderFood : orderFoods);
	orderDataModel.categoryValue = tableCategory;
	orderDataModel.id = _c.grid.order.id;
	orderDataModel.orderDate = (typeof _c.grid.order == 'undefined' ? '' : _c.grid.order.orderDate);
	
	setButtonDisabled(true);
	commitOperate.show();
	
	Ext.Ajax.request({
		url : '../../InsertOrder.do',
		params : {
			commitOrderData : JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)),
			type : (typeof _c.commitType != 'undefined'? _c.commitType : isFree ? 1 : 7),
			notPrint : _c.notPrint === true ? true : false
		},
		success : function(response, options) {

			var jr = Ext.util.JSON.decode(response.responseText);
			_c.title = jr.title;
			_c.msg = jr.msg;
			
			if (jr.success == true) {
				skip(_c);
			} else {
				jr.reCommitData = _c;
				jr.reCommitData.commitType = 23;
				refreshOrder(jr);
				setButtonDisabled(false);
				commitOperate.hide();
			}
		},
		failure : function(response, options) {
			setButtonDisabled(false);
			Ext.ux.showMsg(Ext.util.JSON.decode(response.responseText));
		}
	});
/*	现在可以空单提交
 * else if(orderFoods.length == 0){
		Ext.MessageBox.show({
			msg : '还没有选择任何菜品，暂时不能提交',
			width : 300,
			buttons : Ext.MessageBox.OK
		});
	}*/
}

/**
 * 账单组提交
 */

/**
 * 提交账单信息
 */
function submitOrderHandler(_c){
	_c = _c != null && typeof _c != 'undefined' ? _c : {};
	_c.grid = orderSingleGridPanel;
	if(isRepaid){
		submitRepaidOrderHandler(_c);
	}else{
		submitSingleOrderHandler(_c);
	}
	
};

/**
 * 设置按钮操作状态
 */
function setButtonDisabled(s){
	if(typeof s == 'boolean'){
		orderPanel.buttons[0].setDisabled(s);
		orderPanel.buttons[1].setDisabled(s);
		orderPanel.buttons[2].setDisabled(s);
		orderPanel.buttons[3].setDisabled(s);
		orderPanel.buttons[4].setDisabled(s);
		orderPanel.buttons[5].setDisabled(s);
	}
}

/**
 * 
 */
function skip(_c){
/*	var interval = 3;
	var action = '';
	if(typeof(_c.href) != 'undefined'){
		action = '&nbsp;<span id="returnInterval" style="color:red;"></span>&nbsp;之后自动跳转.';
		new Ext.util.TaskRunner().start({
			run: function(){
				if(interval < 1){
					var num = _c.href.indexOf('?');
					if(num <= 0){
						location.href = _c.href;	
					}else{
						setDynamicKey(_c.href.substring(0, num), _c.href.substr(num + 1));
					}
												
				}
				Ext.getDom('returnInterval').innerHTML = interval;
				interval--;
			},
			interval : 1000
		});
	}
	Ext.MessageBox.show({
		msg : (_c.msg + action),
		width : 300,
		buttons : Ext.MessageBox.OK,
		fn : function() {
			if(typeof(_c.href) != 'undefined'){
				var num = _c.href.indexOf('?');
				if(num <= 0){
					location.href = _c.href;	
				}else{
					setDynamicKey(_c.href.substring(0, num), _c.href.substr(num + 1));
				}						
			}
		}
	});*/
	
			if(typeof(_c.href) != 'undefined'){
				var num = _c.href.indexOf('?');
				if(num <= 0){
					location.href = _c.href;	
				}else{
					setDynamicKey(_c.href.substring(0, num), _c.href.substr(num + 1));
				}						
			}	
}