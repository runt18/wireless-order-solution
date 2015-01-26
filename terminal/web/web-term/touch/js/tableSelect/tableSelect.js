//全部餐桌
var tables = [];
//设置就餐餐桌数组
var busyTables = [];
//设置空闲餐桌数组
var freeTables = [];
//当前状态下的被选中区域的餐桌数组
var tempForRegion = [];
//被选中区域的所有状态餐桌数组
var tempForAllStatus = [];
//临时餐桌数组
var temp = [];
//定义存在餐桌的区域id数组
var regionId = [];
var region = [];
//设置当前状态类型（busy， free, allStatus）
var statusType = "";

//餐桌选择包,tt：转台, rn: 区域
var ts={
	table : {},
	rn : {},
	tt : {},
	tf : {},
	searchTable : false,
	commitTableOrTran : 'table'
}

var regionCmpTemplet = '<a data-role="button" data-inline="true" class="regionBtn" onclick="">{name}</a>';

$(function(){
	//点菜界面高度
	$('#orderFoodCenterCmp').height(document.body.clientHeight - 210);
	document.getElementById('foodsCmp').style.height = (document.body.clientHeight - 210)+'px';		
	//已点菜界面高度
	$('#orderFoodListCmp').height(document.body.clientHeight - 125);
//	$('#orderFoodLists').height(document.body.clientHeight - 125);
	//餐厅选择界面高度
	$('#tableAndRegionsCmp').height(document.body.clientHeight - 86);
	
	/**
	 * 餐桌分页包
	 */
	ts.tp = new Util.to.padding({
		renderTo : 'divTableShowForSelect',
		displayId : 'divDescForTableSelect-padding-msg',
		templet : function(c){
			return tableCmpTemplet.format({
				dataIndex : c.index,
				id : c.data.id,
				click : 'ts.selectTable({event : this, tableAlias :'+ c.data.alias +'})',
				alias : c.data.alias,
				theme : c.data.statusValue == '1' ? "e" : "c",
				name : c.data.name == "" || typeof c.data.name != 'string' ? c.data.alias + "号桌" : c.data.name
			});				
		}
	});
 		
	Util.LM.show();		
	
	$.ajax({
		url : '../VerifyLogin.do',
		success : function(data, status, xhr){
			Util.LM.hide();	
			if(data.success){
				$('#spanStaffNameForDisplayToTS').html('服务员: '+ data.other.staff.staffName);
				
				/**
				 * 定时器，定时刷新餐桌选择页面数据
				 */
				window.setInterval("initTableData()", 240000);
				//加载基础数据
				initTableData();
				initFoodData();	
				
				//验证员工权限	
				$.ajax({
					url : "../QueryStaff.do",
					type : 'post',
					data : {
						"privileges" : 1003,
						"checkPrivilege" : true
					},
					success : function(jr, status, xhr){
						if(jr.success){
							if(jr.other.havePrivileges != null){
//								Wireless.ux.staffGift = true;
								$('#giftFoodOperate').show();
							}
						}
					},
					error : function(request, status, err){
					}
				}); 				
			}else{	
				Util.msg.alert({
					msg : '请先登录',
					renderTo : 'tableSelectMgr',
					time : 3,
					fn : function(){
						location.href = 'login.html';
					}
				});
				return;
			}
		},
		error : function(request, status, error){
			Util.LM.hide();	
			Util.msg.alert({
				msg : '请先登录',
				renderTo : 'tableSelectMgr',
				time : 3,
				fn : function(){
					location.href = 'login.html';
				}
			});
			return;
		}
	});
	
	//餐台选择, 转菜, 查台输入框
	ts.s.init({file : 'txtTableNumForTS'});	
	
	//设置数字键盘输入
	$('.numberInputStyle').focus(function(){
		focusInput = this.id;
	});		
	
});	


