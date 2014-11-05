//餐台选择匹配
ts.s = {
	file : null,
	fileValue : null,
	init : function(c){
		this.file = getDom(c.file);
		if(typeof this.file.oninput != 'function'){
			this.file.oninput = function(e){
				ts.s.fileValue = ts.s.file.value;
				var data = null, temp = null;
				if(ts.s.fileValue.trim().length > 0){
					data = [];
					temp = tables.slice(0);
					for(var i = 0; i < temp.length; i++){
//						alert(temp[i].alias + ', ' + ts.s.fileValue.trim())
						if((temp[i].alias + '').indexOf(ts.s.fileValue.trim()) != -1){
							data.push(temp[i]);
						}
					}				
				}
				initSearchTables({
					data : data.slice(0, 5)
				});
				data = null;
				temp = null;
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
	}
};


function initSearchTables(c){
	var html = '';
	for (var i = 0; i < c.data.length; i++) {
		
		html += Templet.ts.search_boxTable.format({
			dataIndex : c.data[i].id,
			alias : c.data[i].alias,
			dataClass : c.data[i].statusValue == '1' ? "\"main-box-base table-busy\"" : "\"main-box-base\"",
			tableName : c.data[i].name == "" || typeof c.data[i].name != 'string' ? c.data[i].alias + "号桌" : c.data[i].name
		});	
	}
	
	$('#divSelectTablesForTs').html(html);
}

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
		for(x in tempTables){
			statusTables.push(tempTables[x]);
		}	
	}
	return statusTables;
}

/**
 * 点击区域，显示不同状态的餐桌数组
 * @param {object} o 
 */
