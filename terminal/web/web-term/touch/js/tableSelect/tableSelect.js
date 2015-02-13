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

var payment_searchMemberTypeTemplet = '<div data-role="popup" id="payment_searchMemberType" data-theme="d" class="payment_searchMemberType">'+
'<ul id="payment_searchMemberTypeCmp" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">'+
'<li data-role="divider" data-theme="e" style="line-height: 30px;">选择号码来源:</li>'+
'<li  class="popupButtonList" onclick="readMemberByCondtion(1)"><a >手机卡</a></li>'+
'<li  class="popupButtonList" onclick="readMemberByCondtion(3)"><a >会员实体卡</a></li>'+
'<li  class="popupButtonList" onclick="readMemberByCondtion(2)"><a >微信卡</a></li>'+
'</ul></div>'; 

var payment_popupDiscountCmp4MemberTemplet = '<div data-role="popup" id="payment_popupDiscountCmp4Member" data-theme="d" class="payment_popupDiscountCmp4Member">'+
    		'<ul id="payment_discountList4Member" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b"></ul>'+
    	'</div>';

var payment_popupPricePlanCmp4MemberTemplet = '<div data-role="popup" id="payment_popupPricePlanCmp4Member" data-theme="d" class="payment_popupPricePlanCmp4Member">'+
		    '<ul id="payment_pricePlanList4Member" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">'+
		'</ul></div>';

var payment_popupCouponCmp4MemberTemplet = '<div data-role="popup" id="payment_popupCouponCmp4Member" data-theme="d" class="payment_popupCouponCmp4Member">'+
			'<ul id="payment_couponList4Member" data-role="listview" data-inset="true" style="min-width:150px;" data-theme="b">'+
		'</ul></div>';

$(function(){
	//餐厅选择界面高度
	$('#tableAndRegionsCmp').height(document.body.clientHeight - 86);	
	//点菜界面高度
	$('#orderFoodCenterCmp').height(document.body.clientHeight - 210);
	document.getElementById('foodsCmp').style.height = (document.body.clientHeight - 210)+'px';		
	//沽清菜界面高度
	$('#stopSellCmp').height(document.body.clientHeight - 125);	
	document.getElementById('foods4StopSellCmp').style.height = (document.body.clientHeight - 210)+'px';
	document.getElementById('divFoods4StopSellCmp').style.height = (document.body.clientHeight - 210)+'px';
	//已点菜界面高度
	$('#orderFoodListCmp').height(document.body.clientHeight - 125);
	//结账界面高度 & 菜品列表高度
	$('#paymentCmp').height(document.body.clientHeight - 86);	
	$('#payment_orderFoodListCmp').height(document.body.clientHeight - 126);	
	
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
				click : 'ts.selectTable({event : this, id : '+ c.data.id +',tableAlias :'+ c.data.alias +'})',
				alias : c.data.alias && c.data.alias != 0?c.data.alias:'<font color="green">搭台</font>',
				theme : c.data.statusValue == '1' ? "e" : "c",
				name : c.data.name == "" || typeof c.data.name != 'string' ? c.data.alias + "号桌" : c.data.name
			});				
		}
	});
 		
	
	
	Util.LM.show();		
	
	$.ajax({
		url : '../VerifyLogin.do',
		success : function(data, status, xhr){
			
			if(data.success){
				//刷新时去除#
				if(location.href.indexOf('#') > 0){
					location.href = 'tableSelect.html';
					return;
				}
				
				$('#spanStaffNameForDisplayToTS').html('服务员: '+ data.other.staff.staffName);
				
				/**
				 * 定时器，定时刷新餐桌选择页面数据
				 */
				window.setInterval("initTableData()", 20 * 60 * 1000);
				//加载基础数据
				Util.LM.show();
				initTableData();
				initFoodData({firstTime:true});
				
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
								$('#giftFoodOperate').show();
							}
						}
					},
					error : function(request, status, err){
					}
				}); 				
			}else{	
				Util.LM.hide();	
				Util.msg.alert({
					msg : '请先登录',
					topTip : true
				});
				setTimeout(function(){
					location.href = 'index.htm';
				}, 2000);
			}
		},
		error : function(request, status, error){
			Util.LM.hide();	
			Util.msg.alert({
				msg : '操作有误,请刷新页面',
				topTip : true
			});
			setTimeout(function(){
				location.href = 'index.htm';
			}, 2000);
		}
	});
	
	//餐台选择, 转菜, 查台输入框
	ts.s.init({file : 'txtTableNumForTS'});	
	
	//设置数字键盘输入
	$('.numberInputStyle').focus(function(){
		focusInput = this.id;
	});		
	
});	