window.onload=function(){
	
	$('input[data-type=txt]').focus(function(){
		//if(getcookie('isNeedWriter') == 'true'){
		if(true){
			//关闭数字键盘
			$('#numberKeyboard').hide();
			
			YBZ_open($(this)[0]);			
		}
	});
	
	$('input[data-type=num]').focus(function(){
		//if(getcookie('isNeedNumKeyboard') == 'true'){
		if(true){
			//关闭易笔字
			if(YBZ_win){
				YBZ_win.close();
			}		
			
			$('#numberKeyboard').show();			
		}
	});	
	
	//设置数字键盘输入
	$('.countInputStyle').focus(function(){
		focusInput = this.id;
	});	
	
	//打开手写板
	$(".handWriteCmp").change(function(){
		//$(this).val() 得到当前选中的值
		if($(this).val() == 'on'){
			//关闭数字键盘
			$('#numberKeyboard').hide();
			var myselect = $(".numberKeyboard");
			for (var i = 0; i < myselect.length; i++) {
				myselect[i].selectedIndex = 0;
			}
			myselect.slider('refresh'); 	
			
			YBZ_open(document.getElementById($(this).attr('data-for')));
			
			$('#'+$(this).attr('data-for')).focus();
		}else{
			YBZ_win.close();
		}
	});
	
	//打开数字键盘
	$(".numberKeyboard").change(function(){
		//$(this).val() 得到当前选中的值
		if($(this).val() == 'on'){
			$('#numberKeyboard').show();
			
			//关闭易笔字
			var myselect = $(".handWriteCmp");
			for (var i = 0; i < myselect.length; i++) {
				myselect[i].selectedIndex = 0;
			}
			myselect.slider('refresh'); 
			
			if(YBZ_win){
				YBZ_win.close();
			}
			$('#'+$(this).attr('data-for')).focus();
			
//			document.getElementById($(this).attr('data-for')).style.display="block";
		}else{
			$('#numberKeyboard').hide();
			//document.getElementById($(this).attr('data-for')).style.display="none";
		}
	});	

	

}

function initFoodData(){
	//加载菜品列表
	$.post('../QueryMenu.do', {dataSource:'foodList'}, function(data){
		var deptNodes = data.root;
		
		of.foodList = data.other.foodList;
		of.depts = {root:[]};
		of.kitchens = {totalProperty:0, root:[]}; 
		
		
		for (var i = 0; i < deptNodes.length; i++) {
			of.depts.root.push(deptNodes[i].deptNodeKey);
			for (var j = 0; j < deptNodes[i].deptNodeValue.length; j++) {
				var kitNode = deptNodes[i].deptNodeValue[j];
				kitNode.kitchenNodeKey.foods = kitNode.kitchenNodeValue.foodList;
				
				of.kitchens.root.push(kitNode.kitchenNodeKey);
			}
		}
		
		of.depts.totalProperty = of.depts.root.length;
		
		of.kitchens.totalProperty = of.kitchens.root.length;
		
		//清除没有菜品的厨房
		for(var i = of.kitchens.root.length - 1; i >= 0; i--){
			if(of.kitchens.root[i].foods.length <= 0){
				of.kitchens.root.splice(i, 1);
			}
		}	
		
		//加载临时厨房
		$.post('../QueryMenu.do', {dataSource:'isAllowTempKitchen'}, function(data){
			of.tempKitchens = data.root;
		});

		//加载所有口味
		$.post('../QueryMenu.do', {dataSource:'tastes'}, function(result){
			var tastes = result;
			
			of.allTastes = [];
			
			var data = [];
			if(tastes.root.length > 0){
				data.push({
					id : tastes.root[0].taste.cateValue,
					name : tastes.root[0].taste.cateText,
					items : []
				});
			}
			var has = true, temp = {};
			for(var i = 0; i < tastes.root.length; i++){
				
				of.allTastes.push(tastes.root[i]);
				
				has = false;
				for(var k = 0; k < data.length; k++){
					if(tastes.root[i].taste.cateValue == data[k].id){
						data[k].items.push(tastes.root[i]);
						has = true;
						break;
					}
				}
				if(!has){
					temp = {
						id : tastes.root[i].taste.cateValue,
						name : tastes.root[i].taste.cateText,
						items : []
					};
					temp.items.push(tastes.root[i]);
					data.push(temp);
				}
			}	
			
			of.tasteGroups = data;
			of.tasteGroups.unshift({
				id : -10,
				name : '常用口味',
				items : of.allTastes		
			});
			
		});			
		
	});	
}





/**
 * 餐台选择匹配
 */
