
function showSearch(target) {
    var content = '<div id="Advanced_Search">' +
                    '<div class="title">' +
                        '<div class="title_left" style="width: 451px;"><font class="font">高级搜索</font></div>' +
                        '<div class="title_right" style="float:left"></div>' +
                    '</div>' +
                     '<form id="searchForm" name="searchForm" action="' + target +'"  method="post" onkeydown="searchOrderKeyDown()">' +
                    '<div class="Advanced_Search_Content" style="width:480px;height:180px">' +
                        '<div class="pop_Content">' +
                            '<div class="pop_Content1">日期：<input type="text" id="dateFrom" name="dateFrom" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;</div>' +
                            '<div class="pop_Content2">结帐方式：<select id="type" name="type"><option value="" selected="selected">全部</option><option value="1">现金</option><option value="2">刷卡</option>	<option value="3">会员卡</option><option value="4">挂账</option><option value="5">签单</option></select></div>' +
                            '<div class="pop_Content1">金额：<input type="text" name="priceFrom" style="width:136px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" name="priceTo" style="width:136px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" /></div>' +
                            '<div class="pop_Content2">台号：<input type="text" name="alias_id" size="10" height="20" onkeypress="return event.keyCode>=48&&event.keyCode<=57" /></div>' +
                        '</div>' +
                        '<span class="action-span" style="margin-left:110px;"><a href="#"  onclick="document.searchForm.submit();">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
                         '<span class="action-span1" style="margin-right:100px;"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
                    '</div>' +
                    '</form>' +
                '</div>';
    showMessageBox(content, 540, 188);
    document.getElementById("dateFrom").focus();
}


function searchOrderKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        document.searchForm.submit();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}
function showOrderDetail(id, alias, date, totalPrice, count, foods, isPaid,owner_name,type_name) {
    var f = "";
    var fs = foods.split(",");
    for (var i = 0; i < fs.length; i++) {
        var str = fs[i].split("|");
        //        f += "<li><table width='100%' align='left'><tr><td align='left' width='70%'>菜" + (i + 1) + "：" + str[0] + "</td><td align='right' width='30%'>￥" + str[1] + "</td></tr></table></li>"
        f += "<li><table width='100%' align='left'><tr><td align='left' width='70%'>菜" + (i + 1) + "：" + str[0] + "(" + formatNum(str[1]) + ")</td><td align='right' width='30%'>￥" + str[2] + "</td></tr></table></li>";
        // f += "<li>菜" + (i + 1) + "：" + str[0] + "&nbsp;&nbsp;&nbsp;&nbsp;￥" + str[1] + "</td></tr></table></li>";

    }
	var isPaidStr = "";
	if(isPaid == "0"){
		isPaidStr = "未结帐";
	}else{
		isPaidStr = "已结帐";
	}
    var content = '<div id="Has_order">' +
					  '<div class="title">' +
						'<div class="title_left"><font class="font" style="width:160px">查看 （' + id + '号帐单）</font></div>' +
						'<div class="title_right"></div>' +
					  '</div>' +
					  '<div class="Has_order_Content">' +
						'<div class="pop_Content">' +
		                  '<div class="pop_Content3">帐单号：' + id + '</div>' +
						  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;台号：' + alias + '</div>' +
						  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;日期：' + date + '</div>' +
						  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;结帐方式：' + type_name + '</div>' +
						  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;人数：' + count + '</div>' +
		                  '<div class="pop_Content3">服务员：' +  owner_name + '</div>' +
		                  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;状态：' +  isPaidStr + '</div>' +
						'</div>' +
						'<div class="title1"><font class="font1">已点菜</font></div>' +
						  '<div class="cls_container">' +
							'<ul>' +
							 f +
							'</ul>' +
							'<ul>' +
							  '<li>' +
								'<div align="right" style="font-size:15px"><strong>合计：￥' + totalPrice + '</strong></div>' +
							  '</li>' +
							'</ul>' +
						  '</div>' +
						'<span class="action-span" style="height:40px;margin-left:60px;"><a href="#" onclick="closeWindow()">确&nbsp;&nbsp;&nbsp;&nbsp;定</a></span><span class="action-span"><a href="#" onclick="printreport()">打&nbsp;&nbsp;&nbsp;&nbsp;印</a></span>' +
					  '</div>' +
					'</div>';
    showMessageBox(content, 342, 350);
}

