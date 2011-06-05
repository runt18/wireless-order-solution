function canEditOrder(id, total_price_2, target, type_value, table_id, category_value) {
    var editType = "canEditOrder";
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">修改帐单</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="orderForm" action="' + target + '"  method="post" onkeydown="editOrderKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<input type="hidden" name="total_price_2" value="' + total_price_2 + '" />' +
	                      '<input type="hidden" name="target" value="' + target + '" />' +
	                      '<input type="hidden" name="type_value" value="' + type_value + '" />' +
	                      '<input type="hidden" name="table_id" value="' + table_id + '" />' +
	                      '<input type="hidden" name="category_value" value="' + category_value + '" />' +
	                      '<div class="add_foot_Content" style="height:130px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                           '<div class="pop_Content1">请输入权限密码：<input type="password" id="pwd2" name="pwd2" size="25" height="20"/></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitCanEdit()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    document.getElementById("pwd2").focus();
}
function submitCanEdit() {
    var pwd2 = document.getElementById("pwd2").value;
    if (pwd2 == undefined || pwd2 == null || pwd2 == "") {
        alert("权限密码不能为空！");
        return;
    }
    document.orderForm.submit();
}
function editOrder(id, total_price_2, target, type_value, table_id, category_value) {    
    var editType = "editOrder";
    var content = ' <div class="add_foot">' +
                        '<div class="title">' +
	                        '<div class="title_left"><font class="font" style="width:350px;">修改</font></div>' +
	                        '<div class="title_right"></div>' +
	                    '</div>' +
	                    '<form name="orderForm" action="' + target + '"  method="post" onkeydown="editOrderKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="id" value="' + id + '" />' +
	                      '<div class="add_foot_Content" style="height:200px;text-align:center">' +
	                        '<div class="pop_Content">' +
	                           '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;台号：<input type="text" id="table_id" name="table_id" value="' + table_id + '" size="25" height="20" onfocus="this.select()" ' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                             '<div class="pop_Content1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类型：<select id="sel_category" name="sel_category">' +
	                                    '<option value="1" selected="selected">一般</option>' +
	                                    '<option value="2">外卖</option>' +
	                                    '<option value="3">拼台</option>' +
	                                    '</select></div>' +	                                    
	                            '<div class="pop_Content1">结帐方式：<select id="sel_type" name="sel_type">' +
	                                    '<option value="1" selected="selected">现金</option>' +
	                                    '<option value="2">刷卡</option>' +
	                                    '<option value="3">会员卡</option>' +
	                                    '<option value="4">挂账</option>' +
	                                    '<option value="5">签单</option>' +
	                                    '</select></div>' +
	                            '<div class="pop_Content1">实收金额：<input type="text" id="total_price_2" name="total_price_2" onfocus="this.select()" size="25" height="20" value="' + total_price_2 + '" ' +
	                            ' onkeypress="return event.keyCode>=48&&event.keyCode<=57||event.keyCode==46||event.keyCode==45"' +
	                            ' onpaste="return !clipboardData.getData(&quot;text&quot;).match(/\D/)" ondragenter="return false" ' +
	                            ' style="ime-mode:Disabled" /></div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="submitOrderData()">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 342, 350);
    document.getElementById("table_id").focus();
    document.all.sel_type.value = type_value;
    document.all.sel_category.value = category_value;
}

function editOrderKeyDown() {
    if (event.keyCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        submitOrderData();
    }
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        closeWindow();
    }
}

function submitOrderData() {
    var total_price_2 = document.getElementById("total_price_2").value;
    if (total_price_2 == undefined || total_price_2 == null || total_price_2 == "") {
        alert("实收金额不能为空！");
        return;
    }
    document.orderForm.submit();
}