ts.s = {
	file : null,
	fileValue : null,
	init : function(c){
		this.file = document.getElementById(c.file);
		if(typeof this.file.oninput != 'function'){
			this.file.oninput = function(e){

				ts.s.fileValue = ts.s.file.value;
				//数字键盘触发, 除了开台和点餐转台
				if(ts.commitTableOrTran != 'openTable' && ts.commitTableOrTran != 'tableTransTable'){
					var data = null, temp = null;
					if(ts.s.fileValue.trim().length > 0){
						data = [];
						temp = tables.slice(0);
						for(var i = 0; i < temp.length; i++){
							if((temp[i].alias + '').indexOf(ts.s.fileValue.trim()) != -1){
								data.push(temp[i]);
							}
						}				
					}
					if(data != null){
						initSearchTables({
							data : data.slice(0, 8)
						});					
					}
					data = null;
					temp = null;					
				}
				

			};
		}
		return this.file;
	},
	valueBack : function(){
		this.file.value = this.file.value.substring(0, this.file.value.length - 1);
		this.file.oninput(this.file);
		this.file.focus();
	},
	select : function(){
		this.file.select();
	},
	clear : function(){
		this.file.value = '';
		this.file.oninput(this.file);
		this.file.select();
	},
	callback : function(){
		co.s.clear();
	},
	onInput : function(){
		this.file.oninput(this.file);		
	},	
	fireEvent : function(){
		ts.s.onInput();
	}
};

/**
 * 执行转台
 * @param c  当前台号alias, 转去的台号oldAlias
 */
ts.transTable = function(c){
	var oldTableAlias;
	if(c && c.oldAlias){
		oldTableAlias = c.oldAlias;
	}else{
		oldTableAlias = uo.table.alias;
	}
	
	$.post('../TransTable.do', {
		oldTableAlias : oldTableAlias,
		newTableAlias : c.alias
	},function(data){
		if(data.success){
			uo.closeTransOrderFood();
			initTableData();
			Util.msg.alert({
				msg : data.msg, 
				topTip : true
			});
		}else{
			Util.msg.alert({
				title : '提示',
				msg : data.msg, 
				renderTo : 'orderFoodListMgr'
			});				
		}			
	});	
}

/**
 * 搜索出来的结果点击直接提交
 */
ts.toOrderFoodOrTransFood = function(alias){
	if(ts.commitTableOrTran == 'table'){
		ts.renderToCreateOrder(alias, 1);
	}else if(ts.commitTableOrTran == 'trans'){
		uo.transFood({alias:alias});
	}else if(ts.commitTableOrTran == 'allTrans'){
		uo.transFood({alias:alias, allTrans : -1});
	}else if(ts.commitTableOrTran == 'transTable'){
		ts.transTable({alias:alias})
	}else if(ts.commitTableOrTran == 'lookup'){
		updateTable({
			alias : alias
		});		
	}
}

/**
 * 确定精确的搜索条件
 */
ts.submitForSelectTableOrTransFood = function(){
	if(ts.commitTableOrTran == 'table'){
		ts.submitForSelectTableNumTS();
	}else if(ts.commitTableOrTran == 'trans'){
		uo.transFood({alias:$('#txtTableNumForTS').val()});
	}else if(ts.commitTableOrTran == 'allTrans'){
		uo.transFood({alias:$('#txtTableNumForTS').val(), allTrans : -1});
	}else if(ts.commitTableOrTran == 'transTable'){
		ts.transTable({alias:$('#txtTableNumForTS').val()})
	}else if(ts.commitTableOrTran == 'tableTransTable'){
		ts.transTable({alias:$('#numToOtherTable').val(), oldAlias:$('#txtTableNumForTS').val()})
	}else if(ts.commitTableOrTran == 'lookup'){
		updateTable({
			alias : $('#txtTableNumForTS').val()
		});
	}else if(ts.commitTableOrTran == 'openTable'){
		ts.createOrderForShowMessageTS();
	}
}


/**
 * 设置控件为转台
 */
ts.transTableForTS = function(){
	//隐藏数量输入
	$('#td4TxtFoodNumForTran').hide();
	
	//显示要转去的台号输入
	$('#td4ToOtherTable').show();
	
	ts.commitTableOrTran = 'tableTransTable';
	
	$('#transSomethingTitle').html("请输入桌号，确定转台");
	
	//打开控件
	uo.openTransOrderFood();
}