function deleteOrder(id,target) {
    if (confirm("确认删除" + id + "号帐单的信息？")) {
        var formDelete = document.createElement("form");
        formDelete.action = target;
        formDelete.method = "post";
        var deleteId = document.createElement("input");
        deleteId.name = "deleteId";
        deleteId.value = id;
        deleteId.type = "hidden";
        formDelete.appendChild(deleteId);
        document.body.appendChild(formDelete);
        formDelete.submit();
    }
}

function dailyCheckOut(id) {
    if (confirm("确认要进行日结操作？")) {
        var formDelete = document.createElement("form");
        formDelete.action = "order.php";
        formDelete.method = "post";       
        var editType = document.createElement("input");
        editType.name = "editType";
        editType.value = "dailyCheckOut";
        editType.type = "hidden";
        formDelete.appendChild(editType);
        document.body.appendChild(formDelete);
        formDelete.submit();
    }
}


function showHideCondition(select) {
    document.getElementById("condition_type").style.display = "none";
    document.getElementById("type").style.display = "none";
    document.getElementById("keyword").style.display = "inline";
    var keyword = document.getElementById("keyword");
    keyword.value = "";
    keyword.onclick = null;
    keyword.value = "";
    var option = select.options[select.selectedIndex];
    if (option.value == "is_Price" || option.value == "is_day") {
        document.getElementById("condition_type").style.display = "inline";
    }           
    if (option.value == "is_day") {
        keyword.onclick = showCal;
    }
    if (option.value == "is_type") {
        document.getElementById("type").style.display = "inline";
        document.getElementById("keyword").style.display = "none";
    }   
}
function showCal() {
    ShowCalendar('keyword');
}

function ininOriginal() {
    var keyword_type = document.getElementById("keyword_type");
    var keyword_type_value = document.getElementById("keyword_type_value").value;
    var keyword = document.getElementById("keyword");
    var keyword_value = document.getElementById("keyword_value").value;
    var condition_type = document.getElementById("condition_type");
    var condition_type_value = document.getElementById("condition_type_value").value;
    var type = document.getElementById("type");
    var type_value = document.getElementById("type_value");
    for (var i = 0; i < keyword_type.options.length; i++) {
        if (keyword_type.options[i].value == keyword_type_value) {
            keyword_type.options[i].selected = true;
            break;
        }
    }
    for (var i = 0; i < condition_type.options.length; i++) {
        if (condition_type.options[i].value == condition_type_value) {
            condition_type.options[i].selected = true;
            break;
        }
    }
    for (var i = 0; i < type.options.length; i++) {
        if (type.options[i].value == type_value) {
            type.options[i].selected = true;
            break;
        }
    }
    keyword.value = keyword_value;
    if (keyword_type_value == "is_Price" || keyword_type_value == "is_day") {
        document.getElementById("condition_type").style.display = "inline";
    }
    if (keyword_type_value == "is_day") {        
        keyword.onclick = showCal;
    }
    if (keyword_type_value == "is_type") {
        document.getElementById("type").style.display = "inline";
        document.getElementById("keyword").style.display = "none";
    }
}

function formatNum(num) {
    var num1 = num.substr(num.length - 1, 1);
    if (num1 == "0") {
        num = num.substr(0, num.length - 1);
        var num2 = num.substr(num.length - 1, 1);
        if (num2 == "0") {
            num = num.substr(0, num.length - 2);
        }
    }

    return num;
}
function viewOrderStat(statType) {
    var editType = "viewStat";
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">日结/月结汇总 - 请选择日期区间</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form id="searchForm" name="searchForm" action="order_history.php"  method="post" onkeydown="searchOrderKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="statType" value="' + statType + '" />' +
	                      '<div class="add_foot_Content" style="height:130px;text-align:center">' +
	                        '<div class="pop_Content">' +	                          
	                           '<div class="pop_Content1" style="padding-left:0px;text-align:center">日期：<input type="text" id="dateFrom" name="dateFrom" style="width:100px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:100px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;</div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="document.searchForm.submit();">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    document.getElementById("dateFrom").focus();
}