function showSearch(target) {
    var content = '<div id="Advanced_Search">' +
                    '<div class="title">' +
                        '<div class="title_left" style="width: 451px;"><font class="font">高级搜索</font></div>' +
                        '<div class="title_right" style="float:left"></div>' +
                    '</div>' +
                     '<form id="searchForm" name="searchForm" action="' + target + '"  method="post" onkeydown="searchOrderKeyDown()">' +
                    '<div class="Advanced_Search_Content" style="width:480px;height:180px">' +
                        '<div class="pop_Content">' +
                            '<div class="pop_Content1">日期：<input type="text" id="dateFrom" name="dateFrom" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;</div>' +
                            '<div class="pop_Content1">金额：<input type="text" name="priceFrom" style="width:136px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" name="priceTo" style="width:136px" onkeypress="return event.keyCode>=48&&event.keyCode<=57" /></div>' +
                            '<div class="pop_Content2">台号：<input type="text" name="alias_id" size="10" height="20" onkeypress="return event.keyCode>=48&&event.keyCode<=57" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;结帐方式：<select id="type" name="type"><option value="" selected="selected">全部</option><option value="1">现金</option><option value="2">刷卡</option>	<option value="3">会员卡</option><option value="4">挂账</option><option value="5">签单</option></select></div>' +
                            '<div class="pop_Content2">类型：<select id="category" name="category"><option value="" selected="selected">全部</option><option value="1">一般</option><option value="2">外卖</option>	<option value="3">拼台</option></select></div>' +
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
function showOrderDetail(id, alias, date, totalPrice, count, foods, isPaid, owner_name, type_name, totalPrice_2, category_name, comment) {
    var f = "";
    var fs = foods.split(";");
    for (var i = 0; i < fs.length; i++) {
        var str = fs[i].split("|");
        //        f += "<li><table width='100%' align='left'><tr><td align='left' width='70%'>菜" + (i + 1) + "：" + str[0] + "</td><td align='right' width='30%'>￥" + str[1] + "</td></tr></table></li>"
        f += "<li><table width='100%' align='left'><tr><td align='left' width='70%'>菜" + (i + 1) + "：" + str[0] + "(" + formatNum(str[1]) + ")" + str[2] + str[3] + "</td><td align='right' width='30%'>￥" + str[4] + "</td></tr></table></li>";
        // f += "<li>菜" + (i + 1) + "：" + str[0] + "&nbsp;&nbsp;&nbsp;&nbsp;￥" + str[1] + "</td></tr></table></li>";

    }
    var isPaidStr = "";
    if (isPaid == "0") {
        isPaidStr = "未结帐";
    } else {
        isPaidStr = "已结帐(" + type_name + ")";
    }
    var showTotalPrice_2 = "";
    var showCategoryName = "";
    var showComment = "";
    if (!(totalPrice_2 == undefined || totalPrice_2 == null || totalPrice_2 == "")) {
      showTotalPrice_2 = '<li>' +
								'<div align="right" style="font-size:15px"><strong>实收：￥' + totalPrice_2 + '</strong></div>' +
							  '</li>';
  }
  if (!(category_name == undefined || category_name == null || category_name == "")) {
      showCategoryName = '(' + category_name + ')';
  }
  if (!(comment == undefined || comment == null || comment == "")) {
      showComment = '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;备注：' + comment + '</div>';
  }
    var content = '<div id="Has_order">' +
					  '<div class="title">' +
						'<div class="title_left"><font class="font" style="width:160px">查看 （' + id + '号帐单）</font></div>' +
						'<div class="title_right"></div>' +
					  '</div>' +
					  '<div class="Has_order_Content">' +
						'<div class="pop_Content">' +
		                  '<div class="pop_Content3">帐单号：' + id + showCategoryName + '</div>' +
						  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;台号：' + alias + '</div>' +
						  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;日期：' + date + '</div>' +
    //'<div class="pop_Content3">&nbsp;&nbsp;&nbsp;结帐方式：' + type_name + '</div>' +
						  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;人数：' + count + '</div>' +
		                  '<div class="pop_Content3">服务员：' + owner_name + '</div>' +
		                  '<div class="pop_Content3">&nbsp;&nbsp;&nbsp;状态：' + isPaidStr + '</div>' +
		                  showComment +
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
							        showTotalPrice_2 +
							'</ul>' +
						  '</div>' +
						'<span class="action-span" style="height:40px;margin-left:60px;"><a href="#" onclick="closeWindow()">确&nbsp;&nbsp;&nbsp;&nbsp;定</a></span><span class="action-span"><a href="#" onclick="printreport()">打&nbsp;&nbsp;&nbsp;&nbsp;印</a></span>' +
					  '</div>' +
					'</div>';
    showMessageBox(content, 342, 350);
}

function deleteOrder(id, target) {
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
    document.getElementById("category").style.display = "none";
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
    if (option.value == "is_category") {
        document.getElementById("category").style.display = "inline";
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
    var type_value = document.getElementById("type_value").value;
    var category = document.getElementById("category");
    var category_value = document.getElementById("category_value").value;
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
    for (var i = 0; i < category.options.length; i++) {
        if (category.options[i].value == category_value) {
            category.options[i].selected = true;
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
    if (keyword_type_value == "is_category") {
        document.getElementById("category").style.display = "inline";
        document.getElementById("keyword").style.display = "none";
    }
}

function adminIninOriginal() {
    var keyword_type = document.getElementById("keyword_type");
    var keyword_type_value = document.getElementById("keyword_type_value").value;
    var keyword = document.getElementById("keyword");
    var keyword_value = document.getElementById("keyword_value").value;
    var condition_type = document.getElementById("condition_type");
    var condition_type_value = document.getElementById("condition_type_value").value;
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
    keyword.value = keyword_value;
    if (keyword_type_value == "is_Price" || keyword_type_value == "is_day") {
        document.getElementById("condition_type").style.display = "inline";
    }
    if (keyword_type_value == "is_day") {
        keyword.onclick = showCal;
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
    var title = "";
    if (statType == "daily") {
        title = "日结汇总 - 请选择日期区间";
    }
    else {
        title = "月结汇总 - 请选择日期区间";
    }
    var content = ' <div class="add_foot">' +
                        '<div class="title" style="width:430px">' +
	                        '<div class="title_left" style="width:385px;"><font class="font" style="width:260px;">' + title + '</font></div>' +
	                        '<div class="title_right" style="float:left;width:35px;"></div>' +
	                    '</div>' +
	                    '<form id="searchForm" name="searchForm" action="order_history.php"  method="post" onkeydown="searchOrderKeyDown()">' +
	                      '<input type="hidden" name="editType" value="' + editType + '" />' +
	                      '<input type="hidden" name="statType" value="' + statType + '" />' +
	                      '<div class="add_foot_Content" style="height:130px;text-align:center;width:413px">' +
	                        '<div class="pop_Content">' +
	                         '<div class="pop_Content1" style="padding-left:0px;text-align:center"><input type="radio" checked="checked" name="viewType" value="total_price_2" />按实收&nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" name="viewType" value="total_price" />按金额</div>' +
	                           '<div class="pop_Content1" style="padding-left:0px;text-align:center">日期：<input type="text" id="dateFrom" name="dateFrom" style="width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;&nbsp;至&nbsp;&nbsp;<input type="text" id="dateTo" name="dateTo" style="width:width:136px" onclick="javascript:ShowCalendar(this.id)" />&nbsp;</div>' +
	                        '</div>' +
	                            '<span class="pop_action-span"><a href="#" onclick="document.searchForm.submit();">确&nbsp;&nbsp;&nbsp;&nbsp;认</a></span>' +
	                            '<span class="pop_action-span1"><a href="#" onclick="closeWindow()">取&nbsp;&nbsp;&nbsp;&nbsp;消</a></span>' +
	                      '</div>' +
	                      '</form>' +
	                '</div>';

    showMessageBox(content, 430, 350);
    document.getElementById("dateFrom").focus();
}

function showOrderStat(statType, dateFrom, dateTo,viewType) {
    var title = "";
    var viewTypeName = "按实收";
    if (viewType == "total_price") {
        viewTypeName = "按金额";
    }
    if (statType == "daily") {
        title = viewTypeName + "日结汇总";
        if (dateFrom != "" && dateTo != "") {
            title += "（" + dateFrom + "~" + dateTo + "）";
        }
        else if (dateFrom != "" && dateTo == "") {
            title += "（" + dateFrom + "之后）";
        }
        else if (dateFrom == "" && dateTo != "") {
            title += "（" + dateTo + "之前）";
        }
    }
    else {
        title = viewTypeName + "月结汇总";
        var df = dateFrom.substring(0, dateFrom.lastIndexOf("-"));
        var dt = dateTo.substring(0, dateTo.lastIndexOf("-"));
        if (dateFrom != "" && dateTo != "") {
            title += "（" + df + "~" + dt + "）";
            dateFrom = df + "-1";
            dateTo = dt + "-31";
        }
        else if (dateFrom != "" && dateTo == "") {
            title += "（" + df + "之后）";
        }
        else if (dateFrom == "" && dateTo != "") {
            title += "（" + dt + "之前）";
        }
    }


    var content = ' <div class="add_foot" style="height:550px;width:100%">' +
                        '<div class="title" style="width:100%">' +
	                        '<div class="title_left" style="width:914px"><font id="dynamicTitle" style="font-size: 16px;font-weight: normal;color: #FFF;margin-left: 15px;line-height: 30px;text-align: left;" >' + title + '</font></div>' +
	                        '<div class="title_right"  style="width:35px;float:left"></div>' +
	                    '</div>' +
	                      '<div class="add_foot_Content" style="height:370px;text-align:center;width:99%">' +
	                            '<iframe src="orderStat.php?statType=' + statType + '&dateFrom=' + dateFrom + '&dateTo=' + dateTo + '&viewType=' + viewType + '" scrolling="no" style="width:100%;height:100%;" />' +
	                        '</div>' +
	                '</div>';
    showMessageBox(content, 950, 350);
}

function orderStatKeyDown() {
    if (event.keyCode == 27) {
        event.returnValue = false;
        event.cancel = true;
        parent.closeWindow();
    }
}

function addTitle(title) {
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
                           '<div class="pop_Content2">餐厅：<select id="search_restaurant_type" name="search_restaurant_type" ><option value="Code" selected="selected">编号</option><option value="Name">名称</option></select>&nbsp;' +
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

function viewOrderDetail(id,table_name) {
    var content = ' <div class="add_foot" style="height:550px;width:100%">' +
                              '<div class="title" style="width:100%">' +
	                          '<div class="title_left" style="width:813px"><font class="font" style="width:200px">帐单明细(' + id + '号帐单)</font></div>' +
	                          '<div class="title_right" style="width:35px;float:left" ></div>' +
	                          '</div>' +
	                          '<div class="add_foot_Content" style="height:370px;text-align:center;width:99%">' +
	                              '<iframe src="viewOrderDetail.php?id=' + id + '&table_name=' + table_name + '" scrolling="no" style="width:100%;height:100%" />' +
	                          '</div>' +
						   '</div>';
    showMessageBox(content, 850, 350);
}