/**
 * 设置控件为直接开台
 */
/*ts.createOrderForTS = function(){
	//隐藏数量输入
	$('#td4TxtFoodNumForTran').hide();
	
	//显示人数输入
	$('#td4OpenTablePeople').show();
	
	ts.commitTableOrTran = 'openTable';
	
	$('#transSomethingTitle').html("请输入桌号和人数，确定开台");
	
	//打开控件
	uo.openTransOrderFood();
}*/

/**
 * 开台操作
 */
ts.openTableAction = function(){
	var customNum = $('#inputTableCustomerCountSet').val();
	
	if(isNaN(customNum)){
		Util.msg.alert({
			msg : '请填写正确的人数',
			topTip : true
		});			
		$('#inputTableCustomerCountSet').focus();
		return;
	}else if(customNum <= 0){
		Util.msg.alert({
			msg : '就餐人数不能少于0',
			topTip : true
		});
		$('#inputTableCustomerCountSet').focus();
		return;
	}
	
	orderDataModel.tableAlias = ts.table.alias;
	orderDataModel.customNum = customNum;
	orderDataModel.orderFoods = [];
	orderDataModel.categoryValue =  ts.table.categoryValue;
	
	$.post('../InsertOrder.do', {
		commitOrderData : JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)),
		type : 1,
		notPrint : false
	}, function(result){
		if (result.success) {
			ts.closeTableWithPeople();
			initTableData();
			Util.msg.alert({
				msg : '开台成功!',
				topTip : true
			});
		}
	});
	
}

/**
 * 设置为查台
 */
ts.createOrderForLookup = function (){
	//隐藏数量输入
	$('#td4TxtFoodNumForTran').hide();
	ts.commitTableOrTran = 'lookup';
	
	$("#txtTableNumForTS").val("");
	
	$('#transSomethingTitle').html("请输入桌号，查看已下单菜品");
	
	//打开控件
	uo.openTransOrderFood();	
}

/**
 * 查台后开台操作
 */
ts.createTableWithPeople = function(){
	var customNum = $('#inputTableCustomerCountSet').val();
	
	if(isNaN(customNum)){
		Util.msg.alert({
			msg : '请填写正确的人数',
			topTip : true
		});			
		$('#inputTableCustomerCountSet').focus();
		return;
	}else if(customNum <= 0){
		Util.msg.alert({
			msg : '就餐人数不能少于0',
			topTip : true
		});
		$('#inputTableCustomerCountSet').focus();
		return;
	}
	
	ts.renderToCreateOrder(ts.table.alias, customNum);
	$('#inputTableCustomerCountSet').val(1);
}

/**
 * 关闭开台
 */
ts.closeTableWithPeople = function(){
	$('#tableCustomerCountSet').popup('close');
	//人数输入框设置默认
	$('#inputTableCustomerCountSet').val(1);
}


/**
 * 选中一张餐桌
 * @param c
 */
ts.selectTable = function(c){
	updateTable({
		alias : c.tableAlias,
		event : c.event
	});
};

/**
 *桌号输入页面的确定按钮
 */
ts.submitForSelectTableNumTS = function(){
	//获得餐桌号
	var tableNo;
	var peopleNo = 1;
	if($("#txtTableNumForTS").val()){
		tableNo = parseInt($("#txtTableNumForTS").val());
		
	}else{
		tableNo = -1;
	}
	
	ts.renderToCreateOrder(tableNo, peopleNo);	
};

/**
 * 桌号人数界面的点菜按钮
 */
ts.createOrderForShowMessageTS = function(){
	var tableNo;
	var peopleNo;
	tableNo = parseInt($("#txtTableNumForTS").val());
	if($("#openTablePeople").val()){
		peopleNo = parseInt($("#txtPeopleNumForSM").val());
	}else{
		peopleNo = 1;
	}	
	renderToCreateOrder(tableNo, peopleNo);
};