function addTables(o){
    //把当前区域餐桌数组清空
    temp = [];
    tempForAllStatus = [];
    tempForRegion = [];
    var statusTable = [];
    //初始化当前区域的所有状态餐桌数组
    for(x in tables){
    	if(o == "allTable"){
    		tempForAllStatus.push(tables[x]);	
    	}else if(tables[x].region.id == o.id.substr(6)){
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
    	if(o == "allTable"){
    		tempForRegion.push(statusTable[x]);
    		$(".button-base.regionSelect").css("backgroundColor", "#D4F640");
    		$("#divAllArea").css("backgroundColor", "#FFA07A");
    	}else if(statusTable[x].region.id == o.id.substr(6)){
    		tempForRegion.push(statusTable[x]);
    		$(".button-base.regionSelect").css("backgroundColor", "#D4F640");
    	    $("#divAllArea").css("backgroundColor", "#4EEE99");
    	    ts.rn.selectingId = o.id;
    	    $("#" + o.id).css("backgroundColor", "#FFA07A");
    	} 
     }  
    temp = tempForRegion;
    //如果没有餐桌，则改变区域选中色
    if(temp.length == 0){
    	$(".button-base.regionSelect").css("backgroundColor", "#D4F640");
	    $("#divAllArea").css("backgroundColor", "#4EEE99");
	    $("#" + o.id).css("backgroundColor", "#FFA07A");
    }
    showTable(temp);
}

/**
 * 点击全部状态按钮
 */
function showStatus(){
	$("#divStatus").slideToggle();
	$("#divAllStatus").css("backgroundColor", "#FFA07A");
	$("#busyForTableSelect").css("backgroundColor", "#D4F640");
	$("#freeForTableSelect").css("backgroundColor", "#D4F640");
	statusType = "allStatus";
	temp = tempForAllStatus;
	showTable(temp);
}

/**
 * 选择空闲餐桌
 */
ts.selectFreeStatus = function(){
	$("#freeForTableSelect").css("backgroundColor", "#FFA07A");
	$("#busyForTableSelect").css("backgroundColor", "#D4F640");
	$("#divAllStatus").css("backgroundColor", "#4EEE99");
	statusType = "free";
	temp = getStatusTables("free", tempForAllStatus);
	showTable(temp);
};

/**
 * 选择就餐餐桌
 */
ts.selectBusyStatus = function(){
	$("#busyForTableSelect").css("backgroundColor", "#FFA07A");
	$("#freeForTableSelect").css("backgroundColor", "#D4F640");
	$("#divAllStatus").css("backgroundColor", "#4EEE99");
	statusType = "busy";
	temp = getStatusTables("busy", tempForAllStatus);
	showTable(temp);
};

/**
 * 选中一张餐桌
 * @param c
 */
ts.selectTable = function(c){
	updateTable({
		alias : c.tableAlias
	});
};

/**
 * 当选中餐桌时，依据餐桌状态处理餐桌
 */
function handleTableForTS(c){
	var table = c.table;
	if(table != null){
		//判断是否为已点菜餐桌
		if(table.statusText == "就餐"){	
			//判断餐桌是否已经改变状态
			if(!$(c.event).hasClass('table-busy')){
				initTableData();
			}
			uo.show({
				table : table
			});
		}else{
			//判断餐桌是否已经改变状态
			if($(c.event).hasClass('table-busy')){
				initTableData();
			}
			Util.dialongDisplay({
				type : 'show', 
				renderTo : 'divShowMessageForTableSelect',
			});
			//关闭该界面
			$("#btnCancelForShowMessageTS").click(function (){
				Util.dialongDisplay({
					type:'hide', 
					renderTo:'divShowMessageForTableSelect'
				});
				inputNumVal = "";
				$("#txtPeopleNumForSM").val("1");
			});
			$("#txtTableNumForSM").val(table.alias);
			$("#txtPeopleNumForSM").select();
			inputNumId  = "txtPeopleNumForSM";
		}	
	}
}

/**
 * 点击数字键盘上的数字，对输入框进行输入
 * @param {object} o
 */
function inputNum(o){
	
	inputNumVal = $("#" + inputNumId).val();
	inputNumVal += o.innerHTML;
	$("#" + inputNumId).val(inputNumVal);
	//判断人数是否超过限定
	if(inputNumId == "txtPeopleNumForSM"){
		if(parseInt(inputNumVal) > 255){
			Util.msg.alert({
				title : '温馨提示',
				msg : '人数超过限定，请重新输入.', 
				time : 2
			});
			inputNumVal = "";
			$("#" + inputNumId).val(inputNumVal);
		}
	}
	//判断桌号是否超过限定
	if(inputNumId == "txtTableNumForSM" || inputNumId =="txtTableNumForTS"
		|| inputNumId == "txtOldTableForTS" || inputNumId == "txtNewTableForTS"){
		if(parseInt(inputNumVal) > 65536){
			Util.msg.alert({
				title : '温馨提示',
				msg : '桌号超过限定，请重新输入.', 
				time : 2
			});
			inputNumVal = "";
			$("#" + inputNumId).val(inputNumVal);
		}
	}
	$("#" + inputNumId).focus();
	if(getDom(inputNumId).oninput){
		getDom(inputNumId).oninput();
	}
}

//进入点菜界面
function renderToCreateOrder(tableNo, peopleNo){
	if(hasTable(tables, tableNo)){
		
		Util.dialongDisplay({
			type:'hide', 
			renderTo:'divSelectTableNumForTs'
		});
		Util.dialongDisplay({
			type:'hide', 
			renderTo:'divShowMessageForTableSelect'
		});
		inputNumVal = "";
		$("#txtTableNumForTS").val();
		$('#divSelectTablesForTs').html("");
		$("#txtPeopleNumForSM").val(inputNumVal);
		
		//同时操作餐台时,选中状态没变化的餐桌处理
		//直接写台豪点菜时判断是否已点菜, 是则先给co.order.orderFoods赋值
		if(getTableByAlias(tableNo).statusValue == 1){
//			uo.show({
//				table : getTableByAlias(tableNo),
//				type : 'createOrder',
//			});
//			co.show({
//				table : uo.table,
//				order : uo.order,
//				callback : function(){
//					initTableData();
//				}
//			});
//			uo.show({
//				table : getTableByAlias(tableNo),
//				type : 'createOrder',
//			});
			initOrderData({
				table : getTableByAlias(tableNo),
				createrOrder : 'createrOrder'
			});
//			co.show({
//				table : uo.table,
//				order : uo.order,
//				callback : function(){
//					initTableData();
//				}
//			});
		}else{
			var theTable = getTableByAlias(tableNo);
			theTable.customNum = peopleNo;
			co.show({
				table : theTable,
				callback : function(){
					initTableData();
				}
			});
		}		
	}else{
		Util.msg.alert({
			title : '温馨提示',
			msg : '没有该餐桌，请重新输入一个桌号.', 
			time : 2,
		});
	}
}

/**
 * 桌号人数界面的点菜按钮
 */
ts.createOrderForShowMessageTS = function(){
	var tableNo;
	var peopleNo;
	tableNo = parseInt($("#txtTableNumForSM").val());
	if($("#txtPeopleNumForSM").val() == ""){
		peopleNo = 0;
	}else{
		peopleNo = parseInt($("#txtPeopleNumForSM").val());
	}	
	renderToCreateOrder(tableNo, peopleNo);
};

/**
 *桌号输入页面的确定按钮
 */
ts.submitForSelectTableNumTS = function(){
	//获得餐桌号
	var tableNo;
	var peopleNo = 1;
	if($("#txtTableNumForTS") == ""){
		tableNo = -1;
	}else{
		tableNo = parseInt($("#txtTableNumForTS").val());
	}
	renderToCreateOrder(tableNo, peopleNo);	
};

function ts_transTable(c){
	$.post('../TransTable.do', {
		oldTableAlias : uo.table.alias,
		newTableAlias : c.alias
	},function(data){
		if(data.success){
			$('#btnCloseForSelectTableNumTS').click();
			Util.msg.alert({
				title : '温馨提示',
				msg : data.msg, 
				time : 2
			});				
			//返回主界面
			Util.toggleContentDisplay({type:'hide', renderTo:'divUpdateOrder'});
			initTableData();			
		}else{
			Util.msg.alert({
				title : '温馨提示',
				msg : data.msg, 
				time : 2
			});				
		}			
	});	
}

function uo_transFood(c){
	ts.tf.count = $('#txtFoodNumForTran').val();
	$.post('../TransFood.do', {
		orderId : uo.order.id,
		aliasId : c.alias,
		transFoods : (c.allTrans?c.allTrans:(ts.tf.id + ',' + ts.tf.count))		
	},function(data){
		if(data.success){
			$('#btnCloseForSelectTableNumTS').click();
			Util.msg.alert({
				title : '温馨提示',
				msg : data.msg, 
				time : 2
			});				
			//刷新已点菜
			updateTable({
				alias : uo.table.alias
			});
			ts.tf={};
		}else{
			Util.msg.alert({
				title : '温馨提示',
				msg : data.msg, 
				time : 2
			});				
		}			
	});
}

ts.submitForSelectTableOrTransFood = function(){
	if(ts.commitTableOrTran == 'table'){
		ts.submitForSelectTableNumTS();
	}else if(ts.commitTableOrTran == 'trans'){
		uo_transFood({alias:$('#txtTableNumForTS').val()});
	}else if(ts.commitTableOrTran == 'allTrans'){
		uo_transFood({alias:$('#txtTableNumForTS').val(), allTrans : -1});
	}else if(ts.commitTableOrTran == 'transTable'){
		ts_transTable({alias:$('#txtTableNumForTS').val()})
	}
}

ts.toOrderFoodOrTransFood = function(alias){
	if(ts.commitTableOrTran == 'table'){
		renderToCreateOrder(alias, 1);
	}else if(ts.commitTableOrTran == 'trans'){
		uo_transFood({alias:alias});
	}else if(ts.commitTableOrTran == 'allTrans'){
		uo_transFood({alias:alias, allTrans : -1});
	}else if(ts.commitTableOrTran == 'transTable'){
		ts_transTable({alias:alias})
	}
}


//点击工具栏上的点菜按钮，弹出桌号选择框，能够转到点菜页面
function createOrderForTS(){
	$('#divTransFoodNumber').hide();
	$('#divTransFoodTableAlias > span').removeClass('trans-food-label');
	$('#divTransFoodTableAlias > span').addClass('select-food-label');
	ts.commitTableOrTran = 'table';
	showSelectTableNumTS();
	var title = "";
	title = "请输入桌号，确定进入点菜界面";
	$("#divTopForSelectTableNumTS").html("<div style = 'font-size: 20px; " +
			"font-weight: bold; color: #fff; margin: 15px;'>" + title + "</div>");	
}



/**
 * 刷新
 */
function reFreshForTS(){
	initTableData();
	initFoodData();
}

/**
 * 转台按钮
 */
function transTableForTS(){
	Util.dialongDisplay({
		type : 'show',
		renderTo : 'divTransTableForTableSelect'
	});
	$("#txtOldTableForTS").focus();
	inputNumId  = "txtOldTableForTS";
}

/**
 * 确定转台操作
 */
ts.tt.submit = function(){
	var oldTable = $("#txtOldTableForTS").val();
	var newTable = $("#txtNewTableForTS").val();
	var oldflag = false, newflag = true;
	//判断餐桌是否符合条件
	if(hasTable(tables, oldTable)){
		if(getTableByAlias(oldTable).statusValue == 1){
			oldflag = true;
		}else{
			Util.msg.alert({
				title : '温馨提示',
				msg : oldTable + '号桌不是就餐状态，不能转台.', 
				time : 2,
			});
			return;
		}
	}else{
		Util.msg.alert({
			title : '温馨提示',
			msg : '没有' + oldTable + '号桌，请重新输入一个桌号.', 
			time : 2,
		});
		return;
	}
	if(hasTable(tables, newTable)){
		if(getTableByAlias(newTable).statusValue == 0){
			newflag = true;
		}else{
			Util.msg.alert({
				title : '温馨提示',
				msg : newTable + '号桌不是空台，不能转台.', 
				time : 2,
			});
			return;
		}
	}else{
		Util.msg.alert({
			title : '温馨提示',
			msg : '没有' + newTable + '号桌，请重新输入一个桌号.', 
			time : 2,
		});
		return;
	}
	//提交转台信息
	if(oldflag && newflag){
		Util.LM.show();
		$.ajax({
			url : '../TransTable.do',
			type : 'post',
			data : {
				oldTableAlias : oldTable,
				newTableAlias : newTable
			},
			success : function(data, status, xhr){
				Util.LM.hide();
				if(data.success){
					Util.msg.alert({
						title : data.title,
						msg : data.msg,
						time : 3,
						fn : function(btn){
							ts.tt.back();
							initTableData();
						}
					});
				}else{
					Util.msg.alert({
						title : data.title,
						msg : data.msg,
						time : 2,
					});
				}
			},
			error : function(request, status, err){
				Util.msg.alert({
					title : '错误',
					msg : err, 
					time : 2,
				});
			}
		});
	}
};


/**
 * 转台操作返回
 */
ts.tt.back = function(){
	Util.dialongDisplay({
		type : 'hide',
		renderTo : 'divTransTableForTableSelect',
	});
	inputNumVal = '';
	$("#txtOldTableForTS").val(inputNumVal);
	$("#txtNewTableForTS").val(inputNumVal);
};

/**
 * 选择输入框
 */
ts.selectInput = function(c){
	var renderTo = c.renderTo;
	inputNumId = renderTo;
	inputNumVal = "";
	inputNumVal += $("#" + inputNumId).val();
};

/**
 * 选中
 */
ts.selectingTxt = function(c){
	var renderTo = c.renderTo;
	inputNumId = renderTo;
	inputNumVal = "";
	$("#" + inputNumId).select();
};

//弹出和关闭桌号选择界面
function showSelectTableNumTS(){
	Util.dialongDisplay({
		type : 'show',
		renderTo : 'divSelectTableNumForTs'
	});

	//关闭该界面
	$("#btnCloseForSelectTableNumTS").click(function(){
		Util.dialongDisplay({
			type : 'hide',
			renderTo : 'divSelectTableNumForTs'
		});
		inputNumVal = "";
		$("#txtTableNumForTS").val("");
		$('#divSelectTablesForTs').html("");
	});
	$("#txtTableNumForTS").select();
	inputNumId  = "txtTableNumForTS";
}

/**
 * 清除一位数字
 */
ts.backOne = function(){
	var tempNum;
	tempNum = $("#" + inputNumId).val();
	if(tempNum.length > 0){
		inputNumVal = tempNum.substring(0, tempNum.length-1);
		$("#" + inputNumId).val(inputNumVal);
	}
	$("#" + inputNumId).focus();
	if(getDom(inputNumId).oninput){
		getDom(inputNumId).oninput();
	}	
};
/**
 * 重置数字
 */
ts.backAll = function(){
	inputNumVal = "";
	$("#" + inputNumId).val(inputNumVal);
	$("#" + inputNumId).focus();
};