/**
 * 通过其他界面返回餐台选择
 */
ts.loadData = function(){
	location.href = '#tableSelectMgr';
	initTableData();
	of.loadFoodDateAction = window.setInterval("keepLoadTableData()", 500);	
}

//设置搜索出来的餐台升序, 按名称长短
ts.searchTableCompareByName = function (obj1, obj2) {
    var val1 = obj1.name.length;
    var val2 = obj2.name.length;
    if (val1 > val2) {
        return 1;
    } else if (val1 < val2) {
        return -1;
    } else {
        return 0;
    }            
} 

window.onload=function(){
	
YBZ_win_title = "手写板";//手写板名称
YBZ_follow = false;//手写板吸附在输入框附近 false 右下角打开
YBZ_skin = "black";
//default||aero||chrome||opera||simple||idialog||twitter||blue||black||green
YBZ_tipsopen = false;//是否在网页输入框中加入手写提示
YBZ_fixed = true;//是否固定手写窗口	
	
	
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
	
	$('#searchFoodInput').focus(function(){
		focusInput = this.id;
		if(this.id == 'searchFoodInput'){
			of.s.fireEvent();
		}		
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

	//快捷键
	$(document).keydown(function(event){
		if($.mobile.activePage.attr( "id" ) == 'paymentMgr'){//结账界面中使用
	    	if(event.which == "109") {//减号 
				if(isMixedPay){
					mixPayAction(true);
				}else{
					paySubmit(6);
				}
	    	}else if(event.which == "107"){//加号
				if(isMixedPay){
					mixPayAction();
				}else{
					paySubmit(1);
				}
	    	}      		
		}else if($.mobile.activePage.attr( "id" ) == 'tableSelectMgr'){//餐厅选择界面使用
	    	if(event.which == "107"){//加号
				if(ts.commitTableOrTran != 'lookup'){
					ts.createOrderForLookup();
				}else{
					ts.submitForSelectTableOrTransFood();
				}
	    	}else if(event.which == "13"){//回车
	    		ts.toPaymentMgr();
	    	}  			
		}
	});	
	//渲染会员读取窗口
	$('#lookupOrderDetail').trigger('create').trigger('refresh');
	
	//渲染会员读取窗口
	$('#readMemberWin').trigger('create').trigger('refresh');	
	
	//会员读卡
    $('#txtMemberInfo4Read').on('keypress',function(event){
        if(event.keyCode == "13")    
        {
        	readMemberByCondtion();
        }
    });		
	
	//找零快捷键
    $('#txtInputRecipt').on('keypress',function(event){
        if(event.keyCode == "13")    
        {
        	payInputRecipt();
        }
    }); 
    
}


/**
 * 输入台号后直接结账
 */
ts.toPaymentMgr = function(){
	var tableInfo = $('#txtTableNumForTS').val();
	var tableId;
	if(isNaN(tableInfo)){
		var temp = tables.slice(0);
		var table4Search = [];
		for(var i = 0; i < temp.length; i++){
			if((temp[i].name + '').indexOf(tableInfo.toUpperCase()) != -1){
				table4Search.push(temp[i]);
			}
		}	
		table4Search = table4Search.sort(ts.searchTableCompareByName);
		if(table4Search.length > 0){
			tableId = table4Search[0].id;
		}else{
			Util.msg.alert({
				msg : '没有此餐台, 请重新输入',
				topTip : true
			});			
			tableInfo.focus();
			return;
		}
		
	}
	updateTable({
		toPay : true,
		id : tableId,
		alias : !tableId?tableInfo:''
	});	
}

//改变窗口时
window.onresize = function(){
	//动态高度
	$('#orderFoodCenterCmp').height(document.body.clientHeight - 210);
	document.getElementById('foodsCmp').style.height = (document.body.clientHeight - 210)+'px';
}

/**
 * 更新菜品列表
 * @param c
 */
function initFoodData(c){
	c = c || {};
	Util.LM.show();
	//加载菜品列表
	$.ajax({
		url : '../QueryMenu.do',
		type : 'post',
		async: c.firstTime?false : true,
		data : {
			dataSource : 'foodList'
		},
		success : function(data, status, xhr){
			Util.LM.hide();	
			
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
			
			if(c.firstTime){
				//加载临时厨房
				$.post('../QueryMenu.do', {dataSource:'isAllowTempKitchen'}, function(data){
					of.tempKitchens = data.root;
				});
				
				//加载所有口味
				$.ajax({
					url : '../QueryMenu.do',
					type : 'post',
					data : {
						dataSource : 'tastes'
					},
					success : function(result, status, xhr){
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
					},
					error : function(request, status, err){
						Util.msg.alert({
							msg : request.msg,
							renderTo : 'tableSelectMgr'
						});
					}
				}); 
			}
			

		},
		error : function(request, status, err){
			Util.msg.alert({
				msg : request.msg,
				renderTo : 'tableSelectMgr'
			});
		}
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
				//数字键盘触发, 除了开台和点餐转台和会员
				if(ts.commitTableOrTran != 'openTable' && ts.commitTableOrTran != 'tableTransTable' && ts.commitTableOrTran != 'member'){
					var data = null, temp = null;
					if(ts.s.fileValue.trim().length > 0){
						data = [];
						temp = tables.slice(0);
						for(var i = 0; i < temp.length; i++){
							if(ts.commitTableOrTran == 'apartTable' && temp[i].alias == 0){
								continue;
							}else if((temp[i].name + '').indexOf(ts.s.fileValue.trim().toUpperCase()) != -1){
								data.push(temp[i]);
							}else if((temp[i].alias + '').indexOf(ts.s.fileValue.trim()) != -1){
								data.push(temp[i]);
							}
						}				
					}
					if(data != null){
						initSearchTables({
							data : data.slice(0, 8)
						});					
					}
					//如果是拆台则关闭后缀
					if(ts.commitTableOrTran == 'apartTable'){
						$('#divSelectTablesSuffixForTs').hide();
						$('#divSelectTablesForTs').show();
					}
					
					data = null;
					temp = null;					
				}
				

			};
		}
		return this.file;
	},
	valueBack : function(){
		if(this.file.value){
			this.file.value = this.file.value.substring(0, this.file.value.length - 1);
			this.file.oninput(this.file);			
		}

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
	var oldTable;
	if(c && c.oldAlias){
		oldTable = getTableByAlias(c.oldAlias);
	}else{
		oldTable = uo.table;
	}
	
	var newTable = getTableByAlias(c.alias);
	
	$.post('../OperateTable.do', {
		dataSource : 'transTable',
		oldTableId : oldTable.id,
		newTableId : newTable.id
	},function(data){
		if(data.success){
			uo.closeTransOrderFood();
			initTableData();
			Util.msg.alert({
				msg : data.msg, 
				topTip : true
			});
			//返回主界面
			ts.loadData();
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
ts.toOrderFoodOrTransFood = function(c){
	if(ts.commitTableOrTran == 'table'){
		ts.renderToCreateOrder(c.alias, 1);
	}else if(ts.commitTableOrTran == 'trans'){
		uo.transFood({alias:c.alias});
	}else if(ts.commitTableOrTran == 'allTrans'){
		uo.transFood({alias:c.alias, allTrans : -1});
	}else if(ts.commitTableOrTran == 'transTable'){
		ts.transTable({alias:c.alias})
	}else if(ts.commitTableOrTran == 'lookup'){
		updateTable({
			id : c.id,
			alias : c.alias
		});		
	}else if(ts.commitTableOrTran == 'apartTable'){
		$('#divSelectTablesForTs').hide();
		$('#divSelectTablesSuffixForTs').show();
		ts.table.id = c.id;
	}
}

/**
 * 确定精确的搜索条件
 */
ts.submitForSelectTableOrTransFood = function(){
	if(ts.commitTableOrTran == 'table'){//普通选台
		ts.submitForSelectTableNumTS();
	}else if(ts.commitTableOrTran == 'trans'){//单条转菜
		uo.transFood({alias:$('#txtTableNumForTS').val()});
	}else if(ts.commitTableOrTran == 'allTrans'){//全单转菜
		uo.transFood({alias:$('#txtTableNumForTS').val(), allTrans : -1});
	}else if(ts.commitTableOrTran == 'transTable'){//转台
		ts.transTable({alias:$('#txtTableNumForTS').val()})
	}else if(ts.commitTableOrTran == 'tableTransTable'){//前台转台
		ts.transTable({alias:$('#numToOtherTable').val(), oldAlias:$('#txtTableNumForTS').val()})
	}else if(ts.commitTableOrTran == 'lookup'){//查台
		var tableInfo = $('#txtTableNumForTS').val();
		var tableId;
		if(isNaN(tableInfo)){
			var temp = tables.slice(0);
			var table4Search = [];
			for(var i = 0; i < temp.length; i++){
				if((temp[i].name + '').indexOf(tableInfo.toUpperCase()) != -1){
					table4Search.push(temp[i]);
				}
			}	
			table4Search = table4Search.sort(ts.searchTableCompareByName);
			if(table4Search.length > 0){
				tableId = table4Search[0].id;
			}else{
				Util.msg.alert({
					msg : '没有此餐台, 请重新输入',
					topTip : true
				});			
				tableInfo.focus();
				return;
			}
			
		}		
		updateTable({
			id : tableId,
			alias : !tableId?tableInfo:''
		});			
	}else if(ts.commitTableOrTran == 'openTable'){//开台
		ts.createOrderForShowMessageTS();
	}else if(ts.commitTableOrTran == 'apartTable'){//拆台
		$('#divSelectTablesForTs').hide();
		$('#divSelectTablesSuffixForTs').show();
		ts.table = getTableByAlias($('#txtTableNumForTS').val());
	}else if(ts.commitTableOrTran == 'member'){//会员
		uo.useMemberForOrderAction();
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
	
	Util.LM.show();
	
	orderDataModel.tableID = ts.table.id;
	orderDataModel.customNum = customNum;
	orderDataModel.orderFoods = [];
	orderDataModel.categoryValue =  ts.table.categoryValue;
	
	$.post('../InsertOrder.do', {
		commitOrderData : JSON.stringify(Wireless.ux.commitOrderData(orderDataModel)),
		type : 1,
		notPrint : false
	}, function(result){
		Util.LM.hide();
		if (result.success) {
			ts.closeTableWithPeople();
			initTableData();
			Util.msg.alert({
				msg : '开台成功!',
				topTip : true
			});
		}else{
			Util.msg.alert({
				msg : result.msg,
				topTip : true
			});			
		}
	});
	
}
/**
 * 设置为查台
 */
ts.createOrderForLookup = function (){

	ts.commitTableOrTran = 'lookup';
	//隐藏数量输入
	$('#td4TxtFoodNumForTran').hide();
	//增加结账按钮
	$('#ts_toPaymentMgr').show();
	$('#certain4searchTableCmps').buttonMarkup('refresh');
	$('#certain4searchTableCmps .ui-btn-text').html('点菜(+)');
	//设置为3个按钮并排
	$('#searchTableCmpsFoot a').addClass('tablePopbottomBtn');
	
	$("#txtTableNumForTS").val("");
	
	$('#transSomethingTitle').html("请输入桌号，查看已下单菜品");
	
	//打开控件
	uo.openTransOrderFood();	
}

/**
 * 设置为拆台
 */
ts.openApartTable = function(){
	//隐藏数量输入
	$('#td4TxtFoodNumForTran').hide();
	ts.commitTableOrTran = 'apartTable';
	
	$("#txtTableNumForTS").val("");
	
	$('#transSomethingTitle').html("请输入桌号，确认拆台");
	
	//打开控件
	uo.openTransOrderFood();		
}


/**
 * 拆台
 */
ts.openApartTableAction = function(s){
	Util.LM.show();
	$.post('../OperateTable.do', {
		dataSource : 'apartTable',
		tableID : ts.table.id,
		suffix : s
	}, function(result){
		Util.LM.hide();
		if(result.success){
			uo.closeTransOrderFood();
			location.href = '#orderFoodListMgr';
			uo.show({
				table : result.root[0]
			});
		}else{
			Util.msg.alert({
				msg : result.msg,
				topTip : true
			});
		}
	});		
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
		id : c.id,
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

/**
 * 去沽清界面
 */
ts.stopSellMgr = function(){
	location.href = '#stopSellMgr';
	ss.entry();
}

//进入点菜界面
ts.renderToCreateOrder = function(tableNo, peopleNo){
	if(tableNo > 0){
		//关闭台操作popup
		uo.closeTransOrderFood();
		//设置餐台人数为默认
		$('#inputTableCustomerCountSet').val(1);
		
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
 * 根据alias或id返回table的最新状态
 * toPay : 是否去结账界面
 */
function updateTable(c){
	var table = null;
	$.ajax({
		url : '../QueryTable.do',
		type : 'post',
		data : {
			tableID : !c.alias || c.alias == 0?c.id : '', 
			alias : c.alias,
			random : Math.random()
		},
//		async : false,
		success : function(data, status, xhr){
			if(data.success && data.root.length > 0){
				table = data.root[0];
				c.table = table;
				if(c.toPay){
					//关闭选台
					uo.closeTransOrderFood();
					showPaymentMgr(c)
				}else{
					handleTableForTS(c);
				}
			}else{
				Util.msg.alert({
					msg : '没有此餐台, 请重新输入',
					topTip : true
				});				
			}

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
				$('#inputTableCustomerCountSet').focus();
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
				$('#inputTableCustomerCountSet').focus();
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
				//设置各状态数量
				$('#ts_freeTablesCount').text(freeTables.length);
				$('#ts_busyTablesCount').text(busyTables.length);
				
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
    	}else if(statusTable[x].region.id == o.id){
    		tempForRegion.push(statusTable[x]);
    	    ts.rn.selectingId = o.id;
    	} 
     }  
    temp = tempForRegion;
    //改变区域选中色
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
	
	of.loadFoodDateAction = window.setInterval(function(){
		if(!$('#foodsCmp').html()){
			of.initKitchenContent({deptId:-1});
		}else{
			clearInterval(of.loadFoodDateAction);
		}
	}, 400);
	
	of.initKitchenContent({deptId:-1});
	
	of.initNewFoodContent();
}

/**
 * 注销操作
 */
function loginOut(){
	Util.LM.show();
	$.ajax({
		url : '../LoginOut.do',
		success : function(data, status, xhr){
			location.href = "index.htm";
		}
	});	
	
}