//进入点菜界面
ts.renderToCreateOrder = function(tableNo, peopleNo){
	if(tableNo > 0){
		uo.closeTransOrderFood();
		
		var theTable = getTableByAlias(tableNo);
		//同时操作餐台时,选中状态没变化的餐桌处理
		//直接写台豪点菜时判断是否已点菜, 是则先给co.order.orderFoods赋值
		if(theTable.statusValue == 1){
			initOrderData({
				table : getTableByAlias(tableNo),
				createrOrder : 'createrOrder'
			});
		}else{
			theTable.customNum = peopleNo;
			of.show({
				table : theTable
/*				,callback : function(){
					initTableData();
				}*/
			});
		}		
	}else{
		Util.msg.alert({
			msg : '没有该餐桌，请重新输入一个桌号.', 
			topTip : true
		});
	}
}

/**
 * 根据餐桌alias，返回餐桌对象
 * @param {int} tableAlias
 * @returns {object} 
 */
function getTableByAlias(tableAlias){
	for(x in tables){
		if(tables[x].alias == tableAlias){
			return tables[x];		
		}
	}
}

/**
 * 选择空闲餐桌
 */
ts.selectFreeStatus = function(c){
	
	statusType = "free";
	temp = getStatusTables("free", tempForAllStatus);
	showTable(temp);
	
	$('#labTableStatus .ui-btn-text').text($(c.event).text());
};

/**
 * 选择就餐餐桌
 */
ts.selectBusyStatus = function(c){
	statusType = "busy";
	temp = getStatusTables("busy", tempForAllStatus);
	showTable(temp);
	
	$('#labTableStatus .ui-btn-text').text($(c.event).text());
};

/**
 * 选择全部餐桌
 */
ts.selectAllStatus = function(c){
	statusType = "allStatus";
	temp = getStatusTables("allStatus", tempForAllStatus);
	showTable(temp);
	
	$('#labTableStatus .ui-btn-text').text($(c.event).text());
};

/**
 * 取得当前区域下不同状态的餐桌数组 
 * @param {string} type 状态类型，分为空闲（free），就餐（busy），全部状态（allStatus）
 * @param {object} tempTables 区域数组对象
 * @returns {object} statusTables 餐桌数组对象 
 */
function getStatusTables(type, tempTables){
	var statusTables = [];
	if(type == "free"){
		for(x in tempTables){
			if(tempTables[x].statusValue == 0){
				statusTables.push(tempTables[x]);
			}
		}
	}else if(type == "busy"){
		for(x in tempTables){
			if(tempTables[x].statusValue == 1){
				statusTables.push(tempTables[x]);
			}
		}		
	}else if(type == "allStatus"){
/*		for(x in tempTables){
			statusTables.push(tempTables[x]);
		}	*/
		statusTables = tempTables;
	}
	return statusTables;
}

/**
 * 根据alias,返回table的最新状态
 */
function updateTable(c){
	var table = null;
	$.ajax({
		url : '../QueryTable.do',
		type : 'post',
		data : {
			alias : c.alias,
			random : Math.random()
		},
//		async : false,
		success : function(data, status, xhr){
			if(data.success){
				table = data.root[0];
			}
			c.table = table;
			handleTableForTS(c);
		},
		error : function(request, status, err){
			Util.msg.alert({
				title : '温馨提示',
				msg : err, 
				renderTo : 'orderFoodListMgr',
				time : 2
			});
		}
	});
}

/**
 * 当选中餐桌时，依据餐桌状态处理餐桌
 */