function showOrderStat(statType,dateFrom,dateTo)
{
    var title = "";
    if(statType == "daily")
    {
        title = "日结汇总";
        if (dateFrom != "") {
            var t = dateFrom;
            if (dateTo != "") {
                t += "~" + dateTo;
            }
            title += "（" + t + "）";
        }
        else {
            if (dateTo != "") {
                title += "（" + dateTo + "）";

            }
        }
    }
    else
    {
        title = "月结汇总";
        if (dateFrom != "") {
            var t = dateFrom.substring(0, dateFrom.lastIndexOf("-"));
            if (dateTo != "") {
                t += "~" + dateTo.substring(0, dateTo.lastIndexOf("-"));
            }
            title += "（" + t + "）";
        }
        else {
            if (dateTo != "") {
                title += "（" + dateTo.substring(0, dateTo.lastIndexOf("-")) + "）";

            }
        }
    }
    
    
    var content = ' <div class="add_foot" style="height:550px;width:100%">' +
                        '<div class="title" style="width:100%">' +
	                        '<div class="title_left" style="width:95%"><font id="dynamicTitle" style="font-size: 16px;font-weight: normal;color: #FFF;margin-left: 15px;line-height: 30px;text-align: left;" >' + title + '</font></div>' +
	                        '<div class="title_right"  style="width:4%;float:left"></div>' +
	                    '</div>' +
	                      '<div class="add_foot_Content" style="height:370px;text-align:center;width:99%">' + 	                        
	                            '<iframe src="orderStat.php?StatType=' + statType + '&dateFrom=' + dateFrom + '&dateTo=' + dateTo + '" scrolling="no" style="width:100%;height:100%" />' +	                           
	                        '</div>' +	                      
	                '</div>';
	showMessageBox(content, 850, 350);
}

function orderStatKeyDown() {   
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        parent.closeWindow();
    }
}

function addTitle(title)
{
//    alert(parent.document.getElementById('title').innerText);
//    var t1 = parent.document.getElementById('title').innerText;
//    t1 += title;
//    alert(t1);
    parent.document.getElementById('dynamicTitle').innerText += ("                  总营业额：￥" + title);
}



function showAdminSearch() {    
    var content = '<div id="Advanced_Search">' +
                    '<div class="title">' +
                        '<div class="title_left"><font class="font" style="width:200px;">高级搜索（帐单）</font></div>' +
                        '<div class="title_right"></div>' +
                    '</div>' +
                     '<form name="searchForm" action="admin_order.php"  method="post" onkeydown="searchOrderKeyDown()">' +
                    '<div class="Advanced_Search_Content" style="height:210px">' +
                        '<div class="pop_Content" >' +
                            '<div class="pop_Content2">台号：<input type="text" id="alias_id" name="alias_id" size="20" height="20" style="width:120px;" onkeypress="return event.keyCode>=48&&event.keyCode<=57" /></div>' +
                           '<div class="pop_Content2">餐厅：<select id="search_restaurant_type" name="search_restaurant_type" ><option value="Code" selected="selected">编号</option><option value="Name">名称</option></select>&nbsp;'+
                            '<input type="text" id="restaurant" name="restaurant" size="20" height="20" style="width:120px;"/></div>' +  
                            '<div class="pop_Content1">日期：<input type="text" id="dateFrom" name="dateFrom" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;</div>' +
                            '<div class="pop_Content1">金额：<input type="text" name="priceFrom" style="width:136px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" name="priceTo" style="width:136px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" /></div>' +                           
                        '</div>' +
                        '<span class="action-span"><a href="#"  onclick="document.searchForm.submit();">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
                         '<span class="action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
                    '</div>' +
                    '</form>' +
                '</div>';  
                showMessageBox(content, 540, 188);
                document.getElementById("alias_id").focus();
}


