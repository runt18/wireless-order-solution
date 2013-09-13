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
//定义输入框id
var inputNumId;
//定义输入框显示的值
var inputNumVal = "";
//设置输入桌号界面的类型
var typeForInputTableNum;
//设置当前状态类型（busy， free, allStatus）
var statusType = "";

$(function(){
	/**
	 * 餐桌分页包
	 */
	ts.tp = new Util.padding({
		renderTo : 'divTableShowForSelect',
		displayId : 'divDescForTableSelect-padding-msg',
		templet : function(c){
			return Templet.ts.boxTable.format({
				dataIndex : c.dataIndex,
				alias : c.data.alias,
				dataClass : c.data.statusValue == '1' ? "\"main-box-base table-busy\"" : "\"main-box-base\"",
				tableName : c.data.name == "" || typeof c.data.name != 'string' ? c.data.alias + "号桌" : c.data.name,
				customNum : c.data.statusValue == 1 ? c.data.customNum + '人用餐' : '',
			});
		}
	});
});

/**
 * 定时器，定时刷新餐桌选择页面数据
 */
window.setInterval("initTables()", 240000);

/**
 * 初始化餐桌信息，保存到tables数组中
 * freeTables存放空闲餐桌，busyTables存放就餐餐桌
 */
function initTables(){
	$('#spanStaffNameForDisplayToTS').html(staffData.staffName);
	// 加载菜单数据
	$.ajax({
		url : '../QueryTable.do',
		type : 'post',
		data : {
			random : Math.random(),
		},
		success : function(data, status, xhr){
			tables = [];
			busyTables = [];
			freeTables = [];
			region = [];
			regionId = [];
			data = eval("(" + data + ")");
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
				showRegion(region, ts.rn.pageNow);
				//默认显示全部状态下的全部区域
				statusType = "allStatus";
				$("#divAllStatus").css("backgroundColor", "#FFA07A");
				$("#busyForTableSelect").css("backgroundColor", "#D4F640");
				$("#freeForTableSelect").css("backgroundColor", "#D4F640");
				tempForAllStatus = tables;
				temp = tables;
				showTable(temp);
			}else{
				Util.msg.alert({
					title : data.title,
					msg : data.msg, 
				});
			}
		},
		error : function(request, status, err){
			Util.msg.alert({
				title : '温馨提示',
				msg : err, 
			});
		}
	});	
}

/**
 * 显示区域
 */
function showRegion(temp, pageNow){
	//添加区域信息
	var pageRegion;
	var limit = ts.rn.limit;
	pageRegion = getPagingData((pageNow-1) * limit, limit, temp, true);
	var regionHtml = "";
	for(x in pageRegion){
		regionHtml += "<div class='button-base regionSelect' id='region"+pageRegion[x].id+
			"' style='margin-bottom: 2px;' onclick='addTables(this)'>"+pageRegion[x].name+"</div>";		
	}
	$("#divShowRegion").html(regionHtml);
	//设置区域未选中状态的背景色（#D4F640）
	$(".button-base.regionSelect").css("backgroundColor", "#D4F640");
	$("#" + ts.rn.selectingId).css("backgroundColor", "#FFA07A");
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
	}else{
		$("#divTableShowForSelect").html("");
	}	
}

//判断是否有该餐桌alias
function hasTable(tableObject, tableNo){
	for(x in tableObject){
		if(tableNo == tableObject[x].alias){
			return true;
		}
	}
	return false;
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
 * 定义分页函数
 * @param {int} start 开始下标
 * @param {int} limit 一页最多显示的数目
 * @param {object} tempObject 需要分页的数组对象
 * @param {boolean} isPaging 是否需要分页
 * @returns {object} pageRoot 已经完成分页的数组对象
 */
function getPagingData(start, limit, tempObject, isPaging){
    var pageRoot = [];
    if(tempObject.length != 0 && isPaging){ 
    	var dataIndex = start, dataSize = limit;		
    	dataSize = (dataIndex + dataSize) > tempObject.length ? dataSize - ((dataIndex + dataSize) - tempObject.length) : dataSize;			
    	pageRoot = tempObject.slice(dataIndex, dataIndex + dataSize);	
    }else{
    	pageRoot = tempObject;
    }	
	return pageRoot;
}


/**
 * 区域上翻
 */
ts.rn.prePage = function(){
	if(ts.rn.pageNow <= 1){
		return;
	}else{
		ts.rn.pageNow = ts.rn.pageNow - 1;
		showRegion(region, ts.rn.pageNow);
	}
};

/**
 * 区域下翻
 */
ts.rn.nextPage = function(){
	if(ts.rn.pageNow == ts.rn.pageCount || ts.rn.pageNow == ts.rn.pageCount){
		return;
	}else{
		ts.rn.pageNow = ts.rn.pageNow + 1;
		showRegion(region, ts.rn.pageNow);
	}
};