function handleTableForTS(c){
	var table = c.table;
	if(table != null){
		//更新状态后, 如果是查台, 则执行不同操作
		if(ts.commitTableOrTran == "lookup"){
			if(table.statusText == "就餐"){	
				//判断餐桌是否已经改变状态
				if(c.event && $(c.event).attr('data-theme') != 'e'){
					initTableData();
				}
				//关闭选台
				uo.closeTransOrderFood();
				//去已点菜界面
				location.href="#orderFoodListMgr";
				uo.show({
					table : table
				});
				
			}else{
				//把table信息放到全局变量中, 方便callback调用
				ts.table = table;
/*				Util.msg.alert({
					title : '温馨提示',
					msg : c.table.alias + '号餐桌还未开台, 请选择操作.', 
					btnEnter : '开台',
					buttons : 'YESBACK',
					renderTo : 'tableSelectMgr',
					certainCallback : function(){

					}
				});	*/
				uo.closeTransOrderFood();
				//选择开台人数
				firstTimeInput = true;
				$('#tableCustomerCountSetTitle').text(c.table.name + ' -- 输入人数');
				$('#tableCustomerCountSet').parent().addClass("pop").addClass("in");
				$('#tableCustomerCountSet').popup('open');
				$('#inputTableCustomerCountSet').select();				
				
			}			
		}else{
			//判断是否为已点菜餐桌
			if(table.statusText == "就餐"){	
				//判断餐桌是否已经改变状态
				if(c.event && $(c.event).attr('data-theme') != 'e'){
					initTableData();
				}
				//去已点菜界面
				location.href="#orderFoodListMgr";
				uo.show({
					table : table
				});
			}else{
				ts.table = table;
				//判断餐桌是否已经改变状态
				if(c.event && $(c.event).attr('data-theme') == 'e'){
					initTableData();
				}
				
				//选择开台人数
				firstTimeInput = true;
				$('#tableCustomerCountSetTitle').text(c.table.name + ' -- 输入人数');
				$('#tableCustomerCountSet').parent().addClass("pop").addClass("in");
				$('#tableCustomerCountSet').popup('open');
				$('#inputTableCustomerCountSet').select();				
			}			
		}
	
	}
}

/**
 * 初始化餐桌信息，保存到tables数组中
 * freeTables存放空闲餐桌，busyTables存放就餐餐桌
 */
function initTableData(){
	
	Util.LM.show();
	// 加载菜单数据
	$.ajax({
		url : '../QueryTable.do',
		type : 'post',
		data : {
			random : Math.random()
		},
		success : function(data, status, xhr){
			Util.LM.hide();
			tables = [];
			busyTables = [];
			freeTables = [];
			regionId = [];
			region = [];
			if(data.success){
				//把所有餐桌对象都放到本地数组tables中,freeTables存放空闲餐桌，busyTables存放就餐餐桌
				for(x in data.root){	
					if(data.root[x].statusValue == 0){
						freeTables.push(data.root[x]);
						
					}else if(data.root[x].statusValue == 1){
						busyTables.push(data.root[x]);
					}
					tables.push(data.root[x]);
				}
				//从tables数组中，遍历得到含有餐桌的区域数组region
				region.push(tables[0].region);
				regionId.push(tables[0].region.id);
				for(x in tables){
					var flag = false;
					for(y in regionId){
						if(regionId[y] == tables[x].region.id){		
							flag = true;
							break;
						}			
					}
					if(!flag){
						region.push(tables[x].region);
						regionId.push(tables[x].region.id);
					}
				}
				ts.rn.selectingId = 'divAllArea';
				ts.rn.pageNow = 1;
				var regionH = $("#divToolRightForSelect").height() - 6 * 65;
				ts.rn.limit = Math.floor(regionH/62);
				ts.rn.pageCount = Math.ceil(region.length/ts.rn.limit);
				
				//显示区域
				showRegion(region);
				
				//默认显示全部状态下的全部区域
				statusType = "allStatus";
				tempForAllStatus = tables;
				temp = tables;
				showTable(temp);
			}else{
				Util.msg.alert({
					title : data.title,
					msg : data.msg,
					renderTo : 'tableSelectMgr',
					time : 2
				});
			}
		},
		error : function(request, status, err){
			Util.LM.hide();
			Util.msg.alert({
				title : '温馨提示',
				msg : err, 
				renderTo : 'tableSelectMgr',
				time : 2
			});
		}
	});	
}

/**
 * 当div高度还未来得及变化时, 不断去渲染
 */
function keepLoadTableData(){
	if(!$('#divTableShowForSelect').html()){
		ts.tp.init({
		    data : temp
		});
		ts.tp.getFirstPage();
	}else{
		clearInterval(ts.loadTableDateAction);
	}
}

/**
 * 显示餐桌
 * @param {object} temp 需要显示的餐桌数组
 */
function showTable(temp){	
	if(temp.length != 0){
		ts.tp.init({
		    data : temp
		});
		ts.tp.getFirstPage();
		
		//关闭可能存在的状态popup
		$('#popupAllStatusCmp').popup('close');
	}else{
		$("#divTableShowForSelect").html("");
	}	
}

/**
 * 显示区域
 */
function showRegion(temp, pageNow){
	//添加区域信息
	var html = '';
	for (var i = 0; i < temp.length; i++) {
		html += '<a data-role="button" data-inline="true" data-type="region" class="regionBtn" onclick="ts.addTables({event:this, id:'+ temp[i].id +'})">'+ temp[i].name +'</a>'
	}
	
	$('#divSelectRegionForTS').html(html).trigger('create').trigger('refresh');
}

/**
 * 定义分页函数
 * @param {int} start 开始下标
 * @param {int} limit 一页最多显示的数目
 * @param {object} tempObject 需要分页的数组对象
 * @param {boolean} isPaging 是否需要分页
 * @returns {object} pageRoot 已经完成分页的数组对象
 */
/*function getPagingData(start, limit, tempObject, isPaging){
    var pageRoot = [];
    if(tempObject.length != 0 && isPaging){ 
    	var dataIndex = start, dataSize = limit;		
    	dataSize = (dataIndex + dataSize) > tempObject.length ? dataSize - ((dataIndex + dataSize) - tempObject.length) : dataSize;			
    	pageRoot = tempObject.slice(dataIndex, dataIndex + dataSize);	
    }else{
    	pageRoot = tempObject;
    }	
	return pageRoot;
}*/


/**
 * 点击区域，显示不同状态的餐桌数组
 * @param {object} o 
 */
ts.addTables = function(o){
    //把当前区域餐桌数组清空
    temp = [];
    tempForAllStatus = [];
    tempForRegion = [];
    var statusTable = [];
    //初始化当前区域的所有状态餐桌数组
    for(x in tables){
    	if(o.id == -1){
    		tempForAllStatus.push(tables[x]);	
    	}else if(tables[x].region.id == o.id){
    		tempForAllStatus.push(tables[x]); 		
    	} 
     }
   //判断当前处于哪个状态
    if(statusType == "allStatus"){
    	statusTable = tables;
    }else if(statusType == "free"){
    	statusTable = freeTables;
    }else if(statusType == "busy"){
    	statusTable = busyTables;
    }
    //获得当前状态的对应区域餐桌数组
    for(x in statusTable){
    	if(o.id == -1){
    		tempForRegion.push(statusTable[x]);
//    		$(".button-base.regionSelect").css("backgroundColor", "#D4F640");
//    		$("#divAllArea").css("backgroundColor", "#FFA07A");
    	}else if(statusTable[x].region.id == o.id){
    		tempForRegion.push(statusTable[x]);
//    		$(".button-base.regionSelect").css("backgroundColor", "#D4F640");
//    	    $("#divAllArea").css("backgroundColor", "#4EEE99");
    	    ts.rn.selectingId = o.id;
//    	    $("#" + o.id).css("backgroundColor", "#FFA07A");
    	} 
     }  
    temp = tempForRegion;
    //改变区域选中色
//    if(temp.length == 0){
//    	$(".button-base.regionSelect").css("backgroundColor", "#D4F640");
//	    $("#divAllArea").css("backgroundColor", "#4EEE99");
//	    $("#" + o.id).css("backgroundColor", "#FFA07A");
//    }
    
	$('#tableAndRegionsCmp a[data-type=region]').attr('data-theme', 'c').removeClass('ui-btn-up-e').addClass('ui-btn-up-c');
	
	$(o.event).attr('data-theme', 'e').removeClass('ui-btn-up-c').addClass('ui-btn-up-e');
    
    showTable(temp);
}


function toOrderFoodPage(table){
	//去点餐界面
	location.href = '#orderFoodMgr';

	$('#divNFCOTableBasicMsg').html(table.alias + '<br>' + table.name);
	
	of.table = table;
	of.newFood = [];
	
	//渲染数据
	of.initDeptContent();
	
	of.loadFoodDateAction = window.setInterval("keepLoadFoodData()", 500);
	
	of.initKitchenContent({deptId:-1});
	
	of.initNewFoodContent();
}


function loginOut(){
	Util.LM.show();
	$.ajax({
		url : '../LoginOut.do',
		success : function(data, status, xhr){
			location.href = "login.html";
		}
	});	
	
}












